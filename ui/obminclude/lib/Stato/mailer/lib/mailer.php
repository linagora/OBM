<?php

/**
 * Class allowing you to group email sending features of your application
 * 
 * This class allows you to send emails using templates :
 * <code>
 * Stato_Mailer::setTemplateRoot('/path/to/msg/templates');
 * 
 * class UserMailer extends Stato_Mailer
 * {
 *     public function welcomeEmail($user) {
 *         $mail = new Stato_Mail();
 *         $mail->setTo($user->email_address);
 *         $mail->setBody($this->renderMessage('welcome', array('username' => $user->name)));
 *     }
 * }
 * </code>
 * In the mail defined above, the template at /path/to/msg/templates/welcome.php 
 * would be used to render the mail body. Parameters passed as second argument 
 * would be available as variables in the template :
 * <code>
 * Hello <?php echo $username; ?>
 * </code>
 * 
 * By default, mails are sent with the Stato_SendmailTransport class which 
 * uses mail() PHP function, but you can use another transport implementing 
 * the Stato_IMailTransport interface :
 * <code>
 * $transport = new Stato_SmtpTransport();
 * Stato_Mailer::setDefaultTransport($transport);
 * </code>
 *
 * @package Stato
 * @subpackage mailer
 */
class Stato_Mailer
{
    protected $recipients;
    protected $from;
    protected $date;
    protected $subject;
    protected $body;
    protected $parts;
    protected $attachments;
    
    protected static $templateRoot;
    protected static $transport;
    
    protected static $partDefaults = array(
        'content' => null,
        'content_type' => 'text/plain',
        'encoding' => '8bit',
        'charset' => 'UTF-8'
    );
    
    protected static $attachmentDefaults = array(
        'content' => null,
        'content_type' => 'application/octet-stream',
        'encoding' => 'base64',
        'filename' => null
    );
    
    public static function setTemplateRoot($path)
    {
        self::$templateRoot = $path;
    }
    
    public static function setDefaultTransport(Stato_IMailTransport $transport)
    {
        self::$transport = $transport;
    }
    
    public function __construct()
    {
        $this->reset();
    }
    
    public function __call($methodName, $args)
    {
        if (preg_match('/^send([a-zA-Z0-9_]*)$/', $methodName, $m))
            return $this->send($m[1], $args);
        elseif (preg_match('/^prepare([a-zA-Z0-9_]*)$/', $methodName, $m))
            return $this->prepare($m[1], $args);
        
        throw new Stato_MailException(get_class($this)."::$methodName() method does not exist");
    }
    
    public function prepare($methodName, $args)
    {
        if (!method_exists($this, $methodName))
            throw new Stato_MailException(get_class($this)."::$methodName() method does not exist");
        
        $mail = call_user_func_array(array($this, $methodName), $args);
        if (!($mail instanceof Stato_Mail)) $mail = $this->create($methodName, $args);
        return $mail;
    }
    
    public function send($methodName, $args)
    {
        $mail = $this->prepare($methodName, $args);
        return $mail->send($this->getTransport());
    }
    
    protected function create($methodName, $args)
    {
        $mail = new Stato_Mail($this->date);
        
        if (is_array($this->from)) $mail->setFrom($this->from[0], $this->from[1]);
        else $mail->setFrom($this->from);
        
        if (!is_array($this->recipients)) $this->recipients = array($this->recipients);
        foreach ($this->recipients as $to) {
            if (is_array($to)) $mail->addTo($to[0], $to[1]);
            else $mail->addTo($to);
        }
        
        $mail->setSubject($this->subject);
        
        if (is_array($this->body)) {
            $template = $this->getTemplateName($methodName);
            if (file_exists($this->getTemplatePath($template.'.plain')))
                $mail->setText($this->render($template.'.plain', $this->body));
            if (file_exists($this->getTemplatePath($template.'.html')))
                $mail->setHtmlText($this->render($template.'.html', $this->body));
        }
        
        foreach ($this->parts as $p) {
            $p = array_merge(self::$partDefaults, $p);
            $mail->addPart($p['content'], $p['content_type'], $p['encoding'], $p['charset']);
        }
        
        foreach ($this->attachments as $a) {
            $a = array_merge(self::$attachmentDefaults, $a);
            $mail->addAttachment($a['content'], $a['filename'], $a['content_type'], $a['encoding']);
        }
        
        $this->reset();
        
        return $mail;
    }
    
    protected function getTemplateName($methodName)
    {
        return strtolower(preg_replace('/([a-z\d])([A-Z])/', '\1_\2', 
                          preg_replace('/([A-Z]+)([A-Z][a-z])/', '\1_\2', $methodName)));
    }
    
    /**
     * Renders a message template
     * 
     * Throws an exception if the specified template is missing.
     * 
     * @param string $templateName
     * @param array $locals 
     * @return string
     */
    protected function render($templateName, $locals = array())
    {
        $templatePath = $this->getTemplatePath($templateName);
        
        if (!file_exists($templatePath) || !is_readable($templatePath))
            throw new Stato_MailException("Missing template $templatePath");
            
        extract($locals);
        ob_start();
        include $templatePath;
        return ob_get_clean();
    }
    
    /**
     * Returns the absolute path of a template
     * 
     * @param string $templateName
     * @return string
     */
    protected function getTemplatePath($templateName)
    {
        if (file_exists($templateName)) return $templateName;
        
        if (!isset(self::$templateRoot))
            throw new Stato_MailException('Template root not set');
            
        $templatePath = self::$templateRoot.'/'.$templateName.'.php';
            
        return $templatePath;
    }
    
    protected function getTransport()
    {
        return (isset(self::$transport)) ? self::$transport : new Stato_SendmailTransport();
    }
    
    protected function reset()
    {
        $this->recipients = array();
        $this->from = '';
        $this->date = null;
        $this->subject;
        $this->body = '';
        $this->parts = array();
        $this->attachments = array();
    }
}