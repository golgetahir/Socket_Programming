import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ServiceConfigurationError;
import java.util.concurrent.TimeUnit;


public class SecureClient {
    public Socket soc;
    public String port;
    public String ip;
    public InputStream in;
    public DataOutputStream dOut;
    SecureClient(String ip, String port){
        this.ip = ip;
        this.port = port;
        int iport = Integer.parseInt(port);
        if(!connect(ip, iport)) {
            System.out.println("Cannot connect to the server. IP: " + ip + " PORT: " + port);
        }
        else System.out.println("Connected to " + ip + ":" + port);
    }
    public boolean connect(String ip, int port) {
        try {
            soc = new Socket(ip, port);

            in = new DataInputStream(soc.getInputStream());
            dOut = new DataOutputStream(soc.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void sendHello(){
        try {
            byte[] len = new byte[]{0000};
            String txt = "HELLOxxx";
            byte[] b = txt.getBytes();

            //Send HELLOxxx
            dOut.write(b);
            dOut.flush();

            //Send LENGTH
            dOut.write(len);
            dOut.flush();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public byte[] receiveHello(){
        try {
            byte[] resp = new byte[8];
            in.read(resp);
            String ressp = new String(resp);
            System.out.println("response " + ressp);

            //Get length of data
            byte[] resp_len = new byte[4];
            in.read(resp_len);
            int len_data = ByteBuffer.wrap(resp_len).getInt();
            System.out.println("response len" + len_data);
            byte[] cert = new byte[len_data];
            in.read(cert);
            String res_cer = new String(cert);
            System.out.println("response " + res_cer);
            return cert;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public byte[] getSignature(byte[] cert){
        byte[] signat = Arrays.copyOfRange(cert,cert.length - 8, cert.length);
        return signat;
    }
    public String getCa(byte[] cert){
        String certificate = new String(cert);
        String[] split = certificate.split("=");
        return split[3].substring(0,10);
    }
    public byte[] getPK(byte[] cert){
        byte[] part1 = "NAME=www.pa2.comPK=".getBytes();
        byte[] pkey = Arrays.copyOfRange(cert,part1.length, part1.length + 8);
        return pkey;
    }
    public void sendSecret(byte[] enc){
        try {
            String txt = "SECRETxx";
            byte[] b = txt.getBytes();

            dOut.write(b);
            dOut.flush();
            byte[] len = new byte[]{0000};
            dOut.write(len);
            dOut.flush();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendSTARTENC(){
        try{
            String ak = "STARTENC";
            byte[] d = ak.getBytes();
            System.out.println("burda " + d);
            System.out.println("burda " + new String(d));
            dOut.write(d);
            dOut.flush();
            byte[] len = new byte[]{0000};
            dOut.write(len);
            dOut.flush();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendAUTH(byte[] auth){
        try{
            String ak = "AUTHxxxx";
            byte[] d = ak.getBytes();
            dOut.write(d);
            dOut.flush();
            byte[] len = new byte[]{0000};

            dOut.write(auth);
            dOut.flush();

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
        try {
            //Initializations
            CryptoHelper crypto = new CryptoHelper();
            byte[] ServerPublicKey;
            byte[] signature;
            byte[] certificate;
            SecureClient client;

            //HANDSHAKE STARTED
            int i = 0;
            while(true) {
                //Client initialization
                TimeUnit.SECONDS.sleep(1);
                client = new SecureClient("localhost", "60000");
                i++;
                System.out.println(i + "th trial");
                System.out.println("Client started");

                //Send Hello to initiate handshake
                client.sendHello();

                //Get Response
                certificate = client.receiveHello();
                signature = client.getSignature(certificate);
                String ca = client.getCa(certificate);
                ServerPublicKey = client.getPK(certificate);

                System.out.println("CA = " + ca);
                System.out.println("SIGNATURE = " + new String(signature));
                System.out.println("SERVER PUBLIC KEY = " + new String(ServerPublicKey));

                //Check certificate, if wrong certificate try it again for security
                if (crypto.verifySignature(certificate, signature, ca, ServerPublicKey) == true) {
                    System.out.println("Verification successfull");
                    break;
                } else {
                    System.out.println("Verification unsuccessfull");
                    client.soc.close();

                }
            }
            //Create and send encrypted secret
            int secret = crypto.generateSecret();
            byte[] secretEncrypted = crypto.encryptSecretAsymmetric(secret, ServerPublicKey);
            System.out.println(secretEncrypted);
            client.sendSecret(secretEncrypted);

            //HANDSHAKE ENDED
            //AUTHENTICATION START
            client.sendSTARTENC();
            byte[] authEncrypted = crypto.encryptSymmetric("bilkent cs421", secret);
            client.sendAUTH(authEncrypted);
            Boolean flag = true;
            //while (flag)
            //{

                //byte[] cert = new byte[100];
                //System.out.println(cert);



            //}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
