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
 * iCalendar base class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 * @license GPL 2.0
 */
class Vpdi_Icalendar extends Vpdi_Entity {
  
  protected static $innerEntities = array(
    Vpdi_Icalendar_Component::VEVENT => 'Vpdi_Icalendar_Vevent',
    Vpdi_Icalendar_Component::VFREEBUSY  => 'Vpdi_Icalendar_Vfreebusy'
  );
  
  protected $components;
  
  public function addProperty($property) {
    if (!$property instanceof Vpdi_Property && !$property instanceof Vpdi_Entity) {
      throw new Vpdi_InvalidPropertyException($property);
    }
    if ($property instanceof Vpdi_Property) {
      $this->properties[] = $property;
    } else {
      // TODO : when all possible Vcalendar components will be implemented as classes,
      // we'll need to check here if the component's type is allowed and to not fallback
      // to a generic class
      if (!$property instanceof Vpdi_Icalendar_Component) {
        $property = new Vpdi_Icalendar_Component($property->getProperties());
      }
      $this->components[] = $property;
    }
  }
  
  public function getComponents($type = null) {
    if (is_null($type)) {
      return $this->components;
    }
    if (!array_key_exists($type, self::$innerEntities)) {
      throw new Vpdi_Icalendar_UnknownComponentTypeException($type);
    }
    $class = self::$innerEntities[$type];
    $comps = array();
    foreach ($this->components as $c) {
      if (get_class($c) == $class) {
        $comps[] = $c;
      }
    }
    return $comps;
  }
  
  public function getBusyPeriods() {
    $periods = array();
    foreach ($this->getComponents(Vpdi_Icalendar_Component::VFREEBUSY) as $vfb) {
      $fbs = $vfb->getFreebusys();
      // Google Calendar hack !!! (no FREEBUSY property, but multiple VFREEBUSY components instead)
      if (count($fbs) == 0) {
        $periods[] = new Vpdi_Icalendar_Freebusy($vfb->dtstart, $vfb->dtend);
      } else {
        $periods = array_merge($periods, $fbs);
      }
    }
    return $periods;
  }
  
  public function getBusyPeriodsWithinInterval(DateTime $start, DateTime $end) {
    $periods = array();
    foreach ($this->getBusyPeriods() as $k => $p) {
      if (($p->start >= $start && $p->start < $end) || ($p->end > $start && $p->end <= $end)) {
        $periods[] = $p;
      }
    }
    return $periods;
  }
}