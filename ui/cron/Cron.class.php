<?php
/**
 * Cron 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
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
    while (false !== ($jobFile = $jobsFile->read())) {
      if(is_file($jobsPath.$jobFile) && Cron::isCronJob($jobFile)) {
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
