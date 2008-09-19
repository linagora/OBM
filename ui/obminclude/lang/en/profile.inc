<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                   //
//     - File     : profile.inc                                              //
// 2008-09-12 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile.inc 2917 2008-05-31 15:08:08Z pierre $ //
///////////////////////////////////////////////////////////////////////////////

// Properties label
$l_level = "Level";
$l_level_managepeers = "Manage same level users";
$l_access_restriction = "Access restriction";
$l_access_exeptions = "Access restriction exceptions";
$l_admin_realm = "Administration realm";

// Errors message
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

// Infos
$l_admin_realm_info = "* Accepted values for Admin realm field are :<blockquote><i>user</i>, <i>delegation</i>, <i>domain</i></blockquote>or a combined succession of these value separated by a space character.";