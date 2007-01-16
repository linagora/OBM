<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_display.inc                                             //
//     - Desc : User Display functions File                                  //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["userobm_login"] = $l_login;
$fieldnames["userobm_local"] = $l_local;
$fieldnames["userobm_ext_id"] = $l_ext_id;
$fieldnames["userobm_perms"] = $l_user_perms;
$fieldnames["userobm_archive"] = $l_archive;
$fieldnames["userobm_lastname"] = $l_lastname;
$fieldnames["userobm_firstname"] = $l_firstname;
$fieldnames["userobm_phone"] = $l_phone;
$fieldnames["userobm_phone2"] = $l_phone2;
$fieldnames["userobm_fax"] = $l_fax;
$fieldnames["userobm_fax2"] = $l_fax2;
$fieldnames["datebegin"] = $l_datebegin;
$fieldnames["timelastaccess"] = $l_lastaccess;
$fieldnames["userobm_email"] = $l_email;
$fieldnames["userobm_description"] = $l_desc;

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
  global $path, $set_theme, $ico_mail,$ext_element, $params;

  $ext_url = $params["ext_url"];

  if (($fieldname == "userobm_login")) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_user_get_id(".$OD->data_set->f("userobm_id").",'".addslashes($OD->data_set->f("userobm_login"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_user_get_id_url('$ext_url',".$OD->data_set->f("userobm_id").");";
    } else {
      if ($link_ok)
        $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("userobm_id");
    }
  }

  if (($fieldname == "data_element") && $ext_element != "") {
    $res["name"] = "<span id=\"data-user-".$OD->data_set->f("userobm_id")."\" style=\"display:none;\">".
    $OD->data_set->f("userobm_lastname")." ".$OD->data_set->f("userobm_firstname").
    "</span>";
  }  

  else if ($fieldname == "userobm_email") {
    $email = $OD->data_set->f("userobm_email");
    if (strcmp($email ,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"$ico_mail\" alt=\"$email\">$email";
      $res["txt_name"] = $email;
    }
  } 

  else if ($fieldname == "userobm_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "userobm_local") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "timelastaccess") {
    $date = $OD->data_set->f($fieldname);
    $res["name"] = of_datetime_format($date, 1);
  }
  else if ($fieldname == "datebegin") {
    $datebegin = $OD->data_set->f($fieldname);
    $res["name"] = of_date_format($datebegin, 1);
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display User search form
// Parameters:
//   - $user[]   : default form values
//     keys used : login, lastname, perms, archive, popup
///////////////////////////////////////////////////////////////////////////////
function html_user_search_form($user) {
  global $l_login, $l_lastname, $l_user_perms, $l_find, $l_all, $l_archive;
  global $l_email, $l_desc, $l_group, $l_add_users;
  global $display, $c_all, $profiles;
  
  $login = $user["login"];
  $perms = $user["perms"];
  $lname = stripslashes($user["lastname"]);
  $email = $user["email"];
  $desc = $user["desc"];
  $group = $user["group"];
  $archive = ($user["archive"] == "1" ? "checked" : "");
  $popup = $user["popup"];
  if ($user["restriction_calendar"]) {
    $dis_restriction = "<input name=\"restriction_calendar\" type=\"hidden\" value=\"calendar\">";
  }

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_title = stripslashes($user["ext_title"]);
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext_widget = $user["ext_widget"];
    $ext_element = $user["ext_element"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
	    <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    if ($ext_title == "") {
      $ext_title = $l_add_users;
    }
    $display["title"] = "<h1 class=\"title\">$ext_title</h1>";
  }

  foreach ($profiles as $profile => $one_profile) {
    $block_pro .= "
      <option value=\"$profile\"> $profile</option>";
  }

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"". url_prepare("user_index.php")."\" class=\"search\"> 

    <label>$l_login<br />
      <input type=\"text\" name=\"tf_login\" size=\"16\" maxlength=\"32\"
      value=\"$login\" />
    </label>
    <label>$l_lastname<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"16\" maxlength=\"32\"
      value=\"$lname\" />
    </label>
    <label>$l_user_perms<br />
      <select type=\"text\" name=\"sel_perms\">
        <option value=\"$c_all\"> $l_all</option>
        $block_pro
      </select>
    </label>
    <label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" maxlength=\"32\"
      value=\"$email\" />
    </label>
    <label>$l_desc<br />
      <input type=\"text\" name=\"tf_desc\" size=\"16\" maxlength=\"32\"
      value=\"$desc\" />
    </label>
    <label>$l_group<br />
      <input type=\"text\" name=\"tf_group\" size=\"16\" maxlength=\"32\"
      value=\"$group\" />
    </label>
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $dis_restriction
      $ext
    </label>
    <p class=\"CL\" />
   </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result
// Parameters:
//   - $user[]   : user search criteria
//     keys used : name, perms
///////////////////////////////////////////////////////////////////////////////
function dis_user_search_list($user) {
  global $l_found, $l_no_found;
  global $display, $auth;

  $popup = $user["popup"];
  $restriction = $user["restriction_calendar"];
  $obm_q = run_query_user_search($user);

  $prefs = get_display_pref($auth->auth["uid"], "user");
  $nb_user = $obm_q->num_rows_total();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_user $l_found");
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
  global $l_close, $l_add;

  $login = urlencode($user["login"]);
  $lname = urlencode($user["lastname"]);
  $perms = $user["perms"];
  $email = $user["email"];
  $desc = urlencode($user["desc"]);
  $group = urlencode($user["group"]);
  $archive = $user["archive"];

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext_widget = $user["ext_widget"];
    $ext_widget_text = $user["ext_widget_text"];
    $ext_element = $user["ext_element"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget";
  }

  $url = url_prepare("user_index.php?action=search&amp;tf_login=$login&amp;tf_lastname=$lname&amp;sel_perms=$perms&amp;tf_email=$email&amp;tf_desc=$desc&amp;tf_group=$group&amp;cba_archive=$archive$url_ext");

  $user_d = new OBM_DISPLAY("DATA", $prefs, "user");
  if ($popup) {
    //    if ($ext_action == "ext_get_ids") {
    echo "pop=$popup";
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "userobm_id";
    $user_d->data_cb_name = "data-u-";
    if ($ext_element != "") {
      $user_d->data_cb_name = "";
      $user_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this); return false;\">";
    } elseif ($ext_widget != "") {
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
    if ($ext_url != "") {
      $user_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $user_d->display_ext = "get_id";
    }
  }*/

  $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $user_d->display("dis_user_data");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Form
// Parameters:
//   - $usr_q     : user database result
//   - $user      : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
///////////////////////////////////////////////////////////////////////////////
function html_user_form($usr_q, $user) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname;
  global $l_user, $l_email, $l_archive, $l_insert, $l_update;
  global $l_phone, $l_phone2, $l_fax, $l_fax2, $l_desc, $l_datebegin;
  global $action, $profiles;
  global $c_multidomain, $obm, $c_default_domain, $l_domain;
  
  // if update mode and first time value are taken from database
  if ($action == "detailupdate") {
    $id = $usr_q->f("userobm_id");
    $login = $usr_q->f("userobm_login");
    $passwd = "";
    $perms = $usr_q->f("userobm_perms");
    $lname = $usr_q->f("userobm_lastname");
    $fname = $usr_q->f("userobm_firstname");
    $phone = $usr_q->f("userobm_phone");
    $phone2 = $usr_q->f("userobm_phone2");
    $fax = $usr_q->f("userobm_fax");
    $fax2 = $usr_q->f("userobm_fax2");
    $email = $usr_q->f("userobm_email");
    $desc = $usr_q->f("userobm_description");
    $datebegin = $usr_q->f("userobm_datebegin");
    $archive = ($usr_q->f("userobm_archive") == "1" ? " checked" : "");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($user["user_id"])) { $id = $user["user_id"]; }
  if (isset($user["login"])) { $login = $user["login"]; }
  if (isset($user["passwd"])) { $passwd = $user["passwd"]; }
  if (isset($user["perms"])) { $perms = $user["perms"]; }
  if (isset($user["lastname"])) { $lname = stripslashes($user["lastname"]); }
  if (isset($user["firstname"])) { $fname = stripslashes($user["firstname"]); }
  if (isset($user["phone"])) { $phone = $user["phone"]; }
  if (isset($user["phone2"])) { $phone2 = $user["phone2"]; }
  if (isset($user["fax"])) { $fax = $user["fax"]; }
  if (isset($user["fax2"])) { $fax2 = $user["fax2"]; }
  if (isset($user["email"])) { $email = $user["email"]; }
  if (isset($user["datebegin"])) { $datebegin = $user["datebegin"]; }
  if (isset($user["desc"])) { $desc = $user["desc"]; }
  if (isset($user["archive"])) { $archive = ($user["archive"] == 1 ? "checked" : ""); }

  // Domain
  if ($c_multidomain && $obm["domain_id"] == $c_default_domain) {
    $domain_list = get_global_domain_list();
    $sel_domain = "<tr><th>$l_domain</th><td>
      $domain_list </td></tr>";
  }

  // Profiles
  foreach ($profiles as $profile => $one_profile) {
    if ($perms == $profile) {
      $selected = " selected";
    } else {
      $selected = "";
    }
    $block_pro .= "
      <option value=\"$profile\"$selected> $profile</option>";
  }

  // Buttons
  if (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\" />
          <input type=\"submit\" value=\"$l_insert\" />";

  } elseif (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\" />
          <input type=\"hidden\" name=\"user_id\" value=\"$id\" />
          <input type=\"submit\" value=\"$l_update\" />";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_user(this)) return true; else return false;\"
      action=\"" . url_prepare("user_index.php") . "\">

<fieldset class=\"detail extra\">
    <legend>$l_user</legend>

    <table>
    <tr>
      <th>$l_login</th>
      <td><input name=\"tf_login\" size=\"24\" maxlength=\"32\" value=\"$login\" /></td>
    </tr><tr>
      <th>$l_password</th>
      <td><input type=\"password\" name=\"tf_passwd\" size=\"24\" maxlength=\"32\" value=\"$passwd\" /></td>
    </tr>$sel_domain<tr>
      <th>$l_archive</th>
      <td><input name=\"cba_archive\" type=\"checkbox\" value=\"1\" $archive /></td>
    </tr><tr>
      <th>$l_user_perms</th>
      <td><select name=\"sel_perms\">
        $block_pro
        </select>
      </td>
    </tr><tr>
      <th>$l_lastname</th>
      <td><input name=\"tf_lastname\" size=\"32\" maxlength=\"32\" value=\"$lname\" /></td>
    </tr><tr>
      <th>$l_firstname</th>
      <td><input name=\"tf_firstname\" size=\"32\" maxlength=\"32\" value=\"$fname\" /></td>
    </tr><tr>
      <th>$l_phone</th>
      <td><input name=\"tf_phone\" size=\"20\" maxlength=\"20\" value=\"$phone\" /></td>
    </tr><tr>
      <th>$l_phone2</th>
      <td><input name=\"tf_phone2\" size=\"20\" maxlength=\"20\" value=\"$phone2\" /></td>
    </tr><tr>
      <th>$l_fax</th>
      <td><input name=\"tf_fax\" size=\"20\" maxlength=\"20\" value=\"$fax\" /></td>
    </tr><tr>
      <th>$l_fax2</th>
      <td><input name=\"tf_fax2\" size=\"20\" maxlength=\"20\" value=\"$fax2\" /></td>
    </tr><tr>
      <th>$l_email</th>
      <td><input name=\"tf_email\" size=\"32\" maxlength=\"64\" value=\"$email\" /></td>
    </tr><tr>
      <th>$l_datebegin</th>
      <td>
      <input type=\"text\" name=\"tf_datebegin\" value=\"$datebegin\" class=\"datePicker\"/>
    </tr><tr>
      <th>$l_desc</th>
      <td><input name=\"tf_desc\" size=\"48\" maxlength=\"255\" value=\"$desc\" /></td>
    </tr>
    </table>
 </fieldset>

    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Consultation
// Parameters:
//   - $user : user database result
///////////////////////////////////////////////////////////////////////////////
function dis_user_consult($user) {
  global $display, $l_err_reference;

  $id = $user["user_id"];

  if ($id > 0) {
    $obm_q = run_query_user_detail($id);
    if ($obm_q->num_rows() == 1) {
      $block = html_user_consult($obm_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  return $block;
}




///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation
// Parameters:
//   - $obm_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_consult($obm_q) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname;
  global $l_user, $l_lastaccess, $l_archive, $l_datebegin;
  global $l_phone, $l_phone2, $l_fax, $l_fax2, $l_email, $l_desc;
  global $l_group_member, $l_group, $l_local, $l_ext_id;
  global $path, $display, $c_yes, $c_no;

  $id = $obm_q->f("userobm_id");
  $login = $obm_q->f("userobm_login");
  $local = ($obm_q->f("userobm_local") == 1 ? $c_yes : $c_no);
  $ext_id = $obm_q->f("userobm_ext_id");
  $perms = $obm_q->f("userobm_perms");
  $lname = $obm_q->f("userobm_lastname");
  $fname = $obm_q->f("userobm_firstname");
  $phone = $obm_q->f("userobm_phone");
  $phone2 = $obm_q->f("userobm_phone2");
  $fax = $obm_q->f("userobm_fax");
  $fax2 = $obm_q->f("userobm_fax2");
  $email = $obm_q->f("userobm_email");
  $desc = $obm_q->f("userobm_description");
  $datebegin = $obm_q->f("userobm_datebegin");
  $lastaccess = of_datetime_format($obm_q->f("userobm_timelastaccess"), 1);
  $archive = ($obm_q->f("userobm_archive") == 1 ? $c_yes : $c_no);

  // Liste des groupes de l'utilisateur
  $g_q = run_query_user_user_group($id);
  if ($g_q->num_rows() > 0) {
    $dis_group = "
    <div class=\"detail infos\">
    <h1>$l_group_member</h1>
    <table>
    <tr>";
    while ($g_q->next_record()) {
      $grp_id = $g_q->f("group_id");
      $grp_name = $g_q->f("group_name");
      $dis_group .= "
    </tr><tr>
      <th>$l_group</th>
      <td><a href=\"$path/group/group_index.php?action=detailconsult&amp;group_id=$grp_id\">$grp_name</a></td>";
    }
    $dis_group .= "
    </tr>
    </table></div>";
  }

  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "<h1 class=\"title\">$l_user : $fname $lname</h1>";

  $block = "
<div class=\"detail infos\">
  <h1>$l_user</h1>
 
    <table>
    <tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr><tr>
      <th>$l_login</th>
      <td>$login</td>
    </tr><tr>
      <th>$l_local</th>
      <td>$local</td>
    </tr><tr>
      <th>$l_ext_id</th>
      <td>$ext_id</td>
    </tr><tr>
      <th>$l_user_perms</th>
      <td>$perms</td>
    </tr><tr>
      <th>$l_lastname</th>
      <td>$lname</td>
    </tr><tr>
      <th>$l_firstname</th>
      <td>$fname</td>
    </tr><tr>
      <th>$l_phone</th>
      <td>$phone</td>
    </tr><tr>
      <th>$l_phone2</th>
      <td>$phone2</td>
    </tr><tr>
      <th>$l_fax</th>
      <td>$fax</td>
    </tr><tr>
      <th>$l_fax2</th>
      <td>$fax2</td>
    </tr><tr>
      <th>$l_email</th>
      <td>$email</td>
    </tr><tr>
      <th>$l_datebegin</th>
      <td>$datebegin</td>
    </tr><tr>
      <th>$l_lastaccess</th>
      <td>$lastaccess</td>
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
    $dis_group
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Group form
// Parameters:
//   - $usr_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_group_consult($usr_q) {
  global $l_update_group, $l_user, $l_group_member, $l_group;

  $id = $usr_q->f("userobm_id");
  $login = $usr_q->f("userobm_login");
  $lname = $usr_q->f("userobm_lastname");
  $fname = $usr_q->f("userobm_firstname");

  // Liste des groupes
  $g_q = run_query_user_group($id);

  // Liste des groupes de l'utilisateur : on remplit le tableau $groups
  $gu_q = run_query_user_user_group($id);
  $groups = array();
  while ($gu_q->next_record()) {
    $g_id = $gu_q->f("userobmgroup_group_id");
    array_push($groups, $g_id);
  }

  while ($g_q->next_record()) {
    $grp_id = $g_q->f("group_id");
    $grp_name = $g_q->f("group_name");
    $checked = (in_array($grp_id, $groups) ? " checked" : "" );
    $dis_group .= "
    <tr>
      <th>$grp_name</th>
      <td><input name=\"data-g-$grp_id\" type=\"checkbox\" value=\"1\" $checked></td>
    </tr>";
  }

  $block = "
    <form method=\"post\" name=\"f_entity\"
      action=\"" . url_prepare("user_index.php") . "\">

  <h1>$l_user $login ($lname $fname) $l_group_member</h1>
  <table>
  $dis_group
  </table>

  <p>
  <input type=\"hidden\" name=\"user_id\" value=\"$id\">
  <input type=\"hidden\" name=\"action\" value=\"group_update\">
  <input type=\"submit\" value=\"$l_update_group\">
  </form>";

  return $block;
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

  $login = $user["login"];
  $passwd = $user["passwd"];
  $perms = $user["perms"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $datebegin = $user["datebegin"];
  $phone = $user["phone"];
  $phone2 = $user["phone2"];
  $fax = $user["fax"];
  $fax2 = $user["fax2"];
  $email = $user["email"];
  $desc = $user["desc"];
  $archive = $user["archive"];

  $display["msg"] .= display_warn_msg($l_check_sameuser);
  while ($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
    $samename = $usr_q->f("userobm_login");
    $dis_same_user .= "
        <li>
          <a href=\"" . url_prepare("user_index.php?action=detailconsult&amp;user_id=$id") . "\">$samename</a>
        </li>";
  }

  $hidden_fields = "
    <input type=\"hidden\" name=\"tf_login\" value=\"$login\" />
    <input type=\"hidden\" name=\"tf_passwd\" value=\"$passwd\" />
    <input type=\"hidden\" name=\"sel_perms\" value=\"$perms\" />
    <input type=\"hidden\" name=\"tf_lastname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_firstname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"tf_datebegin\" value=\"$datebegin\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_phone2\" value=\"$phone2\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_fax2\" value=\"$fax2\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"tf_desc\" value=\"$desc\" />
    <input type=\"hidden\" name=\"cba_archive\" value=\"$archive\" />";

  $block = "
  <div class=\"detail\">
    <ul>
    $dis_same_user
    </ul>
  </div>
  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" . url_prepare("user_index.php") . "\">
    $hidden_fields
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    <form name=\"form_back\" method=\"post\"
    action=\"" . url_prepare("user_index.php") . "\">
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

  $url = url_prepare("user_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"user_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  // Display entities where the user is referenced
  $obm_q = new DB_OBM;
  $tables = $obm_q->table_names();

  $i = 0;
  while ($tables[$i]["table_name"]) {
    $table = $tables[$i]["table_name"];

    // We exclude POSTGRES meta table (sql_xxxx)
    if (! (substr($table, 0, 4) == "sql_")) {
      $usercreate = $table . "_usercreate";
      $userupdate = $table . "_userupdate";

      $obm_qm = new DB_OBM;
      $fields = $obm_qm->metadata($table, false);
      $fieldexist_uc = false;
      $fieldexist_uu = false;
      $j = 0;
      while ($fields[$j]["name"]) {
	if (strtoupper($fields[$j]["name"]) == strtoupper($usercreate)) {
	  $fieldexist_uc = true;
	}
	if (strtoupper($fields[$j]["name"]) == strtoupper($userupdate)) {
	  $fieldexist_uu = true;
	}
	$j++;
      }
      if ($fieldexist_uc) {
	$query = "SELECT count(*) FROM $table WHERE $usercreate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbc = $c_q->f("0");
      }
      if ($fieldexist_uu) {
	$query = "SELECT count(*) FROM $table WHERE $userupdate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbu = $c_q->f("0");
      }
      if ($fieldexist_uc || $fieldexist_uu) {
	$dis_user .= "
       <h1>$table</h1>
       <ul>
        <li><b>$nbc</b> $l_check_usercreate</li>
        <li><b>$nbu</b> $l_check_userupdate</li>
	</ul>
      ";
      } else {
	$dis_user .= "
        <h1>$table</h1>
	<ul>
        <li>$l_no_user</li>
	</ul>
	";
      }
      $dis_user .= "<hr />";
    }
    $i++;
  }
 
  $block .= "
    <div class=\"buttons\">
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
// Display: the User Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_user_display_pref($prefs) {
  global $l_user_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "user");
  $dis_pref->pref_title = $l_user_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


?>
