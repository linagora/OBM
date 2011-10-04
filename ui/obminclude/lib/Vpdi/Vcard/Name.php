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
 * vCard name
 * 
 * Includes all the components from the N and FN properties
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Vcard_Name {
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
   * Value of the FN property
   * 
   * @var string
   */
  public $fullname;
  
  public static function decode(Vpdi_Property $N, Vpdi_Property $FN = null) {
    $N = Vpdi::decodeTextList($N->rawValue(), ';');
    
    $name = new Vpdi_Vcard_Name;
  
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
  
  /**
   * Commodity constructor
   * 
   * Takes a fullname as argument, like "John Doe"
   * 
   * @access public
   * @param $fullname
   * @return void
   */
  public function __construct($fullname = null) {
    if (!is_null($fullname)) {
      list($this->given, $this->family) = explode(' ', $fullname);
      $this->fullname = $fullname;
    }
  }
  
  public function encode() {
    return new Vpdi_Property('n', Vpdi::encodeTextList(array($this->family, $this->given, 
      $this->additional, $this->prefixes, $this->suffixes), ';'));
  }
  
  public function encodeFn() {
    $fn = (!empty($this->fullname)) ? $this->fullname : $this->formatted();
    return new Vpdi_Property('fn', $fn);
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