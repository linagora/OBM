Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS MODIFIÉ !
------------------------------------------------------------------

Le rendez-vous ${old.subject}, initialement prévu du ${old.start?string.medium_short} au ${old.end?string.medium_short}, (lieu : ${old.location}), a été modifié :

du            : ${new.start?string.medium_short}

au            : ${new.end?string.medium_short}

sujet         : ${new.subject}

lieu          : ${new.location}

organisateur  : ${new.organizer}

créé par      : ${new.creator}

participant(s): ${new.attendees}


::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications.

:: Pour plus de détails : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
