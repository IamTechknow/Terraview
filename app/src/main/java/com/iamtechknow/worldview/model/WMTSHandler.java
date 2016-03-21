package com.iamtechknow.worldview.model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Parses WMTSCapabilities.xml to obtain layer data. Uses the online version which is the most up-to-date.
 */
public class WMTSHandler extends DefaultHandler {

    //Container for layers
    private ArrayList<Layer> contents;

    //Current layer
    private Layer currLayer;

    //The current element right now
    private String currentElement;

    public WMTSHandler() throws ParserConfigurationException, SAXException {
        contents = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //We're at the beginning of an element, check what it is
        //Make a layer obect or save element string
        switch(localName) {
            case "Layer":
                currLayer = new Layer();
                break;
            case "Title":
            case "Format":
            case "TileMatrixSet":
                currentElement = localName;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //At the end of an element, we just need to know if we're finished with a Layer element
        if(localName.equals("Layer"))
            contents.add(currLayer);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //We have access to the characters, check the element first then save it
        switch(currentElement) {
            case "Title":
                currLayer.setTitle(new String(ch, start, length));
                break;
            case "Format":
                String str = new String(ch, start, length);
                currLayer.setFormat(str.substring(str.indexOf('/') + 1));
                break;
            case "TileMatrixSet":
                currLayer.setTileMatrixSet(new String(ch, start, length));
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        //TODO: Save data to database, use callback to get result?
    }

    public ArrayList<Layer> getResult() {
        return contents;
    }
}
