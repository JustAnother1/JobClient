package de.nomagic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CommunicationTask extends Thread
{
    private String myId = "notSet";
    private int myPort = 2323;
    private boolean shouldRun = true;

    public CommunicationTask()
    {
        super.setName("Communication");
    }

    public void setWorkerId(String workerId)
    {
        myId = workerId;
    }

    public void setPort(int TcpPort)
    {
        myPort = TcpPort;
    }

    @Override
    public void run()
    {
        // Startup
        ServerSocket TcpListenSocket = null;
        try
        {
            TcpListenSocket = new ServerSocket(myPort);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        System.out.println("Started Worker on port " + myPort);
        while((false == isInterrupted()) && (true == shouldRun))
        {
            try
            {
                final Socket connectionSocket = TcpListenSocket.accept();
                ConnectionTask cn = new ConnectionTask(connectionSocket, myId);
                cn.start();
            }
            catch(IOException e)
            {
                if(true == shouldRun)
                {
                    e.printStackTrace();
                }
            }
        }
        try
        {
            TcpListenSocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // Shutdown
        System.out.println("closing communication channel!");
    }

}
