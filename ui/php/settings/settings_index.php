<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

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
// OBM - File : settings_index.php                                           //
//     - Desc : Settings (Language, themes,...) management index file        //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'settings';
$display = array();
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude='obminclude';
include("$obminclude/global.inc");
$params = get_settings_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));

if(!isset($_SESSION['set_cal_first_hour'])) $_SESSION['set_cal_first_hour'] = $ccalendar_first_hour;
if(!$_SESSION['set_cal_last_hour']) $_SESSION['set_cal_last_hour'] = $ccalendar_last_hour;

if ($params['lang'] != '') {
  $_SESSION['set_lang'] = $params['lang'];
  update_user_pref($obm['uid'], 'set_lang', $_SESSION['set_lang']);
}
if ($params['theme'] != '') {
  $_SESSION['set_theme'] = $params['theme'];
  update_user_pref($obm['uid'], 'set_theme', $_SESSION['set_theme']);
}

// Validate user preferences
if ($params['form_user_pref']) {

  if ($perm->check_right($module, $cright_write_admin)) {
    $set_debug = $params['debug_id'] | $params['debug_param'] | $params['debug_sess'] | $params['debug_sql'] | $params['debug_exe'] | $params['debug_solr'];
    $_SESSION['set_debug'] = $set_debug;
    update_user_pref($obm['uid'], 'set_debug', $_SESSION['set_debug']);
  }

  if ($params['menu'] != '') {
    $_SESSION['set_menu'] = $params['menu'];
    update_user_pref($obm['uid'], 'set_menu', $_SESSION['set_menu']);
  }

  if ($params['display'] == 'yes') {
    $_SESSION['set_display'] = 'yes';
  } else {
    $_SESSION['set_display'] = 'no';
  }
  update_user_pref($obm['uid'], 'set_display', $_SESSION['set_display']);

  if ($params['rows'] != '') {
    if ($params['rows'] > 0 && $params['rows'] <= $conf_display_max_rows) { 
      $_SESSION['set_rows'] = $params['rows'];
    } else {
      $_SESSION['set_rows'] = $conf_display_max_rows;
    }
    update_user_pref($obm['uid'], 'set_rows', $_SESSION['set_rows']);
  }

  if (($params['todo'] != '') && ($params['todo'] != $_SESSION['set_todo'])) {
    $_SESSION['set_todo'] = $params['todo'];
    update_user_pref($obm['uid'], 'set_todo', $_SESSION['set_todo']);
    global_session_load_user_todos($_SESSION['set_todo']);
  }

  if ($params['dsrc'] != '') {
    $_SESSION['set_dsrc'] = $params['dsrc'];
    update_user_pref($obm['uid'], 'set_dsrc', $_SESSION['set_dsrc']);
  }

  if ($params['date'] != '') {
    $_SESSION['set_date'] = $params['date'];
    Of_Date::setOption('outputdate',$params['timezone']);
    update_user_pref($obm['uid'], 'set_date', $_SESSION['set_date']);
  }
  
  if ($params['date_upd'] != '') {
    $_SESSION['set_date_upd'] = $params['date_upd'];
    Of_Date::setOption('inputedate',$params['timezone']);
    update_user_pref($obm['uid'], 'set_date_upd', $_SESSION['set_date_upd']);
  }
  
  if ($params['timezone'] != '') {
    $_SESSION['set_timezone'] = $params['timezone'];
    Of_Date::setOption('timezone',$params['timezone']);
    update_user_pref($obm['uid'], 'set_timezone', $_SESSION['set_timezone']);
    unset($_SESSION['cal_current_view']);
  }

  if ($params['timeformat'] != '') {
    $_SESSION['set_timeformat'] = $params['timeformat'];
    Of_Date::setOption('timeformat',$params['timeformat']);
    update_user_pref($obm['uid'], 'set_timeformat', $_SESSION['set_timeformat']);
  }

  if ($params['commentorder'] != '') {
    $_SESSION['set_commentorder'] = $params['commentorder'];
    update_user_pref($obm['uid'], 'set_commentorder', $_SESSION['set_commentorder']);
  }

  if ($params['mail'] == 'yes') {
    $_SESSION['set_mail'] = 'yes';
  } else {
    $_SESSION['set_mail'] = 'no';
  }
  update_user_pref($obm['uid'], 'set_mail', $_SESSION['set_mail']);

  if ($params['mail_participation'] == 'yes') {
    $_SESSION['set_mail_participation'] = 'yes';
  } else {
    $_SESSION['set_mail_participation'] = 'no';
  }
  update_user_pref($obm['uid'], 'set_mail_participation', $_SESSION['set_mail_participation']);

  // days to display in the week view
  if ($params['display_days']=='0000000') {
    $params['display_days'] = '1111111';
  }
  $_SESSION['set_cal_display_days'] = $params['display_days'];
  update_user_pref($obm['uid'], 'set_cal_display_days', $_SESSION['set_cal_display_days']);

  if ($params['cal_interval'] != '') {
    $_SESSION['set_cal_interval'] = $params['cal_interval'];
    update_user_pref($obm['uid'], 'set_cal_interval', $_SESSION['set_cal_interval'], 1);
  }

  if ($params['cal_first_hour'] !== '') {
    if ($params['cal_first_hour'] >= 0 && $params['cal_first_hour'] <= 24) {
      $_SESSION['set_cal_first_hour'] = $params['cal_first_hour'];
    } else {
      $_SESSION['set_cal_first_hour'] = $ccalendar_first_hour;
    }
    update_user_pref($obm['uid'], 'set_cal_first_hour', $_SESSION['set_cal_first_hour'], 1);
  }
  if ($params['cal_last_hour'] != '') {
    if ($params['cal_last_hour'] >= 0 && $params['cal_last_hour'] <= 24) {
      $_SESSION['set_cal_last_hour'] = $params['cal_last_hour'];
    } else {
      $_SESSION['set_cal_last_hour'] = $ccalendar_last_hour;
    }
    update_user_pref($obm['uid'], 'set_cal_last_hour', $_SESSION['set_cal_last_hour'], 1);
  }
  if ($params['cal_alert'] != '') {
      $_SESSION['set_cal_alert'] = $params['cal_alert'];
      update_user_pref($obm['uid'], 'set_cal_alert', $_SESSION['set_cal_alert'], 1);
  }
  if ($params['cal_allday_opacity'] != '') {
  	$_SESSION['set_allday_opacity'] = $params['cal_allday_opacity'];
  	update_user_pref($obm['uid'], 'set_allday_opacity', $_SESSION['set_allday_opacity'], 1);
  }
  if ($params['csv_sep'] != '') {
    $_SESSION['set_csv_sep'] = $params['csv_sep'];
    update_user_pref($obm['uid'], 'set_csv_sep', $_SESSION['set_csv_sep']);
  }

  if ($params['public_fb'] == 'yes') {
    $_SESSION['set_public_fb'] = $params['public_fb'];
    update_user_pref($obm['uid'], 'set_public_fb', $params['public_fb']);    
  } else {
    $_SESSION['set_public_fb'] = 'no';
    update_user_pref($obm['uid'], 'set_public_fb', 'no');   
  }

  if(is_array($params['custom'])) {
    foreach($params['custom'] as  $key => $value) {
      if(strpos($key, 'set_custom') === 0) {
        $_SESSION[$key] = $value;
        update_user_pref($obm['uid'], $key, $value);    
      }
    }
  }

}

