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
