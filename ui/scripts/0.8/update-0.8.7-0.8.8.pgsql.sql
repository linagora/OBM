-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.7 to 0.8.8                     //
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
ALTER TABLE Company ADD COLUMN temp_expresspostal varchar(16) null;
UPDATE Company set temp_expresspostal=company_expresspostal;
ALTER TABLE Company DROP COLUMN company_expresspostal;
ALTER TABLE Company RENAME COLUMN temp_expresspostal TO company_expresspostal;

-- Contact Table
ALTER TABLE Contact ADD COLUMN temp_expresspostal varchar(16) null;
UPDATE Contact set temp_expresspostal=contact_expresspostal;
ALTER TABLE Contact DROP COLUMN contact_expresspostal;
ALTER TABLE Contact RENAME COLUMN temp_expresspostal TO contact_expresspostal;


-------------------------------------------------------------------------------
-- Update Kind table
-------------------------------------------------------------------------------
-- New columns
ALTER TABLE Kind ADD COLUMN kind_default integer;
UPDATE Kind set kind_default=0;
ALTER TABLE Kind ALTER COLUMN kind_default SET NOT NULL;
ALTER TABLE Kind ALTER COLUMN kind_default SET DEFAULT 0;
