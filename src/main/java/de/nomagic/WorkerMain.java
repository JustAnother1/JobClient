package de.nomagic;

import java.io.File;
import java.io.IOException;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class WorkerMain
{
    private boolean somethingWrong = false;

    private String workerId = null;
    private int comTcpPort = 54321;

    public WorkerMain()
    {
    }

    private void configure(String[] args)
    {
        // read worker.cfg
        Wini ini;
        try {
            ini = new Wini(new File("worker.cfg"));
            workerId = ini.get("worker", "name");
            comTcpPort = ini.get("worker", "port", int.class);
        }
        catch (InvalidFileFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean canWork()
    {
        if (true == somethingWrong)
        {
            System.err.println("something went wrong!");
            return false;
        }
        if(null == workerId)
        {
            System.err.println("No worker ID given!");
            return false;
        }
        // TODO additional checks go here
        return true;
    }

    private void work()
    {
        CommunicationTask com = new CommunicationTask();
        com.setWorkerId(workerId);
        com.setPort(comTcpPort);
        com.start();
        do {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // is OK
            }
        } while(com.isAlive());
    }

    public static void main(String[] args)
    {
        WorkerMain worker = new WorkerMain();
        worker.configure(args);
        if(false == worker.canWork())
        {
            System.out.println("I can not start working like this !");
            System.exit(23);
        }
        worker.work();
        if(false == worker.canWork())
        {
            System.out.println("I can not work like this !");
            System.exit(23);
        }
        else
        {
            System.out.println("Finished working.");
            System.exit(0);
        }
    }

}
