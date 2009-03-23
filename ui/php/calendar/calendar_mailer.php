<?php

class CalendarMailer extends OBM_Mailer
{
  protected $module = 'calendar';
  
  protected $attachIcs = true;
  
  public function __construct()
  {
    parent::__construct();
    $this->attachIcs = $GLOBALS['ccalendar_send_ics'];
  }
  
  protected function eventInvitation($event, $attendees)
  {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->subject = __('New event on OBM: %title%', array('%title%' => $event['title']));
    $this->body = $this->extractEventDetails($event, $this->from);
    if ($this->attachIcs) {
      $this->parts[] = array(
        'content' => fopen($this->generateIcs($event, $attendees), 'r'), 
        'content_type' => 'text/calendar'
      );
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $attendees), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'application/ics'
      );
    }
  }
  
  protected function resourceReservation($event, $resourceOwners)
  {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($resourceOwners);
    $this->subject = __('New resource reservation on OBM: %title%', array('%title%' => $event['title']));
    $this->body = $this->extractEventDetails($event, $this->from);
  }
  
  protected function eventUpdate($event, $oldEvent, $attendees)
  {
    $this->from = $this->getSender();
    $this->recipients = $this->getRecipients($attendees);
    $this->subject = __('New event on OBM: %title%', array('%title%' => $event['title']));
    $this->body = array_merge($this->extractEventDetails($event, $this->from),
                              $this->extractEventDetails($oldEvent, $this->from, 'old_'));
    if ($this->attachIcs) {
      $this->attachments[] = array(
        'content' => fopen($this->generateIcs($event, $attendees), 'r'), 
        'filename' => 'meeting.ics', 'content_type' => 'text/calendar'
      );
    }
  }
  
  /**
   * Perform the export meeting to the vCalendar format
   */
  private function generateIcs($event, $attendees)
  {
    include_once('obminclude/of/vcalendar/writer/ICS.php');
    include_once('obminclude/of/vcalendar/reader/OBM.php');
    
    $reader = new Vcalendar_Reader_OBM(array('user' => array($this->userId => 'dummy')), array($event['calendar_id']));
    $document = $reader->getDocument();
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
  
  private function extractEventDetails($event, $sender, $prefix = '')
  {
    return array(
      $prefix.'id'       => $event['calendar_id'],
      $prefix.'start'    => $event['date_begin']->getOutputDateTime(),
      $prefix.'end'      => $event['date_end']->getOutputDateTime(),
      $prefix.'title'    => $event['title'],
      $prefix.'location' => $event['location'],
      $prefix.'auteur'   => $sender[1]
    );
  }
}