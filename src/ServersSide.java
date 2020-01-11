import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServersSide implements Runnable {




    private volatile boolean running;

    public void run(){

        System.out.println("Server is working");
        running = true;
        try {
            DatagramSocket socket = new DatagramSocket(3117);
            while (running) {
                byte[] buf = new byte[1104];
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                new Thread(() -> {
                    byte[] data =unpackMessage(packet.getData());
                    if(data!= null) {
                        DatagramPacket packetSend = new DatagramPacket(data, data.length,packet.getAddress(),packet.getPort());
                        try {
                            socket.send(packetSend);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }

                    }

                }).start();

            }
            System.out.println("Server is close");
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private byte[] unpackMessage(byte[] data){


        String teamName = copyfromto(data ,0 , 32);
        String type = copyfromto(data ,32 , 33);
        String ans;

        ////discover
        if(type.equals("1")){
            ans= teamName+"2";
            return ans.getBytes(StandardCharsets.UTF_8);

        }
        /// request
        else if(type.equals("3")){
            String hash = copyfromto(data ,33 , 73);
            String length =  copyfromto(data ,73 , 74);

            try {
                Integer.parseInt(length);
            }
            catch (Exception e){
                return null;
            }
            int lengthNum = Integer.parseInt(length);
            String start = copyfromto(data ,74 , 74+lengthNum);
            String end = copyfromto(data ,74+lengthNum , 74+(2*lengthNum));
            String afterHash = tryDeHash(start, end, hash);
            if(afterHash !=null){
                ans = teamName+"4"+hash+length+afterHash;
                return ans.getBytes(StandardCharsets.UTF_8);
            }else{
                ans = teamName+"5";
                return ans.getBytes(StandardCharsets.UTF_8);
            }
        }else{

            return null;

        }

    }

    private String copyfromto(byte[] data ,int from , int to){

        byte[] subData = new byte[to-from];
        for(int i=0;from<to;from++,i++){
            subData[i] = data[from];
        }
        return new String(subData);
    }
    
    public String hash(String toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(toHash.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32){
                hashText.insert(0, "0");
            }
            return hashText.toString();
        }
        catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    private String tryDeHash(String startRange, String endRange, String originalHash){
        BigInteger start = convertStringToInt(startRange);
        BigInteger end = convertStringToInt(endRange);
        int length = startRange.length();
        for(BigInteger i = start; i.compareTo(end) <= 0; i=i.add(new BigInteger("1"))){
            String currentString = converxtIntToString(i, length);
            String hash = hash(currentString);
            if(originalHash.equals(hash)){
                return currentString;
            }
        }
        return null;
    }

    private BigInteger convertStringToInt(String toConvert) {
        char[] charArray = toConvert.toCharArray();
        BigInteger num = new BigInteger("0") ;
        for(char c : charArray){
            if(c < 'a' || c > 'z'){
                throw new RuntimeException();
            }
            num = num.multiply(new BigInteger("26"));
            int x =c-'a';
            num = num.add(new BigInteger(Integer.toString(x)));
        }
        return num;
    }

    private String converxtIntToString(BigInteger toConvert, int length) {
        StringBuilder s = new StringBuilder(length);
        while (toConvert.compareTo(new BigInteger("0"))>0 ){
            BigInteger c = toConvert.mod(new BigInteger("26"));
            s.insert(0, (char) (c.intValue() + 'a'));
            toConvert = toConvert.divide(new BigInteger(("26")));
            length --;
        }
        while (length > 0){
            s.insert(0, 'a');
            length--;
        }
        return s.toString();
    }

    public static void main(String[] args){

        ServersSide server = new ServersSide();
        server.run();

    }



}
