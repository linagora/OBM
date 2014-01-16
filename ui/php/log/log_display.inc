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
// OBM - File : log_display.inc                                              //
//     - Desc : log Display File                                             //
//  2007-07-02 AliaSource - Pierre Baudracco                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Log index
// Parameters:
//   - $params[] : hash with backup values
///////////////////////////////////////////////////////////////////////////////
function dis_log_index($params='') {
  global $display;

  $display['search'] = dis_log_search_form($params);
  $display['result'] = dis_log_search_list($params);

  return $true;
}


///////////////////////////////////////////////////////////////////////////////
// Display Log search form
// Parameters:
//   - $params[] : hash with parameters values
///////////////////////////////////////////////////////////////////////////////
function dis_log_search_form($params='') {

  $block .= html_log_search_form($params);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Log search Form
// Parameters:
//   - $params[] : default form values
///////////////////////////////////////////////////////////////////////////////
function html_log_search_form($params) {
  global $display, $ico_delete,$path;
  global $l_log_file, $l_user, $l_find;

  $email = stripslashes($backup["filename"]);

  $url = url_prepare("log_index.php");

  $users = array();
  // userobm select
  if (is_array($users)) {
    foreach ($users as $u_id => $u) {
      $u_name = $u['label'];
      $sel_id = "data-user-$u_id";
      $div_id = "sel_user_id-$sel_id";
      $dis_sel_user .= "<div class=\"elementRow\" id=\"$div_id\">
      <a href=\"javascript: remove_element('$div_id','sel_user_id');\">
      <img src=\"$ico_delete\" alt=\"[Delete]\" />
      </a>
      $u_name
      <input value=\"$sel_id\" name=\"sel_user_id[]\" type=\"hidden\" />
      </div>";
    }
  }

  // Log file select
  $files = get_log_mail_files();
  if (is_array($files)) {
    $dis_sel_files .= "<select name=\"sel_file\">";
    foreach ($files as $f_date => $f) {
      $filename = $f['filename'];
      $f_size = $f['size'];
      $dis_sel_files .= "\n<option value=\"$filename\">$filename - $f_date - $f_size</option>";
    }
    $dis_sel_files .= "\n</select>";
  }
 
  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">
    <label>$l_log_file
    $dis_sel_files
    </label>
    <label>
      <table>
      <tr>
        <td>$l_user</td>
      </tr>
      <tr>
        <td><input type=\"text\" name=\"\" value=\"\" id=\"userSearch\" autocomplete=\"off\" /></td>
      </tr><tr>
        <td id=\"sel_user_id\">$dis_sel_user</td>
      </tr>
      </table>
    </label>
    <label>
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext&nbsp; 
    </label>
    <p class=\"CL\" />
  </form>

  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_user_id', 'userSearch');
    });
  </script>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the log search result
// Parameters:
//   - $params[] : log search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_log_search_list($params) {
  global $display, $clog_root, $cdg_exe, $l_no_found;

  $emails = get_log_emails_from_users($params);
  $ret = run_query_users_view_log($params);
  
  if ($ret == '') {
    $display["msg"] .= display_warn_msg($l_no_found);
  }
  $html = "<pre>". htmlentities($ret) ."</pre>";

  return $html;
}


?>
