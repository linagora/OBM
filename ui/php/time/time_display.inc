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
$fieldnames["deal_label"] = $l_project;
$fieldnames["tasktype_label"] = $l_tasktype;
$fieldnames["timetask_label"] = $l_tasklabel;
$fieldnames["company_name"] = $l_company;
$fieldnames["total_length"] = $l_total_period;
$fieldnames["total_spent"] = $l_total_spent;
$fieldnames["total_before"] = $l_total_before;
$fieldnames["total_after"] = $l_total_after;

$fieldnames["date_task"] = $l_date_task;
$fieldnames["timetask_deal_label"] = $l_deallabel;
$fieldnames["timetask_company_name"] = $l_company;
$fieldnames["timetask_length"] = $l_length;


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
  global $time;

  $res["style"] = "";
  
  if (($fieldname == "row_style") && ($OD->data_set->f("timetask_id"))) {
    $parity = $OD->data_set->f("date_parity") % 2;

    if ($OD->data_set->f("timetask_status") == 0)
      $res["style"] = "class=\"timeUnvalid\"";
    else
      $res["style"] = "class=\"timeValid$parity\"";
  }

  if ($fieldname == "timetask_deal_label") {
    $label = ($OD->data_set->f("timetask_deal_label"));

    if (strlen($label) > 30)
      $res["name"] = substr($label,0,30) . "...";
  }

  else if ($fieldname == "timetask_id") {

    if ($OD->data_set->f("test_status") != 0) {
      $res["popup"] = true;
      $res["popup_width"] = 700;
      $res["popup_height"] = 210;
      $res["url"] = "time_index.php?action=detailupdate&amp;popup=1&amp;task_id=".$OD->data_set->f("timetask_id")."&amp;wbegin=".$time["date"];
      $res["name"] = "<img border=\"0\" src=\"/images/$set_theme/modify.png\" alt=\"\" />";
    } else {
      $res["name"] = "&nbsp;";
    }
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

  echo "<br />BEGIN----- debug_array ------<br />";
  foreach($a as $key => $val) {
    if (is_null($val))
      echo "clé $key val -- NULL -- <br />";
    else if (is_array($val)) {
      echo "clé $key val ";
      print_r($val);
      echo " <br />";
    }
    else
      echo "clé $key val --$val-- <br />";
  }
  echo "_POST<br />";
  foreach($_POST as $key => $val) {
    if (is_array($val)) {
      echo "clé $key val ";
      print_r($val);
      echo " <br />";
    }
    else
      echo "clé $key val --$val-- <br />";
  }

  echo "----- debug_array ------END<br />";
}


