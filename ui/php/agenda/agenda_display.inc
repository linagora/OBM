<?
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_display.inc                                           //
//     - Desc : Agenda Display File                                          //
// 2002-11-26 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the year
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $agenda: agenda parameters
//    $obm_q_events : list of event's records 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_year_planning($agenda,$obm_q,$user_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer;
  global $set_weekstart_default,$l_daysofweek,$l_daysofweekshort;
  
  $getdate = $agenda["date"];

  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1]. '01'. '01';
  $this_year2 = $day_array2[1];
  $stop_year = ($day_array2[1]+1). '01'. '01';
  $unix_time = strtotime($getdate);
  $startYear = strtotime ($this_year);
  $checkad = date ("Ymd", $startYear);

  $next_year = strtotime ("+1 year", strtotime("$getdate"));
  $next_year = date ("Ymd", $next_year);
  $prev_year = strtotime ("-1 year", strtotime("$getdate"));
  $prev_year = date ("Ymd", $prev_year);

  $m=0;	
  $n=0;

  $calendar_user = store_users($user_q);
  $td_heigth = 20/count($calendar_user);
  $td_colspan = count($calendar_user);
  store_daily_events($obm_q, $current_events, $event_data,$this_year,$stop_year);

  $start_day = strtotime($set_weekstart_default);
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_day);
    $day = $l_daysofweekshort[$day_num];
    $dis_day .= "<td class=\"agendaHead2\">$day</td>\n";
    $start_day = strtotime("+1 day", $start_day); 
  }
  do {
    $monthlink = date("Ym", $startYear); 
    $monthlink = $monthlink . $this_day;        
    $minical_time = $startYear;
    $minical_month = date("m", $minical_time);
    $minical_year = date("Y", $minical_time);
    $first_of_month = $minical_year.$minical_month."01";
    $start_day = strtotime(dateOfWeek($first_of_month, $week_start_day));
    $i = 0;
    $whole_month = TRUE;
    $num_of_events = 0;
 
    $dis_calendar .= "<td width=\"210\">\n";
    $dis_calendar .= "<table border=\"0\" width=\"210\" cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCal\">\n";
    $dis_calendar .= "<tr>\n";
    $dis_calendar .= "<td colspan=\"7\" class=\"agendaHead2\">\n";
    $dis_calendar .= "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$monthlink)."\">\n";
    $dis_calendar .= localizeDate ("month", $startYear);
    $dis_calendar .= "</a>\n";
    $dis_calendar .= "</td>\n";
    $dis_calendar .= "</tr>\n";
    $dis_calendar .= "<tr>\n";
    $dis_calendar .= $dis_day;
    $dis_calendar .= "</tr>\n";
    $dis_calendar .= "<tr>\n";
    $dis_calendar .= "<td colspan=\"7\">\n";
    $dis_calendar .= "<table border=\"0\" width=\"210\" cellspacing=\"1\" cellpadding=\"0\" class=\"agendaYear\">\n";
    do {
      $day = date ("j", $start_day);
      $daylink = date ("Ymd", $start_day);
      $check_month = date ("m", $start_day);
      $current_time = $daylink;  
      if(is_array($current_events[$current_time])) {
	$dis_event_cal ="<table width=\"100%\">";
       	foreach($calendar_user as $id => $data ) {
	  $dis_event_cal .= "<tr>";   
   	   foreach($current_events[$current_time] as $event => $state) {
   	     if(in_array($id, $event_data[$event]["user_id"])){ 
   	       if($event_data[$event]["status"] == 0) {
		 $dis_event_cal .= "<td width=\"100%\" height=\"$td_heigth\" class=\"agendaEventHead\"></td>";
   		 $event_data[$event]["status"] = 1;
   	       }
   	       else{
   		 $dis_event_cal .= "<td width=\"100%\" height=\"$td_heigth\" class=\"eventbg\">";
    		  $dis_event_cal .= "</td>";
   	       }
   	     }
   	   }
   	   $dis_event_cal .= "</tr>"; 
     	}
	$dis_event_cal .="</table>";
      }
      else {
	$dis_event_cal="";
      }
      if ($check_month != $minical_month) $day= "$day";
      if ($i == 0) $dis_calendar .= "<tr>";
      if ( $daylink == date ("Ymd", time()) ) {
	$dis_calendar .= "<td width=\"30\" height=\"30\" class=\"agendaMonthOn\"\n"; 
	$dis_calendar .= "onmouseover=\"this.style.backgroundColor='#dddddd'\" "; 
	$dis_calendar .= "onmouseout=\"this.style.backgroundColor='#fdf8b9'\" "; 
	$dis_calendar .= "onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=".$daylink."'\">\n";
	$dis_calendar .= "<a class=\"agendaLink\" ";
	$dis_calendar .= "href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">\n";
	$dis_calendar .= "$day</a>$dis_event_cal</td>\n";
      }	
      elseif ($check_month == $minical_month) {	   
	$dis_calendar .= "<td width=\"30\" height=\"30\" class=\"agendaMonthReg\"\n"; 
	$dis_calendar .= "onmouseover=\"this.style.backgroundColor='#dddddd'\" "; 
	$dis_calendar .= "onmouseout=\"this.style.backgroundColor='#ffffff'\" "; 
	$dis_calendar .= "onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=".$daylink."'\">\n";
	$dis_calendar .= "<a class=\"agendaLink\" ";
	$dis_calendar .= "href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">\n";
	$dis_calendar .= "$day</a>$dis_event_cal</td> \n";
      }else {
	$dis_calendar .= "<td width=\"30\" height=\"30\" class=\"agendaMonthOff\" \n";
	$dis_calendar .= "onmouseover=\"this.style.backgroundColor='#dddddd'\" ";
	$dis_calendar .= "onmouseout=\"this.style.backgroundColor='#f2f2f2'\"  ";
	$dis_calendar .= "onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=".$daylink."'\"> \n";
	$dis_calendar .= "<a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">";
	$dis_calendar .= "$day ";
	$dis_calendar .= "</a> ";
	$dis_calendar .= "$dis_event_cal</td>\n";
      }
      $start_day = strtotime("+1 day", $start_day); 
      $i++;
      if ($i == 7) { 
	$dis_calendar .= "</tr>";
        $i = 0;
        $checkagain = date ("m", $start_day);
        if ($checkagain != $minical_month) $whole_month = FALSE;	
      }
    }  while ($whole_month == TRUE);
    $startYear = strtotime ("+1 month", $startYear);
    $dis_calendar .= "</table></td></tr></table></td>\n";
    if ($m < 2) $dis_calendar .= "<td width=\"20\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"20\" height=\"1\" alt=\"\" /></td>";
    $m++;
    $n++;
    if (($m == 3) && ($n < 12)) {
      $m = 0;
      $dis_calendar .= "</tr><tr><td colspan=\"5\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" /></td>\n";
      $dis_calendar .= "</tr><tr>\n";
    }
  } while (($m < 3) && ($n < 12)); 
  
  
  echo " 
