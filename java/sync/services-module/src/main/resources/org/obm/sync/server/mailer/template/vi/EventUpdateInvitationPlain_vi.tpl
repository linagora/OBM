Email này được gửi tự động từ OBM 
------------------------------------------------------------------
CUÔC HẸN ĐƯỢC THAY ĐỔI!
------------------------------------------------------------------

Cuộc hẹn  ${new.subject}, dự kiến từ  ${old.start} đến ${old.end}, (địa điểm : ${old.location}),
đã được thay đổi, bắt đầu từ ${new.start} đến ${new.end}, (địa điểm  : ${new.location}).


:: Chấp nhận thay đổi :
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Từ chối thay đổi : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Thông tin chi tiết : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}
