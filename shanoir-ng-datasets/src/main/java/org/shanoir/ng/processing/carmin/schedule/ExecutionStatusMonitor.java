package org.shanoir.ng.processing.carmin.schedule;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExecutionStatusMonitor extends Thread {

    private final String VIP_URI = "/";
    private boolean stop = false;
    private String identifier;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;


    @Override
    public void run() {

        /**
         * calls VIP api (needs keycloak token ??)
         */
        // RestTemplate restTemplate = new RestTemplate();
        // Execution execution = restTemplate.getForObject(VIP_URI + identifier,
        // Execution.class);

        Execution execution = new Execution("workflow-0OEKpv", ExecutionStatus.RUNNING);

        while (!stop) {
            try {
                if (ExecutionStatus.FINISHED == execution.getStatus()) {
                    /**
                     * updates the status and finish the job
                     */
                    LOG.info("finished execution...");
                    LOG.info(this.identifier);

                    DatasetProcessing datasetProcessing = this.datasetProcessingService.findByComment(this.identifier)
                            .orElseThrow(() -> new EntityNotFoundException("not found by :" + this.identifier));
                    LOG.info(datasetProcessing.getComment());

                    CarminDatasetProcessing carminDatasetProcessing = (CarminDatasetProcessing) datasetProcessing;

                    LOG.info(carminDatasetProcessing.getIdentifier());
                    carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);
                    this.carminDatasetProcessingService.update(carminDatasetProcessing.getId(),
                            carminDatasetProcessing);

                    LOG.info("execution status updated stopping job...");

                    stop = true;
                }

                LOG.info("Execution Status not finished yet ..." + execution.getStatus());
                Thread.sleep(20000);
                execution.setStatus(ExecutionStatus.FINISHED);

            } catch (InterruptedException e) {
                LOG.error("error in thread sleeping : ", e);
                e.getStackTrace();
            } catch (EntityNotFoundException e) {
                LOG.error("entity not found :", e);
                e.getMessage();
            }

        }

    }


    public String getIdentifier() {
        return identifier;
    }


    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    
}
