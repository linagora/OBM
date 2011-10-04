<?php

class SI18nException extends Exception {}

function __($key, $values = array())
{
    return SI18n::translate($key, $values);
}

function _f($key, $values = array())
{
    return SI18n::translatef($key, $values);
}

function _p($key, $count = 0)
{
    return SI18n::translate_and_pluralize($key, $count);
}

/**
 * I18n and localization class
 *
 * @package Stato
 * @subpackage i18n
 */
class SI18n
{
    private static $backend;
    
    private static $locale;
    
    private static $default_locale = 'en';
    
    private static $data_paths = array();
    
    public static function set_backend(SAbstractBackend $backend)
    {
        self::$backend = $backend;
    }
    
    public static function get_backend()
    {
        if (!isset(self::$backend)) 
            self::set_backend(new SSimpleBackend(self::$data_paths));
        
        return self::$backend;
    }
    
    public static function set_default_locale($locale)
    {
        self::$default_locale = $locale;
    }
    
    public static function get_default_locale()
    {
        return self::$default_locale;
    }
    
    public static function set_locale($locale)
    {
        self::$locale = $locale;
    }
    
    public static function get_locale()
    {
        if (!isset(self::$locale))
            return self::get_default_locale();
        
        return self::$locale;
    }
    
    public static function add_data_path($path)
    {
        self::$data_paths[] = $path;
    }
    
    public static function get_data_paths()
    {
        return self::$data_paths;
    }
    
    public static function translate($key, $values = array())
    {
        $locale = self::get_locale();
        return self::get_backend()->translate($locale, $key, $values);
    }
    
    public static function translatef($key, $values = array())
    {
        $locale = self::get_locale();
        return self::get_backend()->translatef($locale, $key, $values);
    }
    
    public static function translate_and_pluralize($key, $count = 0)
    {
        $locale = self::get_locale();
        return self::get_backend()->translate_and_pluralize($locale, $key, $count);
    }
}
