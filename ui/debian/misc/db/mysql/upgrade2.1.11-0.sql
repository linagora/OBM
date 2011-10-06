UPDATE CalendarEvent SET calendarevent_owner=(SELECT MIN(userobm_id) FROM UserObm WHERE userobm_perms='admin' and userobm_domain_id=calendarevent_domain_id) WHERE calendarevent_owner not in (select userobm_id from UserObm);
delete from CalendarAlert where calendaralert_user_id not in ( select userobm_id from UserObm);
delete from CalendarAlert where calendaralert_event_id not in ( select calendarevent_id from CalendarEvent);
