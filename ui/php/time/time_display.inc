<SCRIPT language="php">

///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_display.php                                             //
//     - Desc : Time Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
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
// Display Time specific dataset fields                                      //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_time(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme;
  global $time; // ???? Pour quoi ???

  if (($fieldname == "timetask_id")
      && ($OD->data_set->f("timetask_status")==0) ) {
    $res["popup"] = true;
    $res["popup_width"] = 970;
    $res["popup_height"] = 270;
    $res["url"] = "time_index.php?action=detailupdate&amp;popup=1&amp;task_id=".$this->data_set->f("timetask_id")."&amp;param_begin=".$time["date"]."&amp;param_end=".$time["date_end"];
    $res["name"] = "<img border=\"0\" src=\"/images/$set_theme/modify.png\">";
  }

  else if ($fieldname == "task_id") {
    $res["name"] = "&nbsp;";
  }
  
  return $res;
}


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
  global $hum_date, $l_month2, $l_from, $l_to;
  // WEEK

  $user_id = $time["user_id"];

  if ($type == "week") {

    //get the current start and end of week
    $d_start_week = first_day_week($time);
    $d_end_week = $d_start_week+6*86400;

    // get the previous and next beginning of week
    $d_start_prev = $d_start_week-7*86400;
    $d_start_next = $d_start_week+7*86400; 
     
    // bcontins ???
    $hum_date_start = dis_human_date(date("Ymd", $d_start_week));
    $hum_date_end = dis_human_date(date("Ymd", $d_end_week));

    echo "
      <center> <font size=-2>
         <table border=1>
            <tr><td class=\"timeLinks\" align=\"left\">
               <a href=\"" .
                  url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=".date("Ymd",$d_start_prev))."\">
               &lt;&nbsp;".$l_week_prev."
               </a>
            </td>
            <td class=\"timePeriod\">&nbsp;
	      $l_week $l_from $hum_date_start $l_to $hum_date_end
              &nbsp;</td>
            <td class=\"timeLinks\" align=\"right\">
               <a href=\"" .
                  url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=".date("Ymd",$d_start_next))."\">
               ".$l_week_next."&gt;&nbsp;
               </a>
            </td></tr>
         </table>    
      </font> </center>    
        ";

  } else if ($type == "month") {
    if (debug_level_isset($cdg_param)) {
      echo "dis_time_links \$time <br>";
      debug_array($time);
    }

    if (debug_level_isset($cdg_param)) {
      echo "DIS_TIME_LINKS <br>";
      echo "date ". $time["date"] . " <br>";
    }

    // This should work for january also (month 0 = december) 
    $month=substr($time["date"],4,2);
    $year=substr($time["date"],0,4);

    $d_start_prev = date("Ymd", mktime(0,0,0,$month-1,1,$year));
    $d_start_next = date("Ymd", mktime(0,0,0,$month+1,1,$year));

    if (debug_level_isset($cdg_param)) {
      echo "previous $d_start_prev, next $d_start_next, <br>";
    }

    //Daylight Savings Time
    // nothing to do for this

    echo "
      <center> <font size=-2>
         <table border=1>
            <tr><td class=\"timeLinks\" align=\"left\">
               <a href=\"" .
                  url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=$d_start_prev")."\">
               &lt;&nbsp;".$l_month_prev."
               </a>
            </td>
            <td class=\"timePeriod\">
	      &nbsp;&nbsp;&nbsp;$l_month2 ". month_name($month) ."&nbsp;&nbsp;&nbsp;
            </td>
            <td class=\"timeLinks\" align=\"right\">
               <a href=\"" .
                  url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=$d_start_next")."\">
               ".$l_month_next."&gt;&nbsp;
               </a>
            </td></tr>
         </table>    
      </font> </center>    
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
    $sel_user_id = $time["user_id"];
  
    // UserObm Select
    $sel_user = "<select name=\"sel_user_id\" size=6>";
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
	  $act = "viewmonth";

    echo "<br>
      <center>
      <form method=post name=f_time action=\"" .
      url_prepare("time_index.php") . "\">
      <table border=1>
      <tr>
        <td><font size=-2> $l_user </font></td>
        <td align=left>
        $sel_user
        </td>
        <td valign=bottom>&nbsp;&nbsp;
          <input type=hidden name=wbegin value=\"".$time["date"]."\">
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

  //bcontins task list is no more a display
  //$pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
  //echo "IN dis_time_list <br>";
  // obm_q contains the time events for a user and an interval
  // bcontins what's that for ?
  $task_q = run_query_search($time, $new_order, $order_dir);
  $nb_task = $task_q->num_rows();

  $pref_day_q = run_query_display_pref($auth->auth["uid"], "time_day");

  // array of validated days
  $val_days = run_query_valid_search($time);
  if (debug_level_isset($cdg_param)) {
    echo "validated days : ".sizeof($val_days)." <br>";
    print_r($val_days);
  }

  // Creating the dates for the selected (or current) date
  // format "Mon 25 May"
  $d_start_week = first_day_week($time);  
  $day_q = get_this_week($d_start_week, $c_days_in_a_week);
  
  if (debug_level_isset($cdg_param)) {
    echo "list of days <br>";
    print_r($day_q);
  }

  // Get elements for insertion of new task
  // TaskType
  $obm_tasktype_q = run_query_tasktype($time["user_id"]);
  // Project
  $obm_project_q = run_query_project($time);
  // ProjectTask
  $obm_projecttask_q = run_query_projecttask($time);

  // dis_form_addtask does not display anything if days are full
  //   and validated
  dis_form_addtask("insert", 
                   $obm_project_q, 
                   $obm_projecttask_q, 
                   $obm_tasktype_q, 
                   $day_q,
                   $c_day_fraction,
                   $time, 
                   $val_days); 
 
  if ($nb_task == 0) {
    dis_table_title($l_no_found) ;
  }
  else {
    //      $names = run_query_get_names($time["user_id"]);
    //      $names->next_record();
      
    // Don't display task list if 
    //   - period is validated (each day is filled and validated)
    //   - show_task_detail is false

    // bcontins display task list
   dis_time_list_result($task_q, $pref_q, $time);
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
  
  if ($time["interval"] == "month") {

    $pref_month_task_q = run_query_display_pref($auth->auth["uid"], "time_task_month");

    // If $action=globalview, show all users
    if ($time['action'] == "globalview")
      $obm_month_task_q = run_query_task_one_month($time, 'all');
    else
      $obm_month_task_q = run_query_task_one_month($time, '');
  
    // Task for the month
    if ($time['action'] == "globalview")
      dis_time_one_month($obm_month_task_q, $pref_month_task_q, $time, true);
    else
      dis_time_one_month($obm_month_task_q, $pref_month_task_q, $time);

  }
  else {
    $obm_week_task_q = run_query_task_one_week($time, '');

    dis_time_one_week($obm_week_task_q, $time);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display the Stats TimeManager result page                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
// function dis_time_stats($time) {
//   global $l_no_found, $cdg_param;
//   global $auth, $new_order, $order_dir, $new_order2;
//   global $status_id; // "Signed" status for deals
//   global $fraction_jour; // nbre de fraction possible d'une journée
//   global $days_in_a_week; // how many days in a week have to be filled (5)
//   $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
//   //echo "IN dis_time_list <br>";
//   // obm_q contains the time events for a user and an interval
// //  $obm_q = run_query_search($time, $new_order, $order_dir, $new_order2);
// //  $nb_task = $obm_q->num_rows();

//   $pref_day_q = run_query_display_pref($auth->auth["uid"], "time_day");
//   $obm_day_q = run_query_by_day($time, $new_order, $order_dir, $new_order2);

//   // OLD
//   //  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
//   //  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir, $new_order2);

//   $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
//   $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir);
//   $obm_stats_q = run_query_stats_by_deal($time, $new_order, $order_dir);

//   // Get elements for insertion of new task
//   // TaskType
//   $obm_tasktype_q = run_query_tasktype();
//   // Project
//   $obm_project_q = run_query_project();

//   //get the current start and end of week
//   $d_start_week = first_day_week($time);   
  
//   $date_start = date("Ymd",$d_start_week);
//   $date_end = date("Ymd",$d_start_week + (6*86400));
     
//   $hum_date_start = dis_human_date($date_start);
//   $hum_date_end = dis_human_date($date_end);

//   dis_time_period($time);

//   // array of validated days
//   $val_days = run_query_valid_search($time);
//   if (debug_level_isset($cdg_param)) {
//     echo "jours validés : ".sizeof($val_days)." <br>";
//     print_r($val_days);
//   }

//   dis_time_menu($time);

//   // Creating the dates for the selected (or current) date
//   // format "Mon 25 May"
//   $day_q = get_this_week($d_start_week, $days_in_a_week);
  
//   if (debug_level_isset($cdg_param)) {
//     echo "liste des jours <br>";
//     print_r($day_q);
//   }


//   dis_time_deal_list($obm_deal_q,$pref_deal_q,$nb_task,$time);
//   dis_time_deal_list($obm_stats_q,$pref_deal_q,$nb_task,$time);

  
// }

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
  //  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir);
  $obm_stats_q = run_query_stats_by_deal($time, $new_order, $order_dir);

  //  dis_time_deal_export($obm_deal_q,$pref_deal_q,$nb_task,$time);
  dis_time_deal_export($obm_stats_q,$pref_deal_q,$nb_task,$time);

  
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
function dis_time_list_result($task_q,$pref_q,$time) {
  //-- Themes
  global $set_theme,$set_date,$ico_mail;
  //-- Labels
  global $l_date_task, $l_company, $l_deallabel, $l_tasklabel, $l_tasktype;
  global $l_found, $_task_del, $l_delete, $l_task_del, $l_found_task;
  global $l_update, $l_length;

  dis_table_title($l_found_task) ;

  // bcontins obsolete inutile
  //echo "<font color=\"#$col_textem\">$nb_task $l_found</font><br>";

  $url = url_prepare("time_index.php?action=index&amp;wbegin=".$time["date"]);
		     //"&amp;param_end=".$time["date_end"].
					//"&amp;user_id=".$time["user_id"].
					//"&amp;tf_week=".urlencode($week).
		     //	"&amp;st_detail=".$time["show_task_detail"]);

  // on boucle sur les taches (s
  $tab_tasks = "<table width=95% cellspacing=0>";

  $tab_tasks .= "<th size=5% class=\"timeTask\">$l_task_del</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_date_task</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_company</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_deallabel</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_tasklabel</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_tasktype</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_length</td>\n";
  $tab_tasks .= "<th class=\"timeTask\">$l_update</td>\n";

  while ($task_q->next_record()) {

    $status = $task_q->f("timetask_status");

    $taskid =  $task_q->f("Id");
    $date = $task_q->f("date_task");
    $date = date("$set_date", $date);
    $company = $task_q->f("timetask_company_name");
    $deal = $task_q->f("timetask_deal_label");
    $task = $task_q->f("timetask_label");
    $tt = $task_q->f("tasktype_label");
    $length = $task_q->f("timetask_length");

    if ($status != 0)
      $tab_tasks .= "<tr class=\"timeValid\">\n";
    else
      $tab_tasks .= "<tr class=\"timeUnvalid\">\n";

    if ($status != 2)
      $tab_tasks .= "<td><input id=delete_$taskid type=checkbox name=delete_$taskid></td>\n";
    else
      $tab_tasks .= "<td>&nbsp;</td>\n";

    $tab_tasks .= "<td class=\"timeTask\">$date</td>\n";
    $tab_tasks .= "<td class=\"timeTask\">$company</td>\n";
    $tab_tasks .= "<td class=\"timeTask\">$deal</td>\n";
    $tab_tasks .= "<td class=\"timeTask\">$task</td>\n";
    $tab_tasks .= "<td class=\"timeTask\">$tt</td>\n";
    $tab_tasks .= "<td class=\"timeTask\">$length</td>\n";

    if ($status != 2) {
      $popup_width = 570;
      $popup_height = 270;
      $url = "time_index.php?action=update&amp;popup=1&amp;task_id=$taskid&amp;wbegin=".$time["date"];

      $tab_tasks .= "<td class=\"timeTask\" align=center><a href=\"\" OnClick=\"window.name='root';window.open('$url','',config='height=$popup_height,width=$popup_width,scrollbar=no,toolbar=no,menubar=no,resizable=no'); return false;\">";
      $tab_tasks .= "<img border=0 src=\"/images/$set_theme/modify.png\"></a><td>\n";
    }
    else 
      $tab_tasks .= "<td>&nbsp;</td>\n";

  }

  echo "<center>";
  echo "<form method=post name=f_task_del action=\"".url_prepare("time_index.php").
    "\" \"onSubmit=\"if (confirm_del()) return true; else return false\" >

  
  $tab_tasks
  </table>\n";

  echo "
    <br/>
    <input type=\"hidden\" name=\"user_id[]\" value=\"".$time["user_id"][0]."\">
    <input type=\"hidden\" name=\"wbegin\" value=\"".$time["date"]."\">
    <input type=\"hidden\" name=\"action\" value=\"delete\">
    <input name=\"submit\" type=\"submit\" value=\"".$l_delete."\" >
    </form>
       ";
}


// bcontins obsolete va changer
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
  global $set_lang,$set_theme,$ico_mail;
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

   $dis_task_list->display("dis_data_time");

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
  global $set_theme;
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
  
  $dis_task_list->display("dis_data_time");


  echo "<table>";
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
  global $set_theme;
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
  global $set_theme;
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
  $day=1;

  // bcontins ca sert à rien
  //   $day=intval(substr($time["date"],6,2));

  // first day of the month in second
  $first_day_sec = mktime(0,0,0,$month, 1, $year);
  $time["date"] = date("Ymd", $first_day_sec);

  // First Weekday
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
      echo "<br/>\n";
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
  global $col_orange, $col_blue;
  global $l_total, $l_fill;
  global $l_workingdays2, $l_total_user_msg1, $l_total_days_msg3, $l_total_days_msg4; 

  //  $col = "b2ebe8";
  //  $col_red="a00000";
  /*  echo "<br> res ";
  print_r($array_res); */
  $col_width=20;
  $month=substr($time["date"],4,2);
  $year=substr($time["date"],0,4);
  $day=substr($time["date"],6,2);
  
  // Display the array
  while (list($key,$val) = each($array_res)) {
    if ($working_days[$val["wday"]] == 0) {
      $line1 .= "<td width=$col_width class=\"timeOffdayHead\">$key</td>\n";
      $line2 .= "<td class=\"timeOffday\">&nbsp;</td>\n";
      $line3 .= "<td class=\"timeOffday\">&nbsp;</td>\n";
    }
    else {
      $cell_1 = "<td width=$col_width class=\"timeWorkdayHead\" >$key</td>\n";
      //       $cell_1 = "<td size=$col_width class=\"OBMDisplayPref\">$key</td>";

      //       $cell_1_short = $cell_1;

      if ($val["length"] == $c_day_fraction)
        $line2 .= "<td class=\"timeWorkday\">OK</td>\n";
      else
	$line2 .= "<td class=\"timeWorkday\" bgcolor=\"#$col_orange\">".$val["length"]."/$c_day_fraction</td>\n";
      
      // first day of the week 
      if ($val["wday"] == 1) {
	// return first and last day of the week
	$day = $key < 10 ? "0".$key : $key; 
	$week = get_week_point($year.$month.$day);
	$url= url_prepare("time_index.php?action=index".
			  "&amp;wbegin=".$week[0]);
        $line3.="<td colspan=\"$c_days_in_a_week\"><a href=\"$url\">$l_fill</a></td>";
	

	//         $cell_1_short="<td width=$col_width align=center><a href=\"$url\">$key</a></td>";
      }
      //or first day of the month
      else if ($key == 1) {
	// return first and last day of the week
	$week = get_week_point($year.$month."01");
	$url= url_prepare("time_index.php?action=index".
			  "&amp;wbegin=".$week[0]);

	// This week is not $c_days_in_a_week long
	$week_length = $c_days_in_a_week - $val["wday"] +1;
	$line3.="<td colspan=\"$week_length\"><a href=\"$url\">$l_fill</a></td>";
	$cell_1_short="<td width=$col_width align=center><a href=\"$url\">$key</a></td>";
      }
      
      //       if ($shortview)
      // 	$line1.=$cell_1_short;
      //       else
      $line1.=$cell_1;
    }
  }
  
  // we display a recap for this user
  if ($time["interval"] == "month") {
    $fract = $total/$c_day_fraction;
    $tot_workingdays = get_nb_working_days($time["date"]);
    
    $last_cell_1 = "<td>&nbsp;&nbsp;</td><td width=60 align=center>$l_total</td>";
    $bg_color= $fract != $tot_workingdays ? "bgcolor=\"#$col_blue\"" : "";
    $dis_tot= $fract != $tot_workingdays ? "<b>$fract / $tot_workingdays</b>" : "$fract / $tot_workingdays";
    $last_cell_2 = "<td>&nbsp;&nbsp;</td><td $bg_color>$dis_tot</td>";
    $line1.=$last_cell_1;
    $line2.=$last_cell_2;
  }

  echo "<center><table border=1>
           <tr><td width=150 align=left rowspan=3><b>&nbsp;$title</b></td>
  $line1
  </tr><tr>
  $line2
  </tr>";

  if (! $shortview)
    echo "<tr>
  $line3
    </tr>";

  echo "       </table></center>";

  echo "$tab_total";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Deal  result                                        //
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_time_one_week($obm_dq, $time) {
  //-- Themes
  global $set_theme;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;
  global $c_day_fraction, $c_days_in_a_week;

  $obm_user_q = run_query_userobm($time["user_id"]);
  $obm_user_q->next_record();

  $userid = $obm_user_q->f("userobm_id");
  $f_name = $obm_user_q->f("userobm_firstname");
  $l_name = $obm_user_q->f("userobm_lastname");
  $user = $f_name . " " . $l_name;

  $length_tot = 0;

  while ($obm_dq->next_record())  {

    $date = $obm_dq->f("day_in_week");
    $year=substr($date,0,4);
    $month=substr($date,4,2);
    $day=substr($date,6,2);
    
    $day_in_sec = mktime(0,0,0,$month, $day, $year);
    $wday =date("w", $day_in_sec);

    $result[$wday] = $obm_dq->f("total_length");
    $length_tot += $obm_dq->f("total_length");
  }
  
  display_weekview($time, $result, $length_tot, $user);
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

function display_weekview($time, $result, $total, $title) {
  global $working_days;  // array of the days in a week where we are supposed to work
  global $c_day_fraction, $c_days_in_a_week;
  global $col_orange,$col_blue;
  global $l_total;
  global $l_fill, $l_daysofweekshort;
  global $l_workingdays2, $l_total_user_msg1, $l_total_days_msg3, $l_total_days_msg4; 

  //  $col = "b2ebe8";
  //  $col_red="a00000";
  /*  echo "<br> res ";
  print_r($array_res); */
  $col_width=25;
  
  $curr_day = first_day_week($time);
  $last_day = $curr_day + 7*86400;

  // Display the array
  while ($curr_day < $last_day) {
    
    $wday =date("w", $curr_day);
    $hum_wday = $l_daysofweekshort[$wday];

    if (!(isset($result[$wday])))
      $result[$wday]=0;

    if ($working_days[$wday] == 0) {
      $line1 .= "<td width=$col_width class=\"timeOffdayHead\">";
      $line1 .= "$hum_wday</td>\n";
      $line2 .= "<td class=\"timeOffday\">&nbsp;</td>\n";
    }
    else {
      $line1 .= "<td width=$col_width class=\"timeWorkdayHead\" >";
      $line1 .= "$hum_wday</td>\n";

      if ($result[$wday] == $c_day_fraction)
        $line2 .= "<td class=\"timeWorkday\">OK</td>\n";
      else
	$line2 .= "<td class=\"timeWorkday\" bgcolor=\"#$col_orange\">".$result[$wday]."/$c_day_fraction</td>\n";
    }
    
    $curr_day += 86400;        
  }
  
  $line1 .= "<td width=$col_width>&nbsp;&nbsp;</td>";
  $line1 .= "<td width=60 align=center>$l_total</td>";
  //     $bg_color= $fract != $tot_workingdays ? "bgcolor=\"#$col_blue\"" : "";
  
  $fract = $total/$c_day_fraction;
  $dis_tot= "$fract/$c_days_in_a_week";

  $line2 .= "<td>&nbsp;&nbsp;</td>";
  $line2 .= "<td bgcolor=\"#$col_blue\" align=center>$dis_tot</td>";

  echo "<center><table border=1 width=50%>
           <tr><td width=150 align=left rowspan=3><b>&nbsp;$title</b></td>
  $line1
  </tr><tr>
  $line2
  </tr>";

  echo "       </table></center>";
  echo "<p></p>";

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

function dis_form_addtask($action, $project_q, $projecttask_q, $taskt_q,
                          $day_q, $fraction_t, $time, $validate_days) {

  global $auth, $time, $fraction_jour ,$task_id_modify;
  global $l_addtask_start, $l_addtask_end, $l_add, $task_list, $l_none;
  global $l_label,$l_date,$l_project,$l_duration,$l_task, $l_update, $l_close;
  //for list classement of task
  global $l_task_fac,$l_task_div,$l_task_int, $l_intern;
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

  }

  if (sizeof($validate_days) == $c_days_in_a_week) {
    echo "<p></p>";
    return;
  }
  
  $task_list  = array($l_task_fac, $l_task_div, $l_task_int);
  
  // bcontins faut voir a quoi ca sert
  //get different attribute for task to be modify
  if ($action == "update") {
    $task_id = $time["task_id"];
    $task_to_be_modify = run_query_get_task($task_id);
    $is_day = substr($task_to_be_modify->f("timetask_date"),0,8);
    $is_tt = $task_to_be_modify->f("timetask_tasktype_id");
    $is_proj = $task_to_be_modify->f("timetask_project_id");
    $is_projtask = $task_to_be_modify->f("timetask_projecttask_id");
    $is_time = $task_to_be_modify->f("timetask_length");
  }

//   // Day check construction
  if ($action == "update") {
    for ($day=0;$day< sizeof($day_q);  $day++) {
      $dlabel = $day_q[$day][0];
      $rd_day .= "<input type=\"radio\" name=\"rd_day\" value=\"".$day_q[$day][1]."\"";
      
      if ($day_q[$day][1] == $is_day)
	$rd_day .= "checked";
      
      $rd_day .= ">$dlabel\n";
    }
  }
  
  // Day check construction
  else {
    for ($day=0;$day< sizeof($day_q);  $day++) {
      if(sizeof($validate_days)>0 && isset($validate_days[$day_q[$day][1]])) continue;
      $dlabel = $day_q[$day][0];
      $ch_day .= "<input type=\"checkbox\" name=\"cb_day[".$day_q[$day][1]."]\"";
      
      if ($day_q[$day][1] == date("Ymd"))
	$ch_day .= "checked";
      
      $ch_day .= ">$dlabel\n";
    }
  }


  // data array for the project dynamic list construction
  // the generated array is used by fill_project()
  $previous_tt = 0;
  $proj_data = "\nproject = new Array (";

  while($project_q->next_record()) {
    $proj_tt  = $project_q->f("deal_tasktype_id");
    $proj_id = $project_q->f("deal_id");
    
    $company = $project_q->f("company_name");

    if ($company == "") $company = $l_intern;

    $proj_label = "\" $company -- ". $project_q->f("deal_label"). "\"";

    if ($previous_tt != $proj_tt) {
      if ($previous_tt != 0) {
        $proj_data .= "\n),";
      }

      $proj_data .= "\nnew Array (\n". $proj_tt .",\n";
      $previous_tt = $proj_tt;
    }
    else {
      $proj_data .= ",\n";
    }

    $proj_data .= "new Array(". $proj_label .", ". $proj_id .")";

  }

  $proj_data .= "\n)\n);\n\n";

  $script = "\n\n<script language=\"JavaScript\">\n";
  $script .= $proj_data;
  $script .= "\n</script>\n\n";


  // data array for the projecttask dynamic list construction
  // the generated array is used by fill_project()
  $previous_dl = 0;
  $projtask_data = "\nprojecttask = new Array (";

  while($projecttask_q->next_record()) {
    $projtask_dl  = $projecttask_q->f("projecttask_deal_id");
    $projtask_id = $projecttask_q->f("projecttask_id");
    
    $projtask_label = "\"". $projecttask_q->f("projecttask_label") ."\"";

    if ($previous_dl != $projtask_dl) {
      if ($previous_dl != 0) {
        $projtask_data .= "\n),";
      }

      $projtask_data .= "\nnew Array (\n". $projtask_dl .",\n";
      $previous_dl = $projtask_dl;
    }
    else {
      $projtask_data .= ",\n";
    }

    $projtask_data .= "new Array(". $projtask_label .", ". $projtask_id .")";

  }

  $projtask_data .= "\n)\n);\n\n";

  $script2 = "\n\n<script language=\"JavaScript\">\n";
  $script2 .= $projtask_data;
  $script2 .= "\n</script>\n\n";


  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=sel_tasktype ";
  $sel_tt .= "  onchange=\"fill_project(this.form.sel_project, this.form.sel_tasktype.value,";
  $sel_tt .= "  this.form.sel_projecttask)\">";
  $sel_tt .= "<option value=\"0\">$l_none</option>";
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
  $sel_proj = "<select name=sel_project onchange=\"fill_projecttask(this.form.sel_projecttask, this.form.sel_project.value)\">";
  $sel_proj .= "<option value=\"0\">$l_none</option>";
  $sel_proj .= "</select>";

  // ProjectTask select construction
  $sel_projtask = "<select name=sel_projecttask>";
  $sel_projtask .= "<option value=\"0\">$l_none</option>";
  $sel_projtask .= "</select>";

  // Project Label
   if ($action == "update")
     $tf_label ="<input type=\"text\" name=\"tf_label\" size=30 value=\"".$task_to_be_modify->f("task_label")."\" />";
   else
     $tf_label ="<input type=\"text\" name=\"tf_label\" size=30 />";

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
  echo "$script";
  echo "$script2";
  echo "<p></p>";

  if ($action == "insert") {
    dis_table_title($l_addtask_start.$fraction_jour.$l_addtask_end) ;
    echo "
      <form method=post name=form_add_task
        onSubmit=\"if (check_form(this)) return true; else return false;\" 
        action=\"".url_prepare("time_index.php")."\">\n
      <input type=\"hidden\" name=\"action\" value=\"$action\">\n
      <input type=\"hidden\" name=\"popup\" value=0>\n";
  }
  else {
    dis_table_title($l_update_task);
    echo "
      <form method=post  name=form_add_task 
        onSubmit=\"if (check_form(this)) return true; else return false;\"
        action=\"".url_prepare("time_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"update\">
      <input type=\"hidden\" name=\"popup\" value=0>";
  }

  echo "
  <center> 
  <table border=0 cellspacing=0 width=95%>
   <tr class=\"timeHeader\">";

  // bcontins garder un seul ?
  if ($action != "update")
    echo "<td>".col_table($col_textem, $l_date)."<br>$ch_day</td>";
  else
    echo "<td>".col_table($col_textem, $l_date)."<br>$rd_day</td>";

  echo "<td>".col_table($col_textem, $l_duration)."<br>$sel_time</td>
     <td>".col_table($col_textem, $l_task)."<br>$sel_tt</td>
    </tr>
    <tr class=\"timeHeader\">
     <td colspan=2>".col_table($col_textem, $l_project)."<br>$sel_proj</td>
     <td>".col_table($col_textem, $l_label)."<br>$sel_projtask</td>
    </tr>
    <tr class=\"timeHeader\">
     <td colspan=\"4\">".col_table($col_textem, $l_label)."<br>$tf_label</td>
    </tr>
  </table>";

  if ($action == "insert") {
    echo "<br><input type=submit value=\"$l_add\">";
  } 
  else {
    echo "<br><input type=hidden name=task_id value=\"$task_id\">
         <input TYPE=submit value=\"$l_update\">
     <input type=submit value=\"$l_close\" onclick=\"window.close();\">";
  }     
  
  echo "</center>
    <input type=hidden name=wbegin value=\"".$time["date"]."\">\n";
  
  if ($action == "update"){
    echo "<script language=\"JavaScript\">\n";
//     echo "alert(document.form_add_task.sel_tasktype.value);";
    echo "fill_project(document.form_add_task.sel_project,";
    echo " document.form_add_task.sel_tasktype.value,";
    echo " document.form_add_task.sel_projecttask);\n";
    echo "fill_projecttask(document.form_add_task.sel_projecttask, $is_proj);";
    echo "select_default(document.form_add_task.sel_project, $is_proj);\n";
    echo "select_default(document.form_add_task.sel_projecttask, $is_projtask);\n";
    echo "</script>\n";
  }
  
  echo "</FORM><p></p>";
}


function col_table($col, $data) {
   return "<font color=\"#$col\">$data</font>";
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Time search form with params                                  //
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
