<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_display.inc                                           //
//     - Desc : Agenda Display File                                          //
// 2002-11-26 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the year
// Parameters:
//   - $agenda: agenda parameters
//   - $obm_q_events : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_year_planning($agenda,$obm_q,$calendar_user,$usr_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer;
  global $set_weekstart_default,$l_daysofweek,$l_daysofweekshort,$ico_calendar_nouser;
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

  $td_heigth = 8;
  $td_width = 8;
  $td_colspan = count($calendar_user);
  store_daily_events($obm_q, $current_events, $event_data,strtotime($this_year),strtotime($stop_year));
  $start_day = strtotime($set_weekstart_default);
  for ($i=0; $i<7; $i++) {
    $day_num = date("w","$start_day");
    $day = $l_daysofweekshort[$day_num];
    $dis_day .= "<td class=\"agendaHead2\">$day</td>";
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
    $num_week = 0;
    $dis_calendar .= "<td width=\"210\">
    <table border=\"0\" width=\"210\" cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCal\">
    <tr>
      <td colspan=\"7\" class=\"agendaHead2\">
      <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_month&amp;param_date=$monthlink")."\">" . localizeDate ("month", $startYear) . "</a>
      </td>
    </tr>
    <tr>
      $dis_day
    </tr>
    <tr>
      <td colspan=\"7\">
      <table border=\"0\" width=\"210\" cellspacing=\"1\" cellpadding=\"0\" class=\"agendaYear\">\n";
     do {
      $day = date ("j", $start_day);
      $daylink = date ("Ymd", $start_day);
      $check_month = date ("m", $start_day);
      $current_time = $daylink;  
      if(is_array($current_events[$current_time])) {
	$dis_event_cal ="<table class=\"agendaEventYear\"><tr>";
	$tr_users =0;
      foreach($calendar_user as $id => $data ) {
	  if($tr_users == 3) {
	    $dis_event_cal .= "</tr><tr>";   
	   }
	   if(is_array($current_events[$current_time][$id])) {
    	     $dis_event_cal .= "<td class=\"agendaEventYear\">";
	     $dis_event_cal .= "<img src=\"/images/$set_theme/".$data["image"]."\" alt=\"\"/></td>";
     	   }
   	   else {
	     $dis_event_cal .= "<td class=\"agendaEventYear\"><img src=\"/images/$set_theme/$ico_calendar_nouser\" alt=\"\"/></td>";		
	   }
	   $tr_users++;	   
	 }
	 $dis_event_cal .="</tr></table>";
      }
      else {
	$dis_event_cal="";
      }
      if ($check_month != $minical_month) $day= "$day";
      if ($i == 0) $dis_calendar .= "<tr>";
      if ( $daylink == date ("Ymd", time()) ) {
	$dis_calendar .= "
          <td width=\"30\" height=\"30\" class=\"agendaMonthOn\"
              onmouseover=\"this.style.backgroundColor='#dddddd'\" 
	      onmouseout=\"this.style.backgroundColor='#f4e5a9'\"
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=$daylink'\">
	  <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$daylink")."\">$day</a>$dis_event_cal
          </td>";
      }	
      elseif ($check_month == $minical_month) {	   
	$dis_calendar .= "
          <td width=\"30\" height=\"30\" class=\"agendaMonthReg\" 
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#ffffff'\" 
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=$daylink'\">
          <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$daylink")."\">$day</a>$dis_event_cal
          </td>";
      } else {
	$dis_calendar .= "
          <td width=\"30\" height=\"30\" class=\"agendaMonthOff\"
	      onmouseover=\"this.style.backgroundColor='#dddddd'\"
	      onmouseout=\"this.style.backgroundColor='#f2f2f2'\"
	      onclick=\"window.location.href='agenda_index.php?action=view_day&amp;param_date=$daylink'\">
	  <a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$daylink")."\">$day</a>$dis_event_cal
          </td>";
      }
      $start_day = strtotime("+1 day", $start_day); 
      $i++;
      if ($i == 7) { 
	$num_week++; 
	$dis_calendar .= "</tr>";
        $i = 0;
        $checkagain = date ("m", $start_day);
        if ($checkagain != $minical_month) $whole_month = FALSE;	
      }
    }  while ($whole_month == TRUE);
    if($num_week < 6) {
      $dis_calendar .= "<tr><td  height=\"30\" class=\"agendaMonthOff\" colspan=\"7\"></td></tr>";
    }
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
  
  
  $block = "
<div class=\"agendaMain\">
   <table cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCalYear\">
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
   <table cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCalYear\">
    <tr>
     $dis_calendar
    </tr>
   </table>  
</div>

";
return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the planning for the month
// Parameters:
//   - $agenda: agenda parameters
//   - $obm_q_events : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_month_planning($agenda,$obm_q,$calendar_user,$usr_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer,$ico_new_event,$auth;
  global $set_weekstart_default,$l_daysofweek,$l_description,$ico_event,$set_start_time,$set_stop_time;
  global $l_private, $l_private_description;  
  $date = mktime(0,0,0,"$this_month","$this_day","$this_year");	
  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);  
 
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1];
  $first_of_month = $this_year.$this_month."01";
  $stop_of_month = $this_year.(substr("0".($this_month +1),-2))."01";
  $start_month_day = dateOfWeek($first_of_month,$set_weekstart_default);  
  $next_month = date( "Ymd", strtotime("+1 month",  $unix_time));
  $prev_month = date( "Ymd", strtotime("-1 month",  $unix_time));
  $display_date = localizeDate("month", $unix_time);  
  $start_day = strtotime($set_weekstart_default);
  $sunday = strtotime($start_month_day);
  $whole_month = TRUE;
  $num_of_events = 0;
  $nb_user = count($calendar_user);
  $td_width = floor(105/$nb_user);
  $td_colspan = $nb_user;
    
  store_daily_events($obm_q, $current_events, $event_data,strtotime($first_of_month),strtotime($stop_of_month));
  //Display the day of a week
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_day);
    $day = $l_daysofweek[$day_num];
    
    $dis_day_head .= "<td width=\"105\" class=\"agendaHead2\">$day</td>";
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
    if ($i == 0) {
      $dis_month_cal .= "<tr>\n";
      $num = date('w', strtotime($set_weekstart_default));
      $delta_thursday = date("w",strtotime("-$num days",strtotime("thursday")))-date("w",strtotime("-$num days",$sunday));
      $week_num = date("W",strtotime("$delta_thursday days",$sunday));
      $dis_month_cal .= "<td class=\"agendaHead\" style=\"vertical-align:middle;\"><a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$daylink")."\" >$week_num</a></td>";
    }
    $daylink_begin = date("YmdHi",strtotime("+$set_start_time hours",strtotime($daylink)));
    $daylink_end = date("YmdHi",strtotime("+$set_stop_time hours",strtotime($daylink)));

    $dis_month_cal .= "
      <td $bgclass width=\"105\" height=\"105\">
        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\">
        <tr>
          <td colspan=\"$td_colspan\" class=\"agendaCell2\">
          <a href=\"".url_prepare("agenda_index.php?action=new&amp;param_date_begin=$daylink_begin&amp;param_date_end=$daylink_end")."\" />
          <img align=\"left\" src=\"/images/$set_theme/$ico_new_event\" alt=\"[New]\" />
          </a>";
  
    if (($check_month == $this_month)) {
      $dis_month_cal .= "<a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$daylink")."\">";
    } else {
      $dis_month_cal .= "<a class=\"agendaLink2\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$daylink")."\">";
    }
    $dis_month_cal .= "$day</a></td>";
    $current_time = $daylink;  
    if(is_array($current_events[$current_time])) {
      $dis_month_cal .= "<tr>";
       foreach($calendar_user as $id => $data ) {	
	 $dis_month_cal .= "<td  valign=\"top\" width=\"$td_width\"><table heigth=\"100%\" width=\"$td_width\">";
	 if(is_array($current_events[$current_time][$id]))
	   foreach($current_events[$current_time][$id] as $event => $event_id) {
	     $dis_month_cal .= "<tr>
	       <td class=\"".$data["class"]."\">";
	     if($event_data[$event_id]["privacy"] == 0 || $id == $auth->auth["uid"]) {
	       $titleEvent = $event_data[$event_id]["title"];
	       $descEvent = $event_data[$event_id]["description"];
       	       $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$event_id."&param_date=".$daylink;
	       $typeEvent = $event_data[$event_id]["type"];
	     }
	     else {
	       $titleEvent = $l_private;
	       $descEvent = $l_private_description;
	       $linkEvent = "javascript:return false;";
	       $typeEvent = $l_private;
	     }   
	     $dis_month_cal .= "
	      <a class=\"agendaLink4\" href=\"$linkEvent\" 
	       onMouseOver=\"show(event,'$daylink-$event_id-$id'); return true;\" onMouseOut=\"hide('$daylink-$event_id-$id'); return true;\">";
	       	     
	     $hidden_div .= "
	      <div id=\"$daylink-$event_id-$id\" class=\"agendaHidden\">
		  <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
		  <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
		  <tr><td class=\"agendaEventHead\">
		     $typeEvent ".$event_data[$event_id]["begin"]."-".$event_data[$event_id]["end"]."
	            </td>
                  </tr>
		  <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
		  </table>
	      </div>";	
	     if ($nb_user == 1) {
      	       $dis_month_cal .= $event_data[$event_id]["begin"]."-";
	       $dis_month_cal .= $event_data[$event_id]["end"];
	     } else {
	       $dis_month_cal .= "<img src=\"/images/$set_theme/$ico_event\" alt=\"[D]\" /></a></td>";	
	     } 
	     $dis_month_cal .= "</tr>";   
	  }
	 else {
	   $dis_month_cal .= "<tr><td>&nbsp;</td>";
	 }
	 $dis_month_cal .= "</table></td>"; 
        }
	$dis_month_cal .= "</tr>";
      }
    $dis_month_cal .= "</table>
      </td>";
    $sunday = strtotime("+1 day", $sunday); 
    $i++;
    if ($i == 7) { 
      $dis_month_cal .= "</tr>\n";
      $i = 0;
      $checkagain = date ("m", $sunday);
      if ($checkagain != $this_month) $whole_month = FALSE;	
    }
  } while ($whole_month == TRUE);
  
  
  $block = "
<div class=\"agendaMain\">
   <table cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCalMonth\">
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
</div>
$hidden_div
    
";

return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the planning for the week beginning at the specified date
// Parameters:
//    - $agenda         : agenda parameters
//    - $obm_q_events   : list of event's records 
//    - $contacts_array : array of selected contacts 
//    - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_week_planning($agenda,$obm_q,$calendar_user,$usr_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer,$l_description ,$ico_event,$auth;
  global $set_weekstart_default, $set_start_time, $set_stop_time,$set_cal_interval,$ico_new_event;
  global $l_private,$l_private_description;
  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);
  $num = date('w', strtotime($set_weekstart_default));
  $delta_thursday = date("w",strtotime("-$num days",strtotime("thursday")))-date("w",strtotime("-$num days",$unix_time));
  $week_num = date("W",strtotime("$delta_thursday days",$unix_time));
  $time_unit = 60 / $set_cal_interval;
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
  $nb_user = count($calendar_user);
  $td_width = ($nb_user > 0 ? floor(70/$nb_user) : 70);
  $td_colspan = $nb_user;
  store_events($obm_q, $current_events, $event_data,$start_week_time,$end_week_time);
  for($i = $start_week_time; $i < ($start_week_time + 7*(25 * 60 * 60)); $i += (25 * 60 * 60) ) {
    $this_date = date("Ymd", $i);
    $this_date_l = localizeDate("week_list", $i);
    $week_day_list .= "
      <td colspan=\"$td_colspan\" width=\"70\"  class=\"agendaCell\">
        <a class=\"agendaLink\" href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$this_date")."\">$this_date_l</a>
      </td>";
    $daylink_begin = date("YmdHi",strtotime("+$set_start_time hours",strtotime($this_date)));
    $daylink_end = date("YmdHi",strtotime("+$set_stop_time hours",strtotime($this_date)));

    $under_week_day_list .= "
    <td colspan=\"$td_colspan\" width=\"70\"  class=\"agendaCell\">
     <a href=\"".url_prepare("agenda_index.php?action=new&amp;param_date_begin=$daylink_begin&amp;param_date_end=$daylink_end")."\" > 
      <img align=\"middle\" src=\"/images/$set_theme/$ico_new_event\" alt=\"[New]\" />
    </a>
   </td>";
  }
