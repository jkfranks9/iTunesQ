package itunesq;

import java.util.Date;
import java.util.Iterator;

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

    //---------------- Class variables -------------------------------------
	
	/**
	 * Map key to obtain playlists for a track.
	 * <p>
	 * This is a column definition, but is not displayed as such. It holds 
	 * playlist names that are displayed on the right side of a split pane 
	 * when the results of a track query are displayed.
	 */
	public static final String MAP_PLAYLISTS  = "Playlists";
	
	/**
	 * Map key to obtain bypassed playlists for a track.
	 * <p>
	 * This is a column definition, but is not displayed as such.
	 */
	public static final String MAP_BYPASSED  = "Bypassed";
	
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
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * The rating value is the number of stars times 20, for some odd reason.
	 */
	private static final int RATING_DIVISOR = 20;
	
	/**
	 * Class constructor specifying the track ID.
	 * 
	 * @param ID track ID
	 */
	public Track (int ID)
	{
		trkID = ID;
		trkPlaylists = new ArrayList<TrackPlaylistInfo>();
	}
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the track ID.
	 * 
	 * @return track ID
	 */
	public int getID ()
	{
		return trkID;
	}

	/**
	 * Gets the track name.
	 * 
	 * @return track name
	 */
	public String getName ()
	{
		return trkName;
	}

	/**
	 * Sets the track name.
	 * 
	 * @param name track name
	 */
	public void setName (String name)
	{
		this.trkName = name;
	}

	/**
	 * Gets the artist name.
	 * 
	 * @return artist name
	 */
	public String getArtist ()
	{
		return trkArtist;
	}

	/**
	 * Sets the artist name.
	 * 
	 * @param artist artist name
	 */
	public void setArtist (String artist)
	{
		this.trkArtist = artist;
	}
	
	/**
	 * Gets the composer name.
	 * 
	 * @return composer name
	 */
	public String getComposer() {
		return trkComposer;
	}

	/**
	 * Sets the composer name.
	 * 
	 * @param composer composer name
	 */
	public void setComposer(String composer) {
		this.trkComposer = composer;
	}

	/**
	 * Gets the album name.
	 * 
	 * @return album name
	 */
	public String getAlbum ()
	{
		return trkAlbum;
	}

	/**
	 * Sets the album name.
	 * 
	 * @param album album name
	 */
	public void setAlbum (String album)
	{
		this.trkAlbum = album;
	}

	/**
	 * Gets the genre.
	 * 
	 * @return genre
	 */
	public String getGenre() {
		return trkGenre;
	}

	/**
	 * Sets the genre.
	 * @param genre genre
	 */
	public void setGenre(String genre) {
		this.trkGenre = genre;
	}

	/**
	 * Gets the kind of track.
	 * 
	 * @return kind of track
	 */
	public String getKind ()
	{
		return trkKind;
	}

	/**
	 * Sets the kind of track.
	 * 
	 * @param kind kind of track
	 */
	public void setKind (String kind)
	{
		this.trkKind = kind;
	}

	/**
	 * Gets the size of the track in bytes.
	 * 
	 * @return size of the track
	 */
	public int getSize() {
		return trkSize;
	}

	/**
	 * Sets the size of the track in bytes.
	 * 
	 * @param size size of the track
	 */
	public void setSize(int size) {
		this.trkSize = size;
	}

	/**
	 * Gets the duration of the track.
	 * 
	 * @return duration of the track
	 */
	public int getDuration ()
	{
		return trkDuration;
	}

	/**
	 * Sets the duration of the track.
	 * 
	 * @param duration duration of the track
	 */
	public void setDuration (int duration)
	{
		this.trkDuration = duration;
	}

	/**
	 * Gets the year of release.
	 * 
	 * @return year of release
	 */
	public int getYear ()
	{
		return trkYear;
	}

	/**
	 * Sets the year of release.
	 * 
	 * @param year year of release
	 */
	public void setYear (int year)
	{
		this.trkYear = year;
	}

	/**
	 * Gets the modification date.
	 * 
	 * @return modification date
	 */
	public Date getModified() {
		return trkModified;
	}

	/**
	 * Sets the modification date.
	 * 
	 * @param modified modification date
	 */
	public void setModified(Date modified) {
		this.trkModified = modified;
	}

	/**
	 * Gets the date the track was added.
	 * 
	 * @return date the track was added
	 */
	public Date getDateAdded ()
	{
		return trkDateAdded;
	}

	/**
	 * Sets the date the track was added.
	 * 
	 * @param dateAdded date the track was added
	 */
	public void setDateAdded (Date dateAdded)
	{
		this.trkDateAdded = dateAdded;
	}

	/**
	 * Gets the bit rate.
	 * 
	 * @return bit rate
	 */
	public int getBitRate() {
		return trkBitRate;
	}

	/**
	 * Sets the bit rate.
	 * 
	 * @param bitRate bit rate
	 */
	public void setBitRate(int bitRate) {
		this.trkBitRate = bitRate;
	}

	/**
	 * Gets the sample rate.
	 * 
	 * @return sample rate
	 */
	public int getSampleRate() {
		return trkSampleRate;
	}

	/**
	 * Sets the sample rate.
	 * 
	 * @param sampleRate sample rate
	 */
	public void setSampleRate(int sampleRate) {
		this.trkSampleRate = sampleRate;
	}

	/**
	 * Gets the play count.
	 * 
	 * @return play count
	 */
	public int getPlayCount() {
		return trkPlayCount;
	}

	/**
	 * Sets the play count.
	 * 
	 * @param playCount play count
	 */
	public void setPlayCount(int playCount) {
		this.trkPlayCount = playCount;
	}

	/**
	 * Gets the release date.
	 * 
	 * @return release date
	 */
	public Date getReleased() {
		return trkReleased;
	}

	/**
	 * Sets the release date.
	 * 
	 * @param released release date
	 */
	public void setReleased(Date released) {
		this.trkReleased = released;
	}

	/**
	 * Gets the track rating. This is the uncorrected rating as exists in the
	 * XML file.
	 * 
	 * @return track rating
	 */
	public int getRawRating ()
	{
		return trkRating;
	}

	/**
	 * Gets the corrected track rating. This is the rating on a scale of
	 * 0 to 5.
	 * 
	 * @return corrected track rating
	 */
	public int getCorrectedRating ()
	{
		return trkRating / RATING_DIVISOR;
	}

	/**
	 * Sets the track rating.
	 * 
	 * @param rating track rating
	 */
	public void setRating (int rating)
	{
		this.trkRating = rating;
	}
	
    //---------------- Public methods --------------------------------------

	/**
	 * Gets the track playlist count. This only includes playlists that are 
	 * not bypassed.
	 * 
	 * @return track playlist count
	 */
	public int getTrkPlaylistCount()
	{
		int playlistCount = 0;
		
		Iterator<TrackPlaylistInfo> trkPlaylistsIter = trkPlaylists.iterator();
		while (trkPlaylistsIter.hasNext())
		{
			TrackPlaylistInfo playlistInfo = trkPlaylistsIter.next();
			
			if (playlistInfo.getBypassed() == false)
			{
				playlistCount++;
			}
		}
		
		return playlistCount;
	}

	/**
	 * Adds playlist info to the track.
	 * 
	 * @param playlistInfo playlist info
	 */
	public void addPlaylistInfoToTrack(TrackPlaylistInfo playlistInfo)
	{
		this.trkPlaylists.add(playlistInfo);
	}

	/**
	 * Compares a given track to this track, used for sorting. We sort by 
	 * track name.
	 * 
	 * @param t2 track to be compared to this track
	 * @return negative value, zero, or positive value to indicate less than, 
	 * equal to, or greater than, respectively
	 */
	public int compareTo (Track t2)
	{
		return this.trkName.compareTo(t2.trkName);
	}
	
	/**
	 * Returns a hash map of the track attributes.
	 * 
	 * @param trackNum track number to include, if greater than 0
	 * @return hash map of the track attributes
	 */
	public HashMap<String, String> toDisplayMap (int trackNum)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		
		if (trackNum > 0)
		{
			result.put(TrackDisplayColumns.ColumnNames.NUMBER.getDisplayValue(), 
					Integer.toString(trackNum));
		}
		result.put(TrackDisplayColumns.ColumnNames.ID.getDisplayValue(), 
				Integer.toString(trkID));
		result.put(TrackDisplayColumns.ColumnNames.NAME.getDisplayValue(), 
				trkName);
		result.put(TrackDisplayColumns.ColumnNames.ARTIST.getDisplayValue(), 
				trkArtist);
		result.put(TrackDisplayColumns.ColumnNames.COMPOSER.getDisplayValue(), 
				trkComposer);
		result.put(TrackDisplayColumns.ColumnNames.ALBUM.getDisplayValue(), 
				trkAlbum);
		result.put(TrackDisplayColumns.ColumnNames.GENRE.getDisplayValue(), 
				trkGenre);
		result.put(TrackDisplayColumns.ColumnNames.KIND.getDisplayValue(), 
				trkKind);
		result.put(TrackDisplayColumns.ColumnNames.SIZE.getDisplayValue(), 
				Integer.toString(trkSize));
		result.put(TrackDisplayColumns.ColumnNames.DURATION.getDisplayValue(), 
				Utilities.convertMillisecondTime(trkDuration));
		
		/*
		 * If year is 0 then it doesn't exist, so use a value of null so it won't be seen in
		 * track details.
		 */
		result.put(TrackDisplayColumns.ColumnNames.YEAR.getDisplayValue(), 
				(trkYear > 0) ? Integer.toString(trkYear) : null);
		
		result.put(TrackDisplayColumns.ColumnNames.MODIFIED.getDisplayValue(), 
				Utilities.formatDate(trkModified));
		result.put(TrackDisplayColumns.ColumnNames.ADDED.getDisplayValue(), 
				Utilities.formatDate(trkDateAdded));
		result.put(TrackDisplayColumns.ColumnNames.BITRATE.getDisplayValue(), 
				Integer.toString(trkBitRate));
		result.put(TrackDisplayColumns.ColumnNames.SAMPLERATE.getDisplayValue(), 
				Integer.toString(trkSampleRate));
		result.put(TrackDisplayColumns.ColumnNames.PLAYCOUNT.getDisplayValue(), 
				Integer.toString(trkPlayCount));
		
		/*
		 * Release date is optional, so use null if it doesn't exist.
		 */
		result.put(TrackDisplayColumns.ColumnNames.RELEASED.getDisplayValue(), 
				(trkReleased != null) ? Utilities.formatDate(trkReleased) : null);
		
		result.put(TrackDisplayColumns.ColumnNames.RATING.getDisplayValue(), 
				Integer.toString(trkRating / RATING_DIVISOR));

		/*
		 * Create the string of playlist names and the corresponding bypassed indicators.
		 */
		StringBuilder playlistsStr = new StringBuilder();
		StringBuilder bypassedStr = new StringBuilder();
		
		Iterator<TrackPlaylistInfo> trkPlaylistsIter = trkPlaylists.iterator();
		while (trkPlaylistsIter.hasNext())
		{
			TrackPlaylistInfo playlistInfo = trkPlaylistsIter.next();
			
			if (playlistsStr.length() > 0)
			{
				playlistsStr.append(",");
				bypassedStr.append(",");
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
		
		result.put(MAP_PLAYLISTS, playlistsStr.toString());
		result.put(MAP_BYPASSED, bypassedStr.toString());
		
		return result;
	}
}
