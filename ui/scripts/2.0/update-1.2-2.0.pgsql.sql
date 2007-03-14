-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 1.2 to 2.0                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='2.0' where obminfo_name='db_version';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('update_state', '0');
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('remote_access', '0');


-------------------------------------------------------------------------------
-- Add Domain info
-------------------------------------------------------------------------------
--
-- Table structure for table 'Domain'
--
CREATE TABLE Domain (
  domain_id             serial,
  domain_timeupdate     timestamp,
  domain_timecreate     timestamp,
  domain_usercreate     integer,
  domain_userupdate     integer,
  domain_label          varchar(32) NOT NULL,
  domain_description    varchar(255),
  domain_name           varchar(128),
  domain_alias          text,
  domain_mail_server_id integer DEFAULT NULL,
  PRIMARY KEY (domain_id)
);

INSERT INTO Domain (
  domain_id,
  domain_timecreate,
  domain_usercreate,
  domain_label,
  domain_description,
  domain_name)
VALUES (
  1,
  NOW(),
  0,
  'Main',
  'Main domain',
  'localdomain.local');


--
-- Update All tables to include Domain info
--
ALTER TABLE UserObm ADD Column userobm_domain_id integer;
ALTER TABLE UserObm ALTER COLUMN userobm_domain_id SET DEFAULT 0;
UPDATE UserObm SET userobm_domain_id=1;

ALTER TABLE DataSource ADD Column datasource_domain_id integer;
ALTER TABLE DataSource ALTER COLUMN datasource_domain_id SET DEFAULT 0;
UPDATE DataSource SET datasource_domain_id=1;

ALTER TABLE Country ADD Column country_domain_id integer;
ALTER TABLE Country ALTER COLUMN country_domain_id  SET DEFAULT 0;
UPDATE Country SET country_domain_id=1;

ALTER TABLE Region ADD Column region_domain_id integer;
ALTER TABLE Region ALTER COLUMN region_domain_id SET DEFAULT 0;
UPDATE Region SET region_domain_id=1;

ALTER TABLE CompanyType ADD Column companytype_domain_id integer;
ALTER TABLE CompanyType ALTER COLUMN companytype_domain_id SET DEFAULT 0;
UPDATE CompanyType SET companytype_domain_id=1;

ALTER TABLE CompanyActivity ADD Column companyactivity_domain_id integer;
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_domain_id SET DEFAULT 0;
UPDATE CompanyActivity SET companyactivity_domain_id=1;

ALTER TABLE CompanyNafCode ADD Column companynafcode_domain_id integer;
ALTER TABLE CompanyNafCode ALTER COLUMN companynafcode_domain_id SET DEFAULT 0;
UPDATE CompanyNafCode SET companynafcode_domain_id=1;

ALTER TABLE Company ADD Column company_domain_id integer;
ALTER TABLE Company ALTER COLUMN company_domain_id SET DEFAULT 0;
UPDATE Company SET company_domain_id=1;

ALTER TABLE Contact ADD Column contact_domain_id integer;
ALTER TABLE Contact ALTER COLUMN contact_domain_id SET DEFAULT 0;
UPDATE Contact SET contact_domain_id=1;

ALTER TABLE Kind ADD Column kind_domain_id integer;
ALTER TABLE Kind ALTER COLUMN kind_domain_id SET DEFAULT 0;
UPDATE Kind SET kind_domain_id=1;

ALTER TABLE ContactFunction ADD Column contactfunction_domain_id integer;
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_domain_id SET DEFAULT 0;
UPDATE ContactFunction SET contactfunction_domain_id=1;

ALTER TABLE LeadSource ADD Column leadsource_domain_id integer;
ALTER TABLE LeadSource ALTER COLUMN leadsource_domain_id SET DEFAULT 0;
UPDATE LeadSource SET leadsource_domain_id=1;

ALTER TABLE Lead ADD Column lead_domain_id integer;
ALTER TABLE Lead ALTER COLUMN lead_domain_id SET DEFAULT 0;
UPDATE Lead SET lead_domain_id=1;

ALTER TABLE ParentDeal ADD Column parentdeal_domain_id integer;
ALTER TABLE ParentDeal ALTER COLUMN parentdeal_domain_id SET DEFAULT 0;
UPDATE ParentDeal SET parentdeal_domain_id=1;

