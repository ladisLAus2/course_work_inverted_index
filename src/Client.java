import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private Thread handleThread;
    private final int port = 1234;

    public Client() throws IOException {
        client = new Socket("localhost", port);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
    }

    public static void main(String[] args) {
        Client client = null;
        try {
            client = new Client();
            client.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }

    }

    @Override
    public void run() {
        try {
            Handler handler = new Handler();
            handleThread = new Thread(handler);
            handleThread.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                if (inMessage.startsWith("/shutdown")) {
                    System.out.println("Сервер несподівано завершив роботу. З'єднання розірване. Введіть, будь-що для припинення роботи клієнта. ");
                    shutdown();
                    break;
                } else {
                    System.out.println(inMessage);
                }

            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
            if(handleThread != null){
                handleThread.interrupt();
            }
        } catch (IOException e) {
            //
        }
    }

    class Handler implements Runnable {
        @Override
        public void run() {
            try(BufferedReader inRead = new BufferedReader(new InputStreamReader(System.in))) {

                String message;
                while ((message = inRead.readLine()) != null) {
                    if(Thread.interrupted()){
                        break;
                    }
                    if (message.startsWith("/exit")) {
                        out.println("/exit");
                        shutdown();
                        inRead.close();
                        break;
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
