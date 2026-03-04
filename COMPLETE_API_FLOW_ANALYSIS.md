# 📊 COMPLETE API FLOW ANALYSIS - /api/sale-orders/save

## 🎯 API ENDPOINT

```
Method: POST
URL: http://{{host}}/api/sale-orders/save
Content-Type: application/json
```

---

## 🔄 COMPLETE FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────┐
│ 1. CLIENT (Postman / Frontend)                              │
│    Sends JSON request to /api/sale-orders/save              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ POST with SaleOrderDTO
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. CONTROLLER LAYER                                         │
│    SaleOrderMasterController                                │
│    @PostMapping("/save")                                    │
│    public ResponseEntity<?> save(@RequestBody SaleOrderDTO) │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Call service.save(dto)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. SERVICE LAYER                                            │
│    SaleOrderMasterServiceImpl                                │
│    @Transactional                                           │
│    public SaleOrderMasterDto save(SaleOrderDTO dto)         │
│                                                             │
│    Step 1: Validate required fields                         │
│      - companyRefId (required)                              │
│      - customerRefId (required)                             │
│      - cNumber (required)                                   │
│                                                             │
│    Step 2: Check for duplicate cNumber                      │
│                                                             │
│    Step 3: Map DTO to Entity                                │
│                                                             │
│    Step 4: Set audit fields                                 │
│      - CreatedDate = now()                                  │
│      - ModifiedDate = now()                                 │
│                                                             │
│    Step 5: Initialize defaults                              │
│      - All amounts → 0.0                                    │
│      - Active → 0                                           │
│      - JobMasterRefId → 0                                   │
│                                                             │
│    Step 6: Save master record                               │
│      - INSERT into SALE_ORDER_MASTER                        │
│      - Get generated ID                                     │
│                                                             │
│    Step 7: Save child records                               │
│      - SaleDetails                                          │
│      - PickupDetails                                        │
│      - DeliveryDetails                                      │
│      - ForwardingDetails                                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Save entities
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. MAPPER LAYER                                             │
│    SaleOrderMasterMapper                                    │
│    Converts DTO → Entity                                    │
│    Converts Entity → DTO (response)                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Map to entity
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. REPOSITORY LAYER                                         │
│    SaleOrderMasterRepository                                │
│    SaleDetailsRepository                                    │
│    SaleOrderPickupRepository                                │
│    SaleOrderDeliveryRepository                              │
│    SaleOrderForwardingRepository                            │
│                                                             │
│    Performs database operations                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ JDBC/SQL
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. DATABASE LAYER                                           │
│    Tables:                                                  │
│    - SALE_ORDER_MASTER (main)                               │
│    - SALE_DETAILS (child)                                   │
│    - SALE_ORDER_PICKUP (child)                              │
│    - SALE_ORDER_DELIVERY (child)                            │
│    - SALE_ORDER_FORWARDING (child)                          │
│                                                             │
│    Operations:                                              │
│    INSERT into SALE_ORDER_MASTER                            │
│    INSERT into SALE_DETAILS                                 │
│    INSERT into SALE_ORDER_PICKUP                            │
│    INSERT into SALE_ORDER_DELIVERY                          │
│    INSERT into SALE_ORDER_FORWARDING                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Success
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. RESPONSE                                                 │
│    HTTP Status: 201 Created                                 │
│    Body: SaleOrderMasterDto with ID and all data            │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 REQUEST STRUCTURE

### Controller Receives
```java
@PostMapping("/save")
public ResponseEntity<?> save(@Valid @RequestBody SaleOrderDTO dto)
```

### SaleOrderDTO Fields

**Main Order Fields:**
```
id: Integer (0 for new, >0 for update)
companyRefId: Integer (REQUIRED)
customerRefId: Integer (REQUIRED)
cNumber: Integer (REQUIRED - C Number)
cNumberDisplay: String
jobMasterRefId: Integer
employeeRefId: Integer
saleDate: String (ISO format)
saleType: String (STANDARD, CREDIT, etc)
billType: String (MY, etc)
grossAmount: String
taxAmount: String
discountAmount: Integer
remarks: String
... 50+ other fields ...
```

