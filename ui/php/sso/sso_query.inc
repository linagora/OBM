<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
  global $c_singleNameSpace;
  
  $ticket_dir = sys_get_temp_dir().DIRECTORY_SEPARATOR.'OBM_Ticket'.DIRECTORY_SEPARATOR;
  run_query_purge_dir($ticket_dir);
  if(!file_exists($ticket_dir.$ticket)) {
    return false;
  }
  $fdata = unserialize(file_get_contents($ticket_dir.$ticket,$buf));
  unlink($ticket_dir.$ticket);

  $obm_q = new DB_OBM;
  $query = "SELECT domain_name, userobm_password, userobm_login FROM UserObm LEFT JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_login = '".$obm_q->escape($fdata[login])."' AND domain_name = '$fdata[domain]'";
  $obm_q->query($query);
  if($obm_q->next_record()) {
    $login = $obm_q->f('userobm_login');
    $password = $obm_q->f('userobm_password');
    $domain = $obm_q->f('domain_name');
    if(!$c_singleNameSpace) {
      return array('login' => "$login@$domain", 'password' => $password);
    } else {
      return array('login' => $login, 'password' => $password);
    }
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
