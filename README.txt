=======================================
Build your own federated search engine
=======================================

Possom is an open sourced Search Middleware with federation capabilities and a built-in search portal framework. Possom enables a single user query to be dispatched to multiple information sources. These results can be analysed, weighted, federated, and clustered 
before being presented to the user according to declarative business rules.

    Possom is the core technology used to power http://sesam.no and http://sesam.se, which are scandinavian search, news and directory sites that utilise a large number of data sources including Yahoo!, PicSearch, Solr (Lucene), Youtube, and enterprise search 
systems from FAST.

    Possom makes it easy to build applications that look for information in many different places simultaneously. Possom can connect to almost any kind of data source that can be accessed using Java - databases, search indexes, files, back office systems, web 
services, ESBs. Similar product examples are FAST Unity, WebFeat, DeepWeb Explorit, dbWIZ, Carrot2, and Raritan SIFT.

    Possom takes care of all the complex tasks of communicating with multiple search indexes simultaneously, query- and result analysis, business rules application; leaving the developers to focus on other aspects of their application, such as presentation and 
usability.

    Possom is developed in an open environment and released under the Lesser General Public License. We invite you to participate in this open development project.


More information about Possom can be found at
http://sesat.no

Possom is licensed under GNU's Lesser General Public License version 3 (or later).
See LICENSE.txt for full license.

=======================================
Getting Started
=======================================

http://sesat.no/development-guidelines.html



=======================================
Beginners Tutorial
=======================================

http://sesat.no/tutorial-building-sesamcom.html




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
More information
=======================================

Possom - SEsam Search Application Toolkit - is a stack of software applications aiming to help efficient creation of feature-rich, stable and multi-source based search portals and search applications. Based on experience and learnings from the first installations 
of Sesam.no and Sesam.se, Possom is now an extensive toolkit with one primary focus: reduce development complexity and dramatically improve quality and delivery time. In fact, Possom lets customers create fully-functional portals in a few days rather than many 
months, as proved by the teams behind Sesam.no and Sesam.se, all using Possom to speed up site and site search development.

In general, writing a search application from scratch given only one or more indexes providing search results as data objects or XML, the following steps are usual:

    Create an application where the user types a search query
    Create a search function for each of the data sources to be searched in, tailored to the index profile / index data model
    The application queries each of the data sources in turn
    Display the results to the user

But, this is not at all flexible or scalable, with code that needs to be rewritten and tailored to any change in the underlying data source models. A much better approach would be to:

    Define a dynamic data model for each of the indexes to be searched in
    Implement either a parallel or a sequential search function that takes the search string and queries the different data sources, collects the data and delivers it to a templating engine. For such a function to be stable, it should
        Be able to analyse the search query to predict which indexes are worth searching in
        Handle faulty indexes
        Handle slow response time together with increased traffic, preventing resource starvation
        Handle the different APIs for the different indexes
        Have a model for unifying/normalising the search result
        Be robust when the index model / index profile changes (there should be no need to alter the search function)
    Implement a template engine that gets data from the search function and display it to the user
    Implement a logger that can register all searches with details about the user client, search context, zero-hits, index response time etc.
    Implement some sort of rules engine that triggers on particular terms in the search query, to intelligently boost which data source should be displayed first
    Allow for the application to present search results differently depending on IP-range, user session id, personalisation settings.
    Design templates to display the result

With Possom, you get all of the above by

    Set up your development environment,
    Define your data sources in a configuration file, and
    Hack the example templates that is included.

