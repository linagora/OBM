<?php

class obm_addressbook_backend extends rcube_addressbook {
  public $database_name;
  public $primary_key = 'ID';
  public $readonly = true;
  public $groups = false;
  public $obmuid;
  public $name;
  public $list_page = 1;
  public $page_size = 10;
  public $uid;
  public $coltypes = array(
    /* Basic Fields */ 'name', 'firstname', 'surname' , 'email', 
    /* Work Fields */ 'jobtitle', 'organization', 'department', 'assistant', 'manager',
    /* Multiples Items Fields */ 'phone', 'address', 'im', 'website',
    /* Personnal Fields*/ 'nickname', 'spouse', 'notes'
    );
  private $filter;
  private $contacts;
  private $result;
  private $cache;
  private $requester;

  public function __construct(obmSyncRequester $requester, $obmuid, $name, $uid, $readonly){
    global $CONFIG;

    $this->requester = $requester;
    $this->obmuid = $obmuid;
    $this->database_name = $name;
    $this->name = $this->convert_to_pretty_name($name);
    $this->uid = $uid;
    $this->readonly = $readonly == "false" ? false:true;
    if ($CONFIG['addressbook_pagesize'] != 0) {
      $CONFIG['pagesize'] = $CONFIG['addressbook_pagesize'];
    }
    $this->page_size = $CONFIG['addressbook_pagesize'];
  }

  public function set_search_set($filter) {
    $this->filter = $filter;
  }

  public function get_search_set() {
    return $this->filter;
  }

  public function get_name()
  {
    return $this->name;
  }

  private static function convert_to_pretty_name($name) {
    if ( $name == "collected_contacts")
      return "Contacts collectés";
    else if ( $name == "public_contacts")
      return "Contacts publics";
    else if ($name == "contacts")
      return "Mes contacts";
    else if ($name == "users")
      return "Utilisateurs";

    return $name;
  }

  public function reset() {
    $this->result = null;
    $this->filter = null;
    $this->search_fields = null;
    $this->search_string = null;
    $this->cache = null;
  }

  public function list_records($cols=null, $subset=0, $nocount=false) {

    // Check if list_records() is called within a search request
    if (isset($_GET['_search']))
      $this->set_search_set($_SESSION['search_params']['data'][1]);

    if ($nocount || $this->list_page <= 1) {
      // create dummy result, we don't need a count now
      $this->result = new rcube_result_set();
    } else {
      // count all records
      $this->result = $this->count();
    }

    $offset = $subset < 0 ? $this->result->first + $this->page_size + $subset : $this->result->first;

    if (is_array($cols['value'])) { // Value is an IDs array
      foreach ($cols['value'] as $id) {
        $this->requester->callObmSyncService(
          "getContactFromId",
          array("bookId"=>$this->obmuid, "contactId"=>$id));

        $this->buildContactsArray();
      }
    } else {
      $pattern = $this->get_search_set();
      if (!empty($pattern) && $cols['value'] && preg_match("/^[a-z0-9\.-_ ]+$/", $cols['value'])) {
        $strict = $cols['strict']?'':'*';
        $this->set_search_set($cols['value']);
      } else {
        $this->set_search_set('');
      }
      $this->requester->callObmSyncService(
        "searchContactInGroup",
          array(
            "groupId"  => $this->obmuid,
            "pattern"  => $pattern,
            "limit"    => $this->page_size,
            "offset"   => $offset
        )
      );


      $this->buildContactsArray();
    }

    $cnt = count($this->result->records);
    $this->result->count = $nocount ? $cnt : $this->cache['count'] = $this->retrieveContactCount();
    return $this->result;
  }

  /**
   * Count number of available contacts in database
   *
   * @return rcube_result_set Result object
   */
  public function count()
  {
      $count = isset($this->cache['count']) ? $this->cache['count'] : $this->retrieveContactCount();
      return new rcube_result_set($count, ($this->list_page-1) * $this->page_size);
  }

  private function retrieveContactCount() {
    $this->requester->callObmSyncService("countContactsInGroup", array("groupId"  => $this->obmuid, "pattern" => $this->get_search_set()));
    $xml = $this->requester->getLastValidXmlResponse();
    return (int) $xml->getElementsByTagName('count')->item(0)->nodeValue;
  }

