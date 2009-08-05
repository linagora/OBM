<?php

abstract class SAbstractBackend
{
    private static $plural_rules = array
    (
        '0' => array('hu','ja','ko','tr'),
        '$c == 1 ? 0 : 1' => array('da','nl','en','de','no','sv','et','fi','fr','el','he','it','pt','es','eo'),
        '$c == 1 ? 0 : ($c == 2 ? 1 : 2)' => array('ga','gd'),
        '($c%10 == 1 && $c%100 != 11) ? 0 : ($c%10 >= 2 && $c%10 <= 4 && ($c%100 < 10 || $c%100 >= 20) ? 1 : 2)' => array('hr','cs','ru','sk','uk'),
        '($c%10 == 1 && $c%100 != 11) ? 0 : ($c != 0 ? 1 : 2)' => array('lv'),
        '($c%10 == 1 && $c%100 != 11) ? 0 : ($c%10 >= 2 && ($c%100 < 10 || $c%100 >= 20) ? 1 : 2)' => array('lt'),
        '$c == 1 ? 0 : ($c%10 >= 2 && $c%10 <= 4 && ($c%100 < 10 || $c%100 >= 20) ? 1 : 2)' => array('pl'),
        '$c%100 == 1 ? 0 : ($c%100 == 2 ? 1 : ($c%100 == 3 || $c%100 == 4 ? 2 : 3))' => array('sl')
    );
    
    public function translate($locale, $key, $values = array())
    {
        $entry = $this->lookup($locale, $key);
        if (!empty($values)) $entry = $this->interpolate($locale, $entry, $values);
        return $entry;
    }
    
    public function translatef($locale, $key, $values = array())
    {
        $entry = $this->lookup($locale, $key);
        return vsprintf($entry, $values);
    }
    
    public function translate_and_pluralize($locale, $key, $count = 0)
    {
        $entry = $this->lookup($locale, $key);
        return $this->pluralize($locale, $entry, $count);
    }
    
    protected function interpolate($locale, $entry, $values)
    {
        $p_values = array();
        foreach ($values as $k => $v) {
            if (!preg_match('/%[a-zA-Z0-9_\-]+%/', $k)) $k = '%'.$k.'%';
            $p_values[$k] = $v;
        }
        return str_replace(array_keys($p_values), array_values($p_values), $entry);
    }
    
    protected function pluralize($locale, $entry, $c)
    {
        if (!is_array($entry)) return $entry;
        if ($c == 0 && array_key_exists('zero', $entry)) $key = 'zero';
        else $key = eval($this->get_plural_rule($locale));
        
        if (!array_key_exists($key, $entry))
            throw new SI18nException('Invalid pluralization data: '.var_export($entry, true)."\n count: $c");
        
        return sprintf($entry[$key], $c);
    }
    
    protected function get_plural_rule($locale)
    {
        foreach (self::$plural_rules as $rule => $locales)
            if (in_array($locale, $locales)) return 'return '.$rule.';';
    }
    
    abstract protected function lookup($locale, $key);
}