**Child Collections:**
```
saleDetails: List<SaleDetailDTO>
pickupDetails: List<PickupDetailDTO>
deliveryDetails: List<DeliveryDetailDTO>
forwardingDetails: List<ForwardingDetailDTO>
```

---

## 🔧 SERVICE PROCESSING

### SaleOrderMasterServiceImpl.save() Method

**Step-by-Step Processing:**

1. **Validate Required Fields**
   ```java
   if (dto.getCompanyRefId() == null) 
       throw new RuntimeException("Company Reference ID is required");
   if (dto.getCustomerRefId() == null)
       throw new RuntimeException("Customer Reference ID is required");
   if (dto.getCNumber() == null)
       throw new RuntimeException("C Number is required and cannot be null");
   ```

2. **Check for Duplicate cNumber**
   ```java
   if (repository.existsByCompanyRefIdAndCNumber(...))
       throw new RuntimeException("C Number already exists");
   ```

3. **Map DTO to Entity**
   ```java
   SaleOrderMaster entity = mapper.toEntity(dto);
   ```

4. **Set Audit Fields**
   ```java
   entity.setCreatedDate(LocalDateTime.now());
   entity.setModifiedDate(LocalDateTime.now());
   if (entity.getActive() == null) entity.setActive(0);
   ```

5. **Ensure Critical Field**
   ```java
   if (entity.getCNumber() == null) {
       entity.setCNumber(dto.getCNumber());
   }
   ```

6. **Initialize All Defaults**
   ```java
   initializeDefaults(entity);
   // Sets all null amounts to 0.0
   // Sets JobMasterRefId to 0 if null
   // Sets Active to 0 if null
   ```

7. **Save Master Record**
   ```java
   SaleOrderMaster saved = repository.save(entity);
   Integer masterId = saved.getId();
   ```

8. **Save Child Records**
   ```java
   if (dto.getSaleDetails() != null) {
       List<SaleDetails> saleDetails = mapper.toSaleDetailsentity(...);
       saleDetails.forEach(detail -> detail.setSaleOrderMasterRefId(masterId));
       saleDetailsRepository.saveAll(saleDetails);
   }
   // ... similar for other child records
   ```

---

## 🔄 COMPLETE METHOD CODE

