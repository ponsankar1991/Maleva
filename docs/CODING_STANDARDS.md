# 📚 Maleva Project - Coding Standards & Architecture Guide

> **Version:** 1.0  
> **Last Updated:** March 26, 2026  
> **Project:** Maleva API (Spring Boot)

---

## 📁 RECOMMENDED FOLDER STRUCTURE

```
src/main/java/my/maleva/api/
│
├── 📄 MalevaApplication.java          # Main entry point
├── 📄 ServletInitializer.java         # WAR deployment initializer
│
├── 📂 common/                          # ═══════════════════════════════
│   │                                   # SHARED COMPONENTS (Cross-cutting)
│   │
│   ├── 📂 config/                      # All Spring configurations
│   │   ├── DataSourceConfig.java
│   │   ├── JpaConfig.java
│   │   ├── RedisConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── WebMvcConfig.java
│   │
│   ├── 📂 constant/                    # Constants & Enums
│   │   ├── ApiConstants.java
│   │   ├── AppConstants.java
│   │   ├── ErrorCodes.java
│   │   └── UserRoles.java
│   │
│   ├── 📂 dto/                         # Common DTOs
│   │   ├── ApiResponse.java
│   │   ├── PageRequest.java
│   │   ├── PageResponse.java
│   │   └── ErrorResponse.java
│   │
│   ├── 📂 exception/                   # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ApiError.java
│   │   ├── EntityNotFoundException.java
│   │   ├── InvalidRequestException.java
│   │   ├── UnauthorizedException.java
│   │   └── BusinessException.java
│   │
│   ├── 📂 util/                        # Utility classes
│   │   ├── DateTimeUtil.java
│   │   ├── StringUtils.java
│   │   ├── JsonUtils.java
│   │   └── ValidationUtils.java
│   │
│   └── 📂 annotation/                  # Custom annotations
│       ├── Auditable.java
│       └── RequiresPermission.java
│
├── 📂 security/                        # ═══════════════════════════════
│   │                                   # AUTHENTICATION & AUTHORIZATION
│   │
│   ├── 📂 controller/
│   │   └── AuthController.java
│   ├── 📂 dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── TokenRefreshRequest.java
│   ├── 📂 filter/
│   │   └── JwtAuthenticationFilter.java
│   ├── 📂 service/
│   │   ├── JwtService.java
│   │   ├── TokenStore.java
│   │   └── RedisTokenStore.java
│   └── 📂 config/
│       └── SecurityConfig.java
│
├── 📂 integration/                     # ═══════════════════════════════
│   │                                   # EXTERNAL SERVICE INTEGRATIONS
│   │
│   ├── 📂 qne/
│   │   ├── QneClient.java
│   │   ├── QneUrlBuilder.java
│   │   └── QneResponse.java
│   ├── 📂 email/
│   │   └── EmailService.java
│   └── 📂 sms/
│       └── SmsService.java
│
└── 📂 module/                          # ═══════════════════════════════
    │                                   # BUSINESS MODULES (Domain-Driven)
    │
    ├── 📂 sale/                        # ──────────────────────────────
    │   │                               # SALES MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── SaleMasterController.java
    │   │   ├── SaleDetailsController.java
    │   │   ├── SaleOrderMasterController.java
    │   │   ├── SaleOrderDetailsController.java
    │   │   ├── SaleCreditMasterController.java
    │   │   ├── SaleCreditDetailsController.java
    │   │   ├── SaleOrderBOController.java
    │   │   ├── SaleOrderDeliveryController.java
    │   │   ├── SaleOrderForwardingController.java
    │   │   └── SaleOrderPickupController.java
    │   │
    │   ├── 📂 dto/
    │   │   ├── 📂 request/
    │   │   │   ├── SaleOrderCreateRequest.java
    │   │   │   └── SaleOrderFilterRequest.java
    │   │   ├── 📂 response/
    │   │   │   ├── SaleOrderResponse.java
    │   │   │   └── SaleReportResponse.java
    │   │   ├── SaleMasterDto.java
    │   │   ├── SaleDetailsDto.java
    │   │   ├── SaleOrderMasterDto.java
    │   │   └── SaleOrderDetailsDto.java
    │   │
    │   ├── 📂 entity/
    │   │   ├── SaleMaster.java
    │   │   ├── SaleDetails.java
    │   │   ├── SaleOrderMaster.java
    │   │   ├── SaleOrderDetails.java
    │   │   ├── SaleCreditMaster.java
    │   │   ├── SaleCreditDetails.java
    │   │   └── SaleOrderBO.java
    │   │
    │   ├── 📂 mapper/
    │   │   ├── SaleMasterMapper.java
    │   │   ├── SaleOrderMasterMapper.java
    │   │   └── SaleCreditMasterMapper.java
    │   │
    │   ├── 📂 repository/
    │   │   ├── SaleMasterRepository.java
    │   │   ├── SaleOrderMasterRepository.java
    │   │   └── SaleCreditMasterRepository.java
    │   │
    │   ├── 📂 service/
    │   │   ├── SaleMasterService.java
    │   │   ├── SaleOrderMasterService.java
    │   │   ├── SaleCreditMasterService.java
    │   │   └── 📂 impl/
    │   │       ├── SaleMasterServiceImpl.java
    │   │       └── SaleOrderMasterServiceImpl.java
    │   │
    │   └── 📂 specification/
    │       └── SaleOrderSpecification.java
    │
    ├── 📂 purchase/                    # ──────────────────────────────
    │   │                               # PURCHASE MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── PurchaseMasterController.java
    │   │   ├── PurchaseDetailsController.java
    │   │   ├── PurchaseOrderMasterController.java
    │   │   └── PurchaseOrderDetailsController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 payment/                     # ──────────────────────────────
    │   │                               # PAYMENT & RECEIPT MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── PaymentController.java
    │   │   ├── PaymentVoucherController.java
    │   │   ├── ReceiptController.java
    │   │   ├── PettyCashMasterController.java
    │   │   └── PendingPaymentController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 customer/                    # ──────────────────────────────
    │   │                               # CUSTOMER MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── CustomerController.java
    │   │   ├── CustomerQuotationController.java
    │   │   ├── CustomerQuotationMasterController.java
    │   │   ├── CustomerJobNotifyController.java
    │   │   └── EnquiryMasterController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 supplier/                    # ──────────────────────────────
    │   │                               # SUPPLIER MODULE
    │   │
    │   ├── 📂 controller/
    │   │   └── SupplierController.java
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 inventory/                   # ──────────────────────────────
    │   │                               # INVENTORY & STOCK MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── ItemMasterController.java
    │   │   ├── ProductMasterController.java
    │   │   ├── StockInController.java
    │   │   ├── ItemMasterCStockController.java
    │   │   └── ProductMasterCStockController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 job/                         # ──────────────────────────────
    │   │                               # JOB MANAGEMENT MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── JobDetailsController.java
    │   │   ├── JobStatusMasterController.java
    │   │   ├── JobStatusDetailsController.java
    │   │   ├── JobTypeMasterController.java
    │   │   └── ItemMasterJobDetailsController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 planning/                    # ──────────────────────────────
    │   │                               # PLANNING MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── PlanningMasterController.java
    │   │   ├── PlanningDetailsController.java
    │   │   ├── VesselPlanningController.java
    │   │   └── PreAlertController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 fleet/                       # ──────────────────────────────
    │   │                               # TRUCK & FLEET MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── TruckMasterController.java
    │   │   ├── TruckSparePartsController.java
    │   │   ├── DriverMasterController.java
    │   │   ├── FuelEntryController.java
    │   │   ├── FuelFillingsController.java
    │   │   ├── TollEntryController.java
    │   │   ├── EngineHoursController.java
    │   │   ├── SpeedReportController.java
    │   │   ├── LicenseMasterController.java
    │   │   └── SummonController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 employee/                    # ──────────────────────────────
    │   │                               # EMPLOYEE & HR MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── EmployeeMasterController.java
    │   │   ├── SalaryEntryController.java
    │   │   ├── ForwardingSalaryController.java
    │   │   └── CashierController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 accounting/                  # ──────────────────────────────
    │   │                               # ACCOUNTING MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── AccountController.java
    │   │   ├── AccountsGroupMasterController.java
    │   │   ├── GLAccountsController.java
    │   │   ├── ExpenseMasterController.java
    │   │   ├── ExpenseEntryController.java
    │   │   ├── SubExpenseMasterController.java
    │   │   └── ClaimVoucherController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 billing/                     # ──────────────────────────────
    │   │                               # BILLING MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── BillMasterController.java
    │   │   ├── BillDetailsController.java
    │   │   ├── BillsOrderMasterController.java
    │   │   └── BillsOrderDetailsController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 master/                      # ──────────────────────────────
    │   │                               # MASTER DATA MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── BankMasterController.java
    │   │   ├── CountryMasterController.java
    │   │   ├── LocationMasterController.java
    │   │   ├── PortMasterController.java
    │   │   ├── TaxMasterController.java
    │   │   ├── UomController.java
    │   │   ├── CurrencyValueController.java
    │   │   ├── SymbolMasterController.java
    │   │   ├── MSICCodeController.java
    │   │   ├── ClassificationController.java
    │   │   ├── PaymentTermsMasterController.java
    │   │   ├── RulesTypeMasterController.java
    │   │   ├── CardMasterController.java
    │   │   ├── CounterController.java
    │   │   ├── DoMasterController.java
    │   │   ├── SequenceNoMasterController.java
    │   │   └── AddressMasterController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 user/                        # ──────────────────────────────
    │   │                               # USER MANAGEMENT MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── AppUserController.java
    │   │   ├── AuthUserController.java
    │   │   ├── MENUMasterController.java
    │   │   ├── MENUPrivilegeController.java
    │   │   └── FormTransactionPasswordController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 company/                     # ──────────────────────────────
    │   │                               # COMPANY & SETTINGS MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── CompanyController.java
    │   │   ├── CompanySettingsController.java
    │   │   ├── MainSettingController.java
    │   │   ├── MasterSettingController.java
    │   │   └── AgentController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 agentcompany/                # ──────────────────────────────
    │   │                               # AGENT COMPANY MODULE
    │   │
    │   ├── 📂 common/
    │   ├── 📂 controller/
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    ├── 📂 communication/               # ──────────────────────────────
    │   │                               # COMMUNICATION MODULE
    │   │
    │   ├── 📂 controller/
    │   │   ├── EmailInboxController.java
    │   │   └── PhoneCallEntryController.java
    │   │
    │   ├── 📂 dto/
    │   ├── 📂 entity/
    │   ├── 📂 mapper/
    │   ├── 📂 repository/
    │   └── 📂 service/
    │
    └── 📂 file/                        # ──────────────────────────────
        │                               # FILE MANAGEMENT MODULE
        │
        ├── 📂 controller/
        │   ├── FileUploadController.java
        │   └── ImageUploadController.java
        │
        ├── 📂 dto/
        ├── 📂 entity/
        ├── 📂 mapper/
        ├── 📂 repository/
        └── 📂 service/
```

