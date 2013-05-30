<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          参与者状态已更新
        </th>
    </tr>
    <tr>
      <td colspan="2">
		${user}已经${participationState}了计划在${startDate}开始的事件${subject}。
      </td>
    </tr>
    <tr>
	  <td style="text-align:right;padding-right:1em;">评论</td><td style="font-weight:bold;">${comment}</td>
    </tr>
    <tr>
        <td style="text-align:left;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">细节</a>
        </td>
    </tr>
</table>
