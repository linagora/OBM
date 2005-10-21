<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : settings_index.php                                           //
//     - Desc : Settings (Language, themes,...) management index file        //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "settings";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude="obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
$uid = $auth->auth["uid"];

if ($param_lang != "") {
  $set_lang = $param_lang;
  $sess->register("set_lang");
  update_user_pref($uid, "set_lang", $set_lang);
}
if ($param_theme != "") {
  $set_theme = $param_theme;
  $sess->register("set_theme");
  update_user_pref($uid, "set_theme", $set_theme);
}

// Validate user preferences
if ($form_user_pref) {

  $param_debug = $param_debug_id | $param_debug_param | $param_debug_sess | $param_debug_sql;
  $set_debug = $param_debug;
  $sess->register("set_debug");
  update_user_pref($uid, "set_debug", $set_debug);
  if ($param_menu != "") {
    $set_menu = $param_menu;
    $sess->register("set_menu");
    update_user_pref($uid, "set_menu", $set_menu);
  }

  if ($param_display == "yes") {
    $set_display = "yes";
  } else {
    $set_display = "no";
  }
  $sess->register("set_display");
  update_user_pref($uid, "set_display", $set_display);

  if ($param_rows != "") {
    $set_rows = $param_rows;
    $_SESSION['set_rows'] = $set_rows;
    //    $sess->register("set_rows");
    update_user_pref($uid, "set_rows", $set_rows);
  }

  if (($param_todo != "") && ($param_todo != "$set_todo")) {
    $set_todo = $param_todo;
    $sess->register("set_todo");
    update_user_pref($uid, "set_todo", $set_todo);
    session_load_user_todos($set_todo);
  }

  if ($param_dsrc != "") {
    $set_dsrc = $param_dsrc;
    $sess->register("set_dsrc");
    update_user_pref($uid, "set_dsrc", $set_dsrc);
  }

  if ($param_date != "") {
    $set_date = $param_date;
    $sess->register("set_date");
    update_user_pref($uid, "set_date", $set_date);
  }

  if ($param_commentorder != "") {
    $set_commentorder = $param_commentorder;
    $sess->register("set_commentorder");
    update_user_pref($uid, "set_commentorder", $set_commentorder);
  }

  if ($param_mail == "yes") {
    $set_mail = "yes";
  } else {
    $set_mail = "no";
  }
  $sess->register("set_mail");
  update_user_pref($uid, "set_mail", $set_mail);

  if ($param_cal_interval != "") {
    $set_cal_interval = $param_cal_interval;
    $sess->register("set_cal_interval");
    update_user_pref($uid, "set_cal_interval", $set_cal_interval, 1);
  }

  if ($param_csv_sep != "") {
    $set_csv_sep = $param_csv_sep;
    $sess->register("set_csv_sep");
    update_user_pref($uid, "set_csv_sep", $set_csv_sep);
  }

}
page_close();

require("$obminclude/global_pref.inc");
require("settings_display.inc");

if (($set_debug & $cdg_id) == $cdg_id) $dg_id = "checked";
if (($set_debug & $cdg_param) == $cdg_param) $dg_param = "checked";
if (($set_debug & $cdg_sess) == $cdg_sess) $dg_sess = "checked";
if (($set_debug & $cdg_sql) == $cdg_sql) $dg_sql = "checked";

if ($set_menu == $cme_txt) $me_txt = "checked";
if ($set_menu == $cme_ico) $me_ico = "checked";
if ($set_menu == $cme_both) $me_both = "checked";

if ($set_date == $cda_iso) $da_iso = "checked";
if ($set_date == $cda_en) $da_en = "checked";
if ($set_date == $cda_fr) $da_fr = "checked";
if ($set_date == $cda_txt) $da_txt = "checked";

if ($set_commentorder == $cco_chro) $co_chro = "checked";
if ($set_commentorder == $cco_rev) $co_rev = "checked";

if ($set_cal_interval == $ccal_4) $cal_4 = "checked";
if ($set_cal_interval == $ccal_2) $cal_2 = "checked";
if ($set_cal_interval == $ccal_1) $cal_1 = "checked";

if ($set_csv_sep == $ccsvd_sc) $csvd_sc = "checked";
if ($set_csv_sep == $ccsvd_tab) $csvd_tab = "checked";

if ($action == "") $action = "index";
get_settings_actions();
$perm->check_permissions($module, $action);

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
$display["header"] = generate_menu($module, $section);

