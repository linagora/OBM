-- MySQL dump 10.13  Distrib 5.1.66, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: obm
-- ------------------------------------------------------
-- Server version	5.1.66-0+squeeze1

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
-- Table structure for table `Account`
--

DROP TABLE IF EXISTS `Account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Account` (
  `account_id` int(8) NOT NULL AUTO_INCREMENT,
  `account_domain_id` int(8) NOT NULL,
  `account_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `account_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `account_userupdate` int(8) DEFAULT NULL,
  `account_usercreate` int(8) DEFAULT NULL,
  `account_bank` varchar(60) NOT NULL DEFAULT '',
  `account_number` varchar(64) NOT NULL DEFAULT '0',
  `account_balance` double(15,2) NOT NULL DEFAULT '0.00',
  `account_today` double(15,2) NOT NULL DEFAULT '0.00',
  `account_comment` varchar(100) DEFAULT NULL,
  `account_label` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`account_id`),
  KEY `account_domain_id_domain_id_fkey` (`account_domain_id`),
  KEY `account_usercreate_userobm_id_fkey` (`account_usercreate`),
  KEY `account_userupdate_userobm_id_fkey` (`account_userupdate`),
  CONSTRAINT `account_userupdate_userobm_id_fkey` FOREIGN KEY (`account_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `account_domain_id_domain_id_fkey` FOREIGN KEY (`account_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `account_usercreate_userobm_id_fkey` FOREIGN KEY (`account_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AccountEntity`
--

DROP TABLE IF EXISTS `AccountEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AccountEntity` (
  `accountentity_entity_id` int(8) NOT NULL,
  `accountentity_account_id` int(8) NOT NULL,
  PRIMARY KEY (`accountentity_entity_id`,`accountentity_account_id`),
  KEY `accountentity_account_id_account_id_fkey` (`accountentity_account_id`),
  CONSTRAINT `accountentity_account_id_account_id_fkey` FOREIGN KEY (`accountentity_account_id`) REFERENCES `Account` (`account_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `accountentity_entity_id_entity_id_fkey` FOREIGN KEY (`accountentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ActiveUserObm`
--

DROP TABLE IF EXISTS `ActiveUserObm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ActiveUserObm` (
  `activeuserobm_sid` varchar(32) NOT NULL DEFAULT '',
  `activeuserobm_session_name` varchar(32) NOT NULL DEFAULT '',
  `activeuserobm_userobm_id` int(11) DEFAULT NULL,
  `activeuserobm_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activeuserobm_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `activeuserobm_nb_connexions` int(11) NOT NULL DEFAULT '0',
  `activeuserobm_lastpage` varchar(64) NOT NULL DEFAULT '0',
  `activeuserobm_ip` varchar(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (`activeuserobm_sid`),
  KEY `activeuserobm_userobm_id_userobm_id_fkey` (`activeuserobm_userobm_id`),
  CONSTRAINT `activeuserobm_userobm_id_userobm_id_fkey` FOREIGN KEY (`activeuserobm_userobm_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Address`
--

DROP TABLE IF EXISTS `Address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Address` (
  `address_id` int(8) NOT NULL AUTO_INCREMENT,
  `address_entity_id` int(8) NOT NULL,
  `address_street` text,
  `address_zipcode` varchar(14) DEFAULT NULL,
  `address_town` varchar(128) DEFAULT NULL,
  `address_state` varchar(128) DEFAULT NULL,
  `address_expresspostal` varchar(16) DEFAULT NULL,
  `address_country` char(2) DEFAULT NULL,
  `address_label` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`address_id`),
  KEY `address_entity_id_entity_id_fkey` (`address_entity_id`),
  CONSTRAINT `address_entity_id_entity_id_fkey` FOREIGN KEY (`address_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AddressBook`
--

DROP TABLE IF EXISTS `AddressBook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AddressBook` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `domain_id` int(8) NOT NULL,
  `timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userupdate` int(8) DEFAULT NULL,
  `usercreate` int(8) DEFAULT NULL,
  `origin` varchar(255) NOT NULL,
  `owner` int(8) DEFAULT NULL,
  `name` varchar(64) NOT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `syncable` tinyint(1) DEFAULT '1',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AddressbookEntity`
--

DROP TABLE IF EXISTS `AddressbookEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AddressbookEntity` (
  `addressbookentity_entity_id` int(8) NOT NULL,
  `addressbookentity_addressbook_id` int(8) NOT NULL,
  PRIMARY KEY (`addressbookentity_entity_id`,`addressbookentity_addressbook_id`),
  KEY `addressbookentity_addressbook_id_addressbook_id_fkey` (`addressbookentity_addressbook_id`),
  CONSTRAINT `addressbookentity_addressbook_id_addressbook_id_fkey` FOREIGN KEY (`addressbookentity_addressbook_id`) REFERENCES `AddressBook` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `addressbookentity_entity_id_entity_id_fkey` FOREIGN KEY (`addressbookentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CV`
--

DROP TABLE IF EXISTS `CV`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CV` (
  `cv_id` int(8) NOT NULL AUTO_INCREMENT,
  `cv_domain_id` int(8) NOT NULL,
  `cv_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cv_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `cv_userupdate` int(8) DEFAULT NULL,
  `cv_usercreate` int(8) DEFAULT NULL,
  `cv_userobm_id` int(8) NOT NULL,
  `cv_title` varchar(255) DEFAULT NULL,
  `cv_additionnalrefs` text,
  `cv_comment` text,
  PRIMARY KEY (`cv_id`),
  KEY `cv_domain_id_domain_id_fkey` (`cv_domain_id`),
  KEY `cv_userobm_id_userobm_id_fkey` (`cv_userobm_id`),
  KEY `cv_userupdate_userobm_id_fkey` (`cv_userupdate`),
  KEY `cv_usercreate_userobm_id_fkey` (`cv_usercreate`),
  CONSTRAINT `cv_usercreate_userobm_id_fkey` FOREIGN KEY (`cv_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `cv_domain_id_domain_id_fkey` FOREIGN KEY (`cv_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `cv_userobm_id_userobm_id_fkey` FOREIGN KEY (`cv_userobm_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `cv_userupdate_userobm_id_fkey` FOREIGN KEY (`cv_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CalendarEntity`
--

DROP TABLE IF EXISTS `CalendarEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CalendarEntity` (
  `calendarentity_entity_id` int(8) NOT NULL,
  `calendarentity_calendar_id` int(8) NOT NULL,
  PRIMARY KEY (`calendarentity_entity_id`,`calendarentity_calendar_id`),
  KEY `calendarentity_calendar_id_calendar_id_fkey` (`calendarentity_calendar_id`),
  CONSTRAINT `calendarentity_calendar_id_calendar_id_fkey` FOREIGN KEY (`calendarentity_calendar_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `calendarentity_entity_id_entity_id_fkey` FOREIGN KEY (`calendarentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Campaign`
--

DROP TABLE IF EXISTS `Campaign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Campaign` (
  `campaign_id` int(8) NOT NULL AUTO_INCREMENT,
  `campaign_name` varchar(50) DEFAULT NULL,
  `campaign_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `campaign_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `campaign_userupdate` int(8) DEFAULT NULL,
  `campaign_usercreate` int(8) DEFAULT NULL,
  `campaign_manager_id` int(8) DEFAULT NULL,
  `campaign_tracker_key` int(11) DEFAULT NULL,
  `campaign_refer_url` varchar(255) DEFAULT NULL,
  `campaign_nb_sent` int(10) DEFAULT NULL,
  `campaign_nb_error` int(10) DEFAULT NULL,
  `campaign_nb_inqueue` int(10) DEFAULT NULL,
  `campaign_progress` int(3) DEFAULT NULL,
  `campaign_start_date` date DEFAULT NULL,
  `campaign_end_date` date DEFAULT NULL,
  `campaign_status` int(3) DEFAULT NULL,
  `campaign_type` int(2) DEFAULT NULL,
  `campaign_objective` text,
  `campaign_comment` text,
  `campaign_domain_id` int(8) NOT NULL,
  `campaign_email` int(8) DEFAULT NULL,
  `campaign_parent` int(8) DEFAULT NULL,
  `campaign_child_order` int(2) DEFAULT NULL,
  PRIMARY KEY (`campaign_id`),
  KEY `campaign_parent_fkey` (`campaign_parent`),
  KEY `campaign_email_fkey` (`campaign_email`),
  CONSTRAINT `campaign_parent_fkey` FOREIGN KEY (`campaign_parent`) REFERENCES `Campaign` (`campaign_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaign_email_fkey` FOREIGN KEY (`campaign_email`) REFERENCES `Document` (`document_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CampaignDisabledEntity`
--

DROP TABLE IF EXISTS `CampaignDisabledEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CampaignDisabledEntity` (
  `campaigndisabledentity_entity_id` int(8) NOT NULL,
  `campaigndisabledentity_campaign_id` int(8) NOT NULL,
  PRIMARY KEY (`campaigndisabledentity_entity_id`,`campaigndisabledentity_campaign_id`),
  KEY `campaigndisabledentity_campaign_id_campaign_id_fkey` (`campaigndisabledentity_campaign_id`),
  CONSTRAINT `campaigndisabledentity_campaign_id_campaign_id_fkey` FOREIGN KEY (`campaigndisabledentity_campaign_id`) REFERENCES `Campaign` (`campaign_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaigndisabledentity_entity_id_entity_id_fkey` FOREIGN KEY (`campaigndisabledentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CampaignEntity`
--

DROP TABLE IF EXISTS `CampaignEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CampaignEntity` (
  `campaignentity_entity_id` int(8) NOT NULL,
  `campaignentity_campaign_id` int(8) NOT NULL,
  PRIMARY KEY (`campaignentity_entity_id`,`campaignentity_campaign_id`),
  KEY `campaignentity_campaign_id_campaign_id_fkey` (`campaignentity_campaign_id`),
  CONSTRAINT `campaignentity_campaign_id_campaign_id_fkey` FOREIGN KEY (`campaignentity_campaign_id`) REFERENCES `Campaign` (`campaign_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaignentity_entity_id_entity_id_fkey` FOREIGN KEY (`campaignentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CampaignMailContent`
--

DROP TABLE IF EXISTS `CampaignMailContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CampaignMailContent` (
  `campaignmailcontent_id` int(8) NOT NULL AUTO_INCREMENT,
  `campaignmailcontent_refext_id` varchar(8) DEFAULT NULL,
  `campaignmailcontent_content` blob,
  PRIMARY KEY (`campaignmailcontent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CampaignMailTarget`
--

DROP TABLE IF EXISTS `CampaignMailTarget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CampaignMailTarget` (
  `campaignmailtarget_id` int(8) NOT NULL AUTO_INCREMENT,
  `campaignmailtarget_campaign_id` int(8) NOT NULL,
  `campaignmailtarget_entity_id` int(8) DEFAULT NULL,
  `campaignmailtarget_status` int(8) DEFAULT NULL,
  PRIMARY KEY (`campaignmailtarget_id`),
  KEY `campaignmailtarget_campaign_id_campaign_id_fkey` (`campaignmailtarget_campaign_id`),
  KEY `campaignmailtarget_entity_id_entity_id_fkey` (`campaignmailtarget_entity_id`),
  CONSTRAINT `campaignmailtarget_campaign_id_campaign_id_fkey` FOREIGN KEY (`campaignmailtarget_campaign_id`) REFERENCES `Campaign` (`campaign_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaignmailtarget_entity_id_entity_id_fkey` FOREIGN KEY (`campaignmailtarget_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CampaignPushTarget`
--

DROP TABLE IF EXISTS `CampaignPushTarget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CampaignPushTarget` (
  `campaignpushtarget_id` int(8) NOT NULL AUTO_INCREMENT,
  `campaignpushtarget_mailcontent_id` int(8) NOT NULL,
  `campaignpushtarget_refext_id` varchar(8) DEFAULT NULL,
  `campaignpushtarget_status` int(2) NOT NULL DEFAULT '1',
  `campaignpushtarget_email_address` varchar(512) NOT NULL,
  `campaignpushtarget_properties` text,
  `campaignpushtarget_start_time` datetime DEFAULT NULL,
  `campaignpushtarget_sent_time` datetime DEFAULT NULL,
  `campaignpushtarget_retries` int(3) DEFAULT NULL,
  PRIMARY KEY (`campaignpushtarget_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CampaignTarget`
--

DROP TABLE IF EXISTS `CampaignTarget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CampaignTarget` (
  `campaigntarget_id` int(8) NOT NULL AUTO_INCREMENT,
  `campaigntarget_campaign_id` int(8) NOT NULL,
  `campaigntarget_entity_id` int(8) DEFAULT NULL,
  `campaigntarget_status` int(8) DEFAULT NULL,
  PRIMARY KEY (`campaigntarget_id`),
  KEY `campaigntarget_campaign_id_campaign_id_fkey` (`campaigntarget_campaign_id`),
  KEY `campaigntarget_entity_id_entity_id_fkey` (`campaigntarget_entity_id`),
  CONSTRAINT `campaigntarget_campaign_id_campaign_id_fkey` FOREIGN KEY (`campaigntarget_campaign_id`) REFERENCES `Campaign` (`campaign_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaigntarget_entity_id_entity_id_fkey` FOREIGN KEY (`campaigntarget_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Category`
--

DROP TABLE IF EXISTS `Category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Category` (
  `category_id` int(8) NOT NULL AUTO_INCREMENT,
  `category_domain_id` int(8) DEFAULT NULL,
  `category_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `category_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `category_userupdate` int(8) DEFAULT NULL,
  `category_usercreate` int(8) DEFAULT NULL,
  `category_category` varchar(24) NOT NULL DEFAULT '',
  `category_code` varchar(100) NOT NULL DEFAULT '',
  `category_label` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `categorycategory_categorycode_uniquekey` (`category_domain_id`,`category_category`,`category_code`,`category_label`),
  KEY `cat_idx_cat` (`category_category`),
  KEY `category_domain_id_domain_id_fkey` (`category_domain_id`),
  KEY `category_userupdate_userobm_id_fkey` (`category_userupdate`),
  KEY `category_usercreate_userobm_id_fkey` (`category_usercreate`),
  CONSTRAINT `category_usercreate_userobm_id_fkey` FOREIGN KEY (`category_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `category_domain_id_domain_id_fkey` FOREIGN KEY (`category_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `category_userupdate_userobm_id_fkey` FOREIGN KEY (`category_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CategoryLink`
--

DROP TABLE IF EXISTS `CategoryLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CategoryLink` (
  `categorylink_category_id` int(8) NOT NULL,
  `categorylink_entity_id` int(8) NOT NULL,
  `categorylink_category` varchar(24) NOT NULL DEFAULT '',
  PRIMARY KEY (`categorylink_category_id`,`categorylink_entity_id`),
  KEY `catl_idx_ent` (`categorylink_entity_id`),
  KEY `catl_idx_cat` (`categorylink_category`),
  CONSTRAINT `categorylink_entity_id_entity_id_fkey` FOREIGN KEY (`categorylink_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `categorylink_category_id_category_id_fkey` FOREIGN KEY (`categorylink_category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CommitedOperation`
--

DROP TABLE IF EXISTS `CommitedOperation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CommitedOperation` (
  `commitedoperation_hash_client_id` varchar(44) NOT NULL,
  `commitedoperation_entity_id` int(11) NOT NULL,
  `commitedoperation_kind` enum('VEVENT','VCONTACT') NOT NULL,
  PRIMARY KEY (`commitedoperation_hash_client_id`),
  KEY `commitedoperation_entity_id_fkey` (`commitedoperation_entity_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Company`
--

DROP TABLE IF EXISTS `Company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Company` (
  `company_id` int(8) NOT NULL AUTO_INCREMENT,
  `company_domain_id` int(8) NOT NULL,
  `company_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `company_userupdate` int(8) DEFAULT NULL,
  `company_usercreate` int(8) DEFAULT NULL,
  `company_datasource_id` int(8) DEFAULT NULL,
  `company_number` varchar(32) DEFAULT NULL,
  `company_vat` varchar(20) DEFAULT NULL,
  `company_siret` varchar(14) DEFAULT NULL,
  `company_archive` char(1) NOT NULL DEFAULT '0',
  `company_name` varchar(96) NOT NULL DEFAULT '',
  `company_aka` varchar(255) DEFAULT NULL,
  `company_sound` varchar(48) DEFAULT NULL,
  `company_type_id` int(8) DEFAULT NULL,
  `company_activity_id` int(8) DEFAULT NULL,
  `company_nafcode_id` int(8) DEFAULT NULL,
  `company_marketingmanager_id` int(8) DEFAULT NULL,
  `company_contact_number` int(5) NOT NULL DEFAULT '0',
  `company_deal_number` int(5) NOT NULL DEFAULT '0',
  `company_deal_total` int(5) NOT NULL DEFAULT '0',
  `company_comment` text,
  PRIMARY KEY (`company_id`),
  KEY `company_domain_id_domain_id_fkey` (`company_domain_id`),
  KEY `company_userupdate_userobm_id_fkey` (`company_userupdate`),
  KEY `company_usercreate_userobm_id_fkey` (`company_usercreate`),
  KEY `company_datasource_id_datasource_id_fkey` (`company_datasource_id`),
  KEY `company_type_id_companytype_id_fkey` (`company_type_id`),
  KEY `company_activity_id_companyactivity_id_fkey` (`company_activity_id`),
  KEY `company_nafcode_id_companynafcode_id_fkey` (`company_nafcode_id`),
  KEY `company_marketingmanager_id_userobm_id_fkey` (`company_marketingmanager_id`),
  CONSTRAINT `company_marketingmanager_id_userobm_id_fkey` FOREIGN KEY (`company_marketingmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `company_activity_id_companyactivity_id_fkey` FOREIGN KEY (`company_activity_id`) REFERENCES `CompanyActivity` (`companyactivity_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `company_datasource_id_datasource_id_fkey` FOREIGN KEY (`company_datasource_id`) REFERENCES `DataSource` (`datasource_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `company_domain_id_domain_id_fkey` FOREIGN KEY (`company_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `company_nafcode_id_companynafcode_id_fkey` FOREIGN KEY (`company_nafcode_id`) REFERENCES `CompanyNafCode` (`companynafcode_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `company_type_id_companytype_id_fkey` FOREIGN KEY (`company_type_id`) REFERENCES `CompanyType` (`companytype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `company_usercreate_userobm_id_fkey` FOREIGN KEY (`company_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `company_userupdate_userobm_id_fkey` FOREIGN KEY (`company_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CompanyActivity`
--

DROP TABLE IF EXISTS `CompanyActivity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CompanyActivity` (
  `companyactivity_id` int(8) NOT NULL AUTO_INCREMENT,
  `companyactivity_domain_id` int(8) NOT NULL,
  `companyactivity_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `companyactivity_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `companyactivity_userupdate` int(8) DEFAULT NULL,
  `companyactivity_usercreate` int(8) DEFAULT NULL,
  `companyactivity_code` varchar(10) DEFAULT '',
  `companyactivity_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`companyactivity_id`),
  KEY `companyactivity_domain_id_domain_id_fkey` (`companyactivity_domain_id`),
  KEY `companyactivity_userupdate_userobm_id_fkey` (`companyactivity_userupdate`),
  KEY `companyactivity_usercreate_userobm_id_fkey` (`companyactivity_usercreate`),
  CONSTRAINT `companyactivity_usercreate_userobm_id_fkey` FOREIGN KEY (`companyactivity_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `companyactivity_domain_id_domain_id_fkey` FOREIGN KEY (`companyactivity_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `companyactivity_userupdate_userobm_id_fkey` FOREIGN KEY (`companyactivity_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CompanyEntity`
--

DROP TABLE IF EXISTS `CompanyEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CompanyEntity` (
  `companyentity_entity_id` int(8) NOT NULL,
  `companyentity_company_id` int(8) NOT NULL,
  PRIMARY KEY (`companyentity_entity_id`,`companyentity_company_id`),
  KEY `companyentity_company_id_company_id_fkey` (`companyentity_company_id`),
  CONSTRAINT `companyentity_company_id_company_id_fkey` FOREIGN KEY (`companyentity_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `companyentity_entity_id_entity_id_fkey` FOREIGN KEY (`companyentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CompanyNafCode`
--

DROP TABLE IF EXISTS `CompanyNafCode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CompanyNafCode` (
  `companynafcode_id` int(8) NOT NULL AUTO_INCREMENT,
  `companynafcode_domain_id` int(8) NOT NULL,
  `companynafcode_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `companynafcode_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `companynafcode_userupdate` int(8) DEFAULT NULL,
  `companynafcode_usercreate` int(8) DEFAULT NULL,
  `companynafcode_title` int(1) NOT NULL DEFAULT '0',
  `companynafcode_code` varchar(4) DEFAULT NULL,
  `companynafcode_label` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`companynafcode_id`),
  KEY `companynafcode_domain_id_domain_id_fkey` (`companynafcode_domain_id`),
  KEY `companynafcode_userupdate_userobm_id_fkey` (`companynafcode_userupdate`),
  KEY `companynafcode_usercreate_userobm_id_fkey` (`companynafcode_usercreate`),
  CONSTRAINT `companynafcode_usercreate_userobm_id_fkey` FOREIGN KEY (`companynafcode_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `companynafcode_domain_id_domain_id_fkey` FOREIGN KEY (`companynafcode_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `companynafcode_userupdate_userobm_id_fkey` FOREIGN KEY (`companynafcode_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CompanyType`
--

DROP TABLE IF EXISTS `CompanyType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CompanyType` (
  `companytype_id` int(8) NOT NULL AUTO_INCREMENT,
  `companytype_domain_id` int(8) NOT NULL,
  `companytype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `companytype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `companytype_userupdate` int(8) DEFAULT NULL,
  `companytype_usercreate` int(8) DEFAULT NULL,
  `companytype_code` varchar(10) DEFAULT '',
  `companytype_label` char(12) DEFAULT NULL,
  PRIMARY KEY (`companytype_id`),
  KEY `companytype_domain_id_domain_id_fkey` (`companytype_domain_id`),
  KEY `companytype_userupdate_userobm_id_fkey` (`companytype_userupdate`),
  KEY `companytype_usercreate_userobm_id_fkey` (`companytype_usercreate`),
  CONSTRAINT `companytype_usercreate_userobm_id_fkey` FOREIGN KEY (`companytype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `companytype_domain_id_domain_id_fkey` FOREIGN KEY (`companytype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `companytype_userupdate_userobm_id_fkey` FOREIGN KEY (`companytype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Contact`
--

DROP TABLE IF EXISTS `Contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Contact` (
  `contact_id` int(8) NOT NULL AUTO_INCREMENT,
  `contact_domain_id` int(8) NOT NULL,
  `contact_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contact_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contact_userupdate` int(8) DEFAULT NULL,
  `contact_usercreate` int(8) DEFAULT NULL,
  `contact_datasource_id` int(8) DEFAULT NULL,
  `contact_addressbook_id` int(8) DEFAULT NULL,
  `contact_company_id` int(8) DEFAULT NULL,
  `contact_company` varchar(64) DEFAULT NULL,
  `contact_kind_id` int(8) DEFAULT NULL,
  `contact_marketingmanager_id` int(8) DEFAULT NULL,
  `contact_commonname` varchar(256) DEFAULT '',
  `contact_lastname` varchar(64) DEFAULT NULL,
  `contact_firstname` varchar(64) DEFAULT NULL,
  `contact_middlename` varchar(32) DEFAULT NULL,
  `contact_suffix` varchar(16) DEFAULT NULL,
  `contact_aka` varchar(255) DEFAULT NULL,
  `contact_sound` varchar(48) DEFAULT NULL,
  `contact_manager` varchar(64) DEFAULT NULL,
  `contact_assistant` varchar(64) DEFAULT NULL,
  `contact_spouse` varchar(64) DEFAULT NULL,
  `contact_category` varchar(255) DEFAULT NULL,
  `contact_service` varchar(64) DEFAULT NULL,
  `contact_function_id` int(8) DEFAULT NULL,
  `contact_title` varchar(64) DEFAULT NULL,
  `contact_mailing_ok` char(1) DEFAULT '0',
  `contact_newsletter` char(1) DEFAULT '0',
  `contact_archive` char(1) DEFAULT '0',
  `contact_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contact_birthday_id` int(8) DEFAULT NULL,
  `contact_anniversary_id` int(8) DEFAULT NULL,
  `contact_photo_id` int(8) DEFAULT NULL,
  `contact_comment` text,
  `contact_comment2` text,
  `contact_comment3` text,
  `contact_collected` tinyint(1) DEFAULT '0',
  `contact_origin` varchar(255) NOT NULL,
  PRIMARY KEY (`contact_id`),
  KEY `contact_domain_id_domain_id_fkey` (`contact_domain_id`),
  KEY `contact_company_id_company_id_fkey` (`contact_company_id`),
  KEY `contact_userupdate_userobm_id_fkey` (`contact_userupdate`),
  KEY `contact_usercreate_userobm_id_fkey` (`contact_usercreate`),
  KEY `contact_datasource_id_datasource_id_fkey` (`contact_datasource_id`),
  KEY `contact_addressbook_id_addressbook_id_fkey` (`contact_addressbook_id`),
  KEY `contact_kind_id_kind_id_fkey` (`contact_kind_id`),
  KEY `contact_marketingmanager_id_userobm_id_fkey` (`contact_marketingmanager_id`),
  KEY `contact_function_id_contactfunction_id_fkey` (`contact_function_id`),
  KEY `contact_birthday_id_fkey` (`contact_birthday_id`),
  KEY `contact_anniversary_id_fkey` (`contact_anniversary_id`),
  KEY `contact_photo_id_document_id_fkey` (`contact_photo_id`),
  CONSTRAINT `contact_function_id_contactfunction_id_fkey` FOREIGN KEY (`contact_function_id`) REFERENCES `ContactFunction` (`contactfunction_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_company_id_company_id_fkey` FOREIGN KEY (`contact_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contact_datasource_id_datasource_id_fkey` FOREIGN KEY (`contact_datasource_id`) REFERENCES `DataSource` (`datasource_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_addressbook_id_addressbook_id_fkey` FOREIGN KEY (`contact_addressbook_id`) REFERENCES `AddressBook` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_domain_id_domain_id_fkey` FOREIGN KEY (`contact_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contact_kind_id_kind_id_fkey` FOREIGN KEY (`contact_kind_id`) REFERENCES `Kind` (`kind_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_marketingmanager_id_userobm_id_fkey` FOREIGN KEY (`contact_marketingmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_usercreate_userobm_id_fkey` FOREIGN KEY (`contact_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_userupdate_userobm_id_fkey` FOREIGN KEY (`contact_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_birthday_id_fkey` FOREIGN KEY (`contact_birthday_id`) REFERENCES `Event` (`event_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_anniversary_id_fkey` FOREIGN KEY (`contact_anniversary_id`) REFERENCES `Event` (`event_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contact_photo_id_document_id_fkey` FOREIGN KEY (`contact_photo_id`) REFERENCES `Document` (`document_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContactEntity`
--

DROP TABLE IF EXISTS `ContactEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContactEntity` (
  `contactentity_entity_id` int(8) NOT NULL,
  `contactentity_contact_id` int(8) NOT NULL,
  PRIMARY KEY (`contactentity_entity_id`,`contactentity_contact_id`),
  KEY `contactentity_contact_id_contact_id_fkey` (`contactentity_contact_id`),
  CONSTRAINT `contactentity_contact_id_contact_id_fkey` FOREIGN KEY (`contactentity_contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contactentity_entity_id_entity_id_fkey` FOREIGN KEY (`contactentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContactFunction`
--

DROP TABLE IF EXISTS `ContactFunction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContactFunction` (
  `contactfunction_id` int(8) NOT NULL AUTO_INCREMENT,
  `contactfunction_domain_id` int(8) NOT NULL,
  `contactfunction_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contactfunction_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contactfunction_userupdate` int(8) DEFAULT NULL,
  `contactfunction_usercreate` int(8) DEFAULT NULL,
  `contactfunction_code` varchar(10) DEFAULT '',
  `contactfunction_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`contactfunction_id`),
  KEY `contactfunction_domain_id_domain_id_fkey` (`contactfunction_domain_id`),
  KEY `contactfunction_userupdate_userobm_id_fkey` (`contactfunction_userupdate`),
  KEY `contactfunction_usercreate_userobm_id_fkey` (`contactfunction_usercreate`),
  CONSTRAINT `contactfunction_usercreate_userobm_id_fkey` FOREIGN KEY (`contactfunction_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contactfunction_domain_id_domain_id_fkey` FOREIGN KEY (`contactfunction_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contactfunction_userupdate_userobm_id_fkey` FOREIGN KEY (`contactfunction_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContactList`
--

DROP TABLE IF EXISTS `ContactList`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContactList` (
  `contactlist_list_id` int(8) NOT NULL,
  `contactlist_contact_id` int(8) NOT NULL,
  KEY `contactlist_list_id_list_id_fkey` (`contactlist_list_id`),
  KEY `contactlist_contact_id_contact_id_fkey` (`contactlist_contact_id`),
  CONSTRAINT `contactlist_contact_id_contact_id_fkey` FOREIGN KEY (`contactlist_contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contactlist_list_id_list_id_fkey` FOREIGN KEY (`contactlist_list_id`) REFERENCES `List` (`list_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Contract`
--

DROP TABLE IF EXISTS `Contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Contract` (
  `contract_id` int(8) NOT NULL AUTO_INCREMENT,
  `contract_domain_id` int(8) NOT NULL,
  `contract_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contract_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contract_userupdate` int(8) DEFAULT NULL,
  `contract_usercreate` int(8) DEFAULT NULL,
  `contract_deal_id` int(8) DEFAULT NULL,
  `contract_company_id` int(8) DEFAULT NULL,
  `contract_label` varchar(128) DEFAULT NULL,
  `contract_number` varchar(20) DEFAULT NULL,
  `contract_datesignature` date DEFAULT NULL,
  `contract_datebegin` date DEFAULT NULL,
  `contract_dateexp` date DEFAULT NULL,
  `contract_daterenew` date DEFAULT NULL,
  `contract_datecancel` date DEFAULT NULL,
  `contract_type_id` int(8) DEFAULT NULL,
  `contract_priority_id` int(8) DEFAULT NULL,
  `contract_status_id` int(8) DEFAULT NULL,
  `contract_kind` int(2) DEFAULT NULL,
  `contract_format` int(2) DEFAULT NULL,
  `contract_ticketnumber` int(8) DEFAULT NULL,
  `contract_duration` int(8) DEFAULT NULL,
  `contract_autorenewal` int(2) DEFAULT NULL,
  `contract_contact1_id` int(8) DEFAULT NULL,
  `contract_contact2_id` int(8) DEFAULT NULL,
  `contract_techmanager_id` int(8) DEFAULT NULL,
  `contract_marketmanager_id` int(8) DEFAULT NULL,
  `contract_privacy` int(2) DEFAULT '0',
  `contract_archive` int(1) DEFAULT '0',
  `contract_clause` text,
  `contract_comment` text,
  PRIMARY KEY (`contract_id`),
  KEY `contract_domain_id_domain_id_fkey` (`contract_domain_id`),
  KEY `contract_deal_id_deal_id_fkey` (`contract_deal_id`),
  KEY `contract_company_id_company_id_fkey` (`contract_company_id`),
  KEY `contract_userupdate_userobm_id_fkey` (`contract_userupdate`),
  KEY `contract_usercreate_userobm_id_fkey` (`contract_usercreate`),
  KEY `contract_type_id_contracttype_id_fkey` (`contract_type_id`),
  KEY `contract_priority_id_contractpriority_id_fkey` (`contract_priority_id`),
  KEY `contract_status_id_contractstatus_id_fkey` (`contract_status_id`),
  KEY `contract_contact1_id_contact_id_fkey` (`contract_contact1_id`),
  KEY `contract_contact2_id_contact_id_fkey` (`contract_contact2_id`),
  KEY `contract_techmanager_id_userobm_id_fkey` (`contract_techmanager_id`),
  KEY `contract_marketmanager_id_userobm_id_fkey` (`contract_marketmanager_id`),
  CONSTRAINT `contract_marketmanager_id_userobm_id_fkey` FOREIGN KEY (`contract_marketmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_company_id_company_id_fkey` FOREIGN KEY (`contract_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contract_contact1_id_contact_id_fkey` FOREIGN KEY (`contract_contact1_id`) REFERENCES `Contact` (`contact_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_contact2_id_contact_id_fkey` FOREIGN KEY (`contract_contact2_id`) REFERENCES `Contact` (`contact_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_deal_id_deal_id_fkey` FOREIGN KEY (`contract_deal_id`) REFERENCES `Deal` (`deal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contract_domain_id_domain_id_fkey` FOREIGN KEY (`contract_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contract_priority_id_contractpriority_id_fkey` FOREIGN KEY (`contract_priority_id`) REFERENCES `ContractPriority` (`contractpriority_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_status_id_contractstatus_id_fkey` FOREIGN KEY (`contract_status_id`) REFERENCES `ContractStatus` (`contractstatus_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_techmanager_id_userobm_id_fkey` FOREIGN KEY (`contract_techmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_type_id_contracttype_id_fkey` FOREIGN KEY (`contract_type_id`) REFERENCES `ContractType` (`contracttype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_usercreate_userobm_id_fkey` FOREIGN KEY (`contract_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_userupdate_userobm_id_fkey` FOREIGN KEY (`contract_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContractEntity`
--

DROP TABLE IF EXISTS `ContractEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContractEntity` (
  `contractentity_entity_id` int(8) NOT NULL,
  `contractentity_contract_id` int(8) NOT NULL,
  PRIMARY KEY (`contractentity_entity_id`,`contractentity_contract_id`),
  KEY `contractentity_contract_id_contract_id_fkey` (`contractentity_contract_id`),
  CONSTRAINT `contractentity_contract_id_contract_id_fkey` FOREIGN KEY (`contractentity_contract_id`) REFERENCES `Contract` (`contract_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contractentity_entity_id_entity_id_fkey` FOREIGN KEY (`contractentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContractPriority`
--

DROP TABLE IF EXISTS `ContractPriority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContractPriority` (
  `contractpriority_id` int(8) NOT NULL AUTO_INCREMENT,
  `contractpriority_domain_id` int(8) NOT NULL,
  `contractpriority_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contractpriority_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contractpriority_userupdate` int(8) DEFAULT NULL,
  `contractpriority_usercreate` int(8) DEFAULT NULL,
  `contractpriority_code` varchar(10) DEFAULT '',
  `contractpriority_color` varchar(6) DEFAULT NULL,
  `contractpriority_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`contractpriority_id`),
  KEY `contractpriority_domain_id_domain_id_fkey` (`contractpriority_domain_id`),
  KEY `contractpriority_userupdate_userobm_id_fkey` (`contractpriority_userupdate`),
  KEY `contractpriority_usercreate_userobm_id_fkey` (`contractpriority_usercreate`),
  CONSTRAINT `contractpriority_usercreate_userobm_id_fkey` FOREIGN KEY (`contractpriority_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contractpriority_domain_id_domain_id_fkey` FOREIGN KEY (`contractpriority_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contractpriority_userupdate_userobm_id_fkey` FOREIGN KEY (`contractpriority_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContractStatus`
--

DROP TABLE IF EXISTS `ContractStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContractStatus` (
  `contractstatus_id` int(8) NOT NULL AUTO_INCREMENT,
  `contractstatus_domain_id` int(8) NOT NULL,
  `contractstatus_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contractstatus_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contractstatus_userupdate` int(8) DEFAULT NULL,
  `contractstatus_usercreate` int(8) DEFAULT NULL,
  `contractstatus_code` varchar(10) DEFAULT '',
  `contractstatus_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`contractstatus_id`),
  KEY `contractstatus_domain_id_domain_id_fkey` (`contractstatus_domain_id`),
  KEY `contractstatus_userupdate_userobm_id_fkey` (`contractstatus_userupdate`),
  KEY `contractstatus_usercreate_userobm_id_fkey` (`contractstatus_usercreate`),
  CONSTRAINT `contractstatus_usercreate_userobm_id_fkey` FOREIGN KEY (`contractstatus_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contractstatus_domain_id_domain_id_fkey` FOREIGN KEY (`contractstatus_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contractstatus_userupdate_userobm_id_fkey` FOREIGN KEY (`contractstatus_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContractType`
--

DROP TABLE IF EXISTS `ContractType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContractType` (
  `contracttype_id` int(8) NOT NULL AUTO_INCREMENT,
  `contracttype_domain_id` int(8) NOT NULL,
  `contracttype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contracttype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contracttype_userupdate` int(8) DEFAULT NULL,
  `contracttype_usercreate` int(8) DEFAULT NULL,
  `contracttype_code` varchar(10) DEFAULT '',
  `contracttype_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`contracttype_id`),
  KEY `contracttype_domain_id_domain_id_fkey` (`contracttype_domain_id`),
  KEY `contracttype_userupdate_userobm_id_fkey` (`contracttype_userupdate`),
  KEY `contracttype_usercreate_userobm_id_fkey` (`contracttype_usercreate`),
  CONSTRAINT `contracttype_usercreate_userobm_id_fkey` FOREIGN KEY (`contracttype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contracttype_domain_id_domain_id_fkey` FOREIGN KEY (`contracttype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contracttype_userupdate_userobm_id_fkey` FOREIGN KEY (`contracttype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Country`
--

DROP TABLE IF EXISTS `Country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Country` (
  `country_domain_id` int(8) NOT NULL,
  `country_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `country_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `country_userupdate` int(8) DEFAULT NULL,
  `country_usercreate` int(8) DEFAULT NULL,
  `country_iso3166` char(2) NOT NULL,
  `country_name` varchar(64) DEFAULT NULL,
  `country_lang` char(2) NOT NULL,
  `country_phone` varchar(4) DEFAULT NULL,
  PRIMARY KEY (`country_iso3166`,`country_lang`),
  KEY `country_domain_id_domain_id_fkey` (`country_domain_id`),
  KEY `country_userupdate_userobm_id_fkey` (`country_userupdate`),
  KEY `country_usercreate_userobm_id_fkey` (`country_usercreate`),
  CONSTRAINT `country_usercreate_userobm_id_fkey` FOREIGN KEY (`country_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `country_domain_id_domain_id_fkey` FOREIGN KEY (`country_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `country_userupdate_userobm_id_fkey` FOREIGN KEY (`country_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CvEntity`
--

DROP TABLE IF EXISTS `CvEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CvEntity` (
  `cventity_entity_id` int(8) NOT NULL,
  `cventity_cv_id` int(8) NOT NULL,
  PRIMARY KEY (`cventity_entity_id`,`cventity_cv_id`),
  KEY `cventity_cv_id_cv_id_fkey` (`cventity_cv_id`),
  CONSTRAINT `cventity_cv_id_cv_id_fkey` FOREIGN KEY (`cventity_cv_id`) REFERENCES `CV` (`cv_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `cventity_entity_id_entity_id_fkey` FOREIGN KEY (`cventity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DataSource`
--

DROP TABLE IF EXISTS `DataSource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DataSource` (
  `datasource_id` int(8) NOT NULL AUTO_INCREMENT,
  `datasource_domain_id` int(8) NOT NULL,
  `datasource_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `datasource_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `datasource_userupdate` int(8) DEFAULT NULL,
  `datasource_usercreate` int(8) DEFAULT NULL,
  `datasource_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`datasource_id`),
  KEY `datasource_domain_id_domain_id_fkey` (`datasource_domain_id`),
  KEY `datasource_userupdate_userobm_id_fkey` (`datasource_userupdate`),
  KEY `datasource_usercreate_userobm_id_fkey` (`datasource_usercreate`),
  CONSTRAINT `datasource_usercreate_userobm_id_fkey` FOREIGN KEY (`datasource_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `datasource_domain_id_domain_id_fkey` FOREIGN KEY (`datasource_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `datasource_userupdate_userobm_id_fkey` FOREIGN KEY (`datasource_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Deal`
--

DROP TABLE IF EXISTS `Deal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Deal` (
  `deal_id` int(8) NOT NULL AUTO_INCREMENT,
  `deal_domain_id` int(8) NOT NULL,
  `deal_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deal_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `deal_userupdate` int(8) DEFAULT NULL,
  `deal_usercreate` int(8) DEFAULT NULL,
  `deal_number` varchar(32) DEFAULT NULL,
  `deal_label` varchar(128) DEFAULT NULL,
  `deal_datebegin` date DEFAULT NULL,
  `deal_parentdeal_id` int(8) DEFAULT NULL,
  `deal_type_id` int(8) DEFAULT NULL,
  `deal_region_id` int(8) DEFAULT NULL,
  `deal_tasktype_id` int(8) DEFAULT NULL,
  `deal_company_id` int(8) NOT NULL,
  `deal_contact1_id` int(8) DEFAULT NULL,
  `deal_contact2_id` int(8) DEFAULT NULL,
  `deal_marketingmanager_id` int(8) DEFAULT NULL,
  `deal_technicalmanager_id` int(8) DEFAULT NULL,
  `deal_source_id` int(8) DEFAULT NULL,
  `deal_source` varchar(64) DEFAULT NULL,
  `deal_dateproposal` date DEFAULT NULL,
  `deal_dateexpected` date DEFAULT NULL,
  `deal_datealarm` date DEFAULT NULL,
  `deal_dateend` date DEFAULT NULL,
  `deal_amount` decimal(12,2) DEFAULT NULL,
  `deal_margin` decimal(12,2) DEFAULT NULL,
  `deal_commission` decimal(5,2) DEFAULT '0.00',
  `deal_hitrate` int(3) NOT NULL DEFAULT '0',
  `deal_status_id` int(2) DEFAULT NULL,
  `deal_archive` char(1) DEFAULT '0',
  `deal_todo` varchar(128) DEFAULT NULL,
  `deal_privacy` int(2) NOT NULL DEFAULT '0',
  `deal_comment` text,
  PRIMARY KEY (`deal_id`),
  KEY `deal_domain_id_domain_id_fkey` (`deal_domain_id`),
  KEY `deal_parentdeal_id_parentdeal_id_fkey` (`deal_parentdeal_id`),
  KEY `deal_company_id_company_id_fkey` (`deal_company_id`),
  KEY `deal_userupdate_userobm_id_fkey` (`deal_userupdate`),
  KEY `deal_usercreate_userobm_id_fkey` (`deal_usercreate`),
  KEY `deal_type_id_dealtype_id_fkey` (`deal_type_id`),
  KEY `deal_region_id_region_id_fkey` (`deal_region_id`),
  KEY `deal_tasktype_id_tasktype_id_fkey` (`deal_tasktype_id`),
  KEY `deal_contact1_id_contact_id_fkey` (`deal_contact1_id`),
  KEY `deal_contact2_id_contact_id_fkey` (`deal_contact2_id`),
  KEY `deal_marketingmanager_id_userobm_id_fkey` (`deal_marketingmanager_id`),
  KEY `deal_technicalmanager_id_userobm_id_fkey` (`deal_technicalmanager_id`),
  KEY `deal_source_id_leadsource_id_fkey` (`deal_source_id`),
  CONSTRAINT `deal_source_id_leadsource_id_fkey` FOREIGN KEY (`deal_source_id`) REFERENCES `LeadSource` (`leadsource_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_company_id_company_id_fkey` FOREIGN KEY (`deal_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `deal_contact1_id_contact_id_fkey` FOREIGN KEY (`deal_contact1_id`) REFERENCES `Contact` (`contact_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_contact2_id_contact_id_fkey` FOREIGN KEY (`deal_contact2_id`) REFERENCES `Contact` (`contact_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_domain_id_domain_id_fkey` FOREIGN KEY (`deal_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `deal_marketingmanager_id_userobm_id_fkey` FOREIGN KEY (`deal_marketingmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_parentdeal_id_parentdeal_id_fkey` FOREIGN KEY (`deal_parentdeal_id`) REFERENCES `ParentDeal` (`parentdeal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `deal_region_id_region_id_fkey` FOREIGN KEY (`deal_region_id`) REFERENCES `Region` (`region_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_tasktype_id_tasktype_id_fkey` FOREIGN KEY (`deal_tasktype_id`) REFERENCES `TaskType` (`tasktype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_technicalmanager_id_userobm_id_fkey` FOREIGN KEY (`deal_technicalmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_type_id_dealtype_id_fkey` FOREIGN KEY (`deal_type_id`) REFERENCES `DealType` (`dealtype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_usercreate_userobm_id_fkey` FOREIGN KEY (`deal_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `deal_userupdate_userobm_id_fkey` FOREIGN KEY (`deal_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DealCompany`
--

DROP TABLE IF EXISTS `DealCompany`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DealCompany` (
  `dealcompany_id` int(8) NOT NULL AUTO_INCREMENT,
  `dealcompany_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dealcompany_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dealcompany_userupdate` int(8) DEFAULT NULL,
  `dealcompany_usercreate` int(8) DEFAULT NULL,
  `dealcompany_deal_id` int(8) NOT NULL,
  `dealcompany_company_id` int(8) NOT NULL,
  `dealcompany_role_id` int(8) DEFAULT NULL,
  PRIMARY KEY (`dealcompany_id`),
  KEY `dealcompany_idx_deal` (`dealcompany_deal_id`),
  KEY `dealcompany_company_id_company_id_fkey` (`dealcompany_company_id`),
  KEY `dealcompany_role_id_dealcompanyrole_id_fkey` (`dealcompany_role_id`),
  KEY `dealcompany_userupdate_userobm_id_fkey` (`dealcompany_userupdate`),
  KEY `dealcompany_usercreate_userobm_id_fkey` (`dealcompany_usercreate`),
  CONSTRAINT `dealcompany_usercreate_userobm_id_fkey` FOREIGN KEY (`dealcompany_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `dealcompany_company_id_company_id_fkey` FOREIGN KEY (`dealcompany_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dealcompany_deal_id_deal_id_fkey` FOREIGN KEY (`dealcompany_deal_id`) REFERENCES `Deal` (`deal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dealcompany_role_id_dealcompanyrole_id_fkey` FOREIGN KEY (`dealcompany_role_id`) REFERENCES `DealCompanyRole` (`dealcompanyrole_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `dealcompany_userupdate_userobm_id_fkey` FOREIGN KEY (`dealcompany_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DealCompanyRole`
--

DROP TABLE IF EXISTS `DealCompanyRole`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DealCompanyRole` (
  `dealcompanyrole_id` int(8) NOT NULL AUTO_INCREMENT,
  `dealcompanyrole_domain_id` int(8) NOT NULL,
  `dealcompanyrole_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dealcompanyrole_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dealcompanyrole_userupdate` int(8) DEFAULT NULL,
  `dealcompanyrole_usercreate` int(8) DEFAULT NULL,
  `dealcompanyrole_code` varchar(10) DEFAULT '',
  `dealcompanyrole_label` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`dealcompanyrole_id`),
  KEY `dealcompanyrole_domain_id_domain_id_fkey` (`dealcompanyrole_domain_id`),
  KEY `dealcompanyrole_userupdate_userobm_id_fkey` (`dealcompanyrole_userupdate`),
  KEY `dealcompanyrole_usercreate_userobm_id_fkey` (`dealcompanyrole_usercreate`),
  CONSTRAINT `dealcompanyrole_usercreate_userobm_id_fkey` FOREIGN KEY (`dealcompanyrole_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `dealcompanyrole_domain_id_domain_id_fkey` FOREIGN KEY (`dealcompanyrole_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dealcompanyrole_userupdate_userobm_id_fkey` FOREIGN KEY (`dealcompanyrole_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DealEntity`
--

DROP TABLE IF EXISTS `DealEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DealEntity` (
  `dealentity_entity_id` int(8) NOT NULL,
  `dealentity_deal_id` int(8) NOT NULL,
  PRIMARY KEY (`dealentity_entity_id`,`dealentity_deal_id`),
  KEY `dealentity_deal_id_deal_id_fkey` (`dealentity_deal_id`),
  CONSTRAINT `dealentity_deal_id_deal_id_fkey` FOREIGN KEY (`dealentity_deal_id`) REFERENCES `Deal` (`deal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dealentity_entity_id_entity_id_fkey` FOREIGN KEY (`dealentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DealStatus`
--

DROP TABLE IF EXISTS `DealStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DealStatus` (
  `dealstatus_id` int(2) NOT NULL AUTO_INCREMENT,
  `dealstatus_domain_id` int(8) NOT NULL,
  `dealstatus_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dealstatus_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dealstatus_userupdate` int(8) DEFAULT NULL,
  `dealstatus_usercreate` int(8) DEFAULT NULL,
  `dealstatus_label` varchar(24) DEFAULT NULL,
  `dealstatus_order` int(2) DEFAULT NULL,
  `dealstatus_hitrate` char(3) DEFAULT NULL,
  PRIMARY KEY (`dealstatus_id`),
  KEY `dealstatus_domain_id_domain_id_fkey` (`dealstatus_domain_id`),
  KEY `dealstatus_userupdate_userobm_id_fkey` (`dealstatus_userupdate`),
  KEY `dealstatus_usercreate_userobm_id_fkey` (`dealstatus_usercreate`),
  CONSTRAINT `dealstatus_usercreate_userobm_id_fkey` FOREIGN KEY (`dealstatus_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `dealstatus_domain_id_domain_id_fkey` FOREIGN KEY (`dealstatus_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dealstatus_userupdate_userobm_id_fkey` FOREIGN KEY (`dealstatus_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DealType`
--

DROP TABLE IF EXISTS `DealType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DealType` (
  `dealtype_id` int(8) NOT NULL AUTO_INCREMENT,
  `dealtype_domain_id` int(8) NOT NULL,
  `dealtype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dealtype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dealtype_userupdate` int(8) DEFAULT NULL,
  `dealtype_usercreate` int(8) DEFAULT NULL,
  `dealtype_inout` varchar(1) DEFAULT '-',
  `dealtype_code` varchar(10) DEFAULT NULL,
  `dealtype_label` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`dealtype_id`),
  KEY `dealtype_domain_id_domain_id_fkey` (`dealtype_domain_id`),
  KEY `dealtype_userupdate_userobm_id_fkey` (`dealtype_userupdate`),
  KEY `dealtype_usercreate_userobm_id_fkey` (`dealtype_usercreate`),
  CONSTRAINT `dealtype_usercreate_userobm_id_fkey` FOREIGN KEY (`dealtype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `dealtype_domain_id_domain_id_fkey` FOREIGN KEY (`dealtype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `dealtype_userupdate_userobm_id_fkey` FOREIGN KEY (`dealtype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DefaultOdtTemplate`
--

DROP TABLE IF EXISTS `DefaultOdtTemplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DefaultOdtTemplate` (
  `defaultodttemplate_id` int(8) NOT NULL AUTO_INCREMENT,
  `defaultodttemplate_domain_id` int(8) NOT NULL,
  `defaultodttemplate_entity` varchar(32) DEFAULT NULL,
  `defaultodttemplate_document_id` int(8) NOT NULL,
  `defaultodttemplate_label` varchar(64) DEFAULT '',
  PRIMARY KEY (`defaultodttemplate_id`),
  KEY `defaultodttemplate_domain_id_domain_id_fkey` (`defaultodttemplate_domain_id`),
  KEY `defaultodttemplate_document_id_document_id_fkey` (`defaultodttemplate_document_id`),
  CONSTRAINT `defaultodttemplate_document_id_document_id_fkey` FOREIGN KEY (`defaultodttemplate_document_id`) REFERENCES `Document` (`document_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `defaultodttemplate_domain_id_domain_id_fkey` FOREIGN KEY (`defaultodttemplate_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Deleted`
--

DROP TABLE IF EXISTS `Deleted`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Deleted` (
  `deleted_id` int(8) NOT NULL AUTO_INCREMENT,
  `deleted_domain_id` int(8) DEFAULT NULL,
  `deleted_user_id` int(8) DEFAULT NULL,
  `deleted_delegation` varchar(256) DEFAULT '',
  `deleted_table` varchar(32) DEFAULT NULL,
  `deleted_entity_id` int(8) DEFAULT NULL,
  `deleted_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`deleted_id`),
  KEY `deleted_user_id_userobm_id_fkey` (`deleted_user_id`),
  KEY `deleted_domain_id_domain_id_fkey` (`deleted_domain_id`),
  CONSTRAINT `deleted_domain_id_domain_id_fkey` FOREIGN KEY (`deleted_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `deleted_user_id_userobm_id_fkey` FOREIGN KEY (`deleted_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedAddressbook`
--

DROP TABLE IF EXISTS `DeletedAddressbook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedAddressbook` (
  `addressbook_id` int(8) NOT NULL,
  `user_id` int(8) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `origin` varchar(255) NOT NULL,
  PRIMARY KEY (`addressbook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedContact`
--

DROP TABLE IF EXISTS `DeletedContact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedContact` (
  `deletedcontact_contact_id` int(8) NOT NULL,
  `deletedcontact_addressbook_id` int(8) NOT NULL,
  `deletedcontact_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deletedcontact_origin` varchar(255) NOT NULL,
  PRIMARY KEY (`deletedcontact_contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedEvent`
--

DROP TABLE IF EXISTS `DeletedEvent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedEvent` (
  `deletedevent_id` int(8) NOT NULL AUTO_INCREMENT,
  `deletedevent_event_id` int(8) DEFAULT NULL,
  `deletedevent_event_ext_id` varchar(300) DEFAULT '',
  `deletedevent_user_id` int(8) DEFAULT NULL,
  `deletedevent_origin` varchar(255) NOT NULL,
  `deletedevent_type` enum('VEVENT','VTODO','VJOURNAL','VFREEBUSY') DEFAULT 'VEVENT',
  `deletedevent_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`deletedevent_id`),
  KEY `idx_dce_event` (`deletedevent_event_id`),
  KEY `idx_dce_user` (`deletedevent_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedEventLink`
--

DROP TABLE IF EXISTS `DeletedEventLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedEventLink` (
  `deletedeventlink_id` int(11) NOT NULL AUTO_INCREMENT,
  `deletedeventlink_userobm_id` int(11) NOT NULL,
  `deletedeventlink_event_id` int(11) NOT NULL,
  `deletedeventlink_event_ext_id` varchar(300) NOT NULL,
  `deletedeventlink_time_removed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`deletedeventlink_id`),
  KEY `deletedeventlink_userobm_id_userobm_id_fkey` (`deletedeventlink_userobm_id`),
  KEY `deletedeventlink_event_id_event_id_fkey` (`deletedeventlink_event_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedSyncedAddressbook`
--

DROP TABLE IF EXISTS `DeletedSyncedAddressbook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedSyncedAddressbook` (
  `user_id` int(8) NOT NULL,
  `addressbook_id` int(8) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`addressbook_id`),
  KEY `DeletedSyncedAddressbook_user_id_user_id_fkey` (`user_id`),
  KEY `DeletedSyncedAddressbook_addressbook_id_addressbook_id_fkey` (`addressbook_id`),
  CONSTRAINT `DeletedSyncedAddressbook_user_id_userobm_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `DeletedSyncedAddressbook_addressbook_id_addressbook_id_fkey` FOREIGN KEY (`addressbook_id`) REFERENCES `AddressBook` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedUser`
--

DROP TABLE IF EXISTS `DeletedUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedUser` (
  `deleteduser_user_id` int(8) NOT NULL,
  `deleteduser_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`deleteduser_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DisplayPref`
--

DROP TABLE IF EXISTS `DisplayPref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DisplayPref` (
  `display_id` int(8) NOT NULL AUTO_INCREMENT,
  `display_user_id` int(8) DEFAULT NULL,
  `display_entity` varchar(32) NOT NULL DEFAULT '',
  `display_fieldname` varchar(64) NOT NULL DEFAULT '',
  `display_fieldorder` int(3) unsigned DEFAULT NULL,
  `display_display` int(1) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`display_id`),
  UNIQUE KEY `displaypref_key` (`display_user_id`,`display_entity`,`display_fieldname`),
  KEY `idx_user` (`display_user_id`),
  KEY `idx_entity` (`display_entity`),
  CONSTRAINT `display_user_id_userobm_id_fkey` FOREIGN KEY (`display_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Document`
--

DROP TABLE IF EXISTS `Document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Document` (
  `document_id` int(8) NOT NULL AUTO_INCREMENT,
  `document_domain_id` int(8) NOT NULL,
  `document_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `document_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `document_userupdate` int(8) DEFAULT NULL,
  `document_usercreate` int(8) DEFAULT NULL,
  `document_title` varchar(255) DEFAULT NULL,
  `document_name` varchar(255) DEFAULT NULL,
  `document_kind` int(2) DEFAULT NULL,
  `document_mimetype_id` int(8) DEFAULT NULL,
  `document_privacy` int(2) NOT NULL DEFAULT '0',
  `document_size` int(15) DEFAULT NULL,
  `document_author` varchar(255) DEFAULT NULL,
  `document_path` text,
  `document_acl` text,
  PRIMARY KEY (`document_id`),
  KEY `document_domain_id_domain_id_fkey` (`document_domain_id`),
  KEY `document_userupdate_userobm_id_fkey` (`document_userupdate`),
  KEY `document_usercreate_userobm_id_fkey` (`document_usercreate`),
  KEY `document_mimetype_id_documentmimetype_id_fkey` (`document_mimetype_id`),
  CONSTRAINT `document_mimetype_id_documentmimetype_id_fkey` FOREIGN KEY (`document_mimetype_id`) REFERENCES `DocumentMimeType` (`documentmimetype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `document_domain_id_domain_id_fkey` FOREIGN KEY (`document_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `document_usercreate_userobm_id_fkey` FOREIGN KEY (`document_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `document_userupdate_userobm_id_fkey` FOREIGN KEY (`document_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DocumentEntity`
--

DROP TABLE IF EXISTS `DocumentEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DocumentEntity` (
  `documententity_entity_id` int(8) NOT NULL,
  `documententity_document_id` int(8) NOT NULL,
  PRIMARY KEY (`documententity_entity_id`,`documententity_document_id`),
  KEY `documententity_document_id_document_id_fkey` (`documententity_document_id`),
  CONSTRAINT `documententity_document_id_document_id_fkey` FOREIGN KEY (`documententity_document_id`) REFERENCES `Document` (`document_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `documententity_entity_id_entity_id_fkey` FOREIGN KEY (`documententity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DocumentLink`
--

DROP TABLE IF EXISTS `DocumentLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DocumentLink` (
  `documentlink_document_id` int(8) NOT NULL,
  `documentlink_entity_id` int(8) NOT NULL,
  `documentlink_usercreate` int(8) DEFAULT NULL,
  PRIMARY KEY (`documentlink_document_id`,`documentlink_entity_id`),
  KEY `documentlink_entity_id_entity_id_fkey` (`documentlink_entity_id`),
  KEY `documentlink_usercreate_userobm_id_fkey` (`documentlink_usercreate`),
  CONSTRAINT `documentlink_document_id_document_id_fkey` FOREIGN KEY (`documentlink_document_id`) REFERENCES `Document` (`document_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `documentlink_entity_id_entity_id_fkey` FOREIGN KEY (`documentlink_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `documentlink_usercreate_userobm_id_fkey` FOREIGN KEY (`documentlink_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DocumentMimeType`
--

DROP TABLE IF EXISTS `DocumentMimeType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DocumentMimeType` (
  `documentmimetype_id` int(8) NOT NULL AUTO_INCREMENT,
  `documentmimetype_domain_id` int(8) NOT NULL,
  `documentmimetype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `documentmimetype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `documentmimetype_userupdate` int(8) DEFAULT NULL,
  `documentmimetype_usercreate` int(8) DEFAULT NULL,
  `documentmimetype_label` varchar(255) DEFAULT NULL,
  `documentmimetype_extension` varchar(10) DEFAULT NULL,
  `documentmimetype_mime` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`documentmimetype_id`),
  KEY `documentmimetype_domain_id_domain_id_fkey` (`documentmimetype_domain_id`),
  KEY `documentmimetype_userupdate_userobm_id_fkey` (`documentmimetype_userupdate`),
  KEY `documentmimetype_usercreate_userobm_id_fkey` (`documentmimetype_usercreate`),
  CONSTRAINT `documentmimetype_usercreate_userobm_id_fkey` FOREIGN KEY (`documentmimetype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `documentmimetype_domain_id_domain_id_fkey` FOREIGN KEY (`documentmimetype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `documentmimetype_userupdate_userobm_id_fkey` FOREIGN KEY (`documentmimetype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Domain`
--

DROP TABLE IF EXISTS `Domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Domain` (
  `domain_id` int(8) NOT NULL AUTO_INCREMENT,
  `domain_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `domain_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `domain_usercreate` int(8) DEFAULT NULL,
  `domain_userupdate` int(8) DEFAULT NULL,
  `domain_label` varchar(32) NOT NULL,
  `domain_description` varchar(255) DEFAULT NULL,
  `domain_name` varchar(128) DEFAULT NULL,
  `domain_alias` text,
  `domain_global` tinyint(1) DEFAULT '0',
  `domain_uuid` char(36) NOT NULL,
  PRIMARY KEY (`domain_id`),
  KEY `domain_userupdate_userobm_id_fkey` (`domain_userupdate`),
  KEY `domain_usercreate_userobm_id_fkey` (`domain_usercreate`),
  CONSTRAINT `domain_usercreate_userobm_id_fkey` FOREIGN KEY (`domain_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `domain_userupdate_userobm_id_fkey` FOREIGN KEY (`domain_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DomainEntity`
--

DROP TABLE IF EXISTS `DomainEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DomainEntity` (
  `domainentity_entity_id` int(8) NOT NULL,
  `domainentity_domain_id` int(8) NOT NULL,
  PRIMARY KEY (`domainentity_entity_id`,`domainentity_domain_id`),
  KEY `domainentity_domain_id_domain_id_fkey` (`domainentity_domain_id`),
  CONSTRAINT `domainentity_domain_id_domain_id_fkey` FOREIGN KEY (`domainentity_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `domainentity_entity_id_entity_id_fkey` FOREIGN KEY (`domainentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DomainProperty`
--

DROP TABLE IF EXISTS `DomainProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DomainProperty` (
  `domainproperty_key` varchar(255) NOT NULL,
  `domainproperty_type` varchar(32) DEFAULT NULL,
  `domainproperty_default` varchar(64) DEFAULT NULL,
  `domainproperty_readonly` int(1) DEFAULT '0',
  PRIMARY KEY (`domainproperty_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DomainPropertyValue`
--

DROP TABLE IF EXISTS `DomainPropertyValue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DomainPropertyValue` (
  `domainpropertyvalue_domain_id` int(8) NOT NULL,
  `domainpropertyvalue_property_key` varchar(255) NOT NULL,
  `domainpropertyvalue_value` varchar(255) NOT NULL,
  PRIMARY KEY (`domainpropertyvalue_domain_id`,`domainpropertyvalue_property_key`),
  CONSTRAINT `domainpropertyvalue_domain_id_domain_id_fkey` FOREIGN KEY (`domainpropertyvalue_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Email`
--

DROP TABLE IF EXISTS `Email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Email` (
  `email_id` int(8) NOT NULL AUTO_INCREMENT,
  `email_entity_id` int(8) NOT NULL,
  `email_label` varchar(255) NOT NULL,
  `email_address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`email_id`),
  KEY `email_address` (`email_address`),
  KEY `email_entity_id_entity_id_fkey` (`email_entity_id`),
  CONSTRAINT `email_entity_id_entity_id_fkey` FOREIGN KEY (`email_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Entity`
--

DROP TABLE IF EXISTS `Entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Entity` (
  `entity_id` int(8) NOT NULL AUTO_INCREMENT,
  `entity_mailing` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`entity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EntityRight`
--

DROP TABLE IF EXISTS `EntityRight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EntityRight` (
  `entityright_id` int(8) NOT NULL AUTO_INCREMENT,
  `entityright_entity_id` int(8) NOT NULL,
  `entityright_consumer_id` int(8) DEFAULT NULL,
  `entityright_access` int(1) NOT NULL DEFAULT '0',
  `entityright_read` int(1) NOT NULL DEFAULT '0',
  `entityright_write` int(1) NOT NULL DEFAULT '0',
  `entityright_admin` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`entityright_id`),
  KEY `entityright_entity_id_entity_id` (`entityright_entity_id`),
  KEY `entityright_consumer_id_entity_id` (`entityright_consumer_id`),
  CONSTRAINT `entityright_entity_id_entity_id` FOREIGN KEY (`entityright_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `entityright_consumer_id_entity_id` FOREIGN KEY (`entityright_consumer_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Event`
--

DROP TABLE IF EXISTS `Event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Event` (
  `event_id` int(8) NOT NULL AUTO_INCREMENT,
  `event_domain_id` int(8) NOT NULL,
  `event_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `event_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `event_userupdate` int(8) DEFAULT NULL,
  `event_usercreate` int(8) DEFAULT NULL,
  `event_ext_id` varchar(300) NOT NULL,
  `event_type` enum('VEVENT','VTODO','VJOURNAL','VFREEBUSY') DEFAULT 'VEVENT',
  `event_origin` varchar(255) NOT NULL DEFAULT '',
  `event_owner` int(8) DEFAULT NULL,
  `event_timezone` varchar(255) DEFAULT 'GMT',
  `event_opacity` enum('OPAQUE','TRANSPARENT') DEFAULT 'OPAQUE',
  `event_title` varchar(255) DEFAULT NULL,
  `event_location` varchar(255) DEFAULT NULL,
  `event_category1_id` int(8) DEFAULT NULL,
  `event_priority` int(2) DEFAULT NULL,
  `event_privacy` int(2) NOT NULL DEFAULT '0',
  `event_date` datetime DEFAULT NULL,
  `event_duration` int(8) NOT NULL DEFAULT '0',
  `event_allday` tinyint(1) DEFAULT '0',
  `event_repeatkind` varchar(20) NOT NULL DEFAULT 'none',
  `event_repeatfrequence` int(3) DEFAULT NULL,
  `event_repeatdays` varchar(7) DEFAULT NULL,
  `event_endrepeat` datetime DEFAULT NULL,
  `event_color` varchar(7) DEFAULT NULL,
  `event_completed` datetime DEFAULT NULL,
  `event_url` text,
  `event_allow_documents` tinyint(1) DEFAULT '0',
  `event_description` text,
  `event_properties` text,
  `event_tag_id` int(8) DEFAULT NULL,
  `event_sequence` int(8) DEFAULT '0',
  PRIMARY KEY (`event_id`),
  KEY `event_domain_id_domain_id_fkey` (`event_domain_id`),
  KEY `event_owner_userobm_id_fkey` (`event_owner`),
  KEY `event_userupdate_userobm_id_fkey` (`event_userupdate`),
  KEY `event_usercreate_userobm_id_fkey` (`event_usercreate`),
  KEY `event_category1_id_eventcategory1_id_fkey` (`event_category1_id`),
  KEY `event_tag_id_eventtag_id_fkey` (`event_tag_id`),
  CONSTRAINT `event_category1_id_eventcategory1_id_fkey` FOREIGN KEY (`event_category1_id`) REFERENCES `EventCategory1` (`eventcategory1_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `event_domain_id_domain_id_fkey` FOREIGN KEY (`event_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `event_owner_userobm_id_fkey` FOREIGN KEY (`event_owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `event_tag_id_eventtag_id_fkey` FOREIGN KEY (`event_tag_id`) REFERENCES `EventTag` (`eventtag_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `event_usercreate_userobm_id_fkey` FOREIGN KEY (`event_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `event_userupdate_userobm_id_fkey` FOREIGN KEY (`event_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventAlert`
--

DROP TABLE IF EXISTS `EventAlert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventAlert` (
  `eventalert_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eventalert_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `eventalert_userupdate` int(8) DEFAULT NULL,
  `eventalert_usercreate` int(8) DEFAULT NULL,
  `eventalert_event_id` int(8) DEFAULT NULL,
  `eventalert_user_id` int(8) DEFAULT NULL,
  `eventalert_duration` int(8) NOT NULL DEFAULT '0',
  KEY `idx_eventalert_user` (`eventalert_user_id`),
  KEY `eventalert_event_id_event_id_fkey` (`eventalert_event_id`),
  KEY `eventalert_userupdate_userobm_id_fkey` (`eventalert_userupdate`),
  KEY `eventalert_usercreate_userobm_id_fkey` (`eventalert_usercreate`),
  CONSTRAINT `eventalert_usercreate_userobm_id_fkey` FOREIGN KEY (`eventalert_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventalert_event_id_event_id_fkey` FOREIGN KEY (`eventalert_event_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventalert_userupdate_userobm_id_fkey` FOREIGN KEY (`eventalert_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventalert_user_id_userobm_id_fkey` FOREIGN KEY (`eventalert_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventCategory1`
--

DROP TABLE IF EXISTS `EventCategory1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventCategory1` (
  `eventcategory1_id` int(8) NOT NULL AUTO_INCREMENT,
  `eventcategory1_domain_id` int(8) NOT NULL,
  `eventcategory1_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eventcategory1_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `eventcategory1_userupdate` int(8) DEFAULT NULL,
  `eventcategory1_usercreate` int(8) DEFAULT NULL,
  `eventcategory1_code` varchar(10) DEFAULT '',
  `eventcategory1_label` varchar(128) DEFAULT NULL,
  `eventcategory1_color` char(6) DEFAULT NULL,
  PRIMARY KEY (`eventcategory1_id`),
  KEY `eventcategory1_domain_id_domain_id_fkey` (`eventcategory1_domain_id`),
  KEY `eventcategory1_userupdate_userobm_id_fkey` (`eventcategory1_userupdate`),
  KEY `eventcategory1_usercreate_userobm_id_fkey` (`eventcategory1_usercreate`),
  CONSTRAINT `eventcategory1_usercreate_userobm_id_fkey` FOREIGN KEY (`eventcategory1_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventcategory1_domain_id_domain_id_fkey` FOREIGN KEY (`eventcategory1_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventcategory1_userupdate_userobm_id_fkey` FOREIGN KEY (`eventcategory1_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventEntity`
--

DROP TABLE IF EXISTS `EventEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventEntity` (
  `evententity_entity_id` int(8) NOT NULL,
  `evententity_event_id` int(8) NOT NULL,
  PRIMARY KEY (`evententity_entity_id`,`evententity_event_id`),
  KEY `evententity_event_id_event_id_fkey` (`evententity_event_id`),
  CONSTRAINT `evententity_event_id_event_id_fkey` FOREIGN KEY (`evententity_event_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `evententity_entity_id_entity_id_fkey` FOREIGN KEY (`evententity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventException`
--

DROP TABLE IF EXISTS `EventException`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventException` (
  `eventexception_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eventexception_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `eventexception_userupdate` int(8) DEFAULT NULL,
  `eventexception_usercreate` int(8) DEFAULT NULL,
  `eventexception_parent_id` int(8) NOT NULL,
  `eventexception_child_id` int(8) DEFAULT NULL,
  `eventexception_date` datetime NOT NULL,
  PRIMARY KEY (`eventexception_parent_id`,`eventexception_date`),
  KEY `eventexception_parent_id_event_id_fkey` (`eventexception_parent_id`),
  KEY `eventexception_child_id_event_id_fkey` (`eventexception_child_id`),
  KEY `eventexception_userupdate_userobm_id_fkey` (`eventexception_userupdate`),
  KEY `eventexception_usercreate_userobm_id_fkey` (`eventexception_usercreate`),
  CONSTRAINT `eventexception_usercreate_userobm_id_fkey` FOREIGN KEY (`eventexception_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventexception_parent_id_event_id_fkey` FOREIGN KEY (`eventexception_parent_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventexception_child_id_event_id_fkey` FOREIGN KEY (`eventexception_child_id`) REFERENCES `Event` (`event_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventexception_userupdate_userobm_id_fkey` FOREIGN KEY (`eventexception_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventLink`
--

DROP TABLE IF EXISTS `EventLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventLink` (
  `eventlink_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eventlink_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `eventlink_userupdate` int(8) DEFAULT NULL,
  `eventlink_usercreate` int(8) DEFAULT NULL,
  `eventlink_event_id` int(8) NOT NULL,
  `eventlink_entity_id` int(8) NOT NULL,
  `eventlink_state` enum('NEEDS-ACTION','ACCEPTED','DECLINED','TENTATIVE','DELEGATED','COMPLETED','IN-PROGRESS') DEFAULT 'NEEDS-ACTION',
  `eventlink_required` enum('CHAIR','REQ','OPT','NON') DEFAULT 'REQ',
  `eventlink_percent` int(3) DEFAULT '0',
  `eventlink_is_organizer` tinyint(1) DEFAULT '0',
  `eventlink_comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`eventlink_event_id`,`eventlink_entity_id`),
  KEY `eventlink_userupdate_userobm_id_fkey` (`eventlink_userupdate`),
  KEY `eventlink_usercreate_userobm_id_fkey` (`eventlink_usercreate`),
  KEY `eventlink_entity_id_entity_id_fkey` (`eventlink_entity_id`),
  CONSTRAINT `eventlink_entity_id_entity_id_fkey` FOREIGN KEY (`eventlink_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventlink_event_id_event_id_fkey` FOREIGN KEY (`eventlink_event_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventlink_usercreate_userobm_id_fkey` FOREIGN KEY (`eventlink_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventlink_userupdate_userobm_id_fkey` FOREIGN KEY (`eventlink_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTag`
--

DROP TABLE IF EXISTS `EventTag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTag` (
  `eventtag_id` int(8) NOT NULL AUTO_INCREMENT,
  `eventtag_user_id` int(8) NOT NULL,
  `eventtag_label` varchar(128) DEFAULT NULL,
  `eventtag_color` char(7) DEFAULT NULL,
  PRIMARY KEY (`eventtag_id`),
  KEY `eventtag_label_fkey` (`eventtag_label`),
  KEY `eventtag_user_id_userobm_id_fkey` (`eventtag_user_id`),
  CONSTRAINT `eventtag_user_id_userobm_id_fkey` FOREIGN KEY (`eventtag_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTemplate`
--

DROP TABLE IF EXISTS `EventTemplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTemplate` (
  `eventtemplate_id` int(8) NOT NULL AUTO_INCREMENT,
  `eventtemplate_domain_id` int(8) NOT NULL,
  `eventtemplate_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eventtemplate_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `eventtemplate_userupdate` int(8) DEFAULT NULL,
  `eventtemplate_usercreate` int(8) DEFAULT NULL,
  `eventtemplate_owner` int(8) DEFAULT NULL,
  `eventtemplate_name` varchar(255) DEFAULT NULL,
  `eventtemplate_title` varchar(255) DEFAULT NULL,
  `eventtemplate_location` varchar(100) DEFAULT NULL,
  `eventtemplate_category1_id` int(8) DEFAULT NULL,
  `eventtemplate_priority` int(2) DEFAULT NULL,
  `eventtemplate_privacy` int(2) NOT NULL DEFAULT '0',
  `eventtemplate_date` datetime DEFAULT NULL,
  `eventtemplate_duration` int(8) NOT NULL DEFAULT '0',
  `eventtemplate_allday` tinyint(1) DEFAULT '0',
  `eventtemplate_repeatkind` varchar(20) NOT NULL DEFAULT 'none',
  `eventtemplate_repeatfrequence` int(3) DEFAULT NULL,
  `eventtemplate_repeatdays` varchar(7) DEFAULT NULL,
  `eventtemplate_endrepeat` datetime DEFAULT NULL,
  `eventtemplate_allow_documents` tinyint(1) DEFAULT '0',
  `eventtemplate_alert` int(8) NOT NULL DEFAULT '0',
  `eventtemplate_description` text,
  `eventtemplate_properties` text,
  `eventtemplate_tag_id` int(8) DEFAULT NULL,
  `eventtemplate_user_ids` text,
  `eventtemplate_contact_ids` text,
  `eventtemplate_resource_ids` text,
  `eventtemplate_document_ids` text,
  `eventtemplate_organizer` int(11) DEFAULT '0',
  `eventtemplate_group_ids` text,
  `eventtemplate_force_insertion` tinyint(1) DEFAULT '0',
  `eventtemplate_opacity` enum('OPAQUE','TRANSPARENT') DEFAULT 'OPAQUE',
  `eventtemplate_show_user_calendar` tinyint(1) DEFAULT '0',
  `eventtemplate_show_resource_calendar` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`eventtemplate_id`),
  KEY `eventtemplate_domain_id_domain_id_fkey` (`eventtemplate_domain_id`),
  KEY `eventtemplate_owner_userobm_id_fkey` (`eventtemplate_owner`),
  KEY `eventtemplate_userupdate_userobm_id_fkey` (`eventtemplate_userupdate`),
  KEY `eventtemplate_usercreate_userobm_id_fkey` (`eventtemplate_usercreate`),
  KEY `eventtemplate_category1_id_eventcategory1_id_fkey` (`eventtemplate_category1_id`),
  KEY `eventtemplate_tag_id_eventtag_id_fkey` (`eventtemplate_tag_id`),
  CONSTRAINT `eventtemplate_category1_id_eventcategory1_id_fkey` FOREIGN KEY (`eventtemplate_category1_id`) REFERENCES `EventCategory1` (`eventcategory1_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventtemplate_domain_id_domain_id_fkey` FOREIGN KEY (`eventtemplate_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventtemplate_owner_userobm_id_fkey` FOREIGN KEY (`eventtemplate_owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eventtemplate_tag_id_eventtag_id_fkey` FOREIGN KEY (`eventtemplate_tag_id`) REFERENCES `EventTag` (`eventtag_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventtemplate_usercreate_userobm_id_fkey` FOREIGN KEY (`eventtemplate_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eventtemplate_userupdate_userobm_id_fkey` FOREIGN KEY (`eventtemplate_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupEntity`
--

DROP TABLE IF EXISTS `GroupEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupEntity` (
  `groupentity_entity_id` int(8) NOT NULL,
  `groupentity_group_id` int(8) NOT NULL,
  PRIMARY KEY (`groupentity_entity_id`,`groupentity_group_id`),
  KEY `groupentity_group_id_group_id_fkey` (`groupentity_group_id`),
  CONSTRAINT `groupentity_group_id_group_id_fkey` FOREIGN KEY (`groupentity_group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `groupentity_entity_id_entity_id_fkey` FOREIGN KEY (`groupentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupGroup`
--

DROP TABLE IF EXISTS `GroupGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupGroup` (
  `groupgroup_parent_id` int(8) NOT NULL,
  `groupgroup_child_id` int(8) NOT NULL,
  PRIMARY KEY (`groupgroup_parent_id`,`groupgroup_child_id`),
  KEY `groupgroup_child_id_group_id_fkey` (`groupgroup_child_id`),
  CONSTRAINT `groupgroup_child_id_group_id_fkey` FOREIGN KEY (`groupgroup_child_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `groupgroup_parent_id_group_id_fkey` FOREIGN KEY (`groupgroup_parent_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Host`
--

DROP TABLE IF EXISTS `Host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Host` (
  `host_id` int(8) NOT NULL AUTO_INCREMENT,
  `host_domain_id` int(8) NOT NULL,
  `host_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `host_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `host_userupdate` int(8) DEFAULT NULL,
  `host_usercreate` int(8) DEFAULT NULL,
  `host_uid` int(8) DEFAULT NULL,
  `host_gid` int(8) DEFAULT NULL,
  `host_archive` int(1) NOT NULL DEFAULT '0',
  `host_name` varchar(32) NOT NULL,
  `host_fqdn` varchar(255) DEFAULT NULL,
  `host_ip` varchar(16) DEFAULT NULL,
  `host_delegation` varchar(256) DEFAULT '',
  `host_description` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`host_id`),
  UNIQUE KEY `host_name` (`host_name`),
  KEY `host_domain_id_domain_id_fkey` (`host_domain_id`),
  KEY `host_userupdate_userobm_id_fkey` (`host_userupdate`),
  KEY `host_usercreate_userobm_id_fkey` (`host_usercreate`),
  CONSTRAINT `host_usercreate_userobm_id_fkey` FOREIGN KEY (`host_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `host_domain_id_domain_id_fkey` FOREIGN KEY (`host_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `host_userupdate_userobm_id_fkey` FOREIGN KEY (`host_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `HostEntity`
--

DROP TABLE IF EXISTS `HostEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HostEntity` (
  `hostentity_entity_id` int(8) NOT NULL,
  `hostentity_host_id` int(8) NOT NULL,
  PRIMARY KEY (`hostentity_entity_id`,`hostentity_host_id`),
  KEY `hostentity_host_id_host_id_fkey` (`hostentity_host_id`),
  CONSTRAINT `hostentity_host_id_host_id_fkey` FOREIGN KEY (`hostentity_host_id`) REFERENCES `Host` (`host_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `hostentity_entity_id_entity_id_fkey` FOREIGN KEY (`hostentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IM`
--

DROP TABLE IF EXISTS `IM`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IM` (
  `im_id` int(8) NOT NULL AUTO_INCREMENT,
  `im_entity_id` int(8) NOT NULL,
  `im_label` varchar(255) NOT NULL,
  `im_address` varchar(255) DEFAULT NULL,
  `im_protocol` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`im_id`),
  KEY `im_address` (`im_address`),
  KEY `im_entity_id_entity_id_fkey` (`im_entity_id`),
  CONSTRAINT `im_entity_id_entity_id_fkey` FOREIGN KEY (`im_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Import`
--

DROP TABLE IF EXISTS `Import`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Import` (
  `import_id` int(8) NOT NULL AUTO_INCREMENT,
  `import_domain_id` int(8) NOT NULL,
  `import_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `import_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `import_userupdate` int(8) DEFAULT NULL,
  `import_usercreate` int(8) DEFAULT NULL,
  `import_name` varchar(64) NOT NULL,
  `import_datasource_id` int(8) DEFAULT NULL,
  `import_marketingmanager_id` int(8) DEFAULT NULL,
  `import_separator` varchar(3) DEFAULT NULL,
  `import_enclosed` char(1) DEFAULT NULL,
  `import_desc` text,
  PRIMARY KEY (`import_id`),
  UNIQUE KEY `import_name` (`import_name`),
  KEY `import_domain_id_domain_id_fkey` (`import_domain_id`),
  KEY `import_userupdate_userobm_id_fkey` (`import_userupdate`),
  KEY `import_usercreate_userobm_id_fkey` (`import_usercreate`),
  KEY `import_datasource_id_datasource_id_fkey` (`import_datasource_id`),
  KEY `import_marketingmanager_id_userobm_id_fkey` (`import_marketingmanager_id`),
  CONSTRAINT `import_marketingmanager_id_userobm_id_fkey` FOREIGN KEY (`import_marketingmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `import_datasource_id_datasource_id_fkey` FOREIGN KEY (`import_datasource_id`) REFERENCES `DataSource` (`datasource_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `import_domain_id_domain_id_fkey` FOREIGN KEY (`import_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `import_usercreate_userobm_id_fkey` FOREIGN KEY (`import_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `import_userupdate_userobm_id_fkey` FOREIGN KEY (`import_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ImportEntity`
--

DROP TABLE IF EXISTS `ImportEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ImportEntity` (
  `importentity_entity_id` int(8) NOT NULL,
  `importentity_import_id` int(8) NOT NULL,
  PRIMARY KEY (`importentity_entity_id`,`importentity_import_id`),
  KEY `importentity_import_id_import_id_fkey` (`importentity_import_id`),
  CONSTRAINT `importentity_import_id_import_id_fkey` FOREIGN KEY (`importentity_import_id`) REFERENCES `Import` (`import_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `importentity_entity_id_entity_id_fkey` FOREIGN KEY (`importentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Incident`
--

DROP TABLE IF EXISTS `Incident`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Incident` (
  `incident_id` int(8) NOT NULL AUTO_INCREMENT,
  `incident_domain_id` int(8) NOT NULL,
  `incident_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `incident_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `incident_userupdate` int(8) DEFAULT NULL,
  `incident_usercreate` int(8) DEFAULT NULL,
  `incident_contract_id` int(8) NOT NULL,
  `incident_label` varchar(100) DEFAULT NULL,
  `incident_reference` varchar(32) DEFAULT NULL,
  `incident_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `incident_priority_id` int(8) DEFAULT NULL,
  `incident_status_id` int(8) DEFAULT NULL,
  `incident_resolutiontype_id` int(11) DEFAULT NULL,
  `incident_logger` int(8) DEFAULT NULL,
  `incident_owner` int(8) DEFAULT NULL,
  `incident_duration` char(4) DEFAULT '0',
  `incident_archive` char(1) NOT NULL DEFAULT '0',
  `incident_comment` text,
  `incident_resolution` text,
  PRIMARY KEY (`incident_id`),
  KEY `incident_domain_id_domain_id_fkey` (`incident_domain_id`),
  KEY `incident_contract_id_contract_id_fkey` (`incident_contract_id`),
  KEY `incident_userupdate_userobm_id_fkey` (`incident_userupdate`),
  KEY `incident_usercreate_userobm_id_fkey` (`incident_usercreate`),
  KEY `incident_priority_id_incidentpriority_id_fkey` (`incident_priority_id`),
  KEY `incident_status_id_incidentstatus_id_fkey` (`incident_status_id`),
  KEY `incident_resolutiontype_id_incidentresolutiontype_id_fkey` (`incident_resolutiontype_id`),
  KEY `incident_logger_userobm_id_fkey` (`incident_logger`),
  KEY `incident_owner_userobm_id_fkey` (`incident_owner`),
  CONSTRAINT `incident_owner_userobm_id_fkey` FOREIGN KEY (`incident_owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incident_contract_id_contract_id_fkey` FOREIGN KEY (`incident_contract_id`) REFERENCES `Contract` (`contract_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incident_domain_id_domain_id_fkey` FOREIGN KEY (`incident_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incident_logger_userobm_id_fkey` FOREIGN KEY (`incident_logger`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incident_priority_id_incidentpriority_id_fkey` FOREIGN KEY (`incident_priority_id`) REFERENCES `IncidentPriority` (`incidentpriority_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incident_resolutiontype_id_incidentresolutiontype_id_fkey` FOREIGN KEY (`incident_resolutiontype_id`) REFERENCES `IncidentResolutionType` (`incidentresolutiontype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incident_status_id_incidentstatus_id_fkey` FOREIGN KEY (`incident_status_id`) REFERENCES `IncidentStatus` (`incidentstatus_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incident_usercreate_userobm_id_fkey` FOREIGN KEY (`incident_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incident_userupdate_userobm_id_fkey` FOREIGN KEY (`incident_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IncidentEntity`
--

DROP TABLE IF EXISTS `IncidentEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IncidentEntity` (
  `incidententity_entity_id` int(8) NOT NULL,
  `incidententity_incident_id` int(8) NOT NULL,
  PRIMARY KEY (`incidententity_entity_id`,`incidententity_incident_id`),
  KEY `incidententity_incident_id_incident_id_fkey` (`incidententity_incident_id`),
  CONSTRAINT `incidententity_incident_id_incident_id_fkey` FOREIGN KEY (`incidententity_incident_id`) REFERENCES `Incident` (`incident_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidententity_entity_id_entity_id_fkey` FOREIGN KEY (`incidententity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IncidentPriority`
--

DROP TABLE IF EXISTS `IncidentPriority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IncidentPriority` (
  `incidentpriority_id` int(8) NOT NULL AUTO_INCREMENT,
  `incidentpriority_domain_id` int(8) NOT NULL,
  `incidentpriority_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `incidentpriority_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `incidentpriority_userupdate` int(8) DEFAULT NULL,
  `incidentpriority_usercreate` int(8) DEFAULT NULL,
  `incidentpriority_code` varchar(10) DEFAULT '',
  `incidentpriority_label` varchar(32) DEFAULT NULL,
  `incidentpriority_color` char(6) DEFAULT NULL,
  PRIMARY KEY (`incidentpriority_id`),
  KEY `incidentpriority_domain_id_domain_id_fkey` (`incidentpriority_domain_id`),
  KEY `incidentpriority_userupdate_userobm_id_fkey` (`incidentpriority_userupdate`),
  KEY `incidentpriority_usercreate_userobm_id_fkey` (`incidentpriority_usercreate`),
  CONSTRAINT `incidentpriority_usercreate_userobm_id_fkey` FOREIGN KEY (`incidentpriority_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incidentpriority_domain_id_domain_id_fkey` FOREIGN KEY (`incidentpriority_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidentpriority_userupdate_userobm_id_fkey` FOREIGN KEY (`incidentpriority_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IncidentResolutionType`
--

DROP TABLE IF EXISTS `IncidentResolutionType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IncidentResolutionType` (
  `incidentresolutiontype_id` int(8) NOT NULL AUTO_INCREMENT,
  `incidentresolutiontype_domain_id` int(8) NOT NULL,
  `incidentresolutiontype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `incidentresolutiontype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `incidentresolutiontype_userupdate` int(8) DEFAULT NULL,
  `incidentresolutiontype_usercreate` int(8) DEFAULT NULL,
  `incidentresolutiontype_code` varchar(10) DEFAULT '',
  `incidentresolutiontype_label` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`incidentresolutiontype_id`),
  KEY `incidentresolutiontype_domain_id_domain_id_fkey` (`incidentresolutiontype_domain_id`),
  KEY `incidentresolutiontype_userupdate_userobm_id_fkey` (`incidentresolutiontype_userupdate`),
  KEY `incidentresolutiontype_usercreate_userobm_id_fkey` (`incidentresolutiontype_usercreate`),
  CONSTRAINT `incidentresolutiontype_usercreate_userobm_id_fkey` FOREIGN KEY (`incidentresolutiontype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incidentresolutiontype_domain_id_domain_id_fkey` FOREIGN KEY (`incidentresolutiontype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidentresolutiontype_userupdate_userobm_id_fkey` FOREIGN KEY (`incidentresolutiontype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `IncidentStatus`
--

DROP TABLE IF EXISTS `IncidentStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IncidentStatus` (
  `incidentstatus_id` int(8) NOT NULL AUTO_INCREMENT,
  `incidentstatus_domain_id` int(8) NOT NULL,
  `incidentstatus_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `incidentstatus_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `incidentstatus_userupdate` int(8) DEFAULT NULL,
  `incidentstatus_usercreate` int(8) DEFAULT NULL,
  `incidentstatus_code` varchar(10) DEFAULT '',
  `incidentstatus_label` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`incidentstatus_id`),
  KEY `incidentstatus_domain_id_domain_id_fkey` (`incidentstatus_domain_id`),
  KEY `incidentstatus_userupdate_userobm_id_fkey` (`incidentstatus_userupdate`),
  KEY `incidentstatus_usercreate_userobm_id_fkey` (`incidentstatus_usercreate`),
  CONSTRAINT `incidentstatus_usercreate_userobm_id_fkey` FOREIGN KEY (`incidentstatus_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `incidentstatus_domain_id_domain_id_fkey` FOREIGN KEY (`incidentstatus_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `incidentstatus_userupdate_userobm_id_fkey` FOREIGN KEY (`incidentstatus_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Invoice`
--

DROP TABLE IF EXISTS `Invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Invoice` (
  `invoice_id` int(8) NOT NULL AUTO_INCREMENT,
  `invoice_domain_id` int(8) NOT NULL,
  `invoice_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `invoice_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `invoice_userupdate` int(8) DEFAULT NULL,
  `invoice_usercreate` int(8) DEFAULT NULL,
  `invoice_company_id` int(8) NOT NULL,
  `invoice_deal_id` int(8) DEFAULT NULL,
  `invoice_project_id` int(8) DEFAULT NULL,
  `invoice_number` varchar(10) DEFAULT '0',
  `invoice_label` varchar(40) NOT NULL DEFAULT '',
  `invoice_amount_ht` double(10,2) DEFAULT NULL,
  `invoice_amount_ttc` double(10,2) DEFAULT NULL,
  `invoice_status_id` int(4) NOT NULL,
  `invoice_date` date NOT NULL DEFAULT '0000-00-00',
  `invoice_expiration_date` date DEFAULT NULL,
  `invoice_payment_date` date DEFAULT NULL,
  `invoice_inout` char(1) DEFAULT NULL,
  `invoice_credit_memo` int(1) NOT NULL DEFAULT '0',
  `invoice_archive` char(1) NOT NULL DEFAULT '0',
  `invoice_comment` text,
  PRIMARY KEY (`invoice_id`),
  KEY `invoice_domain_id_domain_id_fkey` (`invoice_domain_id`),
  KEY `invoice_company_id_company_id_fkey` (`invoice_company_id`),
  KEY `invoice_project_id_project_id_fkey` (`invoice_project_id`),
  KEY `invoice_deal_id_deal_id_fkey` (`invoice_deal_id`),
  KEY `invoice_userupdate_userobm_id_fkey` (`invoice_userupdate`),
  KEY `invoice_usercreate_userobm_id_fkey` (`invoice_usercreate`),
  CONSTRAINT `invoice_usercreate_userobm_id_fkey` FOREIGN KEY (`invoice_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `invoice_company_id_company_id_fkey` FOREIGN KEY (`invoice_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `invoice_deal_id_deal_id_fkey` FOREIGN KEY (`invoice_deal_id`) REFERENCES `Deal` (`deal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `invoice_domain_id_domain_id_fkey` FOREIGN KEY (`invoice_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `invoice_project_id_project_id_fkey` FOREIGN KEY (`invoice_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `invoice_userupdate_userobm_id_fkey` FOREIGN KEY (`invoice_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `InvoiceEntity`
--

DROP TABLE IF EXISTS `InvoiceEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `InvoiceEntity` (
  `invoiceentity_entity_id` int(8) NOT NULL,
  `invoiceentity_invoice_id` int(8) NOT NULL,
  PRIMARY KEY (`invoiceentity_entity_id`,`invoiceentity_invoice_id`),
  KEY `invoiceentity_invoice_id_invoice_id_fkey` (`invoiceentity_invoice_id`),
  CONSTRAINT `invoiceentity_invoice_id_invoice_id_fkey` FOREIGN KEY (`invoiceentity_invoice_id`) REFERENCES `Invoice` (`invoice_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `invoiceentity_entity_id_entity_id_fkey` FOREIGN KEY (`invoiceentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Kind`
--

DROP TABLE IF EXISTS `Kind`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Kind` (
  `kind_id` int(8) NOT NULL AUTO_INCREMENT,
  `kind_domain_id` int(8) NOT NULL,
  `kind_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `kind_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `kind_userupdate` int(8) DEFAULT NULL,
  `kind_usercreate` int(8) DEFAULT NULL,
  `kind_minilabel` varchar(64) DEFAULT NULL,
  `kind_header` varchar(64) DEFAULT NULL,
  `kind_lang` char(2) DEFAULT NULL,
  `kind_default` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`kind_id`),
  KEY `kind_domain_id_domain_id_fkey` (`kind_domain_id`),
  KEY `kind_userupdate_userobm_id_fkey` (`kind_userupdate`),
  KEY `kind_usercreate_userobm_id_fkey` (`kind_usercreate`),
  CONSTRAINT `kind_usercreate_userobm_id_fkey` FOREIGN KEY (`kind_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `kind_domain_id_domain_id_fkey` FOREIGN KEY (`kind_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `kind_userupdate_userobm_id_fkey` FOREIGN KEY (`kind_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Lead`
--

DROP TABLE IF EXISTS `Lead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Lead` (
  `lead_id` int(8) NOT NULL AUTO_INCREMENT,
  `lead_domain_id` int(8) NOT NULL,
  `lead_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lead_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `lead_userupdate` int(8) DEFAULT NULL,
  `lead_usercreate` int(8) DEFAULT NULL,
  `lead_source_id` int(8) DEFAULT NULL,
  `lead_manager_id` int(8) DEFAULT NULL,
  `lead_company_id` int(8) NOT NULL,
  `lead_contact_id` int(8) DEFAULT NULL,
  `lead_priority` int(2) DEFAULT '0',
  `lead_privacy` int(2) NOT NULL DEFAULT '0',
  `lead_name` varchar(64) DEFAULT NULL,
  `lead_date` date DEFAULT NULL,
  `lead_datealarm` date DEFAULT NULL,
  `lead_status_id` int(2) DEFAULT NULL,
  `lead_archive` char(1) DEFAULT '0',
  `lead_todo` varchar(128) DEFAULT NULL,
  `lead_comment` text,
  PRIMARY KEY (`lead_id`),
  KEY `lead_domain_id_domain_id_fkey` (`lead_domain_id`),
  KEY `lead_company_id_company_id_fkey` (`lead_company_id`),
  KEY `lead_userupdate_userobm_id_fkey` (`lead_userupdate`),
  KEY `lead_usercreate_userobm_id_fkey` (`lead_usercreate`),
  KEY `lead_source_id_leadsource_id_fkey` (`lead_source_id`),
  KEY `lead_manager_id_userobm_id_fkey` (`lead_manager_id`),
  KEY `lead_contact_id_contact_id_fkey` (`lead_contact_id`),
  KEY `lead_status_id_leadstatus_id_fkey` (`lead_status_id`),
  CONSTRAINT `lead_status_id_leadstatus_id_fkey` FOREIGN KEY (`lead_status_id`) REFERENCES `LeadStatus` (`leadstatus_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `lead_company_id_company_id_fkey` FOREIGN KEY (`lead_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `lead_contact_id_contact_id_fkey` FOREIGN KEY (`lead_contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `lead_domain_id_domain_id_fkey` FOREIGN KEY (`lead_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `lead_manager_id_userobm_id_fkey` FOREIGN KEY (`lead_manager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `lead_source_id_leadsource_id_fkey` FOREIGN KEY (`lead_source_id`) REFERENCES `LeadSource` (`leadsource_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `lead_usercreate_userobm_id_fkey` FOREIGN KEY (`lead_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `lead_userupdate_userobm_id_fkey` FOREIGN KEY (`lead_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LeadEntity`
--

DROP TABLE IF EXISTS `LeadEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LeadEntity` (
  `leadentity_entity_id` int(8) NOT NULL,
  `leadentity_lead_id` int(8) NOT NULL,
  PRIMARY KEY (`leadentity_entity_id`,`leadentity_lead_id`),
  KEY `leadentity_lead_id_lead_id_fkey` (`leadentity_lead_id`),
  CONSTRAINT `leadentity_lead_id_lead_id_fkey` FOREIGN KEY (`leadentity_lead_id`) REFERENCES `Lead` (`lead_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `leadentity_entity_id_entity_id_fkey` FOREIGN KEY (`leadentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LeadSource`
--

DROP TABLE IF EXISTS `LeadSource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LeadSource` (
  `leadsource_id` int(8) NOT NULL AUTO_INCREMENT,
  `leadsource_domain_id` int(8) NOT NULL,
  `leadsource_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `leadsource_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `leadsource_userupdate` int(8) DEFAULT NULL,
  `leadsource_usercreate` int(8) DEFAULT NULL,
  `leadsource_code` varchar(10) DEFAULT '',
  `leadsource_label` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`leadsource_id`),
  KEY `leadsource_domain_id_domain_id_fkey` (`leadsource_domain_id`),
  KEY `leadsource_userupdate_userobm_id_fkey` (`leadsource_userupdate`),
  KEY `leadsource_usercreate_userobm_id_fkey` (`leadsource_usercreate`),
  CONSTRAINT `leadsource_usercreate_userobm_id_fkey` FOREIGN KEY (`leadsource_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `leadsource_domain_id_domain_id_fkey` FOREIGN KEY (`leadsource_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `leadsource_userupdate_userobm_id_fkey` FOREIGN KEY (`leadsource_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LeadStatus`
--

DROP TABLE IF EXISTS `LeadStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LeadStatus` (
  `leadstatus_id` int(2) NOT NULL AUTO_INCREMENT,
  `leadstatus_domain_id` int(8) NOT NULL,
  `leadstatus_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `leadstatus_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `leadstatus_userupdate` int(8) DEFAULT NULL,
  `leadstatus_usercreate` int(8) DEFAULT NULL,
  `leadstatus_code` varchar(10) DEFAULT NULL,
  `leadstatus_label` varchar(24) DEFAULT NULL,
  PRIMARY KEY (`leadstatus_id`),
  KEY `leadstatus_domain_id_domain_id_fkey` (`leadstatus_domain_id`),
  KEY `leadstatus_userupdate_userobm_id_fkey` (`leadstatus_userupdate`),
  KEY `leadstatus_usercreate_userobm_id_fkey` (`leadstatus_usercreate`),
  CONSTRAINT `leadstatus_usercreate_userobm_id_fkey` FOREIGN KEY (`leadstatus_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `leadstatus_domain_id_domain_id_fkey` FOREIGN KEY (`leadstatus_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `leadstatus_userupdate_userobm_id_fkey` FOREIGN KEY (`leadstatus_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `List`
--

DROP TABLE IF EXISTS `List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `List` (
  `list_id` int(8) NOT NULL AUTO_INCREMENT,
  `list_domain_id` int(8) NOT NULL,
  `list_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `list_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `list_userupdate` int(8) DEFAULT NULL,
  `list_usercreate` int(8) DEFAULT NULL,
  `list_privacy` int(2) NOT NULL DEFAULT '0',
  `list_name` varchar(64) NOT NULL,
  `list_subject` varchar(128) DEFAULT NULL,
  `list_email` varchar(128) DEFAULT NULL,
  `list_mode` int(1) DEFAULT '0',
  `list_mailing_ok` int(1) DEFAULT '0',
  `list_contact_archive` int(1) DEFAULT '0',
  `list_info_publication` int(1) DEFAULT '0',
  `list_static_nb` int(10) DEFAULT '0',
  `list_query_nb` int(10) DEFAULT '0',
  `list_query` text,
  `list_structure` text,
  PRIMARY KEY (`list_id`),
  UNIQUE KEY `list_name` (`list_name`),
  KEY `list_domain_id_domain_id_fkey` (`list_domain_id`),
  KEY `list_userupdate_userobm_id_fkey` (`list_userupdate`),
  KEY `list_usercreate_userobm_id_fkey` (`list_usercreate`),
  CONSTRAINT `list_usercreate_userobm_id_fkey` FOREIGN KEY (`list_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `list_domain_id_domain_id_fkey` FOREIGN KEY (`list_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `list_userupdate_userobm_id_fkey` FOREIGN KEY (`list_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ListEntity`
--

DROP TABLE IF EXISTS `ListEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ListEntity` (
  `listentity_entity_id` int(8) NOT NULL,
  `listentity_list_id` int(8) NOT NULL,
  PRIMARY KEY (`listentity_entity_id`,`listentity_list_id`),
  KEY `listentity_list_id_list_id_fkey` (`listentity_list_id`),
  CONSTRAINT `listentity_list_id_list_id_fkey` FOREIGN KEY (`listentity_list_id`) REFERENCES `List` (`list_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `listentity_entity_id_entity_id_fkey` FOREIGN KEY (`listentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LogModifiedAddress`
--

DROP TABLE IF EXISTS `LogModifiedAddress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LogModifiedAddress` (
  `contact_id` int(8) NOT NULL DEFAULT '0',
  `contact_firstname` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `contact_lastname` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `address_street` text CHARACTER SET utf8,
  `address_zipcode` varchar(14) CHARACTER SET utf8 DEFAULT NULL,
  `address_town` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `address_expresspostal` varchar(16) CHARACTER SET utf8 DEFAULT NULL,
  `address_state` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
  `address_country` char(2) CHARACTER SET utf8 DEFAULT NULL,
  `address_label` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `address_entity_id` int(8) NOT NULL,
  `contactentity_contact_id` int(8) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MailShare`
--

DROP TABLE IF EXISTS `MailShare`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MailShare` (
  `mailshare_id` int(8) NOT NULL AUTO_INCREMENT,
  `mailshare_domain_id` int(8) NOT NULL,
  `mailshare_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mailshare_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `mailshare_userupdate` int(8) DEFAULT NULL,
  `mailshare_usercreate` int(8) DEFAULT NULL,
  `mailshare_name` varchar(32) DEFAULT NULL,
  `mailshare_archive` int(1) NOT NULL DEFAULT '0',
  `mailshare_quota` int(11) NOT NULL DEFAULT '0',
  `mailshare_mail_server_id` int(8) DEFAULT NULL,
  `mailshare_delegation` varchar(256) DEFAULT '',
  `mailshare_description` varchar(255) DEFAULT NULL,
  `mailshare_email` text,
  PRIMARY KEY (`mailshare_id`),
  KEY `mailshare_domain_id_domain_id_fkey` (`mailshare_domain_id`),
  KEY `mailshare_mail_server_id_mailserver_id_fkey` (`mailshare_mail_server_id`),
  KEY `mailshare_userupdate_userobm_id_fkey` (`mailshare_userupdate`),
  KEY `mailshare_usercreate_userobm_id_fkey` (`mailshare_usercreate`),
  CONSTRAINT `mailshare_usercreate_userobm_id_fkey` FOREIGN KEY (`mailshare_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `mailshare_domain_id_domain_id_fkey` FOREIGN KEY (`mailshare_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mailshare_mail_server_id_mailserver_id_fkey` FOREIGN KEY (`mailshare_mail_server_id`) REFERENCES `Host` (`host_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mailshare_userupdate_userobm_id_fkey` FOREIGN KEY (`mailshare_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MailboxEntity`
--

DROP TABLE IF EXISTS `MailboxEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MailboxEntity` (
  `mailboxentity_entity_id` int(8) NOT NULL,
  `mailboxentity_mailbox_id` int(8) NOT NULL,
  PRIMARY KEY (`mailboxentity_entity_id`,`mailboxentity_mailbox_id`),
  KEY `mailboxentity_mailbox_id_mailbox_id_fkey` (`mailboxentity_mailbox_id`),
  CONSTRAINT `mailboxentity_mailbox_id_mailbox_id_fkey` FOREIGN KEY (`mailboxentity_mailbox_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mailboxentity_entity_id_entity_id_fkey` FOREIGN KEY (`mailboxentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MailingList`
--

DROP TABLE IF EXISTS `MailingList`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MailingList` (
  `mailinglist_id` int(8) NOT NULL AUTO_INCREMENT,
  `mailinglist_domain_id` int(8) NOT NULL,
  `mailinglist_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mailinglist_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `mailinglist_userupdate` int(8) DEFAULT NULL,
  `mailinglist_usercreate` int(8) DEFAULT NULL,
  `mailinglist_owner` int(8) NOT NULL,
  `mailinglist_name` varchar(64) NOT NULL,
  PRIMARY KEY (`mailinglist_id`),
  KEY `linglist_domain_id_domain_id_fkey` (`mailinglist_domain_id`),
  KEY `mailinglist_userupdate_userobm_id_fkey` (`mailinglist_userupdate`),
  KEY `mailinglist_usercreate_userobm_id_fkey` (`mailinglist_usercreate`),
  KEY `mailinglist_owner_userobm_id_fkey` (`mailinglist_owner`),
  CONSTRAINT `mailinglist_usercreate_userobm_id_fkey` FOREIGN KEY (`mailinglist_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `mailinglist_domain_id_domain_id_fkey` FOREIGN KEY (`mailinglist_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mailinglist_userupdate_userobm_id_fkey` FOREIGN KEY (`mailinglist_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `mailinglist_owner_userobm_id_fkey` FOREIGN KEY (`mailinglist_owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MailingListEmail`
--

DROP TABLE IF EXISTS `MailingListEmail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MailingListEmail` (
  `mailinglistemail_id` int(8) NOT NULL AUTO_INCREMENT,
  `mailinglistemail_mailinglist_id` int(8) NOT NULL,
  `mailinglistemail_label` varchar(255) NOT NULL,
  `mailinglistemail_address` varchar(255) NOT NULL,
  PRIMARY KEY (`mailinglistemail_id`),
  KEY `mailinglistemail_mailinglist_id_mailinglist_id_fkey` (`mailinglistemail_mailinglist_id`),
  CONSTRAINT `mailinglistemail_mailinglist_id_mailinglist_id_fkey` FOREIGN KEY (`mailinglistemail_mailinglist_id`) REFERENCES `MailingList` (`mailinglist_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MailshareEntity`
--

DROP TABLE IF EXISTS `MailshareEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MailshareEntity` (
  `mailshareentity_entity_id` int(8) NOT NULL,
  `mailshareentity_mailshare_id` int(8) NOT NULL,
  PRIMARY KEY (`mailshareentity_entity_id`,`mailshareentity_mailshare_id`),
  KEY `mailshareentity_mailshare_id_mailshare_id_fkey` (`mailshareentity_mailshare_id`),
  CONSTRAINT `mailshareentity_mailshare_id_mailshare_id_fkey` FOREIGN KEY (`mailshareentity_mailshare_id`) REFERENCES `MailShare` (`mailshare_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mailshareentity_entity_id_entity_id_fkey` FOREIGN KEY (`mailshareentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OGroup`
--

DROP TABLE IF EXISTS `OGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OGroup` (
  `ogroup_id` int(8) NOT NULL AUTO_INCREMENT,
  `ogroup_domain_id` int(8) NOT NULL,
  `ogroup_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ogroup_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ogroup_userupdate` int(8) DEFAULT NULL,
  `ogroup_usercreate` int(8) DEFAULT NULL,
  `ogroup_organizationalchart_id` int(8) NOT NULL,
  `ogroup_parent_id` int(8) DEFAULT NULL,
  `ogroup_name` varchar(32) NOT NULL,
  `ogroup_level` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`ogroup_id`),
  KEY `ogroup_domain_id_domain_id_fkey` (`ogroup_domain_id`),
  KEY `ogroup_organizationalchart_id_organizationalchart_id_fkey` (`ogroup_organizationalchart_id`),
  KEY `ogroup_parent_id_ogroup_id_fkey` (`ogroup_parent_id`),
  KEY `ogroup_userupdate_userobm_id_fkey` (`ogroup_userupdate`),
  KEY `ogroup_usercreate_userobm_id_fkey` (`ogroup_usercreate`),
  CONSTRAINT `ogroup_usercreate_userobm_id_fkey` FOREIGN KEY (`ogroup_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `ogroup_domain_id_domain_id_fkey` FOREIGN KEY (`ogroup_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogroup_organizationalchart_id_organizationalchart_id_fkey` FOREIGN KEY (`ogroup_organizationalchart_id`) REFERENCES `OrganizationalChart` (`organizationalchart_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogroup_parent_id_ogroup_id_fkey` FOREIGN KEY (`ogroup_parent_id`) REFERENCES `OGroup` (`ogroup_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogroup_userupdate_userobm_id_fkey` FOREIGN KEY (`ogroup_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OGroupLink`
--

DROP TABLE IF EXISTS `OGroupLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OGroupLink` (
  `ogrouplink_id` int(8) NOT NULL AUTO_INCREMENT,
  `ogrouplink_domain_id` int(8) NOT NULL,
  `ogrouplink_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ogrouplink_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ogrouplink_userupdate` int(8) DEFAULT NULL,
  `ogrouplink_usercreate` int(8) DEFAULT NULL,
  `ogrouplink_ogroup_id` int(8) NOT NULL,
  `ogrouplink_entity_id` int(8) NOT NULL,
  PRIMARY KEY (`ogrouplink_id`),
  KEY `ogrouplink_ogroup_id_ogroup_id_fkey` (`ogrouplink_ogroup_id`),
  KEY `ogrouplink_domain_id_domain_id_fkey` (`ogrouplink_domain_id`),
  KEY `ogrouplink_userupdate_userobm_id_fkey` (`ogrouplink_userupdate`),
  KEY `ogrouplink_usercreate_userobm_id_fkey` (`ogrouplink_usercreate`),
  KEY `ogrouplink_entity_id_entity_id_fkey` (`ogrouplink_entity_id`),
  CONSTRAINT `ogrouplink_entity_id_entity_id_fkey` FOREIGN KEY (`ogrouplink_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogrouplink_usercreate_userobm_id_fkey` FOREIGN KEY (`ogrouplink_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `ogrouplink_domain_id_domain_id_fkey` FOREIGN KEY (`ogrouplink_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogrouplink_ogroup_id_ogroup_id_fkey` FOREIGN KEY (`ogrouplink_ogroup_id`) REFERENCES `OGroup` (`ogroup_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogrouplink_userupdate_userobm_id_fkey` FOREIGN KEY (`ogrouplink_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ObmBookmark`
--

DROP TABLE IF EXISTS `ObmBookmark`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ObmBookmark` (
  `obmbookmark_id` int(8) NOT NULL AUTO_INCREMENT,
  `obmbookmark_user_id` int(8) NOT NULL,
  `obmbookmark_label` varchar(48) NOT NULL DEFAULT '',
  `obmbookmark_entity` varchar(24) NOT NULL DEFAULT '',
  PRIMARY KEY (`obmbookmark_id`),
  KEY `bkm_idx_user` (`obmbookmark_user_id`),
  CONSTRAINT `obmbookmark_user_id_userobm_id_fkey` FOREIGN KEY (`obmbookmark_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ObmBookmarkProperty`
--

DROP TABLE IF EXISTS `ObmBookmarkProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ObmBookmarkProperty` (
  `obmbookmarkproperty_id` int(8) NOT NULL AUTO_INCREMENT,
  `obmbookmarkproperty_bookmark_id` int(8) NOT NULL,
  `obmbookmarkproperty_property` varchar(64) NOT NULL DEFAULT '',
  `obmbookmarkproperty_value` varchar(256) NOT NULL DEFAULT '',
  PRIMARY KEY (`obmbookmarkproperty_id`),
  KEY `bkmprop_idx_bkm` (`obmbookmarkproperty_bookmark_id`),
  CONSTRAINT `obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey` FOREIGN KEY (`obmbookmarkproperty_bookmark_id`) REFERENCES `ObmBookmark` (`obmbookmark_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ObmInfo`
--

DROP TABLE IF EXISTS `ObmInfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ObmInfo` (
  `obminfo_name` varchar(32) NOT NULL DEFAULT '',
  `obminfo_value` varchar(255) DEFAULT '',
  PRIMARY KEY (`obminfo_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ObmSession`
--

DROP TABLE IF EXISTS `ObmSession`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ObmSession` (
  `obmsession_sid` varchar(32) NOT NULL DEFAULT '',
  `obmsession_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `obmsession_name` varchar(32) NOT NULL DEFAULT '',
  `obmsession_data` text,
  PRIMARY KEY (`obmsession_sid`,`obmsession_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ObmbookmarkEntity`
--

DROP TABLE IF EXISTS `ObmbookmarkEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ObmbookmarkEntity` (
  `obmbookmarkentity_entity_id` int(8) NOT NULL,
  `obmbookmarkentity_obmbookmark_id` int(8) NOT NULL,
  PRIMARY KEY (`obmbookmarkentity_entity_id`,`obmbookmarkentity_obmbookmark_id`),
  KEY `obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey` (`obmbookmarkentity_obmbookmark_id`),
  CONSTRAINT `obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey` FOREIGN KEY (`obmbookmarkentity_obmbookmark_id`) REFERENCES `ObmBookmark` (`obmbookmark_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `obmbookmarkentity_entity_id_entity_id_fkey` FOREIGN KEY (`obmbookmarkentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OgroupEntity`
--

DROP TABLE IF EXISTS `OgroupEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OgroupEntity` (
  `ogroupentity_entity_id` int(8) NOT NULL,
  `ogroupentity_ogroup_id` int(8) NOT NULL,
  PRIMARY KEY (`ogroupentity_entity_id`,`ogroupentity_ogroup_id`),
  KEY `ogroupentity_ogroup_id_ogroup_id_fkey` (`ogroupentity_ogroup_id`),
  CONSTRAINT `ogroupentity_ogroup_id_ogroup_id_fkey` FOREIGN KEY (`ogroupentity_ogroup_id`) REFERENCES `OGroup` (`ogroup_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ogroupentity_entity_id_entity_id_fkey` FOREIGN KEY (`ogroupentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrganizationalChart`
--

DROP TABLE IF EXISTS `OrganizationalChart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OrganizationalChart` (
  `organizationalchart_id` int(8) NOT NULL AUTO_INCREMENT,
  `organizationalchart_domain_id` int(8) NOT NULL,
  `organizationalchart_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `organizationalchart_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `organizationalchart_userupdate` int(8) DEFAULT NULL,
  `organizationalchart_usercreate` int(8) DEFAULT NULL,
  `organizationalchart_name` varchar(32) NOT NULL,
  `organizationalchart_description` varchar(64) DEFAULT NULL,
  `organizationalchart_archive` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`organizationalchart_id`),
  KEY `organizationalchart_domain_id_domain_id_fkey` (`organizationalchart_domain_id`),
  KEY `organizationalchart_userupdate_userobm_id_fkey` (`organizationalchart_userupdate`),
  KEY `organizationalchart_usercreate_userobm_id_fkey` (`organizationalchart_usercreate`),
  CONSTRAINT `organizationalchart_usercreate_userobm_id_fkey` FOREIGN KEY (`organizationalchart_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `organizationalchart_domain_id_domain_id_fkey` FOREIGN KEY (`organizationalchart_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `organizationalchart_userupdate_userobm_id_fkey` FOREIGN KEY (`organizationalchart_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `OrganizationalchartEntity`
--

DROP TABLE IF EXISTS `OrganizationalchartEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OrganizationalchartEntity` (
  `organizationalchartentity_entity_id` int(8) NOT NULL,
  `organizationalchartentity_organizationalchart_id` int(8) NOT NULL,
  PRIMARY KEY (`organizationalchartentity_entity_id`,`organizationalchartentity_organizationalchart_id`),
  KEY `organizationalchart_id_organizationalchart_id_fkey` (`organizationalchartentity_organizationalchart_id`),
  CONSTRAINT `organizationalchart_id_organizationalchart_id_fkey` FOREIGN KEY (`organizationalchartentity_organizationalchart_id`) REFERENCES `OrganizationalChart` (`organizationalchart_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `organizationalchartentity_entity_id_entity_id_fkey` FOREIGN KEY (`organizationalchartentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_CategoryLink`
--

DROP TABLE IF EXISTS `P_CategoryLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_CategoryLink` (
  `categorylink_category_id` int(8) NOT NULL,
  `categorylink_entity_id` int(8) NOT NULL,
  `categorylink_category` varchar(24) NOT NULL DEFAULT '',
  PRIMARY KEY (`categorylink_category_id`,`categorylink_entity_id`),
  KEY `catl_idx_ent` (`categorylink_entity_id`),
  KEY `catl_idx_cat` (`categorylink_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_Domain`
--

DROP TABLE IF EXISTS `P_Domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_Domain` (
  `domain_id` int(8) NOT NULL AUTO_INCREMENT,
  `domain_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `domain_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `domain_usercreate` int(8) DEFAULT NULL,
  `domain_userupdate` int(8) DEFAULT NULL,
  `domain_label` varchar(32) NOT NULL,
  `domain_description` varchar(255) DEFAULT NULL,
  `domain_name` varchar(128) DEFAULT NULL,
  `domain_alias` text,
  `domain_global` tinyint(1) DEFAULT '0',
  `domain_uuid` char(36) NOT NULL,
  PRIMARY KEY (`domain_id`),
  KEY `domain_userupdate_userobm_id_fkey` (`domain_userupdate`),
  KEY `domain_usercreate_userobm_id_fkey` (`domain_usercreate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_DomainEntity`
--

DROP TABLE IF EXISTS `P_DomainEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_DomainEntity` (
  `domainentity_entity_id` int(8) NOT NULL,
  `domainentity_domain_id` int(8) NOT NULL,
  PRIMARY KEY (`domainentity_entity_id`,`domainentity_domain_id`),
  KEY `domainentity_domain_id_domain_id_fkey` (`domainentity_domain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_EntityRight`
--

DROP TABLE IF EXISTS `P_EntityRight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_EntityRight` (
  `entityright_id` int(8) NOT NULL AUTO_INCREMENT,
  `entityright_entity_id` int(8) NOT NULL,
  `entityright_consumer_id` int(8) DEFAULT NULL,
  `entityright_access` int(1) NOT NULL DEFAULT '0',
  `entityright_read` int(1) NOT NULL DEFAULT '0',
  `entityright_write` int(1) NOT NULL DEFAULT '0',
  `entityright_admin` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`entityright_id`),
  KEY `entityright_entity_id_entity_id` (`entityright_entity_id`),
  KEY `entityright_consumer_id_entity_id` (`entityright_consumer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_GroupEntity`
--

DROP TABLE IF EXISTS `P_GroupEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_GroupEntity` (
  `groupentity_entity_id` int(8) NOT NULL,
  `groupentity_group_id` int(8) NOT NULL,
  PRIMARY KEY (`groupentity_entity_id`,`groupentity_group_id`),
  KEY `groupentity_group_id_group_id_fkey` (`groupentity_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_Host`
--

DROP TABLE IF EXISTS `P_Host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_Host` (
  `host_id` int(8) NOT NULL AUTO_INCREMENT,
  `host_domain_id` int(8) NOT NULL,
  `host_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `host_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `host_userupdate` int(8) DEFAULT NULL,
  `host_usercreate` int(8) DEFAULT NULL,
  `host_uid` int(8) DEFAULT NULL,
  `host_gid` int(8) DEFAULT NULL,
  `host_archive` int(1) NOT NULL DEFAULT '0',
  `host_name` varchar(32) NOT NULL,
  `host_fqdn` varchar(255) DEFAULT NULL,
  `host_ip` varchar(16) DEFAULT NULL,
  `host_delegation` varchar(256) DEFAULT '',
  `host_description` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`host_id`),
  UNIQUE KEY `host_name` (`host_name`),
  KEY `host_domain_id_domain_id_fkey` (`host_domain_id`),
  KEY `host_userupdate_userobm_id_fkey` (`host_userupdate`),
  KEY `host_usercreate_userobm_id_fkey` (`host_usercreate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_HostEntity`
--

DROP TABLE IF EXISTS `P_HostEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_HostEntity` (
  `hostentity_entity_id` int(8) NOT NULL,
  `hostentity_host_id` int(8) NOT NULL,
  PRIMARY KEY (`hostentity_entity_id`,`hostentity_host_id`),
  KEY `hostentity_host_id_host_id_fkey` (`hostentity_host_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_MailShare`
--

DROP TABLE IF EXISTS `P_MailShare`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_MailShare` (
  `mailshare_id` int(8) NOT NULL AUTO_INCREMENT,
  `mailshare_domain_id` int(8) NOT NULL,
  `mailshare_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mailshare_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `mailshare_userupdate` int(8) DEFAULT NULL,
  `mailshare_usercreate` int(8) DEFAULT NULL,
  `mailshare_name` varchar(32) DEFAULT NULL,
  `mailshare_archive` int(1) NOT NULL DEFAULT '0',
  `mailshare_quota` int(11) NOT NULL DEFAULT '0',
  `mailshare_mail_server_id` int(8) DEFAULT NULL,
  `mailshare_delegation` varchar(256) DEFAULT '',
  `mailshare_description` varchar(255) DEFAULT NULL,
  `mailshare_email` text,
  PRIMARY KEY (`mailshare_id`),
  KEY `mailshare_domain_id_domain_id_fkey` (`mailshare_domain_id`),
  KEY `mailshare_mail_server_id_mailserver_id_fkey` (`mailshare_mail_server_id`),
  KEY `mailshare_userupdate_userobm_id_fkey` (`mailshare_userupdate`),
  KEY `mailshare_usercreate_userobm_id_fkey` (`mailshare_usercreate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_MailboxEntity`
--

DROP TABLE IF EXISTS `P_MailboxEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_MailboxEntity` (
  `mailboxentity_entity_id` int(8) NOT NULL,
  `mailboxentity_mailbox_id` int(8) NOT NULL,
  PRIMARY KEY (`mailboxentity_entity_id`,`mailboxentity_mailbox_id`),
  KEY `mailboxentity_mailbox_id_mailbox_id_fkey` (`mailboxentity_mailbox_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_MailshareEntity`
--

DROP TABLE IF EXISTS `P_MailshareEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_MailshareEntity` (
  `mailshareentity_entity_id` int(8) NOT NULL,
  `mailshareentity_mailshare_id` int(8) NOT NULL,
  PRIMARY KEY (`mailshareentity_entity_id`,`mailshareentity_mailshare_id`),
  KEY `mailshareentity_mailshare_id_mailshare_id_fkey` (`mailshareentity_mailshare_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_Service`
--

DROP TABLE IF EXISTS `P_Service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_Service` (
  `service_id` int(8) NOT NULL AUTO_INCREMENT,
  `service_service` varchar(255) NOT NULL,
  `service_entity_id` int(8) NOT NULL,
  PRIMARY KEY (`service_id`),
  KEY `service_service_key` (`service_service`),
  KEY `service_entity_id_entity_id_fkey` (`service_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_ServiceProperty`
--

DROP TABLE IF EXISTS `P_ServiceProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_ServiceProperty` (
  `serviceproperty_id` int(8) NOT NULL AUTO_INCREMENT,
  `serviceproperty_service` varchar(255) NOT NULL,
  `serviceproperty_property` varchar(255) NOT NULL,
  `serviceproperty_entity_id` int(8) NOT NULL,
  `serviceproperty_value` text,
  PRIMARY KEY (`serviceproperty_id`),
  KEY `serviceproperty_service_key` (`serviceproperty_service`),
  KEY `serviceproperty_property_key` (`serviceproperty_property`),
  KEY `serviceproperty_entity_id_entity_id_fkey` (`serviceproperty_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_UGroup`
--

DROP TABLE IF EXISTS `P_UGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_UGroup` (
  `group_id` int(8) NOT NULL AUTO_INCREMENT,
  `group_domain_id` int(8) NOT NULL,
  `group_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `group_userupdate` int(8) DEFAULT NULL,
  `group_usercreate` int(8) DEFAULT NULL,
  `group_system` int(1) DEFAULT '0',
  `group_archive` int(1) NOT NULL DEFAULT '0',
  `group_privacy` int(2) DEFAULT '0',
  `group_local` int(1) DEFAULT '1',
  `group_ext_id` varchar(255) DEFAULT NULL,
  `group_samba` int(1) DEFAULT '0',
  `group_gid` int(8) DEFAULT NULL,
  `group_mailing` int(1) DEFAULT '0',
  `group_delegation` varchar(256) DEFAULT '',
  `group_manager_id` int(8) DEFAULT NULL,
  `group_name` varchar(255) NOT NULL,
  `group_desc` varchar(128) DEFAULT NULL,
  `group_email` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  UNIQUE KEY `group_gid` (`group_gid`,`group_domain_id`),
  KEY `group_domain_id_domain_id_fkey` (`group_domain_id`),
  KEY `group_userupdate_userobm_id_fkey` (`group_userupdate`),
  KEY `group_usercreate_userobm_id_fkey` (`group_usercreate`),
  KEY `group_manager_id_userobm_id_fkey` (`group_manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_UserEntity`
--

DROP TABLE IF EXISTS `P_UserEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_UserEntity` (
  `userentity_entity_id` int(8) NOT NULL,
  `userentity_user_id` int(8) NOT NULL,
  PRIMARY KEY (`userentity_entity_id`,`userentity_user_id`),
  KEY `userentity_user_id_user_id_fkey` (`userentity_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_UserObm`
--

DROP TABLE IF EXISTS `P_UserObm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_UserObm` (
  `userobm_id` int(8) NOT NULL AUTO_INCREMENT,
  `userobm_domain_id` int(8) DEFAULT NULL,
  `userobm_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userobm_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_userupdate` int(8) DEFAULT NULL,
  `userobm_usercreate` int(8) DEFAULT NULL,
  `userobm_local` int(1) DEFAULT '1',
  `userobm_ext_id` varchar(16) DEFAULT NULL,
  `userobm_system` int(1) DEFAULT '0',
  `userobm_archive` int(1) NOT NULL DEFAULT '0',
  `userobm_status` enum('INIT','VALID') DEFAULT 'VALID',
  `userobm_timelastaccess` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_login` varchar(80) NOT NULL DEFAULT '',
  `userobm_nb_login_failed` int(2) DEFAULT '0',
  `userobm_password_type` char(6) NOT NULL DEFAULT 'PLAIN',
  `userobm_password` varchar(64) NOT NULL DEFAULT '',
  `userobm_password_dateexp` date DEFAULT NULL,
  `userobm_account_dateexp` date DEFAULT NULL,
  `userobm_perms` varchar(254) DEFAULT NULL,
  `userobm_delegation_target` varchar(256) DEFAULT '',
  `userobm_delegation` varchar(256) DEFAULT '',
  `userobm_calendar_version` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_uid` int(8) DEFAULT NULL,
  `userobm_gid` int(8) DEFAULT NULL,
  `userobm_datebegin` date DEFAULT NULL,
  `userobm_hidden` int(1) DEFAULT '0',
  `userobm_kind` varchar(64) DEFAULT NULL,
  `userobm_commonname` varchar(256) DEFAULT '',
  `userobm_lastname` varchar(64) DEFAULT '',
  `userobm_firstname` varchar(64) DEFAULT '',
  `userobm_title` varchar(256) DEFAULT '',
  `userobm_sound` varchar(64) DEFAULT NULL,
  `userobm_company` varchar(64) DEFAULT NULL,
  `userobm_direction` varchar(64) DEFAULT NULL,
  `userobm_service` varchar(64) DEFAULT NULL,
  `userobm_address1` varchar(64) DEFAULT NULL,
  `userobm_address2` varchar(64) DEFAULT NULL,
  `userobm_address3` varchar(64) DEFAULT NULL,
  `userobm_zipcode` varchar(14) DEFAULT NULL,
  `userobm_town` varchar(64) DEFAULT NULL,
  `userobm_expresspostal` varchar(16) DEFAULT NULL,
  `userobm_country_iso3166` char(2) DEFAULT '0',
  `userobm_phone` varchar(32) DEFAULT '',
  `userobm_phone2` varchar(32) DEFAULT '',
  `userobm_mobile` varchar(32) DEFAULT '',
  `userobm_fax` varchar(32) DEFAULT '',
  `userobm_fax2` varchar(32) DEFAULT '',
  `userobm_web_perms` int(1) DEFAULT '0',
  `userobm_web_list` text,
  `userobm_web_all` int(1) DEFAULT '0',
  `userobm_mail_perms` int(1) DEFAULT '0',
  `userobm_mail_ext_perms` int(1) DEFAULT '0',
  `userobm_email` text,
  `userobm_mail_server_id` int(8) DEFAULT NULL,
  `userobm_mail_quota` int(8) DEFAULT '0',
  `userobm_mail_quota_use` int(8) DEFAULT '0',
  `userobm_mail_login_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_nomade_perms` int(1) DEFAULT '0',
  `userobm_nomade_enable` int(1) DEFAULT '0',
  `userobm_nomade_local_copy` int(1) DEFAULT '0',
  `userobm_email_nomade` text,
  `userobm_vacation_enable` int(1) DEFAULT '0',
  `userobm_vacation_datebegin` datetime DEFAULT NULL,
  `userobm_vacation_dateend` datetime DEFAULT NULL,
  `userobm_vacation_message` text,
  `userobm_samba_perms` int(1) DEFAULT '0',
  `userobm_samba_home` varchar(255) DEFAULT '',
  `userobm_samba_home_drive` char(2) DEFAULT '',
  `userobm_samba_logon_script` varchar(128) DEFAULT '',
  `userobm_host_id` int(8) DEFAULT NULL,
  `userobm_description` varchar(255) DEFAULT NULL,
  `userobm_location` varchar(255) DEFAULT NULL,
  `userobm_education` varchar(255) DEFAULT NULL,
  `userobm_photo_id` int(8) DEFAULT NULL,
  PRIMARY KEY (`userobm_id`),
  KEY `k_login_user` (`userobm_login`),
  KEY `k_uid_user` (`userobm_uid`),
  KEY `userobm_domain_id_domain_id_fkey` (`userobm_domain_id`),
  KEY `userobm_userupdate_userobm_id_fkey` (`userobm_userupdate`),
  KEY `userobm_usercreate_userobm_id_fkey` (`userobm_usercreate`),
  KEY `userobm_mail_server_id_mailserver_id_fkey` (`userobm_mail_server_id`),
  KEY `userobm_host_id_host_id_fkey` (`userobm_host_id`),
  KEY `userobm_photo_id_document_id_fkey` (`userobm_photo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P__contactgroup`
--

DROP TABLE IF EXISTS `P__contactgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P__contactgroup` (
  `contact_id` int(8) NOT NULL,
  `group_id` int(8) NOT NULL,
  PRIMARY KEY (`contact_id`,`group_id`),
  KEY `_contactgroup_contact_id_contact_id_fkey` (`contact_id`),
  KEY `_contactgroup_group_id_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_field`
--

DROP TABLE IF EXISTS `P_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_field` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `entity_id` int(8) NOT NULL,
  `field` varchar(255) DEFAULT NULL,
  `value` text,
  PRIMARY KEY (`id`),
  KEY `field_entity_id_fkey` (`entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `P_of_usergroup`
--

DROP TABLE IF EXISTS `P_of_usergroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `P_of_usergroup` (
  `of_usergroup_group_id` int(8) NOT NULL,
  `of_usergroup_user_id` int(8) NOT NULL,
  PRIMARY KEY (`of_usergroup_group_id`,`of_usergroup_user_id`),
  KEY `of_usergroup_user_id_userobm_id_fkey` (`of_usergroup_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ParentDeal`
--

DROP TABLE IF EXISTS `ParentDeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ParentDeal` (
  `parentdeal_id` int(8) NOT NULL AUTO_INCREMENT,
  `parentdeal_domain_id` int(8) NOT NULL,
  `parentdeal_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `parentdeal_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `parentdeal_userupdate` int(8) DEFAULT NULL,
  `parentdeal_usercreate` int(8) DEFAULT NULL,
  `parentdeal_label` varchar(128) NOT NULL,
  `parentdeal_marketingmanager_id` int(8) DEFAULT NULL,
  `parentdeal_technicalmanager_id` int(8) DEFAULT NULL,
  `parentdeal_archive` char(1) DEFAULT '0',
  `parentdeal_comment` text,
  PRIMARY KEY (`parentdeal_id`),
  KEY `parentdeal_domain_id_domain_id_fkey` (`parentdeal_domain_id`),
  KEY `parentdeal_userupdate_userobm_id_fkey` (`parentdeal_userupdate`),
  KEY `parentdeal_usercreate_userobm_id_fkey` (`parentdeal_usercreate`),
  KEY `parentdeal_marketingmanager_id_userobm_id_fkey` (`parentdeal_marketingmanager_id`),
  KEY `parentdeal_technicalmanager_id_userobm_id_fkey` (`parentdeal_technicalmanager_id`),
  CONSTRAINT `parentdeal_technicalmanager_id_userobm_id_fkey` FOREIGN KEY (`parentdeal_technicalmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `parentdeal_domain_id_domain_id_fkey` FOREIGN KEY (`parentdeal_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `parentdeal_marketingmanager_id_userobm_id_fkey` FOREIGN KEY (`parentdeal_marketingmanager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `parentdeal_usercreate_userobm_id_fkey` FOREIGN KEY (`parentdeal_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `parentdeal_userupdate_userobm_id_fkey` FOREIGN KEY (`parentdeal_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ParentdealEntity`
--

DROP TABLE IF EXISTS `ParentdealEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ParentdealEntity` (
  `parentdealentity_entity_id` int(8) NOT NULL,
  `parentdealentity_parentdeal_id` int(8) NOT NULL,
  PRIMARY KEY (`parentdealentity_entity_id`,`parentdealentity_parentdeal_id`),
  KEY `parentdealentity_parentdeal_id_parentdeal_id_fkey` (`parentdealentity_parentdeal_id`),
  CONSTRAINT `parentdealentity_parentdeal_id_parentdeal_id_fkey` FOREIGN KEY (`parentdealentity_parentdeal_id`) REFERENCES `ParentDeal` (`parentdeal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `parentdealentity_entity_id_entity_id_fkey` FOREIGN KEY (`parentdealentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Payment`
--

DROP TABLE IF EXISTS `Payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Payment` (
  `payment_id` int(8) NOT NULL AUTO_INCREMENT,
  `payment_domain_id` int(8) NOT NULL,
  `payment_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `payment_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `payment_userupdate` int(8) DEFAULT NULL,
  `payment_usercreate` int(8) DEFAULT NULL,
  `payment_company_id` int(8) DEFAULT NULL,
  `payment_account_id` int(8) DEFAULT NULL,
  `payment_paymentkind_id` int(8) DEFAULT NULL,
  `payment_amount` double(10,2) NOT NULL DEFAULT '0.00',
  `payment_date` date DEFAULT NULL,
  `payment_inout` char(1) NOT NULL DEFAULT '+',
  `payment_number` varchar(24) DEFAULT '',
  `payment_checked` char(1) NOT NULL DEFAULT '0',
  `payment_gap` double(10,2) NOT NULL DEFAULT '0.00',
  `payment_comment` text,
  PRIMARY KEY (`payment_id`),
  KEY `payment_domain_id_domain_id_fkey` (`payment_domain_id`),
  KEY `payment_account_id_account_id_fkey` (`payment_account_id`),
  KEY `payment_userupdate_userobm_id_fkey` (`payment_userupdate`),
  KEY `payment_usercreate_userobm_id_fkey` (`payment_usercreate`),
  KEY `payment_company_id_company_id_fkey` (`payment_company_id`),
  KEY `payment_paymentkind_id_paymentkind_id_fkey` (`payment_paymentkind_id`),
  CONSTRAINT `payment_paymentkind_id_paymentkind_id_fkey` FOREIGN KEY (`payment_paymentkind_id`) REFERENCES `PaymentKind` (`paymentkind_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `payment_account_id_account_id_fkey` FOREIGN KEY (`payment_account_id`) REFERENCES `Account` (`account_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `payment_company_id_company_id_fkey` FOREIGN KEY (`payment_company_id`) REFERENCES `Company` (`company_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `payment_domain_id_domain_id_fkey` FOREIGN KEY (`payment_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `payment_usercreate_userobm_id_fkey` FOREIGN KEY (`payment_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `payment_userupdate_userobm_id_fkey` FOREIGN KEY (`payment_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PaymentEntity`
--

DROP TABLE IF EXISTS `PaymentEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PaymentEntity` (
  `paymententity_entity_id` int(8) NOT NULL,
  `paymententity_payment_id` int(8) NOT NULL,
  PRIMARY KEY (`paymententity_entity_id`,`paymententity_payment_id`),
  KEY `paymententity_payment_id_payment_id_fkey` (`paymententity_payment_id`),
  CONSTRAINT `paymententity_payment_id_payment_id_fkey` FOREIGN KEY (`paymententity_payment_id`) REFERENCES `Payment` (`payment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `paymententity_entity_id_entity_id_fkey` FOREIGN KEY (`paymententity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PaymentInvoice`
--

DROP TABLE IF EXISTS `PaymentInvoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PaymentInvoice` (
  `paymentinvoice_invoice_id` int(8) NOT NULL,
  `paymentinvoice_payment_id` int(8) NOT NULL,
  `paymentinvoice_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `paymentinvoice_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `paymentinvoice_userupdate` int(8) DEFAULT NULL,
  `paymentinvoice_usercreate` int(8) DEFAULT NULL,
  `paymentinvoice_amount` double(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`paymentinvoice_invoice_id`,`paymentinvoice_payment_id`),
  KEY `paymentinvoice_payment_id_payment_id_fkey` (`paymentinvoice_payment_id`),
  KEY `paymentinvoice_usercreate_userobm_id_fkey` (`paymentinvoice_usercreate`),
  KEY `paymentinvoice_userupdate_userobm_id_fkey` (`paymentinvoice_userupdate`),
  CONSTRAINT `paymentinvoice_userupdate_userobm_id_fkey` FOREIGN KEY (`paymentinvoice_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `paymentinvoice_invoice_id_invoice_id_fkey` FOREIGN KEY (`paymentinvoice_invoice_id`) REFERENCES `Invoice` (`invoice_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `paymentinvoice_payment_id_payment_id_fkey` FOREIGN KEY (`paymentinvoice_payment_id`) REFERENCES `Payment` (`payment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `paymentinvoice_usercreate_userobm_id_fkey` FOREIGN KEY (`paymentinvoice_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PaymentKind`
--

DROP TABLE IF EXISTS `PaymentKind`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PaymentKind` (
  `paymentkind_id` int(8) NOT NULL AUTO_INCREMENT,
  `paymentkind_domain_id` int(8) NOT NULL,
  `paymentkind_shortlabel` varchar(3) NOT NULL DEFAULT '',
  `paymentkind_label` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`paymentkind_id`),
  KEY `paymentkind_domain_id_domain_id_fkey` (`paymentkind_domain_id`),
  CONSTRAINT `paymentkind_domain_id_domain_id_fkey` FOREIGN KEY (`paymentkind_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Phone`
--

DROP TABLE IF EXISTS `Phone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Phone` (
  `phone_id` int(8) NOT NULL AUTO_INCREMENT,
  `phone_entity_id` int(8) NOT NULL,
  `phone_label` varchar(255) NOT NULL,
  `phone_number` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`phone_id`),
  KEY `phone_entity_id_entity_id_fkey` (`phone_entity_id`),
  CONSTRAINT `phone_entity_id_entity_id_fkey` FOREIGN KEY (`phone_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PlannedTask`
--

DROP TABLE IF EXISTS `PlannedTask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PlannedTask` (
  `plannedtask_id` int(8) NOT NULL AUTO_INCREMENT,
  `plannedtask_domain_id` int(8) DEFAULT '0',
  `plannedtask_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `plannedtask_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `plannedtask_userupdate` int(8) DEFAULT NULL,
  `plannedtask_usercreate` int(8) DEFAULT NULL,
  `plannedtask_user_id` int(8) DEFAULT NULL,
  `plannedtask_datebegin` date DEFAULT NULL,
  `plannedtask_dateend` date DEFAULT NULL,
  `plannedtask_period` enum('MORNING','AFTERNOON','ALLDAY') NOT NULL DEFAULT 'MORNING',
  `plannedtask_project_id` int(8) DEFAULT NULL,
  `plannedtask_tasktype_id` int(8) DEFAULT NULL,
  `plannedtask_overrun` tinyint(1) DEFAULT '0',
  `plannedtask_comment` text,
  PRIMARY KEY (`plannedtask_id`),
  KEY `plannedtask_domain_id_domain_id_fkey` (`plannedtask_domain_id`),
  KEY `plannedtask_user_id_userobm_id_fkey` (`plannedtask_user_id`),
  KEY `plannedtask_datebegin_key` (`plannedtask_datebegin`),
  KEY `plannedtask_dateend_key` (`plannedtask_dateend`),
  KEY `plannedtask_usercreate_userobm_id_fkey` (`plannedtask_usercreate`),
  KEY `plannedtask_userupdate_userobm_id_fkey` (`plannedtask_userupdate`),
  KEY `plannedtask_project_id_project_id_fkey` (`plannedtask_project_id`),
  KEY `plannedtask_tasktype_id_tasktype_id_fkey` (`plannedtask_tasktype_id`),
  CONSTRAINT `plannedtask_domain_id_domain_id_fkey` FOREIGN KEY (`plannedtask_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `plannedtask_usercreate_userobm_id_fkey` FOREIGN KEY (`plannedtask_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `plannedtask_userupdate_userobm_id_fkey` FOREIGN KEY (`plannedtask_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `plannedtask_project_id_project_id_fkey` FOREIGN KEY (`plannedtask_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `plannedtask_tasktype_id_tasktype_id_fkey` FOREIGN KEY (`plannedtask_tasktype_id`) REFERENCES `TaskType` (`tasktype_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `plannedtask_user_id_userobm_id_fkey` FOREIGN KEY (`plannedtask_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Profile`
--

DROP TABLE IF EXISTS `Profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Profile` (
  `profile_id` int(8) NOT NULL AUTO_INCREMENT,
  `profile_domain_id` int(8) NOT NULL,
  `profile_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `profile_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `profile_userupdate` int(8) DEFAULT NULL,
  `profile_usercreate` int(8) DEFAULT NULL,
  `profile_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`profile_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProfileEntity`
--

DROP TABLE IF EXISTS `ProfileEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProfileEntity` (
  `profileentity_entity_id` int(8) NOT NULL,
  `profileentity_profile_id` int(8) NOT NULL,
  PRIMARY KEY (`profileentity_entity_id`,`profileentity_profile_id`),
  KEY `profileentity_profile_id_profile_id_fkey` (`profileentity_profile_id`),
  CONSTRAINT `profileentity_profile_id_profile_id_fkey` FOREIGN KEY (`profileentity_profile_id`) REFERENCES `Profile` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `profileentity_entity_id_entity_id_fkey` FOREIGN KEY (`profileentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProfileModule`
--

DROP TABLE IF EXISTS `ProfileModule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProfileModule` (
  `profilemodule_id` int(8) NOT NULL AUTO_INCREMENT,
  `profilemodule_domain_id` int(8) NOT NULL,
  `profilemodule_profile_id` int(8) DEFAULT NULL,
  `profilemodule_module_name` varchar(64) NOT NULL DEFAULT '',
  `profilemodule_right` int(2) DEFAULT NULL,
  PRIMARY KEY (`profilemodule_id`),
  KEY `profilemodule_profile_id_profile_id_fkey` (`profilemodule_profile_id`),
  CONSTRAINT `profilemodule_profile_id_profile_id_fkey` FOREIGN KEY (`profilemodule_profile_id`) REFERENCES `Profile` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProfileProperty`
--

DROP TABLE IF EXISTS `ProfileProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProfileProperty` (
  `profileproperty_id` int(8) NOT NULL AUTO_INCREMENT,
  `profileproperty_profile_id` int(8) DEFAULT NULL,
  `profileproperty_name` varchar(32) NOT NULL DEFAULT '',
  `profileproperty_value` text NOT NULL,
  PRIMARY KEY (`profileproperty_id`),
  KEY `profileproperty_profile_id_profile_id_fkey` (`profileproperty_profile_id`),
  CONSTRAINT `profileproperty_profile_id_profile_id_fkey` FOREIGN KEY (`profileproperty_profile_id`) REFERENCES `Profile` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProfileSection`
--

DROP TABLE IF EXISTS `ProfileSection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProfileSection` (
  `profilesection_id` int(8) NOT NULL AUTO_INCREMENT,
  `profilesection_domain_id` int(8) NOT NULL,
  `profilesection_profile_id` int(8) DEFAULT NULL,
  `profilesection_section_name` varchar(64) NOT NULL DEFAULT '',
  `profilesection_show` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`profilesection_id`),
  KEY `profilesection_profile_id_profile_id_fkey` (`profilesection_profile_id`),
  CONSTRAINT `profilesection_profile_id_profile_id_fkey` FOREIGN KEY (`profilesection_profile_id`) REFERENCES `Profile` (`profile_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Project`
--

DROP TABLE IF EXISTS `Project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Project` (
  `project_id` int(8) NOT NULL AUTO_INCREMENT,
  `project_domain_id` int(8) NOT NULL,
  `project_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `project_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `project_userupdate` int(8) DEFAULT NULL,
  `project_usercreate` int(8) DEFAULT NULL,
  `project_name` varchar(128) DEFAULT NULL,
  `project_shortname` varchar(10) DEFAULT NULL,
  `project_type_id` int(8) DEFAULT NULL,
  `project_tasktype_id` int(8) DEFAULT NULL,
  `project_company_id` int(8) DEFAULT NULL,
  `project_deal_id` int(8) DEFAULT NULL,
  `project_soldtime` float DEFAULT NULL,
  `project_estimatedtime` float DEFAULT NULL,
  `project_datebegin` date DEFAULT NULL,
  `project_dateend` date DEFAULT NULL,
  `project_archive` char(1) DEFAULT '0',
  `project_comment` text,
  `project_reference_date` varchar(32) DEFAULT NULL,
  `project_reference_duration` varchar(16) DEFAULT NULL,
  `project_reference_desc` text,
  `project_reference_tech` text,
  PRIMARY KEY (`project_id`),
  KEY `project_idx_comp` (`project_company_id`),
  KEY `project_idx_deal` (`project_deal_id`),
  KEY `project_domain_id_domain_id_fkey` (`project_domain_id`),
  KEY `project_userupdate_userobm_id_fkey` (`project_userupdate`),
  KEY `project_usercreate_userobm_id_fkey` (`project_usercreate`),
  KEY `project_tasktype_id_tasktype_id_fkey` (`project_tasktype_id`),
  KEY `project_type_id_dealtype_id_fkey` (`project_type_id`),
  CONSTRAINT `project_type_id_dealtype_id_fkey` FOREIGN KEY (`project_type_id`) REFERENCES `DealType` (`dealtype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_company_id_company_id_fkey` FOREIGN KEY (`project_company_id`) REFERENCES `Company` (`company_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `project_deal_id_deal_id_fkey` FOREIGN KEY (`project_deal_id`) REFERENCES `Deal` (`deal_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `project_domain_id_domain_id_fkey` FOREIGN KEY (`project_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `project_tasktype_id_tasktype_id_fkey` FOREIGN KEY (`project_tasktype_id`) REFERENCES `TaskType` (`tasktype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_usercreate_userobm_id_fkey` FOREIGN KEY (`project_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_userupdate_userobm_id_fkey` FOREIGN KEY (`project_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectCV`
--

DROP TABLE IF EXISTS `ProjectCV`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectCV` (
  `projectcv_project_id` int(8) NOT NULL,
  `projectcv_cv_id` int(8) NOT NULL,
  `projectcv_role` varchar(128) DEFAULT '',
  PRIMARY KEY (`projectcv_project_id`,`projectcv_cv_id`),
  KEY `projectcv_cv_id_cv_id_fkey` (`projectcv_cv_id`),
  CONSTRAINT `projectcv_cv_id_cv_id_fkey` FOREIGN KEY (`projectcv_cv_id`) REFERENCES `CV` (`cv_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projectcv_project_id_project_id_fkey` FOREIGN KEY (`projectcv_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectClosing`
--

DROP TABLE IF EXISTS `ProjectClosing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectClosing` (
  `projectclosing_id` int(8) NOT NULL AUTO_INCREMENT,
  `projectclosing_project_id` int(8) NOT NULL,
  `projectclosing_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `projectclosing_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `projectclosing_userupdate` int(8) DEFAULT NULL,
  `projectclosing_usercreate` int(8) DEFAULT NULL,
  `projectclosing_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `projectclosing_used` int(8) NOT NULL,
  `projectclosing_remaining` int(8) NOT NULL,
  `projectclosing_type` int(8) DEFAULT NULL,
  `projectclosing_comment` text,
  PRIMARY KEY (`projectclosing_id`),
  KEY `projectclosing_project_id_project_id_fkey` (`projectclosing_project_id`),
  KEY `projectclosing_userupdate_userobm_id_fkey` (`projectclosing_userupdate`),
  KEY `projectclosing_usercreate_userobm_id_fkey` (`projectclosing_usercreate`),
  CONSTRAINT `projectclosing_usercreate_userobm_id_fkey` FOREIGN KEY (`projectclosing_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `projectclosing_project_id_project_id_fkey` FOREIGN KEY (`projectclosing_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projectclosing_userupdate_userobm_id_fkey` FOREIGN KEY (`projectclosing_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectEntity`
--

DROP TABLE IF EXISTS `ProjectEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectEntity` (
  `projectentity_entity_id` int(8) NOT NULL,
  `projectentity_project_id` int(8) NOT NULL,
  PRIMARY KEY (`projectentity_entity_id`,`projectentity_project_id`),
  KEY `projectentity_project_id_project_id_fkey` (`projectentity_project_id`),
  CONSTRAINT `projectentity_project_id_project_id_fkey` FOREIGN KEY (`projectentity_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projectentity_entity_id_entity_id_fkey` FOREIGN KEY (`projectentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectRefTask`
--

DROP TABLE IF EXISTS `ProjectRefTask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectRefTask` (
  `projectreftask_id` int(8) NOT NULL AUTO_INCREMENT,
  `projectreftask_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `projectreftask_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `projectreftask_userupdate` int(8) DEFAULT NULL,
  `projectreftask_usercreate` int(8) DEFAULT NULL,
  `projectreftask_tasktype_id` int(8) DEFAULT NULL,
  `projectreftask_label` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`projectreftask_id`),
  KEY `projectreftask_tasktype_id_tasktype_id_fkey` (`projectreftask_tasktype_id`),
  KEY `projectreftask_userupdate_userobm_id_fkey` (`projectreftask_userupdate`),
  KEY `projectreftask_usercreate_userobm_id_fkey` (`projectreftask_usercreate`),
  CONSTRAINT `projectreftask_usercreate_userobm_id_fkey` FOREIGN KEY (`projectreftask_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `projectreftask_tasktype_id_tasktype_id_fkey` FOREIGN KEY (`projectreftask_tasktype_id`) REFERENCES `TaskType` (`tasktype_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projectreftask_userupdate_userobm_id_fkey` FOREIGN KEY (`projectreftask_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectTask`
--

DROP TABLE IF EXISTS `ProjectTask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectTask` (
  `projecttask_id` int(8) NOT NULL AUTO_INCREMENT,
  `projecttask_project_id` int(8) NOT NULL,
  `projecttask_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `projecttask_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `projecttask_userupdate` int(8) DEFAULT NULL,
  `projecttask_usercreate` int(8) DEFAULT NULL,
  `projecttask_label` varchar(128) DEFAULT NULL,
  `projecttask_parenttask_id` int(8) DEFAULT NULL,
  `projecttask_rank` int(8) DEFAULT NULL,
  `projecttask_datebegin` date DEFAULT NULL,
  `projecttask_dateend` date DEFAULT NULL,
  PRIMARY KEY (`projecttask_id`),
  KEY `pt_idx_pro` (`projecttask_project_id`),
  KEY `projecttask_parenttask_id_projecttask_id_fkey` (`projecttask_parenttask_id`),
  KEY `projecttask_userupdate_userobm_id_fkey` (`projecttask_userupdate`),
  KEY `projecttask_usercreate_userobm_id_fkey` (`projecttask_usercreate`),
  CONSTRAINT `projecttask_usercreate_userobm_id_fkey` FOREIGN KEY (`projecttask_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `projecttask_parenttask_id_projecttask_id_fkey` FOREIGN KEY (`projecttask_parenttask_id`) REFERENCES `ProjectTask` (`projecttask_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projecttask_project_id_project_id_fkey` FOREIGN KEY (`projecttask_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projecttask_userupdate_userobm_id_fkey` FOREIGN KEY (`projecttask_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ProjectUser`
--

DROP TABLE IF EXISTS `ProjectUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ProjectUser` (
  `projectuser_id` int(8) NOT NULL AUTO_INCREMENT,
  `projectuser_project_id` int(8) NOT NULL,
  `projectuser_user_id` int(8) NOT NULL,
  `projectuser_projecttask_id` int(8) DEFAULT NULL,
  `projectuser_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `projectuser_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `projectuser_userupdate` int(8) DEFAULT NULL,
  `projectuser_usercreate` int(8) DEFAULT NULL,
  `projectuser_projectedtime` float DEFAULT NULL,
  `projectuser_missingtime` float DEFAULT NULL,
  `projectuser_validity` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `projectuser_soldprice` int(8) DEFAULT NULL,
  `projectuser_manager` int(1) DEFAULT NULL,
  PRIMARY KEY (`projectuser_id`),
  KEY `pu_idx_pro` (`projectuser_project_id`),
  KEY `pu_idx_user` (`projectuser_user_id`),
  KEY `pu_idx_pt` (`projectuser_projecttask_id`),
  KEY `projectuser_userupdate_userobm_id_fkey` (`projectuser_userupdate`),
  KEY `projectuser_usercreate_userobm_id_fkey` (`projectuser_usercreate`),
  CONSTRAINT `projectuser_usercreate_userobm_id_fkey` FOREIGN KEY (`projectuser_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `projectuser_project_id_project_id_fkey` FOREIGN KEY (`projectuser_project_id`) REFERENCES `Project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `projectuser_userupdate_userobm_id_fkey` FOREIGN KEY (`projectuser_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `projectuser_user_id_userobm_id_fkey` FOREIGN KEY (`projectuser_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Publication`
--

DROP TABLE IF EXISTS `Publication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Publication` (
  `publication_id` int(8) NOT NULL AUTO_INCREMENT,
  `publication_domain_id` int(8) NOT NULL,
  `publication_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `publication_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `publication_userupdate` int(8) DEFAULT NULL,
  `publication_usercreate` int(8) DEFAULT NULL,
  `publication_title` varchar(64) NOT NULL,
  `publication_type_id` int(8) DEFAULT NULL,
  `publication_year` int(4) DEFAULT NULL,
  `publication_lang` varchar(30) DEFAULT NULL,
  `publication_desc` text,
  PRIMARY KEY (`publication_id`),
  KEY `publication_domain_id_domain_id_fkey` (`publication_domain_id`),
  KEY `publication_userupdate_userobm_id_fkey` (`publication_userupdate`),
  KEY `publication_usercreate_userobm_id_fkey` (`publication_usercreate`),
  KEY `publication_type_id_publicationtype_id_fkey` (`publication_type_id`),
  CONSTRAINT `publication_type_id_publicationtype_id_fkey` FOREIGN KEY (`publication_type_id`) REFERENCES `PublicationType` (`publicationtype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `publication_domain_id_domain_id_fkey` FOREIGN KEY (`publication_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `publication_usercreate_userobm_id_fkey` FOREIGN KEY (`publication_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `publication_userupdate_userobm_id_fkey` FOREIGN KEY (`publication_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PublicationEntity`
--

DROP TABLE IF EXISTS `PublicationEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PublicationEntity` (
  `publicationentity_entity_id` int(8) NOT NULL,
  `publicationentity_publication_id` int(8) NOT NULL,
  PRIMARY KEY (`publicationentity_entity_id`,`publicationentity_publication_id`),
  KEY `publicationentity_publication_id_publication_id_fkey` (`publicationentity_publication_id`),
  CONSTRAINT `publicationentity_publication_id_publication_id_fkey` FOREIGN KEY (`publicationentity_publication_id`) REFERENCES `Publication` (`publication_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `publicationentity_entity_id_entity_id_fkey` FOREIGN KEY (`publicationentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PublicationType`
--

DROP TABLE IF EXISTS `PublicationType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PublicationType` (
  `publicationtype_id` int(8) NOT NULL AUTO_INCREMENT,
  `publicationtype_domain_id` int(8) NOT NULL,
  `publicationtype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `publicationtype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `publicationtype_userupdate` int(8) DEFAULT NULL,
  `publicationtype_usercreate` int(8) DEFAULT NULL,
  `publicationtype_code` varchar(10) DEFAULT '',
  `publicationtype_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`publicationtype_id`),
  KEY `publicationtype_domain_id_domain_id_fkey` (`publicationtype_domain_id`),
  KEY `publicationtype_userupdate_userobm_id_fkey` (`publicationtype_userupdate`),
  KEY `publicationtype_usercreate_userobm_id_fkey` (`publicationtype_usercreate`),
  CONSTRAINT `publicationtype_usercreate_userobm_id_fkey` FOREIGN KEY (`publicationtype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `publicationtype_domain_id_domain_id_fkey` FOREIGN KEY (`publicationtype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `publicationtype_userupdate_userobm_id_fkey` FOREIGN KEY (`publicationtype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RGroup`
--

DROP TABLE IF EXISTS `RGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RGroup` (
  `rgroup_id` int(8) NOT NULL AUTO_INCREMENT,
  `rgroup_domain_id` int(8) NOT NULL,
  `rgroup_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `rgroup_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `rgroup_userupdate` int(8) DEFAULT NULL,
  `rgroup_usercreate` int(8) DEFAULT NULL,
  `rgroup_privacy` int(2) DEFAULT '0',
  `rgroup_name` varchar(32) NOT NULL,
  `rgroup_delegation` varchar(256) DEFAULT '',
  `rgroup_desc` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`rgroup_id`),
  KEY `rgroup_domain_id_domain_id_fkey` (`rgroup_domain_id`),
  KEY `rgroup_userupdate_userobm_id_fkey` (`rgroup_userupdate`),
  KEY `rgroup_usercreate_userobm_id_fkey` (`rgroup_usercreate`),
  CONSTRAINT `rgroup_usercreate_userobm_id_fkey` FOREIGN KEY (`rgroup_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `rgroup_domain_id_domain_id_fkey` FOREIGN KEY (`rgroup_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `rgroup_userupdate_userobm_id_fkey` FOREIGN KEY (`rgroup_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Region`
--

DROP TABLE IF EXISTS `Region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Region` (
  `region_id` int(8) NOT NULL AUTO_INCREMENT,
  `region_domain_id` int(8) NOT NULL,
  `region_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `region_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `region_userupdate` int(8) DEFAULT NULL,
  `region_usercreate` int(8) DEFAULT NULL,
  `region_code` varchar(10) DEFAULT '',
  `region_label` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`region_id`),
  KEY `region_domain_id_domain_id_fkey` (`region_domain_id`),
  KEY `region_userupdate_userobm_id_fkey` (`region_userupdate`),
  KEY `region_usercreate_userobm_id_fkey` (`region_usercreate`),
  CONSTRAINT `region_usercreate_userobm_id_fkey` FOREIGN KEY (`region_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `region_domain_id_domain_id_fkey` FOREIGN KEY (`region_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `region_userupdate_userobm_id_fkey` FOREIGN KEY (`region_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Resource`
--

DROP TABLE IF EXISTS `Resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Resource` (
  `resource_id` int(8) NOT NULL AUTO_INCREMENT,
  `resource_domain_id` int(8) NOT NULL,
  `resource_rtype_id` int(8) DEFAULT NULL,
  `resource_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `resource_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `resource_userupdate` int(8) DEFAULT NULL,
  `resource_usercreate` int(8) DEFAULT NULL,
  `resource_name` varchar(32) NOT NULL DEFAULT '',
  `resource_delegation` varchar(256) DEFAULT '',
  `resource_description` varchar(255) DEFAULT NULL,
  `resource_qty` int(8) DEFAULT '0',
  `resource_email` text,
  PRIMARY KEY (`resource_id`),
  UNIQUE KEY `resource_email` (`resource_email`(100)),
  KEY `resource_domain_id_domain_id_fkey` (`resource_domain_id`),
  KEY `resource_userupdate_userobm_id_fkey` (`resource_userupdate`),
  KEY `resource_usercreate_userobm_id_fkey` (`resource_usercreate`),
  KEY `resource_rtype_id_resourcetype_id_fkey` (`resource_rtype_id`),
  CONSTRAINT `resource_domain_id_domain_id_fkey` FOREIGN KEY (`resource_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `resource_rtype_id_resourcetype_id_fkey` FOREIGN KEY (`resource_rtype_id`) REFERENCES `ResourceType` (`resourcetype_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `resource_usercreate_userobm_id_fkey` FOREIGN KEY (`resource_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `resource_userupdate_userobm_id_fkey` FOREIGN KEY (`resource_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ResourceEntity`
--

DROP TABLE IF EXISTS `ResourceEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ResourceEntity` (
  `resourceentity_entity_id` int(8) NOT NULL,
  `resourceentity_resource_id` int(8) NOT NULL,
  PRIMARY KEY (`resourceentity_entity_id`,`resourceentity_resource_id`),
  KEY `resourceentity_resource_id_resource_id_fkey` (`resourceentity_resource_id`),
  CONSTRAINT `resourceentity_resource_id_resource_id_fkey` FOREIGN KEY (`resourceentity_resource_id`) REFERENCES `Resource` (`resource_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `resourceentity_entity_id_entity_id_fkey` FOREIGN KEY (`resourceentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ResourceGroup`
--

DROP TABLE IF EXISTS `ResourceGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ResourceGroup` (
  `resourcegroup_rgroup_id` int(8) NOT NULL,
  `resourcegroup_resource_id` int(8) NOT NULL,
  KEY `resourcegroup_rgroup_id_rgroup_id_fkey` (`resourcegroup_rgroup_id`),
  KEY `resourcegroup_resource_id_resource_id_fkey` (`resourcegroup_resource_id`),
  CONSTRAINT `resourcegroup_resource_id_resource_id_fkey` FOREIGN KEY (`resourcegroup_resource_id`) REFERENCES `Resource` (`resource_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `resourcegroup_rgroup_id_rgroup_id_fkey` FOREIGN KEY (`resourcegroup_rgroup_id`) REFERENCES `RGroup` (`rgroup_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ResourceItem`
--

DROP TABLE IF EXISTS `ResourceItem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ResourceItem` (
  `resourceitem_id` int(8) NOT NULL AUTO_INCREMENT,
  `resourceitem_domain_id` int(8) NOT NULL,
  `resourceitem_label` varchar(32) NOT NULL,
  `resourceitem_resourcetype_id` int(8) NOT NULL,
  `resourceitem_description` text,
  PRIMARY KEY (`resourceitem_id`),
  KEY `resourceitem_domain_id_domain_id_fkey` (`resourceitem_domain_id`),
  KEY `resourceitem_resourcetype_id_resourcetype_id_fkey` (`resourceitem_resourcetype_id`),
  CONSTRAINT `resourceitem_resourcetype_id_resourcetype_id_fkey` FOREIGN KEY (`resourceitem_resourcetype_id`) REFERENCES `ResourceType` (`resourcetype_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `resourceitem_domain_id_domain_id_fkey` FOREIGN KEY (`resourceitem_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ResourceType`
--

DROP TABLE IF EXISTS `ResourceType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ResourceType` (
  `resourcetype_id` int(8) NOT NULL AUTO_INCREMENT,
  `resourcetype_domain_id` int(8) NOT NULL,
  `resourcetype_label` varchar(32) NOT NULL,
  `resourcetype_property` varchar(32) DEFAULT NULL,
  `resourcetype_pkind` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`resourcetype_id`),
  KEY `resourcetype_domain_id_domain_id_fkey` (`resourcetype_domain_id`),
  CONSTRAINT `resourcetype_domain_id_domain_id_fkey` FOREIGN KEY (`resourcetype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ResourcegroupEntity`
--

DROP TABLE IF EXISTS `ResourcegroupEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ResourcegroupEntity` (
  `resourcegroupentity_entity_id` int(8) NOT NULL,
  `resourcegroupentity_resourcegroup_id` int(8) NOT NULL,
  PRIMARY KEY (`resourcegroupentity_entity_id`,`resourcegroupentity_resourcegroup_id`),
  KEY `resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey` (`resourcegroupentity_resourcegroup_id`),
  CONSTRAINT `resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey` FOREIGN KEY (`resourcegroupentity_resourcegroup_id`) REFERENCES `RGroup` (`rgroup_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `resourcegroupentity_entity_id_entity_id_fkey` FOREIGN KEY (`resourcegroupentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SSOTicket`
--

DROP TABLE IF EXISTS `SSOTicket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SSOTicket` (
  `ssoticket_ticket` varchar(255) NOT NULL,
  `ssoticket_user_id` int(8) DEFAULT NULL,
  `ssoticket_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ssoticket_ticket`),
  KEY `ssoticket_user_id_userobm_id_fkey` (`ssoticket_user_id`),
  CONSTRAINT `ssoticket_user_id_userobm_id_fkey` FOREIGN KEY (`ssoticket_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Service`
--

DROP TABLE IF EXISTS `Service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Service` (
  `service_id` int(8) NOT NULL AUTO_INCREMENT,
  `service_service` varchar(255) NOT NULL,
  `service_entity_id` int(8) NOT NULL,
  PRIMARY KEY (`service_id`),
  KEY `service_service_key` (`service_service`),
  KEY `service_entity_id_entity_id_fkey` (`service_entity_id`),
  CONSTRAINT `service_entity_id_entity_id_fkey` FOREIGN KEY (`service_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ServiceProperty`
--

DROP TABLE IF EXISTS `ServiceProperty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ServiceProperty` (
  `serviceproperty_id` int(8) NOT NULL AUTO_INCREMENT,
  `serviceproperty_service` varchar(255) NOT NULL,
  `serviceproperty_property` varchar(255) NOT NULL,
  `serviceproperty_entity_id` int(8) NOT NULL,
  `serviceproperty_value` text,
  PRIMARY KEY (`serviceproperty_id`),
  KEY `serviceproperty_service_key` (`serviceproperty_service`),
  KEY `serviceproperty_property_key` (`serviceproperty_property`),
  KEY `serviceproperty_entity_id_entity_id_fkey` (`serviceproperty_entity_id`),
  CONSTRAINT `serviceproperty_entity_id_entity_id_fkey` FOREIGN KEY (`serviceproperty_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Stats`
--

DROP TABLE IF EXISTS `Stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Stats` (
  `stats_name` varchar(32) NOT NULL DEFAULT '',
  `stats_value` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`stats_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Subscription`
--

DROP TABLE IF EXISTS `Subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Subscription` (
  `subscription_id` int(8) NOT NULL AUTO_INCREMENT,
  `subscription_domain_id` int(8) NOT NULL,
  `subscription_publication_id` int(8) NOT NULL,
  `subscription_contact_id` int(8) NOT NULL,
  `subscription_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `subscription_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `subscription_userupdate` int(8) DEFAULT NULL,
  `subscription_usercreate` int(8) DEFAULT NULL,
  `subscription_quantity` int(8) DEFAULT NULL,
  `subscription_renewal` int(1) NOT NULL DEFAULT '0',
  `subscription_reception_id` int(8) DEFAULT NULL,
  `subscription_date_begin` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `subscription_date_end` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`subscription_id`),
  KEY `subscription_domain_id_domain_id_fkey` (`subscription_domain_id`),
  KEY `subscription_publication_id_publication_id_fkey` (`subscription_publication_id`),
  KEY `subscription_contact_id_contact_id_fkey` (`subscription_contact_id`),
  KEY `subscription_userupdate_userobm_id_fkey` (`subscription_userupdate`),
  KEY `subscription_usercreate_userobm_id_fkey` (`subscription_usercreate`),
  KEY `subscription_reception_id_subscriptionreception_id_fkey` (`subscription_reception_id`),
  CONSTRAINT `subscription_reception_id_subscriptionreception_id_fkey` FOREIGN KEY (`subscription_reception_id`) REFERENCES `SubscriptionReception` (`subscriptionreception_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `subscription_contact_id_contact_id_fkey` FOREIGN KEY (`subscription_contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscription_domain_id_domain_id_fkey` FOREIGN KEY (`subscription_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscription_publication_id_publication_id_fkey` FOREIGN KEY (`subscription_publication_id`) REFERENCES `Publication` (`publication_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscription_usercreate_userobm_id_fkey` FOREIGN KEY (`subscription_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `subscription_userupdate_userobm_id_fkey` FOREIGN KEY (`subscription_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubscriptionEntity`
--

DROP TABLE IF EXISTS `SubscriptionEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubscriptionEntity` (
  `subscriptionentity_entity_id` int(8) NOT NULL,
  `subscriptionentity_subscription_id` int(8) NOT NULL,
  PRIMARY KEY (`subscriptionentity_entity_id`,`subscriptionentity_subscription_id`),
  KEY `subscriptionentity_subscription_id_subscription_id_fkey` (`subscriptionentity_subscription_id`),
  CONSTRAINT `subscriptionentity_subscription_id_subscription_id_fkey` FOREIGN KEY (`subscriptionentity_subscription_id`) REFERENCES `Subscription` (`subscription_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscriptionentity_entity_id_entity_id_fkey` FOREIGN KEY (`subscriptionentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubscriptionReception`
--

DROP TABLE IF EXISTS `SubscriptionReception`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubscriptionReception` (
  `subscriptionreception_id` int(8) NOT NULL AUTO_INCREMENT,
  `subscriptionreception_domain_id` int(8) NOT NULL,
  `subscriptionreception_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `subscriptionreception_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `subscriptionreception_userupdate` int(8) DEFAULT NULL,
  `subscriptionreception_usercreate` int(8) DEFAULT NULL,
  `subscriptionreception_code` varchar(10) DEFAULT '',
  `subscriptionreception_label` char(12) DEFAULT NULL,
  PRIMARY KEY (`subscriptionreception_id`),
  KEY `subscriptionreception_domain_id_domain_id_fkey` (`subscriptionreception_domain_id`),
  KEY `subscriptionreception_userupdate_userobm_id_fkey` (`subscriptionreception_userupdate`),
  KEY `subscriptionreception_usercreate_userobm_id_fkey` (`subscriptionreception_usercreate`),
  CONSTRAINT `subscriptionreception_usercreate_userobm_id_fkey` FOREIGN KEY (`subscriptionreception_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `subscriptionreception_domain_id_domain_id_fkey` FOREIGN KEY (`subscriptionreception_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscriptionreception_userupdate_userobm_id_fkey` FOREIGN KEY (`subscriptionreception_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SyncedAddressbook`
--

DROP TABLE IF EXISTS `SyncedAddressbook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SyncedAddressbook` (
  `user_id` int(8) NOT NULL,
  `addressbook_id` int(8) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`addressbook_id`),
  KEY `syncedaddressbook_user_id_user_id_fkey` (`user_id`),
  KEY `syncedaddressbook_addressbook_id_addressbook_id_fkey` (`addressbook_id`),
  CONSTRAINT `syncedaddressbook_user_id_userobm_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `syncedaddressbook_addressbook_id_addressbook_id_fkey` FOREIGN KEY (`addressbook_id`) REFERENCES `AddressBook` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TaskEvent`
--

DROP TABLE IF EXISTS `TaskEvent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TaskEvent` (
  `taskevent_task_id` int(8) NOT NULL,
  `taskevent_event_id` int(8) NOT NULL,
  PRIMARY KEY (`taskevent_event_id`,`taskevent_task_id`),
  KEY `taskevent_task_id_projecttask_id_fkey` (`taskevent_task_id`),
  CONSTRAINT `taskevent_task_id_projecttask_id_fkey` FOREIGN KEY (`taskevent_task_id`) REFERENCES `ProjectTask` (`projecttask_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `taskevent_event_id_event_id_fkey` FOREIGN KEY (`taskevent_event_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TaskType`
--

DROP TABLE IF EXISTS `TaskType`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TaskType` (
  `tasktype_id` int(8) NOT NULL AUTO_INCREMENT,
  `tasktype_domain_id` int(8) NOT NULL,
  `tasktype_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tasktype_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `tasktype_userupdate` int(8) DEFAULT NULL,
  `tasktype_usercreate` int(8) DEFAULT NULL,
  `tasktype_internal` int(1) NOT NULL,
  `tasktype_tasktypegroup_id` int(8) DEFAULT NULL,
  `tasktype_code` varchar(10) DEFAULT NULL,
  `tasktype_label` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`tasktype_id`),
  KEY `tasktype_domain_id_domain_id_fkey` (`tasktype_domain_id`),
  KEY `tasktype_userupdate_userobm_id_fkey` (`tasktype_userupdate`),
  KEY `tasktype_usercreate_userobm_id_fkey` (`tasktype_usercreate`),
  KEY `tasktype_tasktypegroup_id_tasktypegroup_id_fkey` (`tasktype_tasktypegroup_id`),
  CONSTRAINT `tasktype_usercreate_userobm_id_fkey` FOREIGN KEY (`tasktype_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `tasktype_domain_id_domain_id_fkey` FOREIGN KEY (`tasktype_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `tasktype_userupdate_userobm_id_fkey` FOREIGN KEY (`tasktype_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `tasktype_tasktypegroup_id_tasktypegroup_id_fkey` FOREIGN KEY (`tasktype_tasktypegroup_id`) REFERENCES `TaskTypeGroup` (`tasktypegroup_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TaskTypeGroup`
--

DROP TABLE IF EXISTS `TaskTypeGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TaskTypeGroup` (
  `tasktypegroup_id` int(8) NOT NULL AUTO_INCREMENT,
  `tasktypegroup_domain_id` int(8) DEFAULT '0',
  `tasktypegroup_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tasktypegroup_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `tasktypegroup_userupdate` int(8) DEFAULT NULL,
  `tasktypegroup_usercreate` int(8) DEFAULT NULL,
  `tasktypegroup_label` varchar(32) DEFAULT NULL,
  `tasktypegroup_code` varchar(20) DEFAULT NULL,
  `tasktypegroup_bgcolor` varchar(7) DEFAULT NULL,
  `tasktypegroup_fgcolor` varchar(7) DEFAULT NULL,
  PRIMARY KEY (`tasktypegroup_id`),
  KEY `tasktypegroup_domain_id_domain_id_fkey` (`tasktypegroup_domain_id`),
  KEY `tasktypegroup_usercreate_userobm_id_fkey` (`tasktypegroup_usercreate`),
  KEY `tasktypegroup_userupdate_userobm_id_fkey` (`tasktypegroup_userupdate`),
  CONSTRAINT `tasktypegroup_domain_id_domain_id_fkey` FOREIGN KEY (`tasktypegroup_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `tasktypegroup_usercreate_userobm_id_fkey` FOREIGN KEY (`tasktypegroup_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `tasktypegroup_userupdate_userobm_id_fkey` FOREIGN KEY (`tasktypegroup_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TimeTask`
--

DROP TABLE IF EXISTS `TimeTask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TimeTask` (
  `timetask_id` int(8) NOT NULL AUTO_INCREMENT,
  `timetask_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `timetask_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `timetask_userupdate` int(8) DEFAULT NULL,
  `timetask_usercreate` int(8) DEFAULT NULL,
  `timetask_user_id` int(8) DEFAULT NULL,
  `timetask_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `timetask_projecttask_id` int(8) DEFAULT NULL,
  `timetask_length` float DEFAULT NULL,
  `timetask_tasktype_id` int(8) DEFAULT NULL,
  `timetask_label` varchar(255) DEFAULT NULL,
  `timetask_status` int(1) DEFAULT NULL,
  PRIMARY KEY (`timetask_id`),
  KEY `tt_idx_pt` (`timetask_projecttask_id`),
  KEY `timetask_user_id_userobm_id_fkey` (`timetask_user_id`),
  KEY `timetask_tasktype_id_tasktype_id_fkey` (`timetask_tasktype_id`),
  KEY `timetask_userupdate_userobm_id_fkey` (`timetask_userupdate`),
  KEY `timetask_usercreate_userobm_id_fkey` (`timetask_usercreate`),
  CONSTRAINT `timetask_usercreate_userobm_id_fkey` FOREIGN KEY (`timetask_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `timetask_projecttask_id_projecttask_id_fkey` FOREIGN KEY (`timetask_projecttask_id`) REFERENCES `ProjectTask` (`projecttask_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `timetask_tasktype_id_tasktype_id_fkey` FOREIGN KEY (`timetask_tasktype_id`) REFERENCES `TaskType` (`tasktype_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `timetask_userupdate_userobm_id_fkey` FOREIGN KEY (`timetask_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `timetask_user_id_userobm_id_fkey` FOREIGN KEY (`timetask_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TrustToken`
--

DROP TABLE IF EXISTS `TrustToken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TrustToken` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `token` char(36) NOT NULL,
  `login` varchar(80) NOT NULL,
  `time_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UGroup`
--

DROP TABLE IF EXISTS `UGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UGroup` (
  `group_id` int(8) NOT NULL AUTO_INCREMENT,
  `group_domain_id` int(8) NOT NULL,
  `group_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `group_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `group_userupdate` int(8) DEFAULT NULL,
  `group_usercreate` int(8) DEFAULT NULL,
  `group_system` int(1) DEFAULT '0',
  `group_archive` int(1) NOT NULL DEFAULT '0',
  `group_privacy` int(2) DEFAULT '0',
  `group_local` int(1) DEFAULT '1',
  `group_ext_id` varchar(255) DEFAULT NULL,
  `group_samba` int(1) DEFAULT '0',
  `group_gid` int(8) DEFAULT NULL,
  `group_mailing` int(1) DEFAULT '0',
  `group_delegation` varchar(256) DEFAULT '',
  `group_manager_id` int(8) DEFAULT NULL,
  `group_name` varchar(255) NOT NULL,
  `group_desc` varchar(128) DEFAULT NULL,
  `group_email` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  UNIQUE KEY `group_gid` (`group_gid`,`group_domain_id`),
  KEY `group_domain_id_domain_id_fkey` (`group_domain_id`),
  KEY `group_userupdate_userobm_id_fkey` (`group_userupdate`),
  KEY `group_usercreate_userobm_id_fkey` (`group_usercreate`),
  KEY `group_manager_id_userobm_id_fkey` (`group_manager_id`),
  CONSTRAINT `group_manager_id_userobm_id_fkey` FOREIGN KEY (`group_manager_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `group_domain_id_domain_id_fkey` FOREIGN KEY (`group_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `group_usercreate_userobm_id_fkey` FOREIGN KEY (`group_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `group_userupdate_userobm_id_fkey` FOREIGN KEY (`group_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Updated`
--

DROP TABLE IF EXISTS `Updated`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Updated` (
  `updated_id` int(8) NOT NULL AUTO_INCREMENT,
  `updated_domain_id` int(8) DEFAULT NULL,
  `updated_user_id` int(8) DEFAULT NULL,
  `updated_delegation` varchar(256) DEFAULT '',
  `updated_table` varchar(32) DEFAULT NULL,
  `updated_entity_id` int(8) DEFAULT NULL,
  `updated_type` char(1) DEFAULT NULL,
  PRIMARY KEY (`updated_id`),
  KEY `updated_domain_id_domain_id_fkey` (`updated_domain_id`),
  KEY `updated_user_id_userobm_id_fkey` (`updated_user_id`),
  CONSTRAINT `updated_user_id_userobm_id_fkey` FOREIGN KEY (`updated_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `updated_domain_id_domain_id_fkey` FOREIGN KEY (`updated_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Updatedlinks`
--

DROP TABLE IF EXISTS `Updatedlinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Updatedlinks` (
  `updatedlinks_id` int(8) NOT NULL AUTO_INCREMENT,
  `updatedlinks_domain_id` int(8) DEFAULT NULL,
  `updatedlinks_user_id` int(8) DEFAULT NULL,
  `updatedlinks_delegation` varchar(256) DEFAULT '',
  `updatedlinks_table` varchar(32) DEFAULT NULL,
  `updatedlinks_entity` varchar(32) DEFAULT NULL,
  `updatedlinks_entity_id` int(8) DEFAULT NULL,
  PRIMARY KEY (`updatedlinks_id`),
  KEY `updatedlinks_domain_id_domain_id_fkey` (`updatedlinks_domain_id`),
  KEY `updatedlinks_user_id_userobm_id_fkey` (`updatedlinks_user_id`),
  CONSTRAINT `updatedlinks_user_id_userobm_id_fkey` FOREIGN KEY (`updatedlinks_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `updatedlinks_domain_id_domain_id_fkey` FOREIGN KEY (`updatedlinks_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserEntity`
--

DROP TABLE IF EXISTS `UserEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserEntity` (
  `userentity_entity_id` int(8) NOT NULL,
  `userentity_user_id` int(8) NOT NULL,
  PRIMARY KEY (`userentity_entity_id`,`userentity_user_id`),
  KEY `userentity_user_id_user_id_fkey` (`userentity_user_id`),
  CONSTRAINT `userentity_user_id_user_id_fkey` FOREIGN KEY (`userentity_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userentity_entity_id_entity_id_fkey` FOREIGN KEY (`userentity_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserObm`
--

DROP TABLE IF EXISTS `UserObm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserObm` (
  `userobm_id` int(8) NOT NULL AUTO_INCREMENT,
  `userobm_domain_id` int(8) DEFAULT NULL,
  `userobm_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userobm_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_userupdate` int(8) DEFAULT NULL,
  `userobm_usercreate` int(8) DEFAULT NULL,
  `userobm_local` int(1) DEFAULT '1',
  `userobm_ext_id` varchar(16) DEFAULT NULL,
  `userobm_system` int(1) DEFAULT '0',
  `userobm_archive` int(1) NOT NULL DEFAULT '0',
  `userobm_status` enum('INIT','VALID') DEFAULT 'VALID',
  `userobm_timelastaccess` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_login` varchar(80) NOT NULL DEFAULT '',
  `userobm_nb_login_failed` int(2) DEFAULT '0',
  `userobm_password_type` char(6) NOT NULL DEFAULT 'PLAIN',
  `userobm_password` varchar(64) NOT NULL DEFAULT '',
  `userobm_password_dateexp` date DEFAULT NULL,
  `userobm_account_dateexp` date DEFAULT NULL,
  `userobm_perms` varchar(254) DEFAULT NULL,
  `userobm_delegation_target` varchar(256) DEFAULT '',
  `userobm_delegation` varchar(256) DEFAULT '',
  `userobm_calendar_version` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_uid` int(8) DEFAULT NULL,
  `userobm_gid` int(8) DEFAULT NULL,
  `userobm_datebegin` date DEFAULT NULL,
  `userobm_hidden` int(1) DEFAULT '0',
  `userobm_kind` varchar(64) DEFAULT NULL,
  `userobm_commonname` varchar(256) DEFAULT '',
  `userobm_lastname` varchar(64) DEFAULT '',
  `userobm_firstname` varchar(64) DEFAULT '',
  `userobm_title` varchar(256) DEFAULT '',
  `userobm_sound` varchar(64) DEFAULT NULL,
  `userobm_company` varchar(64) DEFAULT NULL,
  `userobm_direction` varchar(64) DEFAULT NULL,
  `userobm_service` varchar(64) DEFAULT NULL,
  `userobm_address1` varchar(64) DEFAULT NULL,
  `userobm_address2` varchar(64) DEFAULT NULL,
  `userobm_address3` varchar(64) DEFAULT NULL,
  `userobm_zipcode` varchar(14) DEFAULT NULL,
  `userobm_town` varchar(64) DEFAULT NULL,
  `userobm_expresspostal` varchar(16) DEFAULT NULL,
  `userobm_country_iso3166` char(2) DEFAULT '0',
  `userobm_phone` varchar(32) DEFAULT '',
  `userobm_phone2` varchar(32) DEFAULT '',
  `userobm_mobile` varchar(32) DEFAULT '',
  `userobm_fax` varchar(32) DEFAULT '',
  `userobm_fax2` varchar(32) DEFAULT '',
  `userobm_web_perms` int(1) DEFAULT '0',
  `userobm_web_list` text,
  `userobm_web_all` int(1) DEFAULT '0',
  `userobm_mail_perms` int(1) DEFAULT '0',
  `userobm_mail_ext_perms` int(1) DEFAULT '0',
  `userobm_email` text,
  `userobm_mail_server_id` int(8) DEFAULT NULL,
  `userobm_mail_quota` int(8) DEFAULT '0',
  `userobm_mail_quota_use` int(8) DEFAULT '0',
  `userobm_mail_login_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_nomade_perms` int(1) DEFAULT '0',
  `userobm_nomade_enable` int(1) DEFAULT '0',
  `userobm_nomade_local_copy` int(1) DEFAULT '0',
  `userobm_email_nomade` text,
  `userobm_vacation_enable` int(1) DEFAULT '0',
  `userobm_vacation_datebegin` datetime DEFAULT NULL,
  `userobm_vacation_dateend` datetime DEFAULT NULL,
  `userobm_vacation_message` text,
  `userobm_samba_perms` int(1) DEFAULT '0',
  `userobm_samba_home` varchar(255) DEFAULT '',
  `userobm_samba_home_drive` char(2) DEFAULT '',
  `userobm_samba_logon_script` varchar(128) DEFAULT '',
  `userobm_host_id` int(8) DEFAULT NULL,
  `userobm_description` varchar(255) DEFAULT NULL,
  `userobm_location` varchar(255) DEFAULT NULL,
  `userobm_education` varchar(255) DEFAULT NULL,
  `userobm_photo_id` int(8) DEFAULT NULL,
  PRIMARY KEY (`userobm_id`),
  KEY `k_login_user` (`userobm_login`),
  KEY `k_uid_user` (`userobm_uid`),
  KEY `userobm_domain_id_domain_id_fkey` (`userobm_domain_id`),
  KEY `userobm_userupdate_userobm_id_fkey` (`userobm_userupdate`),
  KEY `userobm_usercreate_userobm_id_fkey` (`userobm_usercreate`),
  KEY `userobm_mail_server_id_mailserver_id_fkey` (`userobm_mail_server_id`),
  KEY `userobm_host_id_host_id_fkey` (`userobm_host_id`),
  KEY `userobm_photo_id_document_id_fkey` (`userobm_photo_id`),
  CONSTRAINT `userobm_photo_id_document_id_fkey` FOREIGN KEY (`userobm_photo_id`) REFERENCES `Document` (`document_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `userobm_domain_id_domain_id_fkey` FOREIGN KEY (`userobm_domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userobm_host_id_host_id_fkey` FOREIGN KEY (`userobm_host_id`) REFERENCES `Host` (`host_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `userobm_mail_server_id_mailserver_id_fkey` FOREIGN KEY (`userobm_mail_server_id`) REFERENCES `Host` (`host_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `userobm_usercreate_userobm_id_fkey` FOREIGN KEY (`userobm_usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `userobm_userupdate_userobm_id_fkey` FOREIGN KEY (`userobm_userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserObmGroup`
--

DROP TABLE IF EXISTS `UserObmGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserObmGroup` (
  `userobmgroup_group_id` int(8) NOT NULL,
  `userobmgroup_userobm_id` int(8) NOT NULL,
  PRIMARY KEY (`userobmgroup_group_id`,`userobmgroup_userobm_id`),
  KEY `userobmgroup_userobm_id_userobm_id_fkey` (`userobmgroup_userobm_id`),
  CONSTRAINT `userobmgroup_userobm_id_userobm_id_fkey` FOREIGN KEY (`userobmgroup_userobm_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userobmgroup_group_id_group_id_fkey` FOREIGN KEY (`userobmgroup_group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserObmPref`
--

DROP TABLE IF EXISTS `UserObmPref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserObmPref` (
  `userobmpref_id` int(8) NOT NULL AUTO_INCREMENT,
  `userobmpref_user_id` int(8) DEFAULT NULL,
  `userobmpref_option` varchar(50) NOT NULL,
  `userobmpref_value` varchar(50) NOT NULL,
  PRIMARY KEY (`userobmpref_id`),
  UNIQUE KEY `userobmpref_key` (`userobmpref_user_id`,`userobmpref_option`),
  KEY `userobmpref_user_id_userobm_id_fkey` (`userobmpref_user_id`),
  CONSTRAINT `userobmpref_user_id_userobm_id_fkey` FOREIGN KEY (`userobmpref_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserObm_SessionLog`
--

DROP TABLE IF EXISTS `UserObm_SessionLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserObm_SessionLog` (
  `userobm_sessionlog_sid` varchar(32) NOT NULL DEFAULT '',
  `userobm_sessionlog_session_name` varchar(32) NOT NULL DEFAULT '',
  `userobm_sessionlog_userobm_id` int(11) DEFAULT NULL,
  `userobm_sessionlog_timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userobm_sessionlog_timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userobm_sessionlog_nb_connexions` int(11) NOT NULL DEFAULT '0',
  `userobm_sessionlog_lastpage` varchar(32) NOT NULL DEFAULT '0',
  `userobm_sessionlog_ip` varchar(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (`userobm_sessionlog_sid`),
  KEY `userobm_sessionlog_userobm_id_userobm_id_fkey` (`userobm_sessionlog_userobm_id`),
  CONSTRAINT `userobm_sessionlog_userobm_id_userobm_id_fkey` FOREIGN KEY (`userobm_sessionlog_userobm_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserSystem`
--

DROP TABLE IF EXISTS `UserSystem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserSystem` (
  `usersystem_id` int(8) NOT NULL AUTO_INCREMENT,
  `usersystem_login` varchar(32) NOT NULL DEFAULT '',
  `usersystem_password` varchar(32) NOT NULL DEFAULT '',
  `usersystem_uid` varchar(6) DEFAULT NULL,
  `usersystem_gid` varchar(6) DEFAULT NULL,
  `usersystem_homedir` varchar(32) NOT NULL DEFAULT '/tmp',
  `usersystem_lastname` varchar(32) DEFAULT NULL,
  `usersystem_firstname` varchar(32) DEFAULT NULL,
  `usersystem_shell` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`usersystem_id`),
  UNIQUE KEY `k_login_user` (`usersystem_login`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Website`
--

DROP TABLE IF EXISTS `Website`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Website` (
  `website_id` int(8) NOT NULL AUTO_INCREMENT,
  `website_entity_id` int(8) NOT NULL,
  `website_label` varchar(255) NOT NULL,
  `website_url` text,
  PRIMARY KEY (`website_id`),
  KEY `website_entity_id_entity_id_fkey` (`website_entity_id`),
  CONSTRAINT `website_entity_id_entity_id_fkey` FOREIGN KEY (`website_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `_contactgroup`
--

DROP TABLE IF EXISTS `_contactgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `_contactgroup` (
  `contact_id` int(8) NOT NULL,
  `group_id` int(8) NOT NULL,
  PRIMARY KEY (`contact_id`,`group_id`),
  KEY `_contactgroup_contact_id_contact_id_fkey` (`contact_id`),
  KEY `_contactgroup_group_id_group_id` (`group_id`),
  CONSTRAINT `_contactgroup_contact_id_contact_id_fkey` FOREIGN KEY (`contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `_contactgroup_group_id_group_id_fkey` FOREIGN KEY (`group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `_userpattern`
--

DROP TABLE IF EXISTS `_userpattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `_userpattern` (
  `id` int(11) NOT NULL,
  `pattern` varchar(255) DEFAULT NULL,
  KEY `pattern` (`pattern`),
  KEY `_userpattern_id_fkey` (`id`),
  CONSTRAINT `_userpattern_id_fkey` FOREIGN KEY (`id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `calendarcolor`
--

DROP TABLE IF EXISTS `calendarcolor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `calendarcolor` (
  `user_id` int(11) NOT NULL,
  `entity_id` int(11) NOT NULL,
  `eventowner` int(11) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`entity_id`),
  KEY `user_id_fkey` (`user_id`),
  KEY `entity_id_fkey` (`entity_id`),
  CONSTRAINT `user_id_userobm_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `entity_id_entity_id_fkey` FOREIGN KEY (`entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contactgroup`
--

DROP TABLE IF EXISTS `contactgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contactgroup` (
  `contact_id` int(8) NOT NULL,
  `group_id` int(8) NOT NULL,
  PRIMARY KEY (`contact_id`,`group_id`),
  KEY `contactgroup_contact_id_contact_id_fkey` (`contact_id`),
  KEY `contactgroup_group_id_group_id_fkey` (`group_id`),
  CONSTRAINT `contactgroup_contact_id_contact_id_fkey` FOREIGN KEY (`contact_id`) REFERENCES `Contact` (`contact_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `contactgroup_group_id_group_id_fkey` FOREIGN KEY (`group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `field`
--

DROP TABLE IF EXISTS `field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `field` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `entity_id` int(8) NOT NULL,
  `field` varchar(255) DEFAULT NULL,
  `value` text,
  PRIMARY KEY (`id`),
  KEY `field_entity_id_fkey` (`entity_id`),
  CONSTRAINT `field_entity_id_fkey` FOREIGN KEY (`entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `of_usergroup`
--

DROP TABLE IF EXISTS `of_usergroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `of_usergroup` (
  `of_usergroup_group_id` int(8) NOT NULL,
  `of_usergroup_user_id` int(8) NOT NULL,
  PRIMARY KEY (`of_usergroup_group_id`,`of_usergroup_user_id`),
  KEY `of_usergroup_user_id_userobm_id_fkey` (`of_usergroup_user_id`),
  CONSTRAINT `of_usergroup_user_id_userobm_id_fkey` FOREIGN KEY (`of_usergroup_user_id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `of_usergroup_group_id_group_id_fkey` FOREIGN KEY (`of_usergroup_group_id`) REFERENCES `UGroup` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_device`
--

DROP TABLE IF EXISTS `opush_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_device` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(255) NOT NULL,
  `owner` int(11) DEFAULT NULL,
  `type` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `opush_device_owner_userobm_id_fkey` (`owner`),
  CONSTRAINT `opush_device_owner_userobm_id_fkey` FOREIGN KEY (`owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_event_mapping`
--

DROP TABLE IF EXISTS `opush_event_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_event_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `device_id` int(11) NOT NULL,
  `event_uid` varchar(300) NOT NULL,
  `event_ext_id` varchar(300) NOT NULL,
  `event_ext_id_hash` binary(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `opush_event_mapping_device_id_opush_device_id_fkey` (`device_id`),
  UNIQUE KEY `opush_event_mapping_device_id_event_ext_id_fkey` (`device_id`,`event_ext_id_hash`),
  CONSTRAINT `opush_event_mapping_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_folder_mapping`
--

DROP TABLE IF EXISTS `opush_folder_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_folder_mapping` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `device_id` int(11) NOT NULL,
  `collection` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `opush_folder_mapping_device_id_opush_device_id_fkey` (`device_id`),
  CONSTRAINT `opush_folder_mapping_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_folder_snapshot`
--

DROP TABLE IF EXISTS `opush_folder_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_folder_snapshot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `folder_sync_state_id` int(11) NOT NULL,
  `collection_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ofs_folder_sync_state_id_ofssid_fkey` (`folder_sync_state_id`),
  KEY `ofs_collection_id_ofmid_fkey` (`collection_id`),
  CONSTRAINT `ofs_folder_sync_state_id_ofssid_fkey` FOREIGN KEY (`folder_sync_state_id`) REFERENCES `opush_folder_sync_state` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ofs_collection_id_ofmid_fkey` FOREIGN KEY (`collection_id`) REFERENCES `opush_folder_mapping` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_folder_sync_state`
--

DROP TABLE IF EXISTS `opush_folder_sync_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_folder_sync_state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sync_key` varchar(64) NOT NULL,
  `device_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sync_key` (`sync_key`),
  KEY `opush_folder_sync_state_device_id_opush_device_id_fkey` (`device_id`),
  CONSTRAINT `opush_folder_sync_state_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_folder_sync_state_backend_mapping`
--

DROP TABLE IF EXISTS `opush_folder_sync_state_backend_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_folder_sync_state_backend_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data_type` enum('EMAIL','CALENDAR','CONTACTS','TASKS') NOT NULL,
  `folder_sync_state_id` int(11) NOT NULL,
  `last_sync` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `opush_folder_sync_state_backend_mapping_fkey` (`folder_sync_state_id`),
  CONSTRAINT `opush_folder_sync_state_backend_mapping_fkey` FOREIGN KEY (`folder_sync_state_id`) REFERENCES `opush_folder_sync_state` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_ping_heartbeat`
--

DROP TABLE IF EXISTS `opush_ping_heartbeat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_ping_heartbeat` (
  `device_id` int(11) NOT NULL,
  `last_heartbeat` int(11) NOT NULL,
  UNIQUE KEY `unique_opush_col_dev` (`device_id`),
  KEY `opush_ping_heartbeat_devive_id_opush_device_id_fkey` (`device_id`),
  CONSTRAINT `opush_ping_heartbeat_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_sec_policy`
--

DROP TABLE IF EXISTS `opush_sec_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_sec_policy` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `device_password_enabled` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_sync_perms`
--

DROP TABLE IF EXISTS `opush_sync_perms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_sync_perms` (
  `owner` int(11) DEFAULT NULL,
  `device_id` int(11) DEFAULT NULL,
  `policy` int(11) DEFAULT NULL,
  `pending_accept` tinyint(1) NOT NULL,
  KEY `opush_sync_perms_owner_userobm_id_fkey` (`owner`),
  KEY `opush_sync_perms_device_id_opush_device_id_fkey` (`device_id`),
  KEY `opush_sync_perms_policy_opush_sec_policy_id_fkey` (`policy`),
  CONSTRAINT `opush_sync_perms_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE,
  CONSTRAINT `opush_sync_perms_owner_userobm_id_fkey` FOREIGN KEY (`owner`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE,
  CONSTRAINT `opush_sync_perms_policy_opush_sec_policy_id_fkey` FOREIGN KEY (`policy`) REFERENCES `opush_sec_policy` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_sync_state`
--

DROP TABLE IF EXISTS `opush_sync_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_sync_state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sync_key` varchar(64) NOT NULL,
  `collection_id` int(11) NOT NULL,
  `device_id` int(11) NOT NULL,
  `last_sync` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sync_key` (`sync_key`),
  KEY `opush_sync_state_collection_id_opush_folder_mapping_id_fkey` (`collection_id`),
  KEY `opush_sync_state_device_id_opush_device_id_fkey` (`device_id`),
  CONSTRAINT `opush_sync_state_collection_id_opush_folder_mapping_id_fkey` FOREIGN KEY (`collection_id`) REFERENCES `opush_folder_mapping` (`id`) ON DELETE CASCADE,
  CONSTRAINT `opush_sync_state_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opush_synced_item`
--

DROP TABLE IF EXISTS `opush_synced_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opush_synced_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sync_state_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `addition` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `opush_synced_item_sync_state_id_opush_sync_state_id_fkey` (`sync_state_id`),
  CONSTRAINT `opush_synced_item_sync_state_id_opush_sync_state_id_fkey` FOREIGN KEY (`sync_state_id`) REFERENCES `opush_sync_state` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `token`
--

DROP TABLE IF EXISTS `token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `token` (
  `token` varchar(300) NOT NULL,
  `property` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userpattern`
--

DROP TABLE IF EXISTS `userpattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userpattern` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `domain_id` int(8) NOT NULL,
  `timeupdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `timecreate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `userupdate` int(8) DEFAULT NULL,
  `usercreate` int(8) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  KEY `userpattern_domain_id_domain_id_fkey` (`domain_id`),
  KEY `userpattern_userupdate_userobm_id_fkey` (`userupdate`),
  KEY `userpattern_usercreate_userobm_id_fkey` (`usercreate`),
  CONSTRAINT `userpattern_domain_id_domain_id_fkey` FOREIGN KEY (`domain_id`) REFERENCES `Domain` (`domain_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `userpattern_userupdate_userobm_id_fkey` FOREIGN KEY (`userupdate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `userpattern_usercreate_userobm_id_fkey` FOREIGN KEY (`usercreate`) REFERENCES `UserObm` (`userobm_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userpattern_property`
--

DROP TABLE IF EXISTS `userpattern_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userpattern_property` (
  `userpattern_id` int(8) NOT NULL,
  `attribute` varchar(255) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`userpattern_id`,`attribute`),
  KEY `userpattern_property_userpattern_id_userpattern_id_fkey` (`userpattern_id`),
  CONSTRAINT `userpattern_property_userpattern_id_userpattern_id_fkey` FOREIGN KEY (`userpattern_id`) REFERENCES `userpattern` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-04-25 16:03:30

INSERT INTO ObmInfo SELECT 'product_id', LPAD(MD5(FLOOR(RAND()*NOW())), 24, 0);