ALTER TABLE Deal ADD Column deal_domain_id integer;
ALTER TABLE Deal ALTER COLUMN deal_domain_id SET DEFAULT 0;
UPDATE Deal SET deal_domain_id=1;

ALTER TABLE DealStatus ADD Column dealstatus_domain_id integer;
ALTER TABLE DealStatus ALTER COLUMN dealstatus_domain_id  SET DEFAULT 0;
UPDATE DealStatus SET dealstatus_domain_id=1;

ALTER TABLE DealType ADD Column dealtype_domain_id integer;
ALTER TABLE DealType ALTER COLUMN dealtype_domain_id SET DEFAULT 0;
UPDATE DealType SET dealtype_domain_id=1;

ALTER TABLE DealCompanyRole ADD Column dealcompanyrole_domain_id integer;
ALTER TABLE DealCompanyRole ALTER COLUMN dealcompanyrole_domain_id SET DEFAULT 0;
UPDATE DealCompanyRole SET dealcompanyrole_domain_id=1;

ALTER TABLE List ADD Column list_domain_id integer;
ALTER TABLE List ALTER COLUMN list_domain_id SET DEFAULT 0;
UPDATE List SET list_domain_id=1;

ALTER TABLE CalendarEvent ADD Column calendarevent_domain_id integer;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_domain_id SET DEFAULT 0;
UPDATE CalendarEvent SET calendarevent_domain_id=1;

ALTER TABLE CalendarCategory1 ADD Column calendarcategory1_domain_id integer;
ALTER TABLE CalendarCategory1 ALTER COLUMN calendarcategory1_domain_id SET DEFAULT 0;
UPDATE CalendarCategory1 SET calendarcategory1_domain_id=1;

ALTER TABLE Todo ADD Column todo_domain_id integer;
ALTER TABLE Todo ALTER Column todo_domain_id SET default 0;
UPDATE Todo SET todo_domain_id=1;

ALTER TABLE Publication ADD Column publication_domain_id integer;
ALTER TABLE Publication ALTER Column publication_domain_id SET default 0;
UPDATE Publication SET publication_domain_id=1;

ALTER TABLE PublicationType ADD Column publicationtype_domain_id integer;
ALTER TABLE PublicationType ALTER Column publicationtype_domain_id SET default 0;
UPDATE PublicationType SET publicationtype_domain_id=1;

ALTER TABLE Subscription ADD Column subscription_domain_id integer;
ALTER TABLE Subscription ALTER Column subscription_domain_id SET default 0;
UPDATE Subscription SET subscription_domain_id=1;

ALTER TABLE SubscriptionReception ADD Column subscriptionreception_domain_id integer;
ALTER TABLE SubscriptionReception ALTER Column subscriptionreception_domain_id SET default 0;
UPDATE SubscriptionReception SET subscriptionreception_domain_id=1;

ALTER TABLE Document ADD Column document_domain_id integer;
ALTER TABLE Document ALTER Column document_domain_id SET default 0;
UPDATE Document SET document_domain_id=1;

ALTER TABLE DocumentMimeType ADD Column documentmimetype_domain_id integer;
ALTER TABLE DocumentMimeType ALTER Column documentmimetype_domain_id SET default 0;
UPDATE DocumentMimeType SET documentmimetype_domain_id=1;

ALTER TABLE Project ADD Column project_domain_id integer;
ALTER TABLE Project ALTER Column project_domain_id SET default 0;
UPDATE Project SET project_domain_id=1;

ALTER TABLE TaskType ADD Column tasktype_domain_id integer;
ALTER TABLE TaskType ALTER Column tasktype_domain_id SET default 0;
UPDATE TaskType SET tasktype_domain_id=1;

ALTER TABLE Contract ADD Column contract_domain_id integer;
ALTER TABLE Contract ALTER Column contract_domain_id SET default 0;
UPDATE Contract SET contract_domain_id=1;

ALTER TABLE ContractType ADD Column contracttype_domain_id integer;
ALTER TABLE ContractType ALTER Column contracttype_domain_id SET default 0;
UPDATE ContractType SET contracttype_domain_id=1;

