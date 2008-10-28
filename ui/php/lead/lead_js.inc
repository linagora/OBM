<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : lead_js.inc                                                 //
//     - Desc  : lead javascript functions File                              //
// 2007-04-30 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

$extra_js .= "
function check_lead (f_lead) {
  var retour = true;
  var errorMsg = \"\";
  if (f_lead.tf_name.value == \"\") {
    errorMsg += \"$l_fill_name\\n\";
	retour = false;
  }
  if (f_lead.tf_datealarm.value == \"\") {
	errorMsg += \"$l_fill_datealarm\\n\";
	retour = false;  
  }
  if (f_lead.company_new_name.value == \"\") {
    errorMsg += \"$l_fill_company\\n\";
    retour = false;
  }
  
  if (!retour) {
    alert(errorMsg);
  }
  
  return retour;
}


";
?>