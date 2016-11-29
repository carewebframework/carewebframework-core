package org.carewebframework.ui.dialog;

import static org.carewebframework.ui.dialog.DialogConstants.LABEL_ID_CANCEL;
import static org.carewebframework.ui.dialog.DialogConstants.LABEL_ID_OK;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.carewebframework.common.StrUtil;

/**
 * Wrapper for response object used by dialog for displaying and returning responses.
 * 
 * @param <T> The type of response object
 */
public class DialogResponse<T> {
    
    private final T response;
    
    private final String label;
    
    private final boolean excluded;
    
    private final boolean ok;
    
    private final boolean cancel;
    
    private final boolean dflt;
    
    private final String flavor;
    
    /**
     * Returns list of response objects created from a string of vertical bar delimited captions.
     * 
     * @param responses Response list separated by vertical bars.
     * @param exclusions Exclusion list separated by vertical bars (may be null).
     * @param dflt Default response (may be null).
     * @return List of response objects corresponding to response list.
     */
    public static <T> List<DialogResponse<T>> toResponseList(T[] responses, T[] exclusions, T dflt) {
        List<DialogResponse<T>> list = new ArrayList<>();
        boolean forceDefault = dflt == null && responses.length == 1;
        
        for (T response : responses) {
            DialogResponse<T> rsp = new DialogResponse<T>(response,
                    exclusions != null && ArrayUtils.contains(exclusions, response), forceDefault || response.equals(dflt));
            list.add(rsp);
        }
        
        return list;
    }
    
    private DialogResponse(T response, boolean excluded, boolean dflt) {
        this.response = response;
        this.label = response.toString();
        this.ok = isResponseType(label, LABEL_ID_OK);
        this.cancel = isResponseType(label, LABEL_ID_CANCEL);
        this.excluded = excluded;
        this.dflt = dflt;
        this.flavor = ok ? "success" : cancel ? "danger" : dflt ? "primary" : "default";
    }
    
    /**
     * Returns the response object.
     * 
     * @return The response object.
     */
    public T getResponse() {
        return response;
    }
    
    /**
     * Returns the response label.
     * 
     * @return The response label.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Returns true if the response is never to be saved.
     * 
     * @return True to suppress saving of response.
     */
    public boolean isExcluded() {
        return excluded;
    }
    
    /**
     * True if this response corresponds to an "OK" response.
     * 
     * @return True if OK response.
     */
    public boolean isOk() {
        return ok;
    }
    
    /**
     * True if this response corresponds to a "Cancel" response.
     * 
     * @return True if cancel response.
     */
    public boolean isCancel() {
        return cancel;
    }
    
    /**
     * True if this is the default response.
     * 
     * @return True if default response.
     */
    public boolean isDefault() {
        return dflt;
    }
    
    /**
     * Returns the button flavor (CSS class).
     * 
     * @return The button flavor.
     */
    public String getFlavor() {
        return flavor;
    }
    
    /**
     * Returns true if the text corresponds to a specific response type.
     * 
     * @param text Text to test.
     * @return True if represents the specified response type.
     */
    private boolean isResponseType(String text, String label) {
        return label.equals(text) || StrUtil.formatMessage(label).equals(text);
    }
    
}
