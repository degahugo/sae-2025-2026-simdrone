package fr.app.network;

public class MainTestServeur {

    public static void main(String[] args) {
        try {

            NetworkServeur server = new NetworkServeur(62500, 62501);

            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
