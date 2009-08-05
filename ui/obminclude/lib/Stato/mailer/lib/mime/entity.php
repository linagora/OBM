<?php

class SMimeEntity
{
    protected $eol = "\n";
    
    protected $line_length = 72;
    
    protected $headers;
    
    protected $content;
    
    protected $charset;
    
    public function __construct()
    {
        $this->content = null;
        $this->charset = 'UTF-8';
        $this->headers = array();
    }
    
    public function __toString()
    {
        return $this->get_all_header_lines()
        .$this->eol.$this->eol.$this->get_content();
    }
    
    public function get_content()
    {
        return $this->content;
    }
    
    public function get_header($name, $delimiter = ',')
    {
        if (!array_key_exists($name, $this->headers)) return '';
        return $this->implode_header_value($this->headers[$name], $delimiter);
    }
    
    public function set_header($name, $value, $encode = true)
    {
        if ($encode) $value = $this->encode_header($value);
        $this->headers[$name] = $value;
    }
    
    public function add_header($name, $value, $encode = true)
    {
        if ($encode) $value = $this->encode_header($value);
        if (isset($this->headers[$name])) $this->headers[$name][] = $value;
        else $this->headers[$name] = array($value);
    }
    
    public function get_all_header_lines()
    {
        return $this->get_non_matching_header_lines(array());
    }
    
    public function get_matching_header_lines(array $names)
    {
        $h = array();
        foreach ($names as $name)
            if (array_key_exists($name, $this->headers))
                $h[] = "$name: ".$this->get_header($name);
        
        return implode($this->eol, $h);
    }
    
    public function get_non_matching_header_lines(array $names)
    {
        $h = array();
        foreach (array_keys($this->headers) as $name)
            if (!in_array($name, $names))
                $h[] = "$name: ".$this->get_header($name);
        
        return implode($this->eol, $h);
    }
    
    protected function implode_header_value($value, $delimiter = ',')
    {
        if (!is_array($value)) return $value;
        if (count($value) == 1) return array_pop($value);
        return implode($delimiter.' ', $value);
    }
    
    protected function encode_header($text)
    {
        return mb_encode_mimeheader($text, $this->charset, 'Q', $this->eol);
    }
}