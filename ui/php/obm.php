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
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");

if ($action == "logout") {
  include("$obminclude/global_pref.inc");
  $display["head"] = display_head("OBM Version $obm_version");
  $display["end"] = display_end();
  $display["detail"] = dis_logout_detail();
  run_query_logout();
  $auth->logout();
  $sess->delete();
  $action = "";
  display_page($display);
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


///////////////////////////////////////////////////////////////////////////////
// Display detail of logout page                                             //
///////////////////////////////////////////////////////////////////////////////
function dis_logout_detail() {

  $block = "
<table width=\"100%\">
<tr>
  <td width=\"20%\">
    <a href=\"http://www.aliacom.fr/\"><img align=\"middle\" border=\"0\" src=\"/images/standard/standard.jpg\"></a>$obm_version</td>
  <td width=\"5%\">&nbsp;</td>
  <td width=\"50%\" align=\"center\">
    <h1>OBM CONNECTION CLOSED</h1></td>
  <td width=\"25%\" align=\"center\">&nbsp;</td>
</tr>
<tr>
  <td align=\"center\">&nbsp;</td>
</tr>
<tr>
  <td align=\"center\" colspan=\"4\"><hr></td>
</tr>
</table>

<P>
<center>
<a href=\"obm.php\">click here to Login</a>
</center>";

  return $block;
}

</script>
