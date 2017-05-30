/* Reedit Syed Shahriar
 * 4 April 2017 to present
 * VectorFieldGenerator
 * This program generates a vector field based on the function given through vectorfield.txt.
 * */

// import modules for window programming
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// import modules for processing and evaluation of expressions
import java.io.*;
import java.util.Scanner;
import javax.script.*;

public class VectorFieldGenerator {

	// instantiate objects for evaluation of expressions
	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private String xFunc,yFunc;
	
	public VectorFieldGenerator(String in){
		
		// extracts x and y functions from file
		xFunc = in.substring(3,in.lastIndexOf('=')-2);
		yFunc = in.substring(in.lastIndexOf('=')+1,in.length()-1);
		
		// makes new object for evaluating expressions
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");
		
	}
	
	public static void main(String[] args) throws ScriptException, FileNotFoundException {
		
		// reads input from file
		Scanner in = new Scanner(new File("vectorfield.txt"));
		String func = in.nextLine();
		in.close();
		
		// creates object based on function string, frame for output
		VectorFieldGenerator fs = new VectorFieldGenerator(func);
		VFFrame frame = fs.new VFFrame(fs);

	}
	
	public Vector vectorAt(double x,double y) throws ScriptException{
		
		// prepares function for evaluation
		String xCopy = xFunc.replace("x",x+"").replace("y",y+"");
		String yCopy = yFunc.replace("x",x+"").replace("y",y+"");
		
		// evaluates function
		String val = engine.eval(xCopy).toString();
		double xVal = Double.parseDouble(val);
		val = engine.eval(yCopy).toString();
		double yVal = Double.parseDouble(val);
		
		// outputs vector based on evaluated functions
		return new Vector(x,y,xVal,yVal);
		
	}
	
	class Vector{
		
		private double x,y; // vector location
		private double xVal,yVal; // vector length in each direction
		private Color color; // according color of vector based on norm
		
		public Vector(double xIn,double yIn,double xValIn,double yValIn){
			
			// color velocity - how fast color changes relative to norm of vector
			final byte COLOR_VEL = 10;
			
			// assigns given values
			x = xIn;
			y = yIn;
			xVal = xValIn;
			yVal = yValIn;
			
			// calculates color based on magnitude/norm of vector
			double norm = Math.sqrt(xVal*xVal+yVal*yVal);
			if (norm > 10) color = Color.getHSBColor(1.0F,1.0F,1.0F);
			else color = Color.getHSBColor((float)(COLOR_VEL-norm/COLOR_VEL),1.0F,1.0F);
			
		}
		
		public void draw(Graphics g){ // draws vector to graphics object
			
			// gets "slope" of vector
			double m = yVal/xVal;
			
			// length of vector as drawn on screen
			double l = 10;
			
			// gets old color
			Color old = g.getColor();
			
			// sets color according to norm
			g.setColor(color);
			
			// draws vector on screen
			g.drawLine((int)((x+24)*25),(int)(600-(25*(y+12))),(int)((x+24)*25-(int)(l/Math.sqrt(m*m+1))),
				(int)(600-(25*(y+12))+(int)((l*m)/Math.sqrt(m*m+1))));
			g.drawLine((int)((x+24)*25),(int)(600-(25*(y+12))),(int)((x+24)*25+(int)(l/Math.sqrt(m*m+1))),
				(int)(600-(25*(y+12))-(int)((l*m)/Math.sqrt(m*m+1))));
			
			// draws arrowhead of vector
			Point selectedPt = null;
			Point pt1 = new Point((int)((x+24)*25-(int)(l/Math.sqrt(m*m+1))),(int)(600-(25*(y+12))+(int)((l*m)/Math.sqrt(m*m+1))));
			Point pt2 = new Point((int)((x+24)*25+(int)(l/Math.sqrt(m*m+1))),(int)(600-(25*(y+12))-(int)((l*m)/Math.sqrt(m*m+1))));
			if (xVal < 0){
				if (pt1.x < pt2.x) selectedPt = pt1;
				else selectedPt = pt2;
			} else if (xVal > 0){
				if (pt1.x < pt2.x) selectedPt = pt2;
				else selectedPt = pt1;
			} else {
				if (yVal > 0){
					if (pt1.y < pt2.y) selectedPt = pt2;
					else selectedPt = pt1;
				} else {
					if (pt1.y < pt2.y) selectedPt = pt1;
					else selectedPt = pt2;
				}
			}
			g.fillRect(selectedPt.x,selectedPt.y,3,3);
			
			// restores old colors
			g.setColor(old);
			
		}
		
	}
	
	class VFPanel extends JPanel implements ActionListener{
		
		VectorFieldGenerator fs;
		private Particle[][] particles;
		private final byte SPARSITY = 4;
		private boolean showParticles;
		private int zoom;
		
		public VFPanel(VectorFieldGenerator fsIn){
			
			super();
			fs = fsIn;
			setBackground(Color.WHITE);
			Timer timer = new Timer(2,this);
			timer.start();
			showParticles = true;
			zoom = 25;
			particles = new Particle[48/SPARSITY][24/SPARSITY];
			for (int x=-24;x<24;x+=SPARSITY){
				for (int y=-12;y<12;y+=SPARSITY){
					particles[(x+24)/SPARSITY][(y+12)/SPARSITY] = new Particle(x,y);
				}
			}
			
		}
		
