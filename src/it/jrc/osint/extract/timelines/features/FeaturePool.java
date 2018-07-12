package it.jrc.osint.extract.timelines.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import it.jrc.osint.extract.timelines.internal.Utils;

/**
 * 
 * This class provides functionality to launch a pool of features
 * 
 * @author Jakub Piskorski
 *
 */

public class FeaturePool { 
	
private Feature[] features;
	
	private HashMap<String,Feature> nameToFeature;
			
	private FeaturePool()
	 { this.nameToFeature = new HashMap<String,Feature>();	   		
	 }
	
	/** Returns the number of initialized features
	 * 
	 * @return the number of initialized features
	 */
	
	public int getNumberOfFeatures()
	 { return this.features.length; }
	
	/**
	 * Returns the i-th feature
	 * 
	 * @param i index of the feature
	 * @return the i-th feature or <code>null</code> in case the index is invalid
	 */
	
	public Feature getFeature(int i)
	 { if((i>=0)&&(i<this.features.length))
		 return this.features[i];		
	   else
		 return null; 
	 }
	
	/**
	 * Returns the feature with a given name
	 * 
	 * @param name name of the feature
	 * @return the feature with the given name or <code>null</code> in case the name is unknown
	 */
	
	public Feature getFeature(String name)
	 { return this.nameToFeature.get(name); }		
	
	/**
	 * Returns a set of names of all features
	 * @return a set of names of all features
	 */
	
	public Set<String> getFeatureNames()
	 { Set<String> myNames = new HashSet<String>();
	   Iterator<String> it = this.nameToFeature.keySet().iterator();
	   while(it.hasNext())
		 myNames.add(it.next());
	   return myNames;
	 }
	
	/**
	 * Method for creating FeaturePool
	 * 
	 * @param configuration configuration for all features. 
	 * 
	 * The property <b>FEATURES</b> should include a comma-separated list of items in the format: 
	 * classNameOfTheFeature(configName1:configName2: ... :configNameN)
	 * 
     * For each feature and configName (has to be UNIQUE) a different instance of the feature will be created.
     * For the initialization of the specific version (which will be named "configName") of the given feature 
     * only properties with an initial prefix "configName." will be considered. 	 
	 * 
	 * @return an instance of FeaturePool with features specified in the configuration 
	 * @throws FeaturePoolException 
	 */
	
	public static FeaturePool createInstance(Properties configuration) throws FeaturePoolException
	 { FeaturePool pool = new FeaturePool();
	   String featureNames = configuration.getProperty("FEATURES");
	   if(featureNames==null)
		  throw new FeaturePoolException("The property FEATURES is missing in the configuration");
	   HashMap<String,String> configNameToFeature = new HashMap<String,String>();	   	   
	   try { StringTokenizer st = new StringTokenizer(featureNames,",",false);	  	         
	         while(st.hasMoreTokens())
	           { StringTokenizer mT = new StringTokenizer(st.nextToken(),"(:)",false);
	             String featureName = mT.nextToken();	             
	             while(mT.hasMoreTokens())
	              { String configName = mT.nextToken();
	            	configNameToFeature.put(configName, featureName);	            	
	              }
	           }	                   
	       }
	   catch(Exception e)
	       { throw new FeaturePoolException("Error while parsing the value of FEATURES property: " + featureNames);		   
   	       }
	   int numFeatures = configNameToFeature.size();
	   pool.features = new Feature[numFeatures];	   
	   int i = 0;
	   Set<String> configNames = configNameToFeature.keySet();
	   for(String configName : configNames)
         { String feature = configNameToFeature.get(configName);
		   try { System.out.println("Initialising feature: " + feature + " with name: " + configName);
		         Feature newService = Feature.createInstance(feature);
		         Properties newProps = Utils.getPropertiesStartingWithPrefix(configuration, configName + ".");
		         System.out.println("Properties for the feature: " + newProps);
                 newService.initialize(newProps);
                 newService.setName(configName);                 
                 pool.features[i] = newService;
                 pool.nameToFeature.put(configName,newService);
                 i++;
               }
           catch(Exception e)
            { throw new FeaturePoolException("The feature: " + feature + " with name: " + configName + " could not be launched");
            }
         }
	   return pool;	
	 }  

}
