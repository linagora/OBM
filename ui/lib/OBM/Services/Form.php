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

