<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_display.php                                             //
//     - Desc : Time Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// HTML Display Array content                                         //
// Parameters:
//   - $array : any array
///////////////////////////////////////////////////////////////////////////////
function debug_array($a) {
  global $HTTP_POST_VARS;

  foreach($a as $key => $val) {
    echo "clé $key val $val <br>";
  }
  echo "HTTP_POST_VARS<br>";
  foreach($HTTP_POST_VARS as $key => $val) {
    echo "clé $key val $val <br>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display Changing Time Links                                              //
// Parameters:
//   - $time : array with $time["date"] == current date 
//   - $type : "week", "month", "all"
//     default : all
///////////////////////////////////////////////////////////////////////////////
function html_time_links (&$time, $type="all") {
  global $sess, $l_week, $hum_date,$l_week_prev,$l_week_next;


  //get the current start and end of week
  $d_start_week = first_day_week($time);
  if ( ! isset($time["date_end"]) ) 
    $time["date_end"] = date("Ymd",$d_start_week+6*86400);
 
  $d_start_prev = $d_start_week-7*86400;
  $d_start_next = $d_start_week+7*86400;
 
   
  
  //Daylight Savings Time
  if ( date('I',$d_start_week) == '0' and  date('I',$d_start_prev) == '1' ) { //winter to summer

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
               $sess->url("time_index.php?action=index&amp;param_begin=".date("Ymd",$d_start_prev)."&amp;param_end=".date("Ymd",$d_end_prev))."&amp;user_id=".$time["contact_id"]."\">
               ".$l_week_prev."
               </A>
            </TD>  
            <TD>
               <A HREF=\"" .
               $sess->url("time_index.php?action=index&amp;param_begin=".date("Ymd",$d_start_next)."&amp;param_end=".date("Ymd",$d_end_next))."&amp;user_id=".$time["contact_id"]."\">
               ".$l_week_next."
               </A>
            </TD></TR>
         </TABLE>    
      </FONT> </CENTER>    
        ";     
}


 
///////////////////////////////////////////////////////////////////////////////
// Display TimeManager Week search Form                                      //
// Parameters:
//   - $time[] : default form values
//     keys used  : week
///////////////////////////////////////////////////////////////////////////////
function html_time_search_form ($time, $obm_q_contact_list, $contacts_array) {
  global $l_contact_name, $l_from_company, $l_see, $l_week, $l_from_user, $project_managers;
  global $l_user;
  global $sess , $uid;
 

  $hum_date_start = dis_human_date($time["date"]);
  $hum_date_end = dis_human_date($time["date_end"]);

  if( in_array($uid,$project_managers)) {
    $sel_contact_id = $time["contact_id"];
  
    echo "<BR><CENTER>
       <FORM METHOD=POST NAME=f_time ACTION=\"" .
       $sess->url("time_index.php") . "\">
	  <TABLE BORDER=1><TR>
         <TD><FONT SIZE=-2>$l_week</FONT></TD>
         <TD><FONT SIZE=-1>Du $hum_date_start au $hum_date_end </FONT></TD>
         </TR>
         <TR>
         <TD>&nbsp;&nbsp;</TD>
         <TD align=left><FONT SIZE=-2> $l_user </FONT><BR>";

    // Available Contacts 
    echo "\n<CENTER><SELECT name=\"sel_contact_id\" size=6>";
    while ($obm_q_contact_list->next_record()) {
      echo "\n<OPTION value=".$obm_q_contact_list->f("contact_id");
 
      // Get parameter from Form 
      if (! is_null($sel_contact_id)) {
         $tag_contact="";
	   if ($obm_q_contact_list->f("contact_id") == $sel_contact_id ) {
	     $tag_contact=" selected";
	   }
      }
      // Or Get it from user id.
      elseif (count($contacts_array)>0) {
         $tag_contact="";
         while ( (list($key, $contact_id) = each($contacts_array) ) && (strcmp($tag_contact,"")==0)) {
	   if ($obm_q_contact_list->f("contact_id") == $contact_id ) {
	     $tag_contact=" selected";
	   }
         }
         reset($contacts_array);
      }    
  
      echo " ".$tag_contact.">".$obm_q_contact_list->f("contact_lastname")." ".$obm_q_contact_list->f("contact_firstname")." </OPTION>";
    }
    echo "</SELECT></CENTER>";


    echo "
       </TD>
       <TD VALIGN=bottom>&nbsp;&nbsp;
         <INPUT TYPE=hidden name=param_begin value=\"".$time["date"]."\">
         <INPUT TYPE=hidden name=param_end value=\"".$time["date_end"]."\">
         <INPUT NAME=\"action\" TYPE=hidden VALUE=\"search\">
         <INPUT NAME=\"submit\" TYPE=submit VALUE=\"$l_see\">&nbsp;
       </TD>
     </TR>
     </TABLE>
     </FORM><HR>
   </CENTER>";
  }
  else
    echo "<BR>Semaine du $hum_date_start au $hum_date_end<BR><BR>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact search result                                         //
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_time_list($time) {
  global $l_no_found, $l_addtask, $set_debug;
  global $auth, $new_order, $order_dir, $new_order2;
  global $status_id; // "Signed" status for deals
  global $fraction_jour; // nbre de fraction possible d'une journée

  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  $obm_q = run_query_search($time, $new_order, $order_dir, $new_order2);
  $nb_task = $obm_q->num_rows();
  $pref_day_q = run_query_display_pref($auth->auth["uid"], "time_day");
  $obm_day_q = run_query_by_day($time, $new_order, $order_dir, $new_order2);

  $pref_deal_q = run_query_display_pref($auth->auth["uid"], "time_deal");
  $obm_deal_q = run_query_by_deal($time, $new_order, $order_dir, $new_order2);

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

  if ($nb_task == 0) {
    display_warn_msg("<BR>".$l_no_found);
  } else {
      $names = run_query_get_names($time["contact_id"]);
      $names->next_record();
      
   
    html_time_list($obm_q,$pref_q,$nb_task,$time);
  }
  
  // Creating the dates for the selected (or current) date
  $day_q = get_this_week($d_start_week, 5);
  
  if ($set_debug > 0)
    echo "liste des jours <br>" . print_r($day_q);

  dis_form_addtask("insert", $obm_project_q, $obm_tasktype_q, $day_q, $fraction_jour,$time);  

  if($nb_task != 0) {

    html_time_day_list($obm_day_q,$pref_day_q,$nb_task,$time);

    html_time_deal_list($obm_deal_q,$pref_deal_q,$nb_task,$time);
  }
  
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Task  result                                        //
// Parameters : 
//   - $obm_q      : list of the contacts to display 
//   - $pref_q     : the fields which have to be displayed
//   - $nb_task : nb contacts returned by the search query 
//   - $time[]  : time search criteria
//     keys used   : week
///////////////////////////////////////////////////////////////////////////////
function html_time_list($obm_q,$pref_q,$nb_task,$time) {
  //-- Themes
  global $sess, $set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_modify, $l_task_del, $l_found_task;

  dis_table_title($l_found_task) ;
  echo "<FONT color=\"#$col_textem\">";
  echo "$nb_task $l_found";
  echo "</FONT><BR>";

  $url = $sess->url("time_index.php?action=index&amp;param_begin=".$time["date"]."&amp;param_end=".$time["date_end"]."&amp;user_id=".$time["contact_id"]."&amp;tf_week=".urlencode($week));
 
  echo "
    <form METHOD=POST NAME=f_task_del ACTION=\"".$sess->self_url("time_index.php")."\" \"onSubmit=\"if (confirm_del()) return true; else return false\" >
       ";

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
    <INPUT TYPE=hidden name=user_id value=\"".$time["contact_id"]."\">
    <INPUT TYPE=hidden name=param_begin value=\"".$time["date"]."\">
    <INPUT TYPE=hidden name=param_end value=\"".$time["date_end"]."\">
    <input NAME=\"action\" TYPE=hidden VALUE=\"delete\">
    <input NAME=\"submit\" TYPE=submit VALUE=\"".$l_delete."\" >
    </form>
       ";
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Day  result                                        //
// Parameters : 
//   - $obm_q      : list of the contacts to display 
//   - $pref_q     : the fields which have to be displayed
//   - $nb_task : nb contacts returned by the search query 
//   - $time[]  : time search criteria
//     keys used   : week
///////////////////////////////////////////////////////////////////////////////
function html_time_day_list($obm_dq,$pref_dq,$nb_task,$time) {
  //-- Themes
  global $sess,$set_lang,$set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete;
  global $l_total_day, $l_projects_manager, $l_validate, $l_valid;
  //session
  global $uid, $project_managers;


  $lname = stripslashes($contact["lname"]);
  $company = stripslashes($contact["company"]);
  $company_id = $contact["company_id"];

  echo "<FONT color=\"#$col_textem\">";
  echo "</FONT><BR>";

  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));


 dis_table_title("$l_total_day"); 
 echo "
   <form METHOD=POST NAME=f_task_val ACTION=\"".$sess->self_url("time_index.php")."\" >
      ";

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
    <BR>
    <INPUT TYPE=hidden name=user_id value=\"".$time["contact_id"]."\">
    <INPUT TYPE=hidden name=param_begin value=\"".$time["date"]."\">
    <INPUT TYPE=hidden name=param_end value=\"".$time["date_end"]."\">
    <INPUT NAME=\"action\" TYPE=hidden VALUE=\"validate\">
    <INPUT NAME=\"submit\" TYPE=submit VALUE=\"".$l_valid."\" >
    </form>
       ";

//for project managers
  if( in_array($uid,$project_managers)) {
    dis_table_title("$l_projects_manager"); 
    $admin_db = run_query_admin($time);

    echo "
      <form METHOD=POST NAME=f_task_val_admin ACTION=\"".$sess->self_url("time_index.php")."\" >
         ";

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
     <BR>
     <INPUT TYPE=hidden name=user_id value=\"".$time["contact_id"]."\">
     <INPUT TYPE=hidden name=param_begin value=\"".$time["date"]."\">
     <INPUT TYPE=hidden name=param_end value=\"".$time["date_end"]."\">
     <INPUT NAME=\"action\" TYPE=hidden VALUE=\"validate_admin\">
     <INPUT NAME=\"submit\" TYPE=submit VALUE=\"".$l_valid."\" >
     </form>
        ";    
   }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Time Deal  result                                        //
// Parameters : 
//   - $obm_q      : list of the contacts to display 
//   - $pref_q     : the fields which have to be displayed
//   - $nb_task : nb contacts returned by the search query 
//     keys used   : week
///////////////////////////////////////////////////////////////////////////////
function html_time_deal_list($obm_dq,$pref_dq,$nb_task) {
  //-- Themes
  global $sess, $set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $_task_del, $l_delete, $l_total_deal;


  dis_table_title($l_total_deal);  

  $url = $sess->url("time_index.php?action=search&amp;tf_week=".urlencode($week));


  $dis_task_list = new OBM_DISPLAY("DATA",$pref_dq);
  $dis_task_list->data_set = $obm_dq;
  $dis_task_list->data_order = false;
  $dis_task_list->data_page = false;
  $dis_task_list->data_header = "top";
  $dis_task_list->data_url = $url;
  
// display in another table to limit size
  echo "<table border=0 width=50%><tr><td>";
  $dis_task_list->display();
  echo "<td><tr></table>";

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
// Display the Table description or message                             //
// Parameters:
//   - $message : message to display
///////////////////////////////////////////////////////////////////////////////
function dis_form_addtask($action, $project_q, $taskt_q, $day_q, $fraction_t,$time) {
   global $sess, $auth,$time ,$l_addtask_start, $l_addtask_end, $fraction_jour ,$task_id_modify;
   global $col_table, $col_tableh, $col_textem, $l_add, $task_list;
   global $l_label,$l_date,$l_project,$l_duration,$l_task, $l_modify, $l_modtask, $l_close;
   //for list classement of task
   global $l_task_fac,$l_task_int;
   $task_list  = array($l_task_fac, $l_task_int);

  $validate_day = run_query_valid_search($time);

  //get different attribute for task to be modify
  if ( $action == "modify") {
    $task_to_be_modify = run_query_get_task($task_id_modify);
    $is_day = substr($task_to_be_modify->f("task_date"),0,8);
    $is_tt = $task_to_be_modify->f("task_tasktype_id");
    $is_proj = $task_to_be_modify->f("task_deal_id");
    $is_time = $task_to_be_modify->f("task_length");
  }

  // Day select construction
  $sel_day = "<SELECT name=sel_date OnClick=\"uncheck_days(this.form)\">";
  for ($day=0;$day< sizeof($day_q);  $day++) {
    if(sizeof($validate_day)>0 && in_array($day_q[$day][1],$validate_day) != FALSE) continue;
    $dlabel = $day_q[$day][0];
    $sel_day .= "<OPTION value=\"".$day_q[$day][1]."\"";
    if ($is_day == $day_q[$day][1]) $sel_day .= " selected"; 
    $sel_day .= ">$dlabel</OPTION>\n";
  }
  $sel_day .= "</SELECT>";

  // Day check construction
  if( $action != "modify") {
   for ($day=0;$day< sizeof($day_q);  $day++) {
     if(sizeof($validate_day)>0 && in_array($day_q[$day][1],$validate_day) != FALSE) continue;
     $dlabel = $day_q[$day][0];
     $ch_day .= "<INPUT type=\"checkbox\" name=\"cb_day[".$day_q[$day][1]."]\"";
     $ch_day .= ">$dlabel\n";
   }
  }
  // TaskType select construction
  $type_prec = -1;
  $sel_tt = "<SELECT name=sel_tasktype>";
  while($taskt_q->next_record()) {
    //put menu titles
    if (($task_i = $taskt_q->f("tasktype_internal")) != $type_prec) {
      $sel_tt .= "<option disabled=\"disabled\">".$task_list[$task_i]." </option>";
      $type_prec = $task_i;
    }
    $tt_id = $taskt_q->f("tasktype_id");
    $tt_label = $taskt_q->f("tasktype_label");
    $sel_tt .= "\n<OPTION value=\"$tt_id\"";
    if ($is_tt == $tt_id) $sel_tt .= " selected"; 
    $sel_tt .= ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$tt_label</OPTION>\n";
  }
  $sel_tt .= "</SELECT>";

  // Project select construction
  $sel_proj = "<SELECT name=sel_project>";
  while($project_q->next_record()) {
    $proj_id = $project_q->f("deal_id");
    $proj_label = $project_q->f("company_name")." -- ". $project_q->f("deal_label");
    $sel_proj .= "\n<OPTION value=\"$proj_id\"";
    if ($is_proj == $proj_id) $sel_proj .= " selected"; 
    $sel_proj .= ">$proj_label</OPTION>\n";
  }
  $sel_proj .= "</SELECT>";

  // Project Label
  if ($action == "modify")
    $tf_label ="<INPUT TYPE=\"text\" NAME=\"tf_label\" SIZE=30 VALUE=\"".$task_to_be_modify->f("task_label")."\">";
  else
    $tf_label ="<INPUT TYPE=\"text\" NAME=\"tf_label\" SIZE=30>";

  // Project time length
  $sel_time="<SELECT name=sel_time>";
  for ($t=1;$t<=$fraction_t;  $t++) {
    $dlabel = $t.'/'.$fraction_t;
    $sel_time .= "<OPTION value=\"$t\"";
   if ($is_time == $t) $sel_time .= " selected"; 
   $sel_time .= ">$dlabel</OPTION>\n";
  }
  $sel_time .= "</SELECT>";


// FORM
  echo "<p>&nbsp;</p>";
  if($action == "insert") {
    dis_table_title($l_addtask_start.$fraction_jour.$l_addtask_end) ;
    echo "
<FORM METHOD=POST NAME=form_add_task 
        ACTION=\"".$sess->self_url("time_index.php")."\">
      <INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"$action\">
      <INPUT TYPE=\"hidden\" NAME=\"popup\" VALUE=0>
  ";
  }
  else {
    dis_table_title($l_modtask);
    echo "
<FORM METHOD=POST  NAME=form_add_task 
        ACTION=\"".$sess->self_url("time_index.php")."\">
      <INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"$action\">
      <INPUT TYPE=\"hidden\" NAME=\"popup\" VALUE=0>
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
</table>";

   if ($action == "insert") {
      echo "<BR><INPUT TYPE=hidden name=user_id value=\"".$time["contact_id"]."\">
            <CENTER> <INPUT TYPE=submit value=\"$l_add\"> </CENTER>
           ";
   }
   else {
      echo "<BR><INPUT TYPE=hidden name=user_id value=\"".$time["contact_id"]."\">
            <CENTER> <INPUT TYPE=submit value=\"$l_modify\"> </CENTER>
           ";
      echo "
            <BR><CENTER> <INPUT TYPE=submit value=\"$l_close\" onclick=\"window.close()\"> </CENTER>
           ";
   }     
 echo "</FORM> <p>&nbsp;</p>";

}

function col_table($col, $data) {
   return "<font color=\"#$col\">$data</font>";
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Time search form with params                                 //
// Parameters:
//   - $time[] : time search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_time_search($time) {
  global $l_no_found;
  global $auth, $new_order, $order_dir, $new_order2;

  $pref_q = run_query_display_pref($auth->auth["uid"], "time");
  $obm_q = run_query_search($time, $new_order, $order_dir, $new_order2);
  $nb_contact = $obm_q->num_rows();
  if ($nb_contact == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_contact_search_list($obm_q,$pref_q,$nb_contact,$contact);
  }
}

 

</SCRIPT>
