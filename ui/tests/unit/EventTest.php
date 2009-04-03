<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
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
?>
<?php

require_once dirname(__FILE__).'/TestsHelper.php';
require_once 'php/calendar/calendar_query.inc';
require_once 'of_date.inc';
require_once 'obmlib.inc';

/**
 * FIXME Currently EventFactory does not handle database... pretty useless isn't it?
 * 
 * @uses OBM
 * @uses _Database_TestCase
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
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
      'user' => array (0 => '2'),
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
      'user' => array (0 => '2'),
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
      'user' => array (0 => '2'),
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

}
