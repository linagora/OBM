#!/usr/bin/env python

import argparse
import datetime
import locale
import logging
import os.path
import sys

import obm.build as ob

"""
This script updates the changelog of Debian and RPM packages.
"""

def build_argument_parser(args):
    """
    Builds the argument parser. The *args* parameters should be a list of parameters to
    parse (eg, argv).
    """
    parser = argparse.ArgumentParser(description='Updates OBM changelogs')

    parser.add_argument('-c', '--config', help='build configuration file',
            default='build.cfg', dest='configuration_file', type=file)

    parser.add_argument('-V', '--version', help='version of OBM',
            default=None, dest='obm_version')

    parser.add_argument('-r', '--release', help='release of OBM',
            default=None, dest='obm_release')

    return parser

def update_changelogs(config, obm_version, obm_release, packages, date, obm_dir):
    for package_type in ['deb', 'rpm']:
        logging.info("Processing %s changelogs" % package_type)
        template_section = "%s_templates" % package_type
        template = config.get(template_section, 'release_changelog')
        changelog_updater = ob.ChangelogUpdater(changelog_template=template,
                package_type=package_type, date=date, sha1=None,
                mode=ob.ChangelogUpdater.APPEND, version=obm_version,
                release=obm_release)
        for package in packages:
            changelog = ob.get_changelog(package.name, package_type,
                    package.path)
            if os.path.exists(changelog):
                logging.info("Updating changelog %s" % changelog)
                changelog_updater.update(package.name, changelog)

def main():
    logging.basicConfig(level=logging.DEBUG)

    locale_name = 'en_US.UTF-8'
    logging.info("Setting locale to %s" % locale_name)
    locale.setlocale(locale.LC_ALL, locale_name)

    argument_parser = build_argument_parser(sys.argv)
    args = argument_parser.parse_args()

    config = ob.config.read_config(args.configuration_file)

    obm_version = args.obm_version if args.obm_version is not None else config.get('global', 'version')
    obm_release = args.obm_release if args.obm_release is not None else config.get('global', 'release')

    obm_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    available_packages = ob.config.read_packages(config, obm_dir)

    date = datetime.datetime.today()
    update_changelogs(config, obm_version, obm_release, available_packages, date, obm_dir)

if __name__ == "__main__":
    main()
