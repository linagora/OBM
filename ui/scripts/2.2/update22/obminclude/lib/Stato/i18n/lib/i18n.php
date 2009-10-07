<?php

class Stato_I18nException extends Exception {}

function __($key, $options = array())
{
    return Stato_I18n::translate($key, $options);
}

/**
 * I18n and localization class
 *
 * @package Stato
 * @subpackage i18n
 */
class Stato_I18n
{
    private static $backend;
    
    private static $locale;
    
    private static $defaultLocale = 'en';
    
    private static $dataPaths = array();
    
    public static function setBackend(Stato_I18n_AbstractBackend $backend)
    {
        self::$backend = $backend;
    }
    
    public static function getBackend()
    {
        if (!isset(self::$backend)) 
            self::setBackend(new Stato_I18n_SimpleBackend());
        
        return self::$backend;
    }
    
    public static function setDefaultLocale($locale)
    {
        self::$defaultLocale = $locale;
    }
    
    public static function getDefaultLocale()
    {
        return self::$defaultLocale;
    }
    
    public static function setLocale($locale)
    {
        self::$locale = $locale;
    }
    
    public static function getLocale()
    {
        if (!isset(self::$locale))
            return self::getDefaultLocale();
        
        return self::$locale;
    }
    
    public static function addDataPath($path)
    {
        self::$dataPaths[] = $path;
    }
    
    public static function getDataPaths()
    {
        return self::$dataPaths;
    }
    
    public static function translate($key, $options = array())
    {
        $locale = self::getLocale();
        return self::getBackend()->translate($locale, $key, $options);
    }
}
