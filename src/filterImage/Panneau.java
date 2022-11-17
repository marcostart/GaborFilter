package filterImage;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;

import javax.swing.JPanel;

public class Panneau extends JPanel{
	
	private String texte;
	public Panneau(String text) {
		texte= text;
		
	}
	public void paintComponent (Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		Font police = new Font("Tahoma", Font.BOLD, 16 );
		g2d.setColor(Color.black);
		g2d.drawRect(0, 0, this.getWidth(), this.getHeight());
		g2d.setFont(police);
		g2d.setColor(Color.red);
		
		FontMetrics fm = g2d.getFontMetrics();
		//Hauteur de la police d'écriture
		int height = fm.getHeight();
		//Largeur totale de la chaîne passée en paramètre
		int width = fm.stringWidth(this.texte);
		g2d.drawString(texte, this.getWidth()/2-(width/2), this.getHeight()/2+(height/4));
		
	}
}