---

## 📋 MODULE SUMMARY TABLE

| Module | Description | Controllers |
|--------|-------------|-------------|
| `common` | Shared utilities, configs, exceptions | - |
| `security` | Authentication, JWT, Authorization | 1 |
| `integration` | External API integrations (QNE, etc.) | - |
| `sale` | Sales, Orders, Credits, Delivery | 10 |
| `purchase` | Purchase orders, Purchase details | 4 |
| `payment` | Payments, Receipts, Vouchers, Petty Cash | 8 |
| `customer` | Customer management, Quotations | 5 |
| `supplier` | Supplier management | 1 |
| `inventory` | Items, Products, Stock management | 5 |
| `job` | Job management, Status tracking | 5 |
| `planning` | Planning, Vessel planning, Pre-alerts | 4 |
| `fleet` | Trucks, Drivers, Fuel, Tolls | 10 |
| `employee` | Employee management, Salary, HR | 4 |
| `accounting` | Accounts, GL, Expenses, Claims | 7 |
| `billing` | Bills, Bill orders | 4 |
| `master` | Master data (Bank, Country, Tax, etc.) | 17 |
| `user` | User management, Menus, Privileges | 5 |
| `company` | Company settings, Agents | 5 |
| `agentcompany` | Agent company management | 1 |
| `communication` | Email, Phone calls | 2 |
| `file` | File & Image uploads | 2 |

