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

class GlobalBackup extends CronJob {
  var $logger;
  var $request;
  var $backup;

  function mustExecute($date) {
    $delta   = 24*60;         //every days
    $instant = (2*60)%$delta; //at 2:00
    $min = (int)($date/60);
    return ($min%$delta === $instant);
  }
  
  function execute() {
    $this->logger->info('Doing backup of users and mailshares using the Satellite BackupEntity module');
    $domains = $this->getDomains();
    foreach ($domains as $domain_id => $domain_name) {
      $this->processDomain($domain_id, $domain_name);
    }
    try {
      $this->prepareCurl($this->backup->getDetails());
      $this->notifyEndOfBackups();
    } catch (Exception $e) {
      $this->logger->debug($e->getMessage());
    }
  }

  protected function prepareCurl($args) {
    global $host_satellite, $port_satellite;

    $this->logger->debug('Preparing curl to notify obm-satellite');
    $this->request = curl_init();
    $auth = new SatelliteAuth('obmsatelliterequest');

    // default options
    $options = array(
      CURLOPT_URL => $this->buildUrl($args),
      CURLOPT_HTTPAUTH => CURLAUTH_BASIC,
      CURLOPT_USERPWD => $auth->credentials,
      CURLOPT_SSL_VERIFYPEER => FALSE,
      CURLOPT_SSL_VERIFYHOST => FALSE
    );
    $options[CURLOPT_POST] = TRUE;
    $options[CURLOPT_RETURNTRANSFER] = TRUE;
    $options[CURLOPT_HEADER] = FALSE;
    $options[CURLOPT_FORBID_REUSE] = TRUE;
    $options[CURLOPT_HTTPHEADER] = array('Expect: ');

    $retour = curl_setopt_array($this->request, $options);
    if ($retour===FALSE)
      throw new Exception('Invalid query options');
  }

  protected function notifyEndOfBackups() {
    $this->logger->info('Notify obm-satellite of the end of backups');
    $body = curl_exec($this->request);
    $code = curl_getinfo($this->request, CURLINFO_HTTP_CODE);
    if ($body===FALSE)
      throw new Exception(curl_error($this->request));

    curl_close($this->request);
    if (($code >= 200) && ($code < 300)) {
      $this->logger->info('Successful file has been created in backupRoot directory on satellite host');
      return;
    }

    throw new Exception("$code: Unexpected response code from the satellite");
  }

  protected function processDomain($domain_id, $domain_name) {
  	global $obminclude, $admin_mail_address;

    $this->logger->info("Processing users and mailshares of domain $domain_name (id: $domain_id)");
    $GLOBALS['obm']['domain_id'] = $domain_id;

    $options = array('sendMail'=>false);
    $errors = 0;
    $count = 0;
    $mailBody = "";
    $mailIntro = "";

	$this->processUsersBackup($domain_id, $options, $errors, $mailBody, $count);

    if ($errors > 0) {
        $mailSubject = "Errors with the daily obm backups";
        $mailIntro .="$errors errors on global backups for the domain $domain_name\n";
	    $mailTo = $admin_mail_address ? $admin_mail_address : "x-obm-backup@$domain_name";
	    $mailFrom = "x-obm-backup@$domain_name";
	    $mailBody = $mailIntro."\n".$mailBody;

        $this->logger->info("Sending mail to $mailTo because there are some errors in the backup for domain : $domain_name");
	    send_mail_from($mailSubject, $mailBody, $mailFrom, array(), array($mailTo), false);
    }
  }

  protected function processUsersBackup($domain_id, $options, &$errors, &$mailBody, &$count) {
  	$users = $this->getDomainUsers($domain_id);
  	$count = count($users);
  	$this->logger->debug($count." users found in this domain (id: $domain_id)");
  	foreach ($users as $user_id => $login) {
  		$this->logger->debug("Processing user : $login (id: $user_id)...");
  		try {
  			$this->backup = new Backup('user', $user_id);
  			$result = $this->backup->doBackup($options);
  			$this->logger->debug("User $login (id: $user_id) backup finished");
  		} catch (Exception $e) {
  			$errors++;
  			$mailBody .= "Error when processing user : $login (id: $user_id) :\n";
  			$this->logger->error("Error when processing user $login (id: $user_id)");
  			$mailBody .= "-> ".$e->getMessage()."\n";
  			$this->logger->error($e->getMessage());
  		}
  	}
  }

  protected function getDomainUsers($domain_id) {
    $users = array();
    $obm_q = new DB_OBM;
    $query = 
     "SELECT userobm_id, userobm_login
      FROM UserObm
      WHERE userobm_archive=0 
      AND userobm_domain_id=$domain_id 
      AND userobm_mail_perms=1;";
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $users[$obm_q->f('userobm_id')] = $obm_q->f('userobm_login');
    }
    return $users;
  }

  protected function getDomains() {
    $domains = array();
    $obm_q = new DB_OBM;
    $query = 
     "SELECT domain_id, domain_name 
      FROM Domain 
      WHERE domain_global='f'";
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $domains[$obm_q->f('domain_id')] = $obm_q->f('domain_name');
    }
    return $domains;
  }

  protected function buildUrl($args) {
    extract($args);
    $port = OBM_Satellite_Icredentials::$port;
    return "https://{$host}:{$port}/endOfBackups/";
  }
};
?>
