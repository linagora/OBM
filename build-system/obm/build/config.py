import os.path
import ConfigParser
from package import Package

def read_config(config_fd):
    """
    Reads the configuration file *config_fd*, which should be a file object.
    Returns an instance of :class:`ConfigParser.RawConfigParser`.
    """
    config = ConfigParser.RawConfigParser()
    with config_fd:
        config.readfp(config_fd)
    return config

def read_packages(config, checkout_dir):
    """
    Parses the package names for the configuration file and returns a list of
    :class:`Package` objects. The *config* argument should be an instance of
    :class:`ConfigParser.RawConfigParser`. The *checkout_dir* argument is the
    directory where the files should be checkout out.
    """
    package_names = config.get('global', 'packages')
    packages = []
    for package_name in set(package_names.split(",")):
        stripped_package_name = package_name.strip()
        package_section_name = "package:%s" % stripped_package_name
        package_path = None
        end_of_path = None
        if config.has_option(package_section_name, 'path'):
            end_of_path = config.get(package_section_name, 'path')
        else:
            end_of_path = stripped_package_name

        package_path = os.path.join(checkout_dir, end_of_path)

        sub_packages = []
        if config.has_option(package_section_name, 'sub-packages'):
            sub_package_names = config.get(package_section_name, 'sub-packages')
            for sub_package_name in set(sub_package_names.split(",")):
                stripped_sub_package_name = sub_package_name.strip()
                sub_package_section_name = "sub-package:%s" %\
                        stripped_sub_package_name
                if config.has_option(sub_package_section_name, 'source_path'):
                    end_of_sub_package_path = config.get(sub_package_section_name,
                            'source_path')
                else:
                    end_of_sub_package_path = stripped_sub_package_name
                sub_package_path = os.path.join(package_path,
                        end_of_sub_package_path)
                sub_package = ob.SubPackage(stripped_sub_package_name,
                        sub_package_path)
                sub_packages.append(sub_package)

        package = Package(stripped_package_name, package_path, sub_packages)
        packages.append(package)
    return packages
