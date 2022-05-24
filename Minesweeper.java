import java.util.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

public class Minesweeper extends JPanel implements ActionListener,MouseListener
{
	JFrame frame;
	int dimR=9,dimC=9,numMines=5,buttonSizeX=50, buttonSizeY=50,minesFlagged=0,spacesClicked=0,numNonMines=dimR*dimC-numMines, timePassed=0;;
	JToggleButton[][] grid;
	JMenuItem[] difficulties;
	JButton resetButton;
	JTextField timerText;
	java.util.Timer timer;
	JPanel gridPanel;
	boolean firstClick = true;
	ImageIcon[] icons;
	ImageIcon winIcon, loseIcon, idleIcon, holdIcon;
	boolean gameOn;
	Font clockFont;
	GraphicsEnvironment ge;

	public Minesweeper()
	{
		icons = new ImageIcon[12];
		try{
			Image img = ImageIO.read(getClass().getResource("/Minesweeper Images\\0.png"));
			Image scaledImage = img.getScaledInstance(buttonSizeX, buttonSizeY, java.awt.Image.SCALE_SMOOTH);
			icons[0] = new ImageIcon(scaledImage);

			for(int iconNum=1; iconNum<9; iconNum++)
			{
				img = ImageIO.read(getClass().getResource("/Minesweeper Images\\"+iconNum+".png"));
				scaledImage = img.getScaledInstance(buttonSizeX, buttonSizeY, java.awt.Image.SCALE_SMOOTH);
				icons[iconNum] = new ImageIcon(scaledImage);
			}

			img = ImageIO.read(getClass().getResource("/Minesweeper Images\\flagged.png"));
			scaledImage = img.getScaledInstance(buttonSizeX, buttonSizeY, java.awt.Image.SCALE_SMOOTH);
			icons[9] = new ImageIcon(scaledImage);

			img = ImageIO.read(getClass().getResource("/Minesweeper Images\\mine.png"));
			scaledImage = img.getScaledInstance(buttonSizeX, buttonSizeY, java.awt.Image.SCALE_SMOOTH);
			icons[10] = new ImageIcon(scaledImage);

			img = ImageIO.read(getClass().getResource("/Minesweeper Images\\facingDown.png"));
			scaledImage = img.getScaledInstance(buttonSizeX, buttonSizeY, java.awt.Image.SCALE_SMOOTH);
			icons[11] = new ImageIcon(scaledImage);

			img = ImageIO.read(getClass().getResource("/pogFace.png"));
			scaledImage = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
			winIcon = new ImageIcon(scaledImage);

			img = ImageIO.read(getClass().getResource("/Minesweeper Images\\smile0.png"));
			scaledImage = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
			idleIcon = new ImageIcon(scaledImage);

			img = ImageIO.read(getClass().getResource("/Minesweeper Images\\dead0.png"));
			scaledImage = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
			loseIcon = new ImageIcon(scaledImage);

			img = ImageIO.read(getClass().getResource("/Minesweeper Images\\wait0.png"));
			scaledImage = img.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
			holdIcon = new ImageIcon(scaledImage);
		}catch(Exception e){}

		frame = new JFrame("Minesweeper");
		frame.add(this);

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Difficulties");
		difficulties = new JMenuItem[3];
		difficulties[0] = new JMenuItem("Beginner");
		difficulties[1] = new JMenuItem("Intermediate");
		difficulties[2] = new JMenuItem("Advanced");
		for(JMenuItem j : difficulties)
		{
			j.addActionListener(this);
			menu.add(j);
		}

		menuBar.add(menu);

		resetButton = new JButton();
		resetButton.setIcon(idleIcon);
		resetButton.addActionListener(this);
		menuBar.add(resetButton);

		try{
			ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			clockFont = Font.createFont(Font.TRUETYPE_FONT, new File("Minesweeper Images\\digital-7.ttf"));
		}catch(IOException|FontFormatException e){}

		timerText = new JTextField("Time: 0:00");
		timerText.setEditable(false);
		timerText.setBackground(Color.BLACK);
		timerText.setForeground(Color.GREEN);
		timerText.setFont(clockFont.deriveFont(24f));

		menuBar.add(timerText);
		frame.setJMenuBar(menuBar);
		setGrid(dimR,dimC);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	public void setGrid(int dimR, int dimC)
	{
		minesFlagged = 0;
		spacesClicked = 0;
		if(timer != null)
			timer.cancel();
		timePassed = 0;
		timerText.setText("Time: 0:00");
		gameOn = true;
		if(gridPanel != null)
			frame.remove(gridPanel);

		firstClick = true;
		gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(dimR,dimC));
		grid = new JToggleButton[dimR][dimC];
		for(int i=0; i<grid.length; i++)
		{
			for(int j=0; j<grid[i].length; j++)
			{
				grid[i][j] = new JToggleButton();
				grid[i][j].setFocusable(false);
				grid[i][j].addMouseListener(this);
				grid[i][j].putClientProperty("row",i);
				grid[i][j].putClientProperty("col",j);
				grid[i][j].putClientProperty("isFlagged",false);
				grid[i][j].putClientProperty("state",0); // -1 is a bomb, 0-8 is # of bombs next to the tile
				try{
					grid[i][j].setIcon(icons[11]);
					grid[i][j].setDisabledIcon(icons[11]);
				}
				catch(Exception e){}
				gridPanel.add(grid[i][j]);
			}
		}


		frame.setSize(buttonSizeX*dimC,buttonSizeY*dimR);
		frame.add(gridPanel, BorderLayout.CENTER);
		frame.revalidate();
	}
	public void disableGrid()
	{
		for(int i=0; i<grid.length; i++)
		{
			for(int j=0; j<grid[i].length; j++)
			{
				int state = (int)grid[i][j].getClientProperty("state");
				grid[i][j].setEnabled(false);
				if(checkWin())
				{
					if(state==-1)
						if(!(boolean)grid[i][j].getClientProperty("isFlagged"))
							grid[i][j].setDisabledIcon(icons[9]);
				}
				else
				{
					if(state!=-1)
						grid[i][j].setDisabledIcon(icons[state]);
					else
						grid[i][j].setDisabledIcon(icons[10]);
				}
			}
		}
		timer.cancel();
	}

