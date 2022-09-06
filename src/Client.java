import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Levi Schanding
 * @version 1.0
 */
public class Client {
    private static final String HOSTNAME = "localhost";
    private static final int PORT_NUMBER = 7788;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(HOSTNAME, PORT_NUMBER);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String server_msg, client_msg;
            boolean end_game  = false;
            while((server_msg = in.readLine()) != null){

                switch (server_msg){
                    case "INPUT":{ //Server sends back an INPUT message which tells the client that it is expecting them to make a move
                        client_msg = stdin.readLine();
                        if (client_msg != null){
                            out.println(client_msg);
                        }
                        break;
                    }
                    case "GAME_END":{ //Server sends back an END_GAME  message which tells the client that the game is over so that it may quit
                        end_game = true;
                        break;
                    }
                    default:{
                        /* Display the consequences of the last move. */
                        if (server_msg.contains("MOVE")){
                            String[] move_results = server_msg.split("\\s+");
                            if (move_results.length == 3){
                                System.out.printf("The CPU marked position %s %s\n", move_results[1], move_results[2]);
                            } else {
                                if(move_results[3].equals("TIE")){
                                    System.out.printf("Its a tie! %s move %s %s made it so!\n",
                                            (move_results[1].equals("-1")) ? "Your" : "The CPU's",
                                            move_results[1], move_results[2]
                                    );
                                }else {
                                    System.out.printf("%s! %s won with the move %s %s.\n",
                                            (move_results[3].equals("WIN")) ? "Victory" : "Defeat",
                                            (move_results[3].equals("WIN")) ? "You have" : "The CPU has",
                                            move_results[1], move_results[2]
                                    );
                                }
                            }
                        }
                        else{
                            System.out.println(server_msg);
                        }
                    }
                }
                if (end_game) break; //Break out of the loop and let the client process terminate.
            }
            socket.close();
            System.out.println("Thanks for playing!");
        } catch (IOException e) {}

    }


}
