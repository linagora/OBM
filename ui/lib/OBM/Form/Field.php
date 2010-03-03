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

class OBM_Form_Field extends Stato_Webflow_Forms_Form {

  protected $entity;
  protected $fields;

  public function __construct($entity) {
    $this->entity = $entity;
    $this->fields = $GLOBALS['cgp_user'][$entity]['field'];
  }

  public function hasCustomField() {
    return is_array($this->fields);
  }


  public function buildEntityForm($class, $entity_id=null) {
    if (is_array($this->fields)) {
      if ($entity_id != null) $values = self::getValue($entity_id);
      foreach($this->fields as $field => $options) {
        $label = $GLOBALS["l_$field"];
        $value = "";
        if ($GLOBALS['action'] == 'detailupdate') { 
          $value = $values[$field];
        }
        if(isset($GLOBALS['params']["custom_$field"])) $value = $GLOBALS['params']["custom_$field"] ;
        $input = "build".ucfirst($options['type'])."Form";
        if (method_exists($this, $input)) {
          $input = $this->$input($field, $value);
        } else {
          $input = $this->buildTextForm($field, $value);
        }
        $block .= "<tr>
         <th class='$class[$field]'><label for='tf_custom_$field'>$label</label></th>
         <td>$input</td>
        </tr>"; 
      }
    }
    return $block;
  }

  private function buildTextForm($field, $value) {
    $f = new Stato_Webflow_Forms_CharField();
    return $f->render("tf_custom_$field", $value);
  }


  private function buildTextareaForm($field, $value) {
    $f = new Stato_Webflow_Forms_TextField();
    return $f->render("tf_custom_$field", $value);
  }


  private function buildBooleanForm($field, $value) {
    $f = new Stato_Webflow_Forms_BooleanField();
    return $f->render("tf_custom_$field", $value);
  }

  public function buildSearchForm() {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $label = $GLOBALS["l_$field"];
        $value = $GLOBALS['params']["custom_$field"];
        if ($options['type'] == "boolean") {
          $input = $this->buildBooleanForm($field, $value);
        } else {
          $input = $this->buildTextForm($field, $value);
        }
        $block .= "<label>$label<br/>
         $input</label>"; 
      }
    }
    return $block;
  }


  public function buildConsultDetail($entity_id) {
    if (is_array($this->fields)) {
      $values = self::getValue($entity_id);
      foreach($this->fields as $field => $options) {
        $label = $GLOBALS["l_$field"];
        $value = nl2br($values[$field]);
        $block .="<tr>
         <th>$label</th>
         <td>$value</td>
       </tr>";
      }
    }
    return $block;
  }


  public function checkMandatory() {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $value = $GLOBALS['params']["custom_$field"];
        if ($options['mandatory'] && empty($value)) {
          $GLOBALS['err']['field'] = $field;
          $GLOBALS['err']['msg'] = $GLOBALS["l_$field"];
          return false;
        }
      }
    }
    return true;
  }


  public function insert($entity_id) {
    if (is_array($this->fields)) {
      $this->delete($entity_id);
      $id = of_entity_get($this->entity, $entity_id);
      $db = new DB_OBM();
      $query = "DELETE FROM field WHERE entity_id = '$id'";
      $db->query($query);
      foreach($this->fields as $field => $options) {
        $value = $GLOBALS['params']["custom_$field"];
        $query = "INSERT INTO field (entity_id, field, value) VALUES ('$id', '$field', '$value')";
        $db->query($query);
      }
    }
  }

  public function delete($entity_id) {
    if ($entity_id != null) {
      $id = of_entity_get($this->entity, $entity_id);
      $db = new DB_OBM();
      $query = "DELETE FROM field WHERE entity_id = '$id'";
      $db->query($query);
    }
  }


  public function getValue($entity_id) {
    $id = of_entity_get($this->entity, $entity_id);
    $db = new DB_OBM();
    $query = "SELECT * FROM field WHERE entity_id = '$id'";
    $db->query($query);
    $ret = array();
    while($db->next_record()) {
      $f = $db->f('field');
      if ($this->fields[$f]['type'] == "boolean") {
        $ret[$f] = $db->f('value') == 1 ? $GLOBALS['l_yes']:$GLOBALS['l_no'];
      } else {
        $ret[$f] = $db->f('value');
      }
    }
    return $ret;
  }


  public function buildQuery() {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $value = $GLOBALS['params']["custom_$field"];
        if (!empty($value)) { 
          $join .= " LEFT JOIN field custom$field ON custom$field.entity_id=userentity_entity_id";
          $where .= " AND (custom$field.value #LIKE '$value%' AND custom$field.field='$field')";
        }
      }
    }
    $query['where'] = $where;
    $query['join'] = $join;
    return $query;
  }


  public function buildSearchUrl() {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $value = $GLOBALS['params']["custom_$field"];
        if (!empty($value)) { 
          $block .= "&amp;tf_custom_$field=$value";
        }
      }
    }
    return $block;
  }

}


?>
