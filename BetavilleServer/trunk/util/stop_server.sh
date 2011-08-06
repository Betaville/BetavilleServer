#!/bin/sh
# A simple script for turning off the Betaville server software
# Copyright 2011 Brooklyn eXperimental Media Center
# Released under BSD 3-Clause license
# author: Skye Book <skye.book@gmail.com>

PID=$(ps aux | egrep [B]etavilleServer | awk '{print $ 2}')
kill $PID
