#!/usr/bin/env python

"""
OBM build script. This script is responsible for building OBM as Debian and RPM packages.

Designed to run on Debian Squeeze.

Needs:
    python 2.x, >= 2.6
    python-argparse

Desgined to run on Debian Squeeze with packages:
    dev-scripts
    rpm

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
import multiprocessing
import errno

import obm.build as ob

def boolean_value_of(envstr):
    return envstr.lower() in ['true', 't', '1', 'yes']

def build_argument_parser(args):
    """
    Builds the argument parser. The *args* parameters should be a list of parameters to
    parse (eg, argv).
    """
    parser = argparse.ArgumentParser(description='Packages OBM')

    parser.add_argument('-o', '--oncommit', help="triggers an oncommit build. Defaults to $OBM_ONCOMMIT or 'False'",
            default=boolean_value_of(os.environ.get('OBM_ONCOMMIT', '0')), action='store_true', dest='oncommit')

    parser.add_argument('-c', '--config', help="build configuration file. Defaults to $OBM_BUILDCFG or './build.cfg'",
            default=os.environ.get('OBM_BUILDCFG', 'build.cfg'), dest='configuration_file', type=file)

    parser.add_argument('-V', '--version', help="version of OBM. Defaults to $OBM_VERSION or 'None'",
            default=os.environ.get('OBM_VERSION', None), dest='obm_version')

    parser.add_argument('-r', '--release', help="release of OBM. Defaults to $OBM_RELEASE or 'None'",
            default=os.environ.get('OBM_RELEASE', None), dest='obm_release')

    parser.add_argument('--perl-version', help="perl flavor (only for RPMs). Defaults to $OBM_PERLVERSION or '5.8'",
            default=os.environ.get('OBM_PERLVERSION', '5.8'), dest='perl_version', choices=['5.8', '5.10'])

    parser.add_argument('-n', '--nocompile', help="Do not attempt to compile anything. Defaults to $OBM_NOCOMPILE or 'False'",
            default=boolean_value_of(os.environ.get('OBM_NOCOMPILE', '0')), action='store_true', dest='nocompile')

    parser.add_argument('-P', '--processcount', help="set the number of concurrent processes usedused  to build the packages. "
                       "One process by Debian control file or RPM .SPEC file. Defaults to $OBM_PROCESSCOUNT or "
                       "number of available cores",
                       default=int(os.environ.get('OBM_PROCESSCOUNT', multiprocessing.cpu_count())), dest='processcount')

    package_types = ['deb', 'rpm']
    parser.add_argument('-p', '--package-type', metavar='PACKAGETYPE',
            help="package type, may be one of: %s. Defaults to $OBM_PACKAGETYPE or 'deb' " % ", ".join(package_types),
            choices=package_types, default=os.environ.get('OBM_PACKAGETYPE', 'deb'), dest='package_type')

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
    if args.oncommit:
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
    if args.oncommit:
        template_section = "%s_templates" % args.package_type
        template = config.get(template_section,
            'oncommit_changelog')
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
        perl_vendorlib, args.nocompile) for p in packages]
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

def launch_packager(packager):
    try:
        logging.info("\tBUILD PREPARE: %s" % packager.package.name)
        packager.prepare_build()
        logging.info("\tBUILD START: %s" % packager.package.name)
        packager.build()
        logging.info("\tBUILD COMPLETE: %s" % packager.package.name)
        return True
    except (Exception, SystemExit, KeyboardInterrupt) as ex:
        logging.exception("\tBUILD ERROR: %s" % packager.package.name)
        return False

def exit_failure(pool):
    logging.error("TERMINATING CHILD PROCESSES")
    pool.terminate()
    pool.join()
    logging.error("BUILD FAILURE")
    sys.exit(1)

def exit_success(pool):
    logging.info("WAITING FOR CHILD PROCESSES TO FINISH")
    pool.join()
    logging.info("BUILD SUCCESS")

def mkdir_lenient(path):
    try:
        os.mkdir(path)
    except (OSError) as err:
        if err.errno != errno.EEXIST:
            logging.exception("BUILD ERROR:")
            logging.error("BUILD FAILURE")
            sys.exit(1)
        else:
            logging.warn("It seems directory at %s already exists, skipping..." % path)

def prepare_dirs(workdir):
    mkdir_lenient(workdir)
    mkdir_lenient(workdir + os.sep + "log")

def main():
    logging.basicConfig(level=logging.DEBUG)

    locale_name = 'en_US.UTF-8'
    logging.info("Setting locale to %s" % locale_name)
    locale.setlocale(locale.LC_ALL, locale_name)

    argument_parser = build_argument_parser(sys.argv)
    args = argument_parser.parse_args()

    logging.info("The following parameters will be used from the environment and the command line :")
    argsd = vars(args)
    for key in argsd.iterkeys():
        logging.info("\t[%s] = %s" % (key, argsd[key]))
    logging.info("End of parameter list")

    config = ob.read_config(args.configuration_file)
    checkout_dir = os.path.dirname(os.path.abspath('.'))

    available_packages = ob.read_packages(config, checkout_dir)
    package_names = set(args.packages)
    assert_package_option_is_correct(argument_parser.format_usage(),
            package_names, available_packages)

    if 'all' in package_names:
        packages = [p for p in available_packages]
    else:
        packages = [p for p in available_packages if p.name in package_names]

    prepare_dirs(args.work_dir)

    packagers = make_packagers(config, args, args.work_dir, checkout_dir,
            packages)

    try:
        pool = multiprocessing.Pool(args.processcount)
        tasks = set([pool.apply_async(launch_packager, [packager]) for packager in packagers])
        pending_tasks = tasks.copy()
        pool.close()
        logging.info("Processing %d packaging tasks..." % len(tasks))
        while len(pending_tasks) > 0:
            for task in tasks:
                if task in pending_tasks and task.ready() is True:
                    if task.get() is False:
                        exit_failure(pool)
                    else:
                        pending_tasks.discard(task)
                        logging.info("%d packaging tasks remaining..." % len(pending_tasks))
                task.wait(1.)
        exit_success(pool)
    except (KeyboardInterrupt) as ex:
        logging.warning("KEYBOARD INTERRUPT")
        exit_failure(pool)

if __name__ == "__main__":
    main()
