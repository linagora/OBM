<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_display.inc                                           //
//     - Desc : Agenda Display File                                          //
// 2001-07-03 Francois Bloque                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display Event Form                                                        //
///////////////////////////////////////////////////////////////////////////////
// Arguments : 
// -----------
//   $action            : action to make ("new" or "detailupdate")
// ( when update : )
//   $obm_q_event       : the event record 
//   $obm_q_affected_contacts  : list of contacts assigned to the event
//   $obm_q_affected_groups : list of groups assigned to the event 
//   $param_event       : id of the event to delete or update
//   $contact_list      : list of user records 
//   $group_list        : list of user's groups records 
//   $category_list     : list of event categories records
// ( when new :   )
//   $p_title           : title to assign to the event
//   $p_contacts_array     : array containing contacts to affect to the event
//   $p_groups_array    : array containing contacts groups to affect to the event 
//   $p_category_id     : id of the category to assign to the event
//   $p_priority        : priority to assign to the event (0, 1 or 2) 
//   $p_privacy      : privacy flag to assign to the event (private or not - 1 or 0 )
//   $p_datebegin       : date and time of begin, like "yyyymmddhhmmss" (year/month/day/hour/minute/second)
//   $p_dateend         : date and time of end, like "yyyymmddhhmmss" (year/month/day/hour/minute/second)
//   $p_description     : description to assign to the event
//   $p_occupied_day    : flag (0 or 1) indicating if entire day is reserved for the task (or not) 
//   $p_repeat_kind     : kind of repetition : "daily", "weekly", "monthlybydate", monthlybyday", "yearly"
//   $p_repeat_interval : interval between two repetitions (if repeat kind is "daily" then an interval of 2 means the event is repeated all twice days)
//   $p_repeat_days     : "0" and "1" characters string, each one representing a day : "0100100" means Tuesday and Friday ... 
//   $p_repeat_end      : date and time of end repeat, like "yyyymmddhhmmss" (year/month/day/hour/minute/second)
//
///////////////////////////////////////////////////////////////////////////////
function dis_event_form($action,$obm_q_event,$obm_q_affected_contacts,$obm_q_affected_groups,$param_event,$contact_list,$group_list,$category_list,$p_title,$p_contacts_array,$p_groups_array,$p_category_id,$p_priority,$p_privacy,$p_datebegin,$p_dateend,$p_description,$p_occupied_day,$p_repeat_kind,$p_repeat_interval,$p_repeat_days,$p_repeat_end) {

  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table;
  
  // - Labels 
  global $l_users, $l_groups, $l_title, $l_priority, $l_private, $l_category, $l_description,$l_high,$l_low,$l_medium,$l_datebegin,$l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_delete,$l_delete_all,$l_update,$l_repeat,$l_periodicity,$l_repeatkind,$l_repeatdays,$l_use_endrepeat,$l_interval,$l_occupied_day,$l_none,$l_daily,$l_weekly,$l_monthlybyday,$l_monthlybydate,$l_yearly;
  // - Javascript Messages 
  global $mj_fill_title_event,$mj_fill_time_begin,$mj_fill_time_end,$mj_select_contacts,$mj_delete_confirm; 
  
  // - Variables 
  global $sess;
  
  $title=($p_title ? $p_title : $obm_q_event->f("calendarevent_title"));
  $category_id=($p_category_id ? $p_category_id : $obm_q_event->f("calendarevent_category_id"));
  $priority=($p_priority ? $p_priority : $obm_q_event->f("calendarevent_priority"));
  $description=($p_description ? $p_description : $obm_q_event->f("calendarevent_description"));
  $datebegin=($p_datebegin ? substr($p_datebegin,0,8) : substr($obm_q_event->f("calendarevent_datebegin"),0,8));
  $timebegin=($p_datebegin ? substr($p_datebegin,8,6) : substr($obm_q_event->f("calendarevent_datebegin"),8,6));
  $dateend=($p_dateend ? substr($p_dateend,0,8) : substr($obm_q_event->f("calendarevent_dateend"),0,8));
  $timeend=($p_dateend ? substr($p_dateend,8,6) : substr($obm_q_event->f("calendarevent_dateend"),8,6));
  $privacy=($p_privacy ? $p_privacy : $obm_q_event->f("calendarevent_privacy")); 
  $occupied_day=($p_occupied_day ? $p_occupied_day : $obm_q_event->f("calendarevent_occupied_day"));

  $repeat_kind=($p_repeat_kind ? $p_repeat_kind : $obm_q_event->f("calendarevent_repeatkind"));
  $repeat_interval=($p_repeat_interval ? $p_repeat_interval : $obm_q_event->f("calendarevent_repeat_interval"));
  $repeat_days=($p_repeat_days ? $p_repeat_days : $obm_q_event->f("calendarevent_repeatdays")); 
  $repeat_end=($p_repeat_end ? $p_repeat_end : $obm_q_event->f("calendarevent_endrepeat"));
  
  // contacts assigned to the event  
  if ($p_contacts_array) {
    $contacts_array=$p_contacts_array;
  } else if ($obm_q_affected_contacts->nf() >0) {
    while ($obm_q_affected_contacts->next_record()) {
      $contacts_array[] = $obm_q_affected_contacts->f("contact_id");
    } 
  } 
  
  // groups assigned to the event 
  if ($p_groups) {
    $groups=$p_groups;
  } else if ($obm_q_affected_groups->nf())  {
    while ($obm_q_affected_groups->next_record()) {
      $groups[] = $obm_q_affected_groups->f("eventuser_group_id");
    }
  } 

  // current, last and next years
  $current_year=date("Y");
  $last_year=date("Y",mktime(0,0,0,1,1,$current_year-1));
  $next_year=date("Y",mktime(0,0,0,1,1,$current_year+1));
  // day, month and year of the date of begin
  $yearbegin=substr($datebegin,0,4);
  $monthbegin=substr($datebegin,4,2);
  $daybegin=substr($datebegin,6,2);
  // day, month and year of the date of end 
  $yearend=substr($dateend,0,4);
  $monthend=substr($dateend,4,2);
  $dayend=substr($dateend,6,2);
  // day, month and year of the date of end repeat
  $year_repeatend=substr($repeat_end,0,4);
  $month_repeatend=substr($repeat_end,4,2);
  $day_repeatend=substr($repeat_end,6,2);
  // hour and minutes of the time of begin 
  $hour_begin=substr($timebegin,0,2);
  $minute_begin=substr($timebegin,2,2);
  // hour and minutes of the time of end
  $hour_end=substr($timeend,0,2);
  $minute_end=substr($timeend,2,2);
  
  echo "<FORM name=\"form_event\" method=POST onSubmit=\"if (check_insert_event(this)) return true; else return false;\" action=\"".$sess->url("agenda_index.php")."\" onSubmit=\"\">"; 
  echo "<TABLE border=0 width=80%>       
        <TR><TD align=center>";

  echo "<TABLE border=0>
        <TR><TD colspan=2>";   
  // Title 
  echo "<FONT COLOR=\"#$col_label\">".$l_title."</FONT><BR>
       <INPUT type=text name=\"tf_event_title\" value=\"".$title."\" size=50>"; 

  echo "</TD></TR>
        <TR><TD>";
  // Available Contacts 
  echo "<FONT COLOR=\"#$col_label\">".$l_users."</FONT><BR>";
  echo "<SELECT name=\"sel_user_id[]\" size=6 multiple>";
  while ($contact_list->next_record()) {
    echo "\n<OPTION value=".$contact_list->f("contact_id");
       
    if (count($contacts_array)>0) {
       $tag_user="";
       while ( (list($key, $user_id) = each($contacts_array) ) && (strcmp($tag_user,"")==0)) {
	 if ($contact_list->f("contact_id") == $user_id ) {
	   $tag_user=" selected";
	 }
       }
       reset($contacts_array);
    }
    
    echo " ".$tag_user.">".$contact_list->f("contact_lastname")." ".$contact_list->f("contact_firstname")."</OPTION>";
  } 
  echo "</SELECT>";

  echo "</TD><TD>"; 
  // Available Groups 
  echo "<FONT COLOR=\"#$col_label\">".$l_groups."</FONT><BR>";
  echo "<SELECT name=\"sel_group_id[]\" size=6 multiple>";
  while ($group_list->next_record()) {
    echo "\n<OPTION value=".$group_list->f("list_id");

    if (count($groups)>0) {
      $tag_group="";
      while ((list($key, $group_id) = each ($groups)) && (strcmp($tag_group,"")==0)) {
	if ($group_list->f("list_id") == $group_id ) {
	  $tag_group=" selected";
	}
      }
      reset($groups);
    }

    echo " ".$tag_group.">".$group_list->f("list_name")."</OPTION>";
  }
  echo "</SELECT>";

  echo " </TD></TR>
        <TR><TD colspan=2 align=center>";  
 
  echo "<TABLE border=0 width=100%>
        <TR><TD>";
  // Available Event Categories 
  echo  "<FONT COLOR=\"#$col_label\">".$l_category."</FONT><br>
        <SELECT name=\"sel_category_id\">"; 
  while ($category_list->next_record()) {
    echo "<OPTION value=".$category_list->f("eventcategory_id");
    if ($category_list->f("eventcategory_id")==$category_id) {
      echo " selected";
    }
    echo ">".$category_list->f("eventcategory_label")."</OPTION>";
  }
  echo "</SELECT>";
 
  echo "</TD><TD>";
  
  // Priorities 
  if ($priority != '') {
    $tag_low="";
    $tag_medium="";
    $tag_high="";
    switch($priority) {
     case 0 : $tag_low="selected"; break;
     case 1 : $tag_medium="selected"; break;
     case 2 : $tag_high="selected"; break; 
     default :
       $tag_low="";
       $tag_medium="";
       $tag_high=""; 
       break;
    }
  }
  echo "<FONT COLOR=\"#$col_label\">".$l_priority."</FONT><BR>
        <SELECT name=\"sel_priority\">
        \n<OPTION value=1 ".$tag_medium.">".$l_medium."</OPTION>
        \n<OPTION value=0 ".$tag_low.">".$l_low."</OPTION>
        \n<OPTION value=2 ".$tag_high.">".$l_high."</OPTION>
        \n</SELECT>";

   echo "</TD><TD>";		    
  
  // Privacy 
  echo "<TABLE border=2><TR><TD>".
  "<FONT COLOR=\"#$col_label\">".$l_private."</FONT><INPUT type=checkbox name=\"cb_private_event\" value=1";
  if ($privacy==1) {
     echo " checked";
  }
  echo "></TD></TR></TABLE>";

  echo "</TD></TR>
      </TABLE>";

  echo "</TD></TR>
     <TR><TD colspan=2>";
  
  //-------------
  // Dates & Times
  echo "<TABLE border=0>";
 
  // Date begin
  echo "<TR><TD>"; 
  echo "<FONT COLOR=\"#$col_label\">".$l_datebegin."</FONT>";
  echo "</TD><TD>";
  echo "<SELECT name=\"sel_day_begin\" OnChange=\"SynchronizeDay(this.form)\">";
  for ($num_day=1 ; $num_day<32 ; $num_day++) {
    echo "\n<OPTION value=";
    if ($num_day<10) {
      $num_day_value="0".$num_day;
    } else {
      $num_day_value=$num_day;
    }
    echo $num_day_value;
    if ($daybegin==$num_day_value) {
      echo " selected";
    }
    echo ">".$num_day."</OPTION>";
  }
  echo "</SELECT>";
  echo "</TD><TD>";
  echo "<SELECT name=\"sel_month_begin\" OnChange=\"SynchronizeMonth(this.form)\">";
  for ($num_month=1 ; $num_month<13 ; $num_month++) {
    echo "\n<OPTION value=";
    if ($num_month<10) {
      $num_month_value="0".$num_month;
    } else {
      $num_month_value=$num_month;
    }
    echo $num_month_value;
    if ($monthbegin==$num_month_value) {
      echo " selected";
    }
    echo ">".month_short_name($num_month)."</OPTION>";
  }
  echo "\n</SELECT>";
  echo "</TD><TD>";
  switch ($yearbegin) {
     case $last_year : $tag_last = "selected"; break;
     case $current_year : $tag_current = "selected"; break;
     case $next_year : $tag_next = "selected"; break;
  }
  echo "<SELECT name=\"sel_year_begin\" OnChange=\"SynchronizeYear(this.form)\">
  \n<OPTION value=".$current_year." ".$tag_current.">".$current_year."</OPTION>
  \n<OPTION value=".$last_year." ".$tag_last.">".$last_year."</OPTION>
  \n<OPTION value=".$next_year." ".$tag_next.">".$next_year."</OPTION>
  </SELECT>"; 
  echo "</TD></TR>";
     
  // Time begin
  echo "<TR><TD>";
  echo "<FONT COLOR=\"#$col_label\">".$l_timebegin."</FONT>";
  echo "</TD><TD colspan=3>";
  echo "<INPUT type=text name=\"tf_hour_begin\" size=3 maxlength=2 value=\"".$hour_begin."\">&nbsp; <b>:</b> &nbsp;";
  echo "<INPUT type=text name=\"tf_minute_begin\" size=3 maxlength=2 value=\"".$minute_begin."\">";
  echo "</TD></TR>";
       
  // Date end
  echo "<TR><TD>";
  echo "<FONT COLOR=\"#$col_label\">".$l_dateend."</FONT>";
  echo "</TD><TD>";
  echo "<SELECT name=\"sel_day_end\">";
  for ($num_day=1 ; $num_day<32 ; $num_day++) {
    echo "\n<OPTION value=";
    if ($num_day<10) {
      $num_day_value="0".$num_day;
    } else {
      $num_day_value=$num_day;
    }
    echo $num_day_value;
    if ($dayend==$num_day_value) {
      echo " selected";
    }
    echo ">".$num_day."</OPTION>";
  }
  echo "</SELECT>";
  echo "</TD><TD>";
  echo "<SELECT name=\"sel_month_end\">";
  for ($num_month=1 ; $num_month<13 ; $num_month++) {
    echo "\n<OPTION value=";
    if ($num_month<10) {
      $num_month_value="0".$num_month;
    } else {
      $num_month_value=$num_month;
    }
    echo $num_month_value;
    if ($monthend==$num_month_value) {
      echo " selected";
    }
    echo ">".month_short_name($num_month)."</OPTION>";
  }
  echo "</SELECT>";
  echo "</TD><TD>";
  $tag_last="";
  $tag_next="";
  $tag_current="";
  switch ($yearend) {
    case $last_year : $tag_last = "selected"; break;
    case $current_year : $tag_current = "selected"; break;
    case $next_year : $tag_next = "selected"; break;
  }
  echo "<SELECT name=\"sel_year_end\">
  \n<OPTION value=".$current_year." ".$tag_current.">".$current_year."</OPTION>
  \n<OPTION value=".$last_year." ".$tag_last.">".$last_year."</OPTION>
  \n<OPTION value=".$next_year." ".$tag_next.">".$next_year."</OPTION>
  </SELECT>"; 
  echo "</TD></TR>";

  // Time end
  echo "<TR><TD>";
  echo "<FONT COLOR=\"#$col_label\">".$l_timeend."</FONT>";
  echo "</TD><TD colspan=3>";
  echo "<INPUT type=text name=\"tf_hour_end\" size=3 maxlength=2 value=\"".$hour_end."\">&nbsp; <b>:</b> &nbsp;";
  echo "<INPUT type=text name=\"tf_minute_end\" size=3 maxlength=2 value=\"".$minute_end."\">";
  echo "</TD></TR>";
  echo "<TR><TD>";
  echo "<FONT COLOR=\"#$col_label\">".$l_occupied_day." ?</FONT>";
  echo "</TD><TD colspan=3>";
  echo "<INPUT type=checkbox name=\"cb_occupied_day\" value=1";
  if ($occupied_day==1) {  
    echo " checked";
  } 
  echo "></TD></TR>
         </TABLE>";

  echo "</TD></TR>
        </TABLE>";


  echo "</TD><TD align=right valign=center>";

  //---------------
  // Repetiton Rules 
  echo "<TABLE border=1 bgcolor=\"#$col_table\">
        <TR><TD align=center>";

  echo "<B><FONT color=\"#$col_textem\">" . $l_repeat .
        "</FONT></B>";

  echo "</TD></TR><TR><TD>";
  // Repeat kind  
  echo "<FONT COLOR=\"#$col_label\"> ".$l_repeatkind." : </FONT>";
  if ($action == "new") {
    $tag_daily="";
    $tag_weekly="";
    $tag_monthlybyday="";
    $tag_monthlybydate="";
    $tag_yearly="";
    switch ($repeat_kind) {
      case "daily" : $tag_daily=" selected"; break;
      case "weekly" : $tag_weekly=" selected"; break;
      case "monthlybyday" : $tag_monthlybyday=" selected"; break;
      case "monthlybydate": $tag_monthlybydate=" selected"; break;
      case "yearly": $tag_yearly=" selected"; break;
      default : 
        $tag_daily="";$tag_weekly="";$tag_monthlybyday="";$tag_monthlybydate="";$tag_yearly=""; break;
    } 
    echo "<SELECT name=\"sel_repeat_kind\">
        \n<OPTION value=\"0\">aucune</OPTION>
        \n<OPTION value=\"daily\"".$tag_daily.">".$l_daily."</OPTION>
        \n<OPTION value=\"weekly\"".$tag_weekly.">".$l_weekly."</OPTION>
        \n<OPTION value=\"monthlybydate\"".$tag_monthlybydate.">".$l_monthlybydate."</OPTION> 
        \n<OPTION value=\"monthlybyday\"".$tag_monthlybyday.">".$l_monthlybyday."</OPTION> 
        \n<OPTION value=\"yearly\"".$tag_yearly.">".$l_yearly."</OPTION> 
        \n</SELECT>";
  } else {
    switch ($repeat_kind) {
      case "0" : $label=$l_none; break;
      case "daily" : $label=$l_daily; break;
      case "weekly" : $label=$l_weekly; break;
      case "monthlybyday" : $label=$l_monthlybydate; break;
      case "monthlybydate": $label=$l_monthlybyday; break;
      case "yearly": $label=$l_yearly ; break;
    } 
    echo $label;
  }
  
  echo "</TD></TR>
        <TR><TD>";
  // Repeat Interval 
  echo  "<FONT COLOR=\"#$col_label\"> ".$l_interval." </FONT>";
  if ($action=="new") {
    // when create
    echo "<INPUT type=text name=\"tf_repeat_interval\" size=3 maxlength=2 value=\"".$repeat_interval."\">";
  } else {
    // when update
    echo " : ".$repeat_interval;
  }
  echo "</TD></TR>
        <TR><TD>";

  echo "<TABLE border=0>
        <TR><TD>";
  // Repeat days (for a weekly repetition)
  echo "<FONT COLOR=\"#$col_label\"> ".$l_repeatdays.": </FONT>";
  echo "</TD></TR>
        <TR><TD>";
  if ($action=="new") {
    // when create
    for ($i=0;$i<5;$i++) {
      echo weekday_short_name($i)." \n<INPUT type=checkbox name=\"cb_repeatday_".$i."\" value=1";
      if (strcmp(substr($repeat_days,$i,1),"1")==0) {
	echo " checked";
      }
      echo ">";
    }
    for ($i;$i<7;$i++) {
      echo weekday_short_name($i)." \n<INPUT type=checkbox name=\"cb_repeatday_".$i."\" value=1";
      if (strcmp(substr($repeat_days,$i,1),"1")==0) {
	echo " checked";
      }
      echo ">";
    }
  } else {
    // when update ...
    for ($i=0;$i<7;$i++) {
      if ($repeat_days[$i]==1) echo weekday_short_name($i)." ";
    }
  }
    
  echo "</TD></TR></TABLE>";

  echo "</TD></TR>
       <TR><TD>";
  // End repeat Date
  echo "<TABLE border=0>";
  echo "<TR><TD colspan=3>";
  echo "<FONT COLOR=\"#$col_label\">".$l_use_endrepeat."</FONT>"; 
  echo "</TD></TR>";    
  if ($action=="new") {
    // when create ...
    echo "<TR><TD>";
    echo "<SELECT name=\"sel_day_repeatend\">";
    echo "\n<OPTION value=\"0\">--</OPTION>";
    for ($num_day=1 ; $num_day<32 ; $num_day++) {
      echo "\n<OPTION value=";
      if ($num_day<10) {
	$num_day_value="0".$num_day;
      } else {
	$num_day_value=$num_day;
      }
      echo $num_day_value;
      if ($day_repeatend==$num_day_value) {
	echo " selected";
      }
      echo ">".$num_day."</OPTION>";
    }
    echo "</SELECT>";
    echo "</TD><TD>";
    echo "<SELECT name=\"sel_month_repeatend\">";
    echo "\n<OPTION value=\"0\">--</OPTION>";
    for ($num_month=1 ; $num_month<13 ; $num_month++) {
      echo "\n<OPTION value=";
      if ($num_month<10) {
	$num_month_value="0".$num_month;
      } else {
	$num_month_value=$num_month;
      }
      echo $num_month_value;
      if ($month_repeatend==$num_month_value) {
	echo " selected";
      }
      echo ">".month_short_name($num_month)."</OPTION>";
    }
    echo "</SELECT>";
    echo "</TD><TD>";
    $tag_last="";
    $tag_next="";
    $tag_current="";
    switch ($year_repeatend) {
    case $last_year : $tag_last = "selected"; break;
    case $current_year : $tag_current = "selected"; break;
    case $next_year : $tag_next = "selected"; break;
    }
    echo "<SELECT name=\"sel_year_repeatend\">
         <OPTION value=\"0\">----</OPTION>
         <OPTION value=\"$current_year $tag_current\">$current_year</OPTION>
         <OPTION value=\"$last_year $tag_last\">$last_year</OPTION>
         <OPTION value=\"$next_year $tag_next\">$next_year</OPTION>
         </SELECT>"; 
  } else {
    // when update ...
    echo "<TR><TD colspan=3>";
    echo $year_repeatend."-".$month_repeatend."-".$day_repeatend." &nbsp;";
  }
  
  echo "
                </td>
              </tr>
              </table>
            </td>
          </tr>
          </table>
        </td>
      </tr>
      </table>
      <br>

<!--  Event Description -->

      <table border=0>
      <tr>
        <td>
          <font COLOR=\"#$col_label\">$l_description</font>
          <br>
          <textarea NAME=\"ta_event_description\" rows=6 cols=72>$description</textarea>
        </td>
        <td>&nbsp;</td>
        <td valign=center align=right>";
  
  // Actions : 
  if ($action == "detailupdate") { 
    echo "<table border=0><TR><TD align=center>
	  <INPUT type=submit name=\"sub_update_event\" value=\"$l_update\">
	  <INPUT type=hidden name=\"action\" value=\"update\">
	  <INPUT type=hidden name=\"param_event\" value=\"$param_event\">
          </FORM>
        </td>
      </tr><tr>
        <td align=center>
          <form name=\"form_delete_event\" onSubmit=\"if (deletion_valid()) return true; else return false;\" method=POST action=\"".$sess->url("agenda_index.php")."\">
          <INPUT type=submit name=\"sub_delete_event\" value=\"$l_delete\">
          <INPUT type=hidden name=\"param_event\" value=\"$param_event\">
          <INPUT type=hidden name=\"action\" value=\"delete\">
          </form>
        </td>
      </tr><tr>
        <td align=center>
          <FORM name=\"form_delete_event_all\"onSubmit=\"if (deletion_all_valid()) return true; else return false;\" method=POST action=\"".$sess->url("agenda_index.php")."\">
          <INPUT type=submit name=\"sub_delete_event_all\" value=\"".$l_delete_all."\">
          <INPUT type=hidden name=\"param_event\" value=\"$param_event\">
          <INPUT type=hidden name=\"action\" value=\"delete_all\">
        </td>
      </tr>
      </table>";
  } else { 
    echo "<INPUT type=submit name=\"sub_new_event\" value=\"$l_insert\">
          <INPUT type=hidden name=\"param_event\" value=\"$param_event\">
          <INPUT type=hidden name=\"action\" value=\"insert\">";
  }
   
  echo "</td>
  </tr>
  </table>
  </form>";

}






