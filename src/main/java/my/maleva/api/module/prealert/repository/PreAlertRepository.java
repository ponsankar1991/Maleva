package my.maleva.api.module.prealert.repository;

import my.maleva.api.module.prealert.entity.PreAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreAlertRepository extends JpaRepository<PreAlert, Integer> {

    /**
     * Find all PreAlert records by PreAlertMaster reference ID
     */
    List<PreAlert> findByPreAlertMasterRefId(Integer preAlertMasterRefId);

    /**
     * Count PreAlert records by PreAlertMaster ID
     */
    Long countByPreAlertMasterRefId(Integer preAlertMasterRefId);

    /**
     * Delete PreAlert records by PreAlertMaster ID
     */
    void deleteByPreAlertMasterRefId(Integer preAlertMasterRefId);
}
