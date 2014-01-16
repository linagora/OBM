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
 * Base class for RFC2425 entity properties
 * 
 * @package Vpdi
 * @version $Id:$
 * @author Raphaël Rougeron <raphael.rougeron@gmail.com> 
 */
class Vpdi_Property {
  
  private $group;
  
  private $name;
  
  private $params;
  
  private $value;
  
  public static function decode($line) {
    $parts = Vpdi::decodeLine($line);
    return new Vpdi_Property($parts['name'], $parts['value'], 
                             $parts['params'], $parts['group']);
  }
  
  public function __construct($name, $value, $params = array(), $group = null) {
    $this->group  = $group;
    $this->name   = $name;
    $this->params = $params;
    $this->value  = $value;
  }
  
  public function __toString() {
    $line = '';
    $upper_case = Vpdi::getConfig('always_encode_in_upper_case');
    if ($this->group !== null) {
      $line.= $this->group.'.';
    }
    $line.= ($upper_case) ? strtoupper($this->name) : $this->name;
    foreach ($this->params as $name => $values) {
      if (!is_array($values)) {
        $values = array($values);
      }
      if (count($values) === 0) {
        continue;
      }
      if (strtoupper($name) == 'TYPE' && Vpdi::getConfig('type_values_as_a_parameter_list') == true) {
        $list = array();
        foreach ($values as $v) {
          $list[] = 'TYPE='.$v;
        }
        $line.= ';'.implode(';', $list);
      } else {
        $line.= ';'.$name.'='.implode(',', $values);
      }
    }
    $line.= ':'.$this->value;
    
    return $line;
  }
  
  public function name() {
    return $this->name;
  }
  
  public function rawValue() {
    return $this->value;
  }
  
  public function value() {
    switch ($this->encoding()) {
      case 'B':
        return base64_decode($this->value);
      case 'BASE64':
        return base64_decode($this->value);
      case 'QUOTED-PRINTABLE':
        return quoted_printable_decode($this->value);
      default:
        return $this->value;
    }
  }
  
  public function encoding() {
    $e = $this->getParam('ENCODING');
    if (!$e) {
      return null;
    }
    if (is_array($e)) {
      throw new Vpdi_InvalidEncodingException('multi-valued ENCODING param :'.implode(',', $e));
    }
    return strtoupper($e);
  }
  
  public function nameEquals($name) {
    return strcasecmp($this->name, $name) == 0;
  }
  
  public function isCanceledException(){
	return $this->nameEquals('EXDATE');
  }
  
  public function isMovedException(){
	return $this->nameEquals('RECURRENCE-ID');
  }
  
  
  public function typeEquals($type) {
    $types = $this->getParam('TYPE');
    if (!$types) {
      return false;
    }
    if (!is_array($types)) {
      $types = array($types);
    }
    foreach ($types as $t) {
      if (strcasecmp($t, $type) == 0) {
        return true;
      }
    }
    return false;
  }
  
  public function getParam($param) {
    foreach ($this->params as $p => $v) {
      if (strcasecmp($p, $param) == 0) {
        return $v;
      }
    }
    return false;
  }
}