//----------------------------------------------------------------------------------------------------------------------------------
//////////////////////////////////////////////////////////////////////////////////////////////
// Display information abou the specified Event
//////////////////////////////////////////////////////////////////////////////////////////////
// Params : 
// --------
//   $obm_q_event : Event record
//   $obm_q_categories : Event categories records 
//   $obm_q_eventcontacts : contacts the event is assigned to
//   $obm_q_eventgroups   : groups the event is assigned to
//   $member : flag (0,1) indicating if the user is one of the event's contacts    
//////////////////////////////////////////////////////////////////////////////////////////////
function dis_event_consult($obm_q_event,$obm_q_eventcategories,$obm_q_eventcontacts,$obm_q_eventgroups,$member) {

  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table,$col_error;
  
  // - Labels 
  global $l_users, $l_groups, $l_title, $l_priority, $l_private,$l_public,$l_state, $l_category, $l_description,$l_high,$l_low,$l_medium,$l_datebegin,$l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_delete,$l_update,$l_repeat,$l_periodicity,$l_repeatkind,$l_repeatdays,$l_use_endrepeat,$l_interval,$l_accepted,$l_rejected,$l_waiting,$l_short_accepted,$l_short_rejected,$l_short_waiting,$l_none,$l_weekly,$l_daily,$l_monthlybydate,$l_monthlybyday,$l_yearly;
 
  // - Session Variables 
  global $sess,$auth;
  

  echo "\n<TABLE border=0>
      <TR><TD align=left valign=top>";

  echo "\n<TABLE border=0>
      \n<TR><TD>";
  // Title 
  echo "<FONT COLOR=\"#$col_label\">".$l_title."</FONT>";      
  echo "\n</TD><TD>";
  if ($obm_q_event->f("calendarevent_privacy")==1) {
     // private 
    if ((!$member) && ($obm_q_event->f("calendarevent_usercreate")!=$auth->auth["uid"])) {
      // not allowed 
      echo "<FONT color=\"#$col_error\"><b>".$l_private."</b></FONT>";
    } else {
      // allowed
      echo $obm_q_event->f("calendarevent_title");  
    }
  } else {
     // not private 
    echo $obm_q_event->f("calendarevent_title");  
  }

  echo "\n</TD></TR><TR><TD>";
  // Category
  echo  "<FONT COLOR=\"#$col_label\">".$l_category."</FONT>";
  echo "\n</TD><TD>";
  if ($obm_q_event->f("calendarevent_privacy")==1) {
    // private 
    if ((!$member) && ($obm_q_event->f("calendarevent_usercreate")!=$auth->auth["uid"])) {
      // not allowed 
      echo "<FONT color=\"#$col_error\"><b>".$l_private."</b></FONT>";
    } else {
      // allowed
      while ($obm_q_eventcategories->next_record()) {
	if ($obm_q_eventcategories->f("eventcategory_id")==$obm_q_event->f("calendarevent_category_id")) {
	  echo $obm_q_eventcategories->f("eventcategory_label");
	}
      }
    }
  } else {
    // not private 
    while ($obm_q_eventcategories->next_record()) {
      if ($obm_q_eventcategories->f("eventcategory_id")==$obm_q_event->f("calendarevent_category_id")) {
	echo $obm_q_eventcategories->f("eventcategory_label");
      }
    }
  }

  echo "\n</TD></TR><TR><TD>";
  // Priority
  $priority='';
  switch($obm_q_event->f("calendarevent_priority")) {
    case 0 : $priority=$l_low; break;
    case 1 : $priority=$l_medium; break; 
    case 2 : $priority=$l_high; break;  
  }
  echo "<FONT COLOR=\"#$col_label\">".$l_priority."</FONT>";
  echo "\n</TD><TD>";
  echo $priority;       
  echo "\n</TD></TR><TR><TD>";
  // Privacy   
  echo "<FONT COLOR=\"#$col_label\">".$l_state."</FONT>";
  echo "\n</TD><TD>";
  if ($obm_q_event->f("calendarevent_privacy")==1) {
    echo $l_private;
  } else {
    echo $l_public;
  } 
  echo "\n</TD></TR><TR><TD>";
  // Date begin
  echo "<FONT COLOR=\"#$col_label\">".$l_datebegin."</FONT>";
  echo "\n</TD><TD>";
  $year=substr($obm_q_event->f("calendarevent_datebegin"),0,4);
  $month=substr($obm_q_event->f("calendarevent_datebegin"),4,2);
  $day=substr($obm_q_event->f("calendarevent_datebegin"),6,2);
  $hour=substr($obm_q_event->f("calendarevent_datebegin"),8,2);
  $minute=substr($obm_q_event->f("calendarevent_datebegin"),10,2);
  echo $year."-".$month."-".$day." &nbsp;".$hour.":".$minute;
  echo "\n</TD></TR><TR><TD>"; 
  // Date End
  echo "<FONT COLOR=\"#$col_label\">".$l_dateend."</FONT>";
  echo "\n</TD><TD>";
  $year=substr($obm_q_event->f("calendarevent_dateend"),0,4);
  $month=substr($obm_q_event->f("calendarevent_dateend"),4,2);
  $day=substr($obm_q_event->f("calendarevent_dateend"),6,2);
  $hour=substr($obm_q_event->f("calendarevent_dateend"),8,2);
  $minute=substr($obm_q_event->f("calendarevent_dateend"),10,2);
  echo $year."-".$month."-".$day." &nbsp;".$hour.":".$minute;
  echo "\n</TD></TR><TR><TD colspan=2>&nbsp;";
  echo "\n</TD></TR><TR><TD colspan=2 align=left valign=bottom>"; 

  // Description 
  echo "<TABLE border=0><TR><TD>";
  echo "<FONT COLOR=\"#$col_label\">".$l_description."</FONT>";  
  echo "\n</TD></TR><TR><TD>";
  if ($obm_q_event->f("calendarevent_privacy")==1) {
    if ((!$member) && ($obm_q_event->f("calendarevent_usercreate")!=$auth->auth["uid"])) {
      echo "<FONT color=\"#$col_error\"><b>".$l_private."</b></FONT>";
    } else {
      echo nl2br($obm_q_event->f("calendarevent_description"));
    }
  } else {
    echo nl2br($obm_q_event->f("calendarevent_description")); 
  }
  echo "\n</TD></TR>
     </TABLE>";

 echo "\n</TD></TR>
    </TABLE>";  
  
  echo "\n</TD><TD align=right valign=top>";

  echo "\n<TABLE border=0>
      <TR><TD align=right>";

  echo "\n<TABLE border=1 bgcolor=\"#$col_table\"> 
      <TR><TD align=right>";

  // Contacts
  echo "\n<TABLE border=0>
      <TR><TD colspan=2>";
  echo "<FONT COLOR=\"#$col_label\">".$l_users."</FONT>";
  echo "\n</TD></TR><TR><TD>";
  while ($obm_q_eventcontacts->next_record()) {
    switch ($obm_q_eventcontacts->f("eventuser_state")) {
      case "A" : $tag_state=$l_short_accepted;break;
      case "R" : $tag_state=$l_short_rejected;break;
      case "W" : $tag_state=$l_short_waiting;break;
    }
    echo $obm_q_eventcontacts->f("contact_lastname")."&nbsp;".$obm_q_eventcontacts->f("contact_firstname");
    echo "</TD><TD>";
    echo "<I>(".$tag_state.")</I><br>";
    echo "</TD></TR><TR><TD>";
  }
  echo "&nbsp;</TD></TR>
      </TABLE>";

  echo "\n</TD><TD valign=top>";

  // Groups
  echo "\n<TABLE border=0>
      <TR><TD>";
  echo "<FONT COLOR=\"#$col_label\">".$l_groups."</FONT>";
  echo "\n</TD></TR><TR><TD>";
  while ($obm_q_eventgroups->next_record()) {
    echo $obm_q_eventgroups->f("list_name")."<br>";
  }
  echo "&nbsp;</TD></TR>
      </TABLE>";
  
  echo "</TD></TR>
      </TABLE>";
  
  echo "</TD><TD align=center>";

  echo "<TABLE border=0>
        <TR><TD><FONT size=-1><I>".$l_short_accepted." :</I></FONT></TD><TD><FONT size=-1><I>".$l_accepted."</I></FONT></TD></TR>
        <TR><TD><FONT size=-1><I>".$l_short_rejected." :</I></FONT></TD><TD><FONT size=-1><I>".$l_rejected."</I></FONT></TD></TR>
        <TR><TD><FONT size=-1><I>".$l_short_waiting." :</I></FONT></TD><TD><FONT size=-1><I>".$l_waiting."</I></FONT>
        </TD></TR>
        </TABLE>";

  echo "\n</TD></TR>
       <TR><TD colspan=2>&nbsp;";

  echo "<TR><TD colspan=2 align=center>";
    //</TABLE>";
 
  //---------------
  // Repetiton Rules 
  echo "<TABLE border=0> 
        <TR><TD>"; 
  if (strcmp($obm_q_event->f("calendarevent_repeatkind"),"daily")==0) 
    $label=$l_daily;
  elseif (strcmp($obm_q_event->f("calendarevent_repeatkind"),"weekly")==0) 
    $label=$l_weekly;
  elseif (strcmp($obm_q_event->f("calendarevent_repeatkind"),"monthlybydate")==0)
    $label=$l_monthlybydate;
  elseif (strcmp($obm_q_event->f("calendarevent_repeatkind"),"monthlybyday")==0)
    $label=$l_monthlybyday;
  elseif (strcmp($obm_q_event->f("calendarevent_repeatkind"),"monthlybyday")==0)
    $label=$l_yearly;
  else 
    $label=$l_none;
  if($label!=$l_none) {
  echo "<FONT COLOR=\"#$col_label\">".$l_repeatkind." : </FONT>".$label;
  echo "</TD></TR>
        <TR><TD>";
  // Repeat Interval 
  echo "<FONT COLOR=\"#$col_label\">".$l_interval." </FONT> : ".$obm_q_event->f("calendarevent_repeat_interval");
  echo "</TD></TR>
        <TR><TD>";
  echo "<TABLE border=0>
        <TR><TD>";
  // Repeat days (for a weekly repetition)
  if($obm_q_event->f("calendarevent_repeatdays")!=0) {
   echo "<FONT COLOR=\"#$col_label\">".$l_repeatdays.": </FONT>";
   echo "</TD></TR>
        <TR><TD>";
   $repeat_days=$obm_q_event->f("calendarevent_repeatdays");
    for ($i=0;$i<7;$i++) {  
     if ($repeat_days[$i]==1) echo weekday_short_name($i)." ";
    }
  }
  echo "</TD></TR></TABLE>";
  echo "</TD></TR>
       <TR><TD>";
  // End repeat Date
  echo "<TABLE border=0>";
  echo "<TR><TD colspan=3>";
  echo "<FONT COLOR=\"#$col_label\">".$l_use_endrepeat."</FONT>"; 
  echo "</TD></TR>";    
  echo "<TR><TD colspan=3>";
  if($obm_q_event->f("calendarevent_endrepeat")==0){
   $year=substr($obm_q_event->f("calendarevent_datebegin"),0,4)+1;
   $month=substr($obm_q_event->f("calendarevent_datebegin"),4,2);
   $day=substr($obm_q_event->f("calendarevent_datebegin"),6,2);
   $hour=substr($obm_q_event->f("calendarevent_datebegin"),8,2);
   $minute=substr($obm_q_event->f("calendarevent_datebegin"),10,2);
   echo $year."-".$month."-".$day;
  }
  else {
  echo substr($obm_q_event->f("calendarevent_endrepeat"),0,4)."-". substr($obm_q_event->f("calendarevent_endrepeat"),4,2)."-".substr($obm_q_event->f("calendarevent_endrepeat"),6,2);
  }
  echo "</TD></TR>
        </TABLE>";
  echo "</TD></TR>
        </TABLE>";
  }
  echo "\n</TD></TR>
        </TABLE>";

  echo "\n</TD></TR>";
   
  if (/*($obm_q_event->f("calendarevent_privacy")==0) ||*/ ($member==1) || ($obm_q_event->f("calendarevent_usercreate")==$auth->auth["uid"])) {
    echo "<TR><TD colspan=2 align=center><br>";
    echo "\n<FORM name=\"form_event_detailconsult\" method=POST action=\"".$sess->url("agenda_index.php")."\">";
    echo "\n<INPUT type=submit name=\"sub_event_detailupdate\" value=\"".$l_update."\">";
    echo "\n<INPUT type=hidden name=\"action\" value=\"detailupdate\">";
    echo "</FORM>"; 
    echo "</TD></TR>";
  } 
  
  echo "</TABLE>"; 

}








