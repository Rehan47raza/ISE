import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import javax.imageio.ImageIO;
import java.sql.*;
class ImagePHash {

	private int size = 32;
	private int smallerSize = 8;
	public ImagePHash() {
		initCoefficients();
	}
	public ImagePHash(int size, int smallerSize) {
		this.size = size;
		this.smallerSize = smallerSize;
		
		initCoefficients();
	}
	
	public int distance(String s1, String s2) {
		int counter = 0;
		for (int k = 0; k < s1.length();k++) {
			if(s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	} 
	public String getHash(InputStream is) throws Exception {
		BufferedImage img = ImageIO.read(is);
		
		/* 1. Reduce size. 
		 * Like Average Hash, pHash starts with a small image. 
		 * However, the image is larger than 8x8; 32x32 is a good size. 
		 * This is really done to simplify the DCT computation and not 
		 * because it is needed to reduce the high frequencies.
		 */
		//img = resize(img, size, size);
		
		/* 2. Reduce color. 
		 * The image is reduced to a grayscale just to further simplify 
		 * the number of computations.
		 */
		img = grayscale(img);
		img = resize(img, size, size);
		
		double[][] vals = new double[size][size];
		
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				vals[x][y] = getBlue(img, x, y);
			}
		}
		
		/* 3. Compute the DCT. 
		 * The DCT separates the image into a collection of frequencies 
		 * and scalars. While JPEG uses an 8x8 DCT, this algorithm uses 
		 * a 32x32 DCT.
		 */
		double[][] dctVals = applyDCT(vals);
		
		/* 4. Reduce the DCT. 
		 * This is the magic step. While the DCT is 32x32, just keep the 
		 * top-left 8x8. Those represent the lowest frequencies in the 
		 * picture.
		 */
		/* 5. Compute the average value. 
		 * Like the Average Hash, compute the mean DCT value (using only 
		 * the 8x8 DCT low-frequency values and excluding the first term 
		 * since the DC coefficient can be significantly different from 
		 * the other values and will throw off the average).
		 */
		double total = 0;
		
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				total += dctVals[x][y];
			}
		}
		total -= dctVals[0][0];
		
		double avg = total / (double) ((smallerSize * smallerSize) - 1);
	
		/* 6. Further reduce the DCT. 
		 * This is the magic step. Set the 64 hash bits to 0 or 1 
		 * depending on whether each of the 64 DCT values is above or 
		 * below the average value. The result doesn't tell us the 
		 * actual low frequencies; it just tells us the very-rough 
		 * relative scale of the frequencies to the mean. The result 
		 * will not vary as long as the overall structure of the image 
		 * remains the same; this can survive gamma and color histogram 
		 * adjustments without a problem.
		 */
		String hash = "";
		
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				hash += (dctVals[x][y] > avg?"1":"0");
			}
		}
		return hash;
	}
	
	private BufferedImage resize(BufferedImage image, int width,	int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
	
	private ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

	private BufferedImage grayscale(BufferedImage img) {
        colorConvert.filter(img, img);
        return img;
    }
	
	private static int getBlue(BufferedImage img, int x, int y) {
		return (img.getRGB(x, y)) & 0xff;
	}
	private double[] c;
	private void initCoefficients() {
		c = new double[size];
		
        for (int i=1;i<size;i++) {
            c[i]=1;
        }
        c[0]=1/Math.sqrt(2.0);
    }
	
	private double[][] applyDCT(double[][] f) {
		int N = size;
		
        double[][] F = new double[N][N];
        for (int u=0;u<N;u++) {
          for (int v=0;v<N;v++) {
            double sum = 0.0;
            for (int i=0;i<N;i++) {
              for (int j=0;j<N;j++) {
                sum+=Math.cos(((2*i+1)/(2.0*N))*u*Math.PI)*Math.cos(((2*j+1)/(2.0*N))*v*Math.PI)*(f[i][j]);
              }
            }
            sum*=((c[u]*c[v])/4.0);
            F[u][v] = sum;
          }
        }
        return F;
    }

}
class ImagesInDB
{
	public static void main(String args[]) throws Exception
    {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    	Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "SYSTEM", "12345");
    	File folder = new File("C:/Users/Siddharth/workspace/Image Search Engine/bin/imagestoadd");
    	File[] listOfFiles = folder.listFiles();

    	for (File file : listOfFiles) {
    	    if (file.isFile()){
    	    	String path = file.getPath();
    	    	FileInputStream fis = new FileInputStream(file);
    	    	ImagePHash ih = new ImagePHash();
    	    	InputStream is = new BufferedInputStream(new FileInputStream(path));
    	    	String hash=ih.getHash(is);
    	    	String name = file.getName();
    	    	String newpath = "C:/Users/Siddharth/workspace/Image Search Engine/bin/ImagesAdded/" + name;
    	    	PreparedStatement stmt = con.prepareStatement("insert into dataset values(?,?,?)");
    	    	stmt.setString(1, name);
    	    	stmt.setBinaryStream(2, fis, (int)file.length());
    	    	stmt.setString(3, hash);
    	    	int norows = stmt.executeUpdate();
    	    	System.out.println("Image Name:" + file.getName() + "\nHash Code:" + hash +"\nImage Inserted\nNumber Of Rows Affected:"+norows+ "\n");
    	    	File file2 = new File(newpath);
    	    	InputStream instream = new FileInputStream(file);
    	    	OutputStream outstream = new FileOutputStream(file2);
    	    	byte[] buffer = new byte[1024];
        		
        	    int length;
        	    //copy the file content in bytes 
        	    while ((length = instream.read(buffer)) > 0){
        	  
        	    	outstream.write(buffer, 0, length);
        	 
        	    }
        	    instream.close();
        	    outstream.close();
    	    	fis.close();
    	    }
    	}
    	
    	for (File file : listOfFiles)
    	{
    		if (file.isFile())
    		{
    			System.gc();
    			file.delete();
    		} 
    	}
    	System.out.println("All images moved To \"ImagesAdded\" directory.");
    	con.close();
    }
}