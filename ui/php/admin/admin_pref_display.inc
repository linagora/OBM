<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : admin_pref_display.inc                                      //
//     - Desc  : Pref admin display File                                     //
// 2002-07-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the help screen
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_help($mode) {

  if ($mode == "html") echo "<table border=1><tr><td><pre>\n";
  include ("admin_pref_help.inc");
  if ($mode == "html") echo "\n</pre></td></tr></table>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the search form for pref module
// Parameters:
//   - $mode   : "txt" ou "html"
///////////////////////////////////////////////////////////////////////////////
function dis_pref_index($mode, $actions) {

  switch ($mode) {
  case "txt":
    echo "try -h to help\n";
    break;
  case "html":
    html_pref_index($actions);
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML index screen for the pref admin module
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_pref_index($actions) {
  global $sess;
  global $l_execute, $l_help;
  global $col_textem, $col_a_table, $col_a_text, $col_a_tableh;

  echo "
    <a href=\"" . $sess->url("admin_pref_index.php?action=help&amp;mode=html"). "\">$l_help</a>
    <p>
    <FORM method=get action=\"" . $sess->url("admin_pref_index.php"). "\">
    <table border=1>
    <tr>
      <td>action : pref_update<br>
        <font color=\"#$col_textem\">Update all users preferences with default values</font></td>
      <td><input type=hidden name=mode value=\"html\">
          <input type=hidden name=action value=pref_update>
          <input type=submit value=\"$l_execute\"></td>
    </tr>
    </table>
    </FORM>";

}


///////////////////////////////////////////////////////////////////////////////
// Display Pref updates
// Parameters:
//   - $mode   : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_pref_update($mode) {
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
    $update = "<th>Update</th>";

    echo "<table border=1>
      <tr>
        <th colspan=2>User ($nb_u)</th>
        $update
      </tr><tr>
        <th>Id</th>
        <th>Name</th>
        $update
      </tr>";
  }

  while ($u_q->next_record()) {
    $cpt_u++;

    $id = $u_q->f("userobm_id");
    $name = $u_q->f("userobm_username");

    if ($mode == "txt") {
      echo "\nUser (id=$id) : $name ";
    } else {
      echo "<tr>
        <td>$id</td>
        <td>$name</td>";
    }

    $retour = run_query_default_preferences_insert($id);

    if ($retour) {
      if ($mode == "txt") {
	echo "OK";
      } else {
	echo "<td>OK</td>";
      }
    } else {
      if ($mode == "txt") {
	echo "Problem !";
      } else {
	echo "<td>Problem !</td>";
      }
    }

    if ($mode == "html") {
      echo "</tr>";
    }

    if ($cpt_u % 10 == 0) {
      if ($mode == "txt") {
	echo ".";
      } else {
	echo "<tr><th colspan=3 align=center>$cpt_u</th></tr>";
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
        <td colspan=3>User : $nb_u to parse, $cpt_u updated</td>
      </tr>
      </table>";
  }
}
