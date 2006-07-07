-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 1.1 to 1.2                         //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.2' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- list_name length
ALTER TABLE List ADD COLUMN temp_name varchar(64);
UPDATE List SET temp_name = list_name;
ALTER TABLE List DROP COLUMN list_name;
ALTER TABLE List RENAME COLUMN temp_name TO list_name;


-------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- contract_label length
ALTER TABLE Contract ADD COLUMN temp_label varchar(128);
UPDATE Contract SET temp_label = contract_label;
ALTER TABLE Contract DROP COLUMN contract_label;
ALTER TABLE Contract RENAME COLUMN temp_label TO contract_label;


-------------------------------------------------------------------------------
-- Clean some tables content
-------------------------------------------------------------------------------
UPDATE ProjectUser SET projectuser_manager=0 WHERE projectuser_manager is null;
UPDATE Contact SET contact_address1='' WHERE contact_address1 is null;
UPDATE Contact SET contact_address2='' WHERE contact_address2 is null;
UPDATE Contact SET contact_address3='' WHERE contact_address3 is null;
UPDATE Contact SET contact_zipcode='' WHERE contact_zipcode is null;
UPDATE Contact SET contact_town='' WHERE contact_town is null;
UPDATE Contact SET contact_expresspostal='' WHERE contact_expresspostal is null;
UPDATE Contact SET contact_country_iso3166=0 WHERE contact_country_iso3166='' OR contact_country_iso3166 is null;

ALTER TABLE Contact ALTER COLUMN contact_country_iso3166 SET DEFAULT '0';
 
UPDATE Company SET company_zipcode='' WHERE company_zipcode is null;
UPDATE Company SET company_country_iso3166=0 WHERE company_country_iso3166='' OR company_country_iso3166 is null;
ALTER TABLE Company ALTER COLUMN company_country_iso3166 SET DEFAULT '0';


-------------------------------------------------------------------------------
-- Clean UserObm table
-------------------------------------------------------------------------------
-- set lastname and firstname to default '' instead of null (cause of concat)
UPDATE UserObm SET userobm_lastname = '' where userobm_lastname is null;
ALTER TABLE UserObm ALTER COLUMN userobm_lastname SET DEFAULT '';
UPDATE UserObm SET userobm_firstname = '' where userobm_firstname is null;
ALTER TABLE UserObm ALTER COLUMN userobm_firstname SET DEFAULT '';


-------------------------------------------------------------------------------
-- Re-create Payment table
-------------------------------------------------------------------------------
DROP TABLE Payment;

