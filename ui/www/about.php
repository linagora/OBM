<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- $Id$ -->
<html>
<head>
<title>Document sans titre</title>
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
    OBM is an Intranet application which goal is to help manage a company.
    (in practice our own company, Aliacom, but other uses are reported).
    As OBM improve, it is approaching what are called CRM (with sales force
    , help desk, time tracking sections)
    but can be used simply as a contact database or as a shared calendar.
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
    <p>OBM is developped under Linux, Apache, PHP, PHPlib, MySQL or PostgreSQL.
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
        <td bgcolor="#E2F1F3">2.0.31 - 2.4.22 </td>
        <td bgcolor="#E2F1F3">OBM shouldn't be related to kernel and OS
          version directly (should run on every UNIX)</td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td>Apache</td>
        <td>1.3.3 - 1.3.27; 2.0.40 - </td>
        <td>Not tested on 1.2 but should work with</td>
      </tr>
      <tr bgcolor="#E2F1F3"> 
        <td>PHP</td>
        <td>OBM <b>0.3, 0.4</b> : 3.0.5 - 3.0.16; 4.0.1 pl2 - 4.1.x
          <br />OBM <b>0.5.x</b> : 4.0.x - 4.3.x (PHP3 no more supported)
          <br />OBM <b>0.6.x</b> : 4.0.x - 4.3.x
          <br />OBM <b>0.7.0</b> : 4.1.x - 4.3.x
        </td>
        <td>One or two &quot;;&quot; to delete with 4.0.x (for OBM 0.3)
          but that's all </td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td>PHPlib</td>
        <td>7 - 7.2</td>
        <td>Parts used are included (but modified) in OBM
          </td>
      </tr>
      <tr bgcolor="#E2F1F3"> 
        <td>MySQL</td>
        <td>3.22.8 - 3.22.32; 3.23.x; 4.0.x </td>
        <td>&nbsp;</td>
      </tr>
      <tr bgcolor="#F1F9FA"> 
        <td>Postgres</td>
        <td>7.1.x, 7.2.x, 7.3.x</td>
        <td>Is supported since 0.3.0 <br>
          (Version prior to 7.1 (6.5.3, 7.0.x) were supported till OBM 0.5.1) 
          <br>
          (Postgres creation scripts are lacking for now in recent releases)</td>
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
      All OBM versions support MySQL, and PostgreSQL is supported since 0.3.0. 
      Multidatabase support is done throught Phplib, and all database interactions are done in specific files (module_query.inc) so it should be easy 
      now to add support for other databases. </p>
     <p />Phplib dependance will be removed in the future when the enhanced
     session and authentification layers are completed and a new database
     abstraction layer has been chosen.
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
