import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;

import static java.lang.Thread.sleep;

public class Main {

    /**
     * Agent numéro 1.
     */
    private static Agent agent1;

    /**
     * Agent numéro 2.
     */
    private static Agent agent2;


    /**
     * Le main.
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {

        agent1 = new Agent("Agent1");
        agent2 = new Agent("Agent2");

        // On transmet les clefs
        agent1.setPublicKeyAutre(agent2.getKeyPair().encodePublicKey());

        agent2.setPublicKeyAutre(agent1.getKeyPair().encodePublicKey());

        // On active les threads des agents en passent les réponses et requêtes de l'autre agent
       agent1.demarrageThread(agent2.getRequetes(), agent2.getReponse());
       agent2.demarrageThread(agent1.getRequetes(), agent1.getReponse());


    }




}