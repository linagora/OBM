Name:           obm-cert-ca
Version:        %{obm_version}
Release:        %{obm_release}%{?dist}
Summary:        Generate SSL for OBM 
Group:          Development/Tools
License:        GPLv2
Vendor:		www.obm.org
BuildArch:      noarch
# Source obm-ssl-ca
Source0:    obm-ca-%{version}.tar.gz
Source1:    obm-cert.sh
# fin source obm-ssl-ca

# Source obm-ssl-cert
# fin source obm-ssl-cert

BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
OBM certificate SSL

%package	-n obm-ca
Summary:	Genrerate CA and CS
Group:		Development/Tools
Requires:	openssl

%description 	-n obm-ca
OBM CA management scripts
 Script to manage a certification authority within obm

%package	-n obm-cert
Summary:	OBM certificat
Group:		Development/Tools
Requires:	obm-ca
Requires:	obm-config
Requires:	mod_ssl

%description	-n obm-cert
OBM certificates
Certicate generation for OBM
Installation of OBM

%prep
%setup -q -n obm-ca-%{version} 

%build

%install
rm -rf $RPM_BUILD_ROOT

# Construction de l'arbo virtuel
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-ca
mkdir -p $RPM_BUILD_ROOT%{_docdir}/obm-ca-%{version}


# install obm-ssl-ca
# Installation des fichiers dans l'arbo virtuel
install -p -m 755 createcert.sh $RPM_BUILD_ROOT%{_datadir}/obm-ca/createcert.sh
install -p -m 755 buildca.sh $RPM_BUILD_ROOT%{_datadir}/obm-ca/buildca.sh
install -p -m 644 camgr.lib $RPM_BUILD_ROOT%{_datadir}/obm-ca/camgr.lib
install -p -m 644 ca.cnf $RPM_BUILD_ROOT%{_datadir}/obm-ca/ca.cnf
install -p -m 644 cert.cnf.template $RPM_BUILD_ROOT%{_datadir}/obm-ca/cert.cnf.template
install -p -m 644 README $RPM_BUILD_ROOT%{_docdir}/obm-ca-%{version}/README

# install obm-ssl-cert
install -p -m 755 %{SOURCE1} $RPM_BUILD_ROOT%{_bindir}/obm-cert

%pre

%post -n obm-ca
if [ $1 = 1 ]; then
	/usr/share/obm-ca/buildca.sh
fi

%preun

%postun  -n obm-ca
if [ $1 = 0 ]; then
	rm -rf /var/lib/obm-ca
fi

%files -n obm-ca
%defattr(-,root,root,-)
%{_datadir}/obm-ca/createcert.sh
%{_datadir}/obm-ca/buildca.sh
%{_datadir}/obm-ca/camgr.lib
%{_datadir}/obm-ca/ca.cnf
%{_datadir}/obm-ca/cert.cnf.template
%{_docdir}/obm-ca-%{version}/README

%files -n obm-cert
%defattr(-,root,root,-)
%{_bindir}/obm-cert



%changelog
* Sun Apr 19 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.1-1
- first stable rpm release
* Thu Feb 25 2009 Ronan Lanore <ronan.lanore[at]obm.org>
+ obm-ssl-1.0-1
- Create

