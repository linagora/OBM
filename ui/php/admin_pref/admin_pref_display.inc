<?php
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
function dis_admin_pref_help($mode) {

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
function dis_admin_pref_index($mode) {

  switch ($mode) {
  case "txt":
    echo "\ntry -h for help\n";
    break;
  case "html":
    html_admin_pref_index();
    break;
  default:
    echo "Invalid Mode ! (txt | html)";
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display the HTML index screen for the pref admin module
///////////////////////////////////////////////////////////////////////////////
function html_admin_pref_index() {
  global $l_validate, $l_execute, $l_help, $l_user_pref;
  global $l_user_pref_reset, $l_user_pref_update_one;

  $userpref_q = get_admin_pref_default_user_pref();
  // User preferences SELECT
  $sel_userpref = "<select name=\"sel_userpref\">";
  while ($userpref_q->next_record()) {
    $option = $userpref_q->f("userobmpref_option");
    $value = $userpref_q->f("userobmpref_value");
    $sel_userpref .= "<option value=\"$option\">$option - $value";
  }
  $sel_userpref .= "</select>";

  $block = "
    <br />
    <form method=\"get\" action=\"" . url_prepare("admin_pref_index.php"). "\">
    <table class=\"admin\">
    <tr>
      <td class=\"adminHead\" colspan=\"2\">$l_user_pref</td>
    </tr>
    <tr>
      <td class=\"adminText\">action : user_pref_reset
        <br />$l_user_pref_reset</td>
      <td class=\"adminLabel\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"user_pref_reset\" />
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
        $l_user_pref_update_one
      </td>
      <td class=\"adminText\">
        $sel_userpref</td>
      <td class=\"adminText\">
        <input type=\"text\" name=\"tf_pref_value\" value=\"\" maxlength=\"20\" size=\"8\"/></td>
    </tr>
    </table>
    </form>
";

  echo $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Pref updates
// Parameters:
//   - $mode   : "txt" or "html"
///////////////////////////////////////////////////////////////////////////////
function dis_admin_pref_user_pref_reset($mode) {
  global $l_reset_ok;
  
  //-- Reset default values (drop all specific users values)
  reset_preferences_to_default("");

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** $l_reset_ok
------------------------------------------------------------------------------
";
  } else {
    $block = display_ok_msg($l_reset_ok);
    echo $block;
  }

}


///////////////////////////////////////////////////////////////////////////////
// One User Pref update for all users
// Parameters:
//   - $mode : "txt" or "html"
//   - $pref : name or option of the pref
//   - $val  : optional : value to set for all users
///////////////////////////////////////////////////////////////////////////////
function dis_admin_pref_user_pref_update_one($mode, $pref, $val="") {
  global $cdg_sql;

  //-- Drop specific user preference if same as new value
  $obm_q = new DB_OBM;
  $query = "DELETE FROM UserObmPref
    WHERE userobmpref_option='$pref'
      AND userobmpref_value='$val'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  //-- Update default value
  run_query_user_pref_update(0, $pref, $val);

  if ($mode == "txt") {
    echo "
------------------------------------------------------------------------------
*** Default value to $pref set to $val
------------------------------------------------------------------------------
";
  } else {
    $block = display_ok_msg("Default value to $pref set to $val");
    echo $block;
  }

}

?>