// Hour of the day
  for($i = $set_start_time; $i < $set_stop_time; $i++) {
    $hour_day_list .= 	"<tr>";
    $hour_day_list .= 	"<td height=\"15\" rowspan=\"$set_cal_interval\" class=\"agendaHour\">$i:00</td>";
    $hour_day_list .= 	"<td width=\"1\" height=\"15\"><img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" /></td>";
    for($j=0; $j<7;$j++) {
      $current_time = strtotime("+$j days $i hours",$start_week_day);  
      if(is_array($current_events[$current_time])) {
	$user_indice=1;
	foreach($calendar_user as $id => $user_data ) {
	  if(is_array($current_events[$current_time][$id])){
	    $temp_data =$event_data[current($current_events[$current_time][$id])];
	    $temp_ib = substr("0$i:00",-5);
	    $temp_ie = date("H:i", strtotime("+$j days $i hours $time_unit minutes",$start_week_day));
	    if(($temp_data["status"] > 0 && $temp_data["begin"] >= $temp_ib && $temp_data["begin"] < $temp_ie) || $i==$set_start_time) {
  	      $is_title = FALSE;	  
	      reset($current_events);
	      $indice_time = $current_time;
	      if(key($current_events) != $indice_time) {
		do{
		  next($current_events);
		}while(key($current_events) != $indice_time);
	      }
	      $rowspan_event = 0;
	      $event_head_display ="";
	      $temp_array = array();
	      $end_of_day = date("Ymd".$set_stop_time."00",$indice_time);
	      do{
		foreach($current_events[$indice_time][$id] as $h => $idOfEvent) {
	  	  if($is_title == FALSE) {
		    $td_begin = "<td width=\"$td_width\" class=\"".$user_data["class"]."\" ";
		    $is_title = TRUE;
		    $tempDate = date("Ymd",$indice_time);
		    if($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
		      $titleEvent = $event_data[$idOfEvent]["title"];
		      $descEvent = $event_data[$idOfEvent]["description"];
		      $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
		      $typeEvent = $event_data[$idOfEvent]["type"];
		    }
		    else {
		      $titleEvent = $l_private;
		      $descEvent = $l_private_description;
		      $linkEvent = "javascript:return false;";
		      $typeEvent = $l_private;
		    } 
		    $event_head_display .= "
		     <a class=\"agendaLink4\" href=\"$linkEvent\"
		      onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
		      onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		     
		    $hidden_div .= "
		    <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
			 <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			  <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			  <tr><td class=\"agendaEventHead\">
			    $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
		     	  </td></tr>
			  <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
			 </table>
		     </div>";		     
		    if($nb_user == 1) {	      
     		      $event_head_display .= "&#8226; $titleEvent";
		    }else {
		      $td_begin .= "style=\"text-align: center;\" ";	
		      $event_head_display .= "<img class=\"agendaEvent\" src=\"/images/$set_theme/$ico_event\" alt=\"[D]\" />";
		    }
		    $event_head_display .= "</a>";	
		    if($event_data[$idOfEvent]["status"] > 0 && $event_data[$idOfEvent]["begin"] >= $temp_ib && 
		       $event_data[$idOfEvent]["begin"] < $temp_ie) {
		      $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;  
		    }
		  }
		  elseif(!in_array($idOfEvent,$temp_array)) {	
		    $event_head_display .= "<br />";
		    $tempDate = date("Ymd",$indice_time);
		    if($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
		      $titleEvent = $event_data[$idOfEvent]["title"];
		      $descEvent = $event_data[$idOfEvent]["description"];
		      $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
		      $typeEvent = $event_data[$idOfEvent]["type"];
		    }
		    else {
		      $titleEvent = $l_private;
		      $descEvent = $l_private_description;
		      $linkEvent = "javascript:return false;";
		      $typeEvent = $l_private;
		    }  
		    $event_head_display .= "
		    <a class=\"agendaLink4\" href=\"$linkEvent\"
		      onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
		      onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		    $hidden_div .= "
		    <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
			 <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			  <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			  <tr><td class=\"agendaEventHead\">
			    $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
		     	  </td></tr>
			  <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
			 </table>
		     </div>";
                    if ($nb_user == 1) {	      
     		      $event_head_display .= "&#8226; $titleEvent";
		    } else {
		      $td_begin .= "style=\"text-align: center;\" ";	
		      $event_head_display .= "<img class=\"agendaEvent\" src=\"/images/$set_theme/$ico_event\" alt=\"[D]\" />";
		    }
		    $event_head_display .= "</a>";	 
		    if($event_data[$idOfEvent]["status"] > 0 && $event_data[$idOfEvent]["begin"] >= $temp_ib &&
		       $event_data[$idOfEvent]["begin"] < $temp_ie) {
		      $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;  
		    }
		  }
		}
		$rowspan_event++;
		$temp_array = $current_events[$indice_time][$id];
		$temp_array2 = next($current_events);
		$indice_time = key($current_events);
		$now = date("YmdHi",$indice_time);
	      } while (is_array($temp_array2[$id]) && count(array_intersect($temp_array,$temp_array2[$id])) != 0 
	             && $now < $end_of_day);
	      $rowspan = "rowspan=\"$rowspan_event\"";
	      $hour_day_list .= $td_begin."".$rowspan.">".$event_head_display;
	      $hour_day_list .= "</td>";	
	    }
	  }
	  else {
       	    if ($user_indice != $nb_user) {
	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaNoEvent\">&nbsp;</td>";
	    }
	    else {
      	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
       	    }
	  }
      	  $user_indice++;
      	}	  
      }else {
	$hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
      }
    }
    $hour_day_list .= "</tr>\n";
    for ($k=1;$k<$set_cal_interval;$k++) {
      $hour_day_list .= "<tr><td height=\"15\" width=\"1\"><img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" /></td>";
      for ($j=0; $j<7;$j++) {
      	$current_time = strtotime("+$j days $i hours ".($k*$time_unit)." minutes",$start_week_day);  
	if (is_array($current_events[$current_time])) {
	  $user_indice=1;
	  foreach ($calendar_user as $id => $user_data ) {
	    if(is_array($current_events[$current_time][$id])){    
	      $temp_data =$event_data[current($current_events[$current_time][$id])];
	      $temp_ib = substr("0$i:".(($k)*$time_unit),-5);
	      $temp_ie = date("H:i", strtotime("+$j days $i hours ".(($k+1)*$time_unit)." minutes",$start_week_day));
	      if($temp_data["status"] > 0 && $temp_data["begin"] >= $temp_ib && $temp_data["begin"] < $temp_ie) {	     
		$is_title = FALSE;	  
	       	$indice_time = $current_time;
		reset($current_events);
		if(key($current_events) != $indice_time) {
		  do{
		    next($current_events);
		  }while(key($current_events) != $indice_time);
		}
		$rowspan_event = 0;
		$event_head_display ="";
		$temp_array = array();
		$end_of_day = date("Ymd".$set_stop_time."00",$indice_time);
		do {
		  foreach($current_events[$indice_time][$id] as $h => $idOfEvent) {
	  	    if ($is_title == FALSE) {
	  	      $td_begin = "<td width=\"$td_width\" class=\"".$user_data["class"]."\" ";
		      $is_title = TRUE;
		      $tempDate = date("Ymd",$indice_time);
		      if ($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
			$titleEvent = $event_data[$idOfEvent]["title"];
			$descEvent = $event_data[$idOfEvent]["description"];
			$linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
			$typeEvent = $event_data[$idOfEvent]["type"];
		      }
		      else {
			$titleEvent = $l_private;
			$descEvent = $l_private_description;
			$linkEvent = "javascript: return false;";
			$typeEvent = $l_private;
		      }  
		      $event_head_display .= "
		       <a class=\"agendaLink4\" href=\"$linkEvent\"
			onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
			onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		       
		      $hidden_div .= "
		      <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
			   <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			    <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			    <tr><td class=\"agendaEventHead\">
			      $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
			    </td></tr>
			    <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
			   </table>
		       </div>";		     
		      if ($nb_user == 1) {	      
			$event_head_display .= "&#8226; $titleEvent";
		      } else {
			$td_begin .= "style=\"text-align: center;\" ";	
			$event_head_display .= "<img class=\"agendaEvent\" src=\"/images/$set_theme/$ico_event\" alt=\"[D]\" />";
		      }
		      $event_head_display .= "</a>";	 
		      $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;
		    }
		    elseif(!in_array($idOfEvent,$temp_array)) {	
		      $event_head_display .= "<br />";
		      $tempDate = date("Ymd",$indice_time);
		      if($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
			$titleEvent = $event_data[$idOfEvent]["title"];
			$descEvent = $event_data[$idOfEvent]["description"];
			$linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
			$typeEvent = $event_data[$idOfEvent]["type"];
		      }
		      else {
			$titleEvent = $l_private;
			$descEvent = $l_private_description;
			$linkEvent = "javascript: return false;";
			$typeEvent = $l_private;
		      }  
		      $event_head_display .= "
		       <a class=\"agendaLink4\" href=\"$linkEvent\"
			onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
			onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		       
		      $hidden_div .= "
		      <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
			   <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			    <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			    <tr><td class=\"agendaEventHead\">
			      $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
			    </td></tr>
			    <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
			   </table>
		       </div>";		     
		      if($nb_user == 1) {	      
			$event_head_display .= "&#8226; $titleEvent";
		      }else {
			$td_begin .= "style=\"text-align: center;\" ";	
			$event_head_display .= "<img class=\"agendaEvent\" src=\"/images/$set_theme/$ico_event\" alt=\"[D]\" />";
		      }
		      $event_head_display .= "</a>";	 
		      $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;
		    }
		  }
		  $rowspan_event++;
	  	  $temp_array = $current_events[$indice_time][$id];
	  	  $temp_array2 = next($current_events);
	  	  $indice_time = key($current_events);
		  $now = date("YmdHi",$indice_time);
		}while(is_array($temp_array2[$id]) && count(array_intersect($temp_array,$temp_array2[$id])) != 0 
	               &&  $now < $end_of_day);
  		$rowspan = "rowspan=\"$rowspan_event\"";
		$hour_day_list .= $td_begin."".$rowspan.">".$event_head_display;
		$hour_day_list .= "</td>";	
	      }
	    }
	    else {
	      if($user_indice != $nb_user) {
      		$hour_day_list .= "<td width=\"$td_width\" class=\"agendaNoEvent\">&nbsp;</td>";
	      }
	      else {
		$hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
	      }
	    }
	    $user_indice++;
	  }	  
      	}else {
      	  $hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
       	}  
      }
      $hour_day_list .=   "</tr>";
    }
  }
$block = "
  <div class=\"agendaMain\">
   <table cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCalWeek\">
    <tr>
     <td>
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$prev_week")."\">
          <img src=\"/images/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" />
         </a>
        </td>
        <td class=\"agendaHead\">
         $display_date
        </td>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$next_week")."\">
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
	  <tr>
	   <td class=\"agendaCell\" width=\"60\">
            $week_num<img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" />
           </td>
           <td width=\"1\">
           </td>
	   $under_week_day_list
          </tr>
            $hour_day_list
         </table>
        </td>
       </tr>
      </table>
     </tr>
    </table>      
   </div>
