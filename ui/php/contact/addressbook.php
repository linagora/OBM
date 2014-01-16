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



require_once 'obminclude/of/of_search.php';
require_once 'obminclude/of/of_contact.php';

class OBM_AddressBook implements OBM_ISearchable {
  private $id;
  private $name;
  private $displayname;
  private $owner;
  private $isDefault;
  private $access;
  private $read;
  private $write;
  private $admin;
  private $syncable;
  private $synced;
  private $queryFilter;

  public function __construct($id, $name, $is_default, $owner, $syncable, $synced, $access, $read, $write, $admin) {
    $this->id = $id;
    $this->name = $name;
    $this->displayname = $name;
    $this->access = $access;
    $this->read = $read;
    $this->write = $write;
    if($GLOBALS['obm'])
    if((Perm::get_module_rights($entityType) & $GLOBALS['cright_write_admin']) == $GLOBALS['cright_write_admin']) $this->admin = 1;
    else $this->admin = $admin;
    $this->isDefault = $is_default;
    $this->owner = $owner;
    $this->syncable = TRUE;
    if ($this->name == 'public_contacts' && $this->isDefault) $this->syncable = FALSE;
    $this->synced = $synced;
    $this->db = new DB_OBM;
    $this->setQueryFilter();
  }

  public function __set($property, $value) {
    if ($property != 'id') {
      $this->$property = $value;
    }
  }

  public function __get($property) {
    if (property_exists($this, $property)) {
      if ($property == "displayname") {
        if ($this->isDefault) {
          if ($this->owner == $GLOBALS['obm']['uid'] || $this->name == 'public_contacts') {
            return $GLOBALS["l_{$this->name}"];
          } else {
            $owner = get_entity_info($this->owner, 'user');
            return $GLOBALS["l_{$this->name}"]." ($owner[label])";
          }
        } else {
          if ($this->owner == $GLOBALS['obm']['uid'] || $this->name == 'public_contacts') {
            return $this->name;
          } else {
            $owner = get_entity_info($this->owner, 'user');
            return $this->name." ($owner[label])";
          }
        }
      } else {
        return $this->$property;
      }
    }
    return;
  }

  private function setQueryFilter() {
    $this->queryFilter = "addressbookId:(".$this->id.")";
  } 

  public static function fieldsMap() {
    $fields['*'] = array('AddressBook.name' => 'text');
    $fields['name'] = array('AddressBook.name' => 'text');
    $fields['id'] = array('AddressBook.id' => 'integer');
    $fields['owner'] = array('AddressBook.owner' => 'integer');
    $fields['default'] = array('AddressBook.is_default' => 'boolean');

    return $fields;
  }

  public function getContacts($pattern='', $offset=0, $limit=100) {
    if(trim($pattern)) $pattern = "($pattern ".$this->queryFilter.") AND ".$this->queryFilter;
    else $pattern = $this->queryFilter;
    return OBM_Contact::search($pattern, $offset, $limit);
  }

  public function addContact($fields) {
    return OBM_Contact::create($fields, $this);
  }

  public static function get($pattern) {
    return self::search($pattern)->current();
  }
  
  public static function writable() {
    $addressBooks = self::search();
    $writable = array();
    foreach ($addressBooks as $id => $book) {
      if ($book->isWritable()) {
        $writable[$id] = $book;
      }
    }
    return new OBM_AddressBookArray($writable);
  }

