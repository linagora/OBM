<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : German                                                   //
//     - File     : user.inc                                                 //
// 1999-01-24 Florent Goalabre                                               //
// 2001-07    / Fink / created from english version                          //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_login = "Benutzer";
$l_user_perms = "Berechtigung";
$l_lastname = "Lastname";
$l_firstname = "Firstname";
$l_password = "Password";
$l_lastaccess = "Letzter Zugriff";
$l_email = "E-Mail";
$l_perms_user = "Lesen";
$l_perms_editor = "Bearbeiten";
$l_perms_admin = "Administrator";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["userobm_login"] = $l_login;
$fieldnames["userobm_perms"] = $l_user_perms;
$fieldnames["userobm_lastname"] = $l_lastname;
$fieldnames["userobm_firstname"] = $l_firstname;
$fieldnames["userobm_timelastaccess"] = $l_lastaccess;
$fieldnames["userobm_email"] = $l_email;

// Titres
$l_user = "Benutzer";
$l_user_detail = "Benutzer-Daten";
$l_user_new = "Neuer Benutzer";
$l_user_insert = "Benutzer einfügen";
$l_user_update = "Benutzer ändern";
$l_user_delete = "Benutzer löschen";

// Actions
$l_insert = "anlegen Benutzer";
$l_update = "ändern Benutzer";
$l_delete = "löschen Benutzer";

// Messages
$l_found = "Benutzer gefunden";
$l_no_found = "No user found matching the query !";
$l_delete_ok = "The user has been deleted successfully.";
$l_delete_error = "The deletion of the user has failed !";
$l_insert_ok = "The user has been inserted successfully.";
$l_insert_error = "The insertion of the user has failed !";
$l_update_ok = "The user has been updated successfully.";
$l_update_error = "The update of the user has failed !";
$l_query_error = "Error while executing the query";

$l_exist_error = "bereits vorhanden";
$l_login_error = "ungültiger Benutzername!";
$l_password_error = "ungültiges Passwort!";
$l_email_error = "ungültige E-Mail-Adresse";

// JavaScript Messages (specials charcaters must be escaped whith \ -> \\)
$l_fill_login = "\\n You have to fill the login before submitting.";
$l_fill_password = "\\n You have to fill the password before submitting.";
$l_delete_confirm = "Really delete the user?";
$lj_check_email = "The email adress is not valid.";
