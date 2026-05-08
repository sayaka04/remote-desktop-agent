package remoteagent;

import remoteagent.handler.JobHandler;
import remoteagent.utils.SystemInfo;

public class Main {

    public static void main(String[] args) {

        SystemInfo.getScreenSize();

        JobHandler job = new JobHandler();
        job.start();


    }

}