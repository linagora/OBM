<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_js.inc                                                  //
//     - Desc : Time javascript functions File                               //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
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
";

////////////////////////
// Deal Form checking //
////////////////////////
echo "
function check_form(f_aff) {

  if (! check_sel_tasktype(f_aff)) {
    return false;
  }
  else if (! check_sel_project(f_aff)) {
    return false;
  }
  else
    return true;
}
";

echo "
function check_sel_tasktype(form) {
  //  alert(\"value\" + form.sel_tasktype.selectedIndex);
  if (form.sel_tasktype.options[form.sel_tasktype.selectedIndex].value == 0) {
    alert (\"$l_j_select_tasktype\");
    return false;
  } else
    return true;
}

function check_sel_project(form) {
  //alert(\"value\" + form.sel_tasktype.selectedIndex);
  if ((form.sel_project.options[form.sel_project.selectedIndex].value == 0) &&
       (form.sel_tasktype.options[form.sel_tasktype.selectedIndex].value < 10)) {
    alert (\"$l_j_select_project\");
    return false;
  } else
    return true;
}
// -->
</SCRIPT>
";

