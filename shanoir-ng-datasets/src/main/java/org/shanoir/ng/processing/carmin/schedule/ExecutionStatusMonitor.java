package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.ws.rs.NotFoundException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

    private String VIP_URI = "http://192.168.255.18:9090/rest/executions/";

    private String importDir = "/tmp/vip_uploads";

    private long sleepTime = 20000;

    private boolean stop;
    private String identifier;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ImporterService importerService;

    @Autowired
    private StudyRepository studyRepository;

    @Async
    @Override
    @Transactional
    public void startJob(String identifier) {

        this.identifier = identifier;
        this.stop = false;
        
        String uri = VIP_URI + identifier + "/summary";
        RestTemplate restTemplate = new RestTemplate();

        while (!stop) {

            Execution execution = restTemplate.getForObject(uri, Execution.class);

            try {

                CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
                        .getCarminDatasetProcessingByComment(this.identifier);

                switch (execution.getStatus()) {

                    case FINISHED:
                        /**
                         * updates the status and finish the job
                         */

                        carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

                        this.carminDatasetProcessingService.update(carminDatasetProcessing.getId(),
                                carminDatasetProcessing);

                        // untar the .tgz files
                        final File userImportDir = new File(
                                this.importDir + File.separator +
                                        carminDatasetProcessing.getResultsLocation());

                        final PathMatcher matcher = userImportDir.toPath().getFileSystem()
                                .getPathMatcher("glob:**/*.tgz");
                        final Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath());

                        stream.filter(matcher::matches)
                                .forEach(zipFile -> decompressTGZ(zipFile.toFile(),
                                        userImportDir.getAbsoluteFile(),
                                        carminDatasetProcessing));

                        LOG.info("execution status updated stopping job...");

                        stop = true;

                        break;

                    case UNKOWN:
                    case EXECUTION_FAILED:
                    case KILLED:

                        carminDatasetProcessing.setStatus(execution.getStatus());

                        this.carminDatasetProcessingService.update(carminDatasetProcessing.getId(),
                                carminDatasetProcessing);
                        LOG.info("execution status updated stopping job...");

                        stop = true;
                        break;

                    case RUNNING:
                        Thread.sleep(sleepTime); // sleep/stop a thread for 20 seconds
                        break;

                    default:
                        this.stop = true;
                        break;

                }

            } catch (InterruptedException e) {
                LOG.error("sleep thread exception :", e);
                e.getMessage();
            } catch (EntityNotFoundException e) {
                LOG.error("entity not found :", e);
                e.getMessage();
            } catch (IOException e) {
                LOG.error("file exception :", e);
                e.getMessage();
            }

        }
    }

    /**
     * 
     * @param in
     * @param out
     * @param carminDatasetProcessing
     */
    private void decompressTGZ(File in, File out, CarminDatasetProcessing carminDatasetProcessing) {
        try (TarArchiveInputStream fin = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(in)))) {
            TarArchiveEntry entry;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                File cacheFolder = new File(out.getAbsolutePath() + File.separator + "cache");
                if (!cacheFolder.exists()) {
                    cacheFolder.mkdirs();
                }

                File currentFile = new File(cacheFolder, entry.getName());

                IOUtils.copy(fin, new FileOutputStream(currentFile));

                if (entry.getName().endsWith(".nii.gz")) {
                    createProcessedDataset(currentFile, cacheFolder.getAbsolutePath(),
                            carminDatasetProcessing);
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 
     * @param zipFilePath
     * @param destDir
     * @param carminDatasetProcessing
     */
    private void createProcessedDataset(File niiftiFile, String destDir,
            CarminDatasetProcessing carminDatasetProcessing) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists())
            dir.mkdirs();

        try {

            ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
            DatasetProcessing datasetProcessing = datasetProcessingService
                    .findById(carminDatasetProcessing.getId())
                    .orElseThrow(() -> new NotFoundException("datasetProcessing not found"));

            Study study = studyRepository.findById(datasetProcessing.getStudyId())
                    .orElseThrow(() -> new NotFoundException("study not found"));

            processedDataset.setDatasetProcessing(datasetProcessing);
            LOG.info(niiftiFile.getAbsolutePath());

            processedDataset.setProcessedDatasetFilePath(niiftiFile.getAbsolutePath());
            processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
            processedDataset.setStudyId(datasetProcessing.getStudyId());
            processedDataset.setStudyName(study.getName());

            // TODO get a subject for the processedDataset, hard coded for the time being
            processedDataset.setSubjectId(1L);
            processedDataset.setSubjectName("DemoSubject");
            processedDataset.setProcessedDatasetName(getNameWithoutExtension(niiftiFile.getName()));
            processedDataset.setDatasetType("Mesh");

            importerService.createProcessedDataset(processedDataset);

            // importerService.cleanTempFiles(processedDataset.getProcessedDatasetFilePath());
            deleteCacheDir(Paths.get(destDir));

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    private void deleteCacheDir(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private String getNameWithoutExtension(String file) {
        int dotIndex = file.indexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }

}
