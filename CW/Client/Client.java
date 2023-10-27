import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.crypto.SealedObject;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;


public class Client{
    public static void main(String[] args) {
        if (args.length < 1) {
        System.out.println("Usage: java Client n");
        return;
        }
 
          int n = Integer.parseInt(args[0]);
          try {
                String name = "Auction"; // For the coursework Auction is the name of the RMI service that the client needs to connect to so that it can access info from Server Class
                Registry registry = LocateRegistry.getRegistry("localhost");
                Auction server = (Auction) registry.lookup(name);
                String keyFilePath = "../keys/testKey.aes";  //The client needs the aesKey for the decryption of data that is received from the server.
                SecretKey aesKey = readAESKeyFromFile(keyFilePath);
                SealedObject sealed = server.getSpec(n);    // Using what ID the user parses in, the client will request it from the server and it is a sealed object as its encrypted.
                 
                if (sealed != null) {                       // Used to catch if any invalid items are entered by user.
                  AuctionItem result = decryptData(sealed, aesKey);
  
                  System.out.println("Item ID: " + result.itemID);  //Multiple print statements for parts of the item.
                  System.out.println("Name: " + result.name);
                  System.out.println("Description: " + result.description);
                  System.out.println("Highest Bid: " + result.highestBid);
              } else {
                  System.out.println("Item not found.");
              }
               }
               catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
                }
       }

    // This function reads the key from the testkey.AES file
    private static SecretKey readAESKeyFromFile(String fileName) throws Exception {
        try (FileInputStream keyFile = new FileInputStream(fileName)) {
            byte[] keyBytes = new byte[keyFile.available()];
            keyFile.read(keyBytes);
            return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
        }
    }

    // Using the cipher class library, the key and sealed object will be parameters for decryption to be displayed back to the user as result.
    public static AuctionItem decryptData(SealedObject sealedObject, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        //byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return (AuctionItem)sealedObject.getObject(cipher);
    }
    
}
