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
include_once('CronJob.class.php');

class VacationCronJob extends CronJob{

  var $jobDelta = 900;

  /**
   * mustExecute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function mustExecute($date) {
    global $cgp_use;
    if ($cgp_use["service"]["mail"]) {
      $min = date('i');
      return ($min%15 === 0);
    } else {
      return false;
    }
  }

  /**
   * getJobsFiles 
   * 
   * @access public
   * @return void
   */
  function getJobsFiles() {
    return array('php/vacation/vacation_query.inc');
  }

  /**
   * execute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function execute($date) {
    $delta = $this->jobDelta - 1;
    $end_time = $date + $this->jobDelta;
    $this->logger->debug('Getting vacation to enable before '.date('Y-m-d H:i:s',$end_time));
    $enable = $this->getVacationToInsert($end_time);
    $this->logger->debug('Getting vacation to disable before '.date('Y-m-d H:i:s',$end_time));
    $disable = $this->getVacationToRemove($date);
    $intersec = array_intersect(array_keys($enable),array_keys($disable));
    if(count($intersec) != 0) {
      $this->logger->warn(count($intersec).' vacation messages are set to be enabled AND disabled in the same job : '.implode(',',$intersec));
      foreach($intersec as $id) {
        unset($enable[$id]);
      }
    }
    $this->enableVacation($enable);
    $this->disableVacation($disable);
    
  }

  /**
   * getVacationToInsert 
   * 
   * @param mixed $end_time 
   * @access public
   * @return void
   */
  function getVacationToInsert($date) {
    $vacation = array();
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $vacation_datebegin = sql_date_format($db_type,"userobm_vacation_datebegin");

    $obm_q = new DB_OBM;
    $query = "SELECT userobm_id, userobm_login, userobm_domain_id FROM UserObm
      WHERE userobm_vacation_enable = 0 AND
      $vacation_datebegin > 0 AND
      $vacation_datebegin <= $date";

    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->nf()." vacations to enable");
    while($obm_q->next_record()) {
      $vacation[$obm_q->f('userobm_id')] = array("login" => $obm_q->f('userobm_login'), "domain" => $obm_q->f('userobm_domain_id'));
    }
    $this->logger->info('List of vacation to enable : '.implode(',', array_keys($vacation)));
    return $vacation;
  }

  /**
   * getVacationToInsert 
   * 
   * @param mixed $end_time 
   * @access public
   * @return void
   */
  function getVacationToRemove($date) {
    $vacation = array();
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $vacation_dateend = sql_date_format($db_type,"userobm_vacation_dateend");

    $obm_q = new DB_OBM;
    $query = "SELECT userobm_id, userobm_login, userobm_domain_id FROM UserObm
      WHERE  $vacation_dateend <= $date AND $vacation_dateend > 0";

    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->nf()." vacations to disable");
    while($obm_q->next_record()) {
      $vacation[$obm_q->f('userobm_id')] = array("login" => $obm_q->f('userobm_login'), "domain" => $obm_q->f('userobm_domain_id'));
    }
    $this->logger->info('List of vacation to disable : '.implode(',', array_keys($vacation)));
    return $vacation;
  }  
  /**
   * enableVacation 
   * 
   * @param mixed $users 
   * @access public
   * @return void
   */
  function enableVacation($users) {
    if(count($users) > 0) {
      $this->logger->debug("Enabling ".count($users)."vacations in main table");
      $obm_q = new DB_OBM;
      $query = "UPDATE UserObm set userobm_vacation_enable = 1 WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $this->logger->debug("Enabling ".count($users)."vacations in P table");
      $obm_q = new DB_OBM;
      $query = "UPDATE P_UserObm set userobm_vacation_enable = 1 WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $this->logger->debug("Enabling ".count($users)."vacations in sieve");
      $this->updateVacation($users);
    }
  }
  /**
   * disableVacation 
   * 
   * @param mixed $users 
   * @access public
   * @return void
   */
  function disableVacation($users) {
    if(count($users) > 0) {
      $this->logger->debug("Disabling ".count($users)."vacations in main table");
      $obm_q = new DB_OBM;
      $query = "UPDATE UserObm set 
        userobm_vacation_enable = 0, 
        userobm_vacation_datebegin = NULL,
        userobm_vacation_dateend = NULL 
        WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $this->logger->debug("Disabling ".count($users)."vacations in P table");
      $obm_q = new DB_OBM;
      $query = "UPDATE P_UserObm set userobm_vacation_enable = 0, 
        userobm_vacation_datebegin = NULL,
        userobm_vacation_dateend = NULL
        WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $this->logger->debug("Disabling ".count($users)."vacations in sieve");
      $this->updateVacation($users);
    }    
  }
  /**
   * enableVacation 
   * 
   * @param mixed $users 
   * @access public
   * @return void
   */
  function updateVacation($users) {
    foreach($users as $user_identity) {
      $login = $user_identity['login'];
      $domain = $user_identity['domain'];
      $this->logger->debug("Calling sieve automate for login : $login domain : $domain");
      update_sieve(array(), $login, $domain); 
    }
  }
}
