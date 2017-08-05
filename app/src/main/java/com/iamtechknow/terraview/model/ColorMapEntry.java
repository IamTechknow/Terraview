package com.iamtechknow.terraview.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="ColorMapEntry", strict = false)
public class ColorMapEntry {

    @Attribute(name="rgb")
    private String rgb;

    @Attribute(name="value", required=false)
    private String val;

    @Attribute(name="label")
    private String label;

    private final int r, g, b;

    private final boolean isInvalid;

    public ColorMapEntry(@Attribute(name="rgb") String rgb, @Attribute(name="value", required=false) String val,
                         @Attribute(name="label") String label) {
        this.rgb = rgb;
        this.val = val;
        this.label = label;

        //Get RGB values
        String[] temp = rgb.split(",");
        r = Integer.parseInt(temp[0]);
        g = Integer.parseInt(temp[1]);
        b = Integer.parseInt(temp[2]);

        isInvalid = label.equalsIgnoreCase("No Data") || label.contains("Unknown") || label.equals("Fill") || label.equals("Land") || label.equals("Ice") || label.equals("Water");
    }

    public String getVal() {
        return val;
    }

    public String getLabel() {
        return label;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    /**
     * Formula for the hash code, use the 31x + y rule then combine with String hashcode
     * @return hash code for a color map entry
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + r;
        hash = 31 * hash + g;
        hash = 31 * hash + b;
        return hash + label.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof ColorMapEntry))
            return false;
        ColorMapEntry entry = (ColorMapEntry) obj;
        return r == entry.getR() && g == entry.getG() && b == entry.getB();
    }
}
