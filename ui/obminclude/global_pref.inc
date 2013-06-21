<script language="php">
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_pref.inc                                              //
//     - Desc : Global preference OBM include file                           //
// 2002-07-31 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

// Update the cookie expires time : now + lifetime or session wide
// but not in txt mode or if headers have already been sent
if ( (! headers_sent()) && ($params['mode'] != 'txt'))
  $sess->setcookie();

// if prefs have not been loaded (we check the var set_lang) - Login Phase
if (($_SESSION['set_lang'] == '') && ($params['mode'] != 'txt')) {
  // We Load and make session variables from User preferences
  session_load_user_prefs();
  if ($cgp_show['module']['todo']) 
    global_session_load_user_todos($_SESSION['set_todo']); 

  // We update the last login date for the user
  run_query_userobm_update_lastaccess($obm['uid']);
}

if ( $_SESSION['set_timezone'] ) {
  date_default_timezone_set($_SESSION['set_timezone']);
  Of_Date::setOption('timezone',$_SESSION['set_timezone']);
}

// Display parameters if debug level is set
display_debug_param($params);

// Display session content if debug level is set
if (($_SESSION['set_debug'] & $cdg_sess) == $cdg_sess) {
  while (list($key, $val) = each ($_SESSION)) {
    if (is_array($val)) {
      $val = var_export($val, true);
    }
    if(is_object($val)) {
      $val = 'Object';
    }
    $dis_session .= "<b>$key</b> : $val<br />\n";
  }
}

// Default settings
$set_lang_default = 'en';
$set_theme_default = 'default';
$set_menu_default = $cme_both;
$set_display_default = 'no';
$set_rows_default = 10;
$set_todo_default = 'todo_priority';
$set_date_upd_default = $cda_dmy;
$set_date_default = $cda_iso;
$set_commentorder_default = $cco_chro;
$set_debug_default = 0;
$set_cal_interval_default = 2;

if ($_SESSION['set_lang'] == '') $_SESSION['set_lang'] = $set_lang_default;
if ($_SESSION['set_theme'] == '') $_SESSION['set_theme'] = $set_theme_default;
if ($_SESSION['set_menu'] == '') $_SESSION['set_menu'] = $set_menu_default;
if ($_SESSION['set_display'] == '') $_SESSION['set_display'] = $set_display_default;
if ($_SESSION['set_rows'] == '') $_SESSION['set_rows'] = $set_rows_default;
//if ($_SESSION['set_todo'] == '') $_SESSION['set_todo'] = $set_todo_default;
if ($_SESSION['set_date_upd'] == '') $_SESSION['set_date_upd'] = $set_date_upd_default;
if ($_SESSION['set_date'] == '') $_SESSION['set_date'] = $set_date_default;
if ($_SESSION['set_commentorder'] == '') $_SESSION['set_commentorder'] = $set_commentorder_default;
if ($_SESSION['set_debug'] == '') $_SESSION['set_debug'] = $set_debug_default;
if ($_SESSION['set_cal_interval'] == '') $_SESSION['set_cal_interval'] = $set_cal_interval_default;


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Theme.                                           //
///////////////////////////////////////////////////////////////////////////////
$theme_file = 'resources/themes/'.$_SESSION['set_theme'].'/theme.inc';
if (file_exists("$path/../$theme_file")) {
  include_once("$theme_file");
} else {
  include_once("resources/themes/$set_theme_default/theme.inc");
}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Language File, depending from which module this  //
// page is called and the selected language.                                 //
///////////////////////////////////////////////////////////////////////////////
$lang_file = "$obminclude/lang/".$_SESSION['set_lang'].'/global.inc';
if (file_exists("$path/../".$lang_file)) {
  include_once("$lang_file");
}

// Specific conf global lang file
if ($conf_lang) {
  $lang_file = 'conf/lang/'.$_SESSION['set_lang'].'/global.inc';
  if (file_exists("$path/../".$lang_file)) {
    include_once("$lang_file");
  }
}

// Module lang file
if ($module != '') {
  $lc_module = strtolower($module);
  $lang_file = "$obminclude/lang/".$_SESSION['set_lang']."/$lc_module.inc";
  if (file_exists("$path/../".$lang_file)) {
    include_once("$lang_file");
  } else {
    $lang_file = "$obminclude/lang/$set_lang_default/$lc_module.inc";
    if (file_exists("$path/../".$lang_file)) {
      include_once("$lang_file");
    }
  }

  // Specific conf module lang file
  if ($conf_lang) {
    $lang_file = 'conf/lang/'.$_SESSION['set_lang']."/$lc_module.inc";
    if (file_exists("$path/../".$lang_file)) {
      include_once("$lang_file");
    } else {
      $lang_file = "conf/lang/$set_lang_default/$lc_module.inc";
      //      if (file_exists("$path/../".$lang_file)) {
      //	include_once("$lang_file");
      //      }
    }
  }
}

