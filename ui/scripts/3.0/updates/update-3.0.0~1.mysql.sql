-- Write that the 2.5->3.0 has started
UPDATE ObmInfo SET obminfo_value='2.5.x->3.0.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `batch`
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `status` ENUM('IDLE', 'RUNNING', 'ERROR', 'SUCCESS') NOT NULL,
  `timecreate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `timecommit` timestamp,
  `domain` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_batch_domain_id_fkey` FOREIGN KEY (`domain`)
      REFERENCES Domain (`domain_id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `batch_operation`
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `status` ENUM('IDLE', 'RUNNING', 'ERROR', 'SUCCESS') NOT NULL,
  `timecreate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `timecommit` timestamp,
  `error` text,
  `resource_path` text NOT NULL,
  `body` text,
  `verb` ENUM('PUT', 'PATCH', 'GET', 'POST', 'DELETE') NOT NULL,
  `entity_type` ENUM('GROUP', 'USER', 'GROUP_MEMBERSHIP', 'USER_MEMBERSHIP') NOT NULL,
  `batch` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_operation_batch_fkey` FOREIGN KEY (`batch`)
      REFERENCES batch (`id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS batch_operation_param
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `param_key` text NOT NULL,
  `value` text NOT NULL,
  `operation` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_operation_param_operation_fkey` FOREIGN KEY (`operation`)
      REFERENCES batch_operation (`id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX batch_operation_param_operation_idx ON batch_operation_param(operation);
CREATE INDEX batch_operation_batch_idx ON batch_operation(batch);

UPDATE UserObm
SET userobm_ext_id = UUID()
WHERE userobm_ext_id IS NULL;

ALTER TABLE UserObm MODIFY userobm_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE UserObm ADD UNIQUE (userobm_domain_id, userobm_ext_id);

UPDATE P_UserObm pu
INNER JOIN UserObm u
ON u.userobm_id = pu.userobm_id
SET pu.userobm_ext_id = u.userobm_ext_id;

ALTER TABLE P_UserObm MODIFY userobm_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE P_UserObm ADD UNIQUE (userobm_domain_id, userobm_ext_id);

UPDATE UGroup
SET group_ext_id = UUID()
WHERE group_ext_id IS NULL;

ALTER TABLE UGroup MODIFY group_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE UGroup ADD UNIQUE (group_domain_id, group_ext_id);

UPDATE P_UGroup pug
INNER JOIN UGroup ug
ON ug.group_id = pug.group_id
SET pug.group_ext_id = ug.group_ext_id;

ALTER TABLE P_UGroup MODIFY group_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE P_UGroup ADD UNIQUE (group_domain_id, group_ext_id);

UPDATE ProfileProperty SET profileproperty_value = 'domain' WHERE profileproperty_value = 'admin';

-- -----------------------------------------------------------------------------

TRUNCATE TrustToken;

ALTER TABLE TrustToken DROP COLUMN login;

ALTER TABLE TrustToken ADD COLUMN userobm_id int(8) NOT NULL;
ALTER TABLE TrustToken
  ADD CONSTRAINT `TrustToken_userobm_id_fkey` FOREIGN KEY (`userobm_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE;

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
SELECT 'set_top_bar', 'yes' FROM Dual
WHERE NOT EXISTS (SELECT 1 FROM UserObmPref WHERE userobmpref_option='set_top_bar');

INSERT INTO UserObmPref (userobmpref_user_id, userobmpref_option, userobmpref_value)
SELECT userobm_id, 'set_top_bar', 'no' FROM UserObm;

-- Roundcube Webmail initial database structure


/*!40014  SET FOREIGN_KEY_CHECKS=0 */;

-- Table structure for table `session`

CREATE TABLE `rc_session` (
 `sess_id` varchar(128) NOT NULL,
 `created` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `ip` varchar(40) NOT NULL,
 `vars` mediumtext NOT NULL,
 PRIMARY KEY(`sess_id`),
 INDEX `changed_index` (`changed`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `users`

CREATE TABLE `rc_users` (
 `user_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `username` varchar(128) BINARY NOT NULL,
 `mail_host` varchar(128) NOT NULL,
 `created` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `last_login` datetime DEFAULT NULL,
 `language` varchar(5),
 `preferences` text,
 PRIMARY KEY(`user_id`),
 UNIQUE `username` (`username`, `mail_host`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `cache`

CREATE TABLE `rc_cache` (
 `user_id` int(10) UNSIGNED NOT NULL,
 `cache_key` varchar(128) /*!40101 CHARACTER SET ascii COLLATE ascii_general_ci */ NOT NULL ,
 `created` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `data` longtext NOT NULL,
 CONSTRAINT `user_id_fk_cache` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 INDEX `created_index` (`created`),
 INDEX `user_cache_index` (`user_id`,`cache_key`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `cache_index`

CREATE TABLE `rc_cache_index` (
 `user_id` int(10) UNSIGNED NOT NULL,
 `mailbox` varchar(255) BINARY NOT NULL,
 `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `valid` tinyint(1) NOT NULL DEFAULT '0',
 `data` longtext NOT NULL,
 CONSTRAINT `user_id_fk_cache_index` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 INDEX `changed_index` (`changed`),
 PRIMARY KEY (`user_id`, `mailbox`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `cache_thread`

CREATE TABLE `rc_cache_thread` (
 `user_id` int(10) UNSIGNED NOT NULL,
 `mailbox` varchar(255) BINARY NOT NULL,
 `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `data` longtext NOT NULL,
 CONSTRAINT `user_id_fk_cache_thread` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 INDEX `changed_index` (`changed`),
 PRIMARY KEY (`user_id`, `mailbox`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `cache_messages`

CREATE TABLE `rc_cache_messages` (
 `user_id` int(10) UNSIGNED NOT NULL,
 `mailbox` varchar(255) BINARY NOT NULL,
 `uid` int(11) UNSIGNED NOT NULL DEFAULT '0',
 `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `data` longtext NOT NULL,
 `flags` int(11) NOT NULL DEFAULT '0',
 CONSTRAINT `user_id_fk_cache_messages` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 INDEX `changed_index` (`changed`),
 PRIMARY KEY (`user_id`, `mailbox`, `uid`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `contacts`

CREATE TABLE `rc_contacts` (
 `contact_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `del` tinyint(1) NOT NULL DEFAULT '0',
 `name` varchar(128) NOT NULL DEFAULT '',
 `email` text NOT NULL,
 `firstname` varchar(128) NOT NULL DEFAULT '',
 `surname` varchar(128) NOT NULL DEFAULT '',
 `vcard` longtext NULL,
 `words` text NULL,
 `user_id` int(10) UNSIGNED NOT NULL,
 PRIMARY KEY(`contact_id`),
 CONSTRAINT `user_id_fk_contacts` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 INDEX `user_contacts_index` (`user_id`,`del`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;

-- Table structure for table `contactgroups`

CREATE TABLE `rc_contactgroups` (
  `contactgroup_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int(10) UNSIGNED NOT NULL,
  `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
  `del` tinyint(1) NOT NULL DEFAULT '0',
  `name` varchar(128) NOT NULL DEFAULT '',
  PRIMARY KEY(`contactgroup_id`),
  CONSTRAINT `user_id_fk_contactgroups` FOREIGN KEY (`user_id`)
    REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `contactgroups_user_index` (`user_id`,`del`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;

CREATE TABLE `rc_contactgroupmembers` (
  `contactgroup_id` int(10) UNSIGNED NOT NULL,
  `contact_id` int(10) UNSIGNED NOT NULL,
  `created` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
  PRIMARY KEY (`contactgroup_id`, `contact_id`),
  CONSTRAINT `contactgroup_id_fk_contactgroups` FOREIGN KEY (`contactgroup_id`)
    REFERENCES `rc_contactgroups`(`contactgroup_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contact_id_fk_contacts` FOREIGN KEY (`contact_id`)
    REFERENCES `rc_contacts`(`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `contactgroupmembers_contact_index` (`contact_id`)
) /*!40000 ENGINE=INNODB */;


-- Table structure for table `identities`

CREATE TABLE `rc_identities` (
 `identity_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `user_id` int(10) UNSIGNED NOT NULL,
 `changed` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
 `del` tinyint(1) NOT NULL DEFAULT '0',
 `standard` tinyint(1) NOT NULL DEFAULT '0',
 `name` varchar(128) NOT NULL,
 `organization` varchar(128) NOT NULL DEFAULT '',
 `email` varchar(128) NOT NULL,
 `reply-to` varchar(128) NOT NULL DEFAULT '',
 `bcc` varchar(128) NOT NULL DEFAULT '',
 `signature` text,
 `html_signature` tinyint(1) NOT NULL DEFAULT '0',
 PRIMARY KEY(`identity_id`),
 CONSTRAINT `user_id_fk_identities` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 INDEX `user_identities_index` (`user_id`, `del`),
 INDEX `email_identities_index` (`email`, `del`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `dictionary`

CREATE TABLE `rc_dictionary` (
  `user_id` int(10) UNSIGNED DEFAULT NULL,
  `language` varchar(5) NOT NULL,
  `data` longtext NOT NULL,
  CONSTRAINT `user_id_fk_dictionary` FOREIGN KEY (`user_id`)
    REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE `uniqueness` (`user_id`, `language`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `searches`

CREATE TABLE `rc_searches` (
 `search_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `user_id` int(10) UNSIGNED NOT NULL,
 `type` int(3) NOT NULL DEFAULT '0',
 `name` varchar(128) NOT NULL,
 `data` text,
 PRIMARY KEY(`search_id`),
 CONSTRAINT `user_id_fk_searches` FOREIGN KEY (`user_id`)
   REFERENCES `rc_users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 UNIQUE `uniqueness` (`user_id`, `type`, `name`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;


-- Table structure for table `system`

CREATE TABLE `rc_system` (
 `name` varchar(64) NOT NULL,
 `value` mediumtext,
 PRIMARY KEY(`name`)
) /*!40000 ENGINE=INNODB */ /*!40101 CHARACTER SET utf8 COLLATE utf8_general_ci */;

/*!40014 SET FOREIGN_KEY_CHECKS=1 */;

INSERT INTO rc_system (name, value) VALUES ('roundcube-version', '2013011700');



-- ----------------------------------------------------------------------------
-- Write that the 2.5->3.0 is completed
-- ----------------------------------------------------------------------------
UPDATE ObmInfo SET obminfo_value='3.0.0' WHERE obminfo_name='db_version';
