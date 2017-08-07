package bi.know.kettle.neo4j.output;

import java.net.URI;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



public class Neo4JOutput  extends BaseStep implements StepInterface {
	private static Class<?> PKG = Neo4JOutput.class; // for i18n purposes, needed by Translator2!!

	private Neo4JOutputMeta meta; 
	private Neo4JOutputData data;
	private Transaction tx;
	private int nbRows;
	private String[] fieldNames;
	private Object[] r; 
	private Object[] outputRow;
	private Driver driver; 
	private Session session;
	
    public Neo4JOutput(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s,stepDataInterface,c,t,dis);
	}
    
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
    	
    	meta = (Neo4JOutputMeta)smi;
    	data =  (Neo4JOutputData)sdi;
    	
    	r = getRow();
    	
        if (first){
            first = false;
            nbRows= 0;
            
	        data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
          	data.outputRowMeta.addValueMeta(new ValueMetaString("nodeURI"));
   	        fieldNames =  data.outputRowMeta.getFieldNames(); 
        }
        
        if(r == null){
        	setOutputDone();
        	return false; 
        }else{
        	try{ 
        		outputRow = RowDataUtil.resizeArray( r, data.outputRowMeta.size());
        		if(meta.getFromNodeProps().length > 0) {
    	   	     	createNode(meta.getFromNodeLabels(), meta.getFromNodeProps(), meta.getFromNodePropNames()); 
        		}else {
        			System.out.println("From node has no properties, ignoring");
        		}
        		if(meta.getToNodeProps().length > 0) {
    	   	     	createNode(meta.getToNodeLabels(), meta.getToNodeProps(), meta.getToNodePropNames()); 
        		}else {
        			System.out.println("To node has no properties, ignoring.");
        		}
    			createRelationship();
	   	     	putRow(data.outputRowMeta, outputRow);
	   	     	nbRows++;
	   	     	setLinesWritten(nbRows);
	   	     	setLinesOutput(nbRows);
        	}catch(Exception e){
        		logError(BaseMessages.getString(PKG, "Neo4JOutput.addNodeError") + e.getMessage());
        	}
        	
        	try{            	
        	}catch(Exception e){
        		logError(BaseMessages.getString(PKG, "Neo4JOutput.addRelationshipError") + e.getMessage());
        	}
        	return true; 
        }
    }
    
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
	    meta = (Neo4JOutputMeta)smi;
	    data = (Neo4JOutputData)sdi;
	    
	    String url = meta.getProtocol() + "://" + meta.getHost() + ":" + meta.getPort();  
	    driver = GraphDatabase.driver(url, AuthTokens.basic(meta.getUsername(), meta.getPassword()));
	    session = driver.session();
	    tx = session.beginTransaction();
	    
	    return super.init(smi, sdi);
	}

	/**
	 * TODO: 
	 * 1. handle errors on session.close();
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		try {
			tx.success();
			tx.close();
		}catch(ClientException ce) {
			System.out.println("CE: " + ce.getMessage());
		}
		session.close();
		driver.close();
	    super.dispose(smi, sdi);
	}

	
	/**
	 * TODO: 
	 * 1. parameterized statements 
	 * 2. batch mode 
	 * 3. option to return node id (compatible with batch mode?)
	 * @return
	 */
	public int createNode(String[] nLabels, String[] nProps, String[] nPropNames) {
    	String[] nodeLabels = nLabels;
    	String[] nodeProps = nProps;
    	String[] nodePropNames = nPropNames;
    	
		// Add labels
		String labels = "n:";
		// TODO: convert to List<String> 
    	for(int i=0; i < nodeLabels.length; i++){
    		String label = escapeLabel(String.valueOf(r[Arrays.asList(fieldNames).indexOf(nodeLabels[i])]));
    		labels += label;
    		if(i != (nodeLabels.length)-1) {
    			labels += ":";
    		}
    	}
		
    	// Add properties
    	String props = " { "; 
    	for(int i=0; i < nodeProps.length; i++){
    		String propName = "";
    		if(!nodePropNames[i].isEmpty()) {
    			propName = nodePropNames[i]; 
    		}else {
    			propName = nodeProps[i]; 
    		}
    		String tmpPropStr = String.valueOf(r[Arrays.asList(fieldNames).indexOf(nodeProps[i])]);
    		tmpPropStr = escapeProp(tmpPropStr);
    		props += propName + " : " + "\"" + tmpPropStr + "\"";
    		if(i != (nodeProps.length)-1) {
    			props += ", ";
    		}
    		// e.g. { name: 'Andres', title: 'Developer' }
    	}
    	props += "}";

		// CREATE (n:Person:Mens:`Human Being` { name: 'Andres', title: 'Developer' }) return n;
    	String stmt = "MERGE (" + labels + props + ");";
    	
    	System.out.println(stmt);
    	
    	
    	try{
    		tx.run(stmt);
    	}catch(Exception e) {
        	logError("Error executing statement: " + stmt);
    	}
		return 0; 
	}
	
	
    private void createRelationship(){

    	try {
        	String[] fromNodeProps = meta.getFromNodeProps();
        	String[] toNodeProps = meta.getToNodeProps();
        	
        	String[] fromNodePropNames = meta.getFromNodePropNames();
        	String[] toNodePropNames = meta.getToNodePropNames();
        	
        	String[] fNodeLabels = meta.getFromNodeLabels();
        	String[] tNodeLabels = meta.getToNodeLabels();
        	
        	String[] relProps = meta.getRelProps();
        	String[] relPropNames = meta.getRelPropNames(); 
        	
        	String fLabels = "";
        	for(int i=0; i < fNodeLabels.length; i++){
        		String label = escapeProp(String.valueOf(r[Arrays.asList(fieldNames).indexOf(fNodeLabels[i])]));
        		fLabels += label;
        		if(i != (fNodeLabels.length)-1) {
        			fLabels += ":";
        		}
        	}
        	String tLabels = "";
        	for(int i=0; i < tNodeLabels.length; i++){
        		String label = escapeProp(String.valueOf(r[Arrays.asList(fieldNames).indexOf(tNodeLabels[i])]));
        		tLabels += label;
        		if(i != (tNodeLabels.length)-1) {
        			tLabels += ":";
        		}
        	}        	
        	
        	String props = ""; 
        	for(int i=0; i < fromNodeProps.length; i++){
        		String prop = "";
        		if(i == 0) {
        			prop += " WHERE a.";
        		}else {
        			prop += " AND a.";
        		}
        		if(!fromNodePropNames[i].isEmpty()) {
        			prop += fromNodePropNames[i]; 
        		}else {
        			prop += fromNodeProps[i]; 
        		}
        		
        		String tmpPropStr = String.valueOf(r[Arrays.asList(fieldNames).indexOf(fromNodeProps[i])]); 
        		tmpPropStr = escapeProp(tmpPropStr);
        		
        		props += prop + " = " + "\"" + tmpPropStr + "\"";
        	}

        	for(int i=0; i < toNodeProps.length; i++){
        		String prop = "";
    			prop += " AND b.";
        		if(!toNodePropNames[i].isEmpty()) {
        			prop += toNodePropNames[i]; 
        		}else {
        			prop += toNodeProps[i]; 
        		}
        		
        		String tmpPropStr = String.valueOf(r[Arrays.asList(fieldNames).indexOf(toNodeProps[i])]);
        		tmpPropStr = escapeProp(tmpPropStr); 
        		props += prop + " = " + "\"" + tmpPropStr  + "\"";
        	}

        	
        	// Add properties
        	String relPropStr = ""; 
        	if(relProps.length > 0) {
            	relPropStr = " { "; 
            	for(int i=0; i < relProps.length; i++){
            		String propName = "";
            		if(!relPropNames[i].isEmpty()) {
            			propName = relPropNames[i]; 
            		}else {
            			propName = relProps[i]; 
            		}
            		String tmpPropStr = String.valueOf(r[Arrays.asList(fieldNames).indexOf(relProps[i])]);
            		tmpPropStr = escapeProp(tmpPropStr); 
            		relPropStr += propName + " : " + "\"" + tmpPropStr + "\"";
            		if(i != (relProps.length)-1) {
            			relPropStr += ", ";
            		}
            		// e.g. { name: 'Andres', title: 'Developer' }
            	}
            	relPropStr += "}";
        	}
        	String stmt = "MATCH (a:" + fLabels + "), (b:" + tLabels + ")"
        			+ props
        			+ " CREATE (a)-[r:`" + String.valueOf(r[Arrays.asList(fieldNames).indexOf(meta.getRelationship())]) + "` " + relPropStr + "] -> (b)"; 
        	try{
        		tx.run(stmt);
        	}catch(Exception e) {
            	logError("Error executing statement: " + stmt);
        	}
    	}catch(NullPointerException npe) {
    		System.out.println("Not all relationship properties (from node, to node, relationship) were provided. No relationship will be created.");
    	}catch(ArrayIndexOutOfBoundsException oobe) {
    		System.out.println("Not all relationship properties (from node, to node, relationship) were provided. No relationship will be created.");
    	}
    }
    
    
	public String escapeLabel(String str) {
		if(str.contains(" ") || str.contains(".")) {
			str = "`" + str + "`";
		}
		
		return str; 
	}
    
	public String escapeProp(String str) {
		String newStr = "";
		StringEscapeUtils su = new StringEscapeUtils();
		newStr = su.escapeJava(str);
		return newStr; 
	}
    
      private int addRelationshipProperty(String relationshipId, String[] relProps){
    	try{
    		String relPropsJSON = "{";
    		for(int i=0; i < relProps.length; i++){
    			String propName = (String)relProps[i];
    			String propVal = (String)r[Arrays.asList(fieldNames).indexOf(relProps[i])];
    			relPropsJSON += "\"" + propName + "\" : \"" +  propVal + "\" ";
    			if(i <  (relProps.length-1)){
    				relPropsJSON += ", ";
    			}
    		}
	       	return 0; 
    	}catch(Exception e){
        	logDetailed(BaseMessages.getString(PKG, "Neo4JOutput.addRelationshipPropertyError") + relationshipId);
        	return -1; 
    	}
    }
    
    private String getIdFromURI(URI uri){
    	return uri.toString().substring(uri.toString().lastIndexOf("/")+1);
    }
}
