-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.2 to 0.7.3                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Project (now separated from Deal !)
-------------------------------------------------------------------------------
-- Create new table
CREATE TABLE Project (
  project_id int(8) DEFAULT '0' NOT NULL auto_increment,
  project_timeupdate timestamp(14),
  project_timecreate timestamp(14),
  project_userupdate int(8),
  project_usercreate int(8),
  project_name varchar(128),
  project_tasktype_id int(8),
  project_company_id int(8),
  project_deal_id int(8),
  project_soldtime int(8) DEFAULT NULL,
  project_datebegin date,
  project_dateend date,
  project_archive char(1) DEFAULT '0',
  project_comment text,
  PRIMARY KEY (project_id),
  INDEX project_idx_comp (project_company_id),
  INDEX project_idx_deal (project_deal_id)
);


-- module 'project'

DELETE FROM DisplayPref where display_user_id='0' and display_entity='project';

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_company', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_tasktype', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_soldtime', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_datebegin', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_dateend', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_archive', 7, 1);


--
-- Table structure for table 'ProjectStat'
--
DROP table IF EXISTS ProjectStat;

CREATE TABLE ProjectStat (
  projectstat_project_id int(8) NOT NULL,
  projectstat_usercreate int(8) NOT NULL,
  projectstat_date timestamp(14) NOT NULL,
  projectstat_useddays int(8) default NULL,
  projectstat_remainingdays int(8) default NULL,
  PRIMARY KEY (projectstat_project_id, projectstat_usercreate, projectstat_date)
);


-------------------------------------------------------------------------------
-- Update structure for table 'ProjectUser'
-------------------------------------------------------------------------------
-- Replace projectuser_deal_id by projectuser_project_id

-- Add new column : projectuser_project_id
ALTER table ProjectUser add column projectuser_project_id int(8) NOT NULL AFTER projectuser_id;

-- Update projectuser_project_id to projectuser_deal_id value
UPDATE ProjectUser set projectuser_project_id=projectuser_deal_id;

-- Drop deprecated columnprojectuser_deal_id
ALTER table ProjectUser DROP column projectuser_deal_id;

-- Add Indexes
ALTER table ProjectUser add INDEX pu_idx_pro (projectuser_project_id);
ALTER table ProjectUser add INDEX pu_idx_user (projectuser_user_id);


-------------------------------------------------------------------------------
-- Update structure for table 'ProjectTask'
-------------------------------------------------------------------------------
-- Replace projecttask_deal_id by projecttask_project_id
ALTER table ProjectTask change projecttask_deal_id projecttask_project_id int(8) NOT NULL;

-- Add Indexes
ALTER table ProjectTask add INDEX pt_idx_pro (projecttask_project_id);


-------------------------------------------------------------------------------
-- DisplayPref updates
-------------------------------------------------------------------------------

-- Update project_label to project_name in DisplayPref, entity project
UPDATE DisplayPref set display_fieldname='project_name' where display_entity='project' and display_fieldname='project_label';

-- Update deal_label to project_name in DisplayPref, entity time_proj
UPDATE DisplayPref set display_fieldname='project_name' where display_entity='time_proj' and display_fieldname='deal_label';

-- Update timetask_deal_label to timetask_project_name in entity time_proj
UPDATE DisplayPref set display_fieldname='timetask_project_name' where display_entity='time' and display_fieldname='timetask_deal_label';
