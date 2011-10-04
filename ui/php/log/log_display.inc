<?php
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
