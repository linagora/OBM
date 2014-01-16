<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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

// Vérification à l'insertion d'un nouveau mode de paiement
function check_kind_new(form) {
  var errMsg='';
  var ret=true;
  if (form.tf_kind_label.value == '') {
    errMsg += '$l_j_fill_kind_label\\n';
    ret = false;
  }
  if (form.tf_kind_short_label.value == '') {
    errMsg += '$l_j_fill_kind_short_label';
    ret = false;
  }
  
  if (!ret) {
    alert(errMsg);
  }
  
  return ret;
}

// Copy a field value into another field
function copyFieldValue(sourceField, targetField) {
  if (sourceField !== undefined && targetField !== undefined) {
    targetField.value = sourceField.value;
  }
}
";
?>