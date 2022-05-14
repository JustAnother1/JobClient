package de.nomagic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class GetJob
{

    // configuration:
    private String ServerURL = "127.0.0.1";
    private String ClientId = null;
    private int ServerPort = 4321;
    private int protocolVersion = 2;
    private String additionalParameters = "";

    private boolean isConnected = false;
    private Socket clientSocket;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private boolean printErrorMessages = false;

    public GetJob()
    {
    }

    private void printHelpText()
    {
        System.err.println("Parameters:");
        System.err.println("===========");
        System.err.println("-h");
        System.err.println("     : This text");
        System.err.println("-host hostname");
        System.err.println("     : connect to the remote server on the host 'hostname'");
        System.err.println("-port 1234");
        System.err.println("     : use the given port instead of the default port " + ServerPort);
        System.err.println("-requestId clientName");
        System.err.println("     : send 'clientName' to the server as identification.");
        System.err.println("-pv 1");
        System.err.println("     : use the given protocl version (default:2)");
        System.err.println("-errors");
        System.err.println("     : Print Status and error messages.");
        System.err.println("-param hw=RaspberryPi:cpuCores=4");
        System.err.println("     : defines addition parameters that will be send to the server.");


    }

    public void getConfigFromCommandLine(String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            if(true == args[i].startsWith("-"))
            {
                if(true == "-host".equals(args[i]))
                {
                    i++;
                    ServerURL = args[i];
                }
                else if(true == "-port".equals(args[i]))
                {
                    i++;
                    ServerPort = Integer.parseInt(args[i]);
                }
                else if(true == "-requestId".equals(args[i]))
                {
                    i++;
                    ClientId = args[i];
                }
                else if(true == "-pv".equals(args[i]))
                {
                    i++;
                    protocolVersion = Integer.parseInt(args[i]);
                }
                else if(true == "-param".equals(args[i]))
                {
                    i++;
                    additionalParameters = args[i];
                    additionalParameters = additionalParameters.trim();
                    if(true == additionalParameters.startsWith(":"))
                    {
                        additionalParameters = additionalParameters.substring(1);
                    }
                    if(false == additionalParameters.endsWith(":"))
                    {
                        additionalParameters = additionalParameters + ":";
                    }
                }
                else if(true == "-h".equals(args[i]))
                {
                    printHelpText();
                    System.exit(0);
                }
                else
                {
                    System.err.println(" ! ! ! Invalid Parameter : " + args[i]);
                    printHelpText();
                    System.exit(1);
                }
            }
        }
    }

    private void doTheWork()
    {
        String JobInfo = "";
        // Connect to server
        connectTo(ServerURL, ServerPort);
        if(false == isConnected)
        {
            if(true == printErrorMessages)
            {
                System.err.println("Could not connect to Server !");
            }
            System.exit(1);
        }

        JobInfo =  getJobInformation();

        disconnect();
        if(null != JobInfo)
        {
            JobInfo = JobInfo.trim();
            if(0 < JobInfo.length())
            {
                System.out.println("Job:" + JobInfo);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        GetJob m = new GetJob();
        m.getConfigFromCommandLine(args);
        m.doTheWork();
        System.exit(0);
    }

    public void disconnect()
    {
        try
        {
            clientSocket.close();
        }
        catch (IOException e)
        {
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
        isConnected = false;
    }

    public String getJobInformation()
    {
        try
        {
            if(1 == protocolVersion)
            {
                if(null == ClientId)
                {
                    outToServer.writeBytes("getNextJob\n");
                }
                else
                {
                    outToServer.writeBytes("login:" + ClientId + "\n");
                }
                outToServer.flush();
                String rep;
                rep = inFromServer.readLine();
                return rep;
            }
            else if(2 == protocolVersion)
            {
                do
                {
                    if(null == ClientId)
                    {
                        outToServer.writeBytes("2:" + additionalParameters + "\n");
                    }
                    else
                    {
                        outToServer.writeBytes("2:name=" + ClientId + ":" + additionalParameters + "\n");
                    }
                    outToServer.flush();
                    String rep;
                    rep = inFromServer.readLine();
                    if(rep.startsWith("2:0:"))
                    {
                        // OK
                        return rep;
                    }
                    else if(rep.startsWith("2:1:"))
                    {
                        if(true == printErrorMessages)
                        {
                            System.out.println("No more new Jobs!");
                        }
                        return "";
                    }
                    else if(rep.startsWith("2:2:"))
                    {
                        Thread.sleep(1000);
                    }
                }while(true);
            }
            else
            {
                if(true == printErrorMessages)
                {
                    System.out.println("ERROR: Invalid Protocol Version(" + protocolVersion + ")!");
                }
            }
        }
        catch (IOException e)
        {
            isConnected = false;
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
        catch (InterruptedException e)
        {
            isConnected = false;
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    public void connectTo(String serverURL, int serverPort)
    {
        try
        {
            clientSocket = new Socket(serverURL, serverPort);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            isConnected = true;
        }
        catch (UnknownHostException e)
        {
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
    }

}
