#!/bin/bash


wait_tcp_port() {
    local host="$1" port="$2"

    while ! nc -vz $host $port; do
        echo "waiting for port $host $port"
        sleep 1 # wait for 1/10 of the second before check again
    done
}

get_default_ip() {
 default_ip="$(route | grep '^default' | grep -o '[^ ]*$' |xargs -n 1 ifconfig |grep 'inet addr:'| cut -d: -f2| awk '{ print $1}')"
 echo "Using default ip ${default_ip}"
}

start_locator() {
   local locator_name=$1 locator_port=$2
   $GEMFIRE_HOME/bin/gfsh start locator --name=$locator_name --port=$locator_port --mcast-port=0 --locators="${default_ip}[$locator_port]" --classpath=${target_dir}/functions-assembly-0.1-SNAPSHOT.jar
   wait_tcp_port $default_ip $locator_port
}

start_server() {
   local server_name=$1 server_port=$2 http_port=$3
   $GEMFIRE_HOME/bin/gfsh start server --name=$server_name --locators="${default_ip}[9009],${default_ip}[9010]" --server-port=$server_port --J=-Dgemfire.http-port=$http_port --classpath=${target_dir}/functions-assembly-0.1-SNAPSHOT.jar
   wait_tcp_port $default_ip $server_port
}

create_region() {
 local region_name=$1 region_type=$2
 $GEMFIRE_HOME/bin/gfsh -e "connect --locator=${default_ip}[9009]" -e "create region --name=$region_name --type=$region_type"
}

configure_pdx_read_serialized() {
 $GEMFIRE_HOME/bin/gfsh -e "connect --locator=${default_ip}[9009]" -e "configure pdx --read-serialized=true"
}

get_default_ip

parent_dir=$(dirname $(pwd))
target_dir="${parent_dir}/functions/target/scala-2.11"
echo $(dir $target_dir)


start_locator "locator1" 9009

start_locator "locator2" 9010

configure_pdx_read_serialized

start_server "server1" 40404 8081
start_server "server2" 40405 8083
start_server "server3" 40406 8084

create_region "Positions" "REPLICATE"
create_region "FxRates" "REPLICATE"

tail -f ./server1/server1.log ./server2/server2.log  ./server3/server3.log ./locator1/locator1.log ./locator2/locator2.log
