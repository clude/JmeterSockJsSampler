package com.ouyeel.hippo.JmeterSockjsSampler;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.Serializable;

/**
 * @Description
 * @Author clude
 * @Date 2022/10/11
 * @Version 1.0
 **/
public class SocketJsStopSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;
    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult sampleResult = new SampleResult();
        // record the start time of a sample
        sampleResult.sampleStart();

        WebSocketStompClient stompClient = SocketJsHelper.threadLocalCachedConnection.get();
        stompClient.stop();

        sampleResult.sampleEnd();
        sampleResult.setSuccessful(true);
        sampleResult.setResponseMessage("Socket Close");
        sampleResult.setResponseCodeOK();

        return sampleResult;
    }
}
