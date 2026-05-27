package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
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
        // On le centre sur l'écran
        loadingText.setLocalTranslation(
                settings.getWidth() / 2f - loadingText.getLineWidth() / 2f,
                settings.getHeight() / 2f + loadingText.getLineHeight() / 2f,
                0
        );
        // On l'affiche immédiatement à l'écran
        guiNode.attachChild(loadingText);
    }

    /**
     * Affiche un texte à l'écran
     * @param text Le texte à afficher
     * @return BitmapText pour le modifier ou le supprimer plus tard
     */
    public BitmapText CreateLIGUI(String text, Vector3f offset){
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText guiText = new BitmapText(guiFont, false);
        guiText.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        guiText.setText(text);
        guiText.setLocalTranslation(
                settings.getWidth() / 2f - guiText.getLineWidth() / 2f + offset.x,
                settings.getHeight() / 2f + guiText.getLineHeight() / 2f + offset.y,
                offset.z
        );
        guiNode.attachChild(guiText);
        return guiText;
    }

    public void UpdateLIGUIElement(BitmapText guiText, String newText, Vector3f offset){
        guiText.setText(newText);
        guiText.setLocalTranslation(
                settings.getWidth() / 2f - guiText.getLineWidth() / 2f + offset.x,
                settings.getHeight() / 2f + guiText.getLineHeight() / 2f + offset.y,
                offset.z
        );
    }

    public void RemoveWaitingGUI(){
        guiNode.detachChild(loadingText);
    }

    /**
     * Retire un text du GUI
     * @param text Le texte à retirer
     */
    public void RemoveLIGUI(BitmapText text){guiNode.detachChild(text);}
}
