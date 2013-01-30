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
-- Update P_Host
--
ALTER TABLE P_Host
MODIFY `host_delegation` varchar(256) default '';

--
-- Update MailShare
--
ALTER TABLE MailShare
MODIFY `mailshare_delegation` varchar(256) default '';

--
-- Update P_MailShare
--
ALTER TABLE P_MailShare
MODIFY `mailshare_delegation` varchar(256) default '';

--
-- UGroup
--
ALTER TABLE UGroup
MODIFY `group_delegation` varchar(256) default '';

--
-- P_UGroup
--
ALTER TABLE P_UGroup
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
ADD COLUMN userobm_commonname varchar(256) default '',
MODIFY `userobm_delegation` varchar(256) default '',
MODIFY `userobm_delegation_target` varchar(256) default '',
MODIFY `userobm_login` varchar(80),
MODIFY `userobm_kind` varchar(64) default NULL,
MODIFY `userobm_title` varchar(256);

--
-- UserObm
--
ALTER TABLE P_UserObm 
ADD COLUMN userobm_commonname varchar(256) default '',
MODIFY `userobm_delegation` varchar(256) default '',
MODIFY `userobm_delegation_target` varchar(256) default '',
MODIFY `userobm_login` varchar(80),
MODIFY `userobm_kind` varchar(64) default NULL,
MODIFY `userobm_title` varchar(256);


--
-- Contact
--
ALTER TABLE Contact 
ADD COLUMN contact_commonname varchar(256) default '';

--
-- SyncedAddressbook
--
DELETE FROM SyncedAddressbook WHERE user_id NOT IN (SELECT userobm_id FROM UserObm);
DELETE FROM SyncedAddressbook WHERE addressbook_id NOT IN (SELECT id FROM AddressBook);
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

--
-- Token
--
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
  `token` varchar(300) NOT NULL, 
  `property` varchar(255) NOT NULL, 
  `value` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `contactgroup`
--

