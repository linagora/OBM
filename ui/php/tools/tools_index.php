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
    'links'    => array(
      'rights' => array(
        'table' => 'EntityRight', 
        'join'  => array (
          'table'  => 'MailboxEntity',
          'id'     => 'mailboxentity_entity_id',
          'joinId' => 'entityright_entity_id'
        ),      
        'id' => 'mailboxentity_mailbox_id'
      )
    ),
    'prefix'  => 'userobm',
    'exclude' => array('domain_id' => 1, 'timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1, 
                       'local' => 1, 'timelastaccess' => 1, 'nb_login_failed' => 1, 'delegation_target' => 1, 
                       'calendar_version' => 1, 'vacation_datebegin' => 1,
                       'vacation_dateend' => 1),
    'properties' => array (
      'categories' => array (
        'table' => 'CategoryLink',
        'join'  => array (
          'table'  => 'UserEntity',
          'id'     => 'userentity_entity_id',
          'joinId' => 'categorylink_entity_id'
        ),
        'id' => 'userentity_user_id'
      ),
      'fields' => array (
        'table' => 'field',
        'join'  => array (
          'table'  => 'UserEntity',
          'id'     => 'userentity_entity_id',
          'joinId' => 'entity_id'
        ),
        'id' => 'userentity_user_id'
      ),
    ),
    'rules'   => array("status" => "VALID"),
    'display' => array('firstname', 'lastname', 'email'),
    'display_format' => '%s %s <%s>'
  ),
  'group' => array(
    'table'   => 'UGroup',
    'links'    => array(
      'user' =>array(
        'table' => 'of_usergroup', 
        'id' => 'of_usergroup_group_id'
      ),
      'contact' =>array(
        'table' => '_contactgroup', 
        'id' => 'group_id'
      )      
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
    'links'    => array(
      'service' => array (
        'table' => 'Service',
        'join' => array (
          'table' => 'HostEntity',
          'id' => 'hostentity_entity_id',
          'joinId' => 'service_entity_id'
        ),
        'id' => 'hostentity_host_id',
        'rules' => array('service_service' => array('smtp_in', 'smtp_out', 'imap', 'samba'))
      ),
    ),
    'prefix'  => 'host',
    'exclude' => array('timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1),
    'rules'   => array(),
    'display' => array('name', 'ip'),
    'display_format' => '%s (%s)'
  ),
  'mailshare' => array(
    'table'   => 'MailShare',
    'links'    => array(
      'rights' => array(
        'table' => 'EntityRight', 
        'join' => array (
          'table' => 'MailshareEntity',
          'id' => 'mailshareentity_entity_id',
          'joinId' => 'entityright_entity_id'
        ),      
        'id' => 'mailshareentity_mailshare_id'      
      )
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

?>
