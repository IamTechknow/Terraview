package com.iamtechknow.terraview.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Deserialized representation of a color map XML. The root element is ColorMap,
 * ColorMapEntry elements. This allows the elements to be parsed as an element list
 * Non-strict parsing is used to avoid errors with the noNamespaceSchemaLocation attribute
 */
@Root(name="ColorMap", strict = false)
public class ColorMap {

    private ArrayList<ColorMapEntry> list;

    public ColorMap(@ElementList(inline=true) ArrayList<ColorMapEntry> list) {
        this.list = list;
    }

    @ElementList(inline=true)
    public ArrayList<ColorMapEntry> getList() {
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj instanceof ColorMap) {
            ColorMap that = (ColorMap) obj;
            return (list == null && that.list == null) || (list != null && that.list != null && list.equals(that.list));
        }
        return false;
    }
}