		public void paintComponent(Graphics g){
			
			g.setColor(Color.WHITE);
			g.fillRect(0,0,1250,600);
			
			// Draws and labels axes
			g.setColor(Color.BLACK);
			g.drawLine(599, 0, 599, 600);
			g.drawLine(600, 0, 600, 600);
			g.drawLine(601, 0, 601, 600);
			g.drawLine(0, 299, 1250, 299);
			g.drawLine(0, 300, 1250, 300);
			g.drawLine(0, 301, 1250, 301);
			g.drawString("X", 1175, 295);
			g.drawString("Y", 590, 25);
			
			g.setColor(Color.GRAY);
			// Draws grid
			for (int i=1;i<=24;i++){
				g.drawLine(600+zoom*i,0,600+zoom*i,600);
				g.drawLine(600-zoom*i,0,600-zoom*i,600);
			}
			for (int i=1;i<=12;i++){
				g.drawLine(0,300+zoom*i,1250,300+zoom*i);
				g.drawLine(0,300-zoom*i,1250,300-zoom*i);
			}
			
			for (int x=-600/zoom;x<600/zoom;x+=25/zoom){
				for (int y=-300/zoom;y<300/zoom;y+=25/zoom){
					if (x%SPARSITY==0 && showParticles) particles[(x+24)/SPARSITY][(y+12)/SPARSITY].draw(g);
					try {
						fs.vectorAt(x,y).draw(g);
					} catch (ScriptException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			
			g.setColor(Color.BLACK);
			g.fillRect(1000, 0, 225, 50);
			g.setColor(Color.WHITE);
			g.drawString("Vector Field Generator",1005,25);
			g.drawString("By Reedit Shahriar, 4-8 April 2017",1005,37);
			
		}
		
		public void toggleParticles(){
			
			if (!showParticles){
				particles = new Particle[12][6];
				for (int x=-24;x<24;x+=SPARSITY){
					for (int y=-12;y<12;y+=SPARSITY){
						particles[(x+24)/SPARSITY][(y+12)/SPARSITY] = new Particle(x,y);
					}
				}
			}
			showParticles = !showParticles;
			
		}
		
		public void updateZoom(int zoomIn){ // Work in progress
			
			zoom = zoomIn;
			//System.out.println(zoom);
			repaint();
			
		}
		
		public void actionPerformed(ActionEvent e) {
			
			for (int x=0;x<48/SPARSITY;x++){
				for (int y=0;y<24/SPARSITY;y++)
					if (showParticles) particles[x][y].update();
			}
			repaint();
			
		}
		
		class Particle{
			
			private double x,y;
			private boolean move;
			
			public Particle(double xIn,double yIn){
				
				x = xIn;
				y = yIn;
				move = true;
				
			}
			
			public void draw(Graphics g){
				
				Color old = g.getColor();
				g.setColor(Color.BLACK);
				g.fillOval((int)((x+24)*25),(int)(600-(25*(y+12))),5,5);
				g.setColor(old);
				
			}
			
			public void update(){
				
				Vector pointVector = null;
				try {
					pointVector = fs.vectorAt(x,y);
				} catch (ScriptException e) {
					toggleParticles();
					move = false;
				}
				if (move){
					x += pointVector.xVal/25;
					y += pointVector.yVal/25;
				}
				move = ((-24 <= x || x <= 24) || (-12 <= y || y <= 12));
				
			}
			
		}
		
	}
	
	class VFFrame extends JFrame{
		
		VFPanel panel;
		
		public VFFrame(VectorFieldGenerator fs){
			
			super("Function Grapher");
			setSize(1250,600);
			setLocation(150,150);
			panel = new VFPanel(fs);
			VFButton button = new VFButton();
			VFMenuBar menu = new VFMenuBar();
			setLayout(new BorderLayout());
			add(panel,BorderLayout.CENTER);
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1,2));
			panel.add(button);
			panel.add(menu);
			add(panel,BorderLayout.NORTH);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setResizable(false);
			setVisible(true);
			
		}
		
		class VFButton extends JButton implements ActionListener{
			
			public VFButton(){
				
				super("Toggle Particles");
				addActionListener(this);
				
			}

			public void actionPerformed(ActionEvent e) {
				
				panel.toggleParticles();
				
			}
			
		}
		
		class VFMenuBar extends JMenuBar implements ActionListener{

			public VFMenuBar(){
				
				super();
				JMenuItem zoom50 = new JMenuItem("Double Normal Zoom");
				JMenuItem zoom25 = new JMenuItem("Normal Zoom");
				JMenu menu = new JMenu("Customize Zoom");
				menu.add(zoom25);
				menu.add(zoom50);
				add(menu);
				zoom50.addActionListener(this);
				zoom25.addActionListener(this);
				
			}
			
			public void actionPerformed(ActionEvent e) {
				
				String cmd = e.getActionCommand();
				switch (cmd){
				case "Double Normal Zoom":
					panel.updateZoom(50);
					break;
				case "Normal Zoom":
					panel.updateZoom(25);
					break;
				default: break;
				}
				panel.repaint();
				
			}
			
		}
		
	}

}
