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
  domainproperty_default   varchar(64),
  domainproperty_readonly  int(1) DEFAULT 0,
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
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default,domainproperty_readonly) VALUES ('update_state','integer', 1,1);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_users','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_mailshares','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('max_resources','integer', 0);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('quota_mail','integer', 100);
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('delegation','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address1','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('address2','text', '');
INSERT INTO DomainProperty (domainproperty_key, domainproperty_type, domainproperty_default) VALUES ('town','text', '');


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
-- Update CalendarEvent table
-------------------------------------------------------------------------------
-- Add ext_id column
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_ext_id varchar(32) DEFAULT '' AFTER calendarevent_usercreate;
-- Add extension column
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_properties text AFTER calendarevent_description;
-- Add color column
ALTER TABLE CalendarEvent ADD COLUMN  calendarevent_color varchar(7) AFTER calendarevent_endrepeat;

--
-- Table structure for table 'CalendarAlert'
--
CREATE TABLE CalendarAlert (
  calendaralert_timeupdate  timestamp(14),
  calendaralert_timecreate  timestamp(14),
  calendaralert_userupdate  int(8) default NULL,
  calendaralert_usercreate  int(8) default NULL,
  calendaralert_event_id    int(8),
  calendaralert_user_id     int(8),
  calendaralert_duration    int(8) NOT NULL default 0
);


-------------------------------------------------------------------------------
-- Update Incident table (fix : only in MySQL)
-------------------------------------------------------------------------------
ALTER TABLE Incident CHANGE COLUMN incident_date incident_date timestamp(14); 


-------------------------------------------------------------------------------
-- Update UserObm table
-------------------------------------------------------------------------------
-- Add nb login failed
ALTER TABLE UserObm ADD COLUMN userobm_nb_login_failed int(2) DEFAULT 0 AFTER userobm_login;
-- Add hidden field
ALTER TABLE UserObm ADD COLUMN userobm_hidden int(1) DEFAULT 0 AFTER userobm_datebegin;
-- Add kind field
ALTER TABLE UserObm ADD COLUMN userobm_kind varchar(12) AFTER userobm_hidden;
-- Add mail_quota_use, last login date
ALTER TABLE UserObm ADD COLUMN userobm_mail_quota_use int(8) DEFAULT 0 AFTER userobm_mail_quota;
ALTER TABLE UserObm ADD COLUMN userobm_mail_login_date timestamp(14) AFTER userobm_mail_quota_use;
ALTER TABLE UserObm ADD COLUMN userobm_photo_id int(8) AFTER userobm_education;
-- Add company, direction field
ALTER TABLE UserObm ADD COLUMN userobm_company varchar(64) AFTER userobm_sound;
ALTER TABLE UserObm ADD COLUMN userobm_direction varchar(64) AFTER userobm_company;
-- Add vacation_date, nomade fields
ALTER TABLE UserObm ADD COLUMN userobm_vacation_datebegin timestamp(14) AFTER userobm_vacation_enable;
ALTER TABLE UserObm ADD COLUMN userobm_vacation_dateend timestamp(14) AFTER userobm_vacation_datebegin;
ALTER TABLE UserObm ADD COLUMN userobm_nomade_datebegin timestamp(14) AFTER userobm_nomade_local_copy;
ALTER TABLE UserObm ADD COLUMN userobm_nomade_dateend timestamp(14) AFTER userobm_nomade_datebegin;
-- user expiration date fields
ALTER TABLE UserObm ADD COLUMN userobm_password_dateexp date AFTER userobm_password;
ALTER TABLE UserObm ADD COLUMN userobm_account_dateexp date AFTER userobm_password_dateexp;
-- Default value modification
ALTER TABLE UserObm MODIFY COLUMN userobm_web_perms int(1) DEFAULT 0;
ALTER TABLE UserObm MODIFY COLUMN userobm_mail_perms int(1) DEFAULT 0;
ALTER TABLE UserObm MODIFY COLUMN userobm_mail_ext_perms int(1) DEFAULT 0;

-- Production table
-- Add nb login failed
ALTER TABLE P_UserObm ADD COLUMN userobm_nb_login_failed int(2) DEFAULT 0 AFTER userobm_login;
-- Add hidden field
ALTER TABLE P_UserObm ADD COLUMN userobm_hidden int(1) DEFAULT 0 AFTER userobm_datebegin;
-- Add kind field
ALTER TABLE P_UserObm ADD COLUMN userobm_kind varchar(12) AFTER userobm_hidden;
-- Add mail_quota_use, last login date
ALTER TABLE P_UserObm ADD COLUMN userobm_mail_quota_use int(8) DEFAULT 0 AFTER userobm_mail_quota;
ALTER TABLE P_UserObm ADD COLUMN userobm_mail_login_date timestamp(14) AFTER userobm_mail_quota_use;
ALTER TABLE P_UserObm ADD COLUMN userobm_photo_id int(8) AFTER userobm_education;
-- Add company, direction field
ALTER TABLE P_UserObm ADD COLUMN userobm_company varchar(64) AFTER userobm_sound;
ALTER TABLE P_UserObm ADD COLUMN userobm_direction varchar(64) AFTER userobm_company;
-- Add vacation_date, nomade fields
ALTER TABLE P_UserObm ADD COLUMN userobm_vacation_datebegin timestamp(14) AFTER userobm_vacation_enable;
ALTER TABLE P_UserObm ADD COLUMN userobm_vacation_dateend timestamp(14) AFTER userobm_vacation_datebegin;
ALTER TABLE P_UserObm ADD COLUMN userobm_nomade_datebegin timestamp(14) AFTER userobm_nomade_local_copy;
ALTER TABLE P_UserObm ADD COLUMN userobm_nomade_dateend timestamp(14) AFTER userobm_nomade_datebegin;
-- user expiration date fields
ALTER TABLE P_UserObm ADD COLUMN userobm_password_dateexp date AFTER userobm_password;
ALTER TABLE P_UserObm ADD COLUMN userobm_account_dateexp date AFTER userobm_password_dateexp;
-- Default value modification
ALTER TABLE P_UserObm MODIFY COLUMN userobm_web_perms int(1) DEFAULT 0;
ALTER TABLE P_UserObm MODIFY COLUMN userobm_mail_perms int(1) DEFAULT 0;
ALTER TABLE P_UserObm MODIFY COLUMN userobm_mail_ext_perms int(1) DEFAULT 0;


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

--
-- Table structure for the table 'EntityRight'
--
ALTER TABLE EntityRight ADD COLUMN entityright_admin int(1) NOT NULL default 0 AFTER entityright_write;
ALTER TABLE P_EntityRight ADD COLUMN entityright_admin int(1) NOT NULL default 0 AFTER entityright_write;

--
-- UPDATE EntityRight DATA
--
UPDATE EntityRight SET entityright_admin = entityright_write;
UPDATE P_EntityRight SET entityright_admin = entityright_write;

--
-- UPDATE TimeTask Structure
--
ALTER TABLE TimeTask CHANGE COLUMN timetask_length timetask_length float;
ALTER TABLE ProjectUser CHANGE COLUMN projectuser_projectedtime projectuser_projectedtime float;
ALTER TABLE ProjectUser CHANGE COLUMN projectuser_missingtime projectuser_missingtime float;

--
-- UPDATE Prefs
--
UPDATE UserObmPref SET userobmpref_value = 'm/d/y' WHERE userobmpref_value = 'mdy' AND userobmpref_option = 'set_date_upd';
UPDATE UserObmPref SET userobmpref_value = 'd/m/y' WHERE userobmpref_value = 'dmy' AND userobmpref_option = 'set_date_upd';

--
-- UPDATE CalendarCategory Structure
--
ALTER TABLE CalendarCategory1 ADD COLUMN calendarcategory1_color char(6) AFTER calendarcategory1_label;


--
-- UPDATE Deal Structure
--
ALTER TABLE Deal ADD COLUMN deal_margin decimal(12,2) AFTER deal_amount;


--
-- Add credit card payment kind
--
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES (1, 'CB', 'Carte de crédit');


--
-- UPDATE Payment Structure
--
ALTER TABLE Payment ADD COLUMN payment_gap double(10,2) DEFAULT '0.0' NOT NULL AFTER payment_checked;


--
-- Move Invoice status id
--
UPDATE Invoice set invoice_status_id = 0 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='To create' OR invoicestatus_label='A créer');
UPDATE Invoice set invoice_status_id = 1 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Sent' OR invoicestatus_label='Envoyée');
UPDATE Invoice set invoice_status_id = 3 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Partially paid' OR invoicestatus_label='Payée partiellement');
UPDATE Invoice set invoice_status_id = 4 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Paid' OR invoicestatus_label='Payée');
UPDATE Invoice set invoice_status_id = 5 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Conflict' OR invoicestatus_label='Litige');
UPDATE Invoice set invoice_status_id = 6 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Cancelled' OR invoicestatus_label='Annulée');
UPDATE Invoice set invoice_status_id = 7 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Loss' OR invoicestatus_label='Pertes et profits');
UPDATE Invoice set invoice_status_id = 1 WHERE invoice_status_id IN 
(SELECT invoicestatus_id from InvoiceStatus where invoicestatus_label='Received' OR invoicestatus_label='Reçue');

