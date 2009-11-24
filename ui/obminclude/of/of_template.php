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
    if(!empty($address['country'])) $formatedAddress[] = $address['country'];
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
    $return .= '
        </select></th>
        <th colspan="2"><textarea rows="3" name="addresses['.$addressIndex.'][street]" alt="'.__('Street').'" title="'.__('Street').'">'.$value['street'].'</textarea></th>
      </tr><tr>
        <th><input type="text" name="addresses['.$addressIndex.'][zipcode]" alt="'.__('Zip code').'" title="'.__('Zip code').'" value="'.$value['zipcode'].'"/></th>
        <th><input type="text" name="addresses['.$addressIndex.'][town]" alt="'.__('Town').'" title="'.__('Town').'" value="'.$value['town'].'"/></th>
      </tr><tr>
        <th><input type="text" name="addresses['.$addressIndex.'][expresspostal]" alt="'.__('Express postal').'" title="'.__('Express postal').'" value="'.$value['expresspostal'].'"/></th>
        <th><select name="addresses['.$addressIndex.'][country_iso3166]" alt="'.__('Country').'" title="'.__('Country').'"><option value="none">---</option><option value="AL">Albanie</option><option value="DZ">Algérie</option><option value="DE">Allemagne</option><option value="AO">Angola</option><option value="SA">Arabie Saoudite</option><option value="AM">Arménie</option><option value="AU">Australie</option><option value="AZ">Azerbaidjan</option><option value="BS">Bahamas</option><option value="BD">Bangladesh</option><option value="BE">Belgique</option><option value="BJ">Benin</option><option value="BY">Bielorussie</option><option value="BO">Bolivie</option><option value="BR">Brésil</option><option value="BG">Bulgarie</option><option value="BF">Burkina Faso</option><option value="CM">Cameroun</option><option value="CA">Canada</option><option value="CN">Chine</option><option value="CY">Chypre</option><option value="CO">Colombie</option><option value="KP">Corée du Nord</option><option value="CR">Costa Rica</option><option value="HR">Croatie</option><option value="CU">Cuba</option><option value="DK">Danemark</option><option value="EG">Egypte</option><option value="AE">Emirats Arabes Unis</option><option value="EC">Equateur</option><option value="ES">Espagne</option><option value="EE">Estonie</option><option value="GA">Gabon</option><option value="GE">Georgie</option><option value="GH">Ghana</option><option value="GI">Gibraltar</option><option value="GR">Grèce</option><option value="GL">Groenland</option><option value="GT">Guatemala</option><option value="GN">Guinée</option><option value="HK">Hong Kong</option><option value="HU">Hongrie</option><option value="IN">Inde</option><option value="IR">Iran</option><option value="IE">Irlande</option><option value="IS">Islande</option><option value="IL">Israel</option><option value="IT">Italie</option><option value="JM">Jamaique</option><option value="JP">Japon</option><option value="JO">Jordanie</option><option value="KZ">Kazakhstan</option><option value="KE">Kenya</option><option value="KW">Koweit</option><option value="BB">La Barbade</option><option value="LV">Lettonie</option><option value="LB">Liban</option><option value="LY">Libye</option><option value="LI">Liechtenstein</option><option value="LU">Luxembourg</option><option value="MY">Malaisie</option><option value="MW">Malawi</option><option value="MT">Malte</option><option value="MA">Maroc</option><option value="MU">Mauritius</option><option value="MX">Mexique</option><option value="MD">Moldova</option><option value="MC">Monaco</option><option value="NP">Népal</option><option value="NI">Nicaragua</option><option value="NE">Nigeria</option><option value="NO">Norvège</option><option value="NZ">Nouvelle Zélande</option><option value="OM">Oman</option><option value="PK">Pakistan</option><option value="NL">Pays Bas</option><option value="PE">Pérou</option><option value="PH">Phillipines</option><option value="PL">Pologne</option><option value="PT">Portugal</option><option value="CZ">Rep.Tchèque</option><option value="GB">Royaume Uni</option><option value="LK">Sri Lanka</option><option value="CH">Suisse</option></select></th>
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
        <th><input type="text" name="emails['.$emailIndex.'][address]" alt="'.__('Email').'" title="'.__('Email').'" value="'.$value['address'].'"/></th>
      </tr>
      </tbody>
      </table>';    
    $emailIndex++;
    return $return;
  }

  public static function __getentitylink($value, $id, $module) {
    if($GLOBALS['perm']->check_module_rights($module)) {
      return '<a href='.$GLOBALS['path'].'/'.$module.'/'.$module.'_index.php?action=detailconsult&amp;'.$module.'_id='.$id.'>'.$value.'</a>';
    } else {
      return $value;
    }
  }

  public static function __setentitylink($name, $value, $id, $module, $label, $idSuffix = 'Field') {
    if($GLOBALS['perm']->check_module_rights('company')) {
      return  "
        <input type='text' name='$name' value='$value' id='${name}Field' autocomplete='off'/>
        <input type='hidden' name='${name}_id' value='id' id='${name}_idField' />
        <script type='text/javascript'>
          new obm.AutoComplete.Search('$path/$module/${module}_index.php?action=ext_search', '${name}_idField', '${name}Field', {mode: 'mono', locked: true, resetable: true});
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
    if(!$value) $value['label'] = 'XMPP';
    else $value['label'] = OBM_Contact::labelToString($value['label'], null, false, '_');
    $return = '
      <table class="coordinate ims" id="ims['.$imIndex.']">
      <tbody>
      <tr>
        <th><select  name="ims['.$imIndex.'][protocol]">';
    foreach($GLOBALS['l_im_labels'] as $label => $locale) {
      if($value['label'] == $label) $return .= '<option selected="selected" value="'.$label.'">'.$locale.'</option>';
      else $return .= '<option value="'.$label.'">'.$locale.'</option>';
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
    $uri = ((strpos($uri[0],'://') === false)?'http://'.$uri:$uri);
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
}
