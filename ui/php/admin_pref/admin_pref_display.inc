<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
    echo "<table>
      <tr>
        <td><pre>";

  include ("admin_pref_help.inc");

  if ($mode == "html")
    echo "</pre></td>
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
  $sel_userpref = "<select style=\"width: 12em;\" name=\"sel_userpref\">";
  while ($userpref_q->next_record()) {
    $option = $userpref_q->f("userobmpref_option");
    $value = $userpref_q->f("userobmpref_value");
    $sel_userpref .= "<option value=\"$option\">$option - $value";
  }
  $sel_userpref .= "</select>";

  $block = "
    <form method=\"get\" action=\"" . url_prepare("admin_pref_index.php"). "\">
    <fieldset class=\"detail infos\">
    <legend>$l_user_pref</legend>
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>Action : user_pref_reset</th>
    </tr>
    </thead>
    <tbody>
      <tr>
      <td>$l_user_pref_reset</td>
      </tr>
    </tbody>
    </table>
    <fieldset class=\"buttons\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"user_pref_reset\" />
        <input type=\"submit\" value=\"$l_execute\" />
      </fieldset>
      </fieldset>
    </form>

    <form method=\"get\" action=\"" . url_prepare("admin_pref_index.php"). "\">
    <fieldset class=\"detail infos\">
    <legend>Update Preferences</legend>    
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <td>action : user_pref_update_one</td>
      <td>Pref</td>
      <td>Value</td>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>
        $l_user_pref_update_one
      </td>
      <td>
      $sel_userpref
      </td>
      <td>
      <input type=\"text\" style=\"width: 8em;\" name=\"tf_pref_value\" value=\"\" maxlength=\"20\" size=\"14\"/>
      </td>
    </tr>
    </tbody>
    </table>
    <fieldset class=\"buttons\">
        <input type=\"hidden\" name=\"mode\" value=\"html\" />
        <input type=\"hidden\" name=\"action\" value=\"user_pref_update_one\" />
        <input type=\"submit\" value=\"$l_execute\" />
    </fieldset>
    </fieldset>
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
