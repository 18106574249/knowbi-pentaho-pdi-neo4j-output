package bi.know.kettle.neo4j.output;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
//import org.neo4j.rest.graphdb.RestAPI;
//import org.neo4j.rest.graphdb.RestAPIFacade;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;




public class Neo4JOutputDialog extends BaseStepDialog implements StepDialogInterface{
	private static Class<?> PKG = Neo4JOutputDialog.class; // for i18n purposes, needed by Translator2!!

	
	private Neo4JOutputMeta input; 
	
	private Label wlProtocol, wlHost; 
	private TextVar wProtocol, wHost, wPort, wUsername, wPassword;
	private FormData fdlProtocol, fdProtocol, fdlHost, fdHost, fdlPort, fdPort, fdlUsername, fdUsername, fdlPassword, fdPassword;
	private Combo wKey;
	private TableView 	 wFromPropsGrid, wFromLabelGrid, wToPropsGrid, wToLabelGrid, wRelationshipGrid, wRelPropsGrid; 
	private String[]  fieldNames;
	private CTabFolder wTabFolder;
//	private CTabItem wNodeTab, wRelationshipTab;


	
	public Neo4JOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(Neo4JOutputMeta)in;
	}


	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook( shell );
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		
		// Fields
		try{
			RowMetaInterface prevFields = transMeta.getPrevStepFields( stepname );
			fieldNames = prevFields.getFieldNames();
		}catch(KettleStepException kse){
			logError( BaseMessages.getString( PKG, "TripleOutput.Log.ErrorGettingFieldNames" ));
		}


		

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.StepName.Label")); //$NON-NLS-1$
        props.setLook( wlStepname );
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
        props.setLook( wStepname );
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// protocol 
		wlProtocol = new Label(shell, SWT.RIGHT);
		wlProtocol.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.Protocol.Label"));
		props.setLook(wlProtocol);
		fdlProtocol = new FormData();
		fdlProtocol.left = new FormAttachment(0,0);
		fdlProtocol.right = new FormAttachment(middle,margin);
		fdlProtocol.top = new FormAttachment(0,margin);
		wlProtocol.setLayoutData(fdlProtocol);
		wProtocol = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wProtocol);
		wProtocol.addModifyListener(lsMod);
		fdProtocol = new FormData();
		fdProtocol.left = new FormAttachment(middle, 0);
		fdProtocol.right= new FormAttachment(100, 0);
		fdProtocol.top  = new FormAttachment(wProtocol, margin);
		wProtocol.setLayoutData(fdProtocol);

		// Host line
		wlHost=new Label(shell, SWT.RIGHT);
		wlHost.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.Host.Label")); //$NON-NLS-1$
        props.setLook( wlHost );
		fdlHost=new FormData();
		fdlHost.left = new FormAttachment(0, 0);
		fdlHost.right= new FormAttachment(middle, -margin);
		fdlHost.top  = new FormAttachment(wProtocol, margin);
		wlHost.setLayoutData(fdlHost);
		wHost=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook( wHost );
		wHost.addModifyListener(lsMod);
		fdHost=new FormData();
		fdHost.left = new FormAttachment(middle, 0);
		fdHost.right= new FormAttachment(100, 0);
		fdHost.top  = new FormAttachment(wProtocol, margin);
		wHost.setLayoutData(fdHost);
		
		
		// Port
		Label wlPort = new Label(shell, SWT.RIGHT);
		wlPort.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.Port.Label"));
		props.setLook(wlPort);
		fdlPort = new FormData();
		fdlPort.left = new FormAttachment(0,0);
		fdlPort.right = new FormAttachment(middle, -margin);
		fdlPort.top = new FormAttachment(wHost, margin);
		wlPort.setLayoutData(fdlPort);
		wPort = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wPort);
		wPort.addModifyListener(lsMod);
		fdPort = new FormData();
		fdPort.left = new FormAttachment(middle, 0);
		fdPort.right = new FormAttachment(100, 0);
		fdPort.top = new FormAttachment(wHost, margin);
		wPort.setLayoutData(fdPort);
		
		
		// Username
		Label wlUsername = new Label(shell, SWT.RIGHT);
		wlUsername.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.Username.Label"));
		props.setLook(wlUsername);
		fdlUsername = new FormData();
		fdlUsername.left = new FormAttachment(0,0);
		fdlUsername.right = new FormAttachment(middle, -margin);
		fdlUsername.top = new FormAttachment(wPort, margin);
		wlUsername.setLayoutData(fdlUsername);
		wUsername = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wUsername);
		wUsername.addModifyListener(lsMod);
		fdUsername = new FormData();
		fdUsername.left = new FormAttachment(middle, 0);
		fdUsername.right = new FormAttachment(100, 0);
		fdUsername.top = new FormAttachment(wPort, margin);
		wUsername.setLayoutData(fdUsername);
		
			
		// Password
		Label wlPassword=new Label(shell, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.Password.Label")); //$NON-NLS-1$
        props.setLook( wlPassword);
		fdlPassword = new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.right= new FormAttachment(middle, -margin);
		fdlPassword.top  = new FormAttachment(wUsername, margin);
		wlPassword.setLayoutData(fdlPassword);
		Button wbTest =new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook( wbTest );
        wbTest.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				processClick(arg0);
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				processClick(arg0);
				
			}
			
			private void processClick(SelectionEvent evt){
			    String SERVER_URI = wProtocol.getText() + "://" + wHost.getText() + ":" + wPort.getText() + "/db/data";
			    String message = "";
			    try{
//				    RestAPI graphDb = new RestAPIFacade(SERVER_URI, wUsername.getText(), wPassword.getText());	    
//				    graphDb.getAllLabelNames();
					message = BaseMessages.getString(PKG, "Neo4JOutputDialog.ConnectionTest.Success");
			    }catch(Exception e){
					message = BaseMessages.getString(PKG, "Neo4JOutputDialog.ConnectionTest.Failed");
			    }
				ShowMessageDialog msgDialog = new ShowMessageDialog(shell, SWT.OK, BaseMessages.getString(PKG, "Neo4JOutputDialog.ConnectionTest.Title") , message);
				msgDialog.open();				
			}
        	
        });
        wbTest.setText(BaseMessages.getString(PKG, "System.Button.Test")); //$NON-NLS-1$
		FormData fdbTest = new FormData();
		fdbTest.right= new FormAttachment(100, 0);
		fdbTest.top  = new FormAttachment(wUsername, margin);
		wbTest.setLayoutData(fdbTest);
		wPassword=new TextVar(transMeta, shell, SWT.PASSWORD | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook( wPassword);
		wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.right= new FormAttachment(wbTest, -margin);
		fdPassword.top  = new FormAttachment(wUsername, margin);
		wPassword.setLayoutData(fdPassword);

		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, wTabFolder);
        
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		
		
		
		
		
		
		/**
		 * FROM
		 */
		// Labels 
		Label wlFromLabel = new Label(shell, SWT.RIGHT);
		wlFromLabel.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.LabelsField.FromLabel"));
		props.setLook(wlFromLabel);
		FormData fdlFromLabels = new FormData();
		fdlFromLabels.left = new FormAttachment(0, 0);
		fdlFromLabels.right = new FormAttachment(middle, -margin);
		fdlFromLabels.top = new FormAttachment(wPassword, margin);
		wlFromLabel.setLayoutData(fdlFromLabels);
		ColumnInfo[] fromLabelInf = new ColumnInfo[]{
			      new ColumnInfo( BaseMessages.getString(PKG, "Neo4JOutputDialog.FromLabelsTable.FromFields"), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNames ),
		};
		wFromLabelGrid =
			    new TableView( Variables.getADefaultVariableSpace(), shell, SWT.BORDER
			      | SWT.FULL_SELECTION | SWT.MULTI, fromLabelInf, 5, null, PropsUI.getInstance() );
		props.setLook(wFromLabelGrid);
		FormData fdFromLabelGrid = new FormData();
		fdFromLabelGrid.left = new FormAttachment( middle, 0 );
		fdFromLabelGrid.right = new FormAttachment( 100, 0);
		fdFromLabelGrid.top = new FormAttachment( wPassword, margin );
		wFromLabelGrid.setLayoutData( fdFromLabelGrid );
		
		// Node properties
		Label wlFromFields = new Label(shell, SWT.RIGHT);
		wlFromFields.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.FromFields.Properties"));
		props.setLook(wlFromFields);
		FormData fdlFromFields = new FormData();
		fdlFromFields.left = new FormAttachment(0,0);
		fdlFromFields.right = new FormAttachment(middle, -margin);
		fdlFromFields.top = new FormAttachment(wFromLabelGrid, margin);
		
		ColumnInfo[] colinf =
				    new ColumnInfo[] {
				      new ColumnInfo( BaseMessages.getString(PKG, "Neo4JOutputDialog.FromFieldsTable.FromPropFields"), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNames ),
				      };

		wFromPropsGrid =
				    new TableView( Variables.getADefaultVariableSpace(), shell, SWT.BORDER
				      | SWT.FULL_SELECTION | SWT.MULTI, colinf, 5, null, PropsUI.getInstance() );

		props.setLook(wFromPropsGrid);
		FormData fdFromPropsGrid = new FormData();
		fdFromPropsGrid.left = new FormAttachment( middle, 0 );
		fdFromPropsGrid.right = new FormAttachment( 100, 0);
		fdFromPropsGrid.top = new FormAttachment( wFromLabelGrid, margin );
		wFromPropsGrid.setLayoutData( fdFromPropsGrid );

		
		/**
		 * TO
		 */
		// Labels 
		Label wlToLabel = new Label(shell, SWT.RIGHT);
		wlToLabel.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.LabelsField.ToLabel"));
		props.setLook(wlToLabel);
		FormData fdlToLabels = new FormData();
		fdlToLabels.left = new FormAttachment(0, 0);
		fdlToLabels.right = new FormAttachment(middle, -margin);
		fdlToLabels.top = new FormAttachment(wFromPropsGrid, margin);
		wlToLabel.setLayoutData(fdlToLabels);
		ColumnInfo[] toLabelInf = new ColumnInfo[]{
			      new ColumnInfo( BaseMessages.getString(PKG, "Neo4JOutputDialog.ToLabelsTable.ToFields"), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNames ),
		};
		wToLabelGrid =
			    new TableView( Variables.getADefaultVariableSpace(), shell, SWT.BORDER
			      | SWT.FULL_SELECTION | SWT.MULTI, toLabelInf, 5, null, PropsUI.getInstance() );
		props.setLook(wToLabelGrid);
		FormData fdToLabelGrid = new FormData();
		fdToLabelGrid.left = new FormAttachment( middle, 0 );
		fdToLabelGrid.right = new FormAttachment( 100, 0);
		fdToLabelGrid.top = new FormAttachment( wFromPropsGrid, margin );
		wToLabelGrid.setLayoutData( fdToLabelGrid );
		
		// Node properties
		Label wlToFields = new Label(shell, SWT.RIGHT);
		wlToFields.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.ToFields.Properties"));
		props.setLook(wlToFields);
		FormData fdlToFields = new FormData();
		fdlToFields.left = new FormAttachment(0,0);
		fdlToFields.right = new FormAttachment(middle, -margin);
		fdlToFields.top = new FormAttachment(wToLabelGrid, margin);
		
		ColumnInfo[] toColinf =
				    new ColumnInfo[] {
				      new ColumnInfo( BaseMessages.getString(PKG, "Neo4JOutputDialog.ToFieldsTable.ToFields"), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNames ),
				      };

		wToPropsGrid =
				    new TableView( Variables.getADefaultVariableSpace(), shell, SWT.BORDER
				      | SWT.FULL_SELECTION | SWT.MULTI, toColinf, 5, null, PropsUI.getInstance() );

		props.setLook(wToPropsGrid);
		FormData fdToPropsGrid = new FormData();
		fdToPropsGrid.left = new FormAttachment( middle, 0 );
		fdToPropsGrid.right = new FormAttachment( 100, 0);
		fdToPropsGrid.top = new FormAttachment( wToLabelGrid, margin );
		wToPropsGrid.setLayoutData( fdToPropsGrid );
		
		

		/** 
		 * Relationships
		 */
		// Relationship properties 
		Label wlRelProps= new Label(shell, SWT.RIGHT);
		wlRelProps.setText(BaseMessages.getString(PKG, "Neo4JOutputDialog.RelationshipField.Label"));
		props.setLook(wlRelProps);
		FormData fdlRelProps = new FormData();
		fdlRelProps.left = new FormAttachment(0, 0);
		fdlRelProps.right = new FormAttachment(middle, -margin);
		fdlRelProps.top = new FormAttachment(wToPropsGrid, margin*5);
		wlRelProps.setLayoutData(fdlRelProps);
		ColumnInfo[] relPropsInf = new ColumnInfo[]{
			      new ColumnInfo( BaseMessages.getString(PKG, "Neo4JOutputDialog.RelPropsTable.PropertiesField"), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNames )
		};
		wRelPropsGrid =
			    new TableView( Variables.getADefaultVariableSpace(), shell, SWT.BORDER
			      | SWT.FULL_SELECTION | SWT.MULTI, relPropsInf, 5, null, PropsUI.getInstance() );
		props.setLook(wRelPropsGrid);
		FormData fdRelPropsGrid = new FormData();
		fdRelPropsGrid.left = new FormAttachment( middle, 0 );
		fdRelPropsGrid.right = new FormAttachment( 100, 0);
		fdRelPropsGrid.top = new FormAttachment( wToPropsGrid, margin *5);
		wRelPropsGrid.setLayoutData( fdRelPropsGrid );		

		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void getData(){
		wStepname.setText(stepname);
		wStepname.selectAll();
		if(input.getProtocol() != null){
			wProtocol.setText(input.getProtocol());
		}
		
		if(input.getHost() != null){
			wHost.setText(input.getHost());
		}
		
		if(input.getPort() != null){
			wPort.setText(input.getPort());
		}
		
		if(input.getUsername() != null){
			wUsername.setText(input.getUsername());
		}
		
		if(input.getPassword() != null){
			wPassword.setText(input.getPassword());
		}
		
//		if(input.getKey() != null){
//			wKey.setText(input.getKey());
//		}
		
		if(input.getFromNodeLabels() != null){
			String fromNodeLabels[] = input.getFromNodeLabels();
			
			for(int i=0; i < fromNodeLabels.length; i++){
				TableItem item = wFromLabelGrid.table.getItem(i);
				item.setText(1, fromNodeLabels[i]);
			}
		}
		
		
		if(input.getFromNodeProps() != null){
			String fromNodeProps[] = input.getFromNodeProps();
			
			for(int i=0; i < fromNodeProps.length; i++){
				TableItem item = wFromPropsGrid.table.getItem(i);
				item.setText(1, fromNodeProps[i]);
			}
		}
		
		
		if(input.getToNodeLabels() != null){
			String toNodeLabels[] = input.getToNodeLabels();
			
			for(int i=0; i < toNodeLabels.length; i++){
				TableItem item = wToLabelGrid.table.getItem(i);
				item.setText(1, toNodeLabels[i]);
			}
		}
		
		
		if(input.getToNodeProps() != null){
			String toNodeProps[] = input.getToNodeProps();
			
			for(int i=0; i < toNodeProps.length; i++){
				TableItem item = wToPropsGrid.table.getItem(i);
				item.setText(1, toNodeProps[i]);
			}
		}
		
		
//		if(input.getRelationships() != null){
//			String relationships[][] = input.getRelationships();
//			for(int i=0; i < relationships.length ; i++){
//				TableItem item = wRelationshipGrid.table.getItem(i);
//				item.setText(1, relationships[i][0]);
//				item.setText(2, relationships[i][1]);
//				item.setText(3, relationships[i][2]);
//				item.setText(4, relationships[i][3]);
//				item.setText(5, relationships[i][4]);
//			}
//		}
		
		if(input.getRelProps() != null){
			String relProps[] = input.getRelProps();
			
			for(int i=0; i < relProps.length; i++){
				TableItem item = wRelPropsGrid.table.getItem(i);
				item.setText(1, relProps[i]);
			}
		}

	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok(){
		stepname = wStepname.getText();
		input.setProtocol(wProtocol.getText());
		input.setHost(wHost.getText());
		input.setPort(wPort.getText());
		input.setUsername(wUsername.getText());
		input.setPassword(wPassword.getText());
		
//		input.setKey(wKey.getText());

		String fromNodeLabels[] = new String[wFromLabelGrid.nrNonEmpty()];
		for(int i=0; i < fromNodeLabels.length; i++){
			TableItem item = wFromLabelGrid.table.getItem(i);
			fromNodeLabels[i] = item.getText(1);
		}
		input.setFromNodeLabels(fromNodeLabels);
		
		String toNodeLabels[] = new String[wToLabelGrid.nrNonEmpty()];
		for(int i=0; i < toNodeLabels.length; i++){
			TableItem item = wToLabelGrid.table.getItem(i);
			toNodeLabels[i] = item.getText(1);
		}
		input.setToNodeLabels(toNodeLabels);

		
		String fromNodeProps[] = new String[wFromPropsGrid.nrNonEmpty()];
		for(int i=0; i < fromNodeProps.length; i++){
			TableItem item = wFromPropsGrid.table.getItem(i);
			fromNodeProps[i] = item.getText(1);
		}
		input.setFromNodeProps(fromNodeProps);

		
		String toNodeProps[] = new String[wToPropsGrid.nrNonEmpty()];
		for(int i=0; i < toNodeProps.length; i++){
			TableItem item = wToPropsGrid.table.getItem(i);
			toNodeProps[i] = item.getText(1);
		}
		input.setToNodeProps(toNodeProps);
		
		
		
		
		
		
//		String nodeLabels[] = new String[wLabelGrid.nrNonEmpty()];
//		for(int i=0; i < nodeLabels.length; i++){
//			TableItem item = wLabelGrid.table.getItem(i);
//			nodeLabels[i] = item.getText(1);
//		}
//		input.setLabels(nodeLabels);
		
//		String nodeProps[] = new String[wGrid.nrNonEmpty()];
//		for(int i=0; i < nodeProps.length; i++){
//			TableItem item = wGrid.table.getItem(i);
//			nodeProps[i] = item.getText(1);
//		}
//		input.setNodeProps(nodeProps);
		
//		String[][] relationships = new String[wRelationshipGrid.nrNonEmpty()][5];
//		for(int i=0; i<relationships.length; i++){
//			TableItem item = wRelationshipGrid.table.getItem(i);
//			String relationship[] = new String[5];
//			relationship[0] = item.getText(1); 
//			relationship[1] = item.getText(2); 
//			relationship[2] = item.getText(3);
//			relationship[3] = item.getText(4);
//			relationship[4] = item.getText(5);
//			relationships[i] = relationship;
//		}
//		input.setRelationships(relationships);
		
		String relProps[] = new String[wRelPropsGrid.nrNonEmpty()];
		for(int i=0; i < relProps.length; i++){
			TableItem item = wRelPropsGrid.table.getItem(i);
			relProps[i] = item.getText(1);
		}
		input.setRelProps(relProps);
		
		dispose();
	}
}
