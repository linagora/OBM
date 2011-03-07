UPDATE ObmInfo SET obminfo_value = '2.3.17-pre' WHERE obminfo_name = 'db_version';

CREATE TABLE `DeletedSyncedAddressbook` (
  `user_id`        int(8) NOT NULL,
  `addressbook_id` int(8) NOT NULL,
  `timestamp`      timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`user_id`, `addressbook_id`),
  KEY `DeletedSyncedAddressbook_user_id_user_id_fkey` (`user_id`),
  KEY `DeletedSyncedAddressbook_addressbook_id_addressbook_id_fkey` (`addressbook_id`),
  CONSTRAINT `DeletedSyncedAddressbook_user_id_userobm_id_fkey` FOREIGN KEY (user_id) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT DeletedSyncedAddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES AddressBook (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


UPDATE ObmInfo SET obminfo_value = '2.3.17' WHERE obminfo_name = 'db_version';