//----------------------------------------------------------------------------------------------------------------------------------
////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $p_start_date : start date of the week ("YYYYmmddHHmmss")
//    $obm_q_events : list of event's records 
//    $user_contact_id : id of the contact assigned to the user 
//         (usually run_query_contactid_user($auth->auth["uid"])
//         used to know if he has rights to watch events on the planning 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
//    $color_attends : color of an event the user attend
//    $color_not_attend : color of an event the user doesn't attend 
////////////////////////////////////////////////////////////////////////////////////
function dis_week_planning($p_start_date,$obm_q_events,$user_contact_id,$contacts_array,$groups_array,$color_attend,$color_not_attend) {
  
  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table;
  // - Labels 
  global $l_private;
  // - Session variables
  global $sess,$auth;
  
  $year=substr($p_start_date,0,4);
  $month=substr($p_start_date,4,2);
  $day=substr($p_start_date,6,2);
  
  $p_start_time=mktime(0,0,0,$month,$day,$year);
  $p_end_time=mktime(0,0,0,$month,$day+6,$year);
  
  // Sunday=0 but we begin with Monday=1 
  for ($i=0;$i<7;$i++) {
    $days[]=$p_start_time + 24 * 3600 * $i;
  }

  $num_rows=$obm_q_events->nf();
  $cpt_rows = 0;
  $obm_q_events->next_record();
 
  $end_occupied_time=0;          // time used to know when the colored cells end  
  $end_occupied_attend=0;        // flag used to know if the current user attend to the event which is the last to finish

  // some event could have begun the day before and not be finished 
  while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$p_start_time)) && ($obm_q_events->f("calendarevent_dateend")>$p_start_time) ) {
    $current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
    if ($current_end_time > $end_occupied_time) {
      $end_occupied_time=$current_end_time;
      if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	$end_occupied_attend=1;
      } else {
	$end_occupied_attend=0;
      }
    }
    $obm_q_events->next_record();
    $cpt_rows++;
  } 

  // events are sorted by day and by hours 
  if ($num_rows>0) {
    for ($d=0;$d<7;$d++) {
      for ($h=8 ; $h<20 ; $h++) { 
	$attend=0;
	$current_time = $p_start_time + $d * 86400 + $h * 3600; 
	$current_time_next = $p_start_time + $d * 86400 + $h * 3600 + 3600; 
	$flag_event=0;
	while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")>=date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$current_time_next)) ) {
	  $flag_event=1;
	  if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	      $attend=1;
	  }
	  $events_array[$d][]=$cpt_rows; // number of the record 
	  $current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
	  if ($current_end_time > $end_occupied_time) {
	    $end_occupied_time=$current_end_time;
	    // is the user attend to the event ? 
	    if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	      $end_occupied_attend=1;
	    } else {
	      $end_occupied_attend=0;
	    }
	  }
	  $obm_q_events->next_record();
	  $cpt_rows++;      
	}
	// cell color : 
	if ($attend==1) {
	  // the current user attend to the event 
	  $color_array[$d][$h]=$color_attend;
	} elseif ($flag_event==1) {
	  $color_array[$d][$h]=$color_not_attend;
	} else {
	  // is there at least an event for this hour ?
	  if (!$flag_event) { 
	    if ($current_time<$end_occupied_time) {
	      // a previous event continue in this time zone
	      if ($end_occupied_attend==1) {
		// the user attend to this event 
		$color_array[$d][$h]=$color_attend; 
	      } else {
		$color_array[$d][$h]=$color_not_attend; 
	      }
	    } else {
	      $color_array[$d][$h]="";
	    }
	  }    
	}
      }
    }
  }

  // contacts id used as GET vars 
  $params_contacts="";
  if ($contacts_array) {
    for ($i=0 ; $i<count($contacts_array) ; $i++) {
      $param_contacts.="&amp;contact_$i=".$contacts_array[$i];
    }
  }
  // groups id used as GET vars
  $param_groups="";
  if ($groups_array) {
    for ($i=0 ; $i<count($groups_array) ; $i++) {
      $param_groups.="&amp;group_$i=".$groups_array[$i];
    }
  }

  // links to the previous and weeks 
  echo "<CENTER><TABLE border=0><TR><TD align=left>";
  //previous week
  $url="agenda_index.php?action=view_week&amp;param_begin_date=".date("YmdHis",$p_start_time-7*86400);
  if ($param_contacts!="") $url.=$param_contacts;
  if ($param_groups!="") $url.=$param_groups;
  echo "\n<A href=\"".$sess->url($url)."\"> << </A>&nbsp;&nbsp;&nbsp;&nbsp;";

  echo "\n</TD><TD align=center><FONT size=+1 color=\"#$col_ok\"><B>".date("d",$p_start_time)." ".month_name(date("m",$p_start_time))." ".date("Y",$p_start_time)." - ".date("d",$p_end_time)." ".month_name(date("m",$p_end_time))." ".date("Y",$p_end_time)."</B></FONT></TD><TD align=right>";
  // next week 
  $url="agenda_index.php?action=view_week&amp;param_begin_date=".date("YmdHis",$p_start_time+7*86400);
  if ($param_contacts!="") $url.=$param_contacts;
  if ($param_groups!="") $url.=$param_groups;
  echo "\n<A href=\"".$sess->url($url)."\"> >> </A>";  

  echo "</TD></TR></TABLE></CENTER>";
  
  echo "<BR>";
  echo "<TABLE border=1 width=100% bgcolor=\"#$col_table\">
        \n<TR>
        <TD width=9%>&nbsp;</TD>";


  // days 
  for ($d=0;$d<7;$d++) {
    $current_month=date("m",$days[$d]);
    $current_day=date("d",$days[$d]);
    $current_year=date("Y",$days[$d]);
    $current_weekday=date("w",$days[$d]);
    echo "<TD width=13% align=center>".weekday_name($current_weekday)."<br>";
    echo "<A href=\"".$sess->url("agenda_index.php?action=view_month&amp;param_begin_date=".date("YmdHis",mktime(0,0,0,$current_month,1,$current_year)))."\">".month_short_name($current_month)."</A>&nbsp;";
    echo "<A href=\"".$sess->url("agenda_index.php?action=view_day&amp;param_begin_date=".date("YmdHis",mktime(0,0,0,$current_month,$current_day,$current_year)))."\">".$current_day."</A></TD>";
  }  

  echo "</TR>";
  $last_event_id=0;

  // hours 
  for ($h=8 ; $h<20 ; $h++) {
    $current_time = $p_start_time + 3600 * $h;
    $current_time_next = $current_time + 3600;
    echo "\n<TR><TD width=9% align=center>".date("H:i",$current_time)."</TD>";
    for ($d=0;$d<7;$d++) {
      $current_time = $p_start_time + 86400 * $d + 3600 * $h;
      $current_time_next = $current_time + 3599;
      echo "<TD width=13%";
      // occupied time is colored 
      if ($color_array[$d][$h]!="") {
	echo " bgcolor=\"#".$color_array[$d][$h]."\""; 
      }
      echo ">";
      if ($events_array) {
      // event(s) assigned to the current time 
      for ($i=0 ; $i< count($events_array[$d]) ; $i++) {
	$obm_q_events->seek($events_array[$d][$i]);
	$obm_q_events->next_record();
	if ( ($obm_q_events->f("calendarevent_datebegin") >= date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$current_time_next)) ) { 
	  // the bagin date match the current time 
	  if ($obm_q_events->f("calendarevent_id") != $last_event_id) {
	    // we don't want the same event twice for two different contacts
	    echo substr($obm_q_events->f("calendarevent_datebegin"),8,2).":".substr($obm_q_events->f("calendarevent_datebegin"),10,2)."-".substr($obm_q_events->f("calendarevent_dateend"),8,2).":".substr($obm_q_events->f("calendarevent_dateend"),10,2)."- ";
	    echo "<A href=\"".$sess->url("agenda_index.php?action=detailconsult&amp;param_event=".$obm_q_events->f("calendarevent_id"))."\">";
	    // private event's title doesn't appear
	    if ($obm_q_events->f("calendarevent_privacy")==1) {
	      if (($user_contact_id == $obm_q_events->f("eventuser_contact_id")) || ($auth->auth["uid"] == $obm_q_events->f("calendarevent_usercreate")) ) {
		echo $obm_q_events->f("calendarevent_title")."</A><br>";
	      } else {
		echo $l_private."</A><br>";
	      }
	    } else {
	      echo $obm_q_events->f("calendarevent_title")."</A><br>";
	    }
	    $last_event_id=$obm_q_events->f("calendarevent_id");
	  }
	}
      }
      }
      echo "&nbsp;</TD>";
    }
    echo "</TR>";
  }
  
  echo "</TABLE>";
  echo "<br><br>";


}