$hidden_div
";
return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the planning for the day
// Parameters:
//   - $agenda         : agenda parameters
//   - $obm_q_events   : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_day_planning($agenda,$obm_q,$calendar_user,$usr_q) {
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer,$ico_new_event;
  global $set_start_time, $set_stop_time,$set_cal_interval,$l_description,$set_weekstart_default;
  global $l_private,$l_private_description,$auth;

  $getdate = $agenda["date"];
  $unix_time = strtotime($getdate);
  //Des tests sont necessaires car la fonction W de date bug...
  $num = date('w', strtotime($set_weekstart_default));
  $delta_thursday = date("w",strtotime("-$num days",strtotime("thursday")))-date("w",strtotime("-$num days",$unix_time));
  $week_num = date("W",strtotime("$delta_thursday days",$unix_time));
  $tomorrows_date = date( "Ymd", strtotime("+1 day",  $unix_time));
  $yesterdays_date = date( "Ymd", strtotime("-1 day",  $unix_time));
  $time_unit = 60 / $set_cal_interval; 
  $display_date = localizeDate("day", $unix_time);
  $start_time = strtotime("+$set_start_time hours",strtotime($getdate));
  $end_time =  strtotime("+$set_stop_time hours",strtotime($getdate));
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $getdate, $day_array2);
  $this_day = $day_array2[3]; 
  $this_month = $day_array2[2];
  $this_year = $day_array2[1];
  $nb_user = count($calendar_user);
  $td_width = floor(460/$nb_user);
  $td_colspan = $nb_user;
  store_events($obm_q, $current_events, $event_data,$start_time,$end_time);
  for($i = $set_start_time; $i < $set_stop_time; $i++) {
    $hour_day_list .= "
      <tr>
        <td height=\"15\" width=\"60\" rowspan=\"$set_cal_interval\" class=\"agendaHour\">$i:00</td>
        <td height=\"15\" width=\"1\"></td>";
    $current_time = strtotime("+$i hours",strtotime($getdate));  
    if (is_array($current_events[$current_time])) {
      $user_indice = 0;
      foreach ($calendar_user as $id => $user_data) {
	if (is_array($current_events[$current_time][$id])) {
	  if ($event_data[current($current_events[$current_time][$id])]["status"]>0) {
	    $is_title = FALSE;	  
	    $indice_time = $current_time;
	    reset($current_events);
	    if (key($current_events) != $indice_time) {
		do {
		  next($current_events);
		} while (key($current_events) != $indice_time);
	    }
	    $rowspan_event = 0;
	    $event_head_display ="";
	    $temp_array = array();
	    do {
	      foreach ($current_events[$indice_time][$id] as $h => $idOfEvent) {
		if ($is_title == FALSE) {
		  $td_begin = "<td width=\"$td_width\" class=\"".$user_data["class"]."\" ";
		  $is_title = true;
		  $tempDate = date("Ymd",$indice_time);
		  if($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
		    $titleEvent = $event_data[$idOfEvent]["title"];
		    $descEvent = $event_data[$idOfEvent]["description"];
		    $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
		    $typeEvent = $event_data[$idOfEvent]["type"];
		  }
		  else {
		    $titleEvent = $l_private;
		    $descEvent = $l_private_description;
		    $linkEvent = "javascript: return false;";
		    $typeEvent = $l_private;
		  }  
		  $event_head_display .= "
		   <a class=\"agendaLink4\" href=\"$linkEvent\"
		    onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
		    onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		   
		  $hidden_div .= "
		  <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
		       <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			<tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			<tr><td class=\"agendaEventHead\">
			  $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
			</td></tr>
			<tr><td class=\"agendaEventHead\">$descEvent</td></tr>
		       </table>
		   </div>";	
		  $event_head_display .= "&#8226;";
		  if ($nb_user == 1) {
		    $event_head_display .= $event_data[$idOfEvent]["begin"]."-";
		    $event_head_display .= $event_data[$idOfEvent]["end"]." : ";
		  }		      
		  $event_head_display .= $titleEvent;
		  $event_head_display .= "</a>";	  
	  	  $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;
		}
		elseif (!in_array($idOfEvent,$temp_array)){
		  $event_head_display .= "<br />";
		  $tempDate = date("Ymd",$indice_time);
		  if ($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
		    $titleEvent = $event_data[$idOfEvent]["title"];
		    $descEvent = $event_data[$idOfEvent]["description"];
		    $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
		    $typeEvent = $event_data[$idOfEvent]["type"];
		  }
		  else {
		    $titleEvent = $l_private;
		    $descEvent = $l_private_description;
		    $linkEvent = "javascript: return false";
		    $typeEvent = $l_private;
		  }  
		  $event_head_display .= "
		   <a class=\"agendaLink4\" href=\"$linkEvent\"
		    onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
		    onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		   
		  $hidden_div .= "
		  <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
		       <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			<tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			<tr><td class=\"agendaEventHead\">
			  $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
			</td></tr>
			<tr><td class=\"agendaEventHead\">$descEvent</td></tr>
		       </table>
		   </div>";	
		  $event_head_display .= "&#8226;";
		  if($nb_user == 1) {
		    $event_head_display .= $event_data[$idOfEvent]["begin"]."-";
		    $event_head_display .= $event_data[$idOfEvent]["end"]." : ";
		  }		      
		  $event_head_display .= $titleEvent;
		  $event_head_display .= "</a>";	  
		  $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;
	       }
	      }
	      $rowspan_event++;
	      $temp_array = $current_events[$indice_time][$id];
	      $temp_array2 = next($current_events);
	      $indice_time = key($current_events);
	    }while(is_array($temp_array2[$id]) && count(array_intersect($temp_array,$temp_array2[$id])) != 0);
	    $rowspan = "rowspan=\"$rowspan_event\"";
	    $hour_day_list .= $td_begin."".$rowspan.">".$event_head_display;
	    $hour_day_list .= "</td>";	
	  }
        }       
	else {
	  if ($user_indice != $nb_user) {
	    $hour_day_list .= "<td width=\"$td_width\" class=\"agendaNoEvent\">&nbsp;</td>";
	  }
	  else {
	    $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
	  }	    
	}
	$user_indice++;
      }	  
    } else {
      $hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
    }    
    $hour_day_list .=   "</tr>\n";
    for ($k=1;$k<$set_cal_interval;$k++) {
      $hour_day_list .= "<tr><td height=\"15\" width=\"1\"></td>";
      $current_time = strtotime("+$i hours ".($k*$time_unit)." minutes",strtotime($getdate));  
      if (is_array($current_events[$current_time])) {
	$user_indice = 1;
	foreach ($calendar_user as $id => $user_data ) {
	  if (isset($current_events[$current_time][$id]) && $current_events[$current_time][$id] != -1){
	    if ($event_data[current($current_events[$current_time][$id])]["status"]>0) {
	      $is_title = FALSE;	  
	      $indice_time = $current_time;
	      reset($current_events);
	      if (key($current_events) != $indice_time) {
		do {
		  next($current_events);
		} while (key($current_events) != $indice_time);
	      }
	      $rowspan_event = 0;
	      $event_head_display ="";
	      $temp_array = array();
	      do {
	      	foreach($current_events[$indice_time][$id] as $h => $idOfEvent) {	
		  if ($is_title == FALSE) {
		    $td_begin = "<td width=\"$td_width\" class=\"".$user_data["class"]."\" ";
		    $tempDate = date("Ymd",$indice_time);
		    if ($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
		      $titleEvent = $event_data[$idOfEvent]["title"];
		      $descEvent = $event_data[$idOfEvent]["description"];
		      $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
		      $typeEvent = $event_data[$idOfEvent]["type"];
		    }
		    else {
		      $titleEvent = $l_private;
		      $descEvent = $l_private_description;
		      $linkEvent = "javascript:return false;";
		      $typeEvent = $l_private;
		    }  
		    $event_head_display .= "
		     <a class=\"agendaLink4\" href=\"$linkEvent\"
		      onMouseOver=\"show(event,'$tempDate-$idOfEvent--$id'); return true;\"
		      onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		     
		    $hidden_div .= "
		    <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
			 <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			  <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			  <tr><td class=\"agendaEventHead\">
			    $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
			  </td></tr>
			  <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
			 </table>
		     </div>";		     
		    $event_head_display .= "&#8226;";
		    if($nb_user == 1) {
		      $event_head_display .= $event_data[$idOfEvent]["begin"]."-";
		      $event_head_display .= $event_data[$idOfEvent]["end"]." : ";
		    }		      
		    $event_head_display .= "$titleEvent</a>";	  
		    $is_title = TRUE; 
		    $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;
		  }
		  elseif (!in_array($idOfEvent,$temp_array)){
		    $event_head_display .=" <br />";
		    $tempDate = date("Ymd",$indice_time);
		    if ($event_data[$idOfEvent]["privacy"] == 0 || $id == $auth->auth["uid"]) {
		      $titleEvent = $event_data[$idOfEvent]["title"];
		      $descEvent = $event_data[$idOfEvent]["description"];
		      $linkEvent = "agenda_index.php?action=detailconsult&param_event=".$idOfEvent."&param_date=".$tempDate;
		      $typeEvent = $event_data[$idOfEvent]["type"];
		    }
		    else {
		      $titleEvent = $l_private;
		      $descEvent = $l_private_description;
		      $linkEvent = "javascript:return false;";
		      $typeEvent = $l_private;
		    }  
		    $event_head_display .= "
		     <a class=\"agendaLink4\" href=\"$linkEvent\"
		      onMouseOver=\"show(event,'$tempDate-$idOfEvent-$id'); return true;\"
		      onMouseOut=\"hide('$tempDate-$idOfEvent-$id'); return true;\">";
		     
		    $hidden_div .= "
		    <div id=\"$tempDate-$idOfEvent-$id\" class=\"agendaHidden\">
			 <table bgcolor=\"#000000\" border=\"1\" width=\"200\" cellpadding=\"0\" cellspacing=\"1\">
			  <tr><td  class=\"agendaHead2\">$titleEvent</td></tr>
			  <tr><td class=\"agendaEventHead\">
			    $typeEvent ".$event_data[$idOfEvent]["begin"]."-".$event_data[$idOfEvent]["end"]."
			  </td></tr>
			  <tr><td class=\"agendaEventHead\">$descEvent</td></tr>
			 </table>
		     </div>";		     
		    $event_head_display .= "&#8226;";
		    if ($nb_user == 1) {
		      $event_head_display .= $event_data[$idOfEvent]["begin"]."-";
		      $event_head_display .= $event_data[$idOfEvent]["end"]." : ";
		    }		      
		    $event_head_display .= $titleEvent;
		    $event_head_display .= "</a>";	
		    $is_title = TRUE; 
		    $event_data[$idOfEvent]["status"] = $event_data[$idOfEvent]["status"] -1;
	       	  }
		}
  		$rowspan_event++;
	      	$temp_array = $current_events[$indice_time][$id];
		$temp_array2 = next($current_events);
		$indice_time = key($current_events);
	      } while (is_array($temp_array2[$id]) && count(array_intersect($temp_array,$temp_array2[$id])) != 0);
	      $rowspan = "rowspan=\"$rowspan_event\"";
	      $hour_day_list .= $td_begin."".$rowspan.">".$event_head_display;
	      $hour_day_list .= "</td>";	
	    }
	  }
	  else {
    	    if ($user_indice != $nb_user) {
  	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaNoEvent\">&nbsp;</td>";
  	    }
  	    else {
  	      $hour_day_list .= "<td width=\"$td_width\" class=\"agendaVide\">&nbsp;</td>";
  	    }	    
	  }
	  $user_indice++;
	}	  
      } else {
	$hour_day_list .= "<td colspan=\"$td_colspan\" class=\"agendaVide\">&nbsp;</td>";
      }  
    }
    $hour_day_list .=   "</tr>";
  }
  $daylink_begin = date("YmdHi",strtotime("+$set_start_time hours",strtotime($getdate)));
  $daylink_end = date("YmdHi",strtotime("+$set_stop_time hours",strtotime($getdate)));  
  $block = "

<div class=\"agendaMain\">
   <table cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCalDay\">
    <tr>
     <td align=\"center\">
      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$yesterdays_date")."\">
	  <img src=\"/images/$set_theme/$ico_left_day\" alt=\"[Yesterday]\" />
	 </a>
        </td>
        <td class=\"agendaHead\">
         $display_date
        </td>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=view_day&amp;param_date=$tomorrows_date")."\">
          <img src=\"/images/$set_theme/$ico_right_day\" alt=\"[Tomorrow]\" />
         </a>
        </td>
       </tr>
       <tr>
	<td class=\"agendaHead\" style=\"vertical-align:middle;\">
	 <a href=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$getdate")."\" class=\"agendaLink\">
	  $week_num
 	 </a>
  	</td>
	<td colspan=\"2\" class=\"agendaHead\">
	 <a href=\"".url_prepare("agenda_index.php?action=new&amp;param_date_begin=$daylink_begin&amp;param_date_end=$daylink_end")."\" > <img src=\"/images/$set_theme/$ico_new_event\" alt=\"[New]\" /> </a>
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
</div>
$hidden_div
";

