package de.nomagic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationTask extends Thread
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private String myId = "notSet";
    private int myPort = 2323;
    private WorkerTask worker;
    private boolean shouldRun = true;

    public CommunicationTask()
    {
        super.setName("Communication");
    }

    public void close()
    {
        shouldRun = false;
        this.interrupt();
    }

    public void setWorkerId(String workerId)
    {
        myId = workerId;
    }

    public void setPort(int TcpPort)
    {
        myPort = TcpPort;
    }

    public void setWorker(WorkerTask worker)
    {
        this.worker = worker;
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
        log.info("Started Worker on port {}", myPort);
        while((false == isInterrupted()) && (true == shouldRun))
        {
            try
            {
                final Socket connectionSocket = TcpListenSocket.accept();  // this blocks until a connection becomes available
                ConnectionTask cn = new ConnectionTask(connectionSocket, myId, worker);
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
        log.info("closing communication channel!");
    }

}
