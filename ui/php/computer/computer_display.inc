<?
///////////////////////////////////////////////////////////////////////////////
// OBM - File : computer_display.inc
//     - Desc : Computer Display File                                        //
// 2000-01-20 Vincent MARGUERIT                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display List New Form AND modify computer                                 //
///////////////////////////////////////////////////////////////////////////////
function dis_compu_new_form() {
  global $ret, $action, $computer_id ;
  global $tf_name, $tf_domain, $tf_ip, $tf_user, $ta_comments ;
  global $l_modify_compu, $l_compu_name, $l_compu_domain_name, $l_compu_ip, $l_compu_user, $l_save_compu, $l_domain_name_novalid, $l_ip_novalid, $l_compu_comments, $l_update_compu ;
  global $col_tableh, $col_textem ;
  global $sess;

  if($action == "modify_computer") {
    if(isset($ret)) {
      if($ret == 2) {
	$message = $l_domain_name_novalid ;
	$old = $tf_name.".".$tf_domain ;
	$tf_name = ""; $tf_domain = "" ;
      }else {
	$message = $l_ip_novalid ;
	$old = $tf_ip ;
	$tf_ip = "" ;
      }
    }
    $compu_data = run_query_data_compu($computer_id) ;
    $compu_data->next_record() ;
    dis_action_compu(
		     $compu_data->f("computer_name").".".$compu_data->f("computer_domain"),
		     $compu_data->f("computer_auth_scan"),
		     $l_modify_compu) ;
    $tf_name = $compu_data->f("computer_name") ;
    $tf_domain = $compu_data->f("computer_domain") ;
    $tf_ip = $compu_data->f("computer_ip") ;
    $tf_user = $compu_data->f("computer_user") ;
    $ta_comments = $compu_data->f("computer_comments") ;
  } else {
    if(isset($ret)) {
      if($ret == 2) {
	$message = $l_domain_name_novalid ;
	$old = $tf_name.".".$tf_domain ;
	$tf_name = ""; $tf_domain = "" ;
      }else {
	$message = $l_ip_novalid ;
	$old = $tf_ip ;
	$tf_ip = "" ;
      }
    }
  }

  echo "
  <div align=\"center\">
  <form method=\"post\" name=\"f_compu_new\" action=".$sess->url("computer_index.php").">
    <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\">" ;
  if(isset($ret)) {
    echo "
      <tr>
        <td colspan=\"2\" bgcolor=\"$col_tableh\" align=\"center\">
          <font color=\"$col_textem\"> $message : $old </font>
        </td>
      </tr>" ;
    }
    echo "
      <tr>
        <td align=\"right\">
          <b>$l_compu_name : </b>
        </td>
        <td align=\"left\">
         <input type=\"text\" name=\"tf_name\" value=\"$tf_name\">
        </td>
      </tr>
      <tr>
        <td align=\"right\">
          <b>$l_compu_domain_name : </b>
        </td>
        <td align=\"left\">
         <input type=\"text\" name=\"tf_domain\" value=\"$tf_domain\">
        </td>
      </tr>
      <tr>
        <td align=\"right\">
          <b>$l_compu_ip : </b>
        </td>
        <td align=\"left\">
         <input type=\"text\" size=15 maxlength=15 name=\"tf_ip\" value=\"$tf_ip\">
        </td>
      </tr>
      <tr>
        <td align=\"right\">
           <b>$l_compu_user : </b>
        </td>
        <td align=\"left\">
         <input type=\"text\" name=\"tf_user\" value=\"$tf_user\">
        </td>
      </tr>
      <tr>
        <td valign=\"top\" align=\"right\">
           <b>$l_compu_comments : </b>
        </td>
        <td align=\"left\">
         <textarea name=\"ta_comments\" cols=\"35\" rows=\"7\" wrap=\"virtual\">$ta_comments</textarea>
        </td>
      </tr>
      <tr>
        <td>&nbsp;</td>
        <td align=\"left\">
          <br>" ;
if($action == "modify_computer") {
  echo "
          <input type=\"hidden\" name=\"computer_id\" value=\"$computer_id\">
          <input type=\"hidden\" name=\"action\" value=\"update_computer\">
          <input type=\"submit\" value=\"$l_update_compu\">" ;
}else {
  echo "
          <input type=\"hidden\" name=\"action\" value=\"new_computer\">
          <input type=\"submit\" value=\"$l_save_compu\">" ;
}
echo "
        </td>
      </tr>
   </table>
  </form>
  </div>" ;       
}


