
--
-- Table structure for table `Profile`
--

DROP TABLE IF EXISTS Profile;
CREATE TABLE Profile (
  profile_id int(8) NOT NULL auto_increment,
  profile_domain_id int(8) NOT NULL,
  profile_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  profile_timecreate timestamp NOT NULL default '0000-00-00 00:00:00',
  profile_userupdate int(8) default NULL,
  profile_usercreate int(8) default NULL,
  profile_name varchar(64) default NULL,
  PRIMARY KEY  (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `ProfileModule`
--

DROP TABLE IF EXISTS ProfileModule;
CREATE TABLE ProfileModule (
  profilemodule_id int(8) NOT NULL auto_increment,
  profilemodule_domain_id int(8) NOT NULL,
  profilemodule_profile_id int(8) default NULL,
  profilemodule_module_name varchar(16) NOT NULL default '',
  profilemodule_right int(2) default NULL,
  PRIMARY KEY (profilemodule_id),
  CONSTRAINT profilemodule_profile_id_profile_id_fkey FOREIGN KEY (profilemodule_profile_id) REFERENCES Profile (profile_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `ProfileSection`
--

DROP TABLE IF EXISTS ProfileSection;
CREATE TABLE ProfileSection (
  profilesection_id int(8) NOT NULL auto_increment,
  profilesection_domain_id int(8) NOT NULL,
  profilesection_profile_id int(8) default NULL,
  profilesection_section_name varchar(16) NOT NULL default '',
  profilesection_show tinyint(1) default NULL,
  PRIMARY KEY (profilesection_id),
  CONSTRAINT profilesection_profile_id_profile_id_fkey FOREIGN KEY (profilesection_profile_id) REFERENCES Profile (profile_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

