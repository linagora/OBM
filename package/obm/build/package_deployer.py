import logging
import subprocess as subp
import sys

class PackageDeployer(object):
    """
    Deploys the Debian packages to a remote repository. The constructor accepts
    the following parameters:
        - *host*: the host to which the packages should be deployed (it should
          be a valid entry in **~/.dput.cf**)
        - *build_dir*: the work directory containing the packages and the .changes
          files
        - *dry_run*: if **True**, simulates a deployment
    """
    def __init__(self, host, build_dir, dry_run=False):
        self.host = host
        self.build_dir = build_dir
        self.dry_run = dry_run

    def deploy(self):
        """
        Uploads the packages to the remote repository.
        """
        logging.info("Deploying to %s" % self.host)
        logging.debug("Currently in directory %s" % self.build_dir)
        command = "dput '%s' *.changes" % self.host
        if not self.dry_run:
            subp.check_call(command, cwd=self.build_dir,
                    stdout=sys.stdout, stderr=sys.stderr, shell=True)
        else:
            print command
