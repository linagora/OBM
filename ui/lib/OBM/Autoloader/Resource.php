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
 * Resource loader 
 * 
 * @package OBM_Autoloader 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */

class OBM_Autoloader_Resource {

  /**
   * Attempt to autoload a class
   *
   * @param  string $class
   * @return mixed False if not matched, otherwise result if include operation
   */
  public function autoload($class) {
    $classPath = $this->getClassPath($class);
    if ($classPath !== false) {
      return include $classPath;
    }
    return false;
  }  

  /**
   * Helper method to calculate the correct class path
   *
   * @param string $class
   * @return False if not matched other wise the correct path
   */
  public function getClassPath($class) {

    $classPath = str_replace('_', '/', $class) . '.php';
    if (is_readable($classPath)) {
      return $classPath;
    }
    if(is_readable('models/'.$classPath)) {
      return $classPath;
    }
    if(is_readable(dirname(__FILE__).'/../../../obminclude/lib/'.$classPath)) {
      return 'obminclude/lib/'.$classPath;
    }
    return false;
  }
}
