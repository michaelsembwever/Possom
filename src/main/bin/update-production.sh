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
lockfile -l 900 update-production.lck

#  Update update-production.sh
#
rm -f update-production.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/update-production.sh -q -O update-production.tmp >/dev/null
DIFF=`diff -q update-production.sh update-production.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    mv update-production.tmp update-production.sh
    chmod u+x update-production.sh
    chmod g+x update-production.sh
    echo "Updated myself. Did not update production. Please run again."
    rm -f update-production.lck
    exit 1
fi
rm -f update-production.tmp
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
    for f in /www/schibstedsok/data/production-builds/${PROJECTS[$i]}/${PRODUCTION_VERSION}/*.war ; do
            warFile=$f
    done

    # Check that it's not already being used and copy it to tomcat's deploy directory
    #
    while ! diff $warFile /www/schibstedsok-prod/www.sesam.no/${CONTEXTS[$i]} ; do
            echo "cp $warFile   ${CONTEXTS[$i]}"
            cp $warFile /www/schibstedsok-prod/www.sesam.no/${CONTEXTS[$i]}
    done

    # Keep permissions on it friendly
    #
    chmod g+w /www/schibstedsok-prod/www.sesam.no/${CONTEXTS[$i]} 2>/dev/null
    #base= `echo ${SVNPROJECTS[$i]} | sed s/branches\/version-.*\//tags\//`
    #/www/schibstedsok/home/mickw/svn/svn cp -m "update-production.sh" https://dev.schibstedsok.no/svn/${SVNPROJECTS[$i]} https://dev.schibstedsok.no/svn/${base}/~release~`date +"%Y%m%d%H%M"`

done

# Update our version.txt accordingly
#
date +"%Y%m%d%H%M" > /www/schibstedsok-prod/www.sesam.no/version.txt
date >> /www/schibstedsok-prod/www.sesam.no/version.txt
whoami >> /www/schibstedsok-prod/www.sesam.no/version.txt
chmod g+w version.txt 2>/dev/null


# Clean lock
#
rm -f update-production.lck
