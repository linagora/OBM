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
echo "   <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
   <html xmlns=\"http://www.w3.org/1999/xhtml\"> 
    <head>
     <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />
     <title>$l_title - OBM Version $obm_version</title>
     <link rel=\"stylesheet\" type=\"text/css\" href=\"/images/$set_theme/style.css\" />
    </head>
    <body >";

generate_menu("","");

echo "<center>
<b>OBM</b> version $obm_version - " . date("Y-m-d H:i:s") . "
</center>
</body></html>";

</script>
