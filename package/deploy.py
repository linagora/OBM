#!/usr/bin/env python

import argparse
import logging
import os.path
import sys

import obm.build as ob


def build_argument_parser():
    parser = argparse.ArgumentParser(description='Builds OBM')

    package_types = ['deb', 'rpm']

    parser.add_argument('-d', '--dry-run', action='store_true',
            help="Fakes a deployment", default=False, dest='dry_run')

    parser.add_argument('-H', '--host', help='host to upload to',
            default='24-oncommit')

    parser.add_argument('-p', '--package-type', metavar='PACKAGETYPE',
            help="package type, may be one of: %s" % ", ".join(package_types),
            choices=package_types, default='deb')

    parser.add_argument('work_dir', metavar='WORKDIR', help='directory where '
            'the packages will be built')
    return parser

def main():
    logging.basicConfig(level=logging.DEBUG)

    locale_name = 'en_US.UTF-8'

    argument_parser = build_argument_parser()
    args = argument_parser.parse_args()

    packages_dir = os.path.join(args.work_dir, args.package_type)
    deployer = ob.PackageDeployer(args.host, packages_dir, args.dry_run)
    deployer.deploy()

if __name__ == "__main__":
    main()
