<?php

class SMailException extends Exception {}

/**
 * Class representing an email message
 * 
 * 
 * <code>
 * $mail = new SMail();
 * $mail->addTo('foo@bar.net');
 * $mail->setText('hello world');
 * $mail->send(new SSendmailTransport());
 * </code>
 *
 * @package Stato
 * @subpackage mailer
 */
class SMail extends SMimeEntity
{   
    protected $mime_version = '1.0';
    
    protected $date;
    
    protected $from;
    
    protected $recipients;
    
    public function __construct(DateTime $date = null, $charset = 'UTF-8')
    {
        parent::__construct();
        if ($date === null) {
            $tz = date_default_timezone_get();
            $date = new DateTime('now', new DateTimeZone($tz));
        }
        $this->date = $date;
        $this->charset = $charset;
        $this->recipients = array();
        $this->set_default_headers();
    }
    
    public function send(SIMailTransport $transport)
    {
        return $transport->send($this);
    }
    
    public function add_to($adress, $name = null)
    {
        $this->add_recipient('To', $adress, $name);
    }
    
    public function add_cc($adress, $name = null)
    {
        $this->add_recipient('Cc', $adress, $name);
    }
    
    public function add_bcc($adress, $name = null)
    {
        $this->add_recipient('Bcc', $adress, $name);
    }
    
    public function set_from($adress, $name = null)
    {
        $this->from = $adress;
        $this->add_recipient('From', $adress, $name);
    }
    
    public function set_subject($text)
    {
        $this->add_header('Subject', $text);
    }
    
    public function set_text($text, $content_type = 'text/plain')
    {
        if ($this->content === null) 
            $this->set_content(new SMimePart($text, $content_type));
        else 
            $this->add_part($text, $content_type);
    }
    
    public function set_html_text($text, $content_type = 'text/html')
    {
        $this->set_text($text, $content_type);
    }
    
    public function add_part($content, $content_type = 'text/plain', $encoding = '8bit', $charset = 'UTF-8')
    {
        $content = new SMimePart($content, $content_type, $encoding, $charset);
        if ($this->is_multipart()) 
            $this->content->add_part($content);
        elseif ($this->content === null)
            $this->set_content($content);
        else 
            $this->set_content(new SMimeMultipart(SMimeMultipart::ALTERNATIVE, array($this->content, $content)));
    }
    
    public function add_attachment($content, $filename = null, $content_type = 'application/octet-stream', $encoding = 'base64')
    {
        $content = new SMimeAttachment($content, $filename, $content_type, $encoding);
        if ($this->is_multipart() && $this->content->get_subtype() == SMimeMultipart::MIXED)
            $this->content->add_part($content);
        else 
            $this->set_content(new SMimeMultipart(SMimeMultipart::MIXED, array($this->content, $content)));
    }
    
    public function add_embedded_image($content, $content_id, $filename = null, $content_type = 'application/octet-stream', $encoding = 'base64')
    {
        $content = new SMimePart($content, $content_type, $encoding);
        if ($filename !== null) $content->set_content_type($content_type, array('name' => $filename));
        $content->set_header('Content-ID', '<'.$content_id.'>');
        if ($this->is_multipart() && $this->content->get_subtype() == SMimeMultipart::RELATED)
            $this->content->add_part($content);
        else 
            $this->set_content(new SMimeMultipart(SMimeMultipart::RELATED, array($this->content, $content)));
    }
    
    public function set_content($content, $content_type = 'text/plain')
    {
        if ($content instanceof SMimePart || $content instanceof SMimeMultipart)
            $this->content = $content;
        else
            $this->content = new SMimePart($content, $content_type);
    }
    
    public function set_boundary($boundary)
    {
        if (!$this->is_multipart())
            throw new SMailException('This message is not multipart, you can\'t set boundaries');
            
        $this->content->set_boundary($boundary);
    }
    
    public function get_all_header_lines()
    {
        return parent::get_all_header_lines();
    }
    
    public function get_matching_header_lines(array $names)
    {
        $lines = parent::get_matching_header_lines($names);
        if (is_object($this->content)) 
            $lines.= $this->eol.$this->content->get_matching_header_lines($names);
        return $lines;
    }
    
    public function get_non_matching_header_lines(array $names)
    {
        $lines = parent::get_non_matching_header_lines($names);
        if (is_object($this->content)) 
            $lines.= $this->eol.$this->content->get_non_matching_header_lines($names);
        return $lines;
    }
    
    public function get_content()
    {
        if ($this->content === null)
            throw new SMailException('No body specified');
        
        return $this->content->get_content();
    }
    
    public function get_to()
    {
        if (!array_key_exists('To', $this->headers))
            throw new SMailException('To: recipient is not specified');
        
        return $this->get_header('To');
    }
    
    public function get_from()
    {
        return $this->get_header('From');
    }
    
    public function get_cc()
    {
        return $this->get_header('Cc');
    }
    
    public function get_bcc()
    {
        return $this->get_header('Bcc');
    }
    
    public function get_subject()
    {
        return $this->get_header('Subject');
    }
    
    public function get_return_path()
    {
        return $this->from;
    }
    
    public function get_recipients()
    {
        return $this->recipients;
    }
    
    public function is_multipart()
    {
        return ($this->content instanceof SMimeMultipart);
    }
    
    private function add_recipient($header, $address, $name)
    {
        $address = strtr($address, "\r\n\t", '???');
        if (!in_array($address, $this->recipients)) $this->recipients[] = $address;
        if ($name !== null) $address = $this->encode_header($name)." <$address>";
        $this->add_header($header, $address, false);
    }
    
    private function set_default_headers()
    {
        $this->headers['Date'] = $this->date->format(DateTime::RFC822);
        $this->headers['MIME-Version'] = $this->mime_version;
    }
}

interface SIMailTransport
{
    public function send(SMail $mail);
}
