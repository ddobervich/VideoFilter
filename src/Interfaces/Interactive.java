package Interfaces;

import core.DImage;

public interface Interactive {
    /***
     * Action you wish to perform when the mouse is clicked or key is pressed.
     * For example, on mouseClicked you could store the color of the pixel where
     * the mouse was clicked to set a threshold or a target color.
     *
     * Note: This may behave in unexpected ways if your filtered image is a different size from your original.
     *
     * @param mouseX the x coordiante where the mouse was clicked ( with (0, 0) the upper left corner of img )
     * @param mouseY the y coordiante where the mouse was clicked (with (0, 0) the upper left corner of img )
     * @param original the original unfiltered image that was clicked.
     * @param filtered the output from running processImg on the original image.
     */
    public void mouseClicked(int mouseX, int mouseY, DImage original, DImage filtered);
    public void keyPressed(char key);
}
