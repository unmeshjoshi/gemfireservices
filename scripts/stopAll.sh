ps ax | grep -i 'LocatorLauncher' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'ServerLauncher' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
