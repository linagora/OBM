Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS RÉCURRENT MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous récurrent ${old.subject}, initialement prévu du ${old.start} au ${old.recurrenceEnd} de ${old.startTime?string.short} à ${old.endTime?string.short}, (lieu : ${old.location}, type de récurrence : ${old.recurrenceKind}), a été modifié :

du            : ${new.start}

au            : ${new.recurrenceEnd}

heure         : ${new.startTime?string.short} - ${new.endTime?string.short}

recurrence    : ${new.recurrenceKind}

sujet         : ${new.subject}

lieu          : ${new.location}

organisateur  : ${new.organizer}

créé par      : ${new.creator}

participant(s): ${new.attendees}


:: Pour accepter les modifications :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
