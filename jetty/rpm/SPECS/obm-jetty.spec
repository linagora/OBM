# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name: obm-jetty
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: configuration for Jetty for Open Business Management
Vendor: obm.org
URL: http://www.obm.org
Group: Development/Tools
License: GPLv2+
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: jetty.xml.sample
Source1: jetty-logging.xml.sample

#Clear previous jetty6 from jpackage.org provided by obm.org
Provides: jetty6 = 6.1.14-2
Obsoletes: jetty6 <= 6.1.14-2

BuildArch: noarch
Requires(post): jetty



%description
It allows Jetty Server to start after its install and changes the port.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%install
mkdir -p $RPM_BUILD_ROOT%{_docdir}/obm-jetty
install -p -m 644 %{SOURCE0} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty.xml.sample
install -p -m 644 %{SOURCE1} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty-logging.xml.sample

%files
%defattr(-,root,root,-)
%{_docdir}/obm-jetty/jetty.xml.sample
%{_docdir}/obm-jetty/jetty-logging.xml.sample

%post
service jetty stop > /dev/null 2>&1 || :
if [ -e %{_sysconfdir}/jetty/jetty.xml ] && [ `diff %{_docdir}/obm-jetty/jetty.xml.sample %{_sysconfdir}/jetty/jetty.xml` -ne 0 ]; then
	cp %{_sysconfdir}/jetty/jetty.xml %{_sysconfdir}/jetty/jetty.xml.orig
fi
cp %{_docdir}/obm-jetty/jetty.xml.sample %{_sysconfdir}/jetty/jetty.xml

if [ -e %{_sysconfdir}/jetty/jetty-logging.xml ] && [ `diff %{_docdir}/obm-jetty/jetty-logging.xml.sample %{_sysconfdir}/jetty/jetty-logging.xml` ]; then
	cp %{_sysconfdir}/jetty/jetty-logging.xml %{_sysconfdir}/jetty/jetty-logging.xml.orig
fi
cp %{_docdir}/obm-jetty/jetty-logging.xml.sample %{_sysconfdir}/jetty/jetty-logging.xml
if [ `grep -F %{_sysconfdir}/jetty/jetty-logging.xml %{_sysconfdir}/jetty/jetty-logging.xml` -ne 0 ]; then
    echo %{_sysconfdir}/jetty/jetty-logging.xml >> %{_sysconfdir}/jetty/jetty-logging.xml
fi
service jetty start > /dev/null 2>&1 || :
