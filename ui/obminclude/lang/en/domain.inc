<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : domain.inc                                               //
// 2003-05-17 AliaSource                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$l_domain_name = "Domain name";
$l_alias = "Alias";
$l_mail_server = "Mail server choice";
$l_mail_servers = "Mail servers";
$l_sync_servers = "Synchronization servers";
$l_solr_servers = "Indexing servers";

// Mail chooser labels
$l_mailboxchoice_least_boxes = "having less mailboxes";
$l_mailboxchoice_least_used_memory = "using less memory";

// Actions
$l_insert = "Insert the domain";
$l_update = "Update the domain";
$l_delete = "Delete the Domain";

// Messages
$l_no_display = "Fill the search fields and press < Find > to retrieve the domains";
$l_found = " domain(s) found";
$l_no_found = "No domain found !";
$l_delete_ok = "The domain has been deleted successfully.";
$l_delete_error = "The deletion of the domain has failed !";
$l_label_error = "Invalid label !";
$l_can_delete = "The domain can be deleted.";
$l_cant_delete = "The domain can't be deleted !";
$l_cant_add_domain_single = "Only one domain can be created when OBM is configured in mono domain mode (singleNameSpace) ! Beware, going multi-domain mode needs an important system work";

$l_update_state = "Update status";
$l_max_users = "# users max";
$l_max_resources = "# resources max";
$l_max_mailshares = "# mailshares max";
$l_mail_quota = "Email quota";
$l_address1 = "$l_address 1";
$l_address2 = "$l_address 2";
$l_address3 = "$l_address 3";
$l_group_admin = "Admin group id";
$l_group_com = "Commercial group id";
$l_group_prod = "Production group id";

$l_name_error = "Invalid name!";
$l_link_user = "The domain has users";
$l_link_user_no = "The domain has no users";
$l_link_host = "The domain has hosts";
$l_link_host_no = "The domain has no hosts";
$l_link_user_mailserver = "Mailserver stores some users in the domain";
$l_mailserver_cant_delete = "Mailserver can be removed from the domain";
$l_name_in_use = "Those name or aliases are already in use";
$l_alias_user_used = "Cannot change or remove aliases used in email adress";
// JavaScript Messages
$l_fill_title = "You have to fill the Title before submitting.";
$l_domain_confirm_delete = "CAUTION : You will delete a profile ! This operation is irreversible.\\nContinue anyway ?";

$l_mailserver_warn = "You have to create an host of this kind first";
$l_syncserver_warn = "You have to create an host of this kind first";
$l_mailserver = "Mail server";
$l_imap = 'IMAP';
$l_smtp_in = 'SMTP in';
$l_smtp_out = 'SMTP out';
$l_obm_sync = 'OBM sync';
$l_obm_solr = 'Solr';

$l_samba = 'Windows server';
$l_samba_sid = 'domain SID';
$l_samba_domain = 'Windows domain name';
$l_samba_profile = 'Profil default path';
$l_samba_home = 'User home default path (%u will be replaced by user login)';
$l_samba_drive = 'User home default drive';
$l_autogen_sid = 'Autogen SID';

$l_samba_sid_error = 'Invalid SID, the sid must have the followinf form : S-1-5-21-XXXXXXXXXX-XXXXXXXXX-XXXXXXXXXX';
$l_samba_sid_used = 'This SID is already in use';
$l_samba_domain_error = 'Invalid workgroup name';
$l_samba_domain_used = 'This workgroup name is already in use';
$l_samba_profile_error = 'Invalid profil default path, it should look like \\\\computeur\\path\\to\\profile';
$l_samba_home_error = 'Invalid users home default path'; 
$l_samba_drive_error = 'Invalid users home default drive';
