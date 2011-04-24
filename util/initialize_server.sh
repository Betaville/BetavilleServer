#!/bin/sh
# initialize_server.sh
# Creates a blank database with some essential parameters so that an instance
# of Betaville can be created
# by Skye Book <skye.book@gmail.com>
# Released under GPL v3, which can be found at http://www.gnu.org/licenses/gpl-3.0.html

# prompt the user for, and collect, database connection info
echo "Database user (needs privileges to create a database):"
read dbUser
echo "Database password:"
stty -echo
read dbPass
stty echo

# get account information
echo "Pick a name for your administrator account for Betaville:"
read adminUser
echo "Please provide your email as a means of recovery (and optional notifications):"
read adminEmail
echo "Pick a password:"
stty -echo
read adminPass
echo "Confirm password:"
read adminPassConfirm
stty echo

if [ "$adminPass" != "$adminPassConfirm" ]; then
	echo "\n"
	echo "+-----------------ERROR-----------------+"
	echo "|The passwords you entered do not match!|"
	echo "|Please run the script again            |"
	echo "+---------------------------------------+"
	echo "\n"
	exit 2
fi

# get city info
echo "\n"
echo "Ok, we need a city to start in!  Please provide the country"
read country
country="\"$country\""
echo "And now the state or similar administrative district"
read state
state="\"$state\""
echo "And finally the city or similar locality"
read city
city="\"$city\""

command -v mysql >/dev/null && echo "mysql located" || echo "mysql not found, are you sure it is on your \$PATH?"
command -v mysqladmin >/dev/null && echo "mysqladmin located" || echo "mysqladmin not found, are you sure it is on your \$PATH?"

dbName=betaville

# create the empty database
mysqladmin -u $dbUser -p$dbPass create $dbName

# generate the database tables
mysql -u $dbUser -p$dbPass $dbName < ../sql/betaville.sql


# This is irritating, we need to move all of the jars
mkdir ../deploy
cp ../lib/*.jar ../deploy/
cp ../lib/javamail-1.4.3/mail.jar ../deploy/mail.jar
cp ../lib/javamail-1.4.3/lib/*.jar ../deploy
cp ../lib/MySQL/*.jar ../deploy
mv ../Betaville*.jar ../deploy
mv ../PopulateDatabase.jar ../deploy


cd ../deploy
echo "working from `pwd`"
java -Djava.library.path=`pwd` -cp `pwd` -jar PopulateDatabase.jar edu.poly.bxmc.betaville.server.util.PopulateDatabase -u $dbUser -p$dbPass -adminuser $adminUser -adminpass$adminPass -adminmail $adminEmail -city $city -state $state -country $country

# set the user to administrator (this will be built into the Java database functionality soon and should be migrated over)
mysql --user=$dbUser --password=$dbPass --database=$dbName -e "UPDATE user SET type = 'admin' WHERE userName LIKE '$adminUser'"

echo "Success!"
echo "To run the Betaville server, run the command 'java -jar BetavilleServer.jar' in the BetavilleServer/deploy directory"
