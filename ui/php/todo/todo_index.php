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
// OBM - File : company_index.php                                            //
//     - Desc : Company Index File                                           //
// 2003-09-15 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the todo index
// - insert             -- search fields  -- insert a new todo
// - delete             --                -- delete selected todos
// - delete_unique      --                -- delete one todo
// - update             --                -- update a todo
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'todo';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_todo_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
$max_priority = 5;
require('todo_query.inc');
require('todo_display.inc');
require_once('todo_js.inc');

get_todo_action();
$perm->check_permissions($module, $action);

page_close();


if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_todo_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_todo_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = dis_todo_search_form($params);
  $display['result'] .= dis_todo_search_list($params);

} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_todo_form($params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_todo_consult($params);

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_todo_data_form($params)) {
    $id = run_query_todo_insert($params);
    if ($id > 0) {
      $params['todo_id'] = $id;
      $display['msg'] .= display_ok_msg("$l_todo : $l_insert_ok");
      $display['detail'] .= dis_todo_consult($params);
    } else {
      $display['msg'] .= display_err_msg("$l_todo : $l_insert_error");
      $display['detail'] = dis_todo_form($params);
    }
  // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_todo_form($params);
  }

} else if ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_todo_delete($_POST);
  $display['msg'] = display_ok_msg("$l_todo : $l_delete_ok");
  $display['detail'] .= dis_todo_search_form($params);

} else if ($action == 'delete_unique') {
///////////////////////////////////////////////////////////////////////////////
  if (check_todo_can_delete($params['todo_id'])) {
    $retour = run_query_todo_delete_unique($params['todo_id']);
    if ($retour) {
      $display['msg'] = display_ok_msg("$l_todo : $l_delete_ok");
    } else {
      $display['msg'] = display_err_msg("$l_todo : $l_delete_error");
    }
    $display['detail'] .= dis_todo_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] .= dis_todo_consult($params);
  }

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $params_q = run_query_todo_detail($params);
  $display['detail'] = dis_todo_form($params, $params_q);

} else if ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_todo_data_form($params)) {
    $retour = run_query_todo_update($params);
    if ($popup) {
      $display['result'] .= "
      <script language=\"javascript\">
       window.opener.location.href=\"$path/todo/todo_index.php?action=index\";
       window.close();
      </script>";
    } else {
      $display['detail'] = dis_todo_consult($params);
    }

    // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_todo_form($params);
  }

}  elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'todo', 1);
  $display['detail'] = dis_todo_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'todo', 1);
  $display['detail'] = dis_todo_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'todo', 1);
  $display['detail'] = dis_todo_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Todo top list (same as the bookmarks : id and titles are registered)
///////////////////////////////////////////////////////////////////////////////
// If the todo list was updated, we reload the todo in session
if (in_array($action, array('insert', 'update', 'delete', 'delete_unique')))
  global_session_load_user_todos();


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  update_todo_action();     
  $display['header'] = display_menu($module);
}
$display['head'] = display_head($l_todo);
$display['end'] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $company hash
// returns : $company hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_todo_params() {
  
// Get global params
  $params = get_global_params('Todo');
  
  // Get todo specific params
  if (is_array($params['user_id'])) {
    while ( list( $key, $value ) = each($params['user_id'])) {
      // user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),'data-user-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['user_ids'][] = $id;
      } else {
        // direct id
        $params['user_ids'][] = $value;
      }
    }
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Todo Actions 
///////////////////////////////////////////////////////////////////////////////
function get_todo_action() {
  global $params, $actions, $path;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_header_find, $l_header_new, $l_header_delete, $l_header_update;
  global $l_header_consult, $l_header_admin, $l_header_display;

// Index
  $actions['todo']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/todo/todo_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	 );

// Search
  $actions['todo']['search'] = array (
    'Url'      => "$path/todo/todo_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                    	 );

// New
  $actions['todo']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/todo/todo_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index','search','insert','new','detailconsult','update','check_delete','delete','admin','display')
                                     );

// Detail Consult
  $actions['todo']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/todo/todo_index.php?action=detailconsult&amp;todo_id=". $params['todo_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'insert', 'update')
                                    	 );

// Insert a todo
  $actions['todo']['insert'] = array (
    'Url'      => "$path/todo/todo_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                    	 );

// Delete a list of todo
  $actions['todo']['delete'] = array (
    'Url'      => "$path/todo/todo_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// Detail Update
  $actions['todo']['detailupdate']  = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/todo/todo_index.php?action=detailupdate&amp;todo_id=". $params['todo_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                      );

// Update
  $actions['todo']['update']  = array (
    'Url'      => "$path/todo/todo_index.php?action=update&amp;todo_id=". $params['todo_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                      );

// Delete a todo
  $actions['todo']['delete_unique'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/todo/todo_index.php?action=delete_unique&amp;todo_id=". $params['todo_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update') 

                                     );

// Display
   $actions['todo']['display'] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/todo/todo_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Preferences
   $actions['todo']['dispref_display'] = array (
    'Url'      => "$path/todo/todo_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
   $actions['todo']['dispref_level']  = array (
    'Url'      => "$path/todo/todo_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}


///////////////////////////////////////////////////////////////////////////////
// Todo Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_todo_action() {
  global $params, $actions, $path, $cright_write_admin;

  $id = $params['todo_id'];
  if ($id > 0) {

    // Detail Consult
    $actions['todo']['detailconsult']['Url'] = "$path/todo/todo_index.php?action=detailconsult&amp;todo_id=$id";
    
    // Detail Update
    $actions['todo']['detailupdate']['Url'] = "$path/todo/todo_index.php?action=detailupdate&amp;todo_id=$id";
    $actions['todo']['detailupdate']['Condition'][] = 'insert';
    
    // Check Delete
    $actions['todo']['delete_unique']['Url'] = "$path/todo/todo_index.php?action=delete_unique&amp;todo_id=$id";
    $actions['todo']['delete_unique']['Condition'][] = 'insert';
  }
}


?>
