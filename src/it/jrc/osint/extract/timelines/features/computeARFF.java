package it.jrc.osint.extract.timelines.features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import it.jrc.osint.extract.timelines.Timeline;
import it.jrc.osint.extract.timelines.TimelineItem;
import it.jrc.osint.extract.timelines.internal.Utils;

/**
 * This program computes features in ARFF format using the FeaturePool object
 * <p>
 * %1 - path to the file to read Timeline object from
 * %2 - path to the file with annotated TimelineItems
 * %3 - path to the output ARFF file
 * %4 - path to the configuration file for initializing the pool of features
 *
 * @author Jakub Piskorski
 */


public class computeARFF {
	
	private static String EOL = System.lineSeparator();

	public static void main(String[] args) throws IOException, FeaturePoolException {
		String inputTimelineFile = args[0]; // timeline (binary)
		String annotatedDataFile = args[1]; // the file with the annotated timeline items
		String outputFileName = args[2]; // output ARFF file
		String FeaturePoolConfig = args[3]; // configuration file

		System.out.println("Launching the pool with features");
		// read the configuration file
		Properties poolProperties = Utils.readProperties(FeaturePoolConfig);
		// create a pool of services for computing features
		FeaturePool fPool = FeaturePool.createInstance(poolProperties);
		Set<String> myFeatureNames = fPool.getFeatureNames();
		int numFeatures = fPool.getNumberOfFeatures();
		System.out.println("The pool with features includes: ");
		for (String name : myFeatureNames)
			System.out.println("  - " + name);
		// read timeline
		System.out.println(" Loading timeline from: " + inputTimelineFile);
		Timeline t = Timeline.createFromFile(inputTimelineFile);		
		//System.out.println(t. + " Events Loaded");
		//HashMap<String, Event> evts = new HashMap<>();
		//for (Event e : events.data)
        //			evts.put(e.getGuid(), e);
		// read annotated event pairs
		ArrayList<String> annotatedData = Utils.FileToStringArray(annotatedDataFile, "UTF-8");
		int numTimelineItems = annotatedData.size();
		// compute feature values
		double[] processingTime = new double[fPool.getNumberOfFeatures()];
		Arrays.fill(processingTime, 0.0);
		System.out.println("Start computing features for all annotated data");
		try (BufferedWriter result = Files.newBufferedWriter(Paths.get(outputFileName), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			result.append("@relation entity-in-event-participation");
			result.append(EOL);
			result.append(EOL);
			//result.append("@attribute\tleft_guid\tstring").append(EOL);
			//result.append("@attribute\tright_guid\tstring").append(EOL);
			result.append("@attribute\tTIMELINE-ID\tstring").append(EOL);
			for (int i = 0; i < numFeatures; i++) {
				result.append("@attribute");
				result.append("\t");
				result.append(fPool.getFeature(i).getName());
				result.append("\t");
				result.append(fPool.getFeature(i).getFeatureType());
				result.append(EOL);
			}
			result.append("@attribute\tentity-in-event-participation\t{true,false,discard}");
			result.append(EOL);
			result.append(EOL);
			result.append("@data");
			result.append(EOL);
			// iterate over all annotated data (timeline items)
			int count = 0;
			for(int i=1; i<numTimelineItems; i++)
			//for (String ePair : eventPairs) 
			  { StringTokenizer st = new StringTokenizer(annotatedData.get(i), "\t");
				if (st.countTokens() != 6) 
				 { System.out.println("Wrong format in line: " + i);
				   continue;
				 }
				String ID = st.nextToken();	
				String ENTITY = st.nextToken();
				String SENTENCE = st.nextToken();
				String EVENT_PHRASE = st.nextToken();
				String EVENT_CATEGORY = st.nextToken();
				String TARGET_ENTITY_PARTICIPANT = st.nextToken();
				int id = Integer.valueOf(ID);
				TimelineItem tItem = t.getEvent(id);
				if(tItem == null) 
				 { System.out.println("TimelineItem with id: " + id + " is not available.");
					continue;
				}
				// iterate over all features in the pool
				result.append(ID);
				result.append(",");
				for (int k = 0; k < numFeatures; k++) { // get the feature
					Feature f = fPool.getFeature(k);
					// compute
					double startTime = System.currentTimeMillis();
					String val = f.computeValue(tItem);
					processingTime[k] = processingTime[k] + (System.currentTimeMillis() - startTime);
					result.append(val);
					result.append(",");
				}
				result.append(TARGET_ENTITY_PARTICIPANT);
				result.append(EOL);  
				
				count++;
				if (count % 10 == 0)
					System.out.println("Computed features for: " + count + " Timeline items");
			}
		}
		System.out.println("PROCESSING TIME: ");
		double totalTime = 0.0;
		for (int i = 0; i < numFeatures; i++)
			totalTime = totalTime + processingTime[i];
		for (int i = 0; i < numFeatures; i++)
			System.out.println(fPool.getFeature(i).getName() + " : " + processingTime[i] / 1000.0 + " seconds [" + roundOff(processingTime[i] / totalTime) + "%]");
	}

	private static String roundOff(double val) {
		return String.format("%.2f", val);
	}
	

}