DROP TABLE InvoiceStatus;
--
-- Move Incident status id
--
UPDATE Incident set incident_status_id = 1 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Open' OR incidentstatus_label='Ouvert');
UPDATE Incident set incident_status_id = 1 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Call' OR incidentstatus_label='Appel');
UPDATE Incident set incident_status_id = 2 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Wait for call' OR incidentstatus_label='Attente Appel');
UPDATE Incident set incident_status_id = 3 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Paused' OR incidentstatus_label='En Pause');
UPDATE Incident set incident_status_id = 5 WHERE incident_status_id IN
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Closed' OR incidentstatus_label='Cloturé');


--
-- UPDATE ProjectTask Structure
--
ALTER TABLE ProjectTask ADD COLUMN projecttask_datebegin date;
ALTER TABLE ProjectTask ADD COLUMN projecttask_dateend date;


--
-- Table structure for table 'ProjectClosing'
--
CREATE TABLE ProjectClosing (
  projectclosing_id           int(8) auto_increment,
  projectclosing_project_id   int(8) NOT NULL,
  projectclosing_timeupdate   timestamp(14),
  projectclosing_timecreate   timestamp(14),
  projectclosing_userupdate   int(8),
  projectclosing_usercreate   int(8) NOT NULL,
  projectclosing_date         timestamp(14) NOT NULL,
  projectclosing_used         int(8) NOT NULL,
  projectclosing_remaining    int(8) NOT NULL,
  projectclosing_type         int(8),
  PRIMARY KEY (projectclosing_id)
);


