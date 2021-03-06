package qmask;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import png.PngWriter;

public class QuickMask {
	public static boolean gathering=false;
	public static boolean scattering=false;
	public static void main(String[] args) /*throws Exception*/ {
		final JFrame frame=new JFrame("QuickMask Emergency UI");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weightx=1;
		final JLabel file=new JLabel();
		c.gridwidth=2;
		frame.add(file, c);
		JButton pickfile=new JButton("Pick XML");
		frame.add(pickfile, c);
		pickfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setAcceptAllFileFilterUsed(false);
				jfc.setDialogTitle("Pick QuickNII XML");
				jfc.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "QuickNII XML";
					}
					@Override
					public boolean accept(File f) {
						String s=f.getName().toLowerCase();
						return f.isDirectory()||s.endsWith(".xml");
					}
				});
				if(jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					file.setText(jfc.getSelectedFile().getPath());
			}
		});
		c.weighty=1;
		c.gridwidth=1;
		JLabel tl=new JLabel("Top-left");
		c.gridx=0;
		c.gridy=2;
		frame.add(tl, c);
		JLabel tr=new JLabel("Top-right");
		c.gridy++;
		frame.add(tr, c);
		JLabel bl=new JLabel("Bottom-left");
		c.gridy++;
		frame.add(bl, c);
		JLabel x=new JLabel("x");
		c.gridx++;
		c.gridy=1;
		c.fill=GridBagConstraints.CENTER;
		frame.add(x, c);
		JLabel y=new JLabel("y");
		c.gridx++;
		frame.add(y, c);
		JLabel z=new JLabel("z");
		c.gridx++;
		frame.add(z, c);
		c.fill=GridBagConstraints.BOTH;
		final JTextField combined=new JTextField();
		c.gridx=0;
		c.gridy=5;
		c.gridwidth=4;
		frame.add(combined, c);
		c.gridwidth=1;
		final JTextField corn[]=new JTextField[9];
		DocumentListener gather=new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent e) {gather();}
			@Override public void insertUpdate(DocumentEvent e) {gather();}
			@Override public void changedUpdate(DocumentEvent e) {gather();}
			public void gather() {
				if(scattering)return;
				gathering=true;
				String s=corn[0].getText();
				for(int i=1;i<corn.length;i++)
					s+=" "+corn[i].getText();
				combined.setText(s);
				gathering=false;
			}
		};
		for(int j=0;j<3;j++)
			for(int i=0;i<3;i++) {
				JTextField jtf=corn[i+j*3]=new JTextField();
				jtf.getDocument().addDocumentListener(gather);
				jtf.setHorizontalAlignment(JTextField.CENTER);
				c.gridx=i+1;
				c.gridy=j+2;
				frame.add(jtf,c);
			}
		combined.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent e) {scatter();}
			@Override public void insertUpdate(DocumentEvent e) {scatter();}
			@Override public void changedUpdate(DocumentEvent e) {scatter();}
			public void scatter() {
				if(gathering)return;
				scattering=true;
				String items[]=combined.getText().trim().split("\\s+");
				for(int i=0;i<corn.length;i++)
					corn[i].setText(i<items.length?items[i]:"");
				scattering=false;
			}
		});
		final JLabel dir=new JLabel();
		c.weighty=0;
		c.gridwidth=2;
		c.gridy=6;
		c.gridx=0;
		frame.add(dir, c);
		JButton pickdir=new JButton("Destination");
		c.gridx=2;
		frame.add(pickdir, c);
		pickdir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setDialogTitle("Pick output folder");
				if(jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
					dir.setText(jfc.getSelectedFile().getPath());
			}
		});
		JButton go=new JButton("Go");
		c.gridwidth=4;
		c.gridy=7;
		c.gridx=0;
		frame.add(go, c);
		frame.setSize(400, 300);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		go.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
				final double cx=Double.parseDouble(corn[0].getText());
				final double cy=Double.parseDouble(corn[1].getText());
				final double cz=Double.parseDouble(corn[2].getText());
				final double nux=Double.parseDouble(corn[3].getText())-cx;
				final double nuy=Double.parseDouble(corn[4].getText())-cy;
				final double nuz=Double.parseDouble(corn[5].getText())-cz;
				final double nvx=Double.parseDouble(corn[6].getText())-cx;
				final double nvy=Double.parseDouble(corn[7].getText())-cy;
				final double nvz=Double.parseDouble(corn[8].getText())-cz;
				final double nx=nuy*nvz-nvy*nuz;
				final double ny=nuz*nvx-nvz*nux;
				final double nz=nux*nvy-nvx*nuy;
				Scanner s=new Scanner(new File(file.getText()));
				String xml=s.useDelimiter("~~trallala~~").next().replaceAll("&", "&amp;");
				NodeList n=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement().getElementsByTagName("slice");
				s.close();
				for(int i=0;i<n.getLength();i++)
				{
					System.out.println(i);
					Element e=(Element)n.item(i);
					String filename=e.getAttribute("filename");
					if(filename.endsWith(".png"))filename=filename.substring(0, filename.length()-4);
					String a[]=e.getAttribute("anchoring").split("[=|&]");
					double ox=Double.parseDouble(a[1]);
					double oy=Double.parseDouble(a[3]);
					double oz=Double.parseDouble(a[5]);
					double ux=Double.parseDouble(a[7]);
					double uy=Double.parseDouble(a[9]);
					double uz=Double.parseDouble(a[11]);
					double vx=Double.parseDouble(a[13]);
					double vy=Double.parseDouble(a[15]);
					double vz=Double.parseDouble(a[17]);
					int width=(int)Math.sqrt(ux*ux+uy*uy+uz*uz)+1;
					int height=(int)Math.sqrt(vx*vx+vy*vy+vz*vz)+1;
					FileOutputStream fos=new FileOutputStream(dir.getText()+File.separator+filename+"_mask.png");
					PngWriter png=new PngWriter(fos, width, height, PngWriter.TYPE_GRAYSCALE, null);
					byte line[]=new byte[width];
					for(int v=0;v<height;v++) {
						for(int u=0;u<width;u++)
							line[u]=(ox+ux*u/width+vx*v/height-cx)*nx+(oy+uy*u/width+vy*v/height-cy)*ny+(oz+uz*u/width+vz*v/height-cz)*nz>0?(byte)255:0;
						png.writeline(line);
					}
					fos.close();
				}
				JOptionPane.showMessageDialog(null, "Everything went fine", "All good", JOptionPane.INFORMATION_MESSAGE);
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}catch(Exception ex) {
					JOptionPane.showMessageDialog(null, ex.toString(), "Something went wrong", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
}
