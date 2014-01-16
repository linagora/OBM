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
    $end_time = new Of_Date($date + $this->jobDelta);
    $this->logger->debug('Getting vacation to enable before '.$end_time->getIso());
    $enable = $this->getVacationToInsert($end_time);
    $this->logger->debug('Getting vacation to disable before '.$end_time->getIso());
    $date = new Of_Date($date);
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

    $obm_q = new DB_OBM;
    $query = "SELECT userobm_id, userobm_login, userobm_domain_id, userobm_vacation_enable FROM UserObm
      WHERE  userobm_mail_perms = 1 AND userobm_vacation_enable = 0 AND
      userobm_vacation_datebegin IS NOT NULL AND
      userobm_vacation_datebegin <= '$date'";

    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->nf()." vacations to enable");
    while($obm_q->next_record()) {
      $vacation[$obm_q->f('userobm_id')] = array("login" => $obm_q->f('userobm_login'), "domain" => $obm_q->f('userobm_domain_id'), "enable" => $obm_q->f('userobm_vacation_enable'));
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
    $query = "SELECT userobm_id, userobm_login, userobm_domain_id, userobm_vacation_enable FROM UserObm
      WHERE userobm_mail_perms = 1 AND userobm_vacation_dateend <= '$date' AND userobm_vacation_dateend IS NOT NULL";
    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->nf()." vacations to disable");
    while($obm_q->next_record()) {
      $vacation[$obm_q->f('userobm_id')] = array("login" => $obm_q->f('userobm_login'), "domain" => $obm_q->f('userobm_domain_id'), "enable" => $obm_q->f('userobm_vacation_enable'));
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
      $disable = array();
      foreach ($users as $id => $user) {
        if($user['enable'] == 0) {
          $this->logger->warn("User $login domain : $domain vacation is set to be disabled but is already disabled. Noting will be done.");
        } else {
          $disable[$id] = $user;
        }
      }
      $this->logger->debug("Disabling ".count($users)."vacations in sieve");
      if(count($disable) > 0)
        $this->updateVacation($disable);
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
