<script language="php">
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
$fieldnames["group_desc"] = $l_desc;
$fieldnames["group_email"] = $l_email;
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
// Display Group specific dataset fields                                     //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_group(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new,$ext_element;

  if (($fieldname == "group_name") && $link_ok) {
    $res["url"] = "$path/group/group_index.php?action=detailconsult&amp;param_group=".$OD->data_set->f("group_id");
  }
  if (($fieldname == "hidden_field") && $ext_element != "") {
    $res["name"] = "<span id=\"data".$OD->data_set->f("group_id")."\" style=\"display:none;\">".
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
    $query = "select count(*) from UserObmGroup where userobmgroup_group_id='$id'";
    $nb_q->query($query);
    $nb_q->next_record();
    $res["name"] = $nb_q->f(0);
    $res["align"] = "center";
  }
  // For User member lists
  elseif (($fieldname == "group_user_lastname")  && $link_ok) {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("group_user_id");
  }

  else if ($fieldname == "group_email") {
    $email = $OD->data_set->f("group_email");
    if (strcmp($email,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"$email\" />";
      $res["txt_name"] = $email;
    }
  }


  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Group search Form                                                 //
// Parameters : 
//   - $group[]   : default form values
//     keys used  : name, user
///////////////////////////////////////////////////////////////////////////////
function html_group_search_form ($group) {
  global $display, $l_name, $l_group_user, $l_email, $l_find, $l_add_groups;
  global $c_all, $l_all, $l_visibility, $l_private, $l_public, $c_private, $c_public;
 
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
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($group["name"]);
  $email = stripslashes($group["email"]);
  $user = stripslashes($group["user"]);
  $privacy = $group["privacy"];

  // Privacy select
  $sel_privacy = "<select id=\"sel_privacy\" name=\"sel_privacy\"><option value=\"$c_all\">$l_all</option>";
  $sel_privacy .= "\n<option value=\"$c_public\"";
  if ($privacy == $c_public) { $sel_privacy .= " selected=\"selected\""; }
  $sel_privacy .= ">". $l_public . "</option>\n";
  $sel_privacy .= "\n<option value=\"$c_private\"";
  if ($privacy  == $c_private) { $sel_privacy .= " selected=\"selected\""; }
  $sel_privacy .= ">". $l_private . "</option>\n";
  $sel_privacy .= "</select>";

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" action=\"".url_prepare("group_index.php")."\">
  <div class=\"search\">

    <div class=\"searchLabel\">$l_name<br />
      <input name=\"tf_name\" size=\"16\" maxlength=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_group_user<br />
      <input name=\"tf_user\" size=\"16\" maxlength=\"16\" value=\"$user\" />
    </div>
    <div class=\"searchLabel\">$l_email<br />
      <input name=\"tf_email\" size=\"20\" maxlength=\"20\" value=\"$email\" />
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
// Display the Group search result                                           //
// Parameters:
//   - $group[]   : group search criteria
//     keys used  : name, description, email
///////////////////////////////////////////////////////////////////////////////
function dis_group_search_group($group) {
  global $l_no_found, $display, $auth;

  $child_res = $group["children_restriction"];
  $widget = $group["ext_widget"];

  $pref_q = run_query_display_pref($auth->auth["uid"], "group");
  if ($child_res) {
    $obm_q = run_query_search_possible_children($group);
  }  else {
    $obm_q = run_query_search($group);
  }

  $nb_group = $obm_q->num_rows_total();
  if ($nb_group == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_group_search_group($obm_q, $pref_q, $nb_group, $group);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Group search result
// Parameters : 
//   - $obm_q     : list of the groups to display 
//   - $pref_q    : the fields which have to be displayed
//   - $nb_group  : nb groups returned by the search query 
//   - $group[]   : group search criteria
//     keys used  : name, user, popup
///////////////////////////////////////////////////////////////////////////////
function html_group_search_group($obm_q, $pref_q, $nb_group, $group) {
  global $display, $l_found, $l_add, $l_close;

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
  
  $dis_group = new OBM_DISPLAY("DATA", $pref_q);
  if ($popup) {
    $dis_group->display_link = false;
    $dis_group->data_cb_text = "X";
    $dis_group->data_idfield = "group_id";
    $dis_group->data_cb_name = "cb_g";
    if ($ext_element != "") {
      $dis_group->data_cb_name = "";
      $form_attr = "
      onsubmit=\"fill_ext_form2(this); return false;\" ";
    }
    elseif ($widget == "") {
      $form_attr = "target=\"$ext_target\" method=\"get\" action=\"$ext_url\"";
    } else {
      $form_attr = "onsubmit=\"fill_ext_form(this);return false;\"";
      $input_target = "<input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />";
    }
    $display_popup_head = "<form $form_attr>";
    $display_popup_end = "
    <p class=\"detailButton\">
    <p class=\"detailButtons\">
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"param_group\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      $input_target
    </p>
    </p>
    </form>
    <p><a href=\"\" onclick='window.close();'>$l_close</a></p>";
  }

  $dis_group->data_set = $obm_q;
  $dis_group->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_info_msg("$nb_group $l_found");
  $block .= $display_popup_head;
  $block .= $dis_group->display("dis_data_group");
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
function html_group_form($action, $group_q, $group) {
  global $cgroup_private_default, $l_group, $l_name, $l_desc, $l_email;
  global $l_private, $l_insert, $l_update, $l_back;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $group_q->f("group_id");
    $usercreate = $group_q->f("group_usercreate");
    $name = $group_q->f("group_name");
    $desc = $group_q->f("group_desc");
    $email = $group_q->f("group_email");
    $priv = $group_q->f("group_privacy");
  } elseif ($action == "new") {
    if ($cgroup_private_default) {
      $private_c = " checked";
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($group["id"])) { $id = $group["id"]; }
  if (isset($group["name"])) { $name = stripslashes($group["name"]); }
  if (isset($group["desc"])) { $desc = stripslashes($group["desc"]); }
  if (isset($group["email"])) { $email = stripslashes($group["email"]); }
  if (isset($group["priv"])) { $priv = stripslashes($group["priv"]); }

  // If new group or group update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action == "detailupdate") || ($action == "update")) &&
         ($usercreate == $auth->auth["uid"]) ) ) {
    if ($priv == '1') {
      $private_c = " checked";
    }
    $display_private = "<input name=\"cb_priv\" type=\"checkbox\" value=\"1\" $private_c />";
  }

  $block = "<center>
  <form method=\"get\" name=\"f_group\" action=\"".url_prepare("group_index.php")."\">

  <div class=\"detailHead\">$l_group</div>

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$name\" /></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_desc</td>
    <td class=\"detailForm\">
      <input name=\"tf_desc\" value=\"$desc\" size=\"32\" maxlength=\"70\" /></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input name=\"tf_email\" maxlength=\"64\" size=\"32\" value=\"$email\" /></td>
  </tr>
  </tr><tr>
    <td class=\"detailLabel\">$l_private</td>
    <td class=\"detailForm\"><input name=\"cb_priv\" type=\"checkbox\" value=\"1\" $private_c /></td>
  </tr>
  </table>";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_but = "
      <input type=\"hidden\" name=\"param_group\" value=\"$id\" />
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
function dis_group_consult($group, $uid) {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $g_id = $group["id"];

  $group_q = run_query_detail($g_id);

  $pref_u_q = run_query_display_pref($uid, "group_user");
  $u_q = run_query_user_group($group);
  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM UserObmGroup WHERE userobmgroup_group_id=$g_id");
    $u_q->set_num_rows_total($count);
  }

  $pref_g_q = run_query_display_pref($uid, "group_group");
  $g_q = run_query_group_group($group);
  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM GroupGroup WHERE groupgroup_parent_id=$g_id");
    $g_q->set_num_rows_total($count);
  }
  $block = html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Group Consultation
// Parameters:
//   - $group_q  : group database result 
//   - $pref_u_q : group user preference display 
//   - $u_q      : user database result 
///////////////////////////////////////////////////////////////////////////////
function html_group_consult($group_q, $pref_u_q, $u_q, $pref_g_q, $g_q) {
  global $auth;
  global $l_yes, $l_no, $ico_mail;
  global $l_group, $l_name, $l_desc, $l_email, $l_visibility, $l_no_user, $l_no_group_group;
  global $l_visibility, $l_private, $l_public;
  global $l_system, $l_local, $l_ext_id;
  global $l_del_user_sel, $l_user_member, $l_del_group_sel, $l_group_member;
  global $display, $perm, $module, $action, $cright_write;

  $id = $group_q->f("group_id");
  $usercreate = $group_q->f("group_usercreate");
  $userupdate = $group_q->f("group_userupdate");
  $timecreate = $group_q->f("timecreate");
  $timeupdate = $group_q->f("timeupdate");
  $system = ($group_q->f("group_system") == 1 ? $l_yes : $l_no);
  $local = ($group_q->f("group_local") == 1 ? $l_yes : $l_no);
  $ext_id = $group_q->f("group_ext_id");
  $name = $group_q->f("group_name");
  $desc = $group_q->f("group_desc");
  $email = $group_q->f("group_email");
  $priv = $group_q->f("group_privacy");

  //$mail = get_mail_info();
  //$main_domain = $mail["main_domain"];

  $display["detailInfo"] = display_record_info($group_q);
  $display["title"] = "<div class=\"title\">$l_group : $name</div>";

  // Privacy
  if ($priv == "1") {
    $l_priv = $l_private;
  } else {
    $l_priv = $l_public;
  }
  $dis_priv = "</tr><tr>
      <td class=\"detailLabel\">$l_visibility</td>
      <td class=\"detailText\">$l_priv</td>";

  $block = "
    <div class=\"detailHead\">$l_group</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_system</td>
      <td class=\"detailText\">$system</td>
    $dis_priv
    </tr><tr>
      <td class=\"detailLabel\">$l_local</td>
      <td class=\"detailText\">$local</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_ext_id</td>
      <td class=\"detailText\">$ext_id</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_desc</td>
      <td class=\"detailText\">$desc</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr>
    </table>";


  // Affichage des groupes membres 
  $nb_g = $g_q->num_rows_total();
  if ($nb_g == 0) {
    $message = $l_no_group_group;
  } else {
    $message = "$nb_g $l_group_member";
  }

  $block .= display_info_msg($message);

  if ($nb_g != 0) {
    $url = url_prepare("group_index.php?action=detailconsult&amp;param_group=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $pref_g_q);
    $dis_infos->data_set = $g_q;
    $dis_infos->display_entity = "group_group";
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "top";

    if ($perm->check_right($module, $cright_write)) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "child_id";
      $dis_infos->data_cb_name = "cb_g";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"group_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_group");

    if ($perm->check_right($module, $cright_write)) {
      $block .= "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_group_sel\" /> 
        <input type=\"hidden\" name=\"param_group\" value=\"$id\" /> 
        <input type=\"hidden\" name=\"action\" value=\"group_del\" /> 
        </p>
      </p>
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

  $block .= display_info_msg($message);

  if ($nb_u != 0) {

    $url = url_prepare("group_index.php?action=detailconsult&amp;param_group=$id");
    $dis_infos = new OBM_DISPLAY("DATA", $pref_u_q);
    $dis_infos->data_set = $u_q;
    $dis_infos->display_entity = "group_user";
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($perm->check_right($module, $cright_write)) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "group_user_id";
      $dis_infos->data_cb_name = "cb_u";
      $dis_infos->data_cb_field = "";
      $block .= "<form method=\"get\" action=\"group_index.php\">";
    }

    $block .= $dis_infos->display("dis_data_group");

    if ($perm->check_right($module, $cright_write)) {
      $block .= "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_user_sel\" />
        <input type=\"hidden\" name=\"param_group\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"user_del\" />
        </p>
      </p>
      </form>";
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Group Display preference screen                               //
// Parameters:
//   - $pref_group_q : DBO : Group Field group to display
//   - $pref_u_q     : DBO : User Field group to display
///////////////////////////////////////////////////////////////////////////////
function dis_group_display_pref($pref_group_q, $pref_u_q) {
  global $l_group_display, $l_user_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_group_q);
  $dis_pref->display_entity = "group"; 
  $dis_pref->pref_title = $l_group_display;
  $dis_pref->pref_dis_help = 0;

  $block = "
    <table class=\"detail\">
    <tr>
      <td>" . $dis_pref->display() . "</td>";

  $dis_pref->display_pref = $pref_u_q;
  $dis_pref->display_module = "group";
  $dis_pref->display_entity = "group_user";
  $dis_pref->pref_title = $l_user_display;

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
function dis_warn_delete($id) {
  global $display, $l_warn_delete, $l_delete, $l_back, $l_cant_delete_system;
  global $uid, $l_cant_delete_public;

  $lgroup = get_group_info($id);
  if ($lgroup["system"] != "0") {
    $display["msg"] .= display_warn_msg($l_cant_delete_system);
    $dis_delete = "";
  } else if (($lgroup["privacy"] == "0") && ($lgroup["usercreate"] != "$uid")) {
    $display["msg"] .= display_warn_msg($l_cant_delete_public);
    $dis_delete = "";
  } else {
    $nb_u = get_group_nb_user($id);
    $display["msg"] .= display_warn_msg("$nb_u $l_warn_delete");

    $dis_delete = "
    <p class=\"detailButtons\">
      <form method=\"get\" name=\"form_delete\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"delete\" />
      <input type=\"hidden\" name=\"hd_group_id\" value=\"$id\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_delete\" />
      </form>
    </p>";
  }

  $dis_back = "
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
      <input type=\"hidden\" name=\"param_group\" value=\"$id\" />
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
function dis_group_warn_insert($id, $g_q, $group) {
  global $display, $l_check_samegroup, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $group["name"];
  $desc = $group["desc"];

  $display["msg"] .= display_warn_msg($l_check_samegroup);
  while ($g_q->next_record()) {
    $id = $g_q->f("group_id");
    $samename = $g_q->f("group_name");
    $samedesc = $g_q->f("group_desc");
    $dis_same_group .= "
      <tr><td class=\"detailLabel\">
        <a href=\"" .url_prepare("group_index.php?action=detailconsult&amp;param_group=$id") . "\">$samename ($samedesc)</a>
      </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_group
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
      <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      </form>
    </p>
    <p class=\"detailButtons\">
      <form name=\"form_back\" method=\"get\"
      action=\"" .url_prepare("group_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  return $block;
}


</script>