```java
@Transactional
public SaleOrderMasterDto save(SaleOrderDTO dto) {
    logger.info("Creating SaleOrderMaster for company: {}", dto.getCompanyRefId());

    // Validate required fields
    if (dto.getCompanyRefId() == null) {
        throw new RuntimeException("Company Reference ID is required");
    }
    if (dto.getCustomerRefId() == null) {
        throw new RuntimeException("Customer Reference ID is required");
    }
    if (dto.getCNumber() == null) {
        throw new RuntimeException("C Number is required and cannot be null");
    }

    // Check for duplicate
    if (repository.existsByCompanyRefIdAndCNumber(dto.getCompanyRefId(), dto.getCNumber())) {
        throw new RuntimeException("C Number already exists: " + dto.getCNumber());
    }

    // Map and prepare entity
    SaleOrderMaster entity = mapper.toEntity(dto);
    entity.setCreatedDate(LocalDateTime.now());
    entity.setModifiedDate(LocalDateTime.now());
    if (entity.getActive() == null) entity.setActive(0);

    // Ensure CNumber is set
    if (entity.getCNumber() == null) {
        entity.setCNumber(dto.getCNumber());
    }

    // Ensure Amount fields
    if (entity.getAmount() == null) {
        entity.setAmount(0.0);
    }

    // Set all financial defaults
    if (entity.getGrossAmount() == null) {
        entity.setGrossAmount(convertStringToDouble(dto.getGrossAmount()));
    }
    if (entity.getTaxAmount() == null) {
        entity.setTaxAmount(convertStringToDouble(dto.getTaxAmount()));
    }
    if (entity.getDiscountAmount() == null) {
        entity.setDiscountAmount(0.0);
    }
    if (entity.getPlusAmount() == null) {
        entity.setPlusAmount(0.0);
    }
    if (entity.getMinusAmount() == null) {
        entity.setMinusAmount(0.0);
    }
    if (entity.getCoinage() == null) {
        entity.setCoinage(0.0);
    }

    // Initialize all other defaults
    initializeDefaults(entity);

    // Save master record
    SaleOrderMaster saved = repository.save(entity);
    Integer masterId = saved.getId();

    // Save SaleDetails
    if (dto.getSaleDetails() != null) {
        List<SaleDetails> saleDetails = mapper.toSaleDetailsentity(dto.getSaleDetails());
        saleDetails.forEach(detail -> detail.setSaleOrderMasterRefId(masterId));
        saleDetailsRepository.saveAll(saleDetails);
    }

    // Save Pickup
    if (dto.getPickupDetails() != null) {
        List<SaleOrderPickup> pickup = mapper.toSaleOrderPickupentity(dto.getPickupDetails());
        pickup.forEach(p -> p.setSaleOrderMasterRefId(masterId));
        saleOrderPickupRepository.saveAll(pickup);
    }

    // Save Delivery
    if (dto.getDeliveryDetails() != null) {
        List<SaleOrderDelivery> delivery = mapper.toSaleOrderDeliveryentity(dto.getDeliveryDetails());
        delivery.forEach(d -> d.setSaleOrderMasterRefId(masterId));
        saleOrderDeliveryRepository.saveAll(delivery);
    }

    // Save Forwarding
    if (dto.getForwardingDetails() != null) {
        List<SaleOrderForwarding> forwarding = mapper.toSaleOrderForwardingentity(dto.getForwardingDetails());
        forwarding.forEach(f -> f.setSaleOrderMasterRefId(masterId));
        saleOrderForwardingRepository.saveAll(forwarding);
    }

    logger.info("SaleOrderMaster created with ID: {}", saved.getId());
    return mapper.toDto(saved);
}
```

---

## 📊 DATABASE OPERATIONS

### Master Table Insert
```sql
INSERT INTO SALE_ORDER_MASTER (
    CompanyRefId, CustomerRefId, CNumber, SaleDate, SaleType, 
    BillType, GrossAmount, TaxAmount, Amount, Active,
    Created_Date, Modified_Date, ...
) VALUES (
    6, 12, 2601065, '2026-03-02', 'CREDIT',
    'MY', 0.0, 0.0, 0.0, 0,
    NOW(), NOW(), ...
);
```

### Child Table Inserts
```sql
INSERT INTO SALE_DETAILS (
    SaleOrderMasterRefId, ProductCode, ProductName, TaxPercent, ...
) VALUES (
    1, 'ADD DROP', 'ADDITIONAL DROP', 6, ...
);

INSERT INTO SALE_ORDER_PICKUP (
    SaleOrderMasterRefId, PickupAddress, PickupTime, ...
) VALUES (
    1, 'Address...', '2026-03-19 14:52:00', ...
);

-- Similar for DELIVERY and FORWARDING
```

---

## 📦 RESPONSE STRUCTURE

### Success Response (HTTP 201)
```json
{
  "id": 1,
  "companyRefId": 6,
  "customerRefId": 12,
  "cNumber": 2601065,
  "cNumberDisplay": "MY002601065",
  "saleDate": "2026-03-02T00:00:00",
  "saleType": "CREDIT",
  "billType": "MY",
  "grossAmount": 0.0,
  "taxAmount": 0.0,
  "amount": 0.0,
  
  "saleDetails": [
    {
      "id": 1,
      "saleOrderMasterRefId": 1,
      "productCode": "ADD DROP",
      "productName": "ADDITIONAL DROP",
      "taxPercent": 6
    }
  ],
  
  "pickupDetails": [...],
  "deliveryDetails": [...],
  "forwardingDetails": [...]
}
```

