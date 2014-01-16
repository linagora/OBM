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
 *  
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */

$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
if( isset($GLOBALS['ccalendar_ics_eventStompObserver']) && $GLOBALS['ccalendar_ics_eventStompObserver']) {
  include_once("$obminclude/stomp/Stomp.php");
}

class OBM_Event /*Implements OBM_PropertyChangeSupport*/{

  private $id;
  private $uid;
  private $title;
  private $owner;
  private $opacity;
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
  private $sequence;
  private $ics_files = array();
  private $creator;
  private $negative_recurrence_id;


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
    $this->negative_recurrence_id = null;
  }

  /**
   * Delete all ICS file registered in this event
   * @return void
   */
  public function __destruct() {
    foreach ($this->ics_files as $ics_file) {
      unlink($ics_file);
    }
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
      throw new InvalidArgumentException('The '.$property.' property is not supported');
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

  private function get_ics_file_key($method, $include_attachments = false) {
    return $method . ($include_attachments ? '-include' : '-notinclude');
  }

  public static function generateIcs($event, $method, $userId, $include_attachments = false) {
    include_once('obminclude/of/vcalendar/writer/ICS.php');
    include_once('obminclude/of/vcalendar/reader/OBM.php');
    
    $recurrenceId = $event->negative_recurrence_id;

    $reader = new Vcalendar_Reader_OBM(array('user' => array($userId => 'dummy')), array($event->id), null, null, null, $recurrenceId);
    $document = $reader->getDocument($method, $include_attachments);
    $writer = new Vcalendar_Writer_ICS();  
    $writer->writeDocument($document);

    $tmpFilename = secure_tmpname('.ics','ics_');
    $res = fopen($tmpFilename, 'w');

    if (!$res) {
      throw new Exception('Unable to open file');
    }
    fputs($res, $writer->buffer);
    fclose($res);
    return $tmpFilename;
  }

  /**
   * Return the ICS filename, generate the file if needed
   * @param $method string The ICS METHOD (cf RFC5545) to use
   * @param $include_attachments boolean Include documents links
   */
  public function getIcs($userId, $method, $include_attachments = false) {
    $key = $this->get_ics_file_key($method, $include_attachments);
    if ( !array_key_exists($key, $this->ics_files) || !file_exists($this->ics_files[$key]) ) {
      $this->ics_files[$key] = OBM_Event::generateIcs($this, $method, $userId, $include_attachments);
    }
    return $this->ics_files[$key];
  }

  /**
  * return true if the event is different enough from the passed event so that we have to
  * bump the sequence number
  *
  * @param $old other OBM_Event to compare to
  * @return boolean whether or not the changes should bring a sequence bump 
  */
  public function shouldIncrementSequence($old, $exceptionsMatter = true) {
	$isDifferentEnough = $this->isDifferent($old);

    if ($exceptionsMatter) {
  		$isDifferentEnough = $isDifferentEnough || $this->hasDifferentExceptions($old);
    }

    return $isDifferentEnough;
  }
  
  public function isDifferent($old) {
  	return $this->location != $old->location
		|| $this->allday  != $old->allday
		|| $this->date_begin->compare($old->date_begin) != 0
		|| $this->duration != $old->duration
		|| $this->repeat_kind    != $old->repeat_kind
		|| ($this->repeat_kind == 'weekly' && $this->repeat_days != $old->repeat_days)
		|| ($this->repeat_kind != 'none' && ( 
						($this->repeat_end && !$old->repeat_end) 
					  || (!$this->repeat_end && $old->repeat_end) 
					  || $this->repeat_end && $old->repeat_end && $this->repeat_end->compareDateIso($old->repeat_end) != 0 
						) 
	  )
		|| ($this->repeat_kind != 'none' && $this->repeatfrequency != $old->repeatfrequency);
  }
  
  public function hasDifferentExceptions($old) {
  	return (count(array_udiff($this->date_exception, $old->date_exception, array('Of_Date', 'cmp'))))
  	  || (count(array_udiff($old->date_exception, $this->date_exception, array('Of_Date', 'cmp'))));
  } 
}

