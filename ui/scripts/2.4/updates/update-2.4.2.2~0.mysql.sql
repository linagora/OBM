BEGIN;

UPDATE ObmInfo SET obminfo_value = '2.4.2.2-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE opush_sync_perms ADD COLUMN pending_accept BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE opush_sync_perms ALTER pending_accept DROP DEFAULT;

--
-- Add Table: CommitedOperation: In order to map client_id (as SHA1) to entity_id on elements persisted
--

--
-- Table structure for table CommitedOperation 
--

CREATE TABLE CommitedOperation (
    `commitedoperation_hash_client_id` varchar(44) NOT NULL,
    `commitedoperation_entity_id` integer NOT NULL,
    `commitedoperation_kind` enum('VEVENT','VCONTACT') NOT NULL,
    PRIMARY KEY  (`commitedoperation_hash_client_id`),
    CONSTRAINT `commitedoperation_entity_id_fkey` FOREIGN KEY (`commitedoperation_entity_id`) REFERENCES `Entity` (`entity_id`) ON DELETE CASCADE
);

--
-- Correctes update-2.4.2.0~beta3 migration
--
-- DO NOT REIMPLEMENT THIS MIGRATION STEP IN THE NEW MIGRATION TOOL
--

-- Temp table in order to delete old duplicates after update
CREATE TABLE old_opush_folder_mapping (
       id               SERIAL PRIMARY KEY,
       device_id        INTEGER NOT NULL,
       collection       VARCHAR(255) NOT NULL
);
INSERT INTO old_opush_folder_mapping
	SELECT id, device_id, collection FROM opush_folder_mapping
	WHERE SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 4), '\\', -1) = 'contacts' -- is contact collection
	AND SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 5), '\\', -1) LIKE '%:%'; -- has folder after obm:\\zadmin@thilaire.lng.org\contacts

ALTER TABLE opush_folder_mapping ADD COLUMN old_separator_substring varchar(32) DEFAULT NULL;
ALTER TABLE opush_folder_mapping ADD COLUMN new_separator_substring varchar(32) DEFAULT NULL;

-- Load old subcollection
UPDATE opush_folder_mapping SET old_separator_substring = SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 5), '\\', -1)
	WHERE SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 4), '\\', -1) = 'contacts' -- is contact collection
	AND CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) >= 4; -- has folder after obm:\\zadmin@thilaire.lng.org\contacts
-- Generate new subcollection
UPDATE opush_folder_mapping SET new_separator_substring = REPLACE(old_separator_substring, '-', ':')
	WHERE old_separator_substring IS NOT NULL;
-- Specific update for users collection
UPDATE opush_folder_mapping SET new_separator_substring = '-1:users'
	WHERE old_separator_substring = '0-users';
-- Update collection with new subcollection
UPDATE opush_folder_mapping SET collection = REPLACE(collection, old_separator_substring, new_separator_substring)
	WHERE old_separator_substring IS NOT NULL;

-- Delete duplicates
DELETE FROM opush_folder_mapping
	WHERE old_separator_substring IS NOT NULL
	AND EXISTS (SELECT * FROM old_opush_folder_mapping o
		WHERE opush_folder_mapping.device_id = o.device_id
		AND opush_folder_mapping.collection = o.collection
		AND opush_folder_mapping.id < o.id);

-- Drop temporary elements
ALTER TABLE opush_folder_mapping DROP COLUMN old_separator_substring;
ALTER TABLE opush_folder_mapping DROP COLUMN new_separator_substring;

DROP TABLE old_opush_folder_mapping;

UPDATE ObmInfo SET obminfo_value = '2.4.2.2' WHERE obminfo_name = 'db_version';
COMMIT;

