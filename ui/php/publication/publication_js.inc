<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : publication_js.inc                                          //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");


if (isset($params["ext_target"]) && $params["ext_target"] != "") {
  //if (isset($params["ext_target"])) {
  $extra_js .= "

  function check_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_publication\");
      return false;
    } else {
      window.opener.document.".$params["ext_target"].".publication_id.value=valeur;
      window.opener.document.".$params["ext_target"].".tf_title.value=text;
      window.close();
      return true;
    }
  }

";
}


$extra_js .= "
function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


function check_publication(form) {

  // MANDATORY : Check that the publication name is not empty
  if (trim(form.tf_title.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  return true;
}


function check_subscription(form) {
  // MANDATORY : Check that a publication has been selected
  if (!form.publication_id.value > 0) {
    alert (\"$l_j_select_publication\");
    return false;
  }

  // Check the quantity
  quantity = form.tf_quantity.value;
  if (quantity == \"\") {
    alert (\"$l_j_fill_quantity\");
    return false;
  }


    return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Check if the renew check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_renew_checkdel(form)
{
   if (form.sel_renew.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_renew_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the renew update is possible
///////////////////////////////////////////////////////////////////////////////
function check_renew_upd(form,formd)
{
  renew_pos = formd.sel_renew.selectedIndex;
  if ((renew_pos >= 0) && (form.tf_renew.value != \"\")) {
    form.sel_renew.value=formd.sel_renew.options[renew_pos].value;
    return true;
  } else {
    alert(\"$l_j_renew_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the renew creation is possible
///////////////////////////////////////////////////////////////////////////////
function check_renew_new(form)
{
   if (form.tf_renew.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_renew_create\");
      return false;
   }
}



";

?>