--
-- Drop deprecated ProjectStat table
--
DROP table ProjectStat;


--
-- UPDATE MailShare Structure
--
-- Add archive flag
ALTER TABLE MailShare ADD COLUMN mailshare_archive int(1) not null default 0 AFTER mailshare_name;
-- Production table
ALTER TABLE P_MailShare ADD COLUMN mailshare_archive int(1) not null default 0 AFTER mailshare_name;


--
-- UPDATE MailServer Structure to allow different mail servers roles
--
-- Add mailserver_type
ALTER TABLE MailServer ADD COLUMN mailserver_timeupdate timestamp(14) AFTER mailserver_id;
ALTER TABLE MailServer ADD COLUMN mailserver_timecreate timestamp(14) AFTER mailserver_timeupdate;
ALTER TABLE MailServer ADD COLUMN mailserver_userupdate int(8) AFTER mailserver_timecreate;
ALTER TABLE MailServer ADD COLUMN mailserver_usercreate int(8) AFTER mailserver_userupdate;
ALTER TABLE MailServer ADD COLUMN mailserver_imap int(1) default 0;
ALTER TABLE MailServer ADD COLUMN mailserver_smtp_in int(1) default 0;
ALTER TABLE MailServer ADD COLUMN mailserver_smtp_out int(1) default 0;

