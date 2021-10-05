package itunesq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the file save dialog. This dialog is available from the
 * File {@literal ->} Save menu on a page showing the results of a track or
 * playlist query, or a list of duplicate tracks. This dialog allows the user to
 * save or print the query results.
 * 
 * @author Jon
 *
 */
public class FileSaveDialog
{

    // ---------------- Private variables -----------------------------------

    private static final int OUTPUT_WIDTH = 120;
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private Dialog fileSaveDialog = null;
    private MenuBars owningWindow = null;
    private String saveFileName = null;
    private boolean limitPlaylists = false;
    private boolean printResults = false;
    private boolean doneHandlerExecuted = false;

    private Logger logger = null;

    /*
     * Window attributes.
     */
    private TracksWindow tracksWindowHandler = null;
    private QueryPlaylistsWindow playlistsWindowHandler = null;
    private ListQueryType.Type queryType = null;
    private String queryStr = null;
    private List<String> columnNames = null;

    /*
     * BXML variables.
     */
    @BXML private TablePane fileSaveTablePane = null;
    @BXML private Border fileSaveDetailsBorder = null;
    @BXML private BoxPane fileSaveDetailsBoxPane = null;
    @BXML private Separator fileSaveDetailsFileSeparator = null;
    @BXML private Label fileSaveDetailsLabel = null;
    @BXML private TextInput fileSaveDetailsTextInput = null;
    @BXML private Separator fileSaveDetailsPrintSeparator = null;
    @BXML private Checkbox fileSaveDetailsPrintCheckbox = null;
    @BXML private Separator fileSaveDetailsOptionsSeparator = null;
    @BXML private Checkbox fileSaveDetailsLimitCheckbox = null;
    @BXML private Border fileSaveButtonBorder = null;
    @BXML private BoxPane fileSaveButtonBoxPane = null;
    @BXML private PushButton fileSaveDoneButton = null;

    /**
     * Class constructor.
     * 
     * @param owner owning window. This dialog is modal over the window.
     */
    @SuppressWarnings("unchecked")
    public FileSaveDialog(MenuBars owner)
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
        owningWindow = owner;

        /*
         * Get the window attributes. The handler is an instance of either the TracksWindow or
         * QueryPlaylistsWindow, based on the query type.
         */
        queryType = (ListQueryType.Type) owningWindow.getAttribute(MenuBars.WindowAttributes.QUERY_TYPE);
        queryStr = (String) owningWindow.getAttribute(MenuBars.WindowAttributes.QUERY_STRING);
        columnNames = (List<String>) owningWindow.getAttribute(MenuBars.WindowAttributes.COLUMN_NAMES);
        
        if (queryType == ListQueryType.Type.PLAYLIST_FAMILY)
        {
            playlistsWindowHandler = 
                    (QueryPlaylistsWindow) owningWindow.getAttribute(MenuBars.WindowAttributes.HANDLER);
        }
        else
        {
            tracksWindowHandler = 
                    (TracksWindow) owningWindow.getAttribute(MenuBars.WindowAttributes.HANDLER);
        }

        logger.trace("FileSaveDialog constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the file save dialog.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayFileSaveDialog(Display display) 
            throws IOException, SerializationException
    {
        logger.trace("displayFileSaveDialog: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        /*
         * Get the BXML information for the dialog, and gather the list of
         * components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Listener to handle the done button press. We only collect entered
         * information here. It's processed when the dialog close event occurs.
         */
        fileSaveDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("done button pressed");

                if (fileSaveDetailsPrintCheckbox.isSelected() == true)
                {
                    printResults = true;
                }
                else
                {
                    saveFileName = fileSaveDetailsTextInput.getText();
                    
                    /*
                     * Yell if the file is a directory or is not writable.
                     */
                    Path saveFilePath = Paths.get(saveFileName);
                    if (Files.isDirectory(saveFilePath))
                    {
                        Alert.alert(MessageType.INFO, StringConstants.ALERT_FILE_IS_DIRECTORY, owningWindow);
                    }
                    else if (Files.exists(saveFilePath) && !Files.isWritable(saveFilePath))
                    {
                        Alert.alert(MessageType.INFO, StringConstants.ALERT_FILE_NOT_WRITABLE, owningWindow);
                    }
                    else
                    {

                        /*
                         * Remove trailing slash if present.
                         */
                        if (saveFileName != null && saveFileName.endsWith("/"))
                        {
                            String correctedDirectory = saveFileName.substring(0, saveFileName.length() - 2);
                            saveFileName = correctedDirectory;
                        }

                        /*
                         * Get the limit flag.
                         */
                        limitPlaylists = fileSaveDetailsLimitCheckbox.isSelected();
                        
                        /*
                         * Indicate that this handler has executed. This prevents the close listener
                         * from doing anything if the user closes the dialog using the host controls.
                         */
                        doneHandlerExecuted = true;

                        fileSaveDialog.close();
                    }
                }
            }
        });

