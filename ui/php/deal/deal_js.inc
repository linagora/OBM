<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : company_js.inc                                              //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";

$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/lang/$set_lang/check_date_js.inc");
require("$obminclude/javascript/check_js.inc");

///////////////////////////////////////////
// Deal Search Form checking (Dates,...) //
///////////////////////////////////////////
echo "function check_search_form(f_aff) {
  if ((f_aff.tf_dateafter.value != \"\") &&
      (check_date(f_aff.tf_dateafter, \"$l_date_alarm_after\") == false)) {
    return false;
  }
  else if ((f_aff.tf_datebefore.value != \"\") &&
        (check_date(f_aff.tf_datebefore, \"$l_date_alarm_before\") == false)) {
    return false;
  }
  else return true;
} ";

echo "
function verifie(fsoc) {
  valeur = fsoc.param_company.options[fsoc.param_company.selectedIndex].value;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_company\");
    return false;
  }
  else
    return true;
}
";

echo "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}
";

////////////////////////
// Deal Form checking //
////////////////////////
echo "
function check_form(f_aff) {
  if (trim(f_aff.tf_label.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }

  if (trim(f_aff.tf_datealarm.value) == \"\") {
    alert (\"$l_fill_datealarm\");
    return false;
  }

  if (trim(f_aff.tf_datebegin.value) == \"\") {
    alert (\"$l_fill_datebegin\");
    return false;
  }

  //else if (trim(f_aff.tf_num.value) == \"\") {
  //  alert (\"$l_fill_number\")
  //  return false;
  //}

  else if (check_date(f_aff.tf_datebegin, \"$l_date_init\") == false) {
    return false;
  }
  else if (check_date(f_aff.tf_datealarm, \"$l_date_alarm\") == false) {
    return false;
  }
  else if ((f_aff.tf_dateprop.value != \"\") &&
          (check_date(f_aff.tf_dateprop, \"$l_proposition\") == false)) {
    return false;
  }
  else
    return true;
}
";


echo "
function check_get_id(form) {
  valeur = form.param_deal.options[form.param_deal.selectedIndex].value;
  text = form.param_deal.options[form.param_deal.selectedIndex].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_deal\");
    return false;
  } else {
    window.opener.document.form_contract_update.param_deal.value=valeur;
    window.opener.document.form_contract_update.deal_name.value=text;
    window.close();
    return true;
  }
}


function check_get_id_url(form, p_url) {
  valeur = form.param_deal.options[form.param_deal.selectedIndex].value;
  text = form.param_deal.options[form.param_deal.selectedIndex].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_deal\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}
";


///////////////////////////////////////////////////////////////////////////////
// Check if the obligatory fields are set while creating a new ParentDeal    //
///////////////////////////////////////////////////////////////////////////////
echo "
function check_parentdeal(f_parent) {
  if (trim(f_parent.tf_plabel.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }
  else
    return true;
}
";


/////////////////////////////////////////////////////////////////////////////
// Check if the obligatory fields are set while creating a new ParentDeal  //
/////////////////////////////////////////////////////////////////////////////
echo "
function verifier_affect(f_affect) {
  if (f_affect.sel_parentdeal.value == null) {
    alert (\"$l_fill_parentdeal\");
    return false;
  }
  else
    return true;
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Kind deletion is possible                               //
///////////////////////////////////////////////////////////////////////////////
function check_kind_checkdel(form)
{
   if (form.sel_kind.selectedIndex >= 0)
   {
     return true;
   }
   else
   {
     alert(\"$l_j_kind_delete\");
     return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Status deletion is possible                            //
///////////////////////////////////////////////////////////////////////////////
function check_status_checkdel(form)
{  
   if (form.sel_state.selectedIndex >= 0)
   { 
     return true;
   }
   else
   {
     alert(\"$l_j_status_delete\");
     return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Category deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_cat_checkdel(form)
{
   if (form.sel_cat.selectedIndex >= 0)
   {
     return true;
   }
   else
   {
     alert(\"$l_j_category_delete\");
     return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the kind update is possible                                      //
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form,formd)
{
   form.sel_kind.value=formd.sel_kind.options[formd.sel_kind.selectedIndex].value;
   if ((formd.sel_kind.selectedIndex >= 0) && (form.tf_kind.value != \"\"))
      return true;
   else {
      alert(\"$l_j_kind_update\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the status update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_status_upd(form,formd)
{
   form.sel_state.value=formd.sel_state.options[formd.sel_state.selectedIndex].value;
   if ((formd.sel_state.selectedIndex >= 0) && (form.tf_status.value != \"\"))
      return true;
   else {
      alert(\"$l_j_status_update\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the category update is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_category_upd(form,formd)
{

   form.sel_cat.value=formd.sel_cat.options[formd.sel_cat.selectedIndex].value;
   if ((formd.sel_cat.selectedIndex >= 0) && (form.tf_cat.value != \"\"))
      return true;
   else {
      alert(\"$l_j_category_update\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Kind creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_kind_new(form)
{
   if (form.tf_kind.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_kind_create\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the status creation is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_status_new(form)
{
   if (form.tf_status.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_status_create\");
      return false;
   }
}
";

echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the category creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_category_new(form)
{
   if (form.tf_cat.value != \"\")
     return true;
   else
   {
      alert(\"$l_j_category_create\");
      return false;
   }
}
";


echo "
// -->
</SCRIPT>
";

