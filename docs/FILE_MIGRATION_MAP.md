# 📋 Maleva Project - Complete File Migration Map

> **Purpose:** Shows EXACTLY where each file should be placed in the new module-based structure  
> **Total Files:** 500+ files mapped

---

## 🗂️ LEGEND

| Symbol | Meaning |
|--------|---------|
| 📄 | File |
| 📂 | Folder |
| ✅ | Keep in same location |
| ➡️ | Move to new location |
| 🆕 | New file to create |

---

## 📁 ROOT APPLICATION FILES

| Current Location | New Location | Status |
|------------------|--------------|--------|
| `MalevaApplication.java` | `MalevaApplication.java` | ✅ Keep |
| `ServletInitializer.java` | `ServletInitializer.java` | ✅ Keep |
| `WelcomeController.java` | `common/controller/WelcomeController.java` | ➡️ Move |

---

## 📁 COMMON MODULE

### 📂 common/config/

| Current Location | New Location |
|------------------|--------------|
| `config/DataSourceConfig.java` | `common/config/DataSourceConfig.java` |
| `config/FileUploadConfig.java` | `common/config/FileUploadConfig.java` |
| `config/JpaConfig.java` | `common/config/JpaConfig.java` |
| `config/QneProperties.java` | `common/config/QneProperties.java` |
| `config/RedisConfig.java` | `common/config/RedisConfig.java` |
| `config/RedisStartupChecker.java` | `common/config/RedisStartupChecker.java` |
| `config/SecurityConfig.java` | `security/config/SecurityConfig.java` |

### 📂 common/constant/

| Current Location | New Location |
|------------------|--------------|
| `constant/ApiConstants.java` | `common/constant/ApiConstants.java` |
| `util/AppConstants.java` | `common/constant/AppConstants.java` |
| `util/UserRoles.java` | `common/constant/UserRoles.java` |

### 📂 common/exception/

| Current Location | New Location |
|------------------|--------------|
| `exception/ApiError.java` | `common/exception/ApiError.java` |
| `exception/EntityNotFoundException.java` | `common/exception/EntityNotFoundException.java` |
| `exception/GlobalExceptionHandler.java` | `common/exception/GlobalExceptionHandler.java` |
| `exception/InvalidRequestException.java` | `common/exception/InvalidRequestException.java` |

### 📂 common/util/

| Current Location | New Location |
|------------------|--------------|
| `util/DateTimeUtil.java` | `common/util/DateTimeUtil.java` |
| `util/SaleOrderFormatter.java` | `module/sale/util/SaleOrderFormatter.java` |
| `util/StringUtils.java` | `common/util/StringUtils.java` |

### 📂 common/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/ApiResponse.java` | `common/dto/ApiResponse.java` |
| `dto/ComboListModel.java` | `common/dto/ComboListModel.java` |

---

## 📁 SECURITY MODULE

### 📂 security/

| Current Location | New Location |
|------------------|--------------|
| `auth/AuthController.java` | `security/controller/AuthController.java` |
| `auth/JwtAuthenticationFilter.java` | `security/filter/JwtAuthenticationFilter.java` |
| `auth/JwtService.java` | `security/service/JwtService.java` |
| `auth/LoginRequest.java` | `security/dto/LoginRequest.java` |
| `auth/RedisTokenStore.java` | `security/service/RedisTokenStore.java` |
| `auth/TokenStore.java` | `security/service/TokenStore.java` |
| `config/SecurityConfig.java` | `security/config/SecurityConfig.java` |

---

## 📁 INTEGRATION MODULE

### 📂 integration/qne/

| Current Location | New Location |
|------------------|--------------|
| `integration/qne/QneUrlBuilder.java` | `integration/qne/QneUrlBuilder.java` |

---

## 📁 SALE MODULE (`module/sale/`)

### 📂 module/sale/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/SaleMasterController.java` | `module/sale/controller/SaleMasterController.java` |
| `controller/SaleDetailsController.java` | `module/sale/controller/SaleDetailsController.java` |
| `controller/SaleOrderMasterController.java` | `module/sale/controller/SaleOrderMasterController.java` |
| `controller/SaleOrderDetailsController.java` | `module/sale/controller/SaleOrderDetailsController.java` |
| `controller/SaleCreditMasterController.java` | `module/sale/controller/SaleCreditMasterController.java` |
| `controller/SaleCreditDetailsController.java` | `module/sale/controller/SaleCreditDetailsController.java` |
| `controller/SaleCreditKnockOffController.java` | `module/sale/controller/SaleCreditKnockOffController.java` |
| `controller/SaleOrderBOController.java` | `module/sale/controller/SaleOrderBOController.java` |
| `controller/SaleOrderBONotRequiredController.java` | `module/sale/controller/SaleOrderBONotRequiredController.java` |
| `controller/SaleOrderDeliveryController.java` | `module/sale/controller/SaleOrderDeliveryController.java` |
| `controller/SaleOrderForwardingController.java` | `module/sale/controller/SaleOrderForwardingController.java` |
| `controller/SaleOrderPickupController.java` | `module/sale/controller/SaleOrderPickupController.java` |
| `controller/SaleMasterReferenceController.java` | `module/sale/controller/SaleMasterReferenceController.java` |
| `controller/SportSaleOrderController.java` | `module/sale/controller/SportSaleOrderController.java` |

### 📂 module/sale/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/SaleMasterDto.java` | `module/sale/dto/SaleMasterDto.java` |
| `dto/SaleDetailsDto.java` | `module/sale/dto/SaleDetailsDto.java` |
| `dto/SaleOrderMasterDto.java` | `module/sale/dto/SaleOrderMasterDto.java` |
| `dto/SaleOrderDetailsDto.java` | `module/sale/dto/SaleOrderDetailsDto.java` |
| `dto/SaleCreditMasterDto.java` | `module/sale/dto/SaleCreditMasterDto.java` |
| `dto/SaleCreditDetailsDto.java` | `module/sale/dto/SaleCreditDetailsDto.java` |
| `dto/SaleCreditKnockOffDto.java` | `module/sale/dto/SaleCreditKnockOffDto.java` |
| `dto/SaleOrderBODto.java` | `module/sale/dto/SaleOrderBODto.java` |
| `dto/SaleOrderBONotRequiredDto.java` | `module/sale/dto/SaleOrderBONotRequiredDto.java` |
| `dto/SaleOrderDeliveryDto.java` | `module/sale/dto/SaleOrderDeliveryDto.java` |
| `dto/SaleOrderForwardingDto.java` | `module/sale/dto/SaleOrderForwardingDto.java` |
| `dto/SaleOrderPickupDto.java` | `module/sale/dto/SaleOrderPickupDto.java` |
| `dto/SaleMasterReferenceDto.java` | `module/sale/dto/SaleMasterReferenceDto.java` |
| `dto/SportSaleOrderDto.java` | `module/sale/dto/SportSaleOrderDto.java` |
| `dto/SaleDetailsViewModel.java` | `module/sale/dto/SaleDetailsViewModel.java` |
| `dto/SaleMasterViewModel.java` | `module/sale/dto/SaleMasterViewModel.java` |
| `dto/SaleF5View.java` | `module/sale/dto/SaleF5View.java` |
| `dto/SaleOrderDTO.java` | `module/sale/dto/SaleOrderDTO.java` |
| `dto/SaleOrderFilterDTO.java` | `module/sale/dto/request/SaleOrderFilterDTO.java` |
| `dto/DeliveryDetailDTO.java` | `module/sale/dto/DeliveryDetailDTO.java` |
| `dto/ForwardingDetailDTO.java` | `module/sale/dto/ForwardingDetailDTO.java` |
| `dto/PickupDetailDTO.java` | `module/sale/dto/PickupDetailDTO.java` |

### 📂 module/sale/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/SaleMaster.java` | `module/sale/entity/SaleMaster.java` |
| `model/SaleDetails.java` | `module/sale/entity/SaleDetails.java` |
| `model/SaleOrderMaster.java` | `module/sale/entity/SaleOrderMaster.java` |
| `model/SaleOrderDetails.java` | `module/sale/entity/SaleOrderDetails.java` |
| `model/SaleCreditMaster.java` | `module/sale/entity/SaleCreditMaster.java` |
| `model/SaleCreditDetails.java` | `module/sale/entity/SaleCreditDetails.java` |
| `model/SaleCreditKnockOff.java` | `module/sale/entity/SaleCreditKnockOff.java` |
| `model/SaleOrderBO.java` | `module/sale/entity/SaleOrderBO.java` |
| `model/SaleOrderBONotRequired.java` | `module/sale/entity/SaleOrderBONotRequired.java` |
| `model/SaleOrderDelivery.java` | `module/sale/entity/SaleOrderDelivery.java` |
| `model/SaleOrderForwarding.java` | `module/sale/entity/SaleOrderForwarding.java` |
| `model/SaleOrderPickup.java` | `module/sale/entity/SaleOrderPickup.java` |
| `model/SaleMasterReference.java` | `module/sale/entity/SaleMasterReference.java` |
| `model/SportSaleOrder.java` | `module/sale/entity/SportSaleOrder.java` |

### 📂 module/sale/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/SaleMasterMapper.java` | `module/sale/mapper/SaleMasterMapper.java` |
| `mapper/SaleDetailsMapper.java` | `module/sale/mapper/SaleDetailsMapper.java` |
| `mapper/SaleOrderMasterMapper.java` | `module/sale/mapper/SaleOrderMasterMapper.java` |
| `mapper/SaleOrderDetailsMapper.java` | `module/sale/mapper/SaleOrderDetailsMapper.java` |
| `mapper/SaleCreditMasterMapper.java` | `module/sale/mapper/SaleCreditMasterMapper.java` |
| `mapper/SaleCreditDetailsMapper.java` | `module/sale/mapper/SaleCreditDetailsMapper.java` |
| `mapper/SaleCreditKnockOffMapper.java` | `module/sale/mapper/SaleCreditKnockOffMapper.java` |
| `mapper/SaleOrderBOMapper.java` | `module/sale/mapper/SaleOrderBOMapper.java` |
| `mapper/SaleOrderBONotRequiredMapper.java` | `module/sale/mapper/SaleOrderBONotRequiredMapper.java` |
| `mapper/SaleOrderDeliveryMapper.java` | `module/sale/mapper/SaleOrderDeliveryMapper.java` |
| `mapper/SaleOrderForwardingMapper.java` | `module/sale/mapper/SaleOrderForwardingMapper.java` |
| `mapper/SaleOrderPickupMapper.java` | `module/sale/mapper/SaleOrderPickupMapper.java` |
| `mapper/SaleMasterReferenceMapper.java` | `module/sale/mapper/SaleMasterReferenceMapper.java` |
| `mapper/SportSaleOrderMapper.java` | `module/sale/mapper/SportSaleOrderMapper.java` |
| `mapper/SaleF5ViewMapper.java` | `module/sale/mapper/SaleF5ViewMapper.java` |

