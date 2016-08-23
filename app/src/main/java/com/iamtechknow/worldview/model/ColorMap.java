package com.iamtechknow.worldview.model;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Deserialized representation of a color map XML. The root element is ColorMap,
 * ColorMapEntry elements. This allows the elements to be parsed as an element list
 */
@Root(name="ColorMap")
public class ColorMap {
    @ElementList(inline=true)
    private ArrayList<ColorMapEntry> list;

    public ColorMap(@ElementList(inline=true) ArrayList<ColorMapEntry> list) {
        this.list = list;
    }

    @ElementList(inline=true)
    public ArrayList<ColorMapEntry> getList() {
        return list;
    }
}
