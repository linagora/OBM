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

--
-- Update All tables to include Domain info
--
ALTER TABLE UserObm ADD Column userobm_domain_id int(8) default 0 after userobm_id;
UPDATE UserObm SET userobm_domain_id=0;
ALTER TABLE DataSource ADD Column datasource_domain_id int(8) default 0 after datasource_id;
ALTER TABLE Country ADD Column country_domain_id int(8) default 0 first;
ALTER TABLE Region ADD Column region_domain_id int(8) default 0 after region_id;
ALTER TABLE CompanyType ADD Column companytype_domain_id int(8) default 0 after companytype_id;
ALTER TABLE CompanyActivity ADD Column companyactivity_domain_id int(8) default 0 after companyactivity_id;
ALTER TABLE CompanyNafCode ADD Column companynafcode_domain_id int(8) default 0 after companynafcode_id;
ALTER TABLE Company ADD Column company_domain_id int(8) default 0 after company_id;
ALTER TABLE Contact ADD Column contact_domain_id int(8) default 0 after contact_id;
ALTER TABLE Kind ADD Column kind_domain_id int(8) default 0 after kind_id;
ALTER TABLE ContactFunction ADD Column contactfunction_domain_id int(8) default 0 after contactfunction_id;
ALTER TABLE LeadSource ADD Column leadsource_domain_id int(8) default 0 after leadsource_id;
ALTER TABLE Lead ADD Column lead_domain_id int(8) default 0 after lead_id;
ALTER TABLE ParentDeal ADD Column parentdeal_domain_id int(8) default 0 after parentdeal_id;
ALTER TABLE Deal ADD Column deal_domain_id int(8) default 0 after deal_id;
ALTER TABLE DealStatus ADD Column dealstatus_domain_id int(8) default 0 after dealstatus_id;
ALTER TABLE DealType ADD Column dealtype_domain_id int(8) default 0 after dealtype_id;
ALTER TABLE DealCompanyRole ADD Column dealcompanyrole_domain_id int(8) default 0 after dealcompanyrole_id;
ALTER TABLE List ADD Column list_domain_id int(8) default 0 after list_id;
ALTER TABLE CalendarEvent ADD Column calendarevent_domain_id int(8) default 0 after calendarevent_id;
ALTER TABLE EventEntity ADD Column evententity_domain_id int(8) default 0 first;
ALTER TABLE CalendarException ADD Column calendarexception_domain_id int(8) default 0 first;
ALTER TABLE CalendarCategory1 ADD Column calendarcategory1_domain_id int(8) default 0 after calendarcategory1_id;
ALTER TABLE EntityRight ADD Column entityright_domain_id int(8) default 0 first;
ALTER TABLE Todo ADD Column todo_domain_id int(8) default 0 after todo_id;
ALTER TABLE Publication ADD Column publication_domain_id int(8) default 0 after publication_id;
ALTER TABLE PublicationType ADD Column publicationtype_domain_id int(8) default 0 after publicationtype_id;
ALTER TABLE Subscription ADD Column subscription_domain_id int(8) default 0 after subscription_id;
ALTER TABLE SubscriptionReception ADD Column subscriptionreception_domain_id int(8) default 0 after subscriptionreception_id;
ALTER TABLE Document ADD Column document_domain_id int(8) default 0 after document_id;
ALTER TABLE DocumentMimeType ADD Column documentmimetype_domain_id int(8) default 0 after documentmimetype_id;
ALTER TABLE DocumentEntity ADD Column documententity_domain_id int(8) default 0 first;
ALTER TABLE Project ADD Column project_domain_id int(8) default 0 after project_id;
ALTER TABLE ProjectTask ADD Column projecttask_domain_id int(8) default 0 after projecttask_id;
ALTER TABLE ProjectRefTask ADD Column projectreftask_domain_id int(8) default 0 after projectreftask_id;
ALTER TABLE ProjectUser ADD Column projectuser_domain_id int(8) default 0 after projectuser_id;
ALTER TABLE ProjectStat ADD Column projectstat_domain_id int(8) default 0 first;
ALTER TABLE TimeTask ADD Column timetask_domain_id int(8) default 0 after timetask_id;
ALTER TABLE TaskType ADD Column tasktype_domain_id int(8) default 0 after tasktype_id;
ALTER TABLE Contract ADD Column contract_domain_id int(8) default 0 after contract_id;
ALTER TABLE ContractType ADD Column contracttype_domain_id int(8) default 0 after contracttype_id;
ALTER TABLE ContractPriority ADD Column contractpriority_domain_id int(8) default 0 after contractpriority_id;
ALTER TABLE ContractStatus ADD Column contractstatus_domain_id int(8) default 0 after contractstatus_id;
ALTER TABLE Incident ADD Column incident_domain_id int(8) default 0 after incident_id;
ALTER TABLE IncidentPriority ADD Column incidentpriority_domain_id int(8) default 0 after incidentpriority_id;
ALTER TABLE IncidentStatus ADD Column incidentstatus_domain_id int(8) default 0 after incidentstatus_id;
ALTER TABLE Invoice ADD Column invoice_domain_id int(8) default 0 after invoice_id;
ALTER TABLE InvoiceStatus ADD Column invoicestatus_domain_id int(8) default 0 after invoicestatus_id;
ALTER TABLE Payment ADD Column payment_domain_id int(8) default 0 after payment_id;
ALTER TABLE PaymentKind ADD Column paymentkind_domain_id int(8) default 0 after paymentkind_id;
ALTER TABLE PaymentInvoice ADD Column paymentinvoice_domain_id int(8) default 0 first;
ALTER TABLE Account ADD Column account_domain_id int(8) default 0 after account_id;
ALTER TABLE UGroup ADD Column group_domain_id int(8) default 0 after group_id;
ALTER TABLE Import ADD Column import_domain_id int(8) default 0 after import_id;
ALTER TABLE Resource ADD Column resource_domain_id int(8) default 0 after resource_id;
ALTER TABLE RGroup ADD Column rgroup_domain_id int(8) default 0 after rgroup_id;


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