### Error Response (HTTP 409)
```json
{
  "error": "Error: C Number already exists: 2601065"
}
```

---

## ✅ DATA VALIDATION

### Required Fields
- ✓ companyRefId (must be > 0)
- ✓ customerRefId (must be > 0)
- ✓ cNumber (must be > 0 and unique)

### Auto-Set Fields
- ✓ id (auto-generated by database)
- ✓ createdDate (set to current timestamp)
- ✓ modifiedDate (set to current timestamp)
- ✓ active (default 0)
- ✓ jobMasterRefId (default 0 if null)

### Default Values
- ✓ All amounts → 0.0 if null
- ✓ discountAmount → 0 if null
- ✓ coinage → 0.0 if null
- ✓ All other amounts → 0.0

---

## 🔗 DEPENDENCIES

### Repositories Used
- `SaleOrderMasterRepository` - Master records
- `SaleDetailsRepository` - Detail records
- `SaleOrderPickupRepository` - Pickup info
- `SaleOrderDeliveryRepository` - Delivery info
- `SaleOrderForwardingRepository` - Forwarding info

### Mapper Used
- `SaleOrderMasterMapper` - DTO ↔ Entity conversion

### DTOs Used
- `SaleOrderDTO` - Request DTO
- `SaleOrderMasterDto` - Response DTO
- `SaleDetailDTO` - Child detail DTO
- `PickupDetailDTO` - Child pickup DTO
- `DeliveryDetailDTO` - Child delivery DTO
- `ForwardingDetailDTO` - Child forwarding DTO

---

## 🎯 USAGE EXAMPLE

### Postman Configuration

**URL**: `http://localhost:8080/api/sale-orders/save`

**Method**: POST

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <token>
```

**Body** (raw JSON):
```json
{
  "id": 0,
  "companyRefId": 6,
  "customerRefId": 12,
  "cNumber": 2601065,
  "cNumberDisplay": "MY002601065",
  "saleDate": "2026-03-02T00:00:00",
  "saleType": "CREDIT",
  "billType": "MY",
  "grossAmount": "0.00",
  "taxAmount": "0.00",
  "amount": "0.00",
  "remarks": "test data",
  
  "saleDetails": [
    {
      "productCode": "ADD DROP",
      "productName": "ADDITIONAL DROP",
      "taxPercent": 6
    }
  ],
  
  "pickupDetails": [
    {
      "pickupAddress": "Address here",
      "pickupTime": "2026-03-19T14:52:00",
      "pickupQuantity": "10pkg"
    }
  ]
}
```

**Expected Response** (HTTP 201):
```json
{
  "id": 1,
  "companyRefId": 6,
  "customerRefId": 12,
  "cNumber": 2601065,
  ...
  "saleDetails": [...],
  "pickupDetails": [...]
}
```

---

## 🔍 TRANSACTION ISOLATION

- **@Transactional** ensures ACID compliance
- **Rollback** on any error
- **All or nothing** - entire operation succeeds or fails
- **No partial saves**

---

## ⚠️ ERROR SCENARIOS

| Error | Cause | HTTP Status |
|-------|-------|-------------|
| Company ID required | companyRefId is null | 409 CONFLICT |
| Customer ID required | customerRefId is null | 409 CONFLICT |
| C Number required | cNumber is null | 409 CONFLICT |
| C Number already exists | Duplicate cNumber | 409 CONFLICT |
| Unexpected error | Any unhandled exception | 500 SERVER ERROR |

---

## 📋 FILES INVOLVED

1. **Controller**: SaleOrderMasterController.java
2. **Service**: SaleOrderMasterServiceImpl.java
3. **Mapper**: SaleOrderMasterMapper.java
4. **DTOs**: SaleOrderDTO.java, SaleOrderMasterDto.java
5. **Repositories**: 5 repositories for master and children
6. **Entities**: SaleOrderMaster + 4 child entities
7. **Database**: 5 tables with relationships

---

**Status**: ✅ API FULLY DOCUMENTED  
**Ready**: YES - Ready to use

