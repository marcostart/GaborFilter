package filterImage;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ZProjector;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class FilterFen extends JFrame{
	private Panneau source = new Panneau("Image source");
	private Panneau filtre = new Panneau("Image filtrée");
	private JPanel container = new JPanel();
	private JButton bouton1 = new JButton("Importer");
	private JButton bouton2 = new JButton("filtrer");
	private JLabel image = new JLabel();
	private JLabel imageFiltre = new JLabel();
	private String path = "";
	
	public FilterFen() {
		this.setTitle("Filtrage");
		this.setSize(500,500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		bouton1.addActionListener(new ImportListener());
		bouton2.addActionListener(new FilterListener());
		container.setLayout(new BorderLayout());
		JPanel contain = new JPanel();
		contain.setLayout(new GridLayout(1,2));
		source.add(image);
		filtre.add(imageFiltre);
		contain.add(source);
		contain.add(filtre);
		JPanel cont2 = new JPanel();
		cont2.setLayout(new GridLayout(1,2));
		cont2.add(bouton1);
		cont2.add(bouton2);
		bouton2.setEnabled(false);
		container.add(contain, BorderLayout.CENTER);
		container.add(cont2, BorderLayout.NORTH);
		this.setContentPane(container);
		this.setVisible(true);
	}
	class ImportListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			JFileChooser file = new JFileChooser();
	        file.setCurrentDirectory(new File(System.getProperty("user.home")));
	        //filtrer les fichiers
	        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images","jpg","png");
	        file.addChoosableFileFilter(filter);
	        int res = file.showSaveDialog(null);
	        //si l'utilisateur clique sur enregistrer dans Jfilechooser
	        if(res == JFileChooser.APPROVE_OPTION){
	          File selFile = file.getSelectedFile();
	          path = selFile.getAbsolutePath();
	          
	          image.setIcon(resize(path));
	          
	        }
//			Opener opener = new Opener();
//			ImagePlus image = opener.openImage("/home/marcolin/Documents/Mon_Projet/Java_projects/prog.jpeg");
			bouton2.setEnabled(true);
			//image.show();
		}
		
		public ImageIcon resize(String imgPath)
		  {
		    ImageIcon path = new ImageIcon(imgPath);
		    Image img = path.getImage();
		    Image newImg = img.getScaledInstance(source.getWidth(), source.getHeight(), Image.SCALE_SMOOTH);
		    ImageIcon image = new ImageIcon(newImg);
		    return image;
		  }
		
	}
	class FilterListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Opener opener = new Opener();
			  ImagePlus image2 = opener.openImage(path);
			  double F = 3.0 ;
			  double sigma_x = 4 ;
			  double sigma_y = 16;
			  
			  //image2.show();
			  image2 = new ImagePlus(image2.getTitle(), image2.getProcessor().convertToFloat());
			  int width = image2.getWidth();
			  int height = image2.getHeight();
			  ImageProcessor ip = image2.getProcessor().duplicate();
			 
			  double sigma_x2 = sigma_x * sigma_x ;
			  double sigma_y2 = sigma_y * sigma_y ;
			  int filterSizeX = 19;
			  int filterSizeY = 19;
			  int middleX = ( int ) Math . round ( filterSizeX / 2 ) ;
			  int middleY = ( int ) Math . round ( filterSizeY / 2 ) ;
			  FloatProcessor filter = new FloatProcessor(filterSizeX, filterSizeY);
			  ImageStack kernels = new ImageStack(filterSizeX, filterSizeY);
			  ImageStack is = new ImageStack(width, height);
			  double theta = Math . PI / ( double ) 6 ;
			  for ( int x=-middleX ; x<= middleX ; x ++) 
			  {
				  for ( int y=-middleY ; y<= middleY ; y ++) 
				  {
					  double xPrime = ( double ) x * Math.cos(theta) + (double) y*Math.sin(theta);
					  double yPrime = ( double ) y * Math.cos(theta) - (double) x*Math.sin(theta);
					  double a = 1.0 / ( 2.0 * Math.PI * sigma_x * sigma_y ) * Math . exp ( -0.5 * ( xPrime*xPrime / sigma_x2 + yPrime*yPrime / sigma_y2 ) ) ;
					  double c = a * Math . cos ( 2.0 * Math.PI * ( F * xPrime ) / filterSizeX ) ;
					  filter.setf(x+middleX, y+middleY, (float)(c) );
				  }
			  }
			  kernels.addSlice("kernel angle = " + theta, filter);
			  ImagePlus ip_kernels = new ImagePlus("kernels", kernels);
			  ip_kernels.show();
			  
			// Apply kernels
			  Convolver c = new Convolver();                
			    
			  float[] kernel = (float[]) kernels.getProcessor(1).getPixels();
			  c.convolveFloat(ip, kernel, filterSizeX, filterSizeY);      

			    is.addSlice("gabor angle = " + 1, ip);
			 // Normalize filtered stack
			    ContrastEnhancer c2 = new ContrastEnhancer();
			    for(int i=1 ; i <= is.getSize(); i++)
			    {
			        c2.stretchHistogram(is.getProcessor(i), 0.4);
			    }


			    ImagePlus projectStack = new ImagePlus("filtered stack",is);
			    IJ.run(projectStack, "Enhance Contrast", "saturated=0.4 normalize normalize_all");
                
			    ImageStack resultStack = new ImageStack(width, height);
			                    
			    ZProjector zp = new ZProjector(projectStack);
			    zp.setStopSlice(is.getSize());
			    for (int i=0;i<=5; i++)
			    {
			        zp.setMethod(i);
			        zp.doProjection();
			        resultStack.addSlice("Gabor_" + i +"_"+sigma_x+"_" + sigma_x/sigma_y + "_1_"+F, zp.getProjection().getChannelProcessor());
			    }
			    
			    ImagePlus resultf = (new ImagePlus("gabor, sigma="+sigma_x+" gamma="+sigma_x/sigma_y+ " psi=1", is));
			    BufferedImage bufferIm = resultf.getBufferedImage();
			    Image newImg = bufferIm.getScaledInstance(filtre.getWidth(), filtre.getHeight(), Image.SCALE_SMOOTH);
			    ImageIcon imageIcon = new ImageIcon(newImg);
			    imageFiltre.setIcon(imageIcon);
			    ImagePlus result= new ImagePlus ("Gabor stack projections", resultStack) ;
			    IJ.run(result, "Enhance Contrast", "saturated=0.4 normalize normalize_all");
			    result.show();
			
		}
		
	}
	
}
