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

/**
 *  
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_Event /*Implements OBM_PropertyChangeSupport*/{

  private $id;
  private $title;
  private $owner;
  private $allday;
  private $location;
  private $category1;
  private $privacy;
  private $date_begin;
  private $date_end;
  private $duration;
  private $priority;
  private $color;
  private $repeat_kind;
  private $repeatfrequency;
  private $repeat_end;
  private $date_exception;
  private $description;
  private $event_duration;
  private $repeat_days; 
  private $user;
  private $resource;
  private $contact;


  /**
   * __construct 
   * 
   * @param array $properties 
   * @access public
   * @throw InvalidArgumentException
   * @return void
   */
  public function __construct($id) {
    $this->id = $id;
    $this->user = array();
    $this->resource = array();
    $this->contact = array();
    $this->date_exception = array();
  }

  /**
   * __get 
   * 
   * @param string $property 
   * @access public
   * @throw InvalidArgumentException
   * @return mixed
   */
  public function __get($property) {
    if(property_exists($this, $property)) {
      return $this->$property;
    } else {
      throw new InvalidArgumentException($property.' property is not supported');
    }
  }

  /**
   * __set 
   * 
   * @param string $property 
   * @param mixed $value 
   * @access public
   * @throw InvalidArgumentException
   * @return void
   */
  public function __set($property, $value) {
    $fn = 'set'.ucfirst($property);
    if(method_exists($this, $fn)) {
      $this->$fn($property, $value);  
    } elseif(property_exists($this, $property)) {
      $this->$property = $value;  
    } else {
      throw new InvalidArgumentException($property.' property is not supported');
    }
  }

  /**
   * get 
   * 
   * @param string $property 
   * @access public
   * @return mixed
   */
  public function get($property) {
    return $this->__get($property);
  }

  /**
   * set 
   * 
   * @param string $property 
   * @param mixed $value 
   * @access public
   * @return void
   */
  public function set($property, $value) {
    $this->__set($property, $value);
  }

  /**
   * add 
   * 
   * @param string $property 
   * @param mixed $value 
   * @access public
   * @return void
   */
  public function add($property, $value) {
    $fn = 'add'.ucfirst($property);
    if(method_exists($this, $fn)) {
      $this->$fn($property, $value);  
    } elseif(property_exists($this, $property)) {
      array_push($this->$property,$value);  
    } else {
      throw new InvalidArgumentException($property.' property is not supported');
    }    
  }

  /**
   * del 
   * 
   * @param string $property 
   * @param mixed $value 
   * @access public
   * @return void
   */
  public function del($property, $value) {
    $fn = 'del'.ucfirst($property);
    if(method_exists($this, $fn)) {
      $this->$fn($property, $value);  
    } elseif(property_exists($this, $property)) {
      //FIXME Doesn't work
      $index = array_search($value, $this->$property);
      if($index !== false) {
        unset($this->$property[$index]);
      }      
    } else {
      throw new InvalidArgumentException($property.' property is not supported');
    }    
  }  

  /**
   * addUser
   *
   * @param string $property
   * @param string $value
   * @access public
   * @return void
   */
  public function addUser($property, $value) {
    $entity = get_entity_info($value, $property);
    $user = new OBM_EventAttendee($value, null, $entity["label"]);
    array_push($this->$property, $user);
  }

  /**
   * delUser
   *
   * @param string $property
   * @param string $value
   * @access public
   * @return void
   */
  public function delUser($property, $value) {
    foreach($this->$property as $user) {
      if ($value == $user->id) {
        $index = array_search($user, $this->$property);
        unset($this->{$property}[$index]);
        break;
      }
    }
  }

  /**
   * addResource
   *
   * @param string $property
   * @param string $value
   * @access public
   * @return void
   */
  public function addResource($property, $value) {
    $entity = get_entity_info($value, $property);
    $res = new OBM_EventAttendee($value, null, $entity["label"]);
    array_push($this->$property, $res);
  }

  /**
   * delUser
   *
   * @param string $property
   * @param string $value
   * @access public
   * @return void
   */
  public function delResource($property, $value) {
    foreach($this->$property as $res) {
      if ($value == $res->id) {
        $index = array_search($res, $this->$property);
        unset($this->{$property}[$index]);
        break;
      }
    }
  }

}

