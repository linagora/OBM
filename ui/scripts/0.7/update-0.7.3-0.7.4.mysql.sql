-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.3 to 0.7.4                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update structure for table 'TimeTask'
-------------------------------------------------------------------------------
-- Add new index to improve Project performance
ALTER table TimeTask add INDEX tt_idx_pt (timetask_projecttask_id);


-------------------------------------------------------------------------------
-- Update structure for table 'ProjectUser'
-------------------------------------------------------------------------------
-- Add new index to improve Project performance
ALTER table ProjectUser add INDEX pu_idx_pt (projectuser_projecttask_id);

-- Some cleaning
DELETE from ProjectUser where projectuser_user_id='0';


-------------------------------------------------------------------------------
-- Update structure for table 'Project'
-------------------------------------------------------------------------------
-- Add new column : project_estimatedtime
ALTER table Project add column project_estimatedtime int(8) DEFAULT NULL after project_soldtime;
UPDATE Project set project_estimatedtime=project_soldtime;
