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
 * iCalendar base class
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
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
    if (is_array($this->components)) {
      foreach ($this->components as $c) {
        if (get_class($c) == $class) {
          $comps[] = $c;
        }
      }
    }
    return $comps;
  }
 
  public function getVevents($type = null) {
    $vevents = array();
    foreach($this->getComponents($type) as $component) {
      if ( $component instanceof Vpdi_Icalendar_Vevent) {
        $vevents[] = $component;
      }
    }
    return $vevents;
  }
 
  public function getBusyPeriods() {
    $periods = array();
    if (count($this->getComponents(Vpdi_Icalendar_Component::VFREEBUSY)) > 0) {
      foreach ($this->getComponents(Vpdi_Icalendar_Component::VFREEBUSY) as $vfb) {
        $fbs = $vfb->getFreebusys();
        // Google Calendar hack !!! (no FREEBUSY property, but multiple VFREEBUSY components instead)
        if (count($fbs) == 0) {
          $periods[] = new Vpdi_Icalendar_Freebusy($vfb->dtstart, $vfb->dtend);
        } else {
          $periods = array_merge($periods, $fbs);
        }
      }
    } else if (count($this->getComponents(Vpdi_Icalendar_Component::VEVENT)) > 0) {
      foreach($this->getComponents(Vpdi_Icalendar_Component::VEVENT) as $vevent) {
        $periods[] = new Vpdi_Icalendar_Event($vevent);
      }
    }
    return $periods;
  }
  
  public function getBusyPeriodsWithinInterval(Of_Date $start, Of_Date $end) {
    $periods = array();
    foreach ($this->getBusyPeriods() as $k => $p) {
      if ($rrule = $p->getRrule()) {
        $pstart = clone $p->start;
        if ($pstart->compare($end) < 0 && ($rrule['until'] == null || $rrule['until']->compare($start) > 0)) { 
          $periods[] = $p;
        }
      } else if($p->start->compare($end) < 0 && $p->end->compare($start) > 0) {
        $periods[] = $p;
      }
    }
    return $periods;
  }
}
