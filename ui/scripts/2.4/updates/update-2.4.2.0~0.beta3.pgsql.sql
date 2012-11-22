--
-- This script add the addressbook.id to opush_folder_mapping.collection for contact based collection
-- That turn paths like "obm:\\zadmin@thilaire.lng.org\contacts\myFolder" into "obm:\\zadmin@thilaire.lng.org\contacts\5-myFolder"
--

BEGIN;

-- Remove devices sync states of users which have duplicates on addressbook.name
DELETE 
FROM opush_device device 
WHERE EXISTS (
	SELECT COUNT(*)
	FROM AddressBook book
	WHERE book.owner = device.owner
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
ALTER TABLE opush_folder_mapping ADD COLUMN tmp_book_id integer default 0;

--
-- Fill this new column with the expected Addressbook.id
--
UPDATE opush_folder_mapping SET tmp_book_id = (
	SELECT book.id 
	FROM AddressBook book
	WHERE book.owner = (
		SELECT owner
		FROM opush_device device
		WHERE device.id = device_id) 
	AND book.name = split_part(collection, '\\', 5)
	ORDER BY book.id
	LIMIT 1
)
WHERE split_part(collection, '\\', 5) <> ''; -- has folder after obm:\\zadmin@thilaire.lng.org\contacts
UPDATE opush_folder_mapping SET tmp_book_id = (
	SELECT book.id 
	FROM AddressBook book
	WHERE book.owner = (
		SELECT owner
		FROM opush_device device
		WHERE device.id = device_id) 
	AND book.name = split_part(collection, '\\', 5) ||
			'\\' ||
			split_part(collection, '\\', 6)
	ORDER BY book.id
	LIMIT 1
)
WHERE split_part(collection, '\\', 5) <> '' -- has folder after obm:\\zadmin@thilaire.lng.org\contacts
AND split_part(collection, '\\', 6) <> ''; -- has folder after obm:\\zadmin@thilaire.lng.org\contacts\bla

--
-- Update the opush_folder_mapping.collection
--
-- Subsubfolder
UPDATE opush_folder_mapping 
	SET collection = 
		'obm:\\\\' || 
		split_part(collection, '\\', 3) || 
		'\\contacts\\' || 
		tmp_book_id || 
		'-' ||
		split_part(collection, '\\', 5)	||
		'\\' ||
		split_part(collection, '\\', 6),
	tmp_book_id = 0
	WHERE split_part(collection, '\\', 4) = 'contacts' -- is contact collection
	AND split_part(collection, '\\', 5) <> '' -- has folder after obm:\\zadmin@thilaire.lng.org\contacts
	AND split_part(collection, '\\', 6) <> '' -- has folder after obm:\\zadmin@thilaire.lng.org\contacts\bla
	AND tmp_book_id <> 0;
-- Subfolder
UPDATE opush_folder_mapping 
	SET collection = 
		'obm:\\\\' || 
		split_part(collection, '\\', 3) || 
		'\\contacts\\' || 
		tmp_book_id || 
		'-' ||
		split_part(collection, '\\', 5)	
	WHERE split_part(collection, '\\', 4) = 'contacts' -- is contact collection
	AND split_part(collection, '\\', 5) <> '' -- has folder after obm:\\zadmin@thilaire.lng.org\contacts
	AND tmp_book_id <> 0;
-- Unknown AddressBook
UPDATE opush_folder_mapping 
	SET collection = 
		'obm:\\\\' || 
		split_part(collection, '\\', 3) || 
		'\\contacts\\' || 
		0 || 
		'-' ||
		split_part(collection, '\\', 5)	
	WHERE split_part(collection, '\\', 4) = 'contacts' -- is contact collection
	AND split_part(collection, '\\', 5) <> '' -- has folder after obm:\\zadmin@thilaire.lng.org\contacts
	AND tmp_book_id IS NULL;

--
-- Drop the temporary column
--
ALTER TABLE opush_folder_mapping DROP COLUMN tmp_book_id;

--
-- Changes related to opush PolicyKey implementation
--
ALTER TABLE opush_sync_perms DROP CONSTRAINT opush_sync_perms_policy_fkey;
ALTER TABLE opush_sync_perms ADD CONSTRAINT opush_sync_perms_policy_fkey FOREIGN KEY (policy) REFERENCES opush_sec_policy ON DELETE CASCADE;


COMMIT;