---

## 🏗️ STANDARD MODULE STRUCTURE

Each module should follow this internal structure:

```
📂 module_name/
│
├── 📂 controller/           # REST API endpoints
│   └── XxxController.java
│
├── 📂 dto/                  # Data Transfer Objects
│   ├── 📂 request/          # Request DTOs
│   │   └── XxxRequest.java
│   ├── 📂 response/         # Response DTOs
│   │   └── XxxResponse.java
│   └── XxxDto.java          # General DTOs
│
├── 📂 entity/               # JPA Entities (Database models)
│   └── Xxx.java
│
├── 📂 mapper/               # Object mappers (Entity <-> DTO)
│   └── XxxMapper.java
│
├── 📂 repository/           # Data access layer
│   └── XxxRepository.java
│
├── 📂 service/              # Business logic layer
│   ├── XxxService.java      # Interface
│   └── 📂 impl/
│       └── XxxServiceImpl.java
│
└── 📂 specification/        # JPA Specifications (if needed)
    └── XxxSpecification.java
```

---

## 📝 NAMING CONVENTIONS

### Files

| Type | Pattern | Example |
|------|---------|---------|
| Entity | `{Name}.java` | `Customer.java` |
| DTO | `{Name}Dto.java` | `CustomerDto.java` |
| Request DTO | `{Name}Request.java` | `CustomerCreateRequest.java` |
| Response DTO | `{Name}Response.java` | `CustomerResponse.java` |
| Controller | `{Name}Controller.java` | `CustomerController.java` |
| Service Interface | `{Name}Service.java` | `CustomerService.java` |
| Service Impl | `{Name}ServiceImpl.java` | `CustomerServiceImpl.java` |
| Repository | `{Name}Repository.java` | `CustomerRepository.java` |
| Mapper | `{Name}Mapper.java` | `CustomerMapper.java` |
| Specification | `{Name}Specification.java` | `CustomerSpecification.java` |

