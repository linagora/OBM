<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : contact_index.php                                          //
//     - Desc  : Contact Index File                                          //
// 1999-03-19 Vincent MARGUERIT : Last Update 2001-06-25                     //
///////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
// Make things dynamic for the web browser                                  //
/////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms  Management                                            //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/phplib/obmlib.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");

page_close();
$sess->start() ;//To call the method for headers (no-cache)  and so much... :)

$menu="COMPUTER";
require("$obminclude/global.inc");
include("$obminclude/global_display.inc") ;
include("$obminclude/global_query.inc") ;
include("computer_display.inc");
include("computer_query.inc");
require("computer_js.inc") ;

function get_service_file() {
  if(($file = fopen("obm-services","r"))) {
    $i = 0 ;
    while (!feof($file)) {
      $buffer = fgets($file, 4096) ;
      if($buffer[0] != "#") {
	ereg("^[a-zA-Z][a-z0-9:\-]+", $buffer, $name_service) ;
	//ereg("[0-9]{1,6}/udp",$buffer,$proto_service) ;
	ereg("[0-9]{1,6}/tcp",$buffer,$proto_service) ;
	ereg("#[a-zA-Z0-9 \-]+", $buffer, $comment_service) ;
	if( ($name_service[0] != "")&&
            ($proto_service[0] != "")&&
            ($comment_service[0] != "") ) {
	  $services_infos[$i]["name_service"] = $name_service[0] ;
	  $services_infos[$i]["proto_service"] = $proto_service[0] ;
	  $services_infos[$i]["comment_service"] = $comment_service[0] ;
	  $i++ ;
	}
      }
      $name_service[0] = "" ;
      $proto_service[0] = "" ;
      $comment_service[0] = "" ;
    }
    return $services_infos ;
  }
}

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_contact);     // Head & Body
generate_menu($menu);         // Menu
display_bookmarks();

if ($action == "index") {
  dis_compu_search_form();
  if ($set_display == "yes") {
    $db_computers = run_query_search_computer($tf_name,$tf_domain, $tf_ip, $tf_user) ;
    dis_search_computers($db_computers) ;
  } else {
    display_ok_msg($l_no_display);
  }
}

else if(($action == "search")||($action == "search_forauth")) {
  dis_compu_search_form();
  $db_computers = run_query_search_computer($tf_name,$tf_domain, $tf_ip, $tf_user) ;
  if($db_computers->nf() != 0) {
    dis_search_computers($db_computers) ;   
  }else {
    display_ok_msg($l_no_compu);
  }
}

else if($action == "new") {
  dis_compu_new_form() ;
}

else if($action == "new_computer") {
  $ret = run_query_new_compu($tf_name, $tf_domain, 
                             $tf_ip, $tf_user, $ta_comments) ;
  if($ret != 0) {
    dis_compu_new_form() ;
  } else {
    dis_compu_search_form() ;
  }
}

else if(($action == "detailconsult")||($action=="del_port")) {
  $compu_data = run_query_data_compu($computer_id) ;
  $services_list = run_query_get_services($computer_id) ;
  $option_dis_compu = run_query_display_option($auth,"computer") ;
  $option_dis_port = run_query_display_option($auth,"port") ;
  dis_infos_one_compu($compu_data, $option_dis_compu, $option_dis_port, $services_list) ;
}

else if($action == "optionconsult") {
  $option_dis_computer = run_query_display_option($auth,"computer",1) ;
  $option_dis_port = run_query_display_option($auth,"port",1) ;
  dis_chge_options($option_dis_computer, $option_dis_port) ;
}

else if($action == "ch_opt_dis") {
  $computer_id = $element_id ;
  run_query_ch_display($auth, $fieldname, $actual_option, $from) ;
  $option_dis_computer = run_query_display_option($auth,"computer",1) ;
  $option_dis_port = run_query_display_option($auth,"port",1) ;
  dis_chge_options($option_dis_computer, $option_dis_port) ;
}

else if($action == "ch_opt_level") {
  $computer_id = $element_id ;
  run_query_ch_level($auth, $which, $new_level, $fieldorder, $from) ; 
  $option_dis_computer = run_query_display_option($auth,"computer",1) ;
  $option_dis_port = run_query_display_option($auth,"port",1) ;
  dis_chge_options($option_dis_computer, $option_dis_port) ;
}

else if($action == "verif_infos") {
  $compu_data = run_query_data_compu($computer_id) ;
  dis_verif_infos($compu_data) ;
}

else if($action == "modify_computer") {
  dis_compu_new_form() ;
}

else if($action == "update_computer") {
  $ret = run_query_modify_compu($computer_id, $tf_name, $tf_domain, 
                                $tf_ip, $tf_user, $ta_comments) ;
  $services_list = run_query_get_services($computer_id) ;
  if($ret == 0) {
    $compu_data = run_query_data_compu($computer_id) ;
    $option_dis_compu = run_query_display_option($auth,"computer") ;
    $option_dis_port = run_query_display_option($auth,"port") ;
    dis_infos_one_compu($compu_data, $option_dis_compu, $option_dis_port, $services_list) ;
  }else {
    $action = "modify_computer" ;
    dis_compu_new_form() ;
  }
}

