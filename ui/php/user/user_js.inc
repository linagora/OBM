<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : user_js.inc                                                 //
//     - Desc  : User javascript functions File                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


require("$obminclude/javascript/check_js.inc");

// to avoid a javascript error when ext_widget is not defined
if ($ext_widget != "") {
  $extra_js .= "

function fill_ext_form(int_form) {
   size = int_form.length;
   ext_field = window.opener.document.$ext_widget;
   for(i=0; i <size ; i++) {
     if(int_form.elements[i].type == 'checkbox'){
       if(int_form.elements[i].checked == true) {
	 ext_size = ext_field.length;
	 for(j=0; j< ext_size; j++) {
	   if('data-u-' + ext_field.options[j].value == int_form.elements[i].name) {
	     window.opener.document.$ext_widget.options[j].selected =true;
	   }
	 }
       }
     }
   }
}";
}

if ($ext_widget != "" && $ext_widget_text != ""){

  $extra_js .= "
  function check_user_get_id(valeur,text) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_user\");
      return false;
    } else {
      window.opener.document.$ext_widget.value=valeur;
      window.opener.document.$ext_widget_text.value=text;
      window.close();
      return true;
    }
  }";
}

$extra_js .="
  function check_user_get_id_url(p_url, valeur) {
    if ((valeur < 1) || (valeur == null)) {
      alert (\"$l_j_select_user\");
      return false;
    } else {
      new_url = p_url + valeur;
      window.opener.location.href=new_url;
      window.close();
      return true;
    }
  }
";


if ($ext_element != "") {
  require_once("$obminclude/of/of_select.inc");
}


$extra_js .= "
function valider_suppression() {
  if (confirm (\"$l_delete_confirm\"))
    return true;
  else
    return false;
}

function check_user(form) {
  // MANDATORY : Check that Login is not empty
  if (trim(form.tf_login.value) == \"\") {
    alert (\"$l_fill_login\");
    return false;
  }

  // Check the email
  lemail = form.tf_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
    return false;
  }

  return true;
}

";

?>