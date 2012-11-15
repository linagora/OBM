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
  private static $captchaActive = null;

  public static function loadHookFile() {
    global $path;
    $captchaHookFile = "$path/../conf/hooks/captcha/preload.inc";
    if( !file_exists($captchaHookFile) ) {
      self::$captchaActive = true;
      return ;
    }
    require_once($captchaHookFile);
    self::$captchaActive = hook_captcha_preload();
  }

  public static function loadDriver() {
    global $captcha_driver;
    if ( self::$captchaActive === null ) {
      self::loadHookFile();
    }
    if ( !self::$captchaActive ) {
      return ;
    }
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
    if ( !self::$captchaActive ) {
      return "";
    }
    if ( !self::$driver ) {
      die("unable to provide captcha HTML, captcha driver is not loaded");
    }
    return self::$driver->getCaptchaHTML();
  }

  public static function validateCaptcha() {
    self::loadDriver();
    if ( !self::$captchaActive ) {
      return true;
    }
    if ( !self::$driver ) {
      die("unable to validate captcha, captcha driver is not loaded");
    }
    return self::$driver->validateCaptcha();
  }
}

