package fr.app.model;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import fr.app.view.GUIManager;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import fr.utln.dronsim.viewer.primitives.ReferentialNode;
import com.jme3.math.FastMath;
import com.jme3.texture.Texture;


public class PhysicsEngine{

    private SimpleApplication app;
    private BulletAppState bulletAppState;
    private AssetManager assetManager;
    private WorldView model;
    private Node rootNode;
    private GUIManager guiManager;
    private volatile long lastCollisionTime = 0;

    public PhysicsEngine(SimpleApplication app, WorldView model, GUIManager guiManager, boolean showMeshWireframe) {
        this.model = model;
        this.app = app;
        // Getting the asset manager
        this.assetManager = this.app.getAssetManager();
        // Getting the gui manager
        this.guiManager = guiManager;
        // Initialize bullet engine
        this.bulletAppState = new BulletAppState();
        this.rootNode = this.app.getRootNode();
        // Threading mode : SEQUENTIAL (by default)
        this.bulletAppState.setThreadingType(BulletAppState.ThreadingType.SEQUENTIAL);
        // Attach it to the global manager
        this.app.getStateManager().attach(bulletAppState);
        // true to see the blue wireframe, false if you want FPS
        this.bulletAppState.setDebugEnabled(showMeshWireframe);
        System.out.println("PhysicsEngine : Successfully initialized");
    }

    /**
     * Create the terrain node
     * @param terrain : 1 = line, 2 = spiral, 3 = point_borlo
     * @return A node
     */
    /*public Node CreateTerrainScene() {
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
    public Node CreateTerrainScene(int terrain, Runnable onTerrainReady) {
        Node node = new Node("TerrainNode");
        // Configuration des locators
        assetManager.registerLocator("data/asset", FileLocator.class);
        final String name;
        /*
        terrain =
                1 : target-approach-line-terrain
                2 : target-approach-spiral-terrain
                3 : point_borlo
         */
        if(terrain==1){
            assetManager.registerLocator("data/asset/terrain/military", FileLocator.class);
            name = "terrain/military/target-approach-line-terrain";
        } else if (terrain==2) {
            assetManager.registerLocator("data/asset/terrain/military", FileLocator.class);
            name = "terrain/military/target-approach-spiral-terrain";
        } else if(terrain==3){
            // Add point_borlo things
            assetManager.registerLocator("data/asset/terrain/cassidaigne/point_borloo", FileLocator.class);
            name = "terrain/cassidaigne/point_borloo/scene-texture";
        } else {
            System.out.println("Terrain does not exist. Using terrain 1");
            return CreateTerrainScene(1,onTerrainReady);
        }
        guiManager.CreateWaitingGUI();
        BitmapText loadingScreen = guiManager.CreateLIGUI(" Chargement de " + name, new Vector3f(0,-40,0));
        // Using thread to launch the simulator faster
        new Thread(() -> {
            // On charge les fichiers
            System.out.println("Loading terrain " + name + " in the background");
            // Initialisation des Spatial
            Spatial terrainVisuel;
            Spatial terrainLowPoly;
            terrainVisuel = assetManager.loadModel(name + ".j3o");
            System.out.println("Visuel de la map chargé");
            // Set a texture only for the 3rd map : point_borloo
            if(terrain==3){
                System.out.println("Chargement de la texture de point_borloo");
                Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                mat.setTexture("DiffuseMap", assetManager.loadTexture("texdefrag_scene.ply_optimized_texture_1.png"));
                mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                terrainVisuel.setMaterial(mat);
                System.out.println("Texture de point_borloo terminé");
            }
            else{
                convertirEnMateriauEclaire(terrainVisuel);
            }
            System.out.println("Chargement du modèle physique lowpoly");
            app.enqueue(() -> {
                // Obligatoire sinon crash aléatoire car on modifie un élément potentiellement au moment où JME le lit
                // donc kaboom
                guiManager.UpdateLIGUIElement(loadingScreen, "Chargement du modèle physique", new Vector3f(0,-40,0));
                return null;
            });
            terrainLowPoly = assetManager.loadModel(name + "-lowpoly.j3o");
            System.out.println("Terrain loaded");
            System.out.println("Beggining mesh generation");

            app.enqueue(() -> {
                // Obligatoire sinon crash aléatoire car on modifie un élément potentiellement au moment où JME le lit
                // donc kaboom
                guiManager.UpdateLIGUIElement(loadingScreen, "Création du mesh", new Vector3f(0,-40,0));
                return null;
            });
            // Generating mesh
            CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(terrainLowPoly);
            System.out.println("Mesh generated\nGenerating RigidBodyControl with this mesh");
            // JMonkey stuff
            RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
            System.out.println("RigidBodyControl generated");
            /*
            A bit of friction to avoid things getting stuck in odd ways and a bit bouncy
            Both should be adjusted to get a realistic underwater physic
             */
            landscape.setFriction(0.1f);
            landscape.setRestitution(0.2f);
            // Correct axes but not for point_borloo
            if (terrain != 3) {
                terrainVisuel.rotate(-FastMath.HALF_PI, 0.0f, 0.0f);
                landscape.setPhysicsRotation(terrainVisuel.getWorldRotation());
            } else {
                landscape.setPhysicsRotation(terrainVisuel.getWorldRotation());
            }
            // Once the processing is finished, we can inject it cleanly
            // app should be a reference to JME application (SimpleApplication)
            app.enqueue(() -> {
                if (terrain == 3) {
                    // Déplace le modèle visuel vers le bas
                    // En théorie on a pas besoin de toucher au physique vu qu'il n'a pas encore été rattaché
                    terrainVisuel.setLocalTranslation(0.0f, -5.0f, 0.0f);
                }
                terrainVisuel.addControl(landscape);
                // Par sécurité on force la position physique au cas où la condition pour point_borloo n'a pas été faite
                landscape.setPhysicsLocation(terrainVisuel.getWorldTranslation());
                bulletAppState.getPhysicsSpace().add(landscape);
                node.attachChild(terrainVisuel);
                System.out.println("Terrain visual and physic injected succesfully");
                guiManager.RemoveWaitingGUI();
                guiManager.RemoveLIGUI(loadingScreen);
                // Callback to generate cubes or drones
                if (onTerrainReady != null) {
                    // The physics engine knows it has something to do but it doesn't know what it is
                    onTerrainReady.run();
                }
                return null;
            });
        }).start();

