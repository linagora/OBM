此邮件由OBM自动发送
------------------------------------------------------------------
约会已更新！
------------------------------------------------------------------

初步计划从${old.start}到${old.end}(地点: ${old.location})的约会${new.subject}
将被更改为从${new.start}到${new.end}(地点: ${new.location})。


::要接受此更新:
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

::要拒绝此更新: 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

::更多信息: 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
