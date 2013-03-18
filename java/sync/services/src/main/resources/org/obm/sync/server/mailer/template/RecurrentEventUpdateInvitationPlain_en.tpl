This email was automatically sent by OBM
------------------------------------------------------------------
RECURRENT APPOINTMENT UPDATED !
------------------------------------------------------------------

The recurrent appointment ${old.subject}, initially scheduled from ${old.start?date} to ${old.recurrenceEnd} at ${old.startTime?string.short} - ${old.endTime?string.short}, (location : ${old.location}, recurrence kind : ${old.recurrenceKind}), was updated :

from          : ${new.start?date}

to            : ${new.recurrenceEnd}

time          : ${new.startTime?string.short} - ${new.endTime?string.short}

recurrence    : ${new.recurrenceKind}  

subject       : ${new.subject}

location      : ${new.location}

organizer     : ${new.organizer}

created by    : ${new.creator}

attendee(s)   : ${new.attendees}


:: To accept this update :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this update : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: More information : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
