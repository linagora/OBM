ObmSatellite
------------

This service is needed by OBM to do operation on distant server.

It's REST compatible, and use PLAIN or XML (depend of module) over HTTPs.

ObmSatellite configuration file :
  - /etc/obm/obm_conf.ini : needed
  - /etc/obm-satellite/obmSatellite.ini : optional, override obm_conf.ini common
    values

To get information on module, see perldoc ObmSatellite::Modules::<moduleName>


Modules:

Available modules are listed in /etc/obm-satellite/mods-available directory.
Enabled modules are listed in /etc/obm-satellite/mods-enabled directory.

use osenmod and osdismod to enable or disable module :
 - enable module locator :  osenmod locator
 - disbale module locator :  osdismod locator

Module configuration is done in /etc/obm-satellite/mods-available corresponding
file.

Some of them doesn't need any configuration options,
/etc/obm-satellite/mods-available corresponding file must exist anyway to
enabling it.

A module can use services.


Services:

SQL: get database informations from '/etc/obm/obm_conf.ini'

LDAP: get LDAP informations from '/etc/obm/obm_conf.ini' (automate section)
overrided by '/etc/obm-satellite/obmSatellite.ini' values if defined.
