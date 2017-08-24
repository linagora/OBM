<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                   //
//     - File     : mailshare.inc                                            //
///////////////////////////////////////////////////////////////////////////////

$l_mailshare_display = "Display options for mailshare lists"; 
$l_mail_server = "Mail server";
$l_no_display = "Fill in the search fields and press < Find > to retrieve the mailshares"; 
$l_mailshare_found = "mailshares found.";
$l_no_found = "No mailshare matching the query has been found !"; 
$l_can_delete = "Mailshare can be deleted"; 
$l_cant_delete = "Mailshare cannot be deleted"; 
$l_name_error = 
"Mailshare name not valid ! It cannot contain spaces nor special char
"; 
$l_name_in_use = "Mailshare name already used !";
$l_err_mailshare_name_empty = "Mailshare name must be filled in"; 
$l_err_mailshare_name_exist = "Mailshare name already used";
$l_err_mailshare_quota = "Invalid mailshare quota !";
$l_err_mailshare_quota_max = "Mailshare quota exceeds maximum quota set in domain !";
$l_err_mailshare_email = "Invalid mailshare E-mail !";
$l_err_mailshare_email_empty = "Mailshare E-mail must be filled in !"; 
$l_err_mailshare_email_exist = "Mailshare E-mail already used !";
$l_err_mailshare_uemail_exist = "Same E-mail address used several times !";
$l_check_samemailshare = "This mailshare seems similar to the new one"; 
$l_toggle_public_access = "";
$l_toggle_public_read = "This will allow everybody to read the mailshare. Do you want to continue ?"; 
$l_toggle_public_write = "This will make this mailshare writable by everybody. Do you want to continue ?"; 
$l_toggle_public_admin = "This will allow everybody to update this mailshare rights. Do you want to continue ?"; 

$l_max_quota = "Max value";

// Backup messages
$l_bytes = "bytes";
$l_no_display = "Fill in the search fields and press < Find > to retrieve the backup";
$l_backup_found = "backup found";
$l_no_found = "No backup matching the query has been found!"; 
$l_restore_ok = "The backup has been restored successfully.";
$l_restore_error = "The backup restoration has failed !";
$l_err_backup_no_mail = "You can't backup this entity, because it doesn't have mail enabled.";
$l_create_backup_error = "The backup file creation has failed !";
$l_delete_file_error = "The backup file deletion has failed !";
$l_err_backup_name = "The backup file name is not valid !";
$l_err_backup_file = "The backup file cannot be read !"; 
$l_err_backup_dir_not_exist = "The backup directory does not exist !";
$l_err_backup_dir_not_writable = "The backup directory is not writable !";
$l_can_delete = "The backup can be deleted.";
$l_cant_delete = "The backup cannot be deleted !"; 
$l_can_create = "The backup can be created.";
$l_cant_create = "The backup cannot be created !"; 
$l_err_unknown_db = "Unknown database Type !";
$l_delete = "Delete";
$l_insert = "Create";
$l_restore = "Restore";

$l_restore_mailbox = "Mailbox";
$l_restore_calendar = "Calendar";
$l_restore_contact = "Contacts";
$l_restore_all = "Everything";
$l_backup_complete = "Backup completed successfully.";
$l_restore_complete = "Restoration completed successfully.";
$l_no_backup_available = "No backup available !";
$l_err_cant_backup = "An error happend and the backup didn't proceed !";
$l_err_cant_restore = "An error occured and the restoration failed !";
$l_err_cant_get_availablebackup = "Unable to get the list of available backups !";
$l_cant_backup_pending_modifications = "Unable to do backup. The entity have pending modifications, please use system configuration tools to validate it";
$l_err_obm_satellite_usersystem = "Connexion to obm-satellite fail : Can't get the system user to use.";

$l_push_backup_ftp_failed = "The backup hasn't been added on the FTP server";
$l_retrieve_from_ftp = "Retrieve backups";
$l_retrieve_from_ftp_success = "All available backups on the FTP server has been retrieved";
$l_no_backup_ftp_specified = "No FTP server specified for the domain.";
$l_retrieve_from_backup_ftp = "Click on the button bellow to retrieve all available backups for '%s', available on the domain FTP server.";
$l_unable_to_retrieve_backup = "Unable to retrieve archived backups from the FTP server";

