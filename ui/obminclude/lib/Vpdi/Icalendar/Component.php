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

class Vpdi_Icalendar_UnknownComponentTypeException extends Exception {}

/**
 * iCalendar component base class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Icalendar_Component extends Vpdi_Entity {
  
  const VEVENT = 'VEVENT';
  
  const VFREEBUSY = 'VFREEBUSY';
  
  const VTODO = 'VTODO';
  
  protected function setProperty($name, $value) {
    $this->deleteProperties($name);
    $this->addProperty(new Vpdi_Property($name, $value));
  }
  
  protected function deleteProperties($name) {
    foreach ($this->properties as $k => $property) {
      if ($property->nameEquals($name)) {
        unset($this->properties[$k]);
      }
    }
  }
  
  protected function setDateTime($propName, $date) {
    $this->addProperty(new Vpdi_Property($propName, Vpdi::encodeDateTime($date)));
  }
  
  protected function getDateTime($propName) {
    $prop = $this->getProperty($propName);
    if (is_null($prop)) {
      return null;
    }
    $dt = Vpdi::decodeDateTime($prop->value());
    if (($tzid = $prop->getParam('TZID')) !== false) {
      $dt->setTimezone(Vpdi::decodeTimezone($tzid));
    }
    return $dt;
  }
}