<?php

class MakemessagesCommand extends SCommand
{
    protected $allowed_options = array('path' => true, 'lang' => true, 'backend' => true);
    
    private $root_path;
    private $tokens;
    private $messages;
    private $functions = array('__', '_f', '_p');
    
    public function execute()
    {
        if (isset($this->options['path'])) $this->root_path = rtrim($this->options['path'], '/');
        else $this->root_path = getcwd();
        
        $app_path = $this->root_path.'/app';
        $locale_path = $this->root_path.'/app/i18n';
        
        if (!file_exists($app_path))  {
            echo "It looks like you're not at the root directory of a Stato project.\n";
            return;
        }
        
        if (!isset($this->options['lang'])) {
            echo "Please provide a language code.\n";
            return;
        }
        
        $backend_name = (!isset($this->options['backend'])) ? 'simple' : $this->options['backend'];
        $backend_class = 'S'.ucfirst($backend_name).'Backend';
        $backend = new $backend_class($locale_path);
        
        $this->messages = array();
        
        $it = new RecursiveDirectoryIterator($app_path);
        foreach (new RecursiveIteratorIterator($it) as $file) {
            if ($file->isFile() && !preg_match('/\.svn/', $file->getPathname()))
                $this->extract_messages((string) $file);
        }
        
        foreach ($this->messages as $comment => $message) {
            $backend->add_key($this->options['lang'], $message, $comment);
        }
        
        $backend->save($this->options['lang'], $locale_path);
    }
    
    private function extract_messages($filepath)
    {
        $this->tokens = token_get_all(file_get_contents($filepath));
        while ($token = current($this->tokens)) {
            if (!is_string($token)) {
                list($id, $text) = $token;
                if ($id == T_STRING && in_array($text, $this->functions)) {
                    $this->process_message($filepath);
                    continue;
                }
            }
            next($this->tokens);
        }
    }
    
    private function process_message($current_file)
    {
        next($this->tokens);
        while ($t = current($this->tokens)) {
            if (is_string($t) || (is_array($t) && ($t[0] == T_WHITESPACE || $t[0] == T_DOC_COMMENT || $t[0] == T_COMMENT))) {
                next($this->tokens);
            } else {
                $this->store_message(trim($t[1], "'"), $current_file, $t[2]);
                next($this->tokens);
                return;
            }
        }
    }
    
    private function store_message($message, $current_file, $line)
    {
        $file = str_replace($this->root_path.'/', '', $current_file);
        $this->messages[$file.':'.$line] = $message;
    }
}

?>
