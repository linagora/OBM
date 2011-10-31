import logging
import os
import platform
import re
import shutil
import subprocess as subp
import sys
import tarfile

from packaging_error import PackagingError

class Packager(object):
    """
    Builds the Debian package. The constructor accepts the following parameters:
        - *package*: the :class:`Package` to build
        - *package_type*: should be either `deb` or `rpm`
        - *build_dir*: the path to the work directory where the packages will be
          built
        - *changelog_updater*: an instance of :class:`ChangelogUpdater` (may be
          **None**)
    """

    def __init__(self, package, package_type, build_dir, changelog_updater,
            version, release):
        self.package = package
        self.package_type = package_type
        self.changelog_updater = changelog_updater
        self.version = version
        self.release = release
        self.build_dir = build_dir
        self._target_dir = os.path.join(self.build_dir, package.name)

    def prepare_build(self):
        """
        Updates the changelog and copies the packages to the build directory.
        """
        if self.package_type == 'deb':
            self._copy()
            self._update_changelog()
        elif self.package_type == 'rpm':
            self._copy()
            self._tar_all_sources()
        else:
            raise PackagingError("Unknown packaging type: %s" % \
                    self.package_type)

    def _tar_all_sources(self):
        """
        Tars the sources for the RPM.
        """
        spec_path = os.path.join(self._target_dir, 'rpm', 'SPECS',
                "%s.spec" % self.package.name)
        if self.package.sub_packages:
            for sub_package in self.package.sub_packages:
                self._tar_sources(sub_package.name, spec_path,
                        sub_package.source_path)
        else:
            self._tar_sources(self.package.name, spec_path, self.package.path)

    def _tar_sources(self, name, spec_path, source_path):
        root = "%s-%s" % (name, self.version)
        archive_name = "%s.tar.gz" % root
        archive_path = os.path.join(self._target_dir, "rpm", "SOURCES", archive_name)
        source_file_or_dirs = os.listdir(source_path)
        logging.info("Compressing sources to %s" % archive_path)
        with tarfile.open(archive_path, mode='w:gz') as gzfd:
            for source_file_or_dir in source_file_or_dirs:
                path = os.path.join(source_path, source_file_or_dir)
                gzfd.add(path, os.path.join(root, source_file_or_dir))

    def _copy(self):
        """
        Copies the packages to the work directory.
        """
        logging.info("Copying package %s" % self.package.name)
        logging.debug("Package %s: copying %s to %s" % (self.package.name,
            self.package.path, self._target_dir))
        shutil.copytree(self.package.path, self._target_dir)

    def _update_changelog(self):
        """
        Triggers the overwrite of the changelog. The value of
        *changelog_update_mode* determines whether the changelog updated is the
        changelog of the sources (if set to `'update_changelog_in_sources'`) or
        in the build directory (`'update_changelog_in_build'`).
        """
        if self.changelog_updater:
            debian_parent_dir = self._target_dir
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
        elif self.package_type == "rpm":
            distname, version, id = platform.linux_distribution() 
            # If we're not building on RedHat/CentOS, we need to make sure we're
            # not going to take system directories from the current platform
            redefine_platform_params = distname != 'redhat'
            command = "rpmbuild -ba --nodeps --define '_topdir %s' " \
                "--define '_rpmdir %s' --define '_srcrpmdir %s' " \
                "--define 'obm_version %s' --define 'obm_release %s' " % \
                (os.path.join(self._target_dir, 'rpm'), self._target_dir,
                        self._target_dir, self.version, self.release)
            if redefine_platform_params:
                # Override the Perl module destination directory
                command += "--define 'perl_vendorlib " \
                    "/usr/lib/perl5/vendor_perl/5.8.8' "
            command += "rpm/SPECS/*.spec"
        else:
            raise PackagingError("Unknown packaging type: %s" % \
                    self.package_type)
        subp.check_call(command, cwd=self._target_dir, stdout=sys.stdout,
                stderr=sys.stderr, shell=True)
