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



require_once(dirname(__FILE__).'/../lib/Stato/i18n/i18n.php');
require_once('of_helpers.php');

class OBM_Template {
  
  private $template;
  
  private $locals;

  private $paths;

  public function __construct($name, $module=null, $mode='html', $locals = array()) {
    if(!$module && $GLOBALS['module']) 
      $module = $GLOBALS['module'];
    elseif(!$module)
      $module = '';
    $this->mode = $mode;
    $this->paths = array(
      dirname(__FILE__)."/../../conf/views/html/".$module,
      dirname(__FILE__).'/../../views/html/'.$module,
      dirname(__FILE__)."/../../conf/views/html/common",
      dirname(__FILE__).'/../../views/html/common'
    );    
    $this->module = $module;
    $this->template = $this->__include($name);
    $this->locals = $locals;
  }

  private function __include($name) {
    foreach ($this->paths as $path) {
      if (file_exists($path.'/'.$name.'.'.$this->mode.'.php') && is_readable($path)) {
        return $path.'/'.$name.'.'.$this->mode.'.php';
      }
    }
  }

  private function __template($template, $default=null) {
    if($template instanceof OBM_Template) {
      return $template->render($this->locals, false);
    } elseif(is_string($template)) {
      $template = new OBM_Template($template, $this->module, $this->mode, $this->locals);
      return $template->render($this->locals);
    } elseif($default) {
      $template = new OBM_Template($default, $this->module, $this->mode, $this->locals);
      return $template->render($this->locals);
    }
    return false;    
  }

  public function set($local, $value = null) {
    if(is_array($local)) {
      $this->locals = array_merge($this->locals, $local);
    } else {
      $this->locals[$local] = $value;
    }
  }

  public function render($locals = array(), $overwrite = true) {
    if($overwrite) {
      $this->locals = array_merge($locals, $this->locals);
    } else {
      $this->locals = array_merge($this->locals, $locals);
    }
    extract($this->locals);
    ob_start();
    include $this->template;
    return ob_get_clean();
  }


  public static function toJs($string) {
    return phpStringToJsString($string);
  }

  /**
   * __actionlink 
   * 
   * @param mixed $action 
   * @param array $params 
   * @param mixed $module 
   * @static
   * @access public
   * @return void
   */
  //TODO : Refactor : getActionLink ?
  public static function __actionlink($action, $params = array(), $module=null) {
    if(!$module) $module = $GLOBALS['module'];
    if(count($params) > 0) $query = '&amp;'.http_build_query($params, '&amp;');
    return $GLOBALS['path'].'/'.$module.'/'.$module.'_index.php?action='.$action.$query;
  }

  public static function __getaddress($address) {
    $formatedAddress = array();
    if(!empty($address['street'])) $formatedAddress[] = $address['street'];
    if(!empty($address['zipcode']) || !empty($address['town'])) $formatedAddress[] = $address['zipcode'].' '.$address['town'];
    if(!empty($address['expresspostal']) || !empty($address['state'])) $formatedAddress[] = $address['expresspostal'].' '.$address['state'];
    if(!empty($address['address_country'])) $formatedAddress[] = get_localized_country($address['address_country']);
    return implode('<br />', $formatedAddress);
  }

  public static function __setaddress($value = null) {
    static $addressIndex = 0;
    if(!$value) $value['label'] = 'WORK';
    else $value['label'] = OBM_Contact::labelToString($value['label'], null, false, '_');
    $return = '
      <table class="coordinate addresses" id="addresses['.$addressIndex.']">
      <tbody>
      <tr>
      <th rowspan="3"><select  name="addresses['.$addressIndex.'][label]">';
    foreach($GLOBALS['l_address_labels'] as $label => $locale) {
      if($value['label'] == $label) $return .= '<option selected="selected" value="'.$label.'">'.$locale.'</option>';
      else $return .= '<option value="'.$label.'">'.$locale.'</option>';
    }

    $country = (!empty($value['address_country'])) ? $value['address_country'] : $value['country_iso3166'];
    $countries = get_localized_countries_array();
    $sel_countries = self::__setlist('addresses['.$addressIndex.'][country_iso3166]', $countries, __('Country'), $country, true, '');

    
    $return .= '
        </select></th>
        <th colspan="2"><textarea rows="3" name="addresses['.$addressIndex.'][street]" alt="'.__('Street').'" title="'.__('Street').'">'.$value['street'].'</textarea></th>
      </tr><tr>
        <th><input type="text" name="addresses['.$addressIndex.'][zipcode]" alt="'.__('Zip code').'" title="'.__('Zip code').'" value="'.$value['zipcode'].'"/></th>
        <th><input type="text" name="addresses['.$addressIndex.'][town]" alt="'.__('Town').'" title="'.__('Town').'" value="'.$value['town'].'"/></th>
      </tr><tr>
        <th><input type="text" name="addresses['.$addressIndex.'][expresspostal]" alt="'.__('Express postal').'" title="'.__('Express postal').'" value="'.$value['expresspostal'].'"/></th>
        <th>'.$sel_countries.'</th>
      </tr>
      </tbody>
      </table>
      ';
    $addressIndex++;
    return $return;
  }

