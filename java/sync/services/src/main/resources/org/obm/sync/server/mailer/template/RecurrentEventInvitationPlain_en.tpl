This email was automatically sent by OBM
------------------------------------------------------------------
NEW RECURRENT APPOINTMENT !
------------------------------------------------------------------

You are invited to attend this appointment :

from          : ${start?date}

to            : ${recurrenceEnd}

time          : ${startTime?string.short} - ${endTime?string.short}

recurrence    : ${recurrenceKind}  

subject       : ${subject}

location      : ${location}

organizer     : ${organizer}

created by    : ${creator}

attendee(s)   : ${attendees}


:: To accept this appointment : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this appointment : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: More information about this appointment : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}

