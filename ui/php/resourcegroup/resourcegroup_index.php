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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : resourcegroup_index.php                                      //
//     - Desc : Resource Group Index File                                    //
// 2005-03-15 Florent Goalabr�                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the group search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new group form
// - detailconsult   -- $param_resourcegroup  -- show the group detail
// - detailupdate    -- $param_resourcegroup  -- show the group detail form
// - insert          -- form fields    -- insert the group
// - update          -- form fields    -- update the group
// - check_delete    -- $param_resourcegroup  -- check delete the group
// - delete          -- $param_resourcegroup  -- delete the group
// - resourcegroup_add --                -- add groups
// - resourcegroup_del --                -- delete some resource of the group
// - resource_add    --                -- add resources
// - resource_del    --                -- delete some resource of the group
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple groups (url or sel) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "resourcegroup";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_resourcegroup_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("resourcegroup_display.inc");
require("resourcegroup_query.inc");
require("resourcegroup_js.inc");
require("$obminclude/of/of_right.inc");
require("$obminclude/of/of_category.inc");
get_resourcegroup_action();
$perm->check_permissions($module, $action);

if (! check_privacy($module, "RGroup", $action, $params["resourcegroup_id"], 0)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("resourcegroup", $params["resourcegroup_id"], $action);
}

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resourcegroup_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] .= dis_resourcegroup_search_resourcegroup("");
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resourcegroup_search_form($params);
  $display["result"] = dis_resourcegroup_search_resourcegroup($params);

} elseif ($action == "ext_search") {
///////////////////////////////////////////////////////////////////////////////
  $res_q = run_query_resourcegroup_ext_search($params);
  json_search_resourcegroups($params, $res_q);
  echo "(".$display['json'].")";
  exit();

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_resourcegroup_form($action, "", $params);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_update_rights($params)) {
    $obm_q = run_query_resourcegroup_detail($params["resourcegroup_id"]);
    $display["detail"] = html_resourcegroup_form($action, $obm_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_resourcegroup_data_form($params)) {

    // If the context (same group) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $params["resourcegroup_id"] = run_query_resourcegroup_insert($params);
      if ($params["resourcegroup_id"]) {
	$display["msg"] .= display_ok_msg("$l_resourcegroup : $l_insert_ok");
	$display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);
      } else {
	$display["msg"] .= display_err_msg("$l_resourcegroup : $l_insert_error");
	$display["detail"] = html_resourcegroup_form($action, "", $params);
      }
      
      // If it is the first try, we warn the user if some groups seem similar
    } else {
      $obm_q = check_resourcegroup_context("", $params);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_resourcegroup_warn_insert("", $obm_q, $params);
      } else {
	$params["resourcegroup_id"] = run_query_resourcegroup_insert($params);
	if ($params["resourcegroup_id"] > 0) {
	  $display["msg"] .= display_ok_msg("$l_resourcegroup : $l_insert_ok");
	  $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);
	} else {
	  $display["msg"] .= display_err_msg("$l_resourcegroup : $l_insert_error");
	  $display["detail"] = html_resourcegroup_form($action, "", $params);
	}
      }
    }
    
    // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = html_resourcegroup_form($action, "", $params);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_resourcegroup_data_form($params)) {
    $retour = run_query_resourcegroup_update($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_resourcegroup : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_resourcegroup : $l_update_error");
    }
    $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $params_q = run_query_resourcegroup_detail($params["resourcegroup_id"]);
    $display["detail"] = html_resourcegroup_form($action, $params_q, $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resourcegroup_warn_delete($params["resourcegroup_id"]);

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_resourcegroup_delete($params["resourcegroup_id"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_resourcegroup : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_resourcegroup : $l_delete_error");
  }
  $display["search"] = html_resourcegroup_search_form("");

} elseif ($action == "resource_add") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_update_rights($params)) {
    if ($params["resource_nb"] > 0) {
      $nb = run_query_resourcegroup_resourcegroup_insert($params);
      $display["msg"] .= display_ok_msg("$nb $l_resource_added");
    } else {
      $display["msg"] .= display_err_msg($l_no_resource_added);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);

} elseif ($action == "resource_del") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_update_rights($params)) {
    if ($params["resource_nb"] > 0) {
      $nb = run_query_resourcegroup_resourcegroup_delete($params);
      $display["msg"] .= display_ok_msg("$nb $l_resource_removed");
    } else {
      $display["msg"] .= display_err_msg($l_no_resource_deleted);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);

} elseif ($action == "resourcegroup_add") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_update_rights($params)) {
    if ($params["resourcegroup_nb"] > 0) {
      $nb = run_query_resourcegroupresourcegroup_insert($params);
      $display["msg"] .= display_ok_msg("$nb $l_resourcegroup_added");
    } else {
      $display["msg"] .= display_err_msg($l_no_resourcegroup_added);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);

} elseif ($action == "resourcegroup_del") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resourcegroup_update_rights($params)) {
    if ($params["resourcegroup_nb"] > 0) {
      $nb = run_query_resourcegroupresourcegroup_delete($params);
      $display["msg"] .= display_ok_msg("$nb $l_resourcegroup_removed");
    } else {
      $display["msg"] .= display_err_msg($l_no_resourcegroup_deleted);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }
  $display["detail"] = dis_resourcegroup_consult($params, $obm["uid"]);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm["uid"], "resourcegroup", 1);
  $prefs_r = get_display_pref($obm["uid"], "resourcegroup_resource", 1);
  $display["detail"] = dis_resourcegroup_display_pref($prefs, $prefs_r);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "resourcegroup", 1);
  $prefs_r = get_display_pref($obm["uid"], "resourcegroup_resource", 1);
  $display["detail"] = dis_resourcegroup_display_pref($prefs, $prefs_r);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "resourcegroup", 1);
  $prefs_r = get_display_pref($obm["uid"], "resourcegroup_resource", 1);
  $display["detail"] = dis_resourcegroup_display_pref($prefs, $prefs_r);

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
} else if ($action == "ext_get_ids") {
  $display["search"] = html_resourcegroup_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_resourcegroup_search_resourcegroup($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "get_json_resource_group") {
///////////////////////////////////////////////////////////////////////////////
  get_json_resource_group($params['res_id']);
  echo '({'.$display['json'].'})';
  exit();
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// Update actions url in case some values have been updated (id after insert) 
update_resourcegroup_action();
$display["head"] = display_head($l_resourcegroup);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $group hash, Group parameters transmited
// returns : $group hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_params() {

  // Get global params
  $params = get_global_params("ResourceGroup");

  //Get resourcegroup specific params
  if (isset ($params["ext_id"])) $params["resourcegroup_id"] = $params["ext_id"];

  $nb_u = 0;
  $nb_resourcegroup = 0;
  foreach($params as $key => $value ) {
    if (strcmp(substr($key, 0, 7),"data-r-") == 0) {
$nb_u++;
      $u_num = substr($key, 7);
      $params["resource$nb_u"] = $u_num;
    } elseif (strcmp(substr($key, 0, 7),"data-g-") == 0) {
$nb_resourcegroup++;
      $params_num = substr($key, 7);
      $params["resourcegroup_$nb_resourcegroup"] = $params_num;
    }
  }
  $params["resource_nb"] = $nb_u;
  $params["resourcegroup_nb"] = $nb_resourcegroup;


  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Group Action 
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_action() {
  global $params, $actions, $path, $l_resourcegroup;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $l_header_add_resource, $l_add_resource, $l_header_add_resourcegroup, $l_add_resourcegroup;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["resourcegroup"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// Search
  $actions["resourcegroup"]["search"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
  $actions["resourcegroup"]["ext_search"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
// New
  $actions["resourcegroup"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all')
                                  );

// Detail Consult
  $actions["resourcegroup"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=".$params["resourcegroup_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate')
                                  );

// Detail Update
  $actions["resourcegroup"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=detailupdate&amp;resourcegroup_id=".$params["resourcegroup_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'resource_add', 'resource_del', 'resourcegroup_add', 'resourcegroup_del', 'update')
                                     	   );

// Insert
  $actions["resourcegroup"]["insert"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=insert",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["resourcegroup"]["update"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["resourcegroup"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=check_delete&amp;resourcegroup_id=".$params["resourcegroup_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'resource_add', 'resource_del', 'resourcegroup_add', 'resourcegroup_del', 'update')
                                     	   );

// Delete
  $actions["resourcegroup"]["delete"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     );

// Ext get Ids : external Group selection
  $actions["resourcegroup"]["ext_get_ids"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	  );

// sel resourcegroup add : Groups selection
  $actions["resourcegroup"]["sel_resourcegroup_add"] = array (
    'Name'     => $l_header_add_resourcegroup,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resourcegroup)."&amp;ext_action=resourcegroup_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=".$params["resourcegroup_id"]."&amp;ext_target=$l_resourcegroup&amp;child_res=1",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_resourcegroup,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                    	  );

// Sel resource add : Resources selection
  $actions["resourcegroup"]["sel_resource_add"] = array (
    'Name'     => $l_header_add_resource,
    'Url'      => "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resource)."&amp;ext_action=resource_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=".$params["resourcegroup_id"]."&amp;ext_target=$l_resourcegroup",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_resourcegroup,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult','resource_add','resource_del', 'resourcegroup_add','resourcegroup_del', 'update')
                                    	  );

// Resource add
  $actions["resourcegroup"]["resource_add"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resource_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Resource del
  $actions["resourcegroup"]["resource_del"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resource_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Group add
  $actions["resourcegroup"]["resourcegroup_add"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resourcegroup_add",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Group del
  $actions["resourcegroup"]["resourcegroup_del"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=resourcegroup_del",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     );

// Display
  $actions["resourcegroup"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );
// Display
  $actions["resourcegroup"]["dispref_display"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );
// Display
  $actions["resourcegroup"]["dispref_level"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );

// JSON
  $actions["resourcegroup"]["get_json_resource_group"] = array (
    'Url'      => "$path/resourcegroup/resourcegroup_index.php?action=get_json_resource_group",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );

}


///////////////////////////////////////////////////////////////////////////////
// ResourceGroup Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_resourcegroup_action() {
  global $params, $actions, $path, $l_add_resource, $l_add_resourcegroup, $l_resourcegroup;

  $id = $params["resourcegroup_id"];
  if ($id > 0) {
    if (check_resourcegroup_update_rights($params, $g)) {

      // Detail Update
      $actions["resourcegroup"]["detailupdate"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=detailupdate&amp;resourcegroup_id=$id";
      $actions["resourcegroup"]["detailupdate"]['Condition'][] = 'insert';

      // Check Delete
      $actions["resourcegroup"]["check_delete"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=check_delete&amp;resourcegroup_id=$id";
      $actions["resourcegroup"]["check_delete"]['Condition'][] = 'insert';

      // Sel Resource add
      $actions["resourcegroup"]["sel_resource_add"]['Url'] = "$path/resource/resource_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resource)."&amp;ext_action=resource_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=$id&amp;ext_target=$l_resourcegroup";
      $actions["resourcegroup"]["sel_resource_add"]['Condition'][] = 'insert';

      // Sel group add : Groups selection
      $actions["resourcegroup"]["sel_resourcegroup_add"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_resourcegroup)."&amp;ext_action=resourcegroup_add&amp;ext_url=".urlencode($path."/resourcegroup/resourcegroup_index.php")."&amp;ext_id=$id&amp;ext_target=$l_resourcegroup&amp;child_res=1";
      $actions["resourcegroup"]["sel_resourcegroup_add"]['Condition'][] = 'insert';      

    } else {
      // User does not have update rights
      $actions['resourcegroup']['detailupdate']['Condition'] = array('None');
      $actions['resourcegroup']['check_delete']['Condition'] = array('None');
      $actions['resourcegroup']['sel_resource_add']['Condition'] = array('None');
      $actions['resourcegroup']['sel_resourcegroup_add']['Condition'] = array('None');
    }    
    // Detail Consult
    $actions["resourcegroup"]["detailconsult"]['Url'] = "$path/resourcegroup/resourcegroup_index.php?action=detailconsult&amp;resourcegroup_id=$id";
    $actions["resourcegroup"]["detailconsult"]['Condition'][] = 'insert';

  }
}

?>
