package com.iamtechknow.worldview.model;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class WMTSReader {
    private XMLReader reader;

    public WMTSReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory _f = SAXParserFactory.newInstance();
        SAXParser _p = _f.newSAXParser();
        reader = _p.getXMLReader();
        reader.setContentHandler(new WMTSHandler());
    }

    public void run(InputStream is) throws IOException, SAXException {
        reader.parse(new InputSource(is));
    }

    public ArrayList<Layer> getResult() {
        WMTSHandler handler = (WMTSHandler) reader.getContentHandler();
        return handler.getResult();
    }
}
