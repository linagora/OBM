#!/bin/bash

echo > compileLastCommits.results

BRANCH=$(git rev-parse --abbrev-ref HEAD)
COUNT_TO_BUILD=${1:-2}

for rev in $(git rev-list --reverse --max-count=$COUNT_TO_BUILD HEAD); do
    git checkout $rev
    mvn clean package -T4C > "compileLastCommits.logs.$rev"
    echo "Result for revision $rev is $?" >> compileLastCommits.results
done

git checkout $BRANCH