//----------------------------------------------------------------------------------------------------------------------------------
////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the specified day 
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $p_start_date : specified day ("YYYYmmddHHmmss")
//    $obm_q_events : list of event's records 
//    $user_contact_id : id of the contact assigned to the user 
//         (usually run_query_contactid_user($auth->auth["uid"])
//         used to know if he has rights to watch events on the planning 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
//    $color_attends : color of an event the user attend
//    $color_not_attend : color of an event the user doesn't attend 
////////////////////////////////////////////////////////////////////////////////////
function dis_day_planning($p_start_date,$obm_q_events,$user_contact_id,$contacts_array,$groups_array,$color_attend,$color_not_attend) {

  


  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table,$ico_new_event;
  // - Labels 
  global $l_private;
  // - Session variables
  global $sess,$auth;
  
  $year=substr($p_start_date,0,4);
  $month=substr($p_start_date,4,2);
  $day=substr($p_start_date,6,2);
    
  $p_start_time=mktime(0,0,0,$month,$day,$year);
  
  // contacts id used as GET vars 
  $params_contacts="";
  if ($contacts_array) {
    for ($i=0 ; $i<count($contacts_array) ; $i++) {
      $param_contacts.="&amp;contact_$i=".$contacts_array[$i];
    }
  }
  // groups id used as GET vars 
  $param_groups="";
  if ($groups_array) {
    for ($i=0 ; $i<count($groups_array) ; $i++) {
      $param_groups.="&amp;group_$i=".$groups_array[$i];
    }
  }


  // Links to the previous and next days 
  echo "<CENTER><TABLE border=0 width=25%><TR>";
  echo "<TD align=left> ";
  // previous
  $url="agenda_index.php?action=view_day&amp;param_begin_date=".date("YmdHis",$p_start_time-86400);
  if ($param_contacts!="") $url.=$param_contacts;
  if ($param_groups!="") $url.=$param_groups;
  echo "\n<A href=\"".$sess->url($url)."\"> << </A>&nbsp;&nbsp;&nbsp;&nbsp;";

  echo "</TD><TD align=center> <FONT size=+1 color=\"#$col_ok\"><b>".weekday_name(date("w",mktime(0,0,0,$month,$day,$year)))." ".month_short_name($month)."&nbsp;".$day."</b></FONT></TD><TD align=right>";  // title 
  // next
  $url="agenda_index.php?action=view_day&amp;param_begin_date=".date("YmdHis",$p_start_time+86400);
  if ($param_contacts!="") $url.=$param_contacts;
  if ($param_groups!="") $url.=$param_groups;
  echo "\n<A href=\"".$sess->url($url)."\"> >>  </A>&nbsp;&nbsp;&nbsp;&nbsp;";
  
  echo "</TD></TR></TABLE></CENTER>";
  echo "<BR>";
  
  $num_rows=$obm_q_events->nf(); // number of events
  $cpt_rows = 0;                 
  $obm_q_events->next_record();
  $end_occupied_time=0;          // time used to know when the colored cells end  
  $end_occupied_attend=0;        // flag used to know if the current user attend to the event which is the last to finish
  
  // some event could have begun the day before and not be finished 
  // we go to the first not occupied time area
  while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$p_start_time)) && ($obm_q_events->f("calendarevent_dateend")>$p_start_time) ) {
    $current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
    if ($current_end_time > $end_occupied_time) {
      $end_occupied_time=$current_end_time;
      if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	$end_occupied_attend=1;
      } else {
	$end_occupied_attend=0;
      }
    }
    $obm_q_events->next_record();
    $cpt_rows++;
  }
  $last_event_id=0;
  


  // events are sorted by hours 
  if ($num_rows>0) {
    //for ($d=0;$d<7;$d++) {
      for ($h=8 ; $h<20 ; $h++) { 
	$attend=0;
	$current_time = $p_start_time + $h * 3600; 
	$current_time_next = $current_time + 3600; 
	$flag_event=0;
	while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")>=date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$current_time_next)) ) {
	  $flag_event=1;
	  if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	      $attend=1;
	  }
	  $events_array[]=$cpt_rows; // number of the record 
	  $current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
	  if ($current_end_time > $end_occupied_time) {
	    $end_occupied_time=$current_end_time;
	    // is the user attend to the event ? 
	    if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	      $end_occupied_attend=1;
	    } else {
	      $end_occupied_attend=0;
	    }
	  }
	  $obm_q_events->next_record();
	  $cpt_rows++;      
	}
	// cell color : 
	if ($attend==1) {
	  // the current user attend to the event 
	  $color_array[$h]=$color_attend;
	} elseif ($flag_event==1) {
	  $color_array[$h]=$color_not_attend;
	} else {
	  // is there at least an event for this hour ?
	  if (!$flag_event) { 
	    if ($current_time<$end_occupied_time) {
	      // a previous event continue in this time zone
	      if ($end_occupied_attend==1) {
		// the user attend to this event 
		$color_array[$h]=$color_attend; 
	      } else {
		$color_array[$h]=$color_not_attend; 
	      }
	    } else {
	      $color_array[$h]="";
	    }
	  }    
	}
      }
  }
  

  echo "<TABLE border=1 width=80% bgcolor=\"#$col_table\">";
  // Planning of the day from 8:00 to 20:00

   for ($h=8 ; $h<20 ; $h++) {
    $current_time = $p_start_time + 3600 * $h;
    $current_time_next = $current_time + 3600;
    echo "\n<TR><TD width=20% align=center>".date("H:i",$current_time)."</TD>";
    echo "<TD width=80% valign=top";
    // occupied time is colored 
    if ($color_array[$h]!="") {
      echo " bgcolor=\"#".$color_array[$h]."\""; 
    }
    echo ">";
    
    echo "<TABLE border=0 width=100%>
       <TR><TD align=right>";
    echo "<A href=\"".$sess->url("agenda_index.php?action=new&amp;param_begin_date=".date("YmdHis",$current_time)."&amp;param_end_date=".date("YmdHis",$current_time_next))."\"> <IMG border=no src=\"/images/$set_theme/$ico_new_event\"></A>";
    echo "</TD></TR><TR><TD align=left>";
    
    // event(s) assigned to the current time 
    for ($i=0 ; $i< count($events_array) ; $i++) {
      $obm_q_events->seek($events_array[$i]);
      $obm_q_events->next_record();
      if ( ($obm_q_events->f("calendarevent_datebegin") >= date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")<date("YmdHis",$current_time_next)) ) { 
	// the begin date match the current time 
	if ($obm_q_events->f("calendarevent_id") != $last_event_id) {
	  // we don't want the same event twice for two different contacts
	  echo substr($obm_q_events->f("calendarevent_datebegin"),8,2).":".substr($obm_q_events->f("calendarevent_datebegin"),10,2)."-".substr($obm_q_events->f("calendarevent_dateend"),8,2).":".substr($obm_q_events->f("calendarevent_dateend"),10,2)."- ";
	  echo "<A href=\"".$sess->url("agenda_index.php?action=detailconsult&amp;param_event=".$obm_q_events->f("calendarevent_id"))."\">";
	  // private event's title doesn't appear
	  if ($obm_q_events->f("calendarevent_privacy")==1) {
	    if (($user_contact_id == $obm_q_events->f("eventuser_contact_id")) || ($auth->auth["uid"] == $obm_q_events->f("calendarevent_usercreate")) ) {
	      echo $obm_q_events->f("calendarevent_title")."</A><br>";
	    } else {
	      echo $l_private."</A><br>";
	    }
	  } else {
	    echo $obm_q_events->f("calendarevent_title")."</A><br>";
	  }
	  $last_event_id=$obm_q_events->f("calendarevent_id");
	}
      }
    }
    echo "&nbsp;</TD></TR></TABLE>";
    echo "</TD></TR>";
  }
  
  echo "</TABLE>";
 


  

  echo "<br><br>";
   

}





