<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : publication_js.inc                                              //
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

$extra_js .= "

function check_get_id(valeur,text) {
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_publication\");
    return false;
  } else {
    window.opener.document.form_mod_contact.publication_new_id.value=valeur;
    window.opener.document.form_mod_contact.publication_new_name.value=text;
    window.close();
    return true;
  }
}


function check_get_id_url(form, p_url) {
  comp_pos = form.param_publication.selectedIndex;
  if (! (comp_pos >= 0)) {
    alert (\"$l_j_select_publication\");
    return false;
  }
  valeur = form.param_publication.options[comp_pos].value;
  text = form.param_publication.options[comp_pos].text;
  if ((valeur < 1) || (valeur == null)) {
    alert (\"$l_j_select_publication\");
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


function check_publication(form) {

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
// Check if the cat creation is possible                                    //
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

-->
</script>
";
// -->
</script>
