<?PHP



class OBM_EventResourceMailObserver implements  OBM_IObserver {

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


  public function update($old, $new) {
    if ( !$this->mustBeSent($old, $new) ) {
      return ;
    }
    $eventDiff = new OBM_EventDiff($old, $new);
    $attendeesDiff = $eventDiff->getAttendeesDiff();
    if ( !$old ) {
      $resourcesDiff = $attendeesDiff["new"]["resource"];
      $this->sendNewResourceMail($old, $new, $resourcesDiff);
    } else if ( !$new ) {
      $resourcesDiff = $attendeesDiff["old"]["resource"];
      $this->sendOldResourceMail($old, $new, $resourcesDiff);
    } else {
      if ( $eventDiff->hasEventFullyChanged() ) {
        $resourcesDiff = $attendeesDiff["current"]["resource"];
        $this->sendCurrentResourceMail($old, $new, $resourcesDiff);
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
    foreach ($recipients as $resource) {
      $resourceOwners = array_keys(OBM_Acl::getEntityWriters('resource', $resource->id));
      if (!in_array($GLOBALS['obm']['uid'], $resourceOwners) && count($resourceOwners) > 0) {
        $this->mailer->sendResourceUpdate($new, $old, $resourceOwners, $resource);
      }
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
        $this->mailer->sendResourceReservation($new, $resourceOwners, $resource);
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
        $this->mailer->sendResourceCancel($old, $resourceOwners, $resource);
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
}