/**
 * Event attendees 
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_EventAttendee {

  private $kind;
  private $state;
  private $label;
  private $id;

  public function __construct($id, $state, $label) {
    $this->id = $id + 0;
    $this->state = $state;
    $this->label = $label;
  }

  /**
   * __get 
   * 
   * @param string $property 
   * @access public
   * @throw InvalidArgumentException
   * @return mixed
   */
  public function __get($property) {
    if(property_exists($this, $property)) {
      return $this->$property;
    } else {
      throw new InvalidArgumentException($property.' property is not supported');
    }
  }

  /**
   * __set 
   * 
   * @param string $property 
   * @param mixed $value 
   * @access public
   * @throw InvalidArgumentException
   * @return void
   */
  public function __set($property, $value) {
    $fn = 'set'.ucfirst($property);
    if(method_exists($this, $fn)) {
      $this->$fn($property, $value);  
    } elseif(property_exists($this, $property)) {
      $this->$property = $value;  
    } else {
      throw new InvalidArgumentException($property.' property is not supported');
    }
  }


  /**
   * get 
   * 
   * @param string $property 
   * @access public
   * @return mixed
   */
  public function get($property) {
    return $this->__get($property);
  }

  /**
   * set 
   * 
   * @param string $property 
   * @param mixed $value 
   * @access public
   * @return void
   */
  public function set($property, $value) {
    $this->__set($property, $value);
  }

  /**
   * __toString 
   * 
   * @access public
   * @return void
   */
  public function __toString() {
    return $this->id . '';
  }

  /**
   * cmp 
   * 
   * @param mixed $attendee1 
   * @param mixed $attendee2 
   * @static
   * @access public
   * @return void
   */
  public static function cmp($attendee1, $attendee2) {
    if($attendee1->id == $attendee2->id) return 1;
    return strcmp($attendee1->label, $attendee2->label);
  }

  public static function cmpState($attendee1, $attendee2) {
    if($attendee1->id != $attendee2->id) return 1;
    return strcmp($attendee1->state, $attendee2->state);
  }
}


