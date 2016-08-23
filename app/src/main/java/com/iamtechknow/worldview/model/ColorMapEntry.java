package com.iamtechknow.worldview.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="ColorMapEntry")
public class ColorMapEntry {

    @Attribute(name="rgb")
    private String rgb;

    @Attribute(name="value", required=false)
    private String val;

    @Attribute(name="label")
    private String label;

    @Attribute(name="transparent")
    private boolean transparent;

    private final byte r, g, b;

    private final boolean isInvalid;

    public ColorMapEntry(@Attribute(name="rgb") String rgb, @Attribute(name="value", required=false) String val,
                         @Attribute(name="label") String label, @Attribute(name="transparent") boolean transparent) {
        this.rgb = rgb;
        this.val = val;
        this.label = label;
        this.transparent = transparent;

        //Get RGB values
        String[] temp = rgb.split(",");
        r = Byte.parseByte(temp[0]);
        g = Byte.parseByte(temp[1]);
        b = Byte.parseByte(temp[2]);

        isInvalid = label.contains("No Data") || label.contains("Unknown");
    }

    public String getVal() {
        return val;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public byte getR() {
        return r;
    }

    public byte getG() {
        return g;
    }

    public byte getB() {
        return b;
    }

    public boolean isInvalid() {
        return isInvalid;
    }
}
