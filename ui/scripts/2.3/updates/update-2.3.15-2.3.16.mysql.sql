UPDATE ObmInfo SET obminfo_value = '2.3.16-pre' WHERE obminfo_name = 'db_version';
DROP TABLE IF EXISTS `MailingList`;
CREATE TABLE `MailingList` (
  `mailinglist_id` int(8) NOT NULL auto_increment,
  `mailinglist_domain_id` int(8) NOT NULL,
  `mailinglist_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mailinglist_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mailinglist_userupdate` int(8) default NULL,
  `mailinglist_usercreate` int(8) default NULL,
  `mailinglist_owner` int(8) NOT NULL,
  `mailinglist_name` varchar(64) NOT NULL,
  PRIMARY KEY  (`mailinglist_id`),
  KEY `linglist_domain_id_domain_id_fkey` (`mailinglist_domain_id`),
  KEY `mailinglist_userupdate_userobm_id_fkey` (`mailinglist_userupdate`),
  KEY `mailinglist_usercreate_userobm_id_fkey` (`mailinglist_usercreate`),
  KEY `mailinglist_owner_userobm_id_fkey` (`mailinglist_owner`),
  CONSTRAINT `mailinglist_usercreate_userobm_id_fkey` FOREIGN KEY (`mailinglist_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `mailinglist_domain_id_domain_id_fkey` FOREIGN KEY (`mailinglist_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mailinglist_userupdate_userobm_id_fkey` FOREIGN KEY (`mailinglist_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `mailinglist_owner_userobm_id_fkey` FOREIGN KEY (`mailinglist_owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `Email`
--

DROP TABLE IF EXISTS `MailingListEmail`;
CREATE TABLE `MailingListEmail` (
  `mailinglistemail_id` int(8) NOT NULL auto_increment,
  `mailinglistemail_mailinglist_id` int(8) NOT NULL,
  `mailinglistemail_label` varchar(255) NOT NULL,
  `mailinglistemail_address` varchar(255) NOT NULL,
  PRIMARY KEY  (`mailinglistemail_id`),
  KEY `mailinglistemail_mailinglist_id_mailinglist_id_fkey` (`mailinglistemail_mailinglist_id`),
  CONSTRAINT `mailinglistemail_mailinglist_id_mailinglist_id_fkey` FOREIGN KEY (`mailinglistemail_mailinglist_id`) REFERENCES `MailingList` (`mailinglist_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


UPDATE ObmInfo SET obminfo_value = '2.3.16' WHERE obminfo_name = 'db_version';
