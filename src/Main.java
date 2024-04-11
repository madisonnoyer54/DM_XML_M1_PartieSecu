import java.security.NoSuchAlgorithmException;

public class Main {
    private static Agent agent1;
    private static Agent agent2;

    /**
     * Main
     * @param args
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {

        agent1 = new Agent("Agent1",3);

        agent2 = new Agent("Agent2",3);

        // On transmet les clef
        agent1.setPublicKeyAutre(agent2.getKeyPair().encodePublicKey());

        agent2.setPublicKeyAutre(agent1.getKeyPair().encodePublicKey());

        // On active les thread des agents
       agent1.demarrageThread(agent2.getRequetes(), agent2.getNom());
       agent2.demarrageThread(agent1.getRequetes(), agent2.getNom());

    }


}