<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////

echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";

$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/javascript/check_js.inc");


echo "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function uncheck_days(data) {
 longueur=data.length;
 for(i=3;i<longueur;i++){
  data.elements[i].checked = 0;
 }

}
// -->
</SCRIPT>
";

