-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.1 to 0.8.2                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Update Company tables
-------------------------------------------------------------------------------
-- Add column : company_nafcode_id
ALTER TABLE Company ADD column company_nafcode_id int(8) after company_activity_id;

-- 
-- Table structure for table 'CompanyNafCode'
--
CREATE TABLE CompanyNafCode (
  companynafcode_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  companynafcode_timeupdate  timestamp(14),
  companynafcode_timecreate  timestamp(14),
  companynafcode_userupdate  int(8),
  companynafcode_usercreate  int(8),
  companynafcode_code        varchar(4),
  companynafcode_label       varchar(128),
  PRIMARY KEY (companynafcode_id)
);

-- Add column : comapny_nafcode_id
ALTER TABLE Company ADD column company_nafcode int(8) after company_activity_id;


-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- Add column : list_private
ALTER TABLE List ADD column list_visibility int(2) DEFAULT '0' after list_usercreate;


-------------------------------------------------------------------------------
-- Update Country table
-------------------------------------------------------------------------------
-- Change column : country_phone -> varchar(5) (bug#339)
ALTER TABLE Country CHANGE column country_phone country_phone VARCHAR(5);

UPDATE DisplayPref set display_fieldorder = 11 WHERE display_entity = 'list_contact' AND display_fieldname ='contact_email' AND
display_user_id = 0;
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'subscription_quantity', 10, 1);
