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
  domainpropertyvalue_domain_id    integer NOT NULL,
  domainpropertyvalue_property_key varchar(255) NOT NULL,
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
ALTER TABLE Lead ADD COLUMN lead_contact_id integer;
ALTER TABLE Lead ALTER COLUMN lead_contact_id SET DEFAULT 0;


-------------------------------------------------------------------------------
-- Update Invoice table
-------------------------------------------------------------------------------
-- Add credit memo flag
ALTER TABLE Invoice ADD COLUMN invoice_credit_memo integer;
ALTER TABLE Invoice ALTER COLUMN invoice_credit_memo SET DEFAULT 0;
UPDATE Invoice set invoice_credit_memo = 0;
ALTER TABLE Invoice ALTER COLUMN invoice_credit_memo SET NOT NULL;


-------------------------------------------------------------------------------
-- Update CalendarEvent table
-------------------------------------------------------------------------------
-- Add ext_id column
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_ext_id varchar(32);
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_ext_id DEFAULT '';
-- Add extension column
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_properties text;
-- Add color column
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_color char(6);


--
-- Table structure for table 'CalendarAlert'
--
CREATE TABLE CalendarAlert (
  calendaralert_timeupdate  timestamp,
  calendaralert_timecreate  timestamp,
  calendaralert_userupdate  integer default NULL,
  calendaralert_usercreate  integer default NULL,
  calendaralert_event_id    integer,
  calendaralert_user_id     integer,
  calendaralert_duration    integer NOT NULL default 0
);
CREATE INDEX idx_calendaralert_user ON CalendarAlert (calendaralert_user_id);


-------------------------------------------------------------------------------
-- Update UserObm table
-------------------------------------------------------------------------------
-- Add hidden field
ALTER TABLE UserObm ADD COLUMN userobm_hidden integer;
ALTER TABLE UserObm ALTER COLUMN userobm_hidden SET DEFAULT 0;
-- Add kind field
ALTER TABLE UserObm ADD COLUMN userobm_kind varchar(12);
-- Add mail_quota_use, last login date
ALTER TABLE UserObm ADD COLUMN userobm_mail_quota_use integer;
ALTER TABLE UserObm ALTER COLUMN userobm_mail_quota_use SET DEFAULT 0;
ALTER TABLE UserObm ADD COLUMN userobm_mail_login_date timestamp;
ALTER TABLE UserObm ADD COLUMN userobm_photo_id integer;
-- Add company field
ALTER TABLE UserObm ADD COLUMN userobm_company varchar(64);
ALTER TABLE UserObm ADD COLUMN userobm_direction varchar(64);
-- Add vacation_date field
ALTER TABLE UserObm ADD COLUMN userobm_vacation_date timestamp;

-------------------------------------------------------------------------------
-- Update Resource table
-------------------------------------------------------------------------------
-- Add ResourceType link
ALTER TABLE Resource ADD COLUMN resource_rtype_id integer AFTER resource_domain_id;

-------------------------------------------------------------------------------
-- Add delegation fields
-------------------------------------------------------------------------------
-- UserObm (delegation + delegation target)
ALTER TABLE UserObm ADD COLUMN userobm_delegation_target varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_delegation_target SET DEFAULT '';
ALTER TABLE P_UserObm ADD COLUMN userobm_delegation_target varchar(64);
ALTER TABLE P_UserObm ALTER COLUMN userobm_delegation_target SET DEFAULT '';
ALTER TABLE UserObm ADD COLUMN userobm_delegation varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_delegation SET DEFAULT '';
ALTER TABLE P_UserObm ADD COLUMN userobm_delegation varchar(64);
ALTER TABLE P_UserObm ALTER COLUMN userobm_delegation SET DEFAULT '';

-- UGroup
ALTER TABLE UGroup ADD COLUMN group_delegation varchar(64);
ALTER TABLE UGroup ALTER COLUMN group_delegation SET DEFAULT '';
ALTER TABLE UGroup ADD COLUMN group_manager_id integer SET DEFAULT 0;
ALTER TABLE P_UGroup ADD COLUMN group_delegation varchar(64);
ALTER TABLE P_UGroup ALTER COLUMN group_delegation SET DEFAULT '';
ALTER TABLE P_UGroup ADD COLUMN group_manager_id integer SET DEFAULT 0;

-- MailShare
ALTER TABLE MailShare ADD COLUMN mailshare_delegation varchar(64);
ALTER TABLE MailShare ALTER COLUMN mailshare_delegation SET DEFAULT '';
ALTER TABLE P_MailShare ADD COLUMN mailshare_delegation varchar(64);
ALTER TABLE P_MailShare ALTER COLUMN mailshare_delegation SET DEFAULT '';

-- MailShare
ALTER TABLE Host ADD COLUMN host_delegation varchar(64);
ALTER TABLE Host ALTER COLUMN host_delegation SET DEFAULT '';
ALTER TABLE P_Host ADD COLUMN host_delegation varchar(64);
ALTER TABLE P_Host ALTER COLUMN host_delegation SET DEFAULT '';


-------------------------------------------------------------------------------
-- Tables needed for Automate work
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Deleted'
--
CREATE TABLE Deleted (
  deleted_id         serial,
  deleted_domain_id  integer,
  deleted_user_id    integer,
  deleted_delegation varchar(64) DEFAULT '',
  updated_table      varchar(32),
  deleted_entity_id  integer,
  deleted_timestamp  timestamp,
  PRIMARY KEY (deleted_id)
);


