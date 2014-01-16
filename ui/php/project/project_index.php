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
// OBM - File : project_index.php                                            //
//     - Desc : Project Index File                                           //
// 2003-07-08 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions           -- Parameter
// - index (default) -- search fields  -- show the project search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new project form
// - detailconsult   -- $param_project -- show the project detail
// - detailupdate    -- $param_project -- show the project detail form
// - insert          -- form fields    -- insert the project
// - update          -- form fields    -- update the project
// - check_delete    -- $param_project -- check links before delete
// - delete          -- $param_project -- delete the project
// - task            -- $param_project -- show project tasks main screen
// - task_add        -- form fields    -- 
// - task_update     -- form fields    -- 
// - task_del        -- form fields    -- 
// - member          -- $param_project -- show project members main screen
// - sel_member      -- ext: user
// - member_add      -- form fields    --
// - member_del      -- form fields    -- 
// - member_update   -- form fields    -- 
// - allocate        -- $param_project -- 
// - allocate_update -- $param_project -- 
// - advance         -- $param_project -- show the project allocation/progress
// - advance_update  -- form fields    -- update project allocation/progress
// - dashboard       -- $param_project -- dashboard / stats of the project
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_id      -- $ext_params    -- select a deal (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'project';

$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_project_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");

require('project_query.inc');
require('project_display.inc');
require('project_js.inc');
include("$obminclude/of/of_category.inc");

update_last_visit('project', $params['project_id'], $action);

get_project_action();
$perm->check_permissions($module, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_id') {
  $display['search'] = dis_project_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_project_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'ext_get_id_cv') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_project_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_project_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'ext_get_task_id') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = display_project_task_selector($params);

} elseif ($action == 'ext_get_reftask_ids') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = display_project_reftask_selector($params);   

