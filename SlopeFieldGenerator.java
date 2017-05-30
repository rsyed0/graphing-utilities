import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.script.*;
import javax.swing.*;

public class SlopeFieldGenerator {

	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private String function;
	
	public SlopeFieldGenerator(String in){
		in = process(in);
		function = in;
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");
	}

	public static String process(String in){
		
		PrintWriter pw = null;
		String out = in;
		List<Integer> indices = new ArrayList<Integer>();
		int index = -1;
		do{
			indices.add(out.indexOf('^',index+1));
			index = out.indexOf('^',index+1);
		} while (index != -1);
		indices.remove(indices.size()-1);
		for (int i:indices){
			out = out.substring(0,out.lastIndexOf('(',i))
			+"Math.pow("+out.substring(out.lastIndexOf('(',i)+1,i-1)
			+","+out.substring(i+2,out.indexOf(')',i))+")"
			+out.substring(out.indexOf(')',i)+1);
		}
		try{
			pw = new PrintWriter(new File("slopefield.in"));
		}catch (IOException e){
			e.printStackTrace();
		}
		pw.println(out);
		pw.close();
		return out;
	}
	
	public static void main(String[] args) throws ScriptException, FileNotFoundException {
		
		Scanner in = new Scanner(new File("slopefield.in"));
		String func = in.nextLine();
		SlopeFieldGenerator fs = new SlopeFieldGenerator(func);
		SFFrame frame = fs.new SFFrame(fs);
		in.close();

	}
	
	public double slopeAt(double x,double y) throws ScriptException{
		
		String copy = function;
		String val = "";
		int xIndex = -1,yIndex = -1,runs = 0;
		do{
			if (runs != 0) copy = copy.substring(0,xIndex)+x+copy.substring(xIndex+1);
			xIndex = copy.indexOf('x',xIndex+1);
			runs++;
		} while (xIndex != -1);
		runs=0;
		do{
			if (runs != 0) copy = copy.substring(0,yIndex)+y+copy.substring(yIndex+1);
			yIndex = copy.indexOf('y',yIndex+1);
			runs++;
		} while (yIndex != -1);
		val = engine.eval(copy).toString();
		double m = Double.parseDouble(val);
		try{
			assert Math.abs(m) < 10000000;
		} catch (AssertionError e){
			Thread.currentThread().interrupt();
		}
		return m;
		
	}
	
	class SFPanel extends JPanel implements MouseListener,KeyListener{
		
		SlopeFieldGenerator fs;
		
		public SFPanel(SlopeFieldGenerator fsIn){
			
			super();
			fs = fsIn;
			setBackground(Color.WHITE);
			addMouseListener(this);
			addKeyListener(this);
			
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
			
			g.setColor(Color.RED);
			double m = 0;
			double l = 7;
			
			for (int x=-24;x<=24;x++){
				for (int y=-12;y<=12;y++){
					try {
						m = fs.slopeAt(x,y);
					} catch (ScriptException e) {
						e.printStackTrace();
						System.exit(1);
					}
					//l = 10/m;
					g.drawLine((x+24)*25,600-(25*(y+12)),(x+24)*25-(int)(l/Math.sqrt(m*m+1)),
						600-(25*(y+12))+(int)((l*m)/Math.sqrt(m*m+1)));
					g.drawLine((x+24)*25,600-(25*(y+12)),(x+24)*25+(int)(l/Math.sqrt(m*m+1)),
						600-(25*(y+12))-(int)((l*m)/Math.sqrt(m*m+1)));
					//System.out.println("X: "+x+",Y: "+y+",M: "+m);
				}
			}
			
			// Draws rectangle to give function information (WIP)
			g.setColor(Color.BLACK);
			g.fillRect(1000, 0, 225, 50);
			g.setColor(Color.WHITE);
			g.drawString("Slope Field & Integral Curve Generator",1005,25);
			g.drawString("By Reedit Shahriar, 3-4 April 2017",1005,37);
			
		}

		public void mouseClicked(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			
			requestFocusInWindow();
			//Graphics g = getGraphics();
			Point initCond = new Point(e.getX(),e.getY()+15);
			//double x = 0,y = (12-initCond.y/25),m = 0;
			DrawThread right = new DrawThread(initCond,true);
			DrawThread left = new DrawThread(initCond,false);
			right.start();
			left.start();
			
		}

		public void mouseReleased(MouseEvent e) {}

		public void keyPressed(KeyEvent e) {
			
			if (e.getKeyCode() == KeyEvent.VK_ENTER){
				Graphics g = getGraphics();
				Color old = g.getColor();
				g.setColor(Color.WHITE);
				g.fillRect(0,0,1250,600);
				g.setColor(old);
				g.dispose();
				repaint();
			}
			
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {}
		
		class DrawThread extends Thread{
			
			private double x,y;
			private boolean right;
			
			public DrawThread(Point init,boolean goingRight){
				
				x = ((init.x/25.0)-24);
				y = (12-init.y/25);
				right = goingRight;
				
			}
			
			public void run(){
				
				Graphics g = getGraphics();
				Color old = g.getColor();
				g.setColor(Color.BLUE);
				double m = 0,prevY = 0;
				if (right){
					for (x=x;x<=24;x+=0.04){
						try {
							m = fs.slopeAt(x,y);
						} catch (ScriptException e1) {
							e1.printStackTrace();
							System.exit(1);
						}
						prevY = y;
						y += 0.04*m;
						g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevY+12))),
							(int)((x+24)*25),(int)(600-(25*(y+12))+m));
						g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevY+12)))+1,
							(int)((x+24)*25),(int)(600-(25*(y+12))+m)+1);
						g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevY+12)))-1,
							(int)((x+24)*25),(int)(600-(25*(y+12))+m)-1);
						if (Math.abs(m) > 100){
							g.drawLine((int)((x+24)*25),0,(int)((x+24)*25),600);
							x+=0.04;
						} else if (y >= 12 || y <= -12) break;
					}
				} else {
					for (x=x;x>=-24;x-=0.04){
						try {
							m = fs.slopeAt(x,y);
						} catch (ScriptException e1) {
							e1.printStackTrace();
							System.exit(1);
						}
						prevY = y;
						y -= 0.04*m;
						g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevY+12))),
							(int)((x+24)*25),(int)(600-(25*(y+12))+m));
						g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevY+12)))+1,
							(int)((x+24)*25),(int)(600-(25*(y+12))+m)+1);
						g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevY+12)))-1,
							(int)((x+24)*25),(int)(600-(25*(y+12))+m)-1);
						if (Math.abs(m) > 100){
							g.drawLine((int)((x+24)*25),0,(int)((x+24)*25),600);
							x-=0.04;
						} else if (y >= 12 || y <= -12) break;
					}
				}
				g.setColor(old);
				g.dispose();
				
			}
			
		}

	}
	
	class SFFrame extends JFrame{
		
		public SFFrame(SlopeFieldGenerator fs){
			
			super("Function Grapher");
			setSize(1250,600);
			setLocation(150,150);
			SFPanel panel = new SFPanel(fs);
			setContentPane(panel);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setResizable(false);
			setVisible(true);
			
		}
		
	}

}
