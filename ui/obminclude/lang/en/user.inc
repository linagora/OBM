<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : user.inc                                                 //
// 1999-01-24 Florent Goalabre - Last update 2001-09-22 PB                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_back = "Back";
$l_none = "---";
$l_user_name = "User";
$l_user_perms = "Authorization";
$l_password = "Password";
$l_all = "All";
$l_find = "Find";
$l_lastaccess = "Last access";
$l_email = "@M";
$l_associated_contact = "Associated Contact";
$l_perms_user = "User";
$l_perms_editor = "Editor";
$l_perms_admin = "Administrator";
$l_check_usercreate = " entries owned by this user.";
$l_check_userupdate = " entries last updated by this user.";
$l_no_user = "No user fields in this table";

$l_invalid_data = "Invalid data";

$l_last_deal="Last deal";
$l_last_company = "Last company";
$l_last_contact = "Last contact";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["userobm_username"] = $l_user_name;
$fieldnames["userobm_perms"] = $l_user_perms;
$fieldnames["userobm_timelastaccess"] = $l_lastaccess;
$fieldnames["userobm_email"] = $l_email;

// Titres
$l_user = "User";

// Actions
$l_insert = "Insert the User";
$l_update = "Update the User";
$l_checkdelete = "Check or Delete the User";
$l_reset = "Reset user's preferences";
$l_delete = "Delete the User";
$l_confirm= "Confirm the action";

// Messages
$l_no_display = "Fill the search fields and press < Find > to retrieve the users";
$l_found = "users found.";
$l_no_found = "No user found matching the query !";
$l_delete_ok = "The user has been deleted successfully.";
$l_delete_error = "The deletion of the user has failed !";
$l_insert_ok = "The user has been inserted successfully.";
$l_insert_error = "The insertion of the user has failed !";
$l_update_ok = "The user has been updated successfully.";
$l_update_error = "The update of the user has failed !";
$l_reset_ok = "User's preferences reseted to default values";
$l_query_error = "Error while executing the query";
$l_username_error = "Invalid user name !";
$l_password_error = "Invalid password !";
$l_email_error = "Invalid email";
$l_err_user_contact = "This contact is already asigned to these users";
$l_exist_error = "already exists";
$l_check_sameuser = "These users seem similar to this new entry !";

// JavaScript Messages (specials charcaters must be escaped whith \ -> \\)
$l_fill_username = "You have to fill the user before submitting.";
$l_fill_password = "You have to fill the password before submitting.";
$l_delete_confirm = "Really delete the user?";
$l_j_check_email = "The email adress is not valid.";

// Specifics fields
// MySQL Date and time format for display in the deal detail screen
$l_date_format = "%Y-%m-%d %H:%i:%s";

