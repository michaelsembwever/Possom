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
Https://dev.schibstedsok.no/schibstedsok-ca.pem in your Java keystore. It can be
done after downloading the pem-file using this command:

$ $JAVA_HOME/bin/keytool -import -alias schibstedsokca -file schibstedsok-ca.pem \
-keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit

