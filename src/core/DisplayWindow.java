package core;

import Interfaces.Drawable;
import Interfaces.Interactive;
import Interfaces.PixelFilter;
import com.github.sarxos.webcam.Webcam;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Movie;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

/**
 * Display class for working with image filters
 * by David Dobervich
 */
public class DisplayWindow extends PApplet {
    private static final int WEBCAM = 1;
    private static final int IMAGE = 2;
    private static final int VIDEO = 3;     // TODO: remove this mode or fix gstreamer library?

    private Webcam webcam;
    private Movie movie;
    private DImage inputImage;

    private boolean currentlyViewingFilteredImage = false;
    private int source;
    private DImage frame, filteredFrame, oldFilteredFrame, currentDisplayFrame;
    private boolean loading = false;

    private int centerX, centerY;

    private PixelFilter filter;
    private int count = 0;
    private String colorString = "";
    private boolean paused = false;
    private boolean initiallyPaused = false;

    private int initWidth = 900;
    private int initHeight = 800;

    private static String DEFAULT_FILTER = "DoNothingFilter";

    public void settings() {
        initializeImageSource(args);

        try {
            for (String arg : args) {
                int index = arg.indexOf("dimensions:");
                if (index != -1) {
                    String dimString = arg.substring(index + "dimensions:".length());
                    initWidth = Integer.parseInt(dimString.substring(0, dimString.indexOf("x")));
                    initHeight = Integer.parseInt(dimString.substring(dimString.indexOf("x") + 1));
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting dimensions. Using defaults of 900x800");
        }

        size(initWidth, initHeight);
        centerX = width / 2;
        centerY = height / 2;

        this.filter = loadNewFilter(DEFAULT_FILTER);
    }

    private void initializeImageSource(String[] args) {
        if (args == null || args.length == 0) {         // if no input provided, do it interactively with user
            displayVideoSourceChoiceDialog();
            return;
        }

        String sourcePath = null;
        for (String arg : args) {
            int startIndex = arg.indexOf("filepath:");
            if (startIndex != -1) {
                sourcePath = args[0].substring(startIndex + "filepath:".length());
            }
        }
        if (sourcePath == null) {
            System.err.println("malformed image source path.  Format \"filepath:<the file path>\"");
            displayVideoSourceChoiceDialog();
            return;
        }
        this.inputImage = tryToLoadStillImage(sourcePath);
        source = IMAGE;

        if (inputImage == null) {
            this.movie = new Movie(this, sourcePath);
            this.source = VIDEO;
        }
    }

    private void displayVideoSourceChoiceDialog() {
        Object[] options = {"Load image from disk",
                "Use a webcam"};
        this.source = JOptionPane.showOptionDialog(null,
                "What source would you like to use?",
                "Source",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);

        if (source != WEBCAM) {
            String sourcePath = fileChooser();
            this.inputImage = tryToLoadStillImage(sourcePath);
            source = IMAGE;

            if (inputImage == null) {
                this.movie = new Movie(this, sourcePath);   // TODO: this is probably broken
                this.source = VIDEO;
            }
        }
    }

    private String fileChooser() {
        String userDirLocation = System.getProperty("user.dir");
        File userDir = new File(userDirLocation + "/images");

        JFileChooser fc = new JFileChooser(userDir);
        int returnVal = fc.showOpenDialog(null);
        File file = fc.getSelectedFile();

        if (file != null) return file.getAbsolutePath();
        else return userDir.getAbsolutePath() + "/1.png";
    }

    private DImage tryToLoadStillImage(String moviePath) {
        try {
            PImage input = this.loadImage(moviePath);
            return new DImage(input);
        } catch (Exception e) {
            return null;
        }
    }

    public void setup() {
        if (source == VIDEO && movie == null) {
            System.err.println("No file loaded, switching to webcam as video source");
            source = WEBCAM;
        } else if (source != WEBCAM && movie != null) {
            movie.play();
        }

        if (source == IMAGE && inputImage == null) {
            System.err.println("No file loaded, switching to webcam as video source");
            source = WEBCAM;
        }

        if (source == WEBCAM && webcam == null) {
            System.out.println("Loading webcam...");
            webcam = Webcam.getDefault();

            Dimension[] views = webcam.getViewSizes();
            webcam.setViewSize(views[views.length - 1]);  // set view size to largest supported

            this.displayHeight = (int) (webcam.getViewSize().getHeight());
            this.displayWidth = (int) (webcam.getViewSize().getWidth());
            webcam.open();
        }

        initiallyPaused = (source == IMAGE);    // initially pause if it's an image
    }

    public void draw() {
        background(200);
        if (!paused) {
            fetchImageAndApplyFilter();
        }
        if (frame == null) return;

        if (oldFilteredFrame == null) oldFilteredFrame = frame;

        DImage currentFiltered = (!loading && filteredFrame != null) ? filteredFrame : oldFilteredFrame;
        currentDisplayFrame = (!currentlyViewingFilteredImage) ? frame : filteredFrame;

        if (!currentlyViewingFilteredImage) {
            drawFrame(frame, frame, currentFiltered, centerX - frame.getWidth() / 2, centerY - frame.getHeight() / 2);
        } else {        // viewing filtered
            drawFrame(currentFiltered, frame, currentFiltered, centerX - currentFiltered.getWidth() / 2, centerY - currentFiltered.getHeight() / 2);
        }

        fill(200);
        rect(0, height - 20 * 2, width, 20 * 2);
        fill(0);

        count++;
        if (count == 11) {
            colorString = colorStringAt(mouseX, mouseY);
            count = 0;
        }
        fill(0);
        textSize(16);
        textAlign(LEFT, CENTER);
        text(mousePositionString(currentDisplayFrame) + " " + colorString, 10, height - 22);

        if (filter == null) {
            text("Press 'f' to load a filter", 350, height - 22);
        } else if (!currentlyViewingFilteredImage) {
            text("Press 's' to show filtered image", 350, height - 22);
        }

        if (paused) {
            text("Press 'p' to unpause", 620, height - 22);
        } else {
            text("Press 'p' to pause", 620, height - 22);
        }

        stroke(200);
        strokeWeight(1);
        line(0, mouseY, width, mouseY);
        line(mouseX, 0, mouseX, height);

        if (filter != null && initiallyPaused) {          // hack hack!  display one frame, then pause
            initiallyPaused = false;
            paused = true;
        }
    }

    private void fetchImageAndApplyFilter() {
        if (source == IMAGE) {
            applyFilterToImage(inputImage.getPImage());
        }
        if (source == WEBCAM) {
            if (webcam == null) return;
            BufferedImage img = webcam.getImage();
            if (img == null) return;
            applyFilterToImage(new PImage(img));

        }
    }

    private String colorStringAt(int mouseX, int mouseY) {
        loadPixels();
        int c = pixels[mouseY * width + mouseX];
        float red = red(c);
        float green = green(c);
        float blue = blue(c);
        return "r: " + red + " g: " + green + " b: " + blue;
    }

    private int getImageMouseX(DImage displayImage) {
        return mouseX - centerX + displayImage.getWidth() / 2;
    }

    private int getImageMouseY(DImage displayImage) {
        return mouseY - centerY + displayImage.getHeight() / 2;
    }

    private String mousePositionString(DImage displayImage) {
        return "(" + getImageMouseX(displayImage) + ", " + getImageMouseY(displayImage) + ")";
    }

    public void drawFrame(DImage toDisplay, DImage original, DImage filtered, int x, int y) {
        image(toDisplay.getPImage(), x, y);

        if (filter != null) {
            pushMatrix();
            translate(x, y);

            if (filter instanceof Drawable) {
                ((Drawable) filter).drawOverlay(this, original, filtered);
            }
            popMatrix();
        }
    }

    public void applyFilterToImage(PImage img) {
        oldFilteredFrame = filteredFrame;
        loading = true;

        frame = new DImage(img);
        filteredFrame = new DImage(frame);
        filteredFrame = runFilters(filteredFrame);

        loading = false;
    }

    public void movieEvent(Movie m) {
        if (paused) return;

        oldFilteredFrame = filteredFrame;
        loading = true;
        m.read();
        frame = new DImage(m.get());
        filteredFrame = new DImage(frame);

        filteredFrame = runFilters(filteredFrame);

        loading = false;
    }

    private DImage runFilters(DImage frameToFilter) {
        if (filter == null) {
            System.err.println("Error: Your filter is null!  You haven't defined one to use...");
            return filteredFrame;
        } else {
            DImage output = filter.processImage(frameToFilter);
            if (output == null) {
                System.err.println("Your filter has returned a null output.  Check your processImage method!");
                System.exit(1);
            }
            return output;
        }
    }

    public void keyReleased() {
        if (key == 'f' || key == 'F') {
            this.filter = selectNewFilterDialog();
            fetchImageAndApplyFilter();
            paused = false; // hack hack; can we just apply new filter to current frame instead of unpausing?
        }

        if (key == 's' || key == 'S') {
            currentlyViewingFilteredImage = !currentlyViewingFilteredImage;
        }

        if (key == 'b' || key == 'B') {
            displayVideoSourceChoiceDialog();
        }

        if (key == 'p' || key == 'P') {
            paused = !paused;

            if (source != WEBCAM && movie != null) {
                if (paused) {
                    movie.pause();
                } else {
                    movie.play();
                }
            }
        }

        if (frame != null && (filter instanceof Interactive)) {
            ((Interactive) filter).keyPressed(key);
        }
    }

    public void mouseReleased() {
        if (this.filter != null && this.filter instanceof Interactive) {
            // TODO: should we provide the original frame or the filtered frame?  both?
            //           ((Interactive) filter).mouseClicked(getImageMouseX(currentDisplayFrame), getImageMouseY(currentDisplayFrame), currentDisplayFrame);
            if (frame != null) {
                ((Interactive) filter).mouseClicked(getImageMouseX(frame), getImageMouseY(frame), frame);
            }
        }
    }

    private PixelFilter loadNewFilter(String name) {
        PixelFilter f = null;
        try {
            Class c = Class.forName("Filters." + name.toString());
            f = (PixelFilter) c.newInstance();
        } catch (Exception e) {
            System.err.println("Something went wrong when instantiating your class!  (running its constructor). " +
                    "Double-check you typed the name correctly.  Double-check you have a constructor that takes " +
                    "no inputs!");
            System.err.println(e.getMessage());
        }
        return f;
    }

    private PixelFilter selectNewFilterDialog() {
        String userDirLocation = System.getProperty("user.dir");
        File userDir = new File(userDirLocation + "/src/Filters");

        String[] filters = new String[Objects.requireNonNull(userDir.list()).length];
        for (int i = 0; i < filters.length; i++) {
            filters[i] = Objects.requireNonNull(userDir.list())[i].replace(".java", "");
        }
        Object name = JOptionPane.showInputDialog(null, "Select your filter", "Filter Selection", JOptionPane.QUESTION_MESSAGE, null, filters, DEFAULT_FILTER);

        return loadNewFilter(name.toString());
    }

    public static void showFor(String filePath) {
        PApplet.main("core.DisplayWindow", new String[]{"filepath:" + filePath});
    }

    public static void showFor(String filePath, int width, int height) {
        PApplet.main("core.DisplayWindow", new String[]{"filepath:" + filePath, "dimensions:" + width + "x" + height});
    }

    public static void showFor(String filePath, int width, int height, String default_filter) {
        DEFAULT_FILTER = default_filter;
        PApplet.main("core.DisplayWindow", new String[]{"filepath:" + filePath, "dimensions:" + width + "x" + height});
    }

    public static void getInputInteractively() {
        PApplet.main("core.DisplayWindow", new String[]{});
    }

    public static void getInputInteractively(int width, int height) {
        PApplet.main("core.DisplayWindow", new String[]{"dimensions:" + width + "x" + height});
    }
}

