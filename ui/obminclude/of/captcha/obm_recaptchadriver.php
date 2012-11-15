<?PHP

global $obminclude;
require_once($obminclude."/lib/captcha/recaptchalib.php");

class OBM_RecaptchaDriver implements OBM_CaptchaDriver {

  private $privateKey;
  private $publicKey;
  private $useSSL = true;
  

  public function __construct() {
    $configurationFile = "/etc/obm/captcha/recaptcha.php";
    require $configurationFile;
    $this->publicKey = $conf_recaptcha_publickey;
    $this->privateKey = $conf_recaptcha_privatekey;
    if ( isset($conf_recaptcha_use_ssl) ) {
      $this->useSSL = $conf_recaptcha_use_ssl;
    }
    if ( !strlen($this->publicKey) ) {
      die("recaptcha public key not found (using $configurationFile)");
    }
    if ( !strlen($this->publicKey) ) {
      die("recaptcha private key not found (using $configurationFile)");
    }
  }

  public function getCaptchaHTML() {
    $recaptcha_error = null;
    $recaptcha = recaptcha_get_html($this->publicKey, $recaptcha_error, $this->useSSL);
    $html = '
      <table class="captcha">
        <tr>
          <th><label for="captcha">reCAPTCHA</label></th>
          <td>'.$recaptcha.'</td>
        </tr>
      </table>
    ';
    return $html;
  }

  public function validateCaptcha() {
    $resp = recaptcha_check_answer ($this->privateKey,
                                    $_SERVER["REMOTE_ADDR"],
                                    $_POST["recaptcha_challenge_field"],
                                    $_POST["recaptcha_response_field"]);
    return $resp->is_valid;
  }
}