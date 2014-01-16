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
// OBM - File : publication_index.php                                        //
//     - Desc : Company Index File                                           //
// 2004-01-28 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the publication search form
// - search             -- search fields  -- show the result set of search
// - new                --                -- show the new publication form
// - detailconsult      -- $param_publication -- show publication detail
// - detailupdate       -- $param_publication -- show publication detail form
// - insert             -- form fields    -- insert the publication
// - update             -- form fields    -- update the publication
// - check_delete       -- $param_publication -- check links before delete
// - delete             -- $param_publication -- delete the publication
// - admin              --                -- admin index (type)
// - type_insert        -- form fields    -- insert the type
// - type_update        -- form fields    -- update the type
// - type_checklink     --                -- check if type is used
// - type_delete        -- $sel_type      -- delete the type
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_id         -- $title         -- select a publication (return id) 
///////////////////////////////////////////////////////////////////////////////

// Contact category used to select auto contact subscription
$public_contact_cat = 'contactcategory1';

//-----------------------------------------------------------------------------
$path = '..';
$module = 'publication';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_publication_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('publication_query.inc');
require('publication_display.inc');
require_once("$obminclude/of/of_category.inc");

update_last_visit('publication', $params['publication_id'], $action);

require('publication_js.inc');
get_publication_action();
$perm->check_permissions($module, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ext_get_id') {
  $type_q = run_query_publication_type();
  $display['search'] = html_publication_search_form($type_q, $params);
  $display['result'] = dis_publication_search_list($params);  

///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////
  $type_q = run_query_publication_type();
  $display['search'] = html_publication_search_form($type_q, $params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_publication_search_list($params);
  } else {
    $display['msg'] = display_info_msg($l_no_display);
  }

} elseif ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $type_q = run_query_publication_type();
  $display['search'] = html_publication_search_form($type_q, $params);
  $display['result'] = dis_publication_search_list($params);

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_publication_form($action, $params);

} elseif ($action == 'new_subscription') {
///////////////////////////////////////////////////////////////////////////////
  $recept_q = run_query_publication_subscription_reception();
  $params['lang'] = run_query_publication_get_contact_lang($params['contact_id']);
  $display['detail'] = html_publication_subscription_form($action,$sub_q, $recept_q, $params);

} elseif ($action == 'new_auto') {
///////////////////////////////////////////////////////////////////////////////
  $recept_q = run_query_publication_subscription_reception();
  $pub_q = run_query_publication_detail($params['publication_id']);
  if ($pub_q->nf() == 1) {
    $display['detail'] = html_publication_auto_subscription_form($action,$pub_q, $recept_q, $params);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_publication_consult($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_publication_form($action, $params);

} elseif ($action == 'detailupdate_subscription') {
///////////////////////////////////////////////////////////////////////////////
  $recept_q = run_query_publication_subscription_reception();
  $params['lang'] = run_query_publication_get_contact_lang($params['contact_id']);
  $sub_q = run_query_publication_subscription_detail($params);
  $display['detail'] = html_publication_subscription_form($action,$sub_q, $recept_q, $params);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  require("$path/list/list_query.inc");
  if (check_publication_data('', $params)) {

    // If the context (same publications) was confirmed ok, we proceed
    if ($params['confirm'] == $c_yes) {
      $params['publication_id'] = run_query_publication_insert($params);
      if ($params['publication_id']) {
        $display['msg'] .= display_ok_msg("$l_publication : $l_insert_ok");
      } else {
        $display['msg'] .= display_err_msg("$l_publication : $l_insert_error");
      }
      $display['detail'] = dis_publication_consult($params);
    // If it is the first try, we warn the user if some publications seem similar
    } else {
      $obm_q = check_publication_context('', $params);
      if ($obm_q->num_rows() > 0) {
        $display['detail'] = dis_publication_warn_insert('', $obm_q, $params);
      } else {
        $params['publication_id'] = run_query_publication_insert($params);
        if ($params['publication_id']) {
          $display['msg'] .= display_ok_msg("$l_publication : $l_insert_ok");
        } else {
          $display['msg'] .= display_err_msg("$l_publication : $l_insert_error");
        }
	$display['detail'] = dis_publication_consult($params);
      }
    }

  // Form data are not valid
  } else {
    $display['msg'] = display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_publication_form($action, $params);
  }

} elseif ($action == 'insert_subscription') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_publication_data_subscription_form('', $params)) {
    $retour = run_query_publication_insert_subscription($params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_subscription : $l_insert_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_subscription : $l_insert_error");
    }
    $recept_q = run_query_publication_subscription_reception();
    $params['lang'] = run_query_publication_get_contact_lang($params['contact_id']);
    $display['detail'] = html_publication_subscription_form($action,$sub_q, $recept_q, $params);
    $display['detail'] .= "
    <br />
    <a href=\"javascript: void(0);\" onclick=\"window.opener.location.href='".$params['ext_url']."';window.close();\" >
    $l_close
    </a>";
  // Form data are not valid
  } else {
    $display['msg'] = display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $recept_q = run_query_publication_subscription_reception();
    $params['lang'] = run_query_publication_get_contact_lang($params['contact_id']);
    $display['detail'] = html_publication_subscription_form($action,$cont_q, $recept_q, $params);
  }

} elseif ($action == 'new_group_subscription') {
///////////////////////////////////////////////////////////////////////////////
  $pub_q = run_query_publication_detail($params['publication_id']);
  $display['detail'] = html_publication_group_subscription_form($action,$pub_q,$params);

} elseif ($action == 'insert_group_subscription') {
///////////////////////////////////////////////////////////////////////////////
  require("$path/list/list_query.inc");
  // If the context (same publications) was confirmed ok, we proceed
  $pub_q = run_query_publication_detail($params['publication_id']);
  $params['lang'] = $pub_q->f('publication_lang');
  if ( (is_array($params[$public_contact_cat])
	&& count($params[$public_contact_cat])>0)
       || (is_array($params['list']) && (count($params['list'])>0)) ) {
    $nb = run_query_publication_auto_subscription($params);
  }
  if ($nb !== false) {
    $display['msg'] .= display_ok_msg("$l_subscription : $l_insert_ok ($nb)");
  } else {
    $display['msg'] .= display_err_msg("$l_subscription : $l_insert_error");
  }
  $display['detail'] = dis_publication_consult($params);

} elseif ($action == 'insert_auto') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_auto_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_subscription : $l_insert_ok");
    $display['detail'] = dis_publication_consult($params);
  } else {
    $display['msg'] .= display_err_msg("$l_subscription : $l_insert_error");
    $recept_q = run_query_publication_subscription_reception();
    $pub_q = run_query_publication_detail($params['publication_id']);
     echo '1;';
    if ($pub_q->nf() == 1) {
      $display['detail'] = html_publication_auto_subscription_form($action,$pub_q, $recept_q, $params);
    }    
  } 
  
} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_publication_data($params['publication_id'], $params)) {
    $retour = run_query_publication_update($params['publication_id'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_publication : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_publication : $l_update_error");
    }
    $display['detail'] = dis_publication_consult($params);
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_publication_form($action, $params);
  }

} elseif ($action == 'update_subscription') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_publication_data_subscription_form('', $params)) {
    $retour = run_query_publication_update_subscription($params);
    if ($retour) {
      $quit = "
<script type=\"text/javascript\">
   window.opener.location.href='".$params['ext_url']."';
   window.close();
</script>
";
      $display['msg'] .= display_ok_msg("$l_subscription : $l_update_ok", false);
      $display['detail'] = $quit;
    } else {
      $display['msg'] .= display_err_msg("$l_subscription : $l_update_error");
    }
  // Form data are not valid
  } else {
    $display['msg'] = display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $recept_q = run_query_publication_subscription_reception();
    $params['lang'] = run_query_publication_get_contact_lang($params['contact_id']);
    $display['detail'] = html_publication_subscription_form($action,$cont_q, $recept_q, $params);
  }

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_publication_can_delete($params['publication_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_publication_can_delete($params['publication_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = $block;
    $display['detail'] .= dis_publication_consult($params);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_publication_can_delete($params['publication_id'])) {
    $retour = run_query_publication_delete($params['publication_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_publication : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_publication : $l_delete_error");
    }
    $type_q = run_query_publication_type();
    $display['search'] = html_publication_search_form($type_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_publication_consult($params);
  }

} elseif ($action == 'delete_subscription') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_delete_subscription($params);
  $display['detail'] = "
  <br />
  <a href=\"javascript: void(0);\" onclick=\"window.opener.location.reload();window.close();\" >
  $l_close
  </a>";
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_subscription : $l_delete_ok$quit");
  } else {
    $display['msg'] .= display_err_msg("$l_subscription : $l_delete_error $quit");
  }

} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_publication_admin_index();

} elseif ($action == 'type_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_type_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_type : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_type : $l_insert_error");
  }
  $display['detail'] .= dis_publication_admin_index();

} elseif ($action == 'type_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_type_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_type : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_type : $l_update_error");
  }
  $display['detail'] .= dis_publication_admin_index();

} elseif ($action == 'type_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_publication_type_links($params);

} elseif ($action == 'type_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_type_delete($params['type']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_type : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_type : $l_delete_error");
  }
  $display['detail'] .= dis_publication_admin_index();

} elseif ($action == 'recept_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_recept_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_recept : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_recept : $l_insert_error");
  }
  $display['detail'] .= dis_publication_admin_index();

} elseif ($action == 'recept_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_recept_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_recept : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_recept : $l_update_error");
  }
  $display['detail'] .= dis_publication_admin_index();

} elseif ($action == 'recept_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_publication_recept_links($params);

} elseif ($action == 'recept_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_publication_recept_delete($params['recept']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_recept : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_recept : $l_delete_error");
  }
  $display['detail'] .= dis_publication_admin_index();

}  elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'publication', 1);
  $display['detail'] = dis_publication_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'publication', 1);
  $display['detail'] = dis_publication_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'publication', 1);
  $display['detail'] = dis_publication_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_publication);
