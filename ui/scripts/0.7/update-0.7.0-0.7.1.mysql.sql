-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.7.0 to 0.7.1                                //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Todo
-------------------------------------------------------------------------------
-- Create new table
CREATE TABLE Todo (
  todo_id int(8) DEFAULT '0' NOT NULL auto_increment,
  todo_timeupdate timestamp(14),
  todo_timecreate timestamp(14),
  todo_userupdate int(8),
  todo_usercreate int(8),
  todo_user int(8),
  todo_date timestamp(14) default NULL,
  todo_deadline timestamp(14) default NULL,
  todo_priority int(8) default NULL,
  todo_title Varchar(80) default NULL,
  todo_content text default NULL,
  PRIMARY KEY (todo_id)
);


-------------------------------------------------------------------------------
-- Document
-------------------------------------------------------------------------------
ALTER TABLE Document ADD document_kind int(2) AFTER document_name;


-------------------------------------------------------------------------------
-- Preferences
-------------------------------------------------------------------------------
-- CSV Export separator
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_csv_sep',';');


-------------------------------------------------------------------------------
-- ProjectStat
-------------------------------------------------------------------------------
-- add missing lines (only from update-0.6.6-0.7.0 and not create)
ALTER TABLE ProjectStat ADD projectstat_timeupdate timestamp(14) AFTER projectstat_date;
ALTER TABLE ProjectStat ADD projectstat_userupdate int(8) AFTER projectstat_usercreate;
