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
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailbox_index.php                                            //
//     - Desc : Mailbox Index File                                           //
// 2007-03-28 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - rights_admin    --                  -- see mailbox rights
// - rights_update   --                  -- update mailbox rights
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "mailbox";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_mailbox_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("mailbox_display.inc");
require("mailbox_query.inc");
require('obminclude/javascript/check_js.inc');
require("$obminclude/of/of_right.inc");
if (($action == "") || ($action == "index")) {
  $action = "rights_admin";
}

$profiles = get_all_profiles(false);

get_mailbox_action();
$perm->check_permissions($module, $action);

page_close();


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_mailshare_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_mailshare_search_list("");
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  $peer_profile_id = get_user_profile_id($params['entity_id']);
  if((Obm_Acl::isAllowed($obm['uid'], 'mailbox', $params['entity_id'], "admin")
      || check_mailbox_update_rights($params))
      || Perm::user_can_update_peer($obm['uid'], $profiles[$obm['profile']], $params['entity_id'], $profiles[$peer_profile_id])) {
    $display["detail"] = dis_mailbox_right_dis_admin($params["entity_id"]);
  } else {
    $err['msg'] = $l_insufficient_permission;
    $display['msg'] .= display_err_msg($err['msg']);
  }

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  $peer_profile_id = get_user_profile_id($params['entity_id']);
  if (Perm::user_can_update_peer($obm['uid'], $profiles[$obm['profile']],
      $params['entity_id'], $profiles[$peer_profile_id]) &&
      OBM_Acl_Utils::updateRights('mailbox', $params['entity_id'], $obm['uid'], $params)) {
    $mailbox_owner_login = get_user_login($params['entity_id']);
    update_mailbox_acl( $mailbox_owner_login, $obm['domain_id'] );
    $display["msg"] .= display_ok_msg("$l_rights : $l_update_ok");
  } else {
    $display["msg"] .= display_warn_msg($l_of_right_err_auth);
  }
  $display["detail"] = dis_mailbox_right_dis_admin($params["entity_id"]);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_mailbox);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_mailbox_params() {
  global $action, $cdg_param, $popup;
  global $cb_read_public, $cb_write_public,$sel_accept_write,$sel_accept_read;

  // Get global params
  $params = get_global_params("Mailbox");

  if ((isset ($params["entity_id"])) && (! isset($params["mailbox_id"]))) {
    $params["mailbox_id"] = $params["entity_id"];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Mailbox Action 
///////////////////////////////////////////////////////////////////////////////
function get_mailbox_action() {
  global $params, $actions, $path;
  global $l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Rights Admin.
  $actions["mailbox"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/mailbox/mailbox_index.php?action=rights_admin&amp;entity_id=".$params["mailbox_id"],
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                     );

// Rights Update
  $actions["mailbox"]["rights_update"] = array (
    'Url'      => "$path/mailbox/mailbox_index.php?action=rights_update&amp;entity_id=".$params["mailbox_id"],
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     );
}


</script>
