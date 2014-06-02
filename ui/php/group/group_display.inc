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
// OBM - File : group_display.php                                            //
//     - Desc : Group Display File                                           //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames['group_name'] = $l_name;
$fieldnames['group_email'] = $l_email;
if($perm->check_right('group', $cright_read)) {
  $fieldnames['domain_label'] = $l_domain;
  $fieldnames['group_gid'] = $l_gid;
  $fieldnames['group_delegation'] = $l_delegation;
  $fieldnames['group_system'] = $l_system;
  $fieldnames['group_privacy'] = $l_private;
  $fieldnames['group_local'] = $l_local;
  $fieldnames['group_ext_id'] = $l_ext_id;
  $fieldnames['group_samba'] = $l_smb;
  $fieldnames['group_desc'] = $l_desc;
  $fieldnames['usercreate'] = $l_creator;
  $fieldnames['userupdate'] = $l_updater;
  $fieldnames['timecreate'] = $l_date_creation;
  $fieldnames['timeupdate'] = $l_date_last_update;
  // Calculated fields
  $fieldnames['group_nb_user'] = $l_nb_user;
  $fieldnames['group_user_login'] = $l_login;
  $fieldnames['group_user_lastname'] = $l_lastname;
  $fieldnames['group_user_firstname'] = $l_firstname;
  $fieldnames['group_user_function'] = $l_function;
  $fieldnames['group_user_phone'] = $l_phone;
  $fieldnames['group_user_email'] = $l_email;
  $fieldnames['group_contact_lastname'] = $l_lastname;
  $fieldnames['group_contact_firstname'] = $l_firstname;
  $fieldnames['group_contact_function'] = $l_function;
  $fieldnames['group_contact_phone'] = $l_phone;
  $fieldnames['group_contact_email'] = $l_email;  
}

