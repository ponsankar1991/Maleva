# 🔧 MAPPER FIX - Why SaleOrderDTO is Needed

## 🎯 THE PROBLEM YOU IDENTIFIED

**Line 178** in `SaleOrderMasterServiceImpl.java`:
```java
mapper.updateEntityFromDto(dto, entity);  // ❌ WRONG - dto is SaleOrderDTO
```

The issue: The mapper method signature was:
```java
void updateEntityFromDto(SaleOrderMasterDto dto, @MappingTarget SaleOrderMaster entity);
```

**But `dto` parameter is `SaleOrderDTO`, NOT `SaleOrderMasterDto`!**

---

## ❌ WHY SEPARATE DTOs ARE NEEDED

### 1. **Different Use Cases = Different DTOs**

#### SaleOrderDTO
- **Used for**: `/api/sale-orders/save` endpoint (your new API)
- **Contains**: Complete order + child records in nested lists
- **Purpose**: Request DTO - accepts full order structure from client
- **Structure**:
```java
public class SaleOrderDTO {
    private Integer id;
    private Integer companyRefId;
    private Integer customerRefId;
    // ... all master fields ...
    
    private List<SaleDetailsDto> saleDetails;      // ← Nested
    private List<PickupDetailDTO> pickupDetails;   // ← Nested
    private List<DeliveryDetailDTO> deliveryDetails; // ← Nested
    private List<ForwardingDetailDTO> forwardingDetails; // ← Nested
}
```

#### SaleOrderMasterDto
- **Used for**: Response DTO + other existing methods
- **Contains**: Master record data only (no child lists)
- **Purpose**: Response DTO - returns single order from API
- **Structure**:
```java
public class SaleOrderMasterDto {
    private Integer id;
    private Integer companyRefId;
    private Integer customerRefId;
    // ... all master fields only ...
    // NO nested child lists
}
```

---

## 🔄 WHY YOU CAN'T USE SAME DTO

### Problem with Using Single DTO:

```java
// ❌ WRONG: Using SaleOrderDTO for everything
public class SaleOrderDTO {
    private Integer id;
    private Integer companyRefId;
    // ... master fields ...
    private List<SaleDetailsDto> saleDetails;      // ← Always there?
    private List<PickupDetailDTO> pickupDetails;   // ← Always needed?
    // ... etc
}
```

**Issues**:
1. **Child records not always provided**
   - Request 1: Has saleDetails
   - Request 2: No saleDetails
   - Response: Shouldn't include saleDetails again

2. **Different field names in different DTOs**
   - SaleOrderDTO: `saleDetails`
   - SaleOrderMasterDto: `saleDetails` (same)
   - But semantics are different

3. **Overhead on responses**
   - Client asks for simple order
   - Gets back massive nested structure
   - Unnecessary bandwidth

4. **Validation complexity**
   - Master-only request: @Valid would validate child DTOs (wrong!)
   - Full request: Must validate children

---

## ✅ THE SOLUTION: TWO MAPPER METHODS

I've added an **overloaded method** to the mapper:

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderMasterMapper {
    
    // Existing method (for old flow)
    void updateEntityFromDto(SaleOrderMasterDto dto, @MappingTarget SaleOrderMaster entity);
    
    // NEW: For the save() method which uses SaleOrderDTO
    void updateEntityFromDto(SaleOrderDTO dto, @MappingTarget SaleOrderMaster entity);
}
```

---

## 🔄 COMPLETE FLOW WITH CORRECT DTOs

### INSERT Flow (id = 0):
```
POST /api/sale-orders/save
Body: SaleOrderDTO
    ├─ id = 0
    ├─ companyRefId = 6
    ├─ saleDetails = [...]  ← Nested
    └─ pickupDetails = [...]  ← Nested
    ↓
save(SaleOrderDTO dto)  ← Receives SaleOrderDTO
    ├─ mapper.toEntity(dto)  ← Maps SaleOrderDTO → SaleOrderMaster
    ├─ save SaleDetails via mapper.toSaleDetailsentity()
    ├─ save PickupDetails via mapper.toSaleOrderPickupentity()
    └─ return mapper.toDto(entity)  ← Returns SaleOrderMasterDto
    ↓
Response: SaleOrderMasterDto (master only)
```

### UPDATE Flow (id > 0):
```
POST /api/sale-orders/save
Body: SaleOrderDTO
    ├─ id = 123  ← Existing record
    ├─ companyRefId = 6
    ├─ saleDetails = [...]
    └─ pickupDetails = [...]
    ↓
save(SaleOrderDTO dto)  ← Receives SaleOrderDTO
    ├─ repository.findById(123)  ← Gets existing SaleOrderMaster
    ├─ mapper.updateEntityFromDto(dto, entity)  ← NEW METHOD ✅
    │  ← Maps SaleOrderDTO fields to existing entity
    ├─ Delete old children
    ├─ Save new SaleDetails
    ├─ Save new PickupDetails
    └─ return mapper.toDto(entity)  ← Returns SaleOrderMasterDto
    ↓
