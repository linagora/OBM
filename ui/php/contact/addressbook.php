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
<?php
require_once 'obminclude/of/of_search.php';

class OBM_AddressBook implements OBM_ISearchable {
  public $id;
  public $name;
  public $owner;
  public $isDefault;
  public $access;
  public $read;
  public $write;
  public $admin;
  public $syncable;
  public $synced;

  public function __construct($id, $name, $is_default, $owner, $syncable, $synced, $access, $read, $write, $admin) {
    $this->id = $id;
    $this->name = $name;
    $this->access = $access;
    $this->read = $write;
    $this->write = $write;
    $this->admin = $admin;
    $this->isDefault = $is_default;
    $this->owner = $owner;
    $this->syncable = $syncable;
    $this->synced = $synced;
    $this->db = new DB_OBM;
  }

  public function __set($property, $value) {
    if ($property == "name") {
      $this->name = $value;
    } else {
      return;
    }
  }

  public function __get($property) {
    if ($property == "name") {
      if ($this->isDefault) {
        return $GLOBALS["l_{$this->name}"];
      } else {
        return $this->name;
      }
    } else {
      return $this->$property;
    }
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
    $pattern .= ' addressbook:'.$this->id;
    return OBM_Contact::search($pattern, $limit, $offset);
  }

  public function addContact($fields) {
    return OBM_Contact::create($fields, $this);
  }

  public static function get($pattern) {
    if(is_numeric($pattern))
      $pattern = 'id:'.$pattern; 
    return self::search($pattern)->current();
  }

  public static function search($searchPattern=null) {
    if($searchPattern !== null) {
      $query = 'AND '.OBM_Search::buildSearchQuery('OBM_AddressBook', $searchPattern);
    }
    $db = new DB_OBM;
    $addressBooks = array();
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
        (SELECT count (*) FROM SyncedAddressbook WHERE addressbook_id=AddressBook.id) AS synced
      FROM AddressBook 
      INNER JOIN ('.OBM_Acl::getAclSubselect($columns, 'addressbook', null, $GLOBALS['obm']['uid'], 'read').') AS Rights ON AddressBook.id = Rights.addressbookentity_addressbook_id
      WHERE 1=1 '.$query.' ORDER BY AddressBook.name');
    while($db->next_record()) {
      $addressBooks[$db->f('id')] = new OBM_AddressBook($db->f('id'), $db->f('name'), $db->f('is_default'), $db->f('owner'), $db->f('syncable'), $db->f('synced'), $db->f('entityright_access'),
                                                        $db->f('entityright_read'), $db->f('entityright_write'),$db->f('entityright_admin'));
    }    
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
        (SELECT count (*) FROM SyncedAddressbook WHERE addressbook_id=AddressBook.id) AS synced
      FROM AddressBook 
      WHERE AddressBook.owner = '.$GLOBALS['obm']['uid']. $query.' ORDER BY AddressBook.name'); 
    while($db->next_record()) {
      $addressBooks[$db->f('id')] = new OBM_AddressBook($db->f('id'), $db->f('name'), $db->f('is_default'), $db->f('owner'), $db->f('syncable'), $db->f('synced'), $db->f('entityright_access'),
                                                        $db->f('entityright_read'), $db->f('entityright_write'),$db->f('entityright_admin'));
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
    if (!$ad->isDefault && $ad->write) {
      $db = new DB_OBM;
      // Delete contacts
      $query = "DELETE FROM Contact WHERE contact_addressbook_id='$id'";
      $db->query($query);
      // Delete addressbook
      $query = "DELETE FROM AddressBook WHERE id='$id' and owner='$uid'";
      $db->query($query);
    }
  }

  public static function store($addressbook) {
    $id = $addressbook['id'];
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
      $query = "UPDATE AddressBook SET $name_q syncable='$syncable' WHERE id='$id'";
      $db = new DB_OBM;
      $db->query($query);
    }
  }

  public static function setSynced($addressbook) {
    $id = $addressbook['id'];
    $ad = self::get($id);
    $uid = $GLOBALS['obm']['uid'];
    if ($ad->synced) {
      // Remove synchronized addressbook
      $query = "DELETE FROM SyncedAddressbook WHERE user_id='$uid' AND addressbook_id='$id'";
    } else {
      // Add synchronized addressbook
      $query = "INSERT INTO SyncedAddressbook VALUES ($uid, $id, NOW())"; 
    }
    $db = new DB_OBM;
    $db->query($query);
  }
}


class OBM_AddressBookArray implements ArrayAccess, Iterator {

  private $addressbooks = array();

  private $offset = 0;

  public function __construct($addressbooks) {
    $this->addressbooks = $addressbooks;
  }

  public function searchContacts($pattern) {
    if(!empty($this->addressbooks)) {
      $pattern .= ' addressbook:('.implode(',',array_keys($this->addressbooks)).')';
      return OBM_Contact::search($pattern);
    }
  }

  public function getMyContacts() {
    foreach($this->addressbooks as $addressbook) {
      if($addressbook->isDefault && $addressbook->name == 'contacts' && $addressbook->owner == $GLOBALS['obm']['uid'])
        return $addressbook;
    }
  }
  
  public function offsetSet($offset, $value) {
    $this->$addressbooks[$offset] = $value;
  }

  public function offsetExists($offset) {
    return isset($this->addressbooks[$offset]);
  }

  public function offsetUnset($offset) {
    unset($this->addressbooks[$offset]);
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
}
