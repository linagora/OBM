<?php
include_once("php/vacation/vacation_query.inc");
include_once("CronJob.class.php");

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
    return true;
    $min = date("i");
    return ($min%15 === 0);
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
      $vacation_datebegin < $date";

    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->nf()." vacations to enable");
    while($obm_q->next_record()) {
      $vacation[$obm_q->f('userobm_id')] = array($obm_q->f('userobm_login'),$obm_q->f('userobm_domain_id'));
    }
    $this->logger->info('List of vacation to enable : '.implode(',', $vacation));
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
      WHERE  $vacation_dateend > $date";

    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->nf()." vacations to disable");
    while($obm_q->next_record()) {
      $vacation[$obm_q->f('userobm_id')] = array($obm_q->f('userobm_login'),$obm_q->f('userobm_domain_id'));
    }
    $this->logger->info('List of vacation to disable : '.implode(',', $vacation));
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
      $obm_q = new DB_OBM;
      $query = "UPDATE UserObm set userobm_vacation_enable = 1 WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $obm_q = new DB_OBM;
      $query = "UPDATE P_UserObm set userobm_vacation_enable = 1 WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
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
      $obm_q = new DB_OBM;
      $query = "UPDATE UserObm set userobm_vacation_enable = 0 WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $obm_q = new DB_OBM;
      $query = "UPDATE P_UserObm set userobm_vacation_enable = 0 WHERE userobm_id IN (".implode(',',array_keys($users)).")";
      $this->logger->core($query);
      $obm_q->query($query);      
      $this->updateVacation($users);
    }    
    $this->updateVacation($users);
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
      list($login, $domain) = split("@",$user_identity);
      update_sieve(array(), $login, $domain); 
    }
  }
}
