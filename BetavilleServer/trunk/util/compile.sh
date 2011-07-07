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

# Build the application jars
cd ../../BetavilleApp
ant build jar
mv BetavilleApp.jar ../BetavilleServer

# Build the server jars
cd ../BetavilleServer
ant build build jar pop-db-jar

# Make run.sh executable
chmod +x util/run.sh

# This is irritating, we need to move all of the jars
mkdir deploy
cp lib/*.jar deploy/
cp lib/javamail-1.4.3/mail.jar deploy/mail.jar
cp lib/javamail-1.4.3/lib/*.jar deploy
cp lib/MySQL/*.jar deploy
cp util/run.sh deploy/run.sh
mv Betaville*.jar deploy
mv PopulateDatabase.jar deploy
