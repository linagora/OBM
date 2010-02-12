<?php

require_once 'vpdi/vpdi.php';
require_once 'vpdi/field.php';
require_once 'vpdi/entity.php';
require_once 'vpdi/vcard.php';
require_once 'obminclude/of/of_search.php';

class OBM_Contact implements OBM_ISearchable {

  public static $fields = array (
    'timecreate' 	=> array('sql' => 'contact_timecreate', 'sqlkind' => 'text', 'type' => 'text'),
    'timeupdate' 	=> array('sql' => 'contact_timeupdate', 'sqlkind' => 'text', 'type' => 'text'),
    'usercreate' 	=> array('sql' => 'contact_usercreate', 'sqlkind' => 'text', 'type' => 'text'),
    'userupdate' 	=> array('sql' => 'contact_userupdate', 'sqlkind' => 'text', 'type' => 'text'),
    'lastname' 	=> array('sql' => 'contact_lastname', 'sqlkind' => 'text', 'type' => 'text'),
    'firstname'	=> array('sql' => 'contact_firstname', 'sqlkind' => 'text', 'type' => 'text'),
    'mname' 	=> array('sql' => 'contact_mname', 'sqlkind' => 'text', 'type' => 'text'),
    'kind' 	=> array('sql' => 'contact_kind', 'sqlkind' => 'text', 'type' => 'text'),
    'title' 	=> array('sql' => 'contact_title', 'sqlkind' => 'text', 'type' => 'text'),
    'function' 	=> array('sql' => 'contact_function', 'sqlkind' => 'text', 'type' => 'text'),
    'company_id'=> array('sql' => 'contact_company_id', 'sqlkind' => 'text', 'type' => 'text'),
    'company' 	=> array('sql' => 'contact_company', 'sqlkind' => 'text', 'type' => 'text'),
    'market_id' 	=> array('sql' => 'contact_market', 'sqlkind' => 'text', 'type' => 'text'),
    'suffix' 	=> array('sql' => 'contact_suffix', 'sqlkind' => 'text', 'type' => 'text'),
    'aka' 	=> array('sql' => 'contact_aka', 'sqlkind' => 'text', 'type' => 'text'),
    'sound' 	=> array('sql' => 'contact_sound', 'sqlkind' => 'text', 'type' => 'text'),
    'manager' 	=> array('sql' => 'contact_manager', 'sqlkind' => 'text', 'type' => 'text'),
    'assistant' => array('sql' => 'contact_assistant', 'sqlkind' => 'text', 'type' => 'text'),
    'spouse' 	=> array('sql' => 'contact_spouse', 'sqlkind' => 'text', 'type' => 'text'),
    'category' 	=> array('sql' => 'contact_category', 'sqlkind' => 'text', 'type' => 'text'),
    'service' 	=> array('sql' => 'contact_service', 'sqlkind' => 'text', 'type' => 'text'),
    'mailok' 	=> array('sql' => 'contact_mailok', 'sqlkind' => 'text', 'type' => 'text'),
    'newsletter'=> array('sql' => 'contact_newsletter', 'sqlkind' => 'text', 'type' => 'text'),
    'date' 	=> array('sql' => 'contact_date', 'sqlkind' => 'text', 'type' => 'text'),
    'birthday' 	=> array('sql' => 'contact_birthday', 'sqlkind' => 'text', 'type' => 'text'),
    'birthday_event' 	=> array('sql' => 'contact_birthday_event', 'sqlkind' => 'text', 'type' => 'text'),
    'anniversary' 	=> array('sql' => 'contact_anniversary', 'sqlkind' => 'text', 'type' => 'text'),
    'anniversary_event'	=> array('sql' => 'contact_anniversary_event', 'sqlkind' => 'text', 'type' => 'text'),
    'phone' 	=> array('sql' => 'contact_phone', 'sqlkind' => 'text', 'type' => 'text'),
    'email' 	=> array('sql' => 'contact_email', 'sqlkind' => 'text', 'type' => 'text'),
    'address' 	=> array('sql' => 'contact_address', 'sqlkind' => 'text', 'type' => 'text'),
    'im' 	=> array('sql' => 'contact_im', 'sqlkind' => 'text', 'type' => 'text'),
    'website' 	=> array('sql' => 'contact_website', 'sqlkind' => 'text', 'type' => 'text'),
    'archive' 	=> array('sql' => 'contact_archive', 'sqlkind' => 'text', 'type' => 'text'),
    'collected' 	=> array('sql' => 'contact_archive', 'sqlkind' => 'text', 'type' => 'text'),
    'datasource_id' 	=> array('sql' => 'contact_datasource_id', 'sqlkind' => 'text', 'type' => 'text'),
    'comment' 	=> array('sql' => 'contact_comment', 'sqlkind' => 'text', 'type' => 'text'),
    'comment2' 	=> array('sql' => 'contact_comment2', 'sqlkind' => 'text', 'type' => 'text'),
    'comment3' 	=> array('sql' => 'contact_comment3', 'sqlkind' => 'text', 'type' => 'text'),
    'origin' 	=> array('sql' => 'contact_origin', 'sqlkind' => 'text', 'type' => 'text'),
    'addressbook' 	=> array('sql' => 'contact_addressbook', 'sqlkind' => 'text', 'type' => 'text')
  ); 

  private  $id;
  private  $timecreate;
  private  $timeupdate;
  private  $usercreate;
  private  $userupdate;
  private  $entity_id;

  private  $lastname;
  private  $firstname;
  private  $mname;//middlename
  private  $kind;
  private  $title;
  private  $function;
  private  $categories = array();
  private  $company_id;
  private  $company;
  private  $market_id;//marketingmanager_id
  private  $suffix;
  private  $aka;
  private  $sound;
  private  $manager;
  private  $assistant;
  private  $spouse;
  private  $category;
  private  $service;
  private  $mailok;//mailing_ok
  private  $newsletter;
  private  $date;
  private  $birthday;
  private  $birthday_event;
  private  $anniversary;
  private  $anniversary_event;
  private  $phone = array();
  private  $email = array();
  private  $address = array();
  private  $im = array();
  private  $website = array();
  private  $archive;
  private  $collected;
  private  $datasource_id;
  private  $comment;
  private  $comment2;
  private  $comment3;
  private  $origin;
  private  $addressbook_id;
  private  $addressbook;
 
  private static $kinds = null;

  public function display_name() {
    return "{$this->firstname} {$this->lastname}";
  }

  public function __get($key) {
    if (($key=='entity_id') && !is_null($this->id) && is_null($this->entity_id))
      $this->entity_id = of_entity_get('contact', $this->id);
    if ($key == 'email_address') {
      return $this->email[0]['address'];
    }
    if ($key == 'phone_number') {
      foreach($this->phone as $phone) {
        if($phone['label'][0] == 'CELL' && $phone['label'][1] == 'VOICE') {
          return $phone['number'];
        }
      }
    }    
    if ($key == 'name' || $key == 'displayname')
      return $this->lastname.' '.$this->firstname;
    return $this->$key;
  }

  public function __isset($key) {
    return isset($this->$key);
  }