### 📂 module/sale/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/SaleMasterRepository.java` | `module/sale/repository/SaleMasterRepository.java` |
| `repo/SaleDetailsRepository.java` | `module/sale/repository/SaleDetailsRepository.java` |
| `repo/SaleOrderMasterRepository.java` | `module/sale/repository/SaleOrderMasterRepository.java` |
| `repo/SaleOrderDetailsRepository.java` | `module/sale/repository/SaleOrderDetailsRepository.java` |
| `repo/SaleCreditMasterRepository.java` | `module/sale/repository/SaleCreditMasterRepository.java` |
| `repo/SaleCreditDetailsRepository.java` | `module/sale/repository/SaleCreditDetailsRepository.java` |
| `repo/SaleCreditKnockOffRepository.java` | `module/sale/repository/SaleCreditKnockOffRepository.java` |
| `repo/SaleOrderBORepository.java` | `module/sale/repository/SaleOrderBORepository.java` |
| `repo/SaleOrderBONotRequiredRepository.java` | `module/sale/repository/SaleOrderBONotRequiredRepository.java` |
| `repo/SaleOrderDeliveryRepository.java` | `module/sale/repository/SaleOrderDeliveryRepository.java` |
| `repo/SaleOrderForwardingRepository.java` | `module/sale/repository/SaleOrderForwardingRepository.java` |
| `repo/SaleOrderPickupRepository.java` | `module/sale/repository/SaleOrderPickupRepository.java` |
| `repo/SaleMasterReferenceRepository.java` | `module/sale/repository/SaleMasterReferenceRepository.java` |
| `repo/SportSaleOrderRepository.java` | `module/sale/repository/SportSaleOrderRepository.java` |

### 📂 module/sale/service/

| Current Location | New Location |
|------------------|--------------|
| `service/SaleMasterService.java` | `module/sale/service/SaleMasterService.java` |
| `service/SaleDetailsService.java` | `module/sale/service/SaleDetailsService.java` |
| `service/SaleOrderMasterService.java` | `module/sale/service/SaleOrderMasterService.java` |
| `service/SaleOrderDetailsService.java` | `module/sale/service/SaleOrderDetailsService.java` |
| `service/SaleCreditMasterService.java` | `module/sale/service/SaleCreditMasterService.java` |
| `service/SaleCreditDetailsService.java` | `module/sale/service/SaleCreditDetailsService.java` |
| `service/SaleCreditKnockOffService.java` | `module/sale/service/SaleCreditKnockOffService.java` |
| `service/SaleOrderBOService.java` | `module/sale/service/SaleOrderBOService.java` |
| `service/SaleOrderBONotRequiredService.java` | `module/sale/service/SaleOrderBONotRequiredService.java` |
| `service/SaleOrderDeliveryService.java` | `module/sale/service/SaleOrderDeliveryService.java` |
| `service/SaleOrderForwardingService.java` | `module/sale/service/SaleOrderForwardingService.java` |
| `service/SaleOrderPickupService.java` | `module/sale/service/SaleOrderPickupService.java` |
| `service/SaleMasterReferenceService.java` | `module/sale/service/SaleMasterReferenceService.java` |
| `service/SportSaleOrderService.java` | `module/sale/service/SportSaleOrderService.java` |
| `service/impl/SaleMasterServiceImpl.java` | `module/sale/service/impl/SaleMasterServiceImpl.java` |
| `service/impl/SaleDetailsServiceImpl.java` | `module/sale/service/impl/SaleDetailsServiceImpl.java` |
| `service/impl/SaleOrderMasterServiceImpl.java` | `module/sale/service/impl/SaleOrderMasterServiceImpl.java` |
| `service/impl/SaleOrderDetailsServiceImpl.java` | `module/sale/service/impl/SaleOrderDetailsServiceImpl.java` |
| `service/impl/SaleCreditMasterServiceImpl.java` | `module/sale/service/impl/SaleCreditMasterServiceImpl.java` |
| `service/impl/SaleCreditDetailsServiceImpl.java` | `module/sale/service/impl/SaleCreditDetailsServiceImpl.java` |
| `service/impl/SaleCreditKnockOffServiceImpl.java` | `module/sale/service/impl/SaleCreditKnockOffServiceImpl.java` |
| `service/impl/SaleOrderBOServiceImpl.java` | `module/sale/service/impl/SaleOrderBOServiceImpl.java` |
| `service/impl/SaleOrderBONotRequiredServiceImpl.java` | `module/sale/service/impl/SaleOrderBONotRequiredServiceImpl.java` |
| `service/impl/SaleOrderDeliveryServiceImpl.java` | `module/sale/service/impl/SaleOrderDeliveryServiceImpl.java` |
| `service/impl/SaleOrderForwardingServiceImpl.java` | `module/sale/service/impl/SaleOrderForwardingServiceImpl.java` |
| `service/impl/SaleOrderPickupServiceImpl.java` | `module/sale/service/impl/SaleOrderPickupServiceImpl.java` |
| `service/impl/SaleMasterReferenceServiceImpl.java` | `module/sale/service/impl/SaleMasterReferenceServiceImpl.java` |
| `service/impl/SportSaleOrderServiceImpl.java` | `module/sale/service/impl/SportSaleOrderServiceImpl.java` |
| `service/helper/SaleOrderFilterHelper.java` | `module/sale/service/helper/SaleOrderFilterHelper.java` |

### 📂 module/sale/specification/

| Current Location | New Location |
|------------------|--------------|
| `specification/SaleOrderSpecification.java` | `module/sale/specification/SaleOrderSpecification.java` |

### 📂 module/sale/util/

| Current Location | New Location |
|------------------|--------------|
| `util/SaleOrderFormatter.java` | `module/sale/util/SaleOrderFormatter.java` |

---

## 📁 PURCHASE MODULE (`module/purchase/`)

### 📂 module/purchase/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/PurchaseMasterController.java` | `module/purchase/controller/PurchaseMasterController.java` |
| `controller/PurchaseDetailsController.java` | `module/purchase/controller/PurchaseDetailsController.java` |
| `controller/PurchaseOrderMasterController.java` | `module/purchase/controller/PurchaseOrderMasterController.java` |
| `controller/PurchaseOrderDetailsController.java` | `module/purchase/controller/PurchaseOrderDetailsController.java` |

### 📂 module/purchase/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/PurchaseMasterDto.java` | `module/purchase/dto/PurchaseMasterDto.java` |
| `dto/PurchaseDetailsDto.java` | `module/purchase/dto/PurchaseDetailsDto.java` |
| `dto/PurchaseOrderMasterDto.java` | `module/purchase/dto/PurchaseOrderMasterDto.java` |
| `dto/PurchaseOrderDetailsDto.java` | `module/purchase/dto/PurchaseOrderDetailsDto.java` |

### 📂 module/purchase/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/PurchaseMaster.java` | `module/purchase/entity/PurchaseMaster.java` |
| `model/PurchaseDetails.java` | `module/purchase/entity/PurchaseDetails.java` |
| `model/PurchaseOrderMaster.java` | `module/purchase/entity/PurchaseOrderMaster.java` |
| `model/PurchaseOrderDetails.java` | `module/purchase/entity/PurchaseOrderDetails.java` |

### 📂 module/purchase/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/PurchaseMasterMapper.java` | `module/purchase/mapper/PurchaseMasterMapper.java` |
| `mapper/PurchaseDetailsMapper.java` | `module/purchase/mapper/PurchaseDetailsMapper.java` |
| `mapper/PurchaseOrderMasterMapper.java` | `module/purchase/mapper/PurchaseOrderMasterMapper.java` |
| `mapper/PurchaseOrderDetailsMapper.java` | `module/purchase/mapper/PurchaseOrderDetailsMapper.java` |

### 📂 module/purchase/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/PurchaseMasterRepository.java` | `module/purchase/repository/PurchaseMasterRepository.java` |
| `repo/PurchaseDetailsRepository.java` | `module/purchase/repository/PurchaseDetailsRepository.java` |
| `repo/PurchaseOrderMasterRepository.java` | `module/purchase/repository/PurchaseOrderMasterRepository.java` |
| `repo/PurchaseOrderDetailsRepository.java` | `module/purchase/repository/PurchaseOrderDetailsRepository.java` |

### 📂 module/purchase/service/

| Current Location | New Location |
|------------------|--------------|
| `service/PurchaseMasterService.java` | `module/purchase/service/PurchaseMasterService.java` |
| `service/PurchaseDetailsService.java` | `module/purchase/service/PurchaseDetailsService.java` |
| `service/PurchaseOrderMasterService.java` | `module/purchase/service/PurchaseOrderMasterService.java` |
| `service/PurchaseOrderDetailsService.java` | `module/purchase/service/PurchaseOrderDetailsService.java` |
| `service/impl/PurchaseMasterServiceImpl.java` | `module/purchase/service/impl/PurchaseMasterServiceImpl.java` |
| `service/impl/PurchaseDetailsServiceImpl.java` | `module/purchase/service/impl/PurchaseDetailsServiceImpl.java` |
| `service/impl/PurchaseOrderMasterServiceImpl.java` | `module/purchase/service/impl/PurchaseOrderMasterServiceImpl.java` |
| `service/impl/PurchaseOrderDetailsServiceImpl.java` | `module/purchase/service/impl/PurchaseOrderDetailsServiceImpl.java` |

---

## 📁 PAYMENT MODULE (`module/payment/`)

### 📂 module/payment/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/PaymentController.java` | `module/payment/controller/PaymentController.java` |
| `controller/PaymentDetailsController.java` | `module/payment/controller/PaymentDetailsController.java` |
| `controller/PaymentVoucherController.java` | `module/payment/controller/PaymentVoucherController.java` |
| `controller/PaymentVoucherMasterController.java` | `module/payment/controller/PaymentVoucherMasterController.java` |
| `controller/PaymentVoucherDetailsController.java` | `module/payment/controller/PaymentVoucherDetailsController.java` |
| `controller/ReceiptController.java` | `module/payment/controller/ReceiptController.java` |
| `controller/ReceiptDetailsController.java` | `module/payment/controller/ReceiptDetailsController.java` |
| `controller/PettyCashMasterController.java` | `module/payment/controller/PettyCashMasterController.java` |
| `controller/PettyCashDetailController.java` | `module/payment/controller/PettyCashDetailController.java` |
| `controller/PendingPaymentController.java` | `module/payment/controller/PendingPaymentController.java` |
| `controller/PaymentReceiptInfoController.java` | `module/payment/controller/PaymentReceiptInfoController.java` |

### 📂 module/payment/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/PaymentDto.java` | `module/payment/dto/PaymentDto.java` |
| `dto/PaymentDetailsDto.java` | `module/payment/dto/PaymentDetailsDto.java` |
| `dto/PaymentVoucherDto.java` | `module/payment/dto/PaymentVoucherDto.java` |
| `dto/PaymentVoucherMasterDto.java` | `module/payment/dto/PaymentVoucherMasterDto.java` |
| `dto/PaymentVoucherDetailsDto.java` | `module/payment/dto/PaymentVoucherDetailsDto.java` |
| `dto/ReceiptDto.java` | `module/payment/dto/ReceiptDto.java` |
| `dto/ReceiptDetailsDto.java` | `module/payment/dto/ReceiptDetailsDto.java` |
| `dto/PettyCashMasterDto.java` | `module/payment/dto/PettyCashMasterDto.java` |
| `dto/PettyCashDetailDto.java` | `module/payment/dto/PettyCashDetailDto.java` |
| `dto/PendingPaymentDto.java` | `module/payment/dto/PendingPaymentDto.java` |
| `dto/PaymentReceiptInfoDto.java` | `module/payment/dto/PaymentReceiptInfoDto.java` |

