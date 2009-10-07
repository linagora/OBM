<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 
global $set_lang_default, $set_theme_default, $c_singleNameSpace;

$login_javascript_footer = "
<script type=\"text/javascript\">
  if (document.forms.login.login.value == '') {
    document.forms.login.login.focus();
  } else {
    document.forms.login.password.focus();
  }
</script>
";


$theme_file = "resources/themes/$set_theme_default/theme.inc";
include($theme_file);

$lang_file = "obminclude/lang/$set_lang_default/login.inc";
include($lang_file);

$conf_login_page = "$path/../conf/themes/login.html";
if (file_exists($conf_login_page)) {
  $lp = $conf_login_page;
} else {
  $lp = $login_template;
}

if(!$c_singleNameSpace) {
  $domain_id = isset($_REQUEST['sel_domain_id'])?$_REQUEST['sel_domain_id']:$c_default_domain;
  $sel_domain = of_domain_dis_select($domain_id);
  $domain = $l_domain;
}

if (isset($params['login'])) {
  $error = $l_error;
}

$login_action = '';

$login = isset($params['login']) ? $params['login'] : '';

$template = get_template($lp);
eval ("\$template = \"$template\";");
echo $template;

</script>