/**
 * Factory for OBM_Event objects 
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_EventFactory /*Implements OBM_Subject*/{

  private static $instance;
  private $observers;
  private $db;
 
  /**
   * __construct 
   * 
   * @access protected
   * @return void
   */
  protected function __construct() {
    $this->observers = array();
    $this->db = new DB_OBM;
  }
 
  /**
   * Singleton pattern 
   * 
   * @static
   * @access public
   * @return void
   */
  public static function getInstance() {
    if (!self::$instance instanceof self) { 
      self::$instance = new self;
    }
    return self::$instance;
  }


  /**
   * @see OBM_Subject::attach 
   */
  public function attach($observer) {
    $this->observers[] = $observer;
  }

  /**
   * @see OBM_Subject::detach 
   */
  public function detach($observer) {
    $index = array_search($observer, $this->observers);
    if($index !== false) {
      unset($this->observers[$index]);
    }
  }
  /**
   * @see OBM_Subject::notify 
   */
  public function notify($old, $new) {
    foreach($this->observers as $observer) {
      $observer->update($old, $new);
    }
  }

  /**
   * Get an OBM_Event object of the given id
   * 
   * @param int $id Event id
   * @access public
   * @return OBM_Event
   */
  public function getById($id) {
    $query = 'SELECT Event.*, userobm_id, userobm_lastname, userobm_firstname FROM Event INNER JOIN UserObm ON userobm_id = event_owner WHERE event_id = '.$id.'';
    $this->db->query($query);
    $this->db->next_record();
    $event = new OBM_Event($id);
    $event->title = $this->db->f('event_title');
    $event->owner = new OBM_EventAttendee($this->db->f('userobm_id'), null, $this->db->f('userobm_firstname').' '.$this->db->f('userobm_lastname')) ;
    $event->location = $this->db->f('event_location');
    $event->category1 = $this->db->f('event_category1_id');
    $event->privacy = $this->db->f('event_privacy');
    $event->date_begin = new Of_Date($this->db->f('event_date'),'GMT');
    $event->date_end = clone $event->date_begin;
    $event->date_end->addSecond($this->db->f('event_duration'));
    $event->duration = $this->db->f('event_duration');
    $event->priority = $this->db->f('event_priority');
    $event->color = $this->db->f('event_color');
    $event->repeat_kind = $this->db->f('event_repeatkind');
    $event->repeatfrequency = $this->db->f('event_repeatfrequence');
    $event->repeat_end = new Of_Date($this->db->f('event_endrepeat'),'GMT');
    $event->description = $this->db->f('event_description');
    $event->event_duration = $this->db->f('event_event_duration');
    $event->repeat_days = $this->db->f('event_repeatdays'); 

    $event->date_exception = $this->getEventExceptions($id);
    $event->user = $this->getEventUsers($id);
    $event->resource = $this->getEventResources($id);
    $event->contact = $this->getEventContacts($id);

    return $event;
  }

  private function getEventExceptions($id) {
    $exceptions = array();
    $query = 'SELECT * FROM EventException WHERE eventexception_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $exceptions[] = new Of_Date($this->db->f('eventexception_date'), 'GMT');
    }
    return $exceptions;
  }

  private function getEventUsers($id) {
    $users = array();
    $query = 'SELECT userobm_id, eventlink_state, userobm_lastname, userobm_firstname FROM UserObm INNER JOIN UserEntity ON userobm_id = userentity_user_id INNER JOIN EventLink ON eventlink_entity_id = userentity_entity_id WHERE eventlink_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $users[] = new OBM_EventAttendee($this->db->f('userobm_id'), $this->db->f('eventlink_state'), $this->db->f('userobm_firstname').' '.$this->db->f('userobm_lastname'));
    }
    return $users;
  }

  private function getEventResources($id) {
    $resource = array();
    $query = 'SELECT resource_id, eventlink_state, resource_name FROM Resource INNER JOIN ResourceEntity ON resource_id = resourceentity_resource_id INNER JOIN EventLink ON eventlink_entity_id = resourceentity_entity_id WHERE eventlink_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $resource[] = new OBM_EventAttendee($this->db->f('resource_id'), $this->db->f('eventlink_state'), $this->db->f('resource_name'));
    }
    return $resource;
  }

  private function getEventContacts($id) {
    $contact = array();
    $query = 'SELECT contact_id, eventlink_state, contact_lastname, contact_firstname FROM Contact INNER JOIN ContactEntity ON contact_id = contactentity_contact_id INNER JOIN EventLink ON eventlink_entity_id = contactentity_entity_id WHERE eventlink_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $contact[] = new OBM_EventAttendee($this->db->f('contact_id'), $this->db->f('eventlink_state'), $this->db->f('contact_firstname').' '.$this->db->f('contact_lastname'));
    }
    return $contact;
  }

  /**
   * Create a new event in database. 
   * 
   * @param array $properties : Event properties.
   * @access public
   * @return OBM_Event
   */
  public function create($properties) {
    $event = $this->getById($properties['id']);
    //TODO DB create;
    $this->notify(null, $event);
    return $event;
  }

  /**
   * Update an event in database. 
   * 
   * @param OBM_Event $event Modified event
   * @param OBM_Event $oldEvent Old Event : because currently 
   * the factory doesn't handle database (see TODO tags)
   * @access public
   * @return void
   */
  public function store($event, $oldEvent) {
    //$oldEvent = $this->getById($event->id);
    //TODO DB Store
    //
    $this->notify($oldEvent, $event);
  }

  /**
   * Remove an event from database. 
   * 
   * @param OBM_Event $event 
   * @access public
   * @return void
   */
  public function delete($event) {
    //TODO DB Delete
    $this->notify($event, null);
  }
  
}