        /*
         * Initialize the limit checkbox and tooltip based on the type of query.
         */
        switch (queryType)
        {
        case TRACK_QUERY:
        case TRACK_DUPLICATES:
        case TRACK_FAMILY:
            fileSaveDetailsLimitCheckbox.setButtonData(StringConstants.FILESAVE_TRACKS_LIMIT);
            fileSaveDetailsLimitCheckbox.setTooltipText(StringConstants.FILESAVE_TRACKS_LIMIT_TIP);
            fileSaveDetailsLimitCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
            break;

        case TRACK_COMPARE:
            fileSaveDetailsLimitCheckbox.setButtonData(StringConstants.FILESAVE_PLAYLISTS_LIMIT);
            fileSaveDetailsLimitCheckbox.setTooltipText(StringConstants.FILESAVE_PLAYLISTS_LIMIT_TIP);
            fileSaveDetailsLimitCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
            break;
            
        case PLAYLIST_FAMILY:
            fileSaveDetailsOptionsSeparator.setVisible(false);
            fileSaveDetailsLimitCheckbox.setVisible(false);
            break;

        default:
            throw new InternalErrorException(true, "unknown query type '" + queryType + "'");
        }

        /*
         * Set the width of the dialog.
         */
        fileSaveTablePane.setPreferredWidth(InternalConstants.FILE_SAVE_DIALOG_WIDTH);

        /*
         * Set the size of the text input for the file name, and populate it
         * with the user's home directory.
         * 
         * NOTE: It would be nice to use a file chooser dialog to make it easier
         * to select a file, but Pivot's does not let you create a new file, and
         * Java's file chooser requires java.awt.Component in order to open the
         * dialog, which Pivot does not use.
         */
        fileSaveDetailsTextInput.setTextSize(InternalConstants.FILE_SAVE_FILENAME_TEXT_SIZE);
        fileSaveDetailsTextInput.setText(System.getProperty("user.home"));

        /*
         * Add widget texts.
         */
        fileSaveDetailsFileSeparator.setHeading(StringConstants.FILESAVE_SAVE_TO_FILE);
        fileSaveDetailsLabel.setTooltipText(StringConstants.FILESAVE_NAME_TIP);
        fileSaveDetailsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        fileSaveDetailsTextInput.setTooltipText(StringConstants.FILESAVE_NAME_TIP);
        fileSaveDetailsTextInput.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        fileSaveDetailsPrintSeparator.setHeading(StringConstants.FILESAVE_SAVE_TO_PRINTER);
        fileSaveDetailsPrintCheckbox.setTooltipText(StringConstants.FILESAVE_PRINT_TIP);
        fileSaveDetailsPrintCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        fileSaveDetailsLabel.setText(StringConstants.FILESAVE_ENTER_FILE_NAME);
        fileSaveDetailsPrintCheckbox.setButtonData(StringConstants.FILESAVE_SAVE_TO_PRINTER);
        fileSaveDetailsOptionsSeparator.setHeading(StringConstants.FILESAVE_OPTIONS);
        fileSaveDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        fileSaveDialog.setTitle(Skins.Window.FILE_SAVE.getDisplayValue());

        /*
         * Get the skins singleton.
         */
        Skins skins = Skins.getInstance();

        /*
         * Register the file save dialog skin elements.
         */
        skins.registerWindowElements(Skins.Window.FILE_SAVE, components);

        /*
         * Skin the file save dialog.
         */
        skins.skinMe(Skins.Window.FILE_SAVE);

