Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous ${new.subject}, initialement prévu du ${old.start} au ${old.end}, (lieu : ${old.location}),
a été modifié et se déroulera du ${new.start} au ${new.end}, (lieu : ${new.location}).

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}


:: Pour accepter les modifications :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED
