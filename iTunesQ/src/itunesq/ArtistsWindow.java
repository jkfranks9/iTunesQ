package itunesq;

import java.io.IOException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display artists window.
 * 
 * @author Jon
 *
 */
public class ArtistsWindow
{

    // ---------------- Private variables -----------------------------------

    private Window artistsWindow = null;
    private Dialog altNamesDialog = null;
    private Skins skins = null;
    private Logger logger = null;

    /*
     * BXML variables.
     */
    @BXML private Border infoBorder = null;
    @BXML private FillPane infoFillPane = null;
    @BXML private Label numArtistsLabel = null;
    @BXML private Border artistsBorder = null;
    @BXML private TableView artistsTableView = null;
    @BXML private TableViewHeader artistsTableViewHeader = null;
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton doneButton = null;

    @BXML private Border altNamesPrimaryBorder = null;
    @BXML private TablePane altNamesTablePane = null;

    /**
     * Class constructor.
     */
    public ArtistsWindow()
    {

        /*
         * Create a UI logger.
         */
        String className = getClass().getSimpleName();
        logger = (Logger) LoggerFactory.getLogger(className + "_UI");

        /*
         * Get the logging object singleton.
         */
        Logging logging = Logging.getInstance();

        /*
         * Register our logger.
         */
        logging.registerLogger(Logging.Dimension.UI, logger);

        /*
         * Initialize variables.
         */
        skins = Skins.getInstance();

        logger.trace("ArtistsWindow constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the artists in a new window.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayArtists(Display display) throws IOException, SerializationException
    {
        logger.trace("displayArtists: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        /*
         * Get the show remote tracks preference.
         */
        Preferences prefs = Preferences.getInstance();
        boolean showRemoteTracks = prefs.getShowRemoteTracks();

        /*
         * Get the BXML information for the artists window, and generate the
         * list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers();

        /*
         * Set the number of artists label.
         */
        numArtistsLabel.setText(StringConstants.ARTISTS_NUM_ARTISTS + XMLHandler.getNumberOfArtists());

        /*
         * Create a list suitable for the setTableData() method.
         */
        List<HashMap<String, String>> displayArtists = new ArrayList<HashMap<String, String>>();

        /*
         * Now walk the artists, and add them all to the list.
         */
        Map<String, Artist> artists = XMLHandler.getArtists();
        for (String artistKey : artists)
        {
            Artist artistObj = XMLHandler.getArtists().get(artistKey.toLowerCase());

            /*
             * Skip artists with no local tracks if remote tracks are not being
             * shown.
             */
            if (artistObj.getNumLocalTracks() == 0 && showRemoteTracks == false)
            {
                continue;
            }

            HashMap<String, String> artistAttrs = artistObj.toDisplayMap();
            displayArtists.add(artistAttrs);
        }

        logger.info("found " + displayArtists.getLength() + " artists for display");

        /*
         * Create the appropriate column set.
         */
        if (showRemoteTracks == false)
        {
            ArtistDisplayColumns.createColumnSet(ArtistDisplayColumns.ColumnSet.LOCAL_VIEW, artistsTableView);
        }
        else
        {
            ArtistDisplayColumns.createColumnSet(ArtistDisplayColumns.ColumnSet.REMOTE_VIEW, artistsTableView);
        }

        /*
         * Add the artists to the window table view.
         */
        artistsTableView.setTableData(displayArtists);

        /*
         * Add a sort listener to allow column sorting.
         */
        artistsTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView)
            {
                List<Object> tableDataOfTableView = (List<Object>) tableView.getTableData();
                tableDataOfTableView.setComparator(new ITQTableViewRowComparator(tableView, logger));
            }
        });

        /*
         * Add widget texts.
         */
        doneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        artistsWindow.setTitle(Skins.Window.ARTISTS.getDisplayValue());

        /*
         * Register the artists window skin elements.
         */
        Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
        skins.registerWindowElements(Skins.Window.ARTISTS, windowElements);

        /*
         * Skin the artists window.
         */
        skins.skinMe(Skins.Window.ARTISTS);

        /*
         * Push the skinned window onto the skins window stack. It gets popped
         * from our done button press handler.
         */
        skins.pushSkinnedWindow(Skins.Window.ARTISTS);

        /*
         * Open the artists window.
         */
        logger.info("opening artists window");
        artistsWindow.open(display);
    }

    /**
     * Displays a dialog of alternate artist names when the user right clicks on
     * an artist name.
     * 
     * @param artistRowData row that was clicked in the table of artists
     * @param display display object for managing windows
     * @param owningWindow owning window on which to open the dialog
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void handleAltNamesPopup(Map<String, String> artistRowData, Display display, Window owningWindow)
            throws IOException, SerializationException
    {
        logger.trace("handleAltNamesPopup: " + this.hashCode());

        if (artistRowData == null)
        {
            throw new IllegalArgumentException("artistRowData argument is null");
        }

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        if (owningWindow == null)
        {
            throw new IllegalArgumentException("owningWindow argument is null");
        }

        /*
         * Get the artist name and log it.
         */
        String artistName = artistRowData.get(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue());
        logger.info("right clicked on artist '" + artistName + "'");

        /*
         * Get the artist alternate names we want to display.
         */
        Artist artistObj = XMLHandler.getArtists().get(artistName.toLowerCase());
        List<String> altNames = artistObj.getAltNames();

