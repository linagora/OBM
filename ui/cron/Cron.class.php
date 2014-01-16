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
 * Cron 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class Cron {
  
  /**
   * jobs 
   * 
   * @var mixed
   * @access public
   */
  var $jobs;

  /**
   * date 
   * 
   * @var mixed
   * @access public
   */
  var $date;

  /**
   * logger 
   * 
   * @var mixed
   * @access public
   */
  var $logger;



  /**
  * PID file
  *
  * @var string
  * @access private
  */
  var $pidfile = "/var/run/obm-core.cron.php.pid";

  /**
   * Cron 
   * 
   * @param mixed $jobsPath 
   * @access public
   * @return void
   */
  function Cron($jobsPath) {
    $this->logger = new Logger(get_class($this));
    $jobs = array();
    $this->date = time();
    $this->logger->info("Executing OBM crontab at ".date("Y-m-d H:i:s",$this->date));
    $this->parseJobs($jobsPath);
    $this->orderJobs();
  }

  /**
   * Build the jobs array with taking in account the pre/post dependencies.
   * 
   * @access public
   * @return void
   */
  function orderJobs() {
    // TODO Here comes code to have pre and/or post depends on jobs.
  }

  /**
   * pars the jobs repository to pre-fill the jobs array
   * 
   * @param mixed $jobsPath 
   * @access public
   * @return void
   */
  function parseJobs($jobsPath) {
    $jobsFile = dir($jobsPath);
    $jobsToExecute = false;

    if(file_exists($jobsPath."../jobsToExecute.ini"))
      $jobsToExecute = parse_ini_file($jobsPath."../jobsToExecute.ini");

    while (false !== ($jobFile = $jobsFile->read())) {
      if(     (is_file($jobsPath.$jobFile) || is_link($jobsPath.$jobFile))
          &&  Cron::isCronJob($jobFile)
          &&  (!$jobsToExecute || in_array($jobFile, $jobsToExecute["jobs"]))) {
        $klass = Cron::getCronJobClass($jobFile);
        $this->logger->debug("$klass registred");
        include_once($jobsPath.$jobFile);
        $this->jobs[] = new $klass;
      }
    }
    $jobsFile->close();
  }

  /**
   * Lock the cron process 
   * 
   * @access public
   * @return void
   */
  function lock() {
    $this->logger->debug("Trying to get lock");
    if ( file_exists($this->pidfile) ) {
      $pid = trim(file_get_contents($this->pidfile));
      if ( $pid && file_exists("/proc/".$pid) ) {
        $this->logger->debug("Another cron process, PID $pid, is already running");
        return false;
      }
    }
    $ok = file_put_contents($this->pidfile, getmypid());
    if ($ok===false) {
      $this->logger->debug("Unable to write to file ".$this->pidfile);
      return false;
    }
    $this->logger->debug("Cron locked");
    return true;
  }

  /**
   * unlock the cron process
   * TODO : Find a way to force unlock, even if the process is crashed. 
   * 
   * @access public
   * @return void
   */
  function unlock() {
    $this->logger->debug("Unlocking cron process");
    $ok = @unlink($this->pidfile);
    if ( !$ok ) {
      $this->logger->debug("Failed to unlink ".$this->pidfile);
    }
  }

  /**
   * Execute cron Jobs 
   * 
   * @access public
   * @return void
   */
  function process() {
    if($this->lock()) {
      foreach($this->jobs as $job) {
        if($job->mustExecute($this->date)) {
          $this->logger->info(get_class($job)." will be executed");
          $files = $job->getJobsFiles();
          foreach($files as $file) {
            include_once($file);
          }
          $job->execute($this->date); 
        } else {
          $this->logger->info(get_class($job)." will not be executed");
        }
      }
      $this->unlock();
    }
  }

  /**
   * is a file a cron job file 
   * 
   * @param mixed $fileName 
   * @access public
   * @return void
   */
  function isCronJob($fileName) {
    return preg_match("/^.*\.class.php$/",$fileName);
  }

  /**
   * Get a cron job class name from file.
   * TODO : could be getFileClassName
   * 
   * @param mixed $fileName 
   * @access public
   * @return void
   */
  function getCronJobClass($fileName) {
    preg_match("/^(.*)\.class.php$/",$fileName,$className);
    return $className[1];
  }  
}


?>
