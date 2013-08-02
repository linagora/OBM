Email này được gửi tự động từ OBM 
------------------------------------------------------------------
CUỘC HẸN THEO ĐỊNH KỲ 
------------------------------------------------------------------

Bạn được mời tham dự cuộc hẹn theo định kỳ sau:

Từ             : ${start}

Đến            : ${recurrenceEnd}

Thời gian      : ${startTime} - ${endTime}

Loại định kỳ   : ${recurrenceKind}  

Chủ đề         : ${subject}

Địa điểm       : ${location}

Người tổ chức  : ${organizer}

Người tạo      : ${creator}


:: Chấp nhận cuộc hẹn : 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED

:: Từ chối cuộc hẹn: 
${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED

:: Thông tin chi tiết cuộc hẹn : 
${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}

