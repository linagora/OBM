<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- $Id$ -->
<html>
<head>
<title>About OBM</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="style01.css" rel="stylesheet" type="text/css">
</head>

<body>
<?php include("menu.html"); ?>

<div class="center">

  <h1>About OBM</h1>
  <h2> OBM</h2>
    O.B.M. stands for Open Business Management.
    <p />
    OBM is an Intranet application which goal is to help manage a company or an organization.

    As OBM improve, it is approaching what are called CRM (with sales force
    , help desk, time tracking sections)
    but can be used simply as a <b>contact database</b> or as a <b>shared calendar</b>.
    <p />
    OBM represents a framework above wich many modules are written. 
    It is written by an evolving group of developpers from Aliacom (mains
    are Mehdi Rande, Pierre Carlier, Olivier Boyer (design)) lead by Pierre
    Baudracco, with contributions by other 
    people (see the changelog) and released under the terms of the GPL. 
    <p />
    OBM is always evolving and the data and the database model are subject
    to change but each new version come with update scripts. 
    </p>
    <h2>Licence</h2>
    <p>OBM is protected by the GNU General Public License v2 (See file COPYING). 
    </p>
    <h2>Requirements</h2>
    <p>OBM is a web application with full XHTML / CSS output. As so it works with Mozilla >= 1.0, IE and Netscape 7. OBM is developped under Linux, Apache, PHP, PHPlib, MySQL or PostgreSQL.
      <br />
    </p>

    <h3 align="center">OBM has been tested on these versions : </h3>
    <table width="90%" border="0" align="center" cellpadding="2" cellspacing="0" class="table2">
      <tr bgcolor="#75BCC1"> 
        <td width="15%"> 
          <div align="center"><strong>Software</strong></div></td>
        <td width="44%"> 
          <div align="center"><strong>Version</strong></div></td>
        <td width="41%"> 
          <div align="center"><strong>Comment</strong></div></td>
      </tr>
      <tr> 
        <td bgcolor="#E2F1F3">Linux</td>
        <td bgcolor="#E2F1F3">2.0.31 - 2.4.26, 2.6.x </td>
        <td bgcolor="#E2F1F3">OBM shouldn't be related to kernel and OS
          version directly (should run on every UNIX)</td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td>Apache</td>
        <td>1.3.3 - 1.3.27; 2.0.40 - </td>
        <td></td>
      </tr>
      <tr bgcolor="#E2F1F3"> 
        <td>PHP</td>
        <td>OBM <b>0.3, 0.4</b> : 3.0.5 - 3.0.16; 4.0.1 pl2 - 4.1.x
          <br />OBM <b>0.5.x, 0.6.x</b> : 4.0.x - 4.3.x
          <br />OBM <b>0.7.x</b> : 4.1.x - 4.3.x
          <br />OBM <b>0.8.x</b> : 4.2.x - 4.3.x
        </td>
        <td>pg_query() need PHP >= 4.2.0</td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td>PHPlib</td>
        <td>Customized version</td>
        <td>included (but modified) in OBM
          </td>
      </tr>
      <tr bgcolor="#E2F1F3"> 
        <td>MySQL</td>
        <td>3.23.x; 4.0.x </td>
        <td>&nbsp;</td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td>Postgres</td>
        <td>7.1.x, 7.2.x, 7.3.x, 7.4.x</td>
        <td>Was supported in 0.3.0
          <br>
          Postgres support back since 0.8.0</td>
      </tr>
    </table>
    <p>&nbsp; </p>

    <h3 align="center">There are reports of OBM running on </h3>
    <table width="90%" border="0" align="center" cellpadding="2" cellspacing="0" class="table2">
      <tr bgcolor="#75BCC1"> 
        <td width="62%"><div align="center"><strong>Platform</strong></div></td>
        <td width="24%"><div align="center"><strong>Reporter</strong></div></td>
        <td width="14%"><div align="center"><strong>Comment</strong></div></td>
      </tr>
      <tr bgcolor="#E2F1F3"> 
        <td>NT with Apache</td>
        <td>Raymond Gardner</td>
        <td>&nbsp;</td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td><br>
          FreeBSD 4.1, Apache 1.3.12, Mysql 3.23, PHP 3.0.16.</td>
        <td>Ryan Fox</td>
        <td>OBM 0.4 </td>
      </tr>
    </table>

    <div align="center"></div>
    <p /><br>
      All OBM versions support MySQL, and PostgreSQL is supported since 0.8.0.
      Support for other databases should be not too hard.
    </p>

  <ul>
    <li> 
      <div align="left"> Apache : <a href="http://www.apache.org" target="_blank">www.apache.org</a> 
      </div>
    </li>
    <li> 
      <div align="left">PHP : <a href="http://www.php.net" target="_blank">www.php.net</a> 
      </div>
    </li>
    <li> 
      <div align="left">MySQL : <a href="http://www.mysql.org" target="_blank">www.mysql.org</a> 
      </div>
    </li>
    <li> 
      <div align="left">PostgreSQL : <a href="http://www.postgresql.org" target="_parent">www.postgresql.org</a> 
      </div>
    </li>
    <li>
      <div align="left">Phplib : <a href="http://phplib.netuse.de" target="_blank">phplib.netuse.de</a> 
      </div>
    </li>
  </ul>

<?php include("footer.html"); ?>

</div>


</body>
</html>
