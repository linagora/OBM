<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Tiếng Việt                                                   //
//     - File     : document.inc                                             //
// 2003-08-21 Rande Mehdi
//////////////////////////////////////////////////////////////////////////////
// $Id$
//////////////////////////////////////////////////////////////////////////////

$l_author = "Tác giả";
$l_createtime = "Thời gian tạo";
$l_extension = "Phần mở rộng";
$l_mime_label = "Nhãn ";
$l_mime_manage = "Quản lý loại MIME";
$l_mime_exist = "Loại MIME hiện có";
$l_mime_new = "Loại MIME mới";
$l_mimetype = "Loại MIME";
$l_mime_delete =  "Xóa";
$l_path = "Đường dẫn";
$l_title = "Tiêu đề";
$l_updatetime = "Thời điểm cập nhật";
$l_document_display = "Hiển thị tùy chọn cho danh sách tài liệu";
$l_bytes = "bytes";
$l_private = "Riêng tư";
$l_header_new_dir = "Thư mục mới";
$l_dir = "Thư mục";
$l_add_dir = "Tạo thư mục";
$l_update_dir = "Cập nhật thư mục";
$l_new_file = "File để gửi";
$l_link = "HTTP Link";
$l_links = "HTTPS Link";
$l_existing_file = "File đã gửi";
$l_kind = " Loại tài liệu"; 
$l_auto = "Giá trị mặc định";
$l_consult = "Tham khảo tài liệu";
$l_url = "URL";
$l_events = "Sự kiện được liên kết";
$l_add = "Bổ sung tài liệu";
$l_noupdate_file = "Không cập nhật file";
$l_update_file = "Cập nhật file";
$l_have_link = "Tài liệu này được liên kết đến các đối tượng khác nhau"; 
$l_found = "Các tài liệu được tìm thấy";
$l_no_found = "Không có tài liệu phù hợp với truy vấn đã được tìm!"; 
$l_no_display = "Điền vào các trường tìm kiếm và nhấn <Tìm> để lấy tài liệu";
$l_mime_link_document = "Các loại định dạng được tham chiếu như"; 
$l_mime_link_document_no = "Không có tài liệu nào được tham chiếu như định dạng MIME";
$l_mime_cant_delete = "Các loại MIME không thể xóa!";
$l_mime_can_delete = "Các loại MIME có thể xóa";
$l_mime_checkdelete = "Kiểm tra hoặc xóa loại MIME";
$l_mime_update = "Cập nhật loại MIME";
$l_mime_insert = "Thêm loại MIME";
$l_mime_insert_ok = "Loại mime đã được đưa vào thành công.";
$l_mime_insert_error = "Thêm định dạng MIME đã thất bại!";
$l_mime_delete_ok = "Loại mime đã được xóa thành công.";
$l_mime_delete_error = "Xoá kiểu MIME đã thất bại!";
$l_mime_update_ok = "Loại mime đã được cập nhật thành công.";
$l_mime_update_error = "Cập nhật Loại Mime đã thất bại!";
$l_header_tree = "Cây";
$l_invalid_link_file = "Không thể tạo ra một bí danh qua một liên kết";
$l_have_link = "Tài liệu này được liên kết với các đối tượng khác nhau";
$l_have_no_link = "Tài liệu này không có liên kết";
$l_browse = "Trình duyệt";
$l_no_document = "Không tìm thấy tài liệu nào";
$l_invalid_url = "URL không đúng";
$l_can_delete = "Tài liệu có thể xóa.";
$l_cant_delete = "Tài liệu không thể xóa!";
$l_create_file_error = "Lỗi tạo tập tin!";
$l_delete_file_error = "Lỗi trong quá trình xóa file!";
$l_err_system_path = "Root và các thư mục Mặc định không thể xóa!";
$l_err_dir_no_empty = "Thư mục không rỗng!";
$l_dir_can_delete = "Các thư mục có thể xóa.";
$l_dir_cant_delete = "Thư mục không thể xóa!";
$l_checkdelete = "Kiểm tra hoặc xóa tài liệu"; 
$l_delete = "Xóa tài liệu"; 
$l_force_delete = "Xóa bằng mọi cách"; 
$l_insert = "Thêm tài liệu"; 
$l_update = "Cập nhật tài liệu"; 
$l_dir_delete = "Xóa thư mục"; 
$l_fill_title = "Bạn phải điền tiêu đề trước khi gửi.";
$l_fill_author = "Bạn cần phải nạp Tác giả trước khi gửi.";
$l_fill_path = "Bạn phải nhập đường dẫn trước khi gửi."; 
$l_j_mime_create = "Bạn phải điền vào tất cả các trường trước khi gửi.";
$l_j_mime_update = "Bạn phải điền vào tất cả các trường trước khi gửi.";
$l_j_mime_delete = "Bạn phải chọn một định dạng MIME";
$l_fill_dir_name = "Bạn phải đặt tên cho thư mục của bạn";
$l_invalid_dir_path = "Đường dẫn chỉ định không tồn tại!"; 
$l_invalid_file = "file không hợp lệ";
$l_invalid_existing_file = "Đường dẫn không phải là một tập tin hợp lệ";
$l_error_dir = "Thư mục không hợp lệ. Kiểm tra đường dẫn và tên.";
$l_size = "Kích thước";
$l_over_quota_error = "Thêm tài liệu này sẽ vượt quá giới hạn được phân bổ của bạn! Để thêm tài liệu này, một số tài liệu riêng tư phải được xóa.";
$l_quota_msg = "Bạn đang sử dụng %s (%s) của %s dành riêng cho tài liệu riêng của bạn";
$l_filesize_units = array('Byte','KB','MB','GB','TB','PB');
$l_err_file_access_forbidden = "Bạn không được phép truy cập tài liệu này";
$l_file_maxsize = "Cảnh báo: kích thước tập tin tối đa được thiết lập ";
