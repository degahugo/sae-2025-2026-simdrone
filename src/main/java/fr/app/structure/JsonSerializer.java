package fr.app.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonSerializer implements Serializer {

    // ObjectMapper de la classe de Jackson pour sérialiser/désérialiser
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialisation(Object objet) throws SerialisationException {
        try{
            // Conversion de l'objet Java en Json pour ensuite le transfoemr en tableau de bytes
            return mapper.writeValueAsBytes(objet);
        } catch (Exception e) {
            throw new SerialisationException("Erreur de sérialisation JSON : " + e.getMessage());
        }

    }

    @Override
    public <T> T deserisation(byte[] donnee, Class<T> type) throws SerialisationException {
        try{
            //Convertit le tableau de bytes en Json pour ensuite le convertir en objet Java du type spécifié
            return mapper.readValue(donnee,type);
        }
        catch (Exception e) {
            throw new SerialisationException("Erreur de désérialisation JSON : " + e.getMessage());
        }
    }
}