<table class=\"agendaMain\">
 <tr>
  <td>
   <table width=\"676\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCal\">
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
	<td class=\"agendaHead\">
	 <a href=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=".$prev_year)."\">
	  <img src=\"/images/$set_theme/$ico_left_day\" alt=\"\" border=\"0\" align=\"right\" />
	 </a>
	</td>
	<td class=\"agendaHead\">
	 $this_year2
	</td>
	<td class=\"agendaHead\">
	 <a href=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=".$next_year)."\">
	  <img src=\"/images/$set_theme/$ico_right_day\" alt=\"\" border=\"0\" align=\"left\" />
	 </a>
	</td>
       </tr>
      </table>
     </td>
    </tr>
   </table>
   <table border=\"0\" width=\"670\" cellspacing=\"0\" cellpadding=\"0\">
    <tr>
     $dis_calendar
    </tr>
   </table>  
  </td>
  <td width=\"20\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"20\" height=\"1\" alt=\"\" /></td>
  <td width=\"160\" style=\"vertical-align : top;\">";
   html_planning_bar($agenda);
echo "
 </tr>
</table>

";

}

////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $agenda: agenda parameters
//    $obm_q_events : list of event's records 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////

function dis_month_planning($agenda,$obm_q,$user_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer;
  global $set_weekstart_default,$l_daysofweek;
	
  $date = mktime(0,0,0,"$this_month","$this_day","$this_year");	
  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);  
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1];
  $first_of_month = $this_year.$this_month."01";
  $stop_of_month = $this_year.($this_month +1)."01";
  $start_month_day = dateOfWeek($first_of_month,$set_weekstart_default);  
  $next_month = date( "Ymd", strtotime("+1 month",  $unix_time));
  $prev_month = date( "Ymd", strtotime("-1 month",  $unix_time));
  $display_date = localizeDate("month", $unix_time);  
  $start_day = strtotime($set_weekstart_default);
  $sunday = strtotime($start_month_day);
  $whole_month = TRUE;
  $num_of_events = 0;
  $calendar_user = store_users($user_q);
  $td_heigth = 90/count($calendar_user);
  $td_colspan = count($calendar_user);
  store_daily_events($obm_q, $current_events, $event_data,$first_of_month,$stop_of_month);

  //Display the day of a week
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_day);
    $day = $l_daysofweek[$day_num];
    
    $dis_day_head .= "<td width=\"105\" class=\"agendaHead2\">";
    $dis_day_head .= "$day</td>";
    $start_day = strtotime("+1 day", $start_day);
  }

  $i = 0;
  do {
    $day = date ("j", $sunday);
    $daylink = date ("Ymd", $sunday);
    $check_month = date ("m", $sunday);
    if ($check_month != $this_month) {
      $bgclass="class=\"agendaMonthOff\"";
    } else {
      if (date("Ymd",time()) == $daylink) {
	$bgclass="class=\"agendaMonthOn\"";
	} else {
	$bgclass="class=\"agendaMonthReg\"";
      }
    }
    if ($i == 0) $dis_month_cal .= "<tr>\n";
    $dis_month_cal .= "<td $bgclass width=\"105\" height=\"105\">\n";
    $dis_month_cal .= "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\">\n";
    $dis_month_cal .= "<tr>\n";
    $dis_month_cal .= "<td class=\"agendaCell2\">\n";    
    if (($check_month == $this_month)) {
      $dis_month_cal .= "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">";
    } else {
      $dis_month_cal .= "<a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">";
    }
    $dis_month_cal .= "$day</a>\n";
    $dis_month_cal .= "</td>\n";
    $current_time = $daylink;  

    if(is_array($current_events[$current_time])) {
      foreach($calendar_user as $id => $data ) {
	$dis_month_cal .= "<tr>";   
	foreach($current_events[$current_time] as $event => $state) {
	  if(in_array($id, $event_data[$event]["user_id"])){ 
	    if($event_data[$event]["status"] == 0) {
	      $dis_month_cal .= "<td height=\"$td_heigth\" class=\"agendaEventHead\">".$event_data[$event]["title"]."</td>";
	      $event_data[$event]["status"] = 1;
	    }
	    else{
	      $dis_month_cal .= "<td height=\"$td_heigth\" class=\"eventbg\">";
	      $dis_month_cal .= $event_data[$event]["title"]."</td>";
	    }
	  }
	}
	$dis_month_cal .= "</tr>"; 
      }
    }
    $dis_month_cal .= "</tr>\n";
    $dis_month_cal .= "</table>\n";
    $dis_month_cal .= "</td>\n";
    $sunday = strtotime("+1 day", $sunday); 
    $i++;
    if ($i == 7) { 
      $dis_month_cal .= "</tr>\n";
      $i = 0;
      $checkagain = date ("m", $sunday);
      if ($checkagain != $this_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);
  
  
  echo "
<table class=\"agendaMain\">
 <tr>
  <td>
   <table width=\"735\" cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCal\">
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td class=\"agendaHead\">  	 
         <a href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$prev_month)."\">
          <img src=\"/images/$set_theme/$ico_left_day\" alt=\"[Previous Month]\" />
         </a>
        </td>
        <td class=\"agendaHead\">
	 $display_date
	</td>
        <td class=\"agendaHead\">
	 <a href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$next_month)."\">
	  <img src=\"/images/$set_theme/$ico_right_day\" alt=\"[Next Month]\" />
	 </a>
	</td>
       </tr>
      </table> 
     </td>
    </tr>
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td colspan=\"7\" width=\"735\"><img src=\"images/spacer.gif\" width=\"735\" height=\"1\" alt=\"\" /></td>
       </tr>
       <tr>
        $dis_day_head 
       </tr>
       <tr>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
        <td width=\"105\"><img src=\"images/spacer.gif\" width=\"105\" height=\"1\" alt=\"\" /></td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"0\" class=\"agendaMonth\">
       $dis_month_cal
      </table>
     </td>
    </tr>
   </table>
  </td>
  <td width=\"20\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"20\" height=\"1\" alt=\"\" /></td>
  <td width=\"160\" style=\"vertical-align : top;\">";
  html_planning_bar($agenda);
