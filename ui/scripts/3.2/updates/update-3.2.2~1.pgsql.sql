BEGIN;

CREATE INDEX opush_synced_item_item_id_idx ON opush_synced_item (item_id);

COMMIT;
