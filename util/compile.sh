#!/bin/sh
# compile.sh
# Compiles the BetavilleServer project as well as the BetavilleApp project, which needs to be
# in the same directory as BetavilleServer.
# Example Project Structure:
#	+Betaville_Sources
# 	|-BetavilleApp
# 	|-BetavilleServer
# by Skye Book <skye.book@gmail.com>
# Released under GPL v3, which can be found at http://www.gnu.org/licenses/gpl-3.0.html

# Build the server jars
cd ../
ant build build jar pop-db-jar

# Build the application jars
cd ../BetavilleApp
ant build jar