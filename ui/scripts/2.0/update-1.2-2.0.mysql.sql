-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 1.2 to 2.0                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='2.0' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Add Domain info
-------------------------------------------------------------------------------
--
-- Table structure for table 'Domain'
--
CREATE TABLE Domain (
  domain_id             int(8) auto_increment,
  domain_timeupdate     timestamp(14),
  domain_timecreate     timestamp(14),
  domain_usercreate     int(8),
  domain_userupdate     int(8),
  domain_label          varchar(32) NOT NULL,
  domain_description    varchar(255),
  domain_domain_name    varchar(128),
  domain_alias          text,
  PRIMARY KEY (domain_id)
);

INSERT INTO Domain VALUES (1,NOW(),NOW(),0,0,'Main','Main domain','localdomain','');
--
-- Update All tables to include Domain info
--

ALTER TABLE UserObm ADD Column userobm_domain_id int(8) default 0 after userobm_id;
UPDATE UserObm SET userobm_domain_id = 1;

ALTER TABLE DataSource ADD Column datasource_domain_id int(8) default 0 after datasource_id;
UPDATE DataSource SET datasource_domain_id = 1;

ALTER TABLE Country ADD Column country_domain_id int(8) default 0 first;
UPDATE Country SET country_domain_id = 1;

ALTER TABLE Region ADD Column region_domain_id int(8) default 0 after region_id;
UPDATE Region SET region_domain_id = 1;

ALTER TABLE CompanyType ADD Column companytype_domain_id int(8) default 0 after companytype_id;
UPDATE CompanyType SET companytype_domain_id = 1;

ALTER TABLE CompanyActivity ADD Column companyactivity_domain_id int(8) default 0 after companyactivity_id;
UPDATE CompanyActivity SET companyactivity_domain_id = 1;

ALTER TABLE CompanyNafCode ADD Column companynafcode_domain_id int(8) default 0 after companynafcode_id;
UPDATE CompanyNafCode SET companynafcode_domain_id = 1;

ALTER TABLE Company ADD Column company_domain_id int(8) default 0 after company_id;
UPDATE Company SET company_domain_id = 1;

ALTER TABLE Contact ADD Column contact_domain_id int(8) default 0 after contact_id;
UPDATE Contact SET contact_domain_id = 1;

ALTER TABLE Kind ADD Column kind_domain_id int(8) default 0 after kind_id;
UPDATE Kind SET kind_domain_id = 1;

ALTER TABLE ContactFunction ADD Column contactfunction_domain_id int(8) default 0 after contactfunction_id;
UPDATE ContactFunction SET contactfunction_domain_id = 1;

ALTER TABLE LeadSource ADD Column leadsource_domain_id int(8) default 0 after leadsource_id;
UPDATE LeadSource SET leadsource_domain_id = 1;

ALTER TABLE Lead ADD Column lead_domain_id int(8) default 0 after lead_id;
UPDATE Lead SET lead_domain_id = 1;

ALTER TABLE ParentDeal ADD Column parentdeal_domain_id int(8) default 0 after parentdeal_id;
UPDATE ParentDeal SET parentdeal_domain_id = 1;

ALTER TABLE Deal ADD Column deal_domain_id int(8) default 0 after deal_id;
UPDATE Deal SET deal_domain_id = 1;

ALTER TABLE DealStatus ADD Column dealstatus_domain_id int(8) default 0 after dealstatus_id;
UPDATE DealStatus SET dealstatus_domain_id = 1;

ALTER TABLE DealType ADD Column dealtype_domain_id int(8) default 0 after dealtype_id;
UPDATE DealType SET dealtype_domain_id = 1;

ALTER TABLE DealCompanyRole ADD Column dealcompanyrole_domain_id int(8) default 0 after dealcompanyrole_id;
UPDATE DealCompanyRole SET dealcompanyrole_domain_id = 1;

ALTER TABLE List ADD Column list_domain_id int(8) default 0 after list_id;
UPDATE List SET list_domain_id = 1;

