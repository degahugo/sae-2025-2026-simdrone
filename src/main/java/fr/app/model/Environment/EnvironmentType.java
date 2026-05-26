package fr.app.model.Environment;

public enum EnvironmentType {

    UNDERWATER_LINE(
            1,
            "Sous-marin - parcours ligne"
    ),

    UNDERWATER_SPIRAL(
            2,
            "Sous-marin - parcours spirale"
    ),

    POINT_BORLO(
            3,
            "Sous-marin - Point Borlo"
    );

    private final int terrainId;
    private final String label;

    EnvironmentType(int terrainId, String label) {
        this.terrainId = terrainId;
        this.label = label;
    }

    public int getTerrainId() {
        return terrainId;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}