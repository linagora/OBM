-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.4 to 0.7.5                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'DataSource'
--
CREATE TABLE DataSource (
  datasource_id int(8) DEFAULT '0' NOT NULL auto_increment,
  datasource_timeupdate timestamp(14),
  datasource_timecreate timestamp(14),
  datasource_userupdate int(8),
  datasource_usercreate int(8),
  datasource_name varchar(64),
  PRIMARY KEY (datasource_id)
);
