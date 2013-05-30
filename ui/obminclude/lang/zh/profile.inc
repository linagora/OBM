<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                   //
//     - File     : profile.inc                                              //
// 2008-09-12 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile.inc 2917 2008-05-31 15:08:08Z pierre $ //
///////////////////////////////////////////////////////////////////////////////

$l_profile_name_error = "配置文件名无效，请输入1-64的字母数字或'_' '.' '-'！"; //"Profile name is invalid, please enter from 1 to 64 alphanumerical characters or '_' '.' '-' !"
$l_profile_name_empty = "您必须输入一个配置文件名：1-64的字母数字或'_' '.' '-'！"; //"You must enter a profile name : from 1 to 64 alphanumerical characters or '_' '.' '-' !"
$l_level_empty = "等级字段是空的。请输入一个正整数！"; //"The field Level is empty. Please enter a positive integer !"
$l_level_managepeers_empty = "管理同等级用户字段是空的。请输入0或1！"; //"The field Manage same level users is empty. Please enter 0 or 1 !"
$l_access_restriction_empty = "访问受限例外字段是空的。请输入ALLOW_ALL或DENY_ALL !"; //"The field Access restriction exceptions is empty. Please enter ALLOW_ALL or DENY_ALL !"
$l_profile_level_error = "等级字段错误。请输入一个正整数！"; //"Error on Level field. Please enter a positive integer !"
$l_profile_level_managepeers_error = "管理同等级用户字段错误。请输入0或1！"; //"Error on Manage same level users field. Please enter 0 or 1 !"
$l_profile_access_restriction_error = "访问受限字段错误。请输入ALLOW_ALL或DENY_ALL !"; //"Error on Access restriction field. Please enter ALLOW_ALL or DENY_ALL !"
$l_profile_admin_realm_error = "管理范围无效，请输入1-64的字母数字或'_' '.' '-'，使用空格分隔！"; //"Admin realm is invalid, please enter from 1 to 64 alphanumerical characters or '_' '.' '-' separated by space character !"
$l_profile_default_permission_empty = "默认权限错误。请选择至少一个权限！"; //"Default permission error. Please choose at least one permission !"
$l_admin_realm_info = "* 管理范围允许的值为：<blockquote><i>用户</i>, <i>授权</i>, <i>域</i></blockquote>或者是结合这些值并使用空格分隔的继承体。"; //"* Accepted values for Admin realm field are :<blockquote><i>user</i>, <i>delegation</i>, <i>domain</i></blockquote>or a combined succession of these value separated by a space character."
$l_profile_delete_warning = "不允许删除该配置文件！至少保留一个用户。"; //"Removing this profile is not permitted ! At least one user remain."
$l_module_name = "模块"; //"Module"
$l_right_default = "默认"; //"By default"
$l_right_read = "读"; //"Read"
$l_right_write = "写"; //"Write"
$l_right_own = "所有"; //"Own"
$l_right_read_admin = "读管理"; //"Read Adm"
$l_right_write_admin = "写管理"; //"Write Adm"
$l_default_permissions = "默认权限"; //"Default permissions"
$l_general_properties = "主要属性"; //"Main property"
$l_section_disable = "禁用"; //"Disable"
$l_section_enable = "启用"; //"Enable"
$l_active_section = "启用部分"; //"Enable sections"
$l_section_all = "所有"; //"All"
$l_section_none = "一个都不"; //"None"
$l_found = "个配置文件已找到。"; //"profiles found."/
$l_no_found = "未发现与查询匹配的配置文件！"; // "No profile matching the query has been found !"
$l_user = "读"; //"Read"
$l_editor = "写"; //"Write"
$l_admin = "管理"; //"Administration";
$l_modules_right = "模块权限"; //"Modules rights"
$l_sections_right = "部分显示"; //"Section display"
$l_default_settings = "默认设置"; //"Default settings"
$l_specific_settings = "特定设置"; //"Specific settings"
$l_informations = "信息"; //"Information"
$l_add_module_rights = "为一个模块添加特定权限";  //"Add specific rights for a module"
$l_add_section_enabled = "为一个部分添加特定设置"; //"Add specific settings for a section"
$l_default_right = "权限管理屏幕中公共复选框的默认状态"; //"Default status of the public checkbox right in right management screens"
$l_checked = '已检查'; // 'Checked'
$l_unchecked = '未检查'; //'Unchecked'
$l_checked_disabled = '禁止检查'; //'Checked disabled'
$l_level = "等级"; //"Level" 
$l_help_level = "使用该配置文件的用户将不被允许修改使用配置文件等级低于该文件的用户"; //"A user with this profile will not be allowed to modify users with a profile having a lesser level than his"
$l_level_managepeers = "管理同等级用户"; //"Manage same level users"
$l_help_level_managepeers = "如果该选项已启用，那么使用该配置文件的用户将被允许修改使用相同等级配置文件的用户。"; //"If this option is enabled, a user with this profile will be allowed to modify users profiles having the same level as his"
$l_access_restriction = "访问限制"; //"Access restriction"
$l_help_access_restriction = "为该配置文件的网络连接定义默认的安全策略"; //"Define the default security policy for network connection for this profile"
$l_access_exeptions = "访问受限例外"; //"Access restriction exceptions"
$l_help_access_exceptions = "网络默认安全策略例外(IP或IP范围)"; //"Exeption for the network default security policy (IP or IP range)"
$l_admin_realm = "管理范围"; //"Administration realm"
$l_help_admin_realm = "当一个使用该配置文件的用户要执行系统更新时，更新的范围将被该选项定义。"; //"When a user with this profile will execute a system update, the realm of the update will be defined by this option."
$l_deny_connect = '默认拒绝连接'; //'Deny connection by default'
$l_allow_connect = '默认允许连接'; //'Allow connection by default'
$l_none_f = '无'; //'None'
$l_lower = '最弱'; //'The weakest'
$l_higher = "最强";  //"The strongest"
$l_realm_user = '个人修改'; //'His own modifications'
$l_realm_delegation = '其授权内所有的修改'; //'All modifications in his delegation'
$l_realm_domain = "所有的域修改"; //"All domain modifications"
$l_profile_exists= "该配置文件名已经被使用"; //"This profile name is already used"
$l_access_exceptions = "访问例外 "; // "Access exceptions "
$l_section_enabled = "部分已启用"; //"Section enabled "
$l_mail_quota_max= "邮箱的最大配额"; //"Maximum quota email"
$l_help_mail_quota_max = "为使用该配置文件的邮箱用户配置最大配额，0值代表无配额。"; //"Define the max quota for email user with this profil, Value 0 is no quota."
$l_mail_quota_default= "邮箱的默认配额"; //"Default quota email"
$l_help_mail_quota_default = "为使用该配置文件的邮箱用户配置默认配额，0值代表无配额。"; //"Define the default quota for email user with this profil, Value 0 is no quota."
$l_mail_quota_error = "默认配额超出最大配额！"; //"Default quota exceeds maximum quota !"
$l_quota_none = "无"; //"none"
$l_max_quota = '最大值'; //'Max value'

$l_profile_public_access="公共访问"; //"Public access"
$l_profile_public_read="公共读"; //"Public read"
$l_profile_public_write="公共写"; //"Public write"
