package itunesq;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents an artist.
 * 
 * @author Jon
 *
 */
public class Artist
{

    // ---------------- Class variables -------------------------------------

    private String displayName;
    private ArtistNames artistNames;
    private ArtistTrackData artistTrackData;

    /**
     * Class constructor.
     * 
     * @param displayName artist display name
     */
    public Artist(String displayName)
    {
        this.displayName = displayName;
        artistNames = null;
        artistTrackData = new ArtistTrackData();
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the display name. This is the first spelling of the artist we
     * encounter.
     * 
     * @return artist display name
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Gets the artist names object. This contains information about any
     * alternate artist names that are found.
     * 
     * @return artist names object
     */
    public ArtistNames getArtistNames()
    {
        return artistNames;
    }

    /**
     * Sets the artist names object. This contains information about any
     * alternate artist names that are found.
     * 
     * @param artistNames artist names object
     */
    public void setArtistNames(ArtistNames artistNames)
    {
        this.artistNames = artistNames;
    }
    
    /**
     * Gets the artist track data object. This contains information about all
     * tracks for this artist.
     * 
     * @return artist track data object
     */
    public ArtistTrackData getArtistTrackData ()
    {
        return artistTrackData;
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Adds a new track to this artist. This involves updating the track and
     * total time values, as well as updating the list of alternate artist
     * names.
     * 
     * @param track track object
     * @param artistLogger logger to use
     */
    public void addTrackToArtist(Track track, Logger artistLogger)
    {  
        if (track == null)
        {
            throw new IllegalArgumentException("track argument is null");
        }
        
        if (artistLogger == null)
        {
            throw new IllegalArgumentException("artistLogger argument is null");
        }
        
        artistLogger.trace("addTrackToArtist: " + this.hashCode());
        
        String artistName = track.getArtist();

        /*
         * Update the local or remote track counts and total time.
         */
        if (track.getRemote() == false)
        {
            artistTrackData.incrementNumLocalTracks(1);
            artistTrackData.incrementTotalLocalTime(track.getDuration());
        }
        else
        {
            artistTrackData.incrementNumRemoteTracks(1);
            artistTrackData.incrementTotalRemoteTime(track.getDuration());
        }

        /*
         * The artist name could possibly be an alternate, so see if it needs to be saved.
         */
        artistNames.checkAndSaveAlternateName(artistName, track, artistLogger);
    }

    /**
     * Gets a correlator to use with this instance.
     * 
     * @return correlator
     */
    public Integer getCorrelator()
    {
        return this.hashCode();
    }

    /**
     * Returns a hash map of the artist attributes.
     * 
     * @return hash map of the artist attributes
     */
    public HashMap<String, String> toDisplayMap()
    {
        HashMap<String, String> result = new HashMap<String, String>();

        result.put(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), displayName);

        /*
         * Reduce the number of alternate names if there are remote-only artists and remote tracks
         * are not being shown.
         */
        Preferences prefs = Preferences.getInstance();
        boolean showRemoteTracks = prefs.getShowRemoteTracks();

        Map<String, ArtistTrackData> altNames = artistNames.getAltNames();
        int numAltNames = altNames.getCount();

        for (String altName : altNames)
        {
            ArtistTrackData.RemoteArtistControl remoteControl = altNames.get(altName).getRemoteArtistControl();
            if (showRemoteTracks == false && remoteControl == ArtistTrackData.RemoteArtistControl.REMOTE)
            {
                numAltNames--;
            }
        }

        result.put(ArtistDisplayColumns.ColumnNames.NUM_ALTNAMES.getNameValue(),
                Integer.toString(numAltNames));

        result.put(ArtistDisplayColumns.ColumnNames.LOCAL_NUM_TRACKS.getNameValue(),
                Integer.toString(artistTrackData.getNumLocalTracks()));
        result.put(ArtistDisplayColumns.ColumnNames.LOCAL_TOTAL_TIME.getNameValue(),
                Utilities.convertMillisecondTime(artistTrackData.getTotalLocalTime()));
        result.put(ArtistDisplayColumns.ColumnNames.REMOTE_NUM_TRACKS.getNameValue(),
                Integer.toString(artistTrackData.getNumRemoteTracks()));
        result.put(ArtistDisplayColumns.ColumnNames.REMOTE_TOTAL_TIME.getNameValue(),
                Utilities.convertMillisecondTime(artistTrackData.getTotalRemoteTime()));

        return result;
    }
}
