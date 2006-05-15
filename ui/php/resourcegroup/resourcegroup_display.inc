<?php
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
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new,$ext_element;

  if (($fieldname == "rgroup_name") && $link_ok) {
    $res["url"] = "$path/resourcegroup/resourcegroup_index.php?action=detailconsult&amp;param_resourcegroup=".$OD->data_set->f("rgroup_id");
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

  else if (($fieldname == "data_element") && $ext_element != "") {
    $res["name"] = "<span id=\"data-resurcegroup-".$OD->data_set->f("rgroup_id")."\" style=\"display:none;\">".
    $OD->data_set->f("rgroup_name")."</span>";
  } else if ($fieldname == "rgroup_nb_resource") {
    $nb_q = new DB_OBM;
    $id = $OD->data_set->f("rgroup_id");
    $query = "select count(*) from ResourceGroup where resourcegroup_rgroup_id='$id'";
    $nb_q->query($query);
    $nb_q->next_record();
    $res["name"] = $nb_q->f(0);
    $res["align"] = "center";
  }
  // For RGroup resource lists
  elseif (($fieldname == "resourcegroup_resource_name")  && $link_ok) {
    //$res["url"] = "$path/resource/resource_index.php?action=detailconsult&amp;param_resource=".$OD->data_set->f("group_user_id");
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
  global $display, $l_name, $l_resourcegroup_resource, $l_email, $l_find;
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
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($group["name"]);
  $resource = stripslashes($group["resource"]);
  $privacy = $group["privacy"];

  $sel_privacy = dis_privacy_search_select($privacy);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" action=\"".url_prepare("resourcegroup_index.php")."\">
  <div class=\"search\">

    <div class=\"searchLabel\">$l_name<br />
      <input name=\"tf_name\" size=\"16\" maxlength=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_resourcegroup_resource<br />
      <input name=\"tf_resource\" size=\"16\" maxlength=\"16\" value=\"$resource\" />
    </div>
    <div class=\"searchLabel\">$l_visibility<br />
      $sel_privacy
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $child_res
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
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
  global $display, $l_found, $l_no_found, $auth;

  $child_res = $group["children_restriction"];
  $widget = $group["ext_widget"];

  $prefs = get_display_pref($auth->auth["uid"], "resourcegroup");
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
  $resource = urlencode(stripslashes($group["resource"]));

  $url = url_prepare("resourcegroup_index.php?action=search&amp;tf_name=$namet&amp;tf_resource=$resource$url_ext");
  
  $dis_group = new OBM_DISPLAY("DATA", $pref_q, "resourcegroup");
  if ($popup) {
    $dis_group->display_link = false;
    $dis_group->data_cb_text = "X";
    $dis_group->data_idfield = "rgroup_id";
    $dis_group->data_cb_name = "cb_g";
    if ($ext_element != "") {
      $dis_group->data_cb_name = "";
      $form_attr = "
      onsubmit=\"of_extmod_fill_ext_element(this); return false;\" ";
    }
    elseif ($widget == "") {
      $form_attr = "target=\"$ext_target\" method=\"get\" action=\"$ext_url\"";
    } else {
      $form_attr = "onsubmit=\"fill_ext_form(this);return false;\"";
      $input_target = "<input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />";
    }
    $dis_group->data_form_head = "<form $form_attr>";
    $dis_group->data_form_end = "
    <p class=\"detailButton\">
    <p class=\"detailButtons\">
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"param_resourcegroup\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      $input_target
    </p>
    </p>
    </form>";
    $display_popup_end = "
    <p><a href=\"\" onclick='window.close();'>$l_close</a></p>";
  }

  $dis_group->data_set = $obm_q;
  $dis_group->data_url = $url;

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
  global $l_resourcegroup, $l_name, $l_desc, $l_email, $l_private;
  global $cgroup_private_default, $l_insert, $l_update, $l_back;
  global $auth;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $group_q->f("rgroup_id");
    $usercreate = $group_q->f("rgroup_usercreate");
    $name = $group_q->f("rgroup_name");
    $desc = $group_q->f("rgroup_desc");
    $priv = $group_q->f("privacy");
  } elseif ($action == "new") {
    if ($cgroup_private_default) {
      $private_c = " checked";
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($group["id"])) { $id = $group["id"]; }
  if (isset($group["name"])) { $name = stripslashes($group["name"]); }
  if (isset($group["desc"])) { $desc = stripslashes($group["desc"]); }
  if (isset($group["privacy"])) { $priv = stripslashes($group["privacy"]); }

  // If new group or group update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action=="detailupdate") || ($action=="update")) &&
         ($usercreate == $auth->auth["uid"]) ) ) {
    if ($priv == '1') {
      $private_c = " checked";
    }
    $dis_private = "
  <tr>
    <td class=\"detailLabel\">$l_private</td>
    <td class=\"detailForm\"><input name=\"cb_priv\" type=\"checkbox\" value=\"1\" $private_c />
  </tr>";
  } 

  $block = "<center>
  <form method=\"get\" name=\"f_entity\" action=\"".url_prepare("resourcegroup_index.php")."\">

  <div class=\"detailHead\">$l_resourcegroup</div>

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" /></td>
  </tr>
  $dis_private
  <tr>
    <td class=\"detailLabel\">$l_desc</td>
    <td class=\"detailForm\">
      <input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"70\" /></td>
