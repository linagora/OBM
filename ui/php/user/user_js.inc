<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : user_js.inc                                                 //
//     - Desc  : User javascript functions File                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

echo "
<SCRIPT LANGUAGE=\"JavaScript\">
<!--
";
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/javascript/check_js.inc");
echo "
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
} // -->
</SCRIPT>
";