//----------------------------------------------------------------------------------------------------------------------------------
////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the specified month
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $p_start_date : specified month ("YYYYmmddHHmmss")
//    $obm_q_events : list of event's records 
//    $user_contact_id : id of the contact assigned to the user 
//         (usually run_query_contactid_user($auth->auth["uid"])
//         used to know if he has rights to watch events on the planning 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
//    $color_attends : color of an event the user attend
//    $color_not_attend : color of an event the user doesn't attend 
/////////////////////////////////////////////////////////////////////////////////////
function dis_month_planning ($p_start_date,$obm_q_events,$user_contact_id,$contacts_array,$groups_array,$color_attend,$color_not_attend) {

  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table;
  // - Labels 
  global $l_private;
  // - Session variables
  global $sess,$auth;
  
  $year=substr($p_start_date,0,4);
  $month=substr($p_start_date,4,2);
  $day=substr($p_start_date,6,2);

  $p_start_time=mktime(0,0,0,$month,$day,$year);
  $end_time=mktime(0,0,0,$month+1,$day-1 ,$year);
  $next_month_time=mktime(0,0,0,$month+1,$day,$year);
  $prev_month_time=mktime(0,0,0,$month-1,$day,$year);

  $end_day=date("d",$end_time);
  $end_month=date("m",$end_time);
  $end_year=date("Y",$end_time);

  $dow=date("w",$p_start_time);
  $dow_end=date("w",mktime(0,0,0,$end_month,$end_day,$end_year)); 
  
  // Start of the first week 
  if ($dow == 0) {
    $start_week_time=mktime(0,0,0,$month,$day - 6,$year);
  } elseif ( $dow == 1 ) {
    $start_week_time=mktime(0,0,0,$month,$day,$year); 
  } else {
    $start_week_time=mktime(0,0,0,$month,$day - ($dow-1),$year);
  }

  // End of the last week 
  if (($dow_end) == 0) {
    $end_week_time=mktime(0,0,0,$end_month,$end_day-1,$end_year); //ca marche pas autrement mais pourquoi "-1" ???
  } else {
    $end_week_time=mktime(0,0,0,$end_month,$end_day + (7-$dow_end),$end_year);
  }

  // Sunday=0 but we begin with Monday=1 
  for ($i=1;$i<7;$i++) {
    $days[]=$i;
  }
  $days[]=0;
  
  //contacts id used as GET vars
  $params_contacts="";
  if ($contacts_array) {
    for ($i=0 ; $i<count($contacts_array) ; $i++) {
      $param_contacts.="&amp;contact_$i=".$contacts_array[$i];
    }
  }
  //groups id used as GET vars
  $param_groups="";
  if ($groups_array) {
    for ($i=0 ; $i<count($groups_array) ; $i++) {
      $param_groups.="&amp;group_$i=".$groups_array[$i];
    }
  }

  // Links to the previous and next months 
  echo "<CENTER><TABLE border=0 width=25%><TR><TD align=left>";
  // previous
  $url="agenda_index.php?action=view_month&amp;param_begin_date=".date("YmdHis",$prev_month_time);
  if ($param_contacts!="") $url.=$param_contacts;
  if ($param_groups!="") $url.=$param_groups;
  echo "\n<A href=\"".$sess->url($url)."\"> << </A>&nbsp;&nbsp;&nbsp;&nbsp;";
  // next
  echo "\n</TD><TD align=center><FONT size=+1 color=\"#$col_ok\"><B>".month_name($month)." ".$year."</B></FONT></TD><TD align=right>";

  $url="agenda_index.php?action=view_month&amp;param_begin_date=".date("YmdHis",$next_month_time);
  if ($param_contacts!="") $url.=$param_contacts;
  if ($param_groups!="") $url.=$param_groups;
  echo "\n<A href=\"".$sess->url($url)."\"> >> </A>&nbsp;&nbsp;&nbsp;&nbsp;";
  
  echo "</TD></TR></TABLE></CENTER>";
  echo "<BR>";

  $num_rows=$obm_q_events->nf(); // number of events
  $cpt_rows = 0;                 
  $obm_q_events->next_record();
  $end_occupied_time=0;          // time used to know when the colored cells end  
  $end_occupied_attend=0;        // flag used to know if the current user attend to the event which is the last to finish
  
  // some event could have begun the day before and not be finished 
  // we go to the first not occupied time area
  while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$p_start_time)) && ($obm_q_events->f("calendarevent_dateend")>$p_start_time) ) {
    $current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
    if ($current_end_time > $end_occupied_time) {
      $end_occupied_time=$current_end_time;
      if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	$end_occupied_attend=1;
      } else {
	$end_occupied_attend=0;
      }
    }
    $obm_q_events->next_record();
    $cpt_rows++;
  }
  $last_event_id=0;
  

  // events are sorted by days 
  if ($num_rows>0) {
    for ($d=0 ; $d< number_days_month($month) ; $d++) {
      $current_time = $p_start_time + 86400 * $d;
      $current_time_next = $current_time + 86400;
      $attend=0;
      $flag_event=0;
      while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")>=date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$current_time_next)) ) {
	$flag_event=1;
	if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	  $attend=1;
	}
	$events_array[]=$cpt_rows; // number of the record 
	$current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
	if ($current_end_time > $end_occupied_time) {
	  $end_occupied_time=$current_end_time;
	  // is the user attend to the event ? 
	  if ($user_contact_id == $obm_q_events->f("eventuser_contact_id")) {
	    $end_occupied_attend=1;
	  } else {
	    $end_occupied_attend=0;
	  }
	}
	$obm_q_events->next_record();
	$cpt_rows++;      
      }
      // cell color : 
      if ($attend==1) {
	// the current user attend to the event 
	$color_array[$d]=$color_attend;
      } elseif ($flag_event==1) {
	$color_array[$d]=$color_not_attend;
      } else {
	// is there at least an event for this hour ?
	if (!$flag_event) { 
	  if ($current_time<$end_occupied_time) {
	    // a previous event continue in this time zone
	    if ($end_occupied_attend==1) {
	      // the user attend to this event 
	      $color_array[$d]=$color_attend; 
	    } else {
	      $color_array[$d]=$color_not_attend; 
	    }
	  } else {
	    $color_array[$d]="";
	  }
	}    
      }
    }
  }

 

  echo "<TABLE border=1 width=100% bgcolor=\"#$col_table\">
        \n<TR>";

  // week days 
  for ($d=0;$d<7;$d++) {
    echo "\n<TD width=13% align=center>".weekday_name($days[$d])."</TD>";
  }  
  echo "</TR>";

  $num_rows=$obm_q_events->nf();
  $cpt_rows = 0;
  $obm_q_events->next_record(); 
  

  echo "<TR>";
  // Other days of the week which are not belonging to the month
  // Cells will remain blank
  while ($start_week_time < $p_start_time) {
    echo "<TD>&nbsp;</TD>";
    $start_week_time += 86400;
  } 
  
  
  for ($d=0 ; $d< number_days_month($month) ; $d++) {
    $current_time = $p_start_time + 86400 * $d;
    $current_time_next = $current_time + 86400;
    if (date("w",$current_time)==1) {
      // Monday
      echo "<TR>";
    }  
    // number of the day 
    echo "\n<TD width=9% valign=top"; 
    // occupied time is colored 
    if ($color_array[$d]!="") {
      echo " bgcolor=\"#".$color_array[$d]."\""; 
    }
    echo ">";
    
    echo "<TABLE border=0>
         <TR><TD align=left>";
    echo "<FONT size=-1><A href=\"".$sess->url("agenda_index.php?action=view_day&amp;param_begin_date=".date("YmdHis",$current_time))."\">".date("d",$current_time)."</A></FONT>";
    echo "</TD></TR><TR><TD>";
   

    // event(s) assigned to the current time 
    for ($i=0 ; $i< count($events_array) ; $i++) {
      $obm_q_events->seek($events_array[$i]);
      $obm_q_events->next_record();
      if ( ($obm_q_events->f("calendarevent_datebegin") >= date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")<date("YmdHis",$current_time_next)) ) { 
	// the begin date match the current time 
	if ($obm_q_events->f("calendarevent_id") != $last_event_id) {
	  // we don't want the same event twice for two different contacts
	  echo substr($obm_q_events->f("calendarevent_datebegin"),8,2).":".substr($obm_q_events->f("calendarevent_datebegin"),10,2)."-".substr($obm_q_events->f("calendarevent_dateend"),8,2).":".substr($obm_q_events->f("calendarevent_dateend"),10,2)."- ";
	  echo "<A href=\"".$sess->url("agenda_index.php?action=detailconsult&amp;param_event=".$obm_q_events->f("calendarevent_id"))."\">";
	  // private event's title doesn't appear
	  if ($obm_q_events->f("calendarevent_privacy")==1) {
	    if (($user_contact_id == $obm_q_events->f("eventuser_contact_id")) || ($auth->auth["uid"] == $obm_q_events->f("calendarevent_usercreate")) ) {
	      echo $obm_q_events->f("calendarevent_title")."</A><br>";
	    } else {
	      echo $l_private."</A><br>";
	    }
	  } else {
	    echo $obm_q_events->f("calendarevent_title")."</A><br>";
	  }
	  $last_event_id=$obm_q_events->f("calendarevent_id");
	}
      }
    }
    echo "&nbsp;</TD>";
    echo "</TR></TABLE>";
    echo "</TD>";
    if (date("w",$current_time)==0) {
      // Sunday
      echo "</TR>";
    } 
  }

   // Other days of the week which are not belonging to the month
  while ($current_time < $end_week_time) {
    echo "<TD>&nbsp;</TD>";
    $current_time += 86400;
  } 
  echo "</TR>";
   
  
  echo "</TABLE>";


  echo "<br><br>";

}







