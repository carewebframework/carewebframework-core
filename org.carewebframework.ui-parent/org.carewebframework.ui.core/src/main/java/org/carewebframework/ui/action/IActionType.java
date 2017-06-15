package org.carewebframework.ui.action;

public interface IActionType<T> {

    boolean matches(String script);

    T parse(String script);

    void execute(T script);

    String getName();
}
