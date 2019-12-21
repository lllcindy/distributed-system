import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

/*
 *
 * @author Xindi Lan
 * @date 15/10/2019
 *
 * The Block class holds the block object. It has index,
 * timestamp, data, difficulty, nonce and previousHsh.
 * It can be added to blockchain.
 *
 */

public class Block {
    //the position of the block on the chain
    private int index;
    //the time of the block's creation
    private Timestamp timestamp;
    //a String holding the block's single transaction details
    private  String data;
    //an int that specifies the exact number of left most hex digits needed
    private  int difficulty;
    //a BigInteger value determined by a proof of work routine
    private BigInteger nonce = new BigInteger("0");
    //the SHA256 hash of a block's parent
    private  String previousHash;

    public Block(int index, Timestamp timestamp, String data, int difficulty){
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
    }

    /**
     * This method is to calculate hash for the block object
     *
     * @return the calculated hash value
     */
    public String calculateHash(){
        String text = String.valueOf(index)+ String.valueOf(timestamp)+data+previousHash+String.valueOf(nonce)+String.valueOf(difficulty);
        return getHash(text);
    }
    public String getData(){
        return data;
    }
    public int getDifficulty(){
        return difficulty;
    }
    public int getIndex(){
        return index;
    }
    public BigInteger getNonce(){
        return nonce;
    }
    public String getPreviousHash(){
        return previousHash;
    }
    public Timestamp getTimestamp(){
        return timestamp;
    }

    /**
     * This method is to find the correct nonce and the hash value.
     * That is to find whether the current nonce can let hash has
     * correct number of zero at the beginning
     *
     * @return the correct hash value
     */
    public String proofOfWork(){
        int count=0;
        String Hash="";
        String text="";
        do{
            count=0;
            // every time calculate the hash, nonce will be added by 1 for the next calculation
            nonce=nonce.add(new BigInteger("1"));
            text = String.valueOf(index)+ String.valueOf(timestamp)+data+previousHash+String.valueOf(nonce)+String.valueOf(difficulty);
            Hash = getHash(text);
            // After calculate the hash, check whether the hash has the correct number
            // of zero at the beginning. If the number equals to difficulty, then return
            // the hash. If not, add nonce by 1 and calculate again.
            for (int i=0; i<Hash.length();i++){
                if (Hash.charAt(i)=='0'){
                    count++;
                }
                else{
                    break;
                }
            }
        }while(count!=difficulty);
        return Hash;
    }
    public void setData(String data){
        this.data = data;
    }
    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }
    public void setIndex(int index){
        this.index = index;
    }
    public void setPreviousHash(String previousHash){
        this.previousHash = previousHash;
    }
    public void setTimestamp(Timestamp timestamp){
        this.timestamp = timestamp;
    }

    /**
     * This method can convert the plain text into hash
     * value and return hex string of the hash
     *
     * @param text the String need to be converted into hash
     * @return the hex string of the hash
     */
    private static String getHash(String text){
        try {
            // Get the corresponding instance of specific hash type
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(text.getBytes());
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                int halfbyte = (b[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        buf.append((char) ('0' + halfbyte));
                    else
                        buf.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = b[i] & 0x0F;
                } while(two_halfs++ < 1);
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown"
                    + " for incorrect algorithm: " + e);
            return null;
        }
    }

}

