<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_query.inc                                             //
//     - Desc : Agenda query File                                            //
// Created    : 2001-06-27 by Francois Bloque                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Insert a new event and return its id 
// Parameters
//   - $p_title :  event's title   
//   - $p_category :  id of the event's category 
//   - $p_priority :  event's priority (0,1 or 2) 
//   - $p_privacy : event's privacy it means private or not ( 0 or 1 ) 
//   - $datebegin :  event's date of begin like "yyyymmddhhmmss" 
//   - $dateend : event's date of begin like "yyyymmddhhmmss" 
//   - $p_description : event's description 
//   - $p_origin :  id of the original event (when this one is a repetition of an other)
//   - $p_occupied_day : flag (0 or 1) indicating if entire day is reserved for the task (or not) 
//   - $p_repeatkind : kind of repetition 
//   - $p_repeat_interval : frequence of repetition 
//   - $p_repeatdays : days of repetition (when its a weekly repetition) 
//   - $p_endrepeat : date of repetition end 
// Return : event's id ??????
//////////////////////////////////////////////////////////////////////////////
function run_query_event_insert($p_event_id,$p_title,$p_category_id,$p_priority,$p_privacy,$p_datebegin,$p_dateend,$p_description,$p_origin,$p_occupied_day,$p_repeatkind,$p_repeat_interval,$p_repeatdays,$p_endrepeat) {
 
  global $auth, $cdg_sql;
 
  if ($p_privacy != 1) {
    $p_privacy=0;
  }
  if ($p_origin == '') {
    $p_origin="null";
  }
  if ($p_occupied_day != 1) {
    $p_occupied_day=0;
  }
  if ($p_repeat_interval =='') {
    $p_repeat_interval=1;
  }

  $query = "insert into CalendarEvent(calendarevent_id,calendarevent_timeupdate,calendarevent_timecreate,calendarevent_userupdate,calendarevent_usercreate,calendarevent_origin_id,calendarevent_title,calendarevent_description,calendarevent_category_id,calendarevent_priority,calendarevent_privacy,calendarevent_datebegin,calendarevent_dateend,calendarevent_occupied_day,calendarevent_repeatkind,calendarevent_repeat_interval,calendarevent_repeatdays,calendarevent_endrepeat) values (".$p_event_id.",null,'".date("YmdHis")."',null,".$auth->auth["uid"].",$p_origin,'$p_title','$p_description',$p_category_id,$p_priority,$p_privacy,'$p_datebegin','$p_dateend',$p_occupied_day,'$p_repeatkind',$p_repeat_interval,'$p_repeatdays','$p_endrepeat')";

  display_debug_msg($query, $cdg_sql);
  $obm_q =new DB_OBM;
  $obm_q->query($query); 
  
  /*$query="select calendarevent_id from CalendarEvent where calendarevent_title='$p_title' and calendarevent_usercreate='".$auth->auth["uid"]."' and calendarevent_datebegin='$p_datebegin' and calendarevent_dateend='$p_dateend'";
   $obm_db->query($query);
   $obm_db->next_record();
 
  return $obm_db->f("calendarevent_id");*/
}








///////////////////////////////////////////////////////////////
// Update an event (and its repetitions if specified)
///////////////////////////////////////////////////////////////
// Arguments :
// -----------
//   $p_event_id : event's id
//   $p_title : event's title 
//   $p_category_id : id of the event's category
//   $p_priority : event's priority (0,1 or 2)
//   $p_privacy : event's privacy flag (0 means public, 1 means private)
//   $p_datebegin : event's date of begin (like "YYYYmmddHHmmss")
//   $p_dateend : event's date of end (like "YYYYmmddHHmmss")
//   $p_description : event's description 
//   $p_occupied_day : flag (0 or 1) indicating if entire day is reserved for the task (or not) 
//   $all : flag (0 or 1) indicating if the event's repetition have to be updated too or not 
//
//////////////////////////////////////////////////////////////
function run_query_event_update($p_event_id,$p_title,$p_category_id,$p_priority,$p_privacy,$p_datebegin,$p_dateend,$p_description,$p_occupied_day,$all) {
 
  global $auth, $cdg_sql;
 
  if ($p_privacy != 1) {
    $p_privacy=0;
  }
  if ($p_occupied_day != 1) {
    $p_occupied_day=0;
  }

  $query = "update CalendarEvent set calendarevent_timeupdate='".date("YmdHis")."',calendarevent_userupdate=".$auth->auth["uid"].",calendarevent_title='".$p_title."',calendarevent_description='".$p_description."',calendarevent_category_id=".$p_category_id.",calendarevent_priority=".$p_priority.",calendarevent_privacy=".$p_privacy.",calendarevent_datebegin='".$p_datebegin."',calendarevent_dateend='".$p_dateend."',calendarevent_occupied_day=$p_occupied_day where calendarevent_id=".$p_event_id;
  
  if ($all==1) {
    $query.=" or calendarevent_origin_id=".$p_event_id;
  }

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}