echo "
 </tr>
</table>
    
";


}

////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $agenda: agenda parameters
//    $obm_q_events : list of event's records 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_week_planning($agenda,$obm_q,$user_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer;
  global $set_weekstart_default, $set_start_time, $set_stop_time,$set_time_unit;
  
  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);
  $time_unit = 60 / $set_time_unit;
  $next_week = date("Ymd", strtotime("+1 week",  $unix_time));
  $prev_week = date("Ymd", strtotime("-1 week",  $unix_time));
  $tomorrows_date = date( "Ymd", strtotime("+1 day",  $unix_time));
  $yesterdays_date = date( "Ymd", strtotime("-1 day",  $unix_time));
  $start_week_day = strtotime(dateOfWeek($getdate, $set_weekstart_default));
  $end_week_time = $start_week_day + ((6 * 24) * 60 * 60);
  $start_week_time = strtotime("+$set_start_time hours",$start_week_day);
  $end_week_time = strtotime("+$set_stop_time hours",$end_week_time);
  $start_week = localizeDate("week", $start_week_time);
  $end_week =  localizeDate("week", $end_week_time);
  $display_date = "$start_week - $end_week";
  $week_day_list = "";
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1];
  $calendar_user = store_users($user_q);
  $td_width = 70/count($calendar_user);
  $td_colspan = count($calendar_user);
  store_events($obm_q, $current_events, $event_data,date("YmdHi",$start_week_time),date("YmdHi",$end_week_time));
// Week day listcount($contacts_array)
  for($i = $start_week_time; $i < ($start_week_time + 7*(25 * 60 * 60)); $i += (25 * 60 * 60) ) {
    $this_date = date("Ymd", $i);
    $this_date_l = localizeDate("week_list", $i);
    $week_day_list .= "<td colspan=\"$td_colspan\" width=\"70\"  class=\"agendaCell\">\n";
    $week_day_list .= "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$this_date)."\"> ";
    $week_day_list .= "$this_date_l</a>\n";
    $week_day_list .= "</td>\n";
 }
