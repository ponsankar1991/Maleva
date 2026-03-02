# SaleOrderDTO Creation - Summary

## Overview
Successfully created comprehensive DTOs for the Sale Order JSON structure with the filename `SaleOrderDTO.java` as requested. These DTOs follow the existing project standards and patterns.

## DTOs Created

### 1. **SaleOrderDTO** ✅
**File**: `src/main/java/my/maleva/api/dto/SaleOrderDTO.java`

**Purpose**: Main comprehensive DTO that represents the complete sale order structure with all nested details.

**Key Features**:
- Contains all main order fields (id, companyRefId, customerRefId, jobMasterRefId, etc.)
- Boolean flags for various charges (notportchagre, notBoatCPop, forwardingCPop, etc.)
- Vessel and port information (offvesselname, loadingvesselname, sPort, oPort, etc.)
- Dates in string format (saleDate, eta, etb, etd, etc.)
- Amounts stored as strings for precision (grossAmount, taxAmount, amount, etc.)
- Collections for nested details:
  - `List<SaleDetailDTO> saleDetails` - Sale order line items
  - `List<PickupDetailDTO> pickupDetails` - Pickup information
  - `List<DeliveryDetailDTO> deliveryDetails` - Delivery information
  - `List<ForwardingDetailDTO> forwardingDetails` - Forwarding information

### 2. **SaleDetailDTO** ✅
**File**: `src/main/java/my/maleva/api/dto/SaleDetailDTO.java`

**Purpose**: Represents individual line items in a sale order.

**Key Features**:
- Product information (productRefId, productCode, description)
- Quantity and pricing (qty, rate, amount)
- Tax calculation (taxPercentage, gstAmount, totalAmount)
- Row tracking (rowNumber, editMode)
- Min validation on all numeric fields

### 3. **PickupDetailDTO** ✅
**File**: `src/main/java/my/maleva/api/dto/PickupDetailDTO.java`

**Purpose**: Represents pickup information for items.

**Key Features**:
- Pickup address and location details
- Pickup time and weight information (pickupWeaight - note: field name matches JSON exactly)
- Pickup quantity
- Row tracking (rowNumber)

### 4. **DeliveryDetailDTO** ✅
**File**: `src/main/java/my/maleva/api/dto/DeliveryDetailDTO.java`

**Purpose**: Represents delivery information for items.

**Key Features**:
- Delivery address and location
- Delivery time and weight
- Delivery quantity
- Row tracking (rowNumber)

### 5. **ForwardingDetailDTO** ✅
**File**: `src/main/java/my/maleva/api/dto/ForwardingDetailDTO.java`

**Purpose**: Represents forwarding/logistics information during transit.

**Key Features**:
- Forwarding date and agent information
- Reference tracking (enterRef, exitRef, smkNo)
- Seal information (sealByRefId, sealAmount, breakSealAmount)
- Quantity tracking
- Row tracking (rowNumber)

## Design Patterns Used

### ✅ Consistency with Existing Code
- All DTOs use Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor, @Builder)
- Jakarta validation annotations for input validation
- Follows the existing DTO naming convention
- Uses String for date fields to match JSON format

### ✅ Validation
- @NotNull annotations on required fields with meaningful messages
- @Size annotations for string length validation
- @Min annotations for numeric field validation (on SaleDetailDTO)

### ✅ Documentation
- Comprehensive JavaDoc comments on all classes
- Descriptive class-level documentation

## Nested Structure Mapping

The DTO structure mirrors the JSON hierarchy:

```
SaleOrderDTO (Main Order)
├── SaleDetailDTO[] (saleDetails)
├── PickupDetailDTO[] (pickupDetails)
├── DeliveryDetailDTO[] (deliveryDetails)
└── ForwardingDetailDTO[] (forwardingDetails)
```

## JSON to DTO Mapping Examples

### Main Object Fields
```json
{
  "Id": 0,                          → id: Integer
  "SpotId": null,                   → spotId: Integer
  "CompanyRefId": 6,                → companyRefId: Integer (@NotNull)
  "CustomerRefId": 66,              → customerRefId: Integer (@NotNull)
  "SaleDate": "24/10/2023",        → saleDate: String
  "GrossAmount": "0.00",            → grossAmount: String
  "TaxAmount": "0",                 → taxAmount: String
  "DiscountAmount": 0,              → discountAmount: Integer
  "Notportchagre": false,           → notportchagre: Boolean
  "Amount": "0.00",                 → amount: String
  "Offvesselname": "EVERGREEN PKG", → offvesselname: String
  "ETA": "12/03/2026 12:01",       → eta: String
  // ... more fields
}
```

### Nested SaleDetails
```json
"SaleDetails": [
  {
    "Id": 0,                      → id: Integer
    "SaleOrderMasterRefId": 0,    → saleOrderMasterRefId: Integer (@NotNull)
    "ProductCode": "VSL TO VSL",  → productCode: String
    "Description": "...",         → description: String
    "Qty": 1,                     → qty: Integer
    "Rate": 10,                   → rate: Double
    "Amount": 10,                 → amount: Double
    "TaxPercentage": 0,           → taxPercentage: Double
    "GSTAmount": 0,               → gstAmount: Double
    "TotalAmount": 0,             → totalAmount: Double
    "RowNumber": 1,               → rowNumber: Integer
    "EditMode": 1                 → editMode: Integer
  }
]
```

