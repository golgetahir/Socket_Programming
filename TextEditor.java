import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TextEditor{
    public Socket soc;
    public String port;
    public String ip;

    TextEditor(String ip, String port){
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static void main(String[] args) {
        try {
            TextEditor client = new TextEditor(args[0] , args[1]);
            System.out.println("Client started");

            BufferedReader sysRead = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader response  = new BufferedReader(new InputStreamReader(client.soc.getInputStream()));
            PrintWriter message = new PrintWriter(client.soc.getOutputStream());

            Boolean flag = true;
            while (flag)
            {
                System.out.println("Type 'exit' for closing the server, type commands");
                String txt = sysRead.readLine();
                message.println(txt);
                message.flush();
                if (txt == "exit")
                {
                    client.soc.close();
                    sysRead.close();
                    response.close();
                    message.close();
                    flag = false;
                } else
                {
                    String resp = response.readLine();
                    System.out.println(resp);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}


