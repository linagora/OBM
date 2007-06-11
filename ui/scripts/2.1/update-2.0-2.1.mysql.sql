-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 2.0 to 2.1                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='2.1' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Add domain property tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'DomainProperty'
--
CREATE TABLE DomainProperty (
  domainproperty_key       varchar(255) NOT NULL,
  domainproperty_type      varchar(32),
  PRIMARY KEY (domainproperty_key)
);

--
-- Table structure for table 'DomainPropertyValue'
--
CREATE TABLE DomainPropertyValue (
  domainpropertyvalue_domain_id    int(8) NOT NULL,
  domainpropertyvalue_property_key varchar(255)  NOT NULL,
  domainpropertyvalue_value        varchar(255) NOT NULL,
  PRIMARY KEY (domainpropertyvalue_domain_id, domainpropertyvalue_property_key)
);


-------------------------------------------------------------------------------
-- Default Domain properties
-------------------------------------------------------------------------------
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('update_state','integer');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('max_users','integer');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('max_mailshares','integer');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('max_resources','integer');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('quota_mail','integer');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('delegation','text');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('address1','text');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('address2','text');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type) VALUES ('town','text');


-------------------------------------------------------------------------------
-- Update DisplayPref table
-------------------------------------------------------------------------------
-- Add Resource Type
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resourcetype_label', 4, 1);

-- Add OrganizationalChart pref
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'organizationalchart', 'organizationalchart_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'organizationalchart', 'organizationalchart_description', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'organizationalchart', 'organizationalchart_archive', 3, 2);

-------------------------------------------------------------------------------
-- Update Lead table
-------------------------------------------------------------------------------
-- Add contact link
ALTER TABLE Lead ADD COLUMN lead_contact_id int(8) NOT NULL DEFAULT 0 AFTER lead_company_id;


-------------------------------------------------------------------------------
-- Update Invoice table
-------------------------------------------------------------------------------
-- Add credit memo flag
ALTER TABLE Invoice ADD COLUMN invoice_credit_memo int(1) NOT NULL DEFAULT 0 AFTER invoice_inout;
UPDATE Invoice set invoice_credit_memo = 0;


-------------------------------------------------------------------------------
-- Update Incident table
-------------------------------------------------------------------------------
ALTER TABLE Incident CHANGE COLUMN incident_date incident_date timestamp(14); 


-------------------------------------------------------------------------------
-- Update Resource table
-------------------------------------------------------------------------------
-- Add ResourceType link
ALTER TABLE Resource ADD COLUMN resource_rtype_id int(8) AFTER resource_domain_id;

-------------------------------------------------------------------------------
-- Add delegation fields
-------------------------------------------------------------------------------
-- UserObm (delegation + delegation target)
ALTER TABLE UserObm ADD COLUMN userobm_delegation_target varchar(64) DEFAULT '' AFTER userobm_perms;
ALTER TABLE P_UserObm ADD COLUMN userobm_delegation_target varchar(64) DEFAULT '' AFTER userobm_perms;
ALTER TABLE UserObm ADD COLUMN userobm_delegation varchar(64) DEFAULT '' AFTER userobm_delegation_target;
ALTER TABLE P_UserObm ADD COLUMN userobm_delegation varchar(64) DEFAULT '' AFTER userobm_delegation_target;

-- UGroup
ALTER TABLE UGroup ADD COLUMN group_delegation varchar(64) DEFAULT '' AFTER group_mailing;
ALTER TABLE UGroup ADD COLUMN group_manager_id int(8) DEFAULT 0 AFTER group_delegation;
ALTER TABLE P_UGroup ADD COLUMN group_delegation varchar(64) DEFAULT '' AFTER group_mailing;
ALTER TABLE P_UGroup ADD COLUMN group_manager_id int(8) DEFAULT 0 AFTER group_delegation;

-- MailShare
ALTER TABLE MailShare ADD COLUMN mailshare_delegation varchar(64) DEFAULT '' AFTER mailshare_mail_server_id;
ALTER TABLE P_MailShare ADD COLUMN mailshare_delegation varchar(64) DEFAULT '' AFTER mailshare_mail_server_id;

