BEGIN;

--
-- Fixes mistake in create_obmdb_2.5.pgsql.sql which create opush_synced_item_sync_state_id_fkey 
-- before the script itself (see commit 4fbebf4c5f34d5264388f533565cc4a9af1210a9) 
--
DROP INDEX IF EXISTS opush_synced_item_sync_state_id_fkey;

CREATE INDEX opush_synced_item_sync_state_id_fkey ON opush_synced_item(sync_state_id);

CREATE INDEX opush_sync_state_device_id_collection_id_fkey ON opush_sync_state(device_id, collection_id);

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
SELECT 'set_allday_opacity', 'TRANSPARENT'
WHERE NOT EXISTS (SELECT 1 FROM UserObmPref WHERE userobmpref_option='set_allday_opacity');

COMMIT;
