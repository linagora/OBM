<?PHP

interface OBM_CaptchaDriver {

  /**
  * 
  *
  * @return string HTML code to present the captcha to the user
  */
  public function getCaptchaHTML();

  /**
  * 
  *
  * @return boolean whether or not the captcha has been validated
  */
  public function validateCaptcha();
}

/**
*
*
*
* 
*/
class OBM_captcha {
  private static $driver = null;
  private static $defaultDriver = "OBM_NoCaptchaDriver";
  public static function loadDriver() {
    global $captcha_driver;
    if ( self::$driver ) {
      return ;
    }
    if ( !$captcha_driver ) {
      $captcha_driver = self::$defaultDriver;
    }
    $captchaClassFile = self::fileNameFromClassName($captcha_driver);
    if ( !self::fileExists($captchaClassFile) ) {
      die("Unable to load captcha file $captchaClassFile");
    }
    require_once($captchaClassFile);
    self::$driver = new $captcha_driver;
  }

  public static function fileNameFromClassName ($className) {
    global $path;
    return $path."/../obminclude/of/captcha/".preg_replace("/[^a-z0-9_]/","",strtolower($className)).".php";
  }

  public static function fileExists ($fileName) {
    if ( file_exists($fileName) && is_file($fileName) ) {
      return true;
    }
    return false;
  }

  public static function getCaptchaHTML() {
    self::loadDriver();
    if ( !self::$driver ) {
      die("unable to provide captcha HTML, captcha driver is not loaded");
    }
    return self::$driver->getCaptchaHTML();
  }

  public static function validateCaptcha() {
    self::loadDriver();
    if ( !self::$driver ) {
      die("unable to validate captcha, captcha driver is not loaded");
    }
    return self::$driver->validateCaptcha();
  }
}

