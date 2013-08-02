<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Cuộc hẹn được cập nhật
        </th>
    </tr>
    <tr>
        <td colspan="2">
Cuộc hẹn  ${new.subject}, được dự kiến từ ${old.start} đến ${old.end}, (địa điểm : ${old.location}),
đã được cập nhật: </td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%;padding-right:1em;">Chủ đề</td><td style="font-weight:bold;">${new.subject}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Từ</td><td style="font-weight:bold;">${new.start}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Đến</td><td style="font-weight:bold;">${new.end}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Địa điểm</td><td style="font-weight:bold;">${new.location}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Người tổ chức</td><td style="font-weight:bold;">${new.organizer}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Người tạo</td><td style="font-weight:bold;">${new.creator}</td>
    </tr>
    <tr valign="top">
        <td style="text-align:right;padding-right:1em;">Người tham dự</td><td style="font-weight:bold;">${new.attendees}</td>
    </tr>
    <tr>
    	<td colspan="2">
      		<strong>NB : </strong>Nếu bạn đang sử dụng Thunderbird extension hoặc ActiveSync, bạn phải đồng bộ hóa để xem cuộc hẹn này.
    	</td>
	</tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">Thông tin chi tiết</a>
        </td>
    </tr>
</table>
