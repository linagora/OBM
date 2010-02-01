<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : tools_index.php                                              //
//     - Desc : OBM Tools Index File                                         //
// 2002-10-30 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - update_index  --         -- show the Update screen
// - update_update --         -- run the config update
// - update_detail --         -- display the updates
// - halt_halt     --         -- Halt the system
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'tools';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include_once("$obminclude/global.inc");
$params = get_global_params('Tools');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_tools_params();
include_once("$obminclude/global_pref.inc");
require_once('tools_display.inc');
require_once('tools_query.inc');

if ($action == 'index') $action = 'update_detail';
if ($action == 'update_index') $action = 'update_detail';
get_tools_action();
$perm->check_permissions($module, $action);
$extra_css[] = $css_tools;

$entities = array(
  'user' => array(
    'table'   => 'UserObm', 
    'link'    => array(
      'table' => 'EntityRight', 
      'join' => array (
        'table' => 'MailboxEntity',
        'id' => 'mailboxentity_entity_id',
        'joinId' => 'entityright_entity_id'
      ),      
      'id' => 'mailboxentity_mailbox_id'
    ),
    'prefix'  => 'userobm',
    'exclude' => array('domain_id' => 1, 'timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1, 
                       'local' => 1, 'timelastaccess' => 1, 'nb_login_failed' => 1, 'delegation_target' => 1, 
                       'calendar_version' => 1, 'vacation_datebegin' => 1,
                       'vacation_dateend' => 1),
    'rules'   => array("status" => "VALID"),
    'display' => array('firstname', 'lastname', 'email'),
    'display_format' => '%s %s <%s>'
  ),
  'group' => array(
    'table'   => 'UGroup',
    'link'    => array(
      'table' => 'of_usergroup', 
      'id' => 'of_usergroup_group_id'
    ),
    'prefix'  => 'group', 
    'exclude' => array('domain_id' => 1, 'timecreate' => 1, 'usercreate' => 1, 'timeupdate' => 1, 
                        'privacy' => 1,'usercreate' => 1),
    'rules'   => array('privacy' => '0'),
    'display' => array('name', 'email'),
    'display_format' => '%s <%s>'
  ),
  'host' => array(
    'table'   => 'Host',
    'link'    => array(
      'table' => 'Service',
      'join' => array (
        'table' => 'HostEntity',
        'id' => 'hostentity_entity_id',
        'joinId' => 'service_entity_id'
      ),
      'id' => 'hostentity_host_id',
      'rules' => array('service_service' => array('smtp_in', 'smtp_out', 'imap', 'samba'))
    ),
    'prefix'  => 'host',
    'exclude' => array('timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1),
    'rules'   => array(),
    'display' => array('name', 'ip'),
    'display_format' => '%s (%s)'
  ),
  'mailshare' => array(
    'table'   => 'MailShare',
    'link'    => array(
      'table' => 'EntityRight', 
      'join' => array (
        'table' => 'MailshareEntity',
        'id' => 'mailshareentity_entity_id',
        'joinId' => 'entityright_entity_id'
      ),      
      'id' => 'mailshareentity_mailshare_id'      
    ),    
    'prefix'  => 'mailshare',
    'exclude' => array('timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1),
    'rules'   => array(),
    'display' => array('name', 'email'),
    'display_format' => '%s <%s>'
  )
);

if ($action == 'cancel_update') {
///////////////////////////////////////////////////////////////////////////////
  if(!exec_tools_cancel_update($params)) {
    echo "({error:1,message:'".phpStringToJsString($GLOBALS['l_cant_undo_'.$params['state'].'_'.$params['entity']])."'})";
  } else {
    echo "({error:0,message:'".phpStringToJsString($GLOBALS['l_undo_success'])."'})";
  }
  die();
  
} elseif ($action == 'update_detail') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_tools_update_detail();

} elseif ($action == 'update_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_tools_update_context_ok($params)) {
    set_update_lock();
    set_update_state($params['domain_id']);
    store_update_data($params);
    run_query_tools_init_progress();
    $res = exec_tools_update_update($params);
    if ($res === 0) {
      $display['msg'] .= display_ok_msg($l_upd_running);
      $display['detail'] = dis_tools_update_detail();
      remove_update_lock();
    } else {
      $display['msg'] .= display_err_msg("$l_upd_error ($res)");
      remove_update_lock();
      $display['detail'] = dis_tools_update_detail();
    }
  } else {
    // Si le contexte ne permet pas une modification de configuration
    $display['msg'] .= display_warn_msg($err['msg']);
    $display['detail'] = dis_tools_update_detail();
  }

} elseif ($action == 'halt_halt') {
///////////////////////////////////////////////////////////////////////////////
  $display['msg'] .= display_debug_msg($cmd_halt, $cdg_exe);
  $ret = exec($cmd_halt);

} elseif($action == 'progress') {
///////////////////////////////////////////////////////////////////////////////
  json_tools_update_progress ($params['domain_id'], $params['realm']);
  echo "(".$display['json'].")";
  exit();
}
///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_tools);
$display['header'] = display_menu($module);
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Tools parameters transmited in $tools hash
// returns : $tools hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_tools_params() {
  global $obm;

  // Get global params
  $params = get_global_params('Tools');
  if ($obm['domain_global'] == true) {
    $params['domain_id'] = (isset($params['domain_id']) ? $params['domain_id'] : $obm['domain_id']);
  } else {
    $params['domain_id'] = $obm['domain_id'];
  }
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Tools Action 
///////////////////////////////////////////////////////////////////////////////
function get_tools_action() {
  global $params, $actions, $path;
  global $l_header_tools_upd, $l_header_tools_remote;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;


// Tool Update
  $actions['tools']['update_detail'] = array (
    'Name'     => $l_header_tools_upd,
    'Right'    => $cright_read_admin,
    'Url'      => "$path/tools/tools_index.php?action=update_detail",
    'Condition'=> array ('all') 
                                    );

// Confirm Update
  $actions['tools']['update_update'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );
  
// Cancel Update
  $actions['tools']['cancel_update'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

// Tool Remote
  $actions['tools']['remote_update'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

// Tool Halt
  $actions['tools']['halt_halt'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

// Tool Halt
  $actions['tools']['halt_halt'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

// Tool Halt
  $actions['tools']['progress'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );

}

</script>
