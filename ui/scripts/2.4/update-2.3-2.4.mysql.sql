-- Write that the 2.3->2.4 has started
UPDATE ObmInfo SET obminfo_value='2.3.x->2.4.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------


-- FIXME: put upgrades here
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

-- ----------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';




