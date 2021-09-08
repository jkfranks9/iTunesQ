package itunesq;

import java.util.Comparator;
import java.util.Date;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;

/**
 * Class that represents a single track.
 * 
 * @author Jon
 *
 */
public class Track
{

    // ---------------- Class variables -------------------------------------

    /*
     * Track attributes.
     */
    private int trkID;
    private String trkName;
    private String trkArtist;
    private String trkComposer;
    private String trkAlbum;
    private String trkGenre;
    private String trkKind;
    private int trkSize;
    private int trkDuration;
    private int trkYear;
    private Date trkModified;
    private Date trkDateAdded;
    private int trkBitRate;
    private int trkSampleRate;
    private int trkPlayCount;
    private Date trkReleased;
    private int trkRating;
    private List<TrackPlaylistInfo> trkPlaylists;
    private TrackType trackType;
    
    /**
     * The type of track, for example audio or video.
     */
    public enum TrackType
    {
    	
    	/**
    	 * the type of track is not known
    	 */
    	UNKNOWN,
    	
    	/**
    	 * an audio-only track
    	 */
        AUDIO,
        
        /**
         * a video track
         */
        VIDEO;
    }

    // ---------------- Private variables -----------------------------------

    /*
     * The rating value is the number of stars times 20, for some odd reason.
     */
    private static final int RATING_DIVISOR = 20;

    /**
     * Class constructor.
     * 
     * @param ID track ID
     */
    public Track(int ID)
    {
        trkID = ID;
        trkPlaylists = new ArrayList<TrackPlaylistInfo>();

        /*
         * Set a comparator so that when we display the playlists for a track they are sorted.
         */
        trkPlaylists.setComparator(new Comparator<TrackPlaylistInfo>()
        {
            @Override
            public int compare(TrackPlaylistInfo c1, TrackPlaylistInfo c2)
            {
                return c1.compareTo(c2);
            }
        });
    }

    // ---------------- Getters and setters ---------------------------------

    /**
     * Gets the track ID.
     * 
     * @return track ID
     */
    public int getID()
    {
        return trkID;
    }

    /**
     * Gets the track name.
     * 
     * @return track name
     */
    public String getName()
    {
        return trkName;
    }

    /**
     * Sets the track name.
     * 
     * @param name track name
     */
    public void setName(String name)
    {
        this.trkName = name;
    }

    /**
     * Gets the artist name.
     * 
     * @return artist name
     */
    public String getArtist()
    {
        return trkArtist;
    }

    /**
     * Sets the artist name.
     * 
     * @param artist artist name
     */
    public void setArtist(String artist)
    {
        this.trkArtist = artist;
    }

    /**
     * Sets the composer name.
     * 
     * @param composer composer name
     */
    public void setComposer(String composer)
    {
        this.trkComposer = composer;
    }

    /**
     * Gets the album name.
     * 
     * @return album name
     */
    public String getAlbum()
    {
        return trkAlbum;
    }

    /**
     * Sets the album name.
     * 
     * @param album album name
     */
    public void setAlbum(String album)
    {
        this.trkAlbum = album;
    }

    /**
     * Sets the genre.
     * 
     * @param genre genre
     */
    public void setGenre(String genre)
    {
        this.trkGenre = genre;
    }

    /**
     * Gets the kind of track.
     * 
     * @return kind of track
     */
    public String getKind()
    {
        return trkKind;
    }

    /**
     * Sets the kind of track.
     * 
     * @param kind kind of track
     */
    public void setKind(String kind)
    {
        this.trkKind = kind;
    }

    /**
     * Sets the size of the track in bytes.
     * 
     * @param size size of the track
     */
    public void setSize(int size)
    {
        this.trkSize = size;
    }

    /**
     * Gets the duration of the track.
     * 
     * @return duration of the track
     */
    public int getDuration()
    {
        return trkDuration;
    }

    /**
     * Sets the duration of the track.
     * 
     * @param duration duration of the track
     */
    public void setDuration(int duration)
    {
        this.trkDuration = duration;
    }

    /**
     * Gets the year of release.
     * 
     * @return year of release
     */
    public int getYear()
    {
        return trkYear;
    }