///////////////////////////////////////////////////////////////////////////////
// Display List search Form                                                  //
///////////////////////////////////////////////////////////////////////////////
function dis_compu_search_form () {
  global $l_compu_name_short, $l_compu_domain_short, $l_compu_user_short, $l_compu_ip_short, $l_find ;
  global $tf_name, $tf_domain, $tf_user, $tf_ip ; 
  global $perm, $sess,$action, $col_textem, $col_tableh ;
  
 if(($perm->have_perm("editor") && (($action == "admin")||$action == "search_forauth"))) {
   echo "
        <table border=\"0\">
          <tr>
             <td colspan=\"2\" bgcolor=\"#$col_tableh\" align=center>
               <font color=\"#$col_textem\"><b>Autoriser des utilisateurs à scanner les ordinateurs</font></b>
             </td>
          </tr>
        </table>" ;
 }
  echo "
   <center>
      <form method=post name=f_search_compu action=".$sess->url("computer_index.php").">
        <table border=0>
          <tr>
             <td>
               $l_compu_name_short
               <br>
               <input type=text name=tf_name size=20 value=\"$tf_name\">
             </td>
             <td align=left>$l_compu_domain_short<br>
               <input type=text name=tf_domain size=20 value=\"$tf_domain\">
             </td>
             <td align=left>$l_compu_ip_short<br>
               <input type=text name=tf_ip size=15 maxlength=15 value=\"$tf_ip\">
             </td>
          </tr>
          <tr>
             <td>
               $l_compu_user_short
               <br>
               <input type=text name=tf_user size=20 value=\"$tf_user\">
             </td>
             <td colspan=\"2\" valign=bottom>&nbsp;&nbsp;" ;
   if(($perm->have_perm("editor")&&(($action == "admin")||$action == "search_forauth"))) {
     echo " <input name=\"action\" type=hidden value=\"search_forauth\">" ;
   }else {
     echo " <input name=\"action\" type=hidden value=\"search\">" ;
   }
   echo "
               <input name=\"submit\" type=submit value=\"$l_find\">
                &nbsp;&nbsp;
             </td>
         </tr>
     </table>
    </form><hr>
   </center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Search result (if it has to be done                               //
// if ($set_display != "no") or if it comes from the submit bouton or        //
// if a company parameter has been transmited.                               //
///////////////////////////////////////////////////////////////////////////////
function dis_search_computers($db_computers) {
  global $set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  global $l_compu_found, $l_compu_name_short, $l_compu_domain_short, $l_compu_ip_short, $l_compu_user_short ;
  global $tf_name_computer,$tf_domain_name, $tf_ip, $tf_user;
  global $page, $set_rows, $set_rows_default, $perm, $action, $sess;
  
  $nb_list = $db_computers->nf() ;
  
  echo "<div align=\"center\">
         <font color=\"#$col_textem\">".$db_computers->nf()." $l_compu_found
         </font><br>";
  
   if(($perm->have_perm("editor") && ($action == "search_forauth"))) {
     echo "
      <form method=post name=chg_auth action=".$sess->url("computer_index.php").">" ;
   }
  
  // Page display management
  if ($set_rows < 1) {$set_rows=$set_rows_default;}
  $nb_page = floor(($nb_list-1) / $set_rows);
  $first_row = $page * $set_rows;
  $i=0;
  
  // Table of content (pages)
  while ($i <= ($nb_page)) {
    if ($i != 0) echo " | ";
    if ($i != $page) {
      if(($perm->have_perm("editor") && ($action == "search_forauth"))) {
	echo "<a href=\"".$sess->url("computer_index.php?action=search_forauth&amp;page=$i&amp;tf_name=$tf_name&amp;tf_domain=$tf_domain&amp;tf_ip=$tf_ip&amp;tf_user=$tf_user")."\">";
      }else {      
      echo "<a href=\"".$sess->url("computer_index.php?action=search&amp;page=$i&amp;tf_name=$tf_name&amp;tf_domain=$tf_domain&amp;tf_ip=$tf_ip&amp;tf_user=$tf_user").">";
      }
    }
    echo ($i + 1);
    if ($i != $page) echo "</a>";
    $i++;
  }
  echo "</div>
        <div align=\"center\">
        <table border=0>
          <tr>
            <td bgcolor=\"#$col_tableh\" align=center>
               <font color=\"#$col_textem\">&nbsp; $l_compu_name_short</font>
        </td>
        <td bgcolor=\"#$col_tableh\" align=center>
            <font color=\"#$col_textem\">&nbsp; $l_compu_domain_short</font>
        </td>
        <td bgcolor=\"#$col_tableh\" align=center>
             <font color=\"#$col_textem\">$l_compu_ip_short</font>
        </td>
        <td bgcolor=\"#$col_tableh\" align=center>
             <font color=\"#$col_textem\">$l_compu_user_short</font>
        </td>" ;
  if(($perm->have_perm("editor") && ($action == "search_forauth"))) {
    echo "
        <td bgcolor=\"#$col_tableh\" align=center>
             <font color=\"#$col_textem\">Autorisation</font>
        </td>" ;
  }
  echo "
      </tr>";
  $db_computers->seek($first_row);
  $cpt = 0;
  while (($db_computers->next_record()) && ($cpt < $set_rows)) {
    $cpt++;
    echo "<tr>
    <td bgcolor=\"#$col_table\">
       <a href=\"".$sess->url("computer_index.php?action=detailconsult&amp;computer_id=" .
      $db_computers->f("computer_id") )."\">" .
      $db_computers->f("computer_name") . "</a>
    </td>
    <td bgcolor=\"#$col_table\">
       &nbsp;".$db_computers->f("computer_domain")."
    </td> 
    <td align=\"left\" bgcolor=\"#$col_table\">
      &nbsp;".$db_computers->f("computer_ip")."
     </td>
    <td align=\"center\" bgcolor=\"#$col_table\">
     &nbsp;".($db_computers->f("computer_user"))."
    </td>" ;
    if(($perm->have_perm("editor") && ($action == "search_forauth"))) {
      echo "
      <td align=\"center\" bgcolor=\"#$col_table\">
        <select name=\"sel_ch_auth_".$db_computers->f("computer_id")."\">
            <option value=\"0\"" ;
      if($db_computers->f("computer_auth_scan") == 0) { echo " selected " ;}
      echo ">no" ;
      echo "<option value=\"1\"" ;
      if($db_computers->f("computer_auth_scan") == 1) { echo " selected " ;}
      echo ">yes" ;
      echo "
        </select>
    </td>" ;
    }
    echo "
  </tr>" ; 
  }
  if(($perm->have_perm("editor")) && ($action == "search_forauth")) {
    echo "
  <tr>
     <td colspan=\"5\" align=\"center\"><br>
       <input type=\"hidden\" name=\"tf_name_computer\" value=\"$tf_name_computer\"> 
       <input type=\"hidden\" name=\"tf_domain_name\" value=\"$tf_domain_name\"> 
       <input type=\"hidden\" name=\"tf_ip\" value=\"$tf_ip\"> 
       <input type=\"hidden\" name=\"tf_user\" value=\"$tf_user\"> 
       <input type=\"hidden\" name=\"page\" value=\"$page\"> 
       <input type=\"hidden\" name=\"action\" value=\"ch_auth_scan\">
       <input type=\"submit\" value=\"Changer les authorisation\">
     </td>
  </tr>" ;
  }
  echo "</table>" ;
}


////////////////////////////////////////////////////////////////////////////////
///              Function to display INFORMATIONS about one LIST             ///
////////////////////////////////////////////////////////////////////////////////
function dis_infos_one_compu(
			     $compu_data, $option_dis_compu, 
			     $option_dis_port=0, $services_list=0) {
  global $computer_id, $l_compu_infos, $l_port_num, $l_should_scan, $l_port_found, $l_port_del ;
  global $action, $nb_port_added, $set_rows, $set_rows_default, $page, $sess, $perm ;
  global $col_textem, $col_table, $col_tableh ;
  global $sess;

  
  $compu_data->next_record() ;
  $action_message = ($action == "del_port") ? $l_port_del:$l_compu_infos ;
  dis_action_compu($compu_data->f("computer_name").".".$compu_data->f("computer_domain"),
                   $compu_data->f("computer_auth_scan"),
                   $action_message) ;

  $dis_infos = new OBM_DISPLAY("INFORMATIONS",$option_dis_compu) ;
  $dis_infos->inf_datas = $compu_data ;
  $dis_infos->inf_which_element = $l_compu_infos ;
  $dis_infos->display_infos() ;
  
  if($action == "add_port_chosen") {
    echo "<br><br><b>$l_port_num $nb_port_added <br><br>$l_should_scan" ;
  } else {
    $nb_page = floor(($services_list->nf()-1) / $set_rows);
    $first_row = $page * $set_rows;
    $i=0;
    echo "<br>
           <font color=\"$col_textem\">".$services_list->nf()."$l_port_found
           </font><br>" ;
    if($services_list->nf()!=0) {
      while ($i <= ($nb_page)) {
	if ($i != 0) echo " | ";
	if ($i != $page) {
	  echo "<a href=\"".$sess->url("computer_index.php?action=$action&amp;computer_id=$computer_id&amp;page=$i")."\">";
	}
	echo ($i + 1);
	if ($i != $page) echo "</a>";
	$i++;
      }
      
      $services_list->seek($first_row);
      
      if(($action == "del_port") && ($perm->have_perm("editor"))) {
	echo "<form method=post name=f_del_port action=".$sess->url("computer_index.php").">" ;
      }
      echo "<br>
        <table border=\"0\" >
          <tr>" ;

      $dis_infos->which_infos = "ELEMENTS" ;
      $dis_infos->options_display = $option_dis_port ;
      $dis_infos->elem_list = $services_list ;
      $dis_infos->elem_order_for = "order_servicecomputer" ;
      if(($action == "del_port") && ($perm->have_perm("editor"))) {
	$dis_infos->elem_action_name =  "Supprimer" ;
	$dis_infos->elem_ch_name = "del_" ;
	$dis_infos->elem_dbname_id = "service_port" ;
      }

      $dis_infos->display_infos() ;

      if(($action == "del_port") && ($perm->have_perm("editor"))) {
	echo "<br>
        <div align=\"center\"> <br>
          <input type=\"hidden\" name=\"computer_id\" value=\"$computer_id\">
          <input type=\"hidden\" name=\"action\" value=\"del_port_chosen\">
          <input type=\"submit\" value=\"$l_port_del\">
        </div>
        </form>" ;
      }
    }
  }
}




////////////////////////////////////////////////////////////////////////////////
///           Function to display verification of informations               ///
////////////////////////////////////////////////////////////////////////////////
function dis_verif_infos($compu_data) {
  global $l_compu_name_no2, $l_compu_name_ok_notip, $l_compu_name_else, $l_compu_name_no, $l_compu_domain_short, $l_compu_name_short, $l_compu_ip_ok, $l_compu_name_ok, $l_compu_ip_no, $l_compu_verify, $l_compu_data, $l_compu_status, $l_compu_comments, $l_compu_ip_short, $l_compu_should_chg, $l_compu_name_no_res, $l_compu_test_compu, $l_compu_ip_diff, $l_compu_name_ok2 ;
  global $set_theme, $ico_round , $ico_crow, $ico_warning ;
  global $col_tableh, $col_textem, $col_table ;

  $compu_data->next_record() ;
  $name = $compu_data->f("computer_name").".".$compu_data->f("computer_domain") ;
  dis_action_compu($name,
                   $compu_data->f("computer_auth_scan"),
                   $l_compu_verify) ;
  passthru("ping -c 3 ".$compu_data->f("computer_ip").">/dev/null",$ret_sys_ip) ;
  if($ret_sys_ip ==0) {
    $mess_ip = $l_compu_ip_ok ;
    $ico_ip = $ico_round ;
    $hostname =  gethostbyaddr($compu_data->f("computer_ip")) ;
    if(strcmp(strtolower($name) , strtolower($hostname)) == 0) {//Every infos are OK.
      $ico_name = $ico_round ;
      $mess_name = $l_compu_name_ok ;
    }else {
      $ico_name = $ico_crow ;
      if(strcmp(strtolower($compu_data->f("computer_ip")) , strtolower($hostname)) == 0) {
	$mess_name = $l_compu_name_no_res ;//We can't resolv ip in name
	if($name != gethostbyname($name)) {
	  $mess_ip = $l_compu_ip_diff."<b>".gethostbyname($name)."</b>" ;
	  $mess_name = $l_compu_name_ok2 ;
	  $ico_ip = $ico_crow ;
	  $ico_name = $ico_round ;
	  $should = 1 ;
	}
	$should = 1 ;
      } else {
	$mess_name = $l_compu_name_else."<b>".$hostname."</b>" ;//ip and name are different
	$should = 1 ;
      }
    }
  }else { //If the ip is down
    $mess_ip = $l_compu_ip_no ;
    $ico_ip = $ico_crow ;
    passthru("ping -c 3 $name>/dev/null",$ret_sys_host) ;
    if($ret_sys_host == 0) {
      $ico_name = $ico_round ;
      $mess_name = $l_compu_name_ok_notip."<b>".gethostbyname($name)."</b>" ;
      $should = 1 ;
    }else {
      $ico_name = $ico_crow ;
      $mess_name = $l_compu_name_no2 ;
      $should = 1 ;
      $l_compu_should_chg = $l_compu_test_compu ;
    }
  }

  echo "
    <table cellspacing=\"2\" border=\"0\" >
       <tr>
         <td align=\"center\">
                &nbsp;
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                <b>$l_compu_data</b>
            </font>
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                <b>$l_compu_status</b>
            </font>
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                <b>$l_compu_comments</b>
            </font>
         </td>
      </tr>
      <tr>
        <td bgcolor=\"#$col_table\" align=\"center\">
           <b>$l_compu_ip_short</b>
        </td>
        <td bgcolor=\"#$col_table\" align=\"center\">
           ".$compu_data->f("computer_ip")."
        </td>
        <td bgcolor=\"#$col_table\" align=\"center\">
           <img border=0 src=\"/images/$set_theme/$ico_ip\"></a>
        </td>
        <td bgcolor=\"#$col_table\" >
           $mess_ip
        </td>
      </tr>
      <tr>
        <td bgcolor=\"#$col_table\" align=\"center\">
           <b>$l_compu_name_short.$l_compu_domain_short</b>
        </td>
        <td bgcolor=\"#$col_table\" align=\"center\">
           $name
        </td>
        <td bgcolor=\"#$col_table\" align=\"center\">
           <img border=0 src=\"/images/$set_theme/$ico_name\"></a>
        </td>
        <td bgcolor=\"#$col_table\" >
           $mess_name
        </td>
      </tr>
    </table>" ;
  if($should == 1) {
    echo "
    <br><br>
    <table cellspacing=\"2\" border=\"0\" >
       <tr>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
              <img border=0 src=\"/images/$set_theme/$ico_warning\">
         </td>
        <td bgcolor=\"#$col_table\" align=\"left\">
           <b>$l_compu_should_chg</b>
        </td></tr></table>" ;
  }
}



////////////////////////////////////////////////////////////////////////////////
///                    Function to  all options display                      ///
////////////////////////////////////////////////////////////////////////////////
function dis_chge_options($option_dis_computer, $option_dis_port) {
  global $computer_id ;
  global $l_dis_choice, $l_compu_option, $l_services_option ;

  $compu_data = run_query_data_compu($computer_id) ;
  $compu_data->next_record() ;
  dis_action_compu($compu_data->f("computer_name").".".$compu_data->f("computer_domain"),
                   $compu_data->f("computer_auth_scan"),
                   $l_dis_choice) ;
  
  //Special Computer options
  echo "    <div align=\"center\">" ;
  $dis_pref = new OBM_DISPLAY("PREFERENCES",$option_dis_computer) ;
  $dis_pref->pref_element_id = $computer_id ;
  $dis_pref->pref_which = "computer" ;
  $dis_pref->pref_text = $l_compu_option ;
  $dis_pref->pref_from = "computer" ;
  $dis_pref->display_infos() ;

  //Special port options
  echo "<br><br>" ;
  $dis_pref->options_display = $option_dis_port ;
  $dis_pref->pref_which = "service" ;
  $dis_pref->pref_dis_help = 1 ;
  $dis_pref->pref_text = $l_services_option ;
  $dis_pref->display_infos() ;
}



////////////////////////////////////////////////////////////////////////////////
///         Function to display all ports we can add for a computer          ///
////////////////////////////////////////////////////////////////////////////////
function  dis_ports_toaddd($services_infos, $actual_services, 
                           $option_dis_compu, $compu_data) {
  global $computer_id, $page, $set_rows, $set_rows_default, $sess ;
  global $col_textem, 
    $col_tableh, 
    $col_table ;
  global $l_add_port, $l_services_found, $l_port_service_name, $l_port_service_port, $l_port_service_proto, $l_port_service_desc, $l_port_add, $l_port_add2, $l_compu_infos ;
  $compu_data = run_query_data_compu($computer_id) ;
  $compu_data->next_record() ;
  dis_action_compu($compu_data->f("computer_name").".".$compu_data->f("computer_domain"),
                   $compu_data->f("computer_auth_scan"),
                   $l_add_port) ;


  $dis_infos = new OBM_DISPLAY("INFORMATIONS",$option_dis_compu) ;
  $dis_infos->inf_datas = $compu_data ;
  $dis_infos->inf_which_element = $l_compu_infos ;
  $dis_infos->display_infos() ;

  $i = 0 ;
  $j = 0 ;
  $nb_line = sizeof($services_infos) ;
  while($i<$nb_line) {
    $to_show = 1 ;
    if($actual_services->nf()!=0) {
      $actual_services->seek(0) ;
      ereg("[0-9]{1,6}", $services_infos[$i]["proto_service"], $port_number) ;
      ereg("(tcp|udp)", $services_infos[$i]["proto_service"], $port_proto) ;
      while($actual_services->next_record()) {
	if(($actual_services->f("service_port") == $port_number[0])&&
	   ($actual_services->f("service_proto") == $port_proto[0])) {
	  $to_show = 0 ;
	  break ;
	}
      }
    }else {
      $to_show = 1 ;
    }
    if($to_show == 1) {
      $services_toshow[$j] = $services_infos[$i] ;
      $j++ ;
    }
    $i++ ;
  }
 
  $nb_services =  sizeof($services_toshow) ;
  
  echo "<br><div align=\"center\">
         <font color=\"#$col_textem\">
             $nb_services $l_services_found
         </font><BR>";
  // Page display management
  if ($set_rows < 1) $set_rows=$set_rows_default;
  $nb_page = floor(($nb_services-1) / $set_rows);
  $first_row = $page * $set_rows;
  $i=0;
  // Table of content (pages)
  while ($i <= ($nb_page)) {
    if ($i != 0) echo " | ";
    if ($i != $page) {
      echo "<a href=\"";
      echo $sess->url("computer_index.php?action=add_port&amp;page=$i&amp;computer_id=$computer_id")."\">";
    }
    echo ($i + 1);
    if ($i != $page) echo "</a>";
    $i++;
  }
  $i = 0 ;
  echo "
  <br><br>
  <form method=post name=f_search_compu action=".$sess->url("computer_index.php").">
    <table cellspacing=\"2\" border=\"0\" >
       <tr>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                $l_port_service_name 
            </font>
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                $l_port_service_port 
            </font>
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                $l_port_service_proto 
            </font>
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                $l_port_service_desc 
            </font>
         </td>
         <td bgcolor=\"#$col_tableh\" align=\"center\">
            <font color=\"#$col_textem\">
                $l_port_add
            </font>
         </td>
       </tr>" ;
  $j = $first_row ;
  while(($services_toshow[$j]) && ($i < $set_rows)) {
    echo "
       <tr>
         <td bgcolor=\"#$col_table\">".$services_toshow[$j]["name_service"]."</td>
         <td bgcolor=\"#$col_table\">" ;
    ereg("[0-9]{1,6}", $services_toshow[$j]["proto_service"], $port_number) ;
    echo $port_number[0]."</td>
         <td bgcolor=\"#$col_table\">" ;
    ereg("(tcp|udp)", $services_toshow[$j]["proto_service"], $port_proto) ;
    echo $port_proto[0]."</td>
         <td bgcolor=\"#$col_table\">".$services_toshow[$j]["comment_service"]."</td>" ;
    ereg("^[0-9]{1,6}",$services_toshow[$j]["proto_service"],$port) ;
    ereg("(tcp|udp)",$services_toshow[$j]["proto_service"],$proto) ;
    echo "
         <td align=\"center\" bgcolor=\"#$col_table\"><input type=\"checkbox\" name=\"".$proto[0]."_$j\" value=\"".$port[0]."\"></td>
       </tr>" ;
    $i++ ; 
    $j++ ;
  }
  echo "
       <tr><td align=\"center\" colspan=\"5\"><br>
          <input type=\"hidden\" name=\"computer_id\" value=\"$computer_id\">
          <input type=\"hidden\" name=\"action\" value=\"add_port_chosen\">
          <input type=\"submit\" value=\"$l_port_add2\"></td></tr>
 </table>
        
     </form>" ;
}



////////////////////////////////////////////////////////////////////////////////
///         Function to display the name and the choices of actions          ///
////////////////////////////////////////////////////////////////////////////////
function dis_action_compu($compu_name, $auth_port_update, $l_action) {
  global $perm ;
  global $l_compu_compu, $l_compu_infos, $l_compu_verify, $l_dis_choice, $l_add_port, $l_del_port, $l_update_port, $l_modify_compu, $l_del_compu ;

  global $computer_id, $page, $sess ;
  
  echo "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">
          <tr>
           <td align=\"center\" valign=\"top\" width=\"100%\" >
                $l_compu_compu <b>$compu_name</b> : $l_action" ;
  echo "
            </td>
          <tr>
            <td align=\"center\" valign=\"top\" width=\"100%\" nowrap>
   <form name=\"del_computer\" action=\"".$sess->url("computer_index.php")."\" method=\"post\">
       <input type=\"hidden\" name=\"action\" value=\"del_computer\">
       <input type=\"hidden\" name=\"computer_id\" value=\"$computer_id\">" ;
    echo "<a href=\"".$sess->url("computer_index.php?action=detailconsult&amp;computer_id=$computer_id")."\">$l_compu_infos</a> " ;
    echo "| <a href=\"".$sess->url("computer_index.php?action=verif_infos&amp;computer_id=$computer_id")."\">$l_compu_verify</a> " ;
    echo "| <a href=\"".$sess->url("computer_index.php?action=optionconsult&amp;computer_id=$computer_id")."\">$l_dis_choice</a><br>" ;
    if( ($auth_port_update == 1) || 
	($perm->have_perm("editor")) ) {
      echo "<a href=\"".$sess->url("computer_index.php?action=scan_ports&amp;computer_id=$computer_id")."\">$l_update_port</a> " ;
    }
    if($perm->have_perm("editor")) {
      echo "|<a href=\"".$sess->url("computer_index.php?action=add_port&amp;computer_id=$computer_id")."\">$l_add_port</a> " ;
      echo "| <a href=\"".$sess->url("computer_index.php?action=del_port&amp;computer_id=$computer_id")."\">$l_del_port</a> " ;
    }
    if($perm->have_perm("editor")) {
      echo "| <a href=\"".$sess->url("computer_index.php?action=modify_computer&amp;computer_id=$computer_id")."\">$l_modify_compu</a> " ;
      echo "| <a href=\"javascript:document.del_computer.submit();\" onClick=\"return valid_del()\">$l_del_compu</a> " ;
    }
    echo "</form>
            </td>
          </tr>
        </table>
      <br>" ;
}
?>