//////////////////////////////////////////////////////////////////////////////
// Delete an event (and its repetitions if specified)
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $p_event_id : event's id
//    $all        : flag (0 or 1) indicating if all repetition of an event 
//                  are deleted too or not 
//////////////////////////////////////////////////////////////////////////////
function run_query_event_delete($p_event_id,$all) {
  global $cdg_sql;

  $obm_db = new DB_OBM;

  if ($all == 1) {
    // we get all events created from the one to delete 
    $query = "select calendarevent_id from CalendarEvent where calendarevent_id='$p_event_id' or calendarevent_origin_id='$p_event_id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db_id = new DB_OBM;
    $obm_db_id->query($query);
  }
  
  if ($all == 1) {
    if ($obm_db_id->nf() >0) {
      $query="delete from EventUser where eventuser_event_id in (";
      $obm_db_id->next_record();
      $query.=$obm_db_id->f("calendarevent_id");
      while ($obm_db_id->next_record()) {
	$query.=",".$obm_db_id->f("calendarevent_id");
      }
      $query.=")";
      display_debug_msg($query, $cdg_sql);
      $obm_db->query($query);
    }
  } else {
    $query = "delete from EventUser where eventuser_event_id='$p_event_id'";
    display_debug_msg($query, $cdg_sql);
    $obm_db->query($query);
  }

  $query = "delete from CalendarEvent where calendarevent_id='$p_event_id'";
  if ($all==1) {
    $query .= " or calendarevent_origin_id='$p_event_id'";
  }  
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  
}



////////////////////////////////////////////////////////////////////
// Insert a new event for the specified contact 
// only if there's no conflict with another event and if 
// this contact is not already assigned to the event 
////////////////////////////////////////////////////////////////////
// Arguments :
// ------------
//    $p_contact_id  : id of the contact the event is planned for
//    $p_event_id : id of the event  
//    $p_begindate : begin date of the event to insert ("YYYYmmddHHmmss")
//    $p_enddate   : end date of the event to insert ("YYYYmmddHHmmss") 
//    $p_state     : state of the event Accepted ("A"), rejected ("R") or waiting ("W")
// Return :
// --------
//    "0" if there is a conflict else "1"
///////////////////////////////////////////////////////////////////
function run_query_event_contact_insert($p_contact_id,$p_event_id,$p_begindate,$p_enddate,$p_state) {
  global $cdg_sql;

  $obm_db = new DB_OBM; 
  $flag=1;

 // is there already another event at the same time ?
  $obm_q_conflict_events = run_query_event_list($p_begindate,$p_enddate,array($p_contact_id),''); 

 if ($obm_q_conflict_events->nf() == 0) {   
   // if a user enters a new event for himself, it's considered as "Accepted"
   // else it's considered as "Waiting to be approved" 
   
   $query = "select * from EventUser where eventuser_user_id='$p_contact_id' and eventuser_event_id='$p_event_id'";
   display_debug_msg($query, $cdg_sql);
   $obm_db->query($query);
   if ($obm_db->nf()==0) { 
     $query = "insert into EventUser (eventuser_event_id,eventuser_user_id,eventuser_group_id,eventuser_state) values ('$p_event_id','$p_contact_id',null,'$p_state')";
     display_debug_msg($query, $cdg_sql);
     $obm_db->query($query); 
   }  
 } else {
   // conflict 
   $flag=0;   
 }   
 return $flag;
 
}



