-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.5.3 to 0.6.0	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Company Update
-------------------------------------------------------------------------------
-- change address1, address2, town, mail and web (to varchar(64))
ALTER table Company change column company_address1 company_address1 varchar(64);
ALTER table Company change column company_address2 company_address2 varchar(64);
ALTER table Company change column company_town company_town varchar(64);
ALTER table Company change column company_web company_web varchar(64);
ALTER table Company change column company_email company_email varchar(64);

-- Add new column : company_deal_total
ALTER table Company add column company_deal_total int(5) not null default 0 AFTER company_deal_number;

-- Add new column : company_activity_id
ALTER table Company add column company_activity_id int(8) AFTER company_type_id;

-- 
-- Table structure for table 'CompanyActivity'
--
CREATE TABLE CompanyActivity (
  companyactivity_id int(8) DEFAULT '0' NOT NULL auto_increment,
  companyactivity_timeupdate timestamp(14),
  companyactivity_timecreate timestamp(14),
  companyactivity_userupdate int(8),
  companyactivity_usercreate int(8),
  companyactivity_label varchar(64),
  PRIMARY KEY (companyactivity_id)
);
