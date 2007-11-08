-- MySQL dump 10.11
--
-- Host: localhost    Database: fnb
-- ------------------------------------------------------
-- Server version	5.0.45-Debian_1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `fnbl_client_mapping`
--

DROP TABLE IF EXISTS `fnbl_client_mapping`;
CREATE TABLE `fnbl_client_mapping` (
  `principal` bigint(20) NOT NULL,
  `sync_source` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `luid` varchar(200) character set utf8 collate utf8_bin NOT NULL,
  `guid` varchar(200) character set utf8 collate utf8_bin NOT NULL,
  `last_anchor` varchar(20) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`principal`,`sync_source`,`luid`,`guid`),
  KEY `fk_source_cm` (`sync_source`),
  CONSTRAINT `fk_principal_cm` FOREIGN KEY (`principal`) REFERENCES `fnbl_principal` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_source_cm` FOREIGN KEY (`sync_source`) REFERENCES `fnbl_sync_source` (`uri`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_client_mapping`
--

LOCK TABLES `fnbl_client_mapping` WRITE;
/*!40000 ALTER TABLE `fnbl_client_mapping` DISABLE KEYS */;
INSERT INTO `fnbl_client_mapping` VALUES (0,'cal','./2086797268','0','20070919T100656Z'),(0,'cal','./2086797271','1','20070919T100656Z'),(0,'cal','./2086797274','2','20070919T100656Z');
/*!40000 ALTER TABLE `fnbl_client_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_connector`
--

DROP TABLE IF EXISTS `fnbl_connector`;
CREATE TABLE `fnbl_connector` (
  `id` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `name` varchar(200) character set utf8 collate utf8_bin NOT NULL,
  `description` varchar(200) character set utf8 collate utf8_bin default NULL,
  `admin_class` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_connector`
--

LOCK TABLES `fnbl_connector` WRITE;
/*!40000 ALTER TABLE `fnbl_connector` DISABLE KEYS */;
INSERT INTO `fnbl_connector` VALUES ('foundation','FunambolFoundationConnector','Funambol Foundation Connector',NULL),('obm','Funambol OBM Connector','Funambol OBM Connector','');
/*!40000 ALTER TABLE `fnbl_connector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_connector_source_type`
--

DROP TABLE IF EXISTS `fnbl_connector_source_type`;
CREATE TABLE `fnbl_connector_source_type` (
  `connector` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `sourcetype` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  PRIMARY KEY  (`connector`,`sourcetype`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_connector_source_type`
--

LOCK TABLES `fnbl_connector_source_type` WRITE;
/*!40000 ALTER TABLE `fnbl_connector_source_type` DISABLE KEYS */;
INSERT INTO `fnbl_connector_source_type` VALUES ('foundation','calendar-foundation'),('foundation','contact-foundation'),('foundation','fs-foundation'),('foundation','sif-fs-foundation'),('obm','obm-calendar'),('obm','obm-contact');
/*!40000 ALTER TABLE `fnbl_connector_source_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_device`
--

DROP TABLE IF EXISTS `fnbl_device`;
CREATE TABLE `fnbl_device` (
  `id` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `description` varchar(255) default NULL,
  `type` varchar(255) default NULL,
  `client_nonce` varchar(255) default NULL,
  `server_nonce` varchar(255) default NULL,
  `server_password` varchar(255) default NULL,
  `timezone` varchar(32) default NULL,
  `convert_date` char(1) default NULL,
  `charset` varchar(16) default NULL,
  `address` varchar(50) default NULL,
  `msisdn` varchar(50) default NULL,
  `notification_builder` varchar(255) default NULL,
  `notification_sender` varchar(255) default NULL,
  `id_caps` bigint(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_device`
--

LOCK TABLES `fnbl_device` WRITE;
/*!40000 ALTER TABLE `fnbl_device` DISABLE KEYS */;
INSERT INTO `fnbl_device` VALUES ('IMEI:2100000a',NULL,NULL,'','','fnbl',NULL,'N','UTF-8',NULL,NULL,'com/funambol/server/notification/DSNotificationBuilder.xml','com/funambol/server/notification/PushSender.xml',0);
/*!40000 ALTER TABLE `fnbl_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_device_caps`
--

DROP TABLE IF EXISTS `fnbl_device_caps`;
CREATE TABLE `fnbl_device_caps` (
  `id` bigint(20) NOT NULL,
  `version` varchar(16) NOT NULL,
  `man` varchar(100) default NULL,
  `model` varchar(100) default NULL,
  `fwv` varchar(100) default NULL,
  `swv` varchar(100) default NULL,
  `hwv` varchar(100) default NULL,
  `utc` char(1) NOT NULL,
  `lo` char(1) NOT NULL,
  `noc` char(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_device_caps`
--

LOCK TABLES `fnbl_device_caps` WRITE;
/*!40000 ALTER TABLE `fnbl_device_caps` DISABLE KEYS */;
INSERT INTO `fnbl_device_caps` VALUES (0,'1.1','Nexthaus Corp','SyncJe for BlackBerry','','2.34','','Y','Y','Y');
/*!40000 ALTER TABLE `fnbl_device_caps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_device_datastore`
--

DROP TABLE IF EXISTS `fnbl_device_datastore`;
CREATE TABLE `fnbl_device_datastore` (
  `id` bigint(20) NOT NULL,
  `caps` bigint(20) default NULL,
  `sourceref` varchar(128) NOT NULL,
  `label` varchar(128) default NULL,
  `maxguidsize` int(11) default NULL,
  `dsmem` char(1) NOT NULL,
  `shs` char(1) NOT NULL,
  `synccap` varchar(32) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `ind_datastore` (`caps`),
  CONSTRAINT `fk_dev_datastore` FOREIGN KEY (`caps`) REFERENCES `fnbl_device_caps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_device_datastore`
--

LOCK TABLES `fnbl_device_datastore` WRITE;
/*!40000 ALTER TABLE `fnbl_device_datastore` DISABLE KEYS */;
INSERT INTO `fnbl_device_datastore` VALUES (0,0,'./tasks','./tasks',0,'N','N','1,2'),(1,0,'./email','./email',0,'N','N','1,2'),(2,0,'./contacts','./contacts',0,'N','N','1,2'),(3,0,'./calendar','./calendar',0,'N','N','1,2');
/*!40000 ALTER TABLE `fnbl_device_datastore` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_device_ext`
--

DROP TABLE IF EXISTS `fnbl_device_ext`;
CREATE TABLE `fnbl_device_ext` (
  `id` bigint(20) NOT NULL,
  `caps` bigint(20) default NULL,
  `xname` varchar(255) default NULL,
  `xvalue` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `ind_device_ext` (`caps`),
  CONSTRAINT `fk_dev_ext` FOREIGN KEY (`caps`) REFERENCES `fnbl_device_caps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_device_ext`
--

LOCK TABLES `fnbl_device_ext` WRITE;
/*!40000 ALTER TABLE `fnbl_device_ext` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_device_ext` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_ctcap`
--

DROP TABLE IF EXISTS `fnbl_ds_ctcap`;
CREATE TABLE `fnbl_ds_ctcap` (
  `id` bigint(20) NOT NULL,
  `datastore` bigint(20) NOT NULL,
  `type` varchar(64) NOT NULL,
  `version` varchar(16) NOT NULL,
  `field` char(1) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `ind_ctcap` (`datastore`),
  CONSTRAINT `fk_ds_ctcap` FOREIGN KEY (`datastore`) REFERENCES `fnbl_device_datastore` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_ctcap`
--

LOCK TABLES `fnbl_ds_ctcap` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_ctcap` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_ds_ctcap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_ctcap_prop`
--

DROP TABLE IF EXISTS `fnbl_ds_ctcap_prop`;
CREATE TABLE `fnbl_ds_ctcap_prop` (
  `id` bigint(20) NOT NULL,
  `ctcap` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `label` varchar(128) default NULL,
  `type` varchar(32) default NULL,
  `maxoccur` int(11) default NULL,
  `maxsize` int(11) default NULL,
  `truncated` char(1) NOT NULL,
  `valenum` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `ind_ctcap_prop` (`ctcap`),
  CONSTRAINT `fk_ds_ctcap_prop` FOREIGN KEY (`ctcap`) REFERENCES `fnbl_ds_ctcap` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_ctcap_prop`
--

LOCK TABLES `fnbl_ds_ctcap_prop` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_ctcap_prop` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_ds_ctcap_prop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_ctcap_prop_param`
--

DROP TABLE IF EXISTS `fnbl_ds_ctcap_prop_param`;
CREATE TABLE `fnbl_ds_ctcap_prop_param` (
  `property` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `label` varchar(128) default NULL,
  `type` varchar(32) default NULL,
  `valenum` varchar(255) default NULL,
  KEY `ind_ctcappropparam` (`property`),
  CONSTRAINT `fk_ctcap_propparam` FOREIGN KEY (`property`) REFERENCES `fnbl_ds_ctcap_prop` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_ctcap_prop_param`
--

LOCK TABLES `fnbl_ds_ctcap_prop_param` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_ctcap_prop_param` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_ds_ctcap_prop_param` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_cttype_rx`
--

DROP TABLE IF EXISTS `fnbl_ds_cttype_rx`;
CREATE TABLE `fnbl_ds_cttype_rx` (
  `datastore` bigint(20) NOT NULL,
  `type` varchar(64) NOT NULL,
  `version` varchar(16) NOT NULL,
  `preferred` char(1) NOT NULL,
  PRIMARY KEY  (`type`,`version`,`datastore`),
  KEY `ind_cttype_rx` (`datastore`),
  CONSTRAINT `fk_ds_cttype_rx` FOREIGN KEY (`datastore`) REFERENCES `fnbl_device_datastore` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_cttype_rx`
--

LOCK TABLES `fnbl_ds_cttype_rx` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_cttype_rx` DISABLE KEYS */;
INSERT INTO `fnbl_ds_cttype_rx` VALUES (1,'text/message','1.0','Y'),(0,'text/x-vcalendar','1.0','Y'),(3,'text/x-vcalendar','1.0','Y'),(2,'text/x-vcard','2.1','Y');
/*!40000 ALTER TABLE `fnbl_ds_cttype_rx` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_cttype_tx`
--

DROP TABLE IF EXISTS `fnbl_ds_cttype_tx`;
CREATE TABLE `fnbl_ds_cttype_tx` (
  `datastore` bigint(20) NOT NULL,
  `type` varchar(64) NOT NULL,
  `version` varchar(16) NOT NULL,
  `preferred` char(1) NOT NULL,
  PRIMARY KEY  (`type`,`version`,`datastore`),
  KEY `ind_cttype_tx` (`datastore`),
  CONSTRAINT `fk_ds_cttype_tx` FOREIGN KEY (`datastore`) REFERENCES `fnbl_device_datastore` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_cttype_tx`
--

LOCK TABLES `fnbl_ds_cttype_tx` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_cttype_tx` DISABLE KEYS */;
INSERT INTO `fnbl_ds_cttype_tx` VALUES (1,'text/message','1.0','Y'),(0,'text/x-vcalendar','1.0','Y'),(3,'text/x-vcalendar','1.0','Y'),(2,'text/x-vcard','2.1','Y');
/*!40000 ALTER TABLE `fnbl_ds_cttype_tx` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_filter_cap`
--

DROP TABLE IF EXISTS `fnbl_ds_filter_cap`;
CREATE TABLE `fnbl_ds_filter_cap` (
  `datastore` bigint(20) NOT NULL,
  `type` varchar(64) NOT NULL,
  `version` varchar(16) NOT NULL,
  `keywords` varchar(255) default NULL,
  `properties` varchar(255) default NULL,
  PRIMARY KEY  (`type`,`version`,`datastore`),
  KEY `ind_filter_cap` (`datastore`),
  CONSTRAINT `fk_ds_filter_cap` FOREIGN KEY (`datastore`) REFERENCES `fnbl_device_datastore` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_filter_cap`
--

LOCK TABLES `fnbl_ds_filter_cap` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_filter_cap` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_ds_filter_cap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_filter_rx`
--

DROP TABLE IF EXISTS `fnbl_ds_filter_rx`;
CREATE TABLE `fnbl_ds_filter_rx` (
  `datastore` bigint(20) NOT NULL,
  `type` varchar(64) NOT NULL,
  `version` varchar(16) NOT NULL,
  PRIMARY KEY  (`type`,`version`,`datastore`),
  KEY `ind_filter_rx` (`datastore`),
  CONSTRAINT `fk_ds_filter_rx` FOREIGN KEY (`datastore`) REFERENCES `fnbl_device_datastore` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_filter_rx`
--

LOCK TABLES `fnbl_ds_filter_rx` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_filter_rx` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_ds_filter_rx` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_ds_mem`
--

DROP TABLE IF EXISTS `fnbl_ds_mem`;
CREATE TABLE `fnbl_ds_mem` (
  `datastore` bigint(20) default NULL,
  `shared` char(1) NOT NULL,
  `maxmem` int(11) default NULL,
  `maxid` int(11) default NULL,
  KEY `ind_mem` (`datastore`),
  CONSTRAINT `fk_ds_mem` FOREIGN KEY (`datastore`) REFERENCES `fnbl_device_datastore` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_ds_mem`
--

LOCK TABLES `fnbl_ds_mem` WRITE;
/*!40000 ALTER TABLE `fnbl_ds_mem` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_ds_mem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_id`
--

DROP TABLE IF EXISTS `fnbl_id`;
CREATE TABLE `fnbl_id` (
  `idspace` varchar(30) character set utf8 collate utf8_bin NOT NULL,
  `counter` bigint(20) NOT NULL,
  `increment_by` int(11) default '100',
  PRIMARY KEY  (`idspace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_id`
--

LOCK TABLES `fnbl_id` WRITE;
/*!40000 ALTER TABLE `fnbl_id` DISABLE KEYS */;
INSERT INTO `fnbl_id` VALUES ('capability',100,100),('ctcap',0,100),('ctcap_property',0,100),('datastore',100,100),('device',0,100),('ext',0,100),('guid',3,100),('pim.id',100,100),('principal',100,100);
/*!40000 ALTER TABLE `fnbl_id` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_last_sync`
--

DROP TABLE IF EXISTS `fnbl_last_sync`;
CREATE TABLE `fnbl_last_sync` (
  `principal` bigint(20) NOT NULL,
  `sync_source` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `sync_type` int(11) NOT NULL,
  `status` int(11) default NULL,
  `last_anchor_server` varchar(20) character set utf8 collate utf8_bin default NULL,
  `last_anchor_client` varchar(20) character set utf8 collate utf8_bin default NULL,
  `start_sync` bigint(20) default NULL,
  `end_sync` bigint(20) default NULL,
  PRIMARY KEY  (`principal`,`sync_source`),
  KEY `fk_source` (`sync_source`),
  CONSTRAINT `fk_principal` FOREIGN KEY (`principal`) REFERENCES `fnbl_principal` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_source` FOREIGN KEY (`sync_source`) REFERENCES `fnbl_sync_source` (`uri`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_last_sync`
--

LOCK TABLES `fnbl_last_sync` WRITE;
/*!40000 ALTER TABLE `fnbl_last_sync` DISABLE KEYS */;
INSERT INTO `fnbl_last_sync` VALUES (0,'cal',200,200,'20070919T100912Z','1190196570190',1190196570119,1190196571401);
/*!40000 ALTER TABLE `fnbl_last_sync` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_module`
--

DROP TABLE IF EXISTS `fnbl_module`;
CREATE TABLE `fnbl_module` (
  `id` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `name` varchar(200) character set utf8 collate utf8_bin NOT NULL,
  `description` varchar(200) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_module`
--

LOCK TABLES `fnbl_module` WRITE;
/*!40000 ALTER TABLE `fnbl_module` DISABLE KEYS */;
INSERT INTO `fnbl_module` VALUES ('foundation','foundation','Funambol Foundation Connector'),('obm','obm','Funambol OBM Connector');
/*!40000 ALTER TABLE `fnbl_module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_module_connector`
--

DROP TABLE IF EXISTS `fnbl_module_connector`;
CREATE TABLE `fnbl_module_connector` (
  `module` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `connector` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  PRIMARY KEY  (`module`,`connector`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_module_connector`
--

LOCK TABLES `fnbl_module_connector` WRITE;
/*!40000 ALTER TABLE `fnbl_module_connector` DISABLE KEYS */;
INSERT INTO `fnbl_module_connector` VALUES ('foundation','foundation'),('obm','obm');
/*!40000 ALTER TABLE `fnbl_module_connector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_pim_address`
--

DROP TABLE IF EXISTS `fnbl_pim_address`;
CREATE TABLE `fnbl_pim_address` (
  `contact` bigint(20) NOT NULL default '0',
  `type` smallint(6) NOT NULL default '0',
  `street` varchar(128) character set utf8 collate utf8_bin default NULL,
  `city` varchar(64) character set utf8 collate utf8_bin default NULL,
  `state` varchar(64) character set utf8 collate utf8_bin default NULL,
  `postal_code` varchar(16) character set utf8 collate utf8_bin default NULL,
  `country` varchar(32) character set utf8 collate utf8_bin default NULL,
  `po_box` varchar(16) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`contact`,`type`),
  CONSTRAINT `fnbl_pim_address_ibfk_1` FOREIGN KEY (`contact`) REFERENCES `fnbl_pim_contact` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_pim_address`
--

LOCK TABLES `fnbl_pim_address` WRITE;
/*!40000 ALTER TABLE `fnbl_pim_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_pim_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_pim_calendar`
--

DROP TABLE IF EXISTS `fnbl_pim_calendar`;
CREATE TABLE `fnbl_pim_calendar` (
  `id` bigint(20) NOT NULL,
  `userid` varchar(255) character set utf8 collate utf8_bin default NULL,
  `last_update` bigint(20) default NULL,
  `status` char(1) default NULL,
  `type` smallint(6) default NULL,
  `all_day` char(1) default NULL,
  `body` text,
  `busy_status` smallint(6) default NULL,
  `categories` varchar(255) character set utf8 collate utf8_bin default NULL,
  `companies` varchar(255) character set utf8 collate utf8_bin default NULL,
  `birthday` varchar(16) character set utf8 collate utf8_bin default NULL,
  `duration` int(11) default NULL,
  `dstart` datetime default NULL,
  `dend` datetime default NULL,
  `folder` varchar(255) character set utf8 collate utf8_bin default NULL,
  `importance` smallint(6) default NULL,
  `location` varchar(255) character set utf8 collate utf8_bin default NULL,
  `meeting_status` smallint(6) default NULL,
  `mileage` varchar(16) character set utf8 collate utf8_bin default NULL,
  `reminder_time` datetime default NULL,
  `reminder` char(1) default NULL,
  `reminder_sound_file` varchar(255) character set utf8 collate utf8_bin default NULL,
  `reminder_options` int(11) default NULL,
  `reminder_repeat_count` int(11) default NULL,
  `sensitivity` smallint(6) default NULL,
  `subject` varchar(1000) character set utf8 collate utf8_bin default NULL,
  `rec_type` smallint(6) default NULL,
  `rec_interval` int(11) default NULL,
  `rec_month_of_year` smallint(6) default NULL,
  `rec_day_of_month` smallint(6) default NULL,
  `rec_day_of_week_mask` varchar(16) character set utf8 collate utf8_bin default NULL,
  `rec_instance` smallint(6) default NULL,
  `rec_start_date_pattern` varchar(32) character set utf8 collate utf8_bin default NULL,
  `rec_no_end_date` char(1) default NULL,
  `rec_end_date_pattern` varchar(32) character set utf8 collate utf8_bin default NULL,
  `rec_occurrences` smallint(6) default NULL,
  `reply_time` datetime default NULL,
  `completed` datetime default NULL,
  `percent_complete` smallint(6) default NULL,
  PRIMARY KEY  (`id`),
  KEY `ind_pim_calendar` (`userid`,`last_update`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_pim_calendar`
--

LOCK TABLES `fnbl_pim_calendar` WRITE;
/*!40000 ALTER TABLE `fnbl_pim_calendar` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_pim_calendar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_pim_calendar_exception`
--

DROP TABLE IF EXISTS `fnbl_pim_calendar_exception`;
CREATE TABLE `fnbl_pim_calendar_exception` (
  `calendar` bigint(20) NOT NULL default '0',
  `addition` char(1) NOT NULL default '',
  `occurrence_date` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`calendar`,`addition`,`occurrence_date`),
  CONSTRAINT `fnbl_pim_calendar_exception_ibfk_1` FOREIGN KEY (`calendar`) REFERENCES `fnbl_pim_calendar` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_pim_calendar_exception`
--

LOCK TABLES `fnbl_pim_calendar_exception` WRITE;
/*!40000 ALTER TABLE `fnbl_pim_calendar_exception` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_pim_calendar_exception` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_pim_contact`
--

DROP TABLE IF EXISTS `fnbl_pim_contact`;
CREATE TABLE `fnbl_pim_contact` (
  `id` bigint(20) NOT NULL,
  `userid` varchar(255) character set utf8 collate utf8_bin default NULL,
  `last_update` bigint(20) default NULL,
  `status` char(1) default NULL,
  `importance` smallint(6) default NULL,
  `sensitivity` smallint(6) default NULL,
  `subject` varchar(255) character set utf8 collate utf8_bin default NULL,
  `folder` varchar(255) character set utf8 collate utf8_bin default NULL,
  `anniversary` varchar(16) character set utf8 collate utf8_bin default NULL,
  `first_name` varchar(64) character set utf8 collate utf8_bin default NULL,
  `middle_name` varchar(64) character set utf8 collate utf8_bin default NULL,
  `last_name` varchar(64) character set utf8 collate utf8_bin default NULL,
  `display_name` varchar(128) character set utf8 collate utf8_bin default NULL,
  `birthday` varchar(16) character set utf8 collate utf8_bin default NULL,
  `body` text,
  `categories` varchar(255) character set utf8 collate utf8_bin default NULL,
  `children` varchar(255) character set utf8 collate utf8_bin default NULL,
  `hobbies` varchar(255) character set utf8 collate utf8_bin default NULL,
  `initials` varchar(16) character set utf8 collate utf8_bin default NULL,
  `languages` varchar(255) character set utf8 collate utf8_bin default NULL,
  `nickname` varchar(64) character set utf8 collate utf8_bin default NULL,
  `spouse` varchar(128) character set utf8 collate utf8_bin default NULL,
  `suffix` varchar(32) character set utf8 collate utf8_bin default NULL,
  `title` varchar(32) character set utf8 collate utf8_bin default NULL,
  `gender` char(1) default NULL,
  `assistant` varchar(128) character set utf8 collate utf8_bin default NULL,
  `company` varchar(255) character set utf8 collate utf8_bin default NULL,
  `department` varchar(255) character set utf8 collate utf8_bin default NULL,
  `job_title` varchar(128) character set utf8 collate utf8_bin default NULL,
  `manager` varchar(128) character set utf8 collate utf8_bin default NULL,
  `mileage` varchar(16) character set utf8 collate utf8_bin default NULL,
  `office_location` varchar(64) character set utf8 collate utf8_bin default NULL,
  `profession` varchar(64) character set utf8 collate utf8_bin default NULL,
  `companies` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`id`),
  KEY `ind_pim_contact` (`userid`,`last_update`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_pim_contact`
--

LOCK TABLES `fnbl_pim_contact` WRITE;
/*!40000 ALTER TABLE `fnbl_pim_contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_pim_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_pim_contact_item`
--

DROP TABLE IF EXISTS `fnbl_pim_contact_item`;
CREATE TABLE `fnbl_pim_contact_item` (
  `contact` bigint(20) NOT NULL default '0',
  `type` smallint(6) NOT NULL default '0',
  `value` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`contact`,`type`),
  CONSTRAINT `fnbl_pim_contact_item_ibfk_1` FOREIGN KEY (`contact`) REFERENCES `fnbl_pim_contact` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_pim_contact_item`
--

LOCK TABLES `fnbl_pim_contact_item` WRITE;
/*!40000 ALTER TABLE `fnbl_pim_contact_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_pim_contact_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_pim_listener_registry`
--

DROP TABLE IF EXISTS `fnbl_pim_listener_registry`;
CREATE TABLE `fnbl_pim_listener_registry` (
  `id` bigint(20) NOT NULL,
  `username` varchar(50) character set utf8 collate utf8_bin default NULL,
  `push_contacts` char(1) default NULL,
  `push_calendars` char(1) default NULL,
  PRIMARY KEY  (`id`),
  CONSTRAINT `fnbl_pim_listener_registry_ibfk_1` FOREIGN KEY (`id`) REFERENCES `fnbl_push_listener_registry` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_pim_listener_registry`
--

LOCK TABLES `fnbl_pim_listener_registry` WRITE;
/*!40000 ALTER TABLE `fnbl_pim_listener_registry` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_pim_listener_registry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_principal`
--

DROP TABLE IF EXISTS `fnbl_principal`;
CREATE TABLE `fnbl_principal` (
  `username` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `device` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_device` (`device`),
  KEY `ind_principal` (`username`,`device`),
  CONSTRAINT `fk_device` FOREIGN KEY (`device`) REFERENCES `fnbl_device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_principal`
--

LOCK TABLES `fnbl_principal` WRITE;
/*!40000 ALTER TABLE `fnbl_principal` DISABLE KEYS */;
INSERT INTO `fnbl_principal` VALUES ('guest','IMEI:2100000a',0);
/*!40000 ALTER TABLE `fnbl_principal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_push_listener_registry`
--

DROP TABLE IF EXISTS `fnbl_push_listener_registry`;
CREATE TABLE `fnbl_push_listener_registry` (
  `id` bigint(20) NOT NULL,
  `period` bigint(20) default NULL,
  `active` char(1) default NULL,
  `last_update` bigint(20) default NULL,
  `status` varchar(1) character set utf8 collate utf8_bin default NULL,
  `task_bean_file` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_push_listener_registry`
--

LOCK TABLES `fnbl_push_listener_registry` WRITE;
/*!40000 ALTER TABLE `fnbl_push_listener_registry` DISABLE KEYS */;
/*!40000 ALTER TABLE `fnbl_push_listener_registry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_role`
--

DROP TABLE IF EXISTS `fnbl_role`;
CREATE TABLE `fnbl_role` (
  `role` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `description` varchar(200) character set utf8 collate utf8_bin NOT NULL,
  PRIMARY KEY  (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_role`
--

LOCK TABLES `fnbl_role` WRITE;
/*!40000 ALTER TABLE `fnbl_role` DISABLE KEYS */;
INSERT INTO `fnbl_role` VALUES ('sync_administrator','Administrator'),('sync_user','User');
/*!40000 ALTER TABLE `fnbl_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_sync_source`
--

DROP TABLE IF EXISTS `fnbl_sync_source`;
CREATE TABLE `fnbl_sync_source` (
  `uri` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `config` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `name` varchar(200) character set utf8 collate utf8_bin NOT NULL,
  `sourcetype` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  PRIMARY KEY  (`uri`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_sync_source`
--

LOCK TABLES `fnbl_sync_source` WRITE;
/*!40000 ALTER TABLE `fnbl_sync_source` DISABLE KEYS */;
INSERT INTO `fnbl_sync_source` VALUES ('cal','foundation/foundation/calendar-foundation/VCalendarSource.xml','cal','calendar-foundation'),('obm_addressbook','obm/obm/obm-contact/obm_addressbook.xml','obm_addressbook','obm-contact'),('obm_book_vcard','obm/obm/obm-contact/obm_book_vcard.xml','obm_book_vcard','obm-contact'),('obm_cal_ical','obm/obm/obm-calendar/obm_cal_ical.xml','obm_cal_ical','obm-calendar'),('obm_calendar','obm/obm/obm-calendar/obm_calendar.xml','obm_calendar','obm-calendar');
/*!40000 ALTER TABLE `fnbl_sync_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_sync_source_type`
--

DROP TABLE IF EXISTS `fnbl_sync_source_type`;
CREATE TABLE `fnbl_sync_source_type` (
  `id` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  `description` varchar(200) character set utf8 collate utf8_bin default NULL,
  `class` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `admin_class` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_sync_source_type`
--

LOCK TABLES `fnbl_sync_source_type` WRITE;
/*!40000 ALTER TABLE `fnbl_sync_source_type` DISABLE KEYS */;
INSERT INTO `fnbl_sync_source_type` VALUES ('calendar-foundation','PIM Calendar SyncSource','com.funambol.foundation.engine.source.PIMCalendarSyncSource','com.funambol.foundation.admin.PIMCalendarSyncSourceConfigPanel'),('contact-foundation','PIM Contact SyncSource','com.funambol.foundation.engine.source.PIMContactSyncSource','com.funambol.foundation.admin.PIMContactSyncSourceConfigPanel'),('fs-foundation','FileSystem SyncSource','com.funambol.foundation.engine.source.FileSystemSyncSource','com.funambol.foundation.admin.FileSystemSyncSourceConfigPanel'),('obm-calendar','OBM Calendar SyncSource','fr.aliasource.funambol.engine.source.CalendarSyncSource','fr.aliasource.funambol.admin.ObmSyncSourceConfigPanel'),('obm-contact','OBM Contact SyncSource','fr.aliasource.funambol.engine.source.ContactSyncSource','fr.aliasource.funambol.admin.ObmSyncSourceConfigPanel'),('sif-fs-foundation','SIF SyncSource','com.funambol.foundation.engine.source.SIFSyncSource','com.funambol.foundation.admin.SIFSyncSourceConfigPanel');
/*!40000 ALTER TABLE `fnbl_sync_source_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_user`
--

DROP TABLE IF EXISTS `fnbl_user`;
CREATE TABLE `fnbl_user` (
  `username` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `password` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `email` varchar(255) character set utf8 collate utf8_bin default NULL,
  `first_name` varchar(255) character set utf8 collate utf8_bin default NULL,
  `last_name` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY  (`username`),
  KEY `ind_user` (`username`,`password`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_user`
--

LOCK TABLES `fnbl_user` WRITE;
/*!40000 ALTER TABLE `fnbl_user` DISABLE KEYS */;
INSERT INTO `fnbl_user` VALUES ('admin','lltUbBHM7oA=','admin@funambol.com','admin','admin'),('guest','65GUmi03K6o=','guest@funambol.com','guest','guest');
/*!40000 ALTER TABLE `fnbl_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fnbl_user_role`
--

DROP TABLE IF EXISTS `fnbl_user_role`;
CREATE TABLE `fnbl_user_role` (
  `username` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `role` varchar(128) character set utf8 collate utf8_bin NOT NULL,
  PRIMARY KEY  (`username`,`role`),
  CONSTRAINT `fk_userrole` FOREIGN KEY (`username`) REFERENCES `fnbl_user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `fnbl_user_role`
--

LOCK TABLES `fnbl_user_role` WRITE;
/*!40000 ALTER TABLE `fnbl_user_role` DISABLE KEYS */;
INSERT INTO `fnbl_user_role` VALUES ('admin','sync_administrator'),('guest','sync_user');
/*!40000 ALTER TABLE `fnbl_user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2007-09-19 12:47:20
