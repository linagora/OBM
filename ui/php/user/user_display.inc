<script language="php">
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
$fieldnames["userobm_perms"] = $l_user_perms;
$fieldnames["userobm_archive"] = $l_archive;
$fieldnames["userobm_lastname"] = $l_lastname;
$fieldnames["userobm_firstname"] = $l_firstname;
$fieldnames["userobm_phone"] = $l_phone;
$fieldnames["userobm_timelastaccess"] = $l_lastaccess;
$fieldnames["userobm_email"] = $l_email;


///////////////////////////////////////////////////////////////////////////////
// Display User specific dataset fields                                      //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_user(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail;

  if (($fieldname == "userobm_login") && $link_ok) {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("userobm_id");
  }
  else if ($fieldname == "userobm_email") {
    $email = $OD->data_set->f("userobm_email");
    if (strcmp($email ,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\">$email";
    }
  } 
  else if ($fieldname == "userobm_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) $res["name"] = "X";
    else $res["name"] = "&nbsp;";
  }
  else if ($fieldname == "userobm_timelastaccess") {
    $date = $OD->data_set->f($fieldname);
    $res["name"] = datetime_format($date, 1);
  }

  return $res;

}


///////////////////////////////////////////////////////////////////////////////
// Display User search form                                                  //
// Parameters:
//   - $user[]   : default form values
//     keys used : login, lastname, perms, archive, popup
///////////////////////////////////////////////////////////////////////////////
function html_user_search_form($user) {
  global $l_login, $l_lastname, $l_user_perms, $l_find, $l_all, $l_archive;
  global $perms_user, $perms_admin, $perms_editor;
  global $l_perms_user, $l_perms_admin, $l_perms_editor;
  global $display, $c_all;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $archive = ($user["archive"] == "1" ? "checked" : "");
  $popup = $user["popup"];

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_title = $user["ext_title"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  $block = "
  <form method=\"get\" name=\"f_user\"
    action=\"". url_prepare("user_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_login<br />
      <input type=\"text\" name=\"tf_login\" size=\"20\" maxlength=\"32\"
      value=\"$login\" />
    </div>
    <div class=\"searchLabel\">$l_lastname<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"20\" maxlength=\"32\"
      value=\"$lname\" />
    </div>
    <div class=\"searchLabel\">$l_user_perms<br />
      <select type=\"text\" name=\"sel_perms\">
        <option value=\"$c_all\" /> $l_all
        <option value=\"$perms_user\" /> $l_perms_user
        <option value=\"$perms_editor\" /> $l_perms_editor
        <option value=\"$perms_admin\" /> $l_perms_admin
      </select>
    </div>
    <div class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive>
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\">
      <input name=\"submit\" type=\"submit\" value=\"$l_find\">
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp;
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result                                            //
// Parameters:
//   - $user[]   : user search criteria
//     keys used : name, perms
///////////////////////////////////////////////////////////////////////////////
function dis_user_search_list($user) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $popup = $user["popup"];

  $pref_q = run_query_display_pref($auth->auth["uid"], "user");
  $obm_q = run_query_search($user, $new_order, $order_dir);
  $nb_user = $obm_q->num_rows();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_user_search_list($obm_q, $pref_q, $nb_user, $user, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the User Search result                                       //
// Parameters:
//   - $obm_q    : database result (user list)
//   - $pref_q   : the fields which have to be displayed
//   - $nb_user  : nb users returned by the search query
//   - $user[]   : user search criteria
//     keys used : login, lastname, pemrs
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_user_search_list($obm_q, $pref_q, $nb_user, $user, $popup) {
  global $display, $l_found, $l_close, $l_add;

  $login = urlencode($user["login"]);
  $lname = urlencode($user["lastname"]);
  $perms = $user["perms"];
  $archive = $user["archive"];

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  $url = url_prepare("user_index.php?action=search&amp;tf_login=$login&amp;$tf_lastname=$lname&amp;sel_perms=$perms&amp;cb_archive=$archive$url_ext");

  $user_d = new OBM_DISPLAY("DATA", $pref_q, "user");
  if ($popup) {
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "userobm_id";
    $user_d->data_cb_name = "cb_u";
    $display_popup_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $display_popup_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_info_msg("$nb_user $l_found");
  $block = $display_popup_head;
  $block .= $user_d->display("dis_data_user");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User search form                                                  //
// Parameters:
//   - $user[]   : default form values
//     keys used : login, lastname, perms, archive, popup
///////////////////////////////////////////////////////////////////////////////
function html_get_user_search_form($user) {
  global $l_login, $l_lastname, $l_user_perms, $l_find, $l_all, $l_archive;
  global $perms_user, $perms_admin, $perms_editor;
  global $l_perms_user, $l_perms_admin, $l_perms_editor;
  global $display, $c_all;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $archive = ($user["archive"] == "1" ? "checked" : "");
  $popup = $user["popup"];

  $ext_action = $user["ext_action"];
  $ext_title = $user["ext_title"];
  $ext_url = $user["ext_url"];
  $ext_id = $user["ext_id"];
  $ext_target = $user["ext_target"];
  $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
	  <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
	  <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
	  <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
	  <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
  $display["msg"] .= display_info_msg($ext_title);

  $block = "
  <form method=\"get\" name=\"f_user\"
    action=\"". url_prepare("user_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_login<br />
      <input type=\"text\" name=\"tf_login\" size=\"20\" maxlength=\"32\"
      value=\"$login\" />
    </div>
    <div class=\"searchLabel\">$l_lastname<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"20\" maxlength=\"32\"
      value=\"$lname\" />
    </div>
    <div class=\"searchLabel\">$l_user_perms<br />
      <select type=\"text\" name=\"sel_perms\">
        <option value=\"$c_all\" /> $l_all
        <option value=\"$perms_user\" /> $l_perms_user
        <option value=\"$perms_editor\" /> $l_perms_editor
        <option value=\"$perms_admin\" /> $l_perms_admin
      </select>
    </div>
    <div class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive>
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"getsearch\">
      <input name=\"submit\" type=\"submit\" value=\"$l_find\">
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp;
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result                                            //
// Parameters:
//   - $user[]   : user search criteria
//     keys used : name, perms
///////////////////////////////////////////////////////////////////////////////
function dis_get_user_search_list($user) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $popup = $user["popup"];

  $pref_q = run_query_display_pref($auth->auth["uid"], "user");
//  $obm_q = run_query_get_search($user, $new_order, $order_dir);
  $obm_q = run_query_search($user, $new_order, $order_dir);
  $nb_user = $obm_q->num_rows();
  if ($nb_user == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_get_user_search_list($obm_q, $pref_q, $nb_user, $user, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the User Search result                                       //
// Parameters:
//   - $obm_q    : database result (user list)
//   - $pref_q   : the fields which have to be displayed
//   - $nb_user  : nb users returned by the search query
//   - $user[]   : user search criteria
//     keys used : login, lastname, pemrs
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_get_user_search_list($obm_q, $pref_q, $nb_user, $user, $popup) {
  global $display, $l_found, $l_close, $l_add;

  $login = urlencode($user["login"]);
  $lname = urlencode($user["lastname"]);
  $perms = $user["perms"];
  $archive = $user["archive"];

  $ext_target = $user["ext_target"];

  $user_d = new OBM_DISPLAY("DATA", $pref_q, "user");
  if ($popup) {
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "userobm_id";
    $user_d->data_cb_name = "cb_u";
    $display_popup_head = "
      <form onsubmit=\"fill_ext_form(this);return false;\">";
    $display_popup_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
	<input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
	<input name=\"popup\" type=\"hidden\" value=\"1\" />
	<input name=\"action\" type=\"hidden\" value=\"ext_get_users\">
        <input type=\"submit\" value=\"$l_add\" />
        </p>
      </p>
      </form>
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $dislay["msg"] .= display_info_msg("$nb_user $l_found");
  $block = $display_popup_head;
  $block .= $user_d->display("dis_data_user");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display User Form                                                         //
// Parameters:
//   - $first_try : 1: first time, 0 : redisplay the form (error)
//   - $usr_q     : user database result
//   - $user      : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
///////////////////////////////////////////////////////////////////////////////
function html_user_form($first_try, $usr_q, $user) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname;
  global $l_phone, $l_user, $l_email, $l_archive, $l_insert, $l_update;
  global $perms_user,$perms_editor,$perms_admin,$l_perms_user,$l_perms_editor,$l_perms_admin ;
  global $action;

  // if update mode and first time value are taken from database
  if (($action == "detailupdate") && ($first_try == 1)) {
    $id = $usr_q->f("userobm_id");
    $login = $usr_q->f("userobm_login");
    $passwd = "";
    $perms = $usr_q->f("userobm_perms");
    $lname = $usr_q->f("userobm_lastname");
    $fname = $usr_q->f("userobm_firstname");
    $phone = $usr_q->f("userobm_phone");
    $email = $usr_q->f("userobm_email");
    $archive = ($usr_q->f("userobm_archive") == "1" ? " checked" : "");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($user["id"])) { $id = $user["id"]; }
  if (isset($user["login"])) { $login = $user["login"]; }
  if (isset($user["passwd"])) { $passwd = $user["passwd"]; }
  if (isset($user["perms"])) { $perms = $user["perms"]; }
  if (isset($user["lastname"])) { $lname = stripslashes($user["lastname"]); }
  if (isset($user["firstname"])) { $fname = stripslashes($user["firstname"]); }
  if (isset($user["phone"])) { $phone = $user["phone"]; }
  if (isset($user["email"])) { $email = $user["email"]; }
  if (isset($user["archive"])) { $archive = ($user["archive"] == 1 ? "checked" : ""); }


  // perms selectconstruction
  if ($perms == $perms_user) $perms_user_s = " selected";
  if ($perms == $perms_editor) $perms_editor_s = " selected";
  if ($perms == $perms_admin) $perms_admin_s = " selected";

  // Buttons
  if (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"insert\">
          <input type=\"submit\" value=\"$l_insert\">";

  } elseif (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\">
          <input type=\"hidden\" name=\"param_user\" value=\"$id\">
          <input type=\"submit\" value=\"$l_update\">";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
    <form method=\"post\" name=\"form_user\"
      onsubmit=\"if (check_user(this)) return true; else return false;\"
      action=\"" . url_prepare("user_index.php") . "\">

    <div class=\"detailHead\">$l_user</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input name=\"cb_archive\" type=\"checkbox\" value=\"1\" $archive /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_login</td>
      <td class=\"detailForm\"><input name=\"tf_login\" size=\"24\" maxlength=\"32\" value=\"$login\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_password</td>
      <td class=\"detailForm\"><input name=\"tf_passwd\" size=\"24\" maxlength=\"32\" value=\"$passwd\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_user_perms</td>
      <td class=\"detailForm\"><select name=\"sel_perms\">
        <option value=\"$perms_user\" $perms_user_s>$l_perms_user
        <option value=\"$perms_editor\" $perms_editor_s>$l_perms_editor
        <option value=\"$perms_admin\" $perms_admin_s>$l_perms_admin
        </select>
      </td>
    </tr><tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td class=\"detailForm\"><input name=\"tf_lastname\" size=\"32\" maxlength=\"32\" value=\"$lname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td class=\"detailForm\"><input name=\"tf_firstname\" size=\"32\" maxlength=\"32\" value=\"$fname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_phone</td>
      <td class=\"detailForm\"><input name=\"tf_phone\" size=\"20\" maxlength=\"20\" value=\"$phone\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailForm\"><input name=\"tf_email\" size=\"32\" maxlength=\"64\" value=\"$email\" /></td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation                                            //
// Parameters:
//   - $obm_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_consult($obm_q) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname;
  global $l_user, $l_phone, $l_email, $l_lastaccess, $l_archive;
  global $l_insert,$l_update,$l_delete;
  global $perms_user,$perms_editor,$perms_admin,$l_perms_user,$l_perms_editor,$l_perms_admin ;
  global $display, $action, $c_yes, $c_no;

  $id = $obm_q->f("userobm_id");
  $login = $obm_q->f("userobm_login");
  $paswd = $obm_q->f("userobm_password");
  $perms = $obm_q->f("userobm_perms");
  $lname = $obm_q->f("userobm_lastname");
  $fname = $obm_q->f("userobm_firstname");
  $phone = $obm_q->f("userobm_phone");
  $email = $obm_q->f("userobm_email");
  $lastaccess = datetime_format($obm_q->f("userobm_timelastaccess"), 1);
  $archive = ($obm_q->f("userobm_archive") == 1 ? $c_yes : $c_no);

  if ($perms == $perms_user) $l_perms = $l_perms_user;
  if ($perms == $perms_editor) $l_perms = $l_perms_editor;
  if ($perms == $perms_admin) $l_perms = $l_perms_admin;

  $display["detailInfo"] = display_record_info($obm_q);
  $display["title"] = "<div class=\"title\">$l_user : $fname $lname</div>";

  $block = "
  <div class=\"detailHead\">$l_user</div>
 
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_login</td>
      <td class=\"detailText\">$login</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_password</td>
      <td class=\"detailText\">$passwd</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_user_perms</td>
      <td class=\"detailText\">$l_perms</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td class=\"detailText\">$lname</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td class=\"detailText\">$fname</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_phone</td>
      <td class=\"detailText\">$phone</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_lastaccess</td>
      <td class=\"detailText\">$lastaccess</td>
    </tr>
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a user insertion or update                      //
// When similar users exists we show these and ask confirmation
// Parameters:
//   - $p_id     : user id
//   - $usr_q    : user database result (at least 1 row)
//   - $user[]   : default values
//     keys used : login, passwd, perms, lastname, firstname, archive, email
/////////////////////////////////////////////////////////////////////////////
function dis_user_warn_insert($p_id, $usr_q, $user) {
  global $l_check_sameuser, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  $login = $user["login"];
  $passwd = $user["passwd"];
  $perms = $user["perms"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $phone = $user["phone"];
  $email = $user["email"];
  $archive = $user["archive"];

  $display["msg"] .= display_warn_msg($l_check_sameuser);
  while ($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
    $samename = $usr_q->f("userobm_login");
    $dis_same_user .= "
        <tr><td colspan =\"2\" class=\"detailLabel\">
          <a href=\"" . url_prepare("user_index.php?action=detailconsult&amp;param_user=$id") . "\">$samename</a>
        </td></tr>";
  }

  $block = "
  <table class=\"detail\">
    $dis_same_user
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" . url_prepare("user_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"hidden\" name=\"tf_login\" value=\"$login\" />
    <input type=\"hidden\" name=\"tf_passwd\" value=\"$passwd\" />
    <input type=\"hidden\" name=\"sel_perms\" value=\"$perms\" />
    <input type=\"hidden\" name=\"tf_lastname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_firstname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
    <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back\" method=\"post\"
    action=\"" . url_prepare("user_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"hidden\" name=\"tf_login\" value=\"$login\" />
    <input type=\"hidden\" name=\"tf_passwd\" value=\"$passwd\" />
    <input type=\"hidden\" name=\"sel_perms\" value=\"$perms\" />
    <input type=\"hidden\" name=\"tf_lastname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_firstname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays and check the user links (and delete form if ok)                 //
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_no_user, $l_check_usercreate, $l_check_userupdate;

  $delete_ok = true;

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
	$query = "select count(*) from $table where $usercreate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbc = $c_q->f("0");
      }
      if ($fieldexist_uu) {
	$query = "select count(*) from $table where $userupdate='$p_id'";
	$c_q = new DB_OBM($query);
	$c_q->next_record();
	$nbu = $c_q->f("0");
      }
      if ($fieldexist_uc || $fieldexist_uu) {
	$dis_user .= "
      <tr>
        <td class=\"detailHead\" rowspan=\"2\">$table</td>
        <td class=\"detailText\"><b>$nbc</b> $l_check_usercreate</td>
      </tr><tr>
        <td class=\"detailText\"><b>$nbu</b> $l_check_userupdate</td>
      </tr>";
      } else {
	$dis_user .= "
      <tr>
        <td class=\"detailHead\">$table</td>
        <td class=\"detailText\">$l_no_user</td>
      </tr>";
      }
      $dis_user .= "<tr>
      <td colspan=\"2\"><hr /></td>
    </tr>";
    }
    $i++;
  }
 
  $block = "<table class=\"detail\">
    $dis_user
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
   " . dis_delete_form($p_id) . "
    </p>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : user Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $block = "<form name=\"form_delete\"
      method=\"post\" action=\"" . url_prepare("user_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_user\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onClick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the User Display preference screen                               //
// Parameters:
//   - $pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_user_display_pref($pref_q) {
  global $l_user_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "user"; 
  $dis_pref->pref_title = $l_user_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


</SCRIPT>