// Todo Order select
if ($set_todo == $cts_pri) { $todo_pri = "selected=\"selected\" "; }
if ($set_todo == $cts_dead) { $todo_dead = "selected=\"selected\" "; }
$sel_todo = "<select name=\"param_todo\" id=\"param_todo\">
  <option value=\"$cts_pri\" $todo_pri>$l_priority</option>
  <option value=\"$cts_dead\" $todo_dead>$l_deadline</option>
</select>";

// Data source select
$dsrc_q = run_query_datasource();
$sel_dsrc = "<select name=\"param_dsrc\" id=\"param_dsrc\">
  <option value=\"0\">$l_none</option>";
while ($dsrc_q->next_record()) {
  $d_id = $dsrc_q->f("datasource_id");
  $sel_dsrc .= "\n<option value=\"$d_id\"";
  if ($d_id == $set_dsrc) { $sel_dsrc .= " selected=\"selected\" "; }
  $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>";
}
$sel_dsrc .= "</select>";


///////////////////////////////////////////////////////////////////////////////
// Debug block (admin only)
///////////////////////////////////////////////////////////////////////////////
//if ($perm->check_right($module, $cright_write_admin)) {

  $dis_debug = "
  <tr>
    <td class=\"adminLabel\">$l_set_debug ($set_debug)</td>
    <td class=\"adminText\">
      <input type=\"checkbox\" name=\"param_debug_id\" value=\"$cdg_id\" $dg_id />$l_dg_id
      <input type=\"checkbox\" name=\"param_debug_param\" value=\"$cdg_param\" $dg_param />$l_dg_param
      <input type=\"checkbox\" name=\"param_debug_sess\" value=\"$cdg_sess\" $dg_sess />$l_dg_sess
      <input type=\"checkbox\" name=\"param_debug_sql\" value=\"$cdg_sql\" $dg_sql />$l_dg_sql
    </td>
  </tr>";
//}

///////////////////////////////////////////////////////////////////////////////
// For each LANGUAGE directory in the lang direcory (but ., .., CVS)         //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$lang_dir = dir("$path/../$obminclude/lang");
$dis_lang = "<table class=\"admin\">";
while ($entry=$lang_dir->read()) {
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
      && is_dir($lang_dir->path."/".$entry)) {
    $dis_lang .= "<tr>
      <td class=\"adminLabel\">
        <a href=\"" . url_prepare("settings_index.php?param_lang=$entry") ."\">
        <img src=\"".C_IMAGE_PATH."/images/flag-$entry.gif\" /></a>
      </td>
      </tr>";
  }
}
$dis_lang .= "</table>";
$lang_dir->close();


///////////////////////////////////////////////////////////////////////////////
// For each THEME directory in the themes direcory (but ., .., CVS)          //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$theme_dir = dir("$path/../$obminclude/themes");
$dis_theme = "<table class=\"admin\">";
while ($entry = $theme_dir->read()) {
  $dotcase = strcmp($entry, "."); 
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
       && strcmp($entry,"images") && is_dir($theme_dir->path."/".$entry)) {
    $dis_theme .= "<tr>
      <td class=\"adminLabel\">
        <a href=\"".url_prepare("settings_index.php?param_theme=$entry") .
        "\"><img src=\"".C_IMAGE_PATH."/$entry/$entry.jpg\" /></a>
      </td>
      </tr>";
  }
}
$dis_theme .= "</table>";
$theme_dir->close();


///////////////////////////////////////////////////////////////////////////////
// HTML Display
///////////////////////////////////////////////////////////////////////////////
if ($form_user_pref) {
  $display["msg"] .= display_ok_msg($l_update_ok);
}

$display["detail"] .= "
<!--User preferences current config -->

  <center>
  <form action=\"".url_prepare("settings_index.php")."\" method=\"get\">
  <table class=\"admin\">
  <tr>
    <td class=\"adminHead\" colspan=\"2\">$l_cur_settings</td>
  </tr><tr>
    <td class=\"adminLabel\">$l_set_menu</td>
    <td class=\"adminText\">
      <input type=\"radio\" name=\"param_menu\" value=\"$cme_txt\" $me_txt />$l_me_txt
      <input type=\"radio\" name=\"param_menu\" value=\"$cme_ico\" $me_ico />$l_me_ico
      <input type=\"radio\" name=\"param_menu\" value=\"$cme_both\" $me_both />$l_me_both
    </td>
  </tr><tr>
    <td class=\"adminLabel\">$l_auto_display</td>
    <td class=\"adminText\">
      <input type=\"checkbox\" name=\"param_display\" value=\"yes\" ";
