import string

class ChangelogUpdater(object):
    """
    This object updates the Debian changelog or RPM spec file in autocommit mode. Its
    constructor accepts the following parameters:
        - *changelog_template*: the path to a Debian changelog template
        - *package_type*: one of `'deb'` or `'rpm'`
        - *date*: an instance of :class:`datetime.datetime`, set to the time at
          which we started building the packages
        - *sha1*: the SHA1 of the build
        - *version*: the OBM version (ex: 2.4.0)
        - *release*: the OBM release (ex: rc10)
        - *mode*: one of `'replace'`, `'append'` or `'no_update'`. The first
          overwrites the changelog with the instancied template, the second
          inserts the contents of the instancied template at the top of the
          changelog, while `'no_update'` turns :method:`update` into a no-op.
    """
    REPLACE='replace'
    APPEND='append'
    NO_UPDATE='no_update'


    def __init__(self, changelog_template, package_type, date, sha1, version,
            release, mode):
        self.changelog_template = changelog_template
        self.package_type = package_type
        self.date = date
        self.sha1 = sha1
        self.version = version
        self.release = release
        accepted_mode_values = [self.REPLACE, self.APPEND, self.NO_UPDATE]
        if mode not in accepted_mode_values:
            raise ValueError("The mode should be one of: "
                    "%s" % ", ".join(accepted_mode_values))
        self.mode = mode

    def update(self, package_name, changelog):
        """
        Overwrites *changelog*, injecting *package_name* in the template.
        """
        if self.mode == self.NO_UPDATE:
            return

        changelog_date = None
        if self.package_type == 'deb':
            changelog_date = self.date.strftime("%a, %e %b %Y %H:%M:%S +0000")
        elif self.package_type == 'rpm':
            changelog_date = self.date.strftime("%a %b %d %Y")
        else:
            raise ValueError("Unknown package type %s" % self.package_type)

        debian_version = None
        if self.release:
            # For autocommit without a version number, no tilde
            if self.release[0] == "+":
                debian_version = "%s%s" % (self.version, self.release)
            else:
                debian_version = "%s~%s" % (self.version, self.release)
        else:
            debian_version = self.version
        params = dict(package_name=package_name,
                version=self.version,
                release=self.release,
                debian_version=debian_version,
                year=self.date.strftime("%Y"),
                month=self.date.strftime("%m"),
                day=self.date.strftime("%d"),
                hour=self.date.strftime("%H"),
                minute=self.date.strftime("%M"),
                second=self.date.strftime("%S"),
                microsecond=self.date.strftime("%f"),
                sha1=self.sha1,
                changelog_date=changelog_date)
        template_content = None
        with open(self.changelog_template) as template_fd:
            template_content = template_fd.read()
        formatter = string.Formatter()
        changelog_content = formatter.format(template_content, **params)
        if self.package_type == 'deb':
            self._update_deb_changelog(changelog, changelog_content)
        elif self.package_type == 'rpm':
            self._update_rpm_spec(changelog, changelog_content)
        else:
            raise ValueError("Unknown package type %s" % self.package_type)

    def _update_deb_changelog(self, changelog, changelog_content):
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

    def _update_rpm_spec(self, spec, changelog_content):
        spec_content = None
        with open(spec, 'r') as spec_fd:
            spec_content = spec_fd.readlines()

        in_changelog = False
        seen_changelog = False
        with open(spec, 'w') as spec_fd:
            for i, line in enumerate(spec_content):
                if line.startswith("%changelog") and seen_changelog:
                    raise ValueError("Can't deal with more than one"
                            "changelog section at line %d in %s" %
                            (i + 1, spec))
                # We reached the end of the changelog section. Well, maybe
                if in_changelog and line.startswith("%"):
                    in_changelog = False
                # We discard the current line only when we are in replace mode
                # in the changelog section
                if not in_changelog or self.mode == self.APPEND:
                    if line.startswith("%changelog"):
                        in_changelog = True
                        seen_changelog = True
                        spec_fd.write(line)
                        spec_fd.write(changelog_content)
                    else:
                        spec_fd.write(line)
