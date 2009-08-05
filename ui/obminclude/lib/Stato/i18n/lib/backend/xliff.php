<?php

class SXliffBackend extends SSimpleBackend
{
    public function save($locale, $path)
    {
        $xml = new XMLWriter();
        $xml->openMemory();
        $xml->setIndent(true);
        $xml->startDocument('1.0', 'UTF-8');
        $xml->startElement('xliff');
        $xml->writeAttribute('version', '1.0');
        $xml->startElement('file');
        $xml->writeAttribute('original', 'global');
        $xml->writeAttribute('source-language', 'en');
        $xml->writeAttribute('target-language', $locale);
        $xml->writeAttribute('datatype', 'plaintext');
        $xml->startElement('body');
        
        $count = 1;
        foreach ($this->translations[$locale] as $key => $translation) {
            if (is_array($translation)) {
                $count2 = 0;
                $xml->startElement('group');
                $xml->writeAttribute('restype', 'x-gettext-plurals');
                foreach ($translation as $k => $v) {
                    $xml->startElement('trans-unit');
                    $xml->writeAttribute('id', "{$count}[{$count2}]");
                    if (is_string($k)) $xml->writeAttribute('resname', $k);
                    $xml->writeElement('source', $key);
                    $xml->writeElement('target', $v);
                    $xml->endElement();
                    $count2++;
                }
                $xml->endElement();
            } else {
                $xml->startElement('trans-unit');
                $xml->writeAttribute('id', $count);
                $xml->writeElement('source', $key);
                $xml->writeElement('target', $translation);
                $xml->endElement();
            }
            $count++;
        }
        
        $xml->endElement();
        $xml->endElement();
        $xml->endElement();
        file_put_contents($this->get_translation_file_path($path, $locale), $xml->flush());
    }
    
    protected function load_translation_file($file)
    {
        $xml = simplexml_load_file($file);
        $translations = array();
        
        foreach ($xml->xpath("/xliff/file/body/trans-unit") as $unit) {
            $source = (string) $unit->source;
            $translations[$source] = (string) $unit->target;
        }
        
        foreach ($xml->xpath("/xliff/file/body/group[@restype='x-gettext-plurals']") as $group) {
            $group_sources = array();
            $group_translations = array();
            foreach ($group->{'trans-unit'} as $unit) {
                $group_sources[] = (string) $unit->source;
                $resname = (string) $unit['resname'];
                if (!empty($resname))
                    $group_translations[$resname] = (string) $unit->target;
                else
                    $group_translations[] = (string) $unit->target;
            }
            foreach ($group_sources as $source) $translations[$source] = $group_translations;
        }
        
        return $translations;
    }
    
    protected function get_translation_file_path($path, $locale)
    {
        return $path.'/'.$locale.'.xml';
    }
}