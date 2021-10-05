package itunesq;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents artist names, including the original display name, the
 * normalized name (used internally), and a list of alternate names.
 * 
 * @author Jon
 *
 */
public class ArtistNames
{

    // ---------------- Class variables -------------------------------------

    /*
     * The unaltered artist display name.
     */
    private String displayName;

    /*
     * List of alternate artist names, along with track data for the artist.
     */
    private Map<String, ArtistTrackData> altNames;

    /*
     * Type of alternate name post-processing needed, if any.
     */
    private PostProcessType postProcessType;
    
    /**
     * Type of artist post-processing to be performed. Depending on the order
     * that artists are discovered in the input file, some types of 
     * artist alternate names cannot be handled as the input file is read, but
     * instead require post-processing afterwards.
     */
    public enum PostProcessType
    {
        
        /**
         * no post-processing needed
         */
        NONE("none"),
        
        /**
         * artist contains a "featuring" or "with" tag
         */
        FEATURING("featuring"),
        
        /**
         * artist is part of an artist override
         */
        ARTIST_OVERRIDE("artist override");
        
        private String displayValue;
        
        /*
         * Constructor.
         */
        private PostProcessType (String s)
        {
            displayValue = s;
        }
        
        /**
         * Gets the display value.
         * 
         * @return enum display value
         */
        public String getDisplayValue ()
        {
            return displayValue;
        }
    }

    // ---------------- Private variables -----------------------------------

    /*
     * Various strings used in normalization and matching.
     */
    private static final String THE_STR = "the ";
    private static final String FEATURING_STR = " featuring ";
    private static final String FT_STR = " ft ";
    private static final String FEAT_STR = " feat. ";
    private static final String WITH_STR = " with ";
    private static final String AND_STR = " and ";
    private static final String AMPERSAND_WITH_WHITESPACE_STR = " & ";
    private static final String AMPERSAND_NO_WHITESPACE_STR = "&";
    private static final String AMPERSAND_LEADING_WHITESPACE_STR = " &";
    private static final String AMPERSAND_TRAILING_WHITESPACE_STR = "& ";
    private static final String AMPERSAND_THE_STR = " & the ";

    /*
     * Other private variables.
     */
    private String normalizedName;
    private Preferences userPrefs;

    private static Logger artistLogger = (Logger) LoggerFactory.getLogger(ArtistNames.class.getSimpleName() + "_Artist");

    /**
     * Class constructor.
     * 
     * @param displayName artist display name
     */
    public ArtistNames(String displayName)
    {
        this.displayName = displayName;
        altNames = new HashMap<String, ArtistTrackData>();
        postProcessType = PostProcessType.NONE;
        normalizedName = null;
        userPrefs = Preferences.getInstance();
    }

    // ---------------- Getters and setters ---------------------------------
    
