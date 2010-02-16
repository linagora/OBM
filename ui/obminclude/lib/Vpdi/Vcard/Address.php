<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
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

/**
 * vCard address
 * 
 * Represents the value of an ADR property
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Vcard_Address extends Vpdi_Vcard_Property {
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
   * Nonstandard types ; these will be decoded, but not encoded
   * 
   * @var array
   */
  public $nonstandard;
  
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
  
  public static function decode(Vpdi_Property $ADR) {
    $parts = Vpdi::decodeTextList($ADR->value(), ';');
    $add = new Vpdi_Vcard_Address;
    foreach (self::$addr_parts as $i => $part) {
      $add->{$part} = (isset($parts[$i])) ? $parts[$i] : '';
    }
    $add->addTypes($ADR->getParam('TYPE'));
    return $add;
  }
  
  public function __construct(array $parts = array()) {
    foreach ($parts as $k => $part) {
      if (in_array($k, self::$addr_parts)) {
        $this->{$k} = $part;
      }
    }
    $this->preferred = false;
    $this->location = array();
    $this->delivery = array();
    $this->nonstandard = array();
  }
  
  public function addType($type) {
    $type = strtolower($type);
    if ($type == 'pref') {
      $this->preferred = true;
    } elseif (in_array($type, array('home', 'work'))) {
      $this->location[] = $type;
    } elseif (in_array($type, array('postal', 'parcel', 'dom', 'intl'))) {
      $this->delivery[] = $type;
    } else {
      $this->nonstandard[] = $type;
    }
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
    return new Vpdi_Property('ADR', $value, array('TYPE' => $params));
  }
}