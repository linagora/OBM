UPDATE ObmInfo SET obminfo_value = '2.3.19-pre' WHERE obminfo_name = 'db_version';

DELETE FROM opush_sync_mail;
ALTER TABLE opush_sync_mail ADD COLUMN timestamp TIMESTAMP NOT NULL;

CREATE TABLE opush_sync_deleted_mail (
        collection_id   INTEGER NOT NULL REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
        device_id       INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
        mail_uid        INTEGER NOT NULL,
        timestamp       TIMESTAMP NOT NULL
);

UPDATE ObmInfo SET obminfo_value = '2.3.19' WHERE obminfo_name = 'db_version';
