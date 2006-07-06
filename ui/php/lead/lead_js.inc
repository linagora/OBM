<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : invoice_js.inc                                              //
//     - Desc  : invoice javascript functions File                           //
// 2001-08-03 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

$extra_js .= "
// vérification des entrées à la création de paiements
function check_payment (f_pay) {
  if (f_pay.tf_amount.value == \"\") {
    alert (\"$l_no_amount\");
    return false;
  }
  if ((f_pay.tf_amount.value != \"\") && (check_money(f_pay.tf_amount.value)==null)) {
    alert(\"$l_check_amount\");
    return false;
  }
  if ((f_pay.rd_inout[0].checked == \"0\") && (f_pay.rd_inout[1].checked == \"0\")){
    alert (\"$l_check_inout\");
    return false;
  }
   return true;
}


";
?>