require("$obminclude/global_pref.inc");
require('settings_display.inc');

if ($action == '') $action = 'index';
get_settings_actions();
$perm->check_permissions($module, $action);

page_close();

if (($_SESSION['set_debug'] & $cdg_id) == $cdg_id) $dg_id = 'checked';
if (($_SESSION['set_debug'] & $cdg_param) == $cdg_param) $dg_param = 'checked';
if (($_SESSION['set_debug'] & $cdg_sess) == $cdg_sess) $dg_sess = 'checked';
if (($_SESSION['set_debug'] & $cdg_sql) == $cdg_sql) $dg_sql = 'checked';
if (($_SESSION['set_debug'] & $cdg_exe) == $cdg_exe) $dg_exe = 'checked';
if (($_SESSION['set_debug'] & $cdg_solr) == $cdg_solr) $dg_solr = 'checked';

if ($_SESSION['set_menu'] == $cme_txt) $me_txt = 'checked';
if ($_SESSION['set_menu'] == $cme_ico) $me_ico = 'checked';
if ($_SESSION['set_menu'] == $cme_both) $me_both = 'checked';

if ($_SESSION['set_date'] == $cda_iso) $da_iso = 'checked';
if ($_SESSION['set_date'] == $cda_en) $da_en = 'checked';
if ($_SESSION['set_date'] == $cda_fr) $da_fr = 'checked';
if ($_SESSION['set_date'] == $cda_txt) $da_txt = 'checked';

