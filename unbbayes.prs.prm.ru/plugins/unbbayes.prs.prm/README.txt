INSTALLING A UNBBAYES' PLUGIN
------------------------------

This application is a plugin for UnBBayes 
(http://sourceforge.net/projects/unbbayes/). 
By extracting the distributed ZIP file, a folder with
the following name-pattern will be created:

"<PLUGIN_NAME>-<VERSION_NUMBER>"

You need only to copy the extracted folder into UnBBayes' "plugins" folder.
Most UnBBayes' distribution have the following folder structure:

UnBBayes
	|- examples
	|- lib
	|- plugins <- [Here is where you put the extracted folder]


USING A UNBBAYES' PLUGIN
------------------------------

Every plugin will become available when you start UnBBayes
or when you click "Plugins > Reload Plugins" menu.
Check the graphical user interface for new icons or menu items.

For inference, select a cell in the skeleton view and press the "compile" button.

ATTENTION
------------------------------
Some plugins use other plugins. In such case, you must install all dependencies too.

This plugin particularly does not have dependencies to other plugins.

THIS PLUGIN IS IN ALPHA STAGE. IT USES SQL SCRIPTS INSTEAD OF DATABASE, IT PERFORMS NON-LIFTED INFERENCE, AND INFERENCE RESULTS IN A BAYESIAN NETWORK CONTAINING ALL NODES CONNECTED TO THE QUERY NODE, INCLUDING D-SEPARATED AND BARREN NODES.
	
AVALILABILITY
------------------------------
Check the project page (http://sourceforge.net/projects/unbbayes/)
for updates or other plugins.