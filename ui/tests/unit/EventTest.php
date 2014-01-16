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



require_once dirname(__FILE__).'/TestsHelper.php';
require_once 'php/calendar/calendar_query.inc';
require_once 'of_date.inc';
require_once 'obmlib.inc';
require_once 'EventMailObserverTest.php';
require_once 'vcalendar/writer/OBM.php';

/**
 * FIXME Currently EventFactory does not handle database... pretty useless isn't it?
 * 
 * @uses OBM
 * @uses _Database_TestCase
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class EventTest extends OBM_Database_TestCase {

  protected function getDataSet() {
    $csvDataSet = new OBM_Database_CsvDataSet(';');
    $csvDataSet->addTable('Entity');
    $csvDataSet->addTable('Domain');
    $csvDataSet->addTable('DomainEntity');
    $csvDataSet->addTable('UserObm');
    $csvDataSet->addTable('UserEntity');
    $csvDataSet->addTable('UserObmPref');
    $csvDataSet->addTable('UGroup');
    $csvDataSet->addTable('GroupEntity');
    $csvDataSet->addTable('Resource');
    $csvDataSet->addTable('ResourceEntity');
    $csvDataSet->addTable('EventCategory1');
    $csvDataSet->addTable('Event');
    $csvDataSet->addTable('EventLink');
    $csvDataSet->addTable('EventException');
    $csvDataSet->addTable('of_usergroup');
    $csvDataSet->addTable('EntityRight');
    $csvDataSet->addTable('CalendarEntity');
    $csvDataSet->addTable('EventEntity');
    return $csvDataSet;
  }

  public function testCreate() {
    $data = array ( 
      'id' => 1,
      'title' => 'Title' ,
      'owner' => '2' ,
      'location' => 'Location' ,
      'category1' => '9' ,
      'privacy' => 1,
      'date_begin' => new Of_Date('2009-03-24 08:00:00'),
      'date_end' => new Of_Date('2009-03-24 09:00:00'),
      'priority' => '2' ,
      'color' => '#cc3333' ,
      'repeat_kind' => 'daily' ,
      'repeatfrequency' => '1' ,
      'repeat_end' => new Of_Date('2009-04-30'),
      'date_exception' => array (0 => new Of_Date('2009-03-25 08:00:00')),
      'description' => 'Description' ,
      'duration' => 3600,
      'repeat_days' => '0000000', 
      'user' => array (0 => '4'),
      'resource' => array(0 => 1),
      'contact' => array ()
    );
    $event = OBM_EventFactory::getInstance()->create($data);
    foreach($data as $key => $value) {
      if($value instanceof Of_Date) {
        $this->assertTrue($value->equals($event->get($key)));
      } elseif(is_array($value)) {
        foreach($value as $subkey => $subvalue) {
          $eventVal = $event->get($key);
          $eventVal = $eventVal[$subkey];          
          if($subvalue instanceof Of_Date) {
            $this->assertTrue($subvalue->equals($eventVal));
          } elseif($eventVal instanceof OBM_EventAttendee) {
            $this->assertEquals($subvalue, $eventVal->__toString());
          } else {
            $this->assertEquals($eventVal,$subvalue);
          }
        }        
      } elseif($event->get($key) instanceof OBM_EventAttendee) {
        $this->assertEquals($value, $event->get($key)->__toString());
      } else {
        $this->assertEquals($event->get($key),$value);
      }
    }       
  }

  public function testGetById() {
    $data = array ( 
      'title' => 'Title' ,
      'owner' => '2' ,
      'location' => 'Location' ,
      'category1' => '9' ,
      'privacy' => 1,
      'date_begin' => new Of_Date('2009-03-24 08:00:00'),
      'date_end' => new Of_Date('2009-03-24 09:00:00'),
      'priority' => '2' ,
      'color' => '#cc3333' ,
      'repeat_kind' => 'daily' ,
      'repeatfrequency' => '1' ,
      'repeat_end' => new Of_Date('2009-04-30'),
      'date_exception' => array (0 => new Of_Date('2009-03-25 08:00:00')),
      'description' => 'Description' ,
      'duration' => 3600,
      'repeat_days' => '0000000', 
      'user' => array (0 => '4'),
      'resource' => array(0 => 1),
      'contact' => array()
    );    
    $event = OBM_EventFactory::getInstance()->getById(1);
    foreach($data as $key => $value) {
      if($value instanceof Of_Date) {
        $this->assertTrue($value->equals($event->get($key)));
      } elseif(is_array($value)) {
        foreach($value as $subkey => $subvalue) {
          $eventVal = $event->get($key);
          $eventVal = $eventVal[$subkey];          
          if($subvalue instanceof Of_Date) {
            $this->assertTrue($subvalue->equals($eventVal));
          } elseif($eventVal instanceof OBM_EventAttendee) {
            $this->assertEquals($subvalue, $eventVal->__toString());
          } else {
            $this->assertEquals($eventVal,$subvalue);
          }
        }        
      } elseif($event->get($key) instanceof OBM_EventAttendee) {
        $this->assertEquals($value, $event->get($key)->__toString());
      } else {
        $this->assertEquals($event->get($key),$value);
      }
    }    
  }  

  public function testStore() {
    $data = array ( 
      'title' => 'Title' ,
      'owner' => '2' ,
      'location' => 'Location' ,
      'category1' => '9' ,
      'privacy' => 1,
      'date_begin' => new Of_Date('2009-03-24 08:00:00'),
      'date_end' => new Of_Date('2009-03-24 09:00:00'),
      'priority' => '2' ,
      'color' => '#cc3333' ,
      'repeat_kind' => 'daily' ,
      'repeatfrequency' => '1' ,
      'repeat_end' => new Of_Date('2009-04-30'),
      'date_exception' => array (0 => new Of_Date('2009-03-25 08:00:00')),
      'description' => 'Description' ,
      'duration' => 3600,
      'repeat_days' => '0000000', 
      'user' => array (0 => '4'),
      'resource' => array(0 => 1),
      'contact' => array()
    );    
    $event = OBM_EventFactory::getInstance()->getById(1);
    foreach($data as $key => $value) {
      $event->set($key, $value);
    }
    OBM_EventFactory::getInstance()->store($event,OBM_EventFactory::getInstance()->getById(1));
    $event = OBM_EventFactory::getInstance()->getById(1);
    foreach($data as $key => $value) {
      if($value instanceof Of_Date) {
        $this->assertTrue($value->equals($event->get($key)));
      } elseif(is_array($value)) {
        foreach($value as $subkey => $subvalue) {
          $eventVal = $event->get($key);
          $eventVal = $eventVal[$subkey];          
          if($subvalue instanceof Of_Date) {
            $this->assertTrue($subvalue->equals($eventVal));
          } elseif($eventVal instanceof OBM_EventAttendee) {
            $this->assertEquals($subvalue, $eventVal->__toString());
          } else {
            $this->assertEquals($eventVal,$subvalue);
          }
        }        
      } elseif($event->get($key) instanceof OBM_EventAttendee) {
        $this->assertEquals($value, $event->get($key)->__toString());
      } else {
        $this->assertEquals($event->get($key),$value);
      }
    }        
  }    


  public function testModifyPartStat() {
  }


  /*
   * Test that getEventByExtId method of Vcalendar_Writer_OBM returns in the order :
   * - The first event if there is only one event with the same ext id
   * - The event with the given ext id and owned by the current user if exists
   * - The event with the given ext id and where current user is an attendee
   * - The event with the given ext id and where current user has write rights on the event's owner calendar
   * - The first event if several events with the same ext_id exists but user isn't owner or attendee and hasn't got write rights
   */
  public function testVcalendarGetSingleEventByExtId(){
    // lmartin should get the event he owns (the 4)
    $GLOBALS['obm']['uid'] = 9;
    $GLOBALS['obm']['domain_id'] = 3;
    OBM_Acl::initialize();
    $writerOBM = new Vcalendar_Writer_OBM();
    $eventData = $writerOBM->getEventByExtId("double");
    $this->assertEquals(4, $eventData->Record["event_id"]);

    // ytouzet should get the event he has rights on (the 4)
    $GLOBALS['obm']['uid'] = 10;
    $writerOBM = new Vcalendar_Writer_OBM();
    $eventData = $writerOBM->getEventByExtId("double");
    $this->assertEquals(4, $eventData->Record["event_id"]);

    // adupont should get the event which he participates (the 3)
    $GLOBALS['obm']['uid'] = 8;
    $writerOBM = new Vcalendar_Writer_OBM();
    $eventData = $writerOBM->getEventByExtId("double");
    $this->assertEquals(3, $eventData->Record["event_id"]);

    // someone without write rights, not owner nor participant should get the first event with "double" ext_id
    $GLOBALS['obm']['uid'] = 11;
    $writerOBM = new Vcalendar_Writer_OBM();
    $eventData = $writerOBM->getEventByExtId("double");
    $this->assertEquals(2, $eventData->Record["event_id"]);

    // With a single event matching an ext id, getEventByExtId should return this single event
    $GLOBALS['obm']['uid'] = 9;
    $writerOBM = new Vcalendar_Writer_OBM();
    $eventData = $writerOBM->getEventByExtId("single");
    $this->assertEquals(5, $eventData->Record["event_id"]);
  }

}
