<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_pref.inc                                              //
//     - Desc : Global preference OBM include file                           //
// 2002-07-31 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

// Update the cookie expires time : now + lifetime or session wide
// but not in txt mode or if headers have already been sent
if ( (! headers_sent()) && ($mode != "txt"))
  $sess->setcookie();

// if prefs have not been loaded (we check the var set_lang) - Login Phase
if (($set_lang == "")  && ($mode != "txt")) {
  // We Load and make session variables from Global and User preferences
  session_load_global_prefs();
  session_load_user_prefs();

  // We update the last login date for the user
  run_query_userobm_update_lastaccess($auth->auth["uid"]);
}

// Display session content if debug level is set
if (($set_debug & $cdg_sess) == $cdg_sess) {
  $dis_session = "<table border=\"1\">
    <tr><th>Session content</th></tr>
    <tr><td>";
  while (list($key, $val) = each ($_SESSION)) {
    $dis_session .= "$key = $val<br />\n";
  }
  $dis_session .= "</td></tr></table>";
  echo $dis_session;
}

// Default settings
$set_lang_default = "en";
$set_theme_default = "standard";
$set_menu_default = $cme_both;
$set_display_default = "no";
$set_rows_default = 10;
$set_date_default = $cda_iso;
$set_commentorder_default = $cco_chro;
$set_debug_default = 0;
$set_cal_interval_default = 2;

if ($set_lang == "") $set_lang = $set_lang_default;
if ($set_theme == "") $set_theme = $set_theme_default;
if ($set_menu == "") $set_menu = $set_menu_default;
if ($set_display == "") $set_display = $set_display_default;
if ($set_rows == "") $set_rows = $set_rows_default;
if ($set_date == "") $set_date = $set_date_default;
if ($set_commentorder == "") $set_commentorder = $set_commentorder_default;
if ($set_debug == "") $set_debug = $set_debug_default;
if ($set_cal_interval == "") $set_cal_interval = $set_cal_interval_default;

if ($last_company == "") $last_company=$last_company_default;
if ($last_deal == "") $last_deal=$last_deal_default;
if ($last_contact == "") $last_contact=$last_contact_default;
if ($last_account == "") $last_account=$last_account_default;
if ($last_invoice == "") $last_invoice=$last_invoice_default;
if ($last_payment == "") $last_payment=$last_payment_default;
if ($last_contract == "") $last_contract=$last_contract_default;
if ($last_incident == "") $last_incident=$last_incident_default;

///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Theme.                                           //
///////////////////////////////////////////////////////////////////////////////
$theme_file = "$obminclude/themes/$set_theme/theme.inc";
if (file_exists("$path/../$theme_file")) {
  include("$theme_file");
} else {
  include ("$obminclude/themes/$set_theme_default/theme.inc");
}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Language File, depending from which module this  //
// page is called and the selected language.                                 //
///////////////////////////////////////////////////////////////////////////////
$lang_file = "$obminclude/lang/$set_lang/global.inc";
if (file_exists("$path/../".$lang_file)) {
  include("$lang_file");
}

if ($menu != "") {
  $lang_file = "$obminclude/lang/$set_lang/".strtolower($menu).".inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {//if (file_exists("$path/../$obminclude/lang/$set_lang_default/".strtolower($menu).".inc")) {
    include("$obminclude/lang/$set_lang_default/".strtolower($menu).".inc");
  }
}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Display file (global display functrions)         //
///////////////////////////////////////////////////////////////////////////////
$display_include_file = "$obminclude/themes/$set_theme/display.inc";
include("$display_include_file");


// List Export Formats (use theme info so after theme inclusion)
// Format 0 : HTML Stickers
$c_export_format[0]["template"] = false;
$c_export_format[0]["url"] = "addresses.php?fmt=htm&amp;form=label";
$c_export_format[0]["icon"] = $ico_html;
// Format 1 : HTML Table
$c_export_format[1]["template"] = false;
$c_export_format[1]["url"] = "addresses.php?fmt=htm&amp;form=table";
$c_export_format[1]["icon"] = $ico_html;
// Format 2 : RTF Stickers
$c_export_format[2]["template"] = false;
$c_export_format[2]["url"] = "addresses.php?fmt=rtf&amp;form=label";
$c_export_format[2]["icon"] = $ico_rtf;
// Format 3 : Excel data
$c_export_format[3]["template"] = true;
$c_export_format[3]["url"] = "addresses.php?fmt=xls&amp;form=table";
$c_export_format[3]["icon"] = $ico_excel;

//////////////////////////////////////////////////////////////////////////////
// Menu item defintion : Sections
//////////////////////////////////////////////////////////////////////////////
$menus["COM"]    = array('Name' => $l_header_com,
                         'Url'  => "$path/company/company_index.php",
                         'Right'=> $com_read);
$menus["PROD"]   = array('Name' => $l_header_prod,
                         'Url'  => "$path/time/time_index.php",
                         'Right'=> $prod_read);
$menus["COMPTA"] = array('Name' => $l_header_compta,
                         'Url'  => "$path/account/account_index.php",
                         'Right'=> $compta_read);
