
h2. Port SearchFront to maven2

External resource:
[http://mojo.codehaus.org/]  Mojo Codehous
[http://maven.apache.org/]  Maven Apache Org

[http://maven.apache.org/maven1.html#m1-or-m2] Port to maven2 ? 
[http://maven.apache.org/using/bestpractices.html] Best Practises
[http://maven.apache.org/maven1.html]  Maven 1 Users

In order to port search-front-html to m2 the following steps are required:

# Plugins
Upodate sesam maven repository or local repository with, at least, wsdl2java plugin. 
There is no official port for this yet.

Get more plugins (Subversion)
{noformat}
	svn checkout svn://svn.codehaus.org/mojo/scm/trunk/ mojo-site
{noformat}
Compile wsdl2java by typing  *mvn install*

# Config 
Move source from src/java,src/wsdl to src/main/java, src/main/wsdl and so forth
(or symlink for testing)

Change directory into src/main/conf and execute ./genm2tabs.sh  and edit 
configuration.properties. Edit log4j.properties and change file paths.

# Execute mvn war:war ;-)

