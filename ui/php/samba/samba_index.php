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
// OBM - File : samba_index.php                                              //
//     - Desc : Aliamin Samba Administration Index File                      //
// 2004-09-15 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --         -- show the Mail forwarding detail
// - detailupdate -- $system -- show the Mail forwarding detail form
// - update       -- $system -- update the Mail forwarding options
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "samba";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_samba_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("samba_display.inc");
require("samba_query.inc");

get_samba_action();
$perm->check_permissions($module, $action);


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_samba_consult();

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_samba_form($params);

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_data_form($params)) {
    $retour = run_query_samba_update($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
      $display["detail"] = dis_samba_consult();
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
      $display["detail"] = dis_samba_form($params);
    }
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = dis_samba_form($params, $err["field"]);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_samba);
$display["end"] = display_end();
$display["header"] = display_menu($module);

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Samba parameters transmitted in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_samba_params() {

  // Get global params
  $params = get_global_params("samba");

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Samba Action 
///////////////////////////////////////////////////////////////////////////////
function get_samba_action() {
  global $params, $actions, $path;
  global $l_header_consult, $l_header_update;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["samba"]["index"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/samba/samba_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Detail Update
  $actions["samba"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/samba/samba_index.php?action=detailupdate",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('index', 'update') 
                                     	   );

// Update
  $actions["samba"]["update"] = array (
    'Url'      => "$path/samba/samba_index.php?action=update",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                     );

}

?>
