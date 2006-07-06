-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 1.1 to 1.2                              //
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
ALTER TABLE List CHANGE list_name list_name varchar(64);


-------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- contract_label length
ALTER TABLE Contract CHANGE contract_label contract_label varchar(128);


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

ALTER TABLE Contact CHANGE COLUMN contact_country_iso3166 contact_country_iso3166 char(2) DEFAULT '0';
 
UPDATE Company SET company_zipcode='' WHERE company_zipcode is null;
UPDATE Company SET company_country_iso3166=0 WHERE company_country_iso3166='' OR company_country_iso3166 is null;
ALTER TABLE Company CHANGE COLUMN company_country_iso3166 company_country_iso3166 char(2) DEFAULT '0';


-------------------------------------------------------------------------------
-- Re-create Payment table
-------------------------------------------------------------------------------
DROP TABLE IF EXISTS Payment;

CREATE TABLE Payment (
  payment_id              int(8) auto_increment,
  payment_timeupdate      timestamp(14),
  payment_timecreate      timestamp(14),
  payment_userupdate      int(8),
  payment_usercreate      int(8),
  payment_company_id      int(8) NOT NULL,
  payment_account_id      int(8),
  payment_paymentkind_id  int(8) NOT NULL,
  payment_amount          double(10,2) DEFAULT '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL DEFAULT '+',
  payment_number          varchar(24) DEFAULT '',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


-------------------------------------------------------------------------------
-- Update PaymentKind table
-------------------------------------------------------------------------------
ALTER TABLE PaymentKind ADD COLUMN paymentkind_label varchar(40) NOT NULL DEFAULT '';
UPDATE PaymentKind SET paymentkind_label = paymentkind_longlabel;
ALTER TABLE PaymentKind DROP COLUMN paymentkind_longlabel;


-------------------------------------------------------------------------------
-- Drop deprecated tables
-------------------------------------------------------------------------------
DROP TABLE IF EXISTS EntryTemp;
DROP TABLE IF EXISTS PaymentTemp;


-------------------------------------------------------------------------------
-- Update Deal table
-------------------------------------------------------------------------------
ALTER TABLE Deal ADD COLUMN deal_source_id int(8) DEFAULT 0 AFTER deal_technicalmanager_id;
ALTER TABLE Deal ADD COLUMN deal_source varchar(64) AFTER deal_source_id;
ALTER TABLE Deal ADD COLUMN deal_dateend date AFTER deal_dateexpected;
ALTER TABLE Deal ADD COLUMN deal_commission decimal(5,2) DEFAULT 0 AFTER deal_amount;


-------------------------------------------------------------------------------
-- DealCompany tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DealCompanyRole'
--
CREATE TABLE DealCompanyRole (
  dealcompanyrole_id          int(8) auto_increment,
  dealcompanyrole_timeupdate  timestamp(14),
  dealcompanyrole_timecreate  timestamp(14),
  dealcompanyrole_userupdate  int(8) default NULL,
  dealcompanyrole_usercreate  int(8) default NULL,
  dealcompanyrole_code        varchar(10) default '',
  dealcompanyrole_label       varchar(64) NOT NULL default '',
  PRIMARY KEY (dealcompanyrole_id)
);


--
-- Table structure for the table 'DealCompany'
--
CREATE TABLE DealCompany (
  dealcompany_id          int(8) auto_increment,
  dealcompany_timeupdate  timestamp(14),
  dealcompany_timecreate  timestamp(14),
  dealcompany_userupdate  int(8) default NULL,
  dealcompany_usercreate  int(8) default NULL,
  dealcompany_deal_id     int(8) NOT NULL default 0,
  dealcompany_company_id  int(8) NOT NULL default 0,
  dealcompany_role_id     int(8) NOT NULL default 0,
  PRIMARY KEY (dealcompany_id),
  INDEX dealcompany_idx_deal (dealcompany_deal_id)
);


-------------------------------------------------------------------------------
-- Lead module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'LeadSource'
--
CREATE TABLE LeadSource (
  leadsource_id          int(8) auto_increment,
  leadsource_timeupdate  timestamp(14),
  leadsource_timecreate  timestamp(14),
  leadsource_userupdate  int(8),
  leadsource_usercreate  int(8),
  leadsource_code        varchar(10) default '',
  leadsource_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (leadsource_id)
);


--
-- Table structure for the table 'Lead'
--
CREATE TABLE Lead (
  lead_id          int(8) auto_increment,
  lead_timeupdate  timestamp(14),
  lead_timecreate  timestamp(14),
  lead_userupdate  int(8),
  lead_usercreate  int(8),
  lead_source_id   int(8),
  lead_manager_id  int(8),
  lead_company_id  int(8) NOT NULL DEFAULT 0,
  lead_privacy     int(2) NOT NULL DEFAULT 0,
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
ALTER TABLE Project ADD COLUMN project_shortname varchar(10) NOT NULL DEFAULT '';


--
-- Table structure for table 'ProjectReferenceTask'
--
CREATE TABLE ProjectRefTask (
  projectreftask_id         int(8) auto_increment,
  projectreftask_timeupdate  timestamp(14),
  projectreftask_timecreate  timestamp(14),
  projectreftask_userupdate  int(8) default NULL,
  projectreftask_usercreate  int(8) default NULL,
  projectreftask_code        varchar(10) default '',
  projectreftask_label      varchar(128) default NULL,
  PRIMARY KEY (projectreftask_id)
);


-------------------------------------------------------------------------------
-- Incident module tables
-------------------------------------------------------------------------------
ALTER TABLE Incident ADD COLUMN incident_category2_id int(8) DEFAULT 0 AFTER incident_category1_id;


--
-- New table 'IncidentCategory2'
--
CREATE TABLE IncidentCategory2 (
  incidentcategory2_id          int(8) auto_increment,
  incidentcategory2_timeupdate  timestamp(14),
  incidentcategory2_timecreate  timestamp(14),
  incidentcategory2_userupdate  int(8) default NULL,
  incidentcategory2_usercreate  int(8) default NULL,
  incidentcategory2_code        varchar(10) default '',
  incidentcategory2_label       varchar(32) default NULL,
PRIMARY KEY (incidentcategory2_id)
);


-------------------------------------------------------------------------------
-- New Region table
-------------------------------------------------------------------------------
-- 
-- Table structure for table 'Region'
--
CREATE TABLE Region (
  region_id          int(8) auto_increment,
  region_timeupdate  timestamp(14),
  region_timecreate  timestamp(14),
  region_userupdate  int(8),
  region_usercreate  int(8),
  region_code        varchar(10) default '', 
  region_label       varchar(64),
  PRIMARY KEY (region_id)
);


ALTER TABLE Deal ADD COLUMN deal_region_id int(8) NOT NULL DEFAULT 0 AFTER deal_type_id;
