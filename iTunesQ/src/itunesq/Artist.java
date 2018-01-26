package itunesq;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;

/**
 * Class that represents an artist.
 * 
 * @author Jon
 *
 */
public class Artist
{

    //---------------- Class variables -------------------------------------
	
	private String keyName;
	private String displayName;
	private List<String> altNames;
	private Integer numLocalTracks;
	private Integer numRemoteTracks;
	private Integer totalLocalTime;
	private Integer totalRemoteTime;
	private RemoteArtistControl remoteArtistControl;
	
	/**
	 * Control that allows us to keep track of the number of artists that 
	 * contain only remote tracks. Such artists should not be counted if
	 * remote tracks are not being shown.
	 */
	public enum RemoteArtistControl
	{
		
		/**
		 * artist does not contain remote tracks 
		 */
		NO_REMOTE,
		
		/**
		 * artist contains only remote tracks
		 */
		REMOTE,
		
		/**
		 * artist contains local and remote tracks
		 */
		REMOTE_AND_LOCAL;
	}
	
	/**
	 * Class constructor specifying the key name (lower case artist name).
	 */
	public Artist (String keyName)
	{
		this.keyName = keyName;
		altNames = new ArrayList<String>();
		numLocalTracks = 0;
		numRemoteTracks = 0;
		totalLocalTime = 0;
		totalRemoteTime = 0;
		remoteArtistControl = RemoteArtistControl.NO_REMOTE;
	}
	
    //---------------- Getters and setters ---------------------------------
	
	/**
	 * Gets the key name. This is the artist name in lower case.
	 * 
	 * @return lower case artist name
	 */
	public String getKeyName()
	{
		return keyName;
	}
	
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
	 * Gets the list of alternate artist names. This is a list of the artist 
	 * names in any case.
	 * 
	 * @return list of alternate artist names
	 */
	public List<String> getAltNames()
	{
		return altNames;
	}
	
	/**
	 * Gets the number of local tracks for this artist.
	 * 
	 * @return number of local tracks
	 */
	public Integer getNumLocalTracks()
	{
		return numLocalTracks;
	}
	
	/**
	 * Gets the number of remote tracks for this artist.
	 * 
	 * @return number of remote tracks
	 */
	public Integer getNumRemoteTracks()
	{
		return numRemoteTracks;
	}
	
	/**
	 * Gets the total time of local tracks for this artist.
	 * 
	 * @return total time of local tracks
	 */
	public Integer getTotalLocalTime()
	{
		return totalLocalTime;
	}
	
	/**
	 * Gets the total time of remote tracks for this artist.
	 * 
	 * @return total time of remote tracks
	 */
	public Integer getTotalRemoteTime()
	{
		return totalRemoteTime;
	}
	
	/**
	 * Gets the remote artist control. This is used to remember that we've 
	 * accounted for this artist containing both local and remote tracks, and 
	 * have adjusted the count of remote-only artists accordingly.
	 * 
	 * @return remote artist count adjusted flag
	 */
	public RemoteArtistControl getRemoteArtistControl ()
	{
		return remoteArtistControl;
	}
	
	/**
	 * Sets the remote artist control. This is used to remember that we've 
	 * accounted for this artist containing both local and remote tracks, and 
	 * have adjusted the count of remote-only artists accordingly.
	 * 
	 * @param remoteArtistControl remote artist control
	 */
	public void setRemoteArtistControl (RemoteArtistControl remoteArtistControl)
	{
		this.remoteArtistControl = remoteArtistControl;
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Adds a new track to this artist. This involves updating the track and
	 * total time values, as well as updating the list of alternate case 
	 * spellings for the artist name.
	 * 
	 * @param track track object
	 */
	public void addTrackToArtist (Track track)
	{
		String artistName = track.getArtist();
		
		/*
		 * Determine if this spelling of the artist name already exists in the alternate names list.
		 */
		boolean foundName = false;
		for (String altName : altNames)
		{
			if (artistName.equals(altName))
			{
				foundName = true;
			}
		}
		
		/*
		 * If this is a new alternate spelling, add it to the list. Also, use the first such spelling 
		 * found as the display name for the artist.
		 */
		if (foundName == false)
		{
			altNames.add(artistName);
			if (displayName == null || displayName.isEmpty())
			{
				displayName = artistName;
			}
		}
		
		/*
		 * Update the local or remote track counts and total time.
		 */
		if (track.getRemote() == false)
		{
			numLocalTracks++;
			totalLocalTime += track.getDuration();
		}
		else
		{
			numRemoteTracks++;
			totalRemoteTime += track.getDuration();
		}
	}
	
	/**
	 * Returns a hash map of the artist attributes.
	 * 
	 * @return hash map of the artist attributes
	 */
	public HashMap<String, String> toDisplayMap ()
	{
		HashMap<String, String> result = new HashMap<String, String>();
		
		result.put(ArtistDisplayColumns.ColumnNames.ARTIST.getNameValue(), 
				displayName);
		result.put(ArtistDisplayColumns.ColumnNames.NUM_ALTNAMES.getNameValue(), 
				Integer.toString(altNames.getLength() - 1));
		result.put(ArtistDisplayColumns.ColumnNames.LOCAL_NUM_TRACKS.getNameValue(), 
				Integer.toString(numLocalTracks));
		result.put(ArtistDisplayColumns.ColumnNames.LOCAL_TOTAL_TIME.getNameValue(), 
				Utilities.convertMillisecondTime(totalLocalTime));
		result.put(ArtistDisplayColumns.ColumnNames.REMOTE_NUM_TRACKS.getNameValue(), 
				Integer.toString(numRemoteTracks));
		result.put(ArtistDisplayColumns.ColumnNames.REMOTE_TOTAL_TIME.getNameValue(), 
				Utilities.convertMillisecondTime(totalRemoteTime));
		
		return result;
	}
}
