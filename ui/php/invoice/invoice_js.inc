<?php
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