/**
 * Event attendees 
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class OBM_EventAttendee {

  private $kind;
  private $state;
  private $label;
  private $comment;
  private $id;

  public function __construct($id, $state, $label, $comment) {
    $this->id = $id + 0;
    $this->state = $state;
    $this->label = $label;
    $this->comment = $comment;
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
    if($attendee1->id > $attendee2->id) return 1;
    if($attendee1->id < $attendee2->id) return -1;
    return strcmp($attendee1->state, $attendee2->state);
  }
  
  public static function cmpComment($attendee1, $attendee2) {
    if($attendee1->id > $attendee2->id) return 1;
    if($attendee1->id < $attendee2->id) return -1;
    return strcmp($attendee1->comment, $attendee2->comment);
  }
}


/**
 * Factory for OBM_Event objects 
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class OBM_EventFactory extends OBM_ASubject {

  private static $instance;
  private $db;
 
  /**
   * __construct 
   * 
   * @access protected
   * @return void
   */
  protected function __construct() {
    parent::__construct();
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
   * Get an OBM_Event object of the given id
   * 
   * @param int $id Event id
   * @access public
   * @return OBM_Event
   */
  public function getById($id) {
    $query = 'SELECT Event.*, owner.userobm_id AS owner_id, owner.userobm_lastname AS owner_lastname, owner.userobm_firstname AS owner_firstname, creator.userobm_id AS creator_id, creator.userobm_lastname AS creator_lastname, creator.userobm_firstname AS creator_firstname FROM Event INNER JOIN UserObm owner ON owner.userobm_id = event_owner INNER JOIN UserObm creator ON creator.userobm_id = event_usercreate WHERE event_id = '.$id.'';
    $this->db->query($query);
    $this->db->next_record();
    $event = new OBM_Event($id);
    $event->title = $this->db->f('event_title');
    $event->uid = $this->db->f('event_ext_id');
    $event->sequence = $this->db->f('event_sequence');
    $event->owner = new OBM_EventAttendee($this->db->f('owner_id'), null, $this->db->f('owner_firstname').' '.$this->db->f('owner_lastname'), '') ;
    $event->creator = new OBM_EventAttendee($this->db->f('creator_id'), null, $this->db->f('creator_firstname').' '.$this->db->f('creator_lastname'), '') ;
    $event->opacity = $this->db->f('event_opacity');
    $event->location = $this->db->f('event_location');
    $event->category1 = $this->db->f('event_category1_id');
    $event->privacy = $this->db->f('event_privacy');
    $event->date_begin = new Of_Date($this->db->f('event_date'),'GMT');
    $event->date_end = clone $event->date_begin;
    $event->date_end->addSecond($this->db->f('event_duration'));
    $event->duration = $this->db->f('event_duration');
    $event->allday = $this->db->f('event_allday');
    $event->priority = $this->db->f('event_priority');
    $event->color = $this->db->f('event_color');
    $event->repeat_kind = $this->db->f('event_repeatkind');
    $event->repeatfrequency = $this->db->f('event_repeatfrequence');
    if ( $this->db->f('event_endrepeat') ) {
      $event->repeat_end = new Of_Date($this->db->f('event_endrepeat'),'GMT');
    } else {
      $event->repeat_end = null;
    }
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
    $query = 'SELECT * FROM EventException WHERE eventexception_parent_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $exceptions[] = new Of_Date($this->db->f('eventexception_date'), 'GMT');
    }
    return $exceptions;
  }

  private function getEventUsers($id) {
    $users = array();
    $query = 'SELECT userobm_id, eventlink_state, userobm_lastname, userobm_firstname, eventlink_comment FROM UserObm INNER JOIN UserEntity ON userobm_id = userentity_user_id INNER JOIN EventLink ON eventlink_entity_id = userentity_entity_id WHERE eventlink_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $users[] = new OBM_EventAttendee($this->db->f('userobm_id'), $this->db->f('eventlink_state'), $this->db->f('userobm_firstname').' '.$this->db->f('userobm_lastname'), $this->db->f('eventlink_comment'));
    }
    return $users;
  }

  private function getEventResources($id) {
    $resource = array();
    $query = 'SELECT resource_id, eventlink_state, resource_name, eventlink_comment FROM Resource INNER JOIN ResourceEntity ON resource_id = resourceentity_resource_id INNER JOIN EventLink ON eventlink_entity_id = resourceentity_entity_id WHERE eventlink_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $resource[] = new OBM_EventAttendee($this->db->f('resource_id'), $this->db->f('eventlink_state'), $this->db->f('resource_name'), $this->db->f('eventlink_comment'));
    }
    return $resource;
  }

  private function getEventContacts($id) {
    $contact = array();
    $query = 'SELECT contact_id, eventlink_state, contact_lastname, contact_firstname, eventlink_comment FROM Contact INNER JOIN ContactEntity ON contact_id = contactentity_contact_id INNER JOIN EventLink ON eventlink_entity_id = contactentity_entity_id WHERE eventlink_event_id = '.$id.'';
    $this->db->query($query);
    while($this->db->next_record()) {
      $contact[] = new OBM_EventAttendee($this->db->f('contact_id'), $this->db->f('eventlink_state'), $this->db->f('contact_firstname').' '.$this->db->f('contact_lastname'), $this->db->f('eventlink_comment'));
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
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class OBM_EventMailObserver implements  OBM_IObserver {

  private $mailer;
  private static $cache;
  private $eventDiff;

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
   * @see OBM_IObserver::update
   */
  public function update($old, $new) {
    $this->eventDiff = new OBM_EventDiff($old, $new);
    $attendees = $this->eventDiff->getAttendeesDiff();
    if ( $old !== null && $new !== null ) {
      $attendeesDecisionOrComment = $this->eventDiff->getAttendeesStateOrCommentDiff();
      $this->send($old, $new, $attendees, $attendeesDecisionOrComment);
    } else {
      $this->send($old, $new, $attendees);
    }
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
    // we send mail if the parameters we got through UI tells us so
    if ( !$GLOBALS["send_notification_mail"] ) {
        return ;
    }
    
    if(!$this->mustBeSent($old, $new))
      return false;

    foreach($attendees as $state => $attendeesList) {
      foreach($attendeesList as $kind => $recipients) {
        if( $kind != "resource" && count($recipients) > 0) {
          $fn = 'send'.ucfirst($state).ucfirst($kind).'Mail';
          if(method_exists($this, $fn)) {
            $this->$fn($old, $new, $recipients);
          }
        }
      }
    }
    //var_dump($attendeesState);
    if ($attendeesState != null && !$new->shouldIncrementSequence($old) ) {
      foreach($attendeesState['user'] as $ustate) {
        $this->sendEventStateUpdateMail($new, $ustate, $attendeesState);
      }
      foreach($attendeesState['resource'] as $rstate) {
        $this->sendResourceStateUpdateMail($new, $rstate);
      }
    }
  }

  /**
    * Tell if the mail must be sent or not.
    *  For exemple a mail will not be sent if the concerned event is in
    *  the past.
   */
  private function mustBeSent($old, $new) {
    $today = Of_Date::today();
    $willBeSent = false;
    if(isset($new) && $new instanceof OBM_Event) {
      if($new->date_end->compare($today) > 0) {
        $willBeSent = true;
      }
      if($new->repeat_kind != 'none' && (!$new->repeat_end || $new->repeat_end->compare($today) > 0) ) {
        $willBeSent = true;
      }
    }
    if(isset($old) && $old instanceof OBM_Event) {
      if($old->date_end->compare($today) > 0) {
        $willBeSent = true;
      }
      if($old->repeat_kind != 'none' && (!$old->repeat_end || $old->repeat_end->compare($today) > 0) ) {
        $willBeSent = true;
      }
    }
    return $willBeSent;
  }


  /**
   * Send notification for user event participation 
   * 
   * @param OBM_Event $new 
   * @param OBM_EventAttendee $user 
   * @access private
   * @return void
   */
  private function sendEventStateUpdateMail($new, $user, $attendeesState) {
    if($new->get('owner')->get('id') != $user->get('id')) {
      $this->mailer->sendEventStateUpdate($new, $user, $attendeesState);
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
    if($new->get('allday')){
      $this->mailer->sendResourceStateUpdateAllday($new, $res);
    } else {
      $this->mailer->sendResourceStateUpdate($new, $res);
    }
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
    list($invit_recipients, $notice_recipients) = $this->sortObmUsersRecipients($recipients);
    if (!empty($invit_recipients)) {
      if ($new->repeat_kind == 'none') {
      	$this->mailer->sendEventInvitation($new, $invit_recipients);
      } else {
      	$this->mailer->sendRecurrentEventInvitation($new, $invit_recipients);
      }
    }
    if (!empty($notice_recipients)) {
      if ($new->repeat_kind == 'none') {
        $this->mailer->sendEventNotice($new, $notice_recipients);
      } else {
      	$this->mailer->sendRecurrentEventNotice($new, $notice_recipients);
      }  
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
    list($invit_recipients, $notice_recipients) = $this->sortObmUsersRecipients($recipients);
      if (!empty($invit_recipients)) {
        if ($old->repeat_kind == 'none') {
      	$this->mailer->sendEventCancel($old, $invit_recipients);
      } else {
      	$this->mailer->sendRecurrentEventCancel($old, $invit_recipients);
      }
    }
    if (!empty($notice_recipients)) {
      if ($old->repeat_kind == 'none') {
        $this->mailer->sendEventCancelNotice($old, $notice_recipients);
      } else {
      	$this->mailer->sendRecurrentEventCancelNotice($old, $notice_recipients);
      }  
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
    list($invit_recipients, $notice_recipients) = $this->sortObmUsersRecipients($recipients);
    if (!empty($invit_recipients) && $this->eventDiff->hasEventFullyChanged()) {
      if ($new->repeat_kind == 'none') {
      	$this->mailer->sendEventUpdate($new, $old, $invit_recipients);
      } else {
      	$this->mailer->sendRecurrentEventUpdate($new, $old, $invit_recipients);
      }
    }
    if (!empty($notice_recipients) && $this->eventDiff->hasEventFullyChanged()) {
      if ($new->repeat_kind == 'none') {
        $this->mailer->sendEventUpdateNotice($new, $old, $notice_recipients);
      } else {
      	$this->mailer->sendRecurrentEventUpdateNotice($new, $old, $notice_recipients);
      } 
    }
  }
  
  /**
   * Sort recipients (OBM users) between users requiring an invitation (and an ICS) 
   * and users requiring a simple notice (because the event owner has write rights on them)
   * 
   * @param array $recipients 
   * @access private
   * @return array
   */
  private function sortObmUsersRecipients($recipients) {
    $recipients = array_diff($recipients, array($GLOBALS['obm']['uid']));
    $invit_recipients = array_diff($recipients, array_keys(OBM_Acl::getAllowedEntities($GLOBALS['obm']['uid'], 'calendar', 'write')));
    $notice_recipients = array_diff($recipients, $invit_recipients);
    return array($invit_recipients, $notice_recipients);
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
    	if ($new->repeat_kind == 'none') {
      	  $this->mailer->sendContactInvitation($new, $recipients);
    	} else {
    	  $this->mailer->sendRecurrentContactInvitation($new, $recipients);
    	}
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
    	if ($old->repeat_kind == 'none') {
      	  $this->mailer->sendContactCancel($old, $recipients);
    	} else {
    	  $this->mailer->sendRecurrentContactCancel($old, $recipients);
    	}
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
    if ($this->eventDiff->hasEventFullyChanged()) {
    	if ($new->repeat_kind == 'none') {
      	  $this->mailer->sendContactUpdate($new, $old, $recipients);
    	} else {
    	  $this->mailer->sendRecurrentContactUpdate($new, $old, $recipients);
    	}
    }
  }
}


/**
 * This class define a basic Observer.
 * Any class can easily register any OBM_IObserver by subclassing OBM_ASubject.
 */
interface OBM_IObserver {
  /**
   * Function called when the subject is modified
   * @param mixed $old A representation of the old version of the subject
   * @param mixed $nex A representation of the new version of the subject
   */
  public function update($old, $new);
}

/**
 * This class implements basic operations needed by a Subject.
 */
abstract class OBM_ASubject {
  private $observers;

  protected function __construct() {
    $this->observers = array();
  }

  /**
   * Register an OBM_IObserver
   * @param OBM_IObserver $observer The observer to register
   */
  public function attach($observer) {
    if (self::isObserver($observer)) {
      $this->observers[] = $observer;
    } else {
      throw new OBM_ObserverException("This object does not implements OBM_IObserver (type is : " . get_class($observers) . ".\n");
    }
  }

  /**
   * Unregister an OBM_IObserver
   * @param OBM_IObserver $observer The observer to unregister
   */
  public function detach($observer) {
    if (self::isObserver($observer)) {
      $index = array_search($observer, $this->observers);
      if($index !== false) {
        unset($this->observers[$index]);
      }
    } else {
      throw new OBM_ObserverException("This object does not implements OBM_IObserver (type is : " . get_class($observers) . ".\n");
    }
  }

  /**
   * Notify all registered OBM_IObserver
   * @param mixed $old Optional represensation of the subject before it has been changed.
   * @param mixed $new Optional represensation of the subject after it has been changed.
   */
  public function notify($old = null, $new = null) {
    foreach($this->observers as $observer) {
      $observer->update($old, $new);
    }
  }

  private static function isObserver($candidate) {
    $array = class_implements($candidate);
    $index = array_search('OBM_IObserver', $array);
    return ($index !== false);
  }
}

/**
 * A simple Observer, which print OBM_Event properties into a file (useful for debug)
 */
class OBM_EventDebugObserver implements OBM_IObserver {

  private $event_prop_names = array(
                                    'id', 'uid', 'title', 'owner', 'opacity',
                                    'allday', 'location', 'category1',
                                    'privacy', 'date_begin', 'date_end',
                                    'duration', 'priority', 'color',
                                    'repeat_kind', 'repeatfrequency', 
                                    'repeat_end', 'date_exception',
                                    'description', 'event_duration', 
                                    'repeat_days', 'user', 'resource', 'contact',
                                    );

  public function update($old, $new) {
    $h = fopen("/tmp/debug", "a");
    fputs($h, "OBM_EventDebugObserver->update :\n");
    if ($old == null && $new == null) {
      fputs($h, "BOTH null !\n");
    }
    if ($old != null) {
      fputs($h, "\tOLD :\n");
      foreach ($this->event_prop_names as $prop_name) {
        $prop_value = $old->get($prop_name);
        fputs($h, "\t$prop_name\t\t: $prop_value -\n");
      }
    }
    if ($new != null) {
      fputs($h, "\tNEW :\n");
      foreach ($this->event_prop_names as $prop_name) {
        $prop_value = $new->get($prop_name);
        fputs($h, "\t$prop_name\t\t: $prop_value -\n");
      }
//       fputs($h, var_export($new));
    }
    fclose($h);
  }

}

/**
 * An OBM_IObserver that send message to the MQ
 */
class OBM_EventStompObserver implements OBM_IObserver {
               
       public function update($old, $new){
               try {
                       if ($new !== null) { // Update or Create
                               $method = "request";
                               $tmpFilename = $new->getIcs($userId, $method, false);
                       } elseif ($old !== null) { // Delete
                               $method = "cancel";
                               $tmpFilename = $old->getIcs($userId, $method, false);
                       } else {
                               throw new OBM_ObserverException(__('$old and $new cannot be null at the same time in OBM_EventStompObserver'));
                       }
                                               
                       $contentOfTmpFilename = fread(fopen($tmpFilename, "r"), filesize($tmpFilename));
		       $stomp = new Stomp("tcp://".$GLOBALS['stomp_host'].":".$GLOBALS['stomp_port']); 
                       $stomp->connect();
                       $stomp->send("jms.topic.eventChanges", $contentOfTmpFilename, array('persistent'=>'true'));

                       $stomp->disconnect();
               } catch (StompException $e) {
                       throw new OBM_ObserverException(__("An Exception was thrown during OBM_EventStompObserver->update " . $e->getMessage() . " -"));
	       }
       }
}

class OBM_ObserverException extends Exception {}
