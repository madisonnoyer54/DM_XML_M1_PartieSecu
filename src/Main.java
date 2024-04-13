
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