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
