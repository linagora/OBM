Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS RÉCURRENT !
------------------------------------------------------------------

Vous êtes invité(e) à participer à ce rendez-vous :

du            : ${start?date}

au            : ${recurrenceEnd}

heure         : ${startTime?string.short} - ${endTime?string.short}

recurrence    : ${recurrenceKind}

sujet         : ${subject}

lieu          : ${location}

organisateur  : ${organizer}

créé par      : ${creator}

participant(s): ${attendees}


:: Pour accepter : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
