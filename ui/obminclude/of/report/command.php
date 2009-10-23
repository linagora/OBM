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

require_once('obminclude/of/report/reportFactory.php');
require_once('obminclude/of/report/sender.php');
require_once('obminclude/of/report/sender/mailSender.php');
/**
 * Abstract class used to describe program actions. Must be inherited for
 * each report to generate.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
abstract class Command {
  protected $name = 'Report';
  protected $sender;
  protected $filters;
  protected $kind;
  protected $formater;
  const filter = "../../conf/report/default/filter/";
  const sender = "../../conf/report/default/sender/";

  /**
   * Constructor 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->filters = array();
    $this->kind = 'user';
  }

  /**
   * Called by the client. launch setup(), then execute() and then tearDown()
   *
   * @access public
   * @return void
   */
  public function doIt() {
    
    $this->setup();
    $this->execute();
    if (!is_array($this->filters))
      $this->filters = array($this->filters);
    if(is_dir(self::filter)) {
      $d = dir(self::filter);
      while(false != ($file = $d->read())) {
        if(is_file(self::filter.$file)) {
          require_once self::filter.$file;
          $klass = Command::getClass($file);
          $this->filters[] = new $klass;
        }
      }
      $d->close();
    }
    if (isset($this->formater) && isset($this->kind))
      $this->tearDown();
  }

  /**
   * Define default values (like sender to use).
   *
   * @access protected
   * @return void
   */
  protected function setup() {
  }

  /**
   * Main function of the programm.
   *
   * @access protected
   * @return void
   */
  abstract protected function execute();

  /**
   * send the report (call this->sender->send()).
   *
   * @access private
   * @return void
   */
  private function tearDown() {
    $report = ReportFactory::getReport($this->filters, $this->kind);
    
    if(isset($this->sender)) {
      $next = $this->sender;
    }
    
    if (is_dir(self::sender)) {
      $d = dir(self::sender);
      while (false != ($file = $d->read())) {
        if(is_file(self::sender.$file)) {
          require_once self::sender.$file;
          $klass = Command::getClass($file);
          $this->sender = new $klass;
          $this->sender->setNext($next);
          $next = $this->sender;
        }
      }
    }
    
    if (isset($this->sender))
      $this->sender->send($report->format($this->formater), $this->name);
  }

  /**
   * Get a class name from file.
   * TODO : could be getFileClassName
   * 
   * @param mixed $fileName 
   * @access public
   * @return void
   */
  function getClass($fileName) {
    preg_match("/^(.*)\.php$/",$fileName,$className);
    return $className[1];
  }

  /**
   * getName 
   * 
   * @access public
   * @return void
   */
  function getName() {
    return $this->name;
  }
}

