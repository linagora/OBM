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
// OBM - File : invoice_js.inc
//     - Desc : invoice javascript functions File
// 2001-08-03 Aliacom
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

$extra_js .= "

function check_get_id_url(p_url, valeur) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_invoice\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}

// user input check in  html_invoice_search_form :
function check_search_form(f_inv){
  j_ht=f_inv.tf_amount_ht.value;
  j_ttc=f_inv.tf_amount_ttc.value;

  if ((j_ht != \"\") && check_money(j_ht)==null){
    alert(\"$l_check_money_ht\");
    return false;
  }
  if ((j_ttc!=\"\")&& check_money(j_ttc)==null){
    alert(\"$l_check_money_ttc\");
    return false;
  }
  if ((j_ht!=\"\")&&(j_ttc!=\"\")) {
    if (parseFloat(j_ht) > parseFloat(j_ttc)) {
      alert(\"$l_check_amounts\");
      return false;
    }
  }

  return true;
}

// Invoice Form check
function check_invoice(f_inv) {
  var j_ht = f_inv.tf_amount_ht.value;
  var j_ttc = f_inv.tf_amount_ttc.value;
  j_number = f_inv.tf_number.value;
  j_status = f_inv.sel_status.value;

  if (f_inv.tf_label.value==\"\"){
   alert(\"$l_fill_label\");
   return false;
  }
  
  if (f_inv.tf_date.value == \"\") {
    alert(\"$l_fill_date\");
    return false;
  }

  if ((j_ht != \"\") && check_money(j_ht)==null){
    alert(\"$l_check_money_ht\");
    return false;
  }
  if ((j_ttc!=\"\") && check_money(j_ttc)==null){
    alert(\"$l_check_money_ttc\");
    return false;
  }
  if ( (j_ht != \"\") && (j_ttc != \"\") && (j_ht != 0) && (j_ttc != 0 )
       && (parseFloat(j_ht) > parseFloat(j_ttc)) ) {
    alert(\"$l_check_amounts aaa\");
    return false;
  }
  // inout de la facture doit être renseigné :
  if ((f_inv.rd_inout[0].checked == \"0\") && (f_inv.rd_inout[1].checked == \"0\")){
    alert (\"$l_fill_inout\");
    return false;
  }

  return true;
}


// deletion confirm 
function confirm_del() {
  return confirm (\"$l_delete_confirm\");
}


// automagic compute of the tas included amount...
// TODO : if result is NaN, popup an alert and give focus 
// back to HT or rate...
function compute_tax (form, auto) {
  var rate = parseFloat(form.sel_tax.options[form.sel_tax.selectedIndex].value);
  var ht   = parseFloat(form.tf_amount_ht.value);

  if (auto && (isNaN(ht) || isNaN(rate))) {
    return true;
  }

  if (isNaN (rate)) {
    alert (\"$l_rate_nan\");
    form.tf_rate.focus();
    form.tf_rate.select();
    return;
  }
  if (isNaN (ht)) {
    alert (\"$l_ht_nan\");
    form.tf_amount_ht.focus();
    form.tf_amount_ht.select();
    return;
  }

  form.tf_amount_ttc.value = (Math.round (ht * rate*100))/100;
}

";

?>
