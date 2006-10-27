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
  domain_id             serial,
  domain_timeupdate     timestamp,
  domain_timecreate     timestamp,
  domain_usercreate     integer,
  domain_userupdate     integer,
  domain_label          varchar(32) NOT NULL,
  domain_description    varchar(255),
  domain_domain_name    varchar(128),
  domain_alias          text,
  PRIMARY KEY (domain_id)
);

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
ALTER TABLE CompanyCategory1 ADD Column companycategory1_domain_id integer;
ALTER TABLE CompanyCategory1 ALTER COLUMN companycategory1_domain_id SET DEFAULT 0;
ALTER TABLE Contact ADD Column contact_domain_id integer;
ALTER TABLE Contact ALTER COLUMN contact_domain_id SET DEFAULT 0;
ALTER TABLE Kind ADD Column kind_domain_id integer;
ALTER TABLE Kind ALTER COLUMN kind_domain_id SET DEFAULT 0;
ALTER TABLE ContactFunction ADD Column contactfunction_domain_id integer;
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_domain_id SET DEFAULT 0;
ALTER TABLE ContactCategory1 ADD Column contactcategory1_domain_id integer;
ALTER TABLE ContactCategory1 ALTER COLUMN contactcategory1_domain_id SET DEFAULT 0;
ALTER TABLE ContactCategory2 ADD Column contactcategory2_domain_id integer;
ALTER TABLE ContactCategory2 ALTER COLUMN contactcategory2_domain_id SET DEFAULT 0;
ALTER TABLE ContactCategory3 ADD Column contactcategory3_domain_id integer;
ALTER TABLE ContactCategory3 ALTER COLUMN contactcategory3_domain_id SET DEFAULT 0;
ALTER TABLE ContactCategory4 ADD Column contactcategory4_domain_id integer;
ALTER TABLE ContactCategory4 ALTER COLUMN contactcategory4_domain_id SET DEFAULT 0;
ALTER TABLE ContactCategory5 ADD Column contactcategory5_domain_id integer;
ALTER TABLE ContactCategory5 ALTER COLUMN contactcategory5_domain_id SET DEFAULT 0;
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
ALTER TABLE DealCategory1 ADD Column dealcategory1_domain_id integer;
ALTER TABLE DealCategory1 ALTER COLUMN dealcategory1_domain_id SET DEFAULT 0;
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
ALTER TABLE EntityRight ADD Column entityright_domain_id integer;
ALTER TABLE EntityRight ALTER COLUMN entityright_domain_id SET DEFAULT 0;
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
ALTER TABLE DocumentCategory1 ADD Column documentcategory1_domain_id integer default 0;
ALTER TABLE DocumentCategory2 ADD Column documentcategory2_domain_id integer default 0;
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
ALTER TABLE IncidentCategory1 ADD Column incidentcategory1_domain_id integer default 0;
ALTER TABLE IncidentCategory2 ADD Column incidentcategory2_domain_id integer default 0;
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
  category_domain_id   integer,
  category_timeupdate  TIMESTAMP,
  category_timecreate  TIMESTAMP,
  category_userupdate  integer,
  category_usercreate  integer,
  category_category    varchar(24) NOT NULL default '',
  category_code        varchar(10) NOT NULL default '',
  category_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (category_id)
);


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
CREATE INDEX cat_idx_ent ON CategoryLink (categorylink_entity_id);


---------------------------------------------------------------------------
-- Update UserObm table
---------------------------------------------------------------------------
ALTER TABLE UserObm ADD COLUMN userobm_education varchar(255);
ALTER TABLE UserObm ALTER COLUMN userobm_education SET DEFAULT '';


---------------------------------------------------------------------------
--Update Project table
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
--Create CV table
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
--Create ProjectCV table
----------------------------------------------------------------------------
CREATE TABLE ProjectCV (
  projectcv_project_id serial,
  projectcv_cv_id      integer,
  projectcv_role       varchar(128),
  PRIMARY KEY(projectcv_project_id, projectcv_cv_id)
);


----------------------------------------------------------------------------
--Create DefaultOdtTemplate table
----------------------------------------------------------------------------

CREATE TABLE DefaultOdtTemplate (
  defaultodttemplate_id                  serial,
  defaultodttemplate_entity              varchar(32),
  defaultodttemplate_document_id         integer,
  defaultodttemplate_label               varchar(64) DEFAULT '',
  PRIMARY KEY(defaultodttemplate_id)
);

