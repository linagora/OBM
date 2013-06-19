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
        - *osversion*: operating system version for various specifics
        - *build_dir*: the path to the work directory where the packages will be
          built
        - *changelog_updater*: an instance of :class:`ChangelogUpdater` (may be
          **None**)
    """

    def __init__(self, package, package_type, osversion, build_dir, changelog_updater,
            version, release, config, nocompile=False):
        self.package = package
        self.package_type = package_type
        self.osversion = osversion
        self.changelog_updater = changelog_updater
        self.version = version
        self.release = release
        self.build_dir = build_dir
        self.nocompile = nocompile
        self._target_dir = os.path.join(self.build_dir, package.name)
        self._config = config

    def prepare_build(self):
        """
        Updates the changelog and copies the packages to the build directory.
        """
        self.logs_dir = self.build_dir + os.sep + 'log' + os.sep
        self.logfile = self.logs_dir + os.sep + self.package.name + ".log"
        self.logger = logging.getLogger(self.package.name)
        hdlr = logging.FileHandler(self.logfile)
        hdlr.setFormatter(logging.Formatter('%(levelname)s:%(name)s:%(message)s'))
        hdlr.setLevel(logging.DEBUG)
        self.logger.addHandler(hdlr)
        self.logger.propagate = False;
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
        self.logger.info("Compressing sources to %s" % archive_path)
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
        self.logger.info("Copying package directory")
        self.logger.debug("Copying %s to %s" % (self.package.path, self._target_dir))
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
                raise PackagingError("No changelog for package (expected to "
                        "find one in %s)" % changelog)
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
        self.logger.info("Created %d RPM package(s)" % len(generated_rpms))
        for rpm in sorted(generated_rpms):
            self.logger.info("Created RPM package: %s" % rpm)

    def _list_debs(self):
        # Lists the Debian packages generated
        generated_debs = sorted(glob.glob("%s/*.deb" % self.build_dir))
        if not generated_debs:
            raise PackagingError("No Debian package generated!")
        self.logger.info("Created %d Debian package(s)" % len(generated_debs))
        for deb in generated_debs:
            self.logger.info("Created Debian package: %s" % deb)

    def _override_perl(self):
        if self.osversion == "el5":
            perl_version = "5.8"
        elif self.osversion == 'el6':
            perl_version = "5.10"
        else:
             raise PackagingError("Unknown OS version: %s" % \
                    self.osversion)
        # Override the Perl module destination directory
        perl_section_name = "perl_%s" % perl_version
        perl_module_compat = self._config.get(perl_section_name, 'perl_module_compat')
        perl_vendorlib = self._config.get(perl_section_name, 'perl_vendorlib')
        if perl_vendorlib is None or perl_module_compat is None:
            raise ValueError("Need perl_vendorlib and perl_module_compat!")
        command = "--define 'perl_module_compat " \
            "%s' " % perl_module_compat
        command += "--define 'perl_vendorlib " \
            "%s' " % perl_vendorlib
	return command

    def _override_python(self):
        if self.osversion == 'el5':
            pyver = "2.4"
        elif self.osversion == 'el6':
            pyver = "2.6"
        else:
            raise PackagingError("Unknown OS version : %s" % \
                    self.osversion)
        python_sitelib = "/usr/lib/python" + pyver + "/site-packages"
        command = "--define 'python_sitelib " \
            "%s' " % python_sitelib
        command += "--define 'pyver " \
            "%s' " %pyver
        return command

    def _override_platform(self):
        return self._override_python() + self._override_perl()

    def build(self):
        """
        Builds the package itself.
        """
        self.logger.info("Building package")
        command = None
        if self.package_type == "deb":
            command = 'debuild -e OBM_NOCOMPILE --no-tgz-check -us -uc -sa'
            os.putenv('OBM_NOCOMPILE', "%d" % self.nocompile)
        elif self.package_type == "rpm":
            distname, version, id = platform.linux_distribution()
            topdir = os.path.abspath(os.path.join(self._target_dir, 'rpm'))
            target_dir = os.path.abspath(self._target_dir)
            command = "rpmbuild -ba --nodeps --define '_topdir %s' " \
                "--define '_rpmdir %s' --define '_srcrpmdir %s' " \
                "--define 'obm_version %s' --define 'obm_release %s' " % \
                (topdir, target_dir, target_dir, self.version, self.release)
            command += "--define 'obm_nocompile %d' " % self.nocompile
            command += "--define 'obm_osversion %s' " % self.osversion
            # If we're not building on RedHat/CentOS, we need to make sure we're
            # not going to take system directories from the current platform
            if distname != 'redhat':
                command += self._override_platform()
            command += "rpm/SPECS/*.spec"
        else:
            raise PackagingError("Unknown packaging type: %s" % \
                    self.package_type)
        subp_stderrout = open(self.logfile, 'a')
        subp.check_call(command, cwd=self._target_dir, stdout=subp_stderrout,
                stderr=subp_stderrout, shell=True)
        if self.package_type == "rpm":
            self._move_rpms_to_target_dir()
        elif self.package_type == "deb":
            self._list_debs()
