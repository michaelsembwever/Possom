=======================================
About SESAT
=======================================

More information about SESAT can be found at
http://sesat.no
http://sesam.no/omoss

SESAT is licensed under GNU's Affero General Public License version 3 (or later).
See LICENSE.txt for full license.

=======================================
Getting Started
=======================================

http://sesat.no/development-guidelines.html




=======================================
Compiling crash: java.lang.NullPointerException at com.sun.tools.javac.comp.Check.checkCompatibleConcretes(..)
=======================================
When maven is building data-model-javabean-impl it fails with the javac crash:

[INFO] Compilation failure
Failure executing javac, but could not parse the error:
An exception has occurred in the compiler ...
java.lang.NullPointerException at com.sun.tools.javac.comp.Check.checkCompatibleConcretes(..)

try checking out sesat-kernel again with a new name (eg sesat-kernel1) and try building again. repeat until it works.
Or use Java7.
It's a known bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6218229 related to inodes.

=======================================
Windows (sucks)
=======================================

 * search-portal/core-api doesn't build if the current path contains spaces.
	fault from http://mojo.codehaus.org/axistools-maven-plugin/
