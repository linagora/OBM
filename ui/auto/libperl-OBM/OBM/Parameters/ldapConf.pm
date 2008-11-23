package OBM::Parameters::ldapConf;

require Exporter;

use OBM::Parameters::common;

@ISA = qw(Exporter);
@EXPORT_const = qw( $ldapAdminLogin $NODE $ROOT $DOMAINROOT $POSIXUSERS $POSIXGROUPS $SYSTEMUSERS $DOMAINHOSTS $SAMBADOMAIN $SAMBAFREEUNIXID $SAMBAUSERS $SAMBAGROUPS $SAMBAHOSTS $MAILSERVER $MAILSHARE $CONTACTS);
@EXPORT_struct = qw($attributeDef $ldapStruct);
@EXPORT = (@EXPORT_const, @EXPORT_struct);
@EXPORT_OK = qw();

#
# Necessaire pour le bon fonctionnement du package
$debug=1;


#
# delaration de variables
#
# Le login de l'administrateur LDAP
$ldapAdminLogin = "ldapadmin";


#
# Declaration des type de donnees LDAP
#
# Type de noeuds possibles
$NODE = "organizationalUnit";
$ROOT = "rootLdap";
$DOMAINROOT = "domainRootLdap";
#
# Type de donnees possibles
$POSIXUSERS = "posixUsers";
$POSIXGROUPS = "posixGroups";
$SYSTEMUSERS = "systemUsers";
$DOMAINHOSTS = "domainHost";
$SAMBADOMAIN = "sambaDomain";
$SAMBAFREEUNIXID = "sambaFreeUnixId";
$SAMBAUSERS = "sambaUsers";
$SAMBAGROUPS = "sambaGroups";
$SAMBAHOSTS = "sambaHosts";
$MAILSERVER = "mailServer";
$MAILSHARE = "mailShare";
$CONTACTS = "publicContact";

#
# Déclaration des attributs des différents types
$attributeDef = {
    $ROOT => {
        structural => 1,
        is_branch => 1,
        dn_prefix => "dc",
        dn_value => "name",
        objectclass => [ "dcObject", "organization" ],
    },

    $DOMAINROOT => {
        structural => 1,
        is_branch => 1,
        dn_prefix => "dc",
        dn_value => "domain_name",
        objectclass => [ "dcObject", "organization" ],
    },

    $NODE => {
        structural => 1,
        is_branch => 1,
        dn_prefix => "ou",
        dn_value => "name",
        objectclass => [ "organizationalUnit" ],
    },

    $POSIXUSERS => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "uid",
        dn_value => "userobm_login",
        objectclass => [ "posixAccount", "shadowAccount", "inetOrgPerson", "obmUser", "sambaSamAccount" ],
    },

    $SYSTEMUSERS => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "uid",
        dn_value => "user_login",
        objectclass => [ "person", "posixAccount", "obmSystemUser" ],
    },

    $DOMAINHOSTS => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "cn",
        dn_value => "host_name",
        objectclass => [ "device", "ipHost", "obmHost", "sambaSamAccount" ]
    },

    $POSIXGROUPS => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "cn",
        dn_value => "group_name",
        objectclass => [ "posixGroup", "obmGroup", "sambaGroupMapping" ]
    },

    $MAILSHARE => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "cn",
        dn_value => "mailshare_name",
        objectclass => [ "obmMailShare" ],
    },

    $SAMBADOMAIN => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "sambaDomainName",
        dn_value => "sambaConf_domain_name",
        objectclass => [ "sambaDomain", "obmSamba" ],
    },

    $SAMBAUSERS => {
        structural => 0,
        is_branch => 0,
        dn_prefix => "uid",
        dn_value => "user_login",
        objectclass => [ "sambaSamAccount" ],
    },

    $SAMBAGROUPS => {
        structural => 0,
        is_branch => 0,
        dn_prefix => "cn",
        dn_value => "group_name",
        objectclass => [ "sambaGroupMapping" ],
    },

    $SAMBAHOSTS => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "uid",
        dn_value => "host_login",
        objectclass => [ "person", "sambaSamAccount", "obmSamba" ],
    },

    $MAILSERVER => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "cn",
        dn_value => "postfixconf_name",
        objectclass => [ "obmMailServer" ],
    },
    
    $CONTACTS => {
        structural => 1,
        is_branch => 0,
        dn_prefix => "uid",
        objectclass => [ "inetOrgPerson" ]
    }

};


#
# Déclaration de la structure

#
# Création de la racine de l'arbre LDAP d'OBM-Ldap
my @rootNode = split( /,/, $ldapRoot );
my $currentNode;

