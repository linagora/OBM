<?php
/******************************************************************************
Copyright (C) 2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/

class Url {
  private $base_path;
  private $params;

  public function __construct($base_path, $params=null) {
    $this->base_path = $base_path;
    $this->params = $params;
  }

  public function as_string() {
    return $this->params ? $this->base_path.'?'.http_build_query($this->params) : $this->base_path;
  }
}

interface Curlable {
  public function as_curl_flags();
}

class Credentials implements Curlable {
  private $login;
  private $password;
  private $domain;

  public function __construct($login, $password, $domain) {
    $this->login = $login;
    $this->domain = $domain;
    $this->password = $password;
  }

  public function as_curl_flags() {
    return array(
      CURLOPT_USERPWD => $this->login.'@'.$this->domain.':'.$this->password
    );
  }
}

class BasicAuth implements Curlable {

  private $credentials;

  public function __construct($credentials) {
    $this->credentials = $credentials;
  }

  public function as_curl_flags() {
    $result = $this->credentials->as_curl_flags();
    $result[CURLOPT_HTTPAUTH] = CURLAUTH_BASIC;
    return $result;
  }
}

class HttpHeaders implements Curlable {
  private $headers;

  public function __construct($headers) {
    $this->headers = $headers;
  }

  public function as_curl_flags() {
    $header_values = array();
    foreach ($this->headers as $key => $value) {
      $header_values[] = $key.': '.$value;
    }
    return array(
      CURLOPT_HTTPHEADER => $header_values
    );
  }
}

class BodyInterceptor {
  private $callback;

  public function __construct($callback) {
    $this->callback = $callback;
  }

  public function invoke_callbak($curl_handle, $chunk) {
    $callback = $this->callback;
    $callback($chunk);
    return strlen($chunk);
  }
}

class HttpContentCallback implements Curlable {
  private $callback;

  public function __construct($callback) {
    $this->callback = $callback;
  }

  public function as_curl_flags() {
    $body_interceptor = new BodyInterceptor($this->callback);
    return array(
      CURLOPT_WRITEFUNCTION =>  array($body_interceptor, 'invoke_callbak')
    );
  }
}

class HttpConfig implements Curlable {

  public $curlables;
  public $max_redirects;

  public static function from_config($config, $curlables = array(), $max_redirects = 0) {
    return new HttpConfig(array_merge($curlables, $config->curlables), $max_redirects);
  }

  public function __construct($curlables = array(), $max_redirects = 0) {
    $this->curlables = $curlables;
    $this->max_redirects = $max_redirects;
  }

  public function as_curl_flags() {
    $result = array();
    foreach ($this->curlables as $curlable) {
      $curl_flags = $curlable->as_curl_flags();
      $result += $curl_flags;
    }
    return $result;
  }
}

class Result {
  public $code;
  private $content;
  public $headers;

  public function __construct($code, $content, $headers) {
    $this->code = $code;
    $this->content = $content;
    $this->headers = $headers;
  }

  public function as_json() {
    return $this->content ? json_decode($this->content) : null;
  }

  public function as_text() {
    return $this->content;
  }
}

class HttpHeadersWriter {
    private $headers = array();
    private $headers_seen = 0;

    public function parse_header($curl, $header) {
      $this->headers_seen ++;
      // Skip status line
      if ($this->headers_seen > 1 && trim($header)) {
        list($name, $value) = explode(':', $header, 2);
        $this->headers[trim($name)] = trim($value);
      }
      return strlen($header);
    }

    public function get_headers() {
        return $this->headers;
    }
}

class HttpClient {
  private $config;

  public function __construct($config) {
    $this->config = $config;
  }

  private function build_curl($url) {
    $curl = curl_init($url->as_string());
    curl_setopt($curl, CURLOPT_FAILONERROR, 1);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    foreach ($this->config->as_curl_flags() as $flag => $value) {
      curl_setopt($curl, $flag, $value);
    }
    return $curl;
  }

  public function get($url) {
    $curl = $this->build_curl($url);
    return $this->http_exec_with_redirects($curl, $url->as_string());
  }

  public function post($url, $values) {
    $curl = $this->build_curl($url);
    curl_setopt($curl, CURLOPT_POST, 1);
    curl_setopt($curl, CURLOPT_POSTFIELDS, $values);
    return $this->http_exec_with_redirects($curl, $url->as_string());
  }

  public function put($url, $values) {
    $curl = $this->build_curl($url);
    curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "PUT");
    curl_setopt($curl, CURLOPT_POSTFIELDS, $values);
    return $this->http_exec_with_redirects($curl, $url->as_string());
  }

  private function http_exec_with_redirects($curl, $url) {
    $final_result = null;
    $redirects_count = 0;
    while (true) {
      try {
        $result = $this->http_exec($curl, $url);
        if ($result->code == 302 || $result->code == 303) {
          if ($redirects_count > $this->config->max_redirects) {
            throw new HttpClientMaxRedirectsException($url);
          }
          if ($result->code == 303) {
              curl_setopt($curl, CURLOPT_HTTPGET, 1);
          }
          $redirect_url = $result->headers['Location'];
          if (!$redirect_url) {
            throw new HttpClientRedirectLocationMissingException($url);
          }
          curl_setopt($curl, CURLOPT_URL, $redirect_url);
        }
        else {
          $final_result = $result;
          break;
        }
      }
      catch(Exception $e) {
        curl_close($curl);
        throw $e;
      }
      $redirects_count++;
    }
    curl_close($curl);
    return $final_result;
  }

  private function http_exec($curl, $url) {
    $headers_writer = new HttpHeadersWriter();
    curl_setopt($curl, CURLOPT_HEADERFUNCTION, array($headers_writer, 'parse_header'));
    $result = curl_exec($curl);
    $errno = curl_errno($curl);
    $message = curl_error($curl);
    $code = curl_getinfo($curl, CURLINFO_HTTP_CODE);

    $response_headers = $headers_writer->get_headers();
    $this->assert_call_successful($errno, $message, $code, $url);
    return new Result($code, $result, $response_headers);
  }

  private function assert_call_successful($errno, $message, $code, $url) {
    if ($errno) {
      $CURLE_HTTP_RETURNED_ERROR = 22;
      if ($errno == $CURLE_HTTP_RETURNED_ERROR) {
        throw new HttpClientHttpException($url, $code);
      }
      else {
        throw new HttpClientOtherException($url, $errno, $message);
      }
    }
    elseif ($code == 0) {
      throw new HttpClientServerNotFoundException($url);
    }
  }
}

abstract class HttpClientException extends Exception {
  public $url;

  function __construct($url) {
    $this->url = $url;
  }
}

class HttpClientHttpException extends HttpClientException {
  public $code;

  function __construct($url, $code) {
    parent::__construct($url);
    $this->code = $code;
  }
}

class HttpClientOtherException extends HttpClientException {
  public $curl_errno;
  public $message;

  function __construct($url, $curl_errno, $message) {
    parent::__construct($url);
    $this->curl_errno = $curl_errno;
    $this->message = $message;
  }
}

class HttpClientServerNotFoundException extends HttpClientException {}

class HttpClientRedirectLocationMissingException extends HttpClientException {}

class HttpClientMaxRedirectsException extends HttpClientException {}
?>
