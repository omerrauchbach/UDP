import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {



    public static void main(String[] args){



            Thread t = new Thread(new ServersSide());
            t.start();
            ClientSide.main(null);

    }


}
