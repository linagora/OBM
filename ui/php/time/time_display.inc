<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_display.php                                             //
//     - Desc : Time Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Array content
// Parameters:
//   - $array : any array
///////////////////////////////////////////////////////////////////////////////
function debug_array($a) {
  global $HTTP_POST_VARS;

  echo "<br>BEGIN----- debug_array ------<br>";
  foreach($a as $key => $val) {
	  if (is_array($val)) {
      echo "clé $key val ";
	  print_r($val);
      echo " <br>";
	  }
    else
      echo "clé $key val --$val-- <br>";
  }
  echo "HTTP_POST_VARS<br>";
  foreach($HTTP_POST_VARS as $key => $val) {
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
  global $sess, $l_week, $hum_date,$l_week_prev,$l_week_next, $l_month_prev,$l_month_next;
  global $cdg_param;
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

               $sess->url("time_index.php?action=".$time["action"].
        "&amp;param_begin=".date("Ymd",$d_start_prev).
        "&amp;param_end=".date("Ymd",$d_end_prev).
						  //        "&amp;user_id=".$time["user_id"].
        "&amp;st_detail=".$time["show_task_detail"])."\">
               ".$l_week_prev."
               </A>
            </TD>  
            <TD>
               <A HREF=\"" .
               $sess->url("time_index.php?action=".$time["action"].
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

  if (debug_level_isset($cdg_param)) {
    echo "previous $d_start_prev, $d_end_prev next 
       $d_start_next, $d_end_next <br>";
  }

  //Daylight Savings Time
  // nothing to do for this

   echo "
      <CENTER> <FONT SIZE=-2>
         <TABLE BORDER=1>
            <TR><TD>
               <A HREF=\"" .
               $sess->url("time_index.php?action=".$time["action"].
           "&amp;param_begin=$d_start_prev&amp;param_end=$d_end_prev".
						  //		   "&amp;user_id=".$time["user_id"].
           "&amp;st_detail=".$time["show_task_detail"])."\">
               ".$l_month_prev."
               </A>
            </TD>  
            <TD>
               <A HREF=\"" .
            $sess->url("time_index.php?action=".$time["action"].
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
  global $l_from_company, $l_see, $l_week, $l_month, $l_from_user, $project_managers;
  global $l_user;
  global $sess, $uid;

  $hum_date_start = dis_human_date($time["date"]);
  $hum_date_end = dis_human_date($time["date_end"]);

  if ($time["interval"] == "week")
	$l_period = $l_week;
  else if ($time["interval"] == "month")
	$l_period = $l_month;

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


    echo "<br>
      <center>
      <form method=post name=f_time action=\"" .
      $sess->url("time_index.php") . "\">
      <table border=1>
      <tr>
        <td><font size=-2>$l_period</font></td>
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
          <input name=\"action\" type=hidden value=\"search\">
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
  global $l_from_company, $l_see, $l_week, $l_month, $l_from_user, $project_managers;
  global $l_user, $l_all;
  global $l_tasktype, $l_task_all, $l_task_uservalid, $l_task_adminvalid;
  global $sess, $uid;

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
      $sess->url("time_index.php") . "\">
      <table border=1>
      <tr>
        <td><font size=-2>$l_period</font></td>
        <td><font size=-1>Du $hum_date_start au $hum_date_end</font></td>
      </tr><tr>
        <td><font size=-2> $l_user <br>$l_all<input type=checkbox name=cb_allusers $allusers></font></td>
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
  global $l_from_company, $l_see, $l_week, $l_month, $l_from_user, $project_managers;
  global $l_user;
  global $sess, $uid;

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
  global $l_no_found, $l_addtask, $cdg_param;
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
// Display the Stats TimeManager result page                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function dis_time_stats($time) {
  global $l_no_found, $l_addtask, $cdg_param;
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
// Display the Time menu                                                     //
// Parameters : 
//  - $time[] : time search criteria
//    keys : usual as needed
//           - $show_task_detail [true|false] show_task_detail set or not
// Returns :
//   an HTML table
///////////////////////////////////////////////////////////////////////////////
function dis_time_menu($time) {
  global $sess;
  global $col_tableh, $col_table;

  $show_events="Voir le Détail";
  $total_menu="Récapitulatifs";
  $total_tasks="Total par taches";
  $total_companies="Total par société";
  $total_projects="Total par projet/affaire";

  // checked the show event checkbox or not
  $dis_checked= $time["show_task_detail"] ? "checked" : "";

  echo "
    <TABLE bgcolor=\"#$col_tableh\" cellspacing=\"2\" border=\"1\" width=75%>
      <TR>
        <TD align=\"center\">
           $show_events
        </TD>
        <TD align=\"center\" colspan=3>
           $total_menu
        </TD>
      </TR>
      <TR>
        <TD bgcolor=\"#$col_table\" align=\"center\">
           <FORM METHOD=POST NAME=form_show_events 
                 ACTION=\"".$sess->self_url("time_index.php")."\">
              <INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"show_details\">
              <INPUT TYPE=\"checkbox\" NAME=\"show_task_detail\" $dis_checked>
              <INPUT TYPE=\"hidden\" NAME=\"user_id[]\" VALUE=\"".
                 $time["user_id"]."\">
              <INPUT TYPE=\"hidden\" NAME=\"param_begin\" VALUE=\"".
                 $time["date"] ."\">
              <INPUT TYPE=\"hidden\" NAME=\"param_end\" VALUE=\"".
                 $time["date_end"] ."\">
              <INPUT TYPE=submit value=\"OK\" >
           </FORM>
        </TD>
        <TD  bgcolor=\"#$col_table\" align=\"center\">$total_companies</TD>
        <TD bgcolor=\"#$col_table\" align=\"center\">$total_projects</TD>
        <TD bgcolor=\"#$col_table\" align=\"center\">$total_tasks</TD>
      </TR>
    </TABLE>
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
  global $sess, $set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_task_del, $l_found_task;

  dis_table_title($l_found_task) ;
  echo "<font color=\"#$col_textem\">$nb_task $l_found</font><br>";

  $url = $sess->url("time_index.php?action=index&amp;param_begin=".$time["date"].
					"&amp;param_end=".$time["date_end"].
					//					"&amp;user_id=".$time["user_id"].
					"&amp;tf_week=".urlencode($week).
					"&amp;st_detail=".$time["show_task_detail"]);
 
  echo "
    <form method=post NAME=f_task_del ACTION=\"".$sess->url("time_index.php")."\" \"onSubmit=\"if (confirm_del()) return true; else return false\" >";

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
    <BR>
    <INPUT TYPE=hidden name=user_id[] value=\"".$time["user_id"][0]."\">
    <INPUT TYPE=hidden name=param_begin value=\"".$time["date"]."\">
    <INPUT TYPE=hidden name=param_end value=\"".$time["date_end"]."\">
    <INPUT TYPE=hidden name=show_task_detail value=\"".
       $time["show_task_detail"]."\">
    <input NAME=\"action\" TYPE=hidden VALUE=\"delete\">
    <input NAME=\"submit\" TYPE=submit VALUE=\"".$l_delete."\" >
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
  global $sess,$set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate, $l_valid;
  //session
  global $uid, $project_managers;

  echo "<font color=\"#$col_textem\">";
  echo "</font><br>";

  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));


 dis_table_title("$l_total_day"); 
 echo "
   <form method=post name=f_task_val action=\"".$sess->url("time_index.php")."\">";

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;

  $dis_task_list->data_cb_text = "$l_validate";;
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
      <form method=post name=f_task_val_admin action=\"".$sess->url("time_index.php")."\">";

   $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
   $dis_task_list->data_set = $admin_db;

   $dis_task_list->data_cb_text = "$l_validate";;
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
  global $sess,$set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate, $l_valid;
  global $c_days_in_a_week, $c_task_adminvalid;
  //session
  global $uid, $project_managers;

  echo "<font color=\"#$col_textem\">";
  echo "</font><br>";

  // We have to know if all the days have been validated by the admin
  // If so, no need to display the user valid one
  if (is_array($validate_days) and array_sum($validate_days) == $c_days_in_a_week * $c_task_adminvalid) {
	echo "taches déjà validées par l'admin, on affiche pas le tableau de validation utilisateur";
    return ;
  }
     
  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));


 dis_table_title("$l_total_day"); 
 echo "
   <form method=post name=f_task_val action=\"".$sess->url("time_index.php")."\">";

  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;

  $dis_task_list->data_cb_text = "$l_validate";;
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
  global $sess,$set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate, $l_valid;
  global $c_days_in_a_week, $c_task_notvalid;
  //session
  global $uid, $project_managers;

  echo "<font color=\"#$col_textem\">";
  echo "</font><br>";

  // We have to know if any of the days have been validated by the user
  // If not, no need to display the admin valid form
  if (! is_array($validate_days)) {
	echo "aucune tache validée par l'utilisateur, on affiche pas le tableau de validation admin";
    return ;
  }
     

  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));

