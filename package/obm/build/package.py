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


class SubPackage(object):
    def __init__(self, name, source_path):
        self.name = name
        self.source_path = source_path
