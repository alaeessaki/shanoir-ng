package org.shanoir.ng.processing.carmin.schedule;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

    // private final String VIP_URI = "/";
    private boolean stop;
    private String identifier;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Async
    @Override
    public void startJob(String identifier) {
        this.identifier = identifier;
        this.stop = false;

        while (!stop) {
            try {

                // if (ExecutionStatus.FINISHED == execution.getStatus()) {
                // /**
                // * updates the status and finish the job
                // */

                // CarminDatasetProcessing carminDatasetProcessing =
                // this.carminDatasetProcessingService
                // .getCarminDatasetProcessingByComment(this.identifier);

                // carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

                // this.carminDatasetProcessingService.update(carminDatasetProcessing.getId(),
                // carminDatasetProcessing);

                // LOG.info("execution status updated stopping job...");

                // stop = true;
                // }

                Thread.sleep(40000);

                CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
                        .getCarminDatasetProcessingByComment(this.identifier);

                carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

                this.carminDatasetProcessingService.update(carminDatasetProcessing.getId(),
                        carminDatasetProcessing);

                LOG.info("execution status updated stopping job...");

                stop = true;

            } catch (InterruptedException e) {
                LOG.error("error in thread sleeping : ", e);
                e.getStackTrace();
            } catch (EntityNotFoundException e) {
                LOG.error("entity not found :", e);
                e.getMessage();
            }

        }
    }

}
