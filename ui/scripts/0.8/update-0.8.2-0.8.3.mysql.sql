-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.2 to 0.8.3                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Update CompanyNafCode table
-------------------------------------------------------------------------------
-- Add column : companynafcode_title
ALTER TABLE CompanyNafCode ADD column companynafcode_title int(1) NOT NULL DEFAULT '0' after companynafcode_usercreate;

-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
-- Add column : contact_service
ALTER TABLE Contact ADD column contact_service varchar(64) after contact_firstname;

-- Add column : contact_email2
ALTER TABLE Contact ADD column contact_email2 varchar(128) after contact_email;

-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- Add column : list_mailing_ok
ALTER TABLE List ADD column list_mailing_ok int(1) after list_email;

------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- Add column : contract_archive
ALTER TABLE Contract ADD column contract_archive int(1) after contract_marketmanager_id;

-------------------------------------------------------------------------------
-- New table DealCategory
-------------------------------------------------------------------------------
--
-- Table structure for table 'DealCategory'
--
CREATE TABLE DealCategory (
  dealcategory_id          int(8) NOT NULL auto_increment,
  dealcategory_timeupdate  timestamp(14),
  dealcategory_timecreate  timestamp(14),
  dealcategory_userupdate  int(8) default '0',
  dealcategory_usercreate  int(8) default '0',
  dealcategory_code        int(8) default '0',
  dealcategory_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (dealcategory_id)
);

--
-- Table structure for table 'DealCategoryLink'
--
CREATE TABLE DealCategoryLink (
  dealcategorylink_category_id  int(8) NOT NULL default '0',
  dealcategorylink_deal_id      int(8) NOT NULL default '0',
  PRIMARY KEY (dealcategorylink_category_id,dealcategorylink_deal_id)
);
