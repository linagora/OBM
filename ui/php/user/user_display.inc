<?php
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
// OBM - File : user_display.inc                                             //
//     - Desc : User Display functions File                                  //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

include_once("$obminclude/global.inc");

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['userobm_login'] = $l_login;
$fieldnames['userobm_commonname'] = $l_commonname;
$fieldnames['userobm_lastname'] = $l_lastname;
$fieldnames['userobm_firstname'] = $l_firstname;
$fieldnames['userobm_email'] = $l_email;
if($perm->check_right('user', $cright_read)) {
  $fieldnames['userobm_system'] = $l_system;
  $fieldnames['domain_label'] = $l_domain;
  $fieldnames['userobm_local'] = $l_local;
  $fieldnames['userobm_ext_id'] = $l_ext_id;
  $fieldnames['userobm_perms'] = $l_profile;
  $fieldnames['userobm_delegation'] = $l_delegation;
  $fieldnames['userobm_archive'] = $l_archive;
  $fieldnames['userobm_title'] = $l_title;
  $fieldnames['userobm_phone'] = $l_phone;
  $fieldnames['userobm_phone2'] = $l_phone2;
  $fieldnames['userobm_mobile'] = $l_mphone;
  $fieldnames['userobm_fax'] = $l_fax;
  $fieldnames['userobm_fax2'] = $l_fax2;
  $fieldnames['datebegin'] = $l_datebegin;
  $fieldnames['userobm_account_dateexp'] = $l_dateexp;
  $fieldnames['timelastaccess'] = $l_lastaccess;
  $fieldnames['userobm_mail_quota'] = $l_quota;
  $fieldnames['userobm_mail_login_date'] = $l_date;
  $fieldnames['userobm_nomade'] = $l_access_nomade;
  $fieldnames['userobm_samba_perms'] = $l_smb_perms;
  $fieldnames['userobm_description'] = $l_desc;
  $fieldnames['userobm_delegation_target'] = $l_delegation_target;
  $fieldnames['host_name'] = $l_mail_server;
}
$fieldnames["userobm_direction"] = $l_direction;
$fieldnames["userobm_service"] = $l_service;

///////////////////////////////////////////////////////////////////////////////
// Display User specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_user_data(&$OD, $fieldname, $link_ok) {
  global $path,  $ico_mail, $params,$obm,$cgp_use;
  global $l_enabled, $l_disabled, $l_forbidden, $l_perms;
  global $perm, $cright_write_admin, $module, $cright_read_admin;

  $ext_url = $params['ext_url'];
  $ext_element = $params['ext_element'];

  $status = $OD->data_set->f('userobm_status');

  if (($fieldname == 'userobm_login')) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_user_get_id(".$OD->data_set->f('userobm_id').",'".addslashes($OD->data_set->f('userobm_login'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_user_get_id_url('$ext_url',".$OD->data_set->f('userobm_id').");";
    } else if ($status != 'INIT') {
      if ($link_ok && $perm->check_right($module, $cright_read_admin))
        $res['url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f('userobm_id');
    }
  }

  if (($fieldname == 'userobm_lastname') && ($status == 'INIT')){
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_user_get_id(".$OD->data_set->f('userobm_id').",'".addslashes($OD->data_set->f('userobm_lastname'))."');";
    } else {
      $res['url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f('userobm_id');
    }
  }

  if (($fieldname == 'data_element') && $ext_element != '') {
    $res['name'] = "<span id=\"data-user-".$OD->data_set->f('userobm_id')."\" style=\"display:none;\">".
    $OD->data_set->f('userobm_lastname').' '.$OD->data_set->f('userobm_firstname').
    '</span>';
  }  

  else if ($fieldname == 'userobm_system') {
    $res['align'] = 'center';
    $system = $OD->data_set->f('userobm_system');
    if ($system) {
      $res['name'] = 'X';
    } else {
      $res['name'] = ' ';
    }
  }

  else if ($fieldname == 'userobm_perms') {
    $profil = $OD->data_set->f($fieldname); 
    $res['name'] = isset($GLOBALS["l_perm_$profil"]) ? $GLOBALS["l_perm_$profil"] : $profil;
  }

  else if ($fieldname == 'userobm_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = "&nbsp;";
      $res['txt_name'] = ' ';
    }
  }
  else if ($fieldname == 'userobm_samba_perms') {
    $res['align'] = 'center';
    $samba_perms = $OD->data_set->f('userobm_samba_perms');
    if ($samba_perms) {
      $res['name'] = 'X';
    } else {
      $res['name'] = "&nbsp;";
    }
  } 
  else if ($fieldname == 'userobm_email') {
    $email = $OD->data_set->f('userobm_email');
    if($OD->data_set->f('userobm_hidden') == 1 && !$perm->check_right($module, $cright_write_admin)) {
      $res['align'] = 'center';
      $res['name'] = '-';
    } elseif (strcmp($email ,'') != 0) {
      $email = get_entity_email($email, $OD->data_set->f('domain_name'));
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\">$email";
      $res['txt_name'] = "$email";
    }
  } 
  else if ($fieldname == 'userobm_nomade') {
    $nomade_perms = $OD->data_set->f('userobm_nomade_perms');
    $nomade_enable = $OD->data_set->f('userobm_nomade_enable');
    $email_nomade = nl2br($OD->data_set->f('userobm_email_nomade'));

    if ($nomade_perms) {
      if ($nomade_enable) {
	$res['name'] = $email_nomade;
      } else {
	$res['name'] = $l_perms;
      }
    } else {
      $res['name'] = $l_forbidden;
    }
  } 

  else if ($fieldname == 'userobm_local') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = "&nbsp;";
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'userobm_mail_quota') {
    $res['align'] = 'center';
    $mq = $OD->data_set->f($fieldname);
    if (($mq == 0) || ($mq == '')) {
      $res['name'] = '-';
    } else {
      $mqu = $OD->data_set->f('userobm_mail_quota_use');
      $percent = number_format($mqu / $mq * 100);
      $res['name'] = "$percent % ($mqu/$mq)";
    }
  }

  else if ($fieldname == 'timelastaccess') {
    $date = new Of_Date($OD->data_set->f($fieldname), 'GMT');
    $res['name'] = $date->getOutputDateTime();
  }
  else if ($fieldname == 'datebegin') {
    $datebegin = $OD->data_set->f($fieldname);
    $res['name'] = of_date_format($datebegin, 1);
  }
  else if ($fieldname == 'userobm_account_dateexp') {
    $dateexp = $OD->data_set->f("dateexp");
    $res['name'] = of_date_format($dateexp, 1);
  }
  else if ($fieldname == 'userobm_direction') {
    $res['name'] = $OD->data_set->f($fieldname);
  }

  return $res;
}

