#!/bin/bash

RUNDIR=$(dirname $(readlink -f $0))

awk '/type\[[0-1]\] =/ { print $3 }' $RUNDIR/../pony/*java | cut -f2 -d'.' | tr -d ';' | sort | uniq -c | sort -gr
