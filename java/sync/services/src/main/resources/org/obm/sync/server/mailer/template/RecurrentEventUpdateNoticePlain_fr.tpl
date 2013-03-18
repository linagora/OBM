Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS RÉCURRENT MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous récurrent ${old.subject}, initialement prévu du ${old.start?date} au ${old.recurrenceEnd} de ${old.startTime?string.short} à ${old.endTime?string.short}, (lieu : ${old.location}, type de récurrence : ${old.recurrenceKind}), a été modifié :

du            : ${new.start?date}

au            : ${new.recurrenceEnd}

heure         : ${new.startTime?string.short} - ${new.endTime?string.short}

recurrence    : ${new.recurrenceKind}

sujet         : ${new.subject}

lieu          : ${new.location}

organisateur  : ${new.organizer}

créé par      : ${new.creator}

participant(s): ${new.attendees}


::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications.

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