### Packages

```
my.maleva.api.module.{module_name}.{layer}

Examples:
my.maleva.api.module.sale.controller
my.maleva.api.module.sale.service.impl
my.maleva.api.module.customer.dto.request
```

---

## 🔧 CODING STANDARDS

### 1. Controller Layer

```java
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer Management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getAllCustomers() {
        List<CustomerDto> customers = customerService.findAll();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(
            @PathVariable Long id) {
        CustomerDto customer = customerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @PostMapping
    @Operation(summary = "Create new customer")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {
        CustomerDto customer = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customer));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerDto customer = customerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 2. Service Layer

```java
// Interface
public interface CustomerService {
    List<CustomerDto> findAll();
    CustomerDto findById(Long id);
    CustomerDto create(CustomerCreateRequest request);
    CustomerDto update(Long id, CustomerUpdateRequest request);
    void delete(Long id);
}

// Implementation
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDto> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer", id));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerDto create(CustomerCreateRequest request) {
        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CustomerDto update(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer", id));
        customerMapper.updateEntity(customer, request);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer", id);
        }
        customerRepository.deleteById(id);
    }
}
```

### 3. Repository Layer

```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByCode(String code);
    
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Customer c WHERE c.status = :status")
    List<Customer> findByStatus(@Param("status") String status);
    
    Page<Customer> findAll(Specification<Customer> spec, Pageable pageable);
}
```

### 4. Entity Layer

```java
@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 5. DTO Layer

```java
// General DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private Long id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {
    
    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    private String address;
}
```

### 6. Mapper Layer

```java
@Mapper(componentModel = "spring")
public interface CustomerMapper {
    
    CustomerDto toDto(Customer entity);
    
    List<CustomerDto> toDtoList(List<Customer> entities);
    
    Customer toEntity(CustomerCreateRequest request);
    
    void updateEntity(@MappingTarget Customer entity, CustomerUpdateRequest request);
}
```

---

## 📊 API RESPONSE STANDARD

### Standard Response Wrapper

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

### Pagination Response

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
```

---

## 🚨 EXCEPTION HANDLING

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(
            InvalidRequestException ex) {
        log.error("Invalid request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
```

---

## 📋 LOGGING CONFIGURATION (Logback)

### Overview
Maleva uses **Logback** as the primary logging framework configured via `logback-spring.xml` in `src/main/resources/`. The configuration supports different logging behaviors per Spring profile:

- **Development Profile** (default): Console output with INFO level
- **Production Profile** (`prod`): Rolling file output with WARN level

### Log File Storage and Rotation
- **Location**: `logs/` directory in the application root (created automatically)
- **Naming Pattern**: `application-YYYY-MM-DD_HH.log` (hourly rotation)
- **Max File Size**: 100MB per file (size-based rotation)
- **Retention**: 2400 files maximum (≈100 days of logs at hourly rollover)
- **Charset**: UTF-8 (supports international characters)

### Log Message Format
All log messages follow this pattern:
```
[YYYY-MM-DD HH:mm:ss.SSS] [thread-name] LEVEL logger-name - message
```

Example:
```
[2026-06-16 14:30:45.123] [http-nio-8082-exec-1] INFO  m.m.a.m.c.UserController - User login successful
```

### Using Loggers in Code
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    
    public void processCustomer(Long id) {
        log.info("Processing customer: {}", id);
        try {
            // Business logic
        } catch (Exception e) {
            log.error("Error processing customer: {}", id, e);
        }
    }
}
```

**With Lombok**: Add `@Slf4j` annotation to auto-generate logger:
```java
@Slf4j
public class CustomerService {
    public void processCustomer(Long id) {
        log.info("Processing customer: {}", id);
        // ...
    }
}
```

### Development Mode
Run with default configuration or explicitly with `dev` profile:
```bash
./mvnw spring-boot:run                          # Default: development mode
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```
**Output**: Logs appear in console (stdout) with INFO level and above

### Production Mode
Run with `prod` profile:
```bash
java -jar target/maleva-*.jar --spring.profiles.active=prod
```
**Output**: Logs written to rolling files in `logs/` directory with WARN level and above

### Enabling DEBUG Logging for Modules
For troubleshooting, enable DEBUG logging for specific modules by setting environment variables or modifying `application.yaml`:

```yaml
logging:
  level:
    my.maleva.api.module.payment: DEBUG           # Debug payment module
    my.maleva.api.integration.qne: DEBUG          # Debug QNE integration
```

### Log Retention and Disk Space
- **Storage Calculation**: 2400 files × 100MB = 240GB maximum
- **Retention Period**: ~100 days (2400 hours ÷ 24 hours/day)
- **Automatic Cleanup**: Files older than the retention limit are automatically deleted

Monitor disk space in production:
```bash
du -sh logs/                    # Linux/macOS
dir /s logs/                    # Windows
```

### Best Practices
1. **Use appropriate log levels**:
   - `DEBUG`: Detailed diagnostic info (not visible in production by default)
   - `INFO`: General informational messages (customer actions, important milestones)
   - `WARN`: Warning conditions that should be investigated (deprecated API usage)
   - `ERROR`: Error events (exceptions, failed operations)

2. **Use structured logging with parameters**:
   ```java
   log.info("Customer created: id={}, name={}, email={}", id, name, email);  // Good
   log.info("Customer created: " + id + ", " + name);                         // Avoid string concatenation
   ```

3. **Don't log sensitive data** (PII/passwords):
   ```java
   log.info("User authenticated: {}", username);              // OK - username is not sensitive
   log.info("API key: {}", apiKey);                          // BAD - don't log secrets
   ```

4. **Include context in error logs**:
   ```java
   log.error("Failed to process order: id={}, status={}, error={}", orderId, status, e.getMessage(), e);
   ```

---

## ✅ CHECKLIST FOR NEW FEATURES

When adding a new feature/module:

- [ ] Create module folder under `module/`
- [ ] Create subfolders: `controller`, `dto`, `entity`, `mapper`, `repository`, `service`
- [ ] Create Entity class with proper JPA annotations
- [ ] Create DTO classes (with validation annotations)
- [ ] Create Mapper interface (MapStruct)
- [ ] Create Repository interface
- [ ] Create Service interface and implementation
- [ ] Create Controller with proper REST mappings
- [ ] Add Swagger/OpenAPI annotations
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Update API documentation

---

## 📚 REFERENCES

- [Spring Boot Best Practices](https://spring.io/guides)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)

---

**Document maintained by:** Development Team  
**Last Review:** March 26, 2026

