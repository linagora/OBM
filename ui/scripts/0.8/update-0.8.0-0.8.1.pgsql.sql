-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.0 to 0.8.1                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update Active User tables
-------------------------------------------------------------------------------
-- Change column : activeuserobm varchar(64)
DROP table ActiveUserObm;

--
-- Table structure for table 'ActiveUserObm'
--
CREATE TABLE ActiveUserObm (
  activeuserobm_sid            varchar(32) NOT NULL DEFAULT '',
  activeuserobm_session_name   varchar(32) NOT NULL DEFAULT '',
  activeuserobm_userobm_id     integer DEFAULT NULL,
  activeuserobm_timeupdate     timestamp,
  activeuserobm_timecreate     timestamp,
  activeuserobm_nb_connexions  integer NOT NULL DEFAULT '0',
  activeuserobm_lastpage       varchar(64) NOT NULL DEFAULT '0',
  activeuserobm_ip             varchar(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (activeuserobm_sid)
);


-------------------------------------------------------------------------------
-- Update UserObmPref table
-------------------------------------------------------------------------------
-- Change option set_todo
UPDATE UserObmPref set userobmpref_value='todo_priority' where userobmpref_option='set_todo';


-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
DROP table Import;

--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id                   serial,
  import_timeupdate           timestamp,
  import_timecreate           timestamp,
  import_userupdate           integer,
  import_usercreate           integer,
  import_name                 varchar(64) NOT NULL,
  import_datasource_id        integer,
  import_marketingmanager_id  integer,
  import_separator            varchar(3),
  import_enclosed             char(1),
  import_desc                 text,
  PRIMARY KEY (import_id),
  UNIQUE (import_name)
);
