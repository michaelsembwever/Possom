#!/bin/bash
#
# Author: mick
# $Id$
#


# Check we're on correct machine
#
if ! (hostname | grep -q sch-login01); then
        echo "This script can only be executed on sch-login01.dev"
        exit 1
fi

# lock process so it cannot be run multiple times from different users
#
cd /www/schibstedsok
lockfile -l 900 update-searchdev.lck

#  Update update-searchdev.sh
#
rm -f update-searchdev.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/update-searchdev.sh -q -O update-searchdev.tmp >/dev/null
DIFF=`diff -q update-searchdev.sh update-searchdev.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    mv update-searchdev.tmp update-searchdev.sh
    chmod u+x update-searchdev.sh
    chmod g+x update-searchdev.sh
    echo "Updated myself. Did not update searchdev. Please run again."
    rm -f update-searchdev.lck
    exit 1
fi
rm -f update-searchdev.tmp
unset DIFF


#  Update production-projects.sh
#
rm -f production-projects.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/production-projects.sh -q -O production-projects.tmp >/dev/null
DIFF=`diff -q production-projects.sh production-projects.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    mv production-projects.tmp production-projects.sh
    chmod u+x production-projects.sh
    chmod g+x production-projects.sh
    echo "Updated production-projects.sh"
fi
rm -f production-projects.tmp
unset DIFF


#  Update production-branch.sh
#
rm -f production-branch.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/production-branch.sh -q -O production-branch.tmp >/dev/null
DIFF=`diff -q production-branch.sh production-branch.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    mv production-branch.tmp production-branch.sh
    chmod u+x production-branch.sh
    chmod g+x production-branch.sh
    echo "Updated production-branch.sh"
fi
rm -f production-branch.tmp
unset DIFF



#
# Start Update
#

# XXX parametise which project list we load
. production-projects.sh
. production-branch.sh

cnt=${#PROJECTS[*]}

# For each project
#
for (( i = 0; i < cnt ; i++ )) ; do

    # Find the latest build in the version
    #
    for f in /www/schibstedsok/data/searchdev-builds/${PROJECTS[$i]}/${PRODUCTION_VERSION}/*.war ; do
            warFile=$f
    done

    # Check that it's not already being used and copy it to tomcat's deploy directory
    #
    deployFile=`echo ${CONTEXTS[$i]} | sed 's/sesam\./searchdev\.schibstedsok\./g'`
    while ! diff $warFile /www/schibstedsok/deploy/searchdev/tomcat/deploy/search/$deployFile >/dev/null ; do
            echo "cp $warFile   $deployFile"
            cp $warFile /www/schibstedsok/deploy/searchdev/tomcat/deploy/search/$deployFile
    done

    # Keep permissions on it friendly
    #
    chmod g+w /www/schibstedsok/deploy/searchdev/tomcat/deploy/search/$deployFile 2>/dev/null

done

# Update our version.txt accordingly
#
date +"%Y%m%d%H%M" > /www/schibstedsok/searchdev.schibstedsok.no/version.txt
date >> /www/schibstedsok/searchdev.schibstedsok.no/version.txt
whoami >> /www/schibstedsok/searchdev.schibstedsok.no/version.txt
chmod g+w version.txt 2>/dev/null


# Clean lock
#
rm -f update-searchdev.lck
