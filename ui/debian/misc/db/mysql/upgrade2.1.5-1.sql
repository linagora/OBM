UPDATE DomainPropertyValue SET domainpropertyvalue_property_key = 'max_users' WHERE  domainpropertyvalue_property_key = 'max_user';
UPDATE DomainProperty SET domainproperty_key = 'max_mailshares' WHERE  domainproperty_key = 'max_mailshare';

UPDATE DomainPropertyValue SET domainpropertyvalue_property_key = 'max_mailshares' WHERE  domainpropertyvalue_property_key = 'max_mailshare';
UPDATE DomainProperty SET domainproperty_key = 'max_resources' WHERE  domainproperty_key = 'max_resource';
UPDATE DomainPropertyValue SET domainpropertyvalue_property_key = 'max_resources' WHERE  domainpropertyvalue_property_key = 'max_resource';
UPDATE UserObmPref SET userobmpref_option = 'set_cal_first_hour', userobmpref_value='8' WHERE userobmpref_option = 'cal_first_hour';
UPDATE UserObmPref SET userobmpref_option = 'set_cal_last_hour', userobmpref_value='20' WHERE userobmpref_option = 'cal_last_hour';