  private function buildContactsArray () {
    $xmllistcontacts = $this->requester->getLastValidXmlResponse();
    $xmlcontacts = $xmllistcontacts->getElementsByTagName('contact');

    $sortedContacts = $this->getSortedContacts($xmlcontacts);

    $this->result->count = 0;

    foreach ($sortedContacts as $xmlcontact) {

      $email = null;
      $surname = null;
      $firstname = null;

      if($xmlcontact->getElementsByTagName('emails')->item(0)->firstChild) 
        $email = $xmlcontact->getElementsByTagName('emails')->item(0)->firstChild->getAttribute('value');

      if($xmlcontact->getElementsByTagName('last')->length > 0) 
        $surname = $xmlcontact->getElementsByTagName('last')->item(0)->nodeValue;

      if($xmlcontact->getElementsByTagName('first')->length > 0) 
        $firstname = $xmlcontact->getElementsByTagName('first')->item(0)->nodeValue;

      $name = $surname.' '.$firstname;

      if ( method_exists("rcube_contacts", "get_display_name") )
        $name = rcube_contacts::get_display_name($surname, $firstname);

      $arr = array (  'ID'        => $xmlcontact->getAttribute('uid'),
                      'name'      => $name,
                      'surname'   => $surname,
                      'firstname' => $firstname,
                      'email'     => $email
                    );
      $this->cache['contacts'][$arr['ID']] = $arr;
      $this->result->add($arr);
      $this->result->count++;
    }
  }

  private function getSortedContacts($xmlcontacts){
    $nb = $xmlcontacts->length;
    $sortedContacts = array();

    for($i=0; $i<$nb ;$i++) {
      $xmlcontact = $xmlcontacts->item($i);
      $sortKey = obm_addressbook::getUserSortKey($xmlcontact);
      $sortedContacts[$sortKey] = $xmlcontact;
    }
    ksort($sortedContacts);

    return $sortedContacts;
  }

  public function search($fields, $value, $mode=0, $select=true, $nocount=false, $required=array()) {
    if (!empty($value)) {
        $this->set_search_set($value);
        if ($select) {
            $this->list_records(array('value'=>$value, 'strict'=>$mode), 0, true);
        } else
            $this->result = $this->count();
    }

    return $this->result;
  }

