import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Levi Schanding
 * @version 1.0
 */
public class Dispatcher {
    public static ServerSocket port;
    public static final int PORT_NUMBER = 7788;

    public static void main(String[] args){
        /*-[DECLARATIONS]-*/
        ServerThread serverThread;
        Socket socket;
        /*-[BODY]-*/
        try {
            port = new ServerSocket(PORT_NUMBER);

            while(true){
                socket = port.accept();
                serverThread = new ServerThread(socket);
                serverThread.start();
            }

        } catch (IOException e){
            System.out.printf("[PORT FAILED TO BIND] --> %s ", e.toString());
        }
    }
}
