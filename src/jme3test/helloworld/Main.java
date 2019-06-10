package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.Timer;
import com.jme3.util.SkyFactory;
import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.xml.stream.events.Comment;

public class Main extends SimpleApplication {
    //Score
    int score = 0;
    //Texto Inicial
    BitmapText hudText;

    //Timer
    Timer myTimer = getTimer();

    boolean check = false;
    private Spatial sceneModel;


    public static void main(String[] args) {

        Main app = new Main();

        app.start();
    }
    //Alvos
    private Node shootables;
    //Tiro
    private Geometry mark;
    //Bala 
    BulletAppState bulletAppState = new BulletAppState();

    @Override
    public void simpleInitApp() {

        this.setDisplayFps(false);
        this.setDisplayStatView(false);

        
        hudText = new BitmapText(guiFont, false);

        hudText.setSize(guiFont.getCharSet().getRenderedSize());     
        hudText.setColor(ColorRGBA.Yellow);                            
        hudText.setLocalTranslation(0, settings.getHeight(), 0);

        if (check == false) {

            hudText.setText("Aperte Enter para Jogar");           
            guiNode.attachChild(hudText);
        }

        initKeys(); //key mapping

        if (check == true) {

            //Céu
            getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
            CrossHairsInit(); //Mira
            MarkCreation();    
            shootables = new Node("Shootables");
            //rootNode.attachChild(sceneModel);
            rootNode.attachChild(shootables);

            stateManager.attach(bulletAppState);

            //bulletAppState.getPhysicsSpace().setMaxSubSteps(4);
            bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0f, -10f, 0f));
            Geometry f = makeFloor();
           
            bulletAppState.getPhysicsSpace().add(f);

            shootables.attachChild(f);

            AmbientLight al = new AmbientLight();
            al.setColor(ColorRGBA.White.mult(1.3f));
            rootNode.addLight(al);

            DirectionalLight dl = new DirectionalLight();
            dl.setColor(ColorRGBA.White);
            dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
            rootNode.addLight(dl);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
       

        //Adiciona novo alvo
        if (check == true) {
            if (myTimer.getTimeInSeconds() > 2) {

                Vector3f center = new Vector3f(0, 0, 0);

                Vector3f extent = new Vector3f(50, 50, 50);

                float xValue = (float) (Math.random() * extent.x);

                float zValue = (float) (Math.random() * extent.z);

                Spatial golem = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
                golem.scale(0.2f);
                // golem.setLocalTranslation(-1.0f, -1.5f, -0.6f);

                DirectionalLight sun = new DirectionalLight();
                sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
                
                golem.addLight(sun);

                golem.setLocalTranslation(xValue, 10, -zValue);
                golem.addControl(new RigidBodyControl(new SphereCollisionShape(1), 2));

                shootables.attachChild(golem);

                bulletAppState.getPhysicsSpace().add(golem);

                myTimer.reset();
            }
        }
    }

    //Atirar
    private void initKeys() {
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_SPACE), //Atira com barra de espaço

                new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); //Atira com mouse
        inputManager.addListener(actionListener, "Shoot");

        inputManager.addMapping("Play", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, "Play");

    }

    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Play")) {

                check = true;
                hudText.setText("");     

                simpleInitApp();
                guiNode.removeFromParent();

            }

            if (name.equals("Shoot") && !keyPressed) {
                
                CollisionResults results = new CollisionResults();
                
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                //Checa se tiro acertou alvo
                shootables.collideWith(ray, results);
                for (int i = 0; i < results.size(); i++) {
                    
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    //Checa se nao está no chao
                    if (!results.getCollision(i).getGeometry().getName().equals("floor")) {

                        results.getCollision(i).getGeometry().removeFromParent();
                       
                        score = score + 10;

                        hudText.setText("");          

                        hudText.setText("Score " + score);            
                        guiNode.attachChild(hudText);
                        guiNode.removeFromParent();

                    }

                }
                if (results.size() > 0) {
                   
                    CollisionResult closest = results.getClosestCollision();
                    
                    mark.setLocalTranslation(closest.getContactPoint());
                    rootNode.attachChild(mark);
                } else {
                    
                    rootNode.detachChild(mark);
                }
            }
        }
    };

    
    protected Geometry makeFloor() {

        Box box = new Box(100, .2f, 100);
        Geometry floor = new Geometry("floor", box);
        floor.setLocalTranslation(0, -4, -5);
        //Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         Material      mat1 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        mat1.setTexture("DiffuseMap", assetManager.loadTexture("Textures/A1.png"));

        floor.setMaterial(mat1);

        //CollisionShape tableShape = CollisionShapeFactory.createMeshShape((Node) );
        floor.addControl(new RigidBodyControl(0));
        floor.getControl(RigidBodyControl.class).setFriction(100);

        return floor;
    }

    protected void MarkCreation() {
        //Bala
        Sphere sphere = new Sphere(3, 3, 0.1f);
        mark = new Geometry("Spjere", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }

    protected void CrossHairsInit() {
        setDisplayStatView(false);
        //Mira
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); 
        ch.setLocalTranslation( 
                settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

}
