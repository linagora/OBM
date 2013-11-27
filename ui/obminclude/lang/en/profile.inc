<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                   //
//     - File     : profile.inc                                              //
// 2008-09-12 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile.inc 2917 2008-05-31 15:08:08Z pierre $ //
///////////////////////////////////////////////////////////////////////////////

$l_profile_name_error = "Profile name is invalid, please enter from 1 to 64 alphanumerical characters or '_' '.' '-' !";
$l_profile_name_empty = "You must enter a profile name : from 1 to 64 alphanumerical characters or '_' '.' '-' !";
$l_level_empty = "The field Level is empty. Please enter a positive integer !";
$l_level_managepeers_empty = "The field Manage same level users is empty. Please enter 0 or 1 !";
$l_access_restriction_empty = "The field Access restriction exceptions is empty. Please enter ALLOW_ALL or DENY_ALL !";
$l_profile_level_error = "Error on Level field. Please enter a positive integer !";
$l_profile_level_managepeers_error = "Error on Manage same level users field. Please enter 0 or 1 !";
$l_profile_access_restriction_error = "Error on Access restriction field. Please enter ALLOW_ALL or DENY_ALL !";
$l_profile_admin_realm_error = "Admin realm is invalid, please enter from 1 to 64 alphanumerical characters or '_' '.' '-' separated by space character !";
$l_profile_default_permission_empty = "Default permission error. Please choose at least one permission !";
$l_admin_realm_info = "* Accepted values for Admin realm field are :<blockquote><i>user</i>, <i>delegation</i>, <i>domain</i></blockquote>or a combined succession of these value separated by a space character.";
$l_profile_delete_warning = "Removing this profile is not permitted ! At least one user remain.";
$l_module_name = "Module";
$l_right_default = "By default";
$l_right_read = "Read";
$l_right_write = "Write";
$l_right_own = "Own";
$l_right_read_admin = "Read Adm";
$l_right_write_admin = "Write Adm";
$l_default_permissions = "Default permissions";
$l_general_properties = "Main property";
$l_section_disable = "Disable";
$l_section_enable = "Enable";
$l_active_section = "Enable sections";
$l_section_all = "All";
$l_section_none = "None";
$l_found = "profiles found.";
$l_no_found = "No profile matching the query has been found !"; 
$l_user = "Read";
$l_editor = "Write";
$l_admin = "Administration";
$l_modules_right = "Modules rights";
$l_sections_right = "Section display";
$l_default_settings = "Default settings"; 
$l_specific_settings = "Specific settings";
$l_informations = "Information"; 
$l_add_module_rights = "Add specific rights for a module"; 
$l_add_section_enabled = "Add specific settings for a section"; 
$l_default_right = "Default status of the public checkbox right in right management screens";
$l_checked = 'Checked';
$l_unchecked = 'Unchecked';
$l_checked_disabled = 'Checked disabled';
$l_level = "Level";
$l_help_level = "A user with this profile will not be allowed to modify users with a profile having a lesser level than his"; 
$l_level_managepeers = "Manage same level users";
$l_help_level_managepeers = "If this option is enabled, a user with this profile will be allowed to modify users profiles having the same level as his"; 
$l_access_restriction = "Access restriction";
$l_help_access_restriction = "Define the default security policy for network connection for this profile";
$l_user_results_limit = "Maximum number of results in the user search";
$l_help_user_results_limit = "Restricts the maximum number of results in the user search to a given limit, leave blank for no limit";
$l_access_exeptions = "Access restriction exceptions";
$l_help_access_exceptions = "Exeption for the network default security policy (IP or IP range)";
$l_admin_realm = "Administration realm";
$l_help_admin_realm = "When a user with this profile will execute a system update, the realm of the update will be defined by this option."; 
$l_deny_connect = 'Deny connection by default';
$l_allow_connect = 'Allow connection by default';
$l_none_f = 'None';
$l_lower = 'The weakest';
$l_higher = "The strongest"; 
$l_realm_user = 'His own modifications';
$l_realm_delegation = 'All modifications in his delegation';
$l_realm_domain = "All domain modifications"; 
$l_profile_exists= "This profile name is already used";
$l_access_exceptions = "Access exceptions "; 
$l_section_enabled = "Section enabled "; 
$l_mail_quota_max= "Maximum quota email";
$l_help_mail_quota_max = "Define the max quota for email user with this profil, Value 0 is no quota.";
$l_mail_quota_default= "Default quota email";
$l_help_mail_quota_default = "Define the default quota for email user with this profil, Value 0 is no quota.";
$l_mail_quota_error = "Default quota exceeds maximum quota !";
$l_quota_none = "none";
$l_max_quota = 'Max value';

$l_profile_public_access="Public access";
$l_profile_public_read="Public read";
$l_profile_public_write="Public write";
