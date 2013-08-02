<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Tiếng Việt                                               //
//     - File     : domain.inc                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$l_domain_name = "Tên miền";
$l_alias = "Alias";
$l_mail_server = "Lựa chọn máy chủ email";
$l_mail_servers = "Mail servers";
$l_sync_servers = "Các server đồng bộ";
$l_solr_servers = "Indexing servers";
$l_imap_frontend_servers = "IMAP frontend servers";
$l_max_quota = "Giá trị tối đa";

// Mail chooser labels
$l_mailboxchoice_least_boxes = "Có ít hơn hộp thư";
$l_mailboxchoice_least_used_disk = "sử dụng không gian đĩa ít hơn ";
$l_mailshares_quota_default = "# mailshares: hạn mức mặc định";
$l_help_mailshares_quota_default = "Xác định hạn mức mặc định cho mailshares trong miền này, giá trị 0 sẽ không có hạn mức.";
$l_mailshares_quota_max = "# mailshares : hạn mức tối đa";
$l_help_mailshares_quota_max = "Xác định hạn mức tối đa cho mailshares trong miền này, giá trị 0 sẽ không có hạn mức.";
$l_insert = "Thêm miền"; 
$l_update = "Cập nhật tên miền";
$l_delete = "Xóa tên miền";
$l_no_display = "Điền vào các trường tìm kiếm và nhấn <Tìm> để lấy lại tên miền"; 
$l_found = " Các miền đã tìm thấy";
$l_no_found = "Không tìm thấy miền!";
$l_delete_ok = "Xóa miền thành công.";
$l_delete_error = "Xoa miền thất bại!";
$l_label_error = "Nhãn không hợp lệ !";
$l_can_delete = "Có thể xóa miền này.";
$l_warn_delete = "LƯU Ý: Tên miền không được tự do.";
$l_cant_delete = "Không thể xóa miền này!"; 
$l_cant_add_domain_single = "Chỉ có một miền có thể được tạo khi OBM được cấu hình trong chế độ mono miền (singleNameSpace)! Hãy coi chừng, chế độ đa lĩnh vực cần một hệ thống công việc quan trọng";
$l_update_state = "Cập nhật trạng thái";
$l_max_users = "# Tối đa người dùng";
$l_max_resources = "# Tối đa tài nguyên";
$l_max_mailshares = "# Tối đa mailshares";
$l_mail_quota = "Hạn mức Email";
$l_address1 = "$l_address 1";
$l_address2 = "$l_address 2";
$l_address3 = "$l_address 3";
$l_group_admin = "Id nhóm quản trị";
$l_group_com = "Id nhóm thương mại";
$l_group_prod = "Id nhóm sản phẩm";
$l_name_error = "Tên không hợp lệ!";
$l_link_user = "Miền có người dùng";
$l_link_user_no = "Miền không có người dùng";
$l_link_host = "Miền có hosts";
$l_link_host_no = "Miền không có hosts";
$l_link_user_mailserver = "Mailserver lưu trữ một số người dùng trong miền";
$l_mailserver_cant_delete = "Mailserver có thể được loại bỏ khỏi miền";
$l_name_in_use = "Những tên hoặc bí danh đã được sử dụng"; 
$l_alias_user_used = "Không thể thay đổi hoặc loại bỏ bí danh được sử dụng trong địa chỉ email";
$l_fill_title = "Bạn phải điền vào các Tiêu đề trước khi gửi."; 
$l_domain_confirm_delete = "LƯU Ý: Bạn muốn xóa một miền! Hoạt động này là không thể phục hồi.\\n Vẫn tiếp tục?";
$l_mailserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên"; 
$l_syncserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên";
$l_solrserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên";
$l_imapfrontendserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên";

$l_mailserver = "Mail server";
$l_imap = 'IMAP';
$l_smtp_in = 'SMTP vào';
$l_smtp_out = 'SMTP ra';
$l_obm_sync = 'OBM sync';
$l_solr_calendar = 'Solr';

$l_samba = 'Windows server';
$l_samba_sid = 'domain SID';
$l_samba_domain = 'Tên miền Windows';
$l_samba_profile = "Đường dẫn mặc định Profile"; 
$l_samba_home = 'Đường dẫn mặc định trang chủ của người dùng sẽ được thay thế bởi người sử dụng đăng nhập)(%u';
$l_samba_drive = 'Trình điều khiển mặc định trang chủ người dùng';
$l_autogen_sid = 'Autogen SID';
$l_samba_sid_error = "SID không hợp lệ, các sid phải có các dạng như sau: S-1-5-21-XXXXXXXXXX-XXXXXXXXX-XXXXXXXXXX"; 
$l_samba_sid_used = 'SID này đã được sử dụng';
$l_samba_domain_error = 'Tên nhóm làm việc không hợp lệ';
$l_samba_domain_used = 'Tên nhóm làm việc này đã được sử dụng';
$l_samba_profile_error = "Đường dẫn mặc định hồ sơ không hợp lệ, nó phải được \\\\computeur\\path\\to\\profile"; 
$l_samba_home_error = "Đường dẫn mặc định trang chủ của người sử dụng không hợp lệ"; 
$l_samba_drive_error = 'Driver mặc định của trang chủ người sử dụng không hợp lệ';
$l_mailshares_quota_error = "Hạn mức mặc định vượt quá hạn mức tối đa!";
$l_max_quota = "Giá trị tối đa";

$l_backup_ftp_servers = 'Sao lưu máy chủ FTP lưu trữ';
$l_header_select_backupftpserver = 'Chọn FTP server';
$l_backupftpserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên";

$l_opushfrontendserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên";
$l_opush_frontend = 'OPush frontend';
$l_opushserver_warn = "Bạn phải tạo ra một host cho lần đầu tiên";
$l_opush = "OPush";
$l_opush_frontend = "OPush frontend";
