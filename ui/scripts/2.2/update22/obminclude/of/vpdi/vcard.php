<?php

/**
 * OBM vCard class
 * 
 * This class implements vCard 3.0 and should not be used with vCard 2.1
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class Vpdi_VCard extends Vpdi_Entity {

  protected $profile = 'VCARD';
  
  private static $decode_methods = array(
    'bday' => 'decodeDate'
  );
  
  public function __get($field) {
    $get_method = 'get'.ucfirst($field);
    if (method_exists($this, $get_method)) {
      return $this->$get_method();
    }
    return $this->getValue($field);
  }
  
  /**
   * Returns a Vpdi_VCard_Name object wrapping the value of the N field
   * 
   * @access public
   * @return Vpdi_VCard_Name
   */
  public function getName() {
    return Vpdi_VCard_Name::decode($this->getField('N'), $this->getField('FN'));
  }
  
  /**
   * Sets the name fields, N and FN, by passing a Vpdi_VCard_Name object
   * 
   * @access public
   * @param Vpdi_VCard_Name $name
   * @return void
   */
  public function setName(Vpdi_VCard_Name $name) {
    $this->addField($name->encode());
    $this->addField($name->encodeFn());
  }
  
  /**
   * Returns a Vpdi_VCard_Address object wrapping the first ADR value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_VCard_Address
   */
  public function getAddress($type = null) {
    return Vpdi_VCard_Address::decode($this->getField('ADR'));
  }
  
  /**
   * Returns an array of Vpdi_VCard_Address objects wrapping the ADR values
   * 
   * @access public
   * @return array
   */
  public function getAddresses() {
    $addresses = array();
    foreach ($this->getFieldsByName('ADR') as $field) {
      $addresses[] = Vpdi_VCard_Address::decode($field);
    }
    return $addresses;
  }
  
  /**
   * Adds an ADR field by passing a Vpdi_VCard_Address object
   * 
   * @access public
   * @param Vpdi_VCard_Address $address
   * @return void
   */
  public function addAddress(Vpdi_VCard_Address $address) {
    $this->addField($address->encode());
  }
  
  /**
   * Returns a Vpdi_VCard_Phone object wrapping the first TEL value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_VCard_Phone
   */
  public function getPhone($type = null) {
    return Vpdi_VCard_Phone::decode($this->getField('TEL'));
  }
  
  /**
   * Returns an array of Vpdi_VCard_Phone objects wrapping the TEL values
   * 
   * @access public
   * @return array
   */
  public function getPhones() {
    $phones = array();
    foreach ($this->getFieldsByName('TEL') as $field) {
      $phones[] = Vpdi_VCard_Phone::decode($field);
    }
    return $phones;
  }
  
  /**
   * Adds a TEL field by passing a Vpdi_VCard_Phone object
   * 
   * @access public
   * @param Vpdi_VCard_Phone $phone
   * @return void
   */
  public function addPhone(Vpdi_VCard_Phone $phone) {
    $this->addField($phone->encode());
  }
  
  /**
   * Returns a Vpdi_VCard_Email object wrapping the first EMAIL value of type $type
   * 
   * @access public
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @return Vpdi_VCard_Email
   */
  public function getEmail($type = null) {
    return Vpdi_VCard_Email::decode($this->getField('EMAIL'));
  }
  
  /**
   * Returns an array of Vpdi_VCard_Email objects wrapping the EMAIL values
   * 
   * @access public
   * @return array
   */
  public function getEmails() {
    $phones = array();
    foreach ($this->getFieldsByName('EMAIL') as $field) {
      $phones[] = Vpdi_VCard_Email::decode($field);
    }
    return $phones;
  }
  
  /**
   * Adds an EMAIL field by passing a Vpdi_VCard_Email object
   * 
   * @access public
   * @param Vpdi_VCard_Email $email
   * @return void
   */
  public function addEmail(Vpdi_VCard_Email $email) {
    $this->addField($email->encode());
  }
  
  /**
   * Returns the raw value for a specific $name
   * 
   * If no field exists, null is returned.
   * 
   * If multiple fields exist, the method will prefer fields with values 
   * and secondly fields with a type=pref. If multiple fields subsist after
   * this sort, then the first field will be returned.
   * 
   * @param string $name the name of the field
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @access public
   * @return mixed
   */
  public function getRawValue($name, $type = null) {
    $field = $this->getField($name, $type);
    if ($field === null) {
      return null;
    }
    return $field->rawValue();
  }
  
  /**
   * Returns the value for a specific $name
   * 
   * If no field exists, null is returned.
   * 
   * If multiple fields exist, the method will prefer fields with values 
   * and secondly fields with a type=pref. If multiple fields subsist after
   * this sort, then the first field will be returned.
   * 
   * @param string $name the name of the field
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @access public
   * @return mixed
   */
  public function getValue($name, $type = null) {
    $field = $this->getField($name, $type);
    if ($field === null) {
      return null;
    }
    return $this->decodeField($field);
  }
  
  /**
   * Returns the value for a field
   * 
   * @param Vpdi_Field $field
   * @access private
   * @return mixed
   */
  private function decodeField($field) {
    $name  = strtolower($field->name());
    $value = $field->value();
    if (isset(self::$decode_methods[$name])) {
      $method = self::$decode_methods[$name];
      return Vpdi::$method($value);
    }
    return Vpdi::decodeText($value);
  }
  
  /**
   * Returns the field for a specific $name
   * 
   * If no field exists, null is returned.
   * 
   * If multiple fields exist, the method will prefer fields with values 
   * and secondly fields with a type=pref. If multiple fields subsist after
   * this sort, then the first field will be returned.
   * 
   * @param string $name the name of the field
   * @param string $type optional specific type (ex: 'home', 'work', ...)
   * @access public
   * @return mixed
   */
  private function getField($name, $type = null) {
    if ($type === null) {
      $fields = $this->getFieldsByName($name);
    } else {
      $fields = array();
      foreach ($this->getFieldsByName($name) as $f) {
        if ($f->typeEquals($type)) {
          $fields[] = $f;
        }
      }
    }
    
    // TODO : gestion de type=PREF
    
    if (count($fields) == 0) {
      return null;
    }
    return $fields[0];
  }
}

