import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;

public class Server implements Auction {
    private Map<Integer, AuctionItem> auctionItems = new HashMap<>(); //Used to store an item using integers to identify them.
    SecretKey aesKey;
    public Server() {
        super();

        try{

            aesKey = generateKey();
            auctionItems.put(1, new AuctionItem(1, "PC", "Windows", 100));// Two hardcoded items for testing
            auctionItems.put(2, new AuctionItem(2, "MAC", "Apple", 150));

            String keyFilePath = "../keys/testKey.aes"; //the keys directory already exists in the root so this is the file path
            saveKeyToFile(aesKey,keyFilePath);//stores the generated key in the key directory
            
            System.out.println("AES Key Size: " + aesKey.getEncoded().length); // used for debugging the size

        } catch (Exception e){
            System.err.println("Exception");
            e.printStackTrace();
        }
    }

    // Below code within main is nearly the same as the java primer rmi example but the difference is the name of the RMI service being "Auction"
    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }



    // This function exists to use a key generator using the AES algorithm and generating a key of 32 byte size and returning it to the variable aesKey
    private static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // You can choose 128, 192, or 256 bits key size.
        return keyGen.generateKey();
    }


    // Takes the key and filepath for the key directory as parameters and writes the testKey,aes file using the key
    private static void saveKeyToFile(SecretKey key, String fileName) throws IOException {
        byte[] keyBytes = key.getEncoded();
        try (FileOutputStream keyFile = new FileOutputStream(fileName)) {
            keyFile.write(keyBytes);
        }
    }

    // As the Client uses an RMI service it will call upon the getSpec function of the server to look up an auction item and when it does the item is encrypted with the aeskey and returned to the client for decryption
    public SealedObject getSpec(int itemID) throws RemoteException {
        if (auctionItems.containsKey(itemID)) {
            AuctionItem item = auctionItems.get(itemID);
            return encryptItem(item, aesKey);
        } else {
            AuctionItem notFoundItem = new AuctionItem(-1, "Item not found", "Item not in the auction", 0);
            return encryptItem(notFoundItem, aesKey);
        }
    }


    
    // Uses the cipher class library to encrypt the item with the aeskey and returned back to getSpec so that it can reach the client
    public SealedObject encryptItem(AuctionItem item, SecretKey aesKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
    
            // Debugging statement to print the AES key size
            System.out.println("AES Key Size for Encryption: " + aesKey.getEncoded().length);
    
            return new SealedObject(item, cipher);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
}
