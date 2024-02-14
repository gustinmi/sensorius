//#START
import org.apache.jmeter.threads.JMeterVariables
import org.apache.jmeter.util.JMeterUtils
def vars = new JMeterVariables()
def props = JMeterUtils.getJMeterProperties()
//#END

// Producer payload example:
// {"longitude": "46°04'53.6\"N","latitude": "14°29'43.5\"E","elevation": 300,"timestamp": 1707765126101,"temperature": 0.14}

def currentTimeMillis = System.currentTimeMillis();

def elevationRandom = new Random()
def elevation = elevationRandom.nextInt(205 - 200) + 200;

def tempRandom = new Random()
def temp = tempRandom.nextInt(37 - 22) + 22;


def reading = new StringBuilder();
reading.append("{");

// ID 
reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
reading.append(",\"latitude\": \"14°29'43.5\\\"E\"");
reading.append(",\"elevation\": " + String.format("%s", elevation));

// DATA
reading.append(",\"timestamp\": " + String.format("%s", currentTimeMillis));
reading.append(",\"temperature\": " + String.format("%s.01",temp));

reading.append("}");
def jsonSensorData = reading.toString();

log.info("jsonSensorData: " + jsonSensorData);
vars.put("jsonSensorData", jsonSensorData);