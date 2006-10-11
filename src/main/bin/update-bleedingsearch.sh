#!/bin/bash
#
# Author: mick
# $Id$
#


# Check we're on correct machine
#
if ! (hostname | grep -q sch-login01); then
        echo "This script can only be executed on sch-http01.dev"
        rm -f update-bleedingsearch.lck
        exit 1
fi

# lock process so it cannot be run multiple times from different users
#
cd /www/schibstedsok
lockfile -l 900 update-bleedingsearch.lck

#  Update update-bleedingsearch.sh
#
rm -f update-bleedingsearch.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/update-bleedingsearch.sh -O update-bleedingsearch.tmp
if (diff -q update-bleedingsearch.sh update-bleedingsearch.tmp) ; then
    mv update-bleedingsearch.tmp update-bleedingsearch.sh
    echo "Updated myself. Did not update bleedingsearch. Please run again."
    rm -f update-bleedingsearch.lck
    exit 1
fi
rm -f update-bleedingsearch.tmp

#  Update bleedingsearch-projects.sh
#
rm -f bleedingsearch-projects.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/bleedingsearch-projects.sh -O bleedingsearch-projects.tmp
if (diff -q bleedingsearch-projects.sh bleedingsearch-projects.tmp) ; then
    mv update-bleedingsearch.tmp update-bleedingsearch.sh
    echo "Updated bleedingsearch-projects.sh"
fi
rm -f update-bleedingsearch.tmp

#
# Start Update
#

# XXX parametise which project list we load
sh bleedingsearch-projects.sh

cnt=${#PROJECTS[*]}

# For each project
#
for (( i = 0 ; i < cnt ; i++ )) ; do

    # Find the latest version (eg trunk build)
    #
    for f in /www/schibstedsok/data/builds/${PROJECTS[$i]}/* ; do
            if [ -d $f ] ; then
                    versionDir=$f
            fi
    done

    # Find the latest build in the version
    #
    for f in $versionDir/*.war ; do
            warFile=$f
    done

    # Check that it's not already being used and copy it to tomcat's deploy directory
    #
    while ! diff $warFile /www/schibstedsok/deploy/bleedingsearch/tomcat/deploy/search/${CONTEXTS[$i]} >/dev/null ; do
            echo "cp $warFile   ${CONTEXTS[$i]}"
            cp $warFile /www/schibstedsok/deploy/bleedingsearch/tomcat/deploy/search/${CONTEXTS[$i]}
    done

    # Keep permissions on it friendly
    #
    chmod g+w /www/schibstedsok/deploy/bleedingsearch/tomcat/deploy/search/${CONTEXTS[$i]} 2>/dev/null

done

# Update our version.txt accordingly
#
date +"%Y%m%d%H%M" > bleedingsearch.schibstedsok.no/version.txt
date >> bleedingsearch.schibstedsok.no/version.txt
whoami >> bleedingsearch.schibstedsok.no/version.txt
chmod g+w version.txt 2>/dev/null

# Clean lock
#
rm -f update-bleedingsearch.lck
