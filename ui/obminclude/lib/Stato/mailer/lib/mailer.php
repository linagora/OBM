<?php

/**
 * Class allowing you to group email sending features of your application
 * 
 * This class allows you to send emails using templates :
 * <code>
 * SMailer::set_template_root('/path/to/msg/templates');
 * 
 * class UserMailer extends SMailer
 * {
 *     public function welcome_email($user) {
 *         $this->to = $user->email_address;
 *         $this->body = array('username' => $user->name);
 *     }
 * }
 * </code>
 * In the mail defined above, the template at /path/to/msg/templates/welcome_email.php 
 * would be used to render the mail body. Parameters passed as second argument 
 * would be available as variables in the template :
 * <code>
 * Hello <?php echo $username; ?>
 * </code>
 * 
 * By default, mails are sent with the SSendmailTransport class which 
 * uses mail() PHP function, but you can use another transport implementing 
 * the SIMailTransport interface :
 * <code>
 * $transport = new SSmtpTransport();
 * SMailer::set_default_transport($transport);
 * </code>
 *
 * @package Stato
 * @subpackage mailer
 */
class SMailer
{
    protected $recipients;
    protected $from;
    protected $date;
    protected $subject;
    protected $body;
    protected $parts;
    protected $attachments;
    
    protected static $template_root;
    protected static $transport;
    
    protected static $part_defaults = array(
        'content' => null,
        'content_type' => 'text/plain',
        'encoding' => '8bit',
        'charset' => 'UTF-8'
    );
    
    protected static $attachment_defaults = array(
        'content' => null,
        'content_type' => 'application/octet-stream',
        'encoding' => 'base64',
        'filename' => null
    );
    
    public static function set_template_root($path)
    {
        self::$template_root = $path;
    }
    
    public static function set_default_transport(SIMailTransport $transport)
    {
        self::$transport = $transport;
    }
    
    public function __construct()
    {
        $this->reset();
    }
    
    public function __call($method_name, $args)
    {
        if (preg_match('/^send([a-zA-Z0-9_]*)$/', $method_name, $m))
            return $this->send($m[1], $args);
        elseif (preg_match('/^prepare([a-zA-Z0-9_]*)$/', $method_name, $m))
            return $this->prepare($m[1], $args);
        
        throw new SMailException(get_class($this)."::$method_name() method does not exist");
    }
    
    public function prepare($method_name, $args)
    {
        if (strpos($method_name, '_') === 0)
            $method_name = substr($method_name, 1);
        
        if (!method_exists($this, $method_name))
            throw new SMailException(get_class($this)."::$method_name() method does not exist");
        
        $mail = call_user_func_array(array($this, $method_name), $args);
        if (!($mail instanceof SMail)) $mail = $this->create($method_name, $args);
        return $mail;
    }
    
    public function send($method_name, $args)
    {
        $mail = $this->prepare($method_name, $args);
        return $mail->send($this->get_transport());
    }
    
    protected function create($method_name, $args)
    {
        $mail = new SMail($this->date);
        
        if (is_array($this->from)) $mail->set_from($this->from[0], $this->from[1]);
        else $mail->set_from($this->from);
        
        if (!is_array($this->recipients)) $this->recipients = array($this->recipients);
        foreach ($this->recipients as $to) {
            if (is_array($to)) $mail->add_to($to[0], $to[1]);
            else $mail->add_to($to);
        }
        
        $mail->set_subject($this->subject);
        
        if (is_array($this->body)) {
            $template = $this->get_template_name($method_name);
            if (file_exists($this->get_template_path($template.'.plain')))
                $mail->set_text($this->render($template.'.plain', $this->body));
            if (file_exists($this->get_template_path($template.'.html')))
                $mail->set_html_text($this->render($template.'.html', $this->body));
        } elseif (is_string($this->body) && !empty($this->body)) {
            $mail->set_text($this->body);
        }
        
        foreach ($this->parts as $p) {
            $p = array_merge(self::$part_defaults, $p);
            $mail->add_part($p['content'], $p['content_type'], $p['encoding'], $p['charset']);
        }
        
        foreach ($this->attachments as $a) {
            $a = array_merge(self::$attachment_defaults, $a);
            $mail->add_attachment($a['content'], $a['filename'], $a['content_type'], $a['encoding']);
        }
        
        $this->reset();
        
        return $mail;
    }
    
    protected function get_template_name($method_name)
    {
        return strtolower(preg_replace('/([a-z\d])([A-Z])/', '\1_\2', 
                          preg_replace('/([A-Z]+)([A-Z][a-z])/', '\1_\2', $method_name)));
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
    protected function render($template_name, $locals = array())
    {
        $template_path = $this->get_template_path($template_name);
        
        if (!file_exists($template_path) || !is_readable($template_path))
            throw new SMailException("Missing template $template_path");
            
        extract($locals);
        ob_start();
        include $template_path;
        return ob_get_clean();
    }
    
    /**
     * Returns the absolute path of a template
     * 
     * @param string $templateName
     * @return string
     */
    protected function get_template_path($template_name)
    {
        if (file_exists($template_name)) return $template_name;
        
        if (!isset(self::$template_root))
            throw new SMailException('Template root not set');
            
        $template_path = self::$template_root.'/'.$template_name.'.php';
            
        return $template_path;
    }
    
    protected function get_transport()
    {
        return (isset(self::$transport)) ? self::$transport : new SSendmailTransport();
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