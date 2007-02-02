<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
     <title><?php echo "Login - OBM Version $obm_version"; ?></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-15">
<style type="text/css">
<!--
body {
	background-image: url(<?php echo C_IMAGE_PATH; ?>/images/accueil_obm.png);
	background-repeat: no-repeat;
	background-position: center top;
}
tr.logintable {
	border: 1px dashed #CCCCCC;
	}
-->
</style>


<?php
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

  echo "
<script type=\"text/javascript\">
<!--
$function_crypt
// -->
</script>";

} else {
  // No encryption we passe the parameters as they are
  echo "
<script type=\"text/javascript\">
<!--
function doChallengeResponse() {
  document.login.submit();
}
// -->
</script>";
}

if ($c_multidomain) {
  $sel_domain = of_domain_dis_select();
  $sel_domain_list = "
    <tr><td><div align=\"right\"><font size=\"3\" face=\"sans-serif, Verdana, Arial\"><strong>DOMAIN
      :&nbsp;</strong></font></div></td>
    <td>$sel_domain</td></tr>";
}

?>


</head>

<?php
$conf_login_page = "$path/../conf/themes/login.html";
if (file_exists("$conf_login_page2")) {
  //  echo "OK_conf_login : ($conf_login_page)";
  include("$conf_login_page");
} else {
  //  echo "NO_conf_login ($conf_login_page)";
  include("obminclude/themes/login.html");
}

?>
</html>
