<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : agenda_functions.inc                                        //
//     - Desc  : Agenda Php date manipulation functions File                 //
// 2001-06-28  - Francois Bloque                                             //
///////////////////////////////////////////////////////////////////////////////
// Last update 2001-07-13                                               //
///////////////////////////////////////////////////////////////////////////////


function weekday_name ($num_day) {
 
  global $l_monday,$l_tuesday,$l_wednesday,$l_thursday,$l_friday,$l_saturday,$l_sunday,$l_unknown; 
  
  switch($num_day) {
    case 1 : $day_name=$l_monday;    break; 
    case 2 : $day_name=$l_tuesday;   break;
    case 3 : $day_name=$l_wednesday; break;   
    case 4 : $day_name=$l_thursday;  break;
    case 5 : $day_name=$l_friday;    break;
    case 6 : $day_name=$l_saturday;  break;
    case 0 : $day_name=$l_sunday;    break;
    default : $day_name=$l_unknown;  break;   
  }
  return $day_name;
}


function weekday_short_name ($num_day) {

  global $l_short_monday,$l_short_tuesday,$l_short_wednesday,$l_short_thursday,$l_short_friday,$l_short_saturday,$l_short_sunday,$l_unknown; 
 
  switch($num_day) {
    case 1 : $day_name=$l_short_monday;    break;
    case 2 : $day_name=$l_short_tuesday;   break;
    case 3 : $day_name=$l_short_wednesday; break;   
    case 4 : $day_name=$l_short_thursday;  break;
    case 5 : $day_name=$l_short_friday;    break;
    case 6 : $day_name=$l_short_saturday;  break;
    case 0 : $day_name=$l_short_sunday;    break;
    default : $day_name=$l_unknown;        break;
  }
  return $day_name;
}



function number_days_month ($num_month) {

  switch($num_month) {
    case 1 : $number=31;  break;
    case 2 : $number=29;  break;
    case 3 : $number=31;  break;
    case 4 : $number=30;  break;
    case 5 : $number=31;  break;
    case 6 : $number=30;  break;
    case 7 : $number=31;  break;
    case 8 : $number=31;  break;
    case 9 : $number=30;  break; 
    case 10 : $number=31; break;
    case 11 : $number=30; break;
    case 12 : $number=31; break;
    default : $number=0;  break;    
  }

  return $number;
}




function add_to_date ($date,$add_year,$add_month,$add_day,$add_hour,$add_minute,$add_second) {
  
  $year=substr($date,0,4);
  $month=substr($date,4,2);
  $day=substr($date,6,2);
  $hour=substr($date,8,2);
  $minute=substr($date,10,2);
  $second=substr($date,12,2);

  return date("YmdHis",mktime($hour+$add_hour,$minute+$add_minute,$second+$add_second,$month+$add_month,$day+$add_day,$year+$add_year));
}



