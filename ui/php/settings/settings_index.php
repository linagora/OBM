<?php
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
$params = get_settings_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));

if ($params["lang"] != "") {
  $set_lang = $params["lang"];
  $_SESSION['set_lang'] = $set_lang;
  update_user_pref($obm["uid"], "set_lang", $set_lang);
}
if ($params["theme"] != "") {
  $set_theme = $params["theme"];
  $_SESSION['set_theme'] = $set_theme;
  update_user_pref($obm["uid"], "set_theme", $set_theme);
}

// Validate user preferences
if ($params["form_user_pref"]) {

  $set_debug = $params["debug_id"] | $params["debug_param"] | $params["debug_sess"] | $params["debug_sql"] | $params["debug_exe"];
  $_SESSION['set_debug'] = $set_debug;
  update_user_pref($obm["uid"], "set_debug", $set_debug);

  if ($params["menu"] != "") {
    $set_menu = $params["menu"];
    $_SESSION['set_menu'] = $set_menu;
    update_user_pref($obm["uid"], "set_menu", $set_menu);
  }

  if ($params["display"] == "yes") {
    $set_display = "yes";
  } else {
    $set_display = "no";
  }
  $_SESSION['set_display'] = $set_display;
  update_user_pref($obm["uid"], "set_display", $set_display);

  if ($params["rows"] != "") {
    $set_rows = $params["rows"];
    $_SESSION['set_rows'] = $set_rows;
    update_user_pref($obm["uid"], "set_rows", $set_rows);
  }

  if (($params["todo"] != "") && ($paramz["todo"] != "$set_todo")) {
    $set_todo = $params["todo"];
    $_SESSION['set_todo'] = $set_todo;
    update_user_pref($obm["uid"], "set_todo", $set_todo);
    global_session_load_user_todos($set_todo);
  }

  if ($params["dsrc"] != "") {
    $set_dsrc = $params["dsrc"];
    $_SESSION['set_dsrc'] = $set_dsrc;
    update_user_pref($obm["uid"], "set_dsrc", $set_dsrc);
  }

  if ($params["date"] != "") {
    $set_date = $params["date"];
    $_SESSION['set_date'] = $set_date;
    update_user_pref($obm["uid"], "set_date", $set_date);
  }
  
  if ($params["date_upd"] != "") {
    $set_date_upd = $params["date_upd"];
    $_SESSION['set_date_upd'] = $set_date_upd;
    update_user_pref($obm["uid"], "set_date_upd", $set_date_upd);
  }
  
  if ($params["commentorder"] != "") {
    $set_commentorder = $params["commentorder"];
    $_SESSION['set_commentorder'] = $set_commentorder;
    update_user_pref($obm["uid"], "set_commentorder", $set_commentorder);
  }

  if ($params["mail"] == "yes") {
    $set_mail = "yes";
  } else {
    $set_mail = "no";
  }
  $_SESSION['set_mail'] = $set_mail;
  update_user_pref($obm["uid"], "set_mail", $set_mail);

  if ($params["cal_interval"] != "") {
    $set_cal_interval = $params["cal_interval"];
    $_SESSION['set_cal_interval'] = $set_cal_interval;
    update_user_pref($obm["uid"], "set_cal_interval", $set_cal_interval, 1);
  }

  if ($params["csv_sep"] != "") {
    $set_csv_sep = $params["csv_sep"];
    $_SESSION['set_csv_sep'] = $set_csv_sep;
    update_user_pref($obm["uid"], "set_csv_sep", $set_csv_sep);
  }

}

require("$obminclude/global_pref.inc");
require("settings_display.inc");

if ($action == "") $action = "index";
get_settings_actions();
$perm->check_permissions($module, $action);

page_close();

if (($set_debug & $cdg_id) == $cdg_id) $dg_id = "checked";
if (($set_debug & $cdg_param) == $cdg_param) $dg_param = "checked";
if (($set_debug & $cdg_sess) == $cdg_sess) $dg_sess = "checked";
if (($set_debug & $cdg_sql) == $cdg_sql) $dg_sql = "checked";
if (($set_debug & $cdg_exe) == $cdg_exe) $dg_exe = "checked";

if ($set_menu == $cme_txt) $me_txt = "checked";
if ($set_menu == $cme_ico) $me_ico = "checked";
if ($set_menu == $cme_both) $me_both = "checked";

