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

-------------------------------------------------------------------------------
-- Update Contact tables
-------------------------------------------------------------------------------
-- Change column : contact_addresses 1 2 and 3 to varchar(64)

ALTER TABLE Contact ADD COLUMN temp_contact_address1 VARCHAR(64);
ALTER TABLE Contact ADD COLUMN temp_contact_address2 VARCHAR(64);
ALTER TABLE Contact ADD COLUMN temp_contact_address3 VARCHAR(64);
UPDATE Contact SET temp_contact_address1 = contact_address1; 
UPDATE Contact SET temp_contact_address2 = contact_address2;
UPDATE Contact SET temp_contact_address3 = contact_address3;
ALTER TABLE Contact DROP COLUMN contact_address1; 		   
ALTER TABLE Contact DROP COLUMN contact_address2; 		   
ALTER TABLE Contact DROP COLUMN contact_address3; 
ALTER TABLE Contact RENAME COLUMN temp_contact_address1 TO contact_address1; 
ALTER TABLE Contact RENAME COLUMN temp_contact_address2 TO contact_address2; 
ALTER TABLE Contact RENAME COLUMN temp_contact_address3 TO contact_address3; 

-------------------------------------------------------------------------------
-- Update Publication tables
-------------------------------------------------------------------------------
-- Change column : publication_lang varchar(20)

DROP table IF EXISTS Publication;

CREATE TABLE Publication (
  publication_id             serial,
  publication_timeupdate     timestamp,
  publication_timecreate     timestamp,
  publication_userupdate     integer,
  publication_usercreate     integer,
  publication_title          varchar(64) NOT NULL,
  publication_type_id        integer,
  publication_year           integer,
  publication_lang           varchar(30),
  publication_desc           text,
  PRIMARY KEY (publication_id)
);

-------------------------------------------------------------------------------
-- Update Subscription tables
-------------------------------------------------------------------------------
-- Change column : subscription_renewal int(1)

DROP table IF EXISTS Subscription;

CREATE TABLE Subscription (
  subscription_publication_id 	integer NOT NULL,
  subscription_contact_id       integer NOT NULL,
  subscription_timeupdate       timestamp,
  subscription_timecreate       timestamp,
  subscription_userupdate       integer,
  subscription_usercreate       integer,
  subscription_quantity       	integer,
  subscription_renewal          integer NOT NULL,
  subscription_reception_id     integer NOT NULL,
  subscription_date_begin       timestamp,
  subscription_date_end         timestamp,
  PRIMARY KEY (subscription_publication_id,subscription_contact_id)
);
