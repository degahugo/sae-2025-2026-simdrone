package fr.app.model;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import fr.utln.dronsim.viewer.primitives.ReferentialNode;

public class PhysicsEngine{

    private SimpleApplication app;
    private BulletAppState bulletAppState;
    private AssetManager assetManager;
    private WorldView model;
    private Node rootNode;

    public PhysicsEngine(SimpleApplication app, WorldView model) {
        this.model = model;
        this.app = app;
        // Getting the asset manager
        this.assetManager = this.app.getAssetManager();
        // Initialize bullet engine
        this.bulletAppState = new BulletAppState();
        this.rootNode = this.app.getRootNode();
        // Threading mode : SEQUENTIAL (by default)
        this.bulletAppState.setThreadingType(BulletAppState.ThreadingType.SEQUENTIAL);
        // Attach it to the global manager
        this.app.getStateManager().attach(bulletAppState);
        // true to see the blue wireframe, false if you want FPS
        this.bulletAppState.setDebugEnabled(false);
        System.out.println("PhysicsEngine : Successfully initialized");
    }

    /**
     * Create the terrain node
     * @return A node
     */
    /*public Node createTerrainScene() {
        // Old way of doing it, here for legacy and because I didn't commited it before changing it because the issue wasn't fixed
        Node node = new Node("TerrainNode");
        // Locators config, path of the assets
        assetManager.registerLocator("data/asset", FileLocator.class);
        assetManager.registerLocator("data/asset/terrain/military", FileLocator.class);
        System.out.println("Loading terrain");
        // Load terrain with texture
        Spatial terrainVisuel = assetManager.loadModel("terrain/military/target-approach-line-terrain.j3o");
        Spatial terrainLowPoly = assetManager.loadModel("terrain/military/target-approach-line-terrain-lowpoly.j3o");
        System.out.println("Terrain loaded");
        // Generates a terrain using a custom-made method to choose the method used (better for testing)
        CollisionShape sceneShape = GenerateLandscape(terrainLowPoly, "Mesh");
        // Generates a custom-made mesh
        //MeshCollisionShape sceneShape = createCustomCollisionMesh();
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0); // masse 0 = objet statique
        landscape.setFriction(0.1f);
        landscape.setRestitution(0.2f);
        terrainVisuel.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);
        // Correct axes for water rendering (Y = vertical)
        terrainVisuel.rotate(-FastMath.HALF_PI, 0.0f, 0.0f);
        landscape.setPhysicsRotation(terrainVisuel.getWorldRotation());
        node.attachChild(terrainVisuel);
        return node;
    }*/
    public Node createTerrainScene() {
        Node node = new Node("TerrainNode");
        // Configuration des locators
        assetManager.registerLocator("data/asset", FileLocator.class);
        assetManager.registerLocator("data/asset/terrain/military", FileLocator.class);
        // On charge les fichiers
        System.out.println("Loading terrain");
        Spatial terrainVisuel = assetManager.loadModel("terrain/military/target-approach-line-terrain.j3o"); // Visuel
        Spatial terrainLowPoly = assetManager.loadModel("terrain/military/target-approach-line-terrain-lowpoly.j3o"); // Physique
        System.out.println("Terrain loaded");
        // Correct axes
        terrainVisuel.rotate(-FastMath.HALF_PI, 0.0f, 0.0f);
        node.attachChild(terrainVisuel);
        // Using thread to launch the simulator faster
        new Thread(() -> {
            System.out.println("Beggining mesh generation in the background");
            // Generating mesh
            CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(terrainLowPoly);
            // JMonkey stuff
            RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
            /*
            A bit of friction to avoid things getting stuck in odd ways and a bit bouncy
            Both should be adjusted to get a realistic underwater physic
             */
            landscape.setFriction(0.1f);
            landscape.setRestitution(0.2f);
            // Physical rotation to match the visual (-FastMath.HALF_PI)
            landscape.setPhysicsRotation(terrainVisuel.getWorldRotation());
            // Once the processing is finished, we can inject it cleanly
            // app should be a reference to JME application (SimpleApplication)
            app.enqueue(() -> {
                terrainVisuel.addControl(landscape);
                bulletAppState.getPhysicsSpace().add(landscape);
                System.out.println("Terrain physic injected succesfully");
                return null;
            });
        }).start();

        /*
        Return the node without the RigidBodyControl to accelerate loading and avoiding freezes on low end computers.
        Side effect : now you can go through the ground while it's still processing.
         */
        return node;
    }

    /**
     * Génère une forme de collision personnalisée (Proxy) pour éviter de traiter
     * les 11 millions de triangles du modèle visuel.
     * @return MeshCollisionShape La forme de collision simplifiée
     */
    @Deprecated
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
    @Deprecated
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
        System.out.println("Finished terrain mesh using the " + type + " method");
        return sceneShape;
    }

    /**
     * Spawns a cube to test physics
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param color the color of the cube
     */
    public void spawnTestCube(float x, float y, float z, ColorRGBA color) {
        // Cube of 0.5m
        Box boxMesh = new Box(0.5f, 0.5f, 0.5f);
        Geometry cubeGeo = new Geometry("TestCube", boxMesh);
        // Red material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        cubeGeo.setMaterial(mat);
        // Position and angle
        cubeGeo.setLocalTranslation(new Vector3f(x, y, z));
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

    /**
     * Create a referential
     * @param node node where the referential should be created
     */
    public void createRefNode(Node node){
        ReferentialNode refNode = new ReferentialNode(assetManager);
        node.attachChild(refNode);
    }

    /**
     * Getter to add physics objects
     * @return BulletAppState
     */
    public BulletAppState getBulletAppState() {
        return bulletAppState;
    }
}