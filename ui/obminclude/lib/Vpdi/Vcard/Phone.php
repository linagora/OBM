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
 * vCard phone
 * 
 * Represents the value of a TEL property
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Vcard_Phone extends Vpdi_Vcard_Property {
  /**
   * Location of the device (home, work, cell, car, ...)
   * 
   * @var array
   */
  public $location;
  
  /**
   * Capabilities of the device (voice, fax, modem, idsn, ...)
   * 
   * @var array
   */
  public $capability;
  
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
  
  public static function decode($TEL) {
    $ph = new Vpdi_Vcard_Phone($TEL->value());
    $ph->addTypes($TEL->getParam('TYPE'));
    return $ph;
  }
  
  public function __construct($number='') {
    $this->preferred = false;
    $this->location = array();
    $this->capability = array();
    $this->nonstandard = array();
    $this->value = $number;
  }
  
  public function addType($type) {
    $type = strtolower($type);
    if ($type == 'pref') {
      $this->preferred = true;
    } elseif (in_array($type, array('home', 'work', 'cell', 'car', 'pager'))) {
      $this->location[] = $type;
    } elseif (in_array($type, array('voice', 'fax', 'video', 'msg', 'bbs', 'modem', 'isdn', 'pcs'))) {
      $this->capability[] = $type;
    } else {
      $this->nonstandard[] = $type;
    }
  }
  
  public function encode() {
    $params = array();
    $params = array_merge($params, $this->location, $this->capability);
    if ($this->preferred) {
      $params[] = 'pref';
    }
    return new Vpdi_Property('TEL', $this->value, array('TYPE' => $params));
  }
}