///////////////////////////////////////////////////////////////////
// Insert a new event for the specified group of contacts 
// only if there's no conflict with another event
//
// A group event is always in "accepted" state
////////////////////////////////////////////////////////////////////
// Arguments :
// ------------
//    $group_id  : id of the group the event is planned for
//    $event_id : id of the event  
// 
// Return :
// --------
//    "0" if there is a conflict else "1"
//
///////////////////////////////////////////////////////////////////
function run_query_event_group_insert($group_id,$event_id,$p_begindate,$p_enddate) {
  global $cdg_sql;

  $obm_db = new DB_OBM;
  $flag=1;

  // is there already another event at the same time ?
  $obm_q_conflict_events=run_query_event_list($p_begindate,$p_enddate,'',array($group_id)); 

  if ($obm_q_conflict_events->nf() == 0) {   
    // if a user enters a new event for himself, it's considered as "Accepted"
    // else it's considered as "Waiting to be approved" 
    
    // insertion of the association Event/group with "Accepted" state
    $query = "select * from EventUser where eventuser_group_id='".$group_id."' and eventuser_event_id='".$event_id ."'";
    display_debug_msg($query, $cdg_sql);
    $obm_db->query($query);
    if ($obm_db->nf()==0) { 
      $query = "insert into EventUser (eventuser_event_id,eventuser_user_id,eventuser_group_id,eventuser_state) values ('$event_id',null,'$group_id','A')";
      display_debug_msg($query, $cdg_sql);
      $obm_db->query($query); 
    }  
    
  } else {
    // conflict 
    $flag=0; 
  }
  
  return $flag;
}




/////////////////////////////////////////////////////////////////////////////
// Return all not rejected events begining between the 2 specified dates assigned to the specified contacts
// and/or groups 
// and the ones which began before and finish between these dates
//////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//    $p_time_begin : start date
//    $p_time_end : end date
//    $contacts_array : array containing the id of the contact(s) the event is assigned to 
//    $groups_array   : array containing the id of the group(s) the event is assigned to 
/////////////////////////////////////////////////////////////////////////////
function run_query_event_list($p_date_begin,$p_date_end,$contacts_array,$groups_array,$p_event_id_without=0) {
  global $cdg_sql;

  $obm_db = new DB_OBM;
  $query = "select calendarevent_id,
                 calendarevent_usercreate,
                 calendarevent_title,
                 calendarevent_priority,
                 calendarevent_privacy,
                 calendarevent_datebegin,
                 calendarevent_dateend,
                 eventcategory_label,
                 eventuser_user_id
            from CalendarEvent,EventCategory,EventUser
            where calendarevent_category_id=eventcategory_id
              and eventuser_state <> 'R'
              and eventuser_event_id=calendarevent_id";
  // calendarlayer_color ? 

  if (($contacts_array) || ($groups_array)) {
    $query .= " and ";
  } 
  if (($contacts_array) && ($groups_array)) {
    $query .= " ( ";
  }

  if ( ($contacts_array) && (count($contacts_array)>0) ) {
    $query .= "eventuser_user_id in ('";
    $query .= $contacts_array[0];
    for ($i=1; $i<count($contacts_array); $i++) {
      $query.= "','".$contacts_array[$i];
    }
    $query.="')";
  }
  
  if (($contacts_array) && ($groups_array)) {
    $query.=" or ";
  } 
  
  if ( ($groups_array) && (count($groups_array)>0) ) {
    $query.= " eventuser_group_id in ('";
    $query.= $groups_array[0];
    for ($i=1;$i<count($groups_array);$i++) {
      $query.= "','".$groups_array[$i];
    }
    $query.="')";
  }
  
  if (($contacts_array) && ($groups_array)) {
     $query.=" ) ";
  }
  
  // we want all events beginning between the two dates 
  // and the ones which began earlier but finish between these dates too
  $query .= " and (calendarevent_datebegin < $p_date_end and calendarevent_dateend >$p_date_begin
                  and calendarevent_datebegin<$p_date_end
                  )
             ";

  if ($p_event_id_without>0) {
    $query .= " and calendarevent_id <> ".$p_event_id_without;
  }
  

  $query.=" order by calendarevent_datebegin,calendarevent_timecreate"; 
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////
// Return information about the specified event
///////////////////////////////////////////////////////////////////////////
// Arguments :
// ------------
//   $p_event_id : event's id 
//////////////////////////////////////////////////////////////////////////
function run_query_get_event($p_event_id) {
  global $cdg_sql;

  $query = "select * from CalendarEvent where calendarevent_id='$p_event_id'";
  display_debug_msg($query, $cdg_sql);

  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;  
}


