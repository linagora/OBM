<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"> 
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Login - OBM $obm_version</title>
    <link rel="stylesheet" type="text/css" href="$css_obm" />
    <style>
      body {
        text-align: center;
        margin-top: 1em;
      }
      form {
        margin: auto;
        width: 50em;
      }
      select {
        width: 50%;
      }
      #linagora {
        margin-top:0;
        margin-bottom:0;
        width: 100%;
        color: #FFF;
      }
      h1, h2{
        color: #FAFAFA;
      }
      h2{
        font-size: 18px;
      }
      .captcha{
        position:relative;
        margin-left:22%;
      }
      .captcha th{
        font-style: italic;
        vertical-align:top; 
        font-weight:normal;
      }
      .captcha td{
        padding-left:2%;
      }

      #verticalFrame{
        background: #1f61a8;
        background: -moz-linear-gradient(top, #3F83CC 2%, #1f61a8 100%);
        background: -webkit-gradient(linear, left top, left bottom, color-stop(2%,#3F83CC), color-stop(100%,#1f61a8));
        background: -webkit-linear-gradient(top, #3F83CC 2%,#1f61a8 100%);
        background: -o-linear-gradient(top, #3F83CC 2%,#1f61a8 100%);
        background: -ms-linear-gradient(top, #3F83CC 2%,#1f61a8 100%);
        background: linear-gradient(to bottom, #3F83CC 2%,#1f61a8 100%);
        box-shadow: 0 0 5px #333;
        width: 400px;
        height: 100%;
        position: absolute;
        top: 0;
        left: 50%;
        margin-left:-200px;
      }

      #loginContainer{
        font-family: tahoma, verdana, arial, sans-serif;
        color: #FAFAFA;
        margin-left:-52px;
        margin-top: 10%;
        padding-bottom:50px;
        width: 500px;
      }

      fieldset.detail{
        border:none;
        margin: 0 auto;
        padding-top: 20px;
      }

      p.error{
        margin-left: 50px;
      }

      #loginForm{
        background-color: #FAFAFA;
        color: #777;
        border: 2px solid #CCC;
        border-radius: 10px;
        margin-left:25px;
        margin-top: 40px;
        padding: 15px 0 15px 0;
        min-height: 160px;
        max-width: 450px;
        box-shadow: 0 0 5px: #000;
      }

      #loginForm label{
        font-size: 1.1em;
      }

      .arrow_box:after, .arrow_box:before {
        bottom: 100%;
        border: solid transparent;
        content: " ";
        height: 0;
        width: 0;
        position: absolute;
        pointer-events: none;
      }

      .arrow_box:after {
        border-color: rgba(255, 255, 255, 0);
        border-bottom-color: #FAFAFA;
        border-width: 20px;
        left: 50%;
        margin-left: -20px;
      }

      .arrow_box:before {
        border-color: rgba(221, 221, 221, 0);
        border-bottom-color: #DDD;
        border-width: 23px;
        left: 50%;
        margin-left: -23px;
      }

      #loginForm .error{
        background-color: #fafafa;
        text-align: center;
        padding-left: 30%;
      }

      #loginContainer fieldset+fieldset{
        margin-top: 20px;
        margin-bottom: 0;
        padding-bottom: 0;
      }
    </style>
  </head>
  <body>
    <div id="verticalFrame">
      <div id="loginContainer">
          <a href="http://www.obm.org"><img src="$ico_obm_org" alt="OBM $obm_version" /></a>
          <h2>Free communication becomes reality!</h2>
          <h1>$l_obm_title $obm_version</h1>
        <p class="error"></p>
        <form name="loginform" id="loginForm" method="post" action="$login_action" class="arrow_box">
          <fieldset class="detail">
            <legend class="error">$error</legend>
            <table>
            <tr>
              <th><label for="loginField">$l_login</label></th>
              <td><input id="loginField" type="text" name="login" value="$login" placeholder="$l_login"/></td>
            </tr>
            <tr>
              <th><label for="passwordField">$l_password</label></th>
              <td><input id="passwordField" type="password" name="password" value="" placeholder="$l_password"/></td>
            </tr>
            <tr>
              <th><label for="sel_domain_id">$domain</label></th>
              <td>$sel_domain</td>
            </tr>
            </table>
          </fieldset>
          $captcha_formadd
          <fieldset class="buttons">
            <input type="submit" value="$l_validate" />
          </fieldset>
        </form>
      </div>
    </div>
    $login_javascript_footer
  </body>
</html>
