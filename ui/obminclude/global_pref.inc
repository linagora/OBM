<script language="php">
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
  // We Load and make session variables from User preferences
  session_load_user_prefs();
  session_load_user_todos();

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

// Specific site global lang file
if ($cgp_site_include) {
  $lang_file = "$obminclude/site/lang/$set_lang/global.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  }
}

// Module lang file
if ($menu != "") {
  $lc_menu = strtolower($menu);
  $lang_file = "$obminclude/lang/$set_lang/$lc_menu.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    $lang_file = "$obminclude/lang/$set_lang_default/$lc_menu.inc";
    if (file_exists("$path/../".$lang_file)) {
      include("$lang_file");
    }
  }

  // Specific site module lang file
  if ($cgp_site_include) {
    $lang_file = "$obminclude/site/lang/$set_lang/$lc_menu.inc";
    if (file_exists("$path/../".$lang_file)) {
      include("$lang_file");
    } else {
      $lang_file = "$obminclude/site/lang/$set_lang_default/$lc_menu.inc";
      //      if (file_exists("$path/../".$lang_file)) {
      //	include("$lang_file");
      //      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Display file (global display functions)          //
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

///////////////////////////////////////////////////////////////////////////////
// Menu item defintion : Sections
///////////////////////////////////////////////////////////////////////////////
if ($cgp_show["section"]["com"]) {
  $sections["COM"] = array('Name' => $l_header_com,
                           'Url'  => $cgp_show["section"]["com"],
                           'Right'=> $cright_read);
}
if ($cgp_show["section"]["prod"]) {
  $sections["PROD"] = array('Name' => $l_header_prod,
                            'Url'  => $cgp_show["section"]["prod"],
                            'Right'=> $cright_read);
}
if ($cgp_show["section"]["compta"]) {
  $sections["COMPTA"] = array('Name' => $l_header_compta,
                              'Url'  => $cgp_show["section"]["compta"],
                              'Right'=> $cright_read);
}
if ($cgp_show["section"]["user"]) {
  $sections["USER"] = array('Name' => $l_header_users,
                            'Url'  => $cgp_show["section"]["user"],
                            'Right'=> $cright_read);
}
if ($cgp_show["section"]["admin"]) {
  $sections["ADMIN"] = array('Name' => $l_header_admins,
                             'Url'  => $cgp_show["section"]["admin"],
                             'Right'=> $cright_read);
}
if ($cgp_show["section"]["aliamin"]) {
  $lurl = "http://aliamin.aliacom.local/aliamin.php";
  $sections["ALIAMIN"] = array('Name' => $l_header_aliamin,
                               'Url'  => $cgp_show["section"]["aliamin"],
                               'Right'=> $cright_read);
}

///////////////////////////////////////////////////////////////////////////////
// Menu item defintion : Modules
///////////////////////////////////////////////////////////////////////////////
if ($section == "COM") {

  if ($cgp_show["module"]["company"]) {
    $menus["COM"]["COMPANY"] = array('Name'=> $l_header_company,
			     'Ico' => "$ico_company",
			     'Url' => "$path/company/company_index.php",
			     'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["contact"]) {
    $menus["COM"]["CONTACT"] = array('Name'=> $l_header_contact,
                             'Ico' => "$ico_contact",
                             'Url' => "$path/contact/contact_index.php",
                             'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["deal"]) {
    $menus["COM"]["DEAL"] = array('Name'=> $l_header_deal,
                                  'Ico' => "$ico_deal",
                                  'Url' => "$path/deal/deal_index.php",
                                  'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["list"]) {
    $menus["COM"]["LIST"] = array('Name'=> $l_header_list,
                                  'Ico' => "$ico_list",
                                  'Url' => "$path/list/list_index.php",
                                  'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["agenda"]) {
    $menus["COM"]["AGENDA"] = array('Name'=> $l_header_agenda,
                                    'Ico' => "$ico_agenda",
                                    'Url' => "$path/agenda/agenda_index.php",
                                    'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["todo"]) {
    $menus["COM"]["TODO"] = array('Name'=> $l_header_todo,
                                  'Ico' => "$ico_todo",
                                  'Url' => "$path/todo/todo_index.php",
                                  'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["publication"]) {
    $menus["COM"]["PUBLICATION"] = array('Name'=> $l_header_publication,
			           'Ico' => "$ico_publication",
			           'Url' => "$path/publication/publication_index.php",
			           'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["statistic"]) {
    $menus["COM"]["STATISTIC"] = array('Name'=> $l_header_statistic,
			           'Ico' => "$ico_statistic",
			           'Url' => "$path/statistic/statistic_index.php",
			           'Right'=> $cright_read);
  }

} elseif ($section == "PROD") {

  if ($cgp_show["module"]["time"]) {
    $menus["PROD"]["TIME"] = array('Name'=> $l_header_time,
                                   'Ico' => "$ico_time",
                                   'Url' => "$path/time/time_index.php",
                                   'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["project"]) {
    $menus["PROD"]["PROJECT"] = array('Name'=> $l_header_project,
                              'Ico' => "$ico_project",
                              'Url' => "$path/project/project_index.php",
                              'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["contract"]) {
    $menus["PROD"]["CONTRACT"] = array('Name' => $l_header_contract,
                               'Ico' => "$ico_contract",
                               'Url' => "$path/contract/contract_index.php",
                               'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["incident"]) {
    $menus["PROD"]["INCIDENT"] = array('Name'=> $l_header_incident,
                               'Ico' => "$ico_incident",
                               'Url' => "$path/incident/incident_index.php",
                               'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["document"]) {
    $menus["PROD"]["DOCUMENT"] = array('Name'=> $l_header_document,
                               'Ico' => "$ico_document",
                               'Url' => "$path/document/document_index.php",
                               'Right'=> $cright_read);
  }

} elseif ($section == "ADMIN") {

  $menus["ADMIN"]["ADMIN"] = array('Name'=> $l_header_admin,
                  'Ico' => "$ico_admin",
                  'Url' => "$path/admin/admin_index.php?mode=html",
                  'Right'=> $cright_read_admin);
  $menus["ADMIN"]["ADMIN_CODE"] = array('Name'=> $l_header_admin_code,
                  'Ico' => "$ico_code",
		  'Url' => "$path/admin_code/admin_code_index.php?mode=html",
		  'Right'=> $cright_read_admin);
  $menus["ADMIN"]["ADMIN_PREF"] = array('Name'=> $l_header_admin_pref,
                  'Ico' => "$ico_pref",
                  'Url' => "$path/admin_pref/admin_pref_index.php?mode=html",
                  'Right'=> $cright_read_admin);
  $menus["ADMIN"]["ADMIN_DATA"] = array('Name'=> $l_header_admin_data,
                  'Ico' => "$ico_data",
                  'Url' => "$path/admin_data/admin_data_index.php?mode=html",
                  'Right'=> $cright_read_admin);
  $menus["ADMIN"]["ADMIN_LANG"] = array('Name'=> $l_header_admin_lang,
                  'Ico' => "$ico_lang",
                  'Url' => "$path/admin_lang/admin_lang_index.php?mode=html",
                  'Right'=> $cright_read_admin);
  $menus["ADMIN"]["ADMIN_REF"] = array('Name'=> $l_header_admin_ref,
                  'Ico' => "$ico_ref",
                  'Url' => "$path/admin_ref/admin_ref_index.php",
                  'Right'=> $cright_read_admin);
  $menus["ADMIN"]["IMPORT"] = array('Name'=> $l_header_import,
                  'Ico' => "$ico_ref",
                  'Url' => "$path/import/import_index.php",
                  'Right'=> $cright_read_admin);

} elseif ($section == "COMPTA") {

  if ($cgp_show["module"]["account"]) {
    $menus["COMPTA"]["ACCOUNT"] = array('Name'=> $l_header_account,
                               'Ico' => "$ico_account",
                               'Url' => "$path/account/account_index.php",
                               'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["invoice"]) {
    $menus["COMPTA"]["INVOICE"] = array('Name'=> $l_header_invoice,
                                'Ico' => "$ico_invoice",
                                'Url' => "$path/invoice/invoice_index.php",
                                'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["payment"]) {
    $menus["COMPTA"]["PAYMENT"] = array('Name'=> $l_header_payment,
                                'Ico' => "$ico_payment",
                                'Url' => "$path/payment/payment_index.php",
                                'Right'=> $cright_read);
  }

} elseif ($section == "USER") {

  if ($cgp_show["module"]["settings"]) {
    $menus["USER"]["SETTINGS"] = array(
                               'Name'=> $l_header_settings,
                               'Ico' => "$ico_setting",
		               'Url' => "$path/settings/settings_index.php",
			       'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["user"]) {
    $menus["USER"]["USER"] = array('Name'=> $l_header_user,
                                   'Ico' => "$ico_user",
                                   'Url' => "$path/user/user_index.php",
                                   'Right'=> $cright_read);
  }
  if ($cgp_show["module"]["group"]) {
    $menus["USER"]["GROUP"] = array('Name'=> $l_header_group,
                                    'Ico' => "$ico_group",
                                    'Url' => "$path/group/group_index.php",
			            'Right'=> $cright_read);
  }
}
