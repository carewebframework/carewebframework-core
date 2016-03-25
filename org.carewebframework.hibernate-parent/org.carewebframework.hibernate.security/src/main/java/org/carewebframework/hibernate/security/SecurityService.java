/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.security;

import org.carewebframework.security.spring.AbstractSecurityService;

/**
 * Hibernate-based security service.
 */
public class SecurityService extends AbstractSecurityService {
    
    
    private final UserDAO userDAO;
    
    public SecurityService(UserDAO userDAO) throws Exception {
        this.userDAO = userDAO;
    }
    
    @Override
    public boolean validatePassword(String password) {
        return password.equals(getAuthenticatedUser().getPassword());
    }
    
    @Override
    public String changePassword(String oldPassword, String newPassword) {
        if (!validatePassword(oldPassword)) {
            return "Invalid username or password.";
        }
        
        User user = (User) getAuthenticatedUser();
        user.setPassword(newPassword);
        try {
            userDAO.update(user);
            return null;
        } catch (Exception e) {
            user.setPassword(oldPassword);
            return e.getMessage();
        }
    }
    
}
