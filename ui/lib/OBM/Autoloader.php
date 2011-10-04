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
 * OBM_Autoloader 
 * 
 * @package OBM_Autoloader 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
class OBM_Autoloader {

    /**
     * @var OBM_Autoloader Singleton instance
     */
    protected static $_instance;

    /**
     * @var array Concrete autoloader callback implementations
     */
    public $autoloaders = array();

    /**
     * Retrieve singleton instance
     *
     * @return OBM_Autoloader
     */
    public static function getInstance() {
      if (null === self::$_instance) {
          self::$_instance = new self();
      }
      return self::$_instance;
    }

    /**
     * Reset the singleton instance
     *
     * @return void
     */
    public static function resetInstance() {
        self::$_instance = null;
    }

    /**
     * Constructor
     *
     * Registers instance with spl_autoload stack
     *
     * @return void
     */
    protected function __construct() {
      spl_autoload_register(array(__CLASS__, 'autoload'));
    }

    /**
     * Autoload a class
     *
     * @param  string $class
     * @param mixed $class 
     * @static
     * @access public
     * @return bool
     */
    public static function autoload($class) {
      $self = self::getInstance();
      foreach ($self->getClassAutoloaders($class) as $autoloader) {
        if ($autoloader->autoload($class)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Get autoloaders to use when matching class
     *
     * Determines if the class matches a registered namespace, and, if so,
     * returns only the autoloaders for that namespace. Otherwise, it returns
     * all non-namespaced autoloaders.
     *
     * @param  string $class
     * @return array Array of autoloaders to use
     */
    public function getClassAutoloaders($class) {
      $autoloaders = array();
      $size = strlen($class);
      foreach($this->autoloaders as $namespace => $autoloader)  {
        if (strpos($class, $namespace) === 0) {
          $autoloaders[$size - strlen($namespace)] = $autoloader;
        } elseif ($namespace === '*') {
          $autoloaders[0] = $autoloader;
        }
      }
      return $autoloaders;
    }
}
