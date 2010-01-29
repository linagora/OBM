-- Write that the 2.3->2.4 has started
UPDATE ObmInfo SET obminfo_value='2.3.x->2.4.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------



--
-- Table structure for `calendarcolor`
--
DROP TABLE IF EXISTS `calendarcolor`;
CREATE TABLE `calendarcolor` (
  `user_id` int(11) NOT NULL,
  `entity_id` int(11) NOT NULL,
  `eventowner` int(11) default NULL,
  PRIMARY KEY  (`user_id`,`entity_id`),
  KEY `user_id_fkey` (`user_id`),
  KEY `entity_id_fkey` (`entity_id`),
  CONSTRAINT `user_id_userobm_id_fkey` FOREIGN KEY (user_id) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `entity_id_entity_id_fkey` FOREIGN KEY (entity_id) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Update Resource
--
ALTER TABLE Resource
MODIFY `resource_delegation` varchar(256) default '';

--
-- Update Deleted
--
ALTER TABLE Deleted
MODIFY `deleted_delegation` varchar(256) default '';

--
-- Update Host
--
ALTER TABLE Host
MODIFY `host_delegation` varchar(256) default '';

--
-- Update MailShare
--
ALTER TABLE MailShare
MODIFY `mailshare_delegation` varchar(256) default '';

--
-- UGroup
--
ALTER TABLE UGroup
MODIFY `group_delegation` varchar(256) default '';

--
-- Updated
--
ALTER TABLE Updated
MODIFY `updated_delegation` varchar(256) default '';

--
-- Updatedlinks
--
ALTER TABLE Updatedlinks
MODIFY `updatedlinks_delegation` varchar(256) default '';

--
-- UserObm
--
ALTER TABLE UserObm 
MODIFY `userobm_delegation` varchar(256) default '',
MODIFY `userobm_delegation_target` varchar(256) default '';

--
-- SyncedAddressbook
--
ALTER TABLE SyncedAddressbook ENGINE = INNODB DEFAULT CHARSET=utf8;
ALTER TABLE SyncedAddressbook ADD CONSTRAINT `syncedaddressbook_user_id_userobm_id_fkey` FOREIGN KEY (user_id) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE SyncedAddressbook ADD CONSTRAINT syncedaddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES AddressBook (id) ON DELETE CASCADE ON UPDATE CASCADE;



--
-- Table structure for table `userpattern`
--
DROP TABLE IF EXISTS `userpattern`;
CREATE TABLE `userpattern` (
  `id`          int(8) NOT NULL auto_increment,
  `domain_id`   int(8) NOT NULL,
  `timeupdate`  timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `timecreate`  timestamp NOT NULL default '0000-00-00 00:00:00',
  `userupdate`  int(8) default NULL,
  `usercreate`  int(8) default NULL,
  `title`       varchar(255) NOT NULL,
  `description` text default NULL,
  PRIMARY KEY (`id`),
  KEY `userpattern_domain_id_domain_id_fkey` (`domain_id`),
  KEY `userpattern_userupdate_userobm_id_fkey` (`userupdate`),
  KEY `userpattern_usercreate_userobm_id_fkey` (`usercreate`),
  CONSTRAINT `userpattern_domain_id_domain_id_fkey` FOREIGN KEY (`domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userpattern_userupdate_userobm_id_fkey` FOREIGN KEY (`userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `userpattern_usercreate_userobm_id_fkey` FOREIGN KEY (`usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `userpattern_property`
--
DROP TABLE IF EXISTS `userpattern_property`;
CREATE TABLE `userpattern_property` (
  `userpattern_id` int(8) NOT NULL,
  `attribute`      varchar(255) NOT NULL,
  `value`          text NOT NULL,
  PRIMARY KEY (`userpattern_id`,`attribute`),
  KEY `userpattern_property_userpattern_id_userpattern_id_fkey` (`userpattern_id`),
  CONSTRAINT `userpattern_property_userpattern_id_userpattern_id_fkey` FOREIGN KEY (`userpattern_id`) REFERENCES `userpattern` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Display Prefs
--
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','title',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','description',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','timecreate',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','timeupdate',4,1);



-- ----------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';

