<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : account_js.inc                                              //
//     - Desc  : account javascript functions File                           //
// 2001-08-01 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $I$ //
///////////////////////////////////////////////////////////////////////////////


echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";

$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

// user input checking in html_account_search_form()
echo "function check_form(f_acc){
  j_number=f_acc.tf_number.value;
  if ((j_number != \"\") && check_number(j_number)==null){
    alert (\"$l_check_number\"); 
    return false;
  }
  return true;
}";
echo "function check_cpte(f_acc){
 // moving spaces away
 f_acc.tf_number.value=f_acc.tf_number.value.replace(/ /g,\"\");

 if (f_acc.tf_bank.value==\"\") {
    alert ('$l_fill_bank');
    return false;
 }
 // label is mandatory
 if (f_acc.tf_label.value==\"\") {
  alert('$l_fill_label');
  return false;
 } 
 // number is mandatory
 if (f_acc.tf_number.value==\"\"){
  alert ('$l_fill_number');
  return false;
 }
 // number : only digits
 if (check_number(f_acc.tf_number.value) == null) {
  alert ('$l_check_number');
  return false;
 }
 return true;
}";

echo "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

";

// balance compute date checking
echo "
function check_balance_form (f_acc) {
 if (check_date (f_acc.tf_balance_date,\"".$l_choose_balance_date."\") == false) {
  return false;
 }
 return true;
}
";

echo "
// --> 
</SCRIPT>
";

//</SCRIPT>
