<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          周期性约会已更新
        </th>
    </tr>
    <tr>
        <td colspan="2">
初步计划从${old.start}到${old.recurrenceEnd}的${old.startTime} - ${old.endTime}(地点: ${old.location}，周期类型: ${old.recurrenceKind})的约会${new.subject}已更新</td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%;padding-right:1em;">主题</td><td style="font-weight:bold;">${new.subject}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">从</td><td style="font-weight:bold;">${new.start}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">到</td><td style="font-weight:bold;">${new.recurrenceEnd}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">时间</td><td style="font-weight:bold;">${new.startTime} - ${new.endTime}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">周期类型</td><td style="font-weight:bold;">${new.recurrenceKind}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">地点</td><td style="font-weight:bold;">${new.location}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">组织者</td><td style="font-weight:bold;">${new.organizer}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">创建者</td><td style="font-weight:bold;">${new.creator}</td>
    </tr>
    <tr valign="top">
        <td style="text-align:right;padding-right:1em;">参与者</td><td style="font-weight:bold;">${new.attendees}</td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED">接受</a>
          <a href="${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED">拒绝</a>
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">更多信息</a>
        </td>
    </tr>
</table>