 public static function lookupPublicAddressBookIdFor($domain_id) {
  	
  	$db = new DB_OBM;
  	$db->xquery("
  		SELECT id 
  		FROM AddressBook 
  		WHERE name = 'public_contacts'
  		AND domain_id = #INT($domain_id)");
  		
  	$db->next_record();
  	return $db->f('id'); 
  }
  
  public static function search($searchPattern=null) {
    if($searchPattern !== null) {
      $query = " AND AddressBook.id= #INT($searchPattern)";
    }
    $db = new DB_OBM;
    $addressBooks = array();
    $db->xquery('
      SELECT 
        AddressBook.id,
        AddressBook.owner,
        AddressBook.name,
        AddressBook.is_default,
        AddressBook.syncable,
        1 as entityright_access,
        1 as entityright_read,
        1 as entityright_write,
        1 as entityright_admin,
        SyncedAddressbook.user_id as synced
      FROM AddressBook 
      LEFT JOIN SyncedAddressbook ON SyncedAddressbook.addressbook_id = AddressBook.id AND SyncedAddressbook.user_id = '.$GLOBALS['obm']['uid'].'
      WHERE AddressBook.owner = '.$GLOBALS['obm']['uid'].' '.$query.' ORDER BY AddressBook.is_default DESC, AddressBook.name'); 
    while($db->next_record()) {
      $addressBooks[$db->f('id')] = new OBM_AddressBook($db->f('id'), $db->f('name'), $db->f('is_default'), $db->f('owner'), $db->f('syncable'), $db->f('synced'), $db->f('entityright_access'),
                                                        $db->f('entityright_read'), $db->f('entityright_write'),$db->f('entityright_admin'));
    }    
    $columns = array('addressbookentity_addressbook_id', 'entityright_access', 'entityright_read', 'entityright_write', 'entityright_admin');
    $db->xquery('
      SELECT 
        AddressBook.id,
        AddressBook.owner,
        AddressBook.name,
        AddressBook.is_default,
        AddressBook.syncable,
        Rights.entityright_access,
        Rights.entityright_read,
        Rights.entityright_write,
        Rights.entityright_admin,
        SyncedAddressbook.user_id as synced
      FROM AddressBook 
      INNER JOIN ('.OBM_Acl::getAclSubselect($columns, 'addressbook', null, $GLOBALS['obm']['uid']).') AS Rights ON AddressBook.id = Rights.addressbookentity_addressbook_id
      LEFT JOIN SyncedAddressbook ON SyncedAddressbook.addressbook_id = AddressBook.id AND SyncedAddressbook.user_id = '.$GLOBALS['obm']['uid'].'
      WHERE 1=1 '.$query.' AND AddressBook.domain_id = '.$GLOBALS['obm']['domain_id'].' ORDER BY AddressBook.name');
    while($db->next_record()) {
      if($addressBooks[$db->f('id')]) {
        if($db->f('entityright_access') == 1) $addressBooks[$db->f('id')]->access = 1;
        if($db->f('entityright_read') == 1) $addressBooks[$db->f('id')]->read = 1;
        if($db->f('entityright_write') == 1) $addressBooks[$db->f('id')]->write = 1;
        if($db->f('entityright_admin') == 1) $addressBooks[$db->f('id')]->admin = 1;
      } else {
        $addressBooks[$db->f('id')] = new OBM_AddressBook($db->f('id'), $db->f('name'), $db->f('is_default'), $db->f('owner'), $db->f('syncable'), $db->f('synced'), $db->f('entityright_access'),
                                                          $db->f('entityright_read'), $db->f('entityright_write'),$db->f('entityright_admin'));
      }
    }  
    return new OBM_AddressBookArray($addressBooks);
  }

  public static function searchOwnAddressBooks($user_id) {
    $db = new DB_OBM;
    $addressBooks = array();
    $db->xquery("SELECT
	    AddressBook.id,
        AddressBook.owner,
        AddressBook.name,
        AddressBook.is_default,
        AddressBook.syncable,
        1,1,1,1,
        SyncedAddressbook.user_id as synced
        FROM AddressBook
        JOIN SyncedAddressbook ON AddressBook.id = SyncedAddressbook.addressbook_id
        WHERE AddressBook.owner=$user_id");
    while($db->next_record()) {
        $addressBooks[$db->f('id')] = new OBM_AddressBook($db->f('id'), 
        $db->f('name'), $db->f('is_default'), $db->f('owner'), 
        $db->f('syncable'), $db->f('synced'), $db->f('entityright_access'),
        $db->f('entityright_read'), $db->f('entityright_write'),
        $db->f('entityright_admin'));
    }
    return new OBM_AddressBookArray($addressBooks);
  }

  public static function create($addressbook) {

    $domain_id = $GLOBALS['obm']['domain_id'];
    $uid = $GLOBALS['obm']['uid'];
    $ad_name = $addressbook['name'];

    $query = "INSERT INTO AddressBook (
      domain_id,
      timeupdate,
      timecreate,
      userupdate,
      usercreate,
      origin,
      owner,
      name,
      is_default,
      syncable) VALUES (
      $domain_id,
      NOW(),
      NOW(),
      $uid,
      $uid,
      '$GLOBALS[c_origin_web]',
      $uid,
      '$ad_name',
      false,
      true)";
    $db = new DB_OBM;
    $db->query($query);
    $id = $db->lastid();
    $entity_id = of_entity_insert('addressbook', $id);    
    return self::get($id);
  }

  public static function delete($addressbook) {
    $id = $addressbook['addressbook_id'];
    $uid = $GLOBALS['obm']['uid'];
    $ad = self::get($id);
    if (!$ad->isDefault && $ad->admin) {
      $db = new DB_OBM;
      // Delete contacts
      $query = "DELETE FROM Contact WHERE contact_addressbook_id='$id'";
      $db->query($query);
      // Delete addressbook
      $query = "DELETE FROM AddressBook WHERE id='$id'";
      $db->query($query);
      // Delete solr
      OBM_IndexingService::deleteByQuery('contact', "addressbookId:$id");
    }
  }

  public static function timestamp($id) {
    $query = "UPDATE AddressBook SET timeupdate=NOW() WHERE id='$id'";
    $db = new DB_OBM;
    $db->query($query);
  }


  public static function store($addressbook) {
    $id = $addressbook['addressbook_id'];
    $syncable = $addressbook['sync'];
    $name = $addressbook['name'];
    $ad = self::get($id);
    $syncable = $ad->syncable;
    $name = $ad->name;
    if (isset($addressbook['name'])) $name = $addressbook['name'];
    if ($addressbook['action'] == 'setSyncable') {
      $syncable = !$syncable;
    }
    $syncable = $syncable ? 'true':'false';

    if ($ad->write) {
      if (!$ad->isDefault) $name_q =  "name='$name',";
      $query = "UPDATE AddressBook SET $name_q syncable=$syncable WHERE id='$id'";
      $db = new DB_OBM;
      $db->query($query);
    }
  }

  public static function setSynced($addressbook) {
    $id = $addressbook['addressbook_id'];
    $ad = self::get($id);
    $uid = $GLOBALS['obm']['uid'];
    $db = new DB_OBM;
    if(!$ad->syncable) return true;
    if ($ad->synced) {
      // Remove synchronized addressbook
      $db->query("DELETE FROM SyncedAddressbook WHERE user_id='$uid' AND addressbook_id='$id'");
      $db->query("INSERT INTO DeletedSyncedAddressbook VALUES ($uid, $id, NOW())"); 
    } else {
      // Add synchronized addressbook
      $db->query("INSERT INTO SyncedAddressbook VALUES ($uid, $id, NOW())");
      $db->query("DELETE FROM DeletedSyncedAddressbook WHERE user_id='$uid' AND addressbook_id='$id'");
    }
  }

  public function __toString() {
    return $this->__get('displayname');
  }

  public function countContacts($pattern='') {
    if(trim($pattern)) $pattern = "$pattern ";
    else $pattern = '-is:archive';
    return OBM_Search::count('contact', $pattern);
  }

  public function toVcard($pattern='', $offset=0) {
    $vcards = array();
    $pattern .= ' -is:archive addressbookId:('.$this->id.')';
    $contacts = OBM_Contact::search($pattern, $offset);
    if (is_array($contacts)) {
      foreach ($contacts as $c) {
        $vcards[] = $c->toVcard()->__toString();
      }
    }
    return implode("\n", $vcards);
  }

  public function reset() {
    if ($this->name!='public_contacts' && $this->admin) {
      $db = new DB_OBM;
      // Delete contacts
      $query = "DELETE FROM Contact WHERE contact_addressbook_id='$this->id'";
      $db->query($query);
      // Delete solr
      OBM_IndexingService::deleteByQuery('contact', "addressbookId:$this->id");
    }
  }
  
  public function soft_reset() {
    if ($this->name!='public_contacts' && $this->admin) {
      $db = new DB_OBM;
      $query = "INSERT INTO DeletedContact(deletedcontact_contact_id, deletedcontact_addressbook_id, deletedcontact_origin)
                SELECT contact_id, contact_addressbook_id, contact_origin
                FROM Contact LEFT JOIN DeletedContact ON contact_id = deletedcontact_contact_id
                WHERE contact_addressbook_id='$this->id' AND deletedcontact_contact_id IS NULL";
      $db->query($query);
      $this->reset();
    }
  }


  public function isWritable() {
    return $this->write == 1;
  }
}


class OBM_AddressBookArray implements ArrayAccess, Iterator {

  private $addressbooks = array();

  private $offset = 0;

  private $queryFilter;

  public function __construct($addressbooks) {
    $this->addressbooks = $addressbooks;
    $this->setQueryFilter();
  }

  private function setQueryFilter() {
    $this->queryFilter = "addressbookId:(".implode(" OR ",array_keys($this->addressbooks)).")";
  } 

  public function exportContacts($pattern, $offset=0, $limit=100000) {
    return $this->searchContacts($pattern, $offset, $limit);
  }

  public function searchContacts($pattern, $offset=0, $limit=100) {
    if(!empty($this->addressbooks)) {
      if(trim($pattern)) $pattern = "($pattern ".$this->queryFilter.") AND ".$this->queryFilter;
      else $pattern = $this->queryFilter;
      return OBM_Contact::search($pattern, $offset, $limit);
    }
  }

  public function countContacts($pattern) {
    if(!empty($this->addressbooks)) {
      if(trim($pattern)) $pattern = "($pattern ".$this->queryFilter.") AND ".$this->queryFilter;
      else $pattern = $this->queryFilter;
      return OBM_Contact::count($pattern, $offset, $limit);
    }
  }

  public function getMyContacts() {
    foreach($this->addressbooks as $addressbook) {
      if($addressbook->isDefault && $addressbook->name == 'contacts' && $addressbook->owner == $GLOBALS['obm']['uid'])
        return $addressbook;
    }
  }

  public function getCollectedAddressbook() {
    foreach($this->addressbooks as $addressbook) {
      if($addressbook->isDefault && $addressbook->name == 'collected_contacts' && $addressbook->owner == $GLOBALS['obm']['uid'])
        return $addressbook;
    }
  }

  public function getPublicAddressbook() {
    foreach($this->addressbooks as $addressbook) {
      if($addressbook->isDefault && $addressbook->name == 'public_contacts')
        return $addressbook;
    }
  }

  public function offsetSet($offset, $value) {
    $this->addressbooks[$offset] = $value;
    $this->setQueryFilter();
  }

  public function offsetExists($offset) {
    return isset($this->addressbooks[$offset]);
  }

  public function offsetUnset($offset) {
    unset($this->addressbooks[$offset]);
    $this->setQueryFilter();
  }

  public function offsetGet($offset) {
    return isset($this->addressbooks[$offset]) ? $this->addressbooks[$offset] : null;
  }

  public function rewind() {
    reset($this->addressbooks);
  }

  public function current() {
    return current($this->addressbooks);
  }

  public function key() {
    return key($this->addressbooks);
  }

  public function next() {
    next($this->addressbooks);
  }

  public function valid() {
   return current($this->addressbooks); 
  }

  public function getAddressbooks($right=NULL) {
    if($right === NULL) {
      return $this->addressbooks;
    } else {
      $addressbooks = array();
      foreach($this->addressbooks as $addressbook) {
        if($addressbook->$right == 1)
          $addressbooks[$addressbook->id] = $addressbook;
      }      
      return $addressbooks;
    }
  }
}
