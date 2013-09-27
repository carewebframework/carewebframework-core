/**
 * This is a jquery extension that can apply a watermark to an input box. If the
 * browser natively supports watermarks (via the HTML5 placeholder attribute),
 * this capability will be used. In the absence of native support, a watermark
 * is displayed by creating an image containing the desired text and setting
 * that as the background image for the control, toggling the visibility of the
 * image depending on the presence of user input or, optionally, whether or not
 * the control has focus. It requires the txt2img ZK extendlet to correctly
 * process the server request to create the background image. Example usage:
 * 
 * jq(this.$f('txt')).watermark('Watermark Text','pink','Arial-bold-20',true);
 * 
 * Any of the parameters may be null. A null for the text parameter results in
 * removal of any existing watermark.
 */
(function(jq) {
	jq.fn.watermark = function(text, color, font, hideOnFocus) {
		return this.each(function() {
			var input = jq(this);
			var tag = this.nodeName.toLowerCase();
			
			if ("input" != tag && "textarea" != tag) {
				input.find("input,textarea").watermark(text, color, font, hideOnFocus);
				return;
			}
			
			var native = !jq.browser.msie && !hideOnFocus && ("placeholder" in input.get(0));

			function hideWatermark() {
				input.data("cwf_wm_visible", false);
				input.css("background", input.data("cwf_bg_default"));
			}

			function showWatermark() {
				input.data("cwf_wm_visible", true);
				input.css("background", input.data("cwf_bg_watermark"));
			}

			function updateWatermark() {
				var visible = input.val().length == 0;

				if (visible != input.data("cwf_wm_visible"))
					if (visible)
						showWatermark();
					else
						hideWatermark();
			}

			function doBind(evt, fnc) {
				var attr = "cwf_evt_" + evt;

				if (fnc) {
					doBind(evt, null);
					input.bind(evt, fnc);
					input.data(attr, fnc);
				} else {
					fnc = input.data(attr);

					if (fnc) {
						input.unbind(evt, fnc);
						input.removeData(attr);
					}
				}
			}
			
			function strToFont(str) {
				var pcs = str.split("-", 3);
				var result = "font-family: '" + pcs[0] + "';";
				
				if (pcs[1])
					result += "font-weight: " + pcs[1] + ";";
				
				if (pcs[2])
					result += "font-size: " + pcs[2] + "px;";
				
				return result;
			}

			if (input.data("cwf_bg_watermark")) {
				hideWatermark();
				input.removeData("cwf_bg_watermark");
				input.removeData("cwf_bg_default");
				input.removeData("cwf_wm_visible");
				doBind("focus");
				doBind("blur");
				doBind("keyup");
				doBind("select");
				doBind("paste change input wmupdate");
			}

			if (input.data("cwf_wm_style")) {
				input.removeAttr("placeholder");
				var ele = input.data("cwf_wm_style");
				ele.remove();
				input.removeData("cwf_wm_style");
			}
			
			if (!text)
				return;

			if (native) {
				input.attr("placeholder", text);
				var style = "color: " + (color ? color : "gray") + ";";
				
				if (font)
					style += strToFont(font);
				
				if (style) {
					var id = input.attr("id");
					var ele = jq("<style />"); 
					input.data("cwf_wm_style", ele);
					
					if (jq.browser.webkit)
						ele.append("#" + id + "::-webkit-input-placeholder{" + style + "}\n");
					
					if (jq.browser.mozilla)
						ele.append("#" + id + ":-moz-placeholder{" + style + "}\n");
					
					jq("head").append(ele);
				}
				return;
			}
			
			var bgColor = input.css("background-color");
			var bg = "url('zkau/web/.txt2img?text=" + text;

			if (color)
				bg += "&fgcolor=" + color;

			if (font)
				bg += "&font=" + font;

			bg += "') no-repeat 2px";
			
			if ("textarea" == tag)
				bg += " 2px";
			
			if (bgColor)
				bg += " " + bgColor;
			
			input.data("cwf_bg_watermark", bg);
			input.data("cwf_wm_visible", false);
			bg = "";

			$.each([ "color", "image", "repeat", "position" ], function(index,
					value) {
				var v = input.css("background-" + value);

				if (v)
					bg += v + " ";
			});

			input.data("cwf_bg_default", bg);

			if (hideOnFocus) {
				doBind("focus", hideWatermark);
				doBind("blur", updateWatermark);
			} else {
				doBind("keyup", updateWatermark);
				doBind("select", updateWatermark);
			}
			
			doBind("paste change input wmupdate", updateWatermark);
			updateWatermark();
		});
	};

})(jQuery);

/**
 * Allows invoking watermark from server response.
 */
zAu.cmd0.cwf_addWatermark = function(uuid, watermark, color, font, hideOnFocus) {
	jq('#' + uuid).watermark(watermark, color, font, hideOnFocus);
};

/**
 * Hook the setValue functions on input widgets to fire an event to update
 * the watermark visibility.
 */
zk.afterLoad('zul.inp', function() {
	function setValue() {
		this.$setValue.apply(this, arguments);
		var input = jq(this.getInputNode());
		
		if (input && input.data("cwf_bg_watermark"))
			input.trigger("wmupdate");
	}
	
	zk.override(zul.inp.Textbox.prototype, "setValue", setValue);
	zk.override(zul.inp.Combobox.prototype, "setValue", setValue);
});