///////////////////////////////////////////////////////////////////////////////
// Display User search form
// Parameters:
//   - $user[]   : default form values
///////////////////////////////////////////////////////////////////////////////
function html_batch_user_search_form($user) {
  global $l_find, $l_title, $l_profile, $l_nomade, $l_service;
  global $l_group, $l_fax, $l_all, $l_exp, $obm, $cgp_use, $obm;
  global $l_quota, $l_delegation_target, $l_datebegin, $l_mail_server;
  global $display, $c_all, $l_recursive, $obm, $l_direction, $l_service;
  
  $delegation = stripslashes($user['delegation']);
  if ($cgp_use['property']['delegation'] && !($delegation)) $delegation = $obm['delegation_target'];
  $exp = $user['exp'];
  $exp_op = $user['exp_op'];
  $profile = $user['profile'];
  $title = $user['title'];
  $fax = $user['fax'];
  $direction = $user['direction'];
  $service = $user['service'];
  $group = $user['group'];
  $nomade = ($user['nomade'] == '1' ? 'checked' : '');
  $quota = $user['quota'];
  $quota_op = $user['quota_op'];

  if ($user['filter_entity']) {
    $dis_filter_entity = "<input name=\"filter_entity\" type=\"hidden\" value=\"$user[filter_entity]\" />";
    if($user['filter_pattern'])
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"$user[filter_pattern]\" />";
    else
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"read\" />";
  }

  // User defined data
  $block_userdata .= of_userdata_dis_search('user', $user);

  $form_action = "<input name=\"action\" type=\"hidden\" value=\"sel_batch_users\" />";

  $dis_delegation = of_delegation_dis_search_form($delegation);

  foreach ($user['profiles'] as $k_profile => $one_profile) {
    $profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
    $selected = "";
    if ($k_profile == $user['profile']) $selected = "selected=\"selected\"";
    $block_profile .= "<option value=\"$k_profile\" $selected>$profile</option>";
  }
  $options = array('=' => '=', '<' => '<', '>' => '>');
  foreach($options as $topt => $opt) {
    if($quota_op == $topt) {
      $block_quota_opt .= "<option selected='selected' value=\"".urlencode($opt)."\">$opt</option>";
    } else {
      $block_quota_opt .= "<option value=\"".urlencode($opt)."\">$opt</option>";
    }
  }
  foreach($options as $topt => $opt) {
    if($exp_op == $topt) {
      $block_exp_opt .= "<option selected='selected' value=\"".urlencode($opt)."\">$opt</option>";
    } else {
      $block_exp_opt .= "<option value=\"".urlencode($opt)."\">$opt</option>";
    }
  }

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("user_index.php")."\" class=\"search\"> ";

    $block .= "$dis_delegation
                ";
    $block .= "
    <label<br />
      <select type=\"text\" name=\"sel_exp_op\">
        $block_exp_opt
      </select>
    </label>
    <label>$l_exp<br />
      <input type=\"text\" name=\"tf_exp\" size=\"16\"  maxlength=\"32\" value=\"$exp\" />
    </label>";
    $block .= "
    <label>$l_profile<br />
      <select type=\"text\" name=\"sel_profile\">
        <option value=\"$c_all\"> $l_all</option>
        $block_profile
      </select>
    </label>";
    $block .= "
    <label>$l_title<br />
      <input type=\"text\" name=\"tf_title\" size=\"16\" maxlength=\"32\"
      value=\"$title\" />
    </label>";
    $block .= "
    <label>$l_fax<br />
      <input type=\"text\" name=\"tf_fax\" size=\"16\" maxlength=\"32\"
      value=\"$fax\" />
    </label>";
    $block .= "
    <label>$l_direction<br />
      <input type=\"text\" name=\"tf_direction\" size=\"16\" maxlength=\"32\"
      value=\"$direction\" />
    </label>";
    $block .= "
    <label>$l_service<br />
      <input type=\"text\" name=\"tf_service\" size=\"16\" maxlength=\"32\"
      value=\"$service\" />
    </label>";
    $block .= "
    <label>$l_group<br />
      <input type=\"text\" name=\"tf_group\" size=\"16\" maxlength=\"32\"
      value=\"$group\" />
    </label>";
    $block .= "
      <label>$l_nomade<br />
        <input type=\"checkbox\" name=\"cba_nomade\" value=\"1\" $nomade />
      </label>";

    $block .= "
      <label>$l_quota<br \>
      <select type=\"text\" name=\"sel_quota_op\">
        $block_quota_opt
      </select>
      </label>
      <label><br />
      <input type=\"text\" name=\"tf_quota\" size=\"16\" maxlength=\"32\" value=\"$quota\" />
      </label>";

  $block .= "<label>&nbsp;<br />
      $form_action
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $dis_filter_entity
    </label>
";

  $block.= "<p class=\"CL\"></p>
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User search form
// Parameters:
//   - $user[]   : default form values
///////////////////////////////////////////////////////////////////////////////
function html_user_search_form($user) {
  global $l_login, $l_lastname, $l_profile, $l_domain, $l_archive, $l_add_user;
  global $l_group, $l_phone, $l_desc, $l_find, $l_all, $l_firstname, $l_exp, $l_commonname;
  global $l_email, $l_quota, $l_delegation_target, $l_datebegin, $l_mail_server;
  global $display, $c_all, $l_recursive, $obm;
  
  $login = $user['login'];
  $profile = $user['profile'];
  $domain_id = $user['domain_id'];
  $cname = stripslashes($user['commonname']);
  $lname = stripslashes($user['lastname']);
  $fname = stripslashes($user['firstname']);
  $email = $user['email'];
  $phone = $user['phone'];
  $desc = $user['desc'];
  $group = $user['group'];
  $delegation = stripslashes($user['delegation']);
  $exp = $user['exp'];
  $archive = ($user['archive'] == '1' ? 'checked' : '');
  $recursive = ($user['recursive'] == '1' ? 'checked' : '');
  $popup = $user['popup'];
  $next_action = $user['next_action'];
  $user_action = $user['action'];
  $datebegin = $user['datebegin'];
  $direction = $user["direction"];
  $service = $user["service"];

  $delegation_target = $user['delegation_target'];
  $mail_server_host_name = $user['mail_server_host_name'];
  $quota = $user['quota'];
  
  if ($user['filter_entity']) {
    $dis_filter_entity = "<input name=\"filter_entity\" type=\"hidden\" value=\"$user[filter_entity]\" />";
    if($user['filter_pattern'])
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"$user[filter_pattern]\" />";
    else
      $dis_filter_entity .= "<input name=\"filter_pattern\" type=\"hidden\" value=\"read\" />";
  }

  // User defined data
  $customFields = new OBM_Form_Field('user');
  $block_userdata .= of_userdata_dis_search('user', $user);
  $block_userfields = $customFields->buildSearchForm($user);

  if ($popup) {
    $ext_action = $user['ext_action'];
    $ext_title = stripslashes($user['ext_title']);
    $ext_url = $user['ext_url'];
    $ext_id = $user['ext_id'];
    $ext_target = $user['ext_target'];
    $ext_widget = $user['ext_widget'];
    $ext_element = $user['ext_element'];
    $ext_container = $user['ext_container'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\" />
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\" />
	          <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\" />
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />
            <input name=\"ext_container\" type=\"hidden\" value=\"$ext_container\" />";

    if ($ext_title == '') {
      $ext_title = $l_add_users;
    }
    $display['title'] = $ext_title;
  }

  $form_action = "<input name=\"action\" type=\"hidden\" value=\"search\" />";

  foreach ($user['profiles'] as $k_profile => $one_profile) {
    $profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
    $selected = "";
    if ($k_profile == $user['profile']) $selected = "selected=\"selected\"";
    $block_profile .= "<option value=\"$k_profile\" $selected>$profile</option>";
  }

  if ($obm['domain_global']) {
    $sel_domain = of_domain_dis_select($domain_id, 'all');
    $dis_domain = "<label>$l_domain<br />
      $sel_domain
    </label>";
  }

  $dis_delegation = of_delegation_dis_search_form($delegation);
  $display_pref = get_display_pref($obm['uid'],'user');

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("user_index.php")."\" class=\"search\"> ";
  
  if ($display_pref['userobm_login']['status'] != 0) {
    $block .= "
<label>$l_login<br />
  <input type=\"text\" name=\"tf_login\" size=\"16\" maxlength=\"32\" value=\"$login\" />
</label>";
  }
  if ($display_pref['userobm_lastname']['status'] != 0) {
    $block .= "
<label>$l_lastname<br />
<input type=\"text\" name=\"tf_lastname\" size=\"16\" maxlength=\"32\" value=\"$lname\" />
</label>";
  }
  if ($display_pref['userobm_firstname']['status'] != 0) {
    $block .= "
<label>$l_firstname<br />
  <input type=\"text\" name=\"tf_firstname\" size=\"16\" maxlength=\"32\" value=\"$fname\" />
</label>";
  }
  if ($display_pref['userobm_commonname']['status'] != 0) {
    $block .= "
<label>$l_commonname<br />
  <input type=\"text\" name=\"tf_commonname\" size=\"16\" maxlength=\"32\" value=\"$cname\" />
</label>";
  }
 if ($display_pref['domain_label']['status'] != 0) {
    $block .= "
$dis_domain";
  }
  if($display_pref['userobm_perms']['status'] != 0) {
    $block .= "
<label>$l_profile<br />
      <select type=\"text\" name=\"sel_profile\">
        <option value=\"$c_all\"> $l_all</option>
        $block_profile
      </select>
    </label>";
  }
  if($display_pref['userobm_delegation']['status'] != 0) {
    $block .= "
$dis_delegation";
  }
  if($display_pref['userobm_email']['status'] != 0) {
    $block .= "
<label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" maxlength=\"32\"
      value=\"$email\" />
    </label>";
  }
  if($display_pref['userobm_phone']['status'] != 0) {
    $block .= "
<label>$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"16\" maxlength=\"16\"
      value=\"$phone\" />
    </label>";
  }
  if($display_pref['userobm_description']['status'] != 0) {
    $block .= "
<label>$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"16\" maxlength=\"32\"
      value=\"$desc\" />
    </label>";
  }
  if($display_pref['userobm_account_dateexp']['status'] != 0) {
    $block .= "
<label>$l_exp<br />
      <input type=\"text\" name=\"tf_exp\" size=\"16\" maxlength=\"32\"
      value=\"$exp\" />
    </label>";
  }
  if($display_pref['userobm_login']['status'] != 0) {
    $block .= "
<label>$l_group<br />
      <input type=\"text\" name=\"tf_group\" size=\"16\" maxlength=\"32\"
      value=\"$group\" />
    </label>";
  }
  if($display_pref['userobm_login']['status'] != 0) {
    $block .= "
     $block_userdata
     $block_userfields";
  }
  if($display_pref['userobm_delegation_target']['status'] != 0) {
    $block .= "
<label>$l_delegation_target<br \>
      <input type=\"text\" name=\"tf_delegation_target\" size=\"16\" maxlength=\"32\"
      value=\"$delegation_target\" />
    </label>";
  }
  if($display_pref['host_name']['status'] != 0) {
    $block .= "
<label>$l_mail_server<br \>
      <input type=\"text\" name=\"tf_host_name\" size=\"16\" maxlength=\"32\"
      value=\"$mail_server_host_name\" />
    </label>";
  }
  if($display_pref['userobm_mail_quota']['status'] != 0) {
    $block .= "
<label>$l_quota<br \>
      <input type=\"text\" name=\"tf_quota\" size=\"16\" maxlength=\"32\"
      value=\"$quota\" />
    </label>";
  }
  if ($display_pref['datebegin']['status'] != 0) {
    $block .= "
    <label class=\"NW\">$l_datebegin<br />
      <input class=\"datePicker\" type=\"text\"  name=\"tf_datebegin\" value=\"$datebegin\" autocomplete=\"off\"/>
    </label>";
  }
  if ($display_pref['userobm_archive']['status'] != 0) {
    $block .= "
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>";
  }
  if ($display_pref['userobm_direction']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_direction]<br />
      <input type=\"text\" name=\"tf_direction\" size=\"16\" maxlength=\"32\"
      value=\"$direction\" />
    </label>";
  }
  if ($display_pref['userobm_service']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_service]<br />
      <input type=\"text\" name=\"tf_service\" size=\"16\" maxlength=\"32\"
      value=\"$service\" />
    </label>";
  }
  $block .= "
    <label>&nbsp;<br />
      $form_action
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $dis_filter_entity
      $ext
    </label>";

    $block.= "<p class=\"CL\"></p>
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result
// Parameters:
//   - $user[]   : user search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_user_search_list($user, $strongest_profile_level=null, $user_can_manage_peers=null) {
  global $l_found, $l_no_found;
  global $display, $obm;

  $popup = $user['popup'];
  $filter_entity = $user['filter_entity'];
  
  $obm_q = run_query_user_search($user, $strongest_profile_level, $user_can_manage_peers);

  $prefs = get_display_pref($obm['uid'], 'user');
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_user $l_found");
    $block = html_user_search_list($obm_q, $prefs, $user, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the User Search result
// Parameters:
//   - $obm_q    : database result (user list)
//   - $prefs    : the fields which have to be displayed
//   - $user[]   : user search criteria
//     keys used : login, lastname, pemrs
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_user_search_list($obm_q, $prefs, $user, $popup) {
  global $l_close, $l_add, $l_edit_users;

  $login = urlencode($user['login']);
  $lname = urlencode($user['lastname']);
  $fname = urlencode($user['firstname']);
  $cname = urlencode($user['commonname']);
  $profile = $user['profile'];
  $email = $user['email'];
  $delegation = urlencode($user['delegation']);
  $desc = urlencode($user['desc']);
  $exp = urlencode($user['exp']);
  $group = urlencode($user['group']);
  $archive = $user['archive'];
  $action = $user['action'];
  $phone = urlencode($user['phone']);
  $service = $user["service"];
  $direction = $user["direction"];
  $quota = $user['quota'];


  // User defined data
  $customFields = new OBM_Form_Field('user');
  $url_userdata = of_userdata_get_url_search_params('user', $user);
  $url_userfields = $customFields->buildSearchUrl($user);
  if ($popup) {
    $ext_action = $user['ext_action'];
    $ext_url = $user['ext_url'];
    $ext_id = $user['ext_id'];
    $ext_target = $user['ext_target'];
    $ext_widget = $user['ext_widget'];
    $ext_widget_text = $user['ext_widget_text'];
    $ext_element = $user['ext_element'];
    $ext_container = $user['ext_container'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;ext_container=$ext_container";
  }

  $url = url_prepare("user_index.php?action=$action&amp;tf_login=$login&amp;tf_phone=$phone&amp;tf_firstname=$fname&amp;tf_lastname=$lname&amp;tf_commonname=$cname&amp;sel_profile=$profile&amp;tf_email=$email&amp;tf_delegation=$delegation&amp;tf_desc=$desc&amp;tf_exp=$exp&amp;tf_group=$group&amp;tf_service=$service&amp;tf_direction=$direction&amp;tf_quota=$quota&amp;cba_archive=$archive$url_userdata$url_ext$url_userfields");

  $user_d = new OBM_DISPLAY('DATA', $prefs, 'user');
  if ($popup) {
    //    if ($ext_action == 'ext_get_ids') {
    $user_d->display_link = false;
    $user_d->data_cb_text = 'X';
    $user_d->data_idfield = 'userobm_id';
    $user_d->data_cb_name = 'data-user-';
    if ($ext_element != '') {
      $user_d->data_cb_name = '';
      $user_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this); return false;\">";
    } elseif ($ext_widget != '') {
      $user_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $user_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }
    $user_d->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </div>
      </form>";

  /*  else if ($ext_action == "ext_get_id"){
    $user_d->display_link = false;
    if ($ext_url != '') {
      $user_d->display_ext = 'get_id_url';
    } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $user_d->display_ext = 'get_id';
    }
  }*/

  $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
      
  } else if ($user['action'] == 'sel_batch_users') {
    $user_d->data_cb_text = 'X';
    $user_d->data_idfield = 'userobm_id';
    $user_d->data_cb_name = 'data-user-';
    
    $user_d->data_form_head = "
      <form method=\"post\" action=\"".url_prepare('user_index.php')."\">";
    $user_d->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_edit_users\" />
        <input type=\"hidden\" name=\"action\" value=\"edit_batch_values\" />
      </div>
      </form>";
    
  }
  
  $user_d->data_set = $obm_q;
  $user_d->data_header = 'both';
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display('dis_user_data');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Consultation
// Parameters:
//   - $user : user database result
///////////////////////////////////////////////////////////////////////////////
function dis_user_consult($user) {
  global $display, $l_err_reference;

  $id = $user['user_id'];

  if ($id > 0) {
    $obm_q = run_query_user_detail($id);
    if ($obm_q->num_rows() == 1) {
      $block = html_user_consult($obm_q);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation
// Parameters:
//   - $obm_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_consult($obm_q) {
  global $l_login, $l_password, $l_profile, $l_kind, $l_lastname, $l_firstname, $l_commonname;
  global $l_domain, $l_user, $l_lastaccess, $l_archive, $l_datebegin, $l_dateexp, $l_desc;
  global $l_coord, $l_address, $l_postcode, $l_town, $l_expresspostal, $l_title;
  global $l_delegation_target, $l_delegation, $l_direction, $l_service;
  global $l_company, $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_email;
  global $l_group_member, $l_group, $l_gid, $l_local, $l_ext_id,$l_hidden;
  global $l_web, $l_web_mode, $l_web_all, $l_web_list_only;
  global $l_mail, $l_mail_ext, $l_email, $l_quota, $l_email_nomade, $l_local_copy;
  global $l_nomade, $l_perms, $l_access, $l_host, $l_ip, $l_empty;
  global $l_vacation, $l_vacation_message, $l_vacation_datebegin;
  global $l_vacation_dateend,$l_mail_server,$perm, $cright_write_admin;
  global $l_enabled, $l_disabled, $l_web_consult_list, $l_photo, $module;
  global $l_smb, $l_smb_perms, $l_smb_home, $l_smb_home_drive, $l_smb_logon;
  global $obm, $cgp_use, $path, $display, $l_yes, $l_no;

  // Check if domain is a samba domain
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  $profiles = get_all_profiles(false);

  $id = $obm_q->f('userobm_id');
  $domain_label = ($obm_q->f('domain_label') != '' ? $obm_q->f('domain_label') : 'GLOBAL');
  $system = $obm_q->f('userobm_system');
  $login = $obm_q->f('userobm_login');
  $local = ($obm_q->f('userobm_local') == 1 ? $l_yes : $l_no);
  $ext_id = $obm_q->f('userobm_ext_id');
  $gid = $obm_q->f('userobm_gid');
  $group_name = $obm_q->f('group_name');
  $profile = $obm_q->f('userobm_perms');
  $dis_profile = isset($GLOBALS["l_perm_$profile"]) ? $GLOBALS["l_perm_$profile"] : $profile;
  $kind = $obm_q->f('userobm_kind');
  $lname = $obm_q->f('userobm_lastname');
  $fname = $obm_q->f('userobm_firstname');
  $cname = $obm_q->f('userobm_commonname');
  $title = $obm_q->f('userobm_title');
  $delegation_target = $obm_q->f('userobm_delegation_target');
  $delegation = $obm_q->f('userobm_delegation');
  $desc = $obm_q->f('userobm_description');
  $datebegin = of_date_format($obm_q->f('datebegin'), true);
  $dateexp = of_date_format($obm_q->f('dateexp'), true);
  $lastaccess = of_datetime_format($obm_q->f('userobm_timelastaccess'), 1);
  $archive = ($obm_q->f('userobm_archive') == '1' ? $l_yes : $l_no);
  $hidden = ($obm_q->f('userobm_hidden') == '1' ? $l_yes : $l_no);
  $photo_id = $obm_q->f('userobm_photo_id');

  $phone = $obm_q->f('userobm_phone');
  $phone2 = $obm_q->f('userobm_phone2');
  $mobile = $obm_q->f('userobm_mobile');
  $fax = $obm_q->f('userobm_fax');
  $fax2 = $obm_q->f('userobm_fax2');
  $company = $obm_q->f('userobm_company');
  $direction = $obm_q->f('userobm_direction');
  $service = $obm_q->f('userobm_service');
  $ad1 = $obm_q->f('userobm_address1');
  $ad2 = $obm_q->f('userobm_address2');
  $ad3 = $obm_q->f('userobm_address3');
  $zip = $obm_q->f('userobm_zipcode');
  $town = $obm_q->f('userobm_town');
  $cdx = $obm_q->f('userobm_expresspostal');
  $ctry_name = $obm_q->f('country_name');

  $web_perms = $obm_q->f('userobm_web_perms');
  $web_all = $obm_q->f('userobm_web_all');
  $mail_perms = $obm_q->f('userobm_mail_perms');
  $mail_ext_perms = $obm_q->f('userobm_mail_ext_perms');
  //  $mail = get_mail_info();
  if( $obm_q->f('domain_name') != '') {
    $main_domain = $obm_q->f('domain_name');
  } else {
    $main_domain = $obm['domain_name'];
  }  
  if($obm_q->f('userobm_hidden') != 1 || $perm->check_right($module, $cright_write_admin)) {
    $email = get_entity_email($obm_q->f('userobm_email'), $main_domain, false);
  } else {
    $email = '';
  }
  $mail_server_id = $obm_q->f('userobm_mail_server_id');
  $mail_server_host_name = $obm_q->f('mailserver_hostname');
  $mail_quota = $obm_q->f('userobm_mail_quota');
  $email_nomade = str_replace("\r\n","<br/>",$obm_q->f('userobm_email_nomade'));
  $nomade_perms = $obm_q->f('userobm_nomade_perms');
  $nomade_enable = $obm_q->f('userobm_nomade_enable');
  $nomade_local_copy = $obm_q->f('userobm_nomade_local_copy');
  $vacation_enable = $obm_q->f('userobm_vacation_enable');
  //$vacation_datebegin = of_datetime_format($obm_q->f('userobm_vacation_datebegin'), 1);
  $vacation_datebegin = new Of_Date($obm_q->f('userobm_vacation_datebegin'), 'GMT');
  if($vacation_datebegin->error()) {
    $vacation_datebegin = '---';
  } else {
    $vacation_datebegin = $vacation_datebegin->getOutputDateTime();
  }
  $vacation_dateend = new Of_Date($obm_q->f('userobm_vacation_dateend'), 'GMT');
  if($vacation_dateend->error()) {
    $vacation_dateend = '---';
  } else {
    $vacation_dateend = $vacation_dateend->getOutputDateTime();
  }
  $vacation_message = stripslashes($obm_q->f('userobm_vacation_message'));
  $smb_perms = $obm_q->f('userobm_samba_perms');
  $smb_home = $obm_q->f('userobm_samba_home');
  $smb_home_drive = $obm_q->f('userobm_samba_home_drive');
  $smb_logon = $obm_q->f('userobm_samba_logon_script');

  $nomade_perms = ($nomade_perms ? $l_yes : $l_no);
  $nomade_enable = ($nomade_enable ? $l_yes : $l_no);
  $nomade_local_copy = ($nomade_local_copy ? $l_yes : $l_no);
  $vacation_enable = ($vacation_enable ? $l_yes : $l_no);
  $vacation_message = ($vacation_enable ? nl2br($vacation_message) : '---');

  if ($host_name == '') {
    $dis_host = $l_empty;
  } else {
    $dis_host = "<a href=\"$path/host/host_index.php?action=detailconsult&amp;param_host=$host_id\">$host_name</a>";
  }
  if ($host_ip == '') $host_ip = $l_empty;
  $web_access = ($web_all ? $l_web_all : $l_web_list_only);
  $mail_state  = ($mail_perms ? $l_enabled : $l_disabled);
  $mail_ext_state  = ($mail_ext_perms ? $l_enabled : $l_disabled);
  $smb_state  = ($smb_perms ? $l_enabled : $l_disabled);

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult('user', $id);
  $customFields = new OBM_Form_Field('user');
  $block_userfields = $customFields->buildConsultDetail($id);

  // Liste des groupes de l'utilisateur
  $g_q = run_query_user_user_group($id);
  if ($g_q->num_rows() > 0) {
    $dis_group = "
    <div class=\"detail infos\">
    <h1>$l_group_member</h1>
    <table>
    <tr><td>";
    while ($g_q->next_record()) {
      $grp_id = $g_q->f('group_id');
      $grp_name = $g_q->f('group_name');
      $dis_group .= "
    </td></tr><tr>
      <th>$l_group</th>
      <td><a href=\"$path/group/group_index.php?action=detailconsult&amp;group_id=$grp_id\">$grp_name</a></td>";
    }
    $dis_group .= "
    </tr>
    </table></div>";
  }

  // Target Delegation handling (if user as delegation admin realm)
  if ($cgp_use['property']['delegation']) {
    $dis_delegation_target = "
    <tr>
      <th>$l_delegation_target</th>
      <td>$delegation_target</td>
    </tr>";

    $dis_delegation = of_delegation_dis_block_consult($delegation);
  }

  // Section Web
  if ($cgp_use['service']['web']) {
    $web_section = "
    <div class=\"detail infos\">
    <h1>$l_access</h1>

    <table>
    <tr>
      <th>$l_web</th>
      <td>$web_state</td>
    </tr>
    $dis_access
    </table>
    </div>";
  }

  // Section Mail
  if ($cgp_use['service']['mail']) {
    $mail_section = "
    <div class=\"detail infos\">
    <h1>$l_mail</h1>

    <table>
    <tr>
      <th>$l_mail</th>
      <td>$mail_state</td>
    </tr>
    <tr>
      <th>$l_mail_server</th>
      <td>";
      if ($mail_server_id) {
        $mail_section .= "<a href=\"$path/host/host_index.php?action=detailconsult&host_id=".$mail_server_id."\">".$mail_server_host_name."</a>";
      } else {
        $mail_section .= '-';
      }
      $mail_section .= "</td>
    </tr>
    <tr>
      <th>$l_email</th>
      <td>$email </td>
    </tr>
    <tr>
      <th>$l_quota</th>
      <td>$mail_quota</td>
    </tr>
    </table>
    </div>";
  } else {
    $mail_section = "
    <div class=\"detail infos\">
    <h1>$l_mail</h1>

    <table>
    <tr>
      <th>$l_email</th>
      <td>$email</td>
    </tr>
    </table>
    </div>";
  }

  if ($cgp_use['service']['mail_nomad']) {
    $nomad_section = "
    <div class=\"detail infos\">
    <h1>$l_nomade</h1>

    <table>
    <tr>
      <th>$l_email_nomade</th>
      <td>$email_nomade</td>
    </tr>
    <tr>
      <th>$l_perms</th>
      <td>$nomade_perms</td>
    </tr>
    <tr>
      <th>$l_enabled</th>
      <td>$nomade_enable</td>
    </tr>
    <tr>
      <th>$l_local_copy</th>
      <td>$nomade_local_copy</td>
    </tr>
    </table>
    </div>";
  }

  if ($cgp_use['service']['mail_vacation']) {
    $vacation_section = "
    <div class=\"detail infos\">
    <h1>$l_vacation</h1>

    <table>
    <tr>
      <th>$l_enabled</th>
      <td>$vacation_enable</td>
    </tr>
    <tr>
      <th>$l_vacation_datebegin</th>
      <td>$vacation_datebegin</td>
    </tr>
    <tr>
      <th>$l_vacation_dateend</th>
      <td>$vacation_dateend</td>
    </tr>
    <tr>
      <th>$l_vacation_message</th>
      <td>$vacation_message</td>
    </tr>
    </table>
    </div>";
  }

  if (($cgp_use['service']['samba']) && ($samba_domain)) {
    $samba_section = "
    <div class=\"detail infos\">
    <h1>$l_smb</h1>

    <table>
    <tr>
      <th>$l_smb_perms</th>
      <td>$smb_state</td>
    </tr>";
    if ($smb_perms == '1') {
      $samba_section .= "      
      <tr>
        <th>$l_smb_home</th>
        <td>$smb_home</td>
      </tr>
      <tr>
        <th>$l_smb_home_drive</th>
        <td>$smb_home_drive</td>
      </tr>
      <tr>
        <th>$l_smb_logon</th>
        <td>$smb_logon</td>
      </tr>";
    }
    $samba_section .= "      
    </table>
    </div>";
  }

  // Mobile devices
  $mobile_devices = html_user_mobile_devices($id, 'consult');

  // Photo
  if ($photo_id > 0) {
    $block_photo = "<img src=\"$path/document/document_index.php?action=accessfile&document_id=$photo_id\" alt=\"[Photo]\"/ />";
  } else {
    $block_photo = '';
  }
  
  $display['detailInfo'] = display_record_info($obm_q);
  $display['title'] = "$fname $lname";

  $block = "
<div class=\"detail infos\" style=\"overflow:hidden\">
  <h1>$l_user</h1>
 
  <table>
  <tr>
    <th>$l_kind</th>
    <td>$kind</td>
  </tr>
  <tr>
    <th>$l_lastname</th>
    <td>$lname</td>
  </tr>
  <tr>
    <th>$l_firstname</th>
    <td>$fname</td>
  </tr>
   <tr>
    <th>$l_commonname</th>
    <td>$cname</td>
  </tr> 
  <tr>
    <th>$l_login</th>
    <td>$login</td>
  </tr>
  <tr>
    <th>$l_domain</th>
    <td>$domain_label</td>
  </tr>
  <tr>
    <th>$l_local</th>
    <td>$local</td>
  </tr>
  <tr>
    <th>$l_archive</th>
    <td>$archive</td>
  </tr>    
  <tr>
    <th>$l_ext_id</th>
    <td>$ext_id</td>
  </tr>
  <tr>
    <th>$l_gid</th>
    <td>$group_name</td>
  </tr>
  <tr>
    <th>$l_profile</th>
    <td>$dis_profile</td>
  </tr>
  $dis_delegation
  $dis_delegation_target
  <tr>
    <th>$l_title</th>
    <td>$title</td>
  </tr>
  <tr>
    <th>$l_datebegin</th>
    <td>$datebegin</td>
  </tr>
  <tr>
    <th>$l_dateexp</th>
    <td>$dateexp</td>
  </tr>
  <tr>
    <th>$l_lastaccess</th>
    <td>$lastaccess</td>
  </tr>
  <tr>
    <th>$l_photo</th>
    <td>$block_photo</td>
  </tr>
  $block_userfields
</table>
</div>

<div class=\"detail infos\">
  <h1>$l_coord</h1>
  <table>
  <tr>
    <th>$l_phone</th>
    <td>$phone</td>
  </tr>
  <tr>
    <th>$l_phone2</th>
    <td>$phone2</td>
  </tr>
  <tr>
    <th>$l_mphone</th>
    <td>$mobile</td>
  </tr>
  <tr>
    <th>$l_fax</th>
    <td>$fax</td>
  </tr>
  <tr>
    <th>$l_fax2</th>
    <td>$fax2</td>
  </tr>
  <tr>
    <th>$l_company</th>
    <td>$company</td>
  </tr>
  <tr>
    <th>$l_direction</th>
    <td>$direction</td>
  </tr>
  <tr>
    <th>$l_service</th>
    <td>$service</td>
  </tr>
  <tr>
    <th>$l_address 1</th>
    <td>$ad1</td>
  </tr>
  <tr>
    <th>$l_address 2</th>
    <td>$ad2</td>
  </tr>
  <tr>
    <th>$l_address 3</th>
    <td>$ad3</td>
  </tr>
  <tr>
    <th>$l_postcode</th>
    <td>$zip</td>
  </tr>
  <tr>
    <th>$l_town</th>
    <td>$town</td>
  </tr>
  <tr>
    <th>$l_expresspostal</th>
    <td>$cdx</td>
  </tr>
  <tr>
    <th>$l_country</th>
    <td>$ctry_name</td>
  </tr>
  </table>
</div>

<div class=\"detail infos\">
  <h1>$l_desc</h1>
  <table>
  <tr>
    <th>$l_desc</th>
    <td>$desc</td>
  </tr>
  </table>
</div>
$block_userdata
$web_section
$mail_section
$nomad_section
$vacation_section
$samba_section
$dis_group
$mobile_devices";

  return $block;
}


/**
 * Mobile device management HTML
 */
function html_user_mobile_devices($user_id, $type, $user=false) {
  global $perm;

  $is_superman = 'obm.vars.consts.isSuperman='.($perm->is_superman('user')==true?1:0).';';
  $perms_error = 'obm.vars.labels.permsError="'.phpStringToJsString($GLOBALS['l_error_permission']).'";';
  $update_ok = 'obm.vars.labels.updateOk="'.phpStringToJsString($GLOBALS['l_update_ok']).'";';
  $update_error = 'obm.vars.labels.updateError="'.phpStringToJsString($GLOBALS['l_update_error']).'";';
  $unlink = 'obm.vars.labels.unlink="'.phpStringToJsString($GLOBALS['l_unlink_mobile']).'";';

  $mobile_devices  = "";
  $opush = false;
  $obm_q = new DB_OBM();
  foreach($obm_q->table_names() as $table) {
    if ($table['table_name'] == 'opush_device') {
      $opush = true;
    }
  }
  if ($user_id && $opush) {
    $mobile_q = run_query_userobm_get_devices($user_id);
    while($mobile_q->next_record()) {
      $mobile_id = $mobile_q->f('id');
      $mobile_owner = $mobile_q->f('owner');
      $mobile_user_agent = $mobile_q->f('identifier');
      $mobile_type = $mobile_q->f('type');
      $policy = $mobile_q->f('policy');
      $perms_device_id = $mobile_q->f('device_id');
      $last_sync = $GLOBALS['l_undef'];
      if ($mobile_q->f('last_sync')) {
        $last_sync = new Of_Date($mobile_q->f('last_sync'), 'GMT');
      }
      $first_sync = $GLOBALS['l_undef'];
      if ($mobile_q->f('first_sync')) {
        $first_sync = new Of_Date($mobile_q->f('first_sync'), 'GMT');
      }
      $u = get_user_info($user_id);
      $delegation = of_delegation_check_update_rights($user, $u['delegation'] );
      $devices .= "<tr id='mobile_$mobile_id' class='mobile'>
        <td>$mobile_user_agent</td>
        <td>$mobile_type</td>
        <td>$last_sync</td>
        <td><img src='$GLOBALS[ico_search]' onclick='obm.userMobileDeviceManager.showInfo(\"$mobile_id\");'/></td>
        <td><img src='$GLOBALS[ico_delete]' onclick='obm.userMobileDeviceManager.removeMobile(\"$mobile_id\");'/></td>
      </tr>";
      $userMobileDeviceManager .= "obm.userMobileDeviceManager.addDevice('$mobile_id', '$mobile_owner', '$first_sync', '$last_sync', '$perms_device_id', '$delegation');";
    }

    $mobile_devices = "
      <div class='detail infos'>
      <h1>$GLOBALS[l_mobile_devices]</h1>";
    $mobile_devices_partnership = "";
    $mobile_devices_close = "</div>";

    if ($type == 'form') {
    $mobile_devices = "
      <fieldset class='detail infos'>
      <legend>$GLOBALS[l_mobile_devices]</legend>";
    $mobile_devices_partnership = "<tr>
      <td><input type='button' id='mobile_partnership' value='$GLOBALS[l_mobile_add_partnership]' /></td>
      <td></td>
    </tr>";
    $mobile_devices_close = "</fieldset>";
    }

    $mobile_devices .= "
      <table class='spreadSheet'>
        <thead>
          <tr>
            <td>$GLOBALS[l_mobile_identifier]</td>
            <td>$GLOBALS[l_type]</td>
            <td>$GLOBALS[l_last_sync]</td>
            <td></td>
            <td></td>
          </tr>
        </thead> 
        $devices
      </table>

      <table id='mobile_info' style='visibility:hidden;'>
        <tr>
          <th>$GLOBALS[l_first_sync]</th>
          <td id='mobile_first_sync'></td>
        </tr>
        $mobile_devices_partnership
      </table>
      $mobile_devices_close
      <script type='text/javascript'>
        $is_superman
        $perms_error
        $update_ok
        $update_error
        $unlink
        obm.vars.labels.addPartnership = '".phpStringToJsString($GLOBALS['l_mobile_add_partnership'])."';
        obm.vars.labels.removePartnership = '".phpStringToJsString($GLOBALS['l_mobile_remove_partnership'])."';
        obm.userMobileDeviceManager = new Obm.UserMobileDeviceManager();
        $userMobileDeviceManager
      </script>";
  }

  return $mobile_devices;

}

///////////////////////////////////////////////////////////////////////////////
// HTML Display User Group form
// Parameters:
//   - $usr_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_group_consult($usr_q) {
  global $l_update_group, $l_user, $l_group_member, $l_group, $l_groups;
  global $ico_add, $ico_delete,$path;

  $id = $usr_q->f('userobm_id');
  $domain_id = $usr_q->f('userobm_domain_id');
  $gid = $usr_q->f('userobm_gid');
  $login = $usr_q->f('userobm_login');
  $lname = $usr_q->f('userobm_lastname');
  $fname = $usr_q->f('userobm_firstname');

  $default_group_id = get_group_id_from_gid($domain_id, $gid);

  // Liste des groupes de l'utilisateur : on remplit le tableau $groups
  $gu_q = run_query_user_user_group($id);
  $groups = array();

  while ($gu_q->next_record()) {
    $g_id = $gu_q->f('userobmgroup_group_id');
    $grp_name = $gu_q->f('group_name');
    $grp_desc = $gu_q->F('group_desc');
    if ($g_id == $default_group_id) {
      $dis_remove = "<img src=\"$ico_delete\" style=\"visibility: hidden\"/>";
    } else {
      $dis_remove = "<a href=\"javascript: remove_element('sel_accept_group-data-group-$g_id','perms');\"><img src=\"$ico_delete\" alt=[Del]/></a>";
    }
    $dis_group .= "
    <tr id=\"sel_accept_group-data-group-$g_id\">
      <td>$dis_remove $grp_name</td>
      <td><input id=\"sel_accept_group_$g_id\" type=\"hidden\" name=\"data-group-$g_id\" value=\"1\" />$grp_desc</td>
    </tr>";
  }

  $url = "$GLOBALS[path]/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_group&amp;ext_container=table";
  $popup_group_add_button = "
    <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
    <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";

  $block_groups = "
  <tbody id='sel_group'>
    <tr>
      <th>$l_groups</th>
      <th>
        <input type=\"text\" id=\"groupSearch\" autocomplete=\"off\" />
        $popup_group_add_button
      </th>
    </tr>
  </tbody>
  <tbody id='sel_accept_group'>
    $dis_group
  </tbody>
  ";

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"" . url_prepare('user_index.php') . "\">
  <fieldset class=\"detail extra\">
  <legend>$l_user $login ($lname $fname) $l_group_member</legend>
    <table class=\"spreadSheet\">
      $block_groups
    </table>
  </fieldset>
 
  <fieldset class=\"buttons\">
  <input type=\"hidden\" name=\"user_id\" value=\"$id\" />
  <input type=\"hidden\" name=\"action\" value=\"group_update\" />
  <input type=\"submit\" value=\"$l_update_group\" />
  </fieldset>
  </form>";

  $js = "
    <script type =\"text/javascript\">

      var resultValue = function (element, extension) {
      var item_id = element.getProperty('id');
      var id = item_id.substr(('item_').length,item_id.length);
      var entity = this.selectedBox.id;
      var tr_id = entity +'-'+ id;
      var text = $(item_id+'_label').innerHTML;
      if (!$(tr_id)) {
        var result = new Element('tr');
        result.setProperties({'id': tr_id});
        result.injectInside(this.selectedBox);
        if (this.selectedBox.getChildren().length % 2 != 0) {
          result.addClass('pair');
        }
        new Element('td').adopt(
          new Element('a').setProperty('href', '#').adopt(
            new Element('img').setProperty('src', obm.vars.images.del)
          ).addEvent('mousedown',
            function() {
              var item = \$(item_id);
              if (item) { item.removeClass(\"selected\"); }
              remove_element(tr_id, entity);
            }.bind(this)
          )
        ).appendText(' ' + text).injectInside(result);

        new Element('td').adopt(
          new Element('input').setProperty('name', id)
                              .setProperty('value', 1)
                              .setProperty('type','hidden')
        ).appendText('').injectInside(result);
        
      } else {
        this.inputField.blur();
        this.resetFunc();
        this.inputField.focus();
      }
      eval(this.options.selectfunction);
    }

    function apply_backgroud() {
      var tbody_blocks = new Array($('sel_accept_group'));
      
      for (var i=0; i < tbody_blocks.length; i++) {
        var tbody = tbody_blocks[i];
        var tr_elements = (tbody ? tbody.getChildren() : new Array());
        for (var j=0; j < tr_elements.length; j++) {
          if ( (j % 2) == 0) {
            tr_elements[j].addClass('pair');
          }
        }
      }
    }

    apply_backgroud();

    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$GLOBALS[path]/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group', 'groupSearch',{resultValue:resultValue});
    })
    </script>
    ";
  $block .= $js;
  return $block;
}


/**
 * Display User Form
 *
 * @param mixed $action action called
 * @param mixed $user[] : default values
 * @param mixed $field [optionnal] error field
 * @return string XHTML user form
 */
function dis_user_form($action, $user, $field='') {
  global $display, $obm, $l_err_reference;

  $u_id = $user['user_id'];
  if ($u_id > 0) {
    $user_q = run_query_user_detail($u_id);
    if ($user_q->num_rows() == 1) {
      $display['detailInfo'] = display_record_info($user_q);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }

    // User's groups
    $groups = array();
    $g_q = run_query_user_user_group($u_id);
    while ($g_q->next_record()) {
      $g_id = $g_q->f('group_id');
      $gid = $g_q->f('group_gid');
      $gname = $g_q->f('group_name');
      $groups[$g_id] = array('gid'=>$gid, 'name'=>$gname);
    }
  }

  $block = html_user_form($action, $user_q, $user, $groups, $field);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Form
// Parameters:
//   - $usr_q : user database result
//   - $user  : default values or updated values (if error)
//   - $field : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function html_user_form($action, $usr_q, $user, $groups, $field='') {
  global $l_login, $l_password, $l_profile, $l_kind, $l_lastname, $l_firstname, $l_commonname;
  global $l_user, $l_title, $l_archive, $l_datebegin, $l_dateexp, $l_noexperie;
  global $l_insert, $l_update, $l_validate;
  global $l_update_valid, $l_change_password_confirm, $l_password_update;
  global $l_delegation_target, $l_hidden, $l_photo, $l_generate_passwd; 
  global $l_phone, $l_phone2, $l_mphone, $l_fax, $l_fax2, $l_desc;
  global $l_coord, $l_company, $l_direction, $l_service, $l_address;
  global $l_postcode, $l_town, $l_expresspostal;
  global $l_web, $l_web_mode, $l_web_all, $l_web_list_only;
  global $l_mail, $l_mail_ext, $l_email, $l_quota, $l_email_nomade, $l_local_copy;
  global $l_nomade, $l_perms, $l_access, $l_host, $l_ip, $l_empty;
  global $l_vacation, $l_vacation_message, $l_vacation_datebegin;
  global $l_vacation_dateend, $l_mail_server;
  global $l_enabled, $l_disabled, $l_web_consult_list;
  global $l_smb, $l_smb_perms, $l_smb_home, $l_smb_home_drive, $l_smb_logon;
  global $l_mailserver_auto;
  global $obm, $path, $display, $cgp_use;
  global $c_default_domain, $l_domain, $l_gid, $l_header_new, $l_hour;
  global $ccalendar_first_hour, $ccalendar_last_hour, $l_delete_userobmphoto;
  global $cuser_default_quota, $l_enable_auto_vacation;

  // Check if domain is a samba domain and flag it for further info
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  $noexperie = " checked=\"checked\" ";	
  // if update mode and first time value are taken from database
  if ($action == 'detailupdate') {

    // infos
    $id = $usr_q->f('userobm_id');
    $gid = $usr_q->f('userobm_gid');
    $domain_id = $usr_q->f('userobm_domain_id');
    $login = $usr_q->f('userobm_login');
    $old_login = $login;
    $passwd = '';
    $profile = $usr_q->f('userobm_perms');
    $lastaccess = $usr_q->f('lastaccess');
    $kind = $usr_q->f('userobm_kind');
    $lname = $usr_q->f('userobm_lastname');
    $fname = $usr_q->f('userobm_firstname');
    $cname = $usr_q->f('userobm_commonname');
    $title = $usr_q->f('userobm_title');
    $delegation_target = $usr_q->f('userobm_delegation_target');
    $delegation = $usr_q->f('userobm_delegation');
    $desc = $usr_q->f('userobm_description');
    $datebegin = of_date_upd_format($usr_q->f('datebegin'),true);
    $dateexp = of_date_upd_format($usr_q->f('dateexp'),true);
    if (isset($dateexp)){$noexperie = '';};
    $archive = ($usr_q->f('userobm_archive') == '1' ? ' checked' : '');
    $hidden = ($usr_q->f('userobm_hidden') == '1' ? ' checked' : '');
    $photo_id = $usr_q->f('userobm_photo_id');

    // Wait people
    $status = $usr_q->f('userobm_status');

    // coord
    $phone = $usr_q->f('userobm_phone');
    $phone2 = $usr_q->f('userobm_phone2');
    $mobile = $usr_q->f('userobm_mobile');
    $fax = $usr_q->f('userobm_fax');
    $fax2 = $usr_q->f('userobm_fax2');
    $company = $usr_q->f('userobm_company');
    $direction = $usr_q->f('userobm_direction');
    $service = $usr_q->f('userobm_service');
    $ad1 = $usr_q->f('userobm_address1');
    $ad2 = $usr_q->f('userobm_address2');
    $ad3 = $usr_q->f('userobm_address3');
    $zip = $usr_q->f('userobm_zipcode');
    $town = $usr_q->f('userobm_town');
    $cdx = $usr_q->f('userobm_expresspostal');
    $ctry_name = $usr_q->f('country_name');

    // web
    $h_name = $usr_q->f('host_name');
    $h_id = $usr_q->f('userobm_host_id');
    $web_perms = $usr_q->f('userobm_web_perms');
    $web_list = $usr_q->f('userobm_web_list');
    $web_all = $usr_q->f('userobm_web_all');

    // mail
    $mail_perms = $usr_q->f('userobm_mail_perms');
    $mail_ext_perms = $usr_q->f('userobm_mail_ext_perms');
    $mail_server_id = $usr_q->f('mailserver_id');
    $mail_server_host_name = $usr_q->f('mailserver_hostname');
    $email = $usr_q->f('userobm_email');
    $mail_quota = $usr_q->f('userobm_mail_quota');

    // nomad email
    $nomade_perms = $usr_q->f('userobm_nomade_perms');
    $nomade_enable = $usr_q->f('userobm_nomade_enable');
    $nomade_local_copy = $usr_q->f('userobm_nomade_local_copy');
    $email_nomade = $usr_q->f('userobm_email_nomade');

    // vacation
    $vacation_datebegin = new Of_Date($usr_q->f('userobm_vacation_datebegin'), 'GMT');
    $vacation_dateend = new Of_Date($usr_q->f('userobm_vacation_dateend'), 'GMT');
    if (!$vacation_datebegin->error()) {
      $auto_enable = true;
    } else {
      $vacation_datebegin->addHour(1)->setMinute(0)->setSecond(0);
      $vacation_dateend = clone $vacation_datebegin;
      $vacation_dateend->addWeek(1);
    }
    $vacation_enable = $usr_q->f('userobm_vacation_enable');
    $vacation_message = stripslashes($usr_q->f('userobm_vacation_message'));

    // samba
    $smb_perms = $usr_q->f('userobm_samba_perms');
    $smb_home = $usr_q->f('userobm_samba_home');
    $smb_home_drive = trim($usr_q->f('userobm_samba_home_drive'));
    $smb_logon = $usr_q->f('userobm_samba_logon_script');

    $dis_title = "$fname $lname";
  } elseif ($action == 'new') {
    $dom_infos = of_domain_get_domain_infos($obm['domain_id'], true);
    $ad1 = $dom_infos['address1'];
    $ad2 = $dom_infos['address2'];
    $ad3 = $dom_infos['address3'];
    $zip = $dom_infos['postcode'];
    $town = $dom_infos['town'];
    $domain_infos['mailshares_quota_default'];
    if ($samba_domain) {
      $smb = get_samba_info();
      $smb_home = $smb['samba_home_def'];
      $smb_home_drive = $smb['samba_home_drive_def'];
      $smb_perms = 1;
    }
    $mail_quota = $cuser_default_quota;
  }
  // If parameters have been given, they supercede the default action value
  // infos
  if (isset($user['user_id'])) { $id = $user['user_id']; }
  if (isset($user['gid'])) { $gid = $user['gid']; }
  if (isset($user['domain_id'])) { $domain_id = $user['domain_id']; }
  if (isset($user['login'])) { $login = strtolower($user['login']); }
  if (isset($user['old_login'])) { $old_login = $user['old_login']; }
  if (isset($user['passwd'])) { $passwd = $user['passwd']; }
  if (isset($user['profile'])) { $profile = $user['profile']; }
  if (isset($user['profiles'])) { $profiles = $user['profiles']; }
  if (isset($user['kind'])) { $kind = $user['kind']; }
  if (isset($user['lastname'])) { $lname = stripslashes($user['lastname']); }
  if (isset($user['firstname'])) { $fname = stripslashes($user['firstname']); }
  if (isset($user['commonname'])) { $cname = stripslashes($user['commonname']); }
  if (isset($user['title'])) { $title = stripslashes($user['title']); }
  if (isset($user['email'])) { $email = stripslashes($user['email']); }
  if (isset($user['datebegin'])) { $datebegin = of_date_upd_format($user['datebegin'],true); }
  if (isset($user['dateexp'])) { $dateexp = of_date_upd_format($user['dateexp'],true); $noexperie = '';}
  if (isset($user['delegation_target'])) { $delegation_target = stripslashes($user['delegation_target']); }
  if (isset($user['delegation'])) { $delegation = stripslashes($user['delegation']); }
  if (isset($user['desc'])) { $desc = stripslashes($user['desc']); }
  if (isset($user['archive'])) { $archive = ($user['archive'] == '1' ? 'checked' : ''); }
  if (isset($user['hidden'])) { $hidden = ($user['hidden'] == '1' ? 'checked' : ''); }
  if (isset($user['photo_id'])) { $photo_id = $user['photo_id']; }
  if (isset($user['userpattern_id'])) { $userpattern_id = $user['userpattern_id']; }
  
  // wait people
  if (isset($user['status']) && $action != 'detailupdate') { $status = $user['status']; }

  // coord
  if (isset($user['phone'])) { $phone = $user['phone']; }
  if (isset($user['phone2'])) { $phone2 = $user['phone2']; }
  if (isset($user['mobile'])) { $mobile = $user['mobile']; }
  if (isset($user['fax'])) { $fax = $user['fax']; }
  if (isset($user['fax2'])) { $fax2 = $user['fax2']; }
  if (isset($user['company'])) { $company = stripslashes($user['company']); }
  if (isset($user['direction'])) { $direction = stripslashes($user['direction']); }
  if (isset($user['service'])) { $service = $user['service']; }
  if (isset($user['ad1'])) { $ad1 = $user['ad1']; }
  if (isset($user['ad2'])) { $ad2 = $user['ad2']; }
  if (isset($user['ad3'])) { $ad3 = $user['ad3']; }
  if (isset($user['zip'])) { $zip = $user['zip']; }
  if (isset($user['town'])) { $town = $user['town']; }
  if (isset($user['cdx'])) { $cdx = $user['cdx']; }
  if (isset($user['ctry'])) { $ctry = $user['country']; }

  // web
  if (isset($user['host_name'])) { $h_name = $user['host_name']; }
  if (isset($user['host_id'])) { $h_id = $user['host_id']; }
  if (isset($user['web_perms'])) { $web_perms = $user['web_perms']; }
  if (isset($user['web_list'])) { $web_list = $user['web_list']; }
  if (isset($user['web_all'])) { $web_all = $user['web_all']; }

  // mail
  if (isset($user['mail_perms'])) { $mail_perms = $user['mail_perms']; }
  if (isset($user['mail_ext_perms'])) { $mail_ext_perms = $user['mail_ext_perms']; }
  if (isset($user['mail_server_id'])) { $mail_server_id = $user['mail_server_id']; }
  if (isset($user['mail_server_hostname'])) { $mail_server_host_name = $user['mail_server_hostname']; }
  if (isset($user['mail_quota'])) { $mail_quota = $user['mail_quota']; }

  // nomad email
  if (isset($user['nomade_perms'])) { $nomade_perms = $user['nomade_perms']; }
  if (isset($user['nomade_enable'])) { $nomade_enable = $user['nomade_enable']; }
  if (isset($user['nomade_local_copy'])) { $nomade_local_copy = $user['nomade_local_copy']; }
  if (isset($user['email_nomade'])) { $email_nomade = $user['email_nomade']; }

  // vacation
  if (isset($user['vacation_enable'])) { $vacation_enable = $user['vacation_enable']; }
  if (isset($user['vacation_datebegin'])) { $vacation_datebegin = $user['vacation_datebegin']; }
  if (isset($user['vacation_dateend'])) { $vacation_dateend = $user['vacation_dateend']; }
  if (isset($user['vacation_message'])) { $vacation_message = stripslashes( $user['vacation_message'] ); }
  // samba
  if (isset($user['smb_perms'])) { $smb_perms = $user['smb_perms']; }
  if (isset($user['smb_home'])) { $smb_home = stripslashes($user['smb_home']); }
  if (isset($user['smb_home_drive'])) { $smb_home_drive = $user['smb_home_drive']; }
  if (isset($user['smb_logon'])) { $smb_logon = $user['smb_logon']; }
  if (isset($vacation["auto_enable"])) {$auto_enable = $vacation["auto_enable"]; }

  $block_userdata = of_userdata_dis_entity_form('user', $id, $user);

  if ($auto_enable) {
    $schedule_enable = "checked=\"checked\"";
  } else {
    $schedule_disabled = "disabled=\"disabled\"";
    $display_auto = "style=\"display:none\"";
  }

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }
  $customFields = new OBM_Form_Field('user');
  if ($id) {
    $block_userfields = $customFields->buildEntityForm($class, $id, $user);
  } else {
    $block_userfields = $customFields->buildEntityForm($class, null, $user);
  }

  // Default exp is hidden
  if (!isset($dateexp))	{
    $showexp='display:none';
    $disabled='disabled="disabled"';
  }

  // login (can't be updated, so only on insert)
  if (($action == 'new') || ($action == 'insert')) {
    $dis_login = "<input type=\"text\" name=\"tf_login\" size=\"24\"  id=\"userLogin\" autocomplete=\"off\" value=\"$login\" />";
  } else {
    $dis_login = "<input type=\"text\" name=\"tf_login\" size=\"24\"  id=\"userLogin\" autocomplete=\"off\" value=\"$login\" />
                  <input type=\"hidden\" name=\"tf_old_login\" value=\"$old_login\" />";
  }

  // Domain (can't be updated, so only on insert)
  if (($action == 'new' || $action == 'insert')
      && $obm['domain_global']) {
    $sel_domain = of_domain_dis_select($domain_id);
    $dis_domain = "<tr>
      <th>$l_domain</th>
      <td>$sel_domain</td>
  </tr>";
  }


  // User GID automatically set on insert (so display only on update)
  if (($action == 'detailupdate') || ($action == 'update')) {

    // Default group select
    $sel_gid = "<select name=\"sel_gid\" id=\"sel_gid\">";
    if (is_array($groups)) {
      foreach($groups as $g_id => $one_group) {
	$g_gid = $one_group['gid'];
	$sel_gid .= "\n<option value=\"$g_gid\"";
	if ($g_gid == $gid) { $sel_gid .= " selected=\"selected\""; }
	$sel_gid .= ">".$one_group['name']."</option>";
      }
    }
    $sel_gid .= "</select>";

    $dis_gid = "
  <tr>
    <th class=\"$class[gid]\">$l_gid</th>
    <td>$sel_gid
    </td>
  </tr>";
  }

  if(($action == 'new' || $mail_quota === NULL) && $cgp_use['service']['mail']) {
    $dis_sel_profile = "
      <td><select id=\"sel_profile\" name=\"sel_profile\" onchange='showDelegation(this);get_profile_quota(this);'>
      ";
  } else {
    $dis_sel_profile = "
      <td><select id=\"sel_profile\" name=\"sel_profile\" onchange='showDelegation(this)'>
      ";
  }

  // Section Web
  if ($cgp_use['service']['web']) {
    // Web perms checkbox checked value
    $web_state  = ($web_perms ? 'checked' : '');

    // Web mode Radio checked value
    if ($web_all == '0') $web_all_c0 = ' checked';
    else $web_all_c1 = ' checked';
    
    $web_section = "
  <fieldset class=\"detail infos\">
  <legend>$l_access</legend>

  <table>
  <tr>
    <th class=\"$class[web_perms]\"><label for=\"cb_web_perms\">$l_web</label></th>
    <td><input id=\"cb_web_perms\" name=\"cb_web_perms\" type=\"checkbox\" value=\"1\" $web_state /></td>
  </tr>
  $dis_access
  </table>
  </fieldset>";
  }

  if ($cgp_use['service']['mail']) {
    $dis_mail = dis_user_mail_field($domain_id, $email, $class);

    // Mail perms checkbox checked values
    $mail_state  = ($mail_perms ? 'checked' : '');

    if($action == 'new') {
      $mail_quota_default = "
        <td><input name=\"cb_mail_perms\" type=\"checkbox\" id=\"userMailActive\" onclick=\"switch_mail_mode();get_profile_quota($('sel_profile'));\" value=\"1\" $mail_state /></td>
        ";
    } else {
      $mail_quota_default = "
        <td><input name=\"cb_mail_perms\" type=\"checkbox\" id=\"userMailActive\" onclick=\"switch_mail_mode();\" value=\"1\" $mail_state /></td>
        ";
    }

    $mail_ext_state  = ($mail_ext_perms ? 'checked' : '');
    // Mail server can't be changed if set
    if (! $mail_server_host_name) {
      $dis_mailserver = of_domain_dis_mailserver_select('imap', $mail_server_id, $obm['domain_id']);
      
      // BEGIN - automatic mail server chooser hooks
      
      if (has_mail_server_auto()) {
        $dis_mailserver = preg_replace('/(<select [^>]+>)/',
          "\\1 <option value=\"auto\">$l_mailserver_auto</option>", $dis_mailserver);
      }
      
      // END - automatic mail server chooser hooks
    
    } else {
      $dis_mailserver = "
      <a href=\"$path/host/host_index.php?action=detailconsult&host_id=$mail_server_id\">$mail_server_host_name</a>
      <input type=\"hidden\" name=\"sel_mail_server_id\" value=\"$mail_server_id\" />
      <input type=\"hidden\" name=\"sel_mail_server_hostname\" value=\"$mail_server_host_name\" />";
    }
    if ($mail_state) {
      $externalKlass = 'H';
      $externalEnabled = 'disabled="disabled"';
    } else {
      $internalKlass = 'H';
    }
    $main_domain = $obm['domain_name'];
    $mail_section = "
  <fieldset class=\"detail infos\" id=\"userMail\">
  <legend>$l_mail</legend>
  <table>
    <tr>
      <th class=\"$class[mail_perms]\"><label for=\"userMailActive\">$l_mail</label></th>
      $mail_quota_default
    </tr>          
  </table>
  <table id=\"userInternalMail\" class='$internalKlass'>
    <tr id=\"user_mail\">
      <th class=\"$class[mail_server]\">$l_mail_server</th>
      <td>$dis_mailserver</td>
    </tr>
    <tr>
    $dis_mail
    </tr>        
    <tr id=\"user_mail\">
    <th class=\"$class[mail_quota]\">$l_quota</th>
    <td><input id=\"tf_mail_quota\" name=\"tf_mail_quota\" maxlength=\"6\" size=\"6\" value=\"$mail_quota\" /></td>
    </tr>      
  </table>
  <table id=\"userExternalMail\" class='$externalKlass'>
    <tr>
      <th class=\"$class[email]\">$l_email</th>
      <td><input type=\"text\" id=\"externalEmailField\" name=\"ta_email\" $externalEnabled value=\"".((!$mail_state)?$email:'')."\" /></td>
    </tr>  
  </table>
  <script type='text/javascript'>
    obm.vars.consts.domaindefaultquota = '$dom_infos[mailshares_quota_default]';
    var internal = $('userInternalMail');
    var external = $('userExternalMail');
    if($('userMailActive').checked == true) {
      external.dispose();
    } else {
      internal.dispose();  
    }
  </script>
  </fieldset>";
  } else {
  $mail_section = "
    <fieldset class=\"detail infos\"  id=\"user_external_mail\">
    <legend>$l_mail</legend>
    <table>    
    <tr>
      <th class=\"$class[email]\">$l_email</th>
      <td><input type=\"text\" id=\"externalEmailField\" name=\"ta_email\" value=\"$email\" /></td>
    </tr>
    </table>
    </fieldset>";
  }

  if ($cgp_use['service']['mail_nomad']) {
  // Nomad chekbox checked value
    if ($nomade_perms) $nomade_perms_c = ' checked';
    if ($nomade_enable) $nomade_enable_c = ' checked';
    if ($nomade_local_copy) $nomade_local_copy_c = ' checked';

    $nomade_mail_block = dis_user_nomade_mail_field($email_nomade,$class);

    $nomad_section = "
  <fieldset class=\"detail infos\">
  <legend>$l_nomade</legend>

  <table>
  $nomade_mail_block
  <tr>
    <th class=\"$class[nomade_perms]\"><label for=\"cb_nomade_perms\">$l_perms</label></th>
    <td><input id=\"cb_nomade_perms\" name=\"cb_nomade_perms\" type=\"checkbox\" value=\"1\" $nomade_perms_c /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_enable]\"><label for=\"cb_nomade_enable\">$l_enabled</label></th>
    <td><input id=\"cb_nomade_enable\" name=\"cb_nomade_enable\" type=\"checkbox\" value=\"1\" $nomade_enable_c /></td>
  </tr>
  <tr>
    <th class=\"$class[nomade_local_copy]\"><label for=\"cb_nomade_local_copy\">$l_local_copy</label></th>
    <td><input id=\"cb_nomade_local_copy\" name=\"cb_nomade_local_copy\" type=\"checkbox\" value=\"1\" $nomade_local_copy_c /></td>
  </tr>
  </table>
  </fieldset>";
  }

  if ($cgp_use['service']['mail_vacation']) {

    // hour begin
    $dis_hour_b = "<select $schedule_disabled name=\"sel_time_begin\" style=\"width:4em;\">";
    for ($i=$ccalendar_first_hour; $i<$ccalendar_last_hour; $i++) {
      $current_hour = substr("0$i",-2,2); 
      if ($vacation_datebegin!=null && $current_hour == $vacation_datebegin->getHour()) {
        $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
        $vacation_begin_date_picker = $vacation_datebegin->getInputDate();
      } else {
        $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
      }
    }
    $dis_hour_b .= "</select>";  
    $dis_min_b = "<select $schedule_disabled name=\"sel_min_begin\" style=\"width:4em;\">";
    if ($ccalendar_hour_fraction <= 60 && $ccalendar_hour_fraction > 0) {
      $hour_fraction = $ccalendar_hour_fraction;
    } else {
      $hour_fraction = 4;
    }
    $step = 60 / $hour_fraction;
    for ($i=$hour_fraction; $i>=1; $i--) {
      $current_min = substr('0'.round((60 - ($step*$i))),-2);
      if ($vacation_datebegin!=null && $current_min  == $vacation_datebegin->getMinute()) {
        $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
      } else {
        $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
      }
    }
    $dis_min_b .= '</select>';

    // hour end
    $dis_hour_e = "<select $schedule_disabled name=\"sel_time_end\" style=\"width:4em;\">";
    for ($i=$ccalendar_first_hour; $i<=$ccalendar_last_hour; $i++) {
      $current_hour = substr("0$i",-2,2); 
      if ($vacation_dateend!=null && $current_hour == $vacation_dateend->getHour()) {
        $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
        $vacation_end_date_picker = $vacation_dateend->getInputDate();
      } else {
        $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
      }
    }
    $dis_hour_e .= '</select>';  
    $dis_min_e = "<select $schedule_disabled name=\"sel_min_end\" style=\"width:4em;\">";
    for ($i=$hour_fraction; $i>=1; $i--) {
      $current_min = substr('0'.round((60 - ($step*$i))),-2);
      if ($vacation_dateend!=null && $current_min  == $vacation_dateend->getMinute()) {
        $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
      } else {
        $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
      }
    }
    $dis_min_e .= '</select>';

    // Vacation checkbox checked value
    $vacation_state = ($vacation_enable ? 'checked' : '');
    $vacation_section = "
  <fieldset class=\"detail infos\">
  <legend>$l_vacation</legend>
  <script type=\"text/javascript\">
    function showHideVacationDate(elt) {
      if(elt.checked) {
        $('vacation_datebegin').getElements('input').setProperty('disabled',false);
        $('vacation_datebegin').getElements('select').setProperty('disabled',false);
        $('vacation_dateend').getElements('input').setProperty('disabled',false);
        $('vacation_dateend').getElements('select').setProperty('disabled',false);        
        $('vacation_datebegin').setStyle('display','');
        $('vacation_dateend').setStyle('display','');
      } else {
        $('vacation_datebegin').setStyle('display','none');
        $('vacation_dateend').setStyle('display','none');
        $('vacation_datebegin').getElements('input').setProperty('disabled',true);
        $('vacation_datebegin').getElements('select').setProperty('disabled',true);
        $('vacation_dateend').getElements('input').setProperty('disabled',true);
        $('vacation_dateend').getElements('select').setProperty('disabled',true);        
      }
    }
  </script>
  <table>
  <tr>
    <th class=\"$class[vacation_enable]\"><label for=\"cb_vacation_enable\">$l_enabled</label></th>
    <td><input id=\"cb_vacation_enable\" name=\"cb_vacation_enable\" type=\"checkbox\" value=\"1\" $vacation_state /></td>
  </tr>
  <tr >
    <th><label for=\"cba_auto_enable\">$l_enable_auto_vacation</th>
    <td><input $schedule_enable type=\"checkbox\" id=\"cba_auto_enable\" name=\"cba_auto_enable\" $auto_enable value=\"1\" 
    onchange=\"showHideVacationDate(this);\"
  </tr>  
  <tr $display_auto id=\"vacation_datebegin\">
    <th class=\"$class[vacation_datebegin]\">$l_vacation_datebegin</th>
    <td>
      <input $schedule_disabled type=\"text\" name=\"tf_vacation_datebegin\" value=\"$vacation_begin_date_picker\" class=\"datePicker\"/>
      <span id=\"hour_begin\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_b : $dis_min_b</span>
    </td>
  </tr>
  <tr $display_auto id=\"vacation_dateend\">
    <th class=\"$class[vacation_dateend]\">$l_vacation_dateend</th>
    <td>
      <input $schedule_disabled type=\"text\" name=\"tf_vacation_dateend\" value=\"$vacation_end_date_picker\" class=\"datePicker\"/>
      <span id=\"hour_end\" style=\"$style\" class=\"NW\">$l_hour : $dis_hour_e : $dis_min_e</span>
    </td>
  </tr>
  <tr>
    <th class=\"$class[vacation_message]\">$l_vacation_message</th>
    <td><textarea name=\"ta_vacation_message\" cols=\"32\" rows=\"3\">$vacation_message</textarea></td>
  </tr>
  </table>
  </fieldset>";
  }

  if (($cgp_use['service']['samba']) && ($samba_domain)) {
    $samba = run_query_user_samba_properties();
    $smb_home_drive = (isset($smb_home_drive))?$smb_home_drive:$samba['drive'];
    if (isset($smb_home_drive) && $smb_home_drive != '') $drive[$smb_home_drive] = 'selected="selected"';
    for($i='D'; $i < 'Z';$i++) {
      $dis_drive_sel .= "<option ".$drive["$i"]." value='$i'>$i:</option>";
    }
    $dis_drive_sel .= "<option ".$drive['Z']." value='Z'>Z:</option>";
    $dis_drive_sel = "<select style='width:4em;' name='sel_smb_home_drive'>$dis_drive_sel</select>";
    $smb_home = (isset($smb_home))?$smb_home:$samba['home'];
    if ($login != '') {
      $smb_hidden_home = str_replace($login,'%u', $smb_home);
      $smb_home = str_replace('%u', $login, $smb_home);
    } else {
      $smb_hidden_home = $smb_home;
    }
    $smb_state  = ($smb_perms ? 'checked' : '');
    if (!$smb_state) {
      $sambaKlass = 'H';
    }
    $samba_section = "
      <fieldset class=\"detail infos\" id='userSamba'>
      <legend>$l_smb</legend>

      <table>
      <tr>
        <th class=\"$class[smb_perms]\"><label for=\"userSambaActive\">$l_smb_perms</label></th>
        <td><input name=\"cb_smb_perms\" type=\"checkbox\" id=\"userSambaActive\" onclick=\"switch_samba_mode();\" value=\"1\" $smb_state /></td>
      </tr>
      </table>
      <table id=\"userSambaProperties\" class='$sambaKlass'>
      <tr>
        <th class=\"$class[smb_home]\">$l_smb_home</th>
        <td>
        <input name=\"tf_smb_home\" maxlength=\"255\" size=\"64\" id='userSambaHome'value=\"$smb_home\" />
        <input type=\"hidden\" id='userSambaHiddenHome'value=\"$smb_hidden_home\" />
        </td>
      </tr>
      <tr>
        <th class=\"$class[smb_home_drive]\">$l_smb_home_drive</th>
        <td>$dis_drive_sel</td>
      </tr>
      <tr>
        <th class=\"$class[smb_logon]\">$l_smb_logon</th>
        <td><input name=\"tf_smb_logon\" maxlength=\"128\" size=\"64\" value=\"$smb_logon\" /></td>
      </tr>
      </table>
      <script type='text/javascript'>
        var samba = $('userSambaProperties');
        $('userSambaHome').addEvent('focus', function () { $('userSambaHome').value = $('userSambaHiddenHome').value});
        $('userSambaHome').addEvent('blur', function () { $('userSambaHiddenHome').value = $('userSambaHome').value; sambaHomeCompute()});
        $('userSambaHome').addEvent((Browser.Engine.trident || Browser.Engine.webkit) ? 'keydown' : 'keypress', function (e) {
          if(e.key == 'enter') {
            sambaHomeCompute();
          }
        });
        $('userLogin').addEvent('keyup', sambaHomeCompute);
      </script>      
      </fieldset>";
  }

  // Input file (photo)
  $block_photo = display_document_form($user, $l_photo);

  // Buttons
  if (($action == 'new') || ($action == 'insert')) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($action=='detailupdate') || ($action=='update') || ($action=='valid')) {
    $dis_button = "<input type=\"hidden\" id=\"user_action\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"user_id\" value=\"$id\" />";
    if ($status == 'INIT') {
      $dis_button .= "<input type=\"button\" onclick=\"obm.popup.show('idPopupValidUser')\" value=\"$l_update\" />";
    } else {
      $dis_button .= "<input type=\"submit\" value=\"$l_update\" />";
    }
  }

  // Popup password Generate
  $pop_generate = "
    <div id=\"popupModifPassword\" class=\"obmPopup\">
      <h1>
        <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
          <img src=\"$GLOBALS[ico_popup_close]\" alt=\"close\" />
        </a>
        <span>$l_password_update</span>
        </h1>
        <br />
        $l_change_password_confirm
      <fieldset class=\"buttons\">
        <input type=\"button\" onclick=\"generate_passwd()\" id=\"popup_confirm\" class=\"closeSticky\" value=\"$GLOBALS[l_confirm]\" />
        <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$GLOBALS[l_cancel]\" />
      </fieldset>
    </div>";

  // Status User
  if ($status == 'INIT'){
    $dis_status = "
        <input type=\"hidden\" name=\"ta_status\" value=\"$status\" />";
    $pop_up = "
      <div id=\"idPopupValidUser\" class=\"obmPopup\">
        <h1>
          <a href=\"#\" id=\"popup_close\" class=\"closeSticky\">
            <img src=\"$GLOBALS[ico_popup_close]\" alt=\"close\" />
          </a>
          <span>$l_update_valid</span>
        </h1>
        <input name=\"cba_modif\" type=\"radio\" onclick=\"$('user_action').value='valid'\"/><span>$l_update_valid</span>
        <input name=\"cba_modif\" type=\"radio\" checked=\"checked\" onclick=\"$('user_action').value='update'\"/><span>$l_update</span>
        <fieldset class=\"buttons\">
          <input type=\"button\" onclick=\"$('dataUser').submit()\" id=\"popup_manage\" class=\"closeSticky\" value=\"$l_validate\" />
          <input type=\"button\" id=\"popup_cancel\" class=\"closeSticky\" value=\"$GLOBALS[l_cancel]\" />
        </fieldset>
      </div>";
    $dis_archive = "
      <tr>
        <td><input name=\"cba_archive\" type=\"hidden\" value=\"1\" $archive /></td>
      </tr>";
  } else {
    $dis_archive = "
      <tr>
        <th class=\"$class[archive]\"><label for=\"cba_archive\">$l_archive</label></th>
        <td><input id=\"cba_archive\" name=\"cba_archive\" type=\"checkbox\" value=\"1\" $archive /></td>
      </tr>";
  }

  // Photo
  if ($photo_id > 0) {
    $block_photo_detail = "
    <tr id=\"block_photo_detail\">
      <th>&nbsp;</th>
      <td>
        <img src=\"$path/document/document_index.php?action=accessfile&document_id=$photo_id\" alt=\"\"/ />
        <input type=\"hidden\" name=\"photo_id\" value=\"$photo_id\" />
        <a href=\"#\" onclick=\"delete_user_photo();\" >$l_delete_userobmphoto</a>
      </td>
    </tr>";
  } else {
    $block_photo_detail = "";
  }

  // Profiles
  $delegation_modules = array(
    'user' => $GLOBALS['cright_write_admin'],
    'resource' => $GLOBALS['cright_write'], 
    'host' => $GLOBALS['cright_write'],
    'people' => $GLOBALS['cright_write'],
    'group' => $GLOBALS['cright_write_admin'],
    'mailshare' => $GLOBALS['cright_write']);
  $level = $profiles[$obm['profile']]['properties']['level'];
  $managepeers = $profiles[$obm['profile']]['properties']['level_managepeers'];
  foreach ($user['profiles'] as $k_profile => $one_profile) {
    $selected = '';
    foreach($delegation_modules as $mdl => $rgt) {
      $section = $GLOBALS['cgp_show']['module'][$mdl];
      if (Perm::check_section_right($section, $k_profile) && Perm::check_module_right($mdl, $rgt, $k_profile)) {
        $onselect .= "profiles['$k_profile'] = true\n";
        if (!isset($delegation_class) && $profile == $k_profile){
          $delegation_class = '';
          $selected = ' selected';
        } 
        break;
      }
    } 
    if (!isset($delegation_class) && $profile == $k_profile){
      $delegation_class = 'H';
      $selected = ' selected';
    }
    if (isset($profiles[$k_profile]['properties']['level'])) {
      if (($profiles[$k_profile]['properties']['level'] > $level)
    	|| (($profiles[$k_profile]['properties']['level'] == $level) && ($managepeers))) {
        $dis_profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
        $block_profile .= "<option value=\"$k_profile\"$selected>$dis_profile</option>";
      }
    } else {
      $dis_profile = isset($GLOBALS["l_perm_$k_profile"]) ? $GLOBALS["l_perm_$k_profile"] : $k_profile;
      $block_profile .= "<option value=\"$k_profile\"$selected>$dis_profile</option>";
    }
  }
  if ($cgp_use['property']['delegation']) {
    if (!isset($delegation_target)) $delegation_target = $obm['delegation_target'];
    $dis_delegation_target = "
    <tr class='$delegation_class' id='delegationTargetBlock'>
      <th class=\"$class[delegation_target]\">$l_delegation_target</th>
      <td>
      <input id='delegationTargetField' name=\"tf_delegation_target\" size=\"32\" maxlength=\"256\" value=\"$delegation_target\" />
      <script type='text/javascript'>
        $('delegationField').addEvent('keyup' , function () {
          $('delegationTargetField').value = $('delegationField').value;
        })
        $('delegationField').addEvent('change' , function () {
          $('delegationTargetField').value = $('delegationField').value;
        })          
      </script>
      </td>
    </tr>";

    $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);
  }

  // Mobile devices
  $mobile_devices = html_user_mobile_devices($id, 'form', $user);

  // some constants
  $csize_add = '48';
  $cmax_add = '64';
  $csize_phone = '24';

  $display['title'] = $dis_title;

  // User pattern selector
  if (($action == 'new') || ($action == 'insert')) {
    $patterns = UserPattern::getDomainPatternsAndGlobalPatterns($GLOBALS['obm']['domain_id']);
  }
  if (!empty($patterns)) {
    $choices = "<option value=\"\">---</option>";
    foreach ($patterns as $id => $pattern) {
      $selected = '';
      if ($id == $user['userpattern_id']) { $selected = 'selected'; }
      $choices .= "<option value=\"$id\" $selected>$pattern</option>";
    }
    $block_userpattern = "<div id=\"templateSelect\">
              
        {$GLOBALS['l_create_using_userpattern']}
        <select id=\"userpattern_id\" name=\"userpattern_id\">
          $choices
        </select>
         </div>
    ";
    if (is_array($GLOBALS['cgp_user']['user']['field'])) {
      foreach ($GLOBALS['cgp_user']['user']['field'] as $fieldname => $properties) {
        if ($properties['type']=='text' || $properties['type']=='textarea') {
          $extrafields_userpattern_js .= "theUserForm.addField('$fieldname','tf_custom_$fieldname');\n";
        } elseif ($properties['type']=='boolean') {
          $extrafields_userpattern_js .= "theUserForm.addField('$fieldname','tf_custom_$fieldname','BooleanField');\n";
        }
      }
    }
    if (is_array($GLOBALS['cgp_user']['user']['category'])) {
      foreach ($GLOBALS['cgp_user']['user']['category'] as $fieldname => $properties) {
        $extrafields_userpattern_js .= "theUserForm.addField('$fieldname','sel_$fieldname','ChoiceField');\n";
      }
    }
    $js_userpattern = "<script type='text/javascript'>
    obm.initialize.chain(function () {
      Obm.vars.consts.userPatternUrl = '$path/userpattern/userpattern_index.php';
      theUserForm = new Obm.UserPattern.Form({ url: Obm.vars.consts.userPatternUrl, delay: 400 });
      $extrafields_userpattern_js
      $('userpattern_id').addEvent('change' , function () {
        theUserForm.setPattern(this.value);
      });
    })
    </script>
    ";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
    $pop_up
    $pop_generate
  <form id=\"dataUser\" method=\"post\" name=\"f_entity\"
    enctype=\"multipart/form-data\"
    onsubmit=\"return (check_phoneFax_number(this) && check_user(this));\"
    action=\"" . url_prepare('user_index.php') . "\">
   $block_userpattern
  <fieldset class=\"detail infos\">
  <legend>$l_user</legend>
  <script type='text/javascript'>
    var profiles = new Object();
    $onselect
    function showDelegation(field) {
      var value = field.options[field.selectedIndex].value;
      if(profiles[value]) {
        $('delegationTargetBlock').removeClass('H');
      } else {
        $('delegationTargetBlock').addClass('H');
      }
    }
    function showExpiration()	{
      var Props = $('noexperie').getProperty('checked');			
      if(Props==false){
        $('exp').setStyle('display','');
        $('exp').getElement('input').setProperty('disabled',false);
      }else{
        $('exp').setStyle('display','none');
        $('exp').getElement('input').setProperty('disabled',true);
      }	
    }
  </script>
  <table>
  <tr>
    <th class=\"$class[kind]\">$l_kind</th>
    <td><input id=\"userKind\" name=\"tf_kind\" size=\"32\" maxlength=\"64\" value=\"$kind\" autocomplete =\"off\"/>
    </td>
  </tr>
  <tr>
    <th class=\"$class[lastname]\">$l_lastname</th>
    <td><input id=\"userLastname\" name=\"tf_lastname\" size=\"32\" maxlength=\"64\" autocomplete=\"off\" value=\"$lname\" /></td>
  </tr>
  <tr>
    <th class=\"$class[firstname]\">$l_firstname</th>
    <td><input id=\"userFirstname\" name=\"tf_firstname\" size=\"32\" maxlength=\"64\" autocomplete=\"off\" value=\"$fname\" /></td>
  </tr>
   <tr>
    <th class=\"$class[commonname]\">$l_commonname</th>
    <td><input id=\"userCommonname\" name=\"tf_commonname\" size=\"32\" maxlength=\"256\" autocomplete=\"off\" value=\"$cname\" /></td>
  </tr> 
  <tr>
    <th class=\"$class[login]\">$l_login</th>
    <td>$dis_login</td>
  </tr>
  <tr>
    <th class=\"$class[password]\">$l_password</th>
    <td id=\"td_passwd\"><input id=\"passwd\" type=\"password\" name=\"tf_passwd\" autocomplete=\"off\" size=\"24\" maxlength=\"32\" value=\"$passwd\" />
        <button type=\"button\" onclick=\"modif_password()\">$l_generate_passwd</button>
    </td>
  </tr>
  $dis_status
  $dis_domain
  <tr>
    <th class=\"$class[hidden]\"><label for=\"cba_hidden\">$l_hidden</label></th>
    <td><input id=\"cba_hidden\" name=\"cba_hidden\" type=\"checkbox\" value=\"1\" $hidden /></td>
  </tr>
  $dis_archive
  $dis_gid
  <tr>
    <th class=\"$class[profile]\">$l_profile</th>
      $dis_sel_profile 
      $block_profile
      </select>
    </td>
  </tr>
  $block_delegation
  $dis_delegation_target
  <tr>
    <th class=\"$class[title]\">$l_title</th>
    <td><input id=\"userTitle\" name=\"tf_title\" size=\"32\" maxlength=\"256\" value=\"$title\" /></td>
  </tr>
  <tr>
    <th class=\"$class[datebegin]\">$l_datebegin</th>
    <td><input id=\"userDatebegin\" type=\"text\" name=\"tf_datebegin\" value=\"$datebegin\" class=\"datePicker\"/></td>
  </tr>
  <tr>
    <th class=\"$class[noexperie]\"><label for=\"noexperie\">$l_noexperie</label></th>
    <td><input id=\"noexperie\" name=\"cba_noexperie\" type=\"checkbox\" value=\"1\" $noexperie onclick='showExpiration();'/></td>
  </tr>
  <tr id=\"exp\" style=\"$showexp\">
    <th class=\"$class[dateexp]\">$l_dateexp</th>
    <td><input id=\"userDateexp\" type=\"text\" name=\"tf_dateexp\" $disabled value=\"$dateexp\" class=\"datePicker\"/></td>
  </tr>
  $block_photo
  $block_photo_detail
  $block_userdata
  $block_userfields
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_coord</legend>
  <table>
  <tr>
    <th class=\"$class[phone]\">$l_phone</th>
    <td><input id=\"userPhone\" name=\"tf_phone\" size=\"20\" maxlength=\"20\" value=\"$phone\" /></td>
  </tr>
  <tr>
    <th class=\"$class[phone2]\">$l_phone2</th>
    <td><input id=\"userPhone2\" name=\"tf_phone2\" size=\"20\" maxlength=\"20\" value=\"$phone2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[mobile]\">$l_mphone</th>
    <td><input id=\"userMobile\" name=\"tf_mobile\" size=\"20\" maxlength=\"20\" value=\"$mobile\" /></td>
  </tr>
  <tr>
    <th class=\"$class[fax]\">$l_fax</th>
    <td><input id=\"userFax\" name=\"tf_fax\" size=\"20\" maxlength=\"20\" value=\"$fax\" /></td>
  </tr>
  <tr>
    <th class=\"$class[fax2]\">$l_fax2</th>
    <td><input id=\"userFax2\" name=\"tf_fax2\" size=\"20\" maxlength=\"20\" value=\"$fax2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[company]\">$l_company</th>
    <td><input id=\"userCompany\" name=\"tf_company\" size=\"64\" maxlength=\"64\" value=\"$company\" /></td>
  </tr>
  <tr>
    <th class=\"$class[direction]\">$l_direction</th>
    <td><input id=\"userDirection\" name=\"tf_direction\" size=\"64\" maxlength=\"64\" value=\"$direction\" /></td>
  </tr>
  <tr>
    <th class=\"$class[service]\">$l_service</th>
    <td><input id=\"userService\" name=\"tf_service\" size=\"64\" maxlength=\"64\" value=\"$service\" /></td>
  </tr>
  <tr>
    <th class=\"$class[address1]\">$l_address 1</th>
    <td><input id=\"userAd1\" name=\"tf_ad1\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad1\" /></td>
  </tr>
  <tr>
    <th class=\"$class[address2]\">$l_address 2</th>
    <td><input id=\"userAd2\" name=\"tf_ad2\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad2\" /></td>
  </tr>
  <tr>
    <th class=\"$class[address3]\">$l_address 3</th>
    <td><input id=\"userAd3\" name=\"tf_ad3\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad3\" /></td>
  </tr>
  <tr>
    <th class=\"$class[zip]\">$l_postcode</th>
    <td><input id=\"userZip\" name=\"tf_zip\" size=\"8\" maxlength=\"8\" value=\"$zip\" /></td>
  </tr>
  <tr>
    <th class=\"$class[town]\">$l_town</th>
    <td><input id=\"userTown\" name=\"tf_town\" size=\"24\" maxlength=\"24\" value=\"$town\" /></td>
  </tr>
  <tr>
    <th class=\"$class[expresspostal]\">$l_expresspostal</th>
    <td><input id=\"userCdx\" name=\"tf_cdx\" size=\"16\" maxlength=\"16\" value=\"$cdx\" /></td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_desc</legend>
  <table>
  <tr>
    <th class=\"$class[desc]\">$l_desc</th>
    <td><input id=\"userDesc\" name=\"tf_desc\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
  </tr>
  </table>
  </fieldset>
$web_section
$mail_section
$nomad_section
$vacation_section
$samba_section
$mobile_devices
  <fieldset class=\"buttons\">
  $dis_button
  </fieldset>
  </form>
  <script type='text/javascript'>
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$GLOBALS[path]/contact/contact_index.php?action=ext_get_kind', 'userKind', 'userKind',{mode: 'mono', strict:false, fieldText: '', chars: 0});
      })
  </script>
  $js_userpattern";

  return $block;
}


