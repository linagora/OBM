Name: obm-jetty
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: configuration for Jetty for Open Business Management
Vendor: obm.org
URL: http://www.obm.org
Group: Development/Tools
License: GPLv2
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: jetty.xml.sample
Source1: jetty-logging.xml.sample


BuildArch:      noarch
Requires: jetty6

%description
It allows Jetty Server to start after its install and changes the port.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%install
mkdir -p $RPM_BUILD_ROOT%{_docdir}/obm-jetty
install -p -m 755 %{SOURCE0} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty.xml.sample
install -p -m 755 %{SOURCE1} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty-logging.xml.sample

%files
%defattr(-,root,root,-)
%{_docdir}/obm-jetty/jetty.xml.sample
%{_docdir}/obm-jetty/jetty-logging.xml.sample

%post
/etc/init.d/jetty6 stop > /dev/null 2>&1 || :
if [ -e /etc/jetty6/jetty.xml ] && [ `diff /usr/share/doc/obm-jetty/jetty.xml.sample /etc/jetty6/jetty.xml` -ne 0 ]; then
	cp /etc/jetty6/jetty.xml /etc/jetty6/jetty.xml.orig
fi
cp /usr/share/doc/obm-jetty/jetty.xml.sample /etc/jetty6/jetty.xml

if [ -e /etc/jetty6/jetty-logging.xml ] && [ `diff /usr/share/doc/obm-jetty/jetty-logging.xml.sample /etc/jetty6/jetty-logging.xml` ]; then
	cp /etc/jetty6/jetty-logging.xml /etc/jetty6/jetty-logging.xml.orig
fi
cp /usr/share/doc/obm-jetty/jetty-logging.xml.sample /etc/jetty6/jetty-logging.xml
if [ `grep -F /etc/jetty6/jetty-logging.xml /etc/jetty6/jetty-logging.xml` -ne 0 ]; then
    echo /etc/jetty6/jetty-logging.xml >> /etc/jetty6/jetty-logging.xml
fi
/etc/init.d/jetty6 start > /dev/null 2>&1 || :
