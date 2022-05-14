package de.nomagic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionTask extends Thread
{
    public static final int VERSION = 1;

    private final Socket connectionSocket;
    private final String myName;
    private final BufferedReader fromServer;
    private final DataOutputStream toServer;
    private boolean shouldRun = true;

    public ConnectionTask(Socket connectionSocket, String myName) throws IOException
    {
        this.connectionSocket = connectionSocket;
        this.myName = myName;
        fromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        toServer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    @Override
    public void run()
    {
        System.out.println("Openeing connection!");
        try
        {
            toServer.writeBytes("Hi, I'm " + myName + ". How can I help you?\r\n");
            String cmd = fromServer.readLine();
            while((null != cmd) && (true == shouldRun))
            {
                String response = parse(cmd);
                toServer.writeBytes(response);
                toServer.writeBytes("\r\n");
                toServer.flush();
                if(true == shouldRun)
                {
                    cmd = fromServer.readLine();
                }
            }
            connectionSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("Closed connection");
    }

    private String parse(String cmd)
    {
        if(null == cmd)
        {
            return "";
        }
        if(1 > cmd.length())
        {
            return "";
        }
        if(cmd.startsWith("status"))
        {
            return "idle";
        }
        if(cmd.startsWith("version"))
        {
            return "" + VERSION;
        }
        if(cmd.startsWith("exit"))
        {
            shouldRun = false;
            return "Bye.";
        }
        return "invalid command : " + cmd + " !";
    }

}