  public function __set($key,$value) {
    // Can't modify id
    if ($key=='id')
      return;

    // remember that company, anniversary or birthday has been modified
    if ($key=='company_id')
      $this->old_company_id = $this->company_id;
    if ($key=='birthday')
      $this->old_birthday = $this->birthday;
    if ($key=='anniversary')
      $this->old_anniversary = $this->anniversary;

    // update phonetic if lastname is modified
    if ($key=='lastname')
      $this->sound = phonetic_key($value);

    // Archived contact unsubscribed from mailings
    if (($key=='archive') && ($value)) {
      $this->mailok = '0';
      $this->newsletter = '0';
    }

    //check data
    if (($key=='mailok') || ($key=='newsletter') || ($key=='archive'))
      $value = $value ? '1' : '0';
    if (($key=='birthday') || ($key=='anniversary')) {
      $date = of_isodate_convert($value, true);
      $value = (!empty($date) ? new Of_Date($date) : null);
    }

    $this->$key = $value;
  }

  public function getCoords($kind, $label = null) {
    if(!$label) return $this->$kind;
    $label = explode(';', $label);
    $return = array();
    foreach($this->$kind as $coord) {
      $clabel = $coord['label'];
      array_pop($clabel);
      if($label == $clabel) $return[] = $coord; 
    }
    return $return;
  }

  public static function get($id, $domain = null) {
    $where = "contact_id = '{$id}'";
    if ($domain !== null) {
      $where.= " AND (contact_domain_id='{$domain}')";
    }
    $contacts = self::fetchAll($where);
    return array_pop($contacts);
  }


  public static function fetchPrivate($userId) {
    $where = "contact_usercreate = {$userId} ";
    $where.= sql_multidomain('contact');
    return self::fetchAll($where);
  }

  public static function fieldsMap() {
    $fields['*'] = array(
      'contact_lastname' => 'text',
      'contact_firstname' => 'text',
      'contact_middlename' => 'text',
      'contact_spouse' => 'text',
      'contact_suffix' => 'text',
      'contact_title' => 'text',
      'contact_aka' => 'text',
      'contact_sound' => 'text',
      'contact_company' => 'text',
      'company_name' => 'text'
    );
    $fields['company'] = array(
      'contact_company' => 'text',
      'company_name' => 'text'
    );
    $fields['displayname'] = array(
      'contact_lastname' => 'text',
      'contact_firstname' => 'text'
    );
    $fields['country'] = array(
      'address_country' => 'text',
      'country_name' => 'text'
    );

    $fields['address'] = array(
      'address_street' => 'text',
      'address_zipcode' => 'text',
      'address_expresspostal' => 'text',
      'country_name' => 'text',
      'address_country' => 'text',
      'address_state' => 'text',
      'address_town' => 'text'
    );
    $fields['kind'] = array(
      'kind_header' => 'text',
      'kind_minilabel' => 'text'
    );
    $fields['comment'] = array(
      'contact_comment'  => 'text',
      'contact_comment2' => 'text',
      'contact_comment3' => 'text'
    );
    $fields['id'] = array('contact_id' => 'integer');
    $fields['in'] = array('contact_addressbook_id' => 'integer');
    $fields['addressbook'] = array('AddressBook.name' => 'text');
    $fields['company_id'] = array ('contact_company_id' => 'integer');
    $fields['archive'] = array('contact_archive' => 'integer');
    $fields['newsletter'] = array('contact_newsletter' =>  'integer');
    $fields['mailok'] = array('contact_mailing_ok' => 'integer');
    $fields['phone'] = array('phone_number' => 'text' );
    $fields['email'] = array('email_address' => 'text');
    $fields['town'] = array('address_town' => 'text');
    $fields['zipcode'] = array('address_zipcode' => 'text');
    $fields['lastname'] = array('contact_lastname' => 'text');
    $fields['firstname'] = array('contact_firstname' => 'text');
    $fields['mname'] = array('contact_middlename' => 'text');
    $fields['title'] = array('contact_title' => 'text');
    $fields['assistant'] = array('contact_assistant' => 'text');
    $fields['spouse'] = array('contact_spouse' => 'text');
    $fields['category'] = array('contact_category' => 'text');
    $fields['category_id'] = array('categorylink_category_id' => 'integer');
    $fields['service'] = array('contact_service' => 'text');
    $fields['suffix'] = array('contact_suffix' => 'text');
    $fields['aka'] = array('contact_aka' => 'text');
    $fields['sound'] = array('contact_sound' => 'text');
    $fields['function'] = array('contactfunction_label' => 'text');
    $fields['categoryId'] = array('contact_category' => 'integer');
    return $fields;
  }

  public static function search($pattern, $offset=0, $limit=100) {
    return OBM_Contact::fetchAll(
      OBM_Search::buildSearchQuery('contact', $pattern, $offset, $limit, array('sort' => 'lastname asc, firstname asc')));
  }

