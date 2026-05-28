package my.maleva.api.module.accounting.service.impl;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.accounting.dto.ComboListDto;
import my.maleva.api.module.accounting.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.accounting.service.AccountsGroupMasterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountsGroupMasterServiceImpl implements AccountsGroupMasterService {

    private final AccountsGroupMasterRepository repository;

    @Override
    public List<ComboListDto> getAccountsGroupMaster(Integer companyId, String type) {

        List<String> parentCodes = null;

        switch (type.toUpperCase()) {

            case "PV":
                parentCodes = Arrays.asList(
                        "AGE", "SCR", "CUS",
                        "DRI", "TRU", "SEM",
                        "EMP", "SUP", "BAK"
                );
                break;

            case "CUSTOMER":
                parentCodes = List.of("CUS");
                break;

            case "EMPLOYEE":
                parentCodes = List.of("EMP");
                break;

            case "SUPPLIER":
                parentCodes = List.of("SUP");
                break;

            case "AGENT":
                parentCodes = List.of("AGE");
                break;

            case "TRUCK":
                parentCodes = List.of("TRU");
                break;

            case "DRIVER":
                parentCodes = List.of("DRI");
                break;
        }

        List<Object[]> result =
                repository.getAccountsGroupMaster(companyId, parentCodes);

        List<ComboListDto> response = new ArrayList<>();

        for (Object[] row : result) {

            ComboListDto dto = ComboListDto.builder()
                    .id((Integer) row[0])
                    .accountName((String) row[1])
                    .accountName1((String) row[2])
                    .accountCode((String) row[3])
                    .build();

            response.add(dto);
        }

        return response;
    }
}