        /*
         * Open the file save dialog.
         * 
         * NOTE: Because this dialog contains a focusable component (a
         * TextInput), focus is shifted to that component when the dialog opens.
         * This results in the dialog title bar being grayed out. I can find no
         * way to fix this.
         */
        logger.info("opening file save dialog");
        CloseAdapter closeAdapter = new CloseAdapter();
        fileSaveDialog.open(display, owningWindow, closeAdapter);
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Generate the track list as a string.
     */
    private String generateTracksOutput()
    {
        logger.trace("generateTracksOutput: " + this.hashCode());

        StringBuilder output = new StringBuilder();

        /*
         * Get the list of tracks resulting from the query.
         */
        List<HashMap<String, String>> tracks = tracksWindowHandler.getFilteredTrackData();

        /*
         * Generate the file prolog ...
         */

        /*
         * ... header line.
         */
        output.append(StringConstants.FILESAVE_HEADER);

        /*
         * ... time stamp.
         */
        String timeStamp = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm:ss a").format(new Date());
        output.append(timeStamp);
        output.append(LINE_SEPARATOR);

        /*
         * ... query string.
         */
        output.append(queryStr);

        /*
         * ... separator and spacer lines.
         */
        String line = new String(new char[OUTPUT_WIDTH]).replace("\0", "-");
        String separator = LINE_SEPARATOR + LINE_SEPARATOR + line;
        output.append(separator);
        output.append(LINE_SEPARATOR + LINE_SEPARATOR);

        /*
         * Write all the track data.
         */
        int trackNum = 0;
        for (HashMap<String, String> trackData : tracks)
        {

            /*
             * Spacer line between tracks.
             */
            if (trackNum != 0)
            {
                output.append(LINE_SEPARATOR);
            }

            StringBuilder trackStr = new StringBuilder();

            /*
             * 4 digit line number for each track.
             */
            trackStr.append(String.format("%4d", ++trackNum) + ") ");

            /*
             * Track attributes, according to the list of column names.
             */
            int columnNamesLen = columnNames.getLength();
            
            for (int i = 0; i < columnNamesLen; i++)
            {
                String columnName = columnNames.get(i);
                String columnHeader = TrackDisplayColumns.ColumnNames.getEnum(columnName).getHeaderValue();

                /*
                 * Append the column header and associated track data.
                 */
                trackStr.append(columnHeader + "=");
                trackStr.append(trackData.get(columnName));

                /*
                 * Append a separator between fields, or a line separator for
                 * the last field.
                 */
                if (i < columnNamesLen - 1)
                {
                    trackStr.append(", ");
                }
                else
                {
                    trackStr.append(LINE_SEPARATOR + "      ");
                }
            }

            /*
             * Playlists, on a separate line.
             */
            trackStr.append(PlaylistDisplayColumns.ColumnNames.PLAYLIST_NAMES.getHeaderValue() + "=");

            /*
             * Walk through all playlists.
             */
            String playlistNames = 
                    trackData.get(PlaylistDisplayColumns.ColumnNames.PLAYLIST_NAMES.getNameValue());
            String[] playlists = playlistNames.split(InternalConstants.LIST_ITEM_SEPARATOR);
            boolean appendedPlaylist = false;

            for (int i = 0; i < playlists.length; i++)
            {
                if (limitPlaylists == true && limitPlaylist(playlists[i]) == true)
                {
                    continue;
                }

                if (appendedPlaylist == true)
                {
                    trackStr.append(", ");
                }
                trackStr.append(playlists[i]);
                appendedPlaylist = true;
            }

            output.append(trackStr.toString());
        }

        return output.toString();
    }

    /*
     * Generate the playlist list as a string.
     */
    private String generatePlaylistsOutput()
    {
        logger.trace("generatePlaylistsOutput: " + this.hashCode());

        StringBuilder output = new StringBuilder();

        /*
         * Get the list of playlists resulting from the family expansion.
         */
        List<HashMap<String, String>> playlists = playlistsWindowHandler.getFamilyPlaylistData();

        /*
         * Generate the file prolog ...
         */

        /*
         * ... header line.
         */
        output.append(StringConstants.FILESAVE_HEADER);

        /*
         * ... time stamp.
         */
        String timeStamp = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm:ss a").format(new Date());
        output.append(timeStamp);
        output.append(LINE_SEPARATOR);

        /*
         * ... query string.
         */
        output.append(queryStr);

        /*
         * ... separator and spacer lines.
         */
        String line = new String(new char[OUTPUT_WIDTH]).replace("\0", "-");
        String separator = LINE_SEPARATOR + LINE_SEPARATOR + line;
        output.append(separator);
        output.append(LINE_SEPARATOR + LINE_SEPARATOR);

        /*
         * Write all the playlist data.
         */
        int playlistNum = 0;
        for (HashMap<String, String> playlistData : playlists)
        {

            /*
             * Spacer line between playlists.
             */
            if (playlistNum != 0)
            {
                output.append(LINE_SEPARATOR);
            }

            StringBuilder playlistStr = new StringBuilder();

            /*
             * 4 digit line number for each playlist.
             */
            playlistStr.append(String.format("%4d", ++playlistNum) + ") ");

            /*
             * Playlist attributes, according to the list of column names.
             */
            int columnNamesLen = columnNames.getLength();
            
            for (int i = 0; i < columnNamesLen; i++)
            {
                String columnName = columnNames.get(i);
                String columnHeader = PlaylistDisplayColumns.ColumnNames.getEnum(columnName).getHeaderValue();

                /*
                 * Append the column header and associated playlist data.
                 */
                playlistStr.append(columnHeader + "=");
                playlistStr.append(playlistData.get(columnName));

                /*
                 * Append a separator between fields, or a line separator for
                 * the last field.
                 */
                if (i < columnNamesLen - 1)
                {
                    playlistStr.append(", ");
                }
                else
                {
                    playlistStr.append(LINE_SEPARATOR + "      ");
                }
            }

            /*
             * Tracks, on a separate line.
             */
            playlistStr.append(PlaylistDisplayColumns.ColumnNames.TRACK_NAMES.getHeaderValue() + "=");

            /*
             * Walk through all tracks.
             */
            String trackNames = 
                    playlistData.get(PlaylistDisplayColumns.ColumnNames.TRACK_NAMES.getNameValue());
            String[] tracks = trackNames.split(InternalConstants.LIST_ITEM_SEPARATOR);
            boolean appendedTrack = false;

            for (int i = 0; i < tracks.length; i++)
            {
                if (appendedTrack == true)
                {
                    playlistStr.append(", ");
                }
                playlistStr.append(tracks[i]);
                appendedTrack = true;
            }

            output.append(playlistStr.toString());
        }

        return output.toString();
    }

