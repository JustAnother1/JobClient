package de.nomagic;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerTask extends Thread
{
    enum state {
        IDLE,
        WORKING,
        SHUTDOWN,
        ERROR,
    }

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final String workerId;
    private state curState = state.IDLE;
    private boolean shouldRun = true;
    private CommunicationTask com = null;

    public WorkerTask(String Name)
    {
        this.workerId = Name;
    }

    public void setComTask(CommunicationTask com)
    {
        this.com = com;
    }

    public String getStatus()
    {
        switch(curState)
        {
        case IDLE:
            return "idle";
        case WORKING:
            return "working";
        case SHUTDOWN:
            return "shutdown";
        default:
            return "unknown!";
        }
    }

    public boolean wantsToWork()
    {
        switch(curState)
        {
        case IDLE:
        case WORKING:
            return true;

        case SHUTDOWN:
        case ERROR:
        default:
            return false;
        }
    }

    public void kill()
    {
        // stop working and shut down client
        curState = state.SHUTDOWN;
        shouldRun = false;
        this.interrupt();
    }

    @Override
    public void run()
    {
        shouldRun = prepareWorkArea();
        while(true == shouldRun)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // is OK
            }
        }
    }

    private boolean prepareWorkArea()
    {
        File workFolder = new File(workerId);
        if(true == workFolder.exists())
        {
            if( false == workFolder.delete())
            {
                log.error("Failed to remove old working directory!");
                curState = state.ERROR;
                return false;
            }
        }
        if(false == workFolder.mkdir())
        {
            log.error("failed to create work directory");
            curState = state.ERROR;
            return false;
        }
        // now everything is ready to go.
        return true;
    }

    public String[] getNamesOfFinishedJobs()
    {
        return new String[0];
    }

    public String[] getNamesOfQueuedJobs()
    {
        return new String[0];
    }

    public byte[] get(String JobName)
    {
        return new byte[0];
    }


}