//for project managers
  if( in_array($uid,$project_managers)) {
    dis_table_title("$l_projects_manager"); 
    $admin_db = run_query_admin($time);

    echo "
      <form method=post name=f_task_val_admin action=\"".$sess->url("time_index.php")."\">";

   $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
   $dis_task_list->data_set = $admin_db;

   $dis_task_list->data_cb_text = "$l_validate";;
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
     <input type=hidden name=user_id[] value=\"".$time["user_id"][0]."\">
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
// HTML Display the Time Deal  result                                        //
// Parameters : 
//   - $obm_q    : tasks list to display 
//   - $pref_q   : the fields which have to be displayed
//   - $nb_task  : nb tasks returned by the search query 
//     keys used : week
///////////////////////////////////////////////////////////////////////////////
/* OLD
function dis_time_deal_list($obm_dq,$pref_dq,$nb_task, $time) {
  //-- Themes
  global $sess, $set_theme,$col_tableh,$col_textem,$col_table;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;


  dis_table_title($l_total_deal);  

  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));

  // Calculate the task length grand total 
  echo "avant seek (1) <br>";
  if ($obm_dq->nf()) {
    while ($obm_dq->next_record()) {
	  $res += $obm_dq->f("task_totallength");
	}
	// Rewind the table
	$obm_dq->seek();
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
  echo "<table border=0 width=50%><tr><td>";
  $dis_task_list->display();
  echo "</td></tr>";

  if ($time["interval"] == "month") {
    echo "<tr><td>";
	echo "Total jours pour $nb_users users : $res sur ".
		get_nb_working_days($time["date"]) * $nb_users. " à remplir";
	echo "</td></tr>";
	echo "<tr><td>";
	echo "jours ouvrés : ".get_nb_working_days($time["date"]);
	echo "</td></tr>";
  }
  echo "       </table>";

}
*/

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
  global $sess, $set_theme,$col_tableh,$col_textem,$col_table;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;


  dis_table_title($l_total_deal);  

  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));

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
	echo "Total jours pour $nb_users users : $res sur ".
		get_nb_working_days($time["date"]) * $nb_users. " à remplir";
	echo "</td></tr>";
	echo "<tr><td>";
	echo "jours ouvrés : ".get_nb_working_days($time["date"]);
	echo "</td></tr>";
  }
  echo "       </table>";

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
  global $sess, $auth,$time ,$l_addtask_start, $l_addtask_end, $fraction_jour ,$task_id_modify;
  global $col_table, $col_tableh, $col_textem, $l_add, $task_list, $l_none;
  global $l_label,$l_date,$l_project,$l_duration,$l_task, $l_update, $l_modtask, $l_close;
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
        action=\"".$sess->url("time_index.php")."\">
      <input type=\"hidden\" name=\"action\" value=\"$action\">
      <input type=\"hidden\" name=\"popup\" value=0>
  ";
  }
  else {
    dis_table_title($l_update_task);
    echo "
      <form method=get  name=form_add_task 
        onSubmit=\"if (check_form(this)) return true; else return false;\"
        action=\"".$sess->url("time_index.php")."\">
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

</SCRIPT>
