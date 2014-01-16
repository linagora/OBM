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
 * Chain-of-responsibility pattern. Define how to send the report.
 * Note that in a 'pure' implementation of the chain of responsibility
 * pattern, a sender would not pass responsibility further down the chain
 * after handling a message.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 */
abstract class Sender {
  protected $context = 'abstract';
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
      if($GLOBALS['context'] == $this->context) return true;
      switch ($this->context) {
       case 'web' :
         return !(defined('STDIN') && defined('STDOUT') && defined('STDERR'));
       case 'console' :
         return (defined('STDIN') && defined('STDOUT') && defined('STDERR'));
       case 'verbose' :
         return $GLOBALS['params']['verbose'];
       case 'test' :
         return class_exists('PHPUnit_Framework_TestCase');
      }
  }

}
