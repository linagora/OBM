<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Participation : mise à jour !
        </th>
    </tr>
    <tr>
      <td colspan="2">
        ${user} a ${participation} l'événement ${subject} prévu le ${startDate?string.medium_short}.
      </td>
    </tr>
    <tr>
	  <td style="text-align:right;padding-right:1em;">Commentaire</td><td style="font-weight:bold;">${comment}</td>
    </tr>
    <tr>
        <td style="text-align:left;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">Plus de détails</a>
        </td>
    </tr>
</table>