	public void dropMines(int currRow, int currCol)
	{
		int count = numMines;
		while(count>0)
		{
			//find random location to drop mines into
			int row = (int)(Math.random()*dimR);
			int col = (int)(Math.random()*dimC);
			int state = (int)grid[row][col].getClientProperty("state");
			if(Math.abs(row-currRow) > 1 && Math.abs(col-currCol)>1 && state == 0)
			{
				grid[row][col].putClientProperty("state", -1);
				count--;
			}
		}

		for(int r=0; r<grid.length; r++)
		{
			for(int c=0; c<grid[r].length; c++)
			{
				if(((int)grid[r][c].getClientProperty("state"))== -1)
				{
					for(int rr=r-1; rr<=r+1; rr++)
					{
						for(int cc=c-1; cc<=c+1; cc++)
						{
							try
							{
								int state = (int)grid[rr][cc].getClientProperty("state");
								if(state != -1)
									grid[rr][cc].putClientProperty("state", state+1);
							}catch(ArrayIndexOutOfBoundsException e){}
						}
					}

				}
			}
		}
	}

	public void expand(int row,int col)
	{
		if(!grid[row][col].isSelected())
		{
			grid[row][col].setSelected(true);
			grid[row][col].setEnabled(false);
		}

		int state = (int)grid[row][col].getClientProperty("state");
		if(state>0 && state<9)
		{
			grid[row][col].setIcon(icons[state]);
			grid[row][col].setDisabledIcon(icons[state]);
		}
		else if((boolean)grid[row][col].getClientProperty("isFlagged"))
		{
			grid[row][col].setDisabledIcon(icons[9]);
			grid[row][col].setIcon(icons[9]);
		}
		else
		{
			grid[row][col].setDisabledIcon(icons[0]);
			grid[row][col].setIcon(icons[0]);
			for(int r=row-1; r<=row+1; r++)
			{
				for(int c=col-1; c<=col+1; c++)
				{
					try
					{
						if(!grid[r][c].isSelected())
							expand(r,c);
					}catch(ArrayIndexOutOfBoundsException e){}
				}
			}
		}
		grid[row][col].setEnabled(false);
		spacesClicked++;
	}

