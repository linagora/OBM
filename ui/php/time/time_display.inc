<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_display.php                                             //
//     - Desc : Time Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["date_task"] = $l_date_task;
$fieldnames["task_label"] = $l_tasklabel;
$fieldnames["task_deal_label"] = $l_deallabel;
$fieldnames["task_company_name"] = $l_company;
$fieldnames["task_length"] = $l_length;
$fieldnames["task_totallength"] = $l_total_length;
$fieldnames["task_totalbefore"] = $l_total_before;
$fieldnames["task_totalperiod"] = $l_total_period;
$fieldnames["tasktype_label"] = $l_tasktype;
$fieldnames["task_id"] = $l_update;
$fieldnames["total_length"] = $l_total;
$fieldnames["day_in_month"] = $l_day;
// Calculated fields
$fieldnames["contact_company_name"] = $l_company;


///////////////////////////////////////////////////////////////////////////////
// HTML Display Array content
// Parameters:
//   - $array : any array
///////////////////////////////////////////////////////////////////////////////
function debug_array($a) {
  global $_POST;

  echo "<br>BEGIN----- debug_array ------<br>";
  foreach($a as $key => $val) {
	  if (is_null($val))
		echo "clé $key val -- NULL -- <br>";
	  else if (is_array($val)) {
      echo "clé $key val ";
	  print_r($val);
      echo " <br>";
	  }
    else
      echo "clé $key val --$val-- <br>";
  }
  echo "_POST<br>";
  foreach($_POST as $key => $val) {
	  if (is_array($val)) {
      echo "clé $key val ";
	  print_r($val);
      echo " <br>";
	  }
	  else
        echo "clé $key val --$val-- <br>";
  }

  echo "----- debug_array ------END<br>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Changing Time Links                                              //
// Parameters:
//   - $time : array, with all pairs of value set by forms
//         with $time["date"] = current date 
//   - $type : "week", "month", "all"
//     default : all
///////////////////////////////////////////////////////////////////////////////
function dis_time_links (&$time, $type="all") {
	global $cdg_param; 
  global $l_week, $l_week_prev,$l_week_next, $l_month_prev,$l_month_next;
  global $hum_date, $l_month2;
  // WEEK
  if ($type == "week") {

  //get the current start and end of week
  $d_start_week = first_day_week($time);
  if ( ! isset($time["date_end"]) ) 
    $time["date_end"] = date("Ymd",$d_start_week+6*86400);
 
  $d_start_prev = $d_start_week-7*86400;
  $d_start_next = $d_start_week+7*86400; 
     
  //Daylight Savings Time
  if ( date('I',$d_start_week) == '0' and  date('I',$d_start_prev) == '1' ) { 
  //winter to summer

    $d_start_prev -= 3600;   
  }
  else if ( date('I',$d_start_week) == '1' and date('I',$d_start_prev) == '0' ) {  //summer to winter

    $d_start_prev += 3600;
  }
  if ( date('I',$d_start_week) == '0' and  date('I',$d_start_next) == '1' ) { //winter to summer
    
    $d_start_next -= 3600;
  }
  else if ( date('I',$d_start_week) == '1' and date('I',$d_start_next) == '0' ) {  //summer to winter
   
    $d_start_next += 3600;
  }


   //set end of week
   $d_end_next = $d_start_next + 6*86400;
   $d_end_prev = $d_start_prev + 6*86400;
 
   echo "
      <CENTER> <FONT SIZE=-2>
         <TABLE BORDER=1>
            <TR><TD>
               <A HREF=\"" .

               url_prepare("time_index.php?action=".$time["action"].
        "&amp;param_begin=".date("Ymd",$d_start_prev).
        "&amp;param_end=".date("Ymd",$d_end_prev).
						  //        "&amp;user_id=".$time["user_id"].
        "&amp;st_detail=".$time["show_task_detail"])."\">
               ".$l_week_prev."
               </A>
            </TD>  
            <TD>
               <A HREF=\"" .
               url_prepare("time_index.php?action=".$time["action"].
        "&amp;param_begin=".date("Ymd",$d_start_next).
        "&amp;param_end=".date("Ymd",$d_end_next).
						  //        "&amp;user_id=".$time["user_id"].
        "&amp;st_detail=".$time["show_task_detail"])."\">
               ".$l_week_next."
               </A>
            </TD></TR>
         </TABLE>    
      </FONT> </center>    
        ";     

 } else if ($type == "month") {
  if (debug_level_isset($cdg_param)) {
    echo "dis_time_links \$time <br>";
	debug_array($time);
  }
  //get the current start and end of month
  $d_start_month = first_day_month($time);

  if (debug_level_isset($cdg_param)) {
    echo "DIS_TIME_LINKS <br>";
	echo "date ". $time["date"] . " et fin ". $time["date_end"] . " <br>";
  }

  // THIS SHOULD BE SET
  if ( ! isset($time["date_end"]) ) 
    echo "date_end not set !!! <br>";
  //    $time["date_end"] = date("Ymd",$d_start_week+6*86400);

  // This should work for january also (month 0 = december) 
  $month=substr($time["date"],4,2);
  $year=substr($time["date"],0,4);
  $d_start_prev = date("Ymd", mktime(0,0,0,$month-1,1,$year));
  $d_end_prev = date("Ymd", mktime(0,0,0,$month,0,$year));
  $d_start_next = date("Ymd", mktime(0,0,0,$month+1,1,$year));
  $d_end_next = date("Ymd", mktime(0,0,0,$month+2,0,$year));
  $this_month_sec = mktime(0,0,0,$month,1,$year);

  if (debug_level_isset($cdg_param)) {
    echo "previous $d_start_prev, $d_end_prev next 
       $d_start_next, $d_end_next <br>";
  }

  //Daylight Savings Time
  // nothing to do for this
   echo "
      <CENTER> <FONT SIZE=-2>
         <TABLE BORDER=1>
            <TR>
              <TD colspan=2 align=center><font size=+1> $l_month2 ". month_name($month) ." </TD>
            </TR>
            <TR><TD>
               <A HREF=\"" .
               url_prepare("time_index.php?action=".$time["action"].
           "&amp;param_begin=$d_start_prev&amp;param_end=$d_end_prev".
						  //		   "&amp;user_id=".$time["user_id"].
           "&amp;st_detail=".$time["show_task_detail"])."\">
               ".$l_month_prev."
               </A>&nbsp;
            </TD>  
            <TD>&nbsp;
               <A HREF=\"" .
            url_prepare("time_index.php?action=".$time["action"].
                "&amp;param_begin=".
                $d_start_next.
                "&amp;param_end=".
                $d_end_next.
					   //                "&amp;user_id=".
					   //                $time["user_id"].
                "&amp;st_detail=".
                $time["show_task_detail"]).
                "\">".
                $l_month_next.
                "</A>
            </TD></TR>
         </TABLE>    
      </FONT> </CENTER>    
        ";     
   }
}

 
///////////////////////////////////////////////////////////////////////////////
// Display TimeManager User search Form                                      //
// Parameters:
//   - $time[] : default form values
//     keys used  : week
//   - $usr_q  : DBO : UserObm list
//   - $uid    : current user id
///////////////////////////////////////////////////////////////////////////////
function dis_time_search_form ($time, $usr_q, $uid) {
  global $l_see, $l_week, $l_month, $project_managers;
  global $l_user;
  global $uid;

  $hum_date_start = dis_human_date($time["date"]);
  $hum_date_end = dis_human_date($time["date_end"]);

  if ($time["interval"] == "week")
	$period = $l_week;
  else if ($time["interval"] == "month")
	$period = $l_month;

  if (in_array($uid, $project_managers)) {
    $sel_user_id = $time["user_id"]["0"];
  
    // UserObm Select
    $sel_user = "<select name=\"sel_user_id[]\" size=6>";
    $selected = false;
    while ($usr_q->next_record()) {
      $u_id = $usr_q->f("userobm_id");
      $lname = $usr_q->f("userobm_lastname");
      $fname = $usr_q->f("userobm_firstname");
      $sel_user .= "\n<option value=\"$u_id\"";
 
      // decide which user to select:
      // if one has been selected (sel_user_id) select it,
      // else select the current user
      if ( (! $selected)
           && ( (($sel_user_id == $u_id) && (! is_null($sel_user_id)))
                || ( ($u_id == $uid) && (is_null($sel_user_id)) ) ) ) {
        $selected = "1";  
        $sel_user .= "selected";
      }

      $sel_user .= ">$lname $fname</option>";
    }
    $sel_user .= "</select>";

    // Define the action to follow
    if ($time["action"] == "index")
	  $act = "index";
    else
	  $act = "search";

    echo "<br>
      <center>
      <form method=post name=f_time action=\"" .
      url_prepare("time_index.php") . "\">
      <table border=1>
      <tr>
        <td><font size=-2>$period</font></td>
        <td><font size=-1>Du $hum_date_start au $hum_date_end</font></td>
      </tr><tr>
        <td><font size=-2> $l_user </font></td>
        <td align=left>
        $sel_user
        </td>
        <td valign=bottom>&nbsp;&nbsp;
          <input type=hidden name=param_begin value=\"".$time["date"]."\">
          <input type=hidden name=param_end value=\"".$time["date_end"]."\">
         <input type=hidden name=st_detail value=\"".
             $time["show_task_detail"]."\">
          <input name=\"action\" type=hidden value=\"$act\">
          <input name=\"submit\" type=submit value=\"$l_see\">&nbsp;
        </td>
      </tr>
      </table>
      </form><hr>
      </center>";
  }
  else
    echo "<br>$l_week du $hum_date_start au $hum_date_end<br><br>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Select Form for Users                                       //
// Parameters:
//   - $time[] : default form values
//     keys used  : interval
//   - $usr_q  : DBO : UserObm list
//   - $uid    : current user id
///////////////////////////////////////////////////////////////////////////////
function dis_selectusers_form ($time, $usr_q, $uid) {
  global $l_see, $l_week, $l_month, $project_managers, $stats_users;
  global $l_user, $l_all_users;
  global $l_tasktype, $l_task_all, $l_task_uservalid, $l_task_adminvalid;
  global $uid;

  $hum_date_start = dis_human_date($time["date"]);
  $hum_date_end = dis_human_date($time["date_end"]);

  if ($time["interval"] == "week")
	$l_period = $l_week;
  else if ($time["interval"] == "month")
	$l_period = $l_month;

  if (in_array($uid, $project_managers) or in_array($uid, $stats_users)) {
    $sel_user_id = $time["user_id"];
  
  if (debug_level_isset($cdg_param)) {
    echo "tableau users";
	print_r($time["user_id"]);
    echo "<br>";
  }

  if ($time["allusers"])
	  $allusers = " checked";

    // UserObm Select
    $sel_user = "<select name=\"sel_user_id[]\" size=6 multiple>";
    while ($usr_q->next_record()) {
      $u_id = $usr_q->f("userobm_id");
      $lname = $usr_q->f("userobm_lastname");
      $fname = $usr_q->f("userobm_firstname");
      $sel_user .= "\n<option value=\"$u_id\"";
 
      // decide which user to select:
      // if one has been selected (sel_user_id) select it,
      // else we do not select any
      if (in_array($u_id, $sel_user_id)) {
        $sel_user .= "selected";
      }

      $sel_user .= ">$lname $fname</option>";
    }
    $sel_user .= "</select>";


    echo "<br>
      <center>
      <form method=post name=f_time action=\"" .
      url_prepare("time_index.php") . "\">
      <table border=1>
      <tr>
        <td><font size=-2>$l_period</font></td>
        <td><font size=-1>Du $hum_date_start au $hum_date_end</font></td>
      </tr><tr>
        <td><font size=-2> $l_user <br>$l_all_users<input type=checkbox name=cb_allusers $allusers></font></td>
        <td align=left>
        $sel_user
        </td>
        <td valign=bottom>&nbsp;&nbsp;
          <input type=hidden name=param_begin value=\"".$time["date"]."\">
          <input type=hidden name=param_end value=\"".$time["date_end"]."\">
          <input name=\"action\" type=hidden value=\"stats\">
          &nbsp;
        </td>
      </tr>
      <tr>
        <td><font size=-2>$l_tasktype</font></td>
        <td>
           <input name=\"rb_tasktype\" type=radio value=$c_task_notvalid>
           <font size=-2>$l_task_all</font><br>
           <input name=\"rb_tasktype\" type=radio value=$c_task_uservalid>
           <font size=-2>$l_task_uservalid</font><br>
           <input name=\"rb_tasktype\" type=radio value=$c_task_adminvalid>
           <font size=-2>$l_task_adminvalid</font><br>
        </td>
        <td>          
           <input name=\"submit\" type=submit value=\"$l_see\">&nbsp;
        </td>
      </tr>
      </table>
      </form><hr>
      </center>";
  }
  else
    echo "<br>$l_week du $hum_date_start au $hum_date_end<br><br>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Time Period                                      //
// Parameters:
//   - $time[] : default form values
//     keys used  : week
//   - $usr_q  : DBO : UserObm list
//   - $uid    : current user id
///////////////////////////////////////////////////////////////////////////////
function dis_time_period ($time) {
  global $l_see, $l_week, $l_month, $project_managers;
  global $l_user;
  global $uid;

  $hum_date_start = dis_human_date($time["date"]);
  $hum_date_end = dis_human_date($time["date_end"]);

  if ($time["interval"] == "week")
	$l_period = $l_week;
  else if ($time["interval"] == "month")
	$l_period = $l_month;

    echo "<br>
      <center>
      <table border=1>
      <tr>
        <td><font size=-2>$l_period</font></td>
        <td><font size=-1>Du $hum_date_start au $hum_date_end</font></td>
      </tr>
      <tr>
      </table>
      <hr>
      </center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the full TimeManager result page                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_list($time) {
  global $l_no_found, $cdg_param;
  global $auth, $new_order, $order_dir;
  global $status_id; // "Signed" status for deals
  global $c_day_fraction; // nbre de fraction possible d'une journée
  global $c_days_in_a_week; // how many days in a week have to be filled (5)
  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
  //echo "IN dis_time_list <br>";
  // obm_q contains the time events for a user and an interval
  $obm_q = run_query_search($time, $new_order, $order_dir);
  $nb_task = $obm_q->num_rows();

  $pref_day_q = run_query_display_pref($auth->auth["uid"], "time_day");
  $obm_day_q = run_query_by_day($time, $new_order, $order_dir);

  //OLD
  //  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
  //  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir);
  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir);

  // Get elements for insertion of new task
  // TaskType
  $obm_tasktype_q = run_query_tasktype();
  // Project
  $obm_project_q = run_query_project();

  //get the current start and end of week
  $d_start_week = first_day_week($time);   
  
  $date_start = date("Ymd",$d_start_week);
  $date_end = date("Ymd",$d_start_week + (6*86400));
     
  $hum_date_start = dis_human_date($date_start);
  $hum_date_end = dis_human_date($date_end);

  // array of validated days
  $val_days = run_query_valid_search($time);
  if (debug_level_isset($cdg_param)) {
    echo "jours validés : ".sizeof($val_days)." <br>";
    print_r($val_days);
  }
  dis_time_menu($time);

  if ($nb_task == 0) {
    display_warn_msg("<BR>".$l_no_found);
  } else {
	  //      $names = run_query_get_names($time["user_id"]);
      //      $names->next_record();
      
    // Don't display task list if 
    //   - period is validated (each day is filled and validated)
    //   - show_task_detail is false

    if (sizeof($val_days) < $c_days_in_a_week ||
         $time["show_task_detail"] ) 
       dis_time_list_result($obm_q,$pref_q,$nb_task,$time);
    else
       echo "taches non affichees par choix <br>";
  }
  
  // Creating the dates for the selected (or current) date
  // format "Mon 25 May"
  $day_q = get_this_week($d_start_week, $c_days_in_a_week);
  
  if (debug_level_isset($cdg_param)) {
    echo "liste des jours <br>";
    print_r($day_q);
  }

  // dis_form_addtask does not display anything if days are full
  //   and validated
  dis_form_addtask("insert", 
                     $obm_project_q, 
                     $obm_tasktype_q, 
                     $day_q, 
                     $c_day_fraction,
                     $time, 
                     $val_days);  

  if($nb_task != 0) {

    dis_form_uservalid_day($obm_day_q,$pref_day_q,$nb_task,$time, $val_days);
    dis_form_adminvalid_day($obm_day_q,$pref_day_q,$nb_task,$time, $val_days);

	// OLD
	//    dis_time_deal_list($obm_deal_q,$pref_deal_q,$nb_task,$time);

    dis_time_deal_list($obm_deal_q,$pref_deal_q,$nb_task,$time);

  }
  
}


///////////////////////////////////////////////////////////////////////////////
// Display the full TimeManager result page for Index Page                       //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_index($time) {
  global $l_no_found, $cdg_param;
  global $auth, $new_order, $order_dir;
  global $status_id; // "Signed" status for deals
  global $c_day_fraction; // nbre de fraction possible d'une journée
  global $c_days_in_a_week; // how many days in a week have to be filled (5)
  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
  // Display the total by day for a month
  //echo "IN dis_time_list <br>";
  // obm_q contains the time events for a user and an interval
  // $obm_q = run_query_search($time, $new_order, $order_dir);
  // $nb_task = $obm_q->num_rows();

  //  $pref_day_q = run_query_display_pref($auth->auth["uid"], "time_day");
  //  $obm_day_q = run_query_by_day($time, $new_order, $order_dir);

  $pref_month_task_q = run_query_display_pref($auth->auth["uid"], "time_task_month");

  // If $action=globalview, show all users
  if ($time['action'] == "globalview")
     $obm_month_task_q = run_query_task_one_month($time, 'all');
  else
     $obm_month_task_q = run_query_task_one_month($time, '');
  // Get elements for insertion of new task
  // TaskType
  //  $obm_tasktype_q = run_query_tasktype();
  // Project
  //  $obm_project_q = run_query_project();

  //get the current start and end of week
  $d_start_week = first_day_week($time);   
  
  $date_start = date("Ymd",$d_start_week);
  $date_end = date("Ymd",$d_start_week + (6*86400));
     
  $hum_date_start = dis_human_date($date_start);
  $hum_date_end = dis_human_date($date_end);

  // Task for the month
  if ($time['action'] == "globalview")
    dis_time_one_month($obm_month_task_q, $pref_month_task_q, $time, true);
  else
    dis_time_one_month($obm_month_task_q, $pref_month_task_q, $time);

}


///////////////////////////////////////////////////////////////////////////////
// Display the Stats TimeManager result page                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_stats($time) {
  global $l_no_found, $cdg_param;
  global $auth, $new_order, $order_dir, $new_order2;
  global $status_id; // "Signed" status for deals
  global $fraction_jour; // nbre de fraction possible d'une journée
  global $days_in_a_week; // how many days in a week have to be filled (5)
  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
  //echo "IN dis_time_list <br>";
  // obm_q contains the time events for a user and an interval
//  $obm_q = run_query_search($time, $new_order, $order_dir, $new_order2);
//  $nb_task = $obm_q->num_rows();

  $pref_day_q = run_query_display_pref($auth->auth["uid"], "time_day");
  $obm_day_q = run_query_by_day($time, $new_order, $order_dir, $new_order2);

  // OLD
  //  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
  //  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir, $new_order2);

  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir);

  // Get elements for insertion of new task
  // TaskType
  $obm_tasktype_q = run_query_tasktype();
  // Project
  $obm_project_q = run_query_project();

  //get the current start and end of week
  $d_start_week = first_day_week($time);   
  
  $date_start = date("Ymd",$d_start_week);
  $date_end = date("Ymd",$d_start_week + (6*86400));
     
  $hum_date_start = dis_human_date($date_start);
  $hum_date_end = dis_human_date($date_end);

  dis_time_period($time);

  // array of validated days
  $val_days = run_query_valid_search($time);
  if (debug_level_isset($cdg_param)) {
    echo "jours validés : ".sizeof($val_days)." <br>";
    print_r($val_days);
  }

  dis_time_menu($time);

  // Creating the dates for the selected (or current) date
  // format "Mon 25 May"
  $day_q = get_this_week($d_start_week, $days_in_a_week);
  
  if (debug_level_isset($cdg_param)) {
    echo "liste des jours <br>";
    print_r($day_q);
  }

  // OLD
  //  dis_time_deal_list($obm_deal_q,$pref_deal_q,$nb_task,$time);  
dis_time_deal_list($obm_deal_q,$pref_deal_q,$nb_task,$time);

  
}

