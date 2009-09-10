-- Write that the 2.2->2.3 has started
UPDATE ObmInfo SET obminfo_value='2.2.x->2.3.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------

--
-- Table structure for table `addressbook`
--
DROP TABLE IF EXISTS `addressbook`;
CREATE TABLE `addressbook` (
  `id`         int(8) NOT NULL auto_increment,
  `domain_id`  int(8) NOT NULL,
  `timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userupdate` int(8) default NULL,
  `usercreate` int(8) default NULL,
  `origin`     varchar(255) NOT NULL,
  `owner`      int(8) default NULL,
  `name`       varchar(64) NOT NULL,
  `is_default`    int(1) default 0,
  `syncable`   int(1) default 1,
  PRIMARY KEY (`id`),
  KEY `addressbook_domain_id_domain_id_fkey` (`domain_id`),
  KEY `addressbook_userupdate_userobm_id_fkey` (`userupdate`),
  KEY `addressbook_usercreate_userobm_id_fkey` (`usercreate`),
  KEY `addressbook_owner_userobm_id_fkey` (`owner`),
  CONSTRAINT `addressbook_domain_id_domain_id_fkey` FOREIGN KEY (`domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `addressbook_userupdate_userobm_id_fkey` FOREIGN KEY (`userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `addressbook_usercreate_userobm_id_fkey` FOREIGN KEY (`usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `addressbook_owner_userobm_id_fkey` FOREIGN KEY (`owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `addressbookEntity`
--
DROP TABLE IF EXISTS `AddressbookEntity`;
CREATE TABLE `AddressbookEntity` (
  `addressbookentity_entity_id`      int(8) NOT NULL,
  `addressbookentity_addressbook_id` int(8) NOT NULL,
  PRIMARY KEY (`addressbookentity_entity_id`,`addressbookentity_addressbook_id`),
  KEY `addressbookentity_addressbook_id_addressbook_id_fkey` (`addressbookentity_addressbook_id`),
  CONSTRAINT addressbookentity_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbookentity_addressbook_id) REFERENCES addressbook (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `addressbookentity_entity_id_entity_id_fkey` FOREIGN KEY (`addressbookentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Contact update
--
ALTER TABLE Contact ADD COLUMN contact_addressbook_id int(8) default NULL AFTER contact_datasource_id; 
ALTER TABLE Contact ADD CONSTRAINT contact_addressbook_id_addressbook_id_fkey FOREIGN KEY (contact_addressbook_id) REFERENCES addressbook (id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE Contact DROP COLUMN contact_privacy;


--
-- Table structure for table `DeletedAddressbook`
--
DROP TABLE IF EXISTS `DeletedAddressbook`;
CREATE TABLE `DeletedAddressbook` (
  `addressbook_id` int(8) NOT NULL,
  `user_id`        int(8) NOT NULL,
  `timestamp`      timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `origin`         varchar(255) NOT NULL,
  PRIMARY KEY (`addressbook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `SyncedAddressbook`
--
DROP TABLE IF EXISTS `SyncedAddressbook`;
CREATE TABLE `SyncedAddressbook` (
  `user_id`        int(8) NOT NULL,
  `addressbook_id` int(8) NOT NULL,
  `timestamp`      timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`user_id`, `addressbook_id`),
  KEY `syncedaddressbook_user_id_user_id_fkey` (`user_id`),
  KEY `syncedaddressbook_addressbook_id_addressbook_id_fkey` (`addressbook_id`),
  CONSTRAINT `syncedaddressbook_user_id_userobm_id_fkey` FOREIGN KEY (user_id) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT syncedaddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES addressbook (id) ON DELETE CASCADE ON UPDATE CASCADE
);

DROP TABLE IF EXISTS `SynchedContact`;

-- 
-- Table structure for table `EventTag`
-- 
DROP TABLE IF EXISTS `EventTag` ;
CREATE TABLE `EventTag` ( 
  `eventtag_id` int(8) NOT NULL auto_increment, 
  `eventtag_user_id` int(8) NOT NULL, 
  `eventtag_label` varchar(128) default NULL, 
  `eventtag_color` char(7) default NULL, 
  PRIMARY KEY  (`eventtag_id`), 
  KEY `eventtag_label_fkey` (`eventtag_label`),
	KEY `eventtag_user_id_userobm_id_fkey` (`eventtag_user_id`), 
  CONSTRAINT `eventtag_user_id_userobm_id_fkey` FOREIGN KEY (`eventtag_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

-- 
-- Alter table `Event` to add tag infos
-- 
ALTER TABLE `Event` ADD COLUMN `event_tag_id` int(8) ; 
ALTER TABLE `Event` ADD KEY `event_tag_id_eventtag_id_fkey` (`event_tag_id`) ;
ALTER TABLE `Event` ADD CONSTRAINT `event_tag_id_eventtag_id_fkey` FOREIGN KEY (`event_tag_id`) REFERENCES `EventTag` (`eventtag_id`) ON DELETE SET NULL ON UPDATE CASCADE ;

-- 
-- Alter `Event` and `DocumentLink` for document attachment
-- 
ALTER TABLE `Event` ADD COLUMN `event_allow_documents` tinyint(1) NULL DEFAULT '0' AFTER `event_url`;
ALTER TABLE `DocumentLink` ADD COLUMN `documentlink_usercreate` int(8) NULL DEFAULT NULL;
ALTER TABLE `DocumentLink` ADD KEY `documentlink_usercreate_userobm_id_fkey` (`documentlink_usercreate`) ;
ALTER TABLE `DocumentLink` ADD CONSTRAINT `documentlink_usercreate_userobm_id_fkey` FOREIGN KEY (`documentlink_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- ----------------------------------------------------------------------------
-- Write that the 2.2->2.3 is completed
UPDATE ObmInfo SET obminfo_value='2.3.0' WHERE obminfo_name='db_version';

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_direction', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_service', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_address', 13, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_town', 14, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_zipcode', 15, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'user', 'userobm_direction', 26, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'user', 'userobm_service', 27, 1);
