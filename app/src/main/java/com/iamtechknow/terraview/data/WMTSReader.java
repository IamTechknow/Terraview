package com.iamtechknow.terraview.data;

import com.iamtechknow.terraview.model.Layer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Uses a XMLReader with our custom handler function to parse the XML.
 */
public class WMTSReader {
    private XMLReader reader;

    public WMTSReader() throws ParserConfigurationException, SAXException {
        //Create the XMLReader object
        SAXParserFactory _f = SAXParserFactory.newInstance();
        SAXParser _p = _f.newSAXParser();
        reader = _p.getXMLReader();
        reader.setContentHandler(new WMTSHandler());
    }

    public void run(InputStream is) throws IOException, SAXException {
        reader.parse(new InputSource(is));
    }

    public ArrayList<Layer> getResult() {
        //We can access the content handler to get the result
        WMTSHandler handler = (WMTSHandler) reader.getContentHandler();
        return handler.getResult();
    }
}
