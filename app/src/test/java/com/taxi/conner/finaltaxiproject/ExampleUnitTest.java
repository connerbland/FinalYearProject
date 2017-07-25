package com.taxi.conner.finaltaxiproject;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.text.TextUtils;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@SuppressWarnings("deprecation")
public class ExampleUnitTest extends ApplicationTestCase<Application> {

    Context context;
    CountDownLatch signal = null;
    String mResult = null;
    Exception mError = null;

    public ExampleUnitTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        signal = new CountDownLatch(1);
    }

    @Override
    protected void tearDown() throws Exception {
        signal.countDown();
    }

    public void testLogin() throws InterruptedException {
        BackgroundWorker task = new BackgroundWorker(context);
        task.setListener(new BackgroundWorker.getTaskListener() {
            @Override
            public void onComplete(String result, Exception e) {
                mResult = result;
                mError = e;
                signal.countDown();
            }
        }).execute("login", "dee@debug.co.uk", "2ff7e12125e048806a08accfa5f07e2c4ac791b53c11b4d1105258d5fee9f6ca1edb9dcdaf7007c50c38695764faf29eec83cc571637ef8ae9e5e13a7ca0a330");
        signal.await();

        assertNull(mError);
        assertFalse(TextUtils.isEmpty(mResult));
    }
}