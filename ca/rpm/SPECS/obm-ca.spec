# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name:           obm-cert-ca
Version:        %{obm_version}
Release:        %{obm_release}%{?dist}
Summary:        Generate SSL for OBM 
Group:          Development/Tools
License:        AGPLv3
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
Summary:	certification authority management for Open Business Management
Group:		Development/Tools
Requires:	openssl

%description 	-n obm-ca
This package contains scripts to handle SSL certificates for OBM.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	-n obm-cert
Summary:	configuration script for obm-ca
Group:		Development/Tools
Requires:	obm-ca
Requires:	obm-config
Requires:	mod_ssl

%description	-n obm-cert
This package contains a configuration script for obm-ca

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

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
* Wed Jul 10 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.1-rc4
- New upstream release.
* Thu Jul 04 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.1-rc3
- New upstream release.
* Fri Jun 21 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.1-rc2
- New upstream release.
* Wed Jun 19 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.1-rc1
- New upstream release.
* Tue Apr 30 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.0-
- New upstream release.
* Mon Apr 29 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.0-0.rc3
- New upstream release.
* Fri Apr 26 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.0-0.rc2
- New upstream release.
* Tue Apr 23 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.0-0.rc1
- New upstream release.
* Fri Jan 18 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.5.0.0-0.alpha0
- New upstream release.
* Thu Jan 17 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta7
- New upstream release.
* Wed Dec 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta6
- New upstream release.
* Tue Dec 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta5
- New upstream release.
* Mon Dec 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta4
- New upstream release.
* Tue Nov 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta3
- New upstream release.
* Wed Nov 07 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta2
- New upstream release.
* Mon Nov 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.beta1
- New upstream release.
* Tue Oct 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.alpha3
- New upstream release.
* Mon Oct 15 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.alpha2
- New upstream release.
* Wed Sep 26 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.2.0-0.alpha1
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1.1-0.rc2
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1.1-0.rc1
- New upstream release.
* Wed Sep 12 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1.1-0.beta1
- New upstream release.
* Mon Sep 03 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1.0-1
- New upstream release.
* Fri Aug 31 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-
- New upstream release.
* Thu Jul 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-beta2
- New upstream release.
* Mon Jul 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-beta2
- New upstream release.
* Fri Jul 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-beta1
- New upstream release.
* Tue Jun 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha11
- New upstream release.
* Mon May 21 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha10
- New upstream release.
* Fri Apr 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha9
- New upstream release.
* Thu Apr 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha8
- New upstream release.
* Tue Apr 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha7
- New upstream release.
* Thu Apr 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha6
- New upstream release.
* Fri Mar 02 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha5
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha4
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha3
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha2
- New upstream release.
* Fri Jan 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.1-alpha1
- New upstream release.
* Wed Jan 04 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.0.0-rc16
- New upstream release.
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-ca-2.4.0.0-rc14
- New upstream release.
* Sun Apr 19 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.1-1
- first stable rpm release
* Thu Feb 25 2009 Ronan Lanore <ronan.lanore[at]obm.org>
+ obm-ssl-1.0-1
- Create