if($_SESSION['set_lang']) {
  SI18n::set_locale($_SESSION['set_lang']);
  $full_locale = $_SESSION['set_lang'].'_'.strtoupper($_SESSION['set_lang']);
  setlocale(LC_TIME, $full_locale.'.utf8', $full_locale, $_SESSION['set_lang']);
}

///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Display file (global display functions)          //
///////////////////////////////////////////////////////////////////////////////
$display_include_file = 'resources/themes/'.$_SESSION['set_theme'].'/display.inc';
include_once("$display_include_file");


///////////////////////////////////////////////////////////////////////////////
// Inclusion of user defined Code and rules
///////////////////////////////////////////////////////////////////////////////
if ($conf_modules && file_exists("$path/../conf/modules/$module.inc")) {
  include_once("conf/modules/$module.inc");
}

///////////////////////////////////////////////////////////////////////////////
// Sections definitions
///////////////////////////////////////////////////////////////////////////////
foreach ($cgp_show['section'] as $sec => $value) {
  if ($value) {
    $sec_right = $value['right'];
    if ($sec_right == '') {
      $sec_right = $cright_read;
    }
    $sections[$sec] = array('Name' => ${"l_section_$sec"},
                           'Url'  => $cgp_show['section'][$sec]['url'],
                           'Target'  => $cgp_show['section'][$sec]['target'],
                           'Right'=> $sec_right);
  }
}