ALTER TABLE ContractPriority ADD Column contractpriority_domain_id integer;
ALTER TABLE ContractPriority ALTER Column contractpriority_domain_id SET default 0;
UPDATE ContractPriority SET contractpriority_domain_id=1;

ALTER TABLE ContractStatus ADD Column contractstatus_domain_id integer;
ALTER TABLE ContractStatus ALTER Column contractstatus_domain_id SET default 0;
UPDATE ContractStatus SET contractstatus_domain_id=1;

ALTER TABLE Incident ADD Column incident_domain_id integer;
ALTER TABLE Incident ALTER Column incident_domain_id SET default 0;
UPDATE Incident SET incident_domain_id=1;

ALTER TABLE IncidentPriority ADD Column incidentpriority_domain_id integer;
ALTER TABLE IncidentPriority ALTER Column incidentpriority_domain_id SET default 0;
UPDATE IncidentPriority SET incidentpriority_domain_id=1;

ALTER TABLE IncidentStatus ADD Column incidentstatus_domain_id integer;
ALTER TABLE IncidentStatus ALTER Column incidentstatus_domain_id SET default 0;
UPDATE IncidentStatus SET incidentstatus_domain_id=1;

ALTER TABLE Invoice ADD Column invoice_domain_id integer;
ALTER TABLE Invoice ALTER Column invoice_domain_id SET default 0;
UPDATE Invoice SET invoice_domain_id=1;

ALTER TABLE InvoiceStatus ADD Column invoicestatus_domain_id integer;
ALTER TABLE InvoiceStatus ALTER Column invoicestatus_domain_id SET default 0;
UPDATE InvoiceStatus SET invoicestatus_domain_id=1;

ALTER TABLE Payment ADD Column payment_domain_id integer;
ALTER TABLE Payment ALTER Column payment_domain_id SET default 0;
UPDATE Payment SET payment_domain_id=1;

ALTER TABLE PaymentKind ADD Column paymentkind_domain_id integer;
ALTER TABLE PaymentKind ALTER Column paymentkind_domain_id SET default 0;
UPDATE PaymentKind SET paymentkind_domain_id=1;

ALTER TABLE Account ADD Column account_domain_id integer;
ALTER TABLE Account ALTER Column account_domain_id SET default 0;
UPDATE Account SET account_domain_id=1;

ALTER TABLE UGroup ADD Column group_domain_id integer;
ALTER TABLE UGroup ALTER Column group_domain_id SET default 0;
UPDATE UGroup SET group_domain_id=1;

ALTER TABLE Import ADD Column import_domain_id integer;
ALTER TABLE Import ALTER Column import_domain_id SET default 0;
UPDATE Import SET import_domain_id=1;

ALTER TABLE Resource ADD Column resource_domain_id integer;
ALTER TABLE Resource ALTER Column resource_domain_id SET default 0;
UPDATE Resource SET resource_domain_id=1;

ALTER TABLE RGroup ADD Column rgroup_domain_id integer;
ALTER TABLE RGroup ALTER Column rgroup_domain_id SET default 0;
UPDATE RGroup SET rgroup_domain_id=1;


-------------------------------------------------------------------------------
-- Global Category table
-------------------------------------------------------------------------------
--
-- Table structure for table 'Category'
--
CREATE TABLE Category (
  category_id          serial,
  category_domain_id   integer NOT NULL DEFAULT 0,
  category_timeupdate  TIMESTAMP,
  category_timecreate  TIMESTAMP,
  category_userupdate  integer,
  category_usercreate  integer,
  category_category    varchar(24) NOT NULL default '',
  category_code        varchar(10) NOT NULL default '',
  category_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (category_id)
);
CREATE INDEX cat_idx_cat ON Category (category_category);


--
-- Table structure for table 'CategoryLink'
--
CREATE TABLE CategoryLink (
  categorylink_category_id integer,
  categorylink_entity_id   integer,
  categorylink_category    varchar(24) NOT NULL default '',
  categorylink_entity      varchar(32) NOT NULL default '',
  PRIMARY KEY (categorylink_category_id, categorylink_entity_id)
);
CREATE INDEX catl_idx_entid ON CategoryLink (categorylink_entity_id);
CREATE INDEX catl_idx_cat ON CategoryLink (categorylink_category);


