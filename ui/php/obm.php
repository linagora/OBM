<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : obm.php                                                     //
//     - Desc  : OBM Home Page (Login / Logout)                              //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/phplib/obmlib.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");


// script continues here only if the user is authenticate
include("$obminclude/global.inc");
// make session variables from all the current user's preferences
session_load_preferences();
include("$obminclude/global_pref.inc");
$menu="";


if ($action == "logout") {
  run_query_logout();
  $auth->logout();
  $sess->delete();
  $action = "";
  include("$obminclude/auth/logout.ihtml");
 // page_close();
  exit;

} else if ($action == "login") { 
  $obm_q= new DB_OBM;
  $query="update UserObm set userobm_timelastaccess='".date("Y-m-d H:i:s")."' where userobm_id='".$auth->auth["uid"]."'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
echo "<HTML>
<HEAD>
<TITLE>$l_title - O.B.M.</TITLE>
</HEAD>";


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
echo "<BODY TEXT=\"#$col_text\" BACKGROUND=\"/images/$set_theme/$img_bg\" BGCOLOR=\"#$col_bg\" LINK=\"#$col_link\" VLINK=\"#$col_link\" marginwidth=0 marginheight=0 topmargin=0 leftmargin=0>";

generate_menu("");
display_bookmarks(".");

echo "<b>OBM</b> version " . $obm_version . " - " . date("Y-m-d H:i:s");
echo "</BODY></HTML>";
</SCRIPT>
