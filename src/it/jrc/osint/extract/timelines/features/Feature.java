package it.jrc.osint.extract.timelines.features;

import java.util.Properties;

import it.jrc.osint.extract.timelines.TimelineItem;

public abstract class Feature {
	
	private String name;	
	
	/**
	 * Initializes the feature
	 * 
	 * @param configuration an optional set of properties required to initialize the feature
	 *  
	 */
     public abstract void initialize(Properties configuration) throws FeatureException; 
     
     /**
	 * Computes feature value
	 * 
	 * @param it reference to a timeline object
	 * 
	 * @return feature value
	 */
	
	 public abstract String computeValue(TimelineItem it);
		
	 /**
	  * Return feature name
	  */
	
	 public String getName()
	  { return this.name; }
	
     /**
      * Returns the type of the feature (using WEKA ARFF file specification)
      * @return the type of the feature
      */
    
     public abstract String getFeatureType();
    
     /**
      * Sets configuration name
      */
    
     public void setName(String name)
      { this.name = name; }
    
     /**
      * Creates an instance of feature
      *  
      * @param name class name which implements the feature
      * 
      * @return a handle to the created instance of a feature  
      */
    
     public static Feature createInstance(String name)
      { Feature f = null;
        if(name!=null)
          { try
              { Class c = Thread.currentThread().getContextClassLoader().loadClass("it.jrc.osint.extract.timelines.features." + name);         
                f = (Feature)c.newInstance();
                // default name = class name               
                f.setName(c.getName());                
              }
            catch(Exception e)
             { return(null); }                 	     
          }           
	    return f;    	
      }    
                  
     /**
      * Default constructor
      */
     
     Feature()
      { // do nothing for the time being
    	; 
      }

}
