## Introduction

This document describes the backup and restore feature inside OBM. It will first details the administration and configuration parts, then the end-user one.
It has three parts, the obm-ui part including administration and end-user interactions, then the scripting and command lines methods part to control it.

 * The backup creates an archived file which includes files for three entities related to a user of a domain :

| Entities | In the archive |
| -------- | -------------------  |
| Mailbox  | Every files of the cyrus mailbox |
| Contacts | Several VCF (one for each address book) files which contain every contacts |
| Calendar | A single ICS file which contains every events |

 * With the correct right on the backup module, you are able to restore the three entities separately or everything all at once.

## Conventions

This documentation is written considering we are on a squeeze distribution.

 * Some pathes of Debian differ from REHL ones. Here are the interesting ones for the document :

| Debian                      | REHL                   |
| --------------------------- | ---------------------- |
| /usr/share/obm/www/contrib/ | /usr/share/obm/contrib |
| /usr/share/obm/www/cron/    | /usr/share/obm/cron    |


 * Following are listed all other important pathes :

| Path |
| ------- |
| /var/lib/obm/backup |
| /var/log/obm-satellite/obmSatellite.log |
| /etc/obm-satellite/obmSatellite.ini |
| /etc/obm-satellite/mods-available/backupEntity |

 * Moreover, in this documentation, those variables are also included :

| Variable    | Description |
| ----------- | ----------------- |
| domain    | The name of the domain (vm.obm.org in the screenshots) |
| login     | The login of a OBM user |
| timestamp | The time  when the backup has been done |
| obm-satellite.host | The hostname or ip of the obm-satellite |
| end-point | The chosen end point (it may be availablebackup, restoreentity or backupentity) |

## Settings

By default, the backup module is enabled and all backups are written into **/var/lib/obm/backup/<domain>**.
The name of those files follows this pattern :

    user_-_<login>_-_<domain>_-_<timestamp>.tar.gz

 * The backup module can be enabled or disabled by adding or removing the symbolic link **/etc/obm-satellite/mods-enabled/backupEntity**. It is linked to the file **/etc/obm-satellite/mods-available/backupEntity**.

Inside this file, you can set the following variables :

| Name          | Description |
| ------------- | ----------------- |
| tmpDir        | Temporary directory used to prepare the archive |
| imapdConfFile | Cyrus imapd configuration file absolute path |
| backupRoot    | Default directory for backups storage |
| tarCmd        | Tar command absolute path |

**Note that backupRoot is overridden by the same key backupRoot written into /etc/obm/obm_conf.ini.**

 * Other settings can also be set into **/etc/obm/obm_conf.ini** including :

| Name               | Description |
| ------------------ | ----------------- |
| backupRoot         | Default directory for backups storage |
| admin_mail_address | Default email used for reports on errors while backuping any users |
| backupFtpTimeout   | Waiting timeout while uploading a file in the configured ftp |

 * At last, every logs are written into **/var/log/obm-satellite/obmSatellite.log**. You might want to set the level to debug by setting the key **log-level** into **/etc/obm-satellite/obmSatellite.ini** to 5.

## Manage it via OBM-ui

The first thing to do is to enable the Backup/Restore module for each user profiles which should have access to the Backup/Restore web page.

 * Connected to OBM as the global admin (**admin0** by default), access to the User profiles web page and click on the expected profile to edit :

<a href="/media/images/obm_-_profile.png" rel="lightbox[getstarted]"><img src="/media/images/obm_-_profile.png" width="400"/></a>

 * Then add the Backup/Restore module with needed rights by clicking on the **+** button :

<a href="/media/images/obm-backup_addmodule.png" rel="lightbox[getstarted]"><img src="/media/images/obm-backup_addmodule.png" width="400"/></a>

 * Rights on this module are described as below :

| Right          | Description |
| -------------- | ---------------- |
| Read           | The web page is accessible |
| Write          | The web page is accessible and you are able to restore a backup |
| Administration | The web page is accessible, you are able to create and to restore a backup |

### As an administrator

**Note that the Backup/Restore has to be done by an admin of a domain, not the global one**

With the administration right, you are able to manage backups for every OBM users for your domain.

 * Access to one of the Backup/Restore web page of one OBM user :