function create_hidden_input($name, $value) {
  $patterns = array('"','<','>');
  $replacements = array("&quot;","&lt;","&gt;");
  return "<input type=\"hidden\" name=\""
        .str_replace($patterns,$replacements,$name)
        ."\" value=\""
        .str_replace($patterns,$replacements,$value)
        ."\" />\n";
}

///////////////////////////////////////////////////////////////////////////////
// Display the context about a user insertion or update
// When similar users exists we show these and ask confirmation
// Parameters:
//   - $p_id     : user id
//   - $usr_q    : user database result (at least 1 row)
//   - $user[]   : default values
//     keys used : login, passwd, perms, lastname, firstname, archive, email
///////////////////////////////////////////////////////////////////////////////
function dis_user_warn_insert($p_id, $usr_q, $user) {
  global $l_check_sameuser, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  $display['msg'] .= display_warn_msg($l_check_sameuser);
  while ($usr_q->next_record()) {
    $id = $usr_q->f('userobm_id');
    $samename = $usr_q->f('userobm_login');
    $dis_same_user .= "
        <li>
          <a href=\"" . url_prepare("user_index.php?action=detailconsult&amp;user_id=$id") . "\">$samename</a>
        </li>";
  }

  $hidden_fields = "";
  foreach ( $_POST as $form_key => $form_value) {
    if ( $form_key != "action" ) {
      if ( is_array($form_value) ) {
        $form_key.="[]";
        if ( !count($form_value) ) {
          $form_value[] = "";
        }
        foreach ( $form_value as $form_array_value ) {
          $hidden_fields.= create_hidden_input($form_key, $form_array_value);
        }
      } else {
        $hidden_fields.= create_hidden_input($form_key, $form_value);
      }
    }
  }

  $block = "
  <div class=\"detail\">
    <ul>
    $dis_same_user
    </ul>
  </div>
  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" . url_prepare('user_index.php') . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    <form name=\"form_back\" method=\"post\"
    action=\"" . url_prepare('user_index.php') . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
 </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the user delete validation screen
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function dis_user_can_delete($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $l_no_user, $l_check_usercreate, $l_check_userupdate;
  global $display, $l_user, $l_infos;

  $url = url_prepare('user_index.php');

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\" onsubmit=\"return confirm_del(this)\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  // Display entities where the user is referenced
  $obm_q = new DB_OBM;
  $tables = array("Event", "Contact", "Document");

  foreach($tables as $table_name) {
    $usercreate = $table_name."_usercreate";
    $query = "SELECT count(*)
              FROM $table_name
              WHERE $usercreate = '$p_id'";
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    $nbc = $obm_q->f('0');

    if ($nbc > 0) {
      $dis_user .=
        "<h1>$table_name</h1>
          <ul>
            <li><b>$nbc</b> $l_check_usercreate</li>
          </ul>";
    } else {
      $dis_user .=
        "<h1>$table_name</h1>
          <ul>
            <li>$l_no_user</li>
          </ul>";
    }
    $dis_user .= "<hr />";
  }

  $block .=
     "<div class=\"buttons\">
       $dis_delete
       $dis_back
     </div>
     <br />
     <h1>$l_user : $l_infos</h1>

     <table>
       $dis_user
     </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the user backup and restore form
///////////////////////////////////////////////////////////////////////////////
function dis_user_backup_form($backup, $params) {
  $user_id = $params['user_id'];
  $obm_q = run_query_user_detail($user_id);
  $url = url_prepare('user_index.php');

  //New backup form
  if (check_user_can_backup($user_id)) {
    $dis_new_backup = "
    <form method=\"post\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"backup\" />
      <input type=\"hidden\" name=\"execute\" value=\"1\" />
      <input type=\"hidden\" name=\"user_id\" value=\"$user_id\">

      <fieldset class=\"buttons\">
        <input type=\"submit\" name=\"submit\" value=\"{$GLOBALS['l_do_backup']}\" />
      </fielset>
    </form>";
  } else {
    $dis_new_backup = "<p>{$GLOBALS['l_cant_backup_pending_modifications']}</p>";
  }

  try {
    $backups = $backup->availableBackups();
    foreach ($backups as $record) {
      $date = new Of_Date($record['datetime']);
      $dis_date = $date->getOutputDateTime();
      $dis_buttons = '';
      foreach (OBM_Satellite_RestoreEntity::$restoreData as $method) {
        $dis_buttons .= "
        <form method=\"post\" action=\"$url\">
          <input type=\"hidden\" name=\"action\" value=\"restore\" />
          <input type=\"hidden\" name=\"execute\" value=\"1\" />
          <input type=\"hidden\" name=\"filename\" value=\"{$record['filename']}\">
          <input type=\"hidden\" name=\"method\" value=\"$method\">
          <input type=\"hidden\" name=\"user_id\" value=\"$user_id\">
          <input type=\"submit\" name=\"submit\" value=\"{$GLOBALS['l_restore_'.$method]}\" />
        </form>";
      }
      $dis_backups .= "<tr><td>$dis_date</td><td>{$record['filename']}</td><td><div class=\"buttons\">$dis_buttons</div></td></tr>";
    }

    if (!empty($dis_backups)) {
      $dis_backups = "
        <table class=\"spreadSheet\">
          <thead><tr><th>{$GLOBALS['l_date']}</th><th>{$GLOBALS['l_file']}</th><th>{$GLOBALS['l_restore']}</th></tr></thead>
          <tbody>$dis_backups</tbody>
        </table>";
    }

  } catch (Exception $e) {
    $display['msg'] .= display_err_msg($e->getMessage());
  }

  if (empty($dis_backups)) {
    $dis_backups = "<p>{$GLOBALS['l_no_backup_available']}</p>";
  }

  $backupftpservers = of_domain_get_domain_backupftpserver($obm_q->f('userobm_domain_id'));
  if (!empty($backupftpservers)) {
    //FTP backups
    $ftp_desc = sprintf($GLOBALS['l_retrieve_from_backup_ftp'],$obm_q->f('userobm_login').'@'.$obm_q->f('domain_name'));
    $dis_ftp_backups = "
      <form method=\"post\" action=\"$url\">
        <input type=\"hidden\" name=\"action\" value=\"backup\" />
        <input type=\"hidden\" name=\"retrieveAll\" value=\"1\" />
        <input type=\"hidden\" name=\"user_id\" value=\"$user_id\">

        <p>$ftp_desc</p>

        <fieldset class=\"buttons\">
          <input type=\"submit\" name=\"submit\" value=\"{$GLOBALS['l_retrieve_from_ftp']}\" />
        </fieldset>
      </form>";
  } else {
    $dis_ftp_backups = "
      <p>{$GLOBALS['l_no_backup_ftp_specified']}</p>";
  }

  $block = "
    <div class=\"detail extra\">
      <h1>{$GLOBALS['l_new_backup']}</h1>
      $dis_new_backup
    </div>
    <div class=\"detail extra\">
      <h1>{$GLOBALS['l_available_backups']}</h1>
      $dis_backups
    </div>
    <div class=\"detail extra\">
      <h1>{$GLOBALS['l_service_get_ftp_backups']}</h1>
      $dis_ftp_backups
    </div>
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the user administration index
///////////////////////////////////////////////////////////////////////////////
function dis_user_admin_index() {

  $block = of_userdata_dis_admin_form('user');

  return $block;
}


/**
 * Display the user csv import index
 **/
function dis_user_import_index() {
  global $display, $l_user, $l_header_import, $l_enabled, $l_kind, $l_profile;
  global $l_lastname, $l_firstname, $l_commonname,$l_login, $l_password, $l_password_type;
  global $l_mail, $l_mail_server, $l_email, $l_quota;
  global $l_desc, $l_group, $l_email_nomade, $l_access_nomade;
  global $l_delegation, $l_delegation_target;
  global $l_title, $l_phone, $l_mphone, $l_fax, $l_company, $l_direction;
  global $l_service, $l_address, $l_postcode, $l_town, $l_expresspostal;
  global $l_import_msg, $l_file_format, $l_perms, $l_local_copy;
  global $l_smb, $l_smb_perms, $l_smb_home, $l_smb_home_drive, $l_smb_logon;

  $block = "
  <div class=\"detail extra\">
    <h1>$l_file_format</h1>

  <table class=\"spreadSheet\">
  <thead>
  <tr>
    <th>1</th>
    <th>2</th>
    <th>3</th>
    <th>4</th>
    <th>5</th>
    <th>6</th>
    <th>7</th>
    <th>8</th>
    <th>9</th>
    <th>10</th>
    <th>11</th>
    <th>12</th>
    <th>13</th>
    <th>14</th>
    <th>15</th>
    <th>16</th>
    <th>17</th>
    <th>18</th>
    <th>19</th>
    <th>20</th>
    <th>21</th>
    <th>22</th>
    <th>23</th>
    <th>24</th>
    <th>25</th>
    <th>26</th>
    <th>27</th>
    <th>28</th>
    <th>29</th>
    <th>30</th>
    <th>31</th>
    <th>32</th>
    <th>33</th>
    <th>34</th>
    <th>35</th>
    <th>36</th>
    <th>37</th>
    <th>38</th>
    <th>39</th>
 </tr>
  <tr>
    <th>$l_kind</th>
    <th>$l_lastname</th>
    <th>$l_firstname</th>
    <th>$l_commonname</th>
    <th>$l_profile</th>
    <th>$l_delegation_target</th>
    <th>$l_delegation</th>
    <th>$l_login</th>
    <th>$l_password</th>
    <th>$l_password_type</th>
    <th>$l_mail</th>
    <th>$l_mail_server</th>
    <th>$l_email</th>
    <th>$l_quota</th>
    <th colspan=\"4\">$l_email_nomade</th>
    <th>$l_title</th>
    <th>$l_phone</th>
    <th>$l_phone 2</th>
    <th>$l_mphone</th>
    <th>$l_fax</th>
    <th>$l_fax 2</th>
    <th>$l_company</th>
    <th>$l_direction</th>
    <th>$l_service</th>
    <th colspan=\"6\">$l_address</th>
    <th>$l_desc</th>
    <th colspan=\"4\">$l_smb</th>
    <th>$l_group</th>
  </tr>
  <tr>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th>$l_email_nomade</th>
    <th>$l_perms</th>
    <th>$l_enabled</th>
    <th>$l_local_copy</th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th></th>
    <th>1</th>
    <th>2</th>
    <th>3</th>
    <th>$l_postcode</th>
    <th>$l_town</th>
    <th>$l_expresspostal</th>
    <th></th>
    <th>$l_smb_perms</th>
    <th>$l_smb_home</th>
    <th>$l_smb_home_drive</th>
    <th>$l_smb_logon</th>
    <th></th>
  </tr>
  </thead>
  <tr>
    <td>M</td>
    <td>BAUD</td>
    <td>Pierre</td>
    <td>Pierre BAUD</td>
    <td>admin</td>
    <td>France</td>
    <td>France</td>
    <td>pierre</td>
    <td>mot</td>
    <td>PLAIN</td>
    <td>X</td>
    <td>mailserv<br />auto<br />1</td>
    <td>pierre.baud</td>
    <td>300</td>
    <td>pb@mymail.org</td>
    <td>X</td>
    <td>X</td>
    <td></td>
    <td>Chief</td>
    <td>+33 0 11 22 33 44</td>
    <td></td>
    <td>06 06 06 06 06</td>
    <td>05 05 05 05 05</td>
    <td></td>
    <td>OBM corp</td>
    <td>IT</td>
    <td>SI</td>
    <td>rue de la paix</td>
    <td></td>
    <td></td>
    <td>31000</td>
    <td>Ramonville</td>
    <td>cedex 5</td>
    <td>nice guy</td>
    <td>X</td>
    <td>\\home\mydir</td>
    <td>E</td>
    <td>mylogon.bat</td>
    <td>Direction, Production</td>
  </tr>
  <tr>
    <td colspan=\"33\">$l_import_msg</td>
  </tr>
  </table>
  </div>";

  $block .= html_user_import_file();

  return $block;
}


/**
 * HTML Display Import Consultation File block
 **/
function html_user_import_file() {
  global $l_user, $l_header_import, $l_file, $l_import_file, $l_exemple_file;
  global $display, $ico_document, $resources_path;

  $url = url_prepare('user_index.php');

  $block = "
    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"import_file\" />
    <table>
    <tr>
      <th>$l_exemple_file</th>
      <td><a href=\"".url_prepare("$resources_path/public/user_import.sample.csv")."\"><img src=\"$ico_document\" /></a></td>
    </tr>
    <tr>
      <th>$l_import_file</th>
      <td>
        <input type=\"file\" name=\"fi_file\" id=\"fi_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
      </td>
    </tr>
    </table>
    <fieldset class=\"buttons\">
      <input type=\"submit\" name=\"submit\" value=\"$l_import_file\" />
    </fielset>
    </form>
    </div>";

  return $block;
}


/**
 * Process Import file
 * Insert new users from csv file. This does not handle user update
 **/
function dis_user_import_file_run($params) {
  global $obm, $display, $err, $ccscd_tab, $l_user, $l_header_import, $l_group;
  global $l_delegation, $l_login, $l_lastname, $l_firstname, $l_commonname, $l_email;
  global $l_login_in_use, $l_file_error, $l_user_import_row_cols_error;

  $file = $params['file_saved'];
  if ($file == '') {
    $file = $params['file'];
  }
  $file_name = $params['file_name'];
  $file_size = $params['file_size'];
  $csv_sep = $_SESSION['set_csv_sep'];
  if ($csv_sep == $ccsvd_tab) {
    $csv_sep = "\t";
  }

  $array_true = array('X', 'x', 'Y', 'YES', 'y', 'yes', 'O', 'OUI', 'o', 'oui', '1');

  $cpt = 0;

  $c_kind = $cpt++;
  $c_lastname = $cpt++;
  $c_firstname = $cpt++;
  $c_commonname = $cpt++;
  $c_profile = $cpt++;
  $c_delegation_target = $cpt++;
  $c_delegation = $cpt++;
  $c_login = $cpt++;
  $c_password = $cpt++;
  $c_password_type = $cpt++;
  $c_mail = $cpt++;
  $c_mail_server = $cpt++;
  $c_email = $cpt++;
  $c_mail_quota = $cpt++;
  $c_email_nomade = $cpt++;
  $c_nomade_perms = $cpt++;
  $c_nomade_enable = $cpt++;
  $c_nomade_local_copy = $cpt++;
  $c_title = $cpt++;
  $c_phone = $cpt++;
  $c_phone2 = $cpt++;
  $c_mobile = $cpt++;
  $c_fax = $cpt++;
  $c_fax2 = $cpt++;
  $c_company = $cpt++;
  $c_direction = $cpt++;
  $c_service = $cpt++;
  $c_ad1 = $cpt++;
  $c_ad2 = $cpt++;
  $c_ad3 = $cpt++;
  $c_postcode = $cpt++;
  $c_town = $cpt++;
  $c_cdx = $cpt++;
  $c_description = $cpt++;
  $c_smb_perms = $cpt++;
  $c_smb_home = $cpt++;
  $c_smb_home_drive = $cpt++;
  $c_smb_logon = $cpt++;
  $c_group = $cpt++;

  $nb_col = $cpt;

  $domain_id = $obm['domain_id'];
  $d = of_domain_get_domain_infos($domain_id, true);

  $dis_import .= "
  <thead>
  <tr>
    <th>$l_login</th>
    <th>$l_delegation</th>
    <th>$l_lastname</th>
    <th>$l_firstname</th>
    <th>$l_email</th>
    <th># $l_group</th>
    <th></th>
  </tr>
  </thead>";

  // Groups hash
  $groups = get_user_group_public_hash();
  foreach($groups as $name => $id) {
    $groups[strtolower($name)] = $id;
  }

  // Mail server hash
  $mailservers = of_domain_get_domain_mailserver('imap');
  foreach($mailservers as $ms_id => $one_ms_array) {
    foreach($one_ms_array as $one_ms) {
      $name = strtolower($one_ms['name']);
      $ms[$name] = $ms_id;
    }
  }

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('userobm');
  $num_line = 0;
  $fh = fopen($file, 'r');
  if (! $fh) {
    $display['msg'] .= display_err_msg($l_file_error);
    return;
  }
  while ( ($row = fgetcsv($fh, 4000, $csv_sep)) ) {
    $num_line++;
    $nb_group_ins = 0;
    $user = array();
    $user['user_id'] = 0;
    $user['kind'] = addslashes(trim($row[$c_kind]));
    $user['lastname'] = addslashes(trim($row[$c_lastname]));
    $user['firstname'] = addslashes(trim($row[$c_firstname]));
    $user['commonname'] = addslashes(trim($row[$c_commonname]));
    $user['delegation_target'] = addslashes(trim($row[$c_delegation_target]));
    $user['delegation'] = addslashes(trim($row[$c_delegation]));
    $user['login'] = addslashes(trim($row[$c_login]));
    $user['passwd'] = addslashes(trim($row[$c_password]));
    if (trim($row[$c_password_type]) == 'CRYPT') {
      $user['password_type'] = 'CRYPT';
    } else if (trim($row[$c_password_type]) == 'MD5SUM') {
      $user['password_type'] = 'MD5SUM';
    } else {
      $user['password_type'] = 'PLAIN';
    }
    //$user['passwd'] = $user['password'];
    if (in_array(trim($row[$c_mail]), $array_true)) {
      $user['mail_perms'] = 1;
      $user['mail_server_id'] = addslashes(trim($row[$c_mail_server]));
      // get autoselected mail server if mail server set as 'auto'
      if ($user['mail_server_id'] == 'auto') {
	$user = get_user_params_mail_server_id($user);
      }
      // If mail server is a host name, get its id
      if (! is_numeric($user['mail_server_id'])) {
	$user['mail_server_id'] = $ms[strtolower($user['mail_server_id'])];
      }
    }

    $user['email'] = addslashes(trim(strtolower($row[$c_email])));

    // If quota no a number (and a correct one) get the domain default value
    if (is_numeric(addslashes(trim($row[$c_mail_quota])))
	&& (addslashes(trim($row[$c_mail_quota])) > 0)
	&& (addslashes(trim($row[$c_mail_quota])) < 100000)) {
      $user['mail_quota'] = addslashes(trim($row[$c_mail_quota]));
    } else {
      $user['mail_quota'] = $d['mail_quota'];
    }
    $user['profile'] = (trim($row[$c_profile]) != '' ? addslashes(trim($row[$c_profile])) : 'user');
    $user['title'] = addslashes($row[$c_title]);
    $user['phone'] = addslashes($row[$c_phone]);
    $user['phone2'] = addslashes($row[$c_phone2]);
    $user['mobile'] = addslashes($row[$c_mobile]);
    $user['fax'] = addslashes($row[$c_fax]);
    $user['fax2'] = addslashes($row[$c_fax2]);
    $user['company'] = addslashes($row[$c_company]);
    $user['direction'] = addslashes($row[$c_direction]);
    $user['service'] = addslashes($row[$c_service]);
    $user['ad1'] = addslashes($row[$c_ad1]);
    $user['ad2'] = addslashes($row[$c_ad2]);
    $user['ad3'] = addslashes($row[$c_ad3]);
    $user['zip'] = addslashes($row[$c_postcode]);
    $user['town'] = addslashes($row[$c_town]);
    $user['cdx'] = addslashes($row[$c_cdx]);
    $user['desc'] = addslashes($row[$c_description]);

    // Delegation
    $user['delegation'] = str_replace(';', '/', $row[$c_delegation]);
    $user['delegation_target'] = $user['delegation']; 
    
    // Email Nomade
    if (trim($row[$c_email_nomade]) != '') {
      $user['email_nomade'] = trim($row[$c_email_nomade]);
    }
    if (in_array(trim($row[$c_nomade_perms]), $array_true)) {
      $user['nomade_perms'] = 1;
    } else {
      $user['nomade_perms'] = 0;
    }
    if (in_array(trim($row[$c_nomade_enable]), $array_true)) {
      $user['nomade_enable'] = 1;
    } else {
      $user['nomade_enable'] = 0;
    }
    if (in_array(trim($row[$c_nomade_local_copy]), $array_true)) {
      $user['nomade_local_copy'] = 1;
    } else {
      $user['nomade_local_copy'] = 0;
    }
    if (in_array(trim($row[$c_smb_perms]), $array_true)) {
      $user['smb_perms'] = 1;
    } else {
      $user['smb_perms'] = 0;
    }
    $user['smb_home'] = addslashes($row[$c_smb_home]);
    $user['smb_home_drive'] = addslashes($row[$c_smb_home_drive]);
    $user['smb_logon'] = addslashes($row[$c_smb_logon]);
    $user['profiles'] = &$params['profiles']; 

    if (count($row) != $nb_col) {
      // Row not OK
      $user['user_id'] = false;
      $res = $l_user_import_row_cols_error;
    } else if (check_user_login_in_use($domain_id, $user['login'])) {
      $res = "$user[login] : $l_login_in_use";
    } else {
      // Insert the user
      if (check_user_data_form('', $user)) {
	$user['user_id'] = run_query_user_insert($user);
        if ($user['user_id'] > 0) {
          set_update_state();
          $res = '+';
          // handle groups membership
          $group = trim(strtok(strtolower($row[$c_group]),','));
          $cpt_g = 0;
          while ($group) {
            if (array_key_exists($group, $groups)) {
              $cpt_g++;
              $user["group_$cpt_g"] = $groups[$group];
            }
            $group = trim(strtok(','));
          }
          $user['group_nb'] = $cpt_g;
          $nb_group_ins = run_query_user_update_user_group($user);
        }
      } else {
	// User not ok, stop for this row
	$user['user_id'] = false;
	$res = "! $err[field] - $err[msg]";
      }
    }

    $dis_import .= "
    <tr>
      <td>$user[login]</td>
      <td>$user[delegation]</td>
      <td>$user[lastname]</td>
      <td>$user[firstname]</td>
      <td>$user[email]</td>
      <td>$nb_group_ins</td>
      <td>$res</td>
    </tr>";
  }
  fclose($fh);

  $block = "
  <table class=\"spreadSheet\">
  $dis_import
  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the User Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_user_display_pref($prefs) {
  global $l_user_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'user');
  $dis_pref->pref_title = $l_user_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}

/**
 * dis_user_export_pdf 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function dis_user_export_pdf($id) {
  global $ini_array;
  $pdf = OBM_Pdf::load('user');
  $pdf->setFontSize(11);
  
  $user = run_query_user_detail($id);
  $login = $user->f('userobm_login');
  $password = $user->f('userobm_password');
  $domain = $user->f('domain_name');
  $email = get_entity_email($user->f('userobm_email'), $domain, false, null);
  $phone = $user->f('userobm_phone');
  $lname = $user->f('userobm_lastname');
  $fname = $user->f('userobm_firstname');
  $display_name = $user->f('userobm_firstname').' '.$user->f('userobm_lastname');
  if($user->f('userobm_address1')) $address[] = $user->f('userobm_address1');
  if($user->f('userobm_address2')) $address[] = $user->f('userobm_address2');
  if($user->f('userobm_address3')) $address[] = $user->f('userobm_address3');
  if($user->f('userobm_address3')) $address[] = $user->f('userobm_address3');
  if($user->f('userobm_zipcode' || $user->f('userobm_town'))) $address[] = $user->f('userobm_zipcode') . ' ' . $user->f('userobm_town');
  if($user->f('userobm_country_name')) $address[] = $user->f('userobm_country_name');

  $admin = get_user_info();

  $admin_name_max_length = 40;
  $admin_email_max_length = 60;
  $display_name_max_length = 40;
  $user_login_max_length = 70;
  $user_password_max_length = 70;
  $user_email_max_length = 70;

  // Apply font and draw text
  $Y = 705;
  $X = 50;
  $pdf->pages[0]->setFont(Zend_Pdf_Font::fontWithName(Zend_Pdf_Font::FONT_HELVETICA_ITALIC), $pdf->getFontSize());
  $Y -= $pdf->getLineHeight();

  $pdf->drawSplittedText(
          $pdf->pages[0],
          $admin['firstname']." ".$admin['lastname'],
          $admin_name_max_length,
          $X,
          $Y
  );
  $pdf->pages[0]->drawText("$GLOBALS[l_phone] : $admin[phone]", $X, $Y, 'UTF-8');
  $Y -= $pdf->getLineHeight();
  $pdf->drawSplittedText(
          $pdf->pages[0],
          $GLOBALS['l_email']." : ".$admin['email'],
          $admin_email_max_length,
          $X,
          $Y
  );
  $pdf->pages[0]->setFont($pdf->getFont(), $pdf->getFontSize());
  $pdf->pages[0]->setFillColor(new Zend_Pdf_Color_GrayScale(0));
  $Y = 715;
  $X = 300;
  $Y -= $pdf->getLineHeight();
  $pdf->drawSplittedText($pdf->pages[0], $display_name, $display_name_max_length, $X, $Y);
  foreach($address as $address_part) {
    $Y -= $pdf->getLineHeight();  
    $pdf->pages[0]->drawText($address_part, $X, $Y, 'UTF-8');
  }

  $Y = 420;
  $X = 98;
  $Y -= $pdf->getLineHeight();
  if(array_key_exists('show-domain-in-login-pdf',$ini_array) && $ini_array['show-domain-in-login-pdf']  == '1') {
    $displayed_login = $login."@".$domain;
  } else {
    $displayed_login = $login;
  }
  $pdf->drawSplittedText($pdf->pages[0], $GLOBALS['l_login']." : ".$displayed_login, $user_login_max_length, $X, $Y);
  $pdf->drawSplittedText($pdf->pages[0], $GLOBALS['l_password']." : ".$password, $user_password_max_length, $X, $Y);
  for($i = 0; $i < 5 && $i < count($email); $i++) {
    $mail = $email[$i];
    $pdf->drawSplittedText($pdf->pages[0], $GLOBALS['l_email']." : ".$mail, $user_email_max_length, $X, $Y);
    if($i == 4 && $count($email) > 4) {
      $Y -= $pdf->getLineHeight();  
      $pdf->pages[0]->drawText("$GLOBALS[l_email] : [...]", $X, $Y, 'UTF-8');  
    }
  }
  $Y -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText("$GLOBALS[l_phone] : ". $phone, $X, $Y, 'UTF-8');    
  $Y -= $pdf->getLineHeight();  
  $pdf->pages[0]->drawText('OBM : '. $GLOBALS['cgp_host'], $X, $Y, 'UTF-8');
  $pdf->download($login);
}
/**
 * Build a JSON array with all search results 
 * 
 * @param $results array of 'length' => DB user results nb, and 'datas' => DB User search query results
 * @access public
 * @return void
 */
function json_search_users($user,$results) {
  global $display, $module, $cright_write_admin, $perm;

  $user_q = $results['datas'];

  $users = array();
  foreach($user_q as $name => $data) {
    $id = $data['userobm_id'];
    $label = phpStringToJsString($data['userobm_lastname'].' '.$data['userobm_firstname']);
    if($user_q['userobm_hidden'] != 1 || $perm->check_right($module, $cright_write_admin)) {
      $extra = phpStringToJsString(get_entity_email($data['userobm_email']));
    } else {
      $extra = '';
    }
    $users[] = "{id:'data-user-$id', label:'$label', extra:'$extra'}";
  }
  $display['json'] = "{length:".$results['length'].", datas:[".implode(',',$users)."]}";
}


/**
 * Generate email field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_user_mail_field($domain_id, $values, $class) {

  if (!$GLOBALS['obm']['domain_global']) {
    if(!$domain_id) $domain_id = $GLOBALS['obm']['domain_id'];
    $d = of_domain_get_domain_infos($domain_id, true);
    if(!empty($d['alias'])) {
      $aliases = explode("\r\n",$d['alias']);
    } else {
      $aliases = array();
    }
    array_unshift($aliases,$d['name']);
  } else {
    // Creation from GLOBAL, do not propose aliases as we don't know the domain
    $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
    $aliases = array();
  }

  if(!empty($values)) {
    $emails = explode("\r\n",$values);
  }

  $count = 0;
  if(is_array($emails)) {
    foreach($emails as $key => $email) {
      $sel_alias = '';
      list($mail,$domain) = explode('@',$email);  
      foreach($aliases as $alias) {
        if( $alias == $domain) {
          $sel_alias .= "<option selected='selected' value='$alias'>$alias</option>";
        } else {
          $sel_alias .= "<option value='$alias'>$alias</option>";
        } 
      }
      $sel_alias = "<select name='sel_aliases[]'>
        <option value=''>$GLOBALS[l_all_aliases]</option>
        $sel_alias
        </select>";

      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'userMailHome');show_hide_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>        
        <input name='tf_email[]' value=\"$mail\" /> @ $sel_alias
        </div>";
      $count ++;
    }
  }
  $sel_alias = '';
  foreach($aliases as $alias) {
    $sel_alias .= "<option value=\"$alias\">$alias</option>";
    $sel_js .= "aliasSelectTemplate.adopt(new Element('option').setProperty('value','$alias').appendText('$alias'));\n";
  }
  $sel_alias = "
    <select name=\"sel_aliases[]\">
    <option value=\"\">$GLOBALS[l_all_aliases]</option>
    $sel_alias
    </select>";
  if($GLOBALS['c_max_user_alias'] ==0 || $count < $GLOBALS['c_max_user_alias']) {
    $email_block .= "
      <div class=\"multiple\">
      <a onclick=\"remove_element(this.parentNode,'userMailHome');show_hide_add_button();return false\" href=\"\">
       <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
      </a>        
      <input name='tf_email[]' value='' /> @ $sel_alias 
      </div>         
      ";
    $count++;
  } 
  if($GLOBALS['c_max_user_alias'] ==0 || $count < $GLOBALS['c_max_user_alias']) {
    $add_button = "<a id='addMailButton' href='' onclick=\"add_email_field(aliasSelectTemplate);show_hide_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }
  
  $email_block = "
  <th class=\"$class[email]\" id='userMailLabel'>
  $GLOBALS[l_email]
  <script type='text/javascript'>
    var aliasSelectTemplate = new Element('select').setProperty('name','sel_aliases[]');
    aliasSelectTemplate.adopt(new Element('option').setProperty('value','').appendText('$GLOBALS[l_all_aliases]'));
    $sel_js
  </script>
  $add_button
  </th>
  <td id='userMailHome'>
  $email_block
  </td>
  ";

  return $email_block;
}

/**
 * Generate external email field 
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_user_nomade_mail_field($emails, $class) {

  if(!empty($emails) && !is_array($emails)) {
    $emails = explode("\r\n",$emails);
  }

  $count = 0;
  if(is_array($emails)) {
    foreach($emails as $key => $email) {
      $email_block .= "<div class=\"multiple\">
        <a onclick=\"remove_element(this.parentNode,'nomadeMailHome');show_hide_nomade_add_button();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>
        <input class='emailNomade' name='tf_email_nomade[]' value=\"$email\" />
        </div>";
      $count ++;
    }
  }

  if ($GLOBALS['c_max_email_nomade'] ==0 || $count < $GLOBALS['c_max_email_nomade']) {
    $email_block .= "
    <div class=\"multiple\">
    <a onclick=\"remove_element(this.parentNode,'nomadeMailHome');show_hide_nomade_add_button();return false\" href=\"\">
     <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
    </a>
    <input class='emailNomade' name='tf_email_nomade[]' value='' />
    </div>
    ";  
    $count++;
  }
  if ($GLOBALS['c_max_email_nomade'] ==0 || $count < $GLOBALS['c_max_email_nomade']) {
    $add_button = "<a id='addMailNomadeButton' href='' onclick=\"add_nomade_email_field();show_hide_nomade_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }

  $email_block = "
  <tr><th class=\"$class[email_nomade]\" id='nomadeMailLabel'>
  $GLOBALS[l_email_nomade]
  $add_button
  </th>
  <td id='nomadeMailHome'>
  $email_block
  </td></tr>";

  return $email_block;
}

function html_user_batch_form($params) {
  // Globalize detail header
  global $l_user, $l_mail, $l_nomade, $l_coord;
  // Globalize form labels
  global $l_archive, $l_profile, $l_quota, $l_perms, $l_enabled, $l_local_copy;
  global $l_phone, $l_mphone, $l_fax, $l_company, $l_direction, $l_service, $l_address, $l_postcode, $l_town, $l_expresspostal;
  // other
  global $l_yes, $l_no, $l_do_batch_processing;
  
  $l_phone2 = $l_phone.' 2';
  $l_fax2 = $l_fax.' 2';
  $l_address2 = $l_address.' 2';
  $l_address3 = $l_address.' 3';
  
  $profiles = $params['profiles'];
  
  $sel_default = array('label' => '-----', 'value' => '');
  $selected = "selected=\"selected\"";
  
  $archive = $params['archive'];
  $profile = $params['profile_name'];
  $mail_perms = $params['mail_perms'];
  $mail_quota = $params['mail_quota'];
  $nomade_perms = $params['nomade_perms'];
  $nomade_enable = $params['nomade_enable'];
  $nomade_local_copy = $params['nomade_local_copy'];
  if (isset($params['delegation_target'])) { $delegation_target = stripslashes($params['delegation_target']); }
  if (isset($params['delegation'])) { $delegation = stripslashes($params['delegation']); }  
  
  $archive_block = "
  <select name=\"sel_archive\">
  	<option value=\"$sel_default[value]\" ".(!isset($archive) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($archive=='0' ? $selected : '').">$l_no</option>
  	<option value=\"1\" ".($archive=='1' ? $selected : '').">$l_yes</option>
  </select>";
  
  $profile_block = "
  <select name=\"sel_profile_name\">
  	<option value=\"$sel_default[value]\" ".(!isset($profile_id) ? $selected : '').">$sel_default[label]</option>
  ";
  foreach($profiles as $profile_name => $profile_value) {
    $profile_label = isset($GLOBALS["l_perm_$profile_name"]) ? $GLOBALS["l_perm_$profile_name"] : $profile_name;
    $profile_block .= "<option value=\"$profile_name\" ".($profile_name==$profile ? $selected : '').">$profile_label</option>";
  }
  
  $mail_perms_block = "
  <select name=\"sel_mail_perms\">
  	<option value=\"$sel_default[value]\" ".(!isset($mail_perms) ? $selected : '').">$sel_default[label]</option>
  	<option value=\"0\" ".($mail_perms=='0' ? $selected : '').">$l_no</option>
  	<option value=\"1\" ".($mail_perms=='1' ? $selected : '').">$l_yes</option>
  </select>
  <input type='hidden' name='mail_server_id' value='auto' />";
  
  $mail_quota_block = "
  <input type=\"text\" id=\"tf_mail_quota\" name=\"tf_mail_quota\" value=\"$mail_quota\" size=\"6\" maxlength=\"6\" />";
  
  $nomade_perms_block = "
  <select name=\"sel_nomade_perms\">
  	<option value=\"$sel_default[value]\" ".(!isset($nomade_perms) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($nomade_perms=='0' ? $selected : '').">$l_no</option>
  	<option value=\"1\" ".($nomade_perms=='1' ? $selected : '').">$l_yes</option>
  </select>";
  
  $nomade_enable_block = "
  <select name=\"sel_nomade_enable\">
  	<option value=\"$sel_default[value]\" ".(!isset($nomade_enable) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($nomade_enable=='0' ? $selected : '').">$l_no</option>
  	<option value=\"1\" ".($nomade_enable=='1' ? $selected : '').">$l_yes</option>
  </select>";
  
  $nomade_local_copy_block = "
  <select name=\"sel_nomade_local_copy\">
  	<option value=\"$sel_default[value]\" ".(!isset($nomade_local_copy) ? $selected : "").">$sel_default[label]</option>
  	<option value=\"0\" ".($nomade_local_copy=='0' ? $selected : '').">$l_no</option>
  	<option value=\"1\" ".($nomade_local_copy=='1' ? $selected : '').">$l_yes</option>
  </select>";
  
  $buttons_block = "
    <input type=\"hidden\" name=\"action\" value=\"batch_processing\" />
  	<input type=\"submit\" value=\"$l_do_batch_processing\" />";
  if (!isset($params['data-user-id'])) {
    foreach ($params as $params_name => $params_value) {
      if (! preg_match("/^data-user-[0-9]+$/", $params_name)) {
        continue;
      } else if ($params_value == 'on') {
        $user_id = substr($params_name, strlen('data-user-'));
        $buttons_block .= "
        <input type=\"hidden\" name=\"data-user-id"."[]\" value=\"$user_id\" />";
      }
    }
  } else {
    foreach ($params['data-user-id'] as $u_id) {
      $buttons_block .= "
      <input type=\"hidden\" name=\"data-user-id"."[]\" value=\"$u_id\" />";
    }
  }

  if ($GLOBALS['cgp_use']['property']['delegation']) {
    $delegation_block = "
    <fieldset class=\"detail infos\">
      <legend>$GLOBALS[l_delegation]</legend>
      <table id=\"delegation\">
        <tr id=\"delegation_field\">
          <th>$GLOBALS[l_delegation]</th>
          <td>".disp_tf_coord('delegation', 128, $params)."</td>
        </tr><tr id=\"delegation_target_field\">
          <th>$GLOBALS[l_delegation_target]</th>
          <td>".disp_tf_coord('delegation_target', 128, $params)."</td>
        </tr>
      </table> 
    </fieldset>
    ";
  }

  $form_block = "
  <fieldset class=\"detail infos\">
  <legend>$l_user</legend>
    <table>
    <tr>
      <th>$l_archive</th>
      <td>$archive_block</td>
    </tr>
    <tr>
      <th>$l_profile</th>
      <td>$profile_block</td>
    </tr>
    </table>
  </fieldset>
  <fieldset class=\"detail infos\">
    <legend>$l_mail</legend>
    <table>
    <tr>
      <th>$l_mail</th>
      <td>$mail_perms_block</td>
    </tr>
    <tr>
      <th>$l_quota</th>
      <td>$mail_quota_block</td>
    </tr>
    </table>
  </fieldset>
  <fieldset class=\"detail infos\">
    <legend>$l_nomade</legend>
    <table>
    <tr>
      <th>$l_perms</th>
      <td>$nomade_perms_block</td>
    </tr>
    <tr>
      <th>$l_enabled</th>
      <td>$nomade_enable_block</td>
    </tr>
    <tr>
      <th>$l_local_copy</th>
      <td>$nomade_local_copy_block</th>
    </tr>
    </table>
  </fieldset>
  $delegation_block
  <fieldset class=\"detail infos\">
    <legend>$l_coord</legend>
    <table id=\"coord\">
      <tr id=\"phone\">
        <th>$l_phone</th>
        <td>".disp_tf_coord('phone', 32, $params)."</td>
      </tr><tr id=\"phone2\">
        <th>$l_phone2</th>
        <td>".disp_tf_coord('phone2', 32, $params)."</td>
      </tr><tr id=\"mobile\">
        <th>$l_mphone</th>
        <td>".disp_tf_coord('mobile', 32, $params)."</td>
      </tr><tr id=\"fax\">
        <th>$l_fax</th>
        <td>".disp_tf_coord('fax', 32, $params)."</td>
      </tr><tr id=\"fax2\">
        <th>$l_fax2</th>
        <td>".disp_tf_coord('fax2', 32, $params)."</td>
      </tr><tr id=\"company\">
        <th>$l_company</th>
        <td>".disp_tf_coord('company', 64, $params)."</td>
      </tr><tr id=\"direction\">
        <th>$l_direction</th>
        <td>".disp_tf_coord('direction', 64, $params)."</td>
      </tr><tr id=\"service\">
        <th>$l_service</th>
        <td>".disp_tf_coord('service', 64, $params)."</td>
      </tr><tr id=\"address1\">
        <th>$l_address</th>
        <td>".disp_tf_coord('address1', 64, $params)."</td>
      </tr><tr id=\"address2\">
        <th>$l_address2</th>
        <td>".disp_tf_coord('address2', 64, $params)."</td>
      </tr><tr id=\"address3\">
        <th>$l_address3</th>
        <td>".disp_tf_coord('address3', 64, $params)."</td>
      </tr><tr id=\"zipcode\">
        <th>$l_postcode</th>
        <td>".disp_tf_coord('zipcode', 14, $params)."</td>
      </tr><tr id=\"town\">
        <th>$l_town</th>
        <td>".disp_tf_coord('town', 64, $params)."</td>
      </tr><tr id=\"expresspostal\">
        <th>$l_expresspostal</th>
        <td>".disp_tf_coord('expresspostal', 16, $params)."</td>
      </tr>
    </table>
  </fieldset>
  <fieldset class=\"buttons\">
  	$buttons_block
  </fieldset>";
        
  $js_block = "
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      fill_coords($('delegation'))
      fill_coords($('coord'));
    })
  </script>";
  
  $style_block = "
  <style>
  	.icons {
  		padding-left: 0.3em;
  	}
  </style>";
  
  $form_block = "
  $style_block
  <form method=\"post\">
  	$form_block
  	$js_block
  </form>";
  
  return $form_block;
}


// Display tf_ coord function
function disp_tf_coord($name, $size, $params) {
  
  $field_to_update = 'to_update_'.$name;
  if (!isset($params[$field_to_update])) {
    $value = 'false';
  } else {
    $value = $params[$field_to_update];
  }
  
  $block = "
  	<input type=\"hidden\" name=\"$field_to_update\" value=\"$value\" />
  	<input type=\"text\" name=\"tf_$name\" size=\"$size\" maxlengt=\"$size\" value=\"$params[$name]\" />";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result
// Parameters:
//   - $user[]   : user search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_user_wait_list($user) {
  global $l_found, $l_no_found;
  global $display, $obm;
  
  $user['archive'] = 1;
  $user['status'] = 'INIT';
  $user['delegation'] = $obm['delegation_target'];

  $popup = $user['popup'];
  $filter_entity = $user['filter_entity'];
  
  $obm_q = run_query_user_search($user);

  $prefs = get_display_pref($obm['uid'], 'user');
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_user $l_found");
    $block = html_user_wait_list($obm_q, $prefs, $user, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Waiting people Search result
// Parameters:
//   - $obm_q    : database result (people list)
//   - $prefs    : the fields which have to be displayed
//   - $user[]   : user search criteria
//     keys used : login, lastname, pemrs
///////////////////////////////////////////////////////////////////////////////
function html_user_wait_list($obm_q, $prefs, $user, $popup) {
  global $l_close, $l_add;

  $lname = urlencode($user['lastname']);
  $fname = urlencode($user['firstname']);
  $desc = urlencode($user['desc']);

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('user', $user);

  $url = url_prepare("user_index.php?action=search&amp;tf_firstname=$fname&amp;tf_lastname=$lname&amp;tf_email=$email&amp;tf_desc=$desc$url_userdata$url_ext");
  $user_d = new OBM_DISPLAY('DATA', $prefs, 'user');

  $user_d->data_set = $obm_q;
  $user_d->data_header = 'both';
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display('dis_user_data');
  $block .= $display_popup_end;

  return $block;
}
?>
