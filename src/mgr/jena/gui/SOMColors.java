package mgr.jena.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.encog.mathutil.matrices.Matrix;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodBubble;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodRBF;

import com.roots.map.MapPanel;

/**
 * A classic SOM example that shows how the SOM groups similar color shades.
 *
 */
public class SOMColors extends JFrame implements Runnable {

	private SOM network;
	private Thread thread;
	private BasicTrainSOM train;
	NeighborhoodBubble neighborhoodFunction;
	Point networkSize = new Point(4,4);
	private int initialRadius = 3;
	private int iterations = 1000; 
	private int endRadius = 1;
	private double initialLearningRate = 0.8;
	private double endLearningRate = 0.001;
	private int samplesCount = 100;
	
	private BufferedImage img;
	private DrawPanel dPanel;
	
	public SOMColors() {
		try {
		    img = ImageIO.read(new File("image.jpg"));
		} catch (IOException e) {
		}
		
		this.setSize(640, 480);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.network = createNetwork();
		dPanel = new DrawPanel();
		dPanel.setImage(img);
		this.getContentPane().add(dPanel);
		//this.gaussian = new NeighborhoodRBF(RBFEnum.Gaussian,MapPanel.WIDTH,
			//	MapPanel.HEIGHT);
		this.train = new BasicTrainSOM(this.network, 0.8, null, neighborhoodFunction);
		train.setForceWinner(false);
		this.thread = new Thread(this);
		thread.start();
	}

	public SOM getNetwork() {
		return this.network;
	}
	
	private int convertColor(double d)
    {
        double result = 128 * d;
        result += 128;
        result = Math.min(result, 255);
        result = Math.max(result, 0);
        return (int)result;
    }
	
	private double convertColor(int d)
    {
        d -= 128;
        double result = (double)d / 128.0;
        result = Math.min(result, 1.0);
        result = Math.max(result, -1.0);
        return result;
    }

	private SOM createNetwork() {
		neighborhoodFunction = new NeighborhoodBubble(initialRadius);
		SOM result = new SOM(3,networkSize.x * networkSize.y);
		result.reset();
		return result;
	}

	public static void main(String[] args) {
		SOMColors frame = new SOMColors();
		frame.setVisible(true);
	}

	public void run() {

		List<MLData> samples = new ArrayList<MLData>();
		for (int i = 0; i < samplesCount; i++) {
			MLData data = new BasicMLData(3);
			int x = RangeRandomizer.randomInt(0,img.getWidth()-1);
			int y = RangeRandomizer.randomInt(0,img.getHeight()-1);
			Color c = new Color(img.getRGB(x, y));
			data.setData(0, convertColor(c.getRed()));
			data.setData(1, convertColor(c.getGreen()));
			data.setData(2, convertColor(c.getBlue()));
			samples.add(data);
		}
		this.train.setAutoDecay(iterations, initialLearningRate, endLearningRate, initialRadius, endRadius);

		for (int i = 0; i < iterations; i++) {
			int idx = (int) (Math.random() * samples.size());
			MLData c = samples.get(idx);

			this.train.trainPattern(c);
			this.train.autoDecay();
			//this.dPanel.repaint();
			//System.out.println("Iteration " + i + "," + this.train.toString());
		}
		Matrix m = network.getWeights();
		for (int x = 0; x < img.getWidth(); x++)
        {
            for (int y = 0; y < img.getHeight(); y++)
            {
                Color c = new Color(img.getRGB(x, y));
                BasicNeuralData data = new BasicNeuralData(3);
                data.getData()[0] = convertColor(c.getRed());
                data.getData()[1] = convertColor(c.getGreen());
                data.getData()[2] = convertColor(c.getBlue());
                int winner = network.winner(data);
                
                MLData result = new BasicMLData(3);
                Matrix row = network.getWeights().getRow(winner);
                for (int i = 0; i < 3; i++)
                {
                    result.getData()[i] = row.get(0, i);
                }
                int r = convertColor(result.getData(0));
                int g = convertColor(result.getData(1));
                int b = convertColor(result.getData(2));
                Color newColor = new Color(r,g,b);
                img.setRGB(x, y, newColor.getRGB());
                //mReducedPictureBitmap.SetPixel(x, y, newColor);
            }
            dPanel.repaint();
        }
			
	}
	
	class DrawPanel extends JPanel {
	    BufferedImage image;
	    Dimension size = new Dimension();
	  
	    public DrawPanel() { }
	  
	    public DrawPanel(BufferedImage image) {
	        this.image = image;
	        setComponentSize();
	    }
	  
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        g.drawImage(image, 0, 0, this);
	    }
	  
	    public Dimension getPreferredSize() {
	        return size;
	    }
	  
	    public void setImage(BufferedImage bi) {
	        image = bi;
	        setComponentSize();
	        repaint();
	    }
	  
	    private void setComponentSize() {
	        if(image != null) {
	            size.width  = image.getWidth();
	            size.height = image.getHeight();
	            revalidate();  // signal parent/scrollpane
	        }
	    }
	}
}