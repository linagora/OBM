-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.6.6 to 0.7.0                                //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- IncidentPriority
-------------------------------------------------------------------------------
-- Add new column : incidentpriority_color
ALTER table IncidentPriority add column incidentpriority_color char(6) AFTER incidentpriority_order;


-------------------------------------------------------------------------------
-- Company
-------------------------------------------------------------------------------
-- Update company_archive to '0' where = ''
Update Company set company_archive='0' where company_archive='';


-------------------------------------------------------------------------------
-- Deal
-------------------------------------------------------------------------------
-- Add new column : deal_soldtime
ALTER TABLE Deal ADD deal_soldtime int(8) DEFAULT NULL AFTER deal_datealarm;

-- Add new column : deal_project_status
ALTER TABLE Deal ADD deal_project_status int(1) DEFAULT 0 AFTER deal_soldtime;


-------------------------------------------------------------------------------
-- List
-------------------------------------------------------------------------------
-- Add query field
ALTER table List ADD list_query text AFTER list_email;

-------------------------------------------------------------------------------
-- Task -> TimeTask
-------------------------------------------------------------------------------
alter table Task
  change task_id timetask_id int(8) NOT NULL auto_increment,
  change task_timeupdate timetask_timeupdate timestamp(14) NOT NULL,
  change task_timecreate timetask_timecreate timestamp(14) NOT NULL,
  change task_userupdate timetask_userupdate int(8) default NULL,
  change task_usercreate timetask_usercreate int(8) default NULL,
  change task_user_id timetask_user_id int(8) default NULL,
  change task_date timetask_date timestamp(14) NOT NULL,
  change task_deal_id timetask_projecttask_id int(8) default NULL,
  change task_length timetask_length int(2) default NULL,
  change task_tasktype_id timetask_tasktype_id int(8) default NULL,
  change task_label timetask_label varchar(255) default NULL,
  change task_status timetask_status int(1) default NULL;

alter table Task rename to TimeTask;

-------------------------------------------------------------------------------
-- ProjectStat
-------------------------------------------------------------------------------
CREATE TABLE ProjectStat (
  projectstat_deal_id int(8) NOT NULL,
  projectstat_date timestamp(14) NOT NULL,
  projectstat_timecreate timestamp(14) NOT NULL,
  projectstat_usercreate int(8) default NULL,
  projectstat_useddays int(8) default NULL,
  projectstat_remainingdays int(8) default NULL,
  PRIMARY KEY (projectstat_deal_id, projectstat_date)
);

-------------------------------------------------------------------------------
-- ProjectTask
-------------------------------------------------------------------------------
CREATE TABLE ProjectTask (
  projecttask_id int(8) DEFAULT '0' NOT NULL auto_increment,
  projecttask_deal_id int(8) NOT NULL,
  projecttask_timeupdate timestamp(14) NOT NULL,
  projecttask_timecreate timestamp(14) NOT NULL,
  projecttask_userupdate int(8) default NULL,
  projecttask_usercreate int(8) default NULL,
  projecttask_label varchar(255) default NULL,
  projecttask_parenttask_id int(8) default 0,
  projecttask_rank int(8) default NULL,
  PRIMARY KEY (projecttask_id)
);

-------------------------------------------------------------------------------
-- ProjectUser
-------------------------------------------------------------------------------

CREATE TABLE ProjectUser (
  projectuser_id int(8) DEFAULT '0' NOT NULL auto_increment,
  projectuser_user_id int(8) NOT NULL,
  projectuser_deal_id int(8) NOT NULL,
  projectuser_projecttask_id int(8),
  projectuser_timeupdate timestamp(14) NOT NULL,
  projectuser_timecreate timestamp(14) NOT NULL,
  projectuser_userupdate int(8) default NULL,
  projectuser_usercreate int(8) default NULL,
  projectuser_projectedtime int(8) default NULL,
  projectuser_missingtime int(8) default NULL,
  projectuser_validity timestamp(14) default NULL,
  projectuser_soldprice int(8) default NULL,
  projectuser_manager int(1) default NULL,
  PRIMARY KEY (projectuser_id)
);

-------------------------------------------------------------------------------
-- Document
-------------------------------------------------------------------------------
CREATE TABLE Document (
  document_timeupdate timestamp(14) NOT NULL,
  document_timecreate timestamp(14) NOT NULL,
  document_userupdate int(8) default NULL,
  document_usercreate int(8) default NULL,
  document_id int(8) NOT NULL auto_increment,
  document_title varchar(255) default NULL,
  document_name varchar(255) default NULL,
  document_mimetype varchar(255) default NULL,
  document_category1 varchar(255) default NULL,
  document_category2 varchar(255) default NULL,
  document_author varchar(255) default NULL,
  document_private int(1) default NULL,
  document_path text default NULL,
  document_size int(15) default NULL,
  PRIMARY KEY (document_id)
);

