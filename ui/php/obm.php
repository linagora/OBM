<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
$section="";
$menu="";
$path = ".";
///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
require("$obminclude/phplib/obmlib.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");

include("$obminclude/global_pref.inc");

if ($action == "logout") {
  run_query_logout();
  $auth->logout();
  $sess->delete();
  $action = "";
  include("$obminclude/auth/logout.ihtml");
 // page_close();
  exit;

} else if ($action == "login") { 
  // Load and make session variables from Global and User preferences
  session_load_global_prefs();
  session_load_user_prefs();

  $obm_q = new DB_OBM;
  $query = "update UserObm set userobm_timelastaccess='".date("Y-m-d H:i:s")."' where userobm_id='".$auth->auth["uid"]."'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
echo "<html>
<head>
<title>$l_title - O.B.M.</title>
</head>";


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
echo "<BODY TEXT=\"#$col_text\" BACKGROUND=\"/images/$set_theme/$img_bg\" BGCOLOR=\"#$col_bg\" LINK=\"#$col_link\" VLINK=\"#$col_link\" marginwidth=0 marginheight=0 topmargin=0 leftmargin=0>";

generate_menu("","");
display_bookmarks(".");

echo "<b>OBM</b> version $obm_version - " . date("Y-m-d H:i:s");
echo "</BODY></HTML>";
</SCRIPT>
