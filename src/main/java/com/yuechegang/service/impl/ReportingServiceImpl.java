package com.yuechegang.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yuechegang.StatusEnum;
import com.yuechegang.service.ReportingService;
import com.yuechegang.util.CompressUtil;
import com.yuechegang.util.FileUtil;

@Service
public class ReportingServiceImpl implements ReportingService {
    public static final String SUFFIX_ZIP = ".zip";
    public static final String UPLOADED_SUCCESS = "uploaded.sucess";
    public static final int FTP_RENAME_RETRY = 1;
    private static final Logger LOG = LoggerFactory.getLogger(ReportingServiceImpl.class);
    private static final String FILE_BASIC_PATTERN = ".+_\\d{8}+_\\d+";
    private static final String FOLDER_TO_BE_COMPRESSED_PATTERN = FILE_BASIC_PATTERN + "_" + StatusEnum.READY.name();
    private static final String FOLDER_TO_BE_UPLOADED_PATTERN = FILE_BASIC_PATTERN + "_" + StatusEnum.COMPRESSED.name();
    private static final String FOLDER_TO_BE_ARCHIVED_PATTERN = FILE_BASIC_PATTERN + "_" + StatusEnum.UPLOADING.name();
    private static final String FILE_ZIP_PATTERN = FILE_BASIC_PATTERN + "\\.zip";
    private static final String FILE_CSV_PATTERN = "(" + FILE_BASIC_PATTERN + ")_(\\d+)(_\\d+)\\.csv";
    @Value("${directory.source}")
    private String sourceFolder;
    @Value("${directory.archive}")
    private String archiveFolder;
    @Value("${directory.temp}")
    private String tempFolder;

    @Value("${zip.password}")
    private String password;
    /*    @Value("${source.path}")
        private String csvSourcePath;*/
    @Value("${wincp.path}")
    private String wincpPath;
    @Value("${ftp.target.serverUrl}")
    private String targetServerUrl;
    @Value("${ftp.target.username}")
    private String targetUserName;
    @Value("${ftp.target.password}")
    private String targetPassword;
    @Value("${ftp.target.certificate}")
    private String targetCertificate;

    /*
     * create directory folder "Data" if it is not exist
     * create directory folder "Zips" if it is not exist
     * create directory folder "Tmp" if it is not exist
     */
    @PostConstruct
    public void createWorkDirectory() {
        FileUtil.createDirectory(sourceFolder);
        FileUtil.createDirectory(archiveFolder);
        FileUtil.createDirectory(tempFolder);
    }

