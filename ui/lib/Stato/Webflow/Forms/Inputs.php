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




abstract class Stato_Webflow_Forms_Input
{
    protected $attrs;
    protected $type = null;
    protected $isHidden = false;
    
    public function __construct(array $attrs = array())
    {
        $this->attrs = $attrs;
    }
    
    public function addAttrs(array $attrs)
    {
        $this->attrs = array_merge($this->attrs, $attrs);
    }
    
    public function isHidden()
    {
        return $this->isHidden;
    }
    
    public function render($name, $value = null, array $attrs = array())
    {
        $finalAttrs = array_merge(array('type' => $this->type, 'name' => $name), $this->attrs, $attrs);
        if ($value != '') $finalAttrs['value'] = $value;
        return '<input '.$this->flattenAttrs($finalAttrs).' />';
    }
    
    protected function flattenAttrs(array $attrs)
    {
        if (count($attrs) == 0) return;
        $set = array();
        foreach($attrs as $key => $value) {
            if ($value !== null && $value !== false) {
                if ($value === true) $set[] = $key.'="'.$key.'"';
                else $set[] = $key.'="'.htmlspecialchars($value, ENT_QUOTES, 'UTF-8').'"';
            }
        }
        return implode(" ", $set);
    }
    
    protected function htmlEscape($str)
    {
        return htmlspecialchars($str, ENT_QUOTES, 'UTF-8');
    }
}

class Stato_Webflow_Forms_TextInput extends Stato_Webflow_Forms_Input
{
    protected $type = 'text';
}

class Stato_Webflow_Forms_PasswordInput extends Stato_Webflow_Forms_Input
{
    protected $type = 'password';
}

class Stato_Webflow_Forms_HiddenInput extends Stato_Webflow_Forms_Input
{
    protected $type = 'hidden';
    protected $isHidden = true;
}

class Stato_Webflow_Forms_FileInput extends Stato_Webflow_Forms_Input
{
    protected $type = 'file';
    
    public function render($name, $value = null, array $attrs = array())
    {
        return parent::render($name, null, $attrs);
    }
}

class Stato_Webflow_Forms_Textarea extends Stato_Webflow_Forms_Input
{
    public function __construct(array $attrs = array())
    {
        $this->attrs = array_merge(array('cols' => 40, 'rows' => 10), $attrs);
    }
    
    public function render($name, $value = null, array $attrs = array())
    {
        $finalAttrs = array_merge(array('name' => $name), $this->attrs, $attrs);
        if ($value === null) $value = '';
        return '<textarea '.$this->flattenAttrs($finalAttrs).'>'.$value.'</textarea>';
    }
}

class Stato_Webflow_Forms_DateInput extends Stato_Webflow_Forms_TextInput
{
    protected $format = 'Y-m-d';
    
    public function __construct(array $attrs = array())
    {
        if (array_key_exists('format', $attrs)) {
            $this->format = $attrs['format'];
            unset($attrs['format']);
        }
        parent::__construct($attrs);
    }
    
    public function render($name, $value = null, array $attrs = array())
    {
        if ($value instanceof DateTime) $value = $value->format($this->format);
        return parent::render($name, $value, $attrs);
    }
}

class Stato_Webflow_Forms_DateTimeInput extends Stato_Webflow_Forms_DateInput
{
    protected $format = 'Y-m-d H:i:s';
}

class Stato_Webflow_Forms_TimeInput extends Stato_Webflow_Forms_DateInput
{
    protected $format = 'H:i:s';
}

class Stato_Webflow_Forms_CheckboxInput extends Stato_Webflow_Forms_Input
{
    protected $type = 'checkbox';
}

class Stato_Webflow_Forms_Select extends Stato_Webflow_Forms_Input
{
    protected $choices = array();
    
    public function __construct(array $attrs = array())
    {
        if (array_key_exists('choices', $attrs)) {
            $this->setChoices($attrs['choices']);
            unset($attrs['choices']);
        }
        parent::__construct($attrs);
    }
    
    public function setChoices(array $choices)
    {
        $this->choices = $choices;
    }
    
    public function render($name, $value = null, array $attrs = array())
    {
        $finalAttrs = array_merge(array('name' => $name), $this->attrs, $attrs);
        $options = $this->renderOptions($this->choices, $value);
        return '<select '.$this->flattenAttrs($finalAttrs).'>'.$options.'</select>';
    }
    
