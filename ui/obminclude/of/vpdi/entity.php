<?php

class Vpdi_Entity implements ArrayAccess {
  
  protected $fields;
  
  protected $profile;
  
  /**
   * Constructor
   * 
   * @param array $fields an array of Vpdi_Field objects
   * @access public
   * @return void
   */
  public function __construct($fields = array()) {
    $this->fields = $fields;
  }
  
  public function __toString() {
    return "BEGIN:{$this->profile}\n"
           .Vpdi::encodeFields($this->fields)
           ."\nEND:{$this->profile}";
  }
  
  public function offsetExists($offset)
  {
    return (isset($this->fields[$offset]));
  }

  public function offsetGet($offset)
  {
    return @$this->fields[$offset];
  }

  public function offsetSet($offset, $field)
  {
    if (empty($offset)) {
      $this->fields[] = $field;
    } else {
      $this->fields[$offset] = $field;
    }
  }

  public function offsetUnset($offset)
  {
    $this->fields[$offset] = null;
  }
  
  /**
   * Adds a field to the entity
   * 
   * @param Vpdi_Field $field
   * @access public
   * @return void
   */
  public function addField($field) {
    $this->fields[] = $field;
  }
  
  /**
   * Sets the profile of the entity
   * 
   * @param string $profile
   * @access public
   * @return void
   */
  public function setProfile($profile) {
    $this->profile = strtoupper($profile);
  }
  
  /**
   * Returns the profile of the entity
   * 
   * @access public
   * @return void
   */
  public function profile() {
    return $this->profile;
  }
  
  /**
   * Returns an array of all the non empty fields named $name
   * 
   * @param string $name name of the fields
   * @access public
   * @return mixed
   */
  public function getFieldsByName($name) {
    $fields = array();
    foreach ($this->fields as $field) {
      if ($field->nameEquals($name) && $field->value() != '') {
        $fields[] = $field;
      }
    }
    return $fields;
  }
  
  /**
   * Returns the raw value (i.e. encoded) of the first non empty field named $name
   * 
   * @param string $name name of the field
   * @access public
   * @return mixed
   */
  public function getRawValue($name) {
    foreach ($this->getFieldsByName($name) as $field) {
      $value = $field->rawValue();
      if ($value != '') {
        return $value;
      }
    }
    return null;
  }
  
  /**
   * Returns the value of the first non empty field named $name
   * 
   * @param string $name name of the field
   * @access public
   * @return mixed
   */
  public function getValue($name) {
    foreach ($this->getFieldsByName($name) as $field) {
      $value = $field->value();
      if ($value != '') {
        return $value;
      }
    }
    return null;
  }
}
