--
-- This script add the addressbook.id to opush_folder_mapping.collection for contact based collection
-- That turn paths like "obm:\\zadmin@thilaire.lng.org\contacts\myFolder" into "obm:\\zadmin@thilaire.lng.org\contacts\5-myFolder"
--

BEGIN;

-- Remove devices sync states of users which have duplicates on addressbook.name
DELETE
FROM opush_device
WHERE EXISTS (
	SELECT *
	FROM AddressBook book
	WHERE book.owner = opush_device.owner
	AND EXISTS (
		SELECT book2.id
		FROM AddressBook book2
		WHERE book.id <> book2.id
		AND book.owner = book2.owner
		AND book.name = book2.name )
	HAVING COUNT(*) > 1
);

--
-- Create a temporary column to store which uid will be inserted in the collection path
--
ALTER TABLE `opush_folder_mapping` ADD COLUMN tmp_book_id integer default 0;

--
-- Fill this new column with the expected Addressbook.id
--
-- Mark NULL main contacts folder
UPDATE `opush_folder_mapping` SET tmp_book_id = NULL
WHERE CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) = 3;
-- Subfolder
UPDATE `opush_folder_mapping` SET tmp_book_id = (
	SELECT book.id 
	FROM `AddressBook` book
	WHERE book.owner = (
		SELECT owner
		FROM `opush_device` device
		WHERE device.id = device_id) 
	AND book.name = SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 5), '\\', -1)
	ORDER BY book.id
	LIMIT 1
)
WHERE CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) = 4;
-- SubSubfolder
UPDATE `opush_folder_mapping` SET tmp_book_id = (
	SELECT book.id 
	FROM `AddressBook` book
	WHERE book.owner = (
		SELECT owner
		FROM `opush_device` device
		WHERE device.id = device_id) 
	AND book.name = SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 6), '\\', -2)
	ORDER BY book.id
	LIMIT 1
)
WHERE CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) = 5;

--
-- Update the opush_folder_mapping.collection
--
-- Subsubfolder
UPDATE `opush_folder_mapping`
	SET collection = CONCAT(
		SUBSTRING_INDEX(collection, '\\', 4),
		'\\',
		tmp_book_id,
		'-',
		SUBSTRING_INDEX(collection, '\\', -2)),
	tmp_book_id = 0
	WHERE SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 4), '\\', -1) = 'contacts' -- is contact collection
	AND CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) = 5 -- has folder after obm:\\zadmin@thilaire.lng.org\contacts\bla
	AND tmp_book_id <> 0;
-- Subfolder
UPDATE `opush_folder_mapping`
	SET collection = CONCAT(
		SUBSTRING_INDEX(collection, '\\', 4),
		'\\',
		tmp_book_id,
		'-',
		SUBSTRING_INDEX(collection, '\\', -1)),
	tmp_book_id = 0
	WHERE SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 4), '\\', -1) = 'contacts' -- is contact collection
	AND CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) = 4 -- has folder after obm:\\zadmin@thilaire.lng.org\contacts\bla
	AND tmp_book_id <> 0;
-- Unknown AddressBook
UPDATE `opush_folder_mapping`
	SET collection = CONCAT(
		SUBSTRING_INDEX(collection, '\\', 4),
		'\\0-',
		SUBSTRING_INDEX(collection, '\\', -1)),
	tmp_book_id = 0
	WHERE SUBSTRING_INDEX(SUBSTRING_INDEX(collection, '\\', 4), '\\', -1) = 'contacts' -- is contact collection
	AND CAST((LENGTH(collection) - LENGTH(REPLACE(collection, '\\', ""))) AS UNSIGNED) = 4 -- has folder after obm:\\zadmin@thilaire.lng.org\contacts\bla
	AND tmp_book_id IS NULL;

--
-- Drop the temporary column
--
ALTER TABLE `opush_folder_mapping` DROP COLUMN tmp_book_id;

--
-- Changes related to opush PolicyKey implementation
--
ALTER TABLE opush_sync_perms DROP FOREIGN KEY opush_sync_perms_policy_opush_sec_policy_id_fkey;
ALTER TABLE opush_sync_perms ADD CONSTRAINT opush_sync_perms_policy_opush_sec_policy_id_fkey FOREIGN KEY (policy) REFERENCES opush_sec_policy(id) ON DELETE CASCADE;


COMMIT;
