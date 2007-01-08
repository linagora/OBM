/**
 *
 */
package fr.aliasource.funambol.admin;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;

import com.funambol.admin.AdminException;
import com.funambol.admin.ui.SourceManagementPanel;
import com.funambol.framework.engine.source.ContentType;
import com.funambol.framework.engine.source.SyncSourceInfo;

import fr.aliasource.funambol.engine.source.CalendarSyncSource;
import fr.aliasource.funambol.engine.source.ContactSyncSource;
import fr.aliasource.funambol.engine.source.ObmSyncSource;
import fr.aliasource.funambol.utils.Helper;

/**
 * 
 */
public class ObmSyncSourceConfigPanel
	extends SourceManagementPanel
	implements Serializable {

    // --------------------------------------------------------------- Constants

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Allowed characters for name and uri
     */
    public static final String NAME_ALLOWED_CHARS
    = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_.";
    
    public static final String VCARD_TYPE     = "text/x-vcard";
    public static final String VCARD_TYPES    = "text/x-vcard,text/vcard";
    public static final String VCARD_VERSIONS = "2.1, 3.0";
    public static final String ICAL_TYPE      = "text/x-vcalendar";
    public static final String ICAL_TYPES     = "text/x-vcalendar";
    public static final String ICAL_VERSIONS  = "1.0";
    public static final String SIFC_TYPE      = "text/x-s4j-sifc";
    public static final String SIFC_TYPES     = "text/x-s4j-sifc";
    public static final String SIFC_VERSIONS  = "1.0";
    public static final String SIFE_TYPE      = "text/x-s4j-sife";
    public static final String SIFE_TYPES     = "text/x-s4j-sife";
    public static final String SIFE_VERSIONS  = "1.0";
    public static final String SIFN_TYPE      = "text/x-s4j-sifn";
    public static final String SIFN_TYPES     = "text/x-s4j-sifn";
    public static final String SIFN_VERSIONS  = "1.0";
    public static final String SIFT_TYPE      = "text/x-s4j-sift";
    public static final String SIFT_TYPES     = "text/x-s4j-sift";
    public static final String SIFT_VERSIONS  = "1.0";

    // ------------------------------------------------------------ Private data
    /** label for the panel's name */
    private JLabel panelName = new JLabel();

    /** border to evidence the title of the panel */
    private TitledBorder  titledBorder1;

    private JLabel		nameLabel       = new JLabel() ;
    private JTextField	nameValue       = new JTextField() ;
    private JLabel		typeLabel       = new JLabel() ;
    private JComboBox	typeValue		= new JComboBox() ;
    private JLabel		sourceUriLabel	= new JLabel() ;
    private JTextField	sourceUriValue	= new JTextField() ;
    private JLabel		restrictPrivateLabel	= new JLabel() ;
    private JCheckBox	restrictPrivateValue	= new JCheckBox();
    private JLabel		restrictOwnerLabel		= new JLabel() ;
    private	JCheckBox	restrictOwnerValue		= new JCheckBox(); 
    
    private JButton     confirmButton   = new JButton() ;

    private ObmSyncSource  syncSource   = null ;

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new DummySyncSourceConfigPanel instance
     */
    public ObmSyncSourceConfigPanel() {
        init();
    }

    // ----------------------------------------------------------- Private methods

    /**
     * Create the panel
     * @throws Exception if error occures during creation of the panel
     */
    private void init(){
        // set layout
        this.setLayout(null);

        // set properties of label, position and border
        //  referred to the title of the panel
        titledBorder1 = new TitledBorder("");

        panelName.setFont(titlePanelFont);
        panelName.setText("Edit OBM SyncSource");
        panelName.setBounds(new Rectangle(14, 5, 316, 28));
        panelName.setAlignmentX(SwingConstants.CENTER);
        panelName.setBorder(titledBorder1);

        sourceUriLabel.setText("Source URI: ");
        sourceUriLabel.setFont(defaultFont);
        sourceUriLabel.setBounds(new Rectangle(14, 60, 150, 18));
        sourceUriValue.setFont(defaultFont);
        sourceUriValue.setBounds(new Rectangle(170, 60, 350, 18));

        nameLabel.setText("Name: ");
        nameLabel.setFont(defaultFont);
        nameLabel.setBounds(new Rectangle(14, 90, 150, 18));
        nameValue.setFont(defaultFont);
        nameValue.setBounds(new Rectangle(170, 90, 350, 18));

        typeLabel.setText("Type: ");
        typeLabel.setFont(defaultFont);
        typeLabel.setBounds(new Rectangle(14, 120, 150, 18));
        typeValue.setFont(defaultFont);
        typeValue.setBounds(new Rectangle(170, 120, 350, 18));
        
        
        restrictPrivateLabel.setText("No private: ");
        restrictPrivateLabel.setFont(defaultFont);
        restrictPrivateLabel.setBounds(new Rectangle(12,150,150,18));
        restrictPrivateValue.setFont(defaultFont);
        restrictPrivateValue.setBounds(new Rectangle(170,150,350,18));
        
        
        restrictOwnerLabel.setText("Only owner: ");
        restrictOwnerLabel.setFont(defaultFont);
        restrictOwnerLabel.setBounds(new Rectangle(12,180,150,18));
        restrictOwnerValue.setFont(defaultFont);
        restrictOwnerValue.setBounds(new Rectangle(170,180,350,18));
       
        
        confirmButton.setFont(defaultFont);
        confirmButton.setText("Add");
        confirmButton.setBounds(170, 300, 70, 25);

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event ) {
                try {
                    validateValues();
                    getValues();
                    if (getState() == STATE_INSERT) {
                        ObmSyncSourceConfigPanel.this.actionPerformed(new ActionEvent(ObmSyncSourceConfigPanel.this, ACTION_EVENT_INSERT, event.getActionCommand()));
                    } else {
                        ObmSyncSourceConfigPanel.this.actionPerformed(new ActionEvent(ObmSyncSourceConfigPanel.this, ACTION_EVENT_UPDATE, event.getActionCommand()));
                    }
                } catch (Exception e) {
                    notifyError(new AdminException(e.getMessage()));
                }
            }
        });

        // add all components to the panel
        this.add(panelName      , null);
        this.add(nameLabel      , null);
        this.add(nameValue      , null);
        this.add(typeLabel      , null);
        this.add(typeValue      , null);
        this.add(sourceUriLabel , null);
        this.add(sourceUriValue , null);
        
        this.add(restrictPrivateLabel, null);
        this.add(restrictPrivateValue, null);
        this.add(restrictOwnerLabel	, null);
        this.add(restrictOwnerValue, null);
        
        this.add(confirmButton  , null);
        

    }

    /**
     * Loads the given syncSource showing the name, uri and type in the panel's
     * fields.
     *
     * @param syncSource the SyncSource instance
     */
    public void updateForm() {
         if (!(getSyncSource() instanceof ObmSyncSource)) {
          notifyError(
              new AdminException(
                  "This is not an ObmSyncSource."
              )
          );
          return;
        }
        if (getState() == STATE_INSERT) {
          confirmButton.setText("Add");
        } else if (getState() == STATE_UPDATE) {
          confirmButton.setText("Save");
        }

        this.syncSource = (ObmSyncSource) getSyncSource();

        sourceUriValue.setText(syncSource.getSourceURI() );
        nameValue.setText     (syncSource.getName()      );
        
        typeValue.removeAllItems();
        if (syncSource instanceof CalendarSyncSource) {
            typeValue.addItem("SIF-E");
            typeValue.addItem("iCal");
        } else if (syncSource instanceof ContactSyncSource) {
            typeValue.addItem("SIF-C");
            typeValue.addItem("vCard");
        }

        if ((syncSource instanceof ContactSyncSource) ||
            (syncSource instanceof CalendarSyncSource)) {

            String cType = ((ObmSyncSource) syncSource).getType();

            if (VCARD_TYPE.equals(cType) || ICAL_TYPE.equals(cType)) {
                typeValue.setSelectedIndex(1);
            } else {
                typeValue.setSelectedIndex(0);
            }
        }
        
        if (this.syncSource.getSourceURI() != null) {
            sourceUriValue.setEditable(false);
        }
        
        //restrictions
        int rs = syncSource.getRestrictions();
        
        restrictPrivateValue.setSelected(
        		(rs & Helper.RESTRICT_PRIVATE) == Helper.RESTRICT_PRIVATE );
        restrictOwnerValue.setSelected(
        		(rs & Helper.RESTRICT_OWNER) == Helper.RESTRICT_OWNER );
    }

 // ----------------------------------------------------------- Private methods
    /**
     * Checks if the values provided by the user are all valid. In caso of errors,
     * a IllegalArgumentException is thrown.
     */
    private void validateValues() throws IllegalArgumentException {
        String value = null;

        value = nameValue.getText();
        if (StringUtils.isEmpty(value)) {
            throw new
            IllegalArgumentException(
            "Field 'Name' cannot be empty. Please provide a SyncSource name.");
        }

        if (!StringUtils.containsOnly(value, NAME_ALLOWED_CHARS.toCharArray())) {
            throw new
            IllegalArgumentException(
            "Only the following characters are allowed for field 'Name': \n" + NAME_ALLOWED_CHARS);
        }

        value = sourceUriValue.getText();
        if (StringUtils.isEmpty(value)) {
            throw new
            IllegalArgumentException(
            "Field 'Source URI' cannot be empty. Please provide a SyncSource URI.");
        }
    }

    /**
     * Set syncSource properties with the values provided by the user.
     */
    private void getValues() {
    	
    	syncSource = (ObmSyncSource)getSyncSource();
    	
    	syncSource.setSourceURI(sourceUriValue.getText().trim());
        syncSource.setName     (nameValue.getText().trim()     );
        StringTokenizer types    = null;
        StringTokenizer versions = null;

        if (typeValue.getSelectedIndex() == 0) {

        	syncSource.setEncode (true) ;
            if (syncSource instanceof CalendarSyncSource)        {
                syncSource.setType(SIFE_TYPE);
                types = new StringTokenizer(SIFE_TYPES       , "," ) ;
                versions = new StringTokenizer(SIFE_VERSIONS , "," ) ;
            } else if (syncSource instanceof ContactSyncSource ) {
                syncSource.setType(SIFC_TYPE);
                types = new StringTokenizer(SIFC_TYPES       , "," ) ;
                versions = new StringTokenizer(SIFC_VERSIONS , "," ) ;
            }

        } else if (syncSource instanceof CalendarSyncSource)     {
            syncSource.setEncode (false) ;
            syncSource.setType(ICAL_TYPE);
            types = new StringTokenizer(ICAL_TYPES       , "," ) ;
            versions = new StringTokenizer(ICAL_VERSIONS , "," ) ;
        } else if (syncSource instanceof ContactSyncSource )     {
            syncSource.setEncode (false ) ;
            syncSource.setType(VCARD_TYPE);
            types = new StringTokenizer(VCARD_TYPES       , "," ) ;
            versions = new StringTokenizer(VCARD_VERSIONS , "," ) ;
        }

        ContentType[] contentTypes= new ContentType[types.countTokens()];

        for(int i = 0, l = contentTypes.length; i < l; ++i) {
          contentTypes[i] = new ContentType(types.nextToken().trim()    ,
                                            versions.nextToken().trim() );
        }

        syncSource.setInfo(new SyncSourceInfo(contentTypes, 0));
        
        int rs = 0;
        if (restrictPrivateValue.isSelected()) {
        	rs += Helper.RESTRICT_PRIVATE;
        }
        if (restrictOwnerValue.isSelected()) {
        	rs += Helper.RESTRICT_OWNER;
        }
        syncSource.setRestrictions(rs);
    }

}
