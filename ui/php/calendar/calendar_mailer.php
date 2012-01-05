<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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



require("$obminclude/of/of_mailer.php");

class CalendarMailer extends OBM_Mailer {
  protected $module = 'calendar';
  
  protected $attachIcs = true;
  
  protected $icsEncoding = '8bit';

  protected $return_path;
  
  public function __construct() {
    parent::__construct();
    $this->attachIcs = $GLOBALS['ccalendar_send_ics'];
    $this->icsEncoding = $GLOBALS['ccalendar_ics_encoding'];
  }
  
  protected function eventInvitation($event, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->return_path = $this->getOwner($event);
    $this->subject = __('New event created by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    $this->attachIcs($event, "request");
  }
  
  protected function eventNotice($event, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->return_path = $this->getOwner($event);
    $this->subject = __('New event created by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
  }

  protected function eventCancel($event, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->return_path = $this->getOwner($event);
    $this->subject = __('Event cancelled by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    $this->attachIcs($event, "cancel");
  }
  
  protected function eventCancelNotice($event, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->return_path = $this->getOwner($event);
    $this->subject = __('Event cancelled by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
  }
  
  protected function eventUpdate($event, $oldEvent, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->return_path = $this->getOwner($event);
    $this->subject = __('Event updated by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
    $this->attachIcs($event, "request");
  }
  
  protected function eventUpdateNotice($event, $oldEvent, $attendees) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->return_path = $this->getOwner($event);
    $this->subject = __('Event updated by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
  }

  protected function eventStateUpdate($event, $user, $attendeeState) {
    $userId = null;
    if ( $attendeeState && is_array( $attendeeState ) && array_key_exists("user",$attendeeState) &&
	  is_array($attendeeState["user"]) && count($attendeeState["user"]) ) {
      $user = reset($attendeeState["user"]);
      $userId = $user->id ;
      $this->userId = $userId;
    }
    $this->from = $this->getSender($userId);
    $this->recipients = $this->getRecipients(array($event->owner), 'set_mail_participation');
    $this->subject = __('Participation updated: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from, '', $user);

    $this->parts[] = array(
      'content' => file_get_contents($event->getIcs($userId,"reply"), 'r'),
      'content_type' => 'text/calendar; charset=UTF-8; method=REPLY',
      'encoding' => $this->icsEncoding
    );
    $this->attachments[] = array(
      'content' => file_get_contents($event->getIcs($userId,"reply"), 'r'),
      'filename' => 'meeting.ics', 'content_type' => 'application/ics'
    );
  }


  /////////////////////////////////////////////////////////////////////////////
  // RESOURCE
  /////////////////////////////////////////////////////////////////////////////
  protected function resourceReservation($event, $resourceOwners, $resource) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('Resource %resource% reservation: %title%', array('%resource%' => $resource->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
  }

  protected function resourceCancel($event, $resourceOwners) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('Resource %resource% reservation cancelled: %title%', array('%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
  }
  
  protected function resourceUpdate($event, $oldEvent, $resourceOwners, $resource) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('Resource %resource% reservation updated: %title%', array('%resource%' => $resource->label, '%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
  }

  protected function resourceStateUpdate($event, $res) {
    $this->resourceStateUpdateAllday($event, $res);
  }

  protected function resourceStateUpdateAllday($event, $res) {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients(array($event->owner), 'set_mail_participation');
    $this->subject = __('Resource participation updated: %title%', array('%title%' => $event->title));
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
    $this->subject = __('New event created by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    $this->attachIcs($event, "request", true);
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
    $this->subject = __('Event cancelled by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = $this->extractEventDetails($event, $this->from);
    $this->attachIcs($event, "cancel");
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
    $this->subject = __('Event updated by %sender%: %title%', array('%sender%'=>$event->owner->label, '%title%' => $event->title));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
    $this->attachIcs($event, "request", true);
  }
  
  protected function attachIcs($event, $method, $include_attachments = false) {
    if ($this->attachIcs) {
      $ics_file = $event->getIcs($this->userId, $method, $include_attachments);
      $this->parts[] = array(
        'content' => fopen($ics_file, 'r'), 
        'content_type' => 'text/calendar; charset=UTF-8; method='.strtoupper($method),
        'encoding' => $this->icsEncoding
      );
      $this->attachments[] = array(
        'content' => fopen($ics_file, 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );      
    }
  }

  
  private function extractEventDetails($event, $sender, $prefix = '', $target = null) {
    $contacts = $event->contact;
    foreach ($contacts as $contact) {
      $attendees[] = $contact->label;
    }
    $users = $event->user;
    foreach ($users as $user) {
      $attendees[] = $user->label;
    }
    $list_attendees = array_slice($attendees, 0, 9);
    if(count($attendees) >= count($list_attendees)) {
        $suffix = '...';
    }
    $list_attendees = implode(', ', $list_attendees).$suffix;

    return array(
      'host'             => $GLOBALS['cgp_host'],
      $prefix.'id'       => $event->id,
      $prefix.'start'    => $event->date_begin->getOutputDateTime(),
      $prefix.'startDate'    => $event->date_begin->getOutputDate(),
      $prefix.'end'      => $event->date_end->getOutputDateTime(),
      $prefix.'title'    => $event->title,
      $prefix.'location' => $event->location,
      $prefix.'auteur'   => $event->owner->label,
      $prefix.'target'   => $target->label,
      $prefix.'targetState' => __($target->state),
      $prefix.'attendees' => $list_attendees
    );
  }

}

class shareCalendarMailer extends OBM_Mailer {
  
  protected $module = 'calendar';
  
  protected $attachVcard = true;

  public function addRecipient($mail) {
    if($mail != 'all')
      $this->recipients[] = $mail;
  }

  public function userShareHtml($user) {
    $this->from = $this->getSender();
    $this->subject = __('Partage d\'agenda : %firstname% %name%', 
      array('%name%' => $user['lastname'], '%firstname%' => $user['firstname']));
    $this->body = array('user' => $user, 'url' => $this->getHtmlCalUri($user)); 
  }

  public function userShareIcs($user) {
    $this->from = $this->getSender();
    $this->subject = __('Partage d\'agenda : %firstname% %name%', 
      array('%name%' => $user['lastname'], '%firstname%' => $user['firstname']));
    $this->body = array('user' => $user, 'url' => $this->getCalUri($user));
    
    if ($this->attachVcard) {
      $this->attachments[] = array(
        'content' => (string) $this->generateVcard($user), 
        'filename' => 'contact.vcf', 'content_type' => 'text/x-vcard'
      );
    }
  }
  
  private function generateVcard($user) {
    $card = new Vpdi_Vcard();
    
    $name = new Vpdi_Vcard_Name();
    $name->family = $user['lastname'];
    $name->given  = $user['firstname'];
    //$name->fullname =  ;
    $card->setName($name);
    
    $add = new Vpdi_Vcard_Address();
    $add->street = $user['address1'];
    $add->extended = $user['address2'];
    $add->locality = $user['town'];
    $add->postalcode = $user['zipcode'];
    $add->pobox = $user['expresspostal'];
    //$add->country = 'FRANCE';
    $add->location[] = 'work';
    $card->addAddress($add);
    
    if (!empty($user['phone'])) {
      $phone = new Vpdi_Vcard_Phone($user['phone']);
      $phone->location[] = 'work';
      $card->addPhone($phone);
    }
    
    if (!empty($user['mobile'])) {
      $mobile = new Vpdi_Vcard_Phone($user['mobile']);
      $mobile->location[] = 'cell';
      $card->addPhone($mobile);
    }
    
    if (!empty($user['email'])) {
      $email = new Vpdi_Vcard_Email($user['email']);
      $email->location[] = 'work';
      $card->addEmail($email);
    }
    
    $card[] = new Vpdi_Property('caluri', $this->getCalUri($user));
    
    return $card;
  }
  
  private function getCalUri($user) {
    return $GLOBALS['cgp_host'].'calendar/calendar_render.php?action=ics_export'
      .'&externalToken='.$user['token']
      .'&lastname='.urlencode($user['lastname'])
      .'&firstname='.urlencode($user['firstname'])
      .'&email='.urlencode($user['email']);
  }
  
  private function getHtmlCalUri($user) {
    return $GLOBALS['cgp_host'].'calendar/calendar_render.php?externalToken='.$user['token'];
  }
}
