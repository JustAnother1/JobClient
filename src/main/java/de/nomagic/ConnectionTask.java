package de.nomagic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionTask extends Thread
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    public static final int VERSION = 1;
    public static final String LINE_END = "\r\n";
    public static final String PROMPT = "At your service\r\n";

    private final Socket connectionSocket;
    private final String myName;
    private final WorkerTask worker;
    private final BufferedReader fromServer;
    private final DataOutputStream toServer;
    private boolean shouldRun = true;

    public ConnectionTask(Socket connectionSocket, String myName, WorkerTask worker) throws IOException
    {
        this.connectionSocket = connectionSocket;
        this.myName = myName;
        this.worker = worker;
        fromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        toServer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    @Override
    public void run()
    {
        log.info("Openeing connection!");
        try
        {
            toServer.writeBytes("Hi, I'm " + myName + ". How can I help you?" + LINE_END);
            toServer.writeBytes(PROMPT);
            String cmd = fromServer.readLine();
            while((null != cmd) && (true == shouldRun))
            {
                String response = parse(cmd);
                toServer.writeBytes(response);
                toServer.writeBytes(LINE_END);
                toServer.writeBytes(PROMPT);
                toServer.flush();
                if(true == shouldRun)
                {
                    cmd = fromServer.readLine();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            connectionSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        log.info("Closed connection");
    }

    private String cmdAdd(String[] cmd_parts)
    {
        if(cmd_parts[0].length() > 1)
        {
            byte[] data = receiveFile();
            if(data.length > 0)
            {
                return "OK: received the job " + cmd_parts[1];
            }
            else
            {
                return "ERROR: Failed to receive the job " + cmd_parts[1];
            }
        }
        else
        {
            return "ERROR: task name missing !";
        }
    }

    private String cmdExit()
    {
        shouldRun = false;
        return "Bye.";
    }

    private String cmdGet(String[] cmd_parts) throws IOException
    {
        if(cmd_parts[0].length() > 1)
        {
            byte[] data = worker.get(cmd_parts[1]);
            sendFile(data);
            return "";
        }
        else
        {
            return "ERROR: task name missing !";
        }
    }

    private String cmdHelp()
    {
        return "available commands:" + LINE_END
                + "add     - add a new task to the work queue" + LINE_END
                + "exit    - close this connection" + LINE_END
                + "get     - get result of task" + LINE_END
                + "help    - show this list of available commands" + LINE_END
                + "kill    - kills this worker" + LINE_END
                + "list    - list all queued or finished tasks" + LINE_END
                + "status  - show current state of worker thread" + LINE_END
                + "version - show currently running client version" + LINE_END;
    }

    private String cmdKill()
    {
        shouldRun = false;
        worker.kill();
        return "I'm dying!!!!";
    }

    private String cmdList()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Queued tasks:");
        String[] queued = worker.getNamesOfQueuedJobs();
        for(int i = 0; i < queued.length; i++)
        {
            sb.append(LINE_END);
            sb.append(queued[i]);
        }
        sb.append(LINE_END);
        sb.append("finished tasks:");
        String[] finished = worker.getNamesOfFinishedJobs();
        for(int i = 0; i < finished.length; i++)
        {
            sb.append(LINE_END);
            sb.append(finished[i]);
        }
        return sb.toString();
    }

    private String parse(String cmd) throws IOException
    {
        if(null == cmd)
        {
            return "";
        }
        if(1 > cmd.length())
        {
            return "";
        }
        String[] cmd_parts = cmd.split("\\s+");

        switch(cmd_parts[0])
        {
        case "add":     return cmdAdd(cmd_parts);
        case "exit":    return cmdExit();
        case "get":     return cmdGet(cmd_parts);
        case "help":    return cmdHelp();
        case "kill":    return cmdKill();
        case "list":    return cmdList();
        case "status":  return worker.getStatus();
        case "version": return "" + VERSION;
        default:        return "invalid command : " + cmd + " !";
        }
    }

    private byte[] receiveFile()
    {
        // TODO
        return new byte[0];
    }

    private void sendFile(byte[] data) throws IOException
    {
        toServer.writeBytes("fileContentLength=" + data.length + ":" + LINE_END);
        toServer.write(data, 0, data.length);
    }

}
