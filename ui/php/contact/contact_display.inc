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
// OBM - File : contact_display.php                                          //
//     - Desc : Contact Display File                                         //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//


// Direct fields
$fieldnames['displayname'] = $l_displayname;
$fieldnames['aka'] = $l_aka;
$fieldnames['addressbook'] = $l_addressbook;
$fieldnames['archive'] = $l_archive;
$fieldnames['function_label'] = $l_function;
$fieldnames['title'] = $l_title;
$fieldnames['address'] = $l_address;
$fieldnames['service'] = $l_service;
$fieldnames['country'] = $l_country;
$fieldnames['workfax'] = $l_phone_labels['WORK_FAX'];
$fieldnames['homevoice'] = $l_phone_labels['HOME_VOICE'];
$fieldnames['workvoice'] = $l_phone_labels['WORK_VOICE'];
$fieldnames['cellvoice'] = $l_phone_labels['CELL_VOICE'];
$fieldnames['email'] = $l_email;
$fieldnames['archive'] = $l_archive;
$fieldnames['date'] = $l_date;
$fieldnames['company'] = $l_company;
$fieldnames['commonname'] = $l_commonname;

// Calculated fields


///////////////////////////////////////////////////////////////////////////////
// Display Contact specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_contact(&$OD, $fieldname, $link_ok) {
  global $path, $params, $cgp_show, $ico_mail;
  global $l_sync, $l_desync, $ico_sync, $ico_desync;

  $show_date = (! $cgp_hide['contact']['contact_date']);
  $ext_url = $params['ext_url'];

    //  if (($fieldname == 'contact_lastname') && $link_ok) {
  if ($fieldname == 'displayname') {
     $displayname = $OD->data_set->f('contact_firstname').' '.$OD->data_set->f('contact_lastname');
    if(trim($displayname) == '') {
      $displayname = $GLOBALS['l_displayname_alternative'];
    }
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_contact_get_id(".$OD->data_set->f('contact_id').",'".phpStringToJsString($displayname)."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_contact_get_id_url('$ext_url',".$OD->data_set->f('contact_id').");";
    } else {
      $res['url'] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$OD->data_set->f('contact_id');
    }
    $res['name'] = $displayname;
  }

  //  if (($fieldname == 'contact_lastname') && $link_ok) {
  if ($fieldname == 'lastname') {
    if(trim($OD->data_set->f('contact_lastname')) == '') {
      $contact_lastname = '-';
      $res['align'] = 'center';
    } else {
      $contact_lastname = $OD->data_set->f('contact_lastname');
    }
    $res['name'] = $contact_lastname;
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_contact_get_id(".$OD->data_set->f('contact_id').",'".phpStringToJsString($contact_lastname)."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_contact_get_id_url('$ext_url',".$OD->data_set->f('contact_id').");";
    } else {
      $res['url'] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$OD->data_set->f('contact_id');
      // Prepend the sync link
      if($GLOBALS['c_use_connectors'] && $link_ok) {
        $is_synced = get_contact_is_synced($OD->data_set->f('contact_addressbook_id'));
        $alt = $is_synced ? $l_desync : $l_sync;
        $ico = $is_synced ? $ico_sync : $ico_desync;
        $iis_synced = (int) $is_synced;
        $res['name'] = "<a class=\"LF\" x-obm-sync=\"$iis_synced\" href=\"#\" onclick=\"Obm.Contact.syncContact(".$OD->data_set->f('contact_id').", this); return false;\" title=\"$alt\"><img src=\"$ico\" /></a>
        &nbsp;<a href=\"$res[url]\">$res[name]</a>";
        $res['url'] = '';
      }
    }
  }

  else if (($fieldname == 'name') && $link_ok) {
    if ($cgp_show['module']['company']) {
      $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('contact_company_id');
    } else {
      $res['name'] = $OD->data_set->f('contact_company');
    }      
  }

  else if ($fieldname == 'archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if (($fieldname == 'mailing_ok')
	   || ($fieldname == 'contact_newsletter')) {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'email') {
    $email = $OD->data_set->f('email_address');
    if (strcmp($email,'') != 0) {
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\" />";
      $res['txt_name'] = $email;
    }
  }

  else if ($fieldname == 'address') {
    if ($OD->data_set->f('contact_service') != '') {
      $res['name'] .= $OD->data_set->f('contact_service')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('contact_service') . ' ';
    }
    if ($OD->data_set->f('address_street') != '') {
      $res['name'] .= $OD->data_set->f('address_street')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_street') . ' ';
    }
    if ($OD->data_set->f('address_zipcode') != '') {
      $res['name'] .= $OD->data_set->f('address_zipcode')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_zipcode');
    }
    if ($OD->data_set->f('address_town') != '') {
      $res['name'] .= $OD->data_set->f('address_town')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_town');
    }
    if ($OD->data_set->f('address_expresspostal') != '') {
      $res['name'] .= $OD->data_set->f('address_expresspostal')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_expresspostal');
    }
    if ($OD->data_set->f('country_name') != '') {
      $res['name'] .= $OD->data_set->f('country_name')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('country_name');
    }
  }

  else if ($fieldname == 'company') {
    $res['name'].= $OD->data_set->f('company_name');
  }

  else if ($fieldname == 'service') {
    $res['name'].= $OD->data_set->f('contact_service');
  }

  else if ($fieldname == 'country') {
    if ($OD->data_set->f('country_name') != '') {
      $res['name'] .= $OD->data_set->f('country_name');
    }
  }

  else if ($fieldname == 'date') {
    if ($show_date) {
      $date = $OD->data_set->f('contact_date');
      $res['name'] = ($date) ? of_date_format($date) : ' ';
      $res['align'] = 'center';
    }
  }

  return $res;
}


function contact_export_js_labels() {
  $GLOBALS['js']['vars']['labels']['label'] = $GLOBALS['l_label'];
  $GLOBALS['js']['vars']['labels']['remove'] = $GLOBALS['l_remove'];
  $GLOBALS['js']['vars']['labels']['phoneLabel'] = $GLOBALS['l_phone_labels'];
  $GLOBALS['js']['vars']['labels']['emailLabel'] = $GLOBALS['l_email_labels'];
  $GLOBALS['js']['vars']['labels']['addressLabel'] = $GLOBALS['l_address_labels'];
  $GLOBALS['js']['vars']['labels']['websiteLabel'] = $GLOBALS['l_website_labels'];
  $GLOBALS['js']['vars']['labels']['imLabel'] = $GLOBALS['l_im_labels'];
  $GLOBALS['js']['vars']['labels']['phoneNumber'] = $GLOBALS['l_number'];
  $GLOBALS['js']['vars']['labels']['emailAddress'] = $GLOBALS['l_email'];
  $GLOBALS['js']['vars']['labels']['websiteUrl'] = $GLOBALS['l_website'];
  $GLOBALS['js']['vars']['labels']['imAddress'] = $GLOBALS['l_im'];
  $GLOBALS['js']['vars']['labels']['addressStreet'] = $GLOBALS['l_street'];
  $GLOBALS['js']['vars']['labels']['addressTown'] = $GLOBALS['l_town'];
  $GLOBALS['js']['vars']['labels']['addressZipcode'] = $GLOBALS['l_postcode'];
  $GLOBALS['js']['vars']['labels']['addressExpressPostal'] = $GLOBALS['l_expresspostal']; 
  $GLOBALS['js']['vars']['labels']['addressCountry'] = $GLOBALS['l_country'];
  $GLOBALS['js']['vars']['labels']['confirmDeleteAddressBook'] = $GLOBALS['l_confirm_delete_ad'];
  $GLOBALS['js']['vars']['labels']['confirmDeleteContact'] = $GLOBALS['l_confirm_delete_contact'];
  $ctry_q = run_query_global_country_for_lang();
  $GLOBALS['js']['vars']['labels']['countries']['none'] = '---';
  while($ctry_q->next_record()) {
    $GLOBALS['js']['vars']['labels']['countries'][$ctry_q->f('country_iso3166')] = $ctry_q->f('country_name');
  }   

  
}
/**
 * Display Contact search form
 *
 * @param $contact[] : hash with contact values
 **/
function dis_contact_search_form($contact='') {
  global $cgp_hide;

  $GLOBALS['js']['vars']['labels']['label'] = $GLOBALS['l_label'];
  $GLOBALS['js']['vars']['labels']['remove'] = $GLOBALS['l_remove'];
  $GLOBALS['js']['vars']['labels']['phoneLabel'] = $GLOBALS['l_phone_labels'];
  $GLOBALS['js']['vars']['labels']['emailLabel'] = $GLOBALS['l_email_labels'];
  $GLOBALS['js']['vars']['labels']['addressLabel'] = $GLOBALS['l_address_labels'];
  $GLOBALS['js']['vars']['labels']['websiteLabel'] = $GLOBALS['l_website_labels'];
  $GLOBALS['js']['vars']['labels']['imLabel'] = $GLOBALS['l_im_labels'];
  $GLOBALS['js']['vars']['labels']['phoneNumber'] = $GLOBALS['l_number'];
  $GLOBALS['js']['vars']['labels']['emailAddress'] = $GLOBALS['l_email'];
  $GLOBALS['js']['vars']['labels']['websiteUrl'] = $GLOBALS['l_website'];
  $GLOBALS['js']['vars']['labels']['imAddress'] = $GLOBALS['l_im'];
  $GLOBALS['js']['vars']['labels']['addressStreet'] = $GLOBALS['l_street'];
  $GLOBALS['js']['vars']['labels']['addressTown'] = $GLOBALS['l_town'];
  $GLOBALS['js']['vars']['labels']['addressZipcode'] = $GLOBALS['l_postcode'];
  $GLOBALS['js']['vars']['labels']['addressExpressPostal'] = $GLOBALS['l_expresspostal']; 
  $GLOBALS['js']['vars']['labels']['addressCountry'] = $GLOBALS['l_country'];
  $GLOBALS['js']['vars']['labels']['countries']['none'] = '---';
  $ctry_q = run_query_global_country_for_lang();
  while($ctry_q->next_record()) {
    $GLOBALS['js']['vars']['labels']['countries'][$ctry_q->f('country_iso3166')] = $ctry_q->f('country_name');
  }  

  if ($cgp_hide['contact']['responsible']) {
    $usr_q = '';
  } else {
    $usr_q = run_query_userobm_responsable('Contact',array('contact_marketingmanager_id'));
  }
  $funcs = of_category_get_ordered('contact', 'function');
  $dsrc_q = run_query_global_datasource();
  $block .= html_contact_search_form($contact, $usr_q, $funcs, $ctry_q, $dsrc_q);
//  $block = html_contact($contact['contact_id']);
  return $block;
}


