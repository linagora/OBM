<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_js.inc                                                  //
//     - Desc : Time javascript functions File                               //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";

require("$obminclude/javascript/check_js.inc");


echo "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}
";

echo "
function check_addtodoform(form) {

  if (!(check_fill_title(form))) {
    return false;
  }
  else
    return true;
}

function check_fill_title(form) {
  if (trim(form.tf_title.value == \"\")) {
    alert (\"$l_j_fill_title\");
    return false;
  } else
    return true;
}
";

echo "</script>";
