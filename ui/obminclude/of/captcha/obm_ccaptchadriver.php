<?PHP
/******************************************************************************
 Copyright (C) 2011-2014 Linagora

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

class OBM_CCaptchaDriver implements OBM_CaptchaDriver {
  private $configurationFile = "/etc/obm/captcha/ccaptcha.php";
  private $url;
  private $client;

  public function __construct() {
    require $this->configurationFile;

    $this->url = $conf_ccaptcha_url;
    if (!isset($this->url) || !strlen($this->url)) {
      die("CCaptcha server URL not found (using $this>configurationFile)");
    }

    try {
      $this->client = new SoapClient($this->url . "?wsdl");
    } catch (Exception $e) {
      die("Couldn't start SOAP client (using $this->url). Exception: " . $e);
    }
  }

  public function getCaptchaHTML() {
    $captchaInfo = $this->client->getCaptcha();
    if (!$captchaInfo || !is_array($captchaInfo) || count($captchaInfo) < 2) {
      die("CCaptcha service is unavailable");
    }

    $_SESSION["captcha"] = $captchaInfo[0];

    return  '
              <fieldset class="detail">
                <table>
                  <tr>
                    <th><label for="captchaField">' . __("Captcha") . '</label></th>
                    <td><img src="' . $captchaInfo[1] . '" /></td>
                  </tr>
                  <tr>
                    <th><label for="captchaVerificationField">' . __("Verification") . '</label></th>
                    <td><input id="captchaVerificationField" type="text" name="captcha" /></td>
                  </tr>
                </table>
              </fieldset>
            ';
  }

  public function validateCaptcha() {
    return $_POST["captcha"] === $_SESSION["captcha"];
  }
}
