<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : user_js.inc                                                 //
//     - Desc  : User javascript functions File                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
<script LANGUAGE=\"JavaScript\">
<!--
";

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
	   if('cb_u' + ext_field.options[j].value == int_form.elements[i].name) {
	     window.opener.document.$ext_widget.options[j].selected =true;
	   }
	 }
       }
     }
   }
 
}";
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

  // MANDATORY : Check that User password is not empty
  lpassword = form.tf_user_password.value;
  if (lpassword == \"\") {
    alert (\"$l_fill_password\");
    return false;
  }

  // Check the email
  lemail = form.tf_user_email.value;
  if ((lemail != \"\") && (check_email(lemail) == null)) {
    alert (\"$l_j_check_email\");
    return false;
  }

  return true;
}

// -->
</script>
";

