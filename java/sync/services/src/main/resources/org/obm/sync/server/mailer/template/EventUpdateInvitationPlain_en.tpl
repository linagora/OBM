This email was automatically sent by OBM
------------------------------------------------------------------
APPOINTMENT UPDATED !
------------------------------------------------------------------

The appointment ${old.subject}, initially scheduled from ${old.start?string.medium_short} to ${old.end?string.medium_short}, (location : ${old.location}),
was updated :

from        : ${new.start?string.medium_short}

to          : ${new.end?string.medium_short}

subject     : ${new.subject}

location    : ${new.location}

organizer   : ${new.organizer}

created by  : ${new.creator}

attendee(s) : ${new.attendees}


:: To accept this update :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this update : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: More information : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
