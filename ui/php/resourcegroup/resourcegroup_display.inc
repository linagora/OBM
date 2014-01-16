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
// OBM - File : resourcegroup_display.php                                    //
//     - Desc : Resource Group Display File                                  //
// 2005-08-13 Florent Goalabré                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["rgroup_name"] = $l_name;
$fieldnames["rgroup_privacy"] = $l_private;
$fieldnames["rgroup_desc"] = $l_desc;
$fieldnames["rgroup_nb_resource"] = $l_nb_resource;
$fieldnames['rgroup_delegation'] = $l_delegation;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;
// Calculated fields
$fieldnames["resourcegroup_resource_name"] = $l_name;
$fieldnames["resourcegroup_resource_desc"] = $l_desc;
$fieldnames["resourcegroup_resource_qty"] = $l_qty;

///////////////////////////////////////////////////////////////////////////////
// Display Group specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_resourcegroup(&$OD, $fieldname, $link_ok) {
  global $path, $ico_mail, $ico_web, $ico_contact_new,$params;

  if (($fieldname == "rgroup_name") && $link_ok) {
    $res["url"] = "$path/resourcegroup/resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=".$OD->data_set->f("rgroup_id");
  }

  else if ($fieldname == "rgroup_privacy") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if (($fieldname == "data_element") && $params["ext_element"] != "") {
    $res["name"] = "<span id=\"data-resourcegroup-".$OD->data_set->f("rgroup_id")."\" style=\"display:none;\">".
    $OD->data_set->f("rgroup_name")."</span>";
  } else if ($fieldname == "rgroup_nb_resource") {
    $res["align"] = "center";
  }
  // For RGroup resource lists
  elseif (($fieldname == "resourcegroup_resource_name")  && $link_ok) {
    //$res["url"] = "$path/resource/resource_index.php?action=detailconsult&amp;resource_id=".$OD->data_set->f("group_user_id");
    $res["url"] = "";
  }
  elseif (($fieldname == "resourcegroup_resource_desc")  && $link_ok) {
    $res["url"] = "";
  }
  elseif (($fieldname == "resourcegroup_resource_qty")  && $link_ok) {
    $res["url"] = "";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group search Form
// Parameters : 
//   - $group[]   : default form values
//     keys used  : name, user
///////////////////////////////////////////////////////////////////////////////
function html_resourcegroup_search_form ($group) {
  global $obm, $display, $l_name, $l_resourcegroup_resource, $l_find;
  global $l_visibility, $l_add_resourcegroup;

  $popup = $group["popup"];

  if ($group["children_restriction"]) {
    $child_res = "<input name=\"child_res\" type=\"hidden\" value=\"1\">";
  }

  if ($popup) {
    $ext_action = $group["ext_action"];
    $ext_target = $group["ext_target"];
    $ext_widget = $group["ext_widget"];
    $ext_url = $group["ext_url"];
    $ext_id = $group["ext_id"];
    $ext_title = stripslashes($group["ext_title"]);
    $ext_element =  $group["ext_element"];

    $ext = "<input name=\"ext_action\" type=\"hidden\" VALUE=\"$ext_action\">
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
      <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
      <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
      <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">      
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == "") {
      $ext_title = $l_add_resourcegroup;
    }
    $display["title"] = $ext_title;
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($group["name"]);
  $resource = stripslashes($group["resource"]);
  $privacy = $group["privacy"];
  $desc = stripslashes($group['desc']);
  $delegation = stripslashes($group['delegation']);

  $sel_privacy = dis_privacy_search_select($privacy);

  // --- HTML Page display ----------------------------------------------------

  $display_pref = get_display_pref($obm['uid'],'resourcegroup');
  $dis_delegation = of_delegation_dis_search_form($delegation);
  
  $block = "
  <form method=\"get\" name=\"f_search\" action=\"".url_prepare("resourcegroup_index.php")."\" class=\"search\">

    <label>$l_name<br />
      <input name=\"tf_name\" size=\"16\" maxlength=\"16\" value=\"$name\" />
    </label>
    <label>$l_resourcegroup_resource<br />
      <input name=\"tf_resource\" size=\"16\" maxlength=\"16\" value=\"$resource\" />
    </label>
    <label>$l_visibility<br />
      $sel_privacy
    </label>
    ";

    
  if ($display_pref['rgroup_delegation']['status'] != 0) {
    $block .= $dis_delegation;
  }

  if ($display_pref['rgroup_desc']['status'] != 0) {
    $block .= "
    <label>$GLOBALS[l_desc]<br />
      <input name=\"tf_desc\" size=\"16\" maxlength=\"256\" value=\"$desc\" />
    </label>
    ";
  }
  
  $block .= "
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $child_res
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
//     keys used  : name, description
///////////////////////////////////////////////////////////////////////////////
function dis_resourcegroup_search_resourcegroup($group) {
  global $obm, $display, $l_found, $l_no_found;

  $child_res = $group["children_restriction"];
  $widget = $group["ext_widget"];

  $prefs = get_display_pref($obm["uid"], "resourcegroup");
  if ($child_res) {
    $obm_q = run_query_resourcegroup_search_possible_children($group);
  }  else {
    $obm_q = run_query_resourcegroup_search($group);
  }

  $nb_group = $obm_q->num_rows_total();
  if ($nb_group == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_group $l_found");
    $block = html_resourcegroup_search_resourcegroup($obm_q, $prefs, $group);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Group search result
// Parameters : 
//   - $obm_q     : list of the groups to display 
//   - $pref_q    : the fields which have to be displayed
//   - $group[]   : group search criteria
//     keys used  : name, resource, popup
///////////////////////////////////////////////////////////////////////////////
function html_resourcegroup_search_resourcegroup($obm_q, $pref_q, $group) {
  global $l_add, $l_close;

  $child_res = $group["children_restriction"];
  $popup = $group["popup"];
  $widget = $group["ext_widget"];

  if ($popup) {
    $ext_action = $group["ext_action"];
    $ext_url = $group["ext_url"];
    $ext_target = $group["ext_target"];
    $ext_widget = $group["ext_widget"];
    $ext_id = $group["ext_id"];
    $ext_element = $group["ext_element"];
    $ext_title = urlencode(stripslashes($group["ext_title"]));
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_title=$ext_titled&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;child_res=$child_res";
  }

  $name = urlencode(stripslashes($group["name"]));
  $delegation = urlencode(stripslashes($group['delegation']));
  $resource = urlencode(stripslashes($group["resource"]));

  $url = url_prepare("resourcegroup_index.php?action=search&amp;tf_name=$namet&amp;tf_delegation=$delegation&amp;tf_resource=$resource$url_ext");
  
  $dis_group = new OBM_DISPLAY("DATA", $pref_q, "resourcegroup");
  if ($popup) {
    $dis_group->display_link = false;
    $dis_group->data_cb_text = "X";
    $dis_group->data_idfield = "rgroup_id";
    $dis_group->data_cb_name = "data-g-";
    if ($ext_element != "") {
      $dis_group->data_cb_name = "";
      $form_attr = "
      onsubmit=\"of_select_fill_from_checkbox(this); return false;\" ";
    }
    elseif ($widget == "") {
      $form_attr = "target=\"$ext_target\" method=\"get\" action=\"$ext_url\"";
    } else {
      $form_attr = "onsubmit=\"fill_ext_form(this);return false;\"";
      $input_target = "<input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />";
    }
    $dis_group->data_form_head = "<form $form_attr>";
    $dis_group->data_form_end = "
    <fieldset class=\"buttons\">
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"resourcegroup_id\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      $input_target
    </fieldset>
    </form>";
    $display_popup_end = "
    <p><a href=\"\" onclick='window.close();'>$l_close</a></p>";
  }

  $dis_group->data_set = $obm_q;
  $dis_group->data_url = $url;
  $dis_group->data_header = "both";

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_group->display("dis_data_resourcegroup");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group Form
// Parameters :
//   - $action    : action called
//   - $group_q   : DBO : information about the group (null for new group)
//   - $group[]   : default or transmitted values
//     keys used  : name, desc, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_resourcegroup_form($action, $group_q, $group) {
  global $l_resourcegroup, $l_name, $l_desc, $l_header_new;
  global $cgroup_private_default, $l_insert, $l_update, $l_back, $l_private;
  global $obm, $display,$l_no;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $group_q->f("rgroup_id");
    $usercreate = $group_q->f("rgroup_usercreate");
    $delegation = $group_q->f('rgroup_delegation');    
    $name = $group_q->f("rgroup_name");
    $desc = $group_q->f("rgroup_desc");
    $priv = $group_q->f("privacy");
    $dis_title = $name;
  } elseif ($action == "new") {
    if ($cgroup_private_default) {
      $private_c = " checked";
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($group["resourcegroup_id"])) { $id = $group["resourcegroup_id"]; }
  if (isset($group["name"])) { $name = stripslashes($group["name"]); }
  if (isset($group["desc"])) { $desc = stripslashes($group["desc"]); }
  if (isset($group["privacy"])) { $priv = stripslashes($group["privacy"]); }
  if (isset($group['delegation'])) { $delegation = stripslashes($group['delegation']); }

  $block_delegation = of_delegation_dis_block_update($delegation, $class['delegation']);

  // If new group or group update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action=="detailupdate") || ($action=="update")) &&
         ($usercreate == $obm["uid"]) ) ) {
    if ($priv == '1') {
      $private_c = " checked";
    }
    $dis_private = "
  <tr>
    <th><label for=\"cba_privacy\">$l_private</label></th>
    <td><input id=\"cba_privacy\" name=\"cba_privacy\" type=\"checkbox\" value=\"1\" $private_c /></td>
  </tr>";
  } else {
  $dis_private = "
  <tr>
    <th>$l_private</th>
    <td>$l_no</td>
  </tr>";
    
  } 

  $display["title"] = $dis_title;

  $block = "
  <form method=\"get\" name=\"f_entity\" action=\"".url_prepare("resourcegroup_index.php")."\">
 <fieldset class=\"detail infos\">
  <legend>$l_resourcegroup</legend>

  <table>
  <tr>
    <th>$l_name</th>
    <td><input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" /></td>
  </tr>
  $dis_private
  $block_delegation
  </table>
  </fieldset>
  
  <fieldset class=\"detail infos\">
   <legend>$l_desc</legend>
   <table>
    <tr>
    <th>$l_desc</th>
    <td><input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"70\" /></td>
  </tr>
  </table>
  </fieldset>";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_but = "
      <input type=\"hidden\" name=\"resourcegroup_id\" value=\"$id\" />
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
// Display Group Consultation
// Parameters:
//   - $group[] : group parameters
//   - $uid     : user id
///////////////////////////////////////////////////////////////////////////////
function dis_resourcegroup_consult($group, $uid) {
  global $c_all, $cdg_sql, $ctu_sql_limit, $l_err_reference;

  $g_id = $group["resourcegroup_id"];

  if ($g_id > 0) {
    $group_q = run_query_resourcegroup_detail($g_id);

    $prefs_r = get_display_pref($uid, "resourcegroup_resource");
    $r_q = run_query_resource_resourcegroup($group);
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(*) FROM ResourceGroup WHERE resourcegroup_rgroup_id=$g_id");
      $r_q->set_num_rows_total($count);
    }

    $block = html_resourcegroup_consult($group_q, $prefs_r, $r_q);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Group Consultation
// Parameters:
//   - $group_q  : group database result 
//   - $prefs_r  : group resource preference display 
//   - $r_q      : user database result 
///////////////////////////////////////////////////////////////////////////////
function html_resourcegroup_consult($group_q, $prefs_r, $r_q) {
  global $display, $perm, $module, $action, $cright_write;
  global $l_yes, $l_no, $ico_mail;
  global $l_resourcegroup, $l_name, $l_desc, $l_visibility, $l_no_user, $l_no_resourcegroup_resourcegroup;
  global $l_system, $l_local, $l_ext_id;
  global $l_del_resource_sel, $l_resource_member, $l_del_resourcegroup_sel, $l_resourcegroup_member;

  $id = $group_q->f("rgroup_id");
  $usercreate = $group_q->f("rgroup_usercreate");
  $userupdate = $group_q->f("rgroup_userupdate");
  $timecreate = $group_q->f("timecreate");
  $timeupdate = $group_q->f("timeupdate");
  $name = $group_q->f("rgroup_name");
  $delegation = $group_q->f('rgroup_delegation');
  $desc = $group_q->f("rgroup_desc");

  $display["detailInfo"] = display_record_info($group_q);
  $display["title"] = $name;
  $block_delegation = of_delegation_dis_block_consult($delegation);

  $block = "
  <div class=\"detail extra\">
    <h1>$l_resourcegroup</h1>

    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
    </tr>
    $block_delegation
    </table>
  </div>
  <div class=\"detail extra\">
    <h1>$l_desc</h1>
    <table>
    <tr>
      <th>$l_desc</th>
      <td>$desc</td>
    </tr>
    </table>
    </div>";

  // registered Resources display
  $nb_r = $r_q->num_rows();
  if ($nb_r == 0) {
    $message = $l_no_resource;
  } else {
    $message = "$nb_r $l_resource_member";
  }

  $block .= display_info_msg($message);

  if ($nb_r != 0) {
    $url = url_prepare("resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=$id");
    $dis_infos = new OBM_DISPLAY("DATA", $prefs_r, "resourcegroup", "resourcegroup_resource");
    $dis_infos->data_set = $r_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($perm->check_right($module, $cright_write)) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "resourcegroup_resource_id";
      $dis_infos->data_cb_name = "data-r-";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"resourcegroup_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_resourcegroup");

    if ($perm->check_right($module, $cright_write)) {
      $block .= "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_del_resource_sel\" />
        <input type=\"hidden\" name=\"resourcegroup_id\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"resource_del\" />
      
      </div>
      </form>";
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Group Display preference screen
// Parameters:
//   - $pref_group_q : DBO : Group Field group to display
//   - $pref_r_q     : DBO : Resource Field group to display
///////////////////////////////////////////////////////////////////////////////
function dis_resourcegroup_display_pref($pref_group_q, $pref_r_q) {
  global $l_resourcegroup_display, $l_resource_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_group_q, "resourcegroup"); 
  $dis_pref->pref_title = $l_resourcegroup_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  $dis_pref->display_pref = $pref_r_q;
  $dis_pref->display_entity = "resourcegroup_resource";
  $dis_pref->pref_title = $l_resource_display;

  $block .= $dis_pref->display();


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a group deletion
// We ask confirmation or cancel
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function dis_resourcegroup_warn_delete($id) {
  global $display, $l_warn_delete, $l_delete, $l_back, $l_cant_delete_system;
  global $obm, $l_cant_delete_public;

  $lgroup = get_resourcegroup_info($id);
  if (($lgroup["privacy"] == "0") && ($lgroup["usercreate"] != $obm["uid"])) {
    $display["msg"] .= display_warn_msg($l_cant_delete_public);
    $dis_delete = "";
  } else {
    $nb_r = get_resourcegroup_nb_resource($id);
    $display["msg"] .= display_warn_msg("$nb_r $l_warn_delete");

    $dis_delete = "
      <form method=\"get\" name=\"form_delete\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"hd_resourcegroup_id\" value=\"$id\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
      </form>
    ";
  }

  $dis_back = "
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"resourcegroup_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    ";

  $block = "
  <div class=\"buttons\">
    $dis_delete
    $dis_back
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a group insertion or update                     //
// When similar groups exist we show these and ask confirmation
// Parameters:
//   - $id       : group id
//   - $g_q      : group database result (at least 1 row)
//   - $group[]  : values for insertion/update (if confirmation)
//     keys used : name, desc
/////////////////////////////////////////////////////////////////////////////
function dis_resourcegroup_warn_insert($id, $g_q, $group) {
  global $display, $l_check_sameresourcegroup, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $delegation = $group['delegation'];
  $name = $group["name"];
  $desc = $group["desc"];

  $display["msg"] .= display_warn_msg($l_check_samegroup);
  while ($g_q->next_record()) {
    $id = $g_q->f("rgroup_id");
    $samename = $g_q->f("rgroup_name");
    $samedesc = $g_q->f("rgroup_desc");
    $dis_same_group .= "
      <tr><th>
        <a href=\"" .url_prepare("resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=$id") . "\">$samename ($samedesc)</a>
      </th></tr>";
  }

  $block = "
  <table>
    $dis_same_group
  </table>
  <div class=\"buttons\">
      <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"hidden\" name=\"tf_delegation\" value=\"$delegation\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      </form>
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
  </div>";

  return $block;
}


/**
 * Build a JSON array with all search results 
 * 
 * @param $results array of 'length' => DB query results nb, and 'datas' => DB ResourceGroup search query results
 * @access public
 * @return void
 */
function json_search_resourcegroups($rgroups,$results) {
  global $display, $l_desc;

  $rgroup_q = $results['datas'];

  $limit = $rgroups['limit'];
  $form = $rgroups['form'];

  $resourcegroups = array();
  if(isset($rgroups["first_row"])) $rgroup_q->seek($rgroups["first_row"] - 1);
  while($rgroup_q->next_record() && count($users) < $limit ) {
    $id = $rgroup_q->f("rgroup_id");
    $label = phpStringToJsString($rgroup_q->f("rgroup_name"));
    $extra = phpStringToJsString("$l_desc : ".$rgroup_q->f("rgroup_desc"));
    $resourcegroups[] = "{id:'data-resourcegroup-$id', label:'$label', extra:'$extra'}";
  }
  $display["json"] = "{length:".$results['length'].", datas:[".implode(",",$resourcegroups)."]}";
}

?>