    /**
     * Sets the year of release.
     * 
     * @param year year of release
     */
    public void setYear(int year)
    {
        this.trkYear = year;
    }

    /**
     * Sets the modification date.
     * 
     * @param modified modification date
     */
    public void setModified(Date modified)
    {
        this.trkModified = modified;
    }

    /**
     * Sets the date the track was added.
     * 
     * @param dateAdded date the track was added
     */
    public void setDateAdded(Date dateAdded)
    {
        this.trkDateAdded = dateAdded;
    }

    /**
     * Sets the bit rate.
     * 
     * @param bitRate bit rate
     */
    public void setBitRate(int bitRate)
    {
        this.trkBitRate = bitRate;
    }

    /**
     * Sets the sample rate.
     * 
     * @param sampleRate sample rate
     */
    public void setSampleRate(int sampleRate)
    {
        this.trkSampleRate = sampleRate;
    }

    /**
     * Sets the play count.
     * 
     * @param playCount play count
     */
    public void setPlayCount(int playCount)
    {
        this.trkPlayCount = playCount;
    }

    /**
     * Sets the release date.
     * 
     * @param released release date
     */
    public void setReleased(Date released)
    {
        this.trkReleased = released;
    }

    /**
     * Gets the corrected track rating. This is the rating on a scale of 0 to 5.
     * 
     * @return corrected track rating
     */
    public int getCorrectedRating()
    {
        return trkRating / RATING_DIVISOR;
    }

    /**
     * Sets the track rating.
     * 
     * @param rating track rating
     */
    public void setRating(int rating)
    {
        this.trkRating = rating;
    }

    /**
     * Get the list of playlist info objects.
     * 
     * @return list of playlist info objects
     */
    public List<TrackPlaylistInfo> getPlaylists()
    {
        return trkPlaylists;
    }
    
    /**
     * Gets the track type.
     * 
     * @return track type
     */
    public TrackType getTrackType()
    {
    	return trackType;
    }
    
    /**
     * Sets the track type.
     * 
     * @param trackType track type
     */
    public void setTrackType(TrackType trackType)
    {
    	this.trackType = trackType;
    }

    // ---------------- Public methods --------------------------------------

    /**
     * Gets the track non-bypassed playlist count.
     * 
     * @return track non-bypassed playlist count
     */
    public int getNonBypassedPlaylistCount()
    {
        int playlistCount = 0;

        for (TrackPlaylistInfo playlistInfo : trkPlaylists)
        {
            if (playlistInfo.getBypassed() == false)
            {
                playlistCount++;
            }
        }

        return playlistCount;
    }

    /**
     * Clears the track playlist info.
     */
    public void clearPlaylistInfo()
    {
        trkPlaylists.clear();
    }

    /**
     * Adds playlist info to the track.
     * 
     * @param playlistInfo playlist info
     */
    public void addPlaylistInfoToTrack(TrackPlaylistInfo playlistInfo)
    {
        int index = 0;
        boolean playlistFound = false;

        /*
         * Check if we already have a playlist info for the input playlist. This can happen
         * if the bypass preferences are updated.
         */
        for (TrackPlaylistInfo trackPlaylistInfo : trkPlaylists)
        {
            if (trackPlaylistInfo.getPlaylistName().equals(playlistInfo.getPlaylistName()))
            {
                playlistFound = true;
                break;
            }

            index++;
        }

        /*
         * Add or update the playlist info.
         */
        if (playlistFound == false)
        {
            trkPlaylists.add(playlistInfo);
        }
        else
        {
            trkPlaylists.update(index, playlistInfo);
        }
    }

    /**
     * Compares a given track to this track, used for sorting. We sort by track
     * name.
     * 
     * @param t2 track to be compared to this track
     * @return negative value, zero, or positive value to indicate less than,
     * equal to, or greater than, respectively
     */
    public int compareTo(Track t2)
    {
    	
    	/*
    	 * Ignore leading "The" (case insensitive).
    	 */
    	String thisName = this.trkName.replaceAll("^(?i)The ", "");
    	String thatName = t2.trkName.replaceAll("^(?i)The ", "");
    	
        return thisName.toLowerCase().compareTo(thatName.toLowerCase());
    }

