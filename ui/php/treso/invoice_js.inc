<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : invoice_js.inc                                              //
//     - Desc  : invoice javascript functions File                           //
// 2001-08-03 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////


echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";


require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

// user input check in  html_invoice_search_form :
echo "
function check_form(f_inv){
  j_ht=f_inv.tf_amount_HT.value;
  j_ttc=f_inv.tf_amount_TTC.value;
  j_date_apres=f_inv.tf_date_after.value;
  j_date_avant=f_inv.tf_date_before.value;

  if (check_date(f_inv.tf_date_apres,\"".$l_date_apres."\")==false) {
   //alert(\"$l_invalid_date\");
   return false;
  }
  if (check_date(f_inv.tf_date_avant,\"".$l_date_avant."\")==false) {
   //alert(\"$l_invalid_date\");
   return false;
  }
  if ((j_ht!=\"\")&& check_money(j_ht)==null){
    alert(\"$l_check_money_ht\");
    return false;
  }
  if ((j_ttc!=\"\")&& check_money(j_ttc)==null){
    alert(\"$l_check_money_ttc\");
    return false;
  }
  if ((j_ht!=\"\")&&(j_ttc!=\"\")) {
    var ht = j_ht.value;
    var ttc = j_ttc.value;
    if (j_ht.value>j_ttc.value) {
     alert(\"$l_check_amounts\");
    }
    return false;
  }

  return true;
}";

// input check when creating an invoice
echo "
function check_invoice(f_inv) {
  var j_ht=f_inv.tf_amount_HT.value;
  var j_ttc=f_inv.tf_amount_TTC.value;
  j_number=f_inv.tf_number.value;
  j_status=f_inv.sel_status.value;
  j_date=f_inv.tf_date.value;
  j_nb_deals = f_inv.hd_nb_deals.value;
  if (f_inv.tf_label.value==\"\"){
   alert(\"$l_fill_label\");
   return false;
  }
  
  if (check_date(f_inv.tf_date,\"".$l_date."\")==false) {
   //alert(\"$l_invalid_date\");
   return false;
  }

  if ((j_ht!=\"\")&& check_money(j_ht)==null){
    alert(\"$l_check_money_ht\");
    return false;
  }
  if ((j_ttc!=\"\")&& check_money(j_ttc)==null){
    alert(\"$l_check_money_ttc\");
    return false;
  }
  if ((j_ht!=\"\")&&(j_ttc!=\"\")&&(j_ht!=0)&&(j_ttc!=0)&&(j_ht-j_ttc>0)){
    alert(\"$l_check_amounts\");
    return false;
  }
  // inout de la facture doit être renseigné :
  if ((f_inv.rd_inout[0].checked == \"0\") && (f_inv.rd_inout[1].checked == \"0\")){
    alert (\"$l_check_inout\");
    return false;
  }

  // on ne peut pas changer le inout de la facture si elle
  // est liée à des deals...
  if ((j_nb_deals != \"\") && (j_nb_deals > 0)) {
    j_inout_init = f_inv.hd_rd_inout.value;
    // on parcours les rd_inout, et on cherche celui
    // qui est checké
    var i;
    for (i=0; f_inv.rd_inout[i].checked != \"1\";i++) {};
    if (f_inv.rd_inout[i].value != j_inout_init) {
      alert (\"$l_changing_inout\");
      return false;
    }
  }

  // confirmation d'une création sans numéro de facture :
  if (j_number==\"\") {
    return (confirm(\"$l_no_number_confirm\"));
  }
  return true;
}"; 

echo "
function check_invoice_link(f_inv) {
  var j_ht=f_inv.tf_amount_HT.value;
  var j_ttc=f_inv.tf_amount_TTC.value;
  j_number=f_inv.tf_number.value;
  j_status=f_inv.sel_status.value;
  j_date=f_inv.tf_date.value;
  j_nb_deals = f_inv.hd_nb_deals.value;
  if (f_inv.tf_label.value==\"\"){
   alert(\"$l_fill_label\");
   return false;
  }
  
  if (check_date(f_inv.tf_date,\"".$l_date."\")==false) {
   //alert(\"$l_invalid_date\");
   return false;
  }

  if ((j_ht!=\"\")&& check_money(j_ht)==null){
    alert(\"$l_check_money_ht\");
    return false;
  }
  if ((j_ttc!=\"\")&& check_money(j_ttc)==null){
    alert(\"$l_check_money_ttc\");
    return false;
  }
  if ((j_ht!=\"\")&&(j_ttc!=\"\")&&(j_ht!=0)&&(j_ttc!=0)&&(j_ht-j_ttc>0)){
    alert(\"$l_check_amounts\");
    return false;
  }
  // confirmation d'une création sans numéro de facture :
  if (j_number==\"\") {
    return (confirm(\"$l_no_number_confirm\"));
  }
  return true;
}"; 

// deletion confirm 
echo "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

";

// automagic compute of the tas included amount...
// TODO : if result is NaN, popup an alert and give focus 
// back to HT or rate...
echo "
function compute_tax (form) {
 var rate = parseFloat(form.tf_tax_rate.value);
 var ht   = parseFloat(form.tf_amount_HT.value);

 if (isNaN (rate)) {
  alert (\"$l_rate_nan\");
  form.tf_rate.focus();
  form.tf_rate.select();
  return;
 }
 if (isNaN (ht)) {
  alert (\"$l_ht_nan\");
  form.tf_amount_HT.focus();
  form.tf_amount_HT.select();
  return;
 }

 form.tf_amount_TTC.value = (Math.round (ht * rate*100))/100;
}
";
echo "
// --> 
</SCRIPT>
";

//</SCRIPT>
