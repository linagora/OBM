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
 * Represents the organizer of a calendar event
 * 
 * It is a property containing a CAL-ADDRESS value.
 * 
 * Example : ORGANIZER;CN="J. Doe":mailto:jdoe@example.com
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
  
class Vpdi_Icalendar_Organizer {
  /**
   * CAL-ADRESS values are represented by an uri, usually a mailto uri
   * 
   * @var string
   */
  public $uri;
  
  /**
   * Common (or displayable) name associated with the calendar address
   * 
   * @var string
   */
  public $cn;
  
  /**
   * Intended role that the organizer will have (usually CHAIR)
   * 
   * @var string
   */
  public $role;
  
  /**
   * URI to the directory information associated with the calendar address,
   * for example : "ldap://host.com:6666/o=eDABC%20Industries,c=3DUS??(cn=3DBJim%20Dolittle)"
   * 
   * @var string
   */
  public $dir;
  
  /**
   * Language associated with the CN parameter
   * 
   * @var string
   */
  public $language;
  
  /**
   * URI indicating whom is acting on behalf of the person associated with the 
   * calendar address
   * 
   * @var string
   */
  public $sentBy;
  
  protected $propName = 'ORGANIZER';
  
  protected $paramMapping = array(
    'cn' => array('CN', 'ParamValue'),
    'dir' => array('DIR', 'ParamValue'),
    'sentBy' => array('SENT-BY', 'ParamValue'),
    'language' => array('LANGUAGE', 'ParamText'),
    'role' => array('ROLE', 'ParamText'),
  );
  
  public static function decode(Vpdi_Property $ORGANIZER) {
    $org = new Vpdi_Icalendar_Organizer($ORGANIZER->value());
    $org->decodeParameters($ORGANIZER);
    return $org;
  }
  
  public function __construct($uri = '', $cn = null) {
    $this->uri = $uri;
    $this->cn = $cn;
  }
  
  public function __toString() {
    return $this->encode()->__toString();
  }
  
  public function encode() {
    $params = array();
    foreach ($this->paramMapping as $k => $v) {
      if (isset($this->{$k})) {
        list($name, $type) = $v;
        $encodeMethod = 'encode'.$type; 
        $params[$name] = Vpdi::$encodeMethod($this->{$k});
      }
    }
    return new Vpdi_Property($this->propName, $this->uri, $params);
  }
  
  public function decodeParameters(Vpdi_Property $prop) {
    foreach ($this->paramMapping as $k => $v) {
      list($name, $type) = $v;
      if (($param = $prop->getParam($name)) !== false) {
        $decodeMethod = 'decode'.$type;
        if (method_exists('Vpdi', $decodeMethod)) {
          $param = Vpdi::$decodeMethod($param);
        }
        $this->{$k} = $param;
      }
    }
  }
}