<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : computer_query.inc                                          //
//     - Desc  : computer query File                                         //
// 2000-06-07 Vincent MARGUERIT : Last Update 2000-06-07                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// 2001-07-20 / Fink / corrected typo for table computer_timecreate

///////////////////////////////////////////////////////////////////////////////
//               Function to insert a new computer                           //
///////////////////////////////////////////////////////////////////////////////
//Return :
//  - 0 if we can Add this computer.
//  - 1 if the ip computer is already in use.
//  - 2 if the name_computer.domain_name is already in use.
function run_query_new_compu($tf_name, $tf_domain, $tf_ip, $tf_user, $ta_comments) {
  global $auth, $perm, $cdg_sql;

  if($perm->have_perm("editor")) {
    //Test if $tf_name_list exists:
    $connect_db = new DB_OBM ;
    $query = "select computer_ip from Computer where computer_ip = '$tf_ip' " ;
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query) ;
    if($connect_db->nf() != 0) {//That means this ip alreay exists
      return 1 ;
    } else {
      $query = "select computer_name,computer_domain from Computer where computer_name = '$tf_name' and computer_domain = '$tf_domain'" ;
      display_debug_msg($query, $cdg_sql);
      $connect_db->query($query) ;
      if($connect_db->nf() != 0) {//That means this ip alreay exists
	return 2 ;
      } else {//We can add the computer
	$tableport_name = $tf_name."_".$tf_domain ;
	$query ="insert into Computer(
                              computer_timeupdate,
                              computer_timecreate,
                              computer_userupdate,
                              computer_usercreate,
                              computer_name,
                              computer_domain,
                              computer_ip,
                              computer_user,
                              computer_comments)
                       values(
                              '".date("Y-m-d H:i:s")."',
                              '".date("Y-m-d H:i:s")."',
                              ".$auth->auth["uid"].",
                              ".$auth->auth["uid"].",
                              '$tf_name',
                              '$tf_domain',
                              '$tf_ip',
                              '$tf_user',
                              '$ta_comments')" ;
	display_debug_msg($query, $cdg_sql);
	$connect_db->query($query) ;
	return 0 ;
      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
///                      Function to modify a computer                      ///
///////////////////////////////////////////////////////////////////////////////
//Return :
//  - 0 if we can Add this computer.
//  - 1 if the ip computer is already in use.
//  - 2 if the name_computer.domain_name is already in use.
function run_query_modify_compu(
				$computer_id, $tf_name, $tf_domain, 
				$tf_ip, $tf_user, $ta_comments) {

  global $auth, $perm, $cdg_sql;
  
  $compu_data = run_query_data_compu($computer_id) ;
  $compu_data->next_record() ;

  if($perm->have_perm("editor")) {
    //Test if $tf_name_list exists:
    $connect_db = new DB_OBM ;
    $query = "select computer_ip from Computer where computer_ip = '$tf_ip' and computer_id not in($computer_id)" ;
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query) ;
    if($connect_db->nf() != 0) {//That means this ip alreay exists
      return 1 ;
    } else {
      $query = "select computer_name,computer_domain from Computer where computer_name = '$tf_name' and computer_domain = '$tf_domain' and computer_id not in($computer_id)" ;
      display_debug_msg($query, $cdg_sql);
      $connect_db->query($query) ;
      if($connect_db->nf() != 0) {//That means this name+domain alreay exists
	return 2 ;
      } else {//We can update the computer
	$query = "update Computer set computer_name = '$tf_name', computer_domain = '$tf_domain', computer_ip = '$tf_ip', computer_user = '$tf_user', computer_comments = '$ta_comments', computer_timeupdate = '".date("Y-m-d H:i:s")."' where computer_id = $computer_id" ;
	display_debug_msg($query, $cdg_sql);
	$connect_db->query($query);
	return 0 ;
      }
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
//                    Function to search computer                            //
///////////////////////////////////////////////////////////////////////////////
function run_query_search_computer($tf_name, 
				   $tf_domain, $tf_ip, $tf_user) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "select * from Computer where computer_name like '$tf_name%'
            and computer_domain like '$tf_domain%'
            and computer_ip like '$tf_ip%'
            and computer_user like '$tf_user%' order by computer_name" ;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query) ;
  return $connect_db ;
}


///////////////////////////////////////////////////////////////////////////////
///                  Function to get datas of a computer                    ///
///////////////////////////////////////////////////////////////////////////////
function run_query_data_compu($computer_id) {
  global $l_date_format, $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $connect_db = new DB_OBM ;
  
  if ($connect_db->type == $db_type_mysql) {
    $query = "select *, DATE_FORMAT(Computer.computer_timecreate,'$l_date_format') as computer_timecreate, DATE_FORMAT(Computer.computer_timeupdate,'$l_date_format') as computer_timeupdate,  DATE_FORMAT(Computer.computer_date_lastscan,'$l_date_format') as computer_date_lastscan from Computer where computer_id = $computer_id " ;
  } else if($connect_db->type == $db_type_pgsql) {
    $query = "select Computer.*, Computer.computer_timecreate as timecreate, Computer.computer_timeupdate as timeupdate, Computer.computer_date_lastscan as timelastscan from Computer where computer_id = $computer_id" ;
  }
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query) ;
  return $connect_db ;
}


///////////////////////////////////////////////////////////////////////////////
///        Function to get all the display options from ComputerDisplay     ///
///////////////////////////////////////////////////////////////////////////////
function run_query_display_option($auth,$which,$all=0) {
  //$all   ->to get even the not displayed options 
  //$which ->to get computer or service options == "computer" or == "port"

  global $l_error_which, $cdg_sql;
  
  if( (strcmp($which,"computer") != 0)&&(strcmp($which,"port") != 0) ) {
    echo $l_error_which ;
    return 0 ;
  }else {
    $connect_db = new DB_OBM ;
    $query = "select * from ComputerDisplay where " ;
    if($all == 0) {
      $query .= "display_display != 0 and " ;
    } 
    $query .= " display_user_id  = ".$auth->auth["uid"]." " ;
    if($which == "computer") {
      $query .= "and display_fieldname like 'computer_%' " ;
    }else if($which == "port") {
      $query .= "and display_fieldname like 'service_%' " ;
    }
    $query .= "order by display_fieldorder" ;
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query) ;
    /*
     if($connect_db->nf() == 0) {//That means there is no user display spec
       run_query_add_options($auth->auth["uid"], 1, 1) ;
       display_debug_msg($query, $cdg_sql);
       $connect_db->query($query) ;
    }*/

    return $connect_db ;
  }
}


///////////////////////////////////////////////////////////////////////////////
///                 Function to add one service to scan                     ///
///////////////////////////////////////////////////////////////////////////////
function run_query_add_service($computer_id, $services_infos) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  ereg("^[0-9]{1,6}",$services_infos["proto_service"],$port_number) ;
  ereg("(udp|tcp)",$services_infos["proto_service"],$port_proto) ;
  $port = $port_number[0] ;
  $query = "select * from ServiceComputer where service_computer_id = $computer_id and service_proto like '".$port_proto[0]."' and service_port = $port " ;
  display_debug_msg($query, $cdg_sql) ;
  $connect_db->query($query) ;
  if($connect_db->nf() == 0) {
    $query = "insert into ServiceComputer values($computer_id, '".$services_infos["name_service"]."', $port, '".$port_proto[0]."', '".$services_infos["comment_service"]."', 0)" ;
    display_debug_msg($query, $cdg_sql);
    $connect_db->query($query);
    return 1 ;
  }
  return 0 ;
}


