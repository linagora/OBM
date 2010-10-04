UPDATE ObmInfo SET obminfo_value = '2.3.12-pre' WHERE obminfo_name = 'db_version';

-- store last sync dates
CREATE TABLE opush_invitation_mapping (
	mail_collection_id INTEGER REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
	mail_uid INTEGER,
	event_collection_id INTEGER NOT NULL REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
        event_uid VARCHAR(300),
	status VARCHAR(20),
	dtstamp timestamp without time zone,
	sync_key VARCHAR(64) REFERENCES opush_sync_state(sync_key) ON DELETE CASCADE
);

UPDATE ObmInfo SET obminfo_value = '2.3.12' WHERE obminfo_name = 'db_version';
