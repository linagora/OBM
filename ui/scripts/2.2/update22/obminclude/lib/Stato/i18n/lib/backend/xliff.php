<?php

class Stato_I18n_XliffBackend extends Stato_I18n_SimpleBackend
{
    protected function loadTranslationFile($file)
    {
        $xml = simplexml_load_file($file);
        $translations = array();
        
        foreach ($xml->xpath("/xliff/file/body/trans-unit") as $unit) {
            $source = (string) $unit->source;
            $translations[$source] = (string) $unit->target;
        }
        
        foreach ($xml->xpath("/xliff/file/body/group[@restype='x-gettext-plurals']") as $group) {
            $groupSources = array();
            $groupTranslations = array();
            foreach ($group->{'trans-unit'} as $unit) {
                $groupSources[] = (string) $unit->source;
                $resname = (string) $unit['resname'];
                if (!empty($resname))
                    $groupTranslations[$resname] = (string) $unit->target;
                else
                    $groupTranslations[] = (string) $unit->target;
            }
            foreach ($groupSources as $source) $translations[$source] = $groupTranslations;
        }
        
        return $translations;
    }
    
    protected function getTranslationFilePath($path, $locale)
    {
        return $path.'/'.$locale.'.xml';
    }
}