///////////////////////////////////////////////////////////////////////////////
// Display Changing Time Links                                              //
// Parameters:
//   - $time : array, with all pairs of value set by forms
//         with $time["date"] = current date 
//   - $type : "week", "month", "all"
//     default : all
///////////////////////////////////////////////////////////////////////////////
function dis_time_links ($time, $type="all") {
  global $cdg_param, $auth, $set_theme; 
  global $l_week, $l_week_prev,$l_week_next, $l_month_prev,$l_month_next;
  global $hum_date, $l_month2, $l_from, $l_to;
  // WEEK

  $action = $time["action"];

  if (($action=="globalview") or ($action=="validate") or ($action=="unvalidate")) {
    $user_id = $auth->auth["uid"];
    $time["user_id"] = $user_id;
  } else {
    $user_id = $time["user_id"];
  }

  $obm_user_q = run_query_userobm($user_id);
  $obm_user_q->next_record();

  $f_name = $obm_user_q->f("userobm_firstname");
  $l_name = $obm_user_q->f("userobm_lastname");

  $no_name = array("stats","globalview","validate","unvalidate");
  if (!(in_array($action, $no_name)))
    $user = $f_name . " " . $l_name . " :";

  if ($type == "week") {

    $tot_width = 500;

    //get the current start and end of week
    $d_start_week = first_day_week($time);
    $d_end_week = $d_start_week+6*86400;

    // get the previous and next beginning of week
    $d_start_prev = $d_start_week-7*86400;
    $d_start_next = $d_start_week+7*86400; 
     
    $hum_date_start = dis_human_date(date("Ymd", $d_start_week));
    $hum_date_end = dis_human_date(date("Ymd", $d_end_week));

    $block = "
      <div class=\"timeCenter\">
       <table class=\"timeLinks\" width=\"$tot_width\">
        <tr>
         <td align=\"left\">
          <a href=\"" .
      url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=".date("Ymd",$d_start_prev))."\">
           <img border=\"0\" src=\"/images/$set_theme/previous.png\" alt=\"\" />
          </a>
         </td>
         <td class=\"timePeriod\">
           $user $l_from $hum_date_start $l_to $hum_date_end
         </td>
         <td align=\"right\">
          <a href=\"" .
      url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=".date("Ymd",$d_start_next))."\">
           <img border=\"0\" src=\"/images/$set_theme/next.png\" alt=\"\" />
          </a>
         </td>
        </tr>
       </table>    
      </div>
      ";

  } else if ($type == "month") {
    if (debug_level_isset($cdg_param)) {
      echo "dis_time_links \$time <br />";
      debug_array($time);
    }

    if (debug_level_isset($cdg_param)) {
      echo "DIS_TIME_LINKS <br />";
      echo "date ". $time["date"] . " <br />";
    }

    $tot_width = (in_array($action, $no_name)) ? 200 : 400;

    // This should work for january also (month 0 = december) 
    $month=substr($time["date"],4,2);
    $year=substr($time["date"],0,4);

    $d_start_prev = date("Ymd", mktime(0,0,0,$month-1,1,$year));
    $d_start_next = date("Ymd", mktime(0,0,0,$month+1,1,$year));

    if (debug_level_isset($cdg_param)) {
      echo "previous $d_start_prev, next $d_start_next, <br />";
    }

    //Daylight Savings Time
    // nothing to do for this width=\"$tot_width\"

    $block = "
     <div class=\"timeCenter\">
       <table class=\"timeLinks\" width=\"$tot_width\">
        <tr><td class=\"timeLinks\" align=\"left\">
         <a href=\"" .
      url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=$d_start_prev")."\">
           <img border=\"0\" src=\"/images/$set_theme/previous.png\" alt=\"\" />
         </a>
        </td>
        <td class=\"timePeriod\">
         $user $l_month2 ". month_name($month) ."
        </td>
        <td class=\"timeLinks\" align=\"right\">
         <a href=\"" .
      url_prepare("time_index.php?action=".$time["action"].
		  "&amp;wbegin=$d_start_next")."\">
           <img border=\"0\" src=\"/images/$set_theme/next.png\" alt=\"\" />
         </a>
        </td></tr>
       </table>    
     </div>
     ";    
  }

  return $block;
}

 
///////////////////////////////////////////////////////////////////////////////
// Display TimeManager User search Form                                      //
// Parameters:
//   - $time[] : default form values
//     keys used  : week
//   - $usr_q  : DBO : UserObm list
//   - $uid    : current user id
///////////////////////////////////////////////////////////////////////////////
function dis_time_search_form ($time, $usr_q, $uid, $multi=0) {
  global $l_see, $l_week, $l_month, $project_managers, $l_user, $l_all;
  global $uid;

  $hum_date_start = dis_human_date($time["date"]);
  $hum_date_end = dis_human_date($time["date_end"]);

  if ($time["interval"] == "week")
    $period = $l_week;
  else if ($time["interval"] == "month")
    $period = $l_month;

  if (in_array($uid, $project_managers)) {
    $sel_user_id = $time["user_id"];

    if ($multi) {
      $size = 12;
      $type = "multiple";
      $alt = "[]";
    } else {
      $size = 6;
    }
  
    // UserObm Select
    $sel_user = "<select name=\"sel_user_id$alt\" $type size=\"$size\">";
    $selected = false;
    while ($usr_q->next_record()) {
      $u_id = $usr_q->f("userobm_id");
      $lname = $usr_q->f("userobm_lastname");
      $fname = $usr_q->f("userobm_firstname");
      $sel_user .= "\n<option value=\"$u_id\" ";

      if ($multi) {
	if (in_array($u_id,$sel_user_id))
	  $sel_user .= "selected=\"selected\"";
      } else {
	if ($sel_user_id == $u_id)
	  $sel_user .= "selected=\"selected\"";
      };

      $sel_user .= " >$lname $fname</option>";
    }
    $sel_user .= "</select>";

    // Define the action to follow
    if ($time["action"] == "stats")
      $act = "stats";
    else if ($time["action"] == "viewmonth")
      $act = "viewmonth";
    else 
      $act = "index";

    if ($multi)
      $dis_button_all = "<input type=\"submit\" name=\"submit\" value=\"$l_all\"
                                onclick=\"select_all(f_time)\" />";

    $block = "<br />
      <div class=\"timeBar\">
     <form method=\"post\" name=\"f_time\" action=\"" .
      url_prepare("time_index.php") . "\">

       <table class=\"timeUser\">
        <tr>
         <td class=\"timeUserhead\">$l_user</td>
        </tr><tr>
         <td class=\"timeUserback\">$sel_user</td>
        </tr><tr>
         <td class=\"timeUserback\">
          <input type=\"hidden\" name=\"wbegin\" value=\"".$time["date"]."\" />
          <input type=\"hidden\" name=\"action\" value=\"$act\" />
          <input type=\"submit\" name=\"submit\" value=\"$l_see\"/>
          $dis_button_all
         </td>
        </tr>
       </table>
     </form>
      </div>
";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the full TimeManager result page                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_list($time) {
  global $l_no_found, $cdg_param;
  global $auth, $new_order, $order_dir, $display;
  global $status_id; // "Signed" status for deals
  global $c_day_fraction; // nbre de fraction possible d'une journée
  global $c_days_in_a_week; // how many days in a week have to be filled (5)

  $task_q = run_query_search($time);
  $nb_task = $task_q->num_rows();

  $pref_q = run_query_display_pref($auth->auth["uid"], "time");

  // array of validated days
  $val_days = run_query_valid_search($time);
  if (debug_level_isset($cdg_param)) {
    echo "validated days : ".sizeof($val_days)." <br />";
    print_r($val_days);
  }

  // Creating the dates for the selected (or current) date
  // format "Mon 25 May"
  $d_start_week = first_day_week($time);  
  $day_q = get_this_week($d_start_week, $c_days_in_a_week);
  
  if (debug_level_isset($cdg_param)) {
    echo "list of days <br />";
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
  $block .= dis_form_addtask("insert", 
			     $obm_project_q, 
			     $obm_projecttask_q, 
			     $obm_tasktype_q, 
			     $day_q,
			     $c_day_fraction,
			     $time, 
			     $val_days); 
 
  if ($nb_task != 0) {
    $block .= dis_time_list_result($task_q, $pref_q, $time);
  }

  return $block;
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
    if (($time["action"] == "globalview") or
        ($time["action"] == "validate") or
        ($time["action"] == "unvalidate")) {
      $obm_short_q = run_query_short_month($time);
      $block = dis_short_month($obm_short_q, $time);
    } else {
      $obm_month_task_q = run_query_task_one_month($time);
      $block = dis_time_one_month($obm_month_task_q, $time);
    }

  }
  else {
    $obm_week_task_q = run_query_task_one_week($time, '');

    $block = dis_time_one_week($obm_week_task_q, $time);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Stats TimeManager result page                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_statsuser($statproj_q, $stattt_q, $time) {
  global $l_no_found, $cdg_param;
  global $auth, $new_order, $order_dir, $new_order2;
  global $l_proj_stats, $l_no_proj_stats;
  global $l_tt_stats, $l_no_tt_stats;

  if ($statproj_q->num_rows() != 0) {
    $pref_pq = run_query_display_pref($auth->auth["uid"], "time_proj");

    $dis_projuser_list = new OBM_DISPLAY("DATA",$pref_pq);
    $dis_projuser_list->data_set = $statproj_q;
    $dis_projuser_list->data_order = false;
    $dis_projuser_list->data_page = false;
    $dis_projuser_list->data_header = "top";

    $block .= "<div class=\"detailHead\">$l_proj_stats</div>";
    $block .= $dis_projuser_list->display("dis_data_time");
  } else {
    $block .= display_warn_msg($l_no_proj_stats);
  }

  if ($stattt_q->num_rows() != 0) {
    $pref_tq = run_query_display_pref($auth->auth["uid"], "time_tt");

    $dis_ttuser_list = new OBM_DISPLAY("DATA",$pref_tq);
    $dis_ttuser_list->data_set = $stattt_q;
    $dis_ttuser_list->data_order = false;
    $dis_ttuser_list->data_page = false;
    $dis_projuser_list->data_header = "top";

    $block .= "<div class=\"detailHead\">$l_tt_stats</div>";
    $block .= $dis_ttuser_list->display("dis_data_time");
  }else {
    $block .= display_warn_msg($l_no_tt_stats);
  }
  
  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Export the Stats TimeManager result in a file                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
// function dis_time_export_stats($time) {

//   global $auth;
//   global $status_id; // "Signed" status for deals
//   global $fraction_jour; // nbre de fraction possible d'une journée
//   global $days_in_a_week; // how many days in a week have to be filled (5)
//   $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  
//   $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
//   $obm_stats_q = run_query_stats_by_deal($time, $new_order, $order_dir);

//   dis_time_deal_export($obm_stats_q,$pref_deal_q,$nb_task,$time);
// }


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
  global $l_update, $l_length, $l_task_list;

  //  dis_table_title($l_found_task) ;

  //oldstart
  //  $url = url_prepare("time_index.php?action=index&amp;wbegin=".$time["date"]);
 
  $block .= "
     <form method=\"post\" name=\"f_task_del\" action=\"".url_prepare("time_index.php").
    "\" \"onsubmit=\"if (confirm_del()) return true; else return false\" >
     ";

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_q);
  $dis_task_list->data_set = $task_q;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_page = false;
  //  $dis_task_list->data_url = $url; 
  $dis_task_list->data_cb_text = $l_task_del;
  $dis_task_list->data_idfield = "timetask_id"; 
  $dis_task_list->data_order = false; 
  $dis_task_list->data_cb_side = "left";
  $dis_task_list->data_cb_name = "delete_";
  $dis_task_list->data_cb_show = "test_status";

  $block .= "<div class=\"detailHead\">$l_task_list</div>\n";
  $block .= $dis_task_list->display("dis_data_time");

  $dis_button = "
    <input type=\"hidden\" name=\"user_id\" value=\"".$time["user_id"]."\" />
    <input type=\"hidden\" name=\"wbegin\" value=\"".$time["date"]."\" />
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input name=\"submit\" type=\"submit\" value=\"".$l_delete."\" />";

  $block .= "
     <p class=\"detailButton\">
      <p class=\"detailButtons\">$dis_button</p>
     </p>

     </form>
     ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Deal  result                                        //
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
function dis_time_one_month($obm_dq, $time) {
  //-- Themes
  global $set_theme;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;
  global $c_day_fraction, $c_days_in_a_week;

  $uid = $time["user_id"];
  //   $obm_user_q = run_query_userobm($uid);

  $year=substr($time["date"],0,4);
  $month=substr($time["date"],4,2);

  // first day of the month in second
  $day = 1;
  $first_day_sec = mktime(0,0,0,$month, 1, $year);
  $last_day_sec = mktime(23,0,0,$month+1, $day-1, $year);
  $first_wday =date("w", $day_in_sec);

  //   while($obm_user_q->next_record()) {
  //     $userid = $obm_user_q->f("userobm_id");
  //     $f_name = $obm_user_q->f("userobm_firstname");
  //     $l_name = $obm_user_q->f("userobm_lastname");
  //     $array_users["name"] = $f_name . " " . $l_name;
  //   }

  $day_in_sec = $first_day_sec;
  $wday =date("w", $day_in_sec);
  
  while ($day_in_sec < $last_day_sec) {
    $fday = ($day>9) ? "$day" : "0$day";
    $array_res["$fday"]["length"] = 0;
    $array_res["$fday"]["wday"] = $wday;
    $day++;
    $day_in_sec = mktime(0,0,0,$month, $day, $year);
    $wday =date("w", $day_in_sec);
  }
  
  $day_in_sec = $first_day_sec;
  
  while ($obm_dq->next_record())  {
    $day = $obm_dq->f("day_in_month");
    $status = $obm_dq->f("timetask_status");
    $array_res["$day"]["length"] = $obm_dq->f("total_length");

    $length_tot += $obm_dq->f("total_length");
  }

  $block .= display_monthview($time, $array_res, $length_tot, $status);
        
  return $block;
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

function display_monthview($time, $array_res, $total, $status) {
  global $working_days;  // array of the days in a week where we are supposed to work
  global $c_day_fraction, $c_days_in_a_week;
  global $col_orange, $col_blue;
  global $l_total, $l_fill, $l_see;
  global $l_workingdays2, $l_total_user_msg1, $l_total_days_msg3, $l_total_days_msg4; 

  $col_width=20;
  $time_length = 0;
  $time_worked = 0;
  $span = 0;
  $month_length = count($array_res);

  $month=substr($time["date"],4,2);
  $year=substr($time["date"],0,4);
  $day=substr($time["date"],6,2);

  //   $name = $title["name"];
  //   $status = $title["status"];

  //   $first_wday = $array_res["01"]["wday"];
  //   $span = $c_days_in_a_week - $first_wday + 1;
  
  // Display the array
  while (list($day,$val) = each($array_res)) {
    if ($working_days[$val["wday"]] == 0) {
      $line1 .= "<td width=$col_width class=\"timeOffday\">$day</td>\n";
      $line2 .= "<td class=\"timeOffday\">&nbsp;</td>\n";
      $line3 .= "<td class=\"timeOffday\">&nbsp;</td>\n";

    } else {
      $cell_1 = "<td width=$col_width class=\"timeWorkday\" >$day</td>\n";

      if ($val["length"] == $c_day_fraction)
        $line2 .= "<td class=\"timeValid\">".$val["length"]."/$c_day_fraction</td>\n";
      else
	$line2 .= "<td class=\"timeUnvalid\">".$val["length"]."/$c_day_fraction</td>\n";

      if ($val["wday"] == 1) {
	$time_length = 0;
	$time_worked = 0;
	$span = 1;
      } else {
	$time_length += $c_day_fraction;
	$time_worked += $val["length"];
	$span += 1;
      }
      
      // last day of the week 
      if (($val["wday"] == $c_days_in_a_week) or ($day == $month_length)) {
	// return first and last day of the week
	$week = get_week_point($year.$month.$day);
	$url= url_prepare("time_index.php?action=index".
			  "&amp;wbegin=".$week[0]);

	if ($status == 2)
	  $line3 .="<td class=\"timeValid\" colspan=\"$span\"><a href=\"$url\">$l_see</a></td>";
	else if ($time_length != $time_worked)
	  $line3 .="<td class=\"timeUnvalid\" colspan=\"$span\"><a href=\"$url\">$l_fill</a></td>";
	else
	  $line3 .="<td class=\"timeValid\" colspan=\"$span\"><a href=\"$url\">$l_fill</a></td>";
	
      }
      
      $line1.=$cell_1;
    }
  }
  
  $block = "
    <div class=\"timeCenter\">
    <table class=\"timeMonth\">
     <tr>
      $line1
     </tr>
     <tr>
      $line2
     </tr>
     <tr>
      $line3
     </tr>
    </table>
    <div>
   ";

  return $block;
}

//////////////////////////////////////////////////////////////////////////////
// HTML Display the Short View for one user :                               //
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
function dis_short_month($month_q, $time) {
  //-- Themes
  global $set_theme, $display;
  //-- Labels
  global $l_admin_empty;

  $cpt=0;
  
  if ($month_q->nf()==0)
    $block .= display_warn_msg($l_admin_empty);
  else {

    while ($month_q->next_record())  {
      $f_name = $month_q->f("userobm_firstname");
      $l_name = $month_q->f("userobm_lastname");
      $array_res[$cpt]["id"] = $month_q->f("userid");
      $array_res[$cpt]["name"] = $f_name . " " . $l_name;
      $array_res[$cpt]["status"] = $month_q->f("timetask_status");
      $array_res[$cpt]["length"] = $month_q->f("total_length");

      $cpt++;
    }

    $block .= display_shortview($time, $array_res);//, $array_order);
  }
        
  return $block;
}

//////////////////////////////////////////////////////////////////////////////
// HTML Display the Short View for one user :                               //
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
function display_shortview($time, $array_res) { //, $order) {
  global $set_theme, $working_days;  // array of the days in a week where we are supposed to work
  global $c_day_fraction, $c_days_in_a_week;
  global $col_orange, $col_blue;
  global $l_see, $l_fill, $l_valid, $l_unvalid, $l_admin_valid;

  $date = $time["date"];
  $tot_workingdays = get_nb_working_days($date);

  while (list($key) = each($array_res)) {	
    $uid = $array_res[$key]["id"];
    $user_title = $array_res[$key]["name"];
    $user_status = $array_res[$key]["status"];
    $user_total = $array_res[$key]["length"];

    // we display a recap for this user
    $fract = $user_total/$c_day_fraction;

    if ($user_status == 2) {
      $class= "timeValok";
      $dis_validlink = "
       <a href=\"". url_prepare("time_index.php?action=unvalidate&amp;wbegin=$date&amp;user_id=$uid") ."\">
       <img border=0 src=\"/images/$set_theme/non2.gif\" height=\"16\" width=\"16\" alt=\"\" /> $l_unvalid</a>";
      $link_act = "$l_see";
    } else if ($fract != $tot_workingdays) {
      $class= "timeValunfilled";
      $dis_validlink = "&nbsp;";
      $link_act = "$l_fill";
    } else {
      $class= "timeValfilled";
      $dis_validlink = "
       <a href=\"". url_prepare("time_index.php?action=validate&amp;wbegin=$date&amp;user_id=$uid") ."\">
       <img border=0 src=\"/images/$set_theme/oui2.gif\" height=\"16\" width=\"16\" alt=\"\" /> $l_valid</a>";
      $link_act = "$l_see";
    }

    $fract = ($fract != number_format($fract, 0)) ? number_format($fract, 1) : $fract;
    $dis_tot= ($fract != $tot_workingdays) ? "<b>$fract / $tot_workingdays</b>" : "$fract / $tot_workingdays";

    $dis_months .= "
     <tr>
      <td><b>&nbsp;$user_title</b></td>
      <td>&nbsp;&nbsp;</td>
      <td class=\"$class\">$dis_tot</td>
      <td>&nbsp;&nbsp;</td>
      <td>
       <a href=\"". url_prepare("time_index.php?action=viewmonth&amp;wbegin=$date&amp;user_id=$uid") ."\">
       <img border=0 src=\"/images/$set_theme/modify.png\" alt=\"\" /> $link_act</a>
      </td>
      <td>$dis_validlink</td>
     </tr>
     ";
  }

  $block = "
   <div class=\"detailHead\">$l_admin_valid</div>

   <div class=\"timeCenter\">
    <table class=\"timeValid\">
      $dis_months
    </table>
   <div>
   ";

  return $block;
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
  
  $block = display_weekview($time, $result, $length_tot, $user);

  return $block;
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

  $tot_width = 500;
  $col_width = $tot_width / $c_days_in_a_week;

  $curr_day = first_day_week($time);
  $last_day = $curr_day + 7*86400;

  // Display the array
  while ($curr_day < $last_day) {
    
    $wday =date("w", $curr_day);
    $hum_wday = $l_daysofweekshort[$wday];

    if (!(isset($result[$wday])))
      $result[$wday]=0;

    if ($working_days[$wday] != 0) {
      $line1 .= "<td width=\"$col_width\" class=\"timeWeekhead\" >";
      $line1 .= "$hum_wday</td>\n";
      
      if ($result[$wday] == $c_day_fraction)
        $line2 .= "<td class=\"timeWeekvalid\">$c_day_fraction/$c_day_fraction</td>\n";
      else
	$line2 .= "<td class=\"timeWeekunvalid\">".$result[$wday]."/$c_day_fraction</td>\n";
    }
    
    $curr_day += 86400;        
  }
  
  $block = "
   <div class=\"timeCenter\">
    <table class=\"timeLinks\" width=\"$tot_width\">
     <tr>
      $line1
     </tr>
     <tr>
      $line2
     </tr>
    </table>
   </div>
   ";

  return $block;
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
  global $l_addtask_start, $l_addtask_end, $l_add, $l_none;
  global $l_label,$l_date,$l_project,$l_duration,$l_task, $l_update, $l_close;
  global $l_fill_addtask, $l_daysofweekshort;
  //for list classement of task
  global $l_task_fac,$l_task_div,$l_task_int, $l_intern, $l_cat, $l_comment;
  global $c_days_in_a_week; // number of days to be displayed in a week

  // If length of $validate_days = length of week ($days_in_a_week) :
  //   => form not to be shown
  if (debug_level_isset($cdg_param)) {
    debug_array($time);
    echo "<br />day_q <br />";
    print_r($day_q);
    echo "<br />validate_days  <br />";
    print_r($validate_days);
    if (is_array($validate_days))
      echo "<br />sum validate_days " .array_sum($validate_days)."<br />";

  }

  if (sizeof($validate_days) == $c_days_in_a_week) {
    $block .= "<p></p>";
    return;
  }
  
  $task_list  = array($l_task_fac, $l_task_int, $l_task_div);
  
  //get different attribute for task to be modify
  if ($action == "detailupdate") {
    $task_id = $time["task_id"];
    $task_q = run_query_get_task($task_id);
    $is_day = substr($task_q->f("timetask_date"),0,8);
    $is_tt = $task_q->f("timetask_tasktype_id");
    $is_proj = $task_q->f("timetask_project_id");
    $is_projtask = $task_q->f("timetask_projecttask_id");
    $is_time = $task_q->f("timetask_length");
  }

  // Day radio select (for update)
  if ($action == "detailupdate") {
    for ($day=0;$day< sizeof($day_q);  $day++) {
      $dlabel = $day_q[$day][0];
      $rd_day .= "<input type=\"radio\" name=\"rd_day\" value=\"".$day_q[$day][1]."\"";
      
      if ($day_q[$day][1] == $is_day)
	$rd_day .= " checked=\"checked\"";
      
      $rd_day .= " />$dlabel\n";
    }
  }
  
  // Day check construction
  else {

    $day_checked = -1;
    for ($day=0;$day< sizeof($day_q);  $day++) {
      if ($day_q[$day][1] == date("Ymd"))
	$day_checked = $day;
    }

    for ($day=0;$day< sizeof($day_q);  $day++) {

      if(sizeof($validate_days)>0 && isset($validate_days[$day_q[$day][1]]))
	$disabled = 1;
      else
	$disabled = 0;
   
      // $d_label=$l_daysofweekshort[$day];
      $dlabel = $day_q[$day][0];
      $ch_day .= "<input type=\"checkbox\" name=\"cb_day[".$day_q[$day][1]."]\"";
      
      if ($disabled)
	$ch_day .= " disabled";
      else if ($day_checked == -1) {
	$ch_day .= " checked=\"checked\"";
	$day_checked = $day;
      } else if ($day == $day_checked)
	$ch_day .= " checked=\"checked\"";
      
      $ch_day .= " />$dlabel\n";
    }
  }

  // data array for the project dynamic list construction
  // the generated array is used by fill_project()
  if ($project_q->num_rows() != 0) {
    $previous_tt = 0;

    while($project_q->next_record()) {
      $proj_tt  = $project_q->f("deal_tasktype_id");
      $proj_id = $project_q->f("deal_id");

      $company = $project_q->f("company_name");

      if ($company == "") $company = $l_intern;

      $proj_name = htmlspecialchars(addslashes($project_q->f("deal_label")));
      $proj_name = (strlen($proj_name) > 35) ? substr($proj_name,0,35)."..." : $proj_name;

      $proj_label = "\" $company -- ". $proj_name. "\"";

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
    $proj_data .= "\n)\n";
  }

  // data array for the projecttask dynamic list construction
  // the generated array is used by fill_projecttask()
  if ($projecttask_q->num_rows() != 0) {
    $previous_dl = 0;

    while($projecttask_q->next_record()) {
      $projtask_dl  = $projecttask_q->f("projecttask_deal_id");
      $projtask_id = $projecttask_q->f("projecttask_id");
    
      $projtask_name = htmlspecialchars(addslashes($projecttask_q->f("projecttask_label")));
      $projtask_name = (strlen($projtask_name) > 35) ? substr($projtask_name,0,35)."..." : $projtask_name;

      $projtask_label = "\"". $projtask_name ."\"";

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
    $projtask_data .= "\n)\n";
  }

  $script = "
    <script type=\"text/javascript\">
      project = new Array (
        $proj_data
      )

      projecttask = new Array (
        $projtask_data
      )
    </script>\n";


  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<select name=\"sel_tasktype\" ";
  $sel_tt .= "  onchange=\"fill_project(this.form.sel_project, this.form.sel_tasktype.value,";
  $sel_tt .= "             this.form.sel_projecttask, -1) \">";
  $sel_tt .= "<option value=\"0\">$l_none</option>";

  for ($i=0; $i<2; $i++) {
    $tt_q = $taskt_q[$i];

    while($tt_q->next_record()) {
      //put menu titles
      if (($task_i = $tt_q->f("tasktype_internal")) != $type_prec) {
	$sel_tt .= "<option disabled=\"disabled\">".$task_list[$task_i]." </option>";
	$type_prec = $task_i;
      }
      $tt_id = $tt_q->f("tasktype_id");
      $tt_label = $tt_q->f("tasktype_label");
      $sel_tt .= "\n<option value=\"$tt_id\"";
      if ($is_tt == $tt_id) $sel_tt .= " selected=\"selected\""; 
      $sel_tt .= ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$tt_label</option>\n";
    }
  }

  $sel_tt .= "</select>";

  // Project select construction
  $sel_proj = "
    <select name=\"sel_project\"
            onchange=\"fill_tasktype(this.form.sel_tasktype, this.form.sel_project.value);
                       fill_project(this.form.sel_project, this.form.sel_tasktype.value,
                                    this.form.sel_projecttask, this.form.sel_project.value);return true;\">
    <option value=\"0\">$l_none</option>
    </select>";

  // ProjectTask select construction
  $sel_projtask = "
    <select name=\"sel_projecttask\">
    <option value=\"0\">$l_none</option>
    </select>";

  // Project Label
  if ($action == "detailupdate")
    $tf_label ="<input type=\"text\" name=\"tf_label\" size=\"30\" value=\"".$task_q->f("timetask_label")."\" />";
  else
    $tf_label ="<input type=\"text\" name=\"tf_label\" size=\"30\" />";

  // Project time length
  $sel_time="<select name=\"sel_time\">";
  for ($t=1;$t<=$fraction_t;  $t++) {
    $dlabel = $t.'/'.$fraction_t;
    $sel_time .= "<option value=\"$t\"";
    if ($is_time == $t) $sel_time .= " selected=\"selected\""; 
    $sel_time .= ">$dlabel</option>\n";
  }
  $sel_time .= "</select>";


  // FORM
  $block = "$script";
  $block .= "$script2";
  $block .= "<p></p>";

  if ($action == "insert") {
    $block .= "
      <form method=\"post\" name=\"f_add_task\"
        onsubmit=\"if (check_addtaskform(this)) return true; else return false;\" 
        action=\"".url_prepare("time_index.php")."\">\n
      <input type=\"hidden\" name=\"action\" value=\"$action\" />\n
      <input type=\"hidden\" name=\"popup\" value=\"0\" />\n";
  }
  else {
    $block .= "
      <form method=\"post\"  name=\"f_add_task\" 
        onsubmit=\"if (check_addtaskform(this)) return true; else return false;\"
        action=\"".url_prepare("time_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"detailupdate\" />
      <input type=\"hidden\" name=\"popup\" value=\"0\" />";
  }

  if ($action != "detailupdate")
    $dis_date .= "$l_date :</td><td class=\"timeForm\" colspan=\"2\">$ch_day";
  else
    $dis_date .= "$l_date :</td><td class=\"timeForm\" colspan=\"2\">$rd_day";

  // bastien : différents possibilités de mise en page pour le form
  //---------------------------------------------------------------
  // mise en page bouton integre au formulaire
  // (sinon, ca vire et on decommente en bas)

  if ($action == "insert") {
    $dis_button .= "
      <td class=\"timeForm\">
       <input type=\"hidden\" name=\"wbegin\" value=\"".$time["date"]."\" />
       <input type=\"submit\" value=\"$l_add\" />
      </td>";
  } 
  else {
    $dis_button .= "
      <td class=\"timeForm\">
       <input type=\"hidden\" name=\"wbegin\" value=\"".$time["date"]."\" />
       <input type=\"hidden\" name=\"task_id\" value=\"$task_id\" />
       <input type=\"submit\" value=\"$l_update\" />
      </td>";
  }
  
  if ($action == "detailupdate"){
    $dis_script .= "
      <input type=\"submit\" value=\"$l_close\" onclick=\"window.close();\" />
      <script type=\"text/javascript\">
        fill_project(document.f_add_task.sel_project,
          document.f_add_task.sel_tasktype.value,
          document.f_add_task.sel_projecttask);
        fill_projecttask(document.f_add_task.sel_projecttask, $is_proj);
        select_default(document.f_add_task.sel_project, $is_proj);
        select_default(document.f_add_task.sel_projecttask, $is_projtask);
      </script>";
  } else {
    $dis_script .= "
      <script type=\"javascript\">
        fill_projectall(document.f_add_task.sel_project);
      </script>";
  }
  
  //-------------------------------------------------------------
  // enlever dis_button si on sort le bouton du formulaire

  $block .= "
   <div class=\"detailHead\">$l_fill_addtask</div>

   <div class=\"timeCenter\">
   <table class=\"timeDetail\" width=\"100%\">
    <tr>
     <td class=\"timeForm\">$dis_date</td>
     <td class=\"timeForm\">$l_duration :</td>
     <td class=\"timeForm\">$sel_time</td>
    </tr>
    <tr>
     <td class=\"timeForm\">$l_cat :</td>
     <td class=\"timeForm\" colspan=\"4\">$sel_tt</td>
    </tr>
    <tr>
     <td class=\"timeForm\">$l_project :</td>
     <td class=\"timeForm\">$sel_proj</td>
     <td class=\"timeForm\" colspan=\"3\">$sel_projtask</td>
    </tr>
    <tr>
     <td class=\"timeForm\">$l_label :</td>
     <td class=\"timeForm\" colspan=\"3\">$tf_label</td>
     $dis_button
    </tr>
   </table>
   </div>

   $dis_script
   </form>
   ";

  //------------------------------------------------------------
  // mise en page sur 5 lignes

  //   $block .= "
  //    <div class=\"detailHead\">$l_fill_addtask</div>

  //    <center>
  //    <table class=\"timeDetail\" width=\"95%\">
  //     <tr>
  //      <td class=\"timeForm\">$dis_date</td>
  //      <td class=\"timeForm\">$l_duration :</td>
  //      <td class=\"timeForm\">$sel_time</td>
  //     </tr>
  //     <tr>
  //      <td class=\"timeForm\">$l_cat :</td>
  //      <td class=\"timeForm\" colspan=\"3\">$sel_tt</td>
  //     </tr>
  //     <tr>
  //      <td class=\"timeForm\">$l_project :</td>
  //      <td class=\"timeForm\" colspan=\"3\">$sel_proj</td>
  //     </tr>
  //     <tr>
  //      <td class=\"timeForm\">$l_task :</td>
  //      <td class=\"timeForm\" colspan=\"3\">$sel_projtask</td>
  //     </tr>
  //     <tr>
  //      <td class=\"timeForm\">$l_label :</td>
  //      <td class=\"timeForm\" colspan=\"3\">$tf_label</td>
  //     </tr>
  //    </table>
  //    </center>";

  //------------------------------------------------------------
  // a décommenter si on sort le bouton du formulaire

  //   if ($action == "insert") {
  //     $block .= "<br /><input type=\"submit\" value=\"$l_add\" />";
  //   } 
  //   else {
  //     $block .= "<br />
  //      <input type=\"hidden\" name=\"task_id\" value=\"$task_id\" />
  //      <input type=\"submit\" value=\"$l_update\" />
  //      <input type=\"submit\" value=\"$l_close\" onclick=\"window.close();\" />";
  //   }     
  
  //   $block .= "
  //     <input type=\"hidden\" name=\"wbegin\" value=\"".$time["date"]."\" />\n";
  
  //   if ($action == "detailupdate"){
  //     $block .= "
  //       <script language=\"JavaScript\">
  //         fill_project(document.form_add_task.sel_project,
  //           document.form_add_task.sel_tasktype.value,
  //           document.form_add_task.sel_projecttask);
  //         fill_projecttask(document.form_add_task.sel_projecttask, $is_proj);
  //         select_default(document.form_add_task.sel_project, $is_proj);
  //         select_default(document.form_add_task.sel_projecttask, $is_projtask);
  //       </script>";
  //   }
  
  //   $block .= "</form>";

  return $block;
}


// function col_table($col, $data) {
//   return "<font color=\"#$col\">$data</font>";
// } 

///////////////////////////////////////////////////////////////////////////////
// Display the Time Display preference screen                             //
// Parameters:
//   - $display_newpref_q    : DBO : Field list to display
//   - $display_searchpref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_time_display_pref($display_searchpref_q) {
  global $l_searchtime_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_searchpref_q);
  $dis_pref->display_entity = "time";
  $dis_pref->pref_title = $l_searchtime_display;

  $block .= "<td>";
  $block .= $dis_pref->display();
  $block .= "</td></tr></table>";

  $block .= $dis_pref->dis_pref_help();
  return $block;
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
  $obm_q = run_query_search($time);
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

</script>
