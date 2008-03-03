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
// - halt_index    --         -- show the shutdown tool
// - halt_halt     --         -- Halt the system
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'tools';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_tools_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_tools_params();
include("$obminclude/global_pref.inc");
require('tools_display.inc');
require('tools_query.inc');

if ($action == 'index') $action = 'update_detail';
if ($action == 'update_index') $action = 'update_detail';
get_tools_action();
$perm->check_permissions($module, $action);

$entities = array(
  'user' => array(
    'table'   => 'UserObm', 
    'link'    => array(
      'table' => 'EntityRight', 
      'id' => 'entityright_entity_id', 
      'rules' => array('entityright_entity' => 'mailbox')
    ),
    'prefix'  => 'userobm',
    'exclude' => array('domain_id' => 1, 'timeupdate' => 1, 'timecreate' => 1, 'usercreate' => 1, 'userupdate' => 1, 
                       'local' => 1, 'timelastaccess' => 1, 'nb_login_failed' => 1, 'delegation_target' => 1, 
                       'calendar_version' => 1, 'nomade_datebegin' => 1, 'nomade_dateend' => 1, 'vacation_datebegin' => 1,
                       'vacation_dateend' => 1),
    'rules'   => array()
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
    'rules'   => array('privacy' => '0')
  ),
  'host' => array(
    'table'   => 'Host',
    'prefix'  => 'host',
    'exclude' => array(),
    'rules'   => array()
  ),
  'mailshare' => array(
    'table'   => 'MailShare',
    'link'    => array(
      'table' => 'EntityRight', 
      'id' => 'entityright_entity_id', 
      'rules' => array('entityright_entity' => 'MailShare')
    ),    
    'prefix'  => 'mailshare',
    'exclude' => array(),
    'rules'   => array()
  )
);

if ($action == 'update_detail') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_tools_update_detail();

} elseif ($action == 'update_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_tools_update_context_ok($params)) {
    set_update_lock();
    set_update_state($params['domain_id']);
    store_update_data($params);
     
    $res = exec_tools_update_update($params);
    if ($res == '0') {
      $display['msg'] .= display_ok_msg($l_upd_running);
    } else {
      $display['msg'] .= display_err_msg("$l_upd_error ($res)");
    }
    $display['detail'] = dis_tools_update_detail();
    remove_update_lock();
  } else {
    // Si le contexte ne permet pas une modification de configuration
    $display['msg'] .= display_warn_msg($err['msg']);
    $display['detail'] = dis_tools_update_detail();
  }

} elseif ($action == 'halt_index') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_tools_halt_index();

} elseif ($action == 'halt_halt') {
///////////////////////////////////////////////////////////////////////////////
  $display['msg'] .= display_debug_msg($cmd_halt, $cdg_exe);
  $ret = exec($cmd_halt);
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

  if ($obm['domain_id'] == '0') {
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
  global $l_header_tools_upd, $l_header_tools_halt,$l_header_tools_remote;
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

// Tool Remote
  $actions['tools']['remote_update'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );
// Tool Halt
  $actions['tools']['halt_index'] = array (
    'Name'     => $l_header_tools_halt,
    'Url'      => "$path/tools/tools_index.php?action=halt_index",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all') 
                                    );

// Tool Halt
  $actions['tools']['halt_halt'] = array (
    'Right'    => $cright_write_admin,
    'Condition'=> array ('none') 
                                    );


}

</script>
