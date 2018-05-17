package com.yuechegang.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yuechegang.service.QrGenerateService;
import com.yuechegang.util.FileUtil;

@Service
public class QrGenerateServiceImpl implements QrGenerateService {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingServiceImpl.class);

    @Value("${directory.input}")
    private String inputQrPath;

    @Value("${directory.module}")
    private String modulePath;

    @Value("${directory.input_done}")
    private String donePath;

    @Value("${directory.output}")
    private String outputPath;

    @Override
    public void generateFile() {
        List<File> qrFiles = FileUtil.getAllFiles(inputQrPath, true);

        Map<String, String> map = new HashMap<>();
        qrFiles.stream().forEach(f -> {
            try {
                convertToPngByFile(f.getAbsolutePath(), outputPath + f.getName() + ".png", map);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (TranscoderException e) {
                e.printStackTrace();
            }
        });


    }

    public void convertToPngByFile(String filePath, String pngFilePath, Map<String, String> map)
            throws IOException, TranscoderException {
        File file = new File(pngFilePath);
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            convertToPngByFile(filePath, outputStream, map);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void convertToPngByFile(String path, OutputStream outputStream, Map<String, String> map)
            throws TranscoderException, IOException {
        try {
            File file = new File(path);
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            Document doc = f.createDocument(file.toURI().toString());
            for (int i = 1; i <= map.size() / 3; i++) {
                Element e = doc.getElementById(map.get("id" + i));
                System.out.println(map.get("name" + i));
                e.setAttribute(map.get("name" + i), map.get("value" + i));
            }
            PNGTranscoder t = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(doc);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            t.transcode(input, output);
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}