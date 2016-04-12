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

    //Avoid unwanted elements, indicate that we're in the layer element, not style or dimension
    private boolean inLayerTag = true, inTileMatrixSetLink;

    public WMTSHandler() throws ParserConfigurationException, SAXException {
        contents = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //We're at the beginning of an element, check what it is
        //Make a layer object or save element string
        //Don't do this if we're not in the layer top level element, wait until we exit unwanted elements
        if(localName.equals("Layer")) {
            currLayer = new Layer();
            inLayerTag = true;
        }

        if(inLayerTag)
            switch(localName) {
                case "Style": //ignore these three tags and everything within
                case "Dimension":
                case "TileMatrix":
                    inLayerTag = false;
                    break;
                case "Identifier":
                case "Format":
                case "TileMatrixSet":
                    currentElement = localName;
                    break;
                case "TileMatrixSetLink":
                    inTileMatrixSetLink = true;
            }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //At the end of an element, we just need to know if we're finished with a Layer element
        switch (localName) {
            case "Layer":
                contents.add(currLayer);
                inLayerTag = false;
                break;
            case "Style":
            case "Dimension":
                inLayerTag = true;
                break;
            case "TileMatrixSet":
                inLayerTag = inTileMatrixSetLink; //TileMatrixSet may appear outside of a layer tag, we only want the ones that come after TileMatrixSetLink
                break;
            case "TileMatrixSetLink":
                inTileMatrixSetLink = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //We have access to the characters, check the element first then save it
        if(currentElement != null && inLayerTag) {
            switch (currentElement) {
                case "Identifier":
                    currLayer.setIdentifier(new String(ch, start, length));
                    break;
                case "Format":
                    String str = new String(ch, start, length);
                    currLayer.setFormat(str.substring(str.indexOf('/') + 1));
                    break;
                case "TileMatrixSet":
                    currLayer.setTileMatrixSet(new String(ch, start, length));
            }
            currentElement = null; //we are done, reset current element and wait for next one
        }
    }

    public ArrayList<Layer> getResult() {
        return contents;
    }
}