        /*
         * Only display the dialog if there is more than one alternate name.
         */
        if (altNames.getLength() > 1)
        {
            logger.info("found " + altNames.getLength() + " alternate artist names to display");

            /*
             * Get the base BXML information for the alternate names dialog, and
             * start the list of components to be skinned.
             */
            List<Component> components = new ArrayList<Component>();
            initializeAltNamesDialogBxmlVariables(components);

            /*
             * Build table rows to represent the alternate names. This method
             * also adds components that need to be skinned.
             */
            List<TablePane.Row> altNamesRows = buildAltNamesRows(altNames, components);

            /*
             * Add the generated rows to the owning table pane.
             */
            for (TablePane.Row detailRow : altNamesRows)
            {
                altNamesTablePane.getRows().add(detailRow);
            }

            /*
             * Set the window title.
             */
            altNamesDialog.setTitle(Skins.Window.ALTNAMES.getDisplayValue());

            /*
             * Register the window elements.
             */
            Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
            skins.registerWindowElements(Skins.Window.ALTNAMES, windowElements);

            /*
             * Skin the alternate names dialog.
             */
            skins.skinMe(Skins.Window.ALTNAMES);

            /*
             * Open the alternate names dialog. There is no close button, so the
             * user has to close the dialog using the host controls.
             */
            logger.info("opening alternate names dialog");
            altNamesDialog.open(display, owningWindow);
        }
        else
        {
            Alert.alert(MessageType.INFO, StringConstants.ALERT_NO_ALTERNATE_NAMES, artistsWindow);
        }
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers()
    {
        logger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the done button press.
         */
        doneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("done button pressed");

                /*
                 * Close the window.
                 */
                artistsWindow.close();

                /*
                 * Pop the window off the skins window stack.
                 */
                skins.popSkinnedWindow();
            }
        });

        /*
         * Mouse click listener for the table view.
         */
        artistsTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {

                /*
                 * For a right mouse click we pop up a dialog of alternate
                 * names.
                 */
                if (button == Mouse.Button.RIGHT)
                {
                    TableView table = (TableView) component;
                    Display display = component.getDisplay();

                    /*
                     * Get the index for the clicked row, then set that row as
                     * selected.
                     */
                    int index = table.getRowAt(y);
                    table.setSelectedIndex(index);

                    /*
                     * Get the data for the selected row.
                     */
                    @SuppressWarnings("unchecked") HashMap<String, String> selectedTrackRowData = (HashMap<String, String>) table
                            .getSelectedRow();

                    /*
                     * Create and open the alternate names popup dialog.
                     */
                    try
                    {
                        handleAltNamesPopup(selectedTrackRowData, display, artistsWindow);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(logger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }

                return false;
            }
        });
    }

    /*
     * Build the alternate names data for display.
     */
    private List<TablePane.Row> buildAltNamesRows(List<String> altNames, List<Component> components)
    {
        logger.trace("buildAltNamesRows: " + this.hashCode());

        List<TablePane.Row> result = new ArrayList<TablePane.Row>();

        /*
         * Build a row for all alternate names.
         */
        for (String altName : altNames)
        {

            /*
             * Start building a table row to be returned.
             */
            TablePane.Row infoRow = new TablePane.Row();
            infoRow.setHeight("1*");

            /*
             * Build the label that contains the actual alternate name.
             */
            Label altNameLabel = new Label(altName);
            Map<String, Object> altNameLabelStyles = new HashMap<String, Object>();
            altNameLabelStyles.put("padding", 0);
            altNameLabel.setStyles(altNameLabelStyles);

            /*
             * Add the label to the table row.
             */
            infoRow.add(altNameLabel);

            /*
             * Add the components we created so they can be skinned.
             */
            components.add(altNameLabel);

            /*
             * Add the table row to the result.
             */
            result.add(infoRow);
        }

        return result;
    }

    /*
     * Initialize artists window BXML variables and collect the list of
     * components to be skinned.
     */
    private void initializeBxmlVariables(List<Component> components) throws IOException, SerializationException
    {
        logger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        artistsWindow = (Window) windowSerializer.readObject(getClass().getResource("artistsWindow.bxml"));

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars) artistsWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        infoBorder = (Border) windowSerializer.getNamespace().get("infoBorder");
        components.add(infoBorder);
        infoFillPane = (FillPane) windowSerializer.getNamespace().get("infoFillPane");
        components.add(infoFillPane);
        numArtistsLabel = (Label) windowSerializer.getNamespace().get("numArtistsLabel");
        components.add(numArtistsLabel);
        artistsBorder = (Border) windowSerializer.getNamespace().get("artistsBorder");
        components.add(artistsBorder);
        artistsTableView = (TableView) windowSerializer.getNamespace().get("artistsTableView");
        components.add(artistsTableView);
        artistsTableViewHeader = (TableViewHeader) windowSerializer.getNamespace().get("artistsTableViewHeader");
        components.add(artistsTableViewHeader);
        actionBorder = (Border) windowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = (BoxPane) windowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);

        doneButton = (PushButton) windowSerializer.getNamespace().get("doneButton");
        components.add(doneButton);
    }

    /*
     * Initialize alternate names dialog BXML variables and collect the static
     * components to be skinned.
     */
    private void initializeAltNamesDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        logger.trace("initializeAltNamesDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        altNamesDialog = (Dialog) dialogSerializer.readObject(getClass().getResource("artistAltNamesWindow.bxml"));

        altNamesPrimaryBorder = (Border) dialogSerializer.getNamespace().get("altNamesPrimaryBorder");
        components.add(altNamesPrimaryBorder);
        altNamesTablePane = (TablePane) dialogSerializer.getNamespace().get("altNamesTablePane");
        components.add(altNamesTablePane);
    }
}
