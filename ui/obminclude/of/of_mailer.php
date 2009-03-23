<?php

require(dirname(__FILE__).'/../lib/Stato/mailer/mailer.php');
require(dirname(__FILE__).'/../lib/Stato/i18n/i18n.php');

/**
 * OBM Mailer Class
 * 
 * @package 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 * @author Raphaël Rougeron <raphael.rougeron@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_Mailer extends Stato_Mailer
{
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
    Stato_I18n::setLocale($this->locale);
    Stato_I18n::addDataPath(dirname(__FILE__).'/../../locale');
    self::setTemplateRoot(dirname(__FILE__).'/../../views/mail');
    $this->userInfo = get_user_info();
  }
  
  protected function getTemplatePath($templateName) {
    $possiblePaths = array(
      self::$templateRoot."/{$this->module}/{$this->locale}/$templateName.php",
      self::$templateRoot."/{$this->module}/en/$templateName.php"
    );
    foreach ($possiblePaths as $path) {
      if (file_exists($path) && is_readable($path)) {
        return $path;
      }
    }
    return false;
  }
  
  /**
   * Get sender email address
   */
  protected function getSender() {
    global $obm, $cdg_sql;

    $uid = sql_parse_id($obm['uid'], true);
    $query = "SELECT 
    userobm_email, userobm_lastname, userobm_firstname, domain_name
    FROM UserObm
    INNER JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_id $uid ";

    display_debug_msg($query, $cdg_sql, 'run_query_get_sender()');
    $db = new DB_OBM;
    $db->query($query);
    $db->next_record();
    
    $email = $this->getEntityEmail($db->f('userobm_email'), $db->f('domain_name'));

    return array($email, $db->f('userobm_firstname').' '.$db->f('userobm_lastname'));
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

    if (($setting == 'set_mail') || ($setting == 'set_mail_participant')) {
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
      userobm_email, userobm_lastname, userobm_firstname, domain_name
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
      $recipients[] = array($email, $db->f('userobm_firstname').' '.$db->f('userobm_lastname'));
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
    if (is_null($mail)) {
      if (is_null($id)) {
        return false;
      }
      if (is_null($id)) {
        $id = $obm['uid'];
      }
      $e = get_entity_info($id, $entity);
      $mail = $e['email'];
      $domain = $e['domain_name'];
    }

    if (is_null($domain) && $obm['domain_global'] === false) {
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
    if (!is_null($separator)) {
      return implode($separator,$emails);
    } else {
      return $emails;
    }
  }
}