<?php



class GroupContactUpdate extends UpdateObject {

  private $addressbooks;

  public function main() {
    $query = "DELETE FROM contactgroup";
    $result = $this->query($query);
    $result->free();
    $this->addressbooks = $this->getAddressBooks();
    $query = "SELECT group_id, group_contacts, group_domain_id FROM UGroup where group_contacts != '' AND group_contacts IS NOT NULL";
    $result = $this->query($query);
    $groups = array();
    echo $result->nf()." groups with external contact.\n";
    while($result->next_record()) {
      $mails = $result->f('group_contacts');
      $mails = explode("\r\n",$mails);
      $gid = $result->f('group_id');
      echo "- Parsing emails from group $gid \n";
      $domain = $result->f('group_domain_id');
      foreach($mails as $mail) {
        $mail = trim($mail);
        if($mail) {
          $contactId = $this->getContactId($mail, $domain);
          if($contactId > 0) {
            $groups[$gid][] = $contactId;
          }
        }
      }
    }
    $result->free();
    foreach($groups as $gid => $group) {
      $this->storeGroupContacts($gid, $group);
    }
    $query = "ALTER TABLE UGroup DROP COLUMN group_contacts";
    $result = $this->query($query);
    $result->free();
  }

  private function getAddressBooks() {
    $query = "SELECT AddressBook.id, AddressBook.domain_id FROM AddressBook WHERE AddressBook.name = 'public_contacts' AND AddressBook.is_default = TRUE";
    $result = $this->query($query);
    $addressbooks = array();
    while($result->next_record()) {
      $addressbooks[$result->f('domain_id')] = $result->f('id');
    }
    return $addressbooks;
  }

  private function getContactId($mail, $domain) {
    $query = "SELECT contact_id FROM Contact 
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id 
      INNER JOIN Email ON contactentity_entity_id = email_entity_id
      WHERE email_address = '$mail' AND contact_addressbook_id = '".$this->addressbooks[$domain]."'
      LIMIT 1 OFFSET 0";
    $result = $this->query($query);
    if($result->next_record()) {
      $id = $result->f('contact_id');
      echo "-- Contact $mail founded in public address book with id : $id \n";
      $result->free();
      return $id;
    } else {
      $result->free();
      return $this->storeContact($mail, $domain);
    }
  }

  private function storeContact($mail, $domain) {
    list( $firstname, $lastname ) = $this->getNameFromMail($mail);
    $query = "
     INSERT INTO Contact (
      contact_domain_id, 
      contact_timecreate, 
      contact_timeupdate, 
      contact_addressbook_id, 
      contact_lastname, 
      contact_firstname,
      contact_collected, 
      contact_origin
     ) VALUES (
      '$domain',
      NOW(),
      NOW(),
      ".$this->addressbooks[$domain].",
      '$lastname',
      '$firstname',
      TRUE,
      'obm-storage-migration-2.4'
    )";
    $result = $this->query($query);
    $id = $result->lastId();
    $result->free();
    $query = "INSERT INTO Entity (entity_mailing) VALUES (true)";
    $result = $this->query($query);
    $entityId = $result->lastId();
    $result->free();
    $query = "INSERT INTO ContactEntity (contactentity_entity_id, contactentity_contact_id) VALUES ($entityId, $id)";
    $result = $this->query($query);
    $result->free();
    $query = "INSERT INTO Email (email_entity_id,email_label,email_address) VALUES ($entityId, 'INTERNET;X-OBM-Ref1', '$mail')";
    $result = $this->query($query);
    $result->free();
    echo "-- Inserting new contact from mail $mail : $lastname, $firstname with $id \n";
    return $id;
  }

  private function storeGroupContacts($id, $contacts) {
    echo "- Storing ".count($contacts)." from group $id \n";
    if(empty($contacts)) return;
    $query = "
      INSERT INTO contactgroup (contact_id, group_id)
      SELECT contact_id, $id FROM Contact WHERE contact_id IN (".implode(',',$contacts).")";
    $result = $this->query($query);
    $result->free();
    $this->updateGroupHierarchy($id);
  }

  private function updateGroupHierarchy($id) {
    echo "-- Updating hierarchy form $id\n";
    $query = "DELETE FROM _contactgroup WHERE group_id = $id";
    $result = $this->query($query);
    $result->free();
    $query = "
      INSERT INTO _contactgroup (group_id, contact_id) SELECT $id, contact_id FROM contactgroup 
      WHERE group_id IN (SELECT groupgroup_child_id FROM GroupGroup WHERE groupgroup_parent_id = $id)
      OR group_id = $id GROUP BY contact_id";
    $result = $this->query($query);
    $result->free();
    $query = "SELECT groupgroup_parent_id FROM GroupGroup WHERE groupgroup_child_id = $id";
    $result = $this->query($query);
    while($result->next_record()){
      $this->updateGroupHierarchy($result->f('groupgroup_parent_id'));
    }
    $result->free();
  }

   /**
   * 
   * @param mixed $email 
   * @access public
   * @return void
   */
  private function getNameFromMail($email) {
    $first_name = '';
    $last_name  = '';

    if(preg_match('/(.+?)\.(.+?)@/', $email, $match)) {
      $first_name = ucfirst(strtolower($match[1]));
      $last_name  = strtoupper($match[2]);
    } elseif(preg_match('/([^.]+)@/', $email, $match)) {
      $last_name = ucfirst(strtolower($match[1]));
    }

    return array( $first_name, $last_name );
  }
}


class UpdateObject {

  protected static $free;
  protected static $busy;

  protected $database;
  protected $host;
  protected $user;
  protected $password;

  public function __construct() {
    if(!is_array(self::$free)) self::$free = array(); 
    if(!is_array(self::$busy)) self::$busy = array(); 
    $dir = dirname(__FILE__);
    $conf = parse_ini_file($dir.'/../../conf/obm_conf.ini');
    $this->database = $conf['db'];
    $this->host = $conf['host'];
    $this->user = $conf['user'];
    $this->password = $conf['password'];
    if(strtoupper($conf['dbtype']) == 'PGSQL') {
      include_once($dir.'/lib/pgsql.inc');
    } elseif (strtoupper($conf['dbtype']) == 'MYSQL') {
      include_once($dir.'/lib/mysql.inc');
    }
  }

  public function __get($key) {
    return $this->$key;
  }

  protected function query($query) {
    if(count(self::$free) > 0) {
      $con = array_shift(self::$free);
    } else {
      $con = new DB($this); 
    }
    $con->setId(count(self::$busy));
    self::$busy[$con->getId()] = $con;
    $con->query($query);
    return $con;
  }

  public function free($id) {
    $con = self::$busy[$id];
    if(is_object($con)) {
      $con->setId(null);
      unset(self::$busy[$id]);
      self::$free[] = $con;
    }
  }
}

$update = new GroupContactUpdate();
$update->main();
