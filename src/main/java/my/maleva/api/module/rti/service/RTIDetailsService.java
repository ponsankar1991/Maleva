package my.maleva.api.module.rti.service;

import my.maleva.api.module.rti.dto.RTIDetailsDto;
import java.util.List;
import java.util.Optional;

/**
 * RTIDetailsService
 * Business logic interface for RTIDetails operations
 */
public interface RTIDetailsService {

    /**
     * Get all RTIDetails by RTIMaster ID
     */
    List<RTIDetailsDto> getByRtiMasterId(Integer rtiMasterRefId);

    /**
     * Get RTIDetails by ID
     */
    Optional<RTIDetailsDto> getById(Integer id);

    /**
     * Create new RTIDetails record
     */
    RTIDetailsDto create(RTIDetailsDto dto);

    /**
     * Update RTIDetails record
     */
    RTIDetailsDto update(Integer id, RTIDetailsDto dto);

    /**
     * Delete RTIDetails record
     */
    boolean delete(Integer id);

    /**
     * Get RTIDetails by sale order master
     */
    List<RTIDetailsDto> getBySaleOrderMasterId(Integer saleOrderMasterRefId);

    /**
     * Count RTIDetails for an RTIMaster
     */
    long countByRtiMasterId(Integer rtiMasterRefId);

    /**
     * Delete all RTIDetails for an RTIMaster
     */
    void deleteByRtiMasterId(Integer rtiMasterRefId);

    /**
     * For each given sale order id, the latest active RTI containing it.
     * Sale orders with no RTI are omitted from the result.
     */
    List<java.util.Map<String, Object>> getRtiStatusBySaleOrderIds(List<Integer> saleOrderIds);
}

