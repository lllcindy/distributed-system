import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

/*
 *
 * @author Xindi Lan
 * @date 15/10/2019
 *
 * The TCPClient class runs as a client by using TCP to
 * sent json request to ask server to do the corresponding
 * operation to the blockchain, and receive the message
 * returned by server. And it can also use hash to encrypt
 * and sign the message.
 *
 */

public class TCPClient {
    public static void main(String args[]) throws Exception{
        // To inform the client is running
        System.out.println("Client Running!");
        Socket clientSocket = null;
        try {
            // Determine the address and port number
            String addr = "localhost";
            int serverPort = 6789;
            String m = "";
            // give user the prompt to give the input
            System.out.println("0. View basic blockchain status.");
            System.out.println("1. Add a transaction to the blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Corrupt the chain.");
            System.out.println("5. Hide the corruption by repairing the chain.");
            System.out.println("6. Exit.");
            Operate(addr, serverPort, clientSocket);
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    /**
     * This method is to connect client to server and pass
     * the json message to server. It also encrypt and sign the message.
     *
     * @param Addr the host name of server
     * @param serverPort the port number that will be used
     * @param clientSocket the socket used to connect to server
     */
    private static void Operate(String Addr, int serverPort, Socket clientSocket)throws Exception {
        BigInteger n; // n is the modulus for both the private and public keys
        BigInteger e; // e is the exponent of the public key
        BigInteger d; // d is the exponent of the private key
        Random rnd = new Random();

        // Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        // Change 400 to 2048, recompile, and run the program again and you will
        // notice it takes much longer to do the math with that many bits.
        BigInteger p = new BigInteger(400,100,rnd);
        BigInteger q = new BigInteger(400,100,rnd);

        // Compute n by the equation n = p * q.
        n = p.multiply(q);

        // Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger ("65537");

        // Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);

        //Use e+n to compute hash value as id for users
        String input = e.toString()+n.toString();
        String id = convertToHex(getHash(input));

        // Create the connection with server and use bufferedreader to get the input from user, and printwriter to write to server
        clientSocket = new Socket(Addr, serverPort);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
        String nextLine;
        while ((nextLine = typed.readLine()) != null) {
            //create a JsonObject for storing the request
            JsonObject obj=new JsonObject();
            //If the input is not in (1-6), then give a friendly message and start a new loop
            if (!nextLine.equals("0")&&!nextLine.equals("1")&&!nextLine.equals("2")&&!nextLine.equals("3")&&!nextLine.equals("4")&&!nextLine.equals("5")&&!nextLine.equals("6")){
                System.out.println("The input is not valid. Please input the operation number(0,1,2,3,4,5,6) again:");
                continue;
            }
            // If the input is 6, then send the massage to server, break the loop and end the connection
            if (nextLine.equals("6")) {
                obj.put("operation", nextLine);
                //Transform the JsonObject into string
                String req = Jsoner.serialize(obj);
                out.println(req);
                out.flush();
                System.out.println("Client is Quitting.");
                break;
            } else {
                //Ask for add a new block
                if (nextLine.equals("1")){
                    Scanner input1 = new Scanner(System.in);
                    System.out.println("Enter difficulty > 0: ");
                    int diff=0;
                    int i;
                    // To check whether the inputted difficulty is an integer. If not, give a prompt to input again
                    do{
                        do {
                            i = 0;
                            try {
                                diff = input1.nextInt();
                            } catch (Exception e1) {
                                System.out.println("It is not a Integer. Please enter difficulty again:");
                                i = i + 1;
                                input1.nextLine();
                            }
                        }while (i == 1);
                        // To check whether the inputted difficulty is larger than 0. If not, give a prompt to input again
                        if (diff < 0) {
                            System.out.println("Difficulty cannot be less than 0. Please enter difficulty again:");
                        }
                    }while(diff<0);
                    // Accept the inputted transaction from user
                    Scanner input2 = new Scanner(System.in);
                    System.out.println("Please input the transaction: ");
                    String transac = input2.nextLine();
                    //put the operation number, difficulty and transaction into the JsonObject
                    obj.put("operation", nextLine);
                    obj.put("difficulty", diff);
                    obj.put("transaction", transac);
                    nextLine = nextLine+","+diff+","+transac;
                }
                //Ask for corrupting the block chain
                else if (nextLine.equals("4")){
                    Scanner input3 = new Scanner(System.in);
                    Scanner input4 = new Scanner(System.in);
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter block ID of block to Corrupt: ");
                    // To check whether the inputted BlockId is an integer. If not, give a prompt to input again
                    int blockId =0;
                    int i;
                    do{
                        do {
                            i = 0;
                            try {
                                blockId = input3.nextInt();
                            } catch (Exception e1) {
                                System.out.println("It is not a Integer. Please enter BlockId again:");
                                i = i + 1;
                                input3.nextLine();
                            }
                        }while (i == 1);
                        // Check whether the blockId is larger than 0. If not, give a prompt to input again
                        if (blockId< 0) {
                            System.out.println("Block Id cannot be less than 0. Please enter BlockId again:");
                        }
                    }while(blockId<0);
                    System.out.println("Enter new data for block "+ blockId +": ");
                    // Accept the inputted transaction from user
                    String data = input4.nextLine();
                    // Put operation number, blockId and transaction into the JsonObject
                    obj.put("operation", nextLine);
                    obj.put("BlockId", blockId);
                    obj.put("transaction", data);
                    nextLine = nextLine+","+blockId+","+data;
                }
                //Other operation doesn't need additional input, so just put operation number into the JsonObject
                else{
                    obj.put("operation", nextLine);
                }
                // Use id and all the input as the string to be signed
                String sig = id+","+nextLine;
                // Generate signature using Sign method
                String signature = Sign(sig, d, n);
                // put is, e, n and signature into the JsonObject
                obj.put("id", id);
                obj.put("e", e.toString());
                obj.put("n", n.toString());
                obj.put("signature", signature);
                //Transform the JsonObject into string
                String req = Jsoner.serialize(obj);
                out.println(req);
                // Pass to server
                out.flush();
                String data = in.readLine(); // read a line of data from the stream
                // Transform the string into JsonObject
                JsonObject rep =  (JsonObject)(Jsoner.deserialize(data));
                String sig1 = rep.get("id") + "," + rep.get("reply");
                String signature1 = (String)rep.get("signature");
                BigInteger e1 = new BigInteger((String)rep.get("e"));
                BigInteger n1 = new BigInteger((String)rep.get("n"));
                boolean verif = Verify(sig1, signature1, e1, n1);
                if (verif) {
                    // Use get method to get the replied string in it
                    String reply = (String) rep.get("reply");
                    // Use the sign to split the string and print them line by line
                    String[] receive = reply.split("~");
                    for (int i = 0; i < receive.length; i++) {
                        System.out.println(receive[i]);
                    }
                }
                else{
                    System.out.println("Cannot verrify the reply from server.");
                }
                System.out.println("0. View basic blockchain status.");
                System.out.println("1. Add a transaction to the blockchain.");
                System.out.println("2. Verify the blockchain.");
                System.out.println("3. View the blockchain.");
                System.out.println("4. Corrupt the chain.");
                System.out.println("5. Hide the corruption by repairing the chain.");
                System.out.println("6. Exit.");
            }
        }
    }

    /**
     * This method can convert the plain text into hash
     * value and return array of bytes
     *
     * @param text the String need to be converted into hash
     * @return the array of byte converted from text
     */
    private static byte[] getHash(String text){
        try {
            // Get the corresponding instance of specific hash type
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(text.getBytes());
//            byte[] by = new byte[20];
//            System.arraycopy(b, b.length-20, by, 0, 20);
            return b;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                    + " for incorrect algorithm: " + e);
            return null;
        }
    }

    /**
     * This method convert the result of getHash into string based message
     *
     * @param data the array of byte returned from getHash method
     * @return a String converted from the array of byte
     */
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        String r = buf.toString();
        return r.substring(r.length()-20); //Get the last 20 bytes as id
//        return buf.toString(); //Get the last 20 bytes as id
    }