### 📂 module/payment/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/Payment.java` | `module/payment/entity/Payment.java` |
| `model/PaymentDetails.java` | `module/payment/entity/PaymentDetails.java` |
| `model/PaymentVoucher.java` | `module/payment/entity/PaymentVoucher.java` |
| `model/PaymentVoucherMaster.java` | `module/payment/entity/PaymentVoucherMaster.java` |
| `model/PaymentVoucherDetails.java` | `module/payment/entity/PaymentVoucherDetails.java` |
| `model/Receipt.java` | `module/payment/entity/Receipt.java` |
| `model/ReceiptDetails.java` | `module/payment/entity/ReceiptDetails.java` |
| `model/PettyCashMaster.java` | `module/payment/entity/PettyCashMaster.java` |
| `model/PettyCashDetail.java` | `module/payment/entity/PettyCashDetail.java` |
| `model/PendingPayment.java` | `module/payment/entity/PendingPayment.java` |
| `model/PaymentReceiptInfo.java` | `module/payment/entity/PaymentReceiptInfo.java` |

### 📂 module/payment/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/PaymentMapper.java` | `module/payment/mapper/PaymentMapper.java` |
| `mapper/PaymentDetailsMapper.java` | `module/payment/mapper/PaymentDetailsMapper.java` |
| `mapper/PaymentVoucherMapper.java` | `module/payment/mapper/PaymentVoucherMapper.java` |
| `mapper/PaymentVoucherMasterMapper.java` | `module/payment/mapper/PaymentVoucherMasterMapper.java` |
| `mapper/PaymentVoucherDetailsMapper.java` | `module/payment/mapper/PaymentVoucherDetailsMapper.java` |
| `mapper/ReceiptMapper.java` | `module/payment/mapper/ReceiptMapper.java` |
| `mapper/ReceiptDetailsMapper.java` | `module/payment/mapper/ReceiptDetailsMapper.java` |
| `mapper/PettyCashMasterMapper.java` | `module/payment/mapper/PettyCashMasterMapper.java` |
| `mapper/PettyCashDetailMapper.java` | `module/payment/mapper/PettyCashDetailMapper.java` |
| `mapper/PendingPaymentMapper.java` | `module/payment/mapper/PendingPaymentMapper.java` |
| `mapper/PaymentReceiptInfoMapper.java` | `module/payment/mapper/PaymentReceiptInfoMapper.java` |

### 📂 module/payment/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/PaymentRepository.java` | `module/payment/repository/PaymentRepository.java` |
| `repo/PaymentDetailsRepository.java` | `module/payment/repository/PaymentDetailsRepository.java` |
| `repo/PaymentVoucherRepository.java` | `module/payment/repository/PaymentVoucherRepository.java` |
| `repo/PaymentVoucherMasterRepository.java` | `module/payment/repository/PaymentVoucherMasterRepository.java` |
| `repo/PaymentVoucherDetailsRepository.java` | `module/payment/repository/PaymentVoucherDetailsRepository.java` |
| `repo/ReceiptRepository.java` | `module/payment/repository/ReceiptRepository.java` |
| `repo/ReceiptDetailsRepository.java` | `module/payment/repository/ReceiptDetailsRepository.java` |
| `repo/PettyCashMasterRepository.java` | `module/payment/repository/PettyCashMasterRepository.java` |
| `repo/PettyCashDetailRepository.java` | `module/payment/repository/PettyCashDetailRepository.java` |
| `repo/PendingPaymentRepository.java` | `module/payment/repository/PendingPaymentRepository.java` |
| `repo/PaymentReceiptInfoRepository.java` | `module/payment/repository/PaymentReceiptInfoRepository.java` |

### 📂 module/payment/service/

| Current Location | New Location |
|------------------|--------------|
| `service/PaymentService.java` | `module/payment/service/PaymentService.java` |
| `service/PaymentDetailsService.java` | `module/payment/service/PaymentDetailsService.java` |
| `service/PaymentVoucherService.java` | `module/payment/service/PaymentVoucherService.java` |
| `service/PaymentVoucherMasterService.java` | `module/payment/service/PaymentVoucherMasterService.java` |
| `service/PaymentVoucherDetailsService.java` | `module/payment/service/PaymentVoucherDetailsService.java` |
| `service/ReceiptService.java` | `module/payment/service/ReceiptService.java` |
| `service/ReceiptDetailsService.java` | `module/payment/service/ReceiptDetailsService.java` |
| `service/PettyCashMasterService.java` | `module/payment/service/PettyCashMasterService.java` |
| `service/PettyCashDetailService.java` | `module/payment/service/PettyCashDetailService.java` |
| `service/PendingPaymentService.java` | `module/payment/service/PendingPaymentService.java` |
| `service/PaymentReceiptInfoService.java` | `module/payment/service/PaymentReceiptInfoService.java` |
| `service/impl/ReceiptServiceImpl.java` | `module/payment/service/impl/ReceiptServiceImpl.java` |
| `service/impl/ReceiptDetailsServiceImpl.java` | `module/payment/service/impl/ReceiptDetailsServiceImpl.java` |

---

## 📁 CUSTOMER MODULE (`module/customer/`)

### 📂 module/customer/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/CustomerController.java` | `module/customer/controller/CustomerController.java` |
| `controller/CustomerQuotationController.java` | `module/customer/controller/CustomerQuotationController.java` |
| `controller/CustomerQuotationMasterController.java` | `module/customer/controller/CustomerQuotationMasterController.java` |
| `controller/CustomerQuotationDetailsController.java` | `module/customer/controller/CustomerQuotationDetailsController.java` |
| `controller/CustomerQuotationGCController.java` | `module/customer/controller/CustomerQuotationGCController.java` |
| `controller/CustomerJobNotifyController.java` | `module/customer/controller/CustomerJobNotifyController.java` |
| `controller/CustomerNotifyDetailsController.java` | `module/customer/controller/CustomerNotifyDetailsController.java` |
| `controller/EnquiryMasterController.java` | `module/customer/controller/EnquiryMasterController.java` |

### 📂 module/customer/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/CustomerDto.java` | `module/customer/dto/CustomerDto.java` |
| `dto/CustomerQuotationDto.java` | `module/customer/dto/CustomerQuotationDto.java` |
| `dto/CustomerQuotationMasterDto.java` | `module/customer/dto/CustomerQuotationMasterDto.java` |
| `dto/CustomerQuotationDetailsDto.java` | `module/customer/dto/CustomerQuotationDetailsDto.java` |
| `dto/CustomerQuotationGCDto.java` | `module/customer/dto/CustomerQuotationGCDto.java` |
| `dto/CustomerJobNotifyDto.java` | `module/customer/dto/CustomerJobNotifyDto.java` |
| `dto/CustomerNotifyDetailsDto.java` | `module/customer/dto/CustomerNotifyDetailsDto.java` |
| `dto/EnquiryMasterDto.java` | `module/customer/dto/EnquiryMasterDto.java` |
| `dto/request/CustomerSelectRequest.java` | `module/customer/dto/request/CustomerSelectRequest.java` |
| `dto/response/CustomerSelectDto.java` | `module/customer/dto/response/CustomerSelectDto.java` |
| `dto/response/CustomerSelectResult.java` | `module/customer/dto/response/CustomerSelectResult.java` |
| `dto/response/CustomerInsertResult.java` | `module/customer/dto/response/CustomerInsertResult.java` |

### 📂 module/customer/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/Customer.java` | `module/customer/entity/Customer.java` |
| `model/CustomerQuotation.java` | `module/customer/entity/CustomerQuotation.java` |
| `model/CustomerQuotationMaster.java` | `module/customer/entity/CustomerQuotationMaster.java` |
| `model/CustomerQuotationDetails.java` | `module/customer/entity/CustomerQuotationDetails.java` |
| `model/CustomerQuotationGC.java` | `module/customer/entity/CustomerQuotationGC.java` |
| `model/CustomerJobNotify.java` | `module/customer/entity/CustomerJobNotify.java` |
| `model/CustomerNotifyDetails.java` | `module/customer/entity/CustomerNotifyDetails.java` |
| `model/EnquiryMaster.java` | `module/customer/entity/EnquiryMaster.java` |

### 📂 module/customer/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/CustomerMapper.java` | `module/customer/mapper/CustomerMapper.java` |
| `mapper/CustomerQuotationMapper.java` | `module/customer/mapper/CustomerQuotationMapper.java` |
| `mapper/CustomerQuotationMasterMapper.java` | `module/customer/mapper/CustomerQuotationMasterMapper.java` |
| `mapper/CustomerQuotationDetailsMapper.java` | `module/customer/mapper/CustomerQuotationDetailsMapper.java` |
| `mapper/CustomerQuotationGCMapper.java` | `module/customer/mapper/CustomerQuotationGCMapper.java` |
| `mapper/CustomerJobNotifyMapper.java` | `module/customer/mapper/CustomerJobNotifyMapper.java` |
| `mapper/CustomerNotifyDetailsMapper.java` | `module/customer/mapper/CustomerNotifyDetailsMapper.java` |
| `mapper/EnquiryMasterMapper.java` | `module/customer/mapper/EnquiryMasterMapper.java` |

### 📂 module/customer/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/CustomerRepository.java` | `module/customer/repository/CustomerRepository.java` |
| `repo/CustomerQueryRepository.java` | `module/customer/repository/CustomerQueryRepository.java` |
| `repo/CustomerQuotationRepository.java` | `module/customer/repository/CustomerQuotationRepository.java` |
| `repo/CustomerQuotationMasterRepository.java` | `module/customer/repository/CustomerQuotationMasterRepository.java` |
| `repo/CustomerQuotationDetailsRepository.java` | `module/customer/repository/CustomerQuotationDetailsRepository.java` |
| `repo/CustomerQuotationGCRepository.java` | `module/customer/repository/CustomerQuotationGCRepository.java` |
| `repo/CustomerJobNotifyRepository.java` | `module/customer/repository/CustomerJobNotifyRepository.java` |
| `repo/CustomerNotifyDetailsRepository.java` | `module/customer/repository/CustomerNotifyDetailsRepository.java` |
| `repo/EnquiryMasterRepository.java` | `module/customer/repository/EnquiryMasterRepository.java` |

### 📂 module/customer/service/

| Current Location | New Location |
|------------------|--------------|
| `service/CustomerService.java` | `module/customer/service/CustomerService.java` |
| `service/CustomerQuotationService.java` | `module/customer/service/CustomerQuotationService.java` |
| `service/CustomerQuotationMasterService.java` | `module/customer/service/CustomerQuotationMasterService.java` |
| `service/CustomerQuotationDetailsService.java` | `module/customer/service/CustomerQuotationDetailsService.java` |
| `service/CustomerQuotationGCService.java` | `module/customer/service/CustomerQuotationGCService.java` |
| `service/CustomerJobNotifyService.java` | `module/customer/service/CustomerJobNotifyService.java` |
| `service/CustomerNotifyDetailsService.java` | `module/customer/service/CustomerNotifyDetailsService.java` |
| `service/EnquiryMasterService.java` | `module/customer/service/EnquiryMasterService.java` |
| `service/impl/CustomerServiceImpl.java` | `module/customer/service/impl/CustomerServiceImpl.java` |

---

## 📁 SUPPLIER MODULE (`module/supplier/`)

### 📂 module/supplier/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/SupplierController.java` | `module/supplier/controller/SupplierController.java` |

### 📂 module/supplier/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/SupplierDto.java` | `module/supplier/dto/SupplierDto.java` |

### 📂 module/supplier/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/Supplier.java` | `module/supplier/entity/Supplier.java` |

### 📂 module/supplier/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/SupplierMapper.java` | `module/supplier/mapper/SupplierMapper.java` |