Response: SaleOrderMasterDto (master only)
```

---

## 📊 MAPPER METHOD COMPARISON

| Method | Input | Output | Purpose |
|--------|-------|--------|---------|
| `toEntity(SaleOrderMasterDto)` | SaleOrderMasterDto (master only) | SaleOrderMaster entity | Old flow - create |
| `toEntity(SaleOrderDTO)` | SaleOrderDTO (with children) | SaleOrderMaster entity | New save() method - create |
| `updateEntityFromDto(SaleOrderMasterDto, entity)` | SaleOrderMasterDto (master only) | Updates entity | Old flow - update |
| `updateEntityFromDto(SaleOrderDTO, entity)` | SaleOrderDTO (with children) | Updates entity | NEW - save() UPDATE ✅ |

---

## 🎯 WHY NOT MERGE THEM?

### If we tried to use single SaleOrderDTO for everything:

```java
// ❌ BAD DESIGN
public class SaleOrderDTO {
    private Integer id;
    // ... 50+ fields ...
    private List<SaleDetailsDto> saleDetails;
    // This causes problems:
    // 1. Always included in serialization
    // 2. Always validated
    // 3. Large response payloads
    // 4. Confusing API contract
}
```

### Better Design (current approach):

```java
// ✅ GOOD DESIGN - Two DTOs

// 1. Request DTO - Complex, with children
public class SaleOrderDTO {
    // ... master fields ...
    private List<SaleDetailsDto> saleDetails;  // ← Client sends this
}

// 2. Response DTO - Simple, clean
public class SaleOrderMasterDto {
    // ... master fields only ...
    // No children - simpler response
}
```

---

## 🔗 MAPPER METHOD MAPPING

### What Each Method Does:

**Method 1**: `toEntity(SaleOrderMasterDto dto)` 
```java
// Maps: SaleOrderMasterDto → SaleOrderMaster
// Converts: DTO fields → Entity fields
// Usage: In create(SaleOrderMasterDto)
```

**Method 2**: `toEntity(SaleOrderDTO dto)` 
```java
// Maps: SaleOrderDTO → SaleOrderMaster
// Converts: Main DTO fields → Entity (ignores child lists)
// Usage: In save(SaleOrderDTO) - createNewEntity()
```

**Method 3**: `updateEntityFromDto(SaleOrderMasterDto dto, entity)` 
```java
// Updates: Existing SaleOrderMaster with SaleOrderMasterDto
// Usage: In update(SaleOrderMasterDto)
```

**Method 4**: `updateEntityFromDto(SaleOrderDTO dto, entity)` ✅ **NEWLY ADDED**
```java
// Updates: Existing SaleOrderMaster with SaleOrderDTO
// Usage: In save(SaleOrderDTO) - updateExistingEntity()
// Maps only master fields from SaleOrderDTO
// Ignores child lists (they're handled separately)
```

---

## ✨ COMPLETE MAPPER FILE

```java
package my.maleva.api.mapper;

import my.maleva.api.dto.*;
import my.maleva.api.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * SaleOrderMasterMapper - MapStruct mapper for SaleOrderMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderMasterMapper {
    
    // DTO ↔ Entity conversions
    SaleOrderMasterDto toDto(SaleOrderMaster entity);
    SaleOrderMaster toEntity(SaleOrderMasterDto dto);
    SaleOrderMaster toEntity(SaleOrderDTO dto);
    
    // Child entity conversions
    List<SaleDetails> toSaleDetailsentity(List<SaleDetailsDto> dto);
    List<SaleOrderPickup> toSaleOrderPickupentity(List<PickupDetailDTO> dto);
    List<SaleOrderDelivery> toSaleOrderDeliveryentity(List<DeliveryDetailDTO> dto);
    List<SaleOrderForwarding> toSaleOrderForwardingentity(List<ForwardingDetailDTO> dto);
    
    // Update methods (two overloads for different DTOs)
    void updateEntityFromDto(SaleOrderMasterDto dto, @MappingTarget SaleOrderMaster entity);
    void updateEntityFromDto(SaleOrderDTO dto, @MappingTarget SaleOrderMaster entity);
}
```

---

## 🎯 NOW LINE 178 WORKS CORRECTLY

```java
// In updateExistingEntity() method:
private SaleOrderMaster updateExistingEntity(SaleOrderDTO dto) {
    SaleOrderMaster entity = repository.findById(dto.getId())
        .orElseThrow(...);
    
    // ✅ NOW CORRECT: Using SaleOrderDTO with new mapper method
    mapper.updateEntityFromDto(dto, entity);  // ← Works now!
    entity.setModifiedDate(LocalDateTime.now());
    
    sanitizeEntity(entity);
    initializeNumericDefaults(entity);
    
    return entity;
}
```

---

## 📋 SUMMARY

### Problem:
- Line 178 used `SaleOrderDTO` but mapper only had method for `SaleOrderMasterDto`
- Type mismatch error

### Solution:
- Added overloaded mapper method for `SaleOrderDTO`
- Now mapper has TWO updateEntityFromDto() methods (method overloading)
- Java automatically chooses correct one based on parameter type

### Result:
- ✅ Compile error fixed
- ✅ Correct DTO used for each flow
- ✅ Clean separation of concerns
- ✅ Proper type safety

---

**Status**: ✅ **FIXED**  
**Files Updated**: SaleOrderMasterMapper.java  
**Ready**: YES - Ready to build