/**
 * VCard name
 * 
 * Includes all the components from the N and FN fields
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class Vpdi_VCard_Name {
  /**
   * Family name, from N
   * 
   * @var string
   */
  public $family;
  
  /**
   * Given name, from N
   * 
   * @var string
   */
  public $given;
  
  /**
   * Additional names, from N
   * 
   * @var string
   */
  public $additional;
  
  /**
   * Honorific prefixes, such as "Mr." or "Dr.", from N
   * 
   * @var string
   */
  public $prefixes;
  
  /**
   * Honorific suffixes, such as "Jr." or "M.D.", from N
   * 
   * @var string
   */
  public $suffixes;
  
  /**
   * Value of the FN field
   * 
   * @var string
   */
  public $fullname;
  
  /**
   * Constructor
   * 
   * @param Vpdi_Field $N the N field
   * @param Vpdi_Field $FN the FN field
   * @access public
   * @return void
   */
  public static function decode($N, $FN = null) {
    $N = Vpdi::decodeTextList($N->rawValue(), ';');
    
    $name = new Vpdi_VCard_Name;
  
    $name->family     = (isset($N[0])) ? $N[0] : '';
    $name->given      = (isset($N[1])) ? $N[1] : '';
    $name->additional = (isset($N[2])) ? $N[2] : '';
    $name->prefixes   = (isset($N[3])) ? $N[3] : '';
    $name->suffixes   = (isset($N[4])) ? $N[4] : '';
    
    if ($FN !== null) {
      $name->fullname = $FN->rawValue();
    }
    return $name;
  }
  
  public function __construct() {
    
  }
  
  public function encode() {
    return new Vpdi_Field('n', Vpdi::encodeTextList(array($this->family, $this->given, 
      $this->additional, $this->prefixes, $this->suffixes), ';'));
  }
  
  public function encodeFn() {
    $fn = (!empty($this->fullname)) ? $this->fullname : $this->formatted();
    return new Vpdi_Field('fn', $fn);
  }
  
  public function formatted() {
    $parts = array();
    foreach (array($this->family, $this->additional, $this->given, $this->prefixes) as $p) {
      if (!empty($p)) {
        $parts[] = $p;
      }
    }
    $f = implode(' ', $parts);
    if (!empty($this->suffixes)) {
      $f.= ", {$this->suffixes}";
    }
    return $f;
  }
}

/**
 * VCard phone
 * 
 * Represents the value of a TEL field
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class Vpdi_VCard_Phone {
  /**
   * Location of the device (home, work, cell, car, ...)
   * 
   * @var string
   */
  public $location;
  
  /**
   * Capabilities of the device (voice, fax, modem, idsn, ...)
   * 
   * @var string
   */
  public $capability;
  
  /**
   * Whether this is the preferred phone number
   * 
   * @var boolean
   */
  public $preferred;
  
  /**
   * The phone number
   * 
   * @var string
   */
  public $value;
  
  public static function decode($TEL) {
    $value = $TEL->value();
    
    $ph = new Vpdi_VCard_Phone($value);
    
    $types = $TEL->getParam('TYPE');
    if (!is_array($types)) {
      $types = array($types);
    }
    
    foreach ($types as $type) {
      $type = strtolower($type);
      if ($type == 'pref') {
        $ph->preferred = true;
      } else {
        if (in_array($type, array('home', 'work', 'cell', 'car', 'pager'))) {
          $ph->location[] = $type;
        }
        if (in_array($type, array('voice', 'fax', 'video', 'msg', 'bbs', 'modem', 'isdn', 'pcs'))) {
          $ph->capability[] = $type;
        }
      }
    }
    return $ph;
  }
  
  public function __construct($number='') {
    $this->preferred = false;
    $this->location = array();
    $this->capability = array();
    $this->value = $number;
  }
  
  public function encode() {
    $params = array();
    $params = array_merge($params, $this->location, $this->capability);
    if ($this->preferred) {
      $params[] = 'pref';
    }
    return new Vpdi_Field('TEL', $this->value, array('TYPE' => $params));
  }
}