$menus["USERS"]  = array('Name' => $l_header_users,
                         'Url'  => "$path/settings/settings_index.php",
                         'Right'=> $users_read);
$menus["ADMINS"] = array('Name' => $l_header_admins,
                         'Url'  => "$path/admin/admin_index.php?mode=html",
                         'Right'=> $admins_read);

//////////////////////////////////////////////////////////////////////////////
// Menu item defintion : Modules
//////////////////////////////////////////////////////////////////////////////
if ($section == "COM") {

  $menus["COM"]["COMPANY"] = array('Name'=> $l_header_company,
			           'Ico' => "$ico_company",
			           'Url' => "$path/company/company_index.php",
			           'Right'=> $company_read);
  $menus["COM"]["CONTACT"] = array('Name'=> $l_header_contact,
			           'Ico' => "$ico_contact",
			           'Url' => "$path/contact/contact_index.php",
			           'Right'=> $contact_read);
  $menus["COM"]["DEAL"]	   = array('Name'=> $l_header_deal,
			           'Ico' => "$ico_deal",
  			           'Url' => "$path/deal/deal_index.php",
			           'Right'=> $deal_read);
  $menus["COM"]["LIST"]	   = array('Name'=> $l_header_list,
			           'Ico' => "$ico_list",
			           'Url' => "$path/list/list_index.php",
			           'Right'=> $list_read);
  $menus["COM"]["AGENDA"]  = array('Name'=> $l_header_agenda,
			           'Ico' => "$ico_agenda",
			           'Url' => "$path/agenda/agenda_index.php",
			           'Right'=> $agenda_read);

} elseif ($section == "PROD") {

  $menus["PROD"]["TIME"]     = array('Name'=> $l_header_time,
                                      'Ico' => "$ico_time",
			              'Url' => "$path/time/time_index.php",
			              'Right'=> $time_read);
  $menus["PROD"]["PROJECT"]  = array('Name'=> $l_header_project,
                                      'Ico' => "$ico_project",
			              'Url' => "$path/project/project_index.php",
			              'Right'=> $project_read);
  $menus["PROD"]["CONTRACT"] = array('Name' => $l_header_contract,
			           'Ico' => "$ico_contract",
  			           'Url' => "$path/contract/contract_index.php",
			             'Right'=> $contract_read);
  $menus["PROD"]["INCIDENT"] = array('Name'=> $l_header_incident,
                                     'Ico' => "$ico_incident",
  			      	     'Url' => "$path/incident/incident_index.php",
			             'Right'=> $incident_read);

} elseif ($section == "ADMINS") {

  $menus["ADMINS"]["ADMIN"]      = array('Name'=> $l_header_admin,
                                     'Ico' => "$ico_admin",
			             'Url' => "$path/admin/admin_index.php?mode=html",
			             'Right'=> $admin_read);
  $menus["ADMINS"]["ADMIN_CODE"] = array('Name'=> $l_header_admin_code,
                   'Ico' => "$ico_code",
		   'Url' => "$path/admin_code/admin_code_index.php?mode=html",
		   'Right'=> $admin_code_read);
  $menus["ADMINS"]["ADMIN_PREF"] = array('Name'=> $l_header_admin_pref,
                   'Ico' => "$ico_pref",
                   'Url' => "$path/admin_pref/admin_pref_index.php?mode=html",
                   'Right'=> $admin_pref_read);
  $menus["ADMINS"]["ADMIN_DATA"] = array('Name'=> $l_header_admin_data,
                   'Ico' => "$ico_data",
                   'Url' => "$path/admin_data/admin_data_index.php?mode=html",
                   'Right'=> $admin_data_read);
  $menus["ADMINS"]["ADMIN_LANG"] = array('Name'=> $l_header_admin_lang,
                   'Ico' => "$ico_lang",
                   'Url' => "$path/admin_lang/admin_lang_index.php?mode=html",
                   'Right'=> $admini_lang_read);

} elseif ($section == "COMPTA") {

  $menus["COMPTA"]["ACCOUNT"] = array('Name'=> $l_header_account,
                   'Ico' => "$ico_account",
                   'Url' => "$path/account/account_index.php",
                   'Right'=> $account_read);
  $menus["COMPTA"]["INVOICE"] = array('Name'=> $l_header_invoice,
                   'Ico' => "$ico_invoice",
                   'Url' => "$path/invoice/invoice_index.php",
                   'Right'=> $invoice_read);
  $menus["COMPTA"]["PAYMENT"] = array('Name'=> $l_header_payment,
                   'Ico' => "$ico_payment",
                   'Url' => "$path/payment/payment_index.php",
                   'Right'=> $payment_read);

} elseif ($section == "USERS") {
  $menus["USERS"]["SETTINGS"] = array('Name'=> $l_header_settings,
                                      'Ico' => "$ico_setting",
			              'Url' => "$path/settings/settings_index.php",
			              'Right'=> $settings_read);
  $menus["USERS"]["USER"]     = array('Name'=> $l_header_user,
                                      'Ico' => "$ico_user",
			              'Url' => "$path/user/user_index.php",
			              'Right'=> $user_read);

}
