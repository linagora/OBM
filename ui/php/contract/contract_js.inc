<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : company_js.inc                                              //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco : Last Update 2000-01-13                      //
///////////////////////////////////////////////////////////////////////////////
// Last Update 2002/04/13 17:04:12 mehdi Exp                                 //
///////////////////////////////////////////////////////////////////////////////


echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";


require("$obminclude/lang/$set_lang/check_date_js.inc");
require("$obminclude/javascript/check_js.inc");

echo "function check_form(f_aff) { " .
       "if ((f_aff.tf_dateapres_aff.value != \"\") && " .
       "(check_date(f_aff.tf_dateapres_aff, \"" . 
       $l_date_alarm_after . "\") == false)) { " .
         "return false;" .
       "} " .
       "else if ((f_aff.tf_dateavant_aff.value != \"\") && " .
       "(check_date(f_aff.tf_dateavant_aff, \"" .
       $l_date_alarm_before . "\") == false)) { ".
         "return false;" .
       "} ". 
       "else ".
         "return true;" .
     "} ";

echo "
function verifie(fsoc) {
  Index = fsoc.param_company.selectedIndex;
  if ((Index == -1)) {
    alert (\"$l_j_select_company\");
    return false;
  }
  valeur = fsoc.param_company.options[Index].value;
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


echo "
function check_get_id(form) {
  valeur = form.param_contract.options[form.param_contract.selectedIndex].value;
  text = form.param_contract.options[form.param_contract.selectedIndex].text;
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
  valeur = form.param_contract.options[form.param_contract.selectedIndex].value;
  text = form.param_contract.options[form.param_contract.selectedIndex].text;
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

///////////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////////
echo "
function verifier_aff(f_aff) {
  if (trim(f_aff.tf_label_aff.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  }
  if (trim(f_aff.tf_label_aff.value.length) > 40) {
    alert (\"$l_length_label\");
    return false;
  }
  if (trim(f_aff.tf_number_contract.value.length) > 20) {
    alert (\"$l_length_number\");
    return false;
  }

  //else if (trim(f_aff.tf_number_deal.value) == \"\") {
  //  alert (\"$l_fill_number\")
  //  return false;
  //}
  else if (check_date(f_aff.tf_datefin_aff, \"$l_date_expir\") == false) {
    return false;
  }
  else if (check_date(f_aff.tf_datedebut_aff, \"$l_date_init\") == false) {
    return false;
  }
  else if (check_date(f_aff.tf_datealarme_aff, \"$l_date_alarm\") == false) {
    return false;
  }
  else if ((f_aff.tf_dateprop_aff.value != \"\") &&
          (check_date(f_aff.tf_dateprop_aff, \"$l_proposition\") == false)) {
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
function verifier_parentdeal(f_parent) {
  if (trim(f_parent.tf_label_parentdeal.value) == \"\") {
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
// Check if the Deal Type deletion is possible                               //
///////////////////////////////////////////////////////////////////////////////
function check_type_del(form)
{
   if (form.sel_type.selectedIndex >= 0)
   {
      if (confirm(\"$mj_type_delete_confirm\"))
         return true;
      else return false;
   }
   else
   {
      alert(\"$mj_type_delete\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Status deletion is possible                            //
///////////////////////////////////////////////////////////////////////////////
function check_status_del(form)
{  
   if (form.sel_status.selectedIndex >= 0)
   { 
      if (confirm(\"$mj_status_delete_confirm\"))
         return true;
      else return false;
   }
   else
   {
      alert(\"$mj_status_delete\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the Deal Category deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_category_del(form)
{
   if (form.sel_category.selectedIndex >= 0)
   {
      if (confirm(\"$mj_category_delete_confirm\"))
         return true;
      else return false;
   }
   else
   {
      alert(\"$mj_category_delete\");
      return false;
   }
}
";



echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the type update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_type_upd(form,formd)
{
   if (formd.sel_type.selectedIndex < 0){
      alert(\"$mj_type_update\");
      return false;
   }
   form.sel_type.value=formd.sel_type.options[formd.sel_type.selectedIndex].value;
   if ((formd.sel_type.selectedIndex >= 0) && (form.tf_type_upd.value != \"\"))
      return true;
   else {
      alert(\"$mj_type_update\");
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
   form.sel_status.value=formd.sel_status.options[formd.sel_status.selectedIndex].value;
   if ((formd.sel_status.selectedIndex >= 0) && (form.tf_status_upd.value != \"\"))
      return true;
   else {
      alert(\"$mj_status_update\");
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

   form.sel_category.value=formd.sel_category.options[formd.sel_category.selectedIndex].value;
   if ((formd.sel_category.selectedIndex >= 0) && (form.tf_category_upd.value != \"\")   && (form.tf_category_mini_upd.value != \"\"))
      return true;
   else {
      alert(\"$mj_category_update\");
      return false;
   }
}
";


echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the type creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_type_new(form)
{
   if (form.tf_type_new.value != \"\")
      return true;
   else
   {
      alert(\"$mj_type_create\");
      return false;
   }
}
";

echo "
///////////////////////////////////////////////////////////////////////////////
// Check if the status creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_status_new(form)
{
   if (form.tf_status_new.value != \"\")
      return true;
   else
   {
      alert(\"$mj_status_create\");
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
   if ((form.tf_category_new.value != \"\")
  && (form.tf_category_mini_new.value != \"\"))
     return true;
   else
   {
      alert(\"$mj_category_create\");
      return false;
   }
}
";

echo "
/////////////////////////////////////////////////////////////////////////////
// Input Wizards for the textField Mini-Label 
////////////////////////////////////////////////////////////////////////////

// when inserting a new category :
function fill_minilabel_new(form)   
{
	var chaine=new String(form.tf_category_new.value);
	form.tf_category_mini_new.value=chaine.substring(0,12);
}
	
// when updating an existing category :
function fill_minilabel_upd(form)
{
	var chaine=new String(form.tf_category_upd.value);
	form.tf_category_mini_upd.value=chaine.substring(0,12);
}
";



echo "
// -->
</SCRIPT>
";

