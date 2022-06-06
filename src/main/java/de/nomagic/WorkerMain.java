package de.nomagic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class WorkerMain
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private boolean somethingWrong = false;

    private String workerId = null;
    private int comTcpPort = 54321;

    public WorkerMain()
    {
    }

    public String getCommitID()
    {
        try
        {
            final InputStream s = WorkerMain.class.getResourceAsStream("/git.properties");
            final BufferedReader in = new BufferedReader(new InputStreamReader(s));

            String id = "";

            String line = in.readLine();
            while(null != line)
            {
                if(line.startsWith("git.commit.id.full"))
                {
                    id = line.substring(line.indexOf('=') + 1);
                }
                line = in.readLine();
            }
            in.close();
            s.close();
            return id;
        }
        catch( Exception e )
        {
            return e.toString();
        }
    }

    private void startLogging(final String[] args)
    {
        boolean colour = true;
        int numOfV = 0;
        for(int i = 0; i < args.length; i++)
        {
            if(true == "-v".equals(args[i]))
            {
                numOfV ++;
            }
            // -noColour
            if(true == "-noColour".equals(args[i]))
            {
                colour = false;
            }
        }

        // configure Logging
        switch(numOfV)
        {
        case 0: setLogLevel("warn", colour); break;
        case 1: setLogLevel("debug", colour);break;
        case 2:
        default:
            setLogLevel("trace", colour);
            System.err.println("Build from " + getCommitID());
            break;
        }
    }

    private void setLogLevel(String LogLevel, boolean colour)
    {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try
        {
            final JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            final String logCfg;
            if(true == colour)
            {
                logCfg =
                "<configuration>" +
                  "<appender name='STDERR' class='ch.qos.logback.core.ConsoleAppender'>" +
                  "<target>System.err</target>" +
                    "<encoder>" +
                       "<pattern>%highlight(%-5level) [%logger{36}] %msg%n</pattern>" +
                    "</encoder>" +
                  "</appender>" +
                  "<root level='" + LogLevel + "'>" +
                    "<appender-ref ref='STDERR' />" +
                  "</root>" +
                "</configuration>";
            }
            else
            {
                logCfg =
                "<configuration>" +
                  "<appender name='STDERR' class='ch.qos.logback.core.ConsoleAppender'>" +
                  "<target>System.err</target>" +
                    "<encoder>" +
                      "<pattern>%-5level [%logger{36}] %msg%n</pattern>" +
                    "</encoder>" +
                  "</appender>" +
                  "<root level='" + LogLevel + "'>" +
                    "<appender-ref ref='STDERR' />" +
                  "</root>" +
                "</configuration>";
            }
            ByteArrayInputStream bin;
            bin = new ByteArrayInputStream(logCfg.getBytes(StandardCharsets.UTF_8));
            configurator.doConfigure(bin);
        }
        catch (JoranException je)
        {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }


    private void configure(String[] args)
    {
        String workerCfg = "worker.cfg";
        for(int i = 0; i < args.length; i++)
        {
            if(true == args[i].endsWith(".cfg"))
            {
                workerCfg = args[i];
            }
        }
        // read worker.cfg
        Wini ini; // http://ini4j.sourceforge.net/
        try {
            ini = new Wini(new File(workerCfg));
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
            log.error("something went wrong!");
            return false;
        }
        if(null == workerId)
        {
            log.error("No worker ID given!");
            return false;
        }
        // TODO additional checks go here

        // We are now sure that we have everything needed to work
        return true;
    }

    private void work()
    {
        WorkerTask worker = new WorkerTask(workerId);
        CommunicationTask com = new CommunicationTask();
        worker.setComTask(com);
        com.setWorkerId(workerId);
        com.setPort(comTcpPort);
        com.setWorker(worker);

        worker.start();
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
        } while((worker.wantsToWork()) && (com.isAlive()));
    }

    public static void main(String[] args)
    {
        WorkerMain worker = new WorkerMain();
        worker.startLogging(args);
        worker.configure(args);
        if(false == worker.canWork())
        {
            System.out.println("I can not start working like this !");
            System.exit(23);
        }
        worker.work();
        System.out.println("Finished working.");
        System.exit(0);
    }

}
