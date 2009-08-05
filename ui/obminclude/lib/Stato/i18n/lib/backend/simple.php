<?php

class SSimpleBackend extends SAbstractBackend
{
    protected $data_paths = array();
    protected $initialized = array();
    protected $translations = array();
    protected $comments = array();
    
    public function __construct($data_paths)
    {
        if (!is_array($data_paths)) $data_paths = array($data_paths);
        $this->data_paths = $data_paths;   
    }
    
    public function add_key($locale, $key, $comment = null)
    {
        $translation = $this->lookup($locale, $key);
        $this->store($locale, $key, $translation, $comment);
    }
    
    public function store($locale, $key, $translation, $comment = null)
    {
        if (!$this->is_initialized($locale)) $this->init_translations($locale);
        $this->translations[$locale][$key] = $translation;
        if (!is_null($comment)) $this->comments[$locale][$key] = $comment;
    }
    
    public function save($locale, $path)
    {
        $php = '';
        foreach ($this->translations[$locale] as $key => $translation) {
            if (array_key_exists($key, $this->comments[$locale]) && !empty($this->comments[$locale][$key])) {
                $php.= "    //".$this->comments[$locale][$key]."\n";
            }
            $php.= "    '".addcslashes(stripslashes($key), "'")."' => ";
            if (is_array($translation)) {
                $php.= "array(\n";
                foreach ($translation as $k => $v) {
                    $php.= "        ";
                    if (is_string($k)) $php.= "'".addcslashes(stripslashes($k), "'")."' => ";
                    $php.= "'".addcslashes(stripslashes($v), "'")."',\n";
                }
                $php.= "    ),\n";
            } else {
                $php.= "'".addcslashes(stripslashes($translation), "'")."',\n";
            }
        }
        file_put_contents($this->get_translation_file_path($path, $locale), 
                          "<?php\n\nreturn array(\n{$php});");
    }
    
    protected function lookup($locale, $key)
    {
        if (!$this->is_initialized($locale)) $this->init_translations($locale);
        if (array_key_exists($key, $this->translations[$locale]))
            return $this->translations[$locale][$key];
            
        return $key;
    }
    
    protected function init_translations($locale)
    {
        $this->load_translations($locale);
        $this->initialized[] = $locale;
    }
    
    protected function is_initialized($locale)
    {
        return in_array($locale, $this->initialized);
    }
    
    protected function load_translations($locale)
    {
        $this->translations[$locale] = array();
        $this->comments[$locale] = array();
        
        foreach ($this->data_paths as $path) {
            $file = $this->get_translation_file_path($path, $locale);
            if (file_exists($file)) {
                $translations = $this->load_translation_file($file);
                $this->translations[$locale] 
                    = array_merge($this->translations[$locale], $translations);
            }
        }
    }
    
    protected function load_translation_file($file)
    {
        return include($file);
    }
    
    protected function get_translation_file_path($path, $locale)
    {
        return $path.'/'.$locale.'.php';
    }
}