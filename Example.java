import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.lang.Math.*;
import java.util.Arrays;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.
public class Example extends JFrame {
    JFrame thumbnailFrame, reFrame;
    JFrame mainFrame;
    JFrame newFrame;
    JButton mip_button1; //an example button to switch to MIP mode
    JButton mip_button2; //an example button to switch to MIP mode
    JButton mip_button3; //an example button to switch to MIP mode
    JButton resize_button1; //an example button to switch to MIP mode
    JButton resize_button2; //an example button to switch to MIP mode
    JButton resize_button3; //an example button to switch to MIP mode
    JButton thumbnails1; //an example button to show thumbnail side images of the splices
    JButton thumbnails2; //an example button to show thumbnail side images of the splices
    JButton thumbnails3; //an example button to show thumbnail side images of the splices
    JButton histogram1;
    JButton histogram2;
    JButton histogram3;
    JLabel image_icon1; //using JLabel to display an image check online documentation)
    JLabel image_icon2; //using JLabel to display an image check online documentation)
    JLabel image_icon3; //using JLabel to display an image check online documentation)
    JLabel image_iconBig; //using JLabel to display an image check online documentation)
    JLabel[] thumbnailTop = new JLabel[256];
    JLabel[] thumbnailFront = new JLabel[256];
    JLabel[] thumbnailSide = new JLabel[256];
    int[] histogram;
    int[] t;
    int[] mapping;
    JLabel bigThumb = new JLabel();
    JLabel instruction;


	JSlider zslice_slider, yslice_slider, xslice_slider, resize_slider1, resize_slider2, resize_slider3, resize_sliderMain; //sliders to step through the slices z and y directions) (remember 113 slices in z direction 0-112)
    BufferedImage image1, image2, image3, iconImage, imageBig; //storing the image in memory
	short cthead[][][]; //store the 3D volume data set
	short min, max; //min/max value in the 3D volume data set
    short myMaximum;

    /*
        This function sets up the GUI and reads the data set
    */
    public void Example() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
		File file = new File("cthead/CThead");

        //Create a BufferedImage to store the image data
		image1 = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
		image2 = new BufferedImage(256, 113, BufferedImage.TYPE_3BYTE_BGR);
        image3 = new BufferedImage(256, 113, BufferedImage.TYPE_3BYTE_BGR);

        imageBig = new BufferedImage(512, 512, BufferedImage.TYPE_3BYTE_BGR);

		//Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find the equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; //loop through the 3D data set

		min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
		short read; //value read in
		int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

		cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
		//loop through the data reading it in
		for (k=0; k<113; k++) {
			for (j=0; j<256; j++) {
				for (i=0; i<256; i++) {
					//because the Endianess is wrong, it needs to be read byte at a time and swapped
					b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types (C++ is so much easier!)
					b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types (C++ is so much easier!)
					read=(short)((b2<<8) | b1); //and swizzle the bytes around
					if (read<min) min=read; //update the minimum
					if (read>max) max=read; //update the maximum
					cthead[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
				}
			}
		}
		System.out.println(min+" "+max); //diagnostic - for CThead this should be -1117, 2248
		//(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
		//therefore histogram equalization would be a good thing

        // Set up the simple GUI
        // First the container:

        // Container pane = getContentPane();
        // pane.setLayout(new GridLayout(3, 6));

        mainFrame = new JFrame();
        mainFrame.setSize(800,1920);
        mainFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Container pane = mainFrame.getContentPane();
        pane.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;

        // Then our image (as a label icon)
        image_icon1 = new JLabel(new ImageIcon(image1));
        c.weightx = 500;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(image_icon1, c);

        // Then the invert button
        mip_button1 = new JButton("MIP1");
        mip_button1.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 0;
        c.gridy = 1;
        pane.add(mip_button1, c);

        instruction = new JLabel("Click Thumbnail to see actual size");

        zslice_slider = new JSlider(0,112);
        //Add labels (y slider as example)
        zslice_slider.setMajorTickSpacing(50);
        zslice_slider.setMinorTickSpacing(10);
        zslice_slider.setPaintTicks(true);
        zslice_slider.setPaintLabels(true);
        zslice_slider.setValue(0);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(zslice_slider, c);

        thumbnails1 = new JButton("Thumbnails 1");
        thumbnails1.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(thumbnails1, c);

        histogram1 = new JButton("Histogram Equalization Top");
        histogram1.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 0;
        c.gridy = 4;
        pane.add(histogram1, c);

        resize_slider1 = new JSlider(1,512);
        //Add labels (y slider as example)
        resize_slider1.setMajorTickSpacing(100);
        resize_slider1.setMinorTickSpacing(10);
        resize_slider1.setPaintTicks(true);
        resize_slider1.setPaintLabels(true);
        resize_slider1.setValue(512);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 3;
        c.gridy = 3;
        pane.add(resize_slider1, c);


        image_icon2 = new JLabel(new ImageIcon(image2));
        c.weightx = 500;
        c.gridx = 1;
        c.gridy = 0;
        pane.add(image_icon2, c);

        mip_button2 = new JButton("MIP2");
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 1;
        c.gridy = 1;
        pane.add(mip_button2, c);

        //Yslice slider
        yslice_slider = new JSlider(0,255);
        yslice_slider.setMajorTickSpacing(100);
        yslice_slider.setMinorTickSpacing(10);
        yslice_slider.setPaintTicks(true);
        yslice_slider.setPaintLabels(true);
        yslice_slider.setValue(0);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 1;
        c.gridy = 2;
        pane.add(yslice_slider, c);

        thumbnails2 = new JButton("Thumbnails 2");
        thumbnails2.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 1;
        c.gridy = 3;
        pane.add(thumbnails2, c);

        histogram2 = new JButton("Histogram Equalization Front");
        histogram2.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 1;
        c.gridy = 4;
        pane.add(histogram2, c);

        resize_slider2 = new JSlider(1,512);
        //Add labels (y slider as example)
        resize_slider2.setMajorTickSpacing(100);
        resize_slider2.setMinorTickSpacing(10);
        resize_slider2.setPaintTicks(true);
        resize_slider2.setPaintLabels(true);
        resize_slider2.setValue(512);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 3;
        c.gridy = 4;
        pane.add(resize_slider2, c);

        image_icon3 = new JLabel(new ImageIcon(image3));
        c.weightx = 500;
        c.gridx = 2;
        c.gridy = 0;
        pane.add(image_icon3, c);

        mip_button3 = new JButton("MIP3");
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 2;
        c.gridy = 1;
        pane.add(mip_button3, c);

        //Xslice slider
        xslice_slider = new JSlider(0,255);
        xslice_slider.setMajorTickSpacing(50);
        xslice_slider.setMinorTickSpacing(10);
        xslice_slider.setPaintTicks(true);
        xslice_slider.setPaintLabels(true);
        xslice_slider.setValue(0);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 2;
        c.gridy = 2;
        pane.add(xslice_slider, c);

        thumbnails3 = new JButton("Thumbnails 3");
        thumbnails3.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 2;
        c.gridy = 3;
        pane.add(thumbnails3, c);

        histogram3 = new JButton("Histogram Equalization Side");
        histogram3.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 2;
        c.gridy = 4;
        pane.add(histogram3, c);

        resize_slider3 = new JSlider(1,512);
        //Add labels (y slider as example)
        resize_slider3.setMajorTickSpacing(100);
        resize_slider3.setMinorTickSpacing(10);
        resize_slider3.setPaintTicks(true);
        resize_slider3.setPaintLabels(true);
        resize_slider3.setValue(512);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 3;
        c.gridy = 5;
        pane.add(resize_slider3, c);

        image_iconBig = new JLabel(new ImageIcon(imageBig));
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 3;
        c.gridy = 0;
        pane.add(image_iconBig, c);

        resize_button1 = new JButton("Show in resizer");
        resize_button1.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 0;
        c.gridy = 5;
        pane.add(resize_button1, c);
        resize_button2 = new JButton("Show in resizer");
        resize_button2.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 1;
        c.gridy = 5;
        pane.add(resize_button2, c);
        resize_button3 = new JButton("Show in resizer");
        resize_button3.setSize(10, 50);
        c.weightx = 500;
        c.weighty = 500;
        c.gridx = 2;
        c.gridy = 5;
        pane.add(resize_button3, c);

        //see
		//http://download.oracle.com/javase/1.4.2/docs/api/javax/swing/JSlider.html
		//for documentation (e.g. how to get the value, how to display vertically if you want)

        // Now all the handlers class
        GUIEventHandler handler = new GUIEventHandler();

        // associate appropriate handlers
        mip_button1.addActionListener(handler);
        mip_button2.addActionListener(handler);
        mip_button3.addActionListener(handler);

        thumbnails1.addActionListener(handler);
        thumbnails2.addActionListener(handler);
        thumbnails3.addActionListener(handler);

		yslice_slider.addChangeListener(handler);
        zslice_slider.addChangeListener(handler);
        xslice_slider.addChangeListener(handler);

        resize_slider1.addChangeListener(handler);
        resize_slider2.addChangeListener(handler);
        resize_slider3.addChangeListener(handler);

        histogram1.addActionListener(handler);
        histogram2.addActionListener(handler);
        histogram3.addActionListener(handler);

        resize_button1.addActionListener(handler);
        resize_button2.addActionListener(handler);
        resize_button3.addActionListener(handler);

        resize_sliderMain = new JSlider(1,512);
        resize_sliderMain.setMajorTickSpacing(100);
        resize_sliderMain.setMinorTickSpacing(10);
        resize_sliderMain.setPaintTicks(true);
        resize_sliderMain.setPaintLabels(true);
        resize_sliderMain.setValue(512);
        resize_sliderMain.addChangeListener(handler);

        // ... and display everything
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setSize(1920,800);

        mainFrame.setVisible(true);
    }

