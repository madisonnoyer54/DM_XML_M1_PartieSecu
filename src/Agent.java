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
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;

public class Agent {
    /**
     * La pair de clef de l'agent
     */
    private PairClef keyPair;

    /**
     * La clef public que l'autre agent lui à partagé
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
     *
     */
    private ArrayList<Document> reponse;


    /**
     * Le nom de l'agent.
     */
    private String nom;

    /**
     * Permet de savoir le nombre de requet envoyer de l'autre agent.
     */
    private int nombreDocument;



    /**
     * Constructeur
     */
    public Agent(String string, int nombreDoc) {
        nombreDocument = nombreDoc;
        nom = string;

        try {
            chargerBDD();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        // On lui génére sa pair de clef
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

    public void chargerBDD() throws ParserConfigurationException, IOException, SAXException {
        // Initialiser le document XML à partir d'un fichier
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // important pour les requêtes XPath
        DocumentBuilder builder = factory.newDocumentBuilder();
        bdd = builder.parse(Agent.class.getClassLoader().getResourceAsStream("XML/" + nom + "/BDD.xml"));
    }


    /**
     * Fonction qui permet de récuperais toute les requets et de les signer.
     */
    public void requetes(){
        Document document;
        for (int i = 1; i <= nombreDocument; i++) {
            try {
                document = loadXMLDocumentFromResource("XML/" + nom + "/requetes/requete"+i+".xml");
                document = signerDocument(document);
                requetes.add(document);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Fonction qui permet de démarrer le Thread de l'agent, donc la pertage des document.
     */
    public void demarrageThread(ArrayList<Document> lesRequettes, String nom){
        Thread threadAgent = new MonThread(this, lesRequettes, nom);
        threadAgent.start();
    }


    public Document loadXMLDocumentFromResource(String chemin) throws Exception {
        InputStream inputStream = Agent.class.getClassLoader().getResourceAsStream(chemin);
        if (inputStream == null) {
            throw new Exception("Le fichier n'a pas été trouvé");
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        inputStream.close();

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
                null, // Type
                null // ID
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

        // LA faut voir pour déencoder la clef public de l'autree
        // Récupération de la clé publique
        PublicKey publicKey = keyPair.decodePublicKey(publicKeyAutre);


        // Creation d'un contexte de validation
        DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));

        // desamer la signature XML
        XMLSignatureFactory usine = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = usine.unmarshalXMLSignature(valContext);

        // validation de la signature
        return signature.validate(valContext);
    }

    public Document getBdd() {
        return bdd;
    }

    public PairClef getKeyPair() {
        return keyPair;
    }

    public void setPublicKeyAutre(String publicKey) {
        this.publicKeyAutre = publicKey;
    }

    public ArrayList<Document> getRequetes() {
        return requetes;
    }

    public String getNom() {
        return nom;
    }


    public ArrayList<Document> getReponse() {
        return reponse;
    }

    public boolean verifieRequeteBDD(Document requete){
        Boolean resultat = false;


        return resultat;
    }


}
