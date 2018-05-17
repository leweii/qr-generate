package com.yuechegang.service;


public interface ReportingService {
    // group csv files by name and move them to "Tem" folder
    void groupFiles();

    // validate if all files are ready
    void validateFiles();

    // compress files
    void compressFiles();

    // upload ZIP file via FTP
    void uploadFiles();

    // archive files
    void archiveFiles();

}
