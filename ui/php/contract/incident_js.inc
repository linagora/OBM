<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_js.inc                                              //
//     - Desc  : Contact javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////

echo "
<script type=\"text/javascript\">
<!--
";


require("$obminclude/javascript/check_js.inc");
require("$obminclude/lang/$set_lang/check_date_js.inc");

echo "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function verifie(form) {
  Index = form.param_contract.selectedIndex;
  if ((Index == -1)) {
    alert (\"$l_j_select_contract\");
    return false;
  }
  valeur = form.param_contract.options[form.param_contract.selectedIndex].value;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_contract\");
    return false;
  } else {
    return true;
  };
}

function verifier_inc(f_aff) {

  if (f_aff.tf_dateafter.value != '' && check_date(f_aff.tf_dateafter, '".addslashes($l_date_after_js)."') == false) {
    return false;
  }
  if (f_aff.tf_datebefore.value != '' && check_date(f_aff.tf_datebefore,'".addslashes($l_date_before)."') == false) {
    return false;
  }
  return true;
}

function check_incident(form) {
  // MANDATORY : Check that Contact name is not empty
  if (trim(form.tf_lincident.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }
  if (form.tf_lincident.value.length > 100) {
    alert (\"$l_length_label\");
    return false;
  }
  if (check_date(form.tf_date,'".addslashes($l_incident_date)."') == false) { 
      return false;
    }
 return true;
}

function auto_archive(form) {
  if (form.sel_status.value == 'CLOSED')
    form.cb_archive.checked = 1;
  else
    form.cb_archive.checked = 0;
} 
-->
</script>
";
