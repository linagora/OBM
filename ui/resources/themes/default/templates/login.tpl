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
        background:url(../images/structure/pattern.png) top left repeat #FFF;
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
    </style>
  </head>
  <body>
    <p id="linagora">
    <a href="http://www.obm.org">OBM.org</a>
    </p>
    <h1>$l_obm_title $obm_version</h1>
    <img src="$img_home" alt="OBM $obm_version" />
    <p class="error"></p>
    <form name="loginform" id="loginForm" method="post" action="$login_action">
      <fieldset class="detail">
        <legend class="error" >$error</legend>
        <table>
        <tr>
          <th><label for="loginField">$l_login</label></th>
          <td><input id="loginField" type="text" name="login" value="$login" /></td>
        </tr>
        <tr>
          <th><label for="passwordField">$l_password</label></th>
          <td><input id="passwordField" type="password" name="password" value="" /></td>
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
    $login_javascript_footer  
  </body>
</html>
