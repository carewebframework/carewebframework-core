/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.common;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Color-related utilities.
 */
public class ColorUtil {
    
    private static final String[][] NAMED_COLORS = { { "none", "" }, { "Black", "#000000" }, { "Navy", "#000080" },
            { "DarkBlue", "#00008B" }, { "MediumBlue", "#0000CD" }, { "Blue", "#0000FF" }, { "DarkGreen", "#006400" },
            { "Green", "#008000" }, { "Teal", "#008080" }, { "DarkCyan", "#008B8B" }, { "DeepSkyBlue", "#00BFFF" },
            { "DarkTurquoise", "#00CED1" }, { "MediumSpringGreen", "#00FA9A" }, { "Lime", "#00FF00" },
            { "SpringGreen", "#00FF7F" }, { "Aqua", "#00FFFF" }, { "Cyan", "#00FFFF" }, { "MidnightBlue", "#191970" },
            { "DodgerBlue", "#1E90FF" }, { "LightSeaGreen", "#20B2AA" }, { "ForestGreen", "#228B22" },
            { "SeaGreen", "#2E8B57" }, { "DarkSlateGray", "#2F4F4F" }, { "LimeGreen", "#32CD32" },
            { "MediumSeaGreen", "#3CB371" }, { "Turquoise", "#40E0D0" }, { "RoyalBlue", "#4169E1" },
            { "SteelBlue", "#4682B4" }, { "DarkSlateBlue", "#483D8B" }, { "MediumTurquoise", "#48D1CC" },
            { "Indigo", "#4B0082" }, { "DarkOliveGreen", "#556B2F" }, { "CadetBlue", "#5F9EA0" },
            { "CornflowerBlue", "#6495ED" }, { "MediumAquaMarine", "#66CDAA" }, { "DimGray", "#696969" },
            { "SlateBlue", "#6A5ACD" }, { "OliveDrab", "#6B8E23" }, { "SlateGray", "#708090" },
            { "LightSlateGray", "#778899" }, { "MediumSlateBlue", "#7B68EE" }, { "LawnGreen", "#7CFC00" },
            { "Chartreuse", "#7FFF00" }, { "Aquamarine", "#7FFFD4" }, { "Maroon", "#800000" }, { "Purple", "#800080" },
            { "Olive", "#808000" }, { "Gray", "#808080" }, { "SkyBlue", "#87CEEB" }, { "LightSkyBlue", "#87CEFA" },
            { "BlueViolet", "#8A2BE2" }, { "DarkRed", "#8B0000" }, { "DarkMagenta", "#8B008B" },
            { "SaddleBrown", "#8B4513" }, { "DarkSeaGreen", "#8FBC8F" }, { "LightGreen", "#90EE90" },
            { "MediumPurple", "#9370D8" }, { "DarkViolet", "#9400D3" }, { "PaleGreen", "#98FB98" },
            { "DarkOrchid", "#9932CC" }, { "YellowGreen", "#9ACD32" }, { "Sienna", "#A0522D" }, { "Brown", "#A52A2A" },
            { "DarkGray", "#A9A9A9" }, { "LightBlue", "#ADD8E6" }, { "GreenYellow", "#ADFF2F" },
            { "PaleTurquoise", "#AFEEEE" }, { "LightSteelBlue", "#B0C4DE" }, { "PowderBlue", "#B0E0E6" },
            { "FireBrick", "#B22222" }, { "DarkGoldenRod", "#B8860B" }, { "MediumOrchid", "#BA55D3" },
            { "RosyBrown", "#BC8F8F" }, { "DarkKhaki", "#BDB76B" }, { "Silver", "#C0C0C0" },
            { "MediumVioletRed", "#C71585" }, { "IndianRed", "#CD5C5C" }, { "Peru", "#CD853F" }, { "Chocolate", "#D2691E" },
            { "Tan", "#D2B48C" }, { "LightGray", "#D3D3D3" }, { "PaleVioletRed", "#D87093" }, { "Thistle", "#D8BFD8" },
            { "Orchid", "#DA70D6" }, { "GoldenRod", "#DAA520" }, { "Crimson", "#DC143C" }, { "Gainsboro", "#DCDCDC" },
            { "Plum", "#DDA0DD" }, { "BurlyWood", "#DEB887" }, { "LightCyan", "#E0FFFF" }, { "Lavender", "#E6E6FA" },
            { "DarkSalmon", "#E9967A" }, { "Violet", "#EE82EE" }, { "PaleGoldenRod", "#EEE8AA" },
            { "LightCoral", "#F08080" }, { "Khaki", "#F0E68C" }, { "AliceBlue", "#F0F8FF" }, { "HoneyDew", "#F0FFF0" },
            { "Azure", "#F0FFFF" }, { "SandyBrown", "#F4A460" }, { "Wheat", "#F5DEB3" }, { "Beige", "#F5F5DC" },
            { "WhiteSmoke", "#F5F5F5" }, { "MintCream", "#F5FFFA" }, { "GhostWhite", "#F8F8FF" }, { "Salmon", "#FA8072" },
            { "AntiqueWhite", "#FAEBD7" }, { "Linen", "#FAF0E6" }, { "LightGoldenRodYellow", "#FAFAD2" },
            { "OldLace", "#FDF5E6" }, { "Red", "#FF0000" }, { "Fuchsia", "#FF00FF" }, { "Magenta", "#FF00FF" },
            { "DeepPink", "#FF1493" }, { "OrangeRed", "#FF4500" }, { "Tomato", "#FF6347" }, { "HotPink", "#FF69B4" },
            { "Coral", "#FF7F50" }, { "Darkorange", "#FF8C00" }, { "LightSalmon", "#FFA07A" }, { "Orange", "#FFA500" },
            { "LightPink", "#FFB6C1" }, { "Pink", "#FFC0CB" }, { "Gold", "#FFD700" }, { "PeachPuff", "#FFDAB9" },
            { "NavajoWhite", "#FFDEAD" }, { "Moccasin", "#FFE4B5" }, { "Bisque", "#FFE4C4" }, { "MistyRose", "#FFE4E1" },
            { "BlanchedAlmond", "#FFEBCD" }, { "PapayaWhip", "#FFEFD5" }, { "LavenderBlush", "#FFF0F5" },
            { "SeaShell", "#FFF5EE" }, { "Cornsilk", "#FFF8DC" }, { "LemonChiffon", "#FFFACD" },
            { "FloralWhite", "#FFFAF0" }, { "Snow", "#FFFAFA" }, { "Yellow", "#FFFF00" }, { "LightYellow", "#FFFFE0" },
            { "Ivory", "#FFFFF0" }, { "White", "#FFFFFF" } };
    
