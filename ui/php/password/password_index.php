<?php
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


?>
<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : password.php                                                 //
//     - Desc : OBM Password Index File                                      //
// 2004-03-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --           -- show the Password form
// - update       -- $password -- update the User Password
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "password";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_password_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("password_display.inc");
require("password_query.inc");

if ($action == "") $action = "index";
get_password_action();
$perm->check_permissions($module, $action);


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_password($obm["uid"]);
  if ($obm_q->num_rows() == 1) {
    $display["detail"] = html_password_form($obm_q, $params);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_password($obm["uid"]);
  if (check_user_defined_rules() && check_data_form($obm_q, $params)) {
    $retour = run_query_password_update($params, $obm["uid"], $obm_q);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_password : $l_update_ok");
      $obm_q = run_query_password($obm["uid"]);
      $display["detail"] = html_password_form($obm_q, $params);
    } else {
      $display["msg"] = display_err_msg("$l_password : $l_update_error");
      $display["detail"] = html_password_form($obm_q, $params);
    }
  } else {
    $display["msg"] = display_err_msg("$l_password : $err[msg]");
    $display["detail"] = html_password_form($obm_q, $params, $err["field"]);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_password);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Password parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_password_params() {

  // Get global params
  $params = get_global_params("Password");

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Password Action 
///////////////////////////////////////////////////////////////////////////////
function get_password_action() {
  global $params, $actions, $path;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_module_password;

  // Password Index
  $actions["password"]["index"] = array (
    'Name'     => $l_module_password,
    'Url'      => "$path/password/password_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

  // Password Update
  $actions["password"]["update"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
                                    );

}

?>
