package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Point;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mxgraph.examples.swing.SCXMLGraphEditor;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLEdge;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLImportExport;
import com.mxgraph.examples.swing.editor.fileimportexport.SCXMLNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.AddAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.AddCornerToEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.DoLayoutAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditDatamodelAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditEdgeOrderAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditNamespaceAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.EditNodeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.RemoveCornerToEdgeAction;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsCluster;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsFinal;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsInitial;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsOutsourced;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.SetNodeAsParallel;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.ToggleDisplayOutsourcedContentInNode;
import com.mxgraph.examples.swing.editor.scxml.SCXMLEditorActions.ToggleWithTargetAction;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxCellState;

public class SCXMLEditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public SCXMLEditorPopupMenu(SCXMLGraphEditor editor,Point mousePt, Point graphPt, Point screenCoord)
	{
		SCXMLGraphComponent gc = editor.getGraphComponent();
		mxCell c=(mxCell) gc.getCellAt(graphPt.x, graphPt.y);
		SCXMLGraph graph = gc.getGraph();
		mxIGraphModel model = graph.getModel();
		
		boolean inOutsourcedNode=false;
		// if the cell is not editable set it back to null so the menu doesn't allow editing.
		if ((c!=null) && !graph.isCellEditable(c)) inOutsourcedNode=true;
		
		Point unscaledGraphPoint=gc.unscaledGraphCoordinates(graphPt);

		// edit node/transition (change text accordingly to type of element under cursor)
		if (c!=null) {
			if (c.isEdge()) {
				if (!inOutsourcedNode) {
					// for an edge, find out if the pointer is on a control point, or not.
					// if on a control point find out if it's the beginning or end of the endge.
					// -add control point if not on a control point
					// -remove control point if on one that is neither the beginning nor the end.
					add(editor.bind(mxResources.get("editEdge"), new EditEdgeAction(c,screenCoord)));
					addSeparator();
					// if the edge is not a loop you can add/remove corners
					if (c.getSource()!=c.getTarget()) {
						mxCellState cellState=graph.getView().getState(c);
						int index;
						int lastIndex=cellState.getAbsolutePointCount()-1;
						if ((index=cellState.getIndexOfEdgePointAt(graphPt.x, graphPt.y,gc.getTolerance()))==-1) {
							int indexOfNewPoint=cellState.getIndexOfNewPoint(graphPt.x, graphPt.y,gc.getTolerance())-1;
							if (indexOfNewPoint>=0)
								add(editor.bind(mxResources.get("addCorner"), new AddCornerToEdgeAction(c,unscaledGraphPoint,graphPt,indexOfNewPoint)));
						} else if (index>0 && index<lastIndex)
							add(editor.bind(mxResources.get("removeCorner"), new RemoveCornerToEdgeAction(c,index-1)));
					} else {
						JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("toggleWithTarget"), new ToggleWithTargetAction(c)));
						menuItem.setSelected(((SCXMLEdge)(c.getValue())).isCycleWithTarget());
						add(menuItem);
					}
				}
			} else if (c.isVertex()) {				
				if (!inOutsourcedNode) {
					// add node in case the cell under the pointer is a swimlane
					boolean addNodeEnabled=graph.isSwimlane(c) && (editor.getCurrentFileIO()!=null);
					add(editor.bind(mxResources.get("addNode"), new AddAction(mousePt,c))).setEnabled(addNodeEnabled);
					addSeparator();
					mxCell root=SCXMLImportExport.followUniqueDescendantLineTillSCXMLValueIsFound(model);
					add(editor.bind(mxResources.get("editNode"), new EditNodeAction(c,screenCoord))).setEnabled(c!=root);
					add(editor.bind(mxResources.get("editNamespace"), new EditNamespaceAction(c,screenCoord)));
					add(editor.bind(mxResources.get("editDataModel"), new EditDatamodelAction(c,screenCoord)));
					if (c!=root) {
						add(editor.bind(mxResources.get("editOutgoingEdgeOrder"), new EditEdgeOrderAction(c,screenCoord))).setEnabled(graph.getAllOutgoingEdges(c).length>1);
						JMenuItem menuItem2 = new JMenuItem(editor.bind(mxResources.get("editOutsourcedNode"), new SetNodeAsOutsourced(c,screenCoord)));
						menuItem2.setEnabled(!((SCXMLNode)(c.getValue())).isClusterNode() || (c.getChildCount()==0) || ((SCXMLNode)(c.getValue())).isOutsourcedNode());
						add(menuItem2);
						addSeparator();
						JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsInitialNode"), new SetNodeAsInitial(c)));
						menuItem.setSelected(((SCXMLNode)(c.getValue())).isInitial());
						add(menuItem);
						menuItem = new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsFinalNode"), new SetNodeAsFinal(c)));
						menuItem.setSelected(((SCXMLNode)(c.getValue())).isFinal());
						add(menuItem);
						menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsClusterNode"), new SetNodeAsCluster(c)));
						menuItem.setSelected(((SCXMLNode)(c.getValue())).isClusterNode());
						menuItem.setEnabled(!((SCXMLNode)(c.getValue())).isOutsourcedNode());
						add(menuItem);
						menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("setAsParallelNode"), new SetNodeAsParallel(c)));
						menuItem.setSelected(((SCXMLNode)(c.getValue())).isParallel());
						add(menuItem);
					}
				}
				if (((SCXMLNode)(c.getValue())).isOutsourcedNode()) {
					JCheckBoxMenuItem menuItem=new JCheckBoxMenuItem(editor.bind(mxResources.get("toggleViewOutsourcedContent"), new ToggleDisplayOutsourcedContentInNode(c)));
					menuItem.setSelected(c.getChildCount()>0);
					add(menuItem);
				}
				addSeparator();
				add(editor.bind(mxResources.get("doLayout"), new DoLayoutAction(graph,c)));
			}
		} else {
			add(editor.bind(mxResources.get("editNodeEdge"), null)).setEnabled(false);
		}
	}
}