// Hour of the day
  for($i = $set_start_time; $i <= $set_stop_time; $i++) {
    $hour_day_list .= 	"<tr>";
    $hour_day_list .= 	"<td height=\"15\" rowspan=\"$set_time_unit\" class=\"agendaHour\">$i:00</td>";
    $hour_day_list .= 	"<td width=\"1\"></td>";
    for($j=0; $j<7;$j++) {
      $current_time = date("YmdHi", strtotime("+$j days $i hours",$start_week_day));  
      if(is_array($current_events[$current_time])) {
	foreach($calendar_user as $id => $user_data ) {
     	  if(isset($current_events[$current_time][$id]) && $current_events[$current_time][$id] != -1){        
  	    if($event_data[$id]["status"] == 0) {
  	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaEventHead\">H</td>";
  	      $event_data[$id]["status"] = 1;
  	    }
  	    else{
  	      $hour_day_list .= "<td width=\"$td_width\" class=\"eventbg\">";
   	       $hour_day_list .= $event_data[$current_events[$current_time][$id]]["title"]."</td>";
  	    }
  	  }
  	  else {
  	    $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
  	  }
    	}	  
      }else {
	$hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
      }
    }
    $hour_day_list .=   "</tr>\n";
    for($k=1;$k<$set_time_unit;$k++) {
      $hour_day_list .= "<tr><td height=\"15\" width=\"1\"></td>";
      for($j=0; $j<7;$j++) {
	$current_time = date("YmdHi", strtotime("+$j days $i hours ".($k*$time_unit)." minutes",$start_week_day));  
	if(is_array($current_events[$current_time])) {
	  foreach($calendar_user as $id => $user_data ) {
	    if(isset($current_events[$current_time][$id]) && $current_events[$current_time][$id] != -1){        
	      if($event_data[$id]["status"] == 0) {
		$hour_day_list .= "<td width=\"$td_width\" class=\"agendaEventHead\">H</td>";
		$event_data[$id]["status"] = 1;
	      }
	      else{
		$hour_day_list .= "<td width=\"$td_width\" class=\"eventbg\">";
		$hour_day_list .= $event_data[$current_events[$current_time][$id]]["title"]."</td>";
	      }
	    }
	    else {
	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
	    }
	  }	  
	}else {
	  $hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
	}  
      }
      $hour_day_list .=   "</tr>";
    }
  }
							
echo "
<table class=\"agendaMain\">
 <tr>
  <td>
   <table width=\"540\" cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCal\">
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=".$prev_week)."\">
          <img src=\"/images/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" />
         </a>
        </td>
        <td class=\"agendaHead\">
         $display_date
        </td>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=".$next_week)."\">
          <img src=\"/images/$set_theme/$ico_right_day\" alt=\"[Next Week]\" />
         </a>
        </td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">      			
       <tr>
        <td>
         <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
          <tr>
           <td class=\"agendaCell\" width=\"60\">
            <img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" />
           </td>
           <td width=\"1\">
           </td>
            $week_day_list
          </tr>
            $hour_day_list
         </table>
        </td>
       </tr>
      </table>
     </td>
    </tr>
   </table>
  </td>
  <td width=\"20\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"20\" height=\"1\" alt=\"\" /></td>
  <td width=\"160\" style=\"vertical-align : top;\">";
  html_planning_bar($agenda);
echo "
 </tr>
</table>
";
}

////////////////////////////////////////////////////////////////////////////////////
// Display the planning for the day
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $agenda: agenda parameters
//    $obm_q_events : list of event's records 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////

function dis_day_planning($agenda,$obm_q,$user_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer;
  global $set_start_time, $set_stop_time,$set_time_unit;

  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);
  $tomorrows_date = date( "Ymd", strtotime("+1 day",  $unix_time));
  $yesterdays_date = date( "Ymd", strtotime("-1 day",  $unix_time));
  $time_unit = 60 / $set_time_unit; 
  $display_date = localizeDate("day", $unix_time);
  $start_time = strtotime("+$set_start_time hours",strtotime($getdate));
  $end_time =  strtotime("+$set_stop_time hours",strtotime($getdate));
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1];
  $calendar_user = store_users($user_q);
  $td_width = 460/count($calendar_user);
  $td_colspan = count($calendar_user);
  store_events($obm_q, $current_events, $event_data,date("YmdHi",$start_time),date("YmdHi",$end_time));
  for($i = $set_start_time; $i <= $set_stop_time; $i++) {
    $hour_day_list .= 	"<tr>";
    $hour_day_list .= 	"<td height=\"15\" width=\"60\" rowspan=\"2\" class=\"agendaHour\">$i:00</td>";
    $hour_day_list .= 	"<td width=\"1\"></td>";
    $current_time = date("YmdHi", strtotime("+$i hours",strtotime($getdate)));  
    if(is_array($current_events[$current_time])) {
      foreach($calendar_user as $id => $user_data ) {
	if(isset($current_events[$current_time][$id]) && $current_events[$current_time][$id] != -1){        
	  if($event_data[$id]["status"] == 0) {
	    $hour_day_list .= "<td width=\"$td_width\" class=\"agendaEventHead\">H</td>";
	    $event_data[$id]["status"] = 1;
	  }
	  else{
	    $hour_day_list .= "<td width=\"$td_width\" class=\"eventbg\">";
	    $hour_day_list .= $event_data[$current_events[$current_time][$id]]["title"]."</td>";
	  }
	}
	else {
	  $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
	}
      }	  
    }else {
      $hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
    }
    $hour_day_list .=   "</tr>\n";
    for($k=1;$k<$set_time_unit;$k++) {
      $hour_day_list .= "<tr><td height=\"15\" width=\"1\"></td>";
      $current_time = date("YmdHi", strtotime("+$i hours ".($k*$time_unit)." minutes",strtotime($getdate)));  
      if(is_array($current_events[$current_time])) {
	foreach($calendar_user as $id => $user_data ) {
	  if(isset($current_events[$current_time][$id]) && $current_events[$current_time][$id] != -1){        
	    if($event_data[$id]["status"] == 0) {
	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaEventHead\">H</td>";
	      $event_data[$id]["status"] = 1;
	    }
	    else{
	      $hour_day_list .= "<td width=\"$td_width\" class=\"eventbg\">";
	      $hour_day_list .= $event_data[$current_events[$current_time][$id]]["title"]."</td>";
	    }
	  }
	  else {
	    $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
	  }
	}	  
      }else {
	$hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
      }  
    }
    $hour_day_list .=   "</tr>";
  }
 

