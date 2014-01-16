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



/**
 * vCard address
 * 
 * Represents the value of an ADR property
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
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