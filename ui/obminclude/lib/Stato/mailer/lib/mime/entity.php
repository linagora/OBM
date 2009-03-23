<?php

class Stato_MimeEntity
{
    protected $eol = "\n";
    
    protected $lineLength = 72;
    
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
        return $this->getAllHeaderLines()
        .$this->eol.$this->eol.$this->getContent();
    }
    
    public function getContent()
    {
        return $this->content;
    }
    
    public function getHeader($name, $delimiter = ',')
    {
        if (!array_key_exists($name, $this->headers)) return '';
        return $this->implodeHeaderValue($this->headers[$name], $delimiter);
    }
    
    public function setHeader($name, $value, $encode = true)
    {
        if ($encode) $value = $this->encodeHeader($value);
        $this->headers[$name] = $value;
    }
    
    public function addHeader($name, $value, $encode = true)
    {
        if ($encode) $value = $this->encodeHeader($value);
        if (isset($this->headers[$name])) $this->headers[$name][] = $value;
        else $this->headers[$name] = array($value);
    }
    
    public function getAllHeaderLines()
    {
        return $this->getNonMatchingHeaderLines(array());
    }
    
    public function getMatchingHeaderLines(array $names)
    {
        $h = array();
        foreach ($names as $name)
            if (array_key_exists($name, $this->headers))
                $h[] = "$name: ".$this->getHeader($name);
        
        return implode($this->eol, $h);
    }
    
    public function getNonMatchingHeaderLines(array $names)
    {
        $h = array();
        foreach (array_keys($this->headers) as $name)
            if (!in_array($name, $names))
                $h[] = "$name: ".$this->getHeader($name);
        
        return implode($this->eol, $h);
    }
    
    protected function implodeHeaderValue($value, $delimiter = ',')
    {
        if (!is_array($value)) return $value;
        if (count($value) == 1) return array_pop($value);
        return implode($delimiter.' ', $value);
    }
    
    protected function encodeHeader($text)
    {
        return mb_encode_mimeheader($text, $this->charset, 'Q', $this->eol);
    }
}