  public function get_record($id, $assoc=false) {

    $this->requester->callObmSyncService(
                        "getContactFromId",
                        array("bookId"=>$this->obmuid, "contactId"=>$id)
    );
    $xmlcontact = $this->requester->getLastValidXmlResponse();
    $contact = $xmlcontact->getElementsByTagName('contact');
    $arr = array (  
                  'ID'          => $id,
                  'name'        => $contact->item(0)->getElementsByTagName('commonname')->item(0)->nodeValue,
                  'surname'     => $contact->item(0)->getElementsByTagName('last')->item(0)->nodeValue,
                  'firstname'   => $contact->item(0)->getElementsByTagName('first')->item(0)->nodeValue,
                  'nickname'    => $contact->item(0)->getElementsByTagName('aka')->item(0)->nodeValue,
                  'jobtitle'    => $contact->item(0)->getElementsByTagName('title')->item(0)->nodeValue,
                  'organization'=> $contact->item(0)->getElementsByTagName('company')->item(0)->nodeValue,
                  'department'  => $contact->item(0)->getElementsByTagName('service')->item(0)->nodeValue,
                  'manager'     => $contact->item(0)->getElementsByTagName('manager')->item(0)->nodeValue,
                  'assistant'   => $contact->item(0)->getElementsByTagName('assistant')->item(0)->nodeValue,
                  'spouse'      => $contact->item(0)->getElementsByTagName('spouse')->item(0)->nodeValue,
                  'notes'       => $contact->item(0)->getElementsByTagName('comment')->item(0)->nodeValue
                );
    // Mail
    $mails = $contact->item(0)->getElementsByTagName('emails')->item(0)->getElementsByTagName('mail');
    for ($i=0; $i<$mails->length ;$i++) {
      $category = (preg_match('#^OTHER#', $mails->item($i)->getAttribute('label'))) ? 'other' : 'work';
      $arr['email:'.$category][] = $mails->item($i)->getAttribute('value');
    }
    // Phones
    $phones = $contact->item(0)->getElementsByTagName('phones')->item(0)->getElementsByTagName('phone');
    $phoneSubtypes = array( 'home'    => 'HOME;VOICE;',
                            'work'    => 'WORK;VOICE;',
                            'mobile'  => 'CELL;VOICE;',
                            'homefax' => 'HOME;FAX;',
                            'workfax' => 'WORK;FAX;',
                            'pager'   => 'PAGER;',
                            'other'   => 'OTHER;'
                          );
    for ($i=0; $i<$phones->length ;$i++) {
      foreach ($phoneSubtypes as $subtype => $xmlLabel) {
        if(preg_match('#^'.$xmlLabel.'#', $phones->item($i)->getAttribute('label'))){
          $arr['phone:'.$subtype][] = $phones->item($i)->getAttribute('number');
        }
      }
    }
    // Adresses
    $addresses = $contact->item(0)->getElementsByTagName('addresses')->item(0)->getElementsByTagName('address');
    $addressSubtypes = array( 'home' => 'HOME;',
                              'work' => 'WORK;',
                              'other'=> 'OTHER;'
                            );
    for ($i=0; $i<$addresses->length ;$i++) {
      foreach ($addressSubtypes as $subtype => $xmlLabel) {
        if(preg_match('#^'.$xmlLabel.'#', $addresses->item($i)->getAttribute('label'))){
          $arr['address:'.$subtype][] = array(
            'street' => $addresses->item($i)->nodeValue,
            'locality' => $addresses->item($i)->getAttribute('town'),
            'zipcode' => $addresses->item($i)->getAttribute('zip'),
            'region' => $addresses->item($i)->getAttribute('state'),
            'country' => $addresses->item($i)->getAttribute('country'),
            );
        }
      }
    }
    // Websites
    $websites = $contact->item(0)->getElementsByTagName('websites')->item(0)->getElementsByTagName('site');
    $websitesSubtypes = array( 
                            'BLOG'    => 'blog',
                            'URL'     => 'homepage',
                            'WORK'    => 'work',
                            'OTHER'   => 'other'
                          );
    for ($i=0; $i<$websites->length ;$i++) {
      foreach ($websitesSubtypes as $xmlLabel => $subtype) {
        $labelToSearch = $websites->item($i)->getAttribute('label');
        if( preg_match('#^'.$xmlLabel.'#', $labelToSearch) ){
          $arr['website:'.$subtype][] = $websites->item($i)->getAttribute('url');
        }
      }
    }
    // Instant Messaging
    $instantMessaging = $contact->item(0)->getElementsByTagName('instantmessaging')->item(0)->getElementsByTagName('im');
    $instantMessagingSubtypes = array( 
                                      'aim'   => 'AIM',
                                      'icq'   => 'X_ICQ',
                                      'msn'   => 'MSN',
                                      'yahoo' => 'YMSGR',
                                      'jabber'=> 'XMPP',
                                      'other' => 'OTHER'
                                     );
    for ($i=0; $i<$instantMessaging->length ;$i++) {
      foreach ($instantMessagingSubtypes as $subtype => $xmlLabel) {
        $labelToSearch = $instantMessaging->item($i)->getAttribute('protocol');
        if($subtype == 'other' && ( preg_match('#^OTHER#', $labelToSearch) || preg_match('#^X_GTALK#', $labelToSearch) )){
            $arr['im:'.$subtype][] = $instantMessaging->item($i)->getAttribute('address');
        }elseif( preg_match('#^'.$xmlLabel.'#', $labelToSearch) ){
          $arr['im:'.$subtype][] = $instantMessaging->item($i)->getAttribute('address');
        }
      }
    }
    $this->result = new rcube_result_set(1);
    $this->result->add($arr);
    return $arr;
  }

  public function get_result() {
    return $this->result;
  }

}
