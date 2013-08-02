Email này được gửi tự động từ OBM 
------------------------------------------------------------------
CUỘC HẸN ĐỊNH KỲ ĐƯỢC THAY ĐỔI !
------------------------------------------------------------------

Cuộc hẹn định kỳ ${new.subject}, dự kiến diễn ra từ ngày ${old.start} đến ${old.recurrenceEnd} vào lúc ${old.startTime} - ${old.endTime}, (địa điểm : ${old.location}, loại định kỳ : ${old.recurrenceKind}),
được thay đổi và sẽ diễn ra từ ngày ${new.start} đến  ${new.recurrenceEnd} vào lúc ${new.startTime} - ${new.endTime}, (địa điểm : ${new.location}, loại định kỳ : ${new.recurrenceKind}).


:: Chấp nhận thay đổi :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Từ chối thay đổi : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Thông tin chi tiết : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
