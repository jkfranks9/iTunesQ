package itunesq;

import java.io.IOException;
import java.util.BitSet;
import java.util.Comparator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the find duplicates dialog. This dialog allows the user to
 * find exact song duplicates, or fuzzy duplicates based on track attribute
 * search criteria. For example:
 * <ul>
 * <li>Songs with the same name but different artists</li>
 * <li>Songs with the same name and artist but different kind (MP3 vs AAC)</li>
 * <li>Songs with the same name and artist but different duration</li>
 * </ul>
 * 
 * @author Jon
 *
 */
public class FindDuplicatesDialog
{

    // ---------------- Private variables -----------------------------------

    private Dialog findDuplicatesDialog = null;
    private Window owningWindow = null;
    private BitSet matchSpec = null;

    private enum MatchCriteria
    {
        EXACT, ARTIST, NOT_ARTIST, ALBUM, KIND, DURATION, YEAR, RATING
    }

    private static final int NUM_MATCH_CRITERIA = MatchCriteria.values().length;

    private Logger uiLogger = null;
    private Logger trackLogger = null;

    /*
     * BXML variables.
     */
    @BXML private Border duplicatesSpecBorder = null;
    @BXML private BoxPane duplicatesSpecBoxPane = null;
    @BXML private Label duplicatesSpecLabel = null;
    @BXML private TablePane duplicatesSpecTablePane = null;
    @BXML private BoxPane duplicatesSpecExactBoxPane = null;
    @BXML private Checkbox duplicatesSpecExactCheckbox = null;
    @BXML private BoxPane duplicatesSpecFuzzyBoxPane = null;
    @BXML private Checkbox duplicatesSpecArtistCheckbox = null;
    @BXML private Checkbox duplicatesSpecNotArtistCheckbox = null;
    @BXML private Checkbox duplicatesSpecAlbumCheckbox = null;
    @BXML private Checkbox duplicatesSpecKindCheckbox = null;
    @BXML private Checkbox duplicatesSpecDurationCheckbox = null;
    @BXML private Checkbox duplicatesSpecYearCheckbox = null;
    @BXML private Checkbox duplicatesSpecRatingCheckbox = null;
    @BXML private Border duplicatesButtonBorder = null;
    @BXML private BoxPane duplicatesButtonBoxPane = null;
    @BXML private PushButton duplicatesDoneButton = null;

