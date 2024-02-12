package com.gustinmi.sensorius.utils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/** Ensure logger for class is always defined by correct class name. Prevent copy/paste errors - common source of errors.
 * Quite some error occured with spring when wrong logger was passed into component 
* Use like this:
* <code>
* public static final Logger log = LoggingFactory.loggerForThisClass();
* </code>
* 
* @author gustinmi
*
*/
public class LoggingFactory {

    public static final String LOG_LINE_SEPARATOR = System.getProperty("line.separator");
    
    /** Gets the logger for caller class
     * @return
     */
    public static Logger loggerForThisClass() {
        
        // We use the third stack element; second is this method, first is .getStackTrace()
        final StackTraceElement myCaller = Thread.currentThread().getStackTrace()[2];
        return LoggerFactory.getLogger(myCaller.getClassName());
    }
   
    
}
