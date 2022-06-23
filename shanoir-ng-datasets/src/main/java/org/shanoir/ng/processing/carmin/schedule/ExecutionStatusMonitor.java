package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.NotFoundException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

    // private final String VIP_URI = "/";
    private boolean stop;
    private String identifier;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    private String importDir = "/tmp/vip_uploads";

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ImporterService importerService;

    @Async
    @Override
    @Transactional
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

                Thread.sleep(5000);

                CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
                        .getCarminDatasetProcessingByComment(this.identifier);

                carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

                this.carminDatasetProcessingService.update(carminDatasetProcessing.getId(),
                        carminDatasetProcessing);

                // unzip the .tgz files
                final File userImportDir = new File(
                        importDir + File.separator + carminDatasetProcessing.getResultsLocation());

                final PathMatcher matcher = userImportDir.toPath().getFileSystem().getPathMatcher("glob:**/*.tgz");
                final Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath());

                stream.filter(matcher::matches)
                        .forEach(zipFile -> decompressTGZ(zipFile.toFile(), userImportDir.getAbsoluteFile(),
                                carminDatasetProcessing));

                LOG.info("execution status updated stopping job...");

                stop = true;

            } catch (InterruptedException e) {
                LOG.error("error in thread sleeping : ", e);
                e.getStackTrace();
            } catch (EntityNotFoundException e) {
                LOG.error("entity not found :", e);
                e.getMessage();
            } catch (IOException e) {
                LOG.error("entity not found :", e);
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
                System.out.println("untaring ... " + entry.getName());
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(out, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                IOUtils.copy(fin, new FileOutputStream(curfile));

                File cacheFolder = new File(out.getAbsolutePath() + File.separator + "cache");
                if (!cacheFolder.exists()) {
                    cacheFolder.mkdirs();
                }

                if (entry.getName().endsWith(".zip")) {
                    createProcessedDataset(curfile.getAbsolutePath(), cacheFolder.getAbsolutePath(),
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
    private void createProcessedDataset(String zipFilePath, String destDir,
            CarminDatasetProcessing carminDatasetProcessing) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists())
            dir.mkdirs();

        FileInputStream fis;
        // buffer for read and write data to file
        byte[] buffer = new byte[1024];

        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);

                // create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);

                int len;

                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                if (fileName.endsWith(".nii.gz")) {
                    ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
                    DatasetProcessing datasetProcessing = datasetProcessingService.findById(carminDatasetProcessing.getId())
                                                            .orElseThrow(() -> new NotFoundException("datasetProcessing not found"));
                    // DatasetProcessing datasetProcessing = new DatasetProcessing();

                    // datasetProcessing.setId(datasetProcessingDB.getId());
                    // datasetProcessing.setDatasetProcessingType(DatasetProcessingType.SEGMENTATION);
                    // datasetProcessing.setProcessingDate(datasetProcessingDB.getProcessingDate());
                    // datasetProcessing.setStudyId(datasetProcessingDB.getStudyId());
                    // datasetProcessing.setInputDatasets(datasetProcessingDB.getInputDatasets());
                    // datasetProcessing.setOutputDatasets(datasetProcessingDB.getOutputDatasets());

                    processedDataset.setDatasetProcessing(datasetProcessing);
                    LOG.info(newFile.getAbsolutePath());

                    processedDataset.setProcessedDatasetFilePath(newFile.getAbsolutePath());
                    processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
                    processedDataset.setStudyId(1L);
                    processedDataset.setStudyName("DemoStudy");
                    processedDataset.setSubjectId(1L);
                    processedDataset.setSubjectName("DemoSubject");
                    processedDataset.setProcessedDatasetName(fileName);
                    processedDataset.setDatasetType("Mesh");

                    importerService.createProcessedDataset(processedDataset);

                    importerService.cleanTempFiles(processedDataset.getProcessedDatasetFilePath());
                }

                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            // close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

    }

}
