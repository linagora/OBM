-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.8 to 0.8.9                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='0.8.9' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Update Company and Contact countries values
-------------------------------------------------------------------------------
update Contact set contact_country_iso3166='' where contact_country_iso3166='0';

update Company set company_country_iso3166='' where company_country_iso3166='0';

-------------------------------------------------------------------------------
-- Update Invoice table
-------------------------------------------------------------------------------
-- Add invoice_expiration_date
ALTER TABLE Invoice ADD COLUMN invoice_expiration_date date AFTER invoice_date;


-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- Add list_contact_archive
ALTER TABLE List ADD COLUMN list_contact_archive int(1) DEFAULT 0 after list_mailing_ok;

-- Add list_info_publication
ALTER TABLE List ADD COLUMN list_info_publication int(1) DEFAULT 0 after list_contact_archive;

