<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : people.inc                                               //
// 2008-10-06 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: people.inc 3097 2008-09-22 14:22:46Z benoitc $ //
///////////////////////////////////////////////////////////////////////////////

$l_login = "用户名"; //"Login"
$l_password = "密码"; //"Password"
$l_lastaccess = "上次访问"; //"Last access"
$l_phone2 = "$l_phone 2";
$l_fax2 = "$l_fax 2";
$l_email = "邮箱地址"; //"Email address"
$l_datebegin = "开始时间"; //"Entry date"
$l_dateexp = "到期时间"; //"Expiry date"
$l_exp = "日期（过期前）"; //"Day before expiry"
$l_noexperie = "从不过期"; //"Never expire"
$l_perms_user = "用户"; //"User"
$l_perms_editor = "编者"; //"Editor"
$l_perms_admin = "管理员"; //"Administrator"
$l_infos = "信息"; //"Information"
$l_check_usercreate = " 该用户拥有的条目"; //" entries owned by this user."
$l_check_userupdate = " 最后被该用户更新的条目"; //" entries last updated by this user."
$l_no_user = "该表无用户字段"; //"No user fields in this table"
$l_group = "分组"; //"Group"
$l_delegation_target = "授权管理"; //"Delegation target"
$l_photo = "照片"; //"Photo"
$l_delete_userobmphoto = "删除用户照片"; //"Delete user photo"
$l_delegation = '授权'; //'Delegation'
$l_vacation = '假期'; //'Vacation'
$l_hidden = '隐藏的邮箱'; //'Hidden mailbox'
$l_perms = "已允许"; //"Allowed"
$l_group_member = "分组成员";  //"Group member"
$l_password_type = "加密密码"; //"Password Crypt"
$l_insert = "添加用户"; //"Insert User"
$l_update = "更新用户"; //"Update User"
$l_update_group = "更新该用户组"; //"Update the User groups"
$l_checkdelete = "检查或删除用户"; //"Check or Delete User"
$l_reset = "重置用户首选项为默认模式"; //"Reset user preferences by default"
$l_delete = "删除用户"; //"Delete User"
$l_add = "添加这些用户"; //"Add these users"
$l_no_display = "填写搜索字段，并点击<查找>搜索用户。"; //"Fill in the search fields and press < Find > to retrieve the users."
$l_found = "个用户已找到"; //"users found"
$l_no_found = "未找到与查询匹配的用户！"; //"No user matching the query was found !"
$l_reset_ok = "用户首选项已被重置为默认值"; //"User preferences have been reset to default values"
$l_can_delete = "该用户可以被删除"; //"The user can be deleted."
$l_cant_delete = "该用户无法被删除！"; //"The user cannot be deleted !"
$l_link_timetask = "该用户参与了时间管理的一些项目"; //"The user is referenced on projects in time management"
$l_link_timetask_no = "该用户未参与时间管理的一些项目"; //"The user is not referenced on projects in time management"
$l_login_error = "无效的用户名！"; //"Invalid login!"
$l_login_in_use = "该用户名已被使用！"; //"This login is already used !"
$l_password_empty = "您必须输入一个有效的密码 - 4到12个字符(字母数字或-\$\&~#{([|_`^@);]+=}%!:/.,?<>\\\")！"; // "You must enter a valid password - between 4 and 12 chars (alphanumeric or -\$\&~#{([|_`^@);]+=}%!:/.,?<>\\\") !"
$l_password_error = "密码无效 - 密码必须是4到12个字符(字母数字或-\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !"; //"The password is not valid - between 4 and 12 chars (alphanumeric or -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !"
$l_profile_privilege_error = "没有足够的权限来更新该配置文件级别！";  //"Insufficient privileges to update this profile level !"
$l_last_admin_error = "最后一个管理配置文件无法被更新！"; //"The last admin profile cannot be updated !"
$l_email_error = "无效的邮箱地址！";  //"Invalid email address!"
$l_email_empty = "需要邮箱地址才能激活邮件功能！"; //"An email address must be given to activate the mail !"
$l_email_exist = "该邮箱地址已经被使用！"; //"The email address is already in use !"
$l_uemail_exist = "该邮箱地址存在不止一次！"; //"The email address is present more than once !"
$l_check_sameuser = "这些用户似乎与此新条目类似！"; //"These users seem similar to this new entry !"
$l_exist_error = "该用户名已存在！"; //"A user with that name already exists !"
$l_lname_error = "无效名称！"; //"Invalid Name !"
$l_image_type_error = "照片必须是一个图像文件(gif, jpeg或者png)"; //"The photo must be an image file (gif, jpeg or png)"
$l_image_size_error = "该照片最大尺寸是96px x 96px且大小不能超过100KB"; //"The photo image max size is 96px x 96px and must not exceed 100 kbytes"
$l_add_users = "添加用户"; //"Add users"
$l_fill_login = "提交前，您必须输入一个用户ID。"; //"You must enter a User ID before submitting."
$l_fill_password = "提交前，您必须输入密码。"; //"You must enter the password before submitting."
$l_delete_confirm = "确认删除用户？";  //"Confirm user delete?"
$l_j_check_email = "无效的邮箱地址。请验证域。";  //"Invalid email address. Verify domain."
$l_change_login_confirm = "您已更换用户登录。更换期间该用户必须退出所有的OBM应用程序(OBM，电子邮件客户端，Web邮件，连接)。确认更换用户登录？"; //"You have changed the user login. The user must logout from all OBM applications during the update (OBM, email client, webmail, connector). Confirm user login update?"
$l_people_display = "显示用户列表选项"; //"Display options for user lists"
$l_account_dateexp = "过期时间"; //"Expiry date"
$l_forbidden = "被禁止"; // "Forbidden"