///////////////////////////////////////////////////////////////////////////////
// Form used to chose contacts and/or groups which will appear in the planning 
///////////////////////////////////////////////////////////////////////////////
// Args :
// ------
//   $action : current_action 
//   $p_start_date : start date of the planning 
//   $obm_q_contact_list : list of available contacts
//   $obm_q_group_list : list of available groups
//   $contacts_array : array of selected contacts
//   $groups_array : array of selected groups
///////////////////////////////////////////////////////////////////////////////
function dis_planning_contacts($action,$p_start_date,$obm_q_contact_list,$obm_q_group_list,$contacts_array,$groups_array) {
  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table;
  // - Labels 
  global $l_users, $l_groups, $l_select;
  // - Variables 
  global $sess;

  echo "<hr>
    <form name=\"form_planning_contacts\" method=POST action=\"".$sess->url("agenda_index.php")."\">
    <table border=0 width=30%>
    <tr>
      <td>";

  // Available Contacts 
  echo "\n<FONT COLOR=\"#$col_label\">".$l_users."</FONT><BR>";
  echo "\n<SELECT name=\"sel_contact_id[]\" size=6 multiple>";
  while ($obm_q_contact_list->next_record()) {
    echo "\n<OPTION value=".$obm_q_contact_list->f("contact_id");
  
    if (count($contacts_array)>0) {
       $tag_contact="";
       while ( (list($key, $contact_id) = each($contacts_array) ) && (strcmp($tag_contact,"")==0)) {
	 if ($obm_q_contact_list->f("contact_id") == $contact_id ) {
	   $tag_contact=" selected";
	 }
       }
       reset($contacts_array);
    }    
  
    echo " ".$tag_contact.">".$obm_q_contact_list->f("contact_lastname")." ".$obm_q_contact_list->f("contact_firstname")."</OPTION>";
  }
  echo "</SELECT>";

  echo "\n</TD><TD>"; 
  // Available Groups 
  echo "\n<FONT COLOR=\"#$col_label\">".$l_groups."</FONT><BR>";
  echo "\n<SELECT name=\"sel_group_id[]\" size=6 multiple>";
  while ($obm_q_group_list->next_record()) {
    echo "\n<OPTION value=".$obm_q_group_list->f("list_id");

    if (count($groups_array)>0) {
      $tag_group="";
      while ((list($key, $group_id) = each ($groups_array)) && (strcmp($tag_group,"")==0)) {
	if ($obm_q_group_list->f("list_id") == $group_id ) {
	  $tag_group=" selected";
	}
      }
      reset($groups_array);
    }

    echo " ".$tag_group.">".$obm_q_group_list->f("list_name")."</OPTION>";
  }
  echo "</SELECT>";
  echo "</TD></TR>
        </TABLE>";

  echo "\n<INPUT type=submit name=\"sub_planning_contacts\" value=\"".$l_select."\">";
  echo "\n<INPUT type=hidden name=\"action\" value=\"".$action."\">";
  echo "\n<INPUT type=hidden name=\"param_begin_date\" value=\"".$p_start_date."\">";
  echo "</FORM>";

}