    protected function renderOptions($set, $selected = null)
    {
        $str = '';
        $nonAssoc = (key($set) === 0);
        if (!is_array($selected)) $selected = array($selected);
        foreach ($set as $value => $lib) {
            if (is_array($lib)) {
                $str.= '<optgroup label="'.$this->htmlEscape($value).'">'
                    .$this->renderOptions($lib, $selected).'</optgroup>';
            } else {
                if ($nonAssoc) $value = $lib;
                $str.= '<option value="'.$this->htmlEscape($value).'"';
                if (in_array($value, $selected)) $str.= ' selected="selected"';
                $str.= '>'.$this->htmlEscape($lib)."</option>\n";
            }
        }
        return $str;
    }
}

class Stato_Webflow_Forms_MultipleSelect extends Stato_Webflow_Forms_Select
{
    public function render($name, $value = null, array $attrs = array())
    {
        if (!preg_match('/.*\[\]$/', $name)) $name.= '[]';
        $finalAttrs = array_merge(array('name' => $name), $this->attrs, $attrs);
        $options = $this->renderOptions($this->choices, $value);
        return '<select multiple="multiple" '.$this->flattenAttrs($finalAttrs).'>'.$options.'</select>';
    }
}

class Stato_Webflow_Forms_CheckboxMultipleSelect extends Stato_Webflow_Forms_MultipleSelect
{
    public function render($name, $value = null, array $attrs = array())
    {
        if (!preg_match('/.*\[\]$/', $name)) $name.= '[]';
        $htmlAttrs = array_merge(array('type' => 'checkbox', 'name' => $name), $this->attrs, $attrs);
        $options = $this->renderCheckboxes($this->choices, $htmlAttrs, $value);
        return "<ul>\n".$options."</ul>\n";
    }
    
    protected function renderCheckboxes($set, $htmlAttrs, $selected = null, $i = 1)
    {
        $str = '';
        $nonAssoc = (key($set) === 0);
        if (!is_array($selected)) $selected = array($selected);
        foreach ($set as $value => $lib) {
            if (is_array($lib)) {
                $str.= '<li>'.$this->htmlEscape($value)."</li>\n<ul>\n"
                    .$this->renderCheckboxes($lib, $htmlAttrs, $selected, $i)."</ul>\n";
                $i = $i + count($lib);
            } else {
                if ($nonAssoc) $value = $lib;
                $finalAttrs = $htmlAttrs;
                if (array_key_exists('id', $htmlAttrs)) {
                    $finalAttrs['id'] = $htmlAttrs['id'].'_'.$i;
                    $labelFor = ' for="'.$finalAttrs['id'].'"';
                } else {
                    $labelFor = '';
                }
                $str.= '<li><label'.$labelFor.'>';
                $str.= '<input '.$this->flattenAttrs($finalAttrs).' value="'.$this->htmlEscape($value).'"';
                if (in_array($value, $selected)) $str.= ' checked="checked"';
                $str.= ' />'.$this->htmlEscape($lib)."</label></li>\n";
                $i++;
            }
        }
        return $str;
    }
}

class Stato_Webflow_Forms_RadioSelect extends Stato_Webflow_Forms_Select
{
    public function render($name, $value = null, array $attrs = array())
    {
        $htmlAttrs = array_merge(array('type' => 'radio', 'name' => $name), $this->attrs, $attrs);
        $options = $this->renderButtons($this->choices, $htmlAttrs, $value);
        return "<ul>\n".$options."</ul>\n";
    }
    
    protected function renderButtons($set, $htmlAttrs, $selected = null, $i = 1)
    {
        $str = '';
        $nonAssoc = (key($set) === 0);
        foreach ($set as $value => $lib) {
            if (is_array($lib)) {
                $str.= '<li>'.$this->htmlEscape($value)."</li>\n<ul>\n"
                    .$this->renderButtons($lib, $htmlAttrs, $selected, $i)."</ul>\n";
                $i = $i + count($lib);
            } else {
                if ($nonAssoc) $value = $lib;
                $finalAttrs = $htmlAttrs;
                if (array_key_exists('id', $htmlAttrs)) {
                    $finalAttrs['id'] = $htmlAttrs['id'].'_'.$i;
                    $labelFor = ' for="'.$finalAttrs['id'].'"';
                } else {
                    $labelFor = '';
                }
                $str.= '<li><label'.$labelFor.'>';
                $str.= '<input '.$this->flattenAttrs($finalAttrs).' value="'.$this->htmlEscape($value).'"';
                if ($value == $selected) $str.= ' checked="checked"';
                $str.= ' />'.$this->htmlEscape($lib)."</label></li>\n";
                $i++;
            }
        }
        return $str;
    }
}