<?php

require_once(dirname(__FILE__) . '/obm_addressbook_backend.php');
require_once(dirname(__FILE__) . '/obmSyncRequester.class.php');

class obm_addressbook extends rcube_plugin 
{
  public $task = "mail|addressbook|settings";
  public $addressbooks = array();
  private $user;
  private $pass;
  private $origin = null;
  private $editSubtypesForOBM = false;
  private $obmsyncserver;
  private $domain;
  private $sid;
  private $ssl;
  private $requester;
  public static $possible_sort_fields = array(
      "primary_sort_field" => "first",
      "secondary_sort_field" => "last"
  );
  
  public function init() 
  {
    $this->load_config('config.inc.php');
    $this->add_texts('localization/');
    $rcmail = rcmail::get_instance();
    $this->user = $_SESSION['username'];
    $this->pass = $rcmail->decrypt($_SESSION['password']);
    $this->domain = $rcmail->config->get('OBM_domain');
    $this->ssl = $rcmail->config->get('OBM_url').'://';
    $port = $rcmail->config->get('OBM_SYNC_port');
    $timeout = $rcmail->config->get('networkTimeout');

    $requesterOptions = array();
    if ( $timeout ) {
      $requesterOptions["networkTimeout"] = $timeout;
    }
    $httpRequester = new CurlRequester($requesterOptions);    
    obmSyncRequester::$userEmail =  strpos($this->user,'@') === false 
                                    ? $this->user."@".$this->domain 
                                    : $this->user;
    $this->requester = new obmSyncRequester(
                            $httpRequester,
                            $rcmail->config->get('obmAuthType'),
                            $rcmail->config->get('obmSyncIp')
                           );
    $this->requester->setLogin($this->user);
    $this->requester->setPassword($this->pass);
    $this->requester->setDomainName($this->domain);
    
    $this->add_hook('addressbooks_list', array($this, 'addressbooks_list'));
    $this->add_hook('addressbook_get', array($this, 'addressbook_get'));
    $this->add_hook('contact_form', array($this, 'contact_form'));
    $this->add_hook('contact_create', array($this, 'contact_create'));
    $this->add_hook('contact_delete', array($this, 'contact_delete'));
    $this->add_hook('contact_update', array($this, 'contact_update'));
    $this->add_hook('preferences_list', array($this, 'add_addressbook_settings'));
    $this->add_hook('preferences_save', array($this, 'add_addressbook_settings_save'));
    $this->add_hook('message_sent', array($this, 'register_recipients_as_contacts'));

    if ($this->requester->countErrors())
    {
      write_log('errors', '***'.__LINE__." obm_addressbook::init : ".$this->requester->getFirstError());
      return;
    }

    $this->requester->callObmSyncService("listAllBooks");
    if($this->requester->countErrors() > 0){
      write_log('errors', '***'.__LINE__." obm::init Couldn't get addressbooks from obm-sync ".$this->requester->getFirstError());
      return;
    }

    $xmladdressbooks = $this->requester->getLastValidXmlResponse();
    $xmlbooks = $xmladdressbooks->getElementsByTagName('book');
    $nb = $xmlbooks->length;
    for($i=0; $i<$nb ;$i++) {
      $this->addressbooks []= new obm_addressbook_backend(
                                $this->requester,
                                $xmlbooks->item($i)->getAttribute('uid'),
                                $xmlbooks->item($i)->getAttribute('name'),
                                'OBM'.$xmlbooks->item($i)->getAttribute('uid'),
                                $xmlbooks->item($i)->getAttribute('readonly')
                              );
    }
    $config = rcmail::get_instance()->config;
    $sources = (array) $config->get('autocomplete_addressbooks', array('sql'));
    foreach($this->addressbooks as $ad) {
      if (!in_array($ad->uid, $sources)) {
        $sources[] = $ad->uid;
      }
    }

    $config->set('autocomplete_addressbooks', $sources);
    $this->ready = true;
  }

  public function addressbooks_list($p) {
    $rcmail = rcmail::get_instance();
    
    foreach($this->addressbooks as $addressbook) {
      if ($this->isUsersAddressbook($addressbook) && !$rcmail->config->get('OBMaddressbook_showUsersBook')) {
        continue;
      }
      
      if ($this->isPublicAddressbook($addressbook) && !$rcmail->config->get('OBMaddressbook_showPublicContactsBook')) {
        continue;
      }
      
      $p['sources'][$addressbook->uid] = array(
        'id' => ($addressbook->uid),
        'name' => $addressbook->name,
        'readonly' => $addressbook->readonly,
        'groups' => false
      );
    }
    return $p;
  }

  public function addressbook_get($p) {
    if ( !$p['id'] ) {
      $ad = $this->getContactsAddressbook();
      $p['instance'] = $ad;
      $p['id'] = $ad->uid;
      return $p;
    }
    foreach($this->addressbooks as $ad) {
      if ($ad->uid === $p['id']) $p['instance'] = $ad;
    }
    $p['instance']->ready = true;
    return($p);
  }

