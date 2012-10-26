<?PHP


class OBM_EventDiff {

  private $old = null;
  private $new = null;
  private $attendees = null;
  private $attendeesDecisionOrComment = false;

  function __construct($old, $new) {
    $this->oldEvent = $old;
    $this->newEvent = $new;
  }

  public function getAttendeesDiff() {
    if ( $this->attendees != null ) {
      return $this->attendees;
    }
    $attendees = array();
    if($this->oldEvent === null) {
      $attendees['new']['user'] = $this->newEvent->user;
      $attendees['new']['resource'] = $this->newEvent->resource;
      $attendees['new']['contact'] = $this->newEvent->contact;
      $attendees['old'] = array('user'=>array(), 'resource' => array(), 'contact' => array());
      $attendees['current'] = array('user'=>array(), 'resource' => array(), 'contact' => array());
    } elseif($this->newEvent === null) {
      $attendees['old']['user'] = $this->oldEvent->user;
      $attendees['old']['resource'] = $this->oldEvent->resource;
      $attendees['old']['contact'] = $this->oldEvent->contact;
      $attendees['new'] = array('user'=>array(), 'resource' => array(), 'contact' => array());
      $attendees['current'] = array('user'=>array(), 'resource' => array(), 'contact' => array());
    } else {
      $attendees = array();
      $attendees['new']['user'] = array_diff($this->newEvent->user, $this->oldEvent->user);
      $attendees['new']['resource'] = array_diff($this->newEvent->resource, $this->oldEvent->resource);
      $attendees['new']['contact'] = array_diff($this->newEvent->contact, $this->oldEvent->contact);
      $attendees['current']['user'] = array_intersect($this->newEvent->user, $this->oldEvent->user);
      $attendees['current']['resource'] = array_intersect($this->newEvent->resource, $this->oldEvent->resource);
      $attendees['current']['contact'] = array_intersect($this->newEvent->contact, $this->oldEvent->contact);
      $attendees['old']['user'] = array_diff($this->oldEvent->user, $this->newEvent->user);
      $attendees['old']['resource'] = array_diff($this->oldEvent->resource, $this->newEvent->resource);
      $attendees['old']['contact'] = array_diff($this->oldEvent->contact, $this->newEvent->contact); 
    }
    $this->attendees = $attendees;
    return $this->attendees;
  }

  public function getAttendeesStateOrCommentDiff () {
    if ( $this->newEvent == null || $this->oldEvent == null ) {
      return null;
    }
    return $this->diffAttendeesDecisionOrComment($this->oldEvent, $this->newEvent);
  }

  /**
   * Perform attendee state diff between old and new OBM_Event  
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access private
   * @return void
   */
  public function diffAttendeesComment() {
    $newState = array_intersect($this->newEvent->user, $this->oldEvent->user);
    $oldState = array_intersect($this->oldEvent->user, $this->newEvent->user);
    $attendesComment['user'] = array_udiff($newState, $oldState, array('OBM_EventAttendee', 'cmpComment'));
    $newState = array_intersect($this->newEvent->resource, $this->oldEvent->resource);
    $oldState = array_intersect($this->oldEvent->resource, $this->newEvent->resource);
    $attendesComment['resource'] = array_udiff($newState, $oldState, array('OBM_EventAttendee', 'cmpComment'));
    return $attendesComment;
  }

  /**
   * Perform attendee state diff between old and new OBM_Event  
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access private
   * @return void
   */
  public function diffAttendeesState() {
    $newState = array_intersect($this->newEvent->user, $this->oldEvent->user);
    $oldState = array_intersect($this->oldEvent->user, $this->newEvent->user);
    $att['user'] = array_udiff($newState, $oldState, array('OBM_EventAttendee', 'cmpState'));
    $newState = array_intersect($this->newEvent->resource, $this->oldEvent->resource);
    $oldState = array_intersect($this->oldEvent->resource, $this->newEvent->resource);
    $att['resource'] = array_udiff($newState, $oldState, array('OBM_EventAttendee', 'cmpState'));
    return $att;
  }


  /**
  * returns the attendees having participation state or comment changed
  *
  *
  * @return array("user"=> array(...), "resource"=>array(...)) OR null
  */
  public function diffAttendeesDecisionOrComment() {
    if ( $this->attendeesDecisionOrComment !== false ) {
      return $this->attendeesDecisionOrComment;
    }
    $this->attendeesDecisionOrComment = $this->diffAttendeesState();
    if($this->attendeesDecisionOrComment['user'] == null && $this->attendeesDecisionOrComment['resource'] == null){
      $attendeesComment = $this->diffAttendeesComment();
      if($attendeesComment['user'] != null || $attendeesComment['resource'] != null){
        $this->attendeesDecisionOrComment = $attendeesComment;
      }
    }
    return $this->attendeesDecisionOrComment;
  }

  /**
   * Perform delta between old and new event
   * 
   * @param OBM_Event $old 
   * @param OBM_Event $new 
   * @access public 
   * @return void
   */
  public function hasEventFullyChanged($exceptionsMatter = true) {
    return $this->newEvent->shouldIncrementSequence($this->oldEvent, $exceptionsMatter)
      || $this->newEvent->title != $this->oldEvent->title
      || $this->newEvent->category1 != $this->oldEvent->category1
      || $this->newEvent->privacy != $this->oldEvent->privacy
      || $this->newEvent->priority != $this->oldEvent->priority
      || $this->newEvent->color != $this->oldEvent->color
      || $this->newEvent->description != $this->oldEvent->description;
  }

}