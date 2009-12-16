-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.3.0-2.3.1.mysql.sql
-- 2009-12-16 Adrien Poupard
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////

UPDATE ObmInfo SET obminfo_value = '2.3.1-pre' WHERE obminfo_name = 'db_version';

--
-- Table structure for table `opush_ping_heartbeat`
--
CREATE TABLE `opush_ping_heartbeat` (
        `device_id`       INTEGER NOT NULL,
        `last_heartbeat`  INTEGER NOT NULL,
        KEY `opush_ping_heartbeat_devive_id_opush_device_id_fkey` (`device_id`),
        CONSTRAINT `opush_ping_heartbeat_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `opush_ping_heartbeat` ADD CONSTRAINT
unique_opush_col_dev UNIQUE (`device_id`);

UPDATE ObmInfo SET obminfo_value = '2.3.1' WHERE obminfo_name = 'db_version';

