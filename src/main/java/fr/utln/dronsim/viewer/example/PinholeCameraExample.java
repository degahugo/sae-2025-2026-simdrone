package fr.utln.dronsim.viewer.example;

import java.nio.ByteBuffer;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.system.AppSettings;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

import fr.utln.jme3.filter.DistortionFilter;
import fr.utln.jme3.model.Chessboard;

public class PinholeCameraExample extends SimpleApplication{
	
	@Override
	public void simpleInitApp() {
	    
		// Create scene objects
		Chessboard chessboard = new Chessboard(assetManager, 6, 10, 1.0f, 0.1f);
		rootNode.attachChild(chessboard);

	    // Initialize lighting
	    initLighting();

	    // Set up camera
	    cam.setLocation(new Vector3f(chessboard.getCornersCols()*chessboard.getSize()/2.0f, chessboard.getCornersRows()*chessboard.getSize()/2.0f, 10.0f));

	    // Create a pinhole camera
	    Camera physicalCam = new Camera(1280, 1024);

	    FrameBuffer physicalFrameBuffer = new FrameBuffer(physicalCam.getWidth(), physicalCam.getHeight(), 1);

	    // Create a color buffer and attach it to the FrameBuffer
	    Texture2D colorTexture = new Texture2D(physicalCam.getWidth(), physicalCam.getHeight(), Image.Format.RGBA8);
	    physicalFrameBuffer.addColorTarget(FrameBuffer.FrameBufferTarget.newTarget(colorTexture));

	    // Create a depth buffer and attach it to the FrameBuffer
	    Texture2D depthTexture = new Texture2D(physicalCam.getWidth(), physicalCam.getHeight(), Image.Format.Depth);
	    physicalFrameBuffer.setDepthTarget(FrameBuffer.FrameBufferTarget.newTarget(depthTexture));

	    ViewPort physicalViewPort = renderManager.createPostView("Physical", physicalCam);
	    physicalViewPort.setClearFlags(true, true, true);
	    physicalViewPort.setOutputFrameBuffer(physicalFrameBuffer);

	    physicalViewPort.setBackgroundColor(ColorRGBA.Black);

	    physicalViewPort.attachScene(rootNode);

	    // Apply distortion filter
	    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
	    DistortionFilter distortionFilter = new DistortionFilter();
	    fpp.addFilter(distortionFilter);
	    physicalViewPort.addProcessor(fpp);
	}

	/**
	 * Create the main 3D scene
	 */
	private void createScene() {

		
	}

	/**
	 * Initialize the global scene lighting
	 */
	private void initLighting() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);
	}

	/**
	 * Read the given <code>frameBuffer</code> content.
	 * @param frameBuffer the frame buffer to read
	 */
	private void readFrameBufferContent(FrameBuffer frameBuffer) {
	    // Create a ByteBuffer to store the pixel data
	    int width = frameBuffer.getWidth();
	    int height = frameBuffer.getHeight();
	    ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // 4 bytes per pixel (RGBA)

	    // Read the FrameBuffer content into the ByteBuffer
	    Renderer renderer = getRenderer();
	    renderer.readFrameBuffer(frameBuffer, buffer);

	    // Optionally, process the buffer or convert it to an image format
	    // For example, you can save the buffer to a file or use it for further processing
	}

/*	
	public BufferedImage convertByteBufferToBufferedImage(ByteBuffer buffer, int width, int height) {
	    // Create a BufferedImage with the same width and height
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	    // Read the pixel data from the ByteBuffer and set it to the BufferedImage
	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            int i = (x + (width * y)) * 4;
	            int r = buffer.get(i) & 0xFF;
	            int g = buffer.get(i + 1) & 0xFF;
	            int b = buffer.get(i + 2) & 0xFF;
	            int a = buffer.get(i + 3) & 0xFF;
	            int argb = (a << 24) | (r << 16) | (g << 8) | b;
	            image.setRGB(x, height - y - 1, argb); // Flip the image vertically
	        }
	    }

	    return image;
	}
*/
	
	/**
	 * The main method
	 * @param args the main method arguments
	 */
	public static void main(String[] args) {

		AppSettings settings=new AppSettings(true);

		settings.setWidth(1024);
		settings.setHeight(768);

		PinholeCameraExample app = new PinholeCameraExample();
		app.setShowSettings(false);
		app.setSettings(settings);
		app.start(); // start the game
	}
}
