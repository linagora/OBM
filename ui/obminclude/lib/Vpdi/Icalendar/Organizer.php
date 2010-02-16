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
 * Represents the organizer of a calendar event
 * 
 * It is a property containing a CAL-ADDRESS value.
 * 
 * Example : ORGANIZER;CN="J. Doe":mailto:jdoe@example.com
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
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