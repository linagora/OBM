# LDAP

## About LDAP in Opush
In Opush, LDAP is only used for some kind of user lookup depending of the client request.

### When is LDAP used by Opush?
It's often used for recipient auto-completion when the user writes an email.

### When is LDAP NOT used by Opush?
LDAP is not used by Opush to populate the client address books. To achieve this, Opush will ask obm-sync to retrieve the user's contacts.

## Configuration file
The Opush LDAP configuration file is located at _/etc/opush/ldap_conf.ini_

### Fields
Every field is required and cannot be empty.

* **search.ldap.filter** : the filter expression to use for the search
* **search.ldap.url** : the address where the ldap server can be reached (we advise to specify the protocol used, _ldap://_ or _ldaps://_)
* **search.ldap.basedn** : the name of the context or object to search for

### Sample

	search.ldap.filter=(&(objectClass=inetOrgPerson) (|(mail=%q*)(sn=%q*)(givenName=%q*)))
	search.ldap.url=ldap://127.0.0.1
	search.ldap.basedn=dc=%d,dc=local

### Log
You can check your LDAP configuration by looking at logs when Opush starts.
You only have to activate the _CONFIGURATION_ logger, for details check the Logs documentation section.