echo "

<table class=\"agendaMain\">
 <tr>
  <td width=\"520\">
   <table width=\"520\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCal\">
    <tr>
     <td align=\"center\">
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$yesterdays_date)."\">
          <img src=\"/images/$set_theme/$ico_left_day\" alt=\"[Yesterday]\" />
         </a>
        </td>
        <td class=\"agendaHead\">
         $display_date
        </td>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$tomorrows_date)."\">
          <img src=\"/images/$set_theme/$ico_right_day\" alt=\"[Tomorrow]\" />
         </a>
        </td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">      			
       <tr>
        <td>
         <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
          <tr>
           <td width=\"60\"><img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" /></td>
           <td width=\"1\"></td>
           <td><img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" /></td>
          </tr>
	  $hour_day_list
         </table>
        </td>
       </tr>
      </table>
     </td>
    </tr>
   </table>
  </td>
  <td width=\"20\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"20\" height=\"1\" alt=\"\" /></td>
  <td width=\"160\" style=\"vertical-align : top;\">";
  html_planning_bar($agenda);
echo "
 </tr>
</table>
";
}

////////////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $agenda: agenda parameters
///////////////////////////////////////////////////////////////////////////////

function html_planning_bar($agenda) {
  global $set_theme,$ico_spacer,$l_go_to,$l_tomorrow_event;
  global $set_weekstart_default, $l_daysofweekreallyshort;
  
  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);


  $next_week = date("Ymd", strtotime("+1 week",  $unix_time));
  $prev_week = date("Ymd", strtotime("-1 week",  $unix_time));
  $tomorrows_date = date( "Ymd", strtotime("+1 day",  $unix_time));
  $yesterdays_date = date( "Ymd", strtotime("-1 day",  $unix_time));

  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1];

  $check_week = strtotime($getdate);
  $start_week_time = strtotime(dateOfWeek(date("Ymd", strtotime("$this_year-01-01")), $set_weekstart_default));
  $end_week_time = $start_week_time + (6 * 25 * 60 * 60);

  $month_time = strtotime("$this_year-01-01");

  $year_time = strtotime(($this_year-3)."-01-01");
  
  $dis_month = localizeDate ("month", $unix_time);

  $start_week_day = strtotime($set_weekstart_default);

  $minical_time = strtotime($this_year.'-'.$this_month.'-15');
  $minical_month = date("m", $minical_time);
  $minical_year = date("Y", $minical_time);
  $first_of_month = $minical_year.$minical_month."01";
  $start_day = strtotime(dateOfWeek($first_of_month, $set_weekstart_default));


  $whole_month = TRUE;
  $num_of_events = 0;

// Week Selection

  do {
    $weekdate	  = date ("Ymd", $start_week_time);
    $select_week1 = localizeDate("week_jump", $start_week_time);
    $select_week2 = localizeDate("week_jump", $end_week_time);
    if (($check_week >= $start_week_time) && ($check_week <= $end_week_time)) {
      $sel_week .= "<option value=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=".$weekdate)."\" selected=\"selected\">";
      $sel_week .= "$select_week1 - $select_week2</option>";
    } else {
      $sel_week .= "<option value=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=".$weekdate)."\">";
      $sel_week .= "$select_week1 - $select_week2</option>";
    }
    $start_week_time =  strtotime ("+1 week", $start_week_time);
    $end_week_time = $start_week_time + (6 * 25 * 60 * 60);
  } while (date("Y", $start_week_time) <= $this_year);

// Month Selection

  for ($i=0; $i<12; $i++) {
    $monthdate = date ("Ymd", $month_time);
    $month_month = date("m", $month_time);
    $select_month = localizeDate("month", $month_time);
    if ($month_month == $this_month) {
      $sel_month .= "<option value=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$monthdate)."\" selected=\"selected\">";
      $sel_month .= "$select_month</option>\n";
    } else {
      $sel_month .= "<option value=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=".$monthdate)."\">";
      $sel_month .= "$select_month</option>\n";
    }
    $month_time = strtotime ("+1 month", $month_time);
}

