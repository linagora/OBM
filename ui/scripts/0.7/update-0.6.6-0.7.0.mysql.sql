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
  projectuser_projecttask_id int(8) NOT NULL,
  projectuser_user_id int(8) NOT NULL,
  projectuser_timeupdate timestamp(14) NOT NULL,
  projectuser_timecreate timestamp(14) NOT NULL,
  projectuser_userupdate int(8) default NULL,
  projectuser_usercreate int(8) default NULL,
  projectuser_projectedtime int(8) default NULL,
  projectuser_missingtime int(8) default NULL,
  projectuser_validity timestamp(14) default NULL,
  projectuser_soldprice int(8) default NULL,
  projectuser_manager int(1) default NULL,
  PRIMARY KEY (projectuser_projecttask_id, projectuser_user_id)
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
VALUES (0,'project_new','project_initlabel',1,2),
       (0,'project_new','project_company_name',2,2),
       (0,'project_new','project_tasktype',3,2),
       (0,'project','project_label',1,2),
       (0,'project','project_company_name',2,1),
       (0,'project','project_tasktype',3,1),
       (0,'project','project_soldtime',4,1),
       (0,'project','project_archive',5,1),
       (0,'time','date_task',1,2),
       (0,'time','timetask_deal_label',2,2),
       (0,'time','timetask_company_name',3,1),
       (0,'time','timetask_label',4,1),
       (0,'time','tasktype_label',5,1),
       (0,'time','timetask_length',6,2),
       (0,'time','timetask_id',7,2),
       (0,'time_projmonth','deal_label',1,2),
       (0,'time_projmonth','company_name',2,2),
       (0,'time_projmonth','total_length',3,1),
       (0,'time_projmonth','total_before',4,1),
       (0,'time_projmonth','total_after',5,1),
       (0,'time_ttmonth','tasktype_label',1,2),
       (0,'time_ttmonth','total_length',2,1),
       (0,'time_ttmonth','total_before',3,1),
       (0,'time_ttmonth','total_after',4,1),
       (0,'time_projuser','deal_label',1,2),
       (0,'time_projuser','company_name',2,2),
       (0,'time_projuser','total_spent',3,1),
--       (0,'time_projuser','total_before',3,1),
--       (0,'time_projuser','total_after',4,1),
       (0,'time_ttuser','tasktype_label',1,2),
       (0,'time_ttuser','total_spent',2,1)
--       (0,'time_ttuser','total_before',3,1),
--       (0,'time_ttuser','total_after',4,1)
;
