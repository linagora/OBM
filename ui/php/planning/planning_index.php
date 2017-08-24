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
// OBM - File : planning_index.php                                           //
//     - Desc : Planning Index File                                          //
// 2008-10-14 Guillaume Lardon                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) --                -- show the planning for a month
// - detailconsult   -- plannedtask_id -- show the planned task detail
// - detailupdate    -- plannedtask_id -- show the planned task detail form
// - insert          -- form fields    -- insert the planned task
// - update          -- form fields    -- update the planned task
// - delete          -- plannedtask    -- delete the planned task
// - admin           --                -- admin index (type)
// - tasktypegroup_insert -- form fields    -- insert the tasktype group
// - tasktypegroup_update -- form fields    -- update the tasktype group
// - tasktypegroup_checklink -- $sel_ttg    -- check if ttg is used
// - tasktypegroup_delete -- $sel_ttg    -- delete the ttg
// - tasktype_update -- form fields    -- update the tasktype
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'planning';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_planning_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
update_planning_session_params();
include("$obminclude/global_pref.inc");
include('planning_display.inc');
include('planning_query.inc');
#require('obminclude/javascript/check_js.inc');
include("$obminclude/of/of_category.inc");

if ($action == '') $action = 'index';
get_planning_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
 if (($action == 'index') || ($action == '')) {
  $display['detail'] .= dis_planning_view($params);
  $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);
} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_plannedtask_form($action, '', $params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_plannedtask_detail($params['plannedtask_id']);
  $display['detail'] = html_plannedtask_consult($obm_q);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_planning_update_rights($params)) {
    $obm_q = run_query_plannedtask_detail($params['plannedtask_id']);
    $display['detail'] = html_plannedtask_form($action, $obm_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_plannedtask_data_form($params,$conflict_id)) {
    
    $retour = run_query_plannedtask_insert($params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg($l_insert_ok);
    } else {
      $display['msg'] .= display_err_msg($l_insert_error);
    }
    $params['date'] = of_isodate_convert($params['datebegin']);
    $display['detail'] = dis_planning_view($params);
    $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);
   
    // Form data are not valid
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'].= html_plannedtask_form($action, '', $params, $err['field'],$conflict_id);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_plannedtask_data_form($params,$conflict_id)) {
    $retour = run_query_plannedtask_update($params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg($l_update_ok);
    } else {
      $display['msg'] .= display_err_msg($l_update_error);
    }
    $params['date'] = of_isodate_convert($params['datebegin']);
    $display['detail'] = dis_planning_view($params);
    $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $obm_q = run_query_plannedtask_detail($params['plannedtask_id']);
    $display['detail'] = html_plannedtask_form($action, $obm_q, $params, $err['field'],$conflict_id);
  }

}
elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_plannedtask_delete($params['plannedtask_id']);
  if ($retour) {
    set_update_state();
    $display['msg'] .= display_ok_msg($l_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_delete_error);
  }
  $display['detail'] = dis_planning_view($params);
  $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);

}
elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_planning_admin_index();
}
elseif ($action == 'tasktypegroup_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktypegroup_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_tasktypegroup : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_tasktypegroup : $l_insert_error");
  }
  $display['detail'] = dis_planning_admin_index();

} elseif ($action == 'tasktypegroup_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktypegroup_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_tasktypegroup : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_tasktypegroup : $l_update_error");
  }
  $display['detail'] = dis_planning_admin_index();

} elseif ($action == 'tasktypegroup_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_tasktypegroup_links($params['tasktypegroup']);

} elseif ($action == 'tasktypegroup_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktypegroup_delete($params['tasktypegroup_id']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_tasktypegroup : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_tasktypegroup : $l_delete_error");
  }
  $display['detail'] = dis_planning_admin_index();
} elseif ($action == 'tasktype_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tasktype_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_header_tasktype : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_header_tasktype : $l_update_error");
  }
  $display['detail'] = dis_planning_admin_index();
}
else
pt_assert(0,__FILE__,__LINE__,"unknown action: $action");

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_planning);
$display['end'] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Planning parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_planning_params() {

  // Get global params
  $params = get_global_params('Planning');

  return $params;
}

///////////////////////////////////////////////////////////////////////////////
// Update session and parameters
///////////////////////////////////////////////////////////////////////////////
function update_planning_session_params() {
  global $params, $obm;
  global $cplanning_groups,$cplanning_default_group;
  
  // We retrieve the selected groups if any
  if (isset ($params['group_id']))
  // and set it in the user preferences (last)
  update_user_pref($obm['uid'],'last_planning',implode(',',$params['group_id']));
  else
  {
    //else we get them from preferences or default
    $group_ids = get_one_user_pref($obm['uid'],'last_planning');
    if(isset($group_ids))
    $params['group_id'] = explode(',',$group_ids[$obm['uid']]['value']);
    elseif(isset($cplanning_default_group))
    $params['group_id'] = array($cplanning_default_group);
    else
    $params['group_id'] = array();
  }

  // define the group list if unset in the configuration
  if(!is_array($cplanning_groups))
  {
    if($obm['group_id']>0)
    $cplanning_groups = array($obm['group_prod']);
  }
}

///////////////////////////////////////////////////////////////////////////////
// Planning Action 
///////////////////////////////////////////////////////////////////////////////
function get_planning_action() {
  global $params, $actions, $path;
  global $l_planning, $l_reporting, $l_header_consult,$l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['planning']['index'] = array (
    'Name'     => $l_planning,
    'Url'      => "$path/planning/planning_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

  $actions['planning']['reporting'] = array (
    'Name'     => $l_reporting,
    'Url'      => "$path/planning/planning_index.php?reportingmode=1",
    'Right'    => $cright_write,
    'Condition'=> array ('all') );

// New
  $actions['planning']['new'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                  );

// Detail Update
  $actions['planning']['detailupdate'] = array (
     'Right'    => $cright_write,
     'Condition'=> array ('None')
                              	   );

// Insert
  $actions['planning']['insert'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );
// Update
  $actions['planning']['update'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// Delete
  $actions['planning']['delete'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );
// Detail Consult
  $actions['planning']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/planning/planning_index.php?action=detailconsult",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );
  // Admin  
  $actions['planning']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/planning/planning_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

  // TaskType Group Insert
  $actions['planning']['tasktypegroup_insert'] = array (
    'Url'      => "$path/planning/planning_index.php?action=tasktypegroup_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // TaskType Group Update
  $actions['planning']['tasktypegroup_update'] = array (
    'Url'      => "$path/planning/planning_index.php?action=tasktypegroup_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

  // Task Type Group Checklink 
  $actions['planning']['tasktypegroup_checklink'] = array (
    'Url'      => "$path/planning/planning_index.php?action=tasktypegroup_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // Task Type Group Delete 
  $actions['planning']['tasktypegroup_delete'] = array (
    'Url'      => "$path/planning/planning_index.php?action=tasktypegroup_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                          );

  // TaskType Update
  $actions['planning']['tasktype_update'] = array (
    'Url'      => "$path/planning/planning_index.php?action=tasktype_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                           );

}

</script>