///////////////////////////////////////////////////////////////////////////////
// Standard calls
///////////////////////////////////////////////////////////////////////////////

} else if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_project_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_project_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_project_search_form($params);
  $display['result'] = dis_project_search_list($params);

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_project_form($action, '', $params);

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $display['detail'] = dis_project_consult($params['project_id']);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }
  
} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $params_q = run_query_project_detail($params['project_id']);
    $display['detailInfo'] = display_record_info($params_q);
    $display['detail'] = html_project_form($action, $params_q, $params);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_project_form('', $params)) {
    
    $params['project_id'] = run_query_project_insert($params);
    if ($params['project_id'] > 0) {
      $display['msg'] .= display_ok_msg("$l_project : $l_insert_ok");
      $display['detail'] = dis_project_consult($params['project_id']);
    } else {
      $display['msg'] .= display_err_msg("$l_project : $l_insert_error : $err[msg]");
    }
  } else { 
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = html_project_form($action, '', $params);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_project_form($params['project_id'], $params)) {
    $retour = run_query_project_update($params['project_id'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_project : $l_update_ok");
      $display['detail'] = dis_project_consult($params['project_id']);
    } else {
      $display['msg'] .= display_err_msg("$l_project : $l_update_error");
      $display['detail'] = dis_project_consult($params['project_id']);
    }
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = html_project_form($action, '', $params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_project_can_delete($params['project_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_project($params['project_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_project_consult($params['project_id']);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_project_can_delete($params['project_id'])) {
    $retour = run_query_project_delete($params['project_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_project : $l_delete_ok");
      $display['search'] = dis_project_search_form($params);
      if ($_SESSION['set_display'] == 'yes') {
        $display['result'] = dis_project_search_list($params);
      } else {
        $display['msg'] .= display_info_msg($l_no_display);
      }
    } else {
      $display['msg'] .= display_err_msg("$l_project : $l_delete_error");
      $display['detail'] = dis_project_consult($params['project_id']);
    }
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_project_consult($params['project_id']);
  }

} elseif ($action == 'task') {
///////////////////////////////////////////////////////////////////////////////
  $params['name'] = run_query_project_name($params['project_id']);
  $tasks_q = run_query_project_tasks($params['project_id']);
  $display['detail']  = html_project_task_form($tasks_q, $params);
  $display['detail'] .= html_project_tasklist($tasks_q, $params);

} elseif ($action == 'task_add') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_project_task_form($params['project_id'], $params)) {
    $retour = run_query_project_task_insert($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_task : $l_insert_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_task : $l_insert_error");
    }
    $params['name'] = run_query_project_name($params['project_id']);
    $tasks_q = run_query_project_tasks($params['project_id']);
    $display['detail'] = html_project_task_form($tasks_q, $params);
    $display['detail'] .= html_project_tasklist($tasks_q, $params);
  } else { 
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $params['name'] = run_query_project_name($params['project_id']);
    $tasks_q = run_query_project_tasks($params['project_id']);
    $display['detail']  = html_project_task_form($tasks_q, $params);
    $display['detail'] .= html_project_tasklist($tasks_q, $params);
  }

} elseif ($action == 'task_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_project_task_form($params['project_id'], $params)) {
    $retour = run_query_project_task_update($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_task : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_task : $l_update_error");
    }
    $params['name'] = run_query_project_name($params['project_id']);
    $tasks_q = run_query_project_tasks($params['project_id']);
    $display['detail'] = html_project_task_form($tasks_q, $params);
    $display['detail'] .= html_project_tasklist($tasks_q, $params);
  } else { 
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $params['name'] = run_query_project_name($params['project_id']);
    $tasks_q = run_query_project_tasks($params['project_id']);
    $display['detail']  = html_project_task_form($tasks_q, $params);
    $display['detail'] .= html_project_tasklist($tasks_q, $params);
  }

} elseif ($action == 'task_del') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['tsk_nb'] > 0) {
    $nb = run_query_project_task_delete($params);
    if ($nb == $params['tsk_nb']) {
      $display['msg'] .= display_ok_msg("$l_task : $l_delete_ok");
    } else {
      $display['msg'] .= display_warn_msg("$l_task : $l_delete_error");
    }
  } else {
    $display['msg'] .= display_err_msg("$l_no_task_del");
  }
  $params['name'] = run_query_project_name($params['project_id']);
  $tasks_q = run_query_project_tasks($params['project_id']);
  $display['detail'] = html_project_task_form($tasks_q, $params);
  $display['detail'] .= html_project_tasklist($tasks_q, $params);
      
} elseif ($action == 'member') {
///////////////////////////////////////////////////////////////////////////////
  $params['name'] = run_query_project_name($params['project_id']);
  $tasks_q = run_query_project_tasks($params['project_id']);
  $members_q = run_query_project_members($params['project_id']);
  $display['detail'] .= html_project_member_form($members_q, $params );

} elseif ($action == 'allocate') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $params['name'] = run_query_project_name($params['project_id']);
    $tasks_q = run_query_project_tasks($params['project_id']);
    $members_q = run_query_project_members($params['project_id']);
    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display['msg'] = display_warn_msg($l_no_allocation);
    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display['msg'] = display_warn_msg($l_no_allocation);
    } else {
      $allo_q = run_query_project_allocation($params['project_id']);
      $display['detail'] = html_project_allocate_form($tasks_q, $members_q, $allo_q, $params); 
    }
  }

} elseif ($action == 'advance') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $params['name'] = run_query_project_name($params['project_id']);
    $tasks_q = run_query_project_tasks($params['project_id']);
    $members_q = run_query_project_members($params['project_id']);
    if (($tasks_q == 0) or ($tasks_q->num_rows() == 0)) {
      $display['msg'] = display_warn_msg($l_no_allocation);
    } else if (($members_q == 0) or ($members_q->num_rows() == 0)) {
      $display['msg'] = display_warn_msg($l_no_allocation);
    } else {
      $allo_q = run_query_project_allocation($params['project_id']);
      $display['detail'] = html_project_advance_form($tasks_q, $members_q, $allo_q, $params); 
    }
  }

} elseif ($action == 'closing') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $display['detail'] = dis_project_closing($params);
  }

} elseif ($action == 'closing_new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_project_closing_form($action, $params);

} elseif ($action == 'closing_insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_project_closing_form($params)) {
    $params['closing_id'] = run_query_project_closing_insert($params);
    if ($params['closing_id'] > 0) {
      $display['msg'] .= display_ok_msg("$l_closing : $l_insert_ok");
      $display['detail'] = dis_project_closing($params);
    } else {
      $display['msg'] .= display_err_msg("$l_closing : $l_insert_error : $err[msg]");
    }
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_project_closing_form($action, $params);
  }

} elseif ($action == 'allocate_update') {
///////////////////////////////////////////////////////////////////////////////
//  if (check_member_form($params['project_id'], $params)) {
  $ins_err = run_query_project_allocate_update($params);
  if (!($ins_err)) {
    $display['msg'] .= display_ok_msg("$l_project : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_project : $l_update_error");
  }
  $display['detail'] = dis_project_consult($params['project_id']);

} elseif ($action == 'advance_update') {
///////////////////////////////////////////////////////////////////////////////
//  if (check_member_form($params['project_id'], $params)) {
  $ins_err = run_query_project_advance_update($params);
  if (!($ins_err)) {
    $display['msg'] .= display_ok_msg("$l_project : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_project : $l_update_error");
  }
  $display['detail'] = dis_project_consult($params['project_id']);

} elseif ($action == 'dashboard') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $display['detail'] = dis_project_dashboard($params);
  }

} elseif ($action == 'planning') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['project_id'] > 0) {
    $display['detail'] = dis_project_planning($params);
  }

} elseif ($action == 'member_add') {
///////////////////////////////////////////////////////////////////////////////
  $pid = $params['ext_id'];
  $params['project_id'] = $pid;
  $params['name'] = run_query_project_name($pid);
  if ($params['mem_nb'] > 0) {
    $nb = run_query_project_memberlist_insert($params);
    $display['msg'] .= display_ok_msg("$nb $l_member_added");
  } else {
    $display['msg'] .= display_err_msg("$l_no_member_add");
  }
  // gets updated infos
  $members_q = run_query_project_members($pid);
  $display['detail'] = html_project_member_form($members_q, $params);

} elseif ($action == 'member_del') {
///////////////////////////////////////////////////////////////////////////////
  $pid = $params['project_id'];
  $params['name'] = run_query_project_name($params['project_id']);
  if ($params['mem_nb'] > 0) {
    $nb = run_query_project_memberlist_delete($params);
    $display['msg'] .= display_ok_msg("$nb $l_member_removed");
    if ($nb != $params['mem_nb'])
      $display['msg'] .= display_warn_msg("$l_member : $l_delete_error");
  } else {
    $display['msg'] .= display_err_msg("$l_no_member_del");
  }
  // gets updated infos
  $members_q = run_query_project_members($pid);
  $display['detail'] = html_project_member_form($members_q, $params);

} elseif ($action == 'member_update') {
///////////////////////////////////////////////////////////////////////////////
  $pid = $params['project_id'];
  $params['name'] = run_query_project_name($pid);
  $retour = run_query_project_memberstatus_change($params);
  // gets updated infos
  $members_q = run_query_project_members($pid);
  $display['detail'] = html_project_member_form($members_q, $params);

} elseif ($action == 'document_add') {
///////////////////////////////////////////////////////////////////////////////
  $params['project_id'] = $params['ext_id'];
  if ($params['doc_nb'] > 0) {
    $nb = run_query_global_insert_documents_links($params, 'project');
    $display['msg'] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display['msg'] .= display_err_msg($l_no_document_added);
  }
  $display['detail'] = dis_project_consult($params['project_id']);

} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_project_admin_index();

} elseif ($action == 'reftask_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_project_reftask_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_reftask : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_reftask : $l_insert_error");
  }
  $display['detail'] .= dis_project_admin_index();

} elseif ($action == 'reftask_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_project_reftask_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_reftask : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_reftask : $l_update_error");
  }
  $display['detail'] .= dis_project_admin_index();

} elseif ($action == 'reftask_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_project_reftask_delete($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_reftask : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_reftask : $l_delete_error");
  }
  $display['detail'] .= dis_project_admin_index();

}  elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'project', 1);
  $display['detail'] = dis_project_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'project', 1);
  $display['detail'] = dis_project_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'project', 1);
  $display['detail'] = dis_project_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_project);
