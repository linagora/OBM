<?php

class SSmtpTransportException extends Exception {}

class SSmtpTransport implements SIMailTransport
{
    const EOL = "\r\n";
    
    private $host;
    
    private $name;
    
    private $port;
    
    private $ssl;
    
    private $auth;
    
    private $transport;
    
    private $username;
    
    private $password;
    
    private $socket;
    
    private $log;
    
    private $default_config = array(
        'port' => 25,
        'ssl' => false,
        'auth' => false,
        'name' => 'localhost',
        'username' => null,
        'password' => null
    );
    
    public function __construct($host, $config = array())
    {
        $config = array_merge($this->default_config, $config);
        $this->host = $host;
        $this->port = $config['port'];
        $this->name = $config['name'];
        $this->username = $config['username'];
        $this->password = $config['password'];
        
        $this->ssl = (boolean) $config['ssl'];
        $this->transport = ($this->ssl) ? 'ssl' : 'tcp';
        
        if (!in_array($config['auth'], array('plain', 'login', 'cram_md5', false)))
            throw new SSmtpTransportException("Unknown authentication type: {$config['auth']}");
        
        $this->auth = $config['auth'];
        $this->log = '';
    }
    
    public function __destruct()
    {
        $this->disconnect();
    }
    
    public function send(SMail $mail)
    {
        if (!isset($this->socket)) {
            $this->connect();
            $this->helo();
        } else {
            $this->rset();
        }
        $this->mail($mail->get_return_path());
        foreach ($mail->get_recipients() as $recipient) $this->rcpt($recipient);
        return $this->data($mail->__toString());
    }
    
    public function connect()
    {
        $remote = $this->transport.'://'.$this->host.':'.$this->port;
        $timeout = 30;
        $error_num = 0;
        $error_str = '';

        $socket = @stream_socket_client($remote, $error_num, $error_str, $timeout);

        if ($socket === false) {
            if ($error_num == 0) $error_str = 'Could not open socket';
            throw new SSmtpTransportException($error_str);
        }
        if (($result = stream_set_timeout($socket, $timeout)) === false)
            throw new SSmtpTransportException('Could not set stream timeout');

        $this->socket = $socket;
        $this->get_response();
        return $result;
    }
    
    /**
     * Disconnects from remote host
     *
     * @return void
     */
    public function disconnect()
    {
        if (is_resource($this->socket)) fclose($this->socket);
    }
    
    /**
     * Initiates HELO sequence
     *
     * @return void
     */
    public function helo($timeout = 300)
    {
        $this->send_command('HELO '.$this->name, 250, $timeout);
        return $this->auth();
    }
    
    public function auth()
    {
        if (!$this->auth) return true;
        
        switch ($this->auth) {
            case 'plain':
                return $this->plain_auth();
            case 'login':
                return $this->login_auth();
            case 'cram_md5':
                return $this->cram_md5_auth();
        }
    }
    
    public function rset()
    {
        return $this->send_command('RSET', array(250, 220));
    }
    
    public function mail($from)
    {
        return $this->send_command('MAIL FROM:<'.$from.'>', 250);
    }
    
    /**
     * Issues RCPT command
     *
     * @param string $to recipient mailbox
     * @return boolean
     */
    public function rcpt($to)
    {
        return $this->send_command('RCPT TO:<'.$to.'>', array(250, 251));
    }
    
    /**
     * Issues DATA command
     *
     * @param string $data
     * @return boolean
     */
    public function data($data)
    {
        $this->send_command('DATA', 354, 120);

        $lines = explode("\r\n", $data);
        foreach ($lines as $line) {
            // Escape lines beginning with a '.'
            if (strpos($line, '.') === 0) $line = '.'.$line;
            $this->send_command($line);
        }

        return $this->send_command('.', 250, 600);
    }
    
    /**
     * Sends the given command followed by a EOL to the server
     * 
     * If an $expectedCode is provided, this method will get the server
     * response and check the return code, else it will just send the command.
     *
     * @param string $command
     * @param mixed $expectedCode an integer or array of integers
     * @param integer $timeout
     * @return boolean
     */
    public function send_command($command, $expected_code = null, $timeout = null)
    {
        if (!is_resource($this->socket))
            throw new SSmtpTransportException('No connection has been established to '.$this->host);
        
        $result = fwrite($this->socket, $command.self::eol);

        $this->log.= $command.self::eol;

        if ($result === false)
            throw new SSmtpTransportException('Could not send command');

        if ($timeout !== null) {
           stream_set_timeout($this->socket, $timeout);
        }
        
        if ($expected_code === null) return true;
        
        list($code, $msg) = $this->get_response();
        
        if (!is_array($expected_code)) $expected_code = array($expected_code);
        if (!in_array($code, $expected_code))
            throw new SSmtpTransportException("Unexpected server response: $code $msg");
        
        return true;
    }
    
    /**
     * Gets and parses the server response
     *
     * @return array an array composed of the return code and message string
     */
    public function get_response()
    {
        $code = '';
        $msg = '';

        $response = fgets($this->socket, 1024);
        $this->log.= $response;

        $info = stream_get_meta_data($this->socket);

        if (!empty($info['timed_out']))
            throw new SSmtpTransportException($this->host.' has timed out');

        if ($response === false)
            throw new SSmtpTransportException('Could not read response');
        
        sscanf($response, '%d%s', $code, $msg);

        return array($code, $msg);
    }
    
    public function get_log()
    {
        return $this->log;
    }
    
    protected function plain_auth()
    {
        $this->send_command('AUTH PLAIN', 334);
        $this->send_command(base64_encode(chr(0).$this->username.chr(0).$this->password), 235);
        return true;
    }
    
    protected function login_auth()
    {
        $this->send_command('AUTH LOGIN', 334);
        $this->send_command(base64_encode($this->username), 334);
        $this->send_command(base64_encode($this->password), 235);
        return true;
    }
    
    protected function cram_md5_auth()
    {
        throw new SSmtpTransportException('Cram MD5 auth not implemented');
    }
}