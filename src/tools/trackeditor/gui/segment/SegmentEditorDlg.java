/*
 *   SegmentEditorDlg.java
 *   Created on 28 ??? 2005
 *
 *    The SegmentEditorDlg.java is part of TrackEditor-0.3.1.
 *
 *    TrackEditor-0.3.1 is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    TrackEditor-0.3.1 is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with TrackEditor-0.3.1; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gui.segment;

import gui.EditorFrame;
import gui.view.CircuitView;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utils.SurfaceComboBox;
import utils.circuit.Curve;
import utils.circuit.Segment;
import utils.circuit.Surface;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * @author babis
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SegmentEditorDlg extends JDialog implements SliderListener
{
	private Segment					shape;
	CircuitView						view;
	EditorFrame						editorFrame;
	public boolean					dirty						= false;

	private JPanel					jContentPane				= null;	//  @jve:decl-index=0:visual-constraint="377,10"

	private JTabbedPane				jTabbedPane					= null;
	private JPanel					centerPanel					= null;	//  @jve:decl-index=0:visual-constraint="394,34"
	private SegmentSlider			radiusStartSlider			= null;
	private SegmentSlider			radiusEndSlider				= null;
	private SegmentSlider			arcSlider					= null;
	private SegmentSlider			lengthSlider				= null;
	private JLabel					nameLabel					= null;
	private JTextField				nameTextField				= null;
	private JLabel					surfaceLabel				= null;
	private SurfaceComboBox			surfaceComboBox				= null;

	private SegmentSlider			gradeSlider					= null;
	private SegmentSlider			startTangentSlider			= null;
	private SegmentSlider			endTangentSlider			= null;
	private SegmentSlider			bankingStartSlider			= null;
	private SegmentSlider			bankingEndSlider			= null;
	private SegmentSlider			heightStartLeftSlider		= null;
	private SegmentSlider			heightStartRightSlider		= null;
	private SegmentSlider			heightEndLeftSlider			= null;
	private SegmentSlider			heightEndRightSlider		= null;
	private SegmentSlider			profilStepsSlider			= null;
	private SegmentSlider			profilStepsLengthSlider		= null;

	private SegmentSideProperties	rightPanel					= null;
	private SegmentSideProperties	leftPanel					= null;

	private GroupButton				groupButton					= null;
	private ProfileButton			profileButton				= null;

	private JLabel					marksLabel					= null;
	private JTextField				marksTextField				= null;
	private JLabel					commentLabel				= null;
	private JTextField				commentTextField			= null;

	private String[]				roadSurfaceItems			=
	{"asphalt-lines", "asphalt-l-left", "asphalt-l-right",
     "asphalt-l-both", "asphalt-pits", "asphalt", "dirt", "dirt-b", "asphalt2", "road1", "road1-pits",
     "road1-asphalt", "asphalt-road1", "b-road1", "b-road1-l2", "b-road1-l2p", "concrete", "concrete2",
     "concrete3", "b-asphalt", "b-asphalt-l1", "b-asphalt-l1p", "asphalt2-lines", "asphalt2-l-left",
     "asphalt2-l-right", "asphalt2-l-both", "grass", "grass3", "grass5", "grass6", "grass7", "gravel", "sand3",
     "sand", "curb-5cm-r", "curb-5cm-l", "curb-l", "tar-grass3-l", "tar-grass3-r", "tar-sand", "b-road1-grass6",
     "b-road1-grass6-l2", "b-road1-gravel-l2", "b-road1-sand3", "b-road1-sand3-l2", "b-asphalt-grass7",
     "b-asphalt-grass7-l1", "b-asphalt-grass6", "b-asphalt-grass6-l1", "b-asphalt-sand3", "b-asphalt-sand3-l1",
     "barrier", "barrier2", "barrier-turn", "barrier-grille", "wall", "wall2", "tire-wall"};
	private Vector<String>			roadSurfaceVector			= new Vector<String>();

	/**
	 *
	 */
	public SegmentEditorDlg()
	{
		super((Frame) null, "", true);
	}

	public SegmentEditorDlg(CircuitView view, EditorFrame editorFrame, String title, boolean modal, Segment shape)
	{
		super(editorFrame, title, modal);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		try
		{
			this.view = view;
			this.editorFrame = editorFrame;

			// add new surfaces from Surfaces
			addDefaultSurfaces(roadSurfaceVector, roadSurfaceItems);
			
			setShape(shape);

			initialize();
			//			pack();
			this.setVisible(true);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void refresh()
	{
		roadSurfaceVector.clear();
		addDefaultSurfaces(roadSurfaceVector, roadSurfaceItems);
		centerPanel.remove(surfaceComboBox);
		surfaceComboBox = null;
		centerPanel.add(getSurfaceComboBox(), null);

		rightPanel.refresh();
		leftPanel.refresh();
	}

	private void addDefaultSurfaces(Vector<String> surfaceVector, String[] fallback)
	{
		// use the default surfaces if available
		Vector<Surface> defaultSurfaces = editorFrame.getDefaultSurfaces();

		if (defaultSurfaces.isEmpty())
		{
			// add the ones found
			surfaceVector.addAll(Arrays.asList(fallback));
		}
		else
		{
			// add the ones that should be there
	        for (int i = 0; i < defaultSurfaces.size(); i++)
	        {
	        	surfaceVector.add(defaultSurfaces.get(i).getName());
	        }
		}

        Vector<Surface> surfaces = editorFrame.getTrackData().getSurfaces();
        for (int i = 0; i < surfaces.size(); i++)
        {
			String surface = surfaces.elementAt(i).getName();
			if (surface != null)
			{
				boolean found = false;
				for (int j = 0; j < surfaceVector.size(); j++)
				{
					if (surfaceVector.elementAt(i).equals(surfaces.elementAt(i).getName()))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					surfaceVector.add(surface);
				}
			}
        }
		Collections.sort(surfaceVector);
	}

	private void addSurface(Vector<String> surfaceVector, String surface)
	{
		// add this surface if it's not found in default list
		if (surface != null)
		{
			boolean found = false;
			for (int i = 0; i < surfaceVector.size(); i++)
			{
				if (surfaceVector.elementAt(i).equals(surface))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				surfaceVector.add(surface);
				Collections.sort(surfaceVector);
			}
		}
	}

	/**
	 *
	 */
	private void initialize()
	{
		this.setSize(850, 536);
		Point p = editorFrame.getLocation();
		p.x = editorFrame.getProject().getSegmentEditorX();
		p.y = editorFrame.getProject().getSegmentEditorY();
		this.setLocation(p);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null)
		{
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJTabbedPane(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane()
	{
		if (jTabbedPane == null)
		{
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Left", null, getLeftPanel(), null);
			jTabbedPane.addTab("Center", null, getCenterPanel(), null);
			jTabbedPane.addTab("Right", null, getRightPanel(), null);
			jTabbedPane.setSelectedIndex(1);
		}
		return jTabbedPane;
	}
	/**
	 * This method initializes centerPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getCenterPanel()
	{
		if (centerPanel == null)
		{
			nameLabel = new JLabel();
			surfaceLabel = new JLabel();
			marksLabel = new JLabel();
			commentLabel = new JLabel();
			centerPanel = new JPanel();
			centerPanel.setLayout(null);
			nameLabel.setBounds(10, 5, 55, 23);
			nameLabel.setText("Name");
			surfaceLabel.setBounds(10, 33, 55, 23);
			surfaceLabel.setText("Surface");
	        marksLabel.setText("Marks");
	        marksLabel.setBounds(440, 5, 60, 23);
	        commentLabel.setText("Comment");
	        commentLabel.setBounds(440, 33, 60, 23);
	        centerPanel.add(nameLabel, null);
	        centerPanel.add(surfaceLabel, null);
			centerPanel.add(marksLabel, null);
			centerPanel.add(commentLabel, null);
			centerPanel.add(getNameTextField(), null);
			centerPanel.add(getMarksTextField(), null);
			centerPanel.add(getCommentTextField(), null);
			centerPanel.add(getSurfaceComboBox(), null);
			centerPanel.add(getRadiusStartSlider(), null);
			centerPanel.add(getRadiusEndSlider(), null);
			centerPanel.add(getArcSlider(), null);
			centerPanel.add(getLengthSlider(), null);
			centerPanel.add(getGradeSlider(), null);
			centerPanel.add(getStartTangentSlider(), null);
			centerPanel.add(getBankingStartSlider(), null);
			centerPanel.add(getBankingEndSlider(), null);
			centerPanel.add(getEndTangentSlider(), null);
			centerPanel.add(getHeightStartLeftSlider(), null);
			centerPanel.add(getHeightStartRightSlider(), null);
			centerPanel.add(getHeightEndLeftSlider(), null);
			centerPanel.add(getHeightEndRightSlider(), null);
			centerPanel.add(getGroupButton(), null);
			centerPanel.add(getProfileButton(), null);
			centerPanel.add(getProfileStepsSlider(), null);
			centerPanel.add(getProfileStepsLengthSlider(), null);
		}
		return centerPanel;
	}
	/**
	 * This method initializes radiusStartSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getRadiusStartSlider()
	{
		if (radiusStartSlider == null)
		{
			radiusStartSlider = new SegmentSlider();
			radiusStartSlider.setBounds(115, 64, 50, 390);
			radiusStartSlider.setSection("Radius");
			radiusStartSlider.setAttr("Start");
			radiusStartSlider.setMin(1);
			radiusStartSlider.setMax(1000);
			radiusStartSlider.setExtent(10);
			radiusStartSlider.setTickSpacing(1);
			radiusStartSlider.setRealToTextCoeff(1);
			radiusStartSlider.addSliderListener(this);
			if (!shape.getType().equals("str"))
			{
				radiusStartSlider.setMethod("RadiusStart");
			}
		}
		return radiusStartSlider;
	}
	/**
	 * This method initializes radiusEndSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getRadiusEndSlider()
	{
		if (radiusEndSlider == null)
		{
			radiusEndSlider = new SegmentSlider();
			radiusEndSlider.setBounds(170, 64, 50, 390);
			radiusEndSlider.setSection("Radius");
			radiusEndSlider.setAttr("End");
			radiusEndSlider.setMin(1);
			radiusEndSlider.setMax(1000);
			radiusEndSlider.setExtent(10);
			radiusEndSlider.setTickSpacing(1);
			radiusEndSlider.setRealToTextCoeff(1);
			radiusEndSlider.addSliderListener(this);
			if (!shape.getType().equals("str"))
			{
				radiusEndSlider.setMethod("RadiusEnd");
			}
		}
		return radiusEndSlider;
	}
	/**
	 * This method initializes arcSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getArcSlider()
	{
		if (arcSlider == null)
		{
			arcSlider = new SegmentSlider();
			arcSlider.setBounds(60, 64, 50, 390);
			arcSlider.setSection("Arc");
			arcSlider.setAttr("");
			arcSlider.setMin(1);
			arcSlider.setMax(360);
			arcSlider.setExtent(10);
			arcSlider.setTickSpacing(1);
			arcSlider.setRealToTextCoeff(1);
			if (!shape.getType().equals("str"))
			{
				arcSlider.setMethod("ArcDeg");
			}
			arcSlider.addSliderListener(this);
		}
		return arcSlider;
	}
	/**
	 * This method initializes lengthSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getLengthSlider()
	{
		if (lengthSlider == null)
		{
			lengthSlider = new SegmentSlider();
			lengthSlider.setBounds(5, 64, 50, 390);
			lengthSlider.setSection("Length");
			lengthSlider.setAttr("");
			lengthSlider.setMin(0.001);
			lengthSlider.setMax(1000);
			lengthSlider.setExtent(1.0);
			lengthSlider.setTickSpacing(0.1);
			lengthSlider.setRealToTextCoeff(1);
			lengthSlider.setMethod("Length");
			lengthSlider.setValue(shape.getLength());
			lengthSlider.addSliderListener(this);
		}
		return lengthSlider;
	}
	/**
	 * This method initializes gradeSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getGradeSlider()
	{
		if (gradeSlider == null)
		{
			gradeSlider = new SegmentSlider();
			gradeSlider.setBounds(225, 64, 50, 390);
			gradeSlider.setSection("Grade");
			gradeSlider.setAttr("");
			gradeSlider.setMin(-45);
			gradeSlider.setMax(45);
			gradeSlider.setExtent(2);
			gradeSlider.setTickSpacing(0.5);
			gradeSlider.setRealToTextCoeff(1);
			gradeSlider.setMethod("Grade");
			gradeSlider.setValue(shape.getGrade());
			gradeSlider.setOptional(true);
			gradeSlider.addSliderListener(this);
		}
		return gradeSlider;
	}
	/**
	 * This method initializes startTangentSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getStartTangentSlider()
	{
		if (startTangentSlider == null)
		{
			startTangentSlider = new SegmentSlider();
			startTangentSlider.setBounds(280, 64, 50, 390);
			startTangentSlider.setSection("Tangent");
			startTangentSlider.setAttr("Start");
			startTangentSlider.setMin(-45);
			startTangentSlider.setMax(45);
			startTangentSlider.setExtent(2);
			startTangentSlider.setTickSpacing(0.5);
			startTangentSlider.setRealToTextCoeff(1);
			startTangentSlider.setMethod("ProfilStartTangent");
			startTangentSlider.setValue(shape.getProfilStartTangent());
			startTangentSlider.setOptional(true);
			startTangentSlider.addSliderListener(this);
		}
		return startTangentSlider;
	}
	/**
	 * This method initializes endTangentSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getEndTangentSlider()
	{
		if (endTangentSlider == null)
		{
			endTangentSlider = new SegmentSlider();
			endTangentSlider.setBounds(335, 64, 50, 390);
			endTangentSlider.setSection("Tangent");
			endTangentSlider.setAttr("End");
			endTangentSlider.setMin(-45);
			endTangentSlider.setMax(45);
			endTangentSlider.setExtent(2);
			endTangentSlider.setTickSpacing(0.5);
			endTangentSlider.setRealToTextCoeff(1);
			endTangentSlider.setMethod("ProfilEndTangent");
			endTangentSlider.setValue(shape.getProfilEndTangent());
			endTangentSlider.setOptional(true);
			endTangentSlider.addSliderListener(this);
		}
		return endTangentSlider;
	}
	/**
	 * This method initializes profilStepsSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getProfileStepsSlider()
	{
		if (profilStepsSlider == null)
		{
			profilStepsSlider = new SegmentSlider();
			profilStepsSlider.setBounds(390, 64, 50, 390);
			profilStepsSlider.setSection("Steps");
			profilStepsSlider.setAttr("");
			profilStepsSlider.setMin(0);
			profilStepsSlider.setMax(100);
			profilStepsSlider.setExtent(2);
			profilStepsSlider.setTickSpacing(0.5);
			profilStepsSlider.setRealToTextCoeff(1);
			profilStepsSlider.setMethod("ProfilSteps");
			profilStepsSlider.setOptional(true);
			profilStepsSlider.addSliderListener(this);
		}
		return profilStepsSlider;
	}
	/**
	 * This method initializes profilStepsLengthSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getProfileStepsLengthSlider()
	{
		if (profilStepsLengthSlider == null)
		{
			profilStepsLengthSlider = new SegmentSlider();
			profilStepsLengthSlider.setBounds(445, 64, 50, 390);
			profilStepsLengthSlider.setSection("Steps");
			profilStepsLengthSlider.setAttr("Len");
			profilStepsLengthSlider.setMin(0);
			profilStepsLengthSlider.setMax(100);
			profilStepsLengthSlider.setExtent(2);
			profilStepsLengthSlider.setTickSpacing(0.5);
			profilStepsLengthSlider.setRealToTextCoeff(1);
			profilStepsLengthSlider.setMethod("ProfilStepsLength");
			profilStepsLengthSlider.setOptional(true);
			profilStepsLengthSlider.addSliderListener(this);
		}
		return profilStepsLengthSlider;
	}
	/**
	 * This method initializes bankingStartSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getBankingStartSlider()
	{
		if (bankingStartSlider == null)
		{
			bankingStartSlider = new SegmentSlider();
			bankingStartSlider.setBounds(500, 64, 50, 390);
			bankingStartSlider.setSection("Banking");
			bankingStartSlider.setAttr("Start");
			bankingStartSlider.setMin(-45);
			bankingStartSlider.setMax(45);
			bankingStartSlider.setExtent(2);
			bankingStartSlider.setTickSpacing(0.5);
			bankingStartSlider.setRealToTextCoeff(1);
			bankingStartSlider.setMethod("BankingStart");
			bankingStartSlider.setValue(shape.getBankingStart());
			bankingStartSlider.setOptional(true);
			bankingStartSlider.addSliderListener(this);
		}
		return bankingStartSlider;
	}
	/**
	 * This method initializes bankingEndSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getBankingEndSlider()
	{
		if (bankingEndSlider == null)
		{
			bankingEndSlider = new SegmentSlider();
			bankingEndSlider.setBounds(555, 64, 50, 390);
			bankingEndSlider.setSection("Banking");
			bankingEndSlider.setAttr("End");
			bankingEndSlider.setMin(-45);
			bankingEndSlider.setMax(45);
			bankingEndSlider.setExtent(2);
			bankingEndSlider.setTickSpacing(0.5);
			bankingEndSlider.setRealToTextCoeff(1);
			bankingEndSlider.setMethod("BankingEnd");
			bankingEndSlider.setValue(shape.getBankingEnd());
			bankingEndSlider.setOptional(true);
			bankingEndSlider.addSliderListener(this);
		}
		return bankingEndSlider;
	}
	/**
	 * This method initializes heightStartLeftSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getHeightStartLeftSlider()
	{
		if (heightStartLeftSlider == null)
		{
			heightStartLeftSlider = new SegmentSlider();
			heightStartLeftSlider.setBounds(610, 64, 50, 390);
			heightStartLeftSlider.setSection("L Height");
			heightStartLeftSlider.setAttr("Start");
			heightStartLeftSlider.setMin(0);
			heightStartLeftSlider.setMax(200);
			heightStartLeftSlider.setExtent(2);
			heightStartLeftSlider.setTickSpacing(0.5);
			heightStartLeftSlider.setRealToTextCoeff(1);
			heightStartLeftSlider.setMethod("HeightStartLeft");
			heightStartLeftSlider.setOptional(true);
			heightStartLeftSlider.addSliderListener(this);
		}
		return heightStartLeftSlider;
	}
	/**
	 * This method initializes heightStartRightSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getHeightStartRightSlider()
	{
		if (heightStartRightSlider == null)
		{
			heightStartRightSlider = new SegmentSlider();
			heightStartRightSlider.setBounds(665, 64, 50, 390);
			heightStartRightSlider.setSection("R Height");
			heightStartRightSlider.setAttr("Start");
			heightStartRightSlider.setMin(0);
			heightStartRightSlider.setMax(200);
			heightStartRightSlider.setExtent(2);
			heightStartRightSlider.setTickSpacing(0.5);
			heightStartRightSlider.setRealToTextCoeff(1);
			heightStartRightSlider.setMethod("HeightStartRight");
			heightStartRightSlider.setOptional(true);
			heightStartRightSlider.addSliderListener(this);
		}
		return heightStartRightSlider;
	}
	/**
	 * This method initializes heightEndLeftSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getHeightEndLeftSlider()
	{
		if (heightEndLeftSlider == null)
		{
			heightEndLeftSlider = new SegmentSlider();
			heightEndLeftSlider.setBounds(720, 64, 50, 390);
			heightEndLeftSlider.setSection("L Height");
			heightEndLeftSlider.setAttr("End");
			heightEndLeftSlider.setMin(0);
			heightEndLeftSlider.setMax(200);
			heightEndLeftSlider.setExtent(2);
			heightEndLeftSlider.setTickSpacing(0.5);
			heightEndLeftSlider.setRealToTextCoeff(1);
			heightEndLeftSlider.setMethod("HeightEndLeft");
			heightEndLeftSlider.setOptional(true);
			heightEndLeftSlider.addSliderListener(this);
		}
		return heightEndLeftSlider;
	}
	/**
	 * This method initializes heightEndRightSlider
	 *
	 * @return gui.SegmentSlider
	 */
	private SegmentSlider getHeightEndRightSlider()
	{
		if (heightEndRightSlider == null)
		{
			heightEndRightSlider = new SegmentSlider();
			heightEndRightSlider.setBounds(775, 64, 50, 390);
			heightEndRightSlider.setSection("R Height");
			heightEndRightSlider.setAttr("End");
			heightEndRightSlider.setMin(0);
			heightEndRightSlider.setMax(200);
			heightEndRightSlider.setExtent(2);
			heightEndRightSlider.setTickSpacing(0.5);
			heightEndRightSlider.setRealToTextCoeff(1);
			heightEndRightSlider.setMethod("HeightEndRight");
			heightEndRightSlider.setOptional(true);
			heightEndRightSlider.addSliderListener(this);
		}
		return heightEndRightSlider;
	}

	/**
	 * This method initializes nameTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getNameTextField()
	{
		if (nameTextField == null)
		{
			nameTextField = new JTextField();
			nameTextField.setBounds(75, 5, 180, 23);
			nameTextField.setText(shape.getName());
			nameTextField.getDocument().addDocumentListener(new DocumentListener()
			{
				public void removeUpdate(DocumentEvent e)
				{
					shape.setName(nameTextField.getText());
					updateTitle();
				}
				public void insertUpdate(DocumentEvent e)
				{
					shape.setName(nameTextField.getText());
					updateTitle();
				}
				public void changedUpdate(DocumentEvent e)
				{
				}
			});
		}
		return nameTextField;
	}
	/**
	 * This method initializes marksTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMarksTextField()
	{
		if (marksTextField == null)
		{
			marksTextField = new JTextField();
			marksTextField.setBounds(510, 5, 315, 23);
			marksTextField.addKeyListener(new KeyAdapter()
			{
				public void keyReleased(KeyEvent e)
				{
					if (!shape.getType().equals("str"))
					{
						((Curve)shape).setMarks(marksTextField.getText());
					}
				}
			});
		}
		return marksTextField;
	}
	/**
	 * This method initializes commentTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCommentTextField()
	{
		if (commentTextField == null)
		{
			commentTextField = new JTextField();
			commentTextField.setBounds(510, 33, 315, 23);
			commentTextField.addKeyListener(new KeyAdapter()
			{
				public void keyReleased(KeyEvent e)
				{
					shape.setComment(commentTextField.getText());
				}
			});
		}
		return commentTextField;
	}
	/**
	 * This method initializes surfaceComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<String> getSurfaceComboBox()
	{
		if (surfaceComboBox == null)
		{
			surfaceComboBox = new SurfaceComboBox(editorFrame, roadSurfaceVector);
			surfaceComboBox.setBounds(75, 33, 180, 23);
			surfaceComboBox.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					shape.setSurface((String) surfaceComboBox.getSelectedItem());
					try
					{
						view.redrawCircuit();
					} catch (Exception e1)
					{
						e1.printStackTrace();
					}
					editorFrame.documentIsModified = true;
				}
			});

		}
		return surfaceComboBox;
	}

	public void updateTitle()
	{
		this.setTitle("Segment " + shape.getCount() + " : " + shape.getName());
	}

	public void setShape(Segment shape)
	{
		this.shape = shape;
		addSurface(roadSurfaceVector, shape.getSurface());
		this.getRightPanel().setSide(shape.getRight());
		this.getLeftPanel().setSide(shape.getLeft());

		// update all fields

		try
		{
			if (!shape.getType().equals("str"))
			{
				Curve curve = (Curve)shape;
				this.getRadiusStartSlider().setEnabled(true);
				this.getRadiusEndSlider().setEnabled(true);
				this.getArcSlider().setEnabled(true);
				this.getLengthSlider().setEnabled(false);
				this.getLengthSlider().setValue(curve.getLength());
				getGroupButton().setEnabled(true);
				getGroupButton().setSelected(curve.getType());

				this.getArcSlider().setValue(curve.getArcDeg());
				this.getRadiusStartSlider().setValue(curve.getRadiusStart());
				this.getRadiusEndSlider().setValue(curve.getRadiusEnd());
				this.getMarksTextField().setEnabled(true);
				this.getMarksTextField().setText(curve.getMarks());

			} else
			{
				this.getRadiusStartSlider().setEnabled(false);
				this.getRadiusEndSlider().setEnabled(false);
				this.getArcSlider().setEnabled(false);
				this.getLengthSlider().setEnabled(true);
				getGroupButton().setEnabled(false);

				this.getLengthSlider().setValue(shape.getLength());
				this.getMarksTextField().setEnabled(false);
				this.getMarksTextField().setText("");
			}
			getNameTextField().setText(shape.getName());
			getCommentTextField().setText(shape.getComment());

			this.updateTitle();

			// add this surface if it's not found in default list
			String surface = shape.getSurface();
			if (surface != null)
			{
				boolean found = false;
				for (int i = 0; i < roadSurfaceVector.size(); i++)
				{
					if (roadSurfaceVector.elementAt(i).equals(surface))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					roadSurfaceVector.add(surface);
					Collections.sort(roadSurfaceVector);
				}
			}

			if (shape.getSurface() != null)
				getSurfaceComboBox().setSelectedItem(shape.getSurface());
			else
				getSurfaceComboBox().setSelectedIndex(-1);
			
			this.getGradeSlider().setValue(shape.getGrade());
			this.getStartTangentSlider().setValue(shape.getProfilStartTangent());
			this.getEndTangentSlider().setValue(shape.getProfilEndTangent());
			this.getBankingStartSlider().setValue(shape.getBankingStart());
			this.getBankingEndSlider().setValue(shape.getBankingEnd());
			this.getHeightStartLeftSlider().setValue(shape.getHeightStartLeft());
			this.getHeightStartRightSlider().setValue(shape.getHeightStartRight());
			this.getHeightEndLeftSlider().setValue(shape.getHeightEndLeft());
			this.getHeightEndRightSlider().setValue(shape.getHeightEndRight());
			this.getProfileStepsSlider().setValue(shape.getProfilSteps());
			this.getProfileStepsLengthSlider().setValue(shape.getProfilStepsLength());
			if (shape.getProfil() == null || shape.getProfil().isEmpty())
				getProfileButton().setSelected("none");
			else
				getProfileButton().setSelected(shape.getProfil());

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		this.validate();
		this.repaint();
	}

	public void update()
	{
		try
		{
			view.redrawCircuit();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		editorFrame.documentIsModified = true;
		dirty = true;
	}
	/**
	 * This method initializes rightPanel
	 *
	 * @return gui.segment.SegmentSideProperties
	 */
	private SegmentSideProperties getRightPanel()
	{
		if (rightPanel == null)
		{
			rightPanel = new SegmentSideProperties(this, shape.getRight());
			rightPanel.setTitle("Right");
		}
		return rightPanel;
	}
	/**
	 * This method initializes leftPanel
	 *
	 * @return gui.segment.SegmentSideProperties
	 */
	private SegmentSideProperties getLeftPanel()
	{
		if (leftPanel == null)
		{
			leftPanel = new SegmentSideProperties(this, shape.getLeft());
			leftPanel.setTitle("Left");
		}
		return leftPanel;
	}

	/**
	 *
	 */
	public void sideChanged()
	{
		view.redrawCircuit();
		editorFrame.documentIsModified = true;
		dirty = true;
	}
	/**
	 * This method initializes groupButton
	 *
	 * @return gui.segment.GroupButton
	 */
	private GroupButton getGroupButton()
	{
		if (groupButton == null)
		{
			groupButton = new GroupButton();
			groupButton.setBounds(270, 2, 70, 33);
			groupButton.setParent(this);
		}
		return groupButton;
	}

	public void groupChanged()
	{
		shape.setType(getGroupButton().getSelected());
		editorFrame.documentIsModified = true;
		view.redrawCircuit();
	}

	/**
	 * This method initializes profileButton
	 *
	 * @return gui.segment.ProfileButton
	 */
	private ProfileButton getProfileButton()
	{
		if (profileButton == null)
		{
			profileButton = new ProfileButton();
			profileButton.setBounds(350, 2, 70, 49);
			profileButton.setParent(this);
		}
		return profileButton;
	}

	public void profileChanged()
	{
		if (getProfileButton().getSelected().isEmpty() || getProfileButton().getSelected() == "none")
			shape.setProfil("");
		else
			shape.setProfil(getProfileButton().getSelected());
		editorFrame.documentIsModified = true;
	}

	public void windowClosing(WindowEvent anEvent)
	{
		System.out.println("JDialog is closing");
	}


	/* (non-Javadoc)
	 * @see gui.segment.SliderListener#valueChanged(gui.segment.SegmentSlider)
	 */
	public void sliderChanged(SegmentSlider slider)
	{
		// TODO I don't know if this is the best way to fix this but it works
		if (slider.getMethod() == null || slider.getMethod().isEmpty())
			return;

		Interpreter line = new Interpreter();
		String command = "";

		try
		{
			line.set("shape", shape);

			String method = slider.getMethod();

			if (Double.isNaN(slider.getValue()))
				command = "shape.set" + method + "(Double.NaN)";
			else
				command = "shape.set" + method + "(" + slider.getValue() + ")";

			line.eval(command);
			shape = (Segment) line.get("shape");
		} catch (EvalError e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double length = shape.getLength();
		view.redrawCircuit();
		double newLength = shape.getLength();
		if (length != newLength)
		{
			getLengthSlider().setValue(newLength);
		}
		editorFrame.documentIsModified = true;
		dirty = true;
	}

	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			exit();
			view.segmentParamDialog = null;
		}
	}

	private void exit()
	{
		editorFrame.getProject().setSegmentEditorX(this.getX());
		editorFrame.getProject().setSegmentEditorY(this.getY());
	}
} //  @jve:decl-index=0:visual-constraint="10,10"