<!--      
  </tr><tr>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input name=\"tf_email\" maxlength=\"64\" size=\"32\" value=\"$email\" /></td>
  </tr>
  </tr><tr>
    <td class=\"detailLabel\">$l_private</td>
    <td class=\"detailForm\"><input name=\"cb_priv\" type=\"checkbox\" value=\"1\" $private_c /></td>
-->    
  </tr>
  </table>";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_but = "
      <input type=\"hidden\" name=\"param_resourcegroup\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" />";

  } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<div class=\"detailButton\">
    <p class=\"detailButtons\">
    $dis_but
    </p>
    </div>
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
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $g_id = $group["id"];

  $group_q = run_query_resourcegroup_detail($g_id);

  $prefs_r = get_display_pref($uid, "resourcegroup_resource");
  $r_q = run_query_resource_resourcegroup($group);
  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM ResourceGroup WHERE resourcegroup_rgroup_id=$g_id");
    $r_q->set_num_rows_total($count);
  }

  $block = html_resourcegroup_consult($group_q, $prefs_r, $r_q);

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
  global $display, $auth, $perm, $module, $action, $cright_write;
  global $l_yes, $l_no, $ico_mail;
  global $l_resourcegroup, $l_name, $l_desc, $l_email, $l_visibility, $l_no_user, $l_no_resourcegroup_resourcegroup;
  global $l_system, $l_local, $l_ext_id;
  global $l_del_resource_sel, $l_resource_member, $l_del_resourcegroup_sel, $l_resourcegroup_member;

  $id = $group_q->f("rgroup_id");
  $usercreate = $group_q->f("rgroup_usercreate");
  $userupdate = $group_q->f("rgroup_userupdate");
  $timecreate = $group_q->f("timecreate");
  $timeupdate = $group_q->f("timeupdate");
  $name = $group_q->f("rgroup_name");
  $desc = $group_q->f("rgroup_desc");

  $display["detailInfo"] = display_record_info($group_q);
  $display["title"] = "<div class=\"title\">$l_resourcegroup : $name</div>";

  $block = "
    <div class=\"detailHead\">$l_resourcegroup</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailText\">$desc</td>
    </tr>
    </table>";

  // registered Resources display
  $nb_r = $r_q->num_rows();
  if ($nb_r == 0) {
    $message = $l_no_resource;
  } else {
    $message = "$nb_r $l_resource_member";
  }

  $block .= display_info_msg($message);

  if ($nb_r != 0) {
    $url = url_prepare("resourcegroup_index.php?action=detailconsult&amp;param_resourcegroup=$id");
    $dis_infos = new OBM_DISPLAY("DATA", $prefs_r, "resourcegroup", "resourcegroup_resource");
    $dis_infos->data_set = $r_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($perm->check_right($module, $cright_write)) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "resourcegroup_resource_id";
      $dis_infos->data_cb_name = "cb_u";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"resourcegroup_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_resourcegroup");

    if ($perm->check_right($module, $cright_write)) {
      $block .= "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_resource_sel\" />
        <input type=\"hidden\" name=\"param_resourcegroup\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"resource_del\" />
        </p>
      </p>
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
  $dis_pref->pref_dis_help = 0;

  $block = "
    <table class=\"detail\">
    <tr>
      <td>" . $dis_pref->display() . "</td>";

  $dis_pref->display_pref = $pref_r_q;
  $dis_pref->display_entity = "resourcegroup_resource";
  $dis_pref->pref_title = $l_resource_display;

  $block .= "<td>" . $dis_pref->display() . "</td>
      </td>
    </tr>
    </table>";

  $block .= $dis_pref->dis_pref_help();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a group deletion                                   //
// We ask confirmation or cancel
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function dis_resourcegroup_warn_delete($id) {
  global $display, $l_warn_delete, $l_delete, $l_back, $l_cant_delete_system;
  global $uid, $l_cant_delete_public;

  $lgroup = get_resourcegroup_info($id);
  if (($lgroup["privacy"] == "0") && ($lgroup["usercreate"] != "$uid")) {
    $display["msg"] .= display_warn_msg($l_cant_delete_public);
    $dis_delete = "";
  } else {
    $nb_r = get_resourcegroup_nb_resource($id);
    $display["msg"] .= display_warn_msg("$nb_r $l_warn_delete");

    $dis_delete = "
    <p class=\"detailButtons\">
      <form method=\"get\" name=\"form_delete\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"hd_resourcegroup_id\" value=\"$id\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
      </form>
    </p>";
  }

  $dis_back = "
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"param_resourcegroup\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>";

  $block = "
  <div class=\"detailButton\">
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

  $name = $group["name"];
  $desc = $group["desc"];

  $display["msg"] .= display_warn_msg($l_check_samegroup);
  while ($g_q->next_record()) {
    $id = $g_q->f("rgroup_id");
    $samename = $g_q->f("rgroup_name");
    $samedesc = $g_q->f("rgroup_desc");
    $dis_same_group .= "
      <tr><td class=\"detailLabel\">
        <a href=\"" .url_prepare("resourcegroup_index.php?action=detailconsult&amp;param_resourcegroup=$id") . "\">$samename ($samedesc)</a>
      </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_group
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      </form>
    </p>
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("resourcegroup_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  return $block;
}


?>