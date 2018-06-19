package com.yuechegang.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuechegang.service.impl.ReportingServiceImpl;

public class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingServiceImpl.class);

    public static void createDirectory(final String path) {
        Objects.requireNonNull(path);
        File dir = null;
        if (path.endsWith(File.separator)) {
            dir = new File(path);
        } else {
            dir = new File(path.substring(0, path.lastIndexOf(File.separator)));
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static boolean renameFile(final String oldFullPath, final String newFullPath) {
        if (StringUtils.isNotBlank(oldFullPath) && StringUtils.isNotBlank(newFullPath)
                && !StringUtils.equals(oldFullPath, newFullPath)) {
            File oldFile = new File(oldFullPath);
            File newFile = new File(newFullPath);
            if (oldFile.exists() && !newFile.exists()) {
                return oldFile.renameTo(newFile);
            }
        }
        return false;
    }

    public static void copy(final String sourcePath, final String targetPath) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(new File(sourcePath));
            fo = new FileOutputStream(new File(targetPath));
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteFile(final String filePath) {
        File file = new File(filePath);
        boolean result = true;
        if (file.exists()) {
            if (file.isFile()) {
                result = result && file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    result = result && FileUtil.deleteFile(f.getAbsolutePath());
                }
                result = result && file.delete();
            }
        } else {
            result = false;
        }
        return result;
    }


    /**
     * get all files under some directory
     *
     * @param folderName
     * @param includeSubFolder
     * @return
     */
    public static List<File> getAllFiles(String folderName, boolean includeSubFolder) {

        List<File> fileList = new ArrayList<File>();
        getAllFiles(folderName, includeSubFolder, fileList);
        return fileList;
    }

    /**
     * get all files under some directory
     *
     * @param folderName
     * @param includeSubFolder
     * @param fileList
     * @return
     */
    private static List<File> getAllFiles(String folderName, boolean includeSubFolder, List<File> fileList) {

        if (fileList == null) {
            fileList = new ArrayList<File>();
        }

        File folder = new File(folderName);
        if (!folder.exists())
            LOG.warn("The folder: " + folder + " doesn't exist!");

        if (!folder.isDirectory())
            LOG.warn("The folder: " + folder + " is not a Directory!");

        File[] files = folder.listFiles();
        if (files == null || files.length < 1)
            LOG.warn("The folder: " + folder + " is a Empty directory!");

        if (files != null) {
            for (File file : files) {

                if (file != null) {
                    LOG.debug("\tLoop file [" + file.getAbsolutePath() + "]");
                    if (!file.isFile() && !file.isDirectory()) {
                        LOG.debug("The file [" + file.getAbsolutePath() + "] is not a File or Directory");
                        continue;
                    }
                    if (file.isDirectory() && includeSubFolder) {
                        getAllFiles(file.toString(), includeSubFolder, fileList);
                        continue;
                    }
                    if (file.isFile()) {
                        fileList.add(file);
                    }
                }
            }
        }

        return fileList;
    }

    public static File[] sortBy(File files[]) {
        try {
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File file1 = files[i];
                    if (i != files.length - 1) {
                        for (int j = i + 1; j < files.length; j++) {
                            File file2 = files[j];
                            String file1Name = file1.getName().substring(0, 20);
                            String file2Name = file2.getName().substring(0, 20);
                            if (file1Name.compareTo(file2Name) > 0) {
                                File temp = file1;
                                file1 = file2;
                                file2 = temp;
                            }
                        }
                    }
                }

            }
            return files;
        } catch (Exception e) {
            LOG.error("error occurred:IEUtils sortBy method error!", e);
            return files;
        }

    }

    /**
     * @param folderName
     * @throws Exception
     * @title monitorFolder
     * @Description monitor the folder
     */
    public static List<File> scanFolder(String folderName) throws Exception {
        File folder = new File(folderName);
        List<File> resultFileList = new ArrayList<File>();

        if (!folder.exists()) {
            LOG.warn("The folder: " + folder + " doesn't exist!");
            return resultFileList;
        }

        if (!folder.isDirectory()) {
            LOG.warn("The folder: " + folder + " is not a Directory!");
            return resultFileList;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length < 1) {
            LOG.warn("The folder: " + folder + " is a Empty directory!");
            return resultFileList;
        }

        if (files != null) {

            files = sortBy(files);
            for (File file : files) {

                if (file != null) {
                    LOG.debug("\tLoop file [" + file.getAbsolutePath() + "]");
                    if (!file.isFile()) {
                        LOG.debug("The file [" + file.getAbsolutePath() + "] is not a File");
                        continue;
                    }
                    long lastModifiedTime = file.lastModified();
                    LOG.debug("The file [" + file.getAbsolutePath() + "] lastModifiedTime: " + lastModifiedTime);
                    // if (System.currentTimeMillis() - lastModifiedTime < 2000)
                    // continue;
                    resultFileList.add(file);
                }
            }
        }
        LOG.debug("resultFileList size: " + resultFileList);
        return resultFileList;
    }

    /**
     * move file to Directory
     *
     * @param srcFile
     * @param backupFolder
     * @throws Exception
     */
    public static File moveToDirectory(File srcFile, String backupFolder) throws Exception {
        // String errorFolder = fce.getErrorFolder();

        String fileAbPath = srcFile.getAbsolutePath();
        String fileDir = srcFile.getParent();
        String fileName = srcFile.getName();

        if (StringUtils.isEmpty(backupFolder)) {
            backupFolder = fileDir;
        }

        File destFile = new File(backupFolder);

        if (!destFile.exists()) {
            destFile.mkdirs();
        }

        // String backupFileName = backupFolder + File.separator + fileName;
        File backupFile = new File(backupFolder, fileName);

        if (backupFile.exists()) {
            backupFile.delete();
        }

        LOG.info("--------------   fileDir: " + fileDir);
        LOG.info("--------------fileAbPath: " + fileAbPath);
        LOG.info("--------------  fileName: " + fileName);

        if (srcFile.exists()) {
            FileUtils.copyFileToDirectory(srcFile, destFile, true);
        }

        LOG.info("Move " + srcFile + " to " + destFile);

        return new File(backupFolder, fileName);
    }
}
