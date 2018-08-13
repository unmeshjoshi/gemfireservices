
### Setup Instruction
* Download and unzip gemfire from https://network.pivotal.io/products/pivotal-gemfire to you machine (say /work/pivotal-gemfire-9.5.1)
* export environment variable GEMFIRE_HOME to point to gemfire directory (e.g. export GEMFIRE_HOME=/work/pivotal-gemfire-9.5.1)
* Clone this repository
* Go to scripts directory and run ./startAll.sh. This will start gemfire with two locators and three servers. Also creates required regions.
* Run tests from the repository.





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
