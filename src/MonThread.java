import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class MonThread extends Thread {
    private Agent agent;

    /**
     * Les requettes qui viennent de l'autre agent.
     */
    private ArrayList<Document> requetes;

    private ArrayList<Document> reponse;


    public MonThread(Agent agent, ArrayList<Document> lesRequetes, ArrayList<Document> lesReponses) {
        this.agent = agent;
        requetes = lesRequetes;
        reponse = lesReponses;


    }


    @Override
    public void run() {
        verifieLesSignature(requetes); // On verifie d'abord les signature des requettes
        try {
            envoyerLaReponse(); // On envoie la reponse
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        verifieLesSignature(reponse); // On verifie les reponses reçu

        // On affiche
    }

    public void verifieLesSignature(ArrayList<Document> list){
        Boolean resultat;
        Document doc;
        for (int i= 1; i <= list.size(); i++) {
            doc = list.get(i-1);
            try {
                resultat = agent.validerSignature(doc);
                if( resultat ==  false){
                    System.out.println("Signature fause pour le fichier numéro :" + i);
                    list.remove(i-1);
                }
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            } catch (MarshalException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void envoyerLaReponse() throws Exception {
        for(int i= 1; i<= requetes.size() ; i++){
            String resultat = "<Reponse>\n<QUERY>\n";
            resultat += recupererRequet(i-1) + "\n</QUERY> \n <RESULT> \n";
            try {
                if(requetesDansBDD(i-1)){
                    resultat += "OUI, L'agent de nom " + agent.getNom() + " à l'information de cette requete dans sa Base de donnée." ;

                }else{
                    // System.out.println("noon");
                    resultat += "NON, L'agent de nom " + agent.getNom() + " n'à pas l'information de cette requete dans sa Base de donnée." ;

                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
            resultat += "\n</RESULT>\n</Reponse>";

            // Écrire le résultat dans un fichier
            // Construction du chemin de fichier
            String path = "src/ressource/XML/" + agent.getNom() + "/reponsesEnvoyer/reponse" + (i ) + ".xml";
            File file = new File(path);

            // Création des dossiers si nécessaire
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(resultat);
                System.out.println("Le résultat a été écrit dans le fichier : " + file.getPath());
            } catch (IOException e) {
                System.out.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
            }

            Document document = agent.loadXMLDocumentFromResource("XML/" + agent.getNom() + "/reponsesEnvoyer/reponse" + (i ) + ".xml");
            agent.signerDocument(document);
            agent.getReponse().add(document);
        }
    }

    public boolean requetesDansBDD(int i) throws XPathExpressionException {

        XPath xpath = XPathFactory.newInstance().newXPath();
        // Créer une expression XPath pour rechercher tous les éléments avec ce tag et cette valeur
        System.out.println(recupererRequet(i));
        String expression = recupererRequet(i);
        XPathExpression expr = xpath.compile(expression);

        // Évaluer l'expression XPath sur le document
        NodeList nodes = (NodeList) expr.evaluate(agent.getBdd(), XPathConstants.NODESET);

        // Si la liste des nœuds n'est pas vide, l'élément existe
        return nodes.getLength() > 0 ;
    }

    public String recupererRequet(int i){
        try {
            Document document = requetes.get(i);

            // Obtention du premier élément QUERY du document
            NodeList queryNodes = document.getElementsByTagName("QUERY");
            if (queryNodes.getLength() == 0) {
                return null; // Aucun élément QUERY trouvé
            }

            // Lecture du contenu de l'élément QUERY
            String xpathQuery = queryNodes.item(0).getTextContent().trim();

            String[] parts = xpathQuery.split("\n", 2); // Séparation de la chaîne en deux parties sur la première occurrence de saut de ligne

            String xpathExpression = parts[0]; // Première partie est l'expression XPath

            // Retourner l'expression XPath complète
            return xpathExpression;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // En cas d'erreur
    }



    public void lireLesReponse(){

    }






}
