<SCRIPT language="php">
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
$fieldnames["userobm_timelastaccess"] = $l_lastaccess;
$fieldnames["userobm_email"] = $l_email;

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
  global $c_all;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $archive = ($user["archive"] == "1" ? "checked" : "");
  $popup = $user["popup"];

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
  }

  echo "
  <form method=\"get\" name=\"f_user\"
    action=\"". url_prepare("user_index.php")."\">
  <table class=\"details\">
  <tr>
    <td class=\"textLabelForm\">$l_login<br />
    <input type=\"text\" name=\"tf_login\" size=\"20\" maxlength=\"32\"
      value=\"$login\" /></td>
    <td>&nbsp;&nbsp;</td>
    <td class=\"textLabelForm\">$l_lastname<br />
    <input type=\"text\" name=\"tf_lastname\" size=\"20\" maxlength=\"32\"
      value=\"$lname\" /></td>
    <td>&nbsp;&nbsp;</td>
    <td  class=\"textLabelForm\">$l_user_perms<br />
      <select type=\"text\" name=\"sel_perms\">
        <option value=\"$c_all\" /> $l_all
        <option value=\"$perms_user\" /> $l_perms_user
        <option value=\"$perms_editor\" /> $l_perms_editor
        <option value=\"$perms_admin\" /> $l_perms_admin
      </select>
    </td>
    <td>&nbsp;&nbsp;</td>
    <td class=\"textLabelForm\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive>
    </td>
    <td valign=bottom>&nbsp;&nbsp;
      <input name=\"action\" type=\"hidden\" value=\"search\">
      <input name=\"submit\" type=\"submit\" value=\"$l_find\">
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp;
    </td>
  </tr>
  </table>
  </form>
  <hr />";
}


