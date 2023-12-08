import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean working;

    public Client() {

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {
        try {
            client = new Socket("localhost", 1234);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            Handler handler = new Handler();
            new Thread(handler).start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            working = true;
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            //
        }
    }

    class Handler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inRead = new BufferedReader(new InputStreamReader(System.in));
                while (!working) {
                    String message = inRead.readLine();
                    if (message.startsWith("/exit")) {
                        out.println("/exit");
                        inRead.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }
}