ALTER TABLE CalendarEvent ADD Column calendarevent_domain_id int(8) default 0 after calendarevent_id;
UPDATE CalendarEvent SET calendarevent_domain_id = 1;

ALTER TABLE CalendarCategory1 ADD Column calendarcategory1_domain_id int(8) default 0 after calendarcategory1_id;
UPDATE CalendarCategory1 SET calendarcategory1_domain_id = 1;

ALTER TABLE Todo ADD Column todo_domain_id int(8) default 0 after todo_id;
UPDATE Todo SET todo_domain_id = 1; 

ALTER TABLE Publication ADD Column publication_domain_id int(8) default 0 after publication_id;
UPDATE Publication SET publication_domain_id = 1;

ALTER TABLE PublicationType ADD Column publicationtype_domain_id int(8) default 0 after publicationtype_id;
UPDATE PublicationType SET publicationtype_domain_id = 1;

ALTER TABLE Subscription ADD Column subscription_domain_id int(8) default 0 after subscription_id;
UPDATE Subscription SET subscription_domain_id = 1;

ALTER TABLE SubscriptionReception ADD Column subscriptionreception_domain_id int(8) default 0 after subscriptionreception_id;
UPDATE SubscriptionReception SET subscriptionreception_domain_id = 1;

ALTER TABLE Document ADD Column document_domain_id int(8) default 0 after document_id;
UPDATE Document SET document_domain_id = 1;

ALTER TABLE DocumentMimeType ADD Column documentmimetype_domain_id int(8) default 0 after documentmimetype_id;
UPDATE DocumentMimeType SET documentmimetype_domain_id = 1;

ALTER TABLE Project ADD Column project_domain_id int(8) default 0 after project_id;
UPDATE Project SET project_domain_id = 1;

ALTER TABLE TaskType ADD Column tasktype_domain_id int(8) default 0 after tasktype_id;
UPDATE TaskType SET tasktype_domain_id = 1;

ALTER TABLE Contract ADD Column contract_domain_id int(8) default 0 after contract_id;
UPDATE Contract SET contract_domain_id = 1;

ALTER TABLE ContractType ADD Column contracttype_domain_id int(8) default 0 after contracttype_id;
UPDATE ContractType SET contracttype_domain_id = 1;

ALTER TABLE ContractPriority ADD Column contractpriority_domain_id int(8) default 0 after contractpriority_id;
UPDATE ContractPriority SET contractpriority_domain_id = 1;

ALTER TABLE ContractStatus ADD Column contractstatus_domain_id int(8) default 0 after contractstatus_id;
UPDATE ContractStatus SET contractstatus_domain_id = 1;

ALTER TABLE Incident ADD Column incident_domain_id int(8) default 0 after incident_id;
UPDATE Incident SET incident_domain_id = 1;

ALTER TABLE IncidentPriority ADD Column incidentpriority_domain_id int(8) default 0 after incidentpriority_id;
UPDATE IncidentPriority SET incidentpriority_domain_id = 1;

ALTER TABLE IncidentStatus ADD Column incidentstatus_domain_id int(8) default 0 after incidentstatus_id;
UPDATE IncidentStatus SET incidentstatus_domain_id = 1;

ALTER TABLE Invoice ADD Column invoice_domain_id int(8) default 0 after invoice_id;
UPDATE Invoice SET invoice_domain_id = 1;

ALTER TABLE InvoiceStatus ADD Column invoicestatus_domain_id int(8) default 0 after invoicestatus_id;
UPDATE InvoiceStatus SET invoicestatus_domain_id = 1;

ALTER TABLE Payment ADD Column payment_domain_id int(8) default 0 after payment_id;
UPDATE Payment SET payment_domain_id = 1;

ALTER TABLE PaymentKind ADD Column paymentkind_domain_id int(8) default 0 after paymentkind_id;
UPDATE PaymentKind SET paymentkind_domain_id = 1;

ALTER TABLE PaymentInvoice ADD Column paymentinvoice_domain_id int(8) default 0 first;
UPDATE PaymentKind SET paymentkind_domain_id = 1;