-------------------------------------------------------------------------------
-- Move IncidentCategory1 to IncidentResolutionType
-------------------------------------------------------------------------------
--
-- New table 'IncidentResolutionType'
--
CREATE TABLE IncidentResolutionType (
  incidentresolutiontype_id          serial,
  incidentresolutiontype_domain_id   integer default 0,
  incidentresolutiontype_timeupdate  timestamp,
  incidentresolutiontype_timecreate  timestamp,
  incidentresolutiontype_userupdate  integer DEFAULT NULL,
  incidentresolutiontype_usercreate  integer DEFAULT NULL,
  incidentresolutiontype_code        varchar(10) default '',
  incidentresolutiontype_label       varchar(32) DEFAULT NULL,
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


ALTER TABLE Incident ADD COLUMN incident_resolutiontype_id integer;
ALTER TABLE Incident ALTER COLUMN incident_resolutiontype_id SET DEFAULT 0;
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
ALTER TABLE UserObm ADD COLUMN userobm_location varchar(255);
ALTER TABLE UserObm ALTER COLUMN userobm_location SET DEFAULT '';
ALTER TABLE UserObm ADD COLUMN userobm_education varchar(255);
ALTER TABLE UserObm ALTER COLUMN userobm_education SET DEFAULT '';


---------------------------------------------------------------------------
-- Update Project table
---------------------------------------------------------------------------
ALTER TABLE Project ADD COLUMN project_reference_date varchar(32);
ALTER TABLE Project ALTER COLUMN project_reference_date SET DEFAULT '';
ALTER TABLE Project ADD COLUMN project_reference_duration varchar(16);
ALTER TABLE Project ALTER COLUMN project_reference_duration SET DEFAULT '';
ALTER TABLE Project ADD COLUMN project_reference_desc text;
ALTER TABLE Project ALTER COLUMN project_reference_desc SET DEFAULT '';
ALTER TABLE Project ADD COLUMN project_reference_tech text;
ALTER TABLE Project ALTER COLUMN project_reference_tech SET DEFAULT '';


----------------------------------------------------------------------------
-- Create CV table
----------------------------------------------------------------------------
CREATE TABLE CV (
  cv_id              serial,
  cv_domain_id       integer default 0,
  cv_timeupdate      TIMESTAMP,
  cv_timecreate      TIMESTAMP,
  cv_userupdate      integer,
  cv_usercreate      integer,
  cv_userobm_id      integer,
  cv_title           varchar(255),
  cv_additionnalrefs text,
  cv_comment         text,
  PRIMARY KEY(cv_id)
);


----------------------------------------------------------------------------
-- Create ProjectCV table
----------------------------------------------------------------------------
CREATE TABLE ProjectCV (
  projectcv_project_id integer NOT NULL,
  projectcv_cv_id      integer NOT NULL,
  projectcv_role       varchar(128),
  PRIMARY KEY(projectcv_project_id, projectcv_cv_id)
);


----------------------------------------------------------------------------
-- Create DefaultOdtTemplate table
----------------------------------------------------------------------------
CREATE TABLE DefaultOdtTemplate (
  defaultodttemplate_id           serial,
  defaultodttemplate_domain_id    integer DEFAULT 0,
  defaultodttemplate_entity       varchar(32),
  defaultodttemplate_document_id  integer NOT NULL,
  defaultodttemplate_label        varchar(64) DEFAULT '',
  PRIMARY KEY(defaultodttemplate_id)
);


----------------------------------------------------------------------------
-- Update Contact table
----------------------------------------------------------------------------
ALTER TABLE Contact ADD COLUMN contact_newsletter char(1);
ALTER TABLE Contact ALTER COLUMN contact_newsletter SET DEFAULT '0';


---------------------------------------------------------------------------
-- Update UserObm table from Aliamin
---------------------------------------------------------------------------
ALTER TABLE UserObm ADD COLUMN userobm_mobile varchar(32);
ALTER TABLE UserObm ALTER COLUMN userobm_mobile SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_system integer;
ALTER TABLE UserObm ALTER COLUMN userobm_system SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_password_type char(6);
ALTER TABLE UserObm ALTER COLUMN userobm_password_type SET DEFAULT 'PLAIN';

ALTER TABLE UserObm ADD COLUMN userobm_uid integer;

ALTER TABLE UserObm ADD COLUMN userobm_gid integer;

ALTER TABLE UserObm ADD COLUMN userobm_title varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_title SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_sound varchar(48);

ALTER TABLE UserObm ADD COLUMN userobm_service varchar(64);

ALTER TABLE UserObm ADD COLUMN userobm_address1 varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_address1 SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_address2 varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_address2 SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_address3 varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_address3 SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_zipcode varchar(14);
ALTER TABLE UserObm ALTER COLUMN userobm_zipcode SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_town varchar(64);

ALTER TABLE UserObm ADD COLUMN userobm_expresspostal varchar(16);

ALTER TABLE UserObm ADD COLUMN userobm_country_iso3166 char(2);
ALTER TABLE UserObm ALTER COLUMN userobm_country_iso3166 SET DEFAULT '0';

ALTER TABLE UserObm ADD COLUMN userobm_web_perms integer;
ALTER TABLE UserObm ALTER COLUMN userobm_web_perms SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_web_list text;

ALTER TABLE UserObm ADD COLUMN userobm_web_all integer;
ALTER TABLE UserObm ALTER COLUMN userobm_web_all SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_mail_perms integer;
ALTER TABLE UserObm ALTER COLUMN userobm_mail_perms SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_mail_ext_perms integer;
ALTER TABLE UserObm ALTER COLUMN userobm_mail_ext_perms SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN temp_email text;
UPDATE UserObm SET temp_email = userobm_email;
ALTER TABLE UserObm DROP COLUMN userobm_email;
ALTER TABLE UserObm RENAME COLUMN temp_email TO userobm_email;

ALTER TABLE UserObm ADD COLUMN userobm_mail_server_id integer;

ALTER TABLE UserObm ADD COLUMN userobm_mail_quota varchar(8);

ALTER TABLE UserObm ADD COLUMN userobm_nomade_perms integer;
ALTER TABLE UserObm ALTER COLUMN userobm_nomade_perms SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_nomade_enable integer;
ALTER TABLE UserObm ALTER COLUMN userobm_nomade_enable SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_nomade_local_copy integer;
ALTER TABLE UserObm ALTER COLUMN userobm_nomade_local_copy SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_email_nomade varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_email_nomade SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_vacation_enable integer;
ALTER TABLE UserObm ALTER COLUMN userobm_vacation_enable SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_vacation_message text;
ALTER TABLE UserObm ALTER COLUMN userobm_vacation_message SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_samba_perms integer;
ALTER TABLE UserObm ALTER COLUMN userobm_samba_perms SET DEFAULT 0;

ALTER TABLE UserObm ADD COLUMN userobm_samba_home varchar(255);
ALTER TABLE UserObm ALTER COLUMN userobm_samba_home SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_samba_home_drive char(2);
ALTER TABLE UserObm ALTER COLUMN userobm_samba_home_drive SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_samba_logon_script varchar(255);
ALTER TABLE UserObm ALTER COLUMN userobm_samba_logon_script SET DEFAULT '';

ALTER TABLE UserObm ADD COLUMN userobm_host_id integer;

ALTER TABLE UserObm ADD COLUMN temp_password varchar(64);
UPDATE UserObm SET temp_password = userobm_password;
ALTER TABLE UserObm DROP COLUMN userobm_password;
ALTER TABLE UserObm RENAME COLUMN temp_password TO userobm_password;

-- Update user infos to new datas
UPDATE UserObmPref set userobmpref_value='default' WHERE userobmpref_option='set_theme';
UPDATE UserObm set userobm_password_type='md5';

-- add constraint UNIQUE KEY userobm_uid (userobm_uid)
CREATE UNIQUE INDEX k_uid_userobm_index ON UserObm (userobm_uid);


---------------------------------------------------------------------------
-- Update UGroup table from Aliamin
---------------------------------------------------------------------------
ALTER TABLE UGroup ADD COLUMN group_samba integer;
ALTER TABLE UGroup ALTER COLUMN group_samba SET DEFAULT 0;

ALTER TABLE UGroup ADD COLUMN group_gid integer;
ALTER TABLE UGroup ALTER COLUMN group_samba SET DEFAULT 0;

ALTER TABLE UGroup ADD COLUMN group_mailing integer;
ALTER TABLE UGroup ALTER COLUMN group_mailing SET DEFAULT 0;

ALTER TABLE UGroup ADD COLUMN group_contacts text;
ALTER TABLE UGroup ALTER COLUMN group_contacts SET DEFAULT '';

ALTER TABLE UGroup ADD COLUMN temp_ext_id varchar(24);
UPDATE UGroup SET temp_ext_id = group_ext_id;
ALTER TABLE UGroup DROP COLUMN group_ext_id;
ALTER TABLE UGroup RENAME COLUMN temp_ext_id TO group_ext_id;

-- add constraint UNIQUE KEY group_gid (group_gid)
CREATE UNIQUE INDEX k_gid_group_index ON UGroup (group_gid);


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Host'
--
CREATE TABLE Host (
  host_id               serial,
  host_domain_id        integer default 0,
  host_timeupdate       timestamp,
  host_timecreate       timestamp,
  host_userupdate       integer,
  host_usercreate       integer,
  host_uid              integer,
  host_gid              integer,
  host_samba            integer DEFAULT 0,
  host_name             varchar(32) NOT NULL,
  host_ip               varchar(16),
  host_description      varchar(128),
  host_web_perms        integer default 0,
  host_web_list         text default '',
  host_web_all		integer default 0,
  host_ftp_perms        integer default 0,
  host_firewall_perms   varchar(128),
  PRIMARY KEY (host_id),
  UNIQUE (host_name),
  UNIQUE (host_uid)
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
-- Samba parameters table
--
CREATE TABLE Samba (
  samba_domain_id  integer default 0,
  samba_name       varchar(255) NOT NULL default '',
  samba_value      varchar(255) NOT NULL default ''
);


--
-- Shared bals table
--
CREATE TABLE MailShare (
  mailshare_id             serial,
  mailshare_domain_id      integer default 0,
  mailshare_timeupdate     timestamp,
  mailshare_timecreate     timestamp,
  mailshare_userupdate     integer,
  mailshare_usercreate     integer,
  mailshare_name           varchar(32),
  mailshare_quota          varchar(8) default '0' NOT NULL,
  mailshare_mail_server_id integer default 0,
  mailshare_description    varchar(255),
  mailshare_email          text default NULL,
  PRIMARY KEY (mailshare_id)
);


CREATE TABLE UserSystem (
  usersystem_id         serial,
  usersystem_login      varchar(32) NOT NULL default '',
  usersystem_password   varchar(32) NOT NULL default '',
  usersystem_uid        varchar(6) default NULL,
  usersystem_gid        varchar(6) default NULL,
  usersystem_homedir    varchar(32) NOT NULL default '/tmp',
  usersystem_lastname   varchar(32) default NULL,
  usersystem_firstname  varchar(32) default NULL,
  usersystem_shell      varchar(32) default NULL,
  PRIMARY KEY (usersystem_id),
  UNIQUE (usersystem_login)
);


-----------------------------------------------------------------------------
-- Mail server declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServer (
  mailserver_id            serial,
  mailserver_host_id       integer NOT NULL default 0,
  mailserver_relayhost_id  integer default NULL,
  PRIMARY KEY (mailserver_id)
);


-----------------------------------------------------------------------------
-- Mail server network declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServerNetwork (
  mailservernetwork_host_id  integer NOT NULL default 0,
  mailservernetwork_ip       varchar(16) NOT NULL default ''
);


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP Production tables (used by automate)
-------------------------------------------------------------------------------

CREATE TABLE P_UserObm (like UserObm);
CREATE TABLE P_UGroup (like UGroup);
CREATE TABLE P_UserObmGroup (like UserObmGroup);
CREATE TABLE P_GroupGroup (like GroupGroup);
CREATE TABLE P_of_usergroup (like UserObmGroup);
CREATE TABLE P_Host (like Host);
CREATE TABLE P_Samba (like Samba);
CREATE TABLE P_MailServer (like MailServer);
CREATE TABLE P_MailServerNetwork (like MailServerNetwork);
CREATE TABLE P_MailShare (like MailShare);
CREATE TABLE P_EntityRight (like EntityRight);
