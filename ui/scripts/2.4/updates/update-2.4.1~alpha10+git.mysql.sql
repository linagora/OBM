	 
--
-- Update event
--
 
BEGIN;
 
UPDATE Event SET `event_duration`= 0 WHERE `event_duration` < 0 ;
	 
ALTER TABLE Event
ADD CONSTRAINT `duration_check` CHECK `event_duration` >= 0 ;
 
COMMIT;
