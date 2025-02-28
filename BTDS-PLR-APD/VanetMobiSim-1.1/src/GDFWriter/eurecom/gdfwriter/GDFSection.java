package eurecom.gdfwriter;

/**
 * <p>Title: GDF Section</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */

import geotransform.coords.Gdc_Coord_3d;
import geotransform.coords.Utm_Coord_3d;
import geotransform.transforms.Utm_To_Gdc_Converter;
import eurecom.spacegraph.*;
import eurecom.gdfwriter.records.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.core.*;
import de.uni_stuttgart.informatik.canu.mobisim.core.*;
import de.uni_stuttgart.informatik.canu.spatialmodel.geometry.*;
import de.uni_stuttgart.informatik.canu.senv.core.*;
/**
 * GDF Section used by GDFWriter
 * @author Jerome Haerri 
 */
public class GDFSection {
  /**
   * Parent GDF Writer object
   */
  protected GDFWriter writer;

  /**
   * Storage for coordinate records:
   * Key - Coordinate id, Value - associated XYZRecord
   */
  protected java.util.Map coordinates = new java.util.HashMap();

  /**
   * Storage for node records:
   * Key - Node id, Value - associated NodeRecord
   */
  protected java.util.Map nodes = new java.util.HashMap();

  /**
   * Storage for edge records:
   * Key - Edge id, Value - associated EdgeRecord
   */
  protected java.util.Map edges = new java.util.HashMap();

  /**
   * Storage for face records:
   * Key - Face id, Value - associated FaceRecord
   */
  protected java.util.Map faces = new java.util.HashMap();

  /**
   * Storage for point features records:
   * Key - Point Feature id, Value - associated PointFeatureRecord
   */
  protected java.util.Map pointFeatures = new java.util.HashMap();

  /**
   * Storage for line features records:
   * Key - Line Feature id, Value - associated LineFeatureRecord
   */
  protected java.util.Map lineFeatures = new java.util.HashMap();

  /**
   * Storage for area features records:
   * Key - Area Feature id, Value - associated AreaFeatureRecord
   */
  protected java.util.Map areaFeatures = new java.util.HashMap();

  /**
   * Storage for complex features records:
   * Key - Complex Feature id, Value - associated ComplexFeatureRecord
   */
  protected java.util.Map complexFeatures = new java.util.HashMap();

  /**
   * Storage for relationships records:
   * Key - Relationship id, Value - associated RelationshipRecord
   */
  protected java.util.Map relationships = new java.util.HashMap();

  /**
   * Storage for attributes records:
   * Key - Attribute id, Value - associated SegmentedAttributeRecord
   */
  protected java.util.Map attributes = new java.util.HashMap();
	
	/**
   * Storage for attributes Definition records:
   * Key - Attribute id, Value - associated AttributeDefinitionRecord
   */
  protected java.util.Map attributesDefinition = new java.util.HashMap();
	
	/**
   * Storage for attributes Definition records:
   * Key - Attribute id, Value - associated AttributeDefinitionRecord
   */
  protected java.util.Map attributeValuesDefinition = new java.util.HashMap();
	
	/**
   * Storage for default attributes records:
   * Key - Attribute id, Value - associated DefaultAttributeRecord
   */
  protected java.util.Map defaultAttributes = new java.util.HashMap();
	
	/**
   * Storage for feature definition records:
   * Key - Attribute id, Value - associated FeatureDefinitionRecord
   */
  protected java.util.Map featureDefinitions = new java.util.HashMap();

	/**
	* Source Record (A single source record)
	*/
	protected SourceRecord sourceRec = new SourceRecord("1401","1");
	
  /**
   * Constructor
   * @param writer parent GDF Writer object
   */
  public GDFSection(GDFWriter writer)
  {
    this.writer = writer;
  }
	
