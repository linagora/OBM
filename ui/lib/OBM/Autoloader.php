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
 * OBM_Autoloader 
 * 
 * @package OBM_Autoloader 
 * @version $Id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
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
