package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import fr.app.model.WorldView;
import fr.utln.dronsim.viewer.example.WaterFilterExample;
import fr.utln.dronsim.viewer.primitives.ReferentialNode;

/**
 * Main class to launch the simulation
 */
public class Main extends SimpleApplication {

    private WaterFilter waterFilter;
    private WorldView model;
    private BulletAppState bulletAppState;
    private FilterPostProcessor fpp = null;

    public static void main(String[] args) {

        AppSettings settings=new AppSettings(true);
        settings.setWidth(1024);
        settings.setHeight(768);
        settings.setVSync(false);
        settings.setFrameRate(-1);

        Main app = new Main();
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }


    @Override
    public void simpleInitApp() {
        // Initialise model
        model = new WorldView();

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(false); // true pour voir les boites de collision en bleu (nom nom les performances)

        // Configure scene
        configureCamera();
        initLighting();

        Node sceneNode = createTerrainScene();
        rootNode.attachChild(sceneNode);
        initWater();
        spawnTestCube();

        // 3. Attacher le Contrôleur
        //stateManager.attach(new PhysicsEngine(model));

        // Add a sky
        rootNode.attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    }

    /**
     * Create the terrain node
     * @return A node
     */
    private Node createTerrainScene() {
        Node node = new Node("TerrainNode");

        // Locators config, path of the assets
        assetManager.registerLocator("data/asset", FileLocator.class);
        assetManager.registerLocator("data/asset/terrain/military", FileLocator.class);

        ReferentialNode refNode = new ReferentialNode(assetManager);
        node.attachChild(refNode);

        // Load terrain with texture
        Spatial terrain = assetManager.loadModel("terrain/military/target-approach-line-terrain.j3o");
        // Generates a terrain using a custom-made method to choose the method used (better for testing)
        //CollisionShape sceneShape = GenerateLandscape(terrain, "DynamicMesh");

        // Generates a custom-made mesh
        MeshCollisionShape sceneShape = createCustomCollisionMesh();

        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0); // masse 0 = objet statique
        landscape.setFriction(0.1f);
        landscape.setRestitution(0.2f);
        terrain.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);

        // Correct axes for water rendering (Y = vertical)
        terrain.rotate(-FastMath.HALF_PI, 0.0f, 0.0f);
        landscape.setPhysicsRotation(terrain.getWorldRotation());

        node.attachChild(terrain);

        return node;
    }

    /**
     * Génère une forme de collision personnalisée (Proxy) pour éviter de traiter
     * les 11 millions de triangles du modèle visuel.
     * @return MeshCollisionShape La forme de collision simplifiée
     */
    private MeshCollisionShape createCustomCollisionMesh() {
        System.out.println("Creating a custom mesh for the terrain");
        // Dimensions
        System.out.println("Defining dimensions");
        float width = 10f;  // Largeur totale / 2
        float depth = 10f;  // Profondeur totale / 2
        float devers = -2f;  // L'inclinaison (différence de hauteur entre le haut et le bas)
        float offsetY = -5f; // Horizontal offset
        float offsetZ = -0.5f; // Vertical offset
        float wallHeight = model.getWaterHeight(); // Wall height (also ceiling height) by getting the water's height

        // Vertices
        // On crée un rectangle simple avec une pente sur l'axe Z
        System.out.println("Creating vertices");
        Vector3f[] vertices = new Vector3f[8];

        // Local variables to adjust easily and better readability
        // Map borders
        float coordYmin = -width + offsetY;
        float coordYmax = width + offsetY;
        // Ground height
        float coordZmin = devers + offsetZ;
        float coordZmax = 0 + offsetZ;
        //Ceiling height
        float ceilingZ = 0 + offsetZ + wallHeight;

        // Ground
        vertices[0] = new Vector3f(depth, coordYmin, coordZmax);       // Point 1
        vertices[1] = new Vector3f(depth, coordYmax, coordZmax);        // Point 2
        vertices[2] = new Vector3f(-depth, coordYmin, coordZmin);   // Point 3 (pente)
        vertices[3] = new Vector3f(-depth, coordYmax, coordZmin);    // Point 4 (pente)
        // Walls
        vertices[4] = new Vector3f(depth, coordYmin, ceilingZ);
        vertices[5] = new Vector3f(depth, coordYmax, ceilingZ);
        vertices[6] = new Vector3f(-depth, coordYmin, ceilingZ);
        vertices[7] = new Vector3f(-depth, coordYmax, ceilingZ);

        // Triangles (Indices)
        // Sens anti-horaire pour définir la face "sol" vers le haut
        System.out.println("Creating triangles");
        int[] indices = {
                // Ground (2 triangles)
                0, 1, 2,
                2, 1, 3,

                // Bottom wall (points 0, 1, 4, 5)
                0, 1, 4,
                1, 5, 4,

                // Left wall (points 0, 2, 4, 6)
                2, 0, 6,
                0, 4, 6,

                // Right wall (points 1, 3, 5, 7)
                1, 3, 5,
                3, 7, 5,

                // Top wall (points 2, 3, 6, 7)
                3, 2, 7,
                2, 6, 7,

                // Ceiling
                4, 6, 5,
                5, 6, 7
        };

        // Constructing Mesh
        System.out.println("Contructing mesh");
        Mesh customMesh = new com.jme3.scene.Mesh();
        customMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        customMesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        customMesh.updateBound();

        // Returns Collision mesh
        System.out.println("Finishing collision mesh");
        return new MeshCollisionShape(customMesh);
    }

    /**
     * Generates a mesh using 3 differents methods.
     * @param terrain The mesh will be created with this object
     * @param type Type of the generation method, should be : "DynamicMesh", "Mesh" or anything else for a box shape
     * @return A CollisionShape landscape
     */
    private CollisionShape GenerateLandscape(Spatial terrain, String type){
        CollisionShape sceneShape;
        System.out.print("Generating terrain mesh ");
        sceneShape = switch (type) {
            case "DynamicMesh" -> {
                // Works ok but requieres un NASA's pc when a collision occurs if the model is big
                System.out.println("using the DynamicMesh");
                yield CollisionShapeFactory.createDynamicMeshShape(terrain);
            }
            case "Mesh" -> {
                // Very heavy to generate
                System.out.println("using the Mesh");
                yield CollisionShapeFactory.createMeshShape(terrain);
            }
            default -> {
                // Very boxy
                System.out.println("using the Box method (it's a box)");
                yield CollisionShapeFactory.createBoxShape(terrain);
            }
        };
        System.out.println("finished terrain mesh");
        return sceneShape;
    }

    /**
     * Configure the camera settings to fly around
     */
    private void configureCamera() {
        cam.setLocation(new Vector3f(10f, 10f, 10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(10f);
    }

    /**
     * Configure the lightning
     */
    private void initLighting() {
        // Set up scene lighting
        PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White);
        lamp_light.setRadius(4f);
        lamp_light.setPosition(new Vector3f(0.0f, 0.0f, 2.0f));
        rootNode.addLight(lamp_light);

        SpotLight spot = new SpotLight();
        spot.setSpotRange(100f);                           // distance
        spot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot.setColor(ColorRGBA.White);                    // light color
        spot.setPosition(cam.getLocation());               // shine from camera loc
        spot.setDirection(cam.getDirection());             // shine forward from camera loc
        rootNode.addLight(spot);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White);
        rootNode.addLight(al);

        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
    }

    /**
     * Initialize the water filter
     */
    private void initWater() {
        fpp = new FilterPostProcessor(assetManager);
        waterFilter = new WaterFilter(rootNode, new Vector3f(-.5f,-.5f,-.5f));
        waterFilter.setWaterHeight(model.getWaterHeight());
        waterFilter.setSpeed(model.getWaterSpeed());

        fpp.addFilter(waterFilter);
        viewPort.addProcessor(fpp);
    }

    public WaterFilter getWaterFilter() {
        return waterFilter;
    }

    /**
     * Spawns a cube to test the physics
     */
    private void spawnTestCube() {
        // Cube of 0.5m
        Box boxMesh = new Box(0.5f, 0.5f, 0.5f);
        Geometry cubeGeo = new Geometry("TestCube", boxMesh);

        // Red material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        cubeGeo.setMaterial(mat);

        // Position and angle
        cubeGeo.setLocalTranslation(new Vector3f(0, 10f, 0));
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(45 * FastMath.DEG_TO_RAD,
                0,
                20 * FastMath.DEG_TO_RAD);
        cubeGeo.setLocalRotation(rotation);
        rootNode.attachChild(cubeGeo);

        // Cube physics
        RigidBodyControl cubePhys = new RigidBodyControl(1.0f);
        cubeGeo.addControl(cubePhys);

        // Added restitution and a friction to avoid it balancing at unrealistic angles
        cubePhys.setRestitution(0.2f);
        cubePhys.setFriction(0.1f);

        // Add to physic space
        bulletAppState.getPhysicsSpace().add(cubePhys);
    }
}
