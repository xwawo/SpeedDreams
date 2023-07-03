/*
 *   XmlWriter.java
 *   Created on 14 ??? 2005
 *
 *    The XmlWriter.java is part of TrackEditor-0.6.0.
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
package plugin.torcs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;

import gui.EditorFrame;
import utils.Editor;
import utils.SegmentVector;
import utils.circuit.Camera;
import utils.circuit.Curve;
import utils.circuit.EnvironmentMapping;
import utils.circuit.ObjectMap;
import utils.circuit.Sector;
import utils.circuit.Segment;
import utils.circuit.SegmentSide;
import utils.circuit.Straight;
import utils.circuit.Surface;
import utils.circuit.TrackLight;
import utils.circuit.TrackObject;

/**
 * @author Charalampos Alexopoulos
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class XmlWriter
{
	private EditorFrame			editorFrame;
	
	public XmlWriter(EditorFrame editorFrame)
	{
		this.editorFrame = editorFrame;
	}
	
	public void writeXml(String fileName) throws FileNotFoundException, IOException, SecurityException
	{
		Document doc = new Document();

		getXml(doc);
		writeToFile(fileName, doc);
	}

	/**
	 * @param segments
	 * @return
	 */
	private synchronized void getXml(Document doc)
	{
		Comment com;
		Element root = getRoot();

		DocType type = new DocType("params", "../../../src/libs/tgf/params.dtd");
		String entity = "<!--  general definitions for tracks  -->\n";
		entity += "<!ENTITY default-surfaces SYSTEM \"../../../data/tracks/surfaces.xml\">\n";
		entity += "<!ENTITY default-objects SYSTEM \"../../../data/tracks/objects.xml\">\n";
		type.setInternalSubset(entity);
		com = new Comment(getCredit());
		doc.addContent(com);
		com = new Comment(getLicence());
		doc.addContent(com);
		doc.addContent(type);
		doc.setRootElement(root);
		root.addContent(getLights());
		root.addContent(getSurfaces());
		root.addContent(getObjects());
		root.addContent(getHeader());
		root.addContent(getLocal());
		root.addContent(getGraphic());
		root.addContent(getGrid());
		root.addContent(getTrack());
		root.addContent(getCameras());
		root.addContent(getSectors());
	}

	private synchronized void writeToFile(String fileName, Document doc) throws FileNotFoundException, IOException, SecurityException
	{
		FileOutputStream out = new FileOutputStream(fileName);
		XMLOutput op = new XMLOutput(Format.getPrettyFormat());
		op.output(doc, out);
		out.flush();
		out.close();
	}

	/**
	 * @return
	 */
	private synchronized Element getRoot()
	{
		Attribute name = new Attribute("name", "test");
		Attribute val = new Attribute("type", "param");
		Attribute mode = new Attribute("mode", "mw");

		Element root = new Element("params");
		root.setAttribute(name);
		root.setAttribute(val);
		root.setAttribute(mode);

		return root;
	}

	/**
	 * @return
	 */
	private synchronized Element getTrack()
	{
		Attribute name = new Attribute("name", "Main Track");
		Element track = new Element("section");
		Comment com = null;
		track.setAttribute(name);
		addContent(track, "width", "m", editorFrame.getTrackData().getMainTrack().getWidth());
		addContent(track, "profil", editorFrame.getTrackData().getMainTrack().getProfil());
		addContent(track, "profil steps length", "m", editorFrame.getTrackData().getMainTrack().getProfilStepsLength());
		addContent(track, "surface", editorFrame.getTrackData().getMainTrack().getSurface());
		addContent(track, "raceline widthscale", null, editorFrame.getTrackData().getMainTrack().getRacelineWidthscale());
		addContent(track, "raceline int", null, editorFrame.getTrackData().getMainTrack().getRacelineInt());
		addContent(track, "raceline ext", null, editorFrame.getTrackData().getMainTrack().getRacelineExt());

		if (editorFrame.getTrackData().getHeader().getVersion() == 3)
		{
			getSideV3(track, editorFrame.getTrackData().getMainTrack().getLeft(), "l");
			getSideV3(track, editorFrame.getTrackData().getMainTrack().getRight(), "r");
			getPitsV3(track);
		}
		else
		{
			com = new Comment("Left part of track");
			track.addContent(com);
			if (editorFrame.getTrackData().getMainTrack().getLeft().getHasSide())
				track.addContent(getSide(editorFrame.getTrackData().getMainTrack().getLeft(), "Left"));
			if (editorFrame.getTrackData().getMainTrack().getLeft().getHasBorder())
				track.addContent(getBorder(editorFrame.getTrackData().getMainTrack().getLeft(), "Left"));
			if (editorFrame.getTrackData().getMainTrack().getLeft().getHasBarrier())
				track.addContent(getBarrier(editorFrame.getTrackData().getMainTrack().getLeft(), "Left"));
			com = new Comment("End of left part");
			track.addContent(com);
			com = new Comment("Right part of track");
			track.addContent(com);
			if (editorFrame.getTrackData().getMainTrack().getRight().getHasSide())
				track.addContent(getSide(editorFrame.getTrackData().getMainTrack().getRight(), "Right"));
			if (editorFrame.getTrackData().getMainTrack().getRight().getHasBorder())
				track.addContent(getBorder(editorFrame.getTrackData().getMainTrack().getRight(), "Right"));
			if (editorFrame.getTrackData().getMainTrack().getRight().getHasBarrier())
				track.addContent(getBarrier(editorFrame.getTrackData().getMainTrack().getRight(), "Right"));
			com = new Comment("End of right part");
			track.addContent(com);
			track.addContent(getPits());
		}
		track.addContent(getSegments());

		return track;
	}

	/**
	 * @return
	 */
	private synchronized Element getPits()
	{
		Element pits = new Element("section");
		pits.setAttribute(new Attribute("name", "Pits"));

		addContent(pits, "pit style", null, editorFrame.getTrackData().getMainTrack().getPits().getStyle());
		addContent(pits, "side", editorFrame.getTrackData().getMainTrack().getPits().getSide());
		addContent(pits, "entry", editorFrame.getTrackData().getMainTrack().getPits().getEntry());
		addContent(pits, "start", editorFrame.getTrackData().getMainTrack().getPits().getStart());
		addContent(pits, "start buildings", editorFrame.getTrackData().getMainTrack().getPits().getStartBuildings());
		addContent(pits, "stop buildings", editorFrame.getTrackData().getMainTrack().getPits().getStopBuildings());
		addContent(pits, "max pits", null, editorFrame.getTrackData().getMainTrack().getPits().getMaxPits());
		addContent(pits, "end", editorFrame.getTrackData().getMainTrack().getPits().getEnd());
		addContent(pits, "exit", editorFrame.getTrackData().getMainTrack().getPits().getExit());
		addContent(pits, "length", "m", editorFrame.getTrackData().getMainTrack().getPits().getLength());
		addContent(pits, "width", "m", editorFrame.getTrackData().getMainTrack().getPits().getWidth());
		addContent(pits, "pit indicator", null, editorFrame.getTrackData().getMainTrack().getPits().getIndicator());
		addContent(pits, "speed limit", "m", editorFrame.getTrackData().getMainTrack().getPits().getSpeedLimit());

		return pits;
	}

	/**
	 * @return
	 */
	private synchronized void getPitsV3(Element pits)
	{
		if (editorFrame.getTrackData().getMainTrack().getPits().getStyle() == 0)
		{
			addContent(pits, "pit type", "no pits");
		}
		else if (editorFrame.getTrackData().getMainTrack().getPits().getStyle() == 1)
		{
			addContent(pits, "pit type", "track side");
		}
		else if (editorFrame.getTrackData().getMainTrack().getPits().getStyle() == 2)
		{
			addContent(pits, "pit type", "seperate path");
		}

		addContent(pits, "pit side", editorFrame.getTrackData().getMainTrack().getPits().getSide());
		addContent(pits, "pit entry", editorFrame.getTrackData().getMainTrack().getPits().getEntry());
		addContent(pits, "pit start", editorFrame.getTrackData().getMainTrack().getPits().getStart());
		addContent(pits, "start buildings", editorFrame.getTrackData().getMainTrack().getPits().getStartBuildings());
		addContent(pits, "stop buildings", editorFrame.getTrackData().getMainTrack().getPits().getStopBuildings());
		addContent(pits, "pit end", editorFrame.getTrackData().getMainTrack().getPits().getEnd());
		addContent(pits, "pit exit", editorFrame.getTrackData().getMainTrack().getPits().getExit());
		addContent(pits, "pit length", "m", editorFrame.getTrackData().getMainTrack().getPits().getLength());
		addContent(pits, "pit width", "m", editorFrame.getTrackData().getMainTrack().getPits().getWidth());
		addContent(pits, "speed limit", "m", editorFrame.getTrackData().getMainTrack().getPits().getSpeedLimit());
	}

	/**
	 * @return
	 */
	private synchronized Element getSegments()
	{
		SegmentVector segments = editorFrame.getTrackData().getSegments();
		Segment prev = null;
		Attribute name = null;
		if (editorFrame.getTrackData().getHeader().getVersion() == 3)
			name = new Attribute("name", "segments");
		else
			name = new Attribute("name", "Track Segments");
		Comment com = null;
		Element trackSegs = new Element("section");
		trackSegs.setAttribute(name);

		for (int i = 0; i < segments.size(); i++)
		{
			Segment shape = segments.get(i);
			shape.previousShape = prev;
			com = new Comment("******************************");
			trackSegs.addContent(com);
			com = new Comment("     Segment " + (i + 1) + "                ");
			trackSegs.addContent(com);
			com = new Comment("******************************");
			trackSegs.addContent(com);
			trackSegs.addContent(getSegment(shape));
			prev = shape;
		}

		return trackSegs;
	}

	/**
	 * @param shape
	 * @return
	 */
	private synchronized Element getSegment(Segment shape)
	{
		Attribute name = null;
		Comment com = null;
		Element el = null;
		Element segment = new Element("section");
		name = new Attribute("name", shape.getName());
		segment.setAttribute(name);
		addContent(segment, "comment", shape.getComment());
		el = attstrElement("type", shape.getType());
		segment.addContent(el);
		if (shape.getType().equals("str"))
		{
			addContent(segment, "lg", "m", ((Straight) shape).getLength());
		} else
		{
			double arc = ((Curve) shape).getArcDeg();
			addContent(segment, "arc", "deg", arc);

			double radStart = ((Curve) shape).getRadiusStart();
			addContent(segment, "radius", "m", radStart);

			double radEnd = ((Curve) shape).getRadiusEnd();
			if (radStart != radEnd)
			{
				addContent(segment, "end radius", "m", radEnd);
			}
			addContent(segment, "marks", ((Curve) shape).getMarks());
		}
		if (!Double.isNaN(shape.getHeightStartLeft()) &&
			!Double.isNaN(shape.getHeightStartRight()) &&
			shape.getHeightStartLeft() == shape.getHeightStartRight())
		{
			addContent(segment, "z start", "m", shape.getHeightStartLeft());
		}
		else
		{
			addContent(segment, "z start left", "m", shape.getHeightStartLeft());
			addContent(segment, "z start right", "m", shape.getHeightStartRight());
		}
		if (!Double.isNaN(shape.getHeightEndLeft()) &&
			!Double.isNaN(shape.getHeightEndRight()) &&
			shape.getHeightEndLeft() == shape.getHeightEndRight())
		{
			addContent(segment, "z end", "m", shape.getHeightEndLeft());
		}
		else
		{
			addContent(segment, "z end left", "m", shape.getHeightEndLeft());
			addContent(segment, "z end right", "m", shape.getHeightEndRight());
		}
		addContent(segment, "grade", "%", shape.getGrade());
		addContent(segment, "banking start", "deg", shape.getBankingStart());
		addContent(segment, "banking end", "deg", shape.getBankingEnd());
		addContent(segment, "profil", shape.getProfil());
		addContent(segment, "profil steps", "m", shape.getProfilSteps());
		addContent(segment, "profil steps length", "m", shape.getProfilStepsLength());
		addContent(segment, "profil start tangent", "%", shape.getProfilStartTangent());
		addContent(segment, "profil end tangent", "%", shape.getProfilEndTangent());
		addContent(segment, "profil start tangent left", "%", shape.getProfilStartTangentLeft());
		addContent(segment, "profil end tangent left", "%", shape.getProfilEndTangentLeft());
		addContent(segment, "profil start tangent right", "%", shape.getProfilStartTangentRight());
		addContent(segment, "profil end tangent right", "%", shape.getProfilEndTangentRight());
		addContent(segment, "surface", shape.getSurface());
		
		if (editorFrame.getTrackData().getHeader().getVersion() == 3)
		{
			getSideV3(segment, shape.getLeft(), "l");
			getSideV3(segment, shape.getRight(), "r");
		}
		else
		{		
			com = new Comment("Left part of segment");
			segment.addContent(com);
			if (shape.getLeft().getHasSide())
				segment.addContent(getSide(shape.getLeft(), "Left"));
			if (shape.getLeft().getHasBorder())
				segment.addContent(getBorder(shape.getLeft(), "Left"));
			if (shape.getLeft().getHasBarrier())
				segment.addContent(getBarrier(shape.getLeft(), "Left"));
			com = new Comment("End of left part");
			segment.addContent(com);
			com = new Comment("Right part of segment");
			segment.addContent(com);
			if (shape.getRight().getHasSide())
				segment.addContent(getSide(shape.getRight(), "Right"));
			if (shape.getRight().getHasBorder())
			    segment.addContent(getBorder(shape.getRight(), "Right"));
			if (shape.getRight().getHasBarrier())
			    segment.addContent(getBarrier(shape.getRight(), "Right"));
			com = new Comment("End of right part");
			segment.addContent(com);
		}

		return segment;
	}

	/**
	 * @param left
	 * @param string
	 * @return
	 */
	private synchronized Element getBorder(SegmentSide part, String sPart)
	{
		Element side = new Element("section");
		side.setAttribute(new Attribute("name", sPart + " Border"));

		addContent(side, "width", "m", part.getBorderWidth());
		addContent(side, "height", "m", part.getBorderHeight());
		addContent(side, "surface", part.getBorderSurface());
		addContent(side, "style", part.getBorderStyle());

		return side;
	}

	/**
	 * @param left
	 * @param string
	 * @return
	 */
	private synchronized Element getBarrier(SegmentSide part, String sPart)
	{
		Element side = new Element("section");
		side.setAttribute(new Attribute("name", sPart + " Barrier"));

		addContent(side, "width", "m", part.getBarrierWidth());
		addContent(side, "height", "m", part.getBarrierHeight());
		addContent(side, "surface", part.getBarrierSurface());
		addContent(side, "style", part.getBarrierStyle());

		return side;
	}

	/**
	 * @param left
	 * @return
	 */
	private synchronized void getSideV3(Element side, SegmentSide part, String sPart)
	{
		if (!Double.isNaN(part.getSideStartWidth()) &&
			!Double.isNaN(part.getSideEndWidth()) &&
			part.getSideStartWidth() == part.getSideEndWidth())
		{
			addContent(side, sPart + "side width", "m", part.getSideStartWidth());
		}
		else
		{
			addContent(side, sPart + "side start width", "m", part.getSideStartWidth());
			addContent(side, sPart + "side end width", "m", part.getSideEndWidth());
		}
		addContent(side, sPart + "side surface", part.getSideSurface());
		addContent(side, sPart + "side type", part.getSideBankingType());

		addContent(side, sPart + "border start width", "m", part.getBorderWidth());
		addContent(side, sPart + "border start height", "m", part.getBorderHeight());
		addContent(side, sPart + "border surface", part.getBorderSurface());
		addContent(side, sPart + "border type", part.getBorderStyle());

		addContent(side, sPart + "barrier start width", "m", part.getBarrierWidth());
		addContent(side, sPart + "barrier start height", "m", part.getBarrierHeight());
		addContent(side, sPart + "barrier surface", part.getBarrierSurface());
		addContent(side, sPart + "barrier type", part.getBarrierStyle());
	}

	/**
	 * @param left
	 * @return
	 */
	private synchronized Element getSide(SegmentSide part, String sPart)
	{
		Element side = new Element("section");
		side.setAttribute(new Attribute("name", sPart + " Side"));

		if (!Double.isNaN(part.getSideStartWidth()) &&
			!Double.isNaN(part.getSideEndWidth()) &&
			part.getSideStartWidth() == part.getSideEndWidth())
		{
			addContent(side, "width", "m", part.getSideStartWidth());
		}
		else
		{
			addContent(side, "start width", "m", part.getSideStartWidth());
			addContent(side, "end width", "m", part.getSideEndWidth());
		}
		addContent(side, "surface", part.getSideSurface());
		addContent(side, "banking type", part.getSideBankingType());

		return side;
	}

	private synchronized Element getSurfaces()
	{
		Element surfaces = new Element("section");
		surfaces.setAttribute(new Attribute("name", "Surfaces"));
		Element root = surfaces;

		if (editorFrame.getTrackData().getHeader().getVersion() == 3)
		{
			surfaces = new Element("section");
			surfaces.setAttribute(new Attribute("name", "List"));
			root.addContent(surfaces);
		}

		surfaces.setText("&default-surfaces;");

		Vector<Surface> surfaceData = editorFrame.getTrackData().getSurfaces();

		if (surfaceData == null)
			return surfaces;

		for (int i = 0; i < surfaceData.size(); i++)
		{
			Surface surface = surfaceData.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", surface.getName()));

			addContent(el, "color R1", null, surface.getColorR1());
			addContent(el, "color G1", null, surface.getColorG1());
			addContent(el, "color B1", null, surface.getColorB1());
			addContent(el, "color R2", null, surface.getColorR2());
			addContent(el, "color G2", null, surface.getColorG2());
			addContent(el, "color B2", null, surface.getColorB2());
			addContent(el, "texture name", surface.getTextureName());
			addContent(el, "texture type", surface.getTextureType());
			addContent(el, "texture size", "m", surface.getTextureSize());
			addContent(el, "texture radnomness", "m", surface.getTextureRandomness());
			addContent(el, "texture link with previous", surface.getTextureLinkWithPrevious());
			addContent(el, "texture start on boundary", surface.getTextureStartOnBoundary());
			addContent(el, "texture mipmap", "m", surface.getTextureMipMap());
			addContent(el, "friction", null, surface.getFriction());
			addContent(el, "friction rain", null, surface.getFrictionRain());
			addContent(el, "rolling resistance", null, surface.getRollingResistance());
			addContent(el, "rolling resistance rain", null, surface.getRollingResistanceRain());
			addContent(el, "bump name", surface.getBumpName());
			addContent(el, "bump size", "m", surface.getBumpSize());
			addContent(el, "roughness", "m", surface.getRoughness());
			addContent(el, "roughness wavelength", "m", surface.getRoughnessWavelength());
			addContent(el, "raceline name", surface.getRacelineName());
			addContent(el, "dammage", null, surface.getDammage());
			addContent(el, "rebound", null, surface.getRebound());

			surfaces.addContent(el);
		}

		return root;
	}

	private synchronized Element getCameras()
	{
		Element cameras = new Element("section");
		cameras.setAttribute(new Attribute("name", "Cameras"));
		Element root = cameras;

		Vector<Camera> cameraData = editorFrame.getTrackData().getCameras();

		if (cameraData == null)
			return cameras;

		if (editorFrame.getTrackData().getHeader().getVersion() == 3)
		{
			cameras = new Element("section");
			cameras.setAttribute(new Attribute("name", "list"));
			root.addContent(cameras);
		}

		for (int i = 0; i < cameraData.size(); i++)
		{
			Camera camera = cameraData.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", camera.getName()));

			addContent(el, "comment", camera.getComment());
			addContent(el, "segment", camera.getSegment());
			addContent(el, "to right", null, camera.getToRight());
			// units can be meters for straights or degrees for curves
			Segment segment = editorFrame.getSegment(camera.getSegment());
			if (segment != null && !segment.getType().equals("str"))
			{
				addContent(el, "to start", "deg", camera.getToStart());
			}
			else
			{
				addContent(el, "to start", null, camera.getToStart());
			}
			addContent(el, "height", null, camera.getHeight());
			addContent(el, "fov start", camera.getFovStart());
			addContent(el, "fov end", camera.getFovEnd());

			cameras.addContent(el);
		}

		return root;
	}

	private synchronized Element getLights()
	{
		Element lights = new Element("section");
		lights.setAttribute(new Attribute("name", "Track Lights"));

		Vector<TrackLight> lightData = editorFrame.getTrackData().getTrackLights();

		if (lightData == null)
			return lights;

		for (int i = 0; i < lightData.size(); i++)
		{
			TrackLight light = lightData.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", light.getName()));

			addContent(el, "role", light.getRole());

			if (light.getTopLeft() != null)
			{
				Element corner = new Element("section");
				corner.setAttribute(new Attribute("name", "topleft"));

				addContent(corner, "x", null, light.getTopLeft().x);
				addContent(corner, "y", null, light.getTopLeft().y);
				addContent(corner, "z", null, light.getTopLeft().z);

				el.addContent(corner);
			}

			if (light.getBottomRight() != null)
			{
				Element corner = new Element("section");
				corner.setAttribute(new Attribute("name", "bottomright"));

				addContent(corner, "x", null, light.getBottomRight().x);
				addContent(corner, "y", null, light.getBottomRight().y);
				addContent(corner, "z", null, light.getBottomRight().z);

				el.addContent(corner);
			}

			addContent(el, "texture on", light.getTextureOn());
			addContent(el, "texture off", light.getTextureOff());
			addContent(el, "index", null, light.getIndex());
			addContent(el, "red", null, light.getRed());
			addContent(el, "green", null, light.getGreen());
			addContent(el, "blue", null, light.getBlue());

			lights.addContent(el);
		}

		return lights;
	}
	private synchronized Element getSectors()
	{
		Element sectors = new Element("section");
		sectors.setAttribute(new Attribute("name", "Sectors"));

		Vector<Sector> sectorData = editorFrame.getTrackData().getSectors();

		if (sectorData == null)
			return sectors;

		for (int i = 0; i < sectorData.size(); i++)
		{
			Sector sector = sectorData.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", sector.getName()));
			addContent(el, "comment", sector.getComment());
			addContent(el, "distance from start", "m", sector.getDistanceFromStart());

			sectors.addContent(el);
		}

		return sectors;
	}

	private synchronized Element getObjects()
	{
		Element objects = new Element("section");
		objects.setAttribute(new Attribute("name", "Objects"));
		objects.setText("&default-objects;");

		Vector<TrackObject> objectData = editorFrame.getTrackData().getObjects();

		if (objectData == null)
			return objects;

		for (int i = 0; i < objectData.size(); i++)
		{
			TrackObject object = objectData.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", object.getName()));

			addContent(el, "object", object.getObject());
			addHexContent(el, "color", null, object.getColor());
			addContent(el, "orientation type", object.getOrientationType());
			addContent(el, "border distance", "m", object.getBorderDistance());
			addContent(el, "orientation", "deg", object.getOrientation());
			addContent(el, "delta height",null,  object.getDeltaHeight());
			addContent(el, "delta vert", null, object.getDeltaVert());
			addContent(el, "scale type", object.getScaleType());
			addContent(el, "scale", null, object.getScale());
			addContent(el, "scale min",null,  object.getScaleMin());
			addContent(el, "scale max", null, object.getScaleMax());

			objects.addContent(el);
		}

		return objects;
	}

	private synchronized String getCredit()
	{
		String sep = System.getProperty("file.separator");
		String filename = Editor.getProperties().getPath();
		String trackname = filename.substring(filename.lastIndexOf(sep) + 1);
		String tmp = "\n";
		tmp += "file                : " + trackname + ".xml\n";
		tmp += "auto generated      : by " + Editor.getProperties().title + "\n";
		tmp += "version             : " + Editor.getProperties().version + "\n";
		tmp += "copyright           : (C) 2005 by Charalampos Alexopoulos\n";

		return tmp;
	}

	private synchronized String getLicence()
	{
		String tmp = "\n";
		tmp += "This program is free software; you can redistribute it and/or modify it\n";
		tmp += "under the terms of the GNU General Public License as published by\n";
		tmp += "the Free Software Foundation; either version 2 of the License, or\n";
		tmp += "(at your option) any later version.";
		tmp += "\n";

		return tmp;
	}

	private synchronized Element getHeader()
	{
		Attribute name = new Attribute("name", "Header");
		String tmp = "";

		Element header = new Element("section");
		header.setAttribute(name);

		addContent(header, "name", editorFrame.getTrackData().getHeader().getName());

		if (editorFrame.getTrackData().getHeader().getCategory() != null)
		{
			tmp = editorFrame.getTrackData().getHeader().getCategory();
		} else
		{
			tmp = "road";
		}
		addContent(header, "category", tmp);
		addContent(header, "subcategory", editorFrame.getTrackData().getHeader().getSubcategory());
		addContent(header, "version", null, editorFrame.getTrackData().getHeader().getVersion());
		addContent(header, "sky version", null, editorFrame.getTrackData().getHeader().getSkyVersion());

		if (editorFrame.getTrackData().getHeader().getAuthor() != null)
		{
			tmp = editorFrame.getTrackData().getHeader().getAuthor();
		} else
		{
			tmp = "Anonymous";
		}
		addContent(header, "author", tmp);

		if (editorFrame.getTrackData().getHeader().getCopyright() != null)
		{
			tmp = editorFrame.getTrackData().getHeader().getCopyright();
			addContent(header, "copyright", tmp);
		}

		if (editorFrame.getTrackData().getHeader().getEmail() != null)
		{
			tmp = editorFrame.getTrackData().getHeader().getEmail();
			addContent(header, "email", tmp);
		}

		if (editorFrame.getTrackData().getHeader().getDescription() != null)
		{
			tmp = editorFrame.getTrackData().getHeader().getDescription();
		} else
		{
			tmp = "No description provided";
		}
		addContent(header, "description", tmp);

		return header;
	}

	/**
	 * @return
	 */
	private synchronized Element getLocal()
	{
		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Local Info"));

		addContent(element, "station", editorFrame.getTrackData().getLocalInfo().getStation());
		addContent(element, "timezone", null, editorFrame.getTrackData().getLocalInfo().getTimezone());
		addContent(element, "overall rain likelyhood", "%", editorFrame.getTrackData().getLocalInfo().getOverallRainLikelyhood());
		addContent(element, "little rain likelyhood", "%", editorFrame.getTrackData().getLocalInfo().getLittleRainLikelyhood());
		addContent(element, "medium rain likelyhood", "%", editorFrame.getTrackData().getLocalInfo().getMediumRainLikelyhood());
		addContent(element, "time of day", "hour", editorFrame.getTrackData().getLocalInfo().getTimeOfDay());
		addContent(element, "sun ascension", "deg", editorFrame.getTrackData().getLocalInfo().getSunAscension());
		addContent(element, "altitude", "m", editorFrame.getTrackData().getLocalInfo().getAltitude());

		return element;
	}

	private synchronized Element getGrid()
	{
		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Starting Grid"));

		addContent(element, "rows", null, editorFrame.getTrackData().getStartingGrid().getRows());
		addContent(element, "pole position side", editorFrame.getTrackData().getStartingGrid().getPolePositionSide());
		addContent(element, "distance to start", "m", editorFrame.getTrackData().getStartingGrid().getDistanceToStart());
		addContent(element, "distance between columns", "m", editorFrame.getTrackData().getStartingGrid().getDistanceBetweenColumns());
		addContent(element, "offset within a column", "m", editorFrame.getTrackData().getStartingGrid().getOffsetWithinAColumn());
		addContent(element, "initial height", "m", editorFrame.getTrackData().getStartingGrid().getInitialHeight());

		return element;
	}

	/**
	 * @return
	 */
	private synchronized Element getGraphic()
	{
		String sep = System.getProperty("file.separator");
		String filename = Editor.getProperties().getPath();
		String trackname = filename.substring(filename.lastIndexOf(sep) + 1);

		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Graphic"));

		addContent(element, "3d description", trackname + ".ac");
		addContent(element, "3d description night", editorFrame.getTrackData().getGraphic().getDescriptionNight());
		addContent(element, "3d description rain+night", editorFrame.getTrackData().getGraphic().getDescriptionRainNight());
		addContent(element, "background image", editorFrame.getTrackData().getGraphic().getBackgroundImage());
		addContent(element, "background type", null, editorFrame.getTrackData().getGraphic().getBackgroundType());
		addContent(element, "background color R", null, editorFrame.getTrackData().getGraphic().getBackgroundColorR());
		addContent(element, "background color G", null, editorFrame.getTrackData().getGraphic().getBackgroundColorG());
		addContent(element, "background color B", null, editorFrame.getTrackData().getGraphic().getBackgroundColorB());
		addContent(element, "ambient color R", null, editorFrame.getTrackData().getGraphic().getAmbientColorR());
		addContent(element, "ambient color G", null, editorFrame.getTrackData().getGraphic().getAmbientColorG());
		addContent(element, "ambient color B", null, editorFrame.getTrackData().getGraphic().getAmbientColorB());
		addContent(element, "diffuse color R", null, editorFrame.getTrackData().getGraphic().getDiffuseColorR());
		addContent(element, "diffuse color G", null, editorFrame.getTrackData().getGraphic().getDiffuseColorG());
		addContent(element, "diffuse color B", null, editorFrame.getTrackData().getGraphic().getDiffuseColorB());
		addContent(element, "specular color R", null, editorFrame.getTrackData().getGraphic().getSpecularColorR());
		addContent(element, "specular color G", null, editorFrame.getTrackData().getGraphic().getSpecularColorG());
		addContent(element, "specular color B", null, editorFrame.getTrackData().getGraphic().getSpecularColorB());
		addContent(element, "light position x", "m", editorFrame.getTrackData().getGraphic().getLightPositionX());
		addContent(element, "light position y", "m", editorFrame.getTrackData().getGraphic().getLightPositionY());
		addContent(element, "light position z", "m", editorFrame.getTrackData().getGraphic().getLightPositionZ());
		addContent(element, "shininess", null, editorFrame.getTrackData().getGraphic().getShininess());
		addContent(element, "fov factor", null, editorFrame.getTrackData().getGraphic().getFovFactor());

		element.addContent(getTurnMarks());
		element.addContent(getTerrainGeneration());
		element.addContent(getEnvironmentMapping());

		return element;
	}

	private synchronized Element getTurnMarks()
	{
		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Turn Marks"));

		addContent(element, "width", "m", editorFrame.getTrackData().getGraphic().getTurnMarks().getWidth());
		addContent(element, "height", "m", editorFrame.getTrackData().getGraphic().getTurnMarks().getHeight());
		addContent(element, "vertical space", "m", editorFrame.getTrackData().getGraphic().getTurnMarks().getVerticalSpace());
		addContent(element, "horizontal space", "m", editorFrame.getTrackData().getGraphic().getTurnMarks().getHorizontalSpace());

		return element;
	}

	private synchronized Element getTerrainGeneration()
	{
		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Terrain Generation"));

		addContent(element, "track step", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getTrackStep());
		addContent(element, "border margin", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getBorderMargin());
		addContent(element, "border step", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getBorderStep());
		addContent(element, "border height", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getBorderHeight());
		addContent(element, "orientation", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getOrientation());
		addContent(element, "maximum altitude", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getMaximumAltitude());
		addContent(element, "minimum altitude", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getMinimumAltitude());
		addContent(element, "group size", "m", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getGroupSize());
		addContent(element, "elevation map", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getElevationMap());
		addContent(element, "relief file", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getReliefFile());
		addContent(element, "relief border", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getReliefBorder());
		addContent(element, "surface", editorFrame.getTrackData().getGraphic().getTerrainGeneration().getSurface());
		addContent(element, "random seed", null, editorFrame.getTrackData().getGraphic().getTerrainGeneration().getRandomSeed());

		element.addContent(getObjectMaps());

		return element;
	}

	private synchronized Element getEnvironmentMapping()
	{
		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Environment Mapping"));

		Vector<EnvironmentMapping> environment = editorFrame.getTrackData().getGraphic().getEnvironmentMapping();

		for (int i = 0; i < environment.size(); i++)
		{
			EnvironmentMapping data = environment.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", data.getName()));

			addContent(el, "env map image", data.getEnvMapImage());

			element.addContent(el);
		}

		return element;
	}

	private synchronized Element getObjectMaps()
	{
		Element element = new Element("section");
		element.setAttribute(new Attribute("name", "Object Maps"));

		Vector<ObjectMap> objMaps = editorFrame.getTrackData().getGraphic().getTerrainGeneration().getObjectMaps();

		for (int i = 0; i < objMaps.size(); i++)
		{
			ObjectMap data = objMaps.get(i);

			Element el = new Element("section");
			el.setAttribute(new Attribute("name", data.getName()));

			addContent(el, "object map", data.getObjectMap());

			element.addContent(el);

			if (data.getChanged())
			{
				try
				{
					data.writeImage();
				}
				catch (IOException e)
				{
				}
			}
		}

		return element;
	}

	private synchronized Element attstrElement(String attname, String attval)
	{
		Attribute name = null;
		Attribute val = null;

		Element el = new Element("attstr");
		name = new Attribute("name", attname);
		if (attval == null)
		{
			attval = "";
		}
		val = new Attribute("val", attval);
		el.setAttribute(name);
		el.setAttribute(val);

		return el;
	}

	private synchronized Element attnumElement(String attname, String attunit, String attval)
	{
		Attribute name = null;
		Attribute unit = null;
		Attribute val = null;

		Element el = new Element("attnum");
		name = new Attribute("name", attname);
		el.setAttribute(name);
		if (attunit != null)
		{
			unit = new Attribute("unit", attunit);
			el.setAttribute(unit);
		}
		val = new Attribute("val", attval);
		el.setAttribute(val);

		return el;
	}

	private synchronized void addContent(Element section, String attribute, String units, double value)
	{
		if (!Double.isNaN(value))
		{
			section.addContent(attnumElement(attribute, units, value + ""));
		}
	}

	private synchronized void addContent(Element section, String attribute, String units, int value)
	{
		if (value != Integer.MAX_VALUE)
		{
			section.addContent(attnumElement(attribute, units, value + ""));
		}
	}

	private synchronized void addHexContent(Element section, String attribute, String units, int value)
	{
		if (value != Integer.MAX_VALUE)
		{
			section.addContent(attnumElement(attribute, units, String.format("0x%06X", value)));
		}
	}

	private synchronized void addContent(Element section, String attribute, String string)
	{
		if (string != null && !string.isEmpty())
		{
			section.addContent(attstrElement(attribute, string.replace("\"", "&quot;")));
		}
	}
}
