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

-- Day the week start 
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_order','todo_priority');

-- Todo top list
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_1_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_2_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_3_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_4_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_5_id','0');

-- module 'todo'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_title', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_priority', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'date_todo', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'date_deadline', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_id', 5, 2);

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
-- Contact
-------------------------------------------------------------------------------
ALTER TABLE Contact ADD contact_address3 varchar(50) AFTER contact_address2;

-------------------------------------------------------------------------------
-- ProjectStat
-------------------------------------------------------------------------------
-- add missing lines (only from update-0.6.6-0.7.0 and not create)
ALTER TABLE ProjectStat ADD projectstat_timeupdate timestamp(14) AFTER projectstat_date;
ALTER TABLE ProjectStat ADD projectstat_userupdate int(8) AFTER projectstat_usercreate;