/**
 * html_contact_get_headers 
 * 
 * @access public
 * @return void
 */
function html_contact_get_headers() {
  $prefs = get_display_pref($obm['uid'], 'contact');
  return $headers;
}


/**
 * html_addressbooks_get_list()
 */
function html_addressbooks_get_list() {
  $addressBooks = OBM_AddressBook::search();
  foreach($addressBooks as $id => $ad) {
    $name = $ad->displayname;
    $synced = $ad->synced;
    $ico = $GLOBALS['ico_desync'];
    $l_sync = $GLOBALS['l_sync_ad'];
    if ($synced) {
      $ico = $GLOBALS['ico_sync'];
      $l_sync = $GLOBALS['l_desync_ad'];
    }
     
    // Build drop down menu
    $field = '<ul id="ad'.$id.'" class="dropDownMenu addressBookMenu"><li><img src="'.$GLOBALS['icones']['dropdown'].'" alt="'.$GLOBALS['l_add'].'" /><ul>';
    // Can't update or delete default addressbook
    if (!$ad->isDefault && $ad->admin) {
      $field .= "<li><a href=\"\" onclick=\"obm.contact.addressbook.updateAddressBook($id);return false;\" >$GLOBALS[l_update_ad]</a></li>";
      $field .= "<li><a href=\"\" onclick=\"obm.contact.addressbook.deleteAddressBook($id, '".phpStringToJsString($name)."');return false;\" >$GLOBALS[l_delete_ad]</a></li>";
      //$dblclick = 'onDblclick="obm.contact.addressbook.updateAddressBook('.$id.');"';
    }

    if ($ad->admin) {
      $field .= "<li><a href=\"contact_index.php?action=rights_admin&entity_id=$id\" >$GLOBALS[l_rights]</a></li>";
    }

    if ($ad->syncable) {
      $field .= "<li><a href=\"\" onclick=\"obm.contact.addressbook.setSubscription($id);return false;\" >$l_sync</a></li>";
    }

    $field .= "</ul></ul>";

    $dropDownMenu .= "new Obm.DropDownMenu($('ad$id'));";

    $content = "<div id='addressbook_$id'><img src='$ico' />$name $field </div>
      <form id='f_addressbook_$id' name='f_addressbook_$id' onsubmit='obm.contact.addressbook.storeAddressBook($id);return false;'>
      <input type='text' id='tf_addressbook_$id' value='$name' style='display:none;'/>
      </form>";
    $class = "";
    if (!isset($addressBook->id)) {
      if ($ad->name == 'contacts') $class = "current";
    } else {
      if($id == $addressBook->id) $class = "current";
    }
    $adList .= '<tr><td '.$dblclick.' onclick="obm.contact.addressbook.selectAddressBook(this)" id="addressbook-'.$ad->id.'" class="'.$class.'">'.$content.'
      <input type="hidden" value="in:'.$ad->id.' archive:0" id="searchPattern-'.$ad->id.'" />
      <input type="hidden" value="'.$ad->write.'" id="ad-write-'.$ad->id.'"/></td></tr>';
  }

  $adList .= "<tr>
   <td onclick='obm.contact.addressbook.selectAddressBook(this)' id='addressbook-archive' style='padding-left:16px;'>$GLOBALS[l_archive]
   <input type='hidden' value='archive:1' id='searchPattern-archive' />
   <input type='hidden' value='0' id='ad-write-archive'/>
   </td>
  </tr>

  <tr id='searchResultsFolder' style='display:none;'>
   <td onclick='obm.contact.addressbook.selectAddressBook(this);' id='addressbook-searchResults' style='padding-left:16px;'>$GLOBALS[l_search_results]
   <input type='hidden' value='' id='searchPattern-searchResults' />
   <input type='hidden' value='0' id='ad-write-searchResults'/>
   </td>
  </tr>";

  $adList .= "<tr class='filler'><th>&nbsp;</th></tr>";

  $block = "<table>  
     <tbody>
     $adList
     </tbody>
   </table>
   <script type='text/javascript'>
     $dropDownMenu
   </script>";
  return $block;

}


/**
 * html_contact_get_list 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function html_contact_get_list($contacts, $headers) {
  foreach($contacts as $id => $contact) {
    if($class == 'even') {
      $class = 'odd';
    } else {
      $class = 'even';
    } 
    $contactRow = '';
    foreach($headers as $field => $metada) {
      $fieldname = str_replace('contact_','', $field);
      $contactRow .= sprintf($metada['template'], $contact->$fieldname);
    }
    $contactList .= "<tr onclick='obm.contact.addressbook.selectContact(this)' id='contact-$id'class='$class'>$contactRow</tr>\n";
  }
  $block = "
    <table>
    <tbody>
      $contactList
      <tr class='filler'><th>&nbsp;</th></tr>
    </tbody>
    </table>";
  return $block;
  
}


/**
 * dis_contact_consult2 
 * 
 * @param mixed $contact 
 * @access public
 * @return void
 */
//TODO check if this function is used somewhere
function dis_contact_consult2($contact) {
  $addressBooks = OBM_AddressBook::search();
  foreach($addressBooks as $id => $addressBook) {
    if($addressBook->write == 1) {
      $copy .= '<li><a href="" onclick="obm.contact.addressbook.copyContact('.$contact->id.','.$addressBook->id.'); return false;">'.$addressBook->displayname.'</a></li>';
      //$move .= '<li><a href="" onclick="obm.contact.addressbook.moveContact('.$contact->id.','.$addressBook->id.')return false;">'.$addressBook->displayname.'</a></li>';
    }
    if ($addressBook->id == $contact->addressbook_id) {
      $write = $addressBook->write;
    }
  }

  $toolbar = "<ul id='toolbarMenu' class='dropDownMenu'>";

  if ($write) {
    $toolbar .="<li>
      <a href='' onclick='obm.contact.addressbook.updateContact($contact->id, $contact->addressbook_id); return false;' >
      <img src='".$GLOBALS['icones']['edit']."' alt='$GLOBALS[l_header_update]' title='$GLOBALS[l_header_update]' /></a>
    </li><li>
      <a href='' onclick='obm.contact.addressbook.deleteContact($contact->id, \"".$contact->name."\"); return false;' >
      <img src='".$GLOBALS['icones']['delete']."' alt='$GLOBALS[l_header_delete]' title='$GLOBALS[l_header_delete]' /></a></li>";
  }
  $toolbar .="<li><img src='".$GLOBALS['icones']['others']."' title='$GLOBALS[l_others]' alt='$GLOBALS[l_others]' />
        <ul><li>$GLOBALS[l_copy]<ul>$copy</ul></li>
        <li><a href='$GLOBALS[path]/contact/contact_index.php?action=vcard&amp;contact_id=$contact->id' >Exporter</a></li>
        </ul>
      </li>
    </ul>
    <script type='text/javascript'>
      new Obm.DropDownMenu($('toolbarMenu'));
    </script>";

  if($contact->lastname) {
    $nameLayout =  strtoupper($contact->lastname).' ';
  }
  if($contact->mname) {
    $nameLayout .= ucwords($contact->mname).' ';
  }
  if($contact->firstname) {
    $nameLayout .= ucwords($contact->firstname).' ';
  }
  if($contact->suffix) {
    $nameLayout .= '('.ucwords($contact->suffix).') ';
  }
  if(isset($nameLayout)) {
    $nameLayout = '<h1>'.$nameLayout.'</h1>';
  } else {
    $nameLayout = '<h1>'.$GLOBALS['l_noname'].'</h1>';
  }
  if($contact->aka) {
    $nicknameLayout = '<br /><h3>'.$GLOBALS['l_aka'].' : </h3><span>'.$contact->aka.'</span>';
  }
  if($contact->company) {
    $company = $contact->company;
    if($contact->company_id > 0) {
      $company = "<a href='$GLOBALS[path]/company/company_index.php?action=detailconsult&amp;company_id=".$contact->company_id."'>$company</a>";
    }
    $companyLayout = '<br /><h3>'.$GLOBALS['l_company'].' : </h3><span>'.$company.'</span>';
  }
  if($contact->title) {
    $titleLayout = '<h3>'.$GLOBALS['l_title'].' : </h3><span>'.$contact->title.'</span>';
  }
  
  $class = 'H';
  foreach($contact->phone as $phone) {
    $class = '';
    $phoneLayout .= '<dt>'.$contact->labelToString($phone['label'], 'phone').' : </dt><dd>'.$phone['number'].'</dd>';
  }
  $phoneLayout ='<dl id="phoneLayout" class="details '.$class.'">'.$phoneLayout.'</dl>';
  
  $class = 'H';
  foreach($contact->email as $email) {
    $class = '';
    $emailLayout .= '<dt>'.$contact->labelToString($email['label'], 'email').' : </dt><dd><a href="mailto:'.$email['address'].'">'.$email['address'].'</a></dd>';
  }   
  $emailLayout ='<dl id="emailLayout" class="details '.$class.'">'.$emailLayout.'</dl>';
  
  $class = 'H';
  foreach($contact->address as $address) {
    $class = '';
    $label = $address['label'];
    unset($address['label']);
    $addressLayout .= '<dt>'.$contact->labelToString($label, 'address').' : </dt><dd>'.implode('<br />',array_filter($address)).'</dd>';
  }  
  $addressLayout ='<dl id="addressLayout" class="details '.$class.'">'.$addressLayout.'</dl>';

  $class = 'H';
  foreach($contact->im as $im) {
    $class = '';
    $imLayout .= '<dt>'.$contact->labelToString($im['protocol'], 'im').' : </dt><dd>'.$im['address'].'</dd>';
  }  
  $imLayout ='<dl id="imLayout" class="details '.$class.'">'.$imLayout.'</dl>';

  $class = 'H';
  foreach($contact->website as $website) {
    $class = '';
    $websiteLayout .= '<dt>'.$contact->labelToString($website['label'], 'website').' : </dt><dd><a target="_blank" href="'.$website['url'].'">'.$website['url'].'</a></dd>';
  }  
  $websiteLayout ='<dl id="websiteLayout" class="details '.$class.'">'.$websiteLayout.'</dl>';

  $class = 'H';
  if($contact->birthday) {
    $class = '';
    $anniversaryLayout = '<dt>'.$GLOBALS['l_birthday'].' : </dt><dd>'.$contact->birthday->getOutputDate().'</dd>';
  }
  if($contact->anniversary) {
    $class = '';
    $anniversaryLayout .= '<dt>'.$GLOBALS['l_anniversary'].' : </dt><dd>'.$contact->anniversary->getOutputDate().'</dd>';
  }
  if($contact->date) {
    $class = '';
    $anniversaryLayout .= '<dt>'.$GLOBALS['l_date'].' : </dt><dd>'.$contact->date->getOutputDate().'</dd>';
  }  
  $anniversaryLayout ='<dl id="anniversaryLayout" class="details '.$class.'">'.$anniversaryLayout.'</dl>';
  
  $class = 'H';
  if($contact->manager) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_manager'].' : </dt><dd> '.$contact->manager.'</dd>';
  }
  if($contact->assistant) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_assistant'].' : </dt><dd> '.$contact->assistant.'</dd>';
  }
  if($contact->spouse) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_spouse'].' : </dt><dd> '.$contact->spouse.'</dd>';
  }
  if($contact->category) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_category'].' : </dt><dd> '.$contact->category.'</dd>';
  }
  if($contact->service) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_service'].' : </dt><dd> '.$contact->service.'</dd>';
  }
  if($contact->mailok) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_mailing_ok'].' : </dt><dd> '.(($contact->mailok)?$GLOBALS['l_yes']:$GLOBALS['l_no']).'</dd>';
  }
  if($contact->newsletter) {
    $class = '';
    $otherLayout .= '<dt>'.$GLOBALS['l_newsletter'].' : </dt><dd> '.(($contact->newsletter)?$GLOBALS['l_yes']:$GLOBALS['l_no']).'</dd>';
  }
  $otherLayout ='<dl id="otherLayout" class="details '.$class.'">'.$otherLayout.'</dl>';

  $class = 'H';
  if($contact->comment) {
    $class = '';
  }
  $commentLayout ='<dl id="commentLayout" class="details '.$class.'">'.$contact->comment.'</dl>';

  $class = 'H';
  if($contact->comment2) {
    $class = '';
  }
  $comment2Layout ='<dl id="comment2Layout" class="details '.$class.'">'.$contact->comment2.'</dl>';

  $class = 'H';
  if($contact->comment3) {
    $class = '';
  }
  $comment3Layout ='<dl id="comment3Layout" class="details '.$class.'">'.$contact->comment2.'</dl>';

  //  datasource
  //  public 'kind' => null
  //  public 'function' => null
  //  userupdate
  //  usercreate
  //  of_category...


  $block = "
    <table id='contact-card-".$contact->id."'>
    <thead>
    <tr><th><a href='' onclick='obm.contact.addressbook.hideContact(); return false;'><img class='RF' src='".$GLOBALS['icones']['close']."' alt='$GLOBALS[l_close]' /></a>$GLOBALS[l_contact]</th></tr>
    <tr><td class='toolbar'>$toolbar</td></tr>
    </thead>
    <tbody>
    <tr><td>
    <img id='photo' src='$GLOBALS[ico_nophoto]' />
    <div class='head'>
    $nameLayout
    $nicknameLayout
    $titleLayout
    $companyLayout
    </div>
    <p class='LC' />
    $phoneLayout
    $emailLayout
    $addressLayout
    $websiteLayout
    $imLayout
    $anniversaryLayout
    $otherLayout
    $commentLayout
    $comment2Layout
    $comment3Layout
    </td>
    </tr>
    </tbody>
    </table>
  ";
  return $block;
}

