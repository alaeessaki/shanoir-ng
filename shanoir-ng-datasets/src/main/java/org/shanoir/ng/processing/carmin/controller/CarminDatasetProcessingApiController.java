package org.shanoir.ng.processing.carmin.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.shanoir.ng.processing.carmin.dto.CarminDatasetProcessingDTO;
import org.shanoir.ng.processing.carmin.dto.mapper.CarminDatasetProcessingMapper;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.schedule.ExecutionStatusMonitor;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class CarminDatasetProcessingApiController implements CarminDatasetProcessingApi {

    @Autowired
    private CarminDatasetProcessingMapper datasetProcessingMapper;

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ExecutionStatusMonitor executionStatusMonitor;

    @Override
    public ResponseEntity<CarminDatasetProcessingDTO> saveNewCarminDatasetProcessing(
            @Valid @RequestBody CarminDatasetProcessing carminDatasetProcessing, BindingResult result)
            throws RestServiceException {

        /* Validation */
        validate(result);

        /* Save dataset processing in db. */
        final CarminDatasetProcessing createdDatasetProcessing = carminDatasetProcessingService
                .create(carminDatasetProcessing);

        /**
         * run monitoring job
         */
        executionStatusMonitor.setIdentifier(carminDatasetProcessing.getIdentifier());
        executionStatusMonitor.start();

        return new ResponseEntity<>(
                datasetProcessingMapper.carminDatasetProcessingToCarminDatasetProcessingDTO(createdDatasetProcessing),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarminDatasetProcessingDTO> findCarminDatasetProcessingByComment(
            @ApiParam(value = "id of the dataset processing", required = true) @RequestParam("comment") String comment) {

        final Optional<DatasetProcessing> datasetProcessing = datasetProcessingService.findByComment(comment);
        if (!datasetProcessing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(datasetProcessingMapper.carminDatasetProcessingToCarminDatasetProcessingDTO(
                (CarminDatasetProcessing) datasetProcessing.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateCarminDatasetProcessing(
            @ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
            @ApiParam(value = "dataset processing to update", required = true) @Valid @RequestBody CarminDatasetProcessing carminDatasetProcessing,
            final BindingResult result) throws RestServiceException {

        validate(result);
        try {
            carminDatasetProcessingService.update(datasetProcessingId, carminDatasetProcessing);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private void validate(BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap(result);
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                    new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

}
