#!/bin/bash


get_default_ip() {
 default_ip="$(route | grep '^default' | grep -o '[^ ]*$' |xargs -n 1 ifconfig |grep 'inet addr:'| cut -d: -f2| awk '{ print $1}')"
 echo "Using default ip ${default_ip}"
}

get_default_ip

gfsh start locator --name=locator1 --port=9009 --mcast-port=0 --locators="${default_ip}[9009]"

sleep 5

gfsh start locator --name=locator2 --port=9010 --mcast-port=0 --locators="${default_ip}[9009]"

sleep 5

gfsh -e "connect --locator=${default_ip}[9009]" -e "configure pdx --read-serialized=true"

gfsh start server --name=server1 --locators="${default_ip}[9009],${default_ip}[9010]" --server-port=40404--port=8002--J=-Dgemfire.http-port=8081

sleep 5


gfsh start server --name=server2 --locators="${default_ip}[9009],${default_ip}[9010]" --server-port=40405 --port=8003--J=-Dgemfire.http-port=8082

sleep 5

gfsh start server --name=server3 --locators="${default_ip}[9009],${default_ip}[9010]" --server-port=40406 --port=8004 --J=-Dgemfire.http-port=8083

sleep 5

gfsh -e "connect --locator=${default_ip}[9009]" -e "create region --name=Positions --type=REPLICATE"

gfsh -e "connect --locator=${default_ip}[9009]" -e "create region --name=FxRates --type=REPLICATE"

gfsh -e "connect --locator=${default_ip}[9009]" -e "create region --name=Users --type=REPLICATE"

tail -f ./server1/server1.log ./server2/server2.log  ./server3/server3.log ./locator1/locator1.log ./locator2/locator2.log
