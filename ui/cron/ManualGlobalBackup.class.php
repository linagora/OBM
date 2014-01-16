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


$path = pathinfo(__FILE__);
$path = $path['dirname'];
$includePath = realpath("$path/jobs").":".realpath("$path/..");
$jobsPath = "$path/jobs/";
$cronJobProcessing = true;

ini_set('error_reporting', E_ALL & ~E_NOTICE);
ini_set('include_path', ".:$includePath");
  
//set the timezone before any date function to avoid the php 5.3 warning
date_default_timezone_set('GMT');
$obminclude = 'obminclude';   
include_once("$obminclude/global.inc");
include_once("Logger.class.php");
include_once("Cron.class.php");
  
define("L_LEVEL", L_INFO);
  
set_error_handler('errorHandler');
    
    
SI18n::set_default_locale($set_lang_default);

include_once('CronJob.class.php');


class ManualGlobalBackup extends CronJob {
  var $logger;
  var $request;
  var $backup;

  function mustExecute($date) {
    $delta   = 24*60;         //every days
    $instant = (2*60)%$delta; //at 2:00
    $min = (int)($date/60);
    return ($min%$delta === $instant);
  }
  
  function execute($domain_id, $backend, $offset, $nbUsers) {
    $this->logger->debug('Doing backup of users and mailshares using the Satellite BackupEntity module');
    $domain_name = $this->getDomainName($domain_id);
    $this->processDomain($domain_id, $domain_name, $backend, $offset, $nbUsers);
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

  function notifyEndOfBackups() {
    $hosts = $this->getSatelliteHosts();
    
    foreach ($hosts as $host) {
      $args = array("host" => $host);
      $this->prepareCurl($args);
      $this->logger->info('Notify obm-satellite (' . $host . ') of the end of backups');
      $body = curl_exec($this->request);
      $code = curl_getinfo($this->request, CURLINFO_HTTP_CODE);
      if ($body===FALSE)
        throw new Exception(curl_error($this->request));
      
      curl_close($this->request);
      if (($code >= 200) && ($code < 300)) {
        $this->logger->info('Successful file has been created in backupRoot directory on satellite host');
      } else {
        $this->logger->error("Unexpected response code ($code) from the satellite");
      }
    }
  }

  protected function getSatelliteHosts() {
    $obm_q = new DB_OBM;
    $smtp_query = "SELECT
        host_ip
        FROM Host
        WHERE host_id IN (SELECT userobm_mail_server_id FROM UserObm GROUP BY userobm_mail_server_id)";
    $obm_q->query($smtp_query);
    $smtp_servers = array();
    $hosts = array();
    
    while ($obm_q->next_record()) {
      $hosts[] = $obm_q->f('host_ip');
    }
    
    return $hosts;
  }

  protected function processDomain($domain_id, $domain_name, $backend, $offset, $nbUsers) {
  	global $obminclude, $admin_mail_address;

    $this->logger->info("Processing users and mailshares of domain $domain_name (id: $domain_id, backend: $backend)");
    $GLOBALS['obm']['domain_id'] = $domain_id;

    $options = array('sendMail'=>false);
    $errors = 0;
    $count = 0;
    $mailBody = "";
    $mailIntro = "";

    $this->processUsersBackup($domain_id, $options, $errors, $mailBody, $count, $backend, $offset, $nbUsers);

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

  protected function processUsersBackup($domain_id, $options, &$errors, &$mailBody, &$count, $backend, $offset, $nbUsers) {
  	$users = $this->getDomainUsers($domain_id, $backend, $offset, $nbUsers);
  	$count = count($users);
  	$this->logger->debug($count." users found in this domain (id: $domain_id, backend: $backend)");
  	foreach ($users as $user_id => $login) {
  		$this->logger->debug("Processing user : $login (id: $user_id)...");
  		try {
  			$this->backup = new Backup('user', $user_id);
  			$result = $this->backup->doBackup($options);
  			$this->logger->debug("User $login (id: $user_id) backup finished");
  		} catch (Exception $e) {
  			$errors++;
  			$mailBody .= "Error when processing user : $login (id: $user_id) :\n";
  			$this->logger->error("Error when processing user $login (id: $user_id), $e");
  			$mailBody .= "-> ".$e->getMessage()."\n";
  			$this->logger->error($e->getMessage());
  		}
  	}
  }

  protected function getDomainUsers($domain_id, $backend, $offset, $nbUsers) {
    $users = array();
    $obm_q = new DB_OBM;
    $query = 
     "SELECT userobm_id, userobm_login
      FROM UserObm
      WHERE userobm_archive=0 
      AND userobm_mail_server_id=$backend
      AND userobm_domain_id=$domain_id 
      AND userobm_mail_perms=1
      LIMIT $nbUsers OFFSET $offset;";
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

  protected function getDomainName($domain_id) {
    $obm_q = new DB_OBM;
    $query = 
     "SELECT domain_name 
      FROM Domain 
      WHERE domain_global='f' 
      AND domain_id=$domain_id";
    $obm_q->query($query);
    $obm_q->next_record();

    return $obm_q->f('domain_name');
  }

  protected function buildUrl($args) {
    extract($args);
    $port = OBM_Satellite_Icredentials::$port;
    return "https://{$host}:{$port}/endOfBackups/";
  }

  function countUsersByDomain() {
    $domains = $this->getDomains();
    $backupFile = fopen('/tmp/globalBackups.init', 'w+');

    foreach ($domains as $domain_id => $domain_name) {
        $usersByBackend = $this->countUsers($domain_id);
        fputs($backupFile, $domain_id."=".$usersByBackend."\n");
    }

    fclose($backupFile);
  }

  protected function countUsers($domain_id) {
    $obm_q = new DB_OBM;
    $query = 
     "SELECT userobm_mail_server_id as backend, count(userobm_id) as count 
      FROM UserObm 
      WHERE userobm_archive=0 
      AND userobm_domain_id=$domain_id 
      AND userobm_mail_perms=1
      GROUP BY userobm_mail_server_id";
    $obm_q->query($query);
    
    $usersByBackend = array();
    while ($obm_q->next_record()) {
      $usersByBackend[] = $obm_q->f('backend') . ':' . $obm_q->f('count');
    }

    return implode(' ', $usersByBackend);
  }

  function usage() {
    echo "OPTIONS\n";
    echo "\t--init-backup\n";
    echo "\t   Create a /tmp/globalBackups.init file with a domain_id=nbUsers format.\n";
    echo "\t--backup --domain=x --offset=y --nbUsers=z --backend=b\n";
    echo "\t   Do the backup on the domain x of z users, starting at the y one.\n";
    echo "\t--end-backup\n";
    echo "\t   Notify the satellite the end of the backup.\n";
  }
};


$manualGlobalBackup = new ManualGlobalBackup;

if (strcmp($argv[1], "--init-backup") == 0) {
    $manualGlobalBackup->countUsersByDomain();
} else if (strcmp($argv[1], "--end-backup") == 0) {
    $manualGlobalBackup->notifyEndOfBackups();
} else if (strcmp($argv[1], "--backup") == 0 && count($argv) == 6) { 
    $longopts = array("domain:", "offset:", "nbUsers:", "backend:");
    $options = getopt(null, $longopts);
    if (count($options) != 4) {
      $manualGlobalBackup->usage();
    } else {	
      $manualGlobalBackup->execute($options['domain'], $options['backend'], $options['offset'], $options['nbUsers']);
    }
} else {
    $manualGlobalBackup->usage();
}


?>
