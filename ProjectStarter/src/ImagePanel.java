import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ImagePanel extends JPanel implements MouseMotionListener {
	private Image img;
	private Point firstPoint;

	public ImagePanel(Rectangle r) {
		Dimension size = new Dimension(800, 600);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);

		firstPoint = new Point();

		addMouseMotionListener(this);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				firstPoint.setLocation(e.getX(), e.getY());
			}
		});
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void setImage(Image img) {
		this.img = img;
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, null);
	}
}