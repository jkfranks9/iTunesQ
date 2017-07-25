package itunesq;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that represents a playlist tree.
 * <p>
 * There is one primary method, <code>createPlaylistTree</code>, that gathers 
 * the playlists into a form suitable for the <code>treeData</code> field of 
 * a <code>TreeView</code> class. This in turn consists of a 
 * <code>TreeBranch</code>, that is a list of items that are other 
 * <code>TreeBranch</code> lists, or are individual <code>TreeNode</code> 
 * objects, that represent leaves in the tree.
 * <p>
 * There is no limit to the level of nesting of <code>TreeBranch</code> 
 * objects in the tree (he says optimistically).
 * 
 * @author Jon
 *
 */
public final class PlaylistTree
{
	
    //---------------- Private variables -----------------------------------

	private static String className = PlaylistTree.class.getSimpleName();
	private static Logger logger = (Logger) LoggerFactory.getLogger(className + "_Playlist");
	private static Logging logging = Logging.getInstance();
	
	/*
	 * Map of Playlist objects, indexed by the persistent playlist ID.
	 */
	private static Map<String, Playlist> playlists = null;
	
	/**
	 * Class constructor.
	 */
	public PlaylistTree ()
	{
	}
	
    //---------------- Public methods --------------------------------------
	
	/**
	 * Initializes logging. This is called once at application initialization.
	 */
	public static void initializeLogging ()
	{
		logging.registerLogger(Logging.Dimension.PLAYLIST, logger);
	}
	
	/**
	 * Creates the tree of playlists.
	 * 
	 * @return tree branch of all playlists
	 */
	public static TreeBranch createPlaylistTree ()
	{
		logger.trace("createPlaylistTree");
		
		/*
		 * Initialize the top branch.
		 */
		TreeBranch topBranch = new TreeBranch();
        
        /*
         * Get the playlists into a map, and sort it by persistent ID.
         */
        playlists = XMLHandler.getPlaylists();
        playlists.setComparator(new Comparator<String>() 
        {
            @Override
            public int compare(String key1, String key2) 
            {
                return key1.compareTo(key2);
            }
        });
        
        /*
         * Now walk the map, and build the tree.
         */
        Iterator<String> playlistsIter = playlists.iterator();
        while (playlistsIter.hasNext())
        {
        	String playlistKey = playlistsIter.next();
        	Playlist playlistObj = playlists.get(playlistKey);
        	
        	/*
        	 * Ignore filtered out playlists.
        	 */
        	if (playlistObj.getIgnored() == true)
        	{
        		continue;
        	}
        	
        	/*
        	 * If the playlist has no parent, it's either a top-level playlist, or a vanilla
        	 * one.
        	 */
        	if (playlistObj.getParentPersistentID() == null)
        	{ 
        		logger.debug("found playlist without parent '" + playlistObj.getName() + "'");
        		
        		/*
        		 * For a top level playlist, we only want to add it if it doesn't already exist.
        		 * It may have been added already when we found a vanilla playlist with this one
        		 * as parent.
        		 */
            	if (playlistObj.getIsFolder() == true)
            	{
            		TreeBranch playlist = new TreeBranch(playlistObj.getName());
            		playlist.setUserData(playlistObj.getPersistentID());
            		if (indexOfBranchParent(topBranch, playlist) == -1)
            		{
            			logger.debug("adding top level playlist '" + playlistObj.getName() + "'");
            			topBranch.add(playlist);
            		}
            	}
            	
            	/*
            	 * Just add a vanilla playlist. We don't expect any duplicates, so don't check.
            	 */
            	else
            	{
        			logger.debug("adding vanilla playlist '" + playlistObj.getName() + "'");
            		TreeNode node = new TreeNode(playlistObj.getName());
            		node.setUserData(playlistObj.getPersistentID());
        			topBranch.add(node);
            	}
        	}
        	
        	/*
        	 * The playlist has a parent, so add or update the parent, recursively.
        	 */
        	else
        	{
        		addOrUpdateParent(topBranch, playlistObj);
        	}
        }
        
        /*
         * Sort and return the top branch.
         */
        topBranch.setComparator(new TreeNodeComparator());
        return topBranch;
	}
	
    //---------------- Private methods -------------------------------------
	
