#!/bin/bash


wait_tcp_port() {
    local host="$1" port="$2"

    while ! nc -vz $host $port; do
        echo "waiting for port $host $port"
        sleep 1 # wait for 1/10 of the second before check again
    done
}

get_default_ip() {
# default_ip="$(route | grep '^default' | grep -o '[^ ]*$' |xargs -n 1 ifconfig |grep 'inet'| cut -d: -f2| awk '{ print $2}')"
 default_ip="127.0.0.1"
 echo "Using default ip ${default_ip}"
}

start_locator() {
   local locator_name=$1 locator_port=$2
   $GEMFIRE_HOME/bin/gfsh start locator --properties-file=gemfire.properties --name=$locator_name --port=$locator_port --mcast-port=0 --locators="${default_ip}[$locator_port]" --classpath=${target_dir}/functions-assembly-0.1-SNAPSHOT.jar
   wait_tcp_port $default_ip $locator_port
}

start_server() {
   local server_name=$1 server_port=$2 http_port=$3
   $GEMFIRE_HOME/bin/gfsh start server --properties-file=gemfire.properties --user=test --password=test --name=$server_name --cache-xml-file=cache.xml --locators="${default_ip}[9009],${default_ip}[9010]" --server-port=$server_port --J=-Dgemfire.QueryService.allowUntrustedMethodInvocation=true --J=-Dgemfire.http-port=$http_port --classpath=${target_dir}/functions-assembly-0.1-SNAPSHOT.jar
   wait_tcp_port $default_ip $server_port
}

deploy_functions() {
$GEMFIRE_HOME/bin/gfsh -e "connect --user=test --password=test --locator=${default_ip}[9009]" -e "deploy --jar=${target_dir}/functions-assembly-0.1-SNAPSHOT.jar"
}

create_partitioned_region() {
 local region_name=$1 region_type=$2
 $GEMFIRE_HOME/bin/gfsh -e "connect --user=test --password=test --locator=${default_ip}[9009]" -e "create region --name=$region_name --type=$region_type --total-num-buckets=7"
}

create_region() {
 local region_name=$1 region_type=$2
 $GEMFIRE_HOME/bin/gfsh -e "connect --user=test --password=test --locator=${default_ip}[9009]" -e "create region --name=$region_name --type=$region_type"
}

create_all_events_region() {
 local region_name=$1
 $GEMFIRE_HOME/bin/gfsh -e "connect --user=test --password=test --locator=${default_ip}[9009]" -e "create region --name=$region_name --template-region=/InterestPolicyAllRegion --cache-listener=com.gemfire.eventhandlers.CustomEventHandler"
}

#TODO refactor all these functions to take arguments
create_demographic_region_with_loader() {
 local region_name=$1 region_type=$2
 $GEMFIRE_HOME/bin/gfsh -e "connect --user=test --password=test --locator=${default_ip}[9009]" -e "create region --name=$region_name --type=$region_type --cache-loader=com.gemfire.loader.VisibilityLoader"
}

#configure_pdx_read_serialized() {
# $GEMFIRE_HOME/bin/gfsh -e "connect --locator=${default_ip}[9009]" -e "configure pdx --read-serialized=true"
#}

configure_pdx_auto_serializable() {
# $GEMFIRE_HOME/bin/gfsh -e "connect --locator=${default_ip}[9009]" -e "configure pdx --read-serialized=true"
 $GEMFIRE_HOME/bin/gfsh -e "connect --user=test --password=test --locator=${default_ip}[9009]" -e "configure pdx --auto-serializable-classes=com.gemfire.models.*"
}

get_default_ip

parent_dir=$(dirname $(pwd))
target_dir="${parent_dir}/functions/target/scala-2.11"
echo $(dir $target_dir)


start_locator "locator1" 9009

#start_locator "locator2" 9010

#configure_pdx_read_serialized
configure_pdx_auto_serializable

start_server "server1" 8085 8081
start_server "server2" 8086 8083
start_server "server3" 8087 8084

#deploy_functions

create_all_events_region "Positions"
create_demographic_region_with_loader "Visibility" "REPLICATE"
create_region "FxRates" "REPLICATE"
create_region "MarketPrices" "REPLICATE"

tail -f ./server1/server1.log ./server2/server2.log  ./server3/server3.log ./locator1/locator1.log ./locator2/locator2.log
