	 
--
-- Remove duplicates
--
 
BEGIN;

    CREATE TABLE DeletedEventTmp AS SELECT * FROM DeletedEvent e1 JOIN (SELECT MAX(deletedevent_id) AS unique_deletedevent_id FROM DeletedEvent GROUP BY deletedevent_event_id,deletedevent_user_id) e2 ON e1.deletedevent_id=e2.unique_deletedevent_id;
    DELETE FROM DeletedEvent;
    INSERT INTO DeletedEvent (deletedevent_id, deletedevent_event_id,
        deletedevent_event_ext_id, deletedevent_user_id, deletedevent_origin,
        deletedevent_type, deletedevent_timestamp)
        SELECT deletedevent_id, deletedevent_event_id,
            deletedevent_event_ext_id, deletedevent_user_id,
            deletedevent_origin, deletedevent_type, deletedevent_timestamp
       FROM DeletedEventTmp;
    DROP TABLE DeletedEventTmp; 

    ALTER TABLE DeletedEvent ADD CONSTRAINT deletedevent_uniquekey UNIQUE (deletedevent_event_id, deletedevent_user_id);
COMMIT;
