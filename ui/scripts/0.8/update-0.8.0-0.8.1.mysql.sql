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
-- Update Publication tables
-------------------------------------------------------------------------------
-- Change column : publication_lang varchar(20)
ALTER TABLE Publication CHANGE column publication_lang publication_lang VARCHAR(30);

-------------------------------------------------------------------------------
-- Update UserObmPref table
-------------------------------------------------------------------------------
-- Change option set_todo
UPDATE UserObmPref set userobmpref_value='todo_priority' where userobmpref_option='set_todo';

-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
DROP table IF EXISTS Import;

--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id             int(8) DEFAULT '0' NOT NULL auto_increment,
  import_timeupdate     timestamp(14),
  import_timecreate     timestamp(14),
  import_userupdate     int(8),
  import_usercreate     int(8),
  import_name           varchar(64) NOT NULL,
  import_datasource_id  int(8),
  import_separator      varchar(3),
  import_enclosed       char(1),
  import_desc           text,
  PRIMARY KEY (import_id),
  UNIQUE (import_name)
);