///////////////////////////////////////////////////////////////////////////////
// Export the Stats TimeManager result in a file                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_export_stats($time) {

  global $auth;
  global $status_id; // "Signed" status for deals
  global $fraction_jour; // nbre de fraction possible d'une journée
  global $days_in_a_week; // how many days in a week have to be filled (5)
  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir);

  dis_time_deal_export($obm_deal_q,$pref_deal_q,$nb_task,$time);

  
}

///////////////////////////////////////////////////////////////////////////////
// Display the Time menu                                                     //
// Parameters : 
//  - $time[] : time search criteria
//    keys : usual as needed
//           - $show_task_detail [true|false] show_task_detail set or not
// Returns :
//   an HTML table
///////////////////////////////////////////////////////////////////////////////
function dis_time_menu($time) {
  global $col_tableh, $col_table;

  global $show_events, $total_menu, $total_tasks, $total_companies, $total_projects;

  // checked the show event checkbox or not
  $dis_checked= $time["show_task_detail"] ? "checked" : "";

  echo "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$show_events</td>
      <td class=\"detailHead\" colspan=\"3\">$total_menu</td>
    </tr><tr>
        <td class=\"detailLabel\">
           <form method=\"post\" name=\"form_show_events\" 
                 action=\"".url_prepare("time_index.php")."\">
              <input type=\"hidden\" name=\"action\" value=\"show_details\">
              <input type=\"checkbox\" name=\"show_task_detail\" $dis_checked>
<!--              <input type=\"hidden\" name=\"user_id[]\" value=\"".
                 $time["user_id"]."\">
-->
              <input type=\"hidden\" name=\"param_begin\" value=\"".
                 $time["date"] ."\">
              <input type=\"hidden\" name=\"param_end\" value=\"".
                 $time["date_end"] ."\">
              <input type=submit value=\"OK\" >
           </form>
        </td>
        <td class=\"detailLabel\">$total_companies</TD>
        <td class=\"detailLabel\">$total_projects</TD>
        <td class=\"detailLabel\">$total_tasks</TD>
    </tr>
    </table>
  ";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Task  result                                        //
// Parameters : 
//   - $obm_q    : list of tasks to be displayed
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//   - $time[]   : time search criteria
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_time_list_result($obm_q,$pref_q,$nb_task,$time) {
  //-- Themes
  global $set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_task_del, $l_found_task;

  dis_table_title($l_found_task) ;
  echo "<font color=\"#$col_textem\">$nb_task $l_found</font><br>";

  $url = url_prepare("time_index.php?action=index&amp;param_begin=".$time["date"].
					"&amp;param_end=".$time["date_end"].
					//					"&amp;user_id=".$time["user_id"].
					"&amp;tf_week=".urlencode($week).
					"&amp;st_detail=".$time["show_task_detail"]);
 
  echo "
    <form method=post name=f_task_del action=\"".url_prepare("time_index.php")."\" \"onSubmit=\"if (confirm_del()) return true; else return false\" >";

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_q);
  $dis_task_list->data_set = $obm_q;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_page = false;
  $dis_task_list->data_url = $url; 
  $dis_task_list->data_cb_text = $l_task_del;
  $dis_task_list->data_idfield = "task_id"; 
  $dis_task_list->data_cb_side = "left";
  $dis_task_list->data_cb_name = "delete_";
  $dis_task_list->data_cb_show = "test_status";

  $dis_task_list->display();

  echo "
    <br/>
    <input type=\"hidden\" name=\"user_id[]\" value=\"".$time["user_id"][0]."\">
    <input type=\"hidden\" name=\"param_begin\" value=\"".$time["date"]."\">
    <input type=\"hidden\" name=\"param_end\" value=\"".$time["date_end"]."\">
    <input type=\"hidden\" name=\"show_task_detail\" value=\"".
       $time["show_task_detail"]."\">
    <input name=\"action\" type=\"hidden\" value=\"delete\">
    <input name=\"submit\" type=\"submit\" value=\"".$l_delete."\" >
    </form>
       ";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Day result                                        //
// Parameters : 
//   - $obm_q    : list of tasks to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//   - $time[]   : time search criteria
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
// PLUS UTILISE ---- A SUPPRIMER ----- 
// DECOUPE dis_form_uservalid_day et dis_form_adminvalid_day

function dis_time_day_list($obm_dq,$pref_dq,$nb_task,$time) {
  //-- Themes
  global $set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate_task, $l_valid;
  //session
  global $uid, $project_managers;

  echo "<font color=\"#$col_textem\">";
  echo "</font><br>";

  $url = url_prepare("time_index.php?action=search&amp;tf_week=".urlencode($week));


 dis_table_title("$l_total_day"); 
 echo "
   <form method=post name=f_task_val action=\"".url_prepare("time_index.php")."\">";

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;

  $dis_task_list->data_cb_text = "$l_validate_task";;
  $dis_task_list->data_idfield = "task_date"; 
  $dis_task_list->data_cb_field = "task_status";
  $dis_task_list->data_cb_side = "left";
  $dis_task_list->data_cb_name = "validate_";
  $dis_task_list->data_cb_show = "task_valid";
  $dis_task_list->data_order = false;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_page = false;
  $dis_task_list->data_url = $url;

// display in another table to limit size
  echo "<table border=0 width=50%><tr><td>";
  $dis_task_list->display();
  echo "<td><tr></table>";

  echo "
    <br>
    <input type=hidden name=user_id[] value=\"".$time["user_id"][0]."\">
    <input type=hidden name=param_begin value=\"".$time["date"]."\">
    <input type=hidden name=param_end value=\"".$time["date_end"]."\">
    <input type=hidden name=show_task_detail value=\"".
        $time["show_task_detail"]."\">
    <input name=\"action\" type=hidden value=\"validate\">
    <input name=\"submit\" type=submit value=\"$l_valid\" >
    </form>
       ";

//for project managers
  if( in_array($uid,$project_managers)) {
    dis_table_title("$l_projects_manager"); 
    $admin_db = run_query_admin($time);

    echo "
      <form method=post name=f_task_val_admin action=\"".url_prepare("time_index.php")."\">";

   $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
   $dis_task_list->data_set = $admin_db;

   $dis_task_list->data_cb_text = "$l_validate_task";;
   $dis_task_list->data_idfield = "task_date";
   $dis_task_list->data_cb_field = "task_status"; 
   $dis_task_list->data_cb_side = "left";
   $dis_task_list->data_cb_name = "validate_";
   $dis_task_list->data_cb_show = 1;
   $dis_task_list->data_order = false;
   $dis_task_list->data_header = "top";
   $dis_task_list->data_page = false;
   $dis_task_list->data_url = $url;

 // display in another table to limit size
   echo "<table border=0 width=50%><tr><td>";
   $dis_task_list->display();
   echo "<td><tr></table>";

   echo "
     <br>
     <input type=hidden name=user_id[] value=\"".$time["user_id"]."\">
     <input type=hidden name=param_begin value=\"".$time["date"]."\">
     <input type=hidden name=param_end value=\"".$time["date_end"]."\">
     <input type=hidden name=show_task_detail value=\"".
        $time["show_task_detail"]."\">
     <input name=\"action\" type=hidden value=\"validate_admin\">
     <input name=\"submit\" type=submit value=\"".$l_valid."\" >
     </form>
        ";    
   }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Form used by users to validate day tasks                 //
// Parameters : 
//   - $obm_q    : list of tasks to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//   - $time[]   : time search criteria
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_form_uservalid_day($obm_dq,$pref_dq,$nb_task,$time, $validate_days) {
  //-- Themes
  global $set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate_task, $l_valid;
  global $c_days_in_a_week, $c_task_adminvalid;
  //session
  global $uid, $project_managers;


  // We have to know if all the days have been validated by the admin
  // If so, no need to display the user valid one
  if (is_array($validate_days) and array_sum($validate_days) == $c_days_in_a_week * $c_task_adminvalid) {
	echo "taches déjà validées par l'admin, on affiche pas le tableau de validation utilisateur";
    return ;
  }
     
  $url = url_prepare("time_index.php?action=search&amp;tf_week=".urlencode($week));


 dis_table_title("$l_total_day"); 
 echo "
   <form method=\"post\" name=\"f_task_val\" action=\"".url_prepare("time_index.php")."\">";

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;

  $dis_task_list->data_cb_text = "$l_validate_task";;
  $dis_task_list->data_idfield = "date_task"; 
  $dis_task_list->data_cb_field = "task_status";
  $dis_task_list->data_cb_side = "left";
  $dis_task_list->data_cb_name = "validate_";
  $dis_task_list->data_cb_show = "task_valid";
  $dis_task_list->data_order = false;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_page = false;
  $dis_task_list->data_url = $url;

  $dis_task_list->display();

  echo "
    <br/>
    <input type=\"hidden\" name=\"user_id[]\" value=\"".$time["user_id"][0]."\">
    <input type=\"hidden\" name=\"param_begin\" value=\"".$time["date"]."\">
    <input type=\"hidden\" name=\"param_end\" value=\"".$time["date_end"]."\">
    <input type=\"hidden\" name=\"show_task_detail\" value=\"".
        $time["show_task_detail"]."\">
    <input name=\"action\" type=\"hidden\" value=\"validate\">
    <input name=\"submit\" type=\"submit\" value=\"$l_valid\" >
    </form>
       ";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Form used by admins to validate day tasks                //
// Parameters : 
//   - $obm_q    : list of tasks to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//   - $time[]   : time search criteria
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_form_adminvalid_day($obm_dq,$pref_dq,$nb_task,$time, $validate_days) {
  //-- Themes
  global $set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate_task, $l_valid;
  global $c_days_in_a_week, $c_task_notvalid, $l_validate_msg1;
  //session
  global $uid, $project_managers;


  // We have to know if any of the days have been validated by the user
  // If not, no need to display the admin valid form
  if (! is_array($validate_days)) {
	echo "$l_validate_msg1";
    return ;
  }
     

  $url = url_prepare("time_index.php?action=search&amp;tf_week=".urlencode($week));

//for project managers
  if( in_array($uid,$project_managers)) {
    dis_table_title("$l_projects_manager"); 
    $admin_db = run_query_admin($time);

    echo "
      <form method=\"post\" name=\"f_task_val_admin\" action=\"".url_prepare("time_index.php")."\">";

   $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
   $dis_task_list->data_set = $admin_db;

   $dis_task_list->data_cb_text = "$l_validate_task";;
   $dis_task_list->data_idfield = "date_task";
   $dis_task_list->data_cb_field = "task_status"; 
   $dis_task_list->data_cb_side = "left";
   $dis_task_list->data_cb_name = "validate_";
   $dis_task_list->data_cb_show = 1;
   $dis_task_list->data_order = false;
   $dis_task_list->data_header = "top";
   $dis_task_list->data_page = false;
   $dis_task_list->data_url = $url;

 // display in another table to limit size
   $dis_task_list->display();

   echo "
     <br>
     <input type=\"hidden\" name=\"user_id[]\" value=\"".$time["user_id"][0]."\">
     <input type=\"hidden\" name=\"param_begin\" value=\"".$time["date"]."\">
     <input type=\"hidden\" name=\"param_end\" value=\"".$time["date_end"]."\">
     <input type=\"hidden\" name=\"show_task_detail\" value=\"".
        $time["show_task_detail"]."\">
     <input name=\"action\" type=\"hidden\" value=\"validate_admin\">
     <input name=\"submit\" type=\"submit\" value=\"".$l_valid."\" >
     </form>
        ";    
   }
}



///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Deal  result                                        //
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_time_deal_list($obm_dq,$pref_dq,$nb_task, $time, $customer =0) {
  //-- Themes
  global $set_theme,$col_tableh,$col_textem,$col_table;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;
  global $l_workingdays, $l_total_days_msg1, $l_total_days_msg2, $l_total_days_msg3, $l_total_days_msg4; 

  dis_table_title($l_total_deal);  

  $url = url_prepare("time_index.php?action=search&amp;tf_week=".urlencode($week));

  // Calculate the task length grand total 
  if ($obm_dq->nf()) {
    while ($obm_dq->next_record()) {
	  $res += $obm_dq->f("task_totalperiod");
	}
	// Rewind the table
	$obm_dq->seek() or 
    die ("dis_time_deal_list2 : requete list2 sans résultat !");
  }

  // Number of users we grand total
  $nb_users = sizeof($time["user_id"]);

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;
  $dis_task_list->data_order = false;
  $dis_task_list->data_page = false;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_url = $url;
  
// display in another table to limit size
  echo "<table border=0 align=center width=80%><tr><td>";
  $dis_task_list->display();
  echo "</td></tr>";

  if ($time["interval"] == "month") {
    echo "<tr><td>";
	echo "$l_total_days_msg1 $nb_users $l_total_days_msg2  : $res $l_total_days_msg3 ".
		get_nb_working_days($time["date"]) * $nb_users. " $l_total_days_msg1";
	echo "</td></tr>";
	echo "<tr><td>";
	echo "$l_workingdays : ".get_nb_working_days($time["date"]);
	echo "</td></tr>";
  }
  echo "       </table>";

  // $popup = 2 -> file (no headers)
  $url = url_prepare("time_index.php?action=export_stats" .
            "&amp;param_begin=".$time["date"]. 
            "&amp;param_end=".$time["date_end"].
            "&amp;popup=2");
  echo "<a href=\"$url\">exporter</a>";
  // We build a file
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Deal result in a file                          //
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_time_deal_export($obm_dq,$pref_dq,$time) {
  //-- Themes
  global $set_theme,$col_tableh,$col_textem,$col_table;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;
  global $l_workingdays, $l_total_days_msg1, $l_total_days_msg2, $l_total_days_msg3, $l_total_days_msg4; 



  // Calculate the task length grand total 
  if ($obm_dq->nf()) {
    while ($obm_dq->next_record()) {
	  $res += $obm_dq->f("task_totalperiod");
	}
	// Rewind the table
	$obm_dq->seek() or 
    die ("dis_time_deal_list2 : requete list2 sans résultat !");
  }

  // Number of users we grand total
  $nb_users = sizeof($time["user_id"]);

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;
  $dis_task_list->data_order = false;
  $dis_task_list->data_page = false;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_url = $url;
  
  $dis_task_list->dis_data_file($dis_task_list->separator);

  if ($time["interval"] == "month") {

	echo "\n $l_total_days_msg1 $nb_users $l_total_days_msg2 : $res $l_total_days_msg3 ".
		get_nb_working_days($time["date"]) * $nb_users. " $l_total_days_msg1\n";

	echo "$l_workingdays : ".get_nb_working_days($time["date"]);
  }

}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Deal  result                                        //
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_time_one_month($obm_dq,$pref_dq,$time, $shortview=false) {
  //-- Themes
  global $set_theme,$col_tableh,$col_textem,$col_table;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;
  global $c_day_fraction, $c_days_in_a_week;


  dis_table_title($l_total_month);  

  $obm_user_q = run_query_userobm();

  while($obm_user_q->next_record()) {
	$userid = $obm_user_q->f("userobm_id");
	$f_name = $obm_user_q->f("userobm_firstname");
	$l_name = $obm_user_q->f("userobm_lastname");
    $array_users[$userid] = $f_name . " " . $l_name;
  }

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);
  $day=intval(substr($time["date"],6,2));

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, $day, $year);
  // Fist Weekday
  $first_wday =date("w", $first_day_sec);

  // Last day
  $last_day_sec = mktime(23,0,0,$month+1, $day-1, $year);
  $last_wday =date("w", $last_day_sec);

  $day_in_sec = $first_day_sec;
  $wday =date("w", $day_in_sec);

  while ($obm_dq->next_record())  {
	  //echo "day $day, weekday $wday, \$i $day_in_sec  " .
	  //" day_in_month " . intval($obm_dq->f("day_in_month")) ."<br>";
	$user_id = $obm_dq->f("userid");

    if (isset($prev_id) && $user_id != $prev_id) {   
	  // the user changed 1. finish previous array, 2.start new one
      // we ends the array
      while ($day_in_sec < $last_day_sec) {
	    $array_res[$prev_id][$day]["length"] = 0;
		$array_res[$prev_id][$day]["wday"] = $wday;
		$day++;        
		$day_in_sec = mktime(0,0,0,$month, $day, $year);
		$wday =date("w", $day_in_sec);
	  }
      // we initialyse new one
      $day_in_sec = $first_day_sec;
	  $wday =date("w", $day_in_sec);
      $day = 1;
	  
	}
	
	while (($day_in_sec < $last_day_sec) && 
			 ($day <= intval($obm_dq->f("day_in_month")))) {
		//      echo "day $day loop ". $obm_dq->f("day_in_month") . " length ". 
		//		  $obm_dq->f("total_length") . "<br>";
      if (intval($obm_dq->f("day_in_month")) != $day)
		$array_res[$user_id][$day]["length"] = 0;
      else {
		$array_res[$user_id][$day]["length"] = $obm_dq->f("total_length");
        $length_tot[$user_id] += $array_res[$user_id][$day]["length"];
	  }
      $array_res[$user_id][$day]["wday"] = $wday;
	  $day++;        
	  $day_in_sec = mktime(0,0,0,$month, $day, $year);
	  $wday =date("w", $day_in_sec);
	}

  $prev_id=$user_id;
  }

      // we ends the last array
      while ($day_in_sec < $last_day_sec) {
	    $array_res[$prev_id][$day]["length"] = 0;
		$array_res[$prev_id][$day]["wday"] = $wday;
		$day++;        
		$day_in_sec = mktime(0,0,0,$month, $day, $year);
		$wday =date("w", $day_in_sec);
	  }
 

	  // Loop among all users...
  while (list($uid) = each($array_res)) {
	if ($shortview) {
      display_monthview($time, $array_res[$uid], $length_tot[$uid], $array_users[$uid], true);
      echo "<br>\n";
	}
	else
      display_monthview($time, $array_res[$uid], $length_tot[$uid], $array_users[$uid], false);
  }
}


//////////////////////////////////////////////////////////////////////////////
// HTML Display the Month View for one user :                               //
//  | 01 | 02 | 03 | 04
//    OK   OK        5/8
// Parameters : 
//   - $array_res : an associative array build by XXXXX 
//      $array_res[$day_in_month][string]
//     where
//        $day_in_month : [1-31]
//        string : length = total filled for this day [1-$c_day_fraction]
//                 wday   = number of the day in the week
//   - $total : the amount of task filled for the month
//////////////////////////////////////////////////////////////////////////////

function display_monthview($time, $array_res, $total, $title, $shortview=false) {
  global $working_days;  // array of the days in a week where we are supposed to work
  global $c_day_fraction, $c_days_in_a_week;
  global $col_orange, $col_grey, $col_blue;
  global $l_total;
  global $l_fill;
  global $l_workingdays2, $l_total_user_msg1, $l_total_days_msg3, $l_total_days_msg4; 

  //  $col = "b2ebe8";
  //  $col_red="a00000";
  /*  echo "<br> res ";
  print_r($array_res); */
  $col_width=25;
  $month=substr($time["date"],4,2);
  $year=substr($time["date"],0,4);
  $day=substr($time["date"],6,2);

  // Display the array
  while (list($key,$val) = each($array_res)) {
	if ($working_days[$val["wday"]] == 0) {
      $line1 .= "<td width=15 bgcolor=\"#$col_grey\" align=center>$key</td>\n";
	  $line2 .= "<td bgcolor=\"#$col_grey\">&nbsp;</td>\n";
	  $line3 .= "<td bgcolor=\"#$col_grey\">&nbsp;</td>\n";
	}
	else {
      $cell_1 = "<td width=$col_width align=center>$key</td>";
      $cell_1_short = $cell_1;
      if ($val["length"] == $c_day_fraction)
        $line2 .= "<td>OK</td>";
      else
  	    $line2 .= "<td bgcolor=\"#$col_orange\">".$val["length"]."/$c_day_fraction</td>";

      // first day of the week 
	  if ($val["wday"] == 1) {
		// return first and last day of the week
		$day = $key < 10 ? "0".$key : $key; 
		$week = get_week_point($year.$month.$day);
		$url= url_prepare("time_index.php?action=search".
           "&amp;param_begin=".$week[0].
           "&amp;param_end=".$week[1].
						  //        "&amp;user_id=".$time["user_id"].
		   "&amp;st_detail=".$time["show_task_detail"]);
        $line3.="<td colspan=\"$c_days_in_a_week\"><a href=\"$url\">$l_fill</a></td>";
        $cell_1_short="<td width=$col_width align=center><a href=\"$url\">$key</a></td>";
	  }
	  //or first day of the month
	  else if ($key == 1) {
		// return first and last day of the week
		$week = get_week_point($year.$month."01");
		$url= url_prepare("time_index.php?action=search".
           "&amp;param_begin=".$week[0].
           "&amp;param_end=".$week[1].
						  //        "&amp;user_id=".$time["user_id"].
		   "&amp;st_detail=".$time["show_task_detail"]);
        // This week is not $c_days_in_a_week long
        $week_length = $c_days_in_a_week - $val["wday"] +1;
        $line3.="<td colspan=\"$week_length\"><a href=\"$url\">$l_fill</a></td>";
        $cell_1_short="<td width=$col_width align=center><a href=\"$url\">$key</a></td>";
	  }
    if ($shortview)
	  $line1.=$cell_1_short;
    else
	  $line1.=$cell_1;
	}
  }


  // we display a recap for this user
  // shortview

  if ($time["interval"] == "month") {
	$fract = $total/$c_day_fraction;
    $tot_workingdays = get_nb_working_days($time["date"]);
    if($shortview) {
	   $last_cell_1 = "<td>&nbsp;&nbsp;</td><td width=60 align=center>$l_total</td>";
       $bg_color= $fract != $tot_workingdays ? "bgcolor=\"#$col_blue\"" : "";
       $dis_tot= $fract != $tot_workingdays ? "<b>$fract / $tot_workingdays</b>" : "$fract / $tot_workingdays";
	   $last_cell_2 = "<td>&nbsp;&nbsp;</td><td $bg_color>$dis_tot</td>";
	   $line1.=$last_cell_1;
	   $line2.=$last_cell_2;
    }
	else {
    $tab_total = "<table border=0 align=center width=80%><tr><td>\n";
    $tab_total .= "</td></tr>\n";

    $tab_total .= "<tr><td>\n";
	$tab_total .= "$l_total_user_msg1 : $fract $l_total_days_msg3 ".
		" $tot_workingdays $l_workingdays2 $l_total_days_msg4";
	$tab_total .= "</td></tr>\n";
	$tab_total .= "</table>\n";
  }

  }

  echo "<div align=left><table border=1>
           <tr><td width=200 align=left rowspan=3>&nbsp;&nbsp;<b>&nbsp;$title</b></td>
  $line1
  </tr><tr>
  $line2
  </tr>";
  if (! $shortview)
    echo "<tr>
  $line3
    </tr>";

  echo "       </table></div>";

  echo "$tab_total";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Table description or message                             //
// Parameters:
//   - $message : message to display
///////////////////////////////////////////////////////////////////////////////
function dis_table_title($message) {
 

  echo "<center><font size=+1>";
  echo $message;
  echo "<br></font></center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Time Entry Form                                               //
//   - the days already filled are not shown
//   - check box allows to select more than one day at once
//   - if all days in the week are validated, does not display anything
// Parameters:
//   - $action : one of "modify",
//   - $project_q : a Database Object listing deals
//   - $taskt_q : a Database Object listing deal categories
//   - $day_q : a Database Object listing days for this period of time
//   - $time : array of defined variables
//     - $validate_days : array of validated days (not to be shown)
//    
///////////////////////////////////////////////////////////////////////////////
function dis_form_addtask($action, $project_q, $taskt_q, $day_q, $fraction_t,$time, $validate_days) {
  global $auth,$time ,$l_addtask_start, $l_addtask_end, $fraction_jour ,$task_id_modify;
  global $col_table, $col_tableh, $col_textem, $l_add, $task_list, $l_none;
  global $l_label,$l_date,$l_project,$l_duration,$l_task, $l_update, $l_close;
  //for list classement of task
  global $l_task_fac,$l_task_int;
  global $c_days_in_a_week; // number of days to be displayed in a week

  // If length of $validate_days = length of week ($days_in_a_week) :
  //   => form not to be shown
  if (debug_level_isset($cdg_param)) {
	debug_array($time);
	echo "<br>day_q <br>";
    print_r($day_q);
	echo "<br>validate_days  <br>";
    print_r($validate_days);
    if (is_array($validate_days))
		echo "<br>sum validate_days " .array_sum($validate_days)."<br>";

	/*
    for ($day=0;$day< sizeof($day_q);  $day++) {
		if (isset($validate_days[$day_q[$day][1]]))
			echo "<br>day_q". $day_q[$day][1]. " in <br>";
		else
			echo "<br>day_q". $day_q[$day][1]. " out <br>";
	}
	*/
  }

  if (sizeof($validate_days) == $c_days_in_a_week)
     return;

  $task_list  = array($l_task_fac, $l_task_int);

  //get different attribute for task to be modify
  if ($action == "detailupdate") {
    $task_id = $time["task_id"];
    $task_to_be_modify = run_query_get_task($task_id);
    $is_day = substr($task_to_be_modify->f("task_date"),0,8);
    $is_tt = $task_to_be_modify->f("task_tasktype_id");
    $is_proj = $task_to_be_modify->f("task_deal_id");
    $is_time = $task_to_be_modify->f("task_length");
  }

  // Day select construction
  $sel_day = "<select name=sel_date onclick=\"uncheck_days(this.form)\">";
  for ($day=0;$day< sizeof($day_q);  $day++) {

    if(sizeof($validate_days)>0 && isset($validate_days[$day_q[$day][1]])) continue;
    $dlabel = $day_q[$day][0];
    $sel_day .= "<option value=\"".$day_q[$day][1]."\"";
    if ($is_day == $day_q[$day][1]) $sel_day .= " selected"; 
    $sel_day .= ">$dlabel</option>\n";
  }
  $sel_day .= "</select>";

  // Day check construction
  if( $action != "detailupdate") {
   for ($day=0;$day< sizeof($day_q);  $day++) {
     if(sizeof($validate_days)>0 && isset($validate_days[$day_q[$day][1]])) continue;
     $dlabel = $day_q[$day][0];
     $ch_day .= "<input type=\"checkbox\" name=\"cb_day[".$day_q[$day][1]."]\"";
     $ch_day .= ">$dlabel\n";
   }
  }
  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=sel_tasktype><option value=\"0\">$l_none</option>";
  while($taskt_q->next_record()) {
    //put menu titles
    if (($task_i = $taskt_q->f("tasktype_internal")) != $type_prec) {
      $sel_tt .= "<option disabled=\"disabled\">".$task_list[$task_i]." </option>";
      $type_prec = $task_i;
    }
    $tt_id = $taskt_q->f("tasktype_id");
    $tt_label = $taskt_q->f("tasktype_label");
    $sel_tt .= "\n<option value=\"$tt_id\"";
    if ($is_tt == $tt_id) $sel_tt .= " selected"; 
    $sel_tt .= ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$tt_label</option>\n";
  }
  $sel_tt .= "</select>";

  // Project select construction
  $sel_proj = "<select name=sel_project><option value=\"0\">$l_none</option>";
  while($project_q->next_record()) {
    $proj_id = $project_q->f("deal_id");
    $proj_label = $project_q->f("company_name")." -- ". $project_q->f("deal_label");
    $sel_proj .= "\n<option value=\"$proj_id\"";
    if ($is_proj == $proj_id) $sel_proj .= " selected"; 
    $sel_proj .= ">$proj_label</option>\n";
  }
  $sel_proj .= "</select>";

  // Project Label
  if ($action == "detailupdate")
    $tf_label ="<input type=\"text\" name=\"tf_label\" size=30 value=\"".$task_to_be_modify->f("task_label")."\">";
  else
    $tf_label ="<input type=\"text\" name=\"tf_label\" size=30>";

  // Project time length
  $sel_time="<select name=sel_time>";
  for ($t=1;$t<=$fraction_t;  $t++) {
    $dlabel = $t.'/'.$fraction_t;
    $sel_time .= "<option value=\"$t\"";
   if ($is_time == $t) $sel_time .= " selected"; 
   $sel_time .= ">$dlabel</option>\n";
  }
  $sel_time .= "</select>";


// FORM
  echo "<p>&nbsp;</p>";
  if ($action == "insert") {
    dis_table_title($l_addtask_start.$fraction_jour.$l_addtask_end) ;
    echo "
      <form method=get name=form_add_task
        onSubmit=\"if (check_form(this)) return true; else return false;\" 
        action=\"".url_prepare("time_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"$action\">
      <input type=\"hidden\" name=\"popup\" value=0>
  ";
  }
  else {
    dis_table_title($l_update_task);
    echo "
      <form method=get  name=form_add_task 
        onSubmit=\"if (check_form(this)) return true; else return false;\"
        action=\"".url_prepare("time_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"update\">
      <input type=\"hidden\" name=\"popup\" value=0>
  ";
  }
 echo " 
<table bgcolor=\"#$col_tableh\" border=0 width=100%>
   <tr>
     <td>".col_table($col_textem, $l_date)."<br>$sel_day $ch_day</td>
     <td>".col_table($col_textem, $l_duration)."<br>$sel_time</td>
     <td>".col_table($col_textem, $l_task)."<br>$sel_tt</td>
    </tr>
    <tr>
     <td colspan=2>".col_table($col_textem, $l_project)."<br>$sel_proj</td>
     <td>".col_table($col_textem, $l_label)."<br>$tf_label</td>
    </tr>
</table>
  <center>";

 if ($action == "insert") {
   echo "<br><input type=submit value=\"$l_add\">";
 } else {
   echo "<br><input type=hidden name=task_id value=\"$task_id\">
         <input TYPE=submit value=\"$l_update\">
	<br>
     <input type=submit value=\"$l_close\" onclick=\"window.close();\">";
 }     
 
 echo "
  </center>
    <input type=hidden name=user_id[] value=\"".$time["user_id"][0]."\">
    <input type=hidden name=param_begin value=\"".$time["date"]."\">
    <input type=hidden name=param_end value=\"".$time["date_end"]."\">
</FORM> <p>&nbsp;</p>";

}

function col_table($col, $data) {
   return "<font color=\"#$col\">$data</font>";
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Time search form with params                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_search($time) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  $obm_q = run_query_search($time, $new_order, $order_dir);
  $nb_contact = $obm_q->num_rows();
  if ($nb_contact == 0) {
    display_warn_msg($l_no_found);
  } else {
    // From the Contact Display Module
    html_contact_search_list($obm_q,$pref_q,$nb_contact,$contact);
  }
}


//////////////////////////////////////////////////////////////////////////
// Show a date in human format
//  params : $p_date : date in yyyymmdd format
//////////////////////////////////////////////////////////////////////////
function dis_human_date($p_date) {
  global $cdg_sql;
    
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);

   
    $mois = month_name($p_month);

    return "$p_day $mois $p_year";

}

//////////////////////////////////////////////////////////////////////////
// Show a date in mysql format (for query)
//  params : $p_date : date in yyyymmdd format
//////////////////////////////////////////////////////////////////////////
function dis_mysql_date($p_date) {
  global $cdg_sql;
    
    $p_year=substr($p_date,0,4);
    $p_month=substr($p_date,4,2);
    $p_day=substr($p_date,6,2);

    return "'$p_year-$mois-$p_day'";

}


</SCRIPT>