    /*
     * Save the generated track list output to a file or printer.
     */
    private void saveOutput(String output) 
            throws IOException
    {
        logger.trace("saveOutput: " + this.hashCode());

        if (printResults == true)
        {
            printOutput(output);
        }
        else if (saveFileName != null && !saveFileName.isEmpty())
        {
            writeTextFile(saveFileName, output);
        }
        else
        {
            Alert.alert(MessageType.INFO, StringConstants.ALERT_NOTHING_SAVED, owningWindow);
        }
    }

    /*
     * Write the track list as a text file.
     */
    private void writeTextFile(String filename, String output) 
            throws IOException
    {
        logger.trace("writeTextFile: " + this.hashCode());

        Path path = Paths.get(filename);
        Files.write(path, output.getBytes());
    }

    /*
     * Print the track list.
     */
    private void printOutput(String output)
    {
        logger.trace("printOutput: " + this.hashCode());

        /*
         * The print methods require an input stream
         */
        ByteArrayInputStream textStream = new ByteArrayInputStream(output.getBytes());

        /*
         * Build a simple document from the input stream.
         */
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc mydoc = new SimpleDoc(textStream, flavor, null);

        /*
         * Look up all print services, and get the default service.
         */
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        /*
         * Create printer attributes.
         */
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(Sides.DUPLEX);

        DocPrintJob job = null;

        /*
         * If there are no services we might still have a default. Don't ask me
         * why, but I stole this code from someone else.
         */
        if (services.length == 0)
        {
            if (defaultService == null)
            {
                logger.warn("no print service found");
                Alert.alert(MessageType.ERROR, StringConstants.ALERT_NO_PRINTER, owningWindow);
            }

            /*
             * Create a print job using the default service.
             */
            else
            {
                logger.info("creating print job for default service '" + defaultService.getName() + "'");
                job = defaultService.createPrintJob();
            }
        }

        /*
         * At least one service was found, so put up a dialog to let the user
         * play with settings.
         */
        else
        {

            /*
             * Get our base X and Y coordinates, for the print dialog.
             */
            java.awt.Window hostWindow = owningWindow.getDisplay().getHostWindow();
            int xcoord = hostWindow.getX();
            int ycoord = hostWindow.getY();

            /*
             * Display the print dialog.
             */
            PrintService service = ServiceUI.printDialog(null, xcoord, ycoord, services, defaultService, flavor,
                    attributes);

            /*
             * If a print service was returned from the dialog, create a print
             * job.
             */
            if (service != null)
            {
                logger.info("creating print job for service '" + service.getName() + "'");
                job = service.createPrintJob();
            }
            else
            {
                logger.warn("no print service returned from the print dialog");
            }
        }

        /*
         * If we have a print job, try to print. If it fails, just alert the
         * user.
         */
        if (job != null)
        {
            try
            {
                job.print(mydoc, attributes);
            }
            catch (PrintException e)
            {
                logger.error("caught " + e.getClass().getSimpleName(), e);
                Alert.alert(MessageType.ERROR, StringConstants.ALERT_PRINT_FAILED, owningWindow);
            }
        }
    }

