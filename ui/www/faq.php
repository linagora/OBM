<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- $Id$ -->
<html>
<head>
<title>OBM FAQ</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="style01.css" rel="stylesheet" type="text/css">
</head>

<body>
<?php include("menu.html"); ?>

<div class="center"> 
  <h1>OBM FAQ</h1>
  <p>
  <ul>
    <li><a href="#cant_log">I can't log in to OBM, I always return to the login page</a>
    <li><a href="#no_text">I can log in but no text are displayed (in tabs, menus...)</a>
    <li><a href="#doc_size">I can't insert document of size over 2 or 4 Mb</a>
    <li><a href="#user_pref">OBM seems to work but each search lead to a mysql error</a>
  </ul>
  </p>

<br />

<a name="no_text">
<h2>I can't log in to OBM, I always return to the login page</h2>

Check that javascript is enabled in your browser.
If so, check that your php is configured with (in php.ini) :
<pre>
register_globals = On
</pre>


<a name="no_text">
<h2>I can log in but no text are displayed (in tabs, menus...)</h2>

Check that your OBM_INCLUDE_VAR is correctly set (a common mistake is to give it the full path to your obminclude directory). This must be filled with the obminclude directory name ('obminclude' by default).
Your apache virtual host conf should have a line like this :
<pre>
SetEnv OBM_INCLUDE_VAR obminclude
</pre>


<a name="doc_size">
<h2>I can't insert document of size over 2 or 4 Mb</h2>

Check and asjust these values in your php.ini file :
<pre>
post_max_size = 16M
memory_limit = 16M
upload_max_filesize = 16M
</pre>

On some distrib (eg: Redhat) check the php configuration for apache (conf.d/php.conf)

<pre>
LimitRequestBody 16000000
</pre>

<a name="user_pref">
<h2>OBM seems to work but each search lead to a mysql error</h2>

Each search lead to this result :
<pre>
Database error: seek(0) failed: result has 0 rows
MySQL Error: 0 ()
Session halted
</pre>

This occurs when User preferences have not been propagated.
Insert the defaut preferences (see install doc) and then propagate these preferences to all users :
<br />
In OBM, from the ADMINISTRATION section (must be connected with an admin user), go to the <b>Prefs</b> (or Préférences) module and execute the action <b>user_pref_update</b> (which reload defaut preferences for each user)

<p />

<?php include("footer.html"); ?>

</div>

</body>
</html>