///////////////////////////////////////////////////////////////////////////////
// Display the User search result                                            //
// Parameters:
//   - $user[]   : user search criteria
//     keys used : name, perms
///////////////////////////////////////////////////////////////////////////////
function dis_user_search_list($user) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $popup = $user["popup"];

  $pref_q = run_query_display_pref($auth->auth["uid"], "user");
  $obm_q = run_query_search($user, $new_order, $order_dir);
  $nb_user = $obm_q->num_rows();
  if ($nb_user == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_user_search_list($obm_q, $pref_q, $nb_user, $user, $popup);
  }
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
  global $l_found, $l_close;

  $login = urlencode($user["login"]);
  $lname = urlencode($user["lastname"]);
  $perms = $user["perms"];
  $archive = $user["archive"];

  if ($popup) {
    $ext_action = $user["ext_action"];
    $ext_url = $user["ext_url"];
    $ext_id = $user["ext_id"];
    $ext_target = $user["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }

  $url = url_prepare("user_index.php?action=search&amp;tf_login=$login&amp;$tf_lastname=$lname&amp;sel_perms=$perms&amp;cb_archive=$archive&amp;$url_ext");

  $user_d = new OBM_DISPLAY("DATA", $pref_q);
  if ($popup) {
    $user_d->display_link = false;
    $user_d->data_cb_text = "X";
    $user_d->data_idfield = "userobm_id";
    $user_d->data_cb_name = "cb_user";
    $display_popup_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $display_popup_end = "
      <table class=\"details\">
      <tr>
        <td>
      <input type=\"submit\" value=\"$l_add\" />
      <input type=\"hidden\" name=\"param_ext\" value=\"$ext_id\" />
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </td>
      </tr>
      </table>
      </form>
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $user_d->data_set = $obm_q;
  $user_d->data_header = "both";
  $user_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  display_info_msg("$nb_user $l_found");
  echo $display_popup_head;
  $user_d->display();
  echo $display_popup_end;

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
  global $l_user, $l_email, $l_archive, $l_insert, $l_update;
  global $perms_user,$perms_editor,$perms_admin,$l_perms_user,$l_perms_editor,$l_perms_admin ;
  global $action;

  // if update mode and first time value are taken from database
  if (($action == "detailupdate") && ($first_try == 1)) {
    $id = $usr_q->f("userobm_id");
    $login = $usr_q->f("userobm_login");
    $passwd = $usr_q->f("userobm_password");
    $perms = $usr_q->f("userobm_perms");
    $lname = $usr_q->f("userobm_lastname");
    $fname = $usr_q->f("userobm_firstname");
    $email = $usr_q->f("userobm_email");
    $archive = ($usr_q->f("userobm_archive") == "1" ? " checked" : "");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($user["id"])) { $id = $user["id"]; }
  if (isset($user["login"])) { $login = $user["login"]; }
  if (isset($user["passwd"])) { $passwd = $user["passwd"]; }
  if (isset($user["perms"])) { $perms = $user["perms"]; }
  if (isset($user["lname"])) { $lname = stripslashes($user["lname"]); }
  if (isset($user["fname"])) { $fname = stripslashes($user["fname"]); }
  if (isset($user["email"])) { $email = $user["email"]; }
  if (isset($user["archive"])) { $archive = ($user["archive"] == 1 ? "checked" : ""); }


  // perms selectconstruction
  if ($perms == $perms_user) $perms_user_s = " selected";
  if ($perms == $perms_editor) $perms_editor_s = " selected";
  if ($perms == $perms_admin) $perms_admin_s = " selected";

  // Buttons
  if (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=hidden name=action value=\"insert\">
          <input type=submit value=\"$l_insert\">";

  } elseif (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=hidden name=action value=\"update\">
          <input type=hidden name=param_user value=\"$id\">
          <input type=submit value=\"$l_update\">
      </form>";
  }

  // --- HTML Template --------------------------------------------------------
  echo "
    <form method=\"post\" name=\"form_user\"
      onSubmit=\"if (check_user(this)) return true; else return false;\"
      action=\"" . url_prepare("user_index.php") . "\">
    <table class=\"details\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">$l_user</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td><input name=\"cb_archive\" type=\"checkbox\" value=\"1\" $archive /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_login</td>
      <td><input name=\"tf_login\" size=\"24\" maxlength=\"32\" value=\"$login\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_password</td>
      <td><input name=\"tf_passwd\" size=\"24\" maxlength=\"32\" value=\"$passwd\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_user_perms</td>
      <td><select name=sel_perms>
        <option value=\"$perms_user\" $perms_user_s>$l_perms_user
        <option value=\"$perms_editor\" $perms_editor_s>$l_perms_editor
        <option value=\"$perms_admin\" $perms_admin_s>$l_perms_admin
        </select>
      </td>
    </tr><tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td><input name=\"tf_lastname\" size=\"32\" maxlength=\"32\" value=\"$lname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td><input name=\"tf_firstname\" size=\"32\" maxlength=\"32\" value=\"$fname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td><input name=\"tf_email\" size=\"32\" maxlength=\"64\" value=\"$email\" /></td>
    </tr>
    </table>
    <p />
    <center>
    $dis_button
    </center>
    </form>";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation                                            //
// Parameters:
//   - $obm_q : user database result
///////////////////////////////////////////////////////////////////////////////
function html_user_consult($obm_q) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname;
  global $l_user, $l_email, $l_lastaccess, $l_archive;
  global $l_insert,$l_update,$l_delete;
  global $perms_user,$perms_editor,$perms_admin,$l_perms_user,$l_perms_editor,$l_perms_admin ;
  global $action, $c_yes, $c_no;

  $id = $obm_q->f("userobm_id");
  $login = $obm_q->f("userobm_login");
  $paswd = $obm_q->f("userobm_password");
  $perms = $obm_q->f("userobm_perms");
  $lname = $obm_q->f("userobm_lastname");
  $fname = $obm_q->f("userobm_firstname");
  $email = $obm_q->f("userobm_email");
  $lastaccess = datetime_format($obm_q->f("userobm_timelastaccess"));
  $archive = ($obm_q->f("userobm_archive") == 1 ? $c_yes : $c_no);

  if ($perms == $perms_user) $l_perms = $l_perms_user;
  if ($perms == $perms_editor) $l_perms = $l_perms_editor;
  if ($perms == $perms_admin) $l_perms = $l_perms_admin;

  echo "
    <table class=\"details\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">$l_user</td>
    </tr><tr>
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
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_lastaccess</td>
      <td class=\"detailText\">$lastaccess</td>
    </tr>
    </table>
  ";
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
  global $c_yes, $c_no;

  $login = $user["login"];
  $passwd = $user["passwd"];
  $perms = $user["perms"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $email = $user["email"];
  $archive = $user["archive"];

  echo $l_check_sameuser;
  while ($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
    $samename = $usr_q->f("userobm_login");
    echo "<BR><A HREF=\"" . url_prepare("user_index.php?action=detailconsult&amp;param_user=$id") . "\">$samename</A>";
  }

  echo "<P>
    <TABLE><TR><TD>
    <FORM METHOD=POST NAME=form_insert
    ACTION=\"" . url_prepare("user_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=insert>
    <INPUT TYPE=hidden NAME=hd_confirm VALUE=\"$c_yes\">
    <INPUT TYPE=hidden NAME=tf_login VALUE=\"$login\">
    <INPUT TYPE=hidden NAME=tf_passwd VALUE=\"$passwd\">
    <INPUT TYPE=hidden NAME=sel_perms VALUE=\"$perms\">
    <INPUT TYPE=hidden NAME=tf_lastname VALUE=\"$lname\">
    <INPUT TYPE=hidden NAME=tf_firstname VALUE=\"$fname\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <input type=hidden name=cb_archive value=\"$archive\">
    <INPUT TYPE=submit NAME=submit VALUE=\"$l_confirm\">
    </FORM>
    </TD><TD>
    <FORM NAME=form_back METHOD=POST
    ACTION=\"" . url_prepare("user_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=new>
    <INPUT TYPE=hidden NAME=tf_login VALUE=\"$login\">
    <INPUT TYPE=hidden NAME=tf_passwd VALUE=\"$passwd\">
    <INPUT TYPE=hidden NAME=sel_perms VALUE=\"$perms\">
    <INPUT TYPE=hidden NAME=tf_lastname VALUE=\"$lname\">
    <INPUT TYPE=hidden NAME=tf_firstname VALUE=\"$fname\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <input type=hidden name=cb_archive value=\"$archive\">
    <INPUT TYPE=submit VALUE=\"$l_back\">
    </FORM>
    </TD></TR>
    </TABLE>";
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

  echo "<table class=\"details\">";
  $i = 0;
  while ($tables[$i]["table_name"]) {

    $table = $tables[$i]["table_name"];
    $usercreate = $table . "_usercreate";
    $userupdate = $table . "_userupdate";

    $obm_qm = new DB_OBM;
    $fields = $obm_qm->metadata($table, false);
    $fieldsexist = false;
    $j = 0;
    while ($fields[$j]["name"]) {
      if (strtoupper($fields[$j]["name"]) == strtoupper($usercreate)) {
        $fieldsexist = true;
      }
      $j++;
    }
    if ($fieldsexist) {
      $query = "select count(*) from $table where $usercreate='$p_id'";
      $c_q = new DB_OBM($query);
      $c_q->next_record();
      $nbc = $c_q->f("0");
      $query = "select count(*) from $table where $userupdate='$p_id'";
      $c_q = new DB_OBM($query);
      $c_q->next_record();
      $nbu = $c_q->f("0");

      echo "
      <tr>
        <td class=\"detailHead\" rowspan=\"2\">$table</td>
        <td class=\"detailText\"><b>$nbc</b> $l_check_usercreate</td>
      </tr><tr>
        <td class=\"detailText\"><b>$nbu</b> $l_check_userupdate</td>
      </tr>";
    } else {
      echo "
      <tr>
        <td class=\"detailHead\">$table</td>
        <td class=\"detailText\">$l_no_user</td>
      </tr>";
    }
    echo "<tr>
      <td colspan=\"2\"><hr /></td>
    </tr>";
    $i++;
  }
 
  echo "</table>";

  dis_delete_form($p_id);
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : user Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  echo "<form name=\"form_delete\"
      method=post action=\"" . url_prepare("user_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\">
    <input type=\"hidden\" name=\"param_user\" value=\"$p_id\">
    <input type=\"submit\" value=\"$l_delete\"
    onClick=\"if (confirm_del(this.form)) return true; else return false;\">
    </form>";
}



</SCRIPT>