    // public void openResizer(){
    //     reFrame = new JFrame();
    //     reFrame.setVisible(true);
    //     reFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    //     reFrame.getContentPane().add(image_iconBig = new JLabel(new ImageIcon(imageBig)));

    //     //reFrame.add(resize_sliderMain);


    //     reFrame.pack();
    // }

    /*
        This is the event handler for the application
    */
    private class GUIEventHandler implements ActionListener, ChangeListener {

		//Change handler (e.g. for sliders)
         public void stateChanged(ChangeEvent e) {
            if (e.getSource() == zslice_slider) {
                System.out.println(zslice_slider.getValue());
                image1=sliderMIP(image1);
                image_icon1.setIcon(new ImageIcon(image1));
            }
            if (e.getSource() == yslice_slider) {
                image2=sliderMIP2(image2);
                image_icon2.setIcon(new ImageIcon(image2));
            }
            if (e.getSource() == xslice_slider) {
                image3=sliderMIP3(image3);
                image_icon3.setIcon(new ImageIcon(image3));
            }
            if (e.getSource() == resize_slider1) {
                BufferedImage image = getThumbnailTop(image1, zslice_slider.getValue(), resize_slider1.getValue(), resize_slider1.getValue());
                image_iconBig.setIcon(new ImageIcon(image));
            }
            if (e.getSource() == resize_slider2) {
                BufferedImage image = getThumbnailFront(image2, yslice_slider.getValue(), resize_slider2.getValue(), resize_slider2.getValue());
                image_iconBig.setIcon(new ImageIcon(image));
            }
            if (e.getSource() == resize_slider3) {
                BufferedImage image = getThumbnailSide(image3, xslice_slider.getValue(), resize_slider3.getValue(), resize_slider3.getValue());
                image_iconBig.setIcon(new ImageIcon(image));
            }

		}

