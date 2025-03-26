package core;

import processing.core.PApplet;
import processing.core.PImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
// TODO: comment this class

public class DImage {
    private PImage img;
    private int width, height;
    private ColorComponents2d channels;

    public DImage(int width, int height) {
        img = new PImage(width, height);
        this.width = width;
        this.height = height;
    }

    public DImage(PImage img) {
        this.img = img;
        this.width = img.width;
        this.height = img.height;
    }

    public DImage(DImage frame) {
        this.width = frame.width;
        this.height = frame.height;
        this.img = new PImage(width, height);
        this.img.loadPixels();
        System.arraycopy(frame.getColorPixelArray(), 0, this.img.pixels, 0, this.img.pixels.length);
        this.img.updatePixels();
    }

    public DImage(String filename) {
        try {
            BufferedImage img = ImageIO.read(new File(filename));
            if (img == null) {
                System.out.println("Failed to load image");
            }

            PImage newImg = new PImage(img.getWidth(), img.getHeight(), PImage.RGB);

            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int rgb = img.getRGB(x, y);
                    newImg.pixels[y * img.getWidth() + x] = rgb;
                }
            }

            newImg.updatePixels();

            this.img = newImg;
            this.width = newImg.width;
            this.height = newImg.height;
        } catch (IOException e) {
            System.out.println("Error loading image: " + e.getMessage());
            img = new PImage(100, 100);
            width = img.width;
            height = img.height;
        }
    }

    /***
     * Convert hsb color coordinates to rgb.  The values for hue, saturation and value must all be between 0 and 1.
     * @param hsb
     * @return
     */
    public static short[] HSBtoRGB(float[] hsb) {
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        short[] rgbvals = new short[3];
        rgbvals[0] = (short)((rgb >> 16) & 0xFF);
        rgbvals[1] = (short)((rgb >> 8) & 0xFF);
        rgbvals[2] = (short)(rgb & 0xFF);
        return rgbvals;
    }

    public static float[] RGBtoHSB(short[] rgb) {
        return RGBtoHSB(rgb[0], rgb[1], rgb[2]);
    }

    public static float[] RGBtoHSB(short r, short g, short b) {
        return Color.RGBtoHSB(r, g, b, null);
    }

    public int[] getColorPixelArray() {
        img.loadPixels();
        return img.pixels;
    }

    public int[][] getColorPixelGrid() {
        return convertTo2dArray( getColorPixelArray(), this.width, this.height );
    }

    public short[] getBWPixelArray() {
        return convertToShortGreyscale( getColorPixelArray() );
    }

    public short[][] getBWPixelGrid() {
        return convertTo2dArray( getBWPixelArray(), this.width, this.height );
    }

    public void setPixels(int[] pixels) {
        img.loadPixels();
        img.pixels = pixels.clone();    // TODO: does this work?
        img.updatePixels();
    }

    public void setPixels(int[][] pixels) {
        if (img.height != pixels.length || img.width != pixels[0].length) {
            img.resize(pixels[0].length, pixels.length);
        }

        img.loadPixels();
        DImage.fill1dArray(pixels, img.pixels);
        img.updatePixels();
    }

    public void setPixels(short[] pixels) {
        int[] colorPixels = new int[pixels.length];
        fill1dArray(pixels, colorPixels);
        this.setPixels(colorPixels);
    }

    public void setPixels(short[][] pixels) {
        if (img.height != pixels.length || img.width != pixels[0].length) {
            img.resize(pixels[0].length, pixels.length);
        }

        int[] colorPixels = new int[pixels.length*pixels[0].length];
        fill1dArray(pixels, colorPixels);
        this.setPixels(colorPixels);
    }

    // --------------------------------------------------------------------------------------------------------------

    private static final int OPAQUE_ALPHA_VAL = 255;
    private static final int TRANSPARENT_ALPHA_VAL = 0;

    protected static ColorComponents2d getColorComponents2d(int[][] rgbPixels) {
        int h = rgbPixels.length;
        int w = rgbPixels[0].length;
        // TODO: arg check not size 0

        ColorComponents2d out = new ColorComponents2d(w, h);
        int spot = 0;    // index into pix

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                int num = rgbPixels[r][c];
                out.blue[r][c] = (short) (num & 255);
                num = num >> 8;
                out.green[r][c] = (short) (num & 255);
                num = num >> 8;
                out.red[r][c] = (short) (num & 255);
                num = num >> 8;
                out.alpha[r][c] = (short) (num & 255);
            } // for c
        } // for r

        return out;
    }

    private static ColorComponents1d getColorComponents1d(int[][] rgbPixels) {
        int h = rgbPixels.length;
        int w = rgbPixels[0].length;
        // TODO: arg check not size 0

        ColorComponents1d out = new ColorComponents1d(w, h);
        int length = h*w;
        int spot = 0;    // index into pix

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                int num = rgbPixels[r][c];
                out.blue[spot] = (short) (num & 255);
                num = num >> 8;
                out.green[spot] = (short) (num & 255);
                num = num >> 8;
                out.red[spot] = (short) (num & 255);
                num = num >> 8;
                out.alpha[spot] = (short) (num & 255);
                spot++;
            } // for c
        } // for r

        return out;
    }

    private static ColorComponents2d getColorComponents2d(int[] rgbPixels, int w, int h) {
        ColorComponents2d out = new ColorComponents2d(w, h);
        int spot = 0;    // index into pix

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                int num = rgbPixels[spot++];
                out.blue[r][c] = (short) (num & 255);
                num = num >> 8;
                out.green[r][c] = (short) (num & 255);
                num = num >> 8;
                out.red[r][c] = (short) (num & 255);
                num = num >> 8;
                out.alpha[r][c] = (short) (num & 255);
            } // for c
        } // for r

        return out;
    }

    private static ColorComponents1d getColorComponents1d(int[] rgbPixels, int w, int h) {
        ColorComponents1d out = new ColorComponents1d(h, w);
        int length = w*h;
        for (int i=0; i < length; i++) {
            int num = rgbPixels[i];
            out.blue[i] = (short) (num & 255);
            num = num >> 8;
            out.green[i] = (short) (num & 255);
            num = num >> 8;
            out.red[i] = (short) (num & 255);
            num = num >> 8;
            out.alpha[i] = (short) (num & 255);
        }

        return out;
    }

    private static int[] combineColorComponents(ColorComponents2d in) {
        int  pixheight = in.height;
        int pixwidth = in.width;
        int[] pixels = new int[pixwidth * pixheight];

        int tmp;
        int spot = 0;
        for (int r = 0; r < pixheight; r++) {
            for (int c = 0; c < pixwidth; c++) {
                pixels[spot++] = color(in.red[r][c], in.green[r][c], in.blue[r][c], in.alpha[r][c]);
            }
        }

        return pixels;
    }

    private static int[] combineColorComponents(short[] red, short[] green, short[] blue, short[] alpha) {
        // TODO: arg checking
        int[] pixels = new int[red.length];

        int tmp;
        int spot = 0;
        for (int i = 0; i < pixels.length; i++) {
            pixels[spot++] = color(red[i], green[i], blue[i], alpha[i]);
        }

        return pixels;
    }

    private static int[] combineColorComponents(short[] red, short[] green, short[] blue) {
        // TODO: arg checking
        int[] pixels = new int[red.length];

        int tmp;
        int spot = 0;
        for (int i = 0; i < pixels.length; i++) {
            pixels[spot++] = color(red[i], green[i], blue[i], OPAQUE_ALPHA_VAL);
        }

        return pixels;
    }

    private static int[] convertToRGBGreyscale(int[] pixels) {
        int[] out = new int[pixels.length];

        for (int i = 0; i < out.length; i++) {
            out[i] = getGreyValue(pixels[i]);
        }

        return out;
    }

    private static short[] convertToShortGreyscale(int[] pixels) {
        short[] out = new short[pixels.length];

        for (int i = 0; i < out.length; i++) {
            out[i] = getGreyShortVal(pixels[i]);
        }

        return out;
    }

    private static short getGreyShortVal(int color) {
        int num = color;
        int blue = num & 255;
        num = num >> 8;
        int green = num & 255;
        num = num >> 8;
        int red = num & 255;
        num = num >> 8;
        int alpha = num & 255;
        int black = (red + green + blue) / 3;
        return (short)black;
    }

    private static int getOpaqueGreyValue(int color) {
        int num = color;
        int blue = num & 255;
        num = num >> 8;
        int green = num & 255;
        num = num >> 8;
        int red = num & 255;
        num = num >> 8;
        int alpha = num & 255;
        int black = (red + green + blue) / 3;
        num = OPAQUE_ALPHA_VAL;
        num = (num << 8) + black;
        num = (num << 8) + black;
        num = (num << 8) + black;
        return num;
    }

    private static int getGreyValue(int color) {
        int num = color;
        int blue = num & 255;
        num = num >> 8;
        int green = num & 255;
        num = num >> 8;
        int red = num & 255;
        num = num >> 8;
        int alpha = num & 255;
        int black = (red + green + blue) / 3;
        num = alpha;
        num = (num << 8) + black;
        num = (num << 8) + black;
        num = (num << 8) + black;
        return num;
    }

    private static int color(int red, int green, int blue) {
        return color(red, green, blue, OPAQUE_ALPHA_VAL);
    }

    private static int color(int red, int green, int blue, int alpha) {
        int tmp = alpha;
        tmp = tmp << 8;
        tmp += red;
        tmp = tmp << 8;
        tmp += green;
        tmp = tmp << 8;
        tmp += blue;
        return tmp;
    }

    private static int getRed(int color) {
        return (color >> 16) & 255;
    }

    private static int getGreen(int color) {
        return (color >> 8) & 255;
    }

    private static int getBlue(int color) {
        return (color & 255);
    }

    private static int getAlpha(int color) {
        return (color >> 24) & 255;
    }

    private static int[][] convertTo2dArray(int[] pixels, int w, int h) {
        int[][] out = new int[h][w];
        int loc = 0;
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                out[r][c] = pixels[loc];
                loc++;
            }
        }

        return out;
    }

    private static short[][] convertTo2dArray(short[] pixels, int w, int h) {
        short[][] out = new short[h][w];
        int loc = 0;
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                out[r][c] = pixels[loc];
                loc++;
            }
        }

        return out;
    }

    private static void fill1dArray(int[][] vals, int[] arr) {
        if (arr.length != vals.length*vals[0].length) {
            System.err.println("in fill1dArray: different number of elements in 2d and 1d arrays");
        }

        int loc = 0;
        for (int r = 0; r < vals.length; r++) {
            for (int c = 0; c < vals[r].length; c++) {
                arr[loc++] = vals[r][c];
            }
        }

        // no return necessary because we just changed the values in arr
    }

    private static void fill1dArray(short[][] vals, int[] arr) {
        if (arr.length != vals.length*vals[0].length) {
            System.err.println("in fill1dArray: different number of elements in 2d and 1d arrays");
        }

        int loc = 0;
        for (int r = 0; r < vals.length; r++) {
            for (int c = 0; c < vals[r].length; c++) {
                arr[loc++] = shortToRGBGrey(vals[r][c]);
            }
        }

        // no return necessary because we just changed the values in arr
    }

    private static void fill1dArray(short[] vals, int[] arr) {
        for (int i = 0; i < vals.length; i++) {
            arr[i] = shortToRGBGrey(vals[i]);
        }
    }

    private static int shortToRGBGrey(short val) {
        int num = OPAQUE_ALPHA_VAL;
        num = (num << 8) + val;
        num = (num << 8) + val;
        num = (num << 8) + val;
        return num;
    }

    public PImage getPImage() {
        return img;
    }

    public ColorComponents2d getColorChannels() {
        return DImage.getColorComponents2d(this.getColorPixelGrid());
    }

    public short[][] copy(short[][] arr) {
        if (arr == null) {
            return null;
        }

        int height = arr.length;
        int width = (height > 0) ? arr[0].length : 0;
        short[][] copy = new short[height][width];

        for (int i = 0; i < height; i++) {
            System.arraycopy(arr[i], 0, copy[i], 0, width);
        }

        return copy;
    }

    public short[][] getRedChannel() {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        return copy(this.channels.red);
    }

    public short[][] getBlueChannel() {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        return copy(this.channels.blue);
    }

    public short[][] getGreenChannel() {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        return copy(this.channels.green);
    }

    public short[][] getAlphaChannel() {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        return copy(this.channels.alpha);
    }

    public void setRedChannel(short[][] red) {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        this.channels.red = red;
        this.setPixels(combineColorComponents(this.channels));
    }

    public void setGreenChannel(short[][] green) {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        this.channels.green = green;
        this.setPixels(combineColorComponents(this.channels));
    }

    public void setBlueChannel(short[][] blue) {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        this.channels.blue = blue;
        this.setPixels(combineColorComponents(this.channels));
    }

    public void setColorChannels(short[][] red, short[][] green, short[][] blue) {
        if (channels == null) {
            this.channels = getColorChannels();
        }
        this.channels.red = red;
        this.channels.green = green;
        this.channels.blue = blue;
        this.setPixels(combineColorComponents(this.channels));
    }

    public void setColorChannels(short[][] red, short[][] green, short[][] blue, short[][] alpha) {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        this.channels.red = red;
        this.channels.green = green;
        this.channels.blue = blue;
        this.channels.alpha = alpha;
        this.setPixels(combineColorComponents(this.channels));
    }

    public void setAlphaChannel(short[][] alpha) {
        if (channels == null) {
            this.channels = getColorChannels();
        }

        this.channels.alpha = alpha;
        this.setPixels(combineColorComponents(this.channels));
    }

    public int getHeight() {
        if (this.img == null) {
            System.err.println("No image defined for core.DImage");
            return 0;
        }

        return img.height;
    }

    public int getWidth() {
        if (this.img == null) {
            System.err.println("No image defined for core.DImage");
            return 0;
        }

        return img.width;
    }

    public void setColorChannels(ColorComponents2d channels) {
        this.setPixels(DImage.combineColorComponents(channels));
    }

    // Data transfer object
    private static class ColorComponents2d {
        public int width, height;
        public short[][] red, green, blue, alpha;

        public ColorComponents2d(int width, int height) {
            this.width = width;
            this.height = height;
            red = new short[height][width];
            green = new short[height][width];
            blue = new short[height][width];
            alpha = new short[height][width];
        }
    }

    // Data transfer object
    private static class ColorComponents1d {
        public short[] red, green, blue, alpha;
        public int width, height;

        public ColorComponents1d(int width, int height) {
            this.width = width;
            this.height = height;
            int length = width*height;
            red = new short[length];
            green = new short[length];
            blue = new short[length];
            alpha = new short[length];
        }
    }
}
