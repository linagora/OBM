-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.4 to 0.7.5                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DataSource'
--
CREATE TABLE DataSource (
  datasource_id int(8) DEFAULT '0' NOT NULL auto_increment,
  datasource_timeupdate timestamp(14),
  datasource_timecreate timestamp(14),
  datasource_userupdate int(8),
  datasource_usercreate int(8),
  datasource_name varchar(64),
  PRIMARY KEY (datasource_id)
);


--
-- Table structure for the table 'Country'
--
CREATE TABLE Country (
  country_id int(8) DEFAULT '0' NOT NULL auto_increment,
  country_timeupdate timestamp(14),
  country_timecreate timestamp(14),
  country_userupdate int(8),
  country_usercreate int(8),
  country_iso3166 char(2),
  country_name varchar(64) UNIQUE,
  country_lang char(2),
  country_phone varchar(4),
  PRIMARY KEY (country_id)
);


--
-- Table structure for the table 'Function'
--
CREATE TABLE Function (
  function_id int(8) DEFAULT '0' NOT NULL auto_increment,
  function_timeupdate timestamp(14),
  function_timecreate timestamp(14),
  function_userupdate int(8),
  function_usercreate int(8),
  function_label varchar(64),
  PRIMARY KEY (function_id)
);


--
-- Table structure for table 'CompanyCategory'
--
CREATE TABLE CompanyCategory (
  companycategory_id int(8) NOT NULL auto_increment,
  companycategory_timeupdate timestamp(14) NOT NULL,
  companycategory_timecreate timestamp(14) NOT NULL,
  companycategory_userupdate int(8) NOT NULL default '0',
  companycategory_usercreate int(8) NOT NULL default '0',
  companycategory_code varchar(10) NOT NULL default '',
  companycategory_label varchar(100) NOT NULL default '',
  PRIMARY KEY  (companycategory_id)
);


--
-- Table structure for table 'CompanyCategoryLink'
--
CREATE TABLE CompanyCategoryLink (
  companycategorylink_categoryid int(8) NOT NULL default '0',
  companycategorylink_companyid int(8) NOT NULL default '0',
  PRIMARY KEY  (companycategorylink_categoryid,companycategorylink_companyid)
);


-------------------------------------------------------------------------------
-- Update structure for table 'Company'
-------------------------------------------------------------------------------
-- Add new column : company_datasource_id
ALTER table Company add column company_datasource_id int(8) DEFAULT NULL after company_usercreate;

-- Add new column : company_aka (Also Known As)
ALTER table Company add column company_aka varchar(255) after company_name;

-- Add new column : company_sound (Metaphone version of name)
ALTER table Company add column company_sound varchar(24) after company_aka;

-- Drop column : company_country
ALTER table Company drop column company_country;

-- Add new column : company_country_id
ALTER table Company add column company_country_id int(8) after company_expresspostal;


-------------------------------------------------------------------------------
-- Update structure for table 'Contact'
-------------------------------------------------------------------------------
-- Add new column : contact_datasource_id
ALTER table Contact add column contact_datasource_id int(8) DEFAULT NULL after contact_usercreate;

-- Drop column : contact_country
ALTER table Contact drop column contact_country;

-- Add new column : contact_country_id
ALTER table Contact add column contact_country_id int(8) after contact_expresspostal;

-- Add new column : contact_marketingmanager_id
ALTER table Contact add column contact_marketingmanager_id int(8) after contact_kind_id;

-- Change column : contact_function to contact_title
ALTER table Contact change column contact_function contact_title varchar(64);

-- Add new column : contact_function_id
ALTER table Contact add column contact_function_id int(8) after contact_country_id;


-------------------------------------------------------------------------------
-- Update structure for table 'Kind'
-------------------------------------------------------------------------------
-- Add new column : kind_lang
ALTER table Kind add column kind_lang char(2) after kind_label;

-- Change column : contact_label to contact_header
ALTER table Kind change column kind_label kind_header varchar(64);


-------------------------------------------------------------------------------
-- Update structure for table 'ActiveUserObm'
-------------------------------------------------------------------------------
-- Change column : activeuserobm_timeupdate to timestamp
ALTER table ActiveUserObm change column activeuserobm_timeupdate activeuserobm_timeupdate timestamp(14);

-- Change column : activeuserobm_timecreate to timestamp
ALTER table ActiveUserObm change column activeuserobm_timecreate activeuserobm_timecreate timestamp(14);

-------------------------------------------------------------------------------
-- Update structure for table 'UserObm_SessionLog'
-------------------------------------------------------------------------------
-- Change column : userobm_sessionlog_timeupdate to timestamp
ALTER table UserObm_SessionLog change column userobm_sessionlog_timeupdate userobm_sessionlog_timeupdate timestamp(14);

-- Change column : userobm_sessionlog_timecreate to timestamp
ALTER table UserObm_SessionLog change column userobm_sessionlog_timecreate userobm_sessionlog_timecreate timestamp(14);


-------------------------------------------------------------------------------
-- Update some content in ProjectUser (bug#290)
-------------------------------------------------------------------------------
UPDATE ProjectUser set projectuser_projectedtime=0, projectuser_missingtime=0 where projectuser_projecttask_id is NULL;