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
 * Abstract class inherited by all services form class
 * uses the OBM_Services class to build the form
 **/
abstract class OBM_Services_Form extends Stato_Webflow_Forms_Form {
  protected $descriptor;

  /**
   * standard constructor
   * @param  string $entity   the entity type (may be 'Host')
   * @param  string $args     optionnal arguments
   * @access public
   **/
  public function __construct($entity, $args = null) {
    $this->descriptor = OBM_Services::getInstance();
    if (is_array($args)) {
      $data  = is_array($args['data'])  ? $args['data']  : null;
      $files = is_array($args['files']) ? $args['files'] : null;
      parent::__construct($data, $files);
    }
  }

  /**
   * return the html needed to display the form
   * @access public
   * @return string
   **/
  public function render() {
    $html = array();
    $hiddenFields = array();
    foreach ($this->fields as $name => $field) {
      $bf = new OBM_Services_BoundField($this, $field, $name);
      if ($bf->isHidden) {
        $hiddenFields[] = $bf->render();
      } else {
        // FIXME: do something for the error message... like this : $err['msg'] = $bf->error; ?
        $th_class = ($bf->error) ? 'error' : '';
        $html[] = "<tr><th class=\"$th_class\">{$bf->labelTag}</th><td>{$bf->render()}</td></tr>";
      }
    }
    if (!empty($hiddenFields)) $html[] = implode("\n", $hiddenFields);
    return implode("\n", $html);
  }

}

