# Base de données: `obm`
# --------------------------------------------------------

#
# Structure de la table `Contrat`
#

CREATE TABLE Contrat (
  contrat_id int(8) NOT NULL auto_increment,
  contrat_timeupdate timestamp(14) NOT NULL,
  contrat_timecreate timestamp(14) NOT NULL,
  contrat_userupdate int(8) default NULL,
  contrat_usercreate int(8) default NULL,
  contrat_label varchar(40) default NULL,
  contrat_company_id int(8) default NULL,
  contrat_numero varchar(20) default NULL,
  contrat_clause text,
  contrat_debut date default NULL,
  contrat_expiration date default NULL,
  contrat_type_id int(8) default NULL,
  contrat_comment text,
  contrat_responsable_client_id int(8) default NULL,
  contrat_responsable_client2_id int(8) default NULL,
  contrat_responsable_tech_id int(8) default NULL,
  contrat_responsable_com_id int(8) default NULL,
  contrat_typedeal int(8) default NULL,
  PRIMARY KEY  (contrat_id)
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Structure de la table `ContratDisplay`
#

CREATE TABLE ContratDisplay (
  display_user_id int(8) NOT NULL default '0',
  display_fieldname varchar(40) NOT NULL default '',
  display_fieldorder tinyint(3) unsigned default NULL,
  display_display tinyint(1) unsigned NOT NULL default '1'
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Structure de la table `ContratType`
#

CREATE TABLE ContratType (
  contrattype_id int(8) NOT NULL auto_increment,
  contrattype_timeupdate timestamp(14) NOT NULL,
  contrattype_timecreate timestamp(14) NOT NULL,
  contrattype_userupdate int(8) default NULL,
  contrattype_usercreate int(8) default NULL,
  contrattype_label varchar(40) default NULL,
  PRIMARY KEY  (contrattype_id)
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Structure de la table `Incident`
#

CREATE TABLE Incident (
  incident_id int(8) NOT NULL auto_increment,
  incident_contrat_id int(8) default NULL,
  incident_timeupdate timestamp(14) NOT NULL,
  incident_timecreate timestamp(14) NOT NULL,
  incident_userupdate int(8) default NULL,
  incident_usercreate int(8) default NULL,
  incident_date date default NULL,
  incident_description text,
  incident_priority enum('REDHOT','HOT','NORMAL','LOW') default NULL,
  incident_etat enum('OPEN','CALL','WAITCALL','PAUSED','CLOSED') default NULL,
  incident_logger int(8) default NULL,
  incident_owner int(8) default NULL,
  incident_resolution text,
  PRIMARY KEY  (incident_id)
) TYPE=MyISAM;

    