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
package org.carewebframework.maven.plugin.theme;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 * Transforms an input color relative to the base color.
 */
class ColorTransform extends RGBImageFilter {
    
    private class HSBColor {
        
        private final float[] hsb;
        
        private HSBColor(Color color) {
            hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
        
        private HSBColor(int rgba) {
            this(new Color(rgba, true));
        }
        
        private float getHue() {
            return hsb[0];
        }
        
        private float getSaturation() {
            return hsb[1];
        }
        
        private float getBrightness() {
            return hsb[2];
        }
    }
    
    private final HSBColor baseColor;
    
    /**
     * Transforms colors relative to a base color.
     * 
     * @param baseColor The base color.
     */
    public ColorTransform(Color baseColor) {
        this.baseColor = new HSBColor(baseColor);
        canFilterIndexColorModel = true;
    }
    
    /**
     * Modify a pixel's hue, saturation, and brightness relative to the base color.
     */
    @Override
    public int filterRGB(int x, int y, int rgba) {
        HSBColor hsb = new HSBColor(rgba);
        float saturation = hsb.getSaturation() * baseColor.getSaturation();
        float brightness = hsb.getBrightness()
                * ((hsb.getSaturation() * baseColor.getBrightness()) + (1 - hsb.getSaturation()));
        return Color.HSBtoRGB(baseColor.getHue(), saturation, brightness) | (rgba & 0xff000000);
    }
    
}
