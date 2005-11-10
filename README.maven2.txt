
Port SearchFront to maven2

In order to port to m2 the following steps are required:

*  Plugins
Upodate sesam maven repository or local repository with, at least, wsdl2java plugin. 
There is no official port for this yet.

Install wsdl in local repo: 
svn checkout svn://svn.codehaus.org/mojo/scm/trunk/ mojo-site
Compile wsdl2java typing "mvn install"

* Config 
Source should be moved to src/main/java, src/main/wsdl and so forth instead of
src/java, src/wsdl ...

Change directory into src/main/conf and execute ./genm2tabs.sh  and edit 
configuration.properties. Edit log4j.properties and change file paths.


Execute mvn war:war ;-)

