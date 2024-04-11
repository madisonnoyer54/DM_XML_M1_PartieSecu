import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PairClef {
    private KeyPair pair;

    PairClef() throws NoSuchAlgorithmException {
        pair = generateKeyPair();
    }


    /**
     * Méthode pour générer une paire de clés DSA
     * @return la pair de clés
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Taille de la clé de 2048 bits
        // KeyPair kp et kpg.generateKeyPair();
       // X509Certificate cert = (X509Certificate) pair.getCertificate();

        return keyPairGenerator.generateKeyPair();
    }


    /**
     * Méthode pour encoder la clé publique en Base64
     * @return la clef public a encoder
     */
    public String encodePublicKey() {
        byte[] publicKeyBytes = pair.getPublic().getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }


    /**
     * Méthode pour décoder une clé publique encodée en Base64
     * @param encodedPublicKey la clé publique encodée en Base64
     * @return la clé publique décodée
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public PublicKey decodePublicKey(String encodedPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(encodedPublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }



    public PrivateKey getPrivate() {
        return pair.getPrivate();
    }

    public PublicKey getPublic() {
        return  pair.getPublic();
    }
}
