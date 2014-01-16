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

  $obm_q = new DB_OBM();
  $where = 'userobm_perms = \'admin\' AND userobm_email IS NOT NULL AND userobm_email != \' \'';

  if(isset($params['report_delegation']) && $params['report_delegation'] != '') {
    $delegation_target = $params['report_delegation'];
    $where .= " AND userobm_delegation_target = '".$obm_q->escape($delegation_target)."'";
  }
  
  if(isset($obm['domain_id']) && $obm['domain_id'] != '') {
    $domain_id = $obm['domain_id'];
    $where .= " AND userobm_domain_id = $domain_id";
  }

  if(isset($params['service']) && $params['service'] != '') {
    $service = $params['service'];
    $where .= " AND userobm_service = '".$obm_q->escape($service)."'";
  }

  $query = "
    SELECT userobm_email
    FROM UserObm
    WHERE $where
    ";
  
  display_debug_msg($query, $cdg_sql, 'get_report_mail()');

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