<a href="/media/images/obm-backup_adminmanage1.png" rel="lightbox[getstarted]"><img src="/media/images/obm-backup_adminmanage1.png" width="400"/></a>

 *  For the current user, the Backup/Restore web page is composed of the **Backup** button which launches a backup, **erasing if it exists the previous backup**, and of the list of available backups which can be restored :

<a href="/media/images/obm-backup_adminmanage2.png" rel="lightbox[getstarted]"><img src="/media/images/obm-backup_adminmanage2.png" width="400"/></a>

**Note that if a ftp is configured for this domain, the backup would be sent to this ftp.**

### As a user

#### With write right

A user with this right are only able to restore a backup.

<a href="/media/images/obm-backup_userread.png" rel="lightbox[getstarted]"><img src="/media/images/obm-backup_userread.png" width="400"/></a>

#### With read right

A user with this right are only able to list his own backup. There is no other interaction.

<a href="/media/images/obm-backup_useraccess.png" rel="lightbox[getstarted]"><img src="/media/images/obm-backup_useraccess.png" width="400"/></a>

## Manage it via command lines or http requests

To do http request on the satellite, OBM-ui uses basic HTTP Authentification. Credentials are written into UserSystem table in the database. The user used is **obmsatelliterequest**.

### OBM-satellite end points

Obm-satellite provides a few HTTP(S) end points which mostly manage backups and postfix stuff.
For our needs, let's details those related to the Backup/Restore feature.

 * Every end points have the following URL pattern :

        https://<obm-satellite.host>:30000/satellite/<end-point>/<parameters>...

#### End point: availablebackup

    https://127.0.0.1:30000/satellite/availablebackup/user/<login>@<domain>

This GET method lists every backups which can be restored for a user.

On success, it sends a XML document with this structure :

    <?xml version='1.0' encoding='UTF-8' standalone='yes'?>
    <obmSatellite module="backupEntity" status="OK">
      <backupFile>user_-_<login>_-_<domain>_-_<timestamp.tar.gz</backupFile>
    </obmSatellite>

| Tag        | Data Type | Description           | Occurrences |
| -----      | --------- | --------------------- | ----------- |
| backupFile | String    | The available backups | 0+          |


#### End point: backupentity

    https://127.0.0.1:30000/satellite/backupentity/user/<login>@<domain>

This PUT method backups a chosen user. It comes with a XML as request content like the following :

    <?xml version="1.0"?>
    <obmSatellite module="backupEntity">
      <options><report sendMail="true"/></options>
      <calendar></calendar>
      <privateContact>
        <addressBook name="collected_contacts"></addressBook>
        <addressBook name="contacts"></addressBook>
      </privateContact>
    </obmSatellite>

| Tag            | Data Type | Description           | Occurrences |
| -------------- | --------- | --------------------- | ----------- |
| options        |           | Includes all options  | 1 |
| report         |           | child of options: sendMail as attribute is true or false to send or not a report at the end of the backup | 1 |
| email          | String    | child of report: defines every emails which receive the report | 0+ |
| ftp            |           | child of options: push as attribute is true or false and defines if we force push or not in the ftp | 0+ |
| calendar       | String    | The exported ICS of the user's calendar in base64 | 1 |
| privateContact | String    | Includes all addressBook tag | 1 |
| addressBook    | String    | *name* attribute is the name of the address book the VCF file content in base64 | 2+ |

On success, it sends a XML document with this structure :

    <?xml version='1.0' encoding='UTF-8' standalone='yes'?>
    <obmSatellite module="backupEntity" status="OK">
      <content>Sauvegarde <login>@<domain> reussie</content>
      <pushFtp success="false">No backup FTP server linked to OBM domain '<domain>'</pushFtp>
    </obmSatellite>

| Tag            | Data Type | Description           | Occurrences |
| -------------- | --------- | --------------------- | ----------- |
| content        | String    | The status of the backup previously done. | 1 |
| pushFtp        | String    | Informs if the backup has been sent to a FTP server and the status of this action. | 1 |


#### End point: restoreentity

    https://127.0.0.1:30000/satellite/restoreentity/user/<login>@<domain>/(mailbox|contact|calendar)

