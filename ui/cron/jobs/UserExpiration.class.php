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

class UserExpiration extends CronJob {

  var $logger;

  function mustExecute($date) {
    $delta   = 24*60;         //every days
    $instant = (6*60)%$delta; //at 6:00
    $min = (int)($date/60);
    return ($min%$delta === $instant);
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
