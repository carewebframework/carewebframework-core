Purpose:
-------

The idea of the InfoPanel component is to have a common place to display information and alerts, rather than each component implementing its own capability.
The InfoPanel component supports drag-and-drop as well as the ability to push content by firing an event or calling the component's API directly.


Layout:
------

The InfoPanel UI is divided into three sections:  

The menu bar can display custom menu items that may be added via an API.

The upper panel is for the display of general information and serves as the drop target for drag-and-drop
operations.  

The lower panel houses the alert manager and can be collapsed when not used.

Drag-and-Drop Support:
---------------------

The InfoPanel can serve as a drop target for draggable items that meet the following criteria:

1) The dragged item must specify the InfoPanel's drop id in its "draggable" property.  Note that this property can be set to "true" or "false"
to enable or disable draggability without regard to the target, or may be set to one or more drop id's to limit draggability to targets with the
same drop id.  The IInfoPanel.DROP_ID constant contains the proper drop id to set. 

2) The dragged item or one of its ancestors must have an associated drop renderer.  A drop renderer is any class that implements the
IDropRenderer interface.  This interface has three methods:
	a) renderDroppedItem - Takes the dropped item as an argument and returns the root component of the rendered version.  The drop renderer
	is completely responsible for rendering the dropped item.  The InfoPanel component takes the root component returned by this method
	and hosts it within the upper panel.
	b) getDisplayText - Takes the dropped item as an argument and returns text to be displayed in the container's title bar.
	c) isEnabled - Can be used to temporarily disable drag-and-drop.  Normally, it should just return true.
	
	To associate your drop renderer with the dragged item, use the static method DropUtil.setDropRenderer.  This method takes a component as the
	first argument and your drop renderer as the second.  The component may be the dragged item itself (e.g., a list item), or one of its
	ancestors (e.g., the parent list box).  In this way, when a component is dropped on the InfoPanel, the InfoPanel can call the appropriate
	drop renderer to create the UI for the rendered version.
	
Simulating a Drop:
-----------------

You can also simulate a drop programmatically in one of two ways:  by a direct API call (see section on accessing the InfoPanel API), 
or by firing an event:

1) Invoke the IInfoPanel.drop method, which takes the drop item as its argument.

2) Fire a drop request to the InfoPanel using the EventManager.  For example: 
		eventManager.fireLocalEvent(IInfoPanel.DROP_EVENT_NAME, item);
	where item is the item to be dropped.  Only active InfoPanels will respond to this event.

Both of the above techniques require that the drop item have an associated drop renderer.

Adding Custom Menu Items:
------------------------

The InfoPanel API has methods for adding and removing custom menu items, IInfoPanel.registerMenuItem and IInfoPanel.unregisterMenuItem, respectively.

Adding an Alert:
---------------

An alert may be added to the InfoPanel by direct API call or by firing an event:

1) Invoke the IInfoPanel.showAlert method, passing in the root component of your alert.

2) Fire an alert request to the InfoPanel using the EventManager.  For example:
		eventManager.fireLocalEvent(IInfoPanel.ALERT_EVENT_NAME, root);
	where root is the root level component of the alert.  Only active InfoPanels will respond to this event.
	
Accessing the InfoPanel API:
---------------------------

A reference to the IInfoPanel interface can be obtained by a call to the InfoPanelService.findInfoPanel static method.  There are two
overloaded forms of this method.  You will most likely use the one that takes the plugin's container as an argument.  This method will
search the framework's component tree for an active InfoPanel component and return a reference to its IInfoPanel interface if one is
found.  We restrict the search to active InfoPanels only in case there is more than one InfoPanel in the UI.  Note that if this call is
made in a plugin's onActivate method, the InfoPanel might not yet be activated.  In this case, it may be wise to defer the call by using
Events.echoEvent.  You should also handle the case where this method returns null, meaning that an active InfoPanel could not be found.

Examples:
--------

The InfoPanel project has an InfoPanelTestController that illustrates all of the above concepts.
