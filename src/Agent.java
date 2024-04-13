import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;

public class Agent {
    /**
     * La paire de clef de l'agent
     */
    private PairClef keyPair;

    /**
     * La clef publique que l'autre agent lui à partager
     */
    private String publicKeyAutre;

    /**
     * La Base de Donnée de l'agent
     */
    private Document bdd;

    /**
     * Document qu'il veut envoyer, ils sont déja signé.
     */
    private ArrayList<Document> requetes;

    /**
     * Les reponsess à envoyer à l'autre agent
     */
    private ArrayList<Document> reponse;


    /**
     * Le nom de l'agent.
     */
    private String nom;


    /**
     * Constructeur
     * @param nom, le nom de l'agent
     */
    public Agent(String nom) {
        this.nom = nom;

        try {
            chargerBDD();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        // On lui génère sa paire de clef
        try {
            keyPair = new PairClef() ;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        requetes = new ArrayList<>();
        reponse = new ArrayList<>();

        // Charger les requettes
        requetes();
    }


    /**
     * Fonction qui permet de chager la base de donnée de l'agent.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public void chargerBDD() throws ParserConfigurationException, IOException, SAXException {

        // Initialiser le document XML à partir d'un fichier
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // important pour les requêtes XPath
        DocumentBuilder builder = factory.newDocumentBuilder();
        //bdd = builder.parse(Agent.class.getClassLoader().getResourceAsStream("XML/" + nom + "/BDD.xml"));
        bdd = builder.parse("src/XML/" + nom + "/BDD.xml");

    }


    /**
     * Fonction qui permet de récuperais toute les requets et de les signer.
     */
    public void requetes(){

        Document document;
        for (int i = 1; i <= compteFichier(); i++) {
            try {
                document = loadXMLDocumentFromResource("src/XML/" + nom + "/requetes/requete"+i+".xml");
                document = signerDocument(document);
                requetes.add(document);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Fonction qui permet de compter le nombre de fichier dans un dossier.
     * @return, le nombre de fichier.
     */
    public int compteFichier() {
        // Obtention de l'URL du dossier
        URL folderUrl = Agent.class.getClassLoader().getResource("XML/" + nom + "/requetes");
        if (folderUrl == null) {
            throw new IllegalArgumentException("Le dossier spécifié n'existe pas.");
        }

        try {
            // Conversion de l'URL en chemin de fichier
            File folder = new File(folderUrl.toURI());

            // Vérification que le chemin correspond à un dossier existant
            if (!folder.exists() || !folder.isDirectory()) {
                throw new IllegalArgumentException("Le chemin spécifié n'est pas un dossier valide.");
            }

            // Comptage des fichiers dans le dossier
            File[] files = folder.listFiles();
            if (files == null) {
                throw new RuntimeException("Impossible de lister les fichiers dans le dossier spécifié.");
            }

            return files.length;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Erreur lors de la conversion de l'URL en URI : " + e.getMessage());
        }
    }


    /**
     * Fonction qui permet de démarrer le Thread de l'agent, donc la partage des documents.
     */
    public void demarrageThread(ArrayList<Document> lesRequettes, ArrayList<Document> lesReponses){

        Thread threadAgent = new MonThread(this, lesRequettes, lesReponses);
        threadAgent.start();
    }


    /**
     * Fonction qui permet de charger les documents.
     * @param chemin, le chemin du document à récupérer.
     * @return le doucment récupérer.
     * @throws Exception
     */
    public Document loadXMLDocumentFromResource(String chemin) throws Exception {


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(chemin);


        return doc;
    }


    /**
     * Fonction qui permet de signer un document
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws KeyException
     * @throws MarshalException
     * @throws XMLSignatureException
     */
    public Document signerDocument(Document document) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, KeyException, MarshalException, XMLSignatureException {
        // Contexte de singature
        DOMSignContext dsc = new DOMSignContext(keyPair.getPrivate(), document.getDocumentElement());
        // Assemblage de la signature XML
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

        // Assemblage de la signature XML
            // Creation d'un reference objet
        Reference ref = factory.newReference(
                "", // URI
                factory.newDigestMethod(DigestMethod.SHA256, null), // Méthode de hachage
                Collections.singletonList( // Liste de transformations
                        factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                ),
                null,
                null
        );

            // Création de SignedInfo
        SignedInfo si = factory.newSignedInfo(factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), factory.newSignatureMethod(SignatureMethod.RSA_SHA256, null), java.util.Collections.singletonList(ref));

            // option keyInfObjet
        KeyInfoFactory kif =  factory.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(keyPair.getPublic());
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

        // signature document
        XMLSignature signature = factory.newXMLSignature(si, ki);
        signature.sign(dsc);

        return document;
    }


    /**
     * Fonction qui permet de valider la signature du document donnée
     * @param doc, le document a signer.
     * @return
     * @throws ParserConfigurationException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws MarshalException
     * @throws XMLSignatureException
     */
    public Boolean validerSignature(Document doc) throws ParserConfigurationException, NoSuchAlgorithmException, InvalidKeySpecException, MarshalException, XMLSignatureException {
        // Instanti selon lequel le document contient Signature
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();

        // preciser l'élément de signature valide
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        // Vérification s'il y a un seul élément Signature dans le document
        if (nl.getLength() != 1) {
            throw new RuntimeException("L'élément de signature n'a pas été trouvé ou est en double dans le document.");
        }

        // Récupération de la clé publique de l'autre agent.
        PublicKey publicKey = keyPair.decodePublicKey(publicKeyAutre);


        // Creation d'un contexte de validation
        DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));

        // desamer la signature XML
        XMLSignatureFactory usine = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = usine.unmarshalXMLSignature(valContext);

        // validation de la signature
        return signature.validate(valContext);
    }


    /**
     * Getteur de la base de donnée.
     * @return
     */
    public Document getBdd() {
        return bdd;
    }


    /**
     * Getteur de la paire de clef
     * @return
     */
    public PairClef getKeyPair() {
        return keyPair;
    }


    /**
     * Setteur de la clef publique de l'autre agent.
     * @param publicKey, la nouvelle clef.
     */
    public void setPublicKeyAutre(String publicKey) {
        this.publicKeyAutre = publicKey;
    }


    /**
     * Getteur de la liste de requets.
     * @return
     */
    public ArrayList<Document> getRequetes() {
        return requetes;
    }


    /**
     * Getteur du nom.
     * @return
     */
    public String getNom() {
        return nom;
    }


    /**
     * Getteur de la liste des réponses.
     * @return
     */
    public ArrayList<Document> getReponse() {
        return reponse;
    }

}
