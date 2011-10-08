#!/usr/bin/env python

"""
OBM build script. This script is responsible for building the Debian and RPM
packages for OBM (please note that at this time, support for RPM is not
implemented yet). It is also able to upload the packages to the repository.

See the documentation at http://obm.org/doku.php?id=building_obm_packages
"""

import argparse
import datetime
import ConfigParser
import locale
import logging
import os.path
import re
import shutil
import string
import subprocess as subp
import sys

class Package(object):
    """
    Represents a Debian or RPM package. The *name* parameter is the name of the
    package, the *path* is its path relative to the build script, and
    *update_pom* should be set to **True** if the package contains one or more
    `pom.xml` files which should be updated.
    """
    def __init__(self, name, path, update_pom):
        self.name = name
        self.path = path
        self.update_pom = update_pom

class SCMManager(object):
    """
    The SCM manager is responsible for interacting with the version control
    system (only Git is supported at present). Upon creating the manager, it
    will retrieve the list of tags of the repository (which will be accessible
    via the *tags* field).
    """
    def __init__(self):
        self.tags = self._get_tags()
        self.sha1 = self._get_sha1()

    def _get_tags(self):
        output = subp.check_output(["git", "tag"])
        tags = [line for line in output.split("\n") if line]
        return tags

    def _get_sha1(self):
        output = subp.check_output(["git", "rev-parse", "HEAD"])
        sha1 = output.split("\n")[0]
        return sha1

    def commit(self):
        """
        Commits changes to the repository and triggers a pull.
        """
        subp.check_call(["git", "commit", "-a", "Removed snapshots"])

class PomUpdater(object):
    def __init__(self, package):
        self.package = package

    def remove_snapshot(self):
        pass

class ChangelogUpdater(object):
    """
    This object overwrites the Debian changelog in autocommit mode. Its
    constructor accepts the following parameters:
        - *scm_manager*: an instance of :class:`SCMManager`
        - *changelog_template*: the path to a Debian changelog template
    """
    def __init__(self, scm_manager, changelog_template):
        self.changelog_template = changelog_template 
        last_tag = scm_manager.tags[-1]
        print "--"
        print last_tag
        print "--"
        self.version = self._increment_tag(last_tag)
        self.date = datetime.datetime.today()
        self.sha1 = scm_manager.sha1

    def _increment_tag(self, last_tag):

        def increment_version(match):
            preceding_non_number_char = match.group(1)
            last_number_in_version = int(match.group(2))
            incremented_number = last_number_in_version + 1
            replacement = "%s%d" % (preceding_non_number_char,
                    incremented_number)

        current_tag = re.sub(r"([^\d])(\d+)$", increment_version, last_tag)
        current_tag_lower = current_tag.lower()
        return current_tag_lower

    def update(self, package_name, changelog):
        """
        Overwrites *changelog*, injecting *package_name* in the template.
        """
        short_sha1 = self.sha1[:9]
        changelog_date = self.date.strftime("%a, %e %b %Y %X +0000")
        params = dict(package_name=package_name,
                version=self.version, 
                year=self.date.year,
                month=self.date.month,
                day=self.date.day,
                hour=self.date.hour,
                minute=self.date.minute,
                second=self.date.second,
                microsecond=self.date.microsecond,
                sha1=self.sha1,
                short_sha1=short_sha1,
                changelog_date=changelog_date)
        template_content = None
        with open(self.changelog_template) as template_fd:
            template_content = template_fd.read()
        formatter = string.Formatter()
        changelog_content = formatter.format(template_content, **params)
        with open(changelog, 'w') as changelog_fd:
            changelog_fd.write(changelog_content)