  protected function getAddressbookByDatabaseName($name) {
    foreach ($this->addressbooks as $ad) {
      if ($ad->database_name == $name) {
        return $ad;
      }
    }
  }

  protected function getContactsAddressbook() {
    return $this->getAddressbookByDatabaseName('contacts');
  }

  protected function getCollectedContactsAddressbook() {
    return $this->getAddressbookByDatabaseName('collected_contacts');
  }

  protected function isPublicAddressbook($book) {
    return $book->name == $this->getAddressbookByDatabaseName('public_contacts')->name;
  }
  
  protected function isUsersAddressbook($book) {
    return $book->obmuid == -1;
  }

  protected function sanitizeContact($contact) {
    $emailName = array_shift(explode("@",$contact["record"]["email"]));

    if ( !$contact["record"]["surname"] ) {
      $contact["record"]["surname"] = $contact["record"]["name"] ? $contact["record"]["name"] : $emailName;
    }
    unset($contact["record"]["ID"]);
    unset($contact["record"]["contact_id"]);
    unset($contact["record"]["changed"]);
    unset($contact["record"]["del"]);
    unset($contact["record"]["vcard"]);
    unset($contact["record"]["words"]);
    unset($contact["record"]["user_id"]);
    if ( !$contact["record"]["nickname"] ) { $contact["record"]["nickname"] = ''; }
    if ( !$contact["record"]["jobtitle"] ) { $contact["record"]["jobtitle"] = ''; }
    if ( !$contact["record"]["organization"] ) { $contact["record"]["organization"] = ''; }
    if ( !$contact["record"]["department"] ) { $contact["record"]["department"] = ''; }
    if ( !$contact["record"]["phone:home"] ) { $contact["record"]["phone:home"] = array(0 => ''); }
    if ( !$contact["record"]["address:home"] ) {
      $contact["record"]["address:home"] = array(0 => array("street" => '',
                                                          "locality" => '',
                                                          "zipcode" => '',
                                                          "region" => '',
                                                          "country" => ''));
    }
    if ( !$contact["record"]["notes"] ) { $contact["record"]["notes"] = '';}
    return $contact;
  }

  /*
  $contact :
  Array
  (
      [id] => 21
      [record] => Array
    (
        [name] => Willis Bruce
        [firstname] => Bruce2
        [surname] => Willis
        [email] => bw@warner.com
    )

      [source] => OBM456456546456
      [abort] => 
  )
  */

  public function contact_update($contact){
    global $OUTPUT;
    $response = $this->requester->callObmSyncService("updateContact", array("contact"=>$contact));
    if ( $response ) {
      $contact["result"] = $contact['id'];
      $contact["abort"] = true;
      $OUTPUT->show_message('successfullysaved', 'confirmation');
    } else {
      $contact["result"] = false;
    }
    return $contact;
  }


  /*
  $contact :
  Array
  (
      [id] => 21
      [record] => Array
    (
        [name] => Willis Bruce
        [firstname] => Bruce2
        [surname] => Willis
        [email] => bw@warner.com
    )

      [source] => OBM456456546456
      [abort] => 
  )
  */
  public function contact_form($rcArray){
    // Modify the GLOBALS['CONTACT_COLTYPES'] to remove RC specific subtypes
    if($rcArray['form']['contact']['content'] && !$this->editSubtypesForOBM){
      $GLOBALS['CONTACT_COLTYPES']['email']['subtypes'] = array(  'work','other'  );

      $GLOBALS['CONTACT_COLTYPES']['phone']['subtypes'] = array(  'home',
                                                                  'work',
                                                                  'mobile',
                                                                  'homefax',
                                                                  'workfax',
                                                                  'pager',
                                                                  'other'
                                                                );
      $GLOBALS['CONTACT_COLTYPES']['website']['subtypes'] = array(  'homepage',
                                                                    'work',
                                                                    'blog',
                                                                    'other'
                                                                  );
      $GLOBALS['CONTACT_COLTYPES']['im']['subtypes'] = array( 'aim',
                                                              'icq',
                                                              'msn',
                                                              'yahoo',
                                                              'jabber',
                                                              'other'
                                                            );
      $this->editSubtypesForOBM = true;
    }
    return $rcArray;
  }

  public function contact_create(array $contact){
    global $OUTPUT;
    $contact = $this->sanitizeContact($contact);
    if ( !$contact["source"] ) {
      $ad = $this->getContactsAddressbook();
      $contact["source"] = $ad->uid;
    } else {
      $contact["abort"] = true;
    }
    $response = $this->requester->callObmSyncService("createContact", array("contact"=>$contact));
    if ( $response ) {
      $contact["result"] = $response->getElementsByTagName('contact')->item(0)->getAttribute("uid");
      $OUTPUT->show_message('addedsuccessfully', 'confirmation');
    } else {
      $contact["result"] = false;
    }
    return $contact;
  }

  public function contact_delete(array $contact){
    global $OUTPUT;
    foreach ($contact['id'] as $id) {
      $contactToDelete = $contact;
      $contactToDelete['id'] = $id;

      $response = $this->requester->callObmSyncService("deleteContact", array("contact"=>$contactToDelete));
    }
    if ( $response ) {
      $contact["result"] = true;
      $contact["abort"] = true;
      $OUTPUT->show_message('deletedsuccessfully', 'confirmation');      
    } else {
      $contact["result"] = false;
    }
    return $contact;
  }

