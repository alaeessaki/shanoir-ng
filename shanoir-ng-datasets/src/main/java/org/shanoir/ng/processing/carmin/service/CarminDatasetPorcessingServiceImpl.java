package org.shanoir.ng.processing.carmin.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.repository.CarminDatasetProcessingRepository;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarminDatasetPorcessingServiceImpl implements CarminDatasetProcessingService {

        @Autowired
        private DatasetProcessingRepository datasetProcessingRepository;

        @Autowired
        private CarminDatasetProcessingRepository carminDatasetProcessingRepository;

        @Autowired
        private ShanoirEventService eventService;

        protected CarminDatasetProcessing updateValues(CarminDatasetProcessing from, CarminDatasetProcessing to) {
                to.setIdentifier(from.getIdentifier());
                to.setStatus(from.getStatus());
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
                ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT,
                                carminDatasetProcessing.getResultsLocation(), KeycloakUtil.getTokenUserId(),
                                "Starting import...",
                                ShanoirEvent.IN_PROGRESS, 0f);
                eventService.publishEvent(event);

                CarminDatasetProcessing savedEntity = datasetProcessingRepository.save(carminDatasetProcessing);

                event.setStatus(ShanoirEvent.SUCCESS);
                event.setMessage(carminDatasetProcessing.getPipelineIdentifier() + "("
                                + carminDatasetProcessing.getStudyId() + ")"
                                + ": Successfully created carmin dataset processing ");
                event.setProgress(1f);
                eventService.publishEvent(event);

                return savedEntity;
        }

        @Override
        public CarminDatasetProcessing update(final Long datasetProcessingId,
                        final CarminDatasetProcessing carminDatasetProcessing)
                        throws EntityNotFoundException {
                final Optional<DatasetProcessing> entityDbOpt = datasetProcessingRepository
                                .findById(datasetProcessingId);
                final DatasetProcessing entityDb = entityDbOpt.orElseThrow(
                                () -> new EntityNotFoundException(carminDatasetProcessing.getClass(),
                                                carminDatasetProcessing.getId()));

                updateValues(carminDatasetProcessing, (CarminDatasetProcessing) entityDb);
                return (CarminDatasetProcessing) datasetProcessingRepository.save(entityDb);

        }

        @Override
        public CarminDatasetProcessing getCarminDatasetProcessingByComment(String comment)
                        throws EntityNotFoundException {
                return (CarminDatasetProcessing) datasetProcessingRepository.findByComment(comment)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "carminDatasetProcessing not found with : " + comment));
        }

        @Override
        public List<CarminDatasetProcessing> getCarminDatasetProcessings() {
                return Utils.toList(carminDatasetProcessingRepository.findAllByCarminDatasetProcessingByIdDesc());
        }

}
