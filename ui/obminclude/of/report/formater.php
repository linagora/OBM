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
 * Define how an element will be outputed 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
interface IFormater {

  /**
   * Format the current object 
   * 
   * @param mixed $object 
   * @access public
   * @return void
   */
  public function format($record);
  /**
   * get output header 
   * 
   * @access public
   * @return void
   */
  public function getHeader();
  /**
   * get output footer
   * 
   * @access public
   * @return void
   */
  public function getFooter();

}

/**
 * Define a generic format, this formater will display inline
 * all the fields of the object which have been added
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */
class GenericFormater implements IFormater{
  private $_fields;
  
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
    $line = '';
    foreach($this->_fields as $field) {
       $line .= self::escapeField($object->$field).";";
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
        $head .= self::escapeField(${"l_".$field}).";";
      else
        $head .= self::escapeField($field).";";
    }
    $head .= "\n";
    return $head;
  }

  /**
   * @see IFormater::getFooter
   */
  public function getFooter() {
    return '';
  }

  /**
   * Escape a field
   * 
   * @param mixed $field 
   * @static
   * @access private
   * @return void
   */
  protected static function escapeField($field) {
    return '"'.addcslashes($field,";\\\"").'"';
  }
}
