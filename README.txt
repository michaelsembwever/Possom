=======================================
About SESAT
=======================================

More information about SESAT can be found at
http://sesat.no
http://dev.schibstedsok.no
http://sesam.no/omoss

SESAT is licensed under GNU's Affero General Public License version 3 (or later).
See LICENSE.txt for full license.

=======================================
Getting Started
=======================================

https://dev.schibstedsok.no/confluence/display/TECHDEV/Setting+up+Sesam

=======================================
Advanced Operations
=======================================

https://dev.schibstedsok.no/confluence/display/TECHDEV/Operations+Documentation+-+Search+Portal

=======================================
Certificates
=======================================

To use the https-based repository you will also have to import
https://dev.schibstedsok.no/schibstedsok-ca.pem in your Java keystore. It can be
done after downloading the pem-file using this command:

$ $JAVA_HOME/bin/keytool -import -alias schibstedsokca -file schibstedsok-ca.pem \
-keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit


=======================================
Windows (sucks)
=======================================

 * search-portal/core-api doesn't build if the current path contains spaces.
	fault from http://mojo.codehaus.org/axistools-maven-plugin/