$display['end'] = display_end();
if (! $params['popup']) {
  // Update actions url in case some values have been updated (id after insert)
  update_project_action();
  $display['header'] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_project_params() {

  // Get global params
  $params = get_global_params('Project');

  // Get project specific params
  $nb_mem = 0;
  $nb_tsk = 0;

  /*  if (!isset ($params['date'])) {
    $params['date'] = date('Ymd'); 
  }
  */
  // Get project specific params
  
  foreach($params as $key => $value) {
    if (strcmp(substr($key, 0, 10),'data-task-') == 0) {
      $nb_tsk++;
      $tsk_num = substr($key, 10);
      $params["tsk$nb_tsk"] = $tsk_num;
    }
    else if (strcmp(substr($key, 0, 10),'data-user-') == 0) {
      $nb_mem++;
      $mem_num = substr($key, 10);
      $params["mem$nb_mem"] = $mem_num;
    } 
  }

  if (is_array($params['task_id'])) {
    foreach($params['task_id'] as $key => $value) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),'data-task-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['reftask_id'][] = $id;
      } else {
        // direct id
        $params['reftask_id'][] = $value;
      }
    }
  }
  
  $params['mem_nb'] = $nb_mem;
  $params['tsk_nb'] = $nb_tsk;
  
  get_global_params_document($params);

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Project Action 
///////////////////////////////////////////////////////////////////////////////
function get_project_action() {
  global $params, $actions, $path, $l_project;
  global $l_header_find,$l_header_new,$l_header_update, $l_header_delete;
  global $l_header_display, $l_header_add_member, $l_add_member;
  global $l_header_man_task, $l_header_man_member;
  global $l_header_closing, $l_header_closing_new;
  global $l_header_advance, $l_header_man_affect, $l_header_consult;
  global $l_header_dashboard,$l_header_planning,$l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// External call : select one project
  $actions['project']['ext_get_id'] = array (
    'Url'      => "$path/project/project_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     );
                                     
// External call (specific for CV) : select one project and update CV forms
  $actions['project']['ext_get_id_cv'] = array (
    'Url'      => "$path/project/project_index.php?action=ext_get_id_cv",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// External call : select one task
  $actions['project']['ext_get_task_id'] = array (
    'Url'      => "$path/project/project_index.php?action=ext_get_task_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

// External call : select one task
  $actions['project']['ext_get_reftask_ids'] = array (
    'Url'      => "$path/project/project_index.php?action=ext_get_retask_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );  
  
// Index
  $actions['project']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/project/project_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	 );

// Search
  $actions['project']['search'] = array (
    'Url'      => "$path/project/project_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                    	 );

// New
  $actions['project']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/project/project_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all','!new')
    );

// Detail Consult
  $actions['project']['detailconsult']  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/project/project_index.php?action=detailconsult&amp;project_id=".$params['project_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
  );


// Detail Update
  $actions['project']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/project/project_index.php?action=detailupdate&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
    );

// Insert
  $actions['project']['insert'] = array (
    'Url'      => "$path/project/project_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Update
  $actions['project']['update'] = array (
    'Url'      => "$path/project/project_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                    	 );

// Check Delete
  $actions['project']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/project/project_index.php?action=check_delete&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
                                     	      );

// Delete
  $actions['project']['delete'] = array (
    'Url'      => "$path/project/project_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	 );

// Task list
  $actions['project']['task'] = array (
    'Name'     => $l_header_man_task,
    'Url'      => "$path/project/project_index.php?action=task&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
    );

// Add a task
  $actions['project']['task_add'] = array (
    'Url'      => "$path/project/project_index.php?action=task_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Update a task
  $actions['project']['task_update'] = array (
    'Url'      => "$path/project/project_index.php?action=task_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Remove a task
  $actions['project']['task_del'] = array (
    'Url'      => "$path/project/project_index.php?action=task_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Member list
  $actions['project']['member'] = array (
    'Name'     => $l_header_man_member,
    'Url'      => "$path/project/project_index.php?action=member&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
                                     );

// Select members : Lists selection
  $actions['project']['sel_member'] = array (
    'Name'     => $l_header_add_member,
    'Url'      => "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_member)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path.'/project/project_index.php')."&amp;ext_id=".$params['project_id']."&amp;ext_target=$l_project",
    'Right'    => $cright_write,
    'Popup'    => 1,
    'Target'   => $l_project,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
                                    	  );

// Add a member
  $actions['project']['member_add'] = array (
    'Url'      => "$path/project/project_index.php?action=member_add",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Remove a member
  $actions['project']['member_del'] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Update member status
  $actions['project']['member_update'] = array (
    'Url'      => "$path/project/project_index.php?action=member_del",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                       );

// Time Allocation
  $actions['project']['allocate'] = array (
    'Name'     => $l_header_man_affect,
    'Url'      => "$path/project/project_index.php?action=allocate&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
                                     );

// Time allocation Update
  $actions['project']['allocate_update'] = array (
    'Url'      => "$path/project/project_index.php?action=allocate_update&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Advance
  $actions['project']['advance'] = array (
    'Name'     => $l_header_advance,
    'Url'      => "$path/project/project_index.php?action=advance&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
                                     );

// Advance Update
  $actions['project']['advance_update'] = array (
    'Url'      => "$path/project/project_index.php?action=advance_update&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Closing
  $actions['project']['closing'] = array (
    'Name'     => $l_header_closing,
    'Url'      => "$path/project/project_index.php?action=closing&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'member', 'task', 'alloate', 'advance', 'dashboard', 'planning', 'closing_new', 'closing_insert')
                                     );

// Closing new
  $actions['project']['closing_new'] = array (
    'Name'     => $l_header_closing_new,
    'Url'      => "$path/project/project_index.php?action=closing_new&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('closing')
                                        );

// Closing insert
  $actions['project']['closing_insert'] = array (
    'Url'      => "$path/project/project_index.php?action=closing_insert&amp;project_id=".$params['project_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Dashboard
   $actions['project']['dashboard'] = array (
     'Name'     => $l_header_dashboard,
     'Url'      => "$path/project/project_index.php?action=dashboard&amp;project_id=".$params['project_id'],
     'Right'    => $cright_read,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
                                       	 );

// Planning
  $actions['project']['planning']  = array (
    'Name'     => $l_header_planning,
    'Url'      => "$path/project/project_index.php?action=planning&amp;project_id=".$params['project_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('all','!reftask_checklink','!reftask_delete','!reftask_update','!reftask_insert','!admin','!ext_get_id','!index','!search','!new','!display','!dispref_display','!dispref_level')
  );

// Admin
  $actions['project']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/project/project_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Type Insert
  $actions['project']['reftask_insert'] = array (
    'Url'      => "$path/project/project_index.php?action=reftask_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Type Update
  $actions['project']['reftask_update'] = array (
    'Url'      => "$path/project/project_index.php?action=reftask_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Type Check Link
  $actions['project']['reftask_checklink'] = array (
    'Url'      => "$path/project/project_index.php?action=reftask_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Type Delete
  $actions['project']['reftask_delete'] = array (
    'Url'      => "$path/project/project_index.php?action=reftask_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

// Display
   $actions['project']['display'] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/project/project_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all')
                                       	 );

// Display Preferences
  $actions['project']['dispref_display'] = array (
    'Url'      => "$path/project/project_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Display Level
  $actions['project']['dispref_level']  = array (
    'Url'      => "$path/project/project_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     		 );

// Document add
  $actions['project']['document_add'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );

}


///////////////////////////////////////////////////////////////////////////////
// Project Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_project_action() {
  global $params, $actions, $path, $l_add_member, $l_project;

  // Detail Consult
  $actions['project']['detailconsult']['Url'] = "$path/project/project_index.php?action=detailconsult&amp;project_id=".$params['project_id'];

  // Detail Update
  $actions['project']['detailupdate']['Url'] = "$path/project/project_index.php?action=detailupdate&amp;project_id=".$params['project_id'];

  // Check Delete
  $actions['project']['check_delete']['Url'] = "$path/project/project_index.php?action=check_delete&amp;project_id=".$params['project_id'];

  // Tasks
  $actions['project']['task']['Url'] = "$path/project/project_index.php?action=task&amp;project_id=".$params['project_id'];

  // Members
  $actions['project']['member']['Url'] = "$path/project/project_index.php?action=member&amp;project_id=".$params['project_id'];

  // Select Member
  $actions['project']['sel_member']['Url'] = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_member)."&amp;ext_action=member_add&amp;ext_url=".urlencode($path.'/project/project_index.php')."&amp;ext_id=".$params['project_id']."&amp;ext_target=$l_project";

  // Time Allocation
  $actions['project']['allocate']['Url'] = "$path/project/project_index.php?action=allocate&amp;project_id=".$params['project_id'];

  // Advance
  $actions['project']['advance']['Url'] = "$path/project/project_index.php?action=advance&amp;project_id=".$params['project_id'];

  // Dashboard
  $actions['project']['dashboard']['Url'] = "$path/project/project_index.php?action=dashboard&amp;project_id=".$params['project_id'];

  // Planning
  $actions['project']['planning']['Url'] = "$path/project/project_index.php?action=planning&amp;project_id=".$params['project_id'];

}

?>