$display['end'] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $params['popup']) {
  update_publication_action();
  $display['header'] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_publication_params() {
  
  // Get global params
  $params = get_global_params('Publication');

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Company Actions 
///////////////////////////////////////////////////////////////////////////////
function get_publication_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_admin,$l_header_new_auto;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_subscription;

// Index
  $actions['publication']['index'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/publication/publication_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );
// ext_get_id
  $actions['publication']['ext_get_id'] = array (
    'Url'      => "$path/publication/publication_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                	 );
					 
// Search
  $actions['publication']['search'] = array (
    'Url'      => "$path/publication/publication_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions['publication']['new'] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/publication/publication_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                     );
// New Publication from an other one.
  $actions['publication']['new_auto'] = array (
    'Name'     => $l_header_new_auto,
    'Url'      => "$path/publication/publication_index.php?action=new_auto&amp;publication_id=".$params['publication_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','detailconsult', 'update','insert_auto')
                                     );
	     
// Detail Consult
  $actions['publication']['detailconsult']  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/publication/publication_index.php?action=detailconsult&amp;publication_id=".$params['publication_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','insert_auto','detailupdate') 
                                     		 );

// Detail Update
  $actions['publication']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/publication/publication_index.php?action=detailupdate&amp;publication_id=".$params['publication_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','detailconsult', 'update','insert_auto') 
                                     	      );

// Subscribe a group of contact to a publication.
  $actions['publication']['new_group_subscription'] = array (
    'Name'     => $l_subscription,
    'Url'      => "$path/publication/publication_index.php?action=new_group_subscription&amp;publication_id=".$params['publication_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','detailconsult', 'update','insert_auto')
                                     );		
// Subscribe a group of contact to a publication.
  $actions['publication']['insert_group_subscription'] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert_group_subscription",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );						     
// Subscription Update
  $actions['publication']['detailupdate_subscription'] = array (
    'Url'      => "$path/publication/publication_index.php?action=detailupdate_subscription",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	      );
// Insert
  $actions['publication']['insert'] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );
// Insert auto
  $actions['publication']['insert_auto'] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert_auto",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                         );

// Update
  $actions['publication']['update'] = array (
    'Url'      => "$path/publication/publication_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Check Delete
  $actions['publication']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/publication/publication_index.php?action=check_delete&amp;publication_id=".$params['publication_id'],
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions['publication']['delete'] = array (
    'Url'      => "$path/publication/publication_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Delete
  $actions['publication']['delete_subscription'] = array (
    'Url'      => "$path/publication/publication_index.php?action=delete_subscription",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Admin
  $actions['publication']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/publication/publication_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Kind Insert
  $actions['publication']['type_insert'] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions['publication']['type_update'] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions['publication']['type_checklink'] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions['publication']['type_delete'] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
// Reception Insert
  $actions['publication']['recept_insert'] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Reception Update
  $actions['publication']['recept_update'] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Reception Check Link
  $actions['publication']['recept_checklink'] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Reception Delete
  $actions['publication']['recept_delete'] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
// New Subscription
  $actions['publication']['new_subscription'] = array (
    'Url'      => "$path/publication/publication_index.php?action=new_subscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );
// Insert Subscription
  $actions['publication']['insert_subscription'] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert_subscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );
// Update Subscription
  $actions['publication']['update_subscription'] = array (
    'Url'      => "$path/publication/publication_index.php?action=update_subscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );
					       
// Display
  $actions['publication']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/publication/publication_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions['publication']['dispref_display'] = array (
    'Url'      => "$path/publication/publication_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions['publication']['dispref_level']  = array (
    'Url'      => "$path/publication/publication_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}


///////////////////////////////////////////////////////////////////////////////
// Company Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_publication_action() {
  global $params, $actions, $path;

  $id = $params['publication_id'];
  if ($id > 0) {
    // Detail Consult
    $actions['publication']['detailconsult']['Url'] = "$path/publication/publication_index.php?action=detailconsult&amp;publication_id=$id";
    $actions['publication']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['publication']['detailupdate']['Url'] = "$path/publication/publication_index.php?action=detailupdate&amp;publication_id=$id";
    $actions['publication']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['publication']['check_delete']['Url'] = "$path/publication/publication_index.php?action=check_delete&amp;publication_id=$id";
    $actions['publication']['check_delete']['Condition'][] = 'insert';
  }
}

?>
