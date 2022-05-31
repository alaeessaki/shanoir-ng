package org.shanoir.ng.processing.carmin.service;

import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarminDatasetPorcessingServiceImpl implements CarminDatasetProcessingService {

    @Autowired
    private DatasetProcessingRepository datasetProcessingRepository;

    protected CarminDatasetProcessing updateValues(CarminDatasetProcessing from, CarminDatasetProcessing to) {
        to.setIdentifier(from.getIdentifier());
        to.setStatus(from.getStatus());
        ;
        to.setName(from.getName());
        to.setPipelineIdentifier(from.getPipelineIdentifier());
        to.setStartDate(from.getStartDate());
        to.setEndDate(from.getEndDate());
        to.setTimeout(from.getTimeout());
        to.setResultsLocation(from.getResultsLocation());

        return to;
    }

    @Override
    public CarminDatasetProcessing create(final CarminDatasetProcessing carminDatasetProcessing) {
        CarminDatasetProcessing savedEntity = datasetProcessingRepository.save(carminDatasetProcessing);
        return savedEntity;
    }

    @Override
    public CarminDatasetProcessing update(final Long datasetProcessingId,
            final CarminDatasetProcessing carminDatasetProcessing)
            throws EntityNotFoundException {
        final Optional<DatasetProcessing> entityDbOpt = datasetProcessingRepository
                .findById(datasetProcessingId);
        final DatasetProcessing entityDb = entityDbOpt.orElseThrow(
                () -> new EntityNotFoundException(carminDatasetProcessing.getClass(), carminDatasetProcessing.getId()));

        updateValues(carminDatasetProcessing, (CarminDatasetProcessing) entityDb);
        return (CarminDatasetProcessing) datasetProcessingRepository.save(entityDb);

    }

    @Override
    public CarminDatasetProcessing getCarminDatasetProcessingByComment(String comment) throws EntityNotFoundException {
        return (CarminDatasetProcessing) datasetProcessingRepository.findByComment(comment).orElseThrow(()-> new EntityNotFoundException("carminDatasetProcessing not found with : " + comment));
    }

}