    /**
     * Class constructor.
     * 
     * @param owner owning window. This dialog is modal over the window.
     */
    public FindDuplicatesDialog(Window owner)
    {

        /*
         * Create a UI logger.
         */
        String className = getClass().getSimpleName();
        uiLogger = (Logger) LoggerFactory.getLogger(className + "_UI");

        /*
         * Create a track logger.
         */
        trackLogger = (Logger) LoggerFactory.getLogger(className + "_Track");

        /*
         * Get the logging object singleton.
         */
        Logging logging = Logging.getInstance();

        /*
         * Register our loggers.
         */
        logging.registerLogger(Logging.Dimension.UI, uiLogger);
        logging.registerLogger(Logging.Dimension.TRACK, trackLogger);

        /*
         * Initialize variables.
         */
        owningWindow = owner;

        uiLogger.trace("FindDuplicatesDialog constructor: " + this.hashCode());
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the find duplicates dialog.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayFindDuplicatesDialog(Display display) 
            throws IOException, SerializationException
    {
        uiLogger.trace("displayFindDuplicatesDialog: " + this.hashCode());

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
         * Set up the various event handlers.
         */
        createEventHandlers(display);

        /*
         * Initialize for an artist match.
         */
        duplicatesSpecArtistCheckbox.setSelected(true);
        duplicatesSpecNotArtistCheckbox.setEnabled(false);

        /*
         * Add widget texts.
         */
        duplicatesSpecLabel.setText(StringConstants.FIND_DUPLICATES_SPEC);
        duplicatesSpecLabel.setTooltipText(StringConstants.FIND_DUPLICATES_SPEC_TIP);
        duplicatesSpecLabel.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        duplicatesSpecExactCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_EXACT);
        duplicatesSpecArtistCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_ARTIST);
        duplicatesSpecNotArtistCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_NOT_ARTIST);
        duplicatesSpecAlbumCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_ALBUM);
        duplicatesSpecKindCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_KIND);
        duplicatesSpecDurationCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_DURATION);
        duplicatesSpecYearCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_YEAR);
        duplicatesSpecRatingCheckbox.setButtonData(StringConstants.FIND_DUPLICATES_RATING);
        duplicatesDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        findDuplicatesDialog.setTitle(Skins.Window.FINDDUPLICATES.getDisplayValue());

        /*
         * Get the skins singleton.
         */
        Skins skins = Skins.getInstance();

        /*
         * Register the find duplicates dialog skin elements.
         */
        Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
        skins.registerWindowElements(Skins.Window.FINDDUPLICATES, windowElements);

        /*
         * Skin the find duplicates dialog.
         */
        skins.skinMe(Skins.Window.FINDDUPLICATES);

        /*
         * Open the find duplicates dialog.
         */
        uiLogger.info("opening find duplicates dialog");
        findDuplicatesDialog.open(display, owningWindow);
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers(Display display)
    {
        uiLogger.trace("createEventHandlers: " + this.hashCode());

        /*
         * Listener to handle the done button press.
         */
        duplicatesDoneButton.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                uiLogger.info("duplicates done button pressed");

                /*
                 * Gather the selected match criteria.
                 */
                matchSpec = new BitSet(NUM_MATCH_CRITERIA);
                matchSpec.set(MatchCriteria.EXACT.ordinal(), duplicatesSpecExactCheckbox.isSelected());

                if (matchSpec.get(MatchCriteria.EXACT.ordinal()) == false)
                {
                    matchSpec.set(MatchCriteria.ARTIST.ordinal(), duplicatesSpecArtistCheckbox.isSelected());
                    matchSpec.set(MatchCriteria.NOT_ARTIST.ordinal(), duplicatesSpecNotArtistCheckbox.isSelected());
                    matchSpec.set(MatchCriteria.ALBUM.ordinal(), duplicatesSpecAlbumCheckbox.isSelected());
                    matchSpec.set(MatchCriteria.KIND.ordinal(), duplicatesSpecKindCheckbox.isSelected());
                    matchSpec.set(MatchCriteria.DURATION.ordinal(), duplicatesSpecDurationCheckbox.isSelected());
                    matchSpec.set(MatchCriteria.YEAR.ordinal(), duplicatesSpecYearCheckbox.isSelected());
                    matchSpec.set(MatchCriteria.RATING.ordinal(), duplicatesSpecRatingCheckbox.isSelected());
                }
                
                uiLogger.debug("selected match criteria: " + matchSpec.toString());

                /*
                 * Initialize the duplicate tracks list, and sort it by name.
                 */
                List<Track> allDupTracks = new ArrayList<Track>();
                allDupTracks.setComparator(new Comparator<Track>()
                {
                    @Override
                    public int compare(Track t1, Track t2)
                    {
                        return t1.compareTo(t2);
                    }
                });

                /*
                 * Get the duplicates map. This maps track name to all the IDs
                 * with that name.
                 */
                Map<String, List<Integer>> duplicatesMap = XMLHandler.getDuplicatesMap();

                /*
                 * Walk through all the duplicates.
                 */
                for (String dupName : duplicatesMap)
                {

                    /*
                     * Get the list of track IDs for this name.
                     */
                    List<Integer> dupIDs = duplicatesMap.get(dupName);

                    /*
                     * Get the user preferences.
                     */
                    Preferences prefs = Preferences.getInstance();
                    boolean showRemoteTracks = prefs.getShowRemoteTracks();

                    /*
                     * Create a list of track objects that correspond to the
                     * IDs.
                     */
                    List<Track> dupTracksForName = new ArrayList<Track>();
                    for (Integer dupID : dupIDs)
                    {
                        Integer trackIndex = XMLHandler.getTracksMap().get(dupID);
                        Track track = XMLHandler.getTracks().get(trackIndex);

                        /*
                         * Skip remote tracks if the user doesn't want to see
                         * them.
                         */
                        if (track.getRemote() == true && showRemoteTracks == false)
                        {
                            continue;
                        }

                        dupTracksForName.add(track);
                    }

                    /*
                     * Now create the duplicate tracks (if any) based on the
                     * match criteria.
                     */
                    addDuplicateTracks(dupName, dupTracksForName, allDupTracks);
                }

                /*
                 * Display duplicate tracks, if any.
                 */
                if (!allDupTracks.isEmpty())
                {
                    trackLogger.info("found " + allDupTracks.getLength() + " duplicate tracks for display");

                    String queryStr = getMatchCriteriaAsString();
                    TracksWindow tracksWindowHandler = new TracksWindow();
                    tracksWindowHandler.saveWindowAttributes(ListQueryType.Type.TRACK_DUPLICATES,
                            ListQueryType.Type.TRACK_DUPLICATES.getDisplayValue() + ": " + queryStr,
                            TrackDisplayColumns.ColumnSet.DUPLICATES_VIEW.getNamesList());

                    try
                    {
                        tracksWindowHandler.displayTracks(display, allDupTracks, owningWindow);
                    }
                    catch (IOException | SerializationException e)
                    {
                        MainWindow.logException(uiLogger, e);
                        throw new InternalErrorException(true, e.getMessage());
                    }
                }
                else
                {
                    Alert.alert(MessageType.INFO, StringConstants.ALERT_NO_TRACKS, owningWindow);
                }

                findDuplicatesDialog.close();
            }
        });

        /*
         * Listener to enable or disable fuzzy search criteria based on the
         * exact match checkbox.
         */
        duplicatesSpecExactCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                if (button.isSelected())
                {
                    duplicatesSpecArtistCheckbox.setEnabled(false);
                    duplicatesSpecNotArtistCheckbox.setEnabled(false);
                    duplicatesSpecAlbumCheckbox.setEnabled(false);
                    duplicatesSpecKindCheckbox.setEnabled(false);
                    duplicatesSpecDurationCheckbox.setEnabled(false);
                    duplicatesSpecYearCheckbox.setEnabled(false);
                    duplicatesSpecRatingCheckbox.setEnabled(false);
                }
                else
                {
                    duplicatesSpecArtistCheckbox.setEnabled(true);
                    duplicatesSpecNotArtistCheckbox.setEnabled(true);
                    duplicatesSpecAlbumCheckbox.setEnabled(true);
                    duplicatesSpecKindCheckbox.setEnabled(true);
                    duplicatesSpecDurationCheckbox.setEnabled(true);
                    duplicatesSpecYearCheckbox.setEnabled(true);
                    duplicatesSpecRatingCheckbox.setEnabled(true);
                }
            }
        });

        /*
         * Listener to enable or disable the not artist checkbox based on the
         * artist checkbox.
         */
        duplicatesSpecArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                if (button.isSelected())
                {
                    duplicatesSpecNotArtistCheckbox.setEnabled(false);
                    duplicatesSpecNotArtistCheckbox.setSelected(false);
                }
                else
                {
                    duplicatesSpecNotArtistCheckbox.setEnabled(true);
                }
            }
        });

        /*
         * Listener to enable or disable the artist checkbox based on the not
         * artist checkbox.
         */
        duplicatesSpecNotArtistCheckbox.getButtonPressListeners().add(new ButtonPressListener()
        {
            @Override
            public void buttonPressed(Button button)
            {
                if (button.isSelected())
                {
                    duplicatesSpecArtistCheckbox.setEnabled(false);
                    duplicatesSpecArtistCheckbox.setSelected(false);
                }
                else
                {
                    duplicatesSpecArtistCheckbox.setEnabled(true);
                }
            }
        });
    }

    /*
     * Given a list of possible duplicate tracks, determine which are actual
     * duplicates based on the match criteria.
     */
    private void addDuplicateTracks(String dupName, List<Track> dupTracksForName, List<Track> allDupTracks)
    {
        trackLogger.trace("addDuplicateTracks: " + this.hashCode());

        int dupsLen = dupTracksForName.getLength();
        trackLogger.debug("checking possible duplicate track '" + dupName + "', num IDs " + dupsLen);

        for (int i = 0; i < dupsLen; i++)
        {
            Track track1 = dupTracksForName.get(i);

            for (int j = i + 1; j < dupsLen; j++)
            {
                Track track2 = dupTracksForName.get(j);

                /*
                 * Check if the tracks match.
                 */
                if (matchTracks(track1, track2) == true)
                {

                    /*
                     * Since we have nested loops, one or both tracks might
                     * already be in the list, so we only add them if not.
                     * 
                     * NOTE: Some hideous Pivot code to deal with. The indexOf
                     * method uses a binary search if the array list has an
                     * associated comparator, which is indeed the case. This
                     * means our comparator's compare method is used, which only
                     * compares the name, meaning it finds a match based only on
                     * name, which is not what we want. So, we save, clear, and
                     * restore the comparator so the entire object gets
                     * compared.
                     */

                    /*
                     * Add the first track if it's not already there.
                     */
                    Comparator<Track> savedComparator = allDupTracks.getComparator();
                    allDupTracks.setComparator(null);
                    int index = allDupTracks.indexOf(track1);
                    allDupTracks.setComparator(savedComparator);
                    if (index == -1)
                    {
                        allDupTracks.add(track1);
                    }

                    /*
                     * Add the second track if it's not already there.
                     */
                    savedComparator = allDupTracks.getComparator();
                    allDupTracks.setComparator(null);
                    index = allDupTracks.indexOf(track2);
                    allDupTracks.setComparator(savedComparator);
                    if (index == -1)
                    {
                        allDupTracks.add(track2);
                    }
                }
            }
        }
    }

    /*
     * Match a pair of tracks based on the match criteria.
     */
    private boolean matchTracks(Track track1, Track track2)
    {
        trackLogger.trace("matchTracks: " + this.hashCode());

        boolean result = false;

        /*
         * Duration is special: since it's in milliseconds, the actual value for
         * two tracks might be slightly different, even though the time display
         * in MM:SS format is the same. So convert the time to MM:SS for
         * comparison.
         */
        String duration1 = Utilities.convertMillisecondTime(track1.getDuration());
        String duration2 = Utilities.convertMillisecondTime(track2.getDuration());

        /*
         * An exact match must match all relevant criteria.
         */
        if (matchSpec.get(MatchCriteria.EXACT.ordinal()) == true)
        {
            trackLogger.debug("exact match");

            if (isEqual(track1.getArtist(), track2.getArtist()) && isEqual(track1.getAlbum(), track2.getAlbum())
                    && isEqual(track1.getKind(), track2.getKind()) && isEqual(duration1, duration2)
                    && isEqual(track1.getYear(), track2.getYear())
                    && isEqual(track1.getCorrectedRating(), track2.getCorrectedRating()))
            {
                result = true;
            }
        }

        /*
         * A fuzzy match must match only the specified criteria.
         */
        else
        {
            trackLogger.debug("fuzzy match");

            if (matchSpec.get(MatchCriteria.ARTIST.ordinal()) == true)
            {
                result = isEqual(track1.getArtist(), track2.getArtist());
            }
            else if (matchSpec.get(MatchCriteria.NOT_ARTIST.ordinal()) == true)
            {
                result = !isEqual(track1.getArtist(), track2.getArtist());
            }

            if (matchSpec.get(MatchCriteria.ALBUM.ordinal()) == true)
            {
                if (result == true)
                {
                    result = isEqual(track1.getAlbum(), track2.getAlbum());
                }
            }

            if (matchSpec.get(MatchCriteria.KIND.ordinal()) == true)
            {
                if (result == true)
                {
                    result = isEqual(track1.getKind(), track2.getKind());
                }
            }

            if (matchSpec.get(MatchCriteria.DURATION.ordinal()) == true)
            {
                if (result == true)
                {
                    result = isEqual(duration1, duration2);
                }
            }

            if (matchSpec.get(MatchCriteria.YEAR.ordinal()) == true)
            {
                if (result == true)
                {
                    result = isEqual(track1.getYear(), track2.getYear());
                }
            }

            if (matchSpec.get(MatchCriteria.RATING.ordinal()) == true)
            {
                if (result == true)
                {
                    result = isEqual(track1.getCorrectedRating(), track2.getCorrectedRating());
                }
            }
        }

        trackLogger.debug("result " + result);
        return result;
    }

    /*
     * Check equality of two track attributes. One or both might be null. Some
     * attributes are strings and some are primitive int types.
     */
    private boolean isEqual(Object attribute1, Object attribute2)
    {
        boolean result = false;

        if (attribute1 == null && attribute2 == null)
        {
            trackLogger.debug("both attributes null");
            result = true;
        }
        else if (attribute1 != null && attribute2 != null)
        {
            if (attribute1 instanceof String)
            {
                String string1 = (String) attribute1;
                String string2 = (String) attribute2;
                trackLogger.debug("checking '" + string1 + "' against '" + string2 + "'");
                result = string1.equals(string2);
            }
            else
            {
                int int1 = (int) attribute1;
                int int2 = (int) attribute2;
                trackLogger.debug("checking " + int1 + " against " + int2);
                result = int1 == int2;
            }
        }
        else
        {
            trackLogger.debug("one attribute null");
        }

        return result;
    }

    /*
     * Get the current match criteria as a string.
     */
    private String getMatchCriteriaAsString()
    {
        uiLogger.trace("getMatchCriteriaAsString: " + this.hashCode());

        StringBuilder result = new StringBuilder();

        if (matchSpec.get(MatchCriteria.EXACT.ordinal()) == true)
        {
            result.append(StringConstants.FIND_DUPLICATES_EXACT);
        }
        else
        {
            if (matchSpec.get(MatchCriteria.ARTIST.ordinal()) == true)
            {
                result.append(StringConstants.FIND_DUPLICATES_ARTIST);
            }
            else if (matchSpec.get(MatchCriteria.NOT_ARTIST.ordinal()) == true)
            {
                result.append(StringConstants.FIND_DUPLICATES_NOT_ARTIST);
            }

            if (matchSpec.get(MatchCriteria.ALBUM.ordinal()) == true)
            {
                if (result.length() > 0)
                {
                    result.append(", ");
                }
                result.append(StringConstants.FIND_DUPLICATES_ALBUM);
            }

            if (matchSpec.get(MatchCriteria.KIND.ordinal()) == true)
            {
                if (result.length() > 0)
                {
                    result.append(", ");
                }
                result.append(StringConstants.FIND_DUPLICATES_KIND);
            }

            if (matchSpec.get(MatchCriteria.DURATION.ordinal()) == true)
            {
                if (result.length() > 0)
                {
                    result.append(", ");
                }
                result.append(StringConstants.FIND_DUPLICATES_DURATION);
            }

            if (matchSpec.get(MatchCriteria.YEAR.ordinal()) == true)
            {
                if (result.length() > 0)
                {
                    result.append(", ");
                }
                result.append(StringConstants.FIND_DUPLICATES_YEAR);
            }

            if (matchSpec.get(MatchCriteria.RATING.ordinal()) == true)
            {
                if (result.length() > 0)
                {
                    result.append(", ");
                }
                result.append(StringConstants.FIND_DUPLICATES_RATING);
            }
        }

        return result.toString();
    }

    /*
     * Initialize BXML variables and collect the list of components to be
     * skinned.
     */
    private void initializeBxmlVariables(List<Component> components) 
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer dialogSerializer = new BXMLSerializer();

        findDuplicatesDialog = 
                (Dialog) dialogSerializer.readObject(getClass().getResource("findDuplicatesDialog.bxml"));

        duplicatesSpecBorder = 
                (Border) dialogSerializer.getNamespace().get("duplicatesSpecBorder");
        components.add(duplicatesSpecBorder);
        duplicatesSpecBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("duplicatesSpecBoxPane");
        components.add(duplicatesSpecBoxPane);
        duplicatesSpecLabel = 
                (Label) dialogSerializer.getNamespace().get("duplicatesSpecLabel");
        components.add(duplicatesSpecLabel);
        duplicatesSpecTablePane = 
                (TablePane) dialogSerializer.getNamespace().get("duplicatesSpecTablePane");
        components.add(duplicatesSpecTablePane);
        duplicatesSpecExactBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("duplicatesSpecExactBoxPane");
        components.add(duplicatesSpecExactBoxPane);
        duplicatesSpecExactCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecExactCheckbox");
        components.add(duplicatesSpecExactCheckbox);
        duplicatesSpecFuzzyBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("duplicatesSpecFuzzyBoxPane");
        components.add(duplicatesSpecFuzzyBoxPane);
        duplicatesSpecArtistCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecArtistCheckbox");
        components.add(duplicatesSpecArtistCheckbox);
        duplicatesSpecNotArtistCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecNotArtistCheckbox");
        components.add(duplicatesSpecNotArtistCheckbox);
        duplicatesSpecAlbumCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecAlbumCheckbox");
        components.add(duplicatesSpecAlbumCheckbox);
        duplicatesSpecKindCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecKindCheckbox");
        components.add(duplicatesSpecKindCheckbox);
        duplicatesSpecDurationCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecDurationCheckbox");
        components.add(duplicatesSpecDurationCheckbox);
        duplicatesSpecYearCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecYearCheckbox");
        components.add(duplicatesSpecYearCheckbox);
        duplicatesSpecRatingCheckbox = 
                (Checkbox) dialogSerializer.getNamespace().get("duplicatesSpecRatingCheckbox");
        components.add(duplicatesSpecRatingCheckbox);
        duplicatesButtonBorder = 
                (Border) dialogSerializer.getNamespace().get("duplicatesButtonBorder");
        components.add(duplicatesButtonBorder);
        duplicatesButtonBoxPane = 
                (BoxPane) dialogSerializer.getNamespace().get("duplicatesButtonBoxPane");
        components.add(duplicatesButtonBoxPane);
        duplicatesDoneButton = 
                (PushButton) dialogSerializer.getNamespace().get("duplicatesDoneButton");
        components.add(duplicatesDoneButton);
    }
}
