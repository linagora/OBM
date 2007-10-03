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
DELETE FROM ObmInfo where obminfo_name='remote_access';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('remote_access', '0');
DELETE FROM ObmInfo where obminfo_name='update_lock';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('update_lock', '0');


-------------------------------------------------------------------------------
-- Default Admin 0 creation
-------------------------------------------------------------------------------
DELETE FROM UserObm;

-- Global ADMIN
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid) VALUES (0, 'admin0','admin','PLAIN','admin', 'Admin Lastname', 'Firstname', '1000', '512');


-------------------------------------------------------------------------------
-- Default Domain properties
-------------------------------------------------------------------------------
DELETE FROM DomainProperty;
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('update_state','integer', 1);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_user','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_mailshare','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_resource','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('mail_quota','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('delegation','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address1','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address2','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address3','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('postcode','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('town','text', '');

-- Fill the initial update_state for each Domain
INSERT INTO DomainPropertyValue (domainpropertyvalue_domain_id, domainpropertyvalue_property_key, domainpropertyvalue_value) VALUES (0, 'update_state', 1);

