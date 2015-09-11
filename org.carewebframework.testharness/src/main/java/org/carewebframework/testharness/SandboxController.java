/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.testharness;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Plugin to facilitate testing of zul layouts.
 */
public class SandboxController extends PluginController implements ApplicationContextAware {
    
    private static final long serialVersionUID = 1L;
    
    private static final String[] REPLACE_MODES = { "modal", "highlighted", "popup" };
    
    private static ComboitemRenderer<Resource> zulRenderer = new ComboitemRenderer<Resource>() {
        
        @Override
        public void render(Comboitem item, Resource resource, int index) throws Exception {
            item.setValue(resource);
            item.setLabel(resource.getFilename());
            item.setTooltiptext(getPath(resource));
        }
        
        private String getPath(Resource resource) throws IOException {
            String[] pcs = resource.getURL().toString().split("!", 2);
            
            if (pcs.length == 1) {
                return pcs[0];
            }
            
            int i = pcs[0].lastIndexOf('/') + 1;
            return pcs[0].substring(i) + ":\n\n" + pcs[1];
        }
        
    };
    
    // Start of auto-wired section
    
    private Textbox txtContent;
    
    private Combobox cboZul;
    
    private Component contentParent;
    
    // End of auto-wired section
    
    private Component contentBase;
    
    private String content;
    
    private final ListModelList<Resource> model = new ListModelList<>();
    
    /**
     * Find the content base component. We can't assign it an id because of potential id collisions.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        cboZul.setItemRenderer(zulRenderer);
        cboZul.setModel(model);
        cboZul.setVisible(model.size() > 0);
        contentBase = ZKUtil.findChild(contentParent, Idspace.class);
    }
    
    /**
     * Refreshes the view based on the current contents.
     */
    @Override
    public void refresh() {
        super.refresh();
        ZKUtil.detachChildren(contentBase);
        
        if (content != null && !content.isEmpty()) {
            try {
                Events.echoEvent("onModeCheck", this.root, null);
                Executions.createComponentsDirectly(content, null, contentBase, null);
            } catch (Exception e) {
                ZKUtil.detachChildren(contentBase);
                Label label = new Label(ExceptionUtils.getStackTrace(e));
                label.setMultiline(true);
                contentBase.appendChild(label);
            }
        }
    }
    
    /**
     * Check for unsupported window modes. This is done asynchronously to allow modal windows to
     * also be checked.
     */
    public void onModeCheck() {
        modeCheck(contentBase);
    }
    
    /**
     * Check for any window components with mode settings that need to be changed.
     * 
     * @param comp Current component in search.
     */
    private void modeCheck(Component comp) {
        if (comp instanceof Window) {
            Window win = (Window) comp;
            
            if (win.isVisible() && ArrayUtils.contains(REPLACE_MODES, win.getMode())) {
                win.setMode("overlapped");
            }
        }
        
        for (Component child : comp.getChildren()) {
            modeCheck(child);
        }
    }
    
    /**
     * Renders the updated zul content in the view pane.
     */
    public void onClick$btnRenderContent() {
        content = txtContent.getText();
        refresh();
    }
    
    /**
     * Clears combo box selection when content is cleared.
     */
    public void onClick$btnClearContent() {
        txtContent.setText(null);
        cboZul.setSelectedItem(null);
        cboZul.setTooltiptext(null);
    }
    
    /**
     * Clears the view pane.
     */
    public void onClick$btnClearView() {
        ZKUtil.detachChildren(contentBase);
    }
    
    /**
     * Re-renders content in the view pane.
     */
    public void onClick$btnRefreshView() {
        refresh();
    }
    
    /**
     * Load contents of newly selected zul document.
     * 
     * @throws IOException Exception on reading zul document.
     */
    public void onSelect$cboZul() throws IOException {
        Comboitem item = cboZul.getSelectedItem();
        cboZul.setTooltiptext(null);
        Resource resource = item == null ? null : (Resource) item.getValue();
        
        if (resource != null) {
            try (InputStream is = resource.getInputStream()) {
                content = IOUtils.toString(is);
                cboZul.setTooltiptext(item.getTooltiptext());
                txtContent.setText(content);
                txtContent.setFocus(true);
                execution.addAuResponse(new AuInvoke(txtContent, "resync"));
            }
        }
    }
    
    /**
     * Set text box focus upon activation.
     */
    @Override
    public void onActivate() {
        super.onActivate();
        txtContent.focus();
    }
    
    /**
     * Populate combo box model with all zul documents on class path.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        try {
            for (Resource resource : applicationContext.getResources("classpath*:**/*.zul")) {
                model.add(resource);
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
