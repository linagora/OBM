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
  if ($cgp_show["module"]["todo"]) 
    global_session_load_user_todos($set_todo); 

  // We update the last login date for the user
  run_query_userobm_update_lastaccess($auth->auth["uid"]);
}

// Display parameters if debug level is set
display_debug_param($params);

// Display session content if debug level is set
if (($set_debug & $cdg_sess) == $cdg_sess) {
  while (list($key, $val) = each ($_SESSION)) {
    if (is_array($val)) {
      $val = var_export($val, true);
    }
    $dis_session .= "$key = $val<br />\n";
  }
}

// Default settings
$set_lang_default = "en";
$set_theme_default = "standard";
$set_menu_default = $cme_both;
$set_display_default = "no";
$set_rows_default = 10;
$set_todo_default = 'todo_priority';
$set_date_upd_default = $cda_dmy;
$set_date_default = $cda_iso;
$set_commentorder_default = $cco_chro;
$set_debug_default = 0;
$set_cal_interval_default = 2;

if ($set_lang == "") $set_lang = $set_lang_default;
if ($set_theme == "") $set_theme = $set_theme_default;
if ($set_menu == "") $set_menu = $set_menu_default;
if ($set_display == "") $set_display = $set_display_default;
if ($set_rows == "") $set_rows = $set_rows_default;
//if ($set_todo == "") $set_todo = $set_todo_default;
if($set_date_upd == "") $set_date_upd = $set_date_upd_default;
if ($set_date == "") $set_date = $set_date_default;
if ($set_commentorder == "") $set_commentorder = $set_commentorder_default;
if ($set_debug == "") $set_debug = $set_debug_default;
if ($set_cal_interval == "") $set_cal_interval = $set_cal_interval_default;


$obm["domain_id"] = $auth->auth["domain_id"];
$obm["uid"] = $auth->auth["uid"];
$obm["perm"] = $auth->auth["perm"];

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

// Specific conf global lang file
if ($conf_lang) {
  $lang_file = "conf/lang/$set_lang/global.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  }
}