--
-- Table structure for the table 'Updated'
--
CREATE TABLE Updated (
  updated_id         serial,
  updated_domain_id  integer,
  updated_user_id    integer,
  updated_delegation varchar(64) DEFAULT '',
  updated_table      varchar(32),
  updated_entity_id  integer,
  updated_type       char(1),
  PRIMARY KEY (updated_id)
);


--
-- Table structure for the table 'ResourceType'
--
CREATE TABLE ResourceType (
  resourcetype_id					  serial,
  resourcetype_domain_id	  integer DEFAULT 0,	
  resourcetype_label			  varchar(32) NOT NULL,
  resourcetype_property		  varchar(32),
  resourcetype_pkind				integer DEFAULT 0 NOT NULL,
  PRIMARY KEY (resourcetype_id)
);

--
-- Table structure for the table 'ResourceItem'
--
CREATE TABLE ResourceItem (
  resourceitem_id								serial,
  resourceitem_domain_id				integer DEFAULT 0,
  resourceitem_label						varchar(32) NOT NULL,
  resourceitem_resourcetype_id	integer NOT NULL,
  resourceitem_description			text,
  PRIMARY KEY (resourceitem_id)
);

--
-- Table structure for the table 'Updatedlinks'
--
CREATE TABLE Updatedlinks (
  updatedlinks_id         serial,
  updatedlinks_domain_id  integer,
  updatedlinks_user_id    integer,
  updatedlinks_delegation varchar(64),
  updatedlinks_table      varchar(32),
  updatedlinks_entity     varchar(32),
  updatedlinks_entity_id  integer,
  PRIMARY KEY (updatedlinks_id)
);


--
-- Table structure for the table 'OrganizationalChart'
--
CREATE TABLE OrganizationalChart (
  organizationalchart_id			      serial,
  organizationalchart_domain_id     integer default 0,
  organizationalchart_timeupdate    timestamp,
  organizationalchart_timecreate		timestamp,
  organizationalchart_userupdate    integer,
  organizationalchart_usercreate    integer,
  organizationalchart_name          varchar(32) not null,
  organizationalchart_description   varchar(64),
  organizationalchart_archive       integer not null default 0,
  PRIMARY KEY (organizationalchart_id)
);


--
-- Table structure for the table 'OGroup'
--
CREATE TABLE OGroup (
  ogroup_id			   serial,
  ogroup_domain_id                 integer default 0,
  ogroup_timeupdate	           timestamp,
  ogroup_timecreate	           timestamp,
  ogroup_userupdate                integer,
  ogroup_usercreate                integer,
  ogroup_organizationalchart_id    integer not null,
  ogroup_parent_id                 integer not null,
  ogroup_name                      varchar(32) not null,
  ogroup_level                     varchar(16),
  PRIMARY KEY (ogroup_id)
);


--
-- Table structure for the table 'OGroupEntity'
--
CREATE TABLE OGroupEntity (
  ogroupentity_id                  serial,
  ogroupentity_domain_id           integer default 0,
  ogroupentity_timeupdate          timestamp,
  ogroupentity_timecreate          timestamp,
  ogroupentity_userupdate          integer,
  ogroupentity_usercreate          integer,
  ogroupentity_ogroup_id           integer not null,
  ogroupentity_entity_id           integer not null,
  ogroupentity_entity              varchar(32) not null,
  PRIMARY KEY (ogroupentity_id)
);

--
-- Table structure for the table 'EntityRight'
--
ALTER TABLE EntityRight ADD COLUMN entityright_admin integer NOT NULL default 0;
--
-- UPDATE EntityRight DATA
--
UPDATE EntityRight SET entityright_write = entityright_admin;

--
-- UPDATE Prefs
--
UPDATE UserObmPref SET userobmpref_value = 'm/d/y' WHERE userobmpref_value = 'mdy' AND userobmpref_option = 'set_date_upd';
UPDATE UserObmPref SET userobmpref_value = 'd/m/y' WHERE userobmpref_value = 'dmy' AND userobmpref_option = 'set_date_upd';

--
-- UPDATE TimeTask DATA
--
ALTER TABLE TimeTask ALTER COLUMN timetask_length TYPE float;
ALTER TABLE ProjectUser ALTER COLUMN projectuser_projectedtime TYPE float;
ALTER TABLE ProjectUser ALTER COLUMN projectuser_missingtime TYPE float;

--
-- UPDATE CalendarCategory Structure
--
ALTER TABLE CalendarCategory1 ADD COLUMN calendarcategory1_color char(6);


--
-- UPDATE Deal Structure
--
ALTER TABLE Deal ADD COLUMN deal_margin decimal(12,2);


--
-- Add credit card payment kind
--
INSERT INTO PaymentKind (paymentkind_domain_id, paymentkind_shortlabel, paymentkind_label) VALUES (1, 'CB', 'Carte de crédit');


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
--
-- Move Incident status id
--

UPDATE Incident set incident_status_id = 0 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Open' OR incidentstatus_label='Ouvert');
UPDATE Incident set incident_status_id = 1 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Call' OR incidentstatus_label='Appel');
UPDATE Incident set incident_status_id = 2 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Wait for call' OR incidentstatus_label='Attente Appel');
UPDATE Incident set incident_status_id = 3 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Paused' OR incidentstatus_label='En Pause');
UPDATE Incident set incident_status_id = 4 WHERE incident_status_id IN 
(SELECT incidentstatus_id from IncidentStatus where incidentstatus_label='Closed' OR incidentstatus_label='Cloturé');
