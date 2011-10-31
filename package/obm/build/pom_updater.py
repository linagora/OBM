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

