<?php

class Stato_MailException extends Exception {}

/**
 * Class representing an email message
 * 
 * 
 * <code>
 * $mail = new Stato_Mail();
 * $mail->addTo('foo@bar.net');
 * $mail->setText('hello world');
 * $mail->send(new Stato_SendmailTransport());
 * </code>
 *
 * @package Stato
 * @subpackage mailer
 */
class Stato_Mail extends Stato_MimeEntity
{   
    protected $mimeVersion = '1.0';
    
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
        $this->setDefaultHeaders();
    }
    
    public function send(Stato_IMailTransport $transport)
    {
        return $transport->send($this);
    }
    
    public function addTo($adress, $name = null)
    {
        $this->addRecipient('To', $adress, $name);
    }
    
    public function addCc($adress, $name = null)
    {
        $this->addRecipient('Cc', $adress, $name);
    }
    
    public function addBcc($adress, $name = null)
    {
        $this->addRecipient('Bcc', $adress, $name);
    }
    
    public function setFrom($adress, $name = null)
    {
        $this->from = $adress;
        $this->addRecipient('From', $adress, $name);
    }
    
    public function setSubject($text)
    {
        $this->addHeader('Subject', $text);
    }
    
    public function setText($text, $contentType = 'text/plain')
    {
        if ($this->content === null) 
            $this->setContent(new Stato_MimePart($text, $contentType));
        else 
            $this->addPart($text, $contentType);
    }
    
    public function setHtmlText($text, $contentType = 'text/html')
    {
        $this->setText($text, $contentType);
    }
    
    public function addPart($content, $contentType = 'text/plain', $encoding = '8bit', $charset = 'UTF-8')
    {
        $content = new Stato_MimePart($content, $contentType, $encoding, $charset);
        if ($this->isMultipart()) 
            $this->content->addPart($content);
        elseif ($this->content === null)
            $this->setContent($content);
        else 
            $this->setContent(new Stato_MimeMultipart(Stato_MimeMultipart::ALTERNATIVE, array($this->content, $content)));
    }
    
    public function addAttachment($content, $filename = null, $contentType = 'application/octet-stream', $encoding = 'base64')
    {
        $content = new Stato_MimeAttachment($content, $filename, $contentType, $encoding);
        if ($this->isMultipart() && $this->content->getSubtype() == Stato_MimeMultipart::MIXED)
            $this->content->addPart($content);
        else 
            $this->setContent(new Stato_MimeMultipart(Stato_MimeMultipart::MIXED, array($this->content, $content)));
    }
    
    public function addEmbeddedImage($content, $contentId, $filename = null, $contentType = 'application/octet-stream', $encoding = 'base64')
    {
        $content = new Stato_MimePart($content, $contentType, $encoding);
        if ($filename !== null) $content->setContentType($contentType, array('name' => $filename));
        $content->setHeader('Content-ID', '<'.$contentId.'>');
        if ($this->isMultipart() && $this->content->getSubtype() == Stato_MimeMultipart::RELATED)
            $this->content->addPart($content);
        else 
            $this->setContent(new Stato_MimeMultipart(Stato_MimeMultipart::RELATED, array($this->content, $content)));
    }
    
    public function setContent($content, $contentType = 'text/plain')
    {
        if ($content instanceof Stato_MimePart || $content instanceof Stato_MimeMultipart)
            $this->content = $content;
        else
            $this->content = new Stato_MimePart($content, $contentType);
    }
    
    public function setBoundary($boundary)
    {
        if (!$this->isMultipart())
            throw new Stato_MailException('This message is not multipart, you can\'t set boundaries');
            
        $this->content->setBoundary($boundary);
    }
    
    public function getAllHeaderLines()
    {
        return parent::getAllHeaderLines();
    }
    
    public function getMatchingHeaderLines(array $names)
    {
        $lines = parent::getMatchingHeaderLines($names);
        if (is_object($this->content)) 
            $lines.= $this->eol.$this->content->getMatchingHeaderLines($names);
        return $lines;
    }
    
    public function getNonMatchingHeaderLines(array $names)
    {
        $lines = parent::getNonMatchingHeaderLines($names);
        if (is_object($this->content)) 
            $lines.= $this->eol.$this->content->getNonMatchingHeaderLines($names);
        return $lines;
    }
    
    public function getContent()
    {
        if ($this->content === null)
            throw new Stato_MailException('No body specified');
        
        return $this->content->getContent();
    }
    
    public function getTo()
    {
        if (!array_key_exists('To', $this->headers))
            throw new Stato_MailException('To: recipient is not specified');
        
        return $this->getHeader('To');
    }
    
    public function getFrom()
    {
        return $this->getHeader('From');
    }
    
    public function getCc()
    {
        return $this->getHeader('Cc');
    }
    
    public function getBcc()
    {
        return $this->getHeader('Bcc');
    }
    
    public function getSubject()
    {
        return $this->getHeader('Subject');
    }
    
    public function getReturnPath()
    {
        return $this->from;
    }
    
    public function getRecipients()
    {
        return $this->recipients;
    }
    
    public function isMultipart()
    {
        return ($this->content instanceof Stato_MimeMultipart);
    }
    
    private function addRecipient($header, $address, $name)
    {
        $address = strtr($address, "\r\n\t", '???');
        if (!in_array($address, $this->recipients)) $this->recipients[] = $address;
        if ($name !== null) $address = $this->encodeHeader($name)." <$address>";
        $this->addHeader($header, $address, false);
    }
    
    private function setDefaultHeaders()
    {
        $this->headers['Date'] = $this->date->format(DateTime::RFC822);
        $this->headers['MIME-Version'] = $this->mimeVersion;
    }
}

interface Stato_IMailTransport
{
    public function send(Stato_Mail $mail);
}