ALTER TABLE Account ADD Column account_domain_id int(8) default 0 after account_id;
UPDATE Account SET account_domain_id = 1;

ALTER TABLE UGroup ADD Column group_domain_id int(8) default 0 after group_id;
UPDATE UGroup SET group_domain_id = 1;

ALTER TABLE Import ADD Column import_domain_id int(8) default 0 after import_id;
UPDATE Import SET import_domain_id = 1;

ALTER TABLE Resource ADD Column resource_domain_id int(8) default 0 after resource_id;
UPDATE Resource SET resource_domain_id = 1;

ALTER TABLE RGroup ADD Column rgroup_domain_id int(8) default 0 after rgroup_id;
UPDATE RGroup SET rgroup_domain_id = 1;

-------------------------------------------------------------------------------
-- Global Category table
-------------------------------------------------------------------------------
--
-- Table structure for table 'Category'
--
CREATE TABLE Category (
  category_id          int(8) auto_increment,
  category_domain_id   int(8) NOT NULL default 0,
  category_timeupdate  timestamp(14),
  category_timecreate  timestamp(14),
  category_userupdate  int(8) NOT NULL default 0,
  category_usercreate  int(8) NOT NULL default 0,
  category_category    varchar(24) NOT NULL default '',
  category_code        varchar(10) NOT NULL default '',
  category_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (category_id),
  INDEX cat_idx_cat (category_category)
);


--
-- Table structure for table 'CategoryLink'
--
CREATE TABLE CategoryLink (
  categorylink_category_id int(8) NOT NULL default 0,
  categorylink_entity_id   int(8) NOT NULL default 0,
  categorylink_category    varchar(24) NOT NULL default '',
  categorylink_entity      varchar(32) NOT NULL default '',
  PRIMARY KEY (categorylink_category_id, categorylink_entity_id),
  INDEX catl_idx_ent (categorylink_entity_id),
  INDEX catl_idx_cat (categorylink_category)
);


-------------------------------------------------------------------------------
-- Move IncidentCategory1 to IncidentResolutionType
-------------------------------------------------------------------------------
--
-- New table 'IncidentResolutionType'
--
CREATE TABLE IncidentResolutionType (
  incidentresolutiontype_id          int(8) auto_increment,
  incidentresolutiontype_domain_id   int(8) default 0,
  incidentresolutiontype_timeupdate  timestamp(14),
  incidentresolutiontype_timecreate  timestamp(14),
  incidentresolutiontype_userupdate  int(8) default NULL,
  incidentresolutiontype_usercreate  int(8) default NULL,
  incidentresolutiontype_code        varchar(10) default '',
  incidentresolutiontype_label       varchar(32) default NULL,
PRIMARY KEY (incidentresolutiontype_id)
);


INSERT INTO IncidentResolutionType (
  incidentresolutiontype_id,
  incidentresolutiontype_domain_id,
  incidentresolutiontype_timeupdate,
  incidentresolutiontype_timecreate,
  incidentresolutiontype_userupdate,
  incidentresolutiontype_usercreate,
  incidentresolutiontype_code,
  incidentresolutiontype_label)
SELECT
  incidentcategory1_id,
  1,
  incidentcategory1_timeupdate,
  incidentcategory1_timecreate,
  incidentcategory1_userupdate,
  incidentcategory1_usercreate,
  incidentcategory1_code,
  incidentcategory1_label
FROM IncidentCategory1;


ALTER TABLE Incident ADD COLUMN incident_resolutiontype_id int(8) DEFAULT 0 after incident_status_id;
UPDATE Incident set incident_resolutiontype_id=incident_category1_id;
ALTER TABLE Incident DROP COLUMN incident_category1_id;
DROP TABLE IncidentCategory1;


---------------------------------------------------------------------------
-- Add Project reference and cv infos
---------------------------------------------------------------------------

---------------------------------------------------------------------------
-- Update UserObm table
---------------------------------------------------------------------------
-- Add existent column which had not been added
ALTER TABLE UserObm ADD COLUMN userobm_location varchar(255) DEFAULT '' AFTER userobm_description;
ALTER TABLE UserObm ADD COLUMN userobm_education varchar(255) DEFAULT '' AFTER userobm_location;


