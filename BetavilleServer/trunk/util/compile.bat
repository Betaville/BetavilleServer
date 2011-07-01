REM compile.sh
REM Compiles the Betaville Server software for use on a system
REM requires Subversion and Apache Ant to be installed (in addition to Java, of course)
REM Subversion for Windows can be found here: http://sourceforge.net/projects/win32svn/
REM Apache Ant can be downloaded from http://ant.apache.org
REM Instructions for setting ANT up can be found here: http://dita-ot.sourceforge.net/doc/ot-userguide13/xhtml/installing/windows_installingant.html
REM by Skye Book <skye.book@gmail.com>
REM Released under GPL v3, which can be found at http://www.gnu.org/licenses/gpl-3.0.html

chdir ../../BetavilleApp
ant build jar
move BetavilleApp.jar ../BetavilleServer/

chdir ../BetavilleServer
ant build jar pop-db-jar

md deploy
copy lib/*.jar deploy/
copy lib/javamail-1.4.3/mail.jar deploy/mail.jar
copy lib/javamail-1.4.3/lib/*.jar deploy
copy lib/MySQL/*.jar deploy
copy util/run.sh deploy/run.sh
move Betaville*.jar deploy
move PopulateDatabase.jar deploy