if ($set_date == $cda_iso) $da_iso = "checked";
if ($set_date == $cda_en) $da_en = "checked";
if ($set_date == $cda_fr) $da_fr = "checked";
if ($set_date == $cda_txt) $da_txt = "checked";

if($set_date_upd == $cda_upd_dmy) $da_dmy = "checked";
if($set_date_upd == $cda_upd_mdy) $da_mdy = "checked";

if ($set_commentorder == $cco_chro) $co_chro = "checked";
if ($set_commentorder == $cco_rev) $co_rev = "checked";

if ($set_cal_interval == $ccal_4) $cal_4 = "checked";
if ($set_cal_interval == $ccal_2) $cal_2 = "checked";
if ($set_cal_interval == $ccal_1) $cal_1 = "checked";

if ($set_csv_sep == $ccsvd_sc) $csvd_sc = "checked";
if ($set_csv_sep == $ccsvd_tab) $csvd_tab = "checked";

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
$display["header"] = display_menu($module);

// Todo Order select
if ($set_todo == $cts_pri) { $todo_pri = "selected=\"selected\" "; }
if ($set_todo == $cts_dead) { $todo_dead = "selected=\"selected\" "; }
$sel_todo = "<select name=\"todo_id\" id=\"todo_id\">
  <option value=\"$cts_pri\" $todo_pri>$l_priority</option>
  <option value=\"$cts_dead\" $todo_dead>$l_deadline</option>
</select>";

// Data source select
$dsrc_q = run_query_global_datasource();
$sel_dsrc = "<select name=\"dsrc\" id=\"dsrc\">
  <option value=\"$c_undef\">$l_undef</option>";
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
    <th>$l_set_debug ($set_debug)</th>
    <td>
      <input type=\"checkbox\" name=\"debug_id\" value=\"$cdg_id\" $dg_id />$l_dg_id
      <input type=\"checkbox\" name=\"debug_param\" value=\"$cdg_param\" $dg_param />$l_dg_param
      <input type=\"checkbox\" name=\"debug_sess\" value=\"$cdg_sess\" $dg_sess />$l_dg_sess
      <input type=\"checkbox\" name=\"debug_sql\" value=\"$cdg_sql\" $dg_sql />$l_dg_sql
      <input type=\"checkbox\" name=\"debug_exe\" value=\"$cdg_exe\" $dg_exe />$l_dg_exe
    </td>
  </tr>";
//}

///////////////////////////////////////////////////////////////////////////////
// For each LANGUAGE directory in the lang direcory (but ., .., CVS)         //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$lang_dir = dir("$path/../$obminclude/lang");
while ($entry=$lang_dir->read()) {
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
      && is_dir($lang_dir->path."/".$entry)) {
        $dis_lang .= "
      <a href=\"settings_index.php?lang=$entry\">
        <img src=\"".${"flag_$entry"}."\" alt=\"[Language $entry]\"/></a>
      ";
  }
}
$lang_dir->close();


///////////////////////////////////////////////////////////////////////////////
// For each THEME directory in the themes direcory (but ., .., CVS)          //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$theme_dir = dir("../$path/resources/themes");
$dis_theme = "";
while ($entry = $theme_dir->read()) {
  $dotcase = strcmp($entry, "."); 
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
       && strcmp($entry,"images") && is_dir($theme_dir->path."/".$entry)) {
    $dis_theme .= "
      <a href=\"settings_index.php?theme=$entry\">
      <img src=\"".${"preview_".$entry}."\" alt=\"[Theme $entry]\" style=\"height:20%;width:20%;\"
      onmouseover=\"\" 
      onmouseout=\"\"/>
      </a>";
  }
}
$theme_dir->close();


///////////////////////////////////////////////////////////////////////////////
// HTML Display
///////////////////////////////////////////////////////////////////////////////
if ($form_user_pref) {
  $display["msg"] .= display_ok_msg("$l_settings : $l_update_ok");
}

$display["detail"] .= "
<!--User preferences current config -->

  <form action=\"settings_index.php\" method=\"get\">
  <fieldset class=\"detail infos\">
  <legend>$l_cur_settings</legend>
  <table>
  <tr>
    <th>$l_set_menu</th>
    <td>
      <input type=\"radio\" name=\"menu\" value=\"$cme_txt\" $me_txt />$l_me_txt
      <input type=\"radio\" name=\"menu\" value=\"$cme_ico\" $me_ico />$l_me_ico
      <input type=\"radio\" name=\"menu\" value=\"$cme_both\" $me_both />$l_me_both
    </td>
  </tr><tr>
    <th>$l_auto_display</th>
    <td>
      <input type=\"checkbox\" name=\"display\" value=\"yes\" ";