/**
 * dis_contact_consult2 
 * 
 * @param mixed $contact 
 * @access public
 * @return void
 */
//TODO check if this function is used somewhere

function dis_contact_form2($params) {

  $add = array();
  if($params['id'])
    $contact = OBM_Contact::get($params['id']);
  else {
    $contact = new OBM_Contact();
    $contact->addressbook_id = $params['addressbook'];
  }
  $fields = array(
    'lastname' => array('class' => 'head', 'size' => 10, 'mandatory' => true),
    'mname' => array('class' => 'head', 'size' => 8),
    'firstname' => array('class' => 'head', 'size' => 10, 'mandatory' => true),
    'suffix' => array('class' => 'head', 'size' => 4),
    'aka' => array('class' => 'head', 'prepend' => '<br />'),
    'company' => array('class' => 'head', 'prepend' => '<br />', 'mandatory' => true),
    'title' => array('class' => 'head'),
    'phone' => array('type' => 'phone', 'class' => 'details coordinate', 'prepend' => '<p class="LC" />', 'mandatory' => true), 
    'email' => array('type' => 'email', 'class' => 'details coordinate', 'mandatory' => true), 
    'address' => array('type' => 'address', 'class' => 'details coordinate', 'mandatory' => true), 
    'im' => array('type' => 'im', 'class' => 'details coordinate'), 
    'website' => array('type' => 'website', 'class' => 'details coordinate'), 
    'birthday' => array('type' => 'date'), 
    'anniversary' => array('type' => 'date'),
    'date' => array('type' => 'date'),
    'manager' => array(), 
    'assistant' => array(),
    'spouse' => array(), 
    'category' => array(),
    'service' => array(), 
    'mailok' => array('type' => 'checkbox'),
    'newsletter' => array('type' => 'checkbox'), 
    'comment' => array('type' => 'textarea', 'mandatory' => true),
    'comment2' => array('type' => 'textarea'),
    'comment3' => array('type' => 'textarea')
  );
  foreach($fields as $fieldname => $metadata) {
    $addable = false;
    $addJs = array();
    if(isset($metadata['class'])) $class = $metadata['class']; else $class = 'details';
    switch($metadata['type']) {
    case 'date' :
      $addJs[$fieldname] = "$('$fieldname').setStyle('display','');";
      $value = ($contact->$fieldname)?$contact->$fieldname->getInputDate() : '';
      $field = '
        <span class="NW"><input autocomplete="off" size="'.$metadata['size'].'" type="text" title="'.$GLOBALS['l_'.$fieldname].'" name="'.$fieldname.'" value="'.$value.'" />
        <img src="'.$GLOBALS['ico_datepicker'].'" onclick="displayDatePicker($(\''.$fieldname.'\').getElement(\'input\'))" /></span>';
      break;
    case 'checkbox' :
      $addJs[$fieldname] = "$('$fieldname').setStyle('display','');";
      $checked = ($contact->$fieldname == 1)? 'checked="checked"' : '';
      $field = '<input type="checkbox" '.$checked.' title="'.$GLOBALS['l_'.$fieldname].'" name="'.$fieldname.'" value="1" />';
      break;
    case 'textarea':
      $addJs[$fieldname] = "$('$fieldname').setStyle('display','');";
      $value = ($contact->$fieldname)?$contact->$fieldname : '';
      $field = '<textarea size="'.$metadata['size'].'" title="'.$GLOBALS['l_'.$fieldname].'" name="'.$fieldname.'">'.$value.'</textarea>';
      break;      
    case 'phone':
      $field = '<ul id="'.$fieldname.'Menu"  class="dropDownMenu coordinateMenu"><li><img src="'.$GLOBALS['ico_add'].'" alt="'.$GLOBALS['l_add'].'" /><ul>';
      foreach($GLOBALS['l_'.$fieldname.'_labels'] as $label => $title) {
        $field .= "<li><a href=\"\" onclick=\"new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});return false;\" value=\"$label\">$title</a></li>";
        $addJs[$fieldname][$title] = "new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});$('$fieldname').setStyle('display','');return false;";
      }
      $field .= '</li></ul>';      
      if(!empty($contact->$fieldname)) {
        foreach($contact->$fieldname as $coords) {
          $value = "{label: {value: '".phpStringToJsString($contact->labelToString($coords['label'], $fiedname, false))."', label:'".phpStringToJsString($contact->labelToString($coords['label'], $fieldname))."'}, number: {value: '".phpStringToJsString($coords['number'])."'}}";
          $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget($value,{container:'${fieldname}Menu', inject: 'before'});</script>";

        }
      } else {
        $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget({},{container:'${fieldname}Menu', inject: 'before'});</script>";
      }
      $addable = true;
      break;
    case 'website':
      $field = '<ul id="'.$fieldname.'Menu"  class="dropDownMenu coordinateMenu"><li><img src="'.$GLOBALS['ico_add'].'" alt="'.$GLOBALS['l_add'].'" /><ul>';
      foreach($GLOBALS['l_'.$fieldname.'_labels'] as $label => $title) {
        $field .= "<li><a href=\"\" onclick=\"new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});return false;\" value=\"$label\">$title</a></li>";
        $addJs[$fieldname][$title] = "new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});$('$fieldname').setStyle('display','');return false;";
      }
      $field .= '</li></ul>';      
      if(!empty($contact->$fieldname)) {
        foreach($contact->$fieldname as $coords) {
          $value = "{label: {value: '".phpStringToJsString($contact->labelToString($coords['label'], $fiedname, false))."', label:'".phpStringToJsString($contact->labelToString($coords['label'], $fieldname))."'}, url: {value: '".phpStringToJsString($coords['url'])."'}}";
          $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget($value,{container:'${fieldname}Menu', inject: 'before'});</script>";
        }
      } else {
        $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget({},{container:'${fieldname}Menu', inject: 'before'});</script>";
      }      
      $addable = true;
      break;
    case 'email':
      $field = '<ul id="'.$fieldname.'Menu"  class="dropDownMenu coordinateMenu"><li><img src="'.$GLOBALS['ico_add'].'" alt="'.$GLOBALS['l_add'].'" /><ul>';
      foreach($GLOBALS['l_'.$fieldname.'_labels'] as $label => $title) {
        $field .= "<li><a href=\"\" onclick=\"new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});return false;\" value=\"$label\">$title</a></li>";
        $addJs[$fieldname][$title] = "new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});$('$fieldname').setStyle('display','');return false;";
      }
      $field .= '</li></ul>';      
      if(!empty($contact->$fieldname)) {
        foreach($contact->$fieldname as $coords) {
          $value = "{label: {value: '".phpStringToJsString($contact->labelToString($coords['label'], $fiedname, false))."', label:'".phpStringToJsString($contact->labelToString($coords['label'], $fieldname))."'}, address: {value: '".phpStringToJsString($coords['address'])."'}}";
          $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget($value,{container:'${fieldname}Menu', inject: 'before'});</script>";
        }
      } else {
        $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget({},{container:'${fieldname}Menu', inject: 'before'});</script>";
      }      
      $addable = true;
      break; 
    case 'im':
      $field = '<ul id="'.$fieldname.'Menu"  class="dropDownMenu coordinateMenu"><li><img src="'.$GLOBALS['ico_add'].'" alt="'.$GLOBALS['l_add'].'" /><ul>';
      foreach($GLOBALS['l_'.$fieldname.'_labels'] as $label => $title) {
        $field .= "<li><a href=\"\" onclick=\"new Obm.Contact.".strtoupper($fieldname)."Widget({protocol: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});return false;\" value=\"$label\">$title</a></li>";
        $addJs[$fieldname][$title] = "new Obm.Contact.".strtoupper($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});$('$fieldname').setStyle('display','');return false;";
      }
      $field .= '</li></ul>';      
      if(!empty($contact->$fieldname)) {
        foreach($contact->$fieldname as $coords) {
          $value = "{protocol: {value: '".phpStringToJsString($coords['protocol'])."', label:'".phpStringToJsString($contact->labelToString($coords['protocol'], $fieldname))."'}, address: {value: '".phpStringToJsString($coords['address'])."'}}";
          $field .=  "<script language='text/javascript'>new Obm.Contact.".strtoupper($fieldname)."Widget($value,{container:'${fieldname}Menu', inject: 'before'});</script>";
        }
      } else {
        $field .=  "<script language='text/javascript'>new Obm.Contact.".strtoupper($fieldname)."Widget({},{container:'${fieldname}Menu', inject: 'before'});</script>";
      }      
      $addable = true;
      break;   
    case 'address':
      $field = '<ul id="'.$fieldname.'Menu"  class="dropDownMenu coordinateMenu"><li><img src="'.$GLOBALS['ico_add'].'" alt="'.$GLOBALS['l_add'].'" /><ul>';
      foreach($GLOBALS['l_'.$fieldname.'_labels'] as $label => $title) {
        $field .= "<li><a href=\"\" onclick=\"new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});return false;\" value=\"$label\">$title</a></li>";
        $addJs[$fieldname][$title] = "new Obm.Contact.".ucfirst($fieldname)."Widget({label: {value: '".phpStringToJsString($label)."', label:'".phpStringToJsString($title)."'}},{container:'${fieldname}Menu', inject: 'before'});$('$fieldname').setStyle('display','');return false;";
      }
      $field .= '</li></ul>';      
      if(!empty($contact->$fieldname)) {
        foreach($contact->$fieldname as $coords) {
          $value = "street: {value: '".phpStringToJsString($coords['street'])."'}, zipcode: {value: '".phpStringToJsString($coords['zipcode'])."'}, town: {value: '".phpStringToJsString($coords['town'])."'}, expresspostal: {value: '".phpStringToJsString($coords['expresspostal'])."', country: {value: '".phpStringToJsString($coords['country'])."'}";
          $value = "{label: {value: '".phpStringToJsString($contact->labelToString($coords['label'], $fiedname, false))."', label:'".phpStringToJsString($contact->labelToString($coords['label'], $fieldname))."'}, $value}}";
          $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget($value,{container:'${fieldname}Menu', inject: 'before'});</script>";
        }
      } else {
        $field .=  "<script language='text/javascript'>new Obm.Contact.".ucfirst($fieldname)."Widget({},{container:'${fieldname}Menu', inject: 'before'});</script>";
      }      
      $addable = true;
      break;         
    case 'text' :
    default:
      $addJs[$fieldname] = "$('$fieldname').setStyle('display','');";
      $value = ($contact->$fieldname)? $contact->$fieldname : '';
      $field = '<input size="'.$metadata['size'].' "type="text" title="'.$GLOBALS['l_'.$fieldname].'" name="'.$fieldname.'" value="'.$value.'" />';
      break;      
    }
    if(!empty($contact->$fieldname)) {
      $layout .= $metadata['prepend'].'<fieldset id="'.$fieldname.'" class="'.$class.'"><legend>'.$GLOBALS['l_'.$fieldname].'</legend>'.$field.'</fieldset>';
    } elseif($metadata['mandatory']) {
      $layout .= $metadata['prepend'].'<fieldset id="'.$fieldname.'" class="'.$class.'"><legend>'.$GLOBALS['l_'.$fieldname].'</legend>'.$field.'</fieldset>';
    } else {
      $addable = true;
      $layout .= $metadata['prepend'].'<fieldset id="'.$fieldname.'" class="'.$class.'" style="display:none;"><legend>'.$GLOBALS['l_'.$fieldname].'</legend>'.$field.'</fieldset>';
    }
    if($addable )$add = array_merge($add, $addJs); 
  }

  //FIXME
  //if(OBM_Acl::canWrite($obm['uid'], 'contact', $contact['id'])) {

  //}


  foreach($add as $label => $options) {
    if(is_array($options)) {
      $addBar .= '<li>'.$label.'<ul>';
      foreach($options as $sublabel => $suboptions) {
        $addBar .= '<li><a href="" onclick="'.$suboptions.'; this.getParent().dispose(); return false;">'.$sublabel.'</a></li>';
      }
      $addBar .='</ul></li>';
    } else {
      $addBar .= '<li><a href="" onclick="'.$options.'; this.getParent().dispose(); return false;">'.$label.'</a></li>';
    } 
  }
    $toolbar = "
      <ul id='toolbarMenu' class='dropDownMenu'>
        <li><a href='' onclick='obm.contact.addressbook.updateContact($contact->id, $contact->addressbook_id); return false;' >
            <img src='".$GLOBALS['icones']['edit']."' alt='$GLOBALS[l_header_update]' title='$GLOBALS[l_header_update]' /></a></li>
        <li><a href='' onclick='obm.contact.addressbook.deleteContact($contact->id, \"".$contact->name."\", $contact->addressbook_id); return false;' >
            <img src='".$GLOBALS['icones']['delete']."' alt='$GLOBALS[l_header_delete]' title='$GLOBALS[l_header_delete]' /></a></li>
        <li><a href=''><img src='".$GLOBALS['icones']['add']."' title='$GLOBALS[l_add_fields]' alt='$GLOBALS[l_add_fields]' /></a><ul>$addBar</ul></li>
      </ul>
      <script type='text/javascript'>
        new Obm.DropDownMenu($('toolbarMenu'));
        new Obm.DropDownMenu($('addressMenu'));
        new Obm.DropDownMenu($('phoneMenu'));
        new Obm.DropDownMenu($('emailMenu'));
        new Obm.DropDownMenu($('imMenu'));
        new Obm.DropDownMenu($('websiteMenu'));
      </script>
      ";  
    $block = "
    <table>
    <thead>
    <tr><th><a href='' onclick='obm.contact.addressbook.hideContact(); return false;'><img class='RF' src='".$GLOBALS['icones']['close']."' alt='$GLOBALS[l_close]' /></a>Contact</th></tr>
    <tr><td  class='toolbar'>$toolbar</td></tr>
    </thead>
    <tbody>
    <tr><td>
    <form id='contactForm' name='contactForm' action='#' method='post' onsubmit=\"obm.contact.addressbook.storeContact($(this), '$contact->id'); return false;\">
      <img id='photo' src='$GLOBALS[ico_nophoto]' />
      $layout
      <p class='LC C'>
      <br />
      <input type='hidden' name='action' value='storeContact'  />
      <input type='hidden' name='id' value='$contact->id'  />
      <input type='hidden' name='addressbook' value='$contact->addressbook_id' />
      <input style='margin:auto;' type='submit' value='$GLOBALS[l_save]' />
      <input style='margin:auto;' type='button' value='$GLOBALS[l_cancel]' onclick=\"obm.contact.addressbook.consultContact('$contact->id');\" />
      </p>
    </form> 
    </td>
    </tr>
    </tbody>
    </table>         
  ";
  return $block;      
}



