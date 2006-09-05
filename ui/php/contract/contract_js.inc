<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_js.inc                                               //
//     - Desc : Company javascript functions File                            //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget or ext_widget_label not defined
if (($ext_widget != "") || ($ext_widget_text != "")) {
  $extra_js .= "

function check_contract_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_contract\");
    return false;
  } else {
    window.opener.document.$ext_widget.value=valeur;
    window.opener.document.$ext_widget_text.value=text;
    window.close();
    return true;
  }
}";
}

$extra_js .= "

function check_contract_get_id_url(p_url, valeur) {
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

function check_contract(f_con) {
  if (trim(f_con.tf_label.value) == \"\") {
    alert (\"$l_fill_label\");
    return false;
  } else if (f_con.tf_datebegin.value == \"\") {
    alert(\"$l_begin\" + \" : $l_fill_date\");
    return false;
  } else if (f_con.tf_dateexp.value == \"\") {
    alert(\"$l_exp\" + \" : $l_fill_date\");
    return false;
  } else
    return true;
}


function confirm_contract_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_priority_checkdel(form)
{
   if (form.sel_priority.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_pri_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority update is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_priority_upd(form,formd)
{
  pri_pos = formd.sel_priority.selectedIndex;
  if ((pri_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_priority.value=formd.sel_priority.options[pri_pos].value;
    return true;
  } else {
    alert(\"$l_j_pri_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the priority creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_priority_new(form)
{
   if (form.tf_pri.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_pri_create\");
      return false;
   }
}



///////////////////////////////////////////////////////////////////////////////
// Check if the Contract Status deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_status_checkdel(form)
{  
   if (form.sel_status.selectedIndex >= 0)
   { 
     return true;
   }
   else
   {
     alert(\"$l_j_sta_delete\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the status update is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_status_upd(form,formd)
{
  status_pos = formd.sel_status.selectedIndex;
  if ((status_pos >= 0) && (form.tf_label.value != \"\")) {
    form.sel_status.value=formd.sel_status.options[status_pos].value;
    return true;
  } else {
    alert(\"$l_j_sta_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the status creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_status_new(form)
{
   if (form.tf_status.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_sta_create\");
      return false;
   }
}



///////////////////////////////////////////////////////////////////////////////
// Check if the Contract Type deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_contract_type_del(form)
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
function check_contract_type_upd(form,formd)
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
// Check the type of the contract                                            //
///////////////////////////////////////////////////////////////////////////////
function show_kind_contract(form) {

  return (form.rd_format[1].checked);
}



///////////////////////////////////////////////////////////////////////////////
// Check if the type creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_contract_type_new(form)
{
  if (form.tf_type.value != \"\")
    return true;
  else
  {
    alert(\"$l_j_type_create\");
    return false;
  }
}

";

?>