	/**
	* Generates the GDF Records 
	*/
	protected void generateGDFRecords() throws Exception {
		java.util.Random rand=writer.getUniverse().getRandom();
		
		int sourceDesc = (new Integer(sourceRec.getDescr())).intValue(); // Source Description Identifier. We only consider one source.
		
		
		java.util.Iterator iter = writer.spatialModel.getElements().values().iterator();
    while (iter.hasNext()) {
      SpatialModelElement element = (SpatialModelElement)iter.next();
			if ((element.getClassCode().equals("41") && element.getSubClassCode().equals("20"))
							 || (element.getClassCode().equals("71") && element.getSubClassCode().equals("10"))
							 || (element.getClassCode().equals("72") && element.getSubClassCode().equals("20")) 
							 ||	(element.getClassCode().equals("72") && element.getSubClassCode().equals("30")) ) {
				
				
				if (element.getClassCode().equals("41") && element.getSubClassCode().equals("20")) {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("4120","Junction","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				else if (element.getClassCode().equals("71") && element.getSubClassCode().equals("10")) {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("7110","Building","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				else if (element.getClassCode().equals("72") && element.getSubClassCode().equals("20")) {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("7220","Traffic Sign","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				else {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("7230","Traffic Light","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				
				java.util.ArrayList tmpAttributeRecord = new java.util.ArrayList();
				String attr_id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				SegmentedAttributeRecord attribute = new SegmentedAttributeRecord(attr_id,sourceDesc);
				
				java.util.Iterator iter2 = element.getAttributes().keySet().iterator();
				while (iter2.hasNext()) {
				  String attributeCode = (String)iter2.next();
					
					if(!attributesDefinition.containsKey(attributeCode)) {
						
					  if(attributeCode =="TS") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"TRAFFIC SIGN CLASS");
							attributesDefinition.put(attributeCode,attDefRec);
									
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"TS",
																																"50", "RIGHT OF WAY");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
							
							String attr_val_id_2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_2,"TS",
																																"51", "DIRECTIONAL");
							attributeValuesDefinition.put(attr_val_id_2,attValDefRec);
						}
						else if (attributeCode =="SY") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"SYMBOL ON TRAFFIC SIGN");
							attributesDefinition.put(attributeCode,attDefRec);
							
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"SY",
																																"0", "ALL TRAFFIC");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
						
						}
						else if (attributeCode =="50") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"RIGHT OF WAY");
							attributesDefinition.put(attributeCode,attDefRec);
							
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"50",
																																"15", "YIELD");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
							
