package org.carewebframework.shell.test;

import org.carewebframework.ui.test.MockUITest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LayoutParserTest.class, PluginDefinitionParserTest.class })
public class AllTests extends MockUITest {

}