///////////////////////////////////////////////////////////////////////////////
// Display the waiting events list for the current user 
///////////////////////////////////////////////////////////////////////////////
// Args :
// ------
//    $omb_q_waiting_events : list of user's waiting events
///////////////////////////////////////////////////////////////////////////////
function dis_waiting_events_list($obm_q_waiting_events) {
  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table;
  // - Labels 
  global $l_waiting_events,$l_accept,$l_reject,$l_validate,$l_from,$l_to;
  // - Session variables
  global $sess;

  echo "<form name=\"form_event_accept\" method=POST action=\"".$sess->url("agenda_index.php")."\">
    <table border=0>
    <tr>
      <td colspan=2>
        <font COLOR=\"#$col_label\">$l_waiting_events : </font></td>
      <td><font COLOR=\"#$col_label\">$l_accept ?</font></td>
      <td><font COLOR=\"#$col_label\">$l_reject ?</font></td>
    </tr>";
  while ($obm_q_waiting_events->next_record()) {
    $begin=$obm_q_waiting_events->f("calendarevent_datebegin");
    $end=$obm_q_waiting_events->f("calendarevent_dateend");
    echo "\n<TR><TD>";
    echo "<A href=\"".$sess->url("agenda_index.php?action=detailconsult&amp;param_event=".$obm_q_waiting_events->f("calendarevent_id"))."\">".$obm_q_waiting_events->f("calendarevent_title")."</A>";
    echo "</TD><TD>";
    echo "<FONT COLOR=\"#$col_label\">&nbsp;&nbsp;&nbsp;".$l_from."&nbsp;&nbsp;&nbsp;</FONT>"; 
    echo substr($begin,0,4)."-".substr($begin,4,2)."-".substr($begin,6,2)." ".substr($begin,8,2).":".substr($begin,10,2).":".substr($begin,12,2);
    echo "<FONT COLOR=\"#$col_label\">&nbsp;&nbsp;&nbsp;".$l_to."&nbsp;&nbsp;&nbsp;</FONT>";
    echo substr($end,0,4)."-".substr($end,4,2)."-".substr($end,6,2)." ".substr($end,8,2).":".substr($end,10,2).":".substr($end,12,2);
    echo "&nbsp;&nbsp;</TD><TD>";
    echo "<INPUT type=checkbox name=\"cb_accepted_".$obm_q_waiting_events->f("calendarevent_id")."\" 
           value=1 onClick=\"UnSelectChoice(this.form.cb_rejected_".$obm_q_waiting_events->f("calendarevent_id").")\">";
    echo "</TD><TD>";
    echo "<INPUT type=checkbox name=\"cb_rejected_".$obm_q_waiting_events->f("calendarevent_id")."\" 
           value=1 onClick=\"UnSelectChoice(this.form.cb_accepted_".$obm_q_waiting_events->f("calendarevent_id").")\">";
    echo "</TD></TR>";
  }
  echo "</TABLE><BR>";
  echo "\n<CENTER><INPUT type=submit name=\"sub_event_accept\" value=\"".$l_validate."\"></CENTER>";
  echo "\n<INPUT type=hidden name=\"action\" value=\"approve\">";
  echo "\n</FORM>";

}







