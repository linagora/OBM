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
UPDATE Company SET company_zipcode='' WHERE company_zipcode is null;
