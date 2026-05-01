package my.maleva.api.module.accounting;

import my.maleva.api.module.accounting.entity.Account;
import my.maleva.api.module.accounting.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AccountingAuditingTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @WithMockUser(username = "test-user")
    public void testAuditing() {
        Account account = Account.builder()
                .companyRefId(1)
                .accountCode("TEST-AUDIT")
                .rowIndex(1)
                .build();

        Account saved = accountRepository.save(account);

        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getModifiedDate()).isNotNull();
        assertThat(saved.getModifiedBy()).isEqualTo("test-user");
    }
}
