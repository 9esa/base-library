package org.zuzuk.tasks;

/**
 * Created by Gavriil Sitnikov on 03/10/2014.
 * Thread that calls onInterruptException() method while InterruptedException throws in thread.
 * On any other exception there will be throw of RuntimeException. Because threads normally
 * shouldn't fail in background with no notification
 */
public abstract class RuntimeExceptionThread extends Thread {

    public abstract void exceptionRun() throws Exception;

    protected void onInterruptException() {
    }

    @Override
    public void run() {
        try {
            exceptionRun();
        } catch (InterruptedException ex) {
            onInterruptException();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}