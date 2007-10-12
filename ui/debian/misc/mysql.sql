-- MySQL dump 10.11
--
-- Host: localhost    Database: obm
-- ------------------------------------------------------
-- Server version	5.0.32-Debian_7etch1-log

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
CREATE TABLE `Account` (
  `account_id` int(8) NOT NULL auto_increment,
  `account_domain_id` int(8) default '0',
  `account_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `account_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `account_userupdate` int(8) default NULL,
  `account_usercreate` int(8) default NULL,
  `account_bank` varchar(60) NOT NULL default '',
  `account_number` varchar(11) NOT NULL default '0',
  `account_balance` double(15,2) NOT NULL default '0.00',
  `account_today` double(15,2) NOT NULL default '0.00',
  `account_comment` varchar(100) default NULL,
  `account_label` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`account_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Account`
--

LOCK TABLES `Account` WRITE;
/*!40000 ALTER TABLE `Account` DISABLE KEYS */;
/*!40000 ALTER TABLE `Account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ActiveUserObm`
--

DROP TABLE IF EXISTS `ActiveUserObm`;
CREATE TABLE `ActiveUserObm` (
  `activeuserobm_sid` varchar(32) NOT NULL default '',
  `activeuserobm_session_name` varchar(32) NOT NULL default '',
  `activeuserobm_userobm_id` int(11) default NULL,
  `activeuserobm_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `activeuserobm_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `activeuserobm_nb_connexions` int(11) NOT NULL default '0',
  `activeuserobm_lastpage` varchar(64) NOT NULL default '0',
  `activeuserobm_ip` varchar(32) NOT NULL default '0',
  PRIMARY KEY  (`activeuserobm_sid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ActiveUserObm`
--

LOCK TABLES `ActiveUserObm` WRITE;
/*!40000 ALTER TABLE `ActiveUserObm` DISABLE KEYS */;
/*!40000 ALTER TABLE `ActiveUserObm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CV`
--

DROP TABLE IF EXISTS `CV`;
CREATE TABLE `CV` (
  `cv_id` int(8) NOT NULL auto_increment,
  `cv_domain_id` int(8) default '0',
  `cv_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `cv_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `cv_userupdate` int(8) default NULL,
  `cv_usercreate` int(8) default NULL,
  `cv_userobm_id` int(8) NOT NULL,
  `cv_title` varchar(255) default NULL,
  `cv_additionnalrefs` text,
  `cv_comment` text,
  PRIMARY KEY  (`cv_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CV`
--

LOCK TABLES `CV` WRITE;
/*!40000 ALTER TABLE `CV` DISABLE KEYS */;
/*!40000 ALTER TABLE `CV` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CalendarAlert`
--

DROP TABLE IF EXISTS `CalendarAlert`;
CREATE TABLE `CalendarAlert` (
  `calendaralert_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `calendaralert_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `calendaralert_userupdate` int(8) default NULL,
  `calendaralert_usercreate` int(8) default NULL,
  `calendaralert_event_id` int(8) default NULL,
  `calendaralert_user_id` int(8) default NULL,
  `calendaralert_duration` int(8) NOT NULL default '0',
  KEY `idx_calendaralert_user` (`calendaralert_user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CalendarAlert`
--

LOCK TABLES `CalendarAlert` WRITE;
/*!40000 ALTER TABLE `CalendarAlert` DISABLE KEYS */;
/*!40000 ALTER TABLE `CalendarAlert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CalendarCategory1`
--

DROP TABLE IF EXISTS `CalendarCategory1`;
CREATE TABLE `CalendarCategory1` (
  `calendarcategory1_id` int(8) NOT NULL auto_increment,
  `calendarcategory1_domain_id` int(8) default '0',
  `calendarcategory1_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `calendarcategory1_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `calendarcategory1_userupdate` int(8) default NULL,
  `calendarcategory1_usercreate` int(8) default NULL,
  `calendarcategory1_code` varchar(10) default '',
  `calendarcategory1_label` varchar(128) default NULL,
  `calendarcategory1_color` char(6) default NULL,
  PRIMARY KEY  (`calendarcategory1_id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CalendarCategory1`
--

LOCK TABLES `CalendarCategory1` WRITE;
/*!40000 ALTER TABLE `CalendarCategory1` DISABLE KEYS */;
INSERT INTO `CalendarCategory1` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','RDV',NULL),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Formation',NULL),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Commercial',NULL),(4,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Reunion',NULL),(5,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Appel tel.',NULL),(6,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Support',NULL),(7,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Intervention',NULL),(8,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'','Personnel',NULL);
/*!40000 ALTER TABLE `CalendarCategory1` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CalendarEvent`
--

DROP TABLE IF EXISTS `CalendarEvent`;
CREATE TABLE `CalendarEvent` (
  `calendarevent_id` int(8) NOT NULL auto_increment,
  `calendarevent_domain_id` int(8) default '0',
  `calendarevent_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `calendarevent_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `calendarevent_userupdate` int(8) default NULL,
  `calendarevent_usercreate` int(8) default NULL,
  `calendarevent_owner` int(8) default NULL,
  `calendarevent_ext_id` varchar(32) default '',
  `calendarevent_title` varchar(255) default NULL,
  `calendarevent_location` varchar(100) default NULL,
  `calendarevent_category1_id` int(8) default '0',
  `calendarevent_priority` int(2) default NULL,
  `calendarevent_privacy` int(2) NOT NULL default '0',
  `calendarevent_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `calendarevent_duration` int(8) NOT NULL default '0',
  `calendarevent_allday` int(1) NOT NULL default '0',
  `calendarevent_repeatkind` varchar(20) default NULL,
  `calendarevent_repeatfrequence` int(3) default NULL,
  `calendarevent_repeatdays` varchar(7) default NULL,
  `calendarevent_endrepeat` timestamp NOT NULL default '0000-00-00 00:00:00',
  `calendarevent_color` varchar(7) default NULL,
  `calendarevent_description` text,
  `calendarevent_properties` text,
  PRIMARY KEY  (`calendarevent_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CalendarEvent`
--

LOCK TABLES `CalendarEvent` WRITE;
/*!40000 ALTER TABLE `CalendarEvent` DISABLE KEYS */;
/*!40000 ALTER TABLE `CalendarEvent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CalendarException`
--

DROP TABLE IF EXISTS `CalendarException`;
CREATE TABLE `CalendarException` (
  `calendarexception_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `calendarexception_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `calendarexception_userupdate` int(8) default NULL,
  `calendarexception_usercreate` int(8) default NULL,
  `calendarexception_event_id` int(8) NOT NULL default '0',
  `calendarexception_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`calendarexception_event_id`,`calendarexception_date`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CalendarException`
--

LOCK TABLES `CalendarException` WRITE;
/*!40000 ALTER TABLE `CalendarException` DISABLE KEYS */;
/*!40000 ALTER TABLE `CalendarException` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Category`
--

DROP TABLE IF EXISTS `Category`;
CREATE TABLE `Category` (
  `category_id` int(8) NOT NULL auto_increment,
  `category_domain_id` int(8) NOT NULL default '0',
  `category_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `category_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `category_userupdate` int(8) NOT NULL default '0',
  `category_usercreate` int(8) NOT NULL default '0',
  `category_category` varchar(24) NOT NULL default '',
  `category_code` varchar(10) NOT NULL default '',
  `category_label` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`category_id`),
  KEY `cat_idx_cat` (`category_category`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Category`
--

LOCK TABLES `Category` WRITE;
/*!40000 ALTER TABLE `Category` DISABLE KEYS */;
/*!40000 ALTER TABLE `Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CategoryLink`
--

DROP TABLE IF EXISTS `CategoryLink`;
CREATE TABLE `CategoryLink` (
  `categorylink_category_id` int(8) NOT NULL default '0',
  `categorylink_entity_id` int(8) NOT NULL default '0',
  `categorylink_category` varchar(24) NOT NULL default '',
  `categorylink_entity` varchar(32) NOT NULL default '',
  PRIMARY KEY  (`categorylink_category_id`,`categorylink_entity_id`),
  KEY `catl_idx_ent` (`categorylink_entity_id`),
  KEY `catl_idx_cat` (`categorylink_category`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CategoryLink`
--

LOCK TABLES `CategoryLink` WRITE;
/*!40000 ALTER TABLE `CategoryLink` DISABLE KEYS */;
/*!40000 ALTER TABLE `CategoryLink` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Company`
--

DROP TABLE IF EXISTS `Company`;
CREATE TABLE `Company` (
  `company_id` int(8) NOT NULL auto_increment,
  `company_domain_id` int(8) default '0',
  `company_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `company_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `company_userupdate` int(8) default NULL,
  `company_usercreate` int(8) default NULL,
  `company_datasource_id` int(8) default '0',
  `company_number` varchar(32) default NULL,
  `company_vat` varchar(20) default NULL,
  `company_siret` varchar(14) default NULL,
  `company_archive` char(1) NOT NULL default '0',
  `company_name` varchar(96) NOT NULL default '',
  `company_aka` varchar(255) default NULL,
  `company_sound` varchar(48) default NULL,
  `company_type_id` int(8) default NULL,
  `company_activity_id` int(8) default NULL,
  `company_nafcode_id` int(8) default NULL,
  `company_marketingmanager_id` int(8) default NULL,
  `company_address1` varchar(64) default NULL,
  `company_address2` varchar(64) default NULL,
  `company_address3` varchar(64) default NULL,
  `company_zipcode` varchar(14) default NULL,
  `company_town` varchar(64) default NULL,
  `company_expresspostal` varchar(16) default NULL,
  `company_country_iso3166` char(2) default '0',
  `company_phone` varchar(32) default NULL,
  `company_fax` varchar(32) default NULL,
  `company_web` varchar(64) default NULL,
  `company_email` varchar(64) default NULL,
  `company_contact_number` int(5) NOT NULL default '0',
  `company_deal_number` int(5) NOT NULL default '0',
  `company_deal_total` int(5) NOT NULL default '0',
  `company_comment` text,
  PRIMARY KEY  (`company_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Company`
--

LOCK TABLES `Company` WRITE;
/*!40000 ALTER TABLE `Company` DISABLE KEYS */;
/*!40000 ALTER TABLE `Company` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CompanyActivity`
--

DROP TABLE IF EXISTS `CompanyActivity`;
CREATE TABLE `CompanyActivity` (
  `companyactivity_id` int(8) NOT NULL auto_increment,
  `companyactivity_domain_id` int(8) default '0',
  `companyactivity_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `companyactivity_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `companyactivity_userupdate` int(8) default NULL,
  `companyactivity_usercreate` int(8) default NULL,
  `companyactivity_code` varchar(10) default '',
  `companyactivity_label` varchar(64) default NULL,
  PRIMARY KEY  (`companyactivity_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CompanyActivity`
--

LOCK TABLES `CompanyActivity` WRITE;
/*!40000 ALTER TABLE `CompanyActivity` DISABLE KEYS */;
INSERT INTO `CompanyActivity` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Education'),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Industrie');
/*!40000 ALTER TABLE `CompanyActivity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CompanyNafCode`
--

DROP TABLE IF EXISTS `CompanyNafCode`;
CREATE TABLE `CompanyNafCode` (
  `companynafcode_id` int(8) NOT NULL auto_increment,
  `companynafcode_domain_id` int(8) default '0',
  `companynafcode_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `companynafcode_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `companynafcode_userupdate` int(8) default NULL,
  `companynafcode_usercreate` int(8) default NULL,
  `companynafcode_title` int(1) NOT NULL default '0',
  `companynafcode_code` varchar(4) default NULL,
  `companynafcode_label` varchar(128) default NULL,
  PRIMARY KEY  (`companynafcode_id`)
) ENGINE=MyISAM AUTO_INCREMENT=803 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CompanyNafCode`
--

LOCK TABLES `CompanyNafCode` WRITE;
/*!40000 ALTER TABLE `CompanyNafCode` DISABLE KEYS */;
INSERT INTO `CompanyNafCode` VALUES (1,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'01','Agriculture, chasse, services annexes'),(2,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'011A','Culture de céréales ; cultures industrielles'),(3,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'011C','Culture de légumes ; maraichage'),(4,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'011D','Horticulture ; pépinières'),(5,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'011F','Culture fruitière'),(6,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'011G','Viticulture'),(7,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'012A','Elevage de bovins'),(8,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'012C','Elevage d\'ovins, caprins et équidés'),(9,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'012E','Elevage de porcins'),(10,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'012G','Elevage de volailles'),(11,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'012J','Elevage d\'autres animaux'),(12,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'013Z','Culture et élevage associés'),(13,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'014A','Services aux cultures productives'),(14,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'014B','Réalisation et entretien de plantations ornementales'),(15,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'014D','Services annexes à l\'élevage'),(16,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'015Z','Chasse'),(17,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'02','Sylviculture, exploitation forestière, services annexes'),(18,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'020A','Sylviculture'),(19,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'020B','Exploitation forestière'),(20,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'020D','Services forestiers'),(21,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'05','Pêche, aquacultures'),(22,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'050A','Pêche'),(23,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'050C','Pisciculture, aquaculture'),(24,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'10','Extraction de houille, de lignite et de tourbes'),(25,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'101Z','Extraction et agglomération de la houille'),(26,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'102Z','Extraction et agglomération du lignite'),(27,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'103Z','Extraction et agglomération de la tourbe'),(28,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'11','Extraction d\'hydrocarbures, services annexes'),(29,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'111Z','Extraction d\'hydrocarbures'),(30,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'112Z','Services annexes à l\'extraction d\'hydrocarbures'),(31,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'12','Extraction de minerais d\'uraniums'),(32,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'120Z','Extraction de minerais d\'uranium'),(33,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'13','Extraction de minerais metalliques'),(34,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'131Z','Extraction de minerais de fer'),(35,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'132Z','Extraction de minerais de métaux non ferreux'),(36,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'14','Autres industries extractives'),(37,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'141A','Extraction de pierres pour la construction'),(38,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'141C','Extraction de calcaire industriel, de gypse et de craie'),(39,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'141E','Extraction d\'ardoise'),(40,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'142A','Production de sables et de granulats'),(41,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'142C','Extraction d\'argiles et de kaolin'),(42,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'143Z','Extrac de minéraux pour industrie chimique et d\'engrais naturels'),(43,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'144Z','Production de sel'),(44,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'145Z','Activités extractives n.c.a.'),(45,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'15','Industries alimentaires'),(46,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'151A','Production de viandes de boucherie'),(47,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'151C','Production de viandes de volaille'),(48,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'151E','Préparation industrielle de produits à base de viande'),(49,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'151F','Charcuterie'),(50,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'152Z','Industrie du poisson'),(51,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'153A','Transformation et conservation de pommes de terre'),(52,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'153C','Préparation de jus de fruits et légumes'),(53,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'153E','Transformation et conservation de légumes'),(54,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'153F','Transformation et conservation de fruits'),(55,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'154A','Fabrication d\'huiles et graisses brutes'),(56,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'154C','Fabrication d\'huiles et graisses raffinées'),(57,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'154E','Fabrication de margarine'),(58,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'155A','Fabrication de lait liquide et de produits frais'),(59,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'155B','Fabrication de beurre'),(60,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'155C','Fabrication de fromages'),(61,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'155D','Fabrication d\'autres produits laitiers'),(62,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'155F','Fabrication de glaces et sorbets'),(63,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'156A','Meunerie'),(64,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'156B','Autres activités de travail des grains'),(65,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'156D','Fabrication de produits amylacés'),(66,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'157A','Fabrication d\'aliments pour animaux de ferme'),(67,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'157C','Fabrication d\'aliments pour animaux de compagnie'),(68,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158A','Fabrication industrielle de pain et de pâtisserie fraîche'),(69,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158B','Cuisson de produits de boulangerie'),(70,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158C','Boulangerie et boulangerie pâtisserie'),(71,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158D','Pâtisserie'),(72,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158F','Biscotterie, biscuiterie, pâtisserie de conservation'),(73,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158H','Fabrication de sucre'),(74,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158K','Chocolaterie, confiserie'),(75,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158M','Fabrication de pâtes alimentaires'),(76,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158P','Transformation du thé et du café'),(77,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158R','Fabrication de condiments et assaisonnements'),(78,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158T','Fabrication d\'aliments adaptés à l\'enfant et diététiques'),(79,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'158V','Industries alimentaires n.c.a.'),(80,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159A','Production d\'eaux de vie naturelles'),(81,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159B','Fabrication de spiritueux'),(82,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159D','Production d\'alcool éthylique de fermentation'),(83,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159F','Champagnisation'),(84,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159G','Vinification'),(85,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159J','Cidrerie'),(86,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159L','Production d\'autres boissons fermentées'),(87,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159N','Brasserie'),(88,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159Q','Malterie'),(89,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159S','Industrie des eaux de table'),(90,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'159T','Production de boissons rafraîchissantes'),(91,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'16','Industrie du tabacs'),(92,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'160Z','Industrie du tabac'),(93,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'17','Industrie textiles'),(94,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171A','Filature de l\'industrie cotonnière'),(95,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171C','Filature de l\'industrie lainière cycle cardé'),(96,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171E','Préparation de la laine'),(97,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171F','Filature de l\'industrie lainière cycle peigné'),(98,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171H','Préparation et filature du lin'),(99,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171K','Moulinage texturation de soie et textiles artif et synthétiques'),(100,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171M','Fabrication de fils à coudre'),(101,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'171P','Préparation et filature d\'autres fibres'),(102,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'172A','Tissage de l\'industrie cotonnière'),(103,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'172C','Tissage de l\'industrie lainière cycle cardé'),(104,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'172E','Tissage de l\'industrie lainière cycle peigné'),(105,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'172G','Tissage de soieries'),(106,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'172J','Tissage d\'autres textiles'),(107,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'173Z','Ennoblissement textile'),(108,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'174A','Fabrication de linge de maison et d\'articles d\'ameublement'),(109,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'174B','Fabrication de petits articles textiles de literie'),(110,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'174C','Fabrication d\'autres articles confectionnés en textile'),(111,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'175A','Fabrication de tapis et moquettes'),(112,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'175C','Ficellerie, corderie, fabrication de filets'),(113,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'175E','Fabrication de non tissés'),(114,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'175G','Industries textiles n.c.a.'),(115,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'176Z','Fabrication d\'étoffes à maille'),(116,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'177A','Fabrication d\'articles chaussants à maille'),(117,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'177C','Fabrication de pull overs et articles similaires'),(118,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'18','Industrie de l\'habillement et des fourrures'),(119,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'181Z','Fabrication de vêtements en cuir'),(120,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'182A','Fabrication de vêtements de travail'),(121,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'182C','Fabrication de vêtements sur mesure'),(122,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'182D','Fabrication de vêtements de dessus pour hommes et garçonnets'),(123,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'182E','Fabrication de vêtements de dessus pour femmes et fillettes'),(124,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'182G','Fabrication de vêtements de dessous'),(125,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'182J','Fabrication d\'autres vêtements et accessoires'),(126,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'183Z','Industrie des fourrures'),(127,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'19','Industrie du cuir et de la chaussures'),(128,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'191Z','Apprêt et tannage des cuirs'),(129,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'192Z','Fabrication d\'articles de voyage et de maroquinerie'),(130,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'193Z','Fabrication de chaussures'),(131,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'20','Travail du bois et fabrication d\'articles en bois'),(132,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'201A','Sciage et rabotage du bois'),(133,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'201B','Imprégnation du bois'),(134,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'202Z','Fabrication de panneaux de bois'),(135,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'203Z','Fabrication de charpentes et de menuiseries'),(136,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'204Z','Fabrication d\'emballages en bois'),(137,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'205A','Fabrication d\'objets divers en bois'),(138,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'205C','Fabrication d\'objets en liège, vannerie ou sparterie'),(139,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'21','Industrie du papier et du cartons'),(140,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'211A','Fabrication de pâte à papier'),(141,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'211C','Fabrication de papier et de carton'),(142,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212A','Industrie du carton ondulé'),(143,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212B','Fabrication de cartonnages'),(144,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212C','Fabrication d\'emballages en papier'),(145,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212E','Fabrication d\'articles en papier à usage sanitaire ou domestique'),(146,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212G','Fabrication d\'articles de papeterie'),(147,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212J','Fabrication de papiers peints'),(148,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'212L','Fabrication d\'autres articles en papier ou en carton'),(149,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'22','Edition, imprimerie, reproductions'),(150,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'221A','Edition de livres'),(151,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'221C','Edition de journaux'),(152,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'221E','Edition de revues et périodiques'),(153,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'221G','Edition d\'enregistrements sonores'),(154,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'221J','Autres activités d\'édition'),(155,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'222A','Imprimerie de journaux'),(156,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'222C','Autre imprimerie (labeur)'),(157,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'222E','Reliure et finition'),(158,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'222G','Composition et photogravure'),(159,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'222J','Autres activités graphiques'),(160,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'223A','Reproduction d\'enregistrements sonores'),(161,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'223C','Reproduction d\'enregistrements vidéo'),(162,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'223E','Reproduction d\'enregistrements informatiques'),(163,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'23','Cokefaction, raffinage, industries nucléaires'),(164,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'231Z','Cokéfaction'),(165,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'232Z','Raffinage de pétrole'),(166,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'233Z','Elaboration et transformation de matières nucléaires'),(167,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'24','Industrie chimiques'),(168,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241A','Fabrication de gaz industriels'),(169,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241C','Fabrication de colorants et de pigments'),(170,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241E','Fabrication d\'autres produits chimiques inorganiques de base'),(171,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241G','Fabrication d\'autres produits chimiques organiques de base'),(172,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241J','Fabrication de produits azotés et d\'engrais'),(173,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241L','Fabrication de matières plastiques de base'),(174,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'241N','Fabrication de caoutchouc synthétique'),(175,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'242Z','Fabrication de produits agrochimiques'),(176,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'243Z','Fabrication de peintures et vernis'),(177,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'244A','Fabrication de produits pharmaceutiques de base'),(178,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'244C','Fabrication de médicaments'),(179,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'244D','Fabrication d\'autres produits pharmaceutiques'),(180,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'245A','Fabrication de savons, détergents et produits d\'entretien'),(181,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'245C','Fabrication de parfums et de produits pour la toilette'),(182,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'246A','Fabrication de produits explosifs'),(183,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'246C','Fabrication de colles et gélatines'),(184,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'246E','Fabrication d\'huiles essentielles'),(185,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'246G','Fabrication de produits chimiques pour la photographie'),(186,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'246J','Fabrication de supports de données'),(187,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'246L','Fabrication de produits chimiques à usage industriel'),(188,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'247Z','Fabrication de fibres artificielles ou synthétiques'),(189,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'25','Industrie du caoutchouc et des plastiques'),(190,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'251A','Fabrication de pneumatiques'),(191,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'251C','Rechapage de pneumatiques'),(192,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'251E','Fabrication d\'autres articles en caoutchouc'),(193,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'252A','Fabric de plaques, feuilles, tubes et profilés en plastiques'),(194,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'252C','Fabrication d\'emballages en matières plastiques'),(195,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'252E','Fabric d\'éléments en matières plastiques pour la construction'),(196,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'252G','Fabrication d\'articles divers en matières plastiques'),(197,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'252H','Fabrication de pièces techniques en matières plastiques'),(198,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'26','Fabrication d\'autres produits mineraux non metalliques'),(199,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'261A','Fabrication de verre plat'),(200,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'261C','Façonnage et transformation du verre plat'),(201,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'261E','Fabrication de verre creux'),(202,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'261G','Fabrication de fibres de verre'),(203,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'261J','Fabrication et façonnage d\'articles techniques en verre'),(204,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'261K','Fabrication d\'isolateurs en verre'),(205,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'262A','Fabric d\'articles céramiques à usage domestique ou ornemental'),(206,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'262C','Fabrication d\'appareils sanitaires en céramique'),(207,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'262E','Fabrication d\'isolateurs et pièces isolantes en céramique'),(208,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'262G','Fabrication d\'autres produits céramiques à usage technique'),(209,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'262J','Fabrication d\'autres produits céramiques'),(210,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'262L','Fabrication de produits céramiques réfractaires'),(211,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'263Z','Fabrication de carreaux en céramique'),(212,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'264A','Fabrication de briques'),(213,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'264B','Fabrication de tuiles'),(214,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'264C','Fabrication de produits divers en terre cuite'),(215,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'265A','Fabrication de ciment'),(216,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'265C','Fabrication de chaux'),(217,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'265E','Fabrication de plâtre'),(218,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'266A','Fabrication d\'éléments en béton pour la construction'),(219,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'266C','Fabrication d\'éléments en plâtre pour la construction'),(220,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'266E','Fabrication de béton prêt à l\'emploi'),(221,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'266G','Fabrication de mortiers et bétons secs'),(222,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'266J','Fabrication d\'ouvrages en fibre ciment'),(223,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'266L','Fabrication d\'autres ouvrages en béton ou en plâtre'),(224,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'267Z','Travail de la pierre'),(225,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'268A','Fabrication de produits abrasifs'),(226,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'268C','Fabrication de produits minéraux non métalliques n.c.a.'),(227,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'27','Metallurgies'),(228,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'271Y','Sidérurgie'),(229,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'271Z','Sidérurgie (CECA)'),(230,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'272A','Fabrication de tubes en fonte'),(231,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'272C','Fabrication de tubes en acier'),(232,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'273A','Etirage à froid'),(233,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'273C','Laminage à froid de feuillards'),(234,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'273E','Profilage à froid par formage ou pliage'),(235,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'273G','Tréfilage à froid'),(236,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'273J','Production de ferroalliages et autres produits non CECA'),(237,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274A','Production de métaux précieux'),(238,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274C','Production d\'aluminium'),(239,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274D','Première transformation de l\'aluminium'),(240,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274F','Production de plomb, de zinc ou d\'étain'),(241,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274G','Première transformation du plomb, du zinc ou de l\'étain'),(242,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274J','Production de cuivre'),(243,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274K','Première transformation du cuivre'),(244,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'274M','Métallurgie des autres métaux non ferreux'),(245,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'275A','Fonderie de fonte'),(246,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'275C','Fonderie d\'acier'),(247,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'275E','Fonderie de métaux légers'),(248,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'275G','Fonderie d\'autres métaux non ferreux'),(249,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'28','Travail des métauxs'),(250,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'281A','Fabrication de constructions métalliques'),(251,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'281C','Fabrication de menuiseries et fermetures métalliques'),(252,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'282A','Fabrication de réservoirs et citernes métalliques'),(253,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'282B','Fabrication de bouteilles pour gaz comprimés'),(254,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'282C','Fabrication de réservoirs, citernes et conteneurs métalliques'),(255,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'282D','Fabric de radiateurs et de chaudières pour le chauffage central'),(256,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'283A','Fabrication de générateurs de vapeur'),(257,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'283B','Chaudronnerie nucléaire'),(258,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'283C','Chaudronnerie tuyauterie'),(259,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'284A','Forge, estampage, matriçage'),(260,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'284B','Découpage, emboutissage'),(261,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'284C','Métallurgie des poudres'),(262,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'285A','Traitement et revêtement des métaux'),(263,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'285C','Décolletage'),(264,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'285D','Mécanique générale'),(265,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'286A','Fabrication de coutellerie'),(266,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'286C','Fabrication d\'outillage à main'),(267,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'286D','Fabrication d\'outillage mécanique'),(268,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'286F','Fabrication de serrures et ferrures'),(269,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287A','Fabrication de futs et emballages métalliques similaires'),(270,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287C','Fabrication d\'emballages métalliques légers'),(271,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287E','Fabrication d\'articles en fils métalliques'),(272,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287G','Visserie et boulonnerie'),(273,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287H','Fabrication de ressorts'),(274,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287J','Fabrication de chaines'),(275,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287L','Fabrication d\'articles métalliques ménagers'),(276,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287M','Fabrication de coffres forts'),(277,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287N','Fabrication de petits articles métalliques'),(278,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287P','Fabrication d\'articles métalliques n.c.a.'),(279,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'287Q','Fabrication d\'articles métalliques divers'),(280,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'29','Fabrication de machines et équipements'),(281,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291A','Fabrication de moteurs et turbines'),(282,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291B','Fabrication de pompes'),(283,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291C','Fabrication de pompes et compresseurs'),(284,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291D','Fabrication de transmissions hydrauliques et pneumatiques'),(285,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291E','Fabrication de compresseurs'),(286,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291F','Fabrication d\'articles de robinetterie'),(287,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291H','Fabrication de roulements'),(288,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'291J','Fabrication d\'organes mécaniques de transmission'),(289,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292A','Fabrication de fours et brûleurs'),(290,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292C','Fabrication d\'ascenseurs, monte charges et escaliers mécaniques'),(291,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292D','Fabrication d\'équipements de levage et de manutention'),(292,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292F','Fabric d\'équipements aérauliques et frigorifiques industriels'),(293,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292H','Fabrication d\'équipements d\'emballage et de conditionnement'),(294,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292J','Fabrication d\'appareils de pesage'),(295,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292K','Fabrication de machines diverses d\'usage général'),(296,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292L','Fabrication de matériel pour les industries chimiques'),(297,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'292M','Fabrication d\'autres machines d\'usage général'),(298,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'293A','Fabrication de tracteurs agricoles'),(299,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'293C','Réparation de matériel agricole'),(300,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'293D','Fabrication de matériel agricole'),(301,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'294A','Fabrication de machines outils à métaux'),(302,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'294B','Fabrication de machines outils à bois'),(303,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'294C','Fabrication de machines outils portatives à moteur incorporé'),(304,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'294D','Fabrication de matériel de soudage'),(305,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'294E','Fabrication d\'autres machines outils'),(306,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295A','Fabrication de machines pour la métallurgie'),(307,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295B','Fabrication de matériels de mines pour l\'extraction'),(308,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295C','Fabrication de machines pour l\'extraction ou la construction'),(309,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295D','Fabrication de matériels de travaux publics'),(310,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295E','Fabrication de machines pour l\'industrie agroalimentaire'),(311,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295G','Fabrication de machines pour les industries textiles'),(312,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295J','Fabric de machines pour les industries du papier et du carton'),(313,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295L','Fabrication de machines d\'imprimerie'),(314,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295M','Fabric de machines pour travail du caoutchouc ou des plastiques'),(315,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295N','Fabrication de moules et modèles'),(316,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295P','Fabrication d\'autres machines spécialisées'),(317,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295Q','Fabrication de machines d\'assemblage automatique'),(318,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'295R','Fabrication de machines spécialisées diverses'),(319,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'296A','Fabrication d\'armement'),(320,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'296B','Fabrication d\'armes de chasse, de tir et de défense'),(321,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'297A','Fabrication d\'appareils électroménagers'),(322,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'297C','Fabrication d\'appareils ménagers non électriques'),(323,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'30','Fabrication de machines de bureau et de matériel informatiques'),(324,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'300A','Fabrication de machines de bureau'),(325,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'300C','Fabrication d\'ordinateurs et d\'autres équipements informatiques'),(326,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'31','Fabrication de machines et appareils électriques'),(327,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'311A','Fabric de moteurs, génératrices et transfo électriq inf à 750 kW'),(328,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'311B','Fabric de moteurs, génératrices et transfo électriq sup à 750 kW'),(329,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'311C','Réparation de matériels électriques'),(330,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'312A','Fabric matériel distribution ou commande électrique basse tension'),(331,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'312B','Fabric matériel distribution ou commande électrique haute tension'),(332,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'313Z','Fabrication de fils et câbles isolés'),(333,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'314Z','Fabrication d\'accumulateurs et de piles électriques'),(334,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'315A','Fabrication de lampes'),(335,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'315B','Fabrication d\'appareils électriques autonomes de sécurité'),(336,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'315C','Fabrication d\'appareils d\'éclairage'),(337,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'316A','Fabrication de matériels électriques pour moteurs et véhicules'),(338,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'316C','Fabrication de matériel électromagnétique industriel'),(339,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'316D','Fabrication de matériels électriques n.c.a.'),(340,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'32','Fabrication d\'équipements de radio, télévision et communications'),(341,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'321A','Fabrication de composants passifs et de condensateurs'),(342,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'321C','Fabrication de composants électroniques actifs'),(343,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'321D','Assemblage de cartes électroniques pour compte de tiers'),(344,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'322A','Fabrication équipements d\'émission et de transmission hertzienne'),(345,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'322B','Fabrication d\'appareils de téléphonie'),(346,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'323Z','Fab appareils réception, enregistrmt, reproduction son et image'),(347,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'33','Fabrication d\'instruments médicaux, de précision, d\'optique et d\'horlogeries'),(348,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'331A','Fabrication de matériel d\'imagerie médicale et de radiologie'),(349,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'331B','Fabrication d\'appareils médicochirurgicaux'),(350,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'332A','Fabrication d\'équipements d\'aide à la navigation'),(351,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'332B','Fabrication d\'instrumentation scientifique et technique'),(352,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'333Z','Fabrication d\'équipements de contrôle des processus industriels'),(353,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'334A','Fabrication de lunettes'),(354,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'334B','Fabrication d\'instruments d\'optique et de matériel photographique'),(355,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'335Z','Horlogerie'),(356,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'34','Industrie automobiles'),(357,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'341Z','Construction de véhicules automobiles'),(358,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'342A','Fabrication de carrosseries automobiles'),(359,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'342B','Fabrication de caravanes et véhicules de loisirs'),(360,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'343Z','Fabrication d\'équipements automobiles'),(361,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'35','Fabrication d\'autres matériels de transports'),(362,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'351A','Construction de bâtiments de guerre'),(363,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'351B','Construction de navires civils'),(364,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'351C','Réparation navale'),(365,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'351E','Construction de bateaux de plaisance'),(366,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'352Z','Construction de matériel ferroviaire roulant'),(367,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'353A','Construction de moteurs pour aéronefs'),(368,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'353B','Construction de cellules d\'aéronefs'),(369,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'353C','Construction de lanceurs et engins spatiaux'),(370,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'354A','Fabrication de motocycles'),(371,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'354C','Fabrication de bicyclettes'),(372,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'354E','Fabrication de véhicules pour invalides'),(373,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'355Z','Fabrication de matériels de transport n.c.a.'),(374,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'36','Fabrication de meubles; industries diverses'),(375,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361A','Fabrication de sièges'),(376,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361C','Fabrication de meubles de bureau et de magasin'),(377,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361E','Fabrication de meubles de cuisine'),(378,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361G','Fabrication de meubles meublants'),(379,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361H','Fabrication de meubles de jardin et d\'extérieur'),(380,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361J','Fabrication de meubles n.c.a.'),(381,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361K','Industries connexes de l\'ameublement'),(382,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'361M','Fabrication de matelas'),(383,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'362A','Fabrication de monnaies et médailles'),(384,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'362C','Bijouterie, joaillerie, orfèvrerie'),(385,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'363Z','Fabrication d\'instruments de musique'),(386,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'364Z','Fabrication d\'articles de sport'),(387,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'365Z','Fabrication de jeux et jouets'),(388,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'366A','Bijouterie fantaisie'),(389,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'366C','Industrie de la brosserie'),(390,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'366E','Autres activités manufacturières n.c.a.'),(391,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'37','Récupérations'),(392,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'371Z','Récupération de matières métalliques recyclables'),(393,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'372Z','Récupération de matières non métalliques recyclables'),(394,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'40','Production et distribution d\'électricité, de gaz et de chaleurs'),(395,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'401A','Production d\'électricité'),(396,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'401C','Transport d\'électricité'),(397,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'401E','Distribution et commerce d\'électricité'),(398,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'401Z','Production et distribution d\'électricité'),(399,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'402A','Production de combustibles gazeux'),(400,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'402C','Distribution de combustibles gazeux'),(401,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'402Z','Production et distribution de combustibles gazeux'),(402,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'403Z','Production et distribution de chaleur'),(403,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'41','Captage, traitement et distribution d\'eau'),(404,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'410Z','Captage, traitement et distribution d\'eau'),(405,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'45','Constructions'),(406,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'451A','Terrassements divers, démolition'),(407,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'451B','Terrassements en grande masse'),(408,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'451D','Forages et sondages'),(409,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452A','Construction de maisons individuelles'),(410,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452B','Construction de bâtiments divers'),(411,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452C','Construction d\'ouvrages d\'art'),(412,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452D','Travaux souterrains'),(413,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452E','Réalisation de réseaux'),(414,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452F','Construction de lignes électriques et de télécommunications'),(415,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452J','Réalisation de couvertures par éléments'),(416,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452K','Travaux d\'étanchéification'),(417,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452L','Travaux de charpente'),(418,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452N','Construction de voies ferrées'),(419,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452P','Construction de chaussées routières et de sols sportifs'),(420,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452R','Travaux maritimes et fluviaux'),(421,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452T','Levage, montage'),(422,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452U','Autres travaux spécialisés de construction'),(423,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'452V','Travaux de maçonnerie générale'),(424,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'453A','Travaux d\'installation électrique'),(425,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'453C','Travaux d\'isolation'),(426,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'453E','Installation d\'eau et de gaz'),(427,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'453F','Installation d\'équipements thermiques et de climatisation'),(428,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'453H','Autres travaux d\'installation'),(429,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454A','Plâtrerie'),(430,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454C','Menuiserie bois et matières plastiques'),(431,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454D','Menuiserie métallique ; serrurerie'),(432,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454F','Revêtement des sols et des murs'),(433,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454H','Miroiterie de bâtiment ; vitrerie'),(434,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454J','Peinture'),(435,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454L','Agencement de lieux de vente'),(436,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'454M','Travaux de finition n.c.a.'),(437,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'455Z','Location avec opérateur de matériel de construction'),(438,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'50','Commerce et réparation automobiles'),(439,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'501Z','Commerce de véhicules automobiles'),(440,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'502Z','Entretien et réparation de véhicules automobiles'),(441,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'503A','Commerce de gros d\'équipements automobiles'),(442,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'503B','Commerce de détail d\'équipements automobiles'),(443,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'504Z','Commerce et réparation de motocycles'),(444,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'505Z','Commerce de détail de carburants'),(445,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'51','Commerce de gros et intermédiaires du commerces'),(446,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511A','Interméd comm en mat premières agric ou textiles, animaux vivants'),(447,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511C','Interméd comm en combustibles, métaux, minéraux, prod chimiques'),(448,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511E','Intermédiaires du commerce en bois et matériaux de construction'),(449,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511G','Interméd comm en machines, équipemts industriels, navires, avions'),(450,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511J','Interméd comm en meubles, articles de ménage et quincaillerie'),(451,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511L','Interméd comm en textiles, habillement, chaussures, articles cuir'),(452,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511N','Intermédiaires du commerce en produits alimentaires'),(453,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511P','Centrales d\'achats alimentaires'),(454,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511R','Autres intermédiaires spécialisés du commerce'),(455,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511T','Intermédiaires non spécialisés du commerce'),(456,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'511U','Centrales d\'achats non alimentaires'),(457,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'512A','Commerce de gros de céréales et aliments pour le bétail'),(458,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'512C','Commerce de gros de fleurs et plantes'),(459,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'512E','Commerce de gros d\'animaux vivants'),(460,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'512G','Commerce de gros de cuirs et peaux'),(461,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'512J','Commerce de gros de tabac non manufacturé'),(462,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513A','Commerce de gros de fruits et légumes'),(463,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513C','Commerce de gros de viandes de boucherie'),(464,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513D','Commerce de gros de produits à base de viande'),(465,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513E','Commerce de gros de volailles et gibiers'),(466,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513G','Commerce de gros de produits laitiers, oeufs, huiles'),(467,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513J','Commerce de gros de boissons'),(468,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513L','Commerce de gros de tabac'),(469,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513N','Commerce de gros de sucre, chocolat et confiserie'),(470,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513Q','Commerce de gros de café, thé, cacao et épices'),(471,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513S','Commerce de gros de poissons, crustacés et mollusques'),(472,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513T','Commerces de gros alimentaires spécialisés divers'),(473,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513V','Commerce de gros de produits surgelés'),(474,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'513W','Commerce de gros alimentaire non spécialisé'),(475,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514A','Commerce de gros de textiles'),(476,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514C','Commerce de gros d\'habillement'),(477,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514D','Commerce de gros de la chaussure'),(478,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514F','Comm gros d\'appareils électroménagers et de radio télévision'),(479,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514H','Commerce de gros de vaisselle et verrerie de ménage'),(480,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514J','Comm gros de produits pour entretien et aménagement habitat'),(481,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514L','Commerce de gros de parfumerie et produits de beauté'),(482,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514N','Commerce de gros de produits pharmaceutiques'),(483,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514Q','Commerce de gros de papeterie'),(484,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514R','Commerce de gros de jouets'),(485,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'514S','Autres commerces de gros de biens de consommation'),(486,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515A','Commerce de gros de combustibles'),(487,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515C','Commerce de gros de minerais et métaux'),(488,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515E','Commerce de gros de bois et de produits dérivés'),(489,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515F','Comm gros de matériaux de construction et appareils sanitaires'),(490,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515H','Commerce de gros de quincaillerie'),(491,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515J','Commerce de gros de fournitures pour plomberie et chauffage'),(492,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515L','Commerce de gros de produits chimiques'),(493,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515N','Commerce de gros d\'autres produits intermédiaires'),(494,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'515Q','Commerce de gros de déchets et débris'),(495,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516A','Commerce de gros de machines outils'),(496,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516C','Commerce de gros d\'équipements pour la construction'),(497,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516E','Comm gros de machines pour l\'industrie textile et l\'habillement'),(498,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516G','Commerce de gros de machines de bureau et matériel informatique'),(499,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516J','Commerce de gros de matériel électrique et électronique'),(500,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516K','Commerce de gros de fournitures et équipements industriels divers'),(501,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516L','Comm gros fournitures et équipts divers pr commerce et services'),(502,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'516N','Commerce de gros de matériel agricole'),(503,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'517Z','Autres commerces de gros'),(504,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'5181','Commerce de gros de machines outils'),(505,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518C','Commerce de gros de machines pour l\'extraction, la construction et le génie civil'),(506,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518E','Commerce de gros de machines pour l\'industrie textile et l\'habillement'),(507,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518G','Commerce de gros d\'ordinateurs, d\'équipements informatiques périphériques et de progiciels'),(508,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518H','Commerce de gros d\'autres machines et équipements de bureau'),(509,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518J','Commerce de gros de composants et d\'autres équipements électroniques'),(510,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518L','Commerce de gros de matériel électrique'),(511,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518M','Commerce de gros de fournitures et équipement industriels divers'),(512,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518N','Commerce de gros de fournitures et équipement industriels divers pour le commerce et les services'),(513,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'518P','Commerce de gros de matériel agricole'),(514,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'519A','Autres commerces de gros spécialisés'),(515,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'519B','Commerce de gros non spécialisé'),(516,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'52','Commerce de détail et réparation d\'articles domestiques'),(517,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521A','Commerce de détail de produits surgelés'),(518,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521B','Commerce d\'alimentation générale'),(519,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521C','Superettes'),(520,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521D','Supermarchés'),(521,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521E','Magasins populaires'),(522,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521F','Hypermarchés'),(523,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521H','Grands magasins'),(524,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'521J','Autres commerces de détail en magasin non spécialisé'),(525,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522A','Commerce de détail de fruits et légumes'),(526,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522C','Commerce de détail de viandes et produits à base de viande'),(527,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522E','Commerce de détail de poissons, crustacés et mollusques'),(528,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522G','Commerce de détail de pain, pâtisserie et confiserie'),(529,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522J','Commerce de détail de boissons'),(530,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522L','Commerce de détail de tabac'),(531,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522N','Commerce de détail de produits laitiers'),(532,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'522P','Commerces de détail alimentaires spécialisés divers'),(533,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'523A','Commerce de détail de produits pharmaceutiques'),(534,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'523C','Commerce de détail d\'articles médicaux et orthopédiques'),(535,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'523E','Commerce de détail de parfumerie et de produits de beauté'),(536,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524A','Commerce de détail de textiles'),(537,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524C','Commerce de détail d\'habillement'),(538,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524E','Commerce de détail de la chaussure'),(539,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524F','Commerce de détail de maroquinerie et d\'articles de voyage'),(540,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524H','Commerce de détail de meubles'),(541,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524J','Commerce de détail d\'équipements du foyer'),(542,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524L','Comm détail d\'appareils électroménagers et de radio télévision'),(543,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524N','Commerce de détail de quincaillerie'),(544,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524P','Commerce de détail de bricolage'),(545,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524R','Commerce de détail de livres, journaux et papeterie'),(546,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524T','Commerce de détail d\'optique et de photographie'),(547,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524U','Commerce de détail de revêtements de sols et de murs'),(548,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524V','Commerce de détail d\'horlogerie et de bijouterie'),(549,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524W','Commerce de détail d\'articles de sport et de loisir'),(550,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524X','Commerce de détail de fleurs'),(551,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524Y','Commerce de détail de charbons et combustibles'),(552,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'524Z','Commerces de détail divers en magasin spécialisé'),(553,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'525Z','Commerce de détail de biens d\'occasion en magasin'),(554,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'526A','Vente par correspondance sur catalogue général'),(555,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'526B','Vente par correspondance spécialisée'),(556,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'526D','Commerce de détail alimentaire sur éventaires et marchés'),(557,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'526E','Commerce de détail non alimentaire sur éventaires et marchés'),(558,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'526G','Vente à domicile'),(559,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'526H','Vente par automate'),(560,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'527A','Réparation de chaussures et articles en cuir'),(561,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'527C','Réparation de matériel électronique grand public'),(562,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'527D','Réparation d\'articles électriques à usage domestique'),(563,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'527F','Réparation de montres, horloges et bijoux'),(564,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'527H','Réparation d\'articles personnels et domestiques n.c.a.'),(565,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'55','Hôtels et restaurants'),(566,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'551A','Hôtels avec restaurant'),(567,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'551C','Hôtels de tourisme sans restaurant'),(568,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'551D','Hôtels de préfecture'),(569,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'551E','Autres hôtels'),(570,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'552A','Auberges de jeunesse et refuges'),(571,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'552C','Exploitation de terrains de camping'),(572,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'552E','Autre hébergement touristique'),(573,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'552F','Hébergement collectif non touristique'),(574,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'553A','Restauration de type traditionnel'),(575,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'553B','Restauration de type rapide'),(576,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'554A','Cafés tabacs'),(577,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'554B','Débits de boisson'),(578,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'554C','Discothèques'),(579,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'555A','Cantines, restaurants d\'entreprises'),(580,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'555C','Restauration collective sous contrat'),(581,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'555D','Traiteurs, organisation de réceptions'),(582,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'60','Transports terrestres'),(583,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'601Z','Transports ferroviaires'),(584,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602A','Transports urbains de voyageurs'),(585,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602B','Transports routiers réguliers de voyageurs'),(586,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602C','Téléphériques, remontées mécaniques'),(587,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602E','Transport de voyageurs par taxis'),(588,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602G','Autres transports routiers de voyageurs'),(589,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602L','Transports routiers de marchandises de proximité'),(590,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602M','Transports routiers de marchandises interurbains'),(591,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602N','Déménagement'),(592,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'602P','Location de camions avec conducteur'),(593,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'603Z','Transports par conduites'),(594,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'61','Transports par eaux'),(595,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'611A','Transports maritimes'),(596,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'611B','Transports côtiers'),(597,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'612Z','Transports fluviaux'),(598,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'62','Transports aériens'),(599,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'621Z','Transports aériens réguliers'),(600,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'622Z','Transports aériens non réguliers'),(601,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'623Z','Transports spatiaux'),(602,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'63','Services auxilliaires des transports'),(603,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'631A','Manutention portuaire'),(604,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'631B','Manutention non portuaire'),(605,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'631D','Entreposage frigorifique'),(606,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'631E','Entreposage non frigorifique'),(607,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'632A','Gestion d\'infrastructures de transports terrestres'),(608,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'632C','Services portuaires, maritimes et fluviaux'),(609,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'632E','Services aéroportuaires'),(610,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'633Z','Agences de voyage'),(611,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'634A','Messagerie, fret expres'),(612,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'634B','Affretement'),(613,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'634C','Organisation des transports internationaux'),(614,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'64','Postes et télécommunications'),(615,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'641A','Postes nationales'),(616,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'641C','Autres activités de courrier'),(617,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'642A','Télécommunications nationales'),(618,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'642B','Autres activités de télécommunications'),(619,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'642C','Télécommunications hors transmissions audiovisuelles'),(620,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'642D','Transmission d\'émissions de radio er de télévision'),(621,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'65','Intermédiation financières'),(622,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'651A','Banque centrale'),(623,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'651C','Banques'),(624,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'651D','Banques mutualistes'),(625,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'651E','Caisses d\'épargne'),(626,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'651F','Intermédiations monétaires n.c.a.'),(627,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'652A','Crédit bail'),(628,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'652C','Distribution de crédit'),(629,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'652E','Organismes de placement en valeurs mobilières'),(630,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'652F','Intermédiations financières diverses'),(631,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'66','Assurances'),(632,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'660A','Assurance vie et capitalisation'),(633,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'660C','Caisses de retraite'),(634,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'660E','Assurance dommages'),(635,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'660F','Réassurance'),(636,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'660G','Assurance relevant du code de la mutualité'),(637,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'67','Auxiliaires financiers et d\'assurances'),(638,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'671A','Administration de marchés financiers'),(639,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'671C','Gestion de portefeuilles'),(640,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'671E','Autres auxiliaires financiers'),(641,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'672Z','Auxiliaires d\'assurance'),(642,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'70','Activités immobilières'),(643,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'701A','Promotion immobilière de logements'),(644,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'701B','Promotion immobilière de bureaux'),(645,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'701C','Promotion immobilière d\'infrastructures'),(646,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'701D','Supports juridiques de programmes'),(647,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'701F','Marchands de biens immobiliers'),(648,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'702A','Location de logements'),(649,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'702B','Location de terrains'),(650,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'702C','Location d\'autres biens immobiliers'),(651,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'703A','Agences immobilières'),(652,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'703C','Administration d\'immeubles résidentiels'),(653,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'703D','Administration d\'autres biens immobiliers'),(654,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'703E','Supports juridiques de gestion de patrimoine'),(655,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'71','Location sans opérateurs'),(656,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'711A','Location de courte durée de véhicules automobiles'),(657,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'711B','Location de longue durée de véhicules automobiles'),(658,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'711Z','Location de véhicules automobiles'),(659,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'712A','Location d\'autres matériels de transport terrestre'),(660,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'712C','Location de matériels de transport par eau'),(661,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'712E','Location de matériels de transport aérien'),(662,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'713A','Location de matériel agricole'),(663,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'713C','Location de machines et équipements pour la construction'),(664,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'713E','Location de machines de bureau et de matériel informatique'),(665,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'713G','Location de machines et équipements divers'),(666,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'714A','Location de linge'),(667,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'714B','Location d\'autres biens personnels et domestiques'),(668,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'72','Activités informatiques'),(669,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'721Z','Conseil en systèmes informatiques'),(670,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'722A','Edition de logiciels (non personnalisés)'),(671,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'722C','Autres activités de réalisation de logiciels'),(672,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'722Z','Réalisation de logiciels'),(673,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'723Z','Traitement de données'),(674,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'724Z','Activités de banques de données'),(675,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'725Z','Entretien, réparation machines de bureau et matériel informatique'),(676,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'726Z','Autres activités rattachées à l\'informatique'),(677,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'73','Recherche et développements'),(678,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'731Z','Recherche développement en sciences physiques et naturelles'),(679,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'732Z','Recherche développement en sciences humaines et sociales'),(680,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'74','Services fournis principalement aux entreprises'),(681,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'741A','Activités juridiques'),(682,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'741C','Activités comptables'),(683,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'741E','Etudes de marché et sondages'),(684,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'741G','Conseil pour les affaires et la gestion'),(685,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'741J','Administration d\'entreprises'),(686,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'742A','Activités d\'architecture'),(687,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'742B','Métreurs, géomètres'),(688,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'742C','Ingénierie, études techniques'),(689,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'743A','Contrôle technique automobile'),(690,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'743B','Analyses, essais et inspections techniques'),(691,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'744A','Gestion de supports de publicité'),(692,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'744B','Agences, conseil en publicité'),(693,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'745A','Sélection et mise à disposition de personnel'),(694,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'745B','Travail temporaire'),(695,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'746Z','Enquêtes et sécurité'),(696,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'747Z','Activités de nettoyage'),(697,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748A','Studios et autres activités photographiques'),(698,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748B','Laboratoires de développement et de tirage'),(699,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748D','Conditionnement à façon'),(700,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748F','Secrétariat et traduction'),(701,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748G','Routage'),(702,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748H','Centres d\'appel'),(703,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748J','Organisation de foires et salons'),(704,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'748K','Services annexes à la production'),(705,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'75','Administration publiques'),(706,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'751A','Administration publique générale'),(707,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'751C','Tutelle des activités sociales'),(708,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'751E','Tutelle des activités économiques'),(709,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'751G','Activités de soutien aux administrations'),(710,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'752A','Affaires étrangères'),(711,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'752C','Défense'),(712,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'752E','Justice'),(713,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'752G','Police'),(714,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'752J','Protection civile'),(715,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'753A','Activités générales de sécurité sociale'),(716,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'753B','Gestion des retraites complémentaires'),(717,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'753C','Distribution sociale de revenus'),(718,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'80','Educations'),(719,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'801Z','Enseignement primaire'),(720,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'802A','Enseignement secondaire général'),(721,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'802C','Enseignement secondaire technique ou professionnel'),(722,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'803Z','Enseignement supérieur'),(723,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'804A','Ecoles de conduite'),(724,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'804C','Formation des adultes et formation continue'),(725,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'804D','Autres enseignements'),(726,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'85','Santé et action sociales'),(727,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851A','Activités hospitalières'),(728,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851C','Pratique médicale'),(729,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851E','Pratique dentaire'),(730,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851G','Activités des auxiliaires médicaux'),(731,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851H','Soins hors d\'un cadre règlementé'),(732,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851J','Ambulances'),(733,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851K','Laboratoires d\'analyses médicales'),(734,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'851L','Centres de collecte et banques d\'organes'),(735,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'852Z','Activités vétérinaires'),(736,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853A','Accueil des enfants handicapés'),(737,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853B','Accueil des enfants en difficulté'),(738,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853C','Accueil des adultes handicapés'),(739,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853D','Accueil des personnes âgées'),(740,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853E','Autres hébergements sociaux'),(741,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853G','Crèches et garderies d\'enfants'),(742,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853H','Aide par le travail, ateliers protégés'),(743,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853J','Aide à domicile'),(744,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'853K','Autres formes d\'action sociale'),(745,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'90','Assainissement, voierie et gestion des déchets'),(746,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'900A','Epuration des eaux usées'),(747,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'900B','Enlèvement et traitement des ordures ménagères'),(748,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'900C','Elimination et traitement des autres déchets'),(749,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'900E','Traitements des autres déchets solides'),(750,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'900G','Autres travaux d\'assainissement et de voirie'),(751,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'91','Activités associatives'),(752,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'911A','Organisations patronales et consulaires'),(753,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'911C','Organisations professionnelles'),(754,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'912Z','Syndicats de salariés'),(755,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'913A','Organisations religieuses'),(756,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'913C','Organisations politiques'),(757,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'913E','Organisations associatives n.c.a.'),(758,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'92','Activités récréatives, culturelles et sportives'),(759,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921A','Production de films pour la télévision'),(760,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921B','Production de films institutionnels et publicitaires'),(761,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921C','Production de films pour le cinéma'),(762,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921D','Prestations techniques pour le cinéma et la télévision'),(763,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921F','Distribution de films cinématographiques'),(764,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921G','Edition et distribution vidéo'),(765,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'921J','Projection de films cinématographiques'),(766,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'922A','Activités de radio'),(767,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'922B','Production de programmes de télévision'),(768,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'922C','Diffusion de programmes de télévision'),(769,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'922D','Edition de chaînes généralistes'),(770,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'922E','Edition de chaînes thématiques'),(771,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'922F','Distribution de bouquets de programmes de radio et de télévision'),(772,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923A','Activités artistiques'),(773,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923B','Services annexes aux spectacles'),(774,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923D','Gestion de salles de spectacle'),(775,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923F','Manèges forains et parcs d\'attractions'),(776,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923H','Bals et discothèques'),(777,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923J','Autres spectacles'),(778,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'923K','Activités diverses du spectacle'),(779,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'924Z','Agences de presse'),(780,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'925A','Gestion des bibliothèques'),(781,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'925C','Gestion du patrimoine culturel'),(782,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'925E','Gestion du patrimoine naturel'),(783,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'926A','Gestion d\'installations sportives'),(784,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'926C','Autres activités sportives'),(785,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'927A','Jeux de hasard et d\'argent'),(786,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'927C','Autres activités récréatives'),(787,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'93','Services personnels'),(788,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930A','Blanchisserie-teinturerie de gros'),(789,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930B','Blanchisserie-teinturerie de détail'),(790,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930D','Coiffure'),(791,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930E','Soins de beauté'),(792,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930G','Soins aux défunts'),(793,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930H','Pompes funèbres'),(794,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930K','Activités thermales et de thalassothérapie'),(795,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930L','Autres soins corporels'),(796,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'930N','Autres services personnels'),(797,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'95','Services domestiques'),(798,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'950Z','Services domestiques'),(799,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'960Z','Activités indifférenciées des ménages en tant que producteurs de biens pour usage propre'),(800,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'970Z','Activités indifférenciées des ménages en tant que producteurs de services pour usage propre'),(801,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,1,'99','Activités extra territoriales'),(802,1,'2007-10-04 14:49:50','0000-00-00 00:00:00',NULL,NULL,0,'990Z','Activités extra territoriales');
/*!40000 ALTER TABLE `CompanyNafCode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CompanyType`
--

DROP TABLE IF EXISTS `CompanyType`;
CREATE TABLE `CompanyType` (
  `companytype_id` int(8) NOT NULL auto_increment,
  `companytype_domain_id` int(8) default '0',
  `companytype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `companytype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `companytype_userupdate` int(8) default NULL,
  `companytype_usercreate` int(8) default NULL,
  `companytype_code` varchar(10) default '',
  `companytype_label` char(12) default NULL,
  PRIMARY KEY  (`companytype_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `CompanyType`
--

LOCK TABLES `CompanyType` WRITE;
/*!40000 ALTER TABLE `CompanyType` DISABLE KEYS */;
INSERT INTO `CompanyType` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Client'),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Fournisseur'),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Partenaire'),(4,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Prospect'),(5,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'','Media');
/*!40000 ALTER TABLE `CompanyType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Contact`
--

DROP TABLE IF EXISTS `Contact`;
CREATE TABLE `Contact` (
  `contact_id` int(8) NOT NULL auto_increment,
  `contact_domain_id` int(8) default '0',
  `contact_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `contact_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contact_userupdate` int(8) default NULL,
  `contact_usercreate` int(8) default NULL,
  `contact_datasource_id` int(8) default '0',
  `contact_company_id` int(8) default NULL,
  `contact_company` varchar(64) default NULL,
  `contact_kind_id` int(8) default NULL,
  `contact_marketingmanager_id` int(8) default NULL,
  `contact_lastname` varchar(64) NOT NULL default '',
  `contact_firstname` varchar(64) default NULL,
  `contact_aka` varchar(255) default NULL,
  `contact_sound` varchar(48) default NULL,
  `contact_service` varchar(64) default NULL,
  `contact_address1` varchar(64) default NULL,
  `contact_address2` varchar(64) default NULL,
  `contact_address3` varchar(64) default NULL,
  `contact_zipcode` varchar(14) default NULL,
  `contact_town` varchar(64) default NULL,
  `contact_expresspostal` varchar(16) default NULL,
  `contact_country_iso3166` char(2) default '0',
  `contact_function_id` int(8) default NULL,
  `contact_title` varchar(64) default NULL,
  `contact_phone` varchar(32) default NULL,
  `contact_homephone` varchar(32) default NULL,
  `contact_mobilephone` varchar(32) default NULL,
  `contact_fax` varchar(32) default NULL,
  `contact_email` varchar(128) default NULL,
  `contact_email2` varchar(128) default NULL,
  `contact_mailing_ok` char(1) default '0',
  `contact_newsletter` char(1) default '0',
  `contact_archive` char(1) default '0',
  `contact_privacy` int(2) NOT NULL default '0',
  `contact_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contact_comment` text,
  `contact_comment2` text,
  `contact_comment3` text,
  PRIMARY KEY  (`contact_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Contact`
--

LOCK TABLES `Contact` WRITE;
/*!40000 ALTER TABLE `Contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `Contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ContactFunction`
--

DROP TABLE IF EXISTS `ContactFunction`;
CREATE TABLE `ContactFunction` (
  `contactfunction_id` int(8) NOT NULL auto_increment,
  `contactfunction_domain_id` int(8) default '0',
  `contactfunction_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `contactfunction_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contactfunction_userupdate` int(8) default NULL,
  `contactfunction_usercreate` int(8) default NULL,
  `contactfunction_code` varchar(10) default '',
  `contactfunction_label` varchar(64) default NULL,
  PRIMARY KEY  (`contactfunction_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ContactFunction`
--

LOCK TABLES `ContactFunction` WRITE;
/*!40000 ALTER TABLE `ContactFunction` DISABLE KEYS */;
/*!40000 ALTER TABLE `ContactFunction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ContactList`
--

DROP TABLE IF EXISTS `ContactList`;
CREATE TABLE `ContactList` (
  `contactlist_list_id` int(8) NOT NULL default '0',
  `contactlist_contact_id` int(8) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ContactList`
--

LOCK TABLES `ContactList` WRITE;
/*!40000 ALTER TABLE `ContactList` DISABLE KEYS */;
/*!40000 ALTER TABLE `ContactList` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Contract`
--

DROP TABLE IF EXISTS `Contract`;
CREATE TABLE `Contract` (
  `contract_id` int(8) NOT NULL auto_increment,
  `contract_domain_id` int(8) default '0',
  `contract_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `contract_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contract_userupdate` int(8) default NULL,
  `contract_usercreate` int(8) default NULL,
  `contract_deal_id` int(8) default NULL,
  `contract_company_id` int(8) default NULL,
  `contract_label` varchar(128) default NULL,
  `contract_number` varchar(20) default NULL,
  `contract_datesignature` date default NULL,
  `contract_datebegin` date default NULL,
  `contract_dateexp` date default NULL,
  `contract_daterenew` date default NULL,
  `contract_datecancel` date default NULL,
  `contract_type_id` int(8) default NULL,
  `contract_priority_id` int(8) NOT NULL default '0',
  `contract_status_id` int(8) NOT NULL default '0',
  `contract_kind` int(2) default '0',
  `contract_format` int(2) default '0',
  `contract_ticketnumber` int(8) default '0',
  `contract_duration` int(8) default '0',
  `contract_autorenewal` int(2) default '0',
  `contract_contact1_id` int(8) default NULL,
  `contract_contact2_id` int(8) default NULL,
  `contract_techmanager_id` int(8) default NULL,
  `contract_marketmanager_id` int(8) default NULL,
  `contract_privacy` int(2) default '0',
  `contract_archive` int(1) default '0',
  `contract_clause` text,
  `contract_comment` text,
  PRIMARY KEY  (`contract_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Contract`
--

LOCK TABLES `Contract` WRITE;
/*!40000 ALTER TABLE `Contract` DISABLE KEYS */;
/*!40000 ALTER TABLE `Contract` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ContractPriority`
--

DROP TABLE IF EXISTS `ContractPriority`;
CREATE TABLE `ContractPriority` (
  `contractpriority_id` int(8) NOT NULL auto_increment,
  `contractpriority_domain_id` int(8) default '0',
  `contractpriority_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `contractpriority_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contractpriority_userupdate` int(8) default NULL,
  `contractpriority_usercreate` int(8) default NULL,
  `contractpriority_code` varchar(10) default '',
  `contractpriority_color` varchar(6) default NULL,
  `contractpriority_label` varchar(64) default NULL,
  PRIMARY KEY  (`contractpriority_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ContractPriority`
--

LOCK TABLES `ContractPriority` WRITE;
/*!40000 ALTER TABLE `ContractPriority` DISABLE KEYS */;
INSERT INTO `ContractPriority` VALUES (1,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'1','FF0000','Haute'),(2,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'2','FFA0A0','Normale'),(3,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'3','FFF0F0','Faible');
/*!40000 ALTER TABLE `ContractPriority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ContractStatus`
--

DROP TABLE IF EXISTS `ContractStatus`;
CREATE TABLE `ContractStatus` (
  `contractstatus_id` int(8) NOT NULL auto_increment,
  `contractstatus_domain_id` int(8) default '0',
  `contractstatus_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `contractstatus_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contractstatus_userupdate` int(8) default NULL,
  `contractstatus_usercreate` int(8) default NULL,
  `contractstatus_code` varchar(10) default '',
  `contractstatus_label` varchar(64) default NULL,
  PRIMARY KEY  (`contractstatus_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ContractStatus`
--

LOCK TABLES `ContractStatus` WRITE;
/*!40000 ALTER TABLE `ContractStatus` DISABLE KEYS */;
INSERT INTO `ContractStatus` VALUES (1,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'1','En cours'),(2,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'2','Clos');
/*!40000 ALTER TABLE `ContractStatus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ContractType`
--

DROP TABLE IF EXISTS `ContractType`;
CREATE TABLE `ContractType` (
  `contracttype_id` int(8) NOT NULL auto_increment,
  `contracttype_domain_id` int(8) default '0',
  `contracttype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `contracttype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `contracttype_userupdate` int(8) default NULL,
  `contracttype_usercreate` int(8) default NULL,
  `contracttype_code` varchar(10) default '',
  `contracttype_label` varchar(64) default NULL,
  PRIMARY KEY  (`contracttype_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ContractType`
--

LOCK TABLES `ContractType` WRITE;
/*!40000 ALTER TABLE `ContractType` DISABLE KEYS */;
/*!40000 ALTER TABLE `ContractType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Country`
--

DROP TABLE IF EXISTS `Country`;
CREATE TABLE `Country` (
  `country_domain_id` int(8) default '0',
  `country_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `country_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `country_userupdate` int(8) default NULL,
  `country_usercreate` int(8) default NULL,
  `country_iso3166` char(2) NOT NULL,
  `country_name` varchar(64) default NULL,
  `country_lang` char(2) NOT NULL,
  `country_phone` varchar(4) default NULL,
  PRIMARY KEY  (`country_iso3166`,`country_lang`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Country`
--

LOCK TABLES `Country` WRITE;
/*!40000 ALTER TABLE `Country` DISABLE KEYS */;
INSERT INTO `Country` VALUES (1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AE','Emirats Arabes Unis','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AL','Albanie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AO','Angola','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SA','Arabie Saoudite','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AM','Arménie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AU','Australie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AZ','Azerbaidjan','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BE','Belgique','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BD','Bangladesh','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BB','La Barbade','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BJ','Benin','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BG','Bulgarie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BO','Bolivie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BR','Brésil','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BS','Bahamas','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BF','Burkina Faso','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BY','Bielorussie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CM','Cameroun','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CA','Canada','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CH','Suisse','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CN','Chine','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CO','Colombie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'KP','Corée du Nord','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CR','Costa Rica','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CZ','Rep.Tchèque','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CU','Cuba','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CY','Chypre','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'DE','Allemagne','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'DK','Danemark','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'DZ','Algérie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'ES','Espagne','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'EE','Estonie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'EC','Equateur','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'EG','Egypte','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LI','Liechtenstein','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GA','Gabon','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GB','Royaume Uni','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GI','Gibraltar','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GT','Guatemala','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GE','Georgie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GH','Ghana','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GL','Groenland','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GR','Grèce','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'GN','Guinée','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'HU','Hongrie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'HK','Hong Kong','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'JO','Jordanie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'HR','Croatie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IT','Italie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IL','Israel','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IN','Inde','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IR','Iran','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IE','Irlande','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IS','Islande','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'JP','Japon','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'JM','Jamaique','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'KZ','Kazakhstan','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'KE','Kenya','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'KW','Koweit','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LU','Luxembourg','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LY','Libye','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LB','Liban','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LK','Sri Lanka','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LV','Lettonie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MT','Malte','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MA','Maroc','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MY','Malaisie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MC','Monaco','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MD','Moldova','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MX','Mexique','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MU','Mauritius','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MW','Malawi','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NO','Norvège','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NP','Népal','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NI','Nicaragua','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NE','Nigeria','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NL','Pays Bas','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NZ','Nouvelle Zélande','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'OM','Oman','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PT','Portugal','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PE','Pérou','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PH','Phillipines','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PK','Pakistan','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PL','Pologne','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PF','Polynésie Française','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PR','Porto Rico','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'PY','Paraguay','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AR','Argentine','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'TW','Taiwan','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CF','Rép. Centraficaine','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CL','Chili','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CI','Rep. Côte D\'ivoire','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'KR','Corée du Sud','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'ID','Indonésie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MG','Madagascar','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'ML','Mali','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'RO','Roumanie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'UY','Uruguay','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'RU','Russie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SE','Suède','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SM','San Marino','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'FI','Finlande','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SG','Singapour','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SI','Slovenie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SK','Slovaquie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SN','Sénégal','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'NA','Namibie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'SY','Syrie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'TH','Thailande','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'TG','Togo','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'TR','Turquie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'TT','Trinité & Tobago','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'TN','Tunisie','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'UA','Ukraine','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'US','USA','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'VA','Saint-Siège','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'VN','Vietnam','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AT','Autriche','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'VE','Vénézuela','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'ZA','Afriq. Sud','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'ZW','Zimbabwe','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'BA','Bosnie-Herzégovine ','FR','+387'),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'AD','Andorre','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CS','Serbie-Monténégro','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'CG','Congo ','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'LT','Lituanie ','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'FR','France ','FR','+33'),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'FR','France ','EN','+33'),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'IQ','Irak','FR',''),(1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,0,'MK','« L\'ex République Yougoslave de Macedoine »','FR','');
/*!40000 ALTER TABLE `Country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DataSource`
--

DROP TABLE IF EXISTS `DataSource`;
CREATE TABLE `DataSource` (
  `datasource_id` int(8) NOT NULL auto_increment,
  `datasource_domain_id` int(8) default '0',
  `datasource_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `datasource_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `datasource_userupdate` int(8) default NULL,
  `datasource_usercreate` int(8) default NULL,
  `datasource_name` varchar(64) default NULL,
  PRIMARY KEY  (`datasource_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DataSource`
--

LOCK TABLES `DataSource` WRITE;
/*!40000 ALTER TABLE `DataSource` DISABLE KEYS */;
/*!40000 ALTER TABLE `DataSource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Deal`
--

DROP TABLE IF EXISTS `Deal`;
CREATE TABLE `Deal` (
  `deal_id` int(8) NOT NULL auto_increment,
  `deal_domain_id` int(8) default '0',
  `deal_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `deal_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `deal_userupdate` int(8) default NULL,
  `deal_usercreate` int(8) default NULL,
  `deal_number` varchar(32) default NULL,
  `deal_label` varchar(128) default NULL,
  `deal_datebegin` date default NULL,
  `deal_parentdeal_id` int(8) default NULL,
  `deal_type_id` int(8) default NULL,
  `deal_region_id` int(8) NOT NULL default '0',
  `deal_tasktype_id` int(8) default NULL,
  `deal_company_id` int(8) NOT NULL default '0',
  `deal_contact1_id` int(8) default NULL,
  `deal_contact2_id` int(8) default NULL,
  `deal_marketingmanager_id` int(8) default NULL,
  `deal_technicalmanager_id` int(8) default NULL,
  `deal_source_id` int(8) NOT NULL default '0',
  `deal_source` varchar(64) default NULL,
  `deal_dateproposal` date default NULL,
  `deal_dateexpected` date default NULL,
  `deal_datealarm` date default NULL,
  `deal_dateend` date default NULL,
  `deal_amount` decimal(12,2) default NULL,
  `deal_margin` decimal(12,2) default NULL,
  `deal_commission` decimal(5,2) default '0.00',
  `deal_hitrate` int(3) default '0',
  `deal_status_id` int(2) default NULL,
  `deal_archive` char(1) default '0',
  `deal_todo` varchar(128) default NULL,
  `deal_privacy` int(2) NOT NULL default '0',
  `deal_comment` text,
  PRIMARY KEY  (`deal_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Deal`
--

LOCK TABLES `Deal` WRITE;
/*!40000 ALTER TABLE `Deal` DISABLE KEYS */;
/*!40000 ALTER TABLE `Deal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DealCompany`
--

DROP TABLE IF EXISTS `DealCompany`;
CREATE TABLE `DealCompany` (
  `dealcompany_id` int(8) NOT NULL auto_increment,
  `dealcompany_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `dealcompany_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `dealcompany_userupdate` int(8) default NULL,
  `dealcompany_usercreate` int(8) default NULL,
  `dealcompany_deal_id` int(8) NOT NULL default '0',
  `dealcompany_company_id` int(8) NOT NULL default '0',
  `dealcompany_role_id` int(8) NOT NULL default '0',
  PRIMARY KEY  (`dealcompany_id`),
  KEY `dealcompany_idx_deal` (`dealcompany_deal_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DealCompany`
--

LOCK TABLES `DealCompany` WRITE;
/*!40000 ALTER TABLE `DealCompany` DISABLE KEYS */;
/*!40000 ALTER TABLE `DealCompany` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DealCompanyRole`
--

DROP TABLE IF EXISTS `DealCompanyRole`;
CREATE TABLE `DealCompanyRole` (
  `dealcompanyrole_id` int(8) NOT NULL auto_increment,
  `dealcompanyrole_domain_id` int(8) default '0',
  `dealcompanyrole_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `dealcompanyrole_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `dealcompanyrole_userupdate` int(8) default NULL,
  `dealcompanyrole_usercreate` int(8) default NULL,
  `dealcompanyrole_code` varchar(10) default '',
  `dealcompanyrole_label` varchar(64) NOT NULL default '',
  PRIMARY KEY  (`dealcompanyrole_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DealCompanyRole`
--

LOCK TABLES `DealCompanyRole` WRITE;
/*!40000 ALTER TABLE `DealCompanyRole` DISABLE KEYS */;
/*!40000 ALTER TABLE `DealCompanyRole` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DealStatus`
--

DROP TABLE IF EXISTS `DealStatus`;
CREATE TABLE `DealStatus` (
  `dealstatus_id` int(2) NOT NULL auto_increment,
  `dealstatus_domain_id` int(8) default '0',
  `dealstatus_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `dealstatus_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `dealstatus_userupdate` int(8) default NULL,
  `dealstatus_usercreate` int(8) default NULL,
  `dealstatus_label` varchar(24) default NULL,
  `dealstatus_order` int(2) default NULL,
  `dealstatus_hitrate` char(3) default NULL,
  PRIMARY KEY  (`dealstatus_id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DealStatus`
--

LOCK TABLES `DealStatus` WRITE;
/*!40000 ALTER TABLE `DealStatus` DISABLE KEYS */;
INSERT INTO `DealStatus` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'CONTACT',1,NULL),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'RDV',2,NULL),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'Attente de Proposition.',3,NULL),(4,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'PROPOSITION',4,NULL),(5,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'SIGNEE',5,'100'),(6,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'REALISEE',6,'100'),(7,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'FACTUREE',7,'100'),(8,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'PAYEE',8,'100'),(9,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'PERDUE',9,'0'),(10,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'SANS SUITE',10,'0');
/*!40000 ALTER TABLE `DealStatus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DealType`
--

DROP TABLE IF EXISTS `DealType`;
CREATE TABLE `DealType` (
  `dealtype_id` int(8) NOT NULL auto_increment,
  `dealtype_domain_id` int(8) default '0',
  `dealtype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `dealtype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `dealtype_userupdate` int(8) default NULL,
  `dealtype_usercreate` int(8) default NULL,
  `dealtype_label` varchar(16) default NULL,
  `dealtype_inout` varchar(1) default '-',
  PRIMARY KEY  (`dealtype_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DealType`
--

LOCK TABLES `DealType` WRITE;
/*!40000 ALTER TABLE `DealType` DISABLE KEYS */;
INSERT INTO `DealType` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'VENTE','+'),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'ACHAT','-'),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'MEDIA','-'),(4,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'SOCIAL','-'),(5,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'JURIDIQUE','-');
/*!40000 ALTER TABLE `DealType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DefaultOdtTemplate`
--

DROP TABLE IF EXISTS `DefaultOdtTemplate`;
CREATE TABLE `DefaultOdtTemplate` (
  `defaultodttemplate_id` int(8) NOT NULL auto_increment,
  `defaultodttemplate_domain_id` int(8) default '0',
  `defaultodttemplate_entity` varchar(32) default NULL,
  `defaultodttemplate_document_id` int(8) NOT NULL,
  `defaultodttemplate_label` varchar(64) default '',
  PRIMARY KEY  (`defaultodttemplate_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DefaultOdtTemplate`
--

LOCK TABLES `DefaultOdtTemplate` WRITE;
/*!40000 ALTER TABLE `DefaultOdtTemplate` DISABLE KEYS */;
/*!40000 ALTER TABLE `DefaultOdtTemplate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Deleted`
--

DROP TABLE IF EXISTS `Deleted`;
CREATE TABLE `Deleted` (
  `deleted_id` int(8) NOT NULL auto_increment,
  `deleted_domain_id` int(8) default NULL,
  `deleted_user_id` int(8) default NULL,
  `deleted_delegation` varchar(64) default '',
  `deleted_table` varchar(32) default NULL,
  `deleted_entity_id` int(8) default NULL,
  `deleted_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`deleted_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Deleted`
--

LOCK TABLES `Deleted` WRITE;
/*!40000 ALTER TABLE `Deleted` DISABLE KEYS */;
/*!40000 ALTER TABLE `Deleted` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DeletedCalendarEvent`
--

DROP TABLE IF EXISTS `DeletedCalendarEvent`;
CREATE TABLE `DeletedCalendarEvent` (
  `deletedcalendarevent_id` int(8) NOT NULL auto_increment,
  `deletedcalendarevent_event_id` int(8) default NULL,
  `deletedcalendarevent_user_id` int(8) default NULL,
  `deletedcalendarevent_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`deletedcalendarevent_id`),
  KEY `idx_dce_event` (`deletedcalendarevent_event_id`),
  KEY `idx_dce_user` (`deletedcalendarevent_user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DeletedCalendarEvent`
--

LOCK TABLES `DeletedCalendarEvent` WRITE;
/*!40000 ALTER TABLE `DeletedCalendarEvent` DISABLE KEYS */;
/*!40000 ALTER TABLE `DeletedCalendarEvent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DeletedContact`
--

DROP TABLE IF EXISTS `DeletedContact`;
CREATE TABLE `DeletedContact` (
  `deletedcontact_contact_id` int(8) NOT NULL default '0',
  `deletedcontact_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`deletedcontact_contact_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DeletedContact`
--

LOCK TABLES `DeletedContact` WRITE;
/*!40000 ALTER TABLE `DeletedContact` DISABLE KEYS */;
/*!40000 ALTER TABLE `DeletedContact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DeletedTodo`
--

DROP TABLE IF EXISTS `DeletedTodo`;
CREATE TABLE `DeletedTodo` (
  `deletedtodo_todo_id` int(8) NOT NULL default '0',
  `deletedtodo_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`deletedtodo_todo_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DeletedTodo`
--

LOCK TABLES `DeletedTodo` WRITE;
/*!40000 ALTER TABLE `DeletedTodo` DISABLE KEYS */;
/*!40000 ALTER TABLE `DeletedTodo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DeletedUser`
--

DROP TABLE IF EXISTS `DeletedUser`;
CREATE TABLE `DeletedUser` (
  `deleteduser_user_id` int(8) NOT NULL default '0',
  `deleteduser_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`deleteduser_user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DeletedUser`
--

LOCK TABLES `DeletedUser` WRITE;
/*!40000 ALTER TABLE `DeletedUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `DeletedUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DisplayPref`
--

DROP TABLE IF EXISTS `DisplayPref`;
CREATE TABLE `DisplayPref` (
  `display_user_id` int(8) NOT NULL default '0',
  `display_entity` varchar(32) NOT NULL default '',
  `display_fieldname` varchar(64) NOT NULL default '',
  `display_fieldorder` int(3) unsigned default NULL,
  `display_display` int(1) unsigned NOT NULL default '1',
  PRIMARY KEY  (`display_user_id`,`display_entity`,`display_fieldname`),
  KEY `idx_user` (`display_user_id`),
  KEY `idx_entity` (`display_entity`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DisplayPref`
--

LOCK TABLES `DisplayPref` WRITE;
/*!40000 ALTER TABLE `DisplayPref` DISABLE KEYS */;
INSERT INTO `DisplayPref` VALUES (0,'company','company_name',1,2),(0,'company','company_archive',2,1),(0,'company','company_contact_number',3,1),(0,'company','company_new_contact',4,1),(0,'company','company_deal_number',5,1),(0,'company','company_number',6,1),(0,'company','company_vat',7,1),(0,'company','company_siret',8,1),(0,'company','type_label',9,1),(0,'company','activity_label',10,1),(0,'company','nafcode_code',11,1),(0,'company','company_address',12,1),(0,'company','company_address1',13,0),(0,'company','company_address2',14,0),(0,'company','company_address3',15,0),(0,'company','company_zipcode',16,0),(0,'company','company_town',17,0),(0,'company','company_expresspostal',18,0),(0,'company','country_name',19,0),(0,'company','company_phone',20,1),(0,'company','company_fax',21,1),(0,'company','company_email',22,1),(0,'company','company_web',23,1),(0,'contact','contact_lastname',1,2),(0,'contact','contact_firstname',2,1),(0,'contact','contact_archive',3,1),(0,'contact','function_label',4,1),(0,'contact','contact_title',5,1),(0,'contact','kind_minilabel',6,1),(0,'contact','kind_header',7,1),(0,'contact','kind_lang',8,1),(0,'contact','company_name',9,2),(0,'contact','contact_address',10,1),(0,'contact','contact_service',11,0),(0,'contact','contact_address1',12,0),(0,'contact','contact_address2',13,0),(0,'contact','contact_address3',14,0),(0,'contact','contact_zipcode',15,0),(0,'contact','contact_town',16,0),(0,'contact','contact_expresspostal',17,0),(0,'contact','country_name',18,0),(0,'contact','contact_phone',19,1),(0,'contact','contact_homephone',20,1),(0,'contact','contact_mobilephone',21,1),(0,'contact','contact_fax',22,1),(0,'contact','contact_email',23,1),(0,'contact','contact_date',24,1),(0,'contact','contact_mailing_ok',25,1),(0,'contact','contact_newsletter',26,1),(0,'lead','lead_name',1,2),(0,'lead','company_name',2,2),(0,'lead','company_zipcode',3,2),(0,'lead','company_phone',4,1),(0,'lead','leadsource_label',5,1),(0,'lead','date',6,1),(0,'lead','manager',7,1),(0,'lead','datealarm',8,1),(0,'lead','lead_todo',9,1),(0,'deal','deal_label',1,2),(0,'deal','deal_number',2,1),(0,'deal','deal_company_name',3,2),(0,'deal','deal_company_zipcode',4,2),(0,'deal','dealtype_label',5,1),(0,'deal','region_label',6,0),(0,'deal','tasktype_label',7,1),(0,'deal','dealstatus_label',8,1),(0,'deal','deal_marketingmanager',9,1),(0,'deal','deal_source',10,0),(0,'deal','deal_commission',11,0),(0,'deal','deal_amount',12,1),(0,'deal','deal_margin',13,1),(0,'deal','deal_hitrate',14,1),(0,'deal','deal_datebegin',15,0),(0,'deal','deal_dateproposal',16,0),(0,'deal','deal_dateexpected',17,1),(0,'deal','expected_quarter',18,0),(0,'deal','sale_delay',19,0),(0,'deal','deal_dateend',20,0),(0,'deal','end_quarter',21,0),(0,'deal','deal_todo',22,1),(0,'deal','deal_archive',23,1),(0,'deal','deal_datealarm',24,2),(0,'deal','deal_relation',25,0),(0,'parentdeal','parentdeal_label',1,2),(0,'parentdeal','parentdeal_marketing_lastname',2,1),(0,'parentdeal','parentdeal_technical_lastname',3,1),(0,'list','list_name',1,2),(0,'list','list_subject',2,1),(0,'list','list_nb_contact',3,2),(0,'list','list_info_publication',4,1),(0,'list','list_mode',5,1),(0,'list','usercreate',6,1),(0,'list','timecreate',7,1),(0,'list','userupdate',8,1),(0,'list','timeupdate',9,1),(0,'list_contact','contact_lastname',1,2),(0,'list_contact','contact_firstname',2,1),(0,'list_contact','function_label',3,1),(0,'list_contact','contact_title',4,1),(0,'list_contact','kind_minilabel',5,1),(0,'list_contact','kind_header',6,1),(0,'list_contact','kind_lang',7,1),(0,'list_contact','company_name',8,2),(0,'list_contact','address',9,1),(0,'list_contact','service',10,0),(0,'list_contact','address1',11,0),(0,'list_contact','address2',12,0),(0,'list_contact','address3',13,0),(0,'list_contact','zipcode',14,1),(0,'list_contact','town',15,1),(0,'list_contact','expresspostal',16,1),(0,'list_contact','country_name',17,1),(0,'list_contact','contact_phone',18,1),(0,'list_contact','contact_homephone',19,1),(0,'list_contact','contact_mobilephone',20,1),(0,'list_contact','contact_fax',21,1),(0,'list_contact','publication_lang',22,1),(0,'list_contact','publication_title',23,1),(0,'list_contact','subscription_quantity',24,1),(0,'list_contact','contact_email',25,1),(0,'todo','todo_title',1,2),(0,'todo','todo_priority',2,1),(0,'todo','todo_percent',3,1),(0,'todo','date_todo',4,1),(0,'todo','date_deadline',5,1),(0,'todo','todo_update',6,2),(0,'contract','contract_label',1,1),(0,'contract','contract_number',2,1),(0,'contract','contract_company_name',3,2),(0,'contract','contracttype_label',4,1),(0,'contract','contract_priority',5,1),(0,'contract','contract_kind',6,1),(0,'contract','contract_format',7,1),(0,'contract','contract_datebegin',8,1),(0,'contract','contract_dateexp',9,1),(0,'contract','contract_techmanager',10,1),(0,'contract','contract_marketmanager',11,1),(0,'contract','contract_archive',12,1),(0,'incident','incident_label',1,2),(0,'incident','incident_reference',2,2),(0,'incident','incident_company_name',3,2),(0,'incident','contract_label',4,0),(0,'incident','incident_logger_lastname',5,1),(0,'incident','incident_owner_lastname',6,1),(0,'incident','incident_priority',7,1),(0,'incident','incident_status',8,2),(0,'incident','resolutiontype_label',9,1),(0,'incident','incident_date',10,1),(0,'incident','incident_duration',11,1),(0,'incident','timeupdate',12,1),(0,'incident','incident_archive',13,1),(0,'account','account_label',1,2),(0,'account','account_bank',2,1),(0,'account','account_number',3,1),(0,'account','account_today',4,1),(0,'account','account_balance',5,1),(0,'invoice','invoice_label',1,2),(0,'invoice','invoice_archive',2,2),(0,'invoice','invoice_number',3,1),(0,'invoice','invoice_date',4,1),(0,'invoice','invoice_expiration_date',5,1),(0,'invoice','invoice_payment_date',6,1),(0,'invoice','invoice_company',7,2),(0,'invoice','invoice_inout',8,1),(0,'invoice','invoice_credit_memo',9,1),(0,'invoice','invoice_amount_ht',10,1),(0,'invoice','invoice_amount_ttc',11,1),(0,'invoice','invoice_status',12,1),(0,'invoice','invoice_deal',13,1),(0,'invoice','invoice_project',14,1),(0,'payment','payment_date',1,2),(0,'payment','company_name',2,2),(0,'payment','payment_amount',3,2),(0,'payment','payment_number',4,1),(0,'payment','paymentkind_label',5,1),(0,'payment','payment_comment',6,1),(0,'time','date_task',1,1),(0,'time','timetask_company_name',2,1),(0,'time','tasktype_label',3,1),(0,'time','timetask_project_name',4,1),(0,'time','projecttask_label',5,1),(0,'time','timetask_label',6,1),(0,'time','timetask_length',7,2),(0,'time','timetask_id',8,1),(0,'time_proj','project_name',1,2),(0,'time_proj','company_name',2,2),(0,'time_proj','total_length',3,1),(0,'time_proj','total_before',4,1),(0,'time_proj','total_after',5,1),(0,'time_tt','tasktype_label',1,2),(0,'time_tt','total_length',2,1),(0,'time_tt','total_before',3,1),(0,'time_tt','total_after',4,1),(0,'project','project_name',1,2),(0,'project','project_company',2,1),(0,'project','project_tasktype',3,1),(0,'project','project_soldtime',4,1),(0,'project','project_estimatedtime',5,1),(0,'project','project_datebegin',6,1),(0,'project','project_dateend',7,1),(0,'project','project_archive',8,1),(0,'document','document_title',1,2),(0,'document','document_name',2,1),(0,'document','document_size',3,1),(0,'document','document_author',4,1),(0,'document','timecreate',5,1),(0,'document','timeupdate',6,1),(0,'document','documentmimetype_label',9,1),(0,'user','userobm_login',1,2),(0,'user','userobm_archive',2,2),(0,'user','userobm_system',3,0),(0,'user','domain_label',4,1),(0,'user','userobm_delegation',5,1),(0,'user','userobm_local',6,1),(0,'user','userobm_ext_id',7,1),(0,'user','userobm_lastname',8,1),(0,'user','userobm_firstname',9,1),(0,'user','userobm_title',10,1),(0,'user','userobm_perms',11,1),(0,'user','datebegin',12,1),(0,'user','timelastaccess',13,1),(0,'user','userobm_phone',14,1),(0,'user','userobm_phone2',15,1),(0,'user','userobm_mobile',16,1),(0,'user','userobm_fax',17,1),(0,'user','userobm_fax2',18,1),(0,'user','userobm_email',19,1),(0,'user','userobm_mail_quota',20,1),(0,'user','userobm_mail_login_date',21,1),(0,'user','userobm_nomade',22,1),(0,'user','userobm_web_perms',23,1),(0,'user','userobm_samba_perms',24,1),(0,'user','userobm_description',25,1),(0,'group','group_name',1,2),(0,'group','group_delegation',2,1),(0,'group','group_gid',3,0),(0,'group','group_system',4,2),(0,'group','group_privacy',5,1),(0,'group','group_local',6,1),(0,'group','group_ext_id',7,1),(0,'group','group_samba',8,1),(0,'group','group_desc',9,1),(0,'group','group_email',10,1),(0,'group','group_mailing',11,1),(0,'group','group_nb_user',12,2),(0,'group','usercreate',13,1),(0,'group','timecreate',14,1),(0,'group','userupdate',15,1),(0,'group','timeupdate',16,1),(0,'group_user','group_user_login',1,2),(0,'group_user','group_user_lastname',2,1),(0,'group_user','group_user_firstname',3,1),(0,'group_user','group_user_phone',4,1),(0,'group_user','group_user_email',5,1),(0,'group_group','group_name',1,2),(0,'group_group','group_desc',2,1),(0,'group_group','group_email',3,1),(0,'host','host_name',1,2),(0,'host','host_delegation',2,1),(0,'host','host_ip',3,1),(0,'host','host_samba',4,1),(0,'host','host_description',5,1),(0,'host','usercreate',6,1),(0,'host','timecreate',7,1),(0,'host','userupdate',8,1),(0,'host','timeupdate',9,1),(0,'mailshare','mailshare_name',1,2),(0,'mailshare','mailshare_delegation',2,1),(0,'mailshare','mailshare_quota',3,1),(0,'mailshare','mailshare_email',4,1),(0,'mailshare','mailshare_description',5,1),(0,'mailshare','usercreate',6,1),(0,'mailshare','timecreate',7,1),(0,'mailshare','userupdate',8,1),(0,'mailshare','timeupdate',9,1),(0,'import','import_name',1,2),(0,'import','import_datasource',2,2),(0,'import','import_market',3,1),(0,'import','import_separator',4,1),(0,'import','import_enclosed',5,1),(0,'publication','publication_title',1,2),(0,'publication','publicationtype_label',2,2),(0,'publication','publication_year',3,1),(0,'publication','publication_lang',4,1),(0,'resource','resource_name',1,2),(0,'resource','resource_description',2,1),(0,'resource','resource_qty',3,1),(0,'resource','resourcetype_label',4,1),(0,'resourcegroup','rgroup_name',1,2),(0,'resourcegroup','rgroup_privacy',2,2),(0,'resourcegroup','rgroup_desc',3,1),(0,'resourcegroup','rgroup_nb_resource',4,2),(0,'resourcegroup','usercreate',5,1),(0,'resourcegroup','timecreate',6,1),(0,'resourcegroup','userupdate',7,1),(0,'resourcegroup','timeupdate',8,1),(0,'resourcegroup_resource','resourcegroup_resource_name',1,2),(0,'resourcegroup_resource','resourcegroup_resource_desc',2,1),(0,'resourcegroup_resource','resourcegroup_resource_qty',3,1),(0,'domain','domain_label',1,2),(0,'domain','domain_description',2,1),(0,'domain','domain_name',3,1),(0,'domain','domain_alias',4,1),(0,'mailserver','host_name',1,2),(0,'mailserver','host_ip',2,1),(0,'mailserver','mailserver_imap',3,1),(0,'mailserver','mailserver_smtp_in',4,1),(0,'mailserver','mailserver_smtp_out',5,1),(0,'cv','cv_title',1,1),(0,'cv','lastname',2,2),(0,'cv','firstname',3,2),(0,'cv','timeupdate',4,1),(0,'cv','timecreate',5,1),(0,'cv_reference','project_name',1,1),(0,'cv_reference','projectcv_role',2,2),(0,'cv_reference','project_reference_date',3,2),(0,'cv_reference','project_reference_duration',4,2),(0,'cv_reference','project_reference_desc',5,2),(0,'cv_reference','project_reference_tech',6,2),(0,'organizationalchart','organizationalchart_name',1,2),(0,'organizationalchart','organizationalchart_description',2,2),(0,'organizationalchart','organizationalchart_archive',3,2);
/*!40000 ALTER TABLE `DisplayPref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Document`
--

DROP TABLE IF EXISTS `Document`;
CREATE TABLE `Document` (
  `document_id` int(8) NOT NULL auto_increment,
  `document_domain_id` int(8) default '0',
  `document_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `document_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `document_userupdate` int(8) default NULL,
  `document_usercreate` int(8) default NULL,
  `document_title` varchar(255) default NULL,
  `document_name` varchar(255) default NULL,
  `document_kind` int(2) default NULL,
  `document_mimetype_id` int(8) NOT NULL default '0',
  `document_privacy` int(2) NOT NULL default '0',
  `document_size` int(15) default NULL,
  `document_author` varchar(255) default NULL,
  `document_path` text,
  `document_acl` text,
  PRIMARY KEY  (`document_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Document`
--

LOCK TABLES `Document` WRITE;
/*!40000 ALTER TABLE `Document` DISABLE KEYS */;
/*!40000 ALTER TABLE `Document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DocumentEntity`
--

DROP TABLE IF EXISTS `DocumentEntity`;
CREATE TABLE `DocumentEntity` (
  `documententity_document_id` int(8) NOT NULL,
  `documententity_entity_id` int(8) NOT NULL,
  `documententity_entity` varchar(255) NOT NULL,
  PRIMARY KEY  (`documententity_document_id`,`documententity_entity_id`,`documententity_entity`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DocumentEntity`
--

LOCK TABLES `DocumentEntity` WRITE;
/*!40000 ALTER TABLE `DocumentEntity` DISABLE KEYS */;
/*!40000 ALTER TABLE `DocumentEntity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DocumentMimeType`
--

DROP TABLE IF EXISTS `DocumentMimeType`;
CREATE TABLE `DocumentMimeType` (
  `documentmimetype_id` int(8) NOT NULL auto_increment,
  `documentmimetype_domain_id` int(8) default '0',
  `documentmimetype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `documentmimetype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `documentmimetype_userupdate` int(8) default NULL,
  `documentmimetype_usercreate` int(8) default NULL,
  `documentmimetype_label` varchar(255) default NULL,
  `documentmimetype_extension` varchar(10) default NULL,
  `documentmimetype_mime` varchar(255) default NULL,
  PRIMARY KEY  (`documentmimetype_id`)
) ENGINE=MyISAM AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DocumentMimeType`
--

LOCK TABLES `DocumentMimeType` WRITE;
/*!40000 ALTER TABLE `DocumentMimeType` DISABLE KEYS */;
INSERT INTO `DocumentMimeType` VALUES (1,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier Html','html','text/html'),(2,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Image PNG','png','image/png'),(3,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Image Gif','gif','image/gif'),(4,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Image JPG','jpg','image/jpg'),(5,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier PDF','pdf','application/pdf'),(6,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier Excel','xls','application/vnd.ms-excel'),(7,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier Texte','txt','text/plain'),(8,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier Word','doc','application/msword'),(9,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier Binaire','exe','application/octet-stream'),(10,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier PowerPoint','ppt','application/vnd.ms-powerpoint'),(11,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,'Fichier CSV','csv','text/x-csv');
/*!40000 ALTER TABLE `DocumentMimeType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Domain`
--

DROP TABLE IF EXISTS `Domain`;
CREATE TABLE `Domain` (
  `domain_id` int(8) NOT NULL auto_increment,
  `domain_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `domain_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `domain_usercreate` int(8) default NULL,
  `domain_userupdate` int(8) default NULL,
  `domain_label` varchar(32) NOT NULL,
  `domain_description` varchar(255) default NULL,
  `domain_name` varchar(128) default NULL,
  `domain_alias` text,
  `domain_mail_server_id` int(8) default NULL,
  PRIMARY KEY  (`domain_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Domain`
--

LOCK TABLES `Domain` WRITE;
/*!40000 ALTER TABLE `Domain` DISABLE KEYS */;
/*!40000 ALTER TABLE `Domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DomainMailServer`
--

DROP TABLE IF EXISTS `DomainMailServer`;
CREATE TABLE `DomainMailServer` (
  `domainmailserver_domain_id` int(8) NOT NULL default '0',
  `domainmailserver_mailserver_id` int(8) NOT NULL,
  `domainmailserver_role` varchar(16) NOT NULL default 'imap'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DomainMailServer`
--

LOCK TABLES `DomainMailServer` WRITE;
/*!40000 ALTER TABLE `DomainMailServer` DISABLE KEYS */;
/*!40000 ALTER TABLE `DomainMailServer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DomainProperty`
--

DROP TABLE IF EXISTS `DomainProperty`;
CREATE TABLE `DomainProperty` (
  `domainproperty_key` varchar(255) NOT NULL,
  `domainproperty_type` varchar(32) default NULL,
  `domainproperty_default` varchar(64) default NULL,
  `domainproperty_readonly` int(1) default '0',
  PRIMARY KEY  (`domainproperty_key`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DomainProperty`
--

LOCK TABLES `DomainProperty` WRITE;
/*!40000 ALTER TABLE `DomainProperty` DISABLE KEYS */;
INSERT INTO `DomainProperty` VALUES ('update_state','integer','1',1),('max_user','integer','0',0),('max_mailshare','integer','0',0),('max_resource','integer','0',0),('mail_quota','integer','0',0),('delegation','text','',0),('address1','text','',0),('address2','text','',0),('address3','text','',0),('postcode','text','',0),('town','text','',0);
/*!40000 ALTER TABLE `DomainProperty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DomainPropertyValue`
--

DROP TABLE IF EXISTS `DomainPropertyValue`;
CREATE TABLE `DomainPropertyValue` (
  `domainpropertyvalue_domain_id` int(8) NOT NULL,
  `domainpropertyvalue_property_key` varchar(255) NOT NULL,
  `domainpropertyvalue_value` varchar(255) NOT NULL,
  PRIMARY KEY  (`domainpropertyvalue_domain_id`,`domainpropertyvalue_property_key`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `DomainPropertyValue`
--

LOCK TABLES `DomainPropertyValue` WRITE;
/*!40000 ALTER TABLE `DomainPropertyValue` DISABLE KEYS */;
INSERT INTO `DomainPropertyValue` VALUES (0,'update_state','1');
/*!40000 ALTER TABLE `DomainPropertyValue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EntityRight`
--

DROP TABLE IF EXISTS `EntityRight`;
CREATE TABLE `EntityRight` (
  `entityright_entity` varchar(32) NOT NULL default '',
  `entityright_entity_id` int(8) NOT NULL default '0',
  `entityright_consumer` varchar(32) NOT NULL default '',
  `entityright_consumer_id` int(8) NOT NULL default '0',
  `entityright_read` int(1) NOT NULL default '0',
  `entityright_write` int(1) NOT NULL default '0',
  `entityright_admin` int(1) NOT NULL default '0',
  PRIMARY KEY  (`entityright_entity`,`entityright_entity_id`,`entityright_consumer`,`entityright_consumer_id`),
  KEY `entright_idx_ent_id` (`entityright_entity_id`),
  KEY `entright_idx_ent` (`entityright_entity`),
  KEY `entright_idx_con_id` (`entityright_consumer_id`),
  KEY `entright_idx_con` (`entityright_consumer`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `EntityRight`
--

LOCK TABLES `EntityRight` WRITE;
/*!40000 ALTER TABLE `EntityRight` DISABLE KEYS */;
/*!40000 ALTER TABLE `EntityRight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EventEntity`
--

DROP TABLE IF EXISTS `EventEntity`;
CREATE TABLE `EventEntity` (
  `evententity_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `evententity_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `evententity_userupdate` int(8) default NULL,
  `evententity_usercreate` int(8) default NULL,
  `evententity_event_id` int(8) NOT NULL default '0',
  `evententity_entity_id` int(8) NOT NULL default '0',
  `evententity_entity` varchar(32) NOT NULL default '0',
  `evententity_state` char(1) NOT NULL default '0',
  `evententity_required` int(1) NOT NULL default '0',
  PRIMARY KEY  (`evententity_event_id`,`evententity_entity_id`,`evententity_entity`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `EventEntity`
--

LOCK TABLES `EventEntity` WRITE;
/*!40000 ALTER TABLE `EventEntity` DISABLE KEYS */;
/*!40000 ALTER TABLE `EventEntity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `GroupGroup`
--

DROP TABLE IF EXISTS `GroupGroup`;
CREATE TABLE `GroupGroup` (
  `groupgroup_parent_id` int(8) NOT NULL default '0',
  `groupgroup_child_id` int(8) NOT NULL default '0',
  PRIMARY KEY  (`groupgroup_parent_id`,`groupgroup_child_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `GroupGroup`
--

LOCK TABLES `GroupGroup` WRITE;
/*!40000 ALTER TABLE `GroupGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `GroupGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Host`
--

DROP TABLE IF EXISTS `Host`;
CREATE TABLE `Host` (
  `host_id` int(8) NOT NULL auto_increment,
  `host_domain_id` int(8) default '0',
  `host_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `host_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `host_userupdate` int(8) default NULL,
  `host_usercreate` int(8) default NULL,
  `host_uid` int(8) default NULL,
  `host_gid` int(8) default NULL,
  `host_samba` int(1) default '0',
  `host_name` varchar(32) NOT NULL,
  `host_ip` varchar(16) default NULL,
  `host_delegation` varchar(64) default '',
  `host_description` varchar(128) default NULL,
  `host_web_perms` int(1) default '0',
  `host_web_list` text,
  `host_web_all` int(1) default '0',
  `host_ftp_perms` int(1) default '0',
  `host_firewall_perms` varchar(128) default NULL,
  PRIMARY KEY  (`host_id`),
  UNIQUE KEY `host_name` (`host_name`),
  UNIQUE KEY `k_uid_host` (`host_uid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Host`
--

LOCK TABLES `Host` WRITE;
/*!40000 ALTER TABLE `Host` DISABLE KEYS */;
/*!40000 ALTER TABLE `Host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Import`
--

DROP TABLE IF EXISTS `Import`;
CREATE TABLE `Import` (
  `import_id` int(8) NOT NULL auto_increment,
  `import_domain_id` int(8) default '0',
  `import_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `import_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `import_userupdate` int(8) default NULL,
  `import_usercreate` int(8) default NULL,
  `import_name` varchar(64) NOT NULL,
  `import_datasource_id` int(8) default '0',
  `import_marketingmanager_id` int(8) default NULL,
  `import_separator` varchar(3) default NULL,
  `import_enclosed` char(1) default NULL,
  `import_desc` text,
  PRIMARY KEY  (`import_id`),
  UNIQUE KEY `import_name` (`import_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Import`
--

LOCK TABLES `Import` WRITE;
/*!40000 ALTER TABLE `Import` DISABLE KEYS */;
/*!40000 ALTER TABLE `Import` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Incident`
--

DROP TABLE IF EXISTS `Incident`;
CREATE TABLE `Incident` (
  `incident_id` int(8) NOT NULL auto_increment,
  `incident_domain_id` int(8) default '0',
  `incident_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `incident_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `incident_userupdate` int(8) default NULL,
  `incident_usercreate` int(8) default NULL,
  `incident_contract_id` int(8) NOT NULL,
  `incident_label` varchar(100) default NULL,
  `incident_reference` varchar(32) default NULL,
  `incident_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `incident_priority_id` int(8) default '0',
  `incident_status_id` int(8) default '0',
  `incident_resolutiontype_id` int(11) default '0',
  `incident_logger` int(8) default NULL,
  `incident_owner` int(8) default NULL,
  `incident_duration` char(4) default '0',
  `incident_archive` char(1) NOT NULL default '0',
  `incident_comment` text,
  `incident_resolution` text,
  PRIMARY KEY  (`incident_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Incident`
--

LOCK TABLES `Incident` WRITE;
/*!40000 ALTER TABLE `Incident` DISABLE KEYS */;
/*!40000 ALTER TABLE `Incident` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IncidentPriority`
--

DROP TABLE IF EXISTS `IncidentPriority`;
CREATE TABLE `IncidentPriority` (
  `incidentpriority_id` int(8) NOT NULL auto_increment,
  `incidentpriority_domain_id` int(8) default '0',
  `incidentpriority_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `incidentpriority_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `incidentpriority_userupdate` int(8) default NULL,
  `incidentpriority_usercreate` int(8) default NULL,
  `incidentpriority_code` varchar(10) default '',
  `incidentpriority_label` varchar(32) default NULL,
  `incidentpriority_color` char(6) default NULL,
  PRIMARY KEY  (`incidentpriority_id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `IncidentPriority`
--

LOCK TABLES `IncidentPriority` WRITE;
/*!40000 ALTER TABLE `IncidentPriority` DISABLE KEYS */;
INSERT INTO `IncidentPriority` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'1','Urgente','FF0000'),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'2','Forte','EE9D00'),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'3','Normale','550000'),(4,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'4','Basse','000000');
/*!40000 ALTER TABLE `IncidentPriority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IncidentResolutionType`
--

DROP TABLE IF EXISTS `IncidentResolutionType`;
CREATE TABLE `IncidentResolutionType` (
  `incidentresolutiontype_id` int(8) NOT NULL auto_increment,
  `incidentresolutiontype_domain_id` int(8) default '0',
  `incidentresolutiontype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `incidentresolutiontype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `incidentresolutiontype_userupdate` int(8) default NULL,
  `incidentresolutiontype_usercreate` int(8) default NULL,
  `incidentresolutiontype_code` varchar(10) default '',
  `incidentresolutiontype_label` varchar(32) default NULL,
  PRIMARY KEY  (`incidentresolutiontype_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `IncidentResolutionType`
--

LOCK TABLES `IncidentResolutionType` WRITE;
/*!40000 ALTER TABLE `IncidentResolutionType` DISABLE KEYS */;
/*!40000 ALTER TABLE `IncidentResolutionType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IncidentStatus`
--

DROP TABLE IF EXISTS `IncidentStatus`;
CREATE TABLE `IncidentStatus` (
  `incidentstatus_id` int(8) NOT NULL auto_increment,
  `incidentstatus_domain_id` int(8) default '0',
  `incidentstatus_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `incidentstatus_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `incidentstatus_userupdate` int(8) default NULL,
  `incidentstatus_usercreate` int(8) default NULL,
  `incidentstatus_code` varchar(10) default '',
  `incidentstatus_label` varchar(32) default NULL,
  PRIMARY KEY  (`incidentstatus_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `IncidentStatus`
--

LOCK TABLES `IncidentStatus` WRITE;
/*!40000 ALTER TABLE `IncidentStatus` DISABLE KEYS */;
INSERT INTO `IncidentStatus` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'1','Ouvert'),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'2','Appel'),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'3','Attente Appel'),(4,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'4','En Pause'),(5,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',NULL,1,'5','Cloturé');
/*!40000 ALTER TABLE `IncidentStatus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Invoice`
--

DROP TABLE IF EXISTS `Invoice`;
CREATE TABLE `Invoice` (
  `invoice_id` int(8) NOT NULL auto_increment,
  `invoice_domain_id` int(8) default '0',
  `invoice_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `invoice_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `invoice_userupdate` int(8) default NULL,
  `invoice_usercreate` int(8) default NULL,
  `invoice_company_id` int(8) NOT NULL,
  `invoice_deal_id` int(8) default NULL,
  `invoice_project_id` int(8) default NULL,
  `invoice_number` varchar(10) default '0',
  `invoice_label` varchar(40) NOT NULL default '',
  `invoice_amount_ht` double(10,2) default NULL,
  `invoice_amount_ttc` double(10,2) default NULL,
  `invoice_status_id` int(4) NOT NULL default '0',
  `invoice_date` date NOT NULL default '0000-00-00',
  `invoice_expiration_date` date default NULL,
  `invoice_payment_date` date default NULL,
  `invoice_inout` char(1) default NULL,
  `invoice_credit_memo` int(1) NOT NULL default '0',
  `invoice_archive` char(1) NOT NULL default '0',
  `invoice_comment` text,
  PRIMARY KEY  (`invoice_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Invoice`
--

LOCK TABLES `Invoice` WRITE;
/*!40000 ALTER TABLE `Invoice` DISABLE KEYS */;
/*!40000 ALTER TABLE `Invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Kind`
--

DROP TABLE IF EXISTS `Kind`;
CREATE TABLE `Kind` (
  `kind_id` int(8) NOT NULL auto_increment,
  `kind_domain_id` int(8) default '0',
  `kind_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `kind_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `kind_userupdate` int(8) default NULL,
  `kind_usercreate` int(8) default NULL,
  `kind_minilabel` varchar(64) default NULL,
  `kind_header` varchar(64) default NULL,
  `kind_lang` char(2) default NULL,
  `kind_default` int(1) NOT NULL default '0',
  PRIMARY KEY  (`kind_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Kind`
--

LOCK TABLES `Kind` WRITE;
/*!40000 ALTER TABLE `Kind` DISABLE KEYS */;
INSERT INTO `Kind` VALUES (1,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'M.','Monsieur','FR',0),(2,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'Mme','Madame','FR',0),(3,1,'2007-10-04 14:49:49','2007-10-04 14:49:49',2,NULL,'Mlle','Mademoiselle','FR',0);
/*!40000 ALTER TABLE `Kind` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Lead`
--

DROP TABLE IF EXISTS `Lead`;
CREATE TABLE `Lead` (
  `lead_id` int(8) NOT NULL auto_increment,
  `lead_domain_id` int(8) default '0',
  `lead_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `lead_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `lead_userupdate` int(8) default NULL,
  `lead_usercreate` int(8) default NULL,
  `lead_source_id` int(8) default NULL,
  `lead_manager_id` int(8) default NULL,
  `lead_company_id` int(8) NOT NULL default '0',
  `lead_contact_id` int(8) NOT NULL default '0',
  `lead_privacy` int(2) NOT NULL default '0',
  `lead_name` varchar(64) default NULL,
  `lead_date` date default NULL,
  `lead_datealarm` date default NULL,
  `lead_archive` char(1) default '0',
  `lead_todo` varchar(128) default NULL,
  `lead_comment` text,
  PRIMARY KEY  (`lead_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Lead`
--

LOCK TABLES `Lead` WRITE;
/*!40000 ALTER TABLE `Lead` DISABLE KEYS */;
/*!40000 ALTER TABLE `Lead` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `LeadSource`
--

DROP TABLE IF EXISTS `LeadSource`;
CREATE TABLE `LeadSource` (
  `leadsource_id` int(8) NOT NULL auto_increment,
  `leadsource_domain_id` int(8) default '0',
  `leadsource_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `leadsource_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `leadsource_userupdate` int(8) default NULL,
  `leadsource_usercreate` int(8) default NULL,
  `leadsource_code` varchar(10) default '',
  `leadsource_label` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`leadsource_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `LeadSource`
--

LOCK TABLES `LeadSource` WRITE;
/*!40000 ALTER TABLE `LeadSource` DISABLE KEYS */;
/*!40000 ALTER TABLE `LeadSource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `List`
--

DROP TABLE IF EXISTS `List`;
CREATE TABLE `List` (
  `list_id` int(8) NOT NULL auto_increment,
  `list_domain_id` int(8) default '0',
  `list_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `list_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `list_userupdate` int(8) default NULL,
  `list_usercreate` int(8) default NULL,
  `list_privacy` int(2) NOT NULL default '0',
  `list_name` varchar(64) NOT NULL,
  `list_subject` varchar(128) default NULL,
  `list_email` varchar(128) default NULL,
  `list_mode` int(1) default '0',
  `list_mailing_ok` int(1) default '0',
  `list_contact_archive` int(1) default '0',
  `list_info_publication` int(1) default '0',
  `list_static_nb` int(10) default '0',
  `list_query_nb` int(10) default '0',
  `list_query` text,
  `list_structure` text,
  PRIMARY KEY  (`list_id`),
  UNIQUE KEY `list_name` (`list_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `List`
--

LOCK TABLES `List` WRITE;
/*!40000 ALTER TABLE `List` DISABLE KEYS */;
/*!40000 ALTER TABLE `List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MailServer`
--

DROP TABLE IF EXISTS `MailServer`;
CREATE TABLE `MailServer` (
  `mailserver_id` int(8) NOT NULL auto_increment,
  `mailserver_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mailserver_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mailserver_userupdate` int(8) default NULL,
  `mailserver_usercreate` int(8) default NULL,
  `mailserver_host_id` int(8) NOT NULL default '0',
  `mailserver_relayhost_id` int(8) default NULL,
  `mailserver_imap` int(1) default '0',
  `mailserver_smtp_in` int(1) default '0',
  `mailserver_smtp_out` int(1) default '0',
  PRIMARY KEY  (`mailserver_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `MailServer`
--

LOCK TABLES `MailServer` WRITE;
/*!40000 ALTER TABLE `MailServer` DISABLE KEYS */;
/*!40000 ALTER TABLE `MailServer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MailServerNetwork`
--

DROP TABLE IF EXISTS `MailServerNetwork`;
CREATE TABLE `MailServerNetwork` (
  `mailservernetwork_host_id` int(8) NOT NULL default '0',
  `mailservernetwork_ip` varchar(16) NOT NULL default ''
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `MailServerNetwork`
--

LOCK TABLES `MailServerNetwork` WRITE;
/*!40000 ALTER TABLE `MailServerNetwork` DISABLE KEYS */;
/*!40000 ALTER TABLE `MailServerNetwork` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MailShare`
--

DROP TABLE IF EXISTS `MailShare`;
CREATE TABLE `MailShare` (
  `mailshare_id` int(8) NOT NULL auto_increment,
  `mailshare_domain_id` int(8) default '0',
  `mailshare_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mailshare_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mailshare_userupdate` int(8) default NULL,
  `mailshare_usercreate` int(8) default NULL,
  `mailshare_name` varchar(32) default NULL,
  `mailshare_archive` int(1) NOT NULL default '0',
  `mailshare_quota` int(11) NOT NULL default '0',
  `mailshare_mail_server_id` int(8) default '0',
  `mailshare_delegation` varchar(64) default '',
  `mailshare_description` varchar(255) default NULL,
  `mailshare_email` text,
  PRIMARY KEY  (`mailshare_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `MailShare`
--

LOCK TABLES `MailShare` WRITE;
/*!40000 ALTER TABLE `MailShare` DISABLE KEYS */;
/*!40000 ALTER TABLE `MailShare` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OGroup`
--

DROP TABLE IF EXISTS `OGroup`;
CREATE TABLE `OGroup` (
  `ogroup_id` int(8) NOT NULL auto_increment,
  `ogroup_domain_id` int(8) default '0',
  `ogroup_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `ogroup_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `ogroup_userupdate` int(8) default NULL,
  `ogroup_usercreate` int(8) default NULL,
  `ogroup_organizationalchart_id` int(8) NOT NULL,
  `ogroup_parent_id` int(8) NOT NULL,
  `ogroup_name` varchar(32) NOT NULL,
  `ogroup_level` varchar(16) default NULL,
  PRIMARY KEY  (`ogroup_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `OGroup`
--

LOCK TABLES `OGroup` WRITE;
/*!40000 ALTER TABLE `OGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `OGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OGroupEntity`
--

DROP TABLE IF EXISTS `OGroupEntity`;
CREATE TABLE `OGroupEntity` (
  `ogroupentity_id` int(8) NOT NULL auto_increment,
  `ogroupentity_domain_id` int(8) default '0',
  `ogroupentity_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `ogroupentity_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `ogroupentity_userupdate` int(8) default NULL,
  `ogroupentity_usercreate` int(8) default NULL,
  `ogroupentity_ogroup_id` int(8) NOT NULL,
  `ogroupentity_entity_id` int(8) NOT NULL,
  `ogroupentity_entity` varchar(32) NOT NULL,
  PRIMARY KEY  (`ogroupentity_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `OGroupEntity`
--

LOCK TABLES `OGroupEntity` WRITE;
/*!40000 ALTER TABLE `OGroupEntity` DISABLE KEYS */;
/*!40000 ALTER TABLE `OGroupEntity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ObmBookmark`
--

DROP TABLE IF EXISTS `ObmBookmark`;
CREATE TABLE `ObmBookmark` (
  `obmbookmark_id` int(8) NOT NULL auto_increment,
  `obmbookmark_user_id` int(8) NOT NULL,
  `obmbookmark_label` varchar(48) NOT NULL default '',
  `obmbookmark_entity` varchar(24) NOT NULL default '',
  PRIMARY KEY  (`obmbookmark_id`),
  KEY `bkm_idx_user` (`obmbookmark_user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ObmBookmark`
--

LOCK TABLES `ObmBookmark` WRITE;
/*!40000 ALTER TABLE `ObmBookmark` DISABLE KEYS */;
/*!40000 ALTER TABLE `ObmBookmark` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ObmBookmarkProperty`
--

DROP TABLE IF EXISTS `ObmBookmarkProperty`;
CREATE TABLE `ObmBookmarkProperty` (
  `obmbookmarkproperty_id` int(8) NOT NULL auto_increment,
  `obmbookmarkproperty_bookmark_id` int(8) NOT NULL,
  `obmbookmarkproperty_property` varchar(64) NOT NULL default '',
  `obmbookmarkproperty_value` varchar(64) NOT NULL default '',
  PRIMARY KEY  (`obmbookmarkproperty_id`),
  KEY `bkmprop_idx_bkm` (`obmbookmarkproperty_bookmark_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ObmBookmarkProperty`
--

LOCK TABLES `ObmBookmarkProperty` WRITE;
/*!40000 ALTER TABLE `ObmBookmarkProperty` DISABLE KEYS */;
/*!40000 ALTER TABLE `ObmBookmarkProperty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ObmInfo`
--

DROP TABLE IF EXISTS `ObmInfo`;
CREATE TABLE `ObmInfo` (
  `obminfo_name` varchar(32) NOT NULL default '',
  `obminfo_value` varchar(255) default '',
  PRIMARY KEY  (`obminfo_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ObmInfo`
--

LOCK TABLES `ObmInfo` WRITE;
/*!40000 ALTER TABLE `ObmInfo` DISABLE KEYS */;
INSERT INTO `ObmInfo` VALUES ('db_version','2.1'),('remote_access','0'),('update_lock','0');
/*!40000 ALTER TABLE `ObmInfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ObmSession`
--

DROP TABLE IF EXISTS `ObmSession`;
CREATE TABLE `ObmSession` (
  `obmsession_sid` varchar(32) NOT NULL default '',
  `obmsession_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `obmsession_name` varchar(32) NOT NULL default '',
  `obmsession_data` text,
  PRIMARY KEY  (`obmsession_sid`,`obmsession_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ObmSession`
--

LOCK TABLES `ObmSession` WRITE;
/*!40000 ALTER TABLE `ObmSession` DISABLE KEYS */;
/*!40000 ALTER TABLE `ObmSession` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OrganizationalChart`
--

DROP TABLE IF EXISTS `OrganizationalChart`;
CREATE TABLE `OrganizationalChart` (
  `organizationalchart_id` int(8) NOT NULL auto_increment,
  `organizationalchart_domain_id` int(8) default '0',
  `organizationalchart_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `organizationalchart_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `organizationalchart_userupdate` int(8) default NULL,
  `organizationalchart_usercreate` int(8) default NULL,
  `organizationalchart_name` varchar(32) NOT NULL,
  `organizationalchart_description` varchar(64) default NULL,
  `organizationalchart_archive` int(1) NOT NULL default '0',
  PRIMARY KEY  (`organizationalchart_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `OrganizationalChart`
--

LOCK TABLES `OrganizationalChart` WRITE;
/*!40000 ALTER TABLE `OrganizationalChart` DISABLE KEYS */;
/*!40000 ALTER TABLE `OrganizationalChart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_Domain`
--

DROP TABLE IF EXISTS `P_Domain`;
CREATE TABLE `P_Domain` (
  `domain_id` int(8) NOT NULL auto_increment,
  `domain_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `domain_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `domain_usercreate` int(8) default NULL,
  `domain_userupdate` int(8) default NULL,
  `domain_label` varchar(32) NOT NULL,
  `domain_description` varchar(255) default NULL,
  `domain_name` varchar(128) default NULL,
  `domain_alias` text,
  `domain_mail_server_id` int(8) default NULL,
  PRIMARY KEY  (`domain_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_Domain`
--

LOCK TABLES `P_Domain` WRITE;
/*!40000 ALTER TABLE `P_Domain` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_Domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_EntityRight`
--

DROP TABLE IF EXISTS `P_EntityRight`;
CREATE TABLE `P_EntityRight` (
  `entityright_entity` varchar(32) NOT NULL default '',
  `entityright_entity_id` int(8) NOT NULL default '0',
  `entityright_consumer` varchar(32) NOT NULL default '',
  `entityright_consumer_id` int(8) NOT NULL default '0',
  `entityright_read` int(1) NOT NULL default '0',
  `entityright_write` int(1) NOT NULL default '0',
  `entityright_admin` int(1) NOT NULL default '0',
  PRIMARY KEY  (`entityright_entity`,`entityright_entity_id`,`entityright_consumer`,`entityright_consumer_id`),
  KEY `entright_idx_ent_id` (`entityright_entity_id`),
  KEY `entright_idx_ent` (`entityright_entity`),
  KEY `entright_idx_con_id` (`entityright_consumer_id`),
  KEY `entright_idx_con` (`entityright_consumer`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_EntityRight`
--

LOCK TABLES `P_EntityRight` WRITE;
/*!40000 ALTER TABLE `P_EntityRight` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_EntityRight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_GroupGroup`
--

DROP TABLE IF EXISTS `P_GroupGroup`;
CREATE TABLE `P_GroupGroup` (
  `groupgroup_parent_id` int(8) NOT NULL default '0',
  `groupgroup_child_id` int(8) NOT NULL default '0',
  PRIMARY KEY  (`groupgroup_parent_id`,`groupgroup_child_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_GroupGroup`
--

LOCK TABLES `P_GroupGroup` WRITE;
/*!40000 ALTER TABLE `P_GroupGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_GroupGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_Host`
--

DROP TABLE IF EXISTS `P_Host`;
CREATE TABLE `P_Host` (
  `host_id` int(8) NOT NULL auto_increment,
  `host_domain_id` int(8) default '0',
  `host_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `host_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `host_userupdate` int(8) default NULL,
  `host_usercreate` int(8) default NULL,
  `host_uid` int(8) default NULL,
  `host_gid` int(8) default NULL,
  `host_samba` int(1) default '0',
  `host_name` varchar(32) NOT NULL,
  `host_ip` varchar(16) default NULL,
  `host_delegation` varchar(64) default '',
  `host_description` varchar(128) default NULL,
  `host_web_perms` int(1) default '0',
  `host_web_list` text,
  `host_web_all` int(1) default '0',
  `host_ftp_perms` int(1) default '0',
  `host_firewall_perms` varchar(128) default NULL,
  PRIMARY KEY  (`host_id`),
  UNIQUE KEY `host_name` (`host_name`),
  UNIQUE KEY `k_uid_host` (`host_uid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_Host`
--

LOCK TABLES `P_Host` WRITE;
/*!40000 ALTER TABLE `P_Host` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_Host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_MailServer`
--

DROP TABLE IF EXISTS `P_MailServer`;
CREATE TABLE `P_MailServer` (
  `mailserver_id` int(8) NOT NULL auto_increment,
  `mailserver_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mailserver_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mailserver_userupdate` int(8) default NULL,
  `mailserver_usercreate` int(8) default NULL,
  `mailserver_host_id` int(8) NOT NULL default '0',
  `mailserver_relayhost_id` int(8) default NULL,
  `mailserver_imap` int(1) default '0',
  `mailserver_smtp_in` int(1) default '0',
  `mailserver_smtp_out` int(1) default '0',
  PRIMARY KEY  (`mailserver_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_MailServer`
--

LOCK TABLES `P_MailServer` WRITE;
/*!40000 ALTER TABLE `P_MailServer` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_MailServer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_MailServerNetwork`
--

DROP TABLE IF EXISTS `P_MailServerNetwork`;
CREATE TABLE `P_MailServerNetwork` (
  `mailservernetwork_host_id` int(8) NOT NULL default '0',
  `mailservernetwork_ip` varchar(16) NOT NULL default ''
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_MailServerNetwork`
--

LOCK TABLES `P_MailServerNetwork` WRITE;
/*!40000 ALTER TABLE `P_MailServerNetwork` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_MailServerNetwork` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_MailShare`
--

DROP TABLE IF EXISTS `P_MailShare`;
CREATE TABLE `P_MailShare` (
  `mailshare_id` int(8) NOT NULL auto_increment,
  `mailshare_domain_id` int(8) default '0',
  `mailshare_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `mailshare_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `mailshare_userupdate` int(8) default NULL,
  `mailshare_usercreate` int(8) default NULL,
  `mailshare_name` varchar(32) default NULL,
  `mailshare_archive` int(1) NOT NULL default '0',
  `mailshare_quota` int(11) NOT NULL default '0',
  `mailshare_mail_server_id` int(8) default '0',
  `mailshare_delegation` varchar(64) default '',
  `mailshare_description` varchar(255) default NULL,
  `mailshare_email` text,
  PRIMARY KEY  (`mailshare_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_MailShare`
--

LOCK TABLES `P_MailShare` WRITE;
/*!40000 ALTER TABLE `P_MailShare` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_MailShare` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_Samba`
--

DROP TABLE IF EXISTS `P_Samba`;
CREATE TABLE `P_Samba` (
  `samba_domain_id` int(8) default '0',
  `samba_name` varchar(255) NOT NULL default '',
  `samba_value` varchar(255) NOT NULL default ''
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_Samba`
--

LOCK TABLES `P_Samba` WRITE;
/*!40000 ALTER TABLE `P_Samba` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_Samba` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_UGroup`
--

DROP TABLE IF EXISTS `P_UGroup`;
CREATE TABLE `P_UGroup` (
  `group_id` int(8) NOT NULL auto_increment,
  `group_domain_id` int(8) default '0',
  `group_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `group_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `group_userupdate` int(8) default NULL,
  `group_usercreate` int(8) default NULL,
  `group_system` int(1) default '0',
  `group_privacy` int(2) default '0',
  `group_local` int(1) default '1',
  `group_ext_id` varchar(24) default NULL,
  `group_samba` int(1) default '0',
  `group_gid` int(8) default NULL,
  `group_mailing` int(1) default '0',
  `group_delegation` varchar(64) default '',
  `group_manager_id` int(8) default '0',
  `group_name` varchar(32) NOT NULL,
  `group_desc` varchar(128) default NULL,
  `group_email` varchar(128) default NULL,
  `group_contacts` text,
  PRIMARY KEY  (`group_id`),
  UNIQUE KEY `group_gid` (`group_gid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_UGroup`
--

LOCK TABLES `P_UGroup` WRITE;
/*!40000 ALTER TABLE `P_UGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_UGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_UserObm`
--

DROP TABLE IF EXISTS `P_UserObm`;
CREATE TABLE `P_UserObm` (
  `userobm_id` int(8) NOT NULL auto_increment,
  `userobm_domain_id` int(8) default '0',
  `userobm_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `userobm_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_userupdate` int(8) default NULL,
  `userobm_usercreate` int(8) default NULL,
  `userobm_local` int(1) default '1',
  `userobm_ext_id` varchar(16) default NULL,
  `userobm_system` int(1) default '0',
  `userobm_archive` int(1) NOT NULL default '0',
  `userobm_timelastaccess` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_login` varchar(32) NOT NULL default '',
  `userobm_nb_login_failed` int(2) default '0',
  `userobm_password_type` char(6) NOT NULL default 'PLAIN',
  `userobm_password` varchar(64) NOT NULL default '',
  `userobm_password_dateexp` date default NULL,
  `userobm_account_dateexp` date default NULL,
  `userobm_perms` varchar(254) default NULL,
  `userobm_delegation_target` varchar(64) default '',
  `userobm_delegation` varchar(64) default '',
  `userobm_calendar_version` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_uid` int(8) default NULL,
  `userobm_gid` int(8) default NULL,
  `userobm_datebegin` date default NULL,
  `userobm_hidden` int(1) default '0',
  `userobm_kind` varchar(12) default NULL,
  `userobm_lastname` varchar(32) default '',
  `userobm_firstname` varchar(48) default '',
  `userobm_title` varchar(64) default '',
  `userobm_sound` varchar(48) default NULL,
  `userobm_company` varchar(64) default NULL,
  `userobm_direction` varchar(64) default NULL,
  `userobm_service` varchar(64) default NULL,
  `userobm_address1` varchar(64) default NULL,
  `userobm_address2` varchar(64) default NULL,
  `userobm_address3` varchar(64) default NULL,
  `userobm_zipcode` varchar(14) default NULL,
  `userobm_town` varchar(64) default NULL,
  `userobm_expresspostal` varchar(16) default NULL,
  `userobm_country_iso3166` char(2) default '0',
  `userobm_phone` varchar(32) default '',
  `userobm_phone2` varchar(32) default '',
  `userobm_mobile` varchar(32) default '',
  `userobm_fax` varchar(32) default '',
  `userobm_fax2` varchar(32) default '',
  `userobm_web_perms` int(1) default '0',
  `userobm_web_list` text,
  `userobm_web_all` int(1) default '0',
  `userobm_mail_perms` int(1) default '0',
  `userobm_mail_ext_perms` int(1) default '0',
  `userobm_email` text,
  `userobm_mail_server_id` int(8) default NULL,
  `userobm_mail_quota` int(8) default '0',
  `userobm_mail_quota_use` int(8) default '0',
  `userobm_mail_login_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_nomade_perms` int(1) default '0',
  `userobm_nomade_enable` int(1) default '0',
  `userobm_nomade_local_copy` int(1) default '0',
  `userobm_nomade_datebegin` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_nomade_dateend` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_email_nomade` varchar(64) default '',
  `userobm_vacation_enable` int(1) default '0',
  `userobm_vacation_datebegin` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_vacation_dateend` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_vacation_message` text,
  `userobm_samba_perms` int(1) default '0',
  `userobm_samba_home` varchar(255) default '',
  `userobm_samba_home_drive` char(2) default '',
  `userobm_samba_logon_script` varchar(128) default '',
  `userobm_host_id` int(8) default '0',
  `userobm_description` varchar(255) default NULL,
  `userobm_location` varchar(255) default NULL,
  `userobm_education` varchar(255) default NULL,
  `userobm_photo_id` int(8) default NULL,
  PRIMARY KEY  (`userobm_id`),
  UNIQUE KEY `k_login_user` (`userobm_login`),
  KEY `k_uid_user` (`userobm_uid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_UserObm`
--

LOCK TABLES `P_UserObm` WRITE;
/*!40000 ALTER TABLE `P_UserObm` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_UserObm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `P_UserObmGroup`
--

DROP TABLE IF EXISTS `P_UserObmGroup`;
CREATE TABLE `P_UserObmGroup` (
  `userobmgroup_group_id` int(8) NOT NULL default '0',
  `userobmgroup_userobm_id` int(8) NOT NULL default '0',
  PRIMARY KEY  (`userobmgroup_group_id`,`userobmgroup_userobm_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `P_UserObmGroup`
--

LOCK TABLES `P_UserObmGroup` WRITE;
/*!40000 ALTER TABLE `P_UserObmGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `P_UserObmGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ParentDeal`
--

DROP TABLE IF EXISTS `ParentDeal`;
CREATE TABLE `ParentDeal` (
  `parentdeal_id` int(8) NOT NULL auto_increment,
  `parentdeal_domain_id` int(8) default '0',
  `parentdeal_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `parentdeal_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `parentdeal_userupdate` int(8) default NULL,
  `parentdeal_usercreate` int(8) default NULL,
  `parentdeal_label` varchar(128) NOT NULL,
  `parentdeal_marketingmanager_id` int(8) default NULL,
  `parentdeal_technicalmanager_id` int(8) default NULL,
  `parentdeal_archive` char(1) default '0',
  `parentdeal_comment` text,
  PRIMARY KEY  (`parentdeal_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ParentDeal`
--

LOCK TABLES `ParentDeal` WRITE;
/*!40000 ALTER TABLE `ParentDeal` DISABLE KEYS */;
/*!40000 ALTER TABLE `ParentDeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Payment`
--

DROP TABLE IF EXISTS `Payment`;
CREATE TABLE `Payment` (
  `payment_id` int(8) NOT NULL auto_increment,
  `payment_domain_id` int(8) default '0',
  `payment_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `payment_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `payment_userupdate` int(8) default NULL,
  `payment_usercreate` int(8) default NULL,
  `payment_company_id` int(8) NOT NULL,
  `payment_account_id` int(8) default NULL,
  `payment_paymentkind_id` int(8) NOT NULL,
  `payment_amount` double(10,2) NOT NULL default '0.00',
  `payment_date` date default NULL,
  `payment_inout` char(1) NOT NULL default '+',
  `payment_number` varchar(24) default '',
  `payment_checked` char(1) NOT NULL default '0',
  `payment_gap` double(10,2) NOT NULL default '0.00',
  `payment_comment` text,
  PRIMARY KEY  (`payment_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Payment`
--

LOCK TABLES `Payment` WRITE;
/*!40000 ALTER TABLE `Payment` DISABLE KEYS */;
/*!40000 ALTER TABLE `Payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PaymentInvoice`
--

DROP TABLE IF EXISTS `PaymentInvoice`;
CREATE TABLE `PaymentInvoice` (
  `paymentinvoice_invoice_id` int(8) NOT NULL,
  `paymentinvoice_payment_id` int(8) NOT NULL,
  `paymentinvoice_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `paymentinvoice_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `paymentinvoice_userupdate` int(8) default NULL,
  `paymentinvoice_usercreate` int(8) default NULL,
  `paymentinvoice_amount` double(10,2) NOT NULL default '0.00',
  PRIMARY KEY  (`paymentinvoice_invoice_id`,`paymentinvoice_payment_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `PaymentInvoice`
--

LOCK TABLES `PaymentInvoice` WRITE;
/*!40000 ALTER TABLE `PaymentInvoice` DISABLE KEYS */;
/*!40000 ALTER TABLE `PaymentInvoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PaymentKind`
--

DROP TABLE IF EXISTS `PaymentKind`;
CREATE TABLE `PaymentKind` (
  `paymentkind_id` int(8) NOT NULL auto_increment,
  `paymentkind_domain_id` int(8) default '0',
  `paymentkind_shortlabel` varchar(3) NOT NULL default '',
  `paymentkind_label` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`paymentkind_id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `PaymentKind`
--

LOCK TABLES `PaymentKind` WRITE;
/*!40000 ALTER TABLE `PaymentKind` DISABLE KEYS */;
INSERT INTO `PaymentKind` VALUES (1,1,'Ch','Chèque'),(2,1,'Vir','Virement'),(3,1,'TIP','Titre Interbancaire de Paiement'),(4,1,'PA','Prélèvement Automatique'),(5,1,'FrB','Frais bancaires'),(6,1,'BAO','Billet à ordre'),(7,1,'LC','Lettre de change'),(8,1,'CB','Carte de crédit');
/*!40000 ALTER TABLE `PaymentKind` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Project`
--

DROP TABLE IF EXISTS `Project`;
CREATE TABLE `Project` (
  `project_id` int(8) NOT NULL auto_increment,
  `project_domain_id` int(8) default '0',
  `project_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `project_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `project_userupdate` int(8) default NULL,
  `project_usercreate` int(8) default NULL,
  `project_name` varchar(128) default NULL,
  `project_shortname` varchar(10) default NULL,
  `project_tasktype_id` int(8) default NULL,
  `project_company_id` int(8) default NULL,
  `project_deal_id` int(8) default NULL,
  `project_soldtime` int(8) default NULL,
  `project_estimatedtime` int(8) default NULL,
  `project_datebegin` date default NULL,
  `project_dateend` date default NULL,
  `project_archive` char(1) default '0',
  `project_comment` text,
  `project_reference_date` varchar(32) default NULL,
  `project_reference_duration` varchar(16) default NULL,
  `project_reference_desc` text,
  `project_reference_tech` text,
  PRIMARY KEY  (`project_id`),
  KEY `project_idx_comp` (`project_company_id`),
  KEY `project_idx_deal` (`project_deal_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Project`
--

LOCK TABLES `Project` WRITE;
/*!40000 ALTER TABLE `Project` DISABLE KEYS */;
/*!40000 ALTER TABLE `Project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ProjectCV`
--

DROP TABLE IF EXISTS `ProjectCV`;
CREATE TABLE `ProjectCV` (
  `projectcv_project_id` int(8) NOT NULL,
  `projectcv_cv_id` int(8) NOT NULL,
  `projectcv_role` varchar(128) default '',
  PRIMARY KEY  (`projectcv_project_id`,`projectcv_cv_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ProjectCV`
--

LOCK TABLES `ProjectCV` WRITE;
/*!40000 ALTER TABLE `ProjectCV` DISABLE KEYS */;
/*!40000 ALTER TABLE `ProjectCV` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ProjectClosing`
--

DROP TABLE IF EXISTS `ProjectClosing`;
CREATE TABLE `ProjectClosing` (
  `projectclosing_id` int(8) NOT NULL auto_increment,
  `projectclosing_project_id` int(8) NOT NULL,
  `projectclosing_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `projectclosing_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `projectclosing_userupdate` int(8) default NULL,
  `projectclosing_usercreate` int(8) NOT NULL,
  `projectclosing_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `projectclosing_used` int(8) NOT NULL,
  `projectclosing_remaining` int(8) NOT NULL,
  `projectclosing_type` int(8) default NULL,
  PRIMARY KEY  (`projectclosing_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ProjectClosing`
--

LOCK TABLES `ProjectClosing` WRITE;
/*!40000 ALTER TABLE `ProjectClosing` DISABLE KEYS */;
/*!40000 ALTER TABLE `ProjectClosing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ProjectRefTask`
--

DROP TABLE IF EXISTS `ProjectRefTask`;
CREATE TABLE `ProjectRefTask` (
  `projectreftask_id` int(8) NOT NULL auto_increment,
  `projectreftask_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `projectreftask_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `projectreftask_userupdate` int(8) default NULL,
  `projectreftask_usercreate` int(8) default NULL,
  `projectreftask_tasktype_id` int(8) default NULL,
  `projectreftask_label` varchar(128) default NULL,
  PRIMARY KEY  (`projectreftask_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ProjectRefTask`
--

LOCK TABLES `ProjectRefTask` WRITE;
/*!40000 ALTER TABLE `ProjectRefTask` DISABLE KEYS */;
/*!40000 ALTER TABLE `ProjectRefTask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ProjectTask`
--

DROP TABLE IF EXISTS `ProjectTask`;
CREATE TABLE `ProjectTask` (
  `projecttask_id` int(8) NOT NULL auto_increment,
  `projecttask_project_id` int(8) NOT NULL,
  `projecttask_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `projecttask_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `projecttask_userupdate` int(8) default NULL,
  `projecttask_usercreate` int(8) default NULL,
  `projecttask_label` varchar(128) default NULL,
  `projecttask_parenttask_id` int(8) default '0',
  `projecttask_rank` int(8) default NULL,
  `projecttask_datebegin` date default NULL,
  `projecttask_dateend` date default NULL,
  PRIMARY KEY  (`projecttask_id`),
  KEY `pt_idx_pro` (`projecttask_project_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ProjectTask`
--

LOCK TABLES `ProjectTask` WRITE;
/*!40000 ALTER TABLE `ProjectTask` DISABLE KEYS */;
/*!40000 ALTER TABLE `ProjectTask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ProjectUser`
--

DROP TABLE IF EXISTS `ProjectUser`;
CREATE TABLE `ProjectUser` (
  `projectuser_id` int(8) NOT NULL auto_increment,
  `projectuser_project_id` int(8) NOT NULL,
  `projectuser_user_id` int(8) NOT NULL,
  `projectuser_projecttask_id` int(8) default NULL,
  `projectuser_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `projectuser_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `projectuser_userupdate` int(8) default NULL,
  `projectuser_usercreate` int(8) default NULL,
  `projectuser_projectedtime` float default NULL,
  `projectuser_missingtime` float default NULL,
  `projectuser_validity` timestamp NOT NULL default '0000-00-00 00:00:00',
  `projectuser_soldprice` int(8) default NULL,
  `projectuser_manager` int(1) default NULL,
  PRIMARY KEY  (`projectuser_id`),
  KEY `pu_idx_pro` (`projectuser_project_id`),
  KEY `pu_idx_user` (`projectuser_user_id`),
  KEY `pu_idx_pt` (`projectuser_projecttask_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ProjectUser`
--

LOCK TABLES `ProjectUser` WRITE;
/*!40000 ALTER TABLE `ProjectUser` DISABLE KEYS */;
/*!40000 ALTER TABLE `ProjectUser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Publication`
--

DROP TABLE IF EXISTS `Publication`;
CREATE TABLE `Publication` (
  `publication_id` int(8) NOT NULL auto_increment,
  `publication_domain_id` int(8) default '0',
  `publication_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `publication_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `publication_userupdate` int(8) default NULL,
  `publication_usercreate` int(8) default NULL,
  `publication_title` varchar(64) NOT NULL,
  `publication_type_id` int(8) default NULL,
  `publication_year` int(4) default NULL,
  `publication_lang` varchar(30) default NULL,
  `publication_desc` text,
  PRIMARY KEY  (`publication_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Publication`
--

LOCK TABLES `Publication` WRITE;
/*!40000 ALTER TABLE `Publication` DISABLE KEYS */;
/*!40000 ALTER TABLE `Publication` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PublicationType`
--

DROP TABLE IF EXISTS `PublicationType`;
CREATE TABLE `PublicationType` (
  `publicationtype_id` int(8) NOT NULL auto_increment,
  `publicationtype_domain_id` int(8) default '0',
  `publicationtype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `publicationtype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `publicationtype_userupdate` int(8) default NULL,
  `publicationtype_usercreate` int(8) default NULL,
  `publication_code` varchar(10) default '',
  `publicationtype_label` varchar(64) default NULL,
  PRIMARY KEY  (`publicationtype_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `PublicationType`
--

LOCK TABLES `PublicationType` WRITE;
/*!40000 ALTER TABLE `PublicationType` DISABLE KEYS */;
/*!40000 ALTER TABLE `PublicationType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RGroup`
--

DROP TABLE IF EXISTS `RGroup`;
CREATE TABLE `RGroup` (
  `rgroup_id` int(8) NOT NULL auto_increment,
  `rgroup_domain_id` int(8) default '0',
  `rgroup_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `rgroup_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `rgroup_userupdate` int(8) default NULL,
  `rgroup_usercreate` int(8) default NULL,
  `rgroup_privacy` int(2) default '0',
  `rgroup_name` varchar(32) NOT NULL,
  `rgroup_desc` varchar(128) default NULL,
  PRIMARY KEY  (`rgroup_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `RGroup`
--

LOCK TABLES `RGroup` WRITE;
/*!40000 ALTER TABLE `RGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `RGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Region`
--

DROP TABLE IF EXISTS `Region`;
CREATE TABLE `Region` (
  `region_id` int(8) NOT NULL auto_increment,
  `region_domain_id` int(8) default '0',
  `region_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `region_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `region_userupdate` int(8) default NULL,
  `region_usercreate` int(8) default NULL,
  `region_code` varchar(10) default '',
  `region_label` varchar(64) default NULL,
  PRIMARY KEY  (`region_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Region`
--

LOCK TABLES `Region` WRITE;
/*!40000 ALTER TABLE `Region` DISABLE KEYS */;
/*!40000 ALTER TABLE `Region` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Resource`
--

DROP TABLE IF EXISTS `Resource`;
CREATE TABLE `Resource` (
  `resource_id` int(8) NOT NULL auto_increment,
  `resource_domain_id` int(8) default '0',
  `resource_rtype_id` int(8) default NULL,
  `resource_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `resource_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `resource_userupdate` int(8) default NULL,
  `resource_usercreate` int(8) default NULL,
  `resource_name` varchar(32) NOT NULL default '',
  `resource_description` varchar(255) default NULL,
  `resource_qty` int(8) default '0',
  PRIMARY KEY  (`resource_id`),
  UNIQUE KEY `k_label_resource` (`resource_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Resource`
--

LOCK TABLES `Resource` WRITE;
/*!40000 ALTER TABLE `Resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `Resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ResourceGroup`
--

DROP TABLE IF EXISTS `ResourceGroup`;
CREATE TABLE `ResourceGroup` (
  `resourcegroup_rgroup_id` int(8) NOT NULL default '0',
  `resourcegroup_resource_id` int(8) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ResourceGroup`
--

LOCK TABLES `ResourceGroup` WRITE;
/*!40000 ALTER TABLE `ResourceGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `ResourceGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ResourceItem`
--

DROP TABLE IF EXISTS `ResourceItem`;
CREATE TABLE `ResourceItem` (
  `resourceitem_id` int(8) NOT NULL auto_increment,
  `resourceitem_domain_id` int(8) default '0',
  `resourceitem_label` varchar(32) NOT NULL,
  `resourceitem_resourcetype_id` int(8) NOT NULL,
  `resourceitem_description` text,
  PRIMARY KEY  (`resourceitem_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ResourceItem`
--

LOCK TABLES `ResourceItem` WRITE;
/*!40000 ALTER TABLE `ResourceItem` DISABLE KEYS */;
/*!40000 ALTER TABLE `ResourceItem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ResourceType`
--

DROP TABLE IF EXISTS `ResourceType`;
CREATE TABLE `ResourceType` (
  `resourcetype_id` int(8) NOT NULL auto_increment,
  `resourcetype_domain_id` int(8) default '0',
  `resourcetype_label` varchar(32) NOT NULL,
  `resourcetype_property` varchar(32) default NULL,
  `resourcetype_pkind` int(1) NOT NULL default '0',
  PRIMARY KEY  (`resourcetype_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ResourceType`
--

LOCK TABLES `ResourceType` WRITE;
/*!40000 ALTER TABLE `ResourceType` DISABLE KEYS */;
/*!40000 ALTER TABLE `ResourceType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Samba`
--

DROP TABLE IF EXISTS `Samba`;
CREATE TABLE `Samba` (
  `samba_domain_id` int(8) default '0',
  `samba_name` varchar(255) NOT NULL default '',
  `samba_value` varchar(255) NOT NULL default ''
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Samba`
--

LOCK TABLES `Samba` WRITE;
/*!40000 ALTER TABLE `Samba` DISABLE KEYS */;
INSERT INTO `Samba` VALUES (1,'samba_domain','TEST-DOMAIN'),(1,'samba_sid','S-1-5-21-735385164-1086204177-245137893'),(1,'samba_pdc','PDCTEST'),(1,'samba_profile','\\\\PDCTEST\\%u\\.profiles'),(1,'samba_home_def','\\\\PDCTEST\\%u'),(1,'samba_home_drive_def','P');
/*!40000 ALTER TABLE `Samba` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Stats`
--

DROP TABLE IF EXISTS `Stats`;
CREATE TABLE `Stats` (
  `stats_name` varchar(32) NOT NULL default '',
  `stats_value` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`stats_name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Stats`
--

LOCK TABLES `Stats` WRITE;
/*!40000 ALTER TABLE `Stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `Stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Subscription`
--

DROP TABLE IF EXISTS `Subscription`;
CREATE TABLE `Subscription` (
  `subscription_id` int(8) NOT NULL auto_increment,
  `subscription_domain_id` int(8) default '0',
  `subscription_publication_id` int(8) NOT NULL,
  `subscription_contact_id` int(8) NOT NULL,
  `subscription_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `subscription_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `subscription_userupdate` int(8) default NULL,
  `subscription_usercreate` int(8) default NULL,
  `subscription_quantity` int(8) default NULL,
  `subscription_renewal` int(1) NOT NULL default '0',
  `subscription_reception_id` int(8) NOT NULL default '0',
  `subscription_date_begin` timestamp NOT NULL default '0000-00-00 00:00:00',
  `subscription_date_end` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`subscription_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Subscription`
--

LOCK TABLES `Subscription` WRITE;
/*!40000 ALTER TABLE `Subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `Subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SubscriptionReception`
--

DROP TABLE IF EXISTS `SubscriptionReception`;
CREATE TABLE `SubscriptionReception` (
  `subscriptionreception_id` int(8) NOT NULL auto_increment,
  `subscriptionreception_domain_id` int(8) default '0',
  `subscriptionreception_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `subscriptionreception_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `subscriptionreception_userupdate` int(8) default NULL,
  `subscriptionreception_usercreate` int(8) default NULL,
  `subscriptionreception_code` varchar(10) default '',
  `subscriptionreception_label` char(12) default NULL,
  PRIMARY KEY  (`subscriptionreception_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `SubscriptionReception`
--

LOCK TABLES `SubscriptionReception` WRITE;
/*!40000 ALTER TABLE `SubscriptionReception` DISABLE KEYS */;
/*!40000 ALTER TABLE `SubscriptionReception` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TaskType`
--

DROP TABLE IF EXISTS `TaskType`;
CREATE TABLE `TaskType` (
  `tasktype_id` int(8) NOT NULL auto_increment,
  `tasktype_domain_id` int(8) default '0',
  `tasktype_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `tasktype_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `tasktype_userupdate` int(8) default NULL,
  `tasktype_usercreate` int(8) default NULL,
  `tasktype_internal` int(1) NOT NULL,
  `tasktype_label` varchar(32) default NULL,
  PRIMARY KEY  (`tasktype_id`)
) ENGINE=MyISAM AUTO_INCREMENT=22 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `TaskType`
--

LOCK TABLES `TaskType` WRITE;
/*!40000 ALTER TABLE `TaskType` DISABLE KEYS */;
INSERT INTO `TaskType` VALUES (1,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Développement'),(2,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Sav / Maintenance'),(3,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Formation'),(4,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Etudes / Conseil'),(5,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Réseau / Intégration'),(6,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Infographie'),(7,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Hébergement'),(8,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Matériel'),(9,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,0,'Autres'),(10,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Avant vente'),(11,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Préparation formation'),(12,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,1,'Développements internes'),(13,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,1,'Projets internes'),(14,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Auto-Formations,Veille'),(15,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Garantie contractuelle projets'),(16,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Divers(direction,autres)'),(17,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Congés , absences , maladie'),(18,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Déplacements'),(19,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Infographie/Communication'),(20,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Administratif'),(21,1,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,2,'Réunions');
/*!40000 ALTER TABLE `TaskType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TimeTask`
--

DROP TABLE IF EXISTS `TimeTask`;
CREATE TABLE `TimeTask` (
  `timetask_id` int(8) NOT NULL auto_increment,
  `timetask_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `timetask_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `timetask_userupdate` int(8) default NULL,
  `timetask_usercreate` int(8) default NULL,
  `timetask_user_id` int(8) default NULL,
  `timetask_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `timetask_projecttask_id` int(8) default NULL,
  `timetask_length` float default NULL,
  `timetask_tasktype_id` int(8) default NULL,
  `timetask_label` varchar(255) default NULL,
  `timetask_status` int(1) default NULL,
  PRIMARY KEY  (`timetask_id`),
  KEY `tt_idx_pt` (`timetask_projecttask_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `TimeTask`
--

LOCK TABLES `TimeTask` WRITE;
/*!40000 ALTER TABLE `TimeTask` DISABLE KEYS */;
/*!40000 ALTER TABLE `TimeTask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Todo`
--

DROP TABLE IF EXISTS `Todo`;
CREATE TABLE `Todo` (
  `todo_id` int(8) NOT NULL auto_increment,
  `todo_domain_id` int(8) default '0',
  `todo_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `todo_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `todo_userupdate` int(8) default NULL,
  `todo_usercreate` int(8) default NULL,
  `todo_user` int(8) default NULL,
  `todo_privacy` int(2) NOT NULL default '0',
  `todo_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `todo_deadline` timestamp NOT NULL default '0000-00-00 00:00:00',
  `todo_dateend` timestamp NOT NULL default '0000-00-00 00:00:00',
  `todo_priority` int(8) default NULL,
  `todo_percent` int(8) default NULL,
  `todo_title` varchar(80) default NULL,
  `todo_status` varchar(32) default NULL,
  `todo_webpage` varchar(255) default NULL,
  `todo_content` text,
  PRIMARY KEY  (`todo_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Todo`
--

LOCK TABLES `Todo` WRITE;
/*!40000 ALTER TABLE `Todo` DISABLE KEYS */;
/*!40000 ALTER TABLE `Todo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UGroup`
--

DROP TABLE IF EXISTS `UGroup`;
CREATE TABLE `UGroup` (
  `group_id` int(8) NOT NULL auto_increment,
  `group_domain_id` int(8) default '0',
  `group_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `group_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `group_userupdate` int(8) default NULL,
  `group_usercreate` int(8) default NULL,
  `group_system` int(1) default '0',
  `group_privacy` int(2) default '0',
  `group_local` int(1) default '1',
  `group_ext_id` varchar(24) default NULL,
  `group_samba` int(1) default '0',
  `group_gid` int(8) default NULL,
  `group_mailing` int(1) default '0',
  `group_delegation` varchar(64) default '',
  `group_manager_id` int(8) default '0',
  `group_name` varchar(32) NOT NULL,
  `group_desc` varchar(128) default NULL,
  `group_email` varchar(128) default NULL,
  `group_contacts` text,
  PRIMARY KEY  (`group_id`),
  UNIQUE KEY `group_gid` (`group_gid`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `UGroup`
--

LOCK TABLES `UGroup` WRITE;
/*!40000 ALTER TABLE `UGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `UGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Updated`
--

DROP TABLE IF EXISTS `Updated`;
CREATE TABLE `Updated` (
  `updated_id` int(8) NOT NULL auto_increment,
  `updated_domain_id` int(8) default NULL,
  `updated_user_id` int(8) default NULL,
  `updated_delegation` varchar(64) default '',
  `updated_table` varchar(32) default NULL,
  `updated_entity_id` int(8) default NULL,
  `updated_type` char(1) default NULL,
  PRIMARY KEY  (`updated_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Updated`
--

LOCK TABLES `Updated` WRITE;
/*!40000 ALTER TABLE `Updated` DISABLE KEYS */;
/*!40000 ALTER TABLE `Updated` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Updatedlinks`
--

DROP TABLE IF EXISTS `Updatedlinks`;
CREATE TABLE `Updatedlinks` (
  `updatedlinks_id` int(8) NOT NULL auto_increment,
  `updatedlinks_domain_id` int(8) default NULL,
  `updatedlinks_user_id` int(8) default NULL,
  `updatedlinks_delegation` varchar(64) default NULL,
  `updatedlinks_table` varchar(32) default NULL,
  `updatedlinks_entity` varchar(32) default NULL,
  `updatedlinks_entity_id` int(8) default NULL,
  PRIMARY KEY  (`updatedlinks_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Updatedlinks`
--

LOCK TABLES `Updatedlinks` WRITE;
/*!40000 ALTER TABLE `Updatedlinks` DISABLE KEYS */;
/*!40000 ALTER TABLE `Updatedlinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserObm`
--

DROP TABLE IF EXISTS `UserObm`;
CREATE TABLE `UserObm` (
  `userobm_id` int(8) NOT NULL auto_increment,
  `userobm_domain_id` int(8) default '0',
  `userobm_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `userobm_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_userupdate` int(8) default NULL,
  `userobm_usercreate` int(8) default NULL,
  `userobm_local` int(1) default '1',
  `userobm_ext_id` varchar(16) default NULL,
  `userobm_system` int(1) default '0',
  `userobm_archive` int(1) NOT NULL default '0',
  `userobm_timelastaccess` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_login` varchar(32) NOT NULL default '',
  `userobm_nb_login_failed` int(2) default '0',
  `userobm_password_type` char(6) NOT NULL default 'PLAIN',
  `userobm_password` varchar(64) NOT NULL default '',
  `userobm_password_dateexp` date default NULL,
  `userobm_account_dateexp` date default NULL,
  `userobm_perms` varchar(254) default NULL,
  `userobm_delegation_target` varchar(64) default '',
  `userobm_delegation` varchar(64) default '',
  `userobm_calendar_version` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_uid` int(8) default NULL,
  `userobm_gid` int(8) default NULL,
  `userobm_datebegin` date default NULL,
  `userobm_hidden` int(1) default '0',
  `userobm_kind` varchar(12) default NULL,
  `userobm_lastname` varchar(32) default '',
  `userobm_firstname` varchar(48) default '',
  `userobm_title` varchar(64) default '',
  `userobm_sound` varchar(48) default NULL,
  `userobm_company` varchar(64) default NULL,
  `userobm_direction` varchar(64) default NULL,
  `userobm_service` varchar(64) default NULL,
  `userobm_address1` varchar(64) default NULL,
  `userobm_address2` varchar(64) default NULL,
  `userobm_address3` varchar(64) default NULL,
  `userobm_zipcode` varchar(14) default NULL,
  `userobm_town` varchar(64) default NULL,
  `userobm_expresspostal` varchar(16) default NULL,
  `userobm_country_iso3166` char(2) default '0',
  `userobm_phone` varchar(32) default '',
  `userobm_phone2` varchar(32) default '',
  `userobm_mobile` varchar(32) default '',
  `userobm_fax` varchar(32) default '',
  `userobm_fax2` varchar(32) default '',
  `userobm_web_perms` int(1) default '0',
  `userobm_web_list` text,
  `userobm_web_all` int(1) default '0',
  `userobm_mail_perms` int(1) default '0',
  `userobm_mail_ext_perms` int(1) default '0',
  `userobm_email` text,
  `userobm_mail_server_id` int(8) default NULL,
  `userobm_mail_quota` int(8) default '0',
  `userobm_mail_quota_use` int(8) default '0',
  `userobm_mail_login_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_nomade_perms` int(1) default '0',
  `userobm_nomade_enable` int(1) default '0',
  `userobm_nomade_local_copy` int(1) default '0',
  `userobm_nomade_datebegin` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_nomade_dateend` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_email_nomade` varchar(64) default '',
  `userobm_vacation_enable` int(1) default '0',
  `userobm_vacation_datebegin` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_vacation_dateend` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_vacation_message` text,
  `userobm_samba_perms` int(1) default '0',
  `userobm_samba_home` varchar(255) default '',
  `userobm_samba_home_drive` char(2) default '',
  `userobm_samba_logon_script` varchar(128) default '',
  `userobm_host_id` int(8) default '0',
  `userobm_description` varchar(255) default NULL,
  `userobm_location` varchar(255) default NULL,
  `userobm_education` varchar(255) default NULL,
  `userobm_photo_id` int(8) default NULL,
  PRIMARY KEY  (`userobm_id`),
  UNIQUE KEY `k_login_user` (`userobm_login`),
  KEY `k_uid_user` (`userobm_uid`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `UserObm`
--

LOCK TABLES `UserObm` WRITE;
/*!40000 ALTER TABLE `UserObm` DISABLE KEYS */;
INSERT INTO `UserObm` VALUES (1,0,'2007-10-04 14:49:49','0000-00-00 00:00:00',NULL,NULL,1,NULL,0,0,'0000-00-00 00:00:00','admin0',0,'PLAIN','admin',NULL,NULL,'admin','','','0000-00-00 00:00:00',1000,512,NULL,0,NULL,'Admin Lastname','Firstname','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0','','','','','',0,NULL,0,0,0,NULL,NULL,0,0,'0000-00-00 00:00:00',0,0,0,'0000-00-00 00:00:00','0000-00-00 00:00:00','',0,'0000-00-00 00:00:00','0000-00-00 00:00:00',NULL,0,'','','',0,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `UserObm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserObmGroup`
--

DROP TABLE IF EXISTS `UserObmGroup`;
CREATE TABLE `UserObmGroup` (
  `userobmgroup_group_id` int(8) NOT NULL default '0',
  `userobmgroup_userobm_id` int(8) NOT NULL default '0',
  PRIMARY KEY  (`userobmgroup_group_id`,`userobmgroup_userobm_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `UserObmGroup`
--

LOCK TABLES `UserObmGroup` WRITE;
/*!40000 ALTER TABLE `UserObmGroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `UserObmGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserObmPref`
--

DROP TABLE IF EXISTS `UserObmPref`;
CREATE TABLE `UserObmPref` (
  `userobmpref_user_id` int(8) NOT NULL default '0',
  `userobmpref_option` varchar(50) NOT NULL,
  `userobmpref_value` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `UserObmPref`
--

LOCK TABLES `UserObmPref` WRITE;
/*!40000 ALTER TABLE `UserObmPref` DISABLE KEYS */;
INSERT INTO `UserObmPref` VALUES (0,'set_lang','fr'),(0,'set_theme','default'),(0,'set_menu','both'),(0,'set_display','no'),(0,'set_rows','12'),(0,'set_dsrc','0'),(0,'set_date_upd','m/d/Y'),(0,'set_date','Y-m-d'),(0,'set_commentorder','0'),(0,'set_cal_interval','2'),(0,'set_csv_sep',';'),(0,'set_debug','0'),(0,'set_mail','yes'),(0,'set_day_weekstart','monday'),(0,'set_todo','todo_priority');
/*!40000 ALTER TABLE `UserObmPref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserObm_SessionLog`
--

DROP TABLE IF EXISTS `UserObm_SessionLog`;
CREATE TABLE `UserObm_SessionLog` (
  `userobm_sessionlog_sid` varchar(32) NOT NULL default '',
  `userobm_sessionlog_session_name` varchar(32) NOT NULL default '',
  `userobm_sessionlog_userobm_id` int(11) default NULL,
  `userobm_sessionlog_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `userobm_sessionlog_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `userobm_sessionlog_nb_connexions` int(11) NOT NULL default '0',
  `userobm_sessionlog_lastpage` varchar(32) NOT NULL default '0',
  `userobm_sessionlog_ip` varchar(32) NOT NULL default '0',
  PRIMARY KEY  (`userobm_sessionlog_sid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `UserObm_SessionLog`
--

LOCK TABLES `UserObm_SessionLog` WRITE;
/*!40000 ALTER TABLE `UserObm_SessionLog` DISABLE KEYS */;
/*!40000 ALTER TABLE `UserObm_SessionLog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserSystem`
--

DROP TABLE IF EXISTS `UserSystem`;
CREATE TABLE `UserSystem` (
  `usersystem_id` int(8) NOT NULL auto_increment,
  `usersystem_login` varchar(32) NOT NULL default '',
  `usersystem_password` varchar(32) NOT NULL default '',
  `usersystem_uid` varchar(6) default NULL,
  `usersystem_gid` varchar(6) default NULL,
  `usersystem_homedir` varchar(32) NOT NULL default '/tmp',
  `usersystem_lastname` varchar(32) default NULL,
  `usersystem_firstname` varchar(32) default NULL,
  `usersystem_shell` varchar(32) default NULL,
  PRIMARY KEY  (`usersystem_id`),
  UNIQUE KEY `k_login_user` (`usersystem_login`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `UserSystem`
--

LOCK TABLES `UserSystem` WRITE;
/*!40000 ALTER TABLE `UserSystem` DISABLE KEYS */;
INSERT INTO `UserSystem` VALUES (1,'cyrus','cyrus','103','8','/var/spool/cyrus','Cyrus','Administrator','/bin/false'),(2,'ldapadmin','mdp3PaAL','150','65534','/var/lib/ldap','LDAP','Administrator','/bin/false'),(3,'samba','m#Pa!NtA','106','65534','/','SAMBA','Administrateur','/bin/false');
/*!40000 ALTER TABLE `UserSystem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `of_usergroup`
--

DROP TABLE IF EXISTS `of_usergroup`;
CREATE TABLE `of_usergroup` (
  `of_usergroup_group_id` int(8) NOT NULL default '0',
  `of_usergroup_userobm_id` int(8) NOT NULL default '0',
  PRIMARY KEY  (`of_usergroup_group_id`,`of_usergroup_userobm_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `of_usergroup`
--

LOCK TABLES `of_usergroup` WRITE;
/*!40000 ALTER TABLE `of_usergroup` DISABLE KEYS */;
/*!40000 ALTER TABLE `of_usergroup` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2007-10-04 14:50:10
