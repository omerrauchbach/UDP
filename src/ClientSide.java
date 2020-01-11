import sun.awt.Mutex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientSide {


    //public static DatagramSocket socket;
    public List<InetAddress> addrList = new LinkedList ();
    String teamName = "omerR-Team                      " ;

    String hash;
    String length;
    DatagramSocket socket;
    boolean ackType ;

    public ClientSide( String hash , String length){

        this.hash = hash;
        this.length = length;
        this.ackType = false;

    }

    public  void runClient() {

        try {

            socket = new DatagramSocket();
            socket.setBroadcast(true);
            String broadcastMessage = teamName+"1";
            DatagramPacket packet
                    = new DatagramPacket(broadcastMessage.getBytes(StandardCharsets.UTF_8), broadcastMessage.length(), InetAddress.getByName("255.255.255.255"), 3117);
            System.out.println("Sent Broadcast Message");
            socket.send(packet);
            long startTime = System.currentTimeMillis();
            while (true) {
                long currentTime = System.currentTimeMillis();
                int timePass = (int)Math.abs(startTime-currentTime);
                if(timePass > 2000){
                    break;
                }else {
                    socket.setSoTimeout(2000-timePass);
                }
                byte[] buf = new byte[1104];
                DatagramPacket receivedPacket = new DatagramPacket(buf, 1104, InetAddress.getByName("255.255.255.255"), 3117);
                try {
                    socket.receive(receivedPacket);
                }
                catch (IOException e){
                    System.out.println("Time out Broadcast ");
                    break;
                }
                System.out.println("Received offer Message");
                String received = new String(
                        receivedPacket.getData(), 0, receivedPacket.getLength());
                if (isOffer(received)) {
                    addrList.add(receivedPacket.getAddress());
                }
            }
            if(addrList.isEmpty()) {
                System.out.println("Non servers were found");
                socket.close();
                return;
            }


            String[] afterDivade = divideToDomains(Integer.parseInt(length),addrList.size());
            int indexAfterDivade = 0;
            /////Send Request Message!!
            for(int i =0 ; i< addrList.size() ; i++){
                String start = afterDivade[indexAfterDivade];
                indexAfterDivade++;
                String end = afterDivade[indexAfterDivade];
                indexAfterDivade++;
                byte[] buf = getMessage(teamName,"3",hash,length,start,end);
                InetAddress ip = addrList.remove(0);
                DatagramPacket packetRequest = new DatagramPacket(buf, buf.length, ip, 3117);
                System.out.println("Sent Request Message");
                socket.send(packetRequest);

            }
            socket.setSoTimeout(20000);
            while(!ackType){
                try {
                byte[] buf = new byte[1104];
                DatagramPacket receivedPacket = new DatagramPacket(buf, 1104, InetAddress.getByName("255.255.255.255"), 3117);
                socket.receive(receivedPacket);
                //System.out.println("Received offer Message");
                String received = new String(
                        receivedPacket.getData(), 0, receivedPacket.getLength());
                if (isAck(received)) {
                    System.out.println("The input string is:" + received.substring(74));
                    ackType=true;
                    return;
                }

            }
            catch (IOException e){
                break;
            }


        }


            if(!ackType)
                System.out.println("Servers didn't sent Arc ");
            socket.close();

        } catch (UnknownHostException e){
            e.printStackTrace();

        } catch (IOException e){
            e.printStackTrace();
        }


    }

    private boolean isAck(String received){

        if(received.substring(32,33).equals("4"))
            return true;
        else
            return false;
    }

    private boolean isOffer(String received){

        if(received.substring(32,33).equals("2"))
            return true;
        else
            return false;
    }

    private byte[] getMessage(String teamName ,String type ,String hash , String length , String start ,String end ){

       String ans = teamName+type+hash+length+start+end;

        return ans.getBytes(StandardCharsets.UTF_8);


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

    private String [] divideToDomains (int stringLength, int numOfServers){
        String [] domains = new String[numOfServers * 2];

        StringBuilder first = new StringBuilder(); //aaa
        StringBuilder last = new StringBuilder(); //zzz

        for(int i = 0; i < stringLength; i++){
            first.append("a"); //aaa
            last.append("z"); //zzz
        }

        BigInteger total =  convertStringToInt(last.toString());
        BigInteger perServer = total.divide(BigInteger.valueOf(numOfServers));

        domains[0] = first.toString(); //aaa
        domains[domains.length -1 ] = last.toString(); //zzz
        BigInteger summer = new BigInteger("0");

        for(int i = 1; i <= domains.length -2; i += 2){
            summer =  summer.add(perServer);
            domains[i] = converxtIntToString(summer, stringLength); //end domain of server
            summer = summer.add(BigInteger.valueOf(1));//++;
            domains[i + 1] = converxtIntToString(summer, stringLength); //start domain of next server
        }

        return domains;
    }

    public static void main(String[] args){

        boolean play = true;
        Scanner myHash = new Scanner(System.in);
        String hash = "";
        String length= "";
        while(play) {


            System.out.println("Welcome to omerR-Team. Please enter the hash:");
            hash = myHash.nextLine();
            if (hash.length() != 40) {
                System.out.println("Invalid argument");
                continue;
            }
            play =false;
        }
        play = true;
        while (play) {
            myHash = new Scanner(System.in);
            System.out.println("Please enter the input string length:");
            length = myHash.nextLine();
            try {
                Integer.parseInt(length);
            } catch (Exception e) {
                System.out.println("Invalid argument");
                continue;
            }
            if (length.length() > 255 || length.length() == 0 || Integer.parseInt(length) <= 0) {
                System.out.println("Invalid argument");
                continue;
            }
            play =false;

        }

        ClientSide clientSide = new ClientSide(hash ,length);
        clientSide.runClient();


    }

}