if ($_SESSION['set_date_upd'] == $cda_fr) $da_upd_fr = 'checked';
if ($_SESSION['set_date_upd'] == $cda_en) $da_upd_en = 'checked';
if ($_SESSION['set_date_upd'] == $cda_iso) $da_upd_iso = 'checked';

if ($_SESSION['set_commentorder'] == $cco_chro) $co_chro = 'checked';
if ($_SESSION['set_commentorder'] == $cco_rev) $co_rev = 'checked';

if ($_SESSION['set_cal_interval'] == $ccal_4) $cal_4 = 'checked';
if ($_SESSION['set_cal_interval'] == $ccal_2) $cal_2 = 'checked';
if ($_SESSION['set_cal_interval'] == $ccal_1) $cal_1 = 'checked';

if ($_SESSION['set_csv_sep'] == $ccsvd_sc) $csvd_sc = 'checked';
if ($_SESSION['set_csv_sep'] == $ccsvd_tab) $csvd_tab = 'checked';

if ($_SESSION['set_allday_opacity'] == 'TRANSPARENT') $cal_allday_opacity_free = 'checked';
if ($_SESSION['set_allday_opacity'] == 'OPAQUE') $cal_allday_opacity_busy = 'checked';

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
$display['header'] = display_menu($module);

// Todo Order select
if ($_SESSION['set_todo'] == $cts_pri) { $todo_pri = "selected=\"selected\" "; }
if ($_SESSION['set_todo'] == $cts_dead) { $todo_dead = "selected=\"selected\" "; }
$sel_todo = "<select name=\"todo\" id=\"todo_id\">
  <option value=\"$cts_pri\" $todo_pri>$l_priority</option>
  <option value=\"$cts_dead\" $todo_dead>$l_deadline</option>
</select>";

// Data source select
$dsrc_q = run_query_global_datasource();
$sel_dsrc = "<select name=\"dsrc\" id=\"dsrc\">
  <option value=\"$c_undef\">$l_undef</option>";
while ($dsrc_q->next_record()) {
  $d_id = $dsrc_q->f('datasource_id');
  $sel_dsrc .= "\n<option value=\"$d_id\"";
  if ($d_id == $_SESSION['set_dsrc']) { $sel_dsrc .= " selected=\"selected\" "; }
  $sel_dsrc .= '>'. $dsrc_q->f('datasource_name') . '</option>';
}
$sel_dsrc .= '</select>';


///////////////////////////////////////////////////////////////////////////////
// Debug block (need cright_write admin right)
///////////////////////////////////////////////////////////////////////////////
if ($perm->check_right($module, $cright_write_admin)) {

  $dis_debug = "
  <tr id='settings_debug'>
    <th>$l_set_debug ($_SESSION[set_debug])</th>
    <td>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_dg_id\" name=\"debug_id\" value=\"$cdg_id\" $dg_id /><label for=\"lbl_dg_id\">$l_dg_id</label>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_dg_param\" name=\"debug_param\" value=\"$cdg_param\" $dg_param /><label for=\"lbl_dg_param\">$l_dg_param</label>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_dg_sess\" name=\"debug_sess\" value=\"$cdg_sess\" $dg_sess /><label for=\"lbl_dg_sess\">$l_dg_sess</label>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_dg_sql\" name=\"debug_sql\" value=\"$cdg_sql\" $dg_sql /><label for=\"lbl_dg_sql\">$l_dg_sql</label>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_dg_exe\" name=\"debug_exe\" value=\"$cdg_exe\" $dg_exe /><label for=\"lbl_dg_exe\">$l_dg_exe</label>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_dg_solr\" name=\"debug_solr\" value=\"$cdg_solr\" $dg_solr /><label for=\"lbl_dg_solr\">$l_dg_solr</label>
    </td>
  </tr>";
}

