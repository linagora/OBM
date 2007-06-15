--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obm_default_values_2.1.sql                                   //
--     - Desc : Insertion of Default values (database independant)           //
-- 2007-04-23 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default Information (table ObmInfo)
-------------------------------------------------------------------------------

-- Update DB version
DELETE FROM ObmInfo where obminfo_name='db_version';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('db_version', '2.1');
DELETE FROM ObmInfo where obminfo_name='update_state';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('update_state', '0');
DELETE FROM ObmInfo where obminfo_name='remote_access';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('remote_access', '0');
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('update_lock', '0');


-------------------------------------------------------------------------------
-- Default Admin 0 creation
-------------------------------------------------------------------------------
DELETE FROM UserObm;

-- Global ADMIN
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid) VALUES (0, 'admin0','admin','PLAIN','admin', 'Admin Lastname', 'Firstname', '1000', '512');

-- Domain 0 has update (new domain)
INSERT INTO DomainPropertyValue (domainpropertyvalue_domain_id, domainpropertyvalue_property_key, domainpropertyvalue_value) VALUES ('0', 'update_state','1');


-------------------------------------------------------------------------------
-- Default Domain properties
-------------------------------------------------------------------------------
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default,domainproperty_readonly) VALUES ('update_state','integer', 1,1);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_users','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_mailshares','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_resources','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('quota_mail','integer', 100);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('delegation','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address1','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address2','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('town','text', '');
