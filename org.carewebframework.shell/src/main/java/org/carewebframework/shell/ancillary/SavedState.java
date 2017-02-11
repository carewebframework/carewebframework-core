package org.carewebframework.shell.ancillary;

import org.carewebframework.shell.elements.UIElementBase;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Popup;

/**
 * Saves various states of a component prior to configuring it for design mode. The restore method
 * then restores to the saved state.
 */
public class SavedState {
    
    private static final String SAVED_STATE = UIElementBase.class.getName();
    
    final BaseUIComponent component;
    
    final String hint;
    
    final Popup contextMenu;
    
    public SavedState(BaseUIComponent component) {
        this.component = component;
        hint = component.getHint();
        contextMenu = component.getContext();
        component.setAttribute(SAVED_STATE, this);
        component.addClass("cwf-designmode-active");
    }
    
    private void restore() {
        component.setHint(hint);
        component.setContext(contextMenu);
        component.removeAttribute(SAVED_STATE);
        component.removeClass("cwf-designmode-active");
    }
    
    public static void restore(BaseUIComponent comp) {
        SavedState ss = (SavedState) comp.getAttribute(SAVED_STATE);
        
        if (ss != null) {
            ss.restore();
        }
    }
}
