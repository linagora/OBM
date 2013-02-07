<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Attendee state updated !
        </th>
    </tr>
    <tr>
      <td colspan="2">
		${user} has ${participation} the event ${subject} scheduled on ${startDate?string.medium_short}.
      </td>
    </tr>
    <tr>
	  <td style="text-align:right;padding-right:1em;">Comment</td><td style="font-weight:bold;">${comment}</td>
    </tr>
    <tr>
        <td style="text-align:left;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">Details</a>
        </td>
    </tr>
</table>
