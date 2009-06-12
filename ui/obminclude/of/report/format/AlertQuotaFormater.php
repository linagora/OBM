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
 * AlertQuotaFormater formater for display warning quota mail 
 * 
 * @uses IFormater
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 LINAGORA GSO
 * @author Benoît Caudesaygues <benoit.caudesaygues@linagora.com> 
 * @license GPL 2.0
 */
class AlertQuotaFormater implements IFormater{
  private $_fields;
  private $limit = 90;
  
  /**
   * Constructor 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    $this->_fields = array();
  }

  /**
   * Add $field to the field to format 
   * 
   * @param mixed $field 
   * @access public
   * @return void
   */
  public function addField($field) {
    $this->_fields[] = $field;
  }

  /**
   *  
   * @see IFormater::format 
   **/
  public function format($object) {
    global $l_warn_quota;
    $quota = $object->mail_quota;

    $use = $object->mail_quota_use;
    $percent = $this->percentQuota($use,$quota);

    $line = '';
    foreach($this->_fields as $field) {
       $line .=self::escapeField($object->$field).";";
    }
    if($percent != '---') {
      $line .= $percent."%";
      if($percent > $this->limit) {
        $line .= ';'.self::escapeField($l_warn_quota);
      }
    } else {
      $line .= $percent;
    }
    $line .= ";\n";
    return $line;
  }

  /**
   * @see IFormater::getHeader
   */
  public function getHeader() {
    require "obminclude/lang/".$_SESSION['set_lang']."/report.inc";
    $head = '';
    foreach($this->_fields as $field) {
      if(isset(${"l_".$field}))
        $head .= self::escapeField(${"l_".$field}).";";
      else
        $head .= $field.";";
    }
    $head .= self::escapeField($l_used_percent).";";
    $head .= self::escapeField($l_warn).";";
    $head .= "\n";
    return $head;
  }

  /**
   * @see IFormater::getFooter
   */
  public function getFooter() {
    return '';
  }

  private function percentQuota($use,$quota) {
    if($quota != 0)
      $percent = round((($use * 100)/$quota),2);
    else
      $percent = '---';
    return $percent;
  }
  /**
   * Escape a field
   * 
   * @param mixed $field 
   * @static
   * @access private
   * @return void
   */
  private static function escapeField($field) {
    return '"'.addcslashes($field,";\\\"").'"';
  }
}