    /**
     * Returns a hash map of the track attributes.
     * 
     * @param trackNum track number to include, if greater than 0
     * @return hash map of the track attributes
     */
    public HashMap<String, String> toDisplayMap(int trackNum)
    {
        HashMap<String, String> result = new HashMap<String, String>();

        if (trackNum > 0)
        {
            result.put(TrackDisplayColumns.ColumnNames.NUMBER.getNameValue(),
                    Integer.toString(trackNum));
        }
        result.put(TrackDisplayColumns.ColumnNames.ID.getNameValue(), Integer.toString(trkID));
        result.put(TrackDisplayColumns.ColumnNames.NAME.getNameValue(), trkName);
        result.put(TrackDisplayColumns.ColumnNames.ARTIST.getNameValue(), trkArtist);
        result.put(TrackDisplayColumns.ColumnNames.COMPOSER.getNameValue(), trkComposer);
        result.put(TrackDisplayColumns.ColumnNames.ALBUM.getNameValue(), trkAlbum);
        result.put(TrackDisplayColumns.ColumnNames.GENRE.getNameValue(), trkGenre);
        result.put(TrackDisplayColumns.ColumnNames.KIND.getNameValue(), trkKind);
        result.put(TrackDisplayColumns.ColumnNames.SIZE.getNameValue(), Integer.toString(trkSize));
        result.put(TrackDisplayColumns.ColumnNames.DURATION.getNameValue(),
                Utilities.convertMillisecondTime(trkDuration));

        /*
         * If year is 0 then it doesn't exist, so use a value of null so it won't be seen in
         * track details.
         */
        result.put(TrackDisplayColumns.ColumnNames.YEAR.getNameValue(),
                (trkYear > 0) ? Integer.toString(trkYear) : null);

        result.put(TrackDisplayColumns.ColumnNames.MODIFIED.getNameValue(),
                Utilities.formatDate(trkModified));
        result.put(TrackDisplayColumns.ColumnNames.ADDED.getNameValue(),
                Utilities.formatDate(trkDateAdded));
        result.put(TrackDisplayColumns.ColumnNames.BITRATE.getNameValue(),
                Integer.toString(trkBitRate));
        result.put(TrackDisplayColumns.ColumnNames.SAMPLERATE.getNameValue(),
                Integer.toString(trkSampleRate));
        result.put(TrackDisplayColumns.ColumnNames.PLAYCOUNT.getNameValue(),
                Integer.toString(trkPlayCount));

        /*
         * Release date is optional, so use null if it doesn't exist.
         */
        result.put(TrackDisplayColumns.ColumnNames.RELEASED.getNameValue(),
                (trkReleased != null) ? Utilities.formatDate(trkReleased) : null);

        result.put(TrackDisplayColumns.ColumnNames.RATING.getNameValue(),
                Integer.toString(trkRating / RATING_DIVISOR));

        /*
         * Create the string of playlist names and the corresponding bypassed indicators.
         */
        StringBuilder playlistsStr = new StringBuilder();
        StringBuilder bypassedStr = new StringBuilder();

        for (TrackPlaylistInfo playlistInfo : trkPlaylists)
        {
            if (playlistsStr.length() > 0)
            {
                playlistsStr.append(InternalConstants.LIST_ITEM_SEPARATOR);
                bypassedStr.append(InternalConstants.LIST_ITEM_SEPARATOR);
            }

            playlistsStr.append(playlistInfo.getPlaylistName());

            if (playlistInfo.getBypassed() == true)
            {
                bypassedStr.append("Y");
            }
            else
            {
                bypassedStr.append("N");
            }
        }

        result.put(PlaylistDisplayColumns.ColumnNames.PLAYLIST_NAMES.getNameValue(),
                playlistsStr.toString());
        result.put(PlaylistDisplayColumns.ColumnNames.BYPASSED.getNameValue(),
                bypassedStr.toString());

        result.put(TrackDisplayColumns.ColumnNames.NUMPLAYLISTS.getNameValue(),
                Integer.toString(trkPlaylists.getLength()));

        return result;
    }
}
