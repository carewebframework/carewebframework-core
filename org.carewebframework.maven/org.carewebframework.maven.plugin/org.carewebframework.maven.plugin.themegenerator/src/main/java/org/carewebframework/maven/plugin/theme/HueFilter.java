/* 
   This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.carewebframework.maven.plugin.theme;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 * @author JoseLuis Casas
 */
class HueFilter extends RGBImageFilter {
    
    /*
     * A private variable used to hold hue/saturation/brightness values returned
     * from the static conversion methods in Color.
     */
    private final float hsbvals[] = new float[3];
    
    /**
     * the Hue of the indicated new foreground color.
     */
    float fgHue;
    
    /**
     * the Saturation of the indicated new foreground color.
     */
    float fgSaturation;
    
    /**
     * the Brightness of the indicated new foreground color.
     */
    float fgBrightness;
    
    /**
     * Construct a HueFilter object which performs color modifications to warp existing image colors
     * to have a new primary hue.
     * 
     * @param fg - Color
     */
    public HueFilter(final Color fg) {
        Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), this.hsbvals);
        this.fgHue = this.hsbvals[0];
        this.fgSaturation = this.hsbvals[1];
        this.fgBrightness = this.hsbvals[2];
        this.canFilterIndexColorModel = true;
    }
    
    /**
     * Filter an individual pixel in the image by modifying its hue, saturation, and brightness
     * components to be similar to the indicated new foreground color.
     */
    @Override
    public int filterRGB(final int x, final int y, int rgb) {
        final int alpha = (rgb >> 24) & 0xff;
        final int red = (rgb >> 16) & 0xff;
        final int green = (rgb >> 8) & 0xff;
        final int blue = (rgb) & 0xff;
        Color.RGBtoHSB(red, green, blue, this.hsbvals);
        final float newHue = this.fgHue;
        final float newSaturation = this.hsbvals[1] * this.fgSaturation;
        final float newBrightness = this.hsbvals[2] * ((this.hsbvals[1] * this.fgBrightness) + (1 - this.hsbvals[1]));
        rgb = Color.HSBtoRGB(newHue, newSaturation, newBrightness);
        return (rgb & 0x00ffffff) | (alpha << 24);
    }
    
}
