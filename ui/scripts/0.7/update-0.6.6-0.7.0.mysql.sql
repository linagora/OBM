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
-- Deal
-------------------------------------------------------------------------------
-- Add new column : deal_soldtime
ALTER TABLE Deal ADD deal_soldtime int(8) DEFAULT NULL;

-- Add new column : deal_state
ALTER TABLE Deal ADD deal_state int(1) DEFAULT 0;

-------------------------------------------------------------------------------
-- Projectstat
-------------------------------------------------------------------------------
CREATE TABLE ProjectStat (
  projectstat_deal_id int(8) NOT NULL,
  projectstat_date timestamp(14) NOT NULL,
  projectstat_timeupdate timestamp(14) NOT NULL,
  projectstat_timecreate timestamp(14) NOT NULL,
  projectstat_userupdate int(8) default NULL,
  projectstat_usercreate int(8) default NULL,
  projectstat_useddays int(8) default NULL,
  projectstat_remainingdays int(8) default NULL,
  PRIMARY KEY (projectstat_deal_id, projectstat_date)
);

-------------------------------------------------------------------------------
-- Projectuser
-------------------------------------------------------------------------------
CREATE TABLE ProjectUser (
  projectuser_deal_id int(8) NOT NULL,
  projectuser_userobm_id int(8) NOT NULL,
  projectuser_timeupdate timestamp(14) NOT NULL,
  projectuser_timecreate timestamp(14) NOT NULL,
  projectuser_userupdate int(8) default NULL,
  projectuser_usercreate int(8) default NULL,
  projectuser_projectedtime int(8) default NULL,
  projectuser_missingtime int(8) default NULL,
  projectuser_validity timestamp(14) default NULL,
  projectuser_soldprice int(8) default NULL,
  projectuser_manager int(1) default NULL,
  PRIMARY KEY (projectuser_deal_id, projectuser_userobm_id)
);

-------------------------------------------------------------------------------
-- Tasktype
-------------------------------------------------------------------------------
-- change column value : 
Update TaskType set tasktype_internal = 2 where tasktype_internal = 1;

-------------------------------------------------------------------------------
-- DisplayPref
-------------------------------------------------------------------------------
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
       (0,'project','project_archive',5,1);
