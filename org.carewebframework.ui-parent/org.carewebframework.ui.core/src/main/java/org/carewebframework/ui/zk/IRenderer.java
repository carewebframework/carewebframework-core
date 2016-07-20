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
package org.carewebframework.ui.zk;

import org.zkoss.zk.ui.Component;

/**
 * This is a base interface for Renderer classes that render ZK Components but that are not covered
 * by existing ZK Renderer interfaces. Look at the render method for details on how to implement
 * this interface.
 * 
 * @param <RENDERED_COMPONENT_TYPE> Component
 * @param <PARENT_COMPONENT_TYPE> Component
 * @param <DATA_TYPE> data type
 */
public interface IRenderer<RENDERED_COMPONENT_TYPE extends Component, PARENT_COMPONENT_TYPE extends Component, DATA_TYPE> {
    
    /**
     * This method receives an optional parent or base component, data which represents the model,
     * and supporting data, perhaps a list or a map or other data used for rendering. The result of
     * this method is a returned component which is the top level component this object is
     * rendering. Sample Use Cases: Render new component without a parent: Row render (Component
     * parent, Problem problem, Object... supportData) { } Render populated form fields from an
     * existing component: Component render(ProblemEditForm existingFormComponent, Problem problem ,
     * Object... supportData) { } Render new custom Form object that takes parent Component object
     * Component render(Component parent, Problem problem, Object... supportData) { }
     * 
     * @param optionalParentorExistingComponent This is either ignored, a parent component, or an
     *            existing component that you are initializing such as a form
     * @param data This is the data object that represents your primary model
     * @param supportData This is a new field that is a variable arg parameter for passing
     *            additional supplemental data which allows the renderers to stay stateless.
     * @return RENDERED_COMPONENT_TYPE
     */
    RENDERED_COMPONENT_TYPE render(PARENT_COMPONENT_TYPE optionalParentorExistingComponent, DATA_TYPE data,
                                   Object... supportData);
}
