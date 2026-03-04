# SaleOrder JSON Format Correction Guide

## Issue Identified
The `ForwardingDetails` array in your JSON had a data type mismatch:

### Problem Fields in ForwardingDetailDTO:
```java
private Integer forwardingName;    // Expects INTEGER, not String
```

## Your Original JSON ForwardingDetails:
```json
"ForwardingDetails": [
  {
    "Id": 0,
    "SaleOrderMasterRefId": 0,
    "ForwardingDate": "02/03/2026",
    "ForwardingName": "k8",              // ❌ WRONG: String provided, Integer expected
    "EnterRef": "12",
    "SMKNo": "21323",
    "SealByRefId": 76,
    "SealAmount": "",
    "BreakSealByRefId": 117,
    "BreakSealAmount": "",
    "ExitRef": "123123",
    "Quantity": "23",
    "S1": 23,
    "S2": 233,
    "RowNumber": 1
  },
  {
    "Id": 0,
    "SaleOrderMasterRefId": 0,
    "ForwardingDate": "02/03/2026",
    "SMKNo": "",
    "SealByRefId": null,
    "SealAmount": "",
    "BreakSealByRefId": null,
    "BreakSealAmount": "",
    "ExitRef": "",
    "Quantity": "",
    "S1": null,
    "S2": null,
    "RowNumber": 2
  }
]
```

## Corrected JSON ForwardingDetails:
```json
"ForwardingDetails": [
  {
    "id": 0,
    "saleOrderMasterRefId": 0,
    "forwardingDate": "02/03/2026",
    "forwardingName": 1,              // ✅ CORRECT: Integer value
    "enterRef": "12",
    "smkNo": "21323",
    "sealByRefId": 76,
    "sealAmount": "100",              // ✅ Should be String or empty
    "breakSealByRefId": 117,
    "breakSealAmount": "50",          // ✅ Should be String or empty
    "exitRef": "123123",
    "quantity": "23",
    "s1": 23,
    "s2": 233,
    "rowNumber": 1
  },
  {
    "id": 0,
    "saleOrderMasterRefId": 0,
    "forwardingDate": "02/03/2026",
    "forwardingName": null,           // ✅ Can be null or omitted
    "smkNo": "",
    "sealByRefId": null,
    "sealAmount": "",
    "breakSealByRefId": null,
    "breakSealAmount": "",
    "exitRef": "",
    "quantity": "",
    "s1": null,
    "s2": null,
    "rowNumber": 2
  }
]
```

## Key Changes:

### 1. Field Naming Convention
- **DTO uses camelCase** (lowercase first letter)
- Your JSON was using PascalCase (uppercase first letter)
- **Change all field names to camelCase**

### 2. Data Type Corrections
| Field | DTO Type | Your Value | Correction |
|-------|----------|-----------|-----------|
| `forwardingName` | Integer | "k8" (String) | Change to Integer value like `1`, `2`, etc. or `null` |
| `sealAmount` | String | "" | Can be empty string or numeric string like "100" |
| `breakSealAmount` | String | "" | Can be empty string or numeric string like "50" |

### 3. ForwardingDetailDTO Field Mappings
```
JSON Field Name          →  DTO Field Name  →  Type      →  Required?
"id"                     →  id              →  Integer   →  No
"saleOrderMasterRefId"   →  saleOrderMasterRefId → Integer → Yes (@NotNull)
"forwardingDate"         →  forwardingDate  →  String    →  No
"forwardingName"         →  forwardingName  →  Integer   →  No (⚠️ NOT String)
"enterRef"               →  enterRef        →  String    →  No
"smkNo"                  →  smkNo           →  String    →  No
"sealByRefId"            →  sealByRefId     →  Integer   →  No
"sealAmount"             →  sealAmount      →  String    →  No
"breakSealByRefId"       →  breakSealByRefId → Integer   →  No
"breakSealAmount"        →  breakSealAmount →  String    →  No
"exitRef"                →  exitRef         →  String    →  No
"quantity"               →  quantity        →  String    →  No
"s1"                     →  s1              →  Integer   →  No
"s2"                     →  s2              →  Integer   →  No
"rowNumber"              →  rowNumber       →  Integer   →  No
```

## Complete Corrected SaleOrder JSON Request Body:

```json
{
  "id": 0,
  "spotId": null,
  "companyRefId": 6,
  "userRefId": null,
  "employeeRefId": null,
  "agentCompanyRefId": null,
  "agentMasterRefId": null,
  "oAgentCompanyRefId": null,
  "oAgentMasterRefId": null,
  "customerRefId": 12,
  "jobMasterRefId": null,
  "saleDate": "02/03/2026",
  "saleType": "",
  "cNumberDisplay": "MY002601064",
  "cNumber": 2601064,
  "coinage": "",
  "sportsaleorderid": null,
  "grossAmount": "0.00",
  "taxAmount": "0.00",
  "discountAmount": 0,
  "remarks": "test data",
  "remarks1": "",
  "notportchagre": 0,
  "notBoatCPop": 0,
  "notBoatCPop1": 0,
  "notPFPPCPop1": 0,
  "notForwardingCPop": 0,
  "notPermitCPop": 0,
  "notLevyChares": 0,
  "notMMHECPop": 0,
  "notAFpoCPop": 0,
  "notSFWpoCPop": 0,
  "notSFEWpoCPop": 0,
  "portCPop": 0,
  "forwardingCPop": 0,
  "boatCPop": 0,
  "rbtportchagdeop": "",
  "permitCPop": 0,
  "liveCPop": 0,
  "mMHECPop": 0,
  "aFpoCPop": 0,
  "pFPPCPop1": 0,
  "sFWpoCPop": 0,
  "boatCPop1": 0,
  "sFEWpoCPop": 0,
  "plusAmount": 0,
  "minusAmount": 0,
  "dODescription": "DO Description",
  "amount": 0,
  "offvesselname": "",
  "loadingvesselname": "",
  "billType": "MY",
  "sPort": "WESTPORT-B18",
  "oPort": null,
  "vessel": null,
  "oVessel": null,
  "commodity": null,
  "cargo": "NOT ARRIVED",
  "eTA": "10/03/2026 14:52",
  "eTB": "19/03/2026 14:51",
  "eTD": null,
  "oETA": null,
  "oETB": null,
  "oETD": null,
  "forwardingDate": null,
  "forwarding2Date": null,
  "forwarding3Date": null,
  "dOCNo": null,
  "invoiceNo": null,
  "truckRefid": null,
  "driverRefid": null,
  "aWBNo": "108-708-204",
  "pTW": "",
  "lPTW": "",
  "oPTW": "",
  "bLCopy": "",
  "quantity": "100kg",
  "totalWeight": "10kg",
  "truckSize": "",
  "jStatus": null,
  "oStatus": 0,
  "forkliftbyRefid": null,
  "sealbyRefid": null,
  "sealbreakbyRefid": null,
  "sealbyRefid2": null,
  "sealbreakbyRefid2": null,
  "sealbyRefid3": null,
  "sealbreakbyRefid3": null,
  "boardingOfficerRefid": 129,
  "boardingOfficer1Refid": null,
  "boardingAmount": 50,
  "boardingAmount1": "",
  "lBoardingOfficerRefid": null,
  "lBoardingOfficer1Refid": null,
  "lBoardingAmount": "",
  "lBoardingAmount1": "",
  "oBoardingOfficerRefid": null,
  "oBoardingOfficer1Refid": null,
  "oBoardingAmount": "",
  "oBoardingAmount1": "",
  "forwardingEnterRef": "",
  "forwardingExitRef": "",
  "forwardingEnterRef3": "",
  "forwardingExitRef2": "",
  "forwardingEnterRef3": "",
  "forwardingQuantity": "",
  "forwardingQuantity2": "",
  "forwardingQuantity3": "",
  "forwardingExitRef3": "",
  "forwardingSMKNo": "",
  "forwardingSMKNo2": "",
  "forwardingSMKNo3": "",
  "portChargesRef": "",
  "portCharges": "",
  "lPortChargesRef": "",
  "lPortCharges": "",
  "oPortChargesRef": "",
  "oPortCharges": "",
  "sealAmount": "",
  "breakSealAmount2": "",
  "sealAmount2": "",
  "breakSealAmount2": "",
  "sealAmount3": "",
  "breakSealAmount3": "",
  "pickupDate": null,
  "deliveryDate": null,
  "pickupAddress": "CYCLECT ENGINEERING PTE LTD, 33 TUAS VIEW CRESCENT, SINGAPORE 637654\nARVIN /TEL: 8588 8653",
  "pickupQuantityList": "",
  "deliveryQuantityList": "",
  "wareHouseAddress": "",
  "wareHouseEnterDate": null,
  "wareHouseExitDate": null,
  "quantitylist": "10pkg",
  "deliveryAddress": "CYCLECT ENGINEERING PTE LTD, 33 TUAS VIEW CRESCENT, SINGAPORE 637654\nARVIN /TEL: 8588 8653",
  "forwarding": null,
  "forwarding2": null,
  "forwarding3": null,
  "origin": "malasiya",
  "destination": "singapour",
  "sCN": "",
  "lSCN": "",
  "zb": "ZB1",
  "zb2": null,
  "zbRef": "",
  "zbRef2": "",
  "forwarding1S1": null,
  "forwarding1S2": null,
  "forwarding2S1": null,
  "forwarding2S2": null,
  "forwarding3S1": null,
  "forwarding3S2": null,
  "trucksize2": "",
  "originRefId": null,
  "symbolRefId": null,
  "destinationRefId": null,
  "currencyValue": 0,
  "actualNetAmount": "0",
  "flighTime": null,
  "saleDetails": [
    {
      "id": 0,
      "saleOrderMasterRefId": 0,
      "productRefId": null,
      "productCode": "ADD DROP",
      "description": "ADITIONAL DROP",
      "qty": 0,
      "rate": 0,
      "amount": 0,
      "taxPercentage": 0,
      "gSTAmount": 0,
      "totalAmount": 0,
      "rowNumber": 1,
      "editMode": 1
    },
    {
      "id": 0,
      "saleOrderMasterRefId": 0,
      "productRefId": null,
      "productCode": "",
      "description": "",
      "qty": 0,
      "rate": 0,
      "amount": 0,
      "taxPercentage": 0,
      "gSTAmount": 0,
      "totalAmount": 0,
      "rowNumber": 2,
      "editMode": 1
    }
  ],
  "pickupDetails": [
    {
      "id": 0,
      "saleOrderMasterRefId": 0,
      "pickupAddress": "CYCLECT ENGINEERING PTE LTD, 33 TUAS VIEW CRESCENT, SINGAPORE 637654\nARVIN /TEL: 8588 8653",
      "pickupTime": "2026-03-19T14:52",
      "pickupWeaight": "200kg",
      "pickupQuantity": "10pkg",
      "rowNumber": 1
    }
  ],
  "deliveryDetails": [
    {
      "id": 0,
      "saleOrderMasterRefId": 0,
      "deliveryAddress": "CYCLECT ENGINEERING PTE LTD, 33 TUAS VIEW CRESCENT, SINGAPORE 637654\nARVIN /TEL: 8588 8653",
      "deliveryTime": "2026-04-24T20:52",
      "deliveryWeight": "103",
      "deliveryQuantity": "10",
      "rowNumber": 1
    }
  ],
  "forwardingDetails": [
    {
      "id": 0,
      "saleOrderMasterRefId": 0,
      "forwardingDate": "02/03/2026",
      "forwardingName": 1,
      "enterRef": "12",
      "smkNo": "21323",
      "sealByRefId": 76,
      "sealAmount": "100",
      "breakSealByRefId": 117,
      "breakSealAmount": "50",
      "exitRef": "123123",
      "quantity": "23",
      "s1": 23,
      "s2": 233,
      "rowNumber": 1
    },
    {
      "id": 0,
      "saleOrderMasterRefId": 0,
      "forwardingDate": "02/03/2026",
      "forwardingName": null,
      "smkNo": "",
      "sealByRefId": null,
      "sealAmount": "",
      "breakSealByRefId": null,
      "breakSealAmount": "",
      "exitRef": "",
      "quantity": "",
      "s1": null,
      "s2": null,
      "rowNumber": 2
    }
  ]
}
```

## Summary of Changes Required:

1. **Change ALL field names from PascalCase to camelCase** (e.g., `ForwardingDate` → `forwardingDate`)
2. **Fix `forwardingName` in ForwardingDetails**: Change from String "k8" to Integer value (e.g., `1`, `2`, etc.) or `null`
3. **Ensure all null values are properly represented** as `null` (not empty strings)
4. **String fields like `sealAmount` and `breakSealAmount`** should be numeric strings or empty strings

## API Endpoint to Use:
```
POST /api/sale-orders
Content-Type: application/json

{... corrected JSON payload above ...}
```

## Testing in Postman:
1. Create a new POST request to `http://localhost:8080/api/sale-orders`
2. Paste the corrected JSON in the request body
3. Set Content-Type to `application/json`
4. Click Send

Expected Response: `201 Created` with the created SaleOrder data

