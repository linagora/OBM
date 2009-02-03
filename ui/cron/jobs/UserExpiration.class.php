<?php
include_once('CronJob.class.php');

class UserExpiration extends CronJob {

  var $logger;

  function mustExecute($date) {
    $hours = date('G');
    return ($hours == 6);
  }

  function execute($date) {
    $this->logger->debug('Disable account where expery date is before '.date('Y-m-d H:i:s'));
    $enable = $this->DisableExpireAccount();
  }

  function DisableExpireAccount()	{
    $this->logger->debug("Disable account older than ".date("Y-m-d",$date));

    $obm_q = new DB_OBM;
    $query = "UPDATE UserObm SET userobm_archive=1 
      WHERE #DAYDIFF(now(),userobm_account_dateexp)<0";
    $this->logger->core($query);
    $obm_q->xquery($query);
    $this->logger->info($obm_q->affected_rows()." accounts disabled");
  }

  function EnableAccount()	{
    $this->logger->debug("Enable account");

    $obm_q = new DB_OBM;
    $query = "UPDATE Userobm set userobm_archive=0 
      WHERE #DAYDIFF(now(),userobm_account_dateexp)>=0 AND userobm_archive=1";
    $this->logger->core($query);
    $obm_q->xquery($query);
    $this->logger->info($obm_q->affected_rows()." account enabled");
  }
}
?>
