import java.io.*;
import java.sql.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.*;
class ISE extends JFrame implements ActionListener{ 
	static File chosenFile;
	private static final long serialVersionUID = -1113582265865921787L;
	ISE()
	{
		FlowLayout layout = new FlowLayout();
		this.setLayout(layout);
	    //this.setSize(300,300)
		//Container c =this.getContentPane();
		JLabel lbl2=new JLabel();
		try
		{
			String path1 = "C:/Users/Siddharth/Desktop/logo.jpg";
			BufferedImage img2=ImageIO.read(new File(path1));
		    ImageIcon icon2=new ImageIcon(img2);
		    lbl2.setIcon(icon2);
		    this.add(lbl2);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}     
		JButton buttonBrowse =new JButton("Browse...");
	    this.add(buttonBrowse);
	    buttonBrowse.setBounds(600,400,100,50);
	    buttonBrowse.addActionListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public void actionPerformed(ActionEvent ae)
	{
	JFileChooser chooser= new JFileChooser();
	ImagePreviewPanel preview = new ImagePreviewPanel();
	chooser.setAccessory(preview);
	chooser.addPropertyChangeListener(preview);
	int choice = chooser.showOpenDialog(null);
	chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
	 chooser.setAcceptAllFileFilterUsed(true);
	 //	FileFilter filter1 = new ExtensionFileFilter("JPG and JPEG", new String[] { "JPG", "JPEG" });
	//chooser.setFileFilter(imageFilter);

	if (choice != JFileChooser.APPROVE_OPTION) return;
	chosenFile = chooser.getSelectedFile();
	try{
		JFrame fr=new JFrame("Input");
	    fr.setLayout(new FlowLayout());
	    fr.setSize(300,300);
	    JLabel lbl1=new JLabel();
	BufferedImage img1=ImageIO.read(chosenFile);
    ImageIcon icon1=new ImageIcon(img1);
    lbl1.setIcon(icon1);
    
    fr.add(lbl1);
    fr.setVisible(true);
    fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String f=chosenFile.getPath();
		//File file=new File(f);
		//FileInputStream fis = new FileInputStream(file);
		ImagePHash ih = new ImagePHash();
    	InputStream is = new BufferedInputStream(new FileInputStream(f));
    	String hash=ih.getHash(is);
    	//System.out.println(hash);
    	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    	Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "12345");
    	Statement stmt=con.createStatement();
    	ResultSet rs=stmt.executeQuery("select * from dataset");
    while(rs.next())
    {
    	String h=rs.getString(3);
    	int hamming=HammingDistance(hash,h);
    	//System.out.println("Hamming Distance= "+hamming);
    	if(hamming<=15)
    	{
    		Blob b=rs.getBlob(2);
    		
    		byte arr[]=new byte[1024];
    		arr=b.getBytes(1,(int)b.length());
    		String path="C:/Users/Siddharth/Desktop/temp/"+rs.getString(1);
    		FileOutputStream fos=new FileOutputStream(path);
    		fos.write(arr);
    		fos.close();
    		File f1=new File(path);
    		FileInputStream fis=new FileInputStream(f1);
    		PreparedStatement stmt2=con.prepareStatement("insert into temp values(?,?,?)");
    		stmt2.setString(1, rs.getString(1));
    		stmt2.setBinaryStream(2,fis,(int)f1.length());
    		stmt2.setInt(3,hamming);
    		stmt2.executeUpdate();
    		fis.close();
    		stmt2.close();
    		System.gc();
    		f1.delete();
    	}
    	
    }
   String newpath="C:/Users/Siddharth/Desktop/output/";
    Statement stmt3 =con.createStatement();
    ResultSet rs1=stmt3.executeQuery("select * from temp order by hamming");
    JFrame frame=new JFrame("ISE");
    frame.setBounds(500, 200, 647, 418);
    //frame.setLocationRelativeTo(fr);
    frame.setLayout(new FlowLayout());
    frame.setSize(400,400);
    JLabel lbl=new JLabel();
    while(rs1.next())
    {
    	Blob b =rs1.getBlob(2);
    	byte arr[]=new byte[1024];
    	arr=b.getBytes(1,(int)b.length());
    	newpath=newpath+rs1.getString(1);
    	FileOutputStream fos2=new FileOutputStream(newpath);
    	fos2.write(arr);
    	fos2.close();
    	BufferedImage img=ImageIO.read(new File(newpath));
        ImageIcon icon=new ImageIcon(img);
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    }
    PreparedStatement stmt4 = con.prepareStatement("truncate table temp");
    stmt4.executeUpdate();
	}
	catch(Exception e)
	{
		System.out.println(e.getMessage());
	}
	}
	public static void main(String args[]) throws Exception
	{
		
		//JFrame fr1=new JFrame("Input");
	    //fr1.setLayout(new FlowLayout());
	    //fr1.setSize(300,300);
		
		//fr1.add(buttonBrowse);
		//fr1.setVisible(true);
		//buttonBrowse.addActionListener(this);
        //fr1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
/*		JFileChooser chooser= new JFileChooser();
		ImagePreviewPanel preview = new ImagePreviewPanel();
		chooser.setAccessory(preview);
		chooser.addPropertyChangeListener(preview);
		int choice = chooser.showOpenDialog(null);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
		 chooser.setAcceptAllFileFilterUsed(true);
	//	FileFilter filter1 = new ExtensionFileFilter("JPG and JPEG", new String[] { "JPG", "JPEG" });
		//chooser.setFileFilter(imageFilter);

		if (choice != JFileChooser.APPROVE_OPTION) return;

		 chosenFile = chooser.getSelectedFile();*/
		JLabel lbl2=new JLabel();
		BufferedImage img2=ImageIO.read(new File("C:/Users/Siddharth/Desktop/logo.jpg"));
	    ImageIcon icon2=new ImageIcon(img2);
	    lbl2.setIcon(icon2);
		ISE obj=new ISE();
		obj.setTitle("ISE");
		obj.setSize(1366,768);
		obj.add(lbl2);
		obj.setVisible(true);
		/*JFrame fr=new JFrame("Input");
		    fr.setLayout(new FlowLayout());
		    fr.setSize(300,300);
		    JLabel lbl1=new JLabel();
		BufferedImage img1=ImageIO.read(chosenFile);
        ImageIcon icon1=new ImageIcon(img1);
        lbl1.setIcon(icon1);
        fr.add(lbl1);
        fr.setVisible(true);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
        
		/*String f=chosenFile.getPath();
		//File file=new File(f);
		//FileInputStream fis = new FileInputStream(file);
		ImagePHash ih = new ImagePHash();
    	InputStream is = new BufferedInputStream(new FileInputStream(f));
    	String hash=ih.getHash(is);
    	//System.out.println(hash);
    	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    	Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "12345");
    	Statement stmt=con.createStatement();
    	ResultSet rs=stmt.executeQuery("select * from dataset");
    while(rs.next())
    {
    	String h=rs.getString(3);
    	int hamming=HammingDistance(hash,h);
    	//System.out.println("Hamming Distance= "+hamming);
    	if(hamming<=15)
    	{
    		Blob b=rs.getBlob(2);
    		
    		byte arr[]=new byte[1024];
    		arr=b.getBytes(1,(int)b.length());
    		String path="C:/Users/Siddharth/Desktop/temp/"+rs.getString(1);
    		FileOutputStream fos=new FileOutputStream(path);
    		fos.write(arr);
    		fos.close();
    		File f1=new File(path);
    		FileInputStream fis=new FileInputStream(f1);
    		PreparedStatement stmt2=con.prepareStatement("insert into temp values(?,?,?)");
    		stmt2.setString(1, rs.getString(1));
    		stmt2.setBinaryStream(2,fis,(int)f1.length());
    		stmt2.setInt(3,hamming);
    		stmt2.executeUpdate();
    		fis.close();
    		stmt2.close();
    		System.gc();
    		f1.delete();
    	}
    	
    }
   String newpath="C:/Users/Siddharth/Desktop/output/";
    Statement stmt3 =con.createStatement();
    ResultSet rs1=stmt3.executeQuery("select * from temp order by hamming");
    JFrame frame=new JFrame("ISE");
    frame.setBounds(500, 200, 647, 418);
    //frame.setLocationRelativeTo(fr);
    frame.setLayout(new FlowLayout());
    frame.setSize(400,400);
    JLabel lbl=new JLabel();
    while(rs1.next())
    {
    	Blob b =rs1.getBlob(2);
    	byte arr[]=new byte[1024];
    	arr=b.getBytes(1,(int)b.length());
    	newpath=newpath+rs1.getString(1);
    	FileOutputStream fos2=new FileOutputStream(newpath);
    	fos2.write(arr);
    	fos2.close();
    	BufferedImage img=ImageIO.read(new File(newpath));
        ImageIcon icon=new ImageIcon(img);
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    }
    PreparedStatement stmt4 = con.prepareStatement("truncate table temp");
    stmt4.executeUpdate();*/
    
}
	static int HammingDistance(String hash1,String hash2)
	{
		int hd=0;
		for(int i=0;i<hash1.length();i++)
		{
			if(hash1.charAt(i)!=hash2.charAt(i))
			{
				hd=hd+1;
			}
			else
			{
				hd=hd+0;
			}
		}
		return hd;	
	}
}