### 📂 module/supplier/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/SupplierRepository.java` | `module/supplier/repository/SupplierRepository.java` |

### 📂 module/supplier/service/

| Current Location | New Location |
|------------------|--------------|
| `service/SupplierService.java` | `module/supplier/service/SupplierService.java` |
| `service/impl/SupplierServiceImpl.java` | `module/supplier/service/impl/SupplierServiceImpl.java` |

---

## 📁 INVENTORY MODULE (`module/inventory/`)

### 📂 module/inventory/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/ItemMasterController.java` | `module/inventory/controller/ItemMasterController.java` |
| `controller/ItemMasterCStockController.java` | `module/inventory/controller/ItemMasterCStockController.java` |
| `controller/ProductMasterController.java` | `module/inventory/controller/ProductMasterController.java` |
| `controller/ProductMasterCStockController.java` | `module/inventory/controller/ProductMasterCStockController.java` |
| `controller/StockInController.java` | `module/inventory/controller/StockInController.java` |
| `controller/UomController.java` | `module/inventory/controller/UomController.java` |

### 📂 module/inventory/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/ItemMasterDto.java` | `module/inventory/dto/ItemMasterDto.java` |
| `dto/ItemMasterCStockDto.java` | `module/inventory/dto/ItemMasterCStockDto.java` |
| `dto/ProductMasterDto.java` | `module/inventory/dto/ProductMasterDto.java` |
| `dto/ProductMasterCStockDto.java` | `module/inventory/dto/ProductMasterCStockDto.java` |
| `dto/ProductListDto.java` | `module/inventory/dto/ProductListDto.java` |
| `dto/StockInDto.java` | `module/inventory/dto/StockInDto.java` |
| `dto/UomDto.java` | `module/inventory/dto/UomDto.java` |

### 📂 module/inventory/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/ItemMaster.java` | `module/inventory/entity/ItemMaster.java` |
| `model/ItemMasterCStock.java` | `module/inventory/entity/ItemMasterCStock.java` |
| `model/ProductMaster.java` | `module/inventory/entity/ProductMaster.java` |
| `model/ProductMasterCStock.java` | `module/inventory/entity/ProductMasterCStock.java` |
| `model/StockIn.java` | `module/inventory/entity/StockIn.java` |
| `model/Uom.java` | `module/inventory/entity/Uom.java` |

### 📂 module/inventory/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/ItemMasterMapper.java` | `module/inventory/mapper/ItemMasterMapper.java` |
| `mapper/ItemMasterCStockMapper.java` | `module/inventory/mapper/ItemMasterCStockMapper.java` |
| `mapper/ProductMasterMapper.java` | `module/inventory/mapper/ProductMasterMapper.java` |
| `mapper/ProductMasterCStockMapper.java` | `module/inventory/mapper/ProductMasterCStockMapper.java` |
| `mapper/ProductListMapper.java` | `module/inventory/mapper/ProductListMapper.java` |
| `mapper/StockInMapper.java` | `module/inventory/mapper/StockInMapper.java` |
| `mapper/UomMapper.java` | `module/inventory/mapper/UomMapper.java` |

### 📂 module/inventory/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/ItemMasterRepository.java` | `module/inventory/repository/ItemMasterRepository.java` |
| `repo/ItemMasterCStockRepository.java` | `module/inventory/repository/ItemMasterCStockRepository.java` |
| `repo/ProductMasterRepository.java` | `module/inventory/repository/ProductMasterRepository.java` |
| `repo/ProductMasterCStockRepository.java` | `module/inventory/repository/ProductMasterCStockRepository.java` |
| `repo/StockInRepository.java` | `module/inventory/repository/StockInRepository.java` |
| `repo/UomRepository.java` | `module/inventory/repository/UomRepository.java` |

### 📂 module/inventory/service/

| Current Location | New Location |
|------------------|--------------|
| `service/ItemMasterService.java` | `module/inventory/service/ItemMasterService.java` |
| `service/ItemMasterCStockService.java` | `module/inventory/service/ItemMasterCStockService.java` |
| `service/ProductMasterService.java` | `module/inventory/service/ProductMasterService.java` |
| `service/ProductMasterCStockService.java` | `module/inventory/service/ProductMasterCStockService.java` |
| `service/StockInService.java` | `module/inventory/service/StockInService.java` |
| `service/UomService.java` | `module/inventory/service/UomService.java` |
| `service/impl/ProductMasterServiceImpl.java` | `module/inventory/service/impl/ProductMasterServiceImpl.java` |
| `service/impl/ProductMasterCStockServiceImpl.java` | `module/inventory/service/impl/ProductMasterCStockServiceImpl.java` |
| `service/impl/StockInServiceImpl.java` | `module/inventory/service/impl/StockInServiceImpl.java` |

---

## 📁 JOB MODULE (`module/job/`)

### 📂 module/job/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/JobDetailsController.java` | `module/job/controller/JobDetailsController.java` |
| `controller/JobStatusMasterController.java` | `module/job/controller/JobStatusMasterController.java` |
| `controller/JobStatusDetailsController.java` | `module/job/controller/JobStatusDetailsController.java` |
| `controller/JobTypeMasterController.java` | `module/job/controller/JobTypeMasterController.java` |
| `controller/JobTypeAllDataController.java` | `module/job/controller/JobTypeAllDataController.java` |
| `controller/ItemMasterJobDetailsController.java` | `module/job/controller/ItemMasterJobDetailsController.java` |

### 📂 module/job/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/JobDetailsDto.java` | `module/job/dto/JobDetailsDto.java` |
| `dto/JobDetailsWithNameDto.java` | `module/job/dto/JobDetailsWithNameDto.java` |
| `dto/JobStatusMasterDto.java` | `module/job/dto/JobStatusMasterDto.java` |
| `dto/JobStatusDetailsDto.java` | `module/job/dto/JobStatusDetailsDto.java` |
| `dto/JobStatusDetailsWithNameDto.java` | `module/job/dto/JobStatusDetailsWithNameDto.java` |
| `dto/JobTypeMasterDto.java` | `module/job/dto/JobTypeMasterDto.java` |
| `dto/JobTypeAllDataDto.java` | `module/job/dto/JobTypeAllDataDto.java` |
| `dto/ItemMasterJobDetailsDto.java` | `module/job/dto/ItemMasterJobDetailsDto.java` |

### 📂 module/job/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/JobDetails.java` | `module/job/entity/JobDetails.java` |
| `model/JobStatusMaster.java` | `module/job/entity/JobStatusMaster.java` |
| `model/JobStatusDetails.java` | `module/job/entity/JobStatusDetails.java` |
| `model/JobTypeMaster.java` | `module/job/entity/JobTypeMaster.java` |
| `model/ItemMasterJobDetails.java` | `module/job/entity/ItemMasterJobDetails.java` |

### 📂 module/job/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/JobDetailsMapper.java` | `module/job/mapper/JobDetailsMapper.java` |
| `mapper/JobStatusMasterMapper.java` | `module/job/mapper/JobStatusMasterMapper.java` |
| `mapper/JobStatusDetailsMapper.java` | `module/job/mapper/JobStatusDetailsMapper.java` |
| `mapper/JobTypeMasterMapper.java` | `module/job/mapper/JobTypeMasterMapper.java` |
| `mapper/ItemMasterJobDetailsMapper.java` | `module/job/mapper/ItemMasterJobDetailsMapper.java` |

### 📂 module/job/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/JobDetailsRepository.java` | `module/job/repository/JobDetailsRepository.java` |
| `repo/JobStatusMasterRepository.java` | `module/job/repository/JobStatusMasterRepository.java` |
| `repo/JobStatusDetailsRepository.java` | `module/job/repository/JobStatusDetailsRepository.java` |
| `repo/JobTypeMasterRepository.java` | `module/job/repository/JobTypeMasterRepository.java` |
| `repo/ItemMasterJobDetailsRepository.java` | `module/job/repository/ItemMasterJobDetailsRepository.java` |

### 📂 module/job/service/

| Current Location | New Location |
|------------------|--------------|
| `service/JobDetailsService.java` | `module/job/service/JobDetailsService.java` |
| `service/JobStatusMasterService.java` | `module/job/service/JobStatusMasterService.java` |
| `service/JobStatusDetailsService.java` | `module/job/service/JobStatusDetailsService.java` |
| `service/JobTypeMasterService.java` | `module/job/service/JobTypeMasterService.java` |
| `service/JobTypeAllDataService.java` | `module/job/service/JobTypeAllDataService.java` |
| `service/ItemMasterJobDetailsService.java` | `module/job/service/ItemMasterJobDetailsService.java` |
| `service/impl/JobTypeAllDataServiceImpl.java` | `module/job/service/impl/JobTypeAllDataServiceImpl.java` |

---

## 📁 PLANNING MODULE (`module/planning/`)

### 📂 module/planning/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/PlanningMasterController.java` | `module/planning/controller/PlanningMasterController.java` |
| `controller/PlanningDetailsController.java` | `module/planning/controller/PlanningDetailsController.java` |
| `controller/PlaningController.java` | `module/planning/controller/PlaningController.java` |
| `controller/VesselPlanningController.java` | `module/planning/controller/VesselPlanningController.java` |
| `controller/PreAlertController.java` | `module/planning/controller/PreAlertController.java` |
| `controller/PreAlertMasterController.java` | `module/planning/controller/PreAlertMasterController.java` |
| `controller/RTIMasterController.java` | `module/planning/controller/RTIMasterController.java` |
| `controller/RTIDetailsController.java` | `module/planning/controller/RTIDetailsController.java` |

### 📂 module/planning/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/PlanningMasterDto.java` | `module/planning/dto/PlanningMasterDto.java` |
| `dto/PlanningDetailsDto.java` | `module/planning/dto/PlanningDetailsDto.java` |
| `dto/PlanningDetailsModel.java` | `module/planning/dto/PlanningDetailsModel.java` |
| `dto/PlanningMasterViewModel.java` | `module/planning/dto/PlanningMasterViewModel.java` |
| `dto/PlanningF5View.java` | `module/planning/dto/PlanningF5View.java` |
| `dto/VesselPlanningMasterDto.java` | `module/planning/dto/VesselPlanningMasterDto.java` |
| `dto/VesselPlanningDetailsDto.java` | `module/planning/dto/VesselPlanningDetailsDto.java` |
| `dto/PreAlertDto.java` | `module/planning/dto/PreAlertDto.java` |
| `dto/PreAlertMasterDto.java` | `module/planning/dto/PreAlertMasterDto.java` |
| `dto/RTIMasterDto.java` | `module/planning/dto/RTIMasterDto.java` |
| `dto/RTIDetailsDto.java` | `module/planning/dto/RTIDetailsDto.java` |
| `dto/request/PLANINGSearchRequestDto.java` | `module/planning/dto/request/PLANINGSearchRequestDto.java` |
| `dto/request/PlanningF5RequestDto.java` | `module/planning/dto/request/PlanningF5RequestDto.java` |
| `dto/PlaningNumberRequestDTO.java` | `module/planning/dto/request/PlaningNumberRequestDTO.java` |
| `dto/PlaningNumberResponseDTO.java` | `module/planning/dto/response/PlaningNumberResponseDTO.java` |

