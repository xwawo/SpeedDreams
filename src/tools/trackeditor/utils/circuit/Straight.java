/*
 *   Straight.java
 *   Created on 9 ??? 2005
 *
 *    The Straight.java is part of TrackEditor-0.6.0.
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
package utils.circuit;

import java.awt.geom.Point2D;

import gui.EditorFrame;
import utils.Editor;


/**
 * @author Patrice Espie , Charalampos Alexopoulos
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

public class Straight extends Segment
{
	public Straight()
	{
		this(null);
	}

	public Straight(Segment prev)
	{
		super("str");
		this.previousShape = prev;
	}

	public void calcShape(EditorFrame editorFrame) throws Exception
	{
		double currentX = Editor.getProperties().getCurrentX();
		double currentY = Editor.getProperties().getCurrentY();
		double currentA = Editor.getProperties().getCurrentA();
		double showArrows = Editor.getProperties().getShowArrows();
		double trackWidth = editorFrame.getTrackData().getMainTrack().getWidth();
		double leftBorderWidth = getValidLeftBorderWidth(editorFrame);
		double rightBorderWidth = getValidRightBorderWidth(editorFrame);
		double leftSideStartWidth = getValidLeftSideStartWidth(editorFrame);
		double leftSideEndWidth = getValidLeftSideEndWidth(editorFrame);
		double rightSideStartWidth = getValidRightSideStartWidth(editorFrame);
		double rightSideEndWidth = getValidRightSideEndWidth(editorFrame);

		if (points == null || points.length != 4 * (5 + (showArrows > 0.0 ? 1 : 0)))
		{
			points = new Point2D.Double[4 * (5 + (showArrows > 0.0 ? 1 : 0))]; 
			for (int i = 0; i < points.length; i++)
				points[i] = new Point2D.Double();

			trPoints = new Point2D.Double[4 * (5 + (showArrows > 0.0 ? 1 : 0))];
			for (int i = 0; i < trPoints.length; i++)
				trPoints[i] = new Point2D.Double();
		}
		double cos = Math.cos(currentA) * length;
		double sin = Math.sin(currentA) * length;

		double cosTransLeft = Math.cos(currentA + Math.PI / 2);
		double sinTransLeft = Math.sin(currentA + Math.PI / 2);

		// track
		points[0].x = currentX + cosTransLeft * trackWidth / 2;
		points[0].y = currentY + sinTransLeft * trackWidth / 2;

		points[1].x = points[0].x + cos;
		points[1].y = points[0].y + sin;

		points[3].x = currentX - cosTransLeft * trackWidth / 2;
		points[3].y = currentY - sinTransLeft * trackWidth / 2;

		points[2].x = points[3].x + cos;
		points[2].y = points[3].y + sin;

		// left border

		points[4].x = currentX + cosTransLeft * (trackWidth / 2 + leftBorderWidth);
		points[4].y = currentY + sinTransLeft * (trackWidth / 2 + leftBorderWidth);

		points[5].x = points[4].x + cos;
		points[5].y = points[4].y + sin;

		points[7].x = currentX + cosTransLeft * trackWidth / 2;
		points[7].y = currentY + sinTransLeft * trackWidth / 2;

		points[6].x = points[7].x + cos;
		points[6].y = points[7].y + sin;

		// left side

		points[8].x = currentX + cosTransLeft * (trackWidth / 2 + leftBorderWidth + leftSideStartWidth);
		points[8].y = currentY + sinTransLeft * (trackWidth / 2 + leftBorderWidth + leftSideStartWidth);

		points[9].x = currentX + cos + cosTransLeft * (trackWidth / 2 + leftBorderWidth + leftSideEndWidth);
		points[9].y = currentY + sin + sinTransLeft * (trackWidth / 2 + leftBorderWidth + leftSideEndWidth);

		points[10].x = currentX + cos + cosTransLeft * (trackWidth / 2 + leftBorderWidth);
		points[10].y = currentY + sin + sinTransLeft * (trackWidth / 2 + leftBorderWidth);

		points[11].x = currentX + cosTransLeft * (trackWidth / 2 + leftBorderWidth);
		points[11].y = currentY + sinTransLeft * (trackWidth / 2 + leftBorderWidth);

		// right border

		points[12].x = currentX - cosTransLeft * (trackWidth / 2 + rightBorderWidth);
		points[12].y = currentY - sinTransLeft * (trackWidth / 2 + rightBorderWidth);

		points[13].x = points[12].x + cos;
		points[13].y = points[12].y + sin;

		points[15].x = currentX - cosTransLeft * trackWidth / 2;
		points[15].y = currentY - sinTransLeft * trackWidth / 2;

		points[14].x = points[15].x + cos;
		points[14].y = points[15].y + sin;

		// right side

		points[16].x = currentX - cosTransLeft * (trackWidth / 2 + rightBorderWidth + rightSideStartWidth);
		points[16].y = currentY - sinTransLeft * (trackWidth / 2 + rightBorderWidth + rightSideStartWidth);

		points[17].x = currentX + cos - cosTransLeft * (trackWidth / 2 + rightBorderWidth + rightSideEndWidth);
		points[17].y = currentY + sin - sinTransLeft * (trackWidth / 2 + rightBorderWidth + rightSideEndWidth);

		points[18].x = currentX + cos - cosTransLeft * (trackWidth / 2 + rightBorderWidth);
		points[18].y = currentY + sin - sinTransLeft * (trackWidth / 2 + rightBorderWidth);

		points[19].x = currentX - cosTransLeft * (trackWidth / 2 + rightBorderWidth);
		points[19].y = currentY - sinTransLeft * (trackWidth / 2 + rightBorderWidth);

		if (showArrows > 0.0)
		{
			// arrow
			points[20].x = currentX + cosTransLeft * trackWidth / 2;
			points[20].y = currentY + sinTransLeft * trackWidth / 2;

			points[21].x = points[0].x + cos - (cosTransLeft * trackWidth / 2) * 0.99999;
			points[21].y = points[0].y + sin - (sinTransLeft * trackWidth / 2) * 0.99999;

			points[23].x = currentX - cosTransLeft * trackWidth / 2;
			points[23].y = currentY - sinTransLeft * trackWidth / 2;

			points[22].x = points[23].x + cos + (cosTransLeft * trackWidth / 2) * 0.99999;
			points[22].y = points[23].y + sin + (sinTransLeft * trackWidth / 2) * 0.99999;
		}
		// move track center
		this.dx = cos;
		this.dy = sin;
		currentX += cos;
		currentY += sin;

		endTrackCenter.setLocation(currentX, currentY);
		endTrackAlpha = startTrackAlpha;

		//System.out.println("X = "+tmpX+" , Y = "+tmpY+" , Length = "+this.length);
		
		Editor.getProperties().setCurrentA(currentA);
		Editor.getProperties().setCurrentX(currentX);
		Editor.getProperties().setCurrentY(currentY);
		
	}

	public void drag(Point2D.Double dragDelta)
	{
	}

	
	public Object clone()
	{
		Straight s;
		s = (Straight) super.clone();
		
		return s; // return the clone
	}

}