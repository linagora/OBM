<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : invoice_js.inc                                              //
//     - Desc  : invoice javascript functions File                           //
// 2001-08-03 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

$extra_js .= "
// vérification des entrées de recherche :
// points à vérifier :
// number et amount : que des chiffres
// date_after et date_before doivent être des dates valides...
// idéalement, vérifier que date_after < date_before...
function check_search_form (f_pay){
  // number :
  if ((f_pay.tf_number.value!='')&&(check_number(f_pay.tf_number.value)==null)) {
   alert ('$l_no_number');
   return false;
  }
  // amount :
  if ((f_pay.tf_amount.value!='')&&(check_money(f_pay.tf_amount.value)==null)) {
   alert ('$l_check_amount');
   return false;
  }
  // date after, date before :
  if((f_pay.tf_date_after.value!='')&&(check_date(f_pay.tf_date_after,\"".$l_date_apres."\")==false)) {
   return false;
  }
  if((f_pay.tf_date_before.value!='')&&(check_date(f_pay.tf_date_before,\"".$l_date_avant."\")==false)) {
   return false;
  }
 return true;
}

// vérification des entrées à la création de paiements
function check_payment (f_pay) {
  if (f_pay.tf_label.value==\"\") {
   alert(\"$l_fill_label\");
   return false;
  }
  if (f_pay.tf_amount.value == \"\") {
    alert (\"$l_no_amount\");
    return false;
  }
  if ((f_pay.tf_amount.value != \"\") && (check_money(f_pay.tf_amount.value)==null)) {
    alert(\"$l_check_amount\");
    return false;
  }
  if (check_date(f_pay.tf_expected_date,\"".$l_expected_date."\")==false) {
   //alert(\"$l_invalid_date\"); // alert deja fait ds check_date ()
   return false;
  }
  if ((f_pay.rd_inout[0].checked == \"0\") && (f_pay.rd_inout[1].checked == \"0\")){
    alert (\"$l_check_inout\");
    return false;
  }
   return true;
}

// vérification des entrées à l'encaissement de paiements
// - the payment is being banked, it must have an account, a number and a kind.
function check_payment_banking (f_pay) {
  // first, we check fields that are always mandatory :
  if (f_pay.tf_label.value==\"\"){
   alert(\"$l_fill_label\");
   return false;
  }
  if ((f_pay.tf_amount.value!=\"\") && 
      (check_money (f_pay.tf_amount.value)==null)) {
    alert(\"$l_check_amount\");
    return false;
  }
  if (check_date(f_pay.tf_expected_date,\"".$l_expected_date."\")==false) {
   //alert(\"$l_invalid_date\"); // alert deja fait ds check_date ()
   return false;
  }
  // then we check <<critical>> fields
  if ((f_pay.tf_number.value!=\"\") && 
      (check_number (f_pay.tf_number.value)==null)) {
       alert(\"$l_no_number\");
       return false;
  }
  if (check_date (f_pay.tf_date,\"".$l_date."\")==false) {
    //alert(\"$l_invalid_date\"); // check_date() does the alert ;)
    return false;
  }
  if (f_pay.sel_kind.value == $c_all) {
    alert ('$l_check_paymentkind');
    return false;
  }
  if (f_pay.sel_account.value == $c_all) {
    alert ('$l_check_account');
    return false;
  }
/*  // we refuse to go on if the whole amount of the payment
  // or too much of it has been used
  var payment_amount = parseFloat(f_pay.tf_amount.value);
  var used_amount = parseFloat(f_pay.tf_instant_value.value);

  if (used_amount > payment_amount) {
    alert ('$l_too_much');
    return false;
  } else if (used_amount < payment_amount) {
    alert ('$l_not_enough');
    return false;
  }

  // we warn the user if there is an invoice that 
  // received too much money...
  for (var i = 0 ; i < f_pay.elements.length ; i++) {
    // if an invoice gets in tropuble, the field 
    // tf_still_not_paid_'number' is < 0
    if ( ((f_pay.elements[i].name).substr (0,17) == \"tf_still_not_paid\") &&
         (f_pay.elements[i].value < 0) ) {
      return (confirm ('$l_trouble_invoice'));
    }
  }
*/
  return true;
}

// confirmation de la suppression :
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


// evoqued on the banking screen to help the user
// affecting the payment
// share : part of the payment affected to that invoice/line
// old_share : older value of share (ie. before modification)
// not_paid_before : initial amount not paid on the invoice/line
// still_not_paid : amount not paid yet of that invoice/line
// instant : amount already used of the payment for all invoices/lines
function update_values (share, old_share, not_paid_before, still_not_paid, instant) {
  var v_before = parseFloat(not_paid_before.value);
  var v_old_share = parseFloat(old_share.value);
  // modified field : share
  var v_share;
  if (share.value == \"\") {
    v_share = 0;
  } else {
    v_share = parseFloat(share.value);
    if (isNaN(v_share)) {
    alert('$l_invalid_amount');
    share.focus();
    share.select();
    v_share= 0;
    }
  } 

  // fields to modify : 
  var v_still_not_paid = parseFloat(still_not_paid.value);
  var v_instant = parseFloat (instant.value);

  v_still_not_paid = v_before - v_share;
  v_instant = v_instant + v_share - v_old_share ;

  // updating the form :
  still_not_paid.value = v_still_not_paid;
  instant.value = v_instant;
  old_share.value = v_share;
}

";