    /**
     * This method is used to sign the massage and get the signature
     *
     * @param message the message string be signed
     * @param d the exponent of the private key
     * @param n the modulus for both the private and public keys
     * @return the String of signature
     */
    public static String Sign(String message, BigInteger d, BigInteger n) throws Exception {

        // compute the digest with SHA-256
        byte[] bytesOfMessage = message.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bigDigest = md.digest(bytesOfMessage);

        // we only want two bytes of the hash for BabySign
        // we add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageDigest = new byte[3];
        messageDigest[0] = 0;   // most significant set to 0
        messageDigest[1] = bigDigest[0]; // take a byte from SHA-256
        messageDigest[2] = bigDigest[1]; // take a byte from SHA-256 The message digest now has three bytes. Two from SHA-256 and one is 0

        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this big integer string as signature
        return c.toString();
    }

    /**
     * This method is used to verify whether the message sent is well signed by client
     *
     * @param messageToCheck the message that needs to be verified
     * @param encryptedHashStr the signature
     * @param e the exponent of the public key
     * @param n the modulus for both the private and public keys
     * @return a boolean value to represent whether the message is well signed
     */
    public static boolean Verify(String messageToCheck, String encryptedHashStr, BigInteger e, BigInteger n)throws Exception  {

        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedHashStr);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        // Get the bytes from messageToCheck
        byte[] bytesOfMessageToCheck = messageToCheck.getBytes("UTF-8");

        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);

        // messageToCheckDigest is a full SHA-256 digest
        // take two bytes from SHA-256 and add a zero byte
        byte[] extraByte = new byte[3];
        extraByte[0] = 0;
        extraByte[1] = messageToCheckDigest[0];
        extraByte[2] = messageToCheckDigest[1];

        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(extraByte);

        // Compare the two value. If they are equal, then return true. If not, return false
        if(bigIntegerToCheck.compareTo(decryptedHash) == 0) {
            return true;
        }
        else {
            return false;
        }
    }
}