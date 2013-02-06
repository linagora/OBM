This email was automatically sent by OBM
------------------------------------------------------------------
NEW APPOINTMENT
------------------------------------------------------------------

You are invited to participate to this appointment

from        : ${start?string.medium_short}

to          : ${end?string.medium_short}

subject     : ${subject}

location    : ${location}

organizer   : ${organizer}

created by  : ${creator}


:: To accept this appointment : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this appointment : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: More information about this appointment : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}

