<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<?php

/**
 * get_user_service return all the service of the user 
 * 
 * @access public
 * @return void
 */
function get_report_user_service() {
  global $obm_q, $obm, $cdg_sql;

  $domain_id = $obm['domain_id'];
  
  $where = "
    userobm_service IS NOT NULL
    AND userobm_service != ''
    ";

  if ($domain_id != ''){
    $where .= " AND userobm_domain_id = $domain_id";
  }

  $query = "
    SELECT DISTINCT userobm_service
    FROM UserObm
    WHERE $where
    ";

  display_debug_msg($query, $cdg_sql, 'get_report_user_service()');

  $obm_q = new DB_OBM();
  $obm_q->query($query);

  while ($obm_q->next_record()) {
      $retour[] = $obm_q->f('userobm_service');
  }

  return $retour;
}


/**
 * get_report_admin_mail  
 * 
 * @param mixed $service 
 * @param mixed $user 
 * @access public
 * @return array with the admin mail
 */
function get_report_mail_admin($params) {
  global $obm, $cgd_sql, $cmail_report;

  $where = 'userobm_perms = \'admin\' AND userobm_email IS NOT NULL AND userobm_email != \' \'';

  if(isset($params['report_delegation']) && $params['report_delegation'] != '') {
    $delegation_target = $params['report_delegation'];
    $where .= " AND userobm_delegation_target = '$delegation_target'";
  }
  
  if(isset($obm['domain_id']) && $obm['domain_id'] != '') {
    $domain_id = $obm['domain_id'];
    $where .= " AND userobm_domain_id = $domain_id";
  }

  if(isset($params['service']) && $params['service'] != '') {
    $service = $params['service'];
    $where .= " AND userobm_service = '$service'";
  }

  $query = "
    SELECT userobm_email
    FROM UserObm
    WHERE $where
    ";
  
  display_debug_msg($query, $cdg_sql, 'get_report_mail()');

  $obm_q = new DB_OBM();
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $mail[] = $obm_q->f('userobm_email');
  }
  $mail[] = $cmail_report;
  return $mail;
}

/**
 * get_report_delegation_user 
 * 
 * @access public
 * @return array with the delegation
 */
function get_report_delegation_user() {
  global $obm_q, $obm, $cdg_sql;

  $domain_id = $obm['domain_id'];
  
  $where = "
    userobm_service IS NOT NULL
    AND userobm_service != ''
    ";

  if ($domain_id != ''){
    $where .= " AND userobm_domain_id = $domain_id";
  }

  $query = "
    SELECT DISTINCT userobm_delegation
    FROM UserObm
    WHERE $where
    ";

  display_debug_msg($query, $cdg_sql, 'get_report_delegation_user()');

  $obm_q = new DB_OBM();
  $obm_q->query($query);

  while ($obm_q->next_record()) {
      $retour[] = $obm_q->f('userobm_delegation');
  }

  return $retour;
}

?>
