<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : company_js.inc                                              //
//     - Desc  : Company javascript functions File                           //
// 2000-01-13 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


echo "
<script type=\"text/javascript\">
<!--
";

require("$obminclude/javascript/check_js.inc");

echo "

function check_get_id(form) {
  comp_pos = form.param_company.selectedIndex;
  if (! (comp_pos >= 0)) {
    alert (\"$l_j_select_company\");
    return false;
  }
  valeur = form.param_company.options[comp_pos].value;
  text = form.param_company.options[comp_pos].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_company\");
    return false;
  } else {
    window.opener.document.form_mod_contact.company_new_id.value=valeur;
    window.opener.document.form_mod_contact.company_new_name.value=text;
    window.close();
    return true;
  }
}


function check_get_id_url(form, p_url) {
  comp_pos = form.param_company.selectedIndex;
  if (! (comp_pos >= 0)) {
    alert (\"$l_j_select_company\");
    return false;
  }
  valeur = form.param_company.options[comp_pos].value;
  text = form.param_company.options[comp_pos].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_company\");
    return false;
  } else {
    new_url = p_url + valeur;
    window.opener.location.href=new_url;
    window.close();
    return true;
  }
}


function confirm_del() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}


function check_company(form) {

  // MANDATORY : Check that the Company name is not empty
  if (trim(form.tf_name.value) == \"\") {
    alert (\"$l_fill_name\");
    return false;
  }

  // Check the zip code
  lzip = form.tf_zip.value;
  if ((lzip != \"\") && (check_zip(lzip) == null)) {
    alert (\"$l_j_check_zip\");
    return false;
  }

  // Check the phone number
  lphone = form.tf_phone.value;
  if ((lphone != \"\") && (check_phone(lphone) == null)) {
    alert (\"$l_j_check_phone\");
    return false;
  }

  // Check the fax number
  lfax = form.tf_fax.value;
  if ((lfax != \"\") && (check_fax(lfax) == null)) {
    alert (\"$l_j_check_fax\");
    return false;
  }

  // Check the Email
  lemail = form.tf_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
    return false;
  }

  // Check the web
  lweb = form.tf_web.value;
  if ((lweb != \"\") && (check_web(lweb) == null)) {
    alert (\"$l_j_check_web\");
    return false;
  }
  else
    return true;
} 


///////////////////////////////////////////////////////////////////////////////
// Check if the kind check or deletion is possible                           //
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


///////////////////////////////////////////////////////////////////////////////
// Check if the kind update is possible                                    //
///////////////////////////////////////////////////////////////////////////////
function check_kind_upd(form,formd)
{
  kind_pos = formd.sel_kind.selectedIndex;
  if ((kind_pos >= 0) && (form.tf_kind.value != \"\")) {
    form.sel_kind.value=formd.sel_kind.options[kind_pos].value;
    return true;
  } else {
    alert(\"$l_j_kind_update\");
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the kind creation is possible                                    //
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


///////////////////////////////////////////////////////////////////////////////
// Check if the activity check or deletion is possible                       //
///////////////////////////////////////////////////////////////////////////////
function check_act_checkdel(form)
{
   if (form.sel_act.selectedIndex >= 0)
   {
      return true;
   }
   else
   {
      alert(\"$l_j_act_delete\");
      return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the activity update is possible                                  //
///////////////////////////////////////////////////////////////////////////////
function check_act_upd(form,formd)
{
  act_pos = formd.sel_act.selectedIndex;
  if ((act_pos >= 0) && (form.tf_act.value != \"\")) {
    form.sel_act.value=formd.sel_act.options[act_pos].value;
    return true;
   } else {
     alert(\"$l_j_act_update\");
     return false;
   }
}


///////////////////////////////////////////////////////////////////////////////
// Check if the activity creation is possible                                //
///////////////////////////////////////////////////////////////////////////////
function check_act_new(form)
{
   if (form.tf_act.value != \"\")
      return true;
   else
   {
      alert(\"$l_j_act_create\");
      return false;
   }
}


// -->
</script>
";