  function add_addressbook_settings($args)
  {
    if ($args['section'] == 'addressbook') {
        $prefs = rcmail::get_instance()->user->get_prefs();

        foreach(self::$possible_sort_fields as $field_name => $field_value){
            $sort_field_id = 'rcmfd_'.$field_name;
            $select = new html_select(array(
                                    'name' => '_'.$field_name,
                                    'id' => $sort_field_id,
                                    'value' => $field_value
                                  ));
            $select->add(
                $this->getSortLabels(),
                array_values(self::$possible_sort_fields)
            );


            $selected = self::getSortFieldValueForFieldName($fieldname, $prefs);
            $title = preg_replace("/_/", " ", "Address books $field_name");
            $args['blocks']['main']['options'][$field_name] = array(
                'title' => $this->get_i18n_label($title, "obm_addressbook"),
                'content' => $select->show($selected),
            );
        }
    }

    return $args;
  }

  function add_addressbook_settings_save($args){
      $unused_sort_fields =self::$possible_sort_fields;
      $used_sort_fields = array();

      foreach(self::$possible_sort_fields as $field_name => $field_value){

          if(!in_array($field_name, $args["prefs"])) {
            $field_value = $this->getSortFieldValue($field_name, $used_sort_fields, $unused_sort_fields);
            $this->remove_value_from_array($unused_sort_fields, $field_value);
            $used_sort_fields[] = $field_value;
            $args["prefs"][$field_name] = $field_value;
          }

      }

      return $args;
  }

  private function get_i18n_label($label){
      return rcube_label($label, "obm_addressbook");
  }

  private function getSortLabels(){
      $sort_labels = array();
      foreach (self::$possible_sort_fields as $field_value) {
        $sort_labels[] = $this->get_i18n_label($field_value." sort field");
      }
      return $sort_labels;
  }

  private function getSortFieldValue($field_name, $used_sort_fields, $unused_sort_fields){
      $fieldValue = get_input_value('_'.$field_name, RCUBE_INPUT_POST, false);
      if(in_array($fieldValue, $used_sort_fields)){
          $fieldValue = array_shift($unused_sort_fields);
      }
      return $fieldValue;
  }

  private function remove_value_from_array(&$array, $valueToRemove){
      if(!is_array($array)){
          return $array;
      }

      foreach ($array as $key => $value) {
          if($value == $valueToRemove){
              unset($array[$key]);
          }
      }
  }

  public static function getUserSortKey(DOMNode $xmlcontact){
    $prefs = rcmail::get_instance()->user->get_prefs();
    $key = null;

    foreach (self::$possible_sort_fields as $fieldname => $value) {
      $tagName = self::getSortFieldValueForFieldName($fieldname, $prefs);

      if($xmlcontact->getElementsByTagName($tagName)->length > 0) {
        $part = $xmlcontact->getElementsByTagName($tagName)->item(0)->nodeValue;
        if(is_null($key)){
          $key = $part;
        }
        else{
          $key .= " ".$part;
        }
      }
    }

    $key .= " " . $xmlcontact->getAttribute('uid');

    return $key;
  }

  public static function getSortFieldValueForFieldName($fieldname, $prefs){
    if(is_array($prefs) && array_key_exists($fieldname, $prefs)) {
      $tagName = $prefs[$fieldname];
    } else {
      $tagName = self::$possible_sort_fields[$fieldname];
    }

    return $tagName;
  }

  public function register_recipients_as_contacts($p) {
    $rcmail = rcmail::get_instance();

    $IMAP = new rcube_imap(null);
    $headers = $p['headers'];

    $all_recipients = array_merge(
      $IMAP->decode_address_list($headers['To']),
      $IMAP->decode_address_list($headers['Cc']),
      $IMAP->decode_address_list($headers['Bcc'])
    );

    foreach($all_recipients as $recipient) {
      if ($recipient['mailto'] != '') {
        $contact = array(
          'email:work' => array(0 => $recipient['mailto']),
          'name' => $recipient['name']
        );

        if (empty($contact['name']) || $contact['name'] == $recipient['mailto'])
          $contact['name'] = ucfirst(preg_replace('/[\.\-]/', ' ', substr($recipient['mailto'], 0, strpos($recipient['mailto'], '@'))));

        $bookids = (array)$rcmail->config->get('autocomplete_addressbooks');
        foreach ($bookids as $bookid) {
          $book = $rcmail->get_address_book($bookid);

          if(!$this->isPublicAddressbook($book)) {
            $previous_entries = $book->search('email', $recipient['mailto'], false, true, true);
            if ($previous_entries->count) {
              break;
            }
          }
        }
        if (!$previous_entries->count) {
        $contact_wrapper = array('record' => $contact, 'source' => $this->getCollectedContactsAddressbook()->uid);
          $this->contact_create($contact_wrapper);
        }
      }
    }
  }
}