/////////////////////////////////////////////////////////////////////////////////////////////////////////
// Return the arrat if all repetition dates of the event 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
// Arguments : 
// ----------
//   $p_time : date from which the repetition dates are computed (in millisec.) 
//   $p_repeatkind : kind of repeatititon "daily", "weekly", "monthlybydate", "monthlybyday", or "yearly"
//   $p_repeat_interval :
//   $p_repeatdays : 
//   $p_end_repeat : date of end of repeat (in millisec.)
// Return :
// --------
//   the array of all computed dates (in millisec.
//////////////////////////////////////////////////////////////////////////////////////////////////////////
function get_event_repetition_dates($p_date,$p_repeatkind,$p_repeat_interval,$p_repeatdays,$p_end_repeat) {
  
  //------------------------------------------
  $current_year=date("Y");
  $current_month=date("m");
  $current_day=date("d");
  
  $year=substr($p_date,0,4);
  $month=substr($p_date,4,2);
  $day=substr($p_date,6,2); 
  $hour=substr($p_date,8,2); 
  $minute=substr($p_date,10,2);
  /*
  $year=date("Y",$p_time); 
  $month=date("m",$p_time); 
  $day=date("d",$p_time); 
  $hour=date("H",$p_time); 
  $minute=date("i",$p_time);
  */
  $endrepeat_year=substr($p_end_repeat,0,4);
  $endrepeat_month=substr($p_end_repeat,4,2); 
  $endrepeat_day=substr($p_end_repeat,6,2);
  
  /*$endrepeat_year=date("Y",$p_end_repeat);
   $endrepeat_month=date("m",$p_end_repeat);
   $endrepeat_day=date("d",$p_end_repeat);
  */

  //------------------------------------------
  $interval=($p_repeat_interval ? $p_repeat_interval : 1);
  $time_end_repeat=($p_end_repeat ? mktime(0,0,0,$endrepeat_month,$endrepeat_day+1,$endrepeat_year) : mktime(0,0,0,$current_month,$current_day+1,$current_year+1));
  $time=mktime($hour,$minute,0,$month,$day,$year);
 
    if ($p_repeatkind=="daily") {
    //////////////////////////////
    $var_time=$time + (86400 * $interval);
    while ($var_time <= $time_end_repeat )  {
      $event_dates[]=date("YmdHis",$var_time);
      $var_time += 86400 * $interval;
    }  

  } else if ($p_repeatkind=="weekly") {
    ///////////////////////////
    $var_time=$time - date("w",$time) * 86400 + 86400 * 7 * $interval;
    while ($var_time <= $time_end_repeat ) {
      for ($i=0;($i<7) && (($var_time + $i * 86400) <= $time_end_repeat ); $i++) { 
	$day_char=substr($p_repeatdays,$i,1);
	if (strcmp($day_char,"1")==0) {
	  $last=$i;
	  $event_dates[]=date("YmdHis",$var_time + $i * 86400);
	}
      }
      $var_time += $last * 86400 ;
      $var_time = $var_time - date("w",$var_time) * 86400 + 86400 * 7 * $interval; 
    }  
    
  
  } else if ($p_repeatkind=="monthlybydate") {
   /////////////////////////
   $var_month = $month + $interval;
   $var_time = mktime($hour,$minute,0,$var_month,$day,$year);
   while ($var_time <= $time_end_repeat) {
     $event_dates[]=date("YmdHis",$var_time);
     $var_month += $interval;
     $var_time = mktime($hour,$minute,0,$var_month,$day,$year);
   }

  } else if ($p_repeatkind=="monthlybyday") {
   /////////////////////////
   $num_week = floor(date("d",$time)/7);
   $var_month = $month + $interval;
   // days of the week : 
   $dow = date("w",$time);
   $dow_dest = date("w",mktime(0,0,0,$var_month,1,$year));

   $jump=$dow-$dow_dest;
   if ($jump<0) $jump += 7;
   $var_day = $week * 7 + $jump + 1;
   $var_time=mktime($hour,$minute,0,$var_month,$var_day,$year);
   while ($var_time <= $time_end_repeat) {
     $event_dates[]=date("YmdHis",$var_time);
     $var_month += $interval;
     $dow_dest = date("w",mktime(0,0,0,$var_month,1,$year));
     $jump=$dow-$dow_dest;
     if ($jump<0) $jump += 7;
     $var_day = $week * 7 + $jump + 1;
     $var_time=mktime($hour,$minute,0,$var_month,$var_day,$year);	    
   }  

  } else if ($p_repeatkind=="yearly") {  
   /////////////////////////
   $var_year = $year + $interval;
   $var_time = mktime($hour,$minute,0,$month,$day,$var_year);
   while ($var_time <= $time_end_repeat) {
     $event_dates[]=date("YmdHis",$var_time);
     $var_year += $interval;
     $var_time = mktime($hour,$minute,0,$month,$day,$var_year);
   }
  }

  
  return $event_dates;
  
  /*if (count($event_dates) > 0) {
     for ($i=0;$i<count($event_dates);$i++) {
       echo "--->".$event_dates[$i]."<br>";
     }
     return true;
  }  else {
     echo "aucun resultat";
    r eturn false; 
    } */
    
}



?>