// Year Selection

  for ($i=-3; $i < (3); $i++) {
    $year_year = date ("Y", $year_time);
    $yeardate = date("Ymd", $year_time);
    if ($year_year == $this_year) {
      $sel_year .= "<option value=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=".$yeardate)."\" selected=\"selected\">";
      $sel_year .= "$year_year</option>\n";
    } else {
      $sel_year .=  "<option value=\"".url_prepare("agenda_index.php?action=view_year&amp;param_date=".$yeardate)."\">";
      $sel_year .=  "$year_year</option>\n";
    }
    $year_time = strtotime ("+1 year", $year_time);
}

// Minicalendar Head

  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekreallyshort[$day_num];
    $dis_minical_head .= "<td align=\"center\" class=\"agendaHead2\">$day</td>\n";
    $start_week_day = strtotime("+1 day", $start_week_day); 
  }
  
// Minicalendar
  $i = 0;
  do {
    $day = date ("j", $start_day);
    $daylink = date ("Ymd", $start_day);
    $check_month = date ("m", $start_day);
    if ($check_month != $minical_month) 
      $day= "<a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
    else
      if (date("Ymd",time()) == $daylink) {
	$day= "<a class=\"agendaLink3\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
      } else {
	$day= "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=".$daylink)."\">$day</a>";
      }
     
    if ($i == 0) $dis_minical .=  "<tr>\n";
    $dis_minical .= "<td class=\"agendaCell\">\n";
    $dis_minical .=  "$day\n";
    $dis_minical .=  "</td>\n";
    $start_day = strtotime("+1 day", $start_day); 
    $i++;
    if ($i == 7) { 
      $dis_minical .=  "</tr>\n";
      $i = 0;
      $checkagain = date ("m", $start_day);
      if ($checkagain != $minical_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);


echo "

   <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"170\">
    <tr>
     <td>
      <table cellpadding=\"0\" cellspacing=\"0\" width=\"170\" class=\"agendaCal\">
       <tr>
        <td width=\"1%\" class=\"agendaHead2\">
         <img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" />
        </td>
        <td width=\"98%\" class=\"agendaHead2\">$l_go_to</td>
        <td width=\"1%\" class=\"agendaHead2\"></td>
       </tr>
       <tr>
  	<td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\"  /></td>
       </tr>
       <tr>
        <td width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\" alt=\"\" /></td>
	<td colspan=\"2\">
         <form action=\"\">
          <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
            $sel_year
          </select>
         </form>
        </td>
       </tr>
       <tr>
  	<td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
       </tr>
       <tr>
        <td width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\" alt=\"\" /></td>
	<td colspan=\"2\">
         <form action=\"\">
          <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
            $sel_month
          </select>
         </form>
        </td>
       </tr>
       <tr>
  	<td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
       </tr>
       <tr>
        <td width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\"alt=\"\"  /></td>
	<td colspan=\"2\">
         <form action=\"\">
          <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
	    $sel_week
          </select>
         </form>
        </td>
       </tr>
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"3\" alt=\"\" /></td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" /></td>
    </tr>
    <tr>
     <td>
      <table cellpadding=\"0\" cellspacing=\"0\" width=\"170\" class=\"agendaCal\">
       <tr>
        <td width=\"1%\" class=\"agendaHead2\">
         <img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" />
        </td>
        <td width=\"98%\" class=\"agendaHead2\">$l_tomorrow_event</td>
	<td width=\"1%\" class=\"agendaHead2\"></td>
       </tr>
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
       </tr>
       <tr>
        <td width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\" alt=\"\" /></td>
        <td colspan=\"2\" class=\"agendaHead2\" align=\"left\">


<!-- VARIABLE PHP -->
         <a class=\"psf\" href=\"javascript:openEventInfo('CS%2B-%2BLab%2B2%2BDue', 'School', 'All day event', '', '')\">
          <font class=\"G10B\">CS - Lab 2 Due</font>
         </a>


        </td>
       </tr>
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"148\" height=\"6\" alt=\"\" /></td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" /></td>
    </tr>
    <tr>
     <td>

      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\" width=\"170\">
       <tr>
        <td width=\"1\" class=\"agendaHead2\">
         <img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" />
        </td>
	<td nowrap=\"nowrap\" class=\"agendaHead2\">$dis_month</td>
	<td width=\"1\" class=\"agendaHead2\"></td>
       </tr>
       <tr>
        <td colspan=\"3\" bgcolor=\"#FFFFFF\" align=\"center\">
         <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#FFFFFF\">
       	  <tr>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"3\" alt=\"\" /></td>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"1\" alt=\"\" /></td>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"1\" alt=\"\" /></td>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"1\" alt=\"\" /></td>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"1\" alt=\"\" /></td>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"1\" alt=\"\" /></td>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"1\" alt=\"\" /></td>
          </tr>
          <tr>
	   $dis_minical_head
          </tr>
          <tr>
	   <td><img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"3\" alt=\"\" /></td>
          </tr>
	   $dis_minical
          <tr>
           <td colspan=\"7\"><img src=\"/images/$set_theme/$ico_spacer\" height=\"6\" alt=\"\" /></td>
          </tr>
         </table>
        </td>
       </tr>
      </table>
     </td>
    </tr>
   </table>
  </td>
";
}
////////////////////////////////////////////////////////////////////////////////////
// Display the new event form
////////////////////////////////////////////////////////////////////////////////////
// Arguments :
// -----------
//    $agenda: agenda parameters
//    $obm_q_events : list of event's records 
//    $contacts_array : array of selected contacts 
//    $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////

