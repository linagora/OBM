<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : domain.inc                                               //
// 2003-05-17 AliaSource                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$l_domain_name = "域名"; //"Domain name"
$l_alias = "别名"; //"Alias"
$l_mail_server = "选择邮件服务器"; //"Mail server choice"
$l_mail_servers = "邮件服务器"; //"Mail servers"
$l_sync_servers = "同步服务器"; //"Synchronization servers"
$l_solr_servers = "索引服务器"; //"Indexing servers"
$l_imap_frontend_servers = "IMAP前端服务器"; //"IMAP frontend servers"
$l_ldap_servers = "LDAP servers";
$l_max_quota = "最大值"; //"Max value"

// Mail chooser labels
$l_mailboxchoice_least_boxes = "管理较少的邮箱"; //"having less mailboxes"
$l_mailboxchoice_least_used_disk = "使用较少的磁盘空间"; // "using less disk space"
$l_mailshares_quota_default = "# mailshares : 默认配额"; //"# mailshares : default quota"
$l_help_mailshares_quota_default = "在此域定义mailshares默认配额，0值为无配额。"; //"Define the default quota for mailshares in this domain, Value 0 is no quota."
$l_mailshares_quota_max = "# mailshares : 最大配额"; //"# mailshares : maximum quota"
$l_help_mailshares_quota_max = "在此域定义mailshares最大配额，0值为无配额"; //"Define the max quota for mailshares in this domain, Value 0 is no quota."
$l_insert = "添加域";  //"Insert domain"
$l_update = "更新域"; //"Update the domain"
$l_delete = "删除域"; //"Delete the Domain"
$l_no_display = "填写搜索字段，并点击<查找>来搜索域";  //"Fill in the search fields and press < Find > to retrieve the domains"
$l_found = " 个域已找到"; //" domain(s) found"
$l_no_found = "未找到域！"; //"No domain found !"
$l_delete_ok = "该域删除成功。"; //"The domain has been deleted successfully."
$l_delete_error = "该域删除失败！"; //"The deletion of the domain has failed !"
$l_label_error = "无效标签！"; //"Invalid label !"
$l_can_delete = "该域可以被删除。"; //"The domain can be deleted."
$l_warn_delete = "警告: 该域正在使用中。"; //"CAUTION : Domain are not free."
$l_cant_delete = "该域无法被删除！";  //"The domain cannot be deleted !"
$l_cant_add_domain_single = "当OBM配置处于单域模式（singleNameSpace）时只能创建一个域！要注意，多域模式需要一个重要的系统工作"; //"Only one domain can be created when OBM is configured in mono domain mode (singleNameSpace) ! Beware, going multi-domain mode needs an important system work"
$l_update_state = "更新状态"; //"Update status"
$l_max_users = "# 用户的最大值"; //"# users max"
$l_max_resources = "# 资源的最大值"; //"# resources max"
$l_max_mailshares = "# mailshares最大值"; //"# mailshares max"
$l_mail_quota = "邮箱配额最大值"; //"# mailshares max"
$l_address1 = "$l_address 1"; //"$l_address 1"
$l_address2 = "$l_address 2"; //"$l_address 2"
$l_address3 = "$l_address 3"; //"$l_address 3"
$l_group_admin = "管理组ID"; //"Admin group id"
$l_group_com = "商业组ID"; //"Commercial group id"
$l_group_prod = "生产组ID"; //"Production group id"
$l_name_error = "无效名称！"; //"Invalid name!"
$l_link_user = "该域存在用户"; //"The domain has users"
$l_link_user_no = "该域不存在用户"; //"The domain has no users"
$l_link_host = "该域存在主机"; //"The domain has hosts"
$l_link_host_no = "该域不存在主机"; //"The domain has no hosts"
$l_link_user_mailserver = "邮件服务器在此域存储了一些用户"; //"Mailserver stores some users in the domain"
$l_mailserver_cant_delete = "邮件服务器可以在该域被删除"; //"Mailserver can be removed from the domain"
$l_name_in_use = "这些名称或别名已在使用中";  //"Those names or aliases are already in use"
$l_alias_user_used = "无法更改或删除已应用于电子邮件地址中的别名"; //"Cannot change or remove aliases used in email adress"
$l_fill_title = "您必须填写标题，然后提交。";  //"You have to fill in the Title before submitting."
$l_domain_confirm_delete = "注意：您将删除域！此操作是不可逆的。\\n确定继续吗？"; "CAUTION : You will delete a domain ! This operation is irreversible.\\nContinue anyway ?"/
$l_mailserver_warn = "您首先必须创建一个这种类型的主机";  //"You have to create a host of this kind first"
$l_syncserver_warn = "您首先必须创建一个这种类型的主机"; //"You have to create an host of this kind first"
$l_solrserver_warn = "您首先必须创建一个这种类型的主机"; //"You have to create an host of this kind first"
$l_imapfrontendserver_warn = "您首先必须创建一个这种类型的主机"; // "You have to create an host of this kind first"
$l_ldapserver_warn = "您首先必须创建一个这种类型的主机"; //"You have to create an host of this kind first"

