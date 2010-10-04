UPDATE ObmInfo SET obminfo_value = '2.3.12-pre' WHERE obminfo_name = 'db_version';

DROP TABLE IF EXISTS `opush_invitation_mapping`;
CREATE TABLE `opush_invitation_mapping` (
       	`mail_collection_id`    	INTEGER,
       	`mail_uid`              	INT(8),
       	`event_collection_id`    	INTEGER NOT NULL,
       	`event_uid`              	VARCHAR(300) NOT NULL,
       	`status` 			VARCHAR(20) NOT NULL,
       	`dtstamp`                	timestamp,
	`sync_key`		        VARCHAR(64),
        CONSTRAINT `opush_invitation_mapping_email_opush_folder_mapping_id_fkey` FOREIGN KEY (`mail_collection_id`) REFERENCES `opush_folder_mapping` (`id`) ON DELETE CASCADE,
        CONSTRAINT `opush_invitation_mapping_event_opush_folder_mapping_id_fkey` FOREIGN KEY (`event_collection_id`) REFERENCES `opush_folder_mapping` (`id`) ON DELETE CASCADE,
	CONSTRAINT `opush_invitation_mapping_event_opush_sync_state_sk_fkey` FOREIGN KEY (`sync_key`) REFERENCES `opush_sync_state` (`sync_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

UPDATE ObmInfo SET obminfo_value = '2.3.12' WHERE obminfo_name = 'db_version';