  public static function getUserCategory() {
    global $cgp_user;
    if (is_array($cgp_user['contact']['category'])) {
      $i=0;
      foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
        if ($i==0) $block_user .= "<tr>";
        $cats = of_category_user_get_ordered($cat_name);
        $name = "l_${cat_name}";
        global $$name;
        $block_user .= "<th>".$$name."</th><td>
          <select name='categoryId'><option value=''>$GLOBALS[l_none]</option>";
        foreach($cats as $cat) {
          $code = '';
          if ($cat['code'] != '') $code = str_pad($cat['code'] . ' ', 10, '.');
          $block_user .= "<option value=".$cat['id'].">".$code.$cat['label']."</value>";
        }
        $block_user .= "</select></td>";
        if ($i==2) {
          $block_user .= "</tr>";
          $i=0;
        } else {
          $i++;
        }
      }
    }
    return $block_user;
  }
  
  public static function fetchAll($where) {
    $db = new DB_OBM();
    $contacts = self::fetchDetails($db, $where);
    if (count($contacts) != 0) {
      $contacts = self::fetchCoords($db, $contacts);
      $contacts = self::fetchCategories($db, $contacts);
    }
    return $contacts;
  }
  
  //FIXME Redo this, it's totally buggy and crappy
  public static function create($data, $addressbook) {
    global $cgp_show, $cdg_sql, $obm;

    $uid = sql_parse_id($obm['uid']);
    $domain_id = sql_parse_id($obm['domain_id']);

    $data['aka'] = trim($data['aka']);
    // If aka is empty we auto fill it
    if ($aka == '') {
      $auto_aka = format_name($data['lastname'], 0, true, true);
      if ($auto_aka != $data['lastname']) {
        $data['aka'] = $auto_aka;
      }
    }
    $data['sound'] = phonetic_key($data['lastname']);
    $add_comment = $data['add_comment'];
    if ($add_comment != '') {
      $datecomment = of_isodate_convert($data['datecomment']);
      $usercomment = $data['usercomment'];
      $data['comment'] = "\n$datecomment:$usercomment:$add_comment";
    }
    $add_comment2 = $data['add_comment'];
    if ($add_comment2 != '') {
      $datecomment2 = of_isodate_convert($data['datecomment2']);
      $usercomment2 = $data['usercomment2'];
      $data['comment2'] = "\n$datecomment2:$usercomment2:$add_comment2";
    }
    $add_comment3 = $data['add_comment3'];
    if ($add_comment3 != '') {
      $datecomment3 = of_isodate_convert($data['datecomment3']);
      $usercomment3 = $data['usercomment3'];
      $data['comment3'] = "\n$datecomment3:$usercomment3:$add_comment3";
    }
    $data['mailok'] = ($data['mailok'] == '1' ? '1' : '0');
    $data['newsletter'] = ($data['newsletter'] == '1' ? '1' : '0');
    $data['archive'] = ($data['archive'] == '1' ? '1' : '0');
    if (empty($data['datasource_id'])) $data['datasource_id'] = $data['datasource'];

    $contact = new OBM_Contact;
    $contact->lastname  = $data['lastname'];
    $contact->firstname = $data['firstname'];
    $fields = array('mname','kind','title','function','company_id','company',
      'market_id','suffix','aka','sound','manager','assistant','spouse','category',
      'service','mailok','newsletter','archive','comment','comment2','comment3',
      'origin'
    );
    foreach($fields as $field) {
      $contact->$field = $data[$field];
    }
    $date_fields = array('date','birthday','anniversary');
    foreach($date_fields as $field) {
      $date = of_isodate_convert($data[$field], true);
      $contact->$field = (!empty($date) ? new Of_Date($date) : null);
    }
    $contact->phone   = is_array($data['phones'])   ? $data['phones']    : array();
    $contact->email   = is_array($data['emails'])   ? $data['emails']    : array();
    $contact->address = is_array($data['addresses'])? $data['addresses'] : array();
    $contact->im      = is_array($data['ims'])      ? $data['ims']       : array();
    $contact->website = is_array($data['websites']) ? $data['websites']  : array();
    $comp_id = sql_parse_id($contact->company_id);
    $dsrc    = sql_parse_id($contact->datasource_id);
    $kind    = sql_parse_id($contact->kind);
    $market_id  = sql_parse_id($contact->market_id);
    $func    = sql_parse_id($contact->function);
    $contact->addressbook_id = $addressbook->id;

    $date = ($contact->date ? "'{$contact->date}'" : 'null');

    $query = "INSERT INTO Contact (contact_timeupdate,
      contact_timecreate,
      contact_userupdate,
      contact_usercreate,
      contact_domain_id,
      contact_datasource_id,
      contact_company_id,
      contact_company,
      contact_kind_id,
      contact_marketingmanager_id,
      contact_lastname,
      contact_firstname,
      contact_middlename,
      contact_suffix,
      contact_aka,
      contact_sound,
      contact_manager,
      contact_assistant,
      contact_spouse,
      contact_category,
      contact_service,
      contact_function_id,
      contact_title,
      contact_mailing_ok,
      contact_newsletter,
      contact_archive,
      contact_date,
      contact_comment,
      contact_comment2,
      contact_comment3,
      contact_origin,
      contact_addressbook_id
    ) VALUES (
      NOW(),
      NOW(),
      $uid,
      $uid,
      $domain_id,
      $dsrc,
      $comp_id,
      '{$contact->company}',
      $kind,
      $market_id,
      '{$contact->lastname}',
      '{$contact->firstname}',
      '{$contact->mname}',
      '{$contact->suffix}',
      '{$contact->aka}',
      '{$contact->sound}',
      '{$contact->manager}',
      '{$contact->assistant}',
      '{$contact->spouse}',
      '{$contact->category}',
      '{$contact->service}',
      $func,
      '{$contact->title}',
      {$contact->mailok},
      {$contact->newsletter},
      {$contact->archive},
      $date,
      '{$contact->comment}',
      '{$contact->comment2}',
      '{$contact->comment3}',
      '{$GLOBALS['c_origin_web']}',
      '{$addressbook->id}'
    )";

    display_debug_msg($query, $cdg_sql, 'OBM_Contact:create(1)');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);

    $contact->id = $obm_q->lastid();
    if ($contact->id > 0) {
      if (($cgp_show['module']['company']) && ($retour)) {
        run_query_global_company_contact_number_update($comp_id);
      }
      $contact->entity_id = of_entity_insert('contact',$contact->id);

      // Birthday & Anniversary support
      //FIXME: do it better
      self::storeAnniversary('birthday', $contact->id, $uid, null, $contact->display_name(), null, $contact->birthday);
      self::storeAnniversary('anniversary', $contact->id, $uid, null, $contact->display_name(), null, $contact->anniversary);

      //FIXME: do it better
      self::storeCoords($contact);
      of_userdata_query_update('contact', $contact->id, $data);
    }

    OBM_AddressBook::timestamp($addressbook->id);
    
    $ret = OBM_Contact::get($contact->id);

    // Indexing Contact
    self::solrStore($ret);

    return $ret;
  }

  public function solrStore($contact) {

    $doc = new Apache_Solr_Document();

    $doc->setField('id', $contact->id);
    $doc->setField('timecreate', $contact->timecreate->format('Y-m-d\TH:i:s\Z'));
    $doc->setField('timeupdate', $contact->timeupdate->format('Y-m-d\TH:i:s\Z'));
    $doc->setField('usercreate', $contact->usercreate);
    $doc->setField('userupdate', $contact->userupdate);
    $doc->setField('datasource', $contact->datasource_id);
    $doc->setField('domain', $GLOBALS['obm']['domain_id']);
    $doc->setField('in', $contact->addressbook);
    $doc->setField('addressbookId', $contact->addressbook_id);
    $doc->setField('company', $contact->company);
    $doc->setField('companyId', $contact->company_id);
    $doc->setField('lastname', $contact->lastname);
    $doc->setField('firstname', $contact->firstname);
    $doc->setField('middlename', $contact->mname);
    $doc->setField('suffix', $contact->suffix);
    $doc->setField('aka', $contact->aka);
    $doc->setField('kind', $contact->kind);
    //$doc->setField('kind', $db->f('kind_header'));
    $doc->setField('manager', $contact->manager);
    $doc->setField('assistant', $contact->assistant);
    $doc->setField('spouse', $contact->spouse);
    $doc->setField('birthdayId', $contact->birthday_event);
    $doc->setField('anniversaryId', $contact->anniversary_event);
    if($contact->birthday) $doc->setField('birthday', $contact->birthday->format('Y-m-d\TH:i:s\Z'));
    if($contact->anniversary) $doc->setField('anniversary', $contact->anniversary->format('Y-m-d\TH:i:s\Z'));
    $doc->setField('category', $contact->category);
    foreach($contact->categories as $category) {
      foreach($category as $c) {
        $doc->setMultiValue('categoryId', $c['id']);
      }
    }
    $doc->setField('service', $contact->service);
    $doc->setField('function', $contact->function);
    $doc->setField('title', $contact->title);
    if ($contact->archive) {
      $doc->setField('is', 'archive');
    }
    if ($contact->collected) {
      $doc->setField('is', 'collected');
    }
    if ($contact->mailok) {
      $doc->setField('is', 'mailing');
    }        
    if ($contact->newsletter) {
      $doc->setField('is', 'newsletter');
    }
    if($contact->date) $doc->setField('date', $contact->date->format('Y-m-d\TH:i:s\Z'));
    $doc->setField('comment', $contact->comment);
    $doc->setField('comment2', $contact->comment2);
    $doc->setField('comment3', $contact->comment3);
    $doc->setField('from', $contact->origin);

    foreach($contact->email as $email) {
      $doc->setMultiValue('email', $email['address']);
    }

    foreach($contact->phone as $phone) {
      $doc->setMultiValue('phone', $phone['number']);
    }

    foreach($contact->im as $im) {
      $doc->setMultiValue('jabber', $im['address']);
    }

    foreach($contact->address as $address) {
      $doc->setMultiValue('street', $address['street']);
      $doc->setMultiValue('zipcode', $address['zipcode']);
      $doc->setMultiValue('expresspostal', $address['expresspostal']);
      $doc->setMultiValue('town', $address['town']);
      $doc->setMultiValue('country', $address['country']);
    }

    OBM_IndexingService::store('contact', array($doc));
  }
  
  public static function store($contact) {
    global $obm, $cgp_show, $cdg_sql;

    if (!$contact->id) return false;

    $now = date('Y-m-d H:i:s');
    $uid = $obm['uid'];
    $multidomain = sql_multidomain('contact');

    // In case company module not used, to avoid postgres error
    $comp_id = sql_parse_id($contact->company_id);
    $dsrc    = sql_parse_id($contact->datasource_id);
    $kind    = sql_parse_id($contact->kind);
    $market_id  = sql_parse_id($contact->market_id);
    $func    = sql_parse_id($contact->function);

    $date = ($contact->date ? "'{$contact->date}'" : 'null');

    $sql_id = sql_parse_id($contact->id, true);
    $query = "UPDATE Contact SET
      contact_timeupdate='{$now}',
      contact_userupdate='{$uid}',
      contact_datasource_id=$dsrc,
      contact_company_id=$comp_id,
      contact_company='".addslashes($contact->company)."',
      contact_kind_id=$kind,
      contact_marketingmanager_id=$market_id,
      contact_lastname='".addslashes($contact->lastname)."',
      contact_firstname='".addslashes($contact->firstname)."',
      contact_middlename='".addslashes($contact->mname)."',
      contact_suffix='".addslashes($contact->suffix)."',
      contact_aka='".addslashes($contact->aka)."',
      contact_sound='".addslashes($contact->sound)."',
      contact_manager='{$contact->manager}',
      contact_assistant='".addslashes($contact->assistant)."',
      contact_spouse='".addslashes($contact->spouse)."',
      contact_category='".addslashes($contact->category)."',
      contact_service='".addslashes($contact->service)."',
      contact_function_id=$func,
      contact_title='".addslashes($contact->title)."',
      contact_mailing_ok={$contact->mailok},
      contact_newsletter={$contact->newsletter},
      contact_archive={$contact->archive},
      contact_date=$date,
      contact_comment='".addslashes($contact->comment)."',
      contact_comment2='".addslashes($contact->comment2)."',
      contact_comment3='".addslashes($contact->comment3)."',
      contact_origin='{$GLOBALS['c_origin_web']}'
    WHERE contact_id $sql_id 
      $multidomain";

    display_debug_msg($query, $cdg_sql, 'OBM_Contact::store()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);

    if ($cgp_show['module']['company']) {
      // If company has changed, update the companies contact number
      if (($retour) && (!empty($contact->old_company_id)) && ($contact->company_id!=$contact->old_company_id)) {
        run_query_global_company_contact_number_update($contact->old_company_id);
        run_query_global_company_contact_number_update($comp_id);
      }
    }

    // Birthday & Anniversary support
    //FIXME: do it better
    self::storeAnniversary('birthday', $contact->id, $uid, $contact->birthday_event, $contact->display_name(), $contact->old_birthday, $contact->birthday);
    self::storeAnniversary('anniversary', $contact->id, $uid, $contact->anniversary_event, $contact->display_name(), $contact->old_anniversary, $contact->anniversary);

    if ($retour) {
      $ret = of_userdata_query_update('contact', $contact->id, $contact->categories);
      self::storeCoords($contact);
    }
    OBM_AddressBook::timestamp($contact->addressbook_id);

    // Indexing Contact
    self::solrStore($contact);
    OBM_IndexingService::commit('contact');

    return $contact;
  }

  public function copy($contact, $addressbook) {
    foreach(self::$fields as $field => $metadata) {
      $data[$field] = addslashes($contact->$field);
    }
    $data['archive'] = 0;
    $data['phones'] = array();
    foreach($contact->phone as $phone) {
      array_push($data['phones'], array('label'=>$phone['label'][0]."_".$phone['label']['1'], 'number'=>$phone['number']));
    }
    $data['emails'] = array();
    foreach($contact->email as $email) {
      array_push($data['emails'], array('label'=>$email['label'][0], 'address'=>$email['address']));
    }
    $data['ims'] = $contact->im;

    $data['websites'] = array();
    foreach($contact->website as $website) {
      array_push($data['websites'], array('label'=>$website['label'][0], 'url'=>$website['url']));
    }
    $data['addresses'] = array();
    foreach($contact->address as $address) {
      array_push($data['addresses'], array(
        'street' => addslashes($address['street']),
        'label' => $address['label'][0],
        'zipcode' => addslashes($address['zipcode']),
        'town' => addslashes($address['town']),
        'expresspostal' => $address['expresspostal'],
        'country' => $address['country']
      ));
    }
    $ret = self::create($data,$addressbook);
    OBM_IndexingService::commit('contact');
    return $ret;
  }

  public function move($contact, $addressbook) {
    if (!$contact->id) return false;

    $now = date('Y-m-d H:i:s');
    $uid = $GLOBALS['obm']['uid'];
    $sql_id = sql_parse_id($contact->id, true);
    $multidomain = sql_multidomain('contact');

    $query = "UPDATE Contact SET
      contact_timeupdate='{$now}',
      contact_userupdate='{$uid}',
      contact_addressbook_id='{$addressbook->id}'
    WHERE contact_id $sql_id $multidomain";

    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $contact = OBM_Contact::get($contact->id);

    OBM_AddressBook::timestamp($contact->addressbook_id);

    // Indexing Contact
    self::solrStore($contact);
    OBM_IndexingService::commit('contact');
    
    return $contact;
  }

  public static function delete($contact) {
    global $obm, $cdg_sql, $c_use_connectors;
    if (!$contact->id) return false;
    //else
    $obm_q = new DB_OBM;
  
    $multidomain = sql_multidomain('contact');
    $sql_id = sql_parse_id($contact->id);
    $comp_id = sql_parse_id($contact->company_id);
  
    run_query_global_delete_document_links($contact->id, 'contact');    
    $ret = of_userdata_query_delete('contact', $contact->id);
  
    //FIXME: do it better
    // BEGIN birthday and anniversary support
    run_query_contact_birthday_update('birthday', null, null, $contact->birthday_event, null, null, null);
    run_query_contact_birthday_update('anniversary', null, null, $contact->anniversary_event, null, null, null);
    // END birthday and anniversary support

    of_entity_delete('contact', $contact->id);
     
    $query = "DELETE FROM Contact WHERE contact_id = $sql_id $multidomain";
    display_debug_msg($query, $cdg_sql, 'OBM_Contact::delete(1)');
    $retour = $obm_q->query($query);
    OBM_AddressBook::timestamp($contact->addressbook_id);
    // If connectors in use
    if ($c_use_connectors) {
      $uid = sql_parse_id($obm['uid']);
      $query = "INSERT INTO DeletedContact (
          deletedcontact_contact_id,
          deletedcontact_addressbook_id,
          deletedcontact_timestamp,
          deletedcontact_origin)
          VALUES (
            $contact->id, 
            $contact->addressbook_id,
            NOW(),
            '$GLOBALS[c_origin_web]' 
          )
          ";
      display_debug_msg($query, $cdg_sql, 'OBM_Contact::delete(2)');
      $retour = $obm_q->query($query);
    }
  
    // After contact deletion to get correct number
    run_query_global_company_contact_number_update($comp_id);

    // Delete index
    OBM_IndexingService::delete('contact', $contact->id);  

    return $retour;
  }

  public static function import($vcard) {
    $contact = array(
      'lastname' => addslashes($vcard->name->family),
      'firstname' => addslashes($vcard->name->given),
      'function' => addslashes($vcard->role),
      'title' => addslashes($vcard->title),
      'addresses' => array(),
      'phones' => array(),
      'emails' => array(),
      'ims' => array(),
      'websites' => array()
    );
    $id = $vcard->getValue('x-obm-uid');
    if ($id !== null) {
      $contact['contact_id'] = $id;
    }
    if (!empty($vcard->name->prefixes)) {
      $kind_id = self::getKindId($vcard->name->prefixes);
      if ($kind_id !== false) {
        $contact['kind'] = $kind_id;
      }
    }

    // x-obm-* (OBM specific fields)
    $obmSpecificFields = array('mname','company_id','company','market_id','suffix',
      'aka','sound','manager','assistant','spouse','category','service',
      'mailok','newsletter', 'comment');
    foreach ($obmSpecificFields as $field) {
      $value = $vcard->getValue("x-obm-{$field}");
      if (!empty($value)) {
        $contact[$field] = addslashes($value);
      }
    }
    $date = $vcard->getValue("x-obm-date");
    if (!empty($date)) {
      $contact['date'] = $date;
    }
    $anniversary = $vcard->getValue("x-obm-anniversary");
    if (!empty($anniversary)) {
      $contact['anniversary'] = $anniversary;
    }

    if ($vcard->bday !== null) {
      $contact['birthday'] = $vcard->bday->format(Of_date::DATE_ISO);
    }
    foreach ($vcard->addresses as $add) {
      $location = $add->location[0];
      if (empty($location)) $location = "OTHER";
      $contact['addresses'][] = array(
        'street' => addslashes($add->street),
        'label' => strtoupper($location),
        'zipcode' => addslashes($add->postalcode),
        'town' => addslashes($add->locality),
        'expresspostal' => addslashes($add->pobox),
        'country' => addslashes($add->country)
      );
    }
    foreach ($vcard->phones as $ph) {
      $location = $ph->location[0];
      if (empty($location)) $location = "OTHER";
      $contact['phones'][] = array(
        'number' => addslashes($ph->value),
        'label' => strtoupper($location)
      );
    }
    foreach ($vcard->emails as $em) {
      $location = $em->location[0];
      if (empty($location)) $location = "OTHER";
      $contact['emails'][] = array(
        'address' => $em->value,
        'label' => strtoupper($location)
      );
    }
    foreach ($vcard->getFieldsByName('IMPP') as $im) {
      $protocol = $im->getParam('TYPE');
      if (empty($protocol)) $protocol = "OTHER";
      $contact['ims'][] = array(
        'protocol' => $protocol,
        'address' => addslashes($im->value())
      );
    }
    foreach ($vcard->getFieldsByName('URL') as $www) {
      $label = $www->getParam('TYPE');
      if (empty($label)) $label = "OTHER";
      $contact['websites'][] = array(
        'label' => $label,
        'url' => $www->value()
      );
    }
    return $contact;
  }
  
  public function toVcard() {
    $name = new Vpdi_VCard_Name();
    $name->family = $this->lastname;
    $name->given  = $this->firstname;
    if (!empty($this->kind)) {
      $name->prefixes = $this->kind;
    }
    
    $card = new Vpdi_VCard();
    $card->setName($name);
    $card->addField(new Vpdi_Field('x-obm-uid', $this->id));
    
    $simpleFields = array('function' => 'role', 'title' => 'title');
    foreach ($simpleFields as $db => $v) {
      if (!empty($this->$db)) {
        $card->addField(new Vpdi_Field($v, $this->$db));
      }
    }

    // x-obm-* (OBM specific fields)
    $obmSpecificFields = array('mname','company_id','company','market_id','suffix',
      'aka','sound','manager','assistant','spouse','category','service',
      'mailok','newsletter', 'comment');
    foreach ($obmSpecificFields as $field) {
      if (!empty($this->$field)) {
        $card->addField(new Vpdi_Field("x-obm-{$field}", str_replace("\r\n", "\n ", (trim($this->$field)))));        
      }
    }
    if (!empty($this->date)) {
      $card->addField(new Vpdi_Field('x-obm-date', $this->date->get(Of_date::DATE_ISO)));
    }
    if (!empty($this->anniversary)) {
      $card->addField(new Vpdi_Field('x-obm-anniversary', $this->anniversary->get(Of_date::DATE_ISO)));
    }

    //$card->addField(new Vpdi_Field('org', $this->company));
    
    if (!empty($this->birthday)) {
      $card->addField(new Vpdi_Field('bday', $this->birthday->get(Of_date::DATE_ISO)));
    }

    foreach ($this->phone as $phone) {
      $ph = new Vpdi_VCard_Phone($phone['number']);
      $ph->location = $phone['label'];
      $card->addPhone($ph);
    }
    foreach ($this->email as $email) {
      $em = new Vpdi_VCard_Email($email['address']);
      $em->location = $email['label'];
      $card->addEmail($em);
    }
    foreach ($this->address as $address) {
      $ad = new Vpdi_VCard_Address();
      $ad->location = $address['label'];
      $ad->street = str_replace("\r", "\n", str_replace("\r\n", "\n", (trim($address['street']))));
      $ad->postalcode = $address['zipcode'];
      $ad->pobox = $address['expresspostal'];
      $ad->locality = $address['town'];
      $ad->country = $address['country'];
      $card->addAddress($ad);
    }
    foreach ($this->im as $im) {
      $card->addField(new Vpdi_Field('IMPP', $im['address'], array('TYPE' => $im['protocol'])));
    }
    foreach ($this->website as $www) {
      $card->addField(new Vpdi_Field('URL', $www['url'], array('TYPE' => $www['label'])));
    }
    
    return $card;
  }

  public function toCsv() {
    $csv = array();
    $fields = array_keys(get_display_pref($GLOBALS['obm']['uid'], 'contact'));
    foreach($fields as $field) {
      if (!is_array($this->$field)){
        if ($field == "country") {
          array_push($csv, "\"".$this->address[0]['country']."\"");
        } else if($field == "cellvoice") {
          $cellvoice = "";
          foreach($this->phone as $phone) {
            if($phone['label'][0] == 'CELL' && $phone['label'][1] == 'VOICE') {
              $cellvoice = $phone['number'];
            }
          }
          array_push($csv, "\"".$cellvoice."\"");
        } else if($field == "workvoice") {
          $workvoice = "";
          foreach($this->phone as $phone) {
            if($phone['label'][0] == 'WORK' && $phone['label'][1] == 'VOICE') {
              $workvoice = $phone['number'];
            }
          }
          array_push($csv, "\"".$workvoice."\"");
        } else if($field == "workfax") {
          $workfax = "";
          foreach($this->phone as $phone) {
            if($phone['label'][0] == 'WORK' && $phone['label'][1] == 'FAX') {
              $workfax = $phone['number'];
            }
          }
          array_push($csv, "\"".$workfax."\"");
        } else if($field == "homevoice") {
          $homevoice = "";
          foreach($this->phone as $phone) {
            if($phone['label'][0] == 'HOME' && $phone['label'][1] == 'VOICE') {
              $homevoice = $phone['number'];
            }
          }
          array_push($csv, "\"".$homevoice."\"");
        } else {
          array_push($csv,"\"".$this->$field."\"");
        }
      } else {
        if ($field == "email") {
          array_push($csv,"\"".$this->email[0]['address']."\"");
        } else if ($field == "address") {
          array_push($csv,"\"".$this->address[0]['street']." ".
            $this->address[0]['zipcode']." ".$this->address[0]['expresspostal']." ".
            $this->address[0]['town']." ".$this->address[0]['country']."\"");
        }
      }
    }
    return implode(";", $csv);
  }
  
  private static function fetchDetails($db, $where) {
    $contacts = array();

    if (!$where) {
      return $contacts;
    }

    $db_type = $db->type;
    $join = of_userdata_join_query('Contact');
    $query = "SELECT contact_id,
      contact_timecreate,
      contact_timeupdate,
      contact_usercreate,
      contact_userupdate,
      contact_lastname,
      contact_firstname,
      contact_middlename,
      kind_minilabel as contact_kind,
      kind_lang as contact_language,
      kind_header as contact_header,
      contact_kind_id,
      contact_title,
      contact_function_id,
      contactfunction_label as contact_function,
      contact_company_id,
      contact_company,
      company_name,
      userobm_lastname as market_lastname,
      userobm_firstname as market_firstname,
      contact_marketingmanager_id,
      contact_suffix,
      contact_aka,
      contact_sound,
      contact_manager,
      contact_assistant,
      contact_spouse,
      contact_category,
      contact_service,
      contact_mailing_ok,
      contact_newsletter,
      contact_archive,
      contact_collected,
      contact_date,
      contact_addressbook_id,
      AddressBook.name,
      bd.event_id as contact_birthday_event,
      bd.event_date as contact_birthday,
      an.event_id as contact_anniversary_event,
      an.event_date as contact_anniversary,
      contact_datasource_id,
      datasource_name,
      contact_comment,
      contact_comment2,
      contact_comment3,
      contact_origin
    FROM Contact
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
    INNER JOIN AddressBook ON AddressBook.id = contact_addressbook_id
    LEFT JOIN UserObm ON userobm_id = contact_marketingmanager_id
    LEFT JOIN DataSource ON contact_datasource_id = datasource_id
    LEFT JOIN Address ON address_entity_id = contactentity_entity_id 
    LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='FR' 
    LEFT JOIN Company ON contact_company_id = company_id
    LEFT JOIN Kind ON kind_id = contact_kind_id
    LEFT JOIN Event as bd ON contact_birthday_id = bd.event_id
    LEFT JOIN Event as an ON contact_anniversary_id = an.event_id
    LEFT JOIN ContactFunction ON contact_function_id = contactfunction_id
    $join
    WHERE $where
    GROUP BY contact_id,
      contact_timecreate,
      contact_timeupdate,
      contact_usercreate,
      contact_userupdate,
      contact_lastname,
      contact_firstname,
      contact_middlename,
      kind_minilabel,
      kind_lang,
      kind_header,
      contact_kind_id,
      contact_title,
      contactfunction_label,
      contact_company_id,
      contact_company,
      company_name,
      contact_function_id,
      userobm_lastname,
      userobm_firstname,
      contact_marketingmanager_id,
      contact_suffix,
      contact_aka,
      contact_sound,
      contact_manager,
      contact_assistant,
      contact_spouse,
      contact_category,
      contact_service,
      contact_mailing_ok,
      contact_newsletter,
      contact_archive,
      contact_collected,
      contact_date,
      contact_addressbook_id,
      AddressBook.name,
      bd.event_id,
      bd.event_date,
      an.event_id,
      an.event_date,
      contact_datasource_id,
      datasource_name,
      contact_comment,
      contact_comment2,
      contact_comment3,
      contact_origin
    ORDER BY contact_lastname, contact_firstname";

    $db->xquery($query);
    while ($db->next_record()) {
      $contact = new OBM_Contact;
      $contact->id            = $db->f('contact_id');
      $contact->timecreate    = new Of_Date($db->f('contact_timecreate'));
      $contact->timeupdate    = new Of_Date($db->f('contact_timeupdate'));
      $contact->usercreate    = $db->f('contact_usercreate');
      $contact->userupdate    = $db->f('contact_userupdate');
      $contact->lastname      = $db->f('contact_lastname');
      $contact->firstname     = $db->f('contact_firstname');
      $contact->displayname   = __('%lastname% %firstname%', array('%lastname%' => $db->f('contact_lastname'), '%firstname%' => $db->f('contact_firstname')));
      $contact->mname         = $db->f('contact_middlename');
      $contact->kind_id       = $db->f('contact_kind_id');
      $contact->kind          = $db->f('contact_kind');
      $contact->language      = $db->f('contact_language');
      $contact->header        = $db->f('contact_header');
      $contact->title         = $db->f('contact_title');
      $contact->function_id   = $db->f('contact_function_id');
      $contact->function      = $db->f('contact_function');
      $contact->company_id    = $db->f('contact_company_id');
      if($contact->company_id) {
        $contact->company       = $db->f('company_name');
      } else {
        $contact->company       = $db->f('contact_company');
      }
      $contact->market_id     = $db->f('contact_marketingmanager_id');
      $contact->market        = $db->f('market_lastname').' '.$db->f('market_firstname');
      $contact->suffix        = $db->f('contact_suffix');
      $contact->aka           = $db->f('contact_aka');
      $contact->sound         = $db->f('contact_sound');
      $contact->manager       = $db->f('contact_manager');
      $contact->assistant     = $db->f('contact_assistant');
      $contact->spouse        = $db->f('contact_spouse');
      $contact->category      = $db->f('contact_category');
      $contact->service       = $db->f('contact_service');
      $contact->mailok        = $db->f('contact_mailing_ok');
      $contact->newsletter    = $db->f('contact_newsletter');
      $contact->archive       = $db->f('contact_archive');
      $contact->collected     = $db->f('contact_collected');
      $contact->datasource_id = $db->f('contact_datasource_id');
      $contact->datasource    = $db->f('datasource_name');
      $contact->comment       = $db->f('contact_comment');
      $contact->comment2      = $db->f('contact_comment2');
      $contact->comment3      = $db->f('contact_comment3');
      $contact->origin        = $db->f('contact_origin');
      $contact->addressbook_id= $db->f('contact_addressbook_id');
      $contact->addressbook   = $db->f('name');
      if ($db->f('contact_date') && $db->f('contact_date') != '0000-00-00 00:00:00') {
        $contact->date        = new Of_Date($db->f('contact_date'), 'GMT');
      }
      if ($db->f('contact_birthday') && $db->f('contact_birthday') != '0000-00-00 00:00:00') {
        $contact->birthday_event = $db->f('contact_birthday_event');
        $contact->birthday    = new Of_Date($db->f('contact_birthday'), 'GMT');
      }
      if ($db->f('contact_anniversary') && $db->f('contact_anniversary') != '0000-00-00 00:00:00') {
        $contact->anniversary_event = $db->f('contact_anniversary_event');
        $contact->anniversary = new Of_Date($db->f('contact_anniversary'), 'GMT');
      }
      $contacts[$contact->id] = $contact;
    }
    return $contacts;
  }

  private static function fetchCoords($db, $contacts) {
    $contact_ids = implode(',', array_keys($contacts));
    $query = "SELECT contactentity_contact_id AS contact_id, phone_label, phone_number FROM Phone 
              INNER JOIN ContactEntity ON phone_entity_id = contactentity_entity_id 
              WHERE contactentity_contact_id IN ({$contact_ids})  ";
    $query.= "ORDER BY phone_label";
    $db->xquery($query);        
    while ($db->next_record()) {
      $label = (explode(';', $db->f('phone_label')));
      $contacts[$db->f('contact_id')]->phone[] = array('label' => $label, 'number' => $db->f('phone_number'));
    }
    
    $query = "SELECT contactentity_contact_id AS contact_id, email_label, email_address FROM Email 
              INNER JOIN ContactEntity ON email_entity_id = contactentity_entity_id 
              WHERE contactentity_contact_id IN ({$contact_ids})  ";
    $query.= "ORDER BY email_label";
    $db->xquery($query);        
    while ($db->next_record()) {
      $label = (explode(';', $db->f('email_label')));
      $contacts[$db->f('contact_id')]->email[] = array('label' => $label, 'address' => $db->f('email_address'));
    }
    
    $lang = get_lang();
    $query = "SELECT contactentity_contact_id AS contact_id, address_label, address_street, address_zipcode, address_expresspostal, 
              address_town, address_country, country_name, country_iso3166
              FROM Address 
              INNER JOIN ContactEntity ON address_entity_id = contactentity_entity_id 
              LEFT JOIN Country ON country_iso3166 = address_country AND country_lang = '$lang' 
              WHERE contactentity_contact_id IN ({$contact_ids})  ";
    $query.= "ORDER BY address_label";
    $db->xquery($query);        
    while ($db->next_record()) {
      $label = (explode(';',$db->f('address_label')));
      $contacts[$db->f('contact_id')]->address[] = array(
        'label' => $label, 'street' => $db->f('address_street'), 'zipcode' => $db->f('address_zipcode'),
        'expresspostal' => $db->f('address_expresspostal'), 'town' => $db->f('address_town'), 
        'address_country' => $db->f('address_country'),
        'country' => $db->f('country_name'), 'country_iso3166' => $db->f('country_iso3166'));
    }
    
    $query = "SELECT contactentity_contact_id AS contact_id, IM.* FROM IM 
              INNER JOIN ContactEntity ON im_entity_id = contactentity_entity_id 
              WHERE  contactentity_contact_id IN ({$contact_ids})";
    $db->xquery($query);        
    while ($db->next_record()) {
      $contacts[$db->f('contact_id')]->im[] = array('protocol' => $db->f('im_protocol'),'address' => $db->f('im_address'));
    }
    
    $query = "SELECT contactentity_contact_id AS contact_id, website_label, website_url FROM Website 
              INNER JOIN ContactEntity ON website_entity_id = contactentity_entity_id 
              WHERE contactentity_contact_id IN ({$contact_ids}) ";
    $query .= "ORDER BY website_label";
    $db->xquery($query);        
    while ($db->next_record()) {
      $label = (explode(';', $db->f('website_label')));
      $contacts[$db->f('contact_id')]->website[] = array('label' => $label, 'url' => $db->f('website_url'));
    }
    
    return $contacts;
  }


  private static function storeCoords($contact) {
    global $cdg_sql;

    $id = sql_parse_id($contact->__get('entity_id'));
    $obm_q = new DB_OBM;
  
    $query = "DELETE FROM Phone WHERE phone_entity_id = $id";
    display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(phone)');
    $obm_q->query($query);
    if(is_array($contact->phone)) {
      $cpt = array();
      foreach($contact->phone as $phone) {
        if(trim($phone['number']) != '' ) {
          if(is_array($phone['label'])) {
            array_pop($phone['label']);
            $phone['label'] = implode(';',$phone['label']); 
          } else {
            $phone['label'] = str_replace('_', ';', $phone['label']);
          }
          $cpt[$phone['label']]++;
          $query = "INSERT INTO Phone (phone_entity_id, phone_number, phone_label) VALUES ($id, '$phone[number]', '$phone[label];X-OBM-Ref".$cpt[$phone['label']]."')";
          display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(phone)');
          $obm_q->query($query);
        }
      }
    }
  
    $query = "DELETE FROM Address WHERE address_entity_id = $id";
    display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(address)');
    $obm_q->query($query);
    if(is_array($contact->address)) {
      $cpt = array();
      foreach($contact->address as $address) {
        if(trim($address['country_iso3166']) == 'none') $address['country_iso3166'] = '';
        if(trim($address['country_iso3166']) == '' &&  trim($address['country']) != '') {
          $address['country_iso3166'] = self::getCountryIso3166($address['country']);
        }
        if(trim($address['street']) != '' || trim($address['country_iso3166']) != '' 
          || trim($address['zipcode']) != '' || trim($address['expresspostal']) != '') {
          if(is_array($address['label'])) {
            array_pop($address['label']);
            $address['label'] = implode(';',$address['label']); 
          } else {
            $address['label'] = str_replace('_', ';', $address['label']);
          }          
          $cpt[$address['label']]++;
          $query = "INSERT INTO Address (
            address_entity_id,
            address_street,
            address_zipcode,
            address_town,
            address_expresspostal,
            address_country,
            address_label
          ) VALUES (
            $id, 
            '$address[street]',
            '$address[zipcode]',
            '$address[town]',
            '$address[expresspostal]',
            '$address[country_iso3166]',
            '$address[label];X-OBM-Ref".$cpt[$address['label']]."'
          )";
          display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(address)');
          $obm_q->query($query);
        }
      }
    }
  
    $query = "DELETE FROM Website WHERE website_entity_id = $id";
    display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(website)');
    $obm_q->query($query);
  
    if(is_array($contact->website)) {
      $cpt = array();
      foreach($contact->website as $website) {
        if(trim($website['url']) != '' ) {
          if(is_array($website['label'])) {
            array_pop($website['label']);
            $website['label'] = implode(';',$website['label']); 
          } else {
            $website['label'] = str_replace('_', ';', $website['label']);
          }              
          $cpt[$website['label']]++;
          $query = "INSERT INTO Website (website_entity_id, website_url, website_label) VALUES ($id, '$website[url]', '$website[label];X-OBM-Ref".$cpt[$website['label']]."')";
          display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(website)');
          $obm_q->query($query);
        }
      }
    }
  
    $query = "DELETE FROM IM WHERE im_entity_id = $id";
    display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(IM)');
    $obm_q->query($query);
  
    if(is_array($contact->im)) {
      $cpt = array();
      foreach($contact->im as $im) {
        if(trim($im['address']) != '' ) {
          $cpt[$im['protocol']]++;          
          $query = "INSERT INTO IM (im_entity_id, im_address, im_protocol, im_label) VALUES ($id, '$im[address]', '$im[protocol]', '$im[protocol];X-OBM-Ref".$cpt[$im['protocol']]."')";
          display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(IM)');
          $obm_q->query($query);
        }
      }
    }

    $query = "DELETE FROM Email WHERE email_entity_id = $id";
    display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(Email)');
    $obm_q->query($query);
  
    if(is_array($contact->email)) {
      $cpt = array();
      foreach($contact->email as $email) {
        if(trim($email['address']) != '' ) {
          if(is_array($email['label'])) {
            array_pop($email['label']);
            $email['label'] = implode(';',$email['label']); 
          } else {
            $email['label'] = str_replace('_', ';', $email['label']);
          }                
          $cpt[$email['label']]++;
          $query = "INSERT INTO Email (email_entity_id, email_address, email_label) VALUES ($id, '".trim($email['address'])."', '$email[label];X-OBM-Ref".$cpt[$email['label']]."')";
          display_debug_msg($query, $cdg_sql, 'OBM_Contact::storeCoords(Email)');
          $obm_q->query($query);
        }
      }
    }
  }

  private static function fetchCategories($db, $contacts) {
    $contact_ids = implode(',', array_keys($contacts));
    $query = "SELECT contactentity_contact_id, category_category, category_code, category_id, category_label FROM CategoryLink
      INNER JOIN Category ON categorylink_category_id = category_id
      INNER JOIN ContactEntity ON contactentity_entity_id = categorylink_entity_id
      WHERE contactentity_contact_id IN ($contact_ids)
      $multidomain
      ORDER BY contactentity_contact_id, category_category, category_code, category_label";
    $db->xquery($query);        
    while ($db->next_record()) {
      $contacts[$db->f('contactentity_contact_id')]->categories[$db->f('category_category')][$db->f('category_id')] = array(
        'id' => $db->f('category_id'), 'code' => $db->f('category_code'), 'label' => $db->f('category_label'));
    }
    return $contacts;
  }

  private static function storeCategories($db, $contacts) {

  }

  private static function storeAnniversary($date='birthday', $contact_id, $contact_usercreate, $event_id, $contact_fullname, $old_value, $new_value) {
    global $cdg_sql, $obm;
    global $l_birthday_event_title, $l_anniversary_event_title;

    list($nope_event, $insert_event, $update_event, $delete_event) = array(0,1,2,3);
    if ($event_id == null) {
      if ($new_value != null) {
        $do = $insert_event;
      }
    } else {
      if ($new_value == null) {
        $do = $delete_event;
      } else if ($new_value->compare($old_value) != 0) {
        $do = $update_event;
      }
    }

    $obm_q = new DB_OBM;

    $multidomain_contact = sql_multidomain('contact');
    $multidomain_event = sql_multidomain('event');

    switch ($do) {
    case $insert_event:
      $duration = 3600*24;
      $label = ${"l_${date}_event_title"};
      $title = str_replace('\'', '\\\'', sprintf($label, $contact_fullname));
      $ext_id =  get_obm_info('product_id').sha1(uniqid()).sha1($GLOBALS['obm']['domain_name']).sha1(mktime()*rand());

      $query = "INSERT INTO Event
        (event_timeupdate,
        event_timecreate,
        event_usercreate,
        event_origin,
        event_owner,
        event_ext_id,
        event_timezone,
        event_title,
        event_date,
        event_description,
        event_properties,
        event_location,
        event_category1_id,
        event_priority,
        event_privacy,
        event_duration,
        event_repeatkind,
        event_repeatfrequence,
        event_repeatdays,
        event_allday,
        event_color,
        event_endrepeat,
        event_domain_id,
        event_opacity)
        VALUES
        (
         NOW(),
         NOW(),
        '$contact_usercreate',
        '$GLOBALS[c_origin_web]',
        '$contact_usercreate',
        '$ext_id',
        '".Of_Date::getOption('timezone')."',
        '$title',
        '$new_value',
        '',
        '',
        '',
        NULL,
        '2',
        '0',
        '$duration',
        'yearly',
        '1',
        '0000000',
        '1',
        '',
        NULL,
        '$obm[domain_id]',
        'TRANSPARENT')";

      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, 'run_query_contact_birthday_update(insert event)');

      $insert_event_id = $obm_q->lastid();
      if ($insert_event_id) {
        of_entity_insert('event', $insert_event_id);
        $sql_id = sql_parse_id($contact_id);
        $query = "UPDATE Contact
          SET contact_${date}_id = $insert_event_id
          WHERE
          contact_id = $sql_id
          $multidomain_contact";

        $obm_q->query($query);
        display_debug_msg($query, $cdg_sql, "run_query_contact_birthday_update(update birthday id)");
        $entity_id = of_entity_get('user', $contact_usercreate);
        $query = "INSERT INTO EventLink (
          eventlink_timecreate,
          eventlink_usercreate,
          eventlink_event_id, 
          eventlink_entity_id,
          eventlink_state) 
        VALUES (
          NOW(),
          $contact_usercreate,
          $insert_event_id,
          $entity_id,
          'ACCEPTED')";

        $obm_q->query($query);
        display_debug_msg($query, $cdg_sql, "run_query_contact_birthday_update(insert entity)");
      }

      break;

    case $update_event:
      $sql_id = sql_parse_id($event_id);
      $query = "UPDATE Event SET
        event_date = '$new_value',
        event_origin = '$GLOBALS[c_origin_web]'
      WHERE
        event_id = $sql_id
        $multidomain_event";

      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, 'run_query_contact_birthday_update(update event)');

      break;

    case $delete_event:
      of_entity_delete('event',$event_id);
      $sql_id = sql_parse_id($event_id);
      $query = "DELETE FROM Event WHERE event_id = $sql_id
        $multidomain_event";
      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, 'run_query_contact_birthday_update(delete event)');

      break;
    }
  }

  private static function getKindId($kind) {
    if (self::$kinds === null) {
      self::$kinds = self::fetchKinds();
    }
    return array_search($kind, self::$kinds);
  }
  
  private static function fetchKinds() {
    $kinds = array();
    $query = "SELECT kind_id, kind_minilabel FROM Kind";
    $db = new DB_OBM();
    $db->xquery($query);        
    while ($db->next_record()) {
      $kinds[$db->f('kind_id')] = $db->f('kind_minilabel');
    }
    return $kinds;
  }

  public static function labelToString($label, $kind=null, $translate=true, $separator=';') {
    if(is_array($label))array_pop($label);
    else $label = array($label);
    if($translate && $GLOBALS['l_'.strtolower($kind).'_labels'][implode('_',$label)]) {
      return $GLOBALS['l_'.strtolower($kind).'_labels'][implode('_',$label)];
    } else {
      return implode($separator,$label);
    }
  }

  public static function getCountryIso3166($name) {
    $obm_q = new DB_OBM;
    $query = "SELECT country_iso3166 FROM Country WHERE country_name #LIKE '".$name."' LIMIT 1";
    $obm_q->xquery($query);
    $obm_q->next_record();
    return $obm_q->f('country_iso3166');
  }

}
