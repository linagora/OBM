<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
// OBM - File : contact_index.php                                            //
//     - Desc : Contact Index File                                           //
// 1999-03-19 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    -- search fields  -- show the contact search form
// - search             -- search fields  -- show the result set of search
// - new                -- $company_id -- show the new contact form
// - detailconsult      -- $contact_id -- show the contact detail
// - detailupdate       -- $contact_id -- show the contact detail form
// - insert             -- form fields    -- insert the contact
// - update             -- form fields    -- update the contact
// - check_delete       -- $contact_id -- check links before delete
// - delete             -- $contact_id -- delete the contact
// - admin              --                -- admin index (kind)
// - statistics         --                -- statistics index 
// - function_insert    -- form fields    -- insert the function
// - function_update    -- form fields    -- update the function
// - function_checklink --                -- check if function is used
// - function_delete    -- $sel_func      -- delete the function
// - kind_insert        -- form fields    -- insert the kind
// - kind_update        -- form fields    -- update the kind
// - kind_checklink     --                -- check if kind is used
// - kind_delete     	-- $sel_kind      -- delete the kind
// - statistics     	--                -- display contact statistics
// - rights_admin       -- access rights screen
// - rights_update      -- Update contact access rights
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// - sync               -- $contact_id    -- synchronize a contact with the logged user
// - desync             -- $contact_id    -- desynchronize a contact with the logged user
// External API ---------------------------------------------------------------
// - ext_get_ids        --                -- select multiple contacts (ret id) 
// - ext_get_id         -- $title         -- select a contact (return id) 
///////////////////////////////////////////////////////////////////////////////

// XXXXXXX dis_contact_form ? pourquoi param co_q et pas fait a l'interieur a la company ??

$path = '..';
$module = 'contact';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_contact_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require("$obminclude/of/of_contact.php");
require('contact_display.inc');
require('contact_query.inc');
require_once('contact_js.inc');
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_error.php");
require_once('addressbook.php');
$extra_js_include[] = 'contact.js';
$extra_css[] = $css_contact;

get_contact_action();