else if($action == "del_port_chosen") {
  if($perm->have_perm("editor")) {
    reset($HTTP_POST_VARS);
    $nb_port_deleted = 0 ;
    while ( list($key) = each( $HTTP_POST_VARS ) ) {
      if(strcmp(substr($key, 0, 4),"del_") == 0) {
	run_query_del_service($computer_id, substr($key,4)) ;
	$nb_port_deleted++ ;
      }
    }
    $message = "Nom" ;
    $compu_data = run_query_data_compu($computer_id) ;
    $services_list = run_query_get_services($computer_id) ;
    $option_dis_compu = run_query_display_option($auth,"computer") ;
    $option_dis_port = run_query_display_option($auth,"port") ;
    dis_infos_one_compu($compu_data, $option_dis_compu, $option_dis_port, $services_list) ;
  }
}


else if($action == "add_port") {
  $actual_services = run_query_get_services($computer_id) ;
  $services_infos = get_service_file() ;
  $option_dis_compu = run_query_display_option($auth,"computer",1) ;
  $compu_data = run_query_data_compu($computer_id) ;
  //  $option_dis_port = run_query_display_option($auth,"port",1) ;
  dis_ports_toaddd($services_infos, $actual_services, $option_dis_compu, $compu_data) ;
}


else if($action == "add_port_chosen") {
  $services_infos = get_service_file() ;
  if($perm->have_perm("editor")) {
    reset($HTTP_POST_VARS);
    $nb_port_added = 0 ;
    while ( list( $proto, $port  ) = each( $HTTP_POST_VARS ) ) {
      $proto = substr($proto, 0, 3) ;
      if( ($proto == "udp")||($proto == "tcp")) {
	$i = 0 ; 
	while($services_infos[$i]) {
	  $proto_service = $port."/".$proto ;
	  if($proto_service == $services_infos[$i]["proto_service"]) {
	    $ret = run_query_add_service($computer_id, $services_infos[$i]) ;
	    if($ret == 1) {$nb_port_added++ ;} ;
	    break;
	  }
	  $i++ ;
	}
      }
    }
  }
  $compu_data = run_query_data_compu($computer_id) ;
  $option_dis_compu = run_query_display_option($auth,"computer") ;
  $option_dis_port = run_query_display_option($auth,"port") ;
  dis_infos_one_compu($compu_data, $option_dis_compu) ;
}

else if($action == "scan_ports") {
  $compu_data = run_query_data_compu($computer_id) ;
  $compu_data->next_record() ;
  passthru("ping -c 3 ".$compu_data->f("computer_ip").">/dev/null",$ret_sys_ip) ;
  if($ret_sys_ip != 0) {
    $name = $compu_data->f("computer_name").".".$compu_data->f("computer_domain") ;
    passthru("ping -c 3 $name>/dev/null",$ret_sys_host) ;
    if($ret_sys_host != 0) {
      $name = "" ;
    }
  }else {
    $name = $compu_data->f("computer_ip") ;
  }
  if($name != "") {
    $services_toscan = run_query_get_services($computer_id) ;
    while($services_toscan->next_record()) {
      $proto = "" ;
      if($services_toscan->f("service_proto") == "udp") {
	$proto = "udp://" ;
      }
      $url = $proto.$name ;
      $fo = fsockopen("$url", $services_toscan->f("service_port"),$errno, $errstr, 10) ;
      if( ($errno == "")&&($errstr == "")) {
	if($fo) {
	  run_query_update_service($computer_id, $services_toscan, 1) ;
	} else {
	  run_query_update_service($computer_id, $services_toscan, 0) ;
	}
      }else {
	run_query_update_service($computer_id, $services_toscan, 0) ;
      }
    }
    run_query_update_datescan($computer_id) ;
  }
  $action = "detailconsult" ;
  $compu_data = run_query_data_compu($computer_id) ;
  $services_list = run_query_get_services($computer_id) ;
  $option_dis_compu = run_query_display_option($auth,"computer") ;
  $option_dis_port = run_query_display_option($auth,"port") ;
  dis_infos_one_compu($compu_data, $option_dis_compu, $option_dis_port, $services_list) ;
}

else if($action == "del_computer") {
  run_query_del_computer($computer_id) ;
  dis_compu_search_form();
  if ($set_display == "yes") {
    $db_computers = run_query_search_computer($tf_name,$tf_domain, $tf_ip, $tf_user) ;
  } else {
    display_ok_msg($l_no_display);
  }
}


else if($action == "admin") {
  if($perm->have_perm("editor")) {
    dis_compu_search_form();
    if ($set_display == "yes") {
      $db_computers = run_query_search_computer($tf_name,$tf_domain, $tf_ip, $tf_user) ;
    } else {
      display_ok_msg($l_no_display);
    }
  }
}

else if($action == "ch_auth_scan") {
  if($perm->have_perm("editor")) {
    reset($HTTP_POST_VARS);
    $nb_port_added = 0 ;
    while ( list( $computer_id, $value  ) = each( $HTTP_POST_VARS ) ) {
      if(substr($computer_id, 0, 12) == "sel_ch_auth_") {
	$computer_id = substr($computer_id,12) ;
	run_query_chg_authscan($computer_id, $value) ;
      }
    }
    $action = "search_forauth" ;
    dis_compu_search_form();
    $db_computers = run_query_search_computer($tf_name,$tf_domain, $tf_ip, $tf_user) ;
    if($db_computers->nf() != 0) {
      dis_search_computers($db_computers) ;   
    }else {
      display_ok_msg($l_no_compu);
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end() ;

</SCRIPT>
