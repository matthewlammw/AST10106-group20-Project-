import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class ImageProcessorApp {
	public static void main(String[] args) {
		String str = "AST10106 Image Processing Assignment";
		ImageFrame iFrameOriginal = new ImageFrame(str);
		iFrameOriginal.showWin();
		iFrameOriginal.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
}