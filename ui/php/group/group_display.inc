<?php
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
$fieldnames["group_name"] = $l_name;
$fieldnames["group_system"] = $l_system;
$fieldnames["group_privacy"] = $l_private;
$fieldnames["group_local"] = $l_local;
$fieldnames["group_ext_id"] = $l_ext_id;
$fieldnames["group_samba"] = $l_smb;
$fieldnames["group_desc"] = $l_desc;
$fieldnames["group_email"] = $l_email;
$fieldnames["group_mailing"] = $l_mailing;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;
// Calculated fields
$fieldnames["group_nb_user"] = $l_nb_user;
$fieldnames["group_user_lastname"] = $l_lastname;
$fieldnames["group_user_firstname"] = $l_firstname;
$fieldnames["group_user_function"] = $l_function;
$fieldnames["group_user_phone"] = $l_phone;
$fieldnames["group_user_email"] = $l_email;


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

  if (($fieldname == "group_name") && $link_ok) {
    $res["url"] = "$path/group/group_index.php?action=detailconsult&amp;group_id=".$OD->data_set->f("group_id");
  }
  if (($fieldname == "data_element") && $params["ext_element"] != "") {
    $res["name"] = "<span id=\"data-group-".$OD->data_set->f("group_id")."\" style=\"display:none;\">".
    $OD->data_set->f("group_name")."</span>";
  }  

  else if ($fieldname == "group_system") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "group_privacy") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "group_samba") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "group_local") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "group_nb_user") {
    $nb_q = new DB_OBM;
    $id = $OD->data_set->f("group_id");
    $query = "SELECT count(*) FROM UserObmGroup WHERE userobmgroup_group_id='$id'";
    $nb_q->query($query);
    $nb_q->next_record();
    $res["name"] = $nb_q->f(0);
    $res["align"] = "center";
  }

  // For User member lists
  elseif (($fieldname == "group_user_lastname") && $link_ok) {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("group_user_id");
  }

  else if ($fieldname == "group_email") {
    $email = $OD->data_set->f("group_email");
    if (strcmp($email,"") != 0) {
      if ($cgp_use["service"]["mail"]) {
        $email .= "@".$obm["domain_name"];
      }
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"$ico_mail\" alt=\"$email\" />";
      $res["txt_name"] = $email;
    }
  }

  else if ($fieldname == "group_mailing") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
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
  global $display, $l_name, $l_group_user, $l_email, $l_find, $l_add_groups;
  global $l_visibility, $l_mailing;
 
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
      $ext_title = $l_add_groups;
    }
    $display["title"] = "<h1 class=\"title\">$ext_title</h1>";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($group["name"]);
  $email = stripslashes($group["email"]);
  $user = stripslashes($group["user"]);
  $privacy = $group["privacy"];
  $mailing = $group["mailing"];

  $sel_privacy = dis_privacy_search_select($privacy);

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" action=\"".url_prepare("group_index.php")."\" class=\"search\">

    <label>$l_name<br />
      <input name=\"tf_name\" size=\"16\" maxlength=\"16\" value=\"$name\" />
    </label>
    <label>$l_group_user<br />
      <input name=\"tf_user\" size=\"16\" maxlength=\"16\" value=\"$user\" />
    </label>
    <label>$l_email<br />
      <input name=\"tf_email\" size=\"24\" maxlength=\"64\" value=\"$email\" />
    </label>
    <label>$l_visibility<br />
      $sel_privacy
    </label>
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
//     keys used  : name, description, email
///////////////////////////////////////////////////////////////////////////////
function dis_group_search_group($group) {
  global $obm, $display, $l_found, $l_no_found;

  $child_res = $group["children_restriction"];
  $widget = $group["ext_widget"];

  $prefs = get_display_pref($obm["uid"], "group");
  if ($child_res) {
    $obm_q = run_query_group_search_possible_children($group);
  }  else {
    $obm_q = run_query_group_search($group);
  }

  $nb_group = $obm_q->num_rows_total();
  if ($nb_group == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_group $l_found");
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
  $user = urlencode(stripslashes($group["user"]));

  $url = url_prepare("group_index.php?action=search&amp;tf_name=$namet&amp;tf_user=$user$url_ext");
  
  $dis_group = new OBM_DISPLAY("DATA", $prefs, "group");
  if ($popup) {
    $dis_group->display_link = false;
    $dis_group->data_cb_text = "X";
    $dis_group->data_idfield = "group_id";
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
    <div class=\"buttons\">
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"group_id\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      $input_target
    </div>
    </form>";

    $display_popup_end = "
    <a href=\"\" onclick='window.close();'>$l_close</a>";
  }

  $dis_group->data_set = $obm_q;
  $dis_group->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_group->display("dis_data_group");
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

  $g_id = $group["group_id"];

  if ($g_id > 0) {
    $group_q = run_query_group_detail($g_id);

    $prefs_u = get_display_pref($uid, "group_user");
    $u_q = run_query_group_user_group($group);
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(*) FROM UserObmGroup WHERE userobmgroup_group_id=$g_id");
      $u_q->set_num_rows_total($count);
    }

    $prefs_g = get_display_pref($uid, "group_group");
    $g_q = run_query_group_group($group);
    if ($ctu_sql_limit) {
      $count = get_query_count("SELECT count(*) FROM GroupGroup WHERE groupgroup_parent_id=$g_id");
      $g_q->set_num_rows_total($count);
    }
    $block = html_group_consult($group_q, $prefs_u, $u_q, $prefs_g, $g_q);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
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
function html_group_consult($group_q, $prefs_u, $u_q, $prefs_g, $g_q) {
  global $l_yes, $l_no, $ico_mail;
  global $l_group, $l_name, $l_desc, $l_email, $l_mailing, $l_no_user, $l_no_group_group;
  global $l_system, $l_local, $l_ext_id, $l_smb;
  global $l_visibility, $l_private, $l_public;  
  global $l_del_user_sel, $l_user_member, $l_del_group_sel, $l_group_member;
  global $obm, $display, $perm, $module, $action, $cright_write;
  global $cg_gid_smb_user, $cg_gid_smb_admin;
  global $cgp_use, $cgp_hide;

  $id = $group_q->f("group_id");
  $usercreate = $group_q->f("group_usercreate");
  $userupdate = $group_q->f("group_userupdate");
  $timecreate = $group_q->f("timecreate");
  $timeupdate = $group_q->f("timeupdate");
  $system = ($group_q->f("group_system") == 1 ? $l_yes : $l_no);
  $local = ($group_q->f("group_local") == 1 ? $l_yes : $l_no);
  $privacy = ($group_q->f("group_privacy") == 1 ? $l_private : $l_public);
  $ext_id = $group_q->f("group_ext_id");
  $name = $group_q->f("group_name");
  $desc = $group_q->f("group_desc");
  $email = $group_q->f("group_email");
  $mailing = ($group_q->f("group_mailing") == 1 ? $l_yes : $l_no);

  if ($email != "") {
    if ($cgp_use["service"]["mail"]) {
      $email = "$email@$obm[domain_name]";
    } 
    $dis_email = "$email";
  }

  if($group_q->f("group_privacy") == 0) {
    $dis_email = "<tr>
      <th>$l_email</th>
      <td>$dis_email</td>
      </tr>
      <tr>
        <th>$l_mailing</th>
        <td>$mailing</td>
      </tr>      
      ";

    if ($cgp_use["service"]["samba"]) {
      $samba = ($group_q->f("group_samba") == 1 ? $l_yes : $l_no);
      $block_samba = "
      <tr>
        <th>$l_smb</td>
        <td>$samba</td>
      </tr>";
    }    
  } else {
    $dis_email = "";
    $samba = "";
  }
  $block_userdata .= of_userdata_dis_entity_consult("group", $id);

  // Prevent to update samba systems groups
  $update_group = true;
  if (($gid == $cg_gid_smb_admin) || ($gid == $cg_gid_smb_user)
      || (! $perm->check_right($menu, $cright_write))) {
    $update_group = false;
  }

  // Display members groups
  $nb_g = $g_q->num_rows_total();
  if ($nb_g == 0) {
    $message = $l_no_group_group;
  } else {
    $message = "$nb_g $l_group_member";
  }

  //  $block_mg = display_info_msg($message);
  $block_mg .= "<h1>$message</h1>";

  if ($nb_g != 0) {
    $url = url_prepare("group_index.php?action=detailconsult&amp;group_id=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $prefs_g, "group", "group_group","group_group");
    $dis_infos->data_set = $g_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "top";

    if ($update_group) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "child_id";
      $dis_infos->data_cb_name = "data-g-";
      $dis_infos->data_cb_field = "";
      $block_mg .= "<form method=\"get\" action=\"group_index.php\">";
    }

    $block_mg .= $dis_infos->display("dis_data_group");

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
  $block_mu .= "<h1>$message</h1>";

  if ($nb_u != 0) {

    $url = url_prepare("group_index.php?action=detailconsult&amp;group_id=$id");
    $dis_infos = new OBM_DISPLAY("DATA", $prefs_u, "group", "group_user","group_user");
    $dis_infos->data_set = $u_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($update_group) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "group_user_id";
      $dis_infos->data_cb_name = "data-u-";
      $dis_infos->data_cb_field = "";
      $block_mu .= "<form method=\"get\" action=\"group_index.php\">";
    }

    $block_mu .= $dis_infos->display("dis_data_group");

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

  $display["detailInfo"] = display_record_info($group_q);
  $display["title"] = "<h1 class=\"title\">$l_group : $name</h1>";

  $block .= "
  <div class=\"detail infos\">
    <h1>$l_group</h1>

    <table>
    <tr>
      <th>$l_name</th>
      <td>$name</td>
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
      <td>$privacy</td>
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
  $block_mg
  </div>
  <div class=\"detail extra\">
  $block_mu
  </div>
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
function html_group_form($action, $group_q, $group, $field="") {
  global $obm, $cgroup_private_default, $cgp_use;
  global $l_group, $l_name, $l_desc, $l_smb, $l_email, $l_mailing;
  global $l_private, $l_insert, $l_update, $l_back;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $group_q->f("group_id");
    $usercreate = $group_q->f("group_usercreate");
    $name = $group_q->f("group_name");
    $system = $group_q->f("group_system");
    $samba = $group_q->f("group_samba");
    $desc = $group_q->f("group_desc");
    $email = $group_q->f("group_email");
    $priv = $group_q->f("privacy");
    $mailing = $group_q->f("group_mailing");
  } elseif ($action == "new") {
    if ($cgroup_private_default) {
      $private_c = " checked";
      $mail_class = "H";
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($group["group_id"])) { $id = $group["group_id"]; }
  if (isset($group["name"])) { $name = stripslashes($group["name"]); }
  if (isset($group["desc"])) { $desc = stripslashes($group["desc"]); }
  if (isset($group["email"])) { $email = stripslashes($group["email"]); }
  if (isset($group["privacy"])) { $priv = stripslashes($group["privacy"]); }
  if (isset($group["mailing"])) { $mailing = stripslashes($group["mailing"]); }

  // Mark the error field
  if ($field != "") {
    $class[$field] = "error";
  }

  $block_userdata = of_userdata_dis_entity_form("group", $id, $group);

  // If new group or group update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action == "detailupdate") || ($action == "update")) &&
         ($usercreate == $obm["uid"]) ) ) {
    if ($priv == '1') {
      $private_c = " checked";
      $mail_class = "H";
    }
    if ($mailing == '1') {
      $mailing_c = " checked";
    }
    $dis_private = "
  <tr>
    <th>$l_private</th>
    <td>
    <script type=\"text/javascript\">
      function privacyShowHide(input) {
        if($(input).getValue()) {
          $('emailGroup').addClass('H');
          $('sambaGroup').addClass('H');
          $('mailingGroup').addClass('H');
        } else {
          $('emailGroup').removeClass('H')
          $('sambaGroup').removeClass('H');
          $('mailingGroup').removeClass('H');
        }
      }
    </script>
    <input name=\"cba_privacy\" type=\"checkbox\" 
    onchange=\"privacyShowHide(this)\" value=\"1\" $private_c /></td
  </tr>";
  } 

  if ($cgp_use["service"]["samba"]) {
    // Samba group checkbox checked value
    $samba_state  = ($samba ? "checked" : "");

    $block_samba = "
  <tr id=\"sambaGroup\"  class=\"$mail_class\">
    <th class=\"$class[samba]\">$l_smb</th>
    <td><input name=\"cb_samba\" type=\"checkbox\" value=\"1\" $samba_state></td>
  </tr>";
  }

  $dis_mailing = "
  <tr id=\"mailingGroup\"  class=\"$mail_class\">
    <th>$l_mailing</th>
    <td><input name=\"cba_mailing\" type=\"checkbox\" value=\"1\" $mailing_c /></td>
  </tr>";

  $block = "
  <form method=\"get\" name=\"f_entity\" action=\"".url_prepare("group_index.php")."\">
<fieldset class=\"detail infos\">
  <legend>$l_group</legend>

  <table>
  <tr>
    <th>$l_name</th>
    <td><input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" /></td>
  </tr>
  $dis_private
  $block_samba
  <tr id=\"emailGroup\" class=\"$mail_class\">
    <th>$l_email</th>
    <td><input name=\"tf_email\" maxlength=\"64\" size=\"32\" value=\"$email\" /></td>
  </tr>
  $dis_mailing
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
  </fieldset>";

  if (($action == "detailupdate") || ($action == "update")) {
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

  $url = url_prepare("group_index.php");

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

  $display["msg"] .= display_ok_msg($l_can_delete);

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

  $name = $group["name"];
  $desc = $group["desc"];
  $samba = $group["samba"];
  $email = $group["email"];
  $mailing = $group["mailing"];
  $privacy = $group["privacy"];

  $display["msg"] .= display_warn_msg($l_check_samegroup);
  while ($g_q->next_record()) {
    $id = $g_q->f("group_id");
    $samename = $g_q->f("group_name");
    $samedesc = $g_q->f("group_desc");
    $dis_same_group .= "
      <li>
        <a href=\"" .url_prepare("group_index.php?action=detailconsult&amp;group_id=$id") . "\">$samename ($samedesc)</a>
      </li>";
  }

  $hidden_fields = "
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
    <input type=\"hidden\" name=\"cba_samba\" value=\"$samba\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"cba_mailing\" value=\"$mailing\" />
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
    action=\"" .url_prepare("group_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    $hidden_fields
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    <form name=\"form_back\" method=\"get\"
    action=\"" .url_prepare("group_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    $hidden_fields
    <input type=\"submit\" value=\"$l_back\" />
    </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the contact administration index
///////////////////////////////////////////////////////////////////////////////
function dis_group_admin_index() {

  $block = of_userdata_dis_admin_form("group");

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

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs_g, "group");
  $dis_pref->pref_title = $l_group_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  $dis_pref->display_pref = $prefs_u;
  $dis_pref->display_entity = "group_user";
  $dis_pref->pref_title = $l_user_display;

  $block .= $dis_pref->display();

  return $block;
}


/**
 * Build a JSON array with all search results 
 * 
 * @param DB $res_q Group search query results
 * @access public
 * @return void
 */
function json_search_group($group,$grp_q) {
  global $display, $l_desc;

  $limit = $group['limit'];
  $groups = array();
  if(isset($group["first_row"])) $grp_q->seek($group["first_row"] - 1);
  while($grp_q->next_record() && count($groups) < $limit ) {
    $id = $grp_q->f("group_id");
    $label = phpStringToJsString($grp_q->f("group_name"));
    $extra = phpStringToJsString("$l_desc : ".$grp_q->f("group_desc"));
    $groups[] = "{id:'data-group-$id', label:'$label', extra:'$extra'}";
  }
  $display["json"] = "{length:".$grp_q->nf().", datas:[".implode(",",$groups)."]}";
}
?>
