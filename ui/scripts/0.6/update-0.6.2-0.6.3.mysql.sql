-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.6.2 to 0.6.3	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Calendar Update (Right)
-------------------------------------------------------------------------------

CREATE TABLE CalendarRight (
  calendarright_ownerid int(8) NOT NULL default '0',
  calendarright_customerid int(8) NOT NULL default '0',
  calendarright_write int(1) NOT NULL default '0',
  calendarright_read int(1) NOT NULL default '0',
  PRIMARY KEY  (calendarright_ownerid,calendarright_customerid)
);


-------------------------------------------------------------------------------
-- Update deal hitrate values according to state
-------------------------------------------------------------------------------
-- Set hitrate = 100 where status = SIGNED,DONE,INVOICE,PAYED
UPDATE Deal set deal_hitrate = '100' where deal_status_id = '1' or deal_status_id = '7' or deal_status_id = '8' or deal_status_id = '9';


-------------------------------------------------------------------------------
-- Create incident dico tables
-------------------------------------------------------------------------------
--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id int(8) NOT NULL auto_increment,
  incidentpriority_timeupdate timestamp(14) NOT NULL,
  incidentpriority_timecreate timestamp(14) NOT NULL,
  incidentpriority_userupdate int(8) default NULL,
  incidentpriority_usercreate int(8) default NULL,
  incidentpriority_order int(2),
  incidentpriority_label varchar(32) default NULL,
  PRIMARY KEY (incidentpriority_id)
) TYPE=MyISAM;


--
-- Dumping data for table 'IncidentPriority'
--
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,1,'Red Hot');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,2,'Hot');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,3,'Normal');
INSERT INTO IncidentPriority (incidentpriority_timeupdate, incidentpriority_timecreate, incidentpriority_userupdate, incidentpriority_usercreate, incidentpriority_order,incidentpriority_label) VALUES (null,null,null,1,4,'Low');


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id int(8) NOT NULL auto_increment,
  incidentstatus_timeupdate timestamp(14) NOT NULL,
  incidentstatus_timecreate timestamp(14) NOT NULL,
  incidentstatus_userupdate int(8) default NULL,
  incidentstatus_usercreate int(8) default NULL,
  incidentstatus_order int(2),
  incidentstatus_label varchar(32) default NULL,
  PRIMARY KEY (incidentstatus_id)
) TYPE=MyISAM;


--
-- Dumping data for table 'IncidentStatus'
--
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,1,'Open');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,2,'Call');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,3,'Wait for Call');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,4,'Paused');
INSERT INTO IncidentStatus (incidentstatus_timeupdate, incidentstatus_timecreate, incidentstatus_userupdate, incidentstatus_usercreate, incidentstatus_order,incidentstatus_label) VALUES (null,null,null,1,5,'Closed');


-------------------------------------------------------------------------------
-- Update table incident
-------------------------------------------------------------------------------
-- Add new column : incident_priority_id
ALTER table Incident add column incident_priority_id int(8) AFTER incident_priority;

-- Add new column : incident_status_id
ALTER table Incident add column incident_status_id int(8) AFTER incident_state;

-- Update incident_priority_id
Update Incident set incident_priority_id='1' where incident_priority='REDHOT';
Update Incident set incident_priority_id='2' where incident_priority='HOT';
Update Incident set incident_priority_id='3' where incident_priority='NORMAL';
Update Incident set incident_priority_id='4' where incident_priority='LOW';

-- Update incident_status_id
Update Incident set incident_status_id='1' where incident_state='OPEN';
Update Incident set incident_status_id='2' where incident_state='CALL';
Update Incident set incident_status_id='3' where incident_state='WAITCALL';
Update Incident set incident_status_id='4' where incident_state='PAUSED';
Update Incident set incident_status_id='5' where incident_state='CLOSED';

-- Drop column incident_priority
ALTER table Incident drop column incident_priority;

-- Drop column incident_state
ALTER table Incident drop column incident_state;
