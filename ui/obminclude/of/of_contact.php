<?php

require_once 'vpdi/vpdi.php';
require_once 'vpdi/field.php';
require_once 'vpdi/entity.php';
require_once 'vpdi/vcard.php';

class OBM_Contact {
  
  public $id;
  public $lastname;
  public $firstname;
  public $mname;//middlename
  public $kind;
  public $title;
  public $function;
  public $company_id;
  public $company;
  public $market;//marketingmanager_id
  public $suffix;
  public $aka;
  public $sound;
  public $manager;
  public $assistant;
  public $spouse;
  public $category;
  public $service;
  public $mailok;//mailing_ok
  public $newsletter;
  public $date;
  public $birthday;
  public $anniversary;
  public $phone = array();
  public $email = array();
  public $address = array();
  public $im = array();
  public $website = array();
 
  private static $kinds = null;
  
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
  
  public static function fetchAll($where) {
    $db = new DB_OBM();
    $contacts = self::fetchDetails($db, $where);
    if (count($contacts) != 0) {
      $contacts = self::fetchCoords($db, $contacts);
    }
    return $contacts;
  }
  
  public static function import($vcard) {
    $contact = array(
      'lname' => $vcard->name->family,
      'fname' => $vcard->name->given,
      'function' => $vcard->role,
      'title' => $vcard->title,
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
    $obmSpecificFields = array('mname','company_id','company','market','suffix',
      'aka','sound','manager','assistant','spouse','category','service',
      'mailok','newsletter');
    foreach ($obmSpecificFields as $field) {
      $value = $vcard->getValue("x-obm-{$field}");
      if (!empty($value)) {
        $contact[$field] = $value;
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
      $contact['addresses'][] = array(
        'street' => $add->street,
        'label' => strtoupper($add->location[0]),
        'zipcode' => $add->postalcode,
        'town' => $add->locality,
        'expresspostal' => $add->pobox,
        'country' => $add->country
      );
    }
    foreach ($vcard->phones as $ph) {
      $contact['phones'][] = array(
        'number' => $ph->value,
        'label' => strtoupper($ph->location[0])
      );
    }
    foreach ($vcard->emails as $em) {
      $contact['emails'][] = array(
        'address' => $em->value,
        'label' => strtoupper($em->location[0])
      );
    }
    foreach ($vcard->getFieldsByName('IMPP') as $im) {
      $contact['ims'][] = array(
        'protocol' => $im->getParam('TYPE'),
        'address' => $im->value()
      );
    }
    foreach ($vcard->getFieldsByName('URL') as $www) {
      $contact['websites'][] = array(
        'label' => $www->getParam('TYPE'),
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
    $obmSpecificFields = array('mname','company_id','company','market','suffix',
      'aka','sound','manager','assistant','spouse','category','service',
      'mailok','newsletter');
    foreach ($obmSpecificFields as $field) {
      if (!empty($this->$field)) {
        $card->addField(new Vpdi_Field("x-obm-{$field}", $this->$field));
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
      $ph->location[] = $phone['label'];
      $card->addPhone($ph);
    }
    foreach ($this->email as $email) {
      $em = new Vpdi_VCard_Email($email['address']);
      $em->location[] = $email['label'];
      $card->addEmail($em);
    }
    foreach ($this->address as $address) {
      $ad = new Vpdi_VCard_Address();
      $ad->location[] = $address['label'];
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
  
  private static function fetchDetails($db, $where) {

    $db_type = $db->type;
    $birthday = sql_date_format($db_type, 'bd.event_date', 'contact_birthday');
    $anniversary = sql_date_format($db_type, 'an.event_date', 'contact_anniversary');
    $contacts = array();

    $query = "SELECT contact_id,
      contact_lastname,
      contact_firstname,
      contact_middlename,
      kind_minilabel as contact_kind,
      contact_title,
      contactfunction_label as contact_function,
      contact_company_id,
      contact_company,
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
      contact_date,
      bd.event_date as contact_birthday,
      an.event_date as contact_anniversary
    FROM Contact
         LEFT JOIN Kind ON kind_id = contact_kind_id
         LEFT JOIN Event as bd ON contact_birthday_id = bd.event_id
         LEFT JOIN Event as an ON contact_anniversary_id = an.event_id
         LEFT JOIN ContactFunction ON contact_function_id = contactfunction_id
    WHERE {$where}";

    $db->query($query);
    while ($db->next_record()) {
      $contact = new OBM_Contact;
      $contact->id          = $db->f('contact_id');
      $contact->lastname    = $db->f('contact_lastname');
      $contact->firstname   = $db->f('contact_firstname');
      $contact->mname       = $db->f('contact_middlename');
      $contact->kind        = $db->f('contact_kind');
      $contact->title       = $db->f('contact_title');
      $contact->function    = $db->f('contact_function');
      $contact->company_id  = $db->f('contact_company_id');
      $contact->company     = $db->f('contact_company');
      $contact->market      = $db->f('contact_marketingmanager_id');
      $contact->suffix      = $db->f('contact_suffix');
      $contact->aka         = $db->f('contact_aka');
      $contact->sound       = $db->f('contact_sound');
      $contact->manager     = $db->f('contact_manager');
      $contact->assistant   = $db->f('contact_assistant');
      $contact->spouse      = $db->f('contact_spouse');
      $contact->category    = $db->f('contact_category');
      $contact->service     = $db->f('contact_service');
      $contact->mailok      = $db->f('contact_mailing_ok');
      $contact->newsletter  = $db->f('contact_newsletter');
      if ($db->f('contact_date'))
        $contact->date        = new Of_Date($db->f('contact_date'), 'GMT');
      if ($db->f('contact_birthday'))
        $contact->birthday    = new Of_Date($db->f('contact_birthday'), 'GMT');
      if ($db->f('contact_anniversary'))
        $contact->anniversary = new Of_Date($db->f('contact_anniversary'), 'GMT');
      $contacts[$contact->id] = $contact;
    }
    return $contacts;
  }
  
  private static function fetchCoords($db, $contacts) {
    $contact_ids = implode(',', array_keys($contacts));
    $query = "SELECT contactentity_contact_id AS contact_id, phone_label, phone_number FROM Phone 
              INNER JOIN ContactEntity ON phone_entity_id = contactentity_entity_id 
              WHERE contactentity_contact_id IN ({$contact_ids})  ";
    /*$query.= "UNION 
              SELECT contact_id, 'COMPANY' as phone_label, phone_number FROM Phone 
              INNER JOIN CompanyEntity ON phone_entity_id = companyentity_entity_id 
              INNER JOIN Contact ON contact_company_id = companyentity_company_id
              WHERE contact_id IN ({$contact_ids})  ";*/
    $query.= "ORDER BY phone_label";
    $db->query($query);        
    while ($db->next_record()) {
      $label = current(explode(';', $db->f('phone_label')));
      $contacts[$db->f('contact_id')]->phone[] = array('label' => $label, 'number' => $db->f('phone_number'));
    }
    
    $query = "SELECT contactentity_contact_id AS contact_id, email_label, email_address FROM Email 
              INNER JOIN ContactEntity ON email_entity_id = contactentity_entity_id 
              WHERE contactentity_contact_id IN ({$contact_ids})  ";
    /*$query.= "UNION 
              SELECT contact_id, 'COMPANY' as email_label, email_address FROM Email 
              INNER JOIN CompanyEntity ON email_entity_id = companyentity_entity_id 
              INNER JOIN Contact ON contact_company_id = companyentity_company_id
              WHERE contact_id IN ({$contact_ids})  ";*/
    $query.= "ORDER BY email_label";
    $db->query($query);        
    while ($db->next_record()) {
      $label = current(explode(';', $db->f('email_label')));
      $contacts[$db->f('contact_id')]->email[] = array('label' => $label, 'address' => $db->f('email_address'));
    }
    
    $lang = get_lang();
    $query = "SELECT contactentity_contact_id AS contact_id, address_label, address_street, address_zipcode, address_expresspostal, address_town, address_country, country_name
              FROM Address 
              INNER JOIN ContactEntity ON address_entity_id = contactentity_entity_id 
              LEFT JOIN Country ON country_iso3166 = address_country AND country_lang = '$lang' 
              WHERE contactentity_contact_id IN ({$contact_ids})  ";
    /*$query.= "UNION 
              SELECT contact_id, 'COMPANY' as address_label, address_street, address_zipcode, address_expresspostal, address_town, address_country, country_name 
              FROM Address 
              INNER JOIN CompanyEntity ON address_entity_id = companyentity_entity_id 
              INNER JOIN Contact ON contact_company_id = companyentity_company_id
              LEFT JOIN Country ON country_iso3166 = address_country AND country_lang = '$lang' 
              WHERE contact_id IN ({$contact_ids})  ";*/
    $query.= "ORDER BY address_label";
    $db->query($query);        
    while ($db->next_record()) {
      $label = current(explode(';',$db->f('address_label')));
      $contacts[$db->f('contact_id')]->address[] = array(
        'label' => $label, 'street' => $db->f('address_street'), 'zipcode' => $db->f('address_zipcode'),
        'expresspostal' => $db->f('address_expresspostal'), 'town' => $db->f('address_town'), 'country' => $db->f('country_name'));
    }
    
    $query = "SELECT contactentity_contact_id AS contact_id, IM.* FROM IM 
              INNER JOIN ContactEntity ON im_entity_id = contactentity_entity_id 
              WHERE  contactentity_contact_id IN ({$contact_ids})";
    $db->query($query);        
    while ($db->next_record()) {
      $contacts[$db->f('contact_id')]->im[] = array('protocol' => $db->f('im_protocol'),'address' => $db->f('im_address'));
    }
    
    $query = "SELECT contactentity_contact_id AS contact_id, website_label, website_url FROM Website 
              INNER JOIN ContactEntity ON website_entity_id = contactentity_entity_id 
              WHERE contactentity_contact_id IN ({$contact_ids}) ";
    /*$query.= "UNION 
              SELECT contact_id, 'COMPANY' as website_label, website_url FROM Website
              INNER JOIN CompanyEntity ON website_entity_id = companyentity_entity_id 
              INNER JOIN Contact ON contact_company_id = companyentity_company_id
              WHERE contact_id IN ({$contact_ids}) ";*/
    $query .= "ORDER BY website_label";
    $db->query($query);        
    while ($db->next_record()) {
      $label = current(explode(';', $db->f('website_label')));
      $contacts[$db->f('contact_id')]->website[] = array('label' => $label, 'url' => $db->f('website_url'));
    }
    
    return $contacts;
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
    $db->query($query);        
    while ($db->next_record()) {
      $kinds[$db->f('kind_id')] = $db->f('kind_minilabel');
    }
    return $kinds;
  }
}
