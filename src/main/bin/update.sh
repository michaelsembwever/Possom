#!/bin/bash
#
# Author: mick
# $Id$
#

# internal functions
#

update_file() {
    rm -f $UPDATE_FILE.tmp
    wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/$UPDATE_FILE.sh -q -O $UPDATE_FILE.tmp >/dev/null
    DIFF=`diff -q $UPDATE_FILE.sh $UPDATE_FILE.tmp`
    if [[ "x" != "x$DIFF" ]] ; then
        cp $UPDATE_FILE.tmp $UPDATE_FILE.sh
        rm $UPDATE_FILE.tmp
        chmod u+x $UPDATE_FILE.sh
        chmod g+x $UPDATE_FILE.sh
        chmod u+w $UPDATE_FILE.sh
        chmod g+w $UPDATE_FILE.sh
        echo "Updated $UPDATE_FILE.sh"
    fi
    rm -f $UPDATE_FILE.tmp
    unset DIFF
    unset UPDATE_FILE
}


# spaghetti code follows...

# Check we're on correct machine
#
if ! (hostname | grep -q sch-login01); then
        echo "This script can only be executed on sch-login01.dev. Get Back To Where You Came From."
        exit 1
fi

# lock process so it cannot be run multiple times from different users
#  we'll also be able to see who it is, though ownership of the lockfile.
#
cd /www/schibstedsok
lockfile -l 900 update.lck

# which test environment to update
#
if [ "x" == "x$1" ] ; then
    RIG="alpha"
else
    RIG="$1"
fi
export $RIG

case "$RIG" in
	alpha|beta|gamma|nuclei|electron|photon|production)
        ;;
    *)
        echo "This test environment ($RIG) does not exist."
        exit 1
        ;;
esac

case "$RIG" in
	alpha|nuclei)
        PROJECT_SCRIPT=test-projects
        BRANCH_SCRIPT=test-branch
        DEPLOY_PARTITION=schibstedsok
        ;;
	beta|electron)
        PROJECT_SCRIPT=production-projects
        BRANCH_SCRIPT=production-branch
        DEPLOY_PARTITION=schibstedsok
        ;;
	gamma|photon|production)
        PROJECT_SCRIPT=production-projects
        BRANCH_SCRIPT=production-branch
        DEPLOY_PARTITION=schibstedsok-prod
        ;;
esac

case "$RIG" in
	alpha|beta|gamma|nuclei|electron|photon)
        RIG_DOMAIN=${RIG}.test.sesam
        ;;
    production)
        RIG_DOMAIN=sesam
        exit 1
        ;;
esac
export RIG_DOMAIN

#  Update update-test.sh
#
rm -f update.tmp
wget https://dev.schibstedsok.no/svn/search-portal/trunk/src/main/bin/update.sh -q -O update.tmp >/dev/null
DIFF=`diff -q update.sh update.tmp`
if [[ "x" != "x$DIFF" ]] ; then
    cp update.tmp update.sh
    rm update.tmp
    chmod u+x update.sh
    chmod g+x update.sh
    chmod u+w update.sh
    chmod g+w update.sh
    echo "Updated myself. Did not update ${RIG_DOMAIN}. Please run again."
    rm -f update.lck
    exit 1
fi
rm -f update.tmp
unset DIFF

#  Update $PROJECT_SCRIPT.sh
#
UPDATE_FILE=$PROJECT_SCRIPT
update_file


#  Update $BRANCH_SCRIPT.sh
#
UPDATE_FILE=$BRANCH_SCRIPT
update_file


#
# Start Update
#

# XXX parametise which project list we load
. $PROJECT_SCRIPT.sh
. $BRANCH_SCRIPT.sh

cnt=${#PROJECTS[*]}

# For each project
#
for (( i = 0 ; i < cnt ; i++ )) ; do

    if [[ "x" != "x${PROJECTS[$i]}" ]] ; then
        # Find the latest build in the version
        #
        for f in /www/${DEPLOY_PARTITION}/data/$RIG-builds/${PROJECTS[$i]}/${VERSION}/*.war ; do
            warFile=$f
        done

        # Check that it's not already being used and copy it to tomcat's deploy directory
        #
        if ! diff $warFile /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/${CONTEXTS[$i]} >/dev/null ; then
            echo "cp $warFile   ${CONTEXTS[$i]}"
            cp $warFile /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/${CONTEXTS[$i]}
        fi

        # Keep permissions on it friendly
        #
        chmod g+w /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/${CONTEXTS[$i]} 2>/dev/null

    fi
done

# Update our version.txt accordingly
#
date +"%Y%m%d%H%M" > /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/version.txt
date >> /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/version.txt
whoami >> /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/version.txt
echo ${VERSION} >> /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/version.txt
chmod g+w /www/${DEPLOY_PARTITION}/${RIG_DOMAIN}.no/version.txt 2>/dev/null

# Clean lock
#
rm -f update.lck
