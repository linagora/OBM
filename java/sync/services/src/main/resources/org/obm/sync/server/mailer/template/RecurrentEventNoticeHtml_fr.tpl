<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Invitation à un événement récurrent
        </th>
    </tr>
    <tr>
        <td colspan="2">Vous êtes invité(e) à participer à ce rendez-vous récurrent</td>
    </tr>
    <tr>
        <td style="text-align:right;width:20%;padding-right:1em;">Sujet</td><td style="font-weight:bold;">${subject}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Du</td><td style="font-weight:bold;">${start?string.medium_short}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Au</td><td style="font-weight:bold;">${recurrenceEnd}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Heure</td><td style="font-weight:bold;">${startTime?string.short} - ${endTime?string.short}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Type de récurrence</td><td style="font-weight:bold;">${recurrenceKind}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Lieu</td><td style="font-weight:bold;">${location}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Organisateur</td><td style="font-weight:bold;">${organizer}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Créé par</td><td style="font-weight:bold;">${creator}</td>
    </tr>
    <tr valign="top">
        <td style="text-align:right;padding-right:1em;">Participant(s)</td><td style="font-weight:bold;">${attendees}</td>
    </tr>
    <tr>
    	<td colspan="2">
    	  <strong>NB : </strong>Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ce nouveau rendez-vous.
    	</td>
	</tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a
          href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">Plus de détails</a>
        </td>
    </tr>
</table>