$perm->check_permissions($module, $action);
//if (! check_privacy($module, 'Contact', $action, $params['contact_id'], $obm['uid'])) {
//  $display['msg'] = display_err_msg($l_error_visibility);
//  $action = 'index';
//} else {
update_last_visit('contact', $params['id'], $action);
//}
page_close();

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if (($action == 'ext_get_ids') || ($action == 'ext_get_id')) {
  if ($action == 'ext_get_ids') {
    $params['ext_type'] = 'multi';
  } else {
    $params['ext_type'] = 'mono';
  }
  $display['search'] = dis_contact_search_form($params);
  if ($_SESSION['set_display'] == 'yes') {
    $display['result'] = dis_contact_search_list($params);
  } else {
    $display['msg'] .= display_info_msg($l_no_display);
  }

} elseif ($action == 'vcard') {
///////////////////////////////////////////////////////////////////////////////
  dis_contact_vcard_export($params);
  exit();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

} elseif ($action == 'ext_search') {
///////////////////////////////////////////////////////////////////////////////
  $contacts = run_query_contact_ext_search($params);
  json_search_contact($params, $contacts);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'ext_search_mail') {
///////////////////////////////////////////////////////////////////////////////
  $contacts = run_query_contact_ext_search($params);
  json_search_contact($params, $contacts);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'ext_get_kind') {
///////////////////////////////////////////////////////////////////////////////
  $language = get_lang();
  $kinds = run_query_contact_get_kinds($language);
  json_get_kind($kinds);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'import') {
///////////////////////////////////////////////////////////////////////////////
  if($params['addressbook'] && OBM_AddressBook::get($params['addressbook'])->write == 1) {
    $display['detail'] = dis_vcard_import_form($params['addressbook']);
  } else {
    header('location: '.$GLOBALS['path'].'/contact/contact_index.php');
  }
} elseif ($action == 'save') {
///////////////////////////////////////////////////////////////////////////////
  $addressbooks = OBM_AddressBook::search();
  $contacts = $addressbooks->exportContacts($params['searchpattern']);
  dis_contact_vcard_export_all($contacts);
  exit();
} elseif ($action == 'vcard_insert') {
///////////////////////////////////////////////////////////////////////////////
  if (!empty($params[vcard_tmp])) {
    $addressbook = OBM_AddressBook::get($params['addressbook']);
    if($addressbook->write) {
      $ids = run_query_vcard_insert($params, $addressbook);
    } else {
      header('location: '.$GLOBALS['path'].'/contact/contact_index.php');
    }
    if ($ids !== false) {
      header('location: '.$GLOBALS['path'].'/contact/contact_index.php');
    } else {
      $display['msg'] .= display_err_msg("$l_contact : $l_insert_error");
      $display['detail'] .= dis_vcard_import_form($params['addressbook']);
    }
  } else {
    $display['msg'] .= display_err_msg("$l_contact : $l_insert_error");
    $display['detail'] .= dis_vcard_import_form($params['addressbook']);
  }

} elseif ($action == 'export') {
///////////////////////////////////////////////////////////////////////////////
  $addressbooks = OBM_AddressBook::search();
  $contacts = $addressbooks->exportContacts($params['searchpattern']."  -is:archive");
  dis_contact_csv_export_all($contacts);
  exit();
} elseif ($action == 'statistics') {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lang/".$_SESSION['set_lang'].'/statistic.inc');
  // Specific conf statistics lang file
  if ($conf_lang) {
    $lang_file = "$obminclude/conf/lang/".$_SESSION['set_lang']."/statistic.inc";
    if (file_exists("$path/../".$lang_file)) {
      include("$lang_file");
    }
  }
  $display['title'] = display_title($l_stats);
  $display['detail'] = dis_category_contact_stats($params);

} elseif ($action == 'rights_admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_addressbook_right_dis_admin($params);

} elseif ($action == 'rights_update') {
///////////////////////////////////////////////////////////////////////////////
  if (OBM_Acl_Utils::updateRights('addressbook', $params['entity_id'], $obm['uid'], $params)) {
    $display['msg'] .= display_ok_msg($l_right_update_ok);
  } else {
    $display['msg'] .= display_warn_msg($l_of_right_err_auth);
  }
  $display['detail'] = dis_addressbook_right_dis_admin($params);

} elseif ($action == 'admin') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_contact_admin_index();

} elseif ($action == 'function_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('contact', 'function', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_function : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_function : $l_insert_error");
  }
  $display['detail'] .= dis_contact_admin_index();

} elseif ($action == 'function_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('contact', 'function', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_function : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_function : $l_update_error");
  }
  $display['detail'] .= dis_contact_admin_index();

} elseif ($action == 'function_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links('contact', 'function', $params, 'mono');

} elseif ($action == 'function_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('contact', 'function', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_function : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_function : $l_delete_error");
  }
  $display['detail'] .= dis_contact_admin_index();

} elseif ($action == 'kind_insert') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_kind_insert($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_kind : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_kind : $l_insert_error");
  }
  $display['detail'] .= dis_contact_admin_index();

} elseif ($action == 'kind_update') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_kind_update($params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_kind : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_kind : $l_update_error");
  }
  $display['detail'] .= dis_contact_admin_index();

} elseif ($action == 'kind_checklink') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_contact_kind_links($params);

} elseif ($action == 'kind_delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_contact_kind_delete($params['kind']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_kind : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_kind : $l_delete_error");
  }
  $display['detail'] .= dis_contact_admin_index();

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'contact', 1);
  $display['detail'] = dis_contact_display_pref($prefs); 
  
} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'contact', 1);
  $display['detail'] = dis_contact_display_pref($prefs);
  
} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'contact', 1);
  $display['detail'] = dis_contact_display_pref($prefs);

} elseif ($action == 'document_add')  {
///////////////////////////////////////////////////////////////////////////////
  $params['contact_id'] = $params['ext_id'];
  if ($params['doc_nb'] > 0) {
    $nb = run_query_global_insert_documents_links($params, 'contact');
    $display['msg'] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display['msg'] .= display_err_msg($l_no_document_added);
  }
  if ($params['contact_id'] > 0) {
    $display['detail'] = dis_contact_consult($params);
  }
} elseif (!of_category_user_action_switch($module, $action, $params)) {
  if ($action == 'index' || $action == '') {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    contact_export_js_labels();
    $addressbook = $addressbooks->getMyContacts();
    $contacts = $addressbooks->searchContacts("addressbookId:$addressbook->id -is:archive");
    $current['addressbook'] = $addressbook->id;
  } elseif ($action == 'consult' || $action == 'detailconsult')  {
  //////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    $contact = OBM_Contact::get($params['id']);
    $addressbook = $addressbooks[$contact->addressbook_id];
    $current['addressbook'] = $addressbook->id;
    $current['contact'] = $contact->id;
    if ($addressbook && $addressbook->read) {
      $subTemplate['card'] = new OBM_Template('card');
      update_last_visit('contact', $params['id'], $action);
    } else {
      header('HTTP', true, 403);
      //FIXME : Not compatible with the HTML/Ajax implemetation
      OBM_Error::getInstance()->addError('rights', __('Permission denied'));
      echo OBM_Error::getInstance()->toJson();
      exit();
    }
  } elseif ($action == 'updateContact')  {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    if (isset($params['id'])) {
      $contact = OBM_Contact::get($params['id'], null, false);
    } else {
      $contact = new OBM_Contact();
      if($cgp_mailing_default) $contact->mailok = 1;
      //FIXME: Make it less 'crappy'
      if($params['company_id'])  {
        $company = get_company_info($params['company_id']);
        $contact->company_id = $company['id'];
        $contact->company = $company['name'];
        $contact->market_id = $params['market'];
      }
      if($params['shared_calendar_url'])  {
        $label = 'CALURI';
        $website = array(
          'label' => 'CALURI',
          'url' => $params['shared_calendar_url']
        );
	$contact->__set('website',array ( '0' => $website));
      }      
      if(!$params['addressbook']) {
        $contact->addressbook_id = $addressbooks->getMyContacts()->id;
      } else {
        $contact->addressbook_id = $params['addressbook'];    
      }
    }
    $addressbook = $addressbooks[$contact->addressbook_id];
    $current['addressbook'] = $addressbook->id;
    $current['contact'] = $contact->id;
    if ($addressbook && $addressbook->write && check_contact_update_rights($params)) {
      $subTemplate['card'] = new OBM_Template('form');
      $subTemplate['card']->set('categories', of_category_user_get_all('contact'));
      $subTemplate['card']->set('functions', run_query_contact_get_functions());
      $subTemplate['card']->set('datasources', run_query_contact_get_datasources());
      $subTemplate['card']->set('markets', run_query_contact_get_markets($contact->market_id));
      $subTemplate['card']->set('kinds', run_query_contact_get_kinds());
    } else {
      header('HTTP', true, 403);
      //FIXME : Not compatible with the HTML/Ajax implemetation
      OBM_Error::getInstance()->addError('rights', __('Permission denied'));
      echo OBM_Error::getInstance()->toJson();
      exit();
    }
   } elseif ($action == 'removeFromArchive')  {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    if (isset($params['contact_id'])) {
      $contact = OBM_Contact::get($params['contact_id'], null, false);
      $addressbook = $addressbooks[$contact->addressbook_id];
      if ($addressbook && $addressbook->write && (check_contact_update_rights($params))) {
        OBM_Contact::removeFromArchive($contact);
      } else {
        header('HTTP', true, 403);
        OBM_Error::getInstance()->addError('rights', __('Permission denied'));
        echo OBM_Error::getInstance()->toJson();
        exit();
      }
    } else {
      header('HTTP', true, 403);
      OBM_Error::getInstance()->addError('rights', __('Permission denied'));
      echo OBM_Error::getInstance()->toJson();
      exit();
    }
  } elseif ($action == 'storeContact') {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    $params['contact_id'] = $params['id'];
    if($params['addressbook']) 
      $addressbook = $addressbooks[$params['addressbook']];
    else  
      $addressbook = $addressbooks->getMyContacts();
    $current['addressbook'] = $addressbook->id;
    $current['contact'] = $params['id'];
    
  if ($addressbook && $addressbook->write && (check_contact_update_rights($params))) {
        if (OBM_IndexingService::connect('contact')) {
            if (check_user_defined_rules() && check_contact_data_form('', $params)) {
                if(isset($params['id'])) {
                    $c = OBM_Contact::get($params['id']);
                    $retour = run_query_contact_update($params);
                    OBM_IndexingService::commit('contact');          
                    $contact = OBM_Contact::get($params['id']);
                    update_last_visit('contact', $params['id'], $action);    
                } else {
                    $contact = $addressbook->addContact($params);
                    OBM_IndexingService::commit('contact');
                }
                $subTemplate['card'] = new OBM_Template('card');
            } else {
                header('HTTP', true, 400);
                //FIXME : Not compatible with the HTML/Ajax implemetation
                echo OBM_Error::getInstance()->toJson();
                exit();
            }
        } else {
            header('HTTP', true, 503);
            OBM_Error::getInstance()->addError('internal', $GLOBALS['l_solr_connection_err']);
            echo OBM_Error::getInstance()->toJson();
            exit();
        } 
    } else {
        header('HTTP', true, 403);
        //FIXME : Not compatible with the HTML/Ajax implemetation
        OBM_Error::getInstance()->addError('rights', __('Permission denied'));
        echo OBM_Error::getInstance()->toJson();
        exit();
    }
  } elseif ($action == 'copyContact') {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    $params['contact_id'] = $params['id'];
    $contact = OBM_Contact::get($params['id']);
    $current['addressbook'] = $addressbook->id;
    $current['contact'] = $params['id'];
    $source = $addressbooks[$contact->addressbook_id];
    $destination = $addressbooks[$params['addressbook']];
    if ($source->read && $destination && $destination->write) {
      OBM_Contact::copy($contact, $destination);
      $subTemplate['card'] = new OBM_Template('card');
    } else {
      header('HTTP', true, 403);
      //FIXME : Not compatible with the HTML/Ajax implemetation
      OBM_Error::getInstance()->addError('rights', __('Permission denied'));
      echo OBM_Error::getInstance()->toJson();
      exit();
    } 
  } elseif ($action == 'moveContact') {
  ///////////////////////////////////////////////////////////////////////////////
    $contact = OBM_Contact::get($params['id']);
    $addressbooks = OBM_AddressBook::search();
    $source = $addressbooks[$contact->addressbook_id];
    $destination = $addressbooks[$params['addressbook']];
    if ($source && $source->read && $source->write && $destination && $destination->write) {
      OBM_Contact::move($contact, $destination);
      $subTemplate['card'] = new OBM_Template('card');
    } else {
      header('HTTP', true, 403);
      //FIXME : Not compatible with the HTML/Ajax implemetation
      OBM_Error::getInstance()->addError('rights', __('Permission denied'));
      echo OBM_Error::getInstance()->toJson();
      exit();
    }

  } elseif ($action == 'deleteContact') {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    $params['contact_id'] = $params['id'];
    $contact = OBM_Contact::get($params['id']);
    $current['addressbook'] = $addressbook->id;
    $addressbook = $addressbooks[$contact->addressbook_id];
    if ($addressbook && $addressbook->write && check_can_delete_contact($params['id'])) {
      if($contact->archive) {
        OBM_Contact::delete($contact);
        update_last_visit('contact',$params['id'],'delete');
      } else {
        $contact->archive = 1;
        OBM_Contact::store($contact);
      }
      // Update "archive" addressbook
      $contacts = $addressbooks->searchContacts($params['searchpattern']);
      $subTemplate['contacts'] = new OBM_Template('contacts');
      $subTemplate['contacts']->set('fields', get_display_pref($GLOBALS['obm']['uid'], 'contact'));      
    } else {
      header('HTTP', true, 403);
      //FIXME : Not compatible with the HTML/Ajax implemetation
      OBM_Error::getInstance()->addError('rights', __('Permission denied'));
      echo OBM_Error::getInstance()->toJson();
      exit();
    }
  } elseif ($action == 'search') {
  ///////////////////////////////////////////////////////////////////////////////
    $addressbooks = OBM_AddressBook::search();
    //management of archive param  according to global configuration
    if($params['contactfilter']) $pattern .= 'displayname:'.$params['contactfilter'];
    if($params['addressbook']) $current['addressbook'] = $params['addressbook'];
    if($params['archive']) $pattern .= ' '.$params['archive'].' ';
    else $current['addressbook'] = 'search';
    $patternstring = $params['searchpattern'].' '.$pattern;
    if(function_exists('user_modify_contact_search_pattern')) {
      $patternstring = user_modify_contact_search_pattern($patternstring);
    }
    $contacts = $addressbooks->searchContacts($patternstring, $params['offset']);
    if ($params['updateCount']) echo dis_update_addressbook_count($addressbooks, $patternstring.' addressbookId:('.implode(' OR ', array_keys($addressbooks->getAddressbooks())).')', 'search');
    $subTemplate['contacts'] = new OBM_Template('contacts');
    $subTemplate['contacts']->set('offset', $params['offset']);
    $subTemplate['contacts']->set('fields', get_display_pref($GLOBALS['obm']['uid'], 'contact'));
  } elseif ($action == 'countContact') {
  ///////////////////////////////////////////////////////////////////////////////
    if(isset($params['searchpattern'])) {
      if(function_exists('user_modify_contact_search_pattern')) {
        $params['searchpattern'] = user_modify_contact_search_pattern($params['searchpattern']);
      }
      $count = $addressbooks->countContact($params['searchpattern']);
      echo $count;
      exit(0);
    } else {
      $addressbooks = OBM_AddressBook::search();
      $subTemplate['addressbooks'] = new OBM_Template('addressbooks');
    }
    //FIXME Erreur de droit
  } elseif ($action == 'filterContact') {
  ///////////////////////////////////////////////////////////////////////////////
    if(function_exists('user_modify_contact_search_pattern')) {
        $params['searchpattern'] = user_modify_contact_search_pattern($params['searchpattern']);
      }
    $addressbooks = OBM_AddressBook::search();
    if($params['contactfilter']) $pattern = 'displayname:'.$params['contactfilter'];
    $contacts = $addressbooks->searchContacts($params['searchpattern'].' '.$pattern);
    $subTemplate['contacts'] = new OBM_Template('contacts');
    $subTemplate['contacts']->set('fields', get_display_pref($GLOBALS['obm']['uid'], 'contact'));  
  } elseif ($action == 'storeAddressBook') {
  ///////////////////////////////////////////////////////////////////////////////
    if($params['addressbook_id']) {
      OBM_AddressBook::store($params);
    } else {
      OBM_AddressBook::create($params);
    }
    $addressbooks = OBM_AddressBook::search();
    $subTemplate['addressbooks'] = new OBM_Template('addressbooks');
    //FIXME Erreur de droit
  } elseif ($action == 'deleteAddressBook') {
  ///////////////////////////////////////////////////////////////////////////////
    OBM_AddressBook::delete($params);
    $addressbooks = OBM_AddressBook::search();
    $subTemplate['addressbooks'] = new OBM_Template('addressbooks');
    //FIXME Erreur de droit
  } elseif ($action == 'toggleSyncable') {
  ///////////////////////////////////////////////////////////////////////////////
    OBM_AddressBook::store($params);
    $addressbooks = OBM_AddressBook::search();
    $subTemplate['addressbooks'] = new OBM_Template('addressbooks');
    //FIXME Erreur de droit
  } elseif ($action == 'setSubscription') {
  ///////////////////////////////////////////////////////////////////////////////
    OBM_AddressBook::setSynced($params);
    $addressbooks = OBM_AddressBook::search();
    $subTemplate['addressbooks'] = new OBM_Template('addressbooks');
    //FIXME Erreur de droit
  } 

  if($_SERVER['HTTP_X_REQUESTED_WITH'] == 'XMLHttpRequest') {
    foreach($subTemplate as $template) {
      $template->set('contacts', $contacts);
      $template->set('contact', $contact);
      $template->set('addressbooks', $addressbooks);
      $template->set('current', $current);
      echo $template->render();
    } 
    exit();    
  } else {
    contact_export_js_labels();
    $template = new OBM_Template('main');
    if(!$current['addressbook']) $current['addressbook'] = $addressbooks->getMyContacts()->id;
    if(!$contacts) $contacts = $addressbooks->searchContacts("addressbookId:$current[addressbook] -is:archive"); 
    $template->set('searchpattern', $params['searchpattern']);
    $template->set('contactfilter', $params['contactfilter']);
    $template->set('contacts', $contacts);
    $template->set('contact', $contact);
    $template->set('addressbooks', $addressbooks);
    $template->set('current', $current);
    $template->set('searchfields', OBM_Contact::fieldsMap());
    $template->set('customFields', OBM_Contact::getUserCategory());
    //FIXME :  Already set in some actions
    $template->set('fields', get_display_pref($GLOBALS['obm']['uid'], 'contact'));
    $template->set('template', $subTemplate);    
    $display['detail'] = $template->render();
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_contact);
$display['end'] = display_end();
if (! $params['popup']) {
  update_contact_action();
  $display['header'] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Contact parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_contact_params() {
  
  // Get global params
  $params = get_global_params('Contact');
  
  // Get contact specific params
  if (isset ($params['town'])) $params['town'] = get_format_town($params['town']);
  if ((isset ($params['entity_id'])) && (! isset($params['contact_id']))) {
    $params['contact_id'] = $params['entity_id'];
  }
  if(isset($params['contact_id'])) $params['id'] = $params['contact_id'];
  get_global_params_document($params);
  
  // imported file
  if (isset ($_FILES['vcard_file'])) {
    $params['vcard_tmp']  = $_FILES['vcard_file']['tmp_name'];
    $params['vcard_name'] = $_FILES['vcard_file']['name'];
    $params['vcard_size'] = $_FILES['vcard_file']['size'];
    $params['vcard_type'] = $_FILES['vcard_file']['type'];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Contact Action 
///////////////////////////////////////////////////////////////////////////////
function get_contact_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete,$l_header_stats;
  global $l_header_consult, $l_header_display, $l_header_admin, $l_header_index;
  global $l_header_import,$l_header_export, $l_header_vcard, $l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  of_category_user_module_action('contact');

// ext_get_ids
  $actions['contact']['ext_get_ids'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// ext_get_id
  $actions['contact']['ext_get_id'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// Index
  $actions['contact']['index'] = array (
    'Name'     => $l_header_index,
    'Url'      => "$path/contact/contact_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

//FIXME
// Index
  $actions['contact']['consult'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  $actions['contact']['updateContact'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=updateContact",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );  

  $actions['contact']['removeFromArchive'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=removeFromArchive",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  ); 
  $actions['contact']['storeContact'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=storeContact",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );    
  $actions['contact']['deleteContact'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=deleteContact",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );      
  $actions['contact']['list'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=list",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  $actions['contact']['filterContact'] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/contact/contact_index.php?action=filterContact",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  $actions['contact']['storeAddressBook'] = array (
    'Url'      => "$path/contact/contact_index.php?action=storeAddressBook",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );    
  $actions['contact']['deleteAddressBook'] = array (
    'Url'      => "$path/contact/contact_index.php?action=deleteAddressBook",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );  
  $actions['contact']['updateAddressBook'] = array (
    'Url'      => "$path/contact/contact_index.php?action=updateAddressBook",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );  
  $actions['contact']['setSyncable'] = array (
    'Url'      => "$path/contact/contact_index.php?action=setSyncable",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );  
  $actions['contact']['setSubscription'] = array (
    'Url'      => "$path/contact/contact_index.php?action=setSubscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );  

  $actions['contact']['countContact'] = array (
    'Url'      => "$path/contact/contact_index.php?action=countContact",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );  

// Search
  $actions['contact']['search'] = array (
    'Url'      => "$path/contact/contact_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	);

// Search
  $actions['contact']['ext_search'] = array (
    'Url'      => "$path/contact/contact_index.php?action=ext_search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

// Search mail
  $actions['contact']['ext_search_mail'] = array (
    'Url'      => "$path/contact/contact_index.php?action=ext_search_mail",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

// Search kind id 
  $actions['contact']['ext_get_kind'] = array (
    'Url'      => "$path/contact/contact_index.php?action=ext_get_kind",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

// Import VCard
  $actions['contact']['import'] = array (
    'Name'     => $l_header_import,
    'Url'      => "$path/contact/contact_index.php?action=import",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     		 );
                                     		 
// Export all contacts as VCards
  $actions['contact']['save'] = array (
    'Url'      => "$path/contact/contact_index.php?action=save",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     		 );

// Export all contacts as CSV
  $actions['contact']['export'] = array (
    'Url'      => "$path/contact/contact_index.php?action=export",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     		 );

// Detail Consult
 $actions['contact']['detailconsult']   = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$params['contact_id'],
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('None')
  );

// Contact synchronisation
 $actions['contact']['sync']   = array (
    'Url'      => "$path/contact/contact_index.php?action=sync&amp;contact_id=".$params['contact_id'],
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                    		 );

// Contact desynchronisation
 $actions['contact']['desync']   = array (
    'Url'      => "$path/contact/contact_index.php?action=desync&amp;contact_id=".$params['contact_id'],
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                    		 );


// Contact copy
 $actions['contact']['copyContact']   = array (
    'Url'      => "$path/contact/contact_index.php?action=copyContact",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                    		 );

// Contact copy
 $actions['contact']['moveContact']   = array (
    'Url'      => "$path/contact/contact_index.php?action=moveContact",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                    		 );

// Vcard Export
  $actions['contact']['vcard'] = array (
    'Name'     => $l_header_vcard,
    'Url'      => "$path/contact/contact_index.php?action=vcard&amp;popup=1&amp;contact_id=".$params['contact_id'],
    'Right'    => $cright_read,
    'Privacy'  => true,    
    'Condition'=> array ('None')    
                                       );

// Detail Update
  $actions['contact']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/contact/contact_index.php?action=detailupdate&amp;contact_id=".$params['contact_id'],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     		 );

// Insert
  $actions['contact']['insert'] = array (
    'Url'      => "$path/contact/contact_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	);

// Update
  $actions['contact']['update'] = array (
    'Url'      => "$path/contact/contact_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	);
  
// VCard insert
  $actions['contact']['vcard_insert'] = array (
    'Url'      => "$path/contact/contact_index.php?action=vcard_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	);
					
// Document Add
  $actions['contact']['document_add'] = array (
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	);

// Check Delete
  $actions['contact']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/contact/contact_index.php?action=check_delete&amp;contact_id=".$params['contact_id'],
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Delete
  $actions['contact']['delete'] = array (
    'Url'      => "$path/contact/contact_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	);

// Rights Admin.
  $actions['contact']['rights_admin'] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/contact/contact_index.php?action=rights_admin&amp;entity_id=".$params['entity_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     );

// Rights Update
  $actions['contact']['rights_update'] = array (
    'Url'      => "$path/contact/contact_index.php?action=rights_update&amp;entity_id=".$params['contact_id'],
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                     );
// Statistics
  $actions['contact']['statistics'] = array (
    'Name'     => $l_header_stats,
    'Url'      => "$path/contact/contact_index.php?action=statistics",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                        );

// Admin
  $actions['contact']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/contact/contact_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                      		 );

// Function Insert
  $actions['contact']['function_insert'] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Function Update
  $actions['contact']['function_update'] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Function Check Link
  $actions['contact']['function_checklink'] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Function Delete
  $actions['contact']['function_delete'] = array (
    'Url'      => "$path/contact/contact_index.php?action=function_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Kind Insert
  $actions['contact']['kind_insert'] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions['contact']['kind_update'] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions['contact']['kind_checklink'] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions['contact']['kind_delete'] = array (
    'Url'      => "$path/contact/contact_index.php?action=kind_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Display
  $actions['contact']['display'] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/contact/contact_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions['contact']['dispref_display']	= array (
    'Url'      => "$path/contact/contact_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	        );

// Display Level
  $actions['contact']['dispref_level']= array (
    'Url'      => "$path/contact/contact_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                                );


  update_action_rights();
}


///////////////////////////////////////////////////////////////////////////////
// Contact Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_contact_action() {
  global $params, $actions, $path, $cright_read, $cright_write_admin, $obm, $profiles;

  $id = $params['contact_id'];
  if ($id > 0) {

    // Detail Consult
    $actions['contact']['detailconsult']['Url'] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=$id";
    
    // Detail Update
    $actions['contact']['detailupdate']['Url'] = "$path/contact/contact_index.php?action=detailupdate&amp;contact_id=$id";
    $actions['contact']['detailupdate']['Condition'][] = 'insert';
    
    // Check Delete
    $actions['contact']['check_delete']['Url'] = "$path/contact/contact_index.php?action=check_delete&amp;contact_id=$id";
    $actions['contact']['check_delete']['Condition'][] = 'insert';

    // Rights admin
    $actions['contact']['rights_admin']['Url'] = "$path/contact/contact_index.php?action=rights_admin&amp;entity_id=".$id;
    $actions['contact']['rights_admin']['Condition'][] = 'insert';

    update_action_rights();

  } else {
    $actions['contact']['import']['Condition'][] = 'insert';
    $actions['contact']['save']['Condition'][] = 'insert';
  }
}


///////////////////////////////////////////////////////////////////////////////
// Contact entity rights actions updates
///////////////////////////////////////////////////////////////////////////////
function update_action_rights() {
  global $params, $actions, $path, $cright_read, $cright_write_admin, $obm, $profiles;

  $cright_forbidden = 32;

  $id = $params['contact_id'];
  if ($id > 0) {
    $c = get_contact_info($id);

    // Allow public contact handling only if write_admin right
    if ($c['privacy'] != 1) {
      $actions['contact']['detailupdate']['Right'] = $cright_write_admin;
      $actions['contact']['update']['Right'] = $cright_write_admin;
      $actions['contact']['insert']['Right'] = $cright_write_admin;
      $actions['contact']['check_delete']['Right'] = $cright_write_admin;
      $actions['contact']['delete']['Right'] = $cright_write_admin;

    } else {
      // update the admin rights on the current contact
      if ($c['usercreate'] == $obm['uid'] || OBM_Acl::canAdmin($obm['uid'], 'contact', $id)) {
        $actions['contact']['rights_admin']['Right'] = $cright_read;
        $actions['contact']['rights_update']['Right'] = $cright_read;
      } else {
        $actions['contact']['rights_admin']['Right'] = $cright_write_admin;
        $actions['contact']['rights_update']['Right'] = $cright_write_admin;
      }

      // update the update rights on the current contact
      if ($c['usercreate'] == $obm['uid'] || OBM_Acl::canWrite($obm['uid'], 'contact', $id)) {
        $actions['contact']['update']['Right'] = $cright_read;
        $actions['contact']['delete']['Right'] = $cright_read;
        $actions['contact']['detailupdate']['Right'] = $cright_read;
        $actions['contact']['check_delete']['Right'] = $cright_read;
      } else {
        $actions['contact']['update']['Right'] = $cright_forbidden;
        $actions['contact']['delete']['Right'] = $cright_forbidden;
        $actions['contact']['detailupdate']['Right'] = $cright_forbidden;
        $actions['contact']['check_delete']['Right'] = $cright_forbidden;
      }

      // update the read rights on the current contact
      if ($c['usercreate'] == $obm['uid'] || OBM_Acl::canRead($obm['uid'], 'contact', $id)) {
        $actions['contact']['detailconsult']['Right'] = $cright_read;
      } else {
        $actions['contact']['detailconsult']['Right'] = $cright_forbidden;
      }

    }

  }

}


?>