    /*
     * Determine if a given playlist should be excluded from the list of
     * playlists for a track.
     */
    private boolean limitPlaylist(String playlistName)
    {
        boolean result = false;

        switch (queryType)
        {

        /*
         * For a tracks query, the question is whether or not the input playlist
         * is bypassed. So get the playlist object and ask it.
         */
        case TRACK_QUERY:
        case TRACK_DUPLICATES:
        case TRACK_FAMILY:
            String playlistID = Database.getPlaylistsMap().get(playlistName);
            Playlist playlist = Database.getPlaylists().get(playlistID);
            result = playlist.getBypassed();
            break;

        /*
         * For a playlists query, the question is whether or not the input
         * playlist is contained in the query string.
         */
        case TRACK_COMPARE:
            result = !queryStr.contains(playlistName);
            break;

        default:
            throw new InternalErrorException(true, "unknown query type '" + queryType + "'");
        }

        return result;
    }

    /*
     * Initialize BXML variables and collect the list of components to be
     * skinned.
     */
    private void initializeBxmlVariables(List<Component> components) 
            throws IOException, SerializationException
    {
        logger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        fileSaveDialog = 
                (Dialog) dialogSerializer.readObject(getClass().getResource("fileSaveDialog.bxml"));

        /*
         * This doesn't need to be added to the components; it's the overall,
         * invisible, table pane. We just need it to control the width of the
         * file save dialog.
         */
        fileSaveTablePane = 
                (TablePane) dialogSerializer.getNamespace().get("fileSaveTablePane");

        fileSaveDetailsBorder = 
                (Border) dialogSerializer.getNamespace().get("fileSaveDetailsBorder");
        components.add(fileSaveDetailsBorder);
        fileSaveDetailsBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("fileSaveDetailsBoxPane");
        components.add(fileSaveDetailsBoxPane);
        fileSaveDetailsFileSeparator = 
                (Separator) dialogSerializer.getNamespace().get("fileSaveDetailsFileSeparator");
        components.add(fileSaveDetailsFileSeparator);
        fileSaveDetailsLabel = 
                (Label) dialogSerializer.getNamespace().get("fileSaveDetailsLabel");
        components.add(fileSaveDetailsLabel);
        fileSaveDetailsTextInput = 
                (TextInput) dialogSerializer.getNamespace().get("fileSaveDetailsTextInput");
        components.add(fileSaveDetailsTextInput);
        fileSaveDetailsPrintSeparator = 
                (Separator) dialogSerializer.getNamespace().get("fileSaveDetailsPrintSeparator");
        components.add(fileSaveDetailsPrintSeparator);
        fileSaveDetailsPrintCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("fileSaveDetailsPrintCheckbox");
        components.add(fileSaveDetailsPrintCheckbox);
        fileSaveDetailsOptionsSeparator = 
                (Separator) dialogSerializer.getNamespace().get("fileSaveDetailsOptionsSeparator");
        components.add(fileSaveDetailsOptionsSeparator);
        fileSaveDetailsLimitCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("fileSaveDetailsLimitCheckbox");
        components.add(fileSaveDetailsLimitCheckbox);
        fileSaveButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("fileSaveButtonBorder");
        components.add(fileSaveButtonBorder);
        fileSaveButtonBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("fileSaveButtonBoxPane");
        components.add(fileSaveButtonBoxPane);
        fileSaveDoneButton = 
                (PushButton) dialogSerializer.getNamespace().get("fileSaveDoneButton");
        components.add(fileSaveDoneButton);
    }

    // ---------------- Nested classes --------------------------------------

    /*
     * This class implements the DialogCloseListener interface, and gets control
     * when the file save dialog is closed.
     */
    private final class CloseAdapter implements DialogCloseListener
    {

        @Override
        public void dialogClosed(Dialog dialog, boolean modal)
        {
            logger.trace("dialogClosed: " + this.hashCode());
            
            /*
             * Don't do anything if the user closed the window using the host controls.
             */
            if (doneHandlerExecuted == true)
            {

                /*
                 * Generate and save the query results.
                 */
                String output;
                
                if (queryType == ListQueryType.Type.PLAYLIST_FAMILY)
                {
                    output = generatePlaylistsOutput();
                }
                else
                {
                    output = generateTracksOutput();
                }

                try
                {
                    saveOutput(output);
                }
                catch (IOException e)
                {
                    MainWindow.logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }
            }
        }
    }
}