This POST method restore the chosen entity for the chosen backup. It comes with a XML as request content like the following :

    <?xml version="1.0"?>
    <obmSatellite module="backupEntity">
        <options><report sendMail="true"/></options>
        <backupFile>user_-_boss_-_vm.obm.org_-_20130704-101510.tar.gz</backupFile>
    </obmSatellite>

| Tag        | Data Type | Description           | Occurrences |
| ---------- | --------- | --------------------- | ----------- |
| options    |           | Includes all options  | 1 |
| report     |           | sendMail is true or false to send or not a report at the end of the backup | 1 |
| email          | String    | child of report: define every email to send the report | 0+ |
| ftp            |           | child of options: push as attribute is true or false and defines if we force push or not in the ftp | 0+ |
| backupFile | String    | The backup to restore | 1 |

**Note that with no chosen entity as parameter in the URL, it will restore everything.**

On success, it sends a XML document with this structure :

    <?xml version='1.0' encoding='UTF-8' standalone='yes'?>
    <obmSatellite module="backupEntity" status="OK">
      <calendar></calendar>
      <content>Restoring user <login>@<domain> all data successfully</content>
      <mailbox></mailbox>
      <privateContact>
        <addressBook name="contacts"></addressBook>
        <addressBook name="collected_contacts"></addressBook>
      </privateContact>
    </obmSatellite>

| Tag            | Data Type | Description           | Occurrences |
| -------------- | --------- | --------------------- | ----------- |
| calendar       | String | The exported ICS of the user's calendar in base64 | 1 |
| privateContact | String | Includes all addressBook tag | 1 |
| addressBook    | String | *name* attribute is the name of the address book the VCF file content in base64 | 2+ |
| content        | String | The status of the restoration previously done. | 1 |

## Manage it via scripts

### The OBM cron feature

OBM comes with a cron system which launches various php cronjob. It also includes a **GlobalBackup.class.php** which can be used to do a scheduled backup of every users of every domains of an OBM plateform.

 * To enable it you have to copy **/usr/share/obm/www/contrib/cronjobs/GlobalBackup.class.php** ( on REHL) into **/usr/share/obm/www/cron/jobs**.

 * We are doing this way because by default, OBM runs every jobs included into **/usr/share/obm/www/cron/jobs**.

 * You may want to activate only chosen jobs. To do so, move **/usr/share/obm/www/cron/jobsToExecute.ini.sample** to **/usr/share/obm/www/cron/jobsToExecute.ini** and write needed jobs.

        jobs[]=SatelliteBackup.class.php

 * Inside **/usr/share/obm/www/contrib/cronjobs/GlobalBackup.class.php** you can set the schedule behavior of this cron :

<pre>
  function mustExecute($date) {
    $delta   = 24*60;         //every days
    $instant = (2*60)%$delta; //at 2:00
    $min = (int)($date/60);
    return ($min%$delta === $instant);
  }
</pre>

### Scripts examples

Finally, an OBM installation comes with other various shell scripts. Those scripts can be found in the directory **/ui/doc/sample/backup** of the root of obm git repository.

Those scripts are :

 * ManualGlobalBackup.class.php : this script can be used to manually launch a backup of every users on an OBM plateform.

Usage : 

    OPTIONS :
    --init-backup
       Create a /tmp/globalBackups.init file with a domain_id=nbUsers format.
    --backup --domain=x --offset=y --nbUsers=z --backend=b
       Do the backup on the domain x of z users, starting at the y one.
    --end-backup\n";
       Notify the satellite the end of the backup.

 * globalBackup.sh : this script, coupled with a crontab (globalBackup.cron in the same directory is an existing example) which invoke a global backup, can be used to organise the backups into 5 directories (daily, weekly, bi-weekly, monthly, bi-monthly).

Usage :

    OPTIONS :
    -init
       Create the needed 5 directories.
    -d
       Move the backups into the daily directory
    -w
       Move the backups into the weekly directories
    -m
       Move the backups into the monthy directories
    -mw
       Do a complete rotation


 * parallelBackups.sh : this script can be used with the ManualGlobalBackup.class.php to backup high storage size by parallelising the process.

Usage :

    OPTIONS :
    -v
       Verbose output
    -n
       Maximum number of child processes to use
