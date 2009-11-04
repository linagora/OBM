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

require_once(dirname(__FILE__).'/../lib/Stato/i18n/i18n.php');

class OBM_Template {
  
  private $template;
  
  private $locals;

  private $paths;

  public function __construct($name, $module=null, $mode='html') {
    if(!$module && $GLOBALS['module']) 
      $module = $GLOBALS['module'];
    else
      $module = '';
    $this->mode = $mode;
    $this->paths = array(
      dirname(__FILE__)."/../../conf/views/html/".$module,
      dirname(__FILE__).'/../../views/html/'.$module,
      dirname(__FILE__)."/../../conf/views/html/common",
      dirname(__FILE__).'/../../views/html/common'
    );    
    $this->module = $module;
    $this->template = $this->__template($name);
    $this->locals = array();
  }

  private function __template($name) {
    foreach ($this->paths as $path) {
      if (file_exists($path.'/'.$name.'.'.$this->mode.'.php') && is_readable($path)) {
        return $path.'/'.$name.'.'.$this->mode.'.php';
      }
    }
    return false;    
  }

  public function set($local, $value = null) {
    if(is_array($local)) {
      $this->locals = array_merge($this->locals, $locals);
    } else {
      $this->locals[$local] = $value;
    }
  }

  public function render($locals = array()) {
    $this->locals = array_merge($this->locals, $locals);
    extract($this->locals);
    ob_start();
    include $this->template;
    return ob_get_clean();
  }


  public static function toJs($string) {
    return phpStringToJsString($string);
  }

  public static function __actionlink($action, $params = array(), $module=null) {
    if(!$module) $module = $GLOBALS['module'];
    if(count($params) > 0) $query = '&amp;'.http_build_query($params, '&amp;');
    return $GLOBALS['path'].'/'.$module.'/'.$module.'_index.php?action='.$action.$query;
  }

  public static function __icon($name) {
    if($GLOBALS['icons'][$name]) return $GLOBALS['icons'][$name];
    if($GLOBALS['ico_'.$name]) return $GLOBALS['ico_'.$name];
  }

  public static function __getmail($uri, $label=null) {
    if(!$label) $label = $uri;
    return '<a href="mailto:'.$uri.'">'.$label.'</a>';
  }

  public static function __getlink($uri, $label=null) {
    if(!$label) $label = $uri;
    $uri = ((strpos($uri[0],'://') === false)?'http://'.$uri:$uri);
    return '<a href="'.$uri.'" target="__blank">'.$label.'</a>';
  }

  public static function __getdate($date, $time=false) {
    if($time)
      return $date->getOutputDatetime();
    else
      return $date->getOutputDate();
  }

  public static function __setdate($name, $value, $label, $time=false, $idSuffix = 'Field') {
    if(is_object($value)) $value = $value->getInputDate();
    $field = "<span class='NW'>
      <input class='datePicker' autocomplete='off' type='text' title='".__($label)."' name='$name' value='$value' id='${name}$idSuffix' />
      <img src='$GLOBALS[ico_datepicker]' onclick=\"displayDatePicker($('${name}$idSuffix'))\" />
      </span>";
    if($time) {
    }
    return $field;
  }
  
  public static function __getphoto($id) {
    return '/images/themes/default/images/ico_nophoto.png';
  }

  public static function __getaddress($address) {
    $formatedAddress = array();
    if(!empty($address['street'])) $formatedAddress[] = $address['street'];
    if(!empty($address['zipcode']) || !empty($address['town'])) $formatedAddress[] = $address['zipcode'].' '.$address['town'];
    if(!empty($address['expresspostal']) || !empty($address['state'])) $formatedAddress[] = $address['expresspostal'].' '.$address['state'];
    if(!empty($address['country'])) $formatedAddress[] = $address['country'];
    return implode('<br />', $formatedAddress);
  }

  public static function __getboolean($value, $true='Yes', $false='No') {
    return ($value)?__($true):__($false);
  }

  public static function __setboolean($name, $value, $label, $idSuffix = 'Field') {
    return "<input id='${name}$idSuffix' type='checkbox' name='$name' value='1' ".(($value)? "checked='checked'": '')."' title='".__($label)."'/>";
  }

  public static function __setlist($name, $datas, $label, $selected=null, $none=false, $idSuffix = 'Field') {
    $list = '';
    if($none) $list = '<option value="">---</option>';
    if(is_array($datas))
        var_dump($id, $datas);
      foreach($datas as $id => $data) {
        if($id == $selected) $list .= '<option value="'.$id.'" selected="selected">'.$data.'</option>';
        else $list .= '<option value="'.$id.'">'.$data.'</option>';
      }

    return '<select name="'.$name.'" title="'.__($label).'" id="'.$name.$idSuffix.'">'.$list.'</select>';
  } 

  public static function __getentitylink($value, $id, $module) {
    if($GLOBALS['perm']->check_module_rights('contract')) {
      return '<a href='.$GLOBALS['path'].'/'.$module.'/'.$module.'_index.php?action=detailconsult&amp;'.$module.'_id='.$id.'>'.$value.'</a>';
    } else {
      return $value;
    }
  }

  public static function __setentitylink($name, $value, $id, $label, $idSuffix = 'Field') {

  }
}
