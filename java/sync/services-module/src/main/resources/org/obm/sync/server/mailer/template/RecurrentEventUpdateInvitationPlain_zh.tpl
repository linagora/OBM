此邮件由OBM自动发送
------------------------------------------------------------------
周期性约会已更新！
------------------------------------------------------------------

初步计划从${old.start}到${old.recurrenceEnd}的${old.startTime} - ${old.endTime}(地点: ${old.location}，周期类型: ${old.recurrenceKind})的周期性约会${new.subject}
将被更改为从${new.start}到${new.recurrenceEnd}的${new.startTime} - ${new.endTime}(地点: ${new.location}，周期类型: ${new.recurrenceKind})。


::要接受此更新:
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

::要拒绝此更新: 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

::更多信息: 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