// Module lang file
if ($module != "") {
  $lc_module = strtolower($module);
  $lang_file = "$obminclude/lang/$set_lang/$lc_module.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {
    $lang_file = "$obminclude/lang/$set_lang_default/$lc_module.inc";
    if (file_exists("$path/../".$lang_file)) {
      include("$lang_file");
    }
  }

  // Specific conf module lang file
  if ($conf_lang) {
    $lang_file = "conf/lang/$set_lang/$lc_module.inc";
    if (file_exists("$path/../".$lang_file)) {
      include("$lang_file");
    } else {
      $lang_file = "conf/lang/$set_lang_default/$lc_module.inc";
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


///////////////////////////////////////////////////////////////////////////////
// Sections definitions
///////////////////////////////////////////////////////////////////////////////
while (list($sec,$value) = each($cgp_show["section"])) {
  if ($value) {
    $sec_right = $value["right"];
    if ($sec_right == "") {
      $sec_right = $cright_read;
    }
    $sections[$sec] = array('Name' => ${"l_section_$sec"},
                           'Url'  => $cgp_show["section"][$sec]["url"],
                           'Right'=> $sec_right);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Modules defintion
///////////////////////////////////////////////////////////////////////////////
if ($cgp_show["module"]["company"]) {
  $modules["company"] = array('Name'=> $l_module_company,
   			     'Ico' => "$ico_company",
			     'Url' => "$path/company/company_index.php",
			     'Right'=> $cright_read);
}
if ($cgp_show["module"]["contact"]) {
  $modules["contact"] = array('Name'=> $l_module_contact,
                             'Ico' => "$ico_contact",
                             'Url' => "$path/contact/contact_index.php",
                             'Right'=> $cright_read);
}
if ($cgp_show["module"]["lead"]) {
  $modules["lead"] = array('Name'=> $l_module_lead,
                                  'Ico' => "$ico_lead",
                                  'Url' => "$path/lead/lead_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show["module"]["deal"]) {
  $modules["deal"] = array('Name'=> $l_module_deal,
                                  'Ico' => "$ico_deal",
                                  'Url' => "$path/deal/deal_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show["module"]["list"]) {
  $modules["list"] = array('Name'=> $l_module_list,
                                  'Ico' => "$ico_list",
                                  'Url' => "$path/list/list_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show["module"]["agenda"]) {
  $modules["agenda"] = array('Name'=> $l_module_agenda,
                                    'Ico' => "$ico_agenda",
                                    'Url' => "$path/agenda/agenda_index.php",
                                    'Right'=> $cright_read);
}
if ($cgp_show["module"]["cv"]) {
  $modules["cv"] = array('Name'=> $l_module_cv,
                                  'Ico' => "$ico_cv",
                                  'Url' => "$path/cv/cv_index.php",
                                  'Right'=> $cright_read);
                                    
}
if ($cgp_show["module"]["todo"]) {
  $modules["todo"] = array('Name'=> $l_module_todo,
                                  'Ico' => "$ico_todo",
                                  'Url' => "$path/todo/todo_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show["module"]["publication"]) {
  $modules["publication"] = array('Name'=> $l_module_publication,
			           'Ico' => "$ico_publication",
			           'Url' => "$path/publication/publication_index.php",
			           'Right'=> $cright_read);
}
if ($cgp_show["module"]["statistic"]) {
  $modules["statistic"] = array('Name'=> $l_module_statistic,
			           'Ico' => "$ico_statistic",
			           'Url' => "$path/statistic/statistic_index.php",
			           'Right'=> $cright_read);
}

if ($cgp_show["module"]["time"]) {
  $modules["time"] = array('Name'=> $l_module_time,
                                   'Ico' => "$ico_time",
                                   'Url' => "$path/time/time_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show["module"]["project"]) {
  $modules["project"] = array('Name'=> $l_module_project,
                              'Ico' => "$ico_project",
                              'Url' => "$path/project/project_index.php",
                              'Right'=> $cright_read);
}
if ($cgp_show["module"]["contract"]) {
  $modules["contract"] = array('Name' => $l_module_contract,
                               'Ico' => "$ico_contract",
                               'Url' => "$path/contract/contract_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show["module"]["incident"]) {
  $modules["incident"] = array('Name'=> $l_module_incident,
                               'Ico' => "$ico_incident",
                               'Url' => "$path/incident/incident_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show["module"]["document"]) {
  $modules["document"] = array('Name'=> $l_module_document,
                               'Ico' => "$ico_document",
                               'Url' => "$path/document/document_index.php",
                               'Right'=> $cright_read);
}

$modules["admin"] = array('Name'=> $l_module_admin,
                  'Ico' => "$ico_admin",
                  'Url' => "$path/admin/admin_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules["admin_code"] = array('Name'=> $l_module_admin_code,
                  'Ico' => "$ico_code",
		  'Url' => "$path/admin_code/admin_code_index.php?mode=html",
		  'Right'=> $cright_read_admin);
$modules["admin_pref"] = array('Name'=> $l_module_admin_pref,
                  'Ico' => "$ico_pref",
                  'Url' => "$path/admin_pref/admin_pref_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules["admin_data"] = array('Name'=> $l_module_admin_data,
                  'Ico' => "$ico_data",
                  'Url' => "$path/admin_data/admin_data_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules["admin_lang"] = array('Name'=> $l_module_admin_lang,
                  'Ico' => "$ico_lang",
                  'Url' => "$path/admin_lang/admin_lang_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules["admin_ref"] = array('Name'=> $l_module_admin_ref,
                  'Ico' => "$ico_ref",
                  'Url' => "$path/admin_ref/admin_ref_index.php",
                  'Right'=> $cright_read_admin);
$modules["import"] = array('Name'=> $l_module_import,
                  'Ico' => "$ico_ref",
                  'Url' => "$path/import/import_index.php",
                  'Right'=> $cright_read_admin);
$modules["backup"] = array('Name'=> $l_module_backup,
                  'Ico' => "$ico_ref",
                  'Url' => "$path/backup/backup_index.php",
                  'Right'=> $cright_read_admin);
$modules["domain"] = array('Name'=> $l_module_domain,
									'Ico' => "$ico_ref",
                   'Url' => "$path/domain/domain_index.php",
                   'Right'=> $cright_read_admin);
                   
if ($cgp_show["module"]["account"]) {
  $modules["account"] = array('Name'=> $l_module_account,
                               'Ico' => "$ico_account",
                               'Url' => "$path/account/account_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show["module"]["invoice"]) {
  $modules["invoice"] = array('Name'=> $l_module_invoice,
                                'Ico' => "$ico_invoice",
                                'Url' => "$path/invoice/invoice_index.php",
                                'Right'=> $cright_read);
}
if ($cgp_show["module"]["payment"]) {
  $modules["payment"] = array('Name'=> $l_module_payment,
                                'Ico' => "$ico_payment",
                                'Url' => "$path/payment/payment_index.php",
                                'Right'=> $cright_read);
}

if ($cgp_show["module"]["settings"]) {
  $modules["settings"] = array(
                               'Name'=> $l_settings,
                               'Ico' => "$ico_setting",
		               'Url' => "$path/settings/settings_index.php",
			       'Right'=> $cright_read);
}
if ($cgp_show["module"]["user"]) {
  $modules["user"] = array('Name'=> $l_module_user,
                                   'Ico' => "$ico_user",
                                   'Url' => "$path/user/user_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show["module"]["resource"]) {
  $modules["resource"] = array('Name'=> $l_module_resource,
                                   'Ico' => "$ico_resource",
                                   'Url' => "$path/resource/resource_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show["module"]["group"]) {
  $modules["group"] = array('Name'=> $l_module_group,
                                    'Ico' => "$ico_group",
                                    'Url' => "$path/group/group_index.php",
			            'Right'=> $cright_read);
}
if ($cgp_show["module"]["resourcegroup"]) {
  $modules["resourcegroup"] = array('Name'=> $l_module_resourcegroup,
                                    'Ico' => "$ico_resourcegroup",
                                    'Url' => "$path/resourcegroup/resourcegroup_index.php",
			            'Right'=> $cright_read);
}