///////////////////////////////////////////////////////////////////////////////
// For each LANGUAGE directory in the lang direcory (but .*)
// display en entry
///////////////////////////////////////////////////////////////////////////////
$lang_dir = dir("$path/../$obminclude/lang");
while ($entry=$lang_dir->read()) {
  if (!preg_match("/^(\.|_).*$/",$entry) && is_dir($lang_dir->path."/".$entry)) {
        $dis_lang .= "
      <a href=\"settings_index.php?lang=$entry\">
        <img src=\"".${"flag_$entry"}."\" alt=\"[Language $entry]\"/></a>";
  }
}
$lang_dir->close();


///////////////////////////////////////////////////////////////////////////////
// For each THEME directory in the themes direcory (but .*)
// display en entry
///////////////////////////////////////////////////////////////////////////////
$theme_dir = dir("../$path/resources/themes");
$dis_theme = '';
while ($entry = $theme_dir->read()) {
  $dotcase = strcmp($entry, '.'); 
  if (!preg_match("/^\..*$/",$entry) && strcmp($entry,'images') && is_dir($theme_dir->path."/".$entry)) {
		${'preview_'.$entry}="$resources_path/themes/$entry/images/preview_theme.gif"; 	  
    $dis_theme .= "
      <a href=\"settings_index.php?theme=$entry\">
      <img src=\"".${'preview_'.$entry}."\" alt=\"[Theme $entry]\" style=\"height:20%;width:20%;\"
      onmouseover=\"\" 
      onmouseout=\"\"/>
      </a>";
  }
}
$theme_dir->close();


///////////////////////////////////////////////////////////////////////////////
// HTML Display
///////////////////////////////////////////////////////////////////////////////
if ($params['form_user_pref']) {
  $display['msg'] .= display_ok_msg("$l_settings : $l_update_ok");
}
if(function_exists('hook_settings_custom_fields')) {
  $customFields = hook_settings_custom_fields();
}
$display['detail'] .= "
<!--User preferences current config -->

  <form action=\"settings_index.php\" method=\"get\">
  <fieldset id='currentSettigns1' class=\"detail infos\">
  <legend>$l_cur_settings</legend>
  <table>
  <tr id='settings_menu'>
    <th>$l_set_menu</th>
    <td>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"menu\" value=\"$cme_txt\" $me_txt />$l_me_txt</label></span>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"menu\" value=\"$cme_ico\" $me_ico />$l_me_ico</label></span>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"menu\" value=\"$cme_both\" $me_both />$l_me_both</label></span>
    </td>
  </tr><tr id='settings_autoDispay'>
    <th><label for=\"lbl_auto_display\">$l_auto_display</label></th>
    <td>
      <input type=\"checkbox\" class=\"box\" id=\"lbl_auto_display\" name=\"display\" value=\"yes\" ";
if ($_SESSION['set_display'] == 'yes') $display['detail'] .= "checked = \"checked\"";
$display['detail'] .= " /></td>
  </tr>
  <tr id='settings_rows'>
    <th>$l_set_rows</th>
    <td>
      <input size=\"3\" name=\"rows\" value=\"".$_SESSION['set_rows']."\" /></td>
  </tr>";

if ($cgp_show['module']['todo']) {

  $display['detail'] .= "

<!-- Todo Order config -->
  <tr id='settings_todo'>
    <th>$l_set_todo</th>
    <td>$sel_todo</td>
  </tr>";
}

$display['detail'] .= "

<!-- Data Source config -->
  <tr id='settings_datasource'>
    <th>$l_datasource</th>
    <td>$sel_dsrc</td>
  </tr>
  </table>
  </fieldset>
  <fieldset id='currentSettings2' class=\"detail infos\">
  <legend>$l_cur_settings</legend>
  <table>
