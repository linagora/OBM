-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.7 to 0.8.8                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='0.8.8' where obminfo_name='db_version';

-------------------------------------------------------------------------------
-- Update Express postal fields size
-------------------------------------------------------------------------------
-- Company Table
ALTER TABLE Company CHANGE company_expresspostal company_expresspostal varchar(16) null default '';

-- Contact Table
ALTER TABLE Contact CHANGE contact_expresspostal contact_expresspostal varchar(16) null default '';


-------------------------------------------------------------------------------
-- Update Kind table
-------------------------------------------------------------------------------
-- New columns
ALTER TABLE Kind ADD COLUMN kind_default int(1) NOT NULL DEFAULT '0' after kind_lang;