CREATE TABLE Payment (
  payment_id              serial,
  payment_timeupdate      timestamp,
  payment_timecreate      timestamp,
  payment_userupdate      integer,
  payment_usercreate      integer,
  payment_company_id      integer NOT NULL,
  payment_account_id      integer,
  payment_paymentkind_id  integer NOT NULL,
  payment_amount          decimal(10,2) DEFAULT '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL DEFAULT '+',
  payment_number          varchar(24) NOT NULL DEFAULT '',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


-------------------------------------------------------------------------------
-- Update PaymentKind table
-------------------------------------------------------------------------------
ALTER TABLE PaymentKind ADD COLUMN paymentkind_label varchar(40);
ALTER TABLE PaymentKind ALTER COLUMN paymentkind_label SET DEFAULT '';
ALTER TABLE PaymentKind ALTER COLUMN paymentkind_label SET NOT NULL;
UPDATE PaymentKind SET paymentkind_label = paymentkind_longlabel;
ALTER TABLE PaymentKind DROP COLUMN paymentkind_longlabel;


-------------------------------------------------------------------------------
-- Drop deprecated tables
-------------------------------------------------------------------------------
DROP TABLE EntryTemp;
DROP TABLE PaymentTemp;


-------------------------------------------------------------------------------
-- Update Deal table
-------------------------------------------------------------------------------
ALTER TABLE Deal ADD COLUMN deal_source_id integer;
ALTER TABLE Deal ALTER COLUMN deal_source_id SET DEFAULT 0;
ALTER TABLE Deal ADD COLUMN deal_source varchar(64);
ALTER TABLE Deal ADD COLUMN deal_dateend date;
ALTER TABLE Deal ADD COLUMN deal_commission decimal(4,2);
ALTER TABLE Deal ALTER COLUMN deal_commission SET DEFAULT 0;


-------------------------------------------------------------------------------
-- DealCompany tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DealCompanyRole'
--
CREATE TABLE DealCompanyRole (
  dealcompanyrole_id          serial,
  dealcompanyrole_timeupdate  timestamp,
  dealcompanyrole_timecreate  timestamp,
  dealcompanyrole_userupdate  integer default 0,
  dealcompanyrole_usercreate  integer default 0,
  dealcompanyrole_code        varchar(10) default '',
  dealcompanyrole_label       varchar(64) NOT NULL default '',
  PRIMARY KEY (dealcompanyrole_id)
);


--
-- Table structure for the table 'DealCompany'
--
CREATE TABLE DealCompany (
  dealcompany_id          serial,
  dealcompany_timeupdate  timestamp,
  dealcompany_timecreate  timestamp,
  dealcompany_userupdate  integer default 0,
  dealcompany_usercreate  integer default 0,
  dealcompany_deal_id     integer NOT NULL default 0,
  dealcompany_company_id  integer NOT NULL default 0,
  dealcompany_role_id     integer NOT NULL default 0,
  PRIMARY KEY (dealcompany_id),
  INDEX dealcompany_idx_deal (dealcompany_deal_id)
);
CREATE INDEX dealcompany_idx_deal ON DealCompany (dealcompany_deal_id);


-------------------------------------------------------------------------------
-- Lead module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'LeadSource'
--
CREATE TABLE LeadSource (
  leadsource_id          serial,
  leadsource_timeupdate  timestamp,
  leadsource_timecreate  timestamp,
  leadsource_userupdate  integer default 0,
  leadsource_usercreate  integer default 0,
  leadsource_code        varchar(10) default '',
  leadsource_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (leadsource_id)
);


--
-- Table structure for the table 'Lead'
--
CREATE TABLE Lead (
  lead_id          serial,
  lead_timeupdate  timestamp,
  lead_timecreate  timestamp,
  lead_userupdate  integer default 0,
  lead_usercreate  integer default 0,
  lead_source_id   integer default 0,
  lead_manager_id  integer default 0,
  lead_company_id  integer NOT NULL DEFAULT 0,
  lead_privacy     integer DEFAULT 0,
  lead_name        varchar(64),
  lead_date        date,
  lead_datealarm   date,
  lead_archive     char(1) DEFAULT '0',
  lead_todo        varchar(128),
  lead_comment     text,
  PRIMARY KEY (lead_id)
);

-------------------------------------------------------------------------------
-- Project module tables
-------------------------------------------------------------------------------
ALTER TABLE Project ADD COLUMN project_shortname varchar(10) SET DEFAULT '';


--
-- Table structure for table 'ProjectReferenceTask'
--
CREATE TABLE ProjectRefTask (
  projectreftask_id          serial,
  projectreftask_timeupdate  timestamp,
  projectreftask_timecreate  timestamp,
  projectreftask_userupdate  integer default NULL,
  projectreftask_usercreate  integer default NULL,
  projectreftask_tasktype_id        integer,
  projectreftask_label       varchar(128) default NULL,
  PRIMARY KEY (projectreftask_id)
);


-------------------------------------------------------------------------------
-- Incident module tables
-------------------------------------------------------------------------------
ALTER TABLE Incident ADD COLUMN incident_category2_id integer;
ALTER TABLE Incident ALTER COLUMN incident_category2_id SET DEFAULT 0;


--
-- New table 'IncidentCategory2'
--
CREATE TABLE IncidentCategory2 (
  incidentcategory2_id          serial,
  incidentcategory2_timeupdate  timestamp,
  incidentcategory2_timecreate  timestamp,
  incidentcategory2_userupdate  integer DEFAULT NULL,
  incidentcategory2_usercreate  integer DEFAULT NULL,
  incidentcategory2_code        varchar(10) default '',
  incidentcategory2_label       varchar(32) DEFAULT NULL,
  PRIMARY KEY (incidentcategory2_id)
);


-------------------------------------------------------------------------------
-- New Region table
-------------------------------------------------------------------------------
-- 
-- Table structure for table 'Region'
--
CREATE TABLE Region (
  region_id          serial,
  region_timeupdate  TIMESTAMP,
  region_timecreate  TIMESTAMP,
  region_userupdate  integer,
  region_usercreate  integer,
  region_code        varchar(10) NOT NULL default '',
  region_label       varchar(64),
  PRIMARY KEY (region_id)
);


ALTER TABLE Deal ADD COLUMN deal_region_id integer;
ALTER TABLE Deal ALTER COLUMN deal_region_id SET DEFAULT 0;
UPDATE Deal SET deal_regionid=0;
ALTER TABLE Deal ALTER COLUMN deal_region_id SET NOT NULL;
