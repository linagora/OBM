<?
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

  $obm_db = new DB_OBM;
  $db_type = $obm_db->type;    
  $calendarsegment_date = sql_date_format($db_type, "calendarsegment_date","calendarsegment_date"); 
  
  $query = "
  CREATE TABLE CalendarEventData (
    calendareventdata_id int(8)    NOT NULL auto_increment,
    calendareventdata_timeupdate   timestamp(14),
    calendareventdata_timecreate   timestamp(14),
    calendareventdata_userupdate   int(8) default NULL,
    calendareventdata_usercreate   int(8) default NULL,
    calendareventdata_owner	     int(8) default NULL, 
    calendareventdata_title        varchar(255) default NULL,
    calendareventdata_description  text,
    calendareventdata_category_id  int(8) default NULL,
    calendareventdata_priority     int(2) default NULL,
    calendareventdata_privacy      int(2) NOT NULL default '0',
    calendareventdata_date       timestamp(14) NOT NULL,
    calendareventdata_duration     int(8) NOT NULL default '',
    calendareventdata_allday	     int(1) NOT NULL default '0',
    calendareventdata_repeatkind   varchar(20) default NULL,
    calendareventdata_repeatfrequence  int(3) default NULL,
    calendareventdata_repeatdays   varchar(7) default NULL,
    calendareventdata_endrepeat    timestamp(14) NOT NULL,
    PRIMARY KEY (calendareventdata_id)
  )
  ";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $query = "  
  CREATE TABLE CalendarUser (
    calendaruser_timeupdate   timestamp(14),
    calendaruser_timecreate   timestamp(14),
    calendaruser_userupdate   int(8) default NULL,
    calendaruser_usercreate   int(8) default NULL,
    calendaruser_user_id  int(8) NOT NULL default '0',
    calendaruser_event_id     int(8) NOT NULL default '0',
    calendaruser_state       char(1) NOT NULL default '',
    calendaruser_required    int(1) NOT NULL default '0',
    PRIMARY KEY (calendaruser_user_id,calendaruser_event_id)
  )";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  $query = "  
  CREATE TABLE CalendarException (
    calendarexception_timeupdate   timestamp(14),
    calendarexception_timecreate   timestamp(14),
    calendarexception_userupdate   int(8) default NULL,
    calendarexception_usercreate   int(8) default NULL,
    calendarexception_event_id int(8)    NOT NULL auto_increment,
    calendarexception_date         timestamp(14) NOT NULL,
    PRIMARY KEY (calendarexception_event_id,calendarexception_date)
  )";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);




  
  $query = "SELECT * FROM CalendarEvent";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);
  while($obm_db->next_record()) {
    $obm_sub_db = new DB_OBM;
    $obm_ins_db = new DB_OBM;
    $id = $obm_db->f("calendarevent_id");
    $timeupdate = $obm_db->f("calendarevent_timeupdate");
    $timecreate = $obm_db->f("calendarevent_timecreate");   
    $userupdate = $obm_db->f("calendarevent_userupdate");  
    $usercreate = $obm_db->f("calendarevent_usercreate"); 
    $title = $obm_db->f("calendarevent_title");     
    $description = $obm_db->f("calendarevent_description");  
    $category_id = $obm_db->f("calendarevent_category_id"); 
    $priority = $obm_db->f("calendarevent_priority");   
    $privacy = $obm_db->f("calendarevent_privacy");   
    $length = $obm_db->f("calendarevent_length");  
    $repeatkind = $obm_db->f("calendarevent_repeatkind");   
    $repeatdays = $obm_db->f("calendarevent_repeatdays");  
    $allday = 0;
    $repeatfrequence = 1;
    $endrepeat = $obm_db->f("calendarevent_endrepeat");  
    if(($endrepeat >= "20100101000000" || $endrepeat == "00000000000000") && $repeatkind != 'none') {
      $query = "SELECT calendarsegment_customerid, calendarsegment_state, calendarsegment_date
		FROM CalendarSegment WHERE calendarsegment_eventid = '$id' AND calendarsegment_flag = 'begin'
		GROUP BY calendarsegment_customerid, calendarsegment_state, calendarsegment_date
		HAVING calendarsegment_date = MAX(calendarsegment_date)
		ORDER BY calendarsegment_date";
		
      display_debug_msg($query, $cdg_sql);
      $obm_sub_db->query($query);
      $obm_sub_db->next_record();
      $endrepeat =  $obm_sub_db->f("calendarsegment_date");      
    }
    
    $query = "SELECT calendarsegment_customerid, calendarsegment_state, calendarsegment_date
              FROM CalendarSegment WHERE calendarsegment_eventid = '$id' AND calendarsegment_flag = 'begin'
	      GROUP BY calendarsegment_customerid, calendarsegment_state, calendarsegment_date 
	      HAVING calendarsegment_date = MIN(calendarsegment_date)
	      ORDER BY calendarsegment_date";
	      
    display_debug_msg($query, $cdg_sql);
    $obm_sub_db->query($query);
    $obm_sub_db->next_record();
    $date_begin =  $obm_sub_db->f("calendarsegment_date");
    $query = "INSERT INTO CalendarEventData VALUES('".addslashes($id)."', '".addslashes($timeupdate)."',
             '".addslashes($timecreate)."','".addslashes($userupdate)."', '".addslashes($usercreate)."',
	     '".addslashes($usercreate)."','".addslashes($title)."', '".addslashes($description)."', '".addslashes($category_id)."',
             '".addslashes($priority)."','".addslashes($privacy)."','".addslashes($date_begin)."',
	     '".addslashes($length)."','".addslashes($allday)."','".addslashes($repeatkind)."',
	     '".addslashes($repeatfrequence)."','".addslashes($repeatdays)."','".addslashes($endrepeat)."')";
    display_debug_msg($query, $cdg_sql);
    $obm_ins_db->query($query);
        
    $query = "SELECT calendarsegment_customerid, calendarsegment_state 
              FROM CalendarSegment WHERE calendarsegment_eventid = '$id' AND calendarsegment_flag = 'begin'
             AND calendarsegment_type = 'user'	     
              GROUP BY calendarsegment_customerid, calendarsegment_state";
	      
    display_debug_msg($query, $cdg_sql);
    $obm_sub_db->query($query);
    $old_u ="";
    if($obm_sub_db->nf() != 0) {
    while($obm_sub_db->next_record()) {
      $user_id =  $obm_sub_db->f("calendarsegment_customerid");
      $state =  $obm_sub_db->f("calendarsegment_state");
      if($user_id == $old_u || $old_u =="") {
	if($state == "A") {
	  $rec_state = $state;
	}
	elseif($rec_state != "A" && $state == "W" ) {
	  $rec_state = $state;
	}elseif($rec_state != "A" && $rec_state != "W" && $state == "R" ) {
	  $rec_state = $state;
	}
	  
      }
      else {
        $query = "INSERT INTO CalendarUser VALUES('".addslashes($timeupdate)."', '".addslashes($timecreate)."',
                                                '".addslashes($userupdate)."', '".addslashes($usercreate)."',
						'".addslashes($old_u)."','".addslashes($id)."','".addslashes($rec_state)."',0)";	
        display_debug_msg($query, $cdg_sql);
        $obm_ins_db->query($query);
	$rec_state = "$state";
      }
      $old_u = $user_id ;
    }
    $query = "INSERT INTO CalendarUser VALUES('".addslashes($timeupdate)."', '".addslashes($timecreate)."',
                                                '".addslashes($userupdate)."', '".addslashes($usercreate)."',
						'".addslashes($old_u)."','".addslashes($id)."','".addslashes($rec_state)."',0)";	
    display_debug_msg($query, $cdg_sql);
    $obm_ins_db->query($query);
    }

   
  }

  $query = "DROP TABLE CalendarEvent";
  $obm_db->query($query);
  display_debug_msg($query, $cdg_sql);
  $query = "DROP TABLE CalendarSegment";
  $obm_db->query($query);
  display_debug_msg($query, $cdg_sql);
    
  $query = "
  CREATE TABLE CalendarEvent (
  calendarevent_id int(8)    NOT NULL auto_increment,
  calendarevent_timeupdate   timestamp(14),
  calendarevent_timecreate   timestamp(14),
  calendarevent_userupdate   int(8) default NULL,
  calendarevent_usercreate   int(8) default NULL,
  calendarevent_owner	     int(8) default NULL, 
  calendarevent_title        varchar(255) default NULL,
  calendarevent_description  text,
  calendarevent_category_id  int(8) default NULL,
  calendarevent_priority     int(2) default NULL,
  calendarevent_privacy      int(2) NOT NULL default '0',
  calendarevent_date       timestamp(14) NOT NULL,
  calendarevent_duration     int(8) NOT NULL default '',
  calendarevent_allday	     int(1) NOT NULL default '0',
  calendarevent_repeatkind   varchar(20) default NULL,
  calendarevent_repeatfrequence  int(3) default NULL,
  calendarevent_repeatdays   varchar(7) default NULL,
  calendarevent_endrepeat    timestamp(14) NOT NULL,
  PRIMARY KEY (calendarevent_id)
  )
  ";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $query = "INSERT INTO CalendarEvent SELECT * FROM CalendarEventData";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

