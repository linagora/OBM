<?php

class Vpdi_Field {
  
  private $group;
  
  private $name;
  
  private $params;
  
  private $value;
  
  public static function decode($line) {
    $parts = Vpdi::decodeLine($line);
    return new Vpdi_Field($parts['name'], $parts['value'], 
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