### 📂 module/planning/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/PlanningMaster.java` | `module/planning/entity/PlanningMaster.java` |
| `model/PlanningDetails.java` | `module/planning/entity/PlanningDetails.java` |
| `model/VesselPlanningMaster.java` | `module/planning/entity/VesselPlanningMaster.java` |
| `model/VesselPlanningDetails.java` | `module/planning/entity/VesselPlanningDetails.java` |
| `model/PreAlert.java` | `module/planning/entity/PreAlert.java` |
| `model/PreAlertMaster.java` | `module/planning/entity/PreAlertMaster.java` |
| `model/RTIMaster.java` | `module/planning/entity/RTIMaster.java` |
| `model/RTIDetails.java` | `module/planning/entity/RTIDetails.java` |

### 📂 module/planning/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/PlanningMasterMapper.java` | `module/planning/mapper/PlanningMasterMapper.java` |
| `mapper/PlanningDetailsMapper.java` | `module/planning/mapper/PlanningDetailsMapper.java` |
| `mapper/PlaningMapper.java` | `module/planning/mapper/PlaningMapper.java` |
| `mapper/PlanningSearchResultMapper.java` | `module/planning/mapper/PlanningSearchResultMapper.java` |
| `mapper/PlanningF5ViewMapper.java` | `module/planning/mapper/PlanningF5ViewMapper.java` |
| `mapper/VesselPlanningMasterMapper.java` | `module/planning/mapper/VesselPlanningMasterMapper.java` |
| `mapper/VesselPlanningDetailsMapper.java` | `module/planning/mapper/VesselPlanningDetailsMapper.java` |
| `mapper/PreAlertMapper.java` | `module/planning/mapper/PreAlertMapper.java` |
| `mapper/PreAlertMasterMapper.java` | `module/planning/mapper/PreAlertMasterMapper.java` |
| `mapper/RTIMasterMapper.java` | `module/planning/mapper/RTIMasterMapper.java` |
| `mapper/RTIDetailsMapper.java` | `module/planning/mapper/RTIDetailsMapper.java` |

### 📂 module/planning/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/PlanningMasterRepository.java` | `module/planning/repository/PlanningMasterRepository.java` |
| `repo/PlanningDetailsRepository.java` | `module/planning/repository/PlanningDetailsRepository.java` |
| `repo/VesselPlanningMasterRepository.java` | `module/planning/repository/VesselPlanningMasterRepository.java` |
| `repo/VesselPlanningDetailsRepository.java` | `module/planning/repository/VesselPlanningDetailsRepository.java` |
| `repo/PreAlertRepository.java` | `module/planning/repository/PreAlertRepository.java` |
| `repo/PreAlertMasterRepository.java` | `module/planning/repository/PreAlertMasterRepository.java` |
| `repo/RTIMasterRepository.java` | `module/planning/repository/RTIMasterRepository.java` |
| `repo/RTIDetailsRepository.java` | `module/planning/repository/RTIDetailsRepository.java` |

### 📂 module/planning/service/

| Current Location | New Location |
|------------------|--------------|
| `service/PlanningMasterService.java` | `module/planning/service/PlanningMasterService.java` |
| `service/PlanningDetailsService.java` | `module/planning/service/PlanningDetailsService.java` |
| `service/VesselPlanningService.java` | `module/planning/service/VesselPlanningService.java` |
| `service/PreAlertService.java` | `module/planning/service/PreAlertService.java` |
| `service/PreAlertMasterService.java` | `module/planning/service/PreAlertMasterService.java` |
| `service/RTIMasterService.java` | `module/planning/service/RTIMasterService.java` |
| `service/RTIDetailsService.java` | `module/planning/service/RTIDetailsService.java` |
| `service/impl/VesselPlanningServiceImpl.java` | `module/planning/service/impl/VesselPlanningServiceImpl.java` |
| `service/impl/PreAlertServiceImpl.java` | `module/planning/service/impl/PreAlertServiceImpl.java` |
| `service/impl/PreAlertMasterServiceImpl.java` | `module/planning/service/impl/PreAlertMasterServiceImpl.java` |
| `service/impl/RTIMasterServiceImpl.java` | `module/planning/service/impl/RTIMasterServiceImpl.java` |
| `service/impl/RTIDetailsServiceImpl.java` | `module/planning/service/impl/RTIDetailsServiceImpl.java` |

---

## 📁 FLEET MODULE (`module/fleet/`)

### 📂 module/fleet/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/TruckMasterController.java` | `module/fleet/controller/TruckMasterController.java` |
| `controller/TruckSparePartsController.java` | `module/fleet/controller/TruckSparePartsController.java` |
| `controller/TruckComboController.java` | `module/fleet/controller/TruckComboController.java` |
| `controller/DriverMasterController.java` | `module/fleet/controller/DriverMasterController.java` |
| `controller/FuelEntryController.java` | `module/fleet/controller/FuelEntryController.java` |
| `controller/FuelFillingsController.java` | `module/fleet/controller/FuelFillingsController.java` |
| `controller/TollEntryController.java` | `module/fleet/controller/TollEntryController.java` |
| `controller/EngineHoursController.java` | `module/fleet/controller/EngineHoursController.java` |
| `controller/SpeedReportController.java` | `module/fleet/controller/SpeedReportController.java` |
| `controller/LicenseMasterController.java` | `module/fleet/controller/LicenseMasterController.java` |
| `controller/SummonController.java` | `module/fleet/controller/SummonController.java` |
| `controller/AutoPassEntryController.java` | `module/fleet/controller/AutoPassEntryController.java` |
| `controller/LeviEntryController.java` | `module/fleet/controller/LeviEntryController.java` |
| `controller/SubcdiyEntryController.java` | `module/fleet/controller/SubcdiyEntryController.java` |

### 📂 module/fleet/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/TruckMasterDto.java` | `module/fleet/dto/TruckMasterDto.java` |
| `dto/TruckSparePartsDto.java` | `module/fleet/dto/TruckSparePartsDto.java` |
| `dto/DriverMasterDto.java` | `module/fleet/dto/DriverMasterDto.java` |
| `dto/FuelEntryDto.java` | `module/fleet/dto/FuelEntryDto.java` |
| `dto/FuelFillingsDto.java` | `module/fleet/dto/FuelFillingsDto.java` |
| `dto/TollEntryDto.java` | `module/fleet/dto/TollEntryDto.java` |
| `dto/TollEntryDetailsDto.java` | `module/fleet/dto/TollEntryDetailsDto.java` |
| `dto/EngineHoursDto.java` | `module/fleet/dto/EngineHoursDto.java` |
| `dto/SpeedReportDto.java` | `module/fleet/dto/SpeedReportDto.java` |
| `dto/LicenseMasterDto.java` | `module/fleet/dto/LicenseMasterDto.java` |
| `dto/SummonDto.java` | `module/fleet/dto/SummonDto.java` |
| `dto/AutoPassEntryDto.java` | `module/fleet/dto/AutoPassEntryDto.java` |
| `dto/LeviEntryDto.java` | `module/fleet/dto/LeviEntryDto.java` |
| `dto/SubcdiyEntryDto.java` | `module/fleet/dto/SubcdiyEntryDto.java` |
| `dto/request/TruckComboRequest.java` | `module/fleet/dto/request/TruckComboRequest.java` |

### 📂 module/fleet/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/TruckMaster.java` | `module/fleet/entity/TruckMaster.java` |
| `model/TruckSpareParts.java` | `module/fleet/entity/TruckSpareParts.java` |
| `model/DriverMaster.java` | `module/fleet/entity/DriverMaster.java` |
| `model/FuelEntry.java` | `module/fleet/entity/FuelEntry.java` |
| `model/FuelFillings.java` | `module/fleet/entity/FuelFillings.java` |
| `model/TollEntry.java` | `module/fleet/entity/TollEntry.java` |
| `model/TollEntryDetails.java` | `module/fleet/entity/TollEntryDetails.java` |
| `model/EngineHours.java` | `module/fleet/entity/EngineHours.java` |
| `model/SpeedReport.java` | `module/fleet/entity/SpeedReport.java` |
| `model/LicenseMaster.java` | `module/fleet/entity/LicenseMaster.java` |
| `model/Summon.java` | `module/fleet/entity/Summon.java` |
| `model/AutoPassEntry.java` | `module/fleet/entity/AutoPassEntry.java` |
| `model/LeviEntry.java` | `module/fleet/entity/LeviEntry.java` |
| `model/SubcdiyEntry.java` | `module/fleet/entity/SubcdiyEntry.java` |

### 📂 module/fleet/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/TruckMasterMapper.java` | `module/fleet/mapper/TruckMasterMapper.java` |
| `mapper/TruckSparePartsMapper.java` | `module/fleet/mapper/TruckSparePartsMapper.java` |
| `mapper/DriverMasterMapper.java` | `module/fleet/mapper/DriverMasterMapper.java` |
| `mapper/FuelEntryMapper.java` | `module/fleet/mapper/FuelEntryMapper.java` |
| `mapper/FuelFillingsMapper.java` | `module/fleet/mapper/FuelFillingsMapper.java` |
| `mapper/TollEntryMapper.java` | `module/fleet/mapper/TollEntryMapper.java` |
| `mapper/TollEntryDetailsMapper.java` | `module/fleet/mapper/TollEntryDetailsMapper.java` |
| `mapper/EngineHoursMapper.java` | `module/fleet/mapper/EngineHoursMapper.java` |
| `mapper/SpeedReportMapper.java` | `module/fleet/mapper/SpeedReportMapper.java` |
| `mapper/LicenseMasterMapper.java` | `module/fleet/mapper/LicenseMasterMapper.java` |
| `mapper/SummonMapper.java` | `module/fleet/mapper/SummonMapper.java` |
| `mapper/AutoPassEntryMapper.java` | `module/fleet/mapper/AutoPassEntryMapper.java` |
| `mapper/LeviEntryMapper.java` | `module/fleet/mapper/LeviEntryMapper.java` |
| `mapper/SubcdiyEntryMapper.java` | `module/fleet/mapper/SubcdiyEntryMapper.java` |

### 📂 module/fleet/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/TruckMasterRepository.java` | `module/fleet/repository/TruckMasterRepository.java` |
| `repo/TruckSparePartsRepository.java` | `module/fleet/repository/TruckSparePartsRepository.java` |
| `repo/DriverMasterRepository.java` | `module/fleet/repository/DriverMasterRepository.java` |
| `repo/FuelEntryRepository.java` | `module/fleet/repository/FuelEntryRepository.java` |
| `repo/FuelFillingsRepository.java` | `module/fleet/repository/FuelFillingsRepository.java` |
| `repo/TollEntryRepository.java` | `module/fleet/repository/TollEntryRepository.java` |
| `repo/TollEntryDetailsRepository.java` | `module/fleet/repository/TollEntryDetailsRepository.java` |
| `repo/EngineHoursRepository.java` | `module/fleet/repository/EngineHoursRepository.java` |
| `repo/SpeedReportRepository.java` | `module/fleet/repository/SpeedReportRepository.java` |
| `repo/LicenseMasterRepository.java` | `module/fleet/repository/LicenseMasterRepository.java` |
| `repo/SummonRepository.java` | `module/fleet/repository/SummonRepository.java` |
| `repo/AutoPassEntryRepository.java` | `module/fleet/repository/AutoPassEntryRepository.java` |
| `repo/LeviEntryRepository.java` | `module/fleet/repository/LeviEntryRepository.java` |
| `repo/SubcdiyEntryRepository.java` | `module/fleet/repository/SubcdiyEntryRepository.java` |

### 📂 module/fleet/service/

