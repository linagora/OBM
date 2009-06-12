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

/**
 * ActivityFormater 
 * 
 * @uses GenericFormater
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class ActivityFormater extends GenericFormater {

  /**
   *  
   * @see IFormater::format 
   **/
  public function format($object) {
    $line = '';
      var_dump($object->id, $object->groupware_usage);
    if($object->groupware_usage) {
      $percent = (($object->groupware_usage/30) > 1)?"100%":round(($object->groupware_usage/30)*100).'%';
      $line .= self::escapeField($percent).';';
    } else {
      $line .= 'N/A;';
    }
    $line .= parent::format($object);
    return $line;
  }

  /**
   * @see IFormater::getHeader
   */
  public function getHeader() {
    require "obminclude/lang/".$_SESSION['set_lang']."/report.inc";
    $head = '';
    $field = 'groupware_usage';
    if(isset(${"l_".$field}))
      $head .= self::escapeField(${"l_".$field}).";";
    else
      $head .= self::escapeField($field).";";
    $head .= parent::getHeader(); 
    return $head;
  }  
}
