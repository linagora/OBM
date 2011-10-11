#!/bin/sh

# Script qui permet de mettre a jour spamassassin.
#
# Auteur : Christophe Marteau
# Date de dernière modification : 13/11/2007
# Version : 1.0
#
# Realease notes :
# 13/11/2007 : - Version initiale

# Fonction qui affiche un message de debuggage 
# en fonction de la variable DEBUG
# [in] $1 : niveau du debuggage
# [in] $2 : Nom de fonction à debugger
# [in] $3 : Message de debuggage à afficher
function debug() {
  if [ ${DEBUG} -ge ${1} ] ; then
    echo "#DEBUG [${1}] : (${2}) ${3}"  
  fi
}

# Fonction qui affiche les options disponibles pour ce script et quitte
function usage() {
    debug "9" "usage" "BEGIN usage"
    echo ""
    echo "Script qui permet mettre a jour les regles de spamassassin."
    echo ""
    echo "usage : ${0} [--help] [--debug <level>] --config <fichier de configuration>"
    echo ""
    echo "--help          : Affiche cette aide."
    echo "--debug <level> : Mode de débugage [0..9]"
    echo "--config <file> : Specifie un fichier de configuration different de \"/etc/spamassassin/sa-update.cf\""
    debug "9" "usage" "END usage"
    exit 0
}

# Fonction qui analyse le fichier de configuration et renseigne les variables
# Options  possibles:
# - SpamAssassinHttpProxy
function parseConfig() {
	debug "9" "parseConfig" "BEGIN parseConfig"
	if [ ! -z "${SA_CONFIG_FILE}" ] ; then
		SA_HTTP_PROXY=$(grep "SpamAssassinHttpProxy" ${SA_CONFIG_FILE} | sed "s/SpamAssassinHttpProxy=\"\([^\"]*\)\";/\1/")
	  debug "2" "parseConfig" "SA_HTTP_PROXY=[${SA_HTTP_PROXY}]"
    SA_EXTRA_CHANNEL=$(grep "SpamAssassinExtraChannel" ${SA_CONFIG_FILE} | sed "s/SpamAssassinExtraChannel=\"\([^\"]*\)\";/\1/")
    debug "2" "parseConfig" "SA_EXTRA_CHANNEL=[${SA_EXTRA_CHANNEL}]"
	else
		echo "Impossible d'ouvrir le fichier \"${SA_CONFIG_FILE}\""
		exit 0
	fi
	debug "9" "parseConfig" "END parseConfig"
}

# Debut du programme principal
DEBUG=0
DATE="$(date "+%Y-%m-%d %H:%M:%S")"
LC_ALL="POSIX"
SA_CONFIG_FILE="/etc/spamassassin/sa-update.cf";
SA_DEBUG_FLAG="";

# Traitement des options
while [ $# -gt 0 ]; do
    # debuggage du menu
    echo "opt \"$#\" (${1}) = [${2}]"
    case "${1}" in
				"--debug" | "-d")
            if [ -z "$(echo ${2}|sed 's/^[0-9]\{0,1\}$//')" ]; then
                if [ -z ${2} ] ; then
                    DEBUG=1
                else
                    DEBUG=${2}
                fi
								SA_DEBUG_FLAG="-D"
            else
                echo "Le niveau de débugage souhaite n'existe pas"
                usage
            fi
            shift
            ;;
   	    "--config"|"-c")
						if [ ! -f ${2} ] ; then
	            echo "Le fichier de configuration \"${2}\" n'existe pas." 
            else
              SA_CONFIG_FILE=${2}
            fi
						shift
            ;;
        *)
            usage
            ;;
    esac
    shift
done    

debug "9" "main" "BEGIN main" 
parseConfig
if [[ "${SA_EXTRA_CHANNEL}" =~ '^[Yy][Ee][Ss]$' ]] ; then
	debug "9" "main" "http_proxy=$SA_HTTP_PROXY /usr/bin/sa-update ${SA_DEBUG_FLAG} --channel updates.spamassassin.org --allowplugins --allowplugins --gpgkey D1C035168C1EBC08464946DA258CDB3ABDE9DC10 --channel saupdates.openprotect.com"
  http_proxy=$SA_HTTP_PROXY /usr/bin/sa-update ${SA_DEBUG_FLAG} \
		--channel updates.spamassassin.org --allowplugins --allowplugins \
		--gpgkey D1C035168C1EBC08464946DA258CDB3ABDE9DC10 \
		--channel saupdates.openprotect.com
else 
	debug "9" "main" "http_proxy=$SA_HTTP_PROXY /usr/bin/sa-update ${SA_DEBUG_FLAG} --channel updates.spamassassin.org"
	http_proxy=$SA_HTTP_PROXY /usr/bin/sa-update ${SA_DEBUG_FLAG} \
		--channel updates.spamassassin.org
fi
debug "1" main "Restarting amavis"
invoke-rc.d amavis restart >/dev/null 2>/dev/null 
debug "9" "main" "END main"

exit $?
