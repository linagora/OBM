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
    $percent = round($this->percentQuota($use,$quota),2);

    $line = '';
    foreach($this->_fields as $field) {
       $line .=$object->$field."\t";
    }
    $line .= $percent."%\t";
    if($percent > $this->limit) {
      $line .= $l_warn_quota."\t";
    } else {
      $line .= "\t";
    }
    $line .= "\n";
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
        $head .= ${"l_".$field}."\t";
      else
        $head .= $field."\t";
    }
    $head .= $l_used_percent."\t";
    $head .= $l_warn."\t";
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
    $percent = (($use * 100)/$quota);
    return $percent;
  }

}
