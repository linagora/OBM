<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Norwegian                                                //
//     - File     : user.inc                                                 //
// 2000-07-20 Magni Onsoien                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_login = "Brukar";
$l_user_perms = "Autorisering";
$l_lastname = "Lastname";
$l_firstname = "Firstname";
$l_password = "Passord";
$l_lastaccess = "Siste pålogging";
$l_email = "@M";
$l_perms_user = "Brukar";
$l_perms_editor = "Redigere";
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
$l_user = "Brukar";
$l_user_detail = "Brukardetaljar";
$l_user_new = "Ny brukar";
$l_user_insert = "Legg inn brukar";
$l_user_update = "Endre brukar";
$l_user_delete = "Slett brukar";

// Actions
$l_insert = "Legg inn brukaren";
$l_update = "Oppdater brukaren";
$l_delete = "Slett brukaren";

// Messages
$l_no_display = "Fyll inn søkefeltet og trykk < Finn > for å hente ut brukarar";
$l_found = "found (to translate) !";
$l_no_found = "Ingen brukar passa til søket!";
$l_delete_ok = "Brukaren er sletta.";
$l_delete_error = "Brukaren kunne ikkje slettast!";
$l_insert_ok = "Brukaren er lagt til.";
$l_insert_error = "Brukaren kunne ikkje leggast til!";
$l_update_ok = "Brukaren er oppdatert.";
$l_update_error = "Brukaren kunne ikkje oppdaterast!";
$l_query_error = "Problem med å utføre søket.";

$l_exist_error = "finst alt";
$l_login_error = "Ugyldig brukarnamn";
$l_password_error = "Ugyldig passord";
$l_email_error = "Ugyldig mailadresse";

// JavaScript Messages (specials charcaters must be escaped whith \ -> \\)
$l_fill_login = "\\n Du må fylle inn brukaren før du legg inn.";
$l_fill_password = "\\n Du må fylle inn passord før du legg inn.";
$l_delete_confirm = "Slette brukar?";
$lj_check_email = "Mailadressa er ugyldig.";

// Specifics fields
// MySQL Date and time format for display in the deal detail screen
$l_time_format = "%Y-%m-%d %H:%i:%s";
$l_date_format = "%Y-%m-%d %H:%i:%s";