///////////////////////////////////////////////////////////////////////////////
// Modules definition
///////////////////////////////////////////////////////////////////////////////
$u = get_user_info($obm['uid']);
if ($cgp_show['module']['company']) {
  $modules['company'] = array('Name'=> $l_module_company,
   			     'Ico' => "$ico_company",
			     'Url' => "$path/company/company_index.php",
			     'Right'=> $cright_read);
}
if ($cgp_show['module']['contact']) {
  $modules['contact'] = array('Name'=> $l_module_contact,
                             'Ico' => "$ico_contact",
                             'Url' => "$path/contact/contact_index.php",
                             'Right'=> $cright_read);
}
if ($cgp_show['module']['lead']) {
  $modules['lead'] = array('Name'=> $l_module_lead,
                                  'Ico' => "$ico_lead",
                                  'Url' => "$path/lead/lead_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show['module']['deal']) {
  $modules['deal'] = array('Name'=> $l_module_deal,
                                  'Ico' => "$ico_deal",
                                  'Url' => "$path/deal/deal_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show['module']['list']) {
  $modules['list'] = array('Name'=> $l_module_list,
                                  'Ico' => "$ico_list",
                                  'Url' => "$path/list/list_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show['module']['calendar']) {
  $modules['calendar'] = array('Name'=> $l_module_calendar,
                                    'Ico' => "$ico_calendar",
                                    'Url' => "$path/calendar/calendar_index.php?action=waiting_events",
                                    'Right'=> $cright_read);
}
if ($cgp_show['module']['cv']) {
  $modules['cv'] = array('Name'=> $l_module_cv,
                                  'Ico' => "$ico_cv",
                                  'Url' => "$path/cv/cv_index.php",
                                  'Right'=> $cright_read);
                                    
}
if ($cgp_show['module']['todo']) {
  $modules['todo'] = array('Name'=> $l_module_todo,
                                  'Ico' => "$ico_todo",
                                  'Url' => "$path/todo/todo_index.php?action=search",
                                  'Right'=> $cright_read);
}
if ($cgp_show['module']['publication']) {
  $modules['publication'] = array('Name'=> $l_module_publication,
			           'Ico' => "$ico_publication",
			           'Url' => "$path/publication/publication_index.php",
			           'Right'=> $cright_read);
}
if ($cgp_show['module']['statistic']) {
  $modules['statistic'] = array('Name'=> $l_module_statistic,
			           'Ico' => "$ico_statistic",
			           'Url' => "$path/statistic/statistic_index.php",
			           'Right'=> $cright_read);
}

if ($cgp_show['module']['time']) {
  $modules['time'] = array('Name'=> $l_module_time,
                                   'Ico' => "$ico_time",
                                   'Url' => "$path/time/time_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show['module']['project']) {
  $modules['project'] = array('Name'=> $l_module_project,
                              'Ico' => "$ico_project",
                              'Url' => "$path/project/project_index.php",
                              'Right'=> $cright_read);
}
if ($cgp_show['module']['planning']) {
  $modules['planning'] = array('Name'=> $l_module_planning,
                               'Ico' => "$ico_time",
                               'Url' => "$path/planning/planning_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show['module']['contract']) {
  $modules['contract'] = array('Name' => $l_module_contract,
                               'Ico' => "$ico_contract",
                               'Url' => "$path/contract/contract_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show['module']['incident']) {
  $modules['incident'] = array('Name'=> $l_module_incident,
                               'Ico' => "$ico_incident",
                               'Url' => "$path/incident/incident_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show['module']['document']) {
  $modules['document'] = array('Name'=> $l_module_document,
                               'Ico' => "$ico_document",
                               'Url' => "$path/document/document_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show['module']['campaign']) {
  $modules['campaign'] = array('Name'=> $l_module_campaign,
                                  'Ico' => "$ico_cancel",
                                  'Url' => "$path/campaign/campaign_index.php",
                                  'Right'=> $cright_read);
}
if ($cgp_show['module']['log']) {
  $modules['log'] = array('Name'=> $l_module_log,
                                  'Ico' => "$ico_list",
                                  'Url' => "$path/log/log_index.php",
                                  'Right'=> $cright_read);
}

$modules['admin_code'] = array('Name'=> $l_module_admin_code,
                  'Ico' => "$ico_code",
		  'Url' => "$path/admin_code/admin_code_index.php?mode=html",
		  'Right'=> $cright_read_admin);
$modules['admin_pref'] = array('Name'=> $l_module_admin_pref,
                  'Ico' => "$ico_pref",
                  'Url' => "$path/admin_pref/admin_pref_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules['admin_data'] = array('Name'=> $l_module_admin_data,
                  'Ico' => "$ico_data",
                  'Url' => "$path/admin_data/admin_data_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules['admin_lang'] = array('Name'=> $l_module_admin_lang,
                  'Ico' => "$ico_lang",
                  'Url' => "$path/admin_lang/admin_lang_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules['admin_ref'] = array('Name'=> $l_module_admin_ref,
                  'Ico' => "$ico_ref",
                  'Url' => "$path/admin_ref/admin_ref_index.php",
                  'Right'=> $cright_read_admin);

$modules['admin'] = array('Name'=> $l_module_admin,
                  'Ico' => "$ico_admin",
                  'Url' => "$path/admin/admin_index.php?mode=html",
                  'Right'=> $cright_read_admin);
$modules['tools'] = array('Name'=> $l_module_tools,
                  'Ico' => "$ico_tools",
                  'Url' => "$path/tools/tools_index.php",
                  'Right'=> $cright_read_admin);
$modules['control'] = array('Name'=> $l_module_control,
                  'Ico' => "$ico_control",
                  'Url' => "$path/control/control_index.php",
                  'Right'=> $cright_read_admin);
$modules['import'] = array('Name'=> $l_module_import,
                  'Ico' => "$ico_import",
                  'Url' => "$path/import/import_index.php",
                  'Right'=> $cright_read_admin);
$modules['backup'] = array('Name'=> $l_module_backup,
                  'Ico' => "$ico_backup",
                  'Url' => "$path/backup/backup_index.php",
                  'Right'=> $cright_read_admin);
$modules['domain'] = array('Name'=> $l_module_domain,
	       	   'Ico' => "$ico_ref",
                   'Url' => "$path/domain/domain_index.php",
                   'Right'=> $cright_read_admin);
$modules['samba'] = array('Name'=> $l_module_samba,
                  'Ico' => "$ico_samba",
                  'Url' => "$path/samba/samba_index.php",
                  'Right'=> $cright_read_admin);
$modules['report'] = array('Name'=> $l_module_report,
                  'Ico' => "$ico_report",
                  'Url' => "$path/report/report_index.php?mode=html",
                  'Right'=> $cright_read_admin);

if ($cgp_show['module']['account']) {
  $modules['account'] = array('Name'=> $l_module_account,
                               'Ico' => "$ico_account",
                               'Url' => "$path/account/account_index.php",
                               'Right'=> $cright_read);
}
if ($cgp_show['module']['invoice']) {
  $modules['invoice'] = array('Name'=> $l_module_invoice,
                                'Ico' => "$ico_invoice",
                                'Url' => "$path/invoice/invoice_index.php",
                                'Right'=> $cright_read);
}
if ($cgp_show['module']['payment']) {
  $modules['payment'] = array('Name'=> $l_module_payment,
                                'Ico' => "$ico_payment",
                                'Url' => "$path/payment/payment_index.php",
                                'Right'=> $cright_read);
}

if ($cgp_show['module']['user']) {
  $modules['user'] = array('Name'=> $l_module_user,
                                   'Ico' => "$ico_user",
                                   'Url' => "$path/user/user_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show['module']['people']) {
  $modules['people'] = array('Name'=> $l_module_people,
                                   'Ico' => "$ico_user",
                                   'Url' => "$path/people/people_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show['module']['resource']) {
  $modules['resource'] = array('Name'=> $l_module_resource,
                                   'Ico' => "$ico_resource",
                                   'Url' => "$path/resource/resource_index.php",
                                   'Right'=> $cright_read);
}
if ($cgp_show['module']['group']) {
  $modules['group'] = array('Name'=> $l_module_group,
                                    'Ico' => "$ico_group",
                                    'Url' => "$path/group/group_index.php",
			            'Right'=> $cright_read);
}
if ($cgp_show['module']['resourcegroup']) {
  $modules['resourcegroup'] = array('Name'=> $l_module_resourcegroup,
                                    'Ico' => "$ico_resourcegroup",
                                    'Url' => "$path/resourcegroup/resourcegroup_index.php",
			            'Right'=> $cright_read);
}
if ($cgp_show['module']['mailshare']) {
  $modules['mailshare'] = array('Name'=> $l_module_mailshare,
                                    'Ico' => "$ico_mailshare",
                                    'Url' => "$path/mailshare/mailshare_index.php",
			            'Right'=> $cright_read);
}
if ($cgp_show['module']['host']) {
  $modules['host'] = array('Name'=> $l_module_host,
                               'Ico' => "$ico_host",
                               'Url' => "$path/host/host_index.php",
                               'Right'=> $cright_read);
}

if ($cgp_show['module']['settings']) {
  $modules['settings'] = array(
                               'Name'=> $l_module_settings,
                               'Ico' => "$ico_settings",
		               'Url' => "$path/settings/settings_index.php",
			       'Right'=> $cright_read);
}

if ($cgp_show['module']['password']) {
  $modules['password'] = array(
                               'Name'=> $l_module_password,
                               'Ico' => "$ico_password",
		               'Url' => "$path/password/password_index.php",
			       'Right'=> $cright_read);
}

if ($cgp_show['module']['mailforward']) {
  $modules['mailforward'] = array(
                               'Name'=> $l_module_mailforward,
                               'Ico' => "$ico_mailforward",
		               'Url' => "$path/mailforward/mailforward_index.php",
			       'Right'=> $cright_read);
}

if ($cgp_show['module']['vacation']) {
  $modules['vacation'] = array(
                               'Name'=> $l_module_vacation,
                               'Ico' => "$ico_vacation",
		               'Url' => "$path/vacation/vacation_index.php",
			       'Right'=> $cright_read);
}

if ($cgp_show['module']['mailbox']) {
  $modules['mailbox'] = array(
                               'Name'=> $l_module_mailbox,
                               'Ico' => "$ico_mailbox",
		               'Url' => "$path/mailbox/mailbox_index.php",
			       'Right'=> $cright_read);
}

if ($cgp_show['module']['organizationalchart']) {
  $modules['organizationalchart'] = array('Name'=> $l_module_organizationalchart,
                                    'Ico' => "$ico_organizationalchart",
                                    'Url' => "$path/organizationalchart/organizationalchart_index.php",
			            'Right'=> $cright_read);
}

if ($cgp_show['module']['userbackup']) {
  $modules['userbackup'] = array('Name'=> $l_module_userbackup,
                                    'Ico' => "$ico_data",
                                    'Url' => "$path/userbackup/userbackup_index.php",
                  'Right'=> $cright_read);
}

if ($cgp_show['module']['userpattern']) {
  $modules['userpattern'] = array('Name'  => $l_module_userpattern,
			      'Ico'   => "$ico_userpattern",
			      'Url'   => "$path/userpattern/userpattern_index.php",
			      'Right' => $cright_read);
}

if ($cgp_show['module']['profile']) {
  $modules['profile'] = array('Name'  => $l_module_profile,
			      'Ico'   => "$ico_profile",
			      'Url'   => "$path/profile/profile_index.php",
			      'Right' => $cright_read);
}

if ($cgp_show['module']['organizationalchart']) {
  $modules['organizationalchart'] = array('Name'=> $l_module_organizationalchart,
                                    'Ico' => "$ico_organizationalchart",
                                    'Url' => "$path/organizationalchart/organizationalchart_index.php",
			            'Right'=> $cright_read);
}

if ($cgp_show['module']['webmail'] && $u['mail_perms'] == 1) {
  $modules['webmail'] = array('Name'=> $l_module_webmail,
                                    'Ico' => "$ico_webmail",
                                    'Url' => "$path/webmail",
			            'Right'=> $cright_read);
}

if ($cgp_show['module']['device']) {
  $modules['device'] = array('Name'=> $l_module_device,
                                    'Ico' => "$ico_phone",
                                    'Url' => "$path/device/device_index.php",
			            'Right'=> $cright_read);
}
if ($cgp_show['module']['admin_device']) {
  $modules['admin_device'] = array('Name'=> $l_module_admin_device,
                                    'Ico' => "$ico_phone",
                                    'Url' => "$path/admin_device/admin_device_index.php",
			            'Right'=> $cright_write_admin);
}

