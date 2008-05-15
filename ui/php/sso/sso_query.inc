<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : sso_query.inc                                                //
//     - Desc : SSO query & db File                                          //
// 2007-11-28 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id: user_query.inc 2293 2007-11-22 15:17:11Z pierre $
///////////////////////////////////////////////////////////////////////////////


/**
 *  
 */
function run_query_validate($ticket) {
  $ticket_dir = sys_get_temp_dir().DIRECTORY_SEPARATOR.'OBM_Ticket'.DIRECTORY_SEPARATOR;
  run_query_purge_dir($ticket_dir);
  if(!file_exists($ticket_dir.$ticket)) {
    return false;
  }
  $fdata = unserialize(file_get_contents($ticket_dir.$ticket,$buf));
  unlink($ticket_dir.$ticket);

  $obm_q = new DB_OBM;
  $query = "SELECT domain_name, userobm_password, userobm_login FROM UserObm LEFT JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_login = '$fdata[login]' AND domain_name = '$fdata[domain]'";
  $obm_q->query($query);
  if($obm_q->next_record()) {
    $login = $obm_q->f('userobm_login');
    $password = $obm_q->f('userobm_password');
    $domain = $obm_q->f('domain_name');
    return array('login' => "$login@$domain", 'password' => $password);
  } else {
    return false;
  }
}

/**
 * run_query_purge_dir 
 * 
 * @param mixed $dir 
 * @access public
 * @return void
 */
function run_query_purge_dir($dir) {
  if ($handle = opendir($dir)) {
    while (false !== ($file = readdir($handle))) {
      if ($file != "." && $file != "..") {
        $fdata = stat($dir.$file);
        if(time() >= $fdata['ctime'] + 30) {
          unlink($dir.$file);
        }
      }
    }
    closedir($handle);
  }  
}
/**
 *
 */
function run_query_create() {
  global $obm;
  $ticket_dir = sys_get_temp_dir().DIRECTORY_SEPARATOR.'OBM_Ticket'.DIRECTORY_SEPARATOR;
  if(!file_exists($ticket_dir)) {
    mkdir($ticket_dir);
  }
  $random = generateTicket();

  $handler = fopen($ticket_dir.$random,'a+');
  $buf = serialize(array('login' => "$obm[login]", 'domain' => "$obm[domain_name]",'timestamp' => time()));
  fwrite($handler,$buf);
  fclose($handler);
  return $random;
}

/**
 *
 */
function generateTicket() {
  $randlen = 32;
  $randval = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  $random = "";
  for ($i = 1; $i <= $randlen; $i++) {
    $random .= substr($randval, rand(0,(strlen($randval) - 1)), 1);
  }
  return $random;
}

?>
