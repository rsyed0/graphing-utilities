import javax.script.*;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FunctionScanner {

	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private String function;
	
	public FunctionScanner(String in){
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
			pw = new PrintWriter(new File("fs.in"));
		}catch (IOException e){
			e.printStackTrace();
		}
		pw.println(out);
		pw.close();
		return out;
	}
	
	public static void main(String[] args) throws ScriptException, FileNotFoundException {
		
		Scanner in = new Scanner(new File("fs.in"));
		String func = in.nextLine();
		FunctionScanner fs = new FunctionScanner(func);
		FSFrame frame = fs.new FSFrame(fs);
		in.close();

	}
	
	public double valAt(double x) throws ScriptException{
		
		String copy = function;
		double val = 0;
		int index = -1,runs = 0;
		do{
			if (runs != 0) copy = copy.substring(0,index)+x+copy.substring(index+1);
			index = copy.indexOf('x',index+1);
			runs++;
		} while (index != -1);
		val = (Double)engine.eval(copy);
		return val;
		
	}
	
	class FSPanel extends JPanel implements MouseListener,KeyListener,MouseMotionListener{
		
		FunctionScanner fs;
		
		public FSPanel(FunctionScanner fsIn){
			
			super();
			fs = fsIn;
			setBackground(Color.WHITE);
			addMouseListener(this);
			
		}
		
		public void paintComponent(Graphics g){
			
			g.setColor(Color.WHITE);
			g.fillRect(0,0,1250,600);
			
			byte unitSize = 25;
			double value = 0,prevValue = 0;
			
			// Draws and labels axes
			g.setColor(Color.BLACK);
			g.drawLine(599, 0, 599, 600);
			g.drawLine(601, 0, 601, 600);
			g.drawLine(0, 299, 1250, 299);
			g.drawLine(0, 301, 1250, 301);
			g.drawString("X", 1175, 295);
			g.drawString("Y", 590, 25);

			// Draws grid
			for (int i=1;i<=48;i++){
				g.drawLine(unitSize*i,0,unitSize*i,600);
			}
			for (int i=1;i<=24;i++){
				g.drawLine(0,unitSize*i,1250,unitSize*i);
			}
			
			g.setColor(Color.RED);
			// Draws graphs for each value between -24 and 24 that returns an integer 'x' coordinate
			for (double x=-24;x<=24;x+=0.04){
				try{
					value = fs.valAt(x);
				}catch (ScriptException e){
					e.printStackTrace();
					System.exit(1);
				}
				g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevValue+12))),(int)((x+24)*25),(int)(600-(25*(value+12))));
				g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevValue+12)))-1,(int)((x+24)*25),(int)(600-(25*(value+12)))-1);
				g.drawLine((int)((x+24)*25)-1,(int)(600-(25*(prevValue+12)))+1,(int)((x+24)*25),(int)(600-(25*(value+12)))+1);
				prevValue = value;
			}
			
			// Draws rectangle to give function information (WIP)
			g.setColor(Color.BLACK);
			g.fillRect(1000, 0, 225, 200);
			g.setColor(Color.WHITE);
			g.drawString("Graphing Calculator",1005,25);
			g.drawString("By Reedit Shahriar",1005,37);
			
		}

		public void mouseClicked(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			
			requestFocus();
			int x = e.getX(),y = e.getY();
			Graphics g = getGraphics();
			g.setColor(Color.BLUE);
			g.fillOval(x-7,y-7,14,14);
			g.drawString("("+((x/25.0)-24)+","+(12-(y/25.0))+")",x+10,y);
			
		}

		public void mouseReleased(MouseEvent e) {}

		public void keyPressed(KeyEvent e) {
			
			repaint();
			
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {}

		public void mouseDragged(MouseEvent e) {}

		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class FSFrame extends JFrame{
		
		public FSFrame(FunctionScanner fs){
			
			super("Function Grapher");
			setSize(1250,600);
			setLocation(150,150);
			FSPanel panel = new FSPanel(fs);
			setContentPane(panel);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setResizable(false);
			setVisible(true);
			
		}
		
	}
	
}
