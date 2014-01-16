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



include_once('obminclude/of/vcalendar/Element.php');
include_once('obminclude/of/vcalendar/element/Vevent.php');
include_once('obminclude/of/vcalendar/Utils.php');
//TODO : VAlerts
//TODO : Timezone support
//TODO : ICS Writer
//TODO : VCAL Reader
//TODO : Resources items

/**
 * OBM Vcalendar Object  
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class Vcalendar {

  var $vcalendar;
  
  var $error;

  /**
   * Constructor
   * Create a vcalendar Element. //FIXME necessaire?
   * 
   * @access public
   * @return void
   */
  function Vcalendar () {
    $this->vcalendar = new Vcalendar_Element($this,'vcalendar');
  }

  /**
   * Call this to destroy the element
   * Break the circular dependency so the garbage collector can do its job.
   *
   * @access public
   * @return void
   */
  function destroy() {
    $this->vcalendar = null;
  }

  /**
   * Create a new ICS element 
   * 
   * @param string $name kind of element
   * @access public
   * @return Vcalendar_Element
   */
  function & createElement($name) {
    if(class_exists('Vcalendar_Element_'.ucfirst($name), false)) {
      $class = 'Vcalendar_Element_'.ucfirst($name);
      return new $class($this);
    } else {
      return new Vcalendar_Element($this, strtolower($name));
    }
  }

  /**
   * Get all vevent in the document 
   * 
   * @access public
   * @return array of Vcalendar_Element
   */
  function & getVevents() {
    return $this->vcalendar->getElementByName('vevent');
  }
  
  function & getElementByUid($id) {
    return $this->vcalendar->getElementByProperty('uid',$id);
  }
  /**
   * Get all valarms in the document 
   * 
   * @access public
   * @return array of Vcalendar_Element
   */
  function & getValarms() {
    return $this->vcalendar->getElementByName('valarm');
  }
}
?>