DROP TABLE IF EXISTS `contactgroup`;
CREATE TABLE `contactgroup` (
  `contact_id` int(8) NOT NULL,
  `group_id` int(8) NOT NULL,
  PRIMARY KEY  (`contact_id`, `group_id`),
  KEY `contactgroup_contact_id_contact_id_fkey` (`contact_id`),
  KEY `contactgroup_group_id_group_id_fkey` (`group_id`),
  CONSTRAINT `contactgroup_contact_id_contact_id_fkey` FOREIGN KEY (`contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contactgroup_group_id_group_id_fkey` FOREIGN KEY (`group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `_contactgroup`
--

DROP TABLE IF EXISTS `_contactgroup`;
CREATE TABLE `_contactgroup` (
  `contact_id` int(8) NOT NULL,
  `group_id` int(8) NOT NULL,
  PRIMARY KEY  (`contact_id`, `group_id`),
  KEY `_contactgroup_contact_id_contact_id_fkey` (`contact_id`),
  KEY `_contactgroup_group_id_group_id` (`group_id`),
  CONSTRAINT `_contactgroup_contact_id_contact_id_fkey` FOREIGN KEY (`contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `_contactgroup_group_id_group_id_fkey` FOREIGN KEY (`group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Diplay Prefs
--

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_lastname', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_phone', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_email', 4, 1);


--
-- Table structure for table `P__contactgroup`
--

DROP TABLE IF EXISTS `P__contactgroup`;
CREATE TABLE `P__contactgroup` (LIKE `_contactgroup`);


--
-- Table structure for table `CategoryLink`
--

DROP TABLE IF EXISTS `P_CategoryLink`;
CREATE TABLE `P_CategoryLink` (LIKE `CategoryLink`);


--
-- Domain Property
--
INSERT INTO DomainProperty VALUES ('mailshares_quota_default','integer','0','0');
INSERT INTO DomainProperty VALUES ('mailshares_quota_max','integer','0','0');


--
-- Table structure for table `field`
--
DROP TABLE IF EXISTS `field`;
CREATE TABLE `field` (
  `id`        int(8) NOT NULL auto_increment,
  `entity_id` int(8) NOT NULL,
  `field`      varchar(255),
  `value`     text,
  PRIMARY KEY (`id`),
  KEY `field_entity_id_fkey` (`entity_id`),
  CONSTRAINT `field_entity_id_fkey` FOREIGN KEY (`entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `P_field`
--

DROP TABLE IF EXISTS `P_field`;
CREATE TABLE `P_field` (LIKE `field`);

--
-- Resource group delegation
--
ALTER TABLE RGroup ADD COLUMN rgroup_delegation varchar(256) default '';


--
-- update category_code length to 20
-- add unicity constraint
--
ALTER TABLE `Category` MODIFY COLUMN `category_code` varchar(100) NOT NULL default '';
ALTER TABLE `Category` ADD CONSTRAINT UNIQUE `categorycategory_categorycode_uniquekey` (`category_domain_id`,`category_category`,`category_code`,`category_label`);

--
-- possibility to save special informations into an event template :
-- - the forced insertion state
-- - the availability of attendees and resources
-- - the checked state of the show users calendars
--
ALTER TABLE `EventTemplate` ADD COLUMN `eventtemplate_force_insertion` boolean default 0 AFTER eventtemplate_group_ids;
ALTER TABLE `EventTemplate` ADD COLUMN `eventtemplate_opacity` enum('OPAQUE','TRANSPARENT') default 'OPAQUE' AFTER eventtemplate_force_insertion;
ALTER TABLE `EventTemplate` ADD COLUMN `eventtemplate_show_user_calendar` boolean default 0 AFTER eventtemplate_opacity;
ALTER TABLE `EventTemplate` ADD COLUMN `eventtemplate_show_resource_calendar` boolean default 0 AFTER eventtemplate_show_user_calendar;

SET FOREIGN_KEY_CHECKS = 0;

-- store last sync dates
DROP TABLE IF EXISTS `opush_sync_state`;
CREATE TABLE `opush_sync_state` (
        `id` INTEGER NOT NULL auto_increment,
        `sync_key`        VARCHAR(64) UNIQUE NOT NULL,
        `collection_id`   INTEGER NOT NULL,
        `device_id`       INTEGER NOT NULL,
        `last_sync`       TIMESTAMP NOT NULL,
	PRIMARY KEY  (`id`),
        KEY `opush_sync_state_collection_id_opush_folder_mapping_id_fkey` (`collection_id`),
        KEY `opush_sync_state_device_id_opush_device_id_fkey` (`device_id`),
        CONSTRAINT `opush_sync_state_collection_id_opush_folder_mapping_id_fkey` FOREIGN KEY (`collection_id`) REFERENCES `opush_folder_mapping` (`id`) ON DELETE CASCADE,
        CONSTRAINT `opush_sync_state_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `opush_synced_item` (
       `id` INTEGER NOT NULL auto_increment,
       `sync_state_id`	  INTEGER NOT NULL,
       `item_id`          INTEGER NOT NULL,
       PRIMARY KEY  (`id`),
       KEY `opush_synced_item_sync_state_id_opush_sync_state_id_fkey` (`sync_state_id`),
       CONSTRAINT `opush_synced_item_sync_state_id_opush_sync_state_id_fkey` FOREIGN KEY (`sync_state_id`) REFERENCES `opush_sync_state` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `opush_event_mapping` (
       `id`              INTEGER NOT NULL auto_increment,
       `device_id`       INTEGER NOT NULL,
       `event_id`        INTEGER NOT NULL,
       `event_uid`       VARCHAR(300) NOT NULL,
       PRIMARY KEY  (`id`),
       KEY `opush_event_mapping_device_id_opush_device_id_fkey` (`device_id`),
       KEY `opush_event_mapping_event_id_event_id_fkey` (`event_id`),
       CONSTRAINT `opush_event_mapping_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE,
       CONSTRAINT `opush_event_mapping_event_id_event_id_fkey` FOREIGN KEY (`event_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE,
       CONSTRAINT `opush_event_mapping_unique` UNIQUE KEY (`device_id`,`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELETE FROM `opush_invitation_mapping`;
ALTER TABLE `opush_invitation_mapping` MODIFY COLUMN `event_uid` INTEGER NOT NULL;
ALTER TABLE `opush_invitation_mapping` ADD CONSTRAINT `opush_invitation_mapping_event_id_fkey` FOREIGN KEY (`event_uid`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE;

UPDATE Event SET event_ext_id=UUID() WHERE event_ext_id IS NULL;
ALTER TABLE Event MODIFY event_ext_id varchar(300) NOT NULL;

ALTER TABLE `opush_synced_item` ADD COLUMN `addition` BOOLEAN;
UPDATE `opush_synced_item` SET `addition`='1';
ALTER TABLE `opush_synced_item` MODIFY `addition` BOOLEAN NOT NULL;

-- ----------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
-- ----------------------------------------------------------------------------
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';
