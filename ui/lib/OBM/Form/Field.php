<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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



class OBM_Form_Field extends Stato_Webflow_Forms_Form {

  protected $entity;
  protected $fields;
  protected $prefix;

  public function __construct($entity, $prefix='custom') {
    $this->entity = $entity;
    $this->prefix = empty($prefix) ? '' : $prefix.'_';
    $this->fields = $GLOBALS['cgp_user'][$entity]['field'];
  }

  public function hasCustomField() {
    return is_array($this->fields);
  }

  public function buildFieldName($field) {
    return "{$this->prefix}{$field}";
  }

  public function buildEntityForm($class, $entity_id=null, $params = array()) {
    if (is_array($this->fields)) {
      if ($entity_id != null) $values = self::getValue($entity_id);
      foreach($this->fields as $field => $options) {
        $label = $GLOBALS["l_$field"];
        $value = "";
        if ($GLOBALS['action'] == 'detailupdate') { 
          $value = $values[$field];
        }
        $fieldname = $this->buildFieldName($field);
        if(isset($params[$fieldname])) $value = $params[$fieldname] ;
        $input = "build".ucfirst($options['type'])."Form";
        if (method_exists($this, $input)) {
          $input = $this->$input($field, $value);
        } else {
          $input = $this->buildTextForm($field, $value);
        }
        $block .= "<tr>
         <th class='$class[$field]'><label for='tf_$fieldname'>$label</label></th>
         <td>$input</td>
        </tr>"; 
      }
    }
    return $block;
  }

  private function buildTextForm($field, $value) {
    $fieldname = $this->buildFieldName($field);
    $f = new Stato_Webflow_Forms_CharField();
    return $f->render("tf_$fieldname", $value, array('id' => "tf_custom_$field"));
  }


  private function buildTextareaForm($field, $value) {
    $fieldname = $this->buildFieldName($field);
    $f = new Stato_Webflow_Forms_TextField();
    return $f->render("tf_$fieldname", $value, array('id' => "tf_custom_$field"));
  }


  private function buildBooleanForm($field, $value) {
    $fieldname = $this->buildFieldName($field);
    $f = new Stato_Webflow_Forms_BooleanField();
    return $f->render("tf_$fieldname", $value, array('id' => "tf_custom_$field"));
  }

  public function buildSearchForm($params = array()) {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $fieldname = $this->buildFieldName($field);
        $label = $GLOBALS["l_$field"];
        $value = $params[$fieldname];
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


  public function checkMandatory($params = array()) {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $fieldname = $this->buildFieldName($field);
        $value = $params[$fieldname];
        if ($options['mandatory'] && empty($value)) {
          $GLOBALS['err']['field'] = $field;
          $GLOBALS['err']['msg'] = $GLOBALS["l_$field"];
          return false;
        }
      }
    }
    return true;
  }


  public function insert($entity_id, $params = array()) {
    if (is_array($this->fields)) {
      $this->delete($entity_id);
      $id = of_entity_get($this->entity, $entity_id);
      $db = new DB_OBM();
      $query = "DELETE FROM field WHERE entity_id = '$id'";
      $db->query($query);
      foreach($this->fields as $field => $options) {
        $fieldname = $this->buildFieldName($field);
        $value = $params[$fieldname];
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


  public function buildQuery($params = array(), $obm_q = null) {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $fieldname = $this->buildFieldName($field);
        $value = $obm_q ? $obm_q->escape($params[$fieldname]) : $params[$fieldname];
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


  public function buildSearchUrl($params = array()) {
    if (is_array($this->fields)) {
      foreach($this->fields as $field => $options) {
        $fieldname = $this->buildFieldName($field);
        $value = $params[$fieldname];
        if (!empty($value)) { 
          $block .= "&amp;tf_$fieldname=$value";
        }
      }
    }
    return $block;
  }

}


?>
