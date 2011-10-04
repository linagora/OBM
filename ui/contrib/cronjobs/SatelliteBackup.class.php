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

include_once('CronJob.class.php');

global $obminclude;
include_once("$obminclude/of/of_acl.php");
include_once("$obminclude/of/of_category.inc");
OBM_Acl::initialize();
define('MAX_FTP_ERRORS',3);

class SatelliteBackup extends CronJob {
  /**
   * @var Logger
   */
  var $logger;
  
  function mustExecute($date) {
    $delta   = 24*60;         //every days
    $instant = (2*60)%$delta; //at 2:00
    $min = (int)($date/60);
    return ($min%$delta === $instant);
  }

  function execute($date) {
    $this->logger->debug('Doing backup of users and mailshares using the Satellite BackupEntity module');
    $domains = $this->getDomains();
    foreach ($domains as $domain_id => $domain_name) {
      $this->processDomain($domain_id, $domain_name);
    }
  }

  protected function processDomain($domain_id, $domain_name) {
    global $obminclude;
    include("$obminclude/lang/en/backup.inc");

    $this->logger->debug("Processing users and mailshares of domain $domain_name (id: $domain_id)");
    $GLOBALS['obm']['domain_id'] = $domain_id;

    $options = array('sendMail'=>false);
    $errors = array(
      'ftp' => 0,
      'users' => 0,
      'mailshares' => 0
    );
    $count = array(
      'users' => 0,
      'mailshares' => 0
    );

    $mailBody = "";

    $users = $this->getDomainUsers($domain_id);
    $count['users'] = count($users);
    $this->logger->debug($count['users']." users found in domain $domain_name (id: $domain_id)");
    foreach ($users as $user_id => $login) {
      $this->logger->debug("Processing user $login (id: $user_id)...");
      try {
        $backup = new Backup('user', $user_id);
        $result = $backup->doBackup($options);
        $this->logger->debug($result['content']);
        if (!$result['pushFtp']['success']) {
          $errors['ftp']++;
          $this->logger->warn($l_push_backup_ftp_failed);
          $this->logger->warn($result['pushFtp']['msg']);
          if ($errors['ftp'] >= MAX_FTP_ERRORS) {
            $this->logger->warn(MAX_FTP_ERRORS.' ftp errors: disable FTP push');
            $options['noFtp'] = true;
          }
        } else {
          $this->logger->debug($result['pushFtp']['msg']);
        }
        $this->logger->debug("User $login (id: $user_id) backup finished");
      } catch (Exception $e) {
        $errors['users']++;
        $mailBody .=
        __("error when processing user %login%@%domain_name% (id: %user_id%)\n",
            array(
                   "login"=>$login,
                   "domain_name"=>$domain_name,
                   "user_id"=>$user_id
                 )
        )."\n";
        $this->logger->error("error when processing user $login@$domain_name (id: $user_id)");
        $mailBody .= $e->getMessage()."\n";
        $this->logger->error($e->getMessage());
      }
    }

    $mailshares = $this->getDomainMailshares($domain_id);
    $count['mailshares'] = count($mailshares);
    $this->logger->debug($count['mailshares']." mailshares found in domain $domain_name (id: $domain_id)");
    foreach ($mailshares as $mailshare_id => $mailshare_name) {
      $this->logger->debug("Processing mailshare $mailshare_name (id: $mailshare_id)...");
      try {
        $backup = new Backup('mailshare', $mailshare_id);
        $result = $backup->doBackup($options);
        $this->logger->debug($result['content']);
        if (!$result['pushFtp']['success']) {
          $errors['ftp']++;
          $this->logger->warn($l_push_backup_ftp_failed);
          $this->logger->warn($result['pushFtp']['msg']);
          if ($errors['ftp'] >= MAX_FTP_ERRORS) {
            $this->logger->warn(MAX_FTP_ERRORS.' ftp errors: disable FTP push');
            $options['noFtp'] = true;
          }
        } else {
          $this->logger->debug($result['pushFtp']['msg']);
        }
        $this->logger->debug("Mailshare $mailshare_name (id: $mailshare_id) backup finished");
      } catch (Exception $e) {
        $errors['mailshares']++;
        $mailBody .=
        __("error when processing mailshare %mailshare_name%@%domain_name% (id: %user_id%)",
            array(
                   "mailshare_name" =>  $mailshare_name,
                   "domain_name"    =>  $domain_name,
                   "user_id"        =>  $mailshare_id
                 )
        )."\n";
        $this->logger->error("error when processing mailshare $mailshare_name@$domain_name (id: $mailshare_id)");
        $mailBody .= $e->getMessage();
        $this->logger->error($e->getMessage());
      }
    }

    $mailIntro = __("%count% users backuped successfully",
                    array("count" => $count['users']-$errors['users'])
                 )."\n";
    if ($errors['users'] > 0) {
      $mailIntro .= __("%count% errors on users",
                       array("count" => $errors['users'])
                    )."\n";
    }
    $mailIntro .= __("%count% mailshares backuped successfully",
                     array("count" => $count['mailshares']-$errors['mailshares'])
                  )."\n";
    if ($errors['mailshares'] > 0) {
      $mailIntro .= __("%count% errors on mailshares",
                       array("count" => $errors['mailshares'])
                    )."\n";
    }
    if ($errors['ftp'] >= MAX_FTP_ERRORS) {
      $mailIntro .= __("%count% ftp errors",
                       array("count" => $errors['ftp'])
                    )."\n";
      $mailIntro .= __("FTP push has been disabled after %count% errors",
                       array("count" => MAX_FTP_ERRORS)
                    )."\n";
    }

    $totalErrors = array_sum($errors);
    if ($totalErrors > 0) {
      $mailSubject = __("backup of users and mailshares of domain %domain_name% completed with %count% errors",
                           array(
                               "%domain_name%"=>  $domain_name,
                               "count"        =>  $totalErrors
                           )
                     );
    } else {
      $mailSubject = __("backup of users and mailshares of domain %domain_name% completed successfully",
                         array("%domain_name%" => $domain_name)
                     );
    }

    $mailTo = "x-obm-backup@$domain_name";
    $mailFrom = $mailTo;
    $mailBody = $mailIntro."\n".$mailBody;

    send_mail_from($mailSubject, $mailBody, $mailFrom, array(), array($mailTo), false);
  }


  protected function getDomainMailshares($domain_id) {
    $mailshares = array();
    $obm_q = new DB_OBM;
    $query = "SELECT mailshare_id, mailshare_name
      FROM MailShare
      WHERE mailshare_archive=0 AND mailshare_domain_id=$domain_id";
    $this->logger->core($query);
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $mailshares[$obm_q->f('mailshare_id')] = $obm_q->f('mailshare_name');
    }
    return $mailshares;
  }

  protected function getDomainUsers($domain_id) {
    $users = array();
    $obm_q = new DB_OBM;
    $query = "SELECT userobm_id, userobm_login
      FROM UserObm
      WHERE userobm_archive=0 AND userobm_domain_id=$domain_id AND userobm_mail_perms=1";
    $this->logger->core($query);
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $users[$obm_q->f('userobm_id')] = $obm_q->f('userobm_login');
    }
    return $users;
  }

  protected function getDomains() {
    $domains = array();
    $obm_q = new DB_OBM;
    $query = "SELECT domain_id, domain_name FROM Domain WHERE domain_global=0";
    $this->logger->core($query);
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $domains[$obm_q->f('domain_id')] = $obm_q->f('domain_name');
    }
    return $domains;
  }
}
?>
