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
// OBM - File : domain_index.php                                             //
//     - Desc : Domain Index File                                            //
// 2006-17-05 Phan David                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the domain search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new domain form
// - detailconsult   -- $domain_id    -- show the domain detail
// - detailupdate    -- $domain_id    -- show the domain detail form
// - insert          -- form fields    -- insert the domain
// - update          -- form fields    -- update the domain
// - check_delete    -- $domain_id    -- check links before delete
// - delete          -- $domain_id    -- delete the user
// External API ---------------------------------------------------------------
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'domain';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_domain_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('domain_display.inc');
require('domain_query.inc');
require('domain_js.inc');
require_once("$obminclude/of/of_right.inc");

if ($action == '') $action = 'index';
get_domain_action();
$perm->check_permissions($module, $action);

update_last_visit('domain', $params['domain_id'], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_domain_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_domain_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['search'] = html_domain_search_form($params);
  $display['result'] = dis_domain_search_list($params);

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $prop_q = run_query_domain_properties();
  $display['detail'] = html_domain_form('','','', null, null, null, null, $prop_q,$params);

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_domain_consult($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_domain_detail($params['domain_id']);
  $prop_q = run_query_domain_properties($params['domain_id']);
  $ms_q = of_domain_get_domain_mailserver('', $params['domain_id']);
  $sync_q = of_domain_get_domain_syncserver($params['domain_id']);
  $solr_q = of_domain_get_domain_solrserver($params['domain_id']);
  $opush_q = of_domain_get_domain_opushserver($params['domain_id']);
  $opushfrontend_q = of_domain_get_domain_opushfrontendserver($params['domain_id']);
  $ldap_q = of_domain_get_domain_ldapserver($params['domain_id']);
  $imapfrontend_q = of_domain_get_domain_imapfrontendserver($params['domain_id']);
  $imap_archive_q = of_domain_get_domain_imaparchiveserver($params['domain_id']);
  $backupftp_q = of_domain_get_domain_backupftpserver($params['domain_id']);
  $provisioning_q = of_domain_get_domain_provisioningserver($params['domain_id']);
  $samba = run_query_domain_samba_properties($params['domain_id']);
  if ($obm_q->num_rows() == 1) {
    $display['detailInfo'] = display_record_info($obm_q);
    $display['detail'] = html_domain_form($obm_q, $ms_q, $sync_q, $solr_q, $imapfrontend_q, $backupftp_q, $samba, $prop_q, $params, $opush_q, $opushfrontend_q, $ldap_q, $imap_archive_q, $provisioning_q);
  } else {
    $display['msg'] .= display_err_msg($l_query_error . ' - ' . $query . ' !');
  }

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_domain_data_form('', $params)) {
    $cid = run_query_domain_insert($params);
    if ($cid > 0) {
      set_update_state();
      $params['domain_id'] = $cid;
      if (function_exists('specific_insert_domain')) {
        specific_insert_domain($cid);
      }
      $display['msg'] .= display_ok_msg($l_insert_ok);
      $display['detail'] = dis_domain_consult($params);
    } else {
      $display['msg'] .= display_err_msg($l_insert_error);
      $prop_q = run_query_domain_properties();
      $display['detail'] = html_domain_form('','','', null, null, null, null, $prop_q,$params);
    }
    // Form data are not valid
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $prop_q = run_query_domain_properties();
    $display['detail'] = html_domain_form('','','', null, null, null, null, $prop_q,$params);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_domain_data_form($params['domain_id'], $params) && check_domain_can_delete_mailserver($params)) {
    $retour = run_query_domain_update($params['domain_id'], $params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg($l_update_ok);
    } else {
      $display['msg'] .= display_err_msg($l_update_error);
    }
    $display['detail'] = dis_domain_consult($params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $prop_q = run_query_domain_properties($params['domain_id']);
    $display['detail'] = html_domain_form('','','', null, null, null, null, $prop_q, $params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_domain_can_delete($params['domain_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $warn_delete = false;
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $warn_delete = true;
    //$display['msg'] .= display_warn_msg($l_cant_delete, false);
    //$display['detail'] = dis_domain_consult($params);
  }
  $display['detail'] = dis_domain_can_delete($params['domain_id'],$warn_delete);

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_domain_delete($params['domain_id']);
  if ($retour == 0) {
    $display['msg'] .= display_ok_msg($l_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_delete_error);
  }
  $display['search'] = html_domain_search_form($params);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_domain, $module, $params);
if (! $params['popup']) {
  update_domain_action();
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores domain parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_domain_params() {
  
  // Get global params
  $params = get_global_params();

  if (isset ($params['ext_id'])) $params['domain_id'] = trim($params['ext_id']);

  $nb_m = 0;
  foreach ($params as $key => $value) {
    if (strcmp(substr($key, 0, 7),'data-m-') == 0) {
      $nb_m++;
      $m_num = substr($key, 7);
      $params["mailserver$nb_m"] = $m_num;
    }
  }
  $params['mailserver_nb'] = $nb_m;
  if(is_array($params['imap'])) {
    $params['imap'] = array_unique($params['imap']);
    $array = array();
    foreach($params['imap'] as $imap) {
      $imap = trim($imap);
      if(!empty($imap)) {
        $array[] = $imap;
      }
    }
    $params['imap'] = $array;
  }
  if(is_array($params['smtp_out'])) {
    $params['smtp_out'] = array_unique($params['smtp_out']);
    $array = array();
    foreach($params['smtp_out'] as $smtp_out) {
      $smtp_out = trim($smtp_out);
      if(!empty($smtp_out)) {
        $array[] = $smtp_out;
      }
    }
    $params['smtp_out'] = $array;
  }
  if(is_array($params['smtp_in'])) {
    $params['smtp_in'] = array_unique($params['smtp_in']);
    $array = array();
    foreach($params['smtp_in'] as $smtp_in) {
      $smtp_in = trim($smtp_in);
      if(!empty($smtp_in)) {
        $array[] = $smtp_in;
      }
    }
    $params['smtp_in'] = $array;
  }
  if(is_array($params['alias'])) {
    $aliases = array();
    while(!empty($params['alias'])) {
      $alias= trim(array_shift($params['alias']));
      if(!empty($alias)) {
        $aliases[] = $alias;
      }
    }
    $params['alias'] = implode("\r\n",$aliases);
  }
  if(isset($params['samba'])) {
    foreach($params['samba'] as $key => $value) $params['samba'][$key] = stripslashes($value);
  } 
  if (!empty($params['mail_server_id']) && $params['mail_server_id'][0] == 'a') {
    $params['mail_server_auto'] = str_replace('a', '', $params['mail_server_id']) + 0;
    unset($params['mail_server_id']);
  }
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// domain Action 
///////////////////////////////////////////////////////////////////////////////
function get_domain_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display, $l_header_reset;
  global $l_header_add_mailserver, $l_domain;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params['domain_id'];

// Index
  $actions['domain']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/domain/domain_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

  // New
  $actions['domain']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/domain/domain_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('search','index','insert','update','delete','admin','detailconsult','display')
                                  );

// Search
  $actions['domain']['search'] = array (
    'Url'      => "$path/domain/domain_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
  
// Detail Consult
  $actions['domain']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=$id",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                  );

// Detail Update
  $actions['domain']['detailupdate'] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/domain/domain_index.php?action=detailupdate&amp;domain_id=$id",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'update')
                                     	   );
// Insert
  $actions['domain']['insert'] = array (
    'Url'      => "$path/domain/domain_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions['domain']['update'] = array (
    'Url'      => "$path/domain/domain_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions['domain']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/domain/domain_index.php?action=check_delete&amp;domain_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	   );

// Delete
  $actions['domain']['delete'] = array (
    'Url'      => "$path/domain/domain_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     );

}


///////////////////////////////////////////////////////////////////////////////
// domain Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_domain_action() {
  global $params, $actions, $path;
  global $l_domain;

  $id = $params['domain_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['domain']['detailconsult']['Url'] = "$path/domain/domain_index.php?action=detailconsult&amp;domain_id=$id";
    $actions['domain']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['domain']['detailupdate']['Url'] = "$path/domain/domain_index.php?action=detailupdate&amp;domain_id=$id";
    $actions['domain']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['domain']['check_delete']['Url'] = "$path/domain/domain_index.php?action=check_delete&amp;domain_id=$id";
    $actions['domain']['check_delete']['Condition'][] = 'insert';
 }
}


?>