| Current Location | New Location |
|------------------|--------------|
| `service/TruckMasterService.java` | `module/fleet/service/TruckMasterService.java` |
| `service/TruckSparePartsService.java` | `module/fleet/service/TruckSparePartsService.java` |
| `service/DriverMasterService.java` | `module/fleet/service/DriverMasterService.java` |
| `service/FuelEntryService.java` | `module/fleet/service/FuelEntryService.java` |
| `service/FuelFillingsService.java` | `module/fleet/service/FuelFillingsService.java` |
| `service/TollEntryService.java` | `module/fleet/service/TollEntryService.java` |
| `service/EngineHoursService.java` | `module/fleet/service/EngineHoursService.java` |
| `service/SpeedReportService.java` | `module/fleet/service/SpeedReportService.java` |
| `service/LicenseMasterService.java` | `module/fleet/service/LicenseMasterService.java` |
| `service/SummonService.java` | `module/fleet/service/SummonService.java` |
| `service/AutoPassEntryService.java` | `module/fleet/service/AutoPassEntryService.java` |
| `service/LeviEntryService.java` | `module/fleet/service/LeviEntryService.java` |
| `service/SubcdiyEntryService.java` | `module/fleet/service/SubcdiyEntryService.java` |
| `service/impl/TruckMasterServiceImpl.java` | `module/fleet/service/impl/TruckMasterServiceImpl.java` |
| `service/impl/TruckSparePartsServiceImpl.java` | `module/fleet/service/impl/TruckSparePartsServiceImpl.java` |
| `service/impl/FuelEntryServiceImpl.java` | `module/fleet/service/impl/FuelEntryServiceImpl.java` |
| `service/impl/SpeedReportServiceImpl.java` | `module/fleet/service/impl/SpeedReportServiceImpl.java` |
| `service/impl/SummonServiceImpl.java` | `module/fleet/service/impl/SummonServiceImpl.java` |
| `service/impl/TollEntryServiceImpl.java` | `module/fleet/service/impl/TollEntryServiceImpl.java` |
| `service/impl/SubcdiyEntryServiceImpl.java` | `module/fleet/service/impl/SubcdiyEntryServiceImpl.java` |

---

## 📁 EMPLOYEE MODULE (`module/employee/`)

### 📂 module/employee/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/EmployeeMasterController.java` | `module/employee/controller/EmployeeMasterController.java` |
| `controller/SalaryEntryController.java` | `module/employee/controller/SalaryEntryController.java` |
| `controller/ForwardingSalaryController.java` | `module/employee/controller/ForwardingSalaryController.java` |
| `controller/CashierController.java` | `module/employee/controller/CashierController.java` |

### 📂 module/employee/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/EmployeeMasterDto.java` | `module/employee/dto/EmployeeMasterDto.java` |
| `dto/EmployeeAllDto.java` | `module/employee/dto/EmployeeAllDto.java` |
| `dto/SalaryEntryDto.java` | `module/employee/dto/SalaryEntryDto.java` |
| `dto/ForwardingSalaryDto.java` | `module/employee/dto/ForwardingSalaryDto.java` |
| `dto/CashierDto.java` | `module/employee/dto/CashierDto.java` |

### 📂 module/employee/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/EmployeeMaster.java` | `module/employee/entity/EmployeeMaster.java` |
| `model/SalaryEntry.java` | `module/employee/entity/SalaryEntry.java` |
| `model/ForwardingSalary.java` | `module/employee/entity/ForwardingSalary.java` |
| `model/Cashier.java` | `module/employee/entity/Cashier.java` |

### 📂 module/employee/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/EmployeeMasterMapper.java` | `module/employee/mapper/EmployeeMasterMapper.java` |
| `mapper/EmployeeAllMapper.java` | `module/employee/mapper/EmployeeAllMapper.java` |
| `mapper/SalaryEntryMapper.java` | `module/employee/mapper/SalaryEntryMapper.java` |
| `mapper/ForwardingSalaryMapper.java` | `module/employee/mapper/ForwardingSalaryMapper.java` |
| `mapper/CashierMapper.java` | `module/employee/mapper/CashierMapper.java` |

### 📂 module/employee/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/EmployeeMasterRepository.java` | `module/employee/repository/EmployeeMasterRepository.java` |
| `repo/SalaryEntryRepository.java` | `module/employee/repository/SalaryEntryRepository.java` |
| `repo/ForwardingSalaryRepository.java` | `module/employee/repository/ForwardingSalaryRepository.java` |
| `repo/CashierRepository.java` | `module/employee/repository/CashierRepository.java` |

### 📂 module/employee/service/

| Current Location | New Location |
|------------------|--------------|
| `service/EmployeeMasterService.java` | `module/employee/service/EmployeeMasterService.java` |
| `service/SalaryEntryService.java` | `module/employee/service/SalaryEntryService.java` |
| `service/ForwardingSalaryService.java` | `module/employee/service/ForwardingSalaryService.java` |
| `service/CashierService.java` | `module/employee/service/CashierService.java` |
| `service/impl/SalaryEntryServiceImpl.java` | `module/employee/service/impl/SalaryEntryServiceImpl.java` |

---

## 📁 ACCOUNTING MODULE (`module/accounting/`)

### 📂 module/accounting/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/AccountController.java` | `module/accounting/controller/AccountController.java` |
| `controller/AccountsGroupMasterController.java` | `module/accounting/controller/AccountsGroupMasterController.java` |
| `controller/GLAccountsController.java` | `module/accounting/controller/GLAccountsController.java` |
| `controller/ExpenseMasterController.java` | `module/accounting/controller/ExpenseMasterController.java` |
| `controller/ExpenseEntryController.java` | `module/accounting/controller/ExpenseEntryController.java` |
| `controller/SubExpenseMasterController.java` | `module/accounting/controller/SubExpenseMasterController.java` |
| `controller/ClaimVoucherController.java` | `module/accounting/controller/ClaimVoucherController.java` |
| `controller/CurrencyValueController.java` | `module/accounting/controller/CurrencyValueController.java` |

### 📂 module/accounting/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/AccountDto.java` | `module/accounting/dto/AccountDto.java` |
| `dto/AccountsGroupMasterDto.java` | `module/accounting/dto/AccountsGroupMasterDto.java` |
| `dto/GLAccountsDto.java` | `module/accounting/dto/GLAccountsDto.java` |
| `dto/ExpenseMasterDto.java` | `module/accounting/dto/ExpenseMasterDto.java` |
| `dto/ExpenseEntryDto.java` | `module/accounting/dto/ExpenseEntryDto.java` |
| `dto/SubExpenseMasterDto.java` | `module/accounting/dto/SubExpenseMasterDto.java` |
| `dto/ClaimVoucherDto.java` | `module/accounting/dto/ClaimVoucherDto.java` |
| `dto/CurrencyValueDto.java` | `module/accounting/dto/CurrencyValueDto.java` |

### 📂 module/accounting/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/Account.java` | `module/accounting/entity/Account.java` |
| `model/AccountsGroupMaster.java` | `module/accounting/entity/AccountsGroupMaster.java` |
| `model/GLAccounts.java` | `module/accounting/entity/GLAccounts.java` |
| `model/ExpenseMaster.java` | `module/accounting/entity/ExpenseMaster.java` |
| `model/ExpenseEntry.java` | `module/accounting/entity/ExpenseEntry.java` |
| `model/SubExpenseMaster.java` | `module/accounting/entity/SubExpenseMaster.java` |
| `model/ClaimVoucher.java` | `module/accounting/entity/ClaimVoucher.java` |

### 📂 module/accounting/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/AccountMapper.java` | `module/accounting/mapper/AccountMapper.java` |
| `mapper/AccountsGroupMasterMapper.java` | `module/accounting/mapper/AccountsGroupMasterMapper.java` |
| `mapper/GLAccountsMapper.java` | `module/accounting/mapper/GLAccountsMapper.java` |
| `mapper/ExpenseMasterMapper.java` | `module/accounting/mapper/ExpenseMasterMapper.java` |
| `mapper/ExpenseEntryMapper.java` | `module/accounting/mapper/ExpenseEntryMapper.java` |
| `mapper/SubExpenseMasterMapper.java` | `module/accounting/mapper/SubExpenseMasterMapper.java` |
| `mapper/ClaimVoucherMapper.java` | `module/accounting/mapper/ClaimVoucherMapper.java` |

### 📂 module/accounting/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/AccountRepository.java` | `module/accounting/repository/AccountRepository.java` |
| `repo/AccountsGroupMasterRepository.java` | `module/accounting/repository/AccountsGroupMasterRepository.java` |
| `repo/GLAccountsRepository.java` | `module/accounting/repository/GLAccountsRepository.java` |
| `repo/ExpenseMasterRepository.java` | `module/accounting/repository/ExpenseMasterRepository.java` |
| `repo/ExpenseEntryRepository.java` | `module/accounting/repository/ExpenseEntryRepository.java` |
| `repo/SubExpenseMasterRepository.java` | `module/accounting/repository/SubExpenseMasterRepository.java` |
| `repo/ClaimVoucherRepository.java` | `module/accounting/repository/ClaimVoucherRepository.java` |

### 📂 module/accounting/service/

| Current Location | New Location |
|------------------|--------------|
| `service/AccountService.java` | `module/accounting/service/AccountService.java` |
| `service/AccountsGroupMasterService.java` | `module/accounting/service/AccountsGroupMasterService.java` |
| `service/GLAccountsService.java` | `module/accounting/service/GLAccountsService.java` |
| `service/ExpenseMasterService.java` | `module/accounting/service/ExpenseMasterService.java` |
| `service/ExpenseEntryService.java` | `module/accounting/service/ExpenseEntryService.java` |
| `service/SubExpenseMasterService.java` | `module/accounting/service/SubExpenseMasterService.java` |
| `service/ClaimVoucherService.java` | `module/accounting/service/ClaimVoucherService.java` |
| `service/CurrencyValueService.java` | `module/accounting/service/CurrencyValueService.java` |
| `service/impl/CurrencyValueServiceImpl.java` | `module/accounting/service/impl/CurrencyValueServiceImpl.java` |
| `service/impl/SubExpenseMasterServiceImpl.java` | `module/accounting/service/impl/SubExpenseMasterServiceImpl.java` |

---

## 📁 BILLING MODULE (`module/billing/`)

### 📂 module/billing/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/BillMasterController.java` | `module/billing/controller/BillMasterController.java` |
| `controller/BillDetailsController.java` | `module/billing/controller/BillDetailsController.java` |
| `controller/BillsOrderMasterController.java` | `module/billing/controller/BillsOrderMasterController.java` |
| `controller/BillsOrderDetailsController.java` | `module/billing/controller/BillsOrderDetailsController.java` |
| `controller/DoMasterController.java` | `module/billing/controller/DoMasterController.java` |

### 📂 module/billing/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/BillMasterDto.java` | `module/billing/dto/BillMasterDto.java` |
| `dto/BillDetailsDto.java` | `module/billing/dto/BillDetailsDto.java` |
| `dto/BillsOrderMasterDto.java` | `module/billing/dto/BillsOrderMasterDto.java` |
| `dto/BillsOrderDetailsDto.java` | `module/billing/dto/BillsOrderDetailsDto.java` |
| `dto/DoMasterDto.java` | `module/billing/dto/DoMasterDto.java` |

### 📂 module/billing/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/BillMaster.java` | `module/billing/entity/BillMaster.java` |
| `model/BillDetails.java` | `module/billing/entity/BillDetails.java` |
| `model/BillsOrderMaster.java` | `module/billing/entity/BillsOrderMaster.java` |
| `model/BillsOrderDetails.java` | `module/billing/entity/BillsOrderDetails.java` |
| `model/DoMaster.java` | `module/billing/entity/DoMaster.java` |

