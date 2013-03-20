<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
 * vCard name
 * 
 * Includes all the components from the N and FN properties
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
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
  
  public static function decode(Vpdi_Property $N = null, Vpdi_Property $FN = null) {
    $name = new Vpdi_Vcard_Name;
    
    if ($N !== null) {
      $N = Vpdi::decodeTextList($N->rawValue(), ';');
      
      $name->family     = (isset($N[0])) ? $N[0] : '';
      $name->given      = (isset($N[1])) ? $N[1] : '';
      $name->additional = (isset($N[2])) ? $N[2] : '';
      $name->prefixes   = (isset($N[3])) ? $N[3] : '';
      $name->suffixes   = (isset($N[4])) ? $N[4] : '';
    }
    
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