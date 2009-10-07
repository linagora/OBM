<?php

abstract class Stato_I18n_AbstractBackend
{
    private static $pluralRules = array
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
    
    public function translate($locale, $key, $options = array())
    {
        if (array_key_exists('count', $options)) $count = $options['count'];
        $values = array_diff_key($options, array('count' => null));
        
        $entry = $this->lookup($locale, $key);
        if (isset($count)) $entry = $this->pluralize($locale, $entry, $count);
        if (!empty($values)) $entry = $this->interpolate($locale, $entry, $values);
        return $entry;
    }
    
    protected function interpolate($locale, $entry, $values)
    {
        return str_replace(array_keys($values), array_values($values), $entry);
    }
    
    protected function pluralize($locale, $entry, $c)
    {
        if (!is_array($entry)) return $entry;
        if ($c == 0 && array_key_exists('zero', $entry)) $key = 'zero';
        else $key = eval($this->getPluralRule($locale));
        
        if (!array_key_exists($key, $entry))
            throw new Stato_I18nException('Invalid pluralization data: '.var_export($entry, true)."\n count: $c");
        
        return sprintf($entry[$key], $c);
    }
    
    protected function getPluralRule($locale)
    {
        foreach (self::$pluralRules as $rule => $locales)
            if (in_array($locale, $locales)) return 'return '.$rule.';';
    }
    
    abstract protected function lookup($locale, $key);
}