/**
 * Observer on EventFactory to send mail when an action is
 * done on an event.
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_EventMailObserver /*implements  OBM_Observer*/{

  private $mailer;
  private static $cache;


  /**
   * __construct 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->mailer = new CalendarMailer();
  }
  
  /**
   * @see OBM_Observer::update
   */
  public function update($old, $new) {
    if($old === null) {
      $attendees['new']['user'] = $new->user;
      $attendees['new']['resource'] = $new->resource;
      $attendees['new']['contact'] = $new->contact;
      $attendees['old'] = array('user'=>array(), 'resource' => array(), 'contact' => array());
      $attendees['current'] = array('user'=>array(), 'resource' => array(), 'contact' => array());        
      $this->send($old, $new, $attendees);
    } elseif($new === null) {
      $attendees['old']['user'] = $old->user;
      $attendees['old']['resource'] = $old->resource;
      $attendees['old']['contact'] = $old->contact;
      $attendees['new'] = array('user'=>array(), 'resource' => array(), 'contact' => array());
      $attendees['current'] = array('user'=>array(), 'resource' => array(), 'contact' => array());        
      $this->send($old, $new, $attendees);
    } else {
      $attendees = $this->diffAttendees($old, $new);
      $attendeesState = $this->diffAttendeesState($old, $new);
      $this->send($old, $new, $attendees, $attendeesState);
    }
  }

  /**
   * Perform attendee delta between old and new OBM_Event  
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access private
   * @return void
   */
  private function diffAttendees($old, $new) {
    $attendees = array();
    $attendees['new']['user'] = array_diff($new->user, $old->user);
    $attendees['new']['resource'] = array_diff($new->resource, $old->resource);
    $attendees['new']['contact'] = array_diff($new->contact, $old->contact);      
    $attendees['current']['user'] = array_intersect($new->user, $old->user);
    $attendees['current']['resource'] = array_intersect($new->resource, $old->resource);
    $attendees['current']['contact'] = array_intersect($new->contact, $old->contact);
    $attendees['old']['user'] = array_diff($old->user, $new->user);
    $attendees['old']['resource'] = array_diff($old->resource, $new->resource);
    $attendees['old']['contact'] = array_diff($old->contact, $new->contact);      
    return $attendees;
  }

  /**
   * Perform attendee state diff between old and new OBM_Event  
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access private
   * @return void
   */
  private function diffAttendeesState($old, $new) {
    $newState = array_intersect($new->user, $old->user);
    $oldState = array_intersect($old->user, $new->user);
    $att['user'] = array_udiff($newState, $oldState, array('OBM_EventAttendee', 'cmpState')); 
    $newState = array_intersect($new->resource, $old->resource);
    $oldState = array_intersect($old->resource, $new->resource);
    $att['resource'] = array_udiff($newState, $oldState, array('OBM_EventAttendee', 'cmpState')); 
    return $att;
  }


  /**
   * Send notification 
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $attendees 
   * @access private
   * @return void
   */
  private function send($old, $new, $attendees, $attendeesState=null) {
    foreach($attendees as $state => $attendeesList) {
      foreach($attendeesList as $kind => $recipients) {
        if(count($recipients) > 0) {
          $fn = 'send'.ucfirst($state).ucfirst($kind).'Mail';
          if(method_exists($this, $fn)) {
            $this->$fn($old, $new, $recipients);
          }
        }
      }
    }
    if ($attendeesState != null && !$this->hasEventChanged($old, $new)) {
      foreach($attendeesState['user'] as $ustate) {
        $this->sendEventStateUpdateMail($new, $ustate);
      }
      foreach($attendeesState['resource'] as $rstate) {
        $this->sendResourceStateUpdateMail($new, $rstate);
      }
    }
  }

  /**
   * Send notification for user event participation 
   * 
   * @param OBM_Event $new 
   * @param OBM_EventAttendee $user 
   * @access private
   * @return void
   */
  private function sendEventStateUpdateMail($new, $user) {
    if($new->get('id') != $user->get('id')) {
      $this->mailer->sendEventStateUpdate($new, $user);
    }
  }

  /**
   * Send notification for resource event participation 
   * 
   * @param OBM_Event $new 
   * @param OBM_EventAttendee $user 
   * @access private
   * @return void
   */
  private function sendResourceStateUpdateMail($new, $res) {
    $this->mailer->sendResourceStateUpdate($new, $res);
  }

  /**
   * Send notification for new event participation 
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendNewUserMail($old, $new, $recipients) {
    $recipients = array_diff($recipients, array($GLOBALS['obm']['uid']));
    if (!empty($recipients)) {
      $this->mailer->sendEventInvitation($new, $recipients);
    } 
  }

  /**
   * Send notification for cancelled event participation
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendOldUserMail($old, $new, $recipients) {
    $recipients = array_diff($recipients, array($GLOBALS['obm']['uid']));
    if (!empty($recipients)) {
      $this->mailer->sendEventCancel($old, $recipients);
    }       
  }

  /**
   * Send notification for modified event participation
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendCurrentUserMail($old, $new, $recipients) {
    $recipients = array_diff($recipients, array($GLOBALS['obm']['uid']));
    if (!empty($recipients) && $this->hasEventFullyChanged($old, $new)) {
      $this->mailer->sendEventUpdate($new, $old, $recipients);
    }
  }

  /**
   * [CONTACT] Send notification for new event participation 
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendNewContactMail($old, $new, $recipients) {
    if (!empty($recipients)) {
      $this->mailer->sendContactInvitation($new, $recipients);
    } 
  }


  /**
   * [CONTACT] Send notification for cancelled event participation
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendOldContactMail($old, $new, $recipients) {
    if (!empty($recipients)) {
      $this->mailer->sendContactCancel($old, $recipients);
    }       
  }

  /**
   * [CONTACT] Send notification for modified event participation
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendCurrentContactMail($old, $new, $recipients) {
    if ($this->hasEventFullyChanged($old, $new)) {
      $this->mailer->sendContactUpdate($new, $old, $recipients);
    }
  }



  /**
   * Send notification for new resource reservation
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendNewResourceMail($old, $new, $recipients) {
    foreach ($recipients as $resource) {
      $resourceOwners = array_keys(OBM_Acl::getEntityWriters('resource', $resource->id));
      if (!in_array($GLOBALS['obm']['uid'], $resourceOwners) && count($resourceOwners) > 0) {
        $this->mailer->sendResourceReservation($new, $resourceOwners);
      }
    }      
  }

  /**
   * Send notification for cancelled resource reservation
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendOldResourceMail($old, $new, $recipients) {
    foreach ($recipients as $resource) {
      $resourceOwners = array_keys(OBM_Acl::getEntityWriters('resource', $resource->id));
      if (!in_array($GLOBALS['obm']['uid'], $resourceOwners) && count($resourceOwners) > 0) {
        $this->mailer->sendResourceCancel($old, $resourceOwners);
      }
    }       
  }

  /**
   * Send notification for updated resource reservation 
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @param array $recipients 
   * @access private
   * @return void
   */
  private function sendCurrentResourceMail($old, $new, $recipients) {
    if ($this->hasEventFullyChanged($old, $new)) {
      foreach ($recipients as $resource) {
        $resourceOwners = array_keys(OBM_Acl::getEntityWriters('resource', $resource->id));
        if (!in_array($GLOBALS['obm']['uid'], $resourceOwners) && count($resourceOwners) > 0) {
          $this->mailer->sendResourceUpdate($new, $old, $resourceOwners);
        }
      }         
    }
  }  

  /**
   * Perform delta between old and new event to check if
   * there is signifiant change between both
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access private
   * @return void
   */
  public static function hasEventChanged($old, $new) {
    if(!isset(self::$cache[$old->id])) {
      self::$cache[$old->id] =  $new->location != $old->location
        || $new->allday  != $old->allday
        || $new->date_begin->compare($old->date_begin) != 0
        || $new->duration != $old->duration
        || $new->repeat_kind    != $old->repeat_kind
        || $new->repeat_kind    != $old->repeat_kind
        || ($new->repeat_kind == 'weekly' && $new->repeat_days != $old->repeat_days)
        || ($new->repeat_kind != 'none' && $new->repeat_end->compareDateIso($old->repeat_end) != 0)
        || ($new->repeat_kind != 'none' && $new->repeatfrequency != $old->repeatfrequency)
        || (count(array_udiff($new->date_exception, $old->date_exception, array('Of_Date', 'cmp'))))
        || (count(array_udiff($old->date_exception, $new->date_exception, array('Of_Date', 'cmp'))));
    }
    return self::$cache[$old->id];
  }

  /**
   * Perform delta between old and new event
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access public 
   * @return void
   */
  public static function hasEventFullyChanged($old, $new) {
    return self::hasEventChanged($old, $new)
      || $new->title != $old->title
      || $new->category1 != $old->category1
      || $new->privacy != $old->privacy
      || $new->priority != $old->priority
      || $new->color != $old->color
      || $new->description != $old->description;
  }
}

