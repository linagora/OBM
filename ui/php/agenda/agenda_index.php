<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : agenda_index.php                                           //
//     - Desc  : Agenda Index File                                           //
// created 2001-06-28 by Francois Bloque                                     //
///////////////////////////////////////////////////////////////////////////////
// Last update 2001-07-26                                                    //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$menu="AGENDA";
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");
include("$obminclude/global_pref.inc");

require("agenda_query.inc");
require("agenda_display.inc");


page_close();

include("agenda_functions.inc");


///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_deal);     // Head & Body
generate_menu($menu);      // Menu
display_bookmarks();       // Links to last visited contact,and company
//////////////////////////////////////////////////////////////////////////////

require("agenda_js.inc");


if ($action == "index") {
  //////////////////////////////////////////////////////////////////////////////
  
  $obm_q_waiting_events=run_query_get_waiting_events(run_query_contactid_user($auth->auth["uid"]));
  if ($obm_q_waiting_events->nf()>0) {
    // display events to confirm
    dis_waiting_events_list($obm_q_waiting_events);
  } else {
    
    // week planning
    $p_date=date("YmdHis");
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);
    $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));       
    // Weekstart :
    if ($dow == 0) {
      $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
    } elseif ( $dow == 1 ) {
      $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
    } else {
      $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
    }
    $weekend=date("YmdHis",$weekstart + 7 * 86400);
    $weekstart=date("YmdHis",$weekstart);  
    $action="view_week";
    dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
    dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
    
  }


} else if ($action == "approve") {
  //////////////////////////////////////////////////////////////////////////////
  
  // accepted events id
  reset($HTTP_POST_VARS);
  while ( list( $key ) = each( $HTTP_POST_VARS ) ) {
     if(strcmp(substr($key, 0, 12),"cb_accepted_") == 0) {
       run_query_event_state_update(substr($key,12),run_query_contactid_user($auth->auth["uid"]),"A");   
     } 
  }
  // rejected events id
  reset($HTTP_POST_VARS);
  while ( list( $key ) = each( $HTTP_POST_VARS ) ) {
     if(strcmp(substr($key, 0, 12),"cb_rejected_") == 0) {
       run_query_event_state_update(substr($key,12),run_query_contactid_user($auth->auth["uid"]),"R");
     } 
  }  
  display_ok_msg($l_event_approve_ok);




} else if ($action == "new") {
  //////////////////////////////////////////////////////////////////////////////

 
  $p_current_date=date("YmdHis",mktime(9,0,0,date("m"),date("d"),date("Y")));
  $p_date_begin=($param_begin_date ? $param_begin_date : $p_current_date);
  $p_date_end=($param_end_date ? $param_end_date : $p_current_date);
 
  dis_event_form($action,new DB_OBM,new DB_OBM,new DB_OBM,'',run_query_get_mycontacts(), run_query_get_mylists(), run_query_get_eventcategories(),'',array(run_query_contactid_user($auth->auth["uid"])),'','','','',$p_date_begin,$p_date_end,'','',"0",'','','');
    
        



} else if ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////  
  $nb_contact_chosen = count($sel_user_id);
  $nb_group_chosen = count($sel_group_id);

  // begin date
  $begindate=$sel_year_begin.$sel_month_begin.$sel_day_begin;
  if ($cb_occupied_day == 1) {
    $begindate.="080000";
  } else { 
    $begindate.=$tf_hour_begin.$tf_minute_begin."00"; 
  }
  //end date
  $enddate=$sel_year_end.$sel_month_end.$sel_day_end;
  if ($cb_occupied_day == 1) {
    $enddate.="180000";
  } else { 
    $enddate.=$tf_hour_end.$tf_minute_end."00"; 
  }
  // end repetition date 
  $enddate_repeat=$sel_year_repeatend.$sel_month_repeatend.$sel_day_repeatend.$tf_hour_repeatend.$tf_minute_repeatend."000000";
 
  // days of repeat when repeat kind is "weekly"
  $repeat_days="0000000";
  reset($HTTP_POST_VARS);
  while ( list( $key ) = each( $HTTP_POST_VARS ) ) {
     if(strcmp(substr($key, 0, 13),"cb_repeatday_") == 0) {
       $repeat_days[substr($key,13)]="1";
     } 
  }
 
  if (($nb_contact_chosen<=0) && ($nb_group_chosen<=0)) {
    // no contacts to assign to the event 
    echo "<FONT color=#$col_error>".$l_select_contacts."</FONT><BR>";
    dis_event_form("new",new DB_OBM,new DB_OBM,new DB_OBM,'',run_query_get_mycontacts(), run_query_get_mylists(), run_query_get_eventcategories(),$tf_event_title,$sel_user_id,$sel_group_id,$sel_category_id,$sel_priority,$cb_private_event,$begindate,$enddate,$ta_event_description,$cb_occupied_day,$sel_repeat_kind,$tf_repeat_interval,$repeat_days,$enddate_repeat);
  

  } else if ((strcmp($repeat_days,"0000000")==0) && (strcmp($sel_repeat_kind,"weekly")==0))  {
    // no day is selected     
    echo "<FONT color=#$col_error>".$l_select_repeat_day."</FONT><BR>";
    dis_event_form("new",new DB_OBM,new DB_OBM,new DB_OBM,'',run_query_get_mycontacts(), run_query_get_mylists(), run_query_get_eventcategories(),$tf_event_title,$sel_user_id,$sel_group_id,$sel_category_id,$sel_priority,$cb_private_event,$begindate,$enddate,$ta_event_description,$cb_occupied_day,$sel_repeat_kind,$tf_repeat_interval,$repeat_days,$enddate_repeat);



  } else {
    // everything's ok
    $day_diff=$sel_day_end-$sel_day_begin;
    $month_diff=$sel_month_end-$sel_month_begin;
    $year_diff=$sel_year_end-$sel_year_begin;
    
    // compute of the repeating begin dates 
    if (($sel_day_repeatend==0) || ($sel_month_repeatend==0) || ($sel_year_repeatend==0)) {
      $p_end_date='';
      $enddate_repeat="000000000000";
    } else {  
      $p_end_date=$enddate_repeat;
    }
    $event_dates_begin=get_event_repetition_dates($begindate,$sel_repeat_kind,$tf_repeat_interval,$repeat_days,$p_end_date); 
   
    //compute of the repeating end dates 
    if ((count($event_dates_begin)) && (count($event_dates_begin)>0) ){
      for ($i=0;$i<count($event_dates_begin);$i++) {
	$year=substr($event_dates_begin[$i],0,4);
	$month=substr($event_dates_begin[$i],4,2);
	$day=substr($event_dates_begin[$i],6,2);
	$event_dates_end[] = date("YmdHis",mktime(substr($enddate,8,2),substr($enddate,10,2),0,$month+$month_diff,$day+$day_diff,$year+$year_diff));
      }
    } 

    $max_event_id=run_query_get_max_event_id();
    // event's insertion
    run_query_event_insert($max_event_id+1,$tf_event_title,$sel_category_id,$sel_priority,$cb_private_event,$begindate,$enddate,$ta_event_description,'',$cb_occupied_day,$sel_repeat_kind,$tf_repeat_interval,$repeat_days,$enddate_repeat);


    // IS THERE CONFLICT ? 
    $events_array[]=run_query_get_event($max_event_id+1);
    // for each contact chosen : assignement of the event to the contact
    for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) {
      $obm_q_conflict_events=run_query_event_list($begindate,$enddate,array($sel_user_id[$i]),''); 
      if ($obm_q_conflict_events->nf() > 0) {  
	$contact_id_conflict_array[$max_event_id+1][]=run_query_contact($sel_user_id[$i]);
      }
    }
    // for each group chosen : assignement of the event to the group 
    for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
      $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
      $obm_q_conflict_events=run_query_event_list($begindate,$enddate,'',array($sel_group_id[$i])); 
      if ($obm_q_conflict_events->nf() > 0) {  
	$group_id_conflict_array[$max_event_id+1][]=run_query_group($sel_group_id[$i]);
      }
      while ($obm_q_members->next_record()) {
	$obm_q_conflict_events=run_query_event_list($begindate,$enddate,array($obm_q_members->f("ContactList_contactid")),'');
	if ($obm_q_conflict_events->nf() > 0) {  
	  $contact_id_conflict_array[$max_event_id+1][]=run_query_contact($obm_q_members->f("ContactList_contactid"));
	}
      }
    }

    if ((count($event_dates_begin) == count($event_dates_end)) && (count($event_dates_begin)>0) ){
      // for each repetition of the event : 
      for ($cpt_events=0;$cpt_events<count($event_dates_begin);$cpt_events++) {
	$max_repeated_id_array[$cpt_events]=run_query_get_max_event_id();
	run_query_event_insert($max_repeated_id_array[$cpt_events]+1,$tf_event_title,$sel_category_id,$sel_priority,$cb_private_event,$event_dates_begin[$cpt_events],$event_dates_end[$cpt_events],$ta_event_description,$max_event_id+1,$cb_occupied_day,$sel_repeat_kind,$tf_repeat_interval,$repeat_days,$enddate_repeat);
	$events_array[]=run_query_get_event($max_repeated_id_array[$cpt_events]+1);
	// for each contact chosen : assignement of the event to the contact
	for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
	  $obm_q_conflict_events=run_query_event_list($begindate,$enddate,array($sel_user_id[$i]),'');
	  if ($obm_q_conflict_events->nf() > 0) {  
	    $contact_id_conflict_array[$max_repeated_id_array[$cpt_events]+1][]=run_query_contact($sel_user_id[$i]);
	  }
	} 
	// for each group chosen : assignement of the event to the group 
	for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	  $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	  $obm_q_conflict_events=run_query_event_list($begindate,$enddate,'',array($sel_group_id[$i]));
	  if ($obm_q_conflict_events->nf() > 0) {  
	    $group_id_conflict_array[$max_repeated_id_array[$cpt_events]+1][]=run_query_group($sel_group_id[$i]);
	  }
	  while ($obm_q_members->next_record()) {
	    $obm_q_conflict_events=run_query_event_list($begindate,$enddate,array($obm_q_members->f("ContactList_contactid")),'');
	    if ($obm_q_conflict_events->nf() > 0) {  
	      $contact_id_conflict_array[$max_repeated_id_array[$cpt_events]+1][]=run_query_contact($obm_q_members->f("ContactList_contactid"));
	    }
	  } 
	}
      }
    } 
    
    if ( (!$contact_id_conflict_array) && (!$group_id_conflict_array)) {
      // NO CONFLICT 
      
      // for each contact chosen : assignement of the event to the contact
      for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
	// if the event is a repeated or if it's assigned to yourself then it's in "accepted" state
	// else it's in "waiting" state 
	if (strcmp($sel_repeat_kind,0) == 0) {
	  $p_state=(run_query_contactid_user($auth->auth["uid"])==$sel_user_id[$i] ? "A" : "W");
	} else { 
	  $p_state="A";
	} 
	run_query_event_contact_insert($sel_user_id[$i],$max_event_id+1,$begindate,$enddate,$p_state);
      }
      // for each group chosen : assignement of the event to the group 
      for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	$obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	run_query_event_group_insert($sel_group_id[$i],$max_event_id+1,$begindate,$enddate);
	while ($obm_q_members->next_record()) {
	  run_query_event_contact_insert($obm_q_members->f("ContactList_contactid"),$max_event_id+1,$begindate,$enddate,"A");
	} 
      }
      
      if ((count($event_dates_begin) == count($event_dates_end)) && (count($event_dates_begin)>0) ){
	// for each repetition of the event : 
	for ($cpt_events=0;$cpt_events<count($event_dates_begin);$cpt_events++) {
	  // for each contact chosen : assignement of the event to the contact
	  for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
	    run_query_event_contact_insert($sel_user_id[$i],$max_repeated_id_array[$cpt_events]+1,$begindate,$enddate,"A");
	  } 
	  // for each group chosen : assignement of the event to the group 
	  for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	    $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	    run_query_event_group_insert($sel_group_id[$i],$max_repeated_id_array[$cpt_events]+1,$begindate,$enddate);
	    while ($obm_q_members->next_record()) {
	      run_query_event_contact_insert($obm_q_members->f("ContactList_contactid"),$max_repeated_id_array[$cpt_events]+1,$begindate,$enddate,"A");
	    } 
	  }
	}
      } 

      display_ok_msg($l_event_insert_ok);
      
      // Display week planning
      $p_date=date("YmdHis");
      $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
      $p_year=substr($p_date,0,4);
      $p_month=substr($p_date,4,2);
      $p_day=substr($p_date,6,2);
      $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
      // Weekstart :
      if ($dow == 0) {
	$weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
      } elseif ( $dow == 1 ) {
	$weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
      } else {
	$weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
      }
      $weekend=date("YmdHis",$weekstart + 7 * 86400);
      $weekstart=date("YmdHis",$weekstart);
      $action="view_week";
      dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
      dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
  
  
    }  else {
      // CONFLICT 
      echo "<FONT color=#$col_error>".$l_conflict_events."</FONT><BR>";
      dis_conflict_events_form($action,$max_event_id+1,$events_array,$contact_id_conflict_array,$group_id_conflict_array,$sel_user_id,$sel_group_id);   
    }

  }
    



} else if ($action == "cancel_insert") {
  ////////////////////////////////////////////////////////////////////////////////////////////

  if ($param_event>0) {
    run_query_event_delete($param_event,1);
    display_ok_msg($l_event_insert_cancel_ok);

    // Display week planning
    $p_date=date("YmdHis");
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);
    $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
    // Weekstart :
    if ($dow == 0) {
      $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
    } elseif ( $dow == 1 ) {
      $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
    } else {
      $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
      }
    $weekend=date("YmdHis",$weekstart + 7 * 86400);
    $weekstart=date("YmdHis",$weekstart);
    $action="view_week";
    dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
    dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
      
  }




} else if ($action == "confirm_insert") {
  ////////////////////////////////////////////////////////////////////////////////////////////
  
  if ($param_event>0) {
 
    reset($HTTP_POST_VARS);
    while (list($key) =each($HTTP_POST_VARS) ) {
      if (strcmp(substr($key,0,8),"contact_") == 0) {
	$sel_user_id[]=$HTTP_POST_VARS[$key];
      }
    }
    reset($HTTP_POST_VARS);
    while (list($key) =each($HTTP_POST_VARS) ) {
      if (strcmp(substr($key,0,6),"group_") == 0) {
	$sel_group_id[]=$HTTP_POST_VARS[$key];
      }
    }

    $obm_q_event=run_query_get_event($param_event);
    $obm_q_event->next_record();
    
    $nb_group_chosen=count($sel_group_id);
    $nb_contact_chosen=count($sel_user_id);
    $begindate=$obm_q_event->f("calendarevent_datebegin");
    $enddate=$obm_q_event->f("calendarevent_dateend");

    // for each contact chosen : assignement of the event to the contact
    for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
      // if the event is a repeated or if it's assigned to yourself then it's in "accepted" state
      // else it's in "waiting" state 
      if (strcmp($sel_repeat_kind,0) == 0) {
	$p_state=(run_query_contactid_user($auth->auth["uid"])==$sel_user_id[$i] ? "A" : "W");
      } else { 
	$p_state="A";
      } 
      run_query_event_contact_insert($sel_user_id[$i],$param_event,$begindate,$enddate,$p_state);
    }
    // for each group chosen : assignement of the event to the group 
    for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
      $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
      run_query_event_group_insert($sel_group_id[$i],$param_event,$begindate,$enddate);
      while ($obm_q_members->next_record()) {
	run_query_event_contact_insert($obm_q_members->f("ContactList_contactid"),$param_event,$begindate,$enddate,"A");
      } 
    }
    
    $obm_q_repeated_events=run_query_get_event_repetitions($param_event);
    if ($obm_q_repeated_events->nf() > 0) {
      // for each repetition of the event : 
      while ($obm_q_repeated_events->next_record()) {
	// for each contact chosen : assignement of the event to the contact
	for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
	  run_query_event_contact_insert($sel_user_id[$i],$obm_q_repeated_events->f("calendarevent_id"),$begindate,$enddate,"A");
	} 
	// for each group chosen : assignement of the event to the group 
	for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	  $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	  run_query_event_group_insert($sel_group_id[$i],$obm_q_repeated_events->f("calendarevent_id"),$begindate,$enddate);
	  while ($obm_q_members->next_record()) {
	    run_query_event_contact_insert($obm_q_members->f("ContactList_contactid"),$obm_q_repeated_events->f("calendarevent_id"),$begindate,$enddate,"A");
	  } 
	}
      }
    }

    display_ok_msg($l_event_insert_ok);
    
    // Display week planning
    $p_date=date("YmdHis");
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);
    $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
    // Weekstart :
    if ($dow == 0) {
      $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
    } elseif ( $dow == 1 ) {
      $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
    } else {
      $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
    }
    $weekend=date("YmdHis",$weekstart + 7 * 86400);
    $weekstart=date("YmdHis",$weekstart);
    $action="view_week";
    dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
    dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
    
  }
  



} else if ($action == "detailconsult") {
  /////////////////////////////////////////////////////////////////////////////////////////////

  if ($param_event>0) {
    
    $obm_q_event = run_query_get_event($param_event);
    $obm_q_event->next_record();
    // is the current user among the event's contact ?
    $member=0;
    $obm_q_members=run_query_get_eventcontacts($param_event);
    while ($obm_q_members->next_record() && !$member) {
      if ($obm_q_members->f("contact_id")==run_query_contactid_user($auth->auth["uid"])) {
	$member=1;
      }
    }   
    dis_event_consult($obm_q_event,run_query_get_eventcategories(),run_query_get_eventcontacts($param_event),run_query_get_eventgroups($param_event),$member);
 
  }





} elseif ($action == "detailupdate") {
  /////////////////////////////////////////////////////////////////////////////////////////////  
  
  if ($param_event>0) {
    
    $obm_q_event=run_query_get_event($param_event);
    $obm_q_event->next_record();
    $obm_q_contacts=run_query_get_eventcontacts($param_event);
    $obm_q_groups=run_query_get_eventgroups($param_event);
       
    // a user can modify an event if he's concerned by the event or if he's the creator 
    if (($obm_q_event->f("calendarevent_usercreate") == $auth->auth["uid"]) || (run_query_is_attendee($param_event,run_query_contactid_user($auth->auth["uid"])))) {
      $p_current_date=date("YmdHis",mktime(9,0,0,date("m"),date("d"),date("Y")));
      dis_event_form($action,$obm_q_event,$obm_q_contacts,$obm_q_groups,$param_event,run_query_get_mycontacts(), run_query_get_mylists(), run_query_get_eventcategories(),'','','','','','','','','','',"0",'','','');
    } else {
      // the user has not rights to modify this event 
      display_error_permission();  
    }
    
  }
 
 



} else if ($action == "update") {
  ////////////////////////////////////////////////////////////////////////////////////////////

  if ($param_event>0) {
    
    $obm_q_event=run_query_get_event($param_event);
    $obm_q_event->next_record();
    // a user can modify an event if he's concerned by the event or if he's the creator 
    if (($obm_q_event->f("calendarevent_usercreate") == $auth->auth["uid"]) || (run_query_is_attendee($param_event,run_query_contactid_user($auth->auth["uid"])))) {
      
      $nb_contact_chosen = count($sel_user_id);
      $nb_group_chosen = count($sel_group_id);   
      // begin date
      $begindate=$sel_year_begin.$sel_month_begin.$sel_day_begin;
      if ($cb_occupied_day == 1) {
	$begindate.="080000";
      } else { 
	$begindate.=$tf_hour_begin.$tf_minute_begin."00"; 
      }
      //end date
      $enddate=$sel_year_end.$sel_month_end.$sel_day_end;
      if ($cb_occupied_day == 1) {
	$enddate.="180000";
      } else { 
	$enddate.=$tf_hour_end.$tf_minute_end."00"; 
      }
      // end repetition date 
      $enddate_repeat=$sel_year_repeatend.$sel_month_repeatend.$sel_day_repeatend.$tf_hour_repeatend.$tf_minute_repeatend."000000";      
      
      $max_event_id=run_query_get_max_event_id();
      // event's insertion
      run_query_event_insert($max_event_id+1,$tf_event_title,$sel_category_id,$sel_priority,$cb_private_event,$begindate,$enddate,$ta_event_description,'',$cb_occupied_day,'','','','');
      $events_array[]=run_query_get_event($max_event_id+1);
      
      // IS THERE A CONFLICT ?
      // each contact 
      for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
	// insert the association contact / event if it isn't already exist
	$obm_q_conflict_events=run_query_event_list($begindate,$enddate,array($sel_user_id[$i]),'',$param_event); 
	if ($obm_q_conflict_events->nf() > 0) { 
	  $contact_id_conflict_array[$max_event_id+1][]=run_query_contact($sel_user_id[$i]);
	}
      }
      // each group
      for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	$obm_q_conflict_events=run_query_event_list($begindate,$enddate,'',array($sel_group_id[$i]),$param_event); 
	if ($obm_q_conflict_events->nf() > 0) {  
	  $group_id_conflict_array[$max_event_id+1][]=run_query_group($sel_group_id[$i]);
	}
	$obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	while ($obm_q_members->next_record()) {
	  $obm_q_conflict_events=run_query_event_list($begindate,$enddate,array($obm_q_members->f("ContactList_contactid")),'',$param_event); 
	  if ($obm_q_conflict_events->nf() > 0) {  
	    $contact_id_conflict_array[$max_event_id+1][]=run_query_contact($obm_q_members->f("ContactList_contactid"));
	  }
	}
      }
      
      
      if ( (!$contact_id_conflict_array) && (!$group_id_conflict_array) ) {
	// NO CONFLICT

	run_query_event_update($param_event,$tf_event_title,$sel_category_id,$sel_priority,$cb_private_event,$begindate,$enddate,$ta_event_description,$cb_occupied_day,0);
	run_query_event_delete($max_event_id+1,1);
	
	// add each new contact 
	for ( $i = 0; $i <  $nb_contact_chosen; $i++ ) { 
	  // if the event is a repeated or if it's assigned to yourself then it's in "accepted" state
	  // else it's in "waiting" state 
	  if (strcmp($sel_repeat_kind,0) == 0) {
	    $p_state=(run_query_contactid_user($auth->auth["uid"])==$sel_user_id[$i] ? "A" : "W");
	  } else { 
	    $p_state="A";
	  }
	  // insert the association contact / event if it isn't already exist
	  run_query_event_contact_insert($sel_user_id[$i],$param_event,$begindate,$enddate,$p_state);
	}
	  
	// add each new group (without its members)
	for ( $i = 0; $i < $nb_group_chosen; $i++ ) {
	  $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	  // insert the association group / event if it isn't already exist
	  run_query_event_group_insert($sel_group_id[$i],$param_event,$begindate,$enddate);
	}
	
	// determinate which contacts have been removed and delete them 
	$obm_q_event_contacts=run_query_get_eventcontacts($param_event);
	while ($obm_q_event_contacts->next_record()) {
	  $chosen=0;
	  for ( $i = 0; ($i < $nb_contact_chosen) && !$chosen; $i++ ) { 
	    if ($obm_q_event_contacts->f("contact_id")==$sel_user_id[$i]) 
	      $chosen=1;
	  }
	  if (!$chosen) 
	    // remove the association contact / event 
	    run_query_event_contact_delete($param_event,$obm_q_event_contacts->f("contact_id"));	  
	}
	
	// determinate which groups have been removed and delete them and their members
	$obm_q_event_groups=run_query_get_eventgroups($param_event);
	while ($obm_q_event_groups->next_record()) {
	  $chosen=0;
	  for ( $i = 0; ($i < $nb_group_chosen) && !$chosen; $i++ ) { 
	    if ($obm_q_event_groups->f("eventuser_group_id")==$sel_group_id[$i]) 
	      $chosen=1;
	  }
	  if (!$chosen) { 
	    // remove the assocation group / event and the associations group members / event 
	    run_query_event_group_delete($param_event,$obm_q_event_groups->f("eventuser_group_id"));
	  }
	}
	
	// add members of each group
	for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	  $obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	    while ($obm_q_members->next_record()) {
	      // insert the association group member/event if it isn't already exist
	      run_query_event_contact_insert($obm_q_members->f("ContactList_contactid"),$param_event,$begindate,$enddate,"A");
	    }
	}
	
	display_ok_msg($l_event_update_ok);
     
	// Display week planning
	$p_date=date("YmdHis");
	$p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
	$p_year=substr($p_date,0,4);
	$p_month=substr($p_date,4,2);
	$p_day=substr($p_date,6,2);
	$dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
	// Weekstart :
	if ($dow == 0) {
	  $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
	} elseif ( $dow == 1 ) {
	  $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
	} else {
	  $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
	}
	$weekend=date("YmdHis",$weekstart + 7 * 86400);
	$weekstart=date("YmdHis",$weekstart);
	$action="view_week";
	dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
	dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
      
	
	
      } else {
	// CONFLICT   
	echo "<FONT color=#$col_error>".$l_conflict_events."</FONT><BR>";
	dis_conflict_events_form($action,$param_event,$events_array,$contact_id_conflict_array,$group_id_conflict_array,$sel_user_id,$sel_group_id);   
      }
      
      
    } else {  
      // Not allowed 
      display_error_permission();
    }
      
  }


  



} else if ($action == "cancel_update") {
  //////////////////////////////////////////////////////
  echo $param_temp_event."<br>";
  
  if ($param_temp_event>0) {
    run_query_event_delete($param_temp_event,1);
    display_ok_msg($l_event_update_cancel_ok);

    // Display week planning
    $p_date=date("YmdHis");
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);
    $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
    // Weekstart :
    if ($dow == 0) {
      $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
    } elseif ( $dow == 1 ) {
      $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
    } else {
      $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
      }
    $weekend=date("YmdHis",$weekstart + 7 * 86400);
    $weekstart=date("YmdHis",$weekstart);
    $action="view_week";
    dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
    dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
      
  }
  




    
} else if ($action == "confirm_update")   {
  //////////////////////////////////////////////////////

  if (($param_event>0) && ($param_temp_event>0)) {
    
    $obm_q_event=run_query_get_event($param_event);
    $obm_q_event->next_record();
    // a user can modify an event if he's concerned by the event or if he's the creator 
    if (($obm_q_event->f("calendarevent_usercreate") == $auth->auth["uid"]) || (run_query_is_attendee($param_event,run_query_contactid_user($auth->auth["uid"])))) {
      
      reset($HTTP_POST_VARS);
      while (list($key) =each($HTTP_POST_VARS) ) {
	if (strcmp(substr($key,0,8),"contact_") == 0) {
	  $sel_user_id[]=$HTTP_POST_VARS[$key];
	}
      }
      reset($HTTP_POST_VARS);
      while (list($key) =each($HTTP_POST_VARS) ) {
	if (strcmp(substr($key,0,6),"group_") == 0) {
	  $sel_group_id[]=$HTTP_POST_VARS[$key];
	}
      }

      reset($HTTP_POST_VARS);
      while (list($key) =each($HTTP_POST_VARS) ) {
	if (strcmp(substr($key,0,17),"contact_conflict_") == 0) {
	  $conflict_contacts_array[]=$HTTP_POST_VARS[$key];
	}
      }
      reset($HTTP_POST_VARS);
      while (list($key) =each($HTTP_POST_VARS) ) {
	if (strcmp(substr($key,0,15),"group_conflict_") == 0) {
	  $groups_conflict_array[]=$HTTP_POST_VARS[$key];
	}
      }

      $nb_contact_chosen = count($sel_user_id);
      $nb_group_chosen = count($sel_group_id);   
  
      $obm_q_temp_event=run_query_get_event($param_temp_event);
      $obm_q_temp_event->next_record();
      
      // fields 
      $begindate=$obm_q_temp_event->f("calendarevent_datebegin");
      $enddate=$obm_q_temp_event->f("calendarevent_dateend");
      $tf_event_title=$obm_q_temp_event->f("calendarevent_title");
      $sel_category_id=$obm_q_temp_event->f("calendarevent_category_id");
      $sel_priority=$obm_q_temp_event->f("calendarevent_priority");
      $cb_private_event=$obm_q_temp_event->f("calendarevent_privacy");
      $ta_event_description=$obm_q_temp_event->f("calendarevent_description");
      $cb_occupied_day=$obm_q_temp_event->f("calendarevent_occupied_day");
      
      run_query_event_update($param_event,$tf_event_title,$sel_category_id,$sel_priority,$cb_private_event,$begindate,$enddate,$ta_event_description,$cb_occupied_day,0);
      run_query_event_delete($param_temp_event,1);

      // add each new contact 
      for ( $i = 0; $i < $nb_contact_chosen; $i++ ) { 
	// if the event is a repeated or if it's assigned to yourself then it's in "accepted" state
	// else it's in "waiting" state 
	if (strcmp($sel_repeat_kind,0) == 0) {
	  $p_state=(run_query_contactid_user($auth->auth["uid"])==$sel_user_id[$i] ? "A" : "W");
	} else { 
	  $p_state="A";
	}
	// insert the association contact / event if it isn't already exist
	run_query_event_contact_insert($sel_user_id[$i],$param_event,$begindate,$enddate,$p_state);
      }
     
      // add each new group (without its members)
      for ( $i = 0; $i < $nb_group_chosen; $i++ ) {
	$obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	// insert the association group / event if it isn't already exist
	run_query_event_group_insert($sel_group_id[$i],$param_event,$begindate,$enddate);
      }
      
      // determinate which contacts have been removed and delete them 
      $obm_q_event_contacts=run_query_get_eventcontacts($param_event);
      while ($obm_q_event_contacts->next_record()) {
	$chosen=0;
	for ( $i = 0; ($i < $nb_contact_chosen) && !$chosen; $i++ ) { 
	  if ($obm_q_event_contacts->f("contact_id")==$sel_user_id[$i]) 
	    $chosen=1;
	}
	if (!$chosen) 
	  // remove the association contact / event 
	  run_query_event_contact_delete($param_event,$obm_q_event_contacts->f("contact_id"));	  
      }

      // determinate which groups have been removed and delete them and their members
      $obm_q_event_groups=run_query_get_eventgroups($param_event);
      while ($obm_q_event_groups->next_record()) {
	$chosen=0;
	for ( $i = 0; ($i < $nb_group_chosen) && !$chosen; $i++ ) { 
	    if ($obm_q_event_groups->f("eventuser_group_id")==$sel_group_id[$i]) 
	      $chosen=1;
	}
	if (!$chosen) { 
	  // remove the assocation group / event and the associations group members / event 
	  run_query_event_group_delete($param_event,$obm_q_event_groups->f("eventuser_group_id"));
	}
      } 
     
      // add members of each group
      for ( $i = 0; $i < $nb_group_chosen ; $i++ ) {
	$obm_q_members=run_query_get_group_members($sel_group_id[$i]);
	while ($obm_q_members->next_record()) {
	  // insert the association group member/event if it isn't already exist
	  run_query_event_contact_insert($obm_q_members->f("ContactList_contactid"),$param_event,$begindate,$enddate,"A");
	}
      }

      // delete conflicting contacts and groups
      for ($i=0;$i<count($conflict_contacts_array);$i++) {
	run_query_event_contact_delete($param_event,$conflict_contacts_array[$i]);
      }
      for ($i=0;$i<count($conflict_groups_array);$i++) {
	run_query_event_group_delete($param_event,$conflict_groups_array[$i]);
      }

      display_ok_msg($l_event_update_ok);
     
      // Display week planning
      $p_date=date("YmdHis");
      $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
      $p_year=substr($p_date,0,4);
      $p_month=substr($p_date,4,2);
      $p_day=substr($p_date,6,2);
      $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
      // Weekstart :
      if ($dow == 0) {
	$weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
      } elseif ( $dow == 1 ) {
	$weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
      } else {
	$weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
      }
      $weekend=date("YmdHis",$weekstart + 7 * 86400);
      $weekstart=date("YmdHis",$weekstart);
      $action="view_week";
      dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
      dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
      


    } else {  
      // Not allowed 
      display_error_permission();
    }

  }

      




} else if ($action == "delete") {
  ////////////////////////////////////////////////////////////////////////////////////////////

  if ($param_event>0) {
    $obm_q_event=run_query_get_event($param_event);
    $obm_q_event->next_record();
    // only the creator or one of the attendees can delete an event
    if ((run_query_is_attendee($param_event,run_query_contactid_user($auth->auth["uid"]))) || ($obm_q_event->f("calendarevent_usercreate")==$auth->auth["uid"])) {
      run_query_event_delete($param_event,0);
      display_ok_msg($l_event_delete_ok);
    
      // Display week planning
      $p_date=date("YmdHis");
      $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
      $p_year=substr($p_date,0,4);
      $p_month=substr($p_date,4,2);
      $p_day=substr($p_date,6,2);
      $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
      // Weekstart :
      if ($dow == 0) {
	$weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
      } elseif ( $dow == 1 ) {
	$weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
      } else {
	$weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
      }
      $weekend=date("YmdHis",$weekstart + 7 * 86400);
      $weekstart=date("YmdHis",$weekstart);
      $action="view_week";
      dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
      dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
      


    } else {
      display_error_permission();
    }
  } 




} else if ($action == "delete_all") {
  ////////////////////////////////////////////////////////////////////////////////////////////

  if ($param_event>0) {
    $obm_q_event=run_query_get_event($param_event);
    $obm_q_event->next_record();
    // only the creator or one of the attendees can delete an event
    if ((run_query_is_attendee($param_event,run_query_contactid_user($auth->auth["uid"]))) || ($obm_q_event->f("calendarevent_usercreate")==$auth->auth["uid"])) {
      $event_id=($obm_q_event->f("calendarevent_origin_id")>0 ? $obm_q_event->f("calendarevent_origin_id") : $param_event);
      run_query_event_delete($event_id,1);
      display_ok_msg($l_event_delete_all_ok);

      // Display week planning
      $p_date=date("YmdHis");
      $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
      $p_year=substr($p_date,0,4);
      $p_month=substr($p_date,4,2);
      $p_day=substr($p_date,6,2);
      $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));
      // Weekstart :
      if ($dow == 0) {
	$weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
      } elseif ( $dow == 1 ) {
	$weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
      } else {
	$weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
      }
      $weekend=date("YmdHis",$weekstart + 7 * 86400);
      $weekstart=date("YmdHis",$weekstart);
      $action="view_week";
      dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
      dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  
      


    } else {
      display_error_permission();
    }
  }





} else if ($action == "view_week") {
   ////////////////////////////////////////////////////////////////////////////////////////////
  
  // date of the planning (default is current date)
  if ($param_begin_date>0) 
    $p_date=$param_begin_date;
  else 
    $p_date=date("YmdHis");
  
  // groups appearing in planning 
  if ($sel_group_id) {
    // variables from the form
    $p_group_array=$sel_group_id; 
  } elseif ($group_0) { 
    // variables from the links
    reset($HTTP_GET_VARS);
    while ( list( $key ) = each( $HTTP_GET_VARS ) ) {
      if(strcmp(substr($key, 0, 6),"group_") == 0) {
	$p_group_array[]=$HTTP_GET_VARS[$key];
      } 
    }
  }

  // contacts appearing in planning (default is current user) 
  if ($sel_contact_id) {
    $p_contact_array=$sel_contact_id;
  } elseif ($contact_0) { 
    reset($HTTP_GET_VARS);
    while ( list( $key ) = each( $HTTP_GET_VARS ) ) {
      if(strcmp(substr($key, 0, 8),"contact_") == 0) {
	$p_contact_array[]=$HTTP_GET_VARS[$key];
      } 
    }
  } elseif (!$p_group_array) { 
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
  }
    
  $p_year=substr($p_date,0,4);
  $p_month=substr($p_date,4,2);
  $p_day=substr($p_date,6,2);
  $dow=date("w",mktime(0,0,0,$p_month,$p_day,$p_year));

  // Weekstart :
  if ($dow == 0) {
    $weekstart=mktime(0,0,0,$p_month,$p_day - 6,$p_year);
  } elseif ( $dow == 1 ) {
    $weekstart=mktime(0,0,0,$p_month,$p_day,$p_year); 
  } else {
    $weekstart=mktime(0,0,0,$p_month,$p_day - ($dow-1),$p_year);
  }
  $weekend=date("YmdHis",$weekstart + 7 * 86400);
  $weekstart=date("YmdHis",$weekstart);
  $action="view_week";
  dis_week_planning($weekstart, run_query_event_list($weekstart,$weekend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
  dis_planning_contacts($action,$weekstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);  



} else if ($action == "view_day") {
  ///////////////////////////////////////////////////////////////////////////////////////
  
  // date of the planning (default is current date)
  if ($param_begin_date>0) 
    $p_date=$param_begin_date;
  else 
    $p_date=date("YmdHis");
  
  // groups appearing in planning 
  if ($sel_group_id) {
    // variables from the form
    $p_group_array=$sel_group_id; 
  } elseif ($group_0) { 
    // variables from the links
    reset($HTTP_GET_VARS);
    while ( list( $key ) = each( $HTTP_GET_VARS ) ) {
      if(strcmp(substr($key, 0, 6),"group_") == 0) {
	$p_group_array[]=$HTTP_GET_VARS[$key];
      } 
    }
  }

  // contacts appearing in planning (default is current user) 
  if ($sel_contact_id) {
    $p_contact_array=$sel_contact_id;
  } elseif ($contact_0) { 
    reset($HTTP_GET_VARS);
    while ( list( $key ) = each( $HTTP_GET_VARS ) ) {
      if(strcmp(substr($key, 0, 8),"contact_") == 0) {
	$p_contact_array[]=$HTTP_GET_VARS[$key];
      } 
    }
  } elseif (!$p_group_array) { 
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
  }

  
  $p_year=substr($p_date,0,4);
  $p_month=substr($p_date,4,2);
  $p_day=substr($p_date,6,2);
  
  $daystart=mktime(0,0,0,$p_month,$p_day,$p_year);
  $dayend=date("YmdHis",$daystart + 86400);
  $daystart=date("YmdHis",$daystart);
  
  dis_day_planning($daystart, run_query_event_list($daystart,$dayend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
  dis_planning_contacts($action,$daystart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);
 


} else if ($action == "view_month") {
  ///////////////////////////////////////////////////////////////////////////////////////
  
  // date of the planning (default is current date)
  if ($param_begin_date>0) 
    $p_date=$param_begin_date;
  else 
    $p_date=date("YmdHis");
  
   // groups appearing in planning 
  if ($sel_group_id) {
    // variables from the form
    $p_group_array=$sel_group_id; 
  } elseif ($group_0) { 
    // variables from the links
    reset($HTTP_GET_VARS);
    while ( list( $key ) = each( $HTTP_GET_VARS ) ) {
      if(strcmp(substr($key, 0, 6),"group_") == 0) {
	$p_group_array[]=$HTTP_GET_VARS[$key];
      } 
    }
  }

  // contacts appearing in planning (default is current user) 
  if ($sel_contact_id) {
    $p_contact_array=$sel_contact_id;
  } elseif ($contact_0) { 
    reset($HTTP_GET_VARS);
    while ( list( $key ) = each( $HTTP_GET_VARS ) ) {
      if(strcmp(substr($key, 0, 8),"contact_") == 0) {
	$p_contact_array[]=$HTTP_GET_VARS[$key];
      } 
    }
  } elseif (!$p_group_array) { 
    $p_contact_array=array(run_query_contactid_user($auth->auth["uid"]));
  }

  $p_year=substr($p_date,0,4);
  $p_month=substr($p_date,4,2);
  $p_day=substr($p_date,6,2);
  $monthstart=date("YmdHis",mktime(0,0,0,$p_month,1,$p_year));
  $monthend=date("YmdHis",mktime(0,0,0,$p_month+1,1,$p_year));
  
  dis_month_planning($monthstart, run_query_event_list($monthstart,$monthend,$p_contact_array,$p_group_array),run_query_contactid_user($auth->auth["uid"]),$p_contact_array,$p_group_array,$col_attend,$col_not_attend);
  dis_planning_contacts($action,$monthstart,run_query_get_mycontacts(), run_query_get_mylists(),$p_contact_array,$p_group_array);
}
 
  
///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();

</SCRIPT>