///////////////////////////////////////////////////////////////////////////
// Return information about all repetitions of an event 
///////////////////////////////////////////////////////////////////////////
function run_query_get_event_repetitions($p_event_id) {
  global $cdg_sql;

  $query = "select * from CalendarEvent where calendarevent_origin_id='$p_event_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db=new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////
// Return the id of the group(s) the event is assigned to
///////////////////////////////////////////////////////////////////////////
// Arguments :
// ------------
//   $p_event_id : event's id 
//////////////////////////////////////////////////////////////////////////
function run_query_get_eventgroups($p_event_id) {
  global $cdg_sql;

  $query = "select eventuser_group_id,list_name from EventUser,List where list_id=eventuser_group_id and eventuser_event_id='$p_event_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;  
}





///////////////////////////////////////////////////////////////////////////
// return the id of the contacts the event is assigned to
///////////////////////////////////////////////////////////////////////////
// Arguments :
// ------------
//   $p_event_id : event's id 
//////////////////////////////////////////////////////////////////////////
function run_query_get_eventuser($p_event_id) {
  global $cdg_sql;

  $query = "select userobm_id, userobm_lastname, userobm_firstname,
      eventuser_state
    from EventUser, UserObm
    where userobm_id = eventuser_user_id
      and eventuser_event_id='$p_event_id'";
  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  return $obm_db;  
}


//////////////////////////////////////////////////////////////////////////
// Remove associations between the specified group (and its members) 
// and the specified event 
//////////////////////////////////////////////////////////////////////////
// Args : 
// ------
//   $p_event_id : event's id
//   $p_group_id : group's id   (!) 
//////////////////////////////////////////////////////////////////////////
function run_query_event_group_delete($p_event_id,$p_group_id) {
  global $cdg_sql;

  $obm_db=run_query_get_group_members($p_group_id);

  while ($obm_db->next_record()) {
    $id_array[]=$obm_db->f("ContactList_contactid");
  }

  $query="delete from EventUser 
          where (eventuser_group_id=".$p_group_id;

  if ($id_array && count($id_array>0)) {
    $query.= " or eventuser_user_id in (".$id_array[0];
    for ($i=1;$i<count($id_array);$i++) {
      $query.=",".$id_array[$i];
    }
    $query .= ")";
  }
  
  $query .= " ) and eventuser_event_id=".$p_event_id;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

}


//////////////////////////////////////////////////////////////////////
// Remove associations between the specified group (and its members) 
// and the specified event 
//////////////////////////////////////////////////////////////////////
// Args : 
// ------
//   $p_event_id : event's id
//   $p_contact_id : contact's id  (!) 
//////////////////////////////////////////////////////////////////////
function run_query_event_contact_delete($p_event_id,$p_contact_id) {
  global $cdg_sql;

  $query = "delete from EventUser where eventuser_user_id='$p_contact_id' and eventuser_event_id='$p_event_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
}


////////////////////////////////////////////////////////////////////
// Change an event's state
////////////////////////////////////////////////////////////////////
// Args :
// ------
//    $p_event_id : event's id
//    $p_contact_id : contact's id 
//    $p_state : new state ("A", "R", or "W")
////////////////////////////////////////////////////////////////////
function run_query_event_state_update($p_event_id,$p_contact_id,$p_state) {
  global $cdg_sql;

  $query = "update EventUser set eventuser_state='$p_state' where eventuser_event_id='$p_event_id' and eventuser_user_id='$p_contact_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
}


////////////////////////////////////////////////////////////////////
// Get all waiting events of the specified contact 
////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//     $contact_id : contact's id ....
////////////////////////////////////////////////////////////////////
function run_query_get_waiting_events($contact_id) {
  global $cdg_sql;

  $query="select calendarevent_id, calendarevent_title, calendarevent_datebegin,calendarevent_dateend  
          from CalendarEvent,EventUser 
          where calendarevent_id=eventuser_event_id 
            and eventuser_user_id='$contact_id'
            and eventuser_state='W'
          order by calendarevent_datebegin";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  return $obm_db;
}


////////////////////////////////////////////////////////////////////
// Get all rejected events created by the specified user 
////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//     $p_user_id : user's id (!)
////////////////////////////////////////////////////////////////////
function run_query_get_rejected_events($p_user_id) {
  global $cdg_sql;

  $query = "select calendarevent_id, calendarevent_title, calendarevent_datebegin,calendarevent_dateend, contact_lastname, contact_firstname  
          from CalendarEvent,EventUser, Contact 
          where calendarevent_id=eventuser_event_id 
            and contact_id=eventuser_user_id 
            and eventuser_state='R'
            and calendarevent_usercreate='$p_user_id'
          order by calendarevent_datebegin";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  return $obm_db;
}


////////////////////////////////////////////////////////////////////
// Get the color layer of the specified contact
////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//     contact_id
////////////////////////////////////////////////////////////////////
function run_query_get_contactlayer(/*$owner_id,*/$contact_id) {
  global $cdg_sql;

   
  $query = "select calendarlayer_color from CalendarLayer
            where calendarlayer_user_id='$contact_id'";
  // and calendarlayer_owner_id=$owner_id

  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();

  return $obm_db->f("calendarlayer_color");
}


////////////////////////////////////////////////////////////////////
// Get the color layer of the specified group
////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//     group_id
////////////////////////////////////////////////////////////////////
function run_query_get_grouplayer(/*$owner_id,*/$group_id) {
  global $cdg_sql;

   
  $query = "select calendarlayer_color from CalendarLayer
            where calendarlayer_group_id='$group_id'";
  // and calendarlayer_owner_id=$owner_id
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  
  return $obm_db->f("calendarlayer_color");
}


/////////////////////////////////////////////////////////////////////////
// Get all event categories 
/////////////////////////////////////////////////////////////////////////
function run_query_get_eventcategories() {
  global $cdg_sql;

  $query = "select * from EventCategory";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get all User Groups
///////////////////////////////////////////////////////////////////////////////
function run_query_get_usergroups() {
  global $cdg_sql;

  $query = "select * from List order by list_name";
  display_debug_msg("run_query_get_usergroups() : not implemented".$query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db;
}


//////////////////////////////////////////////////////////////////////////
// Return the id of all member of the specified group (list)
//////////////////////////////////////////////////////////////////////////
// Args :
// ------
//   $p_group_id : id of the group 
//////////////////////////////////////////////////////////////////////////
function run_query_get_group_members ($p_group_id) {
  global $cdg_sql;

  $query = "select ContactList_contactid
            from ContactList
            where ContactList_listid='$p_group_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db=new DB_OBM;  
  $obm_db->query($query);
  return $obm_db;
}


//////////////////////////////////////////////////////////////////////////
// Return the greastest id among all event's id in the events table 
//////////////////////////////////////////////////////////////////////////
function run_query_get_max_event_id () {
  global $cdg_sql;

  $query = "select max(calendarevent_id) from CalendarEvent";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  $obm_db->next_record();
  return $obm_db->f("max(calendarevent_id)");
}


/////////////////////////////////////////////////////////////////////////
// test if the contact is assigned to the specified event
/////////////////////////////////////////////////////////////////////////
// Args :
// ------
//   $p_event_id : event's id
//   $p_contact_id : contact's id 
// Return :
// --------
//   1 if the contact is assigned to the event, else 0
/////////////////////////////////////////////////////////////////////////
function run_query_is_attendee($p_event_id,$p_contact_id) {
  global $cdg_sql;

  $query = "select * from EventUser where eventuser_user_id='$p_contact_id' and eventuser_event_id='$p_event_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  return $obm_db->nf()>0;
}


////////////////////////////////////////////////////////////////////////
// Get infos about one group (list) identified buy it's id
////////////////////////////////////////////////////////////////////////
// Args : 
// ------
//    $p_group_id : group's id
///////////////////////////////////////////////////////////////////////
function run_query_group($p_group_id) {
  global $cdg_sql;

  $query = "select * from List where list_id='$p_group_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_db = new DB_OBM;
  $obm_db->query($query);
  
  return $obm_db;
}

</SCRIPT>