$l_mailserver = "邮件服务器"; //"Mail server"
$l_imap = 'IMAP';  //'IMAP'
$l_smtp_in = 'SMTP进'; //'SMTP in'
$l_smtp_out = 'SMTP出'; //'SMTP out'
$l_obm_sync = 'OBM sync'; //'OBM sync'
$l_solr_calendar = 'Solr'; //'Solr'
$l_ldap = 'LDAP';

$l_samba = 'Windows服务器'; //'Windows server'
$l_samba_sid = '域SID'; // 'domain SID'
$l_samba_domain = 'Windows域名'; //'Windows domain name'
$l_samba_profile = "配置文件默认路径";  //"Profile default path"
$l_samba_home = '用户主目录的默认路径 (%u将被用户登录取代)';  //User home default path (%u will be replaced by user login)'
$l_samba_drive = '用户主目录的默认驱动器'; //'User home default drive'
$l_autogen_sid = '生成SID'; //'Autogen SID'
$l_samba_sid_error = "无效SID, SID必须为以下形式: S-1-5-21-XXXXXXXXXX-XXXXXXXXX-XXXXXXXXXX";  //"Invalid SID, the sid must have the following form : S-1-5-21-XXXXXXXXXX-XXXXXXXXX-XXXXXXXXXX"
$l_samba_sid_used = '该SID已经被使用';  //'This SID is already in use'
$l_samba_domain_error = '无效的工作组名称'; //'Invalid workgroup name'
$l_samba_domain_used = '该工作组名称已经被使用'; //'This workgroup name is already in use'
$l_samba_profile_error = "无效的配置文件默认路径，它应该是 \\\\computeur\\path\\to\\profile";  //"Invalid profile default path, it should be \\\\computeur\\path\\to\\profile"/
$l_samba_home_error = "无效的用户主目录默认路径";  //"Invalid user home default path"
$l_samba_drive_error = '无效的用户主目录默认驱动器'; //'Invalid users home default drive'
$l_mailshares_quota_error = "默认的配额超出最高限额！"; //"Default quota exceeds maximum quota !"
$l_max_quota = "最大值"; //"Max value"

$l_backup_ftp_servers = '备份存储FTP服务器'; //'Backup storage FTP servers'
$l_header_select_backupftpserver = '选择FTP服务器'; //'Select FTP server'
$l_backupftpserver_warn = "您首先必须创建一个这种类型的主机"; //"You have to create an host of this kind first"

$l_opushfrontendserver_warn = "请先创建一个此类型的主机";
$l_opush_frontend = "OPush前台";
$l_opushserver_warn = "请先创建一个此类型的主机";
$l_opush = "OPush";
$l_opush_frontend = "OPush前台";
