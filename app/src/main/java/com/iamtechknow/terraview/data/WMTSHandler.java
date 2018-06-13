package com.iamtechknow.terraview.data;

import com.iamtechknow.terraview.model.Layer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Parses WMTSCapabilities.xml to obtain layer data. Uses the online version which is the most up-to-date.
 */
public class WMTSHandler extends DefaultHandler {
    private static final int ROLE_IDX = 1, HREF_IDX = 2;

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

    public WMTSHandler() {
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
                    break;
                case "Metadata":
                    parsePalette(currLayer, attributes);
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
     * because per spec characters() may be called multiple times
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

    /**
     * Parse a metadata tag by looking for an exact string and then setting the palette to avoid doing it four times
     */
    private void parsePalette(Layer curr, Attributes attrs) {
        String role = attrs.getValue(ROLE_IDX);
        if(role.equals("http://earthdata.nasa.gov/gibs/metadata-type/colormap")) {
            String val = attrs.getValue(HREF_IDX);
            curr.setPalette(val.substring(val.lastIndexOf('/') + 1, val.lastIndexOf('.')));
        }
    }
}
