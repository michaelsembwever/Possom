=======================================
Certificates
=======================================

To use the https-based repository you will also have to import
https://dev.schibstedsok.no/schibstedsok-ca.pem in your Java keystore. It can be
done after downloading the pem-file using this command:

$ $JAVA_HOME/bin/keytool -import -alias schibstedsokca -file schibstedsok-ca.pem \
-keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit


=======================================
Maven 2
=======================================

h2. Port SearchFront to maven2

External resource:
[http://mojo.codehaus.org/]  Mojo Codehous
[http://maven.apache.org/]  Maven Apache Org

[http://maven.apache.org/maven1.html#m1-or-m2] Port to maven2 ? 
[http://maven.apache.org/using/bestpractices.html] Best Practises
[http://maven.apache.org/maven1.html]  Maven 1 Users

In order to port search-front-html to m2 the following steps are required:

h3. Pros

* Runs (much)faster than maven1

* Dependency handling and inheritance	

* Plugins can be written in java
  Means its easy for us to write plugins:-)

h3. Installing (not released yet) plugins 
The best way is to deploy plugins like wsdl2java to dev.schiibstedsok.no,  
but heres the manual way of doing it:

Get Mojo plugins with svn(Subversion client)
{noformat}
	svn checkout svn://svn.codehaus.org/mojo/scm/trunk/ mojo-site
{noformat}
Compile wsdl2java by typing  *mvn install*, this will install it in your local repo.

h3. Config  and sourcelocation
Convert parameters from maven1: \@paramname\@  to maven1 format $\{paramname\}

Move source from src/java,src/wsdl to src/main/java, src/main/wsdl and so forth
(or symlink for testing)

Change directory into src/main/conf and execute ./genm2tabs.sh  and edit 
configuration.properties. Edit log4j.properties and change file paths.

h4.  Execute mvn war:war 
	Good luck! ;-)