class Packager(object):
    """
    Builds the Debian package. The constructor accepts the following parameters:
        - *package*: the :class:`Package` to build
        - *package_type*: should be either `deb` or `rpm`
        - *work_dir*: the path to the work directory where the packages will be
          built
        - *scm_manager*: an instance of :class:`SMManager`
        - *changelog_updater*: an instance of :class:`ChangelogUpdater` (may be
          **None**)
    """
    def __init__(self, package, package_type, work_dir, scm_manager,
            changelog_updater):
        self.package = package
        self.scm_manager = scm_manager
        self.changelog_updater = changelog_updater
        self.package_type = package_type
        self.work_dir = work_dir
        self._target_dir = os.path.join(self.work_dir, package.name)

    def copy(self):
        """
        Copies the packages to the work directory.
        """
        logging.info("Copying package %s" % self.package.name)
        logging.debug("Package %s: copying %s to %s" % (self.package.name,
            self.package.path, self._target_dir))
        shutil.copytree(self.package.path, self._target_dir)        

    def update_changelog(self):
        """
        Triggers the overwrite of the changelog.
        """
        if self.changelog_updater:
            changelog = os.path.join(self._target_dir, 'debian', 'changelog')
            if not os.path.exists(changelog):
                raise PackagingError("No changelog for package %s (expected to "
                        "find one in %s)" % (self.package.name, changelog))
            self.changelog_updater.update(self.package.name, changelog)

    def build(self):
        """
        Builds the package itself.
        """
        logging.info("Building package %s" % self.package.name)
        command = None
        if self.package_type == "deb":
            command = ["debuild", "--no-tgz-check", "-us", "-uc", "-sa"]
        else:
            raise PackagingError("Unknown packaging type: %s" % \
                    self.package_type)
        subp.check_call(command, cwd=self._target_dir, stdout=sys.stdout,
                stderr=sys.stderr)

class PackageDeployer(object):
    """
    Deploys the Debian packages to a remote repository. The constructor accepts
    the following parameters:
        - *host*: the host to which the packages should be deployed (it should
          be a valid entry in **~/.dput.cf**)
        - *work_dir*: the work directory containing the packages and the .changes
          files
    """
    def __init__(self, host, work_dir):
        self.host = host
        self.work_dir = work_dir

    def deploy(self):
        """
        Uploads the packages to the remote repository.
        """
        logging.info("Deploying to %s" % self.host)
        logging.debug("Currently in directory %s" % self.work_dir)
        command = "dput '%s' *.changes" % self.host
        subp.check_call(command, cwd=self.work_dir,
                stdout=sys.stdout, stderr=sys.stderr, shell=True)

class PackageBuilder(object):
    """
    This object is responsible for chaining the packaging actions, from updating
    the pom file to copying to a remote repository.

    The constructor accepts the following parameters:
        - *scm_manager*: an instance of :class:`SCMManager`
        - *pom_updaters*: a list of :class:`PomUpdater` objects, may be empty
        - *packagers*: a list of :class:`Packager` objects
        - *deployer*: an instance of :class:`PackageDeployer`, may be **None**
    """
    def __init__(self, scm_manager, pom_updaters, packagers, deployer):
        self.scm_manager = scm_manager
        self.pom_updaters = pom_updaters
        self.packagers = packagers
        self.deployer = deployer

    def build(self):
        """
        This method will trigger the removal of the -SNAPSHOT from the pom.xml
        files, build the packages, and (only if the *scm_manager* field is not
        **None**) deploy to a remote repository and commit the changes in the
        pom.xml files.
        """
        package_names_string = ", ".join([p.package.name for p in self.packagers])
        logging.info("Building the following packages: %s" % \
                package_names_string)

        for pom_updater in self.pom_updaters:
            pom_updater.remove_snapshot()

        for packager in self.packagers:
            packager.copy()
            packager.update_changelog()
            packager.build()

        if self.deployer:
            self.deployer.deploy()
            if self.pom_updaters:
                self.scm_manager.commit()

class PackagingError(StandardError):
    """
    An error triggered during packaging.
    """

