#!/usr/bin/env python

"""
OBM build script. This script is responsible for building the Debian and RPM
packages for OBM (please note that at this time, support for RPM is not
implemented yet). It is also able to upload the packages to the repository.

See the documentation at http://obm.org/doku.php?id=building_obm_packages
"""

import argparse
import ConfigParser
import datetime
import locale
import logging
import os.path
import string
import sys

import obm.build as ob

def build_argument_parser(args):
    """
    Builds the argument parser. The *args* parameters should be a list of parameters to
    parse (eg, argv).
    """
    parser = argparse.ArgumentParser(description='Packages OBM')

    parser.add_argument('-o', '--oncommit', help='triggers an oncommit build',
            default=False, action='store_true', dest='on_commit')

    parser.add_argument('-c', '--config', help='build configuration file',
            default='build.cfg', dest='configuration_file', type=file)

    parser.add_argument('-V', '--version', help='version of OBM',
            default=None, dest='obm_version')

    parser.add_argument('-r', '--release', help='release of OBM',
            default=None, dest='obm_release')

    parser.add_argument('--perl-version', help='perl flavor (only for RPMs)',
            default='5.8', dest='perl_version', choices=['5.8', '5.10'])

    package_types = ['deb', 'rpm']
    parser.add_argument('-p', '--package-type', metavar='PACKAGETYPE',
            help="package type, may be one of: %s" % ", ".join(package_types),
            choices=package_types, default='deb', dest='package_type')

    parser.add_argument('work_dir', metavar='WORKDIR', help='directory where '
            'the packages will be built')

    parser.add_argument('packages', metavar='PACKAGES', nargs='+',
            help="packages to build, depending on the configuration file")

    return parser


def get_version_release(args, config, date, sha1):
    obm_version = args.obm_version if args.obm_version is not None else config.get('global', 'version')
    obm_release = args.obm_release if args.obm_release is not None else config.get('global', 'release')

    if not obm_version:
        raise ValueError("The obm version should be specified on the "
                "command line or in the configuration file")
    version = obm_version
    if args.package_type == 'rpm' and not obm_release:
        obm_release = 1
    short_sha1 = sha1[:7]
    if args.on_commit:
        formatter = string.Formatter()
        params = dict(obm_release=obm_release,
                year=date.strftime("%Y"),
                month=date.strftime("%m"),
                day=date.strftime("%d"),
                hour=date.strftime("%H"),
                minute=date.strftime("%M"),
                short_sha1=short_sha1)
        release = None
        if args.package_type == 'deb':
            release = formatter.format("{obm_release}+git{year}{month}{day}-"
                    "{hour}{minute}-{short_sha1}", **params)
        elif args.package_type == 'rpm':
            release = formatter.format("{obm_release}+git{year}{month}{day}_"
                    "{hour}{minute}_{short_sha1}", **params)
        else:
            raise ValueError("Unknown package type %s" % args.package_type)
    else:
        release = obm_release
    return version, release

def make_packagers(config, args, packages_dir, checkout_dir, packages):
    template = None
    if args.on_commit:
        template_section = "%s_templates" % args.package_type
        template = config.get(template_section,
            'autocommit_changelog')
        mode = ob.ChangelogUpdater.REPLACE
    else:
        template = None
        mode = ob.ChangelogUpdater.NO_UPDATE

    scm_manager = ob.SCMManager(checkout_dir)
    scm_manager.init()

    date = datetime.datetime.today()

    version, release = get_version_release(args, config, date, scm_manager.sha1)

    changelog_updater = ob.ChangelogUpdater(changelog_template=template,
            package_type=args.package_type, date=date, sha1=scm_manager.sha1,
            mode=mode, version=version, release=release)

    perl_section_name = "perl_%s" % args.perl_version
    perl_module_compat = config.get(perl_section_name, 'perl_module_compat')
    perl_vendorlib = config.get(perl_section_name, 'perl_vendorlib')

    packagers = [ob.Packager(p, args.package_type, packages_dir,
        changelog_updater, version, release, perl_module_compat,
        perl_vendorlib) for p in packages]
    return packagers

def assert_package_option_is_correct(usage, package_names, available_packages):
    """
    Checks that the list of packages selected is correct. If it's not the case,
    exits the program. The *usage* argument is the *usage* string from the
    argument parser. The *package_names* argument is the set of package
    names selected from building, while the *available_packages* argument is a
    list of :class:`Package` objects extracted from the configuration file.
    """
    available_package_names = set([p.name for p in available_packages])
    available_package_names.add('all')
    package_diff =  package_names - available_package_names
    if package_diff:
        label = "choice" if len(package_diff)==1 else "choices"
        formatted_package_diff = "'%s'" % "', '".join(package_diff)
        formatted_available_packages = "'%s'" % "', '".join(
                available_package_names)
        sys.stderr.write("%s\n" % usage)
        sys.stderr.write("%s: error: argument PACKAGES: invalid %s: %s (choose "
                "from %s)\n" % (__file__, label, formatted_package_diff,
                    formatted_available_packages))
        sys.exit(2)

def main():
    logging.basicConfig(level=logging.DEBUG)

    locale_name = 'en_US.UTF-8'
    logging.info("Setting locale to %s" % locale_name)
    locale.setlocale(locale.LC_ALL, locale_name)

    argument_parser = build_argument_parser(sys.argv)
    args = argument_parser.parse_args()

    config = ob.read_config(args.configuration_file)

    packages_dir = os.path.join(args.work_dir, args.package_type)

    checkout_dir = os.path.dirname(os.path.abspath('.'))

    available_packages = ob.read_packages(config, checkout_dir)

    package_names = set(args.packages)
    assert_package_option_is_correct(argument_parser.format_usage(),
            package_names, available_packages)

    if 'all' in package_names:
        packages = [p for p in available_packages]
    else:
        packages = [p for p in available_packages if p.name in package_names]

    packagers = make_packagers(config, args, packages_dir, checkout_dir,
            packages)

    for packager in packagers:
        packager.prepare_build()
        packager.build()

if __name__ == "__main__":
    main()
