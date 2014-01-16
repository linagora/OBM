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



require(dirname(__FILE__).'/../lib/Stato/mailer/mailer.php');

/**
 * OBM Mailer Class
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 */
class OBM_Mailer extends SMailer {
  protected $locale;
  
  protected $host;
  
  protected $userId;
  
  protected $module;
  
  public function __construct() {
    parent::__construct();
    $this->locale = $_SESSION['set_lang'];
    $this->host = $GLOBALS['cgp_host'];
    $this->userId = $GLOBALS['obm']['uid'];
    // to move somewhere else...
    mb_internal_encoding("UTF-8");
    SI18n::set_locale($this->locale);
    self::set_template_root(dirname(__FILE__).'/../../views/mail');
    $this->userInfo = get_user_info();
  }
  
  /**
   * If the mail server is not properly configured and mails cannot be sent,
   * Stato will throw an exception, but we prefer to fail silently
   */
  public function send($method_name, $args) {
    try {
      return parent::send($method_name, $args);
    } catch (Exception $e) {
      return false;
    }
  }

  public function prepare($method_name, $args) {
    $mail = parent::prepare($method_name, $args);
    $mail->set_return_path($this->return_path);
    return $mail;
  }
  
  protected function get_template_path($template_name) {
    $possible_paths = array(
      dirname(__FILE__)."/../../conf/views/mail/$module/{$this->locale}/$template_name.php",
      self::$template_root."/{$this->module}/{$this->locale}/$template_name.php",
      dirname(__FILE__)."/../../conf/views/mail/$module/fr/$template_name.php",
      self::$template_root."/{$this->module}/fr/$template_name.php"
    );
    foreach ($possible_paths as $path) {
      if (file_exists($path) && is_readable($path)) {
        return $path;
      }
    }
    return false;
  }
  
  /**
   * Get sender email address
   */
  protected function getSender($userId = null) {
    global $obm, $cdg_sql;
    if ( !$userId ) {
      $userId = $obm['uid'];
    }
    $uid = sql_parse_id($userId, true);
    $query = "SELECT 
    userobm_email, userobm_lastname, userobm_firstname, userobm_commonname, domain_name
    FROM UserObm
    INNER JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_id $uid ";

    display_debug_msg($query, $cdg_sql, 'run_query_get_sender()');
    $db = new DB_OBM;
    $db->query($query);
    $db->next_record();
    
    $email = $this->getEntityEmail($db->f('userobm_email'), $db->f('domain_name'));
    if(!$email) $email = $this->getEntityEmail('noreply');
    $displayname = $db->f('userobm_commonname');
    if (!$displayname) $displayname = sprintf($GLOBALS['l_displayname_template'], $db->f('userobm_firstname'), $db->f('userobm_lastname'));
    return array($email, $displayname);
  }


  /**
   * Get event owner email address
   */
  protected function getOwner($event) {
    global $obm, $cdg_sql;

    $owner_label = $event->owner->label;
    $owner_id = sql_parse_id($event->owner->id, true);
    $query = "SELECT 
    userobm_email, domain_name
    FROM UserObm
    INNER JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_id $owner_id";

    display_debug_msg($query, $cdg_sql, 'run_query_get_sender()');
    $db = new DB_OBM;
    $db->query($query);
    $db->next_record();
    
    $email = $this->getEntityEmail($db->f('userobm_email'), $db->f('domain_name'));
    if(!$email) $email = $this->getEntityEmail('noreply');
    return $email;
  }

  
  /**
   * Get recipients email addresses
   * 
   * $recipients : Id Recipients array
   * $setting    : recipient setting to check (set_mail, set_mail_participant)
   * $force      : Mail should be forced (all user even with no set_mail)
   * Returns: DB with email recipients, or false if no valid recipients
   */
  protected function getRecipients($recipients, $setting='set_mail', $force=false) {
    global $cdg_sql;

    if (($setting == 'set_mail') || ($setting == 'set_mail_participation')) {
      $set_mail = $setting;
    } else {
      $set_mail = 'set_mail';
    }

    if (!$force) {
      $mail_filter = "
        AND (up1.userobmpref_value = 'yes' OR
        (up2.userobmpref_value = 'yes' AND up1.userobmpref_value IS NULL))";
      $join = "LEFT JOIN UserObmPref as up1 ON up1.userobmpref_user_id=userobm_id
        AND up1.userobmpref_option = '$set_mail'
        LEFT JOIN UserObmPref as up2 on up2.userobmpref_user_id IS NULL
        AND up2.userobmpref_option = '$set_mail'";
    }
    $coma = '';
    foreach($recipients as $recipient) {
      if ($recipient) {
        $user_list .= $coma.$recipient;
        $coma = ',';
      }
    }
    if ($user_list != '') {
      $user_in = "userobm_id IN ($user_list) AND";
    } else {
      return false;
    }

    $query = "SELECT 
      userobm_email, userobm_lastname, userobm_firstname, userobm_commonname, domain_name
      FROM UserObm 
      INNER JOIN Domain on userobm_domain_id = domain_id
      $join
      WHERE $user_in
      userobm_email != ''
      $mail_filter";
    display_debug_msg($query, $cdg_sql, 'run_query_get_recipients()');
    $db = new DB_OBM;
    $db->query($query);
    $recipients = array();
    
    while ($db->next_record()) {
      $email = $this->getEntityEmail($db->f('userobm_email'), $db->f('domain_name'));
      $displayname = $db->f('userobm_commonname');
      if (!$displayname) $displayname = sprintf($GLOBALS['l_displayname_template'], $db->f('userobm_firstname'), $db->f('userobm_lastname'));
      if (isset($email) && $email != "") {
        $recipients[] = array($email, $displayname);
      }
    }
    return $recipients;
  }
  
  /**
   * Format entities email. 
   * 
   * @param mixed $mail Brut email(s) (if not set use id and entity to retrieve
   * all informations including domain name)
   * @param mixed $domain current domain name (if not set use obm['domain_name'])
   * @param mixed $first_only return only the first entity email if true, all if
   * false
   * @param string $separator if null return an array, if not null return a 
   * string containing all email, separated by '$separator'
   * @param mixed $id Entity id, needed only if $mail is null 
   * @param string $entity Entity kind, needed only if $mail is null
   * @access public
   * @return array | string
   */
  protected function getEntityEmail($mail=null, $domain=null, $first_only=true, $separator=', ', $id=null, $entity='user') {
    global $obm;
    if ($mail === null) {
      if ($id === null) {
        $id = $obm['uid'];
      }
      $e = get_entity_info($id, $entity);
      $mail = $e['email'];
      $domain = $e['domain_name'];
    }

    if ($domain === null && $obm['domain_global'] === false) {
      $domain = $obm['domain_name'];
    }    
    $mail = explode("\r\n",$mail);
    if ($first_only && count($mail) > 1) {
      $mail = array(array_shift($mail));
    }
    $emails = array();
    foreach ($mail as $key => $email) {
      if (strpos($email,'@') === false && !empty($email)) {
        $emails[] = $email.'@'.$domain;
      } elseif (!empty($email)) {
        $emails[] = $email;
      }
    }
    if ($separator !== null) {
      return implode($separator,$emails);
    } else {
      return $emails;
    }
  }
}
