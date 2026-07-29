package my.maleva.api.module.boardingsettlement.repository;

import my.maleva.api.module.boardingsettlement.entity.BoardingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoardingEventRepository extends JpaRepository<BoardingEvent, Long> {

    @Modifying
    @Query("DELETE FROM BoardingEvent b WHERE b.saleOrderMasterRefId = :saleOrderMasterRefId")
    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    List<BoardingEvent> findByBoardingDateBetweenOrderByBoardingDateAsc(LocalDateTime fromDate, LocalDateTime toDate);

    List<BoardingEvent> findAllByOrderByBoardingDateAsc();
}

