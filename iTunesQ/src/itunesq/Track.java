package itunesq;

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

    //---------------- Class variables -------------------------------------
	
	/**
	 * Definition of the map key to obtain playlists for a track, used in TracksWindow.
	 * 
	 * NOTE: We can have more columns than are displayed. The MAP_PLAYLISTS column is not displayed 
	 * as such. It holds playlist names that are displayed on the right side of a split pane when
	 * the results of a filter are displayed.
	 */
	public static final String MAP_PLAYLISTS  = "playlists";
	
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
	private List<String> trkPlaylists;
	
    //---------------- Private variables -----------------------------------
	
	/*
	 * The rating value is the number of stars times 20, for some odd reason.
	 */
	private static final int RATING_DIVISOR = 20;
	
	/**
	 * Constructor.
	 * 
	 * @param ID Track ID.
	 */
	public Track (int ID)
	{
		trkID = ID;
		trkPlaylists = new ArrayList<String>();
	}
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Get the track ID.
	 * 
	 * @return Track ID.
	 */
	public int getID ()
	{
		return trkID;
	}

	/**
	 * Get the track name.
	 * 
	 * @return Track name.
	 */
	public String getName ()
	{
		return trkName;
	}

	/**
	 * Set the track name.
	 * 
	 * @param name Track name.
	 */
	public void setName (String name)
	{
		this.trkName = name;
	}

	/**
	 * Get the artist name.
	 * 
	 * @return Artist name.
	 */
	public String getArtist ()
	{
		return trkArtist;
	}

	/**
	 * Set the artist name.
	 * 
	 * @param name Artist name.
	 */
	public void setArtist (String artist)
	{
		this.trkArtist = artist;
	}
	
	public String getComposer() {
		return trkComposer;
	}

	public void setComposer(String composer) {
		this.trkComposer = composer;
	}

	/**
	 * Get the album name.
	 * 
	 * @return Album name.
	 */
	public String getAlbum ()
	{
		return trkAlbum;
	}

	/**
	 * Set the album name.
	 * 
	 * @param name Album name.
	 */
	public void setAlbum (String album)
	{
		this.trkAlbum = album;
	}

	public String getGenre() {
		return trkGenre;
	}

	public void setGenre(String genre) {
		this.trkGenre = genre;
	}

	/**
	 * Get the kind of track.
	 * 
	 * @return Track kind.
	 */
	public String getKind ()
	{
		return trkKind;
	}

	/**
	 * Set the kind of track.
	 * 
	 * @param name Track kind.
	 */
	public void setKind (String kind)
	{
		this.trkKind = kind;
	}

	public int getSize() {
		return trkSize;
	}

	public void setSize(int size) {
		this.trkSize = size;
	}

	/**
	 * Get the duration of the track.
	 * 
	 * @return Duration of the track.
	 */
	public int getDuration ()
	{
		return trkDuration;
	}

	/**
	 * Set the duration of the track.
	 * 
	 * @param name Duration of the track.
	 */
	public void setDuration (int duration)
	{
		this.trkDuration = duration;
	}

	/**
	 * Get the track year of release.
	 * 
	 * @return Year of release.
	 */
	public int getYear ()
	{
		return trkYear;
	}

	/**
	 * Set the track year of release.
	 * 
	 * @param name Year of release.
	 */
	public void setYear (int year)
	{
		this.trkYear = year;
	}

	public Date getModified() {
		return trkModified;
	}

	public void setModified(Date modified) {
		this.trkModified = modified;
	}

	/**
	 * Get the date the track was added.
	 * 
	 * @return the Date the track was added.
	 */
	public Date getDateAdded ()
	{
		return trkDateAdded;
	}

	/**
	 * Set the date the track was added.
	 * 
	 * @param name Date the track was added.
	 */
	public void setDateAdded (Date dateAdded)
	{
		this.trkDateAdded = dateAdded;
	}

	public int getBitRate() {
		return trkBitRate;
	}

	public void setBitRate(int bitRate) {
		this.trkBitRate = bitRate;
	}

	public int getSampleRate() {
		return trkSampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.trkSampleRate = sampleRate;
	}

	public int getPlayCount() {
		return trkPlayCount;
	}

	public void setPlayCount(int playCount) {
		this.trkPlayCount = playCount;
	}

	public Date getReleased() {
		return trkReleased;
	}

	public void setReleased(Date released) {
		this.trkReleased = released;
	}

	/**
	 * Get the raw track rating. This is the uncorrected rating as exists in the XML file.
	 * 
	 * @return Track rating.
	 */
	public int getRawRating ()
	{
		return trkRating;
	}

	/**
	 * Get the corrected track rating. This is the rating divided by RATING_DIVISOR.
	 * 
	 * @return Track rating.
	 */
	public int getCorrectedRating ()
	{
		return trkRating / RATING_DIVISOR;
	}

	/**
	 * Set the track rating.
	 * 
	 * @param name track rating.
	 */
	public void setRating (int rating)
	{
		this.trkRating = rating;
	}

	/**
	 * Get the track playlist count.
	 * 
	 * @return The track playlist count.
	 */
	public int getTrkPlaylistCount()
	{
		return trkPlaylists.getLength();
	}

	/**
	 * Add a playlist to the track.
	 */
	public void addPlaylistToTrack(String playlistName)
	{
		this.trkPlaylists.add(playlistName);
	}
	
    //---------------- Public methods --------------------------------------

	/**
	 * Comparison method for itqTrack, used for sorting. We sort by track name.
	 * 
	 * @param t2 Second track to be compared to this track.
	 * @return Negative integer, zero, or positive integer as this is less than, equal to, or greater than the input.
	 */
	public int compareTo (Track t2)
	{
		return this.trkName.compareTo(t2.trkName);
	}
	
	/**
	 * Return a HashMap of the track attributes.
	 * 
	 * @param trackNum Track number to include, if > 0.
	 * @return HashMap of the track attributes.
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
		result.put(TrackDisplayColumns.ColumnNames.YEAR.getDisplayValue(), 
				(trkYear > 0) ? Integer.toString(trkYear) : "");
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
		result.put(TrackDisplayColumns.ColumnNames.RELEASED.getDisplayValue(), 
				Utilities.formatDate(trkReleased));
		result.put(TrackDisplayColumns.ColumnNames.RATING.getDisplayValue(), 
				Integer.toString(trkRating / RATING_DIVISOR));
		
		String playlistsStr = trkPlaylists.toString();
		int startIndex = playlistsStr.indexOf("[");
		int stopIndex = playlistsStr.indexOf("]");
		String playlists = playlistsStr.substring(startIndex + 1, stopIndex);
		
		result.put(MAP_PLAYLISTS, playlists);
		
		return result;
	}
}
