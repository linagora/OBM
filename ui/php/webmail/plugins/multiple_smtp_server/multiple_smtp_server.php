<?php
/**
 * Multiple SMTP server
 *
 * Roundcube plugin to set multiple SMTP server.
 *
 * @version 0.2
 * @author Stefan Koch
 * @url https://github.com/unstko/Roundcube-plugins
 * @licence MIT License
 *
 * Copyright (c) 2011 Stefan Koch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class multiple_smtp_server extends rcube_plugin
{
    /**
     * @var string Task to run the plugin in.
     */
    public $task = 'mail';

    /**
     * @var mixed Instance of rcmail.
     */
    protected $rcmail = null;

    /**
     * @var boolean Saves if list of hosts is blank.
     */
    private $blank_hosts = false;

    /**
     * Mandatory method to initialize the plugin.
     *
     * @return void
     */
    public function init()
    {
        // Get rcmail instance
        $this->rcmail = rcmail::get_instance();

        // Load localization
        $this->add_texts('localization');

        // Load configuration
        $this->load_config('config/config.inc.php.dist');
        $this->load_config('config/config.inc.php');

        // Link hook for SMTP connection to method smtp_connect
        $this->add_hook('smtp_connect', array($this, 'smtp_connect'));
    }

    /**
     * Callback method for SMTP connection.
     *
     * @param mixed $args Argument containing context-specific data.
     * @return mixed Modified context-specific data.
     */
    public function smtp_connect($args)
    {
        // Check for rigth task
        if (strcmp($this->rcmail->task, $this->task)) {
            return $args;
        }

        // Get config values (global and local)
        $default_host = $this->rcmail->config->get('default_host', array());
        $multiple_smtp_server = $this->rcmail->config->get('multiple_smtp_server', array());
        $multiple_smtp_server_message = $this->rcmail->config->get('multiple_smtp_server_message', false);

        // Check config values
        if (!is_array($default_host) || !is_array($multiple_smtp_server) ||
            !array_count_values($multiple_smtp_server)) {
            return $args;
        }
        if (!array_count_values($default_host)) {
            $this->blank_hosts = true;
        }
        else {
            $this->blank_hosts = false;
        }

        // Get session values
        $imap_host = $_SESSION['imap_host'];
        $imap_port = $_SESSION['imap_port'];
        $imap_ssl = $_SESSION['imap_ssl'];
        $username = $_SESSION['username'];
        $password = $_SESSION['password'];

        // Set SMTP server for current host
        if (!$this->blank_hosts) {
            // Look through array of default hosts
            foreach ($default_host as $host_url => $host_name) {
                // Find right host
                $url = parse_url($host_url);
                $host = $url['host'];
                $port = $url['port'];
                $scheme = $url['scheme'];
                if (empty($host) || strcmp($host, $imap_host)) {
                    continue;
                }
                if (!empty($port)) {
                    if ($port != $imap_port) {
                        continue;
                    }
                }
                if (!empty($scheme)) {
                    if (!isset($imap_ssl) || strcmp($scheme, $imap_ssl)) {
                        continue;
                    }
                }

                // Set SMTP server
                foreach ($multiple_smtp_server as $imap_name => $smtp_server) {
                    if (strcmp($imap_name, $host_name)) {
                        continue;
                    }
                    $args['smtp_server'] = $smtp_server;
                    $args['smtp_user'] = $username;
                    $args['smtp_pass'] = $this->rcmail->decrypt($password);
                    break 2;
                }
            }
        }
        else {
            // Create complete host name (ssl, host and port)
            $host = "";
            if (isset($imap_ssl)) {
                // Types: ssl, tls or imaps
                $host .= $imap_ssl;
                $host .= "://";
            }
            $host .= $imap_host;
            $host .= ":";
            $host .= $imap_port;

            // Set SMTP server
            foreach ($multiple_smtp_server as $imap_url => $smtp_server) {
                if (strcmp($imap_url, $host)) {
                    continue;
                }
                $args['smtp_server'] = $smtp_server;
                $args['smtp_user'] = $username;
                $args['smtp_pass'] = $this->rcmail->decrypt($password);
                break;
            }
        }

        // Show message
        if ($multiple_smtp_server_message) {
            $smtp_server_message = $this->gettext('smtp_server_message');
            $url = parse_url($args['smtp_server']);
            if (empty($url['port'])) {
                $server_url = $args['smtp_server'].":".$args['smtp_port'];
            }
            else {
                $server_url = $args['smtp_server'];
            }
            $this->rcmail->output->show_message("$smtp_server_message: $server_url" , 'confirmation');
        }

        // Return (modified) arguments
        return $args;
    }
}
?>
