import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * @author Levi Schanding
 * @version 1.0
 */
public class ServerThread extends Thread {
    private static final String INPUT = "INPUT";
    private static final String GAME_END = "GAME_END";
    private static int threadCount = 0;
    private int threadID;
    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private BufferedReader in;
    private PrintWriter out;
    private Random gen;
    private char[][] board;
    private int row, col;

    public ServerThread(Socket socket) throws IOException {
        this.threadID = threadCount++;
        this.socket = socket;
        this.gen = new Random();
        this.inStream = new DataInputStream(socket.getInputStream());
        this.outStream = new DataOutputStream(socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(this.inStream));
        this.out = new PrintWriter(outStream, true);
        this.board = new char[3][3];
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board.length; j++) {
                this.board[i][j] = ' ';
            }
        }
        this.row = -1;
        this.col = -1;
    }


    @Override
    public void run() {
        System.out.printf("|-[ServerThread-%d][Created]\n", this.threadID);
        int counter = 0;
        String response = "";
        boolean gameover = false;
        boolean turn = gen.nextInt() % 2 == 0; //generate a random number and check if it is even or odd. Even, client goes first. Odd, server goes first.
        System.out.printf("|-[ServerThread-%d][%s going first]\n", this.threadID, (turn) ? "Client" : "Server");
        out.printf("%s goes first\n", (turn) ? "Player" : "CPU");
        while (!gameover) {
            if (turn) {
                System.out.printf("|-[ServerThread-%d][Player's turn]\n", this.threadID);
                out.println("Enter your move (row then column): "); //Send prompt text to client
                out.println(INPUT); //send an input token to the client so that it knows that the server is expecting a message
                try {
                    response = in.readLine();
                } catch (IOException e) {
                    System.out.printf("|-[ServerThread-%d][%s]\n", this.threadID, e.toString());
                }
                String[] clientInput = response.split("\\s+");
                row = Integer.parseInt(clientInput[0]);
                col = Integer.parseInt(clientInput[1]);
                if ((row < 0 || row > 2 ) || (col < 0 | col > 2)){
                    System.out.printf("|-[ServerThread-%d][Client attempted illegal move]\n", this.threadID);
                    out.printf("[%d][%d] is an invalid placement!\n", row, col);
                    continue;
                }
                if (board[row][col] != ' '){
                    System.out.printf("|-[ServerThread-%d][Client attempted illegal move]\n", this.threadID);
                    out.printf("Position %d %d is already occupied!\n", row, col);
                    continue;
                }
                board[row][col] = 'O';
                printBoard(counter++, "PLAYR");
                if(checkWin() || counter == 9){
                    gameover = true;
                    if(checkWin()){
                        out.printf("MOVE -1 -1 WIN\n");
                    }
                    else{
                        out.printf("MOVE -1 -1 TIE\n");
                    }
                }
            }else{
                System.out.printf("|-[ServerThread-%d][Server's turn]\n", this.threadID);
                out.println("CPU is thinking...");
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e){}

                makeMove();
                printBoard(counter++, " CPU ");
                if(checkWin() || counter == 9){
                    gameover = true;
                    if(checkWin()){
                        out.printf("MOVE %d %d LOSS\n", row, col);
                    }
                    else{
                        out.printf("MOVE %d %d TIE\n", row, col);
                    }
                }else{
                    System.out.printf("|-[ServerThread-%d][Sending Client MOVE message]\n", this.threadID);
                    out.printf("MOVE %d %d\n", row, col);
                }
            }
            turn = !turn;
        }
        out.println(GAME_END);

        try {
            socket.close();
            System.out.printf("|-[ServerThread-%d][Socket Closed]\n", this.threadID);
        }catch (IOException e){
            System.out.printf("|-[%s]\n", e.toString());
        }

        System.out.printf("|-[ServerThread-%d][Terminated Successfully]\n", this.threadID);
    }

    private void makeMove() {
        do{
            row = gen.nextInt(3);
            col = gen.nextInt(3);
            if(board[row][col] == ' '){
                board[row][col] = 'X';
                return;
            }
        } while(true);
    }
    //Added parameters turnNum and actor for purposes of display.
    private void printBoard(int turnNum, String actor) {
        out.printf("*++{%s}++*\n* %c | %c | %c *\n|---|---|---|\n* %c | %c | %c *\n|---|---|---|\n* %c | %c | %c *\n*--{Turn%d}--*\n",
                actor,
                board[0][0], board[0][1], board[0][2],
                board[1][0], board[1][1], board[1][2],
                board[2][0], board[2][1], board[2][2],
                turnNum);
    }

    private boolean checkWin() {
        /*Check for vertical match*/
        for (int i = 0; i < 3; i++)
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return true;
            }
        /*Check for horizontal match*/
        for (int i = 0; i < 3; i++)
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true;
            }
        /*Check for diagonal match*/
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return true;
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return true;
        }
        return false;
    }


}


