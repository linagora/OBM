<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_display.inc                                             //
//     - Desc : User Display functions File                                  //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display User search form                                                  //
// Parameters:
//   - $user[]   : default form values
//     keys used : login, lastname, perms, archive
///////////////////////////////////////////////////////////////////////////////
function html_user_search_form($user) {
  global $l_login, $l_lastname, $l_user_perms, $l_find, $l_all, $l_archive;
  global $perms_user, $perms_admin, $perms_editor;
  global $l_perms_user, $l_perms_admin, $l_perms_editor;
  global $c_all, $sess;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $archive = ($user["archive"] == "1" ? "checked" : "");

  echo "<center>
  <form method=get name=f_user action=\"".$sess->url("user_index.php")."\">
  <table border=0>
  <tr>
    <td><font size=-2>$l_login</font><br>
    <input type=text name=tf_login size=20 maxlength=32 value=\"$login\"></td>
    <td>&nbsp;&nbsp;</td>
    <td><font size=-2>$l_lastname</font><br>
    <input type=text name=tf_lastname size=20 maxlength=32 value=\"$lname\"></td>
    <td>&nbsp;&nbsp;</td>
    <td align=left><font size=-2> $l_user_perms </font><br>
      <select type=text name=sel_perms>
        <option value=\"$c_all\"> $l_all
        <option value=\"$perms_user\"> $l_perms_user
        <option value=\"$perms_editor\"> $l_perms_editor
        <option value=\"$perms_admin\"> $l_perms_admin
      </select>
    </td>
    <td>&nbsp;&nbsp;</td>
    <td align=center><font size=-2>$l_archive</font><br>
      <input type=checkbox name=cb_archive value=1 $archive>
    </td>
    <td valign=bottom>&nbsp;&nbsp;
    <input name=\"action\" type=hidden value=\"search\">
    <input name=\"submit\" type=submit value=\"$l_find\">&nbsp;&nbsp;</td>
  </tr>
  </table>

  </form><hr>
  </center>";
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

  $pref_q = run_query_display_pref($auth->auth["uid"], "user");
  $obm_q = run_query_search($user, $new_order, $order_dir);
  $nb_user = $obm_q->num_rows();
  if ($nb_user == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_user_search_list($obm_q, $pref_q, $nb_user, $user);
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
///////////////////////////////////////////////////////////////////////////////
function html_user_search_list($obm_q, $pref_q, $nb_user, $user) {
  global $sess, $col_textem, $l_found;

  $login = $user["login"];
  $lname = $user["lastname"];
  $perms = $user["perms"];
  $archive = $user["archive"];

  echo "<font color=\"#$col_textem\">$nb_user $l_found</font><br>";

  $url = $sess->url("user_index.php?action=search&amp;tf_login=$login&amp;$tf_lastname=$lname&amp;sel_perms=$perms&amp;cb_archive=$archive");

  $user_d = new OBM_DISPLAY("DATA", $pref_q);
  $user_d->data_set = $obm_q;
  $user_d->data_url = $url;
  $user_d->data_header = "both";
  $user_d->display();

}


///////////////////////////////////////////////////////////////////////////////
// Display User Form                                                         //
// Parameters:
//   - $first_try : 1: first time, 0 : redisplay the form (error)
//   - $usr_q     : user database result
//   - $user      : default values or updated values (if error)
//     kes used   : id, name, passwd, perms, email
//   - $mycon_q   : db object, list of all "internal" contacts 
///////////////////////////////////////////////////////////////////////////////
function html_user_form($first_try, $usr_q, $user, $mycon_q) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname;
  global $l_email, $l_archive, $l_insert,$l_update,$l_checkdelete,$l_reset,$l_associated_contact,$l_none;
  global $perms_user,$perms_editor,$perms_admin,$l_perms_user,$l_perms_editor,$l_perms_admin ;
  global $action, $sess;

  // if update mode and first time value are taken from database
  if (($action == "detailupdate") && ($first_try == 1)) {
    $id = $usr_q->f("userobm_id");
    $login = $usr_q->f("userobm_login");
    $passwd = $usr_q->f("userobm_password");
    $perms = $usr_q->f("userobm_perms");
    $lname = $usr_q->f("userobm_lastname");
    $fname = $usr_q->f("userobm_firstname");
    $archive = ($usr_q->f("userobm_archive") == "1" ? " checked" : "");
    $contact = $usr_q->f("userobm_contact_id");
  } else {
    $id = $user["id"];
    $login = $user["login"];
    $passwd = $user["passwd"];
    $perms = $user["perms"];
    $lname = $user["lastname"];
    $fname = $user["firstname"];
    $email = $user["email"];
    $archive = ($user["archive"] == "1" ? "checked" : "");
    $contact = $user["contact"];
  }

  // perms selectconstruction
  if ($perms == $perms_user) $perms_user_s = " selected";
  if ($perms == $perms_editor) $perms_editor_s = " selected";
  if ($perms == $perms_admin) $perms_admin_s = " selected";

  // contact select construction
  $sel_con = "";
  while ($mycon_q->next_record()) {
    $sel_con .= "<OPTION value=" . $mycon_q->f("contact_id");
    if ($contact == $mycon_q->f("contact_id")) { $sel_con .= " selected"; }
    $sel_con .= ">" . $mycon_q->f("contact_lastname") . " " .
               $mycon_q->f("contact_firstname") . "</OPTION>";
  }

  echo "<table>
    <form method=post name=form_user
      onSubmit=\"if (check_user(this)) return true; else return false;\"
      action=\"" . $sess->url("user_index.php") . "\">
    <tr><td>

    <table>
    <tr>
      <td>$l_archive </td>
      <td><input name=cb_archive type=checkbox value=1 $archive></td>
    </tr><tr>
      <td>$l_login </td>
      <td><input name=tf_login size=24 maxlength=32 value=\"$login\"></td>
    </tr><tr>
      <td>$l_password </td>
      <td><input name=tf_passwd size=24 maxlength=32 value=\"$passwd\"></td>
    </tr><tr>
      <td>$l_user_perms </td>
      <td><select name=sel_perms>
        <option value=\"$perms_user\" $perms_user_s>$l_perms_user
        <option value=\"$perms_editor\" $perms_editor_s>$l_perms_editor
        <option value=\"$perms_admin\" $perms_admin_s>$l_perms_admin
        </select>
      </td>
    </tr><tr>
      <td>$l_lastname </td>
      <td><input name=tf_lastname size=32 maxlength=32 value=\"$lname\"></td>
    </tr><tr>
      <td>$l_firstname </td>
      <td><input name=tf_firstname size=32 maxlength=32 value=\"$fname\"></td>
    </tr><tr>
      <td>$l_email </td>
      <td><input name=tf_email size=32 maxlength=64 value=\"$email\"></td>
    </tr><tr>
      <td>$l_associated_contact</td>
      <td><select name=sel_contact>
        <option value='0'>$l_none</option>
        $sel_con
        </select>
      </td>
    </tr>
    </table>

    </td><td align=middle>";

  if (($action=="new") || ($action=="insert")) {
    echo "<input type=hidden name=action value=\"insert\">
          <input type=submit value=\"$l_insert\">";

  } elseif (($action=="detailupdate") || ($action=="update")) {
    echo "<input type=hidden name=action value=\"update\">
          <input type=hidden name=param_user value=\"$id\">
          <input type=submit value=\"$l_update\">
      </form>
      <p>
      <form method=post name=f_del
        action=\"" . $sess->url("user_index.php") . "\">
      <input type=hidden name=\"action\" value=\"check_delete\">
      <input type=hidden name=\"param_user\" value=\"$id\">
      <input type=submit value=\"$l_checkdelete\">";
  }

  echo "</form>
      <form method=post name=f_reset
        action=\"" . $sess->url("user_index.php") . "\">
      <input type=hidden name=\"action\" value=\"reset\">
      <input type=hidden name=\"param_user\" value=\"$id\">
      <input type=submit value=\"$l_reset\">
      </form>
    </td></tr>
  </table>";

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display User Consultation                                            //
// Parameters:
//   - $obm_q     : user database result
//   - $obm_q_con : contact infosdatabase result
///////////////////////////////////////////////////////////////////////////////
function html_user_consult($obm_q, $obm_q_con) {
  global $l_login, $l_password, $l_user_perms, $l_lastname, $l_firstname, $l_email, $l_lastaccess, $l_archive;
  global $l_insert,$l_update,$l_delete;
  global $perms_user,$perms_editor,$perms_admin,$l_perms_user,$l_perms_editor,$l_perms_admin ;
  global $action, $sess, $c_yes, $c_no, $col_label;

  $id = $obm_q->f("userobm_id");
  $login = $obm_q->f("userobm_login");
  $paswd = $obm_q->f("userobm_password");
  $perms = $obm_q->f("userobm_perms");
  $lname = $obm_q->f("userobm_lastname");
  $fname = $obm_q->f("userobm_firstname");
  $email = $obm_q->f("userobm_email");
  $lastaccess = $obm_q->f("userobm_timelastaccess");
  $archive = ($obm_q->f("userobm_archive") == 1 ? $c_yes : $c_no);

  if ($perms == $perms_user) $l_perms = $l_perms_user;
  if ($perms == $perms_editor) $l_perms = $l_perms_editor;
  if ($perms == $perms_admin) $l_perms = $l_perms_admin;

  if ($obm_q->f("userobm_contact_id") > 0) {
    $contact = "(" . $obm_q_con->f("contact_lastname") . " " .
              $obm_q_con->f("contact_firstname") . ")";
  } 

  echo "<table>
    <tr><td>

      <table>
      <tr>
        <td><font color=\"#$col_label\">$l_archive</font></td>
        <td>$archive</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_login</font></td>
        <td>$login $contact</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_password</font></td>
        <td>$passwd</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_user_perms</font></td>
        <td>$l_perms</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_lastname</font></td>
        <td>$lname</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_firstname</font></td>
        <td>$fname</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_email</font></td>
        <td>$email</td>
      </tr><tr>
        <td><font color=\"#$col_label\">$l_lastaccess</font></td>
        <td>$lastaccess</td>
      </tr>
      </table>

      </td><td align=middle>
      <form method=get name=form_user
            action=\"" . $sess->url("user_index.php") . "\">
      <input type=hidden name=\"action\" value=\"detailupdate\">
      <input type=hidden name=param_user value=\"$id\">
      <input type=submit value=\"$l_update\">
      </td>
    </tr>
    </form>
    </table>";
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
  global $sess, $c_yes, $c_no;

  $login = $user["login"];
  $passwd = $user["passwd"];
  $perms = $user["perms"];
  $lname = $user["lastname"];
  $fname = $user["firstname"];
  $email = $user["email"];
  $archive = $user["archive"];
  $contact = $user["contact"];

  echo $l_check_sameuser;
  while ($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
    $samename = $usr_q->f("userobm_login");
    echo "<BR><A HREF=\"" .$sess->url("user_index.php?action=detailconsult&amp;param_user=$id") . "\">$samename</A>";
  }

  echo "<P>
    <TABLE><TR><TD>
    <FORM METHOD=POST NAME=form_insert
    ACTION=\"" .$sess->url("user_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=insert>
    <INPUT TYPE=hidden NAME=hd_confirm VALUE=\"$c_yes\">
    <INPUT TYPE=hidden NAME=tf_login VALUE=\"$login\">
    <INPUT TYPE=hidden NAME=tf_passwd VALUE=\"$passwd\">
    <INPUT TYPE=hidden NAME=sel_perms VALUE=\"$perms\">
    <INPUT TYPE=hidden NAME=tf_lastname VALUE=\"$lname\">
    <INPUT TYPE=hidden NAME=tf_firstname VALUE=\"$fname\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <input type=hidden name=cb_archive value=\"$archive\">
    <INPUT TYPE=hidden NAME=sel_contact VALUE=\"$contact\">
    <INPUT TYPE=submit NAME=submit VALUE=\"$l_confirm\">
    </FORM>
    </TD><TD>
    <FORM NAME=form_back METHOD=POST
    ACTION=\"" .$sess->url("user_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=new>
    <INPUT TYPE=hidden NAME=tf_name VALUE=\"$name\">
    <INPUT TYPE=hidden NAME=tf_passwd VALUE=\"$passwd\">
    <INPUT TYPE=hidden NAME=sel_perms VALUE=\"$perms\">
    <INPUT TYPE=hidden NAME=tf_lastname VALUE=\"$lname\">
    <INPUT TYPE=hidden NAME=tf_firstname VALUE=\"$fname\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <input type=hidden name=cb_archive value=\"$archive\">
    <INPUT TYPE=hidden NAME=sel_contact VALUE=\"$contact\">
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
  global $col_textem;
  global $l_no_user;
  global $l_check_usercreate, $l_check_userupdate;
  global $sess;

  $delete_ok = true;

  $obm_q = new DB_OBM;
  $tables = $obm_q->table_names();

  echo "<TABLE border=0>";
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

      echo "<TR><TD rowspan=2><FONT color=\"#$col_textem\">$table</FONT></TD>
        <TD><FONT color=\"#$col_textem\">$nbc</FONT> $l_check_usercreate</TD>
      </TR><TR>
        <TD><FONT color=\"#$col_textem\">$nbu</FONT> $l_check_userupdate</TD>
      </TR>";
    } else {
      echo "<TR><TD><FONT color=\"#$col_textem\">$table</FONT></TD>
            <TD>$l_no_user</TD></TR>";
    }
    echo "<TR><TD colspan=2><HR></TD></TR>";
    $i++;
  }
 
  echo "</TABLE>";

  dis_delete_form($p_id);
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : user Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;
  global $sess;

  echo "\n<FORM name=\"form_delete\" " .
      "METHOD=post action=\"" . $sess->url("user_index.php") . "\">
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"delete\">
    <INPUT TYPE=\"hidden\" name=\"param_user\" value=\"$p_id\">
    <INPUT TYPE=\"submit\" value=\"$l_delete\"
    onClick=\"if (confirm_del(this.form)) return true; else return false;\">";
  echo "</FORM>\n";
}



</SCRIPT>
