package fr.app.structure;

//Interface définissant les méthodes de sérialisation et de déserialisation (cela permet également de changer facilement l'implémentation en fonction des différentes méthodes (Json ou protobuf ...)
//serialisation : convertit un objet en un tableau de bytes pour le transmettre sur le réseau
//deserialisation : convertit un tableau de bytes reçu du réseau en un objet du type spécifié

public interface Serializer {
    byte[] serialisation(Object objet) throws SerialisationException;
    <T> T deserisation(byte[] donnee,Class<T> type ) throws SerialisationException;
}
