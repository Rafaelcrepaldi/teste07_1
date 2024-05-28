import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CameraPanel extends JPanel {
    private VideoCapture capture;
    private Mat frame;

    public CameraPanel() {
        capture = new VideoCapture(0);
        frame = new Mat();

        Thread cameraThread = new Thread(() -> {
            while (true) {
                if (capture.read(frame)) {
                    BufferedImage image = matToBufferedImage(frame);
                    SwingUtilities.invokeLater(() -> {
                        repaint();
                    });
                }
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (frame != null) {
            BufferedImage image = matToBufferedImage(frame);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }

   private BufferedImage matToBufferedImage(Mat matrix) {
    if (matrix != null && matrix.width() > 0 && matrix.height() > 0) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);

        switch (matrix.channels()) {
            case 1 -> type = BufferedImage.TYPE_BYTE_GRAY;
            case 3 -> {
                type = BufferedImage.TYPE_3BYTE_BGR;
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
            }
            default -> {
                return null;
            }
        }

        BufferedImage image = new BufferedImage(cols, rows, type);
        image.getRaster().setDataElements(0, 0, cols, rows, data);
        return image;
    } else {
        return null;
    }
}


}