### 📂 module/billing/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/BillMasterMapper.java` | `module/billing/mapper/BillMasterMapper.java` |
| `mapper/BillDetailsMapper.java` | `module/billing/mapper/BillDetailsMapper.java` |
| `mapper/BillsOrderMasterMapper.java` | `module/billing/mapper/BillsOrderMasterMapper.java` |
| `mapper/BillsOrderDetailsMapper.java` | `module/billing/mapper/BillsOrderDetailsMapper.java` |
| `mapper/DoMasterMapper.java` | `module/billing/mapper/DoMasterMapper.java` |

### 📂 module/billing/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/BillMasterRepository.java` | `module/billing/repository/BillMasterRepository.java` |
| `repo/BillDetailsRepository.java` | `module/billing/repository/BillDetailsRepository.java` |
| `repo/BillsOrderMasterRepository.java` | `module/billing/repository/BillsOrderMasterRepository.java` |
| `repo/BillsOrderDetailsRepository.java` | `module/billing/repository/BillsOrderDetailsRepository.java` |
| `repo/DoMasterRepository.java` | `module/billing/repository/DoMasterRepository.java` |

### 📂 module/billing/service/

| Current Location | New Location |
|------------------|--------------|
| `service/BillMasterService.java` | `module/billing/service/BillMasterService.java` |
| `service/BillDetailsService.java` | `module/billing/service/BillDetailsService.java` |
| `service/BillsOrderMasterService.java` | `module/billing/service/BillsOrderMasterService.java` |
| `service/BillsOrderDetailsService.java` | `module/billing/service/BillsOrderDetailsService.java` |
| `service/DoMasterService.java` | `module/billing/service/DoMasterService.java` |

---

## 📁 MASTER DATA MODULE (`module/master/`)

### 📂 module/master/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/BankMasterController.java` | `module/master/controller/BankMasterController.java` |
| `controller/CountryMasterController.java` | `module/master/controller/CountryMasterController.java` |
| `controller/LocationMasterController.java` | `module/master/controller/LocationMasterController.java` |
| `controller/PortMasterController.java` | `module/master/controller/PortMasterController.java` |
| `controller/TaxMasterController.java` | `module/master/controller/TaxMasterController.java` |
| `controller/SymbolMasterController.java` | `module/master/controller/SymbolMasterController.java` |
| `controller/MSICCodeController.java` | `module/master/controller/MSICCodeController.java` |
| `controller/ClassificationController.java` | `module/master/controller/ClassificationController.java` |
| `controller/PaymentTermsMasterController.java` | `module/master/controller/PaymentTermsMasterController.java` |
| `controller/RulesTypeMasterController.java` | `module/master/controller/RulesTypeMasterController.java` |
| `controller/CardMasterController.java` | `module/master/controller/CardMasterController.java` |
| `controller/CounterController.java` | `module/master/controller/CounterController.java` |
| `controller/SequenceNoMasterController.java` | `module/master/controller/SequenceNoMasterController.java` |
| `controller/AddressMasterController.java` | `module/master/controller/AddressMasterController.java` |

### 📂 module/master/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/BankMasterDto.java` | `module/master/dto/BankMasterDto.java` |
| `dto/CountryMasterDto.java` | `module/master/dto/CountryMasterDto.java` |
| `dto/LocationMasterDto.java` | `module/master/dto/LocationMasterDto.java` |
| `dto/PortMasterDto.java` | `module/master/dto/PortMasterDto.java` |
| `dto/TaxMasterDto.java` | `module/master/dto/TaxMasterDto.java` |
| `dto/SymbolMasterDto.java` | `module/master/dto/SymbolMasterDto.java` |
| `dto/MSICCodeDto.java` | `module/master/dto/MSICCodeDto.java` |
| `dto/ClassificationDto.java` | `module/master/dto/ClassificationDto.java` |
| `dto/PaymentTermsMasterDto.java` | `module/master/dto/PaymentTermsMasterDto.java` |
| `dto/RulesTypeMasterDto.java` | `module/master/dto/RulesTypeMasterDto.java` |
| `dto/CardMasterDto.java` | `module/master/dto/CardMasterDto.java` |
| `dto/CounterDto.java` | `module/master/dto/CounterDto.java` |
| `dto/SequenceNoMasterDto.java` | `module/master/dto/SequenceNoMasterDto.java` |
| `dto/AddressMasterDto.java` | `module/master/dto/AddressMasterDto.java` |

### 📂 module/master/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/BankMaster.java` | `module/master/entity/BankMaster.java` |
| `model/CountryMaster.java` | `module/master/entity/CountryMaster.java` |
| `model/LocationMaster.java` | `module/master/entity/LocationMaster.java` |
| `model/PortMaster.java` | `module/master/entity/PortMaster.java` |
| `model/TaxMaster.java` | `module/master/entity/TaxMaster.java` |
| `model/SymbolMaster.java` | `module/master/entity/SymbolMaster.java` |
| `model/MSICCode.java` | `module/master/entity/MSICCode.java` |
| `model/Classification.java` | `module/master/entity/Classification.java` |
| `model/PaymentTermsMaster.java` | `module/master/entity/PaymentTermsMaster.java` |
| `model/RulesTypeMaster.java` | `module/master/entity/RulesTypeMaster.java` |
| `model/CardMaster.java` | `module/master/entity/CardMaster.java` |
| `model/Counter.java` | `module/master/entity/Counter.java` |
| `model/SequenceNoMaster.java` | `module/master/entity/SequenceNoMaster.java` |
| `model/AddressMaster.java` | `module/master/entity/AddressMaster.java` |

### 📂 module/master/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/BankMasterMapper.java` | `module/master/mapper/BankMasterMapper.java` |
| `mapper/CountryMasterMapper.java` | `module/master/mapper/CountryMasterMapper.java` |
| `mapper/LocationMasterMapper.java` | `module/master/mapper/LocationMasterMapper.java` |
| `mapper/PortMasterMapper.java` | `module/master/mapper/PortMasterMapper.java` |
| `mapper/TaxMasterMapper.java` | `module/master/mapper/TaxMasterMapper.java` |
| `mapper/SymbolMasterMapper.java` | `module/master/mapper/SymbolMasterMapper.java` |
| `mapper/MSICCodeMapper.java` | `module/master/mapper/MSICCodeMapper.java` |
| `mapper/ClassificationMapper.java` | `module/master/mapper/ClassificationMapper.java` |
| `mapper/PaymentTermsMasterMapper.java` | `module/master/mapper/PaymentTermsMasterMapper.java` |
| `mapper/RulesTypeMasterMapper.java` | `module/master/mapper/RulesTypeMasterMapper.java` |
| `mapper/CardMasterMapper.java` | `module/master/mapper/CardMasterMapper.java` |
| `mapper/CounterMapper.java` | `module/master/mapper/CounterMapper.java` |
| `mapper/SequenceNoMasterMapper.java` | `module/master/mapper/SequenceNoMasterMapper.java` |
| `mapper/AddressMasterMapper.java` | `module/master/mapper/AddressMasterMapper.java` |

### 📂 module/master/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/BankMasterRepository.java` | `module/master/repository/BankMasterRepository.java` |
| `repo/CountryMasterRepository.java` | `module/master/repository/CountryMasterRepository.java` |
| `repo/LocationMasterRepository.java` | `module/master/repository/LocationMasterRepository.java` |
| `repo/PortMasterRepository.java` | `module/master/repository/PortMasterRepository.java` |
| `repo/TaxMasterRepository.java` | `module/master/repository/TaxMasterRepository.java` |
| `repo/SymbolMasterRepository.java` | `module/master/repository/SymbolMasterRepository.java` |
| `repo/MSICCodeRepository.java` | `module/master/repository/MSICCodeRepository.java` |
| `repo/ClassificationRepository.java` | `module/master/repository/ClassificationRepository.java` |
| `repo/PaymentTermsMasterRepository.java` | `module/master/repository/PaymentTermsMasterRepository.java` |
| `repo/RulesTypeMasterRepository.java` | `module/master/repository/RulesTypeMasterRepository.java` |
| `repo/CardMasterRepository.java` | `module/master/repository/CardMasterRepository.java` |
| `repo/CounterRepository.java` | `module/master/repository/CounterRepository.java` |
| `repo/SequenceNoMasterRepository.java` | `module/master/repository/SequenceNoMasterRepository.java` |
| `repo/AddressMasterRepository.java` | `module/master/repository/AddressMasterRepository.java` |

### 📂 module/master/service/

| Current Location | New Location |
|------------------|--------------|
| `service/BankMasterService.java` | `module/master/service/BankMasterService.java` |
| `service/CountryMasterService.java` | `module/master/service/CountryMasterService.java` |
| `service/LocationMasterService.java` | `module/master/service/LocationMasterService.java` |
| `service/PortMasterService.java` | `module/master/service/PortMasterService.java` |
| `service/TaxMasterService.java` | `module/master/service/TaxMasterService.java` |
| `service/SymbolMasterService.java` | `module/master/service/SymbolMasterService.java` |
| `service/MSICCodeService.java` | `module/master/service/MSICCodeService.java` |
| `service/ClassificationService.java` | `module/master/service/ClassificationService.java` |
| `service/PaymentTermsMasterService.java` | `module/master/service/PaymentTermsMasterService.java` |
| `service/RulesTypeMasterService.java` | `module/master/service/RulesTypeMasterService.java` |
| `service/CardMasterService.java` | `module/master/service/CardMasterService.java` |
| `service/CounterService.java` | `module/master/service/CounterService.java` |
| `service/SequenceNoMasterService.java` | `module/master/service/SequenceNoMasterService.java` |
| `service/AddressMasterService.java` | `module/master/service/AddressMasterService.java` |
| `service/impl/RulesTypeMasterServiceImpl.java` | `module/master/service/impl/RulesTypeMasterServiceImpl.java` |
| `service/impl/SymbolMasterServiceImpl.java` | `module/master/service/impl/SymbolMasterServiceImpl.java` |
| `service/impl/TaxMasterServiceImpl.java` | `module/master/service/impl/TaxMasterServiceImpl.java` |

---

## 📁 USER MODULE (`module/user/`)

### 📂 module/user/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/AppUserController.java` | `module/user/controller/AppUserController.java` |
| `controller/AuthUserController.java` | `module/user/controller/AuthUserController.java` |
| `controller/MENUMasterController.java` | `module/user/controller/MENUMasterController.java` |
| `controller/MENUPrivilegeController.java` | `module/user/controller/MENUPrivilegeController.java` |
| `controller/FormTransactionPasswordController.java` | `module/user/controller/FormTransactionPasswordController.java` |

### 📂 module/user/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/AppUserDto.java` | `module/user/dto/AppUserDto.java` |
| `dto/MENUMasterDto.java` | `module/user/dto/MENUMasterDto.java` |
| `dto/MENUPrivilegeDto.java` | `module/user/dto/MENUPrivilegeDto.java` |
| `dto/FormTransactionPasswordDto.java` | `module/user/dto/FormTransactionPasswordDto.java` |

### 📂 module/user/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/AppUser.java` | `module/user/entity/AppUser.java` |
| `model/MENUMaster.java` | `module/user/entity/MENUMaster.java` |
| `model/MENUPrivilege.java` | `module/user/entity/MENUPrivilege.java` |
| `model/FormTransactionPassword.java` | `module/user/entity/FormTransactionPassword.java` |

