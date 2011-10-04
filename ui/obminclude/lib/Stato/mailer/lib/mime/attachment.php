<?php

class SMimeAttachment extends SMimePart
{
    public function __construct($content = '', $filename = null, $content_type = 'application/octet-stream', $encoding = 'base64')
    {
        parent::__construct($content, $content_type, $encoding);
        $options = array();
        if ($filename !== null) $options['name'] = $filename;
        $this->set_content_type($content_type, $options);
        $this->set_content_disposition($filename);
    }
    
    public function set_content_disposition($filename = null)
    {
        $value = 'attachment';
        if ($filename !== null) $value.= "; filename=\"$filename\"";
        $this->set_header('Content-Disposition', $value, false);
    }
}
