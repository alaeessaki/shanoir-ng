package org.shanoir.ng.processing.carmin.service;

import java.util.List;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CarminDatasetProcessingService {

    /**
     * save a CarminDatasetProcessing
     * 
     * @param carminDatasetProcessing
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #carminDatasetProcessing.getId() == null")
    CarminDatasetProcessing create(CarminDatasetProcessing carminDatasetProcessing);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    CarminDatasetProcessing getCarminDatasetProcessingByComment(String comment) throws EntityNotFoundException;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<CarminDatasetProcessing> getCarminDatasetProcessings();

    /**
     * Update an entity.
     *
     * @param entity the entity to update.
     * @return updated entity.
     * @throws EntityNotFoundException
     * @throws MicroServiceCommunicationException
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    CarminDatasetProcessing update(Long datasetProcessingId, CarminDatasetProcessing carminDatasetProcessing) throws EntityNotFoundException;


}
