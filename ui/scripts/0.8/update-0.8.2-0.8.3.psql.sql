-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.2 to 0.8.3                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Update CompanyNafCode table
-------------------------------------------------------------------------------
-- Add column : companynafcode_title
ALTER TABLE CompanyNafCode ADD column companynafcode_title integer;
ALTER TABLE CompanyNafCode ALTER column companynafcode_title SET NOT NULL;
ALTER TABLE CompanyNafCode ALTER column companynafcode_title SET DEFAULT '0';

-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
-- Add column : contact_service
ALTER TABLE Contact ADD column contact_service varchar(64);

-- Add column : contact_email2
ALTER TABLE Contact ADD column contact_email2 varchar(128);

-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- Add column : list_mailing_ok
ALTER TABLE List ADD column list_mailing_ok integer;

-------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- Add column : contract_archive
ALTER TABLE Contact ADD column contract_archive integer; -------------------------------------------------------------------------------
--
-- Table structure for table 'DealCategory'
--
CREATE TABLE DealCategory (
  dealcategory_id          serial,
  dealcategory_timeupdate  timestamp,
  dealcategory_timecreate  timestamp,
  dealcategory_userupdate  integer default 0,
  dealcategory_usercreate  integer default 0,
  dealcategory_code        integer default 0,
  dealcategory_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (dealcategory_id)
);

-- Table structure for table 'DealCategoryLink'
--
CREATE TABLE DealCategoryLink (
  dealcategorylink_category_id  integer NOT NULL default 0,
  dealcategorylink_deal_id   integer NOT NULL default 0,
  PRIMARY KEY (dealcategorylink_category_id,dealcategorylink_deal_id)
);
