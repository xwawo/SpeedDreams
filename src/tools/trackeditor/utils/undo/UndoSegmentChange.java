/*
 *   UndoSegmentChange.java
 *   Created on 29 ??? 2005
 *
 *    The UndoSegmentChange.java is part of TrackEditor-0.6.0.
 *
 *    TrackEditor-0.6.0 is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    TrackEditor-0.6.0 is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with TrackEditor-0.6.0; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package utils.undo;

import gui.EditorFrame;
import utils.SegmentVector;
import utils.circuit.Segment;

/**
 * @author Charalampos Alexopoulos
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UndoSegmentChange implements UndoInterface
{
	private EditorFrame	editorFrame;
	private Segment 	original;
	private Segment 	clone;
	private int 	pos;


	/**
	 * 
	 */
	public UndoSegmentChange(EditorFrame editorFrame, Segment segment)
	{
		this.editorFrame = editorFrame;
		clone = (Segment) segment.clone();
		this.original = segment;

		SegmentVector data = editorFrame.getTrackData().getSegments();
		if (!data.validateLinks())
		{
			System.out.println("UndoSegmentChange");
			data.dumpLinks();
		}
	}

	/* (non-Javadoc)
	 * @see utils.undo.UndoInterface#undo()
	 */
	public void undo()
	{
		SegmentVector data = editorFrame.getTrackData().getSegments();
		if (data != null)
		{
			pos = data.indexOf(original);
			data.set(pos,clone);

			if (!data.validateLinks())
			{
				System.out.println("UndoSegmentChange.undo");
				data.dumpLinks();
			}
		}
	}

	/* (non-Javadoc)
	 * @see utils.undo.UndoInterface#redo()
	 */
	public void redo()
	{
		SegmentVector data = editorFrame.getTrackData().getSegments();
		pos = data.indexOf(clone);
		data.set(pos,original);

		if (!data.validateLinks())
		{
			System.out.println("UndoSegmentChange.redo");
			data.dumpLinks();
		}
	}
}
