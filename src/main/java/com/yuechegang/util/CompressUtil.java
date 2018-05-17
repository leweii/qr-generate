package com.yuechegang.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class CompressUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CompressUtil.class);

    public static String compressFiles(final List<File> files, final String dest, final String password) {
        Objects.requireNonNull(files);
        if (files.size() > 0) {
            try {
                ZipFile zipFile = new ZipFile(dest);
                ArrayList<File> temp = new ArrayList<File>();
                temp.addAll(files);
                zipFile.addFiles(temp, getZipParameters(password));
                return dest;
            } catch (ZipException e) {
                LOG.error(e.getMessage());
            }
        }
        return null;
    }

    public static String compressFile(final File file, final String dest, final boolean isCreateDir, final String password) {
        try {
            ZipFile zipFile = new ZipFile(dest);
            if (file.isDirectory()) {
                if (!isCreateDir) {
                    File[] subFiles = file.listFiles();
                    ArrayList<File> temp = new ArrayList<File>();
                    Collections.addAll(temp, subFiles);
                    zipFile.addFiles(temp, getZipParameters(password));
                    return dest;
                }
                zipFile.addFolder(file, getZipParameters(password));
            } else {
                zipFile.addFile(file, getZipParameters(password));
            }
            return dest;
        } catch (ZipException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    private static ZipParameters getZipParameters(final String passwd) {
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (!StringUtils.isEmpty(passwd)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(passwd.toCharArray());
        }
        return parameters;
    }

    /*    private static String buildDestinationZipFilePath(final File source, String destPath) {
            if (StringUtils.isEmpty(destPath)) {
                if (source.isDirectory()) {
                    destPath = source.getParent() + File.separator + source.getName() + SUFFIX;
                } else {
                    String fileName = source.getName().substring(0, source.getName().lastIndexOf("."));
                    destPath = source.getParent() + File.separator + fileName + SUFFIX;
                }
            } else {
                FileUtil.createDirectory(destPath);
                if (destPath.endsWith(File.separator)) {
                    String fileName = "";
                    if (source.isDirectory()) {
                        fileName = source.getName();
                    } else {
                        fileName = source.getName().substring(0, source.getName().lastIndexOf("."));
                    }
                    destPath += fileName + SUFFIX;
                }
            }
            return destPath;
        }*/
}
