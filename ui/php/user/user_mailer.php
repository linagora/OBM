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
?><?php

require_once('obminclude/of/of_mailer.php');

class UserMailer extends OBM_Mailer {
  protected $module = 'user';

  /**
   * Send a mail to confirm that a user have been validated
   * by an admin to the creator of this user. 
   * 
   * @param mixed $user 
   * @access protected
   * @return void
   */
  protected function validateConfirmation($id) {
    $obm_q = run_query_user_detail($id);
    $admin = get_user_info();
    $this->from = $this->getSender();
    $this->subject = __('%displayname% validation confirmation', array('%displayname%' => $obm_q->f('userobm_firstname').' '.$obm_q->f('userobm_lastname')));
    $this->recipients = $this->getRecipients(array($obm_q->f('userobm_usercreate')));    
    $this->body = array('user_label' => $obm_q->f('userobm_firstname').' '.$obm_q->f('userobm_lastname'),
                        'admin_label' => $admin['firstname'].' '.$admin['lastname']);
  }
}