<!-- Date Format config -->
  <tr id='settings_commentOrder'>
    <th>$l_set_commentorder</th>
    <td>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"commentorder\" value=\"$cco_chro\" $co_chro />$l_co_chro</label></span>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"commentorder\" value=\"$cco_rev\" $co_rev />$l_co_rev</label></span>
    </td>
  </tr><tr id='settings_mail'>
    <th><label for=\"lbl_send_mail\">$l_send_mail</label></th>
    <td class=\"adminText\">
      <input type=\"checkbox\" class=\"box\" id=\"lbl_send_mail\" name=\"mail\" value=\"yes\" ";
if ($_SESSION['set_mail'] == 'yes') $display['detail'] .= 'checked';

$display['detail'] .= " /></td>
  </tr>";

if ($cgp_show['module']['calendar']) {

$display['detail'] .= "
  <tr id='settings_mailParticipation'>
    <th><label for=\"lbl_mail_participation\">$l_send_mail_participation</label></th>
    <td class=\"adminText\">
      <input type=\"checkbox\" class=\"box\" id=\"lbl_mail_participation\" name=\"mail_participation\" value=\"yes\" ";
if ($_SESSION['set_mail_participation'] == 'yes') $display['detail'] .= 'checked';
$display['detail'] .= " /></td>
  </tr>";

  // days to display in the week view
  $display_days = $_SESSION['set_cal_display_days'];
  $start_week_day = strtotime($ccalendar_weekstart);
  if( $display_days == '0111110') {
    $without = 'selected="selected"';
  } else {
    $with = 'selected="selected"';
  }
  
  $dis_hour_b = "<select name=\"cal_first_hour\" style=\"width:4em;\">";
  for ($current_hour=0; $current_hour<24; $current_hour++) {
    if ($current_hour == $_SESSION['set_cal_first_hour']) {
      $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_b .= "</select>"; 

  $dis_hour_e = "<select name=\"cal_last_hour\" style=\"width:4em;\">";
  for ($current_hour=1; $current_hour<=24; $current_hour++) {
    if ($current_hour == $_SESSION['set_cal_last_hour']) {
      $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }

  $selected_alert = $_SESSION['set_cal_alert'];
  $dis_alert = "<select name=\"cal_alert\">
        <option value=\"$c_none\">$l_none</option>";      
  foreach ($ccalendar_alerts as $alert_sec => $alert_label) {
	if(!$alert_label) $alert_label = Of_Date::secondToString($alert_sec);  
    $dis_alert .= "<option value=\"$alert_sec\"";
    if ($selected_alert == $alert_sec) {
      $dis_alert .= " selected=\"selected\"";
    }
    $dis_alert .= ">$alert_label</option>";
  }
  $dis_alert .= '</select>';

  $dis_hour_e .= "</select>";    


  $display['detail'] .= "
  <tr id='settings_displayDays'>
    <th>$l_set_display_days</th>
    <td>
    <select name='sel_display_days'>
    <option $with value='1111111'>$l_with_weekend</option>
    <option $without value='0111110'>$l_without_weekend</option>
    </select>
    </td>
  </tr>
  <tr id='settings_calendarInterval'>
    <th>$l_set_cal_interval</th>
    <td>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"cal_interval\" value=\"$ccal_4\" $cal_4 />$l_cal_4</label></span>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"cal_interval\" value=\"$ccal_2\" $cal_2 />$l_cal_2</label></span>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"cal_interval\" value=\"$ccal_1\" $cal_1 />$l_cal_1</label></span>
    </td>
  </tr>
  <tr id='settings_calendarFirstHour'>
    <th>$l_set_cal_first_hour</th>
    <td>$dis_hour_b</td>
  </tr>
  <tr id='settings_calendarLastHour'>
    <th>$l_set_cal_last_hour</th>
    <td>$dis_hour_e</td>
    </tr>
  <tr id='settings_calendarAlert'>
    <th>$l_set_cal_alert</th>
    <td>$dis_alert</td>
  </tr>
  <tr id='settings_calendarAlldayOpacity'>
    <th>$l_set_allday_opacity</th>
    <td>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"cal_allday_opacity\" value=\"OPAQUE\" $cal_allday_opacity_busy />$l_opacity_busy</label></span>
      <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"cal_allday_opacity\" value=\"TRANSPARENT\" $cal_allday_opacity_free />$l_opacity_free</label></span>
    </td>
  </tr>
  <tr id='settings_calendarPublicFb'>
    <th><label for='public_fb'>$GLOBALS[l_set_public_fb]</label></th>
    <td><input type='checkbox' name='public_fb' id='public_fb' value='yes' ".(($_SESSION['set_public_fb'] == 'yes')?"checked='checked'":"")." /></td>
  </tr>  
";
}

$timezone_identifiers = DateTimeZone::listIdentifiers();
foreach($timezone_identifiers as $tz) {
  if($tz == Of_Date::getOption('timezone')) {
    $timezones .= "<option selected='selected' value='$tz'>$tz</option>";
  } else {
    $timezones .= "<option value='$tz'>$tz</option>";
  }
}

${'_'.Of_Date::getOption('timeformat')} = "selected='selected'";
$sel_timeformat = "
  <select name='timeformat'>
    <option value='12H' $_12H>1:00pm</option>
    <option value='24H' $_24H>13:00</option>
  </select>";

$display['detail'] .= "
  <tr id='settings_csvSeparator'>
  <th>$l_set_csv_sep</th>
  <td>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"csv_sep\" value=\"$ccsvd_sc\" $csvd_sc />$l_csvd_sc</label></span>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"csv_sep\" value=\"$ccsvd_tab\" $csvd_tab />$l_csvd_tab</label></span>
  </td>
  </tr>
  $dis_debug
  </table>
  </fieldset>
  <fieldset id='dateSettings' class=\"detail extra\">
  <legend>$l_date</legend>
  <table id='settings_dateUpdate'>
  <tr>
  <td>$l_set_date_upd</td>
  <td>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date_upd\" value=\"$cda_iso\" $da_upd_iso />$l_da_iso</label></span>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date_upd\" value=\"$cda_en\" $da_upd_en />$l_da_en</label></span>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date_upd\" value=\"$cda_fr\" $da_upd_fr />$l_da_fr</label></span>
  </td>
  <td>$l_set_date</td>
  <td>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date\" value=\"$cda_iso\" $da_iso />$l_da_iso</label></span>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date\" value=\"$cda_en\" $da_en />$l_da_en</label></span>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date\" value=\"$cda_fr\" $da_fr />$l_da_fr</label></span>
    <span class=\"NW\"><label><input type=\"radio\" class=\"box\" name=\"date\" value=\"$cda_txt\" $da_txt />$l_da_txt</label></span>
  </td>
  </tr>
  </table>
  <table id='settings_dateTimezone'>
  <tr>
  <td>$l_timezone</td>
  <td>
    <select name='timezone'>
      $timezones
    </select>
  </td>
  <td>
  </td>
  </tr>  
  <tr>
    <td>$l_timeformat</td>
    <td>$sel_timeformat</td>
    <td></td>
  </tr>
  </table>
  </fieldset>  
  $customFields
  <fieldset class=\"buttons\">
    <input name=\"form_user_pref\" type=\"hidden\" value=\"1\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_validate\" />
  </fieldset>
  </form>
  <hr />
<!-- Lang and theme current config -->
  <div class=\"detail infos\" id='langSettings'>
  <h1>$l_cur_lang</h1>
  <img src=\"".${'flag_'.$_SESSION['set_lang']}."\" alt=\"[language]\" />
  <h1>$l_set_lang</h1>
  $dis_lang
  </div>
  <div class=\"detail infos\" id='themeSettings'>
  <h1>$l_cur_theme</h1>
  <img src=\"".${"preview_".$_SESSION['set_theme']}."\" alt=\"[Theme]\"  />
  <h1>$l_set_theme</h1>
  $dis_theme
  </div>

<!-- Display available configs -->
";


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_settings);
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Settings parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_settings_params() {
  
  // Get global params
  $params = get_global_params('Settings');

  return $params;
}


//////////////////////////////////////////////////////////////////////////////
// Settings actions
//////////////////////////////////////////////////////////////////////////////
function get_settings_actions() {
  global $actions, $cright_read;

  $actions['settings']['index'] = array (
    'Url'      => "$path/settings/settings_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);
}

?>
