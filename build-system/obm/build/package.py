import os.path

class Package(object):
    """
    Represents a Debian or RPM package. The *name* parameter is the name of the
    package, the *path* is its path relative to the build script, and
    *update_pom* should be set to **True** if the package contains one or more
    `pom.xml` files which should be updated. The *sub_packages* argument is a
    list of :class:`SubPackage` objects. It is only useful for RPM at the
    moment, which have different spec files.
    """
    def __init__(self, name, path, sub_packages=[], update_pom=False):
        self.name = name
        self.path = path
        self.sub_packages = sub_packages
        self.update_pom = update_pom

def get_changelog(package_name, package_type, path):
    """
    Returns the path of the changelog of the package, depending on the
    *package_type* argument. It may not exist if the package hasn't been
    package for this packaging type.
    """
    changelog = None
    if package_type == "deb":
        changelog = os.path.join(path, "debian", "changelog")
    elif package_type == "rpm":
        changelog = os.path.join(path, "rpm", "SPECS",
                "%s.spec" % package_name)
    else:
        raise ValueError("Unknown package type %s" % package_type)
    return changelog

class SubPackage(object):
    def __init__(self, name, source_path):
        self.name = name
        self.source_path = source_path
