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



require_once('obminclude/of/of_mailer.php');

/**
 * Sender used to send report by mail.
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
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
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
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
