import datetime
import string

class ChangelogUpdater(object):
    """
    This object updates the Debian changelog in autocommit mode. Its
    constructor accepts the following parameters:
        - *scm_manager*: an instance of :class:`SCMManager`
        - *changelog_template*: the path to a Debian changelog template
        - *version*: the OBM version (ex: 2.4.0)
        - *release*: the OBM release (ex: rc10) 
        - *mode*: one of `'replace'` or `'append'`. The first overwrites the
          changelog with the instancied template, the second inserts the
          contents of the instancied template at the top of the changelog.
    
    """
    REPLACE='replace'
    APPEND='append'
    NO_UPDATE='no_update'


    def __init__(self, changelog_template, date, sha1, version, release, mode):
        self.changelog_template = changelog_template
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

        changelog_date = self.date.strftime("%a, %e %b %Y %H:%M:%S +0000")
        params = dict(package_name=package_name,
                version=self.version,
                release=self.release,
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
