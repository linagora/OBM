<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm.php                                                      //
//     - Desc : OBM Home Page (Login / Logout)                               //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
$section = "";
$menu = "";
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

if ($action == "logout") {
  include("$obminclude/global_pref.inc");
  run_query_logout();
  $auth->logout();
  $sess->delete();
  $action = "";
  include("$obminclude/auth/logout.ihtml");
  exit;

} else {
  include("$obminclude/global_pref.inc");
}

page_close();

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head("OBM Version $obm_version");
$display["header"] = generate_menu("","");

$display["detail"] = "
<center>
<b>OBM</b> version $obm_version - " . date("Y-m-d H:i:s") . "
</center>";
$display["end"] = display_end();

display_page($display);

</script>