    /**
     * Sets the artist display name.
     * 
     * @param displayName artist display name
     */
    public void setDisplayName (String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Gets the list of alternate artist names.
     * 
     * @return list of alternate artist names
     */
    public Map<String, ArtistTrackData> getAltNames()
    {
        return altNames;
    }

    /**
     * Gets the post-processing type, which may indicate additional
     * post-processing is needed for an alternate name.
     * 
     * @return post-processing type
     */
    public PostProcessType getPostProcessType()
    {
        return postProcessType;
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Initializes logging for static methods. This is called once at application 
     * initialization.
     */
    public static void initializeLogging()
    {
        Logging logging = Logging.getInstance();
        logging.registerLogger(Logging.Dimension.ARTIST, artistLogger);
    }

    /**
     * Normalizes the artist name. This involves:
     * <ul>
     * <li>removing leading "the", including immediately after an 
     * ampersand</li>
     * <li>changing "and" to ampersand</li>
     * <li>normalizing various strings that mean "featuring"</li>
     * <li>ensuring that an ampersand is surrounded by whitespace</li>
     * </ul>
     * <p>
     * The resulting normalized name is much more useful in detecting alternate
     * names.
     * 
     * @return normalized name
     */
    public String normalizeName()
    {

        /*
         * Call the private method to do the work.
         */
        normalizedName = normalizeName(displayName);
        
        return normalizedName;
    }

    /**
     * Matches the artist name against the current list of such names. This
     * includes alternate name detection.
     * 
     * @param artistCorrs list of current artist name correlators
     * @param artistLogger logger to use
     * @return zero or a positive index to indicate the index of the found 
     * name, or a negative index to indicate the negation of the index at 
     * which the artist would be inserted if found
     */
    public int matchArtist(ArrayList<ArtistCorrelator> artistCorrs, Logger artistLogger)
    {  
        if (artistCorrs == null)
        {
            throw new IllegalArgumentException("artistCorrs argument is null");
        }
        
        if (artistLogger == null)
        {
            throw new IllegalArgumentException("artistLogger argument is null");
        }

        artistLogger.trace("matchArtist: " + this.hashCode());
        
        /*
         * If we have a diag trigger artist value, allow for a breakpoint if the artist matches.
         */
        if (MainWindow.getDiagTrigger() == MainWindow.DiagTrigger.ARTIST)
        {
            if (displayName.toLowerCase().contains(MainWindow.getDiagTriggerValue()))
            {
                artistLogger.info("matchArtist diag trigger hit!");
            }
        }
        
        /*
         * Loop control variables.
         */
        int controlVal = 0;
        final int loopControlArtistOverride = ++controlVal;
        final int loopControlFeatOrWith     = ++controlVal;
        final int loopControlSwitch         = ++controlVal;

        /*
         * Protect the normalized name from modification.
         */
        String artistToCheck = new String(normalizedName);

        /*
         * Loop until we find a match or the matching rules are exhausted. By normalizing the artist
         * name we've already taken care of the following:
         * 
         * - remove leading "the" (including from second artist if any after "&")
         * - normalize "featuring" strings to the common "feat."
         * 
         * That leaves the following left to do:
         * 
         * - handle any artist overrides
         * - scan for "feat." and remove the substring, so that say, "ABC feat. Q" matches "ABC"
         * - scan for "with" and remove the substring, so that say, "XYZ with Amy" matches "XYZ"
         * - switch the terms around if we have an "&", so that say, "ABC & XYZ" matches "XYZ & ABC"
         * 
         * NOTE: I'm making the assumption that a given artist can't have both "feat." and "with". I
         * wouldn't know how to handle that anyway.
         */
        int index = -1;
        int loopControl = 0;
        boolean performSearch = true;
        String modifiedArtist = null;
        
        while (index < 0)
        {
            ArtistCorrelator searchCorr = new ArtistCorrelator();

            /*
             * Look up the name in the list.
             */
            if (performSearch == true)
            {
                searchCorr.setNormalizedName(artistToCheck);
                index = ArrayList.binarySearch(artistCorrs, searchCorr, artistCorrs.getComparator());

                if (index < 0)
                {
                    artistLogger.debug("artist '" + artistToCheck + "' not found in existing list");
                }
            }

            /*
             * Reset this control flag for the next iteration.
             */
            performSearch = true;

            /*
             * Continue if not found. If it was found, we're done.
             */
            if (index < 0)
            {
                int targetIdx;

                /*
                 * The loop control ensures we progress through the various checks without duplication. 
                 */
                if (loopControl < loopControlArtistOverride)
                {
                    loopControl = loopControlArtistOverride;
                    
                    /*
                     * If an automatic artist override exists, we do NOT want this artist to be an
                     * alternate for the primary from the override, so just return the negative index.
                     * 
                     * NOTE: this means we never do post-processing for automatic overrides.
                     */
                    ArtistAlternateNameOverride autoOverride = userPrefs.getArtistOverride(displayName,
                            ArtistAlternateNameOverride.OverrideType.AUTOMATIC);
                    if (autoOverride != null)
                    {
                        artistLogger.debug("found automatic override for '" + displayName
                                + "', primary '" + autoOverride.getPrimaryArtist() + "'");
                        break;
                    }
                    
                    /*
                     * Check for an artist manual override. We're looking for the primary artist for an
                     * alternate name.
                     */
                    ArtistAlternateNameOverride manualOverride = userPrefs.getArtistOverride(displayName,
                            ArtistAlternateNameOverride.OverrideType.MANUAL);
                    if (manualOverride != null)
                    {
                        ArtistNames temp = new ArtistNames(manualOverride.getPrimaryArtist());
                        artistToCheck = new String(temp.normalizeName());
                        artistLogger.debug("now checking for '" + artistToCheck + "' (artist override)");

                        /*
                         * Set the post-processing type to "artist override" for potential post-processing.
                         */
                        postProcessType = PostProcessType.ARTIST_OVERRIDE;
                    }

                    /*
                     * The name doesn't match the above checks, so make sure we don't search again
                     * on the next iteration, since we made no changes to the name.
                     */
                    else
                    {
                        performSearch = false;
                    }
                }
                else if (loopControl < loopControlFeatOrWith)
                {
                    loopControl = loopControlFeatOrWith;

                    /*
                     * Check for names containing "feat." or "with", and modify the name to check by
                     * removing such substrings from the name.
                     */
                    if ((targetIdx = artistToCheck.indexOf(FEAT_STR)) >= 0
                            || (targetIdx = artistToCheck.indexOf(WITH_STR)) >= 0)
                    {
                        modifiedArtist = artistToCheck.substring(0, targetIdx).trim();
                        artistToCheck = modifiedArtist;
                        artistLogger.debug("now checking for '" + artistToCheck + "'");

                        /*
                         * Set the post-processing type to "featuring" for potential post-processing.
                         */
                        postProcessType = PostProcessType.FEATURING;
                    }

                    /*
                     * The name doesn't match the above checks, so make sure we don't search again
                     * on the next iteration, since we made no changes to the name.
                     */
                    else
                    {
                        performSearch = false;
                    }
                }
                else if (loopControl < loopControlSwitch)
                {
                    loopControl = loopControlSwitch;

                    /*
                     * Check for names containing "&", and modify the name to check by switching the
                     * terms in the name.
                     */
                    artistToCheck = new String(normalizedName);
                    if ((targetIdx = artistToCheck.indexOf(AMPERSAND_WITH_WHITESPACE_STR)) >= 0)
                    {
                        String[] amperSplit = artistToCheck.split(AMPERSAND_WITH_WHITESPACE_STR);
                        StringBuilder changed = new StringBuilder();
                        changed.append(amperSplit[1].trim());
                        changed.append(AMPERSAND_WITH_WHITESPACE_STR);
                        changed.append(amperSplit[0].trim());
                        modifiedArtist = changed.toString();
                        artistToCheck = modifiedArtist;
                        artistLogger.debug("now checking for '" + artistToCheck + "'");
                    }

                    /*
                     * The name doesn't match the above checks, so make sure we don't search again
                     * on the next iteration, since we made no changes to the name.
                     */
                    else
                    {
                        performSearch = false;
                    }
                }
                else
                {
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Checks an artist with a post-processing type to see if the primary
     * artist can be located. If so, then this artist becomes an alternate 
     * name for the primary.
     * 
     * @param artistNames list of all artist name correlators
     * @return zero or a positive index to indicate the index of the found 
     * name, or a negative index to indicate the negation of the index at 
     * which the artist would be inserted if found
     */
    public int checkPostProcessType(ArrayList<ArtistCorrelator> artistNames)
    {
        if (artistNames == null)
        {
            throw new IllegalArgumentException("artistNames argument is null");
        }
        
        int index = -1;
        String artistToCheck = null;

        /*
         * Handle the different types of required post-processing.
         */
        switch (postProcessType)
        {
        
        /*
         * This artist (the alternate) contains the "featuring" or "with" tag, so remove the tag
         * in order to locate the correct primary.
         */
        case FEATURING:
            int targetIdx = normalizedName.indexOf(FEAT_STR);
            if (targetIdx == -1)
            {
                targetIdx = normalizedName.indexOf(WITH_STR);
            }
            if (targetIdx >= 0)
            {
                artistToCheck = normalizedName.substring(0, targetIdx).trim();
            }
            break;

        /*
         * This artist is an artist override type, so we need to get the associated primary.
         * 
         * NOTE: We never do post-processing for automatic overrides. As we're building the
         * database, if we find an automatic override we just treat the artist as unique.
         */    
        case ARTIST_OVERRIDE:
        	ArtistAlternateNameOverride manualOverride = userPrefs.getArtistOverride(displayName,
                    ArtistAlternateNameOverride.OverrideType.MANUAL);
            ArtistNames temp = new ArtistNames(manualOverride.getPrimaryArtist());
            artistToCheck = new String(temp.normalizeName());
            break;
            
        case NONE:
            break;
          
        default:
            throw new InternalErrorException(true, "unknown post-process type '" + postProcessType + "'");
        }

        /*
         * We should always have a name to check, but don't crash if we don't for some reason.
         */
        if (artistToCheck != null)
        {

            /*
             * Now try to find the primary artist.
             */
            ArtistCorrelator searchCorr = new ArtistCorrelator();
            searchCorr.setNormalizedName(artistToCheck);
            index = ArrayList.binarySearch(artistNames, searchCorr, artistNames.getComparator());
        }

        return index;
    }

    /**
     * Checks if an artist name is an alternate name and saves it in the list 
     * if so.
     * 
     * @param artistName artist name to check
     * @param track track object that references this artist
     * @param artistLogger logger to use
     */
    public void checkAndSaveAlternateName(String artistName, Track track, Logger artistLogger)
    {
        if (artistName == null)
        {
            throw new IllegalArgumentException("artistName argument is null");
        }
        
        if (track == null)
        {
            throw new IllegalArgumentException("track argument is null");
        }
        
        if (artistLogger == null)
        {
            throw new IllegalArgumentException("artistLogger argument is null");
        }

        artistLogger.trace("checkAndSaveAlternateName: " + this.hashCode());
        
        /*
         * If we have a diag trigger artist value, allow for a breakpoint if the artist matches.
         */
        if (MainWindow.getDiagTrigger() == MainWindow.DiagTrigger.ARTIST)
        {
            if (displayName.toLowerCase().contains(MainWindow.getDiagTriggerValue()))
            {
                artistLogger.info("checkAndSaveAlternateName diag trigger hit!");
            }
        }

        /*
         * We have nothing to do if the name matches the display name.
         */
        if (!artistName.equals(displayName))
        {
            boolean foundName = false;

            /*
             * Check if the name already exists in the list of alternates.
             */
            for (String altName : altNames)
            {
                if (artistName.equals(altName))
                {
                    foundName = true;
                    break;
                }
            }

            /*
             * Increment the track and time totals.
             */
            if (foundName == true)
            {
                ArtistTrackData artistTrackData = altNames.get(artistName);
                artistTrackData.incrementNumTracks(1);
                artistTrackData.incrementTotalTime(track.getDuration());
            }

            /*
             * If this is a new alternate spelling, add it to the list.
             */
            else
            {
                
                /*
                 * Create a new track data object and initialize the track and time totals.
                 */
                ArtistTrackData artistTrackData = new ArtistTrackData();
                artistTrackData.setNumTracks(1);
                artistTrackData.setTotalTime(track.getDuration());
                
                /*
                 * Add the new alternate artist with its updated track data.
                 */
                altNames.put(artistName, artistTrackData);
                artistLogger.debug("added alternate artist '" + artistName  + "' to '" + displayName + "'");
            }
        }
    }

    /**
     * Adds an alternate artist name to this artist.
     * 
     * @param artistName alternate artist name to be added
     * @param artistTrackData artist track data object
     * @param artistLogger logger to use
     */
    public void addAlternateName(String artistName, ArtistTrackData artistTrackData, Logger artistLogger)
    {
        if (artistName == null)
        {
            throw new IllegalArgumentException("artistName argument is null");
        }

        if (artistTrackData == null)
        {
            throw new IllegalArgumentException("artistTrackData argument is null");
        }
        
        if (artistLogger == null)
        {
            throw new IllegalArgumentException("artistLogger argument is null");
        }

        artistLogger.trace("addAlternateName: " + this.hashCode());
        
        altNames.put(artistName, artistTrackData);
        artistLogger.debug("added alternate artist '" + artistName + "' to '" + displayName + "'");
    }
    
    /**
     * Performs post-processing on artists after all tracks have been read
     * from the input file.
     */
    public static void postProcessArtists()
    {

        /*
         * Now that the tracks (and artists) are all created, post-process the artists
         * to try and find additional alternate names.
         */
        artistLogger.info("looking for post-process artist alternate names");
        lookForArtistAlternateNames();
        
        /*
         * Verify that all artists contained in alternate name overrides still exist.
         */
        artistLogger.info("verifying artist alternate name overrides");
        verifyArtistOverrides();
    }

    /**
     * Transfers an artist as an alternate name to a primary artist.
     * 
     * @param altArtistCorr artist correlator object for the alternate artist
     * @param primaryIdx index into the list of artist correlators for the 
     * primary artist
     * @param artistCorrelatorsIter iterator for the list of artist correlators
     * to be used to remove the alternate artist
     */
    public static void transferArtistToPrimary(ArtistCorrelator altArtistCorr, int primaryIdx,
            Iterator<ArtistCorrelator> artistCorrelatorsIter)
    {
        artistLogger.trace("transferArtistToPrimary (with iterator)");
        
        if (altArtistCorr == null)
        {
            throw new IllegalArgumentException("altArtistCorr argument is null");
        }
        
        if (primaryIdx < 0)
        {
            throw new IllegalArgumentException("primaryIdx argument is negative");
        }
        
        if (artistCorrelatorsIter == null)
        {
            throw new IllegalArgumentException("artistCorrelatorsIter argument is null");
        }
        
        /*
         * Call the implementation method.
         */
        transferArtistToPrimaryImpl(altArtistCorr, primaryIdx);
        
        /*
         * Delete the now-alternate name from the artist correlators list.
         */
        artistCorrelatorsIter.remove();
    }

    /**
     * Transfers an artist as an alternate name to a primary artist.
     * 
     * @param altArtistCorr artist correlator object for the alternate artist
     * @param primaryIdx index into the list of artist correlators for the 
     * primary artist
     * @param altIdx index into the list of artist correlators to be used to 
     * remove the alternate artist 
     */
    public static void transferArtistToPrimary(ArtistCorrelator altArtistCorr, int primaryIdx, int altIdx)
    {
        artistLogger.trace("transferArtistToPrimary (with index)");
        
        if (altArtistCorr == null)
        {
            throw new IllegalArgumentException("altArtistCorr argument is null");
        }
        
        if (primaryIdx < 0)
        {
            throw new IllegalArgumentException("primaryIdx argument is negative");
        }
        
        if (altIdx < 0)
        {
            throw new IllegalArgumentException("altIdx argument is negative");
        }
        
        /*
         * Call the implementation method.
         */
        transferArtistToPrimaryImpl(altArtistCorr, primaryIdx);
        
        /*
         * Delete the now-alternate name from the artist correlators list.
         */
        Database.getArtistCorrelators().remove(altIdx, 1);
    }
    
    /**
     * Transfers an artist from a primary artist (as an alternate) to a 
     * standalone artist.
     * 
     * @param primaryArtist primary artist name
     * @param altArtist alternate artist name
     */
    public static void transferArtistFromPrimary (String primaryArtist, String altArtist)
    {
        artistLogger.trace("transferArtistFromPrimary");
        
        /*
         * Step 1 is to create the appropriate objects and add the alternate artist to the 
         * artistCorrelators and artists lists ...
         */
        
        /*
         * Access primary artist objects.
         */
        Map<Integer, Artist> artists = Database.getArtists();
        ArtistCorrelator primaryArtistCorr = Database.findArtistCorrelator(primaryArtist);
        Artist primaryArtistObj = artists.get(primaryArtistCorr.getArtistKey());
        ArtistNames primaryArtistNames = primaryArtistObj.getArtistNames();

        /*
         * Create the alternate artist names object.
         */
        ArtistNames altArtistNames = new ArtistNames(altArtist);

        /*
         * Create and add the alternate artist correlator object.
         */
        ArtistCorrelator altArtistCorr = new ArtistCorrelator(altArtist);
        altArtistCorr.setNormalizedName(altArtistNames.normalizeName());
        Database.getArtistCorrelators().add(altArtistCorr);

        /*
         * Create the alternate artist object and attach the artist names object.
         */
        Artist altArtistObj = new Artist(altArtist);
        altArtistObj.setArtistNames(altArtistNames);
        
        /*
         * Create the correlator key and add the artist object to the list
         */
        Integer correlator = altArtistObj.getCorrelator();
        altArtistCorr.setArtistKey(correlator);
        artists.put(correlator, altArtistObj);
        
        /*
         * Step 2 is to remove the alternate artist from the primary's list of alternate names. 
         * This returns the alternate artist track data representing the alternate. 
         */
        ArtistTrackData altTrackData = primaryArtistNames.getAltNames().remove(altArtist);
        
        /*
         * Step 3 is to update the track counts and times for both the primary artist (by decrementing 
         * the data for the removed alternate), and the alternate artist (by using the artist track 
         * data retrieved above).
         */
        primaryArtistObj.getArtistTrackData().decrementNumTracks(altTrackData.getNumTracks());
        primaryArtistObj.getArtistTrackData().decrementTotalTime(altTrackData.getTotalTime());
        
        altArtistObj.getArtistTrackData().setNumTracks(altTrackData.getNumTracks());
        altArtistObj.getArtistTrackData().setTotalTime(altTrackData.getTotalTime());
    }
    
    /*
     * Normalize the name. This private method (used only by us, of course) is called by the public
     * method that doesn't have an argument.
     */
    private String normalizeName(String displayName)
    {
        
        /*
         * If we have a diag trigger artist value, allow for a breakpoint if the artist matches.
         */
        if (MainWindow.getDiagTrigger() == MainWindow.DiagTrigger.ARTIST)
        {
            if (displayName.toLowerCase().contains(MainWindow.getDiagTriggerValue()))
            {
                for (int i = 0; i < 1; i++)
                {
                	i++;
                }
            }
        }
        String normalizedName = new String(displayName.toLowerCase().trim());
        int idx;

        /*
         * Remove leading "the" if it exists.
         */
        if (normalizedName.startsWith(THE_STR))
        {
            normalizedName = normalizedName.substring(THE_STR.length());
        }

        /*
         * Change a single "and" within the name to "&".
         */
        idx = normalizedName.indexOf(AND_STR);
        if (idx >= 0 && normalizedName.lastIndexOf(AND_STR) == idx)
        {
            normalizedName = normalizedName.replace(AND_STR, AMPERSAND_WITH_WHITESPACE_STR);
        }

        /*
         * Change a single "featuring" within the name to "feat.".
         */
        idx = normalizedName.indexOf(FEATURING_STR);
        if (idx >= 0 && normalizedName.lastIndexOf(FEATURING_STR) == idx)
        {
            normalizedName = normalizedName.replace(FEATURING_STR, FEAT_STR);
        }

        /*
         * Change a single "ft" within the name to "feat.".
         */
        idx = normalizedName.indexOf(FT_STR);
        if (idx >= 0 && normalizedName.lastIndexOf(FT_STR) == idx)
        {
            normalizedName = normalizedName.replace(FT_STR, FEAT_STR);
        }

        /*
         * If the name contains a single ampersand, ensure it's surrounded by whitespace.
         */
        idx = normalizedName.indexOf(AMPERSAND_NO_WHITESPACE_STR);
        if (idx >= 0 && normalizedName.lastIndexOf(AMPERSAND_NO_WHITESPACE_STR) == idx)
        {

            /*
             * Continue if the ampersand is not surrounded with whitespace.
             */
            if (normalizedName.indexOf(AMPERSAND_WITH_WHITESPACE_STR) == -1)
            {

                /*
                 * Replace just leading whitespace with full whitespace.
                 */
                if (normalizedName.indexOf(AMPERSAND_LEADING_WHITESPACE_STR) >= 0)
                {
                    normalizedName = 
                            normalizedName.replace(AMPERSAND_LEADING_WHITESPACE_STR, 
                                    AMPERSAND_WITH_WHITESPACE_STR);
                }

                /*
                 * Replace just trailing whitespace with full whitespace.
                 */
                else if (normalizedName.indexOf(AMPERSAND_TRAILING_WHITESPACE_STR) >= 0)
                {
                    normalizedName = 
                            normalizedName.replace(AMPERSAND_TRAILING_WHITESPACE_STR, 
                                    AMPERSAND_WITH_WHITESPACE_STR);
                }

                /*
                 * Must have no whitespace at all, so replace with full whitespace.
                 */
                else
                {
                    normalizedName =
                            normalizedName.replace(AMPERSAND_NO_WHITESPACE_STR, 
                                    AMPERSAND_WITH_WHITESPACE_STR);
                }
            }
        }

        /*
         * If the name contains a single ampersand, remove "the" if it immediately follows.
         */
        idx = normalizedName.indexOf(AMPERSAND_THE_STR);
        if (idx >= 0 && normalizedName.lastIndexOf(AMPERSAND_THE_STR) == idx)
        {
            normalizedName = normalizedName.replace(AMPERSAND_THE_STR, 
                    AMPERSAND_WITH_WHITESPACE_STR);
        }

        return normalizedName;
    }

    /*
     * As we collected tracks and artists, we were able to find alternate artist names like
     * the following:
     * 
     *   ABC
     *   ABC Featuring Q
     * 
     * But only if the artists were found in the above order. If the name with the "featuring"
     * tag was found first, then we didn't try looking when artist ABC was found, the reason 
     * being it would be very inefficient to check all artists in such a way for a fringe case.
     * 
     * However, such an artist was flagged in the ArtistNames object. So here we run the artist
     * list looking for those flags and trying to find the primary artist. If so, we then process
     * the name with the "featuring" tag as an alternate for the primary.
     * 
     * Similar logic is used for artist overrides set by the user. We have the same 
     * out of order problem detailed above.
     */
    private static void lookForArtistAlternateNames ()
    {
        artistLogger.trace("lookForArtistAlternateNames");

        /*
         * Loop through the correlator list. We need to use an iterator instead of a foreach
         * so we can safely remove correlator items while looping.
         */
        ArrayList<ArtistCorrelator> artistCorrelators = Database.getArtistCorrelators();
        Iterator<ArtistCorrelator> artistCorrelatorsIter = artistCorrelators.iterator();
        while (artistCorrelatorsIter.hasNext())
        {
            ArtistCorrelator artistCorr = artistCorrelatorsIter.next();

            Artist artistObj = Database.getArtists().get(artistCorr.getArtistKey());
            ArtistNames artistNames = artistObj.getArtistNames();

            /*
             * If the post-processing type is set, try to find the associated primary artist.
             */
            ArtistNames.PostProcessType postProcessType = artistNames.getPostProcessType();
            if (postProcessType != ArtistNames.PostProcessType.NONE)
            {
                artistLogger.debug("artist name '" + artistCorr.getDisplayName() + "', normalized '"
                        + artistCorr.getNormalizedName() + "', post-processing type '"
                        + postProcessType.getDisplayValue() + "'");

                int index = artistNames.checkPostProcessType(artistCorrelators);

                /*
                 * If we found the primary artist then transfer this alternate to it, and remove
                 * this alternate from the correlators list.
                 */
                if (index >= 0)
                {
                    transferArtistToPrimary(artistCorr, index, artistCorrelatorsIter);
                }
            }
        }
    }
    
    /*
     * Verify that artist names contained in any artist overrides are still valid. The 
     * input file could have been changed or updated. Fix the overrides for any invalid ones found.
     */
    private static void verifyArtistOverrides ()
    {
        artistLogger.trace("verifyArtistOverrides");
        
        /*
         * Get the user preferences.
         */
        Preferences userPrefs = Preferences.getInstance();
        
        /*
         * We need a search correlator to look up artists in the database.
         */
        ArtistCorrelator searchCorr = new ArtistCorrelator();
        
        /*
         * Get the artist overrides from the preferences.
         */
        List<ArtistAlternateNameOverride> artistOverrides = userPrefs.getArtistOverrides();
        boolean prefsUpdated = false;
        
        /*
         * Loop through all overrides to verify the artists still exist.
         */
        Iterator<ArtistAlternateNameOverride> artistOverridesIter = artistOverrides.iterator();
        while (artistOverridesIter.hasNext())
        {
            ArrayList<ArtistCorrelator> artistCorrelators = Database.getArtistCorrelators();
            ArtistAlternateNameOverride override = artistOverridesIter.next();
            
            int primaryIdx;
            String primaryArtist = override.getPrimaryArtist();

            /*
             * The primary artists always exist in the database, so we search for them there.
             */
            ArtistNames primaryTemp = new ArtistNames(primaryArtist);
            searchCorr.setNormalizedName(primaryTemp.normalizeName());
            
            /*
             * If the primary from the override doesn't exist in the database, remove the entire
             * override and iterate the loop.
             */
            if ((primaryIdx = ArrayList.binarySearch(artistCorrelators, 
                    searchCorr, artistCorrelators.getComparator())) < 0)
            {
                artistLogger.debug("found invalid primary artist " + primaryArtist);
                
                artistOverridesIter.remove();
                prefsUpdated = true;
                continue;
            }

            /*
             * The primary from the override exists in the database. Check the alternates from the 
             * override.
             */
            List<String> alternateArtists = override.getAlternateArtists();
            
            /*
             * If for some reason we have no alternate artists, remove the entire override.
             */
            if (alternateArtists.getLength() == 0)
            {
                artistLogger.debug("found empty primary artist " + primaryArtist);
                
                artistOverridesIter.remove();
                prefsUpdated = true;
                continue;
            }
            
            Iterator<String> alternateArtistsIter = alternateArtists.iterator();
            while (alternateArtistsIter.hasNext())
            {
                String alternateArtist = alternateArtistsIter.next();
                
                /*
                 * Look for this alternate in the primary's alternate list if the override type is
                 * manual.
                 */
                boolean altInvalid = false;
                if (override.getOverrideType() == ArtistAlternateNameOverride.OverrideType.MANUAL)
                {
                    ArtistCorrelator primaryCorr = artistCorrelators.get(primaryIdx);
                    Artist primaryArtistObj = Database.getArtists().get(primaryCorr.getArtistKey());
                    ArtistNames primaryNames = primaryArtistObj.getArtistNames();
                    Map<String, ArtistTrackData> altNames = primaryNames.getAltNames();

                    /*
                     * If the alternate from the override doesn't exist, flag it.
                     */
                    if (!altNames.containsKey(alternateArtist))
                    {
                        altInvalid = true;
                    }
                }
                
                /*
                 * Look for this alternate in the database if the override type is automatic.
                 */
                else
                {
                    ArtistNames altTemp = new ArtistNames(alternateArtist);
                    searchCorr.setNormalizedName(altTemp.normalizeName());

                    /*
                     * If the alternate from the override doesn't exist, flag it.
                     */
                    if (ArrayList.binarySearch(artistCorrelators, searchCorr, 
                            artistCorrelators.getComparator()) < 0)
                    {
                        altInvalid = true;
                    }
                }
                
                /*
                 * If the alternate is invalid remove it from the override.
                 */
                if (altInvalid == true)
                {
                    artistLogger.debug("found invalid alternate artist " + alternateArtist);

                    alternateArtistsIter.remove();
                    prefsUpdated = true;
                    
                    /*
                     * Remove the entire override if it's now empty.
                     */
                    if (alternateArtists.getLength() == 0)
                    {
                        artistOverridesIter.remove();
                    }
                }
            }
        }
        
        /*
         * Write the user preferences if the overrides were updated.
         */
        if (prefsUpdated == true)
        {
            try
            {
                userPrefs.writePreferences();
            }
            catch (IOException e)
            {
                MainWindow.logException(artistLogger, e);
                throw new InternalErrorException(true, e.getMessage());
            }
        }
    }
    
    /*
     * This is the implementation method for transferring an alternate artist to a primary. There
     * are multiple public methods with differing parameters for deleting the alternate artist
     * correlator, but the bulk of the logic (this method) is identical.
     */
    private static void transferArtistToPrimaryImpl (ArtistCorrelator altArtistCorr, int primaryIdx)
    {
        artistLogger.trace("transferArtistToPrimaryImpl");

        /*
         * Access the objects we need.
         */
        ArrayList<ArtistCorrelator> artistCorrelators = Database.getArtistCorrelators();
        Map<Integer, Artist> artists = Database.getArtists();
        
        Artist altArtistObj = artists.get(altArtistCorr.getArtistKey());

        ArtistCorrelator primaryArtistCorr = artistCorrelators.get(primaryIdx);
        Artist primaryArtistObj = artists.get(primaryArtistCorr.getArtistKey());
        ArtistNames primaryArtistNames = primaryArtistObj.getArtistNames();

        /*
         * Add the alternate name to the primary artist.
         */
        primaryArtistNames.addAlternateName(altArtistObj.getDisplayName(), 
                altArtistObj.getArtistTrackData(), artistLogger);

        /*
         * Update the primary artist counts and times.
         */
        int updatedValue = primaryArtistObj.getArtistTrackData().getNumTracks() + 
                altArtistObj.getArtistTrackData().getNumTracks();
        primaryArtistObj.getArtistTrackData().setNumTracks(updatedValue);
        updatedValue = primaryArtistObj.getArtistTrackData().getTotalTime() + 
                altArtistObj.getArtistTrackData().getTotalTime();
        primaryArtistObj.getArtistTrackData().setTotalTime(updatedValue);

        /*
         * Delete the now-alternate name from the artist list.
         */
        artists.remove(altArtistCorr.getArtistKey());
    }
}
