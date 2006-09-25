<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : domain_js.inc                                                //
//     - Desc : Domain javascript functions File                             //
// 2003-08-22 Aliacom - Phan David                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function check_domain(form) {
  // MANDATORY : Check that the Domain title is not empty
  if (trim(form.tf_label.value) == \"\") {
    alert (\"$l_fill_title\");
    return false;
  }

  return true;
}
  
  ";
  
?>