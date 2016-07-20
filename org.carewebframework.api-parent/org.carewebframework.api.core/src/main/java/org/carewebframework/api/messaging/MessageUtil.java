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
     * @return True if the message should be excluded.
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
     * @return True if the message should be excluded.
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
