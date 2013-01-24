This email was automatically sent by OBM
------------------------------------------------------------------
RECURRENT APPOINTMENT UPDATED !
------------------------------------------------------------------

The recurrent appointment ${new.subject}, initially scheduled from ${old.start} to ${old.recurrenceEnd} at ${old.startTime} - ${old.endTime}, (location : ${old.location}, recurrence kind : ${old.recurrenceKind}),
was updated and will take place from ${new.start} to ${new.recurrenceEnd} at ${new.startTime?string.short} - ${new.endTime?string.short}, (location : ${new.location}, recurrence kind : ${new.recurrenceKind}).


:: To accept this update :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: To refuse this update : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: More information : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