-- Host
ALTER TABLE Host ADD COLUMN host_delegation varchar(64) DEFAULT '' AFTER host_ip;
ALTER TABLE P_Host ADD COLUMN host_delegation varchar(64) DEFAULT '' AFTER host_ip;


-------------------------------------------------------------------------------
-- Tables needed for Automate work
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Deleted'
--
CREATE TABLE Deleted (
  deleted_id         int(8) auto_increment,
  deleted_domain_id  int(8),
  deleted_user_id    int(8),
  deleted_delegation varchar(64) DEFAULT '',
  deleted_table      varchar(32),
  deleted_entity_id  int(8),
  deleted_timestamp  timestamp(14),
  PRIMARY KEY (deleted_id)
);


--
-- Table structure for the table 'Updated'
--
CREATE TABLE Updated (
  updated_id         int(8) auto_increment,
  updated_domain_id  int(8),
  updated_user_id    int(8),
  updated_delegation varchar(64) DEFAULT '',
  updated_table      varchar(32),
  updated_entity_id  int(8),
  updated_type       char(1),
  PRIMARY KEY (updated_id)
);

--
-- Table structure for the table 'ResourceType'
--
CREATE TABLE ResourceType (
  resourcetype_id         int(8) auto_increment,
  resourcetype_domain_id  int(8) DEFAULT 0,	
  resourcetype_label      varchar(32) NOT NULL,
  resourcetype_property   varchar(32),
  resourcetype_pkind      int(1) DEFAULT 0 NOT NULL,
  PRIMARY KEY (resourcetype_id)
);


--
-- Table structure for the table 'ResourceItem'
--
CREATE TABLE ResourceItem (
  resourceitem_id               int(8) auto_increment,
  resourceitem_domain_id        int(8) DEFAULT 0,
  resourceitem_label            varchar(32) NOT NULL,
  resourceitem_resourcetype_id	int(8) NOT NULL,
  resourceitem_description      text,
  PRIMARY KEY (resourceitem_id)
);


--
--
-- Table structure for the table 'Updatedlinks'
--
CREATE TABLE Updatedlinks (
  updatedlinks_id         int(8) auto_increment,
  updatedlinks_domain_id  int(8),
  updatedlinks_user_id    int(8),
  updatedlinks_delegation varchar(64),
  updatedlinks_table      varchar(32),
  updatedlinks_entity     varchar(32),
  updatedlinks_entity_id  int(8),
  PRIMARY KEY (updatedlinks_id)
);


--
-- Table structure for the table 'OrganizationalChart'
--
CREATE TABLE OrganizationalChart (
  organizationalchart_id			      int(8) auto_increment,
  organizationalchart_domain_id     int(8) default 0,
  organizationalchart_timeupdate    timestamp(14),
  organizationalchart_timecreate		timestamp(14),
  organizationalchart_userupdate    int(8),
  organizationalchart_usercreate    int(8),
  organizationalchart_name          varchar(32) not null,
  organizationalchart_description   varchar(64),
  organizationalchart_archive       int(1) not null default 0,
  PRIMARY KEY (organizationalchart_id)
);


--
-- Table structure for the table 'OGroup'
--
CREATE TABLE OGroup (
  ogroup_id					               int(8) auto_increment,
  ogroup_domain_id                 int(8) default 0,
  ogroup_timeupdate	             	 timestamp(14),
  ogroup_timecreate	             	 timestamp(14),
  ogroup_userupdate                int(8),
  ogroup_usercreate                int(8),
  ogroup_organizationalchart_id    int(8) not null,
  ogroup_parent_id                 int(8) not null,
  ogroup_name                      varchar(32) not null,
  ogroup_level                     varchar(16),
  PRIMARY KEY (ogroup_id)
);


--
-- Table structure for the table 'OGroupEntity'
--
CREATE TABLE OGroupEntity (
  ogroupentity_id                  int(8) auto_increment,
  ogroupentity_domain_id           int(8) default 0,
  ogroupentity_timeupdate          timestamp(14),
  ogroupentity_timecreate          timestamp(14),
  ogroupentity_userupdate          int(8),
  ogroupentity_usercreate          int(8),
  ogroupentity_ogroup_id           int(8) not null,
  ogroupentity_entity_id           int(8) not null,
  ogroupentity_entity              varchar(32) not null,
  PRIMARY KEY (ogroupentity_id)
);


