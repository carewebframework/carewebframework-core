/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.ColorUtil;
import org.carewebframework.ui.zk.ColorPicker.Color;

import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.Div;

/**
 * Presents a color picker component.
 */
public class ColorPicker extends AbstractPicker<Color> {
    
    private static final long serialVersionUID = 1L;
    
    public static final String[] DEFAULT_PALETTE = { "#F7E214", "#F4ED7C", "#F4ED47", "#F9E814", "#FCE016", "#C6AD0F",
            "#AD9B0C", "#82750F", "#F7E859", "#F9E526", "#F7DD16", "#F9D616", "#D8B511", "#AA930A", "#99840A", "#F9E55B",
            "#F9E24C", "#F9E04C", "#FCD116", "#C6A00C", "#AA8E0A", "#897719", "#F9E27F", "#F9E070", "#FCD856", "#FFC61E",
            "#E0AA0F", "#B58C0A", "#F7E8AA", "#F9E08C", "#FFCC49", "#FCB514", "#BF910C", "#A37F14", "#7C6316", "#F4E287",
            "#F4DB60", "#F2D13D", "#EAAF0F", "#C6930A", "#9E7C0A", "#705B0A", "#FFD87F", "#FCC963", "#FCBF49", "#FCA311",
            "#D88C02", "#AF7505", "#7A5B11", "#FFD691", "#FCCE87", "#FCBA5E", "#F99B0C", "#CC7A02", "#996007", "#6B4714",
            "#F2CE68", "#F2BF49", "#EFB22D", "#E28C05", "#C67F07", "#9E6B05", "#725E26", "#FFD69B", "#FCCC93", "#FCAD56",
            "#F77F00", "#DD7500", "#BC6D0A", "#995905", "#FFB777", "#FF993F", "#F47C00", "#EF6B00", "#B55400", "#8C4400",
            "#4C280F", "#F4DBAA", "#F2C68C", "#EDA04F", "#E87511", "#C66005", "#9E540A", "#633A11", "#F9BF9E", "#FCA577",
            "#FC8744", "#F96B07", "#D15B05", "#A04F11", "#843F0F", "#F9C6AA", "#FC9E70", "#FC7F3F", "#F96302", "#DD5900",
            "#BC4F07", "#6D3011", "#F9A58C", "#F98E6D", "#F97242", "#F95602", "#A53F0F", "#843511", "#F9BAAA", "#F98972",
            "#F9603A", "#F74902", "#D14414", "#933311", "#6D3321", "#F9AFAD", "#F9827F", "#F95E59", "#F93F26", "#E23D28",
            "#C13828", "#7C2D23", "#F99EA3", "#F9848E", "#FC4F59", "#EF2B2D", "#D62828", "#AF2626", "#7C211E", "#F9B2B7",
            "#FC6675", "#F43F4F", "#CC2D30", "#A03033", "#5B2D28", "#F9BFC1", "#FC8C99", "#FC5E72", "#E8112D", "#CE1126",
            "#AF1E2D", "#7C2128", "#FFA3B2", "#FC758E", "#F4476B", "#E5053A", "#BF0A30", "#992135", "#772D35", "#FCBFC9",
            "#FC9BB2", "#F4547C", "#E00747", "#C10538", "#A80C35", "#931638", "#F4C9C9", "#EF99A3", "#E5566D", "#D81C3F",
            "#C41E3A", "#A32638", "#8C2633", "#F2AFC1", "#ED7A9E", "#E54C7C", "#D30547", "#AF003D", "#8E2344", "#75263D",
            "#FFA0BF", "#FF77A8", "#F94F8E", "#EA0F6B", "#CC0256", "#A50544", "#7C1E3F", "#F4BFD1", "#ED72AA", "#E22882",
            "#D10056", "#AA004F", "#930042", "#70193D", "#F993C4", "#F46BAF", "#ED2893", "#D60270", "#AD005B", "#8C004C",
            "#6D213F", "#FFA0CC", "#FC70BA", "#F43FA5", "#ED0091", "#CE007C", "#AA0066", "#8E0554", "#F9AFD3", "#F484C4",
            "#ED4FAF", "#E0219E", "#C40F89", "#AD0075", "#7C1C51", "#F7C4D8", "#EA6BBF", "#DB28A5", "#C4008C", "#A8007A",
            "#9B0070", "#87005B", "#F2BAD8", "#EDA0D3", "#E87FC9", "#CC00A0", "#B7008E", "#A3057F", "#7F2860", "#EDC4DD",
            "#E29ED6", "#D36BC6", "#BF30B5", "#AF23A5", "#A02D96", "#772D6B", "#E5C4D6", "#D3A5C9", "#9B4F96", "#72166B",
            "#681E5B", "#5E2154", "#542344", "#D8A8D8", "#C687D1", "#AA47BA", "#930FA5", "#820C8E", "#701E72", "#602D59",
            "#D1A0CC", "#BA7CBC", "#9E4FA5", "#872B93", "#70147A", "#66116D", "#5B195E", "#BF93CC", "#AA72BF", "#8E47AD",
            "#66008C", "#5B027A", "#560C70", "#4C145E", "#E0CEE0", "#C6AADB", "#9663C4", "#6D28AA", "#59118E", "#4F2170",
            "#442359", "#C9ADD8", "#B591D1", "#9B6DC6", "#894FBF", "#6607A5", "#56008C", "#44235E", "#BAAFD3", "#9E91C6",
            "#8977BA", "#38197A", "#2B1166", "#260F54", "#2B2147", "#AD9ED3", "#937ACC", "#7251BC", "#4F0093", "#3F0077",
            "#35006D", "#2B0C56", "#D1CEDD", "#A5A0D6", "#6656BC", "#4930AD", "#3F2893", "#332875", "#2B265B", "#BFD1E5",
            "#A5BAE0", "#5E68C4", "#380096", "#1C146B", "#141654", "#14213D", "#AFBCDB", "#5B77CC", "#3044B5", "#2D008E",
            "#1E1C77", "#192168", "#112151", "#B5D1E8", "#99BADD", "#6689CC", "#0C1C8C", "#002B7F", "#002868", "#002654",
            "#9BC4E2", "#75AADB", "#3A75C4", "#0038A8", "#003893", "#00337F", "#002649", "#C4D8E2", "#A8CEE2", "#75B2DD",
            "#0051BA", "#003F87", "#00386B", "#002D47", "#93C6E0", "#60AFDD", "#008ED6", "#005BBF", "#0054A0", "#003D6B",
            "#00334C", "#82C6E2", "#51B5E0", "#00A3DD", "#0072C6", "#005B99", "#004F6D", "#003F54", "#BAE0E2", "#51BFE2",
            "#00A5DB", "#0084C9", "#00709E", "#00546B", "#004454", "#A5DDE2", "#70CEE2", "#00BCE2", "#0091C9", "#007AA5",
            "#00607C", "#003F49", "#72D1DD", "#28C4D8", "#00ADC6", "#0099B5", "#00829B", "#006B77", "#00494F", "#7FD6DB",
            "#2DC6D6", "#00B7C6", "#009BAA", "#00848E", "#006D75", "#00565B", "#C9E8DD", "#93DDDB", "#4CCED1", "#009EA0",
            "#008789", "#007272", "#006663", "#AADDD6", "#56C9C1", "#00B2AA", "#008C82", "#007770", "#006D66", "#005951",
            "#87DDD1", "#56D6C9", "#00C1B5", "#00AA9E", "#006056", "#00493F", "#8CE0D1", "#47D6C1", "#00C6B2", "#00B2A0",
            "#009987", "#008272", "#004F42", "#7AD3C1", "#35C4AF", "#00AF99", "#009B84", "#008270", "#006B5B", "#004438",
            "#BAEAD6", "#A0E5CE", "#5EDDC1", "#00AF93", "#00997C", "#007C66", "#006854", "#9BDBC1", "#7AD1B5", "#00B28C",
            "#009977", "#007A5E", "#006B54", "#00563F", "#8EE2BC", "#54D8A8", "#00C993", "#00B27A", "#007C59", "#006847",
            "#024930", "#B5E2BF", "#96D8AF", "#70CE9B", "#009E60", "#008751", "#006B3F", "#234F33", "#B5E8BF", "#99E5B2",
            "#84E2A8", "#00B760", "#009E49", "#007A3D", "#215B33", "#AADD96", "#A0DB8E", "#60C659", "#1EB53A", "#339E35",
            "#3D8E33", "#3A7728", "#D3E8A3", "#C4E58E", "#AADD6D", "#5BBF21", "#56AA1C", "#568E14", "#566B21", "#D8ED96",
            "#CEEA82", "#BAE860", "#8CD600", "#7FBA00", "#709302", "#566314", "#E0EA68", "#D6E542", "#CCE226", "#BAD80A",
            "#A3AF07", "#939905", "#707014", "#E8ED60", "#E0ED44", "#D6E80F", "#CEE007", "#BAC405", "#9E9E07", "#848205",
            "#F2EF87", "#EAED35", "#E5E811", "#E0E20C", "#C1BF0A", "#AFA80A", "#998E07", "#F2ED6D", "#EFEA07", "#EDE211",
            "#E8DD11", "#B5A80C", "#998C0A", "#6D6002", "#D1C6B5", "#C1B5A5", "#AFA593", "#998C7C", "#827566", "#6B5E4F",
            "#3D332B", "#CEC1B5", "#BAAA9E", "#A8998C", "#99897C", "#7C6D63", "#66594C", "#3D3028", "#C6C1B2", "#B5AFA0",
            "#A39E8C", "#8E8C7A", "#777263", "#605E4F", "#282821", "#D1CCBF", "#BFBAAF", "#AFAAA3", "#96938E", "#827F77",
            "#60605B", "#2B2B28", "#DDDBD1", "#D1CEC6", "#ADAFAA", "#919693", "#666D70", "#444F51", "#30383A", "#E0D1C6",
            "#D3BFB7", "#BCA59E", "#8C706B", "#593F3D", "#493533", "#3F302B", "#D1D1C6", "#BABFB7", "#A3A8A3", "#898E8C",
            "#565959", "#494C49", "#3F3F38", "#E5DBCC", "#DDD1C1", "#CCC1B2", "#B5A899", "#AFA393", "#A39687", "#96897A",
            "#8C7F70", "#827263", "#6D5E51", "#E8E2D6", "#DDD8CE", "#D3CEC4", "#C4C1BA", "#BAB7AF", "#B5B2AA", "#A5A39E",
            "#9B9993", "#8C8984", "#777772", "#686663", "#54472D", "#544726", "#60542B", "#ADA07A", "#C4B796", "#D6CCAF",
            "#E2D8BF", "#604C11", "#877530", "#A09151", "#BCAD75", "#CCBF8E", "#DBCEA5", "#E5DBBA", "#665614", "#998714",
            "#B59B0C", "#DDCC6B", "#E2D67C", "#EADD96", "#EDE5AD", "#5B4723", "#755426", "#876028", "#C1A875", "#D1BF91",
            "#DDCCA5", "#E2D6B5", "#472311", "#8C5933", "#B28260", "#C49977", "#D8B596", "#E5C6AA", "#EDD3BC", "#603311",
            "#51261C", "#7C513D", "#99705B", "#B5917C", "#CCAF9B", "#D8BFAA", "#E2CCBA", "#593D2B", "#633826", "#7A3F28",
            "#AF8970", "#D3B7A3", "#E0CCBA", "#E5D3C1", "#6B3021", "#9B301C", "#D81E05", "#ED9E84", "#EFB5A0", "#F2C4AF",
            "#F2D1BF", "#5B2626", "#752828", "#913338", "#DB828C", "#F2ADB2", "#F4BCBF", "#F7C9C6", "#512826", "#6D332B",
            "#7A382D", "#CE898C", "#EAB2B2", "#F2C6C4", "#F4D1CC", "#441E1C", "#844949", "#A56B6D", "#BC8787", "#D8ADA8",
            "#E2BCB7", "#EDCEC6", "#511E26", "#661E2B", "#7A2638", "#D8899B", "#E8A5AF", "#F2BABF", "#F4C6C9", "#602144",
            "#84216B", "#9E2387", "#D884BC", "#E8A3C9", "#F2BAD3", "#F4CCD8", "#4F213A", "#754760", "#936B7F", "#AD8799",
            "#CCAFB7", "#E0C9CC", "#E8D6D1", "#512D44", "#63305E", "#703572", "#B58CB2", "#C6A3C1", "#D3B7CC", "#E2CCD3",
            "#472835", "#593344", "#8E6877", "#B5939B", "#CCADAF", "#DDC6C4", "#E5D3CC", "#512654", "#68217A", "#7A1E99",
            "#AF72C1", "#CEA3D3", "#D6AFD6", "#E5C6DB", "#35264F", "#493D63", "#605677", "#8C8299", "#B2A8B5", "#CCC1C6",
            "#DBD3D3", "#353842", "#353F5B", "#3A4972", "#9BA3B7", "#ADB2C1", "#C4C6CE", "#D6D3D6", "#003049", "#00335B",
            "#003F77", "#6693BC", "#93B7D1", "#B7CCDB", "#C4D3DD", "#02283A", "#3F6075", "#607C8C", "#8499A5", "#AFBCBF",
            "#C4CCCC", "#D6D8D3", "#0C3844", "#004459", "#5E99AA", "#87AFBF", "#A3C1C9", "#C4D6D6", "#00353A", "#26686D",
            "#609191", "#8CAFAD", "#AAC4BF", "#CED8D1", "#D6DDD6", "#193833", "#3A564F", "#667C72", "#91A399", "#AFBAB2",
            "#C9CEC4", "#CED1C6", "#234435", "#195E47", "#076D54", "#7AA891", "#A3C1AD", "#B7CEBC", "#C6D6C4", "#213D30",
            "#4F6D5E", "#779182", "#96AA99", "#AFBFAD", "#C4CEBF", "#D8DBCC", "#2B4C3F", "#266659", "#1E7A6D", "#7FBCAA",
            "#A0CEBC", "#BCDBCC", "#D1E2D3", "#233A2D", "#546856", "#728470", "#9EAA99", "#BCC1B2", "#C6CCBA", "#D6D6C6",
            "#05705E", "#008772", "#7FC6B2", "#AADBC6", "#BCE2CE", "#CCE5D6", "#495928", "#547730", "#608E3A", "#B5CC8E",
            "#C6D6A0", "#C9D6A3", "#D8DDB5", "#3F4926", "#5E663A", "#777C4F", "#9B9E72", "#B5B58E", "#C6C6A5", "#D8D6B7",
            "#424716", "#6B702B", "#8C914F", "#AAAD75", "#C6C699", "#D3D1AA", "#E0DDBC", "#605E11", "#878905", "#AABA0A",
            "#DBE06B", "#E2E584", "#E8E89B", "#494411", "#75702B", "#9E9959", "#B2AA70", "#CCC693", "#D6CEA3", "#E0DBB5",
            "#F4EDAF", "#F2ED9E", "#F2EA87", "#EDE85B", "#E8DD21", "#DDCE11", "#D3BF11", "#F2EABC", "#EFE8AD", "#EAE596",
            "#E2DB72", "#D6CE49", "#C4BA00", "#AFA00C", "#EAE2B7", "#E2DBAA", "#DDD69B", "#CCC47C", "#B5AA59", "#968C28",
            "#847711", "#D8DDCE", "#C1D1BF", "#A5BFAA", "#7FA08C", "#5B8772", "#21543F", "#0C3026", "#CCE2DD", "#B2D8D8",
            "#8CCCD3", "#54B7C6", "#00A0BA", "#007F99", "#00667F", "#BAE0E0", "#99D6DD", "#6BC9DB", "#00B5D6", "#00A0C4",
            "#008CB2", "#D1D8D8", "#C6D1D6", "#9BAFC4", "#7796B2", "#5E82A3", "#26547C", "#00305E", "#D6D6D8", "#BFC6D1",
            "#9BAABF", "#6D87A8", "#335687", "#0F2B5B", "#0C1C47", "#D6DBE0", "#C1C9DD", "#A5AFD6", "#7F8CBF", "#5960A8",
            "#2D338E", "#0C1975", "#E2D3D6", "#D8CCD1", "#C6B5C4", "#A893AD", "#7F6689", "#664975", "#472B59", "#F2D6D8",
            "#EFC6D3", "#EAAAC4", "#E08CB2", "#D36B9E", "#BC3877", "#A00054", "#EDD6D6", "#EACCCE", "#E5BFC6", "#D39EAF",
            "#B7728E", "#A05175", "#7F284F", "#EFCCCE", "#EABFC4", "#E0AABA", "#C9899E", "#B26684", "#934266", "#702342",
            "#EFD1C9", "#E8BFBA", "#DBA8A5", "#C98C8C", "#B26B70", "#8E4749", "#7F383A", "#F7D1CC", "#F7BFBF", "#F2A5AA",
            "#E8878E", "#D6606D", "#B73844", "#9E2828", "#F9DDD6", "#FCC9C6", "#FCADAF", "#F98E99", "#F26877", "#E04251",
            "#D12D33", "#FFD3AA", "#F9C9A3", "#F9BA82", "#FC9E49", "#F28411", "#D36D00", "#BF5B00", "#F4D1AF", "#EFC49E",
            "#E8B282", "#D18E54", "#BA7530", "#8E4905", "#753802", "#EDD3B5", "#E2BF9B", "#D3A87C", "#C18E60", "#AA753F",
            "#723F0A", "#60330A", "#FCE216", "#F7B50C", "#E29100", "#EA4F00", "#E03A00", "#D62100", "#D11600", "#CC0C00",
            "#C6003D", "#D10572", "#C4057C", "#AA0096", "#720082", "#59008E", "#1C007A", "#0077BF", "#007FCC", "#00A3D1",
            "#007F82", "#008977", "#009677", "#009944", "#009E0F", "#54BC00", "#9EC400", "#A34402", "#704214", "#0A0C11",
            "#3A3321", "#282D26", "#3D3023", "#422D2D", "#1C2630", "#443D38", "#111111", "#111114", "#0F0F0F", "#110C0F",
            "#070C0F", "#33302B", "#00AACC", "#60DD49", "#FFED38", "#FF9338", "#F95951", "#FF0093", "#D6009E", "#0089AF",
            "#1CCE28", "#FFD816", "#FF7F1E", "#F93A2B", "#F7027C", "#BF008C", "#00B59B", "#DDE00F", "#FFCC1E", "#FF7247",
            "#FC2366", "#E50099", "#8C60C1", "#00A087", "#D6D60C", "#FFBC21", "#FF5416", "#FC074F", "#D10084", "#703FAF" };
    
    public static class Color extends Div {
        
        private static final long serialVersionUID = 1L;
        
        protected final String name;
        
        protected final String value;
        
        public Color(String value, String name) {
            super();
            this.value = value == null || value.isEmpty() ? value : value.startsWith("#") ? value : ColorUtil
                    .getRGBFromName(name);
            
            if (name == null) {
                name = ColorUtil.getNameFromRGB(this.value);
                
                if (name == null) {
                    name = value;
                }
            }
            
            this.name = name;
            validateValue();
            setTooltiptext(StringUtils.isEmpty(name) ? value : name);
            
            if (StringUtils.isEmpty(value)) {
                setSclass("cwf-colorpicker-colorcell cwf-colorpicker-colorcell-nocolor");
            } else {
                setSclass("cwf-colorpicker-colorcell");
                setStyle("background-color: " + value);
            }
            
        }
        
        private void validateValue() {
            if (value.length() > 0 && !value.matches("#[A-F0-9]{6}")) {
                throw new IllegalArgumentException("Illegal color value: " + value);
            }
        }
        
        @Override
        public boolean equals(Object object) {
            return object instanceof Color && ((Color) object).value.equals(value);
        }
    };
    
    private boolean showText;
    
    /**
     * Creates the color picker instance and all required child components.
     */
    public ColorPicker() {
        super("cwf-colorpicker", new Color("", ""));
        reset();
    }
    
    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        render(renderer, "showtext", showText);
        render(renderer, "selcolor", getSelectedValue());
    }
    
    /**
     * Resets the palette to default values.
     */
    public void reset() {
        clear();
        addColors(DEFAULT_PALETTE);
        
    }
    
    /**
     * Adds an array of colors to the palette.
     * 
     * @param colors An array of color name/RGB value pairs.
     */
    public void addColors(String[][] colors) {
        for (String[] color : colors) {
            _addItem(new Color(color[1], color[0]));
        }
        
        render();
    }
    
    /**
     * Adds an array of colors to the palette.
     * 
     * @param colors An array of color RGB values.
     */
    public void addColors(String[] colors) {
        for (String color : colors) {
            _addItem(new Color(color, null));
        }
        
        render();
    }
    
    /**
     * Add the specified color to the palette.
     * 
     * @param value Color RGB value.
     */
    public void addColor(String value) {
        addColor(value, "");
    }
    
    /**
     * Add the specified color and color name to the palette.
     * 
     * @param value Color RGB value.
     * @param name Color name
     */
    public void addColor(String value, String name) {
        findItem(new Color(value, name), true);
    }
    
    /**
     * Set the selected color to the specified value.
     * 
     * @param value RGB value of color to select.
     */
    public void setSelectedColor(String value) {
        setSelectedItem(new Color(value, value));
    }
    
    @Override
    protected void doSelectItem(Color color, boolean fireEvent) {
        super.doSelectItem(color, fireEvent);
        smartUpdate("selcolor", color == null ? null : color.value);
    }
    
    @Override
    protected String getItemText(Color color) {
        return showText ? color.name : "";
    }
    
    public String getSelectedValue() {
        Color color = getSelectedItem();
        return color == null ? null : color.value;
    }
    
    /**
     * Set the show text value. If true, the color name (or value if there is no display name) is
     * displayed in the text box and the drop down button background is set to the color. If false,
     * no text is displayed and the text box background is set to the color.
     * 
     * @param showText The show text flag.
     */
    public void setShowText(boolean showText) {
        if (this.showText != showText) {
            this.showText = showText;
            smartUpdate("showtext", showText);
        }
    }
    
    /**
     * Returns the status of the ShowText property.
     * 
     * @return The show text flag.
     */
    public boolean getShowText() {
        return showText;
    }
    
}
