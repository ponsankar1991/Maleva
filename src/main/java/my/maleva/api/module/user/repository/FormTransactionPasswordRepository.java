package my.maleva.api.module.user.repository;

import my.maleva.api.module.user.entity.FormTransactionPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormTransactionPasswordRepository extends JpaRepository<FormTransactionPassword, Integer> {
    List<FormTransactionPassword> findByCompanyRefId(Integer companyRefId);

    /**
     * The gate behind screens that ask for a transaction password
     * ({@code SpclPower}, {@code AdminPower}, {@code EditPassword}…).
     *
     * <p>Ported from legacy {@code LoginServices.EditPassword}, which ran the
     * same count as a concatenated SQL string. The comparison is exact and the
     * stored value is plaintext — that is the existing data, not a choice made
     * here; this only stops the password from having to leave the server.
     */
    boolean existsByCompanyRefIdAndTransactionNameAndPasswordAndActive(
            Integer companyRefId, String transactionName, String password, Integer active);
}
