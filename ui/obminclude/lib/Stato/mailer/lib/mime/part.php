<?php

class SMimePart extends SMimeEntity
{
    public function __construct($content = '', $content_type = 'text/plain', $encoding = '8bit', $charset = 'UTF-8')
    {
        parent::__construct();
        $this->content = $content;
        $this->charset = $charset;
        $this->set_content_type($content_type, array('charset' => $charset));
        $this->set_encoding($encoding);
    }
    
    public function __toString()
    {
        return $this->get_all_header_lines()
        .$this->eol.$this->eol.$this->get_content();
    }
    
    public function get_content()
    {
        return SMime::encode($this->content, $this->get_encoding(), $this->line_length, $this->eol);
    }
    
    public function set_content($content, $content_type = 'text/plain')
    {
        $this->content = $content;
        $this->set_content_type($content_type);
    }
    
    public function set_content_type($content_type, $options = array())
    {
        $value = $content_type;
        foreach ($options as $k => $v) {
            if ($v !== null) $value.= "; $k=\"$v\"";
        }
        $this->set_header('Content-Type', $value, false);
    }
    
    public function set_encoding($encoding)
    {
        $this->set_header('Content-Transfer-Encoding', $encoding);
    }
    
    public function get_encoding()
    {
        return $this->get_header('Content-Transfer-Encoding');
    }
}
