-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.1 to 0.8.2                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Update Company tables
-------------------------------------------------------------------------------
-- Add column : company_nafcode_id
ALTER TABLE Company ADD column company_nafcode_id integer;

-- 
-- Table structure for table 'CompanyNafCode'
--
CREATE TABLE CompanyNafCode (
  companynafcode_id          serial,
  companynafcode_timeupdate  timestamp,
  companynafcode_timecreate  timestamp,
  companynafcode_userupdate  integer,
  companynafcode_usercreate  integer,
  companynafcode_code        varchar(4),
  companynafcode_label       varchar(128),
  PRIMARY KEY (companynafcode_id)
);


-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- Add column : list_private
ALTER TABLE List ADD column list_visibility integer;
ALTER TABLE List ALTER column list_visibility SET DEFAULT '0';


-------------------------------------------------------------------------------
-- Update Country table
-------------------------------------------------------------------------------
-- Change column : country_phone -> varchar(5) (bug#339)
 
ALTER TABLE Country ADD COLUMN temp_country_phone VARCHAR(5);
UPDATE Country SET temp_country_phone = country_phone;
ALTER TABLE Country DROP COLUMN country_phone;
ALTER TABLE Country RENAME COLUMN temp_country_phone TO country_phone;

UPDATE DisplayPref set display_fieldorder = 11 WHERE display_entity = 'list_contact' AND display_fieldname ='contact_email' AND
display_user_id = 0;
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'subscription_quantity', 10, 1);
