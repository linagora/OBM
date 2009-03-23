<?php

class Stato_I18n_SimpleBackend extends Stato_I18n_AbstractBackend
{
    private $initialized = array();
    
    private $translations = array();
    
    protected function lookup($locale, $key)
    {
        if (!$this->isInitialized($locale)) $this->initTranslations($locale);
        if (array_key_exists($key, $this->translations[$locale]))
            return $this->translations[$locale][$key];
            
        return $key;
    }
    
    protected function initTranslations($locale)
    {
        $this->loadTranslations(Stato_I18n::getDataPaths(), $locale);
        $this->initialized[] = $locale;
    }
    
    protected function isInitialized($locale)
    {
        return in_array($locale, $this->initialized);
    }
    
    protected function loadTranslations($paths, $locale)
    {
        $this->translations[$locale] = array();
        
        foreach ($paths as $path) {
            $file = $this->getTranslationFilePath($path, $locale);
            if (file_exists($file)) {
                $translations = $this->loadTranslationFile($file);
                $this->translations[$locale] 
                    = array_merge($this->translations[$locale], $translations);
            }
        }
    }
    
    protected function loadTranslationFile($file)
    {
        return include($file);
    }
    
    protected function getTranslationFilePath($path, $locale)
    {
        return $path.'/'.$locale.'.php';
    }
}