def read_packages(config):
    """
    Parses the package names for the configuration file and returns a list of
    :class:`Package` objects.
    """
    package_names = config.get('global', 'packages')
    packages = []
    for package_name in set(package_names.split(",")):
        stripped_package_name = package_name.strip()

        package_section_name = "package:%s" % stripped_package_name

        package_path = None

        end_of_path = None
        if config.has_option(package_section_name, 'path'):
            end_of_path = config.get(package_section_name, 'path')
        else:
            end_of_path = stripped_package_name
        package_path = os.path.join(os.path.dirname(os.path.abspath(
            os.path.dirname(__file__))), end_of_path)

        update_pom = None
        if config.has_option(package_section_name, 'update_pom'):
            update_pom = config.getboolean(package_section_name, 'update_pom')
        else:
            update_pom = False
        package = Package(stripped_package_name, package_path, update_pom)
        packages.append(package)
    return packages

def parse_args(args, packages):
    """
    Builds the argument parser and returns an object describing the options
    given to the script. The *args* parameters should be a list of parameters to
    parse (eg, argv), and *packages* should be a list of :class:`Packager`
    objects, representing the packages available for building.
    """
    parser = argparse.ArgumentParser(description='Builds obm')

    parser.add_argument('-o', '--oncommit', help='triggers an oncommit build',
            default=False, action='store_true')

    parser.add_argument('-H', '--host', help='host to upload to',
            default='24/private')

    package_types = ['deb', 'rpm']
    parser.add_argument('-p', '--packagetype', metavar='PACKAGETYPE',
            help="package type, may be one of: %s" % ", ".join(package_types),
            choices=package_types, default='deb')

    parser.add_argument('workdir', metavar='WORKDIR', help='directory where '
            'the packages will be built')

    goals = ['build', 'release']
    parser.add_argument('goal', metavar='GOAL', help="action to do, maybe one of: %s" % \
            (", ".join(goals)), choices=goals)

    package_names = [p.name for p in packages]
    package_names.append('all')
    parser.add_argument('packages', metavar='PACKAGES', nargs='+',
            help="packages to build, may be one or more of: %s" % \
                    ", ".join(package_names), choices=package_names)

    args = parser.parse_args()
    return args

def make_package_builder(packages, goal, package_type, work_dir, on_commit,
        autocommit_changelog_template, host):
    """
    Generates the :class:`PackageBuilder` instance. The parameters are:
        - *packages*: a list of :class:`Packager` objects to build 
        - *goal*: `'build'` or `'release'`
        - *package_type*: `'deb'` or `'rpm'`
        - *work_dir*: the work directory
        - *on_commit*: if **True**, the changelog will the overriden by a
          template
        - *host*: the host for deployment
    """
    scm_manager = SCMManager()
    deployer = None
    if goal == 'release':
        deployer = PackageDeployer(host, work_dir)

    pom_updaters = []
    if not on_commit:
        for package in packages:
            if package.update_pom:
                pom_updaters.append(PomUpdater(package))


    changelog_updater = None
    if on_commit:
        changelog_updater = ChangelogUpdater(scm_manager,
                autocommit_changelog_template)
    packagers = [Packager(p, package_type, work_dir, scm_manager,
        changelog_updater) for p in packages]
    package_builder = PackageBuilder(scm_manager, pom_updaters, packagers,
        deployer)
    return package_builder

def main():
    logging.basicConfig(level=logging.DEBUG)

    locale_name = 'en_US.UTF-8'
    logging.info("Setting locale to %s" % locale_name)
    locale.setlocale(locale.LC_ALL, locale_name)


    config_filepath = os.path.join(os.path.abspath(os.path.dirname(__file__)),
            "build.cfg")
    config = ConfigParser.RawConfigParser()
    with open(config_filepath) as config_fd:
        config.readfp(config_fd)
    available_packages = read_packages(config)

    args = parse_args(sys.argv, available_packages)

    package_names = set(args.packages)
    package_type = args.packagetype
    host = args.host 
    autocommit_changelog_template = config.get('autocommit',
        'changelog_template')

    if 'all' in package_names:
        packages = [p for p in available_packages]
    else:
        packages = [p for p in available_packages if p.name in package_names]

    on_commit = args.oncommit
    goal = args.goal
    work_dir = args.workdir

    package_builder = make_package_builder(packages, goal,
            package_type, work_dir, on_commit, autocommit_changelog_template,
            host)
    package_builder.build()

if __name__ == "__main__":
    main()