        //action handlers (e.g. for buttons)
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == mip_button1) {
                //e.g. do something to change the image here
                // Call MIP function
                image1 = MIP(image1);
                // Update image
                image_icon1.setIcon(new ImageIcon(image1));
            }
            if (event.getSource() == mip_button2) {
                //e.g. do something to change the image here
                // Call MIP function
                image2 = MIP2(image2);
                // Update image
                image_icon2.setIcon(new ImageIcon(image2));
            }
            if (event.getSource() == mip_button3) {
                //e.g. do something to change the image here
                // Call MIP function
                image3 = MIP3(image3);
                // Update image
                image_icon3.setIcon(new ImageIcon(image3));
            }
            if (event.getSource() == thumbnails1) {
                showThumbnailsTop();
                for(int n = 0; n<256; n++) {
                    thumbnailTop[n].addMouseListener(new MouseAdapter() {
                      public void mouseClicked(MouseEvent me) {
                        newFrame = new JFrame();
                        newFrame.setVisible(true);
                        newFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        JLabel labelClicked = (JLabel) me.getSource();
                        BufferedImage foobar = getThumbnailTop(image1, Arrays.asList(thumbnailTop).indexOf(labelClicked), 256, 256);
                        bigThumb.setIcon(new ImageIcon(foobar));
                        newFrame.add(bigThumb);
                        newFrame.pack();
                        mainFrame.setLocationRelativeTo(null);
                      }
                    });
                }
            }
            if (event.getSource() == thumbnails2) {
                showThumbnailsFront();
                for(int n =0; n<256; n++) {
                    thumbnailFront[n].addMouseListener(new MouseAdapter() {
                      public void mouseClicked(MouseEvent me) {
                        newFrame = new JFrame();
                        newFrame.setVisible(true);
                        newFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

                        JLabel labelClicked = (JLabel) me.getSource();
                        BufferedImage foobar = getThumbnailFront(image2, Arrays.asList(thumbnailFront).indexOf(labelClicked), 113, 256);
                        bigThumb.setIcon(new ImageIcon(foobar));
                        newFrame.add(bigThumb);

                        newFrame.pack();
                        mainFrame.setLocationRelativeTo(null);
                      }
                    });
                }
            }
            if (event.getSource() == thumbnails3) {
                showThumbnailsSide();
                for(int n =0; n<256; n++) {
                    thumbnailSide[n].addMouseListener(new MouseAdapter() {
                      public void mouseClicked(MouseEvent me) {
                        newFrame = new JFrame();
                        newFrame.setVisible(true);
                        newFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

                        JLabel labelClicked = (JLabel) me.getSource();
                        BufferedImage foobar = getThumbnailSide(image3, Arrays.asList(thumbnailSide).indexOf(labelClicked),113, 256);
                        bigThumb.setIcon(new ImageIcon(foobar));
                        newFrame.add(bigThumb);

                        newFrame.pack();
                        mainFrame.setLocationRelativeTo(null);
                      }
                    });
                }
            }
            if (event.getSource() == histogram1) {
                BufferedImage image = makeHistogram1(image1);
                image = mapImage1(image);
                image_icon1.setIcon(new ImageIcon(image));
            }
            if (event.getSource() == histogram2) {
                BufferedImage image = makeHistogram1(image2);
                image = mapImage2(image);
                image_icon2.setIcon(new ImageIcon(image));
            }
            if (event.getSource() == histogram3) {
                BufferedImage image = makeHistogram1(image3);
                image = mapImage3(image);
                image_icon3.setIcon(new ImageIcon(image));
            }
            if (event.getSource() == resize_button1) {
                imageBig = resizeTop(image1);
                image_iconBig.setIcon(new ImageIcon(imageBig));
            }
            if (event.getSource() == resize_button2) {
                imageBig = resizeFrout(image2);
                image_iconBig.setIcon(new ImageIcon(imageBig));
            }
            if (event.getSource() == resize_button3) {
                imageBig = resizeSide(image3);
                image_iconBig.setIcon(new ImageIcon(imageBig));
            }
        }
    }

    /*
        This function will return a pointer to an array
        of bytes which represent the image data in memory.
        Using such a pointer allows fast access to the image
        data for processing (rather than getting/setting
        individual pixels)
    */
    public static byte[] GetImageData(BufferedImage image) {
        WritableRaster WR=image.getRaster();
        DataBuffer DB=WR.getDataBuffer();
        if (DB.getDataType() != DataBuffer.TYPE_BYTE)
            throw new IllegalStateException("That's not of type byte");
        return ((DataBufferByte) DB).getData();
    }



    public BufferedImage makeHistogram1(BufferedImage image) {
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;

        histogram = new int[3366];
        float t_i = 0f;
        mapping = new int[3366];
        int index;

        for (j=0; j < 256; j++){
            for (i=0; i < 256; i++){
                for (k=0; k < 113; k++){
                    index=cthead[k][j][i]-min;
                    histogram[index]++;
                }
            }
        }
        for (i=0; i < max-min+1; i++){
            t_i += histogram[i];
            mapping[i] = (int)(255.0f*(t_i/(7405568.0)));
        }
        System.out.println(t_i);
        return image;
    }

    public BufferedImage mapImage1(BufferedImage image){
    int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[zslice_slider.getValue()][j][i];
                col=mapping[datum-min];
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*w]=(byte) col;
                }
            }
        }
        return image;
    }
    public BufferedImage mapImage2(BufferedImage image){
    int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][yslice_slider.getValue()][i];
                col=mapping[datum-min];
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*w]=(byte) col;
                }
            }
        }
        return image;
    }
    public BufferedImage mapImage3(BufferedImage image){
    int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][i][xslice_slider.getValue()];
                col=mapping[datum-min];
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*w]=(byte) col;
                }
            }
        }
        return image;
    }


    /*
        This function shows how to carry out an operation on an image.
        It obtains the dimensions of the image, and then loops through
        the image carrying out the copying of a slice of data into the
    	image.
    */
    public BufferedImage sliderMIP(BufferedImage image) {
        //Get image dimensions, and declare loop variables
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        //Obtain pointer to data for fast processing
        byte[] data = GetImageData(image);
		float col;
		short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
				//at this point (i,j) is a single pixel in the image
				//here you would need to do something to (i,j) if the image size
				//does not match the slice size (e.g. during an image resizing operation
				//If you don't do this, your j,i could be outside the array bounds
				//In the framework, the image is 256x256 and the data set slices are 256x256
				//so I don't do anything - this also leaves you something to do for the assignment
				datum=cthead[zslice_slider.getValue()][j][i]; //get values from slice 76 (change this in your assignment)
				//calculate the colour by performing a mapping from [min,max] -> [0,255]
				col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
					//and now we are looping through the bgr components of the pixel
					//set the colour component c of pixel (i,j)
					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop
        return image;
    }

    public BufferedImage sliderMIP2(BufferedImage image) {
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;
        for (j=0; j < h; j++) {
            for (i=0; i < w; i++) {
                datum=cthead[j][yslice_slider.getValue()][i];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*w]=(byte) col;
                }
            }
        }
        return image;
    }

    public BufferedImage sliderMIP3(BufferedImage image) {
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][i][xslice_slider.getValue()];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*w]=(byte) col;
                }
            }
        }
        return image;
    }

    public BufferedImage MIP(BufferedImage image) {
        //Get image dimensions, and declare loop variables
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        //Obtain pointer to data for fast processing
        byte[] data = GetImageData(image);
        float col;
        short datum = 0;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                myMaximum = 0;
                for (k=0; k < 113; k++) {
                    //at this point (i,j) is a single pixel in the image
                    //here you would need to do something to (i,j) if the image size
                    //does not match the slice size (e.g. during an image resizing operation
                    //If you don't do this, your j,i could be outside the array bounds
                    //In the framework, the image is 256x256 and the data set slices are 256x256
                    //so I don't do anything - this also leaves you something to do for the assignment
                    datum=cthead[k][j][i]; //get values from slice 76 (change this in your assignment)
                    if (myMaximum < datum){
                        myMaximum = datum;
                    }
                    //calculate the colour by performing a mapping from [min,max] -> [0,255]
                    col=(255.0f*((float)myMaximum-(float)min)/((float)(max-min)));
                    for (c=0; c<3; c++) {
                        //and now we are looping through the bgr components of the pixel
                        //set the colour component c of pixel (i,j)
                        data[c+3*i+3*j*w] = (byte)col;
                    } // colour loop
                }
            } // column loop
        } // row loop
        return image;
    }

    public BufferedImage MIP2(BufferedImage image) {
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;

        for (j=0; j < h; j++) {
            for (i=0; i < w; i++) {
                myMaximum = 0;
                for (k=0; k < 256; k++) {
                    datum=cthead[j][k][i];
                    if (myMaximum < datum){
                        myMaximum = datum;
                    }
                    col=(255.0f*((float)myMaximum-(float)min)/((float)(max-min)));
                    for (c=0; c<3; c++) {
                        data[c+3*i+3*j*w]=(byte) col;
                    }
                }
            }

        }
        return image;
    }

    public BufferedImage MIP3(BufferedImage image) {
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        byte[] data = GetImageData(image);
        float col;
        short datum;
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                myMaximum = 0;
                for (k=0; k<256; k++) {
                    datum=cthead[j][i][k];
                    if (myMaximum < datum){
                        myMaximum = datum;
                    }
                    col=(255.0f*((float)myMaximum-(float)min)/((float)(max-min)));
                    for (c=0; c<3; c++) {
                        data[c+3*i+3*j*w]=(byte) col;
                    }
                }
            }
        }
        return image;
    }

    public BufferedImage resizeTop(BufferedImage image){
        int w = image.getWidth(), h = image.getHeight(), i, j, c, k;
        int y, x, xA = w, xB = 512, yA = h, yB = 512;
        BufferedImage newImage = new BufferedImage(yB, xB, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = GetImageData(newImage);
        float col;
        short datum;

        float aI[][][] = new float[h][w][3];
        float bI[][][] = new float[yB][xB][3];

        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[zslice_slider.getValue()][j][i];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    aI[j][i][c] = col;
                }
            }
        }

        for (j=0; j<yB-2; j++) {
            for (i=0; i<xB-2; i++) {
                for (c=0; c<3; c++) {
                    float foo = (float)j * (float)yA / (float)yB;
                    float bar = (float)i * (float)xA / (float)xB;

                    int y1 = (int)Math.floor(foo);
                    int y2 = (int)y1 +1;
                    int x1 = (int)Math.floor(bar);
                    int x2 = (int)x1 +1;

                    if (y2 < h){
                        float a = aI[y2][x1][c];
                        float b = aI[y1][x1][c];
                        float c1 = aI[y1][x2][c];
                        float d = aI[y2][x2][c];

                        float f = b + (c1 - b) * ((bar-x1)/(x2-x1));
                        float e = a + (d - a) * ((bar-x1)/(x2-x1));

                        float g = f + (e - f) * ((foo-y1)/(y2-y1));
                        bI[j][i][c] = g;
                    }
                }
            }
        }

        for (j=0; j<yB; j++) {
            for (i=0; i<xB; i++) {
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*xB] = (byte)bI[j][i][c];
                }
            }
        }
        return newImage;
    }
    public BufferedImage resizeFrout(BufferedImage image){
        int w = image.getWidth(), h = image.getHeight(), i, j, c, k;
        int y, x, xA = w, xB = 512, yA = h, yB = 512;
        BufferedImage newImage = new BufferedImage(xB, yB, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = GetImageData(newImage);
        float col;
        short datum;

        //System.out.println(h+", "+w);

        float aI[][][] = new float[h][w][3];
        float bI[][][] = new float[xB][yB][3];

        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][yslice_slider.getValue()][i];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    aI[j][i][c] = col;
                }
            }
        }

        for (j=0; j<yB-2; j++) {
            for (i=0; i<xB-2; i++) {
                for (c=0; c<3; c++) {
                    float foo = (float)j * (float)yA / (float)yB;
                    float bar = (float)i * (float)xA / (float)xB;

                    int y1 = (int)Math.floor(foo);
                    int y2 = (int)y1 +1;
                    int x1 = (int)Math.floor(bar);
                    int x2 = (int)x1 +1;
                    if (y2 < h){
                        float a = aI[y2][x1][c];
                        float b = aI[y1][x1][c];
                        float c1 = aI[y1][x2][c];
                        float d = aI[y2][x2][c];

                        float f = b + (c1 - b) * ((bar-x1)/(x2-x1));
                        float e = a + (d - a) * ((bar-x1)/(x2-x1));

                        float g = f + (e - f) * ((foo-y1)/(y2-y1));
                        bI[j][i][c] = g;
                    }
                }
            }
        }

        for (j=0; j<yB; j++) {
            for (i=0; i<xB; i++) {
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*xB] = (byte)bI[j][i][c];
                }
            }
        }
        return newImage;
    }
    public BufferedImage resizeSide(BufferedImage image){
        int w = image.getWidth(), h = image.getHeight(), i, j, c, k;
        int y, x, xA = w, xB = 512, yA = h, yB = 512;
        BufferedImage newImage = new BufferedImage(xB, yB, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = GetImageData(newImage);
        float col;
        short datum;

        float aI[][][] = new float[h][w][3];
        float bI[][][] = new float[xB][yB][3];

        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][i][xslice_slider.getValue()];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    aI[j][i][c] = col;
                }
            }
        }

        for (j=0; j<yB-2; j++) {
            for (i=0; i<xB-2; i++) {
                for (c=0; c<3; c++) {
                    float foo = (float)j * (float)yA / (float)yB;
                    float bar = (float)i * (float)xA / (float)xB;

                    int y1 = (int)Math.floor(foo);
                    int y2 = (int)y1 +1;
                    int x1 = (int)Math.floor(bar);
                    int x2 = (int)x1 +1;

                    if (y2 < h){
                        float a = aI[y2][x1][c];
                        float b = aI[y1][x1][c];
                        float c1 = aI[y1][x2][c];
                        float d = aI[y2][x2][c];

                        float f = b + (c1 - b) * ((bar-x1)/(x2-x1));
                        float e = a + (d - a) * ((bar-x1)/(x2-x1));

                        float g = f + (e - f) * ((foo-y1)/(y2-y1));
                        bI[j][i][c] = g;
                    }
                }
            }
        }

        for (j=0; j<yB; j++) {
            for (i=0; i<xB; i++) {
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*xB] = (byte)bI[j][i][c];
                }
            }
        }
        return newImage;
    }

    public BufferedImage getThumbnailTop(BufferedImage image, int sliceNum, int yB, int xB){
        int w = image.getWidth(), h = image.getHeight(), i, j, c, k;
        int y, x, xA = w, yA = h;
        BufferedImage newImage = new BufferedImage(xB, yB, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = GetImageData(newImage);
        float col;
        short datum;

        float aI[][][] = new float[h][w][3];
        float bI[][][] = new float[xB][yB][3];

        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[sliceNum][j][i];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    aI[j][i][c] = col;
                }
            }
        }

        for (j=0; j<yB-2; j++) {
            for (i=0; i<xB-2; i++) {
                for (c=0; c<3; c++) {
                    float foo = (float)j * (float)yA / (float)yB;
                    float bar = (float)i * (float)xA / (float)xB;

                    int y1 = (int)Math.floor(foo);
                    int y2 = (int)y1 +1;
                    int x1 = (int)Math.floor(bar);
                    int x2 = (int)x1 +1;

                    if (y2 < h){
                        float a = aI[y2][x1][c];
                        float b = aI[y1][x1][c];
                        float c1 = aI[y1][x2][c];
                        float d = aI[y2][x2][c];

                        float f = b + (c1 - b) * ((bar-x1)/(x2-x1));
                        float e = a + (d - a) * ((bar-x1)/(x2-x1));

                        float g = f + (e - f) * ((foo-y1)/(y2-y1));
                        bI[j][i][c] = g;
                    }
                }
            }
        }

        for (j=0; j<yB; j++) {
            for (i=0; i<xB; i++) {
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*xB] = (byte)bI[j][i][c];
                }
            }
        }
        return newImage;
    }

    public void showThumbnailsTop(){
        thumbnailFrame = new JFrame();
        thumbnailFrame.setVisible(true);
        thumbnailFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        thumbnailFrame.setSize(1920,80);


        JPanel p = new JPanel();

        JScrollPane scroll = new JScrollPane(p);
        thumbnailFrame.add(scroll);
        p.add(instruction);


        for(int i =0; i<112; i++) {
            BufferedImage iconImageNew = getThumbnailTop(image1, i, 70, 70);
            thumbnailTop[i] = new JLabel(new ImageIcon(iconImageNew));
            p.add(thumbnailTop[i]);
        }
        thumbnailFrame.pack();
    }

    public BufferedImage getThumbnailFront(BufferedImage image, int sliceNum, int yB, int xB){
        int w = image.getWidth(), h = image.getHeight(), i, j, c, k;
        int y, x, xA = w, yA = h;
        BufferedImage newImage = new BufferedImage(xB, yB, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = GetImageData(newImage);
        float col;
        short datum;

        float aI[][][] = new float[yA][xA][3];
        float bI[][][] = new float[yB][xB][3];

        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][sliceNum][i];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    aI[j][i][c] = col;
                }
            }
        }

        for (j=0; j<yB-2; j++) {
            for (i=0; i<xB-2; i++) {
                for (c=0; c<3; c++) {
                    float foo = (float)j * (float)yA / (float)yB;
                    float bar = (float)i * (float)xA / (float)xB;

                    int y1 = (int)Math.floor(foo);
                    int y2 = (int)y1 +1;
                    int x1 = (int)Math.floor(bar);
                    int x2 = (int)x1 +1;

                    if (y2 < h){
                        float a = aI[y2][x1][c];
                        float b = aI[y1][x1][c];
                        float c1 = aI[y1][x2][c];
                        float d = aI[y2][x2][c];

                        float f = b + (c1 - b) * ((bar-x1)/(x2-x1));
                        float e = a + (d - a) * ((bar-x1)/(x2-x1));

                        float g = f + (e - f) * ((foo-y1)/(y2-y1));
                        bI[j][i][c] = g;
                    }

                }
            }
        }

        for (j=0; j<yB; j++) {
            for (i=0; i<xB; i++) {
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*xB] = (byte)bI[j][i][c];
                }
            }
        }
        return newImage;

    }

    public void showThumbnailsFront(){
        thumbnailFrame = new JFrame();
        thumbnailFrame.setVisible(true);
        thumbnailFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        thumbnailFrame.setSize(1920,80);


        JPanel p = new JPanel();

        JScrollPane scroll = new JScrollPane(p);
        thumbnailFrame.add(scroll);
        p.add(instruction);


        for(int i =0; i<256; i++) {
            BufferedImage iconImageNew = getThumbnailFront(image2, i, 70, 70);
            thumbnailFront[i] = new JLabel(new ImageIcon(iconImageNew));
            p.add(thumbnailFront[i]);
        }
        thumbnailFrame.pack();
    }

    public BufferedImage getThumbnailSide(BufferedImage image, int sliceNum, int yB, int xB){
        int w = image.getWidth(), h = image.getHeight(), i, j, c, k;
        int y, x, xA = w, yA = h;
        BufferedImage newImage = new BufferedImage(xB, yB, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = GetImageData(newImage);
        float col;
        short datum;


        float aI[][][] = new float[yA][xA][3];
        float bI[][][] = new float[yB][xB][3];

        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                datum=cthead[j][i][sliceNum];
                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    aI[j][i][c] = col;
                }
            }
        }

        for (j=0; j<yB-2; j++) {
            for (i=0; i<xB-2; i++) {
                for (c=0; c<3; c++) {
                    float foo = (float)j * (float)yA / (float)yB;
                    float bar = (float)i * (float)xA / (float)xB;

                    int y1 = (int)Math.floor(foo);
                    int y2 = (int)y1 +1;
                    int x1 = (int)Math.floor(bar);
                    int x2 = (int)x1 +1;

                    if (y2 < h){
                        float a = aI[y2][x1][c];
                        float b = aI[y1][x1][c];
                        float c1 = aI[y1][x2][c];
                        float d = aI[y2][x2][c];

                        float f = b + (c1 - b) * ((bar-x1)/(x2-x1));
                        float e = a + (d - a) * ((bar-x1)/(x2-x1));

                        float g = f + (e - f) * ((foo-y1)/(y2-y1));
                        bI[j][i][c] = g;
                    }
                }
            }
        }

        for (j=0; j<yB; j++) {
            for (i=0; i<xB; i++) {
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*xB] = (byte)bI[j][i][c];
                }
            }
        }

        return newImage;
    }

    public void showThumbnailsSide(){
        thumbnailFrame = new JFrame();
        thumbnailFrame.setVisible(true);
        thumbnailFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        thumbnailFrame.setSize(1920,80);

        JPanel p = new JPanel();

        JScrollPane scroll = new JScrollPane(p);
        thumbnailFrame.add(scroll);
        p.add(instruction);

        for(int i =0; i<256; i++) {
            BufferedImage iconImageNew = getThumbnailSide(image3, i, 70, 70);
            thumbnailSide[i] = new JLabel(new ImageIcon(iconImageNew));
            p.add(thumbnailSide[i]);
        }
        thumbnailFrame.pack();
    }


    public static void main(String[] args) throws IOException {

       Example e = new Example();
       e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       e.Example();
    }
}