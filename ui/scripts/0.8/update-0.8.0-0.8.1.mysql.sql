-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.8.0 to 0.8.1                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update Contact tables
-------------------------------------------------------------------------------
-- Change column : contact_addresses 1 2 and 3 to varchar(64)
 
ALTER TABLE Contact CHANGE column contact_address1 contact_address1 VARCHAR(64);
ALTER TABLE Contact CHANGE column contact_address2 contact_address2 VARCHAR(64);
ALTER TABLE Contact CHANGE column contact_address2 contact_address2 VARCHAR(64);

-------------------------------------------------------------------------------
-- Update UserObmPref table
-------------------------------------------------------------------------------
-- Change option set_todo
UPDATE UserObmPref set userobmpref_value='todo_priority' where userobmpref_option='set_todo';
