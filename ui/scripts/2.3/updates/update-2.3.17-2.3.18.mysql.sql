UPDATE ObmInfo SET obminfo_value = '2.3.18-pre' WHERE obminfo_name = 'db_version';

-- Modification of the hit rate column to be NOT NULL
ALTER TABLE `Deal` CHANGE COLUMN deal_hitrate `deal_hitrate` int(3) NOT NULL default '0';

-- Modification the lastname to allow NULL values (necessary when importing ics)
ALTER TABLE `Contact` CHANGE COLUMN contact_lastname `contact_lastname` varchar(64) default NULL;

ALTER TABLE `opush_sync_mail` ADD COLUMN `is_read` tinyint(1) default 0;

UPDATE ObmInfo SET obminfo_value = '2.3.18' WHERE obminfo_name = 'db_version';

