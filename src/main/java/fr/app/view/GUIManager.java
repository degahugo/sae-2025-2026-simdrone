package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

public class GUIManager {
    private AssetManager assetManager;
    private AppSettings settings;
    private Node guiNode;
    private BitmapText loadingText;

    public GUIManager(AssetManager assetManager, AppSettings settings, Node guiNode){
        this.assetManager = assetManager;
        this.settings = settings;
        this.guiNode = guiNode;
    }

    public void CreateWaitingGUI(){
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        loadingText = new com.jme3.font.BitmapText(guiFont, false);
        loadingText.setSize(guiFont.getCharSet().getRenderedSize() * 2); // Texte assez grand
        loadingText.setText("Chargement de la simulation...");

        // On le centre sur l'écran d'un PC portable standard
        loadingText.setLocalTranslation(
                settings.getWidth() / 2f - loadingText.getLineWidth() / 2f,
                settings.getHeight() / 2f + loadingText.getLineHeight() / 2f,
                0
        );

        // On l'affiche immédiatement à l'écran
        guiNode.attachChild(loadingText);
    }

    public void RemoveWaitingGUI(){
        guiNode.detachChild(loadingText);
    }
}
