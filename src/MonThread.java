import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.MarshalException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

    /**
     * L'agent
     */
    private Agent agent;

    /**
     * Les requêtes qui viennent de l'autre agent.
     */
    private ArrayList<Document> requetes;

    /**
     * Les réponses qui viennent de l'autre agent.
     */
    private ArrayList<Document> reponse;


    /**
     * Constructeur
     * @param agent, l'agent
     * @param lesRequetes, Les requetes qui sont envoyé par l'autre agent.
     * @param lesReponses, les reponses qui sont envoyé par l'autre agent.
     */
    public MonThread(Agent agent, ArrayList<Document> lesRequetes, ArrayList<Document> lesReponses) {
        this.agent = agent;
        requetes = lesRequetes;
        reponse = lesReponses;


    }


    /**
     * Le run du Thread.
     */
    @Override
    public void run() {

        // On vérifie la signature de chaque requête envoyée par l'autre agent.
        verifieLesSignature(requetes);

        try {
            // On envoie les réponses en requête lue précèdament.
            envoyerLaReponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        verifieLesSignature(reponse); // On verifie les reponses reçu

        // On affiche
        affichageReponses();
    }


    /**
     * Fonction qui permet d'afficher les réponses en fonction du document dans la liste.
     */
    public void affichageReponses(){

        for (int i = 0; i < reponse.size(); i++) {
            String[] resulat = new String[2];

            // Recherche de la balise <QUERY> et récupérer l'intérieur
            NodeList queryList = reponse.get(i).getElementsByTagName("QUERY");
            if (queryList.getLength() > 0) {
                Element queryElement = (Element) queryList.item(0);
                String result = queryElement.getTextContent().trim();
                resulat[0] = result;
            }

            // Recherche de la balise <RESULT> et récupérer l'intérieur
            NodeList resultList = reponse.get(i).getElementsByTagName("RESULT");
            if (resultList.getLength() > 0) {
                Element resultElement = (Element) resultList.item(0);
                String result = resultElement.getTextContent().trim();
                resulat[1] = result;
            }

            System.out.println("L'agent qui porte le nom "+ agent.getNom() + " à eu une response à sa requête: " + resulat[0]+".\n La réponse est :\n\n\t" + resulat[1]+"\n\n");
        }
    }


    /**
     * Fonction qui permet de vérifier les signatures des documents données en paramètre.
     * @param list, list qui contient plusieurs documents à l'interieur.
     */
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


    /**
     * Fonctino qui permet de créer le fichier et d'écrire les réponses à l'intérieur.
     * @throws Exception
     */
    public void envoyerLaReponse() throws Exception {
        for (int i = 1; i <= requetes.size(); i++) {
            String resultat = "<REPONSE>\n\t<QUERY>\n";
            resultat += "\t\t" + recupererRequet(i - 1) + "\n\t</QUERY> \n \t<RESULT> \n";
            try {
                if (requetesDansBDD(recupererRequet(i - 1))) {
                    resultat += "\t\tOUI, j'ai l'information de cette requête dans ma Base de donnée.\n\t\t" + agent.getNom();

                } else {
                    resultat += "\t\tNON, je n'ai pas l'information de cette requête dans ma Base de donnée. \n\t\t" + agent.getNom();

                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
            resultat += "\n \t</RESULT>\n</REPONSE>";

            // Écrire le résultat dans un fichier
            String path = "src/XML/" + agent.getNom() + "/reponsesEnvoyer/reponse" + (i) + ".xml";
            File file = new File(path);

            // Création des dossiers si nécessaire (normalement ils sont déjà créés)
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(resultat);
                writer.flush(); // Assure que toutes les données sont écrites sur le disque
                writer.close();

            } catch (IOException e) {
                System.out.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
            }

            // Signer le document
            Document document = agent.loadXMLDocumentFromResource("src/XML/" + agent.getNom() + "/reponsesEnvoyer/reponse" + (i) + ".xml");
            agent.signerDocument(document);

            // Ajouter le document signé à la liste de réponses
            agent.getReponse().add(document);
        }
    }

    /**
     * Fonction qui permet de regarder si l'expression est dans la base de donnée de l'agent.
     * @param expression, l'expression à vérifier.
     * @return return True si l'expression est dans la base de donnée, non sinon.
     * @throws XPathExpressionException
     */
    public boolean requetesDansBDD(String expression) throws XPathExpressionException {

        XPath xpath = XPathFactory.newInstance().newXPath();
        // Créer une expression XPath pour rechercher tous les éléments avec ce tag et cette valeur
        //System.out.println(recupererRequet(i));

        XPathExpression expr = xpath.compile(expression);

        // Évaluer l'expression XPath sur le document
        NodeList nodes = (NodeList) expr.evaluate(agent.getBdd(), XPathConstants.NODESET);

        // Si la liste des nœuds n'est pas vide, l'élément existe
        return nodes.getLength() > 0 ;
    }


    /**
     * Fonction qui permet de récuperais une requête.
     * @param i, le numéro de la requete à récupérer.
     * @return
     */
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


            return xpathExpression;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }









}
