package com.mapper.relationMatcher;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.mapper.utility.Constants;

public class Client
{

    /**
     * @param args
     * @throws IOException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
    {
        //new ReVerbTupleProcessor().processTuples(Constants.IE_TUPLES_PATH);
        new NellTupleProcessor().processTuples(Constants.IE_TUPLES_PATH);
        System.exit(1);
    }

}
