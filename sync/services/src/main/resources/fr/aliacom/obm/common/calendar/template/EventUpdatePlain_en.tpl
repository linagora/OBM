This email was automatically sent by OBM
------------------------------------------------------------------
APPOINTMENT UPDATED !
------------------------------------------------------------------

The appointment ${new.subject}, initially scheduled from ${old.start} to ${old.end}, (location : ${old.location}),
was updated and will take place from ${new.start} to ${new.end}, (location : ${new.location}).


:: To accept this update :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this update : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: More information : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
