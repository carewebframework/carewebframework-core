/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.messaging;

import org.carewebframework.api.messaging.Recipient.RecipientType;

/**
 * Various static utility methods.
 */
public class MessageUtil {
    
    /**
     * Returns true if the message should be excluded based on the given recipient. A message is
     * considered excluded if it has any constraint on the recipient's type and does not have a
     * matching recipient for that type.
     * 
     * @param message The message to examine.
     * @param recipient The recipient.
     */
    public static boolean isMessageExcluded(Message message, Recipient recipient) {
        return isMessageExcluded(message, recipient.getType(), recipient.getValue());
    }
    
    /**
     * Returns true if the message should be excluded based on the given recipient values. A message
     * is considered excluded if it has any constraint on the recipient type and does not have a
     * matching recipient for that type.
     * 
     * @param message The message to examine.
     * @param recipientType The type of recipient.
     * @param recipientValue The recipient's value.
     */
    public static boolean isMessageExcluded(Message message, RecipientType recipientType, String recipientValue) {
        Recipient[] recipients = (Recipient[]) message.getMetadata("cwf.pub.recipients");
        
        if (recipients == null || recipients.length == 0) {
            return false;
        }
        
        boolean excluded = false;
        
        for (Recipient recipient : recipients) {
            if (recipient.getType() == recipientType) {
                excluded = true;
                
                if (recipient.getValue().equals(recipientValue)) {
                    return false;
                }
            }
        }
        
        return excluded;
    }
    
    private MessageUtil() {
    }
}
