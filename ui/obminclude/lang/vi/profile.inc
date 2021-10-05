<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Tiếng việt                                               //
//     - File     : profile.inc                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile.inc 
///////////////////////////////////////////////////////////////////////////////

$l_profile_name_error = "Tên hồ sơ không hợp lệ, vui lòng nhập từ 1 đến 64 ký tự chữ hoặc '_' '.' '-' !";
$l_profile_name_empty = "Bạn phải nhập một tên hồ sơ: từ 1 đến 64 ký tự chữ hoặc '_' '.' '-' !";
$l_level_empty = "Cấp trường có sản phẩm nào. Xin vui lòng nhập một số nguyên dương !";
$l_level_managepeers_empty = "Trường Quản lý người sử dụng cùng một mức độ có sản phẩm nào. Vui lòng nhập 0 hoặc 1 !";
$l_access_restriction_empty = "Trường Truy cập trường hợp ngoại lệ hạn chế có sản phẩm nào. Vui lòng nhập ALLOW_ALL hoặc DENY_ALL !";
$l_profile_level_error = "Lỗi trên trường Cấp. Xin vui lòng nhập một số nguyên dương !";
$l_profile_level_managepeers_error = "Lỗi trên Quản lý cùng một trường người dùng cấp. Vui lòng nhập 0 hoặc 1 !";
$l_profile_access_restriction_error = "Lỗi trên trường hạn chế truy cập. Vui lòng nhập ALLOW_ALL hoặc DENY_ALL !";
$l_profile_admin_realm_error = "Admin realm is invalid, vui lòng nhập từ 1 đến 64 ký tự chữ hoặc '_' '.' '-' phân cách bằng ký tự không gian !";
$l_profile_default_permission_empty = "Lỗi cho phép mặc định. Hãy chọn ít nhất một phép !";
$l_admin_realm_info = "* Giá trị được chấp nhận cho trường miền quản trị là: <blockquote> <i> người dùng </ i>, <i> đoàn </ i>, <i> miền </ i> </ blockquote> hoặc một chuỗi kết hợp của các giá trị cách nhau bằng một nhân vật không gian.";
$l_profile_delete_warning = "Không có phép loại bỏ hồ sơ này! ! At least one user remain.";
$l_module_name = "Mô-đun";
$l_right_default = "Theo mặc định";
$l_right_read = "Đọc";
$l_right_write = "Viết";
$l_right_own = "Chủ sở hữu";
$l_right_read_admin = "Đọc Adm";
$l_right_write_admin = "Viết Adm";
$l_default_permissions = "Cho phép mặc định";
$l_general_properties = "Tài sản chính";
$l_section_disable = "Vô hiệu hóa";
$l_section_enable = "Cho phép";
$l_active_section = "Cho phép phiên";
$l_section_all = "Tất cả";
$l_section_none = "Không có";
$l_found = "Hồ sơ đã được tìm thấy.";
$l_no_found = "Không có hồ sơ phù hợp với truy vấn đã tìm!"; 
$l_user = "Đọc";
$l_editor = "Viết";
$l_admin = "Quản lý";
$l_modules_right = "Quyền mô-đun";
$l_sections_right = "Phần hiển thị";
$l_default_settings = "Thiết lập mặc định"; 
$l_specific_settings = "Cài đặt cụ thể";
$l_informations = "Thông tin"; 
$l_add_module_rights = "Thêm các quyền cụ thể cho một mô-đun"; 
$l_add_section_enabled = "Thêm cài đặt cụ thể cho một phần"; 
$l_default_right = "Trạng thái mặc định trong quản lý quyền được hiển thị trên màn hình ";
$l_checked = 'Kiểm tra';
$l_unchecked = 'Không được kiểm soát';
$l_checked_disabled = 'Đã kiểm tra và vô hiệu hóa';
$l_level = "Mức";
$l_help_level = "Một người sử dụng với hồ sơ này sẽ không được phép thay đổi người sử dụng với một hồ sơ có một mức độ thấp hơn so với của mình"; 
$l_level_managepeers = "Quản lý người dùng cùng cấp";
$l_help_level_managepeers = "Nếu tùy chọn này được kích hoạt, người sử dụng với hồ sơ này sẽ được phép sửa đổi hồ sơ người sử dụng có cùng một mức độ như mình"; 
$l_access_restriction = "Hạn chế truy cập";
$l_help_access_restriction = "Xác định chính sách bảo mật mặc định cho kết nối mạng cho cấu hình này";
$l_access_exeptions = "ngoại lệ truy cập hạn chế";
$l_help_access_exceptions = "Ngoại lệ cho chính sách an ninh mạng mặc định (dải IP hoặc IP)";
$l_admin_realm = "Administration realm";
$l_help_admin_realm = "Khi hồ sơ này người sử dụng thực hiện cập nhật hệ thống, trường cập nhật sẽ được xác định bằng tùy chọn này."; 
$l_deny_connect = 'Từ chối kết nối mặc định';
$l_allow_connect = 'Cho phép kết nối mặc định';
$l_none_f = 'Không có';
$l_lower = 'Yếu nhất';
$l_higher = "Mạnh nhất"; 
$l_realm_user = 'Tự sửa chính mình';
$l_realm_delegation = 'Sửa tất cả trong đoàn đại biểu';
$l_realm_domain = "Sửa tất cả đổi miền"; 
$l_profile_exists= "Tên hồ sơ này đã được sử dụng";
$l_access_exceptions = "Trường hợp ngoại lệ truy cập"; 
$l_section_enabled = "Phần đã kích hoạt "; 
$l_mail_quota_max= "Giới hạn tối đa email";
$l_help_mail_quota_max = "Xác định giới hạn tối đa cho người sử dụng email với hồ sơ này, giá trị 0 là không có giới hạn.";
$l_mail_quota_default= "Email giới hạn mặc định";
$l_help_mail_quota_default = "Xác định hạn ngạch mặc định cho người sử dụng email với profil này, giá trị 0 là không có giới hạn.";
$l_mail_quota_error = "Giới hạn mặc định vượt quá giới hạn tối đa !";
$l_quota_none = "Không có ";
$l_max_quota = 'Giá trị tối đa';

$l_profile_public_access="Truy cập công khai";
$l_profile_public_read="Đọc công khai";
$l_profile_public_write="Viết công khai";
