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

/**
 * Chain-of-responsibility pattern. Define how to send the report.
 * Note that in a 'pure' implementation of the chain of responsibility
 * pattern, a sender would not pass responsibility further down the chain
 * after handling a message.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
abstract class Sender {
  const context = 'abstract';
  private $next;

  /**
   * Set the next sender to call
   *
   * @param mixed $sender next sender to call
   * @access public
   * @return void
   */
  public function setNext($sender) {
    $this->next = $sender;
  }

  /**
   * Send the report message, and call the next sender to do the same
   *
   * @param mixed $report report message
   * @param mixed $name report command name
   * @access public
   * @return void
   */
  public function send($report, $name='') {
    if ($this->checkContext())
      $this->sendMessage($report, $name);
    if (isset($this->next))
      $this->next->send($report);
  }

  /**
   * Describe how the report is really sent (really execute the action)
   *
   * @param mixed $report report message
   * @param mixed $name report command name
   * @access protected
   * @return void
   */
  abstract protected function sendMessage($report, $name);

  /**
   * Used to determine current script execution context
   *
   * @access public
   * @return string
   */
  public static function currentContext() {
    /*if (isset($HTTP_SESSION_VARS))
      return 'web';*/
    //else
    if(isset($GLOBALS['context'])) {
      return $GLOBALS['context'];
    }
    if(!(defined('STDIN') && defined('STDOUT') && defined('STDERR'))) {
      return 'web';
    }
    if (class_exists('PHPUnit_Framework_TestCase'))
      return 'test';
    //else
    return 'console';
  }

  /**
   * Used to check the sender context corresponds to current script execution context
   *
   * @access private
   * @return bool
   */
   private function checkContext() {
     $classname = get_class($this);
     eval("\$class_context = $classname::context;");
     return (in_array(Sender::currentContext(),array($class_context,'test')));
  }

}
