<?php
include_once('CronJob.class.php');

class UserExpiration extends CronJob {

  var $logger;
  
  function mustExecute($date) {
    global $cgp_use;
    if ($cgp_use["service"]["user"]) {
      $hours = date('G');
      return ($hours == 6);
    } else {
     return false;
		}
  }

  function execute($date) {
    $this->logger->debug('Disable account where expery date is before '.date('Y-m-d H:i:s'));
    $enable = $this->DisableExpireAccount();
    //$this->logger->debug('Getting vacation to disable before '.date('Y-m-d H:i:s',$end_time));
    //$disable = $this->EnableAccount();
  }
	
	function DisableExpireAccount()	{
    $this->logger->debug("Disable account older than ".date("Y-m-d",$date));

    $obm_q = new DB_OBM;
		$query = "UPDATE UserObm SET userobm_archive=1 
						WHERE TIMESTAMPDIFF(DAY,now(),userobm_account_dateexp)<0";
    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->affected_rows()." alerts Disable");
	}

	function EnableAccount()	{
    $this->logger->debug("Enable account");

    $obm_q = new DB_OBM;
		$query = "UPDATE Userobm set userobm_archive=0 
						WHERE TIMESTAMPDIFF(DAY,now(),userobm_account_dateexp)>=0 AND userobm_archive=1";
    $this->logger->core($query);
    $obm_q->query($query);
    $this->logger->info($obm_q->affected_rows()." alerts Enable");
	}
}
?>
