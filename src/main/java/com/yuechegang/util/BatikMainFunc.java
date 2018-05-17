package com.yuechegang.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Created by cn40580 at 2018-05-15 4:50 PM.
 */
public class BatikMainFunc {
    public static void main(String[] args) throws IOException {

        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D g = new SVGGraphics2D(document);

        g.drawArc(30, 30, 40, 40, 90, 90);

        // we want to use CSS style attributes
        boolean useCSS = true;

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        Writer out = new OutputStreamWriter(new FileOutputStream(new File("C:\\Users\\cn40580\\Desktop\\abc.svg")), "UTF-8");

        g.stream(out, useCSS);
    }
}