# Création de la racine
for( my $i=$#rootNode; $i>=0; $i-- ) {
    # Création du noeud courant
    my $newNode = {
        dn => "",
        name => "$rootNode[$i]",
        node_type => "$ROOT",
        description => "Racine de l'annuaire",
        data_type => [],
        template => [],
        branch => [],
        ldap_server => {
            server => "",
            login => "",
            passwd => "",
            conn => ""
        }
    };

    if( !defined($ldapStruct) ) {
        $ldapStruct = $newNode;
        $currentNode = $ldapStruct;
    }else {
        push( @{$currentNode->{"branch"}}, $newNode );
        $currentNode = $currentNode->{"branch"}->[0];
    }
}


# Déclaration de la branche des utilisateurs systèmes globaux
my $newNode = {
    dn => "",
    name => "sysusers",
    node_type => "$NODE",
    description => "System users",
    data_type => [ $SYSTEMUSERS ],
    template => [],
    branch => []
};

push( @{$currentNode->{"branch"}}, $newNode );

# Déclaration de la branche des hotes
$newNode = {
    dn => "",
    name => "hosts",
    node_type => "$NODE",
    description => "Hosts description",
    data_type => [ $DOMAINHOSTS ],
    template => [],
    branch => []
};

push( @{$currentNode->{"branch"}}, $newNode );


# Déclaration de la branche des racines des domaines.

# Ce type de branche sera copier autant de fois qu'il y a de domaines à traiter.
# Chaque branche contient les informations d'un domaine.
# Le noeud de type DOMAINROOT peut contenir des informations d'authentifications
# spécifiques à un serveur LDAP (cf. noeud type ROOT).
$newNode = {
    dn => "",
    name => "",
    node_type => "$DOMAINROOT",
    description => "Racine du domaine",
    data_type => [],
    template => [],
    branch => []
};

push( @{$currentNode->{"template"}}, $newNode );
$currentNode = $currentNode->{"template"}->[0];

if( $obmModules->{"samba"} ) {
    push( @{$currentNode->{"data_type"}}, $SAMBADOMAIN );
}

# Déclaration de la branche des utilisateurs
my $userDesc = {
    dn => "",
    name => "users",
    node_type => "$NODE",
    description => "Users account",
    data_type => [ $POSIXUSERS ],
    template => [],
    branch => []
};

if( $obmModules->{"samba"} ) {
    push( @{$userDesc->{"data_type"}}, $SAMBAUSERS );
}

push( @{$currentNode->{"branch"}}, $userDesc );

# Déclaration de la branche des hotes
my $domainHostsDesc = {
    dn => "",
    name => "hosts",
    node_type => "$NODE",
    description => "Hosts description",
    data_type => [ $DOMAINHOSTS ],
    template => [],
    branch => []
};

if( $obmModules->{"samba"} ) {
    push( @{$domainHostsDesc->{"data_type"}}, $SAMBAHOSTS );
}

push( @{$currentNode->{"branch"}}, $domainHostsDesc );

# Branche contenant la déclaration des utilisateurs systèmes
my $systemUsersDesc = {
    dn => "",
    name => "sysusers",
    node_type => "$NODE",
    description => "System users",
    data_type => [ $SYSTEMUSERS ],
    template => [],
    branch => []
};

push( @{$currentNode->{"branch"}}, $systemUsersDesc );

# Banche contenant la déclaration des groupes
my $groupDesc = {
    dn => "",
    name => "groups",
    node_type => "$NODE",
    description => "System Groups",
    data_type => [ $POSIXGROUPS ],
    template => [],
    branch => []
};

if( $obmModules->{"samba"} ) {
    push( @{$groupDesc->{"data_type"}}, $SAMBAGROUPS );
}

push( @{$currentNode->{"branch"}}, $groupDesc );


if( $obmModules->{"mail"} ) {
    # Banche contenant la déclaration des répertoires partagés
    my $mailShareDesc = {
        dn => "",
        name => "mailShare",
        node_type => "$NODE",
        description => "Share Directory",
        data_type => [ $MAILSHARE ],
        template => [],
        branch => []
    };

    push( @{$currentNode->{"branch"}}, $mailShareDesc );
}

if( $obmModules->{"mail"} ) {
    # Banche contenant la déclaration de la configuration des services
    my $postConf = {
        dn => "",
        name => "servicesConfiguration",
        node_type => "$NODE",
        description => "Services configuration",
        data_type => [ $MAILSERVER ],
        template => [],
        branch => []
    };

    push( @{$currentNode->{"branch"}}, $postConf );
}

if( $obmModules->{"contact"} ) {
    # Branche contenant la déclaration des contacts publics OBM
    my $contacts = {
        dn => "",
        name => "contacts",
        node_type => "$NODE",
        description => "Publics contacts",
        data_type => [ $CONTACTS ],
        template => [],
        branch => []
    };

    push(  @{$currentNode->{"branch"}}, $contacts );
}

return 1;