### 📂 module/user/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/MENUMasterMapper.java` | `module/user/mapper/MENUMasterMapper.java` |
| `mapper/MENUPrivilegeMapper.java` | `module/user/mapper/MENUPrivilegeMapper.java` |
| `mapper/FormTransactionPasswordMapper.java` | `module/user/mapper/FormTransactionPasswordMapper.java` |

### 📂 module/user/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/AppUserRepository.java` | `module/user/repository/AppUserRepository.java` |
| `repo/MENUMasterRepository.java` | `module/user/repository/MENUMasterRepository.java` |
| `repo/MENUPrivilegeRepository.java` | `module/user/repository/MENUPrivilegeRepository.java` |
| `repo/FormTransactionPasswordRepository.java` | `module/user/repository/FormTransactionPasswordRepository.java` |

### 📂 module/user/service/

| Current Location | New Location |
|------------------|--------------|
| `service/AppUserService.java` | `module/user/service/AppUserService.java` |
| `service/AppUserDetailsService.java` | `module/user/service/AppUserDetailsService.java` |
| `service/MENUMasterService.java` | `module/user/service/MENUMasterService.java` |
| `service/MENUPrivilegeService.java` | `module/user/service/MENUPrivilegeService.java` |
| `service/FormTransactionPasswordService.java` | `module/user/service/FormTransactionPasswordService.java` |

---

## 📁 COMPANY MODULE (`module/company/`)

### 📂 module/company/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/CompanyController.java` | `module/company/controller/CompanyController.java` |
| `controller/CompanySettingsController.java` | `module/company/controller/CompanySettingsController.java` |
| `controller/MainSettingController.java` | `module/company/controller/MainSettingController.java` |
| `controller/MasterSettingController.java` | `module/company/controller/MasterSettingController.java` |
| `controller/AgentController.java` | `module/company/controller/AgentController.java` |

### 📂 module/company/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/CompanyDto.java` | `module/company/dto/CompanyDto.java` |
| `dto/CompanySettingsDto.java` | `module/company/dto/CompanySettingsDto.java` |
| `dto/MainSettingDto.java` | `module/company/dto/MainSettingDto.java` |
| `dto/MasterSettingDto.java` | `module/company/dto/MasterSettingDto.java` |
| `dto/AgentDto.java` | `module/company/dto/AgentDto.java` |

### 📂 module/company/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/Company.java` | `module/company/entity/Company.java` |
| `model/CompanySettings.java` | `module/company/entity/CompanySettings.java` |
| `model/MainSetting.java` | `module/company/entity/MainSetting.java` |
| `model/MasterSetting.java` | `module/company/entity/MasterSetting.java` |
| `model/Agent.java` | `module/company/entity/Agent.java` |

### 📂 module/company/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/CompanyMapper.java` | `module/company/mapper/CompanyMapper.java` |
| `mapper/CompanySettingsMapper.java` | `module/company/mapper/CompanySettingsMapper.java` |
| `mapper/MainSettingMapper.java` | `module/company/mapper/MainSettingMapper.java` |
| `mapper/MasterSettingMapper.java` | `module/company/mapper/MasterSettingMapper.java` |
| `mapper/AgentMapper.java` | `module/company/mapper/AgentMapper.java` |

### 📂 module/company/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/CompanyRepository.java` | `module/company/repository/CompanyRepository.java` |
| `repo/CompanySettingsRepository.java` | `module/company/repository/CompanySettingsRepository.java` |
| `repo/MainSettingRepository.java` | `module/company/repository/MainSettingRepository.java` |
| `repo/MasterSettingRepository.java` | `module/company/repository/MasterSettingRepository.java` |
| `repo/AgentRepository.java` | `module/company/repository/AgentRepository.java` |

### 📂 module/company/service/

| Current Location | New Location |
|------------------|--------------|
| `service/CompanyService.java` | `module/company/service/CompanyService.java` |
| `service/CompanySettingsService.java` | `module/company/service/CompanySettingsService.java` |
| `service/MainSettingService.java` | `module/company/service/MainSettingService.java` |
| `service/MasterSettingService.java` | `module/company/service/MasterSettingService.java` |
| `service/AgentService.java` | `module/company/service/AgentService.java` |

---

## 📁 AGENT COMPANY MODULE (`module/agentcompany/`)

> **Note:** This module already follows the correct structure! ✅

| Current Location | New Location |
|------------------|--------------|
| `agentcompany/common/ApiResponse.java` | `module/agentcompany/common/ApiResponse.java` |
| `agentcompany/controller/AgentCompanyMasterController.java` | `module/agentcompany/controller/AgentCompanyMasterController.java` |
| `agentcompany/dto/AgentCompanyMasterDTO.java` | `module/agentcompany/dto/AgentCompanyMasterDTO.java` |
| `agentcompany/dto/AgentCompanyRequestDTO.java` | `module/agentcompany/dto/AgentCompanyRequestDTO.java` |
| `agentcompany/dto/AgentCompanyResponseDTO.java` | `module/agentcompany/dto/AgentCompanyResponseDTO.java` |
| `agentcompany/entity/AgentCompanyMaster.java` | `module/agentcompany/entity/AgentCompanyMaster.java` |
| `agentcompany/mapper/AgentCompanyMasterMapper.java` | `module/agentcompany/mapper/AgentCompanyMasterMapper.java` |
| `agentcompany/repository/AgentCompanyMasterRepository.java` | `module/agentcompany/repository/AgentCompanyMasterRepository.java` |
| `agentcompany/service/AgentCompanyMasterService.java` | `module/agentcompany/service/AgentCompanyMasterService.java` |

---

## 📁 COMMUNICATION MODULE (`module/communication/`)

### 📂 module/communication/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/EmailInboxController.java` | `module/communication/controller/EmailInboxController.java` |
| `controller/PhoneCallEntryController.java` | `module/communication/controller/PhoneCallEntryController.java` |

### 📂 module/communication/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/EmailInboxDto.java` | `module/communication/dto/EmailInboxDto.java` |
| `dto/PhoneCallEntryDto.java` | `module/communication/dto/PhoneCallEntryDto.java` |

### 📂 module/communication/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/EmailInbox.java` | `module/communication/entity/EmailInbox.java` |
| `model/PhoneCallEntry.java` | `module/communication/entity/PhoneCallEntry.java` |

### 📂 module/communication/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/EmailInboxMapper.java` | `module/communication/mapper/EmailInboxMapper.java` |
| `mapper/PhoneCallEntryMapper.java` | `module/communication/mapper/PhoneCallEntryMapper.java` |

### 📂 module/communication/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/EmailInboxRepository.java` | `module/communication/repository/EmailInboxRepository.java` |
| `repo/PhoneCallEntryRepository.java` | `module/communication/repository/PhoneCallEntryRepository.java` |

### 📂 module/communication/service/

| Current Location | New Location |
|------------------|--------------|
| `service/EmailInboxService.java` | `module/communication/service/EmailInboxService.java` |
| `service/PhoneCallEntryService.java` | `module/communication/service/PhoneCallEntryService.java` |
| `service/impl/PhoneCallEntryServiceImpl.java` | `module/communication/service/impl/PhoneCallEntryServiceImpl.java` |

---

## 📁 FILE MODULE (`module/file/`)

### 📂 module/file/controller/

| Current Location | New Location |
|------------------|--------------|
| `controller/FileUploadController.java` | `module/file/controller/FileUploadController.java` |
| `controller/ImageUploadController.java` | `module/file/controller/ImageUploadController.java` |

### 📂 module/file/dto/

| Current Location | New Location |
|------------------|--------------|
| `dto/FileUploadResponseDto.java` | `module/file/dto/FileUploadResponseDto.java` |
| `dto/ImageUploadDto.java` | `module/file/dto/ImageUploadDto.java` |

### 📂 module/file/entity/

| Current Location | New Location |
|------------------|--------------|
| `model/ImageUpload.java` | `module/file/entity/ImageUpload.java` |

### 📂 module/file/mapper/

| Current Location | New Location |
|------------------|--------------|
| `mapper/ImageUploadMapper.java` | `module/file/mapper/ImageUploadMapper.java` |

### 📂 module/file/repository/

| Current Location | New Location |
|------------------|--------------|
| `repo/ImageUploadRepository.java` | `module/file/repository/ImageUploadRepository.java` |

### 📂 module/file/service/

| Current Location | New Location |
|------------------|--------------|
| `service/FileUploadService.java` | `module/file/service/FileUploadService.java` |
| `service/ImageUploadService.java` | `module/file/service/ImageUploadService.java` |

---

## 📊 SUMMARY TABLE

| Module | Controllers | DTOs | Entities | Mappers | Repositories | Services |
|--------|-------------|------|----------|---------|--------------|----------|
| common | 1 | 2 | - | 1 | - | - |
| security | 1 | 1 | - | - | - | 3 |
| sale | 14 | 22 | 14 | 15 | 14 | 14+14 |
| purchase | 4 | 4 | 4 | 4 | 4 | 4+4 |
| payment | 11 | 11 | 11 | 11 | 11 | 11+2 |
| customer | 8 | 12 | 8 | 8 | 9 | 8+1 |
| supplier | 1 | 1 | 1 | 1 | 1 | 1+1 |
| inventory | 6 | 7 | 6 | 7 | 6 | 6+3 |
| job | 6 | 8 | 5 | 5 | 5 | 6+1 |
| planning | 8 | 15 | 8 | 11 | 8 | 7+5 |
| fleet | 14 | 15 | 14 | 14 | 14 | 13+7 |
| employee | 4 | 5 | 4 | 5 | 4 | 4+1 |
| accounting | 8 | 8 | 7 | 7 | 7 | 8+2 |
| billing | 5 | 5 | 5 | 5 | 5 | 5 |
| master | 14 | 14 | 14 | 14 | 14 | 14+3 |
| user | 5 | 4 | 4 | 3 | 4 | 5 |
| company | 5 | 5 | 5 | 5 | 5 | 5 |
| agentcompany | 1 | 3 | 1 | 1 | 1 | 1 |
| communication | 2 | 2 | 2 | 2 | 2 | 2+1 |
| file | 2 | 2 | 1 | 1 | 1 | 2 |
| **TOTAL** | **~115** | **~146** | **~114** | **~115** | **~115** | **~160** |

---

## 🔑 QUICK REFERENCE - PACKAGE NAMES

```java
// COMMON
my.maleva.api.common.config
my.maleva.api.common.constant
my.maleva.api.common.dto
my.maleva.api.common.exception
my.maleva.api.common.util

// SECURITY
my.maleva.api.security.controller
my.maleva.api.security.dto
my.maleva.api.security.filter
my.maleva.api.security.service
my.maleva.api.security.config

// INTEGRATION
my.maleva.api.integration.qne

// MODULES
my.maleva.api.module.sale.controller
my.maleva.api.module.sale.dto
my.maleva.api.module.sale.dto.request
my.maleva.api.module.sale.dto.response
my.maleva.api.module.sale.entity
my.maleva.api.module.sale.mapper
my.maleva.api.module.sale.repository
my.maleva.api.module.sale.service
my.maleva.api.module.sale.service.impl
my.maleva.api.module.sale.specification
my.maleva.api.module.sale.util

// Similar pattern for other modules:
my.maleva.api.module.{module_name}.{layer}
```

---

**Document Created:** March 26, 2026  
**Total Files Mapped:** 500+