    /*
     * Move csv file from folder "Data" to "Temp" and group them by file name
     * @see ReportingService#downloadFiles()
     */
    @Override
    public synchronized void groupFiles() {
        File csvSourceFolder = new File(sourceFolder);
        if (csvSourceFolder.exists() && csvSourceFolder.isDirectory()) {
            File[] files = csvSourceFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.matches(FILE_CSV_PATTERN);
                }
            });
            if (files != null && files.length > 0) {
                for (File f : files) {
                    String folderName = getFolderName(f.getName());
                    if (StringUtils.isNotBlank(folderName)) {
                        String targetFolder = tempFolder + folderName + File.separator;
                        FileUtil.createDirectory(targetFolder);
                        String newFileName = targetFolder + f.getName();
                        boolean result = FileUtil.renameFile(f.getAbsolutePath(), newFileName);
                        LOG.info(String.format("Rename file '%s' to '%s:%s'", f.getAbsolutePath(), newFileName, result));
                    } else {
                        LOG.error("unrecongized file " + f.getName());
                    }
                }
            }
        }
    }

    @Override
    public synchronized void validateFiles() {
        File directory = new File(tempFolder);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.matches(FILE_BASIC_PATTERN);
            }
        });
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] csvFiles = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return name.matches(FILE_CSV_PATTERN);
                        }
                    });
                    if (csvFiles != null && csvFiles.length > 0) {
                        File firstFile = csvFiles[0];
                        Matcher m = Pattern.compile(FILE_CSV_PATTERN).matcher(firstFile.getName());
                        boolean areAllDownloaded = false;
                        if (m.matches()) {
                            areAllDownloaded = (csvFiles.length == Integer.valueOf(m.group(2)));
                        }
                        // if all the files are ready, then add suffix _READY to the folder
                        if (areAllDownloaded) {
                            String newFilePath = file.getAbsolutePath() + "_" + StatusEnum.READY.name();
                            boolean result = FileUtil.renameFile(file.getAbsolutePath(), newFilePath);
                            LOG.info(String.format("All file are ready. Rename file '%s' to '%s:%s'", file.getAbsolutePath(), newFilePath,
                                    result));
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized void compressFiles() {
        File directory = new File(tempFolder);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.matches(FOLDER_TO_BE_COMPRESSED_PATTERN);
            }
        });
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] csvFiles = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return name.matches(FILE_CSV_PATTERN);
                        }
                    });
                    if (csvFiles != null && csvFiles.length > 0) {
                        String zipFilePath = file.getAbsolutePath() + File.separator + file.getName().replace("_" + StatusEnum.READY.name(), "")
                                + SUFFIX_ZIP;
                        LOG.info(String.format("Compressing %s file(s) to zip file '%s'", csvFiles.length,
                                zipFilePath));
                        CompressUtil.compressFiles(Arrays.asList(csvFiles), zipFilePath, password);
                        String newFilePath = changeFileStatus(file, StatusEnum.READY, StatusEnum.COMPRESSED);
                        boolean result = FileUtil.renameFile(file.getAbsolutePath(), newFilePath);
                        LOG.info(String.format("Rename file '%s' to '%s:%s'", file.getAbsolutePath(), newFilePath, result));
                    }
                }
            }
        }
    }

    @Override
    public synchronized void uploadFiles() {
        File directory = new File(tempFolder);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.matches(FOLDER_TO_BE_UPLOADED_PATTERN);
            }
        });

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newFilePath = changeFileStatus(file, StatusEnum.COMPRESSED, StatusEnum.UPLOADING);
                    boolean result = FileUtil.renameFile(file.getAbsolutePath(), newFilePath);
                    LOG.info(String.format("Rename file '%s' to '%s:%s'", file.getAbsolutePath(), newFilePath, result));
                    try {
                        File[] zipFiles = new File(newFilePath)
                                .listFiles(new FilenameFilter() {
                                    @Override
                                    public boolean accept(final File dir, final String name) {
                                        return name.matches(FILE_ZIP_PATTERN);
                                    }
                                });
                        if (zipFiles != null && zipFiles.length > 0) {
                            LOG.info(String.format("Uploading file '%s' to %s", zipFiles[0].getAbsolutePath(), targetServerUrl));
                            boolean isUploaded = uploadViaWinCP(zipFiles[0].getAbsolutePath());
                            if (isUploaded) {
                                // create a file named "uploaded.sucess" to indicate the zip has been uploaded to FTP
                                new File(newFilePath + File.separator + UPLOADED_SUCCESS).createNewFile();
                                LOG.info(String.format("Created file '%s'", newFilePath + File.separator + UPLOADED_SUCCESS));
                            } else {
                                LOG.warn("Upload file failed!");
                                LOG.info(String.format("Rename file '%s' to '%s:%s'", newFilePath, file.getAbsolutePath(),
                                        FileUtil.renameFile(newFilePath, file.getAbsolutePath())));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG.error(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void archiveFiles() {
        File directory = new File(tempFolder);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.matches(FOLDER_TO_BE_ARCHIVED_PATTERN);
            }
        });

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] successFiles = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return UPLOADED_SUCCESS.equals(name);
                        }
                    });
                    // check if file already uploaded to FTP
                    if (successFiles != null && successFiles.length > 0) {
                        File[] zipFiles = file.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(final File dir, final String name) {
                                return name.matches(FILE_ZIP_PATTERN);
                            }
                        });
                        if (zipFiles != null) {
                            if (zipFiles.length == 1) {
                                File zipFile = zipFiles[0];
                                String newFilePath = archiveFolder + zipFile.getName();
                                boolean result = FileUtil.renameFile(zipFile.getAbsolutePath(), newFilePath);
                                LOG.info(String.format("Rename file '%s' to '%s:%s'", zipFile.getAbsolutePath(), newFilePath, result));
                                if (result) {
                                    LOG.info(String.format("Delete file '%s':%s'", file.getAbsolutePath(),
                                            FileUtil.deleteFile(file.getAbsolutePath())));
                                }
                            } else if (zipFiles.length > 1) {
                                LOG.warn("This folder contains more than 1 .zip files, this is not possible!");
                            }
                        }
                    }
                }
            }
        }
    }

    private String changeFileStatus(final File file, final StatusEnum from, final StatusEnum to) {
        return file.getAbsolutePath().replace("_" + from.name(), "_" + to.name());
    }

    private String getFolderName(final String fileName) {
        Pattern p = Pattern.compile(FILE_CSV_PATTERN);
        Matcher m = p.matcher(fileName);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private boolean uploadViaWinCP(final String filePath) {
        String exec = String
                .format("%s /command \"open ftpes://%s:%s@%s/ -certificate=\"%s\" -passive=04\" \"put %s\" \"exit\"", wincpPath
                        , targetUserName, targetPassword, targetServerUrl, targetCertificate, filePath);
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(exec);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                LOG.debug(line);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
        return false;
    }
}