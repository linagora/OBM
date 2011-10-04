<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
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
 * Form class to build form and validate service attributes for hosts
 **/
class OBM_Services_HostForm extends OBM_Services_Form {

  /**
   * standard constructor
   * @param  string $entity   the entity type (may be 'Host')
   * @param  string $args     optionnal arguments
   * @access public
   **/
  public function __construct($entity, $args = null) {
    parent::__construct($entity, $args);
    foreach ($this->descriptor->servers as $name => $server) {
      $this->addField($name,'Boolean',array('label' => __($server->label))); //FIXME: label translation
    }
  }

}

