package com.ouyeel.hippo.JmeterSockjsSampler;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.Serializable;

/**
 * @Description
 * @Author clude
 * @Date 2022/10/11
 * @Version 1.0
 **/
public class SocketJsMessageReceiveSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;
    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult sampleResult = new SampleResult();
        // record the start time of a sample
        sampleResult.sampleStart();

        int count = 0;
        try {
            while (count < 10) {
                String message = SocketJsHelper.messageQueue.take();
                count++;
                System.out.println("收到的消息: " + message);
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }

        sampleResult.sampleEnd();
        sampleResult.setSuccessful(true);
        sampleResult.setResponseMessage(String.format("Message receives done , count is %s", count));
        sampleResult.setResponseCodeOK();

        return sampleResult;

    }
}
