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
?><?php
require("$obminclude/of/of_mailer.php");

class CalendarMailer extends OBM_Mailer {
  protected $module = 'calendar';
  
  protected $attachIcs = true;
  
  public function __construct() {
    parent::__construct();
    $this->attachIcs = $GLOBALS['ccalendar_send_ics'];
  }
  
  protected function eventInvitation($event, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->subject = __('New event on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $attendees, "request"), 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method=REQUEST'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $attendees, "request"), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );
    }
  }

  protected function eventCancel($event, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->subject = __('Event cancelled on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $attendees, "cancel"), 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method=CANCEL'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $attendees, "cancel"), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );
    }
  }
  
  protected function eventUpdate($event, $oldEvent, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->subject = __('Event updated on OBM: %title%', array('%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $attendees, "request"), 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method=REQUEST'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $attendees, "request"), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );      
    }
  }

  protected function eventStateUpdate($event, $user) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients(array($event->owner), 'set_mail_participation');
    $this->subject = __('Participation updated on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from, '', $user);
  }


  /////////////////////////////////////////////////////////////////////////////
  // RESOURCE
  /////////////////////////////////////////////////////////////////////////////
  protected function resourceReservation($event, $resourceOwners) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('New resource reservation on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
  }

  protected function resourceCancel($event, $resourceOwners) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('Event cancelled on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
  }
  
  protected function resourceUpdate($event, $oldEvent, $resourceOwners) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('Resource reservation updated on OBM: %title%', array('%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
  }

  protected function resourceStateUpdate($event, $res) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients(array($event->owner), 'set_mail_participation');
    $this->subject = __('Resource participation updated on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from, '', $res);
  }


  /////////////////////////////////////////////////////////////////////////////
  // CONTACT
  /////////////////////////////////////////////////////////////////////////////
  protected function contactInvitation($event, $contacts) {
    $this->from = $this->getSender();
    $recips = array();
    foreach($contacts as $contact) {
      $contact_info = get_entity_info($contact->id, 'contact');
      $label = $contact_info['label']; 
      $email = $contact_info['email'];
      if (trim($email) != "") {
        array_push($recips, array($email, $label));
      }
    }
    $this->recipients = $recips;
    $this->subject = __('New event on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $contacts, "request"), 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method=REQUEST'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $contacts, "request"), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );
    }
  }


  protected function contactCancel($event, $contacts) {
    $this->from = $this->getSender();
    $recips = array();
    foreach($contacts as $contact) {
      $contact_info = get_entity_info($contact->id, 'contact');
      $label = $contact_info['label']; 
      $email = $contact_info['email'];
      if (trim($email) != "") {
        array_push($recips, array($email, $label));
      }
    }
    $this->recipients = $recips;
    $this->subject = __('Event cancelled on OBM: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $contacts, "cancel"), 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method=CANCEL'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $contacts, "cancel"), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );
    }
  }


  protected function contactUpdate($event, $oldEvent, $contacts) {
    $this->from = $this->getSender();
    $recips = array();
    foreach($contacts as $contact) {
      $contact_info = get_entity_info($contact->id, 'contact');
      $label = $contact_info['label']; 
      $email = $contact_info['email'];
      if (trim($email) != "") {
        array_push($recips, array($email, $label));
      }
    }
    $this->recipients = $recips;
    $this->subject = __('Event updated on OBM: %title%', array('%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $contacts, "request"), 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method=REQUEST'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $contacts, "request"), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );      
    }
  }


  /**
   * Perform the export meeting to the vCalendar format
   */
  private function generateIcs($event, $attendees, $method) {
    include_once('obminclude/of/vcalendar/writer/ICS.php');
    include_once('obminclude/of/vcalendar/reader/OBM.php');
    
    $reader = new Vcalendar_Reader_OBM(array('user' => array($this->userId => 'dummy')), array($event->id));
    $document = $reader->getDocument($method);
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
  
  private function extractEventDetails($event, $sender, $prefix = '', $target = null) {
    return array(
      'host'             => $GLOBALS['cgp_host'],
      $prefix.'id'       => $event->id,
      $prefix.'start'    => $event->date_begin->getOutputDateTime(),
      $prefix.'end'      => $event->date_end->getOutputDateTime(),
      $prefix.'title'    => $event->title,
      $prefix.'location' => $event->location,
      $prefix.'auteur'   => $event->owner->label,
      $prefix.'target'   => $target->label,
      $prefix.'targetState' => __($target->state)
    );
  }
}
