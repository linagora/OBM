Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS RÉCURRENT MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous récurrent ${new.subject}, initialement prévu du ${old.start} au ${old.recurrenceEnd} de ${old.startTime} à ${old.endTime}, (lieu : ${old.location}, type de récurrence : ${old.recurrenceKind}),
a été modifié et se déroulera du ${new.start} au ${new.recurrenceEnd} de ${new.startTime?string.short} à ${new.endTime?string.short}, (lieu : ${new.location}, type de récurrence : ${new.recurrenceKind}).

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}


:: Pour accepter les modifications :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED
