--
-- Set integer to boolean when necessary
--

-- CalendarEvent

ALTER TABLE calendarevent ALTER COLUMN calendarevent_allday DROP DEFAULT;
ALTER TABLE calendarevent ALTER COLUMN calendarevent_allday TYPE BOOLEAN USING CASE calendarevent_allday WHEN 1 THEN TRUE ELSE FALSE END;
ALTER TABLE calendarevent ALTER COLUMN calendarevent_allday SET DEFAULT FALSE;