  public static function __getboolean($value, $true='Yes', $false='No') {
    return ($value)?__($true):__($false);
  }

  public static function __setboolean($name, $value, $label, $idSuffix = 'Field') {
    return "<input id='${name}$idSuffix' type='checkbox' name='$name' value='1' ".(($value)? "checked='checked'": '')."' title='".__($label)."'/>";
  }

  public static function __setcategory($name, $datas, $label, $selected=null) {
    if($selected !== null) {
      foreach($selected as $id => $label) {
        $data .= self::__setlist($name, $datas, 'TOTO', $id, true);
      }
    }
    $data .= self::__setlist($name, $datas, 'TOTO', null, true);
    return $data;
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

  //TODO : Add getemail function.
  public static function __setemail($value=null) {
    static $emailIndex = 0;
    if(!$value) $value['label'] = 'INTERNET';
    else $value['label'] = OBM_Contact::labelToString($value['label'], null, false, '_');
    $return = '
      <table class="coordinate emails" id="emails['.$emailIndex.']">
      <tbody>
      <tr>
        <th><select  name="emails['.$emailIndex.'][label]">';
    foreach($GLOBALS['l_email_labels'] as $label => $locale) {
      if($value['label'] == $label) $return .= '<option selected="selected" value="'.$label.'">'.$locale.'</option>';
      else $return .= '<option value="'.$label.'">'.$locale.'</option>';
    }
    $return .= '
        </select></th>
        <th><input type="text" name="emails['.$emailIndex.'][address]" alt="'.__('Email').'" title="'.__('Email').'" value="'.htmlentities($value['address']).'"/></th>
      </tr>
      </tbody>
      </table>';    
    $emailIndex++;
    return $return;
  }

  public static function __getentitylink($value, $id, $module) {
    if($GLOBALS['perm']->check_module_rights($module)) {
      return '<a href="'.$GLOBALS['path'].'/'.$module.'/'.$module.'_index.php?action=detailconsult&amp;'.$module.'_id='.$id.'">'.$value.'</a>';
    } else {
      return $value;
    }
  }

  public static function __setentitylink($name, $value, $id, $module, $label, $idSuffix = 'Field') {
    if($GLOBALS['perm']->check_module_rights('company')) {
      return  "
        <input type='text' name='${name}_text' value='$value' id='${name}Field' autocomplete='off'/>
        <input type='hidden' name='${name}_id' value='$id' id='${name}_idField' />
        <script type='text/javascript'>
          new obm.AutoComplete.Search('$GLOBALS[path]/$module/${module}_index.php?action=ext_search', '${name}_idField', '${name}Field', {mode: 'mono', locked: true, resetable: true});
        </script>
      ";
    } else {
      return "<input type='text' name='$name' id='".$name."Field' value='$value' title='".__($label)."' />";
    }
  }

  public static function __icon($name) {
    if($GLOBALS['icons'][$name]) return $GLOBALS['icons'][$name];
    if($GLOBALS['ico_'.$name]) return $GLOBALS['ico_'.$name];
  }

  //TODO : Add getim function.
  public static function __setim($value=null) {
    static $imIndex = 0;
    if(!$value) $value['protocol'] = 'XMPP';
    else $value['protocol'] = OBM_Contact::labelToString($value['protocol'], null, false, '_');
    $return = '
      <table class="coordinate ims" id="ims['.$imIndex.']">
      <tbody>
      <tr>
        <th><select  name="ims['.$imIndex.'][protocol]">';
    foreach($GLOBALS['l_im_labels'] as $protocol => $locale) {
      if($value['protocol'] == $protocol) $return .= '<option selected="selected" value="'.$protocol.'">'.$locale.'</option>';
      else $return .= '<option value="'.$protocol.'">'.$locale.'</option>';
    }
    $return .= '
        </select></th>
        <th><input type="text" name="ims['.$imIndex.'][address]" alt="'.__('Address').'" title="'.__('Address').'" value="'.$value['address'].'"/></th>
      </tr>
      </tbody>
      </table>';    
    $imIndex++;
    return $return;
  }

  public static function __getlink($uri, $label=null) {
    if(!$label) $label = $uri;
    $uri = ((strpos($uri,'://') === false)?'http://'.$uri:$uri);
    return '<a href="'.$uri.'" target="__blank">'.$label.'</a>';
  }

  public static function __setlist($name, $datas, $label, $selected=null, $none=false, $idSuffix = 'Field') {
    $list = '';
    if($none) $list = '<option value="">---</option>';
    if(is_array($datas))
      foreach($datas as $id => $data) {
        if($id == $selected) $list .= '<option value="'.$id.'" selected="selected">'.$data.'</option>';
        else $list .= '<option value="'.$id.'">'.$data.'</option>';
      }

    return '<select name="'.$name.'" title="'.__($label).'" id="'.$name.$idSuffix.'">'.$list.'</select>';
  } 

  //TODO : Refactor : getmail ? getemail?
  public static function __getmail($uri, $label=null) {
    if(!$label) $label = $uri;
    return '<a href="mailto:'.$uri.'">'.$label.'</a>';
  }

  //TODO : Add getphone function.
  public static function __setphone($value=null) {
    static $phoneIndex = 0;
    if(!$value) $value['label'] = 'WORK_VOICE';
    else $value['label'] = OBM_Contact::labelToString($value['label'], null, false, '_');
    $return = '
      <table class="coordinate phones" id="phones['.$phoneIndex.']">
      <tbody>
      <tr>
        <th><select  name="phones['.$phoneIndex.'][label]">';
    foreach($GLOBALS['l_phone_labels'] as $label => $locale) {
      if($value['label'] == $label) $return .= '<option selected="selected" value="'.$label.'">'.$locale.'</option>';
      else $return .= '<option value="'.$label.'">'.$locale.'</option>';
    }
    $return .= '
        </select></th>
        <th><input type="text" name="phones['.$phoneIndex.'][number]" alt="'.__('Phone').'" title="'.__('Phone').'" value="'.$value['number'].'"/></th>
      </tr>
      </tbody>
      </table>';    
    $phoneIndex++;
    return $return;
  }

  //TODO : Add setphoto function.
  public static function __getphoto($id) {
    return '/images/themes/default/images/ico_nophoto.png';
  }

  //TODO : Add getwebsite function.
  public static function __setwebsite($value=null) {
    static $websiteIndex = 0;
    if(!$value) $value['label'] = 'URL';
    else $value['label'] = OBM_Contact::labelToString($value['label'], null, false, '_');
    $return = '
      <table class="coordinate websites" id="websites['.$websiteIndex.']">
      <tbody>
      <tr>
        <th><select  name="websites['.$websiteIndex.'][label]">';
    foreach($GLOBALS['l_website_labels'] as $label => $locale) {
      if($value['label'] == $label) $return .= '<option selected="selected" value="'.$label.'">'.$locale.'</option>';
      else $return .= '<option value="'.$label.'">'.$locale.'</option>';
    }
    $return .= '
        </select></th>
        <th><input type="text" name="websites['.$websiteIndex.'][url]" alt="'.__('Website').'" title="'.__('Website').'" value="'.$value['url'].'"/></th>
      </tr>
      </tbody>
      </table>';    
    $websiteIndex++;
    return $return;
  }


  public static function __setAddressbookSearchField($addessbooks) {
    $select = "<select id='addressbookSearch' name='addressbookId'>
      <option value=''>---</option>";
    foreach($addessbooks as $ad) {
      $select .= "<option value='$ad->id'>$ad->displayname</option>";
    }
    $select .= "</select>";
    return $select;
  }
}