return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the navigation bar
// Parameters:
//   - $agenda: agenda parameters
///////////////////////////////////////////////////////////////////////////////
function html_planning_bar($agenda,$usr_q,$p_user_array,$calendar_user) {
  global $set_theme,$ico_spacer, $l_go_to,$l_user_calendar,$l_back_mono,$l_free,$l_possible,$l_go,$l_occupied;
  global $set_weekstart_default, $l_daysofweekreallyshort,$l_validate,$set_cal_interval,$l_meeting_legend;
  global $action,$auth;
 
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
      $sel_week .= "
        <option value=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$weekdate")."\" selected=\"selected\">$select_week1 - $select_week2</option>";
    } else {
      $sel_week .= "
        <option value=\"".url_prepare("agenda_index.php?action=view_week&amp;param_date=$weekdate")."\">$select_week1 - $select_week2</option>";
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

// User Selection

  if ($action == "perform_meeting") {
    $duration = $agenda["duration"];     
    $dis_duration = "<input type=\"hidden\" name=\"param_duration\" value=\"$duration\" />";
    $new_action = "$action";
    $p_duration = "&amp;param_duration=$duration";
  }
  elseif ( ($action != "view_day") && ($action != "view_week") &&
       ($action != "view_month") && ($action != "view_year") ) {
    $new_action = "view_week";
    $check_sel_user = "onsubmit=\"if (check_count_user(this)) return true; else return false;\"";
  }  
  else {
    $new_action = $action;
    $check_sel_user = "onsubmit=\"if (check_count_user(this)) return true; else return false;\"";
  }
  if (count($p_user_array)>1 || !in_array($auth->auth["uid"],$p_user_array)) {
    $dis_back_monoview = "<br/><a class=\"agendaLink\" href=\"agenda_index.php?action=$new_action$p_duration&amp;sel_user_id[]=".$auth->auth["uid"]."\" >$l_back_mono</a>";
  }
  $dis_sel_user = "<select name=\"sel_user_id[]\" size=\"12\" multiple=\"multiple\" style=\"font-size:10px\">";
  
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
  $dis_sel_user .= "</select>
    <br />
    <input type=\"hidden\" name=\"action\" value=\"$new_action\" />
    $dis_duration
    <input type=\"submit\" value=\"$l_validate\" />";


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

  // Legend
  if($action == "perform_meeting") {
    $l_legend = $l_meeting_legend;
    $dis_leg_user ="
    <tr><td class=\"agendaPossible\" >
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class=\"detailText\">&nbsp;$l_possible
    </td></tr>
    <tr><td class=\"agendaFree\" >
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class=\"detailText\">&nbsp;$l_free
    </td></tr>
    <tr><td class=\"agendaOccupied\" >
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td class=\"detailText\">&nbsp;$l_occupied
    </td></tr>    
    ";
  }
  else {
    $l_legend = $l_user_calendar;
    foreach ($calendar_user as $user_id => $user_data) {
      $dis_leg_user .="
      <tr>
       <td class=\"".$user_data["class"]."\" >&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
       <td class=\"detailLabel\">&nbsp;&nbsp;".$user_data["name"]."</td>
      </tr>";
    }
  }

  $block = "
      
     <div class=\"agendaBar\">
      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
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
         <form action=\"\" onsubmit=\"return false;\">
          <select name=\"action\" class=\"agendaQuery\" onchange=\"window.location=(this.options[this.selectedIndex].value);\">
            $sel_year
          </select>
	  <input type=\"submit\" value=\"$l_go\" onclick=\"window.location=(this.form.action.options[this.form.action.selectedIndex].value);\" />
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
	  <input type=\"submit\" value=\"$l_go\" onclick=\"window.location=(this.form.action.options[this.form.action.selectedIndex].value);return false;\" />
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
	  <input type=\"submit\" value=\"$l_go\" onclick=\"window.location=(this.form.action.options[this.form.action.selectedIndex].value);return false;\" />
         </form>
        </td>
       </tr>
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"3\" alt=\"\" /></td>
       </tr>
      </table>
     </div>

     <div class=\"agendaBar\">
      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
       <tr>
        <td width=\"1%\" class=\"agendaHead2\">
         <img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" />
        </td>
        <td width=\"98%\" class=\"agendaHead2\">$l_user_calendar</td>
	<td width=\"1%\" class=\"agendaHead2\"></td>
       </tr>
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
       </tr>
       <tr>
	<td width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\" alt=\"\" /></td>
        <td colspan=\"2\" class=\"agendaVide\" align=\"left\">
	 <form action=\"agenda_index.php\" method=\"get\" $check_sel_user>
	  $dis_sel_user
	  <input type=\"hidden\" name=\"param_date\" value=\"$getdate\" />
	  $dis_back_monoview
	 </form>
        </td>
	<td width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\" alt=\"\" /></td>
       </tr>              
      </table>
     </div>
     
     <div class=\"agendaBar\">
      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
       <tr>
        <td width=\"1%\" class=\"agendaHead2\">
         <img src=\"/images/$set_theme/$ico_spacer\" width=\"1\" height=\"20\" alt=\"\" />
        </td>
        <td width=\"98%\" class=\"agendaHead2\">$l_legend</td>
	<td width=\"1%\" class=\"agendaHead2\"></td>
       </tr>
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"21\" height=\"6\" alt=\"\" /></td>
       </tr>
       <tr>
	<td colspan=\"3\" width=\"1%\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"4\" height=\"1\" alt=\"\" /></td>
       </tr>	
       $dis_leg_user
       <tr>
        <td colspan=\"3\"><img src=\"/images/$set_theme/$ico_spacer\" width=\"148\" height=\"6\" alt=\"\" /></td>
       </tr>
      </table>
     </div>

     <div class=\"agendaBar\">
      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
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
     </div>
      
";

    return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//   - $agenda         : agenda parameters
//   - $obm_q_events   : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_event_form($action, $agenda,$obm_q_event, $usr_q,$grp_q, $category_list, $p_user_array) {
  global $display;
  global $set_theme,$set_weekstart_default, $set_start_time, $set_stop_time,$set_cal_interval,$ico_mini_cal;
  global $ico_add_user, $ico_add_group;
  // -- Labels
  global $l_users, $l_title, $l_priority, $l_private, $l_cat, $l_description,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_update,$l_repeat,$popup_height,$popup_width;
  global $l_repeatkind,$l_repeatdays,$l_use_endrepeat,$l_daily,$l_weekly,$path;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_header_agenda,$l_format,$l_daysofweekshort,$l_update;
  global $l_none,$l_insert,$l_force,$l_repeat_update,$l_sendamail,$l_groups;
  global $l_agenda_select_group;

  $time_unit = 60 / $set_cal_interval;  

  if ($action == "detailupdate") {
    $p_grp_array = run_query_event_groups_array($obm_q_event->f("calendarevent_id"),$agenda["date"]);
    $title = htmlspecialchars($obm_q_event->f("calendarevent_title"));
    $category_id = $obm_q_event->f("calendarevent_category_id");
    $priority = $obm_q_event->f("calendarevent_priority");
    $desc = $obm_q_event->f("calendarevent_description");
    $datebegin = $obm_q_event->f("datebegin");
    $old_begin = date("YmdHi",$datebegin);
    $hourbegin = date("H",$datebegin);
    $minbegin = date("i",$datebegin);
    $datebegin = date("Y-m-d",$datebegin);
    $dateend = $obm_q_event->f("dateend");
    $old_end =date("YmdHi",$dateend);        
    $hourend = date("H",$dateend);
    $minend = date("i",$dateend);
    $dateend = date("Y-m-d",$dateend);
    $privacy = $obm_q_event->f("calendarevent_privacy"); 
    $occupied_day = $obm_q_event->f("calendarevent_occupied_day");
    $repeat_kind = $obm_q_event->f("calendarevent_repeatkind");
    $repeat_days =$obm_q_event->f("calendarevent_repeatdays");
    if($repeat_kind != "none") {
      $repeat_end = date("Y-m-d",$obm_q_event->f("calendarevent_endrepeat"));
    }
    $id = $obm_q_event->f("calendarevent_id");

  }

  // If parameters have been given, they supercede the default action value
  if (isset($agenda["id"])) { $id = $agenda["id"]; }
  if (is_array($agenda["group"])) { $p_grp_array = $agenda["group"];}
  if (!is_array($p_grp_array)) {$p_grp_array = array();}
  if (isset($agenda["title"])) { $title = $agenda["title"]; }
  if (isset($agenda["category"])) { $category_id = $agenda["category"]; }
  if (isset($agenda["priority"])) { $priority = $agenda["priority"]; }
  if (isset($agenda["description"])) { $desc = $agenda["description"]; }
  if (isset($agenda["date_begin"]) && !$datebegin) {
    $datebegin = substr($agenda["date_begin"],0,8);    
    ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $datebegin, $day_array);
    $datebegin = $day_array[1]."-".$day_array[2]."-".$day_array[3];
    $hourbegin = substr($agenda["date_begin"],8,2);
    $minbegin = substr($agenda["date_begin"],10,2);
    $day_array[0] = $agenda["date_begin"];
  }
  if (isset($agenda["date_end"]) && !$dateend) {
    $dateend = substr($agenda["date_end"],0,10);
    ereg ("([0-9]{4})([0-9]{2})([0-9]{2})", $dateend, $day_array2);
    $dateend = $day_array2[1]."-".$day_array2[2]."-".$day_array2[3];
    $hourend = substr($agenda["date_end"],8,2);
    $minend = substr($agenda["date_end"],10,2);
    $day_array2[0] = $agenda["date_end"];
  }
  if (isset($agenda["privacy"])) { $privacy = $agenda["privacy"]; }
  if (isset($agenda["occupied"])) { $occupied_day = $agenda["occupied"]; }
  if (isset($agenda["kind"])) { $repeat_kind = $agenda["kind"]; }
  if (isset($agenda["repeat_days"])) { $repeat_days = $agenda["repeat_days"];}
  if (isset($agenda["repeat_end"]) && $agenda["repeat_end"] != "") {
    ereg("([0-9]{4})([0-9]{2})([0-9]{2})", $agenda["repeat_end"], $day_array3);
    $repeat_end = $day_array3[1]."-".$day_array3[2]."-".$day_array3[3];
  }
  if (isset($agenda["force"])) { $force = $agenda["force"]; }
  if (isset($agenda["old_begin"])) { $old_begin = $agenda["old_begin"]; }
  if (isset($agenda["old_end"])) { $old_end = $agenda["old_end"]; }

  
  if (($action == "detailupdate") || ($action == "update")) {
    if ($repeat_kind == "none") {
      $check_activate = "disabled=\"disabled\" ";
      $check_case_style = "style=\"color:#cccccc; bacgound-color: #999999\"";
      $hidden_repeat = "<input type=\"hidden\" name=\"cb_repeat_update\" value=\"1\" />";	
    } else {
      $check_case_style = "style=\"color:red;\"";      
    }	
    $dis_repeat_update = "
      <tr>
        <td class=\"detailLabel\" $check_case_style >$l_repeat_update</td>
        <td class=\"detailForm\"><input type=\"checkbox\"name=\"cb_repeat_update\" $check_activate value=\"1\"</td>
      </tr>"; 
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"param_event\" id=\"param_event\" value=\"$id\" />
      <input type=\"hidden\" name=\"hd_old_begin\" id=\"hd_old_begin\" value=\"$old_begin\" />
      <input type=\"hidden\" name=\"hd_old_end\" id=\"hd_old_end\" value=\"$old_end\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
  }

  // $$repeat_kind: $daily | $weekly | $monthlybydate | $monthlybyday | $yearly
  $$repeat_kind .= "selected=\"selected\" ";
  $dis_sel_kind .= "
    <select name=\"sel_repeat_kind\">
      <option value=\"none\">$l_none</option>
      <option value=\"daily\" $daily>$l_daily</option>
      <option value=\"weekly\" $weekly>$l_weekly</option>
      <option value=\"monthlybydate\" $monthlybydate>$l_monthlybydate</option>
      <option value=\"monthlybyday\" $monthlybyday>$l_monthlybyday</option>
      <option value=\"yearly\" $yearly>$l_yearly</option>
    </select>";
  switch($priority) {
    case 1 : $tag_low = "selected=\"selected\""; break;
    case 2 : $tag_medium = "selected=\"selected\""; break;
    case 3 : $tag_high = "selected=\"selected\""; break; 
    default :
      $tag_low = "";
      $tag_medium = "selected=\"selected\"";
      $tag_high = ""; 
    break;
  }
  
  $dis_sel_prio .= "
    <select name=\"sel_priority\">
      <option value=\"1\" $tag_low>$l_low</option>
      <option value=\"2\" $tag_medium>$l_medium</option>
      <option value=\"3\" $tag_high>$l_high</option>
    </select>";

  // eventcategory select
  $sel_cat = "<select name=\"sel_category_id\">"; 
  while ($category_list->next_record()) {
    $c_id = $category_list->f("calendarcategory_id");
    $c_label = $category_list->f("calendarcategory_label");
    $sel_cat .= "<option value=\"$c_id\"";
    if ($c_id == $category_id) {
      $sel_cat .= " selected=\"selected\"";
    }
    $sel_cat .= ">$c_label</option>";
  }
  $sel_cat .= "</select>";

 // Repetition days
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
  $dis_sel_user = "<select name=\"sel_user_id[]\" size=\"12\" multiple=\"multiple\">";
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
 
  // group select
  $dis_sel_grp = "<select name=\"sel_grp_id[]\" size=\"5\" multiple=\"multiple\">";
  while ($grp_q->next_record()) {
    $g_id = $grp_q->f("group_id");
    $g_name = $grp_q->f("group_name");
    $dis_sel_grp .= "\n<option value=\"$g_id\"";
       
    if (count($p_grp_array)>0) {
      $tag_grp = "";
      while ( (list($key, $grp_id) = each($p_grp_array) )
              && ($tag_grp == "") ) {
	if ($g_id == $grp_id ) {
	  $tag_grp = " selected=\"selected\"";
	}
      }
      reset($p_grp_array);
    }
    
    $dis_sel_grp .= " $tag_grp>$g_name</option>";
  } 
  $dis_sel_grp .= "</select>";
 
  if ($force == 1){
    $dis_force = "<input type=\"checkbox\" id=\"cb_force\" value=\"1\" checked=\"checked\" name=\"cb_force\" />";
  } else {
    $dis_force = "<input type=\"checkbox\" id=\"cb_force\" value=\"1\" name=\"cb_force\" />";
  }
  
  if ($privacy == 1){
    $dis_privacy = "<input type=\"checkbox\" id=\"cb_privacy\" value=\"1\" checked=\"checked\" name=\"cb_privacy\" />";
  } else {
    $dis_privacy = "<input type=\"checkbox\" id=\"cb_privacy\" value=\"1\" name=\"cb_privacy\" />";
  }

  
  $dis_hour_b = "<select name=\"sel_time_begin\">";
  for ($i=$set_start_time;$i<$set_stop_time;$i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourbegin) {
      $dis_hour_b .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_b .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_b .= "</select>";  
 
  $dis_hour_e = "<select name=\"sel_time_end\">";
  for ($i=$set_start_time;$i<=$set_stop_time;$i++) {
    $current_hour = substr("0$i",-2,2); 
    if ($current_hour == $hourend) {
      $dis_hour_e .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    } else {
      $dis_hour_e .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_e .= "</select>";  

  $dis_min_b = "<select name=\"sel_min_begin\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $minbegin) {
      $dis_min_b .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_b .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_b .= "</select>";  

 $dis_min_e = "<select name=\"sel_min_end\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if ($current_min  == $minend) {
      $dis_min_e .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    } else {
      $dis_min_e .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_e .= "</select>";
  $display["title"] = "<div class=\"title\">$l_header_agenda : $title</div>";
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[5]";
  $url2 = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[6]&amp;ext_title=" . urlencode($l_agenda_select_group);


  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_mod_calendar\"
   onsubmit=\"if (check_calendar(this)) return true; else return false;\"
   action=\"".url_prepare("agenda_index.php")."\">
   <div class=\"detailHead\">$l_header_agenda</div>
   <table class=\"detail\">
    <tr>
     <td class=\"detailLabel\">$l_title</td>
     <td class=\"detailForm\"><input type=\"text\" id=\"tf_title\" name=\"tf_title\" maxlength=\"50\" size=\"25\" value=\"$title\" /></td>
    </tr> 
    <tr>
     <td class=\"detailLabel\">$l_sendamail</td>
     <td class=\"detailForm\"><input type=\"checkbox\" id=\"cb_mail\" value=\"1\" name=\"cb_mail\" /></td>
    </tr>       
    <tr>
     <td class=\"detailLabel\">$l_force</td>
     <td class=\"detailForm\">$dis_force</td>
    </tr>   
    <tr>
     <td class=\"detailLabel\">$l_private</td>
     <td class=\"detailForm\">$dis_privacy</td>
    </tr> 
    <tr>
     <td class=\"detailLabel\">$l_cat</td>
     <td class=\"detailForm\">$sel_cat</td>
    </tr>    
    <tr>
     <td class=\"detailLabel\">$l_users&nbsp;&nbsp; 
     <a href=\"javascript: return false;\" onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_add_user\"></a></td>
     <td class=\"detailForm\">$dis_sel_user</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_groups&nbsp;&nbsp;
     <a href=\"javascript: return false;\" onclick=\"window.open('$url2','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_add_group\" /></a></td>
</td>
     <td class=\"detailForm\">$dis_sel_grp</td>
    </tr>    
   </table>
   <div class=\"detailHead\">$l_carac</div>
   <table class=\"detail\">
    <tr>
     <td class=\"detailLabel\">$l_datebegin $l_format</td>
     <td class=\"detailForm\">
      <input onchange=\"changeDateEnd(this.form)\" \"type=\"text\" id=\"tf_date_begin\" name=\"tf_date_begin\" maxlength=\"10\" size=\"12\" value=\"$datebegin\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_date_begin,'agenda_index.php?action=calendar&popup=1') ;\">
       <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
     </td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_timebegin</td>
     <td class=\"detailForm\">$dis_hour_b : $dis_min_b</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_dateend $l_format</td>
     <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_date_end\" name=\"tf_date_end\" maxlength=\"10\" size=\"12\" value=\"$dateend\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_date_end,'agenda_index.php?action=calendar&popup=1');\">
       <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
     </td>       
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_timeend</td>
     <td class=\"detailForm\">$dis_hour_e : $dis_min_e</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_priority</td>
     <td class=\"detailForm\">$dis_sel_prio</td>
    </tr>      
   </table>
   <div class=\"detailHead\">$l_repeat</div>
   <table class=\"detail\">    
    <tr>    
     <td class=\"detailLabel\">$l_repeatkind</td> 
     <td class=\"detailForm\">$dis_sel_kind</td>
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_use_endrepeat</td> 
     <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_repeat_end\" name=\"tf_repeat_end\" maxlength=\"10\" size=\"12\" value=\"$repeat_end\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_repeat_end,'agenda_index.php?action=calendar&popup=1');\">
       <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>     
     </td>
    </tr>
    <tr>  
     <td class=\"detailLabel\">$l_repeatdays</td>
     <td class=\"detailForm\">$dis_repeat_day</td>
    </tr>
    $dis_repeat_update
    </table>
   <div class=\"detailHead\">$l_description</div>
   <table class=\"detail\">     
    <tr>    
     <td colspan=\"2\" class=\"detailLabel\"><textarea name=\"ta_event_description\" rows=\"6\" cols=\"72\">$desc</textarea></td> 
    </tr>     
   </table>
   <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    $hidden_repeat
  </form>
   ";

   return $block;
 }
///////////////////////////////////////////////////////////////////////////////
// Display Waiting events
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function dis_waiting_events($obm_wait) {

  return html_waiting_events($obm_wait);
}



///////////////////////////////////////////////////////////////////////////////
// Display Waiting events
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_waiting_events($obm_wait) {
  global $l_users, $l_title, $l_priority, $l_cat, $l_description,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin2,$l_timeend2,$l_repeat,$auth,$l_change_state,$l_validate;
  global $l_repeatkind,$l_repeatdays2,$l_use_endrepeat,$l_daily,$l_weekly,$set_weekstart_default,$l_wait;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_header_agenda,$l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse;
  while ($obm_wait->next_record()) {
    $current_user = $obm_wait->f("userobm_id");
    $current_lname = $obm_wait->f("userobm_lastname");
    $current_fname = $obm_wait->f("userobm_firstname");
    if($current_user != $old_user) {
      $old_user = $current_user;
      if($dis_waiting_events!="") {
	$dis_waiting_events .= "</table>";
      }
      $dis_waiting_events .= "
      <p/>
      <div class=\"detailHead\">$current_fname $current_lname :</div>
      <table class=\"detail\">
       <tr>
        <td class=\"detailText\">$l_title</td>
        <td class=\"detailText\">$l_cat</td>
        <td class=\"detailText\">$l_priority</td>
        <td class=\"detailText\">$l_repeatkind</td>
	<td class=\"detailText\">$l_repeatdays2</td>
	<td class=\"detailText\">$l_use_endrepeat</td>     
	<td class=\"detailText\">$l_datebegin</td>
	<td class=\"detailText\">$l_dateend</td>
	<td></td>
      </tr>";
    }
    $kind = ${"l_".$obm_wait->f("calendarevent_repeatkind")};
    switch ($obm_wait->f("calendarevent_priority") ) {
      case 1 : 
	$priority = $l_low;
	break;
      case 2 :
	$priority = $l_medium;
	break;
      case 3 :
	$priority = $l_high;
	break;
    }
	
    $start_week_day = strtotime($set_weekstart_default);
    $dis_repeat_days = "";
    for ($i=0; $i<7; $i++) {
      $day_num = date("w", $start_week_day);
      $day = $l_daysofweekshort[$day_num];
      if (strcmp(substr($obm_wait->f("calendarevent_repeatdays"),$i,1),"1")==0) {
	$dis_repeat_days .= "$day ";
      }
      $start_week_day = strtotime("+1 day", $start_week_day); 
    }
    
    $day_array = "";
    $day_array1 = "";
    $day_array2 = "";
    $date_begin = datetime_format($obm_wait->f("datebegin"));
    $date_end = datetime_format($obm_wait->f("dateend"));
    $date_repeat = date_format($obm_wait->f("calendarevent_endrepeat"));
    $dis_waiting_events .= "
    <tr>
     <td class=\"detailForm\">".$obm_wait->f("calendarevent_title")."</td>
     <td class=\"detailForm\">".$obm_wait->f("calendarcategory_label")."</td>
     <td class=\"detailForm\">$priority</td>
     <td class=\"detailForm\">$kind</td>
     <td class=\"detailForm\">$dis_repeat_days</td>
     <td class=\"detailForm\">$date_repeat</td>
     <td class=\"detailForm\">$date_begin</td>
     <td class=\"detailForm\">$date_end</td>
     <td class=\"detailForm\">
      <form method=\"get\" action=\"agenda_index.php\">
       <input type=\"hidden\" name=\"action\" value=\"decision\" />      
       <input type=\"hidden\" name=\"param_event\" value=\"".$obm_wait->f("calendarevent_id")."\" />
       <input type=\"hidden\" name=\"param_user\" value=\"$current_user\" />
       <input type=\"radio\" name=\"rd_decision_event\" value=\"A\"  onclick=\"this.form.submit()\" />$l_accept
       <input type=\"radio\" name=\"rd_decision_event\" value=\"W\" checked=\"checked\" onclick=\"this.form.submit()\" />$l_wait
       <input type=\"radio\" name=\"rd_decision_event\" value=\"R\" onclick=\"this.form.submit()\" />$l_refuse
      </form>
     </td>
    </tr>";
  }
  
  // --- HTML Template --------------------------------------------------------
  $block = "
    $dis_waiting_events
   </table>
  ";

  return $block;
}
 
 
///////////////////////////////////////////////////////////////////////////////
// Display Calendar Consult                                                  //
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_calendar_consult($obm_q_event,$obm_q_cust,$obm_q_grp) {
  global $display;
  global $set_theme,$set_weekstart_default;
  // -- Labels
  global $l_users, $l_title, $l_priority, $l_private, $l_cat, $l_description,$l_high,$l_low,$l_medium,$l_datebegin;
  global $l_dateend,$l_timebegin,$l_timeend,$l_insert,$l_update,$l_repeat,$auth,$l_change_state,$l_groups;
  global $l_repeatkind,$l_repeatdays,$l_use_endrepeat,$l_daily,$l_weekly,$set_weekstart_default,$l_wait,$l_none;
  global $l_monthlybydate,$l_monthlybyday,$l_yearly, $l_carac,$l_header_agenda,$l_format,$l_daysofweekshort,$l_update,$l_accept,$l_refuse;
  
  $title = $obm_q_event->f("calendarevent_title");
  $category = $obm_q_event->f("calendarcategory_label");
  $priority = $obm_q_event->f("calendarevent_priority"); 
  $description = $obm_q_event->f("calendarevent_description");
  $datebegin = datetime_format($obm_q_event->f("datebegin"));
  $hdbegin = date("YmdHis",$obm_q_event->f("datebegin"));
  $hdend = date("YmdHis",$obm_q_event->f("dateend"));
  $dateend =  datetime_format($obm_q_event->f("dateend"));
  $privacy = $obm_q_event->f("calendarevent_privacy");
  $occupied_day = $obm_q_event->f("calendarevent_occupied_day");
  $kind = ${"l_".$obm_q_event->f("calendarevent_repeatkind")};
  $repeat_days =$obm_q_event->f("calendarevent_repeatdays"); 
  if($kind != "none") {
    $repeat_end = date_format($obm_q_event->f("calendarevent_endrepeat"));
  }
  $id = $obm_q_event->f("calendarevent_id");

  switch ($priority ) {
    case 1 : 
      $priority = $l_low;
      break;
    case 2 :
      $priority = $l_medium;
      break;
    case 3 :
      $priority = $l_high;
      break;
  }
    
  $start_week_day = strtotime($set_weekstart_default);
  for ($i=0; $i<7; $i++) {
    $day_num = date("w", $start_week_day);
    $day = $l_daysofweekshort[$day_num];
    if (strcmp(substr($repeat_days,$i,1),"1")==0) {
      $dis_repeat_days .= "$day ";
    }
    $start_week_day = strtotime("+1 day", $start_week_day); 
  }
    
  while ($obm_q_cust->next_record()) {
    if ($auth->auth["uid"] == $obm_q_cust->f("userobm_id")) {
      ${$obm_q_cust->f("calendarsegment_state")} = "checked=\"checked\"";
      $dis_decision_radio = "  
      </table>
      <div class=\"detailHead\">$l_change_state</div>
      <table class=\"detail\">  
      <tr><td colspan=\"2\" class=\"detailText\">
	<input type=\"hidden\" name=\"action\" value=\"update_decision\" />
	<input type=\"hidden\" name=\"param_event\" value=\"$id\" />
	<input type=\"hidden\" name=\"hd_date_begin\" value=\"$hdbegin\" />
	<input type=\"hidden\" name=\"hd_date_end\" value=\"$hdend\" />
	<input type=\"radio\" $A name=\"rd_decision_event\" value=\"A\" onclick=\"this.form.submit()\" />$l_accept
	<input $W type=\"radio\" name=\"rd_decision_event\" value=\"W\" onclick=\"this.form.submit()\" />$l_wait
	<input $R type=\"radio\" name=\"rd_decision_event\" value=\"R\" onclick=\"this.form.submit()\" />$l_refuse
      </td></tr>";
    }
    $dis_user_list .= $obm_q_cust->f("userobm_lastname")." ". $obm_q_cust->f("userobm_firstname")."(".$obm_q_cust->f("calendarsegment_state").")<br />";
  }
  while ($obm_q_grp->next_record()) {
    $dis_grp_list .= $obm_q_grp->f("group_name")."<br />";
  }

  $display["title"] = "<div class=\"title\">$l_header_agenda : $title</div>";
  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_mod_decision\">
  <div class=\"detailHead\">$l_header_agenda</div>
   <table class=\"detail\">
    <tr>
     <td class=\"detailLabel\">$l_title</td>
     <td class=\"detailText\">$title</td>
    </tr>  
    <tr>
     <td class=\"detailLabel\">$l_private</td>
     <td class=\"detailText\">$privacy</td>
    </tr> 
    <tr>
     <td class=\"detailLabel\">$l_cat</td>
     <td class=\"detailText\">$category</td>
    </tr>    
    <tr>
     <td class=\"detailLabel\">$l_users</td>
     <td class=\"detailText\">$dis_user_list</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_groups</td>
     <td class=\"detailText\">$dis_grp_list</td>
    </tr>
   </table>
  <div class=\"detailHead\">$l_carac</div>
   <table class=\"detail\">   
    <tr>
     <td class=\"detailLabel\">$l_datebegin</td>
     <td class=\"detailText\">$datebegin</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_dateend</td>
     <td class=\"detailText\">$dateend</td>       
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_priority</td>
     <td class=\"detailText\">$priority</td>
    </tr>      
   </table>
   <div class=\"detailHead\">$l_repeat</div>
   <table class=\"detail\">   
    <tr>    
     <td class=\"detailLabel\">$l_repeatkind</td> 
     <td class=\"detailText\">$kind</td>
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_use_endrepeat</td> 
     <td class=\"detailText\">$repeat_end</td>
    </tr>
    <tr>  
     <td class=\"detailLabel\">$l_repeatdays</td>
     <td class=\"detailText\">$dis_repeat_days</td>
    </tr>
   </table>
   <div class=\"detailHead\">$l_description</div>
   <table class=\"detail\">   
    <tr>    
     <td colspan=\"2\" class=\"detailText\">$description</td>
    </tr>   
    $dis_decision_radio
   </table>
  </form>
  ";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Conflicts
// Parameters:
//   - $agenda:
//   - $conflict:
//   - $event_id: 
//   - $force: 
///////////////////////////////////////////////////////////////////////////////
function html_dis_conflict($agenda,$conflict,$event_id='',$force) {
  global $set_theme,$set_weekstart_default,$auth;
  // -- Labels
  global $l_title, $l_conflicts, $l_datebegin,$l_dateend, $l_user;
  global $l_force_insert,$l_refuse_insert,$l_cancel_insert,$l_validate;

  $user_id = $agenda["user_id"];
  if ($force == 1) { 
    $dis_form_begin = "
    <form name=\"f_conflict\" method=\"post\">
     <input type=\"hidden\" name=\"param_event\" value=\"$event_id\" />
     <input type=\"hidden\" name=\"action\" value=\"decision\" />
     <input type=\"hidden\" name=\"param_user\" value=\"$user_id\" />
     <input type=\"hidden\" name=\"cb_force\" value=\"1\" />
     <input type=\"hidden\" name=\"rd_decision_event\" value=\"\" />";
    $dis_form_button = "
    <input type=\"submit\" value=\"$l_force_insert\" onclick=\"this.form.rd_decision_event.value='A';\"/>
    <input type=\"submit\" value=\"$l_refuse_insert\" onclick=\"this.form.rd_decision_event.value='R';\"/> 
    <input type=\"submit\" value=\"$l_cancel_insert\" onclick=\"this.form.rd_decision_event.value='W';\"/>
    ";
    $dis_form_end = " </form>";
  }
  foreach ($conflict as $data) {
    $id = $data["event_id"];
    $title = $data["event_title"];
    $user_id = $data["user_id"];
    $user_name = $data["user_name"];
    $date_begin = $data["date_begin"];
    $date_end = $data["date_end"];
    $day_array = "";
    $day_array2 = "";
    $date_b = datetime_format($data["date_begin"]);
    $date_e = datetime_format($data["date_end"]);
    $dis_conflict .= "
      <tr>
        <td class=\"detailForm\">$title</td>
        <td class=\"detailForm\">$date_b</td>
        <td class=\"detailForm\">$date_e</td>
        <td class=\"detailForm\">$user_name</td>";

     $dis_conflict .= "</tr>";
  }
  $block= "
  $dis_form_begin
  <div class=\"detailHead\">$l_conflicts</div>
  <table class=\"detail\">
   <tr>
    <td class=\"detailText\">$l_title</td>
    <td class=\"detailText\">$l_datebegin</td>
    <td class=\"detailText\">$l_dateend</td>
    <td class=\"detailText\">$l_user</td>    
   </tr>
   $dis_conflict
  </table>
  <div class=\"detailButton\">
   <p class=\"detailButtons\">$dis_form_button</p>
  </div>

  $dis_form_end
  ";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Delete Options
// Parameters:
//   - $agenda:
///////////////////////////////////////////////////////////////////////////////
function html_dis_delete($agenda) {
  global $set_theme;
  // -- Labels
  global $ico_validate, $ico_cancel,$ico_confirm_mail;
  global $l_confirm_delete,$l_confirm,$l_cancel,$l_confirm_mail
;
  
  $event_id = $agenda["id"];
  $getdate = $agenda["date"];
  $block = "
  <div class=\"detail\">
   <div class=\"messageWarning\">$l_confirm_delete</div>
   <div class=\"agendaDelete\">
    <a href=\"agenda_index.php?action=delete&param_event=$event_id&cb_mail=1\"><img src=\"/images/$set_theme/$ico_confirm_mail\" alt=\"[ValidateMail]\"/></a>
     $l_confirm_mail
    <a href=\"agenda_index.php?action=delete&param_event=$event_id\"><img src=\"/images/$set_theme/$ico_validate\" alt=\"[Validate]\"  width=\"16\"/></a>
     $l_confirm
    <a href=\"agenda_index.php?action=detailconsult&param_event=$event_id&param_date=$getdate\"><img src=\"/images/$set_theme/$ico_cancel\" alt=\"[Cancel]\" width=\"16\" /></a>
     $l_cancel
   </div>
  </div>
  ";
  return $block;
}
///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $user_q :
///////////////////////////////////////////////////////////////////////////////

function dis_right_admin($agenda) {
  global $display,$auth;
  if(isset($agenda["user_id"])) {
    $user_id = $agenda["user_id"];
  }else {
    $user_id = $auth->auth["uid"];
  }
  $writable_user = run_query_userobm_label_writable(); 
  $user_obm = run_query_userobm_right($user_id);
  $display["features"] = html_userwritable_bar($writable_user,$user_id);
  return html_dis_right_admin($user_obm,$user_id);
}
///////////////////////////////////////////////////////////////////////////////
// User selection for agenda management admin panel
// Parameters:
//   - $writable_user : Array of user on who you can manage the calandar
///////////////////////////////////////////////////////////////////////////////
function html_userwritable_bar($writable_user,$user_id) {
  global $l_user_right,$l_validate;
// User Selection
  if (count($writable_user) > 0) {  
    $dis_sel_user .= "<select name=\"param_user\">";
    foreach ($writable_user as $user) {
      $u_id = $user["id"];
      $u_lname =  $user["lastname"];
      $u_fname = $user["firstname"];
      $dis_sel_user .= "\n<option value=\"$u_id\"";
      if ($u_id == $user_id ) {
	$tag_user = " selected=\"selected\"";
      }
      $dis_sel_user .= " $tag_user>$u_lname $u_fname</option>";
      $tag_user = "";
    }
    $dis_sel_user .= "</select>
    <br />
    <input type=\"hidden\" name=\"action\" value=\"rights_admin\" />
    $dis_duration
    <input type=\"submit\" value=\"$l_validate\" />
    ";

    $block = "<div class=\"agendaBar\">
      <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
       <tr>
        <td class=\"agendaHead2\">$l_user_right</td>
       </tr>
       <tr>
        <td class=\"agendaVide\" align=\"left\">
 	 <form action=\"agenda_index.php\" method=\"get\" $check_sel_user>
	  $dis_sel_user
	 </form>
	</td>
       </tr>              
      </table>
     </div>";

  }
  return $block;
} 



  
///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $user_q :
///////////////////////////////////////////////////////////////////////////////
function html_dis_right_admin($user_q,$user_id) {
  global $l_authorize_list, $l_denie_list, $l_read_permission, $l_write_permission;
  global $l_everyone,  $l_list,$l_authorize,$l_deny;
  
  while ($user_q->next_record()) {
    $u_id = $user_q->f("userobm_id");
    $lname = $user_q->f("userobm_lastname");
    $fname = $user_q->f("userobm_firstname");

    if ($user_q->f("calendarright_write") == NULL) {
      $sel_read_no .= "<option value=\"$u_id\">$lname $fname</option>";
      $sel_write_no .= "<option value=\"$u_id\">$lname $fname</option>";
    } else {
      if ($user_q->f("calendarright_write") == 1) {
	$sel_write_ok .= "<option value=\"$u_id\">$lname $fname</option>";
      }
      if ($user_q->f("calendarright_read") == 1) {
	$sel_read_ok .= "<option value=\"$u_id\">$lname $fname</option>";
      }
      if ($user_q->f("calendarright_read") == 0) { 
	$sel_read_no .= "<option value=\"$u_id\">$lname $fname</option>";
      }
      if ($user_q->f("calendarright_write") == 0) {
	$sel_write_no .= "<option value=\"$u_id\">$lname $fname</option>";
      }
    }
  }
  $block = "
  <table class=\"agendaRight\" >
   <tr>
    <td colspan=\"3\" class=\"detailLabel\">
     &nbsp;
    </td>
   </tr>
   <tr>
    <td colspan=\"3\" class=\"detailHead\">$l_read_permission</td>
   </tr>
   <tr>
    <td colspan=\"3\" class=\"detailLabel\">
     &nbsp;
    </td>
   </tr>
   <tr>
    <td class=\"detailHead\">
     $l_denie_list
    </td>
    <td class=\"detailHead\">
    </td>
    <td class=\"detailHead\">
     $l_authorize_list
    </td>
   </tr>
   <tr>
    <form method=\"post\" action=\"agenda_index.php\">    
     <td class=\"detailHead\">
      <input type=\"hidden\" name=\"param_user\" value=\"$user_id\" />
      <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
      <select name=\"sel_accept_read[]\" multiple=\"multiple\" size=\"15\">
        $sel_read_no
      </select>
     </td>
     <td class=\"detailHead\">
      <input type=\"submit\" value=\"$l_deny\" /><br />
     </form>
     <form method=\"post\" action=\"agenda_index.php\">        
      <br /><input type=\"submit\" value=\"$l_authorize\" />
     </td>
     <td class=\"detailHead\">
      <input type=\"hidden\" name=\"param_user\" value=\"$user_id\" />
      <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
      <select name=\"sel_deny_read[]\" multiple=\"multiple\" size=\"15\">
        $sel_read_ok  
      </select>
    </td>  
    </form>
   </tr>
   <tr>
    <td colspan=\"3\" class=\"detailLabel\">
     &nbsp;
    </td>
   </tr>   
   <tr>
    <td colspan=\"3\" class=\"detailHead\">$l_write_permission</td>
   </tr>
   <tr>
   <tr>
    <td colspan=\"3\" class=\"detailLabel\">
     &nbsp;
    </td>
   </tr>    
   <tr>
    <td class=\"detailHead\">
     $l_denie_list
    </td>
    <td class=\"detailHead\">
    </td>
    <td class=\"detailHead\">
     $l_authorize_list
    </td>
   </tr>
   <tr>
    <form method=\"post\" action=\"agenda_index.php\">
     <td class=\"detailHead\">
      <input type=\"hidden\" name=\"param_user\" value=\"$user_id\" />
      <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
      <select name=\"sel_accept_write[]\" multiple=\"multiple\" size=\"15\">
        $sel_write_no
      </select>
     </td>
     <td class=\"detailHead\">
      <input size=\"30\" type=\"submit\" value=\"$l_deny\" /><br />
     </form>
     <form method=\"post\" action=\"agenda_index.php\">      
      <br /><input size=\"30\" type=\"submit\" value=\"$l_authorize\" />
     </td>
     <td class=\"detailHead\">
      <input type=\"hidden\" name=\"param_user\" value=\"$user_id\" />
      <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
      <select name=\"sel_deny_write[]\" multiple=\"multiple\" size=\"15\">
        $sel_write_ok
      </select>
     </td>
    </form>
   </tr>   
  </table>
  ";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the new event form
// Parameters:
//    -$agenda: agenda parameters
//    -$obm_q_events : list of event's records 
//    -$contacts_array : array of selected contacts 
//    -$groups_array : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_meeting_form($agenda, $usr_q, $p_user_array) {
  global $display, $path;
  global $set_theme,$set_weekstart_default, $set_start_time, $set_stop_time,$set_cal_interval,$ico_mini_cal;
  global $ico_add_user,$popup_height,$popup_width;
  // -- Labels
  global $l_users,$l_datebegin,$l_dateend,$l_format,$l_header_meeting,$l_event_duration,$l_meeting_perform; 

  $time_unit = 60 / $set_cal_interval;
  $datebegin = substr($agenda["date"],0,8);
  ereg ("([0-9]{4})([0-9]{2})([0-9]{2})",$datebegin , $day_array);
  $datebegin = $day_array[1]."-".$day_array[2]."-".$day_array[3];
  $dis_button = "
     <input type=\"hidden\" name=\"param_event\" id=\"param_event\" value=\"$id\" />
     <input type=\"hidden\" name=\"action\" id=\"action\" value=\"perform_meeting\" />
     <input type=\"submit\" value=\"$l_meeting_perform\" /> 
     ";

  // userobm select
  $dis_sel_user = "<select name=\"sel_user_id[]\" size=\"12\" multiple=\"multiple\">";
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
  
  $dis_hour_d = "<select name=\"sel_time_duration\">";
  for ($i=0;$i<24;$i++) {
    $current_hour = substr("0$i",-2,2); 
    if($current_hour == $hourbegin){
      $dis_hour_d .= "<option value=\"$current_hour\" selected=\"selected\">$current_hour</option>";
    }
    else {
      $dis_hour_d .= "<option value=\"$current_hour\">$current_hour</option>";
    }
  }
  $dis_hour_d .= "</select>";  
 
  $dis_min_d = "<select name=\"sel_min_duration\">";
  for ($i=4;$i>=1;$i--) {
    $current_min = substr("0".(60 - (15*$i)),-2);
    if($current_min  == $mindur){
      $dis_min_d .= "<option value=\"$current_min\" selected=\"selected\">$current_min</option>";
    }
    else {
      $dis_min_d .= "<option value=\"$current_min\">$current_min</option>";
    }
  }
  $dis_min_d .= "</select>";  
  $display["title"] = "<div class=\"title\">$l_header_meeting</div>"; 
  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_widget=forms[0].elements[0]"; 
  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_mod_calendar\"
   onsubmit=\"if (check_meeting(this)) return true; else return false;\"
   action=\"".url_prepare("agenda_index.php")."\">
   <div class=\"detailHead\">$l_header_meeting</div>
   <table class=\"detail\">
    <tr>
     <td class=\"detailLabel\">$l_users&nbsp;&nbsp;
      <a href=\"javascript: return false;\" onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_add_user\"></a>
     </td>
     <td class=\"detailForm\">$dis_sel_user</td>
    </tr>
    <tr>
     <td class=\"detailLabel\">$l_datebegin $l_format</td>
     <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_date_begin\" name=\"tf_date_begin\" maxlength=\"10\" size=\"12\" value=\"$datebegin\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_mod_calendar.tf_date_begin,'agenda_index.php?action=calendar&popup=1');\">
       <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
     </td>
    </tr>
    <tr>    
     <td class=\"detailLabel\">$l_event_duration</td>
     <td class=\"detailForm\">$dis_hour_d : $dis_min_d</td>
    </tr>
   </table>
   <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
   </div>    
  </form>
   ";
   return $block;
 }


///////////////////////////////////////////////////////////////////////////////
// Free meeting performing and display
// Parameters:
//   - $agenda         : agenda parameters
//   - $obm_q_events   : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function dis_free_interval($agenda,$obm_q,$user_q,$usr_q, $p_user_array){
  global $display;
  global $set_cal_interval,$set_weekstart_default, $set_start_time, $set_stop_time;
  $getdate = $agenda["date"];
  $start_week_day = strtotime(dateOfWeek($getdate, $set_weekstart_default));
  $end_week_time = $start_week_day + ((6 * 24) * 60 * 60);
  $start_week_time = strtotime("+$set_start_time hours",$start_week_day);
  $end_week_time = strtotime("+$set_stop_time hours",$end_week_time);  
  $calendar_user = store_users($user_q);
  store_events($obm_q, $current_events, $event_data,$start_week_time,$end_week_time);
  $duration = round($agenda["duration"]*$set_cal_interval) - 1;
  $meeting = store_meeting_cell($current_events,$duration,$calendar_user,$getdate);
  $display["detail"] = html_meeting_planning($agenda,$usr_q, $meeting,$calendar_user);
  $display["features"] = html_planning_bar($agenda,$usr_q, $p_user_array,$calendar_user);

  
}


///////////////////////////////////////////////////////////////////////////////
// Display the free cells for the week
// Parameters:
//   - $agenda         : agenda parameters
//   - $obm_q_events   : list of event's records 
//   - $contacts_array : array of selected contacts 
//   - $groups_array   : array of selected groups 
///////////////////////////////////////////////////////////////////////////////
function html_meeting_planning($agenda,$usr_q, $meeting,$calendar_user){
  global $set_theme,$ico_left_day,$ico_right_day,$ico_spacer,$sel_user_id;
  global $set_weekstart_default, $set_start_time, $set_stop_time,$set_cal_interval,$ico_new_event;
  $getdate = $agenda["date"];
  $param_duration = $agenda["duration"];
  $real_duration = $param_duration * 60;
  $unix_time = strtotime($getdate);
  $time_unit = 60 / $set_cal_interval;
  $next_week = date("Ymd", strtotime("+1 week",  $unix_time));
  $prev_week = date("Ymd", strtotime("-1 week",  $unix_time));
  $start_week_day = strtotime(dateOfWeek($getdate, $set_weekstart_default));
  $end_week_time = $start_week_day + ((6 * 24) * 60 * 60);
  $start_week_time = strtotime("+$set_start_time hours",$start_week_day);
  $end_week_time = strtotime("+$set_stop_time hours",$end_week_time);
  $start_week = localizeDate("week", $start_week_time);
  $end_week =  localizeDate("week", $end_week_time);
  $display_date = "$start_week - $end_week";
  $week_day_list = "";
  $td_width = 70;
  for($i = $start_week_time; $i < ($start_week_time + 7*(25 * 60 * 60)); $i += (25 * 60 * 60) ) {
    $this_date = date("Ymd", $i);
    $this_date_l = localizeDate("week_list", $i);
    $week_day_list .= "<td width=\"70\"  class=\"agendaCell\">$this_date_l</td>\n";
  }
  // Hour of the day
  for($i = $set_start_time; $i < $set_stop_time; $i++) {
    $hour_day_list .= 	"<tr>";
    $hour_day_list .= 	"<td height=\"15\" rowspan=\"$set_cal_interval\" class=\"agendaHour\">$i:00</td>";
    $hour_day_list .= 	"<td width=\"1\" height=\"15\"><img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" /></td>";
    for($j=0; $j<7;$j++)  {
      $current_time = strtotime("+$j days $i hours",$start_week_day);
      $current_date = date("YmdHi",$current_time);
      $end_time =  date("YmdHi", strtotime("+$j days $i hours $real_duration minutes",$start_week_day));
      $goto = "agenda_index.php?action=new&param_date_begin=$current_date&param_date_end=$end_time&p_user_meeting=1";
      switch($meeting[$current_time]) {
	case 0 : 
	  $hour_day_list .="
	  <td width=\"$td_width\" class=\"agendaPossible\" onclick=\"window.location='$goto'\"
	   onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer'\"
	   onmouseout=\"this.className='agendaPossible'\">&nbsp;</td>\n";
	  break;
	case 1 : 
	  $hour_day_list .="<td width=\"$td_width\" class=\"agendaFree\" onclick=\"window.location='$goto'\" 
	   onmouseover=\"this.className='agendaFreeHover';this.style.cursor = 'pointer'\"
	   onmouseout=\"this.className ='agendaFree'\">&nbsp;</td>\n";
	  break;
	case 2 : 
	  $hour_day_list .="<td width=\"$td_width\" class=\"agendaOccupied\">&nbsp;</td>\n";
	  break;	 
      }
    }
    $hour_day_list .=   "</tr>\n";
    for($k=1;$k<$set_cal_interval;$k++) {
      $hour_day_list .= "<tr><td height=\"15\" width=\"1\"><img src=\"/images/$set_theme/$ico_spacer\" alt=\"\" /></td>";
       for($j=0; $j<7;$j++) {	 
	 $current_time = strtotime("+$j days $i hours".($k*$time_unit)." minutes",$start_week_day);
   	 $current_date = date("YmdHi",$current_time);
      	 $end_time =  date("YmdHi", strtotime("+$j days $i hours ".(($k*$time_unit)+$real_duration)." minutes",$start_week_day));
	$goto = "agenda_index.php?action=new&param_date_begin=$current_date&param_date_end=$end_time&p_user_meeting=1";
	switch($meeting[$current_time]) {
	  case 0 : 
	    $hour_day_list .="<td width=\"$td_width\" class=\"agendaPossible\" onclick=\"window.location='$goto'\"
	    onmouseover=\"this.className='agendaPossibleHover';this.style.cursor = 'pointer'\"
	    onmouseout=\"this.className='agendaPossible'\">&nbsp;</td>\n";
 	    break;
	  case 1 : 
	    $hour_day_list .="<td width=\"$td_width\" class=\"agendaFree\" onclick=\"window.location='$goto'\"
	     onmouseover=\"this.className='agendaFreeHover';this.style.cursor = 'pointer'\"
	   onmouseout=\"this.className ='agendaFree'\">&nbsp;</td>\n";
	    break;
	  case 2 : 
	    $hour_day_list .="<td width=\"$td_width\" class=\"agendaOccupied\">&nbsp;</td>\n";
	    break;
	}
      }
    }
    $hour_day_list .=   "</tr>";
  }
  
  $block = "
<div class=\"agendaMain\">
   <table cellspacing=\"0\" cellpadding=\"0\" class=\"agendaCalMeeting\">
    <tr>
     <td>
      <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">
       <tr>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;param_date=".$prev_week."&amp;param_duration=".$param_duration)."\">
          <img src=\"/images/$set_theme/$ico_left_day\" alt=\"[Previous Week]\" />
         </a>
        </td>
        <td class=\"agendaHead\">
         $display_date
        </td>
        <td class=\"agendaHead\">
         <a href=\"".url_prepare("agenda_index.php?action=perform_meeting&amp;param_date=".$next_week."&amp;param_duration=".$param_duration)."\">
          <img src=\"/images/$set_theme/$ico_right_day\" alt=\"[Next Week]\" />
         </a>
        </td>
       </tr>
      </table>
     </td>
    </tr>
    <tr>
     <td>
      <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">      			
       <tr>
        <td>
         <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">
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
  </div>
";
return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the agenda administration index
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $category_q = run_query_agendacategory();
  return html_agenda_category_form($category_q);
}


///////////////////////////////////////////////////////////////////////////////
// Company Category section
// Parameters:
//   - $category_q : Category list database object
///////////////////////////////////////////////////////////////////////////////
function html_agenda_category_form($category_q) {
  global $l_category_manage,$l_category_exist;
  global $l_category_checkdelete,$l_category_update,$l_category_new,$l_category_insert;

  $sel_category = "<select name=\"sel_category\" size=\"4\" >";
  while ($category_q->next_record()) {
    $label = $category_q->f("calendarcategory_label");
    $id = $category_q->f("calendarcategory_id");
    $sel_category .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_category .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_category_manage
      </td>
    </tr><tr>
      <td>

<!-- Category Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_admin_category_delete\" method=\"post\"
              action=\"" . url_prepare("agenda_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_category_exist</td>
              <td class=\"adminLabel\">$sel_category</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"category_checklink\" />
              <input type=\"submit\" name=\"sub_category\" value=\"$l_category_checkdelete\" onclick=\"if (check_category_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_admin_category_update\" method=\"get\"
              action=\"" . url_prepare("agenda_index.php") . "\">
            <input type=\"hidden\" name=\"sel_category\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"category_update\" />
            <input type=\"text\" name=\"tf_category_upd\" />
            <input type=\"submit\" name=\"sub_category\" value=\"$l_category_update\"
              onclick=\"if (check_category_upd(this.form,document.form_admin_category_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category Section -->

        <form name=\"form_admin_category_new\" method=\"get\"
          action=\"" . url_prepare("agenda_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_category_new :
            <input name=\"tf_category_new\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"category_insert\" />
            <input type=\"submit\" name=\"sub_category\" value=\"$l_category_insert\"
              onclick=\"if (check_category_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
";
return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Return the category delete display form (button)
// Parameters:
//   - $label : category label
// Retuns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function dis_category_delete_form($agenda) {
  global $l_category_delete;

  $id = $agenda["category_id"];
  $r = "<form name=\"form_category_delete\"
          method=post action=\"" . url_prepare("agenda_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"category_delete\">
        <input type=\"hidden\" name=\"sel_category\" value=\"$id\">
        <input type=\"submit\" name=\"sub_category\" value=\"$l_category_delete\">
        </form>";

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the category links
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_category_links($agenda) {
  global $l_category_link_agenda, $l_category_link_agenda_no;
  global $l_category_can_delete, $l_category_cant_delete;
  global $l_back,$l_link_events,$display;

  $label = get_category_label($agenda["category_id"]);
  $obm_q = run_query_category_links($agenda["category_id"]);
  $del_form = dis_category_delete_form($agenda["category_id"]);
  
  $obm_q->next_record();
  if ($obm_q->f("numlink") > 0) {
    $display["msg"] = display_warn_msg($l_category_cant_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : ".$obm_q->f("numlink")." $l_link_events</td>
    </tr>
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("agenda_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
	
	</p>
    </div>
";
  } else {
    $display["msg"] = display_ok_msg($l_category_can_delete);
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
      $l_category_link_agenda_no '$label' </td>
    </tr>
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">
      $del_form
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("agenda_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
	</p>
    </div>
";

  }
  return "$dis_link";
}
///////////////////////////////////////////////////////////////////////////////
// Perform the export to the vCalendar format                                //
// Parameters:
//   - $label : category label
///////////////////////////////////////////////////////////////////////////////
function dis_export_handle($agenda) {
  global $obm_version,$set_weekstart_default;
  $obm_event = run_query_get_vevent_norepeat($agenda);
  header("Content-Type: text/x-vCalendar");
  header("Content-Disposition: inline; filename=ObmCalendar.ics");
  echo "
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//OBM/OBM V$obm_version Calendar//EN
METHOD:PUBLISH";
  while($obm_event->next_record()) {
    $id = $obm_event->f("calendarevent_id");
    $title = "SUMMARY:" . $obm_event->f("calendarevent_title");
    $title = wordwrap($title,74,"\n ",1);
    $priority = $obm_event->f("calendarevent_priority");
    $category = strtoupper($obm_event->f("calendarcategory_label")); 
    $date_ts = $obm_event->f("calendarevent_timeupdate");
    $date_ts = gmdate("Ymd\THis\Z",mktime(substr($date_ts,8,2),substr($date_ts,10,2),0,substr($date_ts,4,2),substr($date_ts,6,2),substr($date_ts,0,4)));
    if($obm_event->f("calendarevent_description") != "") {
      $description = preg_replace("/\\r?\\n/","\\n",$obm_event->f("calendarevent_description"));
      $description = "DESCRIPTION:" . $description;
      $description = "\n" . wordwrap($description,74,"\n ",1);
    }
    $date_b = $obm_event->f("calendarsegment_date");
    $date_b = gmdate("Ymd\THis\Z",$date_b);
    $obm_event->next_record();
    $date_e = $obm_event->f("calendarsegment_date");
    $date_e = gmdate("Ymd\THis\Z",$date_e);
    switch($priority) {
      case 1 : $priority = 3;
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $obm_event->f("calendarevent_privacy");
    switch($private) {
      case 0 : $private = "PUBLIC";
	break;
      case 1 : $private = "PRIVATE";
	break;
    }        
    echo "
BEGIN:VEVENT
UID:$id    
$title
CLASS:$private
CATEGORIES:$category
STATUS:CONFIRMED$description
DTSTART:$date_b
DTSTAMP:$date_ts
DTEND:$date_e
END:VEVENT";
  }
  $obm_event = run_query_get_vevent_repeat($agenda);
  while($obm_event->next_record()) {
    $id = $obm_event->f("calendarevent_id");
    $title = $obm_event->f("calendarevent_title");
    $priority = $obm_event->f("calendarevent_priority"); 
    $category = strtoupper($obm_event->f("calendarcategory_label")); 
    if($obm_event->f("calendarevent_description") != "") {
      $description = "\nDESCRIPTION:".$obm_event->f("calendarevent_description");
    } 
    $date_b = $obm_event->f("calendarsegment_date");
    $date_b = date("Ymd",$date_b)."T".date("His",$date_b);
    $start_date = $date_b;
    $obm_event->next_record();
    $date_e = $obm_event->f("calendarsegment_date");
    $date_e = date("Ymd",$date_e)."T".date("His",$date_e);
    $kind = $obm_event->f("calendarevent_repeatkind");
    $end = substr($obm_event->f("calendarevent_endrepeat"),0,8)."T".substr($obm_event->f("calendarevent_endrepeat"),8,4)."00";
    $repeat_days = $obm_event->f("calendarevent_repeatdays");
    switch($priority) {
      case 1 : $priority = 3;
	
	break;
      case 3 : $priority = 1;
	break;
    }
    $private = $obm_event->f("calendarevent_privacy");
    switch($private) {
      case 0 : $private = "PUBLIC";
	break;
      case 1 : $private = "PRIVATE";
	break;
    }   
    if($kind == "daily") {
      $repeat = "FREQ=DAILY;UNTIL=$end;INTERVAL=1";
    }
    elseif($kind == "weekly") {
      $l_day_repeat = array("SU","MO","TU","WE","TH","FR","SA");
      $start_week_day = strtotime($set_weekstart_default);
      for ($i=0; $i<7; $i++) {
      	$day_num = date("w", $start_week_day);
      	$day = $l_day_repeat[$day_num];
	if (strcmp(substr($repeat_days,$i,1),"1")==0) {
	  if($i!=0) {
	    $dis_repeat_days .= ",";
	  }
	  $dis_repeat_days .= "$day";
	}
       	$start_week_day = strtotime("+1 day", $start_week_day); 
      } 
      $repeat = "FREQ=WEEKLY;UNTIL=$end;INTERVAL=1;BYDAY=$dis_repeat_days";
    }
    elseif($kind == "monthlybydate") {
      $day = date("d",$start_date);
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=1;BYDAY=$day"; 
    }
    elseif($kind == "monthlybyday") {
      $start_week_day = date("w",$start_date);
      $daypos = ceil(substr($start_date,6,2)/7);
      $day_num = date("w",strtotime("+ $start_week_day days",$start_date,0,8));
      $day = $l_day_repeat[$day_num];
      $repeat = "FREQ=MONTHLY;UNTIL=$end;INTERVAL=1;BYDAY=$daypos$day";
    }
    elseif($kind == "yearly") {
      $monthpos = date("m",$start_date);
      $repeat = "FREQ=YEARLY;UNTIL=$end;INTERVAL=1;BYMONTH=$monthpos";
    }
    echo "
BEGIN:VEVENT
UID:$id    
SUMMARY:$title
CLASS:$private
CATEGORIES:$category
STATUS:CONFIRMED$description
DTSTART:$date_b
DTEND:$date_e
RRULE:$repeat
END:VEVENT";
  }
  echo "
END:VCALENDAR";

}