---------------------------------------------------------------------------
--Update Project table
---------------------------------------------------------------------------
ALTER TABLE Project ADD COLUMN project_reference_date varchar(32) DEFAULT '';
ALTER TABLE Project ADD COLUMN project_reference_duration varchar(16) DEFAULT '';
ALTER TABLE Project ADD COLUMN project_reference_desc text DEFAULT '';
ALTER TABLE Project ADD COLUMN project_reference_tech text DEFAULT '';

----------------------------------------------------------------------------
--Create CV table
----------------------------------------------------------------------------

CREATE TABLE CV (
  cv_id              int(8) auto_increment,
  cv_domain_id       int(8) default 0,
  cv_timeupdate      timestamp(14),
  cv_timecreate      timestamp(14),
  cv_userupdate      int(8),
  cv_usercreate      int(8),
  cv_userobm_id      int(8) NOT NULL,
  cv_title           varchar(255),
  cv_additionnalrefs text,
  cv_comment         text,
  PRIMARY KEY(cv_id)
);

----------------------------------------------------------------------------
--Create ProjectCV table
----------------------------------------------------------------------------

CREATE TABLE ProjectCV (
  projectcv_project_id int(8) NOT NULL,
  projectcv_cv_id      int(8) NOT NULL,
  projectcv_role       varchar(128) DEFAULT '',
  PRIMARY KEY(projectcv_project_id, projectcv_cv_id)
);

----------------------------------------------------------------------------
--Create DefaultOdtTemplate table
----------------------------------------------------------------------------

CREATE TABLE DefaultOdtTemplate (
  defaultodttemplate_id                  int(8) auto_increment,
  defaultodttemplate_entity              varchar(32),
  defaultodttemplate_document_id         int(8) NOT NULL,
  defaultodttemplate_label               varchar(64) DEFAULT '',
  PRIMARY KEY(defaultodttemplate_id)
);


---------------------------------------------------------------------------
-- Update UserObm table from Aliamin
---------------------------------------------------------------------------
-- Add existent column which had not been added
ALTER TABLE UserObm ADD COLUMN userobm_mobile varchar(32) DEFAULT '' AFTER userobm_phone2;

