import glob
import logging
import os
import platform
import re
import shutil
import subprocess as subp
import sys
import tarfile

from packaging_error import PackagingError
from package import get_changelog

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
            version, release, perl_module_compat=None, perl_vendorlib=None, nocompile=False):
        self.package = package
        self.package_type = package_type
        self.changelog_updater = changelog_updater
        self.version = version
        self.release = release
        self.perl_module_compat = perl_module_compat
        self.perl_vendorlib = perl_vendorlib
        self.build_dir = build_dir
        self._target_dir = os.path.join(self.build_dir, package.name)
        self.nocompile = nocompile

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
            self._update_changelog()
        else:
            raise PackagingError("Unknown packaging type: %s" % \
                    self.package_type)

    def _get_spec_path(self):
        spec_path = os.path.join(self._target_dir, 'rpm', 'SPECS',
                "%s.spec" % self.package.name)
        return spec_path

    def _tar_all_sources(self):
        """
        Tars the sources for the RPM.
        """
        spec_path = self._get_spec_path()
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
        gzfd = tarfile.open(archive_path, mode='w:gz')
        try:
            for source_file_or_dir in source_file_or_dirs:
                path = os.path.join(source_path, source_file_or_dir)
                gzfd.add(path, os.path.join(root, source_file_or_dir))
        finally:
            gzfd.close()

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
            changelog = get_changelog(self.package.name, self.package_type, self._target_dir)
            if not os.path.exists(changelog):
                raise PackagingError("No changelog for package %s (expected to "
                        "find one in %s)" % (self.package.name, changelog))
            self.changelog_updater.update(self.package.name, changelog)

    def _move_rpms_to_target_dir(self):
        # rpmbuild builds the RPMs in subdirectories, move them back to
        # the build directory
        rpms_to_move = {}
        for root, dirs, files in os.walk(self._target_dir):
            for f in files:
                if f.endswith(".rpm"):
                    src_path = os.path.join(root, f)
                    target_path = os.path.join(self.build_dir, f)
                    rpms_to_move[src_path] = target_path
        generated_rpms = []
        for src_path, target_path in rpms_to_move.items():
            if target_path != src_path:
                os.rename(src_path, target_path)
            generated_rpms.append(target_path)
        if not generated_rpms:
            raise PackagingError("No RPM package generated!")
        logging.info("Created %d RPM package(s)" % len(generated_rpms))
        for rpm in sorted(generated_rpms):
            logging.info("Created RPM package: %s" % rpm)

    def _list_debs(self):
        # Lists the Debian packages generated
        generated_debs = sorted(glob.glob("%s/*.deb" % self.build_dir))
        if not generated_debs:
            raise PackagingError("No Debian package generated!")
        logging.info("Created %d Debian package(s)" % len(generated_debs))
        for deb in generated_debs:
            logging.info("Created Debian package: %s" % deb)

    def build(self):
        """
        Builds the package itself.
        """
        logging.info("Building package %s" % self.package.name)
        command = None
        if self.package_type == "deb":
            command = 'debuild -e OBM_NOCOMPILE --no-tgz-check -us -uc -sa'
            os.putenv('OBM_NOCOMPILE', "%d" % self.nocompile)
        elif self.package_type == "rpm":
            distname, version, id = platform.linux_distribution()
            # If we're not building on RedHat/CentOS, we need to make sure we're
            # not going to take system directories from the current platform
            redefine_platform_params = distname != 'redhat'
            topdir = os.path.abspath(os.path.join(self._target_dir, 'rpm'))
            target_dir = os.path.abspath(self._target_dir)
            command = "rpmbuild -ba --nodeps --define '_topdir %s' " \
                "--define '_rpmdir %s' --define '_srcrpmdir %s' " \
                "--define 'obm_version %s' --define 'obm_release %s' " % \
                (topdir, target_dir, target_dir, self.version, self.release)
            command += "--define 'obm_nocompile %d' " % self.nocompile
            if redefine_platform_params:
                # Override the Perl module destination directory
                if self.perl_vendorlib is None or self.perl_module_compat is None:
                    raise ValueError("Need perl_vendorlib and perl_module_compat!")
                command += "--define 'perl_module_compat " \
                    "%s' " % self.perl_module_compat
                command += "--define 'perl_vendorlib " \
                    "%s' " % self.perl_vendorlib
            command += "rpm/SPECS/*.spec"
        else:
            raise PackagingError("Unknown packaging type: %s" % \
                    self.package_type)
        subp.check_call(command, cwd=self._target_dir, stdout=sys.stdout,
                stderr=sys.stderr, shell=True)
        if self.package_type == "rpm":
            self._move_rpms_to_target_dir()
        elif self.package_type == "deb":
            self._list_debs()