if ($set_display == "yes") $display["detail"] .= "checked = \"checked\"";
$display["detail"] .= " /></td>
  </tr><tr>
    <td class=\"adminLabel\">$l_set_rows</td>
    <td class=\"adminText\">
      <input size=\"3\" name=\"param_rows\" value=\"$set_rows\" /></td>
  </tr>

<!-- Todo Order config ------------------------------------------------------->
  <tr>
    <td class=\"adminLabel\">$l_set_todo</td>
    <td class=\"adminText\">$sel_todo</td>
  </tr>

<!-- Data Source config ------------------------------------------------------>
  <tr>
    <td class=\"adminLabel\">$l_datasource</td>
    <td class=\"adminText\">$sel_dsrc</td>
  </tr>

<!-- Date Format config ------------------------------------------------------>
  <tr>
    <td class=\"adminLabel\">$l_set_date</td>
    <td class=\"adminText\">
      <input type=\"radio\" name=\"param_date\" value=\"$cda_iso\" $da_iso />$l_da_iso
      <input type=\"radio\" name=\"param_date\" value=\"$cda_en\" $da_en />$l_da_en
      <input type=\"radio\" name=\"param_date\" value=\"$cda_fr\" $da_fr />$l_da_fr
      <input type=\"radio\" name=\"param_date\" value=\"$cda_txt\" $da_txt />$l_da_txt
    </td>
  </tr><tr>
    <td class=\"adminLabel\">$l_set_commentorder</td>
    <td class=\"adminText\">
      <input type=\"radio\" name=\"param_commentorder\" value=\"$cco_chro\" $co_chro />$l_co_chro
      <input type=\"radio\" name=\"param_commentorder\" value=\"$cco_rev\" $co_rev />$l_co_rev
    </td>
  </tr><tr>
    <td class=\"adminLabel\">$l_send_mail</td>
    <td class=\"adminText\">
      <input type=\"checkbox\" name=\"param_mail\" value=\"yes\" ";
if ($set_mail == "yes") $display["detail"] .= "checked";
$display["detail"] .= " /></td>
  </tr><tr>
    <td class=\"adminLabel\">$l_set_cal_interval</td>
    <td class=\"adminText\">
      <input type=\"radio\" name=\"param_cal_interval\" value=\"$ccal_4\" $cal_4 />$l_cal_4
      <input type=\"radio\" name=\"param_cal_interval\" value=\"$ccal_2\" $cal_2 />$l_cal_2
      <input type=\"radio\" name=\"param_cal_interval\" value=\"$ccal_1\" $cal_1 />$l_cal_1
    </td>
  </tr><tr>
    <td class=\"adminLabel\">$l_set_csv_sep</td>
    <td class=\"adminText\">
      <input type=\"radio\" name=\"param_csv_sep\" value=\"$ccsvd_sc\" $csvd_sc />$l_csvd_sc
      <input type=\"radio\" name=\"param_csv_sep\" value=\"$ccsvd_tab\" $csvd_tab />$l_csvd_tab
    </td>
  </tr>
  $dis_debug
  <tr>
    <td class=\"adminLabel\" colspan=\"2\">
      <input name=\"form_user_pref\" type=\"hidden\" value=\"1\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_validate\" />
    </td>
  </tr>
  </table>
  </form>
  <p />

<!-- Lang and theme current config ---------------------------------------- -->

  <table class=\"admin\">
  <tr>
    <td class=\"adminHead\">$l_cur_lang</td>
    <td class=\"adminHead\">$l_cur_theme</td>
  </tr><tr>
    <td class=\"adminLabel\">
      <img src=\"".C_IMAGE_PATH."/images/flag-$set_lang.gif\" />
    </td>
    <td class=\"adminLabel\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$set_theme.jpg\" />
    </td>
  </tr>
  </table>

<!-- Display available configs -------------------------------------------- -->

  <p />$l_new_settings
  <p />
  <table class=\"admin\">
  <tr>
    <td class=\"adminHead\">&nbsp; $l_set_lang &nbsp;</td>
    <td class=\"adminHead\">&nbsp; $l_set_theme &nbsp;</td>
  </tr><tr>
    <td class=\"adminLabel\">
      $dis_lang
    </td>
    <td class=\"adminLabel\">
      $dis_theme
    </td>
  </tr>
  </table>

  </center>
  </body>
  </html>";


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_settings);
$display["end"] = display_end();

display_page($display);


//////////////////////////////////////////////////////////////////////////////
// Settings actions
//////////////////////////////////////////////////////////////////////////////
function get_settings_actions() {
  global $actions, $cright_read;

  $actions["settings"]["index"] = array (
    'Url'      => "$path/settings/settings_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);
}

</script>