function  dis_event_form($action, $agenda, $usr_q, $category_list, $p_user_array) {
  global $set_theme,$set_weekstart_default;
  // -- Labels
  global $l_users, $l_title, $l_priority, $l_private, $l_category, $l_description,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_delete,$l_delete_all,$l_update,$l_repeat;
  global $l_repeatkind,$l_repeatdays,$l_use_endrepeat,$l_interval,$l_none,$l_daily,$l_weekly;
  global $l_monthly,$l_yearly, $l_carac,$l_header_agenda,$l_format,$l_daysofweekshort,$l_update;
  global $l_checkdelete,$l_insert,$l_force;
  
  if($action == "detailupdate") {
    $title = $obm_q_event->f("calendarevent_title");
    $category_id = $obm_q_event->f("calendarevent_category_id");
    $priority = $obm_q_event->f("calendarevent_priority");
    $description = $obm_q_event->f("calendarevent_description");
    $datebegin = substr($obm_q_event->f("calendarevent_datebegin"),0,8);
    $timebegin = substr($obm_q_event->f("calendarevent_datebegin"),8,4);
    $dateend = substr($obm_q_event->f("calendarevent_dateend"),0,8);
    $timeend = substr($obm_q_event->f("calendarevent_dateend"),8,6);
    $privacy = $obm_q_event->f("calendarevent_privacy"); 
    $occupied_day = $obm_q_event->f("calendarevent_occupied_day");
    $repeat_kind = $obm_q_event->f("calendarevent_repeatkind");
    $repeat_interval = $obm_q_event->f("calendarevent_repeat_interval");
    $repeat_days =$obm_q_event->f("calendarevent_repeatdays"); 
    $repeat_end = $obm_q_event->f("calendarevent_endrepeat");
  } else {
    if (isset($agenda["id"])) { $id = $agenda["id"]; }
    $title = $agenda["title"];
    $category_id = $agenda["category"];
    $priority = $agenda["priority"];
    $description = $agenda["description"];
    $datebegin = substr($agenda["date_begin"],0,8);
    $timebegin = substr($agenda["date_begin"],8,4);
    $dateend = substr($agenda["date_end"],0,8);
    $timeend = substr($agenda["date_begin"],8,6);
    $privacy = $agenda["privacy"]; 
    $occupied_day = $agenda["occupied"];
    $repeat_kind = $agenda["kind"];
    $repeat_interval = $agenda["interval"];
    $repeat_days =$agenda["repeat_days"]; 
    $repeat_end = $agenda["repeat_end"];
    $force = $agenda["force"];
  }

  
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"param_event\" id=\"param_event\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
    $dis_delete = "
      <form method=\"post\" name=\"f_sup_societe\"
      onsubmit=\"if (valider_suppression()) return true; else return false;\"
      action=\"" . url_prepare("company_index.php") . "\"> 
      <table class=\"details\">
        <tr>
          <td>
      <input type=\"hidden\" name=\"action\" value=\"check_delete\" />
      <input type=\"hidden\" name=\"param_company\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_checkdelete\" />
         </td>
       </tr>
      </table>
      </form>
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
  }

  $$repeat_kind .= "selected=\"selected\" ";
  $dis_sel_kind .= "<select name=\"sel_repeat_kind\">\n<option value=\"0\">aucune</option>";
  $dis_sel_kind .= "\n<option value=\"daily\" $daily>$l_daily</option>";
  $dis_sel_kind .= "\n<option value=\"weekly\" $weekly>$l_weekly</option>";
  $dis_sel_kind .= "\n<option value=\"monthly\" $monthly>$l_monthly</option> ";
  $dis_sel_kind .= "\n<option value=\"yearly\" $yearly>$l_yearly</option> ";
  $dis_sel_kind .= "\n</select>";

  switch($priority) {
    case 1 : $tag_low="selected=\"selected\""; break;
    case 2 : $tag_medium="selected=\"selected\""; break;
    case 3 : $tag_high="selected=\"selected\""; break; 
    default :
      $tag_low="";
      $tag_medium="selected=\"selected\"";
      $tag_high=""; 
    break;
  }
  
  $dis_sel_prio .= "<select name=\"sel_priority\">";
  $dis_sel_prio .= "<option value=0 ".$tag_low.">".$l_low."</option>";
  $dis_sel_prio .= "<option value=1 ".$tag_medium.">".$l_medium."</option>";
  $dis_sel_prio .= "<option value=2 ".$tag_high.">".$l_high."</option>";
  $dis_sel_prio .= "</select>";

  // eventcategory select
  $sel_cat = "<select name=\"sel_category_id\">"; 
  while ($category_list->next_record()) {
    $c_id = $category_list->f("eventcategory_id");
    $c_label = $category_list->f("eventcategory_label");
    $sel_cat .= "<option value=\"$c_id\"";
    if ($c_id == $category_id) {
      $sel_cat .= " selected=\"selected\"";
    }
    $sel_cat .= ">$c_label</option>";
  }
  $sel_cat .= "</select>";

 //Repetition days
  $start_week_day = strtotime($set_weekstart_default);

  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekshort[$day_num];
    $dis_repeat_day .= "<input type=\"checkbox\" name=\"cb_repeatday_".$i."\" value=\"1\"";
      if (strcmp(substr($repeat_days,$i,1),"1")==0) {
	$dis_repeat_day .= " checked = \"checked\"";
      }
      $dis_repeat_day .= " /> $day";

    $start_week_day = strtotime("+1 day", $start_week_day); 
  }

  // userobm select
  $dis_sel_user = "<select name=\"sel_user_id[]\" size=\"6\" multiple=\"multiple\">";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $u_lname = $usr_q->f("userobm_lastname");
    $u_fname = $usr_q->f("userobm_firstname");
    $dis_sel_user .= "\n<option value=\"$u_id\"";
       
    if (count($p_user_array)>0) {
      $tag_user = "";
      while ( (list($key, $user_id) = each($p_user_array) )
              && ($tag_user == "") ) {
	if ($u_id == $user_id ) {
	  $tag_user = " selected=\"selected\"";
	}
      }
      reset($p_user_array);
    }
    
    $dis_sel_user .= " $tag_user>$u_lname $u_fname</option>";
  } 
  $dis_sel_user .= "</select>";
  
  if($force == 1){
    $dis_force = "<input type=\"checkbox\" id=\"cb_force\" checked=\"checked\" name=\"cb_force\" />";
  }else{
    $dis_force = "<input type=\"checkbox\" id=\"cb_force\" name=\"cb_force\" />";
  }
  
  if($privacy == 1){
    $dis_privacy = "<input type=\"checkbox\" id=\"cb_privacy\" checked=\"checked\" name=\"cb_privacy\" />";
  }else{
    $dis_privacy = "<input type=\"checkbox\" id=\"cb_privacy\" name=\"cb_privacy\" />";
  }

  
  // --- HTML Template --------------------------------------------------------
  echo "
  <form method=\"post\" name=\"f_mod_calendar\"
   onsubmit=\"if (check_calendar(this)) return true; else return false;\"
   action=\"".url_prepare("agenda_index.php")."\">
   <table class=\"details\">
    <tr>
     <td colspan=\"2\" class=\"detailHead\">$l_header_agenda</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_title</td>
     <td><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"25\" value=\"$title\" /></td>
    </tr>  
    <tr>
     <td class=\"detailLabel\">$l_force</td>
     <td>$dis_force</td>
    </tr>   
    <tr>
     <td class=\"detailLabel\">$l_private</td>
     <td>$dis_privacy</td>
    </tr> 
    <tr>
     <td class=\"detailLabel\">$l_category</td>
     <td>$sel_cat</td>
    </tr>    
    <tr>
     <td class=\"detailLabel\">$l_users</td>
     <td>$dis_sel_user</td>
    </tr>
    <tr>
     <td colspan=\"2\" class=\"detailHead\">$l_carac</td>
    </tr>   
    <tr>
     <td class=\"detailLabel\">$l_datebegin $l_format</td>
     <td><input type=\"text\" id=\"tf_date_begin\" name=\"tf_date_begin\" maxlength=\"10\" size=\"12\" value=\"$date_begin\" /></td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_timebegin</td>
     <td><input type=\"text\" id=\"tf_time_begin\" name=\"tf_time_begin\" maxlength=\"5\" size=\"6\" value=\"$timebegin\" /></td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_dateend $l_format</td>
     <td><input type=\"text\" id=\"tf_date_end\" name=\"tf_date_end\" maxlength=\"10\" size=\"12\" value=\"$date_end\" /></td>       
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_timeend</td>
     <td><input type=\"text\" id=\"tf_time_end\" name=\"tf_time_end\" maxlength=\"5\" size=\"6\" value=\"$timeend\" /></td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_priority</td>
     <td>$dis_sel_prio</td>
    </tr>      
    <tr>
     <td colspan=\"2\" class=\"detailHead\">$l_repeat</td>
    </tr>   
    <tr>    
     <td class=\"detailLabel\">$l_repeatkind</td> 
     <td>$dis_sel_kind</td>
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_use_endrepeat</td> 
     <td><input type=\"text\" id=\"tf_repeat_end\" name=\"tf_repeat_end\" maxlength=\"10\" size=\"12\" value=\"$repeat_end\" /></td>
    </tr>
    <tr>  
     <td class=\"detailLabel\">$l_repeatdays</td>
     <td >$dis_repeat_day</td>
    </tr>
    <tr>
     <td colspan=\"2\" class=\"detailHead\">$l_description</td>
    </tr>       
    <tr>    
     <td colspan=\"2\" class=\"detailLabel\"><textarea name=\"ta_event_description\" rows=\"6\" cols=\"72\">$description</textarea></td> 
    </tr>     
   </table>
   <table class=\"details\">
    <tr>
     <td>
      $dis_button
     </td>
    </tr>
   </table>
  </form>
    $dis_delete
   ";
 }
 
