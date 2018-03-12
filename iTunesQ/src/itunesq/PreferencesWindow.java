package itunesq;

import java.io.IOException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.content.ButtonData;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class that handles the user preferences window.
 * 
 * @author Jon
 *
 */
public class PreferencesWindow
{

    // ---------------- Private variables -----------------------------------

    private Sheet preferencesSheet = null;
    private MenuBars owningWindow = null;
    private Skins skins = null;
    private int plusButtonYCoordinate = -1;
    private int minusButtonYCoordinate = -1;
    private Preferences userPrefs = null;
    private Dialog skinPreviewDialog = null;
    private Logging logging = null;
    private Logger logger;
    private String owningWindowTitle;

    private boolean bypassPrefsUpdated;
    private boolean ignoredPrefsUpdated;
    private boolean fullTrackColumnsUpdated;
    private boolean duplicatesTrackColumnsUpdated;
    private boolean filteredTrackColumnsUpdated;
    private boolean playlistTrackColumnsUpdated;
    private boolean showRemoteTracksUpdated;
    private boolean skinPrefsUpdated;
    private boolean logLevelPrefsUpdated;
    private boolean saveDirectoryUpdated;
    private boolean logHistoryPrefsUpdated;

    /*
     * BXML variables ...
     */

    /*
     * ... top level tab pane.
     */
    @BXML private TabPane tabPane = null;

    /*
     * ... first tab.
     */
    @BXML private Border bypassPrefsBorder = null;
    @BXML private BoxPane bypassPrefsBoxPane = null;
    @BXML private Label bypassPrefsBorderLabel = null;
    @BXML private TablePane bypassPrefsTablePane = null;
    @BXML private Border ignoredPrefsBorder = null;
    @BXML private BoxPane ignoredPrefsBoxPane = null;
    @BXML private Label ignoredPrefsBorderLabel = null;
    @BXML private TablePane ignoredPrefsTablePane = null;
    @BXML private Border tab1ResetBorder = null;
    @BXML private BoxPane tab1ResetBoxPane = null;
    @BXML private PushButton tab1ResetButton = null;

    /*
     * ... second tab.
     */
    @BXML private Label columnPrefsBorderLabel = null;
    @BXML private Border columnPrefsBorder = null;
    @BXML private TablePane columnPrefsTablePane = null;
    @BXML private BoxPane fullColumnPrefsBoxPane = null;
    @BXML private Label fullColumnPrefsLabel = null;
    @BXML private Checkbox fullNumberCheckbox = null;
    @BXML private Checkbox fullNameCheckbox = null;
    @BXML private Checkbox fullArtistCheckbox = null;
    @BXML private Checkbox fullAlbumCheckbox = null;
    @BXML private Checkbox fullKindCheckbox = null;
    @BXML private Checkbox fullDurationCheckbox = null;
    @BXML private Checkbox fullYearCheckbox = null;
    @BXML private Checkbox fullAddedCheckbox = null;
    @BXML private Checkbox fullRatingCheckbox = null;
    @BXML private Checkbox fullRemoteCheckbox = null;
    @BXML private BoxPane duplicatesColumnPrefsBoxPane = null;
    @BXML private Label duplicatesColumnPrefsLabel = null;
    @BXML private Checkbox duplicatesNumberCheckbox = null;
    @BXML private Checkbox duplicatesNameCheckbox = null;
    @BXML private Checkbox duplicatesArtistCheckbox = null;
    @BXML private Checkbox duplicatesAlbumCheckbox = null;
    @BXML private Checkbox duplicatesKindCheckbox = null;
    @BXML private Checkbox duplicatesDurationCheckbox = null;
    @BXML private Checkbox duplicatesYearCheckbox = null;
    @BXML private Checkbox duplicatesAddedCheckbox = null;
    @BXML private Checkbox duplicatesRatingCheckbox = null;
    @BXML private Checkbox duplicatesRemoteCheckbox = null;
    @BXML private BoxPane filteredColumnPrefsBoxPane = null;
    @BXML private Label filteredColumnPrefsLabel = null;
    @BXML private Checkbox filteredNumberCheckbox = null;
    @BXML private Checkbox filteredNameCheckbox = null;
    @BXML private Checkbox filteredArtistCheckbox = null;
    @BXML private Checkbox filteredAlbumCheckbox = null;
    @BXML private Checkbox filteredKindCheckbox = null;
    @BXML private Checkbox filteredDurationCheckbox = null;
    @BXML private Checkbox filteredYearCheckbox = null;
    @BXML private Checkbox filteredAddedCheckbox = null;
    @BXML private Checkbox filteredRatingCheckbox = null;
    @BXML private Checkbox filteredRemoteCheckbox = null;
    @BXML private BoxPane playlistColumnPrefsBoxPane = null;
    @BXML private Label playlistColumnPrefsLabel = null;
    @BXML private Checkbox playlistNumberCheckbox = null;
    @BXML private Checkbox playlistNameCheckbox = null;
    @BXML private Checkbox playlistArtistCheckbox = null;
    @BXML private Checkbox playlistAlbumCheckbox = null;
    @BXML private Checkbox playlistKindCheckbox = null;
    @BXML private Checkbox playlistDurationCheckbox = null;
    @BXML private Checkbox playlistYearCheckbox = null;
    @BXML private Checkbox playlistAddedCheckbox = null;
    @BXML private Checkbox playlistRatingCheckbox = null;
    @BXML private Checkbox playlistRemoteCheckbox = null;
    @BXML private Border remoteTracksBorder = null;
    @BXML private BoxPane remoteTracksBoxPane = null;
    @BXML private Label remoteTracksLabel = null;
    @BXML private Checkbox remoteTracksCheckbox = null;
    @BXML private Border tab2ResetBorder = null;
    @BXML private BoxPane tab2ResetBoxPane = null;
    @BXML private PushButton tab2ResetButton = null;

    /*
     * ... third tab.
     */
    @BXML private BoxPane miscPrefsBoxPane = null;
    @BXML private Label skinPrefsBorderLabel = null;
    @BXML private Border skinPrefsBorder = null;
    @BXML private BoxPane skinPrefsBoxPane = null;
    @BXML private Spinner skinPrefsSpinner = null;
    @BXML private Label saveDirectoryBorderLabel = null;
    @BXML private Border saveDirectoryBorder = null;
    @BXML private BoxPane saveDirectoryBoxPane = null;
    @BXML private TextInput saveDirectoryTextInput = null;
    @BXML private Label logHistoryPrefsBorderLabel = null;
    @BXML private Border logHistoryPrefsBorder = null;
    @BXML private BoxPane logHistoryPrefsBoxPane = null;
    @BXML private TextInput logHistoryPrefsTextInput = null;
    @BXML private PushButton skinPrefsButton = null;
    @BXML private Label logLevelPrefsBorderLabel = null;
    @BXML private Border logLevelPrefsBorder = null;
    @BXML private TablePane logLevelPrefsTablePane = null;
    @BXML private BoxPane logLevelPrefsBoxPane = null;
    @BXML private Spinner logLevelPrefsSpinner = null;
    @BXML private Checkbox logLevelPrefsCheckbox = null;
    @BXML private BoxPane uiLogLevelPrefsBoxPane = null;
    @BXML private Label uiLogLevelPrefsLabel = null;
    @BXML private Spinner uiLogLevelPrefsSpinner = null;
    @BXML private BoxPane xmlLogLevelPrefsBoxPane = null;
    @BXML private Label xmlLogLevelPrefsLabel = null;
    @BXML private Spinner xmlLogLevelPrefsSpinner = null;
    @BXML private BoxPane trackLogLevelPrefsBoxPane = null;
    @BXML private Label trackLogLevelPrefsLabel = null;
    @BXML private Spinner trackLogLevelPrefsSpinner = null;
    @BXML private BoxPane playlistLogLevelPrefsBoxPane = null;
    @BXML private Label playlistLogLevelPrefsLabel = null;
    @BXML private Spinner playlistLogLevelPrefsSpinner = null;
    @BXML private BoxPane artistLogLevelPrefsBoxPane = null;
    @BXML private Label artistLogLevelPrefsLabel = null;
    @BXML private Spinner artistLogLevelPrefsSpinner = null;
    @BXML private BoxPane filterLogLevelPrefsBoxPane = null;
    @BXML private Label filterLogLevelPrefsLabel = null;
    @BXML private Spinner filterLogLevelPrefsSpinner = null;
    @BXML private Border tab3ResetBorder = null;
    @BXML private BoxPane tab3ResetBoxPane = null;
    @BXML private PushButton tab3ResetButton = null;

    /*
     * ... action buttons row.
     */
    @BXML private Border actionBorder = null;
    @BXML private BoxPane actionBoxPane = null;
    @BXML private PushButton preferencesDoneButton = null;

    /*
     * ... skin preview window.
     */
    @BXML private TablePane mainTablePane = null;
    @BXML private Border previewTextBorder = null;
    @BXML private BoxPane previewTextBoxPane = null;
    @BXML private Label previewTextLabel = null;
    @BXML private TextInput previewTextInput = null;
    @BXML private Border previewTableBorder = null;
    @BXML private TableView previewTableView = null;
    @BXML private TableView.Column previewTableColumnWeekday = null;
    @BXML private TableView.Column previewTableColumnColor = null;
    @BXML private TableView.Column previewTableColumnMood = null;
    @BXML private TableViewHeader previewTableViewHeader = null;
    @BXML private Border previewButtonBorder = null;
    @BXML private BoxPane previewButtonBoxPane = null;
    @BXML private PushButton previewButton = null;

