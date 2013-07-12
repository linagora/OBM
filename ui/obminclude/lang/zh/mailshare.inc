<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                   //
//     - File     : mailshare.inc                                            //
///////////////////////////////////////////////////////////////////////////////

$l_mailshare_display = "显示mailshare列表选项"; //"Display options for mailshare lists"
$l_mail_server = "邮件服务器"; //"Mail server"
$l_no_display = "填写搜索字段并点击<查找>搜索mailshares";  //"Fill in the search fields and press < Find > to retrieve the mailshares"
$l_mailshare_found = "个mailshares已找到。"; //"mailshares found."
$l_no_found = "未找到与查询匹配的mailshare！";  //"No mailshare matching the query has been found !"
$l_can_delete = "Mailshare可以被删除";  //"Mailshare can be deleted"
$l_cant_delete = "Mailshare无法被删除";  //"Mailshare cannot be deleted"
$l_name_error = "Mailshare名称无效！ 不能包含空格或特殊字符"; //"Mailshare name not valid ! It cannot contain spaces nor special char"
$l_name_in_use = "Mailshare名称已经被使用！"; //"Mailshare name already used !"
$l_err_mailshare_name_empty = "您必须填写Mailshare名称"; //"Mailshare name must be filled in"
$l_err_mailshare_name_exist = "Mailshare名称已经被使用"; //"Mailshare name already used"
$l_err_mailshare_quota = "无效的mailshare配额！"; //"Invalid mailshare quota !"
$l_err_mailshare_quota_max = "Mailshare配额超出了域设置的最大配额！"; //"Mailshare quota exceeds maximum quota set in domain !"
$l_err_mailshare_email = "无效的mailshare电子邮件地址！"; //"Invalid mailshare E-mail !"
$l_err_mailshare_email_empty = "您必须填写Mailshare的电子邮件地址！"; //"Mailshare E-mail must be filled in !"
$l_err_mailshare_email_exist = "Mailshare电子邮件地址已经被使用！"; //"Mailshare E-mail already used !"
$l_err_mailshare_uemail_exist = "相同的电子邮件地址被使用了多次！"; //"Same E-mail address used several times !"
$l_check_samemailshare = "该mailshare似乎与那个新条目类似";  //"This mailshare seems similar to the new one"
$l_toggle_public_access = "";
$l_toggle_public_read = "此操作将允许所有人都可读取该mailshare。您想继续吗？";  //"This will allow everybody to read the mailshare. Do you want to continue ?"
$l_toggle_public_write = "此操作将允许所有人都可编辑该mailshare。您想继续吗？";  //"This will make this mailshare writable by everybody. Do you want to continue ?"
$l_toggle_public_admin = "此操作将允许所有人都能修改该mailshare的权限。您想继续吗?"; //"This will allow everybody to update this mailshare rights. Do you want to continue ?"

$l_max_quota = "最大值"; //"Max value"

// Backup messages
$l_bytes = "字节"; //"bytes"
$l_no_display = "填写搜索字段并点击<查找>搜索备份"; //"Fill in the search fields and press < Find > to retrieve the backup"
$l_backup_found = "个备份已找到"; //"backup found"
$l_no_found = "未找到与查询匹配的备份！";  //"No backup matching the query has been found!"
$l_restore_ok = "该备份已经还原成功。"; //"The backup has been restored successfully."
$l_restore_error = "该备份还原失败！"; //"The backup restoration has failed !"
$l_err_backup_no_mail = "您无法备份该实体，因为它未启用邮件功能。"; //"You can't backup this entity, because it doesn't have mail enabled."
$l_create_backup_error = "该备份文件创建失败！"; //"The backup file creation has failed !"
$l_delete_file_error = "该备份文件删除失败！"; //"The backup file deletion has failed !"
$l_err_backup_name = "该备份文件名称无效！"; //"The backup file name is not valid !"
$l_err_backup_file = "无法读取该备份文件！";  //"The backup file cannot be read !"
$l_err_backup_dir_not_exist = "该备份目录不存在！"; //"The backup directory does not exist !"
$l_err_backup_dir_not_writable = "该备份目录不可写！"; //"The backup directory is not writable !"
$l_can_delete = "该备份可以被删除。"; //"The backup can be deleted."
$l_cant_delete = "该备份无法被删除！";  //"The backup cannot be deleted !"
$l_can_create = "该备份可以被创建。"; //"The backup can be created."
$l_cant_create = "该备份无法该备份！";  //"The backup cannot be created !"
$l_err_unknown_db = "未知的数据库类型！"; //"Unknown database Type !"
$l_delete = "删除"; //"Delete"
$l_insert = "创建"; //"Create"
$l_restore = "还原"; //"Restore"

$l_restore_mailbox = "邮箱"; //"Mailbox"
$l_restore_calendar = "日历"; //"Calendar"
$l_restore_contact = "联系人"; //"Contacts"
$l_restore_all = "所有"; //"Everything"
$l_backup_complete = "备份成功。"; //"Backup completed successfully."
$l_restore_complete = "还原成功。"; //"Restoration completed successfully."
$l_no_backup_available = "无可用备份"; //"No backup available !"
$l_err_cant_backup = "发生错误，备份无法进行！"; //"An error happend and the backup didn't proceed !"
$l_err_cant_restore = "发生错误，还原失败！"; //"An error occured and the restoration failed !"
$l_err_cant_get_availablebackup = "无法获取可用备份的列表！"; //"Unable to get the list of available backups !"
$l_cant_backup_pending_modifications = "无法做备份。该实体有尚未被确认的修改，请使用系统配置工具进行确认"; //"Unable to do backup. The entity have pending modifications, please use system configuration tools to validate it"
$l_err_obm_satellite_usersystem = "连接obm-sateliite失败：无法获取和使用系统用户信息。"; //"Connexion to obm-satellite fail : Can't get the system user to use."

$l_push_backup_ftp_failed = "该备份还没有被添加到FTP服务器"; //"The backup hasn't been added on the FTP server"
$l_retrieve_from_ftp = "搜索备份"; //"Retrieve backups"
$l_retrieve_from_ftp_success = "所有FTP服务器上的可用备份都已经被搜索出来了"; //"All available backups on the FTP server has been retrieved"/
$l_no_backup_ftp_specified = "该域无指定的FTP服务器。"; //"No FTP server specified for the domain."
$l_retrieve_from_backup_ftp = "点击下面的按钮来搜索FTP服务器上所有可用的'%s'备份。"; //"Click on the button bellow to retrieve all available backups for '%s', available on the domain FTP server."
$l_unable_to_retrieve_backup = "无法从FTP服务器上搜索归档备份"; //"Unable to retrieve archived backups from the FTP server"