-- Production table
ALTER TABLE P_MailServer ADD COLUMN mailserver_timeupdate timestamp(14) AFTER mailserver_id;
ALTER TABLE P_MailServer ADD COLUMN mailserver_timecreate timestamp(14) AFTER mailserver_timeupdate;
ALTER TABLE P_MailServer ADD COLUMN mailserver_userupdate int(8) AFTER mailserver_timecreate;
ALTER TABLE P_MailServer ADD COLUMN mailserver_usercreate int(8) AFTER mailserver_userupdate;
ALTER TABLE P_MailServer ADD COLUMN mailserver_imap int(1) default 0;
ALTER TABLE P_MailServer ADD COLUMN mailserver_smtp_in int(1) default 0;
ALTER TABLE P_MailServer ADD COLUMN mailserver_smtp_out int(1) default 0;

-- With OBM < 2.1, the mail servers are 'imap' and 'smtp-in'
UPDATE MailServer SET mailserver_imap=1, mailserver_smtp_in=1;


--
-- Domain - Mail server link table
--
CREATE TABLE DomainMailServer (
  domainmailserver_domain_id      int(8) NOT NULL default 0,
  domainmailserver_mailserver_id  int(8) NOT NULL,
  domainmailserver_role           varchar(16) NOT NULL default 'imap'
);

-- With OBM < 2.1, the mail servers are 'imap' and 'smtp-in' for all domains
INSERT INTO DomainMailServer (domainmailserver_mailserver_id, domainmailserver_domain_id, domainmailserver_role) SELECT i.mailserver_id, j.domain_id, 'imap' FROM MailServer i, Domain j WHERE i.mailserver_imap=1;
INSERT INTO DomainMailServer (domainmailserver_mailserver_id, domainmailserver_domain_id, domainmailserver_role) SELECT i.mailserver_id, j.domain_id, 'smtp_in' FROM MailServer i, Domain j WHERE i.mailserver_smtp_in=1;


--
-- Table structure for table 'ObmBookmark'
--
CREATE TABLE ObmBookmark (
  obmbookmark_id          int(8) auto_increment,
  obmbookmark_user_id     int(8) NOT NULL,
  obmbookmark_label       varchar(48) NOT NULL default '',
  obmbookmark_entity      varchar(24) NOT NULL default '',
  PRIMARY KEY (obmbookmark_id),
  INDEX bkm_idx_user (obmbookmark_user_id)
);


--
-- Table structure for table 'ObmBookmarkProperty'
--
CREATE TABLE ObmBookmarkProperty (
  obmbookmarkproperty_id           int(8) auto_increment,
  obmbookmarkproperty_bookmark_id  int(8) NOT NULL,
  obmbookmarkproperty_property     varchar(64) NOT NULL default '',
  obmbookmarkproperty_value        varchar(64) NOT NULL default '',
  PRIMARY KEY (obmbookmarkproperty_id),
  INDEX bkmprop_idx_bkm (obmbookmarkproperty_bookmark_id)
);


--
-- Update User-Group handling
--
DROP TABLE IF EXISTS of_usergroup;
DROP TABLE IF EXISTS P_of_usergroup;
--
-- Table structure for table 'of_usergroup'
--
CREATE TABLE of_usergroup (
  of_usergroup_group_id    int(8) DEFAULT 0 NOT NULL,
  of_usergroup_user_id     int(8) DEFAULT 0 NOT NULL,
  PRIMARY KEY (of_usergroup_group_id, of_usergroup_user_id)
);
CREATE TABLE P_of_usergroup like of_usergroup;

DROP TABLE P_UserObmGroup;
DROP TABLE P_GroupGroup;
