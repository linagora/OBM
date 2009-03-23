<?php

class Stato_I18n_YamlBackend extends Stato_I18n_SimpleBackend
{
    protected function loadTranslationFile($file)
    {
        if (!function_exists('syck_load'))
            throw new Stato_I18nException('Syck extension is not installed');
            
        return syck_load(file_get_contents($file));
    }
    
    protected function getTranslationFilePath($path, $locale)
    {
        return $path.'/'.$locale.'.yml';
    }
}