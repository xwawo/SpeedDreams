package utils.circuit;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import utils.Editor;

public class ObjectMap
{
	// XML attributes
	private String					name		= null;
	private String					objectMap	= null;
	
	// image information
	private Vector<ObjShapeObject>	objects		= new Vector<ObjShapeObject>();
	private Set<Integer>			colors		= new HashSet<Integer>();
	private int 					imageWidth	= 0;
	private int 					imageHeight	= 0;

	private Boolean					changed		= false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getObjectMap() {
		return objectMap;
	}

	public void setObjectMap(String objectMap) throws IOException {
		this.objectMap = objectMap;
		readImage();
	}

	public Vector<ObjShapeObject> getObjects() {
		return objects;
	}

	public void addObject(ObjShapeObject object)
	{
		objects.add(object);
		changed = true;
	}

	public void removeObject(ObjShapeObject object)
	{
		objects.remove(object);
		changed = true;
	}

	public Set<Integer> getColors() {
		return colors;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public Boolean getChanged() {
		return changed;
	}

	public void setChanged(Boolean changed) {
		this.changed = changed;
	}

	public void readImage() throws IOException
	{
		objects.clear();
		colors.clear();
		imageWidth = 0;
		imageHeight = 0;

		Path filename = Paths.get(objectMap);
		Path trackPath = Paths.get(Editor.getProperties().getPath());

		// check for parent directory
		if (filename.getParent() == null)
		{
			// use track directory
			filename = Paths.get(trackPath.toString(), filename.toString());
		}

		BufferedImage image = ImageIO.read(new File(filename.toString()));
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();

		for (int x = 0; x < imageWidth; x++)
		{
			for (int y = 0; y < imageHeight; y++)
			{
				int rgb = image.getRGB(x, y) & 0x00ffffff;

				if (rgb != 0x0)
				{
					colors.add(rgb);
					objects.add(new ObjShapeObject(rgb, x, y));
				}
			}
		}
	}

	public void writeImage() throws IOException
	{
		if (!changed)
			return;

		if (objectMap == null || objectMap.isEmpty())
			return;

		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphic = image.createGraphics();
		graphic.setColor(new Color(0x00000000));
		graphic.fillRect(0, 0, imageWidth, imageHeight);

		for (int i = 0; i < objects.size(); i++)
		{
			ObjShapeObject object = objects.get(i);

			graphic.setColor(object.getColor());
			graphic.drawLine(object.getImageX(), object.getImageY(), object.getImageX(), object.getImageY());
		}
		graphic.dispose();

		Path filename = Paths.get(objectMap);
		Path trackPath = Paths.get(Editor.getProperties().getPath());

		// check for parent directory
		if (filename.getParent() == null)
		{
			// use track directory
			filename = Paths.get(trackPath.toString(), filename.toString());
		}

		ImageIO.write(image, "png", new File(filename.toString()));
	}

	public void dump(String indent)
    {
		System.out.println(indent + "ObjectMap");
		System.out.println(indent + "  name        : " + name);
		System.out.println(indent + "  objectMap   : " + objectMap);
		System.out.println(indent + "  imageWidth  : " + imageWidth);
		System.out.println(indent + "  imageHeight : " + imageHeight);
		System.out.println(indent + "  colors[" + colors.size() + "]");
		Iterator<Integer> colorsIterator = colors.iterator();
		while (colorsIterator.hasNext())
		{
			System.out.println(indent + "    " + String.format("0x%06X", colorsIterator.next()));
		}
		System.out.println(indent + "  objects[" + objects.size() + "]");
		for (int i = 0; i < objects.size(); i++)
		{
			ObjShapeObject object = objects.get(i);
			System.out.println(indent + "    " + String.format("0x%06X", object.getRGB()) + " " + object.getImageX() + " " + object.getImageY());
		}
    }	
}
