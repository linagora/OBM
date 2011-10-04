<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Appointment Updated
        </th>
    </tr>
    <tr>
        <td colspan="2">
The appointment ${new.subject}, initially scheduled from ${old.start} to ${old.end}, (location : ${old.location}),
was updated and will take place from ${new.start} to ${new.end}, (location : ${new.location}). </td>
    </tr>
    <tr>
    	<td colspan="2">
      		<strong>NB : </strong>If you're using the Thunderbird extension or ActiveSync, you must synchronize to view this update.
    	</td>
	</tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">More informations</a>
        </td>
    </tr>
</table>
