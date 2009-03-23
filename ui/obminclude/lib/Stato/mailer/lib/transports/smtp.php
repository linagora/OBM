<?php

class Stato_SmtpTransportException extends Exception {}

class Stato_SmtpTransport implements Stato_IMailTransport
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
    
    private $defaultConfig = array(
        'port' => 25,
        'ssl' => false,
        'auth' => false,
        'name' => 'localhost',
        'username' => null,
        'password' => null
    );
    
    public function __construct($host, $config = array())
    {
        $config = array_merge($this->defaultConfig, $config);
        $this->host = $host;
        $this->port = $config['port'];
        $this->name = $config['name'];
        $this->username = $config['username'];
        $this->password = $config['password'];
        
        $this->ssl = (boolean) $config['ssl'];
        $this->transport = ($this->ssl) ? 'ssl' : 'tcp';
        
        if (!in_array($config['auth'], array('plain', 'login', 'cram_md5', false)))
            throw new Stato_SmtpTransportException("Unknown authentication type: {$config['auth']}");
        
        $this->auth = $config['auth'];
        $this->log = '';
    }
    
    public function __destruct()
    {
        $this->disconnect();
    }
    
    public function send(Stato_Mail $mail)
    {
        if (!isset($this->socket)) {
            $this->connect();
            $this->helo();
        } else {
            $this->rset();
        }
        $this->mail($mail->getReturnPath());
        foreach ($mail->getRecipients() as $recipient) $this->rcpt($recipient);
        return $this->data($mail->__toString());
    }
    
    public function connect()
    {
        $remote = $this->transport.'://'.$this->host.':'.$this->port;
        $timeout = 30;
        $errorNum = 0;
        $errorStr = '';

        $socket = @stream_socket_client($remote, $errorNum, $errorStr, $timeout);

        if ($socket === false) {
            if ($errorNum == 0) $errorStr = 'Could not open socket';
            throw new Stato_SmtpTransportException($errorStr);
        }
        if (($result = stream_set_timeout($socket, $timeout)) === false)
            throw new Stato_SmtpTransportException('Could not set stream timeout');

        $this->socket = $socket;
        $this->getResponse();
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
        $this->sendCommand('HELO '.$this->name, 250, $timeout);
        return $this->auth();
    }
    
    public function auth()
    {
        if (!$this->auth) return true;
        
        switch ($this->auth) {
            case 'plain':
                return $this->plainAuth();
            case 'login':
                return $this->loginAuth();
            case 'cram_md5':
                return $this->cramMd5Auth();
        }
    }
    
    public function rset()
    {
        return $this->sendCommand('RSET', array(250, 220));
    }
    
    public function mail($from)
    {
        return $this->sendCommand('MAIL FROM:<'.$from.'>', 250);
    }
    
    /**
     * Issues RCPT command
     *
     * @param string $to recipient mailbox
     * @return boolean
     */
    public function rcpt($to)
    {
        return $this->sendCommand('RCPT TO:<'.$to.'>', array(250, 251));
    }
    
    /**
     * Issues DATA command
     *
     * @param string $data
     * @return boolean
     */
    public function data($data)
    {
        $this->sendCommand('DATA', 354, 120);

        $lines = explode("\r\n", $data);
        foreach ($lines as $line) {
            // Escape lines beginning with a '.'
            if (strpos($line, '.') === 0) $line = '.'.$line;
            $this->sendCommand($line);
        }

        return $this->sendCommand('.', 250, 600);
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
    public function sendCommand($command, $expectedCode = null, $timeout = null)
    {
        if (!is_resource($this->socket))
            throw new Stato_SmtpTransportException('No connection has been established to '.$this->host);
        
        $result = fwrite($this->socket, $command.self::EOL);

        $this->log.= $command.self::EOL;

        if ($result === false)
            throw new Stato_SmtpTransportException('Could not send command');

        if ($timeout !== null) {
           stream_set_timeout($this->socket, $timeout);
        }
        
        if ($expectedCode === null) return true;
        
        list($code, $msg) = $this->getResponse();
        
        if (!is_array($expectedCode)) $expectedCode = array($expectedCode);
        if (!in_array($code, $expectedCode))
            throw new Stato_SmtpTransportException("Unexpected server response: $code $msg");
        
        return true;
    }
    
    /**
     * Gets and parses the server response
     *
     * @return array an array composed of the return code and message string
     */
    public function getResponse()
    {
        $code = '';
        $msg = '';

        $response = fgets($this->socket, 1024);
        $this->log.= $response;

        $info = stream_get_meta_data($this->socket);

        if (!empty($info['timed_out']))
            throw new Stato_SmtpTransportException($this->host.' has timed out');

        if ($response === false)
            throw new Stato_SmtpTransportException('Could not read response');
        
        sscanf($response, '%d%s', $code, $msg);

        return array($code, $msg);
    }
    
    public function getLog()
    {
        return $this->log;
    }
    
    protected function plainAuth()
    {
        $this->sendCommand('AUTH PLAIN', 334);
        $this->sendCommand(base64_encode(chr(0).$this->username.chr(0).$this->password), 235);
        return true;
    }
    
    protected function loginAuth()
    {
        $this->sendCommand('AUTH LOGIN', 334);
        $this->sendCommand(base64_encode($this->username), 334);
        $this->sendCommand(base64_encode($this->password), 235);
        return true;
    }
    
    protected function cramMd5Auth()
    {
        throw new Stato_SmtpTransportException('Cram MD5 auth not implemented');
    }
}