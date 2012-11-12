<?PHP

class OBM_NoCaptchaDriver implements OBM_CaptchaDriver {
  public function getCaptchaHTML() {
    return "";
  }

  public function validateCaptcha() {
    return true;
  }
}