    private static final Map<String, String> name2color = new HashMap<>(NAMED_COLORS.length);
    
    private static final Map<String, String> color2name = new HashMap<>(NAMED_COLORS.length);
    
    static {
        for (String[] nvp : NAMED_COLORS) {
            String name = nvp[0].toLowerCase();
            name2color.put(name, nvp[1]);
            color2name.put(nvp[1], nvp[0]);
            
            if (name.contains("gray")) {
                name2color.put(name.replace("gray", "grey"), nvp[1]);
            }
        }
    }
    
    /**
     * Returns the RGB equivalent of the named color.
     * 
     * @param name The name of the color (case insensitive).
     * @return The RGB string equivalent (e.g., #FFFAFA) or null if not found.
     */
    public static String getRGBFromName(String name) {
        return name == null ? null : name2color.get(name.toLowerCase());
    }
    
    /**
     * Returns the name equivalent of the RGB color.
     * 
     * @param value The RGB value (e.g., #FFFAFA) of the color (case insensitive).
     * @return The name equivalent or null if not found.
     */
    public static String getNameFromRGB(String value) {
        return value == null ? null : color2name.get(value.toUpperCase());
    }
    
    /**
     * Returns a Color object that corresponds to the value.
     * 
     * @param value The string representation of the color. This may be a color name or a text
     *            representation of the color's rgb value.
     * @return The Color object corresponding to the value, or null if not a recognized value.
     */
    public static Color toColor(String value) {
        return toColor(value, null);
    }
    
    /**
     * Returns a Color object that corresponds to the value.
     * 
     * @param value The string representation of the color. This may be a color name or a text
     *            representation of the color's rgb value.
     * @param dflt Default color if value cannot be converted to a color.
     * @return The Color object corresponding to the value, or null if not a recognized value.
     */
    public static Color toColor(String value, Color dflt) {
        String rgb = getRGBFromName(value);
        rgb = rgb == null ? value : rgb;
        
        try {
            return rgb == null || rgb.isEmpty() ? dflt : Color.decode(rgb);
        } catch (Exception e) {
            return dflt;
        }
    }
    
    /**
     * Converts a color to a web-friendly string format.
     * 
     * @param value The color value to convert.
     * @return The converted value.
     */
    public static String toString(Color value) {
        String rgb = "#" + Integer.toHexString(value.getRGB() | 0xFF000000).substring(2).toUpperCase();
        String name = getNameFromRGB(rgb);
        return name == null ? rgb : name;
    }
    
    /**
     * Enforce static class.
     */
    private ColorUtil() {
    }
}