/**
 * VCard email
 * 
 * Represents the value of an EMAIL field
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class Vpdi_VCard_Email {
  /**
   * Location referred to by the email address (home, work, ...)
   * 
   * @var string
   */
  public $location;
  
  /**
   * Whether this is the preferred phone number
   * 
   * @var boolean
   */
  public $preferred;
  
  /**
   * The phone number
   * 
   * @var string
   */
  public $value;
  
  public static function decode($EMAIL) {
    $value = $EMAIL->value();
    
    $em = new Vpdi_VCard_Email($value);
    
    $types = $EMAIL->getParam('TYPE');
    if (!is_array($types)) {
      $types = array($types);
    }
    
    foreach ($types as $type) {
      $type = strtolower($type);
      if ($type == 'pref') {
        $em->preferred = true;
      } else {
        if (in_array($type, array('home', 'work'))) {
          $em->location[] = $type;
        }
      }
    }
    return $em;
  }
  
  public function __construct($address='') {
    $this->preferred = false;
    $this->location = array();
    $this->value = $address;
  }
  
  public function encode() {
    $params = $this->location;
    if ($this->preferred) {
      $params[] = 'pref';
    }
    return new Vpdi_Field('EMAIL', $this->value, array('TYPE' => $params));
  }
}

/**
 * VCard address
 * 
 * Represents the value of an ADR field
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class Vpdi_VCard_Address {
  /**
   * Street adress
   * 
   * @var string
   */
  public $street;
  
  /**
   * Extended adress
   * 
   * @var string
   */
  public $extended;
  
  /**
   * Usually the city
   * 
   * @var string
   */
  public $locality;
  
  /**
   * Postal code
   * 
   * @var string
   */
  public $postalcode;
  
  /**
   * Post office box
   * 
   * @var string
   */
  public $pobox;
  
  /**
   * Usually the province or state
   * 
   * @var string
   */
  public $region;
  
  /**
   * Country name
   * 
   * @var string
   */
  public $country;
  
  /**
   * Location referred to by the adress (home, work)
   * 
   * @var array
   */
  public $location;
  
  /**
   * Delivery type of the adress (postal, parcel, dom(estic), intl)
   * 
   * @var array
   */
  public $delivery;
  
  /**
   * Whether this is the preferred adress
   * 
   * @var boolean
   */
  public $preferred;
  
  /**
   * Used to shortify code
   * 
   * @var array
   */
  private static $addr_parts = array('pobox', 'extended', 'street', 'locality',
                                     'region', 'postalcode', 'country');
  
  public static function decode($ADR = null) {
    $parts = Vpdi::decodeTextList($ADR->value(), ';');
    
    $add = new Vpdi_VCard_Address;
    
    foreach (self::$addr_parts as $i => $part) {
      $add->{$part} = (isset($parts[$i])) ? $parts[$i] : '';
    }
    
    $types = $ADR->getParam('TYPE');
    if (!is_array($types)) {
      $types = array($types);
    }
    
    foreach ($types as $type) {
      $type = strtolower($type);
      if ($type == 'pref') {
        $add->preferred = true;
      } else {
        if (in_array($type, array('home', 'work'))) {
          $add->location[] = $type;
        }
        if (in_array($type, array('home', 'work', 'postal', 'parcel', 'dom', 'intl'))) {
          $add->delivery[] = $type;
        }
      }
    }
    return $add;
  }
  
  public function __construct() {
    $this->preferred = false;
    $this->location  = array();
    $this->delivery  = array();
  }
  
  public function encode() {
    $parts = array();
    foreach (self::$addr_parts as $part) {
      $parts[] = $this->{$part};
    }
    $value = Vpdi::encodeTextList($parts, ';');
    
    $params = array();
    $params = array_merge($params, $this->location, $this->delivery);
    if ($this->preferred) {
      $params[] = 'pref';
    }
    return new Vpdi_Field('ADR', $value, array('TYPE' => $params));
  }
}
