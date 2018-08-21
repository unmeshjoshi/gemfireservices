#get_default_ip() {
# default_ip="$(route | grep '^default' | grep -o '[^ ]*$' |xargs -n 1 ifconfig |grep 'inet'| cut -d: -f2| awk '{ print $2}')"
# echo "Using default ip ${default_ip}"
#}
#
#stop_server() {
#   local server_name=$1
#   $GEMFIRE_HOME/bin/gfsh stop server --name=$server_name
#}
#
#stop_server "server1"
#stop_server "server2"
#stop_server "server3"
ps ax | grep -i 'LocatorLauncher' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
ps ax | grep -i 'ServerLauncher' | grep java | grep -v grep | awk '{print $1}' | xargs kill -9
