
### Setup Instruction
* Download and unzip gemfire from https://network.pivotal.io/products/pivotal-gemfire to you machine (say /work/pivotal-gemfire-9.5.1)
* export environment variable GEMFIRE_HOME to point to gemfire directory (e.g. export GEMFIRE_HOME=/work/pivotal-gemfire-9.5.1)
* Clone this repository
* Go to scripts directory and run ./startAll.sh. This will start gemfire with two locators and three servers. Also creates required regions.
* Run tests from the repository.
* docker run --name gemfire-locator --hostname=locator -v $(pwd)/logs/:/logs/ -it gemfireservices
* docker run --name gemfire-server1 --hostname=server1 -v $(pwd)/logs/:/logs/ -it gemfireservices
* /pivotal-gemfire-9.1.0/bin/gfsh start locator --name=locator1 --port=9009 --properties-file=/pivotal-gemfire-9.1.0/config/gemfire.properties --mcast-port=0 --dir=/logs/locator1
* docker exec -it gemfire-locator bash
* /pivotal-gemfire-9.1.0/bin/gfsh 
* connect --locator=172.17.0.2[9009]
* /pivotal-gemfire-9.1.0/bin/gfsh start server --name=server1 --properties-file=/pivotal-gemfire-9.1.0/config/gemfire.properties --cache-xml-file=/pivotal-gemfire-9.1.0/config/cache.xml --mcast-port=0 --locators="172.17.0.2[9009]" --server-port=8085 --dir=/logs/server1
* create region --name=Positions --type=PARTITION_PERSISTENT --total-num-buckets=7
  /pivotal-gemfire-9.1.0/bin/gfsh -e "connect --locator=172.17.0.2[9009]" -e "create region --name=Positions --type=PARTITION_PERSISTENT --total-num-buckets=7" 
create disk-store --name=PDX_TYPES --dir=/pdx/
configure pdx --read-serialized=true
configure pdx --disk-store=PDX_TYPES
create region --name=MarketPrices --type=PARTITION_PERSISTENT --total-num-buckets=7

docker run --name gemfire-server2 --hostname=server2 -v $(pwd)/logs/:/logs/ -it gemfireservices
/pivotal-gemfire-9.1.0/bin/gfsh start server --name=server2 --properties-file=/pivotal-gemfire-9.1.0/config/gemfire.properties --cache-xml-file=/pivotal-gemfire-9.1.0/config/cache.xml --mcast-port=0 --locators="172.17.0.2[9009]" --server-port=8085 --dir=/logs/server2
alter disk-store --name=PDX_POSITION --region=/PdxTypes --disk-dirs=/logs/server1/ --remove


### Request
```
/positions?
&assetClass=CASH
&reportingCurrency=INR
&date=20-Jun-2018
&aggregate=AMOUNT
&aggregate=GAIN_LOSS
&sortBy=AMOUNT
&sortOrder=DESC
&pageSize=20
&page=2
&includeData=true
```

### Response:
```json
{
    "aggregates": {
        "amount": 28908.98,
        "gain_loss": 1039.89
    },
    "elements": [
        {"positionId": 1},
        {"positionId": 2},
        {"positionId": 3}
    ],
    "page": {
        "totalElements": 2000,
        "totalPages": 100,
        "currentPage": 2
    }
}
```
