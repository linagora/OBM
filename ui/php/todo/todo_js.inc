<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_js.inc                                                  //
//     - Desc : Time javascript functions File                               //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_addtodoform(form) {

  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_j_fill_title\");
    return false;
  } 

  if (form.tf_deadline.value == \"\") {
    alert(\"$l_deadline\" + \" : $l_fill_date\");
    return false;
  } else
    return true;
}
";

?>