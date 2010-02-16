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
 * vCard email
 * 
 * Represents the value of an EMAIL property
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Vcard_Email extends Vpdi_Vcard_Property {
  /**
   * Location referred to by the email address (home, work, ...)
   * 
   * @var array
   */
  public $location;
  
  /**
   * The email address format (home, work, ...)
   * 
   * @var string
   */
  public $format;
  
  /**
   * Nonstandard types ; these will be decoded, but not encoded
   * 
   * @var array
   */
  public $nonstandard;
  
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
  
  public static function decode(Vpdi_Property $EMAIL) {
    $em = new Vpdi_Vcard_Email($EMAIL->value());
    $em->addTypes($EMAIL->getParam('TYPE'));
    return $em;
  }
  
  public function __construct($address='') {
    $this->preferred = false;
    $this->location = array();
    $this->nonstandard = array();
    $this->format = 'internet';
    $this->value = $address;
  }
  
  public function addType($type) {
    $type = strtolower($type);
    if ($type == 'pref') {
      $this->preferred = true;
    } elseif (in_array($type, array('home', 'work'))) {
      $this->location[] = $type;
    } elseif (in_array($type, array('internet', 'x400'))) {
      $this->format = $type;
    } else {
      $this->nonstandard[] = $type;
    }
  }
  
  public function encode() {
    $params = $this->location;
    $params[] = $this->format;
    if ($this->preferred) {
      $params[] = 'pref';
    }
    return new Vpdi_Property('EMAIL', $this->value, array('TYPE' => $params));
  }
}