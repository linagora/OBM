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
 * vCard instant messaging
 * 
 * Represents the value of an IMPP property
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Vcard_Impp extends Vpdi_Vcard_Property {
  /**
   * Location of address (home, work, ...)
   * 
   * @var array
   */
  public $location;
  
  /**
   * Purpose of communications (personal, business, ...)
   * 
   * @var array
   */
  public $purpose;
  
  /**
   * Nonstandard types ; these will be decoded, but not encoded
   * 
   * @var array
   */
  public $nonstandard;
  
  /**
   * Whether this is the preferred address
   * 
   * @var boolean
   */
  public $preferred;
  
  /**
   * The address : it is a URL, ie a concatenation of the protocol used and
   * the address of the user, like im:raphael@example.com
   * 
   * Common protocols : xmpp, irc, sip, im, ymsgr, msn, aim
   * 
   * @var string
   */
  public $value;
  
  public static function decode(Vpdi_Property $IMPP) {
    $im = new Vpdi_Vcard_Impp($IMPP->value());
    $im->addTypes($IMPP->getParam('TYPE'));
    return $im;
  }
  
  public function __construct($url='') {
    $this->preferred = false;
    $this->location = array();
    $this->nonstandard = array();
    $this->purpose = array();
    $this->value = $url;
  }
  
  public function addType($type) {
    $type = strtolower($type);
    if ($type == 'pref') {
      $this->preferred = true;
    } elseif (in_array($type, array('home', 'work', 'mobile'))) {
      $this->location[] = $type;
    } elseif (in_array($type, array('personal', 'business'))) {
      $this->purpose[] = $type;
    } else {
      $this->nonstandard[] = $type;
    }
  }
  
  public function encode() {
    $params = array_merge($this->location, $this->purpose);
    if ($this->preferred) {
      $params[] = 'pref';
    }
    return new Vpdi_Property('IMPP', $this->value, array('TYPE' => $params));
  }
}