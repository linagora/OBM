<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : settings_index.php                                           //
//     - Desc : Settings (Language, themes,...) management index file        //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path="..";
///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$menu="SETTINGS";
$obminclude = getenv("OBM_INCLUDE_VAR");
include("$obminclude/global.inc");
require("$obminclude/phplib/obmlib.inc");

page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");
$uid = $auth->auth["uid"];

if ($param_lang != "") {
  $set_lang=$param_lang;
  $sess->register("set_lang");
  run_query_set_options_user($uid, "set_lang", $set_lang, 1);
}
if ($param_theme != "") {
  $set_theme=$param_theme;
  $sess->register("set_theme");
  run_query_set_options_user($uid, "set_theme", $set_theme, 1);
}

// Validate user preferences

if ($form_user_pref) {

  $param_debug = $param_debug_id | $param_debug_param | $param_debug_sql;
  $set_debug=$param_debug;
  $sess->register("set_debug");
  run_query_set_options_user($uid, "set_debug", $set_debug, 1);

  if ($param_display == "yes") {
    $set_display = "yes";
  } else {
    $set_display = "no";
  }
  $sess->register("set_display");
  run_query_set_options_user($uid, "set_display", $set_display, 1);

  if ($param_rows != "") {
    $set_rows=$param_rows;
    $sess->register("set_rows");
    run_query_set_options_user($uid, "set_rows", $set_rows, 1);
  }
}
page_close();

require("$obminclude/global_pref.inc");
require("settings_display.inc");

if (($set_debug & $cdg_id) == $cdg_id) $dg_id = "checked";
if (($set_debug & $cdg_param) == $cdg_param) $dg_param = "checked";
if (($set_debug & $cdg_sql) == $cdg_sql) $dg_sql = "checked";

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_title . $set_lang);     // Head & Body
generate_menu($menu);                   // Menu
display_bookmarks();


if ($col_a_table != "") {
  $lbgcolor = " bgcolor=\"#$col_a_table\"";
} else {
  $lbgcolor = "";
}

///////////////////////////////////////////////////////////////////////////////
// Debug block (admin only)
///////////////////////////////////////////////////////////////////////////////
if ($auth->auth["perm"] == $perms_admin) {
  $dis_debug = "
  <tr>
    <td align=center $lbgcolor>
      <font color=\"#$col_a_text\">$l_set_debug ($set_debug)</font></td>
    <td $lbgcolor><font color=\"#$col_a_text\">
      <input type=hidden name=param_debug value=1>
      <input type=checkbox name=param_debug_id value=\"$cdg_id\" $dg_id>$l_dg_id
      <input type=checkbox name=param_debug_param value=\"$cdg_param\" $dg_param>$l_dg_param
      <input type=checkbox name=param_debug_sql value=\"$cdg_sql\" $dg_sql>$l_dg_sql
      </font>
    </td>
  </tr>";
}

///////////////////////////////////////////////////////////////////////////////
// For each LANGUAGE directory in the lang direcory (but ., .., CVS)         //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$lang_dir = dir("$path/../$obminclude/lang");
$dis_lang = "<table border=0>";
while($entry=$lang_dir->read()) {
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
      && is_dir($lang_dir->path."/".$entry)) {
    $dis_lang .= "<tr>
      <td>
        <a href=\"" . $sess->url("settings_index.php?param_lang=$entry") . "\">
        <img border=0 align=middle src=\"/images/images/flag-$entry.gif\"></a>
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
$dis_theme = "<table border=0>";
while($entry=$theme_dir->read()) {
$dotcase = strcmp($entry, "."); 
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
       && is_dir($theme_dir->path."/".$entry)) {
    $dis_theme .= "<tr>
      <td align=center>
        <a href=\"".$sess->url("settings_index.php?param_theme=$entry") .
        "\"><img border=0 align=middle src=\"/images/$entry/$entry.jpg\"></a>
      </td>
      </tr>";
  }
}
$dis_theme .= "</table>";
$theme_dir->close();


///////////////////////////////////////////////////////////////////////////////
// HTML Display
///////////////////////////////////////////////////////////////////////////////
echo "
  <center>

<!-- User preferences current config -------------------------------------- -->

  <form action=\"".$sess->url("settings_index.php")."\" method=get>
  <table border=1>
  <tr>
    <td align=center colspan=2 bgcolor=\"#$col_a_tableh\">
      <font color=\"#$col_textem\">$l_cur_settings</font></td>
  </tr><tr>
    <td align=center $lbgcolor>
      <font color=\"#$col_a_text\">$l_auto_display</font></td>
    <td $lbgcolor><input type=checkbox name=param_display value=yes ";
if ($set_display == "yes") echo "checked";
echo "></td>
  </tr><tr>
    <td align=center $lbgcolor>
      <font color=\"#$col_a_text\">$l_set_rows</font></td>
    <td $lbgcolor><input size=3 name=param_rows value=\"$set_rows\"></td>
  </tr>
  $dis_debug
  <tr>
    <td colspan=2 align=center $lbgcolor><font color=\"#$col_a_text\">
      <input name=form_user_pref type=hidden value=\"1\">
      <input name=submit type=submit value=\"$l_validate\"></font>
    </td>
  </tr>
  </table>
  </form>
  <p>

<!-- Lang and theme current config ---------------------------------------- -->

  <table border=1>
  <tr bgcolor=\"#$col_a_tableh\">
    <td align=center><font color=\"#$col_textem\">$l_cur_lang</font></td>
    <td align=center><font color=\"#$col_textem\">$l_cur_theme</font></td>
  </tr><tr>
    <td align=center $lbgcolor>
      <img align=middle src=\"/images/images/flag-$set_lang.gif\">
    </td>
    <td align=center $lbgcolor>
      <img align=middle src=\"/images/$set_theme/$set_theme.jpg\">
    </td>
  </tr>
  </table>

<!-- Display available configs -------------------------------------------- -->

  <p>$l_new_settings
  <p>
  <table border=1>
  <tr bgcolor=\"#$col_a_tableh\">
    <td align=center>
      <font color=\"#$col_textem\">&nbsp; $l_set_lang &nbsp;</font></td>
    <td align=center>
      <font color=\"#$col_textem\">&nbsp; $l_set_theme &nbsp;</font></td>
  </tr><tr>
    <td align=center $lbgcolor>
      $dis_lang
    </td>
    <td align=center $lbgcolor>
      $dis_theme
    </td>
  </tr>
  </table>

  </center>
  </body>
  </html>";

</SCRIPT>
