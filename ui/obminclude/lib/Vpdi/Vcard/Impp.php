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
 * vCard instant messaging
 * 
 * Represents the value of an IMPP property
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
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