							String attr_val_id_2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_2,"50",
																																"16", "STOP");
							attributeValuesDefinition.put(attr_val_id_2,attValDefRec);			
						}
						else if (attributeCode =="51") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"DIRECTIONAL");
							attributesDefinition.put(attributeCode,attDefRec);
						
						}
						else {
					    AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																												((String)(element.getAttributes().get(attributeCode))).length());
						  attributesDefinition.put(attributeCode,attDefRec);
						}
						
						
						
						DefaultAttributeRecord defAttRec = new DefaultAttributeRecord(attributeCode);
						defaultAttributes.put(attributeCode,defAttRec);
					}
					attribute.addAttribute(attributeCode,(String)(element.getAttributes().get(attributeCode))); 
				}
				
				attributes.put(attribute.getID(),attribute);
				tmpAttributeRecord.add(attribute.getID());
				// JHNote (08/02/2006) if we have more than one attribute record per element, we can add them 
				//										  to tmpAttributeRecord ArrayList.
				
				java.util.ArrayList tmpNodeRecord = new java.util.ArrayList();
				
				Point p = (Point)element.getGeometry();
				if ((p.getID() ==null) ||(p.getID() =="") ) {
				  String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					p.setID(pointID1);
				}
					
				tmpNodeRecord.add(p.getID());
				// JHNote (08/02/2006) if we have more than one node per point (like a 3-points junction), we add 
				//										  the points to tmpNodeRecord ArrayList.
				
				
				
				if (!nodes.containsKey(p.getID())) {
					Position3D pos = p.getPosition();
					if ((pos.getID() ==null) ||(pos.getID() =="") ) {
						String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						pos.setID(xyzID1);
					}
				
					/*if (pos.getID() ==null)
						System.exit(-1);	*/
					Vertex vertex = writer.spatialModel.getGraph().getVertex(pos.getX(),pos.getY());
					
					XYZRecord positionRecord = new XYZRecord(pos.getID(),sourceDesc);
					//positionRecord.addPoint(pos.getX(),pos.getY(),pos.getZ());
					setOrigin(pos);
					convert(pos,positionRecord);
					
					coordinates.put(positionRecord.getID(),positionRecord);
					
					NodeRecord nodeRecord1 = new NodeRecord(p.getID(),positionRecord.getID(), 2);
					nodes.put(nodeRecord1.getID(),nodeRecord1);
				}
				
				PointFeatureRecord pointRecord = new PointFeatureRecord(element.getID(),(element.getClassCode()+element.getSubClassCode()),
																																sourceDesc,tmpNodeRecord,tmpAttributeRecord);
				pointFeatures.put(pointRecord.getID(),pointRecord);
				
				java.util.ArrayList relations = element.getRelations();
				for (int i = 0; i < relations.size(); i++) {
				  RelationshipRecord rec = (RelationshipRecord)relations.get(i);
					
					if (!relationships.containsKey(rec.getID())) {
					  relationships.put(rec.getID(), rec);
					}			
				}
				
				tmpAttributeRecord.clear();
				tmpNodeRecord.clear();
			}
		}
		
		
	  java.util.Iterator iterBis = writer.spatialModel.getElements().values().iterator();
    while (iterBis.hasNext()) {
      SpatialModelElement element = (SpatialModelElement)iterBis.next();
		  // roadElements
			if (element.getClassCode().equals("41") && element.getSubClassCode().equals("10")) {
			  //java.util.Iterator iter = elements.values().iterator();
				
				FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("4110","RoadElement","ENG","");
				featureDefinitions.put(featureDef.getCode(),featureDef);
				
				java.util.ArrayList tmpAttributeRecord = new java.util.ArrayList();
				
				String attr_id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				SegmentedAttributeRecord attribute = new SegmentedAttributeRecord(attr_id,sourceDesc);
				java.util.Iterator iter2 = element.getAttributes().keySet().iterator();
				while (iter2.hasNext()) {
				  String attributeCode = (String)iter2.next();
					
					if(!attributesDefinition.containsKey(attributeCode)) {
						if(attributeCode =="VT") {
							String attVal = (String)element.getAttributes().get(attributeCode);
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										attVal.length(),
																										"N","COD",0,0,0,"VEHICLE TYPE");
							attributesDefinition.put(attributeCode,attDefRec);
							
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"VT",
																																"25", "PEDESTRIAN");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
							
							String attr_val_id_2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_2,"VT",
																																"11", "CAR");
							attributeValuesDefinition.put(attr_val_id_2,attValDefRec);
							 
							String attr_val_id_3 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_3,"VT",
																																"20", "TRUCK");
							attributeValuesDefinition.put(attr_val_id_3,attValDefRec);
							
							String attr_val_id_4 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_4,"VT",
																																"17", "BUS");
							attributeValuesDefinition.put(attr_val_id_4,attValDefRec);
							
							String attr_val_id_5 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_5,"VT",
																																"0", "ANY");
							attributeValuesDefinition.put(attr_val_id_5,attValDefRec);
							
						}
						else if (attributeCode =="NL") {
							String attVal = (String)element.getAttributes().get(attributeCode);
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										attVal.length(),"N","",0,0,0,"NUMBER OF LANES");
							attributesDefinition.put(attributeCode,attDefRec);
						
						}
						else if (attributeCode =="DF") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","COD",0,0,0,"DIRECTION OF TRAFFIC FLOW");
							attributesDefinition.put(attributeCode,attDefRec);
							
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"DF",
																																"1", "ALLOWED IN BOTH DIRECTIONS");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
							
							String attr_val_id_2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_2,"DF",
																																"2", "CLOSED IN POSITIVE DIRECTION");
							attributeValuesDefinition.put(attr_val_id_2,attValDefRec);
							
							String attr_val_id_3 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_3,"DF",
																																"3", "CLOSED IN NEGATIVE DIRECTION");
							attributeValuesDefinition.put(attr_val_id_3,attValDefRec);
							
							String attr_val_id_4 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_4,"DF",
																																"25", "CLOSED IN BOTH DIRECTIONS");
							attributeValuesDefinition.put(attr_val_id_4,attValDefRec);
							
						}
						else {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length());
							attributesDefinition.put(attributeCode,attDefRec);
						}
					  
						
						
						
						DefaultAttributeRecord defAttRec = new DefaultAttributeRecord(attributeCode);
						defaultAttributes.put(attributeCode,defAttRec);
					}
					attribute.addAttribute(attributeCode,(String)(element.getAttributes().get(attributeCode))); 
				}
				attributes.put(attribute.getID(),attribute);
				tmpAttributeRecord.add(attribute.getID());
					// JHNote (08/02/2006) if we have more than one attribute record per element, we can add them 
				//										  to tmpAttributeRecord ArrayList.
				
				// new generating the lineFeature Record
				
				Polyline shape = (Polyline)element.getGeometry();
				java.util.ArrayList points = shape.getPoints();
				java.util.ArrayList tmpEdgeRecord = new java.util.ArrayList();
				java.util.ArrayList tmpEdgeDirRecord = new java.util.ArrayList();
				String pointFeatureFrom=null;
				String pointFeatureTo=null;
				
				// We retreive the initial and final Point Feature of this LineFeature
				Point pFrom = (Point)points.get(0);
				Point pTo = (Point)points.get(points.size()-1);
				Position3D pos1 = pFrom.getPosition();
				Position3D pos2 = pTo.getPosition();
				
				Vertex vertexFrom = writer.spatialModel.getGraph().getVertex(pos1.getX(),pos1.getY());
				Vertex vertexTo = writer.spatialModel.getGraph().getVertex(pos2.getX(),pos2.getY());
				
				SpatialModelElement tmpElement1 = writer.spatialModel.mapVertexToJunction(vertexFrom);
				SpatialModelElement tmpElement2 = writer.spatialModel.mapVertexToJunction(vertexTo);		
				
				//System.out.println("Got first vertex with pos X : "+ pos1.getX() + " Y: " +pos1.getY());
				
				//System.out.println("Got second vertex with pos X : "+ pos2.getX() + " Y: " +pos2.getY());
				
				String id2 = tmpElement2.getID();
				PointFeatureRecord pointRecord1 = (PointFeatureRecord)pointFeatures.get(tmpElement1.getID());
				PointFeatureRecord pointRecord2 = (PointFeatureRecord)pointFeatures.get(id2);
				
				pointFeatureFrom = pointRecord1.getID();
				pointFeatureTo = pointRecord2.getID();
				// Done.
				
				for (int i = 1; i < points.size(); i++) {
				  Point p = (Point)points.get(i-1);
					if ((p.getID() ==null) ||(p.getID() =="") ) {
						String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						p.setID(pointID1);
					}
					
					Vertex vertex1 = null;
					Vertex vertex2 = null;
					NodeRecord nodeRecord1 = null;
					NodeRecord nodeRecord2 = null;
					
					
					if (!nodes.containsKey(p.getID())) {
						Position3D pos = p.getPosition();
						if ((pos.getID() == null) ||(pos.getID() == "") ) {
							String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							pos.setID(xyzID1);
						}
						/*if (pos.getID() ==null)
							System.exit(-1);	*/
							
						vertex1 = writer.spatialModel.getGraph().getVertex(pos.getX(),pos.getY());
					
						XYZRecord positionRecord = new XYZRecord(pos.getID(),sourceDesc);
						//positionRecord.addPoint(pos.getX(),pos.getY(),pos.getZ());
						setOrigin(pos);
						convert(pos,positionRecord);
						coordinates.put(positionRecord.getID(),positionRecord);
					
						nodeRecord1 = new NodeRecord(p.getID(),positionRecord.getID(), 2);
						nodes.put(nodeRecord1.getID(),nodeRecord1);
					}
					else {
						Position3D pos = p.getPosition();	
					  nodeRecord1 = (NodeRecord)nodes.get(p.getID());
						vertex1 = writer.spatialModel.getGraph().getVertex(pos.getX(),pos.getY());
					}
					/*if (i == 1) {
					  //pointFeatureFrom = p.getID();//(pointFeatureRecord)pointFeatures.get(p.getID());
						
						SpatialModelElement tmpElement = writer.spatialModel.mapVertexToJunction(vertex1);
						
						PointFeatureRecord pointRecord = (PointFeatureRecord)pointFeatures.get(tmpElement.getID());
						pointFeatureFrom = pointRecord.getID();
					}*/
					
					p = (Point)points.get(i);
					if ((p.getID() == null) ||(p.getID() =="") ) {
						String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						p.setID(pointID1);
					}
					
					if (!nodes.containsKey(p.getID())) {
						Position3D pos = p.getPosition();
						if ((pos.getID() ==null) ||(pos.getID() =="") ) {
							String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							pos.setID(xyzID1);
						}
						/*if (pos.getID() ==null)
							System.exit(-1);	*/
						
						vertex2 = writer.spatialModel.getGraph().getVertex(pos.getX(),pos.getY());
						
						XYZRecord positionRecord = new XYZRecord(pos.getID(),sourceDesc);
						//positionRecord.addPoint(pos.getX(),pos.getY(),pos.getZ());
						setOrigin(pos);
						convert(pos,positionRecord);
						
						coordinates.put(positionRecord.getID(),positionRecord);
					
						nodeRecord2 = new NodeRecord(p.getID(),positionRecord.getID(), 2);
						nodes.put(nodeRecord2.getID(),nodeRecord2);
					}
					else {
						Position3D pos = p.getPosition();
					  nodeRecord2 = (NodeRecord)nodes.get(p.getID());
						vertex2 = writer.spatialModel.getGraph().getVertex(pos.getX(),pos.getY());
					}
					
					/*if (i == (points.size()-1)) {
					  pointFeatureTo = p.getID();//(pointFeatureRecord)pointFeatures.get(p.getID());
						
						SpatialModelElement tmpElement = writer.spatialModel.mapVertexToJunction(vertex2);
						
						PointFeatureRecord pointRecord = (PointFeatureRecord)pointFeatures.get(tmpElement.getID());
						pointFeatureTo = pointRecord.getID();
					}*/
					
					Edge edge = writer.spatialModel.findEdge(vertex1,vertex2);
					int dir = writer.spatialModel.findDirEdge(vertex1,vertex2);
					
					// JHNote (08/02/2006) if an edge contains intermediate XYZRecords (curviligne Edge), we can such 
					//										  XYZRecord using edgeRecord.addXYZRecord(...);
						
					// JHNote (09/02/2006): the Edge object cannot guarantee an unique ID throughout the simulation 
					//											(when the graph is reorganized, there is a non null probability that
				 // 												the vertex IDs will be changed.)
					//											For a unique edgeRecordID, we do similarily to Edge. We concatenate 
					//										  the two nodeRecord IDs.
					// JHNote (10/02/2006): The concatenated ID is too long for GDF. We try for now to generate a new ID.
					
					String edgeRecordID = nodeRecord1.getID()+":"+nodeRecord2.getID();
					String edgeID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));																																																 			
					//EdgeRecord edgeRecord = new EdgeRecord(edge.getID(),nodeRecord1.getID(),nodeRecord2.getID(),2);
				
					//	EdgeRecord edgeRecord = new EdgeRecord(edgeRecordID,nodeRecord1.getID(),nodeRecord2.getID(),2);
					EdgeRecord edgeRecord = new EdgeRecord(edgeID1,nodeRecord1.getID(),nodeRecord2.getID(),2);
					
					tmpEdgeRecord.add(edgeRecord.getID());
					tmpEdgeDirRecord.add(String.valueOf(dir));
					edges.put(edgeRecord.getID(),edgeRecord);
				}
				
				LineFeatureRecord lineRec = new LineFeatureRecord(element.getID(), (element.getClassCode()+element.getSubClassCode()), 
																													sourceDesc, tmpEdgeRecord,tmpEdgeDirRecord,tmpAttributeRecord,
																													pointFeatureFrom,pointFeatureTo);
				
				lineFeatures.put(lineRec.getID(),lineRec);
				java.util.ArrayList relations = element.getRelations();
				for (int i = 0; i < relations.size(); i++) {
				  RelationshipRecord rec = (RelationshipRecord)relations.get(i);
					
					if (!relationships.containsKey(rec.getID())) {
					  relationships.put(rec.getID(), rec);
					}			
				}
				
				tmpAttributeRecord.clear();
				tmpEdgeRecord.clear();
				tmpEdgeDirRecord.clear(); 
				
			}
			// junctions or traffic lights or traffic signs or buildings
			/*else if ((element.getClassCode().equals("41") && element.getSubClassCode().equals("20"))
							 || (element.getClassCode().equals("71") && element.getSubClassCode().equals("10"))
							 || (element.getClassCode().equals("72") && element.getSubClassCode().equals("20")) 
							 ||	(element.getClassCode().equals("72") && element.getSubClassCode().equals("30")) ) {
				
				
				if (element.getClassCode().equals("41") && element.getSubClassCode().equals("20")) {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("4120","Junction","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				else if (element.getClassCode().equals("71") && element.getSubClassCode().equals("10")) {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("7110","Building","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				else if (element.getClassCode().equals("72") && element.getSubClassCode().equals("20")) {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("7220","Traffic Sign","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				else {
					FeatureDefinitionRecord featureDef = new FeatureDefinitionRecord("7230","Traffic Light","ENG","");
					featureDefinitions.put(featureDef.getCode(),featureDef);
				}
				
				java.util.ArrayList tmpAttributeRecord = new java.util.ArrayList();
				String attr_id = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
				SegmentedAttributeRecord attribute = new SegmentedAttributeRecord(attr_id,sourceDesc);
				
				java.util.Iterator iter2 = element.getAttributes().keySet().iterator();
				while (iter2.hasNext()) {
				  String attributeCode = (String)iter2.next();
					
					if(!attributesDefinition.containsKey(attributeCode)) {
						
					  if(attributeCode =="TS") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"TRAFFIC SIGN CLASS");
							attributesDefinition.put(attributeCode,attDefRec);
									
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"TS",
																																"50", "RIGHT OF WAY");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
							
							String attr_val_id_2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_2,"TS",
																																"51", "DIRECTIONAL");
							attributeValuesDefinition.put(attr_val_id_2,attValDefRec);
						}
						else if (attributeCode =="SY") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"SYMBOL ON TRAFFIC SIGN");
							attributesDefinition.put(attributeCode,attDefRec);
							
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"SY",
																																"0", "ALL TRAFFIC");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
						
						}
						else if (attributeCode =="50") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"RIGHT OF WAY");
							attributesDefinition.put(attributeCode,attDefRec);
							
							String attr_val_id_1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							AttributeValueDefinitionRecord attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_1,"50",
																																"15", "YIELD");
							attributeValuesDefinition.put(attr_val_id_1,attValDefRec);
							
							String attr_val_id_2 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
							attValDefRec = new AttributeValueDefinitionRecord(attr_val_id_2,"50",
																																"16", "STOP");
							attributeValuesDefinition.put(attr_val_id_2,attValDefRec);			
						}
						else if (attributeCode =="51") {
							AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																										((String)(element.getAttributes().get(attributeCode))).length(),
																										"N","",0,0,0,"DIRECTIONAL");
							attributesDefinition.put(attributeCode,attDefRec);
						
						}
						else {
					    AttributeDefinitionRecord attDefRec = new AttributeDefinitionRecord(attributeCode,
																												((String)(element.getAttributes().get(attributeCode))).length());
						  attributesDefinition.put(attributeCode,attDefRec);
						}
						
						
						
						DefaultAttributeRecord defAttRec = new DefaultAttributeRecord(attributeCode);
						defaultAttributes.put(attributeCode,defAttRec);
					}
					attribute.addAttribute(attributeCode,(String)(element.getAttributes().get(attributeCode))); 
				}
				
				attributes.put(attribute.getID(),attribute);
				tmpAttributeRecord.add(attribute.getID());
				// JHNote (08/02/2006) if we have more than one attribute record per element, we can add them 
				//										  to tmpAttributeRecord ArrayList.
				
				java.util.ArrayList tmpNodeRecord = new java.util.ArrayList();
				
				Point p = (Point)element.getGeometry();
				if ((p.getID() ==null) ||(p.getID() =="") ) {
				  String pointID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
					p.setID(pointID1);
				}
					
				tmpNodeRecord.add(p.getID());
				// JHNote (08/02/2006) if we have more than one node per point (like a 3-points junction), we add 
				//										  the points to tmpNodeRecord ArrayList.
				
				
				
				if (!nodes.containsKey(p.getID())) {
					Position3D pos = p.getPosition();
					if ((pos.getID() ==null) ||(pos.getID() =="") ) {
						String xyzID1 = Integer.toString(rand.nextInt(Integer.MAX_VALUE));
						pos.setID(xyzID1);
					}
				
					
					Vertex vertex = writer.spatialModel.getGraph().getVertex(pos.getX(),pos.getY());
					
					XYZRecord positionRecord = new XYZRecord(pos.getID(),sourceDesc);
					//positionRecord.addPoint(pos.getX(),pos.getY(),pos.getZ());
					setOrigin(pos);
					convert(pos,positionRecord);
					
					coordinates.put(positionRecord.getID(),positionRecord);
					
					NodeRecord nodeRecord1 = new NodeRecord(p.getID(),positionRecord.getID(), 2);
					nodes.put(nodeRecord1.getID(),nodeRecord1);
				}
				
				PointFeatureRecord pointRecord = new PointFeatureRecord(element.getID(),(element.getClassCode()+element.getSubClassCode()),
																																sourceDesc,tmpNodeRecord,tmpAttributeRecord);
				pointFeatures.put(pointRecord.getID(),pointRecord);
				
				java.util.ArrayList relations = element.getRelations();
				for (int i = 0; i < relations.size(); i++) {
				  RelationshipRecord rec = (RelationshipRecord)relations.get(i);
					
					if (!relationships.containsKey(rec.getID())) {
					  relationships.put(rec.getID(), rec);
					}			
				}
				
				tmpAttributeRecord.clear();
				tmpNodeRecord.clear();
			}*/
			
		/*	else { // we do not cover other simple features or complex features. 
			  throw new Exception("GDFWriter.GDFSection.generateGDFRecords() : no support for such code " 
														+ element.getClassCode() + element.getSubClassCode()); 
			}*/
		}
	}
	
	/**
	 * Converts from Universal Transverse Mercator (UTM) coordinates to geodetic (GDC), i.e. lat/long. 
	 * @param pos Position3d
	 * @param xyzRec XYZRecord 
	 */
	protected void convert(Position3D pos, XYZRecord xyzRec) {
	  Gdc_Coord_3d gdc = new Gdc_Coord_3d();
		// JHNote (09/02/2006): We based our convertion on a UTM map code N. 32 
		// 											(Nice-Switzerland-Germany) and the northen hemisphere
		Utm_To_Gdc_Converter.Convert(new Utm_Coord_3d(pos.getX(),pos.getY(), 0,(byte)32,true), gdc);
		
		xyzRec.addPoint((int)((writer.origin.latitude + (gdc.latitude-writer.origin.latitude))/writer.scale_x),
										(int)((writer.origin.longitude + (gdc.longitude-writer.origin.longitude))/writer.scale_y),0);
		//System.exit(-1);
		//xyzRec.addPoint((int)(pos.getX()/writer.scale_x),(int)(pos.getY()/writer.scale_y),0);
	}
	
	/**
	 * Sets the origin geodetic coordinate (min longitude/min latitude of all xyzCoordinate of this Section). 
	 * @param pos Position3d
	 */
	protected void setOrigin(Position3D pos) {
		//Utm_Coord_3d utm = new Utm_Coord_3d();
    Gdc_Coord_3d gdc = new Gdc_Coord_3d();
		
		double min_X = writer.spatialModel.getGraph().getLeftmostCoordinate();
		double min_Y = writer.spatialModel.getGraph().getLowermostCoordinate();
		
		//Utm_To_Gdc_Converter.Convert(new Utm_Coord_3d(pos.getX(),pos.getY(), 0,(byte)32,true), gdc);
		Utm_To_Gdc_Converter.Convert(new Utm_Coord_3d(min_X,min_Y, 0,(byte)32,true), gdc);
		
		//System.out.println("Got the lowest coordinates lat= " + gdc.latitude + " lon " + gdc.longitude);
		//System.exit(-1);
		
		
		//Gdc_To_Utm_Converter.Convert(new Gdc_Coord_3d(pos.getY()*writer.scale_y, pos.getX()*writer.scale_x, 0), utm);
    if (writer.origin==null)
      writer.origin = gdc;
    else {
			if (gdc.longitude<writer.origin.longitude)
        writer.origin.longitude = gdc.longitude;
        
      if (gdc.latitude<writer.origin.latitude)
        writer.origin.latitude = gdc.latitude;
    }
	}

  /**
   * Loads the GDF Section
   */
  public void load() throws Exception {
    generateGDFRecords();
  }

  /**
   * Resolves "lazy" links
   */
  public void initialise() {
    // Nothing to initialize yet
  }
}

