/**
 * <p>
 * Provides the necessary classes to handle events.
 * </p>
 * <p>
 * For Example:
 * </p>
 * <code>
 * <pre>
 *  import org.zkoss.zk.ui.event.EventListener;
 *  
 *  public class DeactivateButtonListener implements EventListener {
 * 
 *      //@Override annotation commented out only for formatting
 *      public void onEvent(Event forwardEvent) throws Exception {
 *          Event event = ZKUtil.getEventOrigin(forwardEvent);
 *      }
 *  }
 *  </pre>
 *  </code>
 */
package ${package}.event;