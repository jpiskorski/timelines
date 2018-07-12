package it.jrc.osint.extract.timelines.features;

import java.util.Properties;

import it.jrc.osint.extract.timelines.TimelineItem;

public class MapValueOfTargetEntityParticipation extends Feature {

	@Override
	public void initialize(Properties configuration) throws FeatureException 
	 { // Nothing to initialise

	 }

	@Override
	public String computeValue(TimelineItem it) 
	 { return Boolean.toString(it.getEventMatch().getEvent().targetEntityIsParticipant());
	 }

	@Override
	public String getFeatureType() 
	 { return "{true,false}";
	 }

}