    /**
     * Class constructor.
     * 
     * @param preferences preferences sheet
     * @param owner owning window
     */
    public PreferencesWindow(Sheet preferences, MenuBars owner)
    {

        /*
         * Create a UI logger.
         */
        String className = getClass().getSimpleName();
        logger = (Logger) LoggerFactory.getLogger(className + "_UI");

        /*
         * Get the logging object singleton.
         */
        logging = Logging.getInstance();

        /*
         * Register our logger.
         */
        logging.registerLogger(Logging.Dimension.UI, logger);

        /*
         * Initialize variables.
         */
        preferencesSheet = preferences;
        owningWindow = owner;
        userPrefs = Preferences.getInstance();
        skins = Skins.getInstance();

        logger.trace("PreferencesWindow constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the user preferences in a new window.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayPreferences(Display display) 
            throws IOException, SerializationException
    {
        logger.trace("displayPreferences: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        /*
         * Get the BXML information for the playlists window, and generate the
         * list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        BXMLSerializer prefsWindowSerializer = new BXMLSerializer();
        initializeWindowBxmlVariables(prefsWindowSerializer, components);

        /*
         * Checkboxes and spinners have a lot of boilerplate to deal with, so do
         * most of it elsewhere.
         */
        initializeTrackColumnStuff(prefsWindowSerializer, components);
        initializeLogLevelStuff(prefsWindowSerializer, components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers();

        /*
         * So tab pane data is a royal pain. There seems to be no simple way to
         * set the tab text. So we have to run the sequence of tabs, get and
         * ensure the tab data is a ButtonData, then replace the button data
         * text. The BXML uses replacement text that I check here and replace.
         * The extra paranoia resulting in exceptions is because I don't feel
         * good about this code.
         */
        TabPane.TabSequence tabs = tabPane.getTabs();
        for (Component component : tabs)
        {
            Object tabData = TabPane.getTabData(component);
            if (tabData instanceof ButtonData)
            {
                ButtonData buttonData = (ButtonData) tabData;
                String buttonText = buttonData.getText();
                if (buttonText.startsWith("TAB1"))
                {
                    buttonData.setText(StringConstants.PREFS_TAB1_BUTTON);
                }
                else if (buttonText.startsWith("TAB2"))
                {
                    buttonData.setText(StringConstants.PREFS_TAB2_BUTTON);
                }
                else if (buttonText.startsWith("TAB3"))
                {
                    buttonData.setText(StringConstants.PREFS_TAB3_BUTTON);
                }
                else
                {
                    throw new IllegalStateException("unexpected tab pane button data '" + buttonText + "'");
                }
            }
            else
            {
                throw new IllegalStateException("tab pane data not of type ButtonData");
            }
        }

        /*
         * Set the widths of the column preferences and remote tracks labels.
         */
        fullColumnPrefsLabel.setPreferredWidth(InternalConstants.PREFS_COLUMN_LABELS_WIDTH);
        duplicatesColumnPrefsLabel.setPreferredWidth(InternalConstants.PREFS_COLUMN_LABELS_WIDTH);
        filteredColumnPrefsLabel.setPreferredWidth(InternalConstants.PREFS_COLUMN_LABELS_WIDTH);
        playlistColumnPrefsLabel.setPreferredWidth(InternalConstants.PREFS_COLUMN_LABELS_WIDTH);
        remoteTracksLabel.setPreferredWidth(InternalConstants.PREFS_COLUMN_LABELS_WIDTH);

        /*
         * Set the spacing of the miscellaneous preferences row elements.
         */
        Map<String, Object> boxStyles = new HashMap<String, Object>();
        boxStyles.put("spacing", InternalConstants.PREFS_MISC_ELEMENT_SPACING);
        miscPrefsBoxPane.setStyles(boxStyles);

        /*
         * Set the widths of the log level preferences labels.
         */
        uiLogLevelPrefsLabel.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_LABELS_WIDTH);
        xmlLogLevelPrefsLabel.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_LABELS_WIDTH);
        trackLogLevelPrefsLabel.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_LABELS_WIDTH);
        playlistLogLevelPrefsLabel.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_LABELS_WIDTH);
        artistLogLevelPrefsLabel.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_LABELS_WIDTH);
        filterLogLevelPrefsLabel.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_LABELS_WIDTH);

        logger.debug("setting up tabs");

        /*
         * Set up the first tab.
         */

        /*
         * Add bypass playlist preference rows if such preferences exist. This
         * populates the component list with table row components.
         */
        List<BypassPreference> bypassPrefs = userPrefs.getBypassPrefs();

        if (bypassPrefs != null && bypassPrefs.getLength() > 0)
        {
            for (BypassPreference bypassPref : bypassPrefs)
            {
                TablePane.Row newRow = createBypassPrefsTableRow(bypassPref, components);
                bypassPrefsTablePane.getRows().add(newRow);
            }
        }

        /*
         * No bypass playlist preferences exist, so add an empty bypass playlist
         * preferences row.
         */
        else
        {
            TablePane.Row newRow = createBypassPrefsTableRow(null, components);
            bypassPrefsTablePane.getRows().add(newRow);
        }

        /*
         * Add ignored playlist preference rows if such preferences exist. This
         * populates the component list with table row components.
         */
        List<String> ignoredPrefs = userPrefs.getIgnoredPrefs();

        if (ignoredPrefs != null && ignoredPrefs.getLength() > 0)
        {
            for (String ignoredPref : ignoredPrefs)
            {
                TablePane.Row newRow = createIgnoredPrefsTableRow(ignoredPref, components);
                ignoredPrefsTablePane.getRows().add(newRow);
            }
        }

        /*
         * No ignored playlist preferences exist, so add the default ignored
         * playlist rows.
         */
        else
        {
            for (String playlist : Playlist.DEFAULT_IGNORED_PLAYLISTS)
            {
                TablePane.Row newRow = createIgnoredPrefsTableRow(playlist, components);
                ignoredPrefsTablePane.getRows().add(newRow);
            }
        }

        /*
         * Set up the second tab.
         */

        /*
         * Fill in the track column checkboxes if preferences exist.
         */
        createFullTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsFullView());
        createDuplicatesTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsDuplicatesView());
        createFilteredTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsFilteredView());
        createPlaylistTrackColumnPrefsCheckboxes(userPrefs.getTrackColumnsPlaylistView());

        /*
         * Fill in the remote tracks checkbox according to the preference.
         */
        remoteTracksCheckbox.setSelected(userPrefs.getShowRemoteTracks());

        /*
         * Set up the third tab.
         */

        /*
         * Populate the list of skin names.
         */
        Sequence<String> skinNames = skins.getSkinNames();
        List<String> skinArray = new ArrayList<String>(skinNames);
        skinPrefsSpinner.setSpinnerData(skinArray);
        skinPrefsSpinner.setCircular(true);
        skinPrefsSpinner.setPreferredWidth(InternalConstants.PREFS_SKIN_SPINNER_WIDTH);

        /*
         * Set the spinner selected index to the current preference if one
         * exists. Otherwise set it to the default skin.
         */
        String skinName;
        int index;
        if ((skinName = userPrefs.getSkinName()) != null)
        {
            index = skinNames.indexOf(skinName);
        }
        else
        {
            index = skinNames.indexOf(Skins.DEFAULT_SKIN);
        }
        skinPrefsSpinner.setSelectedIndex(index);

        /*
         * Initialize the save directory.
         */
        saveDirectoryTextInput.setText(Preferences.getSaveDirectory());

        /*
         * Initialize the log history preference.
         */
        logHistoryPrefsTextInput.setText(Integer.toString(userPrefs.getMaxLogHistory()));

        /*
         * Initialize the log level spinners.
         */
        setupLogLevelSpinner(Logging.Dimension.ALL, logLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.UI, uiLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.XML, xmlLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.TRACK, trackLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.PLAYLIST, playlistLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.ARTIST, artistLogLevelPrefsSpinner);
        setupLogLevelSpinner(Logging.Dimension.FILTER, filterLogLevelPrefsSpinner);

        /*
         * Set up the dimensional log levels according to the global log level
         * preference.
         */
        if (userPrefs.getGlobalLogLevel() == true)
        {
            logLevelPrefsCheckbox.setSelected(true);
            toggleDimensionalLogLevelWidgets(false);
        }
        else
        {
            logLevelPrefsCheckbox.setSelected(false);
            toggleDimensionalLogLevelWidgets(true);
        }

        /*
         * Set the global log level checkbox text.
         */
        logLevelPrefsCheckbox.setButtonData(StringConstants.PREFS_GLOBAL);

        /*
         * Reset the updated indicators.
         */
        bypassPrefsUpdated = false;
        ignoredPrefsUpdated = false;
        fullTrackColumnsUpdated = false;
        duplicatesTrackColumnsUpdated = false;
        filteredTrackColumnsUpdated = false;
        playlistTrackColumnsUpdated = false;
        showRemoteTracksUpdated = false;
        skinPrefsUpdated = false;
        saveDirectoryUpdated = false;
        logHistoryPrefsUpdated = false;
        logLevelPrefsUpdated = false;

        logger.debug("setting up widgets");

        /*
         * Add widget texts ...
         */

        /*
         * ... first tab.
         */
        bypassPrefsBorderLabel.setText(StringConstants.PREFS_BYPASS_BORDER);
        bypassPrefsBorderLabel.setTooltipText(StringConstants.PREFS_BYPASS_TIP);
        bypassPrefsBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        ignoredPrefsBorderLabel.setText(StringConstants.PREFS_IGNORED_BORDER);
        ignoredPrefsBorderLabel.setTooltipText(StringConstants.PREFS_IGNORED_TIP);
        ignoredPrefsBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        tab1ResetButton.setButtonData(StringConstants.PREFS_RESET);
        tab1ResetButton.setTooltipText(StringConstants.PREFS_TAB1_RESET_TIP);
        tab1ResetButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * ... second tab.
         */
        columnPrefsBorderLabel.setText(StringConstants.PREFS_COLUMN_BORDER);
        columnPrefsBorderLabel.setTooltipText(StringConstants.PREFS_COLUMN_TIP);
        columnPrefsBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        fullColumnPrefsLabel.setText(StringConstants.PREFS_COLUMN_FULL);
        fullNumberCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NUMBER);
        fullNameCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NAME);
        fullArtistCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ARTIST);
        fullAlbumCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ALBUM);
        fullKindCheckbox.setButtonData(StringConstants.TRACK_COLUMN_KIND);
        fullDurationCheckbox.setButtonData(StringConstants.TRACK_COLUMN_DURATION);
        fullYearCheckbox.setButtonData(StringConstants.TRACK_COLUMN_YEAR);
        fullAddedCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ADDED);
        fullRatingCheckbox.setButtonData(StringConstants.TRACK_COLUMN_RATING);
        fullRemoteCheckbox.setButtonData(StringConstants.TRACK_COLUMN_REMOTE);
        duplicatesColumnPrefsLabel.setText(StringConstants.PREFS_COLUMN_DUPLICATES);
        duplicatesNumberCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NUMBER);
        duplicatesNameCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NAME);
        duplicatesArtistCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ARTIST);
        duplicatesAlbumCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ALBUM);
        duplicatesKindCheckbox.setButtonData(StringConstants.TRACK_COLUMN_KIND);
        duplicatesDurationCheckbox.setButtonData(StringConstants.TRACK_COLUMN_DURATION);
        duplicatesYearCheckbox.setButtonData(StringConstants.TRACK_COLUMN_YEAR);
        duplicatesAddedCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ADDED);
        duplicatesRatingCheckbox.setButtonData(StringConstants.TRACK_COLUMN_RATING);
        duplicatesRemoteCheckbox.setButtonData(StringConstants.TRACK_COLUMN_REMOTE);
        filteredColumnPrefsLabel.setText(StringConstants.PREFS_COLUMN_FILTERED);
        filteredNumberCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NUMBER);
        filteredNameCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NAME);
        filteredArtistCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ARTIST);
        filteredAlbumCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ALBUM);
        filteredKindCheckbox.setButtonData(StringConstants.TRACK_COLUMN_KIND);
        filteredDurationCheckbox.setButtonData(StringConstants.TRACK_COLUMN_DURATION);
        filteredYearCheckbox.setButtonData(StringConstants.TRACK_COLUMN_YEAR);
        filteredAddedCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ADDED);
        filteredRatingCheckbox.setButtonData(StringConstants.TRACK_COLUMN_RATING);
        filteredRemoteCheckbox.setButtonData(StringConstants.TRACK_COLUMN_REMOTE);
        playlistColumnPrefsLabel.setText(StringConstants.PREFS_COLUMN_PLAYLIST);
        playlistNumberCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NUMBER);
        playlistNameCheckbox.setButtonData(StringConstants.TRACK_COLUMN_NAME);
        playlistArtistCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ARTIST);
        playlistAlbumCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ALBUM);
        playlistKindCheckbox.setButtonData(StringConstants.TRACK_COLUMN_KIND);
        playlistDurationCheckbox.setButtonData(StringConstants.TRACK_COLUMN_DURATION);
        playlistYearCheckbox.setButtonData(StringConstants.TRACK_COLUMN_YEAR);
        playlistAddedCheckbox.setButtonData(StringConstants.TRACK_COLUMN_ADDED);
        playlistRatingCheckbox.setButtonData(StringConstants.TRACK_COLUMN_RATING);
        playlistRemoteCheckbox.setButtonData(StringConstants.TRACK_COLUMN_REMOTE);
        remoteTracksLabel.setText(StringConstants.PREFS_SHOW_REMOTE_TRACKS);
        remoteTracksLabel.setTooltipText(StringConstants.PREFS_SHOW_REMOTE_TRACKS_TIP);
        remoteTracksLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        tab2ResetButton.setButtonData(StringConstants.PREFS_RESET);
        tab2ResetButton.setTooltipText(StringConstants.PREFS_TAB2_RESET_TIP);
        tab2ResetButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * ... third tab.
         */
        skinPrefsBorderLabel.setText(StringConstants.PREFS_SKIN_BORDER);
        skinPrefsBorderLabel.setTooltipText(StringConstants.PREFS_SKIN_TIP);
        skinPrefsBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        skinPrefsButton.setButtonData(StringConstants.PREFS_SKIN_PREVIEW);
        saveDirectoryBorderLabel.setText(StringConstants.PREFS_SAVE_DIR_BORDER);
        saveDirectoryBorderLabel.setTooltipText(StringConstants.PREFS_SAVE_DIR_TIP);
        saveDirectoryBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        logHistoryPrefsBorderLabel.setText(StringConstants.PREFS_LOG_HISTORY_BORDER);
        logHistoryPrefsBorderLabel.setTooltipText(StringConstants.PREFS_LOG_HISTORY_TIP);
        logHistoryPrefsBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        logLevelPrefsBorderLabel.setText(StringConstants.PREFS_LOG_LEVEL_BORDER);
        logLevelPrefsBorderLabel.setTooltipText(StringConstants.PREFS_LOG_LEVEL_TIP);
        logLevelPrefsBorderLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        logLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_GLOBAL_LOG_LEVEL_TIP);
        logLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        logLevelPrefsCheckbox.setTooltipText(StringConstants.PREFS_GLOBAL_LOG_LEVEL_TIP);
        logLevelPrefsCheckbox.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        uiLogLevelPrefsLabel.setText(StringConstants.PREFS_UI_LOG_LEVEL);
        uiLogLevelPrefsLabel.setTooltipText(StringConstants.PREFS_UI_LOG_LEVEL_TIP);
        uiLogLevelPrefsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        uiLogLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_UI_LOG_LEVEL_TIP);
        uiLogLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        xmlLogLevelPrefsLabel.setText(StringConstants.PREFS_XML_LOG_LEVEL);
        xmlLogLevelPrefsLabel.setTooltipText(StringConstants.PREFS_XML_LOG_LEVEL_TIP);
        xmlLogLevelPrefsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        xmlLogLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_XML_LOG_LEVEL_TIP);
        xmlLogLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        trackLogLevelPrefsLabel.setText(StringConstants.PREFS_TRACK_LOG_LEVEL);
        trackLogLevelPrefsLabel.setTooltipText(StringConstants.PREFS_TRACK_LOG_LEVEL_TIP);
        trackLogLevelPrefsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        trackLogLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_TRACK_LOG_LEVEL_TIP);
        trackLogLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        playlistLogLevelPrefsLabel.setText(StringConstants.PREFS_PLAYLIST_LOG_LEVEL);
        playlistLogLevelPrefsLabel.setTooltipText(StringConstants.PREFS_PLAYLIST_LOG_LEVEL_TIP);
        playlistLogLevelPrefsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        playlistLogLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_PLAYLIST_LOG_LEVEL_TIP);
        playlistLogLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        artistLogLevelPrefsLabel.setText(StringConstants.PREFS_ARTIST_LOG_LEVEL);
        artistLogLevelPrefsLabel.setTooltipText(StringConstants.PREFS_ARTIST_LOG_LEVEL_TIP);
        artistLogLevelPrefsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        artistLogLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_ARTIST_LOG_LEVEL_TIP);
        artistLogLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        filterLogLevelPrefsLabel.setText(StringConstants.PREFS_FILTER_LOG_LEVEL);
        filterLogLevelPrefsLabel.setTooltipText(StringConstants.PREFS_FILTER_LOG_LEVEL_TIP);
        filterLogLevelPrefsLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        filterLogLevelPrefsSpinner.setTooltipText(StringConstants.PREFS_FILTER_LOG_LEVEL_TIP);
        filterLogLevelPrefsSpinner.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        tab3ResetButton.setButtonData(StringConstants.PREFS_RESET);
        tab3ResetButton.setTooltipText(StringConstants.PREFS_TAB3_RESET_TIP);
        tab3ResetButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        preferencesDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        preferencesSheet.setTitle(Skins.Window.PREFERENCES.getDisplayValue());

        /*
         * Now register the preferences window skin elements.
         */
        skins.registerWindowElements(Skins.Window.PREFERENCES, components);

        /*
         * Skin the preferences window.
         */
        skins.skinMe(Skins.Window.PREFERENCES);

        /*
         * Open the preferences window.
         */
        logger.info("opening preferences window");
        owningWindowTitle = owningWindow.getTitle();
        owningWindow.setTitle(Skins.Window.PREFERENCES.getDisplayValue());
        preferencesSheet.open(display, owningWindow);
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers()
    {
        logger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the reset button on the first tab.
         */
        tab1ResetButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("tab 1 reset defaults button pressed");

                /*
                 * Remove all current ignored preference rows.
                 */
                TablePane.RowSequence rows = ignoredPrefsTablePane.getRows();
                rows.remove(0, rows.getLength());

                /*
                 * Create a component list for the rows we're about to add.
                 */
                List<Component> rowComponents = new ArrayList<Component>();

                /*
                 * Add the default rows.
                 */
                for (String playlist : Playlist.DEFAULT_IGNORED_PLAYLISTS)
                {
                    TablePane.Row newRow = createIgnoredPrefsTableRow(playlist, rowComponents);
                    ignoredPrefsTablePane.getRows().add(newRow);
                }

                /*
                 * Indicate the ignored preferences have been updated.
                 */
                ignoredPrefsUpdated = true;

                /*
                 * Register the new components and skin them.
                 */
                Map<Skins.Element, List<Component>> windowElements = 
                        skins.registerDynamicWindowElements(Skins.Window.PREFERENCES, rowComponents);
                skins.skinMe(Skins.Window.PREFERENCES, windowElements);

                preferencesSheet.repaint();
            }
        });

        /*
         * Listener to handle the remote tracks checkbox.
         */
        remoteTracksCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                showRemoteTracksUpdated = true;
            }
        });

        /*
         * Listener to handle the reset button on the second tab.
         */
        tab2ResetButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("tab 2 reset defaults button pressed");

                /*
                 * Clear all the checkboxes to start.
                 */
                clearTrackColumnPrefsCheckboxes();

                /*
                 * Get the defaults for the various column sets and set the
                 * checkboxes from them.
                 */
                createFullTrackColumnPrefsCheckboxes(TrackDisplayColumns.getFullColumnDefaults());
                createDuplicatesTrackColumnPrefsCheckboxes(TrackDisplayColumns.getDuplicatesColumnDefaults());
                createFilteredTrackColumnPrefsCheckboxes(TrackDisplayColumns.getFilteredColumnDefaults());
                createPlaylistTrackColumnPrefsCheckboxes(TrackDisplayColumns.getPlaylistColumnDefaults());

                /*
                 * Set the show remote tracks checkbox to unselected.
                 */
                remoteTracksCheckbox.setSelected(false);

                /*
                 * Indicate the above preferences have been updated.
                 */
                fullTrackColumnsUpdated = true;
                duplicatesTrackColumnsUpdated = true;
                filteredTrackColumnsUpdated = true;
                playlistTrackColumnsUpdated = true;
                showRemoteTracksUpdated = true;

                preferencesSheet.repaint();
            }
        });

        /*
         * Listener to handle the reset button on the third tab.
         */
        tab3ResetButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("tab 3 reset defaults button pressed");

                /*
                 * Update the save directory to the default value.
                 */
                saveDirectoryTextInput.setText(Preferences.getDefaultSaveDirectory());

                /*
                 * Update the maximum log history to the default value.
                 */
                logHistoryPrefsTextInput.setText(Integer.toString(Preferences.getDefaultMaxLogHistory()));

                /*
                 * Set the global log level checkbox to selected, and grey out
                 * all the dimensional log level widgets.
                 */
                logLevelPrefsCheckbox.setSelected(true);
                toggleDimensionalLogLevelWidgets(false);

                /*
                 * Update all log level spinners to the default value.
                 */
                Sequence<String> levelNames = logging.getLogLevelValues();
                int index = levelNames.indexOf(logging.getDefaultLogLevel().toString());

                logLevelPrefsSpinner.setSelectedIndex(index);
                uiLogLevelPrefsSpinner.setSelectedIndex(index);
                xmlLogLevelPrefsSpinner.setSelectedIndex(index);
                trackLogLevelPrefsSpinner.setSelectedIndex(index);
                playlistLogLevelPrefsSpinner.setSelectedIndex(index);
                artistLogLevelPrefsSpinner.setSelectedIndex(index);
                filterLogLevelPrefsSpinner.setSelectedIndex(index);

                /*
                 * Indicate the above preferences have been updated.
                 */
                saveDirectoryUpdated = true;
                logHistoryPrefsUpdated = true;
                logLevelPrefsUpdated = true;

                preferencesSheet.repaint();
            }
        });

        /*
         * Listener to handle the skin preview button press.
         * 
         * When the skin preview button is pressed, we pop up a dialog that
         * contains a sampling of window elements. We skin this dialog with the
         * selected skin.
         */
        skinPrefsButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logger.info("skin preview button pressed");

                Display display = button.getDisplay();

                /*
                 * Get the BXML information for the skin preview dialog, and
                 * gather the list of components to be skinned.
                 */
                List<Component> components = new ArrayList<Component>();
                try
                {
                    initializePreviewDialogBxmlVariables(components);
                }
                catch (IOException | SerializationException e)
                {
                    MainWindow.logException(logger, e);
                    throw new InternalErrorException(true, e.getMessage());
                }

                /*
                 * Text input listener to provide typing assistance in the text
                 * input box.
                 */
                previewTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
                {
                    @Override
                    public void textInserted(TextInput textInput, int index, int count)
                    {

                        /*
                         * We want to match weekdays in the text input box.
                         */
                        ArrayList<String> weekdays = new ArrayList<String>(StringConstants.PREFS_SKIN_PREVIEW_SUNDAY,
                                StringConstants.PREFS_SKIN_PREVIEW_MONDAY, StringConstants.PREFS_SKIN_PREVIEW_TUESDAY,
                                StringConstants.PREFS_SKIN_PREVIEW_WEDNESDAY,
                                StringConstants.PREFS_SKIN_PREVIEW_THURSDAY, StringConstants.PREFS_SKIN_PREVIEW_FRIDAY,
                                StringConstants.PREFS_SKIN_PREVIEW_SATURDAY);
                        weekdays.setComparator(String.CASE_INSENSITIVE_ORDER);
                        boolean result = Utilities.typingAssistant(textInput, weekdays, textInput.getText(),
                                Filter.Operator.IS);

                        /*
                         * We got a weekday match.
                         */
                        if (result == true)
                        {
                            String text = textInput.getText();

                            /*
                             * Walk through the preview table, which contains
                             * weekdays.
                             */
                            @SuppressWarnings("unchecked") 
                            List<HashMap<String, String>> tableData = 
                                (List<HashMap<String, String>>) previewTableView.getTableData();
                            for (int i = 0; i < tableData.getLength(); i++)
                            {
                                HashMap<String, String> row = tableData.get(i);

                                /*
                                 * If the entered weekday matches, select the
                                 * corresponding weekday in the table. To
                                 * demonstrate our awesomeness.
                                 */
                                String weekday = row.get("weekday");
                                if (text.equals(weekday))
                                {
                                    previewTableView.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                });

                /*
                 * Set the size of the skin preview dialog.
                 */
                mainTablePane.setPreferredWidth(InternalConstants.SKIN_PREVIEW_DIALOG_WIDTH);
                mainTablePane.setPreferredHeight(InternalConstants.SKIN_PREVIEW_DIALOG_HEIGHT);

                /*
                 * Populate the skin preview table view. Start by creating a
                 * list suitable for the setTableData() method.
                 */
                List<HashMap<String, String>> previewTableData = new ArrayList<HashMap<String, String>>();

                /*
                 * Set up the table for the 7 weekdays. Yeah, this is ugly :O
                 */
                for (int i = 0; i < 7; i++)
                {
                    HashMap<String, String> weekdayStr = new HashMap<String, String>();

                    switch (i)
                    {
                    case 0:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_SUNDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_YELLOW);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_GRATEFUL);
                        break;

                    case 1:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_MONDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_BLACK);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_HATEFUL);
                        break;

                    case 2:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_TUESDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_GRAY);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_SOMBER);
                        break;

                    case 3:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_WEDNESDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_SILVER);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_DETERMINED);
                        break;

                    case 4:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_THURSDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_LIGHT_GREEN);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_OPTIMISTIC);
                        break;

                    case 5:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_FRIDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_GREEN);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_HAPPY);
                        break;

                    case 6:
                        weekdayStr.put("weekday", StringConstants.PREFS_SKIN_PREVIEW_SATURDAY);
                        weekdayStr.put("color", StringConstants.PREFS_SKIN_PREVIEW_RAINBOW);
                        weekdayStr.put("mood", StringConstants.PREFS_SKIN_PREVIEW_RELAXED);
                        break;

                    }

                    previewTableData.add(weekdayStr);
                }

                /*
                 * Finally, set the preview table data.
                 */
                previewTableView.setTableData(previewTableData);

                /*
                 * Add widget texts.
                 */
                previewTextLabel.setText(StringConstants.PREFS_SKIN_PREVIEW_TEXT);
                previewTableColumnWeekday.setHeaderData(StringConstants.PREFS_SKIN_PREVIEW_WEEKDAY);
                previewTableColumnColor.setHeaderData(StringConstants.PREFS_SKIN_PREVIEW_COLOR);
                previewTableColumnMood.setHeaderData(StringConstants.PREFS_SKIN_PREVIEW_MOOD);
                previewButton.setButtonData(StringConstants.PREFS_SKIN_PREVIEW_CLOSE_BUTTON);

                /*
                 * Set the window title.
                 */
                skinPreviewDialog.setTitle(Skins.Window.SKIN_PREVIEW.getDisplayValue());

                /*
                 * Register the preview dialog skin elements.
                 */
                skins.registerWindowElements(Skins.Window.SKIN_PREVIEW, components);

                /*
                 * Save the current skin name so we can restore it after we've
                 * skinned the preview dialog.
                 */
                String currentSkin = skins.getCurrentSkinName();

                /*
                 * Get the selected skin name and initialize the skin elements.
                 */
                String skinPref = (String) skinPrefsSpinner.getSelectedItem();
                skins.initializeSkinElements(skinPref);

                /*
                 * Skin the preview dialog.
                 */
                skins.skinMe(Skins.Window.SKIN_PREVIEW);

                /*
                 * Restore the current skin elements.
                 */
                skins.initializeSkinElements(currentSkin);

                /*
                 * Open the preview dialog. The close button action is included
                 * in the BXML.
                 */
                logger.info("opening skin preview dialog");
                skinPreviewDialog.open(display);
            }
        });

        /*
         * Listener to handle a change to the save directory text box.
         * 
         * This is so we can indicate the save directory has been updated when
         * the user inserts a directory name in the text box.
         */
        saveDirectoryTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
        {
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
                saveDirectoryUpdated = true;
            }
        });

        /*
         * Listener to handle a change to the log history preferences text box.
         * 
         * This is so we can indicate the log history preferences have been
         * updated when the user inserts text in the text box.
         */
        logHistoryPrefsTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
        {
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
                logHistoryPrefsUpdated = true;

                boolean badInput = false;
                int maxHistory = -1;

                /*
                 * Make sure the input is a positive numerical value.
                 */
                try
                {
                    maxHistory = Integer.parseInt(textInput.getText());
                }
                catch (NumberFormatException e)
                {
                    badInput = true;
                }

                if (badInput == true || maxHistory <= 0)
                {
                    Alert.alert(MessageType.WARNING, StringConstants.ALERT_LOG_HISTORY_VALUE, textInput.getWindow());
                }
            }
        });

        /*
         * Listener to handle the done button press.
         */
        preferencesDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                boolean prefsUpdated = false;
                logger.info("done button pressed");

                /*
                 * Collect the preferences from the preferences window, and
                 * update the preferences object.
                 */

                /*
                 * First tab.
                 */

                if (bypassPrefsUpdated == true)
                {
                    logger.info("updating bypass playlist preferences");

                    prefsUpdated = true;

                    List<BypassPreference> bypassPrefs = collectBypassPrefs();
                    userPrefs.replaceBypassPrefs(bypassPrefs);
                    
                    /*
                     * Mark all bypassed playlists.
                     */
                    PlaylistCollection.markBypassedPlaylists();
                    
                    /*
                     * Update the track playlist info for all playlists, which includes the bypassed flag.
                     */
                    PlaylistCollection.updateTrackPlaylistInfo();
                }

                if (ignoredPrefsUpdated == true)
                {
                    logger.info("updating ignored playlist preferences");

                    prefsUpdated = true;

                    List<String> ignoredPrefs = collectIgnoredPrefs();
                    List<String> currentIgnoredPrefs = new ArrayList<String>(userPrefs.getIgnoredPrefs());
                    userPrefs.replaceIgnoredPrefs(ignoredPrefs);
                    PlaylistCollection.modifyIgnoredPlaylists(currentIgnoredPrefs, ignoredPrefs);
                }

                /*
                 * Second tab.
                 */

                if (fullTrackColumnsUpdated == true)
                {
                    logger.info("updating full track column preferences");

                    prefsUpdated = true;

                    List<List<String>> trackColumnPrefs = collectFullTrackColumnPrefs();
                    userPrefs.replaceTrackColumnsFullView(trackColumnPrefs);

                    TrackDisplayColumns.ColumnSet.FULL_VIEW.buildColumnSet(trackColumnPrefs);
                }

                if (duplicatesTrackColumnsUpdated == true)
                {
                    logger.info("updating duplicates track column preferences");

                    prefsUpdated = true;

                    List<List<String>> trackColumnPrefs = collectDuplicatesTrackColumnPrefs();
                    userPrefs.replaceTrackColumnsDuplicatesView(trackColumnPrefs);

                    TrackDisplayColumns.ColumnSet.DUPLICATES_VIEW.buildColumnSet(trackColumnPrefs);
                }

                if (filteredTrackColumnsUpdated == true)
                {
                    logger.info("updating filtered track column preferences");

                    prefsUpdated = true;

                    List<List<String>> trackColumnPrefs = collectFilteredTrackColumnPrefs();
                    userPrefs.replaceTrackColumnsFilteredView(trackColumnPrefs);

                    TrackDisplayColumns.ColumnSet.FILTERED_VIEW.buildColumnSet(trackColumnPrefs);
                }

                if (playlistTrackColumnsUpdated == true)
                {
                    logger.info("updating playlist track column preferences");

                    prefsUpdated = true;

                    List<List<String>> trackColumnPrefs = collectPlaylistTrackColumnPrefs();
                    userPrefs.replaceTrackColumnsPlaylistView(trackColumnPrefs);

                    TrackDisplayColumns.ColumnSet.PLAYLIST_VIEW.buildColumnSet(trackColumnPrefs);
                }

                if (showRemoteTracksUpdated == true)
                {
                    logger.info("updating show remote tracks preference");

                    prefsUpdated = true;

                    userPrefs.setShowRemoteTracks(remoteTracksCheckbox.isSelected());
                    Utilities.updateMainWindowLabels(userPrefs.getXMLFileName());
                }

                /*
                 * Third tab.
                 */

                if (skinPrefsUpdated == true)
                {
                    logger.info("updating skin preference");

                    prefsUpdated = true;

                    String skinPref = (String) skinPrefsSpinner.getSelectedItem();
                    userPrefs.setSkinName(skinPref);

                    skins.initializeSkinElements(skinPref);

                    /*
                     * Since the skin has been updated, re-skin all the
                     * currently stacked windows.
                     */
                    skins.reskinWindowStack();
                }

                if (saveDirectoryUpdated == true)
                {
                    logger.info("updating save directory");

                    String saveDirectory = saveDirectoryTextInput.getText();

                    /*
                     * Remove trailing slash if present.
                     */
                    if (saveDirectory.endsWith("/"))
                    {
                        String correctedDirectory = saveDirectory.substring(0, saveDirectory.length() - 2);
                        saveDirectory = correctedDirectory;
                    }

                    /*
                     * Save the save directory in the user preferences.
                     */
                    Preferences.updateSaveDirectory(saveDirectory);

                    /*
                     * Update the Java preference with the new save directory.
                     */
                    Utilities.saveJavaPreference(Utilities.JAVA_PREFS_KEY_SAVEDIR, saveDirectory);
                }

                if (logHistoryPrefsUpdated == true)
                {
                    logger.info("updating log history preference");

                    prefsUpdated = true;

                    /*
                     * Save the maximum log history in the user preferences.
                     */
                    userPrefs.setMaxLogHistory(Integer.valueOf(logHistoryPrefsTextInput.getText()));

                    /*
                     * Update all loggers with the new maximum log history.
                     */
                    logging.updateMaxHistoryFromPref();
                }

                if (logLevelPrefsUpdated == true)
                {
                    logger.info("updating log level preferences");

                    prefsUpdated = true;

                    /*
                     * Set the preferences from the user's choices.
                     */
                    userPrefs.setGlobalLogLevel(logLevelPrefsCheckbox.isSelected());

                    String levelPref = (String) logLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.ALL, Level.toLevel(levelPref));

                    levelPref = (String) uiLogLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.UI, Level.toLevel(levelPref));
                    levelPref = (String) xmlLogLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.XML, Level.toLevel(levelPref));
                    levelPref = (String) trackLogLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.TRACK, Level.toLevel(levelPref));
                    levelPref = (String) playlistLogLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.PLAYLIST, Level.toLevel(levelPref));
                    levelPref = (String) artistLogLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.ARTIST, Level.toLevel(levelPref));
                    levelPref = (String) filterLogLevelPrefsSpinner.getSelectedItem();
                    userPrefs.setLogLevel(Logging.Dimension.FILTER, Level.toLevel(levelPref));

                    /*
                     * Update the actual log levels from the preferences.
                     */
                    logging.updateLogLevelsFromPrefs();
                }

                /*
                 * Log the current set of preferences.
                 */
                userPrefs.logPreferences("updated");

                /*
                 * Write the updated user preferences.
                 */
                if (prefsUpdated == true)
                {
                    try
                    {
                        userPrefs.writePreferences();
                    }
                    catch (IOException e)
                    {
                        MainWindow.logException(logger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }

                owningWindow.setTitle(owningWindowTitle);
                preferencesSheet.close();
            }
        });

        /*
         * Listener to handle the skin preference spinner.
         */
        skinPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                skinPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });
    }

    /*
     * Create and add a bypass playlist preferences row.
     * 
     * ANNOYED RANT: Pivot listeners are a pain to deal with. It would be real
     * nice to be able to use a single method to handle both bypassed and
     * ignored playlist preferences, since it's a lot of code and most of it is
     * identical. But the listeners don't provide any way for me to distinguish
     * between the two table views, so things like the + and - buttons don't
     * work if a single method is used. So I have no choice but to duplicate a
     * lot of code. The next two methods are very close cousins.
     */
    private TablePane.Row createBypassPrefsTableRow(BypassPreference bypassPref, List<Component> components)
    {
        logger.trace("createBypassPrefsTableRow: " + this.hashCode());

        /*
         * New table row object.
         */
        TablePane.Row newRow = new TablePane.Row();

        /*
         * Create the label for the text box.
         */
        Label textLabel = new Label();
        textLabel.setText(StringConstants.PLAYLIST_NAME);

        /*
         * Create the text input box. Add the playlist name if we were provided
         * a playlist preference object.
         */
        TextInput text = new TextInput();
        if (bypassPref != null)
        {
            text.setText(bypassPref.getPlaylistName());
        }

        /*
         * Add a text input listener so we can indicate the playlist preferences
         * have been updated when the user inserts a playlist name in the text
         * box.
         * 
         * Also, call the typing assistant to fill in the playlist name as soon
         * as enough characters are entered.
         */
        text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
        {
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
                bypassPrefsUpdated = true;

                Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), textInput.getText(),
                        Filter.Operator.IS);
            }
        });

        /*
         * Create the include children checkbox. Set the selected box according
         * to any provided playlist preference object.
         */
        Checkbox includeChildren = new Checkbox(StringConstants.PREFS_BYPASS_INCLUDE);
        includeChildren.setTooltipText(StringConstants.PREFS_BYPASS_INCLUDE_TIP);
        includeChildren.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        if (bypassPref != null)
        {
            includeChildren.setSelected((bypassPref.getIncludeChildren()));
        }

        /*
         * Create the set of buttons:
         * 
         * '+' = insert a new row after this one '-' = delete this row
         */
        PushButton plusButton = new PushButton();
        plusButton.setButtonData("+");
        plusButton.setTooltipText(StringConstants.PREFS_BYPASS_PLUS_BUTTON);
        plusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        PushButton minusButton = new PushButton();
        minusButton.setButtonData("-");
        minusButton.setTooltipText(StringConstants.PREFS_BYPASS_MINUS_BUTTON);
        minusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * We need the ability to insert and remove playlist preference rows
         * based on the specific table rows where the + and - buttons exist. But
         * this a bit of a complex dance. The buttonPressed() method does not
         * provide a means to know on which row the button exists. So we need a
         * ComponentMouseButtonListener to get that function. However, based on
         * the hierarchy of listener calls, and the bubbling up of events
         * through the component hierarchy, the buttonPressed() method gets
         * called before the mouseClick() method. :(
         * 
         * So the buttonPressed() method just records the Y coordinate of the
         * button in a class variable, which is relative to its parent
         * component, which is the TablePane.
         * 
         * Then the mouseClick() method gets the parent, uses the recorded Y
         * coordinate to get the table row, then uses that information to insert
         * a new row, or delete the current one.
         */

        /*
         * Mouse click listener for the + button.
         */
        plusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
                Object parent = component.getParent();
                if (parent instanceof TablePane)
                {
                    TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    logger.info("plus button pressed for bypass playlist pref index " 
                            + playlistPrefsRowIndex);

                    /*
                     * Add the table row and collect the components that need to
                     * be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                    TablePane.Row tableRow = createBypassPrefsTableRow(null, rowComponents);
                    bypassPrefsTablePane.getRows().insert(tableRow, playlistPrefsRowIndex + 1);

                    /*
                     * Request focus for the text input on the newly added row.
                     */
                    for (Component rowComponent : tableRow)
                    {
                        if (rowComponent instanceof TextInput)
                        {
                            rowComponent.requestFocus();
                        }
                    }

                    /*
                     * Register the new components and skin them.
                     */
                    Map<Skins.Element, List<Component>> windowElements = 
                            skins.registerDynamicWindowElements(Skins.Window.PREFERENCES, rowComponents);
                    skins.skinMe(Skins.Window.PREFERENCES, windowElements);

                    preferencesSheet.repaint();
                }

                return false;
            }
        });

        /*
         * Button press listener for the + button.
         */
        plusButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                plusButtonYCoordinate = button.getY();
            }
        });

        /*
         * Mouse click listener for the - button.
         */
        minusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
                Object parent = component.getParent();
                if (parent instanceof TablePane)
                {
                    bypassPrefsUpdated = true;

                    TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    logger.info("minus button pressed for bypass playlist pref index " 
                            + playlistPrefsRowIndex);

                    /*
                     * Get the number of rows and make sure we don't go below
                     * one row.
                     */
                    int numRows = tablePane.getRows().getLength();
                    if (numRows <= 1)
                    {
                        Alert.alert(MessageType.ERROR, StringConstants.ALERT_PLAYLIST_PREFS_TOO_FEW_ROWS,
                                component.getWindow());
                    }
                    else
                    {

                        /*
                         * Remove the table row.
                         */
                        bypassPrefsTablePane.getRows().remove(playlistPrefsRowIndex, 1);

                        preferencesSheet.repaint();
                    }
                }

                return false;
            }
        });

        /*
         * Button press listener for the - button.
         */
        minusButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                minusButtonYCoordinate = button.getY();
            }
        });

        /*
         * Assemble the new row.
         */
        newRow.add(textLabel);
        components.add(textLabel);
        newRow.add(text);
        components.add(text);
        newRow.add(includeChildren);
        components.add(includeChildren);
        newRow.add(plusButton);
        components.add(plusButton);
        newRow.add(minusButton);
        components.add(minusButton);

        return newRow;
    }

    /*
     * Create and add an ignored playlist preferences row.
     */
    private TablePane.Row createIgnoredPrefsTableRow(String ignoredPref, List<Component> components)
    {
        logger.trace("createIgnoredPrefsTableRow: " + this.hashCode());

        /*
         * New table row object.
         */
        TablePane.Row newRow = new TablePane.Row();

        /*
         * Create the label for the text box.
         */
        Label textLabel = new Label();
        textLabel.setText(StringConstants.PLAYLIST_NAME);

        /*
         * Create the text input box. Add the playlist name if we were provided
         * a playlist preference object.
         */
        TextInput text = new TextInput();
        if (ignoredPref != null)
        {
            text.setText(ignoredPref);
        }

        /*
         * Add a text input listener so we can indicate the playlist preferences
         * have been updated when the user inserts a playlist name in the text
         * box.
         * 
         * Also, call the typing assistant to fill in the playlist name as soon
         * as enough characters are entered.
         */
        text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
        {
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
                ignoredPrefsUpdated = true;

                Utilities.typingAssistant(textInput, XMLHandler.getPlaylistNames(), textInput.getText(),
                        Filter.Operator.IS);
            }
        });

        /*
         * Create the set of buttons:
         * 
         * '+' = insert a new row after this one '-' = delete this row
         */
        PushButton plusButton = new PushButton();
        plusButton.setButtonData("+");
        plusButton.setTooltipText(StringConstants.PREFS_IGNORED_PLUS_BUTTON);
        plusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        PushButton minusButton = new PushButton();
        minusButton.setButtonData("-");
        minusButton.setTooltipText(StringConstants.PREFS_IGNORED_MINUS_BUTTON);
        minusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * We need the ability to insert and remove playlist preference rows
         * based on the specific table rows where the + and - buttons exist. But
         * this a bit of a complex dance. The buttonPressed() method does not
         * provide a means to know on which row the button exists. So we need a
         * ComponentMouseButtonListener to get that function. However, based on
         * the hierarchy of listener calls, and the bubbling up of events
         * through the component hierarchy, the buttonPressed() method gets
         * called before the mouseClick() method. :(
         * 
         * So the buttonPressed() method just records the Y coordinate of the
         * button in a class variable, which is relative to its parent
         * component, which is the TablePane.
         * 
         * Then the mouseClick() method gets the parent, uses the recorded Y
         * coordinate to get the table row, then uses that information to insert
         * a new row, or delete the current one.
         */

        /*
         * Mouse click listener for the + button.
         */
        plusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
                Object parent = component.getParent();
                if (parent instanceof TablePane)
                {
                    TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    logger.info("plus button pressed for ignored playlist pref index " 
                            + playlistPrefsRowIndex);

                    /*
                     * Add the table row and collect the components that need to
                     * be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                    TablePane.Row tableRow = createIgnoredPrefsTableRow(null, rowComponents);
                    ignoredPrefsTablePane.getRows().insert(tableRow, playlistPrefsRowIndex + 1);

                    /*
                     * Request focus for the text input on the newly added row.
                     */
                    for (Component rowComponent : tableRow)
                    {
                        if (rowComponent instanceof TextInput)
                        {
                            rowComponent.requestFocus();
                        }
                    }

                    /*
                     * Register the new components and skin them.
                     */
                    Map<Skins.Element, List<Component>> windowElements = 
                            skins.registerDynamicWindowElements(Skins.Window.PREFERENCES, rowComponents);
                    skins.skinMe(Skins.Window.PREFERENCES, windowElements);

                    preferencesSheet.repaint();
                }

                return false;
            }
        });

        /*
         * Button press listener for the + button.
         */
        plusButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                plusButtonYCoordinate = button.getY();
            }
        });

        /*
         * Mouse click listener for the - button.
         */
        minusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
        {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
                Object parent = component.getParent();
                if (parent instanceof TablePane)
                {
                    ignoredPrefsUpdated = true;

                    TablePane tablePane = (TablePane) parent;
                    int playlistPrefsRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    logger.info("minus button pressed for ignored playlist pref index " 
                            + playlistPrefsRowIndex);

                    /*
                     * Get the number of rows and make sure we don't go below
                     * one row.
                     */
                    int numRows = tablePane.getRows().getLength();
                    if (numRows <= 1)
                    {
                        Alert.alert(MessageType.ERROR, StringConstants.ALERT_PLAYLIST_PREFS_TOO_FEW_ROWS,
                                component.getWindow());
                    }
                    else
                    {

                        /*
                         * Remove the table row.
                         */
                        ignoredPrefsTablePane.getRows().remove(playlistPrefsRowIndex, 1);

                        preferencesSheet.repaint();
                    }
                }

                return false;
            }
        });

        /*
         * Button press listener for the - button.
         */
        minusButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                minusButtonYCoordinate = button.getY();
            }
        });

        /*
         * Assemble the new row.
         */
        newRow.add(textLabel);
        components.add(textLabel);
        newRow.add(text);
        components.add(text);
        newRow.add(plusButton);
        components.add(plusButton);
        newRow.add(minusButton);
        components.add(minusButton);

        return newRow;
    }

    /*
     * Collect the entered bypass playlist preferences and update them.
     */
    private List<BypassPreference> collectBypassPrefs()
    {
        logger.trace("collectBypassPrefs: " + this.hashCode());

        /*
         * Indexes into the row elements.
         * 
         * IMPORTANT: These must match the design of the row. See
         * preferencesWindow.bxml for the column definition, and
         * createBypassPrefsTableRow() for the logic to create a row.
         */
        final int textIndex = 1;
        final int includeChildrenIndex = 2;

        /*
         * Iterate through the bypass playlist preferences table rows.
         */
        List<BypassPreference> bypassPrefs = new ArrayList<BypassPreference>();
        TablePane.RowSequence rows = bypassPrefsTablePane.getRows();
        for (TablePane.Row row : rows)
        {

            /*
             * Initialize a new bypass preference object.
             */
            BypassPreference bypassPref = new BypassPreference();

            /*
             * Handle the text input.
             */
            TextInput text = (TextInput) row.get(textIndex);
            bypassPref.setPlaylistName(text.getText());

            /*
             * Handle the include children checkbox.
             */
            Checkbox includeChildren = (Checkbox) row.get(includeChildrenIndex);
            boolean selected = includeChildren.isSelected();
            bypassPref.setIncludeChildren(selected);

            /*
             * Add this playlist preference to the collection.
             */
            bypassPrefs.add(bypassPref);
        }

        return bypassPrefs;
    }

    /*
     * Collect the entered ignored playlist preferences and update them.
     */
    private List<String> collectIgnoredPrefs()
    {
        logger.trace("collectIgnoredPrefs: " + this.hashCode());

        /*
         * Indexes into the row elements.
         * 
         * IMPORTANT: These must match the design of the row. See
         * preferencesWindow.bxml for the column definition, and
         * createIgnoredPrefsTableRow() for the logic to create a row.
         */
        final int textIndex = 1;

        /*
         * Iterate through the ignored playlist preferences table rows.
         */
        List<String> ignoredPrefs = new ArrayList<String>();
        TablePane.RowSequence rows = ignoredPrefsTablePane.getRows();
        for (TablePane.Row row : rows)
        {

            /*
             * Add this playlist preference to the collection.
             */
            TextInput text = (TextInput) row.get(textIndex);
            ignoredPrefs.add(text.getText());
        }

        return ignoredPrefs;
    }

    /*
     * Collect the full track columns preferences. This involves brute force
     * code to check all of the checkboxes to see whether they are selected or
     * not. For all that are selected, add the appropriate column to the column
     * set.
     */
    private List<List<String>> collectFullTrackColumnPrefs()
    {
        logger.trace("collectFullTrackColumnPrefs: " + this.hashCode());

        List<List<String>> columnPrefs = new ArrayList<List<String>>();

        if (fullNumberCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NUMBER.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullNameCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NAME.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullArtistCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ARTIST.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullAlbumCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ALBUM.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullKindCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.KIND.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullDurationCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.DURATION.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullYearCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.YEAR.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullAddedCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ADDED.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullRatingCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.RATING.getNameValue());
            columnPrefs.add(columnData);
        }

        if (fullRemoteCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.REMOTE.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.REMOTE.getNameValue());
            columnPrefs.add(columnData);
        }

        return columnPrefs;
    }

    /*
     * Collect the duplicates track columns preferences. This involves brute
     * force code to check all of the checkboxes to see whether they are
     * selected or not. For all that are selected, add the appropriate column to
     * the column set.
     */
    private List<List<String>> collectDuplicatesTrackColumnPrefs()
    {
        logger.trace("collectDuplicatesTrackColumnPrefs: " + this.hashCode());

        List<List<String>> columnPrefs = new ArrayList<List<String>>();

        if (duplicatesNumberCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NUMBER.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesNameCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NAME.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesArtistCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ARTIST.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesAlbumCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ALBUM.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesKindCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.KIND.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesDurationCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.DURATION.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesYearCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.YEAR.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesAddedCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ADDED.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesRatingCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.RATING.getNameValue());
            columnPrefs.add(columnData);
        }

        if (duplicatesRemoteCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.REMOTE.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.REMOTE.getNameValue());
            columnPrefs.add(columnData);
        }

        return columnPrefs;
    }

    /*
     * Collect the filtered track columns preferences. This involves brute force
     * code to check all of the checkboxes to see whether they are selected or
     * not. For all that are selected, add the appropriate column to the column
     * set.
     */
    private List<List<String>> collectFilteredTrackColumnPrefs()
    {
        logger.trace("collectFilteredTrackColumnPrefs: " + this.hashCode());

        List<List<String>> columnPrefs = new ArrayList<List<String>>();

        if (filteredNumberCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NUMBER.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredNameCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NAME.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredArtistCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ARTIST.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredAlbumCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ALBUM.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredKindCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.KIND.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredDurationCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.DURATION.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredYearCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.YEAR.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredAddedCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ADDED.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredRatingCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.RATING.getNameValue());
            columnPrefs.add(columnData);
        }

        if (filteredRemoteCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.REMOTE.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.REMOTE.getNameValue());
            columnPrefs.add(columnData);
        }

        return columnPrefs;
    }

    /*
     * Collect the playlist track columns preferences. This involves brute force
     * code to check all of the checkboxes to see whether they are selected or
     * not. For all that are selected, add the appropriate column to the column
     * set.
     */
    private List<List<String>> collectPlaylistTrackColumnPrefs()
    {
        logger.trace("collectPlaylistTrackColumnPrefs: " + this.hashCode());

        List<List<String>> columnPrefs = new ArrayList<List<String>>();

        if (playlistNumberCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NUMBER.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NUMBER.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistNameCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.NAME.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.NAME.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistArtistCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ARTIST.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ARTIST.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistAlbumCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ALBUM.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ALBUM.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistKindCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.KIND.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.KIND.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistDurationCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.DURATION.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.DURATION.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistYearCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.YEAR.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.YEAR.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistAddedCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.ADDED.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.ADDED.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistRatingCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.RATING.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.RATING.getNameValue());
            columnPrefs.add(columnData);
        }

        if (playlistRemoteCheckbox.isSelected())
        {
            List<String> columnData = TrackDisplayColumns
                    .buildColumnData(TrackDisplayColumns.ColumnNames.REMOTE.getHeaderValue(),
                            TrackDisplayColumns.ColumnNames.REMOTE.getNameValue());
            columnPrefs.add(columnData);
        }

        return columnPrefs;
    }

    /*
     * Before opening the preferences window, select all checkboxes for which
     * preferences exist, for the full column set. This involves more brute
     * force code to check each preference, so we know which checkboxes to
     * select.
     */
    private void createFullTrackColumnPrefsCheckboxes(List<List<String>> columnPrefs)
    {
        logger.trace("createFullTrackColumnPrefsCheckboxes: " + this.hashCode());

        for (List<String> columnData : columnPrefs)
        {
            String colName = columnData.get(0);
            TrackDisplayColumns.ColumnNames columnName = TrackDisplayColumns.ColumnNames.getEnum(colName);

            switch (columnName)
            {
            case NUMBER:
                fullNumberCheckbox.setSelected(true);
                break;

            case NAME:
                fullNameCheckbox.setSelected(true);
                break;

            case ARTIST:
                fullArtistCheckbox.setSelected(true);
                break;

            case ALBUM:
                fullAlbumCheckbox.setSelected(true);
                break;

            case KIND:
                fullKindCheckbox.setSelected(true);
                break;

            case DURATION:
                fullDurationCheckbox.setSelected(true);
                break;

            case YEAR:
                fullYearCheckbox.setSelected(true);
                break;

            case ADDED:
                fullAddedCheckbox.setSelected(true);
                break;

            case RATING:
                fullRatingCheckbox.setSelected(true);
                break;

            case REMOTE:
                fullRemoteCheckbox.setSelected(true);
                break;

            default:
                throw new InternalErrorException(true, "unknown column name '" + columnName + "'");
            }
        }
    }

    /*
     * Before opening the preferences window, select all checkboxes for which
     * preferences exist, for the duplicates column set. This involves more
     * brute force code to check each preference, so we know which checkboxes to
     * select.
     */
    private void createDuplicatesTrackColumnPrefsCheckboxes(List<List<String>> columnPrefs)
    {
        logger.trace("createDuplicatesTrackColumnPrefsCheckboxes: " + this.hashCode());

        for (List<String> columnData : columnPrefs)
        {
            String colName = columnData.get(0);
            TrackDisplayColumns.ColumnNames columnName = TrackDisplayColumns.ColumnNames.getEnum(colName);

            switch (columnName)
            {
            case NUMBER:
                duplicatesNumberCheckbox.setSelected(true);
                break;

            case NAME:
                duplicatesNameCheckbox.setSelected(true);
                break;

            case ARTIST:
                duplicatesArtistCheckbox.setSelected(true);
                break;

            case ALBUM:
                duplicatesAlbumCheckbox.setSelected(true);
                break;

            case KIND:
                duplicatesKindCheckbox.setSelected(true);
                break;

            case DURATION:
                duplicatesDurationCheckbox.setSelected(true);
                break;

            case YEAR:
                duplicatesYearCheckbox.setSelected(true);
                break;

            case ADDED:
                duplicatesAddedCheckbox.setSelected(true);
                break;

            case RATING:
                duplicatesRatingCheckbox.setSelected(true);
                break;

            case REMOTE:
                duplicatesRemoteCheckbox.setSelected(true);
                break;

            default:
                throw new InternalErrorException(true, "unknown column name '" + columnName + "'");
            }
        }
    }

    /*
     * Before opening the preferences window, select all checkboxes for which
     * preferences exist, for the filtered column set. This involves more brute
     * force code to check each preference, so we know which checkboxes to
     * select.
     */
    private void createFilteredTrackColumnPrefsCheckboxes(List<List<String>> columnPrefs)
    {
        logger.trace("createFilteredTrackColumnPrefsCheckboxes: " + this.hashCode());

        for (List<String> columnData : columnPrefs)
        {
            String colName = columnData.get(0);
            TrackDisplayColumns.ColumnNames columnName = TrackDisplayColumns.ColumnNames.getEnum(colName);

            switch (columnName)
            {
            case NUMBER:
                filteredNumberCheckbox.setSelected(true);
                break;

            case NAME:
                filteredNameCheckbox.setSelected(true);
                break;

            case ARTIST:
                filteredArtistCheckbox.setSelected(true);
                break;

            case ALBUM:
                filteredAlbumCheckbox.setSelected(true);
                break;

            case KIND:
                filteredKindCheckbox.setSelected(true);
                break;

            case DURATION:
                filteredDurationCheckbox.setSelected(true);
                break;

            case YEAR:
                filteredYearCheckbox.setSelected(true);
                break;

            case ADDED:
                filteredAddedCheckbox.setSelected(true);
                break;

            case RATING:
                filteredRatingCheckbox.setSelected(true);
                break;

            case REMOTE:
                filteredRemoteCheckbox.setSelected(true);
                break;

            default:
                throw new InternalErrorException(true, "unknown column name '" + columnName + "'");
            }
        }
    }

    /*
     * Before opening the preferences window, select all checkboxes for which
     * preferences exist, for the playlist column set. This involves more brute
     * force code to check each preference, so we know which checkboxes to
     * select.
     */
    private void createPlaylistTrackColumnPrefsCheckboxes(List<List<String>> columnPrefs)
    {
        logger.trace("createPlaylistTrackColumnPrefsCheckboxes: " + this.hashCode());

        for (List<String> columnData : columnPrefs)
        {
            String colName = columnData.get(0);
            TrackDisplayColumns.ColumnNames columnName = TrackDisplayColumns.ColumnNames.getEnum(colName);

            switch (columnName)
            {
            case NUMBER:
                playlistNumberCheckbox.setSelected(true);
                break;

            case NAME:
                playlistNameCheckbox.setSelected(true);
                break;

            case ARTIST:
                playlistArtistCheckbox.setSelected(true);
                break;

            case ALBUM:
                playlistAlbumCheckbox.setSelected(true);
                break;

            case KIND:
                playlistKindCheckbox.setSelected(true);
                break;

            case DURATION:
                playlistDurationCheckbox.setSelected(true);
                break;

            case YEAR:
                playlistYearCheckbox.setSelected(true);
                break;

            case ADDED:
                playlistAddedCheckbox.setSelected(true);
                break;

            case RATING:
                playlistRatingCheckbox.setSelected(true);
                break;

            case REMOTE:
                playlistRemoteCheckbox.setSelected(true);
                break;

            default:
                throw new InternalErrorException(true, "unknown column name '" + columnName + "'");
            }
        }
    }

    /*
     * Clear all the track column preferences checkboxes.
     */
    private void clearTrackColumnPrefsCheckboxes()
    {
        logger.trace("clearTrackColumnPrefsCheckboxes: " + this.hashCode());

        fullNumberCheckbox.setSelected(false);
        fullNameCheckbox.setSelected(false);
        fullArtistCheckbox.setSelected(false);
        fullAlbumCheckbox.setSelected(false);
        fullKindCheckbox.setSelected(false);
        fullDurationCheckbox.setSelected(false);
        fullYearCheckbox.setSelected(false);
        fullAddedCheckbox.setSelected(false);
        fullRatingCheckbox.setSelected(false);
        fullRemoteCheckbox.setSelected(false);

        duplicatesNumberCheckbox.setSelected(false);
        duplicatesNameCheckbox.setSelected(false);
        duplicatesArtistCheckbox.setSelected(false);
        duplicatesAlbumCheckbox.setSelected(false);
        duplicatesKindCheckbox.setSelected(false);
        duplicatesDurationCheckbox.setSelected(false);
        duplicatesYearCheckbox.setSelected(false);
        duplicatesAddedCheckbox.setSelected(false);
        duplicatesRatingCheckbox.setSelected(false);
        duplicatesRemoteCheckbox.setSelected(false);

        filteredNumberCheckbox.setSelected(false);
        filteredNameCheckbox.setSelected(false);
        filteredArtistCheckbox.setSelected(false);
        filteredAlbumCheckbox.setSelected(false);
        filteredKindCheckbox.setSelected(false);
        filteredDurationCheckbox.setSelected(false);
        filteredYearCheckbox.setSelected(false);
        filteredAddedCheckbox.setSelected(false);
        filteredRatingCheckbox.setSelected(false);
        filteredRemoteCheckbox.setSelected(false);

        playlistNumberCheckbox.setSelected(false);
        playlistNameCheckbox.setSelected(false);
        playlistArtistCheckbox.setSelected(false);
        playlistAlbumCheckbox.setSelected(false);
        playlistKindCheckbox.setSelected(false);
        playlistDurationCheckbox.setSelected(false);
        playlistYearCheckbox.setSelected(false);
        playlistAddedCheckbox.setSelected(false);
        playlistRatingCheckbox.setSelected(false);
        playlistRemoteCheckbox.setSelected(false);

    }

    /*
     * We have a large number of checkboxes to deal with. So do the brute force
     * initialization here so it doesn't clutter up displayPreferences().
     */
    private void initializeTrackColumnStuff(BXMLSerializer prefsWindowSerializer, List<Component> components)
    {
        logger.trace("initializeTrackColumnStuff: " + this.hashCode());

        fullNumberCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullNumberCheckbox");
        components.add(fullNumberCheckbox);

        fullNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullNameCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullNameCheckbox");
        components.add(fullNameCheckbox);

        fullNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullArtistCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullArtistCheckbox");
        components.add(fullArtistCheckbox);

        fullArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullAlbumCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullAlbumCheckbox");
        components.add(fullAlbumCheckbox);

        fullAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullKindCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullKindCheckbox");
        components.add(fullKindCheckbox);

        fullKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullDurationCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullDurationCheckbox");
        components.add(fullDurationCheckbox);

        fullDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullYearCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullYearCheckbox");
        components.add(fullYearCheckbox);

        fullYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullAddedCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullAddedCheckbox");
        components.add(fullAddedCheckbox);

        fullAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullRatingCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullRatingCheckbox");
        components.add(fullRatingCheckbox);

        fullRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        fullRemoteCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("fullRemoteCheckbox");
        components.add(fullRemoteCheckbox);

        fullRemoteCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                fullTrackColumnsUpdated = true;
            }
        });

        duplicatesNumberCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesNumberCheckbox");
        components.add(duplicatesNumberCheckbox);

        duplicatesNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesNameCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesNameCheckbox");
        components.add(duplicatesNameCheckbox);

        duplicatesNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesArtistCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesArtistCheckbox");
        components.add(duplicatesArtistCheckbox);

        duplicatesArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesAlbumCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesAlbumCheckbox");
        components.add(duplicatesAlbumCheckbox);

        duplicatesAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesKindCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesKindCheckbox");
        components.add(duplicatesKindCheckbox);

        duplicatesKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesDurationCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesDurationCheckbox");
        components.add(duplicatesDurationCheckbox);

        duplicatesDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesYearCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesYearCheckbox");
        components.add(duplicatesYearCheckbox);

        duplicatesYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesAddedCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesAddedCheckbox");
        components.add(duplicatesAddedCheckbox);

        duplicatesAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesRatingCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesRatingCheckbox");
        components.add(duplicatesRatingCheckbox);

        duplicatesRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        duplicatesRemoteCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("duplicatesRemoteCheckbox");
        components.add(duplicatesRemoteCheckbox);

        duplicatesRemoteCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                duplicatesTrackColumnsUpdated = true;
            }
        });

        filteredNumberCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredNumberCheckbox");
        components.add(filteredNumberCheckbox);

        filteredNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredNameCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredNameCheckbox");
        components.add(filteredNameCheckbox);

        filteredNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredArtistCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredArtistCheckbox");
        components.add(filteredArtistCheckbox);

        filteredArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredAlbumCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredAlbumCheckbox");
        components.add(filteredAlbumCheckbox);

        filteredAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredKindCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredKindCheckbox");
        components.add(filteredKindCheckbox);

        filteredKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredDurationCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredDurationCheckbox");
        components.add(filteredDurationCheckbox);

        filteredDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredYearCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredYearCheckbox");
        components.add(filteredYearCheckbox);

        filteredYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredAddedCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredAddedCheckbox");
        components.add(filteredAddedCheckbox);

        filteredAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredRatingCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredRatingCheckbox");
        components.add(filteredRatingCheckbox);

        filteredRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        filteredRemoteCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("filteredRemoteCheckbox");
        components.add(filteredRemoteCheckbox);

        filteredRemoteCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                filteredTrackColumnsUpdated = true;
            }
        });

        playlistNumberCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistNumberCheckbox");
        components.add(playlistNumberCheckbox);

        playlistNumberCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistNameCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistNameCheckbox");
        components.add(playlistNameCheckbox);

        playlistNameCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistArtistCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistArtistCheckbox");
        components.add(playlistArtistCheckbox);

        playlistArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistAlbumCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistAlbumCheckbox");
        components.add(playlistAlbumCheckbox);

        playlistAlbumCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistKindCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistKindCheckbox");
        components.add(playlistKindCheckbox);

        playlistKindCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistDurationCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistDurationCheckbox");
        components.add(playlistDurationCheckbox);

        playlistDurationCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistYearCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistYearCheckbox");
        components.add(playlistYearCheckbox);

        playlistYearCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistAddedCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistAddedCheckbox");
        components.add(playlistAddedCheckbox);

        playlistAddedCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistRatingCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistRatingCheckbox");
        components.add(playlistRatingCheckbox);

        playlistRatingCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });

        playlistRemoteCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("playlistRemoteCheckbox");
        components.add(playlistRemoteCheckbox);

        playlistRemoteCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                playlistTrackColumnsUpdated = true;
            }
        });
    }

    /*
     * We have a large number of log widgets to deal with. So do the brute force
     * initialization here so it doesn't clutter up displayPreferences().
     */
    private void initializeLogLevelStuff(BXMLSerializer prefsWindowSerializer, List<Component> components)
    {
        logger.trace("initializeLogLevelStuff: " + this.hashCode());

        logLevelPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("logLevelPrefsSpinner");
        components.add(logLevelPrefsSpinner);

        logLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });

        logLevelPrefsCheckbox = 
                (Checkbox) prefsWindowSerializer.getNamespace().get("logLevelPrefsCheckbox");
        components.add(logLevelPrefsCheckbox);

        logLevelPrefsCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                logLevelPrefsUpdated = true;

                /*
                 * Enable or disable dimensional elements based on the general
                 * checkbox selection.
                 */
                if (button.isSelected())
                {
                    toggleDimensionalLogLevelWidgets(false);
                }
                else
                {
                    toggleDimensionalLogLevelWidgets(true);
                }
            }
        });

        uiLogLevelPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("uiLogLevelPrefsSpinner");
        components.add(uiLogLevelPrefsSpinner);

        uiLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });

        xmlLogLevelPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("xmlLogLevelPrefsSpinner");
        components.add(xmlLogLevelPrefsSpinner);

        xmlLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });

        trackLogLevelPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("trackLogLevelPrefsSpinner");
        components.add(trackLogLevelPrefsSpinner);

        trackLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });

        playlistLogLevelPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("playlistLogLevelPrefsSpinner");
        components.add(playlistLogLevelPrefsSpinner);

        playlistLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });

        artistLogLevelPrefsSpinner =
                (Spinner) prefsWindowSerializer.getNamespace().get("artistLogLevelPrefsSpinner");
        components.add(artistLogLevelPrefsSpinner);

        artistLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });

        filterLogLevelPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("filterLogLevelPrefsSpinner");
        components.add(filterLogLevelPrefsSpinner);

        filterLogLevelPrefsSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener()
        {
            @Override
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex)
            {
                logLevelPrefsUpdated = true;
            }

            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem)
            {
            }
        });
    }

    /*
     * Set up a given log level spinner.
     */
    private void setupLogLevelSpinner(Logging.Dimension dimension, Spinner spinner)
    {
        logger.trace("setupLogLevelSpinner: " + this.hashCode());

        Logging logging = Logging.getInstance();
        Sequence<String> levelNames = logging.getLogLevelValues();
        List<String> levelArray = new ArrayList<String>(levelNames);

        /*
         * Set the spinner selected index to the current preference if one
         * exists. Otherwise set it to the current value in Logging, which
         * should always be set to a valid value.
         */
        Level level;
        int index;
        if ((level = userPrefs.getLogLevel(dimension)) != null)
        {
            index = levelNames.indexOf(level.toString());
        }
        else
        {
            index = levelNames.indexOf(dimension.getLogLevel().toString());
        }

        spinner.setSpinnerData(levelArray);
        spinner.setCircular(true);
        spinner.setPreferredWidth(InternalConstants.PREFS_LOG_LEVEL_SPINNER_WIDTH);
        spinner.setSelectedIndex(index);
    }

    /*
     * Toggle the dimensional log level labels and spinners on or off.
     */
    private void toggleDimensionalLogLevelWidgets(boolean value)
    {
        logger.trace("toggleDimensionalLogLevelWidgets: " + this.hashCode());
        
        uiLogLevelPrefsLabel.setEnabled(value);
        uiLogLevelPrefsSpinner.setEnabled(value);
        xmlLogLevelPrefsLabel.setEnabled(value);
        xmlLogLevelPrefsSpinner.setEnabled(value);
        trackLogLevelPrefsLabel.setEnabled(value);
        trackLogLevelPrefsSpinner.setEnabled(value);
        playlistLogLevelPrefsLabel.setEnabled(value);
        playlistLogLevelPrefsSpinner.setEnabled(value);
        artistLogLevelPrefsLabel.setEnabled(value);
        artistLogLevelPrefsSpinner.setEnabled(value);
        filterLogLevelPrefsLabel.setEnabled(value);
        filterLogLevelPrefsSpinner.setEnabled(value);
    }

    /*
     * Initialize preferences window BXML variables and collect the list of
     * components to be skinned.
     */
    private void initializeWindowBxmlVariables(BXMLSerializer prefsWindowSerializer, List<Component> components)
            throws IOException, SerializationException
    {
        logger.trace("initializeWindowBxmlVariables: " + this.hashCode());

        preferencesSheet = 
                (Sheet) prefsWindowSerializer.readObject(getClass().getResource("preferencesWindow.bxml"));

        tabPane = 
                (TabPane) prefsWindowSerializer.getNamespace().get("tabPane");
        components.add(tabPane);

        /*
         * First tab.
         */
        bypassPrefsBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("bypassPrefsBorder");
        components.add(bypassPrefsBorder);
        bypassPrefsBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("bypassPrefsBoxPane");
        components.add(bypassPrefsBoxPane);
        bypassPrefsBorderLabel = 
                (Label) prefsWindowSerializer.getNamespace().get("bypassPrefsBorderLabel");
        components.add(bypassPrefsBorderLabel);
        bypassPrefsTablePane = 
                (TablePane) prefsWindowSerializer.getNamespace().get("bypassPrefsTablePane");
        components.add(bypassPrefsTablePane);
        ignoredPrefsBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("ignoredPrefsBorder");
        components.add(ignoredPrefsBorder);
        ignoredPrefsBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("ignoredPrefsBoxPane");
        components.add(ignoredPrefsBoxPane);
        ignoredPrefsBorderLabel = 
                (Label) prefsWindowSerializer.getNamespace().get("ignoredPrefsBorderLabel");
        components.add(ignoredPrefsBorderLabel);
        ignoredPrefsTablePane = 
                (TablePane) prefsWindowSerializer.getNamespace().get("ignoredPrefsTablePane");
        components.add(ignoredPrefsTablePane);
        tab1ResetBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("tab1ResetBorder");
        components.add(tab1ResetBorder);
        tab1ResetBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("tab1ResetBoxPane");
        components.add(tab1ResetBoxPane);
        tab1ResetButton = 
                (PushButton) prefsWindowSerializer.getNamespace().get("tab1ResetButton");
        components.add(tab1ResetButton);

        /*
         * Second tab.
         */
        columnPrefsBorderLabel = 
                (Label) prefsWindowSerializer.getNamespace().get("columnPrefsBorderLabel");
        components.add(columnPrefsBorderLabel);
        columnPrefsBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("columnPrefsBorder");
        components.add(columnPrefsBorder);
        columnPrefsTablePane = 
                (TablePane) prefsWindowSerializer.getNamespace().get("columnPrefsTablePane");
        components.add(columnPrefsTablePane);
        fullColumnPrefsBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("fullColumnPrefsBoxPane");
        components.add(fullColumnPrefsBoxPane);
        fullColumnPrefsLabel = 
                (Label) prefsWindowSerializer.getNamespace().get("fullColumnPrefsLabel");
        components.add(fullColumnPrefsLabel);
        duplicatesColumnPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("duplicatesColumnPrefsBoxPane");
        components.add(duplicatesColumnPrefsBoxPane);
        duplicatesColumnPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("duplicatesColumnPrefsLabel");
        components.add(duplicatesColumnPrefsLabel);
        filteredColumnPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("filteredColumnPrefsBoxPane");
        components.add(filteredColumnPrefsBoxPane);
        filteredColumnPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("filteredColumnPrefsLabel");
        components.add(filteredColumnPrefsLabel);
        playlistColumnPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("playlistColumnPrefsBoxPane");
        components.add(playlistColumnPrefsBoxPane);
        playlistColumnPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("playlistColumnPrefsLabel");
        components.add(playlistColumnPrefsLabel);
        remoteTracksBorder =
                (Border) prefsWindowSerializer.getNamespace().get("remoteTracksBorder");
        components.add(remoteTracksBorder);
        remoteTracksBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("remoteTracksBoxPane");
        components.add(remoteTracksBoxPane);
        remoteTracksLabel = 
                (Label) prefsWindowSerializer.getNamespace().get("remoteTracksLabel");
        components.add(remoteTracksLabel);
        remoteTracksCheckbox =
                (Checkbox) prefsWindowSerializer.getNamespace().get("remoteTracksCheckbox");
        components.add(remoteTracksCheckbox);
        tab2ResetBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("tab2ResetBorder");
        components.add(tab2ResetBorder);
        tab2ResetBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("tab2ResetBoxPane");
        components.add(tab2ResetBoxPane);
        tab2ResetButton = 
                (PushButton) prefsWindowSerializer.getNamespace().get("tab2ResetButton");
        components.add(tab2ResetButton);

        /*
         * Third tab.
         */

        /*
         * This doesn't need to be added to the components. It's an invisible
         * box pane. We only need it to control the spacing of the elements on
         * the miscellaneous preferences row.
         */
        miscPrefsBoxPane = (BoxPane) prefsWindowSerializer.getNamespace().get("miscPrefsBoxPane");

        skinPrefsBorderLabel =
                (Label) prefsWindowSerializer.getNamespace().get("skinPrefsBorderLabel");
        components.add(skinPrefsBorderLabel);
        skinPrefsBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("skinPrefsBorder");
        components.add(skinPrefsBorder);
        skinPrefsBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("skinPrefsBoxPane");
        components.add(skinPrefsBoxPane);
        skinPrefsSpinner = 
                (Spinner) prefsWindowSerializer.getNamespace().get("skinPrefsSpinner");
        components.add(skinPrefsSpinner);
        skinPrefsButton = 
                (PushButton) prefsWindowSerializer.getNamespace().get("skinPrefsButton");
        components.add(skinPrefsButton);
        saveDirectoryBorderLabel =
                (Label) prefsWindowSerializer.getNamespace().get("saveDirectoryBorderLabel");
        components.add(saveDirectoryBorderLabel);
        saveDirectoryBorder =
                (Border) prefsWindowSerializer.getNamespace().get("saveDirectoryBorder");
        components.add(saveDirectoryBorder);
        saveDirectoryBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("saveDirectoryBoxPane");
        components.add(saveDirectoryBoxPane);
        saveDirectoryTextInput =
                (TextInput) prefsWindowSerializer.getNamespace().get("saveDirectoryTextInput");
        components.add(saveDirectoryTextInput);
        logHistoryPrefsBorderLabel =
                (Label) prefsWindowSerializer.getNamespace().get("logHistoryPrefsBorderLabel");
        components.add(logHistoryPrefsBorderLabel);
        logHistoryPrefsBorder =
                (Border) prefsWindowSerializer.getNamespace().get("logHistoryPrefsBorder");
        components.add(logHistoryPrefsBorder);
        logHistoryPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("logHistoryPrefsBoxPane");
        components.add(logHistoryPrefsBoxPane);
        logHistoryPrefsTextInput =
                (TextInput) prefsWindowSerializer.getNamespace().get("logHistoryPrefsTextInput");
        components.add(logHistoryPrefsTextInput);
        logLevelPrefsBorderLabel =
                (Label) prefsWindowSerializer.getNamespace().get("logLevelPrefsBorderLabel");
        components.add(logLevelPrefsBorderLabel);
        logLevelPrefsBorder =
                (Border) prefsWindowSerializer.getNamespace().get("logLevelPrefsBorder");
        components.add(logLevelPrefsBorder);
        logLevelPrefsTablePane =
                (TablePane) prefsWindowSerializer.getNamespace().get("logLevelPrefsTablePane");
        components.add(logLevelPrefsTablePane);
        logLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("logLevelPrefsBoxPane");
        components.add(logLevelPrefsBoxPane);
        uiLogLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("uiLogLevelPrefsBoxPane");
        components.add(uiLogLevelPrefsBoxPane);
        uiLogLevelPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("uiLogLevelPrefsLabel");
        components.add(uiLogLevelPrefsLabel);
        xmlLogLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("xmlLogLevelPrefsBoxPane");
        components.add(xmlLogLevelPrefsBoxPane);
        xmlLogLevelPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("xmlLogLevelPrefsLabel");
        components.add(xmlLogLevelPrefsLabel);
        trackLogLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("trackLogLevelPrefsBoxPane");
        components.add(trackLogLevelPrefsBoxPane);
        trackLogLevelPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("trackLogLevelPrefsLabel");
        components.add(trackLogLevelPrefsLabel);
        playlistLogLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("playlistLogLevelPrefsBoxPane");
        components.add(playlistLogLevelPrefsBoxPane);
        playlistLogLevelPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("playlistLogLevelPrefsLabel");
        components.add(playlistLogLevelPrefsLabel);
        artistLogLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("artistLogLevelPrefsBoxPane");
        components.add(artistLogLevelPrefsBoxPane);
        artistLogLevelPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("artistLogLevelPrefsLabel");
        components.add(artistLogLevelPrefsLabel);
        filterLogLevelPrefsBoxPane =
                (BoxPane) prefsWindowSerializer.getNamespace().get("filterLogLevelPrefsBoxPane");
        components.add(filterLogLevelPrefsBoxPane);
        filterLogLevelPrefsLabel =
                (Label) prefsWindowSerializer.getNamespace().get("filterLogLevelPrefsLabel");
        components.add(filterLogLevelPrefsLabel);
        tab3ResetBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("tab3ResetBorder");
        components.add(tab3ResetBorder);
        tab3ResetBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("tab3ResetBoxPane");
        components.add(tab3ResetBoxPane);
        tab3ResetButton = 
                (PushButton) prefsWindowSerializer.getNamespace().get("tab3ResetButton");
        components.add(tab3ResetButton);

        /*
         * Action buttons.
         */
        actionBorder = 
                (Border) prefsWindowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = 
                (BoxPane) prefsWindowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);
        preferencesDoneButton =
                (PushButton) prefsWindowSerializer.getNamespace().get("preferencesDoneButton");
        components.add(preferencesDoneButton);
    }

    /*
     * Initialize skin preview dialog BXML variables and collect the components
     * to be skinned.
     */
    private void initializePreviewDialogBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        logger.trace("initializePreviewDialogBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        skinPreviewDialog = 
                (Dialog) dialogSerializer.readObject(getClass().getResource("skinPreviewDialog.bxml"));

        /*
         * This doesn't need to be added to the components. It's an invisible
         * table pane. It's only needed to control the size of the skin preview
         * dialog.
         */
        mainTablePane = 
                (TablePane) dialogSerializer.getNamespace().get("mainTablePane");

        previewTextBorder = 
                (Border) dialogSerializer.getNamespace().get("previewTextBorder");
        components.add(previewTextBorder);
        previewTextBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("previewTextBoxPane");
        components.add(previewTextBoxPane);
        previewTextLabel = 
                (Label) dialogSerializer.getNamespace().get("previewTextLabel");
        components.add(previewTextLabel);
        previewTextInput = 
                (TextInput) dialogSerializer.getNamespace().get("previewTextInput");
        components.add(previewTextInput);
        previewTableBorder = 
                (Border) dialogSerializer.getNamespace().get("previewTableBorder");
        components.add(previewTableBorder);
        previewTableView = 
                (TableView) dialogSerializer.getNamespace().get("previewTableView");
        components.add(previewTableView);

        /*
         * These don't need to be added to the components list because they're
         * subcomponents.
         */
        previewTableColumnWeekday =
                (TableView.Column) dialogSerializer.getNamespace().get("previewTableColumnWeekday");
        previewTableColumnColor =
                (TableView.Column) dialogSerializer.getNamespace().get("previewTableColumnColor");
        previewTableColumnMood =
                (TableView.Column) dialogSerializer.getNamespace().get("previewTableColumnMood");

        previewTableViewHeader =
                (TableViewHeader) dialogSerializer.getNamespace().get("previewTableViewHeader");
        components.add(previewTableViewHeader);
        previewButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("previewButtonBorder");
        components.add(previewButtonBorder);
        previewButtonBoxPane =
                (BoxPane) dialogSerializer.getNamespace().get("previewButtonBoxPane");
        components.add(previewButtonBoxPane);
        previewButton = 
                (PushButton) dialogSerializer.getNamespace().get("previewButton");
        components.add(previewButton);
    }
}
