	 
--
-- Remove duplicates
--
 
BEGIN;

    DELETE FROM DeletedEvent WHERE deletedevent_id NOT IN (SELECT MAX(deletedevent_id) FROM DeletedEvent GROUP BY deletedevent_event_id, deletedevent_user_id);

    ALTER TABLE DeletedEvent ADD CONSTRAINT deletedevent_uniquekey UNIQUE (deletedevent_event_id, deletedevent_user_id);

COMMIT;
