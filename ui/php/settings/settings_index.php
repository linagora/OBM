<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : settings_index.php                                         //
//     - Desc  : Settings (Language, themes,...) management index file       //
// 1999-03-19 Pierre Baudracco - Last Update : 2001-09-29                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path="..";
///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/phplib/obmlib.inc");

page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");

require("$obminclude/global_query.inc");

if ($param_lang != "") {
  $set_lang=$param_lang;
  $sess->register("set_lang");
  run_query_set_options_user($auth->auth["uid"],"set_lang",$set_lang);
}
if ($param_theme != "") {
  $set_theme=$param_theme;
  $sess->register("set_theme");
  run_query_set_options_user($auth->auth["uid"],"set_theme",$set_theme);
}
if ($param_display == "yes") {
  $set_display="yes";
  $sess->register("set_display");
  run_query_set_options_user($auth->auth["uid"],"set_display",$set_display);
} else if ($submit != "") {
  $set_display = "no";
  $sess->register("set_display");
  run_query_set_options_user($auth->auth["uid"],"set_display",$set_display);
}
if ($param_debug != "") {
  $param_debug = $param_debug_id | $param_debug_param | $param_debug_sql;
  $set_debug=$param_debug;
  $sess->register("set_debug");
  run_query_set_options_user($auth->auth["uid"],"set_debug",$set_debug);
}
if ($param_rows != "") {
  $set_rows=$param_rows;
  $sess->register("set_rows");
  run_query_set_options_user($auth->auth["uid"],"set_rows",$set_rows);
}
page_close();

$menu="SETTINGS";
require("settings_display.inc");
include("$obminclude/global.inc");

if (($set_debug & $cdg_id) == $cdg_id) $dg_id = "checked";
if (($set_debug & $cdg_param) == $cdg_param) $dg_param = "checked";
if (($set_debug & $cdg_sql) == $cdg_sql) $dg_sql = "checked";

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_title . $set_lang);     // Head & Body
generate_menu($menu);                   // Menu
display_bookmarks();


///////////////////////////////////////////////////////////////////////////////
// Display the CURRENT configuration                                         //
///////////////////////////////////////////////////////////////////////////////
echo "\n<CENTER>
     <P>$l_cur_settings<P>";

if ($col_a_table != "") {
    $lbgcolor = " BGCOLOR=\"#$col_a_table\"";
} else {
    $lbgcolor = "";
}

// Configuration variables
echo "\n<TABLE border=1>
     <FORM ACTION=\"".$sess->self_url()."\" METHOD=POST>
     <TR><TD align=center $lbgcolor>
         <FONT COLOR=\"#$col_a_text\">$l_auto_display</FONT></TD>
       <TD $lbgcolor><INPUT TYPE=checkbox NAME=param_display value=yes ";
if ($set_display == "yes") echo "checked";
echo "></TD>
     </TR><TR>
<!-- ROWS Parameter -->
       <TD align=center $lbgcolor>
       <FONT COLOR=\"#$col_a_text\">$l_set_rows</FONT></TD><TD $lbgcolor>
       <INPUT SIZE=3 NAME=param_rows value=\"$set_rows\"></TD></TR>
     <TR><TD";

if ($auth->auth["perm"] == $perms_admin) {
  echo " align=center $lbgcolor>
       <FONT COLOR=\"#$col_a_text\">$l_set_debug ($set_debug)</FONT></TD>
       <TD $lbgcolor><FONT COLOR=\"#$col_a_text\">
         <INPUT TYPE=hidden NAME=param_debug value=1>
         <INPUT TYPE=checkbox NAME=param_debug_id value=\"$cdg_id\" $dg_id>$l_dg_id
         <INPUT TYPE=checkbox NAME=param_debug_param value=\"$cdg_param\" $dg_param>$l_dg_param
         <INPUT TYPE=checkbox NAME=param_debug_sql value=\"$cdg_sql\" $dg_sql>$l_dg_sql
       </font></TD>
     </TR><TR><TD";
}

echo " colspan=2 ALIGN=center $lbgcolor>\n<FONT COLOR=\"#$col_a_text\">
     <INPUT name=submit TYPE=submit value=\"$l_submit\"></FONT>
     </FORM>
     </TD></TR></TABLE>\n<P>";

// Lang and theme current config

echo "<TABLE border=1><TR BGCOLOR=\"#$col_tableh\">
   <TD align=center><FONT COLOR=\"#$col_textem\">$l_cur_lang</TD>
   <TD align=center><FONT COLOR=\"#$col_textem\">$l_cur_theme</TD>
   </TR><TR>
   <TD align=center>
     <IMG ALIGN=MIDDLE SRC=\"/images/images/flag-$set_lang.gif\">
   </TD>
   <TD align=center>
     <IMG ALIGN=MIDDLE SRC=\"/images/$set_theme/$set_theme.jpg\">
   </TD>
 </TR></TABLE>";

///////////////////////////////////////////////////////////////////////////////
// Display available CONFIGs                                                 //
///////////////////////////////////////////////////////////////////////////////
echo "<P>$l_new_settings<P><CENTER>
    <TABLE BORDER=1><TR BGCOLOR=\"#$col_tableh\">
      <TD ALIGN=center>
        <FONT COLOR=\"#$col_textem\">&nbsp; $l_set_lang &nbsp;</FONT></TD>
      <TD ALIGN=center>
        <FONT COLOR=\"#$col_textem\">&nbsp; $l_set_theme &nbsp;</FONT></TD>
    </TR><TR>
      <TD ALIGN=center>";

///////////////////////////////////////////////////////////////////////////////
// For each LANGUAGE directory in the lang direcory (but ., .., CVS)         //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$lang_dir = dir("$path/../$obminclude/lang");
echo "<TABLE BORDER=0>";
while($entry=$lang_dir->read()) {
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
      && is_dir($lang_dir->path."/".$entry)) {
    echo "<TR><TD><A HREF=\"".
      $sess->url("settings_index.php?param_lang=$entry") . "\">" .
      "<IMG BORDER=0 ALIGN=MIDDLE SRC=\"/images/images/flag-" . 
      $entry . ".gif\"></A>
      </TD></TR>";
  }
}
echo "</TABLE>";
$lang_dir->close();
 

echo "</TD>
      <TD align=center>";

///////////////////////////////////////////////////////////////////////////////
// For each THEME directory in the themes direcory (but ., .., CVS)          //
// display en entry                                                          //
///////////////////////////////////////////////////////////////////////////////
$theme_dir = dir("$path/../$obminclude/themes");
echo "<TABLE BORDER=0>";
while($entry=$theme_dir->read()) {
$dotcase = strcmp($entry, "."); 
  if (strcmp($entry, ".") && strcmp($entry,"..") && strcmp($entry,"CVS")
       && is_dir($theme_dir->path."/".$entry)) {
    echo "<TR>
        <TD align=center>
        <A HREF=\"".$sess->url("settings_index.php?param_theme=$entry") .
        "\"><IMG BORDER=0 ALIGN=MIDDLE SRC=\"/images/$entry/$entry.jpg\"></A>
        </TD>
      </TR>";
  }
}

echo "</TABLE>";
$theme_dir->close();

echo "</TD></TR></TABLE>
     </CENTER>
     </BODY>
     </HTML>";

</SCRIPT>
