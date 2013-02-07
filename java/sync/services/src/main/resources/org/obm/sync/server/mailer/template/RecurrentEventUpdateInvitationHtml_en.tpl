<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Recurrent appointment updated !
        </th>
    </tr>
    <tr>
        <td colspan="2">
The recurrent appointment ${old.subject}, initially scheduled from ${old.start?date} to ${old.recurrenceEnd?date} at ${old.startTime?string.short} - ${old.endTime?string.short}, (location : ${old.location}, recurrence kind : ${old.recurrenceKind}), was updated :</td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%;padding-right:1em;">Subject</td><td style="font-weight:bold;">${new.subject}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">From</td><td style="font-weight:bold;">${new.start?date}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">To</td><td style="font-weight:bold;">${new.recurrenceEnd}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Time</td><td style="font-weight:bold;">${new.startTime?string.short} - ${new.endTime?string.short}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Recurrence kind</td><td style="font-weight:bold;">${new.recurrenceKind}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Location</td><td style="font-weight:bold;">${new.location}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Organizer</td><td style="font-weight:bold;">${new.organizer}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Created by</td><td style="font-weight:bold;">${new.creator}</td>
    </tr>
    <tr valign="top">
        <td style="text-align:right;padding-right:1em;">Attendee(s)</td><td style="font-weight:bold;">${new.attendees}</td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED">Accept</a>
          <a href="${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED">Decline</a>
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">More informations</a>
        </td>
    </tr>
</table>
