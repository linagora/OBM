<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_js.inc                                               //
//     - Desc : Company javascript functions File                            //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


echo "
<script type=\"text/javascript\">
<!--
";


require("$obminclude/lang/$set_lang/check_date_js.inc");
require("$obminclude/javascript/check_js.inc");

echo "
function check_search_form(f_con) {
  if ((f_con.tf_dateafter.value != \"\") &&
      (check_date(f_con.tf_dateafter, \"$l_date_alarm_after\") == false)) {
    return false;
  }
  else if ((f_con.tf_datebefore.value != \"\") &&
       (check_date(f_con.tf_datebefore, \"$l_date_alarm_before\") == false)) {
    return false;
  } 
  else
    return true;
}


function check_contract(f_con) {
  if (trim(f_con.tf_label.value) == \"\") {
    alert (\"$l_fill_label\");
    return true;
  }
  else if (check_date(f_con.tf_datebegin, \"$l_date_init\") == false) {
    return false;
  }
  else if (check_date(f_con.tf_dateexp, \"$l_dateexp\") == false) {
    return false;
  }
  else
    return true;
}


function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


function check_get_id(form) {
  cont_pos = form.param_contract.selectedIndex;
  if (! (cont_pos >= 0)) {
    alert (\"$l_j_select_contract\");
    return false;
  }
  valeur = form.param_contract.options[cont_pos].value;
  text = form.param_contract.options[cont_pos].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_contract\");
    return false;
  } else {
    window.opener.document.form_mod_incident.param_contract.value=valeur;
    window.opener.document.form_mod_incident.contract_name.value=text;
    window.close();
    return true;
  }
}


function check_get_id_url(form, p_url) {
  cont_pos = form.param_contract.selectedIndex;
  if (! (cont_pos >= 0)) {
    alert (\"$l_j_select_contract\");
    return false;
  }
  valeur = form.param_contract.options[cont_pos].value;
  text = form.param_contract.options[cont_pos].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_contract\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Type deletion is possible                               //
///////////////////////////////////////////////////////////////////////////////
function check_type_del(form)
{
  if (form.sel_type.selectedIndex >= 0)
    return true;
  else
  {
    alert(\"$l_j_type_delete\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the type update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_type_upd(form,formd)
{
  if (formd.sel_type.selectedIndex < 0){
    alert(\"$l_j_type_update\");
    return false;
  }
  form.sel_type.value=formd.sel_type.options[formd.sel_type.selectedIndex].value;
  if ((formd.sel_type.selectedIndex >= 0) && (form.tf_type.value != \"\"))
    return true;
  else {
    alert(\"$l_j_type_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the type creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_type_new(form)
{
  if (form.tf_type.value != \"\")
    return true;
  else
  {
    alert(\"$l_j_type_create\");
    return false;
  }
}

// -->
</script>
";

