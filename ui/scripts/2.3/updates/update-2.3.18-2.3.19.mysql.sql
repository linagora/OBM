UPDATE ObmInfo SET obminfo_value = '2.3.19-pre' WHERE obminfo_name = 'db_version';

DELETE FROM `opush_sync_mail`;
ALTER TABLE `opush_sync_mail` ADD COLUMN `timestamp` timestamp NOT NULL;

DROP TABLE IF EXISTS `opush_sync_deleted_mail`;
CREATE TABLE opush_sync_deleted_mail (
        `collection_id`   INTEGER NOT NULL,
        `device_id`       INTEGER NOT NULL,
        `mail_uid`        INTEGER NOT NULL,
	`timestamp`  	  timestamp NOT NULL,
        KEY `opush_sync_deletedmail_collection_id_folder_mapping_id_fkey` (`collection_id`),
        KEY `opush_sync_deletedmail_device_id_device_id_fkey` (`device_id`),
        CONSTRAINT `opush_sync_deletedmail_collection_id_folder_mapping_id_fkey` FOREIGN KEY (`collection_id`) REFERENCES `opush_folder_mapping` (`id`) ON DELETE CASCADE,
        CONSTRAINT `opush_sync_deletedmail_device_id_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


UPDATE ObmInfo SET obminfo_value = '2.3.19' WHERE obminfo_name = 'db_version';

