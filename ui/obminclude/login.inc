<script language="php">
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
 
global $set_lang_default, $set_theme_default, $c_singleNameSpace;

require_once("obminclude/of/of_captcha.php");

$login_javascript_footer = "
<script type=\"text/javascript\">
  if (document.forms.loginform.login.value == '') {
    document.forms.loginform.login.focus();
  } else {
    document.forms.loginform.password.focus();
  }
</script>
";

SI18n::set_locale($set_lang_default);

$theme_file = "resources/themes/$set_theme_default/theme.inc";
include_once($theme_file);

$lang_file = "obminclude/lang/$set_lang_default/login.inc";
include_once($lang_file);

$conf_login_page_incl = "$path/../conf/themes/login.inc";
if (file_exists($conf_login_page_incl)) {
  include "$conf_login_page_incl";
}

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

if ( isset($params['login'])) {
  if ( isset($params["auth_captcha_failed"]) ) {
    $error = $l_captcha_error;
  } else {
    $error = $l_error;
  }
}

$login_action = '';

$login = isset($params['login']) ? $params['login'] : '';

$captcha_formadd = OBM_captcha::getCaptchaHTML();

$template = get_template($lp);
eval ("\$template = \"$template\";");
echo $template;

</script>
