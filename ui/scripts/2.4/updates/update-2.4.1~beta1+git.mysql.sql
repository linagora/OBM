
--
-- Create opush_folder_sync_state
--
 
BEGIN;
 
CREATE TABLE `opush_folder_sync_state` (
        `id` 			INTEGER NOT NULL auto_increment,
        `sync_key`        	VARCHAR(64) UNIQUE NOT NULL,
        `device_id`       	INTEGER NOT NULL,
        `collection_id`		INTEGER NOT NULL,
        PRIMARY KEY  (`id`),
        KEY `opush_folder_sync_state_device_id_opush_device_id_fkey` (`device_id`),
        CONSTRAINT `opush_folder_sync_state_device_id_opush_device_id_fkey` FOREIGN KEY (`device_id`) REFERENCES `opush_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Insert datas in opush_folder_sync_state
--

CREATE TEMPORARY TABLE main_folder_mapping (
	`collection_id`		INTEGER,
	`collection_min_length`	INTEGER,
	`device_id`		INTEGER
);

INSERT INTO main_folder_mapping (collection_min_length, device_id)
	SELECT MIN(LENGTH(collection)), device_id FROM opush_folder_mapping
	GROUP BY device_id;

UPDATE main_folder_mapping, opush_folder_mapping SET collection_id = id
	WHERE LENGTH(collection) = collection_min_length
	AND main_folder_mapping.device_id = opush_folder_mapping.device_id;

INSERT INTO opush_folder_sync_state (collection_id, device_id, sync_key)
	SELECT main_folder_mapping.collection_id, main_folder_mapping.device_id, sync_key
		FROM main_folder_mapping, opush_sync_state
	WHERE main_folder_mapping.collection_id = opush_sync_state.collection_id
	AND main_folder_mapping.device_id = opush_sync_state.device_id;

INSERT INTO opush_folder_sync_state (collection_id, device_id, sync_key)
	SELECT collection_id, device_id, sync_key FROM opush_sync_state
	WHERE NOT EXISTS (SELECT collection_id FROM opush_folder_sync_state
		WHERE opush_folder_sync_state.device_id = opush_sync_state.device_id);

--
-- Update opush_folder_mapping
--

ALTER TABLE opush_folder_mapping ADD COLUMN folder_sync_state_id INTEGER;

UPDATE opush_folder_mapping, opush_folder_sync_state SET folder_sync_state_id = opush_folder_sync_state.id
	WHERE opush_folder_mapping.device_id = opush_folder_sync_state.device_id
	AND opush_folder_sync_state.id = (SELECT MAX(id) FROM opush_folder_sync_state
		WHERE opush_folder_sync_state.device_id = opush_folder_mapping.device_id);

ALTER TABLE opush_folder_mapping MODIFY folder_sync_state_id INTEGER NOT NULL;
ALTER TABLE opush_folder_mapping ADD KEY `opush_folder_mapping_sync_key_opush_folder_sync_state_id_fkey` (`folder_sync_state_id`);
ALTER TABLE opush_folder_mapping ADD CONSTRAINT `opush_folder_mapping_sync_key_opush_folder_sync_state_id_fkey` FOREIGN KEY (`folder_sync_state_id`) REFERENCES `opush_folder_sync_state` (`id`) ON DELETE CASCADE;
ALTER TABLE opush_folder_mapping DROP FOREIGN KEY opush_folder_mapping_device_id_opush_device_id_fkey;
ALTER TABLE opush_folder_mapping DROP device_id;

--
-- Create opush_backend_folder_sync_mapping
--

CREATE TABLE opush_folder_sync_state_backend_mapping (
        `id` 			INTEGER NOT NULL auto_increment,
        `data_type`        	enum('EMAIL', 'CALENDAR', 'CONTACTS', 'TASKS') NOT NULL,
        `folder_sync_state_id`  INTEGER NOT NULL,
        `last_sync`		TIMESTAMP NOT NULL,
        PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO opush_folder_sync_state_backend_mapping (data_type, folder_sync_state_id, last_sync)
	SELECT 'CALENDAR', opush_folder_sync_state.id, last_sync
		FROM opush_folder_sync_state, opush_folder_mapping, opush_sync_state
	WHERE opush_folder_mapping.collection LIKE '%calendar%'
	AND opush_folder_mapping.folder_sync_state_id = opush_folder_sync_state.id
	AND opush_sync_state.sync_key = opush_folder_sync_state.sync_key;

INSERT INTO opush_folder_sync_state_backend_mapping (data_type, folder_sync_state_id, last_sync)
	SELECT 'CONTACTS', opush_folder_sync_state.id, last_sync
		FROM opush_folder_sync_state, opush_folder_mapping, opush_sync_state
	WHERE opush_folder_mapping.collection LIKE '%contacts%'
	AND opush_folder_mapping.folder_sync_state_id = opush_folder_sync_state.id
	AND opush_sync_state.sync_key = opush_folder_sync_state.sync_key;

INSERT INTO opush_folder_sync_state_backend_mapping (data_type, folder_sync_state_id, last_sync)
	SELECT 'TASKS', opush_folder_sync_state.id, last_sync
		FROM opush_folder_sync_state, opush_folder_mapping, opush_sync_state
	WHERE opush_folder_mapping.collection LIKE '%tasks%'
	AND opush_folder_mapping.folder_sync_state_id = opush_folder_sync_state.id
	AND opush_sync_state.sync_key = opush_folder_sync_state.sync_key;

INSERT INTO opush_folder_sync_state_backend_mapping (data_type, folder_sync_state_id, last_sync)
	SELECT 'EMAIL', opush_folder_sync_state.id, last_sync
		FROM opush_folder_sync_state, opush_folder_mapping, opush_sync_state
	WHERE opush_folder_mapping.collection LIKE '%email%'
	AND opush_folder_mapping.folder_sync_state_id = opush_folder_sync_state.id
	AND opush_sync_state.sync_key = opush_folder_sync_state.sync_key;

ALTER TABLE opush_folder_sync_state_backend_mapping ADD KEY `opush_folder_sync_state_backend_mapping_fkey` (`folder_sync_state_id`);
ALTER TABLE opush_folder_sync_state_backend_mapping ADD CONSTRAINT `opush_folder_sync_state_backend_mapping_fkey` FOREIGN KEY (`folder_sync_state_id`) REFERENCES `opush_folder_sync_state` (`id`) ON DELETE CASCADE;
ALTER TABLE opush_folder_sync_state DROP collection_id;
 
COMMIT;
