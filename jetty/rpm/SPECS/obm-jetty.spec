# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

# Define obm-jetty configuration
%global obmjettyconf %{_sysconfdir}/jetty6

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
Source2: jetty-default
BuildArch: noarch
Requires(post): jetty6

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
install -p -m 644 %{SOURCE2} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty-default

%files
%defattr(-,root,root,-)
%{_docdir}/obm-jetty/jetty.xml.sample
%{_docdir}/obm-jetty/jetty-logging.xml.sample
%{_docdir}/obm-jetty/jetty-default

%post
for f in jetty.xml jetty-logging.xml ; do
  if [ -e %{obmjettyconf}/${f} ] ; then
    if [ %{obmjettyconf}/${f} -nt %{_docdir}/obm-jetty/${f}.sample ] ; then
      cp -p %{obmjettyconf}/${f} %{obmjettyconf}/${f}.orig
    fi
  fi
  cp -p %{_docdir}/obm-jetty/${f}.sample %{obmjettyconf}/${f}
done

cp -p %{_docdir}/obm-jetty/jetty-default %{_sysconfdir}/default/jetty6

service jetty6 restart > /dev/null 2>&1 || :
