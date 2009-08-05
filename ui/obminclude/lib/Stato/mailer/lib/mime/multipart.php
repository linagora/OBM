<?php

class SMimeMultipart extends SMimeEntity
{
    const MIXED = 'mixed';
    
    const RELATED = 'related';
    
    const ALTERNATIVE = 'alternative';
    
    protected $subtype;
    
    protected $parts;
    
    protected $boundary;
    
    protected $preamble;
    
    public function __construct($subtype = self::ALTERNATIVE, $parts = array(), $boundary = null)
    {
        parent::__construct();
        $this->subtype = $subtype;
        $this->parts = $parts;
        $this->preamble = 'This is a multi-part message in MIME format.';
        $this->set_boundary($boundary);
    }
    
    public function get_content()
    {
        $body = $this->preamble;
        
        foreach ($this->parts as $part)
            $body.= $this->boundary_line().$part->__toString();
            
        return $body.$this->boundary_end();
    }
    
    public function add_part($part)
    {
        if ($part instanceof SMimePart || $part instanceof SMimeMultipart)
            $this->parts[] = $part;
        else
            throw new SMailException('Parts added to a SMimeMultipart must be SMimePart or SMimeMultipart instances');
    }
    
    public function set_subtype($subtype)
    {
        $this->subtype = $subtype;
    }
    
    public function set_boundary($boundary = null)
    {
        if ($boundary === null) $boundary = md5(uniqid(time()));
        $this->boundary = $boundary;
        $this->set_header('Content-Type', "multipart/{$this->subtype}; boundary=\"{$this->boundary}\"", false);
    }
    
    public function set_preamble($preamble)
    {
        $this->preamble = $preamble;
    }
    
    public function get_subtype()
    {
        return $this->subtype;
    }
    
    protected function boundary_line()
    {
        return $this->eol.'--'.$this->boundary.$this->eol;
    }
    
    protected function boundary_end()
    {
        return $this->eol.'--'.$this->boundary.'--';
    }
}