/**
 * Display: XHTML Contact search Form
 *
 * @param $contact[] : default form values
 * @param $usr_q     : database object with userobm list
 * @param $funcs     : array with contact function list
 * @param $ctry_q    : database object with country list
 * @param $dsrc_q    : database object with datasource list
 **/
function html_contact_search_form($contact, $usr_q, $funcs, $ctry_q, $dsrc_q) {
  global $action, $cgp_hide, $l_find, $popup, $display, $c_all, $l_all;
  global $csearch_advanced_default, $l_fuzzy, $l_date, $l_timeupdate;
  global $l_title, $l_market, $l_town, $l_postcode, $l_country, $l_datasource;
  global $l_after, $l_before, $l_mailing_ok_only, $l_newsletter, $l_archive;
  global $l_contact_name, $l_firstname, $l_phone, $l_from_company, $l_email, $l_commonname;
  global $l_visibility;

  $addressbook_id = $contact['addressbook_id'];
  $lastname = stripslashes($contact['lastname']);
  $firstname = stripslashes($contact['firstname']); 
  $commonname = stripslashes($contact['commonname']);
  $phone = stripslashes($contact['phone']);
  $company = stripslashes($contact['company']);
  $email = stripslashes($contact['email']);
  $zip = stripslashes($contact['zip']);
  $town = stripslashes($contact['town']);
  $ctry = $contact['country'];
  $dsrc = $contact['datasource'];
  $func = $contact['function'];
  $title = $contact['title'];
  $mark = $contact['market'];
  $date_field = ($contact['date_field'] ? $contact['date_field'] : 'timeupdate');
  $dateafter = of_date_upd_format(stripslashes($contact['date_after']),true);
  $datebefore = of_date_upd_format(stripslashes($contact['date_before']),true);
  $mailing_ok_only = ($contact['mailing_ok_only'] == '1' ? "checked = \"checked\"" : '');
  $newsletter = ($contact['newsletter'] == '1' ? "checked = \"checked\"" : '');
  $archive = ($contact['archive'] == '1' ? "checked = \"checked\"" : '');
  if (($contact['fuzzy'] == '1') ||
      ($csearch_advanced_default && $action != 'search') ) {
    $fuzzy = "checked = \"checked\"";
  } else {
    $fuzzy = '';
  }
  $company_id = $contact['company_id'];
  $expanded = $contact['expanded'];
  $searchMore_expanded = ($expanded ? 'inline' : 'none');
  $expand_link = ($expanded ? $GLOBALS['l_less_fields'] : $GLOBALS['l_more_fields']);

  $show_func = (! $cgp_hide['contact']['function']);
  $show_title = (! $cgp_hide['contact']['contact_title']);
  $show_resp = (! $cgp_hide['contact']['responsible']);
  $show_date = (! $cgp_hide['contact']['contact_date']);
  $show_mailing = (! $cgp_hide['contact']['contact_mailing']);

  // User defined data
  $block_userdata .= of_userdata_dis_search('contact', $contact);

  // Selected date field
  $s_date_field[$date_field] = "selected=\"selected\"";
  if ($show_date) {
    $sel_date_date = "<option value=\"date\" $s_date_field[date]>$l_date</option>";
  }
  
  // Function select
  $block_function = of_category_dis_search_select('contact', 'function', $funcs, $func);

  if($dsrc_q->nf() != 0) {
    // Data source select
    $sel_dsrc = "<select name=\"sel_datasource\" id=\"sel_datasource\">
      <option value=\"$c_all\">$l_all</option>";
    while ($dsrc_q->next_record()) {
      $d_id = $dsrc_q->f('datasource_id');
      $sel_dsrc .= "\n<option value=\"$d_id\"";
      if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
      $sel_dsrc .= '>'. $dsrc_q->f('datasource_name') . "</option>\n";
    }
    $sel_dsrc .= '</select>';
    $block_datasource = "
      <label>$l_datasource<br />
        $sel_dsrc
      </label>";
  }

  // Title Block
  if ($show_title) {
    $block_title = "
    <label>$l_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"16\"
      value=\"$title\" />
    </label>";
  }

  // Marketing manager select
  if ($show_resp) {
    $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
      <option value=\"$c_all\">$l_all</option>";
    while ($usr_q->next_record()) {
      $u_id = $usr_q->f('userobm_id');
      $u_name = $usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname');
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
      $sel_market .= ">$u_name</option>";
    }
    $sel_market .= '</select>';

    $block_resp = "
    <label>$l_market<br />
      $sel_market
    </label>";
  }

  // Country select
  $sel_ctry = "<select id=\"sel_country\" name=\"sel_country\">
    <option value=\"$c_all\">$l_all</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f('country_iso3166');
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= '>'. $ctry_q->f('country_name') . "</option>\n";
  }
  $sel_ctry .= '</select>';

  if($show_mailing) {
    $block_mailing = "<label>$l_mailing_ok_only<br />
        <input type=\"checkbox\" name=\"cba_mailing_ok_only\" value=\"1\" $mailing_ok_only />
      </label>
      <label>$l_newsletter<br />
        <input type=\"checkbox\" name=\"cba_newsletter\" value=\"1\" $newsletter />
      </label>";
  }
  if ($popup) {
    $ext_action = $contact['ext_action'];
    $ext_type = $contact['ext_type'];
    $ext_url = $contact['ext_url'];
    $ext_id = $contact['ext_id'];
    $ext_title = $contact['ext_title'];
    $ext_target = $contact['ext_target'];
    $ext_widget = $contact['ext_widget'];
    $ext_element = $user['ext_element'];
    $ext_widget_text = $contact['ext_widget_text'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_type\" type=\"hidden\" value=\"$ext_type\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
	    <input name=\"ext_element\" type=\"hidden\" value=\"$ext_element\" />
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display['title'] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" 
  action=\"contact_index.php\">
    <label>$l_contact_name<br />
      <input type=\"text\" name=\"tf_lastname\" size=\"12\" value=\"$lastname\" />
    </label>
    <label>$l_firstname<br />
      <input type=\"text\" name=\"tf_firstname\" size=\"12\" value=\"$firstname\" />
    </label>
    <label>$l_commonname<br />
      <input type=\"text\" name=\"tf_commonname\" size=\"12\" value=\"$commonname\" />
    </label>

    <label>$l_from_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
    </label>
    <label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" value=\"$email\" />
    </label>      
    <div id='searchMore' style=\"display:$searchMore_expanded\">
    <label>$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"12\" maxlength=\"24\" value=\"$phone\" />
    </label>      
    <label>$l_town<br />
      <input type=\"text\" name=\"tf_town\" id=\"tf_town\" size=\"24\"
      value=\"$town\" />
    </label>
    <label>$l_postcode<br />
      <input type=\"text\" name=\"tf_zip\" id=\"tf_zip\" size=\"6\"
      value=\"$zip\" />
    </label>
    <label>$l_country<br />
      $sel_ctry
      </label>
    $block_datasource
    $block_function
    $block_title
    $block_resp
    <label>$l_date<br />
      <select name=\"sel_date_field\">
        <option value=\"timeupdate\" $s_date_field[timeupdate]>$l_timeupdate</option>
        $sel_date_date
      </select>
    </label>
    <label>$l_after<br />
      <input type=\"text\" name=\"tf_date_after\" class=\"datePicker\" value=\"$dateafter\" />
    </label>
    <label>$l_before<br />
      <input type=\"text\" name=\"tf_date_before\" class=\"datePicker\" value=\"$datebefore\" />
    </label>
    $block_mailing
    $block_userdata
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>$l_fuzzy<br />
      <input type=\"checkbox\" name=\"cba_fuzzy\" value=\"1\" $fuzzy />
    </label>
    </div>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"hd_company_id\" type=\"hidden\" value=\"$company_id\" />
      <input id=\"hd_expanded\" name=\"hd_expanded\" type=\"hidden\" value=\"$expanded\" />
      <input name=\"hd_addressbook_id\" type=\"hidden\" value=\"$addressbook_id\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext &nbsp;
    </label>
    <label>
    <br />
    <a href='#' onclick=\"if($('searchMore').getStyle('display') == 'none') { $('searchMore').setStyle('display',''); $('hd_expanded').value='1'; this.innerHTML = '".phpStringToJsString($GLOBALS['l_less_fields'])."'} else { $('searchMore').setStyle('display','none'); $('hd_expanded').value=''; this.innerHTML = '".phpStringToJsString($GLOBALS['l_more_fields'])."'} return false;\">
    $expand_link 
    </a>
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_contact_search_list($contact) {
  global $display, $l_found, $l_no_found, $obm;

  $popup = $contact['popup'];

  $prefs = get_display_pref($obm['uid'], 'contact');
  $obm_q = run_query_contact_search($contact);
  $nb_contact = $obm_q->num_rows_total();
  if ($nb_contact == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_contact $l_found");
    $block = html_contact_search_list($obm_q, $prefs, $contact, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result
// Parameters : 
//   - $obm_q     : list of contacts to display
//   - $prefs     : the fields which have to be displayed
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_list($obm_q, $prefs, $contact, $popup) {
  global $l_add, $l_close;

  if ($popup) {
    $ext_action = $contact['ext_action'];
    $ext_type = $contact['ext_type'];
    $ext_url = $contact['ext_url'];
    $ext_id = $contact['ext_id'];
    $ext_target = $contact['ext_target'];
    $ext_widget = $contact['ext_widget'];
    $ext_widget_text = $contact['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_type=$ext_type&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text";
  }

  // we urlencode to avoid breakage for space char
  $datasource = $contact['datasource'];
  $archive = $contact['archive'];
  $advanced = $contact['fuzzy'];
  $mailing_ok_only = $contact['mailing_ok_only'];
  $lastname = urlencode(stripslashes($contact['lastname']));
  $firstname = stripslashes($contact['firstname']);
  $commonname = stripslashes($contact['commonname']);
  $phone = urlencode(stripslashes($contact['phone']));
  $email = stripslashes($contact['email']);
  $zip = stripslashes($contact['zip']);
  $town = stripslashes($contact['town']);
  $ctry = $contact['country'];
  $dateafter = stripslashes($contact['date_after']);
  $datebefore = stripslashes($contact['date_before']);
  $company = urlencode(stripslashes($contact['company']));
  $company_id = $contact['company_id'];
  $func = $contact['function'];
  $title = urlencode(stripslashes($contact['title']));
  $mark = $contact['market'];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('contact', $contact);

  $url = url_prepare("contact_index.php?action=search$url_userdata&amp;datasource=$datasource&amp;tf_lastname=$lastname&amp;tf_firstname=$firstname&amp;tf_commonname=$commonname&amp;tf_phone=$phone&amp;tf_email=$email&amp;tf_zip=$zip&amp;tf_town=$town&amp;sel_country=$ctry&amp;tf_company=$company&amp;cba_archive=$archive&amp;cba_fuzzy=$advanced&amp;cba_mailing_ok_only=$mailing_ok_only&amp;cba_newsletter=$newsletter&amp;company_id=$company_id&amp;sel_function=$func&amp;tf_title=$title&amp;sel_market=$mark$url_ext");

  $dis_contact = new OBM_DISPLAY('DATA', $prefs, 'contact');
  if ($popup) {
    $dis_contact->display_link = false;
    if ($contact['ext_type'] == 'multi') {
      $dis_contact->data_cb_text = 'X';
      $dis_contact->data_idfield = 'contact_id';
      $dis_contact->data_cb_name = 'cb_con';
      $dis_contact->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
      $dis_contact->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
    } else {
      if ($ext_url != '') {
	$dis_contact->display_ext = 'get_id_url';
      } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
	$dis_contact->display_ext = 'get_id';
      }
      $display_popup_end = "<a href=\"\" onclick='window.close();'>$l_close</a>";
    }
  }

  $dis_contact->data_set = $obm_q;
  $dis_contact->data_url = $url;
  $dis_contact->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_contact->display('dis_data_contact');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation
// Parameters:
//   - $co_q : contact database result
//   - $funcs     : array with contact function list
///////////////////////////////////////////////////////////////////////////////
function html_contact_header($co_q, $funcs) {
  global $cgp_show, $cgp_hide, $l_yes, $l_no;
  global $display, $path, $action, $ccontact_simple_kind;
  global $l_lastname, $l_firstname, $l_middlename, $l_suffix, $l_aka, $l_kind, $l_commonname;
  global $l_contact, $l_company, $l_title, $l_market, $l_archive;
  global $l_date, $l_no_date, $l_birthday, $l_anniversary;
  global $l_other_infos, $l_manager, $l_assistant, $l_spouse, $l_category;
  global $l_service, $l_mailing_ok, $l_newsletter;
  global $l_download_contact_card, $l_exportation, $l_vcard;
  global $l_sync_contact, $l_desync, $l_sync;

  $c_id = $co_q->f('contact_company_id');
  $c_name = $co_q->f('company_name');
  $id = $co_q->f('contact_id');
  $kind_minilabel = $co_q->f('kind_minilabel');
  if (!$ccontact_simple_kind && !empty($kind_minilabel)) {
    $kind = "[" . $co_q->f('kind_lang') . "] $kind_minilabel";
    $kind_header = $co_q->f('kind_header');
    if ($kind_header != $kind) $kind .= " ($kind_header)";
  } else {
    $kind = $kind_minilabel;
  }
  $function = $co_q->f('function_label');
  $title = $co_q->f('contact_title');
  $market = $co_q->f('market_lname') . ' ' . $co_q->f('market_fname');
  $lname = $co_q->f('contact_lastname');
  $fname = $co_q->f('contact_firstname');  
  $cname = $co_q->f('contact_commonname');
  $mname = $co_q->f('contact_middlename');
  $suffix = $co_q->f('contact_suffix');
  $aka = $co_q->f('contact_aka');
  $service = $co_q->f('contact_service');
  $archive = ($co_q->f('contact_archive') == 1 ? $l_yes : $l_no);
  $manager = $co_q->f('contact_manager');
  $assistant = $co_q->f('contact_assistant');
  $spouse = $co_q->f('contact_spouse');
  $category = $co_q->f('contact_category');
  $mailok = ($co_q->f('contact_mailing_ok') == 1 ? $l_yes : $l_no);
  $newsletter = ($co_q->f('contact_newsletter') == 1 ? $l_yes : $l_no);

  if ($co_q->f('contact_date') > '0000-00-00 00:00:00') {
    $date = new Of_Date($co_q->f('contact_date'), 'GMT');
    $date = $date->getOutputDate();
  } else {
    $date = $l_no_date;
  }

  if ($co_q->f('contact_birthday')) {
    $birthday = new Of_Date($co_q->f('contact_birthday'), 'GMT');
    $birthday = $birthday->getOutputDate();
  } else {
    $birthday = $l_no_date;
  }

  if ($co_q->f('contact_anniversary')) {
    $anniversary = new Of_Date($co_q->f('contact_anniversary'), 'GMT');
    $anniversary = $anniversary->getOutputDate();
  } else {
    $anniversary = $l_no_date;
  }
  
  // Display Aka only if non empty
  if ($aka != '') {
    $dis_aka = "
    <tr> 
      <th>$l_aka</th>
      <td>$aka</td>
    </tr>";
  } else {
    $dis_aka = '';
  }

  // Conditionnal fields
  $show_title = (! $cgp_hide['contact']['contact_title']);
  $show_resp = (! $cgp_hide['contact']['responsible']);
  $show_date = (! $cgp_hide['contact']['contact_date']);
  $show_birthday = (! $cgp_hide['contact']['contact_birthday']);
  $show_anniversary = (! $cgp_hide['contact']['contact_anniversary']);

  $show_mailing = (! $cgp_hide['contact']['contact_mailing']);
  $show_service = (! $cgp_hide['contact']['contact_service']);

  $display['title'] = "$lname $fname";

  // Function
  $block_function = of_category_dis_block_consult('contact', 'function', $funcs, 'mono');

  // Company Display
  if ($cgp_show['module']['company']) {
    $dis_company = "<a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id")."\"> $c_name</a>";
  } else {
    $dis_company = $c_name;
  }

  // Title field
  if ($show_title) {
    $block_title = "
    <tr>
      <th>$l_title</th>
      <td>$title</td>
    </tr>";
  }

  // Responsible field
  if ($show_resp) {
    $block_resp = "
    <tr>
      <th>$l_market</th>
      <td>$market</td>
    </tr>";
  }

  // Date field
  if ($show_date) {
    $block_date = "
    <tr>
      <th>$l_date</th>
      <td>$date</td>
    </tr>";
  }
  
  // Birthday field
  if ($show_birthday) {
    $block_birthday = "
    <tr>
      <th>$l_birthday</th>
      <td>$birthday</td>
    </tr>";
  }
  // Anniversary field
  if ($show_anniversary) {
    $block_anniversary = "
    <tr>
      <th>$l_anniversary</th>
      <td>$anniversary</td>
    </tr>";
  }
  // Service field
  if ($show_service) {
    $block_service = "
    <tr>
      <th>$l_service</th>
      <td>$service</td>
    </tr>";
  }
  if ($show_mailing) {
    $block_mailing = "
    <tr>
      <th>$l_mailing_ok</th>
      <td>$mailok</td>
    </tr>
    <tr>
      <th>$l_newsletter</th>
      <td>$newsletter</td>
    </tr>";
  }  

  // Export buttons
  $vcard_button = "
      <input type=\"hidden\" name=\"action\" value=\"vcard\" />
      <input type=\"hidden\" name=\"contact_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_download_contact_card\" />";

  $is_synched = run_query_is_contact_synched($id);
  if($is_synched) {
    $act = 'desync';
    $button_value = $l_desync;
  } else {
    $act = 'sync';
    $button_value = $l_sync;
  }
  $sync_contact_button = "
      <input type=\"submit\" x-obm-sync=\"$is_synched\" onclick=\"Obm.Contact.syncContact($id, this);\" value=\"$button_value\" />";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"detail infos\">
    <h1>$l_contact</h1>
    
    <table>
    <tr>
      <th>$l_kind</th>
      <td>$kind</td>
    </tr>    
    <tr>
      <th>$l_lastname</th>
      <td>$lname</td>
    </tr>
    <tr>
      <th>$l_firstname</th>
      <td>$fname</td>
    </tr>
    <tr>
      <th>$l_commonname</th>
      <td>$cname</td>
    </tr>
    <tr>
      <th>$l_middlename</th>
      <td>$mname</td>
    </tr>
    <tr>
      <th>$l_suffix</th>
      <td>$suffix</td>
    </tr>
    $dis_aka
    <tr>
      <th>$l_company</th>
      <td>$dis_company</td>
    </tr>
    $block_function
    $block_title
    $block_service
    $block_resp
    </table>
    &nbsp;
    <h1>$l_other_infos</h1>
    <table>
    <tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr>
    <tr>
      <th>$l_manager</th>
      <td>$manager</td>
    </tr>
    <tr>
      <th>$l_assistant</th>
      <td>$assistant</td>
    </tr>
    <tr>
      <th>$l_spouse</th>
      <td>$spouse</td>
    </tr>
    <tr>
      <th>$l_category</th>
      <td>$category</td>
    </tr>
    $block_date
    $block_birthday
    $block_anniversary
    $block_mailing
    </table>

    </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation : main info tab
// Parameters:
//   - $co_q : contact database result
//   - $coords : contact secondary info (addresses, phone numbers, ...)
///////////////////////////////////////////////////////////////////////////////
function html_contact_view_main($co_q, $coords) {
  global $l_phone, $l_address, $l_postcode;
  global $l_service, $l_coord, $l_town, $l_country;
  global $l_expresspostal, $l_comment1, $l_comment2, $l_comment3;
  global $l_email, $l_email_other, $l_mailing_ok, $l_newsletter;
  global $ico_clipboard, $l_copy, $l_yes, $l_no, $ico_mail;
  global $display, $cgp_show, $cgp_hide, $cclipboard_address;
  global $l_company;

  $id = $co_q->f('contact_id');
  $title = $co_q->f('contact_title');
  $c_name = $co_q->f('company_name');
  $kind_minilabel = $co_q->f('kind_minilabel');
  $lname = $co_q->f('contact_lastname');
  $fname = $co_q->f('contact_firstname');
  $com  = beautify_comment(nl2br($co_q->f('contact_comment')));
  $com2 = beautify_comment(nl2br($co_q->f('contact_comment2')));
  $com3 = beautify_comment(nl2br($co_q->f('contact_comment3')));
  
  $newline = "\\r\\n";

  // Conditionnal fields
  $show_comment2 = (! $cgp_hide['contact']['contact_comment2']);
  $show_comment3 = (! $cgp_hide['contact']['contact_comment3']);

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult('contact', $id);

  $copy = addslashes("$kind_minilabel $fname $lname") . $newline;
  if (($title != '')  && (in_array('title', $cclipboard_address))) {
    $copy .= addslashes($title) . $newline;
  }
  if ($c_name != '') {
    $copy .= addslashes($c_name) . $newline;
  }
  if ($service != '') {
    $copy .= addslashes($service) . $newline;
  }
  
  foreach($coords as $kind => $row) {
    $coords = '';
    $GLOBALS["l_${kind}_labels"]['COMPANY'] = $l_company;
    foreach($row as $values) {
      $datas = array();
      foreach($values as $label => $value) {
        if ($label == 'label' || $label == 'protocol') {
          if (isset($GLOBALS["l_${kind}_labels"][str_replace(';', '_', $value)])) {
            $header = $GLOBALS["l_${kind}_labels"][str_replace(';', '_', $value)];
          } else {
            $header = $value;
          }
        } elseif(!empty($value)) {
          $datas[] = nl2br($value);
        }
      }
      //if($kind == 'address') {
      //  $l_address 1
      //  <a href=\"javascript: return false;\" onclick=\"contact_copy_clip('$copy'); return false;\" title=\"$l_copy\" ><img alt=\"$l_copy\" src=\"$ico_clipboard\" /></a>
      //}
      $template='<tr><th>%s</th><td>%s</td></tr>';
      switch ($kind) {
        case 'website' :
          $data = ((strpos($datas[0],'://') === false)?'http://'.$datas[0]:$datas[0]);
          $data = "<a target='_blank' href=\"$data\">$data</a>";
          break;
        case 'email' :
          $data = "<a href=\"mailto:$datas[0]\">$datas[0]</a>";
          break;
        case 'address' :
          $data = implode('<br />', $datas);
          $template = '<tr><th>%s</th><td>%s</td></tr><tr><td colspan="2">&nbsp;</td></tr>';
          break;
        default :
          $data = implode('<br />', $datas);
      }
      if(function_exists('hook_pre_contact_consult_coordinate')) {
        $data = hook_pre_contact_consult_coordinate($datas, $kind);
      }
      $coords .= sprintf($template, $header, $data);
    }
    $block .= "
    <div class=\"detail infos\">
    <h1>".$GLOBALS["l_$kind"]."</h1>
    <table>
    $coords
    </table>
    </div>";
  }
  // --- HTML Template --------------------------------------------------------

  $block .= "
    $block_userdata
    <div class=\"detail extra\">
    <h1>$l_comment1</h1>
    $com
    </div>
    $block_comment2 
    $block_comment3 ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact links menu
// Parameters:
//   - $id : contact id
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_contact_links($id) {
  global $ico_document, $ico_add, $ico_deal,$ico_contract;
  global $ico_list, $ico_publication;
  global $l_module_document, $l_contact;
  global $l_module_deal, $l_module_list, $l_module_contract;
  global $l_document_new,$l_document_add;
  global $l_subscription_new, $l_subscription, $l_subscription_list;
  global $path, $cgp_show, $perm;

  // Deal
  if ($perm->check_module_rights('deal')) {
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;contact_id=$id");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;contact_id=$id&amp;cba_archive=1");
    $deal_active = get_global_linked_deal_nb($id, 'contact1', false, 'contact2');
    $deal_total = get_global_linked_deal_nb($id, 'contact1', true, 'contact2');
    $block_deal = "
  <div  class=\"portlet\" id=\"contactDeal\">
  <h1>$l_module_deal</h1>
  <ul>
    <li><a href=\"$url_deal_list\"><img src=\"$ico_deal\" /></a>
        $l_module_deal (
        <a href=\"$url_deal_list\">$deal_active</a> /
        <a href=\"$url_deal_total\">$deal_total</a> )</li>
  </ul>
  </div>";
  }

  // List
  if ($perm->check_module_rights('list')) {
    $url_list = url_prepare("$path/list/list_index.php?action=search&amp;contact_id=$id");
    $list_nb = get_linked_contact_list_nb($id);
    $block_list = "
  <div  class=\"portlet\" id=\"contactList\">  
  <h1>$l_module_list</h1>
  <ul>
    <li><a href=\"$url_list\"><img src=\"$ico_list\" /></a>
        $l_module_list ( <a href=\"$url_list\">$list_nb</a> )</li>
  </ul>
  </div>";
  }

  // Contract
  if ($perm->check_module_rights('contract')) {
    $contract_active = get_global_linked_contract_nb($id,'contact1',false,'contact2');
    $contract_total = get_global_linked_contract_nb($id,'contact1',true,'contact2');
    $url_contract = url_prepare("$path/contract/contract_index.php?action=search&amp;contact_id=$id");
    $url_contract_total = url_prepare("$path/contract/contract_index.php?action=search&amp;contact_id=$id&amp;cba_archive=1");
    $block_contract = "
  <div  class=\"portlet\" id=\"contactContract\">  
  <h1>$l_module_contract</h1>
  <ul>
    <li><a href=\"$url_contract\"><img src=\"$ico_contract\" alt=\"\" /></a>
        $l_module_contract (
        <a href=\"$url_contract\">$contract_active</a> /
        <a href=\"$url_contract_total\">$contract_total</a> )</li>
  </ul>
  </div>";
  }

  // Document  
  if ($perm->check_module_rights('document')) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=contact");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=contact");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contact/contact_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contact";
    $nb_document = run_query_global_document_nb ($id, 'contact');
    $block_doc = "
  <div  class=\"portlet\" id=\"contactDocument\">  
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contact'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  // Publication
  if ($perm->check_module_rights('publication')) {
    $nb_subscription = run_query_contact_subscription_nb($id);
    $url_subscription = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;view=publication&amp;contact_id=$id");
    $url = urlencode($url_subscription);
    $url_subscription_new = url_prepare("$path/publication/publication_index.php?action=new_subscription&amp;contact_id=$id&amp;popup=1&amp;ext_url=$url");
    $block_publi = "
  <div  class=\"portlet\" id=\"contactPublication\">  
  <h1>$l_subscription</h1>
  <ul>
    <li><a href=\"$url_subscription\"><img src=\"$ico_publication\" alt=\"\" /></a>
        <a href=\"$url_subscription\">$l_subscription_list ($nb_subscription)</a></li>
    <li><a href=\"javascript: void(0);\"  onclick=\"window.name='$l_contact'; popup('$url_subscription_new',''); return false;\"><img src=\"$ico_add\" alt=\"\" /></a>
	<a href=\"javascript: void(0);\" onclick=\"window.name='$l_contact'; popup('$url_subscription_new',''); return false;\"> $l_subscription_new</a></li>
  </ul>
  </div>";
  }
  
  // Links Template  
  $block = "
  $block_deal
  $block_list
  $block_contract
  $block_doc
  $block_publi
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the contact administration index
///////////////////////////////////////////////////////////////////////////////
function dis_contact_admin_index() {
  global $cgp_hide;

  $kind_q = run_query_contact_kind();
  $block .= html_contact_kind_form($kind_q);

  if (! $cgp_hide['contact']['function']) {
    $funcs = of_category_get_ordered('contact', 'function');
    $block .= of_category_dis_admin_form('function', $funcs);
  }

  // User defined data
  $block .= of_userdata_dis_admin_form('contact');

  // Hook : Post
  if (function_exists('hook_post_dis_contact_admin_index')) {
    $block = hook_post_dis_contact_admin_index($block);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Kind section
// Parameters:
//   - $kind_q : Kind list database object
///////////////////////////////////////////////////////////////////////////////
function html_contact_kind_form($kind_q) {
  global $l_kind_manage, $l_kind_exist, $l_kind_no;
  global $l_kind_checkdelete, $l_kind_update, $l_kind_new, $l_kind_insert;
  global $l_label, $l_lang, $l_default, $l_header,$ico_ok,$ico_delete,$l_c_new;

  $char_title = '=';
  $key = 0;
  while ($kind_q->next_record()) {
    $id = $kind_q->f('kind_id');
    $lang = $kind_q->f('kind_lang');
    $default = ($kind_q->f('kind_default'))== 1 ? "checked=\"checked\"" : '';
    $mlabel = $kind_q->f('kind_minilabel');
    $header = $kind_q->f('kind_header');
   
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $key ++;
    $c_label = htmlentities($one_cat['label'], ENT_COMPAT, 'UTF-8');
    $kind_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"kind_$id\" value=\"$id\" />
    <a href=\"?action=kind_checklink&amp;category=kind&amp;kind=$id\">
    <img src=\"$ico_delete\" alt=\"$l_kind_checkdelete\" title=\"$l_kind_checkdelete\" />
    </a></td>
    <td><input type=\"checkbox\" id=\"cba_kind_default_$id\" value=\"1\" $default /></td>
    <td><input type=\"text\" id=\"tf_kind_lang_$id\" value=\"$lang\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_kind_label_$id\" value=\"$mlabel\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_kind_header_$id\" value=\"$header\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'kind');\">
    <img src=\"$ico_ok\" alt=\"$l_kind_update\" title=\"$l_kind_update\" />
    </a>
    </td>
    </tr>";
  }


 // --- HTML Template --------------------------------------------------------

  $block = "

 <div class=\"detail admin\" >
  <form id=\"form_kind_update\" action=\"?action=kind_update\" method=\"post\">
  <input type=\"hidden\" name=\"cba_kind_default\" value=\"\" />
  <input type=\"hidden\" name=\"tf_kind_lang\" value=\"\" />
  <input type=\"hidden\" name=\"tf_kind_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_kind_header\" value=\"\" />
  <input type=\"hidden\" name=\"kind\" value=\"\" />
  </form>    
  <h1>$l_kind_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_default</td>
  <td>$l_lang</td>
  <td>$l_label</td>
  <td>$l_header</td>
  <td>&nbsp;</td>  
  </tr>    
  </thead>
  <tbody>
  $kind_list
  </tbody>
  </table>
  </div>  

  <form id=\"form_kind_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_contact_kind_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"4\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_default</td>
  <td>$l_lang</td>
  <td>$l_label</td>    
  <td>$l_header</td>
  </tr>
  <tr>
  <td><input type=\"checkbox\" name=\"cba_kind_default\" value=\"1\" /></td>
  <td><input type=\"text\" name=\"tf_kind_lang\" value=\"\" size=\"6\" /></td>
  <td><input type=\"text\" name=\"tf_kind_label\" value=\"\" size=\"6\" /></td>
  <td><input type=\"text\" name=\"tf_kind_header\" value=\"\" /></td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"4\"><input type=\"submit\" value=\"$l_kind_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"kind_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"kind\" />
  </form>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Kind links
// Parameters:
//   - $ref : kind hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_contact_kind_links($ref) {
  global $path, $display;
  global $l_back, $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_link_contact, $l_kind_link_contact_no;
  global $l_kind_delete, $l_kind_can_delete, $l_kind_cant_delete;

  $delete_ok = true;
  $id = $ref['kind'];
  $name = get_contact_kind_label($id);

  // Contacts Links
  $obm_q = run_query_contact_kind_links($id);
  $nb_kind = $obm_q->num_rows();
  if ($nb_kind > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1> $l_kind_link_contact :$name ($nb_kind)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f('contact_id');
      $cname = $obm_q->f('contact_lastname').' '.$obm_q->f('contact_firstname');
      $dis_link .= "<li><a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;contact_id=$cid") . "\">$cname</a></li>";
    }
    if ($cpt < $nb_kind) {
      $dis_link .= "<li>...</li>";
    }

  } else {
    $dis_link_head = "<h1> $l_kind_link_contact_no $name</h1>";
  }

  $block = "<div class=\"detail\">
             $dis_link_head
            <ul>
             $dis_link
            </ul>
            </div>";

  $dis_back = "<a href=\"$path/contact/contact_index.php?action=admin\">$l_back</a>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_kind_can_delete);
    $dis_del = "<a href=\"$path/contact/contact_index.php?action=kind_delete&amp;sel_kind=$id\" >$l_kind_delete</a>";
    $block .= "
      <div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_kind_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact rights admin form
// Parameters:
//   - $id : contact id
///////////////////////////////////////////////////////////////////////////////
function dis_contact_right_dis_admin($id) {
  global $display, $l_contact;

  $contact_q = run_query_contact_detail($id);
  $name = $contact_q->f('contact_firstname').' '.$contact_q->f('contact_lastname');
  $display['title'] = $name;
  $block = of_right_dis_admin('contact', $id);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_contact_display_pref($prefs) {
  global $l_contact_display;
 
  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'contact');
  $dis_pref->pref_title = $l_contact_display;
  $dis_pref->pref_dis_help = 1;

  // --- HTML Template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the VCard import form
///////////////////////////////////////////////////////////////////////////////
function dis_vcard_import_form($addressbook) {
  global $l_header_import, $l_vcf_file;
  // --- HTML Template --------------------------------------------------------
  $dis_button = "
    <input type=\"hidden\" name=\"action\" value=\"vcard_insert\">
    <input type=\"hidden\" name=\"addressbook\" value=\"$addressbook\">
    <input type=\"submit\" value=\"$l_header_import\">
    <input type=\"button\" value=\"$GLOBALS[l_cancel]\" onclick=\"window.location.href='$GLOBALS[path]/contact/contact_index.php'\"/>
    ";

  $block = "
    <form method=\"post\" name=\"vcard_import\" enctype=\"multipart/form-data\" action=\"contact_index.php\">
    <fieldset class=\"detail extra\">
      <legend></legend>
      <table class=\"detail\">
      <tr>
        <th class=\"detailLabel\">$l_vcf_file</th>
        <td><input name=\"vcard_file\" type=\"file\" value=\"\"/></td>
      </tr>
      </table>
    </fieldset>
    <fieldset class=\"buttons\">
      $dis_button
    </fieldset>
    </form>
    <p class=\"LC\" />" ;

  return $block ;
}


///////////////////////////////////////////////////////////////////////////////
// Display Vcard Export
// Parameters:
//   - $contact[] : contact id
//     keys used  : contact_id
///////////////////////////////////////////////////////////////////////////////
function dis_contact_vcard_export($contact) {
  global $display, $path, $l_err_reference;

  $vcard = OBM_Contact::get($contact['contact_id'])->toVcard();
  
  header("Content-Type: text/x-vCard");
  header("Content-Disposition: inline; filename=".str_replace(' ','_',$vcard->name->family).',_'.str_replace(' ','_',$vcard->name->given).'.vcf');
  header('Cache-Control: maxage=3600'); 
  header('Pragma: public');  
  header('charset=utf-8');
  echo $vcard->__toString();
}


///////////////////////////////////////////////////////////////////////////////
// Display Export all private contacts to VCards
///////////////////////////////////////////////////////////////////////////////
function dis_contact_vcard_export_all($contacts) {
  
  $vcards = array();
	if (count($contacts)>0) {
    foreach ($contacts as $c) {
      $vcards[] = $c->toVcard()->__toString();
    }
  }
  header("Content-Type: text/x-vCard");
  header("Content-Disposition: inline; filename=contacts.vcf");
  header('Cache-Control: maxage=3600'); 
  header('Pragma: public');  
  header('charset=utf-8');
  echo implode("\n", $vcards);
}

///////////////////////////////////////////////////////////////////////////////
// Display Export all contacts to CSV
///////////////////////////////////////////////////////////////////////////////
function dis_contact_csv_export_all($contacts) {
  
  $csv = array();
  $f = array();
  $fields = array_keys(get_display_pref($GLOBALS['obm']['uid'], 'contact'));
  $f[] = $GLOBALS['fieldnames']["displayname"];
  foreach($fields as $field) {
    $f[] = $GLOBALS['fieldnames'][$field];
  }
  
  if (count($contacts)>0) {
    foreach ($contacts as $c) {
      $csv[] = $c->toCsv();
    }
  }
  
  header("Content-Type: text/csv");
  header("Content-Disposition: inline; filename=contacts.csv");
  header('Cache-Control: maxage=3600'); 
  header('Pragma: public');  
  header('charset=utf-8');
  echo implode(";", $f);
  echo "\n";
  echo implode("\n", $csv);
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About Contact number evolution by date
// Parameters:
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_html_contact_date_graph_stats($entity) {
  global $path, $l_contact_date_evolution_graph;

  $url = url_prepare("$path/statistic/statistic_index.php?action=contact_date_evolution_graph");

  $block = "<div class=\"detail extra\">
   <h1>".ucfirst($l_contact_date_evolution_graph)."</h1>
       <img src=\"$url\" />
   </div>";
 
  return $block; 
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About Contact categories 
// Parameters:
//   - $params[] : parameters hash
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_category_contact_stats($params) {
  global $cgp_user, $display, $l_stats, $l_function;
  global $l_contact_date_evolution_graph;

  $order = $params['order'];
  $stat = $params['stat'];

  if (($stat == '') || ($stat == 'index')) {

  } elseif ($stat == 'function') {
    $cats = of_category_query_category_per_entity('contact','function','mono');
    $block_stat = of_category_dis_entity_stats('contact', $stat, $cats,$order);
  } elseif ($stat == 'contact_date_evolution') {
    $block_stat = dis_html_contact_date_graph_stats('contact'); 
  } else {
    $cats = of_category_user_get_category_distribution('contact', $stat);
    $block_stat = of_category_dis_entity_stats('contact', $stat, $cats,$order);
  }

  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      global ${"l_$cat_name"};
      $l_cat = ${"l_$cat_name"};
      $block_contact .= "<li><a href=\"?action=statistics&amp;stat=$cat_name\">$l_cat</a></li>";
    }
  }


  $block = "
    <div class=\"detail extra\">
    <ul>
    <li><a href=\"?action=statistics&amp;stat=function\">$l_function</a></li>
    <li><a href=\"?action=statistics&amp;stat=contact_date_evolution\">$l_contact_date_evolution_graph;</a></li>
    $block_contact
    </ul>
    </div>
    $block_stat
";

  return $block;
}
/**
 * Build a JSON array with all search results 
 * 
 * @param $results array of 'length' => DB contact results nb, and 'datas' => DB User search query results
 * @access public
 * @return void
 */
function json_search_contact($contact ,$results) {
  global $display;
  $contacts = array();
  if (is_array($results)) {
    foreach($results as $c) {
      $contacts[] = "{id:'data-contact-$c->id', label : '".phpStringToJsString($c->displayname)."', extra: '".phpStringToJsString($c->email_address)."'}";
    }
  }
  $display['json'] = "{length:".sizeof($results).", datas:[".implode(',',$contacts)."]}";
}

function json_get_kind($results) {
  global $display;
  $kinds = array();
  if (is_array($results)) {
    foreach($results as $kind_id=>$kind_minilabel) {
      $kinds[] = "{id:'$kind_id', label : '".phpStringToJsString($kind_minilabel)."', extra:''}";
    }
  }
  $display['json'] = "{length:".sizeof($results).", datas:[".implode(',',$kinds)."]}";
}


function json_sync_contact($err, $act) {
  global $display, $l_sync, $l_desync;
  global $l_synchronisation, $l_update_ok, $l_update_error;

  if($act == 'sync') {
    $newact = 'desync';
  } else {
    $newact = 'sync';
  }
  if($err) {
    $msg = "$l_synchronisation : $l_update_error";
  } else {
    $msg = "$l_synchronisation : $l_update_ok";
  }

  $button = phpStringToJsString($button);
  $msg = phpStringToJsString($msg);

  $info[] = "error : ".(int)$err;
  $info[] = "message : '$msg'";
  $info[] = "newact : '$newact'";
  $display['json'] = implode(',',$info);
}

function dis_addressbook_right_dis_admin($addressbook) {
  global $display;

  $display['title'] = $name;

  $ad = OBM_AddressBook::get($addressbook['entity_id']);
  $display['title'] = $ad->displayname;

  $block = of_right_dis_admin('addressbook', $addressbook['entity_id']);
  return $block;
}


function dis_update_addressbook_count($addressbooks, $pattern, $id='') {

  $count = $addressbooks->countContacts($pattern);
  $js =  "<script language=\"text/javascript\"> $('count_addressbook_$id').innerHTML = '$count' </script>";
  return $js;
}
?>