-------------------------------------------------------------------------------
-- DocumentCategory1 
-------------------------------------------------------------------------------
CREATE TABLE DocumentCategory1 (
  documentcategory1_timeupdate timestamp(14) NOT NULL,
  documentcategory1_timecreate timestamp(14) NOT NULL,
  documentcategory1_userupdate int(8) default NULL,
  documentcategory1_usercreate int(8) default NULL,
  documentcategory1_id int(8) NOT NULL auto_increment,
  documentcategory1_label varchar(255) default NULL,
  PRIMARY KEY (documentcategory1_id)
);

-------------------------------------------------------------------------------
-- DocumentCategory2 
-------------------------------------------------------------------------------
CREATE TABLE DocumentCategory2 (
  documentcategory2_timeupdate timestamp(14) NOT NULL,
  documentcategory2_timecreate timestamp(14) NOT NULL,
  documentcategory2_userupdate int(8) default NULL,
  documentcategory2_usercreate int(8) default NULL,
  documentcategory2_id int(8) NOT NULL auto_increment,
  documentcategory2_label varchar(255) default NULL,
  PRIMARY KEY (documentcategory2_id)
);


-------------------------------------------------------------------------------
-- DocumentMimeType 
-------------------------------------------------------------------------------
CREATE TABLE DocumentMimeType (
  documentmimetype_timeupdate timestamp(14) NOT NULL,
  documentmimetype_timecreate timestamp(14) NOT NULL,
  documentmimetype_userupdate int(8) default NULL,
  documentmimetype_usercreate int(8) default NULL,
  documentmimetype_id int(8) NOT NULL auto_increment,
  documentmimetype_label varchar(255) default NULL,
  documentmimetype_extension varchar(10) default NULL,
  documentmimetype_mime varchar(255) default NULL,
  PRIMARY KEY (documentmimetype_id)
);

-------------------------------------------------------------------------------
-- Tasktype
-------------------------------------------------------------------------------
-- change column value : 
Update TaskType set tasktype_internal = 2 where tasktype_internal = 1;

-------------------------------------------------------------------------------
-- DisplayPref
-------------------------------------------------------------------------------
-- delete old time prefs
DELETE
FROM DisplayPref
WHERE display_entity like "time%";

-- add new preferences for the project displays
INSERT INTO DisplayPref
(display_user_id, display_entity, display_fieldname, display_fieldorder, display_display)
VALUES (0,'project','project_label',1,2),
       (0,'project','project_company_name',2,1),
       (0,'project','project_tasktype',3,1),
       (0,'project','project_status',4,1),
       (0,'project','project_archive',5,1),
       (0,'time','date_task',1,2),
       (0,'time','timetask_deal_label',2,2),
       (0,'time','timetask_company_name',3,1),
       (0,'time','timetask_label',4,1),
       (0,'time','tasktype_label',5,1),
       (0,'time','timetask_length',6,2),
       (0,'time','timetask_id',7,2),
       (0,'time_proj','deal_label',1,2),
       (0,'time_proj','company_name',2,2),
       (0,'time_proj','total_length',3,1),
       (0,'time_proj','total_before',4,1),
       (0,'time_proj','total_after',5,1),
       (0,'time_tt','tasktype_label',1,2),
       (0,'time_tt','total_length',2,1),
       (0,'time_tt','total_before',3,1),
       (0,'time_tt','total_after',4,1);

-------------------------------------------------------------------------------
-- Group module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'UGroup' (cause Group is a reserved keyword)
--
CREATE TABLE UGroup (
   group_id int(8) NOT NULL auto_increment,
   group_timeupdate timestamp(14),
   group_timecreate timestamp(14),
   group_userupdate int(8),
   group_usercreate int(8),
   group_name varchar(32) NOT NULL,
   group_desc varchar(128),
   group_email varchar(128),
   PRIMARY KEY (group_id),
   UNIQUE group_name (group_name)
);

-- Add new column : group_system
ALTER TABLE UGroup ADD group_system int(1) DEFAULT 0 AFTER group_usercreate;

-- Add systems Groups
INSERT INTO UGroup (group_system, group_name, group_desc, group_email) VALUES
(1, 'Commercial', 'Commercial system group', ''),
(1, 'Production', 'Production system group', '');

--
-- Table structure for table 'UserObmGroup'
--
CREATE TABLE UserObmGroup (
   userobmgroup_groupid int(8) DEFAULT '0' NOT NULL,
   userobmgroup_userobmid int(8) DEFAULT '0' NOT NULL
);


--
-- Table structure for table 'GroupGroup'
--
CREATE TABLE GroupGroup (
   groupgroup_parentid int(8) DEFAULT '0' NOT NULL,
   groupgroup_childid int(8) DEFAULT '0' NOT NULL
);


-- module 'group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_email', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_nb_user', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'usercreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timecreate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'userupdate', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timeupdate', 8, 1);

-- module 'group_user'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_phone', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_email', 4, 1);

-- module 'group_group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_email', 3, 1);


-------------------------------------------------------------------------------
-- UserObm
-------------------------------------------------------------------------------
-- Add new column : userobm_phone
ALTER table UserObm add column userobm_phone varchar(20) AFTER userobm_firstname;
