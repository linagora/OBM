-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 1.0 to 1.1                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.1' where obminfo_name='db_version';

-------------------------------------------------------------------------------
-- Update Todo table
-------------------------------------------------------------------------------
-- Add columns 
ALTER TABLE Contact ADD COLUMN contact_privacy int(2) NOT NULL DEFAULT 0 after todo_user;
ALTER TABLE Contact ADD COLUMN contact_dateend timestamp(14) after todo_deadline;


