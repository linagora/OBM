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
ALTER TABLE Contact ADD column contract_archive integer;

-------------------------------------------------------------------------------