///////////////////////////////////////////////////////////////////////////////
///                   Function to del services choosen                      ///
///////////////////////////////////////////////////////////////////////////////
function run_query_del_service($computer_id, $port) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "delete from ServiceComputer where service_computer_id = $computer_id and service_port = $port";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
///              Function to UPDATE if a port is OPEN os NOT                ///
///////////////////////////////////////////////////////////////////////////////
function run_query_update_service($computer_id, $services_toscan, $open) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "update ServiceComputer set service_status = $open where service_computer_id = $computer_id and service_proto like '".$services_toscan->f("service_proto")."' and service_port = ".$services_toscan->f("service_port")."" ;
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
///           Function to get all services of a computer                    ///
///////////////////////////////////////////////////////////////////////////////
function run_query_get_services($computer_id) {
  global $auth, $cdg_sql;

  $connect_db = new DB_OBM ;
  $options_order = run_query_get_options_user($auth->auth["uid" ],"order_servicecomputer") ;
  $query = "select * from ServiceComputer where service_computer_id = $computer_id order by  " ;
  $query .= (strcmp($options_order,"") != 0) ? $options_order : "service_port";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query) ;
  return $connect_db ;
}


///////////////////////////////////////////////////////////////////////////////
///                    Function to delelte a computer                       ///
///////////////////////////////////////////////////////////////////////////////
function run_query_del_computer($computer_id) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "delete from Computer where computer_id='$computer_id'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
  $query = "delete from ServiceComputer where service_computer_id='$computer_id'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
///                Function to update the last scan date                    ///
///////////////////////////////////////////////////////////////////////////////
function run_query_update_datescan($computer_id) {
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "update Computer set computer_date_lastscan = '".date("Y-m-d H:i:s")."' where computer_id = $computer_id";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
///              Function to change auth of computer scan                   ///
///////////////////////////////////////////////////////////////////////////////
function run_query_chg_authscan($computer_id, $value){
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "update Computer set computer_auth_scan='$value'
            where computer_id='$computer_id'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query) ;
}


?>
