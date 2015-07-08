<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Tiếng Việt                                                   //
//     - File     : mailshare.inc                                            //
///////////////////////////////////////////////////////////////////////////////

$l_mailshare_display = "Hiển thị tùy chọn cho danh sách thư chia sẻ "; 
$l_mail_server = "Mail server";
$l_no_display = "Điền vào các trường tìm kiếm và nhấn <Tìm> để lấy các thư chia sẻ"; 
$l_mailshare_found = "Thư chia sẻ được tìm thấy.";
$l_no_found = "Không có thư chia sẻ phù hợp với truy vấn đã được tìm!"; 
$l_can_delete = "Thư chia sẻ có thể xóa"; 
$l_cant_delete = "Thư chia sẻ không thể xóa"; 
$l_name_error = 
"Tên thư chia sẻ không hợp lệ! Nó không thể chứa khoảng trắng hay ký hiệu đặc biệt
"; 
$l_name_in_use = "Tên thư chia sẻ đã được sử dụng!";
$l_err_mailshare_name_empty = "Tên thư chia sẻ phải được điền"; 
$l_err_mailshare_name_exist = "Tên thư chia sẻ đã được sử dụng";
$l_err_mailshare_quota = "Giới hạn thư chia sẻ không hợp lệ!";
$l_err_mailshare_quota_max = "Giới hạn Thư chia sẻ vượt quá hạn mức tối đa thiết lập trong miền!";
$l_err_mailshare_email = "Email chia sẻ không hợp lệ!";
$l_err_mailshare_email_empty = "Email chia sẻ  phải được điền!"; 
$l_err_mailshare_email_exist = "Email chia sẻ  đã được dùng!";
$l_err_mailshare_uemail_exist = "Cùng một địa chỉ Email đã sử dụng nhiều lần!";
$l_check_samemailshare = "Thư chia sẻ này có vẻ tương tự như cái mới"; 
$l_toggle_public_access = "";
$l_toggle_public_read = "Điều này sẽ cho phép tất cả mọi người đọc thư chia sẻ . Bạn có muốn tiếp tục?"; 
$l_toggle_public_write = "Điều này sẽ cho phép tất cả mọi người ghi thư chia sẻ . Bạn có muốn tiếp tục?"; 
$l_toggle_public_admin = "Điều này sẽ cho phép tất cả mọi người cập nhật quyền thư chia sẻ . Bạn có muốn tiếp tục?"; 

$l_max_quota = "Giá trị tối đa";

// Backup messages
$l_bytes = "bytes";
$l_no_display = "Điền vào các trường tìm kiếm và nhấn <Tìm> để lấy các bản sao lưu";
$l_backup_found = "Sao lưu được tìm thấy";
$l_no_found = "Không có dự phòng phù hợp với truy vấn đã được tìm!"; 
$l_restore_ok = "Sao lưu đã được phục hồi thành công.";
$l_restore_error = "Sao lưu phục hồi thất bại!";
$l_err_backup_no_mail = "Bạn không thể sao lưu thực thể này, bởi vì nó không có thư kích hoạt.";
$l_create_backup_error = "Việc tạo ra tập tin sao lưu đã thất bại!";
$l_delete_file_error = "Việc xóa tập tin sao lưu đã thất bại!";
$l_err_backup_name = "Tên tập tin sao lưu không hợp lệ!";
$l_err_backup_file = "Các tập tin sao lưu không thể đọc!"; 
$l_err_backup_dir_not_exist = "Thư mục sao lưu không tồn tại!";
$l_err_backup_dir_not_writable = "Thư mục sao lưu không thể ghi!";
$l_can_delete = "Sao lưu có thể xóa.";
$l_cant_delete = "Sao lưu không thể xóa!"; 
$l_can_create = "Sao lưu có thể được tạo.";
$l_cant_create = "Sao lưu không thể được tạo ra!"; 
$l_err_unknown_db = "Không rõ kiểu cơ sở dữ liệu!";
$l_delete = "Xóa";
$l_insert = "Tạo";
$l_restore = "Khôi phục";

$l_restore_mailbox = "Hộp thư";
$l_restore_calendar = "Lịch";
$l_restore_contact = "Liên lạc";
$l_restore_all = "Tất cả";
$l_backup_complete = "Sao lưu hoàn tất thành công.";
$l_restore_complete = "Phục hồi thành công.";
$l_no_backup_available = "Không có sao lưu sẵn có!";
$l_err_cant_backup = "Một lỗi xảy ra và sao lưu đã không được tiến hành!";
$l_err_cant_restore = "Một lỗi xảy ra và phục hồi thất bại!";
$l_err_cant_get_availablebackup = "Không thể lấy danh sách các bản sao lưu sẵn có!";
$l_cant_backup_pending_modifications = "Không thể tạo bản sao lưu. Đơn vị này đã cấp phát chính thức sửa đổi, xin vui lòng sử dụng công cụ cấu hình hệ thống để xác nhận nó";
$l_err_obm_satellite_usersystem = "Kết nối tới OBM vệ tinh thất bại: không thể có người sử dụng hệ thống sử dụng nó.";

$l_push_backup_ftp_failed = "Sao lưu đã không được thêm vào máy chủ FTP";
$l_retrieve_from_ftp = "Lấy bản sao lưu";
$l_retrieve_from_ftp_success = "Tất cả các bản sao lưu sẵn có trên máy chủ FTP đã được lấy ra";
$l_no_backup_ftp_specified = "Không có máy chủ FTP xác định cho miền này.";
$l_retrieve_from_backup_ftp = "Click vào nút dưới đây để lấy tất cả các bản sao lưu sẵn có cho '%s' trên miền máy chủ FTP.";
$l_unable_to_retrieve_backup = "Không thể lấy bản sao lưu lưu trữ từ máy chủ FTP";

