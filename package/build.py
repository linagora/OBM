#!/usr/bin/env python

"""
OBM build script. This script is responsible for building the Debian and RPM
packages for OBM (please note that at this time, support for RPM is not
implemented yet). It is also able to upload the packages to the repository.

See the documentation at http://obm.org/doku.php?id=building_obm_packages
"""

import argparse
import xml.etree.cElementTree as etree
import datetime
import ConfigParser
import locale
import logging
import os
import os.path
import re
import shutil
import string
import subprocess as subp
import sys
import warnings

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
    via the *tags* field). The *version* argument is the version which will be
    committed, it can be either **None** (in which case the version used will be
    the last tag, with its number incremented), an existing revision or tag (in
    which case it will be checked out) or a commit object which does not exist yet.
    """

    GIT_CHECKOUT_FAILURE_CODE = 1

    def __init__(self, scm_repository, branch, checkout_dir, version=None):
        self.scm_repository = scm_repository
        self.branch = branch
        self.checkout_dir = checkout_dir
        self.version = version
        self.is_existing_revision = None
        self.tags = None
        self.sha1 = None
        self._git_env = {'GIT_SSL_NO_VERIFY': '0'}

    def _increment_tag(self, last_tag):

        def increment_version(match):
            preceding_non_number_char = match.group(1)
            last_number_in_version = int(match.group(2))
            incremented_number = last_number_in_version + 1
            replacement = "%s%d" % (preceding_non_number_char,
                    incremented_number)
            return replacement

        current_tag = re.sub(r"([^\d])(\d+)$", increment_version, last_tag)
        current_tag_lower = current_tag.lower()
        return current_tag_lower

    def checkout(self):
        """
        Checks out the sources to *checkout_dir* and retrieves the tags and
        sha1.
        """
        # Don't do a clone if the repository is the same as checkout dir, it
        # means that the repository is a local directory - in this case we
        # assume the checkout has already been performed
        if self.checkout_dir != self.scm_repository:
            checkout_parent_dir = os.path.dirname(self.checkout_dir)
            checkout_dir_name = os.path.basename(self.checkout_dir)

            if os.path.exists(checkout_parent_dir):
                raise PackagingError("The work directory '%s' already exists" % \
                        checkout_parent_dir)
            os.makedirs(checkout_parent_dir)

            logging.info("Checking out OBM sources from branch %s into %s" %
                    (self.branch, checkout_parent_dir))

            subp.check_call(["git", "clone", "-b", self.branch, self.scm_repository,
                checkout_dir_name], cwd=checkout_parent_dir,
                env=self._git_env)

        # Attempt to checkout the version, in case it is an existing git
        # revision or tag
        if self.version:
            return_code = subp.call(["git", "checkout", self.version],
                    cwd=self.checkout_dir, env=self._git_env)
            if return_code == 0:
                self.is_existing_revision = True
            elif return_code == self.GIT_CHECKOUT_FAILURE_CODE:
                self.is_existing_revision = False
            else:
                raise PackagingError("The git checkout process failed with return "
                        "code %d" % return_code)
        else:
            last_tag = self.tags[-1]
            self.version = self._increment_tag(last_tag)
        self.tags = self._get_tags()
        self.sha1 = self._get_sha1()

    def _get_tags(self):
        output = subp.check_output(["git", "tag"], cwd=self.checkout_dir)
        tags = [line for line in output.split("\n") if line]
        return tags

    def _get_sha1(self):
        output = subp.check_output(["git", "rev-parse", "HEAD"],
                cwd=self.checkout_dir)
        sha1 = output.split("\n")[0]
        return sha1

    def commit(self):
        """
        Commits and tags changes, and triggers a pull.
        """
        subp.check_call(["git", "commit", "-a", "Removed snapshots"],
                cwd=self.checkout_dir, env=self._git_env)
        subp.check_call("git", "tag", "-a", self.version, "-m",
                "Tagged for %s release" % self.version, env=self._git_env)
        subp.check_call(["git", "push", "--tags", "origin", self.branch],
                cwd=self.checkout_dir, env=self._git_env)

class NS:
    def __init__(self, uri):
        self.uri = uri
    def __getattr__(self, tag):
        return tag if tag == '.' else self.uri + tag
    def __call__(self, path):
        return "/".join(getattr(self, tag) for tag in path.split("/"))

class PomUpdater(object):
    """
    Updates the pom.xml files to remove snapshot version (including the one in
    *obm.version*.
    """

    POM_NS='http://maven.apache.org/POM/4.0.0'

    POM = NS("{%s}" % POM_NS)

    def __init__(self, packages):
        self.packages = packages
        self._pom_files = {}

    def _scan_poms(self):
        for package in self.packages:
            for root, dirs, files in os.walk(package.path):
                for f in files:
                    if f == 'pom.xml':
                        pom_file = os.path.join(root, f)
                        self._register_pom(pom_file)

    def _register_pom(self, pom_file):
        with open(pom_file) as pom_fd:
           doc = etree.parse(pom_fd)
           component_name = doc.findtext(self.POM('./artifactId'))
           component_group = doc.findtext(self.POM('./groupId'))
           if component_group is None:
               component_group = doc.findtext(self.POM('./parent/groupId'))
           component_full_name = "%s.%s" % (component_group, component_name)
           #if component_name != 'parent':
           version = doc.findtext(self.POM('./version'))
           if version is None:
               version = doc.findtext(self.POM('./parent/version'))
           if version is None:
               warnings.warn("No version tag found in %s" % pom_file)
           elif version.endswith('-SNAPSHOT'):
               self._pom_files[component_full_name] = pom_file

    def _remove_snapshot_from_version(self, version):
        new_version = re.sub(r'-SNAPSHOT$', '', version)
        return new_version

    def remove_snapshot(self):
        """
        Removes the snapshots from the pom.xml files.
        """
        logging.info("Removing snapshots from sources")
        self._scan_poms()
        for pom_file in self._pom_files.values():
            doc = None
            with open(pom_file) as pom_fd:
                doc = etree.parse(pom_fd)
            etree.register_namespace('', self.POM_NS)
            version_elts = []
            version_elts.append(doc.find(self.POM('./version')))
            version_elts.append(doc.find(self.POM('./parent/version')))
            for version_elt in version_elts:
                if version_elt is not None:
                    version_elt.text = self._remove_snapshot_from_version(
                            version_elt.text)

            # Update the 'obm version' property in the parent pom
            props_elt = doc.find(self.POM('./properties'))
            if props_elt:
                for prop in props_elt:
                    if prop.tag == self.POM('obm.version') and \
                        prop.text.endswith('-SNAPSHOT'):
                        prop.text = self._remove_snapshot_from_version(
                                prop.text)
                        break

            for dependency_elt in doc.findall(self.POM(
                './dependencies/dependency')):
                component_name = dependency_elt.findtext(self.POM(
                    './artifactId'))
                component_group = dependency_elt.findtext(self.POM(
                    './groupId'))
                component_full_name = "%s.%s" % (component_group,
                        component_name)
                version_elt = dependency_elt.find(self.POM('version'))
                if version_elt is not None and version_elt.text.endswith(
                        "-SNAPSHOT"):
                    version_elt.text = self._remove_snapshot_from_version(
                            version_elt.text)
            with open(pom_file, 'w') as pom_fd:
                doc.write(pom_fd)

class ChangelogUpdater(object):
    """
    This object updates the Debian changelog in autocommit mode. Its
    constructor accepts the following parameters:
        - *scm_manager*: an instance of :class:`SCMManager`
        - *changelog_template*: the path to a Debian changelog template
        - *mode*: one of `'replace'` or `'append'`. The first overwrites the
          changelog with the instancied template, the second inserts the
          contents of the instancied template at the top of the changelog.
    
    """
    REPLACE='replace'
    APPEND='append'


    def __init__(self, scm_manager, changelog_template, mode):
        self.scm_manager = scm_manager
        self.changelog_template = changelog_template
        self.date = datetime.datetime.today()
        accepted_mode_values = [self.REPLACE, self.APPEND]
        if mode not in accepted_mode_values:
            raise ValueError("The mode should be one of: "
                    "%s" % ", ".join(accepted_mode_values))
        self.mode = mode

    def update(self, package_name, changelog):
        """
        Overwrites *changelog*, injecting *package_name* in the template.
        """
        version = self.scm_manager.version

        sha1 = self.scm_manager.sha1
        short_sha1 = sha1[:9]

        changelog_date = self.date.strftime("%a, %e %b %Y %H:%M:%S +0000")
        params = dict(package_name=package_name,
                version=version, 
                year=self.date.year,
                month=self.date.month,
                day=self.date.day,
                hour=self.date.hour,
                minute=self.date.minute,
                second=self.date.second,
                microsecond=self.date.microsecond,
                sha1=sha1,
                short_sha1=short_sha1,
                changelog_date=changelog_date)
        template_content = None
        with open(self.changelog_template) as template_fd:
            template_content = template_fd.read()
        formatter = string.Formatter()
        changelog_content = formatter.format(template_content, **params)
        file
        if self.mode == self.REPLACE:
            with open(changelog, 'w') as changelog_fd:
                changelog_fd.write(changelog_content)
        elif self.mode == self.APPEND:
            old_changelog_content = None
            with open(changelog, 'r') as changelog_fd:
                old_changelog_content = changelog_fd.read()

            with open(changelog, 'w') as changelog_fd:
                changelog_fd.write(changelog_content)
                changelog_fd.write(old_changelog_content)
        else:
            raise ValueError("Unknown mode value: %s" % self.mode)

class Packager(object):
    """
    Builds the Debian package. The constructor accepts the following parameters:
        - *package*: the :class:`Package` to build
        - *package_type*: should be either `deb` or `rpm`
        - *build_dir*: the path to the work directory where the packages will be
          built
        - *scm_manager*: an instance of :class:`SMManager`
        - *changelog_updater*: an instance of :class:`ChangelogUpdater` (may be
          **None**)
    """

    NO_CHANGELOG_UPDATE='no_changelog_update'
    UPDATE_CHANGELOG_IN_SOURCES='update_changelog_in_sources'
    UPDATE_CHANGELOG_IN_BUILD='update_changelog_in_build'


    def __init__(self, package, package_type, build_dir, scm_manager,
            changelog_updater):
        self.package = package
        self.scm_manager = scm_manager
        self.changelog_updater = changelog_updater
        self.package_type = package_type
        self.build_dir = build_dir
        self._target_dir = os.path.join(self.build_dir, package.name)

    def prepare_build(self, changelog_update_mode):
        """
        Updates the changelog and copies the packages to the build directory.
        The *changelog_update_mode* determines how the operations are done. When
        set to `'no_changelog_update'`, no update of the changelog is performed.
        When set to `'update_changelog_in_sources'`, the changelog is updated
        in the sources before the copy (which means that a commit by
        :class:`SCMManager` will commit the changes as well). When set to
        `'update_changelog_in_build'`, the changelog is updated in the build
        directory after the copy.
        """
        valid_changelog_update_modes = [self.NO_CHANGELOG_UPDATE,
                self.UPDATE_CHANGELOG_IN_SOURCES,
                self.UPDATE_CHANGELOG_IN_BUILD]
        if changelog_update_mode not in valid_changelog_update_modes:
            raise ValueError("Expected changelog_update_mode to be "
                    "one of: %s" % ", ".join(valid_changelog_update_modes))

        if changelog_update_mode == self.UPDATE_CHANGELOG_IN_SOURCES:
            self._update_changelog(changelog_update_mode)
        self._copy()
        if changelog_update_mode == self.UPDATE_CHANGELOG_IN_BUILD:
            self._update_changelog(changelog_update_mode)


    def _copy(self):
        """
        Copies the packages to the work directory.
        """
        logging.info("Copying package %s" % self.package.name)
        logging.debug("Package %s: copying %s to %s" % (self.package.name,
            self.package.path, self._target_dir))
        shutil.copytree(self.package.path, self._target_dir)

    def _update_changelog(self, changelog_update_mode):
        """
        Triggers the overwrite of the changelog. The value of
        *changelog_update_mode* determines whether the changelog updated is the
        changelog of the sources (if set to `'update_changelog_in_sources'`) or
        in the build directory (`'update_changelog_in_build'`).
        """
        if self.changelog_updater:
            debian_parent_dir = None
            if changelog_update_mode == self.UPDATE_CHANGELOG_IN_SOURCES:
                debian_parent_dir = self.package.path
            elif changelog_update_mode == self.UPDATE_CHANGELOG_IN_BUILD:
                debian_parent_dir = self._target_dir
            else:
                raise ValueError("Expected changelog_update_mode to be "
                        "one of: %s" % ", ".join([
                            self.UPDATE_CHANGELOG_IN_SOURCES,
                            self.UPDATE_CHANGELOG_IN_BUILD]))
            changelog = os.path.join(debian_parent_dir, 'debian', 'changelog')
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
        - *build_dir*: the work directory containing the packages and the .changes
          files
    """
    def __init__(self, host, build_dir):
        self.host = host
        self.build_dir = build_dir

    def deploy(self):
        """
        Uploads the packages to the remote repository.
        """
        logging.info("Deploying to %s" % self.host)
        logging.debug("Currently in directory %s" % self.build_dir)
        command = "dput '%s' *.changes" % self.host
        subp.check_call(command, cwd=self.build_dir,
                stdout=sys.stdout, stderr=sys.stderr, shell=True)

class PackageBuilder(object):
    """
    This object is responsible for chaining the packaging actions, from updating
    the pom file to copying to a remote repository.

    The constructor accepts the following parameters:
        - *scm_manager*: an instance of :class:`SCMManager`
        - *pom_updater*: an instance of :class:`PomUpdater`, may be **None**
        - *packagers*: a list of :class:`Packager` objects
        - *deployer*: an instance of :class:`PackageDeployer`, may be **None**
    """
    def __init__(self, scm_manager, pom_updater, packagers, deployer):
        self.scm_manager = scm_manager
        self.pom_updater = pom_updater
        self.packagers = packagers
        self.deployer = deployer

    def build(self):
        """
        This method will trigger the removal of the -SNAPSHOT from the pom.xml
        files, build the packages, and (only if the *scm_manager* field is not
        **None**) deploy to a remote repository and commit the changes in the
        pom.xml files.

        If the version being built has already been tagged, no pom.xml update,
        changelog update or commit will occur.
        """
        package_names_string = ", ".join([p.package.name for p in self.packagers])
        logging.info("Building the following packages: %s" % \
                package_names_string)

        self.scm_manager.checkout()

        is_rebuild = self.scm_manager.is_existing_revision
        if is_rebuild:
            logging.info("This version has already been commited, "
                    "no pom.xml update, changelog update or commit will "
                    "occur")

        if not is_rebuild and self.pom_updater:
            self.pom_updater.remove_snapshot()

        for packager in self.packagers:
            changelog_update_mode = None
            if is_rebuild:
                changelog_update_mode = Packager.NO_CHANGELOG_UPDATE
            else:
                if self.deployer:
                    changelog_update_mode = Packager.UPDATE_CHANGELOG_IN_SOURCES
                else:
                    changelog_update_mode = Packager.UPDATE_CHANGELOG_IN_BUILD

            packager.prepare_build(changelog_update_mode)
            packager.build()

        if self.deployer:
            self.deployer.deploy()
            if self.pom_updaters and not is_rebuild:
                self.scm_manager.commit()

class PackagingError(StandardError):
    """
    An error triggered during packaging.
    """

def read_packages(config, checkout_dir):
    """
    Parses the package names for the configuration file and returns a list of
    :class:`Package` objects. The *config* argument should be an instance of
    :class:`ConfigParser.RawConfigParser`. The *checkout_dir* argument is the
    directory where the files should be checkout out.
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
        package_path = os.path.join(checkout_dir, end_of_path)

        update_pom = None
        if config.has_option(package_section_name, 'update_pom'):
            update_pom = config.getboolean(package_section_name, 'update_pom')
        else:
            update_pom = False
        package = Package(stripped_package_name, package_path, update_pom)
        packages.append(package)
    return packages

def build_argument_parser(args):
    """
    Builds the argument parser. The *args* parameters should be a list of parameters to
    parse (eg, argv).
    """
    parser = argparse.ArgumentParser(description='Builds OBM')

    parser.add_argument('-o', '--oncommit', help='triggers an oncommit build',
            default=False, action='store_true', dest='on_commit')

    parser.add_argument('-c', '--config', help='build configuration file',
            default='build.cfg', dest='configuration_file')

    parser.add_argument('-V', '--releaseversion', help='release version',
            default=None, dest='release_version')

    parser.add_argument('-H', '--host', help='host to upload to',
            default='24/private')

    parser.add_argument('-r', '--repository', help='URL of the source '
            'repository, or path to a locally checkout repository',
            default=None)

    parser.add_argument('-b', '--branch', help='branch to checkout',
            default=None)

    package_types = ['deb', 'rpm']
    parser.add_argument('-p', '--packagetype', metavar='PACKAGETYPE',
            help="package type, may be one of: %s" % ", ".join(package_types),
            choices=package_types, default='deb', dest='package_type')

    parser.add_argument('work_dir', metavar='WORKDIR', help='directory where '
            'the packages will be built')

    goals = ['build', 'release']
    parser.add_argument('goal', metavar='GOAL', help="action to do, may be one of: %s" % \
            (", ".join(goals)), choices=goals)

    parser.add_argument('packages', metavar='PACKAGES', nargs='+',
            help="packages to build, depending on the configuration file")

    return parser

def make_package_builder(packages, checkout_dir,
        packages_dir, scm_repository, branch, args, config):
    """
    Generates the :class:`PackageBuilder` instance. The parameters are:
        - *packages*: a list of :class:`Packager` objects to build 
        - *checkout_dir*: the directory where the sources will be checkout out
        - *packages_dir*: the directory where the packages will be built
          template
        - *scm_repository*: the URL or path of the SCM repository
        - *branch*: the branch to checkout
        - *args*: an instance of :class:`argparse.Namespace`
        - *config*: an instance of :class:`ConfigParser.RawConfigParser`
    """

    scm_manager = SCMManager(scm_repository, branch, checkout_dir,
            args.release_version)

    deployer = None
    if args.goal == 'release':
        deployer = PackageDeployer(args.host, packages_dir)

    pom_packages = []
    if not args.on_commit:
        for package in packages:
            if package.update_pom:
                pom_packages.append(package)
    pom_updater = PomUpdater(pom_packages)


    changelog_updater = None
    template = None
    mode = None
    if args.on_commit:
        template = config.get('debian_templates',
            'autocommit_changelog')
        mode = ChangelogUpdater.REPLACE
    else:
        template = config.get('debian_templates',
            'release_changelog')
        mode = ChangelogUpdater.APPEND
    changelog_updater = ChangelogUpdater(scm_manager=scm_manager,
            changelog_template=template, mode=mode)

    packagers = [Packager(p, args.package_type, packages_dir, scm_manager,
        changelog_updater) for p in packages]
    package_builder = PackageBuilder(scm_manager, pom_updater, packagers,
        deployer)
    return package_builder

def read_config(config_file):
    """
    Reads the configuration file *config_file*, which should be a path to a file
    relative to the build script. Returns an instance of
    :class:`ConfigParser.RawConfigParser`.
    """
    config_filepath = os.path.join(os.path.abspath(os.path.dirname(__file__)),
            config_file)
    config = ConfigParser.RawConfigParser()
    with open(config_filepath) as config_fd:
        config.readfp(config_fd)
    return config

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

    scm_repository = args.repository if args.repository else config.get('scm', 'repository')
    branch = None
    checkout_dir = None
    if os.path.isdir(scm_repository):
        logging.info("The SCM repository is a local directory, no checkout "
                "will be performed")
        checkout_dir = scm_repository
        if args.branch is not None:
            raise ValueError("The --branch option should not be set when "
                    "the SCM repository is a local directory")
        else:
            branch = None
    else: 
        checkout_dir = os.path.join(args.work_dir, "sources")
        branch = args.branch if args.branch else config.get('scm', 'branch')

    packages_dir = os.path.join(args.work_dir, args.package_type)

    config = read_config(args.configuration_file)
    available_packages = read_packages(config, checkout_dir)

    package_names = set(args.packages)
    assert_package_option_is_correct(argument_parser.format_usage(),
            package_names, available_packages)

    if 'all' in package_names:
        packages = [p for p in available_packages]
    else:
        packages = [p for p in available_packages if p.name in package_names]
    # No partial release
    if len(packages) < len(available_packages) and args.goal == "release":
        raise PackagingError("In order to do a release, all packages must be "
                "included")

    package_builder = make_package_builder(packages, 
            checkout_dir, packages_dir, scm_repository,
            branch, args, config)
    package_builder.build()

if __name__ == "__main__":
    main()
