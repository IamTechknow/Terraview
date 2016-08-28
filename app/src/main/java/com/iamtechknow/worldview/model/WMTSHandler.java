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

    //used to fill single string for multiple character() calls
    private StringBuilder buffer;

    //Avoid unwanted elements, indicate that we're in the layer element, not style or dimension
    private boolean inLayerTag, inTileMatrixSetLink;

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
                case "Title":
                    currentElement = localName;
                    buffer = new StringBuilder();
                    break;
                case "TileMatrixSetLink":
                    inTileMatrixSetLink = true;
            }
    }

    /**
     * At the end of an element, we can find out if we're done with a layer object, gone into or out of something relevant
     * and know that the current contents of the buffer are valid.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
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
                if(inLayerTag)
                    currLayer.setTileMatrixSet(buffer.toString());
                break;
            case "TileMatrixSetLink":
                inTileMatrixSetLink = false;
                break;
            case "Identifier":
                if(inLayerTag)
                    currLayer.setIdentifier(buffer.toString());
                break;
            case "Format":
                if(inLayerTag) {
                    String str = buffer.toString();
                    currLayer.setFormat(str.substring(str.indexOf('/') + 1));
                }
                break;
            case "Title":
                if(inLayerTag)
                    currLayer.setTitle(buffer.toString());
        }
        currentElement = null;
    }

    /**
     * Check if the current text is inside a desirable tag, then append it to the buffer
     * because per spec characters() may be called mutiple times
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(currentElement != null && inLayerTag) {
            switch (currentElement) {
                case "Identifier":
                case "Format":
                case "TileMatrixSet":
                case "Title":
                    buffer.append(ch, start, length);
            }
        }
    }

    public ArrayList<Layer> getResult() {
        return contents;
    }
}
