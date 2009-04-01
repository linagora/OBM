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
require_once('obminclude/of/of_mailer.php');

/**
 * Sender used to send report by mail.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
class MailSender extends Sender {
  const context = 'console';
  private $_mailer;

  /**
   * Constructor 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->_mailer = new ReportMailer;
  }

  /**
   * Send the report by mail
   *
   * @param mixed $report report message
   * @param mixed $name report command name
   * @access protected
   * @return void
   */
  protected function sendMessage($report, $name) {
    $this->_mailer->sendReportMail($report, $name);
  }

  /**
   * Allow to add a mail address to mail recipients
   *
   * @param mixed $mail mail address
   * @access public
   * @return void
   */
  public function addRecipient($mail) {
    $this->_mailer->addRecipient($mail);
  }

}


/**
 * Utility class used by MailSender to send mails
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
class ReportMailer extends OBM_Mailer {
  protected $module = 'report';
  const mail_sender = 'root@localhost';

  /**
   * Allow to add a mail address to mail recipients
   *
   * @param mixed $mail mail address
   * @access public
   * @return void
   */
  public function addRecipient($mail) {
    $this->recipients[] = $mail;
  }

  /**
   * Create the report mail
   *
   * @param mixed $report report message
   * @param mixed $name report command name
   * @access public
   * @return void
   */
  public function reportMail($report, $name) {
    $this->from = ReportMailer::mail_sender;
    $this->subject = __('Exploitation report : %name%', array( '%name%' => $name));
    $this->body = array('report' => $report, 'name' => $name);
  }

}
