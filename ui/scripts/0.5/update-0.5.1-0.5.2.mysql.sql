-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.5.1 to 0.5.2	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


--
-- Company updates
--
-- Add column company_contact_number
ALTER table Company add column company_contact_number int(5) NOT NULL default '0' AFTER company_email;
-- Add column company_deal_number
ALTER table Company add column company_deal_number int(5) NOT NULL default '0' AFTER company_contact_number;


--
-- Timemanagement module
--
-- ////////////////////////////////////////////////////////////////////////////
-- // Table structure creation for tables 'Task' and 'TaskType'              //
-- ////////////////////////////////////////////////////////////////////////////

CREATE TABLE Task (
  task_id int(8) NOT NULL auto_increment,
  task_timeupdate timestamp(14) NOT NULL,
  task_timecreate timestamp(14) NOT NULL,
  task_userupdate int(8) default NULL,
  task_usercreate int(8) default NULL,
  task_user_id int(8) default NULL,
  task_date timestamp(14) NOT NULL,
  task_deal_id int(8) default NULL,
  task_length int(2) default NULL,
  task_tasktype_id int(8) default NULL,
  task_label varchar(255) default NULL,
  task_status int(1) default NULL,
  PRIMARY KEY  (task_id)
) TYPE=MyISAM;


CREATE TABLE TaskType (
  tasktype_id int(8) NOT NULL auto_increment,
  tasktype_timeupdate timestamp(14) NOT NULL,
  tasktype_timecreate timestamp(14) NOT NULL,
  tasktype_userupdate int(8) default NULL,
  tasktype_usercreate int(8) default NULL,
  tasktype_internal int(1) NOT NULL,
  tasktype_label varchar(32) default NULL,
  PRIMARY KEY  (tasktype_id)
) TYPE=MyISAM;



-- ////////////////////////////////////////////////////////////////////////////
-- // Table values modifications for table 'TaskType'                        //
-- ////////////////////////////////////////////////////////////////////////////
-- Insert new Tasktypes

-- FRENCH VERSION
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (1,'Développement',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (2,'Sav / Maintenance',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (3,'Formation',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (4,'Etudes / Conseil',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (5,'Réseau / Intégration',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (6,'Infographie',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (7,'Hébergement',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (8,'Matériel',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (9,'Autres',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (11,'Avant vente',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (12,'Préparation formation',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (13,'Développements internes',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (14,'Projets internes',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (15,'Auto-Formations,Veille',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (16,'Garantie contractuelle projets',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (17,'Divers(direction,autres)',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) TaskType VALUES (18,'Congés , absences , maladie',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (19,'Déplacements',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (20,'Infographie/Communication',1);


-- ENGLISH VERSION
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (1,'Development',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (2,'Support / Assistance',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (3,'Learning course',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (4,'Studies',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (5,'Network / Integration',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (6,'Graphics',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (7,'Others',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (8,'Hosting',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (9,'Hardware',0);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (11,'Before selling',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (12,'Support making',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (13,'Internal development',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (14,'Internal project',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (15,'Self formation',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (16,'Contract garanty',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (17,'Miscellaneaous(direction,others)',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (18,'Holydays,...',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (19,'Outgoings',1);
INSERT INTO TaskType (tasktype_id, tasktype_label, tasktype_internal) VALUES (20,'Graphics/Communication',1);


-- ////////////////////////////////////////////////////////////////////////////
-- // Table values modification for table 'Deal'                         //
-- ////////////////////////////////////////////////////////////////////////////
update Deal set deal_category_id=11 where deal_category_id=1;
update Deal set deal_category_id=12 where deal_category_id=2;
update Deal set deal_category_id=13 where deal_category_id=3;
update Deal set deal_category_id=14 where deal_category_id=4;
update Deal set deal_category_id=15 where deal_category_id=5;
update Deal set deal_category_id=16 where deal_category_id=6;
update Deal set deal_category_id=19 where deal_category_id=9;
update Deal set deal_category_id=17 where deal_category_id=7;
update Deal set deal_category_id=18 where deal_category_id=8;

update Deal set deal_category_id=2 where deal_category_id=11;
update Deal set deal_category_id=3 where deal_category_id=12;
update Deal set deal_category_id=1 where deal_category_id=13;
update Deal set deal_category_id=1 where deal_category_id=14;
update Deal set deal_category_id=1 where deal_category_id=15;
update Deal set deal_category_id=5 where deal_category_id=16;
update Deal set deal_category_id=9 where deal_category_id=19;
update Deal set deal_category_id=1 where deal_category_id=17;
update Deal set deal_category_id=7 where deal_category_id=18;

-- ////////////////////////////////////////////////////////////////////////////
-- // Table values modification for table 'Task' colonne 'Autres' de 7 a 9   //
-- ////////////////////////////////////////////////////////////////////////////
update Task set task_tasktype_id='9' where task_tasktype_id='7';


-- ////////////////////////////////////////////////////////////////////////////
-- // Table structure modification for table 'Deal'                          //
-- ////////////////////////////////////////////////////////////////////////////
ALTER TABLE `Deal` CHANGE `deal_category_id` `deal_tasktype_id` INT(8) DEFAULT NULL;

-- ////////////////////////////////////////////////////////////////////////////
-- // DisplayPref values changed                                             //
-- ////////////////////////////////////////////////////////////////////////////
UPDATE DisplayPref set display_fieldname='tasktype_label' where display_fieldname='dealcategory_minilabel';

-- ////////////////////////////////////////////////////////////////////////////
-- // Drop table DealCategory                                                //
-- ////////////////////////////////////////////////////////////////////////////
DROP table DealCategory;


--
-- Table structure for table 'ActiveUserObm'
--
CREATE TABLE ActiveUserObm (
  activeuserobm_sid		varchar(32) NOT NULL default '',
  activeuserobm_session_name	varchar(32) NOT NULL default '',
  activeuserobm_userobm_id	int(11) default NULL,
  activeuserobm_timeupdate	varchar(14) NOT NULL default '',
  activeuserobm_timecreate	varchar(14) NOT NULL default '0',
  activeuserobm_nb_connexions	int(11) NOT NULL default '0',
  activeuserobm_lastpage	varchar(32) NOT NULL default '0',
  activeuserobm_ip		varchar(32) NOT NULL default '0',
  PRIMARY KEY  (activeuserobm_sid)
);

--
-- Table structure for table 'UserObm_SessionLog'
--
CREATE TABLE UserObm_SessionLog (
  userobm_sessionlog_sid varchar(32) NOT NULL default '',
  userobm_sessionlog_session_name varchar(32) NOT NULL default '',
  userobm_sessionlog_userobm_id int(11) default NULL,
  userobm_sessionlog_timeupdate varchar(14) NOT NULL default '',
  userobm_sessionlog_timecreate varchar(14) NOT NULL default '0',
  userobm_sessionlog_nb_connexions int(11) NOT NULL default '0',
  userobm_sessionlog_lastpage varchar(32) NOT NULL default '0',
  userobm_sessionlog_ip varchar(32) NOT NULL default '0',
  PRIMARY KEY  (userobm_sessionlog_sid)
);


