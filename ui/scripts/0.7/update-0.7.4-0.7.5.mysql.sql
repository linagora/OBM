-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.4 to 0.7.5                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'DataSource'
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
-- Table structure for the table  'Country'
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


-------------------------------------------------------------------------------
-- Update structure for table 'Company'
-------------------------------------------------------------------------------
-- Add new column : company_datasource_id
ALTER table Company add column company_datasource_id int(8) DEFAULT NULL after company_usercreate;

-- Add new column : company_aka (Also Known As)
ALTER table Company add column company_aka varchar(255) after company_name;

-- Add new column : company_sound (Metaphone version of name)
ALTER table Company add column company_sound varchar(24) after company_aka;


-------------------------------------------------------------------------------
-- Update structure for table 'Contact'
-------------------------------------------------------------------------------
-- Add new column : contact_datasource_id
ALTER table Contact add column contact_datasource_id int(8) DEFAULT NULL after contact_usercreate;
