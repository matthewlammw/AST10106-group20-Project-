import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImageFrame extends JFrame implements ActionListener, ChangeListener, MouseMotionListener, MouseListener {
	private JDesktopPane theDesktop;
	private JInternalFrame frameOriginal;
	private JInternalFrame frameProcessed;
	private JPanel toolBarPanel;

	// ------------------------------------------

	private JToolBar functionToolBar;
	private JButton openButton;
	private JButton saveButton;
	private JButton undoButton;
	private JButton redoButton;

	// ------------------------------------------

	private JToolBar toolbar;
	private JSlider rgRange;
	private JSlider gbRange;
	private JSlider brRange;
	private int rgRangeValue;
	private int gbRangeValue;
	private int brRangeValue;
	private JLabel xyVal;
	private JLabel redVal;
	private JLabel greenVal;
	private JLabel blueVal;
	
	// ------------------------------------------

	private ImagePanel imagePanelOriginal;
	private ImagePanel imagePanelProcessed;
	private boolean[][] mask;
	private JLabel infoBar;

	// ------------------------------------------

	private JMenuBar menuBar;;
	private JMenu menuFile;
	private JMenu menuEdit;
	private JMenu menuEffects;
	private JMenu menuLookAndFeel;
	private JMenu menuHelp;

	// ------------------------------------------

	private JMenuItem menuItemOpen;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemExit;
	private JMenuItem menuItemUndo;
	private JMenuItem menuItemRedo;
	private JMenuItem menuItemGrayscale;
	private JMenuItem menuItemInvert;
	private JMenuItem menuItemBrightness;
	private JMenuItem menuItemBlurImage;
	private JMenuItem menuItemSwirlImage;
	private JMenuItem menuItemRotateImage;
	private JMenuItem menuItemScaleImage;
	private JMenuItem menuItemPreserveColor;
	private JMenuItem menuItemDetectEdge;
	private JMenuItem menuItemcompressImage;

	private JRadioButtonMenuItem menuItemMetal;
	private JRadioButtonMenuItem menuItemMotif;
	private JRadioButtonMenuItem menuItemWindows;

	private JMenuItem menuItemAbout;

	// ------------------------------------------

	private BufferedImage inputImage;
	private BufferedImage resultImage;
	private String title;
	private Vector<Image> imageBuffer;		// for undo or redo
	private Vector<String> messageBuffer;	// for undo or redo
	private int curImageIndex;

	private Rectangle rect;
	private final int UNDO_BUFFER_SIZE = 10;

	public ImageFrame(String title) {
		theDesktop = new JDesktopPane(); // create desktop pane
		add(theDesktop); // add desktop pane to frame

		toolBarPanel = new JPanel();

		functionToolBar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
		openButton = new JButton();
		openButton.setIcon(new ImageIcon(ImageFrame.class.getResource("/com/sun/java/swing/plaf/windows/icons/TreeOpen.gif")));
		openButton.addActionListener(this);
		
		saveButton = new JButton();
		saveButton.setIcon(new ImageIcon(ImageFrame.class.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		saveButton.addActionListener(this);
		
		undoButton = new JButton();
		undoButton.setIcon(new ImageIcon(ImageFrame.class.getResource("/com/sun/javafx/scene/web/skin/Undo_16x16_JFX.png")));
		undoButton.addActionListener(this);
		
		redoButton = new JButton();
		redoButton.setIcon(new ImageIcon(ImageFrame.class.getResource("/com/sun/javafx/scene/web/skin/Redo_16x16_JFX.png")));	
		redoButton.addActionListener(this);
		
		functionToolBar.add(openButton);
		functionToolBar.add(saveButton);
		functionToolBar.add(new JToolBar.Separator());
		functionToolBar.add(undoButton);
		functionToolBar.add(redoButton);

		toolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);

		//toolBarPanel.setLayout(new GridLayout(0, 1));
		toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));
		toolBarPanel.add(functionToolBar);
		toolBarPanel.add(toolbar);
		toolbar.setVisible(false);
		getContentPane().add(toolBarPanel, BorderLayout.NORTH);

		rgRange = new JSlider(JSlider.HORIZONTAL, 0, 80, 40);
		rgRange.addChangeListener(this);

		gbRange = new JSlider(JSlider.HORIZONTAL, 0, 80, 40);
		gbRange.addChangeListener(this);
		
		brRange = new JSlider(JSlider.HORIZONTAL, 0, 80, 40);
		brRange.addChangeListener(this);

		xyVal = new JLabel("");
		redVal = new JLabel("");
		greenVal = new JLabel("");
		blueVal = new JLabel("");
		
		toolbar.add(new JLabel("Red-Green"));
		toolbar.add(rgRange);
		toolbar.add(new JToolBar.Separator());
		toolbar.add(new JLabel("Green-Blue"));
		toolbar.add(gbRange);
		toolbar.add(new JToolBar.Separator());
		toolbar.add(new JLabel("Blue-Red"));
		toolbar.add(brRange);
		toolbar.add(new JToolBar.Separator());

		xyVal.setPreferredSize(new Dimension(80, 0));
		redVal.setPreferredSize(new Dimension(32, 0));
		greenVal.setPreferredSize(new Dimension(32, 0));
		blueVal.setPreferredSize(new Dimension(32, 0));
		
		toolbar.add(new JLabel("(x,y): "));
		toolbar.add(xyVal);
		toolbar.add(new JLabel("(R,G,B): "));
		toolbar.add(redVal);
		toolbar.add(greenVal);
		toolbar.add(blueVal);
		
		this.title = title;
		menuBar = new JMenuBar();

		menuFile = new JMenu("File");
		menuItemOpen = new JMenuItem("Open");
		menuItemOpen.addActionListener(this);
		menuItemSave = new JMenuItem("Save as..");
		menuItemSave.addActionListener(this);
		menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener(this);
		menuFile.add(menuItemOpen);
		menuFile.add(menuItemSave);
		menuFile.addSeparator();
		menuFile.add(menuItemExit);
		menuItemSave.setEnabled(false);

		menuEdit = new JMenu("Edit");
		menuItemUndo = new JMenuItem("Undo");
		menuItemRedo = new JMenuItem("Redo");
		menuItemUndo.addActionListener(this);
		menuItemRedo.addActionListener(this);
		menuEdit.add(menuItemUndo);
		menuEdit.add(menuItemRedo);
		menuItemUndo.setEnabled(false);
		menuItemRedo.setEnabled(false);

		menuEffects = new JMenu("Effects");
		menuItemGrayscale = new JMenuItem("Turn to Grayscale");
		menuItemInvert = new JMenuItem("Invert Color");
		menuItemBrightness = new JMenuItem("Adjust Brightness");

		menuItemBlurImage = new JMenuItem("Blur Image");
		menuItemSwirlImage = new JMenuItem("Swirl Image");
		menuItemRotateImage = new JMenuItem("Rotate Image");
		menuItemScaleImage = new JMenuItem("Scale Image");
		menuItemPreserveColor = new JMenuItem("Preserve Partial Colors");
		menuItemDetectEdge = new JMenuItem("Detect Edges");
		menuItemcompressImage = new JMenuItem("Monalisa");
		menuItemGrayscale.addActionListener(this);
		menuItemInvert.addActionListener(this);
		menuItemBrightness.addActionListener(this);
		menuItemBlurImage.addActionListener(this);
		menuItemSwirlImage.addActionListener(this);
		menuItemRotateImage.addActionListener(this);
		menuItemScaleImage.addActionListener(this);
		menuItemPreserveColor.addActionListener(this);
		menuItemDetectEdge.addActionListener(this);
		menuItemcompressImage.addActionListener(this);
		
		menuEffects.add(menuItemGrayscale);
		menuEffects.add(menuItemInvert);
		menuEffects.add(menuItemBrightness);
		menuEffects.add(menuItemBlurImage);
		menuEffects.add(menuItemSwirlImage);
		menuEffects.add(menuItemRotateImage);
		menuEffects.add(menuItemScaleImage);
		menuEffects.add(menuItemPreserveColor);
		menuEffects.add(menuItemDetectEdge);
		menuEffects.add(menuItemcompressImage);
		
		menuItemGrayscale.setEnabled(false);
		menuItemInvert.setEnabled(false);
		menuItemBrightness.setEnabled(false);
		menuItemBlurImage.setEnabled(false);
		menuItemSwirlImage.setEnabled(false);
		menuItemRotateImage.setEnabled(false);
		menuItemScaleImage.setEnabled(false);
		menuItemPreserveColor.setEnabled(false);
		menuItemDetectEdge.setEnabled(false);
		menuItemcompressImage.setEnabled(false);
		menuEffects.setEnabled(false);

		menuLookAndFeel = new JMenu("Look and Feel");
		menuItemMetal = new JRadioButtonMenuItem("Metal");
		menuItemMotif = new JRadioButtonMenuItem("Motif");
		menuItemWindows = new JRadioButtonMenuItem("Windows");
		menuItemMetal.addActionListener(this);
		menuItemMotif.addActionListener(this);
		menuItemWindows.addActionListener(this);
		menuLookAndFeel.add(menuItemMetal);
		menuLookAndFeel.add(menuItemMotif);
		menuLookAndFeel.add(menuItemWindows);
		ButtonGroup lookAndFeelGroup = new ButtonGroup();
		lookAndFeelGroup.add(menuItemMetal);
		lookAndFeelGroup.add(menuItemMotif);
		lookAndFeelGroup.add(menuItemWindows);
		menuItemMetal.setSelected(true);

		menuHelp = new JMenu("Help");
		menuItemAbout = new JMenuItem("About");
		menuItemAbout.addActionListener(this);
		menuHelp.add(menuItemAbout);

		infoBar = new JLabel(" Info.:");
		infoBar.setFont(new Font("Arial", Font.PLAIN, 12));

		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		menuBar.add(menuEffects);
		menuBar.add(menuLookAndFeel);
		menuBar.add(menuHelp);

		imagePanelOriginal = new ImagePanel(rect);
		imagePanelOriginal.addMouseMotionListener(this);
		imagePanelOriginal.addMouseListener(this);

		imagePanelProcessed = new ImagePanel(rect);
		getContentPane().add(infoBar, BorderLayout.SOUTH);
		setIconImage(new ImageIcon("app-icon.png").getImage());
		
		inputImage = null;
		resultImage = null;
		mask = null;

		rgRangeValue = 40;
		brRangeValue = 40;
		gbRangeValue = 40;
	}

	public void showWin() {
		setSize(800, 600); // set frame size
		setTitle(title);
		setJMenuBar(menuBar);
		setLocationRelativeTo(null);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public Image getInputImage() {
		return inputImage;
	}

	public Image getResultImage() {
		return resultImage;
	}

	private void imageFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open a file");
		fileChooser.addChoosableFileFilter(new FileExtensionFilter("jpg"));

		int returnVal = fileChooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();

			try {
				inputImage = ImageIO.read(file);
				resultImage = ImageIO.read(file);
				imagePanelOriginal.setImage(inputImage);
				imagePanelProcessed.setImage(resultImage);

				// ---------------------------

				frameOriginal = new JInternalFrame("Original Image", true, true, true, true);
				frameOriginal.add(imagePanelOriginal, BorderLayout.CENTER);
				frameOriginal.pack();
				theDesktop.add(frameOriginal);
				frameOriginal.setVisible(true);

				frameProcessed = new JInternalFrame("Processed Image", true, true, true, true);
				frameProcessed.add(imagePanelProcessed, BorderLayout.CENTER);
				frameProcessed.pack();
				theDesktop.add(frameProcessed);
			
				JInternalFrame[] frames = theDesktop.getAllFrames();
				int x = 10;
				int y = 10;
				for (int i = frames.length - 1; i >= 0; i--) {
					frames[i].setLocation(x, y);
					x += 30;
					y += 30;
				}

				getContentPane().add(theDesktop);
				
				// ---------------------------

				imageBuffer = new Vector<Image>();
				imageBuffer.add(inputImage);
				messageBuffer = new Vector<String>();
				curImageIndex = 0;
				menuItemSave.setEnabled(true);
				menuItemUndo.setEnabled(true);
				menuItemRedo.setEnabled(true);
				menuItemGrayscale.setEnabled(true);
				menuItemInvert.setEnabled(true);
				menuItemBrightness.setEnabled(true);
				menuItemBlurImage.setEnabled(true);
				menuItemSwirlImage.setEnabled(true);
				menuItemRotateImage.setEnabled(true);
				menuItemScaleImage.setEnabled(true);
				menuItemPreserveColor.setEnabled(true);
				menuItemDetectEdge.setEnabled(true);
				menuItemcompressImage.setEnabled(true);

				infoBar.setText(" Info.: Filename = " + file.getName() + ", Image's size = " + inputImage.getWidth()
						+ " x " + inputImage.getHeight());
				messageBuffer.add(infoBar.getText());

				menuEffects.setEnabled(true);
			} catch (IOException e) {
			}
		}
	}

	private void saveImage() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save file as ..");
		fileChooser.addChoosableFileFilter(new FileExtensionFilter("jpg"));

		int returnVal = fileChooser.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getPath() + ".jpg";

			if (filePath != null) {
				try {
					File file = new File(filePath);
					ImageIO.write(resultImage, "jpg", file);
				} 
				catch (IOException e) {
				}
			}
		}
	}

	private void undo() {
		if (curImageIndex > 0)
			curImageIndex--;
		imagePanelProcessed.setImage((Image) imageBuffer.elementAt(curImageIndex));
		infoBar.setText((String) messageBuffer.elementAt(curImageIndex));
	}

	private void redo() {
		if (curImageIndex < imageBuffer.size() - 1)
			curImageIndex++;
		imagePanelProcessed.setImage((Image) imageBuffer.elementAt(curImageIndex));
		infoBar.setText((String) messageBuffer.elementAt(curImageIndex));
	}

	private void addToUndoBuffer(Image image) {
		if (curImageIndex < imageBuffer.size() - 1) {
			int last = imageBuffer.size() - 1;
			for (int i = curImageIndex + 1; i <= last; i++) {
				imageBuffer.remove(imageBuffer.size() - 1);
				messageBuffer.remove(messageBuffer.size() - 1);
			}
		}
		if (imageBuffer.size() >= UNDO_BUFFER_SIZE) {
			imageBuffer.remove(0);
			messageBuffer.remove(0);
		}
		imageBuffer.add(image);
		messageBuffer.add(infoBar.getText());
		curImageIndex++;
	}

	private void grayscaleImage() {
		resultImage = ImageProcessor.convertToGrayScale(inputImage);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Color image is converted to grayscale");
		addToUndoBuffer(resultImage);	
		frameProcessed.setVisible(true);
	}

	private void invertImage() {
		resultImage = ImageProcessor.invertColor(inputImage);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Color image is inverted");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}

	private void adjustImageBightness() {
		String str = JOptionPane.showInputDialog(this, "Enter amount of brightness adjusted", "Input",
				JOptionPane.QUESTION_MESSAGE);
		int nBrightness = Integer.parseInt(str);
		resultImage = ImageProcessor.adjustBrightness(inputImage, nBrightness);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Brightness of color image is adjusted");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}

	private void blurImage() {
		String str = JOptionPane.showInputDialog(this, "Enter pixel offset", "Input", JOptionPane.QUESTION_MESSAGE);
		int offset = Integer.parseInt(str);
		resultImage = ImageProcessor.blur(inputImage, offset);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Image is blurred");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}

	private void swirlImage() {
		String str = JOptionPane.showInputDialog(this, "Enter degree of swirl", "Input", JOptionPane.QUESTION_MESSAGE);
		double deg = Double.parseDouble(str);
		resultImage = ImageProcessor.swirl(inputImage, deg);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Image is swirled");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}
	
	private void rotateImage() {
		String str = JOptionPane.showInputDialog(this, "Enter degree of rotation", "Input", JOptionPane.QUESTION_MESSAGE);
		double deg = Double.parseDouble(str);
		resultImage = ImageProcessor.rotate(inputImage, deg);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Image is rotated");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}
	
	private void scaleImage() {
		String w = JOptionPane.showInputDialog(this, "Enter new width", "Input", JOptionPane.QUESTION_MESSAGE);
		int width = Integer.parseInt(w);
		String h = JOptionPane.showInputDialog(this, "Enter new height", "Input", JOptionPane.QUESTION_MESSAGE);
		int height = Integer.parseInt(h);
		
		resultImage = ImageProcessor.scale(inputImage, width, height);
		imagePanelProcessed.setImage(resultImage);
		
		infoBar.setText(" Info.: Image is scaled");
		addToUndoBuffer(resultImage);
		frameProcessed.setSize(resultImage.getWidth(), resultImage.getHeight());
		frameProcessed.setVisible(true);
	}
	private void compressImage() {
		resultImage = ImageProcessor.compressImage(inputImage);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Color image is inverted");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}

	private void preservePartialColors() {
		// by default, all mask elements are initialized to false
		mask = new boolean[inputImage.getWidth()][inputImage.getHeight()];
		toolbar.setVisible(true);
		theDesktop.updateUI();
		
		resultImage = ImageProcessor.convertToGrayScale(inputImage);
		imagePanelProcessed.setImage(resultImage);
		
		frameProcessed.setVisible(true);
		infoBar.setText(" Info.: Click on the original image to preserve some color");
	}

	private void detectEdgesOfImage() {
		resultImage = ImageProcessor.detectEdges(inputImage);
		imagePanelProcessed.setImage(resultImage);
		infoBar.setText(" Info.: Edge detection is done");
		addToUndoBuffer(resultImage);
		frameProcessed.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == menuItemOpen || source == openButton)
			imageFileChooser();
		else if (source == menuItemSave || source == saveButton)
			saveImage();
		else if (source == menuItemUndo || source == undoButton)
			undo();
		else if (source == menuItemRedo || source == redoButton)
			redo();
		else if (source == menuItemExit)
			System.exit(0);
		else if (source == menuItemGrayscale)
			grayscaleImage();
		else if (source == menuItemInvert)
			invertImage();
		else if (source == menuItemBrightness)
			adjustImageBightness();
		else if (source == menuItemBlurImage)
			blurImage();
		else if (source == menuItemSwirlImage)
			swirlImage();
		else if (source == menuItemRotateImage)
			rotateImage();
		else if (source == menuItemScaleImage)
			scaleImage();
		else if (source == menuItemPreserveColor)
			preservePartialColors();
		else if (source == menuItemDetectEdge)
			detectEdgesOfImage();
		else if (source == menuItemAbout) {
			AboutDialog ad = new AboutDialog(this);
			ad.setVisible(true);
		} else {
			String str = "";
			if (source == menuItemMetal)
				str = "javax.swing.plaf.metal.MetalLookAndFeel";
			else if (source == menuItemMotif)
				str = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			else if (source == menuItemWindows)
				str = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

			try {
				UIManager.setLookAndFeel(str);
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Exception exception) {
			}
		}
		repaint();
	}

	public synchronized void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source == rgRange)
			rgRangeValue = rgRange.getValue();
		else if (source == gbRange)
			gbRangeValue = gbRange.getValue();
		else if (source == brRange)
			brRangeValue = brRange.getValue();
		System.out.println("State is changed: " + rgRangeValue + " " + brRangeValue + " " + gbRangeValue);
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		int val = inputImage.getRGB(e.getX(), e.getY());
		Color color = new Color(val);
		xyVal.setText(e.getX() + ", " + e.getY() + "   ");
		redVal.setText(color.getRed() + ",");
		greenVal.setText(color.getGreen() + ",");
		blueVal.setText(color.getBlue() + "");
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int val = inputImage.getRGB(e.getX(), e.getY());
			Color color = new Color(val);
			resultImage = ImageProcessor.preserveColor(inputImage, mask, color.getRGB(), 
					rgRangeValue, gbRangeValue, brRangeValue);
			imagePanelProcessed.setImage(resultImage);
			imagePanelProcessed.repaint();
			System.out.println("Mouse Right is pressed");
			infoBar.setText(" Info.: Some colors are preserved");
			addToUndoBuffer(resultImage);
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}
}