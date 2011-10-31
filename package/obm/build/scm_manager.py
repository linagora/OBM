import os
import subprocess as subp

class SCMManager(object):
    """
    The SCM manager is responsible for interacting with the version control
    system (only Git is supported at present). Upon calling **init()**, it
    will retrieve the list of tags of the repository (which will be accessible
    via the *tags* field).
    """

    def __init__(self, checkout_dir):
        self.checkout_dir = checkout_dir
        self.is_existing_revision = None
        self.tags = None
        self.sha1 = None
        self._git_env = {'GIT_SSL_NO_VERIFY': '0'}
        if 'HOME' in os.environ:
            self._git_env['HOME'] = os.environ['HOME']

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

    def init(self):
        """
        Retrieves the tags and sha1.
        """
        self.tags = self._get_tags()
        self.sha1 = self._get_sha1()

    def _get_tags(self):
        proc = subp.Popen(["git", "tag"],
		cwd=self.checkout_dir, stdout=subp.PIPE)
        stdout, stderr = proc.communicate()
        if proc.returncode != 0:
            raise PackagingError("The git tag process failed with return "
                    "code %d" % proc.returncode)
        tags = [line for line in stdout.split("\n") if line]
        return tags

    def _get_sha1(self):
        proc = subp.Popen(["git", "rev-parse", "HEAD"],
                cwd=self.checkout_dir, stdout=subp.PIPE)
        stdout, stderr = proc.communicate()
        if proc.returncode != 0:
            raise PackagingError("The git rev-parse process failed with return "
                    "code %d" % proc.returncode)
        sha1 = stdout.split("\n")[0]
        return sha1
