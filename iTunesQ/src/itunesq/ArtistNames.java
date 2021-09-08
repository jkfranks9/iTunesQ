package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;

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
     * that artists are discovered in the XML file, some types of 
     * artist alternate names cannot be handled as the XML file is read, but
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
                    String autoPrimary = userPrefs.getArtistOverridePrimaryName(displayName,
                            ArtistAlternateNameOverride.OverrideType.AUTOMATIC);
                    if (autoPrimary != null)
                    {
                        artistLogger.debug("found automatic override for '" + displayName
                                + "', primary '" + autoPrimary + "'");
                        break;
                    }
                    
                    /*
                     * Check for an artist manual override. We're looking for the primary artist for an
                     * alternate name.
                     */
                    String primaryName = userPrefs.getArtistOverridePrimaryName(displayName,
                            ArtistAlternateNameOverride.OverrideType.MANUAL);
                    if (primaryName != null)
                    {
                        artistToCheck = primaryName;
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
            artistToCheck = normalizeName(userPrefs.getArtistOverridePrimaryName(displayName,
                    ArtistAlternateNameOverride.OverrideType.MANUAL));
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
}