if ($set_display == "yes") $display["detail"] .= "checked = \"checked\"";
$display["detail"] .= " /></td>
  </tr><tr>
    <th>$l_set_rows</th>
    <td>
      <input size=\"3\" name=\"rows\" value=\"$set_rows\" /></td>
  </tr>";

if ($cgp_show["module"]["todo"]) {

  $display["detail"] .= "

<!-- Todo Order config -->
  <tr>
    <th>$l_set_todo</th>
    <td>$sel_todo</td>
  </tr>";
}

$display["detail"] .= "

<!-- Data Source config -->
  <tr>
    <th>$l_datasource</th>
    <td>$sel_dsrc</td>
  </tr>
  </table>
  </fieldset>
  <fieldset class=\"detail infos\">
  <legend>$l_cur_settings</legend>
  <table>
<!-- Date Format config -->
  <tr>
    <th>$l_set_commentorder</th>
    <td>
      <input type=\"radio\" name=\"commentorder\" value=\"$cco_chro\" $co_chro />$l_co_chro
      <input type=\"radio\" name=\"commentorder\" value=\"$cco_rev\" $co_rev />$l_co_rev
    </td>
  </tr><tr>
    <th>$l_send_mail</th>
    <td class=\"adminText\">
      <input type=\"checkbox\" name=\"mail\" value=\"yes\" ";
if ($set_mail == "yes") $display["detail"] .= "checked";

$display["detail"] .= " /></td>
  </tr>";

if ($cgp_show["module"]["calendar"]) {
  $display["detail"] .= "
  <tr>
    <th>$l_set_cal_interval</th>
    <td>
      <input type=\"radio\" name=\"cal_interval\" value=\"$ccal_4\" $cal_4 />$l_cal_4
      <input type=\"radio\" name=\"cal_interval\" value=\"$ccal_2\" $cal_2 />$l_cal_2
      <input type=\"radio\" name=\"cal_interval\" value=\"$ccal_1\" $cal_1 />$l_cal_1
    </td>
  </tr>";
}

$display["detail"] .= "
  <tr>
  <th>$l_set_csv_sep</th>
  <td>
    <input type=\"radio\" name=\"csv_sep\" value=\"$ccsvd_sc\" $csvd_sc />$l_csvd_sc
    <input type=\"radio\" name=\"csv_sep\" value=\"$ccsvd_tab\" $csvd_tab />$l_csvd_tab
  </td>
  </tr>
  $dis_debug
  </table>
  </fieldset>
  <fieldset class=\"detail extra\">
  <legend>$l_date</legend>
  <table>
  <tr>
  <td>$l_set_date_upd</td>
  <td>
    <input type=\"radio\" name=\"date_upd\" value=\"$cda_upd_dmy\" $da_dmy />$l_da_dmy
    <input type=\"radio\" name=\"date_upd\" value=\"$cda_upd_mdy\" $da_mdy />$l_da_mdy
  </td>
  <td>$l_set_date</td>
  <td>
    <input type=\"radio\" name=\"date\" value=\"$cda_iso\" $da_iso />$l_da_iso
    <input type=\"radio\" name=\"date\" value=\"$cda_en\" $da_en />$l_da_en
    <input type=\"radio\" name=\"date\" value=\"$cda_fr\" $da_fr />$l_da_fr
    <input type=\"radio\" name=\"date\" value=\"$cda_txt\" $da_txt />$l_da_txt
  </td>
  </tr>
  </table>
  </fieldset>  
  <fieldset class=\"buttons\">
    <input name=\"form_user_pref\" type=\"hidden\" value=\"1\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_validate\" />
  </fieldset>
  </form>
  <hr />
<!-- Lang and theme current config -->
  <div class=\"detail infos\">
  <h1>$l_cur_lang</h1>
  <img src=\"".${"flag_$set_lang"}."\" alt=\"[language]\" />
  <h1>$l_set_lang</h1>
  $dis_lang
  </div>
  <div class=\"detail infos\">
  <h1>$l_cur_theme</h1>
  <img src=\"".${"preview_$set_theme"}."\" alt=\"[Theme]\"  />
  <h1>$l_set_theme</h1>
  $dis_theme
  </div>

<!-- Display available configs -->
";


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_settings);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Settings parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_settings_params() {
  
  // Get global params
  $params = get_global_params("Settings");
  
  return $params;
}


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

?>
