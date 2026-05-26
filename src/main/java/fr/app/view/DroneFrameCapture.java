package fr.app.view;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import com.jme3.renderer.Camera;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import com.jme3.renderer.queue.RenderQueue;

public class DroneFrameCapture implements SceneProcessor {

    public static final int   LARGEUR      = 320;
    public static final int   HAUTEUR      = 240;
    public static final float QUALITE_JPEG = 0.70f;

    private final ViewPort    viewPortOffScreen;
    private final FrameBuffer frameBuffer;
    private final ByteBuffer  pixelBuffer;
    private final String      droneId;
    private final Camera      cameraLive;
    private       Renderer    renderer;
    private       RenderManager renderManager;

    private final AtomicReference<byte[]> derniereFrame = new AtomicReference<>(null);

    public DroneFrameCapture(String droneId,
                             Camera cameraDrone,
                             RenderManager renderManager,
                             Node rootNode,
                             Node guiNode) {
        this.droneId       = droneId;
        this.cameraLive    = cameraDrone;
        this.renderManager = renderManager;
        this.renderer      = renderManager.getRenderer();

        Camera camOffScreen = cameraDrone.clone();
        camOffScreen.resize(LARGEUR, HAUTEUR, true);

        // RGBA8 au lieu de RGB8 : plus fiable avec readFrameBuffer() en JME3 3.7
        frameBuffer = new FrameBuffer(LARGEUR, HAUTEUR, 1);
        frameBuffer.setColorBuffer(Image.Format.RGBA8);
        frameBuffer.setDepthBuffer(Image.Format.Depth);

        viewPortOffScreen = renderManager.createPreView("offscreen_" + droneId, camOffScreen);
        viewPortOffScreen.setClearFlags(true, true, true);
        viewPortOffScreen.setBackgroundColor(new com.jme3.math.ColorRGBA(0, 0, 0.6f, 1));
        viewPortOffScreen.setOutputFrameBuffer(frameBuffer);
        viewPortOffScreen.attachScene(rootNode);
        viewPortOffScreen.attachScene(guiNode);
        viewPortOffScreen.setEnabled(true);
        viewPortOffScreen.addProcessor(this);

        // 4 octets par pixel (RGBA8)
        pixelBuffer = BufferUtils.createByteBuffer(LARGEUR * HAUTEUR * 4);
        System.out.println("[DroneFrameCapture] Créé pour " + droneId
                + " — viewport: offscreen_" + droneId);
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        this.renderer      = rm.getRenderer();
        this.renderManager = rm;
        System.out.println("[DroneFrameCapture] initialize() appelé pour " + droneId);
    }

    @Override public void reshape(ViewPort vp, int w, int h) {}

    @Override public boolean isInitialized() { return renderer != null; }

    @Override
    public void preFrame(float tpf) {
        // Synchroniser la caméra off-screen avec la caméra live du drone
        Camera camOffScreen = viewPortOffScreen.getCamera();
        camOffScreen.setLocation(cameraLive.getLocation());
        camOffScreen.setRotation(cameraLive.getRotation());
        camOffScreen.update();
    }

    @Override public void postQueue(RenderQueue rq) {}

    @Override
    public void postFrame(FrameBuffer out) {
        try {
            pixelBuffer.clear();
            renderer.readFrameBuffer(frameBuffer, pixelBuffer);
            pixelBuffer.rewind();

            // RGBA8 : 4 octets par pixel (R, G, B, A)
            BufferedImage image = new BufferedImage(LARGEUR, HAUTEUR, BufferedImage.TYPE_3BYTE_BGR);
            for (int y = HAUTEUR - 1; y >= 0; y--) {
                for (int x = 0; x < LARGEUR; x++) {
                    int r = pixelBuffer.get() & 0xFF;
                    int g = pixelBuffer.get() & 0xFF;
                    int b = pixelBuffer.get() & 0xFF;
                    pixelBuffer.get(); // alpha — ignoré
                    image.setRGB(x, y, (r << 16) | (g << 8) | b);
                }
            }

            byte[] jpeg = compresserEnJpeg(image);
            derniereFrame.set(jpeg);
            // Log périodique (1 frame sur 30) pour ne pas spammer
            if (System.currentTimeMillis() % 1000 < 34) {
                System.out.println("[DroneFrameCapture] Frame capturée pour " + droneId
                        + " — " + jpeg.length + " octets JPEG");
            }

        } catch (Exception e) {
            System.err.println("[DroneFrameCapture] Erreur postFrame " + droneId + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override public void cleanup() {}
    @Override public void setProfiler(AppProfiler profiler) {}

    public byte[] capturerFrame() {
        return derniereFrame.get();
    }

    private byte[] compresserEnJpeg(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(QUALITE_JPEG);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    public void dispose(RenderManager rm) {
        viewPortOffScreen.setEnabled(false);
        rm.removePreView(viewPortOffScreen);
        frameBuffer.dispose();
        System.out.println("[DroneFrameCapture] Ressources libérées pour " + droneId);
    }

    public String getDroneId() { return droneId; }
}