<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : admin_pref_display.inc                                       //
//     - Desc : Pref admin display File                                      //
// 2002-07-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_help($mode) {

  if ($mode == "html")
    echo "<table class=\"detail\">
      <tr>
        <td><pre>";

  include ("admin_pref_help.inc");

  if ($mode == "html")
    echo "</pre>
        </td>
      </tr>
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for pref module
// Parameters:
//   - $mode : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_pref_index($mode) {

  // We get lifetime from the database and not from the session variable
  // in case it has been updated since (session value last during the session)
  $lifetime = get_global_pref_lifetime();
  $session_cookie = get_global_pref_session_cookie();

  switch ($mode) {
  case "txt":
    echo "\ntry -h for help\n";
    break;
  case "html":
    html_pref_index($lifetime, $session_cookie);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML index screen for the pref admin module
// Parameters:
//   - $plifetime       : $lifetime value (taken from database)
//   - $psession_cookie : flag to do session_cookie : 1 or true = yes
///////////////////////////////////////////////////////////////////////////////
function html_pref_index($plifetime, $psession_cookie) {
  global $l_validate, $l_execute, $l_help, $l_user_pref, $l_global_pref;
  global $l_session_lifetime, $l_session_cookie;

  if ($psession_cookie)
    $sc = "checked";
  else
    $sc = "";

  $userpref_q = get_default_user_pref();
  // User preferences SELECT
  $sel_userpref = "<select name=\"sel_userpref\">";
  while ($userpref_q->next_record()) {
    $option = $userpref_q->f("userobmpref_option");
    $value = $userpref_q->f("userobmpref_value");
    $sel_userpref .= "<option value=\"$option\">$option - $value";
  }
  $sel_userpref .= "</select>";

  echo "
    <br />
    <form method=\"get\" action=\"" . url_prepare("admin_pref_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\" colspan=\"2\">$l_user_pref</td>
    </tr>
    <tr>
      <td class=\"adminText\">action : user_pref_update
        <br />
        Update all users preferences with default values</td>
      <td class=\"adminLabel\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"user_pref_update\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr>
    </table>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_pref_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\">action : user_pref_update_one</td>
      <td class=\"adminHead\">Pref</td>
      <td class=\"adminHead\">Value</td>
      <td class=\"adminLabel\" rowspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"user_pref_update_one\" />
        <input type=\"submit\" value=\"$l_execute\" /></td>
    </tr>
    <tr>
      <td class=\"adminText\">
        Update the selected preferences with default values</td>
      <td class=\"adminText\">
        $sel_userpref</td>
      <td class=\"adminText\">
        <input type=\"text\" name=\"tf_pref_value\" value=\"\" maxlength=\"20\" size=\"8\"/></td>
    </tr>
    </table>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_pref_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\" colspan=\"2\">$l_global_pref</td>
    </tr>
    <tr>
      <td class=\"adminText\">$l_session_lifetime</td>
      <td class=\"adminLabel\">
        <input name=\"tf_lifetime\" size=\"6\" maxlength=\"6\"
          value=\"$plifetime\" /></td>
    </tr>
    <tr>
      <td class=\"adminText\">$l_session_cookie</td>
      <td class=\"adminLabel\">
        <input type=\"checkbox\" name=\"cb_session_cookie\" value=\"1\"
          \"$sc\" /></td>
    </tr>
    <tr>
      <td class=\"adminLabel\" colspan=\"2\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"global_pref_update\" />
        <input type=\"submit\" value=\"$l_validate\" /></td>
    </tr>
    </table>
    </form>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display Pref updates
// Parameters:
//   - $mode   : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_user_pref_update($mode) {
  global $debug;

  // Get the user list
  $u_q = get_user_list();
  $cpt_u = 0;
  $nb_u = $u_q->num_rows();
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** User : $nb_u to parse
------------------------------------------------------------------------------
";
  } else {
    $update = "<td class=\"adminHead\">Update</td>";

    echo "<table class=\"admin\">
      <tr>
        <td class=\"adminHead\" colspan=\"2\">User ($nb_u)</td>
        $update
      </tr><tr>
        <td class=\"adminHead\">Id</td>
        <td class=\"adminHead\">Login</td>
        $update
      </tr>";
  }

  while ($u_q->next_record()) {
    $cpt_u++;

    $id = $u_q->f("userobm_id");
    $login = $u_q->f("userobm_login");

    if ($mode == "txt") {
      echo "User (id=$id) : $login ";
    } else {
      echo "<tr>
        <td class=\"adminText\">$id</td>
        <td class=\"adminText\">$login</td>";
    }

    $retour = run_query_default_preferences_insert($id);

    if ($retour) {
      if ($mode == "txt") {
	echo "OK";
      } else {
	echo "<td class=\"adminLabel\">OK</td>";
      }
    } else {
      if ($mode == "txt") {
	echo "Problem !";
      } else {
	echo "<td class=\"adminLabel\">Problem !</td>";
      }
    }

    if ($mode == "html") {
      echo "</tr>";
    }

    if ($cpt_u % 10 == 0) {
      if ($mode == "txt") {
	echo ".";
      } else {
	echo "<tr>
          <td class=\"adminLabel\" colspan=\"3\">$cpt_u</td>
          </tr>";
	flush();
      }
    }

  }

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** User : $nb_u to parse, $cpt_u updated
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td class=\"adminLabel\" colspan=\"3\">
          User : $nb_u to parse, $cpt_u updated</td>
      </tr>
      </table>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// One User Pref update for all users
// Parameters:
//   - $mode : "txt" or "html"
//   - $pref : name or option of the pref
//   - $val  : optional : value to set for all users
///////////////////////////////////////////////////////////////////////////////
function dis_user_pref_update_one($mode, $pref, $val="") {
  global $debug;

  // Get the user list
  $u_q = get_user_list();
  $cpt_u = 0;
  $nb_u = $u_q->num_rows();

  //-- Drop current preference for all users (except 0)
  $obm_q = new DB_OBM;
  $query = "delete from UserObmPref where userobmpref_user_id != '0'
    and userobmpref_option='$pref'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  
  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** User : $nb_u to parse
------------------------------------------------------------------------------
";
  } else {
    $update = "<td class=\"adminHead\">Update</td>";

    echo "<table class=\"admin\">
      <tr>
        <td class=\"adminHead\" colspan=\"2\">User ($nb_u)</td>
        $update
      </tr><tr>
        <td class=\"adminHead\">Id</td>
        <td class=\"adminHead\">Login</td>
        $update
      </tr>";
  }

  while ($u_q->next_record()) {
    $cpt_u++;

    $id = $u_q->f("userobm_id");
    $login = $u_q->f("userobm_login");

    if ($mode == "txt") {
      echo "User (id=$id) : $login ";
    } else {
      echo "<tr>
        <td class=\"adminText\">$id</td>
        <td class=\"adminText\">$login</td>";
    }

    $retour = run_query_set_user_pref($id, $pref, $val);

    if ($retour) {
      if ($mode == "txt") {
	echo "OK";
      } else {
	echo "<td class=\"adminLabel\">OK</td>";
      }
    } else {
      if ($mode == "txt") {
	echo "Problem !";
      } else {
	echo "<td class=\"adminLabel\">Problem !</td>";
      }
    }

    if ($mode == "html") {
      echo "</tr>";
    }

    if ($cpt_u % 10 == 0) {
      if ($mode == "txt") {
	echo ".";
      } else {
	echo "<tr>
          <td class=\"adminLabel\" colspan=\"3\">$cpt_u</td>
          </tr>";
	flush();
      }
    }

  }

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** User : $nb_u to parse, $cpt_u updated
------------------------------------------------------------------------------
";
  } else {
    echo "<tr>
        <td class=\"adminLabel\" colspan=\"3\">
          User : $nb_u to parse, $cpt_u updated</td>
      </tr>
      </table>";
  }
}