ALTER TABLE UserObm ADD COLUMN userobm_system int(1) DEFAULT 0 AFTER userobm_ext_id;
ALTER TABLE UserObm ADD COLUMN userobm_password_type char(6) DEFAULT 'PLAIN' AFTER userobm_login;
ALTER TABLE UserObm ADD COLUMN userobm_uid int(8) AFTER userobm_calendar_version;
ALTER TABLE UserObm ADD COLUMN userobm_gid int(8) AFTER userobm_uid;
ALTER TABLE UserObm ADD COLUMN userobm_title varchar(64) DEFAULT '' AFTER userobm_firstname;
ALTER TABLE UserObm ADD COLUMN userobm_sound varchar(48) AFTER userobm_title;
ALTER TABLE UserObm ADD COLUMN userobm_service varchar(64) AFTER userobm_sound;
ALTER TABLE UserObm ADD COLUMN userobm_address1 varchar(64) AFTER userobm_service;
ALTER TABLE UserObm ADD COLUMN userobm_address2 varchar(64) AFTER userobm_address1;
ALTER TABLE UserObm ADD COLUMN userobm_address3 varchar(64) AFTER userobm_address2;
ALTER TABLE UserObm ADD COLUMN userobm_zipcode varchar(14) AFTER userobm_address3;
ALTER TABLE UserObm ADD COLUMN userobm_town varchar(64) AFTER userobm_zipcode;
ALTER TABLE UserObm ADD COLUMN userobm_expresspostal varchar(16) AFTER userobm_town;
ALTER TABLE UserObm ADD COLUMN userobm_country_iso3166 char(2) DEFAULT '0' AFTER userobm_expresspostal;
ALTER TABLE UserObm ADD COLUMN userobm_web_perms int(1) default 0 AFTER userobm_fax2;
ALTER TABLE UserObm ADD COLUMN userobm_web_list text AFTER userobm_web_perms;
ALTER TABLE UserObm ADD COLUMN userobm_web_all int(1) default 0 AFTER userobm_web_list;
ALTER TABLE UserObm ADD COLUMN userobm_mail_perms int(1) default 0 AFTER userobm_web_all;
ALTER TABLE UserObm ADD COLUMN userobm_mail_ext_perms int(1) default 0 AFTER userobm_mail_perms;
ALTER TABLE UserObm CHANGE COLUMN userobm_email userobm_email text default NULL;
ALTER TABLE UserObm ADD COLUMN userobm_mail_server_id int(8) default NULL AFTER userobm_email;
ALTER TABLE UserObm ADD COLUMN userobm_mail_quota varchar(8) default NULL AFTER userobm_mail_server_id;
ALTER TABLE UserObm ADD COLUMN userobm_nomade_perms int(1) default 0 AFTER userobm_mail_quota;
ALTER TABLE UserObm ADD COLUMN userobm_nomade_enable int(1) default 0 AFTER userobm_nomade_perms;
ALTER TABLE UserObm ADD COLUMN userobm_nomade_local_copy int(1) default 0 AFTER userobm_nomade_enable;
ALTER TABLE UserObm ADD COLUMN userobm_email_nomade varchar(64) default '' AFTER userobm_nomade_local_copy;
ALTER TABLE UserObm ADD COLUMN userobm_vacation_enable int(1) default 0 AFTER userobm_email_nomade;
ALTER TABLE UserObm ADD COLUMN userobm_vacation_message text default '' AFTER userobm_vacation_enable;
ALTER TABLE UserObm ADD COLUMN userobm_samba_perms int(1) default 0 AFTER userobm_vacation_message;
ALTER TABLE UserObm ADD COLUMN userobm_samba_home varchar(255) default '' AFTER userobm_samba_perms;
ALTER TABLE UserObm ADD COLUMN userobm_samba_home_drive char(2) default '' AFTER userobm_samba_home;
ALTER TABLE UserObm ADD COLUMN userobm_samba_logon_script varchar(255) default '' AFTER userobm_samba_home_drive;
ALTER TABLE UserObm ADD COLUMN userobm_host_id int(8) default NULL AFTER userobm_samba_logon_script;

-- add constraint ...
--  UNIQUE KEY k_login_user (userobm_login),
--  INDEX k_uid_user (userobm_uid)


---------------------------------------------------------------------------
-- Update UGroup table from Aliamin
---------------------------------------------------------------------------
ALTER TABLE UGroup ADD COLUMN group_samba int(1) default 0 AFTER group_ext_id;
ALTER TABLE UGroup ADD COLUMN group_gid int(8) AFTER group_samba;
ALTER TABLE UGroup ADD COLUMN group_mailing int(1) AFTER group_gid;
ALTER TABLE UGroup ADD COLUMN group_contacts text AFTER group_email;

-- add constraint  UNIQUE KEY group_gid (group_gid)


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Host'
--
CREATE TABLE Host (
  host_id               int(8) NOT NULL auto_increment,
  host_domain_id        int(8) default 0,
  host_timeupdate       timestamp(14),
  host_timecreate       timestamp(14),
  host_userupdate       int(8),
  host_usercreate       int(8),
  host_uid              int(8),
  host_gid              int(8),
  host_samba            int(1) DEFAULT 0,
  host_name             varchar(32) NOT NULL,
  host_ip               varchar(16),
  host_description      varchar(128),
  host_web_perms        int(1) default 0,
  host_web_list         text default '',
  host_web_all		int(1) default 0,
  host_ftp_perms        int(1) default 0,
  host_firewall_perms   varchar(128),
  PRIMARY KEY (host_id),
  UNIQUE host_name (host_name),
  UNIQUE KEY k_uid_host (host_uid)
);


--
-- Storage for stats
--
CREATE TABLE Stats (
  stats_name   varchar(32) NOT NULL default '',
  stats_value  varchar(255) NOT NULL default '',
  PRIMARY KEY (stats_name)
);


