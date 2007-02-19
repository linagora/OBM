<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

if ($auth_encryption) {
  if ($password_encryption == "md5") {
    include("$obminclude/auth/md5.js");
    $function_crypt = "
function doChallengeResponse() {
  str = document.login.login.value + \":\" +
        MD5(document.login.password.value) + \":\" +
        document.login.challenge.value;
  document.login.response.value = MD5(str);
  document.login.password.value = \"\";
}
";
  } else {
    // Crypt method : we do not handle javascript crypt encryption...
    $function_crypt = "
function doChallengeResponse() {
  document.login.submit();
}
";
  }

} else {
  // No encryption we passe the parameters as they are
    $function_crypt = "
function doChallengeResponse() {
  document.login.submit();
}
";
}

$login_javascript_header = "
<script type=\"text/javascript\">
<!--
  // Activate the appropriate input form field.
  if (document.login.login.value == '') {
    document.login.login.focus();
  } else {
    document.login.password.focus();
  }

$function_crypt
// -->
</script>";

$login_javascript_footer = "
<script type=\"text/javascript\">
<!--
  // Activate the appropriate input form field.
  if (document.login.login.value == '') {
    document.login.login.focus();
  } else {
    document.login.password.focus();
  }
// -->
</script>
";

// Failed login msg
if (isset($params["login"])) {
  $login_error_msg = "Either your login or your password are invalid or your session may have expired. Please try again !";
}

if ($c_multidomain) {
  $login_form_domain = of_domain_dis_select();
}

$login_form_header = "<form name=\"login\" method=\"post\" action=\"$login_action\">";

$login = isset($params["login"]) ? $params["login"] : "";
$login_form_login = "<input name=\"login\" type=\"text\" size=\"10\" value=\"$login\">";

$login_form_password = "<input name=\"password\" type=\"password\" size=\"10\">";

$login_form_values = "
<input type=\"hidden\" name=\"challenge\" value=\"$params[challenge]\">
<input type=\"hidden\" name=\"response\" value=\"\">";

$conf_login_page = "$path/../conf/themes/login.html";
if (file_exists("$conf_login_page")) {
  //  echo "OK_conf_login : ($conf_login_page)";
  include("$conf_login_page");
} else {
  //  echo "NO_conf_login ($conf_login_page)";
  include("obminclude/themes/login.html");
}


</script>
