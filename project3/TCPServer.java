import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.github.cliftonlabs.json_simple.JsonObject;

/*
 *
 * @author Xindi Lan
 * @date 15/10/2019
 *
 * The TCPServer class runs as a server by using TCP to
 * receive request from client. First it verify the
 * signature and the hash value. Once that done, it will
 * opearte the block chain based on the request, and then
 * return back the message to client.
 *
 */

public class TCPServer {
    public static void main(String args[]) throws Exception{
        System.out.println("Server Running!");
        Socket clientSocket = null;
        try{
            // the server port we are using
            int serverPort = 6789;
            HashMap<String, Integer> map = new HashMap<>();
            // Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);
            Add(clientSocket, listenSocket, map);
            // Handle exceptions
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
            // If quitting (typically by you sending quit signal) clean up sockets
        }finally {
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
     * This method is to connect client to server.It also
     * verify the message and can operate the blockchain.
     * Finally, it will return the message back to client.
     *
     * @param clientSocket the socket used to connect to server
     * @param listenSocket the socket used for server to accept the connection from client
     * @param map stores the pairs of id and values
     */
    private static void Add(Socket clientSocket, ServerSocket listenSocket, HashMap<String, Integer> map) throws Exception{
        BigInteger n1; // n is the modulus for both the private and public keys
        BigInteger e1; // e is the exponent of the public key
        BigInteger d1; // d is the exponent of the private key
        Random rnd = new Random();

        // Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        // Change 400 to 2048, recompile, and run the program again and you will
        // notice it takes much longer to do the math with that many bits.
        BigInteger p = new BigInteger(400,100,rnd);
        BigInteger q = new BigInteger(400,100,rnd);

        // Compute n by the equation n = p * q.
        n1 = p.multiply(q);

        // Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e1 = new BigInteger ("65537");

        // Compute d as the multiplicative inverse of e modulo phi(n).
        d1 = e1.modInverse(phi);

        //create a blockchain object
        BlockChain bc = new BlockChain();
        Date date1 = new Date();
        // The time stamp is used for create block
        Timestamp t1= new Timestamp(date1.getTime());
        int count = 0;
        // Create a new block
        Block b1 = new Block(count, t1, "block1", 2);
        b1.setPreviousHash("");
        bc.addBlock(b1);
        Scanner input = new Scanner(System.in);
        //To let server always run. After client closed, it will launch a new loop and create a new listencosket
        while (true) {
            try {
                // Create the connection and use scanner to read the request from client, and printwriter to write to client
                clientSocket = listenSocket.accept();
                Scanner in = new Scanner(clientSocket.getInputStream());
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
                while(true) {
                    String data = in.nextLine();
                    // create a jsonobect for sending message
                    JsonObject obj =  (JsonObject)(Jsoner.deserialize(data));
                    // create another JsonObject for storing the reply
                    JsonObject reply = new JsonObject();
                    //If the request is 6, server will ignore that and start a new listensocket
                    if (obj.get("operation").equals("6")) {
                        break;
                    } else {
                        String idt = "";
                        String id = "";
                        boolean verif = false;
                        //If the operation is 1, we will add id, operation, difficulty and transaction into the string that needs to be signed
                        if(obj.get("operation").equals("1")){
                            //sig is the string before signed
                            String sig = obj.get("id") + "," + obj.get("operation") +","+obj.get("difficulty")+","+obj.get("transaction");
                            String signature = (String)obj.get("signature");
                            BigInteger e = new BigInteger((String)obj.get("e"));
                            BigInteger n = new BigInteger((String)obj.get("n"));
                            //Compute hash again and compare with the hash passed from client
                            String ids = e.toString() + n.toString();
                            idt = convertToHex(getHash(ids));
                            id = (String)obj.get("id");
                            //Use verify method to find whether the message is well signed
                            verif = Verify(sig, signature, e, n);
                        }
                        //If the operation is 4, we will add id, operation, blockId and transaction into the string that needs to be signed
                        else if(obj.get("operation").equals("4")){
                            //sig is the string before signed
                            String sig = obj.get("id") + "," + obj.get("operation") +","+obj.get("BlockId")+","+obj.get("transaction");
                            String signature = (String)obj.get("signature");
                            BigInteger e = new BigInteger((String)obj.get("e"));
                            BigInteger n = new BigInteger((String)obj.get("n"));
                            //Compute hash again and compare with the hash passed from client
                            String ids = e.toString() + n.toString();
                            idt = convertToHex(getHash(ids));
                            id = (String)obj.get("id");
                            //Use verify method to find whether the message is well signed
                            verif = Verify(sig, signature, e, n);
                        }
                        //For other operation, there is no other string passed
                        else {
                            //sig is the string before signed
                            String sig = obj.get("id") + "," + obj.get("operation");
                            String signature = (String)obj.get("signature");
                            BigInteger e = new BigInteger((String)obj.get("e"));
                            BigInteger n = new BigInteger((String)obj.get("n"));
                            //Compute hash again and compare with the hash passed from client
                            String ids = e.toString() + n.toString();
                            idt = convertToHex(getHash(ids));
                            id = (String)obj.get("id");
                            //Use verify method to find whether the message is well signed
                            verif = Verify(sig, signature, e, n);
                        }

                        //If the hash is correct and message is well signed, then operate the blockchain
                        if (idt.equals(id) && verif) {
                            String replies = "";
                            // When user input 0, get the size of the chain, compute the hash per second, get the difficulty and get the nonce
                            if (obj.get("operation").equals("0")) {
                                replies = replies+"Current size of chain: " + bc.getChainSize() + "~";
                                replies = replies+"Current hashes per second by this machine: " + bc.hashesPerSecond() + "~";
                                replies = replies+"Difficulty of most recent block: " + bc.getLatestBlock().getDifficulty() + "~";
                                replies = replies+"Nonce for most recent block: " + bc.getLatestBlock().getNonce() + "~";
                                replies = replies+"Chain hash: " + bc.Hash;
                                //add the replies string into the reply JsonObject
                                reply.put("reply", replies);
                            }
                            // When user input 1, create a new block and add into the blockchain according
                            // to user's requirement. In the meantime, calculate the hash
                            else if(obj.get("operation").equals("1")){
                                int diff = Integer.parseInt(obj.get("difficulty").toString());
                                String transac = (String)obj.get("transaction");
                                Date date2 = new Date();
                                Timestamp t2= new Timestamp(date2.getTime());
                                count++;
                                long t= System.currentTimeMillis();
                                Block b2=new Block(count, t2, transac, diff);
                                bc.addBlock(b2);
                                long s= System.currentTimeMillis();
                                long interval = s-t;
                                replies = replies+"Total execution time to add this block was "+interval+" milliseconds.~";
                                //add the replies string into the reply JsonObject
                                reply.put("reply", replies);
                            }
                            // verify whether the block chain is in the correct status and compute the execute time
                            else if (obj.get("operation").equals("2")){
                                replies = replies+"Verifying entire chain"+ "~";
                                long t= System.currentTimeMillis();
                                //Use isChainValid() to check whether the chain have all the hashes correct
                                boolean valid = bc.isChainValid();
                                long s= System.currentTimeMillis();
                                long interval = s-t;
                                replies = replies+"Chain verification: "+ valid + "~";
                                replies = replies+"Total execution time required to verify the chain was "+ interval +" milliseconds.";
                                //add the replies string into the reply JsonObject
                                reply.put("reply", replies);
                            }
                            // return all the detailed information of blocks in the blockchain
                            else if (obj.get("operation").equals("3")){
                                replies = replies+"{\"ds_chain\": [" + "~";
                                for (int i=0; i<bc.getChainSize(); i++){
                                    replies = replies+"{ \"index\": "+bc.blockchain.get(i).getIndex()+", \"time stamp\": \""+bc.blockchain.get(i).getTimestamp()+"\", \"Tx\": \""+bc.blockchain.get(i).getData()+"\", \"PrevHash\": \""+bc.blockchain.get(i).getPreviousHash()+"\", \"nonce\": "+bc.blockchain.get(i).getNonce()+", \"difficulty\": "+bc.blockchain.get(i).getDifficulty()+"}" + "~";
                                }
                                replies = replies+"]," + "~";
                                replies = replies+"\"chainHash\": \""+bc.Hash+"\"}";
                                //add the replies string into the reply JsonObject
                                reply.put("reply", replies);
                            }
                            //corrupt the blockchain by setting another transaction data to a given block
                            else if (obj.get("operation").equals("4")){
                                int blockId = Integer.parseInt(obj.get("BlockId").toString());
                                String d = (String)obj.get("transaction");
                                bc.blockchain.get(blockId).setData(d);
                                replies = replies+"Block "+bc.blockchain.get(blockId).getIndex()+" now holds "+bc.blockchain.get(blockId).getData()+"~";
                                //add the replies string into the reply JsonObject
                                reply.put("reply", replies);
                            }
                            //repair the blockchain that have the wrong hash and also return the execute time.
                            else if (obj.get("operation").equals("5")){
                                replies = replies+"Repairing the entire chain" + "~";
                                long t= System.currentTimeMillis();
                                //Use .repairChain() to repair the incorrect hash in the chain
                                bc.repairChain();
                                long s= System.currentTimeMillis();
                                long interval = s-t;
                                replies = replies+"Total execution time required to repair the chain was " + interval + " milliseconds.";
                                //add the replies string into the reply JsonObject
                                reply.put("reply", replies);
                            }
                            System.out.println("Echoing: The number " + obj.get("id") + " user executes "+obj.get("operation")+" operation.");
                            // Use id and all the input as the string to be signed
                            String sig = id+","+replies;
                            // Generate signature using Sign method
                            String signature = Sign(sig, d1, n1);
                            // put is, e, n and signature into the JsonObject
                            reply.put("id", id);
                            reply.put("e", e1.toString());
                            reply.put("n", n1.toString());
                            reply.put("signature", signature);
                            // Transform the JsonObject into string, so that it can be passed to client
                            String repl = Jsoner.serialize(reply);
                            out.println(repl);
                            out.flush();
                        }
                        // If the hash is not correct or message is not well signed, then return the error message
                        else{
                            String rep = "Echoing: Error in Request.";
                            System.out.println(rep);
                            String repl = Jsoner.serialize(rep);
                            out.println(repl);
                            out.flush();
                        }
                    }
                }
            } catch (IOException e) {
                clientSocket.close();
            }
        }
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
        return r.substring(r.length()-20);
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
}