--
-- LDAP tree structure table
--
CREATE TABLE Ldap (
  ldap_id         int(11) default NULL,
  ldap_domain_id  int(8) default 0,
  ldap_parent_id  int(11) default NULL,
  ldap_name       varchar(255) default NULL,
  ldap_value      varchar(255) default NULL
);


--
-- Mail parameters table
--
CREATE TABLE Mail (
  mail_domain_id  int(8) default 0,
  mail_name       varchar(255) NOT NULL default '',
  mail_value      varchar(255) NOT NULL default ''
);


--
-- Samba parameters table
--
CREATE TABLE Samba (
  samba_domain_id  int(8) default 0,
  samba_name      varchar(255) NOT NULL default '',
  samba_value     varchar(255) NOT NULL default ''
);


--
-- Shared bals table
--
CREATE TABLE MailShareDir (
  mailsharedir_id            int(8) NOT NULL auto_increment,
  mailsharedir_domain_id     int(8) default 0,
  mailsharedir_timeupdate    timestamp(14),
  mailsharedir_timecreate    timestamp(14),
  mailsharedir_userupdate    int(8),
  mailsharedir_usercreate    int(8),
  mailsharedir_name          varchar(32),
  mailsharedir_quota         int default 0 NOT NULL,
  mailsharedir_description   varchar(255),
  mailsharedir_email         text default NULL,
  PRIMARY KEY (mailsharedir_id)
);


-----------------------------------------------------------------------------
-- Table contenant les parametres generaux et non necessaires pour les 
-- sessions
-----------------------------------------------------------------------------
CREATE TABLE Parameters (
  parameters_name   varchar(255) NOT NULL default '',
  parameters_value  varchar(255) NOT NULL default '',
  PRIMARY KEY (parameters_name),
  UNIQUE KEY parameters_name (parameters_name)
);


CREATE TABLE UserSystem (
  usersystem_id         int(8) NOT NULL auto_increment,
  usersystem_login      varchar(32) NOT NULL default '',
  usersystem_password   varchar(32) NOT NULL default '',
  usersystem_uid        varchar(6) default NULL,
  usersystem_gid        varchar(6) default NULL,
  usersystem_homedir    varchar(32) NOT NULL default '/tmp',
  usersystem_lastname   varchar(32) default NULL,
  usersystem_firstname  varchar(32) default NULL,
  usersystem_shell      varchar(32) default NULL,
  PRIMARY KEY (usersystem_id),
  UNIQUE KEY k_login_user (usersystem_login)
);


-----------------------------------------------------------------------------
-- Mail server declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServer (
  mailserver_id             int(8) NOT NULL auto_increment,
  mailserver_host_id        int(8) NOT NULL default 0,
  mailserver_relayhost_id   int(8) default NULL,
  PRIMARY KEY (mailserver_id)
);


-----------------------------------------------------------------------------
-- Mail server network declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServerNetwork (
  mailservernetwork_host_id   int(8) NOT NULL default 0,
  mailservernetwork_ip              varchar(16) NOT NULL default ''
);


-------------------------------------------------------------------------------
-- Suppression de l'ancienne table Mail
-------------------------------------------------------------------------------
DROP TABLE IF EXISTS Mail;


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP Production tables (used by automate)
-------------------------------------------------------------------------------

CREATE TABLE P_UserObm like UserObm;
CREATE TABLE P_UGroup like UGroup;
CREATE TABLE P_UserObmGroup like UserObmGroup;
CREATE TABLE P_GroupGroup like GroupGroup;
CREATE TABLE P_Host like Host;
CREATE TABLE P_Samba like Samba;
CREATE TABLE P_Ldap like Ldap;
CREATE TABLE P_MailServer like MailServer;
CREATE TABLE P_MailServerNetwork like MailServerNetwork;
CREATE TABLE P_MailShareDir like MailShareDir;
CREATE TABLE P_EntityRight like EntityRight;
-- CREATE TABLE P_Network like Network;
