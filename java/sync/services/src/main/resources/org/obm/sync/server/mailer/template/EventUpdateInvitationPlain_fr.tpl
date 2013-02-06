Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous ${old.subject}, initialement prévu du ${old.start?string.medium_short} au ${old.end?string.medium_short}, (lieu : ${old.location}),
a été modifié :

du              : ${new.start?string.medium_short}

au              : ${new.end?string.medium_short}

sujet           : ${new.subject}

lieu            : ${new.location}

organisateur    : ${new.organizer}

créé par        : ${new.creator}

participant(s)	: ${new.attendees}


:: Pour accepter les modifications :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