### Nested PickupDetails
```json
"PickupDetails": [
  {
    "Id": 0,                      → id: Integer
    "SaleOrderMasterRefId": 0,    → saleOrderMasterRefId: Integer (@NotNull)
    "PickupAddress": "",          → pickupAddress: String
    "PickupTime": null,           → pickupTime: String
    "PickupWeaight": "",          → pickupWeaight: String
    "PickupQuantity": "",         → pickupQuantity: String
    "RowNumber": 1                → rowNumber: Integer
  }
]
```

### Nested DeliveryDetails
```json
"DeliveryDetails": [
  {
    "Id": 0,                      → id: Integer
    "SaleOrderMasterRefId": 0,    → saleOrderMasterRefId: Integer (@NotNull)
    "DeliveryAddress": "",        → deliveryAddress: String
    "DeliveryTime": null,         → deliveryTime: String
    "DeliveryWeight": "",         → deliveryWeight: String
    "DeliveryQuantity": "",       → deliveryQuantity: String
    "RowNumber": 1                → rowNumber: Integer
  }
]
```

### Nested ForwardingDetails
```json
"ForwardingDetails": [
  {
    "Id": 0,                      → id: Integer
    "SaleOrderMasterRefId": 0,    → saleOrderMasterRefId: Integer (@NotNull)
    "ForwardingDate": null,       → forwardingDate: String
    "ForwardingName": null,       → forwardingName: Integer
    "EnterRef": "",               → enterRef: String
    "SMKNo": "",                  → smkNo: String
    "SealByRefId": null,          → sealByRefId: Integer
    "SealAmount": "",             → sealAmount: String
    "BreakSealByRefId": null,     → breakSealByRefId: Integer
    "BreakSealAmount": "",        → breakSealAmount: String
    "ExitRef": "",                → exitRef: String
    "Quantity": "",               → quantity: String
    "S1": null,                   → s1: Integer
    "S2": null,                   → s2: Integer
    "RowNumber": 1                → rowNumber: Integer
  }
]
```

## Usage Recommendations

### For API Endpoints
```java
@PostMapping("/sale-order")
public ResponseEntity<?> createSaleOrder(@RequestBody SaleOrderDTO dto) {
    // Implementation
}

@PutMapping("/sale-order/{id}")
public ResponseEntity<?> updateSaleOrder(@PathVariable Integer id, 
                                          @RequestBody SaleOrderDTO dto) {
    // Implementation
}

@GetMapping("/sale-order/{id}")
public ResponseEntity<SaleOrderDTO> getSaleOrder(@PathVariable Integer id) {
    // Implementation
}

@GetMapping("/sale-order")
public ResponseEntity<Page<SaleOrderDTO>> getAllSaleOrders(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    // Implementation
}

@DeleteMapping("/sale-order/{id}")
public ResponseEntity<?> deleteSaleOrder(@PathVariable Integer id) {
    // Implementation
}
```

### For MapStruct Mapper (if needed)
Create mapper interface:
```java
@Mapper(componentModel = "spring")
public interface SaleOrderMapper {
    SaleOrderDTO toDto(SaleOrder entity);
    SaleOrder toEntity(SaleOrderDTO dto);
}
```

### For Service Layer
```java
@Service
public class SaleOrderService {
    @Autowired
    private SaleOrderRepository repository;
    
    @Autowired
    private SaleOrderMapper mapper;
    
    public SaleOrderDTO create(SaleOrderDTO dto) {
        SaleOrder entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }
    
    public SaleOrderDTO getById(Integer id) {
        return repository.findById(id)
            .map(mapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Sale Order not found"));
    }
}
```

## Important Field Notes

1. **pickupWeaight** - Field name in JSON has typo (should be "weight"), but DTO preserves exact JSON field name for deserialization
2. **String Amounts** - Currently stored as String (e.g., "0.00", "10"). Consider using BigDecimal for monetary operations if doing calculations
3. **Dates** - Currently stored as String to match JSON format. Consider converting to LocalDateTime during service layer processing
4. **Boolean flags** - Nullable Boolean to handle tri-state values (true, false, null)
5. **Reference IDs** - Nullable Integer for optional foreign keys

## File Locations

All DTOs are created in the standard location:
```
src/main/java/my/maleva/api/dto/
```

Files created:
- `SaleOrderDTO.java` (Main DTO)
- `SaleDetailDTO.java` (Nested - Sale Details)
- `PickupDetailDTO.java` (Nested - Pickup Details)
- `DeliveryDetailDTO.java` (Nested - Delivery Details)
- `ForwardingDetailDTO.java` (Nested - Forwarding Details)

All DTOs integrate seamlessly with the existing project structure and naming conventions.

## Next Steps (If Needed)

1. **Create JPA Entities** based on these DTOs
2. **Create MapStruct Mappers** for Entity ↔ DTO conversions
3. **Create Repository Interfaces** for data access
4. **Create Service Classes** for business logic
5. **Create Controller Classes** for REST endpoints
6. **Create Request/Response Wrapper DTOs** if needed

---

**Status**: ✅ Complete and Ready for Use
**Date Created**: March 2, 2026

