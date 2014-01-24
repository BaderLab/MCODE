package ca.utoronto.tdccbr.mcode.internal.util.layout;


/**
 * Classes that perform long tasks (like graph algorithms) can implement this interface
 * so that they can be monitored by a GUI like a <code>javax.swing.plaf.ProgressBarUI</code> or a
 * <code>javax.swing.ProgressMonitor</code>
 * Note: this was copied from giny.util because it is being phased out.  Eventually
 * the layout API will be available to use (TODO: remove when layout API is available)
 */
public interface MonitorableTask {

  /**
   * @return <code>true</code> if the task is done, false otherwise
   */
  public boolean isDone();

  /**
   * @return the current progress
   */
  public int getCurrentProgress();

  /**
   * @return the total length of the task
   */
  public int getLengthOfTask();

  /**
   * @return a <code>String</code> describing the task being performed
   */
  public String getTaskDescription();

  /**
   * @return a <code>String</code> status message describing what the task
   * is currently doing (example: "Completed 23% of total.", "Initializing...", etc).
   */
  public String getCurrentStatusMessage ();

  /**
   * Starts doing the task in a separate thread so that the GUI stays responsive
   *
   * @param return_when_done if <code>true</code>, then this method will return only when
   * the task is done, else, it will return immediately after spawning the thread that
   * performs the task
   */
  public void start (boolean return_when_done);

  /**
   * Stops the task if it is currently running.
   */
  public void stop();

  /**
   * @return <code>true</code> if the task was canceled before it was done
   * (for example, by calling <code>MonitorableSwingWorker.stop()</code>,
   * <code>false</code> otherwise
   */
  // TODO: Not sure if needed
  public boolean wasCanceled ();

}
