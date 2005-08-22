<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : publication_js.inc                                          //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script type=\"text/javascript\">
<!--
";

require("$obminclude/javascript/check_js.inc");


if (isset($publication["ext_target"]) && $publication["ext_target"]!="") {
  $extra_js .= "

  function check_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_publication\");
      return false;
    } else {
      window.opener.document.".$publication["ext_target"].".param_publication.value=valeur;
      window.opener.document.".$publication["ext_target"].".tf_title.value=text;
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
  // MANDATORY : Check that the Company name is not empty
  if (form.sel_publi.selectedIndex != 0 || !(form.sel_publi.options[form.sel_publi.selectIndex].value > 0)) {
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
// Check if the type check or deletion is possible                           //
///////////////////////////////////////////////////////////////////////////////
function check_type_checkdel(form)
{
   if (form.sel_type.selectedIndex >= 0)
   {
      return true;
   }
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
  type_pos = formd.sel_type.selectedIndex;
  if ((type_pos >= 0) && (form.tf_type.value != \"\")) {
    form.sel_type.value=formd.sel_type.options[type_pos].value;
    return true;
  } else {
    alert(\"$l_j_type_update\");
    return false;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the type creation is possible
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
// Check if the renew update is possible                                    //
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


///////////////////////////////////////////////////////////////////////////////
// Check if the recept check or deletion is possible
///////////////////////////////////////////////////////////////////////////////
function check_recept_checkdel(form)
{
   if (form.sel_recept.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_recept_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the recept update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_recept_upd(form,formd)
{
  recept_pos = formd.sel_recept.selectedIndex;
  if ((recept_pos >= 0) && (form.tf_recept.value != \"\")) {
    form.sel_recept.value=formd.sel_recept.options[recept_pos].value;
    return true;
  } else {
    alert(\"$l_j_recept_update\");
    return false;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Check if the cat creation is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_recept_new(form)
{
   if (form.tf_recept.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_recept_create\");
      return false;
   }
}


-->
</script>
";
// -->
</script>
