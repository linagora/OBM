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
  PRIMARY KEY (domain_id)
);

INSERT INTO Domain VALUES (1,NOW(),NOW(),0,0,'Main','Main domain','localdomain','');

--
-- Update All tables to include Domain info
--
ALTER TABLE UserObm ADD Column userobm_domain_id integer;
ALTER TABLE UserObm ALTER COLUMN userobm_domain_id SET DEFAULT 0;
UPDATE UserObm SET userobm_domain_id=0;
ALTER TABLE DataSource ADD Column datasource_domain_id integer;
ALTER TABLE DataSource ALTER COLUMN datasource_domain_id SET DEFAULT 0;
ALTER TABLE Country ADD Column country_domain_id integer;
ALTER TABLE Country ALTER COLUMN country_domain_id  SET DEFAULT 0;
ALTER TABLE Region ADD Column region_domain_id integer;
ALTER TABLE Region ALTER COLUMN region_domain_id SET DEFAULT 0;
ALTER TABLE CompanyType ADD Column companytype_domain_id integer;
ALTER TABLE CompanyType ALTER COLUMN companytype_domain_id SET DEFAULT 0;
ALTER TABLE CompanyActivity ADD Column companyactivity_domain_id integer;
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_domain_id SET DEFAULT 0;
ALTER TABLE CompanyNafCode ADD Column companynafcode_domain_id integer;
ALTER TABLE CompanyNafCode ALTER COLUMN companynafcode_domain_id SET DEFAULT 0;
ALTER TABLE Company ADD Column company_domain_id integer;
ALTER TABLE Company ALTER COLUMN company_domain_id SET DEFAULT 0;
ALTER TABLE Contact ADD Column contact_domain_id integer;
ALTER TABLE Contact ALTER COLUMN contact_domain_id SET DEFAULT 0;
ALTER TABLE Kind ADD Column kind_domain_id integer;
ALTER TABLE Kind ALTER COLUMN kind_domain_id SET DEFAULT 0;
ALTER TABLE ContactFunction ADD Column contactfunction_domain_id integer;
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_domain_id SET DEFAULT 0;
ALTER TABLE LeadSource ADD Column leadsource_domain_id integer;
ALTER TABLE LeadSource ALTER COLUMN leadsource_domain_id SET DEFAULT 0;
ALTER TABLE Lead ADD Column lead_domain_id integer;
ALTER TABLE Lead ALTER COLUMN lead_domain_id SET DEFAULT 0;
ALTER TABLE ParentDeal ADD Column parentdeal_domain_id integer;
ALTER TABLE ParentDeal ALTER COLUMN parentdeal_domain_id SET DEFAULT 0;
ALTER TABLE Deal ADD Column deal_domain_id integer;
ALTER TABLE Deal ALTER COLUMN deal_domain_id SET DEFAULT 0;
ALTER TABLE DealStatus ADD Column dealstatus_domain_id integer;
ALTER TABLE DealStatus ALTER COLUMN dealstatus_domain_id  SET DEFAULT 0;
ALTER TABLE DealType ADD Column dealtype_domain_id integer;
ALTER TABLE DealType ALTER COLUMN dealtype_domain_id SET DEFAULT 0;
ALTER TABLE DealCompanyRole ADD Column dealcompanyrole_domain_id integer;
ALTER TABLE DealCompanyRole ALTER COLUMN dealcompanyrole_domain_id SET DEFAULT 0;
ALTER TABLE List ADD Column list_domain_id integer;
ALTER TABLE List ALTER COLUMN list_domain_id SET DEFAULT 0;
ALTER TABLE CalendarEvent ADD Column calendarevent_domain_id integer;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_domain_id SET DEFAULT 0;
ALTER TABLE EventEntity ADD Column evententity_domain_id integer;
ALTER TABLE EventEntity ALTER COLUMN evententity_domain_id SET DEFAULT 0;
ALTER TABLE CalendarException ADD Column calendarexception_domain_id integer;
ALTER TABLE CalendarException ALTER COLUMN calendarexception_domain_id SET DEFAULT 0;
ALTER TABLE CalendarCategory1 ADD Column calendarcategory1_domain_id integer;
ALTER TABLE CalendarCategory1 ALTER COLUMN calendarcategory1_domain_id SET DEFAULT 0;
ALTER TABLE Todo ADD Column todo_domain_id integer;
ALTER TABLE Todo ALTER Column todo_domain_id SET default 0;
ALTER TABLE Publication ADD Column publication_domain_id integer;
ALTER TABLE Publication ALTER Column publication_domain_id SET default 0;
ALTER TABLE PublicationType ADD Column publicationtype_domain_id integer;
ALTER TABLE PublicationType ALTER Column publicationtype_domain_id SET default 0;
ALTER TABLE Subscription ADD Column subscription_domain_id integer;
ALTER TABLE Subscription ALTER Column subscription_domain_id SET default 0;
ALTER TABLE SubscriptionReception ADD Column subscriptionreception_domain_id integer;
ALTER TABLE SubscriptionReception ALTER Column subscriptionreception_domain_id SET default 0;
ALTER TABLE Document ADD Column document_domain_id integer;
ALTER TABLE Document ALTER Column document_domain_id SET default 0;
ALTER TABLE DocumentMimeType ADD Column documentmimetype_domain_id integer default 0;
ALTER TABLE DocumentEntity ADD Column documententity_domain_id integer default 0;
ALTER TABLE Project ADD Column project_domain_id integer default 0;
ALTER TABLE ProjectTask ADD Column projecttask_domain_id integer default 0;
ALTER TABLE ProjectRefTask ADD Column projectreftask_domain_id integer default 0;
ALTER TABLE ProjectUser ADD Column projectuser_domain_id integer default 0;
ALTER TABLE ProjectStat ADD Column projectstat_domain_id integer default 0;
ALTER TABLE TimeTask ADD Column timetask_domain_id integer default 0;
ALTER TABLE TaskType ADD Column tasktype_domain_id integer default 0;
ALTER TABLE Contract ADD Column contract_domain_id integer default 0;
ALTER TABLE ContractType ADD Column contracttype_domain_id integer default 0;
ALTER TABLE ContractPriority ADD Column contractpriority_domain_id integer default 0;
ALTER TABLE ContractStatus ADD Column contractstatus_domain_id integer default 0;
ALTER TABLE Incident ADD Column incident_domain_id integer default 0;
ALTER TABLE IncidentPriority ADD Column incidentpriority_domain_id integer default 0;
ALTER TABLE IncidentStatus ADD Column incidentstatus_domain_id integer default 0;
ALTER TABLE Invoice ADD Column invoice_domain_id integer default 0;
ALTER TABLE InvoiceStatus ADD Column invoicestatus_domain_id integer default 0;
ALTER TABLE Payment ADD Column payment_domain_id integer default 0;
ALTER TABLE PaymentKind ADD Column paymentkind_domain_id integer default 0;
ALTER TABLE PaymentInvoice ADD Column paymentinvoice_domain_id integer default 0;
ALTER TABLE Account ADD Column account_domain_id integer default 0;
ALTER TABLE UGroup ADD Column group_domain_id integer default 0;
ALTER TABLE Import ADD Column import_domain_id integer default 0;
ALTER TABLE Resource ADD Column resource_domain_id integer default 0;
ALTER TABLE RGroup ADD Column rgroup_domain_id integer default 0;


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
  PRIMARY KEY (categorylink_category_id, categorylink_entity_id, categorylink_category, categorylink_entity)
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
  0,
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
  projectcv_project_id serial,
  projectcv_cv_id      integer,
  projectcv_role       varchar(128),
  PRIMARY KEY(projectcv_project_id, projectcv_cv_id)
);


----------------------------------------------------------------------------
-- Create DefaultOdtTemplate table
----------------------------------------------------------------------------

CREATE TABLE DefaultOdtTemplate (
  defaultodttemplate_id                  serial,
  defaultodttemplate_entity              varchar(32),
  defaultodttemplate_document_id         integer,
  defaultodttemplate_label               varchar(64) DEFAULT '',
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

ALTER TABLE Project ADD COLUMN project_reference_tech text;
ALTER TABLE Project ALTER COLUMN project_reference_tech SET DEFAULT '';