///////////////////////////////////////////////////////////////////////////
// Display contacts and groups which can't attend to the event 
// Then the user can confirm or cancel the event insertion ...
///////////////////////////////////////////////////////////////////////////
// Args :
// ------
//    $action : current action (insert, update ... )
//    $p_event_id : main event's id 
//    $events_array : contains non inserted events id (because of conflicts with other events) (there can be several ids because of event's repetitions ... ) 
//    $contacts_conflict_array[event][contact] : 2 dims array indexed on event's id and the id of contact which is in conflict. it contains the contact's record
//    $groups_conflict_array[event][group] : 2 dims array indexed on event's id and the id of the group which is in conflict. it contains the group's record
//    $selected_contacts_array : contains the attending contacts id 
//    $selected_groups_array : contains the attending groups id
///////////////////////////////////////////////////////////////////////////  
function dis_conflict_events_form($action,$p_event_id,$events_array,$contacts_conflict_array,$groups_conflict_array,$selected_contacts_array,$selected_groups_array) {
  
  // - Themes
  global $set_theme,$col_label,$col_table,$col_textem,$col_ok,$col_a_table;
  // - Labels
  global $l_insert, $l_cancel, $l_to,$l_from;
  // - Session variables
  global $sess;

  echo "<br><br>";

  //echo "<ul>";
 
  // for each event 
  for ($i=0;$i<count($events_array);$i++) {
    $events_array[$i]->next_record();

    $year_begin=substr($events_array[$i]->f("calendarevent_datebegin"),0,4);
    $month_begin=substr($events_array[$i]->f("calendarevent_datebegin"),4,2);
    $day_begin=substr($events_array[$i]->f("calendarevent_datebegin"),6,2);
    $hour_begin=substr($events_array[$i]->f("calendarevent_datebegin"),8,2);
    $minute_begin=substr($events_array[$i]->f("calendarevent_datebegin"),10,2);
    $year_end=substr($events_array[$i]->f("calendarevent_dateend"),0,4);
    $month_end=substr($events_array[$i]->f("calendarevent_dateend"),4,2);
    $day_end=substr($events_array[$i]->f("calendarevent_dateend"),6,2);
    $hour_end=substr($events_array[$i]->f("calendarevent_dateend"),8,2);
    $minute_end=substr($events_array[$i]->f("calendarevent_dateend"),10,2);
    
    //echo "\n<li>";
    echo $events_array[$i]->f("calendarevent_title")."<FONT color=\"#$col_label\"> ".$l_from." </FONT>".$year_begin."-".$month_begin."-".$day_begin." ".$hour_begin.":".$minute_begin." <FONT color=\"#$col_label\"> ".$l_to." </FONT>".$year_end."-".$month_end."-".$day_end." ".$hour_end.":".$minute_end."</u><br>";
    $event_id=$events_array[$i]->f("calendarevent_id");
    //echo "<TABLE border=0>";
    echo "\n<ul>";
    if ($contacts_conflict_array) {
      for ($j=0;$j<count($contacts_conflict_array[$event_id]);$j++) {
	//echo "<TR><TD>";
	// contacts which planning is in conflict
	$contacts_conflict_array[$event_id][$j]->next_record();
	echo "\n<li>".$contacts_conflict_array[$event_id][$j]->f("contact_lastname")." ".$contacts_conflict_array[$event_id][$j]->f("contact_firstname");
	//echo "</TD></TR>";
      }
    }
    if ($groups_conflict_array) {
      for ($j=0;$j<count($groups_conflict_array[$event_id]);$j++) {
	//echo "<TR><TD>";
	  // groups which planning is in conflict
	$groups_conflict_array[$event_id][$j]->next_record();
	echo "\n<li>".$groups_conflict_array[$event_id][$j]->f("list_name");
	//echo "</TD></TR>";
      }
    }
    echo "\n</ul><br>";

  }
  //echo "</TABLE>";
  echo "<br><br>";
  //echo "</ul>";
  
  echo "<TABLE border=0><TR><TD>";
  
  // To confirm the event 
  echo "<FORM name=form_confirm_conflict_event method=POST action=\"".$sess->url("agenda_index.php")."\">";
  switch ($action) {
   case "insert": $value="confirm_insert"; break;
   case "update": $value="confirm_update"; break;
  }
  echo "\n<INPUT type=hidden name=action value=\"".$value."\">";
  echo "\n<INPUT type=hidden name=param_event value=".$p_event_id.">";
  
  if ($action=="update") {
    echo "\n<INPUT type=hidden name=param_temp_event value=".$events_array[0]->f("calendarevent_id").">";
    for ($i=0;$i<count($events_array);$i++) {
      $event_id=$events_array[$i]->f("calendarevent_id");
      for ($j=0;$j<count($contacts_conflict_array);$j++) {
	echo "\n<INPUT type=hidden name=contact_conflict_".$j." value=\"".$contacts_conflict_array[$event_id][$j]->f("contact_id")."\">";
      }  
      for ($j=0;$j<count($groups_conflict_array);$j++) {
	echo "\n<INPUT type=hidden name=group_conflict_".$j." value=\"".$groups_conflict_array[$event_id][$i]->f("list_id")."\">";
      }
    }
  }
  
  for ($i=0;$i<count($selected_contacts_array);$i++) {
    echo "\n<INPUT type=hidden name=contact_".$i." value=\"".$selected_contacts_array[$i]."\">";
  }
  for ($i=0;$i<count($selected_groups_array);$i++) {
    echo "\n<INPUT type=hidden name=group_".$i." value=\"".$selected_groups_array[$i]."\">";
  }
  echo "\n<INPUT type=submit name=sub_confirm_conflict value=\"".$l_insert."\">";
  echo "\n</FORM>";

  echo "</TD><TD>";

  // To cancel the event 
  echo "<FORM name=form_cancel_conflict_event method=POST action=\"".$sess->url("agenda_index.php")."\">";
  switch ($action) {
   case "insert": $value="cancel_insert"; break;
   case "update": $value="cancel_update"; break;
  }
  echo "\n<INPUT type=hidden name=action value=\"".$value."\">";
  echo "\n<INPUT type=hidden name=param_event value=".$p_event_id.">";
  if ($action=="update") {
    echo "\n<INPUT type=hidden name=param_temp_event value=".$events_array[0]->f("calendarevent_id").">";
    for ($i=0;$i<count($events_array);$i++) {
      $event_id=$events_array[$i]->f("calendarevent_id");
      for ($j=0;$j<count($contacts_conflict_array);$j++) {
	echo "\n<INPUT type=hidden name=contact_conflict_".$j." value=\"".$contacts_conflict_array[$event_id][$j]->f("contact_id")."\">";
      }  
      for ($j=0;$j<count($groups_conflict_array);$j++) {
	echo "\n<INPUT type=hidden name=group_conflict_".$j." value=\"".$groups_conflict_array[$event_id][$i]->f("list_id")."\">";
      }
    }
  }
  for ($i=0;$i<count($selected_contacts_array);$i++) {
    echo "\n<INPUT type=hidden name=contact_".$i." value=\"".$selected_contacts_array[$i]."\">";
  }
  for ($i=0;$i<count($selected_groups_array);$i++) {
    echo "\n<INPUT type=hidden name=group_".$i." value=\"".$selected_groups_array[$i]."\">";
  }
  echo "\n<INPUT type=submit name=sub_cancel_conflict value=\"".$l_cancel."\">";
  echo "\n</FORM>";

  echo "</TABLE>";
  
  

}
    
  














//-----------------------
// DIS_DAY_PLANNING


 //***********************************************************************************************
  /*

    echo "<TABLE border=1 width=80%>";
  // Planning of the day from 8:00 to 20:00
  for ($h=8 ; $h<20 ; $h++) {
    $current_time = $p_start_time + 3600 * $h;
    $current_time_next = $current_time + 3600;
    echo "\n<TR><TD width=20% align=center>".date("H:i",$current_time)."</TD>";    
    echo "<TD width=80%";

    if ($num_rows>0) {
      $flag_event=0;
      while ( ($cpt_rows<$num_rows) && (($obm_q_events->f("calendarevent_datebegin")>=date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$current_time_next))))  {
	// we update the time of end occupied time
	$current_end_time=mktime(substr($obm_q_events->f("calendarevent_dateend"),8,2),substr($obm_q_events->f("calendarevent_dateend"),10,2),0,substr($obm_q_events->f("calendarevent_dateend"),4,2),substr($obm_q_events->f("calendarevent_dateend"),6,2),substr($obm_q_events->f("calendarevent_dateend"),0,4));
	if ($current_end_time > $end_occupied_time) {
	  $end_occupied_time=$current_end_time;
	}
	// occupied time is colored 
	if (($current_time_next>=$end_occupied_time) || ($current_time<=$end_occupied_time))  {
	  if ($flag_event==0) {
	    echo " bgcolor=\DD\">";
	  } 
	} else { 
	  echo ">"; 
	}
	// we don't want the same event twice for two different contacts
	if ($obm_q_events->f("calendarevent_id") != $last_event_id) {
	  // name of the event and link to its detailed information page
	  echo substr($obm_q_events->f("calendarevent_datebegin"),8,2).":".substr($obm_q_events->f("calendarevent_datebegin"),10,2)."-".substr($obm_q_events->f("calendarevent_dateend"),8,2).":".substr($obm_q_events->f("calendarevent_dateend"),10,2)."- ";
	  echo "<A href=\"".$sess->url("agenda_index.php?action=detailconsult&amp;param_event=".$obm_q_events->f("calendarevent_id"))."\">";
	  // private event's title doesn't appear
	  if ($obm_q_events->f("calendarevent_privacy")==1) {
	    if (($user_contact_id != $obm_q_events->f("eventuser_contact_id")) && ($auth->auth["uid"] != $obm_q_events->f("calendarevent_usercreate")) ) {
	      echo $l_private."</A><br>";
	    } else {
	      echo $obm_q_events->f("calendarevent_title")."</A><br>";
	    }
	  } else {
	    echo $obm_q_events->f("calendarevent_title")."</A><br>";
	  } 
	  $last_event_id=$obm_q_events->f("calendarevent_id");
	}
	// next event 
	$obm_q_events->next_record();
	$cpt_rows++; 
	$flag_event=1;
      }
    }

    // is there at least an event for this hour ?
    if (!$flag_event) { 
      if ($current_time<$end_occupied_time) {
	echo " bgcolor=\"DD\">";
      } else {
	echo ">";
      }    
    }
    echo "&nbsp;</TD></TR>";
  }
  
  
  
  echo "</TABLE>";    
  */   





  
  



//-----------------------
// DIS_MONTH_PLANNING



  /*
  // Days 
  for ($d=0 ; $d< number_days_month($month) ; $d++) {
    $current_time = $p_start_time + 86400 * $d;
    $current_time_next = $current_time + 86400;
    if (date("w",$current_time)==1) {
      // Monday
      echo "<TR>";
    }  
    // number of the day 
    echo "\n<TD width=9% align=center>".date("d",$current_time)."<br>";  
    if ($num_rows>0) {
      while ( ($cpt_rows<$num_rows) && ($obm_q_events->f("calendarevent_datebegin")>=date("YmdHis",$current_time)) && ($obm_q_events->f("calendarevent_datebegin")< date("YmdHis",$current_time_next)) ) {
	// we don't want the same event twice for two different contacts
	if ($obm_q_events->f("calendarevent_id") != $last_event_id) {
	  // events assigned to the current time 
	  echo substr($obm_q_events->f("calendarevent_datebegin"),8,2).":".substr($obm_q_events->f("calendarevent_datebegin"),10,2)."-".substr($obm_q_events->f("calendarevent_dateend"),8,2).":".substr($obm_q_events->f("calendarevent_dateend"),10,2)."- ";
	  echo "<A href=\"".$sess->url("agenda_index.php?action=detailconsult&amp;param_event=".$obm_q_events->f("calendarevent_id"))."\">";
	   // private event's title doesn't appear
	  if ($obm_q_events->f("calendarevent_privacy")==1) {
	    if (($user_contact_id != $obm_q_events->f("eventuser_contact_id")) && ($auth->auth["uid"] != $obm_q_events->f("calendarevent_usercreate")) ) {
	      echo $l_private."</A><br>";
	    } else {
	      echo $obm_q_events->f("calendarevent_title")."</A><br>";
	    }
	  } else {
	    echo $obm_q_events->f("calendarevent_title")."</A><br>";
	  } 
	  $last_event_id=$obm_q_events->f("calendarevent_id");
	}
	$obm_q_events->next_record();
	$cpt_rows++;
      }      
    }
    echo "&nbsp;</TD>";
    if (date("w",$current_time)==0) {
      // Sunday
      echo "</TR>";
    } 
  }
  

   // Other days of the week which are not belonging to the month
  //if ($current_time <  $end_week_time) {
    while ($current_time < $end_week_time) {
      //echo "<TD>".$flag." ".date("Ymd",$current_time)." ? ".date("Ymd",$end_week_time)."&nbsp;%</TD>";
      echo "<TD>&nbsp;%</TD>";
      $current_time += 86400;
    } 
    echo "</TR>";
  //} 
 
  echo "</TABLE>";
  */




</SCRIPT>