///////////////////////////////////////////////////////////////////////////////
// Display Group specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_group(&$OD, $fieldname, $link_ok) {
  global $path, $obm, $params, $ico_mail,$cgp_use;
  global $module, $cright_write_admin, $perm;

  if (($fieldname == 'group_name')) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_group_get_id('.$OD->data_set->f('group_id').",'".addslashes($OD->data_set->f('group_name'))."');";
    } else {
      if ($link_ok) {
        $res['url'] = "$path/group/group_index.php?action=detailconsult&amp;group_id=".$OD->data_set->f('group_id');
      }
    }
  }


  if (($fieldname == 'data_element') && $params['ext_element'] != '') {
    $res['name'] = "<span id=\"data-group-".$OD->data_set->f('group_id')."\" style=\"display:none;\">".
    $OD->data_set->f('group_name').'</span>';
  }  

  else if ($fieldname == 'group_system') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'group_privacy') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'group_samba') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'group_local') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'group_nb_user') {
    $res['align'] = 'center';
  }

  // For User member lists
  elseif (($fieldname == 'group_user_login') && $link_ok) {
    $res['url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f('group_user_id');
  }

  else if ($fieldname == 'group_user_email') {
    $email = $OD->data_set->f('group_user_email');
    if ($OD->data_set->f('userobm_hidden') == 1 && !$perm->check_right('user', $cright_write_admin)) {
      $res['align'] = 'center';
      $res['name'] = '-';
    } elseif (strcmp($email ,'') != 0) {
      $email = get_entity_email($email, $OD->data_set->f('domain_name'));
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\">$email";
      $res['txt_name'] = "$email";
    }
    
  }

  else if ($fieldname == 'group_email') {
    $email = $OD->data_set->f('group_email');
    if (strcmp($email,'') != 0) {
      $email = get_entity_email($email, $OD->data_set->f('domain_name'));
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\" />$email";
      $res['txt_name'] = $email;
    }
  }

  // For User member lists
  elseif (($fieldname == 'group_contact_lastname') && $link_ok) {
    $res['url'] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$OD->data_set->f('group_contact_id');
  }

  else if ($fieldname == 'group_contact_email') {
    $email = $OD->data_set->f('group_contact_email');
    if (strcmp($email ,'') != 0) {
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\">$email";
      $res['txt_name'] = "$email";
    }
    
  }  

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group search Form
// Parameters : 
//   - $group[]   : default form values
//     keys used  : name, user
///////////////////////////////////////////////////////////////////////////////
function html_group_search_form ($group) {
  global $obm, $display, $l_domain;
  global $l_name, $l_group_user, $l_email, $l_find, $l_add_groups,$l_visibility;
 
  $popup = $group['popup'];

  if ($group['children_restriction']) {
    $child_res = "<input name=\"child_res\" type=\"hidden\" value=\"1\">";
  }

  if ($group['restriction_calendar'] == 'calendar') {
    $cal_res = "<input type=\"hidden\" name=\"restriction_calendar\" value=\"calendar\" />";
  }
  if (isset($group['iso_date'])) {
    $iso_date = urlencode($group['iso_date']);
    $date_res = "<input type=\"hidden\" name=\"date\" value=\"$iso_date\" />";
  }
  if ($popup) {
    $ext_action = $group['ext_action'];
    $ext_target = $group['ext_target'];
    $ext_widget = $group['ext_widget'];
    $ext_widget_text = $group['ext_widget_text'];
    $ext_url = $group['ext_url'];
    $ext_id = $group['ext_id'];
    $ext_title = stripslashes($group['ext_title']);
    $ext_element =  $group['ext_element'];
    $ext_container = $group['ext_container'];

    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
      <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">      
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">
      <input name=\"ext_container\" type=\"hidden\" value=\"$ext_container\" />";
    if ($ext_title == '') {
      $ext_title = $l_add_groups;
    }
    $display['title'] = $ext_title;
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($group['name']);
  $domain_id = $group['domain_id'];
  $delegation = stripslashes($group['delegation']);
  $email = stripslashes($group['email']);
  $user = stripslashes($group['user']);
  $privacy = $group['privacy'];
  $desc = $group['desc'];
  $samba = $group['samba'];
  if ($obm['domain_global']) {
    $sel_domain = of_domain_dis_select($domain_id, 'all');
    $dis_domain = "<label>$l_domain<br />
      $sel_domain
    </label>";
  }

  // User defined data
  $block_userdata .= of_userdata_dis_search('group', $group);
  
  $display_pref = get_display_pref($obm['uid'],'group');

  $dis_delegation = of_delegation_dis_search_form($delegation);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" action=\"".url_prepare('group_index.php')."\" class=\"search\">
";
  
  if ($display_pref['group_name']['status'] != 0) {
    $block .= "
    <label>$l_name<br />
      <input name=\"tf_name\" size=\"16\" maxlength=\"256\" value=\"$name\" />
    </label>";
  }
  
  if ($display_pref['domain_label']['status'] != 0) {
    $block .= $dis_domain;
  }
  
  if ($display_pref['group_delegation']['status'] != 0) {
    $block .= $dis_delegation;
  }
  
  $block .= "
    <label>$l_group_user<br />
      <input name=\"tf_user\" size=\"16\" maxlength=\"16\" value=\"$user\" />
    </label>";
  
  if ($display_pref['group_email']['status'] != 0) {
    $block .= "
    <label>$l_email<br />
      <input name=\"tf_email\" size=\"24\" maxlength=\"64\" value=\"$email\" />
    </label>";
  }
  
  if ($display_pref['group_desc']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_desc]<br />
      <input name=\"tf_desc\" size=\"24\" maxlength=\"128\" value=\"$desc\" />
    </label>";
  }
  
  if ($display_pref['group_samba']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_smb]<br />
      <input type='checkbox' name=\"cb_samba\" ".($samba == 1?'checked="true"':'')." value=\"1\" />
    </label>";
  }
  
  $block .= "
    $block_userdata
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $child_res
      $cal_res
      $date_res
      $ext
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Group search result
// Parameters:
//   - $group[]   : group search criteria
//     keys used  : name, description, email
///////////////////////////////////////////////////////////////////////////////
function dis_group_search_group($group) {
  global $obm, $display, $l_found, $l_no_found;

  $child_res = $group['children_restriction'];

  $prefs = get_display_pref($obm['uid'], 'group');
  if ($child_res) {
    $obm_q = run_query_group_search_possible_children($group);
  }  else {
    $obm_q = run_query_group_search($group);
  }

  $nb_group = $obm_q->num_rows_total();
  if ($nb_group == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_group $l_found");
    $block = html_group_search_group($obm_q, $prefs, $group);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Group search result
// Parameters : 
//   - $obm_q     : list of the groups to display 
//   - $prefs     : the fields which have to be displayed
//   - $group[]   : group search criteria
//     keys used  : name, user, popup
///////////////////////////////////////////////////////////////////////////////
function html_group_search_group($obm_q, $prefs, $group) {
  global $l_add, $l_close;

  $child_res = $group['children_restriction'];
  $popup = $group['popup'];

  if ($popup) {
    $ext_action = $group['ext_action'];
    $ext_url = $group['ext_url'];
    $ext_target = $group['ext_target'];
    $ext_widget = $group['ext_widget'];
    $ext_widget_text = $group['ext_widget_text'];
    $ext_id = $group['ext_id'];
    $ext_element = $group['ext_element'];
    $ext_title = urlencode(stripslashes($group['ext_title']));
    $restriction_calendar = $group['restriction_calendar'];
    $ext_container = $group['ext_container'];
    $filter = $group['filter'];
    $url_ext = "&amp;restriction_calendar=$restriction_calendar&amp;popup=1&amp;filter=$filter&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_titled&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;child_res=$child_res&amp;ext_container=$ext_container";
  }

  $name = urlencode(stripslashes($group['name']));
  $delegation = urlencode(stripslashes($group['delegation']));
  $user = urlencode(stripslashes($group['user']));
  $email = urlencode(stripslashes($group['email']));
  $samba = urlencode(stripslashes($group['samba']));
  $privacy = $group['privacy'];

  $url = url_prepare("group_index.php?action=search&amp;tf_name=$name&amp;tf_email=$email&amp;tf_user=$user&amp;tf_delegation=$delegation&amp;cb_samba=$samba&amp;privacy=$privacy$url_ext");
  
  $dis_group = new OBM_DISPLAY('DATA', $prefs, 'group');
  if ($popup) {
    if (empty($ext_widget) && empty($ext_widget_text)) {
      $dis_group->display_link = false;
      $dis_group->data_cb_text = 'X';
      $dis_group->data_idfield = 'group_id';
      $dis_group->data_cb_name = 'data-group-';
    }

    $dis_group->data_form_end = "
    <div class=\"buttons\">
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"group_id\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
    </div>
    </form>";

    if ($ext_element != '') {
      $dis_group->data_cb_name = '';
      $dis_group->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this); return false;\" > ";
    } elseif ($ext_widget != '') {
      $dis_group->display_ext = 'get_id';
      $dis_group->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
      $dis_group->data_form_end = '';
    } else {
      $dis_group->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }

    $display_popup_end = "
    <a href=\"\" onclick='window.close();'>$l_close</a>";
  }

  $dis_group->data_set = $obm_q;
  $dis_group->data_url = $url;
  $dis_group->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_group->display('dis_data_group');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group Consultation
// Parameters:
//   - $group[] : group parameters
//   - $uid     : user id
///////////////////////////////////////////////////////////////////////////////
function dis_group_consult($group, $uid) {
  global $ctu_sql_limit, $l_err_reference;

  $g_id = $group['group_id'];

  if ($g_id > 0) {
    $group_q = run_query_group_detail($g_id);
    $gid = $group_q->f('group_gid');
    $prefs_u = get_display_pref($uid, 'group_user');
    $u_q = run_query_group_user_group($group, $gid);
    if ($ctu_sql_limit) {
      $join = "LEFT JOIN UserObm ON userobm_id=userobmgroup_userobm_id";
      $where_user_not_archive = "AND userobm_archive != 1";
      $query = "SELECT count(*) FROM UserObmGroup $join
      	WHERE userobmgroup_group_id=#INT($g_id) $where_user_not_archive";
      $count = get_query_count($query);
      $u_q->set_num_rows_total($count);
    }

    $prefs_g = get_display_pref($uid, 'group_group');
    $g_q = run_query_group_group($group);
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(*) FROM GroupGroup WHERE groupgroup_parent_id=#INT($g_id)");
      $g_q->set_num_rows_total($count);
    }

    $prefs_c = get_display_pref($uid, 'group_contact');
    $c_q = run_query_group_contact($group);
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(*) FROM contactgroup WHERE group_id=$g_id");
      $c_q->set_num_rows_total($count);
    }


    $block = html_group_consult($group_q, $prefs_u, $u_q, $prefs_g, $g_q, $prefs_c, $c_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Group Consultation
// Parameters:
//   - $group_q  : group database result 
//   - $prefs_u  : group user preference display
//   - $u_q      : user database result 
//   - $prefs_g  : group group preference display
//   - $g_q      : group database result 
///////////////////////////////////////////////////////////////////////////////
function html_group_consult($group_q, $prefs_u, $u_q, $prefs_g, $g_q, $prefs_c, $c_q) {
  global $l_yes, $l_no, $ico_mail, $l_undef, $c_undef;
  global $l_group, $l_name, $l_desc, $l_email, $l_no_user, $l_no_group_group;
  global $l_system, $l_local, $l_ext_id, $l_smb, $l_no,$l_yes;
  global $l_visibility, $l_private, $l_public, $l_manager, $l_gid;
  global $l_del_user_sel, $l_user_member, $l_del_group_sel, $l_group_member;
  global $l_del_contact_sel;
  global $obm, $display, $module, $action, $cright_write;
  global $cg_gid_smb_user, $cg_gid_smb_admin, $path;
  global $cgp_use, $cgp_hide,$l_contacts, $l_no_contact, $l_contact_member;

  $id = $group_q->f('group_id');
  $usercreate = $group_q->f('group_usercreate');
  $userupdate = $group_q->f('group_userupdate');
  $timecreate = $group_q->f('timecreate');
  $timeupdate = $group_q->f('timeupdate');
  $system = ($group_q->f('group_system') == 1 ? $l_yes : $l_no);
  $local = ($group_q->f('group_local') == 1 ? $l_yes : $l_no);
  $gid = $group_q->f('group_gid');
  $privacy = $group_q->f('group_privacy');
  $dis_privacy = ($privacy == 1 ? $l_private : $l_public);
  $ext_id = $group_q->f('group_ext_id');
  $name = $group_q->f('group_name');
  $delegation = $group_q->f('group_delegation');
  $desc = $group_q->f('group_desc');
  $email = $group_q->f('group_email');
  $manager = $group_q->f('userobm_lastname').' '.$group_q->f('userobm_firstname');
  
  // Check if domain is a samba domain
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  $dis_email = get_entity_email($email);

  if ($privacy == 0) {
    $dis_email = "<tr>
      <th>$l_email</th>
      <td>$dis_email</td>
      </tr>";

    if (($cgp_use['service']['samba']) && ($samba_domain)) {
      $samba = ($group_q->f('group_samba') == 1 ? $l_yes : $l_no);
      $block_samba = "
      <tr>
        <th>$l_smb</td>
        <td>$samba</td>
      </tr>";
    }    
  } else {
    $dis_email = '';
    $samba = '';
    $dis_group = '';
  }

  $block_userdata .= of_userdata_dis_entity_consult('group', $id);
  $block_delegation = of_delegation_dis_block_consult($delegation);

  // Check update rights to allow to remove users
  $g = array('group_id' => $id, 'privacy' => $privacy, 'gid' => $gid); 
  $update_group = check_group_update_rights($g);

  // Display members groups
  $nb_g = $g_q->num_rows_total();
  if ($nb_g == 0) {
    $message = $l_no_group_group;
  } else {
    $message = "$nb_g $l_group_member";
  }

  //  $block_mg = display_info_msg($message);
  $block_mg_head = "<h1>$message</h1>";

  $url_mg = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($GLOBALS['l_add_group'])."&amp;ext_action=group_add&amp;ext_url=".urlencode($path.'/group/group_index.php')."&amp;ext_id=".$id."&amp;ext_target=$l_group&amp;child_res=1";
  $url_mu = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($GLOBALS['l_add_user'])."&amp;ext_action=user_add&amp;ext_url=".urlencode($path.'/group/group_index.php')."&amp;ext_id=".$id."&amp;ext_target=$l_group";
  $url_mc = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($GLOBALS['l_add_user'])."&amp;ext_action=user_add&amp;ext_url=".urlencode($path.'/group/group_index.php')."&amp;ext_id=".$id."&amp;ext_target=$l_group";

  if ($update_group) {
    $js .= "
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_groupadd', 'sel_group', {mode:'mono'});";
    $block_mg .= "
    <form method='POST' action='group_index.php'>
      <input  type=\"text\" name=\"group_add\" value=\"\" onfocus=\"\" onblur=\"\" id=\"sel_group\" autocomplete=\"off\" />
      <input type=\"hidden\" name=\"groups_add\" value=\"\" onfocus=\"\" onblur=\"\" id=\"sel_groupadd\" autocomplete=\"off\" />
      <input type='submit' value='$GLOBALS[l_add]' />
      <a href=\"javascript: return false;\" onclick=\"popup('$url_mg','');return false;\">
      <img src=\"$GLOBALS[ico_add]\" alt=\"[Add]\" />
      </a>
      <input name='action' type='hidden' value='group_add' /> 
      <input name='group_id' type='hidden' value='$id' /> 
    </form>              
    <form method=\"get\" action=\"group_index.php\">";
  }

  
  if ($nb_g != 0) {
    $url = url_prepare("group_index.php?action=detailconsult&amp;group_id=$id");
    
    $dis_infos = new OBM_DISPLAY('DATA', $prefs_g, 'group', 'group_group','group_group');
    $dis_infos->data_set = $g_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = 'top';

    if ($update_group) {
      $dis_infos->data_cb_text = 'X';
      $dis_infos->data_idfield = 'child_id';
      $dis_infos->data_cb_name = 'data-group-';
      $dis_infos->data_cb_field = '';
    }

    $block_mg .= $dis_infos->display('dis_data_group');

    if ($update_group) {
      $block_mg .= "  
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_del_group_sel\" /> 
        <input type=\"hidden\" name=\"group_id\" value=\"$id\" /> 
        <input type=\"hidden\" name=\"action\" value=\"group_del\" /> 
      </div>
      </form>";
    }
  }

  // registered Users display
  $nb_u = $u_q->num_rows_total();
  if ($nb_u == 0) {
    $message = $l_no_user;
  } else {
    $message = "$nb_u $l_user_member";
  }

  //  $block_mu .= display_info_msg($message);
  $block_mu_head = "<h1>$message</h1>";

  if ($update_group) {
    $block_mu .= "
    <form method='POST' action='group_index.php'>
      <input type=\"text\" name=\"user_add\" value=\"\" onfocus=\"\" onblur=\"\" id=\"sel_user\" autocomplete=\"off\" />
      <input type=\"hidden\" name=\"users_add\" value=\"\" onfocus=\"\" onblur=\"\" id=\"sel_useradd\" autocomplete=\"off\" />
      <input type='submit' value='$GLOBALS[l_add]' />
      <a href=\"javascript: return false;\" onclick=\"popup('$url_mu','');return false;\">
      <img src=\"$GLOBALS[ico_add]\" alt=\"[Add]\" />
      </a>  
      <input name='action' type='hidden' value='user_add' /> 
      <input name='group_id' type='hidden' value='$id' /> 
    </form>          
    <form method=\"get\" action=\"group_index.php\">";
    $js .= "
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_useradd', 'sel_user', {mode:'mono'});";
  }

  if ($nb_u != 0) {
    $url = url_prepare("group_index.php?action=detailconsult&amp;group_id=$id");
    $dis_infos = new OBM_DISPLAY('DATA', $prefs_u, 'group', 'group_user','group_user');
    $dis_infos->data_set = $u_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = 'both';
    if ($update_group) {
      $dis_infos->data_cb_text = 'X';
      $dis_infos->data_idfield = 'group_user_id';
      $dis_infos->data_cb_name = 'data-user-';
      $dis_infos->data_cb_field = '';
      // If the group is the default user group, do not display the checkbox
      $dis_infos->data_cb_show = 'gid_diff';
    }

    $block_mu .= $dis_infos->display('dis_data_group');

    if ($update_group) {
      $block_mu .= "      
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_del_user_sel\" />
        <input type=\"hidden\" name=\"group_id\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"user_del\" />
      </div>
      </form>";
    }
  }

  // registered Contact display
  $nb_c = $c_q->num_rows_total();
  if ($nb_c == 0) {
    $message = $l_no_contact;
  } else {
    $message = "$nb_c $l_contact_member";
  }

  if($privacy == 0) {
    $block_mc_head = "<h1>$message</h1>";

    if ($update_group) {
      // If the group is the default user group, do not display the checkbox
      $block_mc .= "
      <form method='POST' action='group_index.php'>
        <input type=\"text\" name=\"contact_add\" value=\"\" onfocus=\"\" onblur=\"\" id=\"sel_contact\" autocomplete=\"off\" />
        <input type=\"hidden\" name=\"contacts_add\" value=\"\" onfocus=\"\" onblur=\"\" id=\"sel_contactadd\" autocomplete=\"off\" />
        <input type='submit' value='$GLOBALS[l_add]' />
        <input name='action' type='hidden' value='contact_add' /> 
        <input name='group_id' type='hidden' value='$id' /> 
      </form>          
      <form method=\"get\" action=\"group_index.php\">";
      $addressbooks = OBM_Addressbook::search();
      $addressbook = $addressbooks->getPublicAddressbook();
      $js .= "
        new obm.AutoComplete.Search('$path/contact/contact_index.php?addressbook_id=".$addressbook->id."&action=ext_search', 'sel_contactadd', 'sel_contact', {mode:'mono', strict:false});";
    }

    if ($nb_c != 0) {
      $url = url_prepare("group_index.php?action=detailconsult&amp;group_id=$id");
      $dis_infos = new OBM_DISPLAY('DATA', $prefs_c, 'group', 'group_contact','group_contact');
      $dis_infos->data_set = $c_q;
      $dis_infos->data_url = $url;
      $dis_infos->data_header = 'both';      
      if ($update_group) {
        $dis_infos->data_cb_text = 'X';
        $dis_infos->data_idfield = 'group_contact_id';
        $dis_infos->data_cb_name = 'data-contact-';
        $dis_infos->data_cb_field = '';
      }      
      $block_mc .= $dis_infos->display('dis_data_group');
      if ($update_group) {
        $block_mc .= "      
        <div class=\"buttons\">
          <input type=\"submit\" value=\"$l_del_contact_sel\" />
          <input type=\"hidden\" name=\"group_id\" value=\"$id\" />
          <input type=\"hidden\" name=\"action\" value=\"contact_del\" />
        </div>
        </form>";
      }
    }
  }

  $display['detailInfo'] = display_record_info($group_q);
  $display['title'] = $name;
  $block .= "
  <div class=\"detail infos\" style=\"overflow:hidden\">
    <h1>$l_group</h1>

    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr>
    <tr>
      <th>$l_gid</th>
      <td>$gid</td>
    </tr>
    $block_delegation
    <tr>
      <th>$l_manager</th>
      <td>$manager</td>
    </tr>
    <tr>
      <th>$l_system</th>
      <td>$system</td>
    </tr>
    <tr>
      <th>$l_local</th>
      <td>$local</td>
    </tr>
    <tr>
      <th>$l_ext_id</th>
      <td>$ext_id</td>
    </tr>
    $block_samba
    <tr>
      <th>$l_visibility</th>
      <td>$dis_privacy</td>
    </tr>
    $dis_email
    </table>
    </div>
  
  $block_userdata
  <div class=\"detail infos\">
    <h1>$l_desc</h1>
   
    <table>
    <tr>
      <th>$l_desc</th>
      <td>$desc</td>
    </tr>
    </table>
  </div>
  <div class=\"detail extra\">
  $block_mg_head
  $block_mg
  </div>
  <div class=\"detail extra\">
  $block_mu_head
  $block_mu
  </div>
  <div class=\"detail extra\">
  $block_mc_head
  $block_mc
  </div>
  <script type=\"text/javascript\">
      obm.initialize.chain(function () {
        $js
      });
    </script>  
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group Form
// Parameters :
//   - $action   : action called
//   - $group_q  : DBO : information about the group (null for new group)
//   - $group[]  : default or transmitted values
//   - $field    : [optionnal] : field in error
///////////////////////////////////////////////////////////////////////////////
function html_group_form($action, $group_q, $group, $field='') {
  global $obm, $perm, $cgroup_private_default, $cgp_use, $cright_write_admin;
  global $display, $l_group, $l_name, $l_desc, $l_smb, $l_email;
  global $l_private, $l_public, $l_insert, $l_update, $l_back, $l_no,$l_yes;
  global $l_manager, $c_undef, $l_undef, $l_header_new, $l_contacts;

  // Check if domain is a samba domain
  $samba_domain = false;
  if ($cgp_use['service']['samba']) {
    $props = of_entity_get_service_property('domain', $obm['domain_id']);
    if (is_array($props['samba']) && (count($props['samba']) > 0)) {
      $samba_domain = true;
    }
  }

  // if update mode and first time, values are taken from db
  if ($action == 'detailupdate') {
    $id = $group_q->f('group_id');
    $usercreate = $group_q->f('group_usercreate');
    $name = $group_q->f('group_name');
    $delegation = $group_q->f('group_delegation');
    $system = $group_q->f('group_system');
    $samba = $group_q->f('group_samba');
    $desc = $group_q->f('group_desc');
    $email = $group_q->f('group_email');
    $priv = $group_q->f('privacy');
    $dis_title = $name;
  } elseif ($action == 'new') {
    if (!isset($group['privacy']) && $cgroup_private_default) {
      $private_c = ' checked';
      $mail_class = 'H';
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($group['group_id'])) { $id = $group['group_id']; }
  if (isset($group['name'])) { $name = stripslashes($group['name']); }
  if (isset($group['delegation'])) { $delegation = stripslashes($group['delegation']); }
  if (isset($group['desc'])) { $desc = stripslashes($group['desc']); }
  if (isset($group['email'])) { $email = stripslashes($group['email']); }
  if (isset($group['privacy'])) { $priv = stripslashes($group['privacy']); }

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  $block_userdata = of_userdata_dis_entity_form('group', $id, $group);

  $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);

  // Privacy can not be updated !
  if ($action == 'new' || $action == 'insert') {
    if (! $perm->check_right('group', $cright_write_admin)) {
      $dis_private = "<label><input name=\"cba_privacy\" type=\"hidden\" value=\"1\" />$l_private</label>";

    } else {
      if ($priv == '1') {
        $private_c = ' checked';
        $mail_class = 'H';
      }

      $dis_private = "
    <script type=\"text/javascript\">
      function privacyShowHide(input) {
        input = $(input);
        if($(input).get('inputValue')) {
          if($('emailGroup')) {
            $('emailGroup').addClass('H');
            $('groupMailHome').set('html','');
            add_email_field('groupMailHome', 'tf_email', aliasSelectTemplate); 
            show_hide_add_button();
          }
          if($('sambaGroup')) {
            $('sambaGroup').addClass('H');
            input.form.cb_samba.checked = false;
          }
        } else {
          if($('emailGroup')) {
            $('emailGroup').removeClass('H')
          }
          if($('sambaGroup')) {
            $('sambaGroup').removeClass('H');
          }
        }
      }
    </script>
    <input id=\"cba_privacy\" name=\"cba_privacy\" type=\"checkbox\" 
    onclick=\"privacyShowHide(this)\" value=\"1\" $private_c />";
    }
  } else {
    $dis_private = ($priv == '1' ? $l_yes : $l_no);
  }


  if( ($action == 'new' || $action == 'insert') || !$priv ) {
    if (($cgp_use['service']['samba']) && ($samba_domain)) {
      // Samba group checkbox checked value
      $samba_state  = ($samba ? 'checked' : '');
  
      $block_samba = "
    <tr id=\"sambaGroup\" class=\"$mail_class\">
      <th class=\"$class[samba]\"><label for=\"cb_samba\">$l_smb</label></th>
      <td><input id=\"cb_samba\" name=\"cb_samba\" type=\"checkbox\" value=\"1\" $samba_state></td>
    </tr>";
    }
   if ($cgp_use['service']['mail']) {
     $dis_mail = dis_group_mail_field($email,$class); 
   } else {
     $dis_mail = "<th>$l_email</th>
      <td><input name=\"tf_email\" maxlength=\"64\" size=\"32\" value=\"$email\" /></td>";

   }
    $block_email = "
    <tr id=\"emailGroup\" class=\"$mail_class\">
      $dis_mail
    </tr>";
  }


  if ($action == 'detailupdate' || $action == 'update') {
    // Manager select
    $manager_id = $group_q->f('group_manager_id');
    if (isset($group['manager'])) { $manager_id = $group['manager']; }    
    $users = of_usergroup_get_group_users($id, true);
    $sel_manager = "<select name=\"sel_manager\" id=\"sel_manager\">
        <option value=\"$c_undef\">$l_undef</option>";
    foreach ($users as $u_id => $one_user) {
      $u_name = $one_user['lastname'].' '.$one_user['firstname'];
      if ($u_id == $manager_id) {
        $selected = 'selected';
      } else {
        $selected = '';
      }
      $sel_manager .= "<option value=\"$u_id\" $selected> $u_name </option>";
    }
    $sel_manager .= '</select>';

    $block_manager = "  <tr>
    <th>$l_manager</th>
      <td>$sel_manager</td>
    </tr>";

    $dis_private = "<input name=\"cba_privacy\" type=\"hidden\" value=\"".$priv."\" />".$dis_private;
  }

  $display['title'] = $dis_title;

  $block = "
  <form method=\"get\" name=\"f_entity\" action=\"".url_prepare('group_index.php')."\">
<fieldset class=\"detail infos\">
  <legend>$l_group</legend>

  <table>
  <tr>
    <th class=\"$class[name]\">$l_name</th>
    <td><input name=\"tf_name\" maxlength=\"256\" size=\"32\" value=\"$name\" /></td>
  </tr>
  $block_manager
  $block_delegation
  <tr>
    <th><label for=\"cba_privacy\">$l_private</label></th>
    <td>$dis_private</td>
  </tr>
  $block_samba
  $block_email
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_desc</legend>

  <table>
  <tr>
    <th>$l_desc</th>
    <td><input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"70\" /></td>
  </tr>
  $block_userdata
  </table>
  </fieldset>
  ";

  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_but = "
      <input type=\"hidden\" name=\"group_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<fieldset class=\"buttons\">
    $dis_but
    </fieldset>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the group delete validation screen
// Parameters:
//   - $p_id : group id
///////////////////////////////////////////////////////////////////////////////
function dis_group_can_delete($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare('group_index.php');

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"group_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"group_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
     $dis_delete
     $dis_back
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a group insertion or update
// When similar groups exist we show these and ask confirmation
// Parameters:
//   - $id       : group id
//   - $g_q      : group database result (at least 1 row)
//   - $group[]  : values for insertion/update (if confirmation)
//     keys used : name, desc
/////////////////////////////////////////////////////////////////////////////
function dis_group_warn_insert($id, $g_q, $group) {
  global $display, $l_check_samegroup, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $group['name'];
  $delegation = $group['delegation'];
  $desc = $group['desc'];
  $samba = $group['samba'];
  $email = $group['email'];
  $privacy = $group['privacy'];

  $display['msg'] .= display_warn_msg($l_check_samegroup);
  while ($g_q->next_record()) {
    $id = $g_q->f('group_id');
    $samename = $g_q->f('group_name');
    $samedesc = $g_q->f('group_desc');
    $dis_same_group .= "
      <li>
        <a href=\"" .url_prepare("group_index.php?action=detailconsult&amp;group_id=$id") . "\">$samename ($samedesc)</a>
      </li>";
  }

  $hidden_fields = "
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"tf_delegation\" value=\"$delegation\" />
    <input type=\"hidden\" name=\"tf_desc\" value=\"".stripslashes($desc)."\" />
    <input type=\"hidden\" name=\"cba_samba\" value=\"$samba\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"cba_privacy\" value=\"$privacy\" />
";

  $block = "
  <div class=\"detail\">
    <ul>
    $dis_same_group
    </ul> 
  </div>
  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" .url_prepare('group_index.php') . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    $hidden_fields
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    <form name=\"form_back\" method=\"get\"
    action=\"" .url_prepare('group_index.php') . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    $hidden_fields
    <input type=\"submit\" value=\"$l_back\" />
    </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the group administration index
///////////////////////////////////////////////////////////////////////////////
function dis_group_admin_index() {

  $block = of_userdata_dis_admin_form('group');

  return $block;
}


/**
 * Display the group csv import index
 **/
function dis_group_import_index() {
  global $display, $l_group, $l_header_import;
  global $l_name, $l_email, $l_file_format, $l_user_member, $l_login;
  global $l_delegation, $l_smb;

  $block = "
  <div class=\"detail extra\">
    <h1>$l_file_format</h1>

  <table class=\"spreadSheet\">
  <thead>
  <tr>
    <th>$l_name</th>
    <th>$l_delegation</th>
    <th>$l_email</th>
    <th>$l_smb</th>
    <th>$l_user_member ($l_login)</th>
  </tr>
  </thead>
  <tr>
    <td>CO Coteaux</td>
    <td>France</td>
    <td>coc.football</td>
    <td>X</td>
    <td>pierre.baudracco,mehdi.rande,anthony.prades</td>
  </tr>
  </table>
  </div>";

  $block .= html_group_import_file();

  return $block;
}


/**
 * HTML Display Import Consultation File block
 **/
function html_group_import_file() {
  global $l_group, $l_header_import, $l_file, $l_import_file;
  global $display;

  $url = url_prepare('group_index.php');

  $block = "
    <form method=\"post\" enctype=\"multipart/form-data\" name=\"f_file\" action=\"$url\">
      <input type=\"hidden\" name=\"action\" value=\"import_file\" />
    <table>
    <tr>
      <th>$l_import_file</th>
      <td>
        <input type=\"file\" name=\"fi_file\" id=\"fi_file\" size=\"30\" value=\"\" />
        <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"300000\" />
        <input type=\"submit\" name=\"submit\" value=\"$l_import_file\" /></td>
    </tr>
    </table>
    </form>
    </div>";

  return $block;
}


/**
 * Process Import file
 **/
function dis_group_import_file_run($params) {
  global $obm, $display, $err, $ccscd_tab;
  global $l_group, $l_name, $l_email, $l_header_import, $l_delegation, $l_smb;

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

  $c_name = $cpt++;
  $c_delegation = $cpt++;
  $c_email = $cpt++;
  $c_samba = $cpt++;
  $c_users_login = $cpt++;

  $nb_col = $cpt;

  $dis_import .= "
  <thead>
  <tr>
    <th>$l_name</th>
    <th>$l_delegation</th>
    <th>$l_email</th>
    <th>$l_smb</th>
    <th></th>
  </tr>
  </thead>";

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('group');
  $num_line = 0;
  $fh = fopen($file, 'r');
  while ( ($row = fgetcsv($fh, 4000, $csv_sep)) ) {
    $num_line++;

    $group['name'] = addslashes(trim($row[$c_name]));
    $group['delegation'] = addslashes(trim($row[$c_delegation]));
    $group['email'] = addslashes(trim($row[$c_email]));
    $group['users_login'] = addslashes(trim($row[$c_users_login]));
    if (in_array(trim($row[$c_samba]), $array_true)) {
      $group['samba'] = 1;
    } else {
      $group['samba'] = 0;
    }

    // Test if group name or email exists
    $query = "SELECT group_id, group_name, group_delegation, group_email
    FROM UGroup
    WHERE (group_name='$group[name]' OR group_email='$group[email]')
      AND (group_privacy=0 OR group_usercreate='$obm[uid]')
      $multidomain";

    display_debug_msg($query, $cdg_sql, 'run_group_import_file()');
    $obm_q->query($query);

    if ($obm_q->num_rows() > 0) {
      // Get the existing group
      $obm_q->next_record();
      $group['id'] = $obm_q->f('group_id');
      $group['name'] = $obm_q->f('group_name');
      $res = "=$group[name] : ";
    } else {
      // Insert the group
      if (check_group_data_form($group)) {
	$group['id'] = run_query_group_insert($group);
	$res = "+$group[name] : ";
      } else {
	// Group not ok, stop for this row
	$group['id'] = false;
	$res = "! $err[field] - $err[msg]";
      }
    }

    // Insert group user membership
    if ($group['id'] > 0) {
      $coma = '';
      $login = strtok($group['users_login'],',');
      while($login) {
	$user_id = get_user_id($login);
	if ($user_id > 0) {
	  // Add user membership if not already there
	  $query = "SELECT count(*) as nb
            FROM UserObmGroup
            WHERE userobmgroup_group_id='$group[id]'
              AND userobmgroup_userobm_id=$user_id";
	  display_debug_msg($query, $cdg_sql, 'run_group_import_file(2)');
	  $obm_q->query($query);
	  $obm_q->next_record();
	  if ($obm_q->f('nb') == '0') {
	    $query = "INSERT INTO UserObmGroup (
              userobmgroup_group_id,
              userobmgroup_userobm_id)
            VALUES ($group[id], $user_id)";
	    display_debug_msg($query, $cdg_sql, 'run_group_import_file(3)');
	    $obm_q->query($query);
	    $res .= "$coma$login";
	  } else {
	    $res .= "$coma(=$login)";
	  }
	  $coma = ',';
	}
	$login = strtok(',');
      }
    }

    $dis_samba = '';
    if ($group['samba'] == 1) {
      $dis_samba = 'X';
    }
    $dis_import .= "
    <tr>
      <td>$group[name]</td>
      <td>$group[delegation]</td>
      <td>$group[email]</td>
      <td>$dis_samba</td>
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
// Display the Group Display preference screen
// Parameters:
//   - $prefs_g : Group Display preferences
//   - $prefs_u : User Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_group_display_pref($prefs_g, $prefs_u) {
  global $l_group_display, $l_user_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs_g, 'group');
  $dis_pref->pref_title = $l_group_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  $dis_pref->display_pref = $prefs_u;
  $dis_pref->display_entity = 'group_user';
  $dis_pref->pref_title = $l_user_display;

  $block .= $dis_pref->display();

  return $block;
}


/**
 * Build a JSON array with all search results 
 * 
 * @param $group POST params
 * @param $results array of 'length' => DB query results nb, and 'datas' => DB Group search query results
 * @access public
 * @return void
 */
function json_search_group($group,$results) {
  global $display, $l_desc;

  $grp_q = $results['datas'];

  $groups = array();
  while($grp_q->next_record()) {
    $id = $grp_q->f('group_id');
    $label = phpStringToJsString($grp_q->f('group_name'));
    $extra = phpStringToJsString("$l_desc : ".$grp_q->f('group_desc'));
    $groups[] = "{id:'data-group-$id', label:'$label', extra:'$extra'}";
  }
  $display['json'] = '{length:'.$results['length'].', datas:['.implode(',',$groups).']}';
}


/**
 * Generate email field  
 * 
 * @param mixed $name 
 * @param mixed $values 
 * @access public
 * @return void
 */
function dis_group_mail_field($values,$class) {
  $d = of_domain_get_domain_infos($GLOBALS['obm']['domain_id'], true);
  if(!empty($d['alias'])) {
    $aliases = explode("\r\n",$d['alias']);
  } else {
    $aliases = array();
  }
  array_unshift($aliases,$d['name']);
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
        <a onclick=\"remove_element(this.parentNode,'groupMailHome'); show_hide_add_button();return false\" href=\"\">
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
  if ($GLOBALS['c_max_group_alias'] ==0 || $count < $GLOBALS['c_max_group_alias']) {
    $email_block .= "
      <div class=\"multiple\">
      <a onclick=\"remove_element(this.parentNode,'groupMailHome'); show_hide_add_button();return false\" href=\"\">
       <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
      </a>        
      <input name='tf_email[]' value='' /> @ $sel_alias 
      </div>";
    $count++;
  } 
  if($GLOBALS['c_max_group_alias'] ==0 || $count < $GLOBALS['c_max_group_alias']) {
    $add_button = "<a href=\'\' onclick=\"add_email_field('groupMailHome', 'tf_email',aliasSelectTemplate); show_hide_add_button();return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[Add email field]\" /></a>";
  }
  $email_block = "
  <th class=\"$class[email]\" id='groupMailLabel'>
  $GLOBALS[l_email]
  <script type='text/javascript'>
    var aliasSelectTemplate = new Element('select').setProperty('name','sel_aliases[]');
    aliasSelectTemplate.adopt(new Element('option').setProperty('value','').appendText('$GLOBALS[l_all_aliases]'));
    $sel_js
  </script>
  $add_button
  </th>
  <td id='groupMailHome'>
  $email_block
  </td>";

  return $email_block;
}


?>
