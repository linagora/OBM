<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"> 
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-15" />
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
      #aliasource {
        margin-top:0;
        margin-bottom:0;
        width: 100%;
      }
    </style>
  </head>
  <body>
    <p id="aliasource">
    <a href="http://www.obm.org">OBM.org</a>
    </p>
    <h1>$l_obm_title $obm_version</h1>
    <img src="$img_home" alt="OBM $obm_version" />
    <p class="error"></p>
    <form name="login" method="post" action="$login_action">
      <fieldset class="detail">
        <legend class="error" >$error</legend>
        <table>
        <tr>
          <th>$l_login </th>
          <td><input type="text" name="login" value="$login" /></td>
        </tr>
        <tr>
          <th>$l_password </th>
          <td><input type="password" name="password" value="" /></td>
        </tr>
        <tr>
          <th>$domain </th>
          <td>$sel_domain</td>
        </tr>        
        </table>
      </fieldset>
      <fieldset class="buttons">
        <input type="submit" value="$l_validate" />
      </fieldset>
    </form>
    $login_javascript_footer  
  </body>
</html>
