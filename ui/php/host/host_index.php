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
// OBM - File : host_index.php                                               //
//     - Desc : Host Index File                                              //
// 2004-09-09 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the host search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new host form
// - detailconsult   -- host_id        -- show the host detail
// - detailupdate    -- host_id        -- show the host detail form
// - insert          -- form fields    -- insert the host
// - update          -- form fields    -- update the host
// - check_delete    -- $host_id       -- check the host
// - delete          -- $host_id       -- delete the host
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'host';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_host_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
include('host_display.inc');
include('host_query.inc');
require('host_js.inc');  

$extra_css[] = $css_host;
$extra_js_include[] = 'host.js';

if ($action == '') $action = 'index';
get_host_action();
$perm->check_permissions($module, $action);
update_last_visit('host', $params['host_id'], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_id') {
  $display['search'] = html_host_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_host_search_list($params);
  } else {
    $display['msg'] = display_info_msg($l_no_display);
  }

} else if (($action == 'index') || ($action == '')) {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_host_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_host_search_list('');
  } else {
    $display['msg'] .= display_ok_msg($l_no_display);
  }

} else if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_host_search_form($params);
  $display['result'] = dis_host_search_list($params);

} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_host_form($action, '', $params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_host_consult($params);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_host_update_rights($params)) {
    $obm_q = run_query_host_detail($params['host_id']);
    $display['detail'] = html_host_form($action, $obm_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_host_data_form($params)) {
    
    // If the context (same host) was confirmed ok, we proceed
    if ($params['confirm'] == $c_yes) {
      $retour = run_query_host_insert($params);
      if ($retour) {
	set_update_state();
	$display['msg'] .= display_ok_msg($l_insert_ok);
      } else {
	$display['msg'] .= display_err_msg($l_insert_error);
      }
      $display['search'] = html_host_search_form($params);

      // If it is the first try, we warn the user if some hosts seem similar
    } else {
      $obm_q = check_host_context('', $params);
      if ($obm_q->num_rows() > 0) {
	$display['detail'] = dis_host_warn_insert('', $obm_q, $params);
      } else {
	$retour = run_query_host_insert($params);
	if ($retour) {
	  set_update_state();
	  $display['msg'] .= display_ok_msg($l_insert_ok);
	} else {
	  $display['msg'] .= display_err_msg($l_insert_error);
	}
	$display['search'] = html_host_search_form($params);
      }
    }

    // Form data are not valid
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'].= html_host_form($action, '', $params, $err['field']);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_host_data_form($params)) {
    $retour = run_query_host_update($params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg($l_update_ok);
    } else {
      $display['msg'] .= display_err_msg($l_update_error);
    }
    $display['detail'] = dis_host_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $host_q = run_query_host_detail($params['host_id']);
    $display['detail'] = html_host_form($action, $host_q, $params, $err['field']);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if(check_host_can_delete($params['host_id'])) {
    $display['detail'] = dis_host_warn_delete($params['host_id']);
  } else {
    $display['msg'] .= display_err_msg($err["msg"], false);
    $display['detail'] = dis_host_consult($params);
  }
} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_host_delete($params['host_id']);
  if ($retour) {
    set_update_state();
    $display['msg'] .= display_ok_msg($l_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_delete_error);
  }
  $display['search'] = html_host_search_form('');

} else if ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'host', 1);
  $display['detail'] = dis_host_display_pref($prefs);

} else if($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'host', 1);
  $display['detail'] = dis_host_display_pref($prefs);

} else if($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'host', 1);
  $display['detail'] = dis_host_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
update_host_action();
$display['head'] = display_head($l_host);
$display['end'] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Host parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_host_params() {
  // Get global params
  $params = get_global_params('Host');

  // Group fields
  if (isset ($params['id'])) $params['host_id'] = $params['id'];
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Clean up a string coming from a textarea : 
//  + remove empty lines
//  + remove space at the beginning of the lines
//  + remove space at the end of the lines
//  + remove first empty line and last \n of the string
// Parameters:
//   - $str String to be cleaned up
// Returns:
//   - The string cleaned up  
///////////////////////////////////////////////////////////////////////////////
function cleanup($str) {
  $result = '';
  $lines = explode('\r\n', $str);
  $i = 0;
  while ($i < count($lines)) {
    $currentline = trim($lines[$i]);
    if ($currentline != '') {
      $result .= $currentline.'\r\n';
    }
    $i++;
  }

  // Suppression du dernier retour chariot
  // (plus pratique pour les traitements type explode/tokenizer, on obtient pas de derniere chaine vide)
  $result = preg_replace('/\r\n$/', '', $result);
  return $result;
}


///////////////////////////////////////////////////////////////////////////////
// Host Action 
///////////////////////////////////////////////////////////////////////////////
function get_host_action() {
  global $params, $actions, $path, $l_host;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['host']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/host/host_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions['host']['search'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );

// New
  $actions['host']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/host/host_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','admin', 'showlist','detailconsult','display', 'insert', 'update')
                                  );

// Detail Consult
  $actions['host']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/host/host_index.php?action=detailconsult&amp;host_id=".$params['host_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate','showlist') 
                                  );

// Detail Update
  $actions['host']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/host/host_index.php?action=detailupdate&amp;host_id=".$params['host_id'],
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'showlist', 'update')
                                     	   );

// Show List
  $actions['host']['showlist'] = array (
     'Url'      => "$path/host/host_index.php?action=showlist&amp;host_id=".$obm_user['host_id'],
     'Right'    => $cright_read,
     'Condition'=> array ('None')
                                     	   );

// Insert
  $actions['host']['insert'] = array (
    'Url'      => "$path/host/host_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// Update
  $actions['host']['update'] = array (
    'Url'      => "$path/host/host_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// Check Delete
  $actions['host']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/host/host_index.php?action=check_delete&amp;host_id=".$params['host_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'showlist', 'showlist', 'detailupdate')
                                     	   );

// Delete
  $actions['host']['delete'] = array (
    'Url'      => "$path/host/host_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Dispay
  $actions['host']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/host/host_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );
// Dispay Prefs
  $actions['host']['dispref_display'] = array (
    'Url'      => "$path/host/host_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
// Display
  $actions['host']['dispref_level'] = array (
    'Url'      => "$path/host/host_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

// Dispay
  $actions['host']['ext_get_id'] = array (
    'Url'      => "$path/host/host_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
}

///////////////////////////////////////////////////////////////////////////////
// MailShare Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_host_action() {
  global $params, $actions, $path;

  $id = $params['host_id'];
  if ($id > 0) {
    $h = get_host_info($id);
    // Detail Consult
    $actions['host']['detailconsult']['Url'] = "$path/host/host_index.php?action=detailconsult&amp;host_id=$id";
    if (check_host_update_rights($params, $h)) { 
      // Detail Update
      $actions['host']['detailupdate']['Url'] = "$path/host/host_index.php?action=detailupdate&amp;host_id=$id";
      $actions['host']['detailupdate']['Condition'][] = 'insert';
      
      // Check Delete
      $actions['host']['check_delete']['Url'] = "$path/host/host_index.php?action=check_delete&amp;host_id=$id";
      $actions['host']['check_delete']['Condition'][] = 'insert';
    } else {
      // Detail Update
      $actions['host']['detailupdate']['Condition'] = array('None');
      // Check Delete
      $actions['host']['check_delete']['Condition'] = array('None');
      // Rights admin
      $actions['host']['rights_admin']['Condition'] = array('None');
    }
  }
}
?>
