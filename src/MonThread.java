import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class MonThread extends Thread {
    private Agent agent;

    /**
     * Les requettes qui viennent de l'autre agent.
     */
    private ArrayList<Document> requetes;

    /**
     * nom de l'autre agent
     */
    private String nom;

    public MonThread(Agent agent, ArrayList<Document> lesRequetes, String nom) {
        this.agent = agent;
        this.nom = nom;
        requetes = lesRequetes;


    }


    @Override
    public void run() {
        verifieLesSignature();
    }

    public void verifieLesSignature(){
        Boolean resultat;
        Document doc;
        for (int i= 1; i <= requetes.size(); i++) {
            doc = requetes.get(i-1);
            try {
                resultat = agent.validerSignature(doc);
                if( resultat ==  false){
                    System.out.println("Signature fause pour le fichier numéro :" + i);
                }else{
                    envoyerLaReponse(i-1);
                }
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            } catch (MarshalException e) {
                throw new RuntimeException(e);
            } catch (XMLSignatureException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void envoyerLaReponse(int i) {
        try {
            if(requetesDansBDD(i)){
                System.out.println("oui");

            }else{
                System.out.println("noon");

            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean requetesDansBDD(int i) throws XPathExpressionException {
        Boolean resultat = false;
        String attribue1 = null, attribue2 = null;

        /*
        // Séparer la chaîne en fonction du signe égal '='
        String[] parts = requet.split("=");

        // Si la chaîne est correctement séparée en deux parties
        if (parts.length == 2) {
            // Récupérer le 1er attribue
             attribue1 = parts[0].trim();
            // Récupérer le second attribue
             attribue2 = parts[1].replaceAll("'", "").trim();
        }

        System.out.println( attribue1);
        System.out.println( attribue2);

         */



        XPath xpath = XPathFactory.newInstance().newXPath();
        // Créer une expression XPath pour rechercher tous les éléments avec ce tag et cette valeur
        System.out.println(recupererRequet(i));
        String expression = recupererRequet(i);
        XPathExpression expr = xpath.compile(expression);

        // Évaluer l'expression XPath sur le document
        NodeList nodes = (NodeList) expr.evaluate(agent.getBdd(), XPathConstants.NODESET);

        // Si la liste des nœuds n'est pas vide, l'élément existe
        return nodes.getLength() > 0;
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