        /*
        Return the node without the RigidBodyControl to accelerate loading and avoiding freezes on low end computers.
        Side effect : now you can go through the ground while it's still processing.
         */
        System.out.println("Returning node early without the mesh done");
        return node;
    }

    /**
     * Génère une forme de collision personnalisée (Proxy) pour éviter de traiter
     * les 11 millions de triangles du modèle visuel.
     * @return MeshCollisionShape La forme de collision simplifiée
     */
    @Deprecated
    private MeshCollisionShape CreateCustomCollisionMesh() {
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
     * Convertit le matériau d'un terrain (souvent Unshaded, insensible à la
     * lumière) en matériau Lighting.j3md, afin que les lumières de la scène —
     * notamment la lampe du drone (SpotLight) — puissent réellement l'éclairer.
     * <p>
     * Parcourt toutes les géométries du Spatial. Pour chacune, on tente de
     * récupérer la texture diffuse existante (paramètre "ColorMap" d'Unshaded
     * ou "DiffuseMap") et on la réapplique sur un nouveau matériau Lighting.
     * Si aucune texture n'est trouvée, on applique une couleur diffuse neutre.
     *
     * @param spatial racine du terrain à convertir.
     */
    private void convertirEnMateriauEclaire(Spatial spatial) {
        spatial.depthFirstTraversal(s -> {
            if (s instanceof Geometry geo) {
                Texture diffuse = null;
                Material ancien = geo.getMaterial();
                if (ancien != null) {
                    // Unshaded stocke la texture dans "ColorMap", Lighting dans "DiffuseMap"
                    try {
                        if (ancien.getParam("ColorMap") != null) {
                            diffuse = (Texture) ancien.getParam("ColorMap").getValue();
                        } else if (ancien.getParam("DiffuseMap") != null) {
                            diffuse = (Texture) ancien.getParam("DiffuseMap").getValue();
                        }
                    } catch (Exception ignored) {}
                }

                Material lit = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                if (diffuse != null) {
                    lit.setTexture("DiffuseMap", diffuse);
                } else {
                    // Pas de texture : couleur neutre pour rester visible et éclairable
                    lit.setBoolean("UseMaterialColors", true);
                    lit.setColor("Diffuse", ColorRGBA.LightGray);
                    lit.setColor("Ambient", ColorRGBA.LightGray);
                }
                lit.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                geo.setMaterial(lit);
            }
        });
        System.out.println("[PhysicsEngine] Terrain converti en matériau éclairable (Lighting).");
    }

    /**
     * Spawns a cube to test physics
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param color the color of the cube
     */
    public void SpawnTestCube(Node cubeNode, float x, float y, float z, ColorRGBA color) {
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
        cubeNode.attachChild(cubeGeo);
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
     * Creates a hitbox for the drone
     * @param drone The drone that need to get a hitbox generated
     * @param mode Type of hitbox, "BR2R4" and "Guardian" are manual and anything else is an automatic box (not very good)
     */
    public void CreateDroneHitbox(Drone drone, String mode){
        System.out.println("Generating the drone hitbox using the ");
        CollisionShape droneHitbox = switch(mode){
            case "BR2R4" :
                System.out.println("Manual hitbox for the " + mode + ".");
                yield new BoxCollisionShape(new Vector3f(0.18f, 0.15f, 0.23f));
            case "Guardian" :
                System.out.println("Manual hitbox for the " + mode + ".");
                yield new BoxCollisionShape(new Vector3f(0.22f, 0.13f, 0.4f));
            default :
                System.out.println("Automatic box.");
                yield CollisionShapeFactory.createBoxShape(drone.getDroneSpatial());
        };
        // Set mass to 0 to have the drone float
        RigidBodyControl dronePhysics = new RigidBodyControl(droneHitbox, 5.0f);
        dronePhysics.setFriction(0.3f);
        dronePhysics.setRestitution(0.5f);
        // Inertia
        dronePhysics.setLinearDamping(0.4f);
        dronePhysics.setAngularDamping(0.6f);
        // Attach the physic to the visual
        drone.getDroneSpatial().addControl(dronePhysics);
        // Add the rigid body to the physic space
        bulletAppState.getPhysicsSpace().add(dronePhysics);

        bulletAppState.getPhysicsSpace().addCollisionListener(
                new PhysicsCollisionListener() {

                    @Override
                    public void collision(PhysicsCollisionEvent event) {

                        PhysicsCollisionObject a = event.getObjectA();
                        PhysicsCollisionObject b = event.getObjectB();


                        if (a == dronePhysics || b == dronePhysics) {

                            lastCollisionTime = System.currentTimeMillis();
                            drone.setCollisionDetected(true);
                            drone.setCollisionMessage("Collision avec terrain ou obstacle");
                            System.out.println("Collision détectée");
                        }

                        else {
                            drone.setCollisionDetected(false);
                        }
                    }
                }
        );
    }


    public long getLastCollisionTime() {
        return lastCollisionTime;
    }

    public void CreateDroneHitbox(Drone drone){
        CreateDroneHitbox(drone, "Auto");
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