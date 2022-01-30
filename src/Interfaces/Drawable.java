package Interfaces;

import core.DImage;
import processing.core.PApplet;

public interface Drawable {
    /***
     * A method to allow you to draw on top of the filtered image in the display.
     * This method is run after displaying the filtered image.  You can use
     * processing commands such as rect(...), line(...), ellipse(...) etc. to draw.
     * (0, 0) is located at the upper left of the image (not the display window).
     * Lower right would be (original.getWidth(), original.getHeight()).
     * @param window the PApplet window your filtered image is displayed in
     * @param original the original unfiltered image (in case you want to query pixel values)
     * @param filtered the output from your processImage method
     */
    public void drawOverlay(PApplet window, DImage original, DImage filtered);
}