	/*
	 * Recursively add or update a parent branch with a playlist.
	 */
	private static TreeBranch addOrUpdateParent (TreeBranch enclosingBranch, Playlist playlistObj)
	{
		logger.trace("addOrUpdateParent");
		
		TreeBranch searchBranch = enclosingBranch;
		
		/*
		 * Find the parent playlist in the map.
		 */
		Playlist parent = playlists.get(playlistObj.getParentPersistentID());
		
		/*
		 * If the parent itself also has a parent, recurse. Upon return, the intermediate
		 * parent has been added to its parent. Update the search branch to be that of 
		 * the intermediate parent so we can add this playlist to the intermediate parent.
		 */
		if (parent.getParentPersistentID() != null)
		{
			logger.debug("recursing for playlist '" + playlistObj.getName() + "'");
        	searchBranch = addOrUpdateParent(searchBranch, parent);
		}
		
		/*
		 * Create a node or branch to represent this playlist and a branch to represent the parent.
		 */
		TreeNode node;
		if (playlistObj.getIsFolder() == false)
		{
			node = new TreeNode(playlistObj.getName());
		}
		else
		{
			node = new TreeBranch(playlistObj.getName());
		}
		node.setUserData(playlistObj.getPersistentID());
		TreeBranch parentBranch = new TreeBranch(parent.getName());
		parentBranch.setUserData(playlistObj.getPersistentID());
		
		/*
		 * If the parent branch does not exist in the search branch, then add it.
		 */
		int parentIndex = indexOfBranchParent(searchBranch, parentBranch);
		if (parentIndex == -1)
		{
			logger.debug("adding playlist '" + playlistObj.getName() + "'");
    		parentBranch.add(node);
			logger.debug("adding parent playlist '" + parent.getName() + "'");
    		searchBranch.add(parentBranch);
		}
		
		/*
		 * The parent exists in the search branch.
		 */
		else
		{
			
			/*
			 * Get the parent branch.
			 */
			parentBranch = (TreeBranch) searchBranch.get(parentIndex);
			
			/*
			 * If the playlist node we're working on is a folder, then we only want to add it 
			 * to the parent if it doesn't already exist.
			 */
			if (node instanceof TreeBranch)
			{
				int nodeIndex = indexOfBranchParent(parentBranch, (TreeBranch) node);
				if (nodeIndex == -1)
				{
        			logger.debug("adding playlist '" + playlistObj.getName() + "'");
					parentBranch.add(node);
					parentBranch.setComparator(new TreeNodeComparator());
				}
			}
			
			/*
			 * Just add a vanilla playlist.
			 */
			else
			{
    			logger.debug("adding playlist '" + playlistObj.getName() + "'");
				parentBranch.add(node);
				parentBranch.setComparator(new TreeNodeComparator());
			}
		}
		
		return parentBranch;
	}

	/*
	 * Get the index of a parent branch from a given branch, if it exists.
	 */
    private static int indexOfBranchParent (TreeBranch searchBranch, TreeBranch parentBranch) 
    {
		logger.trace("indexOfBranchParent");
		
        int index = 0;
        
        /*
         * Loop through the search branch.
         */
        int length = searchBranch.getLength();
        while (index < length) 
        {
        	
        	/*
        	 * We only care if the playlist name matches. Nothing else in playlist matters.
        	 */
            TreeNode node = searchBranch.get(index);
            if (parentBranch.getText().equals(node.getText())) 
            {
            	break;
            }

            index++;
        }

        /*
         * We did not find a match.
         */
        if (index == length) 
        {
            index = -1;
        }

        return index;
    }
	
    //---------------- Nested classes --------------------------------------

    /*
     * Class to compare TreeNode instances by name.
     */
    private static final class TreeNodeComparator implements Comparator<TreeNode>, Serializable 
    {
        private static final long serialVersionUID = 1L;

        public TreeNodeComparator () 
        {
        }

        @Override
        public int compare (TreeNode treeNode1, TreeNode treeNode2) 
        {
            String text1 = treeNode1.getText();
            String text2 = treeNode2.getText();

            int result;

            if (text1 == null && text2 == null) 
            {
                result = 0;
            } 
            else if (text1 == null) 
            {
                result = -1;
            } 
            else if (text2 == null) 
            {
                result = 1;
            } 
            else 
            {
                result = text1.compareTo(text2);
            }

            return result;
        }
    }
}