	public boolean checkWin()
	{
		return numMines == minesFlagged || spacesClicked == numNonMines;
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == resetButton)
		{
			setGrid(dimR, dimC);
		}
		else if(e.getSource() == difficulties[0])
		{
			dimR = 9;
			dimC = 9;
			numMines = 10;
			setGrid(dimR, dimC);
		}
		else if(e.getSource() == difficulties[1])
		{
			dimR = 16;
			dimC = 16;
			numMines = 40;
			setGrid(dimR, dimC);
		}
		else if(e.getSource() == difficulties[2])
		{
			dimR = 9;
			dimC = 40;
			numMines = 99;
			setGrid(dimR, dimC);
		}
	}
	public void mouseReleased(MouseEvent e)
	{
		if(gameOn)
		{
			resetButton.setIcon(idleIcon);
			int row = (int)((JToggleButton)e.getComponent()).getClientProperty("row");
			int col = (int)((JToggleButton)e.getComponent()).getClientProperty("col");
			boolean isFlagged = (boolean)grid[row][col].getClientProperty("isFlagged");

			if(firstClick)
			{
				timer = new java.util.Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						timePassed++;
						timerText.setText("Time: "+timePassed/60+":"+(timePassed%60<10? "0"+timePassed%60 : timePassed%60));
					}
				}, 0, 1000);
				firstClick = false;
				dropMines(row,col);
				int state = (int)grid[row][col].getClientProperty("state");
				grid[row][col].setIcon(icons[state]);
				grid[row][col].setDisabledIcon(icons[state]);
				grid[row][col].setEnabled(false);
				expand(row,col);
			}
			else if(e.getButton() == MouseEvent.BUTTON1 && grid[row][col].isEnabled())//LEFT CLICK
			{
				if(!isFlagged)
				{
					int state = (int)grid[row][col].getClientProperty("state");
					if(state == -1)
					{
						grid[row][col].setContentAreaFilled(false);
						grid[row][col].setOpaque(true);
						grid[row][col].setBackground(Color.RED);
						grid[row][col].setIcon(icons[10]);
						grid[row][col].setDisabledIcon(icons[10]);
						disableGrid();
						//JOptionPane.showMessageDialog(this,"You Lose!");
						gameOn = false;
						timer.cancel();
						resetButton.setIcon(loseIcon);
					}
					else
					{
						if(state == 0)
						{
							grid[row][col].setIcon(icons[state]);
							grid[row][col].setDisabledIcon(icons[state]);
							expand(row,col);
							grid[row][col].setEnabled(false);
						}
						else
						{
							grid[row][col].setIcon(icons[state]);
							grid[row][col].setDisabledIcon(icons[state]);
							spacesClicked++;
						}
					}
				}
			}
			else if(e.getButton() == MouseEvent.BUTTON3)//RIGHT CLICK
			{
				if(!grid[row][col].isSelected())
				{
					int state = (int)grid[row][col].getClientProperty("state");
					if(isFlagged)
					{
						grid[row][col].putClientProperty("isFlagged", false);
						grid[row][col].setIcon(icons[11]);
						grid[row][col].setDisabledIcon(icons[11]);
						grid[row][col].setEnabled(true);
						//if(state == -1)
							//minesFlagged--;
					}
					else
					{
						grid[row][col].putClientProperty("isFlagged", true);
						grid[row][col].setIcon(icons[9]);
						grid[row][col].setDisabledIcon(icons[9]);
						grid[row][col].setEnabled(false);
						//if(state == -1)
							//minesFlagged++;
					}
				}
			}
			if(checkWin())
			{
				disableGrid();
				gameOn = false;
				//JOptionPane.showMessageDialog(this,"You Win!");
				resetButton.setIcon(winIcon);
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON1 && gameOn)
		{
			resetButton.setIcon(holdIcon);
		}
	}

	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}

	public static void main(String[] args)
	{
		Minesweeper app = new Minesweeper();
	}


}