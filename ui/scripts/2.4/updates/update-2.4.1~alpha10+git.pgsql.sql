	 
--
-- Update event
--
 
BEGIN;
 
UPDATE event SET event_duration = 0 WHERE event_duration < 0 ;
	 
ALTER TABLE event ADD CONSTRAINT duration_check CHECK (event_duration >= 